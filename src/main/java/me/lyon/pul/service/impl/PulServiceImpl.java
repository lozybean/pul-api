package me.lyon.pul.service.impl;

import me.lyon.pul.model.mapper.PulMapper;
import me.lyon.pul.model.po.PulPO;
import me.lyon.pul.model.vo.PageData;
import me.lyon.pul.model.vo.PulInfo;
import me.lyon.pul.repository.PulRepository;
import me.lyon.pul.service.PulService;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import java.util.*;
import java.util.stream.Collectors;

@CacheConfig(cacheNames = {"pulInfo", "pulInfoPage"})
@Service
public class PulServiceImpl implements PulService {
    private static final Set<String> MAIN_PHYLUM_LIST = Set.of("Actinobacteria", "Bacteroidetes", "Firmicutes", "Proteobacteria");

    @Resource
    PulRepository pulRepository;

    @Cacheable(cacheNames = "pulInfo", key = "#id")
    @Override
    public Optional<PulInfo> queryById(String id) {
        return pulRepository.findById(id)
                .map(PulMapper.INSTANCE::pulInfo);
    }

    @Cacheable(cacheNames = "pulInfoPage")
    @Override
    public PageData<PulInfo> queryPulByType(String pulType, Pageable pageable) {
        Page<PulPO> pulPoPage = pulRepository.findAllByTypeIgnoreCase(pulType, pageable);
        return PageData.<PulInfo>builder()
                .list(pulPoPage.getContent()
                        .stream()
                        .map(PulMapper.INSTANCE::pulInfo)
                        .collect(Collectors.toList()))
                .total((int) pulPoPage.getTotalElements())
                .build();
    }

    @Cacheable(cacheNames = "pulInfoPage")
    @Override
    public PageData<PulInfo> queryPulByLinage(Integer taxonomyId, String assemblyAccession, String spSpecies, String spPhylum, Pageable pageable) {
        final String speciesProp = "species";
        final String otherPhylumExpress = "other";

        Page<PulPO> pulPoPage = pulRepository.findAll((Specification<PulPO>) (root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> list = new ArrayList<>();
            if (Objects.nonNull(taxonomyId)) {
                list.add(criteriaBuilder.equal(root.get(speciesProp).get("taxid").as(Integer.class), taxonomyId));
            }
            if (!StringUtils.isEmpty(assemblyAccession)) {
                Expression<String> lower = criteriaBuilder.lower(root.get(speciesProp).get("gcfNumber").as(String.class));
                list.add(criteriaBuilder.equal(lower, assemblyAccession.toLowerCase()));
            }
            if (!StringUtils.isEmpty(spSpecies)) {
                Expression<String> lower = criteriaBuilder.lower(root.get(speciesProp).get("spSpecies").as(String.class));
                list.add(criteriaBuilder.equal(lower, spSpecies.toLowerCase()));
            }
            if (!StringUtils.isEmpty(spPhylum)) {
                if (otherPhylumExpress.equals(spPhylum)) {
                    final CriteriaBuilder.In<String> in = criteriaBuilder.in(root.get(speciesProp).get("spPhylum").as(String.class));
                    MAIN_PHYLUM_LIST.forEach(in::value);
                    list.add(criteriaBuilder.not(in));
                } else {
                    Expression<String> lower = criteriaBuilder.lower(root.get(speciesProp).get("spPhylum").as(String.class));
                    list.add(criteriaBuilder.equal(lower, spPhylum.toLowerCase()));
                }
            }
            return criteriaBuilder.and(list.toArray(new Predicate[0]));
        }, pageable);
        return PageData.<PulInfo>builder()
                .list(pulPoPage.getContent()
                        .stream()
                        .map(PulMapper.INSTANCE::pulInfo)
                        .collect(Collectors.toList()))
                .total((int) pulPoPage.getTotalElements())
                .build();
    }

    @Cacheable(cacheNames = "pulInfoPage")
    @Override
    public PageData<PulInfo> queryPulByDomainName(String domainName, Pageable pageable) {
        Page<PulPO> pulPoPage = pulRepository.findAllByDomain(domainName.toLowerCase(), pageable);
        return PageData.<PulInfo>builder()
                .list(pulPoPage.getContent()
                        .stream()
                        .map(PulMapper.INSTANCE::pulInfo)
                        .collect(Collectors.toList()))
                .total((int) pulPoPage.getTotalElements())
                .build();
    }


}
