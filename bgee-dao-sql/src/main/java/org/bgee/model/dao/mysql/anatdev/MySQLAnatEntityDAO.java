package org.bgee.model.dao.mysql.anatdev;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.anatdev.AnatEntityDAO;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.mysql.MySQLDAO;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.model.dao.mysql.connector.MySQLDAOResultSet;


/**
 * A {@code AnatEntityDAO} for MySQL. 
 * 
 * @author Valentine Rech de Laval
 * @version Bgee 13
 * @see org.bgee.model.dao.api.gene.AnatEntityDAO.AnatEntityTO
 * @since Bgee 13
 */
public class MySQLAnatEntityDAO extends MySQLDAO<AnatEntityDAO.Attribute> 
                                implements AnatEntityDAO {
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
    public AnatEntityTOResultSet getAllAnatEntities(Set<String> speciesIds) throws DAOException {
        log.entry(speciesIds);

        StringBuilder sql = new StringBuilder(); 
        Collection<AnatEntityDAO.Attribute> attributes = this.getAttributes();
        if (attributes == null || attributes.size() == 0) {
            sql.append("SELECT *");
        } else {
            for (AnatEntityDAO.Attribute attribute: attributes) {
                if (sql.length() == 0) {
                    sql.append("SELECT ");
                } else {
                    sql.append(", ");
                }
                sql.append("anatEntity.");
                sql.append(this.attributeToString(attribute));
            }
        }
        sql.append(" FROM anatEntity");
         if (speciesIds != null && speciesIds.size() > 0) {
             sql.append(" INNER JOIN anatEntityTaxonConstraint ON (" +
                                "anatEntityTaxonConstraint.anatEntityId = anatEntity.anatEntityId)");
             sql.append(" WHERE anatEntityTaxonConstraint.speciesId IS NULL");
             sql.append(" OR anatEntityTaxonConstraint.speciesId IN (");
             sql.append(createStringFromSet(speciesIds, ','));
             sql.append(")");
         }

         //we don't use a try-with-resource, because we return a pointer to the results, 
         //not the actual results, so we should not close this BgeePreparedStatement.
         BgeePreparedStatement stmt = null;
         try {
             stmt = this.getManager().getConnection().prepareStatement(sql.toString());
             return log.exit(new MySQLAnatEntityTOResultSet(stmt));
         } catch (SQLException e) {
             throw log.throwing(new DAOException(e));
         }
    }
    
    /** 
     * Returns a {@code String} that correspond to the given {@code AnatEntityDAO.Attribute}.
     * 
     * @param attribute   An {code AnatEntityDAO.Attribute} that is the attribute to
     *                    convert in a {@code String}.
     * @return            A {@code String} that correspond to the given 
     *                    {@code AnatEntityDAO.Attribute}
     */
    private String attributeToString(AnatEntityDAO.Attribute attribute) {
        log.entry(attribute);
        
        String label = null;
        if (attribute.equals(AnatEntityDAO.Attribute.ID)) {
                label = "anatEntityId";
        } else if (attribute.equals(AnatEntityDAO.Attribute.NAME)) {
            label = "anatEntityName";
        } else if (attribute.equals(AnatEntityDAO.Attribute.DESCRIPTION)) {
            label = "anatEntityDescription";
        } else if (attribute.equals(AnatEntityDAO.Attribute.STARTSTAGEID)) {
            label = "startStageId";
        } else if (attribute.equals(AnatEntityDAO.Attribute.ENDSTAGEID)) {
            label = "endStageId";
        } else {
            throw log.throwing(new IllegalStateException("The attribute provided (" +
                    attribute.toString() + ") is unknown for " + AnatEntityDAO.class.getName()));
        }
        
        return log.exit(label);
    }

    @Override
    public int insertAnatEntities(Collection<AnatEntityTO> anatEntities) throws DAOException {
        log.entry(anatEntities);

        // And we need to build two different queries. 
        String sqlExpression = "INSERT INTO anatEntity " +
                "(anatEntityId, anatEntityName, anatEntityDescription, startStageId, endStageId) " +
                "VALUES (?, ?, ?, ?, ?)";
        
        // To not overload MySQL with an error com.mysql.jdbc.PacketTooBigException, 
        // and because of laziness, we insert expression calls one at a time
        int entityInsertedCount = 0;
        try (BgeePreparedStatement stmt = 
                this.getManager().getConnection().prepareStatement(sqlExpression)) {
            for (AnatEntityTO entity: anatEntities) {
                stmt.setString(1, entity.getId());
                stmt.setString(2, entity.getName());
                stmt.setString(3, entity.getDescription());
                stmt.setString(4, entity.getStartStageId());
                stmt.setString(5, entity.getEndStageId());
                entityInsertedCount += stmt.executeUpdate();
                stmt.clearParameters();
            }
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }

        return log.exit(entityInsertedCount);
    }

    /**
     * A {@code MySQLDAOResultSet} specific to {@code GlobalExpressionToExpressionTO}.
     * 
     * @author Valentine Rech de Laval
     * @version Bgee 13
     * @since Bgee 13
     */
    public class MySQLAnatEntityTOResultSet extends MySQLDAOResultSet<AnatEntityTO> 
                                            implements AnatEntityTOResultSet {
        /**
         * Delegates to {@link MySQLDAOResultSet#MySQLDAOResultSet(BgeePreparedStatement)
         * super constructor.
         * 
         * @param statement The first {@code BgeePreparedStatement} to execute a query on.
         */
        public MySQLAnatEntityTOResultSet(BgeePreparedStatement statement) {
            super(statement);
        }

        @Override
        public AnatEntityTO getTO() throws DAOException {
            log.entry();
            String anatEntityId = null, anatEntityName = null, anatEntityDescription = null, 
                    startStageId = null, endStageId = null;

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
                    }                
                } catch (SQLException e) {
                    throw log.throwing(new DAOException(e));
                }
            }
            return log.exit(new AnatEntityTO(anatEntityId, anatEntityName, anatEntityDescription, 
                    startStageId, endStageId));
        }
    }
}