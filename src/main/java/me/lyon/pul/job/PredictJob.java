package me.lyon.pul.job;

import lombok.extern.slf4j.Slf4j;
import me.lyon.pul.config.PredictConfig;
import me.lyon.pul.constant.JobStatus;
import me.lyon.pul.model.entity.JobInfo;
import me.lyon.pul.service.PredictService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;


@Slf4j
@Component
public class PredictJob {
    @Resource
    PredictService predictService;
    @Resource
    PredictConfig config;

    @Scheduled(initialDelay = 5000, fixedDelay = 5000)
    public void runPredict() {
        Optional<JobInfo> jobInfoOptional = predictService.findFirstRunnableJob();
        if (jobInfoOptional.isPresent()) {
            JobInfo jobInfo = jobInfoOptional.get();
            String token = jobInfo.getToken();
            log.info("find job to run: {}, token: {}", jobInfo.getId(), token);
            predictService.startPredictJob(token);
            predictService.waitPredictJobFinish(token);
        }
    }


    @Scheduled(cron = "0 0 0 * * ?")
    public void cleanUpJobs() {
        List<JobInfo> jobInfoList = predictService.listJobs();
        for (JobInfo jobInfo : jobInfoList) {
            if (jobInfo.getStatus() == JobStatus.INIT || jobInfo.getStatus() == JobStatus.RUNNING) {
                continue;
            }
            Date now = new Date();
            Date lastJobUpdateTime = jobInfo.getUpdateTime();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(lastJobUpdateTime);
            calendar.add(Calendar.DATE, config.getReserveResultDays().intValue());
            if (now.after(calendar.getTime())) {
                predictService.cleanPredictJob(jobInfo.getToken());
            }
        }
    }
}
