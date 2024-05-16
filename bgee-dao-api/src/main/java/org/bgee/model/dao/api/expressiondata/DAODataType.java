package org.bgee.model.dao.api.expressiondata;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.TransferObject;
import org.bgee.model.dao.api.TransferObject.EnumDAOField;

/**
 * {@code Enum} listing the data types used in Bgee:
 *
 * <ul>
 * <li>{@code AFFYMETRIX}
 * <li>{@code EST}
 * <li>{@code IN_SITU}
 * <li>{@code RNA_SEQ}
 * <li>{@code SC_RNA_SEQ}
 * </ul>
 * 
 * @author Freeric Bastian
 * @version Bgee 14 Mar. 2017
 * @since Bgee 14 Mar. 2017
 */
public enum DAODataType implements EnumDAOField {
    //The order of these Enum elements is important and is used to generate field names
    AFFYMETRIX("affymetrix", "affymetrix", "Affy", "affymetrixMeanRank", "affymetrixGlobalMeanRank",
            "affymetrixMeanRankNorm", "affymetrixGlobalMeanRankNorm", "affymetrixDistinctRankSum",
            "affymetrixGlobalDistinctRankSum", "affymetrixMaxRank", "affymetrixGlobalMaxRank", false,
            true, true),
    EST("est", "est", "Est", "estRank", "estGlobalRank", "estRankNorm", "estGlobalRankNorm",
            "estMaxRank", "estGlobalMaxRank", "estMaxRank", "estGlobalMaxRank", true, true, true),
    IN_SITU("in situ", "inSitu", "InSitu", "inSituRank", "inSituGlobalRank", "inSituRankNorm",
            "inSituGlobalRankNorm", "inSituMaxRank", "inSituGlobalMaxRank",
            "inSituMaxRank", "inSituGlobalMaxRank", true, false, true),
    RNA_SEQ("rna-seq", "rnaSeq", "RnaSeq", "rnaSeqMeanRank", "rnaSeqGlobalMeanRank",
            "rnaSeqMeanRankNorm", "rnaSeqGlobalMeanRankNorm", "rnaSeqDistinctRankSum",
            "rnaSeqGlobalDistinctRankSum", "rnaSeqMaxRank", "rnaSeqGlobalMaxRank", false, true, false),
    SC_RNA_SEQ("single-cell RNA-Seq", "scRnaSeqFullLength", "ScRnaSeqFL",
            "scRnaSeqFullLengthMeanRank", "scRnaSeqFullLengthGlobalMeanRank",
            "scRnaSeqFullLengthMeanRankNorm", "scRnaSeqFullLengthGlobalMeanRankNorm",
            "scRnaSeqFullLengthDistinctRankSum", "scRnaSeqFullLengthGlobalDistinctRankSum",
            "scRnaSeqFullLengthMaxRank", "scRnaSeqFullLengthGlobalMaxRank", false, true, false);

    private final static Logger log = LogManager.getLogger(DAODataType.class.getName());

    public static final List<EnumSet<DAODataType>> ALL_COMBINATIONS =
            getAllPossibleDAODataTypeCombinations();

    /**
     * Convert the {@code String} representation of a data type into a {@code DataType}.
     * Operation performed by calling {@link TransferObject#convert(Class, String)} 
     * with {@code DataType} as the {@code Class} argument, and {@code representation} as 
     * the {@code String} argument.
     * 
     * @param representation    A {@code String} representing a data type.
     * @return                  The {@code DataType} corresponding to {@code representation}.
     * @throws IllegalArgumentException If {@code representation} does not correspond 
     *                                  to any {@code DataType}.
     */
    public static final DAODataType convertToDataType(String representation) {
        log.traceEntry("{}", representation);
        return log.traceExit(TransferObject.convert(DAODataType.class, representation));
    }

    public static class DAODataTypeEnumSetComparator implements Comparator<EnumSet<DAODataType>> {
        @Override
        public int compare(EnumSet<DAODataType> e1, EnumSet<DAODataType> e2) {
            log.traceEntry("{}, {}", e1, e2);
            return log.traceExit(DAO.compareEnumSets(e1, e2, DAODataType.class));
        }
    }

    private static final List<EnumSet<DAODataType>> getAllPossibleDAODataTypeCombinations() {
        log.traceEntry();
        return log.traceExit(DAO.getAllPossibleEnumCombinations(DAODataType.class,
                EnumSet.allOf(DAODataType.class)));
    }

    /**
     * See {@link #getStringRepresentation()}
     */
    private final String stringRepresentation;
    /**
     * See {@link #getFieldNamePrefix()}
     */
    private final String fieldNamePrefix;
    /**
     * See {@link #getFieldNamePart()}
     */
    private final String fieldNamePart;
    private final String rankFieldName;
    private final String globalRankFieldName;
    private final String rankNormFieldName;
    private final String globalRankNormFieldName;
    private final String rankWeightFieldName;
    private final String globalRankWeightFieldName;
    private final String condMaxRankFieldName;
    private final String condGlobalMaxRankFieldName;
    private final boolean rankWeightRelatedToCondition;
    private final boolean assayRelatedToCondition;
    private final boolean alwaysPropagated;

    /**
     * Constructor providing the {@code String} representation of this {@code DataType}.
     * 
     * @param stringRepresentation  A {@code String} corresponding to this {@code DataType}.
     * @param fieldNamePrefix       A {@code String} that is the prefix of fields related to this {@code DataType}.
     * @param fieldNamePart         A {@code String} that is the substring used in field names
     *                              related to this {@code DataType} when not starting the field name.
     */
    private DAODataType(String stringRepresentation, String fieldNamePrefix, String fieldNamePart,
            String rankFieldName, String globalRankFieldName, String rankNormFieldName,
            String globalRankNormFieldName, String rankWeightFieldName,
            String globalRankWeightFieldName, String condMaxRankFieldName,
            String condGlobalMaxRankFieldName, boolean rankWeightRelatedToCondition,
            boolean assayRelatedToCondition, boolean alwaysPropagated) {
        this.stringRepresentation = stringRepresentation;
        this.fieldNamePrefix = fieldNamePrefix;
        this.fieldNamePart = fieldNamePart;
        this.rankFieldName = rankFieldName;
        this.globalRankFieldName = globalRankFieldName;
        this.rankNormFieldName = rankNormFieldName;
        this.globalRankNormFieldName = globalRankNormFieldName;
        this.rankWeightFieldName = rankWeightFieldName;
        this.globalRankWeightFieldName = globalRankWeightFieldName;
        this.condMaxRankFieldName = condMaxRankFieldName;
        this.condGlobalMaxRankFieldName = condGlobalMaxRankFieldName;
        this.rankWeightRelatedToCondition = rankWeightRelatedToCondition;
        this.assayRelatedToCondition = assayRelatedToCondition;
        this.alwaysPropagated = alwaysPropagated;
    }
    @Override
    public String getStringRepresentation() {
        return this.stringRepresentation;
    }
    @Override
    public String toString() {
        return this.getStringRepresentation();
    }
    /**
     * @return  A {@code String} that is the prefix of fields related to this {@code DataType}.
     */
    public String getFieldNamePrefix() {
        return this.fieldNamePrefix;
    }
    /**
     * @return  A {@code String} that is the substring used in field names
     *          related to this {@code DataType} when not starting the field name.
     */
    public String getFieldNamePart() {
        return this.fieldNamePart;
    }

    public String getRankFieldName(boolean globalRank) {
        if (!globalRank) {
            return rankFieldName;
        }
        return globalRankFieldName;
    }

    public String getRankNormFieldName(boolean globalRank) {
        if (!globalRank) {
            return rankNormFieldName;
        }
        return globalRankNormFieldName;
    }

    public String getRankWeightFieldName(boolean globalRank) {
        if (!globalRank) {
            return rankWeightFieldName;
        }
        return globalRankWeightFieldName;
    }

    public String getCondMaxRankFieldName(boolean globalRank) {
        if (!globalRank) {
            return condMaxRankFieldName;
        }
        return condGlobalMaxRankFieldName;
    }

    public boolean isRankWeightRelatedToCondition() {
        return rankWeightRelatedToCondition;
    }

    public boolean isAssayRelatedToCondition() {
        return assayRelatedToCondition;
    }
    public boolean isAlwaysPropagated() {
        return alwaysPropagated;
    }

}
