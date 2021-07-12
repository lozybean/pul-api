import ast
import pandas as pd
from pathlib import Path

FILE_DIR = Path('../deploy/pul/external')


def process_pul():
    df = pd.read_csv(FILE_DIR / 'pul_information', sep='\t')
    df = (df.rename(columns={'PUL_id': 'id', 'PUL_type': 'type', 'GCF_number': 'GCF_id',
                             'PUL_locus_start': 'locus_start', 'PUL_locus_end': 'locus_end'})
              .assign(GCF_number=lambda _df: _df['GCF_id'].map('GCF_{:09d}'.format),
                      PUL_id=lambda _df: _df[['GCF_number', 'contig_name', 'locus_start', 'locus_end']]
                      .astype(str)
                      .agg('_'.join, axis=1))
              .loc[:, ['id', 'PUL_id', 'type', 'GCF_id', 'GCF_number',
                       'taxid', 'contig_name', 'locus_start', 'locus_end']])
    df.to_parquet(FILE_DIR / 'pul.parquet', engine='pyarrow', compression='snappy')
    return df


def literal_return(val):
    if val == 'unknown' or pd.isna(val):
        return []
    else:
        return ast.literal_eval(val)


def process_pul_gene():
    df = pd.read_csv(FILE_DIR / 'pul_protein', sep='\t')
    df_pul_id = (pd.read_parquet(FILE_DIR / 'pul.parquet')[['id', 'PUL_id']]
                 .rename(columns={'PUL_id': 'PUL_name'}))

    df = (df.rename(columns={'protein_id': 'id',
                             'protein_locus_start': 'locus_start',
                             'protein_locus_end': 'locus_end'})
              .merge(df_pul_id, left_on='PUL_id', right_on='id', suffixes=('', '_y'))
              .assign(gene_name=lambda _df: _df['PUL_name'] + '.' + (df.groupby('PUL_id').cumcount() + 1).astype(str),
                      domain=lambda _df: _df['domain'].apply(literal_return))
              .loc[:, ['id', 'gene_name', 'PUL_id', 'locus_start', 'locus_end',
                       'domain', 'score', 'classification']])
    df.to_parquet(FILE_DIR / 'gene.parquet', engine='pyarrow', compression='snappy')
    return df


def process_species():
    df = pd.read_csv(FILE_DIR / 'species_information', sep='\t')
    phyla = df.phyla_information.str.split('-')
    df = (df.assign(Kingdom=phyla.apply(lambda x: x[0]),
                    Phylum=phyla.apply(lambda x: x[1] if len(x) > 1 else None),
                    Class=phyla.apply(lambda x: x[2] if len(x) > 2 else None),
                    Order=phyla.apply(lambda x: x[3] if len(x) > 3 else None),
                    Family=phyla.apply(lambda x: x[4] if len(x) > 4 else None),
                    Genus=phyla.apply(lambda x: x[5] if len(x) > 5 else None))
              .rename(columns={'species_name': 'Species'})
              .loc[:, ['taxid', 'Kingdom', 'Phylum', 'Class', 'Order',
                       'Family', 'Genus', 'Species', 'assemble_level', 'phyla_information']])
    df.to_parquet(FILE_DIR / 'species.parquet', engine='pyarrow', compression='snappy')
    return df


if __name__ == '__main__':
    process_pul()
    process_pul_gene()
    process_species()
