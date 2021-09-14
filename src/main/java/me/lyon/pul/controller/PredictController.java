package me.lyon.pul.controller;

import lombok.extern.slf4j.Slf4j;
import me.lyon.pul.constant.JobStatus;
import me.lyon.pul.exception.NotFoundException;
import me.lyon.pul.model.entity.*;
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
    public WebResponse<JobInfo> submitPredictJob(MultipartFile file) {
        JobInfo jobInfo = predictService.createPredictJob(file);
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

    @GetMapping("{token}/puls")
    public WebResponse<PageData<PulInfo>> getPredictResult(
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
        PageData<PulInfo> pageData = PageData.<PulInfo>builder()
                .list(pulInfos)
                .total(pulInfos.size())
                .build();
        return WebResponse.ok(pageData);
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
                .filter(pul -> pul.getId().equals(pulId))
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
