package me.lyon.pul.controller;

import me.lyon.pul.constant.JobStatus;
import me.lyon.pul.model.entity.JobInfo;
import me.lyon.pul.model.entity.WebResponse;
import me.lyon.pul.service.PredictService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

@RestController
@RequestMapping("api/predict")
public class PredictController {
    @Resource
    PredictService predictService;

    @PostMapping
    public WebResponse<String> submitPredictJob(MultipartFile file) {
        String token = predictService.createPulPredictContainer(file);
        return WebResponse.ok(token);
    }

    @GetMapping("{token}/status")
    public WebResponse<JobStatus> getJobStatus(
            @PathVariable String token
    ) {
        JobInfo jobInfo = predictService.findByToken(token);
        return WebResponse.ok(jobInfo.getStatus());
    }
}
