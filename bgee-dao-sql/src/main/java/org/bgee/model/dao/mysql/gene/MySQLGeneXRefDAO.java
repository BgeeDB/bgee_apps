package org.bgee.model.dao.mysql.gene;

import java.sql.SQLException;
import java.util.Collection;
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
 * @version Bgee 13, Apr. 2016
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
        log.entry(attributes);
        return log.exit(this.getGeneXRefs(null, null, null, attributes));
    }

    @Override
    public GeneXRefTOResultSet getGeneXRefsByGeneIds(Collection<String> geneIds, 
            Collection<GeneXRefDAO.Attribute> attributes) throws DAOException {
        log.entry(geneIds, attributes);
        return log.exit(this.getGeneXRefs(geneIds, null, null, attributes));
    }
    
    @Override
    public GeneXRefTOResultSet getGeneXRefsByXRefIds(Collection<String> xRefIds,
            Collection<GeneXRefDAO.Attribute> attributes) throws DAOException {
        log.entry(xRefIds, attributes);
        return log.exit(this.getGeneXRefs(null, xRefIds, null, attributes));
    }
    
    @Override
    public GeneXRefTOResultSet getGeneXRefs(Collection<String> geneIds, 
            Collection<String> xRefIds, Collection<Integer> dataSourceIds,
            Collection<GeneXRefDAO.Attribute> attributes) throws DAOException {
        log.entry(geneIds, xRefIds, dataSourceIds, attributes);

        // Filter arguments
        Set<String> clonedGeneIds = Optional.ofNullable(geneIds)
                .map(c -> new HashSet<String>(c)).orElse(null);
        Set<String> clonedXRefIds = Optional.ofNullable(xRefIds)
                .map(c -> new HashSet<String>(c)).orElse(null);
        Set<Integer> clonedDataSourceIds = Optional.ofNullable(dataSourceIds)
                .map(c -> new HashSet<Integer>(c)).orElse(null);
        Set<GeneXRefDAO.Attribute> clonedAttrs = Optional.ofNullable(attributes)
                .map(e -> EnumSet.copyOf(e)).orElse(EnumSet.allOf(GeneXRefDAO.Attribute.class));
        
        // Construct sql query
        String sql = this.generateSelectClause(clonedAttrs, GENE_X_REF_TABLE_NAME);

        sql += " FROM " + GENE_X_REF_TABLE_NAME;
        
        if (clonedGeneIds != null || clonedXRefIds != null || clonedDataSourceIds != null) {
            sql += " WHERE ";
        }
        
        if (clonedGeneIds != null) {
            sql += GENE_X_REF_TABLE_NAME + ".geneId IN (" + 
                    BgeePreparedStatement.generateParameterizedQueryString(clonedGeneIds.size()) + ")";
        }

        if (clonedGeneIds != null && (clonedXRefIds != null || clonedDataSourceIds != null)) {
            sql += " AND ";
        }
        
        if (clonedXRefIds != null) {
            sql += GENE_X_REF_TABLE_NAME + ".XRefId IN (" + 
                    BgeePreparedStatement.generateParameterizedQueryString(clonedXRefIds.size()) + ")";
        }

        if (clonedXRefIds != null && clonedDataSourceIds != null) {
            sql += " AND ";
        }
        
        if (clonedDataSourceIds != null) {
            sql += GENE_X_REF_TABLE_NAME + ".dataSourceId IN (" + 
                    BgeePreparedStatement.generateParameterizedQueryString(clonedDataSourceIds.size()) + ")";
        }

        // we don't use a try-with-resource, because we return a pointer to the results,
        // not the actual results, so we should not close this BgeePreparedStatement.
        try {
            BgeePreparedStatement stmt = this.getManager().getConnection().prepareStatement(sql);
            if (clonedGeneIds != null) {
                stmt.setStrings(1, clonedGeneIds, true);
            }
            int offsetParamIndex = (clonedGeneIds != null? clonedGeneIds.size() + 1: 1);
            if (clonedXRefIds != null) {
                stmt.setStrings(offsetParamIndex, clonedXRefIds, true);
                offsetParamIndex += clonedXRefIds.size();
            }
            if (clonedDataSourceIds != null) {
                stmt.setIntegers(offsetParamIndex, clonedDataSourceIds, true);
            }
            return log.exit(new MySQLGeneXRefTOResultSet(stmt));
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
        log.entry(attributes, geneXRefTableName);

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

                if (attribute.equals(GeneXRefDAO.Attribute.GENE_ID)) {
                    sql += "geneId";
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
        return log.exit(sql);
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
            log.entry();
            String geneId = null, xRefId = null, xRefName = null;
            Integer dataSourceId = null;
            
            // Get results
            for (Entry<Integer, String> column : this.getColumnLabels().entrySet()) {
                try {
                    if (column.getValue().equals("geneId")) {
                        geneId = this.getCurrentResultSet().getString(column.getKey());

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
            return log.exit(new GeneXRefTO(geneId, xRefId, xRefName, dataSourceId));
        }
    }
}
