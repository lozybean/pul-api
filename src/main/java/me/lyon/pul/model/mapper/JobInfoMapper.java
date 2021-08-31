package me.lyon.pul.model.mapper;

import com.github.dockerjava.api.command.InspectContainerResponse;
import me.lyon.pul.model.po.ContainerStatePO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface JobInfoMapper {
    JobInfoMapper INSTANCE = Mappers.getMapper(JobInfoMapper.class);

    @Mapping(target = "status", expression = "java(me.lyon.pul.constant.ContainerStatus.ofValue(state.getStatus()))")
    ContainerStatePO po(String id, InspectContainerResponse.ContainerState state);
}
