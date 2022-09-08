package org.bgee.model.expressiondata.rawdata;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.expressiondata.DataFilter;
import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.model.gene.GeneFilter;

//XXX: We decided the entry point will always be a conditionFilter i.e filter on propagated conditions.
//     This conditionFitler will then be used to retrieve associated raw conditions using the
//     globalCondToCond . But what about user querying directly the annotation interface to retrieve
//     all annotated experiment containing data coming from brain in human (without substructure)? 
//     How to filter for such queries? We could add columns "conditionRelationOriginConditionParameter"
//     e.g conditionRelationOriginAnatEntity in the globalCondToCond table for each condition parameter.
//     The second question is what happen if we annotated experiments not used to generate globalCalls? 
//     Don't we also want to retrieve them?
//     We should maybe propose both possibilities. Use a global condition filter OR a raw condition filter
//     and check that not both are not null.
//     the global cond filter will be used to go from propagated calls to raw data and the raw cond filter
//     will be used to query the annotation directly. We could even add a boolean allowing to define if we only
//     want to retrieve annotation part of a call (and then use the globalCondToCond to retrieve raw data)
public class RawDataFilter extends DataFilter<RawDataConditionFilter> {
    private final static Logger log = LogManager.getLogger(RawDataFilter.class.getName());

    private final Set<DataType> dataTypes;

    public RawDataFilter(GeneFilter geneFilter, RawDataConditionFilter condFilter, DataType dataTypeFilter) {
        this(Collections.singleton(geneFilter), Collections.singleton(condFilter), EnumSet.of(dataTypeFilter));
    }

    public RawDataFilter(Collection<GeneFilter> geneFilters, Collection<RawDataConditionFilter> conditionFilters, Collection<DataType> dataTypes) {
        super(geneFilters, conditionFilters);
        if (dataTypes == null || dataTypes.isEmpty()) {
            this.dataTypes = EnumSet.allOf(DataType.class);
        } else {
            this.dataTypes = new HashSet<DataType>(dataTypes);
        }
        if (geneFilters == null) {
            throw log.throwing(new IllegalArgumentException("A GeneFilter must be provided"));
        }
    }

    public Set<DataType> getDataTypes() {
        return dataTypes;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Objects.hash(dataTypes);
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
        RawDataFilter other = (RawDataFilter) obj;
        return Objects.equals(dataTypes, other.dataTypes);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("RawDataFilter [geneFilters=").append(getGeneFilters())
               .append(", conditionFilters=").append(getConditionFilters())
               .append("]");
        return builder.toString();
    }
}
