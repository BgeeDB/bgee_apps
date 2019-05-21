package org.bgee.model.dao.mysql.expressiondata;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.expressiondata.RawExpressionCallDAO;
import org.bgee.model.dao.mysql.MySQLDAO;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.model.dao.mysql.connector.MySQLDAOResultSet;
import org.bgee.model.dao.mysql.exception.UnrecognizedColumnException;
import org.bgee.model.dao.mysql.gene.MySQLGeneDAO;

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

    public final static String EXPR_TABLE_NAME = "expression";
    public final static String EXPR_ID_FIELD = "expressionId";
    private final static Map<String, RawExpressionCallDAO.Attribute> colToAttrMap;

    static {
        log.entry();
        Map<String, RawExpressionCallDAO.Attribute> colToAttributesMap = new HashMap<>();
        colToAttributesMap.put(EXPR_ID_FIELD, RawExpressionCallDAO.Attribute.ID);
        colToAttributesMap.put(MySQLGeneDAO.BGEE_GENE_ID, RawExpressionCallDAO.Attribute.BGEE_GENE_ID);
        colToAttributesMap.put(MySQLConditionDAO.RAW_COND_ID_FIELD,
                RawExpressionCallDAO.Attribute.CONDITION_ID);
 
        colToAttrMap = Collections.unmodifiableMap(colToAttributesMap);
        log.exit();
    }

    public MySQLRawExpressionCallDAO(MySQLDAOManager manager) throws IllegalArgumentException {
        super(manager);
    }


    @Override
    public RawExpressionCallTOResultSet getExpressionCallsOrderedByGeneIdAndExprId(
            Collection<Integer> geneIds) throws DAOException, IllegalArgumentException {
        log.entry(geneIds);
        
        if (geneIds == null || geneIds.isEmpty() || geneIds.stream().anyMatch(id -> id == null)) {
            throw log.throwing(new IllegalArgumentException("No gene IDs or null gene ID provided"));
        }
        Set<Integer> clonedGeneIds = new HashSet<>(geneIds);

        StringBuilder sb = new StringBuilder();
        sb.append("SELECT ").append(EXPR_TABLE_NAME).append(".*")
          .append(" FROM ").append(EXPR_TABLE_NAME)
          .append(" WHERE ").append(EXPR_TABLE_NAME).append(".")
          .append(MySQLGeneDAO.BGEE_GENE_ID).append(" IN (")
          .append(BgeePreparedStatement.generateParameterizedQueryString(clonedGeneIds.size())).append(")")
          .append(" ORDER BY ").append(EXPR_TABLE_NAME).append(".").append(MySQLGeneDAO.BGEE_GENE_ID)
          .append(", ").append(EXPR_TABLE_NAME).append(".").append(EXPR_ID_FIELD);
        try {
            BgeePreparedStatement stmt = this.getManager().getConnection().prepareStatement(sb.toString());
            stmt.setIntegers(1, clonedGeneIds, true);
            return log.exit(new MySQLRawExpressionCallTOResultSet(stmt));
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
         * @param statement The {@code BgeePreparedStatement}
         * @param comb      The {@code CondParamCombination} allowing to target the appropriate 
         *                  field and table names.
         */
        private MySQLRawExpressionCallTOResultSet(BgeePreparedStatement statement) {
            super(statement);
        }

        @Override
        protected RawExpressionCallDAO.RawExpressionCallTO getNewTO() throws DAOException {
            try {
                log.entry();
                final ResultSet currentResultSet = this.getCurrentResultSet();
                Integer id = null, bgeeGeneId = null, conditionId = null;

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
                        default:
                            log.throwing(new UnrecognizedColumnException(columnName));
                    }
                }
                return log.exit(new RawExpressionCallTO(id, bgeeGeneId, conditionId));
            } catch (SQLException e) {
                throw log.throwing(new DAOException(e));
            }
        }
    }
}
