package org.bgee.model.dao.mysql.annotation;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map.Entry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.annotation.RawSimilarityAnnotationDAO;
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
 * @see org.bgee.model.dao.api.annotation.RawSimilarityAnnotationDAO.RawSimilarityAnnotationTO
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
        // TODO Auto-generated method stub
        return log.exit(null);
    }

    @Override
    public int insertRawSimilarityAnnotations(Collection<RawSimilarityAnnotationTO> rawTOs)
            throws DAOException, IllegalArgumentException {
        log.entry(rawTOs);
        // TODO Auto-generated method stub
        return log.exit(0);        
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
