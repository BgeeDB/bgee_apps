package org.bgee.model.dao.mysql.expressiondata;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.expressiondata.ConditionDAO;
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

    /**
     * Get a {@code Map} associating column names to corresponding {@code GlobalExpressionCallDAO.Attribute}.
     * 
     * @param comb  The {@code CondParamCombination} allowing to target the appropriate 
     *              field and table names.
     * @return      A {@code Map} where keys are {@code String}s that are column names, 
     *              the associated value being the corresponding {@code GlobalExpressionCallDAO.Attribute}.
     */
    private static Map<String, GlobalExpressionCallDAO.Attribute> getColToAttributesMap(
            CondParamCombination comb) {
        log.entry(comb);
        Map<String, GlobalExpressionCallDAO.Attribute> colToAttributesMap = new HashMap<>();
        colToAttributesMap.put(comb.getGlobalExprIdField(), GlobalExpressionCallDAO.Attribute.ID);
        colToAttributesMap.put(MySQLGeneDAO.BGEE_GENE_ID, GlobalExpressionCallDAO.Attribute.BGEE_GENE_ID);
        colToAttributesMap.put(comb.getCondIdField(), GlobalExpressionCallDAO.Attribute.CONDITION_ID);
        
        colToAttributesMap.put("globalMeanRank", GlobalExpressionCallDAO.Attribute.GLOBAL_MEAN_RANK);
        
        colToAttributesMap.put("affymetrixExpPresentHighSelfCount", 
                GlobalExpressionCallDAO.Attribute.AFFYMETRIX_EXP_PRESENT_HIGH_SELF_COUNT);
        colToAttributesMap.put("affymetrixExpPresentLowSelfCount", 
                GlobalExpressionCallDAO.Attribute.AFFYMETRIX_EXP_PRESENT_LOW_SELF_COUNT);
        colToAttributesMap.put("affymetrixExpAbsentHighSelfCount", 
                GlobalExpressionCallDAO.Attribute.AFFYMETRIX_EXP_ABSENT_HIGH_SELF_COUNT);
        colToAttributesMap.put("affymetrixExpAbsentLowSelfCount", 
                GlobalExpressionCallDAO.Attribute.AFFYMETRIX_EXP_ABSENT_LOW_SELF_COUNT);
        colToAttributesMap.put("affymetrixExpPresentHighDescendantCount", 
                GlobalExpressionCallDAO.Attribute.AFFYMETRIX_EXP_PRESENT_HIGH_DESCENDANT_COUNT);
        colToAttributesMap.put("affymetrixExpPresentLowDescendantCount", 
                GlobalExpressionCallDAO.Attribute.AFFYMETRIX_EXP_PRESENT_LOW_DESCENDANT_COUNT);
        colToAttributesMap.put("affymetrixExpAbsentHighParentCount", 
                GlobalExpressionCallDAO.Attribute.AFFYMETRIX_EXP_ABSENT_HIGH_PARENT_COUNT);
        colToAttributesMap.put("affymetrixExpAbsentLowParentCount", 
                GlobalExpressionCallDAO.Attribute.AFFYMETRIX_EXP_ABSENT_LOW_PARENT_COUNT);
        colToAttributesMap.put("affymetrixExpPresentHighTotalCount", 
                GlobalExpressionCallDAO.Attribute.AFFYMETRIX_EXP_PRESENT_HIGH_TOTAL_COUNT);
        colToAttributesMap.put("affymetrixExpPresentLowTotalCount", 
                GlobalExpressionCallDAO.Attribute.AFFYMETRIX_EXP_PRESENT_LOW_TOTAL_COUNT);
        colToAttributesMap.put("affymetrixExpAbsentHighTotalCount", 
                GlobalExpressionCallDAO.Attribute.AFFYMETRIX_EXP_ABSENT_HIGH_TOTAL_COUNT);
        colToAttributesMap.put("affymetrixExpAbsentLowTotalCount", 
                GlobalExpressionCallDAO.Attribute.AFFYMETRIX_EXP_ABSENT_LOW_TOTAL_COUNT);
        colToAttributesMap.put("affymetrixExpPropagatedCount", 
                GlobalExpressionCallDAO.Attribute.AFFYMETRIX_EXP_PROPAGATED_COUNT);
        
        colToAttributesMap.put("rnaSeqExpPresentHighSelfCount", 
                GlobalExpressionCallDAO.Attribute.RNA_SEQ_EXP_PRESENT_HIGH_SELF_COUNT);
        colToAttributesMap.put("rnaSeqExpPresentLowSelfCount", 
                GlobalExpressionCallDAO.Attribute.RNA_SEQ_EXP_PRESENT_LOW_SELF_COUNT);
        colToAttributesMap.put("rnaSeqExpAbsentHighSelfCount", 
                GlobalExpressionCallDAO.Attribute.RNA_SEQ_EXP_ABSENT_HIGH_SELF_COUNT);
        colToAttributesMap.put("rnaSeqExpAbsentLowSelfCount", 
                GlobalExpressionCallDAO.Attribute.RNA_SEQ_EXP_ABSENT_LOW_SELF_COUNT);
        colToAttributesMap.put("rnaSeqExpPresentHighDescendantCount", 
                GlobalExpressionCallDAO.Attribute.RNA_SEQ_EXP_PRESENT_HIGH_DESCENDANT_COUNT);
        colToAttributesMap.put("rnaSeqExpPresentLowDescendantCount", 
                GlobalExpressionCallDAO.Attribute.RNA_SEQ_EXP_PRESENT_LOW_DESCENDANT_COUNT);
        colToAttributesMap.put("rnaSeqExpAbsentHighParentCount", 
                GlobalExpressionCallDAO.Attribute.RNA_SEQ_EXP_ABSENT_HIGH_PARENT_COUNT);
        colToAttributesMap.put("rnaSeqExpAbsentLowParentCount", 
                GlobalExpressionCallDAO.Attribute.RNA_SEQ_EXP_ABSENT_LOW_PARENT_COUNT);
        colToAttributesMap.put("rnaSeqExpPresentHighTotalCount", 
                GlobalExpressionCallDAO.Attribute.RNA_SEQ_EXP_PRESENT_HIGH_TOTAL_COUNT);
        colToAttributesMap.put("rnaSeqExpPresentLowTotalCount", 
                GlobalExpressionCallDAO.Attribute.RNA_SEQ_EXP_PRESENT_LOW_TOTAL_COUNT);
        colToAttributesMap.put("rnaSeqExpAbsentHighTotalCount", 
                GlobalExpressionCallDAO.Attribute.RNA_SEQ_EXP_ABSENT_HIGH_TOTAL_COUNT);
        colToAttributesMap.put("rnaSeqExpAbsentLowTotalCount", 
                GlobalExpressionCallDAO.Attribute.RNA_SEQ_EXP_ABSENT_LOW_TOTAL_COUNT);
        colToAttributesMap.put("rnaSeqExpPropagatedCount", 
                GlobalExpressionCallDAO.Attribute.RNA_SEQ_EXP_PROPAGATED_COUNT);
        
        colToAttributesMap.put("estLibPresentHighSelfCount", 
                GlobalExpressionCallDAO.Attribute.EST_LIB_PRESENT_HIGH_SELF_COUNT);
        colToAttributesMap.put("estLibPresentLowSelfCount", 
                GlobalExpressionCallDAO.Attribute.EST_LIB_PRESENT_LOW_SELF_COUNT);
        colToAttributesMap.put("estLibPresentHighDescendantCount", 
                GlobalExpressionCallDAO.Attribute.EST_LIB_PRESENT_HIGH_DESCENDANT_COUNT);
        colToAttributesMap.put("estLibPresentLowDescendantCount", 
                GlobalExpressionCallDAO.Attribute.EST_LIB_PRESENT_LOW_DESCENDANT_COUNT);
        colToAttributesMap.put("estLibPresentHighTotalCount", 
                GlobalExpressionCallDAO.Attribute.EST_LIB_PRESENT_HIGH_TOTAL_COUNT);
        colToAttributesMap.put("estLibPresentLowTotalCount", 
                GlobalExpressionCallDAO.Attribute.EST_LIB_PRESENT_LOW_TOTAL_COUNT);
        colToAttributesMap.put("estLibPropagatedCount", 
                GlobalExpressionCallDAO.Attribute.EST_LIB_PROPAGATED_COUNT);
        
        colToAttributesMap.put("inSituExpPresentHighSelfCount", 
                GlobalExpressionCallDAO.Attribute.IN_SITU_EXP_PRESENT_HIGH_SELF_COUNT);
        colToAttributesMap.put("inSituExpPresentLowSelfCount", 
                GlobalExpressionCallDAO.Attribute.IN_SITU_EXP_PRESENT_LOW_SELF_COUNT);
        colToAttributesMap.put("inSituExpAbsentHighSelfCount", 
                GlobalExpressionCallDAO.Attribute.IN_SITU_EXP_ABSENT_HIGH_SELF_COUNT);
        colToAttributesMap.put("inSituExpAbsentLowSelfCount", 
                GlobalExpressionCallDAO.Attribute.IN_SITU_EXP_ABSENT_LOW_SELF_COUNT);
        colToAttributesMap.put("inSituExpPresentHighDescendantCount", 
                GlobalExpressionCallDAO.Attribute.IN_SITU_EXP_PRESENT_HIGH_DESCENDANT_COUNT);
        colToAttributesMap.put("inSituExpPresentLowDescendantCount", 
                GlobalExpressionCallDAO.Attribute.IN_SITU_EXP_PRESENT_LOW_DESCENDANT_COUNT);
        colToAttributesMap.put("inSituExpAbsentHighParentCount", 
                GlobalExpressionCallDAO.Attribute.IN_SITU_EXP_ABSENT_HIGH_PARENT_COUNT);
        colToAttributesMap.put("inSituExpAbsentLowParentCount", 
                GlobalExpressionCallDAO.Attribute.IN_SITU_EXP_ABSENT_LOW_PARENT_COUNT);
        colToAttributesMap.put("inSituExpPresentHighTotalCount", 
                GlobalExpressionCallDAO.Attribute.IN_SITU_EXP_PRESENT_HIGH_TOTAL_COUNT);
        colToAttributesMap.put("inSituExpPresentLowTotalCount", 
                GlobalExpressionCallDAO.Attribute.IN_SITU_EXP_PRESENT_LOW_TOTAL_COUNT);
        colToAttributesMap.put("inSituExpAbsentHighTotalCount", 
                GlobalExpressionCallDAO.Attribute.IN_SITU_EXP_ABSENT_HIGH_TOTAL_COUNT);
        colToAttributesMap.put("inSituExpAbsentLowTotalCount", 
                GlobalExpressionCallDAO.Attribute.IN_SITU_EXP_ABSENT_LOW_TOTAL_COUNT);
        colToAttributesMap.put("inSituExpPropagatedCount", 
                GlobalExpressionCallDAO.Attribute.IN_SITU_EXP_PROPAGATED_COUNT);

        colToAttributesMap.put("affymetrixMeanRank", GlobalExpressionCallDAO.Attribute.AFFYMETRIX_MEAN_RANK);
        colToAttributesMap.put("rnaSeqMeanRank", GlobalExpressionCallDAO.Attribute.RNA_SEQ_MEAN_RANK);
        colToAttributesMap.put("estRank", GlobalExpressionCallDAO.Attribute.EST_RANK);
        colToAttributesMap.put("inSituRank", GlobalExpressionCallDAO.Attribute.IN_SITU_RANK);
        colToAttributesMap.put("affymetrixMeanRankNorm", 
                GlobalExpressionCallDAO.Attribute.AFFYMETRIX_MEAN_RANK_NORM);
        colToAttributesMap.put("rnaSeqMeanRankNorm", 
                GlobalExpressionCallDAO.Attribute.RNA_SEQ_MEAN_RANK_NORM);
        colToAttributesMap.put("estRankNorm", GlobalExpressionCallDAO.Attribute.EST_RANK_NORM);
        colToAttributesMap.put("inSituRankNorm", GlobalExpressionCallDAO.Attribute.IN_SITU_RANK_NORM);
        colToAttributesMap.put("affymetrixDistinctRankSum", 
                GlobalExpressionCallDAO.Attribute.AFFYMETRIX_DISTINCT_RANK_SUM);
        colToAttributesMap.put("rnaSeqDistinctRankSum", 
                GlobalExpressionCallDAO.Attribute.RNA_SEQ_DISTINCT_RANK_SUM);
        
        return log.exit(colToAttributesMap);
    }

    public MySQLGlobalExpressionCallDAO(MySQLDAOManager manager) throws IllegalArgumentException {
        super(manager);
    }

    @Override
    public int getMaxGlobalExprId(Collection<ConditionDAO.Attribute> conditionParameters)
            throws DAOException, IllegalArgumentException {
        log.entry(conditionParameters);
        
        CondParamCombination comb = CondParamCombination.getCombination(conditionParameters);

        String sql = "SELECT MAX(" + comb.getGlobalExprIdField() + ") AS " + comb.getGlobalExprIdField() 
            + " FROM " + comb.getGlobalExprTable();
    
        try (GlobalExpressionCallTOResultSet resultSet = new MySQLGlobalExpressionCallTOResultSet(
                this.getManager().getConnection().prepareStatement(sql), comb)) {
            
            if (resultSet.next() && resultSet.getTO().getId() != null) {
                return log.exit(resultSet.getTO().getId());
            } 
            return log.exit(0);
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
    }

    @Override
    public int insertGlobalCalls(Collection<GlobalExpressionCallTO> callTOs, 
            Collection<ConditionDAO.Attribute> conditionParameters) throws DAOException, IllegalArgumentException {
        log.entry(callTOs, conditionParameters);

        CondParamCombination comb = CondParamCombination.getCombination(conditionParameters);
        
        if (callTOs == null || callTOs.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("No calls provided"));
        }
        
        final Map<String, GlobalExpressionCallDAO.Attribute> colToAttrMap = getColToAttributesMap(comb);
        //The order of the parameters is important for generating the query and then setting the parameters.
        List<GlobalExpressionCallDAO.Attribute> toPopulate = 
                EnumSet.allOf(GlobalExpressionCallDAO.Attribute.class).stream()
                //globalMeanRank is not a column in the table, but a select expression
                //computed on the fly in SELECT queries
                .filter(a -> !GlobalExpressionCallDAO.Attribute.GLOBAL_MEAN_RANK.equals(a))
                .collect(Collectors.toList());

        StringBuilder sql = new StringBuilder(); 
        sql.append("INSERT INTO ").append(comb.getGlobalExprTable()).append(" (")
           .append(toPopulate.stream().map(a -> getSelectExprFromAttribute(a, colToAttrMap))
                          .collect(Collectors.joining(", ")))
           .append(") VALUES ");
        for (int i = 0; i < callTOs.size(); i++) {
            if (i > 0) {
                sql.append(", ");
            }
            sql.append("(").append(BgeePreparedStatement.generateParameterizedQueryString(toPopulate.size()))
               .append(") ");
        }
        try (BgeePreparedStatement stmt = 
                this.getManager().getConnection().prepareStatement(sql.toString())) {
            int paramIndex = 1;
            for (GlobalExpressionCallTO callTO: callTOs) {
                for (GlobalExpressionCallDAO.Attribute attr: toPopulate) {
                    switch (attr) {
                    case ID:
                        stmt.setInt(paramIndex, callTO.getId());
                        paramIndex++;
                        break;
                    case BGEE_GENE_ID:
                        stmt.setInt(paramIndex, callTO.getBgeeGeneId());
                        paramIndex++;
                        break;
                    case CONDITION_ID:
                        stmt.setInt(paramIndex, callTO.getConditionId());
                        paramIndex++;
                        break;
                        
                    case AFFYMETRIX_EXP_PRESENT_HIGH_SELF_COUNT:
                        stmt.setInt(paramIndex, callTO.getAffymetrixExpPresentHighSelfCount());
                        paramIndex++;
                        break;
                    case AFFYMETRIX_EXP_PRESENT_LOW_SELF_COUNT:
                        stmt.setInt(paramIndex, callTO.getAffymetrixExpPresentLowSelfCount());
                        paramIndex++;
                        break;
                    case AFFYMETRIX_EXP_ABSENT_HIGH_SELF_COUNT:
                        stmt.setInt(paramIndex, callTO.getAffymetrixExpAbsentHighSelfCount());
                        paramIndex++;
                        break;
                    case AFFYMETRIX_EXP_ABSENT_LOW_SELF_COUNT:
                        stmt.setInt(paramIndex, callTO.getAffymetrixExpAbsentLowSelfCount());
                        paramIndex++;
                        break;
                    case AFFYMETRIX_EXP_PRESENT_HIGH_DESCENDANT_COUNT:
                        stmt.setInt(paramIndex, callTO.getAffymetrixExpPresentHighDescendantCount());
                        paramIndex++;
                        break;
                    case AFFYMETRIX_EXP_PRESENT_LOW_DESCENDANT_COUNT:
                        stmt.setInt(paramIndex, callTO.getAffymetrixExpPresentLowDescendantCount());
                        paramIndex++;
                        break;
                    case AFFYMETRIX_EXP_ABSENT_HIGH_PARENT_COUNT:
                        stmt.setInt(paramIndex, callTO.getAffymetrixExpAbsentHighParentCount());
                        paramIndex++;
                        break;
                    case AFFYMETRIX_EXP_ABSENT_LOW_PARENT_COUNT:
                        stmt.setInt(paramIndex, callTO.getAffymetrixExpAbsentLowParentCount());
                        paramIndex++;
                        break;
                    case AFFYMETRIX_EXP_PRESENT_HIGH_TOTAL_COUNT:
                        stmt.setInt(paramIndex, callTO.getAffymetrixExpPresentHighTotalCount());
                        paramIndex++;
                        break;
                    case AFFYMETRIX_EXP_PRESENT_LOW_TOTAL_COUNT:
                        stmt.setInt(paramIndex, callTO.getAffymetrixExpPresentLowTotalCount());
                        paramIndex++;
                        break;
                    case AFFYMETRIX_EXP_ABSENT_HIGH_TOTAL_COUNT:
                        stmt.setInt(paramIndex, callTO.getAffymetrixExpAbsentHighTotalCount());
                        paramIndex++;
                        break;
                    case AFFYMETRIX_EXP_ABSENT_LOW_TOTAL_COUNT:
                        stmt.setInt(paramIndex, callTO.getAffymetrixExpAbsentLowTotalCount());
                        paramIndex++;
                        break;
                    case AFFYMETRIX_EXP_PROPAGATED_COUNT:
                        stmt.setInt(paramIndex, callTO.getAffymetrixExpPropagatedCount());
                        paramIndex++;
                        break;
                        
                    case RNA_SEQ_EXP_PRESENT_HIGH_SELF_COUNT:
                        stmt.setInt(paramIndex, callTO.getRNASeqExpPresentHighSelfCount());
                        paramIndex++;
                        break;
                    case RNA_SEQ_EXP_PRESENT_LOW_SELF_COUNT:
                        stmt.setInt(paramIndex, callTO.getRNASeqExpPresentLowSelfCount());
                        paramIndex++;
                        break;
                    case RNA_SEQ_EXP_ABSENT_HIGH_SELF_COUNT:
                        stmt.setInt(paramIndex, callTO.getRNASeqExpAbsentHighSelfCount());
                        paramIndex++;
                        break;
                    case RNA_SEQ_EXP_ABSENT_LOW_SELF_COUNT:
                        stmt.setInt(paramIndex, callTO.getRNASeqExpAbsentLowSelfCount());
                        paramIndex++;
                        break;
                    case RNA_SEQ_EXP_PRESENT_HIGH_DESCENDANT_COUNT:
                        stmt.setInt(paramIndex, callTO.getRNASeqExpPresentHighDescendantCount());
                        paramIndex++;
                        break;
                    case RNA_SEQ_EXP_PRESENT_LOW_DESCENDANT_COUNT:
                        stmt.setInt(paramIndex, callTO.getRNASeqExpPresentLowDescendantCount());
                        paramIndex++;
                        break;
                    case RNA_SEQ_EXP_ABSENT_HIGH_PARENT_COUNT:
                        stmt.setInt(paramIndex, callTO.getRNASeqExpAbsentHighParentCount());
                        paramIndex++;
                        break;
                    case RNA_SEQ_EXP_ABSENT_LOW_PARENT_COUNT:
                        stmt.setInt(paramIndex, callTO.getRNASeqExpAbsentLowParentCount());
                        paramIndex++;
                        break;
                    case RNA_SEQ_EXP_PRESENT_HIGH_TOTAL_COUNT:
                        stmt.setInt(paramIndex, callTO.getRNASeqExpPresentHighTotalCount());
                        paramIndex++;
                        break;
                    case RNA_SEQ_EXP_PRESENT_LOW_TOTAL_COUNT:
                        stmt.setInt(paramIndex, callTO.getRNASeqExpPresentLowTotalCount());
                        paramIndex++;
                        break;
                    case RNA_SEQ_EXP_ABSENT_HIGH_TOTAL_COUNT:
                        stmt.setInt(paramIndex, callTO.getRNASeqExpAbsentHighTotalCount());
                        paramIndex++;
                        break;
                    case RNA_SEQ_EXP_ABSENT_LOW_TOTAL_COUNT:
                        stmt.setInt(paramIndex, callTO.getRNASeqExpAbsentLowTotalCount());
                        paramIndex++;
                        break;
                    case RNA_SEQ_EXP_PROPAGATED_COUNT:
                        stmt.setInt(paramIndex, callTO.getRNASeqExpPropagatedCount());
                        paramIndex++;
                        break;
                        
                    case EST_LIB_PRESENT_HIGH_SELF_COUNT:
                        stmt.setInt(paramIndex, callTO.getESTLibPresentHighSelfCount());
                        paramIndex++;
                        break;
                    case EST_LIB_PRESENT_LOW_SELF_COUNT:
                        stmt.setInt(paramIndex, callTO.getESTLibPresentLowSelfCount());
                        paramIndex++;
                        break;
                    case EST_LIB_PRESENT_HIGH_DESCENDANT_COUNT:
                        stmt.setInt(paramIndex, callTO.getESTLibPresentHighDescendantCount());
                        paramIndex++;
                        break;
                    case EST_LIB_PRESENT_LOW_DESCENDANT_COUNT:
                        stmt.setInt(paramIndex, callTO.getESTLibPresentLowDescendantCount());
                        paramIndex++;
                        break;
                    case EST_LIB_PRESENT_HIGH_TOTAL_COUNT:
                        stmt.setInt(paramIndex, callTO.getESTLibPresentHighTotalCount());
                        paramIndex++;
                        break;
                    case EST_LIB_PRESENT_LOW_TOTAL_COUNT:
                        stmt.setInt(paramIndex, callTO.getESTLibPresentLowTotalCount());
                        paramIndex++;
                        break;
                    case EST_LIB_PROPAGATED_COUNT:
                        stmt.setInt(paramIndex, callTO.getESTLibPropagatedCount());
                        paramIndex++;
                        break;
                        
                    case IN_SITU_EXP_PRESENT_HIGH_SELF_COUNT:
                        stmt.setInt(paramIndex, callTO.getInSituExpPresentHighSelfCount());
                        paramIndex++;
                        break;
                    case IN_SITU_EXP_PRESENT_LOW_SELF_COUNT:
                        stmt.setInt(paramIndex, callTO.getInSituExpPresentLowSelfCount());
                        paramIndex++;
                        break;
                    case IN_SITU_EXP_ABSENT_HIGH_SELF_COUNT:
                        stmt.setInt(paramIndex, callTO.getInSituExpAbsentHighSelfCount());
                        paramIndex++;
                        break;
                    case IN_SITU_EXP_ABSENT_LOW_SELF_COUNT:
                        stmt.setInt(paramIndex, callTO.getInSituExpAbsentLowSelfCount());
                        paramIndex++;
                        break;
                    case IN_SITU_EXP_PRESENT_HIGH_DESCENDANT_COUNT:
                        stmt.setInt(paramIndex, callTO.getInSituExpPresentHighDescendantCount());
                        paramIndex++;
                        break;
                    case IN_SITU_EXP_PRESENT_LOW_DESCENDANT_COUNT:
                        stmt.setInt(paramIndex, callTO.getInSituExpPresentLowDescendantCount());
                        paramIndex++;
                        break;
                    case IN_SITU_EXP_ABSENT_HIGH_PARENT_COUNT:
                        stmt.setInt(paramIndex, callTO.getInSituExpAbsentHighParentCount());
                        paramIndex++;
                        break;
                    case IN_SITU_EXP_ABSENT_LOW_PARENT_COUNT:
                        stmt.setInt(paramIndex, callTO.getInSituExpAbsentLowParentCount());
                        paramIndex++;
                        break;
                    case IN_SITU_EXP_PRESENT_HIGH_TOTAL_COUNT:
                        stmt.setInt(paramIndex, callTO.getInSituExpPresentHighTotalCount());
                        paramIndex++;
                        break;
                    case IN_SITU_EXP_PRESENT_LOW_TOTAL_COUNT:
                        stmt.setInt(paramIndex, callTO.getInSituExpPresentLowTotalCount());
                        paramIndex++;
                        break;
                    case IN_SITU_EXP_ABSENT_HIGH_TOTAL_COUNT:
                        stmt.setInt(paramIndex, callTO.getInSituExpAbsentHighTotalCount());
                        paramIndex++;
                        break;
                    case IN_SITU_EXP_ABSENT_LOW_TOTAL_COUNT:
                        stmt.setInt(paramIndex, callTO.getInSituExpAbsentLowTotalCount());
                        paramIndex++;
                        break;
                    case IN_SITU_EXP_PROPAGATED_COUNT:
                        stmt.setInt(paramIndex, callTO.getInSituExpPropagatedCount());
                        paramIndex++;
                        break;
                        
                    case AFFYMETRIX_MEAN_RANK:
                        stmt.setBigDecimal(paramIndex, callTO.getAffymetrixMeanRank());
                        paramIndex++;
                        break;
                    case RNA_SEQ_MEAN_RANK:
                        stmt.setBigDecimal(paramIndex, callTO.getRNASeqMeanRank());
                        paramIndex++;
                        break;
                    case EST_RANK:
                        stmt.setBigDecimal(paramIndex, callTO.getESTRank());
                        paramIndex++;
                        break;
                    case IN_SITU_RANK:
                        stmt.setBigDecimal(paramIndex, callTO.getInSituRank());
                        paramIndex++;
                        break;
                    case AFFYMETRIX_MEAN_RANK_NORM:
                        stmt.setBigDecimal(paramIndex, callTO.getAffymetrixMeanRankNorm());
                        paramIndex++;
                        break;
                    case RNA_SEQ_MEAN_RANK_NORM:
                        stmt.setBigDecimal(paramIndex, callTO.getRNASeqMeanRankNorm());
                        paramIndex++;
                        break;
                    case EST_RANK_NORM:
                        stmt.setBigDecimal(paramIndex, callTO.getESTRankNorm());
                        paramIndex++;
                        break;
                    case IN_SITU_RANK_NORM:
                        stmt.setBigDecimal(paramIndex, callTO.getInSituRankNorm());
                        paramIndex++;
                        break;
                    case AFFYMETRIX_DISTINCT_RANK_SUM:
                        stmt.setBigDecimal(paramIndex, callTO.getAffymetrixDistinctRankSum());
                        paramIndex++;
                        break;
                    case RNA_SEQ_DISTINCT_RANK_SUM:
                        stmt.setBigDecimal(paramIndex, callTO.getRNASeqDistinctRankSum());
                        paramIndex++;
                        break;
                    default:
                        log.throwing(new IllegalStateException("Unsupported attribute: " + attr));
                    }
                }
            }
            
            return log.exit(stmt.executeUpdate());
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
    }

    @Override
    public int insertGlobalExpressionToRawExpression(
            Collection<GlobalExpressionToRawExpressionTO> globalExprToRawExprTOs, 
            Collection<ConditionDAO.Attribute> conditionParameters) throws DAOException, IllegalArgumentException {
        log.entry(globalExprToRawExprTOs, conditionParameters);

        CondParamCombination comb = CondParamCombination.getCombination(conditionParameters);
        
        if (globalExprToRawExprTOs == null || globalExprToRawExprTOs.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("No expression relation provided"));
        }
        
        //The order of the parameters is important for generating the query and then setting the parameters.
        List<String> toPopulate = Arrays.asList(comb.getRawExprIdField(),
                                                comb.getGlobalExprIdField(),
                                                CALL_ORIGIN_FIELD);
        
        StringBuilder sql = new StringBuilder(); 
        sql.append("INSERT INTO ").append(comb.getGlobalToRawExprTable()).append(" (")
           .append(toPopulate.stream().collect(Collectors.joining(", ")))
           .append(") VALUES ");
        for (int i = 0; i < globalExprToRawExprTOs.size(); i++) {
            if (i > 0) {
                sql.append(", ");
            }
            sql.append("(").append(BgeePreparedStatement.generateParameterizedQueryString(toPopulate.size()))
               .append(") ");
        }
        try (BgeePreparedStatement stmt = 
                this.getManager().getConnection().prepareStatement(sql.toString())) {
            int paramIndex = 1;
            for (GlobalExpressionToRawExpressionTO to: globalExprToRawExprTOs) {
                for (String attr: toPopulate) {
                    if (attr.equals(comb.getRawExprIdField())) {
                        stmt.setInt(paramIndex, to.getRawExpressionId());
                        paramIndex++;
                    } else if (attr.equals(comb.getGlobalExprIdField())) {
                        stmt.setInt(paramIndex, to.getGlobalExpressionId());
                        paramIndex++;
                    } else if (attr.equals(CALL_ORIGIN_FIELD)) {
                        stmt.setString(paramIndex, to.getCallOrigin().getStringRepresentation());
                        paramIndex++;
                    } else {
                        log.throwing(new IllegalStateException("Unsupported attribute: " + attr));
                    }
                }
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
    class MySQLGlobalExpressionCallTOResultSet extends MySQLDAOResultSet<GlobalExpressionCallDAO.GlobalExpressionCallTO>
            implements GlobalExpressionCallTOResultSet {

        /**
         * The {@code CondParamCombination} allowing to target the appropriate field and table names.
         */
        private final CondParamCombination comb;
        
        /**
         * @param statement The {@code BgeePreparedStatement}
         * @param comb      The {@code CondParamCombination} allowing to target the appropriate 
         *                  field and table names.
         */
        private MySQLGlobalExpressionCallTOResultSet(BgeePreparedStatement statement, CondParamCombination comb) {
            super(statement);
            this.comb = comb;
        }

        @Override
        protected GlobalExpressionCallDAO.GlobalExpressionCallTO getNewTO() throws DAOException {
            try {
                log.entry();
                final ResultSet currentResultSet = this.getCurrentResultSet();
                Integer id = null, bgeeGeneId = null, conditionId = null;
                
                Integer affymetrixExpPresentHighSelfCount = null, affymetrixExpPresentLowSelfCount = null, 
                        affymetrixExpAbsentHighSelfCount = null, affymetrixExpAbsentLowSelfCount = null, 
                        affymetrixExpPresentHighDescendantCount = null, 
                        affymetrixExpPresentLowDescendantCount = null, 
                        affymetrixExpAbsentHighParentCount = null, affymetrixExpAbsentLowParentCount = null, 
                        affymetrixExpPresentHighTotalCount = null, affymetrixExpPresentLowTotalCount = null, 
                        affymetrixExpAbsentHighTotalCount = null, affymetrixExpAbsentLowTotalCount = null, 
                        affymetrixExpPropagatedCount = null, rnaSeqExpPresentHighSelfCount = null, 
                        rnaSeqExpPresentLowSelfCount = null, rnaSeqExpAbsentHighSelfCount = null, 
                        rnaSeqExpAbsentLowSelfCount = null, rnaSeqExpPresentHighDescendantCount = null, 
                        rnaSeqExpPresentLowDescendantCount = null, rnaSeqExpAbsentHighParentCount = null, 
                        rnaSeqExpAbsentLowParentCount = null, rnaSeqExpPresentHighTotalCount = null, 
                        rnaSeqExpPresentLowTotalCount = null, rnaSeqExpAbsentHighTotalCount = null, 
                        rnaSeqExpAbsentLowTotalCount = null, rnaSeqExpPropagatedCount = null, 
                        estLibPresentHighSelfCount = null, estLibPresentLowSelfCount = null, 
                        estLibPresentHighDescendantCount = null, estLibPresentLowDescendantCount = null, 
                        estLibPresentHighTotalCount = null, estLibPresentLowTotalCount = null, 
                        estLibPropagatedCount = null, inSituExpPresentHighSelfCount = null, 
                        inSituExpPresentLowSelfCount = null, inSituExpAbsentHighSelfCount = null, 
                        inSituExpAbsentLowSelfCount = null, inSituExpPresentHighDescendantCount = null, 
                        inSituExpPresentLowDescendantCount = null, inSituExpAbsentHighParentCount = null, 
                        inSituExpAbsentLowParentCount = null, inSituExpPresentHighTotalCount = null, 
                        inSituExpPresentLowTotalCount = null, inSituExpAbsentHighTotalCount = null, 
                        inSituExpAbsentLowTotalCount = null, inSituExpPropagatedCount = null;
                
                BigDecimal globalMeanRank = null,
                        affymetrixMeanRank = null, rnaSeqMeanRank = null, estRank = null, 
                        inSituRank = null, affymetrixMeanRankNorm = null, rnaSeqMeanRankNorm = null, 
                        estRankNorm = null, inSituRankNorm = null, 
                        affymetrixDistinctRankSum = null, rnaSeqDistinctRankSum = null;
                
                Map<String, GlobalExpressionCallDAO.Attribute> colToAttrMap = getColToAttributesMap(comb);
                
                for (Map.Entry<Integer, String> col : this.getColumnLabels().entrySet()) {
                    String columnName = col.getValue();
                    GlobalExpressionCallDAO.Attribute attr = getAttributeFromColName(columnName, colToAttrMap);
                    switch (attr) {
                    case ID:
                        id = currentResultSet.getInt(columnName);
                        break;
                    case BGEE_GENE_ID:
                        bgeeGeneId = currentResultSet.getInt(columnName);
                        break;
                    case CONDITION_ID:
                        conditionId = currentResultSet.getInt(columnName);
                        break;

                    case GLOBAL_MEAN_RANK:
                        globalMeanRank = currentResultSet.getBigDecimal(columnName);
                        break;
                        
                    case AFFYMETRIX_EXP_PRESENT_HIGH_SELF_COUNT:
                        affymetrixExpPresentHighSelfCount = currentResultSet.getInt(columnName);
                        break;
                    case AFFYMETRIX_EXP_PRESENT_LOW_SELF_COUNT:
                        affymetrixExpPresentLowSelfCount = currentResultSet.getInt(columnName);
                        break;
                    case AFFYMETRIX_EXP_ABSENT_HIGH_SELF_COUNT:
                        affymetrixExpAbsentHighSelfCount = currentResultSet.getInt(columnName);
                        break;
                    case AFFYMETRIX_EXP_ABSENT_LOW_SELF_COUNT:
                        affymetrixExpAbsentLowSelfCount = currentResultSet.getInt(columnName);
                        break;
                    case AFFYMETRIX_EXP_PRESENT_HIGH_DESCENDANT_COUNT:
                        affymetrixExpPresentHighDescendantCount = currentResultSet.getInt(columnName);
                        break;
                    case AFFYMETRIX_EXP_PRESENT_LOW_DESCENDANT_COUNT:
                        affymetrixExpPresentLowDescendantCount = currentResultSet.getInt(columnName);
                        break;
                    case AFFYMETRIX_EXP_ABSENT_HIGH_PARENT_COUNT:
                        affymetrixExpAbsentHighParentCount = currentResultSet.getInt(columnName);
                        break;
                    case AFFYMETRIX_EXP_ABSENT_LOW_PARENT_COUNT:
                        affymetrixExpAbsentLowParentCount = currentResultSet.getInt(columnName);
                        break;
                    case AFFYMETRIX_EXP_PRESENT_HIGH_TOTAL_COUNT:
                        affymetrixExpPresentHighTotalCount = currentResultSet.getInt(columnName);
                        break;
                    case AFFYMETRIX_EXP_PRESENT_LOW_TOTAL_COUNT:
                        affymetrixExpPresentLowTotalCount = currentResultSet.getInt(columnName);
                        break;
                    case AFFYMETRIX_EXP_ABSENT_HIGH_TOTAL_COUNT:
                        affymetrixExpAbsentHighTotalCount = currentResultSet.getInt(columnName);
                        break;
                    case AFFYMETRIX_EXP_ABSENT_LOW_TOTAL_COUNT:
                        affymetrixExpAbsentLowTotalCount = currentResultSet.getInt(columnName);
                        break;
                    case AFFYMETRIX_EXP_PROPAGATED_COUNT:
                        affymetrixExpPropagatedCount = currentResultSet.getInt(columnName);
                        break;
                        
                    case RNA_SEQ_EXP_PRESENT_HIGH_SELF_COUNT:
                        rnaSeqExpPresentHighSelfCount = currentResultSet.getInt(columnName);
                        break;
                    case RNA_SEQ_EXP_PRESENT_LOW_SELF_COUNT:
                        rnaSeqExpPresentLowSelfCount = currentResultSet.getInt(columnName);
                        break;
                    case RNA_SEQ_EXP_ABSENT_HIGH_SELF_COUNT:
                        rnaSeqExpAbsentHighSelfCount = currentResultSet.getInt(columnName);
                        break;
                    case RNA_SEQ_EXP_ABSENT_LOW_SELF_COUNT:
                        rnaSeqExpAbsentLowSelfCount = currentResultSet.getInt(columnName);
                        break;
                    case RNA_SEQ_EXP_PRESENT_HIGH_DESCENDANT_COUNT:
                        rnaSeqExpPresentHighDescendantCount = currentResultSet.getInt(columnName);
                        break;
                    case RNA_SEQ_EXP_PRESENT_LOW_DESCENDANT_COUNT:
                        rnaSeqExpPresentLowDescendantCount = currentResultSet.getInt(columnName);
                        break;
                    case RNA_SEQ_EXP_ABSENT_HIGH_PARENT_COUNT:
                        rnaSeqExpAbsentHighParentCount = currentResultSet.getInt(columnName);
                        break;
                    case RNA_SEQ_EXP_ABSENT_LOW_PARENT_COUNT:
                        rnaSeqExpAbsentLowParentCount = currentResultSet.getInt(columnName);
                        break;
                    case RNA_SEQ_EXP_PRESENT_HIGH_TOTAL_COUNT:
                        rnaSeqExpPresentHighTotalCount = currentResultSet.getInt(columnName);
                        break;
                    case RNA_SEQ_EXP_PRESENT_LOW_TOTAL_COUNT:
                        rnaSeqExpPresentLowTotalCount = currentResultSet.getInt(columnName);
                        break;
                    case RNA_SEQ_EXP_ABSENT_HIGH_TOTAL_COUNT:
                        rnaSeqExpAbsentHighTotalCount = currentResultSet.getInt(columnName);
                        break;
                    case RNA_SEQ_EXP_ABSENT_LOW_TOTAL_COUNT:
                        rnaSeqExpAbsentLowTotalCount = currentResultSet.getInt(columnName);
                        break;
                    case RNA_SEQ_EXP_PROPAGATED_COUNT:
                        rnaSeqExpPropagatedCount = currentResultSet.getInt(columnName);
                        break;
                        
                    case EST_LIB_PRESENT_HIGH_SELF_COUNT:
                        estLibPresentHighSelfCount = currentResultSet.getInt(columnName);
                        break;
                    case EST_LIB_PRESENT_LOW_SELF_COUNT:
                        estLibPresentLowSelfCount = currentResultSet.getInt(columnName);
                        break;
                    case EST_LIB_PRESENT_HIGH_DESCENDANT_COUNT:
                        estLibPresentHighDescendantCount = currentResultSet.getInt(columnName);
                        break;
                    case EST_LIB_PRESENT_LOW_DESCENDANT_COUNT:
                        estLibPresentLowDescendantCount = currentResultSet.getInt(columnName);
                        break;
                    case EST_LIB_PRESENT_HIGH_TOTAL_COUNT:
                        estLibPresentHighTotalCount = currentResultSet.getInt(columnName);
                        break;
                    case EST_LIB_PRESENT_LOW_TOTAL_COUNT:
                        estLibPresentLowTotalCount = currentResultSet.getInt(columnName);
                        break;
                    case EST_LIB_PROPAGATED_COUNT:
                        estLibPropagatedCount = currentResultSet.getInt(columnName);
                        break;
                        
                    case IN_SITU_EXP_PRESENT_HIGH_SELF_COUNT:
                        inSituExpPresentHighSelfCount = currentResultSet.getInt(columnName);
                        break;
                    case IN_SITU_EXP_PRESENT_LOW_SELF_COUNT:
                        inSituExpPresentLowSelfCount = currentResultSet.getInt(columnName);
                        break;
                    case IN_SITU_EXP_ABSENT_HIGH_SELF_COUNT:
                        inSituExpAbsentHighSelfCount = currentResultSet.getInt(columnName);
                        break;
                    case IN_SITU_EXP_ABSENT_LOW_SELF_COUNT:
                        inSituExpAbsentLowSelfCount = currentResultSet.getInt(columnName);
                        break;
                    case IN_SITU_EXP_PRESENT_HIGH_DESCENDANT_COUNT:
                        inSituExpPresentHighDescendantCount = currentResultSet.getInt(columnName);
                        break;
                    case IN_SITU_EXP_PRESENT_LOW_DESCENDANT_COUNT:
                        inSituExpPresentLowDescendantCount = currentResultSet.getInt(columnName);
                        break;
                    case IN_SITU_EXP_ABSENT_HIGH_PARENT_COUNT:
                        inSituExpAbsentHighParentCount = currentResultSet.getInt(columnName);
                        break;
                    case IN_SITU_EXP_ABSENT_LOW_PARENT_COUNT:
                        inSituExpAbsentLowParentCount = currentResultSet.getInt(columnName);
                        break;
                    case IN_SITU_EXP_PRESENT_HIGH_TOTAL_COUNT:
                        inSituExpPresentHighTotalCount = currentResultSet.getInt(columnName);
                        break;
                    case IN_SITU_EXP_PRESENT_LOW_TOTAL_COUNT:
                        inSituExpPresentLowTotalCount = currentResultSet.getInt(columnName);
                        break;
                    case IN_SITU_EXP_ABSENT_HIGH_TOTAL_COUNT:
                        inSituExpAbsentHighTotalCount = currentResultSet.getInt(columnName);
                        break;
                    case IN_SITU_EXP_ABSENT_LOW_TOTAL_COUNT:
                        inSituExpAbsentLowTotalCount = currentResultSet.getInt(columnName);
                        break;
                    case IN_SITU_EXP_PROPAGATED_COUNT:
                        inSituExpPropagatedCount = currentResultSet.getInt(columnName);
                        break;
                        
                    case AFFYMETRIX_MEAN_RANK:
                        affymetrixMeanRank = currentResultSet.getBigDecimal(columnName);
                        break;
                    case RNA_SEQ_MEAN_RANK:
                        rnaSeqMeanRank = currentResultSet.getBigDecimal(columnName);
                        break;
                    case EST_RANK:
                        estRank = currentResultSet.getBigDecimal(columnName);
                        break;
                    case IN_SITU_RANK:
                        inSituRank = currentResultSet.getBigDecimal(columnName);
                        break;
                    case AFFYMETRIX_MEAN_RANK_NORM:
                        affymetrixMeanRankNorm = currentResultSet.getBigDecimal(columnName);
                        break;
                    case RNA_SEQ_MEAN_RANK_NORM:
                        rnaSeqMeanRankNorm = currentResultSet.getBigDecimal(columnName);
                        break;
                    case EST_RANK_NORM:
                        estRankNorm = currentResultSet.getBigDecimal(columnName);
                        break;
                    case IN_SITU_RANK_NORM:
                        inSituRankNorm = currentResultSet.getBigDecimal(columnName);
                        break;
                    case AFFYMETRIX_DISTINCT_RANK_SUM:
                        affymetrixDistinctRankSum = currentResultSet.getBigDecimal(columnName);
                        break;
                    case RNA_SEQ_DISTINCT_RANK_SUM:
                        rnaSeqDistinctRankSum = currentResultSet.getBigDecimal(columnName);
                        break;

                    default:
                        log.throwing(new UnrecognizedColumnException(columnName));
                    }
                }
                return log.exit(new GlobalExpressionCallTO(id, bgeeGeneId, conditionId,
                        globalMeanRank,
                        affymetrixExpPresentHighSelfCount, affymetrixExpPresentLowSelfCount, 
                        affymetrixExpAbsentHighSelfCount, affymetrixExpAbsentLowSelfCount, 
                        affymetrixExpPresentHighDescendantCount, 
                        affymetrixExpPresentLowDescendantCount, 
                        affymetrixExpAbsentHighParentCount, affymetrixExpAbsentLowParentCount, 
                        affymetrixExpPresentHighTotalCount, affymetrixExpPresentLowTotalCount, 
                        affymetrixExpAbsentHighTotalCount, affymetrixExpAbsentLowTotalCount, 
                        affymetrixExpPropagatedCount, rnaSeqExpPresentHighSelfCount, 
                        rnaSeqExpPresentLowSelfCount, rnaSeqExpAbsentHighSelfCount, 
                        rnaSeqExpAbsentLowSelfCount, rnaSeqExpPresentHighDescendantCount, 
                        rnaSeqExpPresentLowDescendantCount, rnaSeqExpAbsentHighParentCount, 
                        rnaSeqExpAbsentLowParentCount, rnaSeqExpPresentHighTotalCount, 
                        rnaSeqExpPresentLowTotalCount, rnaSeqExpAbsentHighTotalCount, 
                        rnaSeqExpAbsentLowTotalCount, rnaSeqExpPropagatedCount, 
                        estLibPresentHighSelfCount, estLibPresentLowSelfCount, 
                        estLibPresentHighDescendantCount, estLibPresentLowDescendantCount, 
                        estLibPresentHighTotalCount, estLibPresentLowTotalCount, 
                        estLibPropagatedCount, inSituExpPresentHighSelfCount, 
                        inSituExpPresentLowSelfCount, inSituExpAbsentHighSelfCount, 
                        inSituExpAbsentLowSelfCount, inSituExpPresentHighDescendantCount, 
                        inSituExpPresentLowDescendantCount, inSituExpAbsentHighParentCount, 
                        inSituExpAbsentLowParentCount, inSituExpPresentHighTotalCount, 
                        inSituExpPresentLowTotalCount, inSituExpAbsentHighTotalCount, 
                        inSituExpAbsentLowTotalCount, inSituExpPropagatedCount, 
                        affymetrixMeanRank, rnaSeqMeanRank, estRank,
                        inSituRank, affymetrixMeanRankNorm,
                        rnaSeqMeanRankNorm, estRankNorm, inSituRankNorm,
                        affymetrixDistinctRankSum, rnaSeqDistinctRankSum));
            } catch (SQLException e) {
                throw log.throwing(new DAOException(e));
            }
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
        
        /**
         * The {@code CondParamCombination} allowing to target the appropriate field and table names.
         */
        private final CondParamCombination comb;
        
        /**
         * @param statement The {@code BgeePreparedStatement}
         * @param comb      The {@code CondParamCombination} allowing to target the appropriate 
         *                  field and table names.
         */
        private MySQLGlobalExpressionToRawExpressionTOResultSet(BgeePreparedStatement statement, 
                CondParamCombination comb) {
            super(statement);
            this.comb = comb;
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
                    
                    if (columnName.equals(comb.getRawExprIdField())) {
                        rawExpressionId = currentResultSet.getInt(columnName);
                    } else if (columnName.equals(comb.getGlobalExprIdField())) {
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

    @Override
    public GlobalExpressionCallTOResultSet getGlobalCalls(Collection<Integer> arg0,
            Collection<org.bgee.model.dao.api.expressiondata.ConditionDAO.Attribute> arg1,
            Collection<org.bgee.model.dao.api.expressiondata.GlobalExpressionCallDAO.Attribute> arg2)
            throws DAOException, IllegalArgumentException {
        // TODO Auto-generated method stub
        return null;
    }
}
