package org.bgee.model;

import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.expressiondata.ConditionDAO;
import org.bgee.model.dao.api.expressiondata.ConditionDAO.ConditionTO;
import org.bgee.model.expressiondata.Condition;
import org.bgee.model.expressiondata.ConditionService;
import org.bgee.model.expressiondata.baseelements.DataType;

/**
 * Parent class of several {@code Service}s needing to access common methods. 
 * Since we do not want to expose these methods to API users, we do not build this class 
 * as an "utils" that {@code Service}s could use as a dependency, but as a parent class to inherit from.
 * 
 * @author Valentine Rech de Laval
 * @author Frederic Bastian
 * @version Bgee 14 Feb. 2017
 * @since Bgee 14 Feb. 2017
 *
 */
public class CommonService extends Service {
    private final static Logger log = LogManager.getLogger(CommonService.class.getName());

    /**
     * @param serviceFactory    The {@code ServiceFactory} that instantiated this {@code Service}.
     * @throws IllegalArgumentException If {@code serviceFactory} is {@code null}.
     */
    protected CommonService(ServiceFactory serviceFactory) {
        super(serviceFactory);
    }
    
    protected Set<ConditionDAO.Attribute> convertConditionServiceAttrsToConditionDAOAttrs(
        Set<ConditionService.Attribute> attributes) {
        log.entry(attributes);

        return log.exit(attributes.stream().flatMap(attr -> {
            switch (attr) {
                case ANAT_ENTITY_ID: 
                    return Stream.of(ConditionDAO.Attribute.ANAT_ENTITY_ID);
                case DEV_STAGE_ID: 
                    return Stream.of(ConditionDAO.Attribute.STAGE_ID);
                case SPECIES_ID:
                    return Stream.of(ConditionDAO.Attribute.SPECIES_ID);
                default: 
                    throw log.throwing(new IllegalStateException(
                        "Unsupported Attributes from ConditionService: " + attr));
            }
        }).collect(Collectors.toCollection(() -> EnumSet.noneOf(ConditionDAO.Attribute.class))));
    }
    
    /**
     * Map {@code ConditionTO} to a {@code Condition}.
     * 
     * @param condTO    A {@code ConditionTO} that is the condition from db
     *                  to map into {@code Condition}.
     * @return          The mapped {@code Condition}.
     */
    protected Condition mapConditionTOToCondition(ConditionTO condTO) {
        log.entry(condTO);
        if (condTO == null) {
            return log.exit(null);
        }
        Map<DataType, BigDecimal> ranks = new HashMap<>();
        for (DataType dt: DataType.values()) {
            switch (dt) {
                case AFFYMETRIX:
//                    ranks.put(dt, condTO.getAffymetrixMaxRank());
                    break;
                case EST:
//                    ranks.put(dt, condTO.getEstMaxRank());
                    break;
                case IN_SITU:
//                    ranks.put(dt, condTO.getInSituMaxRank());
                    break;
                case RNA_SEQ:
//                    ranks.put(dt, condTO.getRnaSeqMaxRank());
                    break;
                default:
                  throw log.throwing(new IllegalStateException("Unsupported DataType: " + dt));
            }
        }
        if (ranks.isEmpty()) {
            ranks = null;
        }
        return log.exit(new Condition(condTO.getAnatEntityId(), condTO.getStageId(),
            condTO.getSpeciesId(), ranks));
    }
    

}
