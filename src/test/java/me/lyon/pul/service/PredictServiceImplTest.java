package me.lyon.pul.service;

import me.lyon.pul.constant.ContainerStatus;
import me.lyon.pul.constant.JobStatus;
import me.lyon.pul.model.entity.ContainerState;
import me.lyon.pul.model.entity.JobInfo;
import me.lyon.pul.model.entity.PulInfo;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class PredictServiceImplTest {
    @Resource
    PredictService predictService;

    private final org.springframework.core.io.Resource fileResource = new ClassPathResource("GCF_000013665.1_ASM1366v1_genomic.gbff");

    @Test
    public void createPulPredictContainer() throws IOException {
        MockMultipartFile file = new MockMultipartFile("attachments", fileResource.getFilename(),
                MediaType.MULTIPART_FORM_DATA_VALUE,
                fileResource.getInputStream());

        String token = predictService.createPulPredictContainer(file);
        Assert.assertNotNull(token);
        JobInfo jobInfo = predictService.findByToken(token);
        Assert.assertEquals(JobStatus.INIT, jobInfo.getStatus());
        Assert.assertEquals(ContainerStatus.CREATED, jobInfo.getContainerState().getStatus());

        // remove job info
        String containerId = jobInfo.getContainerState().getId();
        predictService.removeContainer(containerId);
        ContainerState state = predictService.inspectContainer(containerId);
        Assert.assertNull(state);
    }

    @Test
    public void findFirstInitJob() {
        Optional<JobInfo> jobInfoOptional = predictService.findFirstInitJob();
        Assert.assertTrue(jobInfoOptional.isPresent());
        Assert.assertEquals(5, jobInfoOptional.get().getId().intValue());
    }

    @Test
    public void readResult() {
        List<PulInfo> pulInfoList = predictService.readPredictResult("MUt1aXRXQ09RNXNFQzJDRw");
        System.out.println(pulInfoList);
    }
}
