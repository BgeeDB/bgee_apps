package org.bgee.model.dao.api.expressiondata;

import java.util.Collection;
import java.util.LinkedHashSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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

    /**
     * @see #getDataFilters()
     */
    private final LinkedHashSet<CallDataDAOFilter> dataFilters;
    
    /**
     * Constructor accepting all requested parameters.
     * <p>
     * WARNING: provide a species ID only if it means "retrieve calls for ALL genes in that species".
     * If you are targeting some specific genes in a given species, by providing some gene IDs,
     * you must NOT also provide its corresponding species ID.
     * 
     * @param geneIds               A {@code Collection} of {@code Integer}s that are IDs of genes 
     *                              to filter expression queries. Can be {@code null} or empty.
     * @param speciesIds            A {@code Collection} of {@code Integer}s that are IDs of species 
     *                              to filter expression queries. Can be {@code null} or empty.
     *                              Only provide species IDs if you want to retrieve calls for all genes
     *                              in that species. Do not provide species IDs corresponding to gene IDs
     *                              provided in {@code geneIds}.
     * @param conditionFilters      A {@code Collection} of {@code ConditionFilter}s to configure 
     *                              the filtering of conditions with expression data. If several 
     *                              {@code ConditionFilter}s are provided, they are seen as "OR" conditions.
     *                              Can be {@code null} or empty.
     * @param dataFilters           A {@code Collection} of {@code CallDataDAOFilter}s allowing to filter
     *                              expression calls based on data produced from each data type.
     *                              If several {@code CallDataDAOFilter}s are provided, they are seen as
     *                              "OR" conditions.
     */
    public CallDAOFilter(Collection<Integer> geneIds, Collection<Integer> speciesIds, 
            Collection<DAOConditionFilter> conditionFilters, Collection<CallDataDAOFilter> dataFilters)
                    throws IllegalArgumentException {
        super(geneIds, speciesIds, conditionFilters);
        log.entry(geneIds, speciesIds, conditionFilters, dataFilters);

        if (dataFilters != null && dataFilters.stream().anyMatch(df -> df == null)) {
            throw log.throwing(new IllegalArgumentException("No CallDataDAOFilter can be null"));
        }

        this.dataFilters = dataFilters == null? new LinkedHashSet<>(): 
            new LinkedHashSet<>(dataFilters);
        
        if (this.getGeneIds().isEmpty() && this.getSpeciesIds().isEmpty() &&
                this.getConditionFilters().isEmpty()) {
            throw log.throwing(new IllegalArgumentException("No filters provided"));
        }
        
        log.exit();
    }

    /**
     * @return  A {@code LinkedHashSet} of {@code CallDataDAOFilter}s to configure the filtering
     *          of conditions with expression data. If several {@code CallDataDAOFilter}s are provided,
     *          they are seen as "OR" conditions. Can be {@code null} or empty. We allow to provide
     *          several {@code CallDataDAOFilter}s to be able to filter in different ways for different data types.
     *          Provided as a {@code LinkedHashSet} for convenience, to consistently set parameters 
     *          in queries.
     */
    public LinkedHashSet<CallDataDAOFilter> getDataFilters() {
        //defensive copying, no unmodifiable LinkedHashSet
        return new LinkedHashSet<>(dataFilters);
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((dataFilters == null) ? 0 : dataFilters.hashCode());
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
        if (dataFilters == null) {
            if (other.dataFilters != null) {
                return false;
            }
        } else if (!dataFilters.equals(other.dataFilters)) {
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
               .append(", dataFilters=").append(dataFilters)
               .append("]");
        return builder.toString();
    }
}
