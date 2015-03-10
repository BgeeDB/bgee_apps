package org.bgee.model.dao.mysql.ontologycommon;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map.Entry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.ontologycommon.EvidenceOntologyDAO;
import org.bgee.model.dao.mysql.MySQLDAO;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.model.dao.mysql.connector.MySQLDAOResultSet;

/**
 * A {@code EvidenceOntologyDAO} for MySQL.
 *
 * @author Valentine Rech de Laval
 * @version Bgee 13
 * @see org.bgee.model.dao.api.ontologycommon.EvidenceOntologyDAO.ECOTermTO
 * @since Bgee 13
 */
public class MySQLEvidenceOntologyDAO extends MySQLDAO<EvidenceOntologyDAO.Attribute> 
                                      implements EvidenceOntologyDAO {

    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = 
            LogManager.getLogger(MySQLEvidenceOntologyDAO.class.getName());
    
    /**
     * Constructor providing the {@code MySQLDAOManager} that this {@code MySQLDAO} 
     * will use to obtain {@code BgeeConnection}s.
     *
     * @param manager                       The {@code MySQLDAOManager} to use.
     * @throws IllegalArgumentException     If {@code manager} is {@code null}.
     */
    public MySQLEvidenceOntologyDAO(MySQLDAOManager manager) throws IllegalArgumentException {
        super(manager);
    }

    @Override
    public ECOTermTOResultSet getAllECOTerms() throws DAOException {
        log.entry();
        // TODO Auto-generated method stub
        return log.exit(null);
    }

    @Override
    public int insertECOTerms(Collection<ECOTermTO> ecoTOs) 
            throws DAOException, IllegalArgumentException {
        log.entry(ecoTOs);
        
        if (ecoTOs == null || ecoTOs.isEmpty()) {
            throw log.throwing(new IllegalArgumentException(
                    "No ECO term is given, then no term is inserted"));
        }

        int termInsertedCount = 0;
        int totalTermNumber = ecoTOs.size();
        
        // And we need to build two different queries. 
        String sqlExpression = "INSERT INTO evidenceOntology " +
                "(ECOId, ECOName, ECODescription) values (?, ?, ?)";
        
        // XXX: To not overload MySQL with an error com.mysql.jdbc.PacketTooBigException, 
        // we insert terms one at a time, but we should insert them several at once,
        // for instance 100 terms at a time.
        try (BgeePreparedStatement stmt = 
                this.getManager().getConnection().prepareStatement(sqlExpression)) {
            for (ECOTermTO ecoTO: ecoTOs) {
                stmt.setInt(1, Integer.parseInt(ecoTO.getId()));
                stmt.setString(2, ecoTO.getName());
                stmt.setString(3, ecoTO.getDescription());
                termInsertedCount += stmt.executeUpdate();
                stmt.clearParameters();
                if (log.isDebugEnabled() && termInsertedCount % 1000 == 0) {
                    log.debug("{}/{} ECO terms inserted", termInsertedCount, totalTermNumber);
                }
            }
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }

        return log.exit(termInsertedCount);        
    }
    
    /**
     * A {@code MySQLDAOResultSet} specific to {@code ECOTermTO}.
     * 
     * @author Valentine Rech de Laval
     * @version Bgee 13
     * @since Bgee 13
     */
    public class MySQLECOTermTOResultSet extends MySQLDAOResultSet<ECOTermTO> 
                                         implements ECOTermTOResultSet {

        /**
         * Delegates to {@link MySQLDAOResultSet#MySQLDAOResultSet(BgeePreparedStatement)}
         * super constructor.
         * 
         * @param statement The first {@code BgeePreparedStatement} to execute a query on.
         */
        private MySQLECOTermTOResultSet(BgeePreparedStatement statement) {
            super(statement);
        }
        
        /**
         * Delegates to {@link MySQLDAOResultSet#MySQLDAOResultSet(BgeePreparedStatement, 
         * int, int, int)} super constructor.
         * 
         * @param statement             The first {@code BgeePreparedStatement} to execute 
         *                              a query on.
         * @param offsetParamIndex      An {@code int} that is the index of the parameter 
         *                              defining the offset argument of a LIMIT clause, 
         *                              in the SQL query hold by {@code statement}.
         * @param rowCountParamIndex    An {@code int} that is the index of the parameter 
         *                              specifying the maximum number of rows to return 
         *                              in a LIMIT clause, in the SQL query 
         *                              hold by {@code statement}.
         * @param rowCount              An {@code int} that is the maximum number of rows to use 
         *                              in a LIMIT clause, in the SQL query 
         *                              hold by {@code statement}.
         * @param filterDuplicates      A {@code boolean} defining whether equal 
         *                              {@code TransferObject}s returned by different queries should 
         *                              be filtered: when {@code true}, only one of them will be 
         *                              returned. This implies that all {@code TransferObject}s 
         *                              returned will be stored, implying potentially 
         *                              great memory usage.
         */
        private MySQLECOTermTOResultSet(BgeePreparedStatement statement, int offsetParamIndex, 
                int rowCountParamIndex, int rowCount, boolean filterDuplicates) {
            super(statement, offsetParamIndex, rowCountParamIndex, rowCount, filterDuplicates);
        }

        @Override
        protected ECOTermTO getNewTO() throws DAOException {
            log.entry();

            String id = null, name = null, description = null;

            ResultSet currentResultSet = this.getCurrentResultSet();
            for (Entry<Integer, String> column: this.getColumnLabels().entrySet()) {
                try {
                    if (column.getValue().equals("ECOId")) {
                        id = currentResultSet.getString(column.getKey());
                        
                    } else if (column.getValue().equals("ECOName")) {
                        name = currentResultSet.getString(column.getKey());

                    } else if (column.getValue().equals("ECODescription")) {
                        description = currentResultSet.getString(column.getKey());
                    }

                } catch (SQLException e) {
                    throw log.throwing(new DAOException(e));
                }
            }
            return log.exit(new ECOTermTO(id, name, description));
        }
    }
}
