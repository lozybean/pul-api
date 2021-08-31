package me.lyon.pul.constant;

import me.lyon.pul.model.po.ContainerStatePO;

import java.util.Set;

public enum JobStatus {
    INIT,
    RUNNING,
    FAILED,
    SUCCESS;

    public static JobStatus fromContainerState(ContainerStatePO state) {
        if (ContainerStatus.CREATED == state.getStatus()) {
            return INIT;
        } else if (Set.of(ContainerStatus.RUNNING, ContainerStatus.PAUSED,
                ContainerStatus.REMOVING, ContainerStatus.RESTARTING).contains(state.getStatus())) {
            return RUNNING;
        } else if (ContainerStatus.DEAD == state.getStatus()) {
            return FAILED;
        } else if (ContainerStatus.EXITED == state.getStatus() && state.getExitCode() == 0L) {
            return SUCCESS;
        } else if (ContainerStatus.EXITED == state.getStatus() && state.getExitCode() != 0L) {
            return FAILED;
        }
        return FAILED;
    }
}
