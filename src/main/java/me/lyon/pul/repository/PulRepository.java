package me.lyon.pul.repository;

import me.lyon.pul.model.po.PulPO;
import me.lyon.pul.model.vo.NameCount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;


public interface PulRepository extends JpaRepository<PulPO, String>, JpaSpecificationExecutor<PulPO> {
    /**
     * select * from pul where type = %type
     *
     * @param type     : pul type
     * @param pageable : 分页信息
     * @return :
     */
    Page<PulPO> findAllByTypeIgnoreCase(String type, Pageable pageable);

    /**
     * 通过 gene.domain 检索
     *
     * @param domain   :
     * @param pageable :
     * @return ：
     */
    @Query(value = "select * from public.pul pul " +
            "inner join public.gene g on pul.id = g.pul_id " +
            "where lower(g.domain\\:\\:varchar)\\:\\:varchar[] @> ARRAY[?1]\\:\\:varchar[]", nativeQuery = true)
    Page<PulPO> findAllByDomain(String domain, Pageable pageable);

    /**
     * 通过类型聚合
     *
     * @return :
     */
    @Query(value = "select new me.lyon.pul.model.vo.NameCount(p.type, count(p)) from PulPO p group by p.type")
    List<NameCount> countTotalPulPOSByTypeClass();

    /**
     * 通过物种门聚合
     *
     * @return :
     */
    @Query(value = "select new  me.lyon.pul.model.vo.NameCount(p.species.spPhylum, count(distinct p.id))" +
            "from PulPO p group by p.species.spPhylum")
    List<NameCount> countTotalPulPOSBySpeciesSpPhylumClass();
}
