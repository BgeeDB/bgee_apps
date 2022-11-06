package org.bgee.model.expressiondata.rawdata;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.expressiondata.DataFilter;
import org.bgee.model.gene.GeneFilter;

/**
 * A {@code DataFilter} allowing to configure retrieval of raw data (see {@link RawDataService}).
 *
 * @author Frederic Bastian
 * @author Julien Wollbrett
 * @version Bgee 15.0, Nov. 2022
 * @since Bgee 15.0, Nov. 2022
 */
public class RawDataFilter extends DataFilter<RawDataConditionFilter> {
    private final static Logger log = LogManager.getLogger(RawDataFilter.class.getName());

    private final Set<String> experimentIds;
    private final Set<String> assayIds;
    private final Set<String> experimentOrAssayIds;

    public RawDataFilter(Collection<GeneFilter> geneFilters, Collection<RawDataConditionFilter> conditionFilters) {
        this(geneFilters, conditionFilters, null, null, null);
    }
    /**
     * @param geneFilters           A {@code Collection} of {@code GeneFilter}s specifying
     *                              the species to target, or some specific genes to target.
     * @param conditionFilters      A {@code Collection} of {@code RawDataConditionFilter}s specifying
     *                              the species to target, or some specific conditions to target.
     * @param experimentIds         A {@code Collection} of {@code String}s that are IDs of experiments
     *                              to consider. Only results part of these experiments will be returned.
     * @param assayIds              A {@code Collection} of {@code String}s that are IDs of assays
     *                              to consider. Only results part of these assays will be returned.
     * @param experimentOrAssayIds  A {@code Collection} of {@code String}s that are IDs of either
     *                              experiments or assays, in case it is not known which {@code String}s
     *                              are experiment IDs, and which are assay IDs.
     *                              Only results part of these experiments and/or assays will be returned.
     * @throws IllegalArgumentException
     */
    public RawDataFilter(Collection<GeneFilter> geneFilters, Collection<RawDataConditionFilter> conditionFilters,
            Collection<String> experimentIds, Collection<String> assayIds, Collection<String> experimentOrAssayIds)
                    throws IllegalArgumentException {
        super(geneFilters, conditionFilters);

        this.experimentIds = Collections.unmodifiableSet(experimentIds == null? new HashSet<>():
            new HashSet<>(experimentIds));
        this.assayIds = Collections.unmodifiableSet(assayIds == null? new HashSet<>():
            new HashSet<>(assayIds));
        this.experimentOrAssayIds = Collections.unmodifiableSet(experimentOrAssayIds == null? new HashSet<>():
            new HashSet<>(experimentOrAssayIds));

        Map<Integer, List<RawDataConditionFilter>> condFiltersPerSpecies = this.getConditionFilters().stream()
                .collect(Collectors.groupingBy(f -> f.getSpeciesId()));
        if (condFiltersPerSpecies.values().stream().anyMatch(l -> {
            boolean noFilter = false;
            boolean filter = false;
            for (RawDataConditionFilter f: l) {
                if (f.areAllCondParamFiltersEmpty()) {
                    noFilter = true;
                } else {
                    filter = true;
                }
            }
            return noFilter && filter;
        })) {
            throw log.throwing(new IllegalArgumentException(
                    "A RawDataConditionFilter queries all conditions in a species,"
                    + "while another RawDataConditionFilter queries some more specific conditions "
                    + "in that species"));
        }
    }

    /**
     * @return  A {@code Set} of {@code String}s that are IDs of experiments
     *          to consider. Only results part of these experiments will be returned.
     */
    public Set<String> getExperimentIds() {
        return experimentIds;
    }
    /**
     * @return  A {@code Set} of {@code String}s that are IDs of assays
     *          to consider. Only results part of these assays will be returned.
     */
    public Set<String> getAssayIds() {
        return assayIds;
    }
    /**
     * @return  A {@code Set} of {@code String}s that are IDs of either experiments or assays,
     *          in case it is not known which {@code String}s are experiment IDs, and which
     *          are assay IDs. Only results part of these experiments and/or assays will be returned.
     */
    public Set<String> getExperimentOrAssayIds() {
        return experimentOrAssayIds;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Objects.hash(assayIds, experimentIds, experimentOrAssayIds);
        return result;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (!(obj instanceof RawDataFilter))
            return false;
        RawDataFilter other = (RawDataFilter) obj;
        return Objects.equals(assayIds, other.assayIds)
                && Objects.equals(experimentIds, other.experimentIds)
                && Objects.equals(experimentOrAssayIds, other.experimentOrAssayIds);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("RawDataFilter [getGeneFilters()=").append(getGeneFilters())
               .append(", getConditionFilters()=").append(getConditionFilters())
               .append(", experimentIds=").append(experimentIds)
               .append(", assayIds=").append(assayIds)
               .append(", experimentOrAssayIds=").append(experimentOrAssayIds)
               .append("]");
        return builder.toString();
    }
}