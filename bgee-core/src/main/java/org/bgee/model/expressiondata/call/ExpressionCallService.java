package org.bgee.model.expressiondata.call;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.expressiondata.call.CallObservedDataDAOFilter2;
import org.bgee.model.dao.api.expressiondata.call.DAOCallFilter;
import org.bgee.model.dao.api.expressiondata.call.DAOConditionFilter2;
import org.bgee.model.dao.api.expressiondata.call.ConditionDAO.ConditionRankInfoTO;
import org.bgee.model.expressiondata.call.CallFilter.ExpressionCallFilter2;
import org.bgee.model.gene.Gene;
import org.bgee.model.gene.GeneBioType;
import org.bgee.model.source.Source;
import org.bgee.model.species.Species;
import org.bgee.model.ServiceFactory;

//TODO: create class Attribute with only a subset of the CallService.Attributes,
//and a new Attribute CONDITION, rather than the list of the condition parameters.
//See comment on top of method ExpressionCallLoader.loadData.
//These Attributes should be accepted by the method loadCallLoader, along with
//the selected condition parameters (that will affect both the call filter AND
//the Condition attributes retrieved).
//Obviously, then both the attributes and the condition parameters should be stored
//in the ExpressionCallProcessedFilter (maybe choose a better name then?)
public class ExpressionCallService extends CallServiceParent {
    private final static Logger log = LogManager.getLogger(ExpressionCallService.class.getName());

    public ExpressionCallService(ServiceFactory serviceFactory) {
        this(serviceFactory, new CallServiceUtils());
    }
    public ExpressionCallService(ServiceFactory serviceFactory, CallServiceUtils utils) {
        super(serviceFactory, utils);
    }

    public ExpressionCallLoader loadCallLoader(ExpressionCallFilter2 filter) {
        log.traceEntry("{}", filter);
        return log.traceExit(this.getCallLoader(this.processExpressionCallFilter(filter)));
    }
    public ExpressionCallLoader getCallLoader(ExpressionCallProcessedFilter processedFilter) {
        log.traceEntry("{}", processedFilter);
        return log.traceExit(new ExpressionCallLoader(processedFilter, this.getServiceFactory()));
    }

    public ExpressionCallProcessedFilter processExpressionCallFilter(ExpressionCallFilter2 filter) {
        log.traceEntry("{}", filter);

        //We load the GeneBioTypes to be used in this method and in RawDataLoader
        Map<Integer, GeneBioType> geneBioTypeMap = loadGeneBioTypeMap(this.geneDAO);
        //Sources to be used by the RawDataLoader
        Map<Integer, Source> sourceMap = this.getServiceFactory().getSourceService()
                .loadSourcesByIds(null);

        //It's OK that the filter is null or empty if we want to retrieve any raw data
        if (filter == null || filter.isEmptyFilter()) {
            return log.traceExit(new ExpressionCallProcessedFilter(filter,
                    //Important to provide a HashSet here, a null value means
                    //"we could not find conditions matching the parameters,
                    //thus there will be no result and no query done".
                    //While here we want to say "give me all results".
                    new HashSet<>(),
                    null, null,
                    //load all Species, gene biotypes, and sources
                    this.loadSpeciesMap(null, false, null), geneBioTypeMap, sourceMap,
                    conditionDAO
                    .getMaxRanks(null,
                            //We always request the max rank over all data types,
                            //independently of the data types requested in the query,
                            //because ranks are all normalized based on the max rank over all data types
                            null),
                    GLOBAL_RANK, EXPRESSION_SCORE_MIN_VALUE, EXPRESSION_SCORE_MAX_VALUE,
                    PRESENT_LOW_LESS_THAN_OR_EQUALS_TO,
                    PRESENT_HIGH_LESS_THAN_OR_EQUALS_TO, ABSENT_LOW_GREATER_THAN,
                    ABSENT_HIGH_GREATER_THAN));
        }
        //At this point, there should always be at least a GeneFilter, it is mandatory
        //to provide one if the filter is not empty (checked just above).
        assert filter.getGeneFilter() != null;
        //And there should be exactly one species selected.
        //NOTE: the code below is still written as if it was permitted to have several species,
        //but it is not the case
        assert filter.getSpeciesIdsConsidered().size() == 1;

        Map<Integer, Species> speciesMap = this.loadSpeciesMap(filter.getSpeciesIdsConsidered(),
                false, null);
        //Retrieve max rank for the requested species if EXPRESSION_SCORE requested
        //(the max rank is required to convert mean ranks into expression scores)
        //TODO: in a future version with Attributes, to retrieve only if necessary
        Map<Integer, ConditionRankInfoTO> maxRankPerSpecies = conditionDAO
                .getMaxRanks(speciesMap.keySet(),
                        //We always request the max rank over all data types,
                        //independently of the data types requested in the query,
                        //because ranks are all normalized based on the max rank over all data types
                        null);

        //To configure the DAOCallFilter we need to always have bgeeGeneIds,
        //so we retrieve all genes of the requested species.
        Map<Integer, Gene> requestedGeneMap = loadGeneMapFromGeneFilters(filter.getGeneFilters(),
                speciesMap, geneBioTypeMap, this.geneDAO);

        //Now, we load specific conditions that can be queried. Again, we need to retrieve
        //all of them to configure the DAOCallFilter, even if there is a large number.
        Set<DAOConditionFilter2> daoCondFilters =
            this.utils.convertConditionFiltersToDAOConditionFilters(filter.getConditionFilters(),
                    this.ontService, filter.getSpeciesIdsConsidered());
        Map<Integer, Condition2> requestedCondMap = daoCondFilters.isEmpty()?
                new HashMap<>():
                this.utils.loadGlobalConditionMap(speciesMap.values(), daoCondFilters,
                        null, this.conditionDAO, this.anatEntityService, this.devStageService,
                        this.sexService, this.strainService);

        //Maybe we have no matching conditions at all for some species,
        //it means we should have no result in the related species.
        //we have to identify the species for which it is the case, to discard them,
        //otherwise, with no condition IDs specified, we could retrieve all results
        //for that species instead of no result.
        //
        //Of note, we don't have this problem with gene IDs: users can only select valid gene IDs,
        //and loadGeneMapFromGeneFilters throws an exception if a gene ID is not found.
        Set<Integer> speciesIdsWithCondFound = requestedCondMap.values().stream()
                .map(c -> c.getSpeciesId())
                .collect(Collectors.toSet());
        Set<Integer> speciesIdsWithCondRequested = filter.getConditionFilters().stream()
                //we use speciesMap.keySet() here, rather than filter.getSpeciesIdsConsidered(),
                //because if filter.getSpeciesIdsConsidered() is empty, speciesMap will contain
                //all the species.
                .flatMap(f -> f.getSpeciesId() == null? speciesMap.keySet().stream():
                    Stream.of(f.getSpeciesId()))
                .collect(Collectors.toSet());
        Set<Integer> speciesIdsWithNoResult = new HashSet<>(speciesIdsWithCondRequested);
        speciesIdsWithNoResult.removeAll(speciesIdsWithCondFound);

        //If some specific conditions were requested for all species, but no condition was found,
        //we can stop here, there will be no results (encoded by providing a null daoFilters collection
        //to the ProcessedFilter). We don't need to check what was provided
        //in the GeneFilters, because the class DataFilter check the consistency
        //between the species requested in GeneFilters and ConditionFilters
        if (speciesMap.keySet().equals(speciesIdsWithCondRequested) && speciesIdsWithCondFound.isEmpty()) {
            return log.traceExit(new ExpressionCallProcessedFilter(filter, null,
                    requestedGeneMap, requestedCondMap,
                    speciesMap, geneBioTypeMap, sourceMap, maxRankPerSpecies,
                    GLOBAL_RANK, EXPRESSION_SCORE_MIN_VALUE, EXPRESSION_SCORE_MAX_VALUE,
                    PRESENT_LOW_LESS_THAN_OR_EQUALS_TO,
                    PRESENT_HIGH_LESS_THAN_OR_EQUALS_TO, ABSENT_LOW_GREATER_THAN,
                    ABSENT_HIGH_GREATER_THAN));
        }

        //*************************************
        // Create the DAO filter
        //*************************************
        //We need to remove gene IDs for which we know there will be no results
        //(a bit useless now, with the new system only one species can be targeted at most,
        //we would have already exited the method)
        Set<Integer> bgeeGeneIds =
                requestedGeneMap.entrySet().stream()
                .filter(e -> !speciesIdsWithNoResult.contains(
                        e.getValue().getSpecies().getId()))
                .map(e -> e.getKey())
                .collect(Collectors.toSet());
        assert !bgeeGeneIds.isEmpty();
        CallObservedDataDAOFilter2 obsDataFilter = this.utils.convertCallObservedDataToDAO(filter);
        DAOCallFilter daoFilter = new DAOCallFilter(
                //this speciesIds argument should be removed from the DAOCallFilter
                null,
                bgeeGeneIds,
                requestedCondMap.keySet(),
                obsDataFilter == null? Set.of(): Set.of(obsDataFilter),
                this.utils.generateExprQualDAOPValFilters(
                        filter, PRESENT_LOW_LESS_THAN_OR_EQUALS_TO,
                        PRESENT_HIGH_LESS_THAN_OR_EQUALS_TO, ABSENT_LOW_GREATER_THAN,
                        ABSENT_HIGH_GREATER_THAN)
                );
        log.debug("daoFilter: {}", daoFilter);

        return log.traceExit(new ExpressionCallProcessedFilter(filter, Set.of(daoFilter),
                requestedGeneMap, requestedCondMap,
                speciesMap, geneBioTypeMap, sourceMap, maxRankPerSpecies,
                GLOBAL_RANK, EXPRESSION_SCORE_MIN_VALUE, EXPRESSION_SCORE_MAX_VALUE,
                PRESENT_LOW_LESS_THAN_OR_EQUALS_TO,
                PRESENT_HIGH_LESS_THAN_OR_EQUALS_TO, ABSENT_LOW_GREATER_THAN,
                ABSENT_HIGH_GREATER_THAN));
    }
}
