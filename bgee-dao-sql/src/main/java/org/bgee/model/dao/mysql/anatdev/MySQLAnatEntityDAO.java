package org.bgee.model.dao.mysql.anatdev;

import java.sql.SQLException;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.anatdev.AnatEntityDAO;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.mysql.MySQLDAO;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.model.dao.mysql.connector.MySQLDAOResultSet;
import org.bgee.model.dao.mysql.exception.UnrecognizedColumnException;


/**
 * An {@code AnatEntityDAO} for MySQL. 
 * 
 * @author Valentine Rech de Laval
 * @author Frederic Bastian
 * @version Bgee 13 Jan. 2016
 * @see org.bgee.model.dao.api.anatdev.AnatEntityDAO.AnatEntityTO
 * @since Bgee 13
 */
public class MySQLAnatEntityDAO extends MySQLDAO<AnatEntityDAO.Attribute> implements AnatEntityDAO {
    
    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = LogManager.getLogger(MySQLAnatEntityDAO.class.getName());

    /**
     * Constructor providing the {@code MySQLDAOManager} that this {@code MySQLDAO} 
     * will use to obtain {@code BgeeConnection}s.
     * 
     * @param manager                       The {@code MySQLDAOManager} to use.
     * @throws IllegalArgumentException     If {@code manager} is {@code null}.
     */
    public MySQLAnatEntityDAO(MySQLDAOManager manager) throws IllegalArgumentException {
        super(manager);
    }

    
    @Override
    public AnatEntityTOResultSet getAnatEntitiesByIds(Collection<String> anatEntitiesIds) {
        log.traceEntry("{}", anatEntitiesIds);
        return log.traceExit(this.getAnatEntities(null, anatEntitiesIds));
    }

    @Override
    public AnatEntityTOResultSet getAnatEntitiesBySpeciesIds(Collection<Integer> speciesIds) 
            throws DAOException {
        log.traceEntry("{}", speciesIds);
        return log.traceExit(this.getAnatEntities(speciesIds, null));
    }
    
    @Override
    public AnatEntityTOResultSet getAnatEntities(Collection<Integer> speciesIds, Collection<String> anatEntitiesIds)
            throws DAOException {
        log.traceEntry("{}, {}", speciesIds, anatEntitiesIds);
        return log.traceExit(this.getAnatEntities(speciesIds, true, anatEntitiesIds, this.getAttributes()));
    }
    
    @Override
    public AnatEntityTOResultSet getAnatEntities(Collection<Integer> speciesIds, Boolean anySpecies, 
            Collection<String> anatEntitiesIds, Collection<AnatEntityDAO.Attribute> attributes) 
                    throws DAOException {
        log.traceEntry("{}, {}, {}, {}", speciesIds, anySpecies, anatEntitiesIds, attributes);
        
        String tableName = "anatEntity";
        
        //*******************************
        // FILTER ARGUMENTS
        //*******************************
        //Species
        Set<Integer> clonedSpeIds = Optional.ofNullable(speciesIds)
                .map(c -> new HashSet<>(c)).orElse(null);
        boolean isSpeciesFilter = clonedSpeIds != null && !clonedSpeIds.isEmpty();
        boolean realAnySpecies = isSpeciesFilter && 
                (Boolean.TRUE.equals(anySpecies) || clonedSpeIds.size() == 1);
        //anat. entity IDs
        Set<String> clonedEntityIds = Optional.ofNullable(anatEntitiesIds)
                .map(c -> new HashSet<String>(c)).orElse(null);
        boolean isEntityFilter = clonedEntityIds != null && !clonedEntityIds.isEmpty();

        //*******************************
        // SELECT CLAUSE
        //*******************************
        String sql = "";
        EnumSet<AnatEntityDAO.Attribute> clonedAttrs = Optional.ofNullable(attributes)
                .map(e -> e.isEmpty()? null: EnumSet.copyOf(e)).orElse(null);
        if (clonedAttrs == null || clonedAttrs.isEmpty()) {
            sql += "SELECT DISTINCT " + tableName + ".*";
        } else {
            for (AnatEntityDAO.Attribute attribute: clonedAttrs) {
                if (sql.length() == 0) {
                    sql += "SELECT DISTINCT ";
                } else {
                    sql += ", ";
                }
                sql += tableName + "." + attributeToString(attribute);
            }
        }
        
        //*******************************
        // FROM CLAUSE
        //*******************************
        sql += " FROM " + tableName;
        String anatEntTaxConstTabName = "anatEntityTaxonConstraint";

        if (realAnySpecies) {
             sql += " INNER JOIN " + anatEntTaxConstTabName + " ON (" +
                          anatEntTaxConstTabName + ".anatEntityId = " + tableName + ".anatEntityId)";
        }

        //*******************************
        // WHERE CLAUSE
        //*******************************
        if (isSpeciesFilter || isEntityFilter) {
            sql += " WHERE ";
        }
        //species
        if (isSpeciesFilter) {
            if  (realAnySpecies) {
                sql += "(" + anatEntTaxConstTabName + ".speciesId IS NULL" +
                        " OR " + anatEntTaxConstTabName + ".speciesId IN (" + 
                        BgeePreparedStatement.generateParameterizedQueryString(
                                clonedSpeIds.size()) + "))";
            } else {
                String existsPart = "SELECT 1 FROM " + anatEntTaxConstTabName + " AS tc WHERE "
                        + "tc.anatEntityId = " + tableName + ".anatEntityId AND tc.speciesId ";
                sql += getAllSpeciesExistsClause(existsPart, clonedSpeIds.size());
            }
        }
        if (isEntityFilter) {
            if (isSpeciesFilter) {
                sql += " AND ";
            }
            sql += tableName + ".anatEntityId IN (" + 
                    BgeePreparedStatement.generateParameterizedQueryString(
                            clonedEntityIds.size()) + ")";
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
            if (isEntityFilter) {
                int offsetParamIndex = (isSpeciesFilter? clonedSpeIds.size() + 1: 1);
                stmt.setStrings(offsetParamIndex, clonedEntityIds, true);
            }

            return log.traceExit(new MySQLAnatEntityTOResultSet(stmt));
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
    }
    
    @Override
    public AnatEntityTOResultSet getNonInformativeAnatEntitiesBySpeciesIds(Collection<Integer> speciesIds,
            boolean evenIfUsedInAnnots, Collection<AnatEntityDAO.Attribute> attributes) throws DAOException {
        log.traceEntry("{}, {}, {}", speciesIds, evenIfUsedInAnnots, attributes);

        Set<Integer> clonedSpeIds = Optional.ofNullable(speciesIds)
                .map(c -> new HashSet<>(c)).orElse(null);
        EnumSet<AnatEntityDAO.Attribute> clonedAttrs = attributes == null || attributes.isEmpty()?
                EnumSet.allOf(AnatEntityDAO.Attribute.class): EnumSet.copyOf(attributes);
        boolean isSpeciesFilter = clonedSpeIds != null && clonedSpeIds.size() > 0;
        String tableName = "anatEntity";
        
        String sql = clonedAttrs.stream()
                .map(a -> tableName + "." + attributeToString(a))
                .collect(Collectors.joining(", ", "SELECT DISTINCT ", " FROM " + tableName));

        String condTabName = "cond";
        String anatEntTaxConstTabName = "anatEntityTaxonConstraint";
        String similarityTabName = "similarityAnnotationToAnatEntityId";

        if (isSpeciesFilter) {
            sql += " INNER JOIN " + anatEntTaxConstTabName + " ON " +
                    anatEntTaxConstTabName + ".anatEntityId = " + tableName + ".anatEntityId";
        }
        sql += " WHERE " + tableName + ".nonInformative = 1 ";
        if (isSpeciesFilter) {
            sql += " AND (" + anatEntTaxConstTabName + ".speciesId IS NULL" +
                   " OR " + anatEntTaxConstTabName + ".speciesId IN (" +
                   BgeePreparedStatement.generateParameterizedQueryString(clonedSpeIds.size()) + 
                   "))";
        }
        if (!evenIfUsedInAnnots) {
            sql += " AND NOT EXISTS (SELECT 1 FROM " + condTabName
                    + " WHERE " + condTabName + ".anatEntityId = " + tableName + ".anatEntityId "
                    + "OR " + condTabName + ".cellTypeId = " + tableName + ".anatEntityId)"
                    + " AND NOT EXISTS (SELECT 1 FROM " + similarityTabName
                    + " WHERE " + similarityTabName + ".anatEntityId = " + tableName + ".anatEntityId)";
        }
        
        //we don't use a try-with-resource, because we return a pointer to the results, 
        //not the actual results, so we should not close this BgeePreparedStatement.
        try {
            BgeePreparedStatement stmt = this.getManager().getConnection().prepareStatement(sql);
            if (isSpeciesFilter) {
                stmt.setIntegers(1, clonedSpeIds, true);
            }             
            return log.traceExit(new MySQLAnatEntityTOResultSet(stmt));
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }

    }

    /** 
     * Returns a {@code String} that correspond to the given {@code AnatEntityDAO.Attribute}.
     * 
     * @param attribute     An {code AnatEntityDAO.Attribute} that is the attribute to
     *                      convert into a {@code String}.
     * @return              A {@code String} that corresponds to the given 
     *                      {@code AnatEntityDAO.Attribute}
     * @throws IllegalArgumentException If the {@code attribute} is unknown.
     */
    private static String attributeToString(AnatEntityDAO.Attribute attribute)
            throws IllegalArgumentException {
        log.traceEntry("{}", attribute);
        
        switch (attribute) {
            case ID: 
                return log.traceExit("anatEntityId");
            case NAME: 
                return log.traceExit("anatEntityName");
            case DESCRIPTION: 
                return log.traceExit("anatEntityDescription");
            case START_STAGE_ID: 
                return log.traceExit("startStageId");
            case END_STAGE_ID: 
                return log.traceExit("endStageId");
            case NON_INFORMATIVE: 
                return log.traceExit("nonInformative");
//            case CELL_TYPE: 
//                return log.traceExit("cellType");
            default: 
                throw log.throwing(new IllegalArgumentException("The attribute provided (" + 
                       attribute.toString() + ") is unknown for " + AnatEntityDAO.class.getName()));
        }
    }

    @Override
    public int insertAnatEntities(Collection<AnatEntityTO> anatEntities) 
            throws DAOException, IllegalArgumentException{
        log.traceEntry("{}", anatEntities);

        if (anatEntities == null || anatEntities.isEmpty()) {
            throw log.throwing(new IllegalArgumentException(
                    "No anatomical entity is given, then no anatomical entity is inserted"));
        }

        // And we need to build two different queries. 
        String sqlExpression = "INSERT INTO anatEntity " +
                "(anatEntityId, anatEntityName, anatEntityDescription, " +
                "startStageId, endStageId, nonInformative) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        
        // To not overload MySQL with an error com.mysql.jdbc.PacketTooBigException, 
        // and because of laziness, we insert expression calls one at a time
        int entityInsertedCount = 0;
        try (BgeePreparedStatement stmt = 
                this.getManager().getConnection().prepareStatement(sqlExpression)) {
            for (AnatEntityTO anatEntity: anatEntities) {
                stmt.setString(1, anatEntity.getId());
                if (StringUtils.isNotBlank(anatEntity.getName())) {
                    stmt.setString(2, anatEntity.getName());
                } else {
                    stmt.setString(2, "");
                }
                stmt.setString(3, anatEntity.getDescription());
                stmt.setString(4, anatEntity.getStartStageId());
                stmt.setString(5, anatEntity.getEndStageId());
                stmt.setBoolean(6, anatEntity.isNonInformative());
                entityInsertedCount += stmt.executeUpdate();
                stmt.clearParameters();
            }
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }

        return log.traceExit(entityInsertedCount);
    }

    /**
     * A {@code MySQLDAOResultSet} specific to {@code AnatEntityTO}.
     * 
     * @author Valentine Rech de Laval
     * @version Bgee 13
     * @since Bgee 13
     */
    public class MySQLAnatEntityTOResultSet extends MySQLDAOResultSet<AnatEntityTO> 
                                            implements AnatEntityTOResultSet {
        /**
         * Delegates to {@link MySQLDAOResultSet#MySQLDAOResultSet(BgeePreparedStatement)}
         * super constructor.
         * 
         * @param statement The first {@code BgeePreparedStatement} to execute a query on.
         */
        private MySQLAnatEntityTOResultSet(BgeePreparedStatement statement) {
            super(statement);
        }

        @Override
        protected AnatEntityTO getNewTO() throws DAOException {
            log.traceEntry();
            
            String anatEntityId = null, anatEntityName = null, anatEntityDescription = null, 
                    startStageId = null, endStageId = null;
            Boolean nonInformative = null, cellType = null;
            
            for (Entry<Integer, String> column: this.getColumnLabels().entrySet()) {
                try {
                    if (column.getValue().equals("anatEntityId")) {
                        anatEntityId = this.getCurrentResultSet().getString(column.getKey());
                    } else if (column.getValue().equals("anatEntityName")) {
                        anatEntityName = this.getCurrentResultSet().getString(column.getKey());
                    } else if (column.getValue().equals("anatEntityDescription")) {
                        anatEntityDescription = this.getCurrentResultSet().getString(column.getKey());
                    } else if (column.getValue().equals("startStageId")) {
                        startStageId = this.getCurrentResultSet().getString(column.getKey());
                    } else if (column.getValue().equals("endStageId")) {
                        endStageId = this.getCurrentResultSet().getString(column.getKey());
                    } else if (column.getValue().equals("nonInformative")) {
                        nonInformative = this.getCurrentResultSet().getBoolean(column.getKey());
                    } else if (column.getValue().equals("cellType")) {
                        cellType = this.getCurrentResultSet().getBoolean(column.getKey());
                    } else {
                        throw log.throwing(new UnrecognizedColumnException(column.getValue()));
                    }           
                } catch (SQLException e) {
                    throw log.throwing(new DAOException(e));
                }
            }
            return log.traceExit(new AnatEntityTO(anatEntityId, anatEntityName, anatEntityDescription, 
                    startStageId, endStageId, nonInformative, cellType));
        }
    }
}
