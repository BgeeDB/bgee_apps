package org.bgee.model.dao.mysql.anatdev.mapping;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.anatdev.mapping.RawSimilarityAnnotationDAO;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.mysql.MySQLDAO;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.model.dao.mysql.connector.MySQLDAOResultSet;


/**
 * A {@code RawSimilarityAnnotationDAO} for MySQL.
 *
 * @author Valentine Rech de Laval
 * @version Bgee 13
 * @see org.bgee.model.dao.api.annotation.anatsimilarity.RawSimilarityAnnotationDAO.RawSimilarityAnnotationTO
 * @since Bgee 13
 */
public class MySQLRawSimilarityAnnotationDAO extends MySQLDAO<RawSimilarityAnnotationDAO.Attribute> 
                                             implements RawSimilarityAnnotationDAO {

    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = 
            LogManager.getLogger(MySQLRawSimilarityAnnotationDAO.class.getName());
    
    /**
     * Constructor providing the {@code MySQLDAOManager} that this {@code MySQLDAO} 
     * will use to obtain {@code BgeeConnection}s.
     *
     * @param manager                       The {@code MySQLDAOManager} to use.
     * @throws IllegalArgumentException     If {@code manager} is {@code null}.
     */
    public MySQLRawSimilarityAnnotationDAO(MySQLDAOManager manager)
            throws IllegalArgumentException {
        super(manager);
    }

    @Override
    public RawSimilarityAnnotationTOResultSet getAllRawSimilarityAnnotations()
            throws DAOException {
        log.entry();
        return log.exit(this.getRawSimilarityAnnotations());
    }

    /**
     * Retrieves raw similarity annotations from data source.
     * 
     * @return              An {@code RawSimilarityAnnotationTOResultSet} containing raw 
     *                      similarity annotations from data source.
     * @throws DAOException If an error occurred when accessing the data source. 
     */
    private RawSimilarityAnnotationTOResultSet getRawSimilarityAnnotations() throws DAOException {
        log.entry();
        
        String tableName = "rawSimilarityAnnotation";
        
        //Construct sql query
        String sql = this.generateSelectClause(this.getAttributes(), tableName);

        sql += " FROM " + tableName;

        //we don't use a try-with-resource, because we return a pointer to the results, 
        //not the actual results, so we should not close this BgeePreparedStatement.
        BgeePreparedStatement stmt = null;
        try {
            stmt = this.getManager().getConnection().prepareStatement(sql.toString());
            return log.exit(new MySQLRawSimilarityAnnotationTOResultSet(stmt));
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
    }

    /**
     * Generates the SELECT clause of a MySQL query used to retrieve 
     * {@code RawSimilarityAnnotationTO}s.
     * 
     * @param attributes                A {@code Set} of {@code Attribute}s defining 
     *                                  the columns/information the query should retrieve.
     * @param tableName                 A {@code String} defining the name of the raw similarity 
     *                                  annotation table used.
     * @return                          A {@code String} containing the SELECT clause 
     *                                  for the requested query.
     * @throws IllegalArgumentException If one {@code Attribute} of {@code attributes} is unknown.
     */
    private String generateSelectClause(Set<RawSimilarityAnnotationDAO.Attribute> attributes,
            String tableName) throws IllegalArgumentException {
        log.entry(attributes, tableName);

        if (attributes == null || attributes.isEmpty()) {
            return log.exit("SELECT * ");
        }
    
        String sql = ""; 
            for (RawSimilarityAnnotationDAO.Attribute attribute: attributes) {
                if (sql.isEmpty()) {
                    sql += "SELECT ";
                    //does the attributes requested ensure that there will be 
                    //no duplicated results?
                    if (!attributes.contains(RawSimilarityAnnotationDAO.Attribute.SUMMARY_SIMILARITY_ANNOTATION_ID) ||  
                        !attributes.contains(RawSimilarityAnnotationDAO.Attribute.REFERENCE_ID) || 
                        !attributes.contains(RawSimilarityAnnotationDAO.Attribute.ECO_ID) || 
                        !attributes.contains(RawSimilarityAnnotationDAO.Attribute.CIO_ID) || 
                        !attributes.contains(RawSimilarityAnnotationDAO.Attribute.NEGATED)) {
                        sql += "DISTINCT ";
                    }
                } else {
                    sql += ", ";
                }
                sql += tableName + ".";
                if (attribute.equals(
                        RawSimilarityAnnotationDAO.Attribute.SUMMARY_SIMILARITY_ANNOTATION_ID)) {
                    sql += "summarySimilarityAnnotationId";
                } else if (attribute.equals(RawSimilarityAnnotationDAO.Attribute.NEGATED)) {
                    sql += "negated";
                } else if (attribute.equals(RawSimilarityAnnotationDAO.Attribute.ECO_ID)) {
                    sql += "ECOId";
                } else if (attribute.equals(RawSimilarityAnnotationDAO.Attribute.CIO_ID)) {
                    sql += "CIOId";
                } else if (attribute.equals(RawSimilarityAnnotationDAO.Attribute.REFERENCE_ID)) {
                    sql += "referenceId";
                } else if (attribute.equals(RawSimilarityAnnotationDAO.Attribute.REFERENCE_TITLE)) {
                    sql += "referenceTitle";
                } else if (attribute.equals(RawSimilarityAnnotationDAO.Attribute.SUPPORTING_TEXT)) {
                    sql += "supportingText";
                } else if (attribute.equals(RawSimilarityAnnotationDAO.Attribute.ASSIGNED_BY)) {
                    sql += "assignedBy";
                } else if (attribute.equals(RawSimilarityAnnotationDAO.Attribute.CURATOR)) {
                    sql += "curator";
                } else if (attribute.equals(RawSimilarityAnnotationDAO.Attribute.ANNOTATION_DATE)) {
                    sql += "annotationDate";
                } else {
                    throw log.throwing(new IllegalArgumentException("The attribute provided (" +
                            attribute.toString() + ") is unknown for " + 
                            RawSimilarityAnnotationDAO.class.getName()));
                }
            }
        return log.exit(sql);
    }
    
    @Override
    public int insertRawSimilarityAnnotations(Collection<RawSimilarityAnnotationTO> rawTOs)
            throws DAOException, IllegalArgumentException {
        log.entry(rawTOs);

        if (rawTOs == null || rawTOs.isEmpty()) {
            throw log.throwing(new IllegalArgumentException(
                    "No raw similarity annotation is given, then no annotation is inserted"));
        }

        int annotationInsertedCount = 0;
        int totalAnnotationNumber = rawTOs.size();
        
        // And we need to build two different queries. 
        String sqlExpression = "INSERT INTO rawSimilarityAnnotation " +
                "(summarySimilarityAnnotationId, negated, ECOId, CIOId, referenceId, " +
                "referenceTitle, supportingText, assignedBy, curator, annotationDate) " +
                "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        // XXX: To not overload MySQL with an error com.mysql.jdbc.PacketTooBigException, 
        // we insert annotations one at a time, but we should insert them several at once,
        // for instance 100 annotations at a time.
        try (BgeePreparedStatement stmt = 
                this.getManager().getConnection().prepareStatement(sqlExpression)) {
            for (RawSimilarityAnnotationTO rawTO: rawTOs) {
                stmt.setInt(1, Integer.parseInt(rawTO.getSummarySimilarityAnnotationId()));
                stmt.setBoolean(2, rawTO.isNegated());
                stmt.setString(3, rawTO.getECOId());
                stmt.setString(4, rawTO.getCIOId());
                stmt.setString(5, rawTO.getReferenceId());
                stmt.setString(6, rawTO.getReferenceTitle());
                stmt.setString(7, rawTO.getSupportingText());
                stmt.setString(8, rawTO.getAssignedBy());
                stmt.setString(9, rawTO.getCurator());
                stmt.setDate(10, rawTO.getAnnotationDate());
                annotationInsertedCount += stmt.executeUpdate();
                stmt.clearParameters();
                if (log.isDebugEnabled() && annotationInsertedCount % 1000 == 0) {
                    log.debug("{}/{} raw similarity annotations inserted", annotationInsertedCount, 
                            totalAnnotationNumber);
                }
            }
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }

        return log.exit(annotationInsertedCount);        
    }
    
    /**
     * A {@code MySQLDAOResultSet} specific to {@code RawSimilarityAnnotationTO}.
     * 
     * @author Valentine Rech de Laval
     * @version Bgee 13
     * @since Bgee 13
     */
    public class MySQLRawSimilarityAnnotationTOResultSet 
                extends MySQLDAOResultSet<RawSimilarityAnnotationTO> 
                implements RawSimilarityAnnotationTOResultSet {

        /**
         * Delegates to {@link MySQLDAOResultSet#MySQLDAOResultSet(BgeePreparedStatement)}
         * super constructor.
         * 
         * @param statement The first {@code BgeePreparedStatement} to execute a query on.
         */
        private MySQLRawSimilarityAnnotationTOResultSet(BgeePreparedStatement statement) {
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
        private MySQLRawSimilarityAnnotationTOResultSet(BgeePreparedStatement statement, 
                int offsetParamIndex, int rowCountParamIndex, int rowCount, 
                boolean filterDuplicates) {
            super(statement, offsetParamIndex, rowCountParamIndex, rowCount, filterDuplicates);
        }

        @Override
        protected RawSimilarityAnnotationTO getNewTO() throws DAOException {
            log.entry();

            String summarySimilarityAnnotationId = null, ecoId = null, cioId = null, 
                    referenceId = null, referenceTitle = null, supportingText = null, 
                    assignedBy = null, curator = null; 
            Boolean negated = null;
            Date annotationDate = null;

            ResultSet currentResultSet = this.getCurrentResultSet();
            for (Entry<Integer, String> column: this.getColumnLabels().entrySet()) {
                try {
                    if (column.getValue().equals("summarySimilarityAnnotationId")) {
                        summarySimilarityAnnotationId = currentResultSet.getString(column.getKey());
                        
                    } else if (column.getValue().equals("negated")) {
                        negated = currentResultSet.getBoolean(column.getKey());

                    } else if (column.getValue().equals("ECOId")) {
                        ecoId = currentResultSet.getString(column.getKey());

                    } else if (column.getValue().equals("CIOId")) {
                        cioId = currentResultSet.getString(column.getKey());

                    } else if (column.getValue().equals("referenceId")) {
                        referenceId = currentResultSet.getString(column.getKey());

                    } else if (column.getValue().equals("referenceTitle")) {
                        referenceTitle = currentResultSet.getString(column.getKey());

                    } else if (column.getValue().equals("supportingText")) {
                        supportingText = currentResultSet.getString(column.getKey());

                    } else if (column.getValue().equals("assignedBy")) {
                        assignedBy = currentResultSet.getString(column.getKey());

                    } else if (column.getValue().equals("curator")) {
                        curator = currentResultSet.getString(column.getKey());

                    } else if (column.getValue().equals("annotationDate")) {
                        annotationDate = currentResultSet.getDate(column.getKey());
                    }

                } catch (SQLException e) {
                    throw log.throwing(new DAOException(e));
                }
            }
            return log.exit(new RawSimilarityAnnotationTO(summarySimilarityAnnotationId, negated, 
                    ecoId, cioId, referenceId, referenceTitle, supportingText, 
                    assignedBy, curator, annotationDate));
        }
    }
}
