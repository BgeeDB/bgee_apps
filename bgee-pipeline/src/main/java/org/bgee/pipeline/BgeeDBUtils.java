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
     * @return A {@code Set} of {@code String}s containing species IDs of the Bgee database.
     * @throws DAOException If an error occurred while getting the data from the Bgee database.
     */
    public static List<String> loadSpeciesIdsFromDb(MySQLDAOUser mySQLDAOUser) throws DAOException {
        log.entry();
        
        log.info("Start retrieving species IDs...");

        SpeciesDAO dao = mySQLDAOUser.getSpeciesDAO();
        dao.setAttributes(SpeciesDAO.Attribute.ID);
        
        SpeciesTOResultSet rsSpecies = dao.getAllSpecies();
        List<String> speciesIdsInBgee = new ArrayList<String>();
        while (rsSpecies.next()) {
            speciesIdsInBgee.add(rsSpecies.getTO().getId());
        }
        //no need for a try with resource or a finally, the insert method will close everything 
        //at the end in any case.
        rsSpecies.close();
        
        log.info("Done retrieving species IDs, {} species found", speciesIdsInBgee.size());
    
        return log.exit(speciesIdsInBgee);        
    }

}
