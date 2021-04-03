package org.bgee.model.expressiondata;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.anatdev.DevStage;
import org.bgee.model.anatdev.Sex;
import org.bgee.model.anatdev.Strain;
import org.bgee.model.expressiondata.Condition.ConditionEntities;
import org.bgee.model.ontology.Ontology;

/**
 * Class providing convenience operations on {@link Condition}s.
 * <p>
 * When this class is instantiated, the constructor retrieves ontologies for provided 
 * {@code Condition}s if they are not provided.
 * 
 * @author  Frederic Bastian
 * @author  Valentine Rech de Laval
 * @version Bgee 14, Oct. 2018
 * @see ConditionGraphService
 * @since   Bgee 13, Dec. 2015
 */
//TODO: Actually, maybe we should have an UtilsFactory, as we have a ServiceFactory. 
//Could return also the ExpressionCallUtils, etc. 
//that would be much cleaner for unit tests.
//Note: there is now a ConditionGraphService, but I let this comment here for other classes.
public class ConditionGraph {
    private static final Logger log = LogManager.getLogger(ConditionGraph.class.getName());

    /**
     * A {@code Map} associating IDs of {@code Condition}s as key to the corresponding {@code Condition} as value.
     */
    private final Set<Condition> conditions;
    /**
     * @see #getAnatEntityOntology()
     */
    private final Ontology<AnatEntity, String> anatEntityOnt;
    /**
     * @see #getDevStageOntology()
     */
    private final Ontology<DevStage, String> devStageOnt;
    /**
     * @see #getCellTypeOntology()
     */
    private final Ontology<AnatEntity, String> cellTypeOnt;
    /**
     * @see #getSexOntology()
     */
    private final Ontology<Sex, String> sexOnt;
    /**
     * @see #getStrainOntology()
     */
    private final Ontology<Strain, String> strainOnt;
    /**
     * @see #isInferredAncestralConditions()
     */
    private final boolean inferAncestralConditions;
    /**
     * @see #isInferredDescendantConditions()
     */
    private final boolean inferDescendantConditions;
    /**
     * Constructor accepting all parameters.  
     * 
     * @param conditions            A {@code Collection} of {@code Condition}s that will be managed 
     *                              by this {@code ConditionGraph}.
     * @param inferAncestralConds   A {@code boolean} defining whether the ancestral conditions
     *                              were inferred.
     * @param inferDescendantConds  A {@code boolean} defining whether the descendant conditions
     *                              were inferred.
     * @param anatEntityOnt         An {@code Ontology} of {@code AnatEntity}s present
     *                              in the provided {@code Condition}s, if any. Otherwise,
     *                              can be {@code null}.
     * @param devStageOnt           An {@code Ontology} of {@code DevStage}s present
     *                              in the provided {@code Condition}s, if any. Otherwise,
     *                              can be {@code null}.
     * @param cellTypeOnt           An {@code Ontology} of {@code AnatEntity}s corresponding to cell 
     *                              type present in the provided {@code Condition}s, if any. 
     *                              Otherwise, can be {@code null}.
     * @param sexOnt                An {@code Ontology} of {@code Sex}s present
     *                              in the provided {@code Condition}s, if any. Otherwise,
     *                              can be {@code null}.
     * @param strainOnt             An {@code Ontology} of {@code Strain}s present
     *                              in the provided {@code Condition}s, if any. Otherwise,
     *                              can be {@code null}.
     * @throws IllegalArgumentException If {@code conditions} is {@code null} or empty, 
     *                                  or if the provided {@code Condition}s does not exist in the same species,
     *                                  or if the {@code Ontology}s are not provided appropriately,
     *                                  or if some {@code AnatEntity} or {@code DevStage} in {@code conditions}
     *                                  does not exist in the related ontologies.
     */
    //XXX: we'll see what we'll do for multi-species later, for now we only accept a single species. 
    //I guess multi-species would need a separate class, e.g., MultiSpeciesConditionUtils.
    public ConditionGraph(Collection<Condition> conditions, 
            boolean inferAncestralConds, boolean inferDescendantConds,
            Ontology<AnatEntity, String> anatEntityOnt, Ontology<DevStage, String> devStageOnt,
            Ontology<AnatEntity, String> cellTypeOnt, Ontology<Sex, String> sexOnt, 
            Ontology<Strain, String> strainOnt)
                    throws IllegalArgumentException {
        log.traceEntry("{}, {}, {}, {}, {}, {}, {}, {}", conditions, inferAncestralConds, 
                inferDescendantConds, anatEntityOnt, devStageOnt, cellTypeOnt, sexOnt, strainOnt);

        if (conditions == null || conditions.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("Some conditions must be provided."));
        }
        if (anatEntityOnt == null && devStageOnt == null) {
            throw log.throwing(new IllegalArgumentException("Ontologies must be provided."));
        }
        if (anatEntityOnt != null && devStageOnt != null 
                && anatEntityOnt.getSpeciesId() != devStageOnt.getSpeciesId()) {
            throw log.throwing(new IllegalArgumentException("Anat. entities and dev. stage ontologies "
                    + "should be in the same species."));
        }
        if (anatEntityOnt != null && cellTypeOnt != null 
                && anatEntityOnt.getSpeciesId() != cellTypeOnt.getSpeciesId()) {
            throw log.throwing(new IllegalArgumentException("Anat. entities and cell type ontologies "
                    + "should be in the same species."));
        }
        if (strainOnt != null && anatEntityOnt != null 
                && strainOnt.getSpeciesId() != anatEntityOnt.getSpeciesId()) {
            throw log.throwing(new IllegalArgumentException("Anat. entities and strains ontologies "
                    + "should be in the same species."));
        }
        if (sexOnt != null && anatEntityOnt != null 
                && sexOnt.getSpeciesId() != anatEntityOnt.getSpeciesId()) {
            throw log.throwing(new IllegalArgumentException("Anat. entities and sexes ontologies "
                    + "should be in the same species."));
        }

        this.conditions = Collections.unmodifiableSet(new HashSet<>(conditions));

        ConditionEntities entities = new ConditionEntities(this.conditions);
        if (entities.getSpecies().size() != 1) {
            throw log.throwing(new IllegalArgumentException("Conditions should be in the same species."));
        }
        if (anatEntityOnt == null && !entities.getAnatEntityIds().isEmpty()) {
            throw log.throwing(new IllegalArgumentException("Anatomical ontology must be provided."));
        }
        if (devStageOnt == null && !entities.getDevStageIds().isEmpty()) {
            throw log.throwing(new IllegalArgumentException("Dev. stage ontology must be provided."));
        }
        if (cellTypeOnt == null && !entities.getCellTypeIds().isEmpty()) {
            throw log.throwing(new IllegalArgumentException("Cell type ontology must be provided."));
        }
        if (sexOnt == null && !entities.getSexes().isEmpty()) {
            throw log.throwing(new IllegalArgumentException("Sex ontology must be provided."));
        }
        if (strainOnt == null && !entities.getStrains().isEmpty()) {
            throw log.throwing(new IllegalArgumentException("Strain ontology must be provided."));
        }

        this.checkEntityExistence(entities.getDevStageIds(), devStageOnt);
        this.checkEntityExistence(entities.getAnatEntityIds(), anatEntityOnt);
        this.checkEntityExistence(entities.getCellTypeIds(), cellTypeOnt);
        this.checkEntityExistence(entities.getSexIds(), sexOnt);
        //For strain IDs, there can be some upper/lowercase discrepancies,
        //the checkEntityExistence method won't work.
        Set<String> recognizedStrainIdsLowerCase = strainOnt.getElements().stream()
                .map(e -> e.getId().toLowerCase()).collect(Collectors.toSet());
        Set<String> strainIdsLowerCase = entities.getStrainIds().stream()
                .map(s -> s.toLowerCase()).collect(Collectors.toSet());
        if (!recognizedStrainIdsLowerCase.containsAll(strainIdsLowerCase)) {
            Set<String> unrecognizedIds = new HashSet<>(strainIdsLowerCase);
            unrecognizedIds.removeAll(recognizedStrainIdsLowerCase);
            throw log.throwing(new IllegalArgumentException("Some entities do not exist "
                    + "in the provided onology: " + unrecognizedIds));
        }

        this.anatEntityOnt = anatEntityOnt;
        this.devStageOnt = devStageOnt;
        this.cellTypeOnt = cellTypeOnt;
        this.strainOnt = strainOnt;
        this.sexOnt = sexOnt;
        this.inferAncestralConditions = inferAncestralConds;
        this.inferDescendantConditions = inferDescendantConds;
        log.traceExit();
    }
    
    /**
     * Check that all elements in {@code entityIds} are present in {@code ont}, 
     * and only them.
     * 
     * @param entityIds A {@code Set} of {@code String}s that are the IDs of the entities 
     *                  to check for existence in {@code ont}.
     * @param ont       An {@code Ontology} that should contain all elements with their ID 
     *                  in {@code entityIds}.
     * @throws IllegalArgumentException If some elements in {@code entityIds} are not present in 
     *                                  {@code ont}.
     * @param <T>   The type of ID of the elements in the ontology or sub-graph.
     */
    private <T extends Comparable<T>> void checkEntityExistence(Set<String> entityIds, Ontology<?, T> ont) 
            throws IllegalArgumentException {
        log.traceEntry("{}, {}", entityIds, ont);
        
        if (ont == null) {
            log.traceExit(); return;
        }
        
        Set<T> recognizedEntityIds = ont.getElements().stream()
                .map(e -> e.getId()).collect(Collectors.toSet());
        if (!recognizedEntityIds.containsAll(entityIds)) {
            Set<String> unrecognizedIds = new HashSet<>(entityIds);
            unrecognizedIds.removeAll(recognizedEntityIds);
            throw log.throwing(new IllegalArgumentException("Some entities do not exist "
                    + "in the provided onology: " + unrecognizedIds));
        }
        
        log.traceExit();
    }
    
    /**
     * Determines whether the second condition is more precise than the first condition. 
     * "More precise" means that the anatomical structure of {@code secondCond} would be a descendant 
     * of the anatomical structure of {@code firstCond}, and the developmental stage 
     * of {@code secondCond} would be a descendant of the developmental stage of {@code firstCond}.
     * 
     * @param firstCond     The first {@code Condition} to be checked for relations to {@code secondCond}. 
     * @param secondCond    The second {@code Condition} to be checked for relations to {@code firstCond}. 
     * @return              {@code true} if {@code secondCond} is more precise than {@code firstCond}.
     * @throws IllegalArgumentException If one of the provided {@code Condition}s is not registered 
     *                                  to this {@code ConditionGraph}, or one of them is {@code null}.
     */
    public boolean isConditionMorePrecise(Condition firstCond, Condition secondCond) throws IllegalArgumentException {
        log.traceEntry("{}, {}", firstCond, secondCond);

        if (firstCond == null || secondCond == null) {
            throw log.throwing(new IllegalArgumentException("No provided Condition can be null"));
        }
        if (!firstCond.getSpecies().equals(secondCond.getSpecies())) {
            throw log.throwing(new IllegalArgumentException("Conditions are not in the same species."
                    + " First condition: " + firstCond + " - Second condition: " + secondCond));
        }
        if (!this.getConditions().contains(firstCond) || !this.getConditions().contains(secondCond)) {
            throw log.throwing(new IllegalArgumentException("Some of the provided conditions "
                    + "are not registered to this ConditionGraph. First condition: " + firstCond 
                    + " - Second condition: " + secondCond));
        }
        if (firstCond.equals(secondCond)) {
            return log.traceExit(false);
        }
        
        //Of note, computations are faster when the less complex ontologies are used first. 
        //TODO: refactor

        if (this.sexOnt != null &&
                firstCond.getSexId() != null && secondCond.getSexId() != null &&
                !firstCond.getSexId().equals(secondCond.getSexId()) &&
                !this.sexOnt.getAncestors(
                        this.sexOnt.getElement(secondCond.getSexId()))
                .contains(this.sexOnt.getElement(firstCond.getSexId()))) {
            return log.traceExit(false);
        }

        if (this.strainOnt != null &&
                firstCond.getStrainId() != null && secondCond.getStrainId() != null &&
                !firstCond.getStrainId().equals(secondCond.getStrainId()) &&
                !this.strainOnt.getAncestors(
                        this.strainOnt.getElement(secondCond.getStrainId()))
                .contains(this.strainOnt.getElement(firstCond.getStrainId()))) {
            return log.traceExit(false);
        }

        if (this.devStageOnt != null && 
                firstCond.getDevStageId() != null && secondCond.getDevStageId() != null && 
                !firstCond.getDevStageId().equals(secondCond.getDevStageId()) && 
                !this.devStageOnt.getAncestors(
                        this.devStageOnt.getElement(secondCond.getDevStageId()))
                .contains(this.devStageOnt.getElement(firstCond.getDevStageId()))) {
            return log.traceExit(false);
        }

        if (this.cellTypeOnt != null &&
                firstCond.getCellTypeId() != null && secondCond.getCellTypeId() != null &&
                !firstCond.getCellTypeId().equals(secondCond.getCellTypeId()) &&
                !this.cellTypeOnt.getAncestors(
                        this.cellTypeOnt.getElement(secondCond.getCellTypeId()))
                .contains(this.cellTypeOnt.getElement(firstCond.getCellTypeId()))) {
            return log.traceExit(false);
        }
        
        if (this.anatEntityOnt != null && 
                firstCond.getAnatEntityId() != null && secondCond.getAnatEntityId() != null && 
                !firstCond.getAnatEntityId().equals(secondCond.getAnatEntityId()) && 
                !this.anatEntityOnt.getAncestors(
                        this.anatEntityOnt.getElement(secondCond.getAnatEntityId()))
                .contains(this.anatEntityOnt.getElement(firstCond.getAnatEntityId()))) {
            return log.traceExit(false);
        }

        if (firstCond.getSexId() != null && secondCond.getSexId() == null ||
                secondCond.getSexId() != null && firstCond.getSexId() == null) {
            return log.traceExit(false);
        }
        if (firstCond.getStrainId() != null && secondCond.getStrainId() == null ||
                secondCond.getStrainId() != null && firstCond.getStrainId() == null) {
            return log.traceExit(false);
        }
        if (firstCond.getDevStageId() != null && secondCond.getDevStageId() == null || 
                secondCond.getDevStageId() != null && firstCond.getDevStageId() == null) {
            return log.traceExit(false);
        }
        if (firstCond.getCellTypeId() != null && secondCond.getCellTypeId() == null ||
                secondCond.getCellTypeId() != null && firstCond.getCellTypeId() == null) {
            return log.traceExit(false);
        }
        if (firstCond.getAnatEntityId() != null && secondCond.getAnatEntityId() == null || 
                secondCond.getAnatEntityId() != null && firstCond.getAnatEntityId() == null) {
            return log.traceExit(false);
        }
        
        return log.traceExit(true);
    }
    
    /**
     * Get all the {@code Condition}s that are less precise than {@code cond}, 
     * among the {@code Condition}s provided at instantiation. 
     * 
     * @param cond          A {@code Condition} for which we want to retrieve ancestors {@code Condition}s.
     * @return              A {@code Set} of {@code Condition}s that are ancestors of {@code cond}.
     * @throws IllegalArgumentException If {@code cond} is not registered to this {@code ConditionGraph}.
     */
    public Set<Condition> getAncestorConditions(Condition cond) {
        log.traceEntry("{}", cond);
        return log.traceExit(this.getAncestorConditions(cond, false));
    }

    //TODO: unit tests
    //TODO: refactor this method, constructor and getDescendantConditions
    /**
     * Get all the {@code Condition}s that are less precise than {@code cond}, 
     * among the {@code Condition}s provided at instantiation. 
     * 
     * @param cond          A {@code Condition} for which we want to retrieve ancestors {@code Condition}s.
     * @param directRelOnly A {@code boolean} defining whether only direct parents 
     *                      or children of {@code element} should be returned.
     * @return              A {@code Set} of {@code Condition}s that are ancestors of {@code cond}.
     * @throws IllegalArgumentException If {@code cond} is not registered to this {@code ConditionGraph}.
     */
    public Set<Condition> getAncestorConditions(Condition cond, boolean directRelOnly) 
            throws IllegalArgumentException {
        log.traceEntry("{}, {}", cond, directRelOnly);
        log.trace("Start retrieving ancestral conditions for {}", cond);
        if (!this.getConditions().contains(cond)) {
            throw log.throwing(new IllegalArgumentException("The provided condition "
                    + "is not registered to this ConditionGraph: " + cond));
        }

        //TODO: these blocks of code should be refactored
        Set<String> devStageIds = new HashSet<>();
        devStageIds.add(cond.getDevStageId());
        if (this.devStageOnt != null && cond.getDevStageId() != null) {
            log.trace("Retrieving dev. stage IDs from ontology for stageId {} - relOnly {}}.", 
                    cond.getDevStageId(), directRelOnly);
            devStageIds.addAll(this.devStageOnt.getAncestors(
                    this.devStageOnt.getElement(cond.getDevStageId()), directRelOnly)
                    .stream().map(e -> e.getId()).collect(Collectors.toSet()));
        }
        
        Set<String> anatEntityIds = new HashSet<>();
        anatEntityIds.add(cond.getAnatEntityId());
        if (this.anatEntityOnt != null && cond.getAnatEntityId() != null) {
            log.trace("Retrieving anat. entity IDs from ontology for stageId {} - relOnly {}.", 
                    cond.getAnatEntityId(), directRelOnly);
            anatEntityIds.addAll(this.anatEntityOnt.getAncestors(
                    this.anatEntityOnt.getElement(cond.getAnatEntityId()), directRelOnly)
                    .stream().map(e -> e.getId()).collect(Collectors.toSet()));
        }

        Set<String> cellTypeIds = new HashSet<>();
        cellTypeIds.add(cond.getCellTypeId());
        if (this.cellTypeOnt != null && cond.getCellTypeId() != null) {
            log.trace("Retrieving cell type IDs from ontology for cellTypeId {} - relOnly {}}.", 
                    cond.getCellTypeId(), directRelOnly);
            cellTypeIds.addAll(this.cellTypeOnt.getAncestors(
                    this.cellTypeOnt.getElement(cond.getCellTypeId()), directRelOnly)
                    .stream().map(e -> e.getId()).collect(Collectors.toSet()));
        }

        Set<String> sexIds = new HashSet<>();
        sexIds.add(cond.getSexId());
        if (this.sexOnt != null && cond.getSexId() != null) {
            log.trace("Retrieving sex IDs from ontology for sexId {} - relOnly {}}.", 
                    cond.getSexId(), directRelOnly);
            sexIds.addAll(this.sexOnt.getAncestors(
                    this.sexOnt.getElement(cond.getSexId()), directRelOnly)
                    .stream().map(e -> e.getId()).collect(Collectors.toSet()));
        }

        Set<String> strainIds = new HashSet<>();
        strainIds.add(cond.getStrainId());
        if (this.strainOnt != null && cond.getStrainId() != null) {
            log.trace("Retrieving strain IDs from ontology for strainId {} - relOnly {}}.", 
                    cond.getStrainId(), directRelOnly);
            strainIds.addAll(this.strainOnt.getAncestors(
                    this.strainOnt.getElement(cond.getStrainId()), directRelOnly)
                    .stream().map(e -> e.getId()).collect(Collectors.toSet()));
        }

        log.trace("Stage IDs retrieved: {}", devStageIds);
        log.trace("Anat. entity IDs retrieved: {}", anatEntityIds);
        log.trace("Cell type IDs retrieved: {}", cellTypeIds);
        log.trace("Sex IDs retrieved: {}", sexIds);
        log.trace("Strain IDs retrieved: {}", strainIds);
        
        Set<Condition> conds = this.conditions.stream()
                .filter(e -> !e.equals(cond) &&
                        devStageIds.contains(e.getDevStageId()) &&
                        anatEntityIds.contains(e.getAnatEntityId()) &&
                        cellTypeIds.contains(e.getCellTypeId()) &&
                        sexIds.contains(e.getSexId()) &&
                        strainIds.contains(e.getStrainId()))
           .collect(Collectors.toSet());
        log.trace("Done retrieving ancestral conditions for {}: {}", cond, conds.size());
        return log.traceExit(conds);
    }

    /**
     * Get all the {@code Condition}s that are more precise than {@code cond}, 
     * among the {@code Condition}s provided at instantiation. 
     * 
     * @param cond  A {@code Condition} for which we want to retrieve descendant {@code Condition}s.
     * @return      A {@code Set} of {@code Condition}s that are descendants of {@code cond}.
     * @throws IllegalArgumentException If {@code cond} is not registered to this {@code ConditionGraph}.
     */
    public Set<Condition> getDescendantConditions(Condition cond) {
        log.traceEntry("{}", cond);
        return log.traceExit(this.getDescendantConditions(cond, false));
    }
    
    /**
     * Get all the {@code Condition}s that are more precise than {@code cond} 
     * among the {@code Condition}s provided at instantiation.
     * 
     * @param cond          A {@code Condition} for which we want to retrieve descendant {@code Condition}s.
     * @param directRelOnly A {@code boolean} defining whether only direct parents 
     *                      or children of {@code element} should be returned.
     * @return              A {@code Set} of {@code Condition}s that are descendants of {@code cond}.
     * @throws IllegalArgumentException If {@code cond} is not registered to this {@code ConditionGraph}.
     */
    public Set<Condition> getDescendantConditions(Condition cond, boolean directRelOnly) {
        log.traceEntry("{}, {}", cond, directRelOnly);
        
        return getDescendantConditions(cond, directRelOnly, null, null);
    }

    /**
     * Get all the {@code Condition}s that are more precise than {@code cond} 
     * among the {@code Condition}s provided at instantiation.
     * 
     * @param cond              A {@code Condition} for which we want to retrieve descendant {@code Condition}s.
     * @param directRelOnly     A {@code boolean} defining whether only direct parents 
     *                          or children of {@code element} should be returned.
     * @param includeSubstages  A {@code boolean} defining whether conditions with child stages
     *                          should be returned.
     * @param substageMaxLevel  An {@code Integer} that is the maximal sub-level in which child stages
     *                          should be retrieved. If less than 1, there is no limitation.
     * @return                  A {@code Set} of {@code Condition}s that are descendants of {@code cond}.
     * @throws IllegalArgumentException If {@code cond} is not registered to this {@code ConditionGraph}.
     */
    //TODO : javadoc. Notably, only condition parameters are allowed in the maps
    // TODO: refactor this method with constructor and getAncestorConditions
    public Set<Condition> getDescendantConditions(Condition cond, boolean directRelOnly,
            Map<CallService.Attribute, Boolean> includeSubPerCondParameter,
            Map<CallService.Attribute, Integer> subCondMaxLevelPerCondParameter) {
        log.traceEntry("{}, {}, {}, {}", cond, directRelOnly, includeSubPerCondParameter, 
                subCondMaxLevelPerCondParameter);

        if (!this.getConditions().contains(cond)) {
            throw log.throwing(new IllegalArgumentException("The provided condition "
                    + "is not registered to this ConditionGraph: " + cond));
        }
        if (includeSubPerCondParameter != null &&
                includeSubPerCondParameter.keySet().stream().anyMatch(c -> !c.isConditionParameter()) ||
            subCondMaxLevelPerCondParameter != null &&
                subCondMaxLevelPerCondParameter.keySet().stream().anyMatch(c -> !c.isConditionParameter())) {
            throw log.throwing(new IllegalArgumentException(
                    "Only condition paramters are allowed in the Maps"));
        }
        Map<CallService.Attribute, Boolean> include = includeSubPerCondParameter;
        if (includeSubPerCondParameter == null) {
            include = EnumSet.allOf(CallService.Attribute.class).stream()
                    .filter(c -> c.isConditionParameter())
                    .collect(Collectors.toMap(c -> c, c -> true));
        }
        Map<CallService.Attribute, Integer> level = subCondMaxLevelPerCondParameter;
        if (subCondMaxLevelPerCondParameter == null) {
            level = EnumSet.allOf(CallService.Attribute.class).stream()
                    .filter(c -> c.isConditionParameter())
                    .collect(Collectors.toMap(c -> c, c -> 0));
        }

        //TODO: refactor
        Set<String> devStageIds = new HashSet<>();
        devStageIds.add(cond.getDevStageId());
        CallService.Attribute condParam = CallService.Attribute.DEV_STAGE_ID;
        if (Boolean.TRUE.equals(include.get(condParam)) &&
                this.devStageOnt != null && cond.getDevStageId() != null) {
            Set<DevStage> descendants;
            Integer maxLevel = level.get(condParam);
            if (maxLevel == null || maxLevel < 1) {
                descendants = this.devStageOnt.getDescendants(
                        this.devStageOnt.getElement(cond.getDevStageId()), directRelOnly);
            } else {
                descendants = this.devStageOnt.getDescendantsUntilSubLevel(
                        this.devStageOnt.getElement(cond.getDevStageId()), maxLevel);
            }
            devStageIds.addAll(descendants.stream().map(e -> e.getId()).collect(Collectors.toSet()));
        }

        Set<String> anatEntityIds = new HashSet<>();
        anatEntityIds.add(cond.getAnatEntityId());
        condParam = CallService.Attribute.ANAT_ENTITY_ID;
        if (Boolean.TRUE.equals(include.get(condParam)) &&
                this.anatEntityOnt != null && cond.getAnatEntityId() != null) {
            Set<AnatEntity> descendants;
            Integer maxLevel = level.get(condParam);
            if (maxLevel == null || maxLevel < 1) {
                descendants = this.anatEntityOnt.getDescendants(
                        this.anatEntityOnt.getElement(cond.getAnatEntityId()), directRelOnly);
            } else {
                descendants = this.anatEntityOnt.getDescendantsUntilSubLevel(
                        this.anatEntityOnt.getElement(cond.getAnatEntityId()), maxLevel);
            }
            anatEntityIds.addAll(descendants.stream().map(e -> e.getId()).collect(Collectors.toSet()));
        }

        Set<String> cellTypeIds = new HashSet<>();
        cellTypeIds.add(cond.getCellTypeId());
        condParam = CallService.Attribute.CELL_TYPE_ID;
        if (Boolean.TRUE.equals(include.get(condParam)) &&
                this.cellTypeOnt != null && cond.getCellTypeId() != null) {
            Set<AnatEntity> descendants;
            Integer maxLevel = level.get(condParam);
            if (maxLevel == null || maxLevel < 1) {
                descendants = this.cellTypeOnt.getDescendants(
                        this.cellTypeOnt.getElement(cond.getCellTypeId()), directRelOnly);
            } else {
                descendants = this.cellTypeOnt.getDescendantsUntilSubLevel(
                        this.cellTypeOnt.getElement(cond.getCellTypeId()), maxLevel);
            }
            cellTypeIds.addAll(descendants.stream().map(e -> e.getId()).collect(Collectors.toSet()));
        }

        Set<String> sexIds = new HashSet<>();
        sexIds.add(cond.getSexId());
        condParam = CallService.Attribute.SEX_ID;
        if (Boolean.TRUE.equals(include.get(condParam)) &&
                this.sexOnt != null && cond.getSexId() != null) {
            Set<Sex> descendants;
            Integer maxLevel = level.get(condParam);
            if (maxLevel == null || maxLevel < 1) {
                descendants = this.sexOnt.getDescendants(
                        this.sexOnt.getElement(cond.getSexId()), directRelOnly);
            } else {
                descendants = this.sexOnt.getDescendantsUntilSubLevel(
                        this.sexOnt.getElement(cond.getSexId()), maxLevel);
            }
            sexIds.addAll(descendants.stream().map(e -> e.getId()).collect(Collectors.toSet()));
        }

        Set<String> strainIds = new HashSet<>();
        strainIds.add(cond.getStrainId());
        condParam = CallService.Attribute.STRAIN_ID;
        if (Boolean.TRUE.equals(include.get(condParam)) &&
                this.strainOnt != null && cond.getStrainId() != null) {
            Set<Strain> descendants;
            Integer maxLevel = level.get(condParam);
            if (maxLevel == null || maxLevel < 1) {
                descendants = this.strainOnt.getDescendants(
                        this.strainOnt.getElement(cond.getStrainId()), directRelOnly);
            } else {
                descendants = this.strainOnt.getDescendantsUntilSubLevel(
                        this.strainOnt.getElement(cond.getStrainId()), maxLevel);
            }
            strainIds.addAll(descendants.stream().map(e -> e.getId()).collect(Collectors.toSet()));
        }

        log.trace("Stage IDs retrieved: {}", devStageIds);
        log.trace("Anat. entity IDs retrieved: {}", anatEntityIds);
        log.trace("Cell type IDs retrieved: {}", cellTypeIds);
        log.trace("Sex IDs retrieved: {}", sexIds);
        log.trace("Strain IDs retrieved: {}", strainIds);
        
        return log.traceExit(this.conditions.stream()
                .filter(e -> !e.equals(cond) &&
                             devStageIds.contains(e.getDevStageId()) &&
                             anatEntityIds.contains(e.getAnatEntityId()) &&
                             cellTypeIds.contains(e.getCellTypeId()) &&
                             sexIds.contains(e.getSexId()) &&
                             strainIds.contains(e.getStrainId()))
                .collect(Collectors.toSet()));
    }
    
    //*********************************
    //  GETTERS/SETTERS
    //*********************************
    /**
     * @return  The {@code Set} of {@code Condition}s to be considered for operations
     *          on this {@code ConditionGraph}.
     */
    public Set<Condition> getConditions() {
        return this.conditions;
    }

    /**
     * @return  An {@code Ontology} of {@code AnatEntity}s used to infer relations between {@code Condition}s. 
     *          Contains only {@code AnatEntity}s and relations for entities present 
     *          in the {@code Condition}s provided at instantiation.
     * @see #getDevStageOntology()
     */
    public Ontology<AnatEntity, String> getAnatEntityOntology() {
        return anatEntityOnt;
    }
    /**
     * @return  An {@code Ontology} of {@code DevStage}s used to infer relations between {@code Condition}s. 
     *          Contains only {@code DevStage}s and relations for entities present 
     *          in the {@code Condition}s provided at instantiation.
     * @see #getAnatEntityOntology()
     */
    public Ontology<DevStage, String> getDevStageOntology() {
        return devStageOnt;
    }
    /**
     * @return  An {@code Ontology} of {@code AnatEntity}s corresponding to cell type used to infer 
     *          relations between {@code Condition}s. 
     *          Contains only {@code AnatEntity}s and relations for entities present 
     *          in the {@code Condition}s provided at instantiation.
     */
    public Ontology<AnatEntity, String> getCellTypeOntology() {
        return cellTypeOnt;
    }
    /**
     * @return  An {@code Ontology} of {@code Sex}s corresponding to sexes used to infer 
     *          relations between {@code Condition}s. Contains only sexes and relations for entities present 
     *          in the {@code Condition}s provided at instantiation.
     */
    public Ontology<Sex, String> getSexOntology() {
        return sexOnt;
    }
    /**
     * @return  An {@code Ontology} of {@code Strain}s corresponding to strains used to infer 
     *          relations between {@code Condition}s. Contains only strains and relations for entities present 
     *          in the {@code Condition}s provided at instantiation.
     */
    public Ontology<Strain, String> getStrainOntology() {
        return strainOnt;
    }
    /** 
     * @return  The {@code boolean} defining whether the ancestral conditions should be inferred.
     */
    public boolean isInferredAncestralConditions() {
        return this.inferAncestralConditions;
    }
    /** 
     * @return  The {@code boolean} defining whether the descendant conditions should be inferred.
     */
    public boolean isInferredDescendantConditions() {
        return this.inferDescendantConditions;
    }

}
