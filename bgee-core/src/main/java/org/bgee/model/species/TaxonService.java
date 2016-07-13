package org.bgee.model.species;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.Service;
import org.bgee.model.ServiceFactory;

/**
 * A {@link Service} to obtain {@link Taxon} objects. 
 * Users should use the {@link ServiceFactory} to obtain {@code TaxonService}.
 * 
 * @author Frederic Bastian
 * @version Bgee 13 Sept. 2015
 * @since Bgee 13 Sept. 2015
 */
public class TaxonService extends Service {

    private static final Logger log = LogManager.getLogger(TaxonService.class.getName());

    /**
     * @param serviceFactory            The {@code ServiceFactory} to be used to obtain {@code Service}s 
     *                                  and {@code DAOManager}.
     * @throws IllegalArgumentException If {@code serviceFactory} is {@code null}.
     */
    public TaxonService(ServiceFactory serviceFactory) {
        super(serviceFactory);
    }
}
