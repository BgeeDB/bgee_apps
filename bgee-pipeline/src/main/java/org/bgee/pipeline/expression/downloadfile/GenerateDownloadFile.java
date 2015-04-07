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
import org.bgee.model.dao.api.expressiondata.DiffExpressionCallDAO.DiffExpressionCallTO.ComparisonFactor;
import org.bgee.model.dao.api.species.SpeciesDAO;
import org.bgee.model.dao.api.species.SpeciesDAO.SpeciesTO;
import org.bgee.model.dao.api.species.SpeciesDAO.SpeciesTOResultSet;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
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
     * A {@code String} that is the name of the column containing the merged quality of the call,
     * in the download file.
     */
    public final static String QUALITY_COLUMN_NAME = "Call quality";
    /**
     * A {@code String} that is the value of the cell containing not applicable,
     * in the download file.
     */
    public final static String NA_VALUE = "N/A";
    /**
     * A {@code String} that is the extension of download files to be generated.
     */
    public final static String EXTENSION = ".tsv";

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
    //FIXME: how is this associated back to the name given to the group of species?
    public GenerateDownloadFile(MySQLDAOManager manager, List<String> speciesIds, 
            Set<? extends FileType> fileTypes, String directory) throws IllegalArgumentException {
        super(manager);
        if (StringUtils.isBlank(directory)) {
            throw log.throwing(new IllegalArgumentException("A directory must be provided"));
        }
        this.speciesIds = speciesIds;
        this.fileTypes = fileTypes;
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
     * Rename temporary files in directory provided at instantiation. 
     *
     * @param generatedFileNames    A {@code Map} where keys are {@code FileType}s corresponding to 
     *                              which type of file should be generated, the associated values
     *                              being {@code String}s corresponding to files names.  
     * @param tmpExtension          A {@code String} that is the temporary extension used to write 
     *                              temporary files.
     */
    protected void renameTempFiles(Map<FileType, String> generatedFileNames, String tmpExtension) {
        for (String fileName: generatedFileNames.values()) {
            //if temporary file exists, rename it.
            File tmpFile = new File(this.directory, fileName + tmpExtension);
            File file = new File(this.directory, fileName);
            if (tmpFile.exists()) {
                tmpFile.renameTo(file);
            }
        }
    }

    /**
     * Delete temporary files from directory provided at instantiation. 
     *
     * @param generatedFileNames    A {@code Map} where keys are {@code FileType}s corresponding to 
     *                              which type of file should be generated, the associated values
     *                              being {@code String}s corresponding to files names.  
     * @param tmpExtension          A {@code String} that is the temporary extension used to write 
     *                              temporary files.
     */
    protected void deleteTempFiles(Map<FileType, String> generatedFileNames, String tmpExtension) {
        for (String fileName: generatedFileNames.values()) {
            //if tmp file exists, remove it.
            File file = new File(this.directory, fileName + tmpExtension);
            if (file.exists()) {
                file.delete();
            }
        }
    }
    
    
    
    
    
    
    /**
     * Class parent of all bean storing expression and differential expression calls, 
     * holding parameters common to all of them.
     * 
     * @author Valentine Rech de Laval
     * @version Bgee 13 Mar. 2015
     * @since Bgee 13
     */
    public static abstract class DownloadFileBean {

        /**
         * @see #getGeneIds()
         */
        private List<String> geneIds;
        /**
         * @see #getGeneNames()
         */
        private List<String> geneNames;
        /**
         * @see #getEntityIds()
         */
        private List<String> entityIds;
        /**
         * @see #getEntityNames()
         */
        private List<String> entityNames;
        /**
         * @see #getStageIds()
         */
        private List<String> stageIds;
        /**
         * @see #getStageNames()
         */
        private List<String> stageNames;
        /**
         * @see #getSpeciesId()
         */
        private String speciesId;
        /**
         * @see #getSpeciesName()
         */
        private String speciesName;
        /**
         * @see getAffymetrixData()
         */
        private String affymetrixData;
        /**
         * @see getAffymetrixQuality()
         */
        private String affymetrixQuality;
        /**
         * @see getRNASeqData()
         */
        private String rnaSeqData;
        /**
         * @see getRNASeqQuality()
         */
        private String rnaSeqQuality;
        
        /**
         * 0-argument constructor of the bean.
         */
        private DownloadFileBean() {
        }

        /**
         * Constructor providing all arguments of the class.
         *
         * @param geneIds           See {@link #getGeneIds()}.
         * @param geneNames         See {@link #getGeneNames()}.
         * @param entityIds         See {@link #getEntityIds()}.
         * @param entityNames       See {@link #getEntityNames()}.
         * @param stageId           See {@link #getStageId()}.
         * @param stageName         See {@link #getStageName()}.
         * @param speciesId         See {@link #getSpeciesId()}.
         * @param speciesName       See {@link #getSpeciesName()}.
         * @param affymetrixData    See {@link #getAffymetrixData()}.
         * @param affymetrixQuality See {@link #getAffymetrixQuality()}.
         * @param rnaSeqData        See {@link #getRNASeqData()}.
         * @param rnaSeqQuality     See {@link #getRNASeqQuality()}.
         */
        private DownloadFileBean(List<String> geneIds, List<String> geneNames, 
                List<String> entityIds, List<String> entityNames, List<String> stageIds, 
                List<String> stageNames, String speciesId, String speciesName, String affymetrixData, 
                String affymetrixQuality, String rnaSeqData, String rnaSeqQuality) {
            this.geneIds = geneIds;
            this.geneNames = geneNames;
            this.entityIds = entityIds;
            this.entityNames = entityNames;
            this.stageIds = stageIds;
            this.stageNames = stageNames;
            this.speciesId = speciesId;
            this.speciesName = speciesName;
            this.affymetrixData = affymetrixData;
            this.affymetrixQuality = affymetrixQuality;
            this.rnaSeqData = rnaSeqData;
            this.rnaSeqQuality = rnaSeqQuality;
        }
                
        /** 
         * @return  the {@code List} of {@code String}s that are the IDs of the genes.
         *          There is more than one gene only in multi-species simple files.
         *          When several are targeted, they are provided in alphabetical order.
         */
        public List<String> getGeneIds() {
            return geneIds;
        }

        /** 
         * @param geneIds   A {@code List} of {@code String}s that are the IDs of the genes. 
         * @see #getGeneIds()
         */
        public void setGeneIds(List<String> geneIds) {
            this.geneIds = geneIds;
        }
        
        /**
         * @return  the {@code List} of {@code String}s that are the names of the genes. 
         *          There is more than one gene only in multi-species simple files. 
         *          When there is several, they are returned in the same order as their 
         *          corresponding ID, as returned by {@link #getGeneIds()}.
         */
        public List<String> getGeneNames() {
            return geneNames;
        }

        /**
         * @param geneName  A {@code List} of {@code String}s that are the names of the genes.
         * @see #getGeneNames()
         */
        public void setGeneNames(List<String> geneNames) {
            this.geneNames = geneNames;
        }
        
        /**
         * @return  the {@code List} of {@code String}s that are the IDs of the anatomical entities.
         *          There is more than one entity only in multi-species files.
         *          When several are targeted, they are provided in alphabetical order.
         */
        public List<String> getEntityIds() {
            return entityIds;
        }
        
        /** 
         * @param entityIds A {@code List} of {@code String}s that are the IDs of the 
         *                  anatomical entities.
         * @see #getEntityIds()
         */
        public void setEntityIds(List<String> entityIds) {
            this.entityIds = entityIds;
        }

        /**
         * @return  the {@code List} of {@code String}s that are the names of the anatomical
         *          entities. There is more than one entity only in multi-species files.
         *          When there is several, they are returned in the same order as their 
         *          corresponding ID, as returned by {@link #getGeneIds()}.
         */
        public List<String> getEntityNames() {
            return entityNames;
        }
        
        /**
         * @param entityNames   A {@code List} of {@code String}s that are the names of the
         *                      anatomical entities.
         * @see #getEntityNames()
         */
        public void setEntityNames(List<String> entityNames) {
            this.entityNames = entityNames;
        }
        
        /** 
         * @return  the {@code List} of {@code String}s that are the IDs of stages.
         */
        public List<String> getStageIds() {
            return stageIds;
        }
        
        /**
         * @param stageIds   A {@code List} of {@code String}s that are the IDs of stages.
         * @see #getStageIds()
         */
        public void setStageIds(List<String> stageIds) {
            this.stageIds = stageIds;
        }
        
        /** 
         * @return  the {@code List} of {@code String}s that are the names of stages.
         */
        public List<String> getStageNames() {
            return stageNames;
        }

        /**
         * @param stageNames A {@code List} of {@code String}s that are the names of stages.
         * @see #getStageNames()
         */
        public void setStageNames(List<String> stageNames) {
            this.stageNames = stageNames;
        }

        /** 
         * @return  the {@code String} that is the ID of the species.
         */
        public String getSpeciesId() {
            return speciesId;
        }
        
        /**
         * @param speciesId   A {@code String} that is the ID of the species.
         * @see #getSpeciesId()
         */
        public void setSpeciesId(String speciesId) {
            this.speciesId = speciesId;
        }

        /** 
         * @return  the {@code String} that is the name of the species.
         */
        public String getSpeciesName() {
            return speciesName;
        }
        
        /**
         * @param speciesName   A {@code String} that is the name of the species.
         * @see #getSpeciesName()
         */
        public void setSpeciesName(String speciesName) {
            this.speciesName = speciesName;
        }

        /**
         * @return  the {@code String} defining the contribution of Affymetrix data 
         *          to the generation of this call.
         */
        public String getAffymetrixData() {
            return affymetrixData;
        }
        
        /**
         * @param affymetrixData    A {@code String} defining the contribution 
         *                          of Affymetrix data to the generation of this call.
         * @see #getAffymetrixData()
         */
        public void setAffymetrixData(String affymetrixData) {
            this.affymetrixData = affymetrixData;
        }
        
        /**
         * @return  the {@code String} defining the call quality found with Affymetrix experiment.
         */
        public String getAffymetrixQuality() {
            return affymetrixQuality;
        }
        
        /** 
         * @param affymetrixQuality A {@code String} defining the call quality found with 
         *                          Affymetrix experiment.
         * @see #getAffymetrixQuality()
         */
        public void setAffymetrixQuality(String affymetrixQuality) {
            this.affymetrixQuality = affymetrixQuality;
        }
        
        /**
         * @return  the {@code String} defining the contribution of RNA-Seq data 
         *          to the generation of this call.
         */
        public String getRNASeqData() {
            return rnaSeqData;
        }
        
        /**
         * @param rnaSeqData    A {@code String} defining the contribution 
         *                      of RNA-Seq data to the generation of this call.
         * @see #getRNASeqData()
         */
        public void setRNASeqData(String rnaSeqData) {
            this.rnaSeqData = rnaSeqData;
        }
        
        /**
         * @return  the {@code String} defining the call quality found with RNA-Seq experiment.
         */
        public String getRNASeqQuality() {
            return rnaSeqQuality;
        }
        
        /** 
         * @param rnaSeqQuality A {@code String} defining the call quality found with 
         *                      RNA-Seq experiment.
         * @see #getRNASeqQuality()
         */
        public void setRNASeqQuality(String rnaSeqQuality) {
            this.rnaSeqQuality = rnaSeqQuality;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((affymetrixData == null) ? 0 : affymetrixData.hashCode());
            result = prime * result + ((affymetrixQuality == null) ? 0 : affymetrixQuality.hashCode());
            result = prime * result + ((entityIds == null) ? 0 : entityIds.hashCode());
            result = prime * result + ((entityNames == null) ? 0 : entityNames.hashCode());
            result = prime * result + ((geneIds == null) ? 0 : geneIds.hashCode());
            result = prime * result + ((geneNames == null) ? 0 : geneNames.hashCode());
            result = prime * result + ((rnaSeqData == null) ? 0 : rnaSeqData.hashCode());
            result = prime * result + ((rnaSeqQuality == null) ? 0 : rnaSeqQuality.hashCode());
            result = prime * result + ((speciesId == null) ? 0 : speciesId.hashCode());
            result = prime * result + ((speciesName == null) ? 0 : speciesName.hashCode());
            result = prime * result + ((stageIds == null) ? 0 : stageIds.hashCode());
            result = prime * result + ((stageNames == null) ? 0 : stageNames.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            DownloadFileBean other = (DownloadFileBean) obj;
            if (affymetrixData == null) {
                if (other.affymetrixData != null)
                    return false;
            } else if (!affymetrixData.equals(other.affymetrixData))
                return false;
            if (affymetrixQuality == null) {
                if (other.affymetrixQuality != null)
                    return false;
            } else if (!affymetrixQuality.equals(other.affymetrixQuality))
                return false;
            if (entityIds == null) {
                if (other.entityIds != null)
                    return false;
            } else if (!entityIds.equals(other.entityIds))
                return false;
            if (entityNames == null) {
                if (other.entityNames != null)
                    return false;
            } else if (!entityNames.equals(other.entityNames))
                return false;
            if (geneIds == null) {
                if (other.geneIds != null)
                    return false;
            } else if (!geneIds.equals(other.geneIds))
                return false;
            if (geneNames == null) {
                if (other.geneNames != null)
                    return false;
            } else if (!geneNames.equals(other.geneNames))
                return false;
            if (rnaSeqData == null) {
                if (other.rnaSeqData != null)
                    return false;
            } else if (!rnaSeqData.equals(other.rnaSeqData))
                return false;
            if (rnaSeqQuality == null) {
                if (other.rnaSeqQuality != null)
                    return false;
            } else if (!rnaSeqQuality.equals(other.rnaSeqQuality))
                return false;
            if (speciesId == null) {
                if (other.speciesId != null)
                    return false;
            } else if (!speciesId.equals(other.speciesId))
                return false;
            if (speciesName == null) {
                if (other.speciesName != null)
                    return false;
            } else if (!speciesName.equals(other.speciesName))
                return false;
            if (stageIds == null) {
                if (other.stageIds != null)
                    return false;
            } else if (!stageIds.equals(other.stageIds))
                return false;
            if (stageNames == null) {
                if (other.stageNames != null)
                    return false;
            } else if (!stageNames.equals(other.stageNames))
                return false;
            return true;
        }

        @Override
        public String toString() {
            return " Gene IDs: " + getGeneIds() + " - Gene names: " + getGeneNames() +
                    " - Entity IDs: " + getEntityIds() + " - Entity names: " + getEntityNames() + 
                    " - Stage IDs: " + getStageIds() + " - Stage names: " + getStageNames() + 
                    " - Species ID: " + getSpeciesId() + " - Species name: " + getSpeciesName() + 
                    " - Affymetrix data: " + getAffymetrixData() + 
                    " - Affymetrix quality: " + getAffymetrixQuality() +
                    " - RNA-Seq data: " + getRNASeqData() + 
                    " - RNA-Seq quality: " + getRNASeqQuality();
        }
    }
    
    /**
     * A bean representing a row of an expression file. Getter and setter names 
     * must follow standard bean definitions.
     * 
     * @author Valentine Rech de Laval
     * @version Bgee 13 Mar. 2015
     * @since Bgee 13
     */
    public static class ExprFileBean extends DownloadFileBean {
        /**
         * @see getESTData()
         */
        private String estData;
        /**
         * @see getESTQuality()
         */
        private String estQuality;
        /**
         * @see getInSituData()
         */
        private String inSituData;
        /**
         * @see getInSituQuality()
         */
        private String inSituQuality;
        /**
         * @see getRelaxedInSituData()
         */
        private String relaxedInSituData;
        /**
         * @see getRelaxedInSituQuality()
         */
        private String relaxedInSituQuality;
        /**
         * @see getExpression()
         */
        private String expression;
        /**
         * @see getCallQuality()
         */
        private String callQuality;
        /**
         * @see isIncludeObservedData()
         */
        private boolean includeObservedData;
        
        /**
         * 0-argument constructor of the bean.
         */
        public ExprFileBean() {
        }

        /**
         * Constructor providing all arguments of the class.
         *
         * @param geneIds               See {@link #getGeneIds()}.
         * @param geneNames             See {@link #getGeneNames()}.
         * @param entityIds             See {@link #getEntityIds()}.
         * @param entityNames           See {@link #getEntityNames()}.
         * @param stageId               See {@link #getStageId()}.
         * @param stageName             See {@link #getStageName()}.
         * @param speciesId             See {@link #getSpeciesId()}.
         * @param speciesName           See {@link #getSpeciesName()}.
         * @param affymetrixData        See {@link #getAffymetrixData()}.
         * @param affymetrixQuality     See {@link #getAffymetrixQuality()}.
         * @param rnaSeqData            See {@link #getRNASeqData()}.
         * @param rnaSeqQuality         See {@link #getRNASeqQuality()}.
         * @param estData               See {@link #getESTData()}.
         * @param estQuality            See {@link #getESTQuality()}.
         * @param inSituData            See {@link #getInSituData()}.
         * @param inSituQuality         See {@link #getInSituQuality()}.
         * @param relaxedInSituData     See {@link #getRelaxedInSituData()}.
         * @param relaxedInSituQuality  See {@link #getRelaxedInSituQuality()}. 
         * @param expression            See {@link #getExpression()}.
         * @param callQuality           See {@link #getCallQuality()}.
         * @param includeObservedData   See {@link #isIncludeObservedData()}.
         */
        public ExprFileBean(List<String> geneIds, List<String> geneNames, 
                List<String> entityIds, List<String> entityNames, 
                List<String> stageIds, List<String> stageNames, 
                String speciesId, String speciesName, 
                String affymetrixData, String affymetrixQuality,
                String rnaSeqData, String rnaSeqQuality, String estData, String estQuality, 
                String inSituData, String inSituQuality, String relaxedInSituData,
                String relaxedInSituQuality, String expression, String callQuality,
                boolean includeObservedData) {
            super(geneIds, geneNames, entityIds, entityNames, 
                    stageIds, stageNames, speciesId, speciesName, 
                    affymetrixData, affymetrixQuality, rnaSeqData, rnaSeqQuality);
            this.estData = estData;
            this.estQuality = estQuality;
            this.inSituData = inSituData;
            this.inSituQuality = inSituQuality;
            this.relaxedInSituData = relaxedInSituData;
            this.relaxedInSituQuality = relaxedInSituQuality;
            this.expression = expression;
            this.callQuality = callQuality;
            this.includeObservedData = includeObservedData;
        }
        
        /**
         * @return  the {@code String} defining the contribution of EST data 
         *          to the generation of this call.
         */
        public String getESTData() {
            return estData;
        }
        /**
         * @param estData   A {@code String} defining the contribution of EST  
         *                  data to the generation of this call.
         * @see #getESTData()
         */
        public void setESTData(String estData) {
            this.estData = estData;
        }
        /**
         * @return  the {@code String} defining the call quality found with EST experiment.
         */
        public String getESTQuality() {
            return estQuality;
        }
        /**
         * @param estQuality    A {@code String} defining the call quality found with EST experiment.
         * @see #getESTQuality()
         */
        public void setESTQuality(String estQuality) {
            this.estQuality = estQuality;
        }
        /**
         * @return  the {@code String} defining the contribution of <em>in situ</em> data 
         *          to the generation of this call.
         */
        public String getInSituData() {
            return inSituData;
        }
        /**
         * @param inSituData    A {@code String} defining the contribution of <em>in situ</em>  
         *                      data to the generation of this call.
         * @see #getInSituData()
         */
        public void setInSituData(String inSituData) {
            this.inSituData = inSituData;
        }
        /**
         * @return  the {@code String} defining the call quality found with <em>in situ</em>
         *          experiment.
         */
        public String getInSituQuality() {
            return inSituQuality;
        }
        /**
         * @param inSituQuality A {@code String} defining the call quality found with 
         *                      <em>in situ</em> experiment.
         * @see #getInSituQuality()
         */
        public void setInSituQuality(String inSituQuality) {
            this.inSituQuality = inSituQuality;
        }
        /**
         * @return  the {@code String} defining the contribution of relaxed <em>in situ</em> data 
         *          to the generation of this call.
         */
        public String getRelaxedInSituData() {
            return relaxedInSituData;
        }
        /**
         * @param relaxedInSituData A {@code String} defining the contribution of relaxed 
         *                          <em>in situ</em> data to the generation of this call.
         * @see #getRelaxedInSituData()
         */
        public void setRelaxedInSituData(String relaxedInSituData) {
            this.relaxedInSituData = relaxedInSituData;
        }
        /**
         * @return  the {@code String} defining the call quality found with relaxed
         *          <em>in situ</em> experiment.
         */
        public String getRelaxedInSituQuality() {
            return relaxedInSituQuality;
        }
        /**
         * @param relaxedInSituQuality  A {@code String} defining the call quality found with 
         *                              relaxed <em>in situ</em> experiment.
         * @see #getRelaxedInSituQuality()
         */
        public void setRelaxedInSituQuality(String relaxedInSituQuality) {
            this.relaxedInSituQuality = relaxedInSituQuality;
        }
        /**
         * @return  the {@code String} defining merged expression/no-expression from different 
         *          data types, in the download file.
         */
        public String getExpression() {
            return expression;
        }
        /**
         * @param expression    A {@code String} defining merged expression/no-expression from 
         *                      different data types.
         * @see #getExpression()
         */
        public void setExpression(String expression) {
            this.expression = expression;
        }
        /**
         * @return  the {@code String} defining the merged quality of the call.
         */
        public String getCallQuality() {
            return callQuality;
        }
        /**
         * @param callQuality   A {@code String} defining the merged quality of the call.
         * @see #getCallQuality()
         */
        public void setCallQuality(String callQuality) {
            this.callQuality = callQuality;
        }
        /**
         * @return  A {@code boolean} defining whether the call include observed data or not. 
         */
        public boolean isIncludeObservedData() {
            return includeObservedData;
        }
        /**
         * @param includeObservedData   A {@code boolean} defining whether the call include 
         *                              observed data or not.
         */
        public void setIncludeObservedData(boolean includeObservedData) {
            this.includeObservedData = includeObservedData;
        }
        
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = super.hashCode();
            result = prime * result + ((callQuality == null) ? 0 : callQuality.hashCode());
            result = prime * result + ((estData == null) ? 0 : estData.hashCode());
            result = prime * result + ((estQuality == null) ? 0 : estQuality.hashCode());
            result = prime * result + ((expression == null) ? 0 : expression.hashCode());
            result = prime * result + ((inSituData == null) ? 0 : inSituData.hashCode());
            result = prime * result + ((inSituQuality == null) ? 0 : inSituQuality.hashCode());
            result = prime * result + (includeObservedData ? 1231 : 1237);
            result = prime * result + ((relaxedInSituData == null) ? 0 : relaxedInSituData.hashCode());
            result = prime * result + 
                    ((relaxedInSituQuality == null) ? 0 : relaxedInSituQuality.hashCode());
            return result;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (!super.equals(obj))
                return false;
            if (getClass() != obj.getClass())
                return false;
            ExprFileBean other = (ExprFileBean) obj;
            if (callQuality == null) {
                if (other.callQuality != null)
                    return false;
            } else if (!callQuality.equals(other.callQuality))
                return false;
            if (estData == null) {
                if (other.estData != null)
                    return false;
            } else if (!estData.equals(other.estData))
                return false;
            if (estQuality == null) {
                if (other.estQuality != null)
                    return false;
            } else if (!estQuality.equals(other.estQuality))
                return false;
            if (expression == null) {
                if (other.expression != null)
                    return false;
            } else if (!expression.equals(other.expression))
                return false;
            if (inSituData == null) {
                if (other.inSituData != null)
                    return false;
            } else if (!inSituData.equals(other.inSituData))
                return false;
            if (inSituQuality == null) {
                if (other.inSituQuality != null)
                    return false;
            } else if (!inSituQuality.equals(other.inSituQuality))
                return false;
            if (includeObservedData != other.includeObservedData)
                return false;
            if (relaxedInSituData == null) {
                if (other.relaxedInSituData != null)
                    return false;
            } else if (!relaxedInSituData.equals(other.relaxedInSituData))
                return false;
            if (relaxedInSituQuality == null) {
                if (other.relaxedInSituQuality != null)
                    return false;
            } else if (!relaxedInSituQuality.equals(other.relaxedInSituQuality))
                return false;
            return true;
        }
        
        @Override
        public String toString() {
            return super.toString() +  " - EST data: " + getESTData() + 
                    " - EST quality: " + getESTQuality() + " - In situ data: " + getInSituData() + 
                    " - In situ quality: " + getInSituQuality() + 
                    " - Relaxed in situ data: " + getRelaxedInSituData() + 
                    " - Relaxed in situ quality: " + getRelaxedInSituQuality() + 
                    " - Expression: " + getExpression() + " - Call quality: " + getCallQuality() + 
                    " - Include observed data: " + isIncludeObservedData();
        }
    }
    
    //TODO: why aren't all these classes specific to diff expression data 
    //into GenerateMultiSpeciesDiffExprFile?
    
    /**
     * A bean representing a row of a differential expression file. Getter and setter names 
     * must follow standard bean definitions.
     * 
     * @author Valentine Rech de Laval
     * @version Bgee 13 Mar. 2015
     * @since Bgee 13
     */
    public static class DiffExprFileBean extends DownloadFileBean {
        
        /**
         * See {@link #getAffymetrixPValue()}.
         */
        private Float affymetrixPValue;
        /**
         * See {@link #getAffymetrixConsistentDEA()}.
         */
        private Double affymetrixConsistentDEA;
        /**
         * See {@link #getAffymetrixInconsistentDEA()}.
         */
        private Double affymetrixInconsistentDEA;
        /**
         * See {@link #getRnaSeqPValue()}.
         */
        private Float rnaSeqPValue;
        /**
         * See {@link #getRnaSeqConsistentDEA()}.
         */
        private Double rnaSeqConsistentDEA;        
        /**
         * See {@link #getRnaSeqInconsistentDEA()}.
         */
        private Double rnaSeqInconsistentDEA;
        /**
         * See {@link #getDifferentialExpression()}.
         */
        private String differentialExpression;
        /**
         * See {@link #getCallQuality()}.
         */
        private String callQuality;
        
        /**
         * 0-argument constructor of the bean.
         */
        public DiffExprFileBean() {
        }
        
        /**
         * Constructor providing all arguments of the class.
         *
         * @param geneIds                   See {@link #getGeneIds()}.
         * @param geneNames                 See {@link #getGeneNames()}.
         * @param entityIds                 See {@link #getEntityIds()}.
         * @param entityNames               See {@link #getEntityNames()}.
         * @param stageId                   See {@link #getStageId()}.
         * @param stageName                 See {@link #getStageName()}.
         * @param speciesId                 See {@link #getSpeciesId()}.
         * @param speciesName               See {@link #getSpeciesName()}.
         * @param affymetrixData            See {@link #getAffymetrixData()}.
         * @param affymetrixQuality         See {@link #getAffymetrixQuality()}.
         * @param affymetrixPValue          See {@link #getAffymetrixPValue()}.
         * @param affymetrixConsistentDEA   See {@link #getAffymetrixConsistentDEA()}.
         * @param affymetrixInconsistentDEA See {@link #getAffymetrixInconsistentDEA()}.
         * @param rnaSeqData                See {@link #getRNASeqData()}.
         * @param rnaSeqQuality             See {@link #getRNASeqQuality()}.
         * @param rnaSeqPValue              See {@link #getAffymetrixPValue()}.
         * @param rnaSeqConsistentDEA       See {@link #getRnaSeqConsistentDEA()}.
         * @param rnaSeqInconsistentDEA     See {@link #getRnaSeqInconsistentDEA()}.
         * @param differentialExpression    See {@link #getDifferentialExpression()}.
         * @param callQuality               See {@link #getCallQuality()}.
         */
        public DiffExprFileBean(List<String> geneIds,
                List<String> geneNames, List<String> entityIds, List<String> entityNames,
                List<String> stageIds, List<String> stageNames, String speciesId, String speciesName, 
                String affymetrixData, String affymetrixQuality, Float affymetrixPValue, 
                Double affymetrixConsistentDEA, Double affymetrixInconsistentDEA,
                String rnaSeqData, String rnaSeqQuality, Float rnaSeqPValue, 
                Double rnaSeqConsistentDEA, Double rnaSeqInconsistentDEA,
                String differentialExpression, String callQuality) {
            super(geneIds, geneNames, entityIds, entityNames, 
                    stageIds, stageNames, speciesId, speciesName, 
                    affymetrixData, affymetrixQuality, rnaSeqData, rnaSeqQuality);
            this.affymetrixPValue = affymetrixPValue;
            this.affymetrixConsistentDEA = affymetrixConsistentDEA;
            this.affymetrixInconsistentDEA = affymetrixInconsistentDEA;
            this.rnaSeqPValue = rnaSeqPValue;
            this.rnaSeqConsistentDEA = rnaSeqConsistentDEA;
            this.rnaSeqInconsistentDEA = rnaSeqInconsistentDEA;
            this.differentialExpression = differentialExpression;
            this.callQuality = callQuality;
        }

        /**
         * @return  the {@code Float} that is the best p-value using Affymetrix.
         */
        public Float getAffymetrixPValue() {
            return affymetrixPValue;
        }
        /**
         * @param affymetrixPValue  A {@code Float} that is the best p-value using Affymetrix.
         */
        public void setAffymetrixPValue(Float affymetrixPValue) {
            this.affymetrixPValue = affymetrixPValue;
        }
        
        /**
         * @return  the {@code Double} that is the number of analysis using 
         *          Affymetrix data where the same call is found.
         */
        public Double getAffymetrixConsistentDEA() {
            return affymetrixConsistentDEA;
        }
        /**
         * @param affymetrixConsistentDEA   A {@code Double} that is the number of analysis using 
         *                                  Affymetrix data where the same call is found.
         */
        public void setAffymetrixConsistentDEA(Double affymetrixConsistentDEA) {
            this.affymetrixConsistentDEA = affymetrixConsistentDEA;
        }
        
        /**
         * @return  the {@code Double} that is the number of analysis using 
         *          Affymetrix data where a different call is found.
         */
        public Double getAffymetrixInconsistentDEA() {
            return affymetrixInconsistentDEA;
        }
        /**
         * @param affymetrixInconsistentDEA A {@code Double} that is the number of analysis using 
         *                                  Affymetrix data where a different call is found.
         */
        public void setAffymetrixInconsistentDEA(Double affymetrixInconsistentDEA) {
            this.affymetrixInconsistentDEA = affymetrixInconsistentDEA;
        }

        /**
         * @return  the {@code Float} that is the best p-value using RNA-Seq.
         */
        public Float getRnaSeqPValue() {
            return rnaSeqPValue;
        }
        /**
         * @param rnaSeqPValue  A {@code Float} that is the best p-value using RNA-Seq.
         */
        public void setRnaSeqPValue(Float rnaSeqPValue) {
            this.rnaSeqPValue = rnaSeqPValue;
        }

        /**
         * @return  the {@code Double} that is the number of analysis using 
         *          RNA-Seq data where the same call is found.
         */
        public Double getRnaSeqConsistentDEA() {
            return rnaSeqConsistentDEA;
        }
        /**
         * @param rnaSeqConsistentDEA   A {@code Double} that is the number of analysis using 
         *                              RNA-Seq data where the same call is found.
         */
        public void setRnaSeqConsistentDEA(Double rnaSeqConsistentDEA) {
            this.rnaSeqConsistentDEA = rnaSeqConsistentDEA;
        }

        /**
         * @return  the {@code Double} that is the number of analysis using 
         *          RNA-Seq data where a different call is found.
         */
        public Double getRnaSeqInconsistentDEA() {
            return rnaSeqInconsistentDEA;
        }
        /**
         * @param rnaSeqInconsistentDEA A {@code Double} that is the number of analysis using 
         *                              RNA-Seq data where a different call is found.
         */
        public void setRnaSeqInconsistentDEA(Double rnaSeqInconsistentDEA) {
            this.rnaSeqInconsistentDEA = rnaSeqInconsistentDEA;
        }

        /**
         * @return  the {@code String} that is merged differential expressions 
         *          from different data types.
         */
        public String getDifferentialExpression() {
            return differentialExpression;
        }
        /**
         * @param differentialExpression    A {@code String} that is merged differential expressions 
         *                                  from different data types.
         */
        public void setDifferentialExpression(String differentialExpression) {
            this.differentialExpression = differentialExpression;
        }

        /** 
         * @return  the {@code String} that is call quality.
         */
        public String getCallQuality() {
            return callQuality;
        }
        /**
         * @param callQuality   A {@code String} that is call quality.
         */
        public void setCallQuality(String callQuality) {
            this.callQuality = callQuality;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = super.hashCode();
            result = prime * result
                    + ((affymetrixConsistentDEA == null) ? 0 : affymetrixConsistentDEA.hashCode());
            result = prime * result
                    + ((affymetrixInconsistentDEA == null) ? 0 : affymetrixInconsistentDEA.hashCode());
            result = prime * result + ((affymetrixPValue == null) ? 0 : affymetrixPValue.hashCode());
            result = prime * result + ((callQuality == null) ? 0 : callQuality.hashCode());
            result = prime * result +
                    ((differentialExpression == null) ? 0 : differentialExpression.hashCode());
            result = prime * result + 
                    ((rnaSeqConsistentDEA == null) ? 0 : rnaSeqConsistentDEA.hashCode());
            result = prime * result + 
                    ((rnaSeqInconsistentDEA == null) ? 0 : rnaSeqInconsistentDEA.hashCode());
            result = prime * result + ((rnaSeqPValue == null) ? 0 : rnaSeqPValue.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (!super.equals(obj))
                return false;
            if (getClass() != obj.getClass())
                return false;
            DiffExprFileBean other = (DiffExprFileBean) obj;
            if (affymetrixConsistentDEA == null) {
                if (other.affymetrixConsistentDEA != null)
                    return false;
            } else if (!affymetrixConsistentDEA.equals(other.affymetrixConsistentDEA))
                return false;
            if (affymetrixInconsistentDEA == null) {
                if (other.affymetrixInconsistentDEA != null)
                    return false;
            } else if (!affymetrixInconsistentDEA.equals(other.affymetrixInconsistentDEA))
                return false;
            if (affymetrixPValue == null) {
                if (other.affymetrixPValue != null)
                    return false;
            } else if (!affymetrixPValue.equals(other.affymetrixPValue))
                return false;
            if (callQuality == null) {
                if (other.callQuality != null)
                    return false;
            } else if (!callQuality.equals(other.callQuality))
                return false;
            if (differentialExpression == null) {
                if (other.differentialExpression != null)
                    return false;
            } else if (!differentialExpression.equals(other.differentialExpression))
                return false;
            if (rnaSeqConsistentDEA == null) {
                if (other.rnaSeqConsistentDEA != null)
                    return false;
            } else if (!rnaSeqConsistentDEA.equals(other.rnaSeqConsistentDEA))
                return false;
            if (rnaSeqInconsistentDEA == null) {
                if (other.rnaSeqInconsistentDEA != null)
                    return false;
            } else if (!rnaSeqInconsistentDEA.equals(other.rnaSeqInconsistentDEA))
                return false;
            if (rnaSeqPValue == null) {
                if (other.rnaSeqPValue != null)
                    return false;
            } else if (!rnaSeqPValue.equals(other.rnaSeqPValue))
                return false;
            return true;
        }

        @Override
        public String toString() {
            return super.toString() + " - Affymetrix p-value: " + getAffymetrixPValue() +
                    " - Affymetrix consistent DEA: " + getAffymetrixConsistentDEA() +
                    " - Affymetrix inconsistent DEA: " + getAffymetrixInconsistentDEA() + 
                    " - RNA-Seq p-value: " + getRnaSeqPValue() + 
                    " - RNA-Seq consistent DEA: " + getRnaSeqConsistentDEA() + 
                    " - RNA-Seq inconsistent DEA: " + getRnaSeqInconsistentDEA() + 
                    " - Differential expression: " + getDifferentialExpression() + 
                    " - Call quality: " + getCallQuality();
        }
    }
    
    /**
     * A bean representing a row of a simple multi-species file. Getter and setter names 
     * must follow standard bean definitions.
     * 
     * @author Valentine Rech de Laval
     * @version Bgee 13 Mar. 2015
     * @since Bgee 13
     */
    public static class SimpleMultiSpeciesExprFileBean extends DownloadFileBean {
        
        /**
         * @see #getOmaId()
         */
        private String omaId;
        /**
         * @see #getOmaDescription()
         */
        private String omaDescription;
        /**
         * @see getCioId()
         */
        private String cioId;
        /**
         * @see getCioName()
         */
        private String cioName;
        /**
         * See {@link #getNbExprGenes()}.
         */
        private String nbExprGenes;
        /**
         * See {@link #getNbNoExprGenes()}.
         */
        private String nbNoExprGenes;
        /**
         * See {@link #getNbNAGenes()}.
         */
        private String nbNAGenes;
        
        /**
         * 0-argument constructor of the bean.
         */
        public SimpleMultiSpeciesExprFileBean() {
        }

        /**
         * Constructor providing all arguments of the class.
         *
         * @param omaId             See {@link #getOmaId()}.
         * @param omaDescription    See {@link #getOmaDescription()}.
         * @param geneIds           See {@link #getGeneIds()}.
         * @param geneNames         See {@link #getGeneNames()}.
         * @param entityIds         See {@link #getEntityIds()}.
         * @param entityNames       See {@link #getEntityNames()}.
         * @param stageId           See {@link #getStageId()}.
         * @param stageName         See {@link #getStageName()}.
         * @param speciesId         See {@link #getSpeciesId()}.
         * @param speciesName       See {@link #getSpeciesName()}.
         * @param cioId             See {@link #getCioId()}.
         * @param cioName           See {@link #getCioName()}.
         * @param nbExprGenes       See {@link #getNbExprGenes()}.
         * @param nbNoExprGenes     See {@link #getNbNoExprGenes()}.
         * @param nbNAGenes         See {@link #getNbNAGenes()}.
         */
        public SimpleMultiSpeciesExprFileBean(String omaId, String omaDescription, List<String> geneIds,
                List<String> geneNames, List<String> entityIds, List<String> entityNames,
                List<String> stageIds, List<String> stageNames, String speciesId, String speciesName, 
                String cioId, String cioName, String nbExprGenes, String nbNoExprGenes, String nbNAGenes) {
            super(geneIds, geneNames, entityIds, entityNames, stageIds, stageNames, 
                    speciesId, speciesName, null, null, null, null);
            this.omaId = omaId;
            this.omaDescription = omaDescription;
            this.nbExprGenes = nbExprGenes;
            this.nbNoExprGenes = nbNoExprGenes;
            this.nbNAGenes = nbNAGenes;
        }
        
        /** 
         * @return  the {@code String} that is the ID of ancestral OMA node of the gene.
         */
        public String getOmaId() {
            return omaId;
        }
        /**
         * @param omaId A {@code String} that is the ID of ancestral OMA node of the gene.
         * @see #getOmaId()
         */
        public void setOmaId(String omaId) {
            this.omaId = omaId;
        }
        
        /** 
         * @return  the {@code String} that is the description of ancestral OMA node.
         */
        public String getOmaDescription() {
            return omaDescription;
        }
        /** 
         * @param omaDescription A {@code String} that is the description of ancestral OMA node.
         * @see #getOmaDescription()
         */
        public void setOmaDescription(String omaDescription) {
            this.omaDescription = omaDescription;
        }

        /**
         * @return  the {@code String} that is the ID of the CIO statement.
         */
        public String getCioId() {
            return cioId;
        }
        /**
         * @param cioId A {@code String} that is the ID of the CIO statement.
         * @see #getCioId()
         */
        public void setCioId(String cioId) {
            this.cioId = cioId;
        }
        
        /** 
         * @return  the {@code String} that is the name of the CIO statement.
         */
        public String getCioName() {
            return cioName;
        }
        /**
         * @param cioName   A {@code String} that is the name of the CIO statement.
         * @see #getCioName()
         */
        public void setCioName(String cioName) {
            this.cioName = cioName;
        }


        /**
         * @return  the {@code String} that is the number of expressed genes in the family.
         */
        public String getNbExprGenes() {
            return nbExprGenes;
        }
        /**
         * @param nbExprGenes   A {@code String} that is the number of expressed genes in the family.
         */
        public void setNbExprGenes(String nbExprGenes) {
            this.nbExprGenes = nbExprGenes;
        }
        
        /**
         * @return  the {@code String} that is the number of no-expressed genes in the family.
         */
        public String getNbNoExprGenes() {
            return nbNoExprGenes;
        }
        /**
         * @param nbNoExprGenes A {@code String} that is the number of no-expressed genes 
         *                      in the family. 
         */
        public void setNbNoExprGenes(String nbNoExprGenes) {
            this.nbNoExprGenes = nbNoExprGenes;
        }
        
        /**
         * @return  the {@code String} that is the number of genes without data in the family.
         */
        public String getNbNAGenes() {
            return nbNAGenes;
        }
        /**
         * @param nbNAGenes A {@code String} that is the number of genes without data in the family.
         */
        public void setNbNAGenes(String nbNAGenes) {
            this.nbNAGenes = nbNAGenes;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = super.hashCode();
            result = prime * result + ((nbExprGenes == null) ? 0 : nbExprGenes.hashCode());
            result = prime * result + ((nbNAGenes == null) ? 0 : nbNAGenes.hashCode());
            result = prime * result + ((nbNoExprGenes == null) ? 0 : nbNoExprGenes.hashCode());
            result = prime * result + ((omaDescription == null) ? 0 : omaDescription.hashCode());
            result = prime * result + ((omaId == null) ? 0 : omaId.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (!super.equals(obj))
                return false;
            if (getClass() != obj.getClass())
                return false;
            SimpleMultiSpeciesExprFileBean other = (SimpleMultiSpeciesExprFileBean) obj;
            if (nbExprGenes == null) {
                if (other.nbExprGenes != null)
                    return false;
            } else if (!nbExprGenes.equals(other.nbExprGenes))
                return false;
            if (nbNAGenes == null) {
                if (other.nbNAGenes != null)
                    return false;
            } else if (!nbNAGenes.equals(other.nbNAGenes))
                return false;
            if (nbNoExprGenes == null) {
                if (other.nbNoExprGenes != null)
                    return false;
            } else if (!nbNoExprGenes.equals(other.nbNoExprGenes))
                return false;
            if (omaDescription == null) {
                if (other.omaDescription != null)
                    return false;
            } else if (!omaDescription.equals(other.omaDescription))
                return false;
            if (omaId == null) {
                if (other.omaId != null)
                    return false;
            } else if (!omaId.equals(other.omaId))
                return false;
            return true;
        }

        @Override
        public String toString() {
            return super.toString() + 
                    " - OMA ID: " + getOmaId() + " - OMA description: " + getOmaDescription() + 
                    " - Number of expressed genes: " + nbExprGenes
                    + " - Number of no-expressed genes: " + nbNoExprGenes + 
                    " - Number of N/A genes: " + nbNAGenes;
        }
    }
    
    /**
     * TODO Javadoc
     *
     * @author 	Valentine Rech de Laval
     * @version Bgee 13
     * @since 	Bgee 13
     */
    //TODO: rename all xxxNbxxx methods into xxxCount (number doesn't have the exact same meaning in English)
    public static class SpeciesCounts {
        
        /**
         * See {@link #getSpeciesId()}.
         */
        private String speciesId;
        /**
         * See {@link #getNbOverExprGenes()}.
         */
        private int nbOverExprGenes;
        /**
         * See {@link #getNbUnderExprGenes()}.
         */
        private int nbUnderExprGenes;
        /**
         * See {@link #getNbNotDiffExprGenes()}.
         */
        private int nbNotDiffExprGenes;
        /**
         * See {@link #getNbNotExprGenes()}.
         */
        private int nbNotExprGenes;
        /**
         * See {@link #getNbNAGenes()}.
         */
        private int nbNAGenes;

        /**
         * Constructor providing all arguments of the class.
         *
         * @param speciesId             See {@link #getSpeciesId()}.
         * @param nbOverExprGenes       See {@link #getNbOverExprGenes()}.
         * @param nbUnderExprGenes      See {@link #getNbUnderExprGenes()}.
         * @param nbNotDiffExprGenes    See {@link #getNbNotDiffExprGenes()}.
         * @param nbNotExprGenes        See {@link #getNbNotExprGenes()}.
         * @param nbNAGenes             See {@link #getNbNAGenes()}.
         */
        public SpeciesCounts(String speciesId, int nbOverExprGenes, int nbUnderExprGenes, 
                int nbNotDiffExprGenes, int nbNotExprGenes, int nbNAGenes) {
            this.speciesId = speciesId;
            this.nbOverExprGenes = nbOverExprGenes;
            this.nbUnderExprGenes = nbUnderExprGenes;
            this.nbNotDiffExprGenes = nbNotDiffExprGenes;
            this.nbNotExprGenes = nbNotExprGenes;
            this.nbNAGenes = nbNAGenes;
        }

        /**
         * @return  the {@code String} that is the ID of the species.
         */
        public String getSpeciesId() {
            return speciesId;
        }
        /**
         * @param speciesId A {@code String} that is the ID of the species.
         */
        public void setSpeciesId(String speciesId) {
            this.speciesId = speciesId;
        }

        /**
         * @return  the {@code int} that is the number of over-expressed genes in the family.
         */
        public int getNbOverExprGenes() {
            return nbOverExprGenes;
        }
        /**
         * @param nbOverExprGenes   An {@code int} that is the number of over-expressed 
         *                          genes in the family.
         */
        public void setNbOverExprGenes(int nbOverExprGenes) {
            this.nbOverExprGenes = nbOverExprGenes;
        }
        
        /**
         * @return  the {@code int} that is the number of under-expressed genes in the family.
         */
        public int getNbUnderExprGenes() {
            return nbUnderExprGenes;
        }
        /**
         * @param nbUnderExprGenes  An {@code int} that is the number of under-expressed 
         *                          genes in the family.
         */
        public void setNbUnderExprGenes(int nbUnderExprGenes) {
            this.nbUnderExprGenes = nbUnderExprGenes;
        }

        /**
         * @return  the {@code int} that is the number of not diff. expressed genes in the family.
         */
        public int getNbNotDiffExprGenes() {
            return nbNotDiffExprGenes;
        }
        /**
         * @param nbNotDiffExprGenes An {@code int} that is the number of not diff. expressed 
         *                           genes in the family.
         */
        public void setNbNotDiffExprGenes(int nbNotDiffExprGenes) {
            this.nbNotDiffExprGenes = nbNotDiffExprGenes;
        }

        /**
         * @return  the {@code int} that is the number of not expressed genes in the family.
         */
        public int getNbNotExprGenes() {
            return nbNotExprGenes;
        }
        /**
         * @param nbNoExprGenes An {@code int} that is the number of not expressed genes 
         *                      in the family. 
         */
        public void setNbNotExprGenes(int nbNoExprGenes) {
            this.nbNotExprGenes = nbNoExprGenes;
        }
        
        /**
         * @return  the {@code int} that is the number of genes without data in the family.
         */
        public int getNbNAGenes() {
            return nbNAGenes;
        }
        /**
         * @param nbNAGenes An {@code int} that is the number of genes without data in the family.
         */
        public void setNbNAGenes(int nbNAGenes) {
            this.nbNAGenes = nbNAGenes;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + nbNAGenes;
            result = prime * result + nbNotDiffExprGenes;
            result = prime * result + nbNotExprGenes;
            result = prime * result + nbOverExprGenes;
            result = prime * result + nbUnderExprGenes;
            result = prime * result + ((speciesId == null) ? 0 : speciesId.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            SpeciesCounts other = (SpeciesCounts) obj;
            if (nbNAGenes != other.nbNAGenes)
                return false;
            if (nbNotDiffExprGenes != other.nbNotDiffExprGenes)
                return false;
            if (nbNotExprGenes != other.nbNotExprGenes)
                return false;
            if (nbOverExprGenes != other.nbOverExprGenes)
                return false;
            if (nbUnderExprGenes != other.nbUnderExprGenes)
                return false;
            if (speciesId == null) {
                if (other.speciesId != null)
                    return false;
            } else if (!speciesId.equals(other.speciesId))
                return false;
            return true;
        }

        @Override
        public String toString() {
            return super.toString() + " - Species ID: " + getSpeciesId() +
                    " - Number of over-expressed genes: " + getNbOverExprGenes() +
                    " - Number of under-expressed genes: " + getNbUnderExprGenes() +
                    " - Number of not diff. expressed genes: " + getNbNotDiffExprGenes() +
                    " - Number of no-expressed genes: " + getNbNotExprGenes() + 
                    " - Number of N/A genes: " + getNbNAGenes();
        }
    }
    
    /**
         * A bean representing a row of a simple differential expression multi-species file. 
         * Getter and setter names must follow standard bean definitions.
         * 
         * @author Valentine Rech de Laval
         * @version Bgee 13 Mar. 2015
         * @since Bgee 13
         */
        public static class SimpleMultiSpeciesDiffExprFileBean extends DownloadFileBean {
    
            /**
             * @see #getOmaId()
             */
            private String omaId;
            /**
             * @see #getOmaDescription()
             */
            private String omaDescription;
            /**
             * @see getCioId()
             */
            private String cioId;
            /**
             * @see getCioName()
             */
            private String cioName;
    
            /**
             * See {@link #getSpeciesCounts()}.
             */
            private List<SpeciesCounts> speciesCounts;
            
            /**
             * 0-argument constructor of the bean.
             */
            public SimpleMultiSpeciesDiffExprFileBean() {
            }
    
            /**
             * Constructor providing all arguments of the class.
             *
             * @param omaId             See {@link #getOmaId()}.
             * @param omaDescription    See {@link #getOmaDescription()}.
             * @param geneIds           See {@link #getGeneIds()}.
             * @param geneNames         See {@link #getGeneNames()}.
             * @param entityIds         See {@link #getEntityIds()}.
             * @param entityNames       See {@link #getEntityNames()}.
             * @param stageId           See {@link #getStageId()}.
             * @param stageName         See {@link #getStageName()}.
             * @param speciesId         See {@link #getSpeciesId()}.
             * @param speciesName       See {@link #getSpeciesName()}.
             * @param cioId             See {@link #getCioId()}.
             * @param cioName           See {@link #getCioName()}.
             * @param speciesCounts     See {@link #getSpeciesCounts()}.
             */
            public SimpleMultiSpeciesDiffExprFileBean(String omaId, String omaDescription, List<String> geneIds,
                    List<String> geneNames, List<String> entityIds, List<String> entityNames,
                    List<String> stageIds, List<String> stageNames, String cioId, String cioName, 
                    List<SpeciesCounts> speciesCounts) {
                super(geneIds, geneNames, entityIds, entityNames, stageIds, stageNames,
                        null, null, null, null, null, null);
                this.omaId = omaId;
                this.omaDescription = omaDescription;
                this.cioId = cioId;
                this.cioName = cioName;
                this.speciesCounts = speciesCounts;
            }
    
            /** 
             * @return  the {@code String} that is the ID of ancestral OMA node of the gene.
             */
            public String getOmaId() {
                return omaId;
            }
            /**
             * @param omaId A {@code String} that is the ID of ancestral OMA node of the gene.
             * @see #getOmaId()
             */
            public void setOmaId(String omaId) {
                this.omaId = omaId;
            }
            
            /** 
             * @return  the {@code String} that is the description of ancestral OMA node.
             */
            public String getOmaDescription() {
                return omaDescription;
            }
            /** 
             * @param omaDescription A {@code String} that is the description of ancestral OMA node.
             * @see #getOmaDescription()
             */
            public void setOmaDescription(String omaDescription) {
                this.omaDescription = omaDescription;
            }
    
            /**
             * @return  the {@code String} that is the ID of the CIO statement.
             */
            public String getCioId() {
                return cioId;
            }
            /**
             * @param cioId A {@code String} that is the ID of the CIO statement.
             * @see #getCioId()
             */
            public void setCioId(String cioId) {
                this.cioId = cioId;
            }
            
            /** 
             * @return  the {@code String} that is the name of the CIO statement.
             */
            public String getCioName() {
                return cioName;
            }
            /**
             * @param cioName   A {@code String} that is the name of the CIO statement.
             * @see #getCioName()
             */
            public void setCioName(String cioName) {
                this.cioName = cioName;
            }
    
    
            /** TODO Javadoc
             * @return the speciesCounts
             */
            public List<SpeciesCounts> getSpeciesCounts() {
                return speciesCounts;
            }
    
            /** TODO Javadoc
             * @param speciesCounts the speciesCounts to set
             */
            public void setSpeciesCounts(List<SpeciesCounts> speciesCounts) {
                this.speciesCounts = speciesCounts;
            }
    
            @Override
            public int hashCode() {
                final int prime = 31;
                int result = super.hashCode();
                result = prime * result + ((cioId == null) ? 0 : cioId.hashCode());
                result = prime * result + ((cioName == null) ? 0 : cioName.hashCode());
                result = prime * result + ((omaDescription == null) ? 0 : omaDescription.hashCode());
                result = prime * result + ((omaId == null) ? 0 : omaId.hashCode());
                result = prime * result + ((speciesCounts == null) ? 0 : speciesCounts.hashCode());
                return result;
            }
    
            @Override
            public boolean equals(Object obj) {
                if (this == obj)
                    return true;
                if (!super.equals(obj))
                    return false;
                if (getClass() != obj.getClass())
                    return false;
                SimpleMultiSpeciesDiffExprFileBean other = (SimpleMultiSpeciesDiffExprFileBean) obj;
                if (cioId == null) {
                    if (other.cioId != null)
                        return false;
                } else if (!cioId.equals(other.cioId))
                    return false;
                if (cioName == null) {
                    if (other.cioName != null)
                        return false;
                } else if (!cioName.equals(other.cioName))
                    return false;
                if (omaDescription == null) {
                    if (other.omaDescription != null)
                        return false;
                } else if (!omaDescription.equals(other.omaDescription))
                    return false;
                if (omaId == null) {
                    if (other.omaId != null)
                        return false;
                } else if (!omaId.equals(other.omaId))
                    return false;
                if (speciesCounts == null) {
                    if (other.speciesCounts != null)
                        return false;
                } else if (!speciesCounts.equals(other.speciesCounts))
                    return false;
                return true;
            }
    
            @Override
            public String toString() {
                return super.toString() + 
                        " - OMA ID: " + getOmaId() + " - OMA description: " + getOmaDescription() + 
                        " - CIO ID: " + getCioId() + " - CIO name: " + getCioName() +
                        " - Species counts=" + getSpeciesCounts().toString();
            }
       }

    /**
     * A bean representing a row of a complete differential expression multi-species file. 
     * Getter and setter names must follow standard bean definitions.
     * 
     * @author Valentine Rech de Laval
     * @version Bgee 13 Mar. 2015
     * @since Bgee 13
     */
    public static class CompleteMultiSpeciesDiffExprFileBean extends DiffExprFileBean {

        /**
         * @see #getOmaId()
         */
        private String omaId;
        /**
         * @see #getOmaDescription()
         */
        private String omaDescription;
        /**
         * @see getCioId()
         */
        private String cioId;
        /**
         * @see getCioName()
         */
        private String cioName;

        /**
         * 0-argument constructor of the bean.
         */
        public CompleteMultiSpeciesDiffExprFileBean() {
        }

        /**
         * Constructor providing all arguments of the class.
         *
         * @param omaId                     See {@link #getOmaId()}.
         * @param omaDescription            See {@link #getOmaDescription()}.         
         * @param geneIds                   See {@link #getGeneIds()}.
         * @param geneNames                 See {@link #getGeneNames()}.
         * @param entityIds                 See {@link #getEntityIds()}.
         * @param entityNames               See {@link #getEntityNames()}.
         * @param stageId                   See {@link #getStageId()}.
         * @param stageName                 See {@link #getStageName()}.
         * @param speciesId                 See {@link #getSpeciesId()}.
         * @param speciesName               See {@link #getSpeciesName()}.
         * @param cioId                     See {@link #getCioId()}.
         * @param cioName                   See {@link #getCioName()}.
         * @param affymetrixData            See {@link #getAffymetrixData()}.
         * @param affymetrixQuality         See {@link #getAffymetrixQuality()}.
         * @param affymetrixPValue          See {@link #getAffymetrixPValue()}.
         * @param affymetrixConsistentDEA   See {@link #getAffymetrixConsistentDEA()}.
         * @param affymetrixInconsistentDEA See {@link #getAffymetrixInconsistentDEA()}.
         * @param rnaSeqData                See {@link #getRNASeqData()}.
         * @param rnaSeqQuality             See {@link #getRNASeqQuality()}.
         * @param rnaSeqPValue              See {@link #getAffymetrixPValue()}.
         * @param rnaSeqConsistentDEA       See {@link #getRnaSeqConsistentDEA()}.
         * @param rnaSeqInconsistentDEA     See {@link #getRnaSeqInconsistentDEA()}.
         * @param differentialExpression    See {@link #getDifferentialExpression()}.
         * @param callQuality               See {@link #getCallQuality()}.
         */
        
        public CompleteMultiSpeciesDiffExprFileBean(String omaId, String omaDescription,
                List<String> geneIds, List<String> geneNames, List<String> entityIds, 
                List<String> entityNames, List<String> stageIds, List<String> stageNames,
                String speciesId, String speciesName, String cioId, String cioName, 
                String affymetrixData, String affymetrixQuality, Float affymetrixPValue, 
                Double affymetrixConsistentDEA, Double affymetrixInconsistentDEA, 
                String rnaSeqData, String rnaSeqQuality, Float rnaSeqPValue, 
                Double rnaSeqConsistentDEA, Double rnaSeqInconsistentDEA,
                String differentialExpression, String callQuality) {
            super(geneIds, geneNames, entityIds, entityNames, stageIds, stageNames, 
                    speciesId, speciesName, affymetrixData, affymetrixQuality, affymetrixPValue, 
                    affymetrixConsistentDEA, affymetrixInconsistentDEA, rnaSeqData, 
                    rnaSeqQuality, rnaSeqPValue, rnaSeqConsistentDEA, rnaSeqInconsistentDEA, 
                    differentialExpression, callQuality);
            this.omaId = omaId;
            this.omaDescription = omaDescription;
            this.cioId = cioId;
            this.cioName = cioName;
        }

        /** 
         * @return  the {@code String} that is the ID of ancestral OMA node of the gene.
         */
        public String getOmaId() {
            return omaId;
        }
        /**
         * @param omaId A {@code String} that is the ID of ancestral OMA node of the gene.
         * @see #getOmaId()
         */
        public void setOmaId(String omaId) {
            this.omaId = omaId;
        }
        
        /** 
         * @return  the {@code String} that is the description of ancestral OMA node.
         */
        public String getOmaDescription() {
            return omaDescription;
        }
        /** 
         * @param omaDescription A {@code String} that is the description of ancestral OMA node.
         * @see #getOmaDescription()
         */
        public void setOmaDescription(String omaDescription) {
            this.omaDescription = omaDescription;
        }

        /**
         * @return  the {@code String} that is the ID of the CIO statement.
         */
        public String getCioId() {
            return cioId;
        }
        /**
         * @param cioId A {@code String} that is the ID of the CIO statement.
         * @see #getCioId()
         */
        public void setCioId(String cioId) {
            this.cioId = cioId;
        }
        
        /** 
         * @return  the {@code String} that is the name of the CIO statement.
         */
        public String getCioName() {
            return cioName;
        }
        /**
         * @param cioName   A {@code String} that is the name of the CIO statement.
         * @see #getCioName()
         */
        public void setCioName(String cioName) {
            this.cioName = cioName;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = super.hashCode();
            result = prime * result + ((cioId == null) ? 0 : cioId.hashCode());
            result = prime * result + ((cioName == null) ? 0 : cioName.hashCode());
            result = prime * result + ((omaDescription == null) ? 0 : omaDescription.hashCode());
            result = prime * result + ((omaId == null) ? 0 : omaId.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (!super.equals(obj))
                return false;
            if (getClass() != obj.getClass())
                return false;
            CompleteMultiSpeciesDiffExprFileBean other = (CompleteMultiSpeciesDiffExprFileBean) obj;
            if (cioId == null) {
                if (other.cioId != null)
                    return false;
            } else if (!cioId.equals(other.cioId))
                return false;
            if (cioName == null) {
                if (other.cioName != null)
                    return false;
            } else if (!cioName.equals(other.cioName))
                return false;
            if (omaDescription == null) {
                if (other.omaDescription != null)
                    return false;
            } else if (!omaDescription.equals(other.omaDescription))
                return false;
            if (omaId == null) {
                if (other.omaId != null)
                    return false;
            } else if (!omaId.equals(other.omaId))
                return false;
            return true;
        }

        @Override
        public String toString() {
            return super.toString() + 
                    " - OMA ID: " + getOmaId() + " - OMA description: " + getOmaDescription() + 
                    " - CIO ID: " + getCioId() + " - CIO name: " + getCioName();
        }
   }
}