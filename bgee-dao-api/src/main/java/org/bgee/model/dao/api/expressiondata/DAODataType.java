package org.bgee.model.dao.api.expressiondata;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
 * <li>{@code FULL_LENGTH}
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
            "affymetrixGlobalDistinctRankSum", false),
    EST("est", "est", "Est", "estRank", "estGlobalRank", "estRankNorm", "estGlobalRankNorm",
            "estMaxRank", "estGlobalMaxRank", true),
    IN_SITU("in situ", "inSitu", "InSitu", "inSituRank", "inSituGlobalRank", "inSituRankNorm",
            "inSituGlobalRankNorm", "inSituMaxRank", "inSituGlobalMaxRank", true),
    RNA_SEQ("rna-seq", "rnaSeq", "RnaSeq", "rnaSeqMeanRank", "rnaSeqGlobalMeanRank",
            "rnaSeqMeanRankNorm", "rnaSeqGlobalMeanRankNorm", "rnaSeqDistinctRankSum",
            "rnaSeqGlobalDistinctRankSum", false),
    FULL_LENGTH("full length single cell RNA-Seq", "scRnaSeqFullLength", "ScRnaSeqFL",
            "scRnaSeqFullLengthMeanRank", "scRnaSeqFullLengthGlobalMeanRank",
            "scRnaSeqFullLengthMeanRankNorm", "scRnaSeqFullLengthGlobalMeanRankNorm",
            "scRnaSeqFullLengthDistinctRankSum", "scRnaSeqFullLengthGlobalDistinctRankSum", false);

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

    private static final List<EnumSet<DAODataType>> getAllPossibleDAODataTypeCombinations() {
        log.traceEntry();
        Collection<DAODataType> allDataTypes = EnumSet.allOf(DAODataType.class);
        List<EnumSet<DAODataType>> combinations = new ArrayList<>();
        DAODataType[] dataTypeArr = allDataTypes.toArray(new DAODataType[allDataTypes.size()]);
        final int n = dataTypeArr.length;
        
        for (int i = 0; i < Math.pow(2, n); i++) {
            String bin = Integer.toBinaryString(i);
            while (bin.length() < n) {
                bin = "0" + bin;
            }
            EnumSet<DAODataType> combination = EnumSet.noneOf(DAODataType.class);
            char[] chars = bin.toCharArray();
            for (int j = 0; j < n; j++) {
                if (chars[j] == '1') {
                    combination.add(dataTypeArr[j]);
                }
            }
            //We don't want the combination where no data type is considered
            if (!combination.isEmpty()) {
                combinations.add(combination);
            }
        }
        return combinations;
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
    private final boolean rankWeightRelatedToCondition;

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
            String globalRankWeightFieldName, boolean rankWeightRelatedToCondition) {
        this.stringRepresentation = stringRepresentation;
        this.fieldNamePrefix = fieldNamePrefix;
        this.fieldNamePart = fieldNamePart;
        this.rankFieldName = rankFieldName;
        this.globalRankFieldName = globalRankFieldName;
        this.rankNormFieldName = rankNormFieldName;
        this.globalRankNormFieldName = globalRankNormFieldName;
        this.rankWeightFieldName = rankWeightFieldName;
        this.globalRankWeightFieldName = globalRankWeightFieldName;
        this.rankWeightRelatedToCondition = rankWeightRelatedToCondition;
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

    public boolean isRankWeightRelatedToCondition() {
        return rankWeightRelatedToCondition;
    }
}
