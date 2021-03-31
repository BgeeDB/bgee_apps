package org.bgee.model.anatdev;

import java.util.Collection;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.Service;
import org.bgee.model.ServiceFactory;

/**
 * A {@link Service} to obtain {@link Strain} objects. 
 * Users should use the {@link org.bgee.model.ServiceFactory} to obtain {@code Strain}s.
 * 
 * @author  Julien Wollbrett
 * @since   Bgee 15.0, Mar. 2021
*/
public class StrainService extends Service {
    private final static Logger log = LogManager.getLogger(Strain.class.getName());    
    /**
     * @param serviceFactory            The {@code ServiceFactory} to be used to obtain {@code Service}s 
     *                                  and {@code DAOManager}.
     * @throws IllegalArgumentException If {@code serviceFactory} is {@code null}.
     */
    public StrainService(ServiceFactory serviceFactory) {
        super(serviceFactory);
    }
    
    /**
     * Retrieve {@code Strain}s for the requested strain IDs. 
     * If an entity in {@code StrainIds} does not exists it will not be returned.
     * 
     * @param strainIds         A {@code Collection} of {@code String}s that are IDs of strains to 
     *                          retrieve. Can be {@code null} or empty.
     * @return                  A {@code Stream} of {@code Strain}s retrieved for the requested parameters.
     */
    //XXX: shouldn't we retrieve valid strains from the database? Maybe unnecessary?
    public Stream<Strain> loadStrains(Collection<String> strainIds) {
        log.traceEntry("{}", strainIds);
        return log.traceExit(strainIds.stream().map(s -> new Strain(s)));
    }

}
