package org.bgee.model.expressiondata.call;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.bgee.model.dao.api.expressiondata.call.DAOCallFilter;
import org.bgee.model.expressiondata.ProcessedFilter;
import org.bgee.model.expressiondata.call.CallFilter.ExpressionCallFilter2;
import org.bgee.model.gene.Gene;
import org.bgee.model.gene.GeneBioType;
import org.bgee.model.source.Source;
import org.bgee.model.species.Species;

//Most methods and constructors are protected, so that only the {@link ExpressionCallService}
//can instantiate this class, and only {@link ExpressionCallLoader} use it.
public class ExpressionCallProcessedFilter extends ProcessedFilter<ExpressionCallFilter2,
DAOCallFilter, Condition> {
    ExpressionCallProcessedFilter(ExpressionCallFilter2 sourceFilter,
            Collection<DAOCallFilter> daoFilters, Map<Integer, Gene> requestedGeneMap,
            Map<Integer, Condition> requestedConditionMap,
            Map<Integer, Species> speciesMap, Map<Integer, GeneBioType> geneBioTypeMap,
            Map<Integer, Source> sourceMap) {
        super(sourceFilter, daoFilters, requestedGeneMap, requestedConditionMap,
                speciesMap, geneBioTypeMap, sourceMap);
    }

    @Override
    protected Set<DAOCallFilter> getDaoFilters() {
        return super.getDaoFilters();
    }
    @Override
    protected Map<Integer, Gene> getRequestedGeneMap() {
        return super.getRequestedGeneMap();
    }
    @Override
    protected Map<Integer, Condition> getRequestedConditionMap() {
        return super.getRequestedConditionMap();
    }
    @Override
    protected Map<Integer, Species> getSpeciesMap() {
        return super.getSpeciesMap();
    }
    @Override
    protected Map<Integer, GeneBioType> getGeneBioTypeMap() {
        return super.getGeneBioTypeMap();
    }
    @Override
    protected Map<Integer, Source> getSourceMap() {
        return super.getSourceMap();
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ExpressionCallProcessedFilter [")
               .append("getSourceFilter()=").append(getSourceFilter())
               .append(", getDaoFilters()=").append(getDaoFilters())
               .append(", getRequestedGeneMap()=").append(getRequestedGeneMap())
               .append(", getRequestedConditionMap()=").append(getRequestedConditionMap())
               .append(", getSpeciesMap()=").append(getSpeciesMap())
               .append(", getGeneBioTypeMap()=").append(getGeneBioTypeMap())
               .append(", getSourceMap()=").append(getSourceMap())
               .append("]");
        return builder.toString();
    }
}
