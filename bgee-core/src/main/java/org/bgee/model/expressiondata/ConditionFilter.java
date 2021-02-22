package org.bgee.model.expressiondata;

import java.util.Collection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A filter to parameterize queries using expression data {@link Condition}s.
 * 
 * @author  Frederic Bastian
 * @author  Valentine Rech de Laval
 * @version Bgee 14, Sept 2018
 * @since   Bgee 13, Oct. 2015
 */
//TODO: be able to EXCLUDE anat. entities/stages. It would be convenient to discard
//non-informative anat. entities.
public class ConditionFilter extends BaseConditionFilter<Condition> {
    private final static Logger log = LogManager.getLogger(ConditionFilter.class.getName());

    /**
     * @see #getObservedConditions()
     */
    private final Boolean observedConditions;

    /**
     * @param anatEntityIds        A {@code Collection} of {@code String}s that are the IDs 
     *                              of the anatomical entities that this {@code ConditionFilter} 
     *                              will specify to use.
     * @param devStageIds           A {@code Collection} of {@code String}s that are the IDs 
     *                              of the developmental stages that this {@code ConditionFilter} 
     *                              will specify to use.
     * @throws IllegalArgumentException If no anatomical entity IDs nor developmental stage IDs
     *                                  are provided. 
     */
    public ConditionFilter(Collection<String> anatEntityIds, Collection<String> devStageIds)
            throws IllegalArgumentException {
        this(anatEntityIds, devStageIds, null);
    }
    /**
     * @param anatEntityIds        A {@code Collection} of {@code String}s that are the IDs 
     *                              of the anatomical entities that this {@code ConditionFilter} 
     *                              will specify to use.
     * @param devStageIds           A {@code Collection} of {@code String}s that are the IDs 
     *                              of the developmental stages that this {@code ConditionFilter} 
     *                              will specify to use.
     * @param observedConditions    A {@code Boolean} defining whether the conditions considered
     *                              should have been observed in expression data in any species.
     *                              See {@link #getObservedConditions()} for more details.
     * @throws IllegalArgumentException If no anatomical entity IDs nor developmental stage IDs
     *                                  nor observed status are provided. 
     */
    //XXX: Should we add two booleans to ask for considering sub-structures and sub-stages?
    //Because it seems it can be managed through query of data propagation in CallFilter
    public ConditionFilter(Collection<String> anatEntityIds, Collection<String> devStageIds,
            Boolean observedConditions) throws IllegalArgumentException {
        super(anatEntityIds, devStageIds);
        if ((anatEntityIds == null || anatEntityIds.isEmpty()) &&
                (devStageIds == null || devStageIds.isEmpty()) &&
                observedConditions == null) {
            throw log.throwing(new IllegalArgumentException("Some anatatomical entity IDs"
                + " or developmental stage IDs or observed data status must be provided."));
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
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        ConditionFilter other = (ConditionFilter) obj;
        if (observedConditions == null) {
            if (other.observedConditions != null)
                return false;
        } else if (!observedConditions.equals(other.observedConditions))
            return false;
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ConditionFilter [anatEntityIds=").append(getAnatEntityIds())
               .append(", devStageIds=").append(getDevStageIds())
               .append(", observedConditions=").append(observedConditions).append("]");
        return builder.toString();
    }


    //Since we cannot use the attribute "observedConditions" to check for the validity of the Condition
    //provided to the 'test' method, we do not need to reimplement the 'test' method of BaseConditionFilter.
    //This might change in the future if other attributes are added.
}
