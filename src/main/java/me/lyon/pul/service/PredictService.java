package me.lyon.pul.service;

import me.lyon.pul.model.entity.ContainerState;
import me.lyon.pul.model.entity.JobInfo;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

public interface PredictService {
    /**
     * find first job which status is INIT
     *
     * @return :
     */
    Optional<JobInfo> findFirstInitJob();

    /**
     * find job info by token
     *
     * @param token :
     * @return :
     */
    JobInfo findByToken(String token);

    /**
     * find job info by container id
     *
     * @param containerId :
     * @return :
     */
    JobInfo findByContainerId(String containerId);

    /**
     * create container for pul predicate
     *
     * @param file : input gbff format file
     * @return : token
     */
    String createPulPredictContainer(MultipartFile file);

    /**
     * start a container
     *
     * @param id : container id
     */
    void startContainer(String id);

    /**
     * wait a container untile exit
     *
     * @param id : container id
     */
    void waitContainer(String id);

    /**
     * inspect docker container
     *
     * @param id     : container id
     * @param update : whether update
     * @return : container state
     */
    ContainerState inspectContainer(String id, boolean update);

    /**
     * default inspect docker container, no update
     *
     * @param id : container id
     * @return : container state
     */
    default ContainerState inspectContainer(String id) {
        return inspectContainer(id, false);
    }

    /**
     * remove container
     *
     * @param id :
     */
    void removeContainer(String id);
}
