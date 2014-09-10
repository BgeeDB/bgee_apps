package org.bgee.model.dao.mysql.ontologycommon;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.mysql.MySQLDAO;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.model.dao.mysql.connector.MySQLDAOResultSet;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.ontologycommon.RelationDAO;
import org.bgee.model.dao.api.ontologycommon.RelationDAO.RelationTO.RelationStatus;
import org.bgee.model.dao.api.ontologycommon.RelationDAO.RelationTO.RelationType;

/**
 * A {@code RelationDAO} for MySQL. 
 * 
 * @author Valentine Rech de Laval
 * @version Bgee 13
 * @see org.bgee.model.dao.api.gene.RelationDAO.RelationTO
 * @since Bgee 13
 */
public class MySQLRelationDAO extends MySQLDAO<RelationDAO.Attribute> 
                                    implements RelationDAO {
    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = LogManager.getLogger(MySQLRelationDAO.class.getName());

    /**
     * Constructor providing the {@code MySQLDAOManager} that this {@code MySQLDAO} 
     * will use to obtain {@code BgeeConnection}s.
     * 
     * @param manager                       The {@code MySQLDAOManager} to use.
     * @throws IllegalArgumentException     If {@code manager} is {@code null}.
     */
    public MySQLRelationDAO(MySQLDAOManager manager) throws IllegalArgumentException {
        super(manager);
    }

    @Override
    public RelationTOResultSet getAllAnatEntityRelations(Set<String> speciesIds, 
            EnumSet<RelationType> relationTypes) {
        log.entry(speciesIds, relationTypes);

        boolean isSpeciesFilter = speciesIds != null && speciesIds.size() > 0;
        boolean isRelationTypeFilter = relationTypes != null && relationTypes.size() > 0;
        
        StringBuilder sql = new StringBuilder(); 
        Collection<RelationDAO.Attribute> attributes = this.getAttributes();
        if (attributes == null || attributes.size() == 0) {
            sql.append("SELECT *");
        } else {
            for (RelationDAO.Attribute attribute: attributes) {
                if (sql.length() == 0) {
                    sql.append("SELECT ");
                } else {
                    sql.append(", ");
                }
                sql.append("anatEntityRelation.");
                sql.append(this.attributeAnatEntityRelationToString(attribute));
            }
        }
        sql.append(" FROM anatEntityRelation");
        
        if (isSpeciesFilter) {
            sql.append(" INNER JOIN anatEntityRelationTaxonConstraint ON (" +
                    "anatEntityRelationTaxonConstraint.anatEntityRelationId = "
                    + "anatEntityRelation.anatEntityRelationId)");
        }
        if (isSpeciesFilter || isRelationTypeFilter) {
                sql.append(" WHERE ");
        }
        if (isSpeciesFilter) {
            sql.append("(anatEntityRelationTaxonConstraint.speciesId IS NULL");
            sql.append(" OR anatEntityRelationTaxonConstraint.speciesId IN (");
            sql.append(createStringFromSet(speciesIds, ','));
            sql.append("))");
        }
        
        if (isSpeciesFilter && isRelationTypeFilter) {
            sql.append(" AND ");
        }
        if (isRelationTypeFilter) {
            Set<String> convertedRelations = new HashSet<String>();
            for (RelationType relation: relationTypes) {
                convertedRelations.add("'"+relation.getStringRepresentation()+"'");
            }
            sql.append(" relationType IN (");
            sql.append(createStringFromSet(convertedRelations, ','));
            sql.append(")");
        }
        

         //we don't use a try-with-resource, because we return a pointer to the results, 
         //not the actual results, so we should not close this BgeePreparedStatement.
         BgeePreparedStatement stmt = null;
         try {
             stmt = this.getManager().getConnection().prepareStatement(sql.toString());
             return log.exit(new MySQLRelationTOResultSet(stmt));
         } catch (SQLException e) {
             throw log.throwing(new DAOException(e));
         }
    }

    /** 
     * Returns a {@code String} that correspond to the given {@code RelationDAO.Attribute}.
     * 
     * @param attribute   A {code RelationDAO.Attribute} that is the attribute to
     *                    convert in a {@code String}.
     * @return            A {@code String} that correspond to the given 
     *                    {@code RelationDAO.Attribute}
     */
    private String attributeAnatEntityRelationToString(RelationDAO.Attribute attribute) {
        log.entry(attribute);
        
        String label = null;
        if (attribute.equals(RelationDAO.Attribute.RELATIONID)) {
                label = "anatEntityRelationId";
        } else if (attribute.equals(RelationDAO.Attribute.SOURCEID)) {
            label = "anatEntitySourceId";
        } else if (attribute.equals(RelationDAO.Attribute.TARGETID)) {
            label = "anatEntityTargetId";
        } else if (attribute.equals(RelationDAO.Attribute.RELATIONTYPE)) {
            label = "relationType";
        } else if (attribute.equals(RelationDAO.Attribute.RELATIONSTATUS)) {
            label = "relationStatus";
        } else {
            throw log.throwing(new IllegalStateException("The attribute provided (" +
                    attribute.toString() + ") is unknown for " + RelationDAO.class.getName()));
        }
        
        return log.exit(label);
    }

    @Override
    public int insertAnatEntityRelations(Collection<RelationTO> relations) {
        log.entry(relations);

        // And we need to build two different queries. 
        String sqlExpression = "INSERT INTO anatEntityRelation " +
                "(anatEntityRelationId, anatEntitySourceId, anatEntityTargetId, " +
                " relationType, relationStatus) " +
                "VALUES (?, ?, ?, ?, ?)";
        
        // To not overload MySQL with an error com.mysql.jdbc.PacketTooBigException, 
        // and because of laziness, we insert expression calls one at a time
        int relationInsertedCount = 0;
        try (BgeePreparedStatement stmt = 
                this.getManager().getConnection().prepareStatement(sqlExpression)) {
            for (RelationTO relation: relations) {
                stmt.setInt(1, Integer.parseInt(relation.getId()));
                stmt.setString(2, relation.getSourceId());
                stmt.setString(3, relation.getTargetId());
                stmt.setString(4, relation.getRelationType().getStringRepresentation());
                stmt.setString(5, relation.getRelationStatus().getStringRepresentation());
                relationInsertedCount += stmt.executeUpdate();
                stmt.clearParameters();
            }
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }

        return log.exit(relationInsertedCount);
    }

    @Override
    public int insertGeneOntologyRelations(Collection<RelationTO> relations) throws DAOException {
        log.entry(relations);
        
        //to not overload MySQL with an error com.mysql.jdbc.PacketTooBigException, 
        //and because of laziness, we insert terms one at a time
        int relInsertedCount = 0;
        //TODO: this is where the new system appears to suck... continue here.
        String sql = "Insert into geneOntologyRelation (goAllTargetId, goAllSourceId) " +
                "values (?, ?) ";
        
        try (BgeePreparedStatement stmt = 
                this.getManager().getConnection().prepareStatement(sql)) {
            
            for (RelationTO rel: relations) {
                stmt.setString(1, rel.getTargetId());
                stmt.setString(2, rel.getSourceId());
                relInsertedCount += stmt.executeUpdate();
                stmt.clearParameters();
            }
            
            return log.exit(relInsertedCount);
            
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
    }
    
    /**
     * A {@code MySQLDAOResultSet} specific to {@code RelationTO}.
     * 
     * @author Valentine Rech de Laval
     * @version Bgee 13
     * @since Bgee 13
     */
    public class MySQLRelationTOResultSet extends MySQLDAOResultSet<RelationTO> 
                                          implements RelationTOResultSet {
        /**
         * Delegates to {@link MySQLDAOResultSet#MySQLDAOResultSet(BgeePreparedStatement)
         * super constructor.
         * 
         * @param statement The first {@code BgeePreparedStatement} to execute a query on.
         */
        public MySQLRelationTOResultSet(BgeePreparedStatement statement) {
            super(statement);
        }

        @Override
        public RelationTO getTO() throws DAOException {
            log.entry();
            String relationId = null, sourceId = null, targetId =null;
            RelationType relationType = null;
            RelationStatus relationStatus = null;
            
            ResultSet currentResultSet = this.getCurrentResultSet();
            for (Entry<Integer, String> column: this.getColumnLabels().entrySet()) {
                try {
                    if (column.getValue().equals("anatEntityRelationId")) {
                        relationId = currentResultSet.getString(column.getKey());
                    } else if (column.getValue().equals("anatEntitySourceId") || column.getValue().equals("goAllSourceId") ) {
                        sourceId = currentResultSet.getString(column.getKey());
                    } else if (column.getValue().equals("anatEntityTargetId") || column.getValue().equals("goAllTargetId") ) {
                        targetId = currentResultSet.getString(column.getKey());
                    } else if (column.getValue().equals("relationStatus")) {
                        relationStatus = RelationStatus.convertToRelationStatus(
                                currentResultSet.getString(column.getKey()));
                    } else if (column.getValue().equals("relationType")) {
                        relationType = RelationType.convertToRelationType(
                                currentResultSet.getString(column.getKey()));
                    }
                } catch (SQLException e) {
                    throw log.throwing(new DAOException(e));
                }
            }
            return log.exit(new RelationTO(relationId, sourceId, targetId, 
                    relationType, relationStatus));
        }
    }
}
