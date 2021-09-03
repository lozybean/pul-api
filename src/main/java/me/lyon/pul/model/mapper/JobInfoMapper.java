package me.lyon.pul.model.mapper;

import com.github.dockerjava.api.command.InspectContainerResponse;
import me.lyon.pul.model.entity.ContainerState;
import me.lyon.pul.model.entity.JobInfo;
import me.lyon.pul.model.po.ContainerStatePO;
import me.lyon.pul.model.po.JobInfoPO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

@Mapper
public interface JobInfoMapper {
    JobInfoMapper INSTANCE = Mappers.getMapper(JobInfoMapper.class);
    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

    default Date parseDate(String dateStr) {

        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        try {
            return dateFormat.parse(dateStr);
        } catch (ParseException e) {
            return null;
        }
    }

    default String formatDate(Date date) {
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        return dateFormat.format(date);
    }

    @Mapping(target = "status", expression = "java(me.lyon.pul.constant.ContainerStatus.ofValue(state.getStatus()))")
    ContainerStatePO po(String id, InspectContainerResponse.ContainerState state);

    ContainerStatePO po(ContainerState state);

    JobInfo entity(JobInfoPO po);

    ContainerState entity(ContainerStatePO po);

    @Mapping(target = "status", expression = "java(me.lyon.pul.constant.ContainerStatus.ofValue(state.getStatus()))")
    ContainerState entity(String id, InspectContainerResponse.ContainerState state);
}
