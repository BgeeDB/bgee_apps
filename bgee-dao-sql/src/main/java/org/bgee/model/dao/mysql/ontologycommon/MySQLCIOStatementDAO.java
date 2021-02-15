package org.bgee.model.dao.mysql.ontologycommon;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.ontologycommon.CIOStatementDAO;
import org.bgee.model.dao.api.ontologycommon.CIOStatementDAO.CIOStatementTO.ConfidenceLevel;
import org.bgee.model.dao.api.ontologycommon.CIOStatementDAO.CIOStatementTO.EvidenceConcordance;
import org.bgee.model.dao.api.ontologycommon.CIOStatementDAO.CIOStatementTO.EvidenceTypeConcordance;
import org.bgee.model.dao.mysql.MySQLDAO;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.model.dao.mysql.connector.MySQLDAOResultSet;
import org.bgee.model.dao.mysql.exception.UnrecognizedColumnException;

/**
 * A {@code CIOStatementDAO} for MySQL.
 *
 * @author Valentine Rech de Laval
 * @version Bgee 13
 * @see org.bgee.model.dao.api.ontologycommon.CIOStatementDAO.CIOStatementTO
 * @since Bgee 13
 */
public class MySQLCIOStatementDAO extends MySQLDAO<CIOStatementDAO.Attribute> 
                                  implements CIOStatementDAO {

    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = 
            LogManager.getLogger(MySQLCIOStatementDAO.class.getName());
    
    /**
     * Constructor providing the {@code MySQLDAOManager} that this {@code MySQLDAO} 
     * will use to obtain {@code BgeeConnection}s.
     *
     * @param manager                       The {@code MySQLDAOManager} to use.
     * @throws IllegalArgumentException     If {@code manager} is {@code null}.
     */
    public MySQLCIOStatementDAO(MySQLDAOManager manager) throws IllegalArgumentException {
        super(manager);
    }

    @Override
    public CIOStatementTOResultSet getAllCIOStatements() throws DAOException {
        log.entry();
        
        String tableName = "CIOStatement";
        
        //Construct sql query
        String sql = this.generateSelectClause(this.getAttributes(), tableName, 
                !this.getAttributes().contains(CIOStatementDAO.Attribute.ID));

        sql += " FROM " + tableName;

        //we don't use a try-with-resource, because we return a pointer to the results, 
        //not the actual results, so we should not close this BgeePreparedStatement.
        try {
            return log.traceExit(new MySQLCIOStatementTOResultSet(
                    this.getManager().getConnection().prepareStatement(sql)));
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
    }

    /**
     * Generates the SELECT clause of a MySQL query used to retrieve 
     * {@code CIOStatement}s.
     * 
     * @param attributes                A {@code Set} of {@code Attribute}s defining 
     *                                  the columns/information the query should retrieve.
     * @param tableName                 A {@code String} defining the name of the CIO statement 
     *                                  table used.
     * @param distinct                  A {@code boolean} defining whether the 'DISTINCT' option 
     *                                  should be used in the 'SELECT' clause.
     * @return                          A {@code String} containing the SELECT clause 
     *                                  for the requested query.
     * @throws IllegalArgumentException If one {@code Attribute} of {@code attributes} is unknown.
     */
    private String generateSelectClause(Set<CIOStatementDAO.Attribute> attributes,
            String tableName, boolean distinct) throws IllegalArgumentException {
        log.entry(attributes, tableName, distinct);

        if (attributes == null || attributes.isEmpty()) {
            return log.traceExit("SELECT " + tableName + ".* ");
        }
    
        String sql = ""; 
            for (CIOStatementDAO.Attribute attribute: attributes) {
                if (sql.isEmpty()) {
                    sql += "SELECT ";
                    if (distinct) {
                        sql += "DISTINCT ";
                    }
                } else {
                    sql += ", ";
                }
                sql += tableName + ".";
                if (attribute.equals(CIOStatementDAO.Attribute.ID)) {
                    sql += "CIOId";
                } else if (attribute.equals(CIOStatementDAO.Attribute.NAME)) {
                    sql += "CIOName";
                } else if (attribute.equals(CIOStatementDAO.Attribute.DESCRIPTION)) {
                    sql += "CIODescription";
                } else if (attribute.equals(CIOStatementDAO.Attribute.TRUSTED)) {
                    sql += "trusted";
                } else if (attribute.equals(CIOStatementDAO.Attribute.CONFIDENCE_LEVEL)) {
                    sql += "confidenceLevel";
                } else if (attribute.equals(CIOStatementDAO.Attribute.EVIDENCE_CONCORDANCE)) {
                    sql += "evidenceConcordance";
                } else if (attribute.equals(CIOStatementDAO.Attribute.EVIDENCE_TYPE_CONCORDANCE)) {
                    sql += "evidenceTypeConcordance";
                } else {
                    throw log.throwing(new IllegalArgumentException("The attribute provided (" +
                            attribute.toString() + ") is unknown for " + 
                            CIOStatementDAO.class.getName()));
                }
            }
        return log.traceExit(sql);
    }

    @Override
    public int insertCIOStatements(Collection<CIOStatementTO> cioTOs)
            throws DAOException, IllegalArgumentException {
        log.entry(cioTOs);
        
        if (cioTOs == null || cioTOs.isEmpty()) {
            throw log.throwing(new IllegalArgumentException(
                    "No CIO statement is given, then no statement is inserted"));
        }

        int stmtInsertedCount = 0;
        
        // And we need to build two different queries. 
        String sqlExpression = "INSERT INTO CIOStatement " +
                "(CIOId, CIOName, CIODescription, trusted, confidenceLevel, evidenceConcordance," +
                "evidenceTypeConcordance) values (?, ?, ?, ?, ?, ?, ?)";

        // XXX: To not overload MySQL with an error com.mysql.jdbc.PacketTooBigException, 
        // we insert statements one at a time, but we should insert them several at once,
        // for instance 100 statements at a time.
        try (BgeePreparedStatement stmt = 
                this.getManager().getConnection().prepareStatement(sqlExpression)) {
            for (CIOStatementTO cioTO: cioTOs) {
                stmt.setString(1, cioTO.getId());
                stmt.setString(2, cioTO.getName());
                stmt.setString(3, cioTO.getDescription());
                stmt.setBoolean(4, cioTO.isTrusted());
                stmt.setEnumDAOField(5, cioTO.getConfidenceLevel());
                stmt.setEnumDAOField(6, cioTO.getEvidenceConcordance());
                stmt.setEnumDAOField(7, cioTO.getEvidenceTypeConcordance());
                stmtInsertedCount += stmt.executeUpdate();
                stmt.clearParameters();
            }
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }

        return log.traceExit(stmtInsertedCount);        
    }
    
    /**
     * A {@code MySQLDAOResultSet} specific to {@code CIOStatementTO}.
     * 
     * @author Valentine Rech de Laval
     * @version Bgee 13
     * @since Bgee 13
     */
    public class MySQLCIOStatementTOResultSet extends MySQLDAOResultSet<CIOStatementTO> 
                                              implements CIOStatementTOResultSet {

        /**
         * Delegates to {@link MySQLDAOResultSet#MySQLDAOResultSet(BgeePreparedStatement)}
         * super constructor.
         * 
         * @param statement The first {@code BgeePreparedStatement} to execute a query on.
         */
        private MySQLCIOStatementTOResultSet(BgeePreparedStatement statement) {
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
        private MySQLCIOStatementTOResultSet(BgeePreparedStatement statement, int offsetParamIndex, 
                int rowCountParamIndex, int rowCount, boolean filterDuplicates) {
            super(statement, offsetParamIndex, rowCountParamIndex, rowCount, filterDuplicates);
        }

        @Override
        protected CIOStatementTO getNewTO() throws DAOException {
            log.entry();

            String id = null, name = null, description = null;
            Boolean trusted = null;
            ConfidenceLevel confidenceLevel = null;
            EvidenceConcordance evidenceConcordance = null;
            EvidenceTypeConcordance evidenceTypeConcordance = null;
            
            for (Entry<Integer, String> column: this.getColumnLabels().entrySet()) {
                try {
                    if (column.getValue().equals("CIOId")) {
                        id = this.getCurrentResultSet().getString(column.getKey());
                        
                    } else if (column.getValue().equals("CIOName")) {
                        name = this.getCurrentResultSet().getString(column.getKey());

                    } else if (column.getValue().equals("CIODescription")) {
                        description = this.getCurrentResultSet().getString(column.getKey());

                    } else if (column.getValue().equals("trusted")) {
                        trusted = this.getCurrentResultSet().getBoolean(column.getKey());

                    } else if (column.getValue().equals("confidenceLevel")) {
                        confidenceLevel = ConfidenceLevel.convertToConfidenceLevel(
                                this.getCurrentResultSet().getString(column.getKey()));

                    } else if (column.getValue().equals("evidenceConcordance")) {
                        evidenceConcordance = EvidenceConcordance.convertToEvidenceConcordance(
                                this.getCurrentResultSet().getString(column.getKey()));

                    } else if (column.getValue().equals("evidenceTypeConcordance")) {
                        evidenceTypeConcordance = 
                                EvidenceTypeConcordance.convertToEvidenceTypeConcordance(
                                this.getCurrentResultSet().getString(column.getKey()));
                    } else {
                        throw log.throwing(new UnrecognizedColumnException(column.getValue()));
                    }

                } catch (SQLException e) {
                    throw log.throwing(new DAOException(e));
                }
            }
            return log.traceExit(new CIOStatementTO(id, name, description, trusted, 
                    confidenceLevel, evidenceConcordance, evidenceTypeConcordance));
        }
    }
}
