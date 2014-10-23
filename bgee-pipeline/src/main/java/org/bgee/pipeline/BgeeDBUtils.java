package org.bgee.pipeline;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.species.SpeciesDAO;
import org.bgee.model.dao.api.species.SpeciesDAO.SpeciesTOResultSet;


/**
 * This class provides convenient common methods that retrieve data from Bgee.
 * 
 * @author Valentine Rech de Laval
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public class BgeeDBUtils {

    /**
     * {@code Logger} of the class.
     */
    private final static Logger log = LogManager.getLogger(BgeeDBUtils.class.getName());
    

    /**
     * Retrieves all species IDs present into the Bgee database. 
     * 
     * @param speciesDAO    A {@code SpeciesDAO} to use to retrieve information about species 
     *                      from the Bgee data source.
     * @return A {@code Set} of {@code String}s containing species IDs of the Bgee database.
     * @throws DAOException If an error occurred while getting the data from the Bgee database.
     */
    public static List<String> getSpeciesIdsFromDb(SpeciesDAO speciesDAO) throws DAOException {
        log.entry(speciesDAO);

        speciesDAO.setAttributes(SpeciesDAO.Attribute.ID);
        
        try (SpeciesTOResultSet rsSpecies = speciesDAO.getAllSpecies()) {
            List<String> speciesIdsInBgee = new ArrayList<String>();
            while (rsSpecies.next()) {
                speciesIdsInBgee.add(rsSpecies.getTO().getId());
            }
            return log.exit(speciesIdsInBgee); 
        } 
    }
    
    /**
     * Retrieve and validate species IDs from the Bgee data source. If {@code speciesIds} 
     * is {@code null} or empty, this method will return the IDs of all the species present 
     * in Bgee (as returned by {@link #getSpeciesIdsFromDb(SpeciesDAO)}). Otherwise, 
     * this method will check that all provided IDs correspond to actual species in Bgee, and 
     * will return the validated {@code Collection} provided as argument. If an ID 
     * is not found in Bgee, this method will throw an {@code IllegalArgumentException}.
     * 
     * @param speciesIds    A {@code List} of {@code String}s that are IDs of species, 
     *                      to be validated.
     * @param speciesDAO    A {@code SpeciesDAO} to use to retrieve information about species 
     *                      from the Bgee data source
     * @return              A {@code List} of {@code String}s that are the IDs of all species 
     *                      in Bgee, if {@code speciesIds} was {@code null} or empty, 
     *                      otherwise, returns the argument {@code speciesIds} itself.
     * @throws IllegalArgumentException If {@code speciesIds} is not {@code null} nor empty 
     *                                  and an ID is not found in Bgee.
     */
    public static List<String> checkAndGetSpeciesIds(List<String> speciesIds, SpeciesDAO speciesDAO) 
            throws IllegalArgumentException {
        log.entry(speciesIds, speciesDAO);
        
        List<String> speciesIdsFromDb = BgeeDBUtils.getSpeciesIdsFromDb(speciesDAO); 
        if (speciesIds == null || speciesIds.isEmpty()) {
            return log.exit(speciesIdsFromDb);
        } else if (!speciesIdsFromDb.containsAll(speciesIds)) {
            //copy to avoid modifying user input, maybe the caller 
            //will recover from the exception (but it should not...)
            List<String> debugSpeciesIds = new ArrayList<String>(speciesIds);
            debugSpeciesIds.removeAll(speciesIdsFromDb);
            throw log.throwing(new IllegalArgumentException("Some species IDs " +
                    "could not be found in Bgee: " + debugSpeciesIds));
        } 
        return log.exit(speciesIds);
    }

}
