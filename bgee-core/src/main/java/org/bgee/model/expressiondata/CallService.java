package org.bgee.model.expressiondata;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
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
import org.bgee.model.anatdev.SexService;
import org.bgee.model.anatdev.StrainService;
import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.expressiondata.CallDAOFilter;
import org.bgee.model.dao.api.expressiondata.CallObservedDataDAOFilter;
import org.bgee.model.dao.api.expressiondata.ConditionDAO;
import org.bgee.model.dao.api.expressiondata.DAOConditionFilter;
import org.bgee.model.dao.api.expressiondata.DAODataType;
import org.bgee.model.dao.api.expressiondata.DAOFDRPValueFilter;
import org.bgee.model.dao.api.expressiondata.DAOPropagationState;
import org.bgee.model.dao.api.expressiondata.GlobalExpressionCallDAO;
import org.bgee.model.dao.api.expressiondata.ConditionDAO.ConditionRankInfoTO;
import org.bgee.model.dao.api.expressiondata.GlobalExpressionCallDAO.GlobalExpressionCallDataTO;
import org.bgee.model.dao.api.expressiondata.GlobalExpressionCallDAO.GlobalExpressionCallTO;
import org.bgee.model.dao.api.gene.GeneDAO;
import org.bgee.model.expressiondata.Call.DiffExpressionCall;
import org.bgee.model.expressiondata.Call.ExpressionCall;
import org.bgee.model.expressiondata.CallData.ExpressionCallData;
import org.bgee.model.expressiondata.CallFilter.DiffExpressionCallFilter;
import org.bgee.model.expressiondata.CallFilter.ExpressionCallFilter;
import org.bgee.model.expressiondata.Condition.ConditionEntities;
import org.bgee.model.expressiondata.MultiGeneExprAnalysis.MultiGeneExprCounts;
import org.bgee.model.expressiondata.baseelements.DataPropagation;
import org.bgee.model.expressiondata.baseelements.PropagationState;
import org.bgee.model.expressiondata.baseelements.QualitativeExpressionLevel;
import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.model.expressiondata.baseelements.EntityMinMaxRanks;
import org.bgee.model.expressiondata.baseelements.ExpressionLevelCategory;
import org.bgee.model.expressiondata.baseelements.ExpressionLevelInfo;
import org.bgee.model.expressiondata.baseelements.FDRPValue;
import org.bgee.model.expressiondata.baseelements.FDRPValueCondition;
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
     * <li>{@code CELL_TYPE_ID}: corresponds to {@link Condition#getCellTypeId()} from {@link Call#getCondition()}.
     * <li>{@code SEX_ID}: corresponds to {@link Condition#getSexId()} from {@link Call#getCondition()}.
     * <li>{@code STRAIN_ID}: corresponds to {@link Condition#getStrainId()} from {@link Call#getCondition()}.
     * <li>{@code CALL_TYPE}: corresponds to {@link Call#getSummaryCallType()}.
     * <li>{@code DATA_QUALITY}: corresponds to {@link Call#getSummaryQuality()}.
     * <li>{@code OBSERVED_DATA}: corresponds to {@link Call#getDataPropagation()}, as well as
     * {@link ExpressionCallData#getDataPropagation()} in the objects returned by
     * {@link Call#getCallData()} for each requested data type.
     * <li>{@code MEAN_RANK}: corresponds to {@link ExpressionCall#getMeanRank()}.
     * <li>{@code EXPRESSION_SCORE}: corresponds to {@link ExpressionCall#getExpressionScore()}.
     * <li>{@code DATA_TYPE_RANK_INFO}: corresponds to {@link ExpressionCallData#getRank()},
     * {@link ExpressionCallData#getNormalizedRank()}, and {@link ExpressionCallData#getWeightForMeanRank()},
     * in the objects returned by {@link Call#getCallData()} for each requested data type.
     * <li>{@code P_VALUE_INFO_ALL_DATA_TYPES}: corresponds to {@link ExpressionCall#getPValues()}
     * and {@link ExpressionCall#getBestDescendantPValues()}, with FDR-corrected p-values:
     *   <ul>
     *   <li>computed from the p-values from all requested data types in the condition itself
     *       and its sub-conditions (stored in {@link ExpressionCall#getPValues()}).
     *   <li>computed from the p-values from only the requested data types that are trusted
     *       for producing ABSENT expression calls, in the condition itself and its sub-conditions
     *       (stored in {@link ExpressionCall#getPValues()}). This mechanism is used to determine
     *       whether an ABSENT call is indeed supported by data types trusted for ABSENT calls.
     *   <li>that is the best FDR-corrected p-value among the sub-conditions of the condition
     *       of the call, computed from the p-values from all requested data types
     *       (stored in {@link ExpressionCall#getBestDescendantPValues()}).
     *   <li>that is the best FDR-corrected p-value among the sub-conditions of the condition
     *       of the call, computed from the p-values from only the requested data types that are trusted
     *       for producing ABSENT expression calls (stored in
     *       {@link ExpressionCall#getBestDescendantPValues()}). This mechanism is used to determine
     *       whether an ABSENT call is indeed supported by data types trusted for ABSENT calls.
     *   </ul>
     * Also, if this attribute is requested, the values returned by
     * {@link ExpressionCallData#getSelfObservationCount()} and
     * {@link ExpressionCallData#getDescendantObservationCount()} will be populated
     * in the objects returned by {@link Call#getCallData()} for each requested data type.
     * This is notably needed to determine which data types had data supporting the call.
     * <li>{@code P_VALUE_INFO_EACH_DATA_TYPE}: retrieve FDR-corrected p-values and observation counts
     * for each individual data types. As a result, in the objects returned by {@link Call#getCallData()}
     * for each data type:
     *   <ul>
     *   <li>{@link ExpressionCallData#getSelfObservationCount()} and
     *   {@link ExpressionCallData#getDescendantObservationCount()} will be populated
     *   (as when using the attribute {@code P_VALUE_INFO_ALL_DATA_TYPES}).
     *   This is notably useful to determine which data types had data supporting the call.
     *   <li>{@link ExpressionCallData#getFDRPValue()} and
     *   {@link ExpressionCallData#getBestDescendantFDRPValue()} will be populated
     *   (unlike when using the attribute {@code P_VALUE_INFO_ALL_DATA_TYPES})
     *   </ul>
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
        //TODO: remove the _ID part from condition parameters
        GENE(false, null), ANAT_ENTITY_ID(true, "anatomicalEntity"),
        DEV_STAGE_ID(true, "developmentalStage"), CELL_TYPE_ID(true, "cellType"),
        SEX_ID(true, "sex"), STRAIN_ID(true, "strain"), CALL_TYPE(false, null),
        DATA_QUALITY(false, null), OBSERVED_DATA(false, null), MEAN_RANK(false, null), 
        EXPRESSION_SCORE(false, null), DATA_TYPE_RANK_INFO(false, null),
        P_VALUE_INFO_ALL_DATA_TYPES(false, null), P_VALUE_INFO_EACH_DATA_TYPE(false, null),
        GENE_QUAL_EXPR_LEVEL(false, null), ANAT_ENTITY_QUAL_EXPR_LEVEL(false, null);

        /**
         * @return  An {@code EnumSet} containing all {@code Attribute}s that are condition parameters
         *          ({@link #isConditionParameter()} returns {@code true}).
         */
        public static EnumSet<Attribute> getAllConditionParameters() {
            log.traceEntry();
            return log.traceExit(Arrays.stream(CallService.Attribute.values())
                    .filter(a -> a.isConditionParameter())
                    .collect(Collectors.toCollection(() -> EnumSet.noneOf(CallService.Attribute.class))));
        }

        private final String condParamName;
        /**
         * @see #isConditionParameter()
         */
        private final boolean conditionParameter;

        private Attribute(boolean conditionParameter, String condParamName) {
            this.conditionParameter = conditionParameter;
            this.condParamName = condParamName;
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
            return this.condParamName;
        }
    }

    public static enum OrderingAttribute implements Service.OrderingAttribute {
        GENE_ID(false), ANAT_ENTITY_ID(true), DEV_STAGE_ID(true), CELL_TYPE_ID(true), 
        SEX_ID(true), STRAIN_ID(true), MEAN_RANK(false);
        
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
    //XXX: As of Bgee 15.0, we always use global ranks rather than self ranks,
    //But we could allow to parameterize that in the future.
    private final static boolean GLOBAL_RANK = true;
    /**
     * A {@code BigDecimal} that is the value a FDR-corrected p-value must be less than or equal to
     * for PRESENT LOW QUALITY.
     */
    public final static BigDecimal PRESENT_LOW_LESS_THAN_OR_EQUALS_TO = new BigDecimal("0.05");
    /**
     * A {@code BigDecimal} that is the value a FDR-corrected p-value must be less than or equal to
     * for PRESENT HIGH QUALITY.
     */
    public final static BigDecimal PRESENT_HIGH_LESS_THAN_OR_EQUALS_TO = new BigDecimal("0.01");
    /**
     * A {@code BigDecimal} that is the value a FDR-corrected p-value must be greater than
     * for ABSENT HIGH QUALITY.
     */
    public final static BigDecimal ABSENT_HIGH_GREATER_THAN = new BigDecimal("0.1");
    /**
     * A {@code BigDecimal} that is the value a FDR-corrected p-value must be greater than
     * for ABSENT LOW QUALITY.
     */
    public final static BigDecimal ABSENT_LOW_GREATER_THAN = PRESENT_LOW_LESS_THAN_OR_EQUALS_TO;

    protected static final DataPropagation DATA_PROPAGATION_IDENTITY = new DataPropagation(null, null, null, null, null, null);
    protected final static Set<PropagationState> ALLOWED_PROP_STATES = EnumSet.of(
            //As of Bgee 14.2 we do not propagate absent calls to substructures anymore
            PropagationState.SELF, /*PropagationState.ANCESTOR,*/ PropagationState.DESCENDANT,
            /*PropagationState.SELF_AND_ANCESTOR,*/ PropagationState.SELF_AND_DESCENDANT
            /*, PropagationState.ANCESTOR_AND_DESCENDANT, PropagationState.ALL*/);
    /**
     * A {@code Map} containing a single {@code Entry} where the key is the {@code ExpressionSummary}
     * and the value is the {@code SummaryQuality} necessary to correctly retrieve all rank info.
     * Used to initialize a new {@link CallFilter.ExpressionCallFilter}.
     */
    private static final Map<SummaryCallType.ExpressionSummary, SummaryQuality>
    CALL_TYPE_QUAL_FOR_RANKS_FILTER = ExpressionCallFilter.BRONZE_PRESENT_ARGUMENT;
    /**
     * A {@code BigDecimal} representing the minimum value that can take an expression score.
     */
    public final static BigDecimal EXPRESSION_SCORE_MIN_VALUE = new BigDecimal("0.01");
    /**
     * A {@code BigDecimal} representing the maximum value that can take an expression score.
     */
    public final static BigDecimal EXPRESSION_SCORE_MAX_VALUE = new BigDecimal("100");
    //*************************************************
    // INSTANCE ATTRIBUTES AND CONSTRUCTOR
    //*************************************************
    protected final ConditionDAO conditionDAO;
    private final GeneDAO geneDAO;
    private final GlobalExpressionCallDAO globalExprCallDAO;
    private final AnatEntityService anatEntityService;
    private final DevStageService devStageService;
    private final SexService sexService;
    private final StrainService strainService;
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
        this.sexService = this.getServiceFactory().getSexService();
        this.strainService = this.getServiceFactory().getStrainService();
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
        log.traceEntry("{}, {}, {}", callFilter, attributes, orderingAttributes);

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
        //If several species were requested, it is necessary to request at least GENE
        //or a condition parameter (ANAT_ENTITY_ID, etc)
        if (speciesMap.size() > 1 && !clonedAttrs.isEmpty() &&
                !clonedAttrs.contains(Attribute.GENE) && clonedAttrs.stream().noneMatch(a -> a.isConditionParameter())) {
            throw log.throwing(new IllegalArgumentException(
                    "You requested data in several species, you should request in Attributes gene information "
                    + "or condition parameter information, otherwise you won't be able to distinguish calls from different species."));
        }

        //Retrieve a Map of Bgee gene IDs to Gene. This will throw a GeneNotFoundException
        //if some requested gene IDs were not found in Bgee.
        Map<Integer, Gene> geneMap = loadGeneMapFromGeneFilters(callFilter.getGeneFilters(),
                speciesMap, this.geneDAO);
        assert !geneMap.isEmpty();

        // Define condition parameter combination allowing to target a specific data aggregation
        final EnumSet<ConditionDAO.Attribute> condParamCombination =
                loadConditionParameterCombination(callFilter, clonedAttrs, clonedOrderingAttrs.keySet());

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
                            generateDAOConditionFilters(callFilter.getConditionFilters(),
                                    condParamCombination),
                            convertCondParamAttrsToCondDAOAttrs(clonedAttrs),
                        this.conditionDAO, this.anatEntityService, this.devStageService,
                        this.sexService, this.strainService));

        // Retrieve min./max ranks per anat. entity if info requested
        // and if the main expression call query will not allow to obtain this information
        Map<Condition, EntityMinMaxRanks<Condition>> anatEntityMinMaxRanks =
                loadMinMaxRanksPerAnatEntity(clonedAttrs, clonedOrderingAttrs, condParamCombination,
                        geneMap, condMap, callFilter);
        // Retrieve min./max ranks per gene if info requested
        // and if the main expression call query will not allow to obtain this information
        Map<Gene, EntityMinMaxRanks<Gene>> geneMinMaxRanks = loadMinMaxRanksPerGene(
                clonedAttrs, clonedOrderingAttrs, condParamCombination, geneMap, callFilter);

        //Retrieve max rank for the requested species if EXPRESSION_SCORE requested
        //(the max rank is required to convert mean ranks into expression scores)
        Map<Integer, ConditionRankInfoTO> maxRankPerSpecies = clonedAttrs.isEmpty() ||
                clonedAttrs.contains(Attribute.EXPRESSION_SCORE)?
                        conditionDAO.getMaxRanks(speciesMap.keySet(),
                                //We always request the max rank over all data types,
                                //independently of the data types requested in the query,
                                //because ranks are all normalized based on the max rank over all data types
                                null):
                        null;


        //All necessary information ready, retrieve ExpressionCalls
        return log.traceExit(loadExpressionCallStream(callFilter, clonedAttrs, clonedOrderingAttrs,
                condParamCombination, geneMap, condMap, maxRankPerSpecies, anatEntityMinMaxRanks, geneMinMaxRanks));
    }

    public Stream<DiffExpressionCall> loadDiffExpressionCalls(Integer speciesId, 
            DiffExpressionCallFilter callFilter) {
        log.traceEntry("{} {}", speciesId, callFilter);
        throw log.throwing(new UnsupportedOperationException("Load of diff. expression calls not implemented yet"));
    }

    /**
     * Retrieve only {@code ExpressionCall}s that have at least a SILVER {@code SummaryQuality} for a given
     * {@code AnatEntity} AND at least a BRONZE {@code SummaryQuality} for the same calls
     * taking into account all condition parameters.
     * These {@code ExpressionCall}s are filtered and ordered by rank using 
     * {@link ExpressionCall#filterAndOrderCallsByRank(Collection, ConditionGraph)}
     * 
     * @param geneFilter    A {@code GeneFilter} targeting a <strong>single gene</strong>
     *                      for which {@code ExpressionCall}s have to be retrieved
     * @return              A {@code LinkedHashMap} where keys correspond to the
     *                      {@code ExpressionCall}s considering only the anat. entity,
     *                      the associated value being a {@code List} of {@code ExpressionCall}s
     *                      taking into account all condition parameters,
     *                      for the same {@code AnatEntity}.
     * @throws IllegalArgumentException If {@code geneFilter} targets not one and only one gene.
     */
    public LinkedHashMap<ExpressionCall, List<ExpressionCall>>
    loadCondCallsWithSilverAnatEntityCallsByAnatEntity(GeneFilter geneFilter) throws IllegalArgumentException {
        log.traceEntry("{}", geneFilter);
        return log.traceExit(this.loadCondCallsWithSilverAnatEntityCallsByAnatEntity(geneFilter, null));
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
     * @return              A {@code LinkedHashMap} where keys correspond to the
     *                      {@code ExpressionCall}s considering only the anat. entity,
     *                      the associated value being a {@code List} of {@code ExpressionCall}s
     *                      taking into account all condition parameters,
     *                      for the same {@code AnatEntity}.
     * @throws IllegalArgumentException If {@code geneFilter} targets not one and only one gene.
     */
    public LinkedHashMap<ExpressionCall, List<ExpressionCall>>
    loadCondCallsWithSilverAnatEntityCallsByAnatEntity(GeneFilter geneFilter, ConditionGraph condGraph)
            throws IllegalArgumentException {
        log.traceEntry("{}, {}", geneFilter, condGraph);

        //**************************************************
        // Sanity checks and prepare arguments
        //**************************************************
        if (geneFilter.getEnsemblGeneIds().size() != 1) {
            throw log.throwing(new IllegalArgumentException("GeneFilter not targeting only one gene"));
        }

        EnumSet<CallService.Attribute> baseAttributes = EnumSet.of(
                CallService.Attribute.GENE, CallService.Attribute.ANAT_ENTITY_ID,
                CallService.Attribute.CELL_TYPE_ID,
                CallService.Attribute.CALL_TYPE, CallService.Attribute.DATA_QUALITY,
                CallService.Attribute.MEAN_RANK, CallService.Attribute.EXPRESSION_SCORE,
                //We need the p-value info per data type to know which data types
                //produced the calls
                CallService.Attribute.P_VALUE_INFO_EACH_DATA_TYPE,
                //We also want to know the global FDR-corrected p-value
                CallService.Attribute.P_VALUE_INFO_ALL_DATA_TYPES);

        //**************************************************
        // Load silver organ calls
        //**************************************************
        LinkedHashMap<CallService.OrderingAttribute, Service.Direction> orderByOrgan =
                new LinkedHashMap<>();
        orderByOrgan.put(CallService.OrderingAttribute.MEAN_RANK, Service.Direction.ASC);
        orderByOrgan.put(CallService.OrderingAttribute.ANAT_ENTITY_ID, Service.Direction.ASC);
        orderByOrgan.put(CallService.OrderingAttribute.CELL_TYPE_ID, Service.Direction.ASC);
        
        List<ExpressionCall> organCalls = this
                .loadExpressionCalls(
                        new ExpressionCallFilter(ExpressionCallFilter.SILVER_PRESENT_ARGUMENT,
                                Collections.singleton(geneFilter),
                                ExpressionCallFilter.ANAT_ENTITY_OBSERVED_DATA_ARGUMENT),
                        baseAttributes,
                        orderByOrgan)
                .collect(Collectors.toList());
        if (organCalls.isEmpty()) {
            log.debug("No calls for gene {}", geneFilter.getEnsemblGeneIds().iterator().next());
            return log.traceExit(new LinkedHashMap<>());
        }

        //**************************************************
        // Load bronze calls with all condition Parameters
        //**************************************************
        LinkedHashMap<CallService.OrderingAttribute, Service.Direction> orderByAllCond =
                new LinkedHashMap<>();
        orderByAllCond.put(CallService.OrderingAttribute.MEAN_RANK, Service.Direction.ASC);
        orderByAllCond.put(CallService.OrderingAttribute.ANAT_ENTITY_ID, Service.Direction.ASC);
        orderByAllCond.put(CallService.OrderingAttribute.CELL_TYPE_ID, Service.Direction.ASC);
        orderByAllCond.put(CallService.OrderingAttribute.DEV_STAGE_ID, Service.Direction.ASC);
        orderByAllCond.put(CallService.OrderingAttribute.SEX_ID, Service.Direction.ASC);
        orderByAllCond.put(CallService.OrderingAttribute.STRAIN_ID, Service.Direction.ASC);
        
        EnumSet<CallService.Attribute> allCondParamAttrs = EnumSet.copyOf(baseAttributes);
        allCondParamAttrs.addAll(CallService.Attribute.getAllConditionParameters());
        ConditionEntities condEntities = new ConditionEntities(organCalls.stream()
                .map(c -> c.getCondition())
                .collect(Collectors.toSet()));

        final List<ExpressionCall> allCondParamsCalls = this
                .loadExpressionCalls(
                        new ExpressionCallFilter(ExpressionCallFilter.BRONZE_PRESENT_ARGUMENT,
                                Collections.singleton(geneFilter),
                                Collections.singleton(new ConditionFilter(condEntities, null)),
                                null, true, null),
                        allCondParamAttrs,
                        orderByAllCond)
                .collect(Collectors.toList());
        
        return log.traceExit(this.loadCondCallsBySilverAnatEntityCalls(
                organCalls, allCondParamsCalls, true, condGraph));
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
     * @param orderedAnatEntityCalls    A {@code List} of {@code ExpressionCall}s with {@code Condition}s
     *                                  considering only the anat. entity parameters,
     *                                  ordered by ranks. They must contain information
     *                                  of expression state, quality, and rank, otherwise an
     *                                  {@code IllegalArgumentException} is thrown.
     * @param orderedConditionCalls     A {@code List} of {@code ExpressionCall}s with {@code Condition}s
     *                                  considering all parameters, ordered by ranks.
     *                                  They must contain information of expression state,
     *                                  quality, and rank, otherwise an
     *                                  {@code IllegalArgumentException} is thrown.
     * @param callsFiltered     A {@code Boolean} that should be {@code true} if the calls
     *                          were already filtered for the appropriate expression status
     *                          and qualities, {@code false} if the calls need to be filtered.
     * @param condGraph         A {@code ConditionGraph} for the species the requested gene belongs to,
     *                          that will be used to order condition calls and filter redundant calls.
     *                          Can be {@code null} if the {@code ConditionGraph} needs to be computed
     *                          by this method.
     * @return                  A {@code LinkedHashMap} where keys correspond to the valid
     *                          {@code ExpressionCall}s in {@code anatEntityCalls}, the associated value
     *                          being a {@code List} of valid {@code ExpressionCall}s from
     *                          {@code conditionCalls}, for the same {@code AnatEntity}.
     * @throws IllegalArgumentException If the {@code ExpressionCall}s do not contain the information
     *                                  needed when filtering them ({@code callsFiltered} set to {@code false})
     */
    public LinkedHashMap<ExpressionCall, List<ExpressionCall>>
    loadCondCallsBySilverAnatEntityCalls(List<ExpressionCall> orderedAnatEntityCalls,
            List<ExpressionCall> orderedConditionCalls, boolean callsFiltered, ConditionGraph condGraph)
                    throws IllegalArgumentException {
        log.traceEntry("{}, {}, {}, {}", orderedAnatEntityCalls, orderedConditionCalls,
                callsFiltered, condGraph);

        //*****************************************************************
        // Filtering of calls based on CallType and Quality, if requested
        //*****************************************************************
        LinkedHashSet<ExpressionCall> filteredOrderedAnatEntityCalls = new LinkedHashSet<>(orderedAnatEntityCalls);
        if (!callsFiltered) {
            filteredOrderedAnatEntityCalls = orderedAnatEntityCalls.stream()
                .filter(c -> {
                    if (c.getSummaryCallType() == null) {
                        throw log.throwing(new IllegalArgumentException(
                                "The provided calls do not have SummaryCallType"));
                    }
                    if (c.getSummaryQuality() == null) {
                        throw log.throwing(new IllegalArgumentException(
                                "The provided calls do not have SummaryQuality"));
                    }
                    if (c.getMeanRank() == null) {
                        throw log.throwing(new IllegalArgumentException(
                                "The provided calls do not have rank info"));
                    }
                    return c.getSummaryCallType().equals(ExpressionSummary.EXPRESSED) &&
                            !c.getSummaryQuality().equals(SummaryQuality.BRONZE);
                })
                .collect(Collectors.toCollection(LinkedHashSet::new));
        }
        List<ExpressionCall> filteredOrderedCondCalls = orderedConditionCalls;
        if (!callsFiltered) {
            final Set<Condition> anatEntityConds = filteredOrderedAnatEntityCalls.stream()
                    .map(c -> c.getCondition())
                    .collect(Collectors.toSet());
            assert anatEntityConds.stream()
            .noneMatch(c -> c.getDevStage() != null || c.getSex() != null || c.getStrain() != null);

            filteredOrderedCondCalls = orderedConditionCalls.stream()
                    .filter(c -> {
                        if (c.getSummaryCallType() == null) {
                            throw log.throwing(new IllegalArgumentException(
                                    "The provided calls do not have SummaryCallType"));
                        }
                        if (c.getMeanRank() == null) {
                            throw log.throwing(new IllegalArgumentException(
                                    "The provided calls do not have rank info"));
                        }
                        if (!c.getSummaryCallType().equals(ExpressionSummary.EXPRESSED)) {
                            return false;
                        }
                        return anatEntityConds.contains(new Condition(c.getCondition().getAnatEntity(),
                                null, c.getCondition().getCellType(), null, null, c.getCondition().getSpecies()));
                    })
                    .collect(Collectors.toList());
        }

        //*****************************************************************
        // Ordering calls and identifying redundant calls
        //*****************************************************************
        if (filteredOrderedCondCalls.isEmpty()) {
            log.debug("No condition calls for gene");
            return log.traceExit(new LinkedHashMap<>());
        }

        //we need to make sure that the ExpressionCalls are ordered in exactly the same way
        //for the display and for the clustering, otherwise the display will be buggy,
        //notably for calls with equal ranks. And we need to take into account
        //relations between Conditions for filtering them, which would be difficult to achieve
        //only by a query to the data source. So, we order them anyway.
        
        //Cond calls
        ConditionGraph conditionGraph = condGraph;
        if (condGraph == null) {
            conditionGraph = this.getServiceFactory().getConditionGraphService().loadConditionGraph(
                    filteredOrderedCondCalls.stream()
                    .map(c -> c.getCondition())
                    .collect(Collectors.toSet()));
        }
        filteredOrderedCondCalls = ExpressionCall.filterAndOrderCallsByRank(filteredOrderedCondCalls,
                conditionGraph);
        //REDUNDANT COND CALLS
        final Set<ExpressionCall> redundantCalls = ExpressionCall.identifyRedundantCalls(
                filteredOrderedCondCalls, conditionGraph);
        
        //*********************
        // Grouping
        //*********************
        //filter calls and group calls by anat. entity. We need to preserve the order 
        //of the keys, as we have already sorted the calls by their rank. 
        //If filterRedundantCalls is true, we completely discard anat. entities 
        //that have only redundant calls, but if an anat. entity has some non-redundant calls 
        //and is not discarded, we preserve all its calls, even the redundant ones.

        //First, build a map partial Condition -> conditionCalls
        Map<Condition, List<ExpressionCall>> condCallsPerAnatEntity =
                filteredOrderedCondCalls.stream()
                .map(c -> new AbstractMap.SimpleEntry<>(new Condition(c.getCondition().getAnatEntity(),
                            null, c.getCondition().getCellType(), null, null, c.getCondition().getSpecies()),
                        c))
                .collect(Collectors.toMap(e -> e.getKey(),
                        e -> new ArrayList<ExpressionCall>(Arrays.asList(e.getValue())),
                        (v1, v2) -> {v1.addAll(v2); return v1;}));
        //Now, group
        return log.traceExit(filteredOrderedAnatEntityCalls.stream()
                //discard if all calls of an anat. entity are redundant
                .filter(c -> {
                    List<ExpressionCall> relatedCondCalls = condCallsPerAnatEntity.get(
                            c.getCondition());
                    if (relatedCondCalls == null) {
                        log.debug("No BRONZE EXPRESSED condition calls in condition: {}",
                                c.getCondition());
                        return true;
                    }
                    return !redundantCalls.containsAll(relatedCondCalls);
                })
                //reconstruct the LinkedHashMap
                //Type inference hint needed, this code was compiling fine in Eclipse,
//              //not with maven... See for instance
//              //https://stackoverflow.com/questions/48135796/java-8-inferred-type-does-not-conform-to-upper-bounds-on-netbean-ide
                .collect(Collectors.<ExpressionCall, ExpressionCall,
                      List<ExpressionCall>, LinkedHashMap<ExpressionCall, List<ExpressionCall>>>toMap(
                        c -> c,
                        c -> {
                            List<ExpressionCall> relatedCondCalls = condCallsPerAnatEntity.get(
                                    c.getCondition());
                            if (relatedCondCalls == null) {
                                return new ArrayList<>();
                            }
                            return relatedCondCalls;
                        },
                        (l1, l2) -> {
                            throw log.throwing(new AssertionError("Not possible to have key collision"));
                        }, 
                        LinkedHashMap::new))
                );
    }

    //XXX: should the loadSingleSpeciesExprAnalysis methods moved to a new service?
    //Maybe a dedicated service for gene expression comparisons, both single and multi-species?
    //The fact that a partial mock using 'spy' was used for unit testing is a code smell.
    //XXX: Actually I don't think we need SingleSpeciesExprAnalysis and MultiSpeciesExprAnalysis,
    //MultiGeneExprAnalysis seems enough with generic type. Which is a stronger case
    //to use a separate, dedicated service.
    public SingleSpeciesExprAnalysis loadSingleSpeciesExprAnalysis(Collection<Gene> requestedGenes) {
        log.traceEntry("{}", requestedGenes);
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
                null, null                         //both observed and propagated calls
                );
        return log.traceExit(this.loadSingleSpeciesExprAnalysis(callFilter, clonedGenes));
    }
    public SingleSpeciesExprAnalysis loadSingleSpeciesExprAnalysis(ExpressionCallFilter callFilter) {
        log.traceEntry("{}", callFilter);
        if (callFilter.getGeneFilters().isEmpty()) {
            throw log.throwing(new IllegalArgumentException("A GeneFilter must be provided"));
        }
        Set<Gene> genes = this.getServiceFactory().getGeneService().loadGenes(callFilter.getGeneFilters())
                .collect(Collectors.toSet());
        return log.traceExit(this.loadSingleSpeciesExprAnalysis(callFilter, genes));
    }
    private SingleSpeciesExprAnalysis loadSingleSpeciesExprAnalysis(ExpressionCallFilter callFilter,
            Set<Gene> genes) {
        log.traceEntry("{}, {}", callFilter, genes);
        if (callFilter.getGeneFilters().size() != 1) {
            throw log.throwing(new IllegalArgumentException(
                    "This method is for comparing the expression of genes in a single species"));
        }
        Set<Attribute> attributes = EnumSet.of(Attribute.GENE, Attribute.ANAT_ENTITY_ID,
                Attribute.CALL_TYPE, Attribute.DATA_QUALITY, Attribute.OBSERVED_DATA, Attribute.EXPRESSION_SCORE);
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
            //Store expression score info for each Gene with data
            Map<Gene, ExpressionLevelInfo> geneToExprScore = list.stream()
            //Collectors.toMap does not accept null values,
            //see https://stackoverflow.com/a/24634007/1768736
            .collect(HashMap::new, (m, v) -> m.put(v.getGene(), v.getExpressionLevelInfo()), Map::putAll);
            Set<Gene> genesWithNoData = new HashSet<>(genes);
            genesWithNoData.removeAll(geneToExprScore.keySet());
            return new AbstractMap.SimpleEntry<>(list.iterator().next().getCondition(),
                    new MultiGeneExprCounts(callTypeToGenes, genesWithNoData, geneToExprScore));
        })
        //And we create the final Map condToCounts
        .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));

        return log.traceExit(new SingleSpeciesExprAnalysis(genes, condToCounts));
    }

    //*************************************************************************
    // METHODS PERFORMING THE QUERIES TO THE DAOs
    //*************************************************************************

    private Map<Condition, EntityMinMaxRanks<Condition>> loadMinMaxRanksPerAnatEntity(
            Set<Attribute> attrs, LinkedHashMap<OrderingAttribute, Service.Direction> orderingAttrs,
            EnumSet<ConditionDAO.Attribute> condParamCombination, Map<Integer, Gene> geneMap,
            Map<Integer, Condition> condMap, ExpressionCallFilter callFilter) {
        log.traceEntry("{}, {} ,{}, {}, {}, {}", attrs, orderingAttrs, condParamCombination, geneMap, condMap, callFilter);

        if (//qualitative expression levels relative to anat. entities not requested
            (!attrs.isEmpty() && !attrs.contains(Attribute.ANAT_ENTITY_QUAL_EXPR_LEVEL)) ||
            //or the main expression call query will allow to obtain the min.max ranks per anat. entity
            isQueryAllowingToComputeAnatEntityQualExprLevel(callFilter, condParamCombination,
                    attrs, orderingAttrs)) {
            //No need to query min./max ranks then
            return log.traceExit(new HashMap<>());
        }

        //Query to retrieve min./max ranks
        //We regenerate a new ExpressionCallFilter for properly performing the query
        ExpressionCallFilter newFilter = new ExpressionCallFilter(
                CALL_TYPE_QUAL_FOR_RANKS_FILTER,
                //new GeneFilters, we need to retrieve data for all genes of the requested species
                callFilter.getGeneFilters().stream().map(gf -> new GeneFilter(gf.getSpeciesId()))
                .collect(Collectors.toSet()),
                //new ConditionFilters, we need to retrieve data for all cond. parameters and not for
                //non-observed conditions.
                //We-re happy to keep a filtering based on anat. entity IDs though
                callFilter.getConditionFilters().stream()
                .map(cf -> new ConditionFilter(cf.getAnatEntityIds(), null, cf.getCellTypeIds(), null, null))
                .collect(Collectors.toSet()),
                //we keep the same data types as requested
                callFilter.getDataTypeFilters(),
                //get both observed and propagated calls, as since Bgee 15.0 a rank is always computed,
                //not only for observed calls
                null,
                //then we don't care about anat. entity/dev. stage/celltype/sex/strain observed data specifically
                null);
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
        throw log.throwing(new UnsupportedOperationException("Need to be reimplemented"));
        //Disable this for now, we need the method globalExprCallDAO.getMinMaxRanksPerAnatEntity
        //to be adapted to consider any condition parameter combination and use a different sort of IDs
        //to indentify the entities targeted
//        return log.traceExit(this.globalExprCallDAO.getMinMaxRanksPerAnatEntity(
//                convertDataTypeToDAODataType(newFilter.getDataTypeFilters()),
//                Arrays.asList(daoFilter))
//                .stream()
//                .map(minMaxRanksTO -> new EntityMinMaxRanks<Condition>(
//                        minMaxRanksTO.getMinRank(), minMaxRanksTO.getMaxRank(),
//                        Optional.ofNullable(idToAnatEntity.get(minMaxRanksTO.getId()))
//                        .orElseThrow(() -> new IllegalStateException(
//                                "Missing AnatEntity for ID " + minMaxRanksTO.getId()))))
//                .collect(Collectors.toMap(emmr -> emmr.getEntityConsidered(), emmr -> emmr)));
    }

    private Map<Gene, EntityMinMaxRanks<Gene>> loadMinMaxRanksPerGene(
            Set<Attribute> attrs, LinkedHashMap<OrderingAttribute, Service.Direction> orderingAttrs,
            EnumSet<ConditionDAO.Attribute> condParamCombination, Map<Integer, Gene> geneMap,
            ExpressionCallFilter callFilter) {
        log.traceEntry("{}, {}, {}, {}, {}", attrs, orderingAttrs, condParamCombination, geneMap, callFilter);

        if (//qualitative expression levels relative to genes not requested
            (!attrs.isEmpty() && !attrs.contains(Attribute.GENE_QUAL_EXPR_LEVEL)) ||
            //or the main expression call query will allow to obtain the min.max ranks per gene
            isQueryAllowingToComputeGeneQualExprLevel(callFilter, condParamCombination,
                    attrs, orderingAttrs)) {
            //No need to query min./max ranks then
            return log.traceExit(new HashMap<>());
        }

        //Query to retrieve min./max ranks
        //We regenerate a new ExpressionCallFilter for properly performing the query
        ExpressionCallFilter newFilter = new ExpressionCallFilter(
                CALL_TYPE_QUAL_FOR_RANKS_FILTER,
                //Use the same GeneFilters
                callFilter.getGeneFilters(),
                //new ConditionFilters, we need to retrieve data for all cond parameters
                null,
                //we keep the same data types as requested
                callFilter.getDataTypeFilters(),
                //get both observed and propagated calls, as since Bgee 15.0 a rank is always computed,
                //not only for observed calls
                null,
              //then we don't care about anat. entity/dev. stage/celltype/sex/strain observed data specifically
                null);
        //convert ExpressionCallFilter into CallDAOFilter
        CallDAOFilter daoFilter = convertCallFilterToCallDAOFilter(geneMap, newFilter,
                condParamCombination);

        //Perform query and map TOs to EntityMinMaxRanks
        return log.traceExit(this.globalExprCallDAO.getMinMaxRanksPerGene(
                convertDataTypeToDAODataType(newFilter.getDataTypeFilters()),
                Arrays.asList(daoFilter))
                .stream()
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
            ExpressionCallFilter callFilter, EnumSet<ConditionDAO.Attribute> condParamCombination,
            Set<Attribute> attributes, LinkedHashMap<OrderingAttribute, Service.Direction> orderingAttributes)
                    throws IllegalArgumentException {
        log.traceEntry("{}, {}, {}, {}, {}", geneMap, callFilter, condParamCombination, attributes, orderingAttributes);

        //TODO: retrieve sub-structures and sub-stages depending on ConditionFilter
        Stream<GlobalExpressionCallTO> calls = this.globalExprCallDAO
            .getGlobalExpressionCalls(Arrays.asList(
                //generate an ExpressionCallDAOFilter from callFilter
                convertCallFilterToCallDAOFilter(geneMap, callFilter, condParamCombination)),
                // Attributes
                convertServiceAttrToGlobalExprDAOAttr(attributes, callFilter),
                convertServiceOrderingAttrToGlobalExprDAOOrderingAttr(orderingAttributes, callFilter))
            //retrieve the Stream resulting from the query. Note that the query is not executed
            //as long as the Stream is not consumed (lazy-loading).
            .stream();

        return log.traceExit(calls);
    }

    //*************************************************************************
    // HELPER METHODS
    //*************************************************************************

    private Stream<ExpressionCall> loadExpressionCallStream(ExpressionCallFilter callFilter,
            Set<Attribute> attrs, LinkedHashMap<OrderingAttribute, Service.Direction> orderingAttrs,
            EnumSet<ConditionDAO.Attribute> condParamCombination,
            final Map<Integer, Gene> geneMap, final Map<Integer, Condition> condMap,
            Map<Integer, ConditionRankInfoTO> maxRankPerSpecies,
            Map<Condition, EntityMinMaxRanks<Condition>> anatEntityMinMaxRanks,
            Map<Gene, EntityMinMaxRanks<Gene>> geneMinMaxRanks) {
        log.traceEntry("{}, {}, {}, {}, {}, {}, {}, {}, {}", callFilter, attrs, orderingAttrs, 
                condParamCombination, geneMap, condMap, maxRankPerSpecies, anatEntityMinMaxRanks, 
                geneMinMaxRanks);

        // Retrieve the Stream<GlobalExpressionCallTO>
        Stream<GlobalExpressionCallTO> toStream = this.performsGlobalExprCallQuery(geneMap, callFilter, 
                condParamCombination, attrs, orderingAttrs);

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
                return log.traceExit(toListStream.flatMap(toList -> {
                    List<ExpressionCall> intermediateCalls = toList.stream()
                            //To retrieve the min./max ranks, we need to know the SummaryCall,
                            //so, rather than computing CallData and SumaryCall several times,
                            //we directly create ExpressionCalls
                            .map(to -> mapGlobalCallTOToExpressionCall(to, geneMap, condMap, callFilter,
                                    maxRankPerSpecies, null, null, attrs))
                            .collect(Collectors.toList());
    
                    if (intermediateCalls.isEmpty()) {
                        return intermediateCalls.stream();
                    }
                    //Compute the anatEntityMinMaxRank from this List of ExpressionCalls if possible
                    EntityMinMaxRanks<Condition> anatEntityMinMaxRank = computeAnatEntityMinMax?
                            getMinMaxRanksFromCallGroup(intermediateCalls,
                                    call -> call.getCondition()):
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
                            c.getPValues(),
                            c.getBestDescendantPValues(),
                            c.getSummaryCallType(),
                            c.getSummaryQuality(),
                            c.getCallData(),
                            loadExpressionLevelInfo(c.getSummaryCallType(), c.getMeanRank(),
                                    c.getExpressionScore(),
                                    c.getExpressionLevelInfo() == null? null:
                                        c.getExpressionLevelInfo().getMaxRankForExpressionScore(),
                                    anatEntityMinMaxRank != null? anatEntityMinMaxRank:
                                        anatEntityMinMaxRanks.get(c.getCondition()),
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
        return log.traceExit(toStream
            .map(to -> mapGlobalCallTOToExpressionCall(to, geneMap, condMap, callFilter,
                    maxRankPerSpecies, anatEntityMinMaxRanks, geneMinMaxRanks, attrs)));
    }

    private static boolean isQueryAllowingToComputeGeneQualExprLevel(ExpressionCallFilter callFilter,
            Set<ConditionDAO.Attribute> condParamCombination, Set<Attribute> attributes,
            LinkedHashMap<OrderingAttribute, Service.Direction> orderingAttributes) {
        log.traceEntry("{}, {}, {}, {}", callFilter, condParamCombination, attributes, orderingAttributes);

        //Perform the checks for qualitative expression levels relative to any entity
        if (!isQueryAllowingToComputeAnyQualExprLevel(callFilter, condParamCombination, attributes)) {
            return log.traceExit(false);
        }
        //Now, we do only the remaining checks for qualitative expression levels relative to genes

        //Obviously we need the gene info
        if (!attributes.contains(Attribute.GENE)) {
            return log.traceExit(false);
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
            return log.traceExit(false);
        }

        //We would need the query to retrieve expression calls in any anat. entity-stage.
        //And not discarding observed conditions or calls, to compute the expression level categories
        //(part of the checks done in isQueryAllowingToComputeAnyQualExprLevel)
        if ((callFilter.getConditionFilters() != null && callFilter.getConditionFilters().stream()
                .anyMatch(cf -> !cf.getAnatEntityIds().isEmpty()))) {
            return log.traceExit(false);
        }

        return log.traceExit(true);
    }

    private static boolean isQueryAllowingToComputeAnatEntityQualExprLevel(ExpressionCallFilter callFilter,
            Set<ConditionDAO.Attribute> condParamCombination, Set<Attribute> attributes,
            LinkedHashMap<OrderingAttribute, Service.Direction> orderingAttributes) {
        log.traceEntry("{}, {}, {}, {}", callFilter, condParamCombination, attributes, orderingAttributes);

        //Perform the checks for qualitative expression levels relative to any entity
        if (!isQueryAllowingToComputeAnyQualExprLevel(callFilter, condParamCombination, attributes)) {
            return log.traceExit(false);
        }
        //Now, we do only the remaining checks for qualitative expression levels relative to anat. entities

        //Obviously we need the anat. entity info
        if (!attributes.contains(Attribute.ANAT_ENTITY_ID)) {
            return log.traceExit(false);
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
            return log.traceExit(false);
        }

        //We would need the query to retrieve expression calls in any dev. stage and for any gene,
        //not discarding observed conditions or calls, to compute the expression level categories
        //(part of the checks done in isQueryAllowingToComputeAnyQualExprLevel)
        if (callFilter.getGeneFilters().stream().anyMatch(gf -> !gf.getEnsemblGeneIds().isEmpty())) {
            return log.traceExit(false);
        }

        return log.traceExit(true);
    }

    private static boolean isQueryAllowingToComputeAnyQualExprLevel(ExpressionCallFilter callFilter,
            Set<ConditionDAO.Attribute> condParamCombination, Set<Attribute> attributes) {
        log.traceEntry("{}, {}, {}", callFilter, condParamCombination, attributes);

        //If ranks not requested, we can't do anything
        if (!attributes.contains(Attribute.MEAN_RANK)) {
            return log.traceExit(false);
        }

        //We would need the query to retrieve calls of presence of expression of any quality
        SummaryQuality exprQual = callFilter.getSummaryCallTypeQualityFilter().get(SummaryCallType.ExpressionSummary.EXPRESSED);
        if (exprQual == null || !exprQual.equals(SummaryQuality.values()[0])) {
            return log.traceExit(false);
        }

        //We need calls to include any observed call state, as since Bgee 15.0 we compute a rank
        //for all calls, propagated or observed
        if (callFilter.getCallObservedData() != null || callFilter.getObservedDataFilter().values()
                .stream().anyMatch(v -> v != null)) {
            return log.traceExit(false);
        }
        //Same for observed conditions (it's different from observed *calls")
        if (callFilter.getConditionFilters().stream()
                .anyMatch(cf -> !cf.getObservedCondForParams().isEmpty())) {
            return log.traceExit(false);
        }

        //We would also need the results to be retrieved ordered by gene/anat. entity IDs first,
        //unless one specific gene or one specific anat. entity was requested (checked in the calling methods).
        //Will work if the query is done on one species only, otherwise we can have a same Ensemble Gene ID
        //(when we use the genome of a closely related species), or same anat. entity ID,
        //linked to different species.
        Set<Integer> requestedSpeciesIds = callFilter.getGeneFilters().stream()
                .map(gf -> gf.getSpeciesId()).collect(Collectors.toSet());
        if (requestedSpeciesIds.size() != 1) {
            return log.traceExit(false);
        }

        //We need the query to retrieve expression calls at least in any stage
        if (callFilter.getConditionFilters().stream().anyMatch(cf -> !cf.getDevStageIds().isEmpty())) {
            return log.traceExit(false);
        }

        return log.traceExit(true);
    }

    private static <T> EntityMinMaxRanks<T> getMinMaxRanksFromCallGroup(
            Collection<ExpressionCall> calls, Function<ExpressionCall, T> extractEntityFun) {
        log.traceEntry("{}, {}", calls, extractEntityFun);

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
        return log.traceExit(new EntityMinMaxRanks<T>(minRankExpressedCalls, maxRankExpressedCalls,
                previousEntity));
    }

    private static ExpressionLevelInfo loadExpressionLevelInfo(ExpressionSummary exprSummary,
            BigDecimal rank, BigDecimal expressionScore, BigDecimal maxRankForExpressionScore,
            EntityMinMaxRanks<Condition> anatEntityMinMaxRank, EntityMinMaxRanks<Gene> geneMinMaxRank) {
        log.traceEntry("{}, {}, {}, {}, {}", exprSummary, rank, expressionScore, anatEntityMinMaxRank, geneMinMaxRank);
        return log.traceExit(new ExpressionLevelInfo(rank, expressionScore, maxRankForExpressionScore,
                loadQualExprLevel(exprSummary, rank, geneMinMaxRank),
                loadQualExprLevel(exprSummary, rank, anatEntityMinMaxRank)));
    }
    private static <T> QualitativeExpressionLevel<T> loadQualExprLevel(ExpressionSummary exprSummary, BigDecimal rank,
            EntityMinMaxRanks<T> minMaxRanks) {
        log.traceEntry("{}, {}, {}", exprSummary, rank, minMaxRanks);
        if (ExpressionSummary.NOT_EXPRESSED.equals(exprSummary)) {
            return log.traceExit(new QualitativeExpressionLevel<>(ExpressionLevelCategory.ABSENT, minMaxRanks));
        }
        if (ExpressionSummary.EXPRESSED.equals(exprSummary) && rank != null && minMaxRanks != null) {
            return log.traceExit(new QualitativeExpressionLevel<>(
                    ExpressionLevelCategory.getExpressionLevelCategory(minMaxRanks, rank),
                    minMaxRanks));
        }
        return log.traceExit((QualitativeExpressionLevel<T>) null);
    }

    //*************************************************************************
    // HELPER METHODS CONVERTING INFORMATION FROM Call LAYER to DAO LAYER
    //*************************************************************************
    private static CallDAOFilter convertCallFilterToCallDAOFilter(Map<Integer, Gene> geneMap,
            ExpressionCallFilter callFilter, EnumSet<ConditionDAO.Attribute> condParamCombination) {
        log.traceEntry("{}, {}, {}", geneMap, callFilter, condParamCombination);

        // *********************************
        // Gene and species IDs filters
        //**********************************
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

        // *********************************
        // Condition filter
        //**********************************
        //For condition parameters that are not requested, we map to the root of the respective
        //ontologies when creating the DAOConditionFilter (as of Bgee 15.0, no condition parameters
        //are stored as null values).
        Set<DAOConditionFilter> daoCondFilters = generateDAOConditionFilters(
                callFilter == null? null: callFilter.getConditionFilters(), condParamCombination);

        // *********************************
        // Call observed data filter
        //**********************************
        Set<CallObservedDataDAOFilter> daoObservedDataFilters =
                callFilter.getCallObservedData() == null &&
                callFilter.getObservedDataFilter().isEmpty()? null:

                Collections.singleton(new CallObservedDataDAOFilter(
                        convertDataTypeToDAODataType(callFilter.getDataTypeFilters()),
                        callFilter.getCallObservedData(),
                        callFilter.getObservedDataFilter().entrySet().stream()
                        .collect(Collectors.toMap(
                                e -> convertCondParamAttrToCondDAOAttr(e.getKey()),
                                e -> e.getValue()))
                ));

        // *********************************
        // P-value filters
        //**********************************
        Collection<Set<DAOFDRPValueFilter>> pValueFilters = generateExprQualDAOPValFilters(callFilter);


        // *********************************
        // Final result
        //**********************************
        return log.traceExit(new CallDAOFilter(
                    // gene IDs
                    geneIdFilter, 
                    //species
                    speciesIds,
                    //ConditionFilters
                    daoCondFilters,
                    //CallObservedDataDAOFilters
                    daoObservedDataFilters,
                    //DAOFDRPValueFilters
                    pValueFilters
                ));
    }
    /**
     * 
     * @param callFilter
     * @param serviceAttrs
     * @param serviceOrderingAttrs
     * @return                      An {@code EnumSet} of {@code ConditionDAO.Attribute}s
     *                              allowing {@code DAO}s to determine which tables to target
     *                              in the data source.
     */
    private static EnumSet<ConditionDAO.Attribute> loadConditionParameterCombination(
            ExpressionCallFilter callFilter, Set<Attribute> serviceAttrs,
            Set<OrderingAttribute> serviceOrderingAttrs) {
        log.traceEntry("{}, {}, {}", callFilter, serviceAttrs, serviceOrderingAttrs);

        final EnumSet<ConditionDAO.Attribute> allDAOCondParamAttrs = EnumSet.allOf(ConditionDAO.Attribute.class)
                .stream().filter(a -> a.isConditionParameter())
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(ConditionDAO.Attribute.class)));

        //******************************************************
        // Condition parameters requested in Attributes
        //******************************************************
        EnumSet<ConditionDAO.Attribute> attrs = convertCondParamAttrsToCondDAOAttrs(serviceAttrs);

        //******************************************************
        // Condition parameters requested in OrderingAttributes
        //******************************************************
        EnumSet<ConditionDAO.Attribute> orderingAttrs = 
                convertCondParamOrderingAttrsToCondDAOAttrs(serviceOrderingAttrs);

        //******************************************************
        // Condition parameters requested in Filters
        //******************************************************
        EnumSet<ConditionDAO.Attribute> filterAttrs = EnumSet.noneOf(ConditionDAO.Attribute.class);
        if (callFilter != null) {
            filterAttrs = callFilter.getConditionFilters().stream()
                .flatMap(condFilter -> {
                    Set<ConditionDAO.Attribute> daoAttrs = new HashSet<>();
                    for (ConditionDAO.Attribute daoAttr: allDAOCondParamAttrs) {
                        switch (daoAttr) {
                        case ANAT_ENTITY_ID:
                            if (!condFilter.getAnatEntityIds().isEmpty()) {
                                daoAttrs.add(daoAttr);
                            }
                            break;
                        case STAGE_ID:
                            if (!condFilter.getDevStageIds().isEmpty()) {
                                daoAttrs.add(daoAttr);
                            }
                            break;
                        case CELL_TYPE_ID:
                            if (!condFilter.getCellTypeIds().isEmpty()) {
                                daoAttrs.add(daoAttr);
                            }
                            break;
                        case SEX_ID:
                            if (!condFilter.getSexIds().isEmpty()) {
                                daoAttrs.add(daoAttr);
                            }
                            break;
                        case STRAIN_ID:
                            if (!condFilter.getStrainIds().isEmpty()) {
                                daoAttrs.add(daoAttr);
                            }
                            break;
                        default:
                            throw log.throwing(new UnsupportedOperationException(
                                    "ConditionDAO.Attribute not supported: " + daoAttr));
                        }
                    }
                    return daoAttrs.stream();
                }).collect(Collectors.toCollection(() -> EnumSet.noneOf(ConditionDAO.Attribute.class)));

            filterAttrs.addAll(callFilter.getObservedDataFilter().keySet()
                    .stream().map(a -> convertCondParamAttrToCondDAOAttr(a))
                    .collect(Collectors.toCollection(() -> EnumSet.noneOf(ConditionDAO.Attribute.class))));
        }

        //******************************************************
        // Final step, store all necessary condition parameters
        //******************************************************
        EnumSet<ConditionDAO.Attribute> daoCondParamComb = EnumSet.noneOf(ConditionDAO.Attribute.class);
        daoCondParamComb.addAll(attrs);
        daoCondParamComb.addAll(orderingAttrs);
        daoCondParamComb.addAll(filterAttrs);

        if (daoCondParamComb.isEmpty()) {
            throw log.throwing(new IllegalArgumentException(
                    "No parameter allowing to determine a condition parameter combination"));
        }

        return log.traceExit(daoCondParamComb);
    }

    private static Set<Set<DAOFDRPValueFilter>> generateExprQualDAOPValFilters(
            ExpressionCallFilter callFilter) {
        log.traceEntry("{}", callFilter);

        EnumSet<DAODataType> daoDataTypes = convertDataTypeToDAODataType(callFilter.getDataTypeFilters());
        return log.traceExit(callFilter.getSummaryCallTypeQualityFilter().entrySet().stream()
        .flatMap(e -> {
            SummaryCallType.ExpressionSummary callType = e.getKey();
            SummaryQuality qual = e.getValue();
            //DAOFDRPValueFilters in the inner sets are seen as "AND" conditions,
            //the Sets in the outer Set are seen as "OR" conditions.
            Set<Set<DAOFDRPValueFilter>> pValFilters = new HashSet<>();

            if (callType.equals(SummaryCallType.ExpressionSummary.EXPRESSED)) {
                if (qual.equals(SummaryQuality.GOLD)) {
                    //If minimum GOLD is requested, we only want calls with FDR-corrected p-value <= 0.1
                    pValFilters.add(Collections.singleton(
                            new DAOFDRPValueFilter(PRESENT_HIGH_LESS_THAN_OR_EQUALS_TO,
                                    daoDataTypes,
                                    DAOFDRPValueFilter.Qualifier.LESS_THAN_OR_EQUALS_TO,
                                    DAOPropagationState.SELF_AND_DESCENDANT,
                                    false)));
                } else {
                    //If minimum SILVER is requested, we want calls with FDR-corrected p-value <= 0.05,
                    //we'll get calls SILVER or GOLD
                    pValFilters.add(Collections.singleton(
                            new DAOFDRPValueFilter(PRESENT_LOW_LESS_THAN_OR_EQUALS_TO,
                                    daoDataTypes,
                                    DAOFDRPValueFilter.Qualifier.LESS_THAN_OR_EQUALS_TO,
                                    DAOPropagationState.SELF_AND_DESCENDANT,
                                    false)));
                    //Then, if minimum BRONZE is requested, we also accept calls that are SILVER or GOLD
                    //in a descendant condition. We end up with the following conditions:
                    // * FDR-corrected p-value in condition including sub-conditions <= 0.05
                    //   (SILVER or GOLD)
                    // * OR FDR-corrected p-value in at least one sub-condition <= 0.05 (BRONZE)
                    if (qual.equals(SummaryQuality.BRONZE)) {
                        pValFilters.add(Collections.singleton(
                                new DAOFDRPValueFilter(PRESENT_LOW_LESS_THAN_OR_EQUALS_TO,
                                        daoDataTypes,
                                        DAOFDRPValueFilter.Qualifier.LESS_THAN_OR_EQUALS_TO,
                                        DAOPropagationState.DESCENDANT,
                                        false)));
                    }
                }

            } else if (callType.equals(SummaryCallType.ExpressionSummary.NOT_EXPRESSED)) {
                //For NOT_EXPRESSED, we request that the p-value of the call is non-significant,
                //But also that it is still non-significant when removing data types
                //that we don't trust to produce absent calls (except for BRONZE absent calls).
                //Requirement both for the p-value coming from the condition and its sub-conditions,
                //and the best p-value among the sub-conditions.
                EnumSet<DAODataType> daoDataTypesTrustedForNotExpressed =
                        convertTrustedAbsentDataTypesToDAODataTypes(callFilter.getDataTypeFilters());
                Set<DAOFDRPValueFilter> absentAndFilters = new HashSet<>();
                //If we request SILVER or GOLD, and there is no data type requested
                //that we trust for generating ABSENT calls, we make an impossible condition
                //so that it returns no result
                if (daoDataTypesTrustedForNotExpressed.isEmpty() && !qual.equals(SummaryQuality.BRONZE)) {
                    absentAndFilters.add(new DAOFDRPValueFilter(new BigDecimal("1"),
                                        daoDataTypes,
                                        DAOFDRPValueFilter.Qualifier.GREATER_THAN,
                                        DAOPropagationState.SELF_AND_DESCENDANT,
                                        true));
                } else {
                    if (qual.equals(SummaryQuality.GOLD)) {
                        absentAndFilters.add(new DAOFDRPValueFilter(ABSENT_HIGH_GREATER_THAN,
                                daoDataTypes,
                                DAOFDRPValueFilter.Qualifier.GREATER_THAN,
                                DAOPropagationState.SELF_AND_DESCENDANT,
                                true));
                        //we want the same condition without considering
                        //the data types that we don't trust to produce absent calls
                        absentAndFilters.add(new DAOFDRPValueFilter(ABSENT_HIGH_GREATER_THAN,
                                daoDataTypesTrustedForNotExpressed,
                                DAOFDRPValueFilter.Qualifier.GREATER_THAN,
                                DAOPropagationState.SELF_AND_DESCENDANT,
                                true));
                    } else {
                        absentAndFilters.add(new DAOFDRPValueFilter(ABSENT_LOW_GREATER_THAN,
                                daoDataTypes,
                                DAOFDRPValueFilter.Qualifier.GREATER_THAN,
                                DAOPropagationState.SELF_AND_DESCENDANT,
                                true));
                        //Unless we request BRONZE quality, we want the same condition without considering
                        //the data types that we don't trust to produce absent calls
                        if (qual.equals(SummaryQuality.SILVER)) {
                            absentAndFilters.add(new DAOFDRPValueFilter(ABSENT_LOW_GREATER_THAN,
                                    daoDataTypesTrustedForNotExpressed,
                                    DAOFDRPValueFilter.Qualifier.GREATER_THAN,
                                    DAOPropagationState.SELF_AND_DESCENDANT,
                                    true));
                        }
                    }
                    //in all cases, we don't want PRESENT calls in a sub-condition
                    absentAndFilters.add(new DAOFDRPValueFilter(PRESENT_LOW_LESS_THAN_OR_EQUALS_TO,
                            daoDataTypes,
                            DAOFDRPValueFilter.Qualifier.GREATER_THAN,
                            DAOPropagationState.DESCENDANT,
                            false));
                    //And unless we request BRONZE, we want the same to hold true
                    //with only the data types we trust to produce ABSENT calls
                    if (!qual.equals(SummaryQuality.BRONZE)) {
                        absentAndFilters.add(new DAOFDRPValueFilter(PRESENT_LOW_LESS_THAN_OR_EQUALS_TO,
                                daoDataTypesTrustedForNotExpressed,
                                DAOFDRPValueFilter.Qualifier.GREATER_THAN,
                                DAOPropagationState.DESCENDANT,
                                false));
                    }
                }
                pValFilters.add(absentAndFilters);
            }
            return pValFilters.stream();
        }).collect(Collectors.toSet()));
    }

    private static EnumSet<ConditionDAO.Attribute> convertCondParamOrderingAttrsToCondDAOAttrs(
            Set<OrderingAttribute> attrs) {
        log.traceEntry("{}", attrs);
        return log.traceExit(attrs.stream()
                .filter(a -> a.isConditionParameter())
                .map(a -> {
                    switch (a) {
                        case ANAT_ENTITY_ID:
                            return ConditionDAO.Attribute.ANAT_ENTITY_ID;
                        case DEV_STAGE_ID: 
                            return ConditionDAO.Attribute.STAGE_ID;
                        case CELL_TYPE_ID: 
                            return ConditionDAO.Attribute.CELL_TYPE_ID; 
                        case SEX_ID: 
                            return ConditionDAO.Attribute.SEX_ID; 
                        case STRAIN_ID: 
                            return ConditionDAO.Attribute.STRAIN_ID; 
                        default: 
                            throw log.throwing(new UnsupportedOperationException(
                                "Condition parameter not supported: " + a));
                    }
                }).collect(Collectors.toCollection(() -> EnumSet.noneOf(ConditionDAO.Attribute.class))));
    }

    private static Set<GlobalExpressionCallDAO.AttributeInfo> convertServiceAttrToGlobalExprDAOAttr(
        Set<Attribute> attributes, ExpressionCallFilter callFilter) {
        log.traceEntry("{}, {}", attributes, callFilter);

        EnumSet<DAODataType> daoDataTypes = convertDataTypeToDAODataType(callFilter.getDataTypeFilters());
        EnumSet<DAODataType> daoDataTypesTrustedForAbsentCalls =
                convertTrustedAbsentDataTypesToDAODataTypes(callFilter.getDataTypeFilters());

        return log.traceExit(attributes.stream().flatMap(attr -> {
            if (attr.isConditionParameter()) {

                return Stream.of(new GlobalExpressionCallDAO.AttributeInfo(
                        GlobalExpressionCallDAO.Attribute.GLOBAL_CONDITION_ID));

            } else if (attr.equals(CallService.Attribute.P_VALUE_INFO_ALL_DATA_TYPES) ||
                    attr.equals(CallService.Attribute.CALL_TYPE) ||
                    attr.equals(CallService.Attribute.DATA_QUALITY)) {

                Set<GlobalExpressionCallDAO.AttributeInfo> pValAttributes = new HashSet<>();
                pValAttributes.add(new GlobalExpressionCallDAO.AttributeInfo(
                                GlobalExpressionCallDAO.Attribute.FDR_P_VALUE_COND_INFO,
                                daoDataTypes));
                pValAttributes.add(new GlobalExpressionCallDAO.AttributeInfo(
                        GlobalExpressionCallDAO.Attribute.FDR_P_VALUE_DESCENDANT_COND_INFO,
                        daoDataTypes));
                if (!daoDataTypesTrustedForAbsentCalls.isEmpty()) {
                    pValAttributes.add(new GlobalExpressionCallDAO.AttributeInfo(
                            GlobalExpressionCallDAO.Attribute.FDR_P_VALUE_COND_INFO,
                            daoDataTypesTrustedForAbsentCalls));
                    pValAttributes.add(new GlobalExpressionCallDAO.AttributeInfo(
                            GlobalExpressionCallDAO.Attribute.FDR_P_VALUE_DESCENDANT_COND_INFO,
                            daoDataTypesTrustedForAbsentCalls));
                }
                return pValAttributes.stream();

            } else if (attr.equals(CallService.Attribute.P_VALUE_INFO_EACH_DATA_TYPE)) {

                return daoDataTypes.stream()
                        .flatMap(dt -> Stream.of(
                                new GlobalExpressionCallDAO.AttributeInfo(
                                        GlobalExpressionCallDAO.Attribute.FDR_P_VALUE_COND_INFO,
                                        EnumSet.of(dt)),
                                new GlobalExpressionCallDAO.AttributeInfo(
                                        GlobalExpressionCallDAO.Attribute.FDR_P_VALUE_DESCENDANT_COND_INFO,
                                        EnumSet.of(dt))));

            } else if (attr.equals(CallService.Attribute.GENE)) {

                return Stream.of(new GlobalExpressionCallDAO.AttributeInfo(
                        GlobalExpressionCallDAO.Attribute.BGEE_GENE_ID));

            } else if (attr.equals(CallService.Attribute.OBSERVED_DATA)) {

                return Stream.of(new GlobalExpressionCallDAO.AttributeInfo(
                        GlobalExpressionCallDAO.Attribute.DATA_TYPE_OBSERVED_DATA,
                        daoDataTypes));

            } else if (attr.equals(CallService.Attribute.MEAN_RANK) ||
                    attr.equals(CallService.Attribute.EXPRESSION_SCORE) ||
                    attr.equals(CallService.Attribute.GENE_QUAL_EXPR_LEVEL) ||
                    attr.equals(CallService.Attribute.ANAT_ENTITY_QUAL_EXPR_LEVEL)) {

                return Stream.of(new GlobalExpressionCallDAO.AttributeInfo(
                        GlobalExpressionCallDAO.Attribute.MEAN_RANK,
                        daoDataTypes));

            } else if (attr.equals(CallService.Attribute.DATA_TYPE_RANK_INFO)) {

                return Stream.of(new GlobalExpressionCallDAO.AttributeInfo(
                        GlobalExpressionCallDAO.Attribute.DATA_TYPE_RANK_INFO,
                        daoDataTypes));

            } else {
                throw log.throwing(new IllegalStateException(
                            "Unsupported Attributes from CallService: " + attr));
            }
        }).collect(Collectors.toSet()));
    }

    private static LinkedHashMap<GlobalExpressionCallDAO.OrderingAttributeInfo, DAO.Direction>
    convertServiceOrderingAttrToGlobalExprDAOOrderingAttr(
            LinkedHashMap<CallService.OrderingAttribute, Service.Direction> orderingAttributes,
            ExpressionCallFilter callFilter) {
        log.traceEntry("{}, {}", orderingAttributes, callFilter);

        EnumSet<DAODataType> daoDataTypes = convertDataTypeToDAODataType(
                callFilter.getDataTypeFilters());

        return log.traceExit(orderingAttributes.entrySet().stream().collect(Collectors.toMap(
                e -> {
                    switch (e.getKey()) {
                        case GENE_ID: 
                            return new GlobalExpressionCallDAO.OrderingAttributeInfo(
                                    GlobalExpressionCallDAO.OrderingAttribute.PUBLIC_GENE_ID);
                        case ANAT_ENTITY_ID:
                            return new GlobalExpressionCallDAO.OrderingAttributeInfo(
                                    GlobalExpressionCallDAO.OrderingAttribute.ANAT_ENTITY_ID);
                        case DEV_STAGE_ID: 
                            return new GlobalExpressionCallDAO.OrderingAttributeInfo(
                                    GlobalExpressionCallDAO.OrderingAttribute.STAGE_ID);
                        case CELL_TYPE_ID: 
                            return new GlobalExpressionCallDAO.OrderingAttributeInfo(
                                    GlobalExpressionCallDAO.OrderingAttribute.CELL_TYPE_ID);
                        case SEX_ID: 
                            return new GlobalExpressionCallDAO.OrderingAttributeInfo(
                                    GlobalExpressionCallDAO.OrderingAttribute.SEX_ID);
                        case STRAIN_ID: 
                            return new GlobalExpressionCallDAO.OrderingAttributeInfo(
                                    GlobalExpressionCallDAO.OrderingAttribute.STRAIN_ID);
                        case MEAN_RANK:
                            return new GlobalExpressionCallDAO.OrderingAttributeInfo(
                                    GlobalExpressionCallDAO.OrderingAttribute.MEAN_RANK,
                                    daoDataTypes);
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
                () -> new LinkedHashMap<GlobalExpressionCallDAO.OrderingAttributeInfo, DAO.Direction>())));
    }

    protected static EnumSet<DAODataType> convertDataTypeToDAODataType(Set<DataType> dts) 
            throws IllegalStateException{
        log.traceEntry("{}", dts);
        
        if (dts == null || dts.isEmpty()) {
            return log.traceExit(EnumSet.allOf(DAODataType.class));
        }
        return log.traceExit(dts.stream()
            .map(dt -> {
                switch(dt) {
                    case AFFYMETRIX: 
                        return log.traceExit(DAODataType.AFFYMETRIX);
                    case EST: 
                        return log.traceExit(DAODataType.EST);
                    case IN_SITU: 
                        return log.traceExit(DAODataType.IN_SITU);
                    case RNA_SEQ: 
                        return log.traceExit(DAODataType.RNA_SEQ);
                    case FULL_LENGTH: 
                        return log.traceExit(DAODataType.FULL_LENGTH);
                    default: 
                        throw log.throwing(new IllegalStateException("Unsupported DAODataType: " + dt));
                }
        }).collect(Collectors.toCollection(() -> EnumSet.noneOf(DAODataType.class))));
    }
    private static EnumSet<DAODataType> convertTrustedAbsentDataTypesToDAODataTypes(
            Set<DataType> dts) throws IllegalStateException {
        log.traceEntry("{}", dts);

        //Find DataTypes that can be trusted for absent calls. Maybe there will be none among
        //the requested data types. So we need to convert to DAODataTypes in two steps,
        //by checking if dataTypesToConsider is empty, because the method
        //convertDataTypeToDAODataType returns all DAODataTypes when the provided argument
        //of DataTypes is empty or null.
        Set<DataType> dataTypesToConsider = (dts == null || dts.isEmpty()? EnumSet.allOf(DataType.class):
            dts).stream().filter(dt -> dt.isTrustedForAbsentCalls()).collect(Collectors.toSet());
        return log.traceExit(dataTypesToConsider.isEmpty()? EnumSet.noneOf(DAODataType.class):
            convertDataTypeToDAODataType(dataTypesToConsider));
    }

    /**
     * Recompute the information of an {@code ExpressionCall} considering only a specific
     * {@code DataType}.
     * <p>
     * Note that the {@code QualitativeExpressionLevel}s are not recomputed (see {@link
     * org.bgee.model.expressiondata.baseelements.ExpressionLevelInfo#getQualExprLevelRelativeToGene()
     * ExpressionLevelInfo#getQualExprLevelRelativeToGene()} and {@link
     * org.bgee.model.expressiondata.baseelements.ExpressionLevelInfo#getQualExprLevelRelativeToAnatEntity()
     * ExpressionLevelInfo#getQualExprLevelRelativeToAnatEntity()} in the {@code ExpressionLevelInfo}
     * returned by method {@link ExpressionCall#getExpressionLevelInfo()}), nor the source
     * {@code ExpressionCall}s (see {@link ExpressionCall#getSourceCalls()}).
     *
     * @param call      The {@code ExpressionCall} to recompute data using {@code dataType}
     * @param dataType  The {@code DataType} to consider for recomputing information for {@code call}.
     * @return          A new {@code ExpressionCall} corresponding to the information from {@code call}
     *                  considering only {@code dataType}. {@code null} if there was no data from
     *                  {@code dataType} supporting {@code call}.
     */
    //XXX: note, maybe all the methods to compute information, such as inferDataPropagation,
    //inferSummaryQuality, etc, and this method, should be dispatched in the corresponding classes
    //rather than all being in this CallService class.
    public static ExpressionCall deriveCallForDataType(ExpressionCall call,
            DataType dataType) {
        log.traceEntry("{}, {}", call, dataType);

        if (dataType == null) {
            throw log.throwing(new IllegalArgumentException("A DataType must be provided"));
        }
        if (call == null) {
            throw log.throwing(new IllegalArgumentException("An ExpressionCall must be provided"));
        }
        if (call.getCallData() == null || call.getCallData().isEmpty()) {
            throw log.throwing(new IllegalArgumentException("Cannot derive call, no CallData stored"));
        }
        Set<ExpressionCallData> consideredCallData = call.getCallData().stream()
                .filter(ecd -> dataType.equals(ecd.getDataType()))
                .collect(Collectors.toSet());
        if (consideredCallData.isEmpty()) {
            return log.traceExit((ExpressionCall) null);
        }
        assert consideredCallData.size() == 1;
        ExpressionCallData callData = consideredCallData.iterator().next();

        ExpressionSummary exprSummary = null;
        SummaryQuality summaryQual = null;
        Set<FDRPValue> fdrPValues = callData.getFDRPValue() == null? null: Collections.singleton(
                    new FDRPValue(callData.getFDRPValue(), EnumSet.of(dataType)));
        //Unless the call was produced by requested only this data type,
        //it won't be possible to retrieve the ID of the sub-condition with the best p-value
        if (log.isWarnEnabled() && call.getBestDescendantPValues().stream()
                .anyMatch(p -> p.getCondition() != null)) {
            log.warn("It is not possible to retrieve the ID of the sub-condition "
                    + "with the best descendant FDR p-value when deriving a call for a data type.");
        }
        Set<FDRPValueCondition> bestDescendantFdrPValues =
                callData.getBestDescendantFDRPValue() == null? null: Collections.singleton(
                    new FDRPValueCondition(callData.getBestDescendantFDRPValue(),
                            EnumSet.of(dataType), null));

        if (call.getSummaryCallType() != null || call.getSummaryQuality() != null) {
            if (call.getPValues() == null && call.getBestDescendantPValues() == null) {
                throw log.throwing(new IllegalArgumentException(
                        "Cannot derive call, no getFDRPValue or getBestDescendantFDRPValue stored "
                        + "in the CallData for data type " + dataType + ". You must request "
                        + "the Attribute P_VALUE_INFO_EACH_DATA_TYPE when retrieving calls "
                        + "to have access to them."));
            }
            if (callData.getFDRPValue() == null && callData.getBestDescendantFDRPValue() == null) {
                return log.traceExit((ExpressionCall) null);
            }
            Entry<ExpressionSummary, SummaryQuality> callQual = inferSummaryCallTypeAndQuality(
                    fdrPValues, bestDescendantFdrPValues, EnumSet.of(dataType));
            exprSummary = call.getSummaryCallType() != null? callQual.getKey(): null;
            summaryQual = call.getSummaryQuality() != null? callQual.getValue(): null;
        }

        return log.traceExit(new ExpressionCall(call.getGene(), call.getCondition(),
                call.getDataPropagation() == null? null: inferDataPropagation(consideredCallData),
                fdrPValues, bestDescendantFdrPValues,
                exprSummary,
                summaryQual,
                consideredCallData,
                loadExpressionLevelInfo(exprSummary, callData.getNormalizedRank(),
                        call.getExpressionScore() == null? null:
                            computeExpressionScore(callData.getNormalizedRank(),
                                    call.getExpressionLevelInfo().getMaxRankForExpressionScore()),
                        call.getExpressionScore() == null? null:
                            call.getExpressionLevelInfo().getMaxRankForExpressionScore(),
                        null, null),
                null));
    }

    //*************************************************************************
    // METHODS MAPPING GlobalExpressionCallTOs TO ExpressionCalls
    //*************************************************************************
    private static ExpressionCall mapGlobalCallTOToExpressionCall(GlobalExpressionCallTO globalCallTO, 
            Map<Integer, Gene> geneMap, Map<Integer, Condition> condMap,
            ExpressionCallFilter callFilter, Map<Integer, ConditionRankInfoTO> maxRankPerSpecies,
            Map<Condition, EntityMinMaxRanks<Condition>> anatEntityMinMaxRanks,
            Map<Gene, EntityMinMaxRanks<Gene>> geneMinMaxRanks,
            Set<CallService.Attribute> attrs) {
        log.traceEntry("{}, {}, {}, {}, {}, {}, {}, {}", globalCallTO, geneMap, condMap, callFilter, 
                maxRankPerSpecies, anatEntityMinMaxRanks, geneMinMaxRanks, attrs);

        //***********************************
        // ExpressionCallData
        //***********************************
        Set<ExpressionCallData> callData = mapGlobalCallTOToExpressionCallData(globalCallTO,
                attrs, callFilter.getDataTypeFilters());

        //***********************************
        // Gene and Condition
        //***********************************
        Condition cond = condMap.get(globalCallTO.getConditionId());
        Gene gene = geneMap.get(globalCallTO.getBgeeGeneId());

        //***********************************
        // Info needed for expression score
        //***********************************
        assert maxRankPerSpecies == null || maxRankPerSpecies.size() <= 1 || cond != null || gene != null;
        assert cond == null || gene == null || cond.getSpeciesId() == gene.getSpecies().getId();
        assert (attrs == null || attrs.isEmpty() || attrs.contains(Attribute.EXPRESSION_SCORE)) &&
        maxRankPerSpecies != null && !maxRankPerSpecies.isEmpty() ||
                attrs != null && !attrs.isEmpty() && !attrs.contains(Attribute.EXPRESSION_SCORE);
        ConditionRankInfoTO maxRankInfo = null;
        if (maxRankPerSpecies != null) {
            if (maxRankPerSpecies.size() == 1) {
                maxRankInfo = maxRankPerSpecies.values().iterator().next();
            } else {
                int speciesId = cond != null? cond.getSpeciesId(): gene.getSpecies().getId();
                maxRankInfo = maxRankPerSpecies.get(speciesId);
            }
            if (maxRankInfo == null) {
                throw log.throwing(new IllegalStateException("No max rank could be retrieved for call " + globalCallTO));
            }
        }
        EnumSet<DAODataType> requestedDAODataTypes = convertDataTypeToDAODataType(
                callFilter.getDataTypeFilters());

        //Retrieve mean rank for the requested data types if needed
        BigDecimal meanRank = null;
        if (attrs == null || attrs.isEmpty() || attrs.contains(Attribute.EXPRESSION_SCORE) ||
                attrs.contains(Attribute.MEAN_RANK) ||
                attrs.contains(Attribute.EXPRESSION_SCORE) ||
                attrs.contains(Attribute.ANAT_ENTITY_QUAL_EXPR_LEVEL) ||
                attrs.contains(Attribute.GENE_QUAL_EXPR_LEVEL)) {
            meanRank = globalCallTO.getMeanRanks()
                    .stream().filter(r -> r.getDataTypes().equals(requestedDAODataTypes))
                    .map(r -> r.getMeanRank())
                    .findAny()
                    .orElseThrow(() -> new IllegalStateException("No matching mean rank found"));
        }
        //Compute expression score
        BigDecimal expressionScore = attrs == null || attrs.isEmpty() ||
                attrs.contains(Attribute.EXPRESSION_SCORE)?
                        computeExpressionScore(meanRank,
                                GLOBAL_RANK? maxRankInfo.getGlobalMaxRank(): maxRankInfo.getMaxRank()):
                null;

        //***********************************
        // FDR-corrected p-values, ExpressionSummary and SummaryQuality
        //***********************************
        Set<FDRPValue> fdrPValues = attrs == null || attrs.isEmpty() ||
                attrs.contains(Attribute.P_VALUE_INFO_ALL_DATA_TYPES) ||
                attrs.contains(Attribute.CALL_TYPE) ||
                attrs.contains(Attribute.DATA_QUALITY)?
                        globalCallTO.getPValues().stream()
                        .map(p -> new FDRPValue(p.getFdrPValue(),
                                mapDAODataTypeToDataType(p.getDataTypes(),
                                callFilter.getDataTypeFilters())))
                        .collect(Collectors.toSet()):
                        null;
       Set<FDRPValueCondition> bestDescendantFdrPValues = attrs == null || attrs.isEmpty() ||
               attrs.contains(Attribute.P_VALUE_INFO_ALL_DATA_TYPES) ||
               attrs.contains(Attribute.CALL_TYPE) ||
               attrs.contains(Attribute.DATA_QUALITY)?
                       globalCallTO.getBestDescendantPValues().stream()
                       .map(p -> new FDRPValueCondition(p.getFdrPValue(),
                               mapDAODataTypeToDataType(p.getDataTypes(),
                               callFilter.getDataTypeFilters()), null))
                       .collect(Collectors.toSet()):
                       null;
        ExpressionSummary exprSummary = null;
        SummaryQuality summaryQual = null;
        if (attrs == null || attrs.isEmpty() || attrs.contains(Attribute.CALL_TYPE) ||
                attrs.contains(Attribute.DATA_QUALITY)) {
            Entry<ExpressionSummary, SummaryQuality> callQual = inferSummaryCallTypeAndQuality(
                    fdrPValues, bestDescendantFdrPValues, callFilter.getDataTypeFilters());
            if (callQual == null) {
                throw log.throwing(new IllegalStateException(
                        "Invalid data to compute ExpressionSummary and SummaryQuality, fdrPValues: "
                        + fdrPValues + ", bestDescendantFdrPValues: " + bestDescendantFdrPValues
                        + ", requestedDataTypes: " + callFilter.getDataTypeFilters()));
            }
            exprSummary = attrs == null || attrs.isEmpty() || attrs.contains(Attribute.CALL_TYPE)?
                    callQual.getKey(): null;
            summaryQual = attrs == null || attrs.isEmpty() || attrs.contains(Attribute.DATA_QUALITY)?
                    callQual.getValue(): null;
        }

       //***********************************
       // Build new ExpressionCall
       //***********************************
        return log.traceExit(new ExpressionCall(
            attrs == null || attrs.isEmpty() || attrs.contains(Attribute.GENE)?
                    gene: null,
            attrs == null || attrs.isEmpty() || attrs.stream().anyMatch(a -> a.isConditionParameter())?
                    cond: null,
            attrs == null || attrs.isEmpty() || attrs.contains(Attribute.OBSERVED_DATA)?
                    inferDataPropagation(callData): null,
            attrs == null || attrs.isEmpty() || attrs.contains(Attribute.P_VALUE_INFO_ALL_DATA_TYPES)?
                    fdrPValues: null,
            attrs == null || attrs.isEmpty() || attrs.contains(Attribute.P_VALUE_INFO_ALL_DATA_TYPES)?
                    bestDescendantFdrPValues: null,
            exprSummary,
            summaryQual,
            attrs == null || attrs.isEmpty() || attrs.contains(Attribute.DATA_TYPE_RANK_INFO) ||
            attrs.contains(Attribute.OBSERVED_DATA) ||
            attrs.contains(Attribute.P_VALUE_INFO_EACH_DATA_TYPE) ||
            attrs.contains(Attribute.P_VALUE_INFO_ALL_DATA_TYPES)?
                    callData: null,
            attrs == null || attrs.isEmpty() || attrs.contains(Attribute.MEAN_RANK) ||
            attrs.contains(Attribute.EXPRESSION_SCORE) ||
            attrs.contains(Attribute.ANAT_ENTITY_QUAL_EXPR_LEVEL) ||
            attrs.contains(Attribute.GENE_QUAL_EXPR_LEVEL)?
                    loadExpressionLevelInfo(exprSummary, meanRank, expressionScore,
                            maxRankInfo == null? null:
                                GLOBAL_RANK? maxRankInfo.getGlobalMaxRank(): maxRankInfo.getMaxRank(),
                            anatEntityMinMaxRanks == null? null:
                                anatEntityMinMaxRanks.get(cond),
                            geneMinMaxRanks == null? null: geneMinMaxRanks.get(gene)): null));
    }

    private static BigDecimal computeExpressionScore(BigDecimal rank, BigDecimal maxRank) {
        log.traceEntry("{}, {}", rank, maxRank);
        if (maxRank == null) {
            throw log.throwing(new IllegalArgumentException("Max rank must be provided"));
        }
        if (rank == null) {
            log.debug("Rank is null, cannot compute expression score");
            return log.traceExit((BigDecimal) null);
        }
        if (rank.compareTo(new BigDecimal("0")) <= 0 || maxRank.compareTo(new BigDecimal("0")) <= 0) {
            throw log.throwing(new IllegalArgumentException("Rank and max rank cannot be less than or equal to 0"));
        }
        if (rank.compareTo(maxRank) > 0) {
            throw log.throwing(new IllegalArgumentException("Rank cannot be greater than maxRank. Rank: " + rank
                    + " - maxRank: " + maxRank));
        }

        BigDecimal invertedRank = maxRank.add(new BigDecimal("1")).subtract(rank);
        BigDecimal expressionScore = invertedRank.multiply(new BigDecimal("100")).divide(maxRank, 5, RoundingMode.HALF_UP);
        //We want expression score to be at least greater than EXPRESSION_SCORE_MIN_VALUE
        if (expressionScore.compareTo(EXPRESSION_SCORE_MIN_VALUE) < 0) {
            expressionScore = EXPRESSION_SCORE_MIN_VALUE;
        }
        if (expressionScore.compareTo(EXPRESSION_SCORE_MAX_VALUE) > 0) {
            log.warn("Expression score should always be lower or equals to " + EXPRESSION_SCORE_MAX_VALUE
                    + ". The value was " + expressionScore + "and was then manually updated to "
                    + EXPRESSION_SCORE_MAX_VALUE + ".");
            expressionScore = EXPRESSION_SCORE_MAX_VALUE;
        }
        return log.traceExit(expressionScore);
    }

    private static Set<ExpressionCallData> mapGlobalCallTOToExpressionCallData(
            GlobalExpressionCallTO globalCallTO, Set<Attribute> attrs, Set<DataType> requestedDataTypes) {
        log.traceEntry("{}, {}, {}", globalCallTO, attrs, requestedDataTypes);
        
        if (globalCallTO.getCallDataTOs() == null || globalCallTO.getCallDataTOs().isEmpty()) {
            return log.traceExit((Set<ExpressionCallData>) null);
        }

        return log.traceExit(globalCallTO.getCallDataTOs().stream().map(cdTO -> {
            DataType dt = mapDAODataTypeToDataType(Collections.singleton(cdTO.getDataType()),
                    requestedDataTypes).iterator().next();

            //Of note, for now we have not implemented the possibility to retrieve
            //selfObservationCount and descendantObservationCount from the data source,
            //this might change in the future.

            boolean getRankInfo = attrs == null || attrs.isEmpty() ||
                    attrs.contains(Attribute.DATA_TYPE_RANK_INFO);
            boolean getDataProp = attrs == null || attrs.isEmpty() ||
                    attrs.contains(Attribute.OBSERVED_DATA);
            //This info of FDR-corrected p-values is stored in the ExpressionCall,
            //but when we also have the p-values per data type, we also store them
            //in the related ExpressionCallData objet.
            //And since we could use P_VALUE_INFO_ALL_DATA_TYPES, but request only one data type,
            //we also accept this attribute
            boolean getPValues = attrs == null || attrs.isEmpty() ||
                    attrs.contains(Attribute.P_VALUE_INFO_EACH_DATA_TYPE);
            boolean getObsCount = attrs == null || attrs.isEmpty() ||
                    attrs.contains(Attribute.P_VALUE_INFO_EACH_DATA_TYPE) ||
                    attrs.contains(Attribute.P_VALUE_INFO_ALL_DATA_TYPES) ||
                    attrs.contains(Attribute.CALL_TYPE) ||
                    attrs.contains(Attribute.DATA_QUALITY);
            //Old note: The following assertion was incorrect: as of Bgee 14.1, if the call is not observed
            //(propagation only), there is no associated rank. Even when we'll have globalRanks,
            //as of Bgee 14.2, there can still be an absent call propagated from a parent,
            //and thus with no rank associated.
            //New note: as of Bgee 15.0, we have global ranks, PLUS we don't propagate absent calls
            //from parents anymore, so we should ALWAYS have a rank, and we reenable this assert
            assert !getRankInfo || cdTO.getRank() != null && cdTO.getRankNorm() != null &&
                    cdTO.getWeightForMeanRank() != null;
            assert !getDataProp || cdTO.getDataPropagation() != null &&
                    !cdTO.getDataPropagation().isEmpty() && cdTO.isConditionObservedData() != null;

            return new ExpressionCallData(dt,
                    getPValues? cdTO.getFDRPValue(): null,
                    getPValues? cdTO.getBestDescendantFDRPValue(): null,
                    getObsCount? cdTO.getSelfObservationCount(): null,
                    getObsCount? cdTO.getDescendantObservationCount(): null,
                    getRankInfo? cdTO.getRank(): null,
                    getRankInfo? cdTO.getRankNorm(): null,
                    getRankInfo? cdTO.getWeightForMeanRank(): null,
                    getDataProp? mapDAOCallDataTOToDataPropagation(cdTO): null);
        }).collect(Collectors.toSet()));
    }

    private static Set<DataType> mapDAODataTypeToDataType(Set<DAODataType> dts,
            Set<DataType> requestedDataTypes) throws IllegalArgumentException, IllegalStateException {
        log.traceEntry("{}, {}", dts, requestedDataTypes);

        Set<DataType> mappedDataTypes = null;
        if (dts == null || dts.isEmpty()) {
            mappedDataTypes = EnumSet.allOf(DataType.class);
        } else {
            mappedDataTypes = dts.stream()
                    .map(dt -> {
                        switch(dt) {
                        case AFFYMETRIX:
                            return log.traceExit(DataType.AFFYMETRIX);
                        case EST:
                            return log.traceExit(DataType.EST);
                        case IN_SITU:
                            return log.traceExit(DataType.IN_SITU);
                        case RNA_SEQ:
                            return log.traceExit(DataType.RNA_SEQ);
                        case FULL_LENGTH:
                            return log.traceExit(DataType.FULL_LENGTH);
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

        return log.traceExit(mappedDataTypes);
    }

    private static PropagationState mapDAOPropStateToPropState(DAOPropagationState propState) {
        log.traceEntry("{}", propState);
        if (propState == null) {
            return log.traceExit((PropagationState) null);
        }
        switch(propState) {
        case ALL:
            return log.traceExit(PropagationState.ALL);
        case SELF:
            return log.traceExit(PropagationState.SELF);
        case ANCESTOR:
            return log.traceExit(PropagationState.ANCESTOR);
        case DESCENDANT:
            return log.traceExit(PropagationState.DESCENDANT);
        case SELF_AND_ANCESTOR:
            return log.traceExit(PropagationState.SELF_AND_ANCESTOR);
        case SELF_AND_DESCENDANT:
            return log.traceExit(PropagationState.SELF_AND_DESCENDANT);
        case ANCESTOR_AND_DESCENDANT:
            return log.traceExit(PropagationState.ANCESTOR_AND_DESCENDANT);
        default:
            throw log.throwing(new IllegalStateException("Unsupported DAOPropagationState: "
                    + propState));
        }
    }

    //*************************************************************************
    // HELPER METHODS FOR INFERENCES
    //*************************************************************************

    private static DataPropagation inferDataPropagation(Set<ExpressionCallData> callData) {
        log.traceEntry("{}", callData);

        if (callData == null || callData.isEmpty() || callData.stream()
                .anyMatch(cd -> cd.getDataPropagation() == null ||
                        cd.getDataPropagation().isIncludingObservedData() == null)) {
            throw log.throwing(new IllegalArgumentException(
                    "Missing info for inferring data propagation. CallData: " + callData));
        }
        return log.traceExit(callData.stream()
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
        log.traceEntry("{}", callDataTO);

        if (callDataTO == null || callDataTO.getDataPropagation() == null ||
                callDataTO.getDataPropagation().isEmpty()) {
            return log.traceExit((DataPropagation) null);
        }

        PropagationState anatEntityPropState = null;
        PropagationState stagePropState = null;
        PropagationState cellTypePropState = null;
        PropagationState sexPropState = null;
        PropagationState strainPropState = null;
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
            case CELL_TYPE_ID:
                cellTypePropState = mapDAOPropStateToPropState(
                        observedDataEntry.getValue());
                break;
            case SEX_ID:
                sexPropState = mapDAOPropStateToPropState(
                        observedDataEntry.getValue());
                break;
            case STRAIN_ID:
                strainPropState = mapDAOPropStateToPropState(
                        observedDataEntry.getValue());
                break;
            default:
                throw log.throwing(new IllegalStateException(
                        "ConditionDAO.Attribute not supported for DataPropagation: "
                        + observedDataEntry.getKey()));
            }
        }
        assert anatEntityPropState != null || stagePropState != null || cellTypePropState != null
                || sexPropState != null || strainPropState != null;
        

        Boolean observedData = callDataTO.isConditionObservedData();

        return log.traceExit(new DataPropagation(anatEntityPropState, stagePropState, cellTypePropState,
                sexPropState, strainPropState, observedData));
    }
    protected static DataPropagation mergeDataPropagations(DataPropagation dataProp1,
            DataPropagation dataProp2) {
        log.traceEntry("{}, {}", dataProp1, dataProp2);

        if (dataProp1 == null && dataProp2 == null) {
            return log.traceExit(DATA_PROPAGATION_IDENTITY);
        }
        if (dataProp1 == null || dataProp1.equals(DATA_PROPAGATION_IDENTITY)) {
            return log.traceExit(dataProp2);
        }
        if (dataProp2 == null || dataProp2.equals(DATA_PROPAGATION_IDENTITY)) {
            return log.traceExit(dataProp1);
        }

        PropagationState anatEntityPropState = mergePropagationStates(
                dataProp1.getAnatEntityPropagationState(), dataProp2.getAnatEntityPropagationState());
        PropagationState stagePropState = mergePropagationStates(
                dataProp1.getDevStagePropagationState(), dataProp2.getDevStagePropagationState());
        PropagationState cellTypePropState = mergePropagationStates(
                dataProp1.getCellTypePropagationState(), dataProp2.getCellTypePropagationState());
        PropagationState sexPropState = mergePropagationStates(
                dataProp1.getSexPropagationState(), dataProp2.getSexPropagationState());
        PropagationState strainPropState = mergePropagationStates(
                dataProp1.getStrainPropagationState(), dataProp2.getStrainPropagationState());

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

        return log.traceExit(new DataPropagation(anatEntityPropState, stagePropState, cellTypePropState,
                sexPropState, strainPropState, retrievedObservedData));
    }
    private static PropagationState mergePropagationStates(PropagationState state1,
            PropagationState state2) {
        log.traceEntry("{}, {}", state1, state2);

        if (state1 != null && !ALLOWED_PROP_STATES.contains(state1)) {
            throw log.throwing(new IllegalArgumentException("Unsupported PropagationState: "
                    + state1));
        }
        if (state2 != null && !ALLOWED_PROP_STATES.contains(state2)) {
            throw log.throwing(new IllegalArgumentException("Unsupported PropagationState: "
                    + state2));
        }

        if (state1 == null && state2 == null) {
            return log.traceExit((PropagationState) null);
        }
        if (state1 == null) {
            return log.traceExit(state2);
        }
        if (state2 == null) {
            return log.traceExit(state1);
        }

        if (state1.equals(state2)) {
            return log.traceExit(state1);
        }
        Set<PropagationState> propStates = EnumSet.of(state1, state2);
        if (propStates.contains(PropagationState.ALL)) {
            return log.traceExit(PropagationState.ALL);
        }

        if (propStates.contains(PropagationState.SELF)) {
            if (propStates.contains(PropagationState.ANCESTOR)) {
                return log.traceExit(PropagationState.SELF_AND_ANCESTOR);
            } else if (propStates.contains(PropagationState.DESCENDANT)) {
                return log.traceExit(PropagationState.SELF_AND_DESCENDANT);
            } else if (propStates.contains(PropagationState.SELF_AND_ANCESTOR)) {
                return log.traceExit(PropagationState.SELF_AND_ANCESTOR);
            } else if (propStates.contains(PropagationState.SELF_AND_DESCENDANT)) {
                return log.traceExit(PropagationState.SELF_AND_DESCENDANT);
            } else if (propStates.contains(PropagationState.ANCESTOR_AND_DESCENDANT)) {
                return log.traceExit(PropagationState.ALL);
            } else {
                throw log.throwing(new AssertionError("Case not covered, " + propStates));
            }
        } else if (propStates.contains(PropagationState.ANCESTOR)) {
            if (propStates.contains(PropagationState.DESCENDANT)) {
                return log.traceExit(PropagationState.ANCESTOR_AND_DESCENDANT);
            } else if (propStates.contains(PropagationState.SELF_AND_ANCESTOR)) {
                return log.traceExit(PropagationState.SELF_AND_ANCESTOR);
            } else if (propStates.contains(PropagationState.SELF_AND_DESCENDANT)) {
                return log.traceExit(PropagationState.ALL);
            } else if (propStates.contains(PropagationState.ANCESTOR_AND_DESCENDANT)) {
                return log.traceExit(PropagationState.ANCESTOR_AND_DESCENDANT);
            } else {
                throw log.throwing(new AssertionError("Case not covered, " + propStates));
            }
        } else if (propStates.contains(PropagationState.DESCENDANT)) {
            if (propStates.contains(PropagationState.SELF_AND_ANCESTOR)) {
                return log.traceExit(PropagationState.ALL);
            } else if (propStates.contains(PropagationState.SELF_AND_DESCENDANT)) {
                return log.traceExit(PropagationState.SELF_AND_DESCENDANT);
            } else if (propStates.contains(PropagationState.ANCESTOR_AND_DESCENDANT)) {
                return log.traceExit(PropagationState.ANCESTOR_AND_DESCENDANT);
            } else {
                throw log.throwing(new AssertionError("Case not covered, " + propStates));
            }
        } else if (propStates.contains(PropagationState.SELF_AND_ANCESTOR)) {
            if (propStates.contains(PropagationState.SELF_AND_DESCENDANT)) {
                return log.traceExit(PropagationState.ALL);
            } else if (propStates.contains(PropagationState.ANCESTOR_AND_DESCENDANT)) {
                return log.traceExit(PropagationState.ALL);
            } else {
                throw log.throwing(new AssertionError("Case not covered, " + propStates));
            }
        } else if (propStates.contains(PropagationState.SELF_AND_DESCENDANT)) {
            if (propStates.contains(PropagationState.ANCESTOR_AND_DESCENDANT)) {
                return log.traceExit(PropagationState.ALL);
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
    static <T extends FDRPValue> Entry<ExpressionSummary, SummaryQuality>
    inferSummaryCallTypeAndQuality(Set<FDRPValue> fdrPValues, Set<T> bestDescendantFdrPValues,
            Set<DataType> requestedDataTypes) {
        log.traceEntry("{}, {}, {}", fdrPValues, bestDescendantFdrPValues, requestedDataTypes);

        EnumSet<DataType> realRequestedDataTypes =
                requestedDataTypes == null || requestedDataTypes.isEmpty()?
                EnumSet.allOf(DataType.class): EnumSet.copyOf(requestedDataTypes);
        EnumSet<DataType> requestedDataTypesTrustedForAbsentCalls = realRequestedDataTypes.stream()
                .filter(dt -> dt.isTrustedForAbsentCalls())
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(DataType.class)));
        BigDecimal dataTypesPVal = null;
        BigDecimal dataTypesTrustedForAbsentPVal = null;
        for (FDRPValue pVal: fdrPValues) {
            if (pVal.getDataTypes().equals(realRequestedDataTypes)) {
                assert dataTypesPVal == null:
                    "There should be only one FDR p-value matching data type selection";
                dataTypesPVal = pVal.getFDRPValue();
            }
            if (!requestedDataTypesTrustedForAbsentCalls.isEmpty() &&
                    pVal.getDataTypes().equals(requestedDataTypesTrustedForAbsentCalls)) {
                assert dataTypesTrustedForAbsentPVal == null:
                    "There should be only one FDR p-value matching data type selection";
                dataTypesTrustedForAbsentPVal = pVal.getFDRPValue();
            }
        }
        BigDecimal dataTypesBestDescendantPVal = null;
        BigDecimal dataTypesTrustedForAbsentBestDescendantPVal = null;
        if(bestDescendantFdrPValues != null) {
            for (FDRPValue pVal: bestDescendantFdrPValues) {
                if (pVal.getDataTypes().equals(realRequestedDataTypes)) {
                    assert dataTypesBestDescendantPVal == null:
                        "There should be only one FDR p-value matching data type selection";
                    dataTypesBestDescendantPVal = pVal.getFDRPValue();
                }
                if (!requestedDataTypesTrustedForAbsentCalls.isEmpty() &&
                        pVal.getDataTypes().equals(requestedDataTypesTrustedForAbsentCalls)) {
                    assert dataTypesTrustedForAbsentBestDescendantPVal == null:
                        "There should be only one FDR p-value matching data type selection";
                    dataTypesTrustedForAbsentBestDescendantPVal = pVal.getFDRPValue();
                }
            }
        }
        log.debug("{} - {} - {} - {} - {} - {}", dataTypesPVal, requestedDataTypesTrustedForAbsentCalls,
                dataTypesTrustedForAbsentPVal, bestDescendantFdrPValues, dataTypesBestDescendantPVal,
                dataTypesTrustedForAbsentBestDescendantPVal);
        String exceptionMsg = "It should have been possible to infer "
                + "ExpressionSummary and SummaryQuality. dataTypesPVal: " + dataTypesPVal
                + ", dataTypesTrustedForAbsentPVal: " + dataTypesTrustedForAbsentPVal
                + ", dataTypesBestDescendantPVal: " + dataTypesBestDescendantPVal
                + ", dataTypesTrustedForAbsentBestDescendantPVal: "
                + dataTypesTrustedForAbsentBestDescendantPVal;
        if (dataTypesPVal == null ||
                !(bestDescendantFdrPValues == null || bestDescendantFdrPValues.isEmpty()) && 
                dataTypesBestDescendantPVal == null) {
            throw log.throwing(new IllegalStateException(exceptionMsg));
        }

        //The order of the comparison is important
        if (dataTypesPVal.compareTo(PRESENT_HIGH_LESS_THAN_OR_EQUALS_TO) <= 0) {
            return log.traceExit(new AbstractMap.SimpleEntry<>(
                    ExpressionSummary.EXPRESSED, SummaryQuality.GOLD));
        }
        if (dataTypesPVal.compareTo(PRESENT_LOW_LESS_THAN_OR_EQUALS_TO) <= 0) {
            return log.traceExit(new AbstractMap.SimpleEntry<>(
                    ExpressionSummary.EXPRESSED, SummaryQuality.SILVER));
        }
        if (dataTypesBestDescendantPVal != null &&
                dataTypesBestDescendantPVal.compareTo(PRESENT_LOW_LESS_THAN_OR_EQUALS_TO) <= 0) {
            return log.traceExit(new AbstractMap.SimpleEntry<>(
                    ExpressionSummary.EXPRESSED, SummaryQuality.BRONZE));
        }
        //If there is PRESENT LOW expression in a sub-condition when only considering
        //the data types trusted for ABSENT calls, the resulting SummaryQuality
        //cannot be better than BRONZE.
        //(the case where there is a PRESENT LOW expression in a sub-condition when considering
        //all requested data types is already addressed in the previous condition).
        //Also if there is no data type trusted for absent calls, it can't be better than bronze
        boolean absCallCannotBeBetterThanBronze = requestedDataTypesTrustedForAbsentCalls.isEmpty() ||
                //In case there was no data from any trusted data type
                dataTypesTrustedForAbsentPVal == null ||
                //Or in case there is a present call in a sub-condition when using trusted data types
                dataTypesTrustedForAbsentBestDescendantPVal != null &&
                        dataTypesTrustedForAbsentBestDescendantPVal
                        .compareTo(PRESENT_LOW_LESS_THAN_OR_EQUALS_TO) <= 0;
        if (dataTypesTrustedForAbsentPVal != null &&
                dataTypesPVal.compareTo(ABSENT_HIGH_GREATER_THAN) > 0 &&
                dataTypesTrustedForAbsentPVal.compareTo(ABSENT_HIGH_GREATER_THAN) > 0) {
            return log.traceExit(new AbstractMap.SimpleEntry<>(
                    ExpressionSummary.NOT_EXPRESSED,
                    absCallCannotBeBetterThanBronze? SummaryQuality.BRONZE: SummaryQuality.GOLD));
        }
        if (dataTypesPVal.compareTo(ABSENT_LOW_GREATER_THAN) > 0) {
            if (dataTypesTrustedForAbsentPVal != null &&
                    dataTypesTrustedForAbsentPVal.compareTo(ABSENT_LOW_GREATER_THAN) > 0) {
                return log.traceExit(new AbstractMap.SimpleEntry<>(
                        ExpressionSummary.NOT_EXPRESSED,
                        absCallCannotBeBetterThanBronze? SummaryQuality.BRONZE: SummaryQuality.SILVER));
            }
            return log.traceExit(new AbstractMap.SimpleEntry<>(
                    ExpressionSummary.NOT_EXPRESSED, SummaryQuality.BRONZE));
        }
        throw log.throwing(new IllegalStateException(exceptionMsg));
    }
}