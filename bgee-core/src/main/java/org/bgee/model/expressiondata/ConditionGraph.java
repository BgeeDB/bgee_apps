package org.bgee.model.expressiondata;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.anatdev.DevStage;
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
//Could return also the ExpressionCallUtils, the ExpressionCall.RankComparator... 
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
            Ontology<AnatEntity, String> anatEntityOnt, Ontology<DevStage, String> devStageOnt)
                    throws IllegalArgumentException {
        log.entry(conditions, inferAncestralConds, inferDescendantConds, anatEntityOnt, devStageOnt);

        if (conditions == null || conditions.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("Some conditions must be provided."));
        }
        if (anatEntityOnt == null && devStageOnt == null) {
            throw log.throwing(new IllegalArgumentException("Ontologies must be provided."));
        }
        if (anatEntityOnt != null && devStageOnt != null 
                && anatEntityOnt.getSpeciesId() != devStageOnt.getSpeciesId()) {
            throw log.throwing(new IllegalArgumentException("Ontologies should be in the same species."));
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
        this.checkEntityExistence(entities.getDevStageIds(), devStageOnt);
        this.checkEntityExistence(entities.getAnatEntityIds(), anatEntityOnt);

        this.anatEntityOnt = anatEntityOnt;
        this.devStageOnt = devStageOnt;
        this.inferAncestralConditions = inferAncestralConds;
        this.inferDescendantConditions = inferDescendantConds;
        log.exit();
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
    private <T> void checkEntityExistence(Set<String> entityIds, Ontology<?, T> ont) 
            throws IllegalArgumentException {
        log.entry(entityIds, ont);
        
        if (ont == null) {
            log.exit(); return;
        }
        
        Set<T> recognizedEntityIds = ont.getElements().stream()
                .map(e -> e.getId()).collect(Collectors.toSet());
        if (!recognizedEntityIds.containsAll(entityIds)) {
            Set<String> unrecognizedIds = new HashSet<>(entityIds);
            unrecognizedIds.removeAll(recognizedEntityIds);
            throw log.throwing(new IllegalArgumentException("Some entities do not exist "
                    + "in the provided onology: " + unrecognizedIds));
        }
        
        log.exit();
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
     *                                  to this {@code ConditionGraph}.
     */
    public boolean isConditionMorePrecise(Condition firstCond, Condition secondCond) throws IllegalArgumentException {
        log.entry(firstCond, secondCond);
        
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
            return log.exit(false);
        }
        
        //Of note, computations are three times faster when checking stages before anat. entities. 
        
        if (this.devStageOnt != null && 
                firstCond.getDevStageId() != null && secondCond.getDevStageId() != null && 
                !firstCond.getDevStageId().equals(secondCond.getDevStageId()) && 
                !this.devStageOnt.getAncestors(
                        this.devStageOnt.getElement(secondCond.getDevStageId()))
                .contains(this.devStageOnt.getElement(firstCond.getDevStageId()))) {
            return log.exit(false);
        }
        
        if (this.anatEntityOnt != null && 
                firstCond.getAnatEntityId() != null && secondCond.getAnatEntityId() != null && 
                !firstCond.getAnatEntityId().equals(secondCond.getAnatEntityId()) && 
                !this.anatEntityOnt.getAncestors(
                        this.anatEntityOnt.getElement(secondCond.getAnatEntityId()))
                .contains(this.anatEntityOnt.getElement(firstCond.getAnatEntityId()))) {
            return log.exit(false);
        }
        
        if (firstCond.getDevStageId() != null && secondCond.getDevStageId() == null || 
                secondCond.getDevStageId() != null && firstCond.getDevStageId() == null) {
            return log.exit(false);
        }
        if (firstCond.getAnatEntityId() != null && secondCond.getAnatEntityId() == null || 
                secondCond.getAnatEntityId() != null && firstCond.getAnatEntityId() == null) {
            return log.exit(false);
        }
        
        return log.exit(true);
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
        log.entry(cond);
        return log.exit(this.getAncestorConditions(cond, false));
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
        log.entry(cond, directRelOnly);
        log.trace("Start retrieving ancestral conditions for {}", cond);
        if (!this.getConditions().contains(cond)) {
            throw log.throwing(new IllegalArgumentException("The provided condition "
                    + "is not registered to this ConditionGraph: " + cond));
        }
        
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
        log.trace("Stage IDs retrieved: {}", devStageIds);
        log.trace("Anat. entity IDs retrieved: {}", anatEntityIds);
        
        Set<Condition> conds = this.conditions.stream()
                .filter(e -> !e.equals(cond) && 
                        devStageIds.contains(e.getDevStageId()) && 
                        anatEntityIds.contains(e.getAnatEntityId()))
           .collect(Collectors.toSet());
        log.trace("Done retrieving ancestral conditions for {}: {}", cond, conds.size());
        return log.exit(conds);
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
        log.entry(cond);
        return log.exit(this.getDescendantConditions(cond, false));
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
        log.entry(cond, directRelOnly);
        return getDescendantConditions(cond, directRelOnly, true);
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
     * @return                  A {@code Set} of {@code Condition}s that are descendants of {@code cond}.
     * @throws IllegalArgumentException If {@code cond} is not registered to this {@code ConditionGraph}.
     */
    // TODO: refactor this method with constructor and getAncestorConditions
    public Set<Condition> getDescendantConditions(Condition cond, boolean directRelOnly,
        boolean includeSubstages) {
        log.entry(cond, directRelOnly, includeSubstages);
        return log.exit(getDescendantConditions(cond, directRelOnly, includeSubstages, null, null));
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
    // TODO: refactor this method with constructor and getAncestorConditions
    public Set<Condition> getDescendantConditions(Condition cond, boolean directRelOnly,
        boolean includeSubstages, Integer subAnatEntityMaxLevel, Integer subStageMaxLevel) {
        log.entry(cond, directRelOnly, includeSubstages, subAnatEntityMaxLevel, subStageMaxLevel);

        if (!this.getConditions().contains(cond)) {
            throw log.throwing(new IllegalArgumentException("The provided condition "
                    + "is not registered to this ConditionGraph: " + cond));
        }
        
        Set<String> devStageIds = new HashSet<>();
        devStageIds.add(cond.getDevStageId());
        if (includeSubstages && this.devStageOnt != null && cond.getDevStageId() != null) {
            Set<DevStage> descendants;
            if (subStageMaxLevel == null || subStageMaxLevel < 1) {
                descendants = this.devStageOnt.getDescendants(
                    this.devStageOnt.getElement(cond.getDevStageId()), directRelOnly);
            } else {
                descendants = this.devStageOnt.getDescendantsUntilSubLevel(
                    this.devStageOnt.getElement(cond.getDevStageId()), subStageMaxLevel);
            }
            devStageIds.addAll(descendants.stream().map(e -> e.getId()).collect(Collectors.toSet()));
       }
        Set<String> anatEntityIds = new HashSet<>();
        anatEntityIds.add(cond.getAnatEntityId());
        if (this.anatEntityOnt != null && cond.getAnatEntityId() != null) {
            Set<AnatEntity> descendants;
            if (subAnatEntityMaxLevel == null || subAnatEntityMaxLevel < 1) {
                descendants = this.anatEntityOnt.getDescendants(
                        this.anatEntityOnt.getElement(cond.getAnatEntityId()), directRelOnly);
            } else {
                descendants = this.anatEntityOnt.getDescendantsUntilSubLevel(
                        this.anatEntityOnt.getElement(cond.getAnatEntityId()), subAnatEntityMaxLevel);
            }
            anatEntityIds.addAll(descendants.stream()
                    .map(e -> e.getId()).collect(Collectors.toSet()));
        }
        log.trace("Stage IDs retrieved: {}", devStageIds);
        log.trace("Anat. entity IDs retrieved: {}", anatEntityIds);
        
        return log.exit(this.conditions.stream()
                .filter(e -> !e.equals(cond) && 
                             devStageIds.contains(e.getDevStageId()) && 
                             anatEntityIds.contains(e.getAnatEntityId()))
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
