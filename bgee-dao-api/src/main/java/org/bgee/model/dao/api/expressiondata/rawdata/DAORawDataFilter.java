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

public class DAORawDataFilter {
    private final static Logger log = LogManager.getLogger(DAORawDataFilter.class.getName());

    // Here there is a speciesId only if there is no other filtering
    // on Bgee gene IDs or raw data condition IDs
    private final Integer speciesId;
    private final Set<Integer> geneIds;
    private final Set<Integer> rawDataCondIds;

    private final Set<String> experimentIds;
    private final Set<String> assayIds;
    private final Set<String> exprOrAssayIds;

    /**
     * Constructor allowing to create a {@code DOARawDataFilter} for a given speciesId,
     * experimentIds and assayIds. The speciesId is mandatory.
     * @param speciesId                 An int corresponding to the ID of the species
     *                                  for which raw data has to be retrieved.
     * @param experimentIds             A {@code Set} of {@code String} corresponding to the
     *                                  experimentIds for which raw data has to be retrieved in the
     *                                  selected species. If null, all experiments are retrieved.
     * @param assayIds                  A {@code Set} of {@code String} corresponding to the
     *                                  assayIds for which raw data has to be retrieved in the
     *                                  selected species. If null, all assay are retrieved.
     * @param exprOrAssayIds            A {@code Set} of {@code String} corresponding rather to
     *                                  assayIds or experimentIds for which raw data has to be
     *                                  retrieved in the selected species. If null, all assay are
     *                                  retrieved.
     */
    public DAORawDataFilter(int speciesId, Set<String> experimentIds, Set<String> assayIds,
            Set<String> exprOrAssayIds) {
        this(speciesId, null, null, experimentIds, assayIds,
                exprOrAssayIds);
    }
    /**
     * Constructor allowing to create a {@code DOARawDataFilter} for a given set of experimentIds,
     * assayIds and exprOrAssayIds.
     * @param speciesId                 An int corresponding to the ID of the species
     *                                  for which raw data has to be retrieved.
     * @param experimentIds             A {@code Set} of {@code String} corresponding to the
     *                                  experimentIds for which raw data has to be retrieved in the
     *                                  selected species. If null, all experiments are retrieved.
     * @param assayIds                  A {@code Set} of {@code String} corresponding to the
     *                                  assayIds for which raw data has to be retrieved in the
     *                                  selected species. If null, all assay are retrieved.
     * @param exprOrAssayIds            A {@code Set} of {@code String} corresponding rather to
     *                                  assayIds or experimentIds for which raw data has to be
     *                                  retrieved in the selected species. If null, all assay are
     *                                  retrieved.
     */
    public DAORawDataFilter(Set<String> experimentIds, Set<String> assayIds,
            Set<String> exprOrAssayIds) {
        this(null, null, null, experimentIds, assayIds, exprOrAssayIds);
    }
    /**
     * Constructor allowing to create a {@code DOARawDataFilter} for given geneIds, rawDataCondIds,
     * experimentIds and assayIds. The speciesId is mandatory.
     * @param geneIds                   A {@code Set} of {@code Integer} corresponding to the IDs
     *                                  of the genes for which raw data has to be retrieved. If
     *                                  null, raw data will be retrieved for all genes. geneIds and
     *                                  rawDataCondIds can not be both null.
     * @param rawDataCondIds            A {@code Set} of {@code Integer} correpsonding to the IDs
     *                                  of the raw data conditions for which raw data has to be
     *                                  retrieved. If null, do not filter on raw data condition
     *                                  IDs. geneIds and rawDataCondIds can not be both null.
     */
    public DAORawDataFilter(Collection<Integer> geneIds, Collection<Integer> rawDataCondIds) {
        this(null, geneIds, rawDataCondIds, null, null, null);
    }
    /**
     * Constructor allowing to create a {@code DOARawDataFilter} for given geneIds, rawDataCondIds,
     * experimentIds and assayIds. The speciesId is mandatory.
     * @param geneIds                   A {@code Set} of {@code Integer} corresponding to the IDs
     *                                  of the genes for which raw data has to be retrieved. If
     *                                  null, raw data will be retrieved for all genes. geneIds and
     *                                  rawDataCondIds can not be both null.
     * @param rawDataCondIds            A {@code Set} of {@code Integer} correpsonding to the IDs
     *                                  of the raw data conditions for which raw data has to be
     *                                  retrieved. If null, do not filter on raw data condition
     *                                  IDs. geneIds and rawDataCondIds can not be both null.
     * @param experimentIds             A {@code Set} of {@code String} corresponding to the
     *                                  experimentIds for which raw data has to be retrieved in the
     *                                  selected species. If null, all experiments are retrieved.
     * @param assayIds                  A {@code Set} of {@code String} corresponding to the
     *                                  assayIds for which raw data has to be retrieved in the
     *                                  selected species. If null, all assay are retrieved.
     * @param exprOrAssayIds            A {@code Set} of {@code String} corresponding rather to
     *                                  assayIds or experimentIds for which raw data has to be
     *                                  retrieved in the selected species. If null, all assay are
     *                                  retrieved.
     */
    public DAORawDataFilter(Collection<Integer> geneIds, Collection<Integer> rawDataCondIds,
            Collection<String> experimentIds, Collection<String> assayIds,
            Collection<String> exprOrAssayIds) {
        this(null, geneIds, rawDataCondIds, experimentIds, assayIds,
                exprOrAssayIds);
    }
    private DAORawDataFilter(Integer speciesId, Collection<Integer> geneIds,
            Collection<Integer> rawDataCondIds, Collection<String> experimentIds,
            Collection<String> assayIds, Collection<String> exprOrAssayIds) {
        log.traceEntry("{}, {}, {}, {}, {}, {}", speciesId, geneIds, rawDataCondIds,
                experimentIds, assayIds, exprOrAssayIds);
        if (speciesId != null && speciesId <= 0) {
            throw log.throwing(new IllegalArgumentException("speciesId must be bigger than 0"));
        }
        this.speciesId = speciesId;
        this.geneIds = Collections.unmodifiableSet(geneIds == null? new HashSet<>() :
            geneIds.stream().filter(g -> g != null).collect(Collectors.toSet()));
        this.rawDataCondIds = Collections.unmodifiableSet(rawDataCondIds == null? new HashSet<>() :
            rawDataCondIds.stream().filter(c -> c != null).collect(Collectors.toSet()));
        this.experimentIds = Collections.unmodifiableSet(experimentIds == null? new HashSet<>() :
            experimentIds.stream().filter(e -> !StringUtils.isBlank(e)).collect(Collectors.toSet()));
        this.assayIds = Collections.unmodifiableSet(assayIds == null? new HashSet<>() :
            assayIds.stream().filter(a -> !StringUtils.isBlank(a)).collect(Collectors.toSet()));
        this.exprOrAssayIds = Collections.unmodifiableSet(exprOrAssayIds == null? new HashSet<>() :
            exprOrAssayIds.stream().filter(a -> !StringUtils.isBlank(a)).collect(Collectors.toSet()));
        if (this.speciesId == null && this.geneIds.isEmpty() && this.rawDataCondIds.isEmpty() &&
                this.experimentIds.isEmpty() && this.assayIds.isEmpty() &&
                this.exprOrAssayIds.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("At least one attribut among"
                    + " speciesId, geneIds, rawDataCondIds, experimentIds, assayIds and"
                    + " exprOrAssayIds should not be null or empty"));
        }
    }
    /**
     * @return The {@code Integer} corresponding to the ID of the species for which raw data has
     * to be retrieved. Not null only if do not filter on gene IDs or raw data condition IDs.
     * If null, do not filter on speciesId.
     */
    public Integer getSpeciesId() {
        return speciesId;
    }

    public Set<Integer> getGeneIds() {
        return geneIds;
    }
    public Set<Integer> getRawDataCondIds() {
        return rawDataCondIds;
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

    @Override
    public int hashCode() {
        return Objects.hash(assayIds, experimentIds, exprOrAssayIds, geneIds, rawDataCondIds, speciesId);
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DAORawDataFilter other = (DAORawDataFilter) obj;
        return Objects.equals(assayIds, other.assayIds) && Objects.equals(experimentIds, other.experimentIds)
                && Objects.equals(exprOrAssayIds, other.exprOrAssayIds) && Objects.equals(geneIds, other.geneIds)
                && Objects.equals(rawDataCondIds, other.rawDataCondIds) && Objects.equals(speciesId, other.speciesId);
    }

    @Override
    public String toString() {
        return "DAORawDataFilter [speciesId=" + speciesId + ", geneIds=" + geneIds + ", rawDataCondIds="
                + rawDataCondIds + ", experimentIds=" + experimentIds + ", assayIds=" + assayIds + ", exprOrAssayIds="
                + exprOrAssayIds + "]";
    }
    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
