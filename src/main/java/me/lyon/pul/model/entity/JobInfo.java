package me.lyon.pul.model.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import me.lyon.pul.constant.JobStatus;

import java.io.Serializable;
import java.util.Date;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JobInfo implements Serializable {
    private Integer id;
    private String token;
    private ContainerState containerState;
    private JobStatus status;
    private Date createTime;
    private Date updateTime;
}
