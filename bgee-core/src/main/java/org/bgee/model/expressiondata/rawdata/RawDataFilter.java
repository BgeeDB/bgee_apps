package org.bgee.model.expressiondata.rawdata;

import java.util.Collection;
import java.util.Collections;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.expressiondata.DataFilter;
import org.bgee.model.gene.GeneFilter;

public class RawDataFilter extends DataFilter<RawDataConditionFilter> {
    private final static Logger log = LogManager.getLogger(RawDataFilter.class.getName());

    public RawDataFilter(GeneFilter geneFilter, RawDataConditionFilter condFilter) {
        this(Collections.singleton(geneFilter), Collections.singleton(condFilter));
    }

    public RawDataFilter(Collection<GeneFilter> geneFilters, Collection<RawDataConditionFilter> conditionFilters) {
        super(geneFilters, conditionFilters);
        if (geneFilters == null) {
            throw log.throwing(new IllegalArgumentException("A GeneFilter must be provided"));
        }
    }

    //As long as there is no more attributes than in the class DataFilter, we rely on
    //the equals/hashCode method of the DataFilter superclass.

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("RawDataFilter [geneFilters=").append(getGeneFilters())
               .append(", conditionFilters=").append(getConditionFilters())
               .append("]");
        return builder.toString();
    }
}
