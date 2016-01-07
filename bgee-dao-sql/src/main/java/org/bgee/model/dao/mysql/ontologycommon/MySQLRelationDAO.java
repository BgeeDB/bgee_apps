package org.bgee.model.dao.mysql.ontologycommon;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.mysql.MySQLDAO;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.model.dao.mysql.connector.MySQLDAOResultSet;
import org.bgee.model.dao.mysql.exception.UnrecognizedColumnException;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.ontologycommon.RelationDAO;
import org.bgee.model.dao.api.ontologycommon.RelationDAO.RelationTO.RelationStatus;
import org.bgee.model.dao.api.ontologycommon.RelationDAO.RelationTO.RelationType;

/**
 * A {@code RelationDAO} for MySQL. 
 * 
 * @author Valentine Rech de Laval
 * @author Frederic Bastian
 * @version Bgee 13, Jan. 2016
 * @since Bgee 13
 * @see org.bgee.model.dao.api.ontologycommon.RelationDAO.RelationTO
 */
//TODO: add a boolean to the methods, to define whether to retrieve relations valid 
//in any requested species, or in all requested species.
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
    public RelationTOResultSet getAnatEntityRelations(Collection<String> speciesIds, Boolean anySpecies, 
            Collection<String> sourceAnatEntityIds, Collection<String> targetAnatEntityIds, Boolean sourceOrTarget, 
            Collection<RelationType> relationTypes, Collection<RelationStatus> relationStatus, 
            Collection<RelationDAO.Attribute> attributes) {
        log.entry(speciesIds, anySpecies, sourceAnatEntityIds, targetAnatEntityIds, sourceOrTarget, 
                relationTypes, relationStatus, attributes);
        
        String tableName = "anatEntityRelation";
        
        //*******************************
        // FILTER ARGUMENTS
        //*******************************
        //Species
        Set<String> clonedSpeIds = Optional.ofNullable(speciesIds)
                .map(c -> new HashSet<String>(c)).orElse(null);
        boolean isSpeciesFilter = clonedSpeIds != null && !clonedSpeIds.isEmpty();
        boolean realAnySpecies = isSpeciesFilter && 
                (Boolean.TRUE.equals(anySpecies) || clonedSpeIds.size() == 1);
        
        //Sources and targets
        Set<String> clonedSourceFilter = Optional.ofNullable(sourceAnatEntityIds)
                .map(c -> new HashSet<String>(c)).orElse(null);
        boolean isSourceAnatEntityFilter = clonedSourceFilter != null && !clonedSourceFilter.isEmpty();
        Set<String> clonedTargetFilter = Optional.ofNullable(targetAnatEntityIds)
                .map(c -> new HashSet<String>(c)).orElse(null);
        boolean isTargetAnatEntityFilter = clonedTargetFilter != null && !clonedTargetFilter.isEmpty();
        boolean isAnatEntityFilter = isSourceAnatEntityFilter || isTargetAnatEntityFilter;
        
        //Relation types and status
        Set<RelationType> clonedRelTypes = Optional.ofNullable(relationTypes)
                .map(c -> c.isEmpty()? null: EnumSet.copyOf(c)).orElse(null);
        boolean isRelationTypeFilter = clonedRelTypes != null && !clonedRelTypes.isEmpty();
        Set<RelationStatus> clonedRelStatus = Optional.ofNullable(relationStatus)
                .map(c -> c.isEmpty()? null: EnumSet.copyOf(c)).orElse(null);
        boolean isRelationStatusFilter = clonedRelStatus != null && !clonedRelStatus.isEmpty();

        //*******************************
        // SELECT CLAUSE
        //*******************************
        String sql = null;
        EnumSet<RelationDAO.Attribute> clonedAttrs = Optional.ofNullable(attributes)
                .map(e -> e.isEmpty()? null: EnumSet.copyOf(e)).orElse(null);
        if (clonedAttrs == null || clonedAttrs.isEmpty()) {
            sql = "SELECT DISTINCT " + tableName + ".*";
        } else {
            for (RelationDAO.Attribute attribute: clonedAttrs) {
                if (StringUtils.isEmpty(sql)) {
                    sql = "SELECT DISTINCT ";
                } else {
                    sql += ", ";
                }
                sql += tableName + "." + this.attributeAnatEntityRelationToString(attribute);
            }
        }
        //*******************************
        // FROM CLAUSE
        //*******************************
        sql += " FROM " + tableName;

        if (realAnySpecies) {
            sql += " INNER JOIN anatEntityRelationTaxonConstraint ON (" +
                    "anatEntityRelationTaxonConstraint.anatEntityRelationId = "
                    + tableName + ".anatEntityRelationId)";
        }

        //*******************************
        // WHERE CLAUSE
        //*******************************
        if (isSpeciesFilter || isAnatEntityFilter || isRelationTypeFilter || isRelationStatusFilter) {
            sql += " WHERE ";
        }
        
        //Species
        if (isSpeciesFilter) {
            if  (realAnySpecies) {
                sql += "(anatEntityRelationTaxonConstraint.speciesId IS NULL " +
                        "OR anatEntityRelationTaxonConstraint.speciesId IN (" +
                        BgeePreparedStatement.generateParameterizedQueryString(
                                clonedSpeIds.size()) + ")) ";
            } else {
                String existsPart = "SELECT 1 FROM anatEntityRelationTaxonConstraint AS tc WHERE "
                        + "tc.anatEntityRelationId = " 
                        + tableName + ".anatEntityRelationId AND tc.speciesId ";
                sql += "(EXISTS(" + existsPart + " IS NULL) OR (" 
                        + clonedSpeIds.stream().map(id -> "EXISTS(" + existsPart + " = ?)")
                                             .collect(Collectors.joining(" AND "))
                        + ")) ";
            }
        }
        
        //Sources and targets
        if (isAnatEntityFilter) {
            if (isSpeciesFilter) {
                sql += "AND ";
            }
            sql += "(";
            if (isSourceAnatEntityFilter) {
                sql += "anatEntitySourceId IN (" 
                       + BgeePreparedStatement.generateParameterizedQueryString(clonedSourceFilter.size()) 
                       + ")";
            }
            if (isTargetAnatEntityFilter) {
                if (isSourceAnatEntityFilter) {
                    if (Boolean.TRUE.equals(sourceOrTarget)) {
                        sql += " OR ";
                    } else {
                        sql += " AND ";
                    }
                }
                sql += "anatEntityTargetId IN (" 
                        + BgeePreparedStatement.generateParameterizedQueryString(clonedTargetFilter.size()) 
                        + ")";
            }
            sql += ") ";
        }
        
        //Relation types and status
        if (isRelationTypeFilter) {
            if (isSpeciesFilter || isAnatEntityFilter) {
                sql += "AND ";
            }
            sql += "relationType IN (" + 
            BgeePreparedStatement.generateParameterizedQueryString(clonedRelTypes.size()) + ") ";
        }
        if (isRelationStatusFilter) {
            if (isSpeciesFilter || isAnatEntityFilter || isRelationTypeFilter) {
                sql += "AND ";
            }
            sql += "relationStatus IN (" + 
                    BgeePreparedStatement.generateParameterizedQueryString(clonedRelStatus.size()) + ") ";
        }

        //*******************************
        // PREPARE STATEMENT
        //*******************************
        //we don't use a try-with-resource, because we return a pointer to the results, 
        //not the actual results, so we should not close this BgeePreparedStatement.
        try {
            BgeePreparedStatement stmt = this.getManager().getConnection().prepareStatement(sql);
            int startIndex = 1;
            if (isSpeciesFilter) {
                stmt.setStringsToIntegers(startIndex, clonedSpeIds, true);
                startIndex += clonedSpeIds.size();
            }
            if (isSourceAnatEntityFilter) {
                stmt.setStrings(startIndex, clonedSourceFilter, true);
                startIndex += clonedSourceFilter.size();
            }
            if (isTargetAnatEntityFilter) {
                stmt.setStrings(startIndex, clonedTargetFilter, true);
                startIndex += clonedTargetFilter.size();
            }
            if (isRelationTypeFilter) {
                stmt.setEnumDAOFields(startIndex, clonedRelTypes, true);
                startIndex += clonedRelTypes.size();
            }
            if (isRelationStatusFilter) {
                stmt.setEnumDAOFields(startIndex, clonedRelStatus, true);
                startIndex += clonedRelStatus.size();
            }
            return log.exit(new MySQLRelationTOResultSet(stmt));
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
    }
    
    @Override
    public RelationTOResultSet getAnatEntityRelationsBySpeciesIds(Set<String> speciesIds, 
            Set<RelationType> relationTypes, Set<RelationStatus> relationStatus) {
        log.entry(speciesIds, relationTypes, relationStatus);    

        return log.exit(this.getAnatEntityRelations(speciesIds, true, null, null, true, 
                relationTypes, relationStatus, this.getAttributes()));
    }
     
    @Override
    public RelationTOResultSet getStageRelations(Set<String> speciesIds, Set<String> stageIds, 
            Set<RelationStatus> relationStatus) {
        //NOTE: there is no relation table for stages, as they are represented 
        //as a nested set model. So, this method will emulate the existence of such a table, 
        //so that retrieval of relations between stages will be consistent with retrieval 
        //of relations between anatomical entities.
        log.entry(speciesIds, stageIds, relationStatus);    

        boolean isSpeciesFilter = speciesIds != null && speciesIds.size() > 0;
        boolean isStageFilter = stageIds != null && stageIds.size() > 0;
        boolean isRelationStatusFilter = relationStatus != null && relationStatus.size() > 0;
        
        String sql = null;
        Collection<RelationDAO.Attribute> attributes = this.getAttributes();
        if (attributes == null || attributes.isEmpty()) {
            sql = "SELECT tempTable.*";
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
            "t3.stageId AS stageSourceId, " +
            "t1.stageId AS stageTargetId, " +
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
        
        if (isStageFilter || isRelationStatusFilter) {
            sql += " WHERE "; 
        }
        if (isStageFilter) {
            sql += "(stageSourceId IN (" + 
            BgeePreparedStatement.generateParameterizedQueryString(stageIds.size()) + 
            ") OR stageTargetId IN (" + 
            BgeePreparedStatement.generateParameterizedQueryString(stageIds.size()) + ")) ";
        }
        if (isRelationStatusFilter) {
            if (isStageFilter) {
                sql += " AND ";
            }
            sql += "relationStatus IN (" + 
            BgeePreparedStatement.generateParameterizedQueryString(relationStatus.size()) + ")";
        }

         //we don't use a try-with-resource, because we return a pointer to the results, 
         //not the actual results, so we should not close this BgeePreparedStatement.
         try {
             BgeePreparedStatement stmt = this.getManager().getConnection().prepareStatement(sql);
             int startIndex = 1;
             if (isSpeciesFilter) {
                 List<Integer> orderedSpeciesIds = speciesIds.stream()
                         .map(e -> e == null? null: Integer.parseInt(e))
                         .collect(Collectors.toList());
                 Collections.sort(orderedSpeciesIds);
                 stmt.setIntegers(startIndex, orderedSpeciesIds, false);
                 startIndex += orderedSpeciesIds.size();
                 //we set the species IDs twice, once for the parent stages, 
                 //once for the child stages
                 stmt.setIntegers(startIndex, orderedSpeciesIds, false);
                 startIndex += orderedSpeciesIds.size();
             }
             if (isStageFilter) {
                 stmt.setStrings(startIndex, stageIds, true);
                 startIndex += stageIds.size();
                 stmt.setStrings(startIndex, stageIds, true);
                 startIndex += stageIds.size();
             }
             if (isRelationStatusFilter) {
                 stmt.setEnumDAOFields(startIndex, relationStatus, true);
                 startIndex += relationStatus.size();
             }
             return log.exit(new MySQLRelationTOResultSet(stmt));
         } catch (SQLException e) {
             throw log.throwing(new DAOException(e));
         }
    }

    @Override
    public RelationTOResultSet getStageRelationsBySpeciesIds(Set<String> speciesIds, 
            Set<RelationStatus> relationStatus) {
        log.entry(speciesIds, relationStatus);
        return log.exit(this.getStageRelations(speciesIds, null, relationStatus));        
    }

    /** 
     * Returns a {@code String} that correspond to the given {@code RelationDAO.Attribute}, 
     * for retrieval of relations between anatomical entities.
     * 
     * @param attribute   A {code RelationDAO.Attribute} that is the attribute to
     *                    convert in a {@code String}.
     * @return            A {@code String} that correspond to the given 
     *                    {@code RelationDAO.Attribute}
     * @throws IllegalArgumentException If the {@code attribute} is unknown.
     */
    private String attributeAnatEntityRelationToString(RelationDAO.Attribute attribute) 
            throws IllegalArgumentException {
        log.entry(attribute);
        
        String label = null;
        if (attribute.equals(RelationDAO.Attribute.RELATION_ID)) {
                label = "anatEntityRelationId";
        } else if (attribute.equals(RelationDAO.Attribute.SOURCE_ID)) {
            label = "anatEntitySourceId";
        } else if (attribute.equals(RelationDAO.Attribute.TARGET_ID)) {
            label = "anatEntityTargetId";
        } else if (attribute.equals(RelationDAO.Attribute.RELATION_TYPE)) {
            label = "relationType";
        } else if (attribute.equals(RelationDAO.Attribute.RELATION_STATUS)) {
            label = "relationStatus";
        } else {
            throw log.throwing(new IllegalArgumentException("The attribute provided (" +
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
     * @throws IllegalArgumentException If the {@code attribute} is unknown.
     */
    private String attributeStageRelationToString(RelationDAO.Attribute attribute) 
            throws IllegalArgumentException {
        log.entry(attribute);
        
        String label = null;
        if (attribute.equals(RelationDAO.Attribute.RELATION_ID)) {
                label = "stageRelationId";
        } else if (attribute.equals(RelationDAO.Attribute.SOURCE_ID)) {
            label = "stageSourceId";
        } else if (attribute.equals(RelationDAO.Attribute.TARGET_ID)) {
            label = "stageTargetId";
        } else if (attribute.equals(RelationDAO.Attribute.RELATION_TYPE)) {
            label = "relationType";
        } else if (attribute.equals(RelationDAO.Attribute.RELATION_STATUS)) {
            label = "relationStatus";
        } else {
            throw log.throwing(new IllegalArgumentException("The attribute provided (" +
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
        private MySQLRelationTOResultSet(BgeePreparedStatement statement) {
            super(statement);
        }

        @Override
        protected RelationTO getNewTO() throws DAOException {
            log.entry();
            String relationId = null, sourceId = null, targetId =null;
            RelationType relationType = null;
            RelationStatus relationStatus = null;
            
            for (Entry<Integer, String> column: this.getColumnLabels().entrySet()) {
                try {
                    if (column.getValue().equals("anatEntityRelationId")) {
                        relationId = this.getCurrentResultSet().getString(column.getKey());
                    } else if (column.getValue().equals("stageRelationId")) {
                        //XXX: for now, we don't generate any stageRelationId (always set to 0), 
                        //so we don't retrieve it. If we needed stageRelationId to be set, 
                        //we would need to edit the query.
                        //relationId = this.getCurrentResultSet().getString(column.getKey());
                    } else if (column.getValue().equals("anatEntitySourceId") || 
                            column.getValue().equals("goAllSourceId") || 
                            column.getValue().equals("stageSourceId")) {
                        sourceId = this.getCurrentResultSet().getString(column.getKey());
                    } else if (column.getValue().equals("anatEntityTargetId") || 
                            column.getValue().equals("goAllTargetId") || 
                            column.getValue().equals("stageTargetId") ) {
                        targetId = this.getCurrentResultSet().getString(column.getKey());
                    } else if (column.getValue().equals("relationStatus")) {
                        relationStatus = RelationStatus.convertToRelationStatus(
                                this.getCurrentResultSet().getString(column.getKey()));
                    } else if (column.getValue().equals("relationType")) {
                        relationType = RelationType.convertToRelationType(
                                this.getCurrentResultSet().getString(column.getKey()));
                    } else {
                        throw log.throwing(new UnrecognizedColumnException(column.getValue()));
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
