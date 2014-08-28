package org.bgee.model.dao.mysql.expressiondata;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.mysql.MySQLDAO;
import org.bgee.model.dao.mysql.connector.BgeeCallableStatement;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.model.dao.mysql.connector.MySQLDAOResultSet;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.expressiondata.ExpressionCallDAO;
import org.bgee.model.dao.api.expressiondata.ExpressionCallParams;
import org.bgee.model.dao.api.expressiondata.CallTO.DataState;

/**
 * A {@code ExpressionCallDAO} for MySQL. 
 * 
 * @author Valentine Rech de Laval
 * @version Bgee 13
 * @see org.bgee.model.dao.api.gene.ExpressionCallDAO.ExpressionCallTO
 * @since Bgee 13
 */
public class MySQLExpressionCallDAO extends MySQLDAO<ExpressionCallDAO.Attribute> 
                                    implements ExpressionCallDAO {

    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = LogManager.getLogger(MySQLExpressionCallDAO.class.getName());

    /**
     * Constructor providing the {@code MySQLDAOManager} that this {@code MySQLDAO} 
     * will use to obtain {@code BgeeConnection}s.
     * 
     * @param manager                       The {@code MySQLDAOManager} to use.
     * @throws IllegalArgumentException     If {@code manager} is {@code null}.
     */
    public MySQLExpressionCallDAO(MySQLDAOManager manager) throws IllegalArgumentException {
        super(manager);
    }

    @Override
    public ExpressionCallTOResultSet getAllExpressionCalls(ExpressionCallParams params) 
            throws DAOException {
        log.entry(params);

        String sql = "{call getAllExpression(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}";

        //we don't use a try-with-resource, because we return a pointer to the results, 
        //not the actual results, so we should not close this BgeeCallableStatement.
        BgeeCallableStatement callStmt = null;
        try {
            callStmt = this.getManager().getConnection().prepareCall(sql);
            callStmt.setString(1, this.createStringFromSet(params.getGeneIds()));
            callStmt.setString(2, this.createStringFromSet(params.getAnatEntityIds()));
            callStmt.setString(3, this.createStringFromSet(params.getDevStageIds()));
            callStmt.setString(4, this.createStringFromSet(params.getSpeciesIds()));
            callStmt.setInt(5, this.getMinLevelData(params.getAffymetrixData()));
            callStmt.setInt(6, this.getMinLevelData(params.getESTData()));
            callStmt.setInt(7, this.getMinLevelData(params.getInSituData()));
            callStmt.setInt(8, this.getMinLevelData(params.getRNASeqData()));
            callStmt.setBoolean(9, params.isIncludeSubStages());
            callStmt.setBoolean(10, params.isIncludeSubstructures());
            callStmt.setBoolean(11, params.isAllDataTypes());
            callStmt.setBoolean(12, params.isUseAnatDescendants());
            callStmt.setBoolean(13, params.isUseDevDescendants());
            return log.exit(new MySQLExpressionCallTOResultSet(callStmt));
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }

    }
    
    /**
     * Return the index of the given {@code DataState}.
     * <p>
     * Note that it returns the index starting with 1 according to MySQL enum.
     * 
     * @param dataState The {@code DataState} defining the requested minimum contribution 
     *                  to the generation of the calls to be used.
     * @return
     */
    private int getMinLevelData(DataState dataState) {
        log.entry(dataState);
        return log.exit(DataState.valueOf(dataState.toString()).ordinal() + 1);
    }

    /**
     * Create a {@code String} composed with all {@code String}s of a {@code Set} separated 
     * by a coma.
     * <p>
     * That methods is useful for passing a list of {@code String} (for instance, IDs) to a store
     * procedure that does not accept {@code Collection} or array.
     * 
     * @param set   A {@code Set} of {@code String}s that must be put into a single {@code String}.
     * @return      A {@code String} composed with all {@code String}s of a {@code Set} separated 
     *              by a coma. If {@code Set} is null or empty, returns an empty {@code String}.
     */
    private String createStringFromSet(Set<String> set) {
        log.entry(set);
        if (set == null || set.size() ==0) {
            return log.exit("");
        }
        StringBuilder myString = new StringBuilder();
        Iterator<String> i= set.iterator();
        boolean isFirst = true;
        while(i.hasNext() ) {
            if (!isFirst && set.size() > 1) {
                myString.append("|");
            }
            myString.append(i.next());
            isFirst = false;
        }
        return log.exit(myString.toString());
    }

    /**
     * A {@code MySQLDAOResultSet} specific to {@code ExpressionCallTO}.
     * 
     * @author Valentine Rech de Laval
     * @version Bgee 13
     * @since Bgee 13
     */
    public class MySQLExpressionCallTOResultSet extends MySQLDAOResultSet<ExpressionCallTO> 
                                                implements ExpressionCallTOResultSet {

        /**
         * Delegates to {@link MySQLDAOResultSet#MySQLDAOResultSet(BgeePreparedStatement)
         * super constructor.
         * 
         * @param statement The first {@code BgeePreparedStatement} to execute a query on.
         */
        public MySQLExpressionCallTOResultSet(BgeePreparedStatement statement) {
            super(statement);
        }

        @Override
        public ExpressionCallTO getTO() throws DAOException {
            log.entry();

            String geneId = null, anatEntityId = null, devStageId = null;
            DataState affymetrixData = DataState.NODATA, estData = DataState.NODATA, 
                    inSituData = DataState.NODATA, relaxedInSituData = DataState.NODATA, 
                    rnaSeqData = DataState.NODATA;
            boolean includeSubstructures = false, includeSubStages = false; 

            ResultSet currentResultSet = this.getCurrentResultSet();
            for (Entry<Integer, String> column: this.getColumnLabels().entrySet()) {
                try {
                    if (column.getValue().equals("geneId")) {
                        geneId = currentResultSet.getString(column.getKey());

                    } else if (column.getValue().equals("anatEntityId")) {
                        anatEntityId = currentResultSet.getString(column.getKey());

                    } else if (column.getValue().equals("stageId")) {
                        devStageId = currentResultSet.getString(column.getKey());

                    } else if (column.getValue().equals("affymetrixData")) {
                        affymetrixData = convertBgeeDataQualityToDataState(
                                currentResultSet.getString(column.getKey()));

                    } else if (column.getValue().equals("estData")) {
                        estData = convertBgeeDataQualityToDataState(
                                currentResultSet.getString(column.getKey()));

                    } else if (column.getValue().equals("inSituData")) {
                        inSituData = convertBgeeDataQualityToDataState(
                                currentResultSet.getString(column.getKey()));

                    } else if (column.getValue().equals("rnaSeqData")) {
                        rnaSeqData = convertBgeeDataQualityToDataState(
                                currentResultSet.getString(column.getKey()));
                    }

                } catch (SQLException e) {
                    throw log.throwing(new DAOException(e));
                }
            }
            log.debug("geneId="+geneId+", anatEntityId="+anatEntityId+", devStageId="+devStageId+", "+
                    "affymetrixData="+affymetrixData+", estData="+estData+", inSituData="+inSituData+", " +
                    "relaxedInSituData="+relaxedInSituData+", rnaSeqData="+rnaSeqData+", "+
                    "includeSubstructures="+includeSubstructures+", includeSubStages="+includeSubStages);
            //TODO set good includeSubstructures, includeSubStages
            return log.exit(new ExpressionCallTO(geneId, anatEntityId, devStageId,
                    affymetrixData, estData, inSituData, relaxedInSituData, rnaSeqData,
                    includeSubstructures, includeSubStages));
        }
        
        /**
         * Convert Bgee database expression data qualities into {@code DataState}.
         * 
         * @param databaseEnum
         * @return
         */
        private DataState convertBgeeDataQualityToDataState(String databaseEnum) {
            log.entry(databaseEnum);
            
            DataState dataState = null;
            if (databaseEnum.equals("no data")) {
                dataState = DataState.NODATA;
            } else if (databaseEnum.equals("poor quality")) {
                dataState = DataState.LOWQUALITY;
            } else if (databaseEnum.equals("high quality")) {
                dataState = DataState.HIGHQUALITY;
            } 
            
            return log.exit(dataState);
        }
    }
}
