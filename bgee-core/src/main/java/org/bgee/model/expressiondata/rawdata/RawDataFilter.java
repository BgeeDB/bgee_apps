package org.bgee.model.expressiondata.rawdata;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    //TODO: refactor with method in ExpressionCallFilter2, also we didn't implemented in the same way
    private static Set<Integer> checkAndLoadSpeciesIdsConsidered(Collection<GeneFilter> geneFilters,
            Collection<RawDataConditionFilter> conditionFilters) throws IllegalArgumentException {
        log.traceEntry("{}, {}", geneFilters, conditionFilters);

        Map<Integer, List<RawDataConditionFilter>> condFiltersPerSpecies =
                (conditionFilters == null? Stream.<RawDataConditionFilter>of(): conditionFilters.stream())
                //Collectors.groupingBy doesn't accept null keys, we replace null key with 0.
                //Alternatively we could use an Optional.ofNullable
                .collect(Collectors.groupingBy(f -> f.getSpeciesId() == null? 0: f.getSpeciesId()));
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
                    "A RawDataConditionFilter queries all conditions in a species, "
                    + "while another RawDataConditionFilter queries some more specific conditions "
                    + "in that species"));
        }

        Set<Integer> geneFilterSpeciesIds = (geneFilters == null? Stream.<GeneFilter>of(): geneFilters.stream())
                .map(gf -> gf.getSpeciesId())
                .collect(Collectors.toSet());

        if (!geneFilterSpeciesIds.isEmpty() && !condFiltersPerSpecies.isEmpty()) {
            Set<Integer> condSpeciesNotFoundInGene = condFiltersPerSpecies.keySet().stream()
                    //0 is a special value to replace a null speciesId, targeting any species
                    //(see above)
                    .filter(id -> id != 0 && !geneFilterSpeciesIds.contains(id))
                    .collect(Collectors.toSet());
            if (!condSpeciesNotFoundInGene.isEmpty()) {
                throw log.throwing(new IllegalArgumentException(
                        "Some species IDs were requested in conditionFilters but not in geneFilters: "
                                + condSpeciesNotFoundInGene));
            }
            //0 is a special value to replace a null speciesId, targeting any species
            //(see above)
            if (!condFiltersPerSpecies.keySet().contains(0)) {
                Set<Integer> geneSpeciesNotFoundInCond = geneFilterSpeciesIds.stream()
                        .filter(id -> !condFiltersPerSpecies.keySet().contains(id))
                        .collect(Collectors.toSet());
                if (!geneSpeciesNotFoundInCond.isEmpty()) {
                    throw log.throwing(new IllegalArgumentException(
                            "Some species IDs were requested in geneFilters but not in conditionFilters: "
                                    + geneSpeciesNotFoundInCond));
                }
            }
        }

        Set<Integer> speciesIdsConsidered = new HashSet<>();
        if (!geneFilterSpeciesIds.isEmpty()) {
            speciesIdsConsidered = geneFilterSpeciesIds;
        } else if (!condFiltersPerSpecies.isEmpty() && !condFiltersPerSpecies.keySet().contains(0)) {
            speciesIdsConsidered = condFiltersPerSpecies.keySet();
        }
        return log.traceExit(speciesIdsConsidered);
    }
    //TODO refactor with method in ExpressionCallFilter2, it is the exact same implementation
    //Actually not used anymore
//    private static Set<Integer> loadSpeciesIdsWithNoParams(
//            Collection<GeneFilter> geneFilters, Collection<RawDataConditionFilter> conditionFilters) {
//        log.traceEntry("{}, {}", geneFilters, conditionFilters);
//
//        Set<Integer> geneFilterSpeciesIdsWithNoParams = (geneFilters == null?
//                Stream.<GeneFilter>of(): geneFilters.stream())
//                .filter(f -> f.getGeneIds().isEmpty())
//                .map(gf -> gf.getSpeciesId())
//                .collect(Collectors.toSet());
//        Set<Integer> condFilterSpeciesIdsWithNoParams = (conditionFilters == null?
//                Stream.<RawDataConditionFilter>of(): conditionFilters.stream())
//                .filter(f -> f.areAllCondParamFiltersEmpty() && f.getSpeciesId() != null)
//                .map(f -> f.getSpeciesId())
//                .collect(Collectors.toSet());
//        boolean condAllSpeciesSelected = (conditionFilters == null?
//                Stream.<RawDataConditionFilter>of(): conditionFilters.stream())
//                .anyMatch(f -> f.getSpeciesId() == null);
//
//        Set<Integer> speciesIdsWithNoParams = new HashSet<>(geneFilterSpeciesIdsWithNoParams);
//        speciesIdsWithNoParams.addAll(condFilterSpeciesIdsWithNoParams);
//        if (geneFilterSpeciesIdsWithNoParams.isEmpty() && condAllSpeciesSelected) {
//            speciesIdsWithNoParams = new HashSet<>();
//        }
//
//        return log.traceExit(speciesIdsWithNoParams);
//    }

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
     * @throws IllegalArgumentException If a RawDataConditionFilter queries all conditions in a species,
                                        while another RawDataConditionFilter queries
                                        some more specific conditions in that species.
                                        Or if a species is present in more than one {@code GeneFilter}.
                                        Or if not the same species are requested in {@code geneFilters}
                                        and {@code conditionFilters}.
     */
    public RawDataFilter(Collection<GeneFilter> geneFilters, Collection<RawDataConditionFilter> conditionFilters,
            Collection<String> experimentIds, Collection<String> assayIds, Collection<String> experimentOrAssayIds)
                    throws IllegalArgumentException {
        super(geneFilters, conditionFilters,
                checkAndLoadSpeciesIdsConsidered(geneFilters, conditionFilters));

        this.experimentIds = Collections.unmodifiableSet(experimentIds == null? new HashSet<>():
            new HashSet<>(experimentIds));
        this.assayIds = Collections.unmodifiableSet(assayIds == null? new HashSet<>():
            new HashSet<>(assayIds));
        this.experimentOrAssayIds = Collections.unmodifiableSet(experimentOrAssayIds == null? new HashSet<>():
            new HashSet<>(experimentOrAssayIds));
    }

    /**
     * @return  {@code true} if at least one of {@link #getExperimentsIds()},
     *          or {@link #getAssayIds}, or {@link #getExperimentOrAssayIds()},
     *          return non-empty {@code Set}s. {@code false} otherwise.
     */
    public boolean hasExperimentAssayIds() {
        log.traceEntry();
        return log.traceExit(!this.getExperimentIds().isEmpty() ||
                !this.getAssayIds().isEmpty() ||
                !this.getExperimentOrAssayIds().isEmpty());
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
        if (getClass() != obj.getClass())
            return false;
        RawDataFilter other = (RawDataFilter) obj;
        return Objects.equals(assayIds, other.assayIds) && Objects.equals(experimentIds, other.experimentIds)
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
               .append(", speciesIdsConsidered=").append(getSpeciesIdsConsidered())
               .append("]");
        return builder.toString();
    }
}