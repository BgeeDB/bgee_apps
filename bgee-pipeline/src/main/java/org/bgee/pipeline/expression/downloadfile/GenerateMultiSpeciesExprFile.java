package org.bgee.pipeline.expression.downloadfile;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.expressiondata.CallDAO.CallTO.DataState;
import org.bgee.model.dao.api.gene.GeneDAO;
import org.bgee.model.dao.api.gene.GeneDAO.GeneTO;
import org.bgee.model.dao.api.ontologycommon.CIOStatementDAO;
import org.bgee.model.dao.api.ontologycommon.CIOStatementDAO.CIOStatementTO;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.model.file.DownloadFile.CategoryEnum;
import org.bgee.pipeline.BgeeDBUtils;
import org.bgee.pipeline.CommandRunner;
import org.bgee.pipeline.Utils;
import org.bgee.pipeline.expression.downloadfile.GenerateMultiSpeciesDiffExprFile.MultiSpeciesDiffExprFileType;
import org.supercsv.cellprocessor.Trim;
import org.supercsv.cellprocessor.constraint.IsElementOf;
import org.supercsv.cellprocessor.constraint.LMinMax;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.constraint.StrNotNullOrEmpty;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.dozer.CsvDozerBeanWriter;
import org.supercsv.io.dozer.ICsvDozerBeanWriter;


/**
 * Class used to generate multi-species expression download files (simple and advanced files) 
 * from the Bgee database.
 *
 * @author 	Valentine Rech de Laval
 * @version Bgee 13, Oct. 2015
 * @since 	Bgee 13
 */
public class GenerateMultiSpeciesExprFile   extends GenerateDownloadFile 
                                            implements GenerateMultiSpeciesDownloadFile {

    /**
     * {@code Logger} of the class.
     */
    private final static Logger log = LogManager.getLogger(
            GenerateMultiSpeciesExprFile.class.getName());
    
    //DRY it's the same in GenerateExprFile
    /**
     * A {@code String} that is the name of the column containing expression/no-expression found
     * with EST experiment, in the download file.
     */
    public final static String EST_DATA_COLUMN_NAME = "EST data";

    /**
     * A {@code String} that is the name of the column containing call quality found with
     * EST experiment, in the download file.
     */
    public final static String EST_CALL_QUALITY_COLUMN_NAME = "EST call quality";

    /**
     * A {@code String} that is the name of the column containing if an EST experiment is observed, 
     * in the download file.
     */
    public final static String EST_OBSERVED_DATA_COLUMN_NAME = "Including EST observed data";

    /**
     * A {@code String} that is the name of the column containing expression/no-expression
     * found with <em>in situ</em> experiment, in the download file.
     */
    public final static String INSITU_DATA_COLUMN_NAME = "In situ data";

    /**
     * A {@code String} that is the name of the column containing call quality found with
     * <em>in situ</em> experiment, in the download file.
     */
    public final static String INSITU_CALL_QUALITY_COLUMN_NAME = "In situ call quality";

    /**
     * A {@code String} that is the name of the column containing if an <em>in situ</em> experiment 
     * is observed, in the download file.
     */
    public final static String INSITU_OBSERVED_DATA_COLUMN_NAME = "Including in situ observed data";

    /**
     * A {@code String} that is the name of the column containing expression/no-expression
     * found with relaxed <em>in situ</em> experiment, in the download file.
     */
    public final static String RELAXED_INSITU_DATA_COLUMN_NAME = "Relaxed in situ data";

    /**
     * A {@code String} that is the name of the column containing call quality found with
     * <em>in situ</em> experiment, in the download file.
     */
    public final static String RELAXED_INSITU_CALL_QUALITY_COLUMN_NAME = 
            "Relaxed in situ call quality";

    /**
     * A {@code String} that is the name of the column containing if a relaxed
     * <em>in situ</em> experiment is observed, in the download file.
     */
    public final static String RELAXED_INSITU_OBSERVED_DATA_COLUMN_NAME = 
            "Including relaxed in situ observed data";

    /**
     * A {@code String} that is the name of the column containing whether the call include
     * observed data or not.
     */
    public final static String INCLUDING_OBSERVED_DATA_COLUMN_NAME = "Including observed data";

    /**
     * A {@code String} that is the name of the column containing merged
     * expression/no-expression from different data types, in the download file.
     */
    public final static String EXPRESSION_COLUMN_NAME = "Expression";

    /**
     * Main method to trigger the generate multi-species expression TSV download files
     * (simple and complete files) from Bgee database. Parameters that must be provided
     * in order in {@code args} are:
     * <ol>
     * <li>a {@code Map} where keys are {@code String}s that are names given 
     *     to groups of species, the associated value being a {@code Set} of {@code String}s 
     *     that are the IDs of the species composing the group. Entries of the {@code Map} 
     *     must be separated by {@link CommandRunner#LIST_SEPARATOR}, keys must be  
     *     separated from their associated value by {@link CommandRunner#KEY_VALUE_SEPARATOR}, 
     *     values must be separated using {@link CommandRunner#VALUE_SEPARATOR}, see 
     *     {@link org.bgee.pipeline.CommandRunner#parseMapArgument(String)}
     * <li>a list of files types that will be generated ('multi-expr-simple' for 
     *     {@link MultiSpExprFileType MULTI_EXPR_SIMPLE} and 'multi-expr-complete' for 
     *     {@link MultiSpExprFileType MULTI_EXPR_COMPLETE}), separated by the 
     *     {@code String} {@link CommandRunner#LIST_SEPARATOR}. If an empty list is provided 
     *     (see {@link CommandRunner#EMPTY_LIST}), all possible file types will be generated.
     * <li>the directory path that will be used to generate download files. 
     * </ol>
     * 
     * @param args  An {@code Array} of {@code String}s containing the requested parameters.
     * @throws IllegalArgumentException If incorrect parameters were provided.
     * @throws IOException              If an error occurred while trying to write generated files.
     */
    public static void main(String[] args) throws IllegalArgumentException, IOException {
        log.entry((Object[]) args);
    
        int expectedArgLength = 3;
        if (args.length != expectedArgLength) {
            throw log.throwing(new IllegalArgumentException(
                    "Incorrect number of arguments provided, expected " + 
                    expectedArgLength + " arguments, " + args.length + " provided."));
        }
        GenerateMultiSpeciesExprFile generator = new GenerateMultiSpeciesExprFile(
                CommandRunner.parseMapArgument(args[0]).entrySet().stream()
                    .collect(Collectors.toMap(Entry::getKey, e -> new HashSet<String>(e.getValue()))),
                GenerateDownloadFile.convertToFileTypes(
                    CommandRunner.parseListArgument(args[1]), MultiSpExprFileType.class),
                args[2]);
        generator.generateMultiSpeciesExprFiles();
        
        log.exit();
    }

    /**
     * A bean representing a row of a multi-species simple expression file. 
     * Getter and setter names must follow standard bean definitions.
     * 
     * @author  Valentine Rech de Laval
     * @version Bgee 13, Oct. 2015
     * @since   Bgee 13
     */
    public static class MultiSpSimpleExprFileBean extends MultiSpeciesFileBean {
    
        /**
         * See {@link #getGeneIds()}
         */
        private List<String> geneIds;
        /**
         * See {@link #getGeneNames()}
         */
        private List<String> geneNames;

        /**
         * 0-argument constructor of the bean.
         */
        public MultiSpSimpleExprFileBean() {
        }
    
        /**
         * Constructor providing all arguments of the class.
         *
         * @param omaId             See {@link #getOmaId()}.
         * @param geneIds           See {@link #getGeneIds()}.
         * @param geneNames         See {@link #getGeneNames()}.
         * @param entityIds         See {@link #getAnatEntityIds()}.
         * @param entityNames       See {@link #getAnatEntityNames()}.
         * @param stageIds          See {@link #getStageIds()}.
         * @param stageNames        See {@link #getStageNames()}.
         */
        public MultiSpSimpleExprFileBean(String omaId, List<String> geneIds, 
                List<String> geneNames, List<String> entityIds, List<String> entityNames, 
                List<String> stageIds, List<String> stageNames) {
            super(omaId, entityIds, entityNames, stageIds, stageNames);
            this.geneIds = geneIds;
            this.geneNames = geneNames;
        }
    
        /**
         * @return  the {@code List} of {@code String}s that are the IDs of the genes.
         *          When there is several genes, they are provided in alphabetical order.
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
         *          When there is several genes, they are provided in same order as their 
         *          corresponding ID, as returned by {@link #getGeneIds()}.
         */
        public List<String> getGeneNames() {
            return geneNames;
        }
        /**
         * @param geneNames A {@code List} of {@code String}s that are the names of genes.
         * @see #getGeneNames()
         */
        public void setGeneNames(List<String> geneNames) {
            this.geneNames = geneNames;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = super.hashCode();
            result = prime * result + ((geneIds == null) ? 0 : geneIds.hashCode());
            result = prime * result + ((geneNames == null) ? 0 : geneNames.hashCode());
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
            MultiSpSimpleExprFileBean other = (MultiSpSimpleExprFileBean) obj;
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
            return true;
        }
        
        @Override
        public String toString() {
            return super.toString() + 
                    " - Gene IDs: " + getGeneIds() + " - Gene names: " + getGeneNames();
        }
    }

    /**
     * A bean representing a row of a complete expression multi-species file. 
     * Getter and setter names must follow standard bean definitions.
     * 
     * @author  Valentine Rech de Laval
     * @version Bgee 13, Oct. 2015
     * @since   Bgee 13
     */
    public static class MultiSpCompleteExprFileBean extends MultiSpeciesCompleteFileBean {
    
        /** 
         * @see getAffymetrixData()
         */
        private String affymetrixData;
        /**
         * @see getAffymetrixQuality()
         */
        private String affymetrixQuality;
        /**
         * See {@link #getAffymetrixPValue()}.
         */
        private Float affymetrixPValue;
        /**
         * See {@link #getAffymetrixConsistentDEA()}.
         */
        private Long affymetrixConsistentDEA;
        /**
         * See {@link #getAffymetrixInconsistentDEA()}.
         */
        private Long affymetrixInconsistentDEA;
        /**
         * @see getRNASeqData()
         */
        private String rnaSeqData;
        /**
         * @see getRNASeqQuality()
         */
        private String rnaSeqQuality;
        /**
         * See {@link #getRnaSeqPValue()}.
         */
        private Float rnaSeqPValue;
        /**
         * See {@link #getRnaSeqConsistentDEA()}.
         */
        private Long rnaSeqConsistentDEA;        
        /**
         * See {@link #getRnaSeqInconsistentDEA()}.
         */
        private Long rnaSeqInconsistentDEA;
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
        public MultiSpCompleteExprFileBean() {
        }
    
        /**
         * Constructor providing all arguments of the class.
         * 
         * @param omaId                     See {@link #getOmaId()}.
         * @param entityIds                 See {@link #getAnatEntityIds()}.
         * @param entityNames               See {@link #getAnatEntityNames()}.
         * @param stageIds                  See {@link #getStageIds()}.
         * @param stageNames                See {@link #getStageNames()}.
         * @param geneId                    See {@link #getGeneId()}.
         * @param geneName                  See {@link #getGeneName()}.
         * @param cioId                     See {@link #getCioId()}.
         * @param cioName                   See {@link #getCioName()}.
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
        public MultiSpCompleteExprFileBean(String omaId, 
                List<String> entityIds, List<String> entityNames, List<String> stageIds, 
                List<String> stageNames, String geneId, String geneName, 
                String cioId, String cioName, String speciesId, String speciesName,
                String affymetrixData, String affymetrixQuality, Float affymetrixPValue, 
                Long affymetrixConsistentDEA, Long affymetrixInconsistentDEA, 
                String rnaSeqData, String rnaSeqQuality, Float rnaSeqPValue, 
                Long rnaSeqConsistentDEA, Long rnaSeqInconsistentDEA,
                String differentialExpression, String callQuality) {
            super(omaId, entityIds, entityNames, stageIds, stageNames, 
                    geneId, geneName, cioId, cioName, speciesId, speciesName);
            this.affymetrixData = affymetrixData;
            this.affymetrixQuality = affymetrixQuality;
            this.affymetrixPValue = affymetrixPValue; 
            this.affymetrixConsistentDEA = affymetrixConsistentDEA;
            this.affymetrixInconsistentDEA = affymetrixInconsistentDEA; 
            this.rnaSeqData = rnaSeqData ;
            this.rnaSeqQuality = rnaSeqQuality;
            this.rnaSeqPValue = rnaSeqPValue; 
            this.rnaSeqConsistentDEA = rnaSeqConsistentDEA;
            this.rnaSeqInconsistentDEA = rnaSeqInconsistentDEA; 
            this.differentialExpression = differentialExpression;
            this.callQuality = callQuality;
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
         * @return  the {@code Long} that is the number of analysis using 
         *          Affymetrix data where the same call is found.
         */
        public Long getAffymetrixConsistentDEA() {
            return affymetrixConsistentDEA;
        }
        /**
         * @param affymetrixConsistentDEA   A {@code Long} that is the number of analysis using 
         *                                  Affymetrix data where the same call is found.
         */
        public void setAffymetrixConsistentDEA(Long affymetrixConsistentDEA) {
            this.affymetrixConsistentDEA = affymetrixConsistentDEA;
        }
    
        /**
         * @return  the {@code Long} that is the number of analysis using 
         *          Affymetrix data where a different call is found.
         */
        public Long getAffymetrixInconsistentDEA() {
            return affymetrixInconsistentDEA;
        }
        /**
         * @param affymetrixInconsistentDEA A {@code Long} that is the number of analysis using 
         *                                  Affymetrix data where a different call is found.
         */
        public void setAffymetrixInconsistentDEA(Long affymetrixInconsistentDEA) {
            this.affymetrixInconsistentDEA = affymetrixInconsistentDEA;
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
         * @return  the {@code Long} that is the number of analysis using 
         *          RNA-Seq data where the same call is found.
         */
        public Long getRnaSeqConsistentDEA() {
            return rnaSeqConsistentDEA;
        }
        /**
         * @param rnaSeqConsistentDEA   A {@code Long} that is the number of analysis using 
         *                              RNA-Seq data where the same call is found.
         */
        public void setRnaSeqConsistentDEA(Long rnaSeqConsistentDEA) {
            this.rnaSeqConsistentDEA = rnaSeqConsistentDEA;
        }
    
        /**
         * @return  the {@code Long} that is the number of analysis using 
         *          RNA-Seq data where a different call is found.
         */
        public Long getRnaSeqInconsistentDEA() {
            return rnaSeqInconsistentDEA;
        }
        /**
         * @param rnaSeqInconsistentDEA A {@code Long} that is the number of analysis using 
         *                              RNA-Seq data where a different call is found.
         */
        public void setRnaSeqInconsistentDEA(Long rnaSeqInconsistentDEA) {
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
            result = prime * result + ((affymetrixData == null) ? 0 : affymetrixData.hashCode());
            result = prime * result + ((affymetrixQuality == null) ? 0 : affymetrixQuality.hashCode());
            result = prime * result + ((affymetrixPValue == null) ? 0 : affymetrixPValue.hashCode());
            result = prime * result +
                    ((affymetrixConsistentDEA == null) ? 0 : affymetrixConsistentDEA.hashCode());
            result = prime * result +
                    ((affymetrixInconsistentDEA == null) ? 0 : affymetrixInconsistentDEA.hashCode());
            result = prime * result + ((rnaSeqData == null) ? 0 : rnaSeqData.hashCode());
            result = prime * result + ((rnaSeqQuality == null) ? 0 : rnaSeqQuality.hashCode());
            result = prime * result + ((rnaSeqPValue == null) ? 0 : rnaSeqPValue.hashCode());
            result = prime * result + 
                    ((rnaSeqConsistentDEA == null) ? 0 : rnaSeqConsistentDEA.hashCode());
            result = prime * result + 
                    ((rnaSeqInconsistentDEA == null) ? 0 : rnaSeqInconsistentDEA.hashCode());
            result = prime * result +
                    ((differentialExpression == null) ? 0 : differentialExpression.hashCode());
            result = prime * result + ((callQuality == null) ? 0 : callQuality.hashCode());
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
            MultiSpCompleteExprFileBean other = (MultiSpCompleteExprFileBean) obj;
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
            if (affymetrixPValue == null) {
                if (other.affymetrixPValue != null)
                    return false;
            } else if (!affymetrixPValue.equals(other.affymetrixPValue))
                return false;
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
            if (rnaSeqPValue == null) {
                if (other.rnaSeqPValue != null)
                    return false;
            } else if (!rnaSeqPValue.equals(other.rnaSeqPValue))
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
            if (differentialExpression == null) {
                if (other.differentialExpression != null)
                    return false;
            } else if (!differentialExpression.equals(other.differentialExpression))
                return false;
            if (callQuality == null) {
                if (other.callQuality != null)
                    return false;
            } else if (!callQuality.equals(other.callQuality))
                return false;
            return true;
        }
    
        @Override
        public String toString() {
            return super.toString() + " - Affymetrix data: " + getAffymetrixData() + 
                    " - Affymetrix quality: " + getAffymetrixQuality() + 
                    " - Affymetrix p-value: " + getAffymetrixPValue() + 
                    " - Affymetrix consistent DEA: " + getAffymetrixConsistentDEA() + 
                    " - Affymetrix inconsistent DEA: " + getAffymetrixInconsistentDEA() + 
                    " - RNA-Seq data: " + getRNASeqData() + 
                    " - RNA-Seq quality: " + getRNASeqQuality() + 
                    " - RNA-Seq p-value: " + getRnaSeqPValue() + 
                    " - RNA-Seq consistent DEA: " + getRnaSeqConsistentDEA() + 
                    " - RNA-Seq inconsistent DEA: " + getRnaSeqInconsistentDEA() + 
                    " - Differential expression: " + getDifferentialExpression() + 
                    " - Call quality: " + getCallQuality();
        }
    }
    /**
     * An {@code Enum} used to define, for each data type (Affymetrix, RNA-Seq, ...),
     * as well as for the summary column, the data state of the call.
     * <ul>
     * <li>{@code NO_DATA}:         no data from the associated data type allowed to produce the call.
     * <li>{@code EXPRESSION}:      expression was detected from the associated data type.
     * <li>{@code NO_EXPRESSION}:   no-expression was detected from the associated data type.
     * <li>{@code WEAK_AMBIGUITY}:  different data types are not coherent with an inferred
     *                              no-expression call (for instance, Affymetrix data reveals an 
     *                              expression while <em>in situ</em> data reveals an inferred
     *                              no-expression).
     * <li>{@code HIGH_AMBIGUITY}:  different data types are not coherent without at least
     *                              an inferred no-expression call (for instance, Affymetrix data 
     *                              reveals expression while <em>in situ</em> data reveals a 
     *                              no-expression without been inferred).
     * </ul>
     * 
     * @author  Valentine Rech de Laval
     * @version Bgee 13, Oct. 2015
     * @since   Bgee 13
     */
    public enum ExpressionData {
        NO_DATA("no data"), NO_EXPRESSION("absent"), EXPRESSION("expression"),
        WEAK_AMBIGUITY(GenerateDownloadFile.WEAK_AMBIGUITY), 
        HIGH_AMBIGUITY(GenerateDownloadFile.STRONG_AMBIGUITY);

        private final String stringRepresentation;

        /**
         * Constructor providing the {@code String} representation of this {@code ExpressionData}.
         * 
         * @param stringRepresentation A {@code String} corresponding to this {@code ExpressionData}.
         */
        private ExpressionData(String stringRepresentation) {
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
     * An {@code Enum} used to define whether the call has been observed. This is to distinguish
     * from propagated data only, that should provide a lower confidence in the call.
     * <ul>
     * <li>{@code OBSERVED}:    the call has been observed at least once.
     * <li>{@code NOTOBSERVED}: the call has never been observed.
     * </ul>
     * 
     * @author  Valentine Rech de Laval
     * @version Bgee 13, Oct. 2015
     * @since   Bgee 13
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
     * An {@code Enum} used to define the possible multi-species expression file types 
     * to be generated.
     * <ul>
     * <li>{@code MULTI_EXPR_SIMPLE}:   presence/absence of expression in multi-species 
     *                                  in a simple download file.
     * <li>{@code MULTI_EXPR_COMPLETE}: presence/absence of expression in multi-species
     *                                  in an advanced download file.
     * </ul>
     * 
     * @author  Valentine Rech de Laval
     * @version Bgee 13, Oct. 2015
     * @since   Bgee 13
     */
    //FIXME: alternatively, if you use the Bean principle, you could simply use different bean types, 
    //so that you don't need this Enum
    public enum MultiSpExprFileType implements FileType {
        MULTI_EXPR_SIMPLE(CategoryEnum.EXPR_CALLS_SIMPLE, true),
        MULTI_EXPR_COMPLETE(CategoryEnum.EXPR_CALLS_COMPLETE, false);
    
        /**
         * A {@code CategoryEnum} that is the category of files of this type.
         */
        private final CategoryEnum category;
        
        /**
         * A {@code boolean} defining whether this {@code MultiSpExprFileType} is a simple 
         * file type.
         */
        private final boolean simpleFileType;
    
        /**
         * Constructor providing the {@code CategoryEnum} of this {@code MultiSpExprFileType},
         * and a {@code boolean} defining whether this {@code MultiSpExprFileType}
         * is a simple file type.
         */
        private MultiSpExprFileType(CategoryEnum category, boolean simpleFileType) {
            this.category = category;
            this.simpleFileType = simpleFileType;
        }
    
        @Override
        public String getStringRepresentation() {
            return this.category.getStringRepresentation();
        }
        @Override
        public boolean isSimpleFileType() {
            return this.simpleFileType;
        }
        @Override
        public String toString() {
            return this.getStringRepresentation();
        }
        @Override
        public CategoryEnum getCategory() {
            return this.category;
        }
    }

    /**
     * A {@code Map} where keys are {@code String}s that are names given to a group of species, 
     * the associated values being {@code Set}s of {@code String}s corresponding to 
     * species IDs belonging to the group.
     */
    private Map<String, Set<String>> providedGroups;

    /**
     * A {@code Map} where keys are {@code String}s corresponding to CIO IDs,
     * the associated values being {@code CIOStatementTO}s corresponding to CIO TOs.
     */
    private Map<String, CIOStatementTO> cioStatementByIds;

    /**
     * Default constructor. 
     */
    //suppress warning as this default constructor should not be used.
    @SuppressWarnings("unused")
    private GenerateMultiSpeciesExprFile() {
        this(null, null, null);
    }

    /**
     * Constructor providing parameters to generate files, using the default {@code DAOManager}.
     * 
     * @param speciesIds    A {@code List} of {@code String}s that are the IDs of species 
     *                      we want to generate data for. If {@code null} or empty, all species 
     *                      are used.
     * @param taxonId       A {@code String} that is the ID of the common ancestor taxon
     *                      we want to into account. If {@code null} or empty, TODO to be decided
     * @param fileTypes     A {@code Set} of {@code MultiSpExprFileType}s that are the types
     *                      of files we want to generate. If {@code null} or empty, 
     *                      all {@code MultiSpExprFileType}s are generated.
     * @param directory     A {@code String} that is the directory where to store files.
     * @param groupPrefix   A {@code String} that is the prefix of the group we want to use 
     *                      for files names. If {@code null} or empty, TODO  to be decided.
     * @throws IllegalArgumentException If {@code directory} is {@code null} or blank.
     */
    public GenerateMultiSpeciesExprFile(Map<String, Set<String>> providedGroups,  
            Set<MultiSpExprFileType> fileTypes, String directory) 
                    throws IllegalArgumentException {
        this(null, providedGroups, fileTypes, directory);
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
     *                      we want to into account. If {@code null} or empty, TODO to be decided.
     * @param fileTypes     A {@code Set} of {@code MultiSpExprFileType}s that are the types
     *                      of files we want to generate. If {@code null} or empty, 
     *                      all {@code MultiSpExprFileType}s are generated.
     * @param directory     A {@code String} that is the directory where to store files.
     * @param groupPrefix   A {@code String} that is the prefix the group we want to use 
     *                      for files names. If {@code null} or empty, TODO  to be decided.
     * @throws IllegalArgumentException If {@code directory} is {@code null} or blank.
     */
    public GenerateMultiSpeciesExprFile(MySQLDAOManager manager, Map<String, Set<String>> providedGroups,  
            Set<MultiSpExprFileType> fileTypes, String directory) throws IllegalArgumentException {
        // We do not use species IDs global variable as for single species files.
        // TODO: Correct class schema of all download files to avoid to set null 
        // when calling super constructor for species IDs
        super(manager, null, fileTypes, directory);

        if (providedGroups == null || providedGroups.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("No group is provided"));
        }
        this.providedGroups = providedGroups;
        this.cioStatementByIds = new HashMap<String, CIOStatementTO>();
    }
    
    /**
     * TODO Javadoc
     */
    public void generateMultiSpeciesExprFiles() throws IOException {
        log.entry();
        log.info("Start generating multi-species expression files {} with parameters {} in the directory {}",
                this.fileTypes, this.providedGroups, this.directory);

        // If no file types are given by user, we set all file types
        if (this.fileTypes == null || this.fileTypes.isEmpty()) {
            this.fileTypes = EnumSet.allOf(MultiSpeciesDiffExprFileType.class);
        }
        
        // We retrieve all CIO so it's common to all groups
        this.getCIOStatementDAO().setAttributes(CIOStatementDAO.Attribute.ID, 
                CIOStatementDAO.Attribute.NAME, CIOStatementDAO.Attribute.TRUSTED);
        this.cioStatementByIds = BgeeDBUtils.getCIOStatementTOsByIds(this.getCIOStatementDAO());
        
        for (Entry<String, Set<String>> currentGroup : this.providedGroups.entrySet()) {
            Set<String> setSpecies = currentGroup.getValue();
            if (setSpecies == null || setSpecies.isEmpty()) {
                throw log.throwing(new IllegalArgumentException("No species ID is provided"));
            }
            //Validate provided species, and retrieve species names
            Map<String, String> speciesNamesByIds = 
                    this.checkAndGetLatinNamesBySpeciesIds(setSpecies);
            
            String currentPrefix = currentGroup.getKey();
            String taxonId = this.getLeastCommonAncestor(setSpecies);
            
            // Retrieve gene TOs, stage names, anat. entity names, and cio names 
            // for all species of the group
            this.getGeneDAO().setAttributes(GeneDAO.Attribute.ID, GeneDAO.Attribute.NAME, 
                    GeneDAO.Attribute.OMA_PARENT_NODE_ID, GeneDAO.Attribute.SPECIES_ID);
            Map<String, GeneTO> geneTOByIds = 
                    BgeeDBUtils.getGeneTOsByIds(setSpecies, this.getGeneDAO());
            Map<String, String> stageNamesByIds = 
                    BgeeDBUtils.getStageNamesByIds(setSpecies, this.getStageDAO());
            Map<String, String> anatEntityNamesByIds = 
                    BgeeDBUtils.getAnatEntityNamesByIds(setSpecies, this.getAnatEntityDAO());

            // Generate multi-species expression files
            log.info("Start generating of multi-species expression files for the group {} " +
                    "with the species {} and the ancestral taxon ID {}...", 
                    currentPrefix, speciesNamesByIds.values(), taxonId);

            try {
                //XXX: maybe all the xxxByIds could be stored in class attributes, 
                //to simplify this method signature
                this.generateMultiSpeciesExprFilesForOneGroup(currentPrefix, taxonId, 
                        speciesNamesByIds, geneTOByIds, stageNamesByIds, anatEntityNamesByIds);
            } finally {
                // Release resources after each group. 
                this.getManager().releaseResources();
            }
            
            log.info("Done generating of multi-species expression files for the group {}.", 
                    currentGroup);
        }
        log.exit();

        Set<String> setSpecies = new HashSet<String>();
        if (this.speciesIds != null && !this.speciesIds.isEmpty()) {
            setSpecies = new HashSet<String>(this.speciesIds);
        } else {
            throw log.throwing(new IllegalArgumentException("No species ID is provided"));
        }
    }

    /**
     * TODO Javadoc
     * @throws IOException 
     */
    private void generateMultiSpeciesExprFilesForOneGroup(String groupName, String taxonId, 
            Map<String, String> speciesNamesByIds, Map<String, GeneTO> geneTOsByIds, 
            Map<String, String> stageNamesByIds, Map<String, String> anatEntityNamesByIds) 
                    throws IOException {
        log.entry(groupName, taxonId, speciesNamesByIds, 
                geneTOsByIds, stageNamesByIds, anatEntityNamesByIds);

        Set<String> speciesFilter = speciesNamesByIds.keySet();

        log.debug("Start generating multi-species expression files for: " + 
                "groupName={}, taxon ID={}, species IDs={} and file types {}...", 
                groupName, taxonId, speciesFilter, this.fileTypes);

        //********************************
        // RETRIEVE DATA FROM DATA SOURCE
        //********************************
        log.trace("Start retrieving data...");
        
        // We load homologous genes 
//        List<GeneTO> homologousGenes = BgeeDBUtils.getHomologousGenes(speciesFilter,
//                this.getManager().getGeneDAO(), taxonId);
        
        // We load homologous organs 
//        List<SimAnnotToAnatEntityTO> homologousAnatEntities = 
//                BgeeDBUtils.getHomologousAnatEntities(speciesFilter, this.taxonId, 
//                        this.getManager().getSummarySimilarityAnnotationDAO());
        
        // Load expression and no-expression calls order by OMA node ID
        // TODO to be implemented

        log.trace("Done retrieving data.");
        
        //****************************
        // PRODUCE AND WRITE DATA
        //****************************
        log.trace("Start generating and writing file content");

        // Now, we write all requested files at once. This way, we will generate the data only once, 
        // and we will not have to store them in memory.
        
        // First we allow to store file names, writers, etc, associated to a FileType, 
        // for the catch and finally clauses. 
        Map<FileType, String> generatedFileNames = new HashMap<FileType, String>();

        // We will write results in temporary files that we will rename at the end
        // if everything is correct
        String tmpExtension = ".tmp";

        // In order to close all writers in a finally clause.
        Map<MultiSpExprFileType, ICsvDozerBeanWriter> writersUsed = 
                new HashMap<MultiSpExprFileType, ICsvDozerBeanWriter>();
        try {
            //**************************
            // OPEN FILES, CREATE WRITERS, WRITE HEADERS
            //**************************
            Map<FileType, CellProcessor[]> processors = new HashMap<FileType, CellProcessor[]>();
            Map<FileType, String[]> headers = new HashMap<FileType, String[]>();

            // Get ordered species names
            List<String> orderedSpeciesNames = this.getSpeciesNameAsList(
                    this.speciesIds, speciesNamesByIds);
            
            for (FileType fileType : this.fileTypes) {
                MultiSpExprFileType currentFileType = (MultiSpExprFileType) fileType;

                String[] fileTypeHeaders = this.generateHeader(currentFileType, orderedSpeciesNames);
                headers.put(currentFileType, fileTypeHeaders);

                CellProcessor[] fileTypeProcessors = 
                        this.generateCellProcessors(currentFileType, fileTypeHeaders);
                processors.put(currentFileType, fileTypeProcessors);

                // Create file name
                String fileName = groupName + "_" +
                        currentFileType.getStringRepresentation() + EXTENSION;
                generatedFileNames.put(currentFileType, fileName);

                // write in temp file
                File file = new File(this.directory, fileName + tmpExtension);
                // override any existing file
                if (file.exists()) {
                    file.delete();
                }

                
                // create and configure writer
                ICsvDozerBeanWriter beanWriter = new CsvDozerBeanWriter(new FileWriter(file),
                        Utils.getCsvPreferenceWithQuote(this.generateQuoteMode(fileTypeHeaders)));
                // configure the mapping from the fields to the CSV columns
                if (currentFileType.isSimpleFileType()) {
                    beanWriter.configureBeanMapping(MultiSpSimpleExprFileBean.class, 
                            this.generateFieldMapping(
                                    currentFileType, fileTypeHeaders, orderedSpeciesNames));
                } else {
                    beanWriter.configureBeanMapping(MultiSpCompleteExprFileBean.class, 
                            this.generateFieldMapping(
                                    currentFileType, fileTypeHeaders, orderedSpeciesNames));
                }
                //  Write header
                beanWriter.writeHeader(fileTypeHeaders);
                log.debug("Write header: {}", Arrays.toString(fileTypeHeaders));
                
                writersUsed.put(currentFileType, beanWriter);
            }

            // ****************************
            // WRITE ROWS
            // ****************************
            // TODO to be implemented
            
//          CallTOResultSet rs = null;
//          Set<CallTO> groupedCallTOs = new HashSet<CallTO>();
//          
//          CallTO previousTO = null;
//          while (rs.next()) {
//              CallTO currentTO = rs.getTO();
//              if (previousTO != null && currentTO.getOMANodeId() != previousTO.getOMANodeId()) {
//                  // We propagate expression and no-expression calls and order them
//                        - when there is no data in a condition, but the no-expression in  
//                          a sub-stage => no expression low quality 
//                        - propagation on high-level stages
//                  // We compute and write the rows in all files
//                        - we filter families with only no-expression low quality            
//                        - we filter families with only no diff expression
//                        - do system to keep only the 'Observed' in single file
//                          but for the first generation we do not active.
//                        - filter untrusted homology annotations in simple file
//                  // We clear the set containing TO of an unique OMA Node ID
//                  groupedCallTOs.clear();
//              }
//              groupedCallTOs.add(to);
//              previousTO = to;
//          }

        } catch (Exception e) {
            this.deleteTempFiles(generatedFileNames, tmpExtension);
            throw e;
        } finally {
            for (ICsvDozerBeanWriter writer : writersUsed.values()) {
                writer.close();
            }
        }

        // Now, if everything went fine, we rename the temporary files
        this.renameTempFiles(generatedFileNames, tmpExtension);

        log.exit();
    }

    /**
     * TODO Javadoc
     *
     * @param speciesIds
     * @param speciesNamesByIds
     * @return
     */
    //TODO: DRY
    private List<String> getSpeciesNameAsList(
            List<String> speciesIds, Map<String, String> speciesNamesByIds) {
        log.entry();
        
        List<String> names = new ArrayList<String>();
        for (String id : speciesIds) {
            names.add(speciesNamesByIds.get(id));
        }
        assert names.size() == speciesIds.size();

        return log.exit(names);
    }

    /**
     * Generates an {@code Array} of {@code CellProcessor}s used to process a multi-species 
     * expression TSV file of type {@code fileType}.
     * 
     * @param fileType  The {@code MultiSpExprFileType} of the file to be generated.
     * @param header    An {@code Array} of {@code String}s representing the names 
     *                  of the columns of a multi-species expression file.
     * @return          An {@code Array} of {@code CellProcessor}s used to process 
     *                  a multi-species expression file.
     * @throw IllegalArgumentException If {@code fileType} is not managed by this method.
     */
    private CellProcessor[] generateCellProcessors(MultiSpExprFileType fileType, String[] header)
            throws IllegalArgumentException {
        log.entry(fileType, header);
        
        //First, we define all set of possible values
        List<Object> data = new ArrayList<Object>();
        for (ExpressionData exprData: ExpressionData.values()) {
            data.add(exprData.getStringRepresentation());
        }
        
        List<Object> qualities = new ArrayList<Object>();
        qualities.add(GenerateDownloadFile.convertDataStateToString(DataState.HIGHQUALITY));
        qualities.add(GenerateDownloadFile.convertDataStateToString(DataState.LOWQUALITY));
        qualities.add(GenerateDownloadFile.NA_VALUE);
        
        List<Object> originValues = new ArrayList<Object>();
        for (ObservedData observedData: ObservedData.values()) {
            originValues.add(observedData.getStringRepresentation());
        }

        //Then, we build the CellProcessor
        CellProcessor[] processors = new CellProcessor[header.length];
        for (int i = 0; i < header.length; i++) {
            switch (header[i]) {
            // *** CellProcessors common to all file types ***
                case OMA_ID_COLUMN_NAME: 
                    processors[i] = new StrNotNullOrEmpty(); 
                    break;
                //XXX change STAGE_XX_COLUMN_NAME to STAGE_XX_LIST_COLUMN_NAME 
                // when we will have several stages for one StageGroup.
                case STAGE_ID_COLUMN_NAME: 
                case STAGE_NAME_COLUMN_NAME: 
                case ANAT_ENTITY_ID_LIST_ID_COLUMN_NAME: 
                case ANAT_ENTITY_NAME_LIST_ID_COLUMN_NAME: 
                    processors[i] = new Utils.FmtMultipleStringValues(new Trim()); 
                    break;
            }
            
            //if it was one of the column common to all file types, 
            //iterate next column name
            if (processors[i] != null) {
                continue;
            }

            if (fileType.isSimpleFileType()) {
                // *** Attributes specific to simple file ***
                switch (header[i]) {
                    case GENE_ID_LIST_COLUMN_NAME: 
                        processors[i] = new Utils.FmtMultipleStringValues(new Trim()); 
                        break;
                    case GENE_NAME_LIST_COLUMN_NAME: 
                        //gene names can be blank
                        processors[i] = new Utils.FmtMultipleStringValues(new Trim(), true); 
                        break;
                }

                //if it was one of the gene columns, iterate next column name
                if (processors[i] != null) {
                    continue;
                }

                // TODO: for the moment, we use LMinMax() constraint but when we will add dealing 
                // with lost homologous organs, we should use StrNotNullOrEmpty() because it may
                // have N/A when an organ is lost.
                if (header[i].startsWith(EXPR_GENE_COUNT_COLUMN_NAME) ||
                        header[i].startsWith(NOT_EXPR_GENE_COUNT_COLUMN_NAME) ||
                        header[i].startsWith(NA_GENES_COUNT_COLUMN_NAME)) {
                    processors[i] = new LMinMax(0, Long.MAX_VALUE);
                }
                    
            } else {
                // *** Attributes specific to complete file ***
                switch (header[i]) {
                    case SPECIES_LATIN_NAME_COLUMN_NAME: 
                    case GENE_ID_COLUMN_NAME:
                    case CIO_ID_COLUMN_NAME:
                    case CIO_NAME_ID_COLUMN_NAME:
                        processors[i] = new StrNotNullOrEmpty();
                        break;
                    case GENE_NAME_COLUMN_NAME:
                        processors[i] = new NotNull();
                        break;
                    case EXPRESSION_COLUMN_NAME:
                    case AFFYMETRIX_DATA_COLUMN_NAME:
                    case EST_DATA_COLUMN_NAME:
                    case INSITU_DATA_COLUMN_NAME:
                    // TODO: when relaxed in situ will be in the database, uncomment following line
//                    case RELAXED_INSITU_DATA_COLUMN_NAME:
                    case RNASEQ_DATA_COLUMN_NAME:
                        processors[i] = new IsElementOf(data);
                        break;
                    case QUALITY_COLUMN_NAME:
                        processors[i] = new IsElementOf(qualities);
                        break;
                    case AFFYMETRIX_CALL_QUALITY_COLUMN_NAME:
                    case EST_CALL_QUALITY_COLUMN_NAME:
                    case INSITU_CALL_QUALITY_COLUMN_NAME:
                    // TODO: when relaxed in situ will be in the database, uncomment following line
//                      case RELAXED_INSITU_CALL_QUALITY_COLUMN_NAME:
                    case RNASEQ_CALL_QUALITY_COLUMN_NAME:
                        processors[i] = new IsElementOf(qualities);
                        break;
                    case AFFYMETRIX_OBSERVED_DATA_COLUMN_NAME:
                    case EST_OBSERVED_DATA_COLUMN_NAME:
                    case INSITU_OBSERVED_DATA_COLUMN_NAME:
                    case RNASEQ_OBSERVED_DATA_COLUMN_NAME:
//                  case RELAXED_INSITU_OBSERVED_DATA_COLUMN_NAME:
                    case INCLUDING_OBSERVED_DATA_COLUMN_NAME:
                        processors[i] = new IsElementOf(originValues);
                        break;
                }
            } 
            
            if (processors[i] == null) {
                throw log.throwing(new IllegalArgumentException("Unrecognized header: " 
                        + header[i] + " for file type: " + fileType.getStringRepresentation()));
            }
        }
        
        return log.exit(processors);
    }

    /**
     * Generates an {@code Array} of {@code String}s used to generate the header of a multi-species
     * expression TSV file of type {@code fileType}.
     * 
     * @param fileType  The {@code MultiSpExprFileType} of the file to be generated.
     * @param nbSpecies A {@code List} of {@code String}s that are the names of species 
     *                  we want to generate data for.
     * @return          An {@code Array} of {@code String}s used to produce the header.
     * @throw IllegalArgumentException If {@code fileType} is not managed by this method.
     */
    private String[] generateHeader(MultiSpExprFileType fileType, List<String> speciesNames)
        throws IllegalArgumentException {
        log.entry(fileType, speciesNames);

        String[] headers = null; 
        // For simple file, we always have 7 columns and 3 columns for each species
        int nbColumns = 7 + 3 * speciesNames.size();
        if (!fileType.isSimpleFileType()) {
            // For complete file, the number of columns is independent of the number of species.
            nbColumns = 25;
        }
        headers = new String[nbColumns];

        // *** Headers common to all file types ***
        headers[0] = OMA_ID_COLUMN_NAME;
        if (fileType.isSimpleFileType()) {
            headers[1] = ANAT_ENTITY_ID_LIST_ID_COLUMN_NAME;
            headers[2] = ANAT_ENTITY_NAME_LIST_ID_COLUMN_NAME;
            //XXX change STAGE_XX_COLUMN_NAME to STAGE_XX_LIST_COLUMN_NAME 
            headers[3] = STAGE_ID_COLUMN_NAME;
            headers[4] = STAGE_NAME_COLUMN_NAME;
        } else {
            //gene ID and gene name will be columns with index 1 and 2
            headers[3] = ANAT_ENTITY_ID_LIST_ID_COLUMN_NAME;
            headers[4] = ANAT_ENTITY_NAME_LIST_ID_COLUMN_NAME;
            headers[5] = STAGE_ID_COLUMN_NAME;
            headers[6] = STAGE_NAME_COLUMN_NAME;
        }

        if (fileType.isSimpleFileType()) {
            // *** Headers specific to simple file ***
            for (int i = 0; i < speciesNames.size(); i++) {
                // the number of columns depends on the number of species
                // gene columns are the end, so there is only 5 columns before species counts
                int columnIndex = 5 + 3 * i;
                String endHeader = " for " + speciesNames.get(i);
                headers[columnIndex] = EXPR_GENE_COUNT_COLUMN_NAME + endHeader;
                headers[columnIndex+1] = NOT_EXPR_GENE_COUNT_COLUMN_NAME + endHeader;
                headers[columnIndex+2] = NA_GENES_COUNT_COLUMN_NAME + endHeader;
            }
            // the indexes depend on the number of columns because gene columns are at the end
            headers[nbColumns - 2] = GENE_ID_LIST_COLUMN_NAME;
            headers[nbColumns - 1] = GENE_NAME_LIST_COLUMN_NAME;
        } else {
            // *** Headers specific to complete file ***
            headers[1] = GENE_ID_COLUMN_NAME;
            headers[2] = GENE_NAME_COLUMN_NAME;

            headers[7] = SPECIES_LATIN_NAME_COLUMN_NAME;                
            headers[8] = EXPRESSION_COLUMN_NAME;
            headers[9] = QUALITY_COLUMN_NAME;
            headers[10] = INCLUDING_OBSERVED_DATA_COLUMN_NAME;
            headers[11] = AFFYMETRIX_DATA_COLUMN_NAME; 
            headers[12] = AFFYMETRIX_CALL_QUALITY_COLUMN_NAME;
            headers[13] = AFFYMETRIX_OBSERVED_DATA_COLUMN_NAME;
            headers[14] = EST_DATA_COLUMN_NAME; 
            headers[15] = EST_CALL_QUALITY_COLUMN_NAME;
            headers[16] = EST_OBSERVED_DATA_COLUMN_NAME;
            headers[17] = INSITU_DATA_COLUMN_NAME; 
            headers[18] = INSITU_CALL_QUALITY_COLUMN_NAME;
            headers[19] = INSITU_OBSERVED_DATA_COLUMN_NAME;
         // TODO: when relaxed in situ will be in the database, uncomment and update following lines
//          headers[] = RELAXED_INSITU_DATA_COLUMN_NAME; 
//          headers[] = RELAXED_INSITU_CALL_QUALITY_COLUMN_NAME;
//          headers[] = RELAXED_INSITU_OBSERVED_DATA_COLUMN_NAME;
            headers[20] = RNASEQ_DATA_COLUMN_NAME; 
            headers[21] = RNASEQ_CALL_QUALITY_COLUMN_NAME;
            headers[22] = RNASEQ_OBSERVED_DATA_COLUMN_NAME;
            headers[23] = CIO_ID_COLUMN_NAME; 
            headers[24] = CIO_NAME_ID_COLUMN_NAME;
        }

        return log.exit(headers);
    }
    
    /**
     * Generate {@code Array} of {@code booleans} (one per CSV column) indicating 
     * whether each column should be quoted or not.
     *
     * @param headers   An {@code Array} of {@code String}s representing the names of the columns.
     * @return          the {@code Array} of {@code booleans} (one per CSV column) indicating 
     *                  whether each column should be quoted or not.
     */
    private boolean[] generateQuoteMode(String[] headers) {
        log.entry((Object[]) headers);
        
        boolean[] quoteMode = new boolean[headers.length];
        for (int i = 0; i < headers.length; i++) {
            switch (headers[i]) {
                case OMA_ID_COLUMN_NAME:
                case GENE_ID_LIST_COLUMN_NAME:
                case GENE_ID_COLUMN_NAME:
                case ANAT_ENTITY_ID_LIST_ID_COLUMN_NAME:
                //XXX change STAGE_ID_COLUMN_NAME to STAGE_ID_LIST_COLUMN_NAME 
                case STAGE_ID_COLUMN_NAME:
                case SPECIES_LATIN_NAME_COLUMN_NAME:                
                case CIO_ID_COLUMN_NAME: 
                case EXPRESSION_COLUMN_NAME:
                case QUALITY_COLUMN_NAME:
                case INCLUDING_OBSERVED_DATA_COLUMN_NAME:
                case AFFYMETRIX_DATA_COLUMN_NAME: 
                case AFFYMETRIX_CALL_QUALITY_COLUMN_NAME:
                case AFFYMETRIX_OBSERVED_DATA_COLUMN_NAME:
                case EST_DATA_COLUMN_NAME:
                case EST_CALL_QUALITY_COLUMN_NAME:
                case EST_OBSERVED_DATA_COLUMN_NAME:
                case INSITU_DATA_COLUMN_NAME:
                case INSITU_CALL_QUALITY_COLUMN_NAME:
                case INSITU_OBSERVED_DATA_COLUMN_NAME:
                // TODO: when relaxed in situ will be in the database, uncomment and update following lines
//                case RELAXED_INSITU_DATA_COLUMN_NAME:
//                case RELAXED_INSITU_CALL_QUALITY_COLUMN_NAME:
//                case RELAXED_INSITU_OBSERVED_DATA_COLUMN_NAME:
                case RNASEQ_DATA_COLUMN_NAME:
                case RNASEQ_CALL_QUALITY_COLUMN_NAME:
                case RNASEQ_OBSERVED_DATA_COLUMN_NAME:
                    quoteMode[i] = false; 
                    break;
                case GENE_NAME_COLUMN_NAME:
                case GENE_NAME_LIST_COLUMN_NAME:
                case ANAT_ENTITY_NAME_LIST_ID_COLUMN_NAME:
                //XXX change STAGE_NAME_COLUMN_NAME to STAGE_NAME_LIST_COLUMN_NAME 
                case STAGE_NAME_COLUMN_NAME:
                case CIO_NAME_ID_COLUMN_NAME:
                    quoteMode[i] = true; 
                    break;
                default:
                    throw log.throwing(new IllegalArgumentException("Unrecognized header: " + 
                            headers[i] + " for multi-species diff. expression file."));
            }
        }
        
        return log.exit(quoteMode);
    }
    
    /**
     * Generate the field mapping for each column of the header of a multi-species
     * expression TSV file of type {@code fileType}.
     *
     * @param fileType              A {@code MultiSpExprFileType} defining the type of file 
     *                              that will be written.
     * @param header                An {@code Array} of {@code String}s representing the names 
     *                              of the columns of a multi-species expression file.
     * @param orderedSpeciesNames   An {@code Array} of {@code String}s representing the names 
     *                              of the columns of a multi-species expression file.
     * @return                      The {@code Array} of {@code String}s that is the field mapping, 
     *                              put in the {@code Array} at the same index as the column they 
     *                              are supposed to process.
     */
    private String[] generateFieldMapping(
            MultiSpExprFileType fileType, String[] header, List<String> orderedSpeciesNames) {
        log.entry(fileType, header, orderedSpeciesNames);

        String[] fieldMapping = new String[header.length];

        // We sort species names by name lengths from the longest to the shortest.
        List<String> speciesNamesOrderedByLength = new ArrayList<String>(orderedSpeciesNames);
        speciesNamesOrderedByLength.sort((s1, s2)-> (s2.length() - s1.length()));
        
        //to do a sanity check on species columns in simple files
        Set<String> speciesFound = new HashSet<String>();
        
        for (int i = 0; i < header.length; i++) {
            switch (header[i]) {
            // *** attributes common to all file types ***
                case OMA_ID_COLUMN_NAME:
                    fieldMapping[i] = "omaId";
                    break;
                case STAGE_ID_COLUMN_NAME:
                    fieldMapping[i] = "stageIds";
                    break;
                case STAGE_NAME_COLUMN_NAME:
                    fieldMapping[i] = "stageNames";
                    break;
                case ANAT_ENTITY_ID_LIST_ID_COLUMN_NAME:
                    fieldMapping[i] = "anatEntityIds";
                    break;
                case ANAT_ENTITY_NAME_LIST_ID_COLUMN_NAME:
                    fieldMapping[i] = "anatEntityNames";
                    break;
            }
            
            //if it was one of the column common to all beans, 
            //iterate next column name
            if (fieldMapping[i] != null) {
                continue;
            }

            if (fileType.isSimpleFileType()) {
                // *** Attributes specific to simple file ***
                
                switch (header[i]) {
                    case GENE_ID_LIST_COLUMN_NAME: 
                        fieldMapping[i] = "geneIds";
                        break;
                    case GENE_NAME_LIST_COLUMN_NAME: 
                        fieldMapping[i] = "geneNames";
                        break;
                }
                //if header found, iterate next column name
                if (fieldMapping[i] != null) {
                    continue;
                }

                // We need to find the species contains in the header to be able to 
                // assign the good index to speciesDiffExprCounts.
                // For that, we iterate all species names from the longest to the shortest to  
                // retrieve the good species even if a species name matches to a subspecies name 
                // (for instance, the subspecies name 'Gorilla gorilla gorilla' and 
                // the species name 'Gorilla gorilla').
                for (String species: speciesNamesOrderedByLength) {
                    int index = orderedSpeciesNames.indexOf(species);
                    if (header[i].toLowerCase().contains(species.toLowerCase())) {
                        speciesFound.add(species);
                        
                        if (header[i].contains(EXPR_GENE_COUNT_COLUMN_NAME)) {
                            fieldMapping[i] = 
                                    "speciesDiffExprCounts[" + index + "].exprGeneCount";
                            
                        } else if (header[i].contains(NOT_EXPR_GENE_COUNT_COLUMN_NAME)) {
                            fieldMapping[i] = 
                                    "speciesDiffExprCounts[" + index + "].notExprGeneCount";
                            
                        } else if (header[i].contains(NA_GENES_COUNT_COLUMN_NAME)) {
                            fieldMapping[i] = 
                                    "speciesDiffExprCounts[" + index + "].naGeneCount";
                        } else {
                            throw log.throwing(new IllegalArgumentException("Unrecognized header: " 
                                    + header[i] + " for file type: " + 
                                    fileType.getStringRepresentation()));
                        }
                        assert(fieldMapping[i] != null);
                        break;
                    }
                }
            } else {
                // *** Attributes specific to complete file ***
                switch (header[i]) {
                    case SPECIES_LATIN_NAME_COLUMN_NAME:
                        fieldMapping[i] = "speciesName";
                        break;
                    case GENE_ID_COLUMN_NAME:
                        fieldMapping[i] = "geneId";
                        break;
                    case GENE_NAME_COLUMN_NAME:
                        fieldMapping[i] = "geneName";
                        break;
                    case CIO_ID_COLUMN_NAME:
                        fieldMapping[i] = "cioId";
                        break;
                    case CIO_NAME_ID_COLUMN_NAME:
                        fieldMapping[i] = "cioName";
                        break;
                    case EXPRESSION_COLUMN_NAME:
                        fieldMapping[i] = "differentialExpression";
                        break;
                    case QUALITY_COLUMN_NAME:
                        fieldMapping[i] = "callQuality";
                        break;
                    case AFFYMETRIX_DATA_COLUMN_NAME:
                        fieldMapping[i] = "affymetrixData";
                        break;
                    case AFFYMETRIX_CALL_QUALITY_COLUMN_NAME:
                        fieldMapping[i] = "affymetrixQuality"; 
                        break;
                    case AFFYMETRIX_OBSERVED_DATA_COLUMN_NAME:
                        fieldMapping[i] = "affymetrixObservedData"; 
                        break;
                    case EST_DATA_COLUMN_NAME:
                        fieldMapping[i] = "estData";
                        break;
                    case EST_CALL_QUALITY_COLUMN_NAME:
                        fieldMapping[i] = "estQuality"; 
                        break;
                    case EST_OBSERVED_DATA_COLUMN_NAME:
                        fieldMapping[i] = "estObservedData"; 
                        break;
                    case INSITU_DATA_COLUMN_NAME:
                        fieldMapping[i] = "inSituData";
                        break;
                    case INSITU_CALL_QUALITY_COLUMN_NAME:
                        fieldMapping[i] = "inSituQuality"; 
                        break;
                    case INSITU_OBSERVED_DATA_COLUMN_NAME:
                        fieldMapping[i] = "inSituObservedData"; 
                        break;
                    // TODO: when relaxed in situ will be in the database, uncomment and update following lines
//                    case RELAXED_INSITU_DATA_COLUMN_NAME:
//                        fieldMapping[i] = "relaxedInSituData";
//                        break;
//                    case RELAXED_INSITU_CALL_QUALITY_COLUMN_NAME:
//                        fieldMapping[i] = "relaxedInSituQuality"; 
//                        break;
//                    case RELAXED_INSITU_OBSERVED_DATA_COLUMN_NAME:
//                        fieldMapping[i] = "relaxedInSituObservedData"; 
//                        break;
                    case RNASEQ_DATA_COLUMN_NAME:
                        fieldMapping[i] = "rnaSeqData";
                        break;
                    case RNASEQ_CALL_QUALITY_COLUMN_NAME:
                        fieldMapping[i] = "rnaSeqQuality";
                        break;
                    case RNASEQ_OBSERVED_DATA_COLUMN_NAME:
                        fieldMapping[i] = "rnaSeqObservedData";
                        break;
                }
            } 
            
            if (fieldMapping[i] == null) {
                throw log.throwing(new IllegalArgumentException("Unrecognized header: " 
                        + header[i] + " for file type: " + fileType.getStringRepresentation()));
            }
        }
        // Verify that we found all species
        if (fileType.isSimpleFileType()) {
            assert speciesFound.containsAll(speciesNamesOrderedByLength) && 
                   speciesNamesOrderedByLength.containsAll(speciesFound): 
                "Some of the provided species were not found in the header: expected: " 
                + speciesNamesOrderedByLength + " - found: " + speciesFound;
        }
        return log.exit(fieldMapping);
    }
}
