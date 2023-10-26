package org.bgee.controller;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.exception.InvalidRequestException;
import org.bgee.controller.exception.PageNotFoundException;
import org.bgee.controller.user.User;
import org.bgee.controller.utils.BgeeCacheService;
import org.bgee.controller.utils.BgeeCacheService.CacheDefinition;
import org.bgee.controller.utils.BgeeCacheService.CacheType;
import org.bgee.model.BgeeEnum;
import org.bgee.model.ServiceFactory;
import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.anatdev.DevStage;
import org.bgee.model.anatdev.Sex;
import org.bgee.model.anatdev.Sex.SexEnum;
import org.bgee.model.expressiondata.BaseConditionFilter2.ComposedFilterIds;
import org.bgee.model.expressiondata.BaseConditionFilter2.FilterIds;
import org.bgee.model.expressiondata.baseelements.ConditionParameter;
import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.model.expressiondata.baseelements.SummaryCallType.ExpressionSummary;
import org.bgee.model.expressiondata.baseelements.SummaryQuality;
import org.bgee.model.expressiondata.call.Call.ExpressionCall2;
import org.bgee.model.expressiondata.call.CallFilter.ExpressionCallFilter2;
import org.bgee.model.expressiondata.call.ConditionFilter2;
import org.bgee.model.expressiondata.call.ExpressionCallLoader;
import org.bgee.model.expressiondata.call.ExpressionCallPostFilter;
import org.bgee.model.expressiondata.call.ExpressionCallProcessedFilter;
import org.bgee.model.expressiondata.call.ExpressionCallService;
import org.bgee.model.expressiondata.rawdata.baseelements.Assay;
import org.bgee.model.expressiondata.rawdata.baseelements.Experiment;
import org.bgee.model.expressiondata.rawdata.baseelements.ExperimentAssay;
import org.bgee.model.expressiondata.rawdata.baseelements.RawDataContainer;
import org.bgee.model.expressiondata.rawdata.baseelements.RawDataContainerWithExperiment;
import org.bgee.model.expressiondata.rawdata.baseelements.RawDataCountContainer;
import org.bgee.model.expressiondata.rawdata.baseelements.RawDataDataType;
import org.bgee.model.expressiondata.rawdata.RawDataConditionFilter;
import org.bgee.model.expressiondata.rawdata.RawDataFilter;
import org.bgee.model.expressiondata.rawdata.RawDataLoader;
import org.bgee.model.expressiondata.rawdata.RawDataLoader.InformationType;
import org.bgee.model.expressiondata.rawdata.RawDataPostFilter;
import org.bgee.model.expressiondata.rawdata.RawDataProcessedFilter;
import org.bgee.model.expressiondata.rawdata.RawDataService;
import org.bgee.model.gene.Gene;
import org.bgee.model.gene.GeneFilter;
import org.bgee.model.job.Job;
import org.bgee.model.job.JobService;
import org.bgee.model.job.exception.ThreadAlreadyWorkingException;
import org.bgee.model.job.exception.TooManyJobsException;
import org.bgee.model.ontology.Ontology;
import org.bgee.model.search.SearchMatchResultService;
import org.bgee.model.species.Species;
import org.bgee.model.species.SpeciesService;
import org.bgee.view.DataDisplay;
import org.bgee.view.ViewFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

/**
 * Controller that handles requests for the raw data page.
 *
 * @author  Frederic Bastian
 * @version Bgee 15.0, Jan. 2023
 * @since   Bgee 15.0, Oct. 2022
 */
public class CommandData extends CommandParent {
    private final static Logger log = LogManager.getLogger(CommandData.class.getName());

    public static class ExpressionCallResponse {

        private final List<ExpressionCall2> calls;
        private final LinkedHashSet<ConditionParameter<?, ?>> condParams;
        private final EnumSet<DataType> requestedDataTypes;

        public ExpressionCallResponse(List<ExpressionCall2> calls,
                LinkedHashSet<ConditionParameter<?, ?>> condParams,
                EnumSet<DataType> requestedDataTypes) {
            this.calls = calls;
            this.condParams = condParams;
            this.requestedDataTypes = requestedDataTypes;
        }

        public List<ExpressionCall2> getCalls() {
            return calls;
        }
        public LinkedHashSet<ConditionParameter<?, ?>> getCondParams() {
            return condParams;
        }
        public EnumSet<DataType> getRequestedDataTypes() {
            return requestedDataTypes;
        }
    }
    public static class ColumnDescription {
        public static enum ColumnType {
            STRING, NUMERIC, INTERNAL_LINK, EXTERNAL_LINK, ANAT_ENTITY, DEV_STAGE,
            DATA_TYPE_SOURCE, LINK_TO_RAW_DATA_ANNOTS, LINK_TO_PROC_EXPR_VALUES,
            LINK_CALL_TO_PROC_EXPR_VALUES
        }
        /**
         * To describe how to link from experiment results to assay results,
         * and from assay results to processed expression values results.
         * The link system automatically prefill the experiment and assay filters
         * to target the appropriate results.
         *
         * @author Frederic Bastian
         * @version Bgee 15.0 Jan. 2023
         * @since Bgee 15.0 Jan. 2023
         */
        public static class FilterTarget {
            /**
             * A {@code String} that is the name of the JSON attribute
             * providing the value to fill the filter with.
             */
            private final String valueAttributeName;
            /**
             * A {@code String} that is the name of the filter to be filled.
             */
            private final String urlParameterName;

            public FilterTarget(String valueAttributeName, String urlParameterName) {
                this.valueAttributeName = valueAttributeName;
                this.urlParameterName = urlParameterName;
            }

            public String getValueAttributeName() {
                return valueAttributeName;
            }
            public String getUrlParameterName() {
                return urlParameterName;
            }

            @Override
            public int hashCode() {
                return Objects.hash(urlParameterName, valueAttributeName);
            }
            @Override
            public boolean equals(Object obj) {
                if (this == obj)
                    return true;
                if (obj == null)
                    return false;
                if (getClass() != obj.getClass())
                    return false;
                FilterTarget other = (FilterTarget) obj;
                return Objects.equals(urlParameterName, other.urlParameterName)
                        && Objects.equals(valueAttributeName, other.valueAttributeName);
            }

            @Override
            public String toString() {
                StringBuilder builder = new StringBuilder();
                builder.append("FilterTarget [")
                       .append("valueAttributeName=").append(valueAttributeName)
                       .append(", urlParameterName=").append(urlParameterName)
                       .append("]");
                return builder.toString();
            }
        }


        public static final String INTERNAL_LINK_TARGET_EXP = "experiment";
        public static final String INTERNAL_LINK_TARGET_GENE = "gene";
        private static final Set<String> INTERNAL_LINK_TARGETS = Set.of(
                INTERNAL_LINK_TARGET_EXP, INTERNAL_LINK_TARGET_GENE);

        private final String title;
        private final String infoBubble;
        private final List<String> attributes;
        private final ColumnType columnType;
        /**
         * Only applicable when {@code columnType} is {@code INTERNAL_LINK}.
         */
        private final String linkTarget;
        /**
         * Only applicable when {@code columnType} is {@code LINK_TO_RAW_DATA_ANNOTS}
         * or {@code LINK_TO_PROC_EXPR_VALUES}.
         */
        private final List<FilterTarget> filterTargets;
        /**
         * A {@code boolean} defining whether the column should be exported in TSV files.
         */
        private final boolean export;
        /**
         * Only applicable when {@code columnType} is {@code INTERNAL_LINK}
         * and {@code linkTarget} is {@code INTERNAL_LINK_TARGET_GENE}.
         */
        private final String geneMappedToSameGeneIdCountResultAttribute;
        /**
         * Only applicable when {@code columnType} is {@code INTERNAL_LINK}
         * and {@code linkTarget} is {@code INTERNAL_LINK_TARGET_GENE}.
         */
        private final String geneSpeciesIdResultAttribute;


        public ColumnDescription(String title, String infoBubble, List<String> attributes,
                ColumnType columnType, String linkTarget, Collection<FilterTarget> filterTargets,
                boolean export, String geneMappedToSameGeneIdCountResultAttribute,
                String geneSpeciesIdResultAttribute) {
            if (columnType == null) {
                throw log.throwing(new IllegalArgumentException(
                        "a column type is mandatory"));
            }
            if (!ColumnType.INTERNAL_LINK.equals(columnType) && StringUtils.isNotBlank(linkTarget)) {
                throw log.throwing(new IllegalArgumentException(
                        "linkTarget only applicable when columnType is INTERNAL_LINK"));
            } else if (ColumnType.INTERNAL_LINK.equals(columnType) && StringUtils.isBlank(linkTarget)) {
                throw log.throwing(new IllegalArgumentException(
                        "linkTarget must be defined when columnType is INTERNAL_LINK"));
            }
            if (StringUtils.isNotBlank(linkTarget) && !INTERNAL_LINK_TARGETS.contains(linkTarget)) {
                throw log.throwing(new IllegalArgumentException(
                        "Invalid value for linkTarget: " + linkTarget));
            }
            if (ColumnType.INTERNAL_LINK.equals(columnType) &&
                    INTERNAL_LINK_TARGET_GENE.equals(linkTarget) &&
                    (StringUtils.isBlank(geneMappedToSameGeneIdCountResultAttribute) ||
                            StringUtils.isBlank(geneSpeciesIdResultAttribute))) {
                throw log.throwing(new IllegalArgumentException(
                        "Result attributes missing for gene internal link"));
            } else if ((!ColumnType.INTERNAL_LINK.equals(columnType) ||
                    !INTERNAL_LINK_TARGET_GENE.equals(linkTarget)) &&
                    (StringUtils.isNotBlank(geneMappedToSameGeneIdCountResultAttribute) ||
                            StringUtils.isNotBlank(geneSpeciesIdResultAttribute))) {
                throw log.throwing(new IllegalArgumentException(
                        "Gene result attributes to be provided only for gene internal links"));
            }
            if (!ColumnType.LINK_TO_RAW_DATA_ANNOTS.equals(columnType) &&
                    !ColumnType.LINK_TO_PROC_EXPR_VALUES.equals(columnType) &&
                    !ColumnType.LINK_CALL_TO_PROC_EXPR_VALUES.equals(columnType) &&
                    filterTargets != null && !filterTargets.isEmpty()) {
                throw log.throwing(new IllegalArgumentException(
                        "filterTarget not applicable"));
            } else if ((ColumnType.LINK_TO_RAW_DATA_ANNOTS.equals(columnType) ||
                    ColumnType.LINK_TO_PROC_EXPR_VALUES.equals(columnType) ||
                    ColumnType.LINK_CALL_TO_PROC_EXPR_VALUES.equals(columnType)) &&
                    (filterTargets == null || filterTargets.isEmpty())) {
                throw log.throwing(new IllegalArgumentException(
                        "filterTarget must be defined"));
            }
            if (StringUtils.isBlank(title)) {
                throw log.throwing(new IllegalArgumentException(
                        "title of the column is mandatory"));
            }
            if ((attributes == null || attributes.isEmpty()) &&
                    !ColumnType.LINK_TO_RAW_DATA_ANNOTS.equals(columnType) &&
                    !ColumnType.LINK_TO_PROC_EXPR_VALUES.equals(columnType) &&
                    !ColumnType.LINK_CALL_TO_PROC_EXPR_VALUES.equals(columnType)) {
                throw log.throwing(new IllegalArgumentException(
                        "a list of attributes is mandatory"));
            }
            this.title = title;
            this.infoBubble = infoBubble;
            this.attributes = attributes;
            this.columnType = columnType;
            this.linkTarget = linkTarget;
            this.filterTargets = filterTargets == null? null: List.copyOf(filterTargets);
            this.export = export;
            this.geneMappedToSameGeneIdCountResultAttribute = geneMappedToSameGeneIdCountResultAttribute;
            this.geneSpeciesIdResultAttribute = geneSpeciesIdResultAttribute;
        }

        public String getTitle() {
            return title;
        }
        public String getInfoBubble() {
            return infoBubble;
        }
        public List<String> getAttributes() {
            return attributes;
        }
        public ColumnType getColumnType() {
            return columnType;
        }
        public String getLinkTarget() {
            return linkTarget;
        }
        public List<FilterTarget> getFilterTargets() {
            return filterTargets;
        }
        public boolean isExport() {
            return export;
        }
        public String getGeneMappedToSameGeneIdCountResultAttribute() {
            return geneMappedToSameGeneIdCountResultAttribute;
        }
        public String getGeneSpeciesIdResultAttribute() {
            return geneSpeciesIdResultAttribute;
        }

        @Override
        public int hashCode() {
            return Objects.hash(attributes, columnType, export, filterTargets,
                    geneMappedToSameGeneIdCountResultAttribute, geneSpeciesIdResultAttribute,
                    infoBubble, linkTarget, title);
        }
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            ColumnDescription other = (ColumnDescription) obj;
            return Objects.equals(attributes, other.attributes)
                    && columnType == other.columnType
                    && export == other.export
                    && Objects.equals(filterTargets, other.filterTargets)
                    && Objects.equals(geneMappedToSameGeneIdCountResultAttribute,
                            other.geneMappedToSameGeneIdCountResultAttribute)
                    && Objects.equals(geneSpeciesIdResultAttribute, other.geneSpeciesIdResultAttribute)
                    && Objects.equals(infoBubble, other.infoBubble)
                    && Objects.equals(linkTarget, other.linkTarget)
                    && Objects.equals(title, other.title);
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("ColumnDescription [")
                   .append("title=").append(title)
                   .append(", infoBubble=").append(infoBubble)
                   .append(", attributes=").append(attributes)
                   .append(", columnType=").append(columnType)
                   .append(", linkTarget=").append(linkTarget)
                   .append(", filterTargets=").append(filterTargets)
                   .append(", export=").append(export)
                   .append(", geneMappedToSameGeneIdCountResultAttribute=")
                       .append(geneMappedToSameGeneIdCountResultAttribute)
                   .append(", geneSpeciesIdResultAttribute=").append(geneSpeciesIdResultAttribute)
                   .append("]");
            return builder.toString();
        }
    }

    public static class DataFormDetails {
        private final Species requestedSpecies;
        private final Ontology<DevStage, String> requestedSpeciesDevStageOntology;
        private final List<Sex> requestedSpeciesSexes;
        private final List<AnatEntity> requestedAnatEntitesAndCellTypes;
        private final List<Gene> requestedGenes;
        private final List<ExperimentAssay> requestedExperimentAndAssays;

        public DataFormDetails(Species requestedSpecies,
                Ontology<DevStage, String> requestedSpeciesDevStageOntology,
                List<Sex> requestedSpeciesSexes, List<AnatEntity> requestedAnatEntitesAndCellTypes,
                List<Gene> requestedGenes, List<ExperimentAssay> requestedExperimentAndAssays) {

            this.requestedSpecies = requestedSpecies;
            this.requestedSpeciesDevStageOntology = requestedSpeciesDevStageOntology;
            this.requestedSpeciesSexes = Collections.unmodifiableList(requestedSpeciesSexes == null?
                    new ArrayList<>(): new ArrayList<>(requestedSpeciesSexes));
            this.requestedAnatEntitesAndCellTypes = Collections.unmodifiableList(
                    requestedAnatEntitesAndCellTypes == null?
                    new ArrayList<>(): new ArrayList<>(requestedAnatEntitesAndCellTypes));
            this.requestedGenes = Collections.unmodifiableList(requestedGenes == null?
                    new ArrayList<>(): new ArrayList<>(requestedGenes));
            this.requestedExperimentAndAssays = Collections.unmodifiableList(
                    requestedExperimentAndAssays == null?
                    new ArrayList<>(): new ArrayList<>(requestedExperimentAndAssays));
        }

        public Species getRequestedSpecies() {
            return requestedSpecies;
        }
        public Ontology<DevStage, String> getRequestedSpeciesDevStageOntology() {
            return requestedSpeciesDevStageOntology;
        }
        public List<Sex> getRequestedSpeciesSexes() {
            return requestedSpeciesSexes;
        }
        public List<AnatEntity> getRequestedAnatEntitesAndCellTypes() {
            return requestedAnatEntitesAndCellTypes;
        }
        public List<Gene> getRequestedGenes() {
            return requestedGenes;
        }
        public List<ExperimentAssay> getRequestedExperimentAndAssays() {
            return requestedExperimentAndAssays;
        }

        public boolean containsAnyInformation() {
            return this.getRequestedSpeciesDevStageOntology() != null ||
                    !this.getRequestedSpeciesSexes().isEmpty() ||
                    !this.getRequestedAnatEntitesAndCellTypes().isEmpty() ||
                    !this.getRequestedGenes().isEmpty() ||
                    !this.getRequestedExperimentAndAssays().isEmpty();
        }
    }

    public static class RawDataCacheKey {
        private final RawDataFilter sourceFilter;
        private final DataType dataType;
        private final EnumSet<InformationType> informationTypes;

        public RawDataCacheKey(RawDataFilter processedFilter, DataType dataType,
                EnumSet<InformationType> informationTypes) {
            this.sourceFilter = processedFilter;
            this.dataType = dataType;
            this.informationTypes = informationTypes;
        }

        public RawDataFilter getSourceFilter() {
            return sourceFilter;
        }
        public DataType getDataType() {
            return dataType;
        }
        public EnumSet<InformationType> getInformationTypes() {
            return informationTypes;
        }

        @Override
        public int hashCode() {
            return Objects.hash(dataType, informationTypes, sourceFilter);
        }
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            RawDataCacheKey other = (RawDataCacheKey) obj;
            return dataType == other.dataType
                    && Objects.equals(informationTypes, other.informationTypes)
                    && Objects.equals(sourceFilter, other.sourceFilter);
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("RawDataCacheKey [")
                   .append("dataType=").append(dataType)
                   .append(", informationTypes=").append(informationTypes)
                   .append(", sourceFilter=").append(sourceFilter)
                   .append("]");
            return builder.toString();
        }
    }
    public static class RawDataResultCacheKey extends RawDataCacheKey {

        private final Long offset;
        private final Integer limit;

        public RawDataResultCacheKey(RawDataFilter processedFilter, DataType dataType,
                EnumSet<InformationType> informationTypes, Long offset, Integer limit) {
            super(processedFilter, dataType, informationTypes);
            this.offset = offset;
            this.limit = limit;
        }

        public Long getOffset() {
            return offset;
        }
        public Integer getLimit() {
            return limit;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = super.hashCode();
            result = prime * result + Objects.hash(limit, offset);
            return result;
        }
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (!super.equals(obj))
                return false;
            if (getClass() != obj.getClass())
                return false;
            RawDataResultCacheKey other = (RawDataResultCacheKey) obj;
            return Objects.equals(limit, other.limit) && Objects.equals(offset, other.offset);
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("RawDataResultCacheKey [")
                   .append("dataType=").append(getDataType())
                   .append(", informationTypes=").append(getInformationTypes())
                   .append(", offset=").append(offset)
                   .append(", limit=").append(limit)
                   .append(", sourceFilter=").append(getSourceFilter())
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

    /**
     * An {@code int} that is the maximum allowed number of results
     * to retrieve in one request, for each requested data type independently.
     * Value: 10,000.
     */
    private final static int LIMIT_MAX = 10000;
    /**
     * An {@code int} that is the default number of results
     * to retrieve in one request, for each requested data type independently.
     * Value: 100.
     */
    private final static int DEFAULT_LIMIT = 100;
    /**
     * A {@code long} that is the execution time in milliseconds of a count query
     * that triggers storing the result in cache. Defined as {@code long}
     * for convenience when comparing to start and end times provided as {@code long}.
     *
     * @see #loadRawDataCounts(RawDataLoader, EnumSet)
     * @see #loadExprCallCounts(ExpressionCallLoader)
     */
    private final static long COMPUTE_TIME_COUNT_CACHE_MS = 1000L;

    private final static CacheDefinition<RawDataCacheKey, RawDataCountContainer>
    RAW_DATA_COUNT_CACHE_DEF = new CacheDefinition<>("rawDataCountCache",
            RawDataCacheKey.class, RawDataCountContainer.class, CacheType.LRU, 300);

    private final static CacheDefinition<ExpressionCallFilter2, Long>
    EXPR_CALL_COUNT_CACHE_DEF = new CacheDefinition<>("exprCallCountCache",
            ExpressionCallFilter2.class, Long.class, CacheType.LRU, 60);

    /**
     * A {@code long} that is the execution time in milliseconds of the processing of a filter
     * that triggers storing the result in cache. Defined as {@code long}
     * for convenience when comparing to start and end times provided as {@code long}.
     *
     * @see #loadRawDataLoader(RawDataFilter)
     * @see #loadExprCallLoader(ExpressionCallFilter2)
     */
    private final static long COMPUTE_TIME_PROCESSED_FILTER_CACHE_MS = 1000L;

    private final static CacheDefinition<RawDataFilter, RawDataProcessedFilter>
    RAW_DATA_PROCESSED_FILTER_CACHE_DEF = new CacheDefinition<>("rawDataProcessedFilterCache",
            RawDataFilter.class, RawDataProcessedFilter.class, CacheType.LRU, 20);

    private final static CacheDefinition<ExpressionCallFilter2, ExpressionCallProcessedFilter>
    EXPR_CALL_PROCESSED_FILTER_CACHE_DEF = new CacheDefinition<>("exprCallProcessedFilterCache",
            ExpressionCallFilter2.class, ExpressionCallProcessedFilter.class, CacheType.LRU, 20);

    /**
     * A {@code long} that is the execution time in milliseconds of the processing of results
     * that triggers storing the result in cache. Defined as {@code long}
     * for convenience when comparing to start and end times provided as {@code long}.
     *
     * @see #loadRawDataResults(RawDataLoader, EnumSet, InformationType)
     * @see #loadExprCallResults(ExpressionCallLoader)
     */
    private final static long COMPUTE_TIME_RESULT_CACHE_MS = 2000L;

    //Suppress warning for RawDataContainer generic type to have inference
    //working with 'RawDataContainer.class'
    @SuppressWarnings("rawtypes")
    private final static CacheDefinition<RawDataResultCacheKey, RawDataContainer>
    RAW_DATA_RESULT_CACHE_DEF = new CacheDefinition<>("rawDataResultCache",
            RawDataResultCacheKey.class, RawDataContainer.class, CacheType.LRU, 100);

    //Suppress warning for List generic type to have inference
    //working with 'List.class'
    @SuppressWarnings("rawtypes")
    private final static CacheDefinition<ExprCallResultCacheKey, List>
    EXPR_CALL_RESULT_CACHE_DEF = new CacheDefinition<>("exprCallResultCache",
            ExprCallResultCacheKey.class, List.class, CacheType.LRU, 20);

    /**
     * A {@code long} that is the execution time in milliseconds of the generation of post-filters
     * that triggers storing the result in cache. Defined as {@code long}
     * for convenience when comparing to start and end times provided as {@code long}.
     *
     * @see #loadRawDataPostFilters(RawDataLoader, EnumSet, InformationType)
     * @see #loadExprCallPostFilters(ExpressionCallLoader)
     */
    private final static long COMPUTE_TIME_POST_FILTER_CACHE_MS = 1000L;

    private final static CacheDefinition<RawDataCacheKey, RawDataPostFilter>
    RAW_DATA_POST_FILTER_CACHE_DEF = new CacheDefinition<>("rawDataPostFilterCache",
            RawDataCacheKey.class, RawDataPostFilter.class, CacheType.LRU, 100);

    private final static CacheDefinition<ExpressionCallFilter2, ExpressionCallPostFilter>
    EXPR_CALL_POST_FILTER_CACHE_DEF = new CacheDefinition<>("exprCallPostFilterCache",
            ExpressionCallFilter2.class, ExpressionCallPostFilter.class, CacheType.LRU, 20);

    /**
     * A {@code String} to recognize the action of requesting an experiment page
     * (there is no corresponding action in {@code RequestParameter}, it is triggered
     * when the URL parameter {@code exp_id} is provided).
     */
    private final static String EXPERIMENT_PAGE_ACTION = "experiment";

    //Static initializer
    {
        if (LIMIT_MAX > RawDataLoader.LIMIT_MAX) {
            throw log.throwing(new IllegalStateException("The maximum limit allowed by this controller "
                    + "is greater than the maximum limit allowed by the RawDataLoader."));
        }
        if (LIMIT_MAX > ExpressionCallLoader.LIMIT_MAX) {
            throw log.throwing(new IllegalStateException("The maximum limit allowed by this controller "
                    + "is greater than the maximum limit allowed by the ExpressionCallLoader."));
        }
        assert DEFAULT_LIMIT <= LIMIT_MAX;
    }


 // ***************************************************
 // INSTANCE ATTRIBUTES AND METHODS
 // ***************************************************

    private final SpeciesService speciesService;

    public CommandData(HttpServletResponse response, RequestParameters requestParameters,
                          BgeeProperties prop, ViewFactory viewFactory, ServiceFactory serviceFactory,
                          JobService jobService, BgeeCacheService cacheService, User user) {
        super(response, requestParameters, prop, viewFactory, serviceFactory, jobService,
                cacheService, user, null, null);
        this.speciesService = this.serviceFactory.getSpeciesService();
    }

    @Override
    public void processRequest() throws Exception {
        log.traceEntry();

        //Species list
        List<Species> speciesList = null;
        if (this.requestParameters.isGetSpeciesList()) {
            speciesList = this.loadSpeciesList();
        }

        //Form details
        DataFormDetails formDetails = this.loadFormDetails();

        //Actions: experiment list, raw data results, processed expression values
        if (RequestParameters.ACTION_EXPERIMENTS.equals(this.requestParameters.getAction()) ||
                RequestParameters.ACTION_RAW_DATA_ANNOTS.equals(this.requestParameters.getAction()) ||
                RequestParameters.ACTION_PROC_EXPR_VALUES.equals(this.requestParameters.getAction())) {

            this.processRawDataPage(speciesList, formDetails);

        } else if (RequestParameters.ACTION_EXPR_CALLS.equals(this.requestParameters.getAction())) {

            this.processExprCallPage(speciesList, formDetails);

        } else if (this.requestParameters.getExperimentId() != null) {

            this.processExperimentPage();

        } else if (speciesList != null || formDetails != null) {
            DataDisplay display = viewFactory.getDataDisplay();
            display.displayDataPage(speciesList, formDetails);
        } else {
            throw log.throwing(new InvalidRequestException(
                    "The request does not have any mandatory parameter"));
        }

        log.traceExit();
    }

    private void processRawDataPage(List<Species> speciesList, DataFormDetails formDetails)
            throws InvalidRequestException, ThreadAlreadyWorkingException,
            TooManyJobsException, IOException {
        log.traceEntry("{}, {}", speciesList, formDetails);

        log.debug("Action identified: {}", this.requestParameters.getAction());
        EnumMap<DataType, RawDataContainer<?, ?>> rawDataContainers = null;
        EnumMap<DataType, RawDataCountContainer> rawDataCountContainers = null;
        EnumMap<DataType, RawDataPostFilter> rawDataPostFilters = null;
        EnumMap<DataType, List<ColumnDescription>> colDescriptions = null;

        EnumSet<DataType> dataTypes = this.checkAndGetDataTypes();

        //Queries that required a RawDataLoader
        if (this.requestParameters.isGetResults() || this.requestParameters.isGetResultCount() ||
                this.requestParameters.isGetFilters()) {
            log.debug("Loading RawDataLoader");
            //try...finally block to manage number of jobs per users,
            //to limit the concurrent number of queries a user can make
            Job job = null;
            try {
                job = this.jobService.registerNewJob(this.user.getUUID().toString());
                job.startJob();
                //If filters are provided, they will be considered with this RawDataLoader
                RawDataLoader rawDataLoader = this.loadRawDataLoader(true);

                InformationType infoType = null;
                switch (this.requestParameters.getAction()) {
                case RequestParameters.ACTION_EXPERIMENTS:
                    infoType = InformationType.EXPERIMENT;
                    break;
                case RequestParameters.ACTION_RAW_DATA_ANNOTS:
                    infoType = InformationType.ASSAY;
                    break;
                case RequestParameters.ACTION_PROC_EXPR_VALUES:
                    infoType = InformationType.CALL;
                    break;
                default:
                    throw log.throwing(new UnsupportedOperationException("Unsupported action: "
                            + this.requestParameters.getAction()));
                }

                //Raw data results
                if (this.requestParameters.isGetResults()) {
                    rawDataContainers = this.loadRawDataResults(rawDataLoader, dataTypes, infoType);
                }
                //Raw data counts
                if (this.requestParameters.isGetResultCount()) {
                    EnumSet<InformationType> infoTypes = EnumSet.of(InformationType.EXPERIMENT);
                    if (RequestParameters.ACTION_RAW_DATA_ANNOTS.equals(this.requestParameters.getAction()) ||
                            RequestParameters.ACTION_PROC_EXPR_VALUES.equals(this.requestParameters.getAction())) {
                        infoTypes.add(InformationType.ASSAY);
                    }
                    if (RequestParameters.ACTION_PROC_EXPR_VALUES.equals(this.requestParameters.getAction())) {
                        infoTypes.add(InformationType.CALL);
                    }
                    rawDataCountContainers = this.loadRawDataCounts(rawDataLoader, dataTypes, infoTypes);
                }
                //Filters
                if (this.requestParameters.isGetFilters()) {
                    //For requesting getFilters, well, the filter parameters must be ignored
                    RawDataLoader loaderToUse = rawDataLoader;
                    RawDataFilter noFilterParamFilter = this.loadRawDataFilter(false);
                    //We try to avoid requesting a ProcessedFilter if not necessary,
                    //by comparing the RawDataFilters
                    if (!rawDataLoader.getRawDataProcessedFilter()
                            .getSourceFilter().equals(noFilterParamFilter)) {
                        loaderToUse = this.loadRawDataLoader(noFilterParamFilter);
                    }
                    rawDataPostFilters = this.loadRawDataPostFilters(loaderToUse, dataTypes, infoType);
                }

                job.completeWithSuccess();
            } finally {
                if (job != null) {
                    job.release();
                }
            }
        }
        if (this.requestParameters.isGetColumnDefinition()) {
            colDescriptions = this.getColumnDescriptions(
                    this.requestParameters.getAction(), dataTypes);
        }
        DataDisplay display = viewFactory.getDataDisplay();
        display.displayDataPage(speciesList, formDetails, colDescriptions,
                rawDataContainers, rawDataCountContainers, rawDataPostFilters);

        log.traceExit();
    }

    private void processExprCallPage(List<Species> speciesList, DataFormDetails formDetails)
            throws InvalidRequestException, ThreadAlreadyWorkingException,
            TooManyJobsException, IOException {
        log.traceEntry("{}, {}", speciesList, formDetails);

        log.debug("Action identified: {}", this.requestParameters.getAction());
        List<ColumnDescription> colDescriptions = null;
        List<ExpressionCall2> calls = null;
        Long count = null;
        ExpressionCallPostFilter postFilter = null;

        URLParameters urlParameters = requestParameters.getUrlParametersInstance();
        //Condition parameters
        Set<String> selectedCondParams = new HashSet<>(
                Optional.ofNullable(requestParameters.getValues(urlParameters.getCondParam2()))
                .orElse(Collections.emptyList()));
        LinkedHashSet<ConditionParameter<?, ?>> condParams = selectedCondParams.isEmpty()?
                //default value
                ConditionParameter.allOf():
                //otherwise retrieve condition parameters from request
                ConditionParameter.allOf()
                    .stream().filter(a -> selectedCondParams.contains(a.getParameterName()))
                    .collect(Collectors.toCollection(() -> new LinkedHashSet<>()));
        log.debug("Condition parameters: {}", condParams);
        EnumSet<DataType> dataTypes = this.checkAndGetDataTypes();
        log.debug("Requested data types: {}", dataTypes);
        

        //Queries that required an ExpressionCallLoader
        if (this.requestParameters.isGetResults() || this.requestParameters.isGetResultCount() ||
                this.requestParameters.isGetFilters()) {
            log.debug("Loading ExpressionCallLoader");

            //Either there is no filtering at all, or some genes must be requested.
            //Having a null species ID is required to have no filtering at all.
            //For other parameters than condParams and dataTypes, it is not possible
            //to set them in a ConditionFilter without providing a species ID,
            //so we don't have to explicitly check here if some are provided.
            if (this.requestParameters.getSpeciesId() != null ||
                    !condParams.isEmpty() && !condParams.containsAll(ConditionParameter.allOf()) ||
                        !dataTypes.isEmpty() && !dataTypes.equals(EnumSet.allOf(DataType.class))) {

                if (this.requestParameters.getGeneIds() == null ||
                    this.requestParameters.getGeneIds().isEmpty()) {
                    throw log.throwing(new InvalidRequestException("Some genes must be selected."));
                }
            }
            //Otherwise, filters are allowed to be requested only when there are some filtering.
            //Rather than throwing an exception, we will return an empty filter
            else if (this.requestParameters.isGetFilters()) {
//                throw log.throwing(new InvalidRequestException(
//                        "Post-filters can be requested only if some form filters are defined."));
                postFilter = new ExpressionCallPostFilter();
            }

            //try...finally block to manage number of jobs per users,
            //to limit the concurrent number of queries a user can make
            Job job = null;
            try {
                job = this.jobService.registerNewJob(this.user.getUUID().toString());
                job.startJob();
                //If filters are provided, they will be considered with this ExpressionCallLoader
                ExpressionCallLoader callLoader = this.loadExprCallLoader(true, condParams, dataTypes);

                //results
                if (this.requestParameters.isGetResults()) {
                    calls = this.loadExprCallResults(callLoader);
                }
                //Raw data counts
                if (this.requestParameters.isGetResultCount()) {
                    count = this.loadExprCallCount(callLoader);
                }
                //Filters. PostFilter is not null and is an empty filter if no genes are specified,
                //in that case we don't retrieve filters.
                if (this.requestParameters.isGetFilters() && postFilter == null) {
                    //For requesting getFilters, well, the filter parameters must be ignored
                    ExpressionCallLoader loaderToUse = callLoader;
                    ExpressionCallFilter2 noFilterParamFilter = this.loadExprCallFilter(
                            false, condParams, dataTypes);
                    //We try to avoid requesting a ProcessedFilter if not necessary,
                    //by comparing the RawDataFilters
                    if (!callLoader.getProcessedFilter()
                            .getSourceFilter().equals(noFilterParamFilter)) {
                        loaderToUse = this.loadExprCallLoader(noFilterParamFilter);
                    }
                    postFilter = this.loadExprCallPostFilters(loaderToUse);
                }

                job.completeWithSuccess();
            } finally {
                if (job != null) {
                    job.release();
                }
            }
        }
        if (this.requestParameters.isGetColumnDefinition()) {
            colDescriptions = this.getExprCallColumnDescriptions(condParams);
        }
        DataDisplay display = viewFactory.getDataDisplay();
        log.debug("Count: {}", count);
        log.trace("Calls: {}", calls);
        display.displayExprCallPage(speciesList, formDetails, colDescriptions,
                new ExpressionCallResponse(calls, condParams, dataTypes), count, postFilter);

        log.traceExit();
    }

    private void processExperimentPage() throws PageNotFoundException, IOException {
        log.traceEntry();

        //We don't use the loadRawDataLoader method, because there is no complex processing
        //of the RawDataFilter, so that we don't want to put it in cache
        RawDataLoader rawDataLoader = this.serviceFactory.getRawDataService()
                .loadRawDataLoader(this.loadRawDataFilter(false));

        //We don't know which data type the experiment belongs to,
        //so we test them all
        DataType dataTypeWithResults = null;
        RawDataContainerWithExperiment<?, ?, ?> container = null;
        for (DataType dt: EnumSet.allOf(DataType.class)) {
            RawDataDataType<? extends RawDataContainerWithExperiment<?, ?, ?>, ?> rdt =
                    RawDataDataType.getRawDataDataTypeWithExperiment(dt);
            //we skip data types that have no experiments
            if (rdt == null) {
                continue;
            }

            container = rawDataLoader.loadData(
                    //We want the assays to be displayed on the experiment page,
                    //The experiment itself will be retrieved along the way
                    InformationType.ASSAY,
                    rdt,
                    0L,
                    //RawDataLoader.LIMIT_MAX should always be defined to remain above
                    //the max number of assays in an experiment
                    RawDataLoader.LIMIT_MAX);
            if (!container.getAssays().isEmpty()) {
                //we found our result
                dataTypeWithResults = dt;
                break;
            }
            //otherwise we continue to search
            container = null;
        }
        if (container == null) {
            throw log.throwing(new PageNotFoundException("The experiment ID "
                    + this.requestParameters.getExperimentId() + " does not exist in Bgee."));
        }

        if (container.getExperiments().size() != 1) {
            throw log.throwing(new IllegalStateException(
                    "Ambiguous experiment ID, should not happen. Experiments retrieved: "
                    + container.getExperiments()));
        }
        assert dataTypeWithResults != null;
        Experiment<?> experiment = container.getExperiments().iterator().next();
        LinkedHashSet<Assay> assays = new LinkedHashSet<>(container.getAssays());
        List<ColumnDescription> colDescr;
        try {
            colDescr = this.getColumnDescriptions(
                    EXPERIMENT_PAGE_ACTION, EnumSet.of(dataTypeWithResults))
                    .get(dataTypeWithResults);
        } catch (InvalidRequestException e) {
            //here it means we didn't correctly called the method getColumnDescriptions,
            //it is not an InvalidRequestException
            throw log.throwing(new IllegalStateException(e));
        }

        DataDisplay display = viewFactory.getDataDisplay();
        display.displayExperimentPage(experiment, assays, dataTypeWithResults, colDescr);

        log.traceExit();
    }

    private List<Species> loadSpeciesList() {
        log.traceEntry();
        return log.traceExit(this.speciesService.loadSpeciesByIds(null, false)
                .stream()
                .sorted(Comparator.comparing(Species::getPreferredDisplayOrder))
                .collect(Collectors.toList()));
    }

    private DataFormDetails loadFormDetails() throws InvalidRequestException {
        log.traceEntry();

        DataFormDetails formDetails = null;
        if (this.requestParameters.isDetailedRequestParameters()) {
            Ontology<DevStage, String> requestedSpeciesDevStageOntology = null;
            List<Sex> requestedSpeciesSexes = null;
            List<AnatEntity> requestedAnatEntitesAndCellTypes = null;
            List<Gene> requestedGenes = null;
            List<ExperimentAssay> expAssays = null;

            Species requestedSpecies = this.loadRequestedSpecies();
            List<String> requestedGeneIds = this.requestParameters.getGeneIds();

            if (requestedSpecies != null) {
                int speciesId = requestedSpecies.getId();
                requestedSpeciesDevStageOntology = this.loadSpeciesStageOntology(speciesId);
                requestedSpeciesSexes = this.loadSpeciesSexes(speciesId);
                requestedGenes = this.loadRequestedGenes(speciesId, requestedGeneIds);

            } else if (requestedGeneIds != null && !requestedGeneIds.isEmpty()) {
                throw log.throwing(new InvalidRequestException(
                        "A species ID must be provided to query genes"));
            }
            requestedAnatEntitesAndCellTypes = this.loadRequestedAnatEntitesAndCellTypes();
            expAssays = this.loadRequestedExperimentsAndAssays();

            formDetails = new DataFormDetails(requestedSpecies, requestedSpeciesDevStageOntology,
                    requestedSpeciesSexes, requestedAnatEntitesAndCellTypes, requestedGenes, expAssays);
        }

        return log.traceExit(formDetails);
    }

    private Species loadRequestedSpecies() throws InvalidRequestException {
        log.traceEntry();
        Integer requestedSpeciesId = this.requestParameters.getSpeciesId();
        if (requestedSpeciesId == null) {
            return log.traceExit((Species) null);
        }
        Species species = this.speciesService.loadSpeciesByIds(Set.of(requestedSpeciesId), false)
                .stream().findAny().orElse(null);
        if (species == null) {
            throw log.throwing(new InvalidRequestException("No species corresponding to ID "
                    + requestedSpeciesId));
        }
        return log.traceExit(species);
    }

    private Ontology<DevStage, String> loadSpeciesStageOntology(int speciesId) {
        log.traceEntry("{}", speciesId);

        Set<DevStage> stages = this.serviceFactory.getDevStageService()
                .loadGroupingDevStages(Set.of(speciesId), null);

        return log.traceExit(this.serviceFactory.getOntologyService()
                .getDevStageOntologyFromDevStages(Set.of(speciesId), stages, false, false)
                .getAsSingleSpeciesOntology(speciesId));
    }

    private List<Sex> loadSpeciesSexes(int speciesId) {
        log.traceEntry("{}", speciesId);

        return log.traceExit(this.serviceFactory.getSexService().loadSexesBySpeciesId(speciesId).stream()
                // We filter out the "any". Users will either:
                // * select no sex to retrieve all results, including "mixed" or "NA"
                // * select all sexes (male, female, hermaphrodite) to retrieve all defined information
                // => there will be no possibility to select "other" (for retrieving only annotations
                // such as "mixed" or "NA".
                .filter(sex -> !sex.getId().equalsIgnoreCase(SexEnum.ANY.getStringRepresentation()))
                //Sort by their EnumSex representation for consistent ordering
                .sorted((s1, s2) -> SexEnum.convertToSexEnum(s1.getId()).compareTo(SexEnum.convertToSexEnum(s2.getId())))
                .collect(Collectors.toList()));
    }

    private List<AnatEntity> loadRequestedAnatEntitesAndCellTypes() throws InvalidRequestException {
        log.traceEntry();

        Set<String> anatEntityAndCellTypeIds = new HashSet<>();
        if (this.requestParameters.getAnatEntity() != null) {
            anatEntityAndCellTypeIds.addAll(this.requestParameters.getAnatEntity());
        }
        if (this.requestParameters.getCellType() != null) {
            anatEntityAndCellTypeIds.addAll(this.requestParameters.getCellType());
        }
        if (anatEntityAndCellTypeIds.isEmpty()) {
            return log.traceExit((List<AnatEntity>) null);
        }

        List<AnatEntity> anatEntities = this.serviceFactory.getAnatEntityService()
                .loadAnatEntities(anatEntityAndCellTypeIds, false)
                .sorted(Comparator.comparing(ae -> ae.getName()))
                .collect(Collectors.toList());
        if (anatEntities.size() != anatEntityAndCellTypeIds.size()) {
            Set<String> retrievedIds = anatEntities.stream()
                    .map(ae -> ae.getId())
                    .collect(Collectors.toSet());
            anatEntityAndCellTypeIds.removeAll(retrievedIds);
            throw log.throwing(new InvalidRequestException(
                    "Some anatomical entities or cell types could not be identified: "
                    + anatEntityAndCellTypeIds));
        }

        return log.traceExit(anatEntities);
    }

    private List<ExperimentAssay> loadRequestedExperimentsAndAssays() throws InvalidRequestException {
        log.traceEntry();

        Set<String> expAssayIds = this.requestParameters.getExpAssayId() == null? new HashSet<>():
            new HashSet<>(this.requestParameters.getExpAssayId());
        if (expAssayIds.isEmpty()) {
            return log.traceExit((List<ExperimentAssay>) null);
        }
        //ExperimentAssay is not a real object in the data source,
        //it was created for convenience for autocomplete searches.
        //For this reason, for now we keep on using the search tool,
        //rather than formally using a RawDataLoader to query experiments and assays
        //for each data type
        SearchMatchResultService service = this.serviceFactory.getSearchMatchResultService(this.prop);
        List<ExperimentAssay> results = expAssayIds.stream()
                .flatMap(id -> service.searchExperimentsAndAssaysByTerm(id, null, null)
                        .getSearchMatches().stream().map(sm -> sm.getSearchedObject()))
                .filter(ea -> expAssayIds.contains(ea.getId()))
                //There can be different assays or experiments with the same IDs
                //(either because assay IDs are not unique inside a data type,
                //or because exp/assay IDs exist in different data types).
                //We select the first one arbitrarily
                .collect(Collectors.groupingBy(ea -> ea.getId())).values().stream()
                .map(l -> l.iterator().next())
                .collect(Collectors.toList());
        log.debug("Results for expAssayIds {}: {}", expAssayIds, results);
        if (results.size() != expAssayIds.size()) {
            Set<String> retrievedIds = results.stream()
                    .map(ea -> ea.getId())
                    .collect(Collectors.toSet());
            expAssayIds.removeAll(retrievedIds);
            throw log.throwing(new InvalidRequestException(
                    "Some experiment or assay IDs could not be identified: "
                    + expAssayIds + " - Assays requested: " + this.requestParameters.getExpAssayId()));
        }

        return log.traceExit(results);
    }

    private List<Gene> loadRequestedGenes(int speciesId, Collection<String> requestedGeneIds) throws InvalidRequestException {
        log.traceEntry();
        if (requestedGeneIds == null || requestedGeneIds.isEmpty()) {
            return log.traceExit((List<Gene>) null);
        }
        Set<String> clonedGeneIds = new HashSet<>(requestedGeneIds);

        GeneFilter filter = new GeneFilter(speciesId, clonedGeneIds);
        List<Gene> genes = this.serviceFactory.getGeneService().loadGenes(filter)
                .sorted(Comparator.<Gene, String>comparing(g -> g.getName())
                        .thenComparing(Comparator.comparing(g -> g.getGeneId())))
                .collect(Collectors.toList());

        if (genes.size() != clonedGeneIds.size()) {
            Set<String> retrieveGeneIds = genes.stream().map(g -> g.getGeneId())
                    .collect(Collectors.toSet());
            clonedGeneIds.removeAll(retrieveGeneIds);
            throw log.throwing(new InvalidRequestException(
                    "Some genes could not be identified: " + clonedGeneIds));
        }
        return log.traceExit(genes);
    }

    private RawDataLoader loadRawDataLoader(boolean consideringFilters) {
        log.traceEntry("{}", consideringFilters);

        return log.traceExit(this.loadRawDataLoader(this.loadRawDataFilter(consideringFilters)));
    }
    private ExpressionCallLoader loadExprCallLoader(boolean consideringFilters,
            Set<ConditionParameter<?, ?>> condParams, EnumSet<DataType> dataTypes)
                    throws InvalidRequestException {
        log.traceEntry("{}, {}, {}", consideringFilters, condParams, dataTypes);

        return log.traceExit(this.loadExprCallLoader(
                this.loadExprCallFilter(consideringFilters, condParams, dataTypes)));
    }

    private RawDataLoader loadRawDataLoader(RawDataFilter filter) {
        log.traceEntry("{}", filter);

        RawDataService rawDataService = this.serviceFactory.getRawDataService();
        //Try to get the processed filter from the cache.
        RawDataProcessedFilter processedFilter = this.cacheService.useCacheNonAtomic(
                RAW_DATA_PROCESSED_FILTER_CACHE_DEF,
                filter,
                () -> rawDataService.processRawDataFilter(filter),
                COMPUTE_TIME_PROCESSED_FILTER_CACHE_MS);
        return log.traceExit(rawDataService.getRawDataLoader(processedFilter));
    }
    private ExpressionCallLoader loadExprCallLoader(ExpressionCallFilter2 filter) {
        log.traceEntry("{}", filter);

        ExpressionCallService callService = this.serviceFactory.getExpressionCallService();
        //Try to get the processed filter from the cache.
        ExpressionCallProcessedFilter processedFilter = this.cacheService.useCacheNonAtomic(
                EXPR_CALL_PROCESSED_FILTER_CACHE_DEF,
                filter,
                () -> callService.processExpressionCallFilter(filter),
                COMPUTE_TIME_PROCESSED_FILTER_CACHE_MS);
        return log.traceExit(callService.getCallLoader(processedFilter));
    }

    private RawDataFilter loadRawDataFilter(boolean consideringFilters) {
        log.traceEntry("{}", consideringFilters);

        Integer speciesId = this.requestParameters.getSpeciesId();

        Integer filterSpeciesId = !consideringFilters? null:
            this.requestParameters.getFirstValue(
                    this.requestParameters.getUrlParametersInstance().getParamFilterSpeciesId());
        List<String> filterExperimentIds = !consideringFilters? null:
            this.requestParameters.getValues(
                    this.requestParameters.getUrlParametersInstance().getParamFilterExperimentId());
        List<String> filterAssayIds = !consideringFilters? null:
            this.requestParameters.getValues(
                    this.requestParameters.getUrlParametersInstance().getParamFilterAssayId());
        List<String> filterAnatEntityIds = !consideringFilters? null:
            this.requestParameters.getValues(
                this.requestParameters.getUrlParametersInstance().getParamFilterAnatEntity());
        List<String> filterDevStageIds = !consideringFilters? null:
            this.requestParameters.getValues(
                this.requestParameters.getUrlParametersInstance().getParamFilterDevStage());
        List<String> filterCellTypeIds = !consideringFilters? null:
            this.requestParameters.getValues(
                this.requestParameters.getUrlParametersInstance().getParamFilterCellType());
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

        RawDataConditionFilter condFilter = null;
        try {
            condFilter = new RawDataConditionFilter(
                    //Filters override the related parameter from the form
                    filterSpeciesId != null? filterSpeciesId: speciesId,
                    filterAnatEntityIds != null && !filterAnatEntityIds.isEmpty()?
                            filterAnatEntityIds: this.requestParameters.getAnatEntity(),
                    filterDevStageIds != null && !filterDevStageIds.isEmpty()?
                            filterDevStageIds: this.requestParameters.getDevStage(),
                    filterCellTypeIds != null && !filterCellTypeIds.isEmpty()?
                            filterCellTypeIds: this.requestParameters.getCellType(),
                    filterSexIds != null && !filterSexIds.isEmpty()?
                            filterSexIds: sexes,
                    filterStrains != null && !filterStrains.isEmpty()?
                            filterStrains: this.requestParameters.getStrain(),
                    //And we never include child terms when the parameter comes from a filter.
                    filterAnatEntityIds != null && !filterAnatEntityIds.isEmpty()?
                            false: Boolean.TRUE.equals(this.requestParameters.getFirstValue(
                                    this.requestParameters.getUrlParametersInstance()
                                    .getParamAnatEntityDescendant())),
                    filterDevStageIds != null && !filterDevStageIds.isEmpty()?
                            false: Boolean.TRUE.equals(this.requestParameters.getFirstValue(
                                    this.requestParameters.getUrlParametersInstance()
                                    .getParamStageDescendant())),
                    filterCellTypeIds != null && !filterCellTypeIds.isEmpty()?
                            false: Boolean.TRUE.equals(this.requestParameters.getFirstValue(
                                    this.requestParameters.getUrlParametersInstance()
                                    .getParamCellTypeDescendant())),
                    //we don't really have an ontology of sexes, only one root with one level down
                    //for sub-terms. Selecting the root should mean "select all terms", so we include
                    //sub-terms by default, unless it comes from a filter.
                    filterSexIds != null && !filterSexIds.isEmpty()? false: true,
                    //we don't really have an ontology of strains, only one root with one level down
                    //for sub-terms. Selecting the root should mean "select all terms", so we include
                    //sub-terms by default, unless it comes from a filter.
                    filterStrains != null && !filterStrains.isEmpty()? false: true);
        } catch (IllegalArgumentException e) {
            //nothing to do, we just did not have the appropriate parameters to create
            //a condition filter
            log.catching(e);
        }


        Collection<String> expOrAssayIds = this.requestParameters.getExpAssayId();
        String experimentId = this.requestParameters.getExperimentId();
        Set<String> experimentIds = experimentId != null? Collections.singleton(experimentId): null;
        GeneFilter geneFilter = speciesId == null && filterSpeciesId == null? null:
            new GeneFilter(filterSpeciesId != null? filterSpeciesId: speciesId,
                    this.requestParameters.getGeneIds());

        return log.traceExit(new RawDataFilter(
                geneFilter != null? Collections.singleton(geneFilter): null,
                condFilter != null? Collections.singleton(condFilter): null,
                filterExperimentIds != null && !filterExperimentIds.isEmpty()?
                        filterExperimentIds: experimentIds,
                //there is no for parameter for assayId only, so we always use the filter directly
                filterAssayIds,
                expOrAssayIds));
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

        //ANAT ENTITY AND CELL TYPE
        FilterIds<String> anatEntityFilter = new FilterIds<>(
                //Filters override the related parameter from the form
                filterAnatEntityCellTypeIds != null && !filterAnatEntityCellTypeIds.isEmpty()?
                        filterAnatEntityCellTypeIds: this.requestParameters.getAnatEntity(),
                //And we never include child terms when the parameter comes from a filter.
                filterAnatEntityCellTypeIds != null && !filterAnatEntityCellTypeIds.isEmpty() ||
                this.requestParameters.getAnatEntity() == null ||
                this.requestParameters.getAnatEntity().isEmpty()?
                        false: Boolean.TRUE.equals(this.requestParameters.getFirstValue(
                                this.requestParameters.getUrlParametersInstance()
                                .getParamAnatEntityDescendant())));
        FilterIds<String> cellTypeFilter = new FilterIds<>(
                //Filters override the related parameter from the form
                filterAnatEntityCellTypeIds != null && !filterAnatEntityCellTypeIds.isEmpty()?
                        filterAnatEntityCellTypeIds: this.requestParameters.getCellType(),
                //And we never include child terms when the parameter comes from a filter.
                filterAnatEntityCellTypeIds != null && !filterAnatEntityCellTypeIds.isEmpty() ||
                this.requestParameters.getCellType() == null ||
                this.requestParameters.getCellType().isEmpty()?
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
                    null);
            if (condFilter.areAllCondParamFiltersEmpty()) {
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
                    null, null));
        } catch (IllegalArgumentException e) {
            log.catching(Level.DEBUG, e);
            throw log.throwing(new InvalidRequestException("Incorrect parameters"));
        }
    }

    private EnumMap<DataType, RawDataContainer<?, ?>> loadRawDataResults(RawDataLoader rawDataLoader,
            EnumSet<DataType> dataTypes, InformationType infoType) throws InvalidRequestException {
        log.traceEntry("{}, {}, {}", rawDataLoader, dataTypes, infoType);

        Integer limit = this.requestParameters.getLimit() == null? DEFAULT_LIMIT:
            this.requestParameters.getLimit();
        if (limit > LIMIT_MAX) {
            throw log.throwing(new InvalidRequestException("It is not possible to request more than "
                    + LIMIT_MAX + " results."));
        }
        Long offset = this.requestParameters.getOffset() == null? 0:
            this.requestParameters.getOffset();
        if (offset != null && offset < 0) {
            throw log.throwing(new InvalidRequestException("Offset cannot be less than 0."));
        }
        if (this.requestParameters.getAction() == null) {
            throw log.throwing(new IllegalStateException("Wrong null value for parameter action"));
        }

        RawDataFilter sourceFilter = rawDataLoader.getRawDataProcessedFilter().getSourceFilter();
        return log.traceExit(dataTypes.stream()
                .collect(Collectors.toMap(
                        dt -> dt,
                        dt -> {
                            RawDataResultCacheKey cacheKey = new RawDataResultCacheKey(
                                    sourceFilter, dt,
                                    EnumSet.of(infoType), offset, limit);

                            return this.cacheService.useCacheNonAtomic(
                                    RAW_DATA_RESULT_CACHE_DEF,
                                    cacheKey,
                                    () -> rawDataLoader.loadData(infoType,
                                            RawDataDataType.getRawDataDataType(dt),
                                            offset, limit),
                                    COMPUTE_TIME_RESULT_CACHE_MS);
                        },
                        (v1, v2) -> {throw new IllegalStateException("Key collision impossible");},
                        () -> new EnumMap<>(DataType.class))));
    }

    private List<ExpressionCall2> loadExprCallResults(ExpressionCallLoader callLoader)
            throws InvalidRequestException {
        log.traceEntry("{}", callLoader);

        Integer limit = this.requestParameters.getLimit() == null? DEFAULT_LIMIT:
            this.requestParameters.getLimit();
        if (limit > LIMIT_MAX) {
            throw log.throwing(new InvalidRequestException("It is not possible to request more than "
                    + LIMIT_MAX + " results."));
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
        List<ExpressionCall2> results = this.cacheService.useCacheNonAtomic(
                EXPR_CALL_RESULT_CACHE_DEF,
                cacheKey,
                () -> callLoader.loadData(offset, limit),
                COMPUTE_TIME_RESULT_CACHE_MS);
        return log.traceExit(results);
    }

    private EnumMap<DataType, RawDataCountContainer> loadRawDataCounts(RawDataLoader rawDataLoader,
            EnumSet<DataType> dataTypes, EnumSet<InformationType> infoTypes) {
        log.traceEntry("{}, {}, {}", rawDataLoader, dataTypes, infoTypes);

        int currentMaxInfoTypeIndex = infoTypes.size() - 1;
        int absoluteMaxInfoTypeIndex = EnumSet.allOf(InformationType.class).size() - 1;
        //we will check whether count results are in cache,
        //otherwise we'll add them to the cache when the query is too slow
        EnumMap<DataType, RawDataCountContainer> counts = new EnumMap<>(DataType.class);
        RawDataFilter sourceFilter = rawDataLoader.getRawDataProcessedFilter().getSourceFilter();
        Map<RawDataCacheKey, RawDataCountContainer> cache =
                this.cacheService.registerCache(RAW_DATA_COUNT_CACHE_DEF);

        for (DataType dt: dataTypes) {
            //We will also search for cached info with MORE information
            List<RawDataCacheKey> cacheKeys = new ArrayList<>();
            cacheKeys.add(new RawDataCacheKey(sourceFilter, dt, infoTypes));
            log.debug("Original cacheKey: {}", cacheKeys.get(0));
            EnumSet<InformationType> addedInfoTypes = EnumSet.copyOf(infoTypes);
            for (int i = currentMaxInfoTypeIndex + 1; i <= absoluteMaxInfoTypeIndex; i++) {
                addedInfoTypes.add(InformationType.values()[i]);
                cacheKeys.add(new RawDataCacheKey(sourceFilter, dt, EnumSet.copyOf(addedInfoTypes)));
            }
            log.debug("All cacheKeys that will be requested: {}", cacheKeys);

            log.debug("Entries in the count cache before: {}", cache.size());
            //We start from -1 to have the correct index after the loop
            int cacheKeyIndex = -1;
            RawDataCountContainer countResult = null;
            while (countResult == null && cacheKeyIndex < cacheKeys.size() - 1) {
                cacheKeyIndex++;
                countResult = cache.get(cacheKeys.get(cacheKeyIndex));
            }

            if (countResult == null) {
                log.debug("Cache miss for search: {}", cacheKeys);
                long startTime = System.currentTimeMillis();
                countResult = rawDataLoader.loadDataCount(infoTypes,
                        RawDataDataType.getRawDataDataType(dt));
                long executionTime = System.currentTimeMillis() - startTime;
                if (executionTime > COMPUTE_TIME_COUNT_CACHE_MS) {
                    log.debug("Slow count query to store in cache, execution time: {}", executionTime);
                    log.trace("Cache before: {}", cache);
                    cache.putIfAbsent(cacheKeys.get(0), countResult);
                    log.trace("Cache after: {}", cache);
                } else {
                    log.debug("Count query fast enough, not stored in cache, execution time: {}", executionTime);
                }
            } else {
                log.debug("Cache hit for search: {}", cacheKeys.get(cacheKeyIndex));
                log.trace("Value: {}", countResult);
            }
            log.debug("Entries in the count cache after: {}", cache.size());

            counts.put(dt, countResult);
        }

        return log.traceExit(counts);
    }
    private long loadExprCallCount(ExpressionCallLoader callLoader) {
        log.traceEntry("{}", callLoader);
        return log.traceExit(this.cacheService.useCacheNonAtomic(
                EXPR_CALL_COUNT_CACHE_DEF,
                callLoader.getProcessedFilter().getSourceFilter(),
                () -> callLoader.loadDataCount(),
                COMPUTE_TIME_COUNT_CACHE_MS));
    }

    private EnumMap<DataType, RawDataPostFilter> loadRawDataPostFilters(RawDataLoader rawDataLoader,
            EnumSet<DataType> dataTypes, InformationType infoType) {
        log.traceEntry("{}, {}, {}", rawDataLoader, dataTypes, infoType);

        return log.traceExit(dataTypes.stream()
                .collect(Collectors.toMap(
                        dt -> dt,
                        dt -> {
                            RawDataCacheKey cacheKey = new RawDataCacheKey(
                                    rawDataLoader.getRawDataProcessedFilter().getSourceFilter(),
                                    dt, EnumSet.of(infoType));
                            return this.cacheService.useCacheNonAtomic(
                                    RAW_DATA_POST_FILTER_CACHE_DEF,
                                    cacheKey,
                                    () -> rawDataLoader.loadPostFilter(
                                            RawDataDataType.getRawDataDataType(dt), true, true,
                                            InformationType.EXPERIMENT.equals(infoType) &&
                                            //in case the DataType has no concept of experiments,
                                            //we need to retrieve the assay info anyway
                                            dt.isWithExperiments()? false: true),
                                    COMPUTE_TIME_POST_FILTER_CACHE_MS);
                        },
                        (v1, v2) -> {throw new IllegalStateException("Key collision impossible");},
                        () -> new EnumMap<>(DataType.class))));
    }
    private ExpressionCallPostFilter loadExprCallPostFilters(ExpressionCallLoader callLoader) {
        log.traceEntry("{}", callLoader);
        return log.traceExit(this.cacheService.useCacheNonAtomic(
                EXPR_CALL_POST_FILTER_CACHE_DEF,
                callLoader.getProcessedFilter().getSourceFilter(),
                () -> callLoader.loadPostFilter(),
                COMPUTE_TIME_POST_FILTER_CACHE_MS
                ));
    }

    private EnumMap<DataType, List<ColumnDescription>> getColumnDescriptions(String action,
            EnumSet<DataType> dataTypes) throws InvalidRequestException {
        log.traceEntry("{}, {}", action, dataTypes);
        EnumMap<DataType, List<ColumnDescription>> dataTypeToColDescr = new EnumMap<>(DataType.class);
        EnumMap<DataType, Supplier<List<ColumnDescription>>> dataTypeTolDescrSupplier =
                new EnumMap<>(DataType.class);

        if (RequestParameters.ACTION_RAW_DATA_ANNOTS.equals(action) ||
                EXPERIMENT_PAGE_ACTION.equals(action)) {
            boolean withExpInfo = RequestParameters.ACTION_RAW_DATA_ANNOTS.equals(action);
            dataTypeTolDescrSupplier.put(DataType.AFFYMETRIX,
                    () -> getAffymetrixRawDataAnnotsColumnDescriptions(withExpInfo));
            dataTypeTolDescrSupplier.put(DataType.RNA_SEQ,
                    () -> getRnaSeqRawDataAnnotsColumnDescriptions(false, withExpInfo));
            dataTypeTolDescrSupplier.put(DataType.SC_RNA_SEQ,
                    () -> getRnaSeqRawDataAnnotsColumnDescriptions(true, withExpInfo));
            //Of note, there's no experiment page for EST
            dataTypeTolDescrSupplier.put(DataType.EST,
                    () -> getESTRawDataAnnotsColumnDescriptions());
            dataTypeTolDescrSupplier.put(DataType.IN_SITU,
                    () -> getInSituRawDataAnnotsColumnDescriptions(withExpInfo));
        } else if (RequestParameters.ACTION_PROC_EXPR_VALUES.equals(action)) {
            dataTypeTolDescrSupplier.put(DataType.AFFYMETRIX,
                    () -> getAffymetrixProcExprValuesColumnDescriptions());
            dataTypeTolDescrSupplier.put(DataType.RNA_SEQ,
                    () -> getRnaSeqProcExprValuesColumnDescriptions(false));
            dataTypeTolDescrSupplier.put(DataType.SC_RNA_SEQ,
                    () -> getRnaSeqProcExprValuesColumnDescriptions(true));
            dataTypeTolDescrSupplier.put(DataType.EST,
                    () -> getESTProcExprValuesColumnDescriptions());
            dataTypeTolDescrSupplier.put(DataType.IN_SITU,
                    () -> getInSituProcExprValuesColumnDescriptions());
        } else if (RequestParameters.ACTION_EXPERIMENTS.equals(action)) {
            dataTypeTolDescrSupplier.put(DataType.AFFYMETRIX,
                    () -> getAffymetrixExperimentsColumnDescriptions());
            dataTypeTolDescrSupplier.put(DataType.RNA_SEQ,
                    () -> getRnaSeqExperimentsColumnDescriptions());
            dataTypeTolDescrSupplier.put(DataType.SC_RNA_SEQ,
                    () -> getRnaSeqExperimentsColumnDescriptions());
            dataTypeTolDescrSupplier.put(DataType.EST,
                    () -> getESTExperimentsColumnDescriptions());
            dataTypeTolDescrSupplier.put(DataType.IN_SITU,
                    () -> getInSituExperimentsColumnDescriptions());
        } else {
            throw log.throwing(new InvalidRequestException("Unsupported action for column definition: " +
                    this.requestParameters.getAction()));
        }

        for (DataType dataType: dataTypes) {
            Supplier<List<ColumnDescription>> supplier = dataTypeTolDescrSupplier.get(dataType);
            if (supplier == null) {
                throw log.throwing(new IllegalStateException(
                        "No column supplier for data type: " + dataType));
            }
            dataTypeToColDescr.put(dataType, supplier.get());
        }

        return log.traceExit(dataTypeToColDescr);
    }
    private List<ColumnDescription> getExprCallColumnDescriptions(Set<ConditionParameter<?, ?>> condParams) {
        log.traceEntry("{}", condParams);
        List<ColumnDescription> colDescr = new ArrayList<>();

        colDescr.add(new ColumnDescription("Gene ID", null,
                List.of("result.gene.geneId"),
                ColumnDescription.ColumnType.INTERNAL_LINK,
                ColumnDescription.INTERNAL_LINK_TARGET_GENE,
                null, true,
                "result.gene.geneMappedToSameGeneIdCount",
                "result.gene.species.id"));
        colDescr.add(new ColumnDescription("Gene name", null,
                List.of("result.gene.name"),
                ColumnDescription.ColumnType.STRING,
                null, null, true, null, null));

        //Call information
        colDescr.add(new ColumnDescription("Present/absent call", null,
                List.of("result.expressionState"),
                ColumnDescription.ColumnType.STRING,
                null, null, true, null, null));
        colDescr.add(new ColumnDescription("Call quality", null,
                List.of("result.expressionQuality"),
                ColumnDescription.ColumnType.STRING,
                null, null, true, null, null));
        colDescr.add(new ColumnDescription("FDR",
                "FDR-corrected p-value of the test of significance of the gene being expressed in the condition.",
                List.of("result.fdr"),
                ColumnDescription.ColumnType.STRING,
                null, null, true, null, null));
        colDescr.add(new ColumnDescription("Expression score",
                "Normalized expression level information. Highest expression level: 100; lowest expression level: 0.",
                List.of("result.expressionScore.expressionScore"),
                ColumnDescription.ColumnType.NUMERIC,
                null, null, true, null, null));
        colDescr.add(new ColumnDescription("Expression score confidence",
                "Confidence in the validity of the expression score. Two values possible: \"low\" and \"high\".",
                List.of("result.expressionScore.expressionScoreConfidence"),
                ColumnDescription.ColumnType.STRING,
                null, null, true, null, null));
        colDescr.add(new ColumnDescription("Supporting data types",
                "Data types used to produce the present/absent expression call.",
                List.of("result.dataTypesWithData"),
                ColumnDescription.ColumnType.DATA_TYPE_SOURCE,
                null, null, true, null, null));

        //Condition
        if (condParams.contains(ConditionParameter.ANAT_ENTITY_CELL_TYPE)) {
            colDescr.add(new ColumnDescription("Anat. entity ID",
                    "ID of the anatomical localization of the sample",
                    List.of("result.condition.anatEntity.id"),
                    ColumnDescription.ColumnType.ANAT_ENTITY,
                    null, null, true, null, null));
            colDescr.add(new ColumnDescription("Anat. entity name",
                    "Name of the anatomical localization of the sample",
                    List.of("result.condition.anatEntity.name"),
                    ColumnDescription.ColumnType.STRING,
                    null, null, true, null, null));
            colDescr.add(new ColumnDescription("Cell type ID",
                    "ID of the cell type of the sample",
                    List.of("result.condition.cellType.id"),
                    ColumnDescription.ColumnType.ANAT_ENTITY,
                    null, null, true, null, null));
            colDescr.add(new ColumnDescription("Cell type name",
                    "Name of the cell type of the sample",
                    List.of("result.condition.cellType.name"),
                    ColumnDescription.ColumnType.STRING,
                    null, null, true, null, null));
        }
        if (condParams.contains(ConditionParameter.DEV_STAGE)) {
            colDescr.add(new ColumnDescription("Stage ID",
                    "ID of the developmental and life stage of the sample",
                    List.of("result.condition.devStage.id"),
                    ColumnDescription.ColumnType.DEV_STAGE,
                    null, null, true, null, null));
            colDescr.add(new ColumnDescription("Stage name",
                    "Name of the developmental and life stage of the sample",
                    List.of("result.condition.devStage.name"),
                    ColumnDescription.ColumnType.STRING,
                    null, null, true, null, null));
        }
        if (condParams.contains(ConditionParameter.SEX)) {
            colDescr.add(new ColumnDescription("Sex",
                    "Annotation of the sex of the sample",
                    List.of("result.condition.sex.name"),
                    ColumnDescription.ColumnType.STRING,
                    null, null, true, null, null));
        }
        if (condParams.contains(ConditionParameter.STRAIN)) {
            colDescr.add(new ColumnDescription("Strain",
                    "Annotation of the strain of the sample",
                    List.of("result.condition.strain.name"),
                    ColumnDescription.ColumnType.STRING,
                    null, null, true, null, null));
        }
        colDescr.add(new ColumnDescription("Species", null,
                List.of("result.condition.species.genus",
                        "result.condition.species.speciesName"),
                ColumnDescription.ColumnType.STRING,
                null, null, true, null, null));

        //Link to supporting proc. expr. values
        colDescr.add(getExprCallsToProcExprValuesColDesc(condParams));

        return log.traceExit(colDescr);
    }

    private List<ColumnDescription> getAffymetrixRawDataAnnotsColumnDescriptions(
            boolean withExperimentInfo) {
        log.traceEntry("{}", withExperimentInfo);
        List<ColumnDescription> colDescr = new ArrayList<>();
        if (withExperimentInfo) {
            colDescr.add(new ColumnDescription("Experiment ID", null,
                    List.of("result.experiment.id"),
                    ColumnDescription.ColumnType.INTERNAL_LINK,
                    ColumnDescription.INTERNAL_LINK_TARGET_EXP, null, true, null, null));
            colDescr.add(new ColumnDescription("Experiment name", null,
                    List.of("result.experiment.name"),
                    ColumnDescription.ColumnType.STRING,
                    null, null, true, null, null));
        }
        colDescr.add(new ColumnDescription("Chip ID", "Identifier of the Affymetrix chip",
                List.of("result.id"),
                ColumnDescription.ColumnType.STRING,
                null, null, true, null, null));

        colDescr.addAll(getConditionColumnDescriptions("result", false));
        colDescr.add(getAnnotsToProcExprValuesColDesc("result.experiment.id", "result.id",
                null, false));

        return log.traceExit(colDescr);
    }
    private List<ColumnDescription> getRnaSeqRawDataAnnotsColumnDescriptions(boolean isSingleCell,
            boolean withExperimentInfo) {
        log.traceEntry("{}, {}", isSingleCell, withExperimentInfo);
        List<ColumnDescription> colDescr = new ArrayList<>();
        if (withExperimentInfo) {
            colDescr.add(new ColumnDescription("Experiment ID", null,
                    List.of("result.library.experiment.id"),
                    ColumnDescription.ColumnType.INTERNAL_LINK,
                    ColumnDescription.INTERNAL_LINK_TARGET_EXP, null, true, null, null));
            colDescr.add(new ColumnDescription("Experiment name", null,
                    List.of("result.library.experiment.name"),
                    ColumnDescription.ColumnType.STRING,
                    null, null, true, null, null));
        }
        colDescr.add(new ColumnDescription("Library ID", "Identifier of the RNA-Seq library",
                List.of("result.library.id"),
                ColumnDescription.ColumnType.STRING,
                null, null, true, null, null));

        colDescr.addAll(getConditionColumnDescriptions("result", isSingleCell));

        colDescr.add(new ColumnDescription("Technology", null,
                List.of("result.library.technology.protocolName"),
                ColumnDescription.ColumnType.STRING,
                null, null, true, null, null));
        colDescr.add(new ColumnDescription("Sequencing platform", null,
                List.of("result.library.technology.sequencingPlatfomName"),
                ColumnDescription.ColumnType.STRING,
                null, null, true, null, null));
        colDescr.add(new ColumnDescription("Sequenced transcript part",
                "Possible values are: full length, all parts of the transcript are sequenced; "
                + "3': only the 3' end of the transcript is sequenced; "
                + "5': only the 5' end of the transcript is sequenced.",
                List.of("result.library.technology.sequencedTranscriptPart"),
                ColumnDescription.ColumnType.STRING,
                null, null, true, null, null));
        if (isSingleCell) {
            colDescr.add(new ColumnDescription("Fractionation",
                    "Possible values are: cell, transcripts are extracted from the cell; "
                    + "nuclei, transcripts are extracted from the nucleus.",
                            List.of("result.library.technology.cellCompartment"),
                            ColumnDescription.ColumnType.STRING,
                            null, null, true, null, null));
        }
        colDescr.add(new ColumnDescription("Fragmentation",
                "Size of the RNA fragmentation",
                List.of("result.library.technology.fragmentation"),
                ColumnDescription.ColumnType.NUMERIC,
                null, null, true, null, null));
        colDescr.add(new ColumnDescription("Run sequencing type",
                "Paired-end or single-read run",
                List.of("result.library.technology.libraryType"),
                ColumnDescription.ColumnType.STRING,
                null, null, true, null, null));

        colDescr.add(new ColumnDescription("Total read count",
                "Total number of reads for the library.",
                List.of("result.library.pipelineSummary.allReadsCount"),
                ColumnDescription.ColumnType.NUMERIC,
                null, null, true, null, null));
        colDescr.add(new ColumnDescription("Mapped read count",
                "Number of reads that could be mapped to the transcriptome.",
                List.of("result.library.pipelineSummary.mappedReadsCount"),
                ColumnDescription.ColumnType.NUMERIC,
                null, null, true, null, null));
        colDescr.add(new ColumnDescription("Total UMI count",
                "Total number of individual RNA molecules (UMI) for the annotated sample. "
                + "Only applicable for libraries producing UMIs.",
                List.of("result.pipelineSummary.allUMIsCount"),
                ColumnDescription.ColumnType.NUMERIC,
                null, null, true, null, null));
        colDescr.add(new ColumnDescription("Mapped UMI count",
                "Number of UMIs that could be mapped to the transcriptome. "
                + "Only applicable for libraries producing UMIs.",
                List.of("result.pipelineSummary.mappedUMIsCount"),
                ColumnDescription.ColumnType.NUMERIC,
                null, null, true, null, null));

        colDescr.add(new ColumnDescription("Distinct rank count",
                "When performing a fractional ranking of the genes in the annotated sample, "
                + "based on their expression level, number of distinct ranks observed, "
                + "to have a value of the power for distinguishing expression levels. "
                + "Used as a weight to compute a weighted mean rank accross samples for each gene and "
                + "compute expression scores in Bgee.",
                List.of("result.pipelineSummary.distinctRankCount"),
                ColumnDescription.ColumnType.NUMERIC,
                null, null, true, null, null));
        colDescr.add(new ColumnDescription("Max rank",
                "When performing a fractional ranking of the genes in the annotated sample, "
                + "based on their expression level, maximum rank attained in the sample. "
                + "Used to normalize ranks accross samples and compute expression scores in Bgee.",
                List.of("result.pipelineSummary.maxRank"),
                ColumnDescription.ColumnType.NUMERIC,
                null, null, true, null, null));

        colDescr.add(getAnnotsToProcExprValuesColDesc("result.library.experiment.id", "result.library.id",
                "result", isSingleCell));

        return log.traceExit(colDescr);
    }
    private List<ColumnDescription> getESTRawDataAnnotsColumnDescriptions() {
        log.traceEntry();
        List<ColumnDescription> colDescr = new ArrayList<>();
        colDescr.add(new ColumnDescription("Library ID", null,
                List.of("result.id"),
                ColumnDescription.ColumnType.STRING,
                null, null, true, null, null));
        colDescr.add(new ColumnDescription("Library name", null,
                List.of("result.name"),
                ColumnDescription.ColumnType.STRING,
                null, null, true, null, null));
        colDescr.add(new ColumnDescription("Description", null,
                List.of("result.description"),
                ColumnDescription.ColumnType.STRING,
                null, null, true, null, null));

        colDescr.addAll(getConditionColumnDescriptions("result", false));
        colDescr.add(getAnnotsToProcExprValuesColDesc(null, "result.id",
                null, false));

        return log.traceExit(colDescr);
    }
    private List<ColumnDescription> getInSituRawDataAnnotsColumnDescriptions(
            boolean withExperimentInfo) {
        log.traceEntry("{}", withExperimentInfo);
        List<ColumnDescription> colDescr = new ArrayList<>();
        if (withExperimentInfo) {
            colDescr.add(new ColumnDescription("Experiment ID", null,
                    List.of("result.experiment.id"),
                    ColumnDescription.ColumnType.INTERNAL_LINK,
                    ColumnDescription.INTERNAL_LINK_TARGET_EXP, null, true, null, null));
        }
        colDescr.add(new ColumnDescription("Evidence ID", null,
                List.of("result.id"),
                ColumnDescription.ColumnType.STRING,
                null, null, true, null, null));

        colDescr.addAll(getConditionColumnDescriptions("result", false));
        colDescr.add(getAnnotsToProcExprValuesColDesc("result.experiment.id", "result.id",
                null, false));

        return log.traceExit(colDescr);
    }
    private ColumnDescription getAnnotsToProcExprValuesColDesc(String expIdAttrName,
            String assayIdAttrName, String annotAttributeStart, boolean displayCellType) {
        log.traceEntry("{}, {}, {}, {}", expIdAttrName, assayIdAttrName,
               annotAttributeStart, displayCellType);

        //First we create the list of FilterTargets
        List<ColumnDescription.FilterTarget> filterTargets = new ArrayList<>();

        //Usual way of selecting raw data annots (based on exp ID and assay ID,
        //because some Affymetrix assay IDs are not unique).
        //expIdAttrName can be null (for EST data, that have no concept of experiments)
        if (expIdAttrName != null) {
            filterTargets.add(new ColumnDescription.FilterTarget(expIdAttrName,
                    this.requestParameters.getUrlParametersInstance()
                    .getParamFilterExperimentId().getName()));
        }
        //Filter on assay ID, always used
        filterTargets.add(new ColumnDescription.FilterTarget(assayIdAttrName,
                        this.requestParameters.getUrlParametersInstance()
                        .getParamFilterAssayId().getName()));

        //For RNA-Seq data, there can be several "annotated samples" in a same library,
        //so we need to prefill the condition filters as well.
        if (annotAttributeStart != null) {
            if (displayCellType) {
                filterTargets.add(new ColumnDescription.FilterTarget(
                        annotAttributeStart + ".annotation.rawDataCondition.cellType.id",
                        this.requestParameters.getUrlParametersInstance()
                        .getParamFilterCellType().getName()));
            }
            filterTargets.add(new ColumnDescription.FilterTarget(
                    annotAttributeStart + ".annotation.rawDataCondition.anatEntity.id",
                    this.requestParameters.getUrlParametersInstance()
                    .getParamFilterAnatEntity().getName()));
            filterTargets.add(new ColumnDescription.FilterTarget(
                    annotAttributeStart + ".annotation.rawDataCondition.devStage.id",
                    this.requestParameters.getUrlParametersInstance()
                    .getParamFilterDevStage().getName()));
            filterTargets.add(new ColumnDescription.FilterTarget(
                    annotAttributeStart + ".annotation.rawDataCondition.sex",
                    this.requestParameters.getUrlParametersInstance()
                    .getParamFilterSex().getName()));
            filterTargets.add(new ColumnDescription.FilterTarget(
                    annotAttributeStart + ".annotation.rawDataCondition.strain",
                    this.requestParameters.getUrlParametersInstance()
                    .getParamFilterStrain().getName()));
            filterTargets.add(new ColumnDescription.FilterTarget(
                    annotAttributeStart + ".annotation.rawDataCondition.species.id",
                    this.requestParameters.getUrlParametersInstance()
                    .getParamFilterSpeciesId().getName()));
        }

        return log.traceExit(new ColumnDescription("Link to processed expression values",
                "See the processed expression value results for this assay",
                null,
                ColumnDescription.ColumnType.LINK_TO_PROC_EXPR_VALUES,
                null, filterTargets, false, null, null));
    }

    private ColumnDescription getExprCallsToProcExprValuesColDesc(Collection<ConditionParameter<?, ?>> condParams) {
        log.traceEntry("{}", condParams);

        //First we create the list of FilterTargets
        List<ColumnDescription.FilterTarget> filterTargets = new ArrayList<>();
        filterTargets.add(new ColumnDescription.FilterTarget("result.gene.geneId",
                this.requestParameters.getUrlParametersInstance()
                .getParamGeneId().getName()));
        filterTargets.add(new ColumnDescription.FilterTarget("result.gene.species.id",
                this.requestParameters.getUrlParametersInstance()
                .getParamSpeciesId().getName()));
        if (condParams.contains(ConditionParameter.ANAT_ENTITY_CELL_TYPE)) {
            filterTargets.add(new ColumnDescription.FilterTarget("result.condition.anatEntity.id",
                    this.requestParameters.getUrlParametersInstance()
                    .getParamAnatEntity().getName()));
            filterTargets.add(new ColumnDescription.FilterTarget("result.condition.cellType.id",
                    this.requestParameters.getUrlParametersInstance()
                    .getParamCellType().getName()));
        }
        if (condParams.contains(ConditionParameter.DEV_STAGE)) {
            filterTargets.add(new ColumnDescription.FilterTarget("result.condition.devStage.id",
                    this.requestParameters.getUrlParametersInstance()
                    .getParamDevStage().getName()));
        }
        if (condParams.contains(ConditionParameter.SEX)) {
            filterTargets.add(new ColumnDescription.FilterTarget("result.condition.sex.id",
                    this.requestParameters.getUrlParametersInstance()
                    .getParamSex().getName()));
        }
        if (condParams.contains(ConditionParameter.STRAIN)) {
            filterTargets.add(new ColumnDescription.FilterTarget("result.condition.strain.id",
                    this.requestParameters.getUrlParametersInstance()
                    .getParamStrain().getName()));
        }

        return log.traceExit(new ColumnDescription("See supporting raw data",
                "See the processed expression values supporting this expression call",
                null,
                ColumnDescription.ColumnType.LINK_CALL_TO_PROC_EXPR_VALUES,
                null, filterTargets, false, null, null));
    }

    private List<ColumnDescription> getAffymetrixProcExprValuesColumnDescriptions() {
        log.traceEntry();
        List<ColumnDescription> colDescr = new ArrayList<>();
        colDescr.add(new ColumnDescription("Experiment ID", null,
                List.of("result.assay.experiment.id"),
                ColumnDescription.ColumnType.INTERNAL_LINK,
                ColumnDescription.INTERNAL_LINK_TARGET_EXP, null, true, null, null));
        colDescr.add(new ColumnDescription("Chip ID", "Identifier of the Affymetrix chip",
                List.of("result.assay.id"),
                ColumnDescription.ColumnType.STRING,
                null, null, true, null, null));
        colDescr.add(new ColumnDescription("Probeset ID", "Identifier of the probeset for the chip type",
                List.of("result.id"),
                ColumnDescription.ColumnType.STRING,
                null, null, true, null, null));
        colDescr.add(new ColumnDescription("Gene ID", null,
                List.of("result.expressionCall.gene.geneId"),
                ColumnDescription.ColumnType.INTERNAL_LINK,
                ColumnDescription.INTERNAL_LINK_TARGET_GENE, null, true,
                "result.expressionCall.gene.geneMappedToSameGeneIdCount",
                "result.expressionCall.gene.species.id"));
        colDescr.add(new ColumnDescription("Gene name", null,
                List.of("result.expressionCall.gene.name"),
                ColumnDescription.ColumnType.STRING,
                null, null, true, null, null));
        colDescr.add(new ColumnDescription("Signal intensity",
                "Normalized signal intensity of the probeset",
                List.of("result.normalizedSignalIntensity"),
                ColumnDescription.ColumnType.NUMERIC,
                null, null, true, null, null));
        colDescr.add(new ColumnDescription("Expression p-value",
                "P-value for the test of expression signal of the gene "
                + "significantly different from background expression",
                List.of("result.expressionCall.pValue"),
                ColumnDescription.ColumnType.STRING,
                null, null, true, null, null));

        colDescr.addAll(getConditionColumnDescriptions("result.assay", false));

        return log.traceExit(colDescr);
    }
    private List<ColumnDescription> getRnaSeqProcExprValuesColumnDescriptions(boolean isSingleCell) {
        log.traceEntry();
        List<ColumnDescription> colDescr = new ArrayList<>();
        colDescr.add(new ColumnDescription("Experiment ID", null,
                List.of("result.assay.library.experiment.id"),
                ColumnDescription.ColumnType.INTERNAL_LINK,
                ColumnDescription.INTERNAL_LINK_TARGET_EXP, null, true, null, null));
        colDescr.add(new ColumnDescription("Library ID", null,
                List.of("result.assay.library.id"),
                ColumnDescription.ColumnType.STRING,
                null, null, true, null, null));
        colDescr.add(new ColumnDescription("Gene ID", null,
                List.of("result.rawCall.gene.geneId"),
                ColumnDescription.ColumnType.INTERNAL_LINK,
                ColumnDescription.INTERNAL_LINK_TARGET_GENE, null, true,
                "result.rawCall.gene.geneMappedToSameGeneIdCount",
                "result.rawCall.gene.species.id"));
        colDescr.add(new ColumnDescription("Gene name", null,
                List.of("result.rawCall.gene.name"),
                ColumnDescription.ColumnType.STRING,
                null, null, true, null, null));
        colDescr.add(new ColumnDescription("Expression level",
                "Expression level in the unit specified.",
                List.of("result.abundance"),
                ColumnDescription.ColumnType.NUMERIC,
                null, null, true, null, null));
        colDescr.add(new ColumnDescription("Expression level unit",
                "Unit to apply to the expression levels.",
                List.of("result.abundanceUnit"),
                ColumnDescription.ColumnType.STRING,
                null, null, true, null, null));
        colDescr.add(new ColumnDescription("Read count",
                "Number of reads mapped to this gene.",
                List.of("result.readCounts"),
                ColumnDescription.ColumnType.NUMERIC,
                null, null, true, null, null));
        colDescr.add(new ColumnDescription("UMI count",
                "Number of UMIs mapped to this gene. "
                + "Only applicable for libraries producing UMIs.",
                List.of("result.umiCounts"),
                ColumnDescription.ColumnType.NUMERIC,
                null, null, true, null, null));
        colDescr.add(new ColumnDescription("Expression p-value",
                "P-value for the test of expression signal of the gene "
                + "significantly different from background expression",
                List.of("result.rawCall.pValue"),
                ColumnDescription.ColumnType.STRING,
                null, null, true, null, null));

        colDescr.addAll(getConditionColumnDescriptions("result.assay", isSingleCell));

        return log.traceExit(colDescr);
    }
    private List<ColumnDescription> getESTProcExprValuesColumnDescriptions() {
        log.traceEntry();
        List<ColumnDescription> colDescr = new ArrayList<>();
        colDescr.add(new ColumnDescription("Library ID", null,
                List.of("result.assay.id"),
                ColumnDescription.ColumnType.STRING,
                null, null, true, null, null));
        colDescr.add(new ColumnDescription("Library name", null,
                List.of("result.assay.name"),
                ColumnDescription.ColumnType.STRING,
                null, null, true, null, null));
        colDescr.add(new ColumnDescription("EST ID", "Identifier of the Expressed Sequence Tag",
                List.of("result.id"),
                ColumnDescription.ColumnType.STRING,
                null, null, true, null, null));
        colDescr.add(new ColumnDescription("Gene ID", null,
                List.of("result.rawCall.gene.geneId"),
                ColumnDescription.ColumnType.INTERNAL_LINK,
                ColumnDescription.INTERNAL_LINK_TARGET_GENE, null, true,
                "result.rawCall.gene.geneMappedToSameGeneIdCount",
                "result.rawCall.gene.species.id"));
        colDescr.add(new ColumnDescription("Gene name", null,
                List.of("result.rawCall.gene.name"),
                ColumnDescription.ColumnType.STRING,
                null, null, true, null, null));
        colDescr.add(new ColumnDescription("Expression p-value",
                "P-value for the test of expression signal of the gene "
                + "significantly different from background expression",
                List.of("result.rawCall.pValue"),
                ColumnDescription.ColumnType.STRING,
                null, null, true, null, null));

        colDescr.addAll(getConditionColumnDescriptions("result.assay", false));

        return log.traceExit(colDescr);
    }
    private List<ColumnDescription> getInSituProcExprValuesColumnDescriptions() {
        log.traceEntry();
        List<ColumnDescription> colDescr = new ArrayList<>();
        colDescr.add(new ColumnDescription("Experiment ID", null,
                List.of("result.assay.experiment.id"),
                ColumnDescription.ColumnType.INTERNAL_LINK,
                ColumnDescription.INTERNAL_LINK_TARGET_EXP, null, true, null, null));
        colDescr.add(new ColumnDescription("Evidence ID", null,
                List.of("result.assay.id"),
                ColumnDescription.ColumnType.STRING,
                null, null, true, null, null));
        colDescr.add(new ColumnDescription("Gene ID", null,
                List.of("result.rawCall.gene.geneId"),
                ColumnDescription.ColumnType.INTERNAL_LINK,
                ColumnDescription.INTERNAL_LINK_TARGET_GENE, null, true,
                "result.rawCall.gene.geneMappedToSameGeneIdCount",
                "result.rawCall.gene.species.id"));
        colDescr.add(new ColumnDescription("Gene name", null,
                List.of("result.rawCall.gene.name"),
                ColumnDescription.ColumnType.STRING,
                null, null, true, null, null));
        colDescr.add(new ColumnDescription("Expression p-value",
                "P-value are defined based on the staining intensity reported from the source database",
                List.of("result.rawCall.pValue"),
                ColumnDescription.ColumnType.STRING,
                null, null, true, null, null));

        colDescr.addAll(getConditionColumnDescriptions("result.assay", false));

        return log.traceExit(colDescr);
    }

    private List<ColumnDescription> getAffymetrixExperimentsColumnDescriptions() {
        log.traceEntry();

        List<ColumnDescription> colDescr = new ArrayList<>();
        colDescr.add(new ColumnDescription("Experiment ID", null,
                List.of("result.id"),
                ColumnDescription.ColumnType.INTERNAL_LINK,
                ColumnDescription.INTERNAL_LINK_TARGET_EXP, null, true, null, null));
        colDescr.add(new ColumnDescription("Experiment name", null,
                List.of("result.name"),
                ColumnDescription.ColumnType.STRING,
                null, null, true, null, null));
        colDescr.add(new ColumnDescription("Description", null,
                List.of("result.description"),
                ColumnDescription.ColumnType.STRING,
                null, null, true, null, null));
        colDescr.add(getExpToAnnotsColDesc("result.id"));
        return log.traceExit(colDescr);
    }
    private List<ColumnDescription> getRnaSeqExperimentsColumnDescriptions() {
        log.traceEntry();

        List<ColumnDescription> colDescr = new ArrayList<>();
        colDescr.add(new ColumnDescription("Experiment ID", null,
                List.of("result.id"),
                ColumnDescription.ColumnType.INTERNAL_LINK,
                ColumnDescription.INTERNAL_LINK_TARGET_EXP, null, true, null, null));
        colDescr.add(new ColumnDescription("Experiment name", null,
                List.of("result.name"),
                ColumnDescription.ColumnType.STRING,
                null, null, true, null, null));
        colDescr.add(new ColumnDescription("Description", null,
                List.of("result.description"),
                ColumnDescription.ColumnType.STRING,
                null, null, true, null, null));
        colDescr.add(getExpToAnnotsColDesc("result.id"));
        return log.traceExit(colDescr);
    }
    private List<ColumnDescription> getESTExperimentsColumnDescriptions() {
        log.traceEntry();

        List<ColumnDescription> colDescr = new ArrayList<>();
        colDescr.add(new ColumnDescription("Library ID", null,
                List.of("result.id"),
                ColumnDescription.ColumnType.STRING,
                null, null, true, null, null));
        colDescr.add(new ColumnDescription("Library name", null,
                List.of("result.name"),
                ColumnDescription.ColumnType.STRING,
                null, null, true, null, null));
        colDescr.add(new ColumnDescription("Description", null,
                List.of("result.description"),
                ColumnDescription.ColumnType.STRING,
                null, null, true, null, null));
        //We don't use the method getExprToAnnotsColDesc here,
        //because EST data have no concept of experiment.
        colDescr.add(new ColumnDescription("Link to raw data annotations",
                "See the raw data annotation results for this library",
                null,
                ColumnDescription.ColumnType.LINK_TO_RAW_DATA_ANNOTS,
                null, List.of(new ColumnDescription.FilterTarget("result.id",
                        this.requestParameters.getUrlParametersInstance()
                        .getParamFilterAssayId().getName())), false, null, null));

        return log.traceExit(colDescr);
    }
    private List<ColumnDescription> getInSituExperimentsColumnDescriptions() {
        log.traceEntry();

        List<ColumnDescription> colDescr = new ArrayList<>();
        colDescr.add(new ColumnDescription("Experiment ID", null,
                List.of("result.id"),
                ColumnDescription.ColumnType.INTERNAL_LINK,
                ColumnDescription.INTERNAL_LINK_TARGET_EXP, null, true, null, null));
        colDescr.add(getExpToAnnotsColDesc("result.id"));
        return log.traceExit(colDescr);
    }
    private ColumnDescription getExpToAnnotsColDesc(String expIdAttrName) {
        log.traceEntry("{}", expIdAttrName);
        return log.traceExit(new ColumnDescription("Link to raw data annotations",
                "See the raw data annotation results for this experiment",
                null,
                ColumnDescription.ColumnType.LINK_TO_RAW_DATA_ANNOTS,
                null, List.of(new ColumnDescription.FilterTarget(expIdAttrName,
                        this.requestParameters.getUrlParametersInstance()
                        .getParamFilterExperimentId().getName())), false, null, null));
    }

    private static List<ColumnDescription> getConditionColumnDescriptions(String attributeStart,
            boolean displayCellType) {
        log.traceEntry("{}, {}", attributeStart, displayCellType);
        List<ColumnDescription> colDescr = new ArrayList<>();

        if (displayCellType) {
            colDescr.add(new ColumnDescription("Cell type ID",
                    "ID of the cell type of the sample",
                    List.of(attributeStart + ".annotation.rawDataCondition.cellType.id"),
                    ColumnDescription.ColumnType.ANAT_ENTITY,
                    null, null, true, null, null));
            colDescr.add(new ColumnDescription("Cell type name",
                    "Name of the cell type of the sample",
                    List.of(attributeStart + ".annotation.rawDataCondition.cellType.name"),
                    ColumnDescription.ColumnType.STRING,
                    null, null, true, null, null));
        }
        colDescr.add(new ColumnDescription("Anat. entity ID",
                "ID of the anatomical localization of the sample",
                List.of(attributeStart + ".annotation.rawDataCondition.anatEntity.id"),
                ColumnDescription.ColumnType.ANAT_ENTITY,
                null, null, true, null, null));
        colDescr.add(new ColumnDescription("Anat. entity name",
                "Name of the anatomical localization of the sample",
                List.of(attributeStart + ".annotation.rawDataCondition.anatEntity.name"),
                ColumnDescription.ColumnType.STRING,
                null, null, true, null, null));
        colDescr.add(new ColumnDescription("Stage ID",
                "ID of the developmental and life stage of the sample",
                List.of(attributeStart + ".annotation.rawDataCondition.devStage.id"),
                ColumnDescription.ColumnType.DEV_STAGE,
                null, null, true, null, null));
        colDescr.add(new ColumnDescription("Stage name",
                "Name of the developmental and life stage of the sample",
                List.of(attributeStart + ".annotation.rawDataCondition.devStage.name"),
                ColumnDescription.ColumnType.STRING,
                null, null, true, null, null));
        colDescr.add(new ColumnDescription("Sex",
                "Annotation of the sex of the sample",
                List.of(attributeStart + ".annotation.rawDataCondition.sex"),
                ColumnDescription.ColumnType.STRING,
                null, null, true, null, null));
        colDescr.add(new ColumnDescription("Strain",
                "Annotation of the strain of the sample",
                List.of(attributeStart + ".annotation.rawDataCondition.strain"),
                ColumnDescription.ColumnType.STRING,
                null, null, true, null, null));
        colDescr.add(new ColumnDescription("Species", null,
                List.of(attributeStart + ".annotation.rawDataCondition.species.genus",
                        attributeStart + ".annotation.rawDataCondition.species.speciesName"),
                ColumnDescription.ColumnType.STRING,
                null, null, true, null, null));
        return log.traceExit(colDescr);
    }


    public void initializeCaches(long sleepBetweenCallsInMs) throws InterruptedException {
        log.traceEntry("{}", sleepBetweenCallsInMs);
        log.info("Initializing CommandData caches: {}", this.prop.isInitializeCommandDataCachesOnStartup());

        if (this.prop.isInitializeCommandDataCachesOnStartup()) {
            RawDataService rawDataService = serviceFactory.getRawDataService();
            EnumSet<DataType> dataTypes = EnumSet.allOf(DataType.class);

            //First we make one call for the counts without any parameter
            RawDataFilter filter = new RawDataFilter(null, null);
            EnumSet<InformationType> infoTypes = EnumSet.allOf(InformationType.class);
            RawDataLoader loader = rawDataService.loadRawDataLoader(filter);
            this.loadRawDataCounts(loader, dataTypes, infoTypes);

            //Then we make one call per species without any other parameters
            Set<Species> allSpecies = serviceFactory.getSpeciesService().loadSpeciesByIds(null, false);
            for (Species species: allSpecies) {
                // Put the thread to sleep so the other threads do not starve
                if (sleepBetweenCallsInMs > 0L) {
                    Thread.sleep(sleepBetweenCallsInMs);
                }
                RawDataFilter speciesFilter = new RawDataFilter(
                        Collections.singleton(new GeneFilter(species.getId())),
                        null);
                RawDataLoader loaderForSpecies = rawDataService.loadRawDataLoader(speciesFilter);
                this.loadRawDataCounts(loaderForSpecies, dataTypes, infoTypes);
            }
        }
        log.info("Initializing CommandData caches done.");
        log.traceExit();
    }
}