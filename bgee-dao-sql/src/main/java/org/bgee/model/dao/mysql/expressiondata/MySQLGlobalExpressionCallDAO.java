package org.bgee.model.dao.mysql.expressiondata;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.expressiondata.CallDAOFilter;
import org.bgee.model.dao.api.expressiondata.CallDataDAOFilter;
import org.bgee.model.dao.api.expressiondata.ConditionDAO;
import org.bgee.model.dao.api.expressiondata.DAOConditionFilter;
import org.bgee.model.dao.api.expressiondata.DAODataType;
import org.bgee.model.dao.api.expressiondata.DAOExperimentCount;
import org.bgee.model.dao.api.expressiondata.DAOExperimentCountFilter;
import org.bgee.model.dao.api.expressiondata.DAOPropagationState;
import org.bgee.model.dao.api.expressiondata.GlobalExpressionCallDAO;
import org.bgee.model.dao.api.expressiondata.DAOExperimentCount.CallType;
import org.bgee.model.dao.mysql.MySQLDAO;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.model.dao.mysql.connector.MySQLDAOResultSet;
import org.bgee.model.dao.mysql.gene.MySQLGeneDAO;

/**
 * A {@code GlobalExpressionCallDAO} for MySQL. 
 * 
 * @author  Frederic Bastian
 * @author  Valentine Rech de Laval
 * @version Bgee 14, May 2017
 * @see org.bgee.model.dao.api.expressiondata.GlobalExpressionCallDAO.GlobalExpressionCallTO
 * @see org.bgee.model.dao.api.expressiondata.GlobalExpressionCallDAO.GlobalExpressionToRawExpressionTO
 * @since   Bgee 14, Feb. 2017
 */
//XXX: completely review git diff 03ea09b6b325dd8f9ec9bb4611678fdaf764e9ab..6e5e4219fd40242abf597878e965cce1b3bedb45
public class MySQLGlobalExpressionCallDAO extends MySQLDAO<GlobalExpressionCallDAO.Attribute> 
implements GlobalExpressionCallDAO {
    private final static Logger log = LogManager.getLogger(MySQLGlobalExpressionCallDAO.class.getName());

    private final static String GLOBAL_EXPR_ID_FIELD = "globalExpressionId";
    private final static String GLOBAL_EXPR_TABLE_NAME = "globalExpression";
    private final static String GLOBAL_MEAN_RANK_FIELD = "meanRank";
    //XXX: I think this information is already managed in another class?
    private final static int DATA_TYPE_COUNT = 4;
    //XXX: I think this information is already managed in another class?
    private final static Set<DAOPropagationState> OBSERVED_STATES = EnumSet.of(DAOPropagationState.ALL,
            DAOPropagationState.SELF, DAOPropagationState.SELF_AND_ANCESTOR, 
            DAOPropagationState.SELF_AND_DESCENDANT);
    //TODO: ADD NON_OBSERVED_STATES

    private static String generateSelectClause(Collection<GlobalExpressionCallDAO.Attribute> attrs,
            Collection<GlobalExpressionCallDAO.OrderingAttribute> orderingAttrs,
            Collection<DAODataType> dataTypes, final String globalExprTableName, final String globalCondTableName,
            String geneTableName) {
        log.entry(attrs, orderingAttrs, dataTypes, globalExprTableName, globalCondTableName, geneTableName);
        
        Set<GlobalExpressionCallDAO.Attribute> clonedAttrs = attrs == null || attrs.isEmpty()?
                EnumSet.allOf(GlobalExpressionCallDAO.Attribute.class):
                    EnumSet.copyOf(attrs);
        //fix for #173, see also the end of this method
        for (GlobalExpressionCallDAO.OrderingAttribute a: orderingAttrs) {
            switch (a) {
            case GENE_ID:
                clonedAttrs.add(GlobalExpressionCallDAO.Attribute.BGEE_GENE_ID);
                break;
            case CONDITION_ID:
                clonedAttrs.add(GlobalExpressionCallDAO.Attribute.CONDITION_ID);
                break;
            case MEAN_RANK:
                clonedAttrs.add(GlobalExpressionCallDAO.Attribute.GLOBAL_MEAN_RANK);
                break;
            default:
                //other ordering attributes do not have a corresponding select attribute
                break;
            }
        }
        Set<DAODataType> clonedDataTypes = Collections.unmodifiableSet(
                dataTypes == null || dataTypes.isEmpty()? EnumSet.allOf(DAODataType.class):
                    EnumSet.copyOf(dataTypes));

        //Ranks: 
        Map<DAODataType, String> dataTypeToNormRankSql = new HashMap<>();
        dataTypeToNormRankSql.put(DAODataType.EST, 
                globalExprTableName + ".estRankNorm ");
        dataTypeToNormRankSql.put(DAODataType.AFFYMETRIX, 
                globalExprTableName + ".affymetrixMeanRankNorm ");
        dataTypeToNormRankSql.put(DAODataType.IN_SITU, 
                globalExprTableName + ".inSituRankNorm ");
        dataTypeToNormRankSql.put(DAODataType.RNA_SEQ, 
                globalExprTableName + ".rnaSeqMeanRankNorm ");
        
        //for weighted mean computation: sum of numbers of distinct ranks for data using 
        //fractional ranking (Affy and RNA-Seq), max ranks for data using dense ranking 
        //and pooling of all samples in a condition (EST and in situ)
        Map<DAODataType, String> dataTypeToWeightSql = new HashMap<>();
        dataTypeToWeightSql.put(DAODataType.EST, 
                globalCondTableName + ".estMaxRank ");
        dataTypeToWeightSql.put(DAODataType.AFFYMETRIX, 
                globalExprTableName + ".affymetrixDistinctRankSum ");
        dataTypeToWeightSql.put(DAODataType.IN_SITU, 
                globalCondTableName + ".inSituMaxRank ");
        dataTypeToWeightSql.put(DAODataType.RNA_SEQ, 
                globalExprTableName + ".rnaSeqDistinctRankSum ");

        StringBuilder sb = new StringBuilder();
        sb.append("SELECT ");

        if (!clonedAttrs.contains(GlobalExpressionCallDAO.Attribute.ID) &&
                (!clonedAttrs.contains(GlobalExpressionCallDAO.Attribute.BGEE_GENE_ID) ||
                        !clonedAttrs.contains(GlobalExpressionCallDAO.Attribute.CONDITION_ID))) {
            sb.append("DISTINCT ");
        }
        sb.append("STRAIGHT_JOIN ");

        sb.append(clonedAttrs.stream().map(a -> {
            switch (a) {
            case ID:
                return globalExprTableName + "." + GLOBAL_EXPR_ID_FIELD;
            case BGEE_GENE_ID:
                return globalExprTableName + "." + MySQLGeneDAO.BGEE_GENE_ID;
            case CONDITION_ID:
                return globalExprTableName + "." + MySQLConditionDAO.GLOBAL_COND_ID_FIELD;
            case GLOBAL_MEAN_RANK:
                return clonedDataTypes.stream()
                        //to avoid division by 0
                        .map(dt -> dataTypeToNormRankSql.get(dt) + " IS NULL")
                        .collect(Collectors.joining(" AND ", "IF (", ", NULL, "))
                        + clonedDataTypes.stream()
                        .map(dataType -> {
                            String rankSql = dataTypeToNormRankSql.get(dataType);
                            String weightSql = dataTypeToWeightSql.get(dataType);
                            if (rankSql == null || weightSql == null) {
                                throw log.throwing(new IllegalStateException(
                                        "No rank clause associated to data type: " + dataType));
                            }
                            return "IF(" + rankSql + " IS NULL, 0, " + rankSql + " * " + weightSql + ")";
                        })
                        .collect(Collectors.joining(" + ", "((", ")"))
                        
                        + clonedDataTypes.stream()
                        .map(dataType -> {
                            String weightSql = dataTypeToWeightSql.get(dataType);
                            return "IF(" + weightSql + " IS NULL, 0, " + weightSql + ")";})
                        .collect(Collectors.joining(" + ", "/ (", "))) AS " + GLOBAL_MEAN_RANK_FIELD));

            case DATA_TYPE_OBSERVED_DATA:
                return clonedDataTypes.stream()
                        .map(dataType -> {
                            switch (dataType) {
                            case EST:
                                return "estAnatEntityPropagationState, estStagePropagationState, "
                                        + "estConditionObservedData";
                            case AFFYMETRIX:
                                return "affymetrixAnatEntityPropagationState, affymetrixStagePropagationState, "
                                + "affymetrixConditionObservedData";
                            case IN_SITU:
                                return "inSituAnatEntityPropagationState, inSituStagePropagationState, "
                                + "inSituConditionObservedData";
                            case RNA_SEQ:
                                return "rnaSeqAnatEntityPropagationState, rnaSeqStagePropagationState, "
                                + "rnaSeqConditionObservedData";
                            default:
                                throw log.throwing(new IllegalStateException(
                                        "Unsupported data type: " + dataType));
                            }
                        })
                        .collect(Collectors.joining(", "));
                
            case DATA_TYPE_EXPERIMENT_TOTAL_COUNTS:
                return clonedDataTypes.stream()
                        .map(dataType -> {
                            switch (dataType) {
                            case EST:
                                return "estLibPresentHighTotalCount, estLibPresentLowTotalCount";
                            case AFFYMETRIX:
                                return "affymetrixExpPresentHighTotalCount, affymetrixExpPresentLowTotalCount, "
                                + "affymetrixExpAbsentHighTotalCount, affymetrixExpAbsentLowTotalCount";
                            case IN_SITU:
                                return "inSituExpPresentHighTotalCount, inSituExpPresentLowTotalCount, "
                                + "inSituExpAbsentHighTotalCount, inSituExpAbsentLowTotalCount";
                            case RNA_SEQ:
                                return "rnaSeqExpPresentHighTotalCount, rnaSeqExpPresentLowTotalCount, "
                                + "rnaSeqExpAbsentHighTotalCount, rnaSeqExpAbsentLowTotalCount";
                            default:
                                throw log.throwing(new IllegalStateException(
                                        "Unsupported data type: " + dataType));
                            }
                        })
                        .collect(Collectors.joining(", "));

            case DATA_TYPE_EXPERIMENT_SELF_COUNTS:
                return clonedDataTypes.stream()
                        .map(dataType -> {
                            switch (dataType) {
                            case EST:
                                return "estLibPresentHighSelfCount, estLibPresentLowSelfCount";
                            case AFFYMETRIX:
                                return "affymetrixExpPresentHighSelfCount, affymetrixExpPresentLowSelfCount, "
                                + "affymetrixExpAbsentHighSelfCount, affymetrixExpAbsentLowSelfCount";
                            case IN_SITU:
                                return "inSituExpPresentHighSelfCount, inSituExpPresentLowSelfCount, "
                                + "inSituExpAbsentHighSelfCount, inSituExpAbsentLowSelfCount";
                            case RNA_SEQ:
                                return "rnaSeqExpPresentHighSelfCount, rnaSeqExpPresentLowSelfCount, "
                                + "rnaSeqExpAbsentHighSelfCount, rnaSeqExpAbsentLowSelfCount";
                            default:
                                throw log.throwing(new IllegalStateException(
                                        "Unsupported data type: " + dataType));
                            }
                        })
                        .collect(Collectors.joining(", "));

            case DATA_TYPE_EXPERIMENT_PROPAGATED_COUNTS:
                return clonedDataTypes.stream()
                        .map(dataType -> {
                            switch (dataType) {
                            case EST:
                                return "estLibPropagatedCount, estLibPresentHighDescendantCount, "
                                        + "estLibPresentLowDescendantCount";
                            case AFFYMETRIX:
                                return "affymetrixExpPropagatedCount, affymetrixExpPresentHighDescendantCount, "
                                        + "affymetrixExpPresentLowDescendantCount, affymetrixExpAbsentHighParentCount, "
                                        + "affymetrixExpAbsentLowParentCount";
                            case IN_SITU:
                                return "inSituExpPropagatedCount, inSituExpPresentHighDescendantCount, "
                                        + "inSituExpPresentLowDescendantCount, inSituExpAbsentHighParentCount, "
                                        + "inSituExpAbsentLowParentCount";
                            case RNA_SEQ:
                                return "rnaSeqExpPropagatedCount, rnaSeqExpPresentHighDescendantCount, "
                                        + "rnaSeqExpPresentLowDescendantCount, rnaSeqExpAbsentHighParentCount, "
                                        + "rnaSeqExpAbsentLowParentCount";
                            default:
                                throw log.throwing(new IllegalStateException(
                                        "Unsupported data type: " + dataType));
                            }
                        })
                        .collect(Collectors.joining(", "));

            case DATA_TYPE_RANK_INFO:
                return clonedDataTypes.stream()
                        .map(dataType -> {
                            switch (dataType) {
                            case EST:
                                return "estRank, estRankNorm";
                            case AFFYMETRIX:
                                return "affymetrixMeanRank, affymetrixMeanRankNorm, "
                                        + "affymetrixDistinctRankSum";
                            case IN_SITU:
                                return "inSituRank, inSituRankNorm";
                            case RNA_SEQ:
                                return "rnaSeqMeanRank, rnaSeqMeanRankNorm, "
                                        + "rnaSeqDistinctRankSum";
                            default:
                                throw log.throwing(new IllegalStateException(
                                        "Unsupported data type: " + dataType));
                            }
                        })
                        .collect(Collectors.joining(", "));
//            
//                
//            case AFFYMETRIX_EXP_PRESENT_HIGH_SELF_COUNT:
//                stmt.setInt(paramIndex, callTO.getAffymetrixExpPresentHighSelfCount());
//                paramIndex++;
//                break;
//            case AFFYMETRIX_EXP_PRESENT_LOW_SELF_COUNT:
//                stmt.setInt(paramIndex, callTO.getAffymetrixExpPresentLowSelfCount());
//                paramIndex++;
//                break;
//            case AFFYMETRIX_EXP_ABSENT_HIGH_SELF_COUNT:
//                stmt.setInt(paramIndex, callTO.getAffymetrixExpAbsentHighSelfCount());
//                paramIndex++;
//                break;
//            case AFFYMETRIX_EXP_ABSENT_LOW_SELF_COUNT:
//                stmt.setInt(paramIndex, callTO.getAffymetrixExpAbsentLowSelfCount());
//                paramIndex++;
//                break;
//            case AFFYMETRIX_EXP_PRESENT_HIGH_DESCENDANT_COUNT:
//                stmt.setInt(paramIndex, callTO.getAffymetrixExpPresentHighDescendantCount());
//                paramIndex++;
//                break;
//            case AFFYMETRIX_EXP_PRESENT_LOW_DESCENDANT_COUNT:
//                stmt.setInt(paramIndex, callTO.getAffymetrixExpPresentLowDescendantCount());
//                paramIndex++;
//                break;
//            case AFFYMETRIX_EXP_ABSENT_HIGH_PARENT_COUNT:
//                stmt.setInt(paramIndex, callTO.getAffymetrixExpAbsentHighParentCount());
//                paramIndex++;
//                break;
//            case AFFYMETRIX_EXP_ABSENT_LOW_PARENT_COUNT:
//                stmt.setInt(paramIndex, callTO.getAffymetrixExpAbsentLowParentCount());
//                paramIndex++;
//                break;
//            case AFFYMETRIX_EXP_PRESENT_HIGH_TOTAL_COUNT:
//                stmt.setInt(paramIndex, callTO.getAffymetrixExpPresentHighTotalCount());
//                paramIndex++;
//                break;
//            case AFFYMETRIX_EXP_PRESENT_LOW_TOTAL_COUNT:
//                stmt.setInt(paramIndex, callTO.getAffymetrixExpPresentLowTotalCount());
//                paramIndex++;
//                break;
//            case AFFYMETRIX_EXP_ABSENT_HIGH_TOTAL_COUNT:
//                stmt.setInt(paramIndex, callTO.getAffymetrixExpAbsentHighTotalCount());
//                paramIndex++;
//                break;
//            case AFFYMETRIX_EXP_ABSENT_LOW_TOTAL_COUNT:
//                stmt.setInt(paramIndex, callTO.getAffymetrixExpAbsentLowTotalCount());
//                paramIndex++;
//                break;
//            case AFFYMETRIX_EXP_PROPAGATED_COUNT:
//                stmt.setInt(paramIndex, callTO.getAffymetrixExpPropagatedCount());
//                paramIndex++;
//                break;
//                
//            case RNA_SEQ_EXP_PRESENT_HIGH_SELF_COUNT:
//                stmt.setInt(paramIndex, callTO.getRNASeqExpPresentHighSelfCount());
//                paramIndex++;
//                break;
//            case RNA_SEQ_EXP_PRESENT_LOW_SELF_COUNT:
//                stmt.setInt(paramIndex, callTO.getRNASeqExpPresentLowSelfCount());
//                paramIndex++;
//                break;
//            case RNA_SEQ_EXP_ABSENT_HIGH_SELF_COUNT:
//                stmt.setInt(paramIndex, callTO.getRNASeqExpAbsentHighSelfCount());
//                paramIndex++;
//                break;
//            case RNA_SEQ_EXP_ABSENT_LOW_SELF_COUNT:
//                stmt.setInt(paramIndex, callTO.getRNASeqExpAbsentLowSelfCount());
//                paramIndex++;
//                break;
//            case RNA_SEQ_EXP_PRESENT_HIGH_DESCENDANT_COUNT:
//                stmt.setInt(paramIndex, callTO.getRNASeqExpPresentHighDescendantCount());
//                paramIndex++;
//                break;
//            case RNA_SEQ_EXP_PRESENT_LOW_DESCENDANT_COUNT:
//                stmt.setInt(paramIndex, callTO.getRNASeqExpPresentLowDescendantCount());
//                paramIndex++;
//                break;
//            case RNA_SEQ_EXP_ABSENT_HIGH_PARENT_COUNT:
//                stmt.setInt(paramIndex, callTO.getRNASeqExpAbsentHighParentCount());
//                paramIndex++;
//                break;
//            case RNA_SEQ_EXP_ABSENT_LOW_PARENT_COUNT:
//                stmt.setInt(paramIndex, callTO.getRNASeqExpAbsentLowParentCount());
//                paramIndex++;
//                break;
//            case RNA_SEQ_EXP_PRESENT_HIGH_TOTAL_COUNT:
//                stmt.setInt(paramIndex, callTO.getRNASeqExpPresentHighTotalCount());
//                paramIndex++;
//                break;
//            case RNA_SEQ_EXP_PRESENT_LOW_TOTAL_COUNT:
//                stmt.setInt(paramIndex, callTO.getRNASeqExpPresentLowTotalCount());
//                paramIndex++;
//                break;
//            case RNA_SEQ_EXP_ABSENT_HIGH_TOTAL_COUNT:
//                stmt.setInt(paramIndex, callTO.getRNASeqExpAbsentHighTotalCount());
//                paramIndex++;
//                break;
//            case RNA_SEQ_EXP_ABSENT_LOW_TOTAL_COUNT:
//                stmt.setInt(paramIndex, callTO.getRNASeqExpAbsentLowTotalCount());
//                paramIndex++;
//                break;
//            case RNA_SEQ_EXP_PROPAGATED_COUNT:
//                stmt.setInt(paramIndex, callTO.getRNASeqExpPropagatedCount());
//                paramIndex++;
//                break;
//                
//            case EST_LIB_PRESENT_HIGH_SELF_COUNT:
//                stmt.setInt(paramIndex, callTO.getESTLibPresentHighSelfCount());
//                paramIndex++;
//                break;
//            case EST_LIB_PRESENT_LOW_SELF_COUNT:
//                stmt.setInt(paramIndex, callTO.getESTLibPresentLowSelfCount());
//                paramIndex++;
//                break;
//            case EST_LIB_PRESENT_HIGH_DESCENDANT_COUNT:
//                stmt.setInt(paramIndex, callTO.getESTLibPresentHighDescendantCount());
//                paramIndex++;
//                break;
//            case EST_LIB_PRESENT_LOW_DESCENDANT_COUNT:
//                stmt.setInt(paramIndex, callTO.getESTLibPresentLowDescendantCount());
//                paramIndex++;
//                break;
//            case EST_LIB_PRESENT_HIGH_TOTAL_COUNT:
//                stmt.setInt(paramIndex, callTO.getESTLibPresentHighTotalCount());
//                paramIndex++;
//                break;
//            case EST_LIB_PRESENT_LOW_TOTAL_COUNT:
//                stmt.setInt(paramIndex, callTO.getESTLibPresentLowTotalCount());
//                paramIndex++;
//                break;
//            case EST_LIB_PROPAGATED_COUNT:
//                stmt.setInt(paramIndex, callTO.getESTLibPropagatedCount());
//                paramIndex++;
//                break;
//                
//            case IN_SITU_EXP_PRESENT_HIGH_SELF_COUNT:
//                stmt.setInt(paramIndex, callTO.getInSituExpPresentHighSelfCount());
//                paramIndex++;
//                break;
//            case IN_SITU_EXP_PRESENT_LOW_SELF_COUNT:
//                stmt.setInt(paramIndex, callTO.getInSituExpPresentLowSelfCount());
//                paramIndex++;
//                break;
//            case IN_SITU_EXP_ABSENT_HIGH_SELF_COUNT:
//                stmt.setInt(paramIndex, callTO.getInSituExpAbsentHighSelfCount());
//                paramIndex++;
//                break;
//            case IN_SITU_EXP_ABSENT_LOW_SELF_COUNT:
//                stmt.setInt(paramIndex, callTO.getInSituExpAbsentLowSelfCount());
//                paramIndex++;
//                break;
//            case IN_SITU_EXP_PRESENT_HIGH_DESCENDANT_COUNT:
//                stmt.setInt(paramIndex, callTO.getInSituExpPresentHighDescendantCount());
//                paramIndex++;
//                break;
//            case IN_SITU_EXP_PRESENT_LOW_DESCENDANT_COUNT:
//                stmt.setInt(paramIndex, callTO.getInSituExpPresentLowDescendantCount());
//                paramIndex++;
//                break;
//            case IN_SITU_EXP_ABSENT_HIGH_PARENT_COUNT:
//                stmt.setInt(paramIndex, callTO.getInSituExpAbsentHighParentCount());
//                paramIndex++;
//                break;
//            case IN_SITU_EXP_ABSENT_LOW_PARENT_COUNT:
//                stmt.setInt(paramIndex, callTO.getInSituExpAbsentLowParentCount());
//                paramIndex++;
//                break;
//            case IN_SITU_EXP_PRESENT_HIGH_TOTAL_COUNT:
//                stmt.setInt(paramIndex, callTO.getInSituExpPresentHighTotalCount());
//                paramIndex++;
//                break;
//            case IN_SITU_EXP_PRESENT_LOW_TOTAL_COUNT:
//                stmt.setInt(paramIndex, callTO.getInSituExpPresentLowTotalCount());
//                paramIndex++;
//                break;
//            case IN_SITU_EXP_ABSENT_HIGH_TOTAL_COUNT:
//                stmt.setInt(paramIndex, callTO.getInSituExpAbsentHighTotalCount());
//                paramIndex++;
//                break;
//            case IN_SITU_EXP_ABSENT_LOW_TOTAL_COUNT:
//                stmt.setInt(paramIndex, callTO.getInSituExpAbsentLowTotalCount());
//                paramIndex++;
//                break;
//            case IN_SITU_EXP_PROPAGATED_COUNT:
//                stmt.setInt(paramIndex, callTO.getInSituExpPropagatedCount());
//                paramIndex++;
//                break;
//                
//            case AFFYMETRIX_MEAN_RANK:
//                stmt.setBigDecimal(paramIndex, callTO.getAffymetrixMeanRank());
//                paramIndex++;
//                break;
//            case RNA_SEQ_MEAN_RANK:
//                stmt.setBigDecimal(paramIndex, callTO.getRNASeqMeanRank());
//                paramIndex++;
//                break;
//            case EST_RANK:
//                stmt.setBigDecimal(paramIndex, callTO.getESTRank());
//                paramIndex++;
//                break;
//            case IN_SITU_RANK:
//                stmt.setBigDecimal(paramIndex, callTO.getInSituRank());
//                paramIndex++;
//                break;
//            case AFFYMETRIX_MEAN_RANK_NORM:
//                stmt.setBigDecimal(paramIndex, callTO.getAffymetrixMeanRankNorm());
//                paramIndex++;
//                break;
//            case RNA_SEQ_MEAN_RANK_NORM:
//                stmt.setBigDecimal(paramIndex, callTO.getRNASeqMeanRankNorm());
//                paramIndex++;
//                break;
//            case EST_RANK_NORM:
//                stmt.setBigDecimal(paramIndex, callTO.getESTRankNorm());
//                paramIndex++;
//                break;
//            case IN_SITU_RANK_NORM:
//                stmt.setBigDecimal(paramIndex, callTO.getInSituRankNorm());
//                paramIndex++;
//                break;
//            case AFFYMETRIX_DISTINCT_RANK_SUM:
//                stmt.setBigDecimal(paramIndex, callTO.getAffymetrixDistinctRankSum());
//                paramIndex++;
//                break;
//            case RNA_SEQ_DISTINCT_RANK_SUM:
//                stmt.setBigDecimal(paramIndex, callTO.getRNASeqDistinctRankSum());
//                paramIndex++;
//                break;
            default:
                throw log.throwing(new IllegalStateException("Unsupported attribute: " + a));
            }
        }).collect(Collectors.joining(", ")));


        //fix for #173, see also beginning of this method
        String selectOrderBy = orderingAttrs.stream()
        .filter(a -> {
            switch(a) {
                case GENE_ID:
                case CONDITION_ID:
                case MEAN_RANK:
                    //These ordering attributes have a correspondence in the select Attributes
                    //and are already dealt with at the beginning of this method
                    return false;
                default:
                    return true;
            }
        })
        .map(a -> {
            switch(a) {
                case ANAT_ENTITY_ID:
                    return globalCondTableName + ".anatEntityId";
                case STAGE_ID:
                    return globalCondTableName + ".stageId";
                case OMA_GROUP_ID:
                    return geneTableName + ".OMAParentNodeId";
                default:
                    throw log.throwing(new IllegalStateException("Unsupported OrderingAttribute: " + a));
            }
        })
        .collect(Collectors.joining(", "));
        if (StringUtils.isNotBlank(selectOrderBy)) {
            sb.append(", ").append(selectOrderBy);
        }


        return log.exit(sb.toString());
    }

    private static String generateTableReferences(final String globalExprTableName,
            final String globalCondTableName, final String condTableName, final String geneTableName,
            boolean observedConditionFiltering, final boolean isOrderByOMANodeId) {
        log.entry(globalExprTableName, globalCondTableName, condTableName, geneTableName,
                observedConditionFiltering, isOrderByOMANodeId);

        StringBuilder sb = new StringBuilder();

        //the order of the tables is important in case we use a STRAIGHT_JOIN clause
        sb.append(" FROM ");

        //we use the gene table to filter by species rather than the condition table,
        //because there is a clustered index for the globalExpression table that is
        //PRIMAR KEY(bgeeGeneId, globalConditionId). So it is much faster to filter
        //from the gene table rather than the globalCond table
        //In order to use the clustered index for the globalExpression table,
        //if we need both to use the gene table and the globalCond table,
        //so we link them using the speciesId field, to make one common join
        //to the globalExpression table afterwards.
        // XXX: I remove usage of gene table when it is not needed because in reality,
        // at the time of writing, queries take much more time. For instance,
        // to retrieve calls of the zebrafish, using gene table it took 22 minutes 
        // while without gene table it took 3 minutes.
        sb.append("globalCond AS ").append(globalCondTableName);

        if (isOrderByOMANodeId)  {
            sb.append(" INNER JOIN ");
            sb.append("gene AS ").append(geneTableName);
            sb.append(" ON ").append(geneTableName).append(".").append(MySQLGeneDAO.BGEE_GENE_ID)
                .append(" = ").append(globalCondTableName).append(".").append(MySQLGeneDAO.BGEE_GENE_ID);
        }

        sb.append(" INNER JOIN ");
        sb.append("globalExpression AS ").append(globalExprTableName);
        sb.append(" ON ").append(globalCondTableName).append(".")
                  .append(MySQLConditionDAO.GLOBAL_COND_ID_FIELD).append(" = ")
                  .append(globalExprTableName).append(".").append(MySQLConditionDAO.GLOBAL_COND_ID_FIELD);

        if (observedConditionFiltering) {
            //we want to filter for observed organs/stages without considering the species,
            //so we don't use the globalCondToCond table
            sb.append(" LEFT OUTER JOIN cond AS ").append(condTableName)
              .append(" ON ");
            sb.append(EnumSet.allOf(ConditionDAO.Attribute.class).stream()
                    .filter(condAttr -> condAttr.isConditionParameter())
                    //actually it should be the same mechanism as in generateWhereClause
                    .map(condAttr -> {
                        switch (condAttr) {
                        case ANAT_ENTITY_ID:
                            return globalCondTableName + ".anatEntityId IS NULL OR "
                                    + globalCondTableName + ".anatEntityId = " 
                                    + condTableName + ".anatEntityId";
                        case STAGE_ID:
                            return globalCondTableName + ".stageId IS NULL OR "
                                    + globalCondTableName + ".stageId = " 
                                    + condTableName + ".stageId";
//                        case SEX:
//                            return globalCondTableName + ".sex IS NULL OR "
//                                    + globalCondTableName + ".sex = " 
//                                    + condTableName + ".sex";
//                        case STRAIN:
//                            return globalCondTableName + ".strain IS NULL OR "
//                                    + globalCondTableName + ".strain = " 
//                                    + condTableName + ".strain";
                        default:
                            throw log.throwing(new IllegalStateException(
                                    "Unsupported condition parameter: " + condAttr));
                        }
                    }));
        }

        sb.append(" ");
        return log.exit(sb.toString());
    }
    private static String generateWhereClause(final LinkedHashSet<CallDAOFilter> callFilters,
            final String globalExprTableName, final String globalCondTableName,
            final String condTableName, Set<ConditionDAO.Attribute> conditionParameters) {
        log.entry(callFilters, globalExprTableName, globalCondTableName, condTableName,
                conditionParameters);
        
        StringBuilder sb = new StringBuilder();
        sb.append(" WHERE ");

        sb.append(EnumSet.allOf(ConditionDAO.Attribute.class).stream()
                .filter(param -> param.isConditionParameter())
                .map(param -> globalCondTableName + "." + param.getTOFieldName()
                    + (conditionParameters.contains(param)? " IS NOT NULL": " IS NULL"))
                .collect(Collectors.joining(" AND ")));

        if (!callFilters.isEmpty()) {
            for (CallDAOFilter callFilter: callFilters) {
                if (callFilter.getGeneIds() != null && !callFilter.getGeneIds().isEmpty()) {
                    //For now, we don't support using both gene IDs and species IDs in a same CallDAOFilter.
                    //If needed, we should either create a "geneSpeciesFilter" in CallDAOFilter,
                    //or use one CallDAOFilter for the gene IDs and another one for the species IDs.
                    if (callFilter.getSpeciesIds() != null && !callFilter.getSpeciesIds().isEmpty()) {
                        throw log.throwing(new UnsupportedOperationException(
                                "Currently not supported to provide both gene and species IDs in a same CallDAOFilter"));
                    }
                    sb.append(" AND ");
                    sb.append(globalExprTableName).append(".").append(MySQLGeneDAO.BGEE_GENE_ID)
                    .append(" IN (")
                    .append(BgeePreparedStatement.generateParameterizedQueryString(callFilter.getGeneIds().size()))
                    .append(")");
                }

                if (callFilter.getSpeciesIds() != null && !callFilter.getSpeciesIds().isEmpty()) {
                    sb.append(" AND ");
                    sb.append(globalCondTableName).append(".speciesId IN (")
                    .append(BgeePreparedStatement.generateParameterizedQueryString(callFilter.getSpeciesIds().size()))
                    .append(") ");
                }

                if (callFilter.getConditionFilters() != null && !callFilter.getConditionFilters().isEmpty()) {
                    sb.append(" AND ");
                    sb.append(
                            callFilter.getConditionFilters().stream().map(f -> {
                                StringBuilder sb2 = new StringBuilder();
                                sb2.append("(");

                                if (!f.getAnatEntityIds().isEmpty()) {
                                    sb2.append(globalCondTableName).append(".anatEntityId IN (")
                                    .append(BgeePreparedStatement.generateParameterizedQueryString(
                                            f.getAnatEntityIds().size()))
                                    .append(")");
                                }
                                if (!f.getDevStageIds().isEmpty()) {
                                    if (!f.getAnatEntityIds().isEmpty()) {
                                        sb2.append(" AND ");
                                    }
                                    sb2.append(globalCondTableName).append(".stageId IN (")
                                    .append(BgeePreparedStatement.generateParameterizedQueryString(
                                            f.getDevStageIds().size()))
                                    .append(")");
                                }
                                if (f.getObservedConditions() != null) {
                                    if (!f.getAnatEntityIds().isEmpty() || !f.getDevStageIds().isEmpty()) {
                                        sb2.append(" AND ");
                                    }
                                    sb2.append(condTableName).append(".").append(MySQLConditionDAO.RAW_COND_ID_FIELD);
                                    if (f.getObservedConditions()) {
                                        sb2.append(" IS NOT NULL ");
                                    } else {
                                        sb2.append(" IS NULL ");
                                    }
                                }

                                sb2.append(")");
                                return sb2.toString();

                            }).collect(Collectors.joining(" OR ", "(", ")"))
                            );
                }

                if (callFilter.getDataFilters() != null && !callFilter.getDataFilters().isEmpty()) {
                    sb.append(" AND ");
                    sb.append(generateDataFilters(callFilter.getDataFilters(), globalExprTableName));
                }

                //TODO: do not hardcode data types
                //TODO: check the use of getCallObservedData (we want NULL to say "any value")
                if (callFilter.getCallObservedData() != null) {
                    sb.append(" AND ").append("(")
                        .append(globalExprTableName).append(".affymetrixConditionObservedData = ? OR ")
                        .append(globalExprTableName).append(".estConditionObservedData = ? OR ")
                        .append(globalExprTableName).append(".inSituConditionObservedData = ? OR ")
                        .append(globalExprTableName).append(".rnaSeqConditionObservedData = ?").append(")");
                }

                if (callFilter.getObservedDataFilter() != null && !callFilter.getObservedDataFilter().isEmpty()) {
                    sb.append(callFilter.getObservedDataFilter().entrySet().stream()
                            //FIXME: manage the "non-observed states"
                            .filter(e -> e.getValue() != null)
                            .map(e -> {
                                StringBuilder sb2 = new StringBuilder();
                                sb2.append(" AND ");
                                sb2.append("(");
                                ConditionDAO.Attribute attr = e.getKey();
                                switch (attr) {
                                    //TODO: do not hardcode data types (see generation for ranks)
                                    case ANAT_ENTITY_ID:
                                        sb2.append(generatePropStateQuery(globalExprTableName,
                                                "affymetrixAnatEntityPropagationState")).append(" OR ")
                                        .append(generatePropStateQuery(globalExprTableName,
                                                "estAnatEntityPropagationState")).append(" OR ")
                                        .append(generatePropStateQuery(globalExprTableName, 
                                                "inSituAnatEntityPropagationState")).append(" OR ")
                                        .append(generatePropStateQuery(globalExprTableName,
                                                "rnaSeqAnatEntityPropagationState"));
                                        break;
                                    case STAGE_ID:
                                        sb2.append(generatePropStateQuery(globalExprTableName,
                                                "affymetrixStagePropagationState")).append(" OR ")
                                        .append(generatePropStateQuery(globalExprTableName,
                                                "estStagePropagationState")).append(" OR ")
                                        .append(generatePropStateQuery(globalExprTableName,
                                                "inSituStagePropagationState")).append(" OR ")
                                        .append(generatePropStateQuery(globalExprTableName,
                                                "rnaSeqStagePropagationState"));
                                        break;
                                    default:
                                        throw log.throwing(new UnsupportedOperationException(
                                                "ConditionDAO.Attribute not supported: " + attr));
                                }
                                sb2.append(")");
                                return sb2.toString();
                            }).collect(Collectors.joining("")));
                }
            }
        }
        return log.exit(sb.toString());
    }
    //FIXME: must manage non-observed states
    private static String generatePropStateQuery(String globalExprTableName, String columnName) {
        log.entry(globalExprTableName, columnName);
        return log.exit(globalExprTableName + "." + columnName + " IN (" +
                BgeePreparedStatement.generateParameterizedQueryString(
                OBSERVED_STATES.size()) + ")");
    }
    private static String generateDataFilters(final LinkedHashSet<CallDataDAOFilter> dataFilters,
            final String globalExprTableName) {
        log.entry(dataFilters, globalExprTableName);

        return dataFilters.stream()
            .map(dataFilter -> {
                return dataFilter.getExperimentCountFilters().stream()
                    .map(countOrFilters -> {
                        return countOrFilters.stream()
                            .map(countFilter -> {
                                String suffix = null;
                                switch (countFilter.getQualifier()) {
                                case GREATER_THAN:
                                    suffix = " > ";
                                    break;
                                case LESS_THAN:
                                    suffix = " < ";
                                    break;
                                case EQUALS_TO:
                                    suffix = " = ";
                                    break;
                                default:
                                    throw new IllegalArgumentException("Unsupported qualifier: " + countFilter.getQualifier());
                                }
                                suffix += "?";

                                return dataFilter.getDataTypes().stream()
                                        //discard request of no-expression calls from EST data, there is no such field
                                        .filter(dataType ->
                                        !CallType.ABSENT.equals(countFilter.getCallType()) || !DAODataType.EST.equals(dataType))

                                        .map(dataType -> getExpCountFilterFieldName(dataType, countFilter))
                                        .collect(Collectors.joining(" + ", "(", suffix + ")"));
                            })
                            .collect(Collectors.joining(" OR ", "(", ")"));
                    })
                    .collect(Collectors.joining(" AND ", "(", ")"));
            })
           .collect(Collectors.joining(" OR ", "(", ")"));
    }

    private static String getExpCountFilterFieldName(DAODataType dataType,
            DAOExperimentCountFilter expCountFilter) {
        log.entry(dataType, expCountFilter);

        StringBuilder sb = new StringBuilder();
        switch(dataType) {
        case EST:
            sb.append("estLib");
            break;
        case AFFYMETRIX:
            sb.append("affymetrixExp");
            break;
        case IN_SITU:
            sb.append("inSituExp");
            break;
        case RNA_SEQ:
            sb.append("rnaSeqExp");
            break;
        default:
            throw log.throwing(new IllegalStateException("Unsupported data type: " + dataType));
        }

        switch (expCountFilter.getCallType()) {
        case PRESENT:
            sb.append("Present");
            break;
        case ABSENT:
            if (DAODataType.EST.equals(dataType)) {
                throw log.throwing(new IllegalArgumentException("Impossible call type for EST data"));
            }
            sb.append("Absent");
            break;
        default:
            throw log.throwing(new IllegalStateException("Unsupported callType " + expCountFilter.getCallType()));
        }

        switch (expCountFilter.getDataQuality()) {
        case LOW:
            sb.append("Low");
            break;
        case HIGH:
            sb.append("High");
            break;
        default:
            throw log.throwing(new IllegalStateException("Unsupported data quality " + expCountFilter.getDataQuality()));
        }

        switch (expCountFilter.getPropagationState()) {
        case SELF:
            sb.append("Self");
            break;
        case ANCESTOR:
            if (DAODataType.EST.equals(dataType)) {
                throw log.throwing(new IllegalArgumentException("Impossible propagation state for EST data"));
            }
            sb.append("Parent");
            break;
        case DESCENDANT:
            sb.append("Descendant");
            break;
        case ALL:
            sb.append("Total");
            break;
        default:
            throw log.throwing(new IllegalStateException("Unsupported propagation state "
                               + expCountFilter.getPropagationState()));
        }

        sb.append("Count");
        return log.exit(sb.toString());
    }
//    static {
//        log.entry();
//        Map<String, GlobalExpressionCallDAO.Attribute> colToAttributesMap = new HashMap<>();
//        colToAttributesMap.put(GLOBAL_EXPR_ID_FIELD, GlobalExpressionCallDAO.Attribute.ID);
//        colToAttributesMap.put(MySQLGeneDAO.BGEE_GENE_ID, GlobalExpressionCallDAO.Attribute.BGEE_GENE_ID);
//        colToAttributesMap.put(MySQLConditionDAO.GLOBAL_COND_ID_FIELD,
//                GlobalExpressionCallDAO.Attribute.CONDITION_ID);
//        
//        colToAttributesMap.put("globalMeanRank", GlobalExpressionCallDAO.Attribute.GLOBAL_MEAN_RANK);
//        
//        colToAttributesMap.put("affymetrixExpPresentHighSelfCount", 
//                GlobalExpressionCallDAO.Attribute.AFFYMETRIX_EXP_PRESENT_HIGH_SELF_COUNT);
//        colToAttributesMap.put("affymetrixExpPresentLowSelfCount", 
//                GlobalExpressionCallDAO.Attribute.AFFYMETRIX_EXP_PRESENT_LOW_SELF_COUNT);
//        colToAttributesMap.put("affymetrixExpAbsentHighSelfCount", 
//                GlobalExpressionCallDAO.Attribute.AFFYMETRIX_EXP_ABSENT_HIGH_SELF_COUNT);
//        colToAttributesMap.put("affymetrixExpAbsentLowSelfCount", 
//                GlobalExpressionCallDAO.Attribute.AFFYMETRIX_EXP_ABSENT_LOW_SELF_COUNT);
//        colToAttributesMap.put("affymetrixExpPresentHighDescendantCount", 
//                GlobalExpressionCallDAO.Attribute.AFFYMETRIX_EXP_PRESENT_HIGH_DESCENDANT_COUNT);
//        colToAttributesMap.put("affymetrixExpPresentLowDescendantCount", 
//                GlobalExpressionCallDAO.Attribute.AFFYMETRIX_EXP_PRESENT_LOW_DESCENDANT_COUNT);
//        colToAttributesMap.put("affymetrixExpAbsentHighParentCount", 
//                GlobalExpressionCallDAO.Attribute.AFFYMETRIX_EXP_ABSENT_HIGH_PARENT_COUNT);
//        colToAttributesMap.put("affymetrixExpAbsentLowParentCount", 
//                GlobalExpressionCallDAO.Attribute.AFFYMETRIX_EXP_ABSENT_LOW_PARENT_COUNT);
//        colToAttributesMap.put("affymetrixExpPresentHighTotalCount", 
//                GlobalExpressionCallDAO.Attribute.AFFYMETRIX_EXP_PRESENT_HIGH_TOTAL_COUNT);
//        colToAttributesMap.put("affymetrixExpPresentLowTotalCount", 
//                GlobalExpressionCallDAO.Attribute.AFFYMETRIX_EXP_PRESENT_LOW_TOTAL_COUNT);
//        colToAttributesMap.put("affymetrixExpAbsentHighTotalCount", 
//                GlobalExpressionCallDAO.Attribute.AFFYMETRIX_EXP_ABSENT_HIGH_TOTAL_COUNT);
//        colToAttributesMap.put("affymetrixExpAbsentLowTotalCount", 
//                GlobalExpressionCallDAO.Attribute.AFFYMETRIX_EXP_ABSENT_LOW_TOTAL_COUNT);
//        colToAttributesMap.put("affymetrixExpPropagatedCount", 
//                GlobalExpressionCallDAO.Attribute.AFFYMETRIX_EXP_PROPAGATED_COUNT);
//        
//        colToAttributesMap.put("rnaSeqExpPresentHighSelfCount", 
//                GlobalExpressionCallDAO.Attribute.RNA_SEQ_EXP_PRESENT_HIGH_SELF_COUNT);
//        colToAttributesMap.put("rnaSeqExpPresentLowSelfCount", 
//                GlobalExpressionCallDAO.Attribute.RNA_SEQ_EXP_PRESENT_LOW_SELF_COUNT);
//        colToAttributesMap.put("rnaSeqExpAbsentHighSelfCount", 
//                GlobalExpressionCallDAO.Attribute.RNA_SEQ_EXP_ABSENT_HIGH_SELF_COUNT);
//        colToAttributesMap.put("rnaSeqExpAbsentLowSelfCount", 
//                GlobalExpressionCallDAO.Attribute.RNA_SEQ_EXP_ABSENT_LOW_SELF_COUNT);
//        colToAttributesMap.put("rnaSeqExpPresentHighDescendantCount", 
//                GlobalExpressionCallDAO.Attribute.RNA_SEQ_EXP_PRESENT_HIGH_DESCENDANT_COUNT);
//        colToAttributesMap.put("rnaSeqExpPresentLowDescendantCount", 
//                GlobalExpressionCallDAO.Attribute.RNA_SEQ_EXP_PRESENT_LOW_DESCENDANT_COUNT);
//        colToAttributesMap.put("rnaSeqExpAbsentHighParentCount", 
//                GlobalExpressionCallDAO.Attribute.RNA_SEQ_EXP_ABSENT_HIGH_PARENT_COUNT);
//        colToAttributesMap.put("rnaSeqExpAbsentLowParentCount", 
//                GlobalExpressionCallDAO.Attribute.RNA_SEQ_EXP_ABSENT_LOW_PARENT_COUNT);
//        colToAttributesMap.put("rnaSeqExpPresentHighTotalCount", 
//                GlobalExpressionCallDAO.Attribute.RNA_SEQ_EXP_PRESENT_HIGH_TOTAL_COUNT);
//        colToAttributesMap.put("rnaSeqExpPresentLowTotalCount", 
//                GlobalExpressionCallDAO.Attribute.RNA_SEQ_EXP_PRESENT_LOW_TOTAL_COUNT);
//        colToAttributesMap.put("rnaSeqExpAbsentHighTotalCount", 
//                GlobalExpressionCallDAO.Attribute.RNA_SEQ_EXP_ABSENT_HIGH_TOTAL_COUNT);
//        colToAttributesMap.put("rnaSeqExpAbsentLowTotalCount", 
//                GlobalExpressionCallDAO.Attribute.RNA_SEQ_EXP_ABSENT_LOW_TOTAL_COUNT);
//        colToAttributesMap.put("rnaSeqExpPropagatedCount", 
//                GlobalExpressionCallDAO.Attribute.RNA_SEQ_EXP_PROPAGATED_COUNT);
//        
//        colToAttributesMap.put("estLibPresentHighSelfCount", 
//                GlobalExpressionCallDAO.Attribute.EST_LIB_PRESENT_HIGH_SELF_COUNT);
//        colToAttributesMap.put("estLibPresentLowSelfCount", 
//                GlobalExpressionCallDAO.Attribute.EST_LIB_PRESENT_LOW_SELF_COUNT);
//        colToAttributesMap.put("estLibPresentHighDescendantCount", 
//                GlobalExpressionCallDAO.Attribute.EST_LIB_PRESENT_HIGH_DESCENDANT_COUNT);
//        colToAttributesMap.put("estLibPresentLowDescendantCount", 
//                GlobalExpressionCallDAO.Attribute.EST_LIB_PRESENT_LOW_DESCENDANT_COUNT);
//        colToAttributesMap.put("estLibPresentHighTotalCount", 
//                GlobalExpressionCallDAO.Attribute.EST_LIB_PRESENT_HIGH_TOTAL_COUNT);
//        colToAttributesMap.put("estLibPresentLowTotalCount", 
//                GlobalExpressionCallDAO.Attribute.EST_LIB_PRESENT_LOW_TOTAL_COUNT);
//        colToAttributesMap.put("estLibPropagatedCount", 
//                GlobalExpressionCallDAO.Attribute.EST_LIB_PROPAGATED_COUNT);
//        
//        colToAttributesMap.put("inSituExpPresentHighSelfCount", 
//                GlobalExpressionCallDAO.Attribute.IN_SITU_EXP_PRESENT_HIGH_SELF_COUNT);
//        colToAttributesMap.put("inSituExpPresentLowSelfCount", 
//                GlobalExpressionCallDAO.Attribute.IN_SITU_EXP_PRESENT_LOW_SELF_COUNT);
//        colToAttributesMap.put("inSituExpAbsentHighSelfCount", 
//                GlobalExpressionCallDAO.Attribute.IN_SITU_EXP_ABSENT_HIGH_SELF_COUNT);
//        colToAttributesMap.put("inSituExpAbsentLowSelfCount", 
//                GlobalExpressionCallDAO.Attribute.IN_SITU_EXP_ABSENT_LOW_SELF_COUNT);
//        colToAttributesMap.put("inSituExpPresentHighDescendantCount", 
//                GlobalExpressionCallDAO.Attribute.IN_SITU_EXP_PRESENT_HIGH_DESCENDANT_COUNT);
//        colToAttributesMap.put("inSituExpPresentLowDescendantCount", 
//                GlobalExpressionCallDAO.Attribute.IN_SITU_EXP_PRESENT_LOW_DESCENDANT_COUNT);
//        colToAttributesMap.put("inSituExpAbsentHighParentCount", 
//                GlobalExpressionCallDAO.Attribute.IN_SITU_EXP_ABSENT_HIGH_PARENT_COUNT);
//        colToAttributesMap.put("inSituExpAbsentLowParentCount", 
//                GlobalExpressionCallDAO.Attribute.IN_SITU_EXP_ABSENT_LOW_PARENT_COUNT);
//        colToAttributesMap.put("inSituExpPresentHighTotalCount", 
//                GlobalExpressionCallDAO.Attribute.IN_SITU_EXP_PRESENT_HIGH_TOTAL_COUNT);
//        colToAttributesMap.put("inSituExpPresentLowTotalCount", 
//                GlobalExpressionCallDAO.Attribute.IN_SITU_EXP_PRESENT_LOW_TOTAL_COUNT);
//        colToAttributesMap.put("inSituExpAbsentHighTotalCount", 
//                GlobalExpressionCallDAO.Attribute.IN_SITU_EXP_ABSENT_HIGH_TOTAL_COUNT);
//        colToAttributesMap.put("inSituExpAbsentLowTotalCount", 
//                GlobalExpressionCallDAO.Attribute.IN_SITU_EXP_ABSENT_LOW_TOTAL_COUNT);
//        colToAttributesMap.put("inSituExpPropagatedCount", 
//                GlobalExpressionCallDAO.Attribute.IN_SITU_EXP_PROPAGATED_COUNT);
//
//        colToAttributesMap.put("affymetrixMeanRank", GlobalExpressionCallDAO.Attribute.AFFYMETRIX_MEAN_RANK);
//        colToAttributesMap.put("rnaSeqMeanRank", GlobalExpressionCallDAO.Attribute.RNA_SEQ_MEAN_RANK);
//        colToAttributesMap.put("estRank", GlobalExpressionCallDAO.Attribute.EST_RANK);
//        colToAttributesMap.put("inSituRank", GlobalExpressionCallDAO.Attribute.IN_SITU_RANK);
//        colToAttributesMap.put("affymetrixMeanRankNorm", 
//                GlobalExpressionCallDAO.Attribute.AFFYMETRIX_MEAN_RANK_NORM);
//        colToAttributesMap.put("rnaSeqMeanRankNorm", 
//                GlobalExpressionCallDAO.Attribute.RNA_SEQ_MEAN_RANK_NORM);
//        colToAttributesMap.put("estRankNorm", GlobalExpressionCallDAO.Attribute.EST_RANK_NORM);
//        colToAttributesMap.put("inSituRankNorm", GlobalExpressionCallDAO.Attribute.IN_SITU_RANK_NORM);
//        colToAttributesMap.put("affymetrixDistinctRankSum", 
//                GlobalExpressionCallDAO.Attribute.AFFYMETRIX_DISTINCT_RANK_SUM);
//        colToAttributesMap.put("rnaSeqDistinctRankSum", 
//                GlobalExpressionCallDAO.Attribute.RNA_SEQ_DISTINCT_RANK_SUM);
//
//        colToAttrMap = Collections.unmodifiableMap(colToAttributesMap);
//        log.exit();
//    }

    private String generateOrderByClause(
            LinkedHashMap<GlobalExpressionCallDAO.OrderingAttribute, DAO.Direction> orderingAttrs,
            String globalExprTableName, String globalCondTableName, String geneTableName) {
        log.entry(orderingAttrs, globalExprTableName, globalCondTableName, geneTableName);

        if (orderingAttrs.isEmpty()) {
            return log.exit("");
        }

        return log.exit(orderingAttrs.entrySet().stream()
                .map(entry -> {
                    String orderBy = "";
                    switch(entry.getKey()) {
                        case GENE_ID:
                            orderBy = globalExprTableName + "." + MySQLGeneDAO.BGEE_GENE_ID;
                            break;
                        case CONDITION_ID:
                            orderBy = globalExprTableName + "." + MySQLConditionDAO.GLOBAL_COND_ID_FIELD;
                            break;
                        case ANAT_ENTITY_ID:
                            orderBy = globalCondTableName + ".anatEntityId";
                            break;
                        case STAGE_ID:
                            orderBy = globalCondTableName + ".stageId";
                            break;
                        case OMA_GROUP_ID:
                            orderBy = geneTableName + ".OMAParentNodeId";
                            break;
                        case MEAN_RANK:
                            orderBy = GLOBAL_MEAN_RANK_FIELD;
                            break;
                        default:
                            throw log.throwing(new IllegalStateException("Unsupported OrderingAttribute: "
                                    + entry.getKey()));
                    }
                    switch(entry.getValue()) {
                        case DESC:
                            orderBy += " desc";
                            break;
                        case ASC:
                            orderBy += " asc";
                            break;
                        default:
                            throw log.throwing(new IllegalStateException("Unsupported Direction: "
                                    + entry.getValue()));
                    }
                    return orderBy;
                })
                .collect(Collectors.joining(", ", " ORDER BY ", "")));
    }

    public MySQLGlobalExpressionCallDAO(MySQLDAOManager manager) throws IllegalArgumentException {
        super(manager);
    }

    @Override
    public GlobalExpressionCallTOResultSet getGlobalExpressionCalls(
            Collection<CallDAOFilter> callFilters, Collection<ConditionDAO.Attribute> conditionParameters,
            Collection<GlobalExpressionCallDAO.Attribute> attributes,
            LinkedHashMap<GlobalExpressionCallDAO.OrderingAttribute, DAO.Direction> orderingAttributes)
                    throws DAOException, IllegalArgumentException {
        log.entry(callFilters, conditionParameters, attributes, orderingAttributes);

        //******************************************
        // CLONE ARGUMENTS AND SANITY CHECKS
        //******************************************
        //needs a LinkedHashSet for consistent settings of the parameters. 
        LinkedHashSet<CallDAOFilter> clonedCallFilters = callFilters == null?
                new LinkedHashSet<>(): new LinkedHashSet<>(callFilters);
        //condition parameter combination
        Set<ConditionDAO.Attribute> clonedCondParams = conditionParameters == null?
                EnumSet.noneOf(ConditionDAO.Attribute.class): EnumSet.copyOf(conditionParameters);
        //attributes
        Set<GlobalExpressionCallDAO.Attribute> clonedAttrs = attributes == null?
                EnumSet.noneOf(GlobalExpressionCallDAO.Attribute.class): EnumSet.copyOf(attributes);
        //ordering attributes
        LinkedHashMap<GlobalExpressionCallDAO.OrderingAttribute, DAO.Direction> clonedOrderingAttrs = 
                orderingAttributes == null? new LinkedHashMap<>(): new LinkedHashMap<>(orderingAttributes);

        //sanity checks
        if (clonedCallFilters.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("Some CallDAOFilters must be provided"));
        }
        if (clonedCondParams.isEmpty()) {
            throw log.throwing(new IllegalArgumentException(
                    "A combination of condition parameters must be provided"));
        }
        if (clonedCondParams.stream().anyMatch(a -> !a.isConditionParameter())) {
            throw log.throwing(new IllegalArgumentException("The condition parameter combination "
                    + "contains some Attributes that are not condition parameters: " + clonedCondParams));
        }
        //TODO: can't we automatically add the field if requested in ordering clause?
        if (clonedOrderingAttrs.containsKey(GlobalExpressionCallDAO.OrderingAttribute.MEAN_RANK) 
                && !clonedAttrs.contains(GlobalExpressionCallDAO.Attribute.GLOBAL_MEAN_RANK)) {
            throw log.throwing(new IllegalArgumentException("To order by " 
                    + GlobalExpressionCallDAO.OrderingAttribute.MEAN_RANK
                    + ", the attribute " + GlobalExpressionCallDAO.Attribute.GLOBAL_MEAN_RANK
                    + " should be retrieved"));
        }


        //******************************************
        // GENERATE QUERY
        //******************************************
        String globalExprTableName = "globalExpression";
        String globalCondTableName = "globalCond";
        String condTableName = "cond";
        String geneTableName = "gene";
        //do we need a join to the raw condition table
        boolean observedConditionFilter = clonedCallFilters.stream()
                .anyMatch(callFilter -> callFilter.getConditionFilters()
                        .stream().anyMatch(condFilter -> condFilter.getObservedConditions() != null));
        //retrieve the data types to retrieve data for
        Set<DAODataType> dataTypes = clonedCallFilters.stream()
                .flatMap(callFilter -> callFilter.getDataFilters().isEmpty()?
                        EnumSet.allOf(DAODataType.class).stream():
                            callFilter.getDataFilters().stream()
                            .flatMap(dataFilter -> dataFilter.getDataTypes().stream()))
                .collect(Collectors.toSet());

        StringBuilder sb = new StringBuilder();
        sb.append(generateSelectClause(clonedAttrs, clonedOrderingAttrs.keySet(), dataTypes,
                globalExprTableName, globalCondTableName, geneTableName));
        sb.append(generateTableReferences(globalExprTableName, globalCondTableName, condTableName,
                geneTableName, observedConditionFilter,
                clonedOrderingAttrs.containsKey(GlobalExpressionCallDAO.OrderingAttribute.OMA_GROUP_ID)));
        sb.append(generateWhereClause(clonedCallFilters, globalExprTableName, globalCondTableName,
                condTableName, clonedCondParams));
        sb.append(generateOrderByClause(clonedOrderingAttrs, globalExprTableName, globalCondTableName, geneTableName));

        //we don't use a try-with-resource, because we return a pointer to the results,
        //not the actual results, so we should not close this BgeePreparedStatement.
        try {
            BgeePreparedStatement stmt = this.getManager().getConnection().prepareStatement(sb.toString());
            int offsetParamIndex = 1;
            for (CallDAOFilter callFilter: clonedCallFilters) {

                if (callFilter.getGeneIds() != null && !callFilter.getGeneIds().isEmpty()) {
                    stmt.setIntegers(offsetParamIndex, callFilter.getGeneIds(), true);
                    offsetParamIndex += callFilter.getGeneIds().size();
                }

                if (callFilter.getSpeciesIds() != null && !callFilter.getSpeciesIds().isEmpty()) {
                    stmt.setIntegers(offsetParamIndex, callFilter.getSpeciesIds(), true);
                    offsetParamIndex += callFilter.getSpeciesIds().size();
                }

                if (callFilter.getConditionFilters() != null && !callFilter.getConditionFilters().isEmpty()) {
                    for (DAOConditionFilter condFilter: callFilter.getConditionFilters()) {

                        if (!condFilter.getAnatEntityIds().isEmpty()) {
                            stmt.setStrings(offsetParamIndex, condFilter.getAnatEntityIds(), true);
                            offsetParamIndex += condFilter.getAnatEntityIds().size();
                        }
                        if (!condFilter.getDevStageIds().isEmpty()) {
                            stmt.setStrings(offsetParamIndex, condFilter.getDevStageIds(), true);
                            offsetParamIndex += condFilter.getDevStageIds().size();
                        }
                        if (condFilter.getObservedConditions() != null) {
                            stmt.setBoolean(offsetParamIndex, condFilter.getObservedConditions());
                            offsetParamIndex++;
                        }
                    }
                }

                if (callFilter.getDataFilters() != null) {
                    for (CallDataDAOFilter dataFilter: callFilter.getDataFilters()) {
                        for (Set<DAOExperimentCountFilter> countOrFilters: dataFilter.getExperimentCountFilters()) {
                            for (DAOExperimentCountFilter countFilter : countOrFilters) {
                                stmt.setInt(offsetParamIndex, countFilter.getCount());
                                offsetParamIndex++;
                            }
                        }
                    }
                }

                if (callFilter.getCallObservedData() != null) {
                    stmt.setBooleans(offsetParamIndex,
                            Collections.nCopies(DATA_TYPE_COUNT, callFilter.getCallObservedData()),
                            true);
                    offsetParamIndex += DATA_TYPE_COUNT;
                }

                if (callFilter.getObservedDataFilter() != null && !callFilter.getObservedDataFilter().isEmpty()) {
                    for (Boolean isObservedData: callFilter.getObservedDataFilter().values()) {
                        if (isObservedData != null) {
                            for (int i = 0; i < DATA_TYPE_COUNT; i++) {
                                //FIXME: manage non-observed states
                                stmt.setEnumDAOFields(offsetParamIndex, OBSERVED_STATES, true);
                                offsetParamIndex += OBSERVED_STATES.size();
                            }
                        }
                    }
                }
            }

            return log.exit(new MySQLGlobalExpressionCallTOResultSet(stmt));

        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }

    }

    @Override
    public int getMaxGlobalExprId() throws DAOException {
        log.entry();

        String sql = "SELECT MAX(" + GLOBAL_EXPR_ID_FIELD + ") AS " + GLOBAL_EXPR_ID_FIELD 
            + " FROM " + GLOBAL_EXPR_TABLE_NAME;
    
        try (GlobalExpressionCallTOResultSet resultSet = new MySQLGlobalExpressionCallTOResultSet(
                this.getManager().getConnection().prepareStatement(sql))) {
            
            if (resultSet.next() && resultSet.getTO().getId() != null) {
                return log.exit(resultSet.getTO().getId());
            } 
            return log.exit(0);
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
    }

    @Override
    public int insertGlobalCalls(Collection<GlobalExpressionCallTO> callTOs)
            throws DAOException, IllegalArgumentException {
        log.entry(callTOs);
        
        if (callTOs == null || callTOs.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("No calls provided"));
        }

        StringBuilder sql = new StringBuilder(); 
        sql.append("INSERT INTO ").append(GLOBAL_EXPR_TABLE_NAME).append(" (")

           .append("globalExpressionId, bgeeGeneId, globalConditionId, ")

           .append("estAnatEntityPropagationState, estStagePropagationState, ")
           .append("estConditionObservedData, estLibPresentHighSelfCount, estLibPresentLowSelfCount, ")
           .append("estLibPresentHighDescendantCount, estLibPresentLowDescendantCount, ")
           .append("estLibPresentHighTotalCount, estLibPresentLowTotalCount, estLibPropagatedCount, ")

           .append("affymetrixAnatEntityPropagationState, affymetrixStagePropagationState, ")
           .append("affymetrixConditionObservedData, affymetrixExpPresentHighSelfCount, ")
           .append("affymetrixExpPresentLowSelfCount, affymetrixExpAbsentHighSelfCount, ")
           .append("affymetrixExpAbsentLowSelfCount, affymetrixExpPresentHighDescendantCount, ")
           .append("affymetrixExpPresentLowDescendantCount, affymetrixExpAbsentHighParentCount, ")
           .append("affymetrixExpAbsentLowParentCount, affymetrixExpPresentHighTotalCount, ")
           .append("affymetrixExpPresentLowTotalCount, affymetrixExpAbsentHighTotalCount, ")
           .append("affymetrixExpAbsentLowTotalCount, affymetrixExpPropagatedCount, ")

           .append("inSituAnatEntityPropagationState, inSituStagePropagationState, ")
           .append("inSituConditionObservedData, inSituExpPresentHighSelfCount, ")
           .append("inSituExpPresentLowSelfCount, inSituExpAbsentHighSelfCount, ")
           .append("inSituExpAbsentLowSelfCount, inSituExpPresentHighDescendantCount, ")
           .append("inSituExpPresentLowDescendantCount, inSituExpAbsentHighParentCount, ")
           .append("inSituExpAbsentLowParentCount, inSituExpPresentHighTotalCount, ")
           .append("inSituExpPresentLowTotalCount, inSituExpAbsentHighTotalCount, ")
           .append("inSituExpAbsentLowTotalCount, inSituExpPropagatedCount, ")

           .append("rnaSeqAnatEntityPropagationState, rnaSeqStagePropagationState, ")
           .append("rnaSeqConditionObservedData, rnaSeqExpPresentHighSelfCount, ")
           .append("rnaSeqExpPresentLowSelfCount, rnaSeqExpAbsentHighSelfCount, ")
           .append("rnaSeqExpAbsentLowSelfCount, rnaSeqExpPresentHighDescendantCount, ")
           .append("rnaSeqExpPresentLowDescendantCount, rnaSeqExpAbsentHighParentCount, ")
           .append("rnaSeqExpAbsentLowParentCount, rnaSeqExpPresentHighTotalCount, ")
           .append("rnaSeqExpPresentLowTotalCount, rnaSeqExpAbsentHighTotalCount, ")
           .append("rnaSeqExpAbsentLowTotalCount, rnaSeqExpPropagatedCount")
           
           .append(") VALUES ");
        for (int i = 0; i < callTOs.size(); i++) {
            if (i > 0) {
                sql.append(", ");
            }
            sql.append("(").append(BgeePreparedStatement.generateParameterizedQueryString(61))
               .append(") ");
        }
        try (BgeePreparedStatement stmt = 
                this.getManager().getConnection().prepareStatement(sql.toString())) {
            int paramIndex = 1;
            for (GlobalExpressionCallTO callTO: callTOs) {
                stmt.setInt(paramIndex, callTO.getId());
                paramIndex++;
                stmt.setInt(paramIndex, callTO.getBgeeGeneId());
                paramIndex++;
                stmt.setInt(paramIndex, callTO.getConditionId());
                paramIndex++;
                
                //create a Map<DAODataType, GlobalExpressionCallDataTO>,
                //to be able to select the appropriate data in the appropriate INSERT order.
                Map<DAODataType, GlobalExpressionCallDataTO> dataTypeToCallDataTO =
                        callTO.getCallDataTOs().stream()
                        .collect(Collectors.toMap(c -> c.getDataType(), c -> c));

                //Just to make sure we cover all data type cases
                assert DAODataType.values().length == 4;
                //the order in which we set the data types is important, see creation of the query.
                //And we need to set all parameters even if there is no data for a data type.
                paramIndex = setStatementCallDataParameters(stmt, paramIndex,
                        dataTypeToCallDataTO.get(DAODataType.EST), DAODataType.EST);
                paramIndex = setStatementCallDataParameters(stmt, paramIndex,
                        dataTypeToCallDataTO.get(DAODataType.AFFYMETRIX), DAODataType.AFFYMETRIX);
                paramIndex = setStatementCallDataParameters(stmt, paramIndex,
                        dataTypeToCallDataTO.get(DAODataType.IN_SITU), DAODataType.IN_SITU);
                paramIndex = setStatementCallDataParameters(stmt, paramIndex,
                        dataTypeToCallDataTO.get(DAODataType.RNA_SEQ), DAODataType.RNA_SEQ);
            }
            
            return log.exit(stmt.executeUpdate());
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
    }
    
    private static int setStatementCallDataParameters(BgeePreparedStatement stmt, int paramIndex,
            GlobalExpressionCallDataTO callDataTO, DAODataType dataType) throws SQLException {
        log.entry(stmt, paramIndex, callDataTO, dataType);
        
        int newParamIndex = paramIndex;
        if (callDataTO == null) {
            //Propagation states, same for all data types
            for (int i = 0; i < 3; i++) {
                stmt.setNull(newParamIndex, Types.VARCHAR);
                newParamIndex++;
            }
            //Expression counts, different between EST and other data types
            int expExprAttrCount = 0;
            switch (dataType) {
            case EST:
                expExprAttrCount = 7;
                break;
            case AFFYMETRIX:
            case IN_SITU:
            case RNA_SEQ:
                expExprAttrCount = 13;
                break;
            default:
                throw log.throwing(new IllegalStateException("Unsupported DAODataType: " + dataType));
            }
            for (int i = 0; i < expExprAttrCount; i++) {
                stmt.setInt(newParamIndex, 0);
                newParamIndex++;
            }

            return log.exit(newParamIndex);
        }

        //** Observed data/ propagation states **
        //make sure we covert all condition parameters
        assert ((int) EnumSet.allOf(ConditionDAO.Attribute.class).stream()
                .filter(a -> a.isConditionParameter())
                .count()) == 2;

        DAOPropagationState anatPropState = callDataTO.getDataPropagation()
                .get(ConditionDAO.Attribute.ANAT_ENTITY_ID);
        if (anatPropState == null) {
            stmt.setNull(newParamIndex, Types.VARCHAR);
            newParamIndex++;
        } else {
            stmt.setString(newParamIndex, anatPropState.getStringRepresentation());
            newParamIndex++;
        }
        DAOPropagationState stagePropState = callDataTO.getDataPropagation()
                .get(ConditionDAO.Attribute.STAGE_ID);
        if (stagePropState == null) {
            stmt.setNull(newParamIndex, Types.VARCHAR);
            newParamIndex++;
        } else {
            stmt.setString(newParamIndex, stagePropState.getStringRepresentation());
            newParamIndex++;
        }
        stmt.setBoolean(newParamIndex, callDataTO.isConditionObservedData());
        newParamIndex++;

        //** Experiment expression counts **
        //present high self
        stmt.setInt(newParamIndex, callDataTO.getExperimentCounts().stream()
                .filter(c -> DAOExperimentCount.CallType.PRESENT.equals(c.getCallType()) &&
                             DAOExperimentCount.DataQuality.HIGH.equals(c.getDataQuality()) &&
                             DAOPropagationState.SELF.equals(c.getPropagationState()))
                .findFirst().orElseThrow(() -> new IllegalArgumentException()).getCount());
        newParamIndex++;
        //present low self
        stmt.setInt(newParamIndex, callDataTO.getExperimentCounts().stream()
                .filter(c -> DAOExperimentCount.CallType.PRESENT.equals(c.getCallType()) &&
                             DAOExperimentCount.DataQuality.LOW.equals(c.getDataQuality()) &&
                             DAOPropagationState.SELF.equals(c.getPropagationState()))
                .findFirst().orElseThrow(() -> new IllegalArgumentException()).getCount());
        newParamIndex++;
        if (!DAODataType.EST.equals(dataType)) {
            //absent high self
            stmt.setInt(newParamIndex, callDataTO.getExperimentCounts().stream()
                    .filter(c -> DAOExperimentCount.CallType.ABSENT.equals(c.getCallType()) &&
                                 DAOExperimentCount.DataQuality.HIGH.equals(c.getDataQuality()) &&
                                 DAOPropagationState.SELF.equals(c.getPropagationState()))
                    .findFirst().orElseThrow(() -> new IllegalArgumentException()).getCount());
            newParamIndex++;
            //absent low self
            stmt.setInt(newParamIndex, callDataTO.getExperimentCounts().stream()
                    .filter(c -> DAOExperimentCount.CallType.ABSENT.equals(c.getCallType()) &&
                                 DAOExperimentCount.DataQuality.LOW.equals(c.getDataQuality()) &&
                                 DAOPropagationState.SELF.equals(c.getPropagationState()))
                    .findFirst().orElseThrow(() -> new IllegalArgumentException()).getCount());
            newParamIndex++;
        }
        //present high descendant
        stmt.setInt(newParamIndex, callDataTO.getExperimentCounts().stream()
                .filter(c -> DAOExperimentCount.CallType.PRESENT.equals(c.getCallType()) &&
                             DAOExperimentCount.DataQuality.HIGH.equals(c.getDataQuality()) &&
                             DAOPropagationState.DESCENDANT.equals(c.getPropagationState()))
                .findFirst().orElseThrow(() -> new IllegalArgumentException()).getCount());
        newParamIndex++;
        //present low descendant
        stmt.setInt(newParamIndex, callDataTO.getExperimentCounts().stream()
                .filter(c -> DAOExperimentCount.CallType.PRESENT.equals(c.getCallType()) &&
                             DAOExperimentCount.DataQuality.LOW.equals(c.getDataQuality()) &&
                             DAOPropagationState.DESCENDANT.equals(c.getPropagationState()))
                .findFirst().orElseThrow(() -> new IllegalArgumentException()).getCount());
        newParamIndex++;
        if (!DAODataType.EST.equals(dataType)) {
            //absent high parent
            stmt.setInt(newParamIndex, callDataTO.getExperimentCounts().stream()
                    .filter(c -> DAOExperimentCount.CallType.ABSENT.equals(c.getCallType()) &&
                                 DAOExperimentCount.DataQuality.HIGH.equals(c.getDataQuality()) &&
                                 DAOPropagationState.ANCESTOR.equals(c.getPropagationState()))
                    .findFirst().orElseThrow(() -> new IllegalArgumentException()).getCount());
            newParamIndex++;
            //absent low parent
            stmt.setInt(newParamIndex, callDataTO.getExperimentCounts().stream()
                    .filter(c -> DAOExperimentCount.CallType.ABSENT.equals(c.getCallType()) &&
                                 DAOExperimentCount.DataQuality.LOW.equals(c.getDataQuality()) &&
                                 DAOPropagationState.ANCESTOR.equals(c.getPropagationState()))
                    .findFirst().orElseThrow(() -> new IllegalArgumentException()).getCount());
            newParamIndex++;
        }
        //present high total
        stmt.setInt(newParamIndex, callDataTO.getExperimentCounts().stream()
                .filter(c -> DAOExperimentCount.CallType.PRESENT.equals(c.getCallType()) &&
                             DAOExperimentCount.DataQuality.HIGH.equals(c.getDataQuality()) &&
                             DAOPropagationState.ALL.equals(c.getPropagationState()))
                .findFirst().orElseThrow(() -> new IllegalArgumentException()).getCount());
        newParamIndex++;
        //present low total
        stmt.setInt(newParamIndex, callDataTO.getExperimentCounts().stream()
                .filter(c -> DAOExperimentCount.CallType.PRESENT.equals(c.getCallType()) &&
                             DAOExperimentCount.DataQuality.LOW.equals(c.getDataQuality()) &&
                             DAOPropagationState.ALL.equals(c.getPropagationState()))
                .findFirst().orElseThrow(() -> new IllegalArgumentException()).getCount());
        newParamIndex++;
        if (!DAODataType.EST.equals(dataType)) {
            //absent high total
            stmt.setInt(newParamIndex, callDataTO.getExperimentCounts().stream()
                    .filter(c -> DAOExperimentCount.CallType.ABSENT.equals(c.getCallType()) &&
                                 DAOExperimentCount.DataQuality.HIGH.equals(c.getDataQuality()) &&
                                 DAOPropagationState.ALL.equals(c.getPropagationState()))
                    .findFirst().orElseThrow(() -> new IllegalArgumentException()).getCount());
            newParamIndex++;
            //absent low total
            stmt.setInt(newParamIndex, callDataTO.getExperimentCounts().stream()
                    .filter(c -> DAOExperimentCount.CallType.ABSENT.equals(c.getCallType()) &&
                                 DAOExperimentCount.DataQuality.LOW.equals(c.getDataQuality()) &&
                                 DAOPropagationState.ALL.equals(c.getPropagationState()))
                    .findFirst().orElseThrow(() -> new IllegalArgumentException()).getCount());
            newParamIndex++;
        }
        //propagated count
        stmt.setInt(newParamIndex, callDataTO.getPropagatedCount());
        newParamIndex++;

        return log.exit(newParamIndex);
    }
    
    /**
     * Implementation of the {@code GlobalExpressionCallTOResultSet}. 
     * 
     * @author Frederic Bastian
     * @version Bgee 14 Feb. 2017
     * @since Bgee 14 Feb. 2017
     */
    static class MySQLGlobalExpressionCallTOResultSet extends MySQLDAOResultSet<GlobalExpressionCallDAO.GlobalExpressionCallTO>
            implements GlobalExpressionCallTOResultSet {

        private MySQLGlobalExpressionCallTOResultSet(BgeePreparedStatement statement) {
            super(statement);
        }

        @Override
        protected GlobalExpressionCallDAO.GlobalExpressionCallTO getNewTO() throws DAOException {
            try {
                log.entry();
                final ResultSet currentResultSet = this.getCurrentResultSet();
                Integer id = null, bgeeGeneId = null, conditionId = null;
                Set<GlobalExpressionCallDataTO> callDataTOs = new HashSet<>();
                BigDecimal globalMeanRank = null;

                Set<String> colNames = new HashSet<>(this.getColumnLabels().values());
                if (colNames.contains(GLOBAL_EXPR_ID_FIELD)) {
                    id = currentResultSet.getInt(GLOBAL_EXPR_ID_FIELD);
                }
                if (colNames.contains(MySQLGeneDAO.BGEE_GENE_ID)) {
                    bgeeGeneId = currentResultSet.getInt(MySQLGeneDAO.BGEE_GENE_ID);
                }
                if (colNames.contains(MySQLConditionDAO.GLOBAL_COND_ID_FIELD)) {
                    conditionId = currentResultSet.getInt(MySQLConditionDAO.GLOBAL_COND_ID_FIELD);
                }
                if (colNames.contains(GLOBAL_MEAN_RANK_FIELD)) {
                    globalMeanRank = currentResultSet.getBigDecimal(GLOBAL_MEAN_RANK_FIELD);
                }
                for (DAODataType dataType: EnumSet.allOf(DAODataType.class)) {
                    GlobalExpressionCallDataTO dataTypeDataTO = loadGlobalExpressionCallDataTO(
                            this, dataType);
                    if (dataTypeDataTO != null) {
                        callDataTOs.add(dataTypeDataTO);
                    }
                }
                return log.exit(new GlobalExpressionCallTO(id, bgeeGeneId, conditionId,
                        globalMeanRank, callDataTOs));
            } catch (SQLException e) {
                throw log.throwing(new DAOException(e));
            }
        }

        private static GlobalExpressionCallDataTO loadGlobalExpressionCallDataTO(
                MySQLGlobalExpressionCallTOResultSet rs, DAODataType dataType) throws SQLException {
            log.entry(rs, dataType);

            final ResultSet currentResultSet = rs.getCurrentResultSet();
            Boolean conditionObservedData = null;
            Map<ConditionDAO.Attribute, DAOPropagationState> dataPropagation = new HashMap<>();
            Set<DAOExperimentCount> experimentCounts = new HashSet<>();
            Integer propagatedCount = null;
            BigDecimal rank = null, rankNorm = null, weightForMeanRank = null;

            boolean infoFound = false;
            for (Map.Entry<Integer, String> col : rs.getColumnLabels().entrySet()) {
                final String columnName = col.getValue();
                if ("estAnatEntityPropagationState".equals(columnName) &&
                        DAODataType.EST.equals(dataType) ||
                    "affymetrixAnatEntityPropagationState".equals(columnName) &&
                        DAODataType.AFFYMETRIX.equals(dataType) ||
                    "inSituAnatEntityPropagationState".equals(columnName) &&
                        DAODataType.IN_SITU.equals(dataType) ||
                    "rnaSeqAnatEntityPropagationState".equals(columnName) &&
                        DAODataType.RNA_SEQ.equals(dataType)) {

                    dataPropagation.put(ConditionDAO.Attribute.ANAT_ENTITY_ID,
                            DAOPropagationState.convertToPropagationState(
                                    currentResultSet.getString(columnName)));
                    infoFound = true;
                } else if ("estStagePropagationState".equals(columnName) &&
                        DAODataType.EST.equals(dataType) ||
                    "affymetrixStagePropagationState".equals(columnName) &&
                        DAODataType.AFFYMETRIX.equals(dataType) ||
                    "inSituStagePropagationState".equals(columnName) &&
                        DAODataType.IN_SITU.equals(dataType) ||
                    "rnaSeqStagePropagationState".equals(columnName) &&
                        DAODataType.RNA_SEQ.equals(dataType)) {

                    dataPropagation.put(ConditionDAO.Attribute.STAGE_ID,
                            DAOPropagationState.convertToPropagationState(
                                    currentResultSet.getString(columnName)));
                    infoFound = true;
                } else if ("estConditionObservedData".equals(columnName) &&
                        DAODataType.EST.equals(dataType) ||
                    "affymetrixConditionObservedData".equals(columnName) &&
                        DAODataType.AFFYMETRIX.equals(dataType) ||
                    "inSituConditionObservedData".equals(columnName) &&
                        DAODataType.IN_SITU.equals(dataType) ||
                    "rnaSeqConditionObservedData".equals(columnName) &&
                        DAODataType.RNA_SEQ.equals(dataType)) {

                    // As getBoolean() returns false if the value is SQL NULL, 
                    // we need to check if the column read had a value of SQL NULL
                    boolean isConditionObservedData = currentResultSet.getBoolean(columnName);
                    if (!currentResultSet.wasNull()) {
                        conditionObservedData = isConditionObservedData;
                    }
                    infoFound = true;
                } else if ("estLibPropagatedCount".equals(columnName) &&
                        DAODataType.EST.equals(dataType) ||
                    "affymetrixExpPropagatedCount".equals(columnName) &&
                        DAODataType.AFFYMETRIX.equals(dataType) ||
                    "inSituExpPropagatedCount".equals(columnName) &&
                        DAODataType.IN_SITU.equals(dataType) ||
                    "rnaSeqExpPropagatedCount".equals(columnName) &&
                        DAODataType.RNA_SEQ.equals(dataType)) {

                    // getInt() returns 0 if the value is SQL NULL,
                    // but in db, propagated counts are not null so we do not need to check
                    // if the column read had a value of SQL NULL
                    propagatedCount = currentResultSet.getInt(columnName);
                    infoFound = true;
                } else if ("estRank".equals(columnName) &&
                        DAODataType.EST.equals(dataType) ||
                    "affymetrixMeanRank".equals(columnName) &&
                        DAODataType.AFFYMETRIX.equals(dataType) ||
                    "inSituRank".equals(columnName) &&
                        DAODataType.IN_SITU.equals(dataType) ||
                    "rnaSeqMeanRank".equals(columnName) &&
                        DAODataType.RNA_SEQ.equals(dataType)) {

                    rank = currentResultSet.getBigDecimal(columnName);
                    infoFound = true;
                } else if ("estRankNorm".equals(columnName) &&
                        DAODataType.EST.equals(dataType) ||
                    "affymetrixMeanRankNorm".equals(columnName) &&
                        DAODataType.AFFYMETRIX.equals(dataType) ||
                    "inSituRankNorm".equals(columnName) &&
                        DAODataType.IN_SITU.equals(dataType) ||
                    "rnaSeqMeanRankNorm".equals(columnName) &&
                        DAODataType.RNA_SEQ.equals(dataType)) {

                    rankNorm = currentResultSet.getBigDecimal(columnName);
                    infoFound = true;
                } else if ("affymetrixDistinctRankSum".equals(columnName) &&
                        DAODataType.AFFYMETRIX.equals(dataType) ||
                    "rnaSeqDistinctRankSum".equals(columnName) &&
                        DAODataType.RNA_SEQ.equals(dataType)) {

                    weightForMeanRank = currentResultSet.getBigDecimal(columnName);
                    infoFound = true;
                } else {
                    DAOExperimentCount count = loadExperimentCount(currentResultSet,
                            columnName, dataType);
                    if (count != null) {
                        experimentCounts.add(count);
                        infoFound = true;
                    }
                }
            }
            if (!infoFound || (conditionObservedData == null
                    && (dataPropagation.isEmpty() || dataPropagation.values().stream().allMatch(dp -> dp == null))
                    && (experimentCounts.isEmpty() || experimentCounts.stream().allMatch(c -> c.getCount() == 0))
                    && (propagatedCount == null || propagatedCount == 0)
                    && rank == null && rankNorm == null && weightForMeanRank == null)) {
                // If all variables are null/empty/0, this means that there is no data for the current data type
                return log.exit(null);
            }
            return log.exit(new GlobalExpressionCallDataTO(dataType, conditionObservedData,
                    dataPropagation, experimentCounts, propagatedCount,
                    rank, rankNorm, weightForMeanRank));
        }
        
        private static DAOExperimentCount loadExperimentCount(ResultSet rs,
                final String columnName, DAODataType dataType) throws SQLException {
            log.entry(rs, columnName, dataType);
            
            if ("estLibPresentHighSelfCount".equals(columnName) && DAODataType.EST.equals(dataType) ||
            "affymetrixExpPresentHighSelfCount".equals(columnName) && DAODataType.AFFYMETRIX.equals(dataType) ||
            "inSituExpPresentHighSelfCount".equals(columnName) && DAODataType.IN_SITU.equals(dataType) ||
            "rnaSeqExpPresentHighSelfCount".equals(columnName) && DAODataType.RNA_SEQ.equals(dataType)) {
                return log.exit(new DAOExperimentCount(
                        DAOExperimentCount.CallType.PRESENT,
                        DAOExperimentCount.DataQuality.HIGH,
                        DAOPropagationState.SELF,
                        rs.getInt(columnName)));
            }
            if ("estLibPresentLowSelfCount".equals(columnName) && DAODataType.EST.equals(dataType) ||
            "affymetrixExpPresentLowSelfCount".equals(columnName) && DAODataType.AFFYMETRIX.equals(dataType) ||
            "inSituExpPresentLowSelfCount".equals(columnName) && DAODataType.IN_SITU.equals(dataType) ||
            "rnaSeqExpPresentLowSelfCount".equals(columnName) && DAODataType.RNA_SEQ.equals(dataType)) {
                return log.exit(new DAOExperimentCount(
                        DAOExperimentCount.CallType.PRESENT,
                        DAOExperimentCount.DataQuality.LOW,
                        DAOPropagationState.SELF,
                        rs.getInt(columnName)));
            }
            if ("affymetrixExpAbsentHighSelfCount".equals(columnName) && DAODataType.AFFYMETRIX.equals(dataType) ||
            "inSituExpAbsentHighSelfCount".equals(columnName) && DAODataType.IN_SITU.equals(dataType) ||
            "rnaSeqExpAbsentHighSelfCount".equals(columnName) && DAODataType.RNA_SEQ.equals(dataType)) {
                return log.exit(new DAOExperimentCount(
                        DAOExperimentCount.CallType.ABSENT,
                        DAOExperimentCount.DataQuality.HIGH,
                        DAOPropagationState.SELF,
                        rs.getInt(columnName)));
            }
            if ("affymetrixExpAbsentLowSelfCount".equals(columnName) && DAODataType.AFFYMETRIX.equals(dataType) ||
            "inSituExpAbsentLowSelfCount".equals(columnName) && DAODataType.IN_SITU.equals(dataType) ||
            "rnaSeqExpAbsentLowSelfCount".equals(columnName) && DAODataType.RNA_SEQ.equals(dataType)) {
                return log.exit(new DAOExperimentCount(
                        DAOExperimentCount.CallType.ABSENT,
                        DAOExperimentCount.DataQuality.LOW,
                        DAOPropagationState.SELF,
                        rs.getInt(columnName)));
            }
            if ("estLibPresentHighDescendantCount".equals(columnName) && DAODataType.EST.equals(dataType) ||
            "affymetrixExpPresentHighDescendantCount".equals(columnName) && DAODataType.AFFYMETRIX.equals(dataType) ||
            "inSituExpPresentHighDescendantCount".equals(columnName) && DAODataType.IN_SITU.equals(dataType) ||
            "rnaSeqExpPresentHighDescendantCount".equals(columnName) && DAODataType.RNA_SEQ.equals(dataType)) {
                return log.exit(new DAOExperimentCount(
                        DAOExperimentCount.CallType.PRESENT,
                        DAOExperimentCount.DataQuality.HIGH,
                        DAOPropagationState.DESCENDANT,
                        rs.getInt(columnName)));
            }
            if ("estLibPresentLowDescendantCount".equals(columnName) && DAODataType.EST.equals(dataType) ||
            "affymetrixExpPresentLowDescendantCount".equals(columnName) && DAODataType.AFFYMETRIX.equals(dataType) ||
            "inSituExpPresentLowDescendantCount".equals(columnName) && DAODataType.IN_SITU.equals(dataType) ||
            "rnaSeqExpPresentLowDescendantCount".equals(columnName) && DAODataType.RNA_SEQ.equals(dataType)) {
                return log.exit(new DAOExperimentCount(
                        DAOExperimentCount.CallType.PRESENT,
                        DAOExperimentCount.DataQuality.LOW,
                        DAOPropagationState.DESCENDANT,
                        rs.getInt(columnName)));
            }
            if ("affymetrixExpAbsentHighParentCount".equals(columnName) && DAODataType.AFFYMETRIX.equals(dataType) ||
            "inSituExpAbsentHighParentCount".equals(columnName) && DAODataType.IN_SITU.equals(dataType) ||
            "rnaSeqExpAbsentHighParentCount".equals(columnName) && DAODataType.RNA_SEQ.equals(dataType)) {
                return log.exit(new DAOExperimentCount(
                        DAOExperimentCount.CallType.ABSENT,
                        DAOExperimentCount.DataQuality.HIGH,
                        DAOPropagationState.ANCESTOR,
                        rs.getInt(columnName)));
            }
            if ("affymetrixExpAbsentLowParentCount".equals(columnName) && DAODataType.AFFYMETRIX.equals(dataType) ||
            "inSituExpAbsentLowParentCount".equals(columnName) && DAODataType.IN_SITU.equals(dataType) ||
            "rnaSeqExpAbsentLowParentCount".equals(columnName) && DAODataType.RNA_SEQ.equals(dataType)) {
                return log.exit(new DAOExperimentCount(
                        DAOExperimentCount.CallType.ABSENT,
                        DAOExperimentCount.DataQuality.LOW,
                        DAOPropagationState.ANCESTOR,
                        rs.getInt(columnName)));
            }
            if ("estLibPresentHighTotalCount".equals(columnName) && DAODataType.EST.equals(dataType) ||
            "affymetrixExpPresentHighTotalCount".equals(columnName) && DAODataType.AFFYMETRIX.equals(dataType) ||
            "inSituExpPresentHighTotalCount".equals(columnName) && DAODataType.IN_SITU.equals(dataType) ||
            "rnaSeqExpPresentHighTotalCount".equals(columnName) && DAODataType.RNA_SEQ.equals(dataType)) {
                return log.exit(new DAOExperimentCount(
                        DAOExperimentCount.CallType.PRESENT,
                        DAOExperimentCount.DataQuality.HIGH,
                        DAOPropagationState.ALL,
                        rs.getInt(columnName)));
            }
            if ("estLibPresentLowTotalCount".equals(columnName) && DAODataType.EST.equals(dataType) ||
            "affymetrixExpPresentLowTotalCount".equals(columnName) && DAODataType.AFFYMETRIX.equals(dataType) ||
            "inSituExpPresentLowTotalCount".equals(columnName) && DAODataType.IN_SITU.equals(dataType) ||
            "rnaSeqExpPresentLowTotalCount".equals(columnName) && DAODataType.RNA_SEQ.equals(dataType)) {
                return log.exit(new DAOExperimentCount(
                        DAOExperimentCount.CallType.PRESENT,
                        DAOExperimentCount.DataQuality.LOW,
                        DAOPropagationState.ALL,
                        rs.getInt(columnName)));
            }
            if ("affymetrixExpAbsentHighTotalCount".equals(columnName) && DAODataType.AFFYMETRIX.equals(dataType) ||
            "inSituExpAbsentHighTotalCount".equals(columnName) && DAODataType.IN_SITU.equals(dataType) ||
            "rnaSeqExpAbsentHighTotalCount".equals(columnName) && DAODataType.RNA_SEQ.equals(dataType)) {
                return log.exit(new DAOExperimentCount(
                        DAOExperimentCount.CallType.ABSENT,
                        DAOExperimentCount.DataQuality.HIGH,
                        DAOPropagationState.ALL,
                        rs.getInt(columnName)));
            }
            if ("affymetrixExpAbsentLowTotalCount".equals(columnName) && DAODataType.AFFYMETRIX.equals(dataType) ||
            "inSituExpAbsentLowTotalCount".equals(columnName) && DAODataType.IN_SITU.equals(dataType) ||
            "rnaSeqExpAbsentLowTotalCount".equals(columnName) && DAODataType.RNA_SEQ.equals(dataType)) {
                return log.exit(new DAOExperimentCount(
                        DAOExperimentCount.CallType.ABSENT,
                        DAOExperimentCount.DataQuality.LOW,
                        DAOPropagationState.ALL,
                        rs.getInt(columnName)));
            }
            
            return log.exit(null);
        }
    }
}
