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
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.expressiondata.CallDAOFilter;
import org.bgee.model.dao.api.expressiondata.ConditionDAO;
import org.bgee.model.dao.api.expressiondata.DAODataType;
import org.bgee.model.dao.api.expressiondata.DAOExperimentCount;
import org.bgee.model.dao.api.expressiondata.DAOPropagationState;
import org.bgee.model.dao.api.expressiondata.GlobalExpressionCallDAO;
import org.bgee.model.dao.api.expressiondata.GlobalExpressionCallDAO.GlobalExpressionToRawExpressionTO.CallOrigin;
import org.bgee.model.dao.mysql.MySQLDAO;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.model.dao.mysql.connector.MySQLDAOResultSet;
import org.bgee.model.dao.mysql.exception.UnrecognizedColumnException;
import org.bgee.model.dao.mysql.gene.MySQLGeneDAO;

/**
 * A {@code GlobalExpressionCallDAO} for MySQL. 
 * 
 * @author Frederic Bastian
 * @version Bgee 14 Feb. 2017
 * @see org.bgee.model.dao.api.expressiondata.GlobalExpressionCallDAO.GlobalExpressionCallTO
 * @see org.bgee.model.dao.api.expressiondata.GlobalExpressionCallDAO.GlobalExpressionToRawExpressionTO
 * @since Bgee 14 Feb. 2017
 */
public class MySQLGlobalExpressionCallDAO extends MySQLDAO<GlobalExpressionCallDAO.Attribute> 
implements GlobalExpressionCallDAO {
    private final static Logger log = LogManager.getLogger(MySQLGlobalExpressionCallDAO.class.getName());
    
    private final static String CALL_ORIGIN_FIELD = "callOrigin";
    private final static String GLOBAL_EXPR_ID_FIELD = "globalExpressionId";
    private final static String GLOBAL_EXPR_TABLE_NAME = "globalExpression";
    private final static String GLOBAL_MEAN_RANK_FIELD = "meanRank";

    private static String generateSelectClause(Collection<GlobalExpressionCallDAO.Attribute> attrs,
            Collection<DAODataType> dataTypes, final String globalExprTableName) {
        log.entry(attrs, dataTypes, globalExprTableName);
        
        Set<GlobalExpressionCallDAO.Attribute> clonedAttrs = Collections.unmodifiableSet(
                attrs == null || attrs.isEmpty()? EnumSet.allOf(GlobalExpressionCallDAO.Attribute.class):
                    EnumSet.copyOf(attrs));
        Set<DAODataType> clonedDataTypes = Collections.unmodifiableSet(
                dataTypes == null || dataTypes.isEmpty()? EnumSet.allOf(DAODataType.class):
                    EnumSet.copyOf(dataTypes));

        StringBuilder sb = new StringBuilder();
        sb.append("SELECT ");

        if (clonedAttrs.size() == GlobalExpressionCallDAO.Attribute.values().length &&
                clonedDataTypes.size() == DAODataType.values().length) {
            sb.append(globalExprTableName).append(".*");
            return log.exit(sb.toString());
        }

        if (!clonedAttrs.contains(GlobalExpressionCallDAO.Attribute.ID) &&
                (!clonedAttrs.contains(GlobalExpressionCallDAO.Attribute.BGEE_GENE_ID) ||
                        !clonedAttrs.contains(GlobalExpressionCallDAO.Attribute.CONDITION_ID))) {
            sb.append("DISTINCT ");
        }

        sb.append(clonedAttrs.stream().map(a -> {
            switch (a) {
            case ID:
                return globalExprTableName + "." + GLOBAL_EXPR_ID_FIELD;
            case BGEE_GENE_ID:
                return globalExprTableName + "." + MySQLGeneDAO.BGEE_GENE_ID;
            case CONDITION_ID:
                return globalExprTableName + "." + MySQLConditionDAO.GLOBAL_COND_ID_FIELD;
            case GLOBAL_MEAN_RANK:
                //use GLOBAL_MEAN_RANK_FIELD
            case DATA_TYPE_OBSERVED_DATA:
            case DATA_TYPE_EXPERIMENT_TOTAL_COUNTS:
            case DATA_TYPE_EXPERIMENT_SELF_COUNTS:
            case DATA_TYPE_EXPERIMENT_PROPAGATED_COUNTS:
            case DATA_TYPE_RANK_INFO:
                throw new UnsupportedOperationException("Not yet implemented");
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

    public MySQLGlobalExpressionCallDAO(MySQLDAOManager manager) throws IllegalArgumentException {
        super(manager);
    }

    @Override
    public GlobalExpressionCallTOResultSet getGlobalExpressionCalls(
            Collection<CallDAOFilter> callFilters, Collection<ConditionDAO.Attribute> conditionParameters,
            Collection<GlobalExpressionCallDAO.Attribute> attributes,
            LinkedHashMap<GlobalExpressionCallDAO.OrderingAttribute, DAO.Direction> orderingAttributes)
                    throws DAOException, IllegalArgumentException {
        log.entry(callFilters, callFilters, conditionParameters, attributes);
        throw log.throwing(new UnsupportedOperationException("Load of global calls not implemented yet"));
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

    @Override
    public int insertGlobalExpressionToRawExpression(
            Collection<GlobalExpressionToRawExpressionTO> globalExprToRawExprTOs)
                    throws DAOException, IllegalArgumentException {
        log.entry(globalExprToRawExprTOs);
        
        if (globalExprToRawExprTOs == null || globalExprToRawExprTOs.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("No expression relation provided"));
        }
        
        StringBuilder sql = new StringBuilder(); 
        sql.append("INSERT INTO globalExpressionToExpression (")
           .append(MySQLRawExpressionCallDAO.EXPR_ID_FIELD).append(", ")
           .append(GLOBAL_EXPR_ID_FIELD).append(", ")
           .append(CALL_ORIGIN_FIELD)
           .append(") VALUES ");
        for (int i = 0; i < globalExprToRawExprTOs.size(); i++) {
            if (i > 0) {
                sql.append(", ");
            }
            sql.append("(").append(BgeePreparedStatement.generateParameterizedQueryString(3))
               .append(") ");
        }
        try (BgeePreparedStatement stmt = 
                this.getManager().getConnection().prepareStatement(sql.toString())) {
            int paramIndex = 1;
            for (GlobalExpressionToRawExpressionTO to: globalExprToRawExprTOs) {
                stmt.setInt(paramIndex, to.getRawExpressionId());
                paramIndex++;
                stmt.setInt(paramIndex, to.getGlobalExpressionId());
                paramIndex++;
                stmt.setString(paramIndex, to.getCallOrigin().getStringRepresentation());
                paramIndex++;
            }
            
            return log.exit(stmt.executeUpdate());
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
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

                    conditionObservedData = currentResultSet.getBoolean(columnName);
                    infoFound = true;
                } else if ("estLibPropagatedCount".equals(columnName) &&
                        DAODataType.EST.equals(dataType) ||
                    "affymetrixExpPropagatedCount".equals(columnName) &&
                        DAODataType.AFFYMETRIX.equals(dataType) ||
                    "inSituExpPropagatedCount".equals(columnName) &&
                        DAODataType.IN_SITU.equals(dataType) ||
                    "rnaSeqExpPropagatedCount".equals(columnName) &&
                        DAODataType.RNA_SEQ.equals(dataType)) {

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
            if (!infoFound) {
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
    
    /**
     * MySQL implementation of {@code GlobalExpressionToRawExpressionTOResultSet}.
     * 
     * @author Frederic Bastian
     * @version Bgee 14 Feb. 2017
     * @since Bgee 14 Feb. 2017
     */
    public class MySQLGlobalExpressionToRawExpressionTOResultSet extends MySQLDAOResultSet<GlobalExpressionToRawExpressionTO> 
    implements GlobalExpressionToRawExpressionTOResultSet {

        private MySQLGlobalExpressionToRawExpressionTOResultSet(BgeePreparedStatement statement) {
            super(statement);
        }
        
        @Override
        protected GlobalExpressionToRawExpressionTO getNewTO() throws DAOException {
            log.entry();
            try {
                final ResultSet currentResultSet = this.getCurrentResultSet();
                Integer rawExpressionId = null, globalExpressionId = null;
                CallOrigin callOrigin = null;
                
                for (Entry<Integer, String> column: this.getColumnLabels().entrySet()) {
                    String columnName = column.getValue();
                    
                    if (columnName.equals(MySQLRawExpressionCallDAO.EXPR_ID_FIELD)) {
                        rawExpressionId = currentResultSet.getInt(columnName);
                    } else if (columnName.equals(GLOBAL_EXPR_ID_FIELD)) {
                        globalExpressionId = currentResultSet.getInt(columnName);
                    } else if (columnName.equals(CALL_ORIGIN_FIELD)) {
                        callOrigin = CallOrigin.convertToCallOrigin(
                                currentResultSet.getString(columnName));
                    }  else {
                        throw log.throwing(new UnrecognizedColumnException(columnName));
                    }
                }
                
                return log.exit(new GlobalExpressionToRawExpressionTO(
                        rawExpressionId, globalExpressionId, callOrigin));
            } catch (SQLException e) {
                throw log.throwing(new DAOException(e));
            }
        }
    }
}
