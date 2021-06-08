package org.bgee.pipeline.expression.downloadfile;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.expressiondata.CallDAO.CallTO.DataState;
import org.bgee.model.dao.api.expressiondata.DiffExpressionCallDAO.DiffExpressionCallTO.ComparisonFactor;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.model.expressiondata.baseelements.CallType.Expression;
import org.bgee.model.expressiondata.baseelements.SummaryCallType.ExpressionSummary;
import org.bgee.model.expressiondata.baseelements.SummaryQuality;
import org.bgee.model.file.DownloadFile.CategoryEnum;
import org.bgee.pipeline.MySQLDAOUser;


/**
 * This abstract class provides convenient common methods that generate TSV download files 
 * from the Bgee database.
 * 
 * @author Valentine Rech de Laval
 * @author Frederic Bastian
 * @version Bgee 14 Sep. 2020
 * @since Bgee 13
 */
public abstract class GenerateDownloadFile extends MySQLDAOUser {
    
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
    public final static String ANAT_ENTITY_ID_COLUMN_NAME = "Anatomical entity ID";
    /**
     * A {@code String} that is the name of the column containing anatomical entity names, 
     * in the download file.
     */
    public final static String ANAT_ENTITY_NAME_COLUMN_NAME = "Anatomical entity name";
    
    /**
     * A {@code String} that is the name of the column containing cell type IDs, 
     * in the download file.
     */
    public final static String CELL_TYPE_ID_COLUMN_NAME = "Cell type ID";
    /**
     * A {@code String} that is the name of the column containing cell type names, 
     * in the download file.
     */
    public final static String CELL_TYPE_NAME_COLUMN_NAME = "Cell type name";
    /**
     * A {@code String} that is the name of the column containing sexes, 
     * in the download file.
     */
    public final static String SEX_COLUMN_NAME = "Sex";
    /**
     * A {@code String} that is the name of the column containing strains, 
     * in the download file.
     */
    public final static String STRAIN_COLUMN_NAME = "Strain";

    public final static String CALL_TYPE_COLUMN_NAME_SUFFIX  = " expression";
    public final static String CALL_QUALITY_COLUMN_NAME_SUFFIX  = " call quality";
    public final static String FDR_COLUMN_NAME_SUFFIX  = " FDR";
    public final static String EXPRESSION_RANK_COLUMN_NAME_SUFFIX  = " expression rank";
    public final static String EXPRESSION_SCORE_COLUMN_NAME_SUFFIX  = " expression score";
    public final static String WEIGHT_COLUMN_NAME_SUFFIX  = " weight for expression rank and score";

    public final static String OBSERVED_DATA_COLUMN_NAME_PREFIX = 
            "Including ";
    public final static String OBSERVED_DATA_COLUMN_NAME_SUFFIX = 
            " observed data";
    public final static String DESCENDANT_OBSERVATION_COUNT_PREFIX = 
            "Descendant observation count ";
    public final static String SELF_OBSERVATION_COUNT_PREFIX = "Self observation count ";
    
    protected final static String AFFYMETRIX_DATATYPE_NAME = "Affymetrix"; 

    /**
     * A {@code String} that is the name of the column containing expression, no-expression or
     * differential expression found with Affymetrix experiment, in the download file.
     */
    public final static String AFFYMETRIX_DATA_COLUMN_NAME = 
            AFFYMETRIX_DATATYPE_NAME + CALL_TYPE_COLUMN_NAME_SUFFIX;
    /**
     * A {@code String} that is the name of the column containing call quality found with
     * Affymetrix experiment, in the download file.
     */
    public final static String AFFYMETRIX_QUAL_COLUMN_NAME = 
            AFFYMETRIX_DATATYPE_NAME + CALL_QUALITY_COLUMN_NAME_SUFFIX;
    
    /**
     * A {@code String} that is the name of the column containing FDR pvalue found with
     * Affymetrix experiment, in the download file.
     */
    public final static String AFFYMETRIX_FDR_COLUMN_NAME = 
            AFFYMETRIX_DATATYPE_NAME + FDR_COLUMN_NAME_SUFFIX;
    /**
     * A {@code String} that is the name of the column containing if an Affymetrix experiment 
     * is observed, in the download file.
     */
    public final static String AFFYMETRIX_OBSERVED_DATA_COLUMN_NAME = 
            OBSERVED_DATA_COLUMN_NAME_PREFIX + AFFYMETRIX_DATATYPE_NAME + OBSERVED_DATA_COLUMN_NAME_SUFFIX;
    /**
     * A {@code String} that is the name of the column containing number of pvalues coming from 
     * self observed Affymetrix data, in the download file.
     */
    public final static String AFFYMETRIX_SELF_OBSERVATION_COUNT_COLUMN_NAME = 
            SELF_OBSERVATION_COUNT_PREFIX + AFFYMETRIX_DATATYPE_NAME;
    /**
     * A {@code String} that is the name of the column containing number of pvalues coming from 
     * descendant observed Affymetrix data, in the download file.
     */
    public final static String AFFYMETRIX_DESCENDANT_OBSERVATION_COUNT_COLUMN_NAME = 
            DESCENDANT_OBSERVATION_COUNT_PREFIX + AFFYMETRIX_DATATYPE_NAME;
    /**
     * A {@code String} that is the name of the column containing
     * the expression rank from Affymetrix data in the download file.
     */
    public final static String AFFYMETRIX_EXPRESSION_RANK_COLUMN_NAME = AFFYMETRIX_DATATYPE_NAME
             + EXPRESSION_RANK_COLUMN_NAME_SUFFIX;
    /**
     * A {@code String} that is the name of the column containing
     * the expression score from Affymetrix data in the download file.
     */
    public final static String AFFYMETRIX_EXPRESSION_SCORE_COLUMN_NAME = AFFYMETRIX_DATATYPE_NAME
             + EXPRESSION_SCORE_COLUMN_NAME_SUFFIX;
    /**
     * A {@code String} that is the name of the column containing in the download file
     * the weight for Affymetrix data when computing mean rank/score.
     */
    public final static String AFFYMETRIX_WEIGHT_COLUMN_NAME = AFFYMETRIX_DATATYPE_NAME
             + WEIGHT_COLUMN_NAME_SUFFIX;
    
    protected final static String EST_DATATYPE_NAME = "EST"; 
    
    /**
     * A {@code String} that is the name of the column containing expression/no-expression found
     * with EST experiment, in the download file.
     */
    public final static String EST_DATA_COLUMN_NAME = EST_DATATYPE_NAME + CALL_TYPE_COLUMN_NAME_SUFFIX;
    /**
     * A {@code String} that is the name of the column containing call quality found with
     * EST experiment, in the download file.
     */
    public final static String EST_QUAL_COLUMN_NAME = EST_DATATYPE_NAME + CALL_QUALITY_COLUMN_NAME_SUFFIX;
    /**
     * A {@code String} that is the name of the column containing FDR pvalue found with
     * EST experiment, in the download file.
     */
    public final static String EST_FDR_COLUMN_NAME = EST_DATATYPE_NAME + FDR_COLUMN_NAME_SUFFIX;
    /**
     * A {@code String} that is the name of the column containing if an EST experiment is observed, 
     * in the download file.
     */
    public final static String EST_OBSERVED_DATA_COLUMN_NAME =
            OBSERVED_DATA_COLUMN_NAME_PREFIX + EST_DATATYPE_NAME + OBSERVED_DATA_COLUMN_NAME_SUFFIX;
    /**
     * A {@code String} that is the name of the column containing number of pvalues coming from 
     * self observed EST data, in the download file.
     */
    public final static String EST_SELF_OBSERVATION_COUNT_COLUMN_NAME = 
            SELF_OBSERVATION_COUNT_PREFIX + EST_DATATYPE_NAME;
    /**
     * A {@code String} that is the name of the column containing number of pvalues coming from 
     * descendant observed EST data, in the download file.
     */
    public final static String EST_DESCENDANT_OBSERVATION_COUNT_COLUMN_NAME = 
            DESCENDANT_OBSERVATION_COUNT_PREFIX + EST_DATATYPE_NAME;
    /**
     * A {@code String} that is the name of the column containing
     * the expression rank from EST data in the download file.
     */
    public final static String EST_EXPRESSION_RANK_COLUMN_NAME = EST_DATATYPE_NAME
             + EXPRESSION_RANK_COLUMN_NAME_SUFFIX;
    /**
     * A {@code String} that is the name of the column containing
     * the expression score from EST data in the download file.
     */
    public final static String EST_EXPRESSION_SCORE_COLUMN_NAME = EST_DATATYPE_NAME
             + EXPRESSION_SCORE_COLUMN_NAME_SUFFIX;
    /**
     * A {@code String} that is the name of the column containing in the download file
     * the weight for EST data when computing mean rank/score.
     */
    public final static String EST_WEIGHT_COLUMN_NAME = EST_DATATYPE_NAME
             + WEIGHT_COLUMN_NAME_SUFFIX;
    
    protected final static String IN_SITU_DATATYPE_NAME = "In situ hybridization"; 

    /**
     * A {@code String} that is the name of the column containing expression/no-expression
     * found with <em>in situ</em> experiment, in the download file.
     */
    public final static String IN_SITU_DATA_COLUMN_NAME = 
            IN_SITU_DATATYPE_NAME + CALL_TYPE_COLUMN_NAME_SUFFIX;
    /**
     * A {@code String} that is the name of the column containing call quality found with
     * in situ hybridization experiment, in the download file.
     */
    public final static String IN_SITU_QUAL_COLUMN_NAME = 
            IN_SITU_DATATYPE_NAME + CALL_QUALITY_COLUMN_NAME_SUFFIX;
    /**
     * A {@code String} that is the name of the column containing FDR pvalue found with
     * RNA-Seq experiment, in the download file.
     */
    public final static String IN_SITU_FDR_COLUMN_NAME = 
            IN_SITU_DATATYPE_NAME + FDR_COLUMN_NAME_SUFFIX;
    /**
     * A {@code String} that is the name of the column containing if an <em>in situ</em> experiment 
     * is observed, in the download file.
     */
    public final static String IN_SITU_OBSERVED_DATA_COLUMN_NAME =
            OBSERVED_DATA_COLUMN_NAME_PREFIX + IN_SITU_DATATYPE_NAME + OBSERVED_DATA_COLUMN_NAME_SUFFIX;
    /**
     * A {@code String} that is the name of the column containing number of pvalues coming from 
     * self observed IN_SITU data, in the download file.
     */
    public final static String IN_SITU_SELF_OBSERVATION_COUNT_COLUMN_NAME = 
            SELF_OBSERVATION_COUNT_PREFIX + IN_SITU_DATATYPE_NAME;
    /**
     * A {@code String} that is the name of the column containing number of pvalues coming from 
     * descendant observed IN_SITU data, in the download file.
     */
    public final static String IN_SITU_DESCENDANT_OBSERVATION_COUNT_COLUMN_NAME = 
            DESCENDANT_OBSERVATION_COUNT_PREFIX + IN_SITU_DATATYPE_NAME;
    /**
     * A {@code String} that is the name of the column containing
     * the expression rank from in situ hybridization data in the download file.
     */
    public final static String IN_SITU_EXPRESSION_RANK_COLUMN_NAME = IN_SITU_DATATYPE_NAME
             + EXPRESSION_RANK_COLUMN_NAME_SUFFIX;
    /**
     * A {@code String} that is the name of the column containing
     * the expression score from in situ hybridization data in the download file.
     */
    public final static String IN_SITU_EXPRESSION_SCORE_COLUMN_NAME = IN_SITU_DATATYPE_NAME
             + EXPRESSION_SCORE_COLUMN_NAME_SUFFIX;
    /**
     * A {@code String} that is the name of the column containing in the download file
     * the weight for in situ hybridization data when computing mean rank/score.
     */
    public final static String IN_SITU_WEIGHT_COLUMN_NAME = IN_SITU_DATATYPE_NAME
             + WEIGHT_COLUMN_NAME_SUFFIX;

    protected final static String RNASEQ_DATATYPE_NAME = "RNA-Seq"; 
    
    /**
     * A {@code String} that is the name of the column containing expression, no-expression or
     * differential expression found with RNA-Seq experiment, in the download file.
     */
    public final static String RNASEQ_DATA_COLUMN_NAME = RNASEQ_DATATYPE_NAME + CALL_TYPE_COLUMN_NAME_SUFFIX;
    /**
     * A {@code String} that is the name of the column containing call quality found with
     * RNA-Seq experiment, in the download file.
     */
    public final static String RNASEQ_QUAL_COLUMN_NAME = RNASEQ_DATATYPE_NAME + CALL_QUALITY_COLUMN_NAME_SUFFIX;
    /**
     * A {@code String} that is the name of the column containing FDR pvalue found with
     * RNA-Seq experiment, in the download file.
     */
    public final static String RNASEQ_FDR_COLUMN_NAME = RNASEQ_DATATYPE_NAME + FDR_COLUMN_NAME_SUFFIX;
    /**
     * A {@code String} that is the name of the column containing if a RNA-Seq experiment 
     * is observed, in the download file.
     */
    public final static String RNASEQ_OBSERVED_DATA_COLUMN_NAME =
            OBSERVED_DATA_COLUMN_NAME_PREFIX + RNASEQ_DATATYPE_NAME + OBSERVED_DATA_COLUMN_NAME_SUFFIX;
    /**
     * A {@code String} that is the name of the column containing number of pvalues coming from 
     * self observed RNA-Seq data, in the download file.
     */
    public final static String RNASEQ_SELF_OBSERVATION_COUNT_COLUMN_NAME = 
            SELF_OBSERVATION_COUNT_PREFIX + RNASEQ_DATATYPE_NAME;
    /**
     * A {@code String} that is the name of the column containing number of pvalues coming from 
     * descendant observed RNA-Seq data, in the download file.
     */
    public final static String RNASEQ_DESCENDANT_OBSERVATION_COUNT_COLUMN_NAME = 
            DESCENDANT_OBSERVATION_COUNT_PREFIX + RNASEQ_DATATYPE_NAME;
    /**
     * A {@code String} that is the name of the column containing
     * the expression rank from RNA-Seq data in the download file.
     */
    public final static String RNASEQ_EXPRESSION_RANK_COLUMN_NAME = RNASEQ_DATATYPE_NAME
             + EXPRESSION_RANK_COLUMN_NAME_SUFFIX;
    /**
     * A {@code String} that is the name of the column containing
     * the expression score from RNA-Seq data in the download file.
     */
    public final static String RNASEQ_EXPRESSION_SCORE_COLUMN_NAME = RNASEQ_DATATYPE_NAME
             + EXPRESSION_SCORE_COLUMN_NAME_SUFFIX;
    /**
     * A {@code String} that is the name of the column containing in the download file
     * the weight for RNA-Seq data when computing mean rank/score.
     */
    public final static String RNASEQ_WEIGHT_COLUMN_NAME = RNASEQ_DATATYPE_NAME
             + WEIGHT_COLUMN_NAME_SUFFIX;
    
    
    protected final static String FULL_LENGTH_DATATYPE_NAME = "Full Length Single cell RNA-Seq"; 
    
    /**
     * A {@code String} that is the name of the column containing expression, no-expression or
     * differential expression found with RNA-Seq experiment, in the download file.
     */
    public final static String FULL_LENGTH_DATA_COLUMN_NAME = FULL_LENGTH_DATATYPE_NAME
            + CALL_TYPE_COLUMN_NAME_SUFFIX;
    /**
     * A {@code String} that is the name of the column containing call quality found with
     * RNA-Seq experiment, in the download file.
     */
    public final static String FULL_LENGTH_QUAL_COLUMN_NAME = FULL_LENGTH_DATATYPE_NAME 
            + CALL_QUALITY_COLUMN_NAME_SUFFIX;
    /**
     * A {@code String} that is the name of the column containing FDR pvalue found with
     * RNA-Seq experiment, in the download file.
     */
    public final static String FULL_LENGTH_FDR_COLUMN_NAME = FULL_LENGTH_DATATYPE_NAME
            + FDR_COLUMN_NAME_SUFFIX;
    /**
     * A {@code String} that is the name of the column containing if a RNA-Seq experiment 
     * is observed, in the download file.
     */
    public final static String FULL_LENGTH_OBSERVED_DATA_COLUMN_NAME =
            OBSERVED_DATA_COLUMN_NAME_PREFIX + FULL_LENGTH_DATATYPE_NAME 
            + OBSERVED_DATA_COLUMN_NAME_SUFFIX;
    /**
     * A {@code String} that is the name of the column containing number of pvalues coming from 
     * self observed RNA-Seq data, in the download file.
     */
    public final static String FULL_LENGTH_SELF_OBSERVATION_COUNT_COLUMN_NAME = 
            SELF_OBSERVATION_COUNT_PREFIX  + FULL_LENGTH_DATATYPE_NAME;
    /**
     * A {@code String} that is the name of the column containing number of pvalues coming from 
     * descendant observed RNA-Seq data, in the download file.
     */
    public final static String FULL_LENGTH_DESCENDANT_OBSERVATION_COUNT_COLUMN_NAME = 
            DESCENDANT_OBSERVATION_COUNT_PREFIX + FULL_LENGTH_DATATYPE_NAME;
    /**
     * A {@code String} that is the name of the column containing
     * the expression rank from RNA-Seq data in the download file.
     */
    public final static String FULL_LENGTH_EXPRESSION_RANK_COLUMN_NAME = 
            FULL_LENGTH_DATATYPE_NAME + EXPRESSION_RANK_COLUMN_NAME_SUFFIX;
    /**
     * A {@code String} that is the name of the column containing
     * the expression score from RNA-Seq data in the download file.
     */
    public final static String FULL_LENGTH_EXPRESSION_SCORE_COLUMN_NAME = 
            FULL_LENGTH_DATATYPE_NAME + EXPRESSION_SCORE_COLUMN_NAME_SUFFIX;
    /**
     * A {@code String} that is the name of the column containing in the download file
     * the weight for RNA-Seq data when computing mean rank/score.
     */
    public final static String FULL_LENGTH_WEIGHT_COLUMN_NAME = 
            FULL_LENGTH_DATATYPE_NAME + WEIGHT_COLUMN_NAME_SUFFIX;

    /**
     * A {@code String} that is the name of the column containing whether the call include
     * observed data or not.
     */
    public final static String INCLUDING_OBSERVED_DATA_COLUMN_NAME = "Including observed data";
    
    /**
     * A {@code String} that is the name of the column containing number of pvalues coming from 
     * self observed data, in the download file.
     */
    public final static String SELF_OBSERVATION_COUNT_COLUMN_NAME = 
            "Self observation count";
    /**
     * A {@code String} that is the name of the column containing number of pvalues coming from 
     * descendant observed data, in the download file.
     */
    public final static String DESCENDANT_OBSERVATION_COUNT_COLUMN_NAME = 
            "Descendant observation count";
    
    /**
     * A {@code String} that is the name of the column containing merged
     * expression/no-expression from different data types, in the download file.
     */
    public final static String EXPRESSION_COLUMN_NAME = "Expression";
    /**
     * A {@code String} that is the name of the column containing the merged quality of the call,
     * in the download file.
     */
    public final static String QUALITY_COLUMN_NAME = "Call quality";
    /**
     * A {@code String} that is the name of the column containing the FDR of the call,
     * in the download file.
     */
    public final static String FDR_COLUMN_NAME = "FDR";
    /**
     * A {@code String} that is the name of the column containing the expression score,
     * in the download file.
     */
    public final static String EXPRESSION_SCORE_COLUMN_NAME = "Expression score";
    /**
     * A {@code String} that is the name of the column containing the expression rank,
     * in the download file.
     */
    public final static String EXPRESSION_RANK_COLUMN_NAME = "Expression rank";
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
     * A {@code String} that is the bronze quality data text, in the download file.
     */
    public final static String BRONZE_QUALITY_TEXT = "bronze quality";
    /**
     * A {@code String} that is the silver quality data text, in the download file.
     */
    public final static String SILVER_QUALITY_TEXT = "silver quality";
    /**
     * A {@code String} that is the gold quality data text, in the download file.
     */
    public final static String GOLD_QUALITY_TEXT = "gold quality";
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
        
        return log.traceExit(fileTypes);
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
            return log.traceExit(HIGH_QUALITY_TEXT);
        }
        if (DataState.LOWQUALITY.equals(dataState)) {
            return log.traceExit(LOW_QUALITY_TEXT);
        }
        return log.traceExit(NA_VALUE);
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
            return log.traceExit(DataState.HIGHQUALITY);
        }
        if (string.equals(LOW_QUALITY_TEXT)) {
            return log.traceExit(DataState.LOWQUALITY);
        }
        return log.traceExit(DataState.NODATA);
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
            return log.traceExit(PRESENT_TEXT);
        case NOT_EXPRESSED:
            return log.traceExit(ABSENT_TEXT);
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
            return log.traceExit(NO_DATA_VALUE);
        }
        switch (expr) {
        case EXPRESSED:
            return log.traceExit(PRESENT_TEXT);
        case NOT_EXPRESSED:
            return log.traceExit(ABSENT_TEXT);
        default:
            throw new IllegalArgumentException("Unrecognized Expression: " + expr);
        }
    }

    /**
     * Convert a {@code org.bgee.model.expressiondata.baseelements.SummaryQuality SummaryQuality}
     * into a {@code String}.
     * <p>  
     * This is because its method {@code getStringRepresentation} is not available for display in files.
     * 
     * @param qual  A {@code SummaryQuality} to be converted.
     * @return      The {@code String} corresponding to {@code qual}, to be used in files
     */
    protected static String convertSummaryQualityToString(SummaryQuality qual) {
        log.entry(qual);
        if (qual == null) {
            return log.traceExit(NA_VALUE);
        }
        switch (qual) {
        case GOLD:
            return log.traceExit(GOLD_QUALITY_TEXT);
        case SILVER:
            return log.traceExit(SILVER_QUALITY_TEXT);
        case BRONZE:
            return log.traceExit(BRONZE_QUALITY_TEXT);
        default:
            throw new IllegalArgumentException("Unrecognized SummaryQuality: " + qual);
        }
    }

    /**
     * Convert a {@code String} into a {@code org.bgee.model.expressiondata.baseelements.SummaryQuality SummaryQuality}.
     * 
     * @param string    A {@code String} to be converted.
     * @return          A {@code SummaryQuality} corresponding to {@code string}, to be used in files.
     */
    protected static SummaryQuality convertStringToSummaryQuality(String string) {
        log.entry(string);
        switch (string) {
            case GOLD_QUALITY_TEXT:
                return log.traceExit(SummaryQuality.GOLD);
            case SILVER_QUALITY_TEXT:
                return log.traceExit(SummaryQuality.SILVER);
            case BRONZE_QUALITY_TEXT:
                return log.traceExit(SummaryQuality.BRONZE);
        }
        throw new IllegalArgumentException("Unrecognized summary quality text: " + string);
    }

    /**
     * Convert a {@code Boolean} defining whether data are observed into a {@code String}.
     * 
     * @param qual  A {@code DataQuality} to be converted.
     * @return      The {@code String} corresponding to {@code qual}, to be used in files
     */
    protected static String convertObservedDataToString(Boolean includingObservedData) {
        log.entry(includingObservedData);
        if (Boolean.TRUE.equals(includingObservedData)) {
            return log.traceExit(ObservedData.OBSERVED.getStringRepresentation());
        }
        return log.traceExit(ObservedData.NOT_OBSERVED.getStringRepresentation());
    }

    /**
     * A {@code List} of {@code String}s that are the IDs of species allowing 
     * to filter the calls to retrieve.
     */
    protected List<Integer> speciesIds;
    
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
    public GenerateDownloadFile(List<Integer> speciesIds, Set<? extends FileType> fileTypes, 
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
    public GenerateDownloadFile(MySQLDAOManager manager, List<Integer> speciesIds, 
            Set<? extends FileType> fileTypes, String directory) throws IllegalArgumentException {
        //FIXME: restore if CallUser is reused?
        // XXX: to remove? restore call to super() because we need it to generate files 
        // and we have no time to remove properly the class
        super(manager);
        if (StringUtils.isBlank(directory)) {
            throw log.throwing(new IllegalArgumentException("A directory must be provided"));
        }
        this.speciesIds = Collections.unmodifiableList(speciesIds == null?
                new ArrayList<>(): new ArrayList<>(speciesIds));
        this.fileTypes = Collections.unmodifiableSet(fileTypes == null?
                new HashSet<>(): new HashSet<>(fileTypes));
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
     * @param sex               A {@code String} that is the name of the sex.
     * @param strain            A {@code String} that is the name of the strain.
     */
    protected void addIdsAndNames(Map<String, Object> row, Integer geneId, String geneName, 
            String anatEntityId, String anatEntityName, String stageId, String stageName, 
            String sex, String strain, String fdr) {
        log.entry(row, geneId, geneName, anatEntityId, anatEntityName, stageId, stageName,
                sex, strain);
        
        if (geneId == null) {
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
        if (StringUtils.isBlank(sex)) {
            throw log.throwing(new IllegalArgumentException("No name provided for sex"));
        }
        if (StringUtils.isBlank(strain)) {
            throw log.throwing(new IllegalArgumentException("No name provided for strain"));
        }
        row.put(GENE_ID_COLUMN_NAME, geneId);
        row.put(GENE_NAME_COLUMN_NAME, geneName);
        row.put(ANAT_ENTITY_ID_COLUMN_NAME, anatEntityId);
        row.put(ANAT_ENTITY_NAME_COLUMN_NAME, anatEntityName);
        row.put(STAGE_ID_COLUMN_NAME, stageId);
        row.put(STAGE_NAME_COLUMN_NAME, stageName);
        row.put(SEX_COLUMN_NAME, sex);
        row.put(STRAIN_COLUMN_NAME, strain);

        log.traceExit();
    }

    /**
     * Format the provided {@code string} replacing whitespace by "_".
     *
     * @param word  A {@code String} that is the word to be used. 
     * @return      A {@code String} that is the modified word where whitespace are replaced by "_".
     */
    protected String formatString(String word) {
        log.entry(word);
        return log.traceExit(word.replaceAll(" ", "_"));
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
        
        // FIXME delete tmp file if contains only header
        for (String fileName: generatedFileNames.values()) {
            //if temporary file exists, rename it.
            File tmpFile = new File(this.directory, fileName + tmpExtension);
            File file = new File(this.directory, fileName);
            if (tmpFile.exists()) {
                tmpFile.renameTo(file);
            }
        }
        
        log.traceExit();
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
        
        log.traceExit();
    }
}