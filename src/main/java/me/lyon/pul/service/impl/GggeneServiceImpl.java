package me.lyon.pul.service.impl;

import lombok.extern.slf4j.Slf4j;
import me.lyon.pul.model.entity.Gggenes;
import me.lyon.pul.model.entity.PulContent;
import me.lyon.pul.model.entity.PulInfo;
import me.lyon.pul.service.GggeneService;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngineException;
import org.rosuda.REngine.Rserve.RConnection;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@CacheConfig(cacheNames = {"gggenes"})
public class GggeneServiceImpl implements GggeneService {
    @Resource(name = "rConnection")
    RConnection rConnection;

    private static final Map<String, String> CLASSIFICATION_FULL_NAME = Map.of(
            "MME", "monosaccharide metabolic enzymes",
            "TF", "Transcription factor"
    );

    private synchronized String plotByRConnection(PulInfo pulInfo) throws REngineException, REXPMismatchException {
        String speciesName = pulInfo.getSpSpecies();
        List<PulContent> pulContents = pulInfo.getContent().stream().sorted().collect(Collectors.toList());
        String[] geneName = pulContents
                .stream()
                .map(PulContent::getGeneId)
                .toArray(String[]::new);
        int[] starts = pulContents.stream().map(PulContent::getGeneStart).mapToInt(i -> i).toArray();
        int[] ends = pulContents.stream().map(PulContent::getGeneEnd).mapToInt(i -> i).toArray();
        int[] strand = pulContents.stream().map(PulContent::getStrand).mapToInt(i -> i).toArray();
        int arrowStart = Math.min(starts[0], ends[0]);
        int arrowEnd = Math.max(ends[ends.length - 1], starts[starts.length - 1]);
        String[] classification = pulContents
                .stream()
                .map(PulContent::getGeneType)
                .map(s -> CLASSIFICATION_FULL_NAME.getOrDefault(s, s))
                .toArray(String[]::new);

        String[] molecule = new String[ends.length];
        Arrays.fill(molecule, pulInfo.getContigName());

        rConnection.assign("molecule", molecule);
        rConnection.assign("gene", geneName);
        rConnection.assign("start", starts);
        rConnection.assign("end", ends);
        rConnection.assign("strand", strand);
        rConnection.assign("classification", classification);
        rConnection.eval("data <- data.frame( list(molecule=molecule, gene=gene, start=start, end=end, strand=strand, classification=classification) )");
        String gggenesCmd = String.format("gg <- ggplot(data, " +
                        "  aes(xmin = start, xmax = end, y = molecule, fill = classification, label = gene, forward = strand)) +\n" +
                        "  geom_gene_arrow(arrowhead_height = unit(3, \"mm\"), arrowhead_width = unit(1, \"mm\")) +\n" +
                        "  annotate(\"text\",x=%d,y=1.4,label=\"%s\",hjust=0.1) +\n" +
                        "  annotate(\"curve\",x=%d,y=1.3, xend = %d, yend = 1.05, curvature = 0, arrow = arrow(length = unit(2, \"mm\"))) +" +
                        "  annotate(\"text\",x=%d,y=1.4,label=\"%s\",hjust=0.9) +\n" +
                        "  annotate(\"curve\",x=%d,y=1.3, xend = %d, yend = 1.05, curvature = 0, arrow = arrow(length = unit(2, \"mm\"))) +" +
                        "  facet_wrap(~ molecule, scales = \"free\", ncol = 1) +\n" +
                        "  scale_fill_manual(values=myColors) +\n" +
                        "  theme_genes() +\n" +
                        "  guides(fill=guide_legend(nrow=2,byrow=FALSE)) +\n" +
                        "  theme(legend.position=\"top\", legend.title = element_text( size=12, face=\"bold\"), axis.title.y=element_blank()) +\n" +
                        "  xlab(\"%s\")",
                arrowStart, geneName[0],
                arrowStart, arrowStart,
                arrowEnd, geneName[geneName.length - 1],
                arrowEnd, arrowEnd,
                speciesName
        );
        log.info("gggenes command: \n{}", gggenesCmd);
        rConnection.eval(gggenesCmd);

        return rConnection.eval("encodeGraphic(gg)").asString();
    }

    @Cacheable(cacheNames = "ggenes", key = "#pulInfo.id")
    @Override
    public Gggenes plotWithBase64(PulInfo pulInfo) throws REngineException, REXPMismatchException {
        return new Gggenes(plotByRConnection(pulInfo));
    }

    @Cacheable(cacheNames = "ggenes", key = "#token.concat('-').concat(#pulInfo.id)")
    @Override
    public Gggenes plotWithBase64WithToken(PulInfo pulInfo, String token) throws REngineException, REXPMismatchException {
        return new Gggenes(plotByRConnection(pulInfo));
    }
}
