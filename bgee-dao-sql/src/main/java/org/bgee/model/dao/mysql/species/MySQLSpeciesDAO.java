package org.bgee.model.dao.mysql.species;

import java.sql.SQLException;
import java.util.Collection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.species.SpeciesDAO;
import org.bgee.model.dao.mysql.MySQLDAO;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;

/**
 * A {@code SpeciesDAO} for MySQL. 
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @see org.bgee.model.dao.api.species.SpeciesTO
 * @since Bgee 01
 */
public class MySQLSpeciesDAO extends MySQLDAO<SpeciesDAO.Attribute> 
    implements SpeciesDAO {
    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = 
            LogManager.getLogger(MySQLSpeciesDAO.class.getName());
    
    /**
     * Constructor providing the {@code MySQLDAOManager} that this {@code MySQLDAO} 
     * will use to obtain {@code BgeeConnection}s.
     * @param manager   the {@code MySQLDAOManager} to use.
     * @throws IllegalArgumentException If {@code manager} is {@code null}.
     */
    public MySQLSpeciesDAO(MySQLDAOManager manager) throws IllegalArgumentException {
        super(manager);
    }
    

    //***************************************************************************
    // METHODS NOT PART OF THE bgee-dao-api, USED BY THE PIPELINE AND NOT MEANT 
    //TO BE EXPOSED TO THE PUBLIC API.
    //***************************************************************************
    /**
     * Inserts the provided species into the Bgee database, represented as 
     * a {@code Collection} of {@code SpeciesTO}s.
     * 
     * @param specieTOs a {@code Collection} of {@code SpeciesTO}s to be inserted 
     *                  into the database.
     * @return          An {@code int} that is the number of species inserted 
     *                  as a result of this method call.
     * @throws DAOException     If a {@code SQLException} occurred while trying 
     *                          to insert {@code species}. The {@code SQLException} 
     *                          will be wrapped into a {@code DAOException} ({@code DAOs} 
     *                          do not expose these kind of implementation details).
     */
    public int insertSpecies(Collection<SpeciesTO> specieTOs) throws DAOException {
        log.entry(specieTOs);
        
        String sql = "Insert into species (speciesId, genus, species, " +
                "speciesCommonName, taxonId, genomeFilePath, genomeSpeciesId, " +
                "fakeGeneIdPrefix) values ";
        for (int i = 0; i < specieTOs.size(); i++) {
            if (i > 0) {
                sql += ", ";
            }
            sql += "(?, ?, ?, ?, ?, ?, ?, ?) ";
        }
        try (BgeePreparedStatement stmt = 
                this.getManager().getConnection().prepareStatement(sql)) {
            int paramIndex = 1;
            for (SpeciesTO speciesTO: specieTOs) {
                stmt.setInt(paramIndex, Integer.parseInt(speciesTO.getId()));
                paramIndex++;
                stmt.setString(paramIndex, speciesTO.getGenus());
                paramIndex++;
                stmt.setString(paramIndex, speciesTO.getSpeciesName());
                paramIndex++;
                stmt.setString(paramIndex, speciesTO.getName());
                paramIndex++;
                stmt.setInt(paramIndex, Integer.parseInt(speciesTO.getParentTaxonId()));
                paramIndex++;
                stmt.setString(paramIndex, speciesTO.getGenomeFilePath());
                paramIndex++;
                //TODO: handles default values in a better way
                if (speciesTO.getGenomeSpeciesId() != null) {
                    stmt.setInt(paramIndex, Integer.parseInt(speciesTO.getGenomeSpeciesId()));
                } else {
                    stmt.setInt(paramIndex, 0);
                }
                paramIndex++;
                //TODO: handles default values in a better way
                if (speciesTO.getFakeGeneIdPrefix() != null) {
                    stmt.setString(paramIndex, speciesTO.getFakeGeneIdPrefix());
                } else {
                    stmt.setString(paramIndex, "");
                }
                paramIndex++;
            }
            
            return log.exit(stmt.executeUpdate());
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
    }
}
