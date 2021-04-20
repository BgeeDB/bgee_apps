package org.bgee.model.dao.api.expressiondata;

import java.util.Collection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A filter to parameterize queries using expression data conditions. 
 * 
 * @author Frederic Bastian
 * @version Bgee 14 Sept. 2018
 * @since Bgee 13 Oct. 2015
 */
public class DAOConditionFilter extends DAOBaseConditionFilter {
    private final static Logger log = LogManager.getLogger(DAOConditionFilter.class.getName());

    /**
     * @see #getObservedConditions()
     */
    private final Boolean observedConditions;
    
    /**
     * @param anatEntityIds        A {@code Collection} of {@code String}s that are the IDs 
     *                              of the anatomical entities that this {@code DAOConditionFilter} 
     *                              will specify to use.
     * @param devStageIds           A {@code Collection} of {@code String}s that are the IDs 
     *                              of the developmental stages that this {@code DAOConditionFilter} 
     *                              will specify to use.
     * @param cellTypeIds           A {@code Collection} of {@code String}s that are the IDs 
     *                              of the cell types that this {@code DAOConditionFilter} 
     *                              will specify to use.
     * @param sexIds                A {@code Collection} of {@code String}s that are the IDs 
     *                              of the sexes that this {@code DAOConditionFilter} 
     *                              will specify to use.
     * @param strainIds             A {@code Collection} of {@code String}s that are the IDs 
     *                              of the strains that this {@code DAOConditionFilter} 
     *                              will specify to use.
     * @param observedConditions    A {@code Boolean} defining whether the conditions considered
     *                              should have been observed in expression data in any species.
     *                              See {@link #getObservedConditions()} for more details.
     * @throws IllegalArgumentException If no anatomical entity IDs and no developmental stage IDs 
     *                                  are provided. 
     */
    public DAOConditionFilter(Collection<String> anatEntitieIds, Collection<String> devStageIds,
            Collection<String> cellTypeIds, Collection<String> sexIds, Collection<String> strainIds,
            Boolean observedConditions) throws IllegalArgumentException {
        super(anatEntitieIds, devStageIds, cellTypeIds, sexIds, strainIds);
        if ((anatEntitieIds == null || anatEntitieIds.isEmpty()) && 
                (devStageIds == null || devStageIds.isEmpty()) &&
                (cellTypeIds == null || cellTypeIds.isEmpty()) &&
                (sexIds == null || sexIds.isEmpty()) &&
                (strainIds == null || strainIds.isEmpty()) &&
                observedConditions == null) {
            throw log.throwing(new IllegalArgumentException("Some anatatomical entity IDs, "
                    + "developmental stage IDs, cell type IDs, sex IDs, strain IDs or observed "
                    + "data status must be provided."));
        }
        this.observedConditions = observedConditions;
    }

    /**
     * @return  A {@code Boolean} defining whether the conditions considered should have been
     *          observed in expression data. If {@code true}, only conditions
     *          observed in expression data are considered, not resulting
     *          only from a data propagation; if {@code false}, only conditions resulting
     *          from data propagation, never observed in expression data,
     *          are considered; if {@code null}, conditions are considered whatever
     *          their observed data status.
     */
    public Boolean getObservedConditions() {
        return observedConditions;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((observedConditions == null) ? 0 : observedConditions.hashCode());
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
        if (getClass() != obj.getClass()) {
            return false;
        }
        DAOConditionFilter other = (DAOConditionFilter) obj;
        if (observedConditions == null) {
            if (other.observedConditions != null) {
                return false;
            }
        } else if (!observedConditions.equals(other.observedConditions)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("DAOConditionFilter [anatEntityIds=").append(getAnatEntityIds())
               .append(", devStageIds=").append(getDevStageIds())
               .append(", cellTypeIds=").append(getCellTypeIds())
               .append(", sexIds=").append(getSexIds())
               .append(", strainIds=").append(getStrainIds())
               .append(", observedConditions=").append(observedConditions).append("]");
        return builder.toString();
    }
}
