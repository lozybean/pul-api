package me.lyon.pul.constant;

import me.lyon.pul.model.po.ContainerStatePO;

import java.util.Set;

public enum JobStatus {
    INIT,
    RUNNING,
    FAILED,
    /**
     * RETRYING 状态
     */
    RETRYING,
    SUCCESS;

    public static JobStatus fromContainerState(ContainerStatePO state, boolean maxRetried) {
        if (ContainerStatus.CREATED == state.getStatus()) {
            return INIT;
        } else if (Set.of(ContainerStatus.RUNNING, ContainerStatus.PAUSED,
                ContainerStatus.REMOVING, ContainerStatus.RESTARTING).contains(state.getStatus())) {
            return RUNNING;
        } else if (ContainerStatus.DEAD == state.getStatus()) {
            if (maxRetried) {
                return FAILED;
            } else {
                return RETRYING;
            }
        } else if (ContainerStatus.EXITED == state.getStatus() && state.getExitCode() == 0L) {
            return SUCCESS;
        } else if (ContainerStatus.EXITED == state.getStatus() && state.getExitCode() != 0L) {
            if (maxRetried) {
                return FAILED;
            } else {
                return RETRYING;
            }
        }
        if (maxRetried) {
            return FAILED;
        } else {
            return RETRYING;
        }
    }
}
