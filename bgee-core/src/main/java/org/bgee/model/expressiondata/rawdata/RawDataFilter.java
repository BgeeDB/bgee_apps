package org.bgee.model.expressiondata.rawdata;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.expressiondata.DataFilter;
import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.model.gene.GeneFilter;

//XXX: We decided the entry point will always be a conditionFilter i.e filter on propagated conditions.
//     This conditionFitler will then be used to retrieve associated raw conditions using the
//     globalCondToCond . But what about user querying directly the annotation interface to retrieve
//     all annotated experiment containing data coming from brain in human (without substructure)? 
//     How to filter for such queries? We could add columns "conditionRelationOriginConditionParameter"
//     e.g conditionRelationOriginAnatEntity in the globalCondToCond table for each condition parameter.
//     The second question is what happen if we annotated experiments not used to generate globalCalls? 
//     Don't we also want to retrieve them?
//     We should maybe propose both possibilities. Use a global condition filter OR a raw condition filter
//     and check that not both are not null.
//     the global cond filter will be used to go from propagated calls to raw data and the raw cond filter
//     will be used to query the annotation directly. We could even add a boolean allowing to define if we only
//     want to retrieve annotation part of a call (and then use the globalCondToCond to retrieve raw data)
public class RawDataFilter extends DataFilter<RawDataConditionFilter> {
    private final static Logger log = LogManager.getLogger(RawDataFilter.class.getName());

    private final EnumSet<DataType> dataTypes;

    public RawDataFilter(GeneFilter geneFilter, RawDataConditionFilter condFilter, DataType dataTypeFilter) {
        this(Collections.singleton(geneFilter), Collections.singleton(condFilter), EnumSet.of(dataTypeFilter));
    }

    /**
     * At least one {@code GeneFilter} or one {@code RawDataConditionFilter} must be provided. The {speciesId}s
     * returned by their method {@link GeneFilter#getSpeciesId()} and {@link RawDataConditionFilter#getSpeciesId()}
     * must match.
     *
     * @param geneFilters
     * @param conditionFilters
     * @param dataTypes                 If {@code null} or empty, all data types are considered.
     * @throws IllegalArgumentException If the Set of species IDs requested in {@code geneFilters} in the one hand,
     *                                  and {@code conditionFilters} in the other hand, don't match;
     *                                  or if no {@code {@code GeneFilter} and no {@code RawDataConditionFilter} is provided.
     */
    public RawDataFilter(Collection<GeneFilter> geneFilters, Collection<RawDataConditionFilter> conditionFilters,
            Collection<DataType> dataTypes) throws IllegalArgumentException {
        super(geneFilters, conditionFilters);
        if (dataTypes == null || dataTypes.isEmpty()) {
            this.dataTypes = EnumSet.allOf(DataType.class);
        } else {
            this.dataTypes = EnumSet.copyOf(dataTypes);
        }
        if (this.getGeneFilters().isEmpty() && this.getConditionFilters().isEmpty()) {
            throw log.throwing(new IllegalArgumentException("A GeneFilter or a RawDataConditionFilter must be provided"));
        }

        Set<Integer> geneFilterSpeciesIds = this.getGeneFilters().stream().map(f -> f.getSpeciesId())
                .collect(Collectors.toSet());
        Set<Integer> condFilterSpeciesIds = this.getConditionFilters().stream().map(f -> f.getSpeciesId())
                .collect(Collectors.toSet());
        if (!geneFilterSpeciesIds.isEmpty() && !condFilterSpeciesIds.isEmpty() &&
                !geneFilterSpeciesIds.equals(condFilterSpeciesIds)) {
            throw log.throwing(new IllegalArgumentException(
                    "Species IDs in GeneFilters and in RawDataConditionFilters do not match"));
        }

        Map<Integer, List<RawDataConditionFilter>> condFiltersPerSpecies = this.getConditionFilters().stream()
                .collect(Collectors.groupingBy(f -> f.getSpeciesId()));
        if (condFiltersPerSpecies.values().stream().anyMatch(l -> {
            boolean noFilter = false;
            boolean filter = false;
            for (RawDataConditionFilter f: l) {
                if (f.areAllFiltersEmptyWithoutConsideringSpeciesIds()) {
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

    public EnumSet<DataType> getDataTypes() {
        return dataTypes;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Objects.hash(dataTypes);
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
        RawDataFilter other = (RawDataFilter) obj;
        return Objects.equals(dataTypes, other.dataTypes);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("RawDataFilter [geneFilters=").append(getGeneFilters())
               .append(", conditionFilters=").append(getConditionFilters())
               .append(", dataTypes=").append(getDataTypes())
               .append("]");
        return builder.toString();
    }
}
