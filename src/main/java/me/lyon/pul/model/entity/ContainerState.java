package me.lyon.pul.model.entity;

import lombok.Data;
import me.lyon.pul.constant.ContainerStatus;

import java.io.Serializable;
import java.util.Date;

@Data
public class ContainerState implements Serializable {
    private String id;
    private ContainerStatus status;
    private Boolean running;
    private Boolean paused;
    private Boolean restarting;
    private Boolean oomKilled;
    private Boolean dead;
    private Long pid;
    private Long exitCode;
    private String error;
    private Date startedAt;
    private Date finishedAt;
}
