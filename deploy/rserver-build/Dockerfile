FROM r-base:4.1.0

RUN apt-get update && apt-get install tini

RUN echo 'options(BioC_mirror="https://mirrors.tuna.tsinghua.edu.cn/bioconductor")' >> ~/.Rprofile && \
	echo 'options("repos" = c(CRAN="https://mirrors.tuna.tsinghua.edu.cn/CRAN/"))' >> ~/.Rprofile
RUN R -e 'install.packages("Rserve",,"http://rforge.net")'
RUN R -e 'install.packages(c("ggplot2", "gggenes"), dependencies = TRUE)'
RUN R -e 'install.packages(c("ggplot2", "gggenes"))'
RUN R -e 'install.packages("Rserve",,"http://rforge.net")'

EXPOSE 6311
ENTRYPOINT R -e "Rserve::run.Rserve(remote=TRUE)"
