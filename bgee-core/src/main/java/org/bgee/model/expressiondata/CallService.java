package org.bgee.model.expressiondata;

import java.math.BigDecimal;
import java.util.AbstractMap.SimpleEntry;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
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
import org.bgee.model.dao.api.expressiondata.CallDAO.CallTO;
import org.bgee.model.dao.api.expressiondata.ConditionDAO;
import org.bgee.model.dao.api.expressiondata.ExpressionCallDAO;
import org.bgee.model.dao.api.expressiondata.ExpressionCallDAO.ExpressionCallTO;
import org.bgee.model.dao.api.expressiondata.GlobalExpressionCallDAO;
import org.bgee.model.dao.api.expressiondata.GlobalExpressionCallDAO.GlobalExpressionCallTO;
import org.bgee.model.expressiondata.Call.DiffExpressionCall;
import org.bgee.model.expressiondata.Call.ExpressionCall;
import org.bgee.model.expressiondata.CallData.ExpressionCallData;
import org.bgee.model.expressiondata.CallFilter.DiffExpressionCallFilter;
import org.bgee.model.expressiondata.CallFilter.ExpressionCallFilter;
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
    
    public static enum Attribute implements Service.Attribute {
        GENE_ID, ANAT_ENTITY_ID, DEV_STAGE_ID, GLOBAL_DATA_QUALITY, GLOBAL_RANK, CALL_DATA, 
        GLOBAL_ANAT_PROPAGATION, GLOBAL_STAGE_PROPAGATION, GLOBAL_OBSERVED_DATA, 
        CALL_DATA_OBSERVED_DATA;
    }
    public static enum OrderingAttribute implements Service.OrderingAttribute {
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
    // FIXME manage correctly filters
    // FIXME check sanity checks
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
        if (callFilter == null) {
            throw log.throwing(new IllegalArgumentException("A CallFilter must be provided."));
        }
        if (speciesId == null || speciesId <= 0) {
            throw log.throwing(new IllegalArgumentException("A species ID must be provided"));
        }

        //To date, possible accepted combinations: 
        //* anat self; stage all  (application example: TopAnat with stage, organ page)
        //* anat all ; stage all  (app: download complete files)
        //* anat self; stage self (app: gene page; TopAnat via R package)
        if (callFilter.getPropagationFilter() != null) {
            PropagationState anatEntityPropFilter = callFilter.getPropagationFilter().getAnatEntityPropagationState();
            PropagationState devStagePropFilter = callFilter.getPropagationFilter().getDevStagePropagationState();
            boolean isValidProp = 
                (PropagationState.SELF.equals(anatEntityPropFilter) && PropagationState.ALL.equals(devStagePropFilter)) || 
                (PropagationState.ALL.equals(anatEntityPropFilter) && PropagationState.ALL.equals(devStagePropFilter)) ||
                (PropagationState.SELF.equals(anatEntityPropFilter) && PropagationState.SELF.equals(devStagePropFilter));
            if (!isValidProp) {
                throw log.throwing(new IllegalArgumentException("Not an accepted combination for data propagation filter"));
            }
        }

        if (callFilter.getPropagationFilter() != null && !clonedAttrs.contains(Attribute.CALL_DATA)) {
            if (callFilter.getPropagationFilter().getIncludingObservedData() != null) {
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

        Set<ConditionDAO.Attribute> condParams = convertCallFilterToConditionDAOAttr(callFilter);
        
        // Retrieve species
        Set<Integer> clnSpId =  Collections.singleton(speciesId);
        final Set<Species> speciesSet = this.getServiceFactory().getSpeciesService()
            .loadSpeciesByIds(clnSpId, false);
        if (speciesSet.size() != 1) {
            throw new IllegalArgumentException("Provided species not found in data source");
        }
        Species species = speciesSet.stream().findAny().get();
        
        // Retrieve conditions by condition IDs
        final Map<Integer, Condition> condMap = Collections.unmodifiableMap(
            this.getDaoManager().getConditionDAO()
                .getConditionsBySpeciesIds(clnSpId, condParams, condParams).stream()
                .collect(Collectors.toMap(cTO -> cTO.getId(), cTO -> mapConditionTOToCondition(cTO))));
        assert !condMap.isEmpty();
        
        // Retrieve genes by Bgee IDs
        // FIXME when GeneFilter is empty
        final Map<Integer, Gene> geneMap = this.getDaoManager().getGeneDAO()
                .getGenesBySpeciesIds(clnSpId, callFilter.getGeneFilter().getGeneIds()).stream()
                .collect(Collectors.toMap(
                    gTO -> gTO.getId(),
                    gTO -> CommonService.mapGeneTOToGene(gTO, species)));
        assert !geneMap.isEmpty();
        
        // Retrieve calls
        Stream<ExpressionCall> calls = this.performsGlobalExprCallQuery(speciesId, geneMap, callFilter, 
                condParams, clonedAttrs, clonedOrderingAttrs)
            .map(to -> mapGlobalCallTOToExpressionCall(to, species, geneMap, condMap, clonedAttrs))
            // We filter ExpressionCalls according to callFilter
            .filter(c -> callFilter.test(c))
            // TODO: do we really need to order?
            .sorted(CallService.convertServiceOrdering(clonedOrderingAttrs));
        
        return log.exit(calls);
    }
    
    private Set<ConditionDAO.Attribute> convertCallFilterToConditionDAOAttr(
            ExpressionCallFilter callFilter) {
        log.entry(callFilter);
        throw log.throwing(new UnsupportedOperationException(
            "Conversion of call filter to ConditionDAO attributes not implemented yet."));
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
                tmpComp = Comparator.comparing(c -> c.getGene().getEnsemblGeneId(), compStr);
                break;
            case ANAT_ENTITY_ID:
                tmpComp = Comparator.comparing(c -> c.getCondition().getAnatEntityId(), compStr);
                break;
            case DEV_STAGE_ID:
                tmpComp = Comparator.comparing(c -> c.getCondition().getDevStageId(), compStr);
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

    public Stream<DiffExpressionCall> loadDiffExpressionCalls(Integer speciesId, 
            DiffExpressionCallFilter callFilter) {
        log.entry(speciesId, callFilter);
        throw log.throwing(new UnsupportedOperationException("Load of diff. expression calls not implemented yet"));
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

        //UnsupportedOperationExceptions (for now)
        if (attributes.contains(Attribute.CALL_DATA_OBSERVED_DATA)) {
            throw log.throwing(new UnsupportedOperationException(
                    "Retrieval of observed data state per data type not yet implemented."));
        }

//        final ExpressionCallDAO exprDao = this.getDaoManager().getExpressionCallDAO();
//        Stream<ExpressionCallTO> expr = exprDao.getExpressionCalls(Arrays.asList(
//                    //generate an ExpressionCallDAOFilter from callFilter 
//                    new CallDAOFilter(
//                            //we will provide the gene IDs to the getExpressionCalls method 
//                            //as a global gene filter, not through the CallDAOFilter. 
//                            null, 
//                            //species
//                            Arrays.asList(String.valueOf(speciesId)), 
//                            //ConditionFilters
//                            callFilter.getConditionFilters().stream()
//                                .map(condFilter -> new DAOConditionFilter(
//                                    condFilter.getAnatEntityIds(), 
//                                    condFilter.getDevStageIds()))
//                            .collect(Collectors.toSet())
//                            )),  
//                    //CallTOFilters
//                    mapCallFilterToExprCallTOFilters(callFilter), 
//                    //includeSubstructures
//                    PropagationState.SELF.equals(callFilter.getPropagationFilter().getAnatEntityPropagationState())?
//                        true : false,
//                    //includeSubStages
//                    PropagationState.SELF.equals(callFilter.getPropagationFilter().getDevStagePropagationState())?
//                        true : false,
//                    //global gene filter
//                    Optional.ofNullable(callFilter.getGeneFilter())
//                        .map(geneFilter -> geneFilter.getGeneIds().stream().map(String::valueOf)
//                            .collect(Collectors.toSet())).orElse(new HashSet<>()), 
//                    //no gene orthology requested
//                    null, 
//                    //Attributes
//                    convertServiceAttrsToExprDAOAttrs(attributes, callFilter.getDataTypeFilter()), 
//                    //OrderingAttributes
//                    convertServiceOrderingAttrsToExprDAOOrderingAttrs(orderingAttributes)
//                )
//                //retrieve the Stream resulting from the query. Note that the query is not executed 
//                //as long as the Stream is not consumed (lazy-loading).
//                .stream();

        final GlobalExpressionCallDAO dao = this.getDaoManager().getGlobalExpressionCallDAO();
        Stream<GlobalExpressionCallTO> calls = dao.getGlobalCalls(geneMap.keySet(), condParameters,
                convertCallServiceAttrToGlobalExprAttr(attributes)).stream();

        return log.exit(calls);
    }

    private Collection<GlobalExpressionCallDAO.Attribute> convertCallServiceAttrToGlobalExprAttr(
        Set<Attribute> attributes) {
        log.entry(attributes);
        throw log.throwing(new UnsupportedOperationException(
            "Conversion of CallService attributes to GlobalExpressionCallDAO attributes not implemented yet."));
    }

    //*************************************************************************
    // METHODS MAPPING CallTOs TO Calls
    //*************************************************************************
    private ExpressionCall mapGlobalCallTOToExpressionCall(GlobalExpressionCallTO globalCallTO, 
            Species species, Map<Integer, Gene> geneMap, Map<Integer, Condition> condMap,
            Set<CallService.Attribute> attrs) {
        log.entry(globalCallTO, species, geneMap, condMap, attrs);
        
        Set<ExpressionCallData> callData = mapGlobalCallToCallData(globalCallTO);

        return log.exit(new ExpressionCall(geneMap.get(globalCallTO.getBgeeGeneId()), 
            condMap.get(globalCallTO.getConditionId()),
            inferIsObservedData(callData),
            inferSummaryCallType(callData),
            inferSummaryQuality(callData),
            callData,
            globalCallTO.getGlobalMeanRank()));
    }
    
    private static Set<ExpressionCallData> mapGlobalCallToCallData(GlobalExpressionCallTO globalCallTO) {
        log.entry();
        
        Set<ExpressionCallData> allCallData = new HashSet<>();
        for (DataType dt : DataType.values()) {
            ExpressionCallData callData;
            switch (dt) {
                case AFFYMETRIX:
                    callData = new ExpressionCallData(dt, globalCallTO.getAffymetrixExpPresentHighSelfCount(),
                        globalCallTO.getAffymetrixExpPresentLowSelfCount(),
                        globalCallTO.getAffymetrixExpAbsentHighSelfCount(),
                        globalCallTO.getAffymetrixExpAbsentLowSelfCount(),
                        globalCallTO.getAffymetrixExpPresentHighDescendantCount(),
                        globalCallTO.getAffymetrixExpPresentLowDescendantCount(),
                        globalCallTO.getAffymetrixExpAbsentHighParentCount(),
                        globalCallTO.getAffymetrixExpAbsentLowParentCount(),
                        globalCallTO.getAffymetrixExpPresentHighTotalCount(),
                        globalCallTO.getAffymetrixExpPresentLowTotalCount(),
                        globalCallTO.getAffymetrixExpAbsentHighTotalCount(),
                        globalCallTO.getAffymetrixExpAbsentLowTotalCount(),
                        globalCallTO.getAffymetrixExpPropagatedCount(),
                        globalCallTO.getAffymetrixMeanRank(), globalCallTO.getAffymetrixMeanRankNorm(),
                        globalCallTO.getAffymetrixDistinctRankSum());
                    break;
                case EST:
                    callData = new ExpressionCallData(dt, globalCallTO.getESTLibPresentHighSelfCount(),
                        globalCallTO.getESTLibPresentLowSelfCount(),
                        0, 0,   // Absent self counts
                        globalCallTO.getESTLibPresentHighDescendantCount(),
                        globalCallTO.getESTLibPresentLowDescendantCount(),
                        0, 0,   // Absent parent counts
                        globalCallTO.getESTLibPresentHighTotalCount(),
                        globalCallTO.getESTLibPresentLowTotalCount(),
                        0, 0,   // Absent total counts
                        globalCallTO.getESTLibPropagatedCount(),
                        globalCallTO.getESTRank(), globalCallTO.getESTRankNorm(),
                        null);  // rankSum

                    break;
                case IN_SITU:
                    callData = new ExpressionCallData(dt, globalCallTO.getInSituExpPresentHighSelfCount(),
                        globalCallTO.getInSituExpPresentLowSelfCount(),
                        globalCallTO.getInSituExpAbsentHighSelfCount(),
                        globalCallTO.getInSituExpAbsentLowSelfCount(),
                        globalCallTO.getInSituExpPresentHighDescendantCount(),
                        globalCallTO.getInSituExpPresentLowDescendantCount(),
                        globalCallTO.getInSituExpAbsentHighParentCount(),
                        globalCallTO.getInSituExpAbsentLowParentCount(),
                        globalCallTO.getInSituExpPresentHighTotalCount(),
                        globalCallTO.getInSituExpPresentLowTotalCount(),
                        globalCallTO.getInSituExpAbsentHighTotalCount(),
                        globalCallTO.getInSituExpAbsentLowTotalCount(),
                        globalCallTO.getInSituExpPropagatedCount(),
                        globalCallTO.getInSituRank(), globalCallTO.getInSituRankNorm(),
                        null);
                    break;
                case RNA_SEQ:
                    callData = new ExpressionCallData(dt, globalCallTO.getRNASeqExpPresentHighSelfCount(),
                        globalCallTO.getRNASeqExpPresentLowSelfCount(),
                        globalCallTO.getRNASeqExpAbsentHighSelfCount(),
                        globalCallTO.getRNASeqExpAbsentLowSelfCount(),
                        globalCallTO.getRNASeqExpPresentHighDescendantCount(),
                        globalCallTO.getRNASeqExpPresentLowDescendantCount(),
                        globalCallTO.getRNASeqExpAbsentHighParentCount(),
                        globalCallTO.getRNASeqExpAbsentLowParentCount(),
                        globalCallTO.getRNASeqExpPresentHighTotalCount(),
                        globalCallTO.getRNASeqExpPresentLowTotalCount(),
                        globalCallTO.getRNASeqExpAbsentHighTotalCount(),
                        globalCallTO.getRNASeqExpAbsentLowTotalCount(),
                        globalCallTO.getRNASeqExpPropagatedCount(),
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

    //*************************************************************************
    // HELPER METHODS CONVERTING INFORMATION FROM CallDAO LAYER TO Call LAYER
    //*************************************************************************
    private static DataQuality convertDataStateToDataQuality(CallTO.DataState state) 
            throws IllegalStateException{
        log.entry(state);
        switch(state) {
        case LOWQUALITY: 
            return log.exit(DataQuality.LOW);
        case HIGHQUALITY:
            return log.exit(DataQuality.HIGH);
        case NODATA: 
            return log.exit(DataQuality.NODATA);
        default: 
            throw log.throwing(new IllegalStateException("Unsupported CallTO.DataState: " + state));
        }
    }
    
    //*************************************************************************
    // HELPER METHODS CONVERTING INFORMATION FROM ExpressionCallDAO LAYER TO Call LAYER
    //*************************************************************************
    private static final Map<ExpressionCallDAO.Attribute, DataType> EXPR_ATTR_TO_DATA_TYPE = Stream.of(
            new SimpleEntry<>(ExpressionCallDAO.Attribute.AFFYMETRIX_DATA, DataType.AFFYMETRIX), 
            new SimpleEntry<>(ExpressionCallDAO.Attribute.EST_DATA, DataType.EST), 
            new SimpleEntry<>(ExpressionCallDAO.Attribute.IN_SITU_DATA, DataType.IN_SITU), 
            new SimpleEntry<>(ExpressionCallDAO.Attribute.RNA_SEQ_DATA, DataType.RNA_SEQ))
            .collect(Collectors.toMap(SimpleEntry::getKey, SimpleEntry::getValue));
            
    //*************************************************************************
    // METHODS MAPPING CallDatas TO ExpressionCallTOs
    //*************************************************************************
    private static Set<ExpressionCallTO> mapCallFilterToGlobalExprCallTOFilters(ExpressionCallFilter filter) {
        log.entry(filter);
        throw log.throwing(new UnsupportedOperationException(
            "Mapping CallFilter To GlobalExprCallTO filters to be implemented"));
    }
    
    //FIXME: to remove, we only need CallTOToCallData
//    private static Set<ExpressionCallTO> mapCallDataToExprCallTOFilters(ExpressionCallData callData, 
//            DataPropagation callFilterPropag) {
//        log.entry(callData, callFilterPropag);
//        
//        //if the dataType of the callData is null, then it means that it targets all data types. 
//        //In order to get OR conditions between data type parameters 
//        //(e.g., affymetrixData >= HIGH OR rnaSeqData >= HIGH), we need to create one ExpressionCallTO 
//        //per data type (because data type parameters inside a same ExpressionCallTO are considered 
//        //as AND conditions). But this is needed only if there is a filtering requested 
//        //on a minimum quality level, of course.
//        //Here, don't use an EnumSet to be able to put 'null' in it (see below).
//        Set<DataType> dataTypes = new HashSet<>();
//        // FIXME It does not compile anymore but I leave it to be able to come back
//        // to it before the cleaning of the class
////        if (callData.getDataType() == null && callData.getDataQuality().equals(DataQuality.LOW)) {
////            //no filtering on data quality for any data type
////            dataTypes.add(null);
////        } else {
////            //filtering requested on data quality for any data type, 
////            //or filtering requested on one specific data type for any quality
////            dataTypes = callData.getDataType() != null? 
////                EnumSet.of(callData.getDataType()): EnumSet.allOf(DataType.class);
////        }
//                
//        return log.exit(dataTypes.stream().map(dataType -> {
//            CallTO.DataState affyState = null;
//            CallTO.DataState estState = null;
//            CallTO.DataState inSituState = null;
//            CallTO.DataState rnaSeqState = null;
//            
//            if (dataType != null) {
//                CallTO.DataState state = convertDataQualityToDataState(callData.getDataQuality());
//                switch (dataType) {
//                case AFFYMETRIX: 
//                    affyState = state;
//                    break;
//                case EST: 
//                    estState = state;
//                    break;
//                case IN_SITU: 
//                    inSituState = state;
//                    break;
//                case RNA_SEQ: 
//                    rnaSeqState = state;
//                    break;
//                default: 
//                    throw log.throwing(new IllegalStateException("Unsupported DataType: " + dataType));
//                }
//            }
//            
//            return new ExpressionCallTO(affyState, estState, inSituState, rnaSeqState, 
//                convertPropagationStateToExprOrigin(callFilterPropag.getAnatEntityPropagationState()), 
//                convertPropagationStateToExprOrigin(callFilterPropag.getDevStagePropagationState()), 
//                callFilterPropag.getIncludingObservedData());
//        })
//        //filter CallTOs that provide no filtering at all
//        .filter(callTO -> !callTO.equals(new ExpressionCallTO(null, null, null, null)))
//        .collect(Collectors.toSet()));
//    }

    //*************************************************************************
    // HELPER METHODS CONVERTING INFORMATION FROM Call LAYER TO CallDAO LAYER
    //*************************************************************************
    private static CallTO.DataState convertDataQualityToDataState(DataQuality qual) 
            throws IllegalStateException{
        log.entry(qual);
        switch(qual) {
        case LOW: 
            return log.exit(CallTO.DataState.LOWQUALITY);
        case HIGH:
            return log.exit(CallTO.DataState.HIGHQUALITY);
        default: 
            throw log.throwing(new IllegalStateException("Unsupported DataQuality: " + qual));
        }
    }
    
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
    private static Set<ExpressionCallDAO.Attribute> convertServiceAttrsToExprDAOAttrs(
            Set<Attribute> attributes, Set<DataType> dataTypesRequested) {
        log.entry(attributes, dataTypesRequested);
        
        //revert the existing map ExpressionCallDAO.Attribute -> DataType
        Map<DataType, ExpressionCallDAO.Attribute> typeToDAOAttr = EXPR_ATTR_TO_DATA_TYPE.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
        
        return log.exit(attributes.stream().flatMap(attr -> {
            switch (attr) {
            case GENE_ID: 
                return Stream.of(ExpressionCallDAO.Attribute.GENE_ID);
            case ANAT_ENTITY_ID: 
                return Stream.of(ExpressionCallDAO.Attribute.CONDITION_ID);
            case DEV_STAGE_ID: 
                return Stream.of(ExpressionCallDAO.Attribute.CONDITION_ID);
            //Whether we need to get a global quality level over all requested data types, 
            //or the detailed quality level per data type, it's the same DAO attributes that we need. 
            case GLOBAL_DATA_QUALITY:
            case CALL_DATA: 
                return dataTypesRequested.stream().map(type -> Optional.ofNullable(typeToDAOAttr.get(type))
                        //bug of javac for type inference, we need to type the exception 
                        //explicitly to RuntimeException,
                        //see http://stackoverflow.com/questions/25523375/java8-lambdas-and-exceptions
                        .<RuntimeException>orElseThrow(() -> log.throwing(new IllegalStateException(
                                "Unsupported DataType: " + type))));
            case GLOBAL_ANAT_PROPAGATION: 
                return Stream.of(ExpressionCallDAO.Attribute.ANAT_ORIGIN_OF_LINE);
            case GLOBAL_STAGE_PROPAGATION: 
                return Stream.of(ExpressionCallDAO.Attribute.STAGE_ORIGIN_OF_LINE);
            case GLOBAL_OBSERVED_DATA: 
                return Stream.of(ExpressionCallDAO.Attribute.OBSERVED_DATA);
            case CALL_DATA_OBSERVED_DATA: 
                //nothing here, the only way to get this information is by performing 2 queries, 
                //one including substructures/sub-stages, another one without substructures/sub-stages.
                return Stream.empty();
            case GLOBAL_RANK: 
                return Stream.of(ExpressionCallDAO.Attribute.GLOBAL_MEAN_RANK);
            default: 
                throw log.throwing(new IllegalStateException("Unsupported Attributes from CallService: "
                        + attr));
            }
        }).collect(Collectors.toCollection(() -> EnumSet.noneOf(ExpressionCallDAO.Attribute.class))));
    }
    
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
            expPresentHigh += cd.getPresentHighTotalCount();
            expPresentLow  += cd.getPresentLowTotalCount();
            expAbsentHigh  += cd.getAbsentHighTotalCount();
            expAbsentLow   += cd.getAbsentLowTotalCount();
        }
        
        if (expPresentHigh >= 2) {
            return log.exit(SummaryQuality.GOLD);
        }
        if (expPresentHigh == 1 || expPresentLow >= 2) {
            return log.exit(SummaryQuality.SILVER);
        }
        if (expPresentLow == 1) {
            return log.exit(SummaryQuality.BRONZE);
        }
        assert expPresentHigh == 0 && expPresentLow == 0;
        
        if (expAbsentHigh >= 2) {
            return log.exit(SummaryQuality.GOLD);
        }
        if (expAbsentHigh == 1 || expAbsentLow >= 2) {
            return log.exit(SummaryQuality.SILVER);
        }
        if (expAbsentLow == 1) {
            return log.exit(SummaryQuality.BRONZE);
        }
        
        throw log.throwing(new IllegalArgumentException("Malformed CallData"));
    }

}
