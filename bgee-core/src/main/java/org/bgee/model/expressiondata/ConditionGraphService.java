package org.bgee.model.expressiondata;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.CommonService;
import org.bgee.model.ServiceFactory;
import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.anatdev.DevStage;
import org.bgee.model.anatdev.Sex;
import org.bgee.model.anatdev.Strain;
import org.bgee.model.expressiondata.Condition.ConditionEntities;
import org.bgee.model.ontology.Ontology;
import org.bgee.model.ontology.RelationType;
import org.bgee.model.species.Species;

/**
 * A {@code Service} for {@code ConditionGraph}.
 *
 * @author  Frederic Bastian
 * @version Bgee 14, Oct. 2018
 * @since   Bgee 14, Oct. 2018
 */
public class ConditionGraphService extends CommonService {
    private final static Logger log = LogManager.getLogger(ConditionGraphService.class.getName());

    /**
     * @param serviceFactory            The {@code ServiceFactory} to be used to obtain {@code Service}s 
     *                                  and {@code DAOManager}.
     * @throws IllegalArgumentException If {@code serviceFactory} is {@code null}.
     */
    public ConditionGraphService(ServiceFactory serviceFactory) {
        super(serviceFactory);
    }

    /** 
     * @param conditions        A {@code Collection} of {@code Condition}s that will be managed 
     *                          by the returned {@code ConditionGraph}.
     * @throws IllegalArgumentException If any of the arguments is {@code null} or empty, 
     *                                  or if the anat. entity or dev. stage of a {@code Condition} 
     *                                  does not exist in the requested species.
     */
    public ConditionGraph loadConditionGraph(Collection<Condition> conditions) {
        log.traceEntry("{}", conditions);
        return log.traceExit(this.loadConditionGraphFromMultipleArgs(conditions, false, false, null, null,
                null, null, null));
    }
    
    /**
     * @param conditions            A {@code Collection} of {@code Condition}s that will be managed 
     *                              by the returned {@code ConditionGraph}.
     * @param inferAncestralConds   A {@code boolean} defining whether the ancestral conditions
     *                              should be inferred.
     * @param inferDescendantConds  A {@code boolean} defining whether the descendant conditions
     *                              should be inferred.
     * @throws IllegalArgumentException If any of the arguments is {@code null} or empty, 
     *                                  or if {@code Condition}s does not exist in the same species.
     */
    public ConditionGraph loadConditionGraph(Collection<Condition> conditions, boolean inferAncestralConds,
            boolean inferDescendantConds) throws IllegalArgumentException {
        log.traceEntry("{}, {}, {}", conditions, inferAncestralConds, inferDescendantConds);
        return log.traceExit(this.loadConditionGraphFromMultipleArgs(conditions, inferAncestralConds,
                inferDescendantConds, null, null, null, null, null));
    }
    
    /**
     * @param conditions            A {@code Collection} of {@code Condition}s that will be managed 
     *                              by the returned {@code ConditionGraph}.
     * @param anatEntityOnt         An {@code Ontology} of {@code AnatEntity}s that is 
     *                              the ontology of anatomical entities of a single species.
     * @param devStageOnt           An {@code Ontology} of {@code DevStage}s that is 
     *                              the ontology of developmental stages of a single species.
     * @param cellTypeOnt           An {@code Ontology} of {@code AnatEntity}s that is 
     *                              the ontology of cell types of a single species.
     *                              If {@code null}, the constructor retrieves the ontology.  
     * @param sexOnt                An {@code Ontology} of {@code Sex}s that is 
     *                              the ontology of sexes of a single species.
     *                              If {@code null}, the constructor retrieves the ontology.  
     * @param strainOnt            An {@code Ontology} of {@code Strain}s that is 
     *                              the ontology of strains of a single species.
     *                              If {@code null}, the constructor retrieves the ontology.  
     * @throws IllegalArgumentException If any of the arguments is {@code null} or empty, 
     *                                  or if {@code Condition}s does not exist in the same species.
     */
    public ConditionGraph loadConditionGraph(Collection<Condition> conditions, Ontology<AnatEntity, String> anatEntityOnt,
            Ontology<DevStage, String> devStageOnt, Ontology<AnatEntity, String> cellTypeOnt, 
            Ontology<Sex, String> sexOnt, Ontology<Strain, String> strainOnt) 
                    throws IllegalArgumentException {
        log.traceEntry("{}, {}, {}, {}, {}, {}", conditions, anatEntityOnt, devStageOnt, cellTypeOnt, 
                sexOnt, strainOnt);
        return log.traceExit(this.loadConditionGraphFromMultipleArgs(conditions, false, false, anatEntityOnt, devStageOnt,
                cellTypeOnt, sexOnt, strainOnt));
    }
    
    /**
     * @param conditions            A {@code Collection} of {@code Condition}s that will be managed 
     *                              by the returned {@code ConditionGraph}.
     * @param inferAncestralConds   A {@code boolean} defining whether the ancestral conditions
     *                              should be inferred.
     * @param inferDescendantConds  A {@code boolean} defining whether the descendant conditions
     *                              should be inferred.
     * @param anatEntityOnt         An {@code Ontology} of {@code AnatEntity}s that is 
     *                              the ontology of anatomical entities of a single species.
     *                              If {@code null}, the constructor retrieves the ontology.  
     * @param devStageOnt           An {@code Ontology} of {@code DevStage}s that is 
     *                              the ontology of developmental stages of a single species.
     *                              If {@code null}, the constructor retrieves the ontology.  
     * @param cellTypeOnt           An {@code Ontology} of {@code AnatEntity}s that is 
     *                              the ontology of cell types of a single species.
     *                              If {@code null}, the constructor retrieves the ontology.  
     * @param sexOnt                An {@code Ontology} of {@code Sex}s that is 
     *                              the ontology of sexes of a single species.
     *                              If {@code null}, the constructor retrieves the ontology.  
     * @param strainOnt            An {@code Ontology} of {@code Strain}s that is 
     *                              the ontology of strains of a single species.
     *                              If {@code null}, the constructor retrieves the ontology.  
     * @throws IllegalArgumentException If any of the arguments is {@code null} or empty, 
     *                                  or if {@code Condition}s does not exist in the same species.
     */
    public ConditionGraph loadConditionGraph(Collection<Condition> conditions, boolean inferAncestralConds,
            boolean inferDescendantConds, Ontology<AnatEntity, String> anatEntityOnt,
            Ontology<DevStage, String> devStageOnt, Ontology<AnatEntity, String> cellTypeOnt,
            Ontology<Sex, String> sexOnt, Ontology<Strain, String> strainOnt) 
                    throws IllegalArgumentException {
        log.traceEntry("{}, {}, {}, {}, {}, {}, {}, {}", conditions, inferAncestralConds, inferDescendantConds, 
                anatEntityOnt, devStageOnt, cellTypeOnt, sexOnt, strainOnt);
        return log.traceExit(this.loadConditionGraphFromMultipleArgs(conditions, inferAncestralConds, inferDescendantConds,
                anatEntityOnt, devStageOnt, cellTypeOnt, sexOnt, strainOnt));
    }

    /**
     * Load a {@code ConditionGraph} by retrieving all {@code Condition}s existing in a species.
     * 
     * @param speciesId             An {@code int} that is the ID of a species for which
     *                              the {@code ConditionGraph} should be loaded.
     * @param condParameters        A {@code Collection} of {@code CallService.Attribute}s
     *                              that are condition parameters (
     *                              {@link CallService.Attribute#isConditionParameter()} returns {@code true}
     *                              for all of them), specifying the parameters
     *                              of the {@code Condition}s that should be loaded.
     * @return                      A {@code ConditionGraph} for the requested species.
     * @throws IllegalArgumentException If {@code speciesId} is less than or equal to 0, or if
     *                                  {@code condParameters} is {@code null}, empty, or contains
     *                                  {@code CallService.Attribute}s that are not condition parameters.
     */
    public ConditionGraph loadConditionGraph(int speciesId, Collection<CallService.Attribute> condParameters)
            throws IllegalArgumentException {
        log.traceEntry("{}, {}", speciesId, condParameters);
        if (speciesId <= 0) {
            throw log.throwing(new IllegalArgumentException("A speciesId must be provided."));
        }
        if (condParameters == null || condParameters.isEmpty() ||
                condParameters.stream().anyMatch(a -> !a.isConditionParameter())) {
            throw log.throwing(new IllegalArgumentException("Condition parameters must be provided."));
        }

        Set<Species> species = this.getServiceFactory().getSpeciesService().loadSpeciesByIds(
                Collections.singleton(speciesId), false);
        if (species.isEmpty()) {
            throw log.throwing(new IllegalArgumentException(
                    "The provided speciesId does not correspond to any species in the data source: " + speciesId));
        }

        Set<Condition> conditions = new HashSet<>(
                loadGlobalConditionMap(
                    species,
                    convertCondParamAttrsToCondDAOAttrs(condParameters),
                    null,
                    this.getDaoManager().getConditionDAO(),
                    this.getServiceFactory().getAnatEntityService(),
                    this.getServiceFactory().getDevStageService(),
                    this.getServiceFactory().getSexService(),
                    this.getServiceFactory().getStrainService()
                ).values());

        return log.traceExit(this.loadConditionGraphFromMultipleArgs(conditions, false, false, null, null, 
                null, null, null));
    }

    /**
     * Constructor accepting all parameters.  
     * 
     * @param conditions            A {@code Collection} of {@code Condition}s that will be managed 
     *                              by the returned {@code ConditionGraph}.
     * @param inferAncestralConds   A {@code boolean} defining whether the ancestral conditions
     *                              should be inferred.
     * @param inferDescendantConds  A {@code boolean} defining whether the descendant conditions
     *                              should be inferred.
     * @param anatEntityOnt         An {@code Ontology} of {@code AnatEntity}s that is 
     *                              the ontology of anatomical entities of a single species.
     *                              If {@code null}, this method retrieves the ontology.  
     * @param devStageOnt           An {@code Ontology} of {@code DevStage}s that is 
     *                              the ontology of developmental stages of a single species.
     *                              If {@code null}, this method retrieves the ontology.  
     * @param cellTypeOnt           An {@code Ontology} of {@code AnatEntity}s that is 
     *                              the ontology of cell types of a single species.
     *                              If {@code null}, this method retrieves the ontology. 
     * @param sexOnt                An {@code Ontology} of {@code Sex}s that is 
     *                              the ontology of sexes. If {@code null}, this method retrieves the ontology.  
     * @param strainOnt             An {@code Ontology} of {@code Strain}s that is 
     *                              the ontology of strains. If {@code null}, this method retrieves the ontology.  
     * @throws IllegalArgumentException If {@code conditions} is {@code null} or empty, 
     *                                  or if the {@code Condition}s does not exist in the same species.
     */
    //XXX: we'll see what we'll do for multi-species later, for now we only accept a single species. 
    //I guess multi-species would need a separate class, e.g., MultiSpeciesConditionUtils.
    //TODO: unit test for ancestral condition inferences
    //TODO: refactor this constructor, methods getAncestorConditions and getDescendantConditions
    private ConditionGraph loadConditionGraphFromMultipleArgs(Collection<Condition> conditions, 
            boolean inferAncestralConds, boolean inferDescendantConds,
            Ontology<AnatEntity, String> anatEntityOnt, Ontology<DevStage, String> devStageOnt,
            Ontology<AnatEntity, String> cellTypeOnt, Ontology<Sex, String> sexOnt, 
            Ontology<Strain, String> strainOnt)
                    throws IllegalArgumentException {
        log.traceEntry("{}, {}, {}, {}, {}, {}, {}, {}", conditions, inferAncestralConds, inferDescendantConds, 
                anatEntityOnt, devStageOnt, cellTypeOnt, sexOnt, strainOnt);
    
        long startTimeInMs = System.currentTimeMillis();
        log.debug("Start creation of ConditionGraph");
        if (conditions == null || conditions.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("Some conditions must be provided."));
        }
    
        Set<Condition> tempConditions = new HashSet<>(conditions);
        
        ConditionEntities entities = new ConditionEntities(tempConditions);
        if (entities.getSpeciesIds().size() != 1) {
            throw log.throwing(new IllegalArgumentException("Conditions should be in the same species."));
        }
        Integer speciesId = entities.getSpeciesIds().iterator().next();

        // generate ontologies to use
        final Ontology<AnatEntity, String> anatEntityOntToUse = entities.getAnatEntityIds().isEmpty()? null:
            anatEntityOnt != null? anatEntityOnt: 
                this.getServiceFactory().getOntologyService().getAnatEntityOntology(
                    speciesId, entities.getAnatEntityIds(), EnumSet.of(RelationType.ISA_PARTOF), 
                    inferAncestralConds, inferDescendantConds);
        final Ontology<DevStage, String> devStageOntToUse = entities.getDevStageIds().isEmpty()? null:
            devStageOnt != null? devStageOnt: 
                this.getServiceFactory().getOntologyService().getDevStageOntology(
                    speciesId, entities.getDevStageIds(), inferAncestralConds, inferDescendantConds);
        if (anatEntityOntToUse != null && devStageOntToUse != null 
                && anatEntityOntToUse.getSpeciesId() != devStageOntToUse.getSpeciesId()) {
            throw log.throwing(new IllegalArgumentException("Ontologies should be in the same species."));
        }
        final Ontology<AnatEntity, String> cellTypeOntToUse = entities.getCellTypeIds().isEmpty()? null:
            cellTypeOnt != null? cellTypeOnt: 
                this.getServiceFactory().getOntologyService().getAnatEntityOntology(
                    speciesId, entities.getCellTypeIds(), EnumSet.of(RelationType.ISA_PARTOF), 
                    inferAncestralConds, inferDescendantConds);
        if (anatEntityOntToUse != null && cellTypeOntToUse != null 
                && anatEntityOntToUse.getSpeciesId() != cellTypeOntToUse.getSpeciesId()) {
            throw log.throwing(new IllegalArgumentException("Ontologies should be in the same species."));
        }
        final Ontology<Sex, String> sexOntToUse = entities.getSexIds().isEmpty()? null:
            sexOnt != null? sexOnt: 
                this.getServiceFactory().getOntologyService().getSexOntology(speciesId, 
                        entities.getSexIds(), inferAncestralConds, inferDescendantConds);
        if (anatEntityOntToUse != null && sexOntToUse != null 
                && anatEntityOntToUse.getSpeciesId() != sexOntToUse.getSpeciesId()) {
            throw log.throwing(new IllegalArgumentException("Ontologies should be in the same species."));
        }
        final Ontology<Strain, String> strainOntToUse = entities.getStrainIds().isEmpty()? null:
            strainOnt != null? strainOnt: 
                this.getServiceFactory().getOntologyService().getStrainOntology(speciesId, 
                        entities.getStrainIds(), inferAncestralConds, inferDescendantConds);
        if (anatEntityOntToUse != null && strainOntToUse != null 
                && anatEntityOntToUse.getSpeciesId() != strainOntToUse.getSpeciesId()) {
            throw log.throwing(new IllegalArgumentException("Ontologies should be in the same species."));
        }
        
        //TODO: test inference of descendant conditions
        if (inferAncestralConds || inferDescendantConds) {
            Set<Condition> newPropagatedConditions = tempConditions.stream().flatMap(cond -> {
                Set<DevStage> propStages = new HashSet<>();
                propStages.add(cond.getDevStage());
                if (devStageOntToUse != null && cond.getDevStageId() != null) {
                    if (inferAncestralConds) {
                        propStages.addAll(devStageOntToUse.getAncestors(cond.getDevStage()));
                    }
                    if (inferDescendantConds) {
                        propStages.addAll(devStageOntToUse.getDescendants(cond.getDevStage()));
                    }
                }
                
                Set<AnatEntity> propAnatEntitys = new HashSet<>();
                propAnatEntitys.add(cond.getAnatEntity());
                if (anatEntityOntToUse != null && cond.getAnatEntityId() != null) {
                    if (inferAncestralConds) {
                        propAnatEntitys.addAll(anatEntityOntToUse.getAncestors(cond.getAnatEntity()));
                    }
                    if (inferDescendantConds) {
                        propAnatEntitys.addAll(anatEntityOntToUse.getDescendants(cond.getAnatEntity()));
                    }
                }
                
                Set<AnatEntity> propCellTypes = new HashSet<>();
                propCellTypes.add(cond.getCellType());
                if (cellTypeOntToUse != null && cond.getCellTypeId() != null) {
                    if (inferAncestralConds) {
                        propCellTypes.addAll(cellTypeOntToUse.getAncestors(cond.getCellType()));
                    }
                    if (inferDescendantConds) {
                        propCellTypes.addAll(cellTypeOntToUse.getDescendants(cond.getCellType()));
                    }
                }
                
                Set<Sex> propSexes = new HashSet<>();
                propSexes.add(cond.getSex());
                if (sexOntToUse != null && cond.getSexId() != null) {
                    if (inferAncestralConds) {
                        propSexes.addAll(sexOntToUse.getAncestors(cond.getSex()));
                    }
                    if (inferDescendantConds) {
                        propSexes.addAll(sexOntToUse.getDescendants(cond.getSex()));
                    }
                }
                
                Set<Strain> propStrains = new HashSet<>();
                propStrains.add(cond.getStrain());
                if (strainOntToUse != null && cond.getStrainId() != null) {
                    if (inferAncestralConds) {
                        propStrains.addAll(strainOntToUse.getAncestors(cond.getStrain()));
                    }
                    if (inferDescendantConds) {
                        propStrains.addAll(strainOntToUse.getDescendants(cond.getStrain()));
                    }
                }
                
                return propAnatEntitys.stream()
                        .flatMap(propAnatEntity -> propStages.stream()
                                .flatMap(propStage -> propCellTypes.stream()
                                        .flatMap( propCellType -> propSexes.stream()
                                                .flatMap( propSexe -> propStrains.stream().map( propStrain ->
                            new Condition(propAnatEntity, propStage, propCellType, propSexe, propStrain, cond.getSpecies()))))))
                        .filter(propCond -> !cond.equals(propCond));
    
            }).collect(Collectors.toSet());
            
            tempConditions.addAll(newPropagatedConditions);
        }
    
        ConditionGraph condGraph = new ConditionGraph(tempConditions, inferAncestralConds, inferDescendantConds,
                anatEntityOntToUse, devStageOntToUse, cellTypeOnt, sexOntToUse, strainOntToUse);
        log.debug("ConditionGraph created in {} ms", System.currentTimeMillis() - startTimeInMs);
        return log.traceExit(condGraph);
    }
}
