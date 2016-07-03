package org.bgee.model.expressiondata;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.ServiceFactory;
import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.anatdev.DevStage;
import org.bgee.model.ontology.Ontology;
import org.bgee.model.ontology.Ontology.RelationType;

/**
 * Class providing convenience operations on {@link Condition}s.
 * 
 * @author Frederic Bastian
 * @version Bgee 13 June 2016
 * @since Bgee 13 Dec. 2015
 */
public class ConditionUtils implements Comparator<Condition> {

    private static final Logger log = LogManager.getLogger(ConditionUtils.class.getName());
    
    /**
     * @see #getConditions()
     */
    private final Set<Condition> conditions;
    
    /**
     * @see #getAnatEntityOntology()
     */
    private final Ontology<AnatEntity> anatEntityOnt;
    /**
     * @see #getDevStageOntology()
     */
    private final Ontology<DevStage> devStageOnt;
    
    /**
     * A {@code String} that is the ID of the species which the {@code Condition}s 
     * should be valid in.
     */
    //XXX: maybe this will become a Set<String> for multi-species management. We'll see later. 
    //We do not provide getter for now, we don't need it and the returned type might change.
    private final String speciesId;

    /**
     * @param speciesId         A {@code String} that is the ID of the species which the {@code Condition}s 
     *                          should be valid in.
     * @param conditions        A {@code Collection} of {@code Condition}s that will be managed 
     *                          by this {@code ConditionUtils}.
     * @param serviceFactory    A {@code ServiceFactory} to acquire {@code Service}s from.
     * @throws IllegalArgumentException If any of the arguments is {@code null} or empty, 
     *                                  or if the anat. entity or dev. stage of a {@code Condition} 
     *                                  does not exist in the requested species.
     */
    public ConditionUtils(String speciesId, Collection<Condition> conditions, 
            ServiceFactory serviceFactory) {
        this(speciesId, conditions, false, serviceFactory);
    }
    //TODO: update javadoc
    public ConditionUtils(String speciesId, Collection<Condition> conditions, boolean inferAncestralConds, 
            ServiceFactory serviceFactory) throws IllegalArgumentException {
        this(speciesId, conditions, inferAncestralConds, serviceFactory, null, null);
    }
    //TODO: update javadoc
    public ConditionUtils(String speciesId, Collection<Condition> conditions, 
            Ontology<AnatEntity> anatEntityOnt, Ontology<DevStage> devStageOnt) throws IllegalArgumentException {
        this(speciesId, conditions, false, anatEntityOnt, devStageOnt);
    }
    //TODO: update javadoc
    public ConditionUtils(String speciesId, Collection<Condition> conditions, boolean inferAncestralConds, 
            Ontology<AnatEntity> anatEntityOnt, Ontology<DevStage> devStageOnt) throws IllegalArgumentException {
        this(speciesId, conditions, inferAncestralConds, null, anatEntityOnt, devStageOnt);
    }
    /**
     * Constructor accepting all required parameters. 
     * 
     * @param speciesId         A {@code String} that is the ID of the species which the {@code Condition}s 
     *                          should be valid in.
     * @param conditions        A {@code Collection} of {@code Condition}s that will be managed 
     *                          by this {@code ConditionUtils}.
     * @param serviceFactory    A {@code ServiceFactory} to acquire {@code Service}s from.
     * @throws IllegalArgumentException If any of the arguments is {@code null} or empty, 
     *                                  or if the anat. entity or dev. stage of a {@code Condition} 
     *                                  does not exist in the requested species.
     */
    //XXX: we'll see what we'll do for multi-species later, for now we only accept a single species. 
    //I guess multi-species would need a separate class, e.g., MultiSpeciesConditionUtils.
    //TODO: update javadoc
    //TODO: unit test for ancestral condition inferences
    //TODO: refactor this constructor, methods getAncestorConditions and getDescendantConditions
    private ConditionUtils(String speciesId, Collection<Condition> conditions, boolean inferAncestralConds, 
            ServiceFactory serviceFactory, Ontology<AnatEntity> anatEntityOnt, 
            Ontology<DevStage> devStageOnt) throws IllegalArgumentException {
        log.entry(speciesId, conditions, inferAncestralConds, serviceFactory, anatEntityOnt, devStageOnt);
        if (StringUtils.isBlank(speciesId)) {
            throw log.throwing(new IllegalArgumentException("A species ID must be provided."));
        }
        if (conditions == null || conditions.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("Some conditions must be provided."));
        }
        if (serviceFactory == null && anatEntityOnt == null && devStageOnt == null) {
            throw log.throwing(new IllegalArgumentException(
                    "A ServiceFactory or some ontologies must be provided."));
        }
        
        this.speciesId = speciesId;
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
        
        if (!anatEntityIds.isEmpty()) {
            //it will be checked later that all anat. entities are present in the ontology
            if (anatEntityOnt != null) {
                this.anatEntityOnt = anatEntityOnt;
            } else if (serviceFactory != null) {
                this.anatEntityOnt = serviceFactory.getOntologyService().getAnatEntityOntology(
                        Arrays.asList(this.speciesId), anatEntityIds, EnumSet.of(RelationType.ISA_PARTOF), 
                        inferAncestralConds? true: false, false, serviceFactory.getAnatEntityService());
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
                        Arrays.asList(this.speciesId), devStageIds, 
                        inferAncestralConds? true: false, false, serviceFactory.getDevStageService());
            } else {
                throw log.throwing(new IllegalArgumentException(
                        "No ServiceFactory nor developmental ontology provided."));
            }
        } else {
            this.devStageOnt = null;
        }

        if (inferAncestralConds) {
            Set<Condition> ancConditions = tempConditions.stream().flatMap(cond -> {
                Set<String> ancStageIds = new HashSet<>();
                ancStageIds.add(cond.getDevStageId());
                if (this.devStageOnt != null && cond.getDevStageId() != null) {
                    ancStageIds.addAll(this.devStageOnt.getAncestors(
                            this.devStageOnt.getElement(cond.getDevStageId()))
                            .stream().map(e -> e.getId()).collect(Collectors.toSet()));
                }
                
                Set<String> ancAnatEntityIds = new HashSet<>();
                ancAnatEntityIds.add(cond.getAnatEntityId());
                if (this.anatEntityOnt != null && cond.getAnatEntityId() != null) {
                    ancAnatEntityIds.addAll(this.anatEntityOnt.getAncestors(
                            this.anatEntityOnt.getElement(cond.getAnatEntityId()))
                            .stream().map(e -> e.getId()).collect(Collectors.toSet()));
                }
                
                return ancAnatEntityIds.stream().flatMap(ancAnatEntityId -> 
                    ancStageIds.stream().map(ancStageId -> new Condition(ancAnatEntityId, ancStageId))
                    ).filter(ancCond -> !cond.equals(ancCond));
                
            }).collect(Collectors.toSet());
            
            tempConditions.addAll(ancConditions);
        }
        this.conditions = Collections.unmodifiableSet(tempConditions);

        this.checkEntityExistence(devStageIds, this.devStageOnt);
        this.checkEntityExistence(anatEntityIds, this.anatEntityOnt);
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
     */
    private void checkEntityExistence(Set<String> entityIds, Ontology<?> ont) throws IllegalArgumentException {
        log.entry(entityIds, ont);
        
        if (ont == null) {
            log.exit(); return;
        }
        
        Set<String> recognizedEntityIds = ont.getElements().stream()
                .map(e -> e.getId()).collect(Collectors.toSet());
        if (!recognizedEntityIds.containsAll(entityIds)) {
            Set<String> unrecognizedIds = new HashSet<>(entityIds);
            unrecognizedIds.removeAll(recognizedEntityIds);
            throw log.throwing(new IllegalArgumentException("Some entities do not exist "
                    + "in the requested species (" + this.speciesId + "): " + unrecognizedIds));
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
     *                                  to this {@code ConditionUtils}.
     */
    public boolean isConditionMorePrecise(Condition firstCond, Condition secondCond) throws IllegalArgumentException {
        log.entry(firstCond, secondCond);
        if (!this.getConditions().contains(firstCond) || !this.getConditions().contains(secondCond)) {
            throw log.throwing(new IllegalArgumentException("Some of the provided conditions "
                    + "are not registered to this ConditionUtils. First condition: " + firstCond 
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
    
    //TODO: javadoc
    //TODO: unit tests
    //TODO: refactor this method, constructor and getDescendantConditions
    public Set<Condition> getAncestorConditions(Condition cond, boolean directRelOnly) {
        log.entry(cond, directRelOnly);
        if (!this.getConditions().contains(cond)) {
            throw log.throwing(new IllegalArgumentException("The provided condition "
                    + "is not registered to this ConditionUtils: " + cond));
        }
        
        Set<String> devStageIds = new HashSet<>();
        devStageIds.add(cond.getDevStageId());
        if (this.devStageOnt != null && cond.getDevStageId() != null) {
            devStageIds.addAll(this.devStageOnt.getAncestors(
                    this.devStageOnt.getElement(cond.getDevStageId()), directRelOnly)
                    .stream().map(e -> e.getId()).collect(Collectors.toSet()));
        }
        
        Set<String> anatEntityIds = new HashSet<>();
        anatEntityIds.add(cond.getAnatEntityId());
        if (this.anatEntityOnt != null && cond.getAnatEntityId() != null) {
            anatEntityIds.addAll(this.anatEntityOnt.getAncestors(
                    this.anatEntityOnt.getElement(cond.getAnatEntityId()), directRelOnly)
                    .stream().map(e -> e.getId()).collect(Collectors.toSet()));
        }
        
        return log.exit(this.conditions.stream()
                .filter(e -> !e.equals(cond) && 
                             devStageIds.contains(e.getDevStageId()) && 
                             anatEntityIds.contains(e.getAnatEntityId()))
                .collect(Collectors.toSet()));
    }

    /**
     * Get all the {@code Conditions} that are more precise than {@code cond}, 
     * among the {@code Condition}s provided at instantiation. 
     * 
     * @param cond  A {@code Condition} for which we want to retrieve descendant {@code Condition}s.
     * @return      A {@code Set} of {@code Condition}s that are descendants of {@code cond}.
     * @throws IllegalArgumentException If {@code cond} is not registered to this {@code ConditionUtils}.
     */
    public Set<Condition> getDescendantConditions(Condition cond) {
        log.entry(cond);
        return log.exit(this.getDescendantConditions(cond, false));
    }
    /**
     * Get all the {@code Conditions} that are more precise than {@code cond}, 
     * among the {@code Condition}s provided at instantiation. 
     * 
     * @param cond  A {@code Condition} for which we want to retrieve descendant {@code Condition}s.
     * @return      A {@code Set} of {@code Condition}s that are descendants of {@code cond}.
     * @throws IllegalArgumentException If {@code cond} is not registered to this {@code ConditionUtils}.
     */
    //TODO: update javadoc for directRelOnly
    //TODO: refactor this method, constructor and getAncestorConditions
    public Set<Condition> getDescendantConditions(Condition cond, boolean directRelOnly) {
        log.entry(cond, directRelOnly);
        if (!this.getConditions().contains(cond)) {
            throw log.throwing(new IllegalArgumentException("The provided condition "
                    + "is not registered to this ConditionUtils: " + cond));
        }
        
        Set<String> devStageIds = new HashSet<>();
        devStageIds.add(cond.getDevStageId());
        if (this.devStageOnt != null && cond.getDevStageId() != null) {
            devStageIds.addAll(this.devStageOnt.getDescendants(
                    this.devStageOnt.getElement(cond.getDevStageId()), directRelOnly)
                    .stream().map(e -> e.getId()).collect(Collectors.toSet()));
        }
        
        Set<String> anatEntityIds = new HashSet<>();
        anatEntityIds.add(cond.getAnatEntityId());
        if (this.anatEntityOnt != null && cond.getAnatEntityId() != null) {
            anatEntityIds.addAll(this.anatEntityOnt.getDescendants(
                    this.anatEntityOnt.getElement(cond.getAnatEntityId()), directRelOnly)
                    .stream().map(e -> e.getId()).collect(Collectors.toSet()));
        }
        
        return log.exit(this.conditions.stream()
                .filter(e -> !e.equals(cond) && 
                             devStageIds.contains(e.getDevStageId()) && 
                             anatEntityIds.contains(e.getAnatEntityId()))
                .collect(Collectors.toSet()));
    }
    
    /**
     * Retrieve an {@code AnatEntity} present in a {@code Condition} provided at instantiation, 
     * based on its ID.
     * 
     * @param anatEntityId  A {@code String} that is the ID of the {@code AnatEntity} to retrieve.
     * @return              The corresponding {@code AnatEntity}. {@code null} if no corresponding 
     *                      {@code AnatEntity} was present in the {@code Condition}s provided 
     *                      at instantiation.
     */
    public AnatEntity getAnatEntity(String anatEntityId) {
        log.entry(anatEntityId);
        return log.exit(this.getAnatEntityOntology().getElement(anatEntityId));
    }
    /**
     * Retrieve an {@code AnatEntity} from a {@code Condition}.
     * 
     * @param condition     The {@code Condition} which to retrieve the ID of the requested 
     *                      {@code AnatEntity} from.
     * @return              The {@code AnatEntity} corresponding to the ID provided by {@code condition}. 
     * @throws IllegalArgumentException If {@code condition} was not part of the {@code Condition}s 
     *                                  provided at instantiation of this {@code ConditionUtils}.
     */
    public AnatEntity getAnatEntity(Condition condition) {
        log.entry(condition);
        if (!this.conditions.contains(condition)) {
            throw log.throwing(new IllegalArgumentException("Unrecognized condition: " + condition));
        }
        return log.exit(this.getAnatEntityOntology().getElement(condition.getAnatEntityId()));
    }
    /**
     * Retrieve a {@code DevStage} present in a {@code Condition} provided at instantiation, 
     * based on its ID.
     * 
     * @param devStageId    A {@code String} that is the ID of the {@code DevStage} to retrieve.
     * @return              The corresponding {@code DevStage}. {@code null} if no corresponding 
     *                      {@code DevStage} was present in the {@code Condition}s provided 
     *                      at instantiation.
     */
    public DevStage getDevStage(String devStageId) {
        log.entry(devStageId);
        return log.exit(this.getDevStageOntology().getElement(devStageId));
    }
    /**
     * Retrieve a {@code DevStage} from a {@code Condition}.
     * 
     * @param condition     The {@code Condition} which to retrieve the ID of the requested 
     *                      {@code DevStage} from.
     * @return              The {@code DevStage} corresponding to the ID provided by {@code condition}. 
     * @throws IllegalArgumentException If {@code condition} was not part of the {@code Condition}s 
     *                                  provided at instantiation of this {@code ConditionUtils}.
     */
    public DevStage getDevStage(Condition condition) {
        log.entry(condition);
        if (!this.conditions.contains(condition)) {
            throw log.throwing(new IllegalArgumentException("Unrecognized condition: " + condition));
        }
        return log.exit(this.getDevStageOntology().getElement(condition.getDevStageId()));
    }
    
    //*********************************
    //  GETTERS/SETTERS
    //*********************************
    /**
     * @return  The {@code Set} of {@code Condition}s to be considered for operations on this {@code ConditionUtils}.
     */
    public Set<Condition> getConditions() {
        return conditions;
    }
    /**
     * @return  An {@code Ontology} of {@code AnatEntity}s used to infer relations between {@code Condition}s. 
     *          Contains only {@code AnatEntity}s and relations for entities present 
     *          in the {@code Condition}s provided at instantiation.
     * @see #getDevStageOntology()
     */
    public Ontology<AnatEntity> getAnatEntityOntology() {
        return anatEntityOnt;
    }
    /**
     * @return  An {@code Ontology} of {@code DevStage}s used to infer relations between {@code Condition}s. 
     *          Contains only {@code DevStage}s and relations for entities present 
     *          in the {@code Condition}s provided at instantiation.
     * @see #getAnatEntityOntology()
     */
    public Ontology<DevStage> getDevStageOntology() {
        return devStageOnt;
    }
    
}
