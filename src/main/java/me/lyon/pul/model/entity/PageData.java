package me.lyon.pul.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageData<T extends Serializable> implements Serializable {
    private List<T> list;
    private Integer total;
}
