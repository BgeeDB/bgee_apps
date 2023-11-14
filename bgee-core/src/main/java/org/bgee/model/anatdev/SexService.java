package org.bgee.model.anatdev;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.CommonService;
import org.bgee.model.Service;
import org.bgee.model.ServiceFactory;

/**
 * A {@link Service} to obtain {@link Sex} objects.
 * 
 * @author  Julien Wollbrett
 * @author  Frederic Bastian
 * @version Bgee 15.0, Oct. 2022
 * @since   Bgee 15.0, Mar. 2021
*/
public class SexService extends CommonService {
    
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
     * @param sexIds    A {@code Collection} of {@code String}s that are the IDs of the sexes
     *                  to retrieve.
     * @return          A {@code Stream} of {@code Sex}s retrieved for the requested IDs.
     */
    public Stream<Sex> loadSexes(Collection<String> sexIds) {
        log.traceEntry("{}", sexIds);
        return log.traceExit(sexIds.stream().map(s -> new Sex(s)));
    }

    /**
     * Retrieve {@code Sex}s valid in the species designated by its ID.
     *
     * @param speciesId An {@code int} that is the ID of the requested species.
     * @return          A {@code Set} of {@code Sex}s valid in the requested species.
     */
    public Set<Sex> loadSexesBySpeciesId(int speciesId) {
        log.traceEntry("{}", speciesId);
        return log.traceExit(this.getDaoManager().getSexDAO().getSpeciesToSex(Set.of(speciesId))
                .stream()
                .map(to -> to.getSex())
                .map(daoRawDataSex -> mapDAORawDataSexToRawDataSex(daoRawDataSex))
                .map(rawDataSex -> mapRawDataSexToSex(rawDataSex))
                .collect(Collectors.toSet()));
    }
}