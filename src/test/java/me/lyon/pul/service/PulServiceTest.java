package me.lyon.pul.service;

import me.lyon.pul.model.entity.PageData;
import me.lyon.pul.model.entity.PulInfo;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class PulServiceTest {
    private final Pageable pageable = PageRequest.of(0, 10);

    @Resource
    PulService service;

    @Test
    public void queryPulByType() {
        PageData<PulInfo> pulInfoPage = service.queryPulByType("agar", pageable);
        Assert.assertEquals(186, pulInfoPage.getTotal().intValue());
    }

    @Test
    public void queryPulByLinage() {
        // 1. query by tax id
        PageData<PulInfo> pulInfoPageData = service.queryPulByLinage(203122, null, null, null, pageable);
        Assert.assertEquals(3, pulInfoPageData.getTotal().intValue());

        // 2. query by assembly accession
        pulInfoPageData = service.queryPulByLinage(null, "GCF_000013665", null, null, pageable);
        Assert.assertEquals(3, pulInfoPageData.getTotal().intValue());

        // 3. query by species
        pulInfoPageData = service.queryPulByLinage(null, null, "Clostridioides difficile QCD-66c26", null, pageable);
        Assert.assertEquals(1, pulInfoPageData.getTotal().intValue());

        // 4. query by phylum
        pulInfoPageData = service.queryPulByLinage(null, null, null, "Firmicutes", pageable);
        Assert.assertEquals(3335, pulInfoPageData.getTotal().intValue());
        pulInfoPageData = service.queryPulByLinage(null, null, null, "other", pageable);
        Assert.assertEquals(58, pulInfoPageData.getTotal().intValue());

        // 5. joint query by phylum and assembly accession
        pulInfoPageData = service.queryPulByLinage(null, "GCF_000013665", null, "Proteobacteria", pageable);
        Assert.assertEquals(3, pulInfoPageData.getTotal().intValue());
        pulInfoPageData = service.queryPulByLinage(null, "GCF_000013665", null, "Firmicutes", pageable);
        Assert.assertEquals(0, pulInfoPageData.getTotal().intValue());
    }

    @Test
    public void queryPulByDomainName() {
        PageData<PulInfo> pulInfoPage = service.queryPulByDomainName("Response_reg", pageable);
        Assert.assertEquals(5333, pulInfoPage.getTotal().intValue());

        pulInfoPage = service.queryPulByDomainName("CorC_HlyC", pageable);
        Assert.assertEquals(1174, pulInfoPage.getTotal().intValue());
    }
}
