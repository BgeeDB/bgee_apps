package org.bgee.model.dao.mysql.expressiondata;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.mysql.MySQLDAO;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.model.dao.mysql.connector.MySQLDAOResultSet;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.expressiondata.ExpressionCallDAO;
import org.bgee.model.dao.api.expressiondata.ExpressionCallDAO.ExpressionCallTO.OriginOfLine;
import org.bgee.model.dao.api.expressiondata.ExpressionCallParams;
import org.bgee.model.dao.api.expressiondata.CallDAO.CallTO.DataState;

/**
 * A {@code ExpressionCallDAO} for MySQL. 
 * 
 * @author Valentine Rech de Laval
 * @version Bgee 13
 * @see org.bgee.model.dao.api.expressiondata.ExpressionCallDAO.ExpressionCallTO
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
    public ExpressionCallTOResultSet getExpressionCalls(ExpressionCallParams params) 
            throws DAOException {
        log.entry(params);
        return log.exit(getExpressionCalls(
                params.getSpeciesIds(), params.isIncludeSubstructures(), params.isIncludeSubStages())); 
        
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

    @Override
    public int getMaxExpressionCallId(boolean isIncludeSubstructures)
            throws DAOException {
        log.entry(isIncludeSubstructures);
        
        String tableName = "expression";
        if (isIncludeSubstructures) {
            tableName = "globalExpression";
        }        
        
        String id = "expressionId";
        if (isIncludeSubstructures) {
            id = "globalExpressionId";
        } 

        String sql = "SELECT MAX(" + id + ") AS " + id + " FROM " + tableName;
    
        try (BgeePreparedStatement stmt = this.getManager().getConnection().prepareStatement(sql)) {
            MySQLExpressionCallTOResultSet resultSet = new MySQLExpressionCallTOResultSet(stmt);
            
            if (resultSet.next() && StringUtils.isNotBlank(resultSet.getTO().getId())) {
                return log.exit(Integer.valueOf(resultSet.getTO().getId()));
            } 
            return log.exit(0);
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
    }

    /**
     * Retrieve expression calls from data source according to a {@code Set} of {@code String}s 
     * that are the IDs of species allowing to filter the calls to use, and a {@code boolean} 
     * defining whether this expression call was generated using data from the anatomical entity 
     * with the ID {@link CallTO#getAnatEntityId()} alone, or by also considering all its 
     * descendants by <em>is_a</em> or <em>part_of</em> relations, even indirect. 
     * <p>
     * The expression calls are retrieved and returned as a {@code ExpressionCallTOResultSet}. 
     * It is the responsibility of the caller to close this {@code DAOResultSet} once 
     * results are retrieved.
     * 
     * @param speciesIds             A {@code Set} of {@code String}s that are the IDs of species 
     *                               allowing to filter the calls to use
     * @param isIncludeSubstructures A {@code boolean} defining whether descendants 
     *                               of the anatomical entity were considered.
     * @return                       An {@code ExpressionCallTOResultSet} containing all expression 
     *                               calls from data source.
     * @throws DAOException          If a {@code SQLException} occurred while trying to get 
     *                               expression calls.   
     */
    private ExpressionCallTOResultSet getExpressionCalls(Set<String> speciesIds, 
            boolean isIncludeSubstructures, boolean isIncludeSubStages) throws DAOException {
        log.entry(speciesIds, isIncludeSubstructures, isIncludeSubStages);

        Collection<ExpressionCallDAO.Attribute> attributes = this.getAttributes();
        
        String tableName = "expression";
        String geneTabName = "gene";

        if (isIncludeSubstructures) {
            tableName = "globalExpression";
        }
        // Construct sql query
        String sql = new String(); 
        //the Attributes INCLUDESUBSTAGES and INCLUDESUBSTRUCTURES does not correspond 
        //to any columns in a table, but they allow to determine how the TOs returned 
        //were generated. 
        //The TOs returned by the ResultSet will have these values set to false 
        //by default. So, only if isIncludeSubstructures or isIncludeSubStages 
        //are true, we add a fake column to the query to provide the information to the ResultSet, 
        //otherwise it is not needed. 
        String sqlIncludeSubstructures = " 0";
        if (isIncludeSubstructures) {
            sqlIncludeSubstructures = " 1";
        }
        sqlIncludeSubstructures += " AS " + this.attributeToString(
                ExpressionCallDAO.Attribute.INCLUDESUBSTRUCTURES, isIncludeSubstructures);
        
        //TODO: add the mechanism for includeSubStages when implemented.
        String sqlIncludeSubStages = " 0 ";
        if (isIncludeSubStages) {
            sqlIncludeSubStages = " 1";
        }
        sqlIncludeSubStages += " AS " + this.attributeToString(
                ExpressionCallDAO.Attribute.INCLUDESUBSTAGES, isIncludeSubstructures);
        
        if (attributes != null) {
            for (ExpressionCallDAO.Attribute attribute: attributes) {
                //TODO: add the mechanism for includeSubStages when implemented.
                //for now, we skip this attribute
                if (attribute.equals(ExpressionCallDAO.Attribute.INCLUDESUBSTAGES)) {
                    continue;
                }
                //ORIGINOFLINE corresponds to a column only in the globalExpression table, 
                //but we can still provide the information SELF for basic calls. As it is 
                //the default value in the TOs returned, we just need to skip this attribute 
                //if basic calls were requested. 
                if (attribute.equals(ExpressionCallDAO.Attribute.ORIGINOFLINE) && 
                        !isIncludeSubstructures) {
                    continue;
                }
                if (sql.isEmpty()) {
                    sql += "SELECT DISTINCT ";
                } else {
                    sql += ", ";
                }
                if (attribute.equals(ExpressionCallDAO.Attribute.INCLUDESUBSTRUCTURES)) {
                    //add fake column
                    sql += sqlIncludeSubstructures;
                } else {
                    //otherwise, real column requested
                    sql +=  tableName + "." + 
                            this.attributeToString(attribute, isIncludeSubstructures);
                }
            }
        }
        if (sql.isEmpty()) {
            //at this point, either there was no attribute requested, or only unnecessary 
            //fake columns were requested. As the latter case is really a weird use case, 
            //we don't bother and retrieve all columns anyway.
            sql += "SELECT " + tableName + ".*, " + sqlIncludeSubstructures;
        }
        sql += " FROM " + tableName;
        if (speciesIds != null && speciesIds.size() > 0) {
             sql += " INNER JOIN " + geneTabName + " ON (" + geneTabName + ".geneId = " + 
                         tableName + ".geneId)" +
                    " WHERE " + geneTabName + ".speciesId IN (" + 
                            BgeePreparedStatement.generateParameterizedQueryString(
                                    speciesIds.size()) + ")";
//                    " ORDER BY " + geneTabName + ".speciesId, " + tableName + ".geneId, " +  
//                            tableName + ".anatEntityId, " + tableName + ".stageId";
//         } else {
//             sql += " ORDER BY " + tableName + ".geneId, " + tableName + ".anatEntityId, " + 
//                             tableName + ".stageId";
         }

        //we don't use a try-with-resource, because we return a pointer to the results, 
        //not the actual results, so we should not close this BgeePreparedStatement.
        BgeePreparedStatement stmt = null;
        try {
            stmt = this.getManager().getConnection().prepareStatement(sql);
            if (speciesIds != null && speciesIds.size() > 0) {
                List<Integer> orderedSpeciesIds = MySQLDAO.convertToIntList(speciesIds);
                Collections.sort(orderedSpeciesIds);
                stmt.setIntegers(1, orderedSpeciesIds);
            }             
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
     * @throws IllegalArgumentException if the {@code attribute} is unknown.
     */
    //NOTE: this method is not responsible for checking for column validity, only to map 
    //an Attribute to a String, as its javadoc states... Especially considering that 
    //it is a private method, so WE are responsible for what we provide to it. 
    //The final IllegalArgumentException is here only to make sure this method will be updated 
    //if we add new Attributes. 
    //TODO: actually, we don't need a method converting Attributes to Strings, we just need 
    //a method generating all the SELECT clause. 
    private String attributeToString(ExpressionCallDAO.Attribute attribute, 
            boolean isIncludeSubstructures) throws IllegalArgumentException {
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
        } else if (attribute.equals(ExpressionCallDAO.Attribute.STAGEID)) {
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
            label = "originOfLine";
        } else if (attribute.equals(ExpressionCallDAO.Attribute.INCLUDESUBSTRUCTURES)) {
            label = "includeSubstructures";
        } else if (attribute.equals(ExpressionCallDAO.Attribute.INCLUDESUBSTAGES)) {
            label = "includeSubStages";
        } else {
            throw log.throwing(new IllegalArgumentException("The attribute provided (" +
                    attribute.toString() + ") is unknown for " + ExpressionCallDAO.class.getName()));
        }
        
        return log.exit(label);
    }

    @Override
    public int insertExpressionCalls(Collection<ExpressionCallTO> expressionCalls) 
            throws DAOException, IllegalArgumentException {
        log.entry(expressionCalls);
        
        if (expressionCalls == null || expressionCalls.isEmpty()) {
            throw log.throwing(new IllegalArgumentException(
                    "No expression call is given, then no expression call is updated"));
        }

        int callInsertedCount = 0;
        int totalCallNumber = expressionCalls.size();
        
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
                stmt.setString(4, call.getStageId());
                stmt.setString(5, call.getESTData().getStringRepresentation());
                stmt.setString(6, call.getAffymetrixData().getStringRepresentation());
                stmt.setString(7, call.getInSituData().getStringRepresentation());
                stmt.setString(8, call.getRNASeqData().getStringRepresentation());
                callInsertedCount += stmt.executeUpdate();
                stmt.clearParameters();
                if (log.isDebugEnabled() && callInsertedCount % 100000 == 0) {
                    log.debug("{}/{} expression calls inserted", callInsertedCount, 
                            totalCallNumber);
                }
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
                stmt.setString(4, call.getStageId());
                stmt.setString(5, call.getESTData().getStringRepresentation());
                stmt.setString(6, call.getAffymetrixData().getStringRepresentation());
                stmt.setString(7, call.getInSituData().getStringRepresentation());
                stmt.setString(8, call.getRNASeqData().getStringRepresentation());
                stmt.setString(9, call.getOriginOfLine().getStringRepresentation());
                callInsertedCount += stmt.executeUpdate();
                stmt.clearParameters();
                if (log.isDebugEnabled() && callInsertedCount % 100000 == 0) {
                    log.debug("{}/{} global expression calls inserted", callInsertedCount, 
                            totalCallNumber);
                }
            }
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
        return log.exit(callInsertedCount);
    }

    @Override
    public int insertGlobalExpressionToExpression(
            Collection<GlobalExpressionToExpressionTO> globalExpressionToExpression) 
                    throws DAOException, IllegalArgumentException {
        log.entry(globalExpressionToExpression);
        
        if (globalExpressionToExpression == null || globalExpressionToExpression.isEmpty()) {
            throw log.throwing(new IllegalArgumentException(
                    "No global expression to expression is given, then nothing is updated"));
        }

        int rowInsertedCount = 0;
        int totalTONumber = globalExpressionToExpression.size();

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
                if (log.isDebugEnabled() && rowInsertedCount % 100000 == 0) {
                    log.debug("{}/{} global expression to expression inserted", 
                            rowInsertedCount, totalTONumber);
                }
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
         * Delegates to {@link MySQLDAOResultSet#MySQLDAOResultSet(BgeePreparedStatement)}
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
            DataState affymetrixData = null, estData = null, inSituData = null, rnaSeqData = null;
            Boolean includeSubstructures = null, includeSubStages = null;
            OriginOfLine originOfLine = null;

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
                        affymetrixData = DataState.convertToDataState(
                                currentResultSet.getString(column.getKey()));

                    } else if (column.getValue().equals("estData")) {
                        estData = DataState.convertToDataState(
                                currentResultSet.getString(column.getKey()));
                        
                    } else if (column.getValue().equals("inSituData")) {
                        inSituData = DataState.convertToDataState(
                                currentResultSet.getString(column.getKey()));

                    } else if (column.getValue().equals("rnaSeqData")) {
                        rnaSeqData = DataState.convertToDataState(
                                currentResultSet.getString(column.getKey()));
                        
                    } else if (column.getValue().equals("originOfLine")) {
                        originOfLine = OriginOfLine.convertToOriginOfLine(
                                currentResultSet.getString(column.getKey()));
                        //NOTE: and what if originOfLine was not requested? we will not see 
                        //that it is a global call...
                        //isGlobalExpression = true;
                    } else if (column.getValue().equals("includeSubstructures")) {
                        includeSubstructures = currentResultSet.getBoolean(column.getKey());
                    } else if (column.getValue().equals("includeSubStages")) {
                        includeSubStages = currentResultSet.getBoolean(column.getKey());
                    } 

                } catch (SQLException e) {
                    throw log.throwing(new DAOException(e));
                }
            }
            
            //TODO manage includeSubStages when complete query will be write
            return log.exit(new ExpressionCallTO(id, geneId, anatEntityId, devStageId,
                    affymetrixData, estData, inSituData, rnaSeqData,
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
         * Delegates to {@link MySQLDAOResultSet#MySQLDAOResultSet(BgeePreparedStatement)}
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
