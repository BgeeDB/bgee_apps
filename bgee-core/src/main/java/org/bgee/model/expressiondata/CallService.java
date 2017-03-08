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
import org.bgee.model.dao.api.expressiondata.CallDAOFilter;
import org.bgee.model.dao.api.expressiondata.CallDataDAOFilter;
import org.bgee.model.dao.api.expressiondata.ConditionDAO;
import org.bgee.model.dao.api.expressiondata.DAOConditionFilter;
import org.bgee.model.dao.api.expressiondata.GlobalExpressionCallDAO;
import org.bgee.model.dao.api.expressiondata.GlobalExpressionCallDAO.GlobalExpressionCallTO;
import org.bgee.model.expressiondata.Call.DiffExpressionCall;
import org.bgee.model.expressiondata.Call.ExpressionCall;
import org.bgee.model.expressiondata.CallData.ExpressionCallData;
import org.bgee.model.expressiondata.CallFilter.DiffExpressionCallFilter;
import org.bgee.model.expressiondata.CallFilter.ExpressionCallFilter;
import org.bgee.model.expressiondata.baseelements.CallType;
import org.bgee.model.expressiondata.baseelements.CountType;
import org.bgee.model.expressiondata.baseelements.DataPropagation.PropagationState;
import org.bgee.model.expressiondata.baseelements.DataQuality;
import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.model.expressiondata.baseelements.SummaryCallType;
import org.bgee.model.expressiondata.baseelements.SummaryCallType.ExpressionSummary;
import org.bgee.model.expressiondata.baseelements.SummaryQuality;
import org.bgee.model.gene.Gene;
import org.bgee.model.species.Species;
import org.bgee.model.species.TaxonomyFilter;

/**
 * A {@link Service} to obtain {@link Call} objects. 
 * Users should use the {@link org.bgee.model.ServiceFactory} to obtain {@code CallService}s.
 * 
 * @author  Frederic Bastian
 * @author  Valentine Rech de Laval
 * @version Bgee 14, Mar. 2017
 * @since   Bgee 13, Oct. 2015
 */
public class CallService extends CommonService {
    private final static Logger log = LogManager.getLogger(CallService.class.getName());
    
    /**
     * An {@code int} that is the minimum count showing expression with a low quality
     * for {@code SummaryQuality.BRONZE).
     */
    private final static int MIN_LOW_BRONZE = 1;
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


    //XXX: Enum class for fields of Call to populate? 
    //(e.g., GENE, ANAT_ENTITY, STAGE, DATA). But this means that we once again 
    //"duplicate" the concepts in the Condition class. 
    
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
         * @return  The {@code boolean} defining whether this attribute corresponds 
         *          to a condition parameter (anat entity, stage, sex, strain), allowing to 
         *          determine which condition to target for queries.
         */
        public boolean isConditionParameter() {
            return this.conditionParameter;
        }
    }

    public static enum OrderingAttribute implements Service.OrderingAttribute {
        //WARNING: these Enums must be declared in the order that will be used
        // by default when returning data. See CallService#getDefaultOrdering()
        GENE_ID, ANAT_ENTITY_ID, DEV_STAGE_ID, GLOBAL_RANK;
    }

    /**
     * @param serviceFactory            The {@code ServiceFactory} to be used to obtain {@code Service}s 
     *                                  and {@code DAOManager}.
     * @throws IllegalArgumentException If {@code serviceFactory} is {@code null}.
     */
    public CallService(ServiceFactory serviceFactory) {
        super(serviceFactory);
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
     * through an {@code ExpressionCallFilter}.
     * 
     * @param speciesId             An {@code Integer} that is the ID of the species 
     *                              for which to return the {@code ExpressionCall}s.
     * @param callFilter            An {@code ExpressionCallFilter} allowing 
     *                              to filter retrieving of data.
     * @param attributes            A {@code Collection} of {@code Attribute}s defining the
     *                              attributes to populate in the returned {@code ExpressionCall}s.
     *                              If {@code null} or empty, all attributes are populated. 
     * @param orderingAttributes    A {@code LinkedHashMap} where keys are 
     *                              {@code CallService.OrderingAttribute}s defining the attributes
     *                              used to order the returned {@code ExpressionCall}s, 
     *                              the associated value being a {@code Service.Direction} defining 
     *                              whether the ordering should be ascendant or descendant.
     * @param doPropagation         A {@code boolean} defining whether propagation should be done or not.
     * @return                      A {@code Stream} of {@code ExpressionCall}s that are propagated
     *                              and reconciled expression calls.
     * @throws IllegalArgumentException If {@code callFilter} or {@code speciesID} are null or empty.
     */
    public Stream<ExpressionCall> loadExpressionCalls(Integer speciesId, 
            ExpressionCallFilter callFilter, Collection<Attribute> attributes, 
            LinkedHashMap<OrderingAttribute, Service.Direction> orderingAttributes)
                    throws IllegalArgumentException {        
        log.entry(speciesId, callFilter, attributes, orderingAttributes);

        final Set<Attribute> clonedAttrs = Collections.unmodifiableSet(
            attributes == null? EnumSet.noneOf(Attribute.class): EnumSet.copyOf(attributes));
        final LinkedHashMap<OrderingAttribute, Service.Direction> clonedOrderingAttrs = 
            orderingAttributes == null? new LinkedHashMap<>(): new LinkedHashMap<>(orderingAttributes);
        
        // Sanity checks        
        // XXX: why a call filter should be provided?
        if (callFilter == null) {
            throw log.throwing(new IllegalArgumentException("A CallFilter must be provided."));
        }
        if (speciesId == null || speciesId <= 0) {
            throw log.throwing(new IllegalArgumentException("A species ID must be provided"));
        }
        if (callFilter.isObservedDataOnly()) {
            if (!clonedAttrs.contains(Attribute.CALL_DATA)) {
                throw log.throwing(new IllegalArgumentException(
                    "CallData is required in attributes if filtering on propagation is requested."));
            }
            if (callFilter.getSummaryCallTypeFilter() != null) {
                throw log.throwing(new IllegalArgumentException(
                    "CallData is required in attributes if filtering on summary call type is requested."));
            }
            if (callFilter.getSummaryQualityFilter() != null) {
                throw log.throwing(new IllegalArgumentException(
                    "CallData is required in attributes if filtering on summary quality is requested."));               
            }
        }

        // Retrieve species
        Set<Integer> clnSpId =  Collections.singleton(speciesId);
        final Set<Species> speciesSet = this.getServiceFactory().getSpeciesService()
            .loadSpeciesByIds(clnSpId, false);
        if (speciesSet.size() != 1) {
            throw new IllegalArgumentException("Provided species not found in data source");
        }
        Species species = speciesSet.stream().findAny().get();
        
        // Define condition parameters
        Set<ConditionDAO.Attribute> condParams = 
                this.convertCallServiceAttrsToConditionDAOAttr(clonedAttrs);
        
        // Retrieve conditions by condition IDs
        final Map<Integer, Condition> condMap = Collections.unmodifiableMap(
            this.getDaoManager().getConditionDAO()
                .getConditionsBySpeciesIds(clnSpId, condParams, condParams).stream()
                .collect(Collectors.toMap(cTO -> cTO.getId(), cTO -> mapConditionTOToCondition(cTO))));
        assert !condMap.isEmpty();
        
        // Retrieve genes by Bgee IDs
        final Map<Integer, Gene> geneMap = this.getDaoManager().getGeneDAO()
            .getGenesBySpeciesIds(clnSpId, 
                    callFilter.getGeneFilter() == null? null : callFilter.getGeneFilter().getGeneIds())
                .stream()
                .collect(Collectors.toMap(
                    gTO -> gTO.getId(),
                    gTO -> CommonService.mapGeneTOToGene(gTO, species)));
        assert !geneMap.isEmpty();
        
        // Retrieve calls
        Stream<ExpressionCall> calls = this.performsGlobalExprCallQuery(speciesId, geneMap,
                callFilter, condParams, clonedAttrs, clonedOrderingAttrs)
            .map(to -> mapGlobalCallTOToExpressionCall(to, species, geneMap, condMap, clonedAttrs))
            // We filter ExpressionCalls according to callFilter
            .filter(c -> callFilter.test(c))
            // TODO: do we need to order here or it is the job of the DAO? I did not think about it.
            .sorted(CallService.convertServiceOrdering(clonedOrderingAttrs));
        
        return log.exit(calls);
    }
    
    public Stream<DiffExpressionCall> loadDiffExpressionCalls(Integer speciesId, 
            DiffExpressionCallFilter callFilter) {
        log.entry(speciesId, callFilter);
        throw log.throwing(new UnsupportedOperationException("Load of diff. expression calls not implemented yet"));
    }
    
    //*************************************************************************
    // HELPER METHODS FOR Call LAYER
    //*************************************************************************            
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
    public static Comparator<ExpressionCall> convertServiceOrdering(
            LinkedHashMap<CallService.OrderingAttribute, Service.Direction> orderingAttributes) {
        log.entry(orderingAttributes);
        
        LinkedHashMap<OrderingAttribute, Service.Direction> clonedOrderingAttrs = 
                orderingAttributes == null || orderingAttributes.isEmpty()?
                        getDefaultOrdering() : new LinkedHashMap<>(orderingAttributes);
        
        Comparator<ExpressionCall> comparator = null;
        for (Entry<CallService.OrderingAttribute, Service.Direction> entry: clonedOrderingAttrs.entrySet()) {
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

    private static LinkedHashMap<OrderingAttribute, Direction> getDefaultOrdering() {
        log.entry();
        LinkedHashMap<CallService.OrderingAttribute, Service.Direction> serviceOrdering = 
                new LinkedHashMap<>();
        for (CallService.OrderingAttribute attr: EnumSet.allOf(CallService.OrderingAttribute.class)) {
            serviceOrdering.put(attr, Service.Direction.ASC);
        }
        return log.exit(serviceOrdering);
    }

    //*************************************************************************
    // METHODS PERFORMING THE QUERIES TO THE DAOs
    //*************************************************************************
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
     * @return                      The {@code Stream} of {@code ExpressionCall}s.
     * @throws IllegalArgumentException If the {@code callFilter} provided define multiple 
     *                                  expression propagation states requested.
     */
    private Stream<GlobalExpressionCallTO> performsGlobalExprCallQuery(Integer speciesId, 
            Map<Integer, Gene> geneMap, ExpressionCallFilter callFilter,
            Set<ConditionDAO.Attribute> condParameters, Set<Attribute> attributes,
            LinkedHashMap<OrderingAttribute, Service.Direction> orderingAttributes)
                    throws IllegalArgumentException {
        log.entry(speciesId, callFilter, geneMap, attributes, orderingAttributes);

        log.trace("Retrieving global expression data...");
        
        final GlobalExpressionCallDAO dao = this.getDaoManager().getGlobalExpressionCallDAO();
        Stream<GlobalExpressionCallTO> calls = dao.getGlobalExpressionCalls(Arrays.asList(
                //generate an ExpressionCallDAOFilter from callFilter 
                new CallDAOFilter(
                    // gene IDs
                    geneMap.keySet(), 
                    //species
                    Arrays.asList(speciesId), 
                    //ConditionFilters
                    callFilter.getConditionFilters().stream()
                        .map(condFilter -> new DAOConditionFilter(
                            condFilter.getAnatEntityIds(),
                            condFilter.getDevStageIds()))
                        .collect(Collectors.toSet())
                        )),
                this.convertCallFilterToCallDataDAOFilter(callFilter),
                // Condition parameters
                condParameters,
                // Attributes
                this.convertCallServiceAttrToGlobalExprDAOAttr(attributes, callFilter.getDataTypeFilter()))
            //retrieve the Stream resulting from the query. Note that the query is not executed 
            //as long as the Stream is not consumed (lazy-loading).
            .stream();

        return log.exit(calls);
    }

    //*************************************************************************
    // HELPER METHODS CONVERTING INFORMATION FROM Call LAYER to DAO LAYER
    //*************************************************************************            
    private Set<ConditionDAO.Attribute> convertCallServiceAttrsToConditionDAOAttr(
            Set<CallService.Attribute> serviceAttrs) {
        log.entry(serviceAttrs);
    
        Set<Attribute> paramAttrs = serviceAttrs.stream()
            .filter(a -> a.isConditionParameter())
            .collect(Collectors.toSet());
        
        if (paramAttrs.isEmpty()) {
            paramAttrs = Arrays.stream(CallService.Attribute.values())
                .filter(a -> a.isConditionParameter()).collect(Collectors.toSet());
        }
        return log.exit(paramAttrs.stream()
            .filter(a -> a.isConditionParameter())
            .map(a -> {
                switch (a) {
                    case ANAT_ENTITY_ID:
                        return ConditionDAO.Attribute.ANAT_ENTITY_ID;
                    case DEV_STAGE_ID: 
                        return ConditionDAO.Attribute.STAGE_ID;                        
                    default: 
                        throw log.throwing(new UnsupportedOperationException(
                            "Condition parameter not taken into account: " + a));
                }
            }).collect(Collectors.toSet()));
    }

    private static final Map<DataType, Set<GlobalExpressionCallDAO.Attribute>> GLOBAL_EXPR_ATTR_TO_DATA_TYPE = Stream.of(
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
            GlobalExpressionCallDAO.Attribute.AFFYMETRIX_EXP_ABSENT_LOW_TOTAL_COUNT))), 
        new SimpleEntry<>(DataType.EST, new HashSet<>(Arrays.asList(
            GlobalExpressionCallDAO.Attribute.EST_LIB_PRESENT_HIGH_SELF_COUNT,
            GlobalExpressionCallDAO.Attribute.EST_LIB_PRESENT_LOW_SELF_COUNT, 
            GlobalExpressionCallDAO.Attribute.EST_LIB_PRESENT_HIGH_DESCENDANT_COUNT,
            GlobalExpressionCallDAO.Attribute.EST_LIB_PRESENT_LOW_DESCENDANT_COUNT, 
            GlobalExpressionCallDAO.Attribute.EST_LIB_PRESENT_HIGH_TOTAL_COUNT,
            GlobalExpressionCallDAO.Attribute.EST_LIB_PRESENT_LOW_TOTAL_COUNT))), 
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
            GlobalExpressionCallDAO.Attribute.IN_SITU_EXP_ABSENT_LOW_TOTAL_COUNT))), 
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
            GlobalExpressionCallDAO.Attribute.RNA_SEQ_EXP_ABSENT_LOW_TOTAL_COUNT))))
        .collect(Collectors.toMap(SimpleEntry::getKey, SimpleEntry::getValue));

    private Set<CallDataDAOFilter> convertCallFilterToCallDataDAOFilter(
                ExpressionCallFilter callFilter) {
        log.entry(callFilter);
        throw log.throwing(new UnsupportedOperationException("Not implement yet"));
        
//        boolean isExpression = callFilter.getSummaryCallTypeFilter() == null || 
//            SummaryCallType.ExpressionSummary.EXPRESSED.equals(callFilter.getSummaryCallTypeFilter());
//        boolean isNoExpression = callFilter.getSummaryCallTypeFilter() == null || 
//            SummaryCallType.ExpressionSummary.NOT_EXPRESSED.equals(callFilter.getSummaryCallTypeFilter());
//
//        Set<CallDataDAOFilter> filters = new HashSet<>();
//        if (callFilter.getSummaryQualityFilter() == null 
//            || SummaryQuality.BRONZE.equals(callFilter.getSummaryQualityFilter())) {
//            if (isExpression) {
//                filters.add(new CallDataDAOFilter(callFilter.isObservedDataOnly(), 
//                    0, MIN_LOW_BRONZE, 0, 0, convertDataTypeToDAODataType(callFilter.getDataTypeFilter())));
//            }
//            if (isNoExpression) {
//                filters.add(new CallDataDAOFilter(callFilter.isObservedDataOnly(), 
//                    0, 0, 0, MIN_LOW_BRONZE, convertDataTypeToDAODataType(callFilter.getDataTypeFilter())));
//            }
//        } else if (SummaryQuality.SILVER.equals(callFilter.getSummaryQualityFilter())) {
//            if (isExpression) {
//                filters.add(new CallDataDAOFilter(callFilter.isObservedDataOnly(), 
//                    MIN_HIGH_SILVER, 0, 0, 0, convertDataTypeToDAODataType(callFilter.getDataTypeFilter())));
//                filters.add(new CallDataDAOFilter(callFilter.isObservedDataOnly(), 
//                    0, MIN_LOW_SILVER, 0, 0, convertDataTypeToDAODataType(callFilter.getDataTypeFilter())));
//            }
//            if (isNoExpression) {
//                filters.add(new CallDataDAOFilter(callFilter.isObservedDataOnly(), 
//                    0, 0, MIN_HIGH_SILVER, 0, convertDataTypeToDAODataType(callFilter.getDataTypeFilter())));
//                filters.add(new CallDataDAOFilter(callFilter.isObservedDataOnly(), 
//                    0, 0, 0, MIN_LOW_SILVER, convertDataTypeToDAODataType(callFilter.getDataTypeFilter())));
//            }
//        } else if (SummaryQuality.GOLD.equals(callFilter.getSummaryQualityFilter())) {
//            if (isExpression) {
//                filters.add(new CallDataDAOFilter(callFilter.isObservedDataOnly(), 
//                    MIN_HIGH_GOLD, 0, 0, 0, convertDataTypeToDAODataType(callFilter.getDataTypeFilter())));
//            }
//            if (isNoExpression) {
//                filters.add(new CallDataDAOFilter(callFilter.isObservedDataOnly(), 
//                    0, 0, MIN_HIGH_GOLD, 0, convertDataTypeToDAODataType(callFilter.getDataTypeFilter())));
//            }
//        }
//        return log.exit(filters);
    }

    private Collection<GlobalExpressionCallDAO.Attribute> convertCallServiceAttrToGlobalExprDAOAttr(
        Set<Attribute> attributes, Set<DataType> dataTypesRequested) {
        log.entry(attributes, dataTypesRequested);
        
        return log.exit(attributes.stream().flatMap(attr -> {
            switch (attr) {
                case GENE: 
                    return Stream.of(GlobalExpressionCallDAO.Attribute.BGEE_GENE_ID);
                case ANAT_ENTITY_ID: 
                case DEV_STAGE_ID: 
                    return Stream.of(GlobalExpressionCallDAO.Attribute.CONDITION_ID);
                    //Whether we need to get a quality level over all requested data types, 
                    //or the detailed quality level per data type, it's the same DAO attributes that we need. 
                case DATA_QUALITY:
                case CALL_DATA: 
                    return dataTypesRequested.stream().map(type -> Optional.ofNullable(GLOBAL_EXPR_ATTR_TO_DATA_TYPE.get(type))
                        //bug of javac for type inference, we need to type the exception 
                        //explicitly to RuntimeException,
                        //see http://stackoverflow.com/questions/25523375/java8-lambdas-and-exceptions
                        .<RuntimeException>orElseThrow(() -> log.throwing(new IllegalStateException(
                            "Unsupported DataType: " + type))))
                        .flatMap(Set::stream);
                case OBSERVED_DATA:
                    return Stream.of(GlobalExpressionCallDAO.Attribute.AFFYMETRIX_EXP_PRESENT_HIGH_SELF_COUNT,
                        GlobalExpressionCallDAO.Attribute.AFFYMETRIX_EXP_PRESENT_LOW_SELF_COUNT, 
                        GlobalExpressionCallDAO.Attribute.AFFYMETRIX_EXP_ABSENT_HIGH_SELF_COUNT,
                        GlobalExpressionCallDAO.Attribute.AFFYMETRIX_EXP_ABSENT_LOW_SELF_COUNT,
                        GlobalExpressionCallDAO.Attribute.RNA_SEQ_EXP_PRESENT_LOW_SELF_COUNT,
                        GlobalExpressionCallDAO.Attribute.RNA_SEQ_EXP_PRESENT_LOW_SELF_COUNT, 
                        GlobalExpressionCallDAO.Attribute.RNA_SEQ_EXP_ABSENT_HIGH_SELF_COUNT,
                        GlobalExpressionCallDAO.Attribute.RNA_SEQ_EXP_ABSENT_LOW_SELF_COUNT,
                        GlobalExpressionCallDAO.Attribute.EST_LIB_PRESENT_HIGH_SELF_COUNT,
                        GlobalExpressionCallDAO.Attribute.EST_LIB_PRESENT_LOW_SELF_COUNT,
                        GlobalExpressionCallDAO.Attribute.IN_SITU_EXP_PRESENT_HIGH_SELF_COUNT,
                        GlobalExpressionCallDAO.Attribute.IN_SITU_EXP_PRESENT_LOW_SELF_COUNT, 
                        GlobalExpressionCallDAO.Attribute.IN_SITU_EXP_ABSENT_HIGH_SELF_COUNT,
                        GlobalExpressionCallDAO.Attribute.IN_SITU_EXP_ABSENT_LOW_SELF_COUNT);
                case RANK: 
                    return Stream.of(GlobalExpressionCallDAO.Attribute.GLOBAL_MEAN_RANK);
                case CALL_TYPE:
                    return Stream.of();
                default: 
                    throw log.throwing(new IllegalStateException("Unsupported Attributes from CallService: "
                        + attr));
            }
        }).collect(Collectors.toCollection(() -> EnumSet.noneOf(GlobalExpressionCallDAO.Attribute.class))));
    }

    private Set<CallDataDAOFilter.DataType> convertDataTypeToDAODataType(Set<DataType> dts) 
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
    private ExpressionCall mapGlobalCallTOToExpressionCall(GlobalExpressionCallTO globalCallTO, 
            Species species, Map<Integer, Gene> geneMap, Map<Integer, Condition> condMap,
            Set<CallService.Attribute> attrs) {
        log.entry(globalCallTO, species, geneMap, condMap, attrs);
        
        Set<ExpressionCallData> callData = mapGlobalCallTOToExpressionCallData(globalCallTO);

        return log.exit(new ExpressionCall(geneMap.get(globalCallTO.getBgeeGeneId()), 
            condMap.get(globalCallTO.getConditionId()),
            this.inferIsObservedData(callData),
            this.inferSummaryCallType(callData),
            this.inferSummaryQuality(callData),
            callData,
            globalCallTO.getGlobalMeanRank()));
    }
    
    private Set<ExpressionCallData> mapGlobalCallTOToExpressionCallData(
            GlobalExpressionCallTO globalCallTO) {
        log.entry(globalCallTO);
        
        Set<ExpressionCallData> allCallData = new HashSet<>();
        for (DataType dt : DataType.values()) {
            ExpressionCallData callData;
            Map<CountType, Integer> counts;
            switch (dt) {
                case AFFYMETRIX:
                    counts = new HashMap<>();
                    counts.put(new CountType(CallType.Expression.EXPRESSED, DataQuality.HIGH,
                        PropagationState.SELF), globalCallTO.getAffymetrixExpPresentHighSelfCount());
                    counts.put(new CountType(CallType.Expression.EXPRESSED, DataQuality.LOW,
                        PropagationState.SELF), globalCallTO.getAffymetrixExpPresentLowSelfCount());
                    counts.put(new CountType(CallType.Expression.NOT_EXPRESSED, DataQuality.HIGH,
                        PropagationState.SELF), globalCallTO.getAffymetrixExpAbsentHighSelfCount());
                    counts.put(new CountType(CallType.Expression.NOT_EXPRESSED, DataQuality.LOW,
                        PropagationState.SELF), globalCallTO.getAffymetrixExpAbsentLowSelfCount());
                    counts.put(new CountType(CallType.Expression.EXPRESSED, DataQuality.HIGH,
                        PropagationState.DESCENDANT), globalCallTO.getAffymetrixExpPresentHighDescendantCount());
                    counts.put(new CountType(CallType.Expression.EXPRESSED, DataQuality.LOW,
                        PropagationState.DESCENDANT), globalCallTO.getAffymetrixExpPresentLowDescendantCount());
                    counts.put(new CountType(CallType.Expression.NOT_EXPRESSED, DataQuality.HIGH,
                        PropagationState.ANCESTOR), globalCallTO.getAffymetrixExpAbsentHighParentCount());
                    counts.put(new CountType(CallType.Expression.NOT_EXPRESSED, DataQuality.LOW,
                        PropagationState.ANCESTOR), globalCallTO.getAffymetrixExpAbsentLowParentCount());
                    counts.put(new CountType(CallType.Expression.EXPRESSED, DataQuality.HIGH,
                        PropagationState.ALL), globalCallTO.getAffymetrixExpPresentHighTotalCount());
                    counts.put(new CountType(CallType.Expression.EXPRESSED, DataQuality.LOW,
                        PropagationState.ALL), globalCallTO.getAffymetrixExpPresentLowTotalCount());
                    counts.put(new CountType(CallType.Expression.NOT_EXPRESSED, DataQuality.HIGH,
                        PropagationState.ALL), globalCallTO.getAffymetrixExpAbsentHighTotalCount());
                    counts.put(new CountType(CallType.Expression.NOT_EXPRESSED, DataQuality.LOW,
                        PropagationState.ALL), globalCallTO.getAffymetrixExpAbsentLowTotalCount());
                    callData = new ExpressionCallData(dt, counts, globalCallTO.getAffymetrixExpPropagatedCount(),
                        globalCallTO.getAffymetrixMeanRank(), globalCallTO.getAffymetrixMeanRankNorm(),
                        globalCallTO.getAffymetrixDistinctRankSum());
                    break;
                case EST:
                    counts = new HashMap<>();
                    counts.put(new CountType(CallType.Expression.EXPRESSED, DataQuality.HIGH, PropagationState.SELF),
                        globalCallTO.getESTLibPresentHighSelfCount());
                    counts.put(new CountType(CallType.Expression.EXPRESSED, DataQuality.LOW, PropagationState.SELF),
                        globalCallTO.getESTLibPresentLowSelfCount());
                    counts.put(new CountType(CallType.Expression.EXPRESSED, DataQuality.HIGH, PropagationState.DESCENDANT),
                        globalCallTO.getESTLibPresentHighDescendantCount());
                    counts.put(new CountType(CallType.Expression.EXPRESSED, DataQuality.LOW, PropagationState.DESCENDANT),
                        globalCallTO.getESTLibPresentLowDescendantCount());
                    counts.put(new CountType(CallType.Expression.EXPRESSED, DataQuality.HIGH, PropagationState.ALL),
                        globalCallTO.getESTLibPresentHighTotalCount());
                    counts.put(new CountType(CallType.Expression.EXPRESSED, DataQuality.LOW, PropagationState.ALL),
                        globalCallTO.getESTLibPresentLowTotalCount());
                    callData = new ExpressionCallData(dt, counts, globalCallTO.getESTLibPropagatedCount(),
                        globalCallTO.getESTRank(), globalCallTO.getESTRankNorm(),
                        null); // rankSum
                    break;
                case IN_SITU:
                    counts = new HashMap<>();
                    counts.put(new CountType(CallType.Expression.EXPRESSED, DataQuality.HIGH, PropagationState.SELF),
                        globalCallTO.getInSituExpPresentHighSelfCount());
                    counts.put(new CountType(CallType.Expression.EXPRESSED, DataQuality.LOW, PropagationState.SELF),
                        globalCallTO.getInSituExpPresentLowSelfCount());
                    counts.put(new CountType(CallType.Expression.NOT_EXPRESSED, DataQuality.HIGH, PropagationState.SELF),
                        globalCallTO.getInSituExpAbsentHighSelfCount());
                    counts.put(new CountType(CallType.Expression.NOT_EXPRESSED, DataQuality.LOW, PropagationState.SELF),
                        globalCallTO.getInSituExpAbsentLowSelfCount());
                    counts.put(new CountType(CallType.Expression.EXPRESSED, DataQuality.HIGH, PropagationState.DESCENDANT),
                        globalCallTO.getInSituExpPresentHighDescendantCount());
                    counts.put(new CountType(CallType.Expression.EXPRESSED, DataQuality.LOW, PropagationState.DESCENDANT),
                        globalCallTO.getInSituExpPresentLowDescendantCount());
                    counts.put(new CountType(CallType.Expression.NOT_EXPRESSED, DataQuality.HIGH, PropagationState.ANCESTOR),
                        globalCallTO.getInSituExpAbsentHighParentCount());
                    counts.put(new CountType(CallType.Expression.NOT_EXPRESSED, DataQuality.LOW, PropagationState.ANCESTOR),
                        globalCallTO.getInSituExpAbsentLowParentCount());
                    counts.put(new CountType(CallType.Expression.EXPRESSED, DataQuality.HIGH, PropagationState.ALL),
                        globalCallTO.getInSituExpPresentHighTotalCount());
                    counts.put(new CountType(CallType.Expression.EXPRESSED, DataQuality.LOW, PropagationState.ALL),
                        globalCallTO.getInSituExpPresentLowTotalCount());
                    counts.put(new CountType(CallType.Expression.NOT_EXPRESSED, DataQuality.HIGH, PropagationState.ALL),
                        globalCallTO.getInSituExpAbsentHighTotalCount());
                    counts.put(new CountType(CallType.Expression.NOT_EXPRESSED, DataQuality.LOW, PropagationState.ALL),
                        globalCallTO.getInSituExpAbsentLowTotalCount());
                    callData = new ExpressionCallData(dt, counts, globalCallTO.getInSituExpPropagatedCount(),
                        globalCallTO.getInSituRank(), globalCallTO.getInSituRankNorm(),
                        null);  // rankSum
                    break;
                case RNA_SEQ:
                    counts = new HashMap<>();
                    counts.put(new CountType(CallType.Expression.EXPRESSED, DataQuality.HIGH, PropagationState.SELF),
                        globalCallTO.getRNASeqExpPresentHighSelfCount());
                    counts.put(new CountType(CallType.Expression.EXPRESSED, DataQuality.LOW, PropagationState.SELF),
                        globalCallTO.getRNASeqExpPresentLowSelfCount());
                    counts.put(new CountType(CallType.Expression.NOT_EXPRESSED, DataQuality.HIGH, PropagationState.SELF),
                        globalCallTO.getRNASeqExpAbsentHighSelfCount());
                    counts.put(new CountType(CallType.Expression.NOT_EXPRESSED, DataQuality.LOW, PropagationState.SELF),
                        globalCallTO.getRNASeqExpAbsentLowSelfCount());
                    counts.put(new CountType(CallType.Expression.EXPRESSED, DataQuality.HIGH, PropagationState.DESCENDANT),
                        globalCallTO.getRNASeqExpPresentHighDescendantCount());
                    counts.put(new CountType(CallType.Expression.EXPRESSED, DataQuality.LOW, PropagationState.DESCENDANT),
                        globalCallTO.getRNASeqExpPresentLowDescendantCount());
                    counts.put(new CountType(CallType.Expression.NOT_EXPRESSED, DataQuality.HIGH, PropagationState.ANCESTOR),
                        globalCallTO.getRNASeqExpAbsentHighParentCount());
                    counts.put(new CountType(CallType.Expression.NOT_EXPRESSED, DataQuality.LOW, PropagationState.ANCESTOR),
                        globalCallTO.getRNASeqExpAbsentLowParentCount());
                    counts.put(new CountType(CallType.Expression.EXPRESSED, DataQuality.HIGH, PropagationState.ALL),
                        globalCallTO.getRNASeqExpPresentHighTotalCount());
                    counts.put(new CountType(CallType.Expression.EXPRESSED, DataQuality.LOW, PropagationState.ALL),
                        globalCallTO.getRNASeqExpPresentLowTotalCount());
                    counts.put(new CountType(CallType.Expression.NOT_EXPRESSED, DataQuality.HIGH, PropagationState.ALL),
                        globalCallTO.getRNASeqExpAbsentHighTotalCount());
                    counts.put(new CountType(CallType.Expression.NOT_EXPRESSED, DataQuality.LOW, PropagationState.ALL),
                        globalCallTO.getRNASeqExpAbsentLowTotalCount());
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
    
    
    
    //FIXME: to remove, maybe we will need convertServiceOrderingAttrsToGlobalExprDAOOrderingAttrs
//    private static LinkedHashMap<ExpressionCallDAO.OrderingAttribute, DAO.Direction> 
//        convertServiceOrderingAttrsToExprDAOOrderingAttrs(
//            LinkedHashMap<OrderingAttribute, Service.Direction> orderingAttrs) {
//        log.entry(orderingAttrs);
//        
//        return log.exit(orderingAttrs.entrySet().stream().collect(Collectors.toMap(
//            entry -> {
//                switch (entry.getKey()) {
//                case GENE_ID: 
//                    return ExpressionCallDAO.OrderingAttribute.GENE_ID;
//                case ANAT_ENTITY_ID: 
//                    return ExpressionCallDAO.OrderingAttribute.CONDITION_ID;
//                case DEV_STAGE_ID: 
//                    return ExpressionCallDAO.OrderingAttribute.CONDITION_ID;
//                case GLOBAL_RANK: 
//                    return ExpressionCallDAO.OrderingAttribute.MEAN_RANK;
//                default: 
//                    throw log.throwing(new IllegalStateException("Unsupported OrderingAttributes from CallService: "
//                            + entry.getKey()));
//                }
//            }, 
//            entry -> {
//                switch (entry.getValue()) {
//                case ASC: 
//                    return DAO.Direction.ASC;
//                case DESC: 
//                    return DAO.Direction.DESC;
//                default: 
//                    throw log.throwing(new IllegalStateException("Unsupported ordering Direction from CallService: "
//                            + entry.getValue()));
//                }
//            }, 
//            (v1, v2) -> {throw log.throwing(new IllegalStateException("No key collision possible"));}, 
//            () -> new LinkedHashMap<ExpressionCallDAO.OrderingAttribute, DAO.Direction>())));
//    }

    //*************************************************************************
    // HELPER METHODS CONVERTING INFORMATION FROM Call LAYER TO ExpressionCallDAO LAYER
    //*************************************************************************
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
    private <T extends CallData<?>> Boolean inferIsObservedData(Set<T> callData) {
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
    private ExpressionSummary inferSummaryCallType(Set<ExpressionCallData> callData) {
        log.entry(callData);
        
        if (callData.stream().anyMatch(cd -> ExpressionSummary.EXPRESSED.equals(cd.getCallType()))) {
            return log.exit(ExpressionSummary.EXPRESSED);
        }
        
        // To make a call as absent, we need to see all call types to make sure that there is not expression
        if (callData.stream().anyMatch(cd -> cd.getCallType() == null)) {
            throw log.throwing(new IllegalArgumentException(
                "Call types has missing value: cannot infer absence of expression"));
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
    private SummaryQuality inferSummaryQuality(Set<ExpressionCallData> callData) {
        log.entry(callData);

        int expPresentHigh = 0, expPresentLow = 0, expAbsentHigh = 0, expAbsentLow = 0;
        
        for (ExpressionCallData cd: callData) {
            expPresentHigh += cd.getCounts().get(new CountType(CallType.Expression.EXPRESSED,
                                                    DataQuality.HIGH, PropagationState.ALL));
            expPresentLow  += cd.getCounts().get(new CountType(CallType.Expression.EXPRESSED,
                                                    DataQuality.LOW, PropagationState.ALL));
            expAbsentHigh  += cd.getCounts().get(new CountType(CallType.Expression.NOT_EXPRESSED,
                                                    DataQuality.HIGH, PropagationState.ALL));
            expAbsentLow   += cd.getCounts().get(new CountType(CallType.Expression.NOT_EXPRESSED,
                                                    DataQuality.LOW, PropagationState.ALL));
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
}
