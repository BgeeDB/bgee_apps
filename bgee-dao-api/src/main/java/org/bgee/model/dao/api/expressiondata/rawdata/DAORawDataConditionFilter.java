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

    /**
     * @see {@link #getIncludeSubConditions()}
     */
    private final boolean includeSubConditions;
    /**
     * @see {@link #getIncludeParentConditions()}
     */
    private final boolean includeParentConditions;

    /**
     * @param anatEntityIds             A {@code Collection} of {@code String}s that are the IDs 
     *                                  of the anatomical entities to use.
     * @param devStageIds               A {@code Collection} of {@code String}s that are the IDs 
     *                                  of the developmental stages to use.
     * @param cellTypeIds               A {@code Collection} of {@code String}s that are the IDs 
     *                                  of the cell types to use.
     * @param sexIds                    A {@code Collection} of {@code String}s that are the IDs 
     *                                  of the sexes to use.
     * @param strainIds                 A {@code Collection} of {@code String}s that are the IDs 
     *                                  of the strains to use.
     * @param includeSubConditions      A {@code boolean} defining whether the sub-conditions
     *                                  of the targeted raw conditions, from which calls of presence
     *                                  of expression are propagated, should be retrieved.
     * @param includeParentConditions   A {@code boolean} defining whether the parent conditions
     *                                  of the targeted raw conditions, from which calls of absence
     *                                  of expression are propagated, should be retrieved.
     * @throws IllegalArgumentException If no anatomical entity IDs nor developmental stage IDs are provided. 
     */
    public DAORawDataConditionFilter(Collection<String> anatEntityIds, Collection<String> devStageIds,
            Collection<String> cellTypeIds, Collection<String>  sexIds, Collection<String> strainIds, 
            boolean includeSubConditions, boolean includeParentConditions) {
        super(anatEntityIds, devStageIds, cellTypeIds, sexIds, strainIds);
        if ((anatEntityIds == null || anatEntityIds.isEmpty()) && 
                (devStageIds == null || devStageIds.isEmpty()) &&
                (cellTypeIds == null || cellTypeIds.isEmpty()) &&
                (sexIds == null || sexIds.isEmpty()) &&
                (strainIds == null || strainIds.isEmpty())) {
            throw log.throwing(new IllegalArgumentException(
                    "Some anatatomical entity IDs, developmental stage IDs, cell type IDs, sex IDs "
                    + "or strain IDs must be provided."));
        }
        this.includeSubConditions = includeSubConditions;
        this.includeParentConditions = includeParentConditions;
    }

    /**
     * @return  A {@code boolean} defining whether the sub-conditions of the targeted raw conditions,
     *          from which calls of presence of expression are propagated, should be retrieved.
     */
    public boolean getIncludeSubConditions() {
        return this.includeSubConditions;
    }
    /**
     * @return  A {@code boolean} defining whether the parent conditions of the targeted raw conditions,
     *          from which calls of absence of expression are propagated, should be retrieved.
     */
    public boolean getIncludeParentConditions() {
        return this.includeParentConditions;
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (includeParentConditions ? 1231 : 1237);
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
        if (includeParentConditions != other.includeParentConditions) {
            return false;
        }
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
               .append(", cellTypeIds=").append(getCellTypeIds())
               .append(", sexIds=").append(getSexIds())
               .append(", strainIds=").append(getStrainIds())
               .append(", includeSubConditions=").append(includeSubConditions)
               .append(", includeParentConditions=").append(includeParentConditions)
               .append("]");
        return builder.toString();
    }
}
