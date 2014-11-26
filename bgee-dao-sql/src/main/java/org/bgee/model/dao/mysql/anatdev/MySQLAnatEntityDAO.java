package org.bgee.model.dao.mysql.anatdev;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.anatdev.AnatEntityDAO;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.mysql.MySQLDAO;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.model.dao.mysql.connector.MySQLDAOResultSet;


/**
 * An {@code AnatEntityDAO} for MySQL. 
 * 
 * @author Valentine Rech de Laval
 * @version Bgee 13
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
    public AnatEntityTOResultSet getAnatEntities(Set<String> speciesIds) throws DAOException {
        log.entry(speciesIds);      
        
        String tableName = "anatEntity";

        String sql = new String(); 
        Collection<AnatEntityDAO.Attribute> attributes = this.getAttributes();
        if (attributes == null || attributes.size() == 0) {
            sql += "SELECT DISTINCT " + tableName + ".*";
        } else {
            for (AnatEntityDAO.Attribute attribute: attributes) {
                if (sql.length() == 0) {
                    sql += "SELECT DISTINCT ";
                } else {
                    sql += ", ";
                }
                sql += tableName + "." + this.attributeToString(attribute);
            }
        }
        sql += " FROM " + tableName;
        String anatEntTaxConstTabName = "anatEntityTaxonConstraint";
        if (speciesIds != null && speciesIds.size() != 0) {
             sql += " INNER JOIN " + anatEntTaxConstTabName + " ON (" +
                          anatEntTaxConstTabName + ".anatEntityId = " + tableName + ".anatEntityId)" +
                    " WHERE " + anatEntTaxConstTabName + ".speciesId IS NULL" +
                    " OR " + anatEntTaxConstTabName + ".speciesId IN (" + 
                        BgeePreparedStatement.generateParameterizedQueryString(
                                speciesIds.size()) + ")";
         }

         //we don't use a try-with-resource, because we return a pointer to the results, 
         //not the actual results, so we should not close this BgeePreparedStatement.
         BgeePreparedStatement stmt = null;
         try {
             stmt = this.getManager().getConnection().prepareStatement(sql.toString());
             if (speciesIds != null && speciesIds.size() != 0) {
                 List<Integer> orderedSpeciesIds = MySQLDAO.convertToIntList(speciesIds);
                 Collections.sort(orderedSpeciesIds);
                 stmt.setIntegers(1, orderedSpeciesIds);
             }             
             return log.exit(new MySQLAnatEntityTOResultSet(stmt));
         } catch (SQLException e) {
             throw log.throwing(new DAOException(e));
         }
    }
    
    @Override
    public AnatEntityTOResultSet getNonInformativeAnatEntities(Set<String> speciesIds) 
            throws DAOException {
        log.entry(speciesIds);      

        boolean isSpeciesFilter = speciesIds != null && speciesIds.size() > 0;
        String tableName = "anatEntity";
        
        String sql = new String(); 
        Collection<AnatEntityDAO.Attribute> attributes = this.getAttributes();
        if (attributes == null || attributes.size() == 0) {
            sql += "SELECT DISTINCT " + tableName + ".*";
        } else {
            for (AnatEntityDAO.Attribute attribute: attributes) {
                if (StringUtils.isEmpty(sql)) {
                    sql += "SELECT DISTINCT ";
                } else {
                    sql += ", ";
                }
                sql += tableName + "." + this.attributeToString(attribute);
            }
        }
        
        String expressionTabName = "expression";
        String noExpressionTabName = "noExpression";

        sql += " FROM " + tableName +
               " LEFT OUTER JOIN " + expressionTabName + " ON (" + 
                        expressionTabName + ".anatEntityId = " + tableName + ".anatEntityId)" +
               " LEFT OUTER JOIN " + noExpressionTabName + " ON (" + 
                       noExpressionTabName + ".anatEntityId = " + tableName + ".anatEntityId)";
        
        String anatEntTaxConstTabName = "anatEntityTaxonConstraint";

        if (isSpeciesFilter) {
            sql += " INNER JOIN " + anatEntTaxConstTabName + " ON (" +
                    anatEntTaxConstTabName + ".anatEntityId = " + tableName + ".anatEntityId)";
        }
        sql += " WHERE " + tableName + ".nonInformative = true " +
               "AND " + expressionTabName + ".anatEntityId IS NULL " +
               "AND " + noExpressionTabName + ".anatEntityId IS NULL";
        if (isSpeciesFilter) {
            sql += " AND (" + anatEntTaxConstTabName + ".speciesId IS NULL" +
                   " OR " + anatEntTaxConstTabName + ".speciesId IN (" +
                   BgeePreparedStatement.generateParameterizedQueryString(speciesIds.size()) + 
                   "))";
        }
//        sql += " ORDER BY " + tableName + ".anatEntityId";
        
        //we don't use a try-with-resource, because we return a pointer to the results, 
        //not the actual results, so we should not close this BgeePreparedStatement.
        BgeePreparedStatement stmt = null;
        try {
            stmt = this.getManager().getConnection().prepareStatement(sql);
            if (isSpeciesFilter) {
                List<Integer> orderedSpeciesIds = MySQLDAO.convertToIntList(speciesIds);
                Collections.sort(orderedSpeciesIds);
                stmt.setIntegers(1, orderedSpeciesIds);
            }             
            return log.exit(new MySQLAnatEntityTOResultSet(stmt));
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }

    }

    /** 
     * Returns a {@code String} that correspond to the given {@code AnatEntityDAO.Attribute}.
     * 
     * @param attribute   An {code AnatEntityDAO.Attribute} that is the attribute to
     *                    convert into a {@code String}.
     * @return            A {@code String} that corresponds to the given 
     *                    {@code AnatEntityDAO.Attribute}
     */
    private String attributeToString(AnatEntityDAO.Attribute attribute) {
        log.entry(attribute);
        
        switch (attribute) {
            case ID: 
                return log.exit("anatEntityId");
            case NAME: 
                return log.exit("anatEntityName");
            case DESCRIPTION: 
                return log.exit("anatEntityDescription");
            case STARTSTAGEID: 
                return log.exit("startStageId");
            case ENDSTAGEID: 
                return log.exit("endStageId");
            case NONINFORMATIVE: 
                return log.exit("nonInformative");
            default: 
                throw log.throwing(new AssertionError("The attribute provided (" + 
                       attribute.toString() + ") is unknown for " + AnatEntityDAO.class.getName()));
        }
    }

    @Override
    public int insertAnatEntities(Collection<AnatEntityTO> anatEntities) 
            throws DAOException, IllegalArgumentException{
        log.entry(anatEntities);

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
                stmt.setString(2, anatEntity.getName());
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

        return log.exit(entityInsertedCount);
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
        public AnatEntityTO getTO() throws DAOException {
            log.entry();
            
            String anatEntityId = null, anatEntityName = null, anatEntityDescription = null, 
                    startStageId = null, endStageId = null;
            Boolean nonInformative = null;
            
            ResultSet currentResultSet = this.getCurrentResultSet();
            for (Entry<Integer, String> column: this.getColumnLabels().entrySet()) {
                try {
                    if (column.getValue().equals("anatEntityId")) {
                        anatEntityId = currentResultSet.getString(column.getKey());
                    } else if (column.getValue().equals("anatEntityName")) {
                        anatEntityName = currentResultSet.getString(column.getKey());
                    } else if (column.getValue().equals("anatEntityDescription")) {
                        anatEntityDescription = currentResultSet.getString(column.getKey());
                    } else if (column.getValue().equals("startStageId")) {
                        startStageId = currentResultSet.getString(column.getKey());
                    } else if (column.getValue().equals("endStageId")) {
                        endStageId = currentResultSet.getString(column.getKey());
                    } else if (column.getValue().equals("nonInformative")) {
                        nonInformative = currentResultSet.getBoolean(column.getKey());
                    }                
                } catch (SQLException e) {
                    throw log.throwing(new DAOException(e));
                }
            }
            return log.exit(new AnatEntityTO(anatEntityId, anatEntityName, anatEntityDescription, 
                    startStageId, endStageId, nonInformative));
        }
    }
}
