package me.lyon.pul.model.mapper;

import com.github.dockerjava.api.command.InspectContainerResponse;
import me.lyon.pul.model.entity.ContainerState;
import me.lyon.pul.model.entity.JobInfo;
import me.lyon.pul.model.po.ContainerStatePO;
import me.lyon.pul.model.po.JobInfoPO;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Mapper
public interface JobInfoMapper {
    JobInfoMapper INSTANCE = Mappers.getMapper(JobInfoMapper.class);
    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    Pattern dateWithMillSecondsPattern = Pattern.compile("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d+)Z");

    default Date parseDate(String dateStr) {
        if (StringUtils.isEmpty(dateStr)) {
            return null;
        }
        Matcher matcher = dateWithMillSecondsPattern.matcher(dateStr);
        if (matcher.matches()) {
            String millSeconds = matcher.group(1);
            dateStr = dateStr.replace(millSeconds, "");
        }
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        try {
            return dateFormat.parse(dateStr);
        } catch (ParseException e) {
            return null;
        }
    }

    default String formatDate(Date date) {
        if (Objects.isNull(date)) {
            return null;
        }
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        return dateFormat.format(date);
    }

    @Mapping(target = "status", expression = "java(me.lyon.pul.constant.ContainerStatus.ofValue(state.getStatus()))")
    ContainerStatePO po(String id, InspectContainerResponse.ContainerState state);

    ContainerStatePO po(ContainerState state);

    JobInfoPO po(JobInfo entity);

    JobInfo entity(JobInfoPO po);

    ContainerState entity(ContainerStatePO po);

    @Mapping(target = "status", expression = "java(me.lyon.pul.constant.ContainerStatus.ofValue(state.getStatus()))")
    ContainerState entity(String id, InspectContainerResponse.ContainerState state);
}
