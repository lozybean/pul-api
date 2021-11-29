package me.lyon.pul.controller;

import lombok.extern.slf4j.Slf4j;
import me.lyon.pul.constant.JobStatus;
import me.lyon.pul.model.entity.*;
import me.lyon.pul.service.PredictService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("api/predict")
public class PredictController {
    @Resource
    PredictService predictService;

    @PostMapping("file")
    public WebResponse<JobInfo> submitPredictJobByFile(MultipartFile file) {
        JobInfo jobInfo = predictService.createPredictJob(file);
        // hidden some field
        jobInfo.setId(null);
        jobInfo.setContainerState(null);
        return WebResponse.ok(jobInfo);
    }

    @PostMapping("fasta")
    public WebResponse<JobInfo> submitPredictJobByPlain(
            @RequestBody String fasta
    ) {
        JobInfo jobInfo = predictService.createPredictJob(fasta);
        // hidden some field
        jobInfo.setId(null);
        jobInfo.setContainerState(null);
        return WebResponse.ok(jobInfo);
    }

    @GetMapping("{token}")
    public WebResponse<JobInfo> getJobInfo(
            @PathVariable String token
    ) {
        JobInfo jobInfo = predictService.findByToken(token);
        // hidden some field
        jobInfo.setId(null);
        jobInfo.setContainerState(null);
        return WebResponse.ok(jobInfo);
    }

    @DeleteMapping("{token}")
    public WebResponse<Void> deleteJob(
            @PathVariable String token
    ) {
        predictService.cleanPredictJob(token);
        return WebResponse.ok(null);
    }

    @GetMapping("{token}/result")
    public WebResponse<String> getPredictResult(
            @PathVariable String token
    ) throws IOException {
        JobInfo jobInfo = predictService.findByToken(token);
        if (JobStatus.INIT.equals(jobInfo.getStatus())) {
            return WebResponse.warn("have not run yet");
        }
        if (JobStatus.RUNNING.equals(jobInfo.getStatus())) {
            return WebResponse.warn("still running");
        }
        if (JobStatus.FAILED.equals(jobInfo.getStatus())) {
            return WebResponse.warn(String.format("failed! %s", jobInfo.getContainerState().getError()));
        }
        String result = predictService.readPredictResult(token);
        return WebResponse.ok(result);
    }
}
