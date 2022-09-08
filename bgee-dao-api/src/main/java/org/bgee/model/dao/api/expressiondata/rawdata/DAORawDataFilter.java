package org.bgee.model.dao.api.expressiondata.rawdata;

import java.util.Collection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.expressiondata.DAODataFilter;

public class DAORawDataFilter extends DAODataFilter<DAORawDataConditionFilter> {
    private final static Logger log = LogManager.getLogger(DAORawDataFilter.class.getName());

    //TODO remove speciesIds, it has to 
    public DAORawDataFilter(Collection<Integer> geneIds, Collection<Integer> speciesIds,
            Collection<DAORawDataConditionFilter> conditionFilters) {
        super(geneIds, speciesIds, conditionFilters);

        if (this.getGeneIds().isEmpty() && this.getSpeciesIds().isEmpty() &&
                this.getConditionFilters().isEmpty()) {
            throw log.throwing(new IllegalArgumentException("No filters provided"));
        }
    }

    public DAORawDataFilter(DAORawDataFilter rawDataFilter) {
        this(rawDataFilter.getGeneIds(), rawDataFilter.getSpeciesIds(),
                rawDataFilter.getConditionFilters());
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
