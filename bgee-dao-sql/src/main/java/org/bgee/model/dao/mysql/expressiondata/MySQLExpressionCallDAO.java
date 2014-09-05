package org.bgee.model.dao.mysql.expressiondata;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.mysql.MySQLDAO;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.model.dao.mysql.connector.MySQLDAOResultSet;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.expressiondata.ExpressionCallDAO;
import org.bgee.model.dao.api.expressiondata.ExpressionCallDAO.ExpressionCallTO.OriginOfLineType;
import org.bgee.model.dao.api.expressiondata.ExpressionCallParams;
import org.bgee.model.dao.api.expressiondata.CallDAO.CallTO.DataState;
import org.bgee.model.dao.api.expressiondata.CallDAO.CallTO;

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
        return log.exit(
                getAllExpressionCalls(params.getSpeciesIds(), params.isIncludeSubstructures())); 
        
//        TODO use the store procedure instead of BgeePreparedStatement.
//        String sql = "{call getAllExpression(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}";
//
//        //we don't use a try-with-resource, because we return a pointer to the results, 
//        //not the actual results, so we should not close this BgeeCallableStatement.
//        BgeeCallableStatement callStmt = null;
//        try {
//            callStmt = this.getManager().getConnection().prepareCall(sql);
//            callStmt.setString(1, createStringFromSet(params.getGeneIds(), '|'));
//            callStmt.setString(2, createStringFromSet(params.getAnatEntityIds(), '|'));
//            callStmt.setString(3, createStringFromSet(params.getDevStageIds(), '|'));
//            callStmt.setString(4, createStringFromSet(params.getSpeciesIds(), '|'));
//            callStmt.setInt(5, CallTO.getMinLevelData(params.getAffymetrixData(), '|'));
//            callStmt.setInt(6, CallTO.getMinLevelData(params.getESTData()));
//            callStmt.setInt(7, CallTO.getMinLevelData(params.getInSituData()));
//            callStmt.setInt(8, CallTO.getMinLevelData(params.getRNASeqData()));
//            callStmt.setBoolean(9, params.isIncludeSubStages());
//            callStmt.setBoolean(10, params.isIncludeSubstructures());
//            callStmt.setBoolean(11, params.isAllDataTypes());
//            callStmt.setBoolean(12, params.isUseAnatDescendants());
//            callStmt.setBoolean(13, params.isUseDevDescendants());
//            return log.exit(new MySQLExpressionCallTOResultSet(callStmt));
//        } catch (SQLException e) {
//            throw log.throwing(new DAOException(e));
//        }
    }

    /**
     * Retrieve all expression calls from data source according to a {@code Set} of {@code String}s 
     * that are the IDs of species allowing to filter the calls to use, and a {@code boolean} 
     * defining whether this expression call was generated using data from the anatomical entity 
     * with the ID {@link CallTO#getAnatEntityId()} alone, or by also considering all its 
     * descendants by <em>is_a</em> or <em>part_of</em> relations, even indirect. 
     * <p>
     * The expression calls are retrieved and returned as a {@code ExpressionCallTOResultSet}. 
     * It is the responsibility of the caller to close this {@code DAOResultSet} once 
     * results are retrieved.
     * 
     * @param speciesIds            A {@code Set} of {@code String}s that are the IDs of species 
     *                              allowing to filter the calls to use
     * @param includeSubstructures  A {@code boolean} defining whether descendants 
     *                              of the anatomical entity were considered.
     * @return                      An {@code ExpressionCallTOResultSet} containing all expression 
     *                              calls from data source.
     * @throws DAOException         If a {@code SQLException} occurred while trying to get 
     *                              expression calls.   
     */
    public ExpressionCallTOResultSet getAllExpressionCalls(Set<String> speciesIds, 
            boolean isIncludeSubstructures) throws DAOException {
        log.entry(speciesIds, isIncludeSubstructures);

        Collection<ExpressionCallDAO.Attribute> attributes = this.getAttributes();
        
        String tableName = "expression";
        if (isIncludeSubstructures) {
            tableName = "globalExpression";
        }
        //Construct sql query
        StringBuilder sql = new StringBuilder(); 
        if (attributes == null || attributes.size() == 0) {
            sql.append("SELECT *");
        } else {
            for (ExpressionCallDAO.Attribute attribute: attributes) {
                if (sql.length() == 0) {
                    sql.append("SELECT ");
                } else {
                    sql.append(", ");
                }
                sql.append(tableName);
                sql.append(".");
                sql.append(this.attributeToString(attribute, isIncludeSubstructures));
            }
        }
        sql.append(" FROM ");
        sql.append(tableName);
         if (speciesIds != null && speciesIds.size() > 0) {
             sql.append(" INNER JOIN gene ON (gene.geneId = ");
             sql.append(tableName + ".geneId)");
             sql.append(" WHERE gene.speciesId IN (");
             sql.append(createStringFromSet(speciesIds, ','));
             sql.append(")");
             sql.append(" ORDER BY gene.speciesId, ");
             sql.append(tableName + ".geneId, ");
             sql.append(tableName + ".anatEntityId, ");
             sql.append(tableName + ".stageId");
         } else {
             sql.append(" ORDER BY ");
             sql.append(tableName + ".geneId, ");
             sql.append(tableName + ".anatEntityId, ");
             sql.append(tableName + ".stageId");
         }

        //we don't use a try-with-resource, because we return a pointer to the results, 
        //not the actual results, so we should not close this BgeePreparedStatement.
        BgeePreparedStatement stmt = null;
        try {
            stmt = this.getManager().getConnection().prepareStatement(sql.toString());
            return log.exit(new MySQLExpressionCallTOResultSet(stmt));
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
    }
    
    /** 
     * Return a {@code String} that correspond to the given {@code ExpressionCallDAO.Attribute}.
     * 
     * @param attribute   An {code ExpressionCallDAO.Attribute} that is the attribute to be
     *                    converted into a {@code String}.
     * @return            A {@code String} that correspond to the given 
     *                    {@code ExpressionCallDAO.Attribute}
     */
    private String attributeToString(ExpressionCallDAO.Attribute attribute, 
            boolean isIncludeSubstructures) {
        log.entry(attribute, isIncludeSubstructures);

        String label = null;
        if (attribute.equals(ExpressionCallDAO.Attribute.ID)) {
            if (isIncludeSubstructures) {
                label = "globalExpressionId";
            } else {
                label = "expressionId";
            }
        } else if (attribute.equals(ExpressionCallDAO.Attribute.GENEID)) {
            label = "geneId";
        } else if (attribute.equals(ExpressionCallDAO.Attribute.DEVSTAGEID)) {
            label = "stageId";
        } else if (attribute.equals(ExpressionCallDAO.Attribute.ANATENTITYID)) {
            label = "anatEntityId";
        } else if (attribute.equals(ExpressionCallDAO.Attribute.AFFYMETRIXDATA)) {
            label = "affymetrixData";
        } else if (attribute.equals(ExpressionCallDAO.Attribute.ESTDATA)) {
            label = "estData";
        } else if (attribute.equals(ExpressionCallDAO.Attribute.INSITUDATA)) {
            label = "inSituData";
        } else if (attribute.equals(ExpressionCallDAO.Attribute.RNASEQDATA)) {
            label = "rnaSeqData";
        } else if (attribute.equals(ExpressionCallDAO.Attribute.ORIGINOFLINE)) {
            if (isIncludeSubstructures) {
                label = "originOfLine";
            } else {
                throw log.throwing(new IllegalStateException("No originOfLine in expression table"));
            }
        } else if (attribute.equals(ExpressionCallDAO.Attribute.RELAXEDINSITUDATA)) {
            throw log.throwing(new IllegalStateException("No relaxed in situ data in data source" +
                                                         "for the moment"));
        } else if (attribute.equals(ExpressionCallDAO.Attribute.INCLUDESUBSTRUCTURES) ||
                attribute.equals(ExpressionCallDAO.Attribute.INCLUDESUBSTAGES)) {
            throw log.throwing(new IllegalStateException(attribute.toString() + 
                    "is not a column of the expression and globalExpression tables"));
        } else {
            throw log.throwing(new IllegalStateException("The attribute provided (" +
                    attribute.toString() + ") is unknown for " + ExpressionCallDAO.class.getName()));
        }
        
        return log.exit(label);
    }

    @Override
    public int insertExpressionCalls(Collection<ExpressionCallTO> expressionCalls) {
        log.entry(expressionCalls);
        
        int callInsertedCount = 0;

        // According to isIncludeSubstructures(), the ExpressionCallTO is inserted in 
        // expression or globalExpression table. As prepared statement is for the 
        // column values not for table name, we need to separate ExpressionCallTOs into
        // two separated collections. 
        Collection<ExpressionCallTO> toInsertInExpression = new ArrayList<ExpressionCallTO>();
        Collection<ExpressionCallTO> toInsertInGlobalExpression = new ArrayList<ExpressionCallTO>();
        for (ExpressionCallTO call: expressionCalls) {
            if (call.isIncludeSubstructures()) {
                toInsertInGlobalExpression.add(call);
            } else {
                toInsertInExpression.add(call);
            }
        }

        // And we need to build two different queries. 
        String sqlExpression = "INSERT INTO expression " +
                "(expressionId, geneId, anatEntityId, stageId, "+
                "estData, affymetrixData, inSituData, rnaSeqData) " +
                "values (?, ?, ?, ?, ?, ?, ?, ?)";
        
        // To not overload MySQL with an error com.mysql.jdbc.PacketTooBigException, 
        // and because of laziness, we insert expression calls one at a time
        try (BgeePreparedStatement stmt = 
                this.getManager().getConnection().prepareStatement(sqlExpression)) {
            for (ExpressionCallTO call: toInsertInExpression) {
                stmt.setInt(1, Integer.parseInt(call.getId()));
                stmt.setString(2, call.getGeneId());
                stmt.setString(3, call.getAnatEntityId());
                stmt.setString(4, call.getDevStageId());
                stmt.setString(5, CallTO.convertDataStateToDataSourceQuality(call.getESTData()));
                stmt.setString(6, CallTO.convertDataStateToDataSourceQuality(call.getAffymetrixData()));
                stmt.setString(7, CallTO.convertDataStateToDataSourceQuality(call.getInSituData()));
                stmt.setString(8, CallTO.convertDataStateToDataSourceQuality(call.getRNASeqData()));
                callInsertedCount += stmt.executeUpdate();
                stmt.clearParameters();
            }
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }

        String sqlGlobalExpression = "INSERT INTO globalExpression " +
                "(globalExpressionId, geneId, anatEntityId, stageId, "+
                "estData, affymetrixData, inSituData, rnaSeqData, originOfLine) " +
                "values (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (BgeePreparedStatement stmt = 
                this.getManager().getConnection().prepareStatement(sqlGlobalExpression)) {
            for (ExpressionCallTO call: toInsertInGlobalExpression) {
                stmt.setInt(1, Integer.parseInt(call.getId()));
                stmt.setString(2, call.getGeneId());
                stmt.setString(3, call.getAnatEntityId());
                stmt.setString(4, call.getDevStageId());
                stmt.setString(5, CallTO.convertDataStateToDataSourceQuality(call.getESTData()));
                stmt.setString(6, CallTO.convertDataStateToDataSourceQuality(call.getAffymetrixData()));
                stmt.setString(7, CallTO.convertDataStateToDataSourceQuality(call.getInSituData()));
                stmt.setString(8, CallTO.convertDataStateToDataSourceQuality(call.getRNASeqData()));
                stmt.setString(9, ExpressionCallTO.
                        convertOriginOfLineTypeToDatasourceEnum(call.getOriginOfLine()));
                callInsertedCount += stmt.executeUpdate();
                stmt.clearParameters();
            }
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
        return log.exit(callInsertedCount);
    }

    @Override
    public int insertGlobalExpressionToExpression(
            Collection<GlobalExpressionToExpressionTO> globalExpressionToExpression) {
        log.entry(globalExpressionToExpression);
        
        int rowInsertedCount = 0;

        // And we need to build two different queries. 
        String sqlExpression = "INSERT INTO globalExpressionToExpression " +
                "(globalExpressionId, expressionId) values (?, ?)";
        
        // To not overload MySQL with an error com.mysql.jdbc.PacketTooBigException, 
        // and because of laziness, we insert rows one at a time
        try (BgeePreparedStatement stmt = 
                this.getManager().getConnection().prepareStatement(sqlExpression)) {
            for (GlobalExpressionToExpressionTO call: globalExpressionToExpression) {
                stmt.setString(1, call.getGlobalExpressionId());
                stmt.setString(2, call.getExpressionId());
                rowInsertedCount += stmt.executeUpdate();
                stmt.clearParameters();
            }
            return log.exit(rowInsertedCount);
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
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

            String id = null, geneId = null, anatEntityId = null, devStageId = null;
            DataState affymetrixData = DataState.NODATA, estData = DataState.NODATA, 
                    inSituData = DataState.NODATA, relaxedInSituData = DataState.NODATA, 
                    rnaSeqData = DataState.NODATA;
            boolean includeSubstructures = false, includeSubStages = false;
            OriginOfLineType originOfLine = OriginOfLineType.SELF;

            boolean isGlobalExpression = false;
            ResultSet currentResultSet = this.getCurrentResultSet();
            for (Entry<Integer, String> column: this.getColumnLabels().entrySet()) {
                try {
                    if (column.getValue().equals("expressionId")) {
                        id = currentResultSet.getString(column.getKey());
                        
                    } else if (column.getValue().equals("globalExpressionId")) {
                            id = currentResultSet.getString(column.getKey());

                    } else if (column.getValue().equals("geneId")) {
                        geneId = currentResultSet.getString(column.getKey());

                    } else if (column.getValue().equals("anatEntityId")) {
                        anatEntityId = currentResultSet.getString(column.getKey());

                    } else if (column.getValue().equals("stageId")) {
                        devStageId = currentResultSet.getString(column.getKey());

                    } else if (column.getValue().equals("affymetrixData")) {
                        affymetrixData = CallTO.convertDataSourceQualityToDataState(
                                currentResultSet.getString(column.getKey()));

                    } else if (column.getValue().equals("estData")) {
                        estData = CallTO.convertDataSourceQualityToDataState(
                                currentResultSet.getString(column.getKey()));

                    } else if (column.getValue().equals("inSituData")) {
                        inSituData = CallTO.convertDataSourceQualityToDataState(
                                currentResultSet.getString(column.getKey()));

                    } else if (column.getValue().equals("relaxedInSituData")) {
                        relaxedInSituData = CallTO.convertDataSourceQualityToDataState(
                                currentResultSet.getString(column.getKey()));

                    } else if (column.getValue().equals("rnaSeqData")) {
                        rnaSeqData = CallTO.convertDataSourceQualityToDataState(
                                currentResultSet.getString(column.getKey()));
                        
                    } else if (column.getValue().equals("originOfLine")) {
                        originOfLine = ExpressionCallTO.convertDatasourceEnumToOriginOfLineType(
                                currentResultSet.getString(column.getKey()));
                        isGlobalExpression = true;
                    }

                } catch (SQLException e) {
                    throw log.throwing(new DAOException(e));
                }
            }
            
            if (isGlobalExpression) {
                includeSubstructures = true;
            }
            
            //TODO manage includeSubStages when complete query will be write
            return log.exit(new ExpressionCallTO(id, geneId, anatEntityId, devStageId,
                    affymetrixData, estData, inSituData, relaxedInSituData, rnaSeqData,
                    includeSubstructures, includeSubStages, originOfLine));
        }
    }
    
    /**
     * A {@code MySQLDAOResultSet} specific to {@code GlobalExpressionToExpressionTO}.
     * 
     * @author Valentine Rech de Laval
     * @version Bgee 13
     * @since Bgee 13
     */
    public class MySQLGlobalExpressionToExpressionTOResultSet 
                                         extends MySQLDAOResultSet<GlobalExpressionToExpressionTO> 
                                         implements GlobalExpressionToExpressionTOResultSet {
        /**
         * Delegates to {@link MySQLDAOResultSet#MySQLDAOResultSet(BgeePreparedStatement)
         * super constructor.
         * 
         * @param statement The first {@code BgeePreparedStatement} to execute a query on.
         */
        public MySQLGlobalExpressionToExpressionTOResultSet(BgeePreparedStatement statement) {
            super(statement);
        }

        @Override
        public GlobalExpressionToExpressionTO getTO() throws DAOException {
            log.entry();
            String globalExpressionId = null, expressionId = null;

            ResultSet currentResultSet = this.getCurrentResultSet();
            for (Entry<Integer, String> column: this.getColumnLabels().entrySet()) {
                try {
                    if (column.getValue().equals("globalExpressionId")) {
                        globalExpressionId = currentResultSet.getString(column.getKey());

                    } else if (column.getValue().equals("expressionId")) {
                        expressionId = currentResultSet.getString(column.getKey());

                    } 
                } catch (SQLException e) {
                    throw log.throwing(new DAOException(e));
                }
            }
            
            return log.exit(new GlobalExpressionToExpressionTO(globalExpressionId, expressionId));
        }
    }
}
