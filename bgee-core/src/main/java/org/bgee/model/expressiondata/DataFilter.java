package org.bgee.model.expressiondata;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.gene.GeneFilter;

/**
 * Parent class of data filters, either raw data or calls.
 *
 * @author Frederic Bastian
 * @version Bgee 14 Sep. 2018
 * @since Bgee 14 Sep. 2018
 *
 * @param <T>   The type of {@code BaseConditionFilter} used by this {@code DataFilter}.
 */
public abstract class DataFilter<T extends BaseConditionFilter<?>> {
    private final static Logger log = LogManager.getLogger(DataFilter.class.getName());

    /**
     * @see #getGeneFilters()
     */
    //Note: The only problem with using directly ConditionFilters and CallDatas in this class, 
    //is that GeneFilters are costly to use in a query; using the class CallDataConditionFilter 
    //was allowing to have a same GeneFilter to target several conditions/call data combinations. 
    //Now, the same query would be doable by using several CallFilters, but with a same GeneFilter 
    //reused several times. This is costly, but we could have a mechanism to provide a global GeneFilter 
    //to the DAO when we see it is always the same GeneFilter used. 
    //I think it's worth it for the simplification it allows in the class CallFilter.
    private final Set<GeneFilter> geneFilters;
    
    /**
     * @see #getConditionFilters()
     */
    //XXX: all parameters are OR conditions
    private final Set<T> conditionFilters;

    private final Set<Integer> speciesIdsConsidered;

    protected DataFilter(Collection<GeneFilter> geneFilters, Collection<T> conditionFilters,
            Set<Integer> speciesIdsConsidered) {
        this.geneFilters = Collections.unmodifiableSet(geneFilters == null? new HashSet<>():
            new HashSet<>(geneFilters));
        this.conditionFilters = Collections.unmodifiableSet(conditionFilters == null? new HashSet<>():
            new HashSet<>(conditionFilters));
        this.speciesIdsConsidered = Collections.unmodifiableSet(speciesIdsConsidered == null? new HashSet<>():
            new HashSet<>(speciesIdsConsidered));
        if (this.geneFilters.stream().anyMatch(f -> f == null)) {
            throw log.throwing(new IllegalArgumentException("No GeneFilter can be null"));
        }
        if (this.conditionFilters.stream().anyMatch(f -> f == null)) {
            throw log.throwing(new IllegalArgumentException("No condition filter can be null"));
        }
        if (this.speciesIdsConsidered.stream().anyMatch(id -> id == null || id < 1)) {
            throw log.throwing(new IllegalArgumentException("No species ID considered can be null or less than 1"));
        }

        //make sure we don't have a same species in different GeneFilters
        if (this.geneFilters.stream().collect(Collectors.groupingBy(gf -> gf.getSpeciesId()))
                .values().stream().anyMatch(l -> l.size() > 1)) {
            throw log.throwing(new IllegalArgumentException(
                    "A species ID must be present in only one GeneFilter."));
        }
    }
    
    /**
     * @return  An unmodifiable {@code Set} {@code GeneFilter}s allowing to configure gene-related
     *          filtering. If several {@code GeneFilter}s are configured, they are seen as "OR" conditions.
     *          A same species ID should not be used in several {@code GeneFilter}s of this {@code Set}.
     */
    public Set<GeneFilter> getGeneFilters() {
        return geneFilters;
    }
    /**
     * @return  An unmodifiable {@code Set} of condition filters, allowing to configure 
     *          the filtering of conditions with expression data. If several 
     *          filters are configured, they are seen as "OR" conditions.
     */
    public Set<T> getConditionFilters() {
        return conditionFilters;
    }

    /**
     * @return  A {@code Set} of {@code Integer}s that are the IDs of species considered
     *          in this {@code DataFilter}. If {@code empty} it means that any species is targeted.
     */
    public Set<Integer> getSpeciesIdsConsidered() {
        return this.speciesIdsConsidered;
    }

    @Override
    public int hashCode() {
        return Objects.hash(conditionFilters, geneFilters, speciesIdsConsidered);
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DataFilter<?> other = (DataFilter<?>) obj;
        return Objects.equals(conditionFilters, other.conditionFilters)
                && Objects.equals(geneFilters, other.geneFilters)
                && Objects.equals(speciesIdsConsidered, other.speciesIdsConsidered);
    }
}
