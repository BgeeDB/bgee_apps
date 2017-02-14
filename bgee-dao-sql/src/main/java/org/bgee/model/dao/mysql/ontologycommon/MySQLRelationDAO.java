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
    public RelationTOResultSet<String> getAnatEntityRelations(Collection<String> speciesIds, Boolean anySpecies, 
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
                sql += getAllSpeciesExistsClause(existsPart, clonedSpeIds.size());
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
            return log.exit(new MySQLRelationTOResultSet<String>(stmt, String.class));
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
    }
    
    @Override
    public RelationTOResultSet<String> getAnatEntityRelationsBySpeciesIds(Set<String> speciesIds, 
            Set<RelationType> relationTypes, Set<RelationStatus> relationStatus) {
        log.entry(speciesIds, relationTypes, relationStatus);    

        return log.exit(this.getAnatEntityRelations(speciesIds, true, null, null, true, 
                relationTypes, relationStatus, this.getAttributes()));
    }
    
    @Override
    public RelationTOResultSet<Integer> getTaxonRelations(Collection<Integer> sourceTaxIds, 
            Collection<Integer> targetTaxIds, Boolean sourceOrTarget,
            Collection<RelationStatus> relationStatus, Collection<RelationDAO.Attribute> attributes) {
        log.entry(sourceTaxIds, targetTaxIds, sourceOrTarget, relationStatus, attributes);
        return log.exit(this.getNestedSetModelFakeRelations(null, null, 
                sourceTaxIds, targetTaxIds, sourceOrTarget, relationStatus, attributes, 
                "taxon", null, "taxonId", "taxonLeftBound", "taxonRightBound", "taxonLevel", 
                Integer.class));
    }

    @Override
    public RelationTOResultSet<String> getStageRelationsBySpeciesIds(Set<String> speciesIds, 
            Set<RelationStatus> relationStatus) {
        log.entry(speciesIds, relationStatus);
        return log.exit(this.getStageRelations(speciesIds, true, null, null, true, relationStatus, 
                this.getAttributes()));        
    }

    @Override
    public RelationTOResultSet<String> getStageRelations(Collection<String> speciesIds, Boolean anySpecies, 
            Collection<String> sourceDevStageIds, Collection<String> targetDevStageIds, Boolean sourceOrTarget, 
            Collection<RelationStatus> relationStatus, 
            Collection<RelationDAO.Attribute> attributes) {
        log.entry(speciesIds, anySpecies, sourceDevStageIds, targetDevStageIds, sourceOrTarget,
                relationStatus, attributes);
        return log.exit(this.getNestedSetModelFakeRelations(speciesIds, anySpecies, 
                sourceDevStageIds, targetDevStageIds, sourceOrTarget, relationStatus, attributes, 
                "stage", "stageTaxonConstraint", "stageId", "stageLeftBound", "stageRightBound", "stageLevel", 
                String.class));
    }
    
    /**
     * Retrieve {@code RelationTO}s for any nested set model stored in the database.
     *  
     * @param speciesIds            A {@code Collection} of {@code String}s that are the IDs of species 
     *                              to retrieve relations for. Can be {@code null} or empty. 
     *                              Only applicable for multiple-species ontologies.
     * @param anySpecies            A {@code Boolean} defining, when {@code speciesIds} contains several IDs, 
     *                              whether the relations retrieved should be valid in any 
     *                              of the requested species (if {@code true}), or in all 
     *                              of the requested species (if {@code false} or {@code null}). 
     *                              Only applicable for multiple-species ontologies.
     * @param sourceIds             A {@code Collection} of {@code T}s that are the IDs of entities
     *                              that should be the sources of the retrieved relations. 
     *                              Can be {@code null} or empty.
     * @param targetIds             A {@code Collection} of {@code T}s that are the IDs of entities
     *                              that should be the targets of the retrieved relations. 
     *                              Can be {@code null} or empty.
     * @param sourceOrTarget        A {@code Boolean} defining, when both {@code sourceIds} 
     *                              and {@code targetIds} are not empty, 
     *                              whether the relations retrieved should have one of {@code sourceIds} 
     *                              as source <strong>and/or</strong> one of {@code targetIds} as target 
     *                              (if {@code true}), or, one of {@code sourceIds} 
     *                              as source <strong>and</strong> one of {@code targetIds} as target 
     *                              (if {@code false} or {@code null}).
     * @param relationStatus        A {@code Collection} of {@code RelationStatus} that are the status
     *                              allowing to filter the relations to retrieve.
     *                              Can be {@code null} or empty.
     * @param attributes            A {@code Collection} of {@code RelationDAO.Attribute}s 
     *                              defining the attributes to populate in the returned 
     *                              {@code RelationTO}s. If {@code null} or empty, 
     *                              all attributes are populated. 
     * @param tableName             A {@code String} that is the name of the main table 
     *                              storing the entities, with their ID, left and right bounds. 
     *                              Example: "stage".
     * @param taxonConstTableName   A {@code String} that is the name of the table containing 
     *                              taxon constraints. Only applicable for multiple-species ontologies.
     *                              Example: "stageTaxonConstraint".
     * @param idFieldName           A {@code String} that is the name of the ID field in main table.
     *                              Example: "stageId".
     * @param leftBoundFieldName    A {@code String} that is the name of the left bound field in main table.
     *                              Example: "stageLeftBound".
     * @param rightBoundFieldName   A {@code String} that is the name of the right bound field in main table.
     *                              Example: "stageRightBound".
     * @param levelFieldName        A {@code String} that is the name of the level field in main table.
     *                              Example: "stageLevel".
     * @param cls                   The {@code Class} corresponding to the generic type {@code <T>}
     * @param <T>                   The type of IDs of source and target in {@code RelationTO}s.
     * @return                      A {@code RelationTOResultSet} allowing to retrieve 
     *                              nested set model relations from data source.
     * @throws DAOException If an error occurred when accessing the data source. 
     */
    private <T> RelationTOResultSet<T> getNestedSetModelFakeRelations(Collection<String> speciesIds, 
            Boolean anySpecies, Collection<T> sourceIds, Collection<T> targetIds, 
            Boolean sourceOrTarget, Collection<RelationStatus> relationStatus, 
            Collection<RelationDAO.Attribute> attributes, String tableName, String taxonConstTableName, 
            String idFieldName, String leftBoundFieldName, String rightBoundFieldName, String levelFieldName, 
            Class<T> cls) {
        log.entry(speciesIds, anySpecies, sourceIds, targetIds, sourceOrTarget, 
                relationStatus, attributes, tableName, taxonConstTableName, idFieldName, 
                leftBoundFieldName, rightBoundFieldName, levelFieldName, cls);
        //NOTE: for nested set models there is no relation table, as for, e.g., anatomical entities. 
        //So, this method will emulate the existence of such a table, 
        //so that retrieval of relations in nested set models is consistent with retrieval 
        //of relations between anatomical entities.
        //*******************************
        // FILTER ARGUMENTS
        //*******************************
        //Species
        Set<String> clonedSpeIds = Optional.ofNullable(speciesIds)
                .map(c -> new HashSet<String>(c)).orElse(null);
        boolean isSpeciesFilter = clonedSpeIds != null && !clonedSpeIds.isEmpty();
        assert !isSpeciesFilter || taxonConstTableName != null; 
        boolean realAnySpecies = isSpeciesFilter && 
                (Boolean.TRUE.equals(anySpecies) || clonedSpeIds.size() == 1);
        
        //Sources and targets
        Set<T> clonedSourceFilter = Optional.ofNullable(sourceIds)
                .map(c -> new HashSet<>(c)).orElse(null);
        boolean isSourceFilter = clonedSourceFilter != null && !clonedSourceFilter.isEmpty();
        Set<T> clonedTargetFilter = Optional.ofNullable(targetIds)
                .map(c -> new HashSet<>(c)).orElse(null);
        boolean isTargetFilter = clonedTargetFilter != null && !clonedTargetFilter.isEmpty();
        boolean isEntityFilter = isSourceFilter || isTargetFilter;
        
        //Relation status
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
            sql = "SELECT tempTable.*";
        } else {
            for (RelationDAO.Attribute attribute: clonedAttrs) {
                if (sql == null) {
                    sql = "SELECT DISTINCT ";
                } else {
                    sql += ", ";
                }
                sql += this.attributeNestedSetModelRelationToString(attribute);
            }
        }
        
        //*******************************
        // FROM CLAUSE
        //*******************************
        sql += " FROM ";
        
        //OK, we create a query that will emulate a temporary table similar to
        //the retrieval of relations between anatomical entities.
        sql += 
            // no relationId, provide 0 for all
            "(SELECT DISTINCT 0 AS relationId, " +
            "t3." + idFieldName + " AS sourceId, " +
            "t1." + idFieldName + " AS targetId, " +
            //no other parenthood relations in nested set models other than is_a
            "'" + RelationType.ISA_PARTOF.getStringRepresentation() + "' AS relationType, " +
            //emulate RelationStatus
            "IF (t1." + idFieldName + " = t3." + idFieldName + ", " + 
                "'" + RelationStatus.REFLEXIVE.getStringRepresentation() + "', " +
                "IF (t3." + levelFieldName + " = t1." + levelFieldName + " + 1, " + 
                "'" + RelationStatus.DIRECT.getStringRepresentation() + "', " +
                "'" + RelationStatus.INDIRECT.getStringRepresentation() + "')) AS relationStatus " +
            "FROM " + tableName + " AS t1 " + 
            "INNER JOIN " + tableName + " AS t3 " +
            "ON t3." + leftBoundFieldName + " >= t1." + leftBoundFieldName + 
           " AND t3." + rightBoundFieldName + " <= t1." + rightBoundFieldName;
        if (taxonConstTableName != null) {
            sql += " INNER JOIN " + taxonConstTableName + " AS t2 " +
                    "ON t1." + idFieldName + " = t2." + idFieldName + 
                   " INNER JOIN " + taxonConstTableName + " AS t4 " +
                    "ON t3." + idFieldName + " = t4." + idFieldName + " AND " +
                    "(t2.speciesId IS NULL OR t4.speciesId IS NULL OR t4.speciesId = t2.speciesId) ";
        }
        if (isSpeciesFilter) {
            sql += "WHERE ";
            if  (realAnySpecies) {
                //a case is not covered in this where clause: for instance, if we query relations 
                //for species 1 or species 2, while entity 1 exists in species 1, and entity 2 
                //in species 2. With only this where clause, we could retrieve 
                //an incorrect relation between entity 1 and entity 2. But this is not possible 
                //thanks to the join clause above between t4 and t2. 
                sql += "(t2.speciesId IS NULL OR t2.speciesId IN (" +
                            BgeePreparedStatement.generateParameterizedQueryString(
                                clonedSpeIds.size()) + ")) " +
                        "AND (t4.speciesId IS NULL OR t4.speciesId IN (" +
                             BgeePreparedStatement.generateParameterizedQueryString(
                                clonedSpeIds.size()) + ")) ";
            } else {
                String existsPart = "SELECT 1 FROM " + taxonConstTableName + " AS tc WHERE "
                        + "tc." + idFieldName + " = t1." + idFieldName + " AND tc.speciesId ";
                sql += getAllSpeciesExistsClause(existsPart, clonedSpeIds.size());
                existsPart = "SELECT 1 FROM " + taxonConstTableName + " AS tc WHERE "
                        + "tc." + idFieldName + " = t3." + idFieldName + " AND tc.speciesId ";
                sql += "AND " + getAllSpeciesExistsClause(existsPart, clonedSpeIds.size());
            }
        }
        sql += ") AS tempTable ";

        //*******************************
        // WHERE CLAUSE (species already filtered in FROM clause subquery)
        //*******************************
        if (isEntityFilter || isRelationStatusFilter) {
            sql += " WHERE "; 
        }
        if (isEntityFilter) {
            
            sql += "(";
            if (isSourceFilter) {
                sql += "sourceId IN (" 
                       + BgeePreparedStatement.generateParameterizedQueryString(clonedSourceFilter.size()) 
                       + ")";
            }
            if (isTargetFilter) {
                if (isSourceFilter) {
                    if (Boolean.TRUE.equals(sourceOrTarget)) {
                        sql += " OR ";
                    } else {
                        sql += " AND ";
                    }
                }
                sql += "targetId IN (" 
                        + BgeePreparedStatement.generateParameterizedQueryString(clonedTargetFilter.size()) 
                        + ")";
            }
            sql += ") ";
        }
        
        if (isRelationStatusFilter) {
            if (isEntityFilter) {
                sql += " AND ";
            }
            sql += "relationStatus IN (" + 
            BgeePreparedStatement.generateParameterizedQueryString(clonedRelStatus.size()) + ")";
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
                 List<Integer> orderedSpeciesIds = clonedSpeIds.stream()
                         .map(e -> e == null? null: Integer.parseInt(e))
                         .collect(Collectors.toList());
                 Collections.sort(orderedSpeciesIds);
                 stmt.setIntegers(startIndex, orderedSpeciesIds, false);
                 startIndex += orderedSpeciesIds.size();
                 //we set the species IDs twice, once for the parents, 
                 //once for the children
                 stmt.setIntegers(startIndex, orderedSpeciesIds, false);
                 startIndex += orderedSpeciesIds.size();
             }
             if (isSourceFilter) {
                 stmt.setObjects(startIndex, clonedSourceFilter, true, cls);
                 startIndex += clonedSourceFilter.size();
             }
             if (isTargetFilter) {
                 stmt.setObjects(startIndex, clonedSourceFilter, true, cls);
                 startIndex += clonedTargetFilter.size();
             }
             if (isRelationStatusFilter) {
                 stmt.setEnumDAOFields(startIndex, clonedRelStatus, true);
                 startIndex += clonedRelStatus.size();
             }
             return log.exit(new MySQLRelationTOResultSet<T>(stmt, cls));
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
     * for retrieval of relations between entities in a nested set model. Note that this does not correspond 
     * to attributes of an actual table, but an emulated temporary table to use 
     * queries consistent with retrieval of relations between anatomical entities. 
     * 
     * @param attribute   A {code RelationDAO.Attribute} that is the attribute to
     *                    convert in a {@code String}.
     * @return            A {@code String} that correspond to the given 
     *                    {@code RelationDAO.Attribute}
     * @throws IllegalArgumentException If the {@code attribute} is unknown.
     */
    private String attributeNestedSetModelRelationToString(RelationDAO.Attribute attribute) 
            throws IllegalArgumentException {
        log.entry(attribute);
        
        String label = null;
        if (attribute.equals(RelationDAO.Attribute.RELATION_ID)) {
                label = "relationId";
        } else if (attribute.equals(RelationDAO.Attribute.SOURCE_ID)) {
            label = "sourceId";
        } else if (attribute.equals(RelationDAO.Attribute.TARGET_ID)) {
            label = "targetId";
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
    public int insertAnatEntityRelations(Collection<RelationTO<String>> relations) 
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
            for (RelationTO<String> relation: relations) {
                stmt.setInt(1, relation.getId());
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
    public int insertGeneOntologyRelations(Collection<RelationTO<String>> relations) 
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
            
            for (RelationTO<String> rel: relations) {
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
     * 
     * @param <T> the type of target and source IDs in related {@code RelationTO}s.
     */
    public class MySQLRelationTOResultSet<T> extends MySQLDAOResultSet<RelationTO<T>> 
                                          implements RelationTOResultSet<T> {
        
        /**
         * The {@code Class} corresponding to the generic type {@code <T>}.
         */
        private final Class<T> cls;
        /**
         * Delegates to {@link MySQLDAOResultSet#MySQLDAOResultSet(BgeePreparedStatement)}
         * super constructor.
         * 
         * @param   statement The first {@code BgeePreparedStatement} to execute a query on.
         * @param cls     The {@code Class} corresponding to the generic type {@code <T>}.
         */
        private MySQLRelationTOResultSet(BgeePreparedStatement statement, Class<T> cls) {
            super(statement);
            this.cls = cls;
        }

        @Override
        protected RelationTO<T> getNewTO() throws DAOException {
            log.entry();
            Integer relationId = null;
            T sourceId = null, targetId =null;
            RelationType relationType = null;
            RelationStatus relationStatus = null;
            
            for (Entry<Integer, String> column: this.getColumnLabels().entrySet()) {
                try {
                    if (column.getValue().equals("anatEntityRelationId")) {
                        relationId = this.getCurrentResultSet().getInt(column.getKey());
                    } else if (column.getValue().equals("relationId")) {
                        //XXX: for now, we don't generate any relationId for nested set model (always set to 0), 
                        //so we don't retrieve it. If we needed relationId to be set, 
                        //we would need to edit the query.
                        //relationId = this.getCurrentResultSet().getInt(column.getKey());
                    } else if (column.getValue().equals("anatEntitySourceId") || 
                            column.getValue().equals("goAllSourceId") || 
                            column.getValue().equals("sourceId")) {
                        sourceId = this.getCurrentResultSet().getObject(column.getKey(), this.cls);
                    } else if (column.getValue().equals("anatEntityTargetId") || 
                            column.getValue().equals("goAllTargetId") || 
                            column.getValue().equals("targetId") ) {
                        targetId = this.getCurrentResultSet().getObject(column.getKey(), this.cls);
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
            return log.exit(new RelationTO<T>(relationId, sourceId, targetId, 
                    relationType, relationStatus));
        }
    }
}
