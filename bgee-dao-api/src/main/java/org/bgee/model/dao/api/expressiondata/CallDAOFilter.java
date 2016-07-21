package org.bgee.model.dao.api.expressiondata;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A filter to parameterize expression data queries. 
 * 
 * @author Frederic Bastian
 * @version Bgee 13 Oct. 2015
 * @since Bgee 13 Oct. 2015
 */
public class CallDAOFilter {
    private final static Logger log = LogManager.getLogger(CallDAOFilter.class.getName());

    /**
     * @see #getGeneIds()
     */
    private final Set<String> geneIds;
    /**
     * @see #getSpeciesIds()
     */
    private final Set<String> speciesIds;
    /**
     * @see #getConditionFilters()
     */
    private final LinkedHashSet<DAOConditionFilter> conditionFilters;
    
    /**
     * Constructor accepting all requested parameters. 
     * 
     * @param geneIds           A {@code Collection} of {@code String}s that are IDs of genes 
     *                          to filter expression queries. Can be {@code null} or empty.
     * @param speciesIds        A {@code Collection} of {@code String}s that are IDs of species 
     *                          to filter expression queries. Can be {@code null} or empty.
     * @param conditionFilters  A {@code Collection} of {@code ConditionFilter}s to configure 
     *                          the filtering of conditions with expression data. If several 
     *                          {@code ConditionFilter}s are provided, they are seen as "OR" conditions.
     *                          Can be {@code null} or empty.
     * @param attributeType     The class type of the {@code Attribute}s of type {@code T}.
     */
    public CallDAOFilter(Collection<String> geneIds, Collection<String> speciesIds, 
            Collection<DAOConditionFilter> conditionFilters) 
                    throws IllegalArgumentException {
        log.entry(geneIds, speciesIds, conditionFilters);

        this.geneIds = Collections.unmodifiableSet(
                geneIds == null? new HashSet<>(): new HashSet<>(geneIds));
        this.speciesIds = Collections.unmodifiableSet(
                speciesIds == null? new HashSet<>(): new HashSet<>(speciesIds));
        //we'll use defensive copying for those ones, no unmodifiableLinkedHashSet method
        this.conditionFilters = conditionFilters == null? new LinkedHashSet<>(): 
            new LinkedHashSet<>(
                conditionFilters.stream().filter(filter -> filter != null).collect(Collectors.toSet())
            );
        
        log.exit();
    }

    /**
     * @return  An unmodifiable {@code Set} of {@code String}s containing the IDs of genes used
     *          to filter expression queries. Can be {@code null} or empty.
     */
    public Set<String> getGeneIds() {
        return geneIds;
    }
    /**
     * @return  An unmodifiable {@code Set} of {@code String}s containing the IDs of species used
     *          to filter expression queries. Can be {@code null} or empty.
     */
    public Set<String> getSpeciesIds() {
        return speciesIds;
    }
    /**
     * @return  A {@code LinkedHashSet} of {@code ConditionFilter}s to configure the filtering 
     *          of conditions with expression data. If several {@code ConditionFilter}s are provided, 
     *          they are seen as "OR" conditions. Can be {@code null} or empty. 
     *          Provided as a {@code LinkedHashSet} for convenience, to consistently set parameters 
     *          in queries.
     */
    public LinkedHashSet<DAOConditionFilter> getConditionFilters() {
        //defensive copying
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

    @Override
    public String toString() {
        return "CallDAOFilter [geneIds=" + geneIds 
                + ", speciesIds=" + speciesIds 
                + ", conditionFilters=" + conditionFilters + "]";
    }
}
