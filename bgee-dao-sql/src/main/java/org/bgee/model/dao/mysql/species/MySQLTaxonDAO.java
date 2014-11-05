package org.bgee.model.dao.mysql.species;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map.Entry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.species.TaxonDAO;
import org.bgee.model.dao.mysql.MySQLDAO;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.model.dao.mysql.connector.MySQLDAOResultSet;

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
    
    
    //***************************************************************************
    // METHODS NOT PART OF THE bgee-dao-api, USED BY THE PIPELINE AND NOT MEANT 
    //TO BE EXPOSED TO THE PUBLIC API.
    //***************************************************************************

    @Override
    public int insertTaxa(Collection<TaxonTO> taxa) throws DAOException, IllegalArgumentException {
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
    
    @Override
    public TaxonTOResultSet getAllTaxa() throws DAOException {
        log.entry();
        
        Collection<TaxonDAO.Attribute> attributes = this.getAttributes();
        //Construct sql query
        String sql = new String(); 
        if (attributes == null || attributes.size() == 0) {
            sql += "SELECT *";
        } else {
            for (TaxonDAO.Attribute attribute: attributes) {
                if (sql.length() == 0) {
                    sql += "SELECT ";
                } else {
                    sql += ", ";
                }
                sql += this.attributeToString(attribute);
            }
        }
        sql += " FROM taxon";

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

    private String attributeToString(TaxonDAO.Attribute attribute) {
        log.entry(attribute);
        
        String label = null;
        if (attribute.equals(TaxonDAO.Attribute.ID)) {
            label = "taxonId";
        } else if (attribute.equals(TaxonDAO.Attribute.COMMONNAME)) {
            label = "taxonCommonName";
        } else if (attribute.equals(TaxonDAO.Attribute.SCIENTIFICNAME)) {
            label = "taxonScientificName";
        } else if (attribute.equals(TaxonDAO.Attribute.LEFTBOUND)) {
            label = "taxonLeftBound";
        } else if (attribute.equals(TaxonDAO.Attribute.RIGHTBOUND)) {
            label = "taxonRightBound";
        } else if (attribute.equals(TaxonDAO.Attribute.LEVEL)) {
            label = "taxonLevel";
        } else if (attribute.equals(TaxonDAO.Attribute.LCA)) {
            label = "bgeeSpeciesLCA";
        } 
        
        return log.exit(label);
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
        public MySQLTaxonTOResultSet(BgeePreparedStatement statement) {
            super(statement);
        }

        @Override
        public TaxonTO getTO() {
            log.entry();
            ResultSet currentResultSet = this.getCurrentResultSet();
            String taxonId=null, taxonName=null, taxonScientificName=null;
            int taxonLeftBound=0, taxonRightBound=0, taxonLevel=0;
            boolean bgeeSpeciesLCA=false;
            // Get results
            for (Entry<Integer, String> column: this.getColumnLabels().entrySet()) {
                try {
                    if (column.getValue().equals("taxonId")) {
                        taxonId = currentResultSet.getString(column.getKey());

                    } else if (column.getValue().equals("taxonCommonName")) {
                        taxonName = currentResultSet.getString(column.getKey());

                    } else if (column.getValue().equals("taxonScientificName")) {
                        taxonScientificName = currentResultSet.getString(column.getKey());

                    } else if (column.getValue().equals("taxonLeftBound")) {
                        taxonLeftBound = currentResultSet.getInt(column.getKey());

                    } else if (column.getValue().equals("taxonRightBound")) {
                        taxonRightBound = currentResultSet.getInt(column.getKey());

                    } else if (column.getValue().equals("taxonLevel")) {
                        taxonLevel = currentResultSet.getInt(column.getKey());

                    } else if (column.getValue().equals("bgeeSpeciesLCA")) {
                        bgeeSpeciesLCA = currentResultSet.getBoolean(column.getKey());
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
