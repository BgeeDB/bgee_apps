package org.bgee.model.dao.mysql.gene;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.gene.GeneXRefDAO;
import org.bgee.model.dao.mysql.MySQLDAO;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.model.dao.mysql.connector.MySQLDAOResultSet;
import org.bgee.model.dao.mysql.exception.UnrecognizedColumnException;

/** 
 * A {@code GeneXRefDAO} for MySQL.
 * 
 * @author  Valentine Rech de Laval
 * @version Bgee 14, Apr. 2019
 * @since   Bgee 13, Apr. 2016
 * @see org.bgee.model.dao.api.gene.GeneXRefDAO.GeneXRefTO
 */
public class MySQLGeneXRefDAO extends MySQLDAO<GeneXRefDAO.Attribute> implements GeneXRefDAO {

    /**
     * {@code Logger} of the class.
     */
    private final static Logger log = LogManager.getLogger(MySQLGeneXRefDAO.class.getName());

    private static final String GENE_X_REF_TABLE_NAME = "geneXRef";

    /**
     * Constructor providing the {@code MySQLDAOManager} that this
     * {@code MySQLDAO} will use to obtain {@code BgeeConnection}s.
     * 
     * @param manager                   The {@code MySQLDAOManager} to use.
     * @throws IllegalArgumentException If {@code manager} is {@code null}.
     */
    public MySQLGeneXRefDAO(MySQLDAOManager manager) throws IllegalArgumentException {
        super(manager);
    }

    @Override
    public GeneXRefTOResultSet getAllGeneXRefs(Collection<GeneXRefDAO.Attribute> attributes)
            throws DAOException {
        log.traceEntry("{}", attributes);
        return log.traceExit(this.getGeneXRefs(null, null, null, attributes));
    }

    @Override
    public GeneXRefTOResultSet getGeneXRefsByBgeeGeneIds(Collection<Integer> bgeeGeneIds,
            Collection<GeneXRefDAO.Attribute> attributes) throws DAOException {
        log.traceEntry("{}, {}", bgeeGeneIds, attributes);
        return log.traceExit(this.getGeneXRefs(bgeeGeneIds, null, null, attributes));
    }
    
    @Override
    public GeneXRefTOResultSet getGeneXRefsByXRefIds(Collection<String> xRefIds,
            Collection<GeneXRefDAO.Attribute> attributes) throws DAOException {
        log.traceEntry("{}, {}", xRefIds, attributes);
        return log.traceExit(this.getGeneXRefs(null, xRefIds, null, attributes));
    }
    
    @Override
    public GeneXRefTOResultSet getGeneXRefs(Collection<Integer> bgeeGeneIds, 
            Collection<String> xRefIds, Collection<Integer> dataSourceIds,
            Collection<GeneXRefDAO.Attribute> attributes) throws DAOException {
        log.traceEntry("{}, {}, {}, {}", bgeeGeneIds, xRefIds, dataSourceIds, attributes);
        return log.traceExit(this.getGeneXRefs(bgeeGeneIds, null, null, xRefIds, dataSourceIds, attributes));
    }

    public GeneXRefTOResultSet getGeneXRefs(Collection<String> geneIds, Collection<Integer> speciesIds,
            Collection<String> xRefIds, Collection<Integer> dataSourceIds,
            Collection<GeneXRefDAO.Attribute> attributes) throws DAOException {
        log.traceEntry("{}, {}, {}, {}, {}", geneIds, speciesIds, xRefIds, dataSourceIds, attributes);
        return log.traceExit(this.getGeneXRefs(null, geneIds, speciesIds, xRefIds, dataSourceIds, attributes));
    }

    private GeneXRefTOResultSet getGeneXRefs(Collection<Integer> bgeeGeneIds,
            Collection<String> geneIds, Collection<Integer> speciesIds,
            Collection<String> xRefIds, Collection<Integer> dataSourceIds,
            Collection<GeneXRefDAO.Attribute> attributes) throws DAOException {
        log.traceEntry("{}, {}, {}, {}, {}, {}", bgeeGeneIds, geneIds, speciesIds, xRefIds,
                dataSourceIds, attributes);
        // Filter arguments
        Set<Integer> clonedBgeeGeneIds = Collections.unmodifiableSet(
                bgeeGeneIds == null? new HashSet<>(): new HashSet<>(bgeeGeneIds));
        Set<String> clonedGeneIds = Collections.unmodifiableSet(
                geneIds == null? new HashSet<>(): new HashSet<>(geneIds));
        Set<Integer> clonedSpeciesIds = Collections.unmodifiableSet(
                speciesIds == null? new HashSet<>(): new HashSet<>(speciesIds));
        Set<String> clonedXRefIds = Collections.unmodifiableSet(
                xRefIds == null? new HashSet<>(): new HashSet<>(xRefIds));
        Set<Integer> clonedDataSourceIds = Collections.unmodifiableSet(
                dataSourceIds == null? new HashSet<>(): new HashSet<>(dataSourceIds));
        Set<GeneXRefDAO.Attribute> clonedAttrs = Collections.unmodifiableSet(
                attributes == null? new HashSet<>(): new HashSet<>(attributes));
        
        // Construct sql query
        String sql = this.generateSelectClause(clonedAttrs, GENE_X_REF_TABLE_NAME);
        sql += " FROM " + GENE_X_REF_TABLE_NAME;

        if (!clonedGeneIds.isEmpty() || !clonedSpeciesIds.isEmpty()) {
            sql += " INNER JOIN gene AS t2 ON " + GENE_X_REF_TABLE_NAME + ".bgeeGeneId = t2.bgeeGeneId";
        }
        if (!clonedGeneIds.isEmpty() || !clonedSpeciesIds.isEmpty() || !clonedBgeeGeneIds.isEmpty() ||
                !clonedXRefIds.isEmpty() || !clonedDataSourceIds.isEmpty()) {
            sql += " WHERE ";
        }
        
        if (!clonedBgeeGeneIds.isEmpty()) {
            sql += GENE_X_REF_TABLE_NAME + ".bgeeGeneId IN (" +
                    BgeePreparedStatement.generateParameterizedQueryString(clonedBgeeGeneIds.size()) + ")";
        }
        if (!clonedGeneIds.isEmpty()) {
            if (!clonedBgeeGeneIds.isEmpty()) {
                sql += " AND ";
            }
            sql += "t2.geneId IN ("
                    + BgeePreparedStatement.generateParameterizedQueryString(clonedGeneIds.size()) + ")";
        }
        if (!clonedSpeciesIds.isEmpty()) {
            if (!clonedBgeeGeneIds.isEmpty() || !clonedGeneIds.isEmpty()) {
                sql += " AND ";
            }
            sql += "t2.speciesId IN ("
                    + BgeePreparedStatement.generateParameterizedQueryString(clonedSpeciesIds.size()) + ")";
        }
        if (!clonedXRefIds.isEmpty()) {
            if (!clonedBgeeGeneIds.isEmpty() || !clonedGeneIds.isEmpty() || !clonedSpeciesIds.isEmpty()) {
                sql += " AND ";
            }
            sql += GENE_X_REF_TABLE_NAME + ".XRefId IN (" + 
                    BgeePreparedStatement.generateParameterizedQueryString(clonedXRefIds.size()) + ")";
        }
        if (!clonedDataSourceIds.isEmpty()) {
            if (!clonedBgeeGeneIds.isEmpty() || !clonedGeneIds.isEmpty() || !clonedSpeciesIds.isEmpty() ||
                    !clonedXRefIds.isEmpty()) {
                sql += " AND ";
            }
            sql += GENE_X_REF_TABLE_NAME + ".dataSourceId IN (" + 
                    BgeePreparedStatement.generateParameterizedQueryString(clonedDataSourceIds.size()) + ")";
        }

        // we don't use a try-with-resource, because we return a pointer to the results,
        // not the actual results, so we should not close this BgeePreparedStatement.
        try {
            BgeePreparedStatement stmt = this.getManager().getConnection().prepareStatement(sql);
            int offsetParamIndex = 1;
            if (!clonedBgeeGeneIds.isEmpty()) {
                stmt.setIntegers(offsetParamIndex, clonedBgeeGeneIds, true);
                offsetParamIndex += clonedBgeeGeneIds.size();
            }
            if (!clonedGeneIds.isEmpty()) {
                stmt.setStrings(offsetParamIndex, clonedGeneIds, true);
                offsetParamIndex += clonedGeneIds.size();
            }
            if (!clonedSpeciesIds.isEmpty()) {
                stmt.setIntegers(offsetParamIndex, clonedSpeciesIds, true);
                offsetParamIndex += clonedSpeciesIds.size();
            }
            if (!clonedXRefIds.isEmpty()) {
                stmt.setStrings(offsetParamIndex, clonedXRefIds, true);
                offsetParamIndex += clonedXRefIds.size();
            }
            if (!clonedDataSourceIds.isEmpty()) {
                stmt.setIntegers(offsetParamIndex, clonedDataSourceIds, true);
            }
            return log.traceExit(new MySQLGeneXRefTOResultSet(stmt));
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
    }

    /**
     * Generates the SELECT clause of a MySQL query used to retrieve {@code GeneXRefTO}s.
     * 
     * @param attributes        A {@code Set} of {@code Attribute}s defining the
     *                          columns/information the query should retrieve.
     * @param geneXRefTableName A {@code String} defining the name of the table used.
     * @return                  A {@code String} containing the SELECT clause for the requested
     *                          query.
     * @throws IllegalArgumentException If one {@code Attribute} of {@code attributes} is unknown.
     */
    private String generateSelectClause(Set<GeneXRefDAO.Attribute> attributes,
            String geneXRefTableName) {
        log.traceEntry("{}, {}", attributes, geneXRefTableName);

        String sql = "";
        EnumSet<GeneXRefDAO.Attribute> clonedAttrs = Optional.ofNullable(attributes)
                .map(e -> e.isEmpty()? null: EnumSet.copyOf(e)).orElse(null);
        if (clonedAttrs == null || clonedAttrs.isEmpty()) {
            sql += "SELECT DISTINCT " + geneXRefTableName + ".*";
        } else {
            for (GeneXRefDAO.Attribute attribute : clonedAttrs) {
                if (sql.isEmpty()) {
                    sql += "SELECT DISTINCT ";
                } else {
                    sql += ", ";
                }
                sql += geneXRefTableName + ".";

                if (attribute.equals(GeneXRefDAO.Attribute.BGEE_GENE_ID)) {
                    sql += "bgeeGeneId";
                } else if (attribute.equals(GeneXRefDAO.Attribute.XREF_ID)) {
                    sql += "XRefId";
                } else if (attribute.equals(GeneXRefDAO.Attribute.XREF_NAME)) {
                    sql += "XRefName";
                } else if (attribute.equals(GeneXRefDAO.Attribute.DATA_SOURCE_ID)) {
                    sql += "dataSourceId";
                } else {
                    throw log.throwing(new IllegalArgumentException(
                            "The attribute provided (" + attribute.toString() + ") is unknown for " + 
                                    GeneXRefDAO.class.getName()));
                }
            }
        }
        return log.traceExit(sql);
    }

    /**
     * A {@code MySQLDAOResultSet} specific to {@code GeneXRefTO}.
     * 
     * @author  Valentine Rech de Laval
     * @version Bgee 13, Apr. 2016
     * @since   Bgee 13, Apr. 2016
     */
    public class MySQLGeneXRefTOResultSet extends MySQLDAOResultSet<GeneXRefTO>
        implements GeneXRefTOResultSet {

        /**
         * Delegates to {@link MySQLDAOResultSet#MySQLDAOResultSet(BgeePreparedStatement)}
         * super constructor.
         * 
         * @param statement The first {@code BgeePreparedStatement} to execute a query on.
         */
        private MySQLGeneXRefTOResultSet(BgeePreparedStatement statement) {
            super(statement);
        }

        @Override
        protected GeneXRefTO getNewTO() {
            log.traceEntry();
            Integer bgeeGeneId = null, dataSourceId = null;
            String xRefId = null, xRefName = null;
            
            // Get results
            for (Entry<Integer, String> column : this.getColumnLabels().entrySet()) {
                try {
                    if (column.getValue().equals("bgeeGeneId")) {
                        bgeeGeneId = this.getCurrentResultSet().getInt(column.getKey());

                    } else if (column.getValue().equals("XRefId")) {
                        xRefId = this.getCurrentResultSet().getString(column.getKey());

                    } else if (column.getValue().equals("XRefName")) {
                        xRefName = this.getCurrentResultSet().getString(column.getKey());

                    } else if (column.getValue().equals("dataSourceId")) {
                        dataSourceId = this.getCurrentResultSet().getInt(column.getKey());

                    } else {
                        throw log.throwing(new UnrecognizedColumnException(column.getValue()));
                    }
                } catch (SQLException e) {
                    throw log.throwing(new DAOException(e));
                }
            }
            // Set GeneXRefTO
            return log.traceExit(new GeneXRefTO(bgeeGeneId, xRefId, xRefName, dataSourceId));
        }
    }
}
