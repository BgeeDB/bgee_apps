package org.bgee.pipeline.expression.downloadfile;

import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;

/**
 * This abstract class provides convenient common methods that generate multi-species TSV 
 * download files from the Bgee database.
 * 
 * @author Valentine Rech de Laval
 * @version Bgee 13
 * @since 	Bgee 13
 */
public abstract class GenerateMultiSpeciesDownloadFile extends GenerateDownloadFile {

    /**
     * {@code Logger} of the class.
     */
    private final static Logger log = LogManager.getLogger(GenerateMultiSpeciesDownloadFile.class.getName());

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
    public final static String NB_NA_GENES_COLUMN_NAME = "Nb N/A genes"; // TODO
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
     * A {@code String} that is the name of the column containing latin species names, 
     * in the download file.
     */
    public final static String SPECIES_LATIN_NAME_COLUMN_NAME = "Latin species name";
    
    /**
     * A {@code String} that is the IDs of the common ancestor taxon we want to into account. 
     * If {@code null} or empty, TODO .
     */
    protected String taxonId;

    /**
     * Default constructor, that will load the default {@code DAOManager} to be used. 
     */
    //suppress warning as this default constructor should not be used.
    @SuppressWarnings("unused")
    private GenerateMultiSpeciesDownloadFile() throws IllegalArgumentException {
        this(null, null, null, null, null);
    }

    /**
     * Constructor providing parameters to generate files, and using the default {@code DAOManager}.
     * 
     * @param speciesIds    A {@code List} of {@code String}s that are the IDs of species 
     *                      we want to generate data for. If {@code null} or empty, all species 
     *                      are used.
     * @param taxonId       A {@code String} that is the ID of the common ancestor taxon
     *                      we want to into account. If {@code null} or empty, TODO .
     * @param fileTypes     A {@code Set} of {@code FileType}s that are the types
     *                      of files we want to generate. If {@code null} or empty, 
     *                      all {@code FileType}s of the given type are generated.
     * @param directory     A {@code String} that is the directory where to store files.
     * @throws IllegalArgumentException If {@code directory} is {@code null} or blank.
     */
    public GenerateMultiSpeciesDownloadFile(List<String> speciesIds, String taxonId,
            Set<? extends FileType> fileTypes, String directory) 
                    throws IllegalArgumentException {
        this(null, speciesIds, taxonId, fileTypes, directory);
    }

    /**
     * Constructor providing parameters to generate files, and the {@code MySQLDAOManager} that will  
     * be used by this object to perform queries to the database. This is useful for unit testing.
     * 
     * @param manager       the {@code MySQLDAOManager} to use.
     * @param speciesIds    A {@code List} of {@code String}s that are the IDs of species 
     *                      we want to generate data for. If {@code null} or empty, all species 
     *                      are used.
     * @param taxonId       A {@code String} that is the ID of the common ancestor taxon
     *                      we want to into account. If {@code null} or empty, TODO .
     * @param fileTypes     A {@code Set} of {@code FileType}s that are the types
     *                      of files we want to generate. If {@code null} or empty, 
     *                      all {@code FileType}s of the given type are generated .
     * @param directory     A {@code String} that is the directory where to store files.
     * @throws IllegalArgumentException If {@code directory} is {@code null} or blank.
     */
    public GenerateMultiSpeciesDownloadFile(MySQLDAOManager manager, List<String> speciesIds, 
            String taxonId, Set<? extends FileType> fileTypes, String directory)
                    throws IllegalArgumentException {
        super(manager, speciesIds, fileTypes, directory);
        this.taxonId = taxonId;
    }
}
