package org.bgee.model.expressiondata.rawdata;

import java.util.Collection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.expressiondata.BaseConditionFilter;

/**
 * A filter to parameterize queries using {@link RawDataCondition}s.
 * 
 * @author  Frederic Bastian
 * @version Bgee 14, Sept 2018
 * @since   Bgee 14, Sept 2018
 */
public class RawDataConditionFilter extends BaseConditionFilter<RawDataCondition> {
    private final static Logger log = LogManager.getLogger(RawDataConditionFilter.class.getName());

    //XXX: should we rather have a "includeSubstructures", "includeSubStages", ...?
    private final boolean includeSubConditions;
    /**
     * @param anatEntityIds        A {@code Collection} of {@code String}s that are the IDs 
     *                              of the anatomical entities that this {@code ConditionFilter} 
     *                              will specify to use.
     * @param devStageIds           A {@code Collection} of {@code String}s that are the IDs 
     *                              of the developmental stages that this {@code ConditionFilter} 
     *                              will specify to use.
     * @throws IllegalArgumentException If no anatomical entity IDs nor developmental stage IDs are provided. 
     */
    public RawDataConditionFilter(Collection<String> anatEntityIds, Collection<String> devStageIds,
            boolean includeSubConditions)
            throws IllegalArgumentException {
        super(anatEntityIds, devStageIds);
        if ((anatEntityIds == null || anatEntityIds.isEmpty()) && 
                (devStageIds == null || devStageIds.isEmpty())) {
            throw log.throwing(new IllegalArgumentException(
                    "Some anatatomical entity IDs or developmental stage IDs must be provided."));
        }
        this.includeSubConditions = includeSubConditions;
    }

    public boolean getIncludeSubConditions() {
        return this.includeSubConditions;
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (includeSubConditions ? 1231 : 1237);
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
        RawDataConditionFilter other = (RawDataConditionFilter) obj;
        if (includeSubConditions != other.includeSubConditions)
            return false;
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("RawDataConditionFilter [anatEntityIds=").append(getAnatEntityIds())
               .append(", devStageIds=").append(getDevStageIds())
               .append(", includeSubConditions=").append(includeSubConditions)
               .append("]");
        return builder.toString();
    }


    //Since we cannot use the attribute "includeSubConditions" to check for the validity of the RawDataCondition
    //provided to the 'test' method, we do not need to reimplement the 'test' method of BaseConditionFilter.
    //This might change in the future if other attributes are added.
}
