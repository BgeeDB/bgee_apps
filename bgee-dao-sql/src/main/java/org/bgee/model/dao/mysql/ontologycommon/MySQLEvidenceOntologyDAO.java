package org.bgee.model.dao.mysql.ontologycommon;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.ontologycommon.EvidenceOntologyDAO;
import org.bgee.model.dao.mysql.MySQLDAO;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.model.dao.mysql.connector.MySQLDAOResultSet;
import org.bgee.model.dao.mysql.exception.UnrecognizedColumnException;

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
        return log.traceExit(this.getECOTerms());
    }
    
    /**
     * Retrieves Evidence Ontology terms from data source.
     * 
     * @return              An {@code ECOTermTOResultSet} containing Evidence Ontology terms
     *                      from data source.
     * @throws DAOException If an error occurred when accessing the data source. 
     */
    private ECOTermTOResultSet getECOTerms() throws DAOException {
        log.entry();
        
        String tableName = "evidenceOntology";
        
        //Construct sql query
        String sql = this.generateSelectClause(this.getAttributes(), tableName);

        sql += " FROM " + tableName;

        //we don't use a try-with-resource, because we return a pointer to the results, 
        //not the actual results, so we should not close this BgeePreparedStatement.
        BgeePreparedStatement stmt = null;
        try {
            stmt = this.getManager().getConnection().prepareStatement(sql.toString());
            return log.traceExit(new MySQLECOTermTOResultSet(stmt));
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
    }
    
    /**
     * Generates the SELECT clause of a MySQL query used to retrieve {@code EvidenceOntologyDAO}s.
     * 
     * @param attributes                A {@code Set} of {@code Attribute}s defining 
     *                                  the columns/information the query should retrieve.
     * @param tableName                 A {@code String} defining the name of the evidence 
     *                                  ontology table used.
     * @return                          A {@code String} containing the SELECT clause 
     *                                  for the requested query.
     * @throws IllegalArgumentException If one {@code Attribute} of {@code attributes} is unknown.
     */
    private String generateSelectClause(Set<EvidenceOntologyDAO.Attribute> attributes,
            String tableName) throws IllegalArgumentException {
        log.entry(attributes, tableName);

        if (attributes == null || attributes.isEmpty()) {
            return log.traceExit("SELECT " + tableName + ".* ");
        }
    
        String sql = ""; 
            for (EvidenceOntologyDAO.Attribute attribute: attributes) {
                if (sql.isEmpty()) {
                    sql += "SELECT ";
                    //does the attributes requested ensure that there will be 
                    //no duplicated results?
                    if (!attributes.contains(EvidenceOntologyDAO.Attribute.ID)) {
                        sql += "DISTINCT ";
                    }
                } else {
                    sql += ", ";
                }
                sql += tableName + ".";
                if (attribute.equals(
                        EvidenceOntologyDAO.Attribute.ID)) {
                    sql += "ECOId";
                } else if (attribute.equals(EvidenceOntologyDAO.Attribute.NAME)) {
                    sql += "ECOName";
                } else if (attribute.equals(EvidenceOntologyDAO.Attribute.DESCRIPTION)) {
                    sql += "ECODescription";
                } else {
                    throw log.throwing(new IllegalArgumentException("The attribute provided (" +
                            attribute.toString() + ") is unknown for " + 
                            EvidenceOntologyDAO.class.getName()));
                }
            }
        return log.traceExit(sql);
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
        
        // And we need to build two different queries. 
        String sqlExpression = "INSERT INTO evidenceOntology " +
                "(ECOId, ECOName, ECODescription) values (?, ?, ?)";
        
        // XXX: To not overload MySQL with an error com.mysql.jdbc.PacketTooBigException, 
        // we insert terms one at a time, but we should insert them several at once,
        // for instance 100 terms at a time.
        try (BgeePreparedStatement stmt = 
                this.getManager().getConnection().prepareStatement(sqlExpression)) {
            for (ECOTermTO ecoTO: ecoTOs) {
                stmt.setString(1, ecoTO.getId());
                stmt.setString(2, ecoTO.getName());
                stmt.setString(3, ecoTO.getDescription());
                termInsertedCount += stmt.executeUpdate();
                stmt.clearParameters();
            }
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }

        return log.traceExit(termInsertedCount);        
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

            for (Entry<Integer, String> column: this.getColumnLabels().entrySet()) {
                try {
                    if (column.getValue().equals("ECOId")) {
                        id = this.getCurrentResultSet().getString(column.getKey());
                        
                    } else if (column.getValue().equals("ECOName")) {
                        name = this.getCurrentResultSet().getString(column.getKey());

                    } else if (column.getValue().equals("ECODescription")) {
                        description = this.getCurrentResultSet().getString(column.getKey());
                    } else {
                        throw log.throwing(new UnrecognizedColumnException(column.getValue()));
                    }

                } catch (SQLException e) {
                    throw log.throwing(new DAOException(e));
                }
            }
            return log.traceExit(new ECOTermTO(id, name, description));
        }
    }
}
