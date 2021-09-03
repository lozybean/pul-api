package me.lyon.pul.job;

import lombok.extern.slf4j.Slf4j;
import me.lyon.pul.model.entity.JobInfo;
import me.lyon.pul.service.PredictService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Optional;


@Slf4j
@Component
public class PredictJob {
    @Resource
    PredictService predictService;

    @Scheduled(initialDelay = 5000, fixedDelay = 5000)
    public void runPredict() {
        Optional<JobInfo> jobInfoOptional = predictService.findFirstInitJob();
        if (jobInfoOptional.isEmpty()) {
            return;
        }
        JobInfo jobInfo = jobInfoOptional.get();
        log.info("find init job to run: {}, token: {}", jobInfo.getId(), jobInfo.getToken());
        predictService.startContainer(jobInfo.getContainerState().getId());
        predictService.waitContainer(jobInfo.getContainerState().getId());
    }
}
