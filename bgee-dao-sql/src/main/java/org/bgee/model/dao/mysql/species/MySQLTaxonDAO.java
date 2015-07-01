package org.bgee.model.dao.mysql.species;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
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
 * @version Bgee 13
 * @see org.bgee.model.dao.api.species.TaxonDAO.TaxonTO
 * @since Bgee 13
 */
public class MySQLTaxonDAO extends MySQLDAO<TaxonDAO.Attribute> 
    implements TaxonDAO {
    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = 
            LogManager.getLogger(MySQLTaxonDAO.class.getName());
    
    /**
     * Constructor providing the {@code MySQLDAOManager} that this {@code MySQLDAO} 
     * will use to obtain {@code BgeeConnection}s.
     * @param manager   the {@code MySQLDAOManager} to use.
     * @throws IllegalArgumentException If {@code manager} is {@code null}.
     */
    public MySQLTaxonDAO(MySQLDAOManager manager) throws IllegalArgumentException {
        super(manager);
    }
    
    /**
     * Generates the SELECT clause of a MySQL query used to retrieve {@code TaxonTO}s.
     * 
     * @param attributes                A {@code Set} of {@code Attribute}s defining 
     *                                  the columns/information the query should retrieve.
     * @param taxonTableName            A {@code String} defining the name used for 
     *                                  the expression table.
     * @return                          A {@code String} containing the SELECT clause 
     *                                  for the requested query, ending with a whitespace.
     */
    private String generateSelectClause(Set<TaxonDAO.Attribute> attributes, 
            String taxonTableName) {
        log.entry(attributes, taxonTableName);
        
        String sql = new String(); 
        //always include the DISTINCT clause, this table is small.
        if (attributes == null || attributes.size() == 0) {
            sql += "SELECT DISTINCT " + taxonTableName + ".*";
        } else {
            for (TaxonDAO.Attribute attribute: attributes) {
                if (sql.length() == 0) {
                    sql += "SELECT DISTINCT ";
                } else {
                    sql += ", ";
                }
                if (attribute.equals(TaxonDAO.Attribute.ID)) {
                    sql += taxonTableName + ".taxonId";
                } else if (attribute.equals(TaxonDAO.Attribute.COMMON_NAME)) {
                    sql += taxonTableName + ".taxonCommonName";
                } else if (attribute.equals(TaxonDAO.Attribute.SCIENTIFIC_NAME)) {
                    sql += taxonTableName + ".taxonScientificName";
                } else if (attribute.equals(TaxonDAO.Attribute.LEFT_BOUND)) {
                    sql += taxonTableName + ".taxonLeftBound";
                } else if (attribute.equals(TaxonDAO.Attribute.RIGHT_BOUND)) {
                    sql += taxonTableName + ".taxonRightBound";
                } else if (attribute.equals(TaxonDAO.Attribute.LEVEL)) {
                    sql += taxonTableName + ".taxonLevel";
                } else if (attribute.equals(TaxonDAO.Attribute.LCA)) {
                    sql += taxonTableName + ".bgeeSpeciesLCA";
                } else {
                    throw log.throwing(new IllegalArgumentException(
                            "The attribute provided (" + attribute.toString() + 
                            ") is unknown for " + TaxonDAO.class.getName()));
                }
            }
        }
        sql += " ";
        
        return log.exit(sql);
    }

    @Override
    public TaxonTOResultSet getAllTaxa() throws DAOException {
        log.entry();
        
        //Construct sql query
        String sql = this.generateSelectClause(this.getAttributes(), "taxon");
        sql += "FROM taxon";
    
        //we don't use a try-with-resource, because we return a pointer to the results, 
        //not the actual results, so we should not close this BgeePreparedStatement.
        BgeePreparedStatement stmt = null;
        try {
            stmt = this.getManager().getConnection().prepareStatement(sql.toString());
            return log.exit(new MySQLTaxonTOResultSet(stmt));
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
    }

    @Override
    //TODO: integration test - test also a case where species are member of a same taxon leaf
    public TaxonTOResultSet getLeastCommonAncestor(Set<String> speciesIds, 
            boolean includeAncestors) throws DAOException, IllegalArgumentException {
        log.entry(speciesIds, includeAncestors);
        
        String sql = this.generateSelectClause(this.getAttributes(), "t1");
        sql += "FROM taxon AS t1 INNER JOIN ";
        //find the min left bound and max right bound of the taxa which the requested species 
        //belong to; the LCA is the lowest node with a leftBound < min left bound && 
        //rightBound > max right bound. 
        //we use a temp table to avoid using two subqueries in the WHERE clause.
        sql += "(SELECT MIN(taxonLeftBound) AS minLeftBound, "
                + "MAX(taxonRightBound) AS maxRightBound FROM taxon "
                + "INNER JOIN species on taxon.taxonId = species.taxonId ";
        //no species requested: find the LCA of all species in Bgee. Otherwise, parameterize. 
        if (speciesIds != null && !speciesIds.isEmpty()) {
            sql += "WHERE speciesId IN (" + 
                    BgeePreparedStatement.generateParameterizedQueryString(
                            speciesIds.size()) + ")";
        }
        //it is important to compare using greater/lower than *or equal to*, 
        //otherwise we would miss the LCA if species are all member of a same taxon leaf.
        sql += ") AS minMaxTable ON t1.taxonLeftBound <= minMaxTable.minLeftBound and "
                + "t1.taxonRightBound >= minMaxTable.maxRightBound ";
        //if we want all ancestors starting from the LCA (includeAncestors == true), 
        //then this is it. Otherwise, we retrieve the lowest node among the valid ancestors.
        if (!includeAncestors) {
            sql += "ORDER BY t1.taxonLeftBound DESC LIMIT 1";
        }
        
        //we don't use a try-with-resource, because we return a pointer to the results, 
        //not the actual results, so we should not close this BgeePreparedStatement.
        try {
            BgeePreparedStatement stmt = this.getManager().getConnection().prepareStatement(sql);
            if (speciesIds != null && !speciesIds.isEmpty()) {
                List<Integer> orderedSpeciesIds = MySQLDAO.convertToOrderedIntList(speciesIds);
                stmt.setIntegers(1, orderedSpeciesIds);
            }
            
            return log.exit(new MySQLTaxonTOResultSet(stmt));
            
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
                stmt.setInt(paramIndex, Integer.parseInt(taxonTO.getId()));
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
            return log.exit(stmt.executeUpdate());
            
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
            String taxonId = null, taxonName = null, taxonScientificName = null;
            Integer taxonLeftBound = null, taxonRightBound = null, taxonLevel = null;
            Boolean bgeeSpeciesLCA = null;
            // Get results
            for (Entry<Integer, String> column: this.getColumnLabels().entrySet()) {
                try {
                    if (column.getValue().equals("taxonId")) {
                        taxonId = this.getCurrentResultSet().getString(column.getKey());

                    } else if (column.getValue().equals("taxonCommonName")) {
                        taxonName = this.getCurrentResultSet().getString(column.getKey());

                    } else if (column.getValue().equals("taxonScientificName")) {
                        taxonScientificName = this.getCurrentResultSet().getString(
                                column.getKey());

                    } else if (column.getValue().equals("taxonLeftBound")) {
                        taxonLeftBound = this.getCurrentResultSet().getInt(column.getKey());

                    } else if (column.getValue().equals("taxonRightBound")) {
                        taxonRightBound = this.getCurrentResultSet().getInt(column.getKey());

                    } else if (column.getValue().equals("taxonLevel")) {
                        taxonLevel = this.getCurrentResultSet().getInt(column.getKey());

                    } else if (column.getValue().equals("bgeeSpeciesLCA")) {
                        bgeeSpeciesLCA = this.getCurrentResultSet().getBoolean(column.getKey());
                    } else {
                        throw log.throwing(new UnrecognizedColumnException(column.getValue()));
                    }
                } catch (SQLException e) {
                    throw log.throwing(new DAOException(e));
                }
            }
            //Set TaxonTO
            return log.exit(new TaxonTO(taxonId, taxonName, taxonScientificName, 
                    taxonLeftBound, taxonRightBound, taxonLevel, bgeeSpeciesLCA));
        }
    }

}
