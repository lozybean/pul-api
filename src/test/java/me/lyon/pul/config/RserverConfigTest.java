package me.lyon.pul.config;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.rosuda.REngine.*;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class RserverConfigTest {
    @Resource
    RConnection rConnection;

    @Test
    public void testConnection() throws RserveException, REXPMismatchException {
        double[] d = rConnection.eval("rnorm(10)").asDoubles();
        Assert.assertEquals(10, d.length);
    }

    @Test
    public void testGggenes() throws REngineException, REXPMismatchException {
        rConnection.eval("library(ggplot2)\n" +
                "library(gggenes)\n" +
                "library(base64enc)");
        rConnection.assign("molecule",
                new String[]{"Genome5", "Genome5", "Genome5", "Genome5", "Genome5","Genome5", "Genome5", "Genome5", "Genome5", "Genome5"});
        rConnection.assign("gene",
                new String[]{"genA", "genB", "genC", "genD", "genE","genF", "protF", "protC", "protD", "protE"});
        rConnection.assign("start",
                new int[]{405113, 407035, 407927, 408387, 408751,409836, 410335, 412621, 412830, 413867});
        rConnection.assign("end",
                new int[]{407035, 407916, 408394, 408737, 409830,410315, 412596, 412833, 413870, 414850});
        rConnection.assign("strand",
                new String[]{"forward", "forward", "forward", "reverse", "forward","forward", "reverse", "forward", "forward", "forward"});
        rConnection.assign("orientation",
                new int[]{-1, -1, -1, -1, 1,-1, 1, 1, -1, -1});
        rConnection.eval("data <- data.frame( list(molecule=molecule, gene=gene, start=start, end=end , strand=strand, orientation=orientation) )");
        rConnection.eval("encodeGraphic <- function(g) {\n" +
                "  fn <- tempfile(fileext = \".png\")\n" +
                "  ggsave(fn, g)\n" +
                "  img <- base64enc::dataURI(file = fn, mime = \"image/png\")\n" +
                "  return(img)\n" +
                "}");
        rConnection.eval("gg <- ggplot(data, aes(xmin = start, xmax = end, y = molecule, fill = gene, forward = orientation) ) +\n" +
                "  geom_gene_arrow() +\n" +
                "  facet_wrap(~ molecule, scales = \"free\", ncol = 1) +\n" +
                "  scale_fill_brewer(palette = \"Set3\") +\n" +
                "  theme_genes()\n");

        String base64 = rConnection.eval("encodeGraphic(gg)").asString();
        System.out.println(base64);
    }
}
