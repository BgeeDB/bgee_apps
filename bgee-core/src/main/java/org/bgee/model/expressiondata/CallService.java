package org.bgee.model.expressiondata;

import java.math.BigDecimal;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.CommonService;
import org.bgee.model.Service;
import org.bgee.model.ServiceFactory;
import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.anatdev.AnatEntityService;
import org.bgee.model.anatdev.DevStage;
import org.bgee.model.anatdev.DevStageService;
import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.expressiondata.CallDAOFilter;
import org.bgee.model.dao.api.expressiondata.CallDataDAOFilter;
import org.bgee.model.dao.api.expressiondata.ConditionDAO;
import org.bgee.model.dao.api.expressiondata.ConditionDAO.ConditionTO;
import org.bgee.model.dao.api.expressiondata.ConditionDAO.ConditionTOResultSet;
import org.bgee.model.dao.api.expressiondata.DAOConditionFilter;
import org.bgee.model.dao.api.expressiondata.DAODataType;
import org.bgee.model.dao.api.expressiondata.DAOExperimentCount;
import org.bgee.model.dao.api.expressiondata.DAOExperimentCountFilter;
import org.bgee.model.dao.api.expressiondata.DAOPropagationState;
import org.bgee.model.dao.api.expressiondata.GlobalExpressionCallDAO;
import org.bgee.model.dao.api.expressiondata.GlobalExpressionCallDAO.GlobalExpressionCallDataTO;
import org.bgee.model.dao.api.expressiondata.GlobalExpressionCallDAO.GlobalExpressionCallTO;
import org.bgee.model.dao.api.gene.GeneDAO;
import org.bgee.model.expressiondata.Call.DiffExpressionCall;
import org.bgee.model.expressiondata.Call.ExpressionCall;
import org.bgee.model.expressiondata.CallData.ExpressionCallData;
import org.bgee.model.expressiondata.CallFilter.DiffExpressionCallFilter;
import org.bgee.model.expressiondata.CallFilter.ExpressionCallFilter;
import org.bgee.model.expressiondata.baseelements.CallType;
import org.bgee.model.expressiondata.baseelements.CallType.Expression;
import org.bgee.model.expressiondata.baseelements.DataPropagation;
import org.bgee.model.expressiondata.baseelements.PropagationState;
import org.bgee.model.expressiondata.baseelements.DataQuality;
import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.model.expressiondata.baseelements.ExperimentExpressionCount;
import org.bgee.model.expressiondata.baseelements.SummaryCallType;
import org.bgee.model.expressiondata.baseelements.SummaryCallType.ExpressionSummary;
import org.bgee.model.expressiondata.baseelements.SummaryQuality;
import org.bgee.model.gene.Gene;
import org.bgee.model.gene.GeneNotFoundException;
import org.bgee.model.species.Species;

/**
 * A {@link Service} to obtain {@link Call} objects. 
 * Users should use the {@link org.bgee.model.ServiceFactory ServiceFactory} to obtain {@code CallService}s.
 * 
 * @author  Frederic Bastian
 * @author  Valentine Rech de Laval
 * @version Bgee 14, Mar. 2017
 * @since   Bgee 13, Oct. 2015
 */
//******************
// Notes from previous CallFilter version, certainly obsolete now
//****************** 
// - if both IDs and multiple species requested AND forceHomology is true 
//   => find missing orthologous genes/homologous organs/comparable stages
// - impact both the gene filtering and the anat.entity and stage filtering.
//Species should be always explicitly targeted.
//    private final TaxonomyFilter taxonFilter; 
// - with this boolean set to true, any multi-species query will search explicitly 
//for homology/orthology relations, and will complete ID list provided to potentially 
//add homolog/orthologs (i.e., impacting both ConditionFilters and GeneFilters).
//if false, then any query is possible, without caring about homology/orthology.
// - If true, retrieve results only in homologous structure/comparable stages, always.
//    private final boolean forceHomology;
//******************
public class CallService extends CommonService {
    private final static Logger log = LogManager.getLogger(CallService.class.getName());

    //*************************************************
    // INTERNAL CLASSES
    //*************************************************
    /**
     * {@code Enum} used to define the attributes to populate in the {@code Call}s 
     * obtained from this {@code CallService}.
     * <ul>
     * <li>{@code GENE}: corresponds to {@link Call#getGene()}.
     * <li>{@code ANAT_ENTITY_ID}: corresponds to {@link Condition#getAnatEntityId()} from {@link Call#getCondition()}.
     * <li>{@code DEV_STAGE_ID}: corresponds to {@link Condition#getDevStageId()} from {@link Call#getCondition()}.
     * <li>{@code CALL_TYPE}: corresponds to {@link Call#getSummaryCallType()}.
     * <li>{@code DATA_QUALITY}: corresponds to {@link Call#getSummaryQuality()}.
     * <li>{@code OBSERVED_DATA}: corresponds to {@link Call#getPropagationState()}.
     * <li>{@code GLOABAL_MEAN_RANK}: corresponds to {@link ExpressionCall#getGlobalMeanRank()}.
     * <li>{@code EXPERIMENT_COUNTS}: corresponds to {@link Call#getCallData()} with experiment
     * expression <strong>total</strong> and <strong>self</strong> counts populated per data type
     * for the requested data types.
     * <li>{@code DATA_TYPE_RANK_INFO}: corresponds to {@link Call#getCallData()}
     * with rank info populated per data type for the requested data types.
     * </ul>
     */
    public static enum Attribute implements Service.Attribute {
        GENE(false), ANAT_ENTITY_ID(true), DEV_STAGE_ID(true), CALL_TYPE(false),
        DATA_QUALITY(false), OBSERVED_DATA(false), GLOBAL_MEAN_RANK(false),
        EXPERIMENT_COUNTS(false), DATA_TYPE_RANK_INFO(false);
        
        /**
         * @see #isConditionParameter()
         */
        private final boolean conditionParameter;

        private Attribute(boolean conditionParameter) {
            this.conditionParameter = conditionParameter;
        }

        /**
         * @return  A {@code boolean} defining whether this attribute corresponds 
         *          to a condition parameter (anat entity, stage, sex, strain), allowing to 
         *          determine which condition to target for queries.
         */
        public boolean isConditionParameter() {
            return this.conditionParameter;
        }
    }

    public static enum OrderingAttribute implements Service.OrderingAttribute {
        GENE_ID(false), ANAT_ENTITY_ID(true), DEV_STAGE_ID(true), GLOBAL_RANK(false);
        
        /**
         * @see #isConditionParameter()
         */
        private final boolean conditionParameter;

        private OrderingAttribute(boolean conditionParameter) {
            this.conditionParameter = conditionParameter;
        }

        /**
         * @return  A {@code boolean} defining whether this attribute corresponds 
         *          to a condition parameter (anat entity, stage, sex, strain), allowing to 
         *          determine which condition to target for queries.
         */
        public boolean isConditionParameter() {
            return this.conditionParameter;
        }
    }


    //*************************************************
    // STATIC ATTRIBUTES
    //*************************************************
    /**
     * An {@code int} that is the minimum count showing expression with a low quality
     * for {@code SummaryQuality.BRONZE).
     */
    private final static int MIN_LOW_BRONZE = 1;
    /**
     * An {@code int} that is the minimum count showing expression with a high quality
     * for {@code SummaryQuality.BRONZE).
     */
    private final static int MIN_HIGH_BRONZE = 1;
    /**
     * An {@code int} that is the minimum count showing expression with a low quality
     * for {@code SummaryQuality.SILVER).
     */
    private final static int MIN_LOW_SILVER = 2;
    /**
     * An {@code int} that is the minimum count showing expression with a high quality
     * for {@code SummaryQuality.SILVER).
     */
    private final static int MIN_HIGH_SILVER = 1;
    /**
     * An {@code int} that is the minimum count showing expression with a high quality
     * for {@code SummaryQuality.GOLD).
     */
    private final static int MIN_HIGH_GOLD = 2;

    protected static final DataPropagation DATA_PROPAGATION_IDENTITY = new DataPropagation(null, null, null);
    protected final static Set<PropagationState> ALLOWED_PROP_STATES = EnumSet.of(
            PropagationState.SELF, PropagationState.ANCESTOR, PropagationState.DESCENDANT,
            PropagationState.SELF_AND_ANCESTOR, PropagationState.SELF_AND_DESCENDANT,
            PropagationState.ANCESTOR_AND_DESCENDANT, PropagationState.ALL);

    //*************************************************
    // INSTANCE ATTRIBUTES AND CONSTRUCTOR
    //*************************************************
    protected final ConditionDAO conditionDAO;
    private final GeneDAO geneDAO;
    private final GlobalExpressionCallDAO globalExprCallDAO;
    private final AnatEntityService anatEntityService;
    private final DevStageService devStageService;
    /**
     * @see #getMaxRank()
     */
    private final BigDecimal maxRank;
    /**
     * @param serviceFactory            The {@code ServiceFactory} to be used to obtain {@code Service}s 
     *                                  and {@code DAOManager}.
     * @throws IllegalArgumentException If {@code serviceFactory} is {@code null}.
     */
    public CallService(ServiceFactory serviceFactory) {
        this(serviceFactory,
                Optional.ofNullable(serviceFactory.getDAOManager().getConditionDAO().getMaxRank())
                .map(maxRankTO -> maxRankTO.getGlobalMaxRank())
                .orElseThrow(() -> new IllegalStateException("No max rank could be retrieved.")));
    }
    /**
     * Constructor useful in case the ranks and max ranks have not yet been inserted,
     * before expression call propagation. There is no check performed to make sure
     * a max rank could be retrieved.
     *
     * @param serviceFactory    The {@code ServiceFactory} to be used to obtain {@code Service}s 
     *                          and {@code DAOManager}.
     * @param maxRank           A {@code BigDecimal} that is the max expression rank over
     *                          all conditions and data types. Can be {@code null}.
     */
    protected CallService(ServiceFactory serviceFactory, BigDecimal maxRank) {
        super(serviceFactory);
        this.conditionDAO = this.getDaoManager().getConditionDAO();
        this.geneDAO = this.getDaoManager().getGeneDAO();
        this.globalExprCallDAO = this.getDaoManager().getGlobalExpressionCallDAO();
        this.anatEntityService = this.getServiceFactory().getAnatEntityService();
        this.devStageService = this.getServiceFactory().getDevStageService();
        this.maxRank = maxRank;
    }

    /**
     * @return  A {@code BigDecimal} that is the max expression rank over all conditions and data types.
     */
    public BigDecimal getMaxRank() {
        return maxRank;
    }

    //*************************************************
    // LOAD CALLS PUBLIC METHODS
    //*************************************************
    /** 
     * Load propagated and reconciled expression calls with parameter provided 
     * through an {@code ExpressionCallFilter}. Notably, the species ID is defined
     * through the {@code GeneFilter} in {@code callFilter}.
     *
     * @param callFilter            An {@code ExpressionCallFilter} allowing 
     *                              to filter retrieving of data. Can be {@code null}
     *                              if not further filtering than the speciesId is needed.
     * @param attributes            A {@code Collection} of {@code Attribute}s defining the
     *                              attributes to populate in the returned {@code ExpressionCall}s.
     *                              If {@code null} or empty, all attributes are populated. 
     * @param orderingAttributes    A {@code LinkedHashMap} where keys are 
     *                              {@code CallService.OrderingAttribute}s defining the attributes
     *                              used to order the returned {@code ExpressionCall}s, 
     *                              the associated value being a {@code Service.Direction} defining 
     *                              whether the ordering should be ascendant or descendant.
     * @return                      A {@code Stream} of {@code ExpressionCall}s that are propagated
     *                              and reconciled expression calls.
     * @throws IllegalArgumentException If {@code callFilter} or {@code speciesID} are null or empty.
     * @throws GeneNotFoundException    If some requested genes in {@code GeneFilter}s were not found
     *                                  in Bgee.
     */
    public Stream<ExpressionCall> loadExpressionCalls(ExpressionCallFilter callFilter,
            Collection<Attribute> attributes, 
            LinkedHashMap<OrderingAttribute, Service.Direction> orderingAttributes)
                    throws GeneNotFoundException, IllegalArgumentException {
        log.entry(callFilter, attributes, orderingAttributes);

        final Set<Attribute> clonedAttrs = Collections.unmodifiableSet(
            attributes == null? EnumSet.noneOf(Attribute.class): EnumSet.copyOf(attributes));
        final LinkedHashMap<OrderingAttribute, Service.Direction> clonedOrderingAttrs = 
            orderingAttributes == null? new LinkedHashMap<>(): new LinkedHashMap<>(orderingAttributes);

        // Sanity checks
        if (callFilter == null || callFilter.getGeneFilters() == null ||
                callFilter.getGeneFilters().isEmpty()) {
            throw log.throwing(new IllegalArgumentException("A CallFilter must be provided, "
                    + "at least to specify the targeted Species (through the GeneFilters)"));
        }

        // Retrieve species, get a map species ID -> Species
        final Set<Integer> clnSpeIds =  Collections.unmodifiableSet(
                callFilter.getGeneFilters().stream().map(f -> f.getSpeciesId())
                .collect(Collectors.toSet()));
        final Map<Integer, Species> speciesMap = Collections.unmodifiableMap(
                this.getServiceFactory().getSpeciesService().loadSpeciesByIds(clnSpeIds, false)
                .stream().collect(Collectors.toMap(s -> s.getId(), s -> s)));
        if (speciesMap.size() != clnSpeIds.size()) {
            throw new IllegalArgumentException("Some provided species not found in data source");
        }

        //Retrieve a Map of Bgee gene IDs to Gene. This will throw a GeneNotFoundException
        //if some requested gene IDs were not found in Bgee.
        Map<Integer, Gene> geneMap = this.loadGeneMap(callFilter, speciesMap);
        assert !geneMap.isEmpty();

        // Define condition parameter combination allowing to target a specific data aggregation
        final Set<ConditionDAO.Attribute> condParamCombination = Collections.unmodifiableSet(
                loadConditionParameterCombination(callFilter, clonedAttrs, clonedOrderingAttrs.keySet()));

        // Retrieve conditions by condition IDs if condition info requested in Attributes
        final Map<Integer, Condition> condMap = Collections.unmodifiableMap(
                clonedAttrs.stream().noneMatch(a -> a.isConditionParameter())? new HashMap<>():
                    this.loadGlobalConditionMap(speciesMap.values(),
                        condParamCombination, convertCondParamAttrsToCondDAOAttrs(clonedAttrs)));

        // Retrieve calls
        Stream<ExpressionCall> calls = this.performsGlobalExprCallQuery(geneMap, callFilter,
                condParamCombination, clonedAttrs, clonedOrderingAttrs)
            .map(to -> mapGlobalCallTOToExpressionCall(to, geneMap, condMap, callFilter, this.getMaxRank(),
                    clonedAttrs))
//            // Job of the DAO to filter retrieved calls now.
//            .filter(c -> callFilter == null || callFilter.test(c))
//            // job of the DAO to do the ordering now.
//            .sorted(CallService.convertServiceOrdering(clonedOrderingAttrs))
            ;

        return log.exit(calls);
    }
    
    public Stream<DiffExpressionCall> loadDiffExpressionCalls(Integer speciesId, 
            DiffExpressionCallFilter callFilter) {
        log.entry(speciesId, callFilter);
        throw log.throwing(new UnsupportedOperationException("Load of diff. expression calls not implemented yet"));
    }
    
    //*************************************************************************
    // METHODS PERFORMING THE QUERIES TO THE DAOs
    //*************************************************************************
    /**
     * 
     * @param species
     * @param condParamCombination  A {@code Set} of {@code ConditionDAO.Attribute}s defining
     *                              the combination of condition parameters that were requested
     *                              for queries, allowing to determine which condition and expression
     *                              results to target.
     * @param conditionDAOAttrs     A {@code Set} of {@code ConditionDAO.Attribute}s defining
     *                              the attributes to populate in the retrieved {@code ConditionTO}s,
     *                              and thus, in the returned {@code Condition}s.
     *                              If {@code null} or empty, then all attributes are retrieved.
     * @return                      A {@code Map} where keys are {@code Integer}s
     *                              that are condition IDs, the associated value being
     *                              the corresponding {@code Condition}.
     */
    private Map<Integer, Condition> loadGlobalConditionMap(Collection<Species> species,
            Collection<ConditionDAO.Attribute> condParamCombination,
            Collection<ConditionDAO.Attribute> conditionDAOAttrs) {
        log.entry(species, condParamCombination, conditionDAOAttrs);

        return log.exit(loadConditionMapFromResultSet(
                (attrs) -> this.conditionDAO.getGlobalConditionsBySpeciesIds(
                        species.stream().map(s -> s.getId()).collect(Collectors.toSet()),
                        condParamCombination, attrs),
                conditionDAOAttrs, species));
    }

    protected Map<Integer, Condition> loadConditionMapFromResultSet(
            Function<Collection<ConditionDAO.Attribute>, ConditionTOResultSet> rsFunc,
            Collection<ConditionDAO.Attribute> conditionDAOAttrs, Collection<Species> species) {
        log.entry(rsFunc, conditionDAOAttrs, species);

        if (species == null || species.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("Some species must be provided"));
        }

        Map<Integer, Species> speMap = species.stream()
                .collect(Collectors.toMap(s -> s.getId(), s -> s, (s1, s2) -> s1));
        Set<String> anatEntityIds = new HashSet<>();
        Set<String> stageIds = new HashSet<>();
        Set<ConditionTO> conditionTOs = new HashSet<>();

        //we need to retrieve the attributes requested, plus the condition ID and species ID in all cases.
        Set<ConditionDAO.Attribute> clonedAttrs = conditionDAOAttrs == null || conditionDAOAttrs.isEmpty()?
                EnumSet.allOf(ConditionDAO.Attribute.class): EnumSet.copyOf(conditionDAOAttrs);
        clonedAttrs.addAll(EnumSet.of(ConditionDAO.Attribute.ID, ConditionDAO.Attribute.SPECIES_ID));
        ConditionTOResultSet rs = rsFunc.apply(clonedAttrs);

        while (rs.next()) {
            ConditionTO condTO = rs.getTO();
            if (!speMap.keySet().contains(condTO.getSpeciesId())) {
                throw log.throwing(new IllegalArgumentException(
                        "The retrieved ConditionTOs do not match the provided Species."));
            }
            conditionTOs.add(condTO);
            if (condTO.getAnatEntityId() != null) {
                anatEntityIds.add(condTO.getAnatEntityId());
            }
            if (condTO.getStageId() != null) {
                stageIds.add(condTO.getStageId());
            }
        }

        final Map<String, AnatEntity> anatMap = anatEntityIds.isEmpty()? new HashMap<>():
            this.anatEntityService.loadAnatEntities(
                    speMap.keySet(), true, anatEntityIds, false)
            .collect(Collectors.toMap(a -> a.getId(), a -> a));
        if (!anatEntityIds.isEmpty() && anatMap.size() != anatEntityIds.size()) {
            anatEntityIds.removeAll(anatMap.keySet());
            throw log.throwing(new IllegalStateException("Some anat. entities used in a condition "
                    + "are not supposed to exist in the related species. Species: " + speMap.keySet()
                    + " - anat. entities: " + anatEntityIds));
        }
        final Map<String, DevStage> stageMap = stageIds.isEmpty()? new HashMap<>():
            this.devStageService.loadDevStages(
                    speMap.keySet(), true, stageIds, false)
            .collect(Collectors.toMap(s -> s.getId(), s -> s));
        if (!stageIds.isEmpty() && stageMap.size() != stageIds.size()) {
            stageIds.removeAll(stageMap.keySet());
            throw log.throwing(new IllegalStateException("Some stages used in a condition "
                    + "are not supposed to exist in the related species. Species: " + speMap.keySet()
                    + " - stages: " + stageIds));
        }

        return log.exit(conditionTOs.stream()
                .collect(Collectors.toMap(cTO -> cTO.getId(), 
                        cTO -> mapConditionTOToCondition(cTO,
                                cTO.getAnatEntityId() == null? null:
                                    Optional.ofNullable(anatMap.get(cTO.getAnatEntityId())).orElseThrow(
                                        () -> new IllegalStateException("Anat. entity not found: "
                                                + cTO.getAnatEntityId())),
                                cTO.getStageId() == null? null:
                                    Optional.ofNullable(stageMap.get(cTO.getStageId())).orElseThrow(
                                        () -> new IllegalStateException("Stage not found: "
                                                + cTO.getStageId())),
                                Optional.ofNullable(speMap.get(cTO.getSpeciesId())).orElseThrow(
                                        () -> new IllegalStateException("Species not found: "
                                                + cTO.getSpeciesId())))
                        ))
                );
    }

    private Map<Integer, Gene> loadGeneMap(CallFilter<?, ?> callFilter, Map<Integer, Species> speciesMap)
            throws GeneNotFoundException {
        log.entry(callFilter, speciesMap);

        //First, get a Map species ID -> requested Ensemble geneIds for making the DAO query.
        final Map<Integer, Set<String>> requestedSpeToGeneIdsMap = Collections.unmodifiableMap(
                callFilter.getGeneFilters().stream()
                .collect(Collectors.toMap(gf -> gf.getSpeciesId(), gf -> gf.getEnsemblGeneIds())));

        //Make the DAO query and map GeneTOs to Genes. Store them in a Map to keep the bgeeGeneIds.
        final Map<Integer, Gene> geneMap = Collections.unmodifiableMap(this.geneDAO
                .getGenesBySpeciesAndGeneIds(requestedSpeToGeneIdsMap)
                .stream()
                .collect(Collectors.toMap(
                        gTO -> gTO.getId(),
                        gTO -> mapGeneTOToGene(gTO,
                                Optional.ofNullable(speciesMap.get(gTO.getSpeciesId()))
                                .orElseThrow(() -> new IllegalStateException("Missing species ID for gene")))
                        )));

        //check that we get all specifically requested genes.
        //First, build a Map Species ID -> Ensembl gene IDs for the retrieved genes.
        final Map<Integer, Set<String>> retrievedSpeToGeneIdsMap = geneMap.values().stream()
                .collect(Collectors.toMap(g -> g.getSpecies().getId(),
                        g -> Stream.of(g.getEnsemblGeneId()).collect(Collectors.toSet()),
                        (s1, s2) -> {s1.addAll(s2); return s1;}));
        //now, check that we found all requested genes.
        Map<Integer, Set<String>> notFoundSpeToGeneIdsMap = requestedSpeToGeneIdsMap.entrySet().stream()
                .map(e -> {
                    Set<String> retrievedGeneIds = retrievedSpeToGeneIdsMap.get(e.getKey());
                    if (e.getValue().isEmpty()) {
                        //if no genes for the requested species, the whole species is offending
                        if (retrievedGeneIds == null) {
                            return e;
                        }
                        //otherwise, it's OK, we found some genes for that species
                        return null;
                    }
                    //Now, if some specific IDs were requested, check we got all of them
                    if (e.getValue().size() == retrievedGeneIds.size()) {
                        return null;
                    }
                    Set<String> offendingGeneIds = e.getValue().stream()
                            .filter(id -> !retrievedGeneIds.contains(id))
                            .collect(Collectors.toSet());
                    return new AbstractMap.SimpleEntry<>(e.getKey(), offendingGeneIds);
                })
                .filter(e -> e != null)
                .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
        if (!notFoundSpeToGeneIdsMap.isEmpty()) {
            throw log.throwing(new GeneNotFoundException(notFoundSpeToGeneIdsMap));
        }

        return log.exit(geneMap);
    }

    /**
     * Perform query to retrieve expressed calls without the post-processing of 
     * the results returned by {@code DAO}s.
     *
     * @param callFilter            An {@code ExpressionCallFilter} allowing 
     *                              to configure retrieving of data throw {@code DAO}s.
     *                              Cannot be {@code null} or empty (check by calling methods).
     * @param attributes            A {@code Set} of {@code Attribute}s defining the attributes
     *                              to populate in the returned {@code ExpressionCall}s.
     *                              If {@code null} or empty, all attributes are populated. 
     * @param orderingAttributes    A {@code LinkedHashMap} where keys are 
     *                              {@code CallService.OrderingAttribute}s defining the attributes
     *                              used to order the returned {@code ExpressionCall}s, 
     *                              the associated value being a {@code Service.Direction} defining 
     *                              whether the ordering should be ascendant or descendant.
     * @return                      The {@code Stream} of {@code GlobalExpressionCallTO}s.
     * @throws IllegalArgumentException If the {@code callFilter} provided define multiple 
     *                                  expression propagation states requested.
     */
    private Stream<GlobalExpressionCallTO> performsGlobalExprCallQuery(Map<Integer, Gene> geneMap,
            ExpressionCallFilter callFilter, Set<ConditionDAO.Attribute> condParamCombination,
            Set<Attribute> attributes, LinkedHashMap<OrderingAttribute, Service.Direction> orderingAttributes)
                    throws IllegalArgumentException {
        log.entry(geneMap, callFilter, condParamCombination, attributes, orderingAttributes);

        //now we map each GeneFilter to Bgee gene IDs rather than Ensembl gene IDs.
        Set<Integer> geneIdFilter = null;
        Set<Integer> speciesIds = null;
        if (callFilter != null) {
            //generate a Map Species ID -> (Map Ensembl ID -> Bgee gene ID)
            final Map<Integer, Map<String, Integer>> speToEnsemblToBgeeIds = geneMap.entrySet().stream()
                    //return one Entry<Integer, Map<String, Integer>> for each gene retrieved
                    .map(e -> {
                        Map<String, Integer> ensemblToBgeeIds = new HashMap<>();
                        ensemblToBgeeIds.put(e.getValue().getEnsemblGeneId(), e.getKey());
                        return new AbstractMap.SimpleEntry<>(e.getValue().getSpecies().getId(),
                                ensemblToBgeeIds);
                    })
                    //merge the Map<String, Integer> in values for a same speciesId in key
                    .collect(Collectors.toMap(
                            e -> e.getKey(),
                            e -> e.getValue(),
                            (v1, v2) -> {v1.putAll(v2); return v1;}));

            //Now retrieve the Bgee gene IDs of the requested genes in GeneFilter.
            geneIdFilter = callFilter.getGeneFilters().stream()
                    .filter(gf -> speToEnsemblToBgeeIds.containsKey(gf.getSpeciesId()))
                    .flatMap(gf -> gf.getEnsemblGeneIds().stream()
                            .map(ensemblId -> speToEnsemblToBgeeIds.get(gf.getSpeciesId())
                                    .get(ensemblId))
                    )
                    .collect(Collectors.toSet());
            //the method retrieving the requested genes (loadGeneMap) should have made sure
            //that all requested genes were found
            assert !geneIdFilter.contains(null);

            //Identify the species IDs for which no gene IDs were specifically requested,
            //maybe no specific ID was requested for some species.
            speciesIds = callFilter.getGeneFilters().stream()
                    .filter(gf -> gf.getEnsemblGeneIds().isEmpty())
                    .map(gf -> gf.getSpeciesId())
                    .collect(Collectors.toSet());
        }

        if ((speciesIds == null || speciesIds.isEmpty()) &&
                (geneIdFilter == null || geneIdFilter.isEmpty())) {
            throw log.throwing(new IllegalArgumentException(
                    "No species nor gene IDs retrieved for filtering results."));
        }
        
        Stream<GlobalExpressionCallTO> calls = this.globalExprCallDAO
            .getGlobalExpressionCalls(Arrays.asList(
                //generate an ExpressionCallDAOFilter from callFilter 
                new CallDAOFilter(
                    // gene IDs
                    geneIdFilter, 
                    //species
                    speciesIds,
                    //ConditionFilters
                    callFilter == null || callFilter.getConditionFilters() == null? null:
                        callFilter.getConditionFilters().stream()
                        .map(condFilter -> new DAOConditionFilter(
                            condFilter.getAnatEntityIds(),
                            condFilter.getDevStageIds(),
                            condFilter.getObservedConditions()))
                        .collect(Collectors.toSet()),
                    //CallDataDAOFilters
                    convertCallFilterToCallDataDAOFilters(callFilter),
                    //observedDataFilters
                    callFilter.getCallObservedData(),
                    convertCallFilterToDAOObservedDataFilter(callFilter, condParamCombination)
                )),
                // Condition parameters
                condParamCombination,
                // Attributes
                convertServiceAttrToGlobalExprDAOAttr(attributes),
                convertServiceOrderingAttrToGlobalExprDAOOrderingAttr(orderingAttributes))
            //retrieve the Stream resulting from the query. Note that the query is not executed 
            //as long as the Stream is not consumed (lazy-loading).
            .stream();

        return log.exit(calls);
    }

    //*************************************************************************
    // HELPER METHODS CONVERTING INFORMATION FROM Call LAYER to DAO LAYER
    //*************************************************************************
    /**
     * 
     * @param callFilter
     * @param serviceAttrs
     * @param serviceOrderingAttrs
     * @return                      An unmodifiable {@code Set} of {@code ConditionDAO.Attribute}s
     *                              allowing {@code DAO}s to determine which tables to target
     *                              in the data source.
     */
    private static Set<ConditionDAO.Attribute> loadConditionParameterCombination(
            ExpressionCallFilter callFilter, Set<Attribute> serviceAttrs,
            Set<OrderingAttribute> serviceOrderingAttrs) {
        log.entry(callFilter, serviceAttrs, serviceOrderingAttrs);

        final Set<ConditionDAO.Attribute> allDAOCondParamAttrs = EnumSet.allOf(ConditionDAO.Attribute.class)
                .stream().filter(a -> a.isConditionParameter())
                .collect(Collectors.toSet());
    
        Set<ConditionDAO.Attribute> attrs = convertCondParamAttrsToCondDAOAttrs(serviceAttrs);
        Set<ConditionDAO.Attribute> orderingAttrs = 
                convertCondParamOrderingAttrsToCondDAOAttrs(serviceOrderingAttrs);
        Set<ConditionDAO.Attribute> filterAttrs = new HashSet<>();
        if (callFilter != null && callFilter.getConditionFilters() != null) {
            filterAttrs = callFilter.getConditionFilters().stream()
                .flatMap(condFilter -> {
                    Set<ConditionDAO.Attribute> daoAttrs = new HashSet<>();
                    for (ConditionDAO.Attribute daoAttr: allDAOCondParamAttrs) {
                        switch (daoAttr) {
                        case ANAT_ENTITY_ID:
                            if (!condFilter.getAnatEntityIds().isEmpty()) {
                                daoAttrs.add(ConditionDAO.Attribute.ANAT_ENTITY_ID);
                            }
                            break;
                        case STAGE_ID:
                            if (!condFilter.getDevStageIds().isEmpty()) {
                                daoAttrs.add(ConditionDAO.Attribute.STAGE_ID);
                            }
                            break;
                        default:
                            throw log.throwing(new UnsupportedOperationException(
                                    "ConditionDAO.Attribute not supported: " + daoAttr));
                        }
                    }
                    return daoAttrs.stream();
                }).collect(Collectors.toSet());
        }
        if (callFilter != null && callFilter.getAnatEntityObservedData() != null) {
            filterAttrs.add(ConditionDAO.Attribute.ANAT_ENTITY_ID);
        }
        if (callFilter != null && callFilter.getDevStageObservedData() != null) {
            filterAttrs.add(ConditionDAO.Attribute.STAGE_ID);
        }

        Set<ConditionDAO.Attribute> daoCondParamComb = new HashSet<>();
        daoCondParamComb.addAll(attrs);
        daoCondParamComb.addAll(orderingAttrs);
        daoCondParamComb.addAll(filterAttrs);

        if (daoCondParamComb.isEmpty()) {
            throw log.throwing(new IllegalArgumentException(
                    "No parameter allowing to determine a condition parameter combination"));
        }

        return log.exit(Collections.unmodifiableSet(daoCondParamComb));
    }
    private static Set<CallDataDAOFilter> convertCallFilterToCallDataDAOFilters(
            ExpressionCallFilter callFilter) {
        log.entry(callFilter);

        //just to make sure SummaryQuality.BRONZE is the lowest Quality level
        assert SummaryQuality.values()[0].equals(SummaryQuality.BRONZE);
        //now determine whether the lowest quality level was requested
        boolean lowestQual = callFilter.getSummaryCallTypeQualityFilter().entrySet().stream()
                .allMatch(e -> SummaryQuality.BRONZE.equals(e.getValue()));
        //now determine whether all data types were requested
        boolean allDataTypesSelected = callFilter.getDataTypeFilters() == null ||
                callFilter.getDataTypeFilters().isEmpty() ||
                callFilter.getDataTypeFilters().equals(EnumSet.allOf(DataType.class));
        //just to make sure we cover all ExpressionSummary cases
        assert SummaryCallType.ExpressionSummary.values().length == 2;
        //now determine whether all CallTypes were requested
        boolean allCallTypesSelected = callFilter.getSummaryCallTypeQualityFilter().keySet().size() == 2;

        //absolutely no filtering necessary on experiment expression counts in following case
        if (allCallTypesSelected && lowestQual && allDataTypesSelected &&
                //this is true only as long as the minimum experiment count threshold is 1
                MIN_LOW_BRONZE <= 1 && MIN_HIGH_BRONZE <= 1) {
            assert callFilter.getSummaryCallTypeQualityFilter().containsKey(
                    SummaryCallType.ExpressionSummary.EXPRESSED) &&
                           callFilter.getSummaryCallTypeQualityFilter().containsKey(
                            SummaryCallType.ExpressionSummary.NOT_EXPRESSED);
            return log.exit(null);
        }

        Set<DAODataType> daoDataTypes = Collections.unmodifiableSet(
                convertDataTypeToDAODataType(callFilter.getDataTypeFilters()));

        //see org.bgee.model.dao.api.expressiondata.CallDAOFilter.getDataFilters()
        //for more details
        Set<CallDataDAOFilter> callDataDAOFilters = null;

        //filters for callTypes/quality if necessary.
        if (!allCallTypesSelected || !lowestQual ||
                //if not all data types were requested and it is the only filtering requested,
                //then we don't need to create a filter for each call type requested (see 'else' clause below).
                //But it is true only as long as the minimum experiment count threshold is 1
                (!allDataTypesSelected && (MIN_LOW_BRONZE > 1 || MIN_HIGH_BRONZE > 1))) {

            callDataDAOFilters = callFilter.getSummaryCallTypeQualityFilter().entrySet().stream().map(e -> {
                SummaryCallType.ExpressionSummary requestedCallType = e.getKey();
                SummaryQuality requestedQual = e.getValue();

                //see org.bgee.model.dao.api.expressiondata.CallDataDAOFilter.getExperimentCountFilters()
                //for more details
                Set<Set<DAOExperimentCountFilter>> daoExperimentCountFilters = new HashSet<>();
  
                Boolean isExpression = null;
                switch (requestedCallType) {
                case EXPRESSED:
                    isExpression = true;
                    break;
                case NOT_EXPRESSED:
                    isExpression = false;
                    break;
                default:
                    throw log.throwing(new IllegalStateException("Unsupported call type: "
                            + requestedCallType));
                }
                assert isExpression != null;

                //reject expression if no expression is requested, through "AND" filters
                //(so, each filter in a separate Set)
                if (!isExpression) {
                    Set<Set<DAOExperimentCountFilter>> rejectExpressionFilters =
                            EnumSet.allOf(DAOExperimentCount.DataQuality.class).stream()
                            .map(dataQual -> Collections.singleton(new DAOExperimentCountFilter(
                                    DAOExperimentCount.CallType.PRESENT, dataQual,
                                    DAOPropagationState.ALL,
                                    DAOExperimentCountFilter.Qualifier.EQUALS_TO, 0)))
                            .collect(Collectors.toSet());

                    daoExperimentCountFilters.addAll(rejectExpressionFilters);
                }

                //requested call type "OR" filters.
                //assert just to make sure we cover all DAOExperimentCount.CallTypes
                assert DAOExperimentCount.CallType.values().length == 2;
                final DAOExperimentCount.CallType daoCallType = convertSummaryCallTypeToDAOCallType(
                        requestedCallType);
                Set<DAOExperimentCountFilter> acceptCallTypeFilters = new HashSet<>();
                switch (requestedQual) {
                case BRONZE:
                    acceptCallTypeFilters.add(new DAOExperimentCountFilter(daoCallType,
                            DAOExperimentCount.DataQuality.LOW,
                            DAOPropagationState.ALL,
                            DAOExperimentCountFilter.Qualifier.GREATER_THAN, MIN_LOW_BRONZE - 1));
                    //also need to get calls supported by high quality data only
                    acceptCallTypeFilters.add(new DAOExperimentCountFilter(daoCallType,
                            DAOExperimentCount.DataQuality.HIGH,
                            DAOPropagationState.ALL,
                            DAOExperimentCountFilter.Qualifier.GREATER_THAN, MIN_HIGH_BRONZE - 1));
                    break;
                case SILVER:
                    acceptCallTypeFilters.add(new DAOExperimentCountFilter(daoCallType,
                            DAOExperimentCount.DataQuality.LOW,
                            DAOPropagationState.ALL,
                            DAOExperimentCountFilter.Qualifier.GREATER_THAN, MIN_LOW_SILVER - 1));
                    //also need to get calls supported by high quality data only
                    acceptCallTypeFilters.add(new DAOExperimentCountFilter(daoCallType,
                            DAOExperimentCount.DataQuality.HIGH,
                            DAOPropagationState.ALL,
                            DAOExperimentCountFilter.Qualifier.GREATER_THAN, MIN_HIGH_SILVER - 1));
                    break;
                case GOLD:
                    acceptCallTypeFilters.add(new DAOExperimentCountFilter(daoCallType,
                            DAOExperimentCount.DataQuality.HIGH,
                            DAOPropagationState.ALL,
                            DAOExperimentCountFilter.Qualifier.GREATER_THAN, MIN_HIGH_GOLD - 1));
                    break;
                default:
                    throw log.throwing(new UnsupportedOperationException(
                            "Unsupported SummaryQuality: " + requestedQual));
                }

                assert !acceptCallTypeFilters.isEmpty();
                daoExperimentCountFilters.add(acceptCallTypeFilters);

                return new CallDataDAOFilter(daoExperimentCountFilters, daoDataTypes);
            }).collect(Collectors.toSet());

        //if no filtering on call type and quality needed but not all data types requested,
        //we need to add a filtering here.
        //No need for a clause 'else if (!allDataTypesSelected) {',
        //the method should have already exited if allDataTypesSelected was true
        } else {
            assert allCallTypesSelected && lowestQual;
            assert !allDataTypesSelected && MIN_LOW_BRONZE <= 1 && MIN_HIGH_BRONZE <= 1;

            Set<Set<DAOExperimentCountFilter>> daoExperimentCountFilters = new HashSet<>();

            daoExperimentCountFilters.add(callFilter
                    .getSummaryCallTypeQualityFilter().entrySet().stream()
                    .flatMap(e -> EnumSet.allOf(DAOExperimentCount.DataQuality.class)
                            .stream()
                            .map(dataQual -> new DAOExperimentCountFilter(
                                    convertSummaryCallTypeToDAOCallType(e.getKey()), dataQual,
                                    DAOPropagationState.ALL,
                                    DAOExperimentCountFilter.Qualifier.GREATER_THAN, 0)))
                    .collect(Collectors.toSet()));

            callDataDAOFilters = Collections.singleton(
                    new CallDataDAOFilter(daoExperimentCountFilters, daoDataTypes));
        }

        //the method should have exited right away if no filtering was necessary
        assert callDataDAOFilters != null && !callDataDAOFilters.isEmpty();
        return log.exit(callDataDAOFilters);
    }
    private static Map<ConditionDAO.Attribute, Boolean> convertCallFilterToDAOObservedDataFilter(
            ExpressionCallFilter callFilter, Set<ConditionDAO.Attribute> condParamCombination) {
        log.entry(callFilter, condParamCombination);

        Map<ConditionDAO.Attribute, Boolean> filter = new HashMap<>();
        if (callFilter != null && callFilter.getAnatEntityObservedData() != null) {
            if (!condParamCombination.contains(ConditionDAO.Attribute.ANAT_ENTITY_ID)) {
                throw log.throwing(new IllegalArgumentException(
                        "Inconsistent condition parameter combination and requested observed data"));
            }
            filter.put(ConditionDAO.Attribute.ANAT_ENTITY_ID, callFilter.getAnatEntityObservedData());
        }
        if (callFilter!= null && callFilter.getDevStageObservedData() != null) {
            if (!condParamCombination.contains(ConditionDAO.Attribute.STAGE_ID)) {
                throw log.throwing(new IllegalArgumentException(
                        "Inconsistent condition parameter combination and requested observed data"));
            }
            filter.put(ConditionDAO.Attribute.STAGE_ID, callFilter.getDevStageObservedData());
        }
        return log.exit(filter);
    }

    private static DAOExperimentCount.CallType convertSummaryCallTypeToDAOCallType(
            SummaryCallType.ExpressionSummary callType) {
        log.entry(callType);

        switch(callType) {
        case EXPRESSED:
            return log.exit(DAOExperimentCount.CallType.PRESENT);
        case NOT_EXPRESSED:
            return log.exit(DAOExperimentCount.CallType.ABSENT);
        default:
            throw log.throwing(new IllegalArgumentException("Unsupported CallType: " + callType));
        }
    }

    private static Set<ConditionDAO.Attribute> convertCondParamAttrsToCondDAOAttrs(
            Set<Attribute> attrs) {
        log.entry(attrs);
        return log.exit(attrs.stream()
                .filter(a -> a.isConditionParameter())
                .map(a -> {
                    switch (a) {
                        case ANAT_ENTITY_ID:
                            return ConditionDAO.Attribute.ANAT_ENTITY_ID;
                        case DEV_STAGE_ID: 
                            return ConditionDAO.Attribute.STAGE_ID;                        
                        default: 
                            throw log.throwing(new UnsupportedOperationException(
                                "Condition parameter not supported: " + a));
                    }
                }).collect(Collectors.toSet()));
    }
    private static Set<ConditionDAO.Attribute> convertCondParamOrderingAttrsToCondDAOAttrs(
            Set<OrderingAttribute> attrs) {
        log.entry(attrs);
        return log.exit(attrs.stream()
                .filter(a -> a.isConditionParameter())
                .map(a -> {
                    switch (a) {
                        case ANAT_ENTITY_ID:
                            return ConditionDAO.Attribute.ANAT_ENTITY_ID;
                        case DEV_STAGE_ID: 
                            return ConditionDAO.Attribute.STAGE_ID;                        
                        default: 
                            throw log.throwing(new UnsupportedOperationException(
                                "Condition parameter not supported: " + a));
                    }
                }).collect(Collectors.toSet()));
    }

    private static Set<GlobalExpressionCallDAO.Attribute> convertServiceAttrToGlobalExprDAOAttr(
        Set<Attribute> attributes) {
        log.entry(attributes);
        
        return log.exit(attributes.stream().flatMap(attr -> {
            switch (attr) {
                case GENE: 
                    return Stream.of(GlobalExpressionCallDAO.Attribute.BGEE_GENE_ID);
                case ANAT_ENTITY_ID: 
                case DEV_STAGE_ID: 
                    return Stream.of(GlobalExpressionCallDAO.Attribute.CONDITION_ID);
                case CALL_TYPE: 
                case DATA_QUALITY:
                    return Stream.of(GlobalExpressionCallDAO.Attribute.DATA_TYPE_EXPERIMENT_TOTAL_COUNTS);
                case EXPERIMENT_COUNTS:
                    return Stream.of(GlobalExpressionCallDAO.Attribute.DATA_TYPE_EXPERIMENT_TOTAL_COUNTS,
                            GlobalExpressionCallDAO.Attribute.DATA_TYPE_EXPERIMENT_SELF_COUNTS,
                            GlobalExpressionCallDAO.Attribute.DATA_TYPE_EXPERIMENT_PROPAGATED_COUNTS);
                case OBSERVED_DATA:
                    return Stream.of(GlobalExpressionCallDAO.Attribute.DATA_TYPE_OBSERVED_DATA);
                case GLOBAL_MEAN_RANK:
                    return Stream.of(GlobalExpressionCallDAO.Attribute.GLOBAL_MEAN_RANK);
                case DATA_TYPE_RANK_INFO:
                    return Stream.of(GlobalExpressionCallDAO.Attribute.DATA_TYPE_RANK_INFO);
                default: 
                    throw log.throwing(new IllegalStateException(
                            "Unsupported Attributes from CallService: " + attr));
            }
        }).collect(Collectors.toCollection(() -> EnumSet.noneOf(GlobalExpressionCallDAO.Attribute.class))));
    }

    private static LinkedHashMap<GlobalExpressionCallDAO.OrderingAttribute, DAO.Direction>
    convertServiceOrderingAttrToGlobalExprDAOOrderingAttr(
            LinkedHashMap<CallService.OrderingAttribute, Service.Direction> orderingAttributes) {
        log.entry(orderingAttributes);
        
        return log.exit(orderingAttributes.entrySet().stream().collect(Collectors.toMap(
                e -> {
                    switch (e.getKey()) {
                        case GENE_ID: 
                            return GlobalExpressionCallDAO.OrderingAttribute.GENE_ID;
                        case ANAT_ENTITY_ID:
                            return GlobalExpressionCallDAO.OrderingAttribute.ANAT_ENTITY_ID;
                        case DEV_STAGE_ID: 
                            return GlobalExpressionCallDAO.OrderingAttribute.STAGE_ID;
                        case GLOBAL_RANK:
                            return GlobalExpressionCallDAO.OrderingAttribute.MEAN_RANK;
                        default: 
                            throw log.throwing(new IllegalStateException(
                                    "Unsupported OrderingAttributes from CallService: " + e.getKey()));
                    }
                },
                e -> {
                    switch (e.getValue()) {
                    case ASC: 
                        return DAO.Direction.ASC;
                    case DESC: 
                        return DAO.Direction.DESC;
                    default: 
                        throw log.throwing(new IllegalStateException(
                                "Unsupported ordering Direction from CallService: " + e.getValue()));
                    }
                }, 
                (v1, v2) -> {throw log.throwing(new IllegalStateException("No key collision possible"));}, 
                () -> new LinkedHashMap<GlobalExpressionCallDAO.OrderingAttribute, DAO.Direction>())));
    }

//    /** 
//     * Return the {@code Comparator} of {@code ExpressionCall}s, performing the comparisons
//     * in order provided by {@code orderingAttributes}.
//     * 
//     * @param orderingAttributes    A {@code LinkedHashMap} where keys are 
//     *                              {@code CallService.OrderingAttribute}s defining the attributes
//     *                              used to order the returned {@code ExpressionCall}s, 
//     *                              the associated value being a {@code Service.Direction} defining 
//     *                              whether the ordering should be ascendant or descendant.
//     * @return                      The {@code Comparator} of {@code ExpressionCall}s.
//     */
//    private static Comparator<ExpressionCall> convertServiceOrdering(
//            LinkedHashMap<CallService.OrderingAttribute, Service.Direction> orderingAttributes) {
//        log.entry(orderingAttributes);
//        
//        Comparator<ExpressionCall> comparator = null;
//        for (Entry<CallService.OrderingAttribute, Service.Direction> entry: orderingAttributes.entrySet()) {
//            Comparator<String> compStr = null;
//            Comparator<BigDecimal> compBigD = null;
//            switch (entry.getValue()) {
//                case ASC:
//                    compStr = Comparator.nullsLast(Comparator.naturalOrder());
//                    compBigD = Comparator.nullsLast(Comparator.naturalOrder());
//                    break;
//                case DESC:
//                    compStr = Comparator.nullsLast(Comparator.reverseOrder());
//                    compBigD = Comparator.nullsLast(Comparator.reverseOrder());
//                    break;
//                default: 
//                    throw log.throwing(new IllegalStateException("Unsupported Service.Direction: " +
//                        entry.getValue()));
//            }
//    
//            Comparator<ExpressionCall> tmpComp = null;
//            switch (entry.getKey()) {
//                case GENE_ID:
//                    tmpComp = Comparator.comparing(c -> c.getGene() == null?
//                        null: c.getGene().getEnsemblGeneId(), compStr);
//                    break;
//                case ANAT_ENTITY_ID:
//                    tmpComp = Comparator.comparing(c -> c.getCondition() == null? 
//                        null : c.getCondition().getAnatEntityId(), compStr);
//                    break;
//                case DEV_STAGE_ID:
//                    tmpComp = Comparator.comparing(c -> c.getCondition() == null? 
//                        null : c.getCondition().getDevStageId(), compStr);
//                    break;
//                case GLOBAL_RANK:
//                    tmpComp = Comparator.comparing(c -> c.getGlobalMeanRank(), compBigD);
//                    break;
//                default: 
//                    throw log.throwing(new IllegalStateException("Unsupported OrderingAttribute: " + 
//                        entry.getKey()));
//            }
//            
//            if (comparator == null) {
//                comparator = tmpComp;
//            } else {
//                comparator = comparator.thenComparing(tmpComp);
//            }
//        }
//        return log.exit(comparator);
//    }

    protected static Set<DAODataType> convertDataTypeToDAODataType(Set<DataType> dts) 
            throws IllegalStateException{
        log.entry(dts);
        
        if (dts == null || dts.isEmpty()) {
            return log.exit(EnumSet.allOf(DAODataType.class));
        }
        return log.exit(dts.stream()
            .map(dt -> {
                switch(dt) {
                    case AFFYMETRIX: 
                        return log.exit(DAODataType.AFFYMETRIX);
                    case EST: 
                        return log.exit(DAODataType.EST);
                    case IN_SITU: 
                        return log.exit(DAODataType.IN_SITU);
                    case RNA_SEQ: 
                        return log.exit(DAODataType.RNA_SEQ);
                    default: 
                        throw log.throwing(new IllegalStateException("Unsupported DAODataType: " + dt));
                }
        }).collect(Collectors.toSet()));
    }

    //*************************************************************************
    // METHODS MAPPING GlobalExpressionCallTOs TO ExpressionCalls
    //*************************************************************************
    private static ExpressionCall mapGlobalCallTOToExpressionCall(GlobalExpressionCallTO globalCallTO, 
            Map<Integer, Gene> geneMap, Map<Integer, Condition> condMap,
            ExpressionCallFilter callFilter, BigDecimal maxRank, Set<CallService.Attribute> attrs) {
        log.entry(globalCallTO, geneMap, condMap, attrs);
        
        Set<ExpressionCallData> callData = mapGlobalCallTOToExpressionCallData(globalCallTO,
                attrs, callFilter.getDataTypeFilters());

        return log.exit(new ExpressionCall(
            attrs == null || attrs.isEmpty() || attrs.contains(Attribute.GENE)?
                    geneMap.get(globalCallTO.getBgeeGeneId()): null,
            attrs == null || attrs.isEmpty() || attrs.stream().anyMatch(a -> a.isConditionParameter())?
                    condMap.get(globalCallTO.getConditionId()): null,
            attrs == null || attrs.isEmpty() || attrs.contains(Attribute.OBSERVED_DATA)?
                    inferDataPropagation(callData): null,
            attrs == null || attrs.isEmpty() || attrs.contains(Attribute.CALL_TYPE)?
                    inferSummaryCallType(callData): null,
            attrs == null || attrs.isEmpty() || attrs.contains(Attribute.DATA_QUALITY)?
                    inferSummaryQuality(callData): null,
            attrs == null || attrs.isEmpty() || attrs.contains(Attribute.EXPERIMENT_COUNTS) ||
                    attrs.contains(Attribute.DATA_TYPE_RANK_INFO)?
                            callData: null,
            attrs == null || attrs.isEmpty() || attrs.contains(Attribute.GLOBAL_MEAN_RANK)?
                    globalCallTO.getGlobalMeanRank(): null,
            attrs == null || attrs.isEmpty() || attrs.contains(Attribute.GLOBAL_MEAN_RANK)?
                    maxRank: null));
    }
    
    private static Set<ExpressionCallData> mapGlobalCallTOToExpressionCallData(
            GlobalExpressionCallTO globalCallTO, Set<Attribute> attrs, Set<DataType> requestedDataTypes) {
        log.entry(globalCallTO, attrs, requestedDataTypes);
        
        if (globalCallTO.getCallDataTOs() == null || globalCallTO.getCallDataTOs().isEmpty()) {
            return log.exit(null);
        }

        return log.exit(globalCallTO.getCallDataTOs().stream().map(cdTO -> {
            DataType dt = mapDAODataTypeToDataType(Collections.singleton(cdTO.getDataType()),
                    requestedDataTypes).iterator().next();

            boolean getExperimentsCounts = attrs == null || attrs.isEmpty() ||
                    attrs.contains(Attribute.EXPERIMENT_COUNTS) ||
                    //we need the experiment counts to infer the call type and quality,
                    //even if we then don't use the CallData in the GlobalCall
                    attrs.contains(Attribute.CALL_TYPE) ||
                    attrs.contains(Attribute.DATA_QUALITY);
            boolean getRankInfo = attrs == null || attrs.isEmpty() ||
                    attrs.contains(Attribute.DATA_TYPE_RANK_INFO);
            boolean getDataProp = attrs == null || attrs.isEmpty() ||
                    attrs.contains(Attribute.OBSERVED_DATA);
            assert !getExperimentsCounts ||
                    cdTO.getExperimentCounts() != null && !cdTO.getExperimentCounts().isEmpty() &&
                            cdTO.getPropagatedCount() != null;
            assert !getRankInfo || cdTO.getRank() != null && cdTO.getRankNorm() != null &&
                    cdTO.getWeightForMeanRank() != null;
            assert !getDataProp || cdTO.getDataPropagation() != null &&
                    !cdTO.getDataPropagation().isEmpty() && cdTO.isConditionObservedData() != null;

            Set<ExperimentExpressionCount> counts = null;
            if (getExperimentsCounts) {
                counts = cdTO.getExperimentCounts().stream()
                        //we only provide ALL and SELF counts using the API for now
                        .filter(c -> {
                            PropagationState propState = mapDAOPropStateToPropState(
                                    c.getPropagationState());
                            return PropagationState.ALL.equals(propState) ||
                                    PropagationState.SELF.equals(propState);
                         })
                        .filter(c -> {
                            CallType callType = mapDAOCallTypeToCallType(c.getCallType());
                            return callType.isValidDataType(dt) &&
                                    callType.isValidPropagationState(
                                            mapDAOPropStateToPropState(c.getPropagationState()));
                        })
                        .map(c -> mapDAOExperimentCountToExperimentExpressionCount(c))
                        .collect(Collectors.toSet());
            }

            return log.exit(new ExpressionCallData(dt, counts,
                    getExperimentsCounts && cdTO.getPropagatedCount() != null?
                            cdTO.getPropagatedCount(): 0,
                    getRankInfo? cdTO.getRank(): null,
                    getRankInfo? cdTO.getRankNorm(): null,
                    getRankInfo? cdTO.getWeightForMeanRank(): null,
                    getDataProp? mapDAOCallDataTOToDataPropagation(cdTO): null));
        }).collect(Collectors.toSet()));
    }

    private static ExperimentExpressionCount mapDAOExperimentCountToExperimentExpressionCount(
            DAOExperimentCount count) {
        log.entry(count);
        return log.exit(new ExperimentExpressionCount(
                mapDAOCallTypeToCallType(count.getCallType()),
                mapDAODataQualityToDataQuality(count.getDataQuality()),
                mapDAOPropStateToPropState(count.getPropagationState()),
                count.getCount()));
    }
    private static CallType.Expression mapDAOCallTypeToCallType(DAOExperimentCount.CallType daoCallType) {
        log.entry(daoCallType);
        if (daoCallType == null) {
            throw log.throwing(new IllegalArgumentException("DAOCallType cannot be null"));
        }
        switch(daoCallType) {
        case PRESENT:
            return log.exit(CallType.Expression.EXPRESSED);
        case ABSENT:
            return log.exit(CallType.Expression.NOT_EXPRESSED);
        default:
            throw log.throwing(new IllegalStateException("DAOCallType not supported: " + daoCallType));
        }
    }
    private static DataQuality mapDAODataQualityToDataQuality(DAOExperimentCount.DataQuality qual) {
        log.entry(qual);
        if (qual == null) {
            throw log.throwing(new IllegalArgumentException("DAODataQuality cannot be null"));
        }
        switch(qual) {
        case LOW:
            return log.exit(DataQuality.LOW);
        case HIGH:
            return log.exit(DataQuality.HIGH);
        default:
            throw log.throwing(new IllegalStateException("DAODataQuality not supported: " + qual));
        }
    }
    private static Set<DataType> mapDAODataTypeToDataType(Set<DAODataType> dts,
            Set<DataType> requestedDataTypes) throws IllegalStateException{
        log.entry(dts, requestedDataTypes);

        Set<DataType> mappedDataTypes = null;
        if (dts == null || dts.isEmpty()) {
            mappedDataTypes = EnumSet.allOf(DataType.class);
        } else {
            mappedDataTypes = dts.stream()
                    .map(dt -> {
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
                            throw log.throwing(new IllegalStateException("Unsupported DataType: " + dt));
                        }
                    }).collect(Collectors.toSet());
        }

        if (requestedDataTypes != null && !requestedDataTypes.isEmpty() &&
                !requestedDataTypes.containsAll(mappedDataTypes)) {
            mappedDataTypes.removeAll(requestedDataTypes);
            throw log.throwing(new IllegalArgumentException(
                    "Some DataTypes were retrieved from data source but were not requested: "
                    + mappedDataTypes));
        }

        return log.exit(mappedDataTypes);
    }

    private static PropagationState mapDAOPropStateToPropState(DAOPropagationState propState) {
        log.entry(propState);
        if (propState == null) {
            return log.exit(null);
        }
        switch(propState) {
        case ALL:
            return log.exit(PropagationState.ALL);
        case SELF:
            return log.exit(PropagationState.SELF);
        case ANCESTOR:
            return log.exit(PropagationState.ANCESTOR);
        case DESCENDANT:
            return log.exit(PropagationState.DESCENDANT);
        case SELF_AND_ANCESTOR:
            return log.exit(PropagationState.SELF_AND_ANCESTOR);
        case SELF_AND_DESCENDANT:
            return log.exit(PropagationState.SELF_AND_DESCENDANT);
        case ANCESTOR_AND_DESCENDANT:
            return log.exit(PropagationState.ANCESTOR_AND_DESCENDANT);
        default:
            throw log.throwing(new IllegalStateException("Unsupported DAOPropagationState: "
                    + propState));
        }
    }
//    private static Set<ExpressionCallDAO.Attribute> convertServiceAttrsToExprDAOAttrs(
//            Set<Attribute> attributes, Set<DataType> dataTypesRequested) {
//        log.entry(attributes, dataTypesRequested);
//        
//        //revert the existing map ExpressionCallDAO.Attribute -> DataType
//        Map<DataType, ExpressionCallDAO.Attribute> typeToDAOAttr = EXPR_ATTR_TO_DATA_TYPE.entrySet().stream()
//                .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
//        
//        return log.exit(attributes.stream().flatMap(attr -> {
//            switch (attr) {
//            case GENE_ID: 
//                return Stream.of(ExpressionCallDAO.Attribute.GENE_ID);
//            case ANAT_ENTITY_ID: 
//                return Stream.of(ExpressionCallDAO.Attribute.CONDITION_ID);
//            case DEV_STAGE_ID: 
//                return Stream.of(ExpressionCallDAO.Attribute.CONDITION_ID);
//            //Whether we need to get a global quality level over all requested data types, 
//            //or the detailed quality level per data type, it's the same DAO attributes that we need. 
//            case GLOBAL_DATA_QUALITY:
//            case CALL_DATA: 
//                return dataTypesRequested.stream().map(type -> Optional.ofNullable(typeToDAOAttr.get(type))
//                        //bug of javac for type inference, we need to type the exception 
//                        //explicitly to RuntimeException,
//                        //see http://stackoverflow.com/questions/25523375/java8-lambdas-and-exceptions
//                        .<RuntimeException>orElseThrow(() -> log.throwing(new IllegalStateException(
//                                "Unsupported DataType: " + type))));
//            case GLOBAL_ANAT_PROPAGATION: 
//                return Stream.of(ExpressionCallDAO.Attribute.ANAT_ORIGIN_OF_LINE);
//            case GLOBAL_STAGE_PROPAGATION: 
//                return Stream.of(ExpressionCallDAO.Attribute.STAGE_ORIGIN_OF_LINE);
//            case GLOBAL_OBSERVED_DATA: 
//                return Stream.of(ExpressionCallDAO.Attribute.OBSERVED_DATA);
//            case CALL_DATA_OBSERVED_DATA: 
//                //nothing here, the only way to get this information is by performing 2 queries, 
//                //one including substructures/sub-stages, another one without substructures/sub-stages.
//                return Stream.empty();
//            case GLOBAL_RANK: 
//                return Stream.of(ExpressionCallDAO.Attribute.GLOBAL_MEAN_RANK);
//            default: 
//                throw log.throwing(new IllegalStateException("Unsupported Attributes from CallService: "
//                        + attr));
//            }
//        }).collect(Collectors.toCollection(() -> EnumSet.noneOf(ExpressionCallDAO.Attribute.class))));
//    }

    //*************************************************************************
    // HELPER METHODS FOR INFERENCES
    //*************************************************************************

    private static <T extends CallData<?>> DataPropagation inferDataPropagation(
            Set<ExpressionCallData> callData) {
        log.entry(callData);

        if (callData == null || callData.isEmpty() || callData.stream()
                .anyMatch(cd -> cd.getDataPropagation() == null ||
                        cd.getDataPropagation().isIncludingObservedData() == null)) {
            throw log.throwing(new IllegalArgumentException("Missing info for inferring data propagation"));
        }
        return log.exit(callData.stream()
                .map(cd -> cd.getDataPropagation())
                .reduce(
                        DATA_PROPAGATION_IDENTITY,
                        //note to self: if we were not mapping to DataPropagation,
                        //we would need to provide the following accumulator BiFunction
//                        (dataProp, cd) -> mergeDataPropagations(dataProp, cd.getDataPropagation()),
                        (dataProp1, dataProp2) -> mergeDataPropagations(dataProp1, dataProp2)
                ));
    }
    private static DataPropagation mapDAOCallDataTOToDataPropagation(
            GlobalExpressionCallDataTO callDataTO) {
        log.entry(callDataTO);

        if (callDataTO == null || callDataTO.getDataPropagation() == null ||
                callDataTO.getDataPropagation().isEmpty()) {
            return log.exit(null);
        }

        PropagationState anatEntityPropState = null;
        PropagationState stagePropState = null;
        for (Entry<ConditionDAO.Attribute, DAOPropagationState> observedDataEntry:
            callDataTO.getDataPropagation().entrySet()) {
            switch(observedDataEntry.getKey()) {
            case ANAT_ENTITY_ID:
                anatEntityPropState = mapDAOPropStateToPropState(
                        observedDataEntry.getValue());
                break;
            case STAGE_ID:
                stagePropState = mapDAOPropStateToPropState(
                        observedDataEntry.getValue());
                break;
            default:
                throw log.throwing(new IllegalStateException(
                        "ConditionDAO.Attribute not supported for DataPropagation: "
                        + observedDataEntry.getKey()));
            }
        }
        assert anatEntityPropState != null || stagePropState != null;

        Boolean observedData = callDataTO.isConditionObservedData();

        return log.exit(new DataPropagation(anatEntityPropState, stagePropState, observedData));
    }
    protected static DataPropagation mergeDataPropagations(DataPropagation dataProp1,
            DataPropagation dataProp2) {
        log.entry(dataProp1, dataProp2);

        if (dataProp1 == null || dataProp1.equals(DATA_PROPAGATION_IDENTITY)) {
            return log.exit(dataProp2);
        }
        if (dataProp2 == null || dataProp2.equals(DATA_PROPAGATION_IDENTITY)) {
            return log.exit(dataProp1);
        }

        PropagationState anatEntityPropState = mergePropagationStates(
                dataProp1.getAnatEntityPropagationState(), dataProp2.getAnatEntityPropagationState());
        PropagationState stagePropState = mergePropagationStates(
                dataProp1.getDevStagePropagationState(), dataProp2.getDevStagePropagationState());

        //Here we cannot infer the ObservedData state from the condition parameter propagation states,
        //as in the method inferDataPropagation: maybe a data type observed some data in an anat. entity
        //but not in a stage (so ObservedData = false), another data type observed some data
        //in a stage but not in an anat. entity (so ObservedData = false).
        //We cannot infer that ObservedData = true simply because merging these DataPropagations
        //will result in having observed data in both the anat. entity and the stage.
        Boolean retrievedObservedData = null;
        if (Boolean.TRUE.equals(dataProp1.isIncludingObservedData()) ||
                Boolean.TRUE.equals(dataProp2.isIncludingObservedData())) {
            retrievedObservedData = true;
        } else if (Boolean.FALSE.equals(dataProp1.isIncludingObservedData()) &&
                Boolean.FALSE.equals(dataProp2.isIncludingObservedData())) {
            retrievedObservedData = false;
        } else {
            //if one of the DataPropagation isIncludingObservedData is null, and none is True,
            //then we cannot know for sure whether there are observed data,
            //so retrievedObservedData will stay null. In practice this should never happen
            throw log.throwing(new IllegalArgumentException("Inconconsistent DataPropagations: "
                    + dataProp1 + " - " + dataProp2));
        }

        return log.exit(new DataPropagation(anatEntityPropState, stagePropState, retrievedObservedData));
    }
    private static PropagationState mergePropagationStates(PropagationState state1,
            PropagationState state2) {
        log.entry(state1, state2);

        if (state1 != null && !ALLOWED_PROP_STATES.contains(state1)) {
            throw log.throwing(new IllegalArgumentException("Unsupported PropagationState: "
                    + state1));
        }
        if (state2 != null && !ALLOWED_PROP_STATES.contains(state2)) {
            throw log.throwing(new IllegalArgumentException("Unsupported PropagationState: "
                    + state2));
        }

        if (state1 == null && state2 == null) {
            return log.exit(null);
        }
        if (state1 == null && state2 != null) {
            return log.exit(state2);
        }
        if (state1 != null && state2 == null) {
            return log.exit(state1);
        }
        assert state1 != null && state2 != null;
        if (state1.equals(state2)) {
            return log.exit(state1);
        }
        Set<PropagationState> propStates = EnumSet.of(state1, state2);
        if (propStates.contains(PropagationState.ALL)) {
            return log.exit(PropagationState.ALL);
        }

        if (propStates.contains(PropagationState.SELF)) {
            if (propStates.contains(PropagationState.ANCESTOR)) {
                return log.exit(PropagationState.SELF_AND_ANCESTOR);
            } else if (propStates.contains(PropagationState.DESCENDANT)) {
                return log.exit(PropagationState.SELF_AND_DESCENDANT);
            } else if (propStates.contains(PropagationState.SELF_AND_ANCESTOR)) {
                return log.exit(PropagationState.SELF_AND_ANCESTOR);
            } else if (propStates.contains(PropagationState.SELF_AND_DESCENDANT)) {
                return log.exit(PropagationState.SELF_AND_DESCENDANT);
            } else if (propStates.contains(PropagationState.ANCESTOR_AND_DESCENDANT)) {
                return log.exit(PropagationState.ALL);
            } else {
                throw log.throwing(new AssertionError("Case not covered, " + propStates));
            }
        } else if (propStates.contains(PropagationState.ANCESTOR)) {
            if (propStates.contains(PropagationState.SELF)) {
                return log.exit(PropagationState.SELF_AND_ANCESTOR);
            } else if (propStates.contains(PropagationState.DESCENDANT)) {
                return log.exit(PropagationState.ANCESTOR_AND_DESCENDANT);
            } else if (propStates.contains(PropagationState.SELF_AND_ANCESTOR)) {
                return log.exit(PropagationState.SELF_AND_ANCESTOR);
            } else if (propStates.contains(PropagationState.SELF_AND_DESCENDANT)) {
                return log.exit(PropagationState.ALL);
            } else if (propStates.contains(PropagationState.ANCESTOR_AND_DESCENDANT)) {
                return log.exit(PropagationState.ANCESTOR_AND_DESCENDANT);
            } else {
                throw log.throwing(new AssertionError("Case not covered, " + propStates));
            }
        } else if (propStates.contains(PropagationState.DESCENDANT)) {
            if (propStates.contains(PropagationState.SELF)) {
                return log.exit(PropagationState.SELF_AND_DESCENDANT);
            } else if (propStates.contains(PropagationState.ANCESTOR)) {
                return log.exit(PropagationState.ANCESTOR_AND_DESCENDANT);
            } else if (propStates.contains(PropagationState.SELF_AND_ANCESTOR)) {
                return log.exit(PropagationState.ALL);
            } else if (propStates.contains(PropagationState.SELF_AND_DESCENDANT)) {
                return log.exit(PropagationState.SELF_AND_DESCENDANT);
            } else if (propStates.contains(PropagationState.ANCESTOR_AND_DESCENDANT)) {
                return log.exit(PropagationState.ANCESTOR_AND_DESCENDANT);
            } else {
                throw log.throwing(new AssertionError("Case not covered, " + propStates));
            }
        } else if (propStates.contains(PropagationState.SELF_AND_ANCESTOR)) {
            if (propStates.contains(PropagationState.SELF)) {
                return log.exit(PropagationState.SELF_AND_ANCESTOR);
            } else if (propStates.contains(PropagationState.ANCESTOR)) {
                return log.exit(PropagationState.SELF_AND_ANCESTOR);
            } else if (propStates.contains(PropagationState.DESCENDANT)) {
                return log.exit(PropagationState.ALL);
            } else if (propStates.contains(PropagationState.SELF_AND_DESCENDANT)) {
                return log.exit(PropagationState.ALL);
            } else if (propStates.contains(PropagationState.ANCESTOR_AND_DESCENDANT)) {
                return log.exit(PropagationState.ALL);
            } else {
                throw log.throwing(new AssertionError("Case not covered, " + propStates));
            }
        } else if (propStates.contains(PropagationState.SELF_AND_DESCENDANT)) {
            if (propStates.contains(PropagationState.SELF)) {
                return log.exit(PropagationState.SELF_AND_DESCENDANT);
            } else if (propStates.contains(PropagationState.ANCESTOR)) {
                return log.exit(PropagationState.ALL);
            } else if (propStates.contains(PropagationState.DESCENDANT)) {
                return log.exit(PropagationState.SELF_AND_DESCENDANT);
            } else if (propStates.contains(PropagationState.SELF_AND_ANCESTOR)) {
                return log.exit(PropagationState.ALL);
            } else if (propStates.contains(PropagationState.ANCESTOR_AND_DESCENDANT)) {
                return log.exit(PropagationState.ALL);
            } else {
                throw log.throwing(new AssertionError("Case not covered, " + propStates));
            }
        } else if (propStates.contains(PropagationState.ANCESTOR_AND_DESCENDANT)) {
            if (propStates.contains(PropagationState.SELF)) {
                return log.exit(PropagationState.ALL);
            } else if (propStates.contains(PropagationState.ANCESTOR)) {
                return log.exit(PropagationState.ANCESTOR_AND_DESCENDANT);
            } else if (propStates.contains(PropagationState.DESCENDANT)) {
                return log.exit(PropagationState.ANCESTOR_AND_DESCENDANT);
            } else if (propStates.contains(PropagationState.SELF_AND_ANCESTOR)) {
                return log.exit(PropagationState.ALL);
            } else if (propStates.contains(PropagationState.SELF_AND_DESCENDANT)) {
                return log.exit(PropagationState.ALL);
            } else {
                throw log.throwing(new AssertionError("Case not covered, " + propStates));
            }
        } else {
            throw log.throwing(new AssertionError("Case not covered, " + propStates));
        }
    }
    /**
     * Infer call type summary from {@code callData}.
     * 
     * @param callData  A {@code Set} of {@code ExpressionCallData}s that are {@code CallData} to be used.
     * @return          The {@code ExpressionSummary} that is the inferred call type quality.
     */
    private static ExpressionSummary inferSummaryCallType(Set<ExpressionCallData> callData) {
        log.entry(callData);
        
        if (callData.stream().anyMatch(cd -> Expression.EXPRESSED.equals(cd.getCallType()))) {
            return log.exit(ExpressionSummary.EXPRESSED);
        }
        return log.exit(ExpressionSummary.NOT_EXPRESSED);
    }

    /**
     * Infer summary quality from {@code callData}.
     * <p>
     * Inside each experiment, only count present; best score gives score of experiment
     * (i.e. 1 present high is enough to call experiment present high).
     *  Per gene condition, count number of experiments with each score:
     *  <ul>
     *  <li>2 high => {@code SummaryQuality.GOLD}</li>
     *  <li>1 high or 2 low => {@code SummaryQuality.SILVER}</li>
     *  <li>1 low => {@code SummaryQuality.BRONZE}</li>
     *  </ul>
     *  
     * @param callData  A {@code Set} of {@code ExpressionCallData}s that are {@code CallData} to be used.
     * @return          The {@code SummaryQuality} that is the inferred summary quality.
     */
    private static SummaryQuality inferSummaryQuality(Set<ExpressionCallData> callData) {
        log.entry(callData);

        int expPresentHigh = 0, expPresentLow = 0, expAbsentHigh = 0, expAbsentLow = 0;

        for (ExpressionCallData cd: callData) {
            expPresentHigh += getCountSum(cd, CallType.Expression.EXPRESSED, DataQuality.HIGH,
                PropagationState.ALL);
            expPresentLow  += getCountSum(cd, CallType.Expression.EXPRESSED, DataQuality.LOW,
                PropagationState.ALL);
            expAbsentHigh  += getCountSum(cd, CallType.Expression.NOT_EXPRESSED, DataQuality.HIGH,
                PropagationState.ALL);
            expAbsentLow   += getCountSum(cd, CallType.Expression.NOT_EXPRESSED, DataQuality.LOW,
                PropagationState.ALL);
        }
        
        if (expPresentHigh >= MIN_HIGH_GOLD) {
            return log.exit(SummaryQuality.GOLD);
        }
        if (expPresentHigh >= MIN_HIGH_SILVER || expPresentLow >= MIN_LOW_SILVER) {
            return log.exit(SummaryQuality.SILVER);
        }
        if (expPresentHigh >= MIN_HIGH_BRONZE || expPresentLow >= MIN_LOW_BRONZE) {
            return log.exit(SummaryQuality.BRONZE);
        }
        
        if (expAbsentHigh >= MIN_HIGH_GOLD) {
            return log.exit(SummaryQuality.GOLD);
        }
        if (expAbsentHigh >= MIN_HIGH_SILVER || expAbsentLow >= MIN_LOW_SILVER) {
            return log.exit(SummaryQuality.SILVER);
        }
        if (expAbsentHigh >= MIN_HIGH_BRONZE || expAbsentLow >= MIN_LOW_BRONZE) {
            return log.exit(SummaryQuality.BRONZE);
        }
        
        throw log.throwing(new IllegalArgumentException("Malformed CallData"));
    }

    /**
     * Calculate the sum of counts of an {@code ExpressionCallData} filtered by an {@code Expression},
     * a {@code DataQuality} and a {@code PropagationState}.
     * 
     * @param cd        An {@code ExpressionCallData} that is the call data
     *                  for which filtered counts should be sum.
     * @param expr      An {@code Expression} that is the call type allowing to filter counts.
     * @param qual      A {@code DataQuality} that is the quality allowing to filter counts.
     * @param state     A {@code PropagationState} that is the propagation state allowing to filter counts.
     * @return          The {@code int} that is the sum of filtered counts. 
     */
    private static int getCountSum(ExpressionCallData cd, Expression expr, DataQuality qual,
            PropagationState state) {
        log.entry(cd, expr, qual, state);
        if (cd.getExperimentCounts() == null) {
            return log.exit(0);
        }
        return log.exit(cd.getExperimentCounts().stream()
            .filter(c -> expr.equals(c.getCallType()) && qual.equals(c.getDataQuality())
                        && state.equals(c.getPropagationState()))
            .map(c -> c.getCount())
            .mapToInt(Integer::intValue)
            .sum());
    }
}
