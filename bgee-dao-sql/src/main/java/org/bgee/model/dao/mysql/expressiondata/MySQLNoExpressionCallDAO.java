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
    private NoExpressionCallTOResultSet getNoExpressionCalls(Set<String> speciesIds,
            boolean isIncludeParentStructures) throws DAOException {
        log.entry(speciesIds, isIncludeParentStructures);        

        Collection<NoExpressionCallDAO.Attribute> attributes = this.getAttributes();        
        String tableName = "noExpression";
        if (isIncludeParentStructures) {
            tableName = "globalNoExpression";
        }
        //Construct sql query
        String sql = new String(); 
        //the Attribute INCLUDEPARENTSTRUCTURES does not correspond to any columns in a table, 
        //but it allows to determine how the TOs returned were generated. 
        //The TOs returned by the ResultSet will have this value set to null by default. 
        //So, we add a fake column to the query to provide the information to the ResultSet. 
        String sqlIncludeParentStructures = " 0";
        if (isIncludeParentStructures) {
            sqlIncludeParentStructures = " 1";
        }
        sqlIncludeParentStructures +=  " AS " + this.attributeToString(
                NoExpressionCallDAO.Attribute.INCLUDE_PARENT_STRUCTURES, isIncludeParentStructures);
        
        //the attribute ORIGINOFLINE does not correspond to any columns in basic no-expression call 
        //table.  
        //The TOs returned by the ResultSet will have these values set to null by default.
        //So, we add a fake column to the query to provide the information to the 
        //ResultSet, otherwise it is not needed. 
        String sqlOriginOfLine = "'" + OriginOfLine.SELF.getStringRepresentation() + "' AS " + 
                this.attributeToString(NoExpressionCallDAO.Attribute.ORIGIN_OF_LINE, 
                        isIncludeParentStructures);

        if (attributes != null) {
            for (NoExpressionCallDAO.Attribute attribute: attributes) {
                //ORIGINOFLINE corresponds to a column only in the globalNoExpression table, 
                //but we can still provide the information SELF for basic calls. As it is 
                //the default value in the TOs returned, we just need to skip this attribute 
                //if basic calls were requested. 
                if (attribute.equals(NoExpressionCallDAO.Attribute.ORIGIN_OF_LINE) && 
                        !isIncludeParentStructures) {
                    continue;
                }
                if (sql.isEmpty()) {
                    sql += "SELECT ";
                    //does the attributes requested ensure that there will be 
                    //no duplicated results?
                    if (!attributes.contains(NoExpressionCallDAO.Attribute.ID) &&  
                            (!attributes.contains(NoExpressionCallDAO.Attribute.GENE_ID) || 
                                !attributes.contains(NoExpressionCallDAO.Attribute.ANAT_ENTITY_ID) || 
                                !attributes.contains(NoExpressionCallDAO.Attribute.STAGE_ID))) {
                        sql += "DISTINCT ";
                    }
                } else {
                    sql += ", ";
                }
                if (attribute.equals(NoExpressionCallDAO.Attribute.INCLUDE_PARENT_STRUCTURES)) {
                    //add fake column
                    sql += sqlIncludeParentStructures;
                } else if (attribute.equals(NoExpressionCallDAO.Attribute.ORIGIN_OF_LINE) 
                        && !isIncludeParentStructures) {
                    //add fake column
                    sql += sqlOriginOfLine;
                } else {
                    //otherwise, real column requested
                    sql +=  tableName + "." + 
                            this.attributeToString(attribute, isIncludeParentStructures);
                }
            }
        }
        if (sql.isEmpty()) {
            //at this point, either there was no attribute requested, or only unnecessary 
            //fake columns were requested. As the latter case is really a weird use case, 
            //we don't bother and retrieve all columns anyway.
            sql += "SELECT " + tableName + ".*, " + sqlIncludeParentStructures; 
            if (!isIncludeParentStructures) {
                sql += ", " + sqlOriginOfLine;
            }
        }
        if (speciesIds != null && speciesIds.size() > 0) {
            //the MySQL optimizer sucks and do the join in the wrong order, 
            //when species are requested. So we use the STRAIGHT_JOIN clause, and order 
            //the tables appropriately (gene table first).
            //TODO: this order might not be optimal if other filtering options are added 
            //in the future (not based only on speciesIds)
            sql += " FROM gene STRAIGHT_JOIN " + tableName + 
                    " ON (gene.geneId = " + tableName + ".geneId) " +
                    
                    " WHERE gene.speciesId IN (" +
                    BgeePreparedStatement.generateParameterizedQueryString(
                            speciesIds.size()) + ")";
        } else {
            sql += " FROM " + tableName;
        }

        //we don't use a try-with-resource, because we return a pointer to the results, 
        //not the actual results, so we should not close this BgeePreparedStatement.
        BgeePreparedStatement stmt = null;
        try {
            stmt = this.getManager().getConnection().prepareStatement(sql.toString());
            if (speciesIds != null && speciesIds.size() > 0) {
                List<Integer> orderedSpeciesIds = MySQLDAO.convertToIntList(speciesIds);
                Collections.sort(orderedSpeciesIds);
                stmt.setIntegers(1, orderedSpeciesIds);
            }             
            return log.exit(new MySQLNoExpressionCallTOResultSet(stmt));
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
    }

    @Override
    public int getMaxNoExpressionCallId(Boolean isIncludeParentStructures)
            throws DAOException {
        log.entry(isIncludeParentStructures);
        
        String tableName = "noExpression";
        if (isIncludeParentStructures) {
            tableName = "globalNoExpression";
        }
        String id = "noExpressionId";
        if (isIncludeParentStructures) {
            id = "globalNoExpressionId";
        }
        
        String sql = "SELECT MAX(" + id + ") AS " + id + " FROM " + tableName;
        
        try (BgeePreparedStatement stmt = this.getManager().getConnection().prepareStatement(sql)) {
            MySQLNoExpressionCallTOResultSet resultSet = new MySQLNoExpressionCallTOResultSet(stmt);
            
            if (resultSet.next() && StringUtils.isNotBlank(resultSet.getTO().getId())) {
                return log.exit(Integer.valueOf(resultSet.getTO().getId()));
            }
            // There is no call in the table 
            return log.exit(0); 
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
     * @throws IllegalArgumentException if the {@code attribute} is unknown.
     */
    //TODO: see note about MySQLExpressionCallDAO#attributeToString(Attribute, boolean)
    private String attributeToString(NoExpressionCallDAO.Attribute attribute,
            boolean isIncludeParentStructures) throws IllegalArgumentException {
        log.entry(attribute, isIncludeParentStructures);

        String label = null;
        if (attribute.equals(NoExpressionCallDAO.Attribute.ID)) {
            if (isIncludeParentStructures) {
                label = "globalNoExpressionId";
            } else {
                label = "noExpressionId";
            }
        } else if (attribute.equals(NoExpressionCallDAO.Attribute.GENE_ID)) {
            label = "geneId";
        } else if (attribute.equals(NoExpressionCallDAO.Attribute.STAGE_ID)) {
            label = "stageId";
        } else if (attribute.equals(NoExpressionCallDAO.Attribute.ANAT_ENTITY_ID)) {
            label = "anatEntityId";
        } else if (attribute.equals(NoExpressionCallDAO.Attribute.AFFYMETRIX_DATA)) {
            label = "noExpressionAffymetrixData";
        } else if (attribute.equals(NoExpressionCallDAO.Attribute.IN_SITU_DATA)) {
            label = "noExpressionInSituData";
        } else if (attribute.equals(NoExpressionCallDAO.Attribute.RELAXED_IN_SITU_DATA)) {
            label = "noExpressionRelaxedInSituData";
        } else if (attribute.equals(NoExpressionCallDAO.Attribute.RNA_SEQ_DATA)) {
            label = "noExpressionRnaSeqData";
        } else if (attribute.equals(NoExpressionCallDAO.Attribute.ORIGIN_OF_LINE)) {
            label = "noExpressionOriginOfLine";
        } else if (attribute.equals(NoExpressionCallDAO.Attribute.INCLUDE_PARENT_STRUCTURES)) {
            label = "includeParentStructures";
        } else {
            throw log.throwing(new IllegalStateException("The attribute provided (" +
                    attribute.toString() + ") is unknown for " + NoExpressionCallDAO.class.getName()));
        }
        
        return log.exit(label);
    }

    @Override
    public int insertNoExpressionCalls(Collection<NoExpressionCallTO> noExpressionCalls) 
            throws DAOException, IllegalArgumentException {
        log.entry(noExpressionCalls);
        
        if (noExpressionCalls == null || noExpressionCalls.isEmpty()) {
            throw log.throwing(new IllegalArgumentException(
                    "No no-expression call given, so no call inserted"));
        }

        int callInsertedCount = 0;
        int totalCallNumber = noExpressionCalls.size();

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
                if (log.isDebugEnabled() && callInsertedCount % 100000 == 0) {
                    log.debug("{}/{} no-expression calls inserted", callInsertedCount, 
                            totalCallNumber);
                }
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
                if (log.isDebugEnabled() && callInsertedCount % 100000 == 0) {
                    log.debug("{}/{} global no-expression calls inserted", callInsertedCount, 
                            totalCallNumber);
                }
            }
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }

        return log.exit(callInsertedCount);
    }
    
    @Override
    public int insertGlobalNoExprToNoExpr(
            Collection<GlobalNoExpressionToNoExpressionTO> globalNoExpressionToNoExpression) 
                    throws DAOException, IllegalArgumentException {
        log.entry(globalNoExpressionToNoExpression);
        
        if (globalNoExpressionToNoExpression == null || globalNoExpressionToNoExpression.isEmpty()) {
            throw log.throwing(new IllegalArgumentException(
                    "No global no-expression to no-expression given, so no row deleted"));
        }

        int rowInsertedCount = 0;
        int totalTONumber = globalNoExpressionToNoExpression.size();

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
                if (log.isDebugEnabled() && rowInsertedCount % 100000 == 0) {
                    log.debug("{}/{} global no-expression to no-expression inserted", 
                            rowInsertedCount, totalTONumber);
                }
            }
            return log.exit(rowInsertedCount);
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
    }
    
    @Override
    public int deleteNoExprCalls(Set<String> noExprIds, boolean globalCalls) 
            throws DAOException, IllegalArgumentException {
        log.entry(noExprIds, globalCalls);

        if (noExprIds == null || noExprIds.isEmpty()) {
            throw log.throwing(new IllegalArgumentException(
                    "No no-expression IDs given, so no no-expression call deleted"));
        }

        String parameterizedQuery = 
                BgeePreparedStatement.generateParameterizedQueryString(noExprIds.size());
        
        // First, we delete rows in globalNoExpressionToNoExpression
        String sqlRelation = "DELETE FROM globalNoExpressionToNoExpression WHERE  ";
        if (globalCalls) {
            sqlRelation += "globalNoExpressionId IN (" + parameterizedQuery + ")";
        } else {
            sqlRelation += "noExpressionId IN (" + parameterizedQuery + ")";
        }
        boolean removedFromLinkTable = false;
        try (BgeePreparedStatement stmt = this.getManager().getConnection().prepareStatement(sqlRelation)) {
            List<Integer> orderedNoExprIds = MySQLDAO.convertToIntList(noExprIds);
            Collections.sort(orderedNoExprIds);
            stmt.setIntegers(1, orderedNoExprIds);
            removedFromLinkTable = (stmt.executeUpdate() > 0);
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
        // Then, if we are removing lines from the noExpression table, we remove propagated 
        // data in globalNoExpression with no more supporting basic calls.
        if (!globalCalls && removedFromLinkTable) {
            String sqlNoSupport = "DELETE t1 FROM globalNoExpression AS t1 " +
            		"LEFT OUTER JOIN globalNoExpressionToNoExpression AS t2 " +
            		    "ON t1.globalNoExpressionId = t2.globalNoExpressionId " +
            		"WHERE t2.globalNoExpressionId IS NULL";
            try (BgeePreparedStatement stmt = 
                    this.getManager().getConnection().prepareStatement(sqlNoSupport)) {
                stmt.executeUpdate();
            } catch (SQLException e) {
                throw log.throwing(new DAOException(e));
            }
        }

        // Then, we delete rows in noExpression or globalNoExpression
        String sql = "DELETE FROM noExpression WHERE noExpressionId IN (" + 
                    parameterizedQuery + ")";
        if (globalCalls) {
            sql = "DELETE FROM globalNoExpression WHERE globalNoExpressionId IN (" + 
                    parameterizedQuery + ")";

        }
        try (BgeePreparedStatement stmt = this.getManager().getConnection().prepareStatement(sql)) {
                List<Integer> orderedNoExprIds = MySQLDAO.convertToIntList(noExprIds);
                Collections.sort(orderedNoExprIds);
                stmt.setIntegers(1, orderedNoExprIds);
                return log.exit(stmt.executeUpdate());
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
    }

    @Override
    public int updateNoExprCalls(Collection<NoExpressionCallTO> noExprCallTOs)
            throws DAOException, IllegalArgumentException {
        log.entry(noExprCallTOs);
        
        if (noExprCallTOs == null || noExprCallTOs.isEmpty()) {
            throw log.throwing(new IllegalArgumentException(
                    "No no-expression call given, so no no-expression call updated"));
        }
        // According to isIncludeParentStructures(), the NoExpressionCallTO is updated in 
        // noExpression or globalNoExpression table. As prepared statement is for the 
        // column values not for table name, we need to separate NoExpressionCallTOs into
        // two separated collections. 
        Collection<NoExpressionCallTO> noExpressionToUpdate = new ArrayList<NoExpressionCallTO>();
        Collection<NoExpressionCallTO> globalnoExpressionToUpdate = 
                new ArrayList<NoExpressionCallTO>();
        for (NoExpressionCallTO call: noExprCallTOs) {
            if (call.isIncludeParentStructures()) {
                globalnoExpressionToUpdate.add(call);
            } else {
                noExpressionToUpdate.add(call);
            }
        }

        log.trace("{} basic no-expression calls and {} global no-expression calls will be updated", 
                noExpressionToUpdate.size(), globalnoExpressionToUpdate.size());

        // Construct sql query for basic no-expression calls
        String sqlNoExpr = "UPDATE noExpression SET geneId = ?, anatEntityId = ?, stageId = ?, " +
                "noExpressionAffymetrixData = ?, noExpressionInSituData = ?, " +
                "noExpressionRelaxedInSituData = ?, noExpressionRnaSeqData = ? " +
                "WHERE noExpressionId = ?";

        // Update basic no-expression calls
        int noExprUpdatedCount = 0;
        try (BgeePreparedStatement stmt = 
                this.getManager().getConnection().prepareStatement(sqlNoExpr)) {
            for (NoExpressionCallTO noExpr: noExpressionToUpdate) {
                stmt.setString(1, noExpr.getGeneId());
                stmt.setString(2, noExpr.getAnatEntityId());
                stmt.setString(3, noExpr.getStageId());
                stmt.setString(4, noExpr.getAffymetrixData().getStringRepresentation());
                stmt.setString(5, noExpr.getInSituData().getStringRepresentation());
                stmt.setString(6, noExpr.getRelaxedInSituData().getStringRepresentation());
                stmt.setString(7, noExpr.getRNASeqData().getStringRepresentation());
                stmt.setInt(8, Integer.parseInt(noExpr.getId()));
                noExprUpdatedCount += stmt.executeUpdate();
                stmt.clearParameters();
            }
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }

        // Construct sql query for global no-expression calls
        String sqlGlobalNoExpr = "UPDATE globalNoExpression SET geneId = ?, anatEntityId = ?, " +
                "stageId = ?, noExpressionAffymetrixData = ?, noExpressionInSituData = ?, " +
                "noExpressionRelaxedInSituData = ?, noExpressionRnaSeqData = ?, " +
                "noExpressionOriginOfLine = ? WHERE globalNoExpressionId = ?";
        
        // Update global no-expression calls
        int globalNoExprUpdatedCount = 0;
        try (BgeePreparedStatement stmt = 
                this.getManager().getConnection().prepareStatement(sqlGlobalNoExpr)) {
            for (NoExpressionCallTO globalNoExpr: globalnoExpressionToUpdate) {
                stmt.setString(1, globalNoExpr.getGeneId());
                stmt.setString(2, globalNoExpr.getAnatEntityId());
                stmt.setString(3, globalNoExpr.getStageId());
                stmt.setString(4, globalNoExpr.getAffymetrixData().getStringRepresentation());
                stmt.setString(5, globalNoExpr.getInSituData().getStringRepresentation());
                stmt.setString(6, globalNoExpr.getRelaxedInSituData().getStringRepresentation());
                stmt.setString(7, globalNoExpr.getRNASeqData().getStringRepresentation());
                stmt.setString(8, globalNoExpr.getOriginOfLine().getStringRepresentation());
                stmt.setString(9, globalNoExpr.getId());
                globalNoExprUpdatedCount += stmt.executeUpdate();
                stmt.clearParameters();
            }
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }

        log.trace("{} basic no-expression calls and {} global no-expression calls updated", 
                    noExprUpdatedCount, globalNoExprUpdatedCount);

        return log.exit(noExprUpdatedCount + globalNoExprUpdatedCount);
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
        private MySQLNoExpressionCallTOResultSet(BgeePreparedStatement statement) {
            super(statement);
        }

        @Override
        protected NoExpressionCallTO getNewTO() throws DAOException {
            log.entry();

            String id = null, geneId = null, anatEntityId = null, stageId = null;
            DataState noExprAffymetrixData = null, noExprInSituData = null, 
                    noExprRelaxedInSituData = null, noExprRnaSeqData = null;
            Boolean includeParentStructures = null;
            OriginOfLine noExpressionOriginOfLine = null;

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
                        stageId = currentResultSet.getString(column.getKey());

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
                        //NOTE: and what if originOfLine was not requested? we will not see 
                        //that it is a global call...
                        //isGlobalExpression = true;
                    } else if (column.getValue().equals("includeParentStructures")) {
                        includeParentStructures = currentResultSet.getBoolean(column.getKey());
                    } 

                } catch (SQLException e) {
                    throw log.throwing(new DAOException(e));
                }
            }

            return log.exit(new NoExpressionCallTO(id, geneId, anatEntityId,
                    stageId, noExprAffymetrixData, noExprInSituData, noExprRelaxedInSituData,
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
        private MySQLGlobalNoExpressionToNoExpressionTOResultSet(BgeePreparedStatement statement) {
            super(statement);
        }

        @Override
        protected GlobalNoExpressionToNoExpressionTO getNewTO() throws DAOException {
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
