package me.lyon.pul.service;

import com.github.dockerjava.api.command.InspectContainerResponse;
import me.lyon.pul.constant.ContainerStatus;
import me.lyon.pul.constant.JobStatus;
import me.lyon.pul.model.po.JobInfoPO;
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

        String containerId = predictService.createPulPredictContainer(file);
        Assert.assertNotNull(containerId);
        JobInfoPO jobInfoPO = predictService.findByContainerId(containerId);
        Assert.assertEquals(JobStatus.INIT, jobInfoPO.getStatus());
        Assert.assertEquals(ContainerStatus.CREATED, jobInfoPO.getContainerState().getStatus());
    }

    @Test
    public void deleteContainer() {
        final String containerId = "64f6d430936a";
        predictService.removeContainer(containerId);
        InspectContainerResponse.ContainerState state = predictService.inspectContainer(containerId);
        Assert.assertNull(state);
    }
}
