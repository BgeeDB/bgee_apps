package org.bgee.model.dao.mysql.anatdev.mapping;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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
 * @version Bgee 14 Mar. 2019
 * @see org.bgee.model.dao.api.anatdev.mapping.SummarySimilarityAnnotationDAO.SummarySimilarityAnnotationTO
 * @since Bgee 13
 */
public class MySQLSummarySimilarityAnnotationDAO 
                                extends MySQLDAO<SummarySimilarityAnnotationDAO.Attribute> 
                                implements SummarySimilarityAnnotationDAO {
    private final static Logger log =
            LogManager.getLogger(MySQLSummarySimilarityAnnotationDAO.class.getName());

    /**
     * A {@code Map} of column name to their corresponding {@code Attribute}.
     */
    private static final Map<String, SummarySimilarityAnnotationDAO.Attribute> COL_TO_ATTR_MAP;
    private static final String SUMMARY_SIM_ANNOT_TABLE = "summarySimilarityAnnotation";
    private static final String SIM_ANNOT_TO_ANAT_ENTITY_TABLE = "similarityAnnotationToAnatEntityId";
    private static final String SUMMARY_SIM_ID_FIELD = "summarySimilarityAnnotationId";
    static {
        COL_TO_ATTR_MAP = new HashMap<>();
        COL_TO_ATTR_MAP.put("summarySimilarityAnnotationId", SummarySimilarityAnnotationDAO.Attribute.ID);
        COL_TO_ATTR_MAP.put("taxonId", SummarySimilarityAnnotationDAO.Attribute.TAXON_ID);
        COL_TO_ATTR_MAP.put("negated", SummarySimilarityAnnotationDAO.Attribute.NEGATED);
        COL_TO_ATTR_MAP.put("CIOId", SummarySimilarityAnnotationDAO.Attribute.CIO_ID);
    }

    private static String generateTableRef(Integer taxonId, boolean ancestralTaxaAnnots,
            boolean descentTaxaAnnots, Boolean trusted) {
        log.entry(taxonId, ancestralTaxaAnnots, descentTaxaAnnots, trusted);
        if (taxonId == null || taxonId <= 0) {
            throw log.throwing(new IllegalArgumentException("Valid taxon ID must be provided"));
        }
        StringBuilder sb = new StringBuilder();

        boolean getAncOrDes = taxonId != null && (ancestralTaxaAnnots || descentTaxaAnnots);
        sb.append(" FROM ");
        if (getAncOrDes) {
            sb.append("taxon AS t1 ");
            //retrieve the ancestors, and the requested taxon itself
            if (ancestralTaxaAnnots) {
                sb.append("INNER JOIN taxon AS t2 ")
                  .append("ON t2.taxonLeftBound <= t1.taxonLeftBound AND ")
                  .append("t2.taxonRightBound >= t1.taxonRightBound ");
            }
            //retrieve the descendants, and the requested taxon itself
            if (descentTaxaAnnots) {
                sb.append("INNER JOIN taxon AS t3 ")
                  .append("ON t3.taxonLeftBound >= t1.taxonLeftBound AND ")
                  .append("t3.taxonRightBound <= t1.taxonRightBound ");
            }
            sb.append("INNER JOIN ");
        }
        sb.append(SUMMARY_SIM_ANNOT_TABLE);
        if (getAncOrDes) {
            sb.append(" ON ");
            if (ancestralTaxaAnnots) {
                sb.append("t2.taxonId = ").append(SUMMARY_SIM_ANNOT_TABLE).append(".taxonId ");
            }
            if (descentTaxaAnnots) {
                if (ancestralTaxaAnnots) {
                    sb.append(" OR ");
                }
                sb.append("t3.taxonId = ").append(SUMMARY_SIM_ANNOT_TABLE).append(".taxonId ");
            }
        }
        if (trusted != null) {
            sb.append(" INNER JOIN CIOStatement AS t4 ")
              .append("ON t4.CIOId = ").append(SUMMARY_SIM_ANNOT_TABLE).append(".CIOId ");
        }

        return log.exit(sb.toString());
    }
    private static String generateWhereCond(Integer taxonId, boolean ancestralTaxaAnnots,
            boolean descentTaxaAnnots, Boolean positiveAnnots, Boolean trusted) {
        log.entry(taxonId, ancestralTaxaAnnots, descentTaxaAnnots, positiveAnnots, trusted);
        if (taxonId == null || taxonId <= 0) {
            throw log.throwing(new IllegalArgumentException("Valid taxon ID must be provided"));
        }
        StringBuilder sb = new StringBuilder();

        String targetTaxonIdTable = SUMMARY_SIM_ANNOT_TABLE;
        boolean getAncOrDes = taxonId != null && (ancestralTaxaAnnots || descentTaxaAnnots);
        if (getAncOrDes) {
            targetTaxonIdTable = "t1";
        }

        boolean whereClauseStarted = false;
        if (taxonId != null) {
            sb.append(" WHERE ").append(targetTaxonIdTable).append(".taxonId = ?");
            whereClauseStarted = true;
        }
        if (positiveAnnots != null) {
            if (!whereClauseStarted) {
                sb.append(" WHERE ");
            } else {
                sb.append(" AND ");
            }
            sb.append(SUMMARY_SIM_ANNOT_TABLE).append(".negated = ?");
            whereClauseStarted = true;
        }
        if (trusted != null) {
            if (!whereClauseStarted) {
                sb.append(" WHERE ");
            } else {
                sb.append(" AND ");
            }
            sb.append("t4.trusted = ?");
            whereClauseStarted = true;
        }
    
        return log.exit(sb.toString());
    }
    private static void parameterizeStatement(BgeePreparedStatement stmt, Integer taxonId,
            Boolean positiveAnnots, Boolean trusted) throws SQLException {
        log.entry(stmt, taxonId, positiveAnnots, trusted);
        int paramIndex = 1;
        if (taxonId != null) {
            stmt.setInt(paramIndex, taxonId);
            paramIndex++;
        }
        if (positiveAnnots != null) {
            stmt.setBoolean(paramIndex, !positiveAnnots);
            paramIndex++;
        }
        if (trusted != null) {
            stmt.setBoolean(paramIndex, trusted);
            paramIndex++;
        }
    }

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
    public SummarySimilarityAnnotationTOResultSet getAllSummarySimilarityAnnotations()
            throws DAOException {
        log.entry();
        return log.exit(this.getSummarySimilarityAnnotations(null, false, false, null, null, null));
    }
    @Override
    public SummarySimilarityAnnotationTOResultSet getSummarySimilarityAnnotations(
            Integer taxonId, boolean ancestralTaxaAnnots, boolean descentTaxaAnnots,
            Boolean positiveAnnots, Boolean trusted,
            Collection<SummarySimilarityAnnotationDAO.Attribute> attrs)
                    throws DAOException, IllegalArgumentException {
        log.entry(taxonId, ancestralTaxaAnnots, descentTaxaAnnots, positiveAnnots, trusted, attrs);

        Set<SummarySimilarityAnnotationDAO.Attribute> clonedAttrs = Collections.unmodifiableSet(
                attrs == null? new HashSet<>(): new HashSet<>(attrs));

        StringBuilder sb = new StringBuilder(generateSelectClause(SUMMARY_SIM_ANNOT_TABLE, COL_TO_ATTR_MAP,
                true, clonedAttrs));
        sb.append(generateTableRef(taxonId, ancestralTaxaAnnots, descentTaxaAnnots, trusted));
        sb.append(generateWhereCond(taxonId, ancestralTaxaAnnots, descentTaxaAnnots,
                positiveAnnots, trusted));

        //we don't use a try-with-resource, because we return a pointer to the results,
        //not the actual results, so we should not close this BgeePreparedStatement.
        try {
            BgeePreparedStatement stmt = this.getManager().getConnection().prepareStatement(sb.toString());
            parameterizeStatement(stmt, taxonId, positiveAnnots, trusted);
            return log.exit(new MySQLSummarySimilarityAnnotationTOResultSet(stmt));
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
    }

    @Override
    public SimAnnotToAnatEntityTOResultSet getAllSimAnnotToAnatEntity() {
        log.entry();
        return log.exit(this.getSimAnnotToAnatEntity(null, false, false, null, null));
    }
    @Override
    public SimAnnotToAnatEntityTOResultSet getSimAnnotToAnatEntity(Integer taxonId,
            boolean ancestralTaxaAnnots, boolean descentTaxaAnnots,
            Boolean positiveAnnots, Boolean trusted) throws DAOException, IllegalArgumentException {
        log.entry(taxonId, ancestralTaxaAnnots, descentTaxaAnnots, positiveAnnots, trusted);

        StringBuilder sb = new StringBuilder("SELECT DISTINCT ");
        sb.append(SIM_ANNOT_TO_ANAT_ENTITY_TABLE).append(".* ");

        sb.append(generateTableRef(taxonId, ancestralTaxaAnnots, descentTaxaAnnots, trusted));
        sb.append(" INNER JOIN ").append(SIM_ANNOT_TO_ANAT_ENTITY_TABLE)
          .append(" ON ").append(SIM_ANNOT_TO_ANAT_ENTITY_TABLE).append(".").append(SUMMARY_SIM_ID_FIELD)
          .append(" = ").append(SUMMARY_SIM_ANNOT_TABLE).append(".").append(SUMMARY_SIM_ID_FIELD);
        sb.append(generateWhereCond(taxonId, ancestralTaxaAnnots, descentTaxaAnnots,
                positiveAnnots, trusted));
        
        //we don't use a try-with-resource, because we return a pointer to the results, 
        //not the actual results, so we should not close this BgeePreparedStatement.
        try {
            BgeePreparedStatement stmt = this.getManager().getConnection().prepareStatement(sb.toString());
            parameterizeStatement(stmt, taxonId, positiveAnnots, trusted);
            return log.exit(new MySQLSimAnnotToAnatEntityTOResultSet(stmt));
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
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
                stmt.setInt(1, summaryTO.getId());
                stmt.setInt(2, summaryTO.getTaxonId());
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
                stmt.setInt(1, simAnnotToAnatEntityTO.getSummarySimilarityAnnotationId());
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

            String cioId = null;
            Integer id = null, taxonId = null;
            Boolean negated = null;

            try {
                for (Entry<Integer, String> column: this.getColumnLabels().entrySet()) {
                    String columnName = column.getValue();
                    int columnIndex = column.getKey();
                    SummarySimilarityAnnotationDAO.Attribute attr = getAttributeFromColName(
                            columnName, COL_TO_ATTR_MAP);
                    switch (attr) {
                    case ID:
                        id = this.getCurrentResultSet().getInt(columnIndex);
                        break;
                    case TAXON_ID:
                        taxonId = this.getCurrentResultSet().getInt(columnIndex);
                        break;
                    case NEGATED:
                        negated = this.getCurrentResultSet().getBoolean(columnIndex);
                        break;
                    case CIO_ID:
                        cioId = this.getCurrentResultSet().getString(columnIndex);
                        break;
                    default:
                        log.throwing(new UnrecognizedColumnException(columnName));
                    }
                }
                return log.exit(new SummarySimilarityAnnotationTO(id, taxonId, negated, cioId));

            } catch (SQLException e) {
                throw log.throwing(new DAOException(e));
            }
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

            Integer summarySimilarityAnnotationId = null;
            String anatEntityId = null; 

            for (Entry<Integer, String> column: this.getColumnLabels().entrySet()) {
                try {
                    if (column.getValue().equals("summarySimilarityAnnotationId")) {
                        summarySimilarityAnnotationId = this.getCurrentResultSet().getInt(column.getKey());
                        
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

