FROM postgres:13.3 as builder

RUN apt update \
    && apt install -y -V ca-certificates lsb-release wget \
    && wget https://apache.jfrog.io/artifactory/arrow/$(lsb_release --id --short | tr 'A-Z' 'a-z')/apache-arrow-apt-source-latest-$(lsb_release --codename --short).deb \
    && apt install -y -V ./apache-arrow-apt-source-latest-$(lsb_release --codename --short).deb \
    && apt update \
    && apt install -y -V libarrow-dev libparquet-dev
RUN apt install -y -V make g++ postgresql-server-dev-13
COPY parquet_fdw-0.2.tar.gz ./parquet_fdw-0.2.tar.gz

RUN tar zxvf ./parquet_fdw-0.2.tar.gz \
    && cd ./parquet_fdw-0.2 \
    && make install

FROM postgres:13.3

COPY --from=builder /usr/lib/x86_64-linux-gnu/* /usr/lib/x86_64-linux-gnu/
COPY --from=builder /usr/lib/postgresql/13/lib/parquet_fdw.so /usr/lib/postgresql/13/lib/
COPY --from=builder /usr/share/postgresql/13/extension/parquet_fdw* /usr/share/postgresql/13/extension/
