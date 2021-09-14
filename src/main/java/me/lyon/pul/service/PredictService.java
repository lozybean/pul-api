package me.lyon.pul.service;

import me.lyon.pul.model.entity.JobInfo;
import me.lyon.pul.model.entity.PulInfo;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

public interface PredictService {
    /**
     * find first job which status is INIT
     *
     * @return :
     */
    Optional<JobInfo> findFirstInitJob();

    /**
     * find first job which status is FAILED and retry times
     *
     * @return :
     */
    Optional<JobInfo> findFirstRetryJob();

    /**
     * find job info by token
     *
     * @param token :
     * @return :
     */
    JobInfo findByToken(String token);

    /**
     * create predict job by Input Gbff file
     *
     * @param file : input gbff format file
     * @return : job info
     */
    JobInfo createPredictJob(MultipartFile file);

    /**
     * start predict job
     *
     * @param token : token
     * @return : job info
     */
    JobInfo startPredictJob(String token);

    /**
     * wait predict job finish
     *
     * @param token : token
     * @return : job info
     */
    JobInfo waitPredictJobFinish(String token);

    /**
     * update predict job status
     *
     * @param token :
     * @return : job info
     */
    JobInfo updatePredictJobStatus(String token);

    /**
     * clean and remove predict job
     *
     * @param token :
     */
    void cleanPredictJob(String token);

    /**
     * read predict result by token
     *
     * @param token :
     * @return : predict pul result
     */
    List<PulInfo> readPredictResult(String token);
}
