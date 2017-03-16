package org.bgee.model.expressiondata;

import java.math.BigDecimal;
import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
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
import org.bgee.model.dao.api.expressiondata.DAOExperimentCountFilter;
import org.bgee.model.dao.api.expressiondata.GlobalExpressionCallDAO;
import org.bgee.model.dao.api.expressiondata.GlobalExpressionCallDAO.GlobalExpressionCallTO;
import org.bgee.model.dao.api.gene.GeneDAO;
import org.bgee.model.expressiondata.Call.DiffExpressionCall;
import org.bgee.model.expressiondata.Call.ExpressionCall;
import org.bgee.model.expressiondata.CallData.ExpressionCallData;
import org.bgee.model.expressiondata.CallFilter.DiffExpressionCallFilter;
import org.bgee.model.expressiondata.CallFilter.ExpressionCallFilter;
import org.bgee.model.expressiondata.baseelements.CallType;
import org.bgee.model.expressiondata.baseelements.CallType.Expression;
import org.bgee.model.expressiondata.baseelements.DataPropagation.PropagationState;
import org.bgee.model.expressiondata.baseelements.DataQuality;
import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.model.expressiondata.baseelements.ExperimentExpressionCount;
import org.bgee.model.expressiondata.baseelements.SummaryCallType;
import org.bgee.model.expressiondata.baseelements.SummaryCallType.ExpressionSummary;
import org.bgee.model.expressiondata.baseelements.SummaryQuality;
import org.bgee.model.gene.Gene;
import org.bgee.model.species.Species;
import org.bgee.model.species.TaxonomyFilter;

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
// Notes from previous CallFilter version
//******************
//XXX: 
// - if both IDs and multiple species requested AND forceHomology is true 
//   => find missing orthologous genes/homologous organs/comparable stages
//XXX: impact both the gene filtering and the anat.entity and stage filtering.
//Species should be always explicitly targeted.
//    private final TaxonomyFilter taxonFilter; 
//XXX: with this boolean set to true, any multi-species query will search explicitly 
//for homology/orthology relations, and will complete ID list provided to potentially 
//add homolog/orthologs (i.e., impacting both ConditionFilters and GeneFilters).
//if false, then any query is possible, without caring about homology/orthology.
//XXX: If true, retrieve results only in homologous structure/comparable stages, always.
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
     * <li>{@code OBSERVED_DATA}: corresponds to {@link Call#getIsObservedData()}.
     * <li>{@code RANK}: corresponds to {@link ExpressionCall#getGlobalMeanRank()}.
     * <li>{@code CALL_DATA}: corresponds to {@link Call#getCallData()}.
     * </ul>
     */
    public static enum Attribute implements Service.Attribute {
        GENE(false), ANAT_ENTITY_ID(true), DEV_STAGE_ID(true), CALL_TYPE(false),
        DATA_QUALITY(false), OBSERVED_DATA(false), RANK(false), CALL_DATA(false);
        
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

    private static final Map<DataType, Set<GlobalExpressionCallDAO.Attribute>> DATA_TYPE_TO_GLOBAL_EXPR_ATTR = 
    Collections.unmodifiableMap(Stream.of(
        new SimpleEntry<>(DataType.AFFYMETRIX, new HashSet<>(Arrays.asList(
            GlobalExpressionCallDAO.Attribute.AFFYMETRIX_EXP_PRESENT_HIGH_SELF_COUNT,
            GlobalExpressionCallDAO.Attribute.AFFYMETRIX_EXP_PRESENT_LOW_SELF_COUNT, 
            GlobalExpressionCallDAO.Attribute.AFFYMETRIX_EXP_ABSENT_HIGH_SELF_COUNT,
            GlobalExpressionCallDAO.Attribute.AFFYMETRIX_EXP_ABSENT_LOW_SELF_COUNT, 
            GlobalExpressionCallDAO.Attribute.AFFYMETRIX_EXP_PRESENT_HIGH_DESCENDANT_COUNT,
            GlobalExpressionCallDAO.Attribute.AFFYMETRIX_EXP_PRESENT_LOW_DESCENDANT_COUNT, 
            GlobalExpressionCallDAO.Attribute.AFFYMETRIX_EXP_ABSENT_HIGH_PARENT_COUNT,
            GlobalExpressionCallDAO.Attribute.AFFYMETRIX_EXP_ABSENT_LOW_PARENT_COUNT, 
            GlobalExpressionCallDAO.Attribute.AFFYMETRIX_EXP_PRESENT_HIGH_TOTAL_COUNT,
            GlobalExpressionCallDAO.Attribute.AFFYMETRIX_EXP_PRESENT_LOW_TOTAL_COUNT, 
            GlobalExpressionCallDAO.Attribute.AFFYMETRIX_EXP_ABSENT_HIGH_TOTAL_COUNT,
            GlobalExpressionCallDAO.Attribute.AFFYMETRIX_EXP_ABSENT_LOW_TOTAL_COUNT,
            GlobalExpressionCallDAO.Attribute.AFFYMETRIX_EXP_PROPAGATED_COUNT,
            GlobalExpressionCallDAO.Attribute.AFFYMETRIX_MEAN_RANK,
            GlobalExpressionCallDAO.Attribute.AFFYMETRIX_MEAN_RANK_NORM,
            GlobalExpressionCallDAO.Attribute.AFFYMETRIX_DISTINCT_RANK_SUM))), 
        new SimpleEntry<>(DataType.EST, new HashSet<>(Arrays.asList(
            GlobalExpressionCallDAO.Attribute.EST_LIB_PRESENT_HIGH_SELF_COUNT,
            GlobalExpressionCallDAO.Attribute.EST_LIB_PRESENT_LOW_SELF_COUNT, 
            GlobalExpressionCallDAO.Attribute.EST_LIB_PRESENT_HIGH_DESCENDANT_COUNT,
            GlobalExpressionCallDAO.Attribute.EST_LIB_PRESENT_LOW_DESCENDANT_COUNT, 
            GlobalExpressionCallDAO.Attribute.EST_LIB_PRESENT_HIGH_TOTAL_COUNT,
            GlobalExpressionCallDAO.Attribute.EST_LIB_PRESENT_LOW_TOTAL_COUNT,
            GlobalExpressionCallDAO.Attribute.EST_LIB_PROPAGATED_COUNT,
            GlobalExpressionCallDAO.Attribute.EST_RANK,
            GlobalExpressionCallDAO.Attribute.EST_RANK_NORM))), 
        new SimpleEntry<>(DataType.IN_SITU, new HashSet<>(Arrays.asList(
            GlobalExpressionCallDAO.Attribute.IN_SITU_EXP_PRESENT_HIGH_SELF_COUNT,
            GlobalExpressionCallDAO.Attribute.IN_SITU_EXP_PRESENT_LOW_SELF_COUNT, 
            GlobalExpressionCallDAO.Attribute.IN_SITU_EXP_ABSENT_HIGH_SELF_COUNT,
            GlobalExpressionCallDAO.Attribute.IN_SITU_EXP_ABSENT_LOW_SELF_COUNT, 
            GlobalExpressionCallDAO.Attribute.IN_SITU_EXP_PRESENT_HIGH_DESCENDANT_COUNT,
            GlobalExpressionCallDAO.Attribute.IN_SITU_EXP_PRESENT_LOW_DESCENDANT_COUNT, 
            GlobalExpressionCallDAO.Attribute.IN_SITU_EXP_ABSENT_HIGH_PARENT_COUNT,
            GlobalExpressionCallDAO.Attribute.IN_SITU_EXP_ABSENT_LOW_PARENT_COUNT, 
            GlobalExpressionCallDAO.Attribute.IN_SITU_EXP_PRESENT_HIGH_TOTAL_COUNT,
            GlobalExpressionCallDAO.Attribute.IN_SITU_EXP_PRESENT_LOW_TOTAL_COUNT, 
            GlobalExpressionCallDAO.Attribute.IN_SITU_EXP_ABSENT_HIGH_TOTAL_COUNT,
            GlobalExpressionCallDAO.Attribute.IN_SITU_EXP_ABSENT_LOW_TOTAL_COUNT,
            GlobalExpressionCallDAO.Attribute.IN_SITU_EXP_PROPAGATED_COUNT,
            GlobalExpressionCallDAO.Attribute.IN_SITU_RANK,
            GlobalExpressionCallDAO.Attribute.IN_SITU_RANK_NORM))), 
        new SimpleEntry<>(DataType.RNA_SEQ, new HashSet<>(Arrays.asList(
            GlobalExpressionCallDAO.Attribute.RNA_SEQ_EXP_PRESENT_HIGH_SELF_COUNT,
            GlobalExpressionCallDAO.Attribute.RNA_SEQ_EXP_PRESENT_LOW_SELF_COUNT, 
            GlobalExpressionCallDAO.Attribute.RNA_SEQ_EXP_ABSENT_HIGH_SELF_COUNT,
            GlobalExpressionCallDAO.Attribute.RNA_SEQ_EXP_ABSENT_LOW_SELF_COUNT, 
            GlobalExpressionCallDAO.Attribute.RNA_SEQ_EXP_PRESENT_HIGH_DESCENDANT_COUNT,
            GlobalExpressionCallDAO.Attribute.RNA_SEQ_EXP_PRESENT_LOW_DESCENDANT_COUNT, 
            GlobalExpressionCallDAO.Attribute.RNA_SEQ_EXP_ABSENT_HIGH_PARENT_COUNT,
            GlobalExpressionCallDAO.Attribute.RNA_SEQ_EXP_ABSENT_LOW_PARENT_COUNT, 
            GlobalExpressionCallDAO.Attribute.RNA_SEQ_EXP_PRESENT_HIGH_TOTAL_COUNT,
            GlobalExpressionCallDAO.Attribute.RNA_SEQ_EXP_PRESENT_LOW_TOTAL_COUNT, 
            GlobalExpressionCallDAO.Attribute.RNA_SEQ_EXP_ABSENT_HIGH_TOTAL_COUNT,
            GlobalExpressionCallDAO.Attribute.RNA_SEQ_EXP_ABSENT_LOW_TOTAL_COUNT,
            GlobalExpressionCallDAO.Attribute.RNA_SEQ_EXP_PROPAGATED_COUNT,
            GlobalExpressionCallDAO.Attribute.RNA_SEQ_MEAN_RANK,
            GlobalExpressionCallDAO.Attribute.RNA_SEQ_MEAN_RANK_NORM,
            GlobalExpressionCallDAO.Attribute.RNA_SEQ_DISTINCT_RANK_SUM))))
        .collect(Collectors.toMap(SimpleEntry::getKey, SimpleEntry::getValue)));

    //*************************************************
    // INSTANCE ATTRIBUTES AND CONSTRUCTOR
    //*************************************************
    private final ConditionDAO conditionDAO;
    private final GeneDAO geneDAO;
    private final GlobalExpressionCallDAO globalExprCallDAO;
    private final AnatEntityService anatEntityService;
    private final DevStageService devStageService;
    /**
     * @param serviceFactory            The {@code ServiceFactory} to be used to obtain {@code Service}s 
     *                                  and {@code DAOManager}.
     * @throws IllegalArgumentException If {@code serviceFactory} is {@code null}.
     */
    public CallService(ServiceFactory serviceFactory) {
        super(serviceFactory);
        this.conditionDAO = this.getDaoManager().getConditionDAO();
        this.geneDAO = this.getDaoManager().getGeneDAO();
        this.globalExprCallDAO = this.getDaoManager().getGlobalExpressionCallDAO();
        this.anatEntityService = this.getServiceFactory().getAnatEntityService();
        this.devStageService = this.getServiceFactory().getDevStageService();
    }
    
    //XXX: example multi-species query, signature/returned value to be better defined. 
    //We could then have the calls ordered by OMA HOG IDs (as in the current pipeline), 
    //to be able to group calls of orthologous genes in homologous organs. I believe 
    //this CallService should manage the ordering if requested, but not the grouping. 
    //XXX: or should we have Calls with MultiSpeciesConditions? Then the grouping 
    //of homologous organs could be performed by the CallService, if provided with the mapping 
    //of homologous organs as method argument. 
    public Stream<Call<? extends SummaryCallType, ? extends CallData<?>>> loadCallsInMultiSpecies(
            TaxonomyFilter taxonFilter, Set<CallFilter<?>> callFilters) {
        log.entry(taxonFilter, callFilters);
        throw log.throwing(new UnsupportedOperationException("Load of calls in multi species not implemented yet"));
    }

        
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
     */
    public Stream<ExpressionCall> loadExpressionCalls(ExpressionCallFilter callFilter,
            Collection<Attribute> attributes, 
            LinkedHashMap<OrderingAttribute, Service.Direction> orderingAttributes)
                    throws IllegalArgumentException {        
        log.entry(callFilter, attributes, orderingAttributes);

        final Set<Attribute> clonedAttrs = Collections.unmodifiableSet(
            attributes == null? EnumSet.noneOf(Attribute.class): EnumSet.copyOf(attributes));
        final LinkedHashMap<OrderingAttribute, Service.Direction> clonedOrderingAttrs = 
            orderingAttributes == null? new LinkedHashMap<>(): new LinkedHashMap<>(orderingAttributes);
        
        // Sanity checks
        if (callFilter == null || callFilter.getGeneFilter() == null) {
            throw log.throwing(new IllegalArgumentException("A CallFilter must be provided, "
                    + "at least to specify the targeted Species (through the GeneFilter)"));
        }

        // Retrieve species
        Set<Integer> clnSpId =  Collections.singleton(callFilter.getGeneFilter().getSpeciesId());
        final Set<Species> speciesSet = this.getServiceFactory().getSpeciesService()
            .loadSpeciesByIds(clnSpId, false);
        if (speciesSet.size() != 1) {
            throw new IllegalArgumentException("Provided species not found in data source");
        }
        Species species = speciesSet.stream().findFirst().get();
        
        // Define condition parameter combination allowing to target specific tables in data source
        Set<ConditionDAO.Attribute> condParamCombination = loadConditionParameterCombination(
                callFilter, clonedAttrs, clonedOrderingAttrs.keySet());
        
        // Retrieve conditions by condition IDs if necessary
        final Map<Integer, Condition> condMap = Collections.unmodifiableMap(
                clonedAttrs.isEmpty()? new HashMap<>(): this.loadConditionMap(species.getId(),
                        condParamCombination, convertServiceAttrsToConditionDAOAttrs(clonedAttrs)));
        
        // Retrieve a Map of Bgee gene IDs to Gene
        Map<Integer, Set<String>> speToGeneIdsMap = new HashMap<>();
        speToGeneIdsMap.put(species.getId(), callFilter.getGeneFilter().getEnsemblGeneIds());
        final Map<Integer, Gene> geneMap = Collections.unmodifiableMap(this.geneDAO
            .getGenesBySpeciesAndGeneIds(speToGeneIdsMap)
                .stream()
                .collect(Collectors.toMap(
                    gTO -> gTO.getId(),
                    gTO -> mapGeneTOToGene(gTO, species))));
        //XXX: check that all requested genes do exist?
        assert !geneMap.isEmpty();
        
        // Retrieve calls
        Stream<ExpressionCall> calls = this.performsGlobalExprCallQuery(species.getId(), geneMap,
                callFilter, condParamCombination, clonedAttrs, clonedOrderingAttrs)
            .map(to -> mapGlobalCallTOToExpressionCall(to, species, geneMap, condMap, clonedAttrs))
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
     * @param speciesId
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
    protected Map<Integer, Condition> loadConditionMap(Integer speciesId,
            Set<ConditionDAO.Attribute> condParamCombination,
            Set<ConditionDAO.Attribute> conditionDAOAttrs) {
        log.entry(speciesId, condParamCombination, conditionDAOAttrs);

        Collection<Integer> speIds = Collections.singleton(speciesId);
        Set<String> anatEntityIds = new HashSet<>();
        Set<String> stageIds = new HashSet<>();
        Set<ConditionTO> conditionTOs = new HashSet<>();

        ConditionTOResultSet rs = this.conditionDAO.getConditionsBySpeciesIds(
                speIds, condParamCombination, conditionDAOAttrs);
        while (rs.next()) {
            ConditionTO condTO = rs.getTO();
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
                    speIds, true, anatEntityIds, false)
            .collect(Collectors.toMap(a -> a.getId(), a -> a));
        final Map<String, DevStage> stageMap = stageIds.isEmpty()? new HashMap<>():
            this.devStageService.loadDevStages(
                    speIds, true, stageIds, false)
            .collect(Collectors.toMap(s -> s.getId(), s -> s));

        return log.exit(conditionTOs.stream()
                .collect(Collectors.toMap(cTO -> cTO.getId(), 
                        cTO -> mapConditionTOToCondition(cTO, speciesId,
                                anatMap.get(cTO.getAnatEntityId()), stageMap.get(cTO.getStageId()))
                        ))
                );
    }

    /**
     * Perform query to retrieve expressed calls without the post-processing of 
     * the results returned by {@code DAO}s.
     * 
     * @param speciesId             An {@code Integer} that is the ID of the species 
     *                              for which to return the {@code ExpressionCall}s.
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
    private Stream<GlobalExpressionCallTO> performsGlobalExprCallQuery(Integer speciesId, 
            Map<Integer, Gene> geneMap, ExpressionCallFilter callFilter,
            Set<ConditionDAO.Attribute> condParamCombination, Set<Attribute> attributes,
            LinkedHashMap<OrderingAttribute, Service.Direction> orderingAttributes)
                    throws IllegalArgumentException {
        log.entry(speciesId, geneMap, callFilter, condParamCombination, attributes, orderingAttributes);

        Set<Integer> geneIdFilter = null;
        if (callFilter != null && callFilter.getGeneFilter() != null &&
                !callFilter.getGeneFilter().getEnsemblGeneIds().isEmpty()) {
            //generate a Map Ensembl ID -> Bgee gene ID
            final Map<String, Integer> ensemblToBgeeIds = geneMap.entrySet().stream()
                    .collect(Collectors.toMap(e -> e.getValue().getEnsemblGeneId(), e -> e.getKey()));
            //Now retrieve the Bgee gene IDs of the requested Ensembl gene IDs in GeneFilter.
            //We have the guarantee that an Ensembl ID will map to only one Bgee ID
            //because only one species was requested (see speciesId argument).
            geneIdFilter = callFilter.getGeneFilter().getEnsemblGeneIds().stream()
                    .filter(ensemblId -> ensemblToBgeeIds.containsKey(ensemblId))
                    .map(ensemblId -> ensemblToBgeeIds.get(ensemblId))
                    .collect(Collectors.toSet());
            assert !geneIdFilter.contains(null);
        }
        
        Stream<GlobalExpressionCallTO> calls = this.globalExprCallDAO
            .getGlobalExpressionCalls(Arrays.asList(
                //generate an ExpressionCallDAOFilter from callFilter 
                new CallDAOFilter(
                    // gene IDs
                    geneIdFilter, 
                    //species
                    Arrays.asList(speciesId), 
                    //ConditionFilters
                    callFilter == null || callFilter.getConditionFilters() == null? null:
                        callFilter.getConditionFilters().stream()
                        .map(condFilter -> new DAOConditionFilter(
                            condFilter.getAnatEntityIds(),
                            condFilter.getDevStageIds()))
                        .collect(Collectors.toSet())
                )),
                convertCallFilterToCallDataDAOFilters(callFilter),
                // Condition parameters
                condParamCombination,
                // Attributes
                convertServiceAttrToGlobalExprDAOAttr(attributes,
                        callFilter == null? null: callFilter.getDataTypeFilters()),
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
            CallFilter<?> callFilter, Set<Attribute> serviceAttrs,
            Set<OrderingAttribute> serviceOrderingAttrs) {
        log.entry(callFilter, serviceAttrs, serviceOrderingAttrs);

        final Set<ConditionDAO.Attribute> allDAOCondParamAttrs = EnumSet.allOf(ConditionDAO.Attribute.class)
                .stream().filter(a -> a.isConditionParameter())
                .collect(Collectors.toSet());
    
        Set<ConditionDAO.Attribute> attrs = convertServiceAttrsToConditionDAOAttrs(serviceAttrs);
        Set<ConditionDAO.Attribute> orderingAttrs = 
                convertServiceOrderingAttrsToConditionDAOAttrs(serviceOrderingAttrs);
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

        Set<ConditionDAO.Attribute> daoCondParamComb = new HashSet<>();
        daoCondParamComb.addAll(attrs);
        daoCondParamComb.addAll(orderingAttrs);
        daoCondParamComb.addAll(filterAttrs);

        if (daoCondParamComb.isEmpty()) {
            daoCondParamComb = allDAOCondParamAttrs;
        }

        return log.exit(Collections.unmodifiableSet(daoCondParamComb));
    }
    private static Set<CallDataDAOFilter> convertCallFilterToCallDataDAOFilters(
            ExpressionCallFilter callFilter) {
        log.entry(callFilter);

        //just to make sure we cover all ExpressionSummary cases
        assert SummaryCallType.ExpressionSummary.values().length == 2;
        boolean isExpression = callFilter.getSummaryCallTypeFilter() == null || 
                SummaryCallType.ExpressionSummary.EXPRESSED.equals(callFilter.getSummaryCallTypeFilter());
        boolean isNoExpression = callFilter.getSummaryCallTypeFilter() == null || 
                SummaryCallType.ExpressionSummary.NOT_EXPRESSED.equals(callFilter.getSummaryCallTypeFilter());
        assert isExpression || isNoExpression;
        //make sure SummaryQuality.BRONZE is the lowest Quality level
        assert SummaryQuality.values()[0].equals(SummaryQuality.BRONZE);
        SummaryQuality requestedQual = callFilter.getSummaryQualityFilter() == null?
                SummaryQuality.BRONZE: callFilter.getSummaryQualityFilter();

        boolean lowestQual = SummaryQuality.BRONZE.equals(requestedQual);
        boolean allDataTypesSelected = callFilter.getDataTypeFilters() == null ||
                callFilter.getDataTypeFilters().isEmpty() ||
                callFilter.getDataTypeFilters().equals(EnumSet.allOf(DataType.class));

        //absolutely no filtering necessary on experiment expression counts in following case
        if (isExpression && isNoExpression && lowestQual &&
                !callFilter.isObservedDataOnly() && allDataTypesSelected) {
            return log.exit(null);
        }
        
        
        Set<CallDataDAOFilter.DataType> daoDataTypes = Collections.unmodifiableSet(
                convertDataTypeToDAODataType(callFilter.getDataTypeFilters()));
        
        Set<DAOExperimentCountFilter.CallType> tmpConsideredCallTypes =
                EnumSet.noneOf(DAOExperimentCountFilter.CallType.class);
        //just to make sure we cover all DAOExperimentCountFilter.CallTypes
        assert DAOExperimentCountFilter.CallType.values().length == 2;
        if (isExpression) {
            tmpConsideredCallTypes.add(DAOExperimentCountFilter.CallType.PRESENT);
        }
        if (isNoExpression) {
            tmpConsideredCallTypes.add(DAOExperimentCountFilter.CallType.ABSENT);
        }
        final Set<DAOExperimentCountFilter.CallType> consideredCallTypes =
                Collections.unmodifiableSet(EnumSet.copyOf(tmpConsideredCallTypes));
        
        
        Set<CallDataDAOFilter> callDataDAOFilters = new HashSet<>();
        
        if (callFilter.isObservedDataOnly()) {
            Set<DAOExperimentCountFilter> observedFilters =
                    //to check observed data we check all call types whatever the requested
                    //call types are.
                    EnumSet.allOf(DAOExperimentCountFilter.CallType.class).stream()
                    .flatMap(callType -> EnumSet.allOf(DAOExperimentCountFilter.DataQuality.class)
                            .stream()
                            .map(dataQual -> new DAOExperimentCountFilter(callType, dataQual,
                                    DAOExperimentCountFilter.PropagationState.SELF,
                                    DAOExperimentCountFilter.Qualifier.GREATER_THAN, 0)))
                    .collect(Collectors.toSet());
            
            callDataDAOFilters.add(new CallDataDAOFilter(observedFilters, daoDataTypes));
        }
        if (isNoExpression && !isExpression) {
            Set<DAOExperimentCountFilter> noExpressionFilters =
                    EnumSet.allOf(DAOExperimentCountFilter.DataQuality.class).stream()
                    .map(dataQual -> new DAOExperimentCountFilter(
                            DAOExperimentCountFilter.CallType.PRESENT, dataQual,
                            DAOExperimentCountFilter.PropagationState.ALL,
                            DAOExperimentCountFilter.Qualifier.EQUALS_TO, 0))
                    .collect(Collectors.toSet());
            
            callDataDAOFilters.add(new CallDataDAOFilter(noExpressionFilters, daoDataTypes));
        }
        if (isExpression != isNoExpression || !lowestQual) {
            Set<DAOExperimentCountFilter> generalFilters = null;
            switch (requestedQual) {
            case BRONZE:
                generalFilters = consideredCallTypes.stream()
                .map(callType -> new DAOExperimentCountFilter(callType,
                        DAOExperimentCountFilter.DataQuality.LOW,
                        DAOExperimentCountFilter.PropagationState.ALL,
                        DAOExperimentCountFilter.Qualifier.GREATER_THAN, MIN_LOW_BRONZE - 1))
                .collect(Collectors.toSet());
                //also need to get calls supported by high quality data only
                generalFilters.addAll(consideredCallTypes.stream()
                        .map(callType -> new DAOExperimentCountFilter(callType,
                                DAOExperimentCountFilter.DataQuality.HIGH,
                                DAOExperimentCountFilter.PropagationState.ALL,
                                DAOExperimentCountFilter.Qualifier.GREATER_THAN, MIN_HIGH_BRONZE - 1))
                        .collect(Collectors.toSet()));
                break;
            case SILVER:
                generalFilters = consideredCallTypes.stream()
                .map(callType -> new DAOExperimentCountFilter(callType,
                        DAOExperimentCountFilter.DataQuality.LOW,
                        DAOExperimentCountFilter.PropagationState.ALL,
                        DAOExperimentCountFilter.Qualifier.GREATER_THAN, MIN_LOW_SILVER - 1))
                .collect(Collectors.toSet());
                generalFilters.addAll(consideredCallTypes.stream()
                        .map(callType -> new DAOExperimentCountFilter(callType,
                                DAOExperimentCountFilter.DataQuality.HIGH,
                                DAOExperimentCountFilter.PropagationState.ALL,
                                DAOExperimentCountFilter.Qualifier.GREATER_THAN, MIN_HIGH_SILVER - 1))
                        .collect(Collectors.toSet()));
                break;
            case GOLD:
                generalFilters = consideredCallTypes.stream()
                .map(callType -> new DAOExperimentCountFilter(callType,
                        DAOExperimentCountFilter.DataQuality.HIGH,
                        DAOExperimentCountFilter.PropagationState.ALL,
                        DAOExperimentCountFilter.Qualifier.GREATER_THAN, MIN_HIGH_GOLD - 1))
                .collect(Collectors.toSet());
                break;
            default:
                throw log.throwing(new UnsupportedOperationException(
                        "Unsupported SummaryQuality: " + requestedQual));
            }
            
            assert generalFilters != null;
            callDataDAOFilters.add(new CallDataDAOFilter(generalFilters, daoDataTypes));
        }
        
        //If no filtering requested so far, but not all data types were requested,
        //we need to create a filter for this.
        if (callDataDAOFilters.isEmpty() && !allDataTypesSelected) {
            assert EnumSet.allOf(CallType.Expression.class).equals(consideredCallTypes);
            assert lowestQual;
            assert isExpression && isNoExpression;
            assert !callFilter.isObservedDataOnly();
            assert daoDataTypes != null && !daoDataTypes.isEmpty() &&
                    !EnumSet.allOf(CallDataDAOFilter.DataType.class).equals(daoDataTypes);
            
            Set<DAOExperimentCountFilter> dataTypeFilters = consideredCallTypes.stream()
                    .flatMap(callType -> EnumSet.allOf(DAOExperimentCountFilter.DataQuality.class)
                            .stream()
                            .map(dataQual -> new DAOExperimentCountFilter(callType, dataQual,
                                    DAOExperimentCountFilter.PropagationState.ALL,
                                    DAOExperimentCountFilter.Qualifier.GREATER_THAN, 0)))
                    .collect(Collectors.toSet());
            
            callDataDAOFilters.add(new CallDataDAOFilter(dataTypeFilters, daoDataTypes));
        }
        
        //the method should have exited right away if no filtering was necessary
        assert !callDataDAOFilters.isEmpty();
        return log.exit(callDataDAOFilters);
    }

    private static Set<ConditionDAO.Attribute> convertServiceAttrsToConditionDAOAttrs(
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
    private static Set<ConditionDAO.Attribute> convertServiceOrderingAttrsToConditionDAOAttrs(
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
        Set<Attribute> attributes, Set<DataType> dataTypesRequested) {
        log.entry(attributes, dataTypesRequested);
        
        Set<GlobalExpressionCallDAO.Attribute> attrFromDataTypes = dataTypesRequested.stream()
                .map(type -> Optional.ofNullable(DATA_TYPE_TO_GLOBAL_EXPR_ATTR.get(type))
                //bug of javac for type inference, we need to type the exception 
                //explicitly to RuntimeException,
                //see http://stackoverflow.com/questions/25523375/java8-lambdas-and-exceptions
                .<RuntimeException>orElseThrow(() -> 
                    log.throwing(new IllegalStateException("Unsupported DataType: " + type))))
                .flatMap(Set::stream)
                .collect(Collectors.toSet());
        
        return log.exit(attributes.stream().flatMap(attr -> {
            switch (attr) {
                case GENE: 
                    return Stream.of(GlobalExpressionCallDAO.Attribute.BGEE_GENE_ID);
                case ANAT_ENTITY_ID: 
                case DEV_STAGE_ID: 
                    return Stream.of(GlobalExpressionCallDAO.Attribute.CONDITION_ID);
                case CALL_TYPE: 
                case DATA_QUALITY:
                    return attrFromDataTypes.stream()
                            .filter(daoAttr -> daoAttr.isTotalAttribute());
                case CALL_DATA:
                    return attrFromDataTypes.stream();
                case OBSERVED_DATA:
                    return attrFromDataTypes.stream()
                            .filter(daoAttr -> daoAttr.isSelfAttribute());
                case RANK: 
                    return Stream.of(GlobalExpressionCallDAO.Attribute.GLOBAL_MEAN_RANK);
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

    /** 
     * Return the {@code Comparator} of {@code ExpressionCall}s, performing the comparisons
     * in order provided by {@code orderingAttributes}.
     * 
     * @param orderingAttributes    A {@code LinkedHashMap} where keys are 
     *                              {@code CallService.OrderingAttribute}s defining the attributes
     *                              used to order the returned {@code ExpressionCall}s, 
     *                              the associated value being a {@code Service.Direction} defining 
     *                              whether the ordering should be ascendant or descendant.
     * @return                      The {@code Comparator} of {@code ExpressionCall}s.
     */
    private static Comparator<ExpressionCall> convertServiceOrdering(
            LinkedHashMap<CallService.OrderingAttribute, Service.Direction> orderingAttributes) {
        log.entry(orderingAttributes);
        
        Comparator<ExpressionCall> comparator = null;
        for (Entry<CallService.OrderingAttribute, Service.Direction> entry: orderingAttributes.entrySet()) {
            Comparator<String> compStr = null;
            Comparator<BigDecimal> compBigD = null;
            switch (entry.getValue()) {
                case ASC:
                    compStr = Comparator.nullsLast(Comparator.naturalOrder());
                    compBigD = Comparator.nullsLast(Comparator.naturalOrder());
                    break;
                case DESC:
                    compStr = Comparator.nullsLast(Comparator.reverseOrder());
                    compBigD = Comparator.nullsLast(Comparator.reverseOrder());
                    break;
                default: 
                    throw log.throwing(new IllegalStateException("Unsupported Service.Direction: " +
                        entry.getValue()));
            }
    
            Comparator<ExpressionCall> tmpComp = null;
            switch (entry.getKey()) {
                case GENE_ID:
                    tmpComp = Comparator.comparing(c -> c.getGene() == null?
                        null: c.getGene().getEnsemblGeneId(), compStr);
                    break;
                case ANAT_ENTITY_ID:
                    tmpComp = Comparator.comparing(c -> c.getCondition() == null? 
                        null : c.getCondition().getAnatEntityId(), compStr);
                    break;
                case DEV_STAGE_ID:
                    tmpComp = Comparator.comparing(c -> c.getCondition() == null? 
                        null : c.getCondition().getDevStageId(), compStr);
                    break;
                case GLOBAL_RANK:
                    tmpComp = Comparator.comparing(c -> c.getGlobalMeanRank(), compBigD);
                    break;
                default: 
                    throw log.throwing(new IllegalStateException("Unsupported OrderingAttribute: " + 
                        entry.getKey()));
            }
            
            if (comparator == null) {
                comparator = tmpComp;
            } else {
                comparator = comparator.thenComparing(tmpComp);
            }
        }
        return log.exit(comparator);
    }

    private static Set<CallDataDAOFilter.DataType> convertDataTypeToDAODataType(Set<DataType> dts) 
            throws IllegalStateException{
        log.entry(dts);
        
        return log.exit(dts.stream()
            .map(dt -> {
                switch(dt) {
                    case AFFYMETRIX: 
                        return log.exit(CallDataDAOFilter.DataType.AFFYMETRIX);
                    case EST: 
                        return log.exit(CallDataDAOFilter.DataType.EST);
                    case IN_SITU: 
                        return log.exit(CallDataDAOFilter.DataType.IN_SITU);
                    case RNA_SEQ: 
                        return log.exit(CallDataDAOFilter.DataType.RNA_SEQ);
                    default: 
                        throw log.throwing(new IllegalStateException("Unsupported DataType: " + dt));
                }
        }).collect(Collectors.toSet()));
    }

    //*************************************************************************
    // METHODS MAPPING GlobalExpressionCallTOs TO ExpressionCalls
    //*************************************************************************
    private static ExpressionCall mapGlobalCallTOToExpressionCall(GlobalExpressionCallTO globalCallTO, 
            Species species, Map<Integer, Gene> geneMap, Map<Integer, Condition> condMap,
            Set<CallService.Attribute> attrs) {
        log.entry(globalCallTO, species, geneMap, condMap, attrs);
        
        Set<ExpressionCallData> callData = mapGlobalCallTOToExpressionCallData(globalCallTO);

        return log.exit(new ExpressionCall(geneMap.get(globalCallTO.getBgeeGeneId()), 
            condMap.get(globalCallTO.getConditionId()),
            inferIsObservedData(callData),
            inferSummaryCallType(callData),
            inferSummaryQuality(callData),
            callData,
            globalCallTO.getGlobalMeanRank()));
    }
    
    private static Set<ExpressionCallData> mapGlobalCallTOToExpressionCallData(
            GlobalExpressionCallTO globalCallTO) {
        log.entry(globalCallTO);
        
        Set<ExpressionCallData> allCallData = new HashSet<>();
        for (DataType dt : DataType.values()) {
            ExpressionCallData callData;
            Set<ExperimentExpressionCount> counts;
            switch (dt) {
                case AFFYMETRIX:
                    counts = new HashSet<>();
                    counts.add(new ExperimentExpressionCount(CallType.Expression.EXPRESSED, DataQuality.HIGH,
                        PropagationState.SELF, globalCallTO.getAffymetrixExpPresentHighSelfCount()));
                    counts.add(new ExperimentExpressionCount(CallType.Expression.EXPRESSED, DataQuality.LOW,
                        PropagationState.SELF, globalCallTO.getAffymetrixExpPresentLowSelfCount()));
                    counts.add(new ExperimentExpressionCount(CallType.Expression.NOT_EXPRESSED, DataQuality.HIGH,
                        PropagationState.SELF, globalCallTO.getAffymetrixExpAbsentHighSelfCount()));
                    counts.add(new ExperimentExpressionCount(CallType.Expression.NOT_EXPRESSED, DataQuality.LOW,
                        PropagationState.SELF, globalCallTO.getAffymetrixExpAbsentLowSelfCount()));
                    counts.add(new ExperimentExpressionCount(CallType.Expression.EXPRESSED, DataQuality.HIGH,
                        PropagationState.DESCENDANT, globalCallTO.getAffymetrixExpPresentHighDescendantCount()));
                    counts.add(new ExperimentExpressionCount(CallType.Expression.EXPRESSED, DataQuality.LOW,
                        PropagationState.DESCENDANT, globalCallTO.getAffymetrixExpPresentLowDescendantCount()));
                    counts.add(new ExperimentExpressionCount(CallType.Expression.NOT_EXPRESSED, DataQuality.HIGH,
                        PropagationState.ANCESTOR, globalCallTO.getAffymetrixExpAbsentHighParentCount()));
                    counts.add(new ExperimentExpressionCount(CallType.Expression.NOT_EXPRESSED, DataQuality.LOW,
                        PropagationState.ANCESTOR, globalCallTO.getAffymetrixExpAbsentLowParentCount()));
                    counts.add(new ExperimentExpressionCount(CallType.Expression.EXPRESSED, DataQuality.HIGH,
                        PropagationState.ALL, globalCallTO.getAffymetrixExpPresentHighTotalCount()));
                    counts.add(new ExperimentExpressionCount(CallType.Expression.EXPRESSED, DataQuality.LOW,
                        PropagationState.ALL, globalCallTO.getAffymetrixExpPresentLowTotalCount()));
                    counts.add(new ExperimentExpressionCount(CallType.Expression.NOT_EXPRESSED, DataQuality.HIGH,
                        PropagationState.ALL, globalCallTO.getAffymetrixExpAbsentHighTotalCount()));
                    counts.add(new ExperimentExpressionCount(CallType.Expression.NOT_EXPRESSED, DataQuality.LOW,
                        PropagationState.ALL, globalCallTO.getAffymetrixExpAbsentLowTotalCount()));
                    callData = new ExpressionCallData(dt, counts, globalCallTO.getAffymetrixExpPropagatedCount(),
                        globalCallTO.getAffymetrixMeanRank(), globalCallTO.getAffymetrixMeanRankNorm(),
                        globalCallTO.getAffymetrixDistinctRankSum());
                    break;
                case EST:
                    counts = new HashSet<>();
                    counts.add(new ExperimentExpressionCount(CallType.Expression.EXPRESSED, DataQuality.HIGH, PropagationState.SELF,
                        globalCallTO.getESTLibPresentHighSelfCount()));
                    counts.add(new ExperimentExpressionCount(CallType.Expression.EXPRESSED, DataQuality.LOW, PropagationState.SELF,
                        globalCallTO.getESTLibPresentLowSelfCount()));
                    counts.add(new ExperimentExpressionCount(CallType.Expression.EXPRESSED, DataQuality.HIGH, PropagationState.DESCENDANT,
                        globalCallTO.getESTLibPresentHighDescendantCount()));
                    counts.add(new ExperimentExpressionCount(CallType.Expression.EXPRESSED, DataQuality.LOW, PropagationState.DESCENDANT,
                        globalCallTO.getESTLibPresentLowDescendantCount()));
                    counts.add(new ExperimentExpressionCount(CallType.Expression.EXPRESSED, DataQuality.HIGH, PropagationState.ALL,
                        globalCallTO.getESTLibPresentHighTotalCount()));
                    counts.add(new ExperimentExpressionCount(CallType.Expression.EXPRESSED, DataQuality.LOW, PropagationState.ALL,
                        globalCallTO.getESTLibPresentLowTotalCount()));
                    callData = new ExpressionCallData(dt, counts, globalCallTO.getESTLibPropagatedCount(),
                        globalCallTO.getESTRank(), globalCallTO.getESTRankNorm(),
                        null); // rankSum
                    break;
                case IN_SITU:
                    counts = new HashSet<>();
                    counts.add(new ExperimentExpressionCount(CallType.Expression.EXPRESSED, DataQuality.HIGH, PropagationState.SELF,
                        globalCallTO.getInSituExpPresentHighSelfCount()));
                    counts.add(new ExperimentExpressionCount(CallType.Expression.EXPRESSED, DataQuality.LOW, PropagationState.SELF,
                        globalCallTO.getInSituExpPresentLowSelfCount()));
                    counts.add(new ExperimentExpressionCount(CallType.Expression.NOT_EXPRESSED, DataQuality.HIGH, PropagationState.SELF,
                        globalCallTO.getInSituExpAbsentHighSelfCount()));
                    counts.add(new ExperimentExpressionCount(CallType.Expression.NOT_EXPRESSED, DataQuality.LOW, PropagationState.SELF,
                        globalCallTO.getInSituExpAbsentLowSelfCount()));
                    counts.add(new ExperimentExpressionCount(CallType.Expression.EXPRESSED, DataQuality.HIGH, PropagationState.DESCENDANT,
                        globalCallTO.getInSituExpPresentHighDescendantCount()));
                    counts.add(new ExperimentExpressionCount(CallType.Expression.EXPRESSED, DataQuality.LOW, PropagationState.DESCENDANT,
                        globalCallTO.getInSituExpPresentLowDescendantCount()));
                    counts.add(new ExperimentExpressionCount(CallType.Expression.NOT_EXPRESSED, DataQuality.HIGH, PropagationState.ANCESTOR,
                        globalCallTO.getInSituExpAbsentHighParentCount()));
                    counts.add(new ExperimentExpressionCount(CallType.Expression.NOT_EXPRESSED, DataQuality.LOW, PropagationState.ANCESTOR,
                        globalCallTO.getInSituExpAbsentLowParentCount()));
                    counts.add(new ExperimentExpressionCount(CallType.Expression.EXPRESSED, DataQuality.HIGH, PropagationState.ALL,
                        globalCallTO.getInSituExpPresentHighTotalCount()));
                    counts.add(new ExperimentExpressionCount(CallType.Expression.EXPRESSED, DataQuality.LOW, PropagationState.ALL,
                        globalCallTO.getInSituExpPresentLowTotalCount()));
                    counts.add(new ExperimentExpressionCount(CallType.Expression.NOT_EXPRESSED, DataQuality.HIGH, PropagationState.ALL,
                        globalCallTO.getInSituExpAbsentHighTotalCount()));
                    counts.add(new ExperimentExpressionCount(CallType.Expression.NOT_EXPRESSED, DataQuality.LOW, PropagationState.ALL,
                        globalCallTO.getInSituExpAbsentLowTotalCount()));
                    callData = new ExpressionCallData(dt, counts, globalCallTO.getInSituExpPropagatedCount(),
                        globalCallTO.getInSituRank(), globalCallTO.getInSituRankNorm(),
                        null);  // rankSum
                    break;
                case RNA_SEQ:
                    counts = new HashSet<>();
                    counts.add(new ExperimentExpressionCount(CallType.Expression.EXPRESSED, DataQuality.HIGH, PropagationState.SELF,
                        globalCallTO.getRNASeqExpPresentHighSelfCount()));
                    counts.add(new ExperimentExpressionCount(CallType.Expression.EXPRESSED, DataQuality.LOW, PropagationState.SELF,
                        globalCallTO.getRNASeqExpPresentLowSelfCount()));
                    counts.add(new ExperimentExpressionCount(CallType.Expression.NOT_EXPRESSED, DataQuality.HIGH, PropagationState.SELF,
                        globalCallTO.getRNASeqExpAbsentHighSelfCount()));
                    counts.add(new ExperimentExpressionCount(CallType.Expression.NOT_EXPRESSED, DataQuality.LOW, PropagationState.SELF,
                        globalCallTO.getRNASeqExpAbsentLowSelfCount()));
                    counts.add(new ExperimentExpressionCount(CallType.Expression.EXPRESSED, DataQuality.HIGH, PropagationState.DESCENDANT,
                        globalCallTO.getRNASeqExpPresentHighDescendantCount()));
                    counts.add(new ExperimentExpressionCount(CallType.Expression.EXPRESSED, DataQuality.LOW, PropagationState.DESCENDANT,
                        globalCallTO.getRNASeqExpPresentLowDescendantCount()));
                    counts.add(new ExperimentExpressionCount(CallType.Expression.NOT_EXPRESSED, DataQuality.HIGH, PropagationState.ANCESTOR,
                        globalCallTO.getRNASeqExpAbsentHighParentCount()));
                    counts.add(new ExperimentExpressionCount(CallType.Expression.NOT_EXPRESSED, DataQuality.LOW, PropagationState.ANCESTOR,
                        globalCallTO.getRNASeqExpAbsentLowParentCount()));
                    counts.add(new ExperimentExpressionCount(CallType.Expression.EXPRESSED, DataQuality.HIGH, PropagationState.ALL,
                        globalCallTO.getRNASeqExpPresentHighTotalCount()));
                    counts.add(new ExperimentExpressionCount(CallType.Expression.EXPRESSED, DataQuality.LOW, PropagationState.ALL,
                        globalCallTO.getRNASeqExpPresentLowTotalCount()));
                    counts.add(new ExperimentExpressionCount(CallType.Expression.NOT_EXPRESSED, DataQuality.HIGH, PropagationState.ALL,
                        globalCallTO.getRNASeqExpAbsentHighTotalCount()));
                    counts.add(new ExperimentExpressionCount(CallType.Expression.NOT_EXPRESSED, DataQuality.LOW, PropagationState.ALL,
                        globalCallTO.getRNASeqExpAbsentLowTotalCount()));
                    callData = new ExpressionCallData(dt, counts, globalCallTO.getRNASeqExpPropagatedCount(),
                        globalCallTO.getRNASeqMeanRank(), globalCallTO.getRNASeqMeanRankNorm(),
                        globalCallTO.getRNASeqDistinctRankSum());
                    break;
                default:
                    throw log.throwing(new IllegalStateException("Unsupported DataType: " + dt));
            }
            allCallData.add(callData);
        }
        assert !allCallData.isEmpty();
        return log.exit(allCallData);
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

    /**
     * Infer if {@code callData} contains observed data.
     * 
     * @param callData  A {@code Set} of {@code U}s that are {@code CallData} to be used.
     * @return          The {@code T} that is the inferred {@code SummaryCallType}.
     * @param <T>       The type of {CallData}.
     */
    private static <T extends CallData<?>> Boolean inferIsObservedData(Set<T> callData) {
        log.entry(callData);
        if (callData.stream().anyMatch(cd -> cd.isObservedData())) {
            return log.exit(true);
        }
        return log.exit(false);
    }
    
    /**
     * Infer call type summary from {@code callData}.
     * 
     * @param callData  A {@code Set} of {@code ExpressionCallData}s that are {@code CallData} to be used.
     * @return          The {@code ExpressionSummary} that is the inferred call type quality.
     */
    private static ExpressionSummary inferSummaryCallType(Set<ExpressionCallData> callData) {
        log.entry(callData);
        
        if (callData.stream().anyMatch(cd -> ExpressionSummary.EXPRESSED.equals(cd.getCallType()))) {
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
        if (expPresentHigh == MIN_HIGH_SILVER || expPresentLow >= MIN_LOW_SILVER) {
            return log.exit(SummaryQuality.SILVER);
        }
        if (expPresentLow == MIN_LOW_BRONZE) {
            return log.exit(SummaryQuality.BRONZE);
        }
        assert expPresentHigh == 0 && expPresentLow == 0;
        
        if (expAbsentHigh >= MIN_HIGH_GOLD) {
            return log.exit(SummaryQuality.GOLD);
        }
        if (expAbsentHigh == MIN_HIGH_SILVER || expAbsentLow >= MIN_LOW_SILVER) {
            return log.exit(SummaryQuality.SILVER);
        }
        if (expAbsentLow == MIN_LOW_BRONZE) {
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
        return log.exit(cd.getExperimentCounts().stream()
            .filter(c -> expr.equals(c.getCallType()) && qual.equals(c.getDataQuality())
                        && state.equals(c.getPropagationState()))
            .map(c -> c.getCount())
            .mapToInt(Integer::intValue)
            .sum());
    }
}
