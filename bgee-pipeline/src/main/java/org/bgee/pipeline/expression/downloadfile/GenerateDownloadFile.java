package org.bgee.pipeline.expression.downloadfile;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.species.SpeciesDAO;
import org.bgee.model.dao.api.species.SpeciesDAO.SpeciesTO;
import org.bgee.model.dao.api.species.SpeciesDAO.SpeciesTOResultSet;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.pipeline.CommandRunner;
import org.bgee.pipeline.expression.CallUser;


/**
 * This abstract class provides convenient common methods that generate TSV download files 
 * from the Bgee database.
 * 
 * @author Valentine Rech de Laval
 * @version Bgee 13
 * @since Bgee 13
 */
public abstract class GenerateDownloadFile extends CallUser {
    
    /**
     * {@code Logger} of the class.
     */
    private final static Logger log = LogManager.getLogger(GenerateDownloadFile.class.getName());
        
    /**
     * A {@code String} that is the name of the column containing gene IDs, in the download file.
     */
    public final static String GENE_ID_COLUMN_NAME = "Gene ID";

    /**
     * A {@code String} that is the name of the column containing gene names, in the download file.
     */
    public final static String GENE_NAME_COLUMN_NAME = "Gene name";

    /**
     * A {@code String} that is the name of the column containing developmental stage IDs, 
     * in the download file.
     */
    public final static String STAGE_ID_COLUMN_NAME = "Developmental stage ID";

    /**
     * A {@code String} that is the name of the column containing developmental stage names, 
     * in the download file.
     */
    public final static String STAGE_NAME_COLUMN_NAME = "Developmental stage name";

    /**
     * A {@code String} that is the name of the column containing anatomical entity IDs, 
     * in the download file.
     */
    public final static String ANATENTITY_ID_COLUMN_NAME = "Anatomical entity ID";

    /**
     * A {@code String} that is the name of the column containing anatomical entity names, 
     * in the download file.
     */
    public final static String ANATENTITY_NAME_COLUMN_NAME = "Anatomical entity name";

    /**
     * A {@code String} that is the name of the column containing expression, no-expression or
     * differential expression found with Affymetrix experiment, in the download file.
     */
    public final static String AFFYMETRIX_DATA_COLUMN_NAME = "Affymetrix data";

    /**
     * A {@code String} that is the name of the column containing expression, no-expression or
     * differential expression found with RNA-Seq experiment, in the download file.
     */
    public final static String RNASEQ_DATA_COLUMN_NAME = "RNA-Seq data";

    /**
     * A {@code String} that is the extension of download files to be generated.
     */
    public final static String EXTENSION = ".tsv";

    /**
     * A {@code List} of {@code String}s that are the IDs of species allowing 
     * to filter the calls to retrieve.
     */
    protected static List<String> speciesIds;
    
    /**
     * A {@code Set} of {@code String}s that are the file types to be generated.
     */
    protected static Set<String> fileTypes;
    
    /**
     * A {@code String} that is the directory to store the generated files.
     */
    protected static String directory;
    
    /**
     * An {@code interface} that must be implemented by {@code Enum}s representing a file type.
     * 
     * @author Valentine Rech de Laval
     * @version Bgee 13
     * @since Bgee 13
     */
    public interface FileType {
        /**
         * @return   A {@code String} that can be used to generate names of files of this type.
         */
        public String getStringRepresentation();

        /**
         * @return   A {@code boolean} defining whether this {@code FileType} is a simple file type.
         */
        public boolean isSimpleFileType();
    }

    /**
     * Default constructor, that will load the default {@code DAOManager} to be used. 
     */
    public GenerateDownloadFile() {
        this(null);
    }

    /**
     * Constructor providing the {@code MySQLDAOManager} that will be used by 
     * this object to perform queries to the database. This is useful for unit testing.
     * 
     * @param manager   the {@code MySQLDAOManager} to use.
     */
    public GenerateDownloadFile(MySQLDAOManager manager) {
        super(manager);
    }

    /**
     * Get the requested class parameters.
     *
     * @param args          An {@code Array} of {@code String}s containing the requested parameters.
     */
    protected static void setClassParameters(String[] args) {
        log.entry((Object[]) args);
        
        int expectedArgLengthWithoutSpecies = 2;
        int expectedArgLengthWithSpecies = 3;
    
        if (args.length != expectedArgLengthWithSpecies &&
                args.length != expectedArgLengthWithoutSpecies) {
            throw log.throwing(new IllegalArgumentException(
                    "Incorrect number of arguments provided, expected " + 
                    expectedArgLengthWithoutSpecies + " or " + expectedArgLengthWithSpecies + 
                    " arguments, " + args.length + " provided."));
        }
              
        if (args.length == expectedArgLengthWithSpecies) {
            speciesIds.addAll(CommandRunner.parseListArgument(args[0]));
            fileTypes.addAll(CommandRunner.parseListArgument(args[1])); 
            directory  = args[2];
        } else {
            fileTypes.addAll(CommandRunner.parseListArgument(args[0])); 
            directory  = args[1];
        }
        
        log.exit();
    }

    /**
     * Add gene, anatomical entity, and stage IDs and names to the provided {@code row}.
     * <p>
     * The provided {@code Map row} will be modified.
     *
     * @param row               A {@code Map} where keys are {@code String}s that are column names,
     *                          the associated values being a {@code String} that is the value
     *                          for the call. 
     * @param geneId            A {@code String} that is the ID of the gene.
     * @param geneName          A {@code String} that is the name of the gene.
     * @param anatEntityId      A {@code String} that is the ID of the anatomical entity.
     * @param anatEntityName    A {@code String} that is the name of the anatomical entity.
     * @param stageId           A {@code String} that is the ID of the stage.
     * @param stageName         A {@code String} that is the name of the stage.
     */
    protected void addIdsAndNames(Map<String, String> row, String geneId, String geneName, 
            String anatEntityId, String anatEntityName, String stageId, String stageName) {
        log.entry(row, geneId, geneName, anatEntityId, anatEntityName, stageId, stageName);
        
        if (StringUtils.isBlank(geneId)) {
            throw log.throwing(new IllegalArgumentException("No Id provided for gene."));
        }
        if (StringUtils.isBlank(stageId)) {
            throw log.throwing(new IllegalArgumentException("No Id provided for stage."));
        }
        if (StringUtils.isBlank(anatEntityId)) {
            throw log.throwing(new IllegalArgumentException("No Id provided for anat entity."));
        }
        //gene name can sometimes be empty, we don't check it
        if (StringUtils.isBlank(anatEntityName)) {
            throw log.throwing(new IllegalArgumentException("No name provided " +
                    "for anatomical entity ID " + anatEntityId));
        }
        if (StringUtils.isBlank(stageName)) {
            throw log.throwing(new IllegalArgumentException("No name provided " +
                    "for stage ID " + stageId));
        }
        row.put(GENE_ID_COLUMN_NAME, geneId);
        row.put(GENE_NAME_COLUMN_NAME, geneName);
        row.put(ANATENTITY_ID_COLUMN_NAME, anatEntityId);
        row.put(ANATENTITY_NAME_COLUMN_NAME, anatEntityName);
        row.put(STAGE_ID_COLUMN_NAME, stageId);
        row.put(STAGE_NAME_COLUMN_NAME, stageName);

        log.exit();
    }

    /**
     * Validate and retrieve information for the provided species IDs, or for all species 
     * if {@code speciesIds} is {@code null} or empty, and returns a {@code Map} 
     * where keys are the species IDs, the associated values being a {@code String} that can be 
     * conveniently used to construct download file names for the associated species. 
     * It is the latin name with all whitespace replaced by "_".
     * <p>
     * If a species ID could not be identified, an {@code IllegalArgumentException} is thrown.
     * 
     * @param speciesIds    A {@code Set} of {@code String}s that are the species IDs 
     *                      to be checked, and for which to generate a {@code String} 
     *                      used to construct download file names. Can be {@code null} or empty 
     *                      to retrieve information for all species. 
     * @return              A {@code Map} where keys are {@code String}s that are the species IDs, 
     *                      the associated values being a {@code String} that is its latin name, 
     *                      with whitespace replaced by "_".
     */
    protected Map<String, String> checkAndGetLatinNamesBySpeciesIds(Set<String> speciesIds) {
        log.entry(speciesIds);
        
        Map<String, String> namesByIds = new HashMap<String, String>();
        SpeciesDAO speciesDAO = this.getSpeciesDAO();
        speciesDAO.setAttributes(SpeciesDAO.Attribute.ID, SpeciesDAO.Attribute.GENUS, 
                SpeciesDAO.Attribute.SPECIES_NAME);
        
        try (SpeciesTOResultSet rs = speciesDAO.getSpeciesByIds(speciesIds)) {
            while (rs.next()) {
                SpeciesTO speciesTO = rs.getTO();
                if (StringUtils.isBlank(speciesTO.getId()) || 
                        StringUtils.isBlank(speciesTO.getGenus()) || 
                        StringUtils.isBlank(speciesTO.getSpeciesName())) {
                    throw log.throwing(new IllegalStateException("Incorrect species " +
                            "information retrieved: " + speciesTO));
                    
                }
                //in case there is a white space in a species name, we do not simply 
                //concatenate using "_", we replace all white spaces
                String latinNameForFile = 
                        speciesTO.getGenus() + " " + speciesTO.getSpeciesName();
                latinNameForFile = latinNameForFile.replace(" ", "_");
                namesByIds.put(speciesTO.getId(), latinNameForFile);
            }
        }
        if (namesByIds.size() < speciesIds.size()) {
            //copy to avoid modifying user input, maybe the caller 
            //will recover from the exception
            Set<String> copySpeciesIds = new HashSet<String>(speciesIds);
            copySpeciesIds.removeAll(namesByIds.keySet());
            throw log.throwing(new IllegalArgumentException("Some species IDs provided " +
                    "do not correspond to any species: " + copySpeciesIds));
        } else if (namesByIds.size() > speciesIds.size() && speciesIds.size() > 0) {
            // if speciesIds is empty or null to get all species contained in database, 
            // speciesIds.size() == 0 but this exception should not be launched
            throw log.throwing(new IllegalStateException("An ID should always be associated " +
                    "to only one species..."));
        }
        
        return log.exit(namesByIds);
    }
    
    /**
     * Rename temporary files. 
     *
     * @param generatedFileNames    A {@code Map} where keys are {@code FileType}s corresponding to 
     *                              which type of file should be generated, the associated values
     *                              being {@code String}s corresponding to files names.  
     * @param directory             A {@code String} that is the directory to store  
     *                              the generated files. 
     * @param tmpExtension          A {@code String} that is the temporary extension used to write 
     *                              temporary files.
     */
    protected void renameTempFiles(String directory,
            Map<FileType, String> generatedFileNames, String tmpExtension) {
        for (String fileName: generatedFileNames.values()) {
            //if temporary file exists, rename it.
            File tmpFile = new File(directory, fileName + tmpExtension);
            File file = new File(directory, fileName);
            if (tmpFile.exists()) {
                tmpFile.renameTo(file);
            }
        }
    }

    /**
     * Delete temporary files. 
     *
     * @param generatedFileNames    A {@code Map} where keys are {@code FileType}s corresponding to 
     *                              which type of file should be generated, the associated values
     *                              being {@code String}s corresponding to files names.  
     * @param directory             A {@code String} that is the directory to store 
     *                              the generated files. 
     * @param tmpExtension          A {@code String} that is the temporary extension used to write 
     *                              temporary files.
     */
    protected void deleteTempFiles(String directory,
            Map<FileType, String> generatedFileNames, String tmpExtension) {
        for (String fileName: generatedFileNames.values()) {
            //if tmp file exists, remove it.
            File file = new File(directory, fileName + tmpExtension);
            if (file.exists()) {
                file.delete();
            }
        }
    }
}