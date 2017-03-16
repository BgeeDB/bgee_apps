package org.bgee.model;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.anatdev.DevStage;
import org.bgee.model.dao.api.expressiondata.ConditionDAO.ConditionTO;
import org.bgee.model.dao.api.gene.GeneDAO.GeneTO;
import org.bgee.model.expressiondata.Condition;
import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.model.gene.Gene;
import org.bgee.model.species.Species;

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
    
    //NOTE: there shouldn't be any ConditionService for now
    //This method should rather map selected CallService attributes to ConditionDAO.Attribute
//    protected static Set<ConditionDAO.Attribute> convertConditionServiceAttrsToConditionDAOAttrs(
//        Collection<ConditionService.Attribute> attributes) {
//        log.entry(attributes);
//
//        return log.exit(attributes.stream().map(attr -> {
//            switch (attr) {
//                case ANAT_ENTITY_ID: 
//                    return ConditionDAO.Attribute.ANAT_ENTITY_ID;
//                case DEV_STAGE_ID: 
//                    return ConditionDAO.Attribute.STAGE_ID;
//                case SPECIES_ID:
//                    return ConditionDAO.Attribute.SPECIES_ID;
//                default: 
//                    throw log.throwing(new IllegalStateException(
//                        "Unsupported Attributes from ConditionService: " + attr));
//            }
//        }).collect(Collectors.toCollection(() -> EnumSet.noneOf(ConditionDAO.Attribute.class))));
//    }
    /**
     * Map {@code ConditionTO} to a {@code Condition}. In case of single-species retrieval
     * of {@code ConditionTO}s, see {@link #mapConditionTOToCondition(ConditionTO, Integer)}.
     * 
     * @param condTO        A {@code ConditionTO} that is the condition from db
     *                      to map into {@code Condition}.
     * @param anatEntity    The {@code AnatEntity} corresponding to the ID returned by
     *                      {@link ConditionTO#getAnatEntityId()}.
     * @param devStage      The {@code DevStage} corresponding to the ID returned by
     *                      {@link ConditionTO#getStageId()}.
     * @return              The mapped {@code Condition}.
     */
    protected static Condition mapConditionTOToCondition(ConditionTO condTO,
            AnatEntity anatEntity, DevStage devStage) {
        log.entry(condTO, anatEntity, devStage);
        return log.exit(mapConditionTOToCondition(condTO, null, anatEntity, devStage));
    }
    /**
     * Map {@code ConditionTO} to a {@code Condition}.
     * 
     * @param condTO        A {@code ConditionTO} that is the condition from db
     *                      to map into {@code Condition}.
     * @param speciesId     An {@code Integer} that is the ID of the species for which
     *                      the {@code ConditionTO}s were retrieved. Allows to avoid requesting
     *                      this attribute from the {@code ConditionDAO} if only one species was requested.
     * @param anatEntity    The {@code AnatEntity} corresponding to the ID returned by
     *                      {@link ConditionTO#getAnatEntityId()}.
     * @param devStage      The {@code DevStage} corresponding to the ID returned by
     *                      {@link ConditionTO#getStageId()}.
     * @return              The mapped {@code Condition}.
     */
    protected static Condition mapConditionTOToCondition(ConditionTO condTO, Integer speciesId,
            AnatEntity anatEntity, DevStage devStage) {
        log.entry(condTO, speciesId, anatEntity, devStage);
        if (condTO == null) {
            return log.exit(null);
        }
        if (speciesId != null && speciesId <= 0) {
            throw log.throwing(new IllegalArgumentException("Invalid speciesId: " + speciesId));
        }
        Map<DataType, BigDecimal> ranks = new HashMap<>();
        for (DataType dt: DataType.values()) {
            switch (dt) {
                case AFFYMETRIX:
                    ranks.put(dt, condTO.getAffymetrixMaxRank());
                    break;
                case EST:
                    ranks.put(dt, condTO.getESTMaxRank());
                    break;
                case IN_SITU:
                    ranks.put(dt, condTO.getInSituMaxRank());
                    break;
                case RNA_SEQ:
                    ranks.put(dt, condTO.getRNASeqMaxRank());
                    break;
                default:
                  throw log.throwing(new IllegalStateException("Unsupported DataType: " + dt));
            }
        }
        if (ranks.isEmpty()) {
            ranks = null;
        }
        //FIXME: implements use of propagatedMaxRanks
        return log.exit(new Condition(anatEntity, devStage,
            speciesId != null? speciesId: condTO.getSpeciesId(), ranks, null));
    }
    protected static ConditionTO mapConditionToConditionTO(int condId, int exprMappedCondId, 
            Condition cond) {
        log.entry(condId, exprMappedCondId, cond);
        
        Map<DataType, BigDecimal> ranksByDataType = cond.getMaxRanksByDataType() == null?
                new HashMap<>(): cond.getMaxRanksByDataType();
                
        return log.exit(new ConditionTO(condId, exprMappedCondId, 
                cond.getAnatEntityId(), cond.getDevStageId(), cond.getSpeciesId(), 
                ranksByDataType.get(DataType.AFFYMETRIX), ranksByDataType.get(DataType.RNA_SEQ), 
                ranksByDataType.get(DataType.EST), ranksByDataType.get(DataType.IN_SITU)));
    }
    
    /**
     * Map {@code GeneTO} to a {@code Gene}.
     * 
     * @param geneTO    A {@code GeneTO} that is the condition from data source
     *                  to map into {@code Gene}.
     * @param species   A {@code Species} that is the species of the gene.
     * @return          The mapped {@code Gene}.
     */
    protected static Gene mapGeneTOToGene(GeneTO geneTO, Species species) {
        log.entry(geneTO, species);
        if (geneTO == null) {
            return log.exit(null);
        }
        if (species == null) {
            throw log.throwing(new IllegalArgumentException("A Species must be provided."));
        }
        if (geneTO.getSpeciesId() != null && !geneTO.getSpeciesId().equals(species.getId())) {
            throw log.throwing(new IllegalArgumentException(
                    "Species ID of the gene does not match provied Species."));
        }
        return log.exit(new Gene(geneTO.getGeneId(), geneTO.getName(), geneTO.getDescription(),
                species, geneTO.getGeneMappedToGeneIdCount()));
    }
}
