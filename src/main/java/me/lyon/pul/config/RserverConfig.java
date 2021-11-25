package me.lyon.pul.config;

import lombok.Data;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "rserver")
public class RserverConfig {
    private String host;
    private Integer port;

    @Bean
    public RConnection rConnection() throws RserveException {
        RConnection rConnection = new RConnection(host, port);
        rConnection.eval("library(ggplot2)\n" +
                "library(gggenes)\n" +
                "library(base64enc)\n" +
                "library(RColorBrewer)");
        rConnection.eval("encodeGraphic <- function(g) {\n" +
                "  fn <- tempfile(fileext = \".png\")\n" +
                "  ggsave(fn, g, width = 10, height = 3, unit='in', dpi = 300)\n" +
                "  img <- base64enc::dataURI(file = fn, mime = \"image/png\")\n" +
                "  return(img)\n" +
                "}");
        rConnection.eval("palette_Set3 <- colorRampPalette(brewer.pal(12, \"Set3\"))");
        rConnection.eval("myColors <- c(\"monosaccharide metabolic enzymes\"=\"#1F78B4\",\n" +
                "              \"sugar transporter or sugar binding protein\"=\"#FB9A99\",\n" +
                "              \"carbonhydrate active enzymes\"=\"#33A02C\",\n" +
                "              \"Transcription factor\"=\"#B2DF8A\",\n" +
                "              \"UNKNOWN\"=\"#A6CEE3\")");
        return rConnection;
    }
}
