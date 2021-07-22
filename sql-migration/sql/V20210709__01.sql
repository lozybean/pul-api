CREATE EXTENSION if not exists parquet_fdw;
CREATE SERVER if not exists pul_parquet FOREIGN DATA WRAPPER parquet_fdw;
create foreign table if not exists public.foreign_pul (
    id varchar(16),
    pul_id varchar(64),
    type varchar(16),
    gcf_id int,
    contig_name varchar(32),
    locus_start int,
    locus_end int
    ) server pul_parquet
    options (
    filename '/mnt/data/pul.parquet',
    use_threads 'true'
    );
create foreign table if not exists public.foreign_gene (
    id varchar(32),
    gene_name varchar(64),
    pul_id varchar(16),
    domain varchar(64)[],
    classification varchar(64),
    locus_start int,
    locus_end int,
    score double precision
    ) server pul_parquet
    options (
    filename '/mnt/data/gene.parquet',
    use_threads 'true'
    );
create foreign table if not exists public.foreign_species (
    id int,
    gcf_number varchar(16),
    taxid int,
    "kingdom" varchar(64),
    "phylum" varchar(64),
    "class" varchar(64),
    "order" varchar(64),
    "family" varchar(64),
    "genus" varchar(64),
    "species" varchar(128),
    "assemble_level" varchar(16),
    "phyla_information" varchar(200)
    ) server pul_parquet
    options (
    filename '/mnt/data/species.parquet',
    use_threads 'true'
    );

create table if not exists public.pul
(
    id          varchar(16),
    pul_id      varchar(64),
    type        varchar(16),
    gcf_id      int,
    contig_name varchar(32),
    locus_start int,
    locus_end   int,
    primary key (id)
);
create index pul_type_index on public.pul (type);
create index pul_gcf_id_index on public.pul (gcf_id);
create table if not exists public.gene
(
    id             varchar(32),
    gene_name      varchar(64),
    pul_id         varchar(16),
    domain         varchar(64)[],
    classification varchar(64),
    locus_start    int,
    locus_end      int,
    score          double precision,
    primary key (id)
);
create index gene_pul_id_index on public.gene (pul_id);
create index gene_domain_index on public.gene (domain);
create table if not exists public.species
(
    id                  int,
    gcf_number          varchar(16),
    taxid               int,
    "kingdom"           varchar(64) null,
    "phylum"            varchar(64) null,
    "class"             varchar(64) null,
    "order"             varchar(64) null,
    "family"            varchar(64) null,
    "genus"             varchar(64) null,
    "species"           varchar(128) null,
    "assemble_level"    varchar(16) null,
    "phyla_information" varchar(200) null,
    primary key (id)
);
create index species_gcf_id_index on public.species (id);
create index species_gcf_number_index on public.species (gcf_number);
create index species_taxid_index on public.species (taxid);
create index species_phylum_index on public.species (phylum);
create index species_species_index on public.species (species);