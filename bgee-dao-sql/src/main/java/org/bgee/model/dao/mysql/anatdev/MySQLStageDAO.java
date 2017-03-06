package org.bgee.model.dao.mysql.anatdev;

import java.sql.SQLException;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.anatdev.StageDAO;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.mysql.MySQLDAO;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.model.dao.mysql.connector.MySQLDAOResultSet;
import org.bgee.model.dao.mysql.exception.UnrecognizedColumnException;

/**
 * A {@code StageDAO} for MySQL. 
 * 
 * @author Valentine Rech de Laval
 * @author Frederic Bastian
 * @version Bgee 13 Jan. 2016
 * @see org.bgee.model.dao.api.anatdev.StageDAO.StageTO
 * @since Bgee 13
 */
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
    public StageTOResultSet getStagesBySpeciesIds(Collection<Integer> speciesIds) throws DAOException {
        log.entry(speciesIds);       
        return log.exit(getStagesBySpeciesIds(speciesIds, null, null));
    }

    @Override
    public StageTOResultSet getStagesBySpeciesIds(Collection<Integer> speciesIds, Boolean isGroupingStage,
            Integer level) throws DAOException {
        log.entry(speciesIds, isGroupingStage, level);
        return log.exit(getStages(speciesIds, null, isGroupingStage, level));
    }

    @Override
    public StageTOResultSet getStagesByIds(Collection<String> stagesIds) throws DAOException {
        log.entry(stagesIds);
        return log.exit(getStages(null, stagesIds, null, null));
    }

    @Override
    public StageTOResultSet getStages(Collection<Integer> speciesIds, Collection<String> stageIds, 
            Boolean isGroupingStage, Integer level) throws DAOException {
        log.entry(speciesIds, stageIds, isGroupingStage, level);
        return log.exit(this.getStages(speciesIds, true, stageIds, isGroupingStage, level, 
                this.getAttributes()));
    }

    @Override
    public StageTOResultSet getStages(Collection<Integer> speciesIds, Boolean anySpecies, 
            Collection<String> stageIds, Boolean isGroupingStage, Integer level, 
            Collection<StageDAO.Attribute> attributes) throws DAOException {
        log.entry(speciesIds, stageIds, isGroupingStage, level);
        
        String tableName = "stage";
        
        //*******************************
        // FILTER ARGUMENTS
        //*******************************
        //Species
        Set<Integer> clonedSpeIds = Optional.ofNullable(speciesIds)
                .map(c -> new HashSet<>(c)).orElse(null);
        boolean isSpeciesFilter = clonedSpeIds != null && !clonedSpeIds.isEmpty();
        boolean realAnySpecies = isSpeciesFilter && 
                (Boolean.TRUE.equals(anySpecies) || clonedSpeIds.size() == 1);
        //stage IDs
        Set<String> clonedEntityIds = Optional.ofNullable(stageIds)
                .map(c -> new HashSet<String>(c)).orElse(null);
        boolean isEntityFilter = clonedEntityIds != null && !clonedEntityIds.isEmpty();
        
        boolean isGroupingStageFilter = isGroupingStage != null;
        boolean isLevelFilter = level != null;

        //*******************************
        // SELECT CLAUSE
        //*******************************
        String sql = "";
        EnumSet<StageDAO.Attribute> clonedAttrs = Optional.ofNullable(attributes)
                .map(e -> e.isEmpty()? null: EnumSet.copyOf(e)).orElse(null);
        if (clonedAttrs == null || clonedAttrs.isEmpty()) {
            sql += "SELECT DISTINCT " + tableName + ".*";
        } else {
            for (StageDAO.Attribute attribute: clonedAttrs) {
                if (sql.length() == 0) {
                    sql += "SELECT DISTINCT ";
                } else {
                    sql += ", ";
                }
                sql += tableName + "." + this.attributeToString(attribute);
            }
        }
        
        //*******************************
        // FROM CLAUSE
        //*******************************
        sql += " FROM " + tableName;
        
        String stageTaxConstTabName = "stageTaxonConstraint";
        if (realAnySpecies) {
             sql += " INNER JOIN " + stageTaxConstTabName + " ON (" +
                          stageTaxConstTabName + ".stageId = " + tableName + ".stageId)";
        }

        //*******************************
        // WHERE CLAUSE
        //*******************************
        if (isSpeciesFilter || isEntityFilter || isLevelFilter || isGroupingStageFilter) {
            sql += " WHERE ";
        }
        if (isSpeciesFilter) {
            if  (realAnySpecies) {
                sql += "(" + stageTaxConstTabName + ".speciesId IS NULL" +
                        " OR " + stageTaxConstTabName + ".speciesId IN (" + 
                        BgeePreparedStatement.generateParameterizedQueryString(clonedSpeIds.size()) + 
                        "))";
            } else {
                String existsPart = "SELECT 1 FROM " + stageTaxConstTabName + " AS tc WHERE "
                        + "tc.stageId = " + tableName + ".stageId AND tc.speciesId ";
                sql += getAllSpeciesExistsClause(existsPart, clonedSpeIds.size());
            }
        }
        if (isEntityFilter) {
            if (isSpeciesFilter) {
                sql += " AND ";
            }
            sql += "(" + tableName + ".stageId IN (" + 
                    BgeePreparedStatement.generateParameterizedQueryString(clonedEntityIds.size()) + "))";
        }

        if (isGroupingStageFilter) {
            if (isSpeciesFilter || isEntityFilter) {
                sql += " AND ";
            }
            sql += tableName + ".groupingStage= ? ";
        }
        if (isLevelFilter) {
            if (isSpeciesFilter || isEntityFilter || isGroupingStageFilter) {
                sql += " AND ";
            }
            sql += tableName + ".stageLevel= ? ";
        }

        //*******************************
        // PREPARE STATEMENT
        //*******************************
        //we don't use a try-with-resource, because we return a pointer to the results, 
        //not the actual results, so we should not close this BgeePreparedStatement.
        try {
            BgeePreparedStatement stmt = this.getManager().getConnection().prepareStatement(sql);
            if (isSpeciesFilter) {
                stmt.setIntegers(1, clonedSpeIds, true);
            }

            int offsetParamIndex = (isSpeciesFilter? clonedSpeIds.size() + 1: 1);
            if (isEntityFilter) {
                stmt.setStrings(offsetParamIndex, clonedEntityIds, true);
                offsetParamIndex += clonedEntityIds.size();
            }

            if (isGroupingStageFilter) {
                stmt.setBoolean(offsetParamIndex, isGroupingStage);
                offsetParamIndex ++;
            }

            if (isLevelFilter) {
                stmt.setInt(offsetParamIndex, level);
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
     * @throws IllegalArgumentException If the {@code attribute} is unknown.
     */
    private String attributeToString(StageDAO.Attribute attribute) throws IllegalArgumentException {
        log.entry(attribute);
        
        switch (attribute) {
            case ID: 
                return log.exit("stageId");
            case NAME: 
                return log.exit("stageName");
            case DESCRIPTION: 
                return log.exit("stageDescription");
            case LEFT_BOUND: 
                return log.exit("stageLeftBound");
            case RIGHT_BOUND: 
                return log.exit("stageRightBound");
            case LEVEL: 
                return log.exit("stageLevel");
            case GRANULAR: 
                return log.exit("tooGranular");
            case GROUPING: 
                return log.exit("groupingStage");
            default: 
                throw log.throwing(new IllegalArgumentException("The attribute provided (" + 
                       attribute.toString() + ") is unknown for " + StageDAO.class.getName()));
        }
    }

    @Override
    public int insertStages(Collection<StageTO> stageTOs) 
            throws DAOException, IllegalArgumentException {
        log.entry(stageTOs);
        
        if (stageTOs == null || stageTOs.isEmpty()) {
            throw log.throwing(new IllegalArgumentException(
                    "No stage is given, then no stage is inserted"));
        }
        
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
        private MySQLStageTOResultSet(BgeePreparedStatement statement) {
            super(statement);
        }

        @Override
        protected StageTO getNewTO() throws DAOException {
            log.entry();
            
            String stageId = null, stageName = null, stageDescription = null;
            Integer leftBound = null, rightBound = null, level = null;
            Boolean tooGranular = null, groupingStage = null;
            
            for (Entry<Integer, String> column: this.getColumnLabels().entrySet()) {
                try {
                    if (column.getValue().equals("stageId")) {
                        stageId = this.getCurrentResultSet().getString(column.getKey());
                    } else if (column.getValue().equals("stageName")) {
                        stageName = this.getCurrentResultSet().getString(column.getKey());
                    } else if (column.getValue().equals("stageDescription")) {
                        stageDescription = this.getCurrentResultSet().getString(column.getKey());
                    } else if (column.getValue().equals("stageLeftBound")) {
                        leftBound = this.getCurrentResultSet().getInt(column.getKey());
                    } else if (column.getValue().equals("stageRightBound")) {
                        rightBound = this.getCurrentResultSet().getInt(column.getKey());
                    } else if (column.getValue().equals("stageLevel")) {
                        level = this.getCurrentResultSet().getInt(column.getKey());
                    } else if (column.getValue().equals("tooGranular")) {
                        tooGranular = this.getCurrentResultSet().getBoolean(column.getKey());
                    } else if (column.getValue().equals("groupingStage")) {
                        groupingStage = this.getCurrentResultSet().getBoolean(column.getKey());
                    } else {
                        throw log.throwing(new UnrecognizedColumnException(column.getValue()));
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
