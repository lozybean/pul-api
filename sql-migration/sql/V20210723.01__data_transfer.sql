
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
    strand         smallint,
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
    "kingdom"           varchar(64)  null,
    "phylum"            varchar(64)  null,
    "class"             varchar(64)  null,
    "order"             varchar(64)  null,
    "family"            varchar(64)  null,
    "genus"             varchar(64)  null,
    "species"           varchar(128) null,
    "assemble_level"    varchar(16)  null,
    "phyla_information" varchar(200) null,
    primary key (id)
);
create index species_gcf_id_index on public.species (id);
create index species_gcf_number_index on public.species (gcf_number);
create index species_taxid_index on public.species (taxid);
create index species_phylum_index on public.species (phylum);
create index species_species_index on public.species (species);

insert into public.pul(id, pul_id, type, gcf_id, contig_name, locus_start, locus_end) select * from public.foreign_pul;
insert into public.gene(id, gene_name, pul_id, domain, classification, locus_start, locus_end, strand, score) select * from public.foreign_gene;
insert into public.species(id, gcf_number, taxid, kingdom, phylum, class, "order", family, genus, species, assemble_level, phyla_information) select * from public.foreign_species;