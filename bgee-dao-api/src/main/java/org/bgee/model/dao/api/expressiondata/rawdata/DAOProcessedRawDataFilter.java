package org.bgee.model.dao.api.expressiondata.rawdata;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @param <T>   The type of IDs of assay in the call table of a specific data type.
 *              Can be different from the public assay IDs if we use internal IDs.
 */
public class DAOProcessedRawDataFilter<T extends Comparable<T>> {

    private final static Logger log = LogManager.getLogger(DAOProcessedRawDataFilter.class.getName());
    private final Set<DAORawDataFilter> rawDataFilters;
    private final Map<DAORawDataFilter, Set<T>> filterToCallTableAssayIds;
    private final Class<T> callTableAssayIdType;
    private final boolean needGeneId;
    private final boolean needAssayId;
    private final boolean needExperimentId;
    private final boolean needSpeciesId;
    private final boolean needConditionId;
    private final boolean alwaysGeneId;

    public DAOProcessedRawDataFilter(Collection<DAORawDataFilter> rawDataFilters) {
        this(rawDataFilters, null, null);
    }
    public DAOProcessedRawDataFilter(Collection<DAORawDataFilter> rawDataFilters,
            Map<DAORawDataFilter, Set<T>> filterToCallTableAssayIds, Class<T> callTableAssayIdType) {

        this.filterToCallTableAssayIds = filterToCallTableAssayIds == null? null:
            Collections.unmodifiableMap(new HashMap<>(filterToCallTableAssayIds));
        if (filterToCallTableAssayIds != null && callTableAssayIdType == null) {
            throw log.throwing(new IllegalArgumentException(
                    "The class type of callTableAssayIds must be provided"));
        }
        this.callTableAssayIdType = callTableAssayIdType;

        this.rawDataFilters = Collections.unmodifiableSet(rawDataFilters == null ?
                new LinkedHashSet<>() : new LinkedHashSet<>(rawDataFilters));
        if (this.filterToCallTableAssayIds != null &&
                !this.filterToCallTableAssayIds.keySet().equals(this.rawDataFilters)) {
            throw log.throwing(new IllegalArgumentException(
                    "Inconsistent DAORawDataFilters provided in filterToCallTableAssayIds"));
        }

        //Actually when using a call table, even if we identified the callTableAssayIds,
        //we could retrieve the species by a join to the gene table.
        //But for now, for performance reasons, we go through the assay table
        //if any callTableAssayIds are provided
        this.needSpeciesId = this.filterToCallTableAssayIds != null? false:
            this.rawDataFilters.stream().anyMatch(e -> e.getSpeciesId() != null);
        this.needAssayId = this.filterToCallTableAssayIds != null? false:
            this.rawDataFilters.stream().anyMatch(e -> !e.getAssayIds().isEmpty() ||
                !e.getExprOrAssayIds().isEmpty());
        this.needConditionId = this.filterToCallTableAssayIds != null? false:
            this.rawDataFilters.stream().anyMatch(e -> !e.getRawDataCondIds().isEmpty());
        this.needExperimentId = this.filterToCallTableAssayIds != null? false:
            this.rawDataFilters.stream().anyMatch(e -> !e.getExperimentIds().isEmpty() ||
                !e.getExprOrAssayIds().isEmpty());
        //Assert to check the coherence of method DAORawDataFilter#hasFilteringNotConsideringGeneIds()
        //with this constructor
        assert(((this.needSpeciesId || this.needAssayId || this.needConditionId ||
                  this.needExperimentId) &&
                  this.filterToCallTableAssayIds == null &&
                  this.rawDataFilters.stream().anyMatch(f -> f.hasFilteringNotConsideringGeneIds()))
               ||
               (!this.needSpeciesId && !this.needAssayId && !this.needConditionId &&
                !this.needExperimentId &&
                (this.filterToCallTableAssayIds != null ||
                this.rawDataFilters.stream().noneMatch(f -> f.hasFilteringNotConsideringGeneIds()))));

        //Gene IDs always go through the call table, so we don't check filterToCallTableAssayIds
        this.needGeneId = this.rawDataFilters.stream().anyMatch(e -> !e.getGeneIds().isEmpty());
        //check filters always used
        //XXX The idea is to not start with result table if geneIds are asked in only one filter
        // but not in others. Indeed, in this scenario forcing to start with porbeset table
        // decrease drastically the time needed to query. It is maybe overthinking as it is
        // probably also the case for other tables (especially the species table). The best
        // optimization is probably to query each DAORawDataFilter separately
        this.alwaysGeneId = !this.rawDataFilters.isEmpty() //allMatch returns true if a stream is empty
                && this.rawDataFilters.stream().allMatch(e -> !e.getGeneIds().isEmpty());
        assert !(alwaysGeneId && needSpeciesId):
            "In a RawDataDAOFilter, when bgeeGeneIds are provided, no species ID is provided";
        log.debug(this.toString());
    }

    public Set<DAORawDataFilter> getRawDataFilters() {
        return rawDataFilters;
    }
    /**
     * @return  A {@code Map} where keys are {@code DAORawDataFilter}s
     *          part of {@link #getRawDataFilters()}, the associated value being a {@code Set}
     *          of {@code T}s that are the IDs of assays matching the {@code DAORawDataFilter}.
     *          If a {@code Set} is empty, it means there were no result for the associated filter.
     *          If this {@code Map} is {@code null}, it means that the callAssayIds were not retrieved
     *          and the filters should be processed normally.
     */
    public Map<DAORawDataFilter, Set<T>> getFilterToCallTableAssayIds() {
        return filterToCallTableAssayIds;
    }

    public boolean isNeedGeneId() {
        return needGeneId;
    }

    public boolean isNeedAssayId() {
        return needAssayId;
    }

    public boolean isNeedExperimentId() {
        return needExperimentId;
    }

    public boolean isNeedSpeciesId() {
        return needSpeciesId;
    }

    public boolean isNeedConditionId() {
        return needConditionId;
    }

    public boolean isAlwaysGeneId() {
        return alwaysGeneId;
    }

    public Class<T> getCallTableAssayIdType() {
        return callTableAssayIdType;
    }

    //We don't consider callTableAssayIdType in these hashCode/equals method,
    //Class does not implement equals
    @Override
    public int hashCode() {
        return Objects.hash(alwaysGeneId, filterToCallTableAssayIds, needAssayId,
                needConditionId, needExperimentId, needGeneId, needSpeciesId, rawDataFilters);
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DAOProcessedRawDataFilter<?> other = (DAOProcessedRawDataFilter<?>) obj;
        return alwaysGeneId == other.alwaysGeneId
                && Objects.equals(filterToCallTableAssayIds, other.filterToCallTableAssayIds)
                && needAssayId == other.needAssayId && needConditionId == other.needConditionId
                && needExperimentId == other.needExperimentId && needGeneId == other.needGeneId
                && needSpeciesId == other.needSpeciesId
                && Objects.equals(rawDataFilters, other.rawDataFilters);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("DAOProcessedRawDataFilter [")
               .append("rawDataFilters=").append(rawDataFilters)
               .append(", filterToCallTableAssayIds=").append(filterToCallTableAssayIds)
               .append(", callTableAssayIdType=").append(callTableAssayIdType)
               .append(", needGeneId=").append(needGeneId)
               .append(", needAssayId=").append(needAssayId)
               .append(", needExperimentId=").append(needExperimentId)
               .append(", needSpeciesId=").append(needSpeciesId)
               .append(", needConditionId=").append(needConditionId)
               .append(", alwaysGeneId=").append(alwaysGeneId)
               .append("]");
        return builder.toString();
    }
}
