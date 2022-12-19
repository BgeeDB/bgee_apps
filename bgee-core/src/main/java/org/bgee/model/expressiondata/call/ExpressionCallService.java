package org.bgee.model.expressiondata.call;

import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.expressiondata.call.CallDAOFilter;
import org.bgee.model.dao.api.expressiondata.call.CallObservedDataDAOFilter;
import org.bgee.model.dao.api.expressiondata.call.CallObservedDataDAOFilter2;
import org.bgee.model.dao.api.expressiondata.call.ConditionDAO;
import org.bgee.model.dao.api.expressiondata.call.DAOCallFilter;
import org.bgee.model.dao.api.expressiondata.call.DAOConditionFilter;
import org.bgee.model.dao.api.expressiondata.call.DAOConditionFilter2;
import org.bgee.model.dao.api.expressiondata.call.DAOFDRPValueFilter;
import org.bgee.model.dao.api.expressiondata.call.DAOFDRPValueFilter2;
import org.bgee.model.dao.api.expressiondata.rawdata.DAORawDataConditionFilter;
import org.bgee.model.dao.api.expressiondata.rawdata.DAORawDataFilter;
import org.bgee.model.expressiondata.ExpressionDataService;
import org.bgee.model.expressiondata.baseelements.ConditionParameter;
import org.bgee.model.expressiondata.call.CallFilter.ExpressionCallFilter;
import org.bgee.model.expressiondata.call.CallFilter.ExpressionCallFilter2;
import org.bgee.model.expressiondata.rawdata.RawDataProcessedFilter;
import org.bgee.model.expressiondata.rawdata.baseelements.RawDataCondition;
import org.bgee.model.gene.Gene;
import org.bgee.model.gene.GeneBioType;
import org.bgee.model.gene.GeneFilter;
import org.bgee.model.source.Source;
import org.bgee.model.species.Species;
import org.bgee.model.ServiceFactory;

public class ExpressionCallService extends CallServiceParent {
    private final static Logger log = LogManager.getLogger(ExpressionCallService.class.getName());

    public ExpressionCallService(ServiceFactory serviceFactory) {
        this(serviceFactory, new CallServiceUtils());
    }
    public ExpressionCallService(ServiceFactory serviceFactory, CallServiceUtils utils) {
        super(serviceFactory, utils);
    }

    public ExpressionCallLoader loadCallLoader(ExpressionCallFilter filter) {
        log.traceEntry("{}", filter);
        //TODO
        return null;
    }

    public ExpressionCallProcessedFilter processExpressionCallFilter(ExpressionCallFilter2 filter) {
        log.traceEntry("{}", filter);

        //We load the GeneBioTypes to be used in this method and in RawDataLoader
        Map<Integer, GeneBioType> geneBioTypeMap = loadGeneBioTypeMap(this.geneDAO);
        //Sources to be used by the RawDataLoader
        Map<Integer, Source> sourceMap = this.getServiceFactory().getSourceService()
                .loadSourcesByIds(null);

        //It's OK that the filter is null if we want to retrieve any raw data
        if (filter == null) {
            return log.traceExit(new ExpressionCallProcessedFilter(filter,
                    //Important to provide a HashSet here, a null value means
                    //"we could not find conditions matching the parameters,
                    //thus there will be no result and no query done".
                    //While here we want to say "give me all results".
                    new HashSet<>(),
                    null, null,
                    //load all Species, gene biotypes, and sources
                    this.loadSpeciesMap(null, false, null), geneBioTypeMap, sourceMap));
        }

        Map<Integer, Species> speciesMap = this.loadSpeciesMap(filter.getSpeciesIdsConsidered(),
                false, null);

        //Now, we load specific genes that can be queried (and not all genes of a species
        //if a GeneFilter contains no gene ID)
        Set<GeneFilter> geneFiltersToUse = filter.getGeneFilters().stream()
                .filter(f -> !f.getGeneIds().isEmpty())
                .collect(Collectors.toSet());
        Map<Integer, Gene> requestedGeneMap = geneFiltersToUse.isEmpty()? new HashMap<>():
            loadGeneMapFromGeneFilters(geneFiltersToUse, speciesMap, geneBioTypeMap, this.geneDAO);

        return null;
//        //Now, we load specific conditions that can be queried (and not all conditions
//        //of a species if a condition filter contains no filtering on condition parameters).
//        Set<DAOConditionFilter2> daoCondFilters =
//            convertConditionFilterToDAOConditionFilter(filter.getConditionFilters(),
//                    this.ontService, filter.getSpeciesIdsConsidered());
//        Set<DAOConditionFilter2> daoCondFiltersToUse = daoCondFilters.stream()
//                .filter(f -> !f.areAllCondParamFiltersEmpty())
//                .collect(Collectors.toSet());
//        Map<Integer, Condition> requestedCondMap = daoCondFiltersToUse.isEmpty()?
//                new HashMap<>():
//                loadGlobalConditionMap(speciesMap.values(), daoCondFiltersToUse,
//                        null, this.conditionDAO, this.anatEntityService, this.devStageService);
//
//        //Maybe we have no matching conditions for some condition filters,
//        //it means we should have no result in the related species.
//        //we have to identify the species for which it is the case, to discard them,
//        //otherwise, with no condition IDs specified, we could retrieve all results
//        //for that species instead of no result.
//        //
//        //Of note, we don't have this problem with gene IDs: users can only select valid gene IDs,
//        //and loadGeneMapFromGeneFilters throws an exception if a gene ID is not found.
//        Set<Integer> speciesIdsWithCondFound = requestedCondMap.values().stream()
//                .map(c -> c.getSpeciesId())
//                .collect(Collectors.toSet());
//        Set<Integer> speciesIdsWithCondRequested = filter.getConditionFilters().stream()
//                .filter(cf -> !cf.areAllCondParamFiltersEmpty())
//                //we use speciesMap.keySet() here, rather than filter.getSpeciesIdsConsidered(),
//                //because if filter.getSpeciesIdsConsidered() is empty, speciesMap will contain
//                //all the species.
//                .flatMap(f -> f.getSpeciesIds().isEmpty()? speciesMap.keySet().stream():
//                    f.getSpeciesIds().stream())
//                .collect(Collectors.toSet());
//        Set<Integer> speciesIdsWithNoResult = new HashSet<>(speciesIdsWithCondRequested);
//        speciesIdsWithNoResult.removeAll(speciesIdsWithCondFound);
//
//        //If some conditions were requested, but no condition was found, we can stop here,
//        //there will be no results (encoded by providing a null daoFilters collection
//        //to the ProcessedFilter). We don't need to check what was provided
//        //in the GeneFilters, because the class DataFilter check the consistency
//        //between the species requested in GeneFilters and ConditionFilters
//        if (!speciesIdsWithCondRequested.isEmpty() && speciesIdsWithCondFound.isEmpty()) {
//            return log.traceExit(new ExpressionCallProcessedFilter(filter, null,
//                    requestedGeneMap, requestedCondMap,
//                    speciesMap, geneBioTypeMap, sourceMap));
//        }
//
//        //if filter.getSpeciesIdsConsidered() is empty, we can create just one DAO DataFilter
//        //it means that there was no GeneFilter provided, only ConditionFilters targeting any species
//        Set<DAOCallFilter> daoFilters = new HashSet<>();
//        if (filter.getSpeciesIdsConsidered().isEmpty()) {
//            assert filter.getGeneFilters().isEmpty() && requestedGeneMap.isEmpty();
//
//            if (requestedCondMap.isEmpty() && filter.hasExperimentAssayIds()) {
//                daoFilters.add(new DAOCallFilter(filter.getExperimentIds(),
//                    filter.getAssayIds(), filter.getExperimentOrAssayIds()));
//                log.debug("DAORawDataFilter created for any species");
//            } else if (!requestedRawDataCondMap.isEmpty()) {
//                daoFilters.add(new DAOCallFilter(null, requestedRawDataCondMap.keySet(),
//                        filter.getExperimentIds(), filter.getAssayIds(),
//                        filter.getExperimentOrAssayIds()));
//                log.debug("DAORawDataFilter created with at least some condition IDs");
//            } else {
//                assert requestedRawDataCondMap.isEmpty() && !filter.hasExperimentAssayIds();
//                log.debug("No DAORawDataFilter created: no species, no genes, no conds, no exp/assay IDs");
//            }
//        } else {
//            daoFilters.addAll(filter.getSpeciesIdsConsidered().stream()
//                    .filter(speciesId -> !speciesIdsWithNoResult.contains(speciesId))
//                    .map(speciesId -> {
//                        Set<Integer> bgeeGeneIds = requestedGeneMap.entrySet().stream()
//                                .filter(e -> speciesId.equals(e.getValue().getSpecies().getId()))
//                                .map(e -> e.getKey())
//                                .collect(Collectors.toSet());
//                        Set<Integer> rawCondIds = requestedRawDataCondMap.entrySet().stream()
//                                .filter(e -> speciesId.equals(e.getValue().getSpeciesId()))
//                                .map(e -> e.getKey())
//                                .collect(Collectors.toSet());
//                        if (bgeeGeneIds.isEmpty() && rawCondIds.isEmpty()) {
//                            log.debug("DAORawDataFilter created without genes nor cond. for species ID: {}",
//                                    speciesId);
//                            return new DAORawDataFilter(Set.of(speciesId), filter.getExperimentIds(),
//                                    filter.getAssayIds(), filter.getExperimentOrAssayIds());
//                        }
//                        log.debug("Complete DAORawDataFilter created for species ID: {}", speciesId);
//                        return new DAORawDataFilter(bgeeGeneIds, rawCondIds, filter.getExperimentIds(),
//                                filter.getAssayIds(), filter.getExperimentOrAssayIds());
//                    })
//                    .collect(Collectors.toSet()));
//        }
//        log.debug("daoFilters: {}", daoFilters);
//
//        return log.traceExit(new ExpressionCallProcessedFilter(filter, daoFilters,
//                requestedGeneMap, requestedRawDataCondMap,
//                speciesMap, geneBioTypeMap, sourceMap));
    }

    private DAOCallFilter convertCallFilterToCallDAOFilter(Map<Integer, Gene> geneMap,
            Map<Integer, Condition> condMap, ExpressionCallFilter2 callFilter,
            EnumSet<ConditionParameter> condParamCombination) {
        log.traceEntry("{}, {}, {}, {}", geneMap, condMap, callFilter, condParamCombination);

        if (callFilter == null) {
            return log.traceExit((DAOCallFilter) null);
        }
        Set<Integer> speciesIds = new HashSet<>();
        // *********************************
        // Gene and species IDs filters
        //**********************************
        //Retrieve the species IDs for which no gene IDs are specified
        speciesIds.addAll(callFilter.getGeneFilters().stream()
                .filter(f -> f.getGeneIds().isEmpty())
                .map(f -> f.getSpeciesId())
                .collect(Collectors.toSet()));
        //For the gene filters for which gene IDs were specified,
        //they should already have been retrieve into geneMap
        Set<Integer> bgeeGeneIds = geneMap.keySet();

        // *********************************
        // Condition filter
        //**********************************
        //Retrieve the species IDs for which no other condition parameters are specified
        speciesIds.addAll(callFilter.getConditionFilters().stream()
                .filter(f -> f.areAllCondParamFiltersEmpty())
                .flatMap(f -> f.getSpeciesIds().stream())
                .collect(Collectors.toSet()));
        //For the condition filters for which parameters were specified,
        //they should already have been retrieve into condMap
        Set<Integer> condIds = condMap.keySet();

        // *********************************
        // Call observed data filter
        //**********************************
        Set<CallObservedDataDAOFilter2> daoObservedDataFilters = callFilter.getCallObservedDataFilter()
                .entrySet().stream().map(e -> new CallObservedDataDAOFilter2(
                        this.utils.convertDataTypeToDAODataType(callFilter.getDataTypeFilters()),
                        this.utils.convertCondParamToDAOCondParams(e.getKey()),
                        e.getValue())
                ).collect(Collectors.toSet());

        // *********************************
        // P-value filters
        //**********************************
        EnumSet<ConditionDAO.ConditionParameter> convertedCondParamComb =
                this.utils.convertCondParamToDAOCondParams(condParamCombination);
        Collection<Set<DAOFDRPValueFilter2>> pValueFilters = this.utils.generateExprQualDAOPValFilters(
                callFilter, convertedCondParamComb, PRESENT_LOW_LESS_THAN_OR_EQUALS_TO,
                PRESENT_HIGH_LESS_THAN_OR_EQUALS_TO, ABSENT_LOW_GREATER_THAN,
                ABSENT_HIGH_GREATER_THAN);


        // *********************************
        // Final result
        //**********************************
        return log.traceExit(new DAOCallFilter(
                //species
                speciesIds,
                // gene IDs
                bgeeGeneIds,
                //condition IDs
                condIds,
                //CallObservedDataDAOFilters
                daoObservedDataFilters,
                //DAOFDRPValueFilters
                pValueFilters
                ));
    }
}
