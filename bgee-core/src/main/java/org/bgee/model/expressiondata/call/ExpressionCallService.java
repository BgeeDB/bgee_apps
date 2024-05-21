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
import org.bgee.model.expressiondata.call.ExpressionCallProcessedFilter.ExpressionCallProcessedFilterConditionPart;
import org.bgee.model.expressiondata.call.ExpressionCallProcessedFilter.ExpressionCallProcessedFilterGeneSpeciesPart;
import org.bgee.model.expressiondata.call.ExpressionCallProcessedFilter.ExpressionCallProcessedFilterInvariablePart;
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

    /**
     * Since the information contained in an {@code ExpressionCallProcessedFilterInvariablePart}
     * will be the same in all {@code ExpressionCallProcessedFilter}s, we store it statically
     * not to retrieve it for each {@code ExpressionCallProcessedFilter} created.
     *
     * @see #loadIfNecessaryAndGetInvariablePart()
     * @see #processExpressionCallFilter(ExpressionCallFilter2,
     * ExpressionCallProcessedFilterGeneSpeciesPart,
     * ExpressionCallProcessedFilterConditionPart,
     * ExpressionCallProcessedFilterInvariablePart)
     */
    private static ExpressionCallProcessedFilterInvariablePart PROCESSED_FILTER_INVARIABLE_PART;
    /**
     * When an {@code ExpressionCallProcessedFilter} is generated for a filtering requesting
     * all species and any gene, the {@code ExpressionCallProcessedFilterGeneSpeciesPart}
     * is always the same, we store it here.
     *
     * @see #loadIfNecessaryAndGetGenericGeneSpeciesPart()
     * @see #processExpressionCallFilter(ExpressionCallFilter2,
     * ExpressionCallProcessedFilterGeneSpeciesPart,
     * ExpressionCallProcessedFilterConditionPart,
     * ExpressionCallProcessedFilterInvariablePart)
     */
    private static ExpressionCallProcessedFilterGeneSpeciesPart PROCESSED_ALL_SPECIES_NO_GENE_PART;

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

    /**
     * Allow to obtain the {@code ExpressionCallProcessedFilter} that is used internally
     * by an {@code ExpressionCallLoader} to retrieve data. An {@code ExpressionCallLoader}
     * can then be obtained using the method {@code #getCallLoader(ExpressionCallProcessedFilter)}.
     * <p>
     * This method  is provided because processing an {@link ExpressionCallFilter2} can be computer-intensive.
     * When calling the different methods of an {@code ExpressionCallLoader} object, we don't want
     * to re-process this information at each call. An {@code ExpressionCallProcessedFilter} is used
     * to store this information outside of an {@link ExpressionCallLoader},
     * because a {@code ExpressionCallLoader} is a {@code Service}, and holds a connection to a data source.
     * If we wanted to store this pre-processed information to, for instance, be reused by different threads,
     * storing it in an {@code ExpressionCallLoader} could maintain the connection open.
     * It can also be beneficial to cache {@code ExpressionCallProcessedFilter}s that are difficult
     * to compute. For instance, one might want to provide a pagination mechanism to iterate expression calls
     * obtained from an {@code ExpressionCallLoader}, without re-processing the {@code ExpressionCallFilter2}
     * for each page. (It would be possible to cache the {@code ExpressionCallLoader} directly,
     * but since it uses a connection to a data source to obtain results, it is more convenient to store
     * the underlying {@code ExpressionCallProcessedFilter} than the {@code ExpressionCallLoader} itself.)
     * <p>
     * This method can be used to obtain such a {@code ExpressionCallProcessedFilter}.
     * The method {@link ExpressionCallLoader#getProcessedFilter()} could also be used
     * to obtain it from an existing {@code ExpressionCallLoader}
     * (see {@link #loadCallLoader(ExpressionCallFilter2)} to directly obtain
     * an {@code ExpressionCallLoader} from an {@code ExpressionCallFilter2}).
     * <p>
     * To obtain an {@code ExpressionCallLoader} from an existing {@code ExpressionCallProcessedFilter},
     * the method {@link #getCallLoader(ExpressionCallProcessedFilter)} can be used.
     * 
     * @param filter    The {@code ExpressionCallFilter2} to process to obtain an {@code ExpressionCallProcessedFilter}.
     * @return          An {@code ExpressionCallProcessedFilter} containing pre-processed information computed
     *                  from {@code filter}, that can then be reused multiple times by {@code ExpressionCallLoader}s.
     * @see #getCallLoader(ExpressionCallProcessedFilter)
     * @see #loadCallLoader(ExpressionCallFilter2)
     */
    public ExpressionCallProcessedFilter processExpressionCallFilter(ExpressionCallFilter2 filter) {
        log.traceEntry("{}", filter);
        return log.traceExit(this.processExpressionCallFilter(filter, null, null, null));
    }

    /**
     * Same as {@link #processExpressionCallFilter(ExpressionCallFilter2)}, but allowing to provide
     * different parts composing an {@code ExpressionCallProcessedFilter}, to avoid recomputing
     * some of them. It is interesting to use when some parts of the filtering information
     * of an {@code ExpressionCallFilter2} were computer-intensive to process,
     * and could be reused in conjunction with other filtering information. For instance,
     * the condition information part for an {@code ExpressionCallFilter2} could have been slow
     * to process, and could be reused to process a new {@code ExpressionCallFilter2},
     * having the same condition filtering but a different gene filtering.
     *
     * @param filter            The {@code ExpressionCallFilter2} to process to obtain
     *                          an {@code ExpressionCallProcessedFilter}.
     * @param geneSpeciesPart   An {@code ExpressionCallProcessedFilterGeneSpeciesPart}
     *                          storing gene filtering information obtained from a previous
     *                          {@code ExpressionCallProcessedFilter}. Can be {@code null}
     *                          if no already-processed gene filtering information exists
     *                          for {@code filter}.
     * @param conditionPart     An {@code ExpressionCallProcessedFilterConditionPart}
     *                          storing condition information obtained from a previous
     *                          {@code ExpressionCallProcessedFilter}. Can be {@code null}
     *                          if no already-processed condition information exists
     *                          for {@code filter}.
     * @param invariablePart    An {@code ExpressionCallProcessedFilterInvariablePart}
     *                          storing the invariable information obtained from a previous
     *                          {@code ExpressionCallProcessedFilter}. Can be {@code null}
     *                          if no already-processed invariable information exists
     *                          for {@code filter}.
     * @return                  An {@code ExpressionCallProcessedFilter} containing pre-processed information
     *                          that can be reused multiple times by {@code ExpressionCallLoader}s.
     */
    public ExpressionCallProcessedFilter processExpressionCallFilter(ExpressionCallFilter2 filter,
            ExpressionCallProcessedFilterGeneSpeciesPart geneSpeciesPart,
            ExpressionCallProcessedFilterConditionPart conditionPart,
            ExpressionCallProcessedFilterInvariablePart invariablePart) {
        log.traceEntry("{}, {}, {}, {}", filter, geneSpeciesPart, conditionPart, invariablePart);

        if (geneSpeciesPart != null && filter != null &&
                !geneSpeciesPart.getGeneFilters().equals(filter.getGeneFilters())) {
            throw log.throwing(new IllegalArgumentException("The ExpressionCallProcessedFilterGeneSpeciesPart "
                    + "does not correspond to the ExpressionCallFilter2"));
        }
        if (conditionPart != null && filter != null &&
                !conditionPart.getConditionFilters().equals(filter.getConditionFilters())) {
            throw log.throwing(new IllegalArgumentException("The ExpressionCallProcessedFilterConditionPart "
                    + "does not correspond to the ExpressionCallFilter2"));
        }
        final ExpressionCallProcessedFilterInvariablePart procInvariablePart = invariablePart != null?
                invariablePart: loadIfNecessaryAndGetInvariablePart();
        final ExpressionCallProcessedFilterGeneSpeciesPart procGeneSpeciesPart =
                geneSpeciesPart != null?
                    geneSpeciesPart:
                    (filter == null || filter.isEmptyFilter()?
                        loadIfNecessaryAndGetGenericGeneSpeciesPart():
                        loadGeneSpeciesPart(filter, procInvariablePart.getGeneBioTypeMap()));

        //It's OK that the filter is null or empty if we want to retrieve any raw data
        if (filter == null || filter.isEmptyFilter()) {
            return log.traceExit(new ExpressionCallProcessedFilter(filter,
                    //Important to provide a HashSet here, a null value means
                    //"we could not find conditions matching the parameters,
                    //thus there will be no result and no query done".
                    //While here we want to say "give me all results".
                    new HashSet<>(),

                    procGeneSpeciesPart,
                    null,
                    procInvariablePart,
                    GLOBAL_RANK, EXPRESSION_SCORE_MIN_VALUE, EXPRESSION_SCORE_MAX_VALUE,
                    PRESENT_LOW_LESS_THAN_OR_EQUALS_TO,
                    PRESENT_HIGH_LESS_THAN_OR_EQUALS_TO, ABSENT_LOW_GREATER_THAN,
                    ABSENT_HIGH_GREATER_THAN));
        }

        //At this point, the filter cannot be null and we can use the method loadConditionPart
        assert filter != null;
        final ExpressionCallProcessedFilterConditionPart procConditionPart = conditionPart != null?
                conditionPart: loadConditionPart(filter, procGeneSpeciesPart.getSpeciesMap());


        //At this point, there should always be at least a GeneFilter, it is mandatory
        //to provide one if the filter is not empty (checked just above).
        assert filter.getGeneFilter() != null;
        //And there should be exactly one species selected.
        //NOTE: the code below is still written as if it was permitted to have several species,
        //but it is not the case
        assert filter.getSpeciesIdsConsidered().size() == 1;


        //Maybe we have no matching conditions at all for some species,
        //it means we should have no result in the related species.
        //we have to identify the species for which it is the case, to discard them,
        //otherwise, with no condition IDs specified, we could retrieve all results
        //for that species instead of no result.
        //
        //Of note, we don't have this problem with gene IDs: users can only select valid gene IDs,
        //and loadGeneMapFromGeneFilters throws an exception if a gene ID is not found.
        Set<Integer> speciesIdsWithCondFound = procConditionPart.getRequestedConditionMap().values().stream()
                .map(c -> c.getSpeciesId())
                .collect(Collectors.toSet());
        Set<Integer> speciesIdsWithCondRequested = filter.getConditionFilters().stream()
                //we use speciesMap.keySet() here, rather than filter.getSpeciesIdsConsidered(),
                //because if filter.getSpeciesIdsConsidered() is empty, speciesMap will contain
                //all the species.
                .flatMap(f -> f.getSpeciesId() == null? procGeneSpeciesPart.getSpeciesMap().keySet().stream():
                    Stream.of(f.getSpeciesId()))
                .collect(Collectors.toSet());
        Set<Integer> speciesIdsWithNoResult = new HashSet<>(speciesIdsWithCondRequested);
        speciesIdsWithNoResult.removeAll(speciesIdsWithCondFound);

        //If some specific conditions were requested for all species, but no condition was found,
        //we can stop here, there will be no results (encoded by providing a null daoFilters collection
        //to the ProcessedFilter). We don't need to check what was provided
        //in the GeneFilters, because the class DataFilter check the consistency
        //between the species requested in GeneFilters and ConditionFilters
        if (procGeneSpeciesPart.getSpeciesMap().keySet().equals(speciesIdsWithCondRequested) &&
                speciesIdsWithCondFound.isEmpty()) {
            return log.traceExit(new ExpressionCallProcessedFilter(filter, null,
                    procGeneSpeciesPart,
                    procConditionPart,
                    procInvariablePart,
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
                procGeneSpeciesPart.getRequestedGeneMap().entrySet().stream()
                .filter(e -> !speciesIdsWithNoResult.contains(
                        e.getValue().getSpecies().getId()))
                .map(e -> e.getKey())
                .collect(Collectors.toSet());
        assert !bgeeGeneIds.isEmpty();
        CallObservedDataDAOFilter2 obsDataFilter = this.utils.convertCallObservedDataToDAO(filter);
        DAOCallFilter daoFilter = new DAOCallFilter(
                //XXX: this speciesIds argument should be removed from the DAOCallFilter
                null,
                bgeeGeneIds,
                procConditionPart.getRequestedConditionMap().keySet(),
                obsDataFilter == null? Set.of(): Set.of(obsDataFilter),
                this.utils.generateExprQualDAOPValFilters(
                        filter, PRESENT_LOW_LESS_THAN_OR_EQUALS_TO,
                        PRESENT_HIGH_LESS_THAN_OR_EQUALS_TO, ABSENT_LOW_GREATER_THAN,
                        ABSENT_HIGH_GREATER_THAN)
                );
        log.debug("daoFilter: {}", daoFilter);

        return log.traceExit(new ExpressionCallProcessedFilter(filter, Set.of(daoFilter),
                procGeneSpeciesPart,
                procConditionPart,
                procInvariablePart,
                GLOBAL_RANK, EXPRESSION_SCORE_MIN_VALUE, EXPRESSION_SCORE_MAX_VALUE,
                PRESENT_LOW_LESS_THAN_OR_EQUALS_TO,
                PRESENT_HIGH_LESS_THAN_OR_EQUALS_TO, ABSENT_LOW_GREATER_THAN,
                ABSENT_HIGH_GREATER_THAN));
    }

    private ExpressionCallProcessedFilterInvariablePart loadIfNecessaryAndGetInvariablePart() {
        //We don't fear a race condition here, because this information is cheap to compute
        //and does not change, so no problem to retrieve and set it multiple times.
        if (PROCESSED_FILTER_INVARIABLE_PART == null) {
            //We load the GeneBioTypes to be used in this method and in RawDataLoader
            Map<Integer, GeneBioType> geneBioTypeMap = loadGeneBioTypeMap(this.geneDAO);
            //Sources to be used by the RawDataLoader
            Map<Integer, Source> sourceMap = this.getServiceFactory().getSourceService()
                    .loadSourcesByIds(null);
            //Retrieve max rank for the requested species if EXPRESSION_SCORE requested
            //(the max rank is required to convert mean ranks into expression scores)
            //TODO: in a future version with Attributes, to retrieve only if necessary
            Map<Integer, ConditionRankInfoTO> maxRankPerSpecies = conditionDAO
                    .getMaxRanks(null,
                            //We always request the max rank over all data types,
                            //independently of the data types requested in the query,
                            //because ranks are all normalized based on the max rank over all data types
                            null);
            PROCESSED_FILTER_INVARIABLE_PART =
                    new ExpressionCallProcessedFilterInvariablePart(geneBioTypeMap, sourceMap,
                            maxRankPerSpecies);
        }
        return log.traceExit(PROCESSED_FILTER_INVARIABLE_PART);
    }
    private ExpressionCallProcessedFilterGeneSpeciesPart loadIfNecessaryAndGetGenericGeneSpeciesPart() {

        //We don't fear a race condition here, because this information is cheap to compute
        //and does not change, so no problem to retrieve and set it multiple times.
        if (PROCESSED_ALL_SPECIES_NO_GENE_PART == null) {
            PROCESSED_ALL_SPECIES_NO_GENE_PART = new ExpressionCallProcessedFilterGeneSpeciesPart(
                    null,
                    null,
                    this.loadSpeciesMap(null, false, null));
        }
        return log.traceExit(PROCESSED_ALL_SPECIES_NO_GENE_PART);
    }
    private ExpressionCallProcessedFilterGeneSpeciesPart loadGeneSpeciesPart(ExpressionCallFilter2 filter,
            Map<Integer, GeneBioType> geneBioTypeMap) {
        log.traceEntry("{}, {}", filter, geneBioTypeMap);

        Map<Integer, Species> speciesMap = this.loadSpeciesMap(filter.getSpeciesIdsConsidered(),
                false, null);
        //To configure the DAOCallFilter we need to always have bgeeGeneIds,
        //so we retrieve all genes of the requested species.
        Map<Integer, Gene> requestedGeneMap = loadGeneMapFromGeneFilters(filter.getGeneFilters(),
                speciesMap, geneBioTypeMap, this.geneDAO);

        return log.traceExit(new ExpressionCallProcessedFilterGeneSpeciesPart(
                filter.getGeneFilters(),
                requestedGeneMap,
                speciesMap));
    }
    private ExpressionCallProcessedFilterConditionPart loadConditionPart(ExpressionCallFilter2 filter,
            Map<Integer, Species> speciesMap) {
        log.traceEntry("{}, {}", filter, speciesMap);

        //Now, we load specific conditions that can be queried. Again, we need to retrieve
        //all of them to configure the DAOCallFilter, even if there is a large number.
        Set<DAOConditionFilter2> daoCondFilters =
                this.utils.convertConditionFiltersToDAOConditionFilters(filter.getConditionFilters(),
                        this.ontService, this.anatEntityService, filter.getSpeciesIdsConsidered());
        Map<Integer, Condition2> requestedCondMap = daoCondFilters.isEmpty()?
                new HashMap<>():
                    this.utils.loadGlobalConditionMap(speciesMap.values(),
                            daoCondFilters,
                            this.utils.convertCondParamsToDAOCondAttributes(filter.getCondParamCombination()),
                            this.conditionDAO, this.anatEntityService, this.devStageService,
                            this.sexService, this.strainService);
        return log.traceExit(new ExpressionCallProcessedFilterConditionPart(
                filter.getConditionFilters(),
                requestedCondMap));
    }
}