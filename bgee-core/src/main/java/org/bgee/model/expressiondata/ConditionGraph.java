package org.bgee.model.expressiondata;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.ServiceFactory;
import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.anatdev.DevStage;
import org.bgee.model.ontology.Ontology;
import org.bgee.model.ontology.RelationType;

/**
 * Class providing convenience operations on {@link Condition}s.
 * <p>
 * When this class is instantiated, the constructor retrieves ontologies for provided 
 * {@code Condition}s if they are not provided.
 * 
 * @author  Frederic Bastian
 * @author  Valentine Rech de Laval
 * @version Bgee 14, Feb. 2017
 * @since   Bgee 13, Dec. 2015
 */
//TODO: Actually, maybe we should have an UtilsFactory, as we have a ServiceFactory. 
//Could return also the ExpressionCallUtils, the ExpressionCall.RankComparator... 
//that would be much cleaner for unit tests. 
public class ConditionGraph implements Comparator<Condition> {

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
     * Constructor providing the {@code conditions} and the {@code serviceFactory}.
     * <p>
     * The constructor retrieves ontologies.
     *   
     * @param conditions        A {@code Collection} of {@code Condition}s that will be managed 
     *                          by this {@code ConditionGraph}.
     * @param serviceFactory    A {@code ServiceFactory} to acquire {@code Service}s from.
     * @throws IllegalArgumentException If any of the arguments is {@code null} or empty, 
     *                                  or if the anat. entity or dev. stage of a {@code Condition} 
     *                                  does not exist in the requested species.
     */
    public ConditionGraph(Collection<Condition> conditions, ServiceFactory serviceFactory) {
        this(conditions, false, false, serviceFactory);
    }
    
    /**
     * Constructor providing the {@code conditions} and defining whether the ancestral conditions
     * should be inferred.
     * <p>
     * The constructor retrieves ontologies.  
     * 
     * @param conditions            A {@code Collection} of {@code Condition}s that will be managed 
     *                              by this {@code ConditionGraph}.
     * @param inferAncestralConds   A {@code boolean} defining whether the ancestral conditions
     *                              should be inferred.
     * @param inferDescendantConds  A {@code boolean} defining whether the descendant conditions
     *                              should be inferred.
     * @throws IllegalArgumentException If any of the arguments is {@code null} or empty, 
     *                                  or if {@code Condition}s does not exist in the same species.
     */
    public ConditionGraph(Collection<Condition> conditions, boolean inferAncestralConds,
            boolean inferDescendantConds, ServiceFactory serviceFactory) throws IllegalArgumentException {
        this(conditions, inferAncestralConds, inferDescendantConds, serviceFactory, null, null);
    }
    
    /**
     * Constructor providing the {@code conditions}, anatomical entity ontology, and
     * developmental stage ontology.
     * <p>
     * The constructor retrieves ontologies if {@code null}.  
     * 
     * @param conditions            A {@code Collection} of {@code Condition}s that will be managed 
     *                              by this {@code ConditionGraph}.
     * @param anatEntityOnt         An {@code Ontology} of {@code AnatEntity}s that is 
     *                              the ontology of anatomical entities of a single species.
     *                              If {@code null}, the constructor retrieves the ontology.  
     * @param devStageOnt           An {@code Ontology} of {@code DevStage}s that is 
     *                              the ontology of developmental stages of a single species.
     *                              If {@code null}, the constructor retrieves the ontology.  
     * @throws IllegalArgumentException If any of the arguments is {@code null} or empty, 
     *                                  or if {@code Condition}s does not exist in the same species.
     */
    public ConditionGraph(Collection<Condition> conditions, Ontology<AnatEntity, String> anatEntityOnt,
            Ontology<DevStage, String> devStageOnt) throws IllegalArgumentException {
        this(conditions, false, false, anatEntityOnt, devStageOnt);
    }
    
    /**
     * Constructor providing the {@code conditions}, anatomical entity ontology, developmental stage
     * ontology, and defining whether the ancestral conditions should be inferred.
     * <p>
     * The constructor retrieves ontologies if {@code null}.  
     * 
     * @param conditions            A {@code Collection} of {@code Condition}s that will be managed 
     *                              by this {@code ConditionGraph}.
     * @param inferAncestralConds   A {@code boolean} defining whether the ancestral conditions
     *                              should be inferred.
     * @param anatEntityOnt         An {@code Ontology} of {@code AnatEntity}s that is 
     *                              the ontology of anatomical entities of a single species.
     *                              If {@code null}, the constructor retrieves the ontology.  
     * @param devStageOnt           An {@code Ontology} of {@code DevStage}s that is 
     *                              the ontology of developmental stages of a single species.
     *                              If {@code null}, the constructor retrieves the ontology.  
     * @throws IllegalArgumentException If any of the arguments is {@code null} or empty, 
     *                                  or if {@code Condition}s does not exist in the same species.
     */
    public ConditionGraph(Collection<Condition> conditions, boolean inferAncestralConds,
            boolean inferDescendantConds, Ontology<AnatEntity, String> anatEntityOnt,
            Ontology<DevStage, String> devStageOnt) throws IllegalArgumentException {
        this(conditions, inferAncestralConds, inferDescendantConds, null, anatEntityOnt, devStageOnt);
    }
    
    /**
     * Constructor accepting all parameters.
     * <p>
     * The constructor retrieves ontologies if {@code null}.  
     * 
     * @param conditions            A {@code Collection} of {@code Condition}s that will be managed 
     *                              by this {@code ConditionGraph}.
     * @param inferAncestralConds   A {@code boolean} defining whether the ancestral conditions
     *                              should be inferred.
     * @param serviceFactory        A {@code ServiceFactory} to acquire {@code Service}s from.
     * @param anatEntityOnt         An {@code Ontology} of {@code AnatEntity}s that is 
     *                              the ontology of anatomical entities of a single species.
     *                              If {@code null}, the constructor retrieves the ontology.  
     * @param devStageOnt           An {@code Ontology} of {@code DevStage}s that is 
     *                              the ontology of developmental stages of a single species.
     *                              If {@code null}, the constructor retrieves the ontology.  
     * @throws IllegalArgumentException If any of the arguments is {@code null} or empty, 
     *                                  or if {@code Condition}s does not exist in the same species.
     */
    //XXX: we'll see what we'll do for multi-species later, for now we only accept a single species. 
    //I guess multi-species would need a separate class, e.g., MultiSpeciesConditionUtils.
    //TODO: unit test for ancestral condition inferences
    //TODO: refactor this constructor, methods getAncestorConditions and getDescendantConditions
    private ConditionGraph(Collection<Condition> conditions, 
            boolean inferAncestralConds, boolean inferDescendantConds,
            ServiceFactory serviceFactory, Ontology<AnatEntity, String> anatEntityOnt,
            Ontology<DevStage, String> devStageOnt) throws IllegalArgumentException {
        log.entry(conditions, inferAncestralConds, inferDescendantConds, serviceFactory, anatEntityOnt, devStageOnt);

        long startTimeInMs = System.currentTimeMillis();
        log.debug("Start creation of ConditionGraph");
        if (conditions == null || conditions.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("Some conditions must be provided."));
        }
        if (serviceFactory == null && anatEntityOnt == null && devStageOnt == null) {
            throw log.throwing(new IllegalArgumentException(
                    "A ServiceFactory or some ontologies must be provided."));
        }
        
        this.inferAncestralConditions = inferAncestralConds;
        this.inferDescendantConditions = inferDescendantConds;
        Set<Condition> tempConditions = new HashSet<>(conditions);
        
        Set<String> anatEntityIds = new HashSet<>();
        Set<String> devStageIds = new HashSet<>();
        for (Condition cond: tempConditions) {
            if (cond.getAnatEntityId() != null) {
                anatEntityIds.add(cond.getAnatEntityId());
            }
            if (cond.getDevStageId() != null) {
                devStageIds.add(cond.getDevStageId());
            }
        }
        
        //Get species ID from conditions and check if it is the same in all conditions
        Set<Integer> speciesIds = conditions.stream().map(c -> c.getSpecies().getId()).collect(Collectors.toSet());
        if (speciesIds.size() != 1) {
            throw log.throwing(new IllegalArgumentException("Conditions should be in the same species."));
        }
        Integer speciesId = speciesIds.iterator().next();
        if (!anatEntityIds.isEmpty()) {
            //it will be checked later that all anat. entities are present in the ontology
            if (anatEntityOnt != null) {
                this.anatEntityOnt = anatEntityOnt;
            } else if (serviceFactory != null) {
                this.anatEntityOnt = serviceFactory.getOntologyService().getAnatEntityOntology(
                        speciesId, anatEntityIds, EnumSet.of(RelationType.ISA_PARTOF), 
                        inferAncestralConds, inferDescendantConds);
            } else {
                throw log.throwing(new IllegalArgumentException(
                        "No ServiceFactory nor anatomical ontology provided."));
            }
        } else {
            this.anatEntityOnt = null;
        }
        if (!devStageIds.isEmpty()) {
            //it will be checked later that all dev. stages are present in the ontology
            if (devStageOnt != null) {
                this.devStageOnt = devStageOnt;
            } else if (serviceFactory != null) {
                this.devStageOnt = serviceFactory.getOntologyService().getDevStageOntology(
                        speciesId, devStageIds, inferAncestralConds, inferDescendantConds);
            } else {
                throw log.throwing(new IllegalArgumentException(
                        "No ServiceFactory nor developmental ontology provided."));
            }
        } else {
            this.devStageOnt = null;
        }
        
        if (this.anatEntityOnt != null && this.devStageOnt != null 
                && this.anatEntityOnt.getSpeciesId() != this.devStageOnt.getSpeciesId()) {
            throw log.throwing(new IllegalArgumentException("Ontologies should be in the same species."));
        }

        //TODO: test inference of descendant conditions
        if (inferAncestralConds || inferDescendantConds) {
            Set<Condition> newPropagatedConditions = tempConditions.stream().flatMap(cond -> {
                Set<DevStage> propStages = new HashSet<>();
                propStages.add(cond.getDevStage());
                if (this.devStageOnt != null && cond.getDevStageId() != null) {
                    if (inferAncestralConds) {
                        propStages.addAll(this.devStageOnt.getAncestors(cond.getDevStage()));
                    }
                    if (inferDescendantConds) {
                        propStages.addAll(this.devStageOnt.getDescendants(cond.getDevStage()));
                    }
                }
                
                Set<AnatEntity> propAnatEntitys = new HashSet<>();
                propAnatEntitys.add(cond.getAnatEntity());
                if (this.anatEntityOnt != null && cond.getAnatEntityId() != null) {
                    if (inferAncestralConds) {
                        propAnatEntitys.addAll(this.anatEntityOnt.getAncestors(cond.getAnatEntity()));
                    }
                    if (inferDescendantConds) {
                        propAnatEntitys.addAll(this.anatEntityOnt.getDescendants(cond.getAnatEntity()));
                    }
                }
                
                return propAnatEntitys.stream()
                        .flatMap(propAnatEntity -> propStages.stream().map(propStage -> 
                            new Condition(propAnatEntity, propStage, cond.getSpecies())))
                        .filter(propCond -> !cond.equals(propCond));

            }).collect(Collectors.toSet());
            
            tempConditions.addAll(newPropagatedConditions);
        }
        this.conditions = Collections.unmodifiableSet(tempConditions);

        this.checkEntityExistence(devStageIds, this.devStageOnt);
        this.checkEntityExistence(anatEntityIds, this.anatEntityOnt);

        log.debug("ConditionGraph created in {} ms", System.currentTimeMillis() - startTimeInMs);
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
     *                                  to this {@code ConditionGraph}, or one of them is {@code null}.
     */
    public boolean isConditionMorePrecise(Condition firstCond, Condition secondCond) throws IllegalArgumentException {
        log.entry(firstCond, secondCond);

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
     * Compare two {@code Condition}s based on their relations between each other. 
     * Will return a negative {@code int} if {@code cond1} is more precise than {@code cond2}, 
     * a positive {@code int} if {@code cond1} is less precise than {@code cond2}, 
     * {@code 0} if {@code cond1} and {@code cond2} are unrelated.
     * <p>
     * For a comparison of {@code Condition}s simply based on their attributes, 
     * see {@code Condition#compareTo(Condition)}. 
     * 
     * @param cond1 The first {@code Condition} to be compared. 
     * @param cond2 The second {@code Condition} to be compared. 
     * @return      a negative {@code int}, zero, or a positive {@code int} 
     *              as the first argument is more precise than, unrelated to, or less precise 
     *              than the second.
     * @see #isConditionMorePrecise(Condition, Condition)
     */
    @Override
    public int compare(Condition cond1, Condition cond2) {
        log.entry(cond1, cond2);

        if (this.isConditionMorePrecise(cond1, cond2)) {
            return log.exit(1);
        }
        if (this.isConditionMorePrecise(cond2, cond1)) {
            return log.exit(-1);
        }
        return log.exit(0);
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
