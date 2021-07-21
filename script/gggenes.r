library(ggplot2)
library(gggenes)
library(base64enc)

encodeGraphic <- function(g) {
  fn <- tempfile(fileext = ".png")
  ggsave(fn, g)
  img <- base64enc::dataURI(file = fn, mime = "image/png")
  return(img)
}


gg <- ggplot(example_genes, aes(xmin = start, xmax = end, y = molecule, fill = gene,
                          forward = orientation)) +
  geom_gene_arrow() +
  facet_wrap(~ molecule, scales = "free", ncol = 1) +
  scale_fill_brewer(palette = "Set3") +
  theme_genes()

hg <- encodeGraphic(gg)

