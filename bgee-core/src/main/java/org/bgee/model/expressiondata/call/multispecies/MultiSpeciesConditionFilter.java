package org.bgee.model.expressiondata.call.multispecies;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A filter to parameterize queries using expression data conditions. 
 * 
 * @author  Julien Wollbrett
 * @author  Frederic Bastian
 * @version Bgee 15.0, Apr. 2021
 * @since   Bgee 14, Mar. 2017
 */

public class MultiSpeciesConditionFilter implements Predicate<MultiSpeciesCondition> {
	 private final static Logger log = LogManager.getLogger(MultiSpeciesCondition.class.getName());

	/**
     * @see #getAnatEntityIds()
     */
    private final Set<String> anatEntityIds;
    /**
     * @see #getDevStageIds()
     */
    private final Set<String> devStageIds;
    /**
     * @see #getCellTypeIds()
     */
    private final Set<String> cellTypeIds;
    /**
     * @see #getSexIds()
     */
    private final Set<String> sexIds;
    
    //XXX Do we have to Enum on ECO evidence for similarity relations?
    private final Set<String> ecoIds;
    
    //XXX CIOId. Do we have to Enum on CIO confidence level?
    private final Set<String> cioIds;

    /**
     * @param anatEntityIds        A {@code Collection} of {@code String}s that are the IDs 
     *                              of the anatomical entities that this {@code MultiSpeciesConditionFilter} 
     *                              will specify to use.
     * @param devStageIds           A {@code Collection} of {@code String}s that are the IDs 
     *                              of the developmental stages that this {@code MultiSpeciesConditionFilter} 
     *                              will specify to use.
     * @param cellTypeIds           A {@code Collection} of {@code String}s that are the IDs 
     *                              of the anatomical entities describing cell types that this 
     *                              {@code MultiSpeciesConditionFilter} will specify to use.
     * @param sexIds                A {@code Collection} of {@code String}s that are the names 
     *                              of the sexes that this {@code MultiSpeciesConditionFilter}
     *                              will specify to use.
     * @throws IllegalArgumentException If no anatomical entity IDs nor developmental stage IDs
     *                                  nor cell type IDs nor sex IDs are provided. 
     */
    public MultiSpeciesConditionFilter(Collection<String> anatEntityIds, Collection<String> devStageIds,
            Collection<String> cellTypeIds, Collection<String> sexIds,
            Collection<String> ecoIds, Collection<String> cioIds) throws IllegalArgumentException {
    	if ((anatEntityIds == null || anatEntityIds.isEmpty()) && 
                (devStageIds == null || devStageIds.isEmpty()) && 
                (cellTypeIds == null || cellTypeIds.isEmpty()) && 
                (sexIds == null || sexIds.isEmpty())) {
            throw log.throwing(new IllegalArgumentException("Some condition parameters must be provided."));
        }
        this.anatEntityIds = Collections.unmodifiableSet(anatEntityIds == null ? 
                new HashSet<>(): new HashSet<>(anatEntityIds));
        this.devStageIds = Collections.unmodifiableSet(devStageIds == null? 
                new HashSet<>(): new HashSet<>(devStageIds));
        this.cellTypeIds = Collections.unmodifiableSet(cellTypeIds == null? 
                new HashSet<>(): new HashSet<>(cellTypeIds));
        this.sexIds = Collections.unmodifiableSet(sexIds == null? 
                new HashSet<>(): new HashSet<>(sexIds));
        this.ecoIds = Collections.unmodifiableSet(ecoIds == null ? 
                new HashSet<>(): new HashSet<>(ecoIds));
        this.cioIds = Collections.unmodifiableSet(cioIds == null ? 
                new HashSet<>(): new HashSet<>(cioIds));
	}

    /**
     * @return  An unmodifiable {@code Set} of {@code String}s that are the IDs 
     *          of the anatomical entities that this {@code ConditionFilter} will specify to use.
     */
    public Set<String> getAnatEntityIds() {
        return anatEntityIds;
    }
    /**
     * @return  An unmodifiable {@code Set} of {@code String}s that are the IDs 
     *          of the developmental stages that this {@code MultiSpeciesConditionFilter} will specify to use.
     */
    public Set<String> getDevStageIds() {
        return devStageIds;
    }
    /**
     * @return  An unmodifiable {@code Set} of {@code String}s that are the IDs 
     *          of the cell type that this {@code MultiSpeciesConditionFilter} will specify to use.
     */
    public Set<String> getCellTypeIds() {
        return cellTypeIds;
    }
    /**
     * @return  An unmodifiable {@code Set} of {@code String}s that are the IDs 
     *          of the sexes that this {@code MultiSpeciesConditionFilter} will specify to use.
     */
    public Set<String> getSexIds() {
        return sexIds;
    }

    /**
     * @return  An unmodifiable {@code Set} of {@code String}s that are the IDs 
     *          of the confidence level that this {@code MultiSpeciesConditionFilter} will specify to use.
     */
    public Set<String> getCioIds() {
        return cioIds;
    }
    /**
     * @return  An unmodifiable {@code Set} of {@code String}s that are the IDs 
     *          of the similarity relations that this {@code ConditionFilter} will specify to use.
     */
    public Set<String> getEcoIds() {
        return ecoIds;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((anatEntityIds == null) ? 0 : anatEntityIds.hashCode());
        result = prime * result + ((devStageIds == null) ? 0 : devStageIds.hashCode());
        result = prime * result + ((cellTypeIds == null) ? 0 : cellTypeIds.hashCode());
        result = prime * result + ((sexIds == null) ? 0 : sexIds.hashCode());
        result = prime * result + ((cioIds == null) ? 0 : cioIds.hashCode());
        result = prime * result + ((ecoIds == null) ? 0 : ecoIds.hashCode());
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
        MultiSpeciesConditionFilter other = (MultiSpeciesConditionFilter) obj;
        if (anatEntityIds == null) {
            if (other.anatEntityIds != null) {
                return false;
            }
        } else if (!anatEntityIds.equals(other.anatEntityIds)) {
            return false;
        }
        if (devStageIds == null) {
            if (other.devStageIds != null) {
                return false;
            }
        } else if (!devStageIds.equals(other.devStageIds)) {
            return false;
        }
        if (cellTypeIds == null) {
            if (other.cellTypeIds != null) {
                return false;
            }
        } else if (!cellTypeIds.equals(other.cellTypeIds)) {
            return false;
        }
        if (sexIds == null) {
            if (other.sexIds != null) {
                return false;
            }
        } else if (!sexIds.equals(other.sexIds)) {
            return false;
        }
        if (cioIds == null) {
            if (other.cioIds != null) {
                return false;
            }
        } else if (!cioIds.equals(other.cioIds)) {
            return false;
        }
        if (ecoIds == null) {
            if (other.ecoIds != null) {
                return false;
            }
        } else if (!ecoIds.equals(other.ecoIds)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("MultiSpeciesConditionFilter [anatEntityIds=").append(anatEntityIds)
               .append(", devStageIds=").append(devStageIds)
               .append(", cellTypeIds=").append(cellTypeIds)
               .append(", sexIds=").append(sexIds)
               .append(", ecoIds=").append(ecoIds)
               .append(", cioIds=").append(cioIds).append("]");
        return builder.toString();
    }

    @Override
	public boolean test(MultiSpeciesCondition t) {
		// TODO
		throw new UnsupportedOperationException();
	}
}