package org.bgee.model.dao.api.expressiondata.call;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.expressiondata.DAODataFilter;

/**
 * A filter to parameterize expression data queries. 
 * 
 * @author  Frederic Bastian
 * @author  Valentine Rech de Laval
 * @version Bgee 14, Jun. 2018
 * @since   Bgee 13, Oct. 2015
 */
public class CallDAOFilter extends DAODataFilter<DAOConditionFilter> {
    private final static Logger log = LogManager.getLogger(CallDAOFilter.class.getName());

    private final LinkedHashSet<CallObservedDataDAOFilter> callObservedDataFilters;
    private final LinkedHashSet<LinkedHashSet<DAOFDRPValueFilter>> pValueFilters;
    
    /**
     * Constructor accepting all requested parameters.
     * <p>
     * WARNING: provide a species ID only if it means "retrieve calls for ALL genes in that species".
     * If you are targeting some specific genes in a given species, by providing some gene IDs,
     * you must NOT also provide its corresponding species ID.
     * 
     * @param geneIds                   A {@code Collection} of {@code Integer}s that are IDs of genes 
     *                                  to filter expression queries. Can be {@code null} or empty.
     * @param speciesIds                A {@code Collection} of {@code Integer}s that are IDs of species 
     *                                  to filter expression queries. Can be {@code null} or empty.
     *                                  Only provide species IDs if you want to retrieve calls for all genes
     *                                  in that species. Do not provide species IDs corresponding to gene IDs
     *                                  provided in {@code geneIds}.
     * @param conditionFilters          A {@code Collection} of {@code ConditionFilter}s to configure 
     *                                  the filtering of conditions with expression data. If several 
     *                                  {@code ConditionFilter}s are provided, they are seen as "OR" conditions.
     *                                  Can be {@code null} or empty.
     * @param callObservedDataFilters   A {@code Collection} of {@code CallObservedDataDAOFilter}s
     *                                  allowing to filter expression calls based on data produced
     *                                  from each data type. If several {@code CallObservedDataDAOFilter}s
     *                                  are provided, they are seen as "OR" conditions.
     * @param pValueFilters             A {@code Collection} of {@code Set}s of {@code DAOFDRPValueFilter}s.
     *                                  The filters in an inner {@code Set} are seen as "AND" conditions.
     *                                  The {@code Set}s in the outer {@code Collection} are seen as
     *                                  "OR" conditions. If the outer {@code Collection} is non-null,
     *                                  none of the inner {@code Set}s can be {@code null}, empty,
     *                                  or to contain {@code null} elements.
     */
    public CallDAOFilter(Collection<Integer> geneIds, Collection<Integer> speciesIds, 
            Collection<DAOConditionFilter> conditionFilters,
            Collection<CallObservedDataDAOFilter> callObservedDataFilters,
            Collection<Set<DAOFDRPValueFilter>> pValueFilters)
                    throws IllegalArgumentException {
        super(geneIds, speciesIds, conditionFilters);
        log.traceEntry("{}, {}, {}, {}, {}", geneIds, speciesIds, conditionFilters,
                callObservedDataFilters, pValueFilters);

        if (callObservedDataFilters != null && callObservedDataFilters.contains(null)) {
            throw log.throwing(new IllegalArgumentException("No callObservedDataFilter can be null"));
        }
        if (pValueFilters != null && pValueFilters.stream()
                .anyMatch(s -> s == null || s.isEmpty() || s.contains(null))) {
            throw log.throwing(new IllegalArgumentException("No DAOFDRPValueFilter can be null"));
        }

        this.callObservedDataFilters = callObservedDataFilters == null? new LinkedHashSet<>():
            //For having a predictable iteration order and improve cache hit.
            //CallObservedDataDAOFilter implements Comparable.
            callObservedDataFilters.stream().sorted().collect(Collectors.toCollection(LinkedHashSet::new));
        this.pValueFilters = pValueFilters == null? new LinkedHashSet<>():
            //For having a predictable iteration order and improve cache hit.
            //DAOFDRPValueFilter implements Comparable.
            pValueFilters.stream()
            .map(s -> s.stream().sorted().collect(Collectors.toCollection(LinkedHashSet::new)))
            .sorted(new DAOFDRPValueFilter.DAOFDRPvalueFilterLinkedHashSetComparator())
            .collect(Collectors.toCollection(LinkedHashSet::new));
        
        if (this.getGeneIds().isEmpty() && this.getSpeciesIds().isEmpty() &&
                this.getConditionFilters().isEmpty()) {
            throw log.throwing(new IllegalArgumentException("No filters provided"));
        }
        
        log.traceExit();
    }

    /**
     * @return  A {@code LinkedHashSet} of {@code CallObservedDataDAOFilter}s to configure the filtering
     *          of conditions with expression data. If several {@code CallObservedDataDAOFilter}s are provided,
     *          they are seen as "OR" conditions. Can be {@code null} or empty. We allow to provide
     *          several {@code CallObservedDataDAOFilter}s to be able to filter in different ways for different data types.
     *          Provided as a {@code LinkedHashSet} for convenience, to consistently set parameters 
     *          in queries.
     */
    public LinkedHashSet<CallObservedDataDAOFilter> getCallObservedDataFilters() {
        //defensive copying, no unmodifiable LinkedHashSet
        return new LinkedHashSet<>(callObservedDataFilters);
    }
    public LinkedHashSet<LinkedHashSet<DAOFDRPValueFilter>> getFDRPValueFilters() {
        //defensive copying, no unmodifiable LinkedHashSet
        return pValueFilters.stream().map(s -> new LinkedHashSet<>(s))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((callObservedDataFilters == null) ? 0 : callObservedDataFilters.hashCode());
        result = prime * result + ((pValueFilters == null) ? 0 : pValueFilters.hashCode());
        return result;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (!(obj instanceof CallDAOFilter)) {
            return false;
        }
        CallDAOFilter other = (CallDAOFilter) obj;
        if (callObservedDataFilters == null) {
            if (other.callObservedDataFilters != null) {
                return false;
            }
        } else if (!callObservedDataFilters.equals(other.callObservedDataFilters)) {
            return false;
        }
        if (pValueFilters == null) {
            if (other.pValueFilters != null) {
                return false;
            }
        } else if (!pValueFilters.equals(other.pValueFilters)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("CallDAOFilter [geneIds=").append(getGeneIds())
               .append(", speciesIds=").append(getSpeciesIds())
               .append(", conditionFilters=").append(getConditionFilters())
               .append(", callObservedDataFilters=").append(callObservedDataFilters)
               .append(", pValueFilters=").append(pValueFilters)
               .append("]");
        return builder.toString();
    }
}
