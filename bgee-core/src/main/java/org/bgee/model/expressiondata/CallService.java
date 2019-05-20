package org.bgee.model.expressiondata;

import java.math.BigDecimal;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.CommonService;
import org.bgee.model.ElementGroupFromListSpliterator;
import org.bgee.model.Service;
import org.bgee.model.ServiceFactory;
import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.anatdev.AnatEntityService;
import org.bgee.model.anatdev.DevStageService;
import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.expressiondata.CallDAOFilter;
import org.bgee.model.dao.api.expressiondata.CallDataDAOFilter;
import org.bgee.model.dao.api.expressiondata.ConditionDAO;
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
import org.bgee.model.expressiondata.MultiGeneExprAnalysis.MultiGeneExprCounts;
import org.bgee.model.expressiondata.baseelements.CallType;
import org.bgee.model.expressiondata.baseelements.CallType.Expression;
import org.bgee.model.expressiondata.baseelements.DataPropagation;
import org.bgee.model.expressiondata.baseelements.PropagationState;
import org.bgee.model.expressiondata.baseelements.QualitativeExpressionLevel;
import org.bgee.model.expressiondata.baseelements.DataQuality;
import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.model.expressiondata.baseelements.EntityMinMaxRanks;
import org.bgee.model.expressiondata.baseelements.ExperimentExpressionCount;
import org.bgee.model.expressiondata.baseelements.ExpressionLevelCategory;
import org.bgee.model.expressiondata.baseelements.ExpressionLevelInfo;
import org.bgee.model.expressiondata.baseelements.SummaryCallType;
import org.bgee.model.expressiondata.baseelements.SummaryCallType.ExpressionSummary;
import org.bgee.model.expressiondata.baseelements.SummaryQuality;
import org.bgee.model.gene.Gene;
import org.bgee.model.gene.GeneFilter;
import org.bgee.model.gene.GeneNotFoundException;
import org.bgee.model.species.Species;

/**
 * A {@link Service} to obtain {@link Call} objects. 
 * Users should use the {@link org.bgee.model.ServiceFactory ServiceFactory} to obtain {@code CallService}s.
 * 
 * @author  Frederic Bastian
 * @author  Valentine Rech de Laval
 * @author  Julien Wollbrett
 * @version Bgee 14, Apr. 2019
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
     * <li>{@code OBSERVED_DATA}: corresponds to {@link Call#getDataPropagation()}.
     * <li>{@code MEAN_RANK}: corresponds to {@link ExpressionCall#getMeanRank()}.
     * <li>{@code EXPERIMENT_COUNTS}: corresponds to {@link Call#getCallData()} with experiment
     * expression <strong>total</strong> and <strong>self</strong> counts populated per data type
     * for the requested data types.
     * <li>{@code DATA_TYPE_RANK_INFO}: corresponds to {@link Call#getCallData()}
     * with rank info populated per data type for the requested data types.
     * <li>{@code GENE_QUAL_EXPR_LEVEL}: corresponds to {@link
     * org.bgee.model.expressiondata.baseelements.ExpressionLevelInfo#getQualExprLevelRelativeToGene()
     * ExpressionLevelInfo.getQualExprLevelRelativeToGene()} from {@link ExpressionCall#getExpressionLevelInfo()}.
     * <strong>Important:</strong> if this attribute is used, then the attributes {@code GENE},
     * {@code CALL_TYPE} and {@code MEAN_RANK} must also be requested, otherwise an {@code IllegalArgumentException}
     * will be thrown by the methods.
     * <li>{@code ANAT_ENTITY_QUAL_EXPR_LEVEL}: corresponds to {@link
     * org.bgee.model.expressiondata.baseelements.ExpressionLevelInfo#getQualExprLevelRelativeToAnatEntity()
     * ExpressionLevelInfo.getQualExprLevelRelativeToAnatEntity()} from {@link ExpressionCall#getExpressionLevelInfo()}.
     * <strong>Important:</strong> if this attribute is used, then the attributes {@code ANAT_ENTITY_ID},
     * {@code CALL_TYPE} and {@code MEAN_RANK} must also be requested, otherwise an {@code IllegalArgumentException}
     * will be thrown by the methods.
     * </ul>
     */
    public static enum Attribute implements Service.Attribute {
        GENE(false), ANAT_ENTITY_ID(true), DEV_STAGE_ID(true), CALL_TYPE(false),
        DATA_QUALITY(false), OBSERVED_DATA(false), MEAN_RANK(false),
        EXPERIMENT_COUNTS(false), DATA_TYPE_RANK_INFO(false),
        GENE_QUAL_EXPR_LEVEL(false), ANAT_ENTITY_QUAL_EXPR_LEVEL(false);
        
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
        
        public String getCondParamName() {
            log.entry();
            if (!this.isConditionParameter()) {
                return log.exit(null);
            }
            switch(this) {
            case ANAT_ENTITY_ID:
                return log.exit("anatomicalEntity");
            case DEV_STAGE_ID:
                return log.exit("developmentalStage");
            default:
                throw log.throwing(new IllegalStateException("Cond param not supported"));
            }
        }

        /**
         * @return  An {@code EnumSet} containing all {@code Attribute}s that are condition parameters
         *          ({@link #isConditionParameter()} returns {@code true}).
         */
        public static EnumSet<Attribute> getAllConditionParameters() {
            log.entry();
            return log.exit(Arrays.stream(CallService.Attribute.values())
                    .filter(a -> a.isConditionParameter())
                    .collect(Collectors.toCollection(() -> EnumSet.noneOf(CallService.Attribute.class))));
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
    //Note: we currently never have a bronze quality produced if we have one experiment
    //showing expression with high quality, but it is there in case we change the threshold
    //in the future.
    //XXX: either we should have also a MIN_LOW_GOLD, or we shouldn't have this MIN_HIGH_BRONZE
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
    /**
     * A {@code Set} of {@code ConditionDAO.Attribute}s used as of Bgee 14 to retrieve ranks
     * (for now, we don't use mean ranks computed for anatomical entities only, or dev. stages only;
     * that might be the case in future releases of Bgee).
     */
    private static final Set<ConditionDAO.Attribute> COND_PARAM_COMBINATION_FOR_RANKS = EnumSet.of(
            ConditionDAO.Attribute.ANAT_ENTITY_ID, ConditionDAO.Attribute.STAGE_ID);
    /**
     * A {@code Map} containing a single {@code Entry} where the key is the {@code ExpressionSummary}
     * and the value is the {@code SummaryQuality} necessary to correctly retrieve all rank info.
     * Used to initialize a new {@link CallFilter.ExpressionCallFilter}.
     */
    private static final Map<SummaryCallType.ExpressionSummary, SummaryQuality>
    CALL_TYPE_QUAL_FOR_RANKS_FILTER = Collections.singletonMap(ExpressionSummary.EXPRESSED,
            SummaryQuality.BRONZE);
    /**
     * A {@code Map} containing a single {@code Entry} where the key is the {@code Expression}
     * and the value is the {@code Boolean} necessary to correctly retrieve all rank info.
     * Used to initialize a new {@link CallFilter.ExpressionCallFilter}.
     * Needed since as of Bgee 14, ranks are computed only for observed calls.
     */
    private final static Map<Expression, Boolean> OBSERVED_DATA_FOR_RANKS_FILTER =
            Collections.singletonMap(null, true);
    //*************************************************
    // INSTANCE ATTRIBUTES AND CONSTRUCTOR
    //*************************************************
    protected final ConditionDAO conditionDAO;
    private final GeneDAO geneDAO;
    private final GlobalExpressionCallDAO globalExprCallDAO;
    private final AnatEntityService anatEntityService;
    private final DevStageService devStageService;
//    /**
//     * @param serviceFactory            The {@code ServiceFactory} to be used to obtain {@code Service}s 
//     *                                  and {@code DAOManager}.
//     * @throws IllegalArgumentException If {@code serviceFactory} is {@code null}.
//     */
//    public CallService(ServiceFactory serviceFactory) {
//        this(serviceFactory,
//                Optional.ofNullable(serviceFactory.getDAOManager().getConditionDAO().getMaxRank())
//                .map(maxRankTO -> maxRankTO.getGlobalMaxRank())
//                .orElseThrow(() -> new IllegalStateException("No max rank could be retrieved.")));
//    }
    /**
//     * Constructor useful in case the ranks and max ranks have not yet been inserted,
//     * before expression call propagation. There is no check performed to make sure
//     * a max rank could be retrieved.
//     *
     * @param serviceFactory    The {@code ServiceFactory} to be used to obtain {@code Service}s 
     *                          and {@code DAOManager}.
//     * @param maxRank           A {@code BigDecimal} that is the max expression rank over
//     *                          all conditions and data types. Can be {@code null}.
     */
    public CallService(ServiceFactory serviceFactory) {
        super(serviceFactory);
        this.conditionDAO = this.getDaoManager().getConditionDAO();
        this.geneDAO = this.getDaoManager().getGeneDAO();
        this.globalExprCallDAO = this.getDaoManager().getGlobalExpressionCallDAO();
        this.anatEntityService = this.getServiceFactory().getAnatEntityService();
        this.devStageService = this.getServiceFactory().getDevStageService();
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
        if (clonedAttrs.contains(Attribute.GENE_QUAL_EXPR_LEVEL) &&
                (!clonedAttrs.contains(Attribute.GENE) || !clonedAttrs.contains(Attribute.MEAN_RANK) ||
                        !clonedAttrs.contains(Attribute.CALL_TYPE))) {
            throw log.throwing(new IllegalArgumentException(
                    "If " + Attribute.GENE_QUAL_EXPR_LEVEL + " is requested, "
                    + Attribute.GENE + ", " + Attribute.MEAN_RANK + ", and " + Attribute.CALL_TYPE
                    + " must also be requested"));
        }
        if (clonedAttrs.contains(Attribute.ANAT_ENTITY_QUAL_EXPR_LEVEL) &&
                (!clonedAttrs.contains(Attribute.ANAT_ENTITY_ID) || !clonedAttrs.contains(Attribute.MEAN_RANK) ||
                        !clonedAttrs.contains(Attribute.CALL_TYPE))) {
            throw log.throwing(new IllegalArgumentException(
                    "If " + Attribute.ANAT_ENTITY_QUAL_EXPR_LEVEL + " is requested, "
                    + Attribute.ANAT_ENTITY_ID + ", " + Attribute.MEAN_RANK + ", and " + Attribute.CALL_TYPE
                    + " must also be requested"));
        }

        // Retrieve species, get a map species ID -> Species
        final Map<Integer, Species> speciesMap = loadSpeciesMapFromGeneFilters(callFilter.getGeneFilters(),
                this.getServiceFactory().getSpeciesService());

        //Retrieve a Map of Bgee gene IDs to Gene. This will throw a GeneNotFoundException
        //if some requested gene IDs were not found in Bgee.
        Map<Integer, Gene> geneMap = loadGeneMapFromGeneFilters(callFilter.getGeneFilters(), speciesMap, this.geneDAO);
        assert !geneMap.isEmpty();

        // Define condition parameter combination allowing to target a specific data aggregation
        final Set<ConditionDAO.Attribute> condParamCombination = Collections.unmodifiableSet(
                loadConditionParameterCombination(callFilter, clonedAttrs, clonedOrderingAttrs.keySet()));

        // Retrieve conditions by condition IDs if condition info requested in Attributes
        // (explicitly, or because clonedAttrs is empty and all attributes are requested),
        // or ANAT_ENTITY_QUAL_EXPR_LEVEL is requested (which is pretty much the same,
        // since it's mandatory to request anat. entity info in that case)
        boolean loadCondMap = clonedAttrs.isEmpty() ||
                clonedAttrs.contains(Attribute.ANAT_ENTITY_QUAL_EXPR_LEVEL) ||
                clonedAttrs.stream().anyMatch(a -> a.isConditionParameter());
        final Map<Integer, Condition> condMap = Collections.unmodifiableMap(
                !loadCondMap?
                    new HashMap<>():
                    loadGlobalConditionMap(speciesMap.values(),
                        condParamCombination, convertCondParamAttrsToCondDAOAttrs(clonedAttrs),
                        this.conditionDAO, this.anatEntityService, this.devStageService));

        // Retrieve min./max ranks per anat. entity if info requested
        // and if the main expression call query will not allow to obtain this information
        Map<AnatEntity, EntityMinMaxRanks<AnatEntity>> anatEntityMinMaxRanks =
                loadMinMaxRanksPerAnatEntity(clonedAttrs, clonedOrderingAttrs, condParamCombination,
                        geneMap, condMap, callFilter);
        // Retrieve min./max ranks per gene if info requested
        // and if the main expression call query will not allow to obtain this information
        Map<Gene, EntityMinMaxRanks<Gene>> geneMinMaxRanks = loadMinMaxRanksPerGene(
                clonedAttrs, clonedOrderingAttrs, condParamCombination, geneMap, callFilter);


        //All necessary information ready, retrieve ExpressionCalls
        return log.exit(loadExpressionCallStream(callFilter, clonedAttrs, clonedOrderingAttrs,
                condParamCombination, geneMap, condMap, anatEntityMinMaxRanks, geneMinMaxRanks));
    }

    public Stream<DiffExpressionCall> loadDiffExpressionCalls(Integer speciesId, 
            DiffExpressionCallFilter callFilter) {
        log.entry(speciesId, callFilter);
        throw log.throwing(new UnsupportedOperationException("Load of diff. expression calls not implemented yet"));
    }

    /**
     * Load {@code ExpressionCall}s with {@code Condition} having both an {@code AnatEntity} and a {@code DevStage}
     * grouped by AnatEntity for one {@code Gene}. The {@code GeneFilter} provided as argument
     * must target only one gene, otherwise an {@code IllegalArgumentException} is thrown.
     * Retrieve only {@code ExpressionCall}s that have at least a SILVER {@code SummaryQuality} for a given
     * {@code AnatEntity} AND at least a BRONZE {@code SummaryQuality} for the same {@code AnatEntity}
     * and a {@code DevStage}.
     * These {@code ExpressionCall}s are filtered and ordered by rank using 
     * {@link ExpressionCall#filterAndOrderCallsByRank(Collection, ConditionGraph)}
     * 
     * @param geneFilter    A {@code GeneFilter} targeting a <strong>single gene</strong>
     *                      for which {@code ExpressionCall}s have to be retrieved
     * @return              The {@code LinkedHashMap} where values correspond to the {@code List} of
     *                      {@code ExpressionCall} and keys correspond to the {@code AnatEntity}
     * @throws IllegalArgumentException If {@code geneFilter} targets not one and only one gene.
     */
    public LinkedHashMap<AnatEntity, List<ExpressionCall>>
    loadCondCallsWithSilverAnatEntityCallsByAnatEntity(GeneFilter geneFilter) throws IllegalArgumentException {
        log.entry(geneFilter);
        return log.exit(this.loadCondCallsWithSilverAnatEntityCallsByAnatEntity(geneFilter, null));
    }
    /**
     * Same method as {@link #loadCondCallsWithSilverAnatEntityCallsByAnatEntity(GeneFilter)},
     * but with an already computed {@code ConditionGraph} provided. This method is used for cases
     * where computations for several genes will be requested independently, to avoid the need of creating
     * a {@code ConditionGraph} for each gene. If provided, the {@code Condition}s
     * in the {@code ConditionGraph} should have the same condition parameters as the {@code Condition}s
     * in the returned {@code ExpressionCall}s.
     * 
     * @param geneFilter    A {@code GeneFilter} targeting a <strong>single gene</strong>
     *                      for which {@code ExpressionCall}s have to be retrieved.
     * @param condGraph     A {@code ConditionGraph} for the species the requested gene belongs to,
     *                      that will be used to order calls and filter redundant calls.
     *                      Can be {@code null} if the {@code ConditionGraph} needs to be computed
     *                      by this method.
     * @return              The {@code LinkedHashMap} where values correspond to the {@code List} of
     *                      {@code ExpressionCall} and keys correspond to the {@code AnatEntity}
     * @throws IllegalArgumentException If {@code geneFilter} targets not one and only one gene.
     */
    public LinkedHashMap<AnatEntity, List<ExpressionCall>>
    loadCondCallsWithSilverAnatEntityCallsByAnatEntity(GeneFilter geneFilter, ConditionGraph condGraph)
            throws IllegalArgumentException {
        log.entry(geneFilter, condGraph);
        if (geneFilter.getEnsemblGeneIds().size() != 1) {
            throw log.throwing(new IllegalArgumentException("GeneFilter not targeting only one gene"));
        }

        Map<SummaryCallType.ExpressionSummary, SummaryQuality> silverExpressedCallFilter = new HashMap<>();
        silverExpressedCallFilter.put(ExpressionSummary.EXPRESSED, SummaryQuality.SILVER);
        Map<CallType.Expression, Boolean> obsDataFilter = new HashMap<>();
        obsDataFilter.put(null, true);
        
        // Load silver organ calls
        List<ExpressionCall> organCalls = this
                .loadExpressionCalls(
                        new ExpressionCallFilter(silverExpressedCallFilter,
                                Collections.singleton(geneFilter),
                                null, null, obsDataFilter, null, null),
                        EnumSet.of(CallService.Attribute.GENE, CallService.Attribute.ANAT_ENTITY_ID,
                                // We don't need the rank here, because we use the min rank of the conditions
                                // for each anat. entity, rather than the mean rank for the anat. entity
                                CallService.Attribute.DATA_QUALITY),
                        null)
                .collect(Collectors.toList());
        if (organCalls.isEmpty()) {
            log.debug("No calls for gene {}", geneFilter.getEnsemblGeneIds().iterator().next());
            return log.exit(new LinkedHashMap<>());
        }

        Map<SummaryCallType.ExpressionSummary, SummaryQuality> summaryCallTypeQualityFilter = new HashMap<>();
        summaryCallTypeQualityFilter.put(ExpressionSummary.EXPRESSED, SummaryQuality.BRONZE);
        LinkedHashMap<CallService.OrderingAttribute, Service.Direction> serviceOrdering = new LinkedHashMap<>();
        // The ordering is not essential here, because anyway we will need to
        // order calls
        // with an equal rank, based on the relations between their conditions,
        // which is difficult
        // to make in a query to the data source.
        // XXX: test if there is a performance difference if we don't use the
        // order by
        serviceOrdering.put(CallService.OrderingAttribute.GLOBAL_RANK, Service.Direction.ASC);
       
        // Load bronze organ-stage calls
        final List<ExpressionCall> organStageCalls = this
                .loadExpressionCalls(
                        new ExpressionCallFilter(summaryCallTypeQualityFilter,
                                Collections.singleton(geneFilter),
                                null, null, obsDataFilter, null, null),
                        EnumSet.of(CallService.Attribute.GENE, CallService.Attribute.ANAT_ENTITY_ID,
                                CallService.Attribute.DEV_STAGE_ID,
                                CallService.Attribute.DATA_QUALITY, CallService.Attribute.MEAN_RANK,
                                //We need the EXPERIMENT_COUNTS to know which data types produced calls
                                CallService.Attribute.EXPERIMENT_COUNTS, CallService.Attribute.CALL_TYPE,
                                //Retrieve qualitative expression levels relative to gene,
                                //this doesn't add any additional queries, since we retrieved all necessary calls
                                //to perform the computation
                                CallService.Attribute.GENE_QUAL_EXPR_LEVEL),
                        serviceOrdering)
                .collect(Collectors.toList());
        
        return log.exit(this.loadCondCallsWithSilverAnatEntityCallsByAnatEntity(
                organCalls, organStageCalls, true, condGraph));
    }

    /**
     * Same methods as {@link #loadCondCallsWithSilverAnatEntityCallsByAnatEntity(GeneFilter)}
     * but with the {@code ExpressionCall}s already retrieved. This is a helper method
     * for cases where the {@code ExpressionCall}s have already been retrieved
     * for other purpose. If {@code callsFiltered} is {@code false}, the calls will be filtered
     * to keep only the {@code organCalls} with "expressed" calls and quality higher than "bronze",
     * and only the {@code organStageCalls} with "expressed" calls (with any quality).
     * <p>
     * An already computed {@code ConditionGraph} can be provided, for cases where computations
     * for several genes will be requested independently, to avoid the need of creating
     * a {@code ConditionGraph} for each gene. A {@code null} {@code ConditionGraph} can be provided
     * if it is requested to be computed by this method. If provided, the {@code Condition}s
     * in the {@code ConditionGraph} should have the same condition parameters as the {@code Condition}s
     * in the {@code ExpressionCall}s in {@code conditionCalls}.
     *
     * @param organCalls        A {@code Collection} of {@code ExpressionCall} with {@code Condition}s
     *                          considering only the anat. entities. They must contain information
     *                          of expression state, quality, and rank, otherwise an
     *                          {@code IllegalArgumentException} is thrown.
     * @param organStageCalls   A {@code List} of {@code ExpressionCall} with {@code Condition}s
     *                          considering all parameters, ordered by ranks. They must contain information
     *                          of expression state, quality, and rank, otherwise an
     *                          {@code IllegalArgumentException} is thrown.
     * @param callsFiltered     A {@code Boolean} that should be {@code true} if the calls
     *                          were already filtered for the appropriate expression status
     *                          and qualities, {@code false} if the calls need to be filtered.
     * @param condGraph         A {@code ConditionGraph} for the species the requested gene belongs to,
     *                          that will be used to order calls and filter redundant calls.
     *                          Can be {@code null} if the {@code ConditionGraph} needs to be computed
     *                          by this method.
     * @return                  The {@code LinkedHashMap} where values correspond to the {@code List} of
     *                          condition {@code ExpressionCall} and keys correspond to the {@code AnatEntity}
     * @throws IllegalArgumentException If the {@code ExpressionCall}s do not contain the information
     *                                  needed when filtering them ({@code callsFiltered} set to {@code false})
     */
    public LinkedHashMap<AnatEntity, List<ExpressionCall>>
    loadCondCallsWithSilverAnatEntityCallsByAnatEntity(Collection<ExpressionCall> organCalls,
            List<ExpressionCall> conditionCalls, boolean callsFiltered, ConditionGraph condGraph)
                    throws IllegalArgumentException {
        log.entry(organCalls, conditionCalls, callsFiltered, condGraph);

        Collection<ExpressionCall> filteredOrganCalls = organCalls;
        if (!callsFiltered) {
            filteredOrganCalls = organCalls.stream()
                .filter(c -> {
                    if (c.getSummaryCallType() == null) {
                        throw log.throwing(new IllegalArgumentException(
                                "The provided calls do not have SummaryCallType"));
                    }
                    if (c.getSummaryQuality() == null) {
                        throw log.throwing(new IllegalArgumentException(
                                "The provided calls do not have SummaryQuality"));
                    }
                    return c.getSummaryCallType().equals(ExpressionSummary.EXPRESSED) &&
                            !c.getSummaryQuality().equals(SummaryQuality.BRONZE);
                })
                .collect(Collectors.toSet());
        }
        List<ExpressionCall> filteredConditionCalls = conditionCalls;
        if (!callsFiltered) {
            filteredConditionCalls = conditionCalls.stream()
                    .filter(c -> {
                        if (c.getSummaryCallType() == null) {
                            throw log.throwing(new IllegalArgumentException(
                                    "The provided calls do not have SummaryCallType"));
                        }
                        if (c.getMeanRank() == null) {
                            throw log.throwing(new IllegalArgumentException(
                                    "The provided calls do not have rank info"));
                        }
                        return c.getSummaryCallType().equals(ExpressionSummary.EXPRESSED);
                    })
                    .collect(Collectors.toList());
        }
        final Set<String> organIds = filteredOrganCalls.stream().map(c -> c.getCondition().getAnatEntityId())
                .collect(Collectors.toSet());
        // XXX: maybe refactor the code differently to have the organIds to perform the SQL query
        // retrieving the condition calls
        List<ExpressionCall> orderedCalls = filteredConditionCalls.stream()
                .filter(c -> organIds.contains(c.getCondition().getAnatEntityId())).collect(Collectors.toList());
        if (orderedCalls.isEmpty()) {
            log.debug("No condition calls for gene");
            return log.exit(new LinkedHashMap<>());
        }
        
        //we need to make sure that the ExpressionCalls are ordered in exactly the same way
        //for the display and for the clustering, otherwise the display will be buggy,
        //notably for calls with equal ranks. And we need to take into account
        //relations between Conditions for filtering them, which would be difficult to achieve
        //only by a query to the data source. So, we order them anyway.
        
        //ORGAN-STAGE
        ConditionGraph conditionGraph = condGraph;
        if (condGraph == null) {
            conditionGraph = this.getServiceFactory().getConditionGraphService().loadConditionGraph(
                orderedCalls.stream().map(ExpressionCall::getCondition).collect(Collectors.toSet()));
        }
        orderedCalls = ExpressionCall.filterAndOrderCallsByRank(orderedCalls, conditionGraph);
        
        //REDUNDANT ORGAN-STAGE CALLS
        final Set<ExpressionCall> redundantCalls = ExpressionCall.identifyRedundantCalls(
                orderedCalls, conditionGraph);
        
        //*********************
        // Grouping
        //*********************
        LinkedHashMap<AnatEntity, List<ExpressionCall>> callsByAnatEntity =
                groupByAnatEntAndFilterCalls(orderedCalls, redundantCalls, true);
        return log.exit(callsByAnatEntity);
    }

    //XXX: should the loadSingleSpeciesExprAnalysis methods moved to a new service?
    //Maybe a dedicated service for gene expression comparisons, bith single and multi-species?
    //The fact that a partial mock using 'spy' was used for unit testing is a code smell.
    //XXX: Actually I don't think we need SingleSpeciesExprAnalysis and MultiSpeciesExprAnalysis,
    //MultiGeneExprAnalysis seems enough with generic type. Which is a stronger case
    //to use a separate, dedicated service.
    public SingleSpeciesExprAnalysis loadSingleSpeciesExprAnalysis(Collection<Gene> requestedGenes) {
        log.entry(requestedGenes);
        if (requestedGenes == null || requestedGenes.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("Some genes must be provided"));
        }
        Set<Gene> clonedGenes = new HashSet<>(requestedGenes);

        Set<GeneFilter> geneFilters = convertGenesToGeneFilters(clonedGenes);
        ExpressionCallFilter callFilter = new ExpressionCallFilter(
                null,                              //we want both present and absent calls, of any quality
                geneFilters,                       //requested genes
                null,                              //any condition
                null,                              //any data type
                null, null, null                   //both observed and propagated calls
                );
        return log.exit(this.loadSingleSpeciesExprAnalysis(callFilter, clonedGenes));
    }
    public SingleSpeciesExprAnalysis loadSingleSpeciesExprAnalysis(ExpressionCallFilter callFilter) {
        log.entry(callFilter);
        if (callFilter.getGeneFilters().isEmpty()) {
            throw log.throwing(new IllegalArgumentException("A GeneFilter must be provided"));
        }
        Set<Gene> genes = this.getServiceFactory().getGeneService().loadGenes(callFilter.getGeneFilters())
                .collect(Collectors.toSet());
        return log.exit(this.loadSingleSpeciesExprAnalysis(callFilter, genes));
    }
    private SingleSpeciesExprAnalysis loadSingleSpeciesExprAnalysis(ExpressionCallFilter callFilter,
            Set<Gene> genes) {
        log.entry(callFilter, genes);
        if (callFilter.getGeneFilters().size() != 1) {
            throw log.throwing(new IllegalArgumentException(
                    "This method is for comparing the expression of genes in a single species"));
        }
        Set<Attribute> attributes = EnumSet.of(Attribute.GENE, Attribute.ANAT_ENTITY_ID,
                Attribute.CALL_TYPE, Attribute.DATA_QUALITY, Attribute.OBSERVED_DATA, Attribute.MEAN_RANK);
        LinkedHashMap<OrderingAttribute, Service.Direction> orderingAttributes = new LinkedHashMap<>();
        //IMPORTANT: results must be ordered by anat. entity so that we can compare expression
        //in each anat. entity without overloading the memory.
        orderingAttributes.put(OrderingAttribute.ANAT_ENTITY_ID, Service.Direction.ASC);


        Stream<ExpressionCall> callStream = this.loadExpressionCalls(callFilter, attributes, orderingAttributes);
        //We're going to group the calls per anat. entity, to be able to compare expression
        //of all genes in anat. entities
        Comparator<Condition> comp = Comparator.comparing(cond -> cond.getAnatEntityId());
        Stream<List<ExpressionCall>> callsByAnatEntity = StreamSupport.stream(
                new ElementGroupFromListSpliterator<>(callStream, ExpressionCall::getCondition, comp),
                false);
        Map<Condition, MultiGeneExprCounts> condToCounts = callsByAnatEntity
        //We keep only conditions where at least one gene has observed data in it
        .filter(list -> list.stream()
                .anyMatch(c -> Boolean.TRUE.equals(c.getDataPropagation().isIncludingObservedData())))
        //Now we create for each Condition an Entry<Condition, MultiGeneExprCounts>
        .map(list -> {
            Map<ExpressionSummary, Collection<Gene>> callTypeToGenes = list.stream()
                    .collect(Collectors.toMap(
                            c -> c.getSummaryCallType(),
                            c -> new HashSet<>(Arrays.asList(c.getGene())),
                            (v1, v2) -> {v1.addAll(v2); return v1;}));
            //Store rank info for each Gene with data
            Map<Gene, BigDecimal> geneToRank = list.stream()
            //Collectors.toMap does not accept null values,
            //see https://stackoverflow.com/a/24634007/1768736
            .collect(HashMap::new, (m, v) -> m.put(v.getGene(), v.getMeanRank()), Map::putAll);
            Set<Gene> genesWithNoData = new HashSet<>(genes);
            genesWithNoData.removeAll(geneToRank.keySet());
            return new AbstractMap.SimpleEntry<>(list.iterator().next().getCondition(),
                    new MultiGeneExprCounts(callTypeToGenes, genesWithNoData, geneToRank));
        })
        //And we create the final Map condToCounts
        .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));

        return log.exit(new SingleSpeciesExprAnalysis(genes, condToCounts));
    }

    //*************************************************************************
    // METHODS PERFORMING THE QUERIES TO THE DAOs
    //*************************************************************************

    private Map<AnatEntity, EntityMinMaxRanks<AnatEntity>> loadMinMaxRanksPerAnatEntity(
            Set<Attribute> attrs, LinkedHashMap<OrderingAttribute, Service.Direction> orderingAttrs,
            Set<ConditionDAO.Attribute> condParamCombination, Map<Integer, Gene> geneMap,
            Map<Integer, Condition> condMap, ExpressionCallFilter callFilter) {
        log.entry(attrs, orderingAttrs, condParamCombination, geneMap, condMap, callFilter);

        if (//qualitative expression levels relative to anat. entities not requested
            (!attrs.isEmpty() && !attrs.contains(Attribute.ANAT_ENTITY_QUAL_EXPR_LEVEL)) ||
            //or the main expression call query will allow to obtain the min.max ranks per anat. entity
            isQueryAllowingToComputeAnatEntityQualExprLevel(callFilter, condParamCombination,
                    attrs, orderingAttrs)) {
            //No need to query min./max ranks then
            return log.exit(new HashMap<>());
        }

        //Query to retrieve min./max ranks
        //We regenerate a new ExpressionCallFilter for properly performing the query
        ExpressionCallFilter newFilter = new ExpressionCallFilter(
                CALL_TYPE_QUAL_FOR_RANKS_FILTER,
                //new GeneFilters, we need to retrieve data for all genes of the requested species
                callFilter.getGeneFilters().stream().map(gf -> new GeneFilter(gf.getSpeciesId()))
                .collect(Collectors.toSet()),
                //new ConditionFilters, we need to retrieve data for all dev. stages and not for
                //non-observed conditions.
                //We-re happy to keep a filtering based on anat. entity IDs though
                callFilter.getConditionFilters().stream()
                .map(cf -> new ConditionFilter(cf.getAnatEntityIds(), null))
                .collect(Collectors.toSet()),
                //we keep the same data types as requested
                callFilter.getDataTypeFilters(),
                //only call observed data, since as of Bgee 14 ranks are computed
                //only for observed data
                OBSERVED_DATA_FOR_RANKS_FILTER,
                //then we don't care about anat. entity/dev. stage observed data specifically
                null, null);
        //convert ExpressionCallFilter into CallDAOFilter
        CallDAOFilter daoFilter = convertCallFilterToCallDAOFilter(geneMap, newFilter,
                condParamCombination);
        log.trace("CallDAOFilter produced: {}", daoFilter);
        //Create a Map from anat. entity ID to AnatEntity from the condMap
        final Map<String, AnatEntity> idToAnatEntity = Collections.unmodifiableMap(
                condMap.values().stream()
                .map(c -> new AbstractMap.SimpleEntry<>(c.getAnatEntityId(), c.getAnatEntity()))
                .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue(),
                        (v1, v2) -> v1)));
        log.trace("Map ID to AnatEntity produced: {}", idToAnatEntity);

        //Perform query and map TOs to EntityMinMaxRanks
        return log.exit(this.globalExprCallDAO.getMinMaxRanksPerAnatEntity(Arrays.asList(daoFilter),
                condParamCombination).stream()
        .map(minMaxRanksTO -> new EntityMinMaxRanks<AnatEntity>(
                minMaxRanksTO.getMinRank(), minMaxRanksTO.getMaxRank(),
                Optional.ofNullable(idToAnatEntity.get(minMaxRanksTO.getId()))
                    .orElseThrow(() -> new IllegalStateException(
                            "Missing AnatEntity for ID " + minMaxRanksTO.getId()))))
        .collect(Collectors.toMap(emmr -> emmr.getEntityConsidered(), emmr -> emmr)));
    }

    private Map<Gene, EntityMinMaxRanks<Gene>> loadMinMaxRanksPerGene(
            Set<Attribute> attrs, LinkedHashMap<OrderingAttribute, Service.Direction> orderingAttrs,
            Set<ConditionDAO.Attribute> condParamCombination, Map<Integer, Gene> geneMap,
            ExpressionCallFilter callFilter) {
        log.entry(attrs, orderingAttrs, condParamCombination, geneMap, callFilter);

        if (//qualitative expression levels relative to genes not requested
            (!attrs.isEmpty() && !attrs.contains(Attribute.GENE_QUAL_EXPR_LEVEL)) ||
            //or the main expression call query will allow to obtain the min.max ranks per gene
            isQueryAllowingToComputeGeneQualExprLevel(callFilter, condParamCombination,
                    attrs, orderingAttrs)) {
            //No need to query min./max ranks then
            return log.exit(new HashMap<>());
        }

        //Query to retrieve min./max ranks
        //We regenerate a new ExpressionCallFilter for properly performing the query
        ExpressionCallFilter newFilter = new ExpressionCallFilter(
                CALL_TYPE_QUAL_FOR_RANKS_FILTER,
                //Use the same GeneFilters
                callFilter.getGeneFilters(),
                //new ConditionFilters, we need to retrieve data for all dev. stages and all
                //anat. entites and not for non-observed conditions.
                null,
                //we keep the same data types as requested
                callFilter.getDataTypeFilters(),
                //only call observed data, since as of Bgee 14 ranks are computed
                //only for observed data
                OBSERVED_DATA_FOR_RANKS_FILTER,
                //then we don't care about anat. entity/dev. stage observed data specifically
                null, null);
        //convert ExpressionCallFilter into CallDAOFilter
        CallDAOFilter daoFilter = convertCallFilterToCallDAOFilter(geneMap, newFilter,
                condParamCombination);

        //Perform query and map TOs to EntityMinMaxRanks
        return log.exit(this.globalExprCallDAO.getMinMaxRanksPerGene(Arrays.asList(daoFilter),
                condParamCombination).stream()
        .map(minMaxRanksTO -> new EntityMinMaxRanks<Gene>(
                minMaxRanksTO.getMinRank(), minMaxRanksTO.getMaxRank(),
                Optional.ofNullable(geneMap.get(minMaxRanksTO.getId()))
                    .orElseThrow(() -> new IllegalStateException(
                            "Missing Gene for ID: " + minMaxRanksTO.getId()))))
        .collect(Collectors.toMap(emmr -> emmr.getEntityConsidered(), emmr -> emmr)));
    }

    /**
     * Perform query to retrieve expressed calls without the post-processing of
     * the results returned by {@code DAO}s.
     *
     * @param geneMap               A {@code Map} where keys are {@code Integer}s representing Bgee gene IDs,
     *                              the associated value being the corresponding {@code Gene}. Note that this {@code Map}
     *                              must be consistent with the {@code GeneFilter}s provided in {@code callFilter}
     *                              (by using, for instance, the method {@link #loadGeneMap(CallFilter, Map)}).
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

        //TODO: retrieve sub-structures and sub-stages depending on ConditionFilter
        Stream<GlobalExpressionCallTO> calls = this.globalExprCallDAO
            .getGlobalExpressionCalls(Arrays.asList(
                //generate an ExpressionCallDAOFilter from callFilter
                convertCallFilterToCallDAOFilter(geneMap, callFilter, condParamCombination)),
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
    // HELPER METHODS
    //*************************************************************************

    private Stream<ExpressionCall> loadExpressionCallStream(ExpressionCallFilter callFilter,
            Set<Attribute> attrs, LinkedHashMap<OrderingAttribute, Service.Direction> orderingAttrs,
            Set<ConditionDAO.Attribute> condParamCombination,
            final Map<Integer, Gene> geneMap, final Map<Integer, Condition> condMap,
            Map<AnatEntity, EntityMinMaxRanks<AnatEntity>> anatEntityMinMaxRanks,
            Map<Gene, EntityMinMaxRanks<Gene>> geneMinMaxRanks) {
        log.entry(callFilter, attrs, orderingAttrs, condParamCombination, geneMap, condMap,
                anatEntityMinMaxRanks, geneMinMaxRanks);

        // Retrieve the Stream<GlobalExpressionCallTO>
        Stream<GlobalExpressionCallTO> toStream = this.performsGlobalExprCallQuery(geneMap, callFilter, condParamCombination,
                attrs, orderingAttrs);

        //Intermediary step in case some min./max ranks are necessary
        //but can be retrieved through the main expression call query.
        //In that case, we will map to a new Stream after the intermediate steps.
        if (attrs.isEmpty() || attrs.contains(Attribute.ANAT_ENTITY_QUAL_EXPR_LEVEL) ||
                attrs.contains(Attribute.GENE_QUAL_EXPR_LEVEL)) {

            //better to check this way than by checking whether anatEntityMinMaxRanks is empty
            //in case there is no matching expression calls
            boolean computeAnatEntityMinMax = isQueryAllowingToComputeAnatEntityQualExprLevel(callFilter,
                    condParamCombination, attrs, orderingAttrs);
            //better to check this way than by checking whether geneMinMaxRanks is empty
            //in case there is no matching expression calls
            boolean computeGeneMinMax = isQueryAllowingToComputeGeneQualExprLevel(callFilter,
                    condParamCombination, attrs, orderingAttrs);
            assert !(computeAnatEntityMinMax && computeGeneMinMax);

            //Remap to a new Stream that will group calls with same anat. entity or with same gene,
            //depending on what can be computed from the calls
            Stream<List<GlobalExpressionCallTO>> toListStream = null;
            if (computeAnatEntityMinMax) {
                assert anatEntityMinMaxRanks.isEmpty();
                toListStream = StreamSupport.stream(
                        new ElementGroupFromListSpliterator<GlobalExpressionCallTO, String>(
                                toStream,
                                callTO -> condMap.get(callTO.getConditionId()).getAnatEntityId(),
                                (id1, id2) -> id1.compareTo(id2)),
                        false);
            } else if (computeGeneMinMax) {
                assert geneMinMaxRanks.isEmpty();
                toListStream = StreamSupport.stream(
                        new ElementGroupFromListSpliterator<GlobalExpressionCallTO, Integer>(
                                toStream,
                                callTO -> callTO.getBgeeGeneId(),
                                (id1, id2) -> id1.compareTo(id2)),
                        false);
            }

            //If indeed it is possible to compute min./max ranks from the list of calls
            if (toListStream != null) {
                return log.exit(toListStream.flatMap(toList -> {
                    List<ExpressionCall> intermediateCalls = toList.stream()
                            //To retrieve the min./max ranks, we need to know the SummaryCall,
                            //so, rather than computing CallData and SumaryCall several times,
                            //we directly create ExpressionCalls
                            .map(to -> mapGlobalCallTOToExpressionCall(to, geneMap, condMap, callFilter,
                                    null, null, attrs))
                            .collect(Collectors.toList());
    
                    if (intermediateCalls.isEmpty()) {
                        return intermediateCalls.stream();
                    }
                    //Compute the anatEntityMinMaxRank from this List of ExpressionCalls if possible
                    EntityMinMaxRanks<AnatEntity> anatEntityMinMaxRank = computeAnatEntityMinMax?
                            getMinMaxRanksFromCallGroup(intermediateCalls,
                                    call -> call.getCondition().getAnatEntity()):
                            null;
                    //Compute the geneMinMaxRank from this List of ExpressionCalls if possible
                    EntityMinMaxRanks<Gene> geneMinMaxRank = computeGeneMinMax?
                            getMinMaxRanksFromCallGroup(intermediateCalls,
                                    call -> call.getGene()):
                            null;
                    //Produce a new ExpressionCall using the information of min./max ranks
                    //we just computed, and/or retrieved from the database through some previous
                    //independent queries
                    return intermediateCalls.stream().map(c -> {
                        log.trace("Intermediate call iterated: {}", c);
                        return new ExpressionCall(
                            c.getGene(),
                            c.getCondition(),
                            c.getDataPropagation(),
                            c.getSummaryCallType(),
                            c.getSummaryQuality(),
                            c.getCallData(),
                            loadExpressionLevelInfo(c.getSummaryCallType(), c.getMeanRank(),
                                    anatEntityMinMaxRank != null? anatEntityMinMaxRank:
                                        anatEntityMinMaxRanks.get(c.getCondition().getAnatEntity()),
                                    geneMinMaxRank != null? geneMinMaxRank:
                                        geneMinMaxRanks.get(c.getGene()))
                            );});
                }));
            }
        }

        //The information of min.max ranks was not requested, or it was not possible
        //to compute some min.max ranks from the retrieved ExpressionCalls
        //(information then already retrieved through previous independent queries,
        //with results provided through arguments anatEntityMinMaxRanks and geneMinMaxRanks).
        //We thus don't use intermediate steps to create the ExpressionCalls.
        return log.exit(toStream
            .map(to -> mapGlobalCallTOToExpressionCall(to, geneMap, condMap, callFilter,
                    anatEntityMinMaxRanks, geneMinMaxRanks, attrs)));
    }

    /**
     * Remove redundant calls from a {@code List} of {@code ExpressionCall}s and retrieve 
     * a {@code LinkedHashMap} where keys correspond to {@code AnatEntity}s 
     * and values correspond to the associated {@code List} of {@code ExpressionCall}s.
     * 
     * @param orderedCalls          A {@code List} of {@code ExpressionCalls} that have been previously sorted.
     * @param redundantCalls        A {@code List} of {@code ExpressionCalls} to remove from orderedCalls.
     * @param filterRedundantCalls  A {@code boolean} to define if redundantCalls have to be removed.
     * @return                      The {@code LinkedHashMap} containing of {@code List} of 
     *                              {@code ExpressionCall}s grouped by {@code AnatEntity}
     */
    private static LinkedHashMap<AnatEntity, List<ExpressionCall>> groupByAnatEntAndFilterCalls(
            List<ExpressionCall> orderedCalls, Set<ExpressionCall> redundantCalls, 
            boolean filterRedundantCalls) {
        log.entry(orderedCalls, redundantCalls, filterRedundantCalls);
        //first, filter calls and group calls by anat. entity. We need to preserve the order 
        //of the keys, as we have already sorted the calls by their rank. 
        //If filterRedundantCalls is true, we completely discard anat. entities 
        //that have only redundant calls, but if an anat. entity has some non-redundant calls 
        //and is not discarded, we preserve all its calls, even the redundant ones. 
        return log.exit(orderedCalls.stream()
                //group by anat. entity
                .collect(Collectors.groupingBy(
                        c -> c.getCondition().getAnatEntity(), 
                        LinkedHashMap::new, 
                        Collectors.toList()))
                .entrySet().stream()
                //discard if all calls of an anat. entity are redundant
                .filter(entry -> !filterRedundantCalls || !redundantCalls.containsAll(entry.getValue()))
                //reconstruct the LinkedHashMap
                .collect(
                        //Type inference hint needed, this code was compiling fine in Eclipse,
                        //not with maven... See for instance
                        //https://stackoverflow.com/questions/48135796/java-8-inferred-type-does-not-conform-to-upper-bounds-on-netbean-ide
                        Collectors.<Entry<AnatEntity, List<ExpressionCall>>, AnatEntity,
                        List<ExpressionCall>, LinkedHashMap<AnatEntity, List<ExpressionCall>>>
                        toMap(e -> e.getKey(), e -> e.getValue(),
                        (l1, l2) -> {
                            throw log.throwing(new AssertionError("Not possible to have key collision"));
                        }, 
                        LinkedHashMap::new)));
    }

    private static boolean isQueryAllowingToComputeGeneQualExprLevel(ExpressionCallFilter callFilter,
            Set<ConditionDAO.Attribute> condParamCombination, Set<Attribute> attributes,
            LinkedHashMap<OrderingAttribute, Service.Direction> orderingAttributes) {
        log.entry(callFilter, condParamCombination, attributes, orderingAttributes);

        //Perform the checks for qualitative expression levels relative to any entity
        if (!isQueryAllowingToComputeAnyQualExprLevel(callFilter, condParamCombination, attributes)) {
            return log.exit(false);
        }
        //Now, we do only the remaining checks for qualitative expression levels relative to genes

        //Obviously we need the gene info
        if (!attributes.contains(Attribute.GENE)) {
            return log.exit(false);
        }

        //We would also need the results to be retrieved ordered by gene IDs first,
        //unless only one gene is requested
        //Will work if the query is done on one species only, otherwise we can have a same Ensemble Gene ID
        //linked to different species, when we use the genome of a closely related species
        //(part of the checks done in isQueryAllowingToComputeAnyQualExprLevel)
        if ((orderingAttributes.isEmpty() ||
                !orderingAttributes.keySet().iterator().next().equals(OrderingAttribute.GENE_ID)) &&
                (callFilter.getGeneFilters().size() != 1 ||
                callFilter.getGeneFilters().iterator().next().getEnsemblGeneIds().size() != 1)) {
            return log.exit(false);
        }

        //We would need the query to retrieve expression calls in any anat. entity-stage.
        //And not discarding observed conditions or calls, to compute the expression level categories
        //(part of the checks done in isQueryAllowingToComputeAnyQualExprLevel)
        if ((callFilter.getConditionFilters() != null && callFilter.getConditionFilters().stream()
                .anyMatch(cf -> !cf.getAnatEntityIds().isEmpty()))) {
            return log.exit(false);
        }

        return log.exit(true);
    }

    private static boolean isQueryAllowingToComputeAnatEntityQualExprLevel(ExpressionCallFilter callFilter,
            Set<ConditionDAO.Attribute> condParamCombination, Set<Attribute> attributes,
            LinkedHashMap<OrderingAttribute, Service.Direction> orderingAttributes) {
        log.entry(callFilter, condParamCombination, attributes, orderingAttributes);

        //Perform the checks for qualitative expression levels relative to any entity
        if (!isQueryAllowingToComputeAnyQualExprLevel(callFilter, condParamCombination, attributes)) {
            return log.exit(false);
        }
        //Now, we do only the remaining checks for qualitative expression levels relative to anat. entities

        //Obviously we need the anat. entity info
        if (!attributes.contains(Attribute.ANAT_ENTITY_ID)) {
            return log.exit(false);
        }

        //We would also need the results to be retrieved ordered by anat. entity IDs first,
        //unless only one anat. entity is requested
        //Will work if the query is done on one species only, otherwise we can have a same anat. entity ID
        //linked to different species (part of the checks done in isQueryAllowingToComputeAnyQualExprLevel)
        if ((orderingAttributes.isEmpty() ||
                !orderingAttributes.keySet().iterator().next().equals(OrderingAttribute.ANAT_ENTITY_ID)) &&
                (callFilter.getConditionFilters().isEmpty() ||
                        callFilter.getConditionFilters().stream().anyMatch(cf -> cf.getAnatEntityIds().size() != 1) ||
                        callFilter.getConditionFilters().stream().flatMap(cf -> cf.getAnatEntityIds().stream())
                                .collect(Collectors.toSet()).size() != 1)) {
            return log.exit(false);
        }

        //We would need the query to retrieve expression calls in any dev. stage and for any gene,
        //not discarding observed conditions or calls, to compute the expression level categories
        //(part of the checks done in isQueryAllowingToComputeAnyQualExprLevel)
        if (callFilter.getGeneFilters().stream().anyMatch(gf -> !gf.getEnsemblGeneIds().isEmpty())) {
            return log.exit(false);
        }

        return log.exit(true);
    }

    private static boolean isQueryAllowingToComputeAnyQualExprLevel(ExpressionCallFilter callFilter,
            Set<ConditionDAO.Attribute> condParamCombination, Set<Attribute> attributes) {
        log.entry(callFilter, condParamCombination, attributes);

        //If ranks not requested, we can't do anything
        if (!attributes.contains(Attribute.MEAN_RANK)) {
            return log.exit(false);
        }

        //Since for now we use ranks only from calls with params GENE-ANAT_ENTITY-DEV_STAGE,
        //This is the condition parameter combination we need
        if (!condParamCombination.equals(COND_PARAM_COMBINATION_FOR_RANKS)) {
            return log.exit(false);
        }

        //We would need the query to retrieve calls of presence of expression of any quality
        SummaryQuality exprQual = callFilter.getSummaryCallTypeQualityFilter().get(SummaryCallType.ExpressionSummary.EXPRESSED);
        if (exprQual == null || !exprQual.equals(SummaryQuality.values()[0])) {
            return log.exit(false);
        }

        //We need calls to include observed data, from any call type (meaning, we need expressed calls from any quality,
        //from observed data, but the call can have been observed from absence of expression in the condition itself,
        //and still be an EXPRESSED call thanks to sub-conditions). So the CallObservedData parameter should not restrain
        //to calls having observed expression in the condition itself
        if ((!callFilter.getCallObservedData().containsKey(null) &&
                !callFilter.getCallObservedData().keySet().containsAll(EnumSet.allOf(CallType.Expression.class))) ||
            callFilter.getCallObservedData().values().stream().anyMatch(b -> Boolean.FALSE.equals(b))) {
            return log.exit(false);
        }
        //We can't use the main query if it was requested to obtain non-observed conditions,
        //or non-observed calls along anatomy of dev. stages
        if (callFilter.getConditionFilters().stream().anyMatch(cf -> Boolean.FALSE.equals(cf.getObservedConditions())) ||
                Boolean.FALSE.equals(callFilter.getAnatEntityObservedData()) ||
                Boolean.FALSE.equals(callFilter.getDevStageObservedData())) {
            return log.exit(false);
        }

        //We would also need the results to be retrieved ordered by gene/anat. entity IDs first,
        //unless one specific gene or one specific anat. entity was requested (checked in the calling methods).
        //Will work if the query is done on one species only, otherwise we can have a same Ensemble Gene ID
        //(when we use the genome of a closely related species), or same anat. entity ID,
        //linked to different species.
        Set<Integer> requestedSpeciesIds = callFilter.getGeneFilters().stream()
                .map(gf -> gf.getSpeciesId()).collect(Collectors.toSet());
        if (requestedSpeciesIds.size() != 1) {
            return log.exit(false);
        }

        //We need the query to retrieve expression calls at least in any stage
        if (callFilter.getConditionFilters().stream().anyMatch(cf -> !cf.getDevStageIds().isEmpty())) {
            return log.exit(false);
        }

        return log.exit(true);
    }

    private static <T> EntityMinMaxRanks<T> getMinMaxRanksFromCallGroup(
            Collection<ExpressionCall> calls, Function<ExpressionCall, T> extractEntityFun) {
        log.entry(calls, extractEntityFun);

        BigDecimal minRankExpressedCalls = null;
        BigDecimal maxRankExpressedCalls = null;
        T previousEntity = null;
        for (ExpressionCall call: calls) {
            T entity = extractEntityFun.apply(call);
            if (previousEntity != null && !previousEntity.equals(entity)) {
                throw log.throwing(new IllegalArgumentException(
                        "Calls do not have the same grouping criterion, previous entity: "
                        + previousEntity + " - current entity: " + entity));
            }
            //We assume that either we are seeing only EXPRESSED calls, and maybe the information
            //is not provided; or the information is provided, and we discard NOT_EXPRESSED calls
            if (ExpressionSummary.NOT_EXPRESSED.equals(call.getSummaryCallType())) {
                continue;
            }
            BigDecimal rank = call.getMeanRank();
            if (rank == null) {
                throw log.throwing(new IllegalArgumentException(
                        "The calls do not have all the required information, call seen: " + call));
            }
            //XXX: maybe we should assume that the Collection of ExpressionCall is a List ordered by ranks,
            //and simply get the first EXPRESSED calls and the last EXPRESSED calls?
            //we have to iterate all the list all the same.
            if (minRankExpressedCalls == null || rank.compareTo(minRankExpressedCalls) < 0) {
                minRankExpressedCalls = rank;
            }
            if (maxRankExpressedCalls == null || rank.compareTo(maxRankExpressedCalls) > 0) {
                maxRankExpressedCalls = rank;
            }
            previousEntity = entity;
        }
        //If there was only NOT_EXPRESSED calls, the min and max ranks will both be null
        return log.exit(new EntityMinMaxRanks<T>(minRankExpressedCalls, maxRankExpressedCalls,
                previousEntity));
    }

    private static ExpressionLevelInfo loadExpressionLevelInfo(ExpressionSummary exprSummary, BigDecimal rank,
            EntityMinMaxRanks<AnatEntity> anatEntityMinMaxRank, EntityMinMaxRanks<Gene> geneMinMaxRank) {
        log.entry(exprSummary, rank, anatEntityMinMaxRank, geneMinMaxRank);
        return log.exit(new ExpressionLevelInfo(rank,
                loadQualExprLevel(exprSummary, rank, geneMinMaxRank),
                loadQualExprLevel(exprSummary, rank, anatEntityMinMaxRank)));
    }
    private static <T> QualitativeExpressionLevel<T> loadQualExprLevel(ExpressionSummary exprSummary, BigDecimal rank,
            EntityMinMaxRanks<T> minMaxRanks) {
        log.entry(exprSummary, rank, minMaxRanks);
        if (ExpressionSummary.NOT_EXPRESSED.equals(exprSummary)) {
            return log.exit(new QualitativeExpressionLevel<>(ExpressionLevelCategory.ABSENT, minMaxRanks));
        }
        if (ExpressionSummary.EXPRESSED.equals(exprSummary) && rank != null && minMaxRanks != null) {
            return log.exit(new QualitativeExpressionLevel<>(
                    ExpressionLevelCategory.getExpressionLevelCategory(minMaxRanks, rank),
                    minMaxRanks));
        }
        return log.exit(null);
    }

    //*************************************************************************
    // HELPER METHODS CONVERTING INFORMATION FROM Call LAYER to DAO LAYER
    //*************************************************************************
    private static CallDAOFilter convertCallFilterToCallDAOFilter(Map<Integer, Gene> geneMap,
            ExpressionCallFilter callFilter, Set<ConditionDAO.Attribute> condParamCombination) {
        log.entry(geneMap, callFilter, condParamCombination);

        //we map each GeneFilter to Bgee gene IDs rather than Ensembl gene IDs.
        Set<Integer> geneIdFilter = null;
        Set<Integer> speciesIds = null;
        if (callFilter != null) {
            Entry<Set<Integer>, Set<Integer>> geneIdsSpeciesIds = convertGeneFiltersToBgeeGeneIdsAndSpeciesIds(
                    callFilter.getGeneFilters(), geneMap);
            geneIdFilter = geneIdsSpeciesIds.getKey();
            speciesIds = geneIdsSpeciesIds.getValue();
        }

        if ((speciesIds == null || speciesIds.isEmpty()) &&
                (geneIdFilter == null || geneIdFilter.isEmpty())) {
            throw log.throwing(new IllegalArgumentException(
                    "No species nor gene IDs retrieved for filtering results."));
        }

        return log.exit(new CallDAOFilter(
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
                    convertCallFilterToCallDataDAOFilters(callFilter, condParamCombination)
                ));
    }
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
            ExpressionCallFilter callFilter, Set<ConditionDAO.Attribute> condParamCombination) {
        log.entry(callFilter, condParamCombination);

        if (checkNoCallDataDAOFilterNeeded(callFilter)) {
            return log.exit(null);
        }

        Set<CallDataDAOFilter> callDataDAOFilters = new HashSet<>();
        //If there is no filtering on call type and quality, we will need only one CallDataDAOFilter
        if (checkAllCallTypesAllQualsRequested(callFilter)) {
            callDataDAOFilters.add(generateCallDataDAOFilter(callFilter, condParamCombination,
                            new HashSet<>()));
        //Otherwise, we need one CallDataDAOFilter for each CallType requested
        } else {
            callDataDAOFilters = callFilter.getSummaryCallTypeQualityFilter()
                .entrySet().stream().map(e -> {
                    SummaryCallType.ExpressionSummary requestedCallType = e.getKey();
                    SummaryQuality requestedQual = e.getValue();
                    
                    if (callFilter.getDataTypeFilters() != null && !callFilter.getDataTypeFilters().isEmpty() &&
                            Collections.disjoint(callFilter.getDataTypeFilters(), requestedCallType.getAllowedDataTypes())) {
                        throw log.throwing(new IllegalArgumentException(
                                "The data types selected do not allow to produce the requested call type. "
                                + "Call Type: " + requestedCallType + " - Data types: " + callFilter.getDataTypeFilters()));
                    }

                    return generateCallDataDAOFilter(callFilter, condParamCombination,
                            //Do not store the value returned by generateExprQualDAOCountFilters in a variable,
                            //because generateCallDataDAOFilter will modify it before using it.
                            generateExprQualDAOCountFilters(requestedCallType, requestedQual));
                })
                .collect(Collectors.toSet());
        }

        //the method should have exited right away if no filtering was necessary
        assert callDataDAOFilters != null && !callDataDAOFilters.isEmpty();
        return log.exit(callDataDAOFilters);
    }
    /**
     * Checks whether this {@code CallFilter} requires creating {@code CallDataDAOFilter}s.
     *
     * @param callFilter    The {@code ExpressionCallFilter} to check for creation of {@code CallDataDAOFilter}s.
     * @return              {@code true} if no {@code CallDataDAOFilter}s need to be created,
     *                      {@code false} otherwise.
     */
    private static boolean checkNoCallDataDAOFilterNeeded(ExpressionCallFilter callFilter) {
        log.entry(callFilter);

        //determine whether all data types were requested
        boolean allDataTypesSelected = callFilter.getDataTypeFilters() == null ||
                callFilter.getDataTypeFilters().isEmpty() ||
                callFilter.getDataTypeFilters().equals(EnumSet.allOf(DataType.class));
        //now check whether all observed data states were requested
        boolean allObservedStates = callFilter.getCallObservedData() == null ||
                callFilter.getCallObservedData().isEmpty() ||
                callFilter.getCallObservedData().values().stream().allMatch(v -> v == null);
        boolean allCondObservedData = callFilter.getAnatEntityObservedData() == null &&
                callFilter.getDevStageObservedData() == null;

        //absolutely no filtering necessary on experiment expression counts in following case.
        //Note: it is not possible to request no-expression calls from EST data,
        //but for convenience we do not consider this an error here, otherwise we could not provide
        //one ExpressionCallFilter to simply say: "give me all calls".
        return log.exit(checkAllCallTypesAllQualsRequested(callFilter) && allDataTypesSelected &&
                allObservedStates && allCondObservedData &&
                //this is true only as long as the minimum experiment count threshold is 1
                MIN_LOW_BRONZE <= 1 && MIN_HIGH_BRONZE <= 1);
    }
    private static boolean checkAllCallTypesAllQualsRequested(ExpressionCallFilter callFilter) {
        log.entry(callFilter);
        //Determine whether the lowest quality level was requested
        final SummaryQuality lowestQual = SummaryQuality.BRONZE;
        //Just to make sure that qualities are in proper order and haven't changed
        assert SummaryQuality.values()[0].equals(SummaryQuality.BRONZE);
        boolean lowestQualSelected = callFilter.getSummaryCallTypeQualityFilter() == null ||
                callFilter.getSummaryCallTypeQualityFilter().isEmpty() ||
                callFilter.getSummaryCallTypeQualityFilter().entrySet().stream()
                .allMatch(e -> e.getValue() == null || lowestQual.equals(e.getValue()));
        //now determine whether all CallTypes were requested
        boolean allCallTypesSelected = callFilter.getSummaryCallTypeQualityFilter() == null ||
                callFilter.getSummaryCallTypeQualityFilter().isEmpty() ||
                callFilter.getSummaryCallTypeQualityFilter().keySet().equals(EnumSet.allOf(SummaryCallType.ExpressionSummary.class));
        return log.exit(lowestQualSelected && allCallTypesSelected);
    }

    private static CallDataDAOFilter generateCallDataDAOFilter(ExpressionCallFilter callFilter,
            Set<ConditionDAO.Attribute> condParamCombination,
            Set<Set<DAOExperimentCountFilter>> daoExperimentCountFilters) {
        log.entry(callFilter, condParamCombination, daoExperimentCountFilters);

        final Set<DAODataType> daoDataTypes = Collections.unmodifiableSet(
                convertDataTypeToDAODataType(callFilter.getDataTypeFilters()));

        //Now we deal with getCallObservedData if filtering on specific call type was requested
        //(Observed data filter with a global null key are managed directly in the CallDAOFilter)
        Set<Set<DAOExperimentCountFilter>> observedDataFilters = new HashSet<>();
        //state of observation requested whatever the expression call type (key null in getCallObservedData).
        Boolean callObservedData = null;
        for (Entry<CallType.Expression, Boolean> obsFilter: callFilter.getCallObservedData().entrySet()) {
            //state of observation requested whatever the expression call type
            if (obsFilter.getKey() == null) {
                callObservedData = obsFilter.getValue();
            } else {
                //state of observation for specific expression types
                //(managed through DAOExperimentCountFilters)
                if (Boolean.TRUE.equals(obsFilter.getValue())) {
                    //self present/absent low > 0 OR self present/absent high > 0
                    //It thus go to the same inner Set
                    Set<DAOExperimentCountFilter> obsDataOrFilters = new HashSet<>();
                    for (DAOExperimentCount.DataQuality qual: DAOExperimentCount.DataQuality.values()) {
                        obsDataOrFilters.add(
                                new DAOExperimentCountFilter(
                                        convertCallTypeToDAOCallType(obsFilter.getKey()),
                                        qual,
                                        DAOPropagationState.SELF,
                                        DAOExperimentCountFilter.Qualifier.GREATER_THAN,
                                        0));
                    }
                    observedDataFilters.add(obsDataOrFilters);
                } else {
                    //self present/absent low = 0 AND self present/absent high = 0
                    //It thus go to different inner Set
                    for (DAOExperimentCount.DataQuality qual: DAOExperimentCount.DataQuality.values()) {
                        Set<DAOExperimentCountFilter> obsDataAndFilters = new HashSet<>();
                        obsDataAndFilters.add(
                                new DAOExperimentCountFilter(
                                        convertCallTypeToDAOCallType(obsFilter.getKey()),
                                        qual,
                                        DAOPropagationState.SELF,
                                        DAOExperimentCountFilter.Qualifier.EQUALS_TO,
                                        0));
                        observedDataFilters.add(obsDataAndFilters);
                    }
                }
            }
        }
        if (!observedDataFilters.isEmpty()) {
            //We shamelessly modify the arguments of this method, since it is a private method,
            //and the argument passed when calling this method is not stored previously in any variable.
            daoExperimentCountFilters.addAll(observedDataFilters);
        }


        return log.exit(new CallDataDAOFilter(daoExperimentCountFilters, daoDataTypes, callObservedData,
                convertCallFilterToDAOObservedDataFilter(callFilter, condParamCombination)));
    }

    private static Set<Set<DAOExperimentCountFilter>> generateExprQualDAOCountFilters(
            SummaryCallType.ExpressionSummary requestedCallType, SummaryQuality requestedQual) {
        log.entry(requestedCallType, requestedQual);

        //see org.bgee.model.dao.api.expressiondata.CallDataDAOFilter.getExperimentCountFilters()
        //for more details
        Set<Set<DAOExperimentCountFilter>> daoExperimentCountFilters = new HashSet<>();

        final Boolean isExpression;
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

        //reject expression if no-expression is requested, through "AND" filters
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

        //Bug fix (commenting of next lines): we do NOT discard improper data types to retrieve no-expression calls,
        //otherwise it would mess up the rejectExpressionFilters, and we could end up
        //retrieving, for instance, no-expression calls from Affymetrix data having expression calls
        //from EST data (and we do not want that, if EST data were selected to retrieve the no-expression calls,
        //despite being incorrect, we do not want to retrieve no-expression calls that are expressed
        //according to EST).
        //=> It means it is the DAO job to discard silently such improper fields
        //(see method org.bgee.model.dao.mysql.expressiondata.MySQLGlobalExpressionCallDAO.generateDataFilters(LinkedHashSet, String)) ).
        //Of note, if only EST data were requested for retrieving no-expression calls,
        //an exception would have been thrown by this method already.
//      Set<DAODataType> filteredDataTypes = daoDataTypes.stream()
//      // we do not keep filters requiring EST data and absence of expression
//          .filter(dt -> !(DAODataType.EST.equals(dt) && !isExpression))
//          .collect(Collectors.toSet());
//      if (filteredDataTypes.isEmpty()) {
//          throw log.throwing(new IllegalArgumentException(
//                  "Impossible to get not expressed calls for EST data only"));
//      }
//      return new CallDataDAOFilter(daoExperimentCountFilters, filteredDataTypes);

        return log.exit(daoExperimentCountFilters);
    }

    /**
     * This method manages only the "observed data" states per anatomical entity and/or dev. stages,
     * it does not manage the condition "observed data" states (see method
     * {@link #performsGlobalExprCallQuery(Map, ExpressionCallFilter, Set, Set, LinkedHashMap)}).
     * @param callFilter
     * @param condParamCombination
     * @return
     */
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
    private static DAOExperimentCount.CallType convertCallTypeToDAOCallType(CallType.Expression callType) {
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
                    return Stream.of(GlobalExpressionCallDAO.Attribute.GLOBAL_CONDITION_ID);
                case CALL_TYPE: 
                case DATA_QUALITY:
                    return Stream.of(GlobalExpressionCallDAO.Attribute.DATA_TYPE_EXPERIMENT_TOTAL_COUNTS);
                case EXPERIMENT_COUNTS:
                    return Stream.of(GlobalExpressionCallDAO.Attribute.DATA_TYPE_EXPERIMENT_TOTAL_COUNTS,
                            GlobalExpressionCallDAO.Attribute.DATA_TYPE_EXPERIMENT_SELF_COUNTS,
                            GlobalExpressionCallDAO.Attribute.DATA_TYPE_EXPERIMENT_PROPAGATED_COUNTS);
                case OBSERVED_DATA:
                    return Stream.of(GlobalExpressionCallDAO.Attribute.DATA_TYPE_OBSERVED_DATA);
                case MEAN_RANK:
                case GENE_QUAL_EXPR_LEVEL:
                case ANAT_ENTITY_QUAL_EXPR_LEVEL:
                    return Stream.of(GlobalExpressionCallDAO.Attribute.MEAN_RANK);
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
                            return GlobalExpressionCallDAO.OrderingAttribute.PUBLIC_GENE_ID;
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
            ExpressionCallFilter callFilter,
            Map<AnatEntity, EntityMinMaxRanks<AnatEntity>> anatEntityMinMaxRanks,
            Map<Gene, EntityMinMaxRanks<Gene>> geneMinMaxRanks,
            Set<CallService.Attribute> attrs) {
        log.entry(globalCallTO, geneMap, condMap, callFilter, anatEntityMinMaxRanks,
                geneMinMaxRanks, attrs);
        
        Set<ExpressionCallData> callData = mapGlobalCallTOToExpressionCallData(globalCallTO,
                attrs, callFilter.getDataTypeFilters());

        Condition cond = condMap.get(globalCallTO.getConditionId());
        Gene gene = geneMap.get(globalCallTO.getBgeeGeneId());
        ExpressionSummary exprSummary = attrs == null || attrs.isEmpty() || attrs.contains(Attribute.CALL_TYPE)?
                inferSummaryCallType(callData): null;
        return log.exit(new ExpressionCall(
            attrs == null || attrs.isEmpty() || attrs.contains(Attribute.GENE)?
                    gene: null,
            attrs == null || attrs.isEmpty() || attrs.stream().anyMatch(a -> a.isConditionParameter())?
                    cond: null,
            attrs == null || attrs.isEmpty() || attrs.contains(Attribute.OBSERVED_DATA)?
                    inferDataPropagation(callData): null,
            exprSummary,
            attrs == null || attrs.isEmpty() || attrs.contains(Attribute.DATA_QUALITY)?
                    inferSummaryQuality(callData): null,
            attrs == null || attrs.isEmpty() || attrs.contains(Attribute.EXPERIMENT_COUNTS) ||
                    attrs.contains(Attribute.DATA_TYPE_RANK_INFO)?
                            callData: null,
            attrs == null || attrs.isEmpty() || attrs.contains(Attribute.MEAN_RANK) ||
            attrs.contains(Attribute.ANAT_ENTITY_QUAL_EXPR_LEVEL) ||
            attrs.contains(Attribute.GENE_QUAL_EXPR_LEVEL)?
                    loadExpressionLevelInfo(exprSummary, globalCallTO.getMeanRank(),
                            anatEntityMinMaxRanks == null? null:
                                anatEntityMinMaxRanks.get(cond.getAnatEntity()),
                            geneMinMaxRanks == null? null: geneMinMaxRanks.get(gene)): null));
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
                    cdTO.getExperimentCounts() != null && !cdTO.getExperimentCounts().isEmpty();
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

            return new ExpressionCallData(dt, counts,
                    getExperimentsCounts? cdTO.getPropagatedCount(): null,
                    getRankInfo? cdTO.getRank(): null,
                    getRankInfo? cdTO.getRankNorm(): null,
                    getRankInfo? cdTO.getWeightForMeanRank(): null,
                    getDataProp? mapDAOCallDataTOToDataPropagation(cdTO): null);
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
            Set<DataType> requestedDataTypes) throws IllegalArgumentException, IllegalStateException {
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

    //*************************************************************************
    // HELPER METHODS FOR INFERENCES
    //*************************************************************************

    private static DataPropagation inferDataPropagation(Set<ExpressionCallData> callData) {
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

        if (dataProp1 == null && dataProp2 == null) {
            return log.exit(DATA_PROPAGATION_IDENTITY);
        }
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
            throw log.throwing(new IllegalArgumentException("Inconsistent DataPropagations: "
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
        if (state1 == null) {
            return log.exit(state2);
        }
        if (state2 == null) {
            return log.exit(state1);
        }

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
            if (propStates.contains(PropagationState.DESCENDANT)) {
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
            if (propStates.contains(PropagationState.SELF_AND_ANCESTOR)) {
                return log.exit(PropagationState.ALL);
            } else if (propStates.contains(PropagationState.SELF_AND_DESCENDANT)) {
                return log.exit(PropagationState.SELF_AND_DESCENDANT);
            } else if (propStates.contains(PropagationState.ANCESTOR_AND_DESCENDANT)) {
                return log.exit(PropagationState.ANCESTOR_AND_DESCENDANT);
            } else {
                throw log.throwing(new AssertionError("Case not covered, " + propStates));
            }
        } else if (propStates.contains(PropagationState.SELF_AND_ANCESTOR)) {
            if (propStates.contains(PropagationState.SELF_AND_DESCENDANT)) {
                return log.exit(PropagationState.ALL);
            } else if (propStates.contains(PropagationState.ANCESTOR_AND_DESCENDANT)) {
                return log.exit(PropagationState.ALL);
            } else {
                throw log.throwing(new AssertionError("Case not covered, " + propStates));
            }
        } else if (propStates.contains(PropagationState.SELF_AND_DESCENDANT)) {
            if (propStates.contains(PropagationState.ANCESTOR_AND_DESCENDANT)) {
                return log.exit(PropagationState.ALL);
            }
            throw log.throwing(new AssertionError("Case not covered, " + propStates));
        } else if (propStates.contains(PropagationState.ANCESTOR_AND_DESCENDANT)) {
            //all cases already covered in previous if/else
            throw log.throwing(new AssertionError("Case not covered, " + propStates));
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
            expPresentHigh += retrieveExperimentCount(cd, CallType.Expression.EXPRESSED, DataQuality.HIGH,
                PropagationState.ALL);
            expPresentLow  += retrieveExperimentCount(cd, CallType.Expression.EXPRESSED, DataQuality.LOW,
                PropagationState.ALL);
            expAbsentHigh  += retrieveExperimentCount(cd, CallType.Expression.NOT_EXPRESSED, DataQuality.HIGH,
                PropagationState.ALL);
            expAbsentLow   += retrieveExperimentCount(cd, CallType.Expression.NOT_EXPRESSED, DataQuality.LOW,
                PropagationState.ALL);
        }
        
        if (expPresentHigh >= MIN_HIGH_GOLD) {
            return log.exit(SummaryQuality.GOLD);
        }
        if (expPresentHigh >= MIN_HIGH_SILVER || expPresentLow >= MIN_LOW_SILVER) {
            return log.exit(SummaryQuality.SILVER);
        }
        if (expPresentHigh >= MIN_HIGH_BRONZE || expPresentLow >= MIN_LOW_BRONZE) {
            //*Currently* we don't have bronze quality if we have some present high.
            //Could change in the future if we change the thresholds.
            assert expPresentHigh == 0;
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
     * Retrieve the count from the {@code ExperimentExpressionCount} in an {@code ExpressionCallData}
     * matching the requested {@code Expression}, {@code DataQuality} and {@code PropagationState}.
     * 
     * @param cd        An {@code ExpressionCallData} that is the call data
     *                  for which count should be retrieved.
     * @param expr      An {@code Expression} that is the call type allowing to filter counts.
     * @param qual      A {@code DataQuality} that is the quality allowing to filter counts.
     * @param state     A {@code PropagationState} that is the propagation state allowing to filter counts.
     * @return          The {@code int} that is the corresponding count. 
     */
    private static int retrieveExperimentCount(ExpressionCallData cd, Expression expr, DataQuality qual,
            PropagationState state) {
        log.entry(cd, expr, qual, state);
        if (cd.getExperimentCounts() == null) {
            return log.exit(0);
        }
        Set<ExperimentExpressionCount> counts = cd.getExperimentCounts().stream()
            .filter(c -> expr.equals(c.getCallType()) && qual.equals(c.getDataQuality())
                        && state.equals(c.getPropagationState()))
            .collect(Collectors.toSet());
        assert counts.size() <= 1: "Only one ExperimentExpressionCount at most can match the requested parameters";
        log.trace("ExpressionCallData: {}, Expression {}, DataQuality: {}, PropagationState: {}, counts: {}",
                cd, expr, qual, state, counts);
        if (counts.isEmpty()) {
            //For instance, if we request no-expression from EST data, no corresponding counts
            return log.exit(0);
        }
        return log.exit(counts.iterator().next().getCount());
    }
}
