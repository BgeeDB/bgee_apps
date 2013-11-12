package org.bgee.model.dao.mysql.species;

import java.sql.SQLException;
import java.util.Collection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.species.TaxonDAO;
import org.bgee.model.dao.api.species.TaxonTO;
import org.bgee.model.dao.mysql.MySQLDAO;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;

/**
 * A {@code TaxonDAO} for MySQL. 
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @see org.bgee.model.dao.api.species.TaxonTO
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
    /**
     * Inserts the provided taxa into the Bgee database, represented as 
     * a {@code Collection} of {@code TaxonTO}s.
     * 
     * @param taxa      a {@code Collection} of {@code TaxonTO}s to be inserted 
     *                  into the database.
     * @throws DAOException     If a {@code SQLException} occurred while trying 
     *                          to insert {@code taxa}. The {@code SQLException} 
     *                          will be wrapped into a {@code DAOException} ({@code DAOs} 
     *                          do not expose these kind of implementation details).
     */
    public int insertTaxa(Collection<TaxonTO> taxa) throws DAOException {
        log.entry(taxa);
        
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
}
