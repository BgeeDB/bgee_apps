package org.bgee.model.dao.api.expressiondata.rawdata;

import java.util.Collection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.expressiondata.DAOBaseConditionFilter;

/**
 * A filter to parameterize queries for raw data.
 *
 * @author Frederic Bastian
 * @version Bgee 14 Sept. 2018
 * @since Bgee 14 Sept. 2018
 */
//TODO: TOComparator and related test
public class DAORawDataConditionFilter extends DAOBaseConditionFilter {
    private final static Logger log = LogManager.getLogger(DAORawDataConditionFilter.class.getName());

    private final boolean includeSubConditions;
    /**
     * @param anatEntityIds        A {@code Collection} of {@code String}s that are the IDs 
     *                              of the anatomical entities to use.
     * @param devStageIds           A {@code Collection} of {@code String}s that are the IDs 
     *                              of the developmental stages to use.
     * @throws IllegalArgumentException If no anatomical entity IDs nor developmental stage IDs are provided. 
     */
    public DAORawDataConditionFilter(Collection<String> anatEntityIds, Collection<String> devStageIds,
            boolean includeSubConditions) {
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
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        DAORawDataConditionFilter other = (DAORawDataConditionFilter) obj;
        if (includeSubConditions != other.includeSubConditions) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("DAORawDataConditionFilter [anatEntityIds=").append(getAnatEntityIds())
               .append(", devStageIds=").append(getDevStageIds())
               .append(", includeSubConditions=").append(includeSubConditions)
               .append("]");
        return builder.toString();
    }
}
