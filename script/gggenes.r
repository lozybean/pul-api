library(ggplot2)
library(gggenes)
library(base64enc)

encodeGraphic <- function(g) {
  fn <- tempfile(fileext = ".png")
  ggsave(fn, g, width = 10, height = 2, unit='in', dpi = 300)
  img <- base64enc::dataURI(file = fn, mime = "image/png")
  return(img)
}


gg <- ggplot(sub_genes, aes(xmin = start, xmax = end, y = molecule, fill = gene,
                          forward = orientation, labels=gene)) +
  geom_gene_arrow() +
  facet_wrap(~ molecule, scales = "free", ncol = 1) +
  discrete_scale("fill", "manual", palette_Dark2) +
  theme_genes() +
  annotate("text",x=405113,y=1.1,label="genA") +
  annotate("text",x=405113,y=1.1,label="genA") +
  theme(legend.position="none")

hg <- encodeGraphic(gg)

