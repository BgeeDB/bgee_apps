package org.bgee.pipeline.expression.downloadfile;

import java.io.File;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.expressiondata.CallDAO.CallTO.DataState;
import org.bgee.model.dao.api.expressiondata.DiffExpressionCallDAO.DiffExpressionCallTO.ComparisonFactor;
import org.bgee.model.dao.api.species.SpeciesDAO;
import org.bgee.model.dao.api.species.SpeciesDAO.SpeciesTO;
import org.bgee.model.dao.api.species.SpeciesDAO.SpeciesTOResultSet;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.model.expressiondata.baseelements.CallType.Expression;
import org.bgee.model.expressiondata.baseelements.DataQuality;
import org.bgee.model.expressiondata.baseelements.SummaryCallType.ExpressionSummary;
import org.bgee.model.file.DownloadFile.CategoryEnum;
import org.bgee.pipeline.expression.CallUser;


/**
 * This abstract class provides convenient common methods that generate TSV download files 
 * from the Bgee database.
 * 
 * @author Valentine Rech de Laval
 * @author Frederic Bastian
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
     * A {@code String} that is the name of the column containing call quality found 
     * with Affymetrix experiment, in the download file.
     */
    public final static String AFFYMETRIX_CALL_QUALITY_COLUMN_NAME = "Affymetrix call quality";
       /**
     * A {@code String} that is the name of the column containing if an Affymetrix experiment 
     * is observed, in the download file.
     */
    public final static String AFFYMETRIX_OBSERVED_DATA_COLUMN_NAME = 
            "Including Affymetrix observed data"; 
    /**
     * A {@code String} that is the name of the column containing expression, no-expression or
     * differential expression found with RNA-Seq experiment, in the download file.
     */
    public final static String RNASEQ_DATA_COLUMN_NAME = "RNA-Seq data";
    /**
     * A {@code String} that is the name of the column containing call quality found 
     * with RNA-Seq experiment, in the download file.
     */
    public final static String RNASEQ_CALL_QUALITY_COLUMN_NAME = "RNA-Seq call quality";
    /**
     * A {@code String} that is the name of the column containing if a RNA-Seq experiment 
     * is observed, in the download file.
     */
    public final static String RNASEQ_OBSERVED_DATA_COLUMN_NAME = "Including RNA-Seq observed data"; 
    /**
     * A {@code String} that is the name of the column containing the merged quality of the call,
     * in the download file.
     */
    public final static String QUALITY_COLUMN_NAME = "Call quality";
    /**
     * A {@code String} that is the value of the cell containing not applicable,
     * in the download file.
     */
    public final static String NA_VALUE = "NA";
    /**
     * A {@code String} that is the extension of download files to be generated.
     */
    public final static String EXTENSION = ".tsv";
    /**
     * A {@code String} that is the value of the cell containing not applicable,
     * in the download file.
     */
    public final static String NO_DATA_VALUE = "no data";
    /**
     * A {@code String} that is the low quality data text, in the download file.
     */
    public final static String LOW_QUALITY_TEXT = "low quality";
    /**
     * A {@code String} that is the high quality data text, in the download file.
     */
    public final static String HIGH_QUALITY_TEXT = "high quality";

    /**
     * A {@code String} that is the presence of expression text for a call data, in the download file.
     */
    public final static String PRESENT_TEXT = "present";
    /**
     * A {@code String} that is the absence of expression text for a call data, in the download file.
     */
    public final static String ABSENT_TEXT = "absent";
    /**
     * A {@code String} that is the weak ambiguity text for a call data, in the download file.
     */
    public final static String WEAK_AMBIGUITY = "weak ambiguity";
    /**
     * A {@code String} that is the strong ambiguity text for a call data, in the download file.
     */
    public final static String STRONG_AMBIGUITY= "strong ambiguity";

    /**
     * An {@code Enum} used to define whether the call has been observed. This is to distinguish
     * from propagated data only, that should provide a lower confidence in the call.
     * <ul>
     * <li>{@code OBSERVED}:    the call has been observed at least once.
     * <li>{@code NOTOBSERVED}: the call has never been observed.
     * </ul>
     * 
     * @author Valentine Rech de Laval
     * @version Bgee 13
     * @since Bgee 13
     */
    public enum ObservedData {
        OBSERVED("yes"), NOT_OBSERVED("no");

        private final String stringRepresentation;

        /**
         * Constructor providing the {@code String} representation of this {@code ObservedData}.
         * 
         * @param stringRepresentation A {@code String} corresponding to this {@code ObservedData}.
         */
        private ObservedData(String stringRepresentation) {
            this.stringRepresentation = stringRepresentation;
        }

        public String getStringRepresentation() {
            return this.stringRepresentation;
        }

        @Override
        public String toString() {
            return this.getStringRepresentation();
        }
    }

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
         * @return   A {@code CategoryEnum} that is the category of files of this type.
         */
        public CategoryEnum getCategory();

        /**
         * @return   A {@code boolean} defining whether this {@code FileType} is a simple file type.
         */
        public boolean isSimpleFileType();
    }
    
    /**
     * An {@code interface} that must be implemented by {@code Enum}s representing a file type
     * containing differential expression calls.
     * 
     * @author Valentine Rech de Laval
     * @version Bgee 13
     * @since Bgee 13
     */
    public interface DiffExprFileType extends FileType {
        /**
         * @return   A {@code ComparisonFactor} defining what is the experimental factor 
         *           compared that generated the differential expression calls.
         */
        public ComparisonFactor getComparisonFactor();
    }

    /**
     * Convert {@code fileTypeNames} into a {@code Set} of {@code FileType}s of type 
     * {@code fileType}. 
     * 
     * @param fileTypeNames A {@code Collection} of {@code String}s corresponding to either 
     *                      the value returned by the method {@code getStringRepresentation}, 
     *                      or to the name of the {@code enum}, of some {@code FileType}s 
     *                      of type {@code fileType}.
     * @param fileType      The {@code Class} defining the type of {@code FileType} that 
     *                      should be retrieved. 
     * @return              A {@code Set} of {@code FileType}s of type {@code fileType}, 
     *                      corresponding to {@code fileTypeNames}.
     * @throws IllegalArgumentException If a {@code String} in {@code fileTypeNames} could not 
     *                                  be converted into a valid {@code FileType}.
     */
    protected static <T extends Enum<T> & FileType> Set<T> convertToFileTypes(
            Collection<String> fileTypeNames, Class<T> fileType) throws IllegalArgumentException {
        log.entry(fileTypeNames, fileType);
        
        Set<T> fileTypes = EnumSet.noneOf(fileType);
        fileTypeName: for (String fileTypeName: fileTypeNames) {
            for (T element: fileType.getEnumConstants()) {
                if (element.getStringRepresentation().equals(fileTypeName) || 
                        element.name().equals(fileTypeName)) {
                    fileTypes.add(element);
                    continue fileTypeName;
                }
            }
            throw log.throwing(new IllegalArgumentException("\"" + fileTypeName + 
                    "\" does not correspond to any element of " + fileType.getName()));
        }
        
        return log.exit(fileTypes);
    }

    /**
     * Convert a {@code org.bgee.model.dao.api.expressiondata.CallDAO.CallTO.DataState}
     * into a {@code String}. This is because its method {@code getStringRepresentation}
     * is not convenient for display in files.
     * 
     * @param dataState A {@code DataState} to be converted.
     * @return          A {@code String} corresponding to {@code dataState}, to be used in files.
     */
    protected static String convertDataStateToString(DataState dataState) {
        log.entry(dataState);
        if (DataState.HIGHQUALITY.equals(dataState)) {
            return log.exit(HIGH_QUALITY_TEXT);
        }
        if (DataState.LOWQUALITY.equals(dataState)) {
            return log.exit(LOW_QUALITY_TEXT);
        }
        return log.exit(NO_DATA_VALUE);
    }

    /**
     * Convert a {@code String} into a {@code org.bgee.model.dao.api.expressiondata.CallDAO.CallTO.DataState}
     * This is because its method {@code getStringRepresentation} is not convenient for display in files.
     * 
     * @param string    A {@code String} to be converted.
     * @return          A {@code DataState} corresponding to {@code string}, to be used in files.
     */
    protected static DataState convertStringToDataState(String string) {
        log.entry(string);
        if (string.equals(HIGH_QUALITY_TEXT)) {
            return log.exit(DataState.HIGHQUALITY);
        }
        if (string.equals(LOW_QUALITY_TEXT)) {
            return log.exit(DataState.LOWQUALITY);
        }
        return log.exit(DataState.NODATA);
    }

    /**
     * Convert an {@code org.bgee.model.expressiondata.baseelements.SummaryCallType.ExpressionSummary ExpressionSummary}
     * into a {@code String}.
     * <p>  
     * This is because its method {@code getStringRepresentation} is not available for display in files.
     * 
     * @param sum   An {@code ExpressionSummary} to be converted.
     * @return      The {@code String} corresponding to {@code sum}, to be used in files
     */
    protected static String convertExpressionSummaryToString(ExpressionSummary sum) {
        log.entry(sum);
        if (sum == null) {
            throw new IllegalArgumentException("ExpressionSummary could not be null");
        }
        switch (sum) {
        case EXPRESSED:
            return log.exit(PRESENT_TEXT);
        case NOT_EXPRESSED:
            return log.exit(ABSENT_TEXT);
        case WEAK_AMBIGUITY:
            return log.exit(WEAK_AMBIGUITY);
        case STRONG_AMBIGUITY:
            return log.exit(STRONG_AMBIGUITY);
        default:
            throw new IllegalArgumentException("Unrecognized ExpressionSummary: " + sum);
        }
    }
    
    /**
     * Convert an {@code org.bgee.model.expressiondata.baseelements.CallType.Expression Expression}
     * into a {@code String}.
     * <p>  
     * This is because its method {@code getStringRepresentation} is not available for display in files.
     * 
     * @param expr  An {@code Expression} to be converted.
     * @return      The {@code String} corresponding to {@code expr}, to be used in files
     */
    protected static String convertExpressionToString(Expression expr) {
        log.entry(expr);
        if (expr == null) {
            return log.exit(NO_DATA_VALUE);
        }
        switch (expr) {
        case EXPRESSED:
            return log.exit(PRESENT_TEXT);
        case NOT_EXPRESSED:
            return log.exit(ABSENT_TEXT);
        default:
            throw new IllegalArgumentException("Unrecognized Expression: " + expr);
        }
    }

    /**
     * Convert a {@code org.bgee.model.expressiondata.baseelements.DataQuality DataQuality}
     * into a {@code String}.
     * <p>  
     * This is because its method {@code getStringRepresentation} is not available for display in files.
     * 
     * @param qual  A {@code DataQuality} to be converted.
     * @return      The {@code String} corresponding to {@code qual}, to be used in files
     */
    protected static String convertDataQualityToString(DataQuality qual) {
        log.entry(qual);
        if (qual == null) {
            return log.exit(NA_VALUE);
        }
        switch (qual) {
        case HIGH:
            return log.exit(HIGH_QUALITY_TEXT);
        case LOW:
            return log.exit(LOW_QUALITY_TEXT);
        case NODATA:
            return log.exit(NO_DATA_VALUE);
        default:
            throw new IllegalArgumentException("Unrecognized DataQuality: " + qual);
        }
    }

    /**
     * Convert a {@code org.bgee.model.expressiondata.baseelements.DataQuality DataQuality}
     * into a {@code String}.
     * <p>  
     * This is because its method {@code getStringRepresentation} is not available for display in files.
     * 
     * @param qual  A {@code DataQuality} to be converted.
     * @return      The {@code String} corresponding to {@code qual}, to be used in files
     */
    protected static String convertObservedDataToString(Boolean includingObservedData) {
        log.entry(includingObservedData);
        if (Boolean.TRUE.equals(includingObservedData)) {
            return log.exit(ObservedData.OBSERVED.getStringRepresentation());
        }
        return log.exit(ObservedData.NOT_OBSERVED.getStringRepresentation());
    }

    /**
     * A {@code List} of {@code String}s that are the IDs of species allowing 
     * to filter the calls to retrieve.
     */
    protected List<String> speciesIds;
    
    /**
     * A {@code List} of {@code String}s that are the file types to be generated.
     */
    //XXX: actually this could be a T extends FileType, with T provided by the extending class.
    //See for instance the need for a line such as : 
    //MultiSpDiffExprFileType currentFileType = (MultiSpDiffExprFileType) fileType;
    //in GenerateMultiSpeciesDiffExprFile.generateMultiSpeciesDiffExprFilesForOneGroup
    protected Set<? extends FileType> fileTypes;
    
    /**
     * A {@code String} that is the directory to store the generated files.
     */
    protected String directory;
    
    /**
     * Default constructor, that will load the default {@code DAOManager} to be used. 
     */
    //suppress warning as this default constructor should not be used.
    @SuppressWarnings("unused")
    private GenerateDownloadFile() {
        this(null, null, null, null);
    }
    /**
     * Constructor providing parameters to generate files, and using the default {@code DAOManager}.
     * 
     * @param speciesIds    A {@code List} of {@code String}s that are the IDs of species 
     *                      we want to generate data for. If {@code null} or empty, all species 
     *                      are used.
     * @param fileTypes     A {@code Set} of {@code FileType}s that are the types
     *                      of files we want to generate. If {@code null} or empty, 
     *                      all {@code FileType}s of the given type are generated.
     * @param directory     A {@code String} that is the directory where to store files.
     * @throws IllegalArgumentException If {@code directory} is {@code null} or blank.
     */
    public GenerateDownloadFile(List<String> speciesIds, Set<? extends FileType> fileTypes, 
            String directory) throws IllegalArgumentException {
        this(null, speciesIds, fileTypes, directory);
    }
    /**
     * Constructor providing the {@code MySQLDAOManager} that will be used by 
     * this object to perform queries to the database. This is useful for unit testing.
     * 
     * @param manager       the {@code MySQLDAOManager} to use.
     * @param speciesIds    A {@code List} of {@code String}s that are the IDs of species 
     *                      we want to generate data for. If {@code null} or empty, all species 
     *                      are used.
     * @param fileTypes     A {@code Set} of {@code FileType}s that are the types
     *                      of files we want to generate. If {@code null} or empty, 
     *                      all {@code FileType}s of the given type are generated .
     * @param directory     A {@code String} that is the directory where to store files.
     * @throws IllegalArgumentException If {@code directory} is {@code null} or blank.
     */
    //TODO: speciesIds shoudn't be defined for multi-species classes that use map. 
    // We need to reorganize generation download file classes.
    public GenerateDownloadFile(MySQLDAOManager manager, List<String> speciesIds, 
            Set<? extends FileType> fileTypes, String directory) throws IllegalArgumentException {
        super(manager);
        if (StringUtils.isBlank(directory)) {
            throw log.throwing(new IllegalArgumentException("A directory must be provided"));
        }
        this.speciesIds = speciesIds;
        this.fileTypes = fileTypes;
        
        if (directory == null || directory.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("No directory is provided"));
        }
        this.directory = directory;
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
     * <p>
     * If a species ID could not be identified, an {@code IllegalArgumentException} is thrown.
     * 
     * @param speciesIds    A {@code Set} of {@code String}s that are the species IDs 
     *                      to be checked, and for which to generate a {@code String} 
     *                      used to construct download file names. Can be {@code null} or empty 
     *                      to retrieve information for all species. 
     * @return              A {@code Map} where keys are {@code String}s that are the species IDs, 
     *                      the associated values being a {@code String} that is its latin name.
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
     * Format the provided {@code string} replacing whitespace by "_".
     *
     * @param word  A {@code String} that is the word to be used. 
     * @return      A {@code String} that is the modified word where whitespace are replaced by "_".
     */
    protected String formatString(String word) {
        log.entry(word);
        return log.exit(word.replaceAll(" ", "_"));
    }
    
    /**
     * Rename temporary files in directory provided at instantiation. 
     *
     * @param generatedFileNames    A {@code Map} where keys are {@code T}s corresponding to 
     *                              which type of file should be generated, the associated values
     *                              being {@code String}s corresponding to files names.  
     * @param tmpExtension          A {@code String} that is the temporary extension used to write 
     *                              temporary files.
     * @param <T>                   A {@code FileType} type parameter.
     */
    protected <T extends FileType> void renameTempFiles(
            Map<T, String> generatedFileNames, String tmpExtension) {
        log.entry(generatedFileNames, tmpExtension);
        
        for (String fileName: generatedFileNames.values()) {
            //if temporary file exists, rename it.
            File tmpFile = new File(this.directory, fileName + tmpExtension);
            File file = new File(this.directory, fileName);
            if (tmpFile.exists()) {
                tmpFile.renameTo(file);
            }
        }
        
        log.exit();
    }

    /**
     * Delete temporary files from directory provided at instantiation. 
     *
     * @param generatedFileNames    A {@code Map} where keys are {@code T}s corresponding to 
     *                              which type of file should be generated, the associated values
     *                              being {@code String}s corresponding to files names.  
     * @param tmpExtension          A {@code String} that is the temporary extension used to write 
     *                              temporary files.
     */
    protected <T extends FileType> void deleteTempFiles(
            Map<T, String> generatedFileNames, String tmpExtension) {
        log.entry(generatedFileNames, tmpExtension);
        
        for (String fileName: generatedFileNames.values()) {
            //if tmp file exists, remove it.
            File file = new File(this.directory, fileName + tmpExtension);
            if (file.exists()) {
                file.delete();
            }
        }
        
        log.exit();
    }
}