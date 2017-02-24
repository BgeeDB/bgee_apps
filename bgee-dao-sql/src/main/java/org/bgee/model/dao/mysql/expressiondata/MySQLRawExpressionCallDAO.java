package org.bgee.model.dao.mysql.expressiondata;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.expressiondata.ConditionDAO;
import org.bgee.model.dao.api.expressiondata.RawExpressionCallDAO;
import org.bgee.model.dao.mysql.MySQLDAO;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.model.dao.mysql.connector.MySQLDAOResultSet;
import org.bgee.model.dao.mysql.exception.UnrecognizedColumnException;

/**
 * A {@code RawExpressionCallDAO} for MySQL. 
 * 
 * @author  Valentine Rech de Laval
 * @author  Frederic Bastian
 * @version Bgee 14, Feb. 2017
 * @see     org.bgee.model.dao.api.expressiondata.RawExpressionCallDAO.RawExpressionCallTO
 * @since   Bgee 14, Feb. 2017
 */
public class MySQLRawExpressionCallDAO extends MySQLDAO<RawExpressionCallDAO.Attribute> 
    implements RawExpressionCallDAO {
    private final static Logger log = LogManager.getLogger(MySQLRawExpressionCallDAO.class.getName());

    /**
     * Get a {@code Map} associating column names to corresponding {@code RawExpressionCallDAO.Attribute}.
     * 
     * @param comb  The {@code CondParamCombination} allowing to target the appropriate 
     *              field and table names.
     * @return      A {@code Map} where keys are {@code String}s that are column names, 
     *              the associated value being the corresponding {@code RawExpressionCallDAO.Attribute}.
     */
    private static Map<String, RawExpressionCallDAO.Attribute> getColToAttributesMap(
            CondParamCombination comb) {
        log.entry(comb);
        Map<String, RawExpressionCallDAO.Attribute> colToAttributesMap = new HashMap<>();
        colToAttributesMap.put(comb.getRawExprIdField(), RawExpressionCallDAO.Attribute.ID);
        colToAttributesMap.put("bgeeGeneId", RawExpressionCallDAO.Attribute.BGEE_GENE_ID);
        colToAttributesMap.put(comb.getCondIdField(), RawExpressionCallDAO.Attribute.CONDITION_ID);
        colToAttributesMap.put("affymetrixMeanRank", RawExpressionCallDAO.Attribute.AFFYMETRIX_MEAN_RANK);
        colToAttributesMap.put("rnaSeqMeanRank", RawExpressionCallDAO.Attribute.RNA_SEQ_MEAN_RANK);
        colToAttributesMap.put("estRank", RawExpressionCallDAO.Attribute.EST_RANK);
        colToAttributesMap.put("inSituRank", RawExpressionCallDAO.Attribute.IN_SITU_RANK);
        colToAttributesMap.put("affymetrixMeanRankNorm", 
                RawExpressionCallDAO.Attribute.AFFYMETRIX_MEAN_RANK_NORM);
        colToAttributesMap.put("rnaSeqMeanRankNorm", 
                RawExpressionCallDAO.Attribute.RNA_SEQ_MEAN_RANK_NORM);
        colToAttributesMap.put("estRankNorm", RawExpressionCallDAO.Attribute.EST_RANK_NORM);
        colToAttributesMap.put("inSituRankNorm", RawExpressionCallDAO.Attribute.IN_SITU_RANK_NORM);
        colToAttributesMap.put("affymetrixDistinctRankSum", 
                RawExpressionCallDAO.Attribute.AFFYMETRIX_DISTINCT_RANK_SUM);
        colToAttributesMap.put("rnaSeqDistinctRankSum", 
                RawExpressionCallDAO.Attribute.RNA_SEQ_DISTINCT_RANK_SUM);
        
        return log.exit(colToAttributesMap);
    }

    public MySQLRawExpressionCallDAO(MySQLDAOManager manager) throws IllegalArgumentException {
        super(manager);
    }


    @Override
    public RawExpressionCallTOResultSet getExpressionCallsOrderedByGeneIdAndExprId(int speciesId,
            Collection<ConditionDAO.Attribute> condParameters) throws DAOException {
        log.entry(speciesId, condParameters);

        CondParamCombination comb = CondParamCombination.getCombination(condParameters);
        
        String sql = generateSelectClause(comb.getRawExprTable(), getColToAttributesMap(comb), 
                false, null) 
                + " FROM " + comb.getRawExprTable() 
                + " INNER JOIN " + comb.getCondTable() 
                    + " ON " + comb.getRawExprTable() + "." + comb.getCondIdField()
                    + " = " + comb.getCondTable() + "." + comb.getCondIdField()
                + " WHERE " + comb.getCondTable() + ".speciesId = ?"
                + " ORDER BY " + comb.getRawExprTable() + ".bgeeGeneId, " 
                    + comb.getRawExprTable() + "." + comb.getRawExprIdField();
        try {
            BgeePreparedStatement stmt = this.getManager().getConnection().prepareStatement(sql);
            stmt.setInt(1, speciesId);
            return log.exit(new MySQLRawExpressionCallTOResultSet(stmt, comb));
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
    }

    /**
     * Implementation of the {@code RawExpressionCallTOResultSet}. 
     * 
     * @author Frederic Bastian
     * @version Bgee 14 Feb. 2017
     * @since Bgee 14 Feb. 2017
     */
    class MySQLRawExpressionCallTOResultSet extends MySQLDAOResultSet<RawExpressionCallDAO.RawExpressionCallTO>
            implements RawExpressionCallTOResultSet {

        /**
         * The {@code CondParamCombination} allowing to target the appropriate field and table names.
         */
        private final CondParamCombination comb;
        
        /**
         * @param statement The {@code BgeePreparedStatement}
         * @param comb      The {@code CondParamCombination} allowing to target the appropriate 
         *                  field and table names.
         */
        private MySQLRawExpressionCallTOResultSet(BgeePreparedStatement statement, 
                CondParamCombination comb) {
            super(statement);
            this.comb = comb;
        }

        @Override
        protected RawExpressionCallDAO.RawExpressionCallTO getNewTO() throws DAOException {
            try {
                log.entry();
                final ResultSet currentResultSet = this.getCurrentResultSet();
                Integer id = null, bgeeGeneId = null, conditionId = null;
                BigDecimal affymetrixMeanRank = null, rnaSeqMeanRank = null, estRank = null, 
                        inSituRank = null, affymetrixMeanRankNorm = null, rnaSeqMeanRankNorm = null, 
                        estRankNorm = null, inSituRankNorm = null, 
                        affymetrixDistinctRankSum = null, rnaSeqDistinctRankSum = null;
                Map<String, RawExpressionCallDAO.Attribute> colToAttrMap = getColToAttributesMap(comb);

                for (Map.Entry<Integer, String> col : this.getColumnLabels().entrySet()) {
                    String columnName = col.getValue();
                    RawExpressionCallDAO.Attribute attr = getAttributeFromColName(columnName, colToAttrMap);
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
                return log.exit(new RawExpressionCallTO(id, bgeeGeneId, conditionId,
                        affymetrixMeanRank, rnaSeqMeanRank, estRank, inSituRank, 
                        affymetrixMeanRankNorm, rnaSeqMeanRankNorm, estRankNorm, inSituRankNorm,
                        affymetrixDistinctRankSum, rnaSeqDistinctRankSum));
            } catch (SQLException e) {
                throw log.throwing(new DAOException(e));
            }
        }
    }
}
