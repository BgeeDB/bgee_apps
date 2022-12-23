package org.bgee.model.dao.api.expressiondata.call;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.expressiondata.DAODataFilter2;

public class DAOCallFilter extends DAODataFilter2 {
    private final static Logger log = LogManager.getLogger(DAOCallFilter.class.getName());

    private final LinkedHashSet<CallObservedDataDAOFilter2> callObservedDataFilters;
    private final LinkedHashSet<LinkedHashSet<DAOFDRPValueFilter2>> pValueFilters;

    public DAOCallFilter(Collection<Integer> speciesIds, Collection<Integer> geneIds,
            Collection<Integer> conditionIds,
            Collection<CallObservedDataDAOFilter2> callObservedDataFilters,
            Collection<Set<DAOFDRPValueFilter2>> pValueFilters) throws IllegalArgumentException {
        super(speciesIds, geneIds, conditionIds);
        log.traceEntry("{}, {}, {}, {}, {}", geneIds, speciesIds, conditionIds,
                callObservedDataFilters, pValueFilters);

        if (callObservedDataFilters != null &&
                //We cannot call contains(null) on Collections refusing null values
                callObservedDataFilters.stream().anyMatch(e -> e == null)) {
            throw log.throwing(new IllegalArgumentException("No callObservedDataFilter can be null"));
        }
        if (pValueFilters != null && pValueFilters.stream()
                .anyMatch(s -> s == null || s.isEmpty() || s.stream().anyMatch(e -> e == null))) {
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
            .sorted(new DAOFDRPValueFilterBase.DAOFDRPvalueFilterLinkedHashSetComparator2())
            .collect(Collectors.toCollection(LinkedHashSet::new));
        log.traceExit();
    }

    /**
     * @return  A {@code LinkedHashSet} of {@code CallObservedDataDAOFilter}s to configure
     *          the filtering of conditions with expression data. If several
     *          {@code CallObservedDataDAOFilter}s are provided, they are seen as "OR" conditions.
     *          Can be {@code null} or empty. We allow to provide several
     *          {@code CallObservedDataDAOFilter}s to be able to filter in different ways
     *          for different data types. Provided as a {@code LinkedHashSet} for convenience,
     *          to consistently set parameters in queries.
     */
    public LinkedHashSet<CallObservedDataDAOFilter2> getCallObservedDataFilters() {
        //defensive copying, no unmodifiable LinkedHashSet
        return new LinkedHashSet<>(callObservedDataFilters);
    }
    public LinkedHashSet<LinkedHashSet<DAOFDRPValueFilter2>> getFDRPValueFilters() {
        //defensive copying, no unmodifiable LinkedHashSet
        return pValueFilters.stream().map(s -> new LinkedHashSet<>(s))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Objects.hash(callObservedDataFilters, pValueFilters);
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
        DAOCallFilter other = (DAOCallFilter) obj;
        return Objects.equals(callObservedDataFilters, other.callObservedDataFilters)
                && Objects.equals(pValueFilters, other.pValueFilters);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("DAOCallFilter [")
                .append("getSpeciesIds()=").append(getSpeciesIds())
                .append(", getGeneIds()=").append(getGeneIds())
                .append(", getConditionIds()=").append(getConditionIds())
                .append(", callObservedDataFilters=").append(callObservedDataFilters)
                .append(", pValueFilters=").append(pValueFilters)
                .append("]");
        return builder.toString();
    }
}
