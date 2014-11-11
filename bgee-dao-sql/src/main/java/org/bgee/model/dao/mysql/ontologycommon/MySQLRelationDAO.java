package org.bgee.model.dao.mysql.ontologycommon;

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
import org.bgee.model.dao.api.ontologycommon.RelationDAO;
import org.bgee.model.dao.api.ontologycommon.RelationDAO.RelationTO.RelationStatus;
import org.bgee.model.dao.api.ontologycommon.RelationDAO.RelationTO.RelationType;

/**
 * A {@code RelationDAO} for MySQL. 
 * 
 * @author Valentine Rech de Laval
 * @version Bgee 13
 * @see org.bgee.model.dao.api.ontologycommon.RelationDAO.RelationTO
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
    public RelationTOResultSet getAnatEntityRelations(Set<String> speciesIds, 
            Set<RelationType> relationTypes, Set<RelationStatus> relationStatus) {
        log.entry(speciesIds, relationTypes, relationStatus);    

        String tableName = "anatEntityRelation";
        
        boolean isSpeciesFilter = speciesIds != null && speciesIds.size() > 0;
        boolean isRelationTypeFilter = relationTypes != null && relationTypes.size() > 0;
        boolean isRelationStatusFilter = relationStatus != null && relationStatus.size() > 0;
        
        String sql = null;
        Collection<RelationDAO.Attribute> attributes = this.getAttributes();
        if (attributes == null || attributes.isEmpty()) {
            sql = "SELECT DISTINCT " + tableName + ".*";
        } else {
            for (RelationDAO.Attribute attribute: attributes) {
                if (StringUtils.isEmpty(sql)) {
                    sql = "SELECT DISTINCT ";
                } else {
                    sql += ", ";
                }
                sql += tableName + "." + this.attributeAnatEntityRelationToString(attribute);
            }
        }
        sql += " FROM " + tableName;
        
        if (isSpeciesFilter) {
            sql += " INNER JOIN anatEntityRelationTaxonConstraint ON (" +
                    "anatEntityRelationTaxonConstraint.anatEntityRelationId = "
                    + tableName + ".anatEntityRelationId)";
        }
        
        if (isSpeciesFilter || isRelationTypeFilter || isRelationStatusFilter) {
            sql += " WHERE ";
        }
        if (isSpeciesFilter) {
            sql += "(anatEntityRelationTaxonConstraint.speciesId IS NULL" +
                   " OR anatEntityRelationTaxonConstraint.speciesId IN (" +
                   BgeePreparedStatement.generateParameterizedQueryString(
                           speciesIds.size()) + "))";
        }
        if (isRelationTypeFilter) {
            if (isSpeciesFilter) {
                sql += " AND ";
            }
            sql += " relationType IN (" + 
            BgeePreparedStatement.generateParameterizedQueryString(relationTypes.size()) + ")";
        }
        if (isRelationStatusFilter) {
            if (isSpeciesFilter || isRelationTypeFilter) {
                sql += " AND ";
            }
            sql += " relationStatus IN (" + 
            BgeePreparedStatement.generateParameterizedQueryString(relationStatus.size()) + ")";
        }
        
//        sql += " ORDER BY " + tableName + ".anatEntitySourceId, " + 
//                              tableName + ".anatEntityTargetId";

         //we don't use a try-with-resource, because we return a pointer to the results, 
         //not the actual results, so we should not close this BgeePreparedStatement.
         BgeePreparedStatement stmt = null;
         try {
             stmt = this.getManager().getConnection().prepareStatement(sql);
             int startIndex = 1;
             if (isSpeciesFilter) {
                 List<Integer> orderedSpeciesIds = MySQLDAO.convertToIntList(speciesIds);
                 Collections.sort(orderedSpeciesIds);
                 stmt.setIntegers(startIndex, orderedSpeciesIds);
                 startIndex += orderedSpeciesIds.size();
             }
             if (isRelationTypeFilter) {
                 List<RelationType> orderedTypes = new ArrayList<RelationType>(relationTypes);
                 Collections.sort(orderedTypes);
                 stmt.setEnumDAOFields(startIndex, orderedTypes);
                 startIndex += orderedTypes.size();
             }
             if (isRelationStatusFilter) {
                 List<RelationStatus> orderedStatus = 
                         new ArrayList<RelationStatus>(relationStatus);
                 Collections.sort(orderedStatus);
                 stmt.setEnumDAOFields(startIndex, orderedStatus);
                 startIndex += orderedStatus.size();
             }
             return log.exit(new MySQLRelationTOResultSet(stmt));
         } catch (SQLException e) {
             throw log.throwing(new DAOException(e));
         }
    }
     
    @Override
    public RelationTOResultSet getStageRelations(Set<String> speciesIds, 
            Set<RelationStatus> relationStatus) {
        //NOTE: there is no relation table for stages, as they are represented 
        //as a nested set model. So, this method will emulate the existence of such a table, 
        //so that retrieval of relations between stages will be consistent with retrieval 
        //of relations between anatomical entities.
        
        log.entry(speciesIds, relationStatus); 
        
        boolean isSpeciesFilter = speciesIds != null && speciesIds.size() > 0;
        boolean isRelationStatusFilter = relationStatus != null && relationStatus.size() > 0;
        
        String sql = null;
        Collection<RelationDAO.Attribute> attributes = this.getAttributes();
        if (attributes == null || attributes.isEmpty()) {
            sql = "SELECT *";
        } else {
            for (RelationDAO.Attribute attribute: attributes) {
                if (sql == null) {
                    sql = "SELECT DISTINCT ";
                } else {
                    sql += ", ";
                }
                sql += this.attributeStageRelationToString(attribute);
            }
        }
        sql += " FROM ";
        
        //OK, we create a query that will emulate a temporary table similar to
        //the retrieval of relations between anatomical entities.
        sql += 
            // no relationId, provide 0 for all
            "(SELECT DISTINCT 0 AS stageRelationId, " +
        	"t1.stageId AS stageSourceId, " +
            "t3.stageId AS stageTargetId, " +
            //no other parenthood relations between stages other than is_a
            "'" + RelationType.ISA_PARTOF.getStringRepresentation() + "' AS relationType, " +
            //emulate RelationStatus
            "IF (t1.stageId = t3.stageId, " + 
                "'" + RelationStatus.REFLEXIVE.getStringRepresentation() + "', " +
                "IF (t3.stageLevel = t1.stageLevel + 1, " + 
                "'" + RelationStatus.DIRECT.getStringRepresentation() + "', " +
                "'" + RelationStatus.INDIRECT.getStringRepresentation() + "')) AS relationStatus " +
            "FROM stage AS t1 " +
            "INNER JOIN stageTaxonConstraint AS t2 " +
                "ON t1.stageId = t2.stageId " +
            "INNER JOIN stage AS t3 " +
                "ON t3.stageLeftBound >= t1.stageLeftBound " +
                "AND t3.stageRightBound <= t1.stageRightBound " +
            "INNER JOIN stageTaxonConstraint AS t4 " +
                "ON t3.stageId = t4.stageId AND " +
                "(t2.speciesId IS NULL OR t4.speciesId IS NULL OR t4.speciesId = t2.speciesId) ";
        if (isSpeciesFilter) {
            //a case is not covered in this where clause: for instance, if we query relations 
            //for species 1 or species 2, while stage 1 exists in species 1, and stqge2 
            //in species 2. With only this where clause, we could retrieve 
            //an incorrect relation between stage 1 an stage 2. But this is not possible 
            //thanks to the join clause above between t4 and t2. 
            sql += "WHERE (t2.speciesId IS NULL OR t2.speciesId IN (" +
                   BgeePreparedStatement.generateParameterizedQueryString(
                           speciesIds.size()) + ")) " +
                   "AND (t4.speciesId IS NULL OR t4.speciesId IN (" +
                   BgeePreparedStatement.generateParameterizedQueryString(
                           speciesIds.size()) + ")) ";
        }
        sql += ") AS tempTable ";
        
        if (isRelationStatusFilter) {
            sql += " WHERE relationStatus IN (" + 
            BgeePreparedStatement.generateParameterizedQueryString(relationStatus.size()) + ")";
        }

         //we don't use a try-with-resource, because we return a pointer to the results, 
         //not the actual results, so we should not close this BgeePreparedStatement.
         BgeePreparedStatement stmt = null;
         try {
             stmt = this.getManager().getConnection().prepareStatement(sql);
             int startIndex = 1;
             if (isSpeciesFilter) {
                 List<Integer> orderedSpeciesIds = MySQLDAO.convertToIntList(speciesIds);
                 Collections.sort(orderedSpeciesIds);
                 stmt.setIntegers(startIndex, orderedSpeciesIds);
                 startIndex += orderedSpeciesIds.size();
                 //we set the species IDs twice, once for the parent stages, 
                 //once for the child stages
                 stmt.setIntegers(startIndex, orderedSpeciesIds);
                 startIndex += orderedSpeciesIds.size();
             }
             if (isRelationStatusFilter) {
                 List<RelationStatus> orderedStatus = 
                         new ArrayList<RelationStatus>(relationStatus);
                 Collections.sort(orderedStatus);
                 stmt.setEnumDAOFields(startIndex, orderedStatus);
                 startIndex += orderedStatus.size();
             }
             return log.exit(new MySQLRelationTOResultSet(stmt));
         } catch (SQLException e) {
             throw log.throwing(new DAOException(e));
         }
    }

    /** 
     * Returns a {@code String} that correspond to the given {@code RelationDAO.Attribute}, 
     * for retrieval of relations between anatomical entities.
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
    /** 
     * Returns a {@code String} that correspond to the given {@code RelationDAO.Attribute}, 
     * for retrieval of relations between stages. Note that this does not correspond 
     * to attributes of an actual table, but an emulated temporary table to use 
     * queries consistent with retrieval of relations between anatomical entities. 
     * 
     * @param attribute   A {code RelationDAO.Attribute} that is the attribute to
     *                    convert in a {@code String}.
     * @return            A {@code String} that correspond to the given 
     *                    {@code RelationDAO.Attribute}
     */
    private String attributeStageRelationToString(RelationDAO.Attribute attribute) {
        log.entry(attribute);
        
        String label = null;
        if (attribute.equals(RelationDAO.Attribute.RELATIONID)) {
                label = "stageRelationId";
        } else if (attribute.equals(RelationDAO.Attribute.SOURCEID)) {
            label = "stageSourceId";
        } else if (attribute.equals(RelationDAO.Attribute.TARGETID)) {
            label = "stageTargetId";
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
    public int insertAnatEntityRelations(Collection<RelationTO> relations) 
            throws DAOException, IllegalArgumentException {

        log.entry(relations);

        if (relations == null || relations.isEmpty()) {
            throw log.throwing(new IllegalArgumentException(
                    "No anatomical entity relation is given, then no relation is inserted"));
        }
        
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
    public int insertGeneOntologyRelations(Collection<RelationTO> relations) 
            throws DAOException, IllegalArgumentException {
        log.entry(relations);
        
        if (relations == null || relations.isEmpty()) {
            throw log.throwing(new IllegalArgumentException(
                    "No Gene Ontology relation is given, then no relation is inserted"));
        }

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
         * Delegates to {@link MySQLDAOResultSet#MySQLDAOResultSet(BgeePreparedStatement)}
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
                    if (column.getValue().equals("anatEntityRelationId") || 
                            column.getValue().equals("stageRelationId")) {
                        relationId = currentResultSet.getString(column.getKey());
                    } else if (column.getValue().equals("anatEntitySourceId") || 
                            column.getValue().equals("goAllSourceId") || 
                            column.getValue().equals("stageSourceId")) {
                        sourceId = currentResultSet.getString(column.getKey());
                    } else if (column.getValue().equals("anatEntityTargetId") || 
                            column.getValue().equals("goAllTargetId") || 
                            column.getValue().equals("stageTargetId") ) {
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
