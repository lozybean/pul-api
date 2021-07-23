package me.lyon.pul.model.vo;

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
public class BrowseData implements Serializable {
    private List<NameCount> polysaccharide;
    private List<NameCount> phylum;
}
