package org.bgee.model.expressiondata;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.CommonService;
import org.bgee.model.Service;
import org.bgee.model.ServiceFactory;

/** 
 * A {@link Service} to obtain {@link Condition} objects. 
 * Users should use the {@link org.bgee.model.ServiceFactory} to obtain {@code ConditionService}s.
 * 
 * @author Valentine Rech de Laval
 * @version Bgee 14, Feb. 2017
 * @since   Bgee 13, Oct. 2016
 */
public class ConditionService extends CommonService {
    
    private static final Logger log = LogManager.getLogger(ConditionService.class.getName());
    
    public static enum Attribute implements Service.Attribute {
        ANAT_ENTITY_ID, DEV_STAGE_ID, SPECIES_ID;
    }

    /**
     * @param serviceFactory            The {@code ServiceFactory} to be used to obtain
     *                                  {@code Service}s and {@code DAOManager}.
     * @throws IllegalArgumentException If {@code serviceFactory} is {@code null}.
     */
    public ConditionService(ServiceFactory serviceFactory) {
        super(serviceFactory);
    }
//    
//    /** 
//     * Retrieve {@code Condition}s for the requested species IDs. If several species IDs 
//     * are provided, the {@code Condition}s existing in any of them are retrieved. 
//     * 
//     * @param speciesIds     A {@code Collection} of {@code Integer}s that are IDs of species 
//     *                      for which to return the {@code Condition}s.
//     * @param attributes    A {@code Collection} of {@code Attribute}s defining the
//     *                      attributes to populate in the returned {@code Condition}s.
//     *                      If {@code null} or empty, all attributes are populated. 
//     * @return              A {@code Stream} of {@code Condition}s retrieved for
//     *                      the requested species IDs.
//     */
//    public Stream<Condition> loadConditionsBySpeciesId(Collection<Integer> speciesIds,
//            Collection<Attribute> attributes) {
//        log.entry(speciesIds, attributes);
//        log.warn("Retrieval of conditions by species ID not yet implemented.");
//        Set<Integer> clonedSpeciesIds = speciesIds == null? new HashSet<>(): new HashSet<>(speciesIds);
//        Set<Attribute> clonedAttributes = attributes == null? new HashSet<>(): new HashSet<>(attributes);
//
//        return log.exit(getDaoManager().getConditionDAO().getConditionsBySpeciesIds(
//                clonedSpeciesIds, convertConditionServiceAttrsToConditionDAOAttrs(clonedAttributes)).stream()
//            .map(cTO-> mapConditionTOToCondition(cTO)));
//    }
}
