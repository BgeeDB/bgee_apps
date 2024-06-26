package org.bgee.model.dao.mysql.anatdev;

import java.sql.SQLException;
import java.sql.Types;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.anatdev.TaxonConstraintDAO;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.mysql.MySQLDAO;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.model.dao.mysql.connector.MySQLDAOResultSet;
import org.bgee.model.dao.mysql.exception.UnrecognizedColumnException;


/**
 * A {@code TaxonConstraintDAO} for MySQL. 
 * 
 * @author  Valentine Rech de Laval
 * @author Frederic Bastian
 * @version Bgee 14 Nov. 2017
 * @see     org.bgee.model.dao.api.anatdev.TaxonConstraintDAO.TaxonConstraintTO
 * @since   Bgee 13
 */
public class MySQLTaxonConstraintDAO extends MySQLDAO<TaxonConstraintDAO.Attribute> 
                                     implements TaxonConstraintDAO {

    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = LogManager.getLogger(MySQLTaxonConstraintDAO.class.getName());

    /**
     * Constructor providing the {@code MySQLDAOManager} that this {@code MySQLDAO} 
     * will use to obtain {@code BgeeConnection}s.
     * 
     * @param manager                       The {@code MySQLDAOManager} to use.
     * @throws IllegalArgumentException     If {@code manager} is {@code null}.
     */
    public MySQLTaxonConstraintDAO(MySQLDAOManager manager) throws IllegalArgumentException {
        super(manager);
    }

    @Override
    public TaxonConstraintTOResultSet<String> getAnatEntityTaxonConstraints(
            Collection<Integer> speciesIds, Collection<TaxonConstraintDAO.Attribute> attributes)
            throws DAOException {
        log.traceEntry("{}, {}", speciesIds, attributes);

        return log.traceExit(this.getTaxonConstraints(
                speciesIds, null, attributes, "anatEntityTaxonConstraint", "anatEntityId", String.class));
    }
    @Override
    public TaxonConstraintTOResultSet<String> getAnatEntityTaxonConstraints(
            Collection<Integer> speciesIds, Collection<String> anatEntityIds,
            Collection<TaxonConstraintDAO.Attribute> attributes) throws DAOException {
        log.traceEntry("{}, {}, {}", speciesIds, anatEntityIds, attributes);

        return log.traceExit(this.getTaxonConstraints(
                speciesIds != null? new HashSet<>(speciesIds): null,
                anatEntityIds != null? new HashSet<>(anatEntityIds): null,
                attributes, "anatEntityTaxonConstraint", "anatEntityId", String.class));
    }

    @Override
    /*
     * (non-javadoc)
     * That method is not factorize with other select method because of table names, 
     * entity ID column names, and, most important of all, types of this column 
     * (int or string) are different.
     */
    public TaxonConstraintTOResultSet<Integer> getAnatEntityRelationTaxonConstraints(
            Collection<Integer> speciesIds, Collection<TaxonConstraintDAO.Attribute> attributes)
            throws DAOException {
        log.traceEntry("{}, {}", speciesIds, attributes);
        return log.traceExit(this.getAnatEntityRelationTaxonConstraints(speciesIds, null, attributes));
    }
    @Override
    /*
     * (non-javadoc)
     * That method is not factorize with other select method because of table names,
     * entity ID column names, and, most important of all, types of this column
     * (int or string) are different.
     */
    public TaxonConstraintTOResultSet<Integer> getAnatEntityRelationTaxonConstraints(
            Collection<Integer> speciesIds, Collection<Integer> relIds,
            Collection<TaxonConstraintDAO.Attribute> attributes) throws DAOException {
        log.traceEntry("{}, {}, {}", speciesIds, relIds, attributes);

        Set<TaxonConstraintDAO.Attribute> clonedAttrs = attributes == null || attributes.isEmpty()?
                EnumSet.allOf(TaxonConstraintDAO.Attribute.class): EnumSet.copyOf(attributes);
        Set<Integer> clonedSpeciesIds = speciesIds == null? new HashSet<Integer>(): new HashSet<Integer>(speciesIds);
        Set<Integer> clonedRelIds     = relIds == null? new HashSet<Integer>(): new HashSet<Integer>(relIds);

        String tableName = "anatEntityRelationTaxonConstraint";

        String sql = "";
        for (TaxonConstraintDAO.Attribute attribute: clonedAttrs) {
            if (sql.isEmpty()) {
                sql += "SELECT DISTINCT ";
            } else {
                sql += ", ";
            }
            sql += tableName + ".";
            if (attribute.equals(TaxonConstraintDAO.Attribute.ENTITY_ID)) {
                sql += "anatEntityRelationId";
            } else if (attribute.equals(TaxonConstraintDAO.Attribute.SPECIES_ID)) {
                sql += "speciesId";
            } else {
                throw log.throwing(new IllegalArgumentException("The attribute provided (" + 
                        attribute.toString() + ") is unknown for " + TaxonConstraintDAO.class.getName()));
            }
        }

        sql += " FROM " + tableName;
        
        if (!clonedSpeciesIds.isEmpty() || !clonedRelIds.isEmpty()) {
            sql += " WHERE ";
            if (!clonedSpeciesIds.isEmpty()) {
                sql += "(" + tableName + ".speciesId IS NULL OR " + tableName + ".speciesId IN (" +
                        BgeePreparedStatement.generateParameterizedQueryString(clonedSpeciesIds.size()) + ")) ";
            }
            if (!clonedRelIds.isEmpty()) {
                if (!clonedSpeciesIds.isEmpty()) {
                    sql += " AND ";
                }
                sql += tableName + ".anatEntityRelationId IN ("
                    + BgeePreparedStatement.generateParameterizedQueryString(clonedRelIds.size()) + ")";
            }
        }

        // we don't use a try-with-resource, because we return a pointer to the results,
        // not the actual results, so we should not close this BgeePreparedStatement.
        try {
            BgeePreparedStatement stmt = this.getManager().getConnection().prepareStatement(sql);
            int i = 1;
            if (!clonedSpeciesIds.isEmpty()) {
                stmt.setIntegers(i, clonedSpeciesIds, true);
                i += clonedSpeciesIds.size();
            }
            if (!clonedRelIds.isEmpty()) {
                stmt.setIntegers(i, clonedRelIds, true);
                i += clonedRelIds.size();
            }
            return log.traceExit(new MySQLTaxonConstraintTOResultSet<>(stmt, Integer.class));
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
    }

    @Override
    public TaxonConstraintTOResultSet<String> getStageTaxonConstraints(
            Collection<Integer> speciesIds, Collection<TaxonConstraintDAO.Attribute> attributes)
            throws DAOException {
        log.traceEntry("{}, {}", speciesIds, attributes);
        return log.traceExit(this.getTaxonConstraints(
                speciesIds, null, attributes, "stageTaxonConstraint", "stageId", String.class));
    }

    /** 
     * Retrieve taxon constrains from data source in {@code tableName}.
     * The constrains can be filtered by species IDs.
     * <p>
     * The taxon constrains are retrieved and returned as a {@code TaxonConstraintTOResultSet}.
     * It is the responsibility of the caller to close this {@code DAOResultSet}
     * once results are retrieved.
     * 
     * @param speciesIds        A {@code Collection} of {@code Integer}s that are the IDs of species 
     *                          to retrieve taxon constrains for.
     * @param entityIds         A {@code Collection} of {@code T}s that are the IDs of the entities 
     *                          to retrieve taxon constrains for.
     * @param attributes        A {@code Collection} of {@code TaxonConstraintDAO.Attribute}s defining  
     *                          the attributes to populate in the returned {@code TaxonConstraintTO}s.
     *                          If {@code null} or empty, all attributes are populated. 
     * @param tableName         A {@code String} that is the name of the table to be used.
     * @param entityColumnName  A {@code String} that is the name of entity ID column.
     * @param cls               A {@code Class} representing the type of ID of related entities.
     * @return                  A {@code TaxonConstraintTOResultSet} allowing to retrieve 
     *                          taxon constrains from data source.
     * @throws DAOException     If an error occurred when accessing the data source. 
     * 
     * @param <T> the type of ID of the related entity.
     */
    private <T> TaxonConstraintTOResultSet<T> getTaxonConstraints(Collection<Integer> speciesIds,
            Collection<T> entityIds, Collection<TaxonConstraintDAO.Attribute> attributes,
            String tableName, String entityColumnName, Class<T> cls) throws DAOException {
        log.traceEntry("{}, {}, {}, {}, {}, {}", speciesIds, entityIds, attributes, tableName,
                entityColumnName, cls);
        
        boolean filterBySpeciesIds = speciesIds != null && !speciesIds.isEmpty();
        boolean filterByEntityIds = entityIds != null && !entityIds.isEmpty();

        String sql = "";
        if (attributes == null || attributes.isEmpty()) {
            attributes = EnumSet.allOf(TaxonConstraintDAO.Attribute.class);
        }
        for (TaxonConstraintDAO.Attribute attribute: attributes) {
            if (sql.isEmpty()) {
                sql += "SELECT DISTINCT ";
            } else {
                sql += ", ";
            }
            sql += tableName + ".";
            if (attribute.equals(TaxonConstraintDAO.Attribute.ENTITY_ID)) {
                sql += entityColumnName;
            } else if (attribute.equals(TaxonConstraintDAO.Attribute.SPECIES_ID)) {
                sql += "speciesId";
            } else {
                throw log.throwing(new IllegalArgumentException("The attribute provided (" + 
                        attribute.toString() + ") is unknown for " + TaxonConstraintDAO.class.getName()));
            }
        }

        sql += " FROM " + tableName;
        if (filterBySpeciesIds || filterByEntityIds) {
            sql += " WHERE ";
        }
        
        if (filterBySpeciesIds) {
            sql += " (" + tableName + ".speciesId IS NULL OR " + tableName + ".speciesId IN (" + 
                    BgeePreparedStatement.generateParameterizedQueryString(speciesIds.size()) + "))";            
        }
        if (filterByEntityIds) {
            if (filterBySpeciesIds) {
                sql += " AND ";
            }
            sql += tableName + "." + entityColumnName + " IN ("
                + BgeePreparedStatement.generateParameterizedQueryString(entityIds.size()) + ")";
        }

        // we don't use a try-with-resource, because we return a pointer to the results,
        // not the actual results, so we should not close this BgeePreparedStatement.
        try {
            BgeePreparedStatement stmt = this.getManager().getConnection().prepareStatement(sql);
            int paramIndex = 1;
            if (filterBySpeciesIds) {
                stmt.setIntegers(paramIndex, speciesIds, true);
                paramIndex += speciesIds.size();
            }
            if (filterByEntityIds) {
                stmt.setObjects(paramIndex, entityIds, true, cls);
                paramIndex += entityIds.size();
            }
            return log.traceExit(new MySQLTaxonConstraintTOResultSet<>(stmt, cls));
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
    }

    @Override
    /*
     * (non-javadoc)
     * All the insert methods of that class are not factorize in a single method because of 
     * table names, entity ID column names, and, most important of all, types of this column 
     * (int or string) are different.
     */
    public int insertAnatEntityRelationTaxonConstraints(Collection<TaxonConstraintTO<Integer>> contraints)
                    throws DAOException, IllegalArgumentException {
        log.traceEntry("{}", contraints);

        if (contraints == null || contraints.isEmpty()) {
            throw log.throwing(new IllegalArgumentException(
                    "No anatomical entity relation taxon constraint is given, " +
                    "then no constraint is inserted"));
        }

        String sqlExpression = "INSERT INTO anatEntityRelationTaxonConstraint " +
                                            "(anatEntityRelationId, speciesId) VALUES (?, ?)";
        
        // To not overload MySQL with an error com.mysql.jdbc.PacketTooBigException, 
        // and because of laziness, we insert expression calls one at a time
        int contraintInsertedCount = 0;
        try (BgeePreparedStatement stmt = 
                this.getManager().getConnection().prepareStatement(sqlExpression)) {
            for (TaxonConstraintTO<Integer> contraint: contraints) {
                stmt.setInt(1, contraint.getEntityId());
                if (contraint.getSpeciesId() == null) {
                    stmt.setNull(2, Types.INTEGER);
                } else {
                    stmt.setInt(2, contraint.getSpeciesId());
                }
                contraintInsertedCount += stmt.executeUpdate();
                stmt.clearParameters();
            }
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }

        return log.traceExit(contraintInsertedCount);
    }

    @Override
    /*
     * (non-javadoc)
     * All the insert methods of that class are not factorize in a single method because of 
     * table names, entity ID column names, and, most important of all, types of this column 
     * (int or string) are different.
     */
    public int insertAnatEntityTaxonConstraints(Collection<TaxonConstraintTO<String>> contraints)
            throws DAOException, IllegalArgumentException {
        log.traceEntry("{}", contraints);

        if (contraints == null || contraints.isEmpty()) {
            throw log.throwing(new IllegalArgumentException(
                    "No anatomical entity taxon constraint is given, " +
                    "then no constraint is inserted"));
        }

        String sqlExpression = "INSERT INTO anatEntityTaxonConstraint (anatEntityId, speciesId) " +
                               "VALUES (?, ?)";
        
        // To not overload MySQL with an error com.mysql.jdbc.PacketTooBigException, 
        // and because of laziness, we insert expression calls one at a time
        int contraintInsertedCount = 0;
        try (BgeePreparedStatement stmt = 
                this.getManager().getConnection().prepareStatement(sqlExpression)) {
            for (TaxonConstraintTO<String> contraint: contraints) {
                stmt.setString(1, contraint.getEntityId());
                if (contraint.getSpeciesId() == null) {
                    stmt.setNull(2, Types.INTEGER);
                } else {
                    stmt.setInt(2, contraint.getSpeciesId());
                }
                contraintInsertedCount += stmt.executeUpdate();
                stmt.clearParameters();
            }
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }

        return log.traceExit(contraintInsertedCount);
    }

    @Override
    /*
     * (non-javadoc)
     * All the insert methods of that class are not factorize in a single method because of 
     * table names, entity ID column names, and, most important of all, types of this column 
     * (int or string) are different.
     */
    public int insertStageTaxonConstraints(Collection<TaxonConstraintTO<String>> contraints)
            throws DAOException, IllegalArgumentException {
        log.traceEntry("{}", contraints);

        if (contraints == null || contraints.isEmpty()) {
            throw log.throwing(new IllegalArgumentException(
                    "No stage taxon constraint is given, then no constraint is inserted"));
        }

        String sqlExpression = "INSERT INTO stageTaxonConstraint (stageId, speciesId) " +
                               "VALUES (?, ?)";
        
        // To not overload MySQL with an error com.mysql.jdbc.PacketTooBigException, 
        // and because of laziness, we insert expression calls one at a time
        int contraintInsertedCount = 0;
        try (BgeePreparedStatement stmt = 
                this.getManager().getConnection().prepareStatement(sqlExpression)) {
            for (TaxonConstraintTO<String> contraint: contraints) {
                stmt.setString(1, contraint.getEntityId());
                if (contraint.getSpeciesId() == null) {
                    stmt.setNull(2, Types.INTEGER);
                } else {
                    stmt.setInt(2, contraint.getSpeciesId());
                }

                contraintInsertedCount += stmt.executeUpdate();
                stmt.clearParameters();
            }
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }

        return log.traceExit(contraintInsertedCount);
    }
    
    /**
     * A {@code MySQLDAOResultSet} specific to {@code MySQLTaxonConstraintTO}.
     * 
     * @author Valentine Rech de Laval
     * @author Frederic Bastian
     * @version Bgee 14 Feb. 2017
     * @since Bgee 13
     * 
     * @param <T> the type of ID of the related entity.
     */
    public class MySQLTaxonConstraintTOResultSet<T> extends MySQLDAOResultSet<TaxonConstraintTO<T>> 
                                                 implements TaxonConstraintTOResultSet<T> {
        private final Class<T> cls;
        /**
         * Delegates to {@link MySQLDAOResultSet#MySQLDAOResultSet(BgeePreparedStatement)}
         * super constructor.
         * 
         * @param statement The first {@code BgeePreparedStatement} to execute a query on.
         */
        private MySQLTaxonConstraintTOResultSet(BgeePreparedStatement statement, Class<T> cls) {
            super(statement);
            this.cls = cls;
        }

        @Override
        protected TaxonConstraintTO<T> getNewTO() throws DAOException {
            log.traceEntry();
            
            T entityId = null;
            Integer speciesId = null;

            for (Entry<Integer, String> column: this.getColumnLabels().entrySet()) {
                try {
                    if (column.getValue().equals("stageId") || 
                                column.getValue().equals("anatEntityId") || 
                                column.getValue().equals("anatEntityRelationId")) {
                        entityId = this.getCurrentResultSet().getObject(column.getKey(), this.cls);
                    } else if (column.getValue().equals("speciesId")) {
                        speciesId = this.getInteger(column.getValue());

                    } else {
                        throw log.throwing(new UnrecognizedColumnException(column.getValue()));
                    }
                } catch (SQLException e) {
                    throw log.throwing(new DAOException(e));
                }
            }
           
            return log.traceExit(new TaxonConstraintTO<T>(entityId, speciesId));
        }
    }
}
