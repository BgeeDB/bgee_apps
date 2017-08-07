package org.bgee.model.dao.api.expressiondata;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A filter to parameterize expression data queries. 
 * 
 * @author  Frederic Bastian
 * @author  Valentine Rech de Laval
 * @version Bgee 14, Mar. 2017
 * @since   Bgee 13, Oct. 2015
 */
public class CallDAOFilter {
    private final static Logger log = LogManager.getLogger(CallDAOFilter.class.getName());

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
    private final LinkedHashSet<DAOConditionFilter> conditionFilters;
    /**
     * @see #getDataFilters()
     */
    private final LinkedHashSet<CallDataDAOFilter> dataFilters;
    /**
     * @see #getCallObservedData()
     */
    private final Boolean callObservedData;
    /**
     * @see #getObservedDataFilter()
     */
    private final LinkedHashMap<ConditionDAO.Attribute, Boolean> observedDataFilter;
    
    /**
     * Constructor accepting all requested parameters. 
     * 
     * @param geneIds               A {@code Collection} of {@code Integer}s that are IDs of genes 
     *                              to filter expression queries. Can be {@code null} or empty.
     * @param speciesIds            A {@code Collection} of {@code Integer}s that are IDs of species 
     *                              to filter expression queries. Can be {@code null} or empty.
     * @param conditionFilters      A {@code Collection} of {@code ConditionFilter}s to configure 
     *                              the filtering of conditions with expression data. If several 
     *                              {@code ConditionFilter}s are provided, they are seen as "OR" conditions.
     *                              Can be {@code null} or empty.
     */
    public CallDAOFilter(Collection<Integer> geneIds, Collection<Integer> speciesIds, 
            Collection<DAOConditionFilter> conditionFilters, Collection<CallDataDAOFilter> dataFilters,
            Boolean callObservedData, Map<ConditionDAO.Attribute, Boolean> observedDataFilter)
                    throws IllegalArgumentException {
        log.entry(geneIds, speciesIds, conditionFilters, dataFilters, callObservedData, observedDataFilter);
        if (geneIds != null && geneIds.stream().anyMatch(id -> id == null)) {
            throw log.throwing(new IllegalArgumentException("No gene ID can be null"));
        }
        if (speciesIds != null && speciesIds.stream().anyMatch(id -> id == null)) {
            throw log.throwing(new IllegalArgumentException("No species ID can be null"));
        }
        if (conditionFilters != null && conditionFilters.stream().anyMatch(cf -> cf == null)) {
            throw log.throwing(new IllegalArgumentException("No DAOConditionFilter can be null"));
        }
        if (dataFilters != null && dataFilters.stream().anyMatch(df -> df == null)) {
            throw log.throwing(new IllegalArgumentException("No CallDataDAOFilter can be null"));
        }
        if (observedDataFilter != null && observedDataFilter.entrySet().stream()
                .anyMatch(e -> e.getKey() == null || e.getValue() == null)) {
            throw log.throwing(new IllegalArgumentException("No ObservedData Entry can have null key or value"));
        }

        this.geneIds = Collections.unmodifiableSet(
                geneIds == null? new HashSet<>(): new HashSet<>(geneIds));
        this.speciesIds = Collections.unmodifiableSet(
                speciesIds == null? new HashSet<>(): new HashSet<>(speciesIds));
        this.callObservedData = callObservedData;
        //we'll use defensive copying for this one, no unmodifiableLinkedHashSet method
        this.conditionFilters = conditionFilters == null? new LinkedHashSet<>(): 
            new LinkedHashSet<>(conditionFilters);
        this.dataFilters = dataFilters == null? new LinkedHashSet<>(): 
            new LinkedHashSet<>(dataFilters);
        this.observedDataFilter = observedDataFilter == null? new LinkedHashMap<>():
            new LinkedHashMap<>(observedDataFilter);
        
        if (this.getGeneIds().isEmpty() && this.getSpeciesIds().isEmpty() &&
                this.getConditionFilters().isEmpty()) {
            throw log.throwing(new IllegalArgumentException("No filters provided"));
        }
        
        log.exit();
    }

    /**
     * @return  An unmodifiable {@code Set} of {@code Integer}s containing the IDs of genes used
     *          to filter expression queries. Can be {@code null} or empty.
     */
    public Set<Integer> getGeneIds() {
        return geneIds;
    }
    /**
     * @return  An unmodifiable {@code Set} of {@code Integer}s containing the IDs of species used
     *          to filter expression queries. Can be {@code null} or empty.
     */
    public Set<Integer> getSpeciesIds() {
        return speciesIds;
    }
    /**
     * @return  A {@code LinkedHashSet} of {@code DAOConditionFilter}s to configure the filtering 
     *          of conditions with expression data. If several {@code ConditionFilter}s are provided, 
     *          they are seen as "OR" conditions. Can be {@code null} or empty. 
     *          Provided as a {@code LinkedHashSet} for convenience, to consistently set parameters 
     *          in queries.
     */
    public LinkedHashSet<DAOConditionFilter> getConditionFilters() {
        //defensive copying
        return new LinkedHashSet<>(conditionFilters);
    }
    /**
     * @return  A {@code LinkedHashSet} of {@code CallDataDAOFilter}s to configure the filtering
     *          of conditions with expression data. If several {@code CallDataDAOFilter}s are provided,
     *          they are seen as "OR" conditions. Can be {@code null} or empty.
     *          Provided as a {@code LinkedHashSet} for convenience, to consistently set parameters 
     *          in queries.
     */
    public LinkedHashSet<CallDataDAOFilter> getDataFilters() {
        //defensive copying
        return new LinkedHashSet<>(dataFilters);
    }
    /**
     * @return  A {@code Boolean} defining a filtering on whether the call was observed in the condition,
     *          if not {@code null}. This is independent from {@link #getObservedDataFilter()},
     *          because even if a data aggregation have produced only SELF propagation states,
     *          we cannot have the guarantee that data were actually observed in the condition
     *          by looking at these independent propagation states.
     */
    public Boolean getCallObservedData() {
        return callObservedData;
    }
    /**
     * @return  A {@code LinkedHashMap} where keys are {@code ConditionDAO.Attribute}s that are 
     *          condition parameters (see {@link ConditionDAO.Attribute#isConditionParameter()}),
     *          the associated value being a {@code Boolean} indicating whether the retrieved data
     *          should have been observed in the specified condition parameter.
     *          Provided as a {@code LinkedHashMap} for convenience, to consistently set parameters
     *          in queries.
     */
    public LinkedHashMap<ConditionDAO.Attribute, Boolean> getObservedDataFilter() {
        //defensive copying
        return new LinkedHashMap<>(observedDataFilter);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((conditionFilters == null) ? 0 : conditionFilters.hashCode());
        result = prime * result + ((dataFilters == null) ? 0 : dataFilters.hashCode());
        result = prime * result + ((geneIds == null) ? 0 : geneIds.hashCode());
        result = prime * result + ((callObservedData == null) ? 0 : callObservedData.hashCode());
        result = prime * result + ((observedDataFilter == null) ? 0 : observedDataFilter.hashCode());
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
        if (getClass() != obj.getClass()) {
            return false;
        }
        CallDAOFilter other = (CallDAOFilter) obj;
        if (conditionFilters == null) {
            if (other.conditionFilters != null) {
                return false;
            }
        } else if (!conditionFilters.equals(other.conditionFilters)) {
            return false;
        }
        if (dataFilters == null) {
            if (other.dataFilters != null) {
                return false;
            }
        } else if (!dataFilters.equals(other.dataFilters)) {
            return false;
        }
        if (geneIds == null) {
            if (other.geneIds != null) {
                return false;
            }
        } else if (!geneIds.equals(other.geneIds)) {
            return false;
        }
        if (callObservedData == null) {
            if (other.callObservedData != null) {
                return false;
            }
        } else if (!callObservedData.equals(other.callObservedData)) {
            return false;
        }
        if (observedDataFilter == null) {
            if (other.observedDataFilter != null) {
                return false;
            }
        } else if (!observedDataFilter.equals(other.observedDataFilter)) {
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

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("CallDAOFilter [geneIds=").append(geneIds)
               .append(", speciesIds=").append(speciesIds)
               .append(", conditionFilters=").append(conditionFilters)
               .append(", dataFilters=").append(dataFilters)
               .append(", callObservedData=").append(callObservedData)
               .append(", observedDataFilter=").append(observedDataFilter)
               .append("]");
        return builder.toString();
    }
}
