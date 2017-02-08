package org.bgee.model.expressiondata;

import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.Service;
import org.bgee.model.ServiceFactory;

/** 
 * A {@link Service} to obtain {@link Condition} objects. 
 * Users should use the {@link org.bgee.model.ServiceFactory} to obtain {@code ConditionService}s.
 * 
 * @author Valentine Rech de Laval
 * @version Bgee 13, Feb. 2017
 * @since   Bgee 13, Oct. 2016
 */
public class ConditionService extends Service {
    
    private static final Logger log = LogManager.getLogger(ConditionService.class.getName());

    public ConditionService(ServiceFactory serviceFactory) {
        super(serviceFactory);
    }
    
    public Stream<Condition> loadConditionsBySpeciesId(String speciesId) {
        log.entry(speciesId);
        log.warn("Retrieval of conditions by species ID not yet implemented.");
        return log.exit(Stream.empty());
    }
}
