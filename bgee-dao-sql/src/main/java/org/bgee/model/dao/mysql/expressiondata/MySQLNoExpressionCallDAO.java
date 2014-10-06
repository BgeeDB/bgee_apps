package org.bgee.model.dao.mysql.expressiondata;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.expressiondata.CallDAO.CallTO.DataState;
import org.bgee.model.dao.api.expressiondata.NoExpressionCallDAO.NoExpressionCallTO.OriginOfLine;
import org.bgee.model.dao.api.expressiondata.NoExpressionCallDAO;
import org.bgee.model.dao.api.expressiondata.NoExpressionCallParams;
import org.bgee.model.dao.mysql.MySQLDAO;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.model.dao.mysql.connector.MySQLDAOResultSet;

/**
 * A {@code NoExpressionCallDAO} for MySQL. 
 * 
 * @author Valentine Rech de Laval
 * @version Bgee 13
 * @see org.bgee.model.dao.api.expressiondata.NoExpressionCallDAO
 * @since Bgee 13
 */
public class MySQLNoExpressionCallDAO extends MySQLDAO<NoExpressionCallDAO.Attribute> 
                                      implements NoExpressionCallDAO {

    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = 
            LogManager.getLogger(MySQLNoExpressionCallDAO.class.getName());

    /**
     * Constructor providing the {@code MySQLDAOManager} that this {@code MySQLDAO} 
     * will use to obtain {@code BgeeConnection}s.
     * 
     * @param manager                       The {@code MySQLDAOManager} to use.
     * @throws IllegalArgumentException     If {@code manager} is {@code null}.
     */
    public MySQLNoExpressionCallDAO(MySQLDAOManager manager) throws IllegalArgumentException {
        super(manager);
    }

    @Override
    public NoExpressionCallTOResultSet getNoExpressionCalls(NoExpressionCallParams params) {
        log.entry(params);
        return log.exit(
                getNoExpressionCalls(params.getSpeciesIds(), params.isIncludeParentStructures())); 
    }

    /**
     * Retrieve all no-expression calls from data source according to a {@code Set} of 
     * {@code String}s that are the IDs of species allowing to filter the calls to use, and a 
     * {@code boolean} defining whether this no-expression call was generated 
     * using the data from the anatomical entity with the ID {@link CallTO#getAnatEntityId()} 
     * alone, or by also considering all its parents by <em>is_a</em> or <em>part_of</em> 
     * relations, even indirect.
     * <p>
     * The no-expression calls are retrieved and returned as a {@code NoExpressionCallTOResultSet}. 
     * It is the responsibility of the caller to close this {@code DAOResultSet} once 
     * results are retrieved.
     * 
     * @param speciesIds                A {@code Set} of {@code String}s that are the IDs of species 
     *                                  allowing to filter the calls to use
     * @param isIncludeParentStructures A {@code boolean} defining whether parents of the 
     *                                  anatomical entity were considered.
     * @return                          A {@code NoExpressionCallTOResultSet} containing all 
     *                                  no-expression calls from data source.
     * @throws DAOException             If a {@code SQLException} occurred while trying to get 
     *                                  no-expression calls.   
     */
    public NoExpressionCallTOResultSet getNoExpressionCalls(Set<String> speciesIds,
            boolean isIncludeParentStructures) throws DAOException {
        log.entry(speciesIds, isIncludeParentStructures);

        Collection<NoExpressionCallDAO.Attribute> attributes = this.getAttributes();        
        String tableName = "noExpression";
        if (isIncludeParentStructures) {
            tableName = "globalNoExpression";
        }
        //Construct sql query
        String sql = new String(); 
        if (attributes == null || attributes.size() == 0) {
            sql += "SELECT " + tableName + ".*";
        } else {
            for (NoExpressionCallDAO.Attribute attribute: attributes) {
                if (sql.length() == 0) {
                    sql += "SELECT DISTINCT ";
                } else {
                    sql += ", ";
                }
                sql += tableName + "." + this.attributeToString(attribute, isIncludeParentStructures);
            }
        }
        sql += " FROM " + tableName;
        String geneTabName = "gene";
         if (speciesIds != null && speciesIds.size() > 0) {
             sql += " INNER JOIN " + geneTabName + " ON (gene.geneId = " + 
                             tableName + "." + this.attributeToString(
                                     NoExpressionCallDAO.Attribute.GENEID, isIncludeParentStructures) +")" +
                    " WHERE " + geneTabName + ".speciesId IN (" +
                             createStringFromSet(speciesIds, ',', false) + ")" +
                    " ORDER BY " + geneTabName + ".speciesId, " + 
                        tableName + "." + this.attributeToString(
                            NoExpressionCallDAO.Attribute.GENEID, isIncludeParentStructures) + ", " +
                        tableName + "." + this.attributeToString(
                            NoExpressionCallDAO.Attribute.ANATENTITYID, isIncludeParentStructures) + ", " +
                        tableName + "." + this.attributeToString(
                            NoExpressionCallDAO.Attribute.DEVSTAGEID, isIncludeParentStructures);
         } else {
             sql += " ORDER BY " + 
                         tableName + "." + this.attributeToString(
                             NoExpressionCallDAO.Attribute.GENEID, isIncludeParentStructures) + ", " +
                         tableName + "." + this.attributeToString(
                             NoExpressionCallDAO.Attribute.ANATENTITYID, isIncludeParentStructures) + ", " +
                         tableName + "." + this.attributeToString(
                             NoExpressionCallDAO.Attribute.DEVSTAGEID, isIncludeParentStructures);
         }

        //we don't use a try-with-resource, because we return a pointer to the results, 
        //not the actual results, so we should not close this BgeePreparedStatement.
        BgeePreparedStatement stmt = null;
        try {
            stmt = this.getManager().getConnection().prepareStatement(sql.toString());
            return log.exit(new MySQLNoExpressionCallTOResultSet(stmt));
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
    }

    /** 
     * Return a {@code String} that correspond to the given {@code NoExpressionCallDAO.Attribute}.
     * 
     * @param attribute                 A {code NoExpressionCallDAO.Attribute} that is the attribute
     *                                  to convert in a {@code String}.
     * @param isIncludeParentStructures A {@code boolean} defining whether parents of the  
     *                                  anatomical entity were considered.
     * @return                          A {@code String} that correspond to the given 
     *                                  {@code NoExpressionCallDAO.Attribute}
     */
    private String attributeToString(NoExpressionCallDAO.Attribute attribute,
            boolean isIncludeParentStructures) {
        log.entry(attribute, isIncludeParentStructures);

        String label = null;
        if (attribute.equals(NoExpressionCallDAO.Attribute.ID)) {
            if (isIncludeParentStructures) {
                label = "globalNoExpressionId";
            } else {
                label = "noExpressionId";
            }
        } else if (attribute.equals(NoExpressionCallDAO.Attribute.GENEID)) {
            label = "geneId";
        } else if (attribute.equals(NoExpressionCallDAO.Attribute.DEVSTAGEID)) {
            label = "stageId";
        } else if (attribute.equals(NoExpressionCallDAO.Attribute.ANATENTITYID)) {
            label = "anatEntityId";
        } else if (attribute.equals(NoExpressionCallDAO.Attribute.AFFYMETRIXDATA)) {
            label = "noExpressionAffymetrixData";
        } else if (attribute.equals(NoExpressionCallDAO.Attribute.INSITUDATA)) {
            label = "noExpressionInSituData";
        } else if (attribute.equals(NoExpressionCallDAO.Attribute.RNASEQDATA)) {
            label = "noExpressionRnaSeqData";
        } else if (attribute.equals(NoExpressionCallDAO.Attribute.ORIGINOFLINE)) {
            if (isIncludeParentStructures) {
                label = "noExpressionOriginOfLine";
            } else {
                throw log.throwing(new IllegalStateException("No noExpressionOriginOfLine in "+
                                                             "expression table"));
            }
        } else if (attribute.equals(NoExpressionCallDAO.Attribute.INCLUDEPARENTSTRUCTURES)) {
            throw log.throwing(new IllegalStateException(
                    attribute.toString() + "is not a column of the noExpression table"));
        } else {
            throw log.throwing(new IllegalStateException("The attribute provided (" +
                    attribute.toString() + ") is unknown for " + NoExpressionCallDAO.class.getName()));
        }
        
        return log.exit(label);
    }

    @Override
    public int insertNoExpressionCalls(Collection<NoExpressionCallTO> noExpressionCalls) {
        log.entry(noExpressionCalls);
        
        int callInsertedCount = 0;

        // According to isIncludeParentStructures(), the NoExpressionCallTO is inserted in 
        // noExpression or globalNoExpression table. As prepared statement is for the 
        // column values not for table name, we need to separate NoExpressionCallTOs into
        // two separated collections. 
        Collection<NoExpressionCallTO> toInsertInNoExpression = new ArrayList<NoExpressionCallTO>();
        Collection<NoExpressionCallTO> toInsertInGlobalNoExpression = 
                new ArrayList<NoExpressionCallTO>();
        for (NoExpressionCallTO call: noExpressionCalls) {
            if (call.isIncludeParentStructures()) {
                toInsertInGlobalNoExpression.add(call);
            } else {
                toInsertInNoExpression.add(call);
            }
        }

        // And we need to build two different queries. 
        String sqlNoExpression = "INSERT INTO noExpression " +
                "(noExpressionId, geneId, anatEntityId, stageId, "+
                "noExpressionAffymetrixData, noExpressionInSituData, noExpressionRnaSeqData) " +
                "values (?, ?, ?, ?, ?, ?, ?)";
        
        // To not overload MySQL with an error com.mysql.jdbc.PacketTooBigException, 
        // and because of laziness, we insert no-expression calls one at a time
        try (BgeePreparedStatement stmt = 
                this.getManager().getConnection().prepareStatement(sqlNoExpression)) {
            for (NoExpressionCallTO call: toInsertInNoExpression) {
                stmt.setInt(1, Integer.parseInt(call.getId()));
                stmt.setString(2, call.getGeneId());
                stmt.setString(3, call.getAnatEntityId());
                stmt.setString(4, call.getStageId());
                stmt.setString(5, call.getAffymetrixData().getStringRepresentation());
                stmt.setString(6, call.getInSituData().getStringRepresentation());
                stmt.setString(7, call.getRNASeqData().getStringRepresentation());
                callInsertedCount += stmt.executeUpdate();
                stmt.clearParameters();
            }
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }

        String sqlGlobalNoExpression = "INSERT INTO globalNoExpression " +
                "(globalNoExpressionId, geneId, anatEntityId, stageId, "+
                "noExpressionAffymetrixData, noExpressionInSituData, noExpressionRnaSeqData, "+
                "noExpressionOriginOfLine) values (?, ?, ?, ?, ?, ?, ?, ?)";
        try (BgeePreparedStatement stmt = 
                this.getManager().getConnection().prepareStatement(sqlGlobalNoExpression)) {
            for (NoExpressionCallTO call: toInsertInGlobalNoExpression) {
                stmt.setInt(1, Integer.parseInt(call.getId()));
                stmt.setString(2, call.getGeneId());
                stmt.setString(3, call.getAnatEntityId());
                stmt.setString(4, call.getStageId());
                stmt.setString(5, call.getAffymetrixData().getStringRepresentation());
                stmt.setString(6, call.getInSituData().getStringRepresentation());
                stmt.setString(7, call.getRNASeqData().getStringRepresentation());
                stmt.setString(8, call.getOriginOfLine().getStringRepresentation());
                callInsertedCount += stmt.executeUpdate();
                stmt.clearParameters();
            }
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }

        return log.exit(callInsertedCount);
    }
    
    @Override
    public int insertGlobalNoExprToNoExpr(
            Collection<GlobalNoExpressionToNoExpressionTO> globalNoExpressionToNoExpression) {
        log.entry(globalNoExpressionToNoExpression);
        
        int rowInsertedCount = 0;

        // And we need to build two different queries. 
        String sqlExpression = "INSERT INTO globalNoExpressionToNoExpression " +
                "(globalNoExpressionId, noExpressionId) values (?, ?)";
        
        // To not overload MySQL with an error com.mysql.jdbc.PacketTooBigException, 
        // and because of laziness, we insert rows one at a time
        try (BgeePreparedStatement stmt = 
                this.getManager().getConnection().prepareStatement(sqlExpression)) {
            for (GlobalNoExpressionToNoExpressionTO call: globalNoExpressionToNoExpression) {
                stmt.setString(1, call.getGlobalNoExpressionId());
                stmt.setString(2, call.getNoExpressionId());
                rowInsertedCount += stmt.executeUpdate();
                stmt.clearParameters();
            }
            return log.exit(rowInsertedCount);
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
    }
    
    /**
     * A {@code MySQLDAOResultSet} specific to {@code NoExpressionCallTO}.
     * 
     * @author Valentine Rech de Laval
     * @version Bgee 13
     * @since Bgee 13
     */
    public class MySQLNoExpressionCallTOResultSet extends MySQLDAOResultSet<NoExpressionCallTO> 
    implements NoExpressionCallTOResultSet {

        /**
         * Delegates to {@link MySQLDAOResultSet#MySQLDAOResultSet(BgeePreparedStatement)}
         * super constructor.
         * 
         * @param statement The first {@code BgeePreparedStatement} to execute a query on.
         */
        public MySQLNoExpressionCallTOResultSet(BgeePreparedStatement statement) {
            super(statement);
        }

        @Override
        public NoExpressionCallTO getTO() throws DAOException {
            log.entry();

            String id = null, geneId = null, anatEntityId = null, devStageId = null;
            DataState noExprAffymetrixData = DataState.NODATA, noExprInSituData = DataState.NODATA, 
                    noExprRelaxedInSituData = DataState.NODATA, noExprRnaSeqData = DataState.NODATA;
            boolean includeParentStructures = false;
            OriginOfLine noExpressionOriginOfLine = OriginOfLine.SELF;

            boolean isGlobalExpression = false;
            ResultSet currentResultSet = this.getCurrentResultSet();
            for (Entry<Integer, String> column: this.getColumnLabels().entrySet()) {
                try {
                    if (column.getValue().equals("noExpressionId")) {
                        id = currentResultSet.getString(column.getKey());
                        
                    } else if (column.getValue().equals("globalNoExpressionId")) {
                        id = currentResultSet.getString(column.getKey());

                    } else if (column.getValue().equals("geneId")) {
                        geneId = currentResultSet.getString(column.getKey());

                    } else if (column.getValue().equals("anatEntityId")) {
                        anatEntityId = currentResultSet.getString(column.getKey());

                    } else if (column.getValue().equals("stageId")) {
                        devStageId = currentResultSet.getString(column.getKey());

                    } else if (column.getValue().equals("noExpressionAffymetrixData")) {
                        noExprAffymetrixData = DataState.convertToDataState(
                                currentResultSet.getString(column.getKey()));

                    } else if (column.getValue().equals("noExpressionInSituData")) {
                        noExprInSituData = DataState.convertToDataState(
                                currentResultSet.getString(column.getKey()));

                    } else if (column.getValue().equals("noExpressionRelaxedInSituData")) {
                        noExprRelaxedInSituData = DataState.convertToDataState(
                                currentResultSet.getString(column.getKey()));

                    } else if (column.getValue().equals("noExpressionRnaSeqData")) {
                        noExprRnaSeqData = DataState.convertToDataState(
                                currentResultSet.getString(column.getKey()));

                    } else if (column.getValue().equals("noExpressionOriginOfLine")) {
                        noExpressionOriginOfLine = OriginOfLine.convertToOriginOfLine(
                                        currentResultSet.getString(column.getKey()));
                        isGlobalExpression = true;
                    }

                } catch (SQLException e) {
                    throw log.throwing(new DAOException(e));
                }
            }
            
            if (isGlobalExpression) {
                includeParentStructures = true;
            }

            return log.exit(new NoExpressionCallTO(id, geneId, anatEntityId,
                    devStageId, noExprAffymetrixData, noExprInSituData, noExprRelaxedInSituData,
                    noExprRnaSeqData, includeParentStructures, noExpressionOriginOfLine));
        }
    }

    /**
     * A {@code MySQLDAOResultSet} specific to {@code GlobalNoExpressionToNoExpressionTO}.
     * 
     * @author Valentine Rech de Laval
     * @version Bgee 13
     * @since Bgee 13
     */
    public class MySQLGlobalNoExpressionToNoExpressionTOResultSet 
                                       extends MySQLDAOResultSet<GlobalNoExpressionToNoExpressionTO> 
                                       implements GlobalNoExpressionToNoExpressionTOResultSet {
        /**
         * Delegates to {@link MySQLDAOResultSet#MySQLDAOResultSet(BgeePreparedStatement)}
         * super constructor.
         * 
         * @param statement The first {@code BgeePreparedStatement} to execute a query on.
         */
        public MySQLGlobalNoExpressionToNoExpressionTOResultSet(BgeePreparedStatement statement) {
            super(statement);
        }

        @Override
        public GlobalNoExpressionToNoExpressionTO getTO() throws DAOException {
            log.entry();
            String globalNoExpressionId = null, noExpressionId = null;

            ResultSet currentResultSet = this.getCurrentResultSet();
            for (Entry<Integer, String> column: this.getColumnLabels().entrySet()) {
                try {
                    if (column.getValue().equals("globalNoExpressionId")) {
                        globalNoExpressionId = currentResultSet.getString(column.getKey());

                    } else if (column.getValue().equals("noExpressionId")) {
                        noExpressionId = currentResultSet.getString(column.getKey());

                    } 
                } catch (SQLException e) {
                    throw log.throwing(new DAOException(e));
                }
            }
            
            return log.exit(new GlobalNoExpressionToNoExpressionTO(
                    globalNoExpressionId, noExpressionId));
        }
    }
}
