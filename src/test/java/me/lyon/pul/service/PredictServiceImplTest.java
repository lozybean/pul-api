package me.lyon.pul.service;

import me.lyon.pul.constant.ContainerStatus;
import me.lyon.pul.constant.JobStatus;
import me.lyon.pul.model.entity.JobInfo;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Optional;

//@Ignore
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class PredictServiceImplTest {
    @Resource
    PredictService predictService;

    private final org.springframework.core.io.Resource fileResource = new ClassPathResource("protein.fas");

    @Test
    public void createPulPredictContainer() throws IOException {
        MockMultipartFile file = new MockMultipartFile("attachments", fileResource.getFilename(),
                MediaType.MULTIPART_FORM_DATA_VALUE,
                fileResource.getInputStream());

        JobInfo jobInfo = predictService.createPredictJob(file);
        Assert.assertNotNull(jobInfo);
        Assert.assertEquals(JobStatus.INIT, jobInfo.getStatus());
        Assert.assertEquals(ContainerStatus.CREATED, jobInfo.getContainerState().getStatus());

        // remove job info
        String token = jobInfo.getToken();
        predictService.cleanPredictJob(token);
    }

    @Test
    public void findFirstInitJob() {
        Optional<JobInfo> jobInfoOptional = predictService.findFirstRunnableJob();
        Assert.assertTrue(jobInfoOptional.isPresent());
        Assert.assertEquals(5, jobInfoOptional.get().getId().intValue());
    }

    @Test
    public void readResult() throws IOException {
        String result = predictService.readPredictResult("eWU5THJEd29DNGNlc2x5cg");
        System.out.println(result);
    }
}
