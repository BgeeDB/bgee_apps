package org.bgee.model.dao.mysql.anatdev;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.anatdev.StageDAO;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.mysql.MySQLDAO;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.model.dao.mysql.connector.MySQLDAOResultSet;

public class MySQLStageDAO extends MySQLDAO<StageDAO.Attribute> implements StageDAO {
    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = 
            LogManager.getLogger(MySQLStageDAO.class.getName());

    /**
     * Constructor providing the {@code MySQLDAOManager} that this {@code MySQLDAO} 
     * will use to obtain {@code BgeeConnection}s.
     * @param manager   the {@code MySQLDAOManager} to use.
     * @throws IllegalArgumentException If {@code manager} is {@code null}.
     */
    public MySQLStageDAO(MySQLDAOManager manager) throws IllegalArgumentException {
        super(manager);
    }

    @Override
    public StageTOResultSet getStages(Set<String> speciesIds) throws DAOException {
        log.entry(speciesIds);       
        
        String tableName = "stage";

        String sql = new String(); 
        Collection<StageDAO.Attribute> attributes = this.getAttributes();
        if (attributes == null || attributes.size() == 0) {
            sql += "SELECT DISTINCT " + tableName + ".*";
        } else {
            for (StageDAO.Attribute attribute: attributes) {
                if (sql.length() == 0) {
                    sql += "SELECT DISTINCT ";
                } else {
                    sql += ", ";
                }
                sql += tableName + "." + this.attributeToString(attribute);
            }
        }
        sql += " FROM " + tableName;
        String stageTaxConstTabName = "stageTaxonConstraint";
        if (speciesIds != null && speciesIds.size() > 0) {
             sql += " INNER JOIN " + stageTaxConstTabName + " ON (" +
                          stageTaxConstTabName + ".stageId = " + 
                          tableName + "." + this.attributeToString(StageDAO.Attribute.ID)+")" +
                    " WHERE " + stageTaxConstTabName + ".speciesId IS NULL" +
                    " OR " + stageTaxConstTabName + ".speciesId IN (" + 
                    BgeePreparedStatement.generateParameterizedQueryString(speciesIds.size()) + 
                    ")";
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
             return log.exit(new MySQLStageTOResultSet(stmt));
         } catch (SQLException e) {
             throw log.throwing(new DAOException(e));
         }
    }

    /** 
     * Returns a {@code String} that correspond to the given {@code StageDAO.Attribute}.
     * 
     * @param attribute   An {code AnatEntityDAO.Attribute} that is the attribute to
     *                    convert into a {@code String}.
     * @return            A {@code String} that corresponds to the given 
     *                    {@code AnatEntityDAO.Attribute}
     */
    private String attributeToString(StageDAO.Attribute attribute) {
        log.entry(attribute);
        
        switch (attribute) {
            case ID: 
                return log.exit("stageId");
            case NAME: 
                return log.exit("stageName");
            case DESCRIPTION: 
                return log.exit("stageDescription");
            case LEFTBOUND: 
                return log.exit("stageLeftBound");
            case RIGHTBOUND: 
                return log.exit("stageRightBound");
            case LEVEL: 
                return log.exit("stageLevel");
            case GRANULAR: 
                return log.exit("tooGranular");
            case GROUPING: 
                return log.exit("groupingStage");
            default: 
                throw log.throwing(new AssertionError("The attribute provided (" + 
                       attribute.toString() + ") is unknown for " + StageDAO.class.getName()));
        }
    }

    @Override
    public int insertStages(Collection<StageTO> stageTOs) throws DAOException {
        log.entry(stageTOs);
        
        StringBuilder sql = new StringBuilder(); 
        sql.append("INSERT INTO stage" +  
                   "(stageId, stageName, stageDescription, stageLeftBound, stageRightBound, " + 
                   "stageLevel, tooGranular, groupingStage) values ");
        for (int i = 0; i < stageTOs.size(); i++) {
            if (i > 0) {
                sql.append(", ");
            }
            sql.append("(?, ?, ?, ?, ?, ?, ?, ?) ");
        }
        try (BgeePreparedStatement stmt = 
                this.getManager().getConnection().prepareStatement(sql.toString())) {
            int paramIndex = 1;
            for (StageTO stageTO: stageTOs) {
                stmt.setString(paramIndex, stageTO.getId());
                paramIndex++;
                stmt.setString(paramIndex, stageTO.getName());
                paramIndex++;
                stmt.setString(paramIndex, stageTO.getDescription());
                paramIndex++;
                stmt.setInt(paramIndex, stageTO.getLeftBound());
                paramIndex++;
                stmt.setInt(paramIndex, stageTO.getRightBound());
                paramIndex++;
                stmt.setInt(paramIndex, stageTO.getLevel());
                paramIndex++;
                stmt.setBoolean(paramIndex, stageTO.isTooGranular());
                paramIndex++;
                stmt.setBoolean(paramIndex, stageTO.isGroupingStage());
                paramIndex++;
            }
            
            return log.exit(stmt.executeUpdate());
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
    }

    /**
     * A {@code MySQLDAOResultSet} specific to {@code StageTO}.
     * 
     * @author Valentine Rech de Laval
     * @version Bgee 13
     * @since Bgee 13
     */
    public class MySQLStageTOResultSet extends MySQLDAOResultSet<StageTO> 
                                            implements StageTOResultSet {
        /**
         * Delegates to {@link MySQLDAOResultSet#MySQLDAOResultSet(BgeePreparedStatement)}
         * super constructor.
         * 
         * @param statement The first {@code BgeePreparedStatement} to execute a query on.
         */
        public MySQLStageTOResultSet(BgeePreparedStatement statement) {
            super(statement);
        }

        @Override
        public StageTO getTO() throws DAOException {
            log.entry();
            
            String stageId = null, stageName = null, stageDescription = null;
            int leftBound = 0, rightBound = 0, level = 0;
            boolean tooGranular = false, groupingStage = false;
            
            ResultSet currentResultSet = this.getCurrentResultSet();
            for (Entry<Integer, String> column: this.getColumnLabels().entrySet()) {
                try {
                    if (column.getValue().equals("stageId")) {
                        stageId = currentResultSet.getString(column.getKey());
                    } else if (column.getValue().equals("stageName")) {
                        stageName = currentResultSet.getString(column.getKey());
                    } else if (column.getValue().equals("stageDescription")) {
                        stageDescription = currentResultSet.getString(column.getKey());
                    } else if (column.getValue().equals("stageLeftBound")) {
                        leftBound = currentResultSet.getInt(column.getKey());
                    } else if (column.getValue().equals("stageRightBound")) {
                        rightBound = currentResultSet.getInt(column.getKey());
                    } else if (column.getValue().equals("stageLevel")) {
                        level = currentResultSet.getInt(column.getKey());
                    } else if (column.getValue().equals("tooGranular")) {
                        tooGranular = currentResultSet.getBoolean(column.getKey());
                    } else if (column.getValue().equals("groupingStage")) {
                        groupingStage = currentResultSet.getBoolean(column.getKey());
                    }                
                } catch (SQLException e) {
                    throw log.throwing(new DAOException(e));
                }
            }
            return log.exit(new StageTO(stageId, stageName, stageDescription, 
                    leftBound, rightBound, level, tooGranular, groupingStage));
        }
    }

}
