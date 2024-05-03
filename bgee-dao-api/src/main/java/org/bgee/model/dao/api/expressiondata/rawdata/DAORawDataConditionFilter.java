package org.bgee.model.dao.api.expressiondata.rawdata;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.expressiondata.DAOBaseConditionFilter;

/**
 * A filter to parameterize queries for raw data.
 *
 * @author Frederic Bastian
 * @version Bgee 15 Sept. 2022
 * @since Bgee 14 Sept. 2018
 */
//TODO: TOComparator and related test
public class DAORawDataConditionFilter extends DAOBaseConditionFilter {
    private final static Logger log = LogManager.getLogger(DAORawDataConditionFilter.class.getName());

    private final Set<Integer> speciesIds;

    /**
     * @param speciesIds                A {@code Collection} of {@code Integer}s that are the IDs 
     *                                  of the species the retrieved conditions will be valid in.
     * @param anatEntityIds             A {@code Collection} of {@code String}s that are the IDs 
     *                                  of the anatomical entities to use.
     * @param devStageIds               A {@code Collection} of {@code String}s that are the IDs 
     *                                  of the developmental stages to use.
     * @param cellTypeIds               A {@code Collection} of {@code String}s that are the IDs 
     *                                  of the cell types to use.
     * @param sexIds                    A {@code Collection} of {@code String}s that are the IDs 
     *                                  of the sexes to use.
     * @param strainIds                 A {@code Collection} of {@code String}s that are the IDs 
     *                                  of the strains to use.
     * @throws IllegalArgumentException If all provided {@code Collection}s are {@code null} or empty. 
     */
    public DAORawDataConditionFilter(Collection<Integer> speciesIds,
            Collection<String> anatEntityIds, Collection<String> devStageIds,
            Collection<String> cellTypeIds, Collection<String>  sexIds, Collection<String> strainIds) {
        super(anatEntityIds, devStageIds, cellTypeIds, sexIds, strainIds, null);
        this.speciesIds = Collections.unmodifiableSet(speciesIds == null? new HashSet<>():
            speciesIds.stream().filter(id -> {return (id != null && id >= 1);}).collect(Collectors.toSet()));

        if (this.getSpeciesIds().isEmpty() &&
                this.areAllCondParamFiltersEmpty()) {
            throw log.throwing(new IllegalArgumentException(
                    "Some speciesIds, anatatomical entity IDs, developmental stage IDs, cell type IDs, sex IDs "
                    + "or strain IDs must be provided."));
        }
    }
    
    public Set<Integer> getSpeciesIds() {
        return speciesIds;
    }

    public boolean areAllCondParamFiltersEmpty() {
        log.traceEntry();
        return log.traceExit(this.getAnatEntityIds().isEmpty() &&
                this.getCellTypeIds().isEmpty() &&
                this.getDevStageIds().isEmpty() &&
                this.getSexIds().isEmpty() &&
                this.getStrainIds().isEmpty());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Objects.hash(speciesIds);
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
        DAORawDataConditionFilter other = (DAORawDataConditionFilter) obj;
        return Objects.equals(speciesIds, other.speciesIds);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("DAORawDataConditionFilter [speciesIds=").append(speciesIds)
               .append(", anatEntityIds=").append(getAnatEntityIds())
               .append(", devStageIds=").append(getDevStageIds())
               .append(", cellTypeIds=").append(getCellTypeIds())
               .append(", sexIds=").append(getSexIds())
               .append(", strainIds=").append(getStrainIds())
               .append("]");
        return builder.toString();
    }
}
