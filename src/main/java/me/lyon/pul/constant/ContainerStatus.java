package me.lyon.pul.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
@AllArgsConstructor
public enum ContainerStatus {
    CREATED("created"),
    RESTARTING("restarting"),
    RUNNING("running"),
    REMOVING("removing"),
    PAUSED("paused"),
    EXITED("exited"),
    DEAD("dead");

    private final String value;
    private static final Map<String, ContainerStatus> VALUE_MAP = Stream
            .of(ContainerStatus.values())
            .collect(Collectors.toMap(ContainerStatus::getValue, Function.identity()));

    public ContainerStatus ofValue(String value) {
        return VALUE_MAP.getOrDefault(value, null);
    }
}
