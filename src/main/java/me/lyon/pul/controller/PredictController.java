package me.lyon.pul.controller;

import lombok.extern.slf4j.Slf4j;
import me.lyon.pul.constant.JobStatus;
import me.lyon.pul.exception.NotFoundException;
import me.lyon.pul.model.entity.Gggenes;
import me.lyon.pul.model.entity.JobInfo;
import me.lyon.pul.model.entity.PulInfo;
import me.lyon.pul.model.entity.WebResponse;
import me.lyon.pul.service.GggeneService;
import me.lyon.pul.service.PredictService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("api/predict")
public class PredictController {
    @Resource
    PredictService predictService;
    @Resource
    GggeneService gggeneService;

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

    @GetMapping("{token}/puls")
    public WebResponse<List<PulInfo>> getPredictResult(
            @PathVariable String token
    ) {
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
        List<PulInfo> pulInfos = predictService.readPredictResult(token);
        return WebResponse.ok(pulInfos);
    }

    @GetMapping("{token}/puls/{pulId}")
    public WebResponse<PulInfo> getPredictPulContent(
            @PathVariable String token,
            @PathVariable String pulId
    ) {
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
        PulInfo pulInfo = predictService.readPredictResult(token)
                .stream()
                .filter(pul -> pul.getId().equals(pulId))
                .findAny()
                .orElseThrow(() -> new NotFoundException("no such pulId in related token result"));
        return WebResponse.ok(pulInfo);
    }

    @GetMapping("{token}/puls/{pulId}/gggenes")
    @ResponseBody
    public WebResponse<Gggenes> plotGggenes(
            @PathVariable String token,
            @PathVariable String pulId
    ) {
        List<PulInfo> pulInfos = predictService.readPredictResult(token);
        PulInfo pulInfo = pulInfos.stream()
                .filter(pul -> pul.getPulId().equals(pulId))
                .findAny()
                .orElseThrow(() -> new NotFoundException("no such pulId in related token result"));
        try {
            Gggenes gggenes = gggeneService.plotWithBase64WithToken(pulInfo, token);
            return WebResponse.ok(gggenes);
        } catch (Exception e) {
            log.warn("预测任务：{} Ggenes绘制失败！{}", token, pulInfo.getPulId(), e);
            return WebResponse.warn("Gggenes绘制失败，请检查！");
        }
    }
}
