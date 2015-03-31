package org.bgee.model.dao.mysql.anatdev.mapping;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.anatdev.mapping.SummarySimilarityAnnotationDAO;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.mysql.MySQLDAO;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.model.dao.mysql.connector.MySQLDAOResultSet;
import org.bgee.model.dao.mysql.exception.UnrecognizedColumnException;

/**
 * A {@code SummarySimilarityAnnotationDAO} for MySQL.
 *
 * @author Valentine Rech de Laval
 * @author Frederic Bastian
 * @version Bgee 13 Mar. 2015
 * @see org.bgee.model.dao.api.annotation.anatsimilarity.SummarySimilarityAnnotationDAO.SummarySimilarityAnnotationTO
 * @since Bgee 13
 */
public class MySQLSummarySimilarityAnnotationDAO 
                                extends MySQLDAO<SummarySimilarityAnnotationDAO.Attribute> 
                                implements SummarySimilarityAnnotationDAO {

    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = 
            LogManager.getLogger(MySQLSummarySimilarityAnnotationDAO.class.getName());
    
    /**
     * Constructor providing the {@code MySQLDAOManager} that this {@code MySQLDAO} 
     * will use to obtain {@code BgeeConnection}s.
     *
     * @param manager                       The {@code MySQLDAOManager} to use.
     * @throws IllegalArgumentException     If {@code manager} is {@code null}.
     */
    public MySQLSummarySimilarityAnnotationDAO(MySQLDAOManager manager)
            throws IllegalArgumentException {
        super(manager);
    }

    @Override
    //TODO: integration test
    public SummarySimilarityAnnotationTOResultSet getAllSummarySimilarityAnnotations()
            throws DAOException {
        log.entry();
        
        String tableName = "summarySimilarityAnnotation";
        
        //Construct sql query
        String sql = this.generateSelectClause(this.getAttributes(), tableName, 
                !this.getAttributes().contains(SummarySimilarityAnnotationDAO.Attribute.ID));

        sql += " FROM " + tableName;

        //we don't use a try-with-resource, because we return a pointer to the results, 
        //not the actual results, so we should not close this BgeePreparedStatement.
        try {
            return log.exit(new MySQLSummarySimilarityAnnotationTOResultSet(
                    this.getManager().getConnection().prepareStatement(sql)));
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
    }

    @Override
    //A warning is issued because we do not close the BgeePreparedStatement we use, 
    //but if we closed the PreparedStatement, it would close the ResultSet returned. 
    //The BgeePreparedStatement will be closed when the ResultSet will be closed. 
    @SuppressWarnings("resource")
    //TODO: integration test
    public SummarySimilarityAnnotationTOResultSet getSummarySimilarityAnnotations(
            String taxonId) throws DAOException {
        log.entry(taxonId);
        
        String sql = this.generateSelectClause(this.getAttributes(), "t3", true);
        sql += " FROM taxon AS t1 INNER JOIN taxon AS t2 "
                + "ON t2.taxonLeftBound <= t1.taxonLeftBound AND "
                + "t2.taxonRightBound >= t1.taxonRightBound "
                + "INNER JOIN summarySimilarityAnnotation AS t3 "
                + "ON t3.taxonId = t2.taxonId "
                + "WHERE t1.taxonId = ?";
        
        //we don't use a try-with-resource, because we return a pointer to the results, 
        //not the actual results, so we should not close this BgeePreparedStatement.
        try {
            BgeePreparedStatement stmt = this.getManager().getConnection().prepareStatement(sql);
            stmt.setString(1, taxonId);
            return log.exit(new MySQLSummarySimilarityAnnotationTOResultSet(stmt));
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
    }

    @Override
    //A warning is issued because we do not close the BgeePreparedStatement we use, 
    //but if we closed the PreparedStatement, it would close the ResultSet returned. 
    //The BgeePreparedStatement will be closed when the ResultSet will be closed. 
    @SuppressWarnings("resource")
    //TODO: integration test
    public SimAnnotToAnatEntityTOResultSet getSimAnnotToAnatEntity(String taxonId, 
            Set<String> speciesIds) throws DAOException {
        log.entry(taxonId, speciesIds);
        
        String sql = this.getAnnotToAnatEntityQueryStart();
        
        if (speciesIds != null && !speciesIds.isEmpty()) {
            //retrieve structures existing in ALL requested species
            sql += "AND (EXISTS (SELECT 1 FROM anatEntityTaxonConstraint AS t5 "
                    + "WHERE t5.anatEntityId = t4.anatEntityId AND t5.speciesId IS NULL) OR (";
            for (int i = 0; i < speciesIds.size(); i++) {
                if (i > 0) {
                    sql += "AND ";
                }
                sql += "EXISTS (SELECT 1 FROM anatEntityTaxonConstraint AS t5 "
                    + "WHERE t5.anatEntityId = t4.anatEntityId AND t5.speciesId = ?) ";
            }
            sql += ")) ";
        }
        
        //we don't use a try-with-resource, because we return a pointer to the results, 
        //not the actual results, so we should not close this BgeePreparedStatement.
        try {
            BgeePreparedStatement stmt = this.getManager().getConnection().prepareStatement(sql);
            stmt.setString(1, taxonId);
            if (speciesIds != null && !speciesIds.isEmpty()) {
                List<Integer> orderedSpeciesIds = MySQLDAO.convertToIntList(speciesIds);
                Collections.sort(orderedSpeciesIds);
                stmt.setIntegers(2, orderedSpeciesIds);
            }
            return log.exit(new MySQLSimAnnotToAnatEntityTOResultSet(stmt));
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
    }

    @Override
    //A warning is issued because we do not close the BgeePreparedStatement we use, 
    //but if we closed the PreparedStatement, it would close the ResultSet returned. 
    //The BgeePreparedStatement will be closed when the ResultSet will be closed. 
    @SuppressWarnings("resource")
    //TODO: integration test
    public SimAnnotToAnatEntityTOResultSet getSimAnnotToLostAnatEntity(String taxonId, 
            Set<String> speciesIds) throws DAOException, IllegalArgumentException {
        log.entry(taxonId, speciesIds);
        if (speciesIds == null || speciesIds.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("Some species must be provided."));
        }
        
        String sql = this.getAnnotToAnatEntityQueryStart();
        
        //retrieve structures existing in ALL requested species
        sql += "AND NOT EXISTS (SELECT 1 FROM anatEntityTaxonConstraint AS t5 "
                + "WHERE t5.anatEntityId = t4.anatEntityId AND t5.speciesId IS NULL) ";
        for (int i = 0; i < speciesIds.size(); i++) {
            sql += "AND NOT EXISTS (SELECT 1 FROM anatEntityTaxonConstraint AS t5 "
                    + "WHERE t5.anatEntityId = t4.anatEntityId AND t5.speciesId = ?) ";
        }
        
        //we don't use a try-with-resource, because we return a pointer to the results, 
        //not the actual results, so we should not close this BgeePreparedStatement.
        try {
            BgeePreparedStatement stmt = this.getManager().getConnection().prepareStatement(sql);
            stmt.setString(1, taxonId);
            List<Integer> orderedSpeciesIds = MySQLDAO.convertToIntList(speciesIds);
            Collections.sort(orderedSpeciesIds);
            stmt.setIntegers(2, orderedSpeciesIds);
            return log.exit(new MySQLSimAnnotToAnatEntityTOResultSet(stmt));
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
    }
    
    /**
     * Generates the SELECT clause of a MySQL query used to retrieve 
     * {@code SummarySimilarityAnnotationTO}s.
     * 
     * @param attributes                A {@code Set} of {@code Attribute}s defining 
     *                                  the columns/information the query should retrieve.
     * @param tableName                 A {@code String} defining the name of the summary similarity 
     *                                  annotation table used.
     * @param distinct                  A {@code boolean} defining whether the 'DISTINCT' option 
     *                                  should be used in the 'SELECT' clause.
     * @return                          A {@code String} containing the SELECT clause 
     *                                  for the requested query.
     * @throws IllegalArgumentException If one {@code Attribute} of {@code attributes} is unknown.
     */
    private String generateSelectClause(Set<SummarySimilarityAnnotationDAO.Attribute> attributes,
            String tableName, boolean distinct) throws IllegalArgumentException {
        log.entry(attributes, tableName, distinct);
    
        String sql = "SELECT "; 
        if (distinct) {
            sql += "DISTINCT ";
        }
        if (attributes == null || attributes.isEmpty()) {
            return log.exit(sql + tableName + ".* ");
        }
    
        boolean firstIteration = true;
        for (SummarySimilarityAnnotationDAO.Attribute attribute: attributes) {
            if (!firstIteration) {
                sql += ", ";
            }
            sql += tableName + ".";
            if (attribute.equals(SummarySimilarityAnnotationDAO.Attribute.ID)) {
                sql += "summarySimilarityAnnotationId";
            } else if (attribute.equals(SummarySimilarityAnnotationDAO.Attribute.TAXON_ID)) {
                sql += "taxonId";
            } else if (attribute.equals(SummarySimilarityAnnotationDAO.Attribute.NEGATED)) {
                sql += "negated";
            } else if (attribute.equals(SummarySimilarityAnnotationDAO.Attribute.CIO_ID)) {
                sql += "CIOId";
            } else {
                throw log.throwing(new IllegalArgumentException("The attribute provided (" +
                        attribute.toString() + ") is unknown for " + 
                        SummarySimilarityAnnotationDAO.class.getName()));
            }
            firstIteration = false;
        }
        return log.exit(sql);
    }

    /**
     * @return  A {@code String} that is the beginning of a SQL query allowing to retrieve 
     *          mappings from similarity annotations to anatomical entities, 
     *          valid in a provided taxon.
     */
    private String getAnnotToAnatEntityQueryStart() {
        log.entry();
        
        String sql = "SELECT DISTINCT t4.* "
                + "FROM taxon AS t1 INNER JOIN taxon AS t2 "
                + "ON t2.taxonLeftBound <= t1.taxonLeftBound AND "
                + "t2.taxonRightBound >= t1.taxonRightBound "
                + "INNER JOIN summarySimilarityAnnotation AS t3 ON t3.taxonId = t2.taxonId "
                + "INNER JOIN similarityAnnotationToAnatEntityId AS t4 "
                + "ON t4.summarySimilarityAnnotationId = t3.summarySimilarityAnnotationId "
                + "WHERE t4.negated = 0 AND t1.taxonId = ? "
                //check that this is the similarity annotated to the most recent valid taxon 
                //for this anatomical structure.
                + "AND NOT EXISTS "
                    + "(SELECT 1 FROM summarySimilarityAnnotation AS t30 "
                    + "INNER JOIN taxon AS t10 ON t30.taxonId = t10.taxonId "
                    + "INNER JOIN similarityAnnotationToAnatEntityId AS t40 "
                    + "ON t40.summarySimilarityAnnotationId = t30.summarySimilarityAnnotationId "
                    //search for different annotations including the same organ
                    + "WHERE t40.anatEntityId = t4.anatEntityId AND "
                    + "t30.summarySimilarityAnnotationId != t3.summarySimilarityAnnotationId "
                    //that are annotated to more recent taxa
                    + "AND t10.taxonLeftBound > t2.taxonLeftBound AND "
                    + "t10.taxonRightBound < t2.taxonRightBound AND "
                    //but that are still annotated to a a valid requested taxon
                    + "t10.taxonLeftBound <= t1.taxonLeftBound AND "
                    + "t10.taxonRightBound >= t1.taxonRightBound) ";
        
        return log.exit(sql);
    }

    @Override
    public int insertSummarySimilarityAnnotations(
            Collection<SummarySimilarityAnnotationTO> summaryTOs)
            throws DAOException, IllegalArgumentException {
        log.entry(summaryTOs);

        if (summaryTOs == null || summaryTOs.isEmpty()) {
            throw log.throwing(new IllegalArgumentException(
                    "No summary similarity annotation is given, then no annotation is inserted"));
        }

        int annotationInsertedCount = 0;
        int totalAnnotationNumber = summaryTOs.size();
        
        // And we need to build two different queries. 
        String sqlExpression = "INSERT INTO summarySimilarityAnnotation " +
                "(summarySimilarityAnnotationId, taxonId, negated, CIOId) " +
                "values (?, ?, ?, ?)";
        
        // XXX: To not overload MySQL with an error com.mysql.jdbc.PacketTooBigException, 
        // we insert annotations one at a time, but we should insert them several at once,
        // for instance 100 annotations at a time.
        try (BgeePreparedStatement stmt = 
                this.getManager().getConnection().prepareStatement(sqlExpression)) {
            for (SummarySimilarityAnnotationTO summaryTO: summaryTOs) {
                stmt.setInt(1, Integer.parseInt(summaryTO.getId()));
                stmt.setInt(2, Integer.parseInt(summaryTO.getTaxonId()));
                stmt.setBoolean(3, summaryTO.isNegated());
                stmt.setString(4, summaryTO.getCIOId());
                annotationInsertedCount += stmt.executeUpdate();
                stmt.clearParameters();
                if (log.isDebugEnabled() && annotationInsertedCount % 1000 == 0) {
                    log.debug("{}/{} summary similarity annotations inserted", 
                            annotationInsertedCount, totalAnnotationNumber);
                }
            }
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }

        return log.exit(annotationInsertedCount);        
    }
    
    @Override
    public int insertSimilarityAnnotationsToAnatEntityIds(
            Collection<SimAnnotToAnatEntityTO> simAnnotToAnatEntityTOs) 
                    throws DAOException, IllegalArgumentException {
        log.entry(simAnnotToAnatEntityTOs);
        
        if (simAnnotToAnatEntityTOs == null || simAnnotToAnatEntityTOs.isEmpty()) {
            throw log.throwing(new IllegalArgumentException(
                    "No summary similarity annotation is given, then no annotation is inserted"));
        }

        int rowInsertedCount = 0;
        int totalToNumber = simAnnotToAnatEntityTOs.size();
        
        // And we need to build two different queries. 
        String sqlExpression = "INSERT INTO similarityAnnotationToAnatEntityId " +
                "(summarySimilarityAnnotationId, anatEntityId) values (?, ?)";
        
        // XXX: To not overload MySQL with an error com.mysql.jdbc.PacketTooBigException, 
        // we insert relations one at a time, but we should insert them several at once,
        // for instance 100 relations at a time.
        try (BgeePreparedStatement stmt = 
                this.getManager().getConnection().prepareStatement(sqlExpression)) {
            for (SimAnnotToAnatEntityTO simAnnotToAnatEntityTO: simAnnotToAnatEntityTOs) {
                stmt.setInt(1, 
                        Integer.parseInt(simAnnotToAnatEntityTO.getSummarySimilarityAnnotationId()));
                stmt.setString(2, simAnnotToAnatEntityTO.getAnatEntityId());
                rowInsertedCount += stmt.executeUpdate();
                stmt.clearParameters();
                if (log.isDebugEnabled() && rowInsertedCount % 1000 == 0) {
                    log.debug("{}/{} similarity annotation to anat. entity inserted", 
                            rowInsertedCount, totalToNumber);
                }
            }
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }

        return log.exit(rowInsertedCount);        
    }

    /**
     * A {@code MySQLDAOResultSet} specific to {@code SummarySimilarityAnnotationTO}.
     * 
     * @author Valentine Rech de Laval
     * @version Bgee 13
     * @since Bgee 13
     */
    public class MySQLSummarySimilarityAnnotationTOResultSet 
                extends MySQLDAOResultSet<SummarySimilarityAnnotationTO> 
                implements SummarySimilarityAnnotationTOResultSet {

        /**
         * Delegates to {@link MySQLDAOResultSet#MySQLDAOResultSet(BgeePreparedStatement)}
         * super constructor.
         * 
         * @param statement The first {@code BgeePreparedStatement} to execute a query on.
         */
        private MySQLSummarySimilarityAnnotationTOResultSet(BgeePreparedStatement statement) {
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
        private MySQLSummarySimilarityAnnotationTOResultSet(BgeePreparedStatement statement, 
                int offsetParamIndex, int rowCountParamIndex, int rowCount, 
                boolean filterDuplicates) {
            super(statement, offsetParamIndex, rowCountParamIndex, rowCount, filterDuplicates);
        }

        @Override
        protected SummarySimilarityAnnotationTO getNewTO() throws DAOException {
            log.entry();

            String id = null, taxonId = null, cioId = null; 
            Boolean negated = null;

            for (Entry<Integer, String> column: this.getColumnLabels().entrySet()) {
                try {
                    if (column.getValue().equals("summarySimilarityAnnotationId")) {
                        id = this.getCurrentResultSet().getString(column.getKey());
                        
                    } else if (column.getValue().equals("taxonId")) {
                        taxonId = this.getCurrentResultSet().getString(column.getKey());

                    } else if (column.getValue().equals("negated")) {
                        negated = this.getCurrentResultSet().getBoolean(column.getKey());

                    } else if (column.getValue().equals("CIOId")) {
                        cioId = this.getCurrentResultSet().getString(column.getKey());
                    } else {
                        throw log.throwing(new UnrecognizedColumnException(column.getValue()));
                    }

                } catch (SQLException e) {
                    throw log.throwing(new DAOException(e));
                }
            }
            return log.exit(new SummarySimilarityAnnotationTO(id, taxonId, negated, cioId));
        }
    }
    
    /**
     * A {@code MySQLDAOResultSet} specific to {@code SimAnnotToAnatEntityTO}.
     * 
     * @author Valentine Rech de Laval
     * @version Bgee 13
     * @since Bgee 13
     */
    public class MySQLSimAnnotToAnatEntityTOResultSet 
                extends MySQLDAOResultSet<SimAnnotToAnatEntityTO> 
                implements SimAnnotToAnatEntityTOResultSet {

        /**
         * Delegates to {@link MySQLDAOResultSet#MySQLDAOResultSet(BgeePreparedStatement)}
         * super constructor.
         * 
         * @param statement The first {@code BgeePreparedStatement} to execute a query on.
         */
        private MySQLSimAnnotToAnatEntityTOResultSet(BgeePreparedStatement statement) {
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
        private MySQLSimAnnotToAnatEntityTOResultSet(BgeePreparedStatement statement, 
                int offsetParamIndex, int rowCountParamIndex, int rowCount, 
                boolean filterDuplicates) {
            super(statement, offsetParamIndex, rowCountParamIndex, rowCount, filterDuplicates);
        }
        
        @Override
        protected SimAnnotToAnatEntityTO getNewTO() throws DAOException {
            log.entry();

            String summarySimilarityAnnotationId = null, anatEntityId = null; 

            for (Entry<Integer, String> column: this.getColumnLabels().entrySet()) {
                try {
                    if (column.getValue().equals("summarySimilarityAnnotationId")) {
                        summarySimilarityAnnotationId = this.getCurrentResultSet().getString(column.getKey());
                        
                    } else if (column.getValue().equals("anatEntityId")) {
                        anatEntityId = this.getCurrentResultSet().getString(column.getKey());

                    } else {
                        throw log.throwing(new UnrecognizedColumnException(column.getValue()));
                    }

                } catch (SQLException e) {
                    throw log.throwing(new DAOException(e));
                }
            }
            return log.exit(new SimAnnotToAnatEntityTO(summarySimilarityAnnotationId, anatEntityId));
        }
    }
}
