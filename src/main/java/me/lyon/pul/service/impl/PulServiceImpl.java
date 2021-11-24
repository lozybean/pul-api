package me.lyon.pul.service.impl;

import lombok.extern.slf4j.Slf4j;
import me.lyon.pul.model.mapper.PulMapper;
import me.lyon.pul.model.po.PulPO;
import me.lyon.pul.model.entity.NameCount;
import me.lyon.pul.model.entity.PageData;
import me.lyon.pul.model.entity.PulInfo;
import me.lyon.pul.model.vo.PulListVO;
import me.lyon.pul.repository.PulRepository;
import me.lyon.pul.service.PulService;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import javax.persistence.criteria.*;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@CacheConfig(cacheNames = {"pulInfo", "pulInfoList", "pulInfoPage", "aggregateByType", "aggregateByPhylum"})
@Service
public class PulServiceImpl implements PulService {
    private static final Set<String> MAIN_PHYLUM_LIST = Set.of("Actinobacteria", "Bacteroidetes", "Firmicutes", "Proteobacteria");
    private static final String SPECIES_PROPS = "species";
    private static final String OTHER_PHYLUM_EXPRESS = "other";

    @Resource
    PulRepository pulRepository;

    static Map<String, String> sortFieldMap = Map.of(
            "id", "id",
            "pul_id", "pulId",
            "pul_type", "type",
            "assembly_accession", "species.gcfNumber",
            "species", "species.spSpecies",
            "phylum", "species.spPhylum"
    );
    static Map<String, String> nativeSortFieldMap = Map.of(
            "id", "id",
            "pul_id", "pul_id",
            "pul_type", "type",
            "assembly_accession", "species.gcf_number",
            "species", "species.species",
            "phylum", "species.phylum"
    );


    @Cacheable(cacheNames = "pulInfo", key = "#id")
    @Override
    public Optional<PulInfo> queryById(String id) {
        return pulRepository.findById(id)
                .map(PulMapper.INSTANCE::pulInfo)
                .map(pul -> {
                    pul.setContent(pul.getContent().stream().sorted().collect(Collectors.toList()));
                    return pul;
                });
    }

    private Sort mapSort(Sort sort, Map<String, String> fieldMap) {
        List<Sort.Order> newOrders = new ArrayList<>();
        for (Sort.Order order : sort) {
            log.info("SORT by: {}", fieldMap.get(order.getProperty()));
            newOrders.add(new Sort.Order(order.getDirection(), fieldMap.get(order.getProperty())));
        }
        if (newOrders.isEmpty()) {
            return Sort.by("id");
        } else {
            return Sort.by(newOrders.toArray(Sort.Order[]::new));
        }
    }

    private Pageable mapPageable(Pageable pageable) {
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), mapSort(pageable.getSort(), sortFieldMap));
    }

    private Pageable mapNativePageable(Pageable pageable) {
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), mapSort(pageable.getSort(), nativeSortFieldMap));
    }

    @Cacheable(cacheNames = "pulInfoPage")
    @Override
    public PageData<PulListVO> queryPulByType(String pulType, Pageable pageable) {
        pageable = mapPageable(pageable);
        Page<PulPO> pulPoPage = pulRepository.findAllByTypeIgnoreCase(pulType, pageable);
        return PageData.<PulListVO>builder()
                .list(pulPoPage.getContent()
                        .stream()
                        .map(PulMapper.INSTANCE::pulListVO)
                        .collect(Collectors.toList()))
                .total((int) pulPoPage.getTotalElements())
                .build();
    }

    @Cacheable(cacheNames = "pulInfoList")
    @Override
    public List<PulListVO> queryPulByType(String pulType) {
        if (pulType.isBlank()) {
            return new LinkedList<>();
        }
        List<PulPO> pulPos = pulRepository.findAllByTypeIgnoreCaseOrderById(pulType);
        return pulPos.stream()
                .map(PulMapper.INSTANCE::pulListVO)
                .collect(Collectors.toList());
    }

    private Predicate buildCriteriaByLinage(Root<PulPO> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder,
                                            Integer taxonomyId, String assemblyAccession, String spSpecies, String spPhylum
    ) {
        List<Predicate> list = new ArrayList<>();
        if (Objects.nonNull(taxonomyId)) {
            list.add(criteriaBuilder.equal(root.get(SPECIES_PROPS).get("taxid").as(Integer.class), taxonomyId));
        }
        if (!StringUtils.isEmpty(assemblyAccession)) {
            Expression<String> lower = criteriaBuilder.lower(root.get(SPECIES_PROPS).get("gcfNumber").as(String.class));
            list.add(criteriaBuilder.equal(lower, assemblyAccession.toLowerCase()));
        }
        if (!StringUtils.isEmpty(spSpecies)) {
            Expression<String> lower = criteriaBuilder.lower(root.get(SPECIES_PROPS).get("spSpecies").as(String.class));
            list.add(criteriaBuilder.like(lower, "%" + spSpecies.toLowerCase() + "%"));
        }
        if (!StringUtils.isEmpty(spPhylum)) {
            if (OTHER_PHYLUM_EXPRESS.equals(spPhylum)) {
                final CriteriaBuilder.In<String> in = criteriaBuilder.in(root.get(SPECIES_PROPS).get("spPhylum").as(String.class));
                MAIN_PHYLUM_LIST.forEach(in::value);
                list.add(criteriaBuilder.not(in));
            } else {
                Expression<String> lower = criteriaBuilder.lower(root.get(SPECIES_PROPS).get("spPhylum").as(String.class));
                list.add(criteriaBuilder.like(lower, "%" + spPhylum.toLowerCase() + "%"));
            }
        }
        return criteriaBuilder.and(list.toArray(new Predicate[0]));
    }

    @Cacheable(cacheNames = "pulInfoPage")
    @Override
    public PageData<PulListVO> queryPulByLinage(Integer taxonomyId, String assemblyAccession, String spSpecies, String spPhylum, Pageable pageable) {
        pageable = mapPageable(pageable);
        Page<PulPO> pulPoPage = pulRepository.findAll((Specification<PulPO>) (root, criteriaQuery, criteriaBuilder) ->
                this.buildCriteriaByLinage(root, criteriaQuery, criteriaBuilder,
                        taxonomyId, assemblyAccession, spSpecies, spPhylum), pageable);
        return PageData.<PulListVO>builder()
                .list(pulPoPage.getContent()
                        .stream()
                        .map(PulMapper.INSTANCE::pulListVO)
                        .collect(Collectors.toList()))
                .total((int) pulPoPage.getTotalElements())
                .build();
    }

    @Cacheable(cacheNames = "pulInfoList")
    @Override
    public List<PulListVO> queryPulByLinage(Integer taxonomyId, String assemblyAccession, String spSpecies, String spPhylum) {
        if (Objects.isNull(taxonomyId) && assemblyAccession.isBlank() && spSpecies.isBlank() && spPhylum.isBlank()) {
            return new LinkedList<>();
        }
        List<PulPO> pulPos = pulRepository.findAll((Specification<PulPO>) (root, criteriaQuery, criteriaBuilder) ->
                this.buildCriteriaByLinage(root, criteriaQuery, criteriaBuilder,
                        taxonomyId, assemblyAccession, spSpecies, spPhylum), Sort.by("id"));
        return pulPos.stream()
                .map(PulMapper.INSTANCE::pulListVO)
                .collect(Collectors.toList());
    }

    @Cacheable(cacheNames = "pulInfoPage")
    @Override
    public PageData<PulListVO> queryPulByDomainName(String domainName, Pageable pageable) {
        pageable = mapNativePageable(pageable);
        Page<PulPO> pulPoPage = pulRepository.findAllByDomain(domainName.toLowerCase(), pageable);
        return PageData.<PulListVO>builder()
                .list(pulPoPage.getContent()
                        .stream()
                        .map(PulMapper.INSTANCE::pulListVO)
                        .collect(Collectors.toList()))
                .total((int) pulPoPage.getTotalElements())
                .build();
    }

    @Cacheable(cacheNames = "pulInfoList")
    @Override
    public List<PulListVO> queryPulByDomainName(String domainName) {
        if (domainName.isBlank()) {
            return new LinkedList<>();
        }
        List<PulPO> pulPos = pulRepository.findAllByDomain(domainName.toLowerCase());
        return pulPos.stream()
                .map(PulMapper.INSTANCE::pulListVO)
                .collect(Collectors.toList());
    }

    @Cacheable(cacheNames = "aggregateByType")
    @Override
    public List<NameCount> aggregateByType() {
        return pulRepository.countTotalPulPOSByTypeClass()
                .stream()
                .filter(o -> Objects.nonNull(o.getName()))
                .sorted(Comparator.reverseOrder())
                .collect(Collectors.toList());
    }

    @Cacheable(cacheNames = "aggregateByPhylum")
    @Override
    public List<NameCount> aggregateByPhylum() {
        return pulRepository.countTotalPulPOSBySpeciesSpPhylumClass()
                .stream()
                .filter(o -> Objects.nonNull(o.getName()))
                .sorted(Comparator.reverseOrder())
                .collect(Collectors.toList());
    }
}
