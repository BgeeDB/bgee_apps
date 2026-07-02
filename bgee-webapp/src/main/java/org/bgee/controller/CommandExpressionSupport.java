package org.bgee.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.exception.InvalidRequestException;
import org.bgee.controller.user.User;
import org.bgee.controller.utils.BgeeCacheService;
import org.bgee.controller.utils.BgeeCacheService.CacheDefinition;
import org.bgee.controller.utils.BgeeCacheService.CacheType;
import org.bgee.model.BgeeEnum;
import org.bgee.model.ServiceFactory;
import org.bgee.model.anatdev.Sex.SexEnum;
import org.bgee.model.dao.api.expressiondata.call.ConditionDAO;
import org.bgee.model.expressiondata.BaseConditionFilter2.ComposedFilterIds;
import org.bgee.model.expressiondata.BaseConditionFilter2.FilterIds;
import org.bgee.model.expressiondata.baseelements.ConditionParameter;
import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.model.expressiondata.baseelements.SummaryQuality;
import org.bgee.model.expressiondata.baseelements.SummaryCallType.ExpressionSummary;
import org.bgee.model.expressiondata.call.ConditionFilter2;
import org.bgee.model.expressiondata.call.ExpressionCallLoader;
import org.bgee.model.expressiondata.call.ExpressionCallProcessedFilter;
import org.bgee.model.expressiondata.call.ExpressionCallService;
import org.bgee.model.expressiondata.call.OTFExpressionCall;
import org.bgee.model.expressiondata.call.CallFilter.ExpressionCallFilter2;
import org.bgee.model.expressiondata.call.ExpressionCallProcessedFilter.ExpressionCallProcessedFilterConditionPart;
import org.bgee.model.gene.GeneFilter;
import org.bgee.model.job.JobService;
import org.bgee.view.ViewFactory;

public abstract class CommandExpressionSupport extends CommandParent{

    private final static Logger log = LogManager.getLogger(CommandExpressionSupport.class.getName());
    
        public static class ExprCallCondPartProcessingCacheKey {
                private final Set<ConditionFilter2> condFilters;
                public ExprCallCondPartProcessingCacheKey(Set<ConditionFilter2> condFilters) {
                        this.condFilters = condFilters;
                }
                public Set<ConditionFilter2> getCondFilters() {
                        return condFilters;
                }
                @Override
                public int hashCode() {
                        return Objects.hash(condFilters);
                }
                @Override
                public boolean equals(Object obj) {
                        if (this == obj)
                                return true;
                        if (obj == null)
                                return false;
                        if (getClass() != obj.getClass())
                                return false;
                        ExprCallCondPartProcessingCacheKey other = (ExprCallCondPartProcessingCacheKey) obj;
                        return Objects.equals(condFilters, other.condFilters);
                }
                @Override
                public String toString() {
                        StringBuilder builder = new StringBuilder();
                        builder.append("ExprCallCondPartProcessingCacheKey [condFilters=")
                                   .append(condFilters)
                                   .append("]");
                        return builder.toString();
                }
        }

        public static class ExprCallResultCacheKey {

                private final ExpressionCallFilter2 sourceFilter;
                private final Long offset;
                private final Integer limit;

                public ExprCallResultCacheKey(ExpressionCallFilter2 sourceFilter, Long offset, Integer limit) {
                        this.sourceFilter = sourceFilter;
                        this.offset = offset;
                        this.limit = limit;
                }

                public ExpressionCallFilter2 getSourceFilter() {
                        return sourceFilter;
                }
                public Long getOffset() {
                        return offset;
                }
                public Integer getLimit() {
                        return limit;
                }

                @Override
                public int hashCode() {
                        return Objects.hash(limit, offset, sourceFilter);
                }
                @Override
                public boolean equals(Object obj) {
                        if (this == obj)
                                return true;
                        if (obj == null)
                                return false;
                        if (getClass() != obj.getClass())
                                return false;
                        ExprCallResultCacheKey other = (ExprCallResultCacheKey) obj;
                        return Objects.equals(limit, other.limit) && Objects.equals(offset, other.offset)
                                        && Objects.equals(sourceFilter, other.sourceFilter);
                }

                @Override
                public String toString() {
                        StringBuilder builder = new StringBuilder();
                        builder.append("ExprCallResultCacheKey [")
                                   .append("offset=").append(offset)
                                   .append(", limit=").append(limit)
                                   .append(", sourceFilter=").append(sourceFilter)
                                   .append("]");
                        return builder.toString();
                }
        }

    private final static CacheDefinition<ExprCallCondPartProcessingCacheKey, ExpressionCallProcessedFilterConditionPart>
    EXPR_CALL_PROCESSED_COND_PART_CACHE_DEF = new CacheDefinition<>("exprCallProcessedCondPartCache",
            ExprCallCondPartProcessingCacheKey.class,
            ExpressionCallProcessedFilter.ExpressionCallProcessedFilterConditionPart.class,
            CacheType.LRU, 20);

        //Suppress warning for List generic type to have inference working with 'List.class'
        @SuppressWarnings("rawtypes")
        private final static CacheDefinition<ExprCallResultCacheKey, List>
        EXPR_CALL_RESULT_CACHE_DEF = new CacheDefinition<>("exprCallResultCache",
                        ExprCallResultCacheKey.class, List.class, CacheType.LRU, 20);

    /**
     * A {@code String} to recognize the action of requesting an experiment page
     * (there is no corresponding action in {@code RequestParameter}, it is triggered
     * when the URL parameter {@code exp_id} is provided).
     */

    protected final static long COMPUTE_TIME_PROCESSED_COND_PART_CACHE_MS = 1000L;
        private final static long COMPUTE_TIME_RESULT_CACHE_MS = 2000L;

    private final static String ID_PARAM_SUMMARY_VALUE = "SUMMARY";
    private final static Set<String> SUMMARY_ANAT_ENTITY_IDS = Set.of(
            "UBERON:0001062",
            "UBERON:0000010", "UBERON:0000211", "UBERON:0000309", "UBERON:0000468",
            "UBERON:0000949", "UBERON:0000990", "UBERON:0001004", "UBERON:0001007",
            "UBERON:0001008", "UBERON:0001009", "UBERON:0001015", "UBERON:0001017",
            "UBERON:0001032", "UBERON:0001434", "UBERON:0002193", "UBERON:0002330",
            "UBERON:0002384", "UBERON:0002405", "UBERON:0002416", "UBERON:0015204");
    private final static String SUMMARY_ANAT_ENTITY_ROOT_ID = "UBERON:0001062";
    private final static Set<String> SUMMARY_DISCARD_ANAT_ENTITY_AND_CHILDREN_IDS =
            Collections.unmodifiableSet(
                   SUMMARY_ANAT_ENTITY_IDS.stream().filter(id -> !id.equals(SUMMARY_ANAT_ENTITY_ROOT_ID))
                   .collect(Collectors.toSet()));
    private final static Set<String> SUMMARY_CELL_TYPE_IDS = Set.of(ConditionDAO.CELL_TYPE_ROOT_ID);

    public CommandExpressionSupport(HttpServletResponse response, RequestParameters requestParameters,
            BgeeProperties prop, ViewFactory viewFactory, ServiceFactory serviceFactory,
            JobService jobService, BgeeCacheService cacheService, User user) {
        super(response, requestParameters, prop, viewFactory, serviceFactory, jobService,
                cacheService, user, null, null);
    }

    public CommandExpressionSupport(HttpServletResponse response, RequestParameters requestParameters,
            BgeeProperties prop, ViewFactory viewFactory, ServiceFactory serviceFactory,
            BgeeCacheService cacheService) {
        super(response, requestParameters, prop, viewFactory, serviceFactory, null, cacheService,
                null, null, null);
    }

    protected ExpressionCallLoader loadExprCallLoader(boolean consideringFilters,
            Set<ConditionParameter<?, ?>> condParams, EnumSet<DataType> dataTypes)
                    throws InvalidRequestException {
        log.traceEntry("{}, {}, {}", consideringFilters, condParams, dataTypes);

        long startTimeFilter = System.currentTimeMillis();
        ExpressionCallFilter2 filter = this.loadExprCallFilter(consideringFilters, condParams, dataTypes);
        log.debug("ExpressionCallFilter2 built in {} ms", System.currentTimeMillis() - startTimeFilter);

        return log.traceExit(this.loadExprCallLoader(filter));
    }

    protected ExpressionCallLoader loadExprCallLoader(ExpressionCallFilter2 filter) {
        log.traceEntry("{}", filter);

        ExpressionCallService callService = this.serviceFactory.getExpressionCallService();
        //Try to get the processed condition part of the processed filter from cache
        long startTimeProcessFilter = System.currentTimeMillis();
        //TODO: remove includeChildTerms (and not excludeTermsAndChildrenIds) from the filter used
        // to retrieve the cache key of the processedfilters. With OTF propagations all conditions from the condition
        // graph have to be processed and the removal of children has to be done after the propagation.
        // Of course filterIds has to be kept as part of the caching key.
       ExpressionCallProcessedFilter processedFilter = this.cacheService.useCacheNonAtomic(
               EXPR_CALL_PROCESSED_COND_PART_CACHE_DEF,
               new ExprCallCondPartProcessingCacheKey(filter.getConditionFilters()),
               () -> callService.processExpressionCallFilter(filter),
               pf -> pf.getConditionPart(),
               condPart -> callService.processExpressionCallFilter(filter,
                       null, condPart, null),
               COMPUTE_TIME_PROCESSED_COND_PART_CACHE_MS);
        log.debug("processExpressionCallFilter (via cache) completed in {} ms",
                System.currentTimeMillis() - startTimeProcessFilter);

        long startTimeGetLoader = System.currentTimeMillis();
        ExpressionCallLoader loader = callService.getCallLoader(processedFilter);
        log.debug("getCallLoader() completed in {} ms", System.currentTimeMillis() - startTimeGetLoader);

        return log.traceExit(loader);
    }

        protected List<OTFExpressionCall> loadExprCallResults(ExpressionCallLoader callLoader,
                        int defaultLimit, int limitMax) throws InvalidRequestException {
                log.traceEntry("{}, {}, {}", callLoader, defaultLimit, limitMax);

                Integer limit = this.requestParameters.getLimit() == null? defaultLimit:
                        this.requestParameters.getLimit();
                if (limit > limitMax) {
                        throw log.throwing(new InvalidRequestException("It is not possible to request more than "
                                        + limitMax + " results."));
                }
                Long offset = this.requestParameters.getOffset() == null? 0:
                        this.requestParameters.getOffset();
                if (offset != null && offset < 0) {
                        throw log.throwing(new InvalidRequestException("Offset cannot be less than 0."));
                }
                ExprCallResultCacheKey cacheKey = new ExprCallResultCacheKey(
                                callLoader.getProcessedFilter().getSourceFilter(),
                                offset, limit);
                //Suppress warnings because we are responsible for the insertion and know the generic type
                @SuppressWarnings("unchecked")
                List<OTFExpressionCall> results = this.cacheService.useCacheNonAtomic(
                                EXPR_CALL_RESULT_CACHE_DEF,
                                cacheKey,
                                () -> callLoader.loadDataOnTheFly().values().stream()
                                                .flatMap(List::stream)
                                                .collect(Collectors.toList()),
                                COMPUTE_TIME_RESULT_CACHE_MS);
                return log.traceExit(results);
        }

    private ExpressionCallFilter2 loadExprCallFilter(boolean consideringFilters,
            Set<ConditionParameter<?, ?>> condParams, EnumSet<DataType> dataTypes)
                    throws InvalidRequestException {
        log.traceEntry("{}, {}, {}", consideringFilters, condParams, dataTypes);

        //Either there is no filtering at all, or some genes must be requested.
        //Checks are made in method #processExprCallPage()
        Integer speciesId = this.requestParameters.getSpeciesId();
        if (speciesId == null) {
            log.debug("No filter present, returning an empty ExpressionCallFilter2");
            return log.traceExit(new ExpressionCallFilter2());
        }
        GeneFilter geneFilter = new GeneFilter(speciesId, this.requestParameters.getGeneIds());
        if (geneFilter.getGeneIds().isEmpty()) {
            throw log.throwing(new InvalidRequestException("Some genes must be selected."));
        }

        //Currently there is only one filter for both anat. entities and cell types
        List<String> filterAnatEntityCellTypeIds = !consideringFilters? null:
            this.requestParameters.getValues(
                this.requestParameters.getUrlParametersInstance().getParamFilterAnatEntity());
        List<String> filterDevStageIds = !consideringFilters? null:
            this.requestParameters.getValues(
                this.requestParameters.getUrlParametersInstance().getParamFilterDevStage());
        List<String> filterSexIds = !consideringFilters? null:
            this.requestParameters.getValues(
                this.requestParameters.getUrlParametersInstance().getParamFilterSex());
        List<String> filterStrains = !consideringFilters? null:
            this.requestParameters.getValues(
                this.requestParameters.getUrlParametersInstance().getParamFilterStrain());

        List<String> sexes = this.requestParameters.getSex();
        if (sexes != null && (sexes.contains(RequestParameters.ALL_VALUE) ||
                sexes.containsAll(
                        EnumSet.allOf(SexEnum.class)
                        .stream()
                        .map(e -> e.name())
                        .collect(Collectors.toSet())))) {
            sexes = null;
        }

        Map<ConditionParameter<?, ?>, ComposedFilterIds<String>> condParamToComposedFilterIds =
                new HashMap<>();

        //--------------
        //Management of "magic" values:
        //If we receive the magic value "SUMMARY", we'll use a fix list of terms.
        List<String> anatEntityIds = this.requestParameters.getAnatEntity() == null? new ArrayList<>():
            new ArrayList<>(this.requestParameters.getAnatEntity());
        boolean summaryTermsRequested = false;
        if (anatEntityIds.contains(ID_PARAM_SUMMARY_VALUE)) {
            summaryTermsRequested = true;
            anatEntityIds.addAll(SUMMARY_ANAT_ENTITY_IDS);
            anatEntityIds.remove(ID_PARAM_SUMMARY_VALUE);
        }
        List<String> cellTypeIds = this.requestParameters.getCellType() == null? new ArrayList<>():
            new ArrayList<>(this.requestParameters.getCellType());
        if (cellTypeIds.contains(ID_PARAM_SUMMARY_VALUE)) {
            cellTypeIds.addAll(SUMMARY_CELL_TYPE_IDS);
            cellTypeIds.remove(ID_PARAM_SUMMARY_VALUE);
        }
        List<String> discardAnatEntityIds = this.requestParameters.getDiscardAnatEntity() == null?
                new ArrayList<>(): new ArrayList<>(this.requestParameters.getDiscardAnatEntity());
        if (discardAnatEntityIds.contains(ID_PARAM_SUMMARY_VALUE)) {
            discardAnatEntityIds.addAll(SUMMARY_DISCARD_ANAT_ENTITY_AND_CHILDREN_IDS);
            discardAnatEntityIds.remove(ID_PARAM_SUMMARY_VALUE);
            if (!summaryTermsRequested) {
                discardAnatEntityIds.removeAll(anatEntityIds);
            }
        }
        boolean requestedAnatEntityDescendant = Boolean.TRUE.equals(this.requestParameters.getFirstValue(
                this.requestParameters.getUrlParametersInstance().getParamAnatEntityDescendant()));
        if (!anatEntityIds.isEmpty() && !discardAnatEntityIds.isEmpty() && !requestedAnatEntityDescendant) {
            throw log.throwing(new InvalidRequestException("Only when anat. entity descendants are requested "
                    + "it is possible to exclude anat. entities and their children."));
        }
        //And we never include child terms when the parameter comes from a filter.
        boolean anatEntityDescendant =
                filterAnatEntityCellTypeIds != null && !filterAnatEntityCellTypeIds.isEmpty() ||
                anatEntityIds.isEmpty()? false:
                    Boolean.TRUE.equals(this.requestParameters.getFirstValue(
                            this.requestParameters.getUrlParametersInstance()
                            .getParamAnatEntityDescendant()));
        //--------------

        //ANAT ENTITY AND CELL TYPE
        FilterIds<String> anatEntityFilter = new FilterIds<>(
                //Filters override the related parameter from the form
                filterAnatEntityCellTypeIds != null && !filterAnatEntityCellTypeIds.isEmpty()?
                        filterAnatEntityCellTypeIds: anatEntityIds,
                anatEntityDescendant,
                filterAnatEntityCellTypeIds != null && !filterAnatEntityCellTypeIds.isEmpty()?
                        null: discardAnatEntityIds,
                null);
        FilterIds<String> cellTypeFilter = new FilterIds<>(
                //Filters override the related parameter from the form
                filterAnatEntityCellTypeIds != null && !filterAnatEntityCellTypeIds.isEmpty()?
                        filterAnatEntityCellTypeIds: cellTypeIds,
                //And we never include child terms when the parameter comes from a filter.
                filterAnatEntityCellTypeIds != null && !filterAnatEntityCellTypeIds.isEmpty() ||
                        cellTypeIds.isEmpty()?
                        false: Boolean.TRUE.equals(this.requestParameters.getFirstValue(
                                this.requestParameters.getUrlParametersInstance()
                                .getParamCellTypeDescendant())));


        List<FilterIds<String>> composedFilterIds = new ArrayList<>(List.of(anatEntityFilter));
        //In case we used the filters, anatEntityFilter and cellTypeFilter should be equal,
        //and we thus don't use the cellTypeFilter
        if (!anatEntityFilter.equals(cellTypeFilter)) {
            composedFilterIds.add(cellTypeFilter);
        }
        ComposedFilterIds<String> anatComposedFilter = new ComposedFilterIds<>(
                composedFilterIds.stream()
                .filter(f -> !f.isEmpty())
                .collect(Collectors.toList()));
        condParamToComposedFilterIds.put(ConditionParameter.ANAT_ENTITY_CELL_TYPE, anatComposedFilter);

        //DEV. STAGE
        FilterIds<String> devStageFilter = new FilterIds<>(
                //Filters override the related parameter from the form
                filterDevStageIds != null && !filterDevStageIds.isEmpty()?
                        filterDevStageIds: this.requestParameters.getDevStage(),
                //And we never include child terms when the parameter comes from a filter.
                filterDevStageIds != null && !filterDevStageIds.isEmpty() ||
                this.requestParameters.getDevStage() == null ||
                this.requestParameters.getDevStage().isEmpty()?
                        false: Boolean.TRUE.equals(this.requestParameters.getFirstValue(
                                this.requestParameters.getUrlParametersInstance()
                                .getParamStageDescendant())));
        condParamToComposedFilterIds.put(ConditionParameter.DEV_STAGE,
                new ComposedFilterIds<>(devStageFilter));

        //SEX
        FilterIds<String> sexFilter = new FilterIds<>(
                //Filters override the related parameter from the form
                filterSexIds != null && !filterSexIds.isEmpty()?
                        filterSexIds: sexes,
                //sex descendant always false: requesting descendants of the root is equivalent
                //to request all sexes, in which case we don't provide requested sex IDs
                false);
        condParamToComposedFilterIds.put(ConditionParameter.SEX,
                new ComposedFilterIds<>(sexFilter));

        //STRAIN
        FilterIds<String> strainFilter = new FilterIds<>(
                //Filters override the related parameter from the form
                filterStrains != null && !filterStrains.isEmpty()?
                        filterStrains: this.requestParameters.getStrain(),
                //strain descendant always false: requesting descendants of the root is equivalent
                //to request all strains, in which case we don't provide requested strains
                false);
        condParamToComposedFilterIds.put(ConditionParameter.STRAIN,
                new ComposedFilterIds<>(strainFilter));

        ConditionFilter2 condFilter = null;
        try {
            condFilter = new ConditionFilter2(speciesId,
                    condParamToComposedFilterIds,
                    condParams,
                    null,
                    this.requestParameters.isExcludeNonInformative());
            if (condFilter.areAllFiltersExceptSpeciesEmpty()) {
                //To request a species a GeneFilter is mandatory,
                //so if there are no other filters, we can discard this ConditionFilter
                condFilter = null;
            }
        } catch (IllegalArgumentException e) {
            log.catching(e);
            throw log.throwing(new InvalidRequestException(e.getMessage()));
        }

        //ExpressionSummary and SummaryQuality
        SummaryQuality tmpQual = SummaryQuality.values()[0];
        if (this.requestParameters.getDataQuality() != null &&
                !this.requestParameters.getDataQuality().isBlank()) {
            try {
                tmpQual = BgeeEnum.convert(SummaryQuality.class, this.requestParameters.getDataQuality());
            } catch (IllegalArgumentException e) {
                log.catching(Level.DEBUG, e);
                throw log.throwing(new InvalidRequestException(
                        "Unrecognized data quality: " + this.requestParameters.getDataQuality()));
            }
        }
        SummaryQuality qual = tmpQual;
        Map<ExpressionSummary, SummaryQuality> summaryCallTypeQualityFilter = new HashMap<>();
        if (this.requestParameters.getExprType() == null || this.requestParameters.getExprType().isEmpty() ||
                this.requestParameters.getExprType().contains(RequestParameters.ALL_VALUE)) {
            summaryCallTypeQualityFilter = EnumSet.allOf(ExpressionSummary.class).stream()
                    .collect(Collectors.toMap(es -> es, es -> qual));
        } else {
            try {
            summaryCallTypeQualityFilter = this.requestParameters.getExprType().stream()
                    .collect(Collectors.toMap(
                            s -> BgeeEnum.convert(ExpressionSummary.class, s),
                            s -> qual));
            } catch (IllegalArgumentException e) {
                log.catching(Level.DEBUG, e);
                throw log.throwing(new InvalidRequestException(
                        "Unrecognized call types: " + this.requestParameters.getExprType()));
            }
        }
        try {
            return log.traceExit(new ExpressionCallFilter2(
                    summaryCallTypeQualityFilter,
                    geneFilter,
                    condFilter != null? Set.of(condFilter): null,
                    dataTypes,
                    condParams,
                    this.requestParameters.getObservedData() == null? null: condParams,
                    this.requestParameters.getObservedData()));
        } catch (IllegalArgumentException e) {
            log.catching(Level.ERROR, e);
            throw log.throwing(new InvalidRequestException("Incorrect parameters"));
        }
    }

}
