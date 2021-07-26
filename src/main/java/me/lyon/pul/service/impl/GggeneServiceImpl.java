package me.lyon.pul.service.impl;

import me.lyon.pul.model.vo.PulContent;
import me.lyon.pul.model.vo.PulInfo;
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
import java.util.stream.Collectors;

@Service
@CacheConfig(cacheNames = {"gggenes"})
public class GggeneServiceImpl implements GggeneService {
    @Resource(name = "rConnection")
    RConnection rConnection;


    @Cacheable(cacheNames = "ggenes", key = "#pulInfo.id")
    @Override
    public synchronized String plotWithBase64(PulInfo pulInfo) throws REngineException, REXPMismatchException {
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

        String[] molecule = new String[ends.length];
        Arrays.fill(molecule, pulInfo.getContigName());

        rConnection.assign("molecule", molecule);
        rConnection.assign("gene", geneName);
        rConnection.assign("start", starts);
        rConnection.assign("end", ends);
        rConnection.assign("strand", strand);
        rConnection.eval("data <- data.frame( list(molecule=molecule, gene=gene, start=start, end=end, strand=strand) )");
        rConnection.eval(String.format("gg <- ggplot(data, " +
                        "  aes(xmin = start, xmax = end, y = molecule, fill = gene, label = gene, forward = strand)) +\n" +
                        "  geom_gene_arrow(arrowhead_height = unit(3, \"mm\"), arrowhead_width = unit(1, \"mm\")) +\n" +
                        "  annotate(\"text\",x=%d,y=1.4,label=\"%s\",hjust=0.1) +\n" +
                        "  annotate(\"curve\",x=%d,y=1.3, xend = %d, yend = 1.05, curvature = 0, arrow = arrow(length = unit(2, \"mm\"))) +" +
                        "  annotate(\"text\",x=%d,y=1.4,label=\"%s\",hjust=0.9) +\n" +
                        "  annotate(\"curve\",x=%d,y=1.3, xend = %d, yend = 1.05, curvature = 0, arrow = arrow(length = unit(2, \"mm\"))) +" +
                        "  facet_wrap(~ molecule, scales = \"free\", ncol = 1) +\n" +
                        "  discrete_scale(\"fill\", \"manual\", palette_Set3) +\n" +
                        "  theme_genes() +\n" +
                        "  theme(legend.position=\"none\")",
                arrowStart, geneName[0],
                arrowStart, arrowStart,
                arrowEnd, geneName[geneName.length - 1],
                arrowEnd, arrowEnd
                )
        );

        return rConnection.eval("encodeGraphic(gg)").asString();
    }
}
