package org.bgee.model.expressiondata;

import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.Service;
import org.bgee.model.ServiceFactory;
import org.bgee.model.dao.api.expressiondata.ConditionDAO;
import org.bgee.model.dao.api.expressiondata.ConditionDAO.ConditionTO;

/** 
 * A {@link Service} to obtain {@link Condition} objects. 
 * Users should use the {@link org.bgee.model.ServiceFactory} to obtain {@code ConditionService}s.
 * 
 * @author Valentine Rech de Laval
 * @version Bgee 14, Feb. 2017
 * @since   Bgee 13, Oct. 2016
 */
public class ConditionService extends Service {
    
    private static final Logger log = LogManager.getLogger(ConditionService.class.getName());
    
    public static enum Attribute implements Service.Attribute {
        ID, ANAT_ENTITY_ID, DEV_STAGE_ID, SPECIES_ID;
    }

    /**
     * @param serviceFactory            The {@code ServiceFactory} to be used to obtain
     *                                  {@code Service}s and {@code DAOManager}.
     * @throws IllegalArgumentException If {@code serviceFactory} is {@code null}.
     */
    public ConditionService(ServiceFactory serviceFactory) {
        super(serviceFactory);
    }
    
    /** 
     * Retrieve {@code Condition}s for the requested species IDs. If several species IDs 
     * are provided, the {@code Condition}s existing in any of them are retrieved. 
     * 
     * @param speciesId     A {@code Collection} of {@code String}s that are IDs of species 
     *                      for which to return the {@code Condition}s.
     * @param attributes    A {@code Collection} of {@code Attribute}s defining the
     *                      attributes to populate in the returned {@code Condition}s.
     *                      If {@code null} or empty, all attributes are populated. 
     * @return              A {@code Stream} of {@code Condition}s retrieved for
     *                      the requested species IDs.
     */
    public Stream<Condition> loadConditionsBySpeciesId(Collection<String> speciesId, Collection<Attribute> attributes) {
        log.entry(speciesId, attributes);
        log.warn("Retrieval of conditions by species ID not yet implemented.");
        Set<String> clonedSpeciesIds = speciesId == null? new HashSet<>(): new HashSet<>(speciesId);
        Set<Attribute> clonedAttributes = attributes == null? new HashSet<>(): new HashSet<>(attributes);

        return log.exit(getDaoManager().getConditionDAO().getConditionsBySpeciesIds(
                clonedSpeciesIds, convertServiceAttrsToDAOAttrs(clonedAttributes)).stream()
            .map(ConditionService::mapFromTO));
    }
    
    private Collection<ConditionDAO.Attribute> convertServiceAttrsToDAOAttrs(
            Collection<Attribute> attributes) {
        log.entry(attributes);
        return log.exit(attributes.stream().flatMap(attr -> {
            switch (attr) {
            case ID: 
                return Stream.of(ConditionDAO.Attribute.ID);
            case ANAT_ENTITY_ID: 
                return Stream.of(ConditionDAO.Attribute.ANAT_ENTITY_ID);
            case DEV_STAGE_ID: 
                return Stream.of(ConditionDAO.Attribute.STAGE_ID);
            case SPECIES_ID: 
                return Stream.of(ConditionDAO.Attribute.SPECIES_ID);
            default: 
                throw log.throwing(new IllegalStateException("Unsupported Attributes from CallService: "
                        + attr));
            }
        }).collect(Collectors.toCollection(() -> EnumSet.noneOf(ConditionDAO.Attribute.class))));
    }

    /**
     * Maps {@code ConditionTO} to a {@code Condition}.
     * 
     * @param condTO    The {@code ConditionTO} to map.
     * @return          The mapped {@code Condition}.
     */
    private static Condition mapFromTO(ConditionTO condTO) {
        log.entry(condTO);
        if (condTO == null) {
            return log.exit(null);
        }
        return log.exit(new Condition(Integer.valueOf(condTO.getId()), condTO.getAnatEntityId(),
            condTO.getStageId(), String.valueOf(condTO.getSpeciesId())));
    }

}
