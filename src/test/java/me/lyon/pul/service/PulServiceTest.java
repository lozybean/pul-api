package me.lyon.pul.service;

import me.lyon.pul.model.entity.PageData;
import me.lyon.pul.model.vo.PulListVO;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class PulServiceTest {
    private final Pageable pageable = PageRequest.of(0, 10);

    @Resource
    PulService service;

    @Test
    public void queryPulByType() {
        PageData<PulListVO> pulInfoPage = service.queryPulByType("agar", pageable);
        Assert.assertEquals(186, pulInfoPage.getTotal().intValue());

        List<PulListVO> pulInfos = service.queryPulByType("agar");
        Assert.assertEquals(186, pulInfos.size());
        Assert.assertEquals(pulInfoPage.getList(), pulInfos.subList(0, 10));
    }

    @Test
    public void queryPulByLinage() {
        // 1. query by tax id
        PageData<PulListVO> pulInfoPageData = service.queryPulByLinage(203122, null, null, null, pageable);
        Assert.assertEquals(3, pulInfoPageData.getTotal().intValue());

        List<PulListVO> pulInfos = service.queryPulByLinage(203122, null, null, null);
        Assert.assertEquals(3, pulInfos.size());
        Assert.assertEquals(pulInfoPageData.getList(), pulInfos);

        // 2. query by assembly accession
        pulInfoPageData = service.queryPulByLinage(null, "GCF_000013665", null, null, pageable);
        Assert.assertEquals(3, pulInfoPageData.getTotal().intValue());

        pulInfos = service.queryPulByLinage(null, "GCF_000013665", null, null);
        Assert.assertEquals(3, pulInfos.size());
        Assert.assertEquals(pulInfoPageData.getList(), pulInfos);

        // 3. query by species
        pulInfoPageData = service.queryPulByLinage(null, null, "Clostridioides difficile QCD-66c26", null, pageable);
        Assert.assertEquals(1, pulInfoPageData.getTotal().intValue());

        pulInfos = service.queryPulByLinage(null, null, "Clostridioides difficile QCD-66c26", null);
        Assert.assertEquals(1, pulInfos.size());
        Assert.assertEquals(pulInfoPageData.getList(), pulInfos);

        // 4. query by phylum
        pulInfoPageData = service.queryPulByLinage(null, null, null, "Firmicutes", pageable);
        Assert.assertEquals(3335, pulInfoPageData.getTotal().intValue());

        pulInfos = service.queryPulByLinage(null, null, null, "Firmicutes");
        Assert.assertEquals(3335, pulInfos.size());
        Assert.assertEquals(pulInfoPageData.getList(), pulInfos.subList(0, 10));

        pulInfoPageData = service.queryPulByLinage(null, null, null, "other", pageable);
        Assert.assertEquals(58, pulInfoPageData.getTotal().intValue());

        pulInfos = service.queryPulByLinage(null, null, null, "other");
        Assert.assertEquals(58, pulInfos.size());
        Assert.assertEquals(pulInfoPageData.getList(), pulInfos.subList(0, 10));

        // 5. joint query by phylum and assembly accession
        pulInfoPageData = service.queryPulByLinage(null, "GCF_000013665", null, "Proteobacteria", pageable);
        Assert.assertEquals(3, pulInfoPageData.getTotal().intValue());

        pulInfos = service.queryPulByLinage(null, "GCF_000013665", null, "Proteobacteria");
        Assert.assertEquals(3, pulInfos.size());
        Assert.assertEquals(pulInfoPageData.getList(), pulInfos);


        pulInfoPageData = service.queryPulByLinage(null, "GCF_000013665", null, "Firmicutes", pageable);
        Assert.assertEquals(0, pulInfoPageData.getTotal().intValue());

        pulInfos = service.queryPulByLinage(null, "GCF_000013665", null, "Firmicutes");
        Assert.assertEquals(0, pulInfos.size());
    }

    @Test
    public void fuzzyQueryPulByLinage() {
        List<PulListVO> pulInfos = service.queryPulByLinage(null, null, "Clostridioides", null);
        for (PulListVO pulInfo : pulInfos) {
            Assert.assertTrue(pulInfo.getSpSpecies().toLowerCase().contains("clostridioides"));
        }

        pulInfos = service.queryPulByLinage(null, null, null, "Proteob");
        for (PulListVO pulInfo : pulInfos) {
            Assert.assertTrue(pulInfo.getSpPhylum().toLowerCase().contains("proteob"));
        }
    }

    @Test
    public void queryPulByDomainName() {
        PageData<PulListVO> pulInfoPage = service.queryPulByDomainName("Response_reg", pageable);
        Assert.assertEquals(5333, pulInfoPage.getTotal().intValue());

        List<PulListVO> pulInfos = service.queryPulByDomainName("Response_reg");
        Assert.assertEquals(5333, pulInfos.size());
        Assert.assertEquals(pulInfoPage.getList(), pulInfos.subList(0, 10));

        pulInfoPage = service.queryPulByDomainName("CorC_HlyC", pageable);
        Assert.assertEquals(1174, pulInfoPage.getTotal().intValue());

        pulInfos = service.queryPulByDomainName("CorC_HlyC");
        Assert.assertEquals(1174, pulInfos.size());
        Assert.assertEquals(pulInfoPage.getList(), pulInfos.subList(0, 10));
    }
}
