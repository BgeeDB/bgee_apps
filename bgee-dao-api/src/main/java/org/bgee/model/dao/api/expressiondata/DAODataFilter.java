package org.bgee.model.dao.api.expressiondata;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class DAODataFilter<T extends DAOBaseConditionFilter> {
    private final static Logger log = LogManager.getLogger(DAODataFilter.class.getName());

    /**
     * @see #getGeneIds()
     */
    private final Set<Integer> geneIds;
    /**
     * @see #getSpeciesIds()
     */
    private final Set<Integer> speciesIds;
    /**
     * @see #getConditionFilters()
     */
    private final LinkedHashSet<T> conditionFilters;

    protected DAODataFilter(Collection<Integer> geneIds, Collection<Integer> speciesIds,
            Collection<T> conditionFilters) {
        log.traceEntry("{}, {}, {}", geneIds, speciesIds, conditionFilters);

        if (geneIds != null && geneIds.stream().anyMatch(id -> id == null)) {
            throw log.throwing(new IllegalArgumentException("No gene ID can be null"));
        }
        if (speciesIds != null && speciesIds.stream().anyMatch(id -> id == null)) {
            throw log.throwing(new IllegalArgumentException("No species ID can be null"));
        }
        if (conditionFilters != null && conditionFilters.stream().anyMatch(cf -> cf == null)) {
            throw log.throwing(new IllegalArgumentException("No DAOConditionFilter can be null"));
        }

        this.geneIds = Collections.unmodifiableSet(
                geneIds == null? new HashSet<>(): new HashSet<>(geneIds));
        this.speciesIds = Collections.unmodifiableSet(
                speciesIds == null? new HashSet<>(): new HashSet<>(speciesIds));
        //we'll use defensive copying for this one, no unmodifiableLinkedHashSet method
        this.conditionFilters = conditionFilters == null? new LinkedHashSet<>(): 
            new LinkedHashSet<>(conditionFilters);

        log.traceExit();
    }

    /**
     * @return  An unmodifiable {@code Set} of {@code Integer}s containing the IDs of genes used
     *          to filter expression queries. Can be {@code null} or empty. The species IDs corresponding to these gene IDs
     *          should <strong>NOT</strong> be provided in the {@code #getSpeciesIds()} {@code Collection}.
     */
    public Set<Integer> getGeneIds() {
        return geneIds;
    }
    /**
     * @return  An unmodifiable {@code Set} of {@code Integer}s containing the IDs of species used
     *          to filter expression queries. Can be {@code null} or empty. Will target all data for the targeted species.
     *          <strong>None</strong> of the gene IDs part of these species should be part of
     *          the {@link getGeneIds} {@code Collection}.
     */
    public Set<Integer> getSpeciesIds() {
        return speciesIds;
    }
    /**
     * @return  A {@code LinkedHashSet} of condition filters to configure the filtering 
     *          of conditions with expression data. If several {@code ConditionFilter}s are provided, 
     *          they are seen as "OR" conditions. Can be {@code null} or empty. 
     *          Provided as a {@code LinkedHashSet} for convenience, to consistently set parameters 
     *          in queries.
     */
    public LinkedHashSet<T> getConditionFilters() {
        //defensive copying, no unmodifiable LinkedHashSet
        return new LinkedHashSet<>(conditionFilters);
    }

    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((conditionFilters == null) ? 0 : conditionFilters.hashCode());
        result = prime * result + ((geneIds == null) ? 0 : geneIds.hashCode());
        result = prime * result + ((speciesIds == null) ? 0 : speciesIds.hashCode());
        return result;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof DAODataFilter)) {
            return false;
        }
        DAODataFilter<?> other = (DAODataFilter<?>) obj;
        if (conditionFilters == null) {
            if (other.conditionFilters != null) {
                return false;
            }
        } else if (!conditionFilters.equals(other.conditionFilters)) {
            return false;
        }
        if (geneIds == null) {
            if (other.geneIds != null) {
                return false;
            }
        } else if (!geneIds.equals(other.geneIds)) {
            return false;
        }
        if (speciesIds == null) {
            if (other.speciesIds != null) {
                return false;
            }
        } else if (!speciesIds.equals(other.speciesIds)) {
            return false;
        }
        return true;
    }
}
