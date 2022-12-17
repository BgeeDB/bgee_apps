package org.bgee.model.expressiondata.call;

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
import org.bgee.model.dao.api.expressiondata.ConditionDAO;
import org.bgee.model.expressiondata.call.Condition.ConditionEntities;
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
        return log.traceExit(this.loadConditionGraphFromMultipleArgs(conditions, false, false,
                anatEntityOnt, devStageOnt,
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
        return log.traceExit(this.loadConditionGraphFromMultipleArgs(conditions,
                inferAncestralConds, inferDescendantConds,
                anatEntityOnt, devStageOnt, cellTypeOnt, sexOnt, strainOnt));
    }

    /**
     * Load a {@code ConditionGraph} by retrieving all {@code Condition}s for requested species
     * and {@code ConditionFilter}s, populated with the requested condition parameter attributes.
     *
     * @param speciesIds        A {@code Collection} of {@code Integer}s that are the IDs of species
     *                          for which the {@code ConditionGraph} should be loaded.
     *                          Can be {@code null} or empty to request for all species in Bgee.
     * @param conditionFilters  A {@code Collection} of {@code ConditionFilter}s allowing
     *                          to parameterize the retrieval of {@code Condition}s
     * @param condParameters    A {@code Collection} of {@code CallService.Attribute}s
     *                          that are condition parameters (
     *                          {@link CallService.Attribute#isConditionParameter()} returns {@code true}
     *                          for all of them), specifying the parameters
     *                          of the {@code Condition}s that should be loaded.
     * @return                  A {@code ConditionGraph} for the requested species.
     * @throws IllegalArgumentException If some species IDs are not recognized,
     *                                  or if {@code condParameters} contains
     *                                  {@code CallService.Attribute}s that are not condition parameters.
     * @see #loadConditionGraph(Collection, Collection, Collection)
     */
    public ConditionGraph loadConditionGraphFromSpeciesIds(Collection<Integer> speciesIds,
            Collection<ConditionFilter> conditionFilters,
            Collection<CallService.Attribute> condParameters) throws IllegalArgumentException {
        log.traceEntry("{}, {}, {}", speciesIds, conditionFilters, condParameters);

        Set<Integer> speciesIdSet = speciesIds != null? new HashSet<>(speciesIds): new HashSet<>();
        Set<Species> species = this.getServiceFactory().getSpeciesService().loadSpeciesByIds(
                speciesIds, false);
        if (!speciesIdSet.isEmpty() && speciesIdSet.size() != species.size()) {
            Set<Integer> foundSpeciesIds = species.stream().map(s -> s.getId()).collect(Collectors.toSet());
            Set<Integer> unrecognizedSpeciesIds = speciesIdSet.stream()
                    .filter(id -> !foundSpeciesIds.contains(id))
                    .collect(Collectors.toSet());
            throw log.throwing(new IllegalArgumentException(
                    "These species IDs does not correspond to any species in the data source: "
                    + unrecognizedSpeciesIds));
        }
        return log.traceExit(this.loadConditionGraph(species, conditionFilters, condParameters));
    }
    /**
     * Load a {@code ConditionGraph} by retrieving all {@code Condition}s for requested species
     * and {@code ConditionFilter}s, populated with the requested condition parameter attributes.
     *
     * @param speciesIds        A {@code Collection} of {@code Species} that are the species
     *                          for which the {@code ConditionGraph} should be loaded.
     *                          Can be {@code null} or empty to request for all species in Bgee.
     * @param conditionFilters  A {@code Collection} of {@code ConditionFilter}s allowing
     *                          to parameterize the retrieval of {@code Condition}s
     * @param condParameters    A {@code Collection} of {@code CallService.Attribute}s
     *                          that are condition parameters (
     *                          {@link CallService.Attribute#isConditionParameter()} returns {@code true}
     *                          for all of them), specifying the parameters
     *                          of the {@code Condition}s that should be loaded.
     * @return                  A {@code ConditionGraph} for the requested species.
     * @throws IllegalArgumentException If some species IDs are not recognized,
     *                                  or if {@code condParameters} contains
     *                                  {@code CallService.Attribute}s that are not condition parameters.
     * @see #loadConditionGraph(Collection, Collection, Collection)
     */
    public ConditionGraph loadConditionGraph(Collection<Species> species,
            Collection<ConditionFilter> conditionFilters,
            Collection<CallService.Attribute> condParameters) throws IllegalArgumentException {
        log.traceEntry("{}, {}, {}", species, conditionFilters, condParameters);

        if (condParameters != null && condParameters.stream().anyMatch(a -> !a.isConditionParameter())) {
            throw log.throwing(new IllegalArgumentException("Only condition parameters must be provided."));
        }
        EnumSet<ConditionDAO.Attribute> daoCondParams = convertCondParamAttrsToCondDAOAttrs(
                condParameters);
        Set<Species> speciesSet = species == null || species.isEmpty()?
                this.getServiceFactory().getSpeciesService().loadSpeciesByIds(
                        null, false):
                new HashSet<>(species);

        Set<Condition> conditions = new HashSet<>(
                loadGlobalConditionMap(
                    speciesSet,
                    generateDAOConditionFilters(conditionFilters, daoCondParams),
                    daoCondParams,
                    this.getDaoManager().getConditionDAO(),
                    this.getServiceFactory().getAnatEntityService(),
                    this.getServiceFactory().getDevStageService(),
                    this.getServiceFactory().getSexService(),
                    this.getServiceFactory().getStrainService()
                ).values());

        return log.traceExit(this.loadConditionGraph(conditions));
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
        if (conditions.stream().anyMatch(c -> c == null)) {
            throw log.throwing(new IllegalArgumentException("No condition can be null."));
        }
    
        Set<Condition> tempConditions = new HashSet<>(conditions);
        
        ConditionEntities entities = new ConditionEntities(tempConditions);
        if (entities.getSpeciesIds().size() != 1) {
            throw log.throwing(new IllegalArgumentException("Conditions should be in the same species."));
        }
        Integer speciesId = entities.getSpeciesIds().iterator().next();

        // generate ontologies to use
        //FIXME: to improve, here the anat entity ontology is requested twice,
        //which is costly (once for the anat entites, once for the cell types).
        //And it will have been also requested once before to load the first conditions passed
        //as argument... at the very least we could avoid requesting again the objects present
        //in the conditions (AnatEntity, etc).
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
                this.getServiceFactory().getOntologyService().getCellTypeOntology(
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
            //We don't want to propagate to non-informative anat. entities,
            //except to the root of the anat. entities and the root of the cell types.
            //Of note, non-informative anat. entities used in annotations are not retrieved
            //by the method loadNonInformativeAnatEntitiesBySpeciesIds.
            Set<AnatEntity> nonInformativeAnatEntities = this.getServiceFactory().getAnatEntityService()
                    .loadNonInformativeAnatEntitiesBySpeciesIds(Collections.singleton(speciesId))
                    .collect(Collectors.toSet());
            AnatEntity rootAnatEntity = new AnatEntity(ConditionDAO.ANAT_ENTITY_ROOT_ID);
            Set<AnatEntity> anatNonInformatives = new HashSet<>(nonInformativeAnatEntities);
            anatNonInformatives.remove(rootAnatEntity);
            AnatEntity rootCellType = new AnatEntity(ConditionDAO.CELL_TYPE_ROOT_ID);
            Set<AnatEntity> cellTypeNonInformatives = new HashSet<>(nonInformativeAnatEntities);
            cellTypeNonInformatives.remove(rootCellType);

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
                
                Set<AnatEntity> propAnatEntities = new HashSet<>();
                if (anatEntityOntToUse != null && cond.getAnatEntityId() != null) {
                    if (inferAncestralConds) {
                        propAnatEntities.addAll(anatEntityOntToUse.getAncestors(cond.getAnatEntity()));
                    }
                    if (inferDescendantConds) {
                        propAnatEntities.addAll(anatEntityOntToUse.getDescendants(cond.getAnatEntity()));
                    }
                    //Remove terms we don't want to propagate to
                    propAnatEntities.removeAll(anatNonInformatives);
                }
                //to make sure we don't exclude the annotated term, we add it afterwards
                propAnatEntities.add(cond.getAnatEntity());
                
                Set<AnatEntity> tempPropCellTypes = new HashSet<>();
                if (cellTypeOntToUse != null && cond.getCellTypeId() != null) {
                    if (inferAncestralConds) {
                        tempPropCellTypes.addAll(cellTypeOntToUse.getAncestors(cond.getCellType()));
                    }
                    if (inferDescendantConds) {
                        tempPropCellTypes.addAll(cellTypeOntToUse.getDescendants(cond.getCellType()));
                    }
                    //Remove terms we don't want to propagate to
                    tempPropCellTypes.removeAll(cellTypeNonInformatives);
                }
                //to make sure we don't exclude the annotated term, we add it afterwards
                tempPropCellTypes.add(cond.getCellType());
                Set<AnatEntity> propCellTypes = tempPropCellTypes;
                
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
                
                return propAnatEntities.stream()
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
                anatEntityOntToUse, devStageOntToUse, cellTypeOntToUse, sexOntToUse, strainOntToUse);
        log.debug("ConditionGraph created in {} ms", System.currentTimeMillis() - startTimeInMs);
        return log.traceExit(condGraph);
    }
}
