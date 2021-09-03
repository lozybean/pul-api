package me.lyon.pul.service;

import me.lyon.pul.model.entity.NameCount;
import me.lyon.pul.model.entity.PageData;
import me.lyon.pul.model.entity.PulInfo;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;


public interface PulService {
    /**
     * 通过id查询pul
     *
     * @param id :
     * @return :
     */
    Optional<PulInfo> queryById(String id);

    /**
     * 通过pul type 检索PUL
     *
     * @param pulType  :
     * @param pageable : 分页信息
     * @return :
     */
    PageData<PulInfo> queryPulByType(String pulType, Pageable pageable);

    /**
     * 通过物种信息 检索PUL
     *
     * @param taxonomyId        : taxid
     * @param assemblyAccession : aka gcf number
     * @param spSpecies         : species name
     * @param spPhylum          : phylum name
     * @param pageable          : 分页信息
     * @return :
     */
    PageData<PulInfo> queryPulByLinage(Integer taxonomyId,
                                       String assemblyAccession,
                                       String spSpecies,
                                       String spPhylum,
                                       Pageable pageable);

    /**
     * 通过 gene.domain 检索PUL
     *
     * @param domainName : domain
     * @param pageable   : 分页信息
     * @return :
     */
    PageData<PulInfo> queryPulByDomainName(String domainName, Pageable pageable);

    /**
     * 根据类型聚合
     *
     * @return :
     */
    List<NameCount> aggregateByType();

    /**
     * 根据物种门聚合
     *
     * @return :
     */
    List<NameCount> aggregateByPhylum();
}
