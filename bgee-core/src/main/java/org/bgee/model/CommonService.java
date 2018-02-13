package org.bgee.model;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.anatdev.DevStage;
import org.bgee.model.anatdev.TaxonConstraint;
import org.bgee.model.dao.api.anatdev.TaxonConstraintDAO.TaxonConstraintTO;
import org.bgee.model.dao.api.expressiondata.DAODataType;
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
 * @version Bgee 14 Nov. 2017
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
    protected static Condition mapConditionTOToCondition(ConditionTO condTO,
            AnatEntity anatEntity, DevStage devStage, Species species) {
        log.entry(condTO, anatEntity, devStage, species);
        if (condTO == null) {
            return log.exit(null);
        }
        if (species == null) {
            throw log.throwing(new IllegalArgumentException("The Species must be provided."));
        }
        if (condTO.getSpeciesId() != null && !condTO.getSpeciesId().equals(species.getId())) {
            throw log.throwing(new IllegalArgumentException(
                    "Incorrect species ID in ConditionTO, expected " + species.getId() + " but was "
                    + condTO.getSpeciesId()));
        }
        if (condTO.getAnatEntityId() != null && anatEntity != null &&
                !condTO.getAnatEntityId().equals(anatEntity.getId())) {
            throw log.throwing(new IllegalArgumentException(
                    "Incorrect anat. entity ID in ConditionTO, expected " + anatEntity.getId() + " but was "
                    + condTO.getAnatEntityId()));
        }
        if (condTO.getStageId() != null && devStage != null &&
                !condTO.getStageId().equals(devStage.getId())) {
            throw log.throwing(new IllegalArgumentException(
                    "Incorrect dev. stage ID in ConditionTO, expected " + devStage.getId() + " but was "
                    + condTO.getStageId()));
        }
        return log.exit(new Condition(anatEntity, devStage, species));
    }
    protected static ConditionTO mapConditionToConditionTO(int condId, Integer exprMappedCondId,
            Condition cond) {
        log.entry(condId, exprMappedCondId, cond);
                
        return log.exit(new ConditionTO(condId, exprMappedCondId, 
                cond.getAnatEntityId(), cond.getDevStageId(), cond.getSpeciesId()));
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
                    "Species ID of the gene does not match provided Species."));
        }
        return log.exit(new Gene(geneTO.getGeneId(), geneTO.getName(), geneTO.getDescription(),
                species, geneTO.getGeneMappedToGeneIdCount()));
    }

    /**
     * Map {@link TaxonConstraintTO} to a {@link TaxonConstraint}.
     *
     * @param taxonConstraintTO A {@code TaxonConstraintTO} that is the transfert object to be mapped.
     * @return                  The mapped {@link TaxonConstraint}.
     */
    protected static <T> TaxonConstraint<T> mapTaxonConstraintTOToTaxonConstraint(TaxonConstraintTO<T> taxonConstraintTO) {
        log.entry(taxonConstraintTO);
        if (taxonConstraintTO == null) {
            return log.exit(null);
        }

        return log.exit(new TaxonConstraint<T>(
                taxonConstraintTO.getEntityId(), taxonConstraintTO.getSpeciesId()));
    }

    protected static DataType convertDaoDataTypeToDataType(DAODataType dt) {
        log.entry(dt);
        switch(dt) {
            case AFFYMETRIX:
                return log.exit(DataType.AFFYMETRIX);
            case EST:
                return log.exit(DataType.EST);
            case IN_SITU:
                return log.exit(DataType.IN_SITU);
            case RNA_SEQ:
                return log.exit(DataType.RNA_SEQ);
        default:
            throw log.throwing(new IllegalStateException("Unsupported SourceToSpeciesTO.DataType: " + dt));
        }
    }
}