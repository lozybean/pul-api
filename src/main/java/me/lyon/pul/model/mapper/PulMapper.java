package me.lyon.pul.model.mapper;

import me.lyon.pul.model.po.GenePO;
import me.lyon.pul.model.po.PulPO;
import me.lyon.pul.model.entity.PulContent;
import me.lyon.pul.model.entity.PulInfo;
import me.lyon.pul.model.vo.PulListVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;


@Mapper
public interface PulMapper {
    PulMapper INSTANCE = Mappers.getMapper(PulMapper.class);

    @Mapping(target = "pulType", source = "type")
    @Mapping(target = "pulStart", source = "locusStart")
    @Mapping(target = "pulEnd", source = "locusEnd")
    @Mapping(target = "assemblyAccession", source = "species.gcfNumber")
    @Mapping(target = "assemblyLevel", source = "species.assembleLevel")
    @Mapping(target = "taxonomyId", source = "species.taxid")
    @Mapping(target = "spKingdom", source = "species.spKingdom")
    @Mapping(target = "spPhylum", source = "species.spPhylum")
    @Mapping(target = "spClass", source = "species.spClass")
    @Mapping(target = "spOrder", source = "species.spOrder")
    @Mapping(target = "spFamily", source = "species.spFamily")
    @Mapping(target = "spSpecies", source = "species.spSpecies")
    @Mapping(target = "content", source = "contents")
    PulInfo pulInfo(PulPO po);

    @Mapping(target = "pulType", source = "type")
    @Mapping(target = "assemblyAccession", source = "species.gcfNumber")
    @Mapping(target = "spPhylum", source = "species.spPhylum")
    @Mapping(target = "spSpecies", source = "species.spSpecies")
    PulListVO pulListVO(PulPO po);

    @Mapping(target = "geneId", source = "id")
    @Mapping(target = "geneName", source = "geneName")
    @Mapping(target = "geneType", source = "classification")
    @Mapping(target = "geneStart", source = "locusStart")
    @Mapping(target = "geneEnd", source = "locusEnd")
    @Mapping(target = "domains", source = "domain")
    @Mapping(target = "strand", source = "strand")
    PulContent pulContent(GenePO po);
}
