package org.bgee.model.anatdev;

import java.util.Collection;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.Service;
import org.bgee.model.ServiceFactory;

/**
 * A {@link Service} to obtain {@link Sex} objects. 
 * Users should use the {@link org.bgee.model.ServiceFactory} to obtain {@code Sex}s.
 * 
 * @author  Julien Wollbrett
 * @since   Bgee 15.0, Mar. 2021
*/
public class SexService extends Service {
    
    /**
     * @param serviceFactory            The {@code ServiceFactory} to be used to obtain {@code Service}s 
     *                                  and {@code DAOManager}.
     * @throws IllegalArgumentException If {@code serviceFactory} is {@code null}.
     */
    public SexService(ServiceFactory serviceFactory) {
        super(serviceFactory);
    }

    private final static Logger log = LogManager.getLogger(SexService.class.getName());
    
    /**
     * Retrieve {@code Sex}s for the requested IDs.
     *
     * @param SexIds    A {@code Collection} of {@code String}s that are the IDs of the sexes 
     *                  to retrieve.
     * @return          A {@code Stream} of {@code AnatEntity}s retrieved for the requested IDs.
     */
    public Stream<Sex> loadSexes(Collection<String> sexIds) {
        log.traceEntry("{}", sexIds);
        return log.traceExit(sexIds.stream().map(s -> new Sex(s)));
    }

}
