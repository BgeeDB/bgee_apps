package org.bgee.model.expressiondata.call;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.CommonService;
import org.bgee.model.ServiceFactory;
import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.anatdev.AnatEntityService;
import org.bgee.model.anatdev.DevStage;
import org.bgee.model.anatdev.DevStageService;
import org.bgee.model.anatdev.Sex;
import org.bgee.model.anatdev.SexService;
import org.bgee.model.anatdev.Strain;
import org.bgee.model.anatdev.StrainService;
import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.expressiondata.DAODataType;
import org.bgee.model.dao.api.expressiondata.call.ConditionDAO;
import org.bgee.model.dao.api.expressiondata.call.ConditionDAO.ConditionTOResultSet;
import org.bgee.model.dao.api.expressiondata.call.GlobalExpressionCallDAO;
import org.bgee.model.dao.api.expressiondata.call.GlobalExpressionCallDAO.GlobalExpressionCallTO;
import org.bgee.model.dao.api.expressiondata.call.GlobalExpressionCallDAO.GlobalExpressionCallTOResultSet;
import org.bgee.model.dao.api.gene.GeneDAO;
import org.bgee.model.expressiondata.baseelements.ConditionParameter;
import org.bgee.model.expressiondata.call.Call.ExpressionCall2;
import org.bgee.model.expressiondata.call.CallFilter.ExpressionCallFilter2;
import org.bgee.model.gene.Gene;
import org.bgee.model.gene.GeneBioType;
import org.bgee.model.species.Species;

public class ExpressionCallLoader extends CommonService {
    private final static Logger log = LogManager.getLogger(ExpressionCallLoader.class.getName());

    /**
     * An {@code int} that is the maximum allowed number of results
     * to retrieve in one method call.
     * Value: 10,000.
     */
    public static int LIMIT_MAX = 10000;
    /**
     * An {@code int} that is the maximum number of elements
     * in {@link #conditionMap} and {@link #geneMap} before starting
     * to flushing some existing entries. It is not a <strong>guarantee</strong>
     * that those {@code Map}s will never exceed that size, just a trigger
     * to flushing entries as much as possible.
     *
     * @see #updateConditionMap(Set)
     * @see #updateGeneMap(Set)
     */
    private static final int MAX_ELEMENTS_IN_MAP = 10000;



    private final GlobalExpressionCallDAO globalExprCallDAO;
    private final GeneDAO geneDAO;
    private final ConditionDAO condDAO;
    private final AnatEntityService anatEntityService;
    private final DevStageService devStageService;
    private final SexService sexService;
    private final StrainService strainService;
    private final CallServiceUtils utils;
    private final CallMapping callMapping;
    /**
     * @see #getProcessedFilter()
     */
    private final ExpressionCallProcessedFilter processedFilter;

    //These attributes are mutable, it is acceptable for a Service.
    //We keep the speciesMap and geneBiotypeMap inside the rawDataProcessedFilter,
    //as there will be no update to them by this RawDataLoader.
    /**
     * A {@code Map} where keys are {@code Integer}s that are internal IDs of raw data conditions,
     * the value being the associated {@code Condition2}. this {@code Map} is used
     * to store the retrieved {@code Condition2}s over several independent calls
     * to this {@code ExpressionCallLoader}, in order to avoid querying multiple times for the same
     * conditions.
     *
     * @see #MAX_ELEMENTS_IN_MAP
     * @see #updateRawDataConditionMap(Set)
     */
    private final Map<Integer, Condition2> conditionMap;
    /**
     * A {@code Map} where keys are {@code Integer}s that are internal IDs of genes,
     * the value being the associated {@code Gene}. this {@code Map} is used
     * to store the retrieved {@code Gene}s over several independent calls
     * to this {@code ExpressionCallLoader}, in order to avoid querying multiple times for the same
     * genes.
     *
     * @see #MAX_ELEMENTS_IN_MAP
     * @see #updateGeneMap(Set)
     */
    private final Map<Integer, Gene> geneMap;

    ExpressionCallLoader(ExpressionCallProcessedFilter processedFilter, ServiceFactory serviceFactory) {
        this(processedFilter, serviceFactory, new CallServiceUtils(),
                new CallMapping(processedFilter));
    }
    //Constructor package protected so that only the RawDataService can instantiate this class
    ExpressionCallLoader(ExpressionCallProcessedFilter processedFilter,
            ServiceFactory serviceFactory, CallServiceUtils utils, CallMapping callMapping) {
        super(serviceFactory);

        if (processedFilter == null) {
            //we need it at least to retrieve, species, gene biotypes, and sources
            throw log.throwing(new IllegalArgumentException(
                    "A processedFilter must be provided"));
        }
        if (utils == null) {
            throw log.throwing(new IllegalArgumentException(
                    "A CallServiceUtils must be provided"));
        }
        if (callMapping == null) {
            throw log.throwing(new IllegalArgumentException(
                    "A CallMapping must be provided"));
        }
        this.utils = utils;
        this.callMapping = callMapping;
        this.globalExprCallDAO = this.getDaoManager().getGlobalExpressionCallDAO();
        this.geneDAO = this.getDaoManager().getGeneDAO();
        this.condDAO = this.getDaoManager().getConditionDAO();
        this.anatEntityService = this.getServiceFactory().getAnatEntityService();
        this.devStageService = this.getServiceFactory().getDevStageService();
        this.sexService = this.getServiceFactory().getSexService();
        this.strainService = this.getServiceFactory().getStrainService();
        this.processedFilter = processedFilter;
        this.conditionMap = new HashMap<>();
        this.geneMap = new HashMap<>();
        //Seed the Maps with any condition or gene already identified
        //from the processed filter.
        //We keep the speciesMap and geneBiotypeMap inside the processedFilter,
        //as there will be no update to them by this Loader.
        this.conditionMap.putAll(this.processedFilter.getRequestedConditionMap());
        this.geneMap.putAll(this.processedFilter.getRequestedGeneMap());
    }

    //If we want to let users decide which of the anat. entity, dev. stage, etc, to retrieve
    //in the Conditions of the ExpressionCall, we should let them set Attributes.
    //Currently, the condition parameters to return are determined by the combination
    //selected in the source ExpressionCallFilter. It's a bit weird that is the a filter
    //that determine the attributes visualized in return.
    //But if there were attributes, we would still need to provide the condition parameters
    //to the filter, because it is important to configure the query.
    //So, maybe that should be its own argument of the method, rather than being in both the filter
    //and the attributes?
    //TODO But then it should be provided at the level of ExpressionCallService.loadCallLoader!
    //(because this is where the ExpressionCallFilter is provided, some of the Conditions retrieved, etc)
    //One of the Attribute could be "CONDITION", rather than the detail of the condition parameters.
    //And then there would be another argument, the condition parameters, that would affect both
    //the filtering in the query and the fields retrieved in the returned Conditions.
    //
    //offset is a Long because sometimes the number of potential results can be very large.
    public List<ExpressionCall2> loadData(Long offset, Integer limit) {
        log.traceEntry("{}, {}", offset, limit);

        //If the DAOCallFilters are null (different from: not-null and empty)
        //it means there was no matching conds and thus no result for sure
        if (this.processedFilter.getDaoFilters() == null) {
            return log.traceExit(new ArrayList<>());
        }

        if (offset != null && offset < 0) {
            throw log.throwing(new IllegalArgumentException("offset cannot be less than 0"));
        }
        if (limit != null && limit <= 0) {
            throw log.throwing(new IllegalArgumentException(
                    "limit cannot be less than or equal to 0"));
        }
        if (limit != null && limit > LIMIT_MAX) {
            throw log.throwing(new IllegalArgumentException("limit cannot be greater than "
                    + LIMIT_MAX));
        }
        long newOffset = offset == null? 0L: offset;
        int newLimit = limit == null? LIMIT_MAX: limit;

        //We obtain the results from the data source
        ExpressionCallFilter2 callFilter = this.processedFilter.getSourceFilter();
        EnumSet<CallService.Attribute> attrs = this.getAttributes(callFilter);
        GlobalExpressionCallTOResultSet rs = this.globalExprCallDAO
                .getGlobalExpressionCalls2(
                        this.processedFilter.getDaoFilters(),
                        convertServiceAttrToGlobalExprDAOAttr(attrs, callFilter),
                        //for now we always order by bgeeGeneId, conditionId
                        convertServiceOrderingAttrToGlobalExprDAOOrderingAttr(callFilter),
                        newOffset,
                        newLimit);

        //We iterate a first time the calls to retrieve the bgeeGeneIds and the condIds,
        //and we store them along the way
        Set<Integer> bgeeGeneIds = new HashSet<>();
        Set<Integer> condIds = new HashSet<>();
        List<GlobalExpressionCallTO> callTOs = new ArrayList<>();
        while (rs.next()) {
            GlobalExpressionCallTO callTO = rs.getTO();
            if (callTO.getBgeeGeneId() != null) {
                bgeeGeneIds.add(callTO.getBgeeGeneId());
            }
            if (callTO.getConditionId() != null) {
                condIds.add(callTO.getConditionId());
            }
            callTOs.add(callTO);
        }
        //Now we update the geneMap and condMap
        this.updateConditionMap(condIds);
        this.updateGeneMap(bgeeGeneIds);

        //Now we generate the final result
        return log.traceExit(callTOs.stream()
                .map(cTO -> this.callMapping.mapGlobalCallTOToExpressionCall(cTO,
                        this.geneMap, this.conditionMap, callFilter,
                        this.processedFilter.getMaxRankPerSpecies(), attrs))
                .collect(Collectors.toList()));
        
    }

    public long loadDataCount() {
        log.traceEntry();

        //If the DAOCallFilters are null (different from: not-null and empty)
        //it means there was no matching conds and thus no result for sure
        if (this.processedFilter.getDaoFilters() == null) {
            return log.traceExit(0L);
        }
        //FIXME: this value, and maybe also per species, must be inserted in a new table of the database,
        //and getGlobalExpressionCallsCount to detect when the filter is empty and use that table
        if (this.processedFilter.getSourceFilter().isEmptyFilter()) {
            return log.traceExit(5207425780L);
        }
        return log.traceExit(this.globalExprCallDAO.getGlobalExpressionCallsCount(
                this.processedFilter.getDaoFilters()));
    }

    public ExpressionCallPostFilter loadPostFilter() {
        log.traceEntry();
        //If the DAOCallFilters are null (different from: not-null and empty)
        //it means there was no matching conds and thus no result for sure
        if (this.processedFilter.getDaoFilters() == null) {
            return log.traceExit(new ExpressionCallPostFilter());
        }

        Function<Collection<ConditionDAO.Attribute>, ConditionTOResultSet> condRequestFun = (attrs) ->
        this.condDAO.getGlobalConditionsFromCallFilters(this.getProcessedFilter().getDaoFilters(), attrs);
        Map<ConditionParameter<?, ?>, Set<? extends Object>> condParamEntities = new HashMap<>();

        // retrieve anatEntities and cell types
        if (this.getProcessedFilter().getSourceFilter().getCondParamCombination()
                .contains(ConditionParameter.ANAT_ENTITY_CELL_TYPE)) {
            Set<String> anatEntityIds = condRequestFun.apply(
                    Set.of(ConditionDAO.Attribute.ANAT_ENTITY_ID)).stream()
                    .map(a -> a.getAnatEntityId()).collect(Collectors.toSet());
            Set<String> cellTypeIds = condRequestFun.apply(
                    Set.of(ConditionDAO.Attribute.CELL_TYPE_ID))
                    .stream()
                    .map(c -> c.getCellTypeId())
                    //cell type is the only condition param that can be NULL,
                    //we end up requesting an anat. entity with ID "NULL"
                    .filter(s -> s != null)
                    .collect(Collectors.toSet());
            Set<String> anatEntityCellTypeIds = new HashSet<>(anatEntityIds);
            anatEntityCellTypeIds.addAll(cellTypeIds);
            Set<AnatEntity> anatEntityCellTypes = anatEntityCellTypeIds.isEmpty()?
                    new HashSet<>() : anatEntityService.loadAnatEntities(anatEntityCellTypeIds, false)
                    .collect(Collectors.toSet());
            condParamEntities.put(ConditionParameter.ANAT_ENTITY_CELL_TYPE, anatEntityCellTypes);
        }

        //retrieve dev. stages
        if (this.getProcessedFilter().getSourceFilter().getCondParamCombination()
                .contains(ConditionParameter.DEV_STAGE)) {
            Set<String> stageIds = condRequestFun.apply(
                    Set.of(ConditionDAO.Attribute.STAGE_ID))
                    .stream().map(c -> c.getStageId()).collect(Collectors.toSet());
            Set<DevStage> stages = stageIds.isEmpty()?
                    new HashSet<>() : devStageService.loadDevStages(null, null, stageIds, false)
                    .collect(Collectors.toSet());
            condParamEntities.put(ConditionParameter.DEV_STAGE, stages);
        }

        // retrieve strains
        if (this.getProcessedFilter().getSourceFilter().getCondParamCombination()
                .contains(ConditionParameter.STRAIN)) {
            Set<String> strainIds = condRequestFun.apply(
                    Set.of(ConditionDAO.Attribute.STRAIN_ID))
                    .stream().map(c -> c.getStrainId()).collect(Collectors.toSet());
            Set<Strain> strains = strainIds.isEmpty()? new HashSet<>():
                this.strainService.loadStrains(strainIds).collect(Collectors.toSet());
            condParamEntities.put(ConditionParameter.STRAIN, strains);
        }

        //retrieve sexes
        if (this.getProcessedFilter().getSourceFilter().getCondParamCombination()
                .contains(ConditionParameter.SEX)) {
            Set<String> sexIds = condRequestFun.apply(
                    Set.of(ConditionDAO.Attribute.SEX_ID))
                    .stream().map(c -> c.getSex().getStringRepresentation()).collect(Collectors.toSet());
            Set<Sex> sexes = this.sexService.loadSexes(sexIds).collect(Collectors.toSet());
            condParamEntities.put(ConditionParameter.SEX, sexes);
        }

        //Species are unnecessary, we allow filtering only when one species is selected
//        Set<Integer> speciesIds = condRequestFun.apply(
//                Set.of(ConditionDAO.Attribute.SPECIES_ID))
//                .stream().map(c -> c.getSpeciesId()).collect(Collectors.toSet());
//        Set<Species> species = speciesIds.isEmpty()?
//                new HashSet<>() : this.getProcessedFilter().getSpeciesMap().values()
//                .stream().filter(s -> speciesIds.contains(s.getId()))
//                .collect(Collectors.toSet());
//        assert speciesIds.size() == species.size();

        return log.traceExit(new ExpressionCallPostFilter(condParamEntities));
    }

    public ExpressionCallProcessedFilter getProcessedFilter() {
        return processedFilter;
    }

    //TODO to continue here
//    private ExpressionCallPostFilter loadConditionPostFilter(BiFunction<Collection<DAOCallFilter>,
//            Collection<ConditionDAO.Attribute>, ConditionTOResultSet> condRequest) {
//        log.traceEntry("{}", condRequest);
//
//        //If the DaoRawDataFilters are null it means there was no matching conds
//        //and thus no result for sure
//        if (this.processedFilter.getDaoFilters() == null) {
//            return log.traceExit(new ExpressionCallPostFilter(null));
//        }
//
//        // retrieve anatEntities
//        Set<String> anatEntityIds = condRequest.apply(this.processedFilter
//        .getDaoFilters(), Set.of(ConditionDAO.Attribute.ANAT_ENTITY_ID)).stream()
//        .map(a -> a.getAnatEntityId()).collect(Collectors.toSet());
//        Set<AnatEntity> anatEntities = anatEntityIds.isEmpty()?
//                new HashSet<>() : anatEntityService.loadAnatEntities(anatEntityIds, false)
//                .collect(Collectors.toSet());
//
//        // retrieve cellTypes
//        Set<String> cellTypeIds = condRequest.apply(this.getRawDataProcessedFilter()
//                        .getDaoFilters(), Set.of(RawDataConditionDAO.Attribute.CELL_TYPE_ID))
//                .stream()
//                .map(c -> c.getCellTypeId())
//                //cell type is the only condition param that can be NULL,
//                //we end up requesting an anat. entity with ID "NULL"
//                .filter(s -> s != null)
//                .collect(Collectors.toSet());
//        Set<AnatEntity> cellTypes = cellTypeIds.isEmpty()?
//                new HashSet<>() : anatEntityService.loadAnatEntities(cellTypeIds, false)
//                .collect(Collectors.toSet());
//
//        //retrieve dev. stages
//        Set<String> stageIds = condRequest.apply(this.getRawDataProcessedFilter()
//                        .getDaoFilters(), Set.of(RawDataConditionDAO.Attribute.STAGE_ID))
//                .stream().map(c -> c.getStageId()).collect(Collectors.toSet());
//        Set<DevStage> stages = stageIds.isEmpty()?
//                new HashSet<>() : devStageService.loadDevStages(null, null, stageIds, false)
//                .collect(Collectors.toSet());
//
//        // retrieve strains
//        Set<String> strains = condRequest.apply(this.getRawDataProcessedFilter()
//                        .getDaoFilters(), Set.of(RawDataConditionDAO.Attribute.STRAIN))
//                .stream().map(c -> c.getStrainId()).collect(Collectors.toSet());
//
//        //retrieve sexes
//        Set<RawDataSex> sexes = condRequest.apply(this.getRawDataProcessedFilter()
//                        .getDaoFilters(), Set.of(RawDataConditionDAO.Attribute.SEX)).stream()
//                .map(c -> mapDAORawDataSexToRawDataSex(c.getSex())).collect(Collectors.toSet());
//
//        return log.traceExit(new RawDataPostFilter(anatEntities, stages, cellTypes,
//                sexes, strains, dataType));
//    }

    private void updateConditionMap(Set<Integer> condIds) {
        log.traceEntry("{}", condIds);

        Set<Integer> missingCondIds = new HashSet<>(condIds);
        missingCondIds.removeAll(this.conditionMap.keySet());
        if (missingCondIds.isEmpty()) {
            log.traceExit(); return;
        }
        Map<Integer, Species> speciesMap = this.processedFilter.getSpeciesMap();
        Map<Integer, Condition2> missingCondMap = this.utils.loadConditionMapFromResultSet(
                        (attrs) -> this.condDAO.getGlobalConditionsFromIds(missingCondIds, attrs),
                        this.utils.convertCondParamsToDAOCondAttributes(
                                this.processedFilter.getSourceFilter().getCondParamCombination()),
                        speciesMap.values(), this.anatEntityService, this.devStageService,
                        this.sexService, this.strainService);
        //If the Map is going to grow too big, we keep only the entries needed
        //for this method call
        if (this.conditionMap.size() + missingCondMap.size() > MAX_ELEMENTS_IN_MAP) {
            this.conditionMap.keySet().retainAll(condIds);
        }
        this.conditionMap.putAll(missingCondMap);

        log.traceExit(); return;
    }
    private void updateGeneMap(Set<Integer> bgeeGeneIds) {
        log.traceEntry("{}", bgeeGeneIds);

        Set<Integer> missingGeneIds = new HashSet<>(bgeeGeneIds);
        missingGeneIds.removeAll(this.geneMap.keySet());
        if (missingGeneIds.isEmpty()) {
            log.traceExit(); return;
        }
        Map<Integer, Species> speciesMap = this.processedFilter.getSpeciesMap();
        Map<Integer, GeneBioType> geneBioTypeMap = this.processedFilter.getGeneBioTypeMap();
        Map<Integer, Gene> missingGeneMap = this.geneDAO.getGenesByBgeeIds(missingGeneIds).stream()
                .collect(Collectors.toMap(gTO -> gTO.getId(), gTO -> mapGeneTOToGene(gTO,
                        Optional.ofNullable(speciesMap.get(gTO.getSpeciesId()))
                        .orElseThrow(() -> new IllegalStateException("Missing species ID for gene")),
                        null, null,
                        Optional.ofNullable(geneBioTypeMap.get(gTO.getGeneBioTypeId()))
                        .orElseThrow(() -> new IllegalStateException("Missing gene biotype ID for gene")))));
        //If the Map is going to grow too big, we keep only the entries needed
        //for this method call
        if (this.geneMap.size() + missingGeneMap.size() > MAX_ELEMENTS_IN_MAP) {
            this.geneMap.keySet().retainAll(bgeeGeneIds);
        }
        this.geneMap.putAll(missingGeneMap);

        log.traceExit(); return;
    }

    private EnumSet<CallService.Attribute> getAttributes(ExpressionCallFilter2 callFilter) {
        log.traceEntry("{}", callFilter);
      //For now we define the attributes ourselves, and we still use the Attributes
        //from the CallService
        //TODO: implement Attributes in ExpressionCallLoader
        EnumSet<CallService.Attribute> attributes = EnumSet.of(
                CallService.Attribute.GENE,
                CallService.Attribute.CALL_TYPE,
                CallService.Attribute.DATA_QUALITY,
                CallService.Attribute.EXPRESSION_SCORE,
                //to know how the propagation status of the call
                CallService.Attribute.OBSERVED_DATA,
                //We need the p-value info per data type to know which data types
                //produced the calls
                CallService.Attribute.P_VALUE_INFO_EACH_DATA_TYPE,
                //We also want to know the global FDR-corrected p-value
                CallService.Attribute.P_VALUE_INFO_ALL_DATA_TYPES);
        attributes.addAll(callFilter.getCondParamCombination().stream()
                .flatMap(param -> {
                    //Any condition parameter attribute would do to retrieve the condition IDs,
                    //but we map properly anyway.
                    if (ConditionParameter.ANAT_ENTITY_CELL_TYPE.equals(param)) {
                        return Stream.of(CallService.Attribute.ANAT_ENTITY_ID,
                                CallService.Attribute.CELL_TYPE_ID);
                    } else if (ConditionParameter.DEV_STAGE.equals(param)) {
                        return Stream.of(CallService.Attribute.DEV_STAGE_ID);
                    } else if (ConditionParameter.SEX.equals(param)) {
                        return Stream.of(CallService.Attribute.SEX_ID);
                    } else if (ConditionParameter.STRAIN.equals(param)) {
                        return Stream.of(CallService.Attribute.STRAIN_ID);
                    }
                    throw log.throwing(new UnsupportedOperationException(
                            "Unsupported ConditionParameter: " + param));
                })
                .collect(Collectors.toSet()));
        return log.traceExit(attributes);
    }
    private Set<GlobalExpressionCallDAO.AttributeInfo> convertServiceAttrToGlobalExprDAOAttr(
            EnumSet<CallService.Attribute> attributes, ExpressionCallFilter2 callFilter) {
        log.traceEntry("{}, {}", attributes, callFilter);

        EnumSet<DAODataType> daoDataTypes = this.utils.convertDataTypeToDAODataType(callFilter == null? null:
            callFilter.getDataTypeFilters());
        EnumSet<DAODataType> daoDataTypesTrustedForAbsentCalls =
                this.utils.convertTrustedAbsentDataTypesToDAODataTypes(callFilter == null? null:
                    callFilter.getDataTypeFilters());
        //TODO to upate to use ConditionDAO.ConditionParameter
        EnumSet<ConditionDAO.Attribute> daoCondParamComb = this.utils
                .convertCondParamsToDAOCondAttributes(callFilter.getCondParamCombination());

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
                        daoDataTypes, null));
                pValAttributes.add(new GlobalExpressionCallDAO.AttributeInfo(
                        GlobalExpressionCallDAO.Attribute.FDR_P_VALUE_DESCENDANT_COND_INFO,
                        daoDataTypes, null));
                if (!daoDataTypesTrustedForAbsentCalls.isEmpty()) {
                    pValAttributes.add(new GlobalExpressionCallDAO.AttributeInfo(
                            GlobalExpressionCallDAO.Attribute.FDR_P_VALUE_COND_INFO,
                            daoDataTypesTrustedForAbsentCalls, null));
                    pValAttributes.add(new GlobalExpressionCallDAO.AttributeInfo(
                            GlobalExpressionCallDAO.Attribute.FDR_P_VALUE_DESCENDANT_COND_INFO,
                            daoDataTypesTrustedForAbsentCalls, null));
                }
                return pValAttributes.stream();

            } else if (attr.equals(CallService.Attribute.P_VALUE_INFO_EACH_DATA_TYPE)) {

                return daoDataTypes.stream()
                        .flatMap(dt -> Stream.of(
                                new GlobalExpressionCallDAO.AttributeInfo(
                                        GlobalExpressionCallDAO.Attribute.FDR_P_VALUE_COND_INFO,
                                        EnumSet.of(dt), null),
                                new GlobalExpressionCallDAO.AttributeInfo(
                                        GlobalExpressionCallDAO.Attribute.FDR_P_VALUE_DESCENDANT_COND_INFO,
                                        EnumSet.of(dt), null)));

            } else if (attr.equals(CallService.Attribute.GENE)) {

                return Stream.of(new GlobalExpressionCallDAO.AttributeInfo(
                        GlobalExpressionCallDAO.Attribute.BGEE_GENE_ID));

            } else if (attr.equals(CallService.Attribute.OBSERVED_DATA)) {

                //TODO: actually why do we use getAllPossibleCondParamCombinations in DAO?
                //We could generate the combination in bgee-core and just convert them
                return ConditionDAO.Attribute.getAllPossibleCondParamCombinations(daoCondParamComb)
                        .stream().map(comb -> new GlobalExpressionCallDAO.AttributeInfo(
                                GlobalExpressionCallDAO.Attribute.DATA_TYPE_OBSERVATION_COUNT_INFO,
                                daoDataTypes, comb));

            } else if (attr.equals(CallService.Attribute.MEAN_RANK) ||
                    attr.equals(CallService.Attribute.EXPRESSION_SCORE) ||
                    attr.equals(CallService.Attribute.GENE_QUAL_EXPR_LEVEL) ||
                    attr.equals(CallService.Attribute.ANAT_ENTITY_QUAL_EXPR_LEVEL)) {

                Set<GlobalExpressionCallDAO.AttributeInfo> rankAttributes = new HashSet<>();
                rankAttributes.add(new GlobalExpressionCallDAO.AttributeInfo(
                        GlobalExpressionCallDAO.Attribute.MEAN_RANK,
                        daoDataTypes, null));
                //We need to know the species to compute expression scores,
                //in order to retrieve the max rank in that species.
                if (attr.equals(CallService.Attribute.EXPRESSION_SCORE)) {
                    //The species info can be retrieved either from the gene or from the condition
                    if (!attributes.contains(CallService.Attribute.GENE) &&
                            Collections.disjoint(attributes,
                                    CallService.Attribute.getAllConditionParameters())) {
                        rankAttributes.add(new GlobalExpressionCallDAO.AttributeInfo(
                                GlobalExpressionCallDAO.Attribute.BGEE_GENE_ID));
                    }
                }
                return rankAttributes.stream();

            } else if (attr.equals(CallService.Attribute.DATA_TYPE_RANK_INFO)) {

                return Stream.of(new GlobalExpressionCallDAO.AttributeInfo(
                        GlobalExpressionCallDAO.Attribute.DATA_TYPE_RANK_INFO,
                        daoDataTypes, null));

            } else {
                throw log.throwing(new IllegalStateException(
                        "Unsupported Attributes from CallService: " + attr));
            }
        }).collect(Collectors.toSet()));
    }

    private LinkedHashMap<GlobalExpressionCallDAO.OrderingAttributeInfo, DAO.Direction>
    convertServiceOrderingAttrToGlobalExprDAOOrderingAttr(ExpressionCallFilter2 callFilter) {
        log.traceEntry("{}", callFilter);
        //for now we always order by bgeeGeneId, conditionId
        LinkedHashMap<GlobalExpressionCallDAO.OrderingAttributeInfo, DAO.Direction> orderAttrs =
                new LinkedHashMap<>();
        orderAttrs.put(
                new GlobalExpressionCallDAO.OrderingAttributeInfo(
                        GlobalExpressionCallDAO.OrderingAttribute.BGEE_GENE_ID),
                DAO.Direction.ASC);
        orderAttrs.put(
                new GlobalExpressionCallDAO.OrderingAttributeInfo(
                        GlobalExpressionCallDAO.OrderingAttribute.GLOBAL_CONDITION_ID),
                DAO.Direction.ASC);

        return log.traceExit(orderAttrs);
    }

    
}
