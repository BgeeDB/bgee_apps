package org.bgee.pipeline.expression.downloadfile;


/**
 * This interface provides convenient common methods that generate multi-species TSV 
 * download files from the Bgee database.
 * 
 * @author Valentine Rech de Laval
 * @version Bgee 13
 * @since 	Bgee 13
 */
public interface GenerateMultiSpeciesDownloadFile {

    /**
     * A {@code String} that is the name of the column containing OMA IDs, in the download file.
     */
    public final static String OMA_ID_COLUMN_NAME = "OMA ID";

//    /**
//     * A {@code String} that is the name of the column containing OMA descriptions,
//     * in the download file.
//     */
//    public final static String OMA_DESC_COLUMN_NAME = "OMA ID";

    /**
     * A {@code String} that is the name of the column containing lists of gene IDs,
     * in the download file.
     */
    public final static String GENE_ID_LIST_ID_COLUMN_NAME = "Gene IDs";
    /**
     * A {@code String} that is the name of the column containing lists of gene names,
     * in the download file.
     */
    public final static String GENE_NAME_LIST_ID_COLUMN_NAME = "Gene names";
    /**
     * A {@code String} that is the name of the column containing lists of anatomical entity IDs, 
     * in the download file.
     */
    public final static String ANAT_ENTITY_ID_LIST_ID_COLUMN_NAME = "Anatomical entity IDs";
    /**
     * A {@code String} that is the name of the column containing lists of anatomical entity names, 
     * in the download file.
     */
    public final static String ANAT_ENTITY_NAME_LIST_ID_COLUMN_NAME = "Anatomical entity names";
    /**
     * A {@code String} that is the name of the column containing CIO statement IDs, 
     * in the download file.
     */
    public final static String CIO_ID_ID_COLUMN_NAME = "CIO ID";
    /**
     * A {@code String} that is the name of the column containing CIO statement names, 
     * in the download file.
     */
    public final static String CIO_NAME_ID_COLUMN_NAME = "CIO name";
    /**
     * A {@code String} that is the name of the column containing number of expressed genes, 
     * in the download file.
     */
    public final static String NB_EXPR_GENE_COLUMN_NAME = "Nb expressed genes";
    /**
     * A {@code String} that is the name of the column containing number of not expressed genes, 
     * in the download file.
     */
    public final static String NB_NO_EXPR_GENES_COLUMN_NAME = "Nb not expressed genes";
    /**
     * A {@code String} that is the name of the column containing number of genes without data, 
     * in the download file.
     */
    public final static String NB_NA_GENES_COLUMN_NAME = "Nb N/A genes"; // TODO  to be decided
    /**
     * A {@code String} that is the name of the column containing number of over-expressed genes, 
     * in the download file.
     */
    public final static String NB_OVER_EXPR_GENES_COLUMN_NAME = "Nb over-expressed genes";
    /**
     * A {@code String} that is the name of the column containing number of under-expressed genes, 
     * in the download file.
     */
    public final static String NB_UNDER_EXPR_GENES_COLUMN_NAME = "Nb under-expressed genes";
    /**
     * A {@code String} that is the name of the column containing number of not diff. expressed 
     * genes, in the download file.
     */
    public final static String NB_NO_DIFF_EXPR_GENES_COLUMN_NAME = "Nb not diff. expressed genes";
    /**
     * A {@code String} that is the name of the column containing number of not diff. expressed 
     * genes, in the download file.
     */
    public final static String NB_NOT_EXPR_GENES_COLUMN_NAME = "Nb not expressed genes";
    /**
     * A {@code String} that is the name of the column containing latin species names, 
     * in the download file.
     */
    public final static String SPECIES_LATIN_NAME_COLUMN_NAME = "Latin species name";
    
}
