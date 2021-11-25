library(ggplot2)
library(gggenes)
library(base64enc)
library(RColorBrewer)

palette_Set3 <- colorRampPalette(brewer.pal(12, "Set3"))

encodeGraphic <- function(g) {
  fn <- tempfile(fileext = ".png")
  ggsave(fn, g, width = 10, height = 2, unit='in', dpi = 300)
  img <- base64enc::dataURI(file = fn, mime = "image/png")
  return(img)
}
myColors <- c("mono saccharide metabolic enzymes"="#1F78B4",
              "Transcription factor"="#B2DF8A",
              "carbonhydrate active enzymes"="#33A02C",
              "sugar transporter or sugar binding protein"="#FB9A99",
              "UNKNOWN"="#A6CEE3")
sub_genes = subset(example_genes, molecule=="Genome5")
sub_genes$classification <- c("Transcription factor", "Transcription factor", "UNKNOWN",
"mono saccharide metabolic enzymes", "mono saccharide metabolic enzymes",
"carbonhydrate associating enzymes", "carbonhydrate associating enzymes", "
sugar transporter or sugar binding protein", "Transcription factor", "UNKNOWN")

gg <- ggplot(sub_genes, aes(xmin = start, xmax = end, y = molecule, fill=classification,
                          forward = orientation, labels=gene)) +
  geom_gene_arrow() +
  facet_wrap(~ molecule, scales = "free", ncol = 1) +
  scale_fill_manual(values=myColors) +
  theme_genes() +
  annotate("text",x=405113,y=1.1,label="genA") +
  annotate("text",x=405113,y=1.1,label="genA") +
  theme(legend.position="top",
        legend.title = element_text( size=12, face="bold"),
        axis.title.y=element_blank()) +
  guides(fill=guide_legend(nrow=5,byrow=TRUE)) +
  xlab("species name")

hg <- encodeGraphic(gg)

