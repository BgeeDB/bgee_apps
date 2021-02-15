package org.bgee.model.dao.mysql.species;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.species.TaxonDAO;
import org.bgee.model.dao.mysql.MySQLDAO;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.model.dao.mysql.connector.MySQLDAOResultSet;
import org.bgee.model.dao.mysql.exception.UnrecognizedColumnException;

/**
 * A {@code TaxonDAO} for MySQL. 
 * 
 * @author Frederic Bastian
 * @version Bgee 14 Mar. 2019
 * @see org.bgee.model.dao.api.species.TaxonDAO.TaxonTO
 * @since Bgee 13
 */
public class MySQLTaxonDAO extends MySQLDAO<TaxonDAO.Attribute> implements TaxonDAO {
    private final static Logger log = LogManager.getLogger(MySQLTaxonDAO.class.getName());

    /**
     * A {@code Map} of column name to their corresponding {@code Attribute}.
     */
    private static final Map<String, TaxonDAO.Attribute> COL_TO_ATTR_MAP;
    private static final String TAXON_TABLE = "taxon";
    static {
        COL_TO_ATTR_MAP = new HashMap<>();
        COL_TO_ATTR_MAP.put("taxonId", TaxonDAO.Attribute.ID);
        COL_TO_ATTR_MAP.put("taxonCommonName", TaxonDAO.Attribute.COMMON_NAME);
        COL_TO_ATTR_MAP.put("taxonScientificName", TaxonDAO.Attribute.SCIENTIFIC_NAME);
        COL_TO_ATTR_MAP.put("taxonLeftBound", TaxonDAO.Attribute.LEFT_BOUND);
        COL_TO_ATTR_MAP.put("taxonRightBound", TaxonDAO.Attribute.RIGHT_BOUND);
        COL_TO_ATTR_MAP.put("taxonLevel", TaxonDAO.Attribute.LEVEL);
        COL_TO_ATTR_MAP.put("bgeeSpeciesLCA", TaxonDAO.Attribute.LCA);
    }
    
    /**
     * Constructor providing the {@code MySQLDAOManager} that this {@code MySQLDAO} 
     * will use to obtain {@code BgeeConnection}s.
     * @param manager   the {@code MySQLDAOManager} to use.
     * @throws IllegalArgumentException If {@code manager} is {@code null}.
     */
    public MySQLTaxonDAO(MySQLDAOManager manager) throws IllegalArgumentException {
        super(manager);
    }

    @Override
    public TaxonTOResultSet getTaxa(Collection<Integer> taxonIds, boolean lca,
            Collection<TaxonDAO.Attribute> attributes) throws DAOException {
        log.entry(taxonIds, lca, attributes);

        Set<Integer> clonedTaxIds = Collections.unmodifiableSet(taxonIds == null? new HashSet<>():
            new HashSet<>(taxonIds));
        Set<TaxonDAO.Attribute> clonedAttrs = Collections.unmodifiableSet(attributes == null? new HashSet<>():
            new HashSet<>(attributes));
        //Construct sql query
        StringBuilder sb = new StringBuilder(generateSelectClause(TAXON_TABLE, COL_TO_ATTR_MAP,
                true, clonedAttrs));
        sb.append(" FROM ").append(TAXON_TABLE);
        if (!clonedTaxIds.isEmpty() || lca) {
            sb.append(" WHERE ");
            if (!clonedTaxIds.isEmpty()) {
                sb.append("taxonId IN (")
                  .append(BgeePreparedStatement.generateParameterizedQueryString(clonedTaxIds.size()))
                  .append(")");
            }
            if (lca) {
                if (!clonedTaxIds.isEmpty()) {
                    sb.append(" AND ");
                }
                sb.append("bgeeSpeciesLCA = 1");
            }
        }
    
        //we don't use a try-with-resource, because we return a pointer to the results, 
        //not the actual results, so we should not close this BgeePreparedStatement.
        BgeePreparedStatement stmt = null;
        try {
            stmt = this.getManager().getConnection().prepareStatement(sb.toString());
            if (!clonedTaxIds.isEmpty()) {
                stmt.setIntegers(1, clonedTaxIds, true);
            }
            return log.traceExit(new MySQLTaxonTOResultSet(stmt));
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
    }

    @Override
    //TODO: integration test - test also a case where species are member of a same taxon leaf
    public TaxonTO getLeastCommonAncestor(Collection<Integer> speciesIds,
            Collection<TaxonDAO.Attribute> attributes) throws DAOException {
        log.entry(speciesIds, attributes);
        
        final Set<Integer> clonedSpeIds = Collections.unmodifiableSet(
                speciesIds == null? new HashSet<>(): new HashSet<>(speciesIds));
        Set<TaxonDAO.Attribute> clonedAttrs = Collections.unmodifiableSet(attributes == null? new HashSet<>():
            new HashSet<>(attributes));

        //Construct sql query
        StringBuilder sb = new StringBuilder(generateSelectClause("t1", COL_TO_ATTR_MAP,
                true, clonedAttrs));
        //We always add taxonLeftBound unless it was requested in attributes.
        //fix for issue #173
        if (!clonedAttrs.isEmpty() && !clonedAttrs.contains(TaxonDAO.Attribute.LEFT_BOUND)) {
            sb.append(", t1.taxonLeftBound ");
        }
        sb.append("FROM taxon AS t1 INNER JOIN ");
        //find the min left bound and max right bound of the taxa which the requested species 
        //belong to; the LCA is the lowest node with a leftBound < min left bound && 
        //rightBound > max right bound. 
        //we use a temp table to avoid using two subqueries in the WHERE clause.
        sb.append("(SELECT MIN(taxonLeftBound) AS minLeftBound, ")
          .append("MAX(taxonRightBound) AS maxRightBound FROM taxon ")
          .append("INNER JOIN species on taxon.taxonId = species.taxonId ");
        //no species requested: find the LCA of all species in Bgee. Otherwise, parameterize. 
        if (!clonedSpeIds.isEmpty()) {
            sb.append("WHERE speciesId IN (")
              .append(BgeePreparedStatement.generateParameterizedQueryString(clonedSpeIds.size()))
              .append(")");
        }
        //it is important to compare using greater/lower than *or equal to*, 
        //otherwise we would miss the LCA if species are all member of a same taxon leaf.
        sb.append(") AS minMaxTable ON t1.taxonLeftBound <= minMaxTable.minLeftBound and ")
          .append("t1.taxonRightBound >= minMaxTable.maxRightBound ");
        //we retrieve the lowest node among the valid ancestors.
        sb.append("ORDER BY t1.taxonLeftBound DESC LIMIT 1");
        
        //we don't use a try-with-resource, because we return a pointer to the results, 
        //not the actual results, so we should not close this BgeePreparedStatement.
        try {
            BgeePreparedStatement stmt = this.getManager().getConnection().prepareStatement(sb.toString());
            if (!clonedSpeIds.isEmpty()) {
                stmt.setIntegers(1, clonedSpeIds, true);
            }
            try(MySQLTaxonTOResultSet rs = new MySQLTaxonTOResultSet(stmt)) {
                return log.traceExit(rs.stream().findFirst().get());
            }
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
    }


    //***************************************************************************
    // METHODS NOT PART OF THE bgee-dao-api, USED BY THE PIPELINE AND NOT MEANT 
    //TO BE EXPOSED TO THE PUBLIC API.
    //***************************************************************************

    @Override
    public int insertTaxa(Collection<TaxonTO> taxa) throws DAOException, 
    IllegalArgumentException {
        log.entry(taxa);
        
        if (taxa == null || taxa.isEmpty()) {
            throw log.throwing(new IllegalArgumentException(
                    "No taxon is given, then no taxon is inserted"));
        }
        
        String sql = "Insert into taxon (taxonId, taxonScientificName, " +
                "taxonCommonName, taxonLeftBound, taxonRightBound, taxonLevel, " +
                "bgeeSpeciesLCA) values ";
        for (int i = 0; i < taxa.size(); i++) {
            if (i > 0) {
                sql += ", ";
            }
            sql += "(?, ?, ?, ?, ?, ?, ?) ";
        }
        try (BgeePreparedStatement stmt = 
                this.getManager().getConnection().prepareStatement(sql)) {
            int paramIndex = 1;
            for (TaxonTO taxonTO: taxa) {
                stmt.setInt(paramIndex, taxonTO.getId());
                paramIndex++;
                stmt.setString(paramIndex, taxonTO.getScientificName());
                paramIndex++;
                stmt.setString(paramIndex, taxonTO.getName());
                paramIndex++;
                stmt.setInt(paramIndex, taxonTO.getLeftBound());
                paramIndex++;
                stmt.setInt(paramIndex, taxonTO.getRightBound());
                paramIndex++;
                stmt.setInt(paramIndex, taxonTO.getLevel());
                paramIndex++;
                stmt.setBoolean(paramIndex, taxonTO.isLca());
                paramIndex++;
            }
            return log.traceExit(stmt.executeUpdate());
            
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
    }

    /**
     * A {@code MySQLDAOResultSet} specific to {@code TaxonTO}.
     * 
     * @author Valentine Rech de Laval
     * @version Bgee 13
     * @since Bgee 13
     */
    public class MySQLTaxonTOResultSet extends MySQLDAOResultSet<TaxonTO> 
            implements TaxonTOResultSet {

        /**
         * Delegates to {@link MySQLDAOResultSet#MySQLDAOResultSet(BgeePreparedStatement)}
         * super constructor.
         * 
         * @param statement The first {@code BgeePreparedStatement} to execute a query on.
         */
        private MySQLTaxonTOResultSet(BgeePreparedStatement statement) {
            super(statement);
        }

        @Override
        protected TaxonTO getNewTO() {
            log.entry();
            String taxonName = null, taxonScientificName = null;
            Integer taxonId = null, taxonLeftBound = null, taxonRightBound = null, taxonLevel = null;
            Boolean bgeeSpeciesLCA = null;
            // Get results
            try {
                for (Entry<Integer, String> column: this.getColumnLabels().entrySet()) {
                    String columnName = column.getValue();
                    int columnIndex = column.getKey();
                    TaxonDAO.Attribute attr = getAttributeFromColName(columnName, COL_TO_ATTR_MAP);
                    switch (attr) {
                    case ID:
                        taxonId = this.getCurrentResultSet().getInt(columnIndex);
                        break;
                    case COMMON_NAME:
                        taxonName = this.getCurrentResultSet().getString(columnIndex);
                        break;
                    case SCIENTIFIC_NAME:
                        taxonScientificName = this.getCurrentResultSet().getString(columnIndex);
                        break;
                    case LEFT_BOUND:
                        taxonLeftBound = this.getCurrentResultSet().getInt(columnIndex);
                        break;
                    case RIGHT_BOUND:
                        taxonRightBound = this.getCurrentResultSet().getInt(columnIndex);
                        break;
                    case LEVEL:
                        taxonLevel = this.getCurrentResultSet().getInt(columnIndex);
                        break;
                    case LCA:
                        bgeeSpeciesLCA = this.getCurrentResultSet().getBoolean(columnIndex);
                        break;
                    default:
                        log.throwing(new UnrecognizedColumnException(columnName));
                    }
                }
                //Set TaxonTO
                return log.traceExit(new TaxonTO(taxonId, taxonName, taxonScientificName, 
                        taxonLeftBound, taxonRightBound, taxonLevel, bgeeSpeciesLCA));
            } catch (SQLException e) {
                throw log.throwing(new DAOException(e));
            }
        }
    }
}