package me.lyon.pul.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.io.Serializable;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NameCount implements Serializable, Comparable<NameCount> {
    private String name;
    private Long count;

    @Override
    public int compareTo(@NotNull NameCount o) {
        return (int) (this.getCount() - o.getCount());
    }
}
