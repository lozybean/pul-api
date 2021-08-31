package me.lyon.pul.service;

import com.github.dockerjava.api.command.InspectContainerResponse;
import me.lyon.pul.model.po.JobInfoPO;
import org.springframework.web.multipart.MultipartFile;

public interface PredictService {
    /**
     * find job info by token
     *
     * @param token :
     * @return :
     */
    JobInfoPO findByToken(String token);

    /**
     * find job info by container id
     *
     * @param containerId :
     * @return :
     */
    JobInfoPO findByContainerId(String containerId);

    /**
     * create container for pul predicate
     *
     * @param file : input gbff format file
     * @return : container id
     */
    String createPulPredictContainer(MultipartFile file);

    /**
     * start a container
     *
     * @param id : container id
     */
    void startContainer(String id);

    /**
     * inspect docker container
     *
     * @param id : container id
     * @return : container state
     */
    InspectContainerResponse.ContainerState inspectContainer(String id);

    /**
     * remove container
     *
     * @param id :
     */
    void removeContainer(String id);
}
