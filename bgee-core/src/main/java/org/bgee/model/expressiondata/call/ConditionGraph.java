package org.bgee.model.expressiondata.call;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.NamedEntity;
import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.anatdev.DevStage;
import org.bgee.model.anatdev.Sex;
import org.bgee.model.anatdev.Strain;
import org.bgee.model.expressiondata.call.Condition.ConditionEntities;
import org.bgee.model.ontology.Ontology;
import org.bgee.model.ontology.OntologyElement;

/**
 * Class providing convenience operations on {@link Condition}s.
 * <p>
 * When this class is instantiated, the constructor retrieves ontologies for provided 
 * {@code Condition}s if they are not provided.
 * 
 * @author  Frederic Bastian
 * @author  Valentine Rech de Laval
 * @version Bgee 15.0, May 2021
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

        Set<Integer> speciesIds = new HashSet<>();
        if (anatEntityOnt != null) {
            speciesIds.add(anatEntityOnt.getSpeciesId());
        }
        if (cellTypeOnt != null) {
            speciesIds.add(cellTypeOnt.getSpeciesId());
        }
        if (devStageOnt != null) {
            speciesIds.add(devStageOnt.getSpeciesId());
        }
        if (sexOnt != null) {
            speciesIds.add(sexOnt.getSpeciesId());
        }
        if (strainOnt != null) {
            speciesIds.add(strainOnt.getSpeciesId());
        }
        if (speciesIds.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("Ontologies must be provided."));
        }
        if (speciesIds.size() > 1) {
            throw log.throwing(new IllegalArgumentException(
                    "All ontologies should be in the same species."));
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
        if (strainOnt != null) {
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

        if (firstCond.getSex() != null && secondCond.getSex() == null ||
                secondCond.getSex() != null && firstCond.getSex() == null) {
            return log.traceExit(false);
        }
        if (firstCond.getStrain() != null && secondCond.getStrain() == null ||
                secondCond.getStrain() != null && firstCond.getStrain() == null) {
            return log.traceExit(false);
        }
        if (firstCond.getDevStage() != null && secondCond.getDevStage() == null ||
                secondCond.getDevStage() != null && firstCond.getDevStage() == null) {
            return log.traceExit(false);
        }
        if (firstCond.getCellType() != null && secondCond.getCellType() == null ||
                secondCond.getCellType() != null && firstCond.getCellType() == null) {
            return log.traceExit(false);
        }
        if (firstCond.getAnatEntity() != null && secondCond.getAnatEntity() == null ||
                secondCond.getAnatEntity() != null && firstCond.getAnatEntity() == null) {
            return log.traceExit(false);
        }

        //Of note, computations are faster when the less complex ontologies are used first. 
        //TODO: refactor

        if (this.sexOnt != null &&
                firstCond.getSex() != null && secondCond.getSex() != null &&
                !firstCond.getSex().equals(secondCond.getSex()) &&
                !this.sexOnt.getAncestors(
                        this.sexOnt.getElement(secondCond.getSexId()))
                .contains(this.sexOnt.getElement(firstCond.getSexId()))) {
            return log.traceExit(false);
        }

        if (this.strainOnt != null &&
                firstCond.getStrain() != null && secondCond.getStrain() != null &&
                !firstCond.getStrain().equals(secondCond.getStrain()) &&
                !this.strainOnt.getAncestors(
                        this.strainOnt.getElement(secondCond.getStrainId()))
                .contains(this.strainOnt.getElement(firstCond.getStrainId()))) {
            return log.traceExit(false);
        }

        if (this.devStageOnt != null && 
                firstCond.getDevStage() != null && secondCond.getDevStage() != null && 
                !firstCond.getDevStage().equals(secondCond.getDevStage()) && 
                !this.devStageOnt.getAncestors(
                        this.devStageOnt.getElement(secondCond.getDevStageId()))
                .contains(this.devStageOnt.getElement(firstCond.getDevStageId()))) {
            return log.traceExit(false);
        }

        if (this.cellTypeOnt != null &&
                firstCond.getCellType() != null && secondCond.getCellType() != null &&
                !firstCond.getCellType().equals(secondCond.getCellType()) &&
                !this.cellTypeOnt.getAncestors(
                        this.cellTypeOnt.getElement(secondCond.getCellTypeId()))
                .contains(this.cellTypeOnt.getElement(firstCond.getCellTypeId()))) {
            return log.traceExit(false);
        }
        
        if (this.anatEntityOnt != null && 
                firstCond.getAnatEntity() != null && secondCond.getAnatEntity() != null && 
                !firstCond.getAnatEntity().equals(secondCond.getAnatEntity()) && 
                !this.anatEntityOnt.getAncestors(
                        this.anatEntityOnt.getElement(secondCond.getAnatEntityId()))
                .contains(this.anatEntityOnt.getElement(firstCond.getAnatEntityId()))) {
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
    /**
     * Get all the {@code Condition}s that are less precise than {@code cond}, 
     * among the {@code Condition}s provided at instantiation. 
     * 
     * @param cond          A {@code Condition} for which we want to retrieve ancestor {@code Condition}s.
     * @param directRelOnly A {@code boolean} defining whether only direct parents 
     *                      or children of {@code element} should be returned.
     * @return              A {@code Set} of {@code Condition}s that are ancestors of {@code cond}.
     * @throws IllegalArgumentException If {@code cond} is not registered to this {@code ConditionGraph}.
     */
    public Set<Condition> getAncestorConditions(Condition cond, boolean directRelOnly) 
            throws IllegalArgumentException {
        log.traceEntry("{}, {}", cond, directRelOnly);
        return log.traceExit(this.getRelativeConditions(cond, true, directRelOnly));
    }

    /**
     * Notes on implementation of this method: because we do not insert all possible conditions
     * in the database, but only those that have some parameters observed in annotations,
     * the graph is disconnected, and some conditions might not always have "real" direct parents or descendants.
     * For this reason, when direct parents or descendants are requested
     * ({@code directRelOnly} is {@code true}), actually we retrieve all parents (or descendants),
     * and filter out those that are themselves parents of these parents (or descendants of these descendants).
     * This way, we properly reconnect the graph, and can infer the "new" "direct" relatives.
     * <p>
     * It is not perfect: it would fail if there was a cycle in the graph; or some of the relatives discarded
     * could actually have had a real direct relation (for this reason we also check the direct relations
     * from the underlying ontologies). But it is probably the best we can do.
     *
     * @param cond          A {@code Condition} for which we want to retrieve ancestor or descendant {@code Condition}s.
     * @param ancestors     A {@code boolean} defining whether ancestors should be retrieved
     *                      (if {@code true}), or descendants (if {@code false}).
     * @param directRelOnly A {@code boolean} defining whether only direct parents
     *                      or children of {@code element} should be returned.
     * @return              A {@code Set} of {@code Condition}s that are ancestors or descendants of {@code cond}.
     * @throws IllegalArgumentException If {@code cond} is not registered to this {@code ConditionGraph}.
     */
    private Set<Condition> getRelativeConditions(Condition cond, boolean ancestors, boolean directRelOnly)
            throws IllegalArgumentException {
        log.traceEntry("{}, {}, {}", cond, ancestors, directRelOnly);
        log.trace("Start retrieving relative conditions for {}", cond);
        if (!this.getConditions().contains(cond)) {
            throw log.throwing(new IllegalArgumentException("The provided condition "
                    + "is not registered to this ConditionGraph: " + cond));
        }

        Set<DevStage> devStages = getRelativeElements(this.devStageOnt, cond.getDevStage(), ancestors, false);
        Set<AnatEntity> anatEntities = getRelativeElements(this.anatEntityOnt, cond.getAnatEntity(), ancestors, false);
        Set<AnatEntity> cellTypes = getRelativeElements(this.cellTypeOnt, cond.getCellType(), ancestors, false);
        Set<Sex> sexes = getRelativeElements(this.sexOnt, cond.getSex(), ancestors, false);
        Set<Strain> strains = getRelativeElements(this.strainOnt, cond.getStrain(), ancestors, false);

        log.trace("Stages retrieved: {}", devStages);
        log.trace("Anat. entities retrieved: {}", anatEntities);
        log.trace("Cell types retrieved: {}", cellTypes);
        log.trace("Sexes retrieved: {}", sexes);
        log.trace("Strains retrieved: {}", strains);
        
        Set<Condition> relativeConds = this.conditions.stream()
                .filter(e -> !e.equals(cond) &&
                        devStages.contains(e.getDevStage()) &&
                        anatEntities.contains(e.getAnatEntity()) &&
                        cellTypes.contains(e.getCellType()) &&
                        sexes.contains(e.getSex()) &&
                        strains.contains(e.getStrain()))
           .collect(Collectors.toSet());

        if (directRelOnly) {
            Set<DevStage> directDevStages = getRelativeElements(this.devStageOnt, cond.getDevStage(),
                    ancestors, true);
            Set<AnatEntity> directAnatEntities = getRelativeElements(this.anatEntityOnt, cond.getAnatEntity(),
                    ancestors, true);
            Set<AnatEntity> directCellTypes = getRelativeElements(this.cellTypeOnt, cond.getCellType(),
                    ancestors, true);
            Set<Sex> directSexes = getRelativeElements(this.sexOnt, cond.getSex(),
                    ancestors, true);
            Set<Strain> directStrains = getRelativeElements(this.strainOnt, cond.getStrain(),
                    ancestors, true);
            Set<Condition> relativesOfRelatives = relativeConds.stream()
                    .flatMap(c -> this.getRelativeConditions(c, ancestors, false).stream())
                    .collect(Collectors.toSet());

            relativeConds = relativeConds.stream()
                    .filter(e ->
                        //Either the relative conditions is really a direct relative
                        //by the relations in the ontologies
                        directDevStages.contains(e.getDevStage()) &&
                        directAnatEntities.contains(e.getAnatEntity()) &&
                        directCellTypes.contains(e.getCellType()) &&
                        directSexes.contains(e.getSex()) &&
                        directStrains.contains(e.getStrain()) ||
                        //Or it is a disconnected relative (because of condition filtering),
                        //not reachable by any other relatives, so we consider it as "direct".
                        !relativesOfRelatives.contains(e))
                    .collect(Collectors.toSet());
        }
        log.trace("Done retrieving relative conditions for {}: {}", cond, relativeConds.size());
        return log.traceExit(relativeConds);
    }

    private static <T extends NamedEntity<?> & OntologyElement<T, ?>> Set<T>
    getRelativeElements(Ontology<T, ?> ont, T startElement, boolean ancestors, boolean directRelsOnly) {
        log.traceEntry("{}, {}, {}", ont, startElement, directRelsOnly);
        if (ont == null || startElement == null) {
            //Set.of does not accept null elements and startElement can be null
            Set<T> result = new HashSet<>();
            result.add(startElement);
            return log.traceExit(result);
        }
        return log.traceExit(
                Stream.concat(
                    Stream.of(startElement),
                    ancestors?
                        ont.getAncestors(startElement, directRelsOnly).stream():
                        ont.getDescendants(startElement, directRelsOnly).stream()
                ).collect(Collectors.toSet()));
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
        return log.traceExit(this.getRelativeConditions(cond, false, directRelOnly));
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
