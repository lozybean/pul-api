package me.lyon.pul.service.impl;

import me.lyon.pul.model.vo.PulContent;
import me.lyon.pul.model.vo.PulInfo;
import me.lyon.pul.service.GggeneService;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngineException;
import org.rosuda.REngine.Rserve.RConnection;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Arrays;

@Service
public class GggeneServiceImpl implements GggeneService {
    @Resource(name = "rConnection")
    RConnection rConnection;


    @Override
    public String plotWithBase64(PulInfo pulInfo) throws REngineException, REXPMismatchException {
        String[] geneName = (String[]) pulInfo.getContent()
                .stream()
                .map(PulContent::getGeneName)
                .toArray();
        int[] starts = pulInfo.getContent().stream().map(PulContent::getGeneStart).mapToInt(i -> i).toArray();
        int[] ends = pulInfo.getContent().stream().map(PulContent::getGeneEnd).mapToInt(i -> i).toArray();

        String[] molecule = new String[ends.length];
        Arrays.fill(molecule, pulInfo.getContigName());

        rConnection.assign("molecule", molecule);
        rConnection.assign("gene", geneName);
        rConnection.assign("start", starts);
        rConnection.assign("end", ends);
        rConnection.eval("data <- data.frame( list(molecule=molecule, gene=gene, start=start, end=end) )");
        rConnection.eval("gg <- ggplot(data, aes(xmin = start, xmax = end, y = molecule, fill = gene) ) +\n" +
                "  geom_gene_arrow() +\n" +
                "  facet_wrap(~ molecule, scales = \"free\", ncol = 1) +\n" +
                "  scale_fill_brewer(palette = \"Set3\") +\n" +
                "  theme_genes()\n");

        return rConnection.eval("encodeGraphic(gg)").asString();
    }
}
