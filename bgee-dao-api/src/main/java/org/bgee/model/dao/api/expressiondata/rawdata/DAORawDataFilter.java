package org.bgee.model.dao.api.expressiondata.rawdata;

import java.util.Collection;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.expressiondata.DAODataFilter;

public class DAORawDataFilter {
    private final static Logger log = LogManager.getLogger(DAORawDataFilter.class.getName());

    // Here there is a speciesId only if there is no other filtering
    // on Bgee gene IDs or raw data condition IDs
    private final Integer speciesId;
    private final Set<Integer> geneIds;
    private final Set<Integer> rawDataCondIds;

    private final Set<String> experimentIds;
    private final Set<String> assayIds;
    //TODO: clear javadoc in constructor and getter
    private final boolean exprIdsAssayIdsIntersect;

    public DAORawDataFilter(int speciesId) {

    }
    public DAORawDataFilter(int speciesId, Collection<String> experimentIds,
            Collection<String> assayIds, boolean exprIdsAssayIdsIntersect) {

    }
    public DAORawDataFilter(Collection<Integer> geneIds, Collection<Integer> rawDataCondIds) {

    }
    public DAORawDataFilter(Collection<Integer> geneIds, Collection<Integer> rawDataCondIds,
            Collection<String> experimentIds, Collection<String> assayIds,
            boolean exprIdsAssayIdsIntersect) {

    }
    private DAORawDataFilter(int speciesId, Collection<Integer> geneIds, Collection<Integer> rawDataCondIds,
            Collection<String> experimentIds, Collection<String> assayIds,
            boolean exprIdsAssayIdsIntersect) {
        //TODO: sanity check speciesId > 0
        // OR
        // TODO: sanity check collections not both null or empty,
        // and that they contains no null elements
    }

    //since we have no attributes but the one in DAODataFilter, we do not implement equals/hashCode for now.

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("DAORawDataFilter [geneIds()=").append(getGeneIds())
               .append(", speciesIds()=").append(getSpeciesIds())
               .append(", conditionFilters()=").append(getConditionFilters()).append("]");
        return builder.toString();
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
