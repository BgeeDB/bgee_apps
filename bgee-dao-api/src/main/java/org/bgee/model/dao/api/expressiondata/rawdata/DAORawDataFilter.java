package org.bgee.model.dao.api.expressiondata.rawdata;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.expressiondata.DAODataFilter2;

public class DAORawDataFilter extends DAODataFilter2 {
    private final static Logger log = LogManager.getLogger(DAORawDataFilter.class.getName());

    private final Set<String> experimentIds;
    private final Set<String> assayIds;
    private final Set<String> exprOrAssayIds;
    private final Boolean usedInPropagatedCalls;

    /**
     * Constructor allowing to create a {@code DOARawDataFilter} for a given speciesId,
     * experimentIds and assayIds.
     * @param speciesIds                A {@code Collection} of {@code Integer}s that are the IDs
     *                                  of the species for which raw data has to be retrieved.
     * @param experimentIds             A {@code Collection} of {@code String} corresponding to the
     *                                  experimentIds for which raw data has to be retrieved in the
     *                                  selected species. If null, all experiments are retrieved.
     * @param assayIds                  A {@code Collection} of {@code String} corresponding to the
     *                                  assayIds for which raw data has to be retrieved in the
     *                                  selected species. If null, all assay are retrieved.
     * @param exprOrAssayIds            A {@code Collection} of {@code String} corresponding rather to
     *                                  assayIds or experimentIds for which raw data has to be
     *                                  retrieved in the selected species. If null, all assay are
     *                                  retrieved.
     * @param usedInPropagatedCalls     A {@code Boolean} allowing to specify if the library has to be used
     *                                  to generate calls. If <strong>true</strong> only libraries used to
     *                                  generate calls are retrieved. If <strong>false</strong> only libraries
     *                                  not used to generate calls are retrieved. If <strong>null</strong> then
     *                                  no filtering on generation of calls is applied to retrieve libraries.
     */
    public DAORawDataFilter(Collection<Integer> speciesIds, Collection<String> experimentIds,
            Collection<String> assayIds, Collection<String> exprOrAssayIds, Boolean usedInPropagatedCalls) {
        this(speciesIds, null, null, experimentIds, assayIds, exprOrAssayIds, usedInPropagatedCalls);
    }
    /**
     * Constructor allowing to create a {@code DOARawDataFilter} for a given set of experimentIds,
     * assayIds and exprOrAssayIds.
     * @param speciesId                 An int corresponding to the ID of the species
     *                                  for which raw data has to be retrieved.
     * @param experimentIds             A {@code Collection} of {@code String} corresponding to the
     *                                  experimentIds for which raw data has to be retrieved in the
     *                                  selected species. If null, all experiments are retrieved.
     * @param assayIds                  A {@code Collection} of {@code String} corresponding to the
     *                                  assayIds for which raw data has to be retrieved in the
     *                                  selected species. If null, all assay are retrieved.
     * @param exprOrAssayIds            A {@code Collection} of {@code String} corresponding rather to
     *                                  assayIds or experimentIds for which raw data has to be
     *                                  retrieved in the selected species. If null, all assay are
     *                                  retrieved.
     * @param usedInPropagatedCalls     A {@code Boolean} allowing to specify if the library has to be used
     *                                  to generate calls. If <strong>true</strong> only libraries used to
     *                                  generate calls are retrieved. If <strong>false</strong> only libraries
     *                                  not used to generate calls are retrieved. If <strong>null</strong> then
     *                                  no filtering on generation of calls is applied to retrieve libraries.
     */
    public DAORawDataFilter(Collection<String> experimentIds, Collection<String> assayIds,
            Collection<String> exprOrAssayIds, Boolean usedInPropagatedCalls) {
        this(null, null, null, experimentIds, assayIds, exprOrAssayIds, usedInPropagatedCalls);
    }
    /**
     * Constructor allowing to create a {@code DOARawDataFilter} for given geneIds, rawDataCondIds,
     * experimentIds and assayIds. The speciesId is mandatory.
     * @param geneIds                   A {@code Collection} of {@code Integer} corresponding to the IDs
     *                                  of the genes for which raw data has to be retrieved. If
     *                                  null, raw data will be retrieved for all genes. geneIds and
     *                                  rawDataCondIds can not be both null.
     * @param rawDataCondIds            A {@code Collection} of {@code Integer} correpsonding to the IDs
     *                                  of the raw data conditions for which raw data has to be
     *                                  retrieved. If null, do not filter on raw data condition
     *                                  IDs. geneIds and rawDataCondIds can not be both null.
     * @param usedInPropagatedCalls     A {@code Boolean} allowing to specify if the library has to be used
     *                                  to generate calls. If <strong>true</strong> only libraries used to
     *                                  generate calls are retrieved. If <strong>false</strong> only libraries
     *                                  not used to generate calls are retrieved. If <strong>null</strong> then
     *                                  no filtering on generation of calls is applied to retrieve libraries.
     */
    public DAORawDataFilter(Collection<Integer> geneIds, Collection<Integer> rawDataCondIds,
            Boolean usedInPropagatedCalls) {
        this(null, geneIds, rawDataCondIds, null, null, null, usedInPropagatedCalls);
    }
    /**
     * Constructor allowing to create a {@code DOARawDataFilter} for given geneIds, rawDataCondIds,
     * experimentIds and assayIds. The speciesId is mandatory.
     * @param geneIds                   A {@code Collection} of {@code Integer} corresponding to the IDs
     *                                  of the genes for which raw data has to be retrieved. If
     *                                  null, raw data will be retrieved for all genes. geneIds and
     *                                  rawDataCondIds can not be both null.
     * @param rawDataCondIds            A {@code Collection} of {@code Integer} corresponding to the IDs
     *                                  of the raw data conditions for which raw data has to be
     *                                  retrieved. If null, do not filter on raw data condition
     *                                  IDs. geneIds and rawDataCondIds can not be both null.
     * @param experimentIds             A {@code Collection} of {@code String} corresponding to the
     *                                  experimentIds for which raw data has to be retrieved in the
     *                                  selected species. If null, all experiments are retrieved.
     * @param assayIds                  A {@code Collection} of {@code String} corresponding to the
     *                                  assayIds for which raw data has to be retrieved in the
     *                                  selected species. If null, all assay are retrieved.
     * @param exprOrAssayIds            A {@code Collection} of {@code String} corresponding rather to
     *                                  assayIds or experimentIds for which raw data has to be
     *                                  retrieved in the selected species. If null, all assay are
     *                                  retrieved.
     * @param usedInPropagatedCalls     A {@code Boolean} allowing to specify if the library has to be used
     *                                  to generate calls. If <strong>true</strong> only libraries used to
     *                                  generate calls are retrieved. If <strong>false</strong> only libraries
     *                                  not used to generate calls are retrieved. If <strong>null</strong> then
     *                                  no filtering on generation of calls is applied to retrieve libraries.
     */
    public DAORawDataFilter(Collection<Integer> geneIds, Collection<Integer> rawDataCondIds,
            Collection<String> experimentIds, Collection<String> assayIds,
            Collection<String> exprOrAssayIds, Boolean usedInPropagatedCalls) {
        this(null, geneIds, rawDataCondIds, experimentIds, assayIds,
                exprOrAssayIds, usedInPropagatedCalls);
    }
    private DAORawDataFilter(Collection<Integer> speciesIds, Collection<Integer> geneIds,
            Collection<Integer> rawDataCondIds, Collection<String> experimentIds,
            Collection<String> assayIds, Collection<String> exprOrAssayIds,
            Boolean usedInPropagatedCalls) {
        super(speciesIds, geneIds, rawDataCondIds);
        log.traceEntry("{}, {}, {}, {}, {}, {}, {}", speciesIds, geneIds, rawDataCondIds,
                experimentIds, assayIds, exprOrAssayIds, usedInPropagatedCalls);

        this.experimentIds = Collections.unmodifiableSet(experimentIds == null? new HashSet<>() :
            experimentIds.stream().filter(e -> !StringUtils.isBlank(e)).collect(Collectors.toSet()));
        this.assayIds = Collections.unmodifiableSet(assayIds == null? new HashSet<>() :
            assayIds.stream().filter(a -> !StringUtils.isBlank(a)).collect(Collectors.toSet()));
        this.exprOrAssayIds = Collections.unmodifiableSet(exprOrAssayIds == null? new HashSet<>() :
            exprOrAssayIds.stream().filter(a -> !StringUtils.isBlank(a)).collect(Collectors.toSet()));
        this.usedInPropagatedCalls = usedInPropagatedCalls;
        // A not null attribute usedInPropagatedCalls is not sufficient to create a DAORawDataFilter
        if (this.getSpeciesIds().isEmpty() && this.getGeneIds().isEmpty() &&
                this.getConditionIds().isEmpty() &&
                this.experimentIds.isEmpty() && this.assayIds.isEmpty() &&
                this.exprOrAssayIds.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("At least one attribut among"
                    + " speciesId, geneIds, rawDataCondIds, experimentIds, assayIds and"
                    + " exprOrAssayIds should not be null or empty"));
        }
    }

    /**
     * @return The {@code Set} of {@code String} corresponding to the experimentIds for which raw
     * data has to be retrieved in the selected species. If null, all experiments are retrieved.
     */
    public Set<String> getExperimentIds() {
        return experimentIds;
    }
    /**
     * @return The {@code Set} of {@code String} corresponding to the assayIds for which raw data
     * has to be retrieved in the selected species. If null, all assay are retrieved.
     */
    public Set<String> getAssayIds() {
        return assayIds;
    }
    /**
     * @return A {@code Set} of {@code String} corresponding rather to assayIds or experimentIds
     * for which raw data has to be retrieved in the selected species. If null, all assay are
     * retrieved.
     */
    public Set<String> getExprOrAssayIds() {
        return exprOrAssayIds;
    }
    public Boolean isUsedInPropagatedCalls() {
        return usedInPropagatedCalls;
    }
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Objects.hash(assayIds, experimentIds, exprOrAssayIds, usedInPropagatedCalls);
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
        DAORawDataFilter other = (DAORawDataFilter) obj;
        return Objects.equals(assayIds, other.assayIds) && Objects.equals(experimentIds, other.experimentIds)
                && Objects.equals(exprOrAssayIds, other.exprOrAssayIds)
                && Objects.equals(usedInPropagatedCalls, other.usedInPropagatedCalls);
    }
    @Override
    public String toString() {
        return "DAORawDataFilter [experimentIds=" + experimentIds + ", assayIds=" + assayIds + ", exprOrAssayIds="
                + exprOrAssayIds + ", usedInPropagatedCalls=" + usedInPropagatedCalls + "]";
    }

}
