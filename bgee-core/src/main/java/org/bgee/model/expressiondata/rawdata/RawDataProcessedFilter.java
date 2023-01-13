package org.bgee.model.expressiondata.rawdata;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.bgee.model.dao.api.expressiondata.rawdata.DAORawDataFilter;
import org.bgee.model.expressiondata.ProcessedFilter;
import org.bgee.model.expressiondata.rawdata.baseelements.RawDataCondition;
import org.bgee.model.gene.Gene;
import org.bgee.model.gene.GeneBioType;
import org.bgee.model.source.Source;
import org.bgee.model.species.Species;

/**
 * Class holding pre-processed information generated from a {@link RawDataFilter},
 * in order not to re-process this information when obtaining different {@link RawDataLoader}s
 * for the same parameters.
 * <p>
 * This class has been created to store this information outside of a {@link RawDataLoader},
 * because a {@code RawDataLoader} is a {@code Service}, and holds a connection to a data source.
 * If we wanted to store this pre-processed information to be reused by different threads,
 * storing it in a {@code RawDataLoader} could maintain the connection open.
 *
 * @author Frederic Bastian
 * @version Bgee 15.0, Nov. 2022
 * @since Bgee 15.0, Nov. 2022
 * @see #getRawDataFilter()
 * @see RawDataLoader#getRawDataProcessedFilter()
 * @see RawDataService#getRawDataLoader(RawDataProcessedFilter)
 * @see RawDataService#loadRawDataLoader(RawDataFilter)
 */
public class RawDataProcessedFilter extends ProcessedFilter<RawDataFilter,
DAORawDataFilter, RawDataCondition> {
    RawDataProcessedFilter(RawDataFilter sourceFilter,
            Collection<DAORawDataFilter> daoFilters, Map<Integer, Gene> requestedGeneMap,
            Map<Integer, RawDataCondition> requestedConditionMap,
            Map<Integer, Species> speciesMap, Map<Integer, GeneBioType> geneBioTypeMap,
            Map<Integer, Source> sourceMap) {
        super(sourceFilter, daoFilters, requestedGeneMap, requestedConditionMap,
                speciesMap, geneBioTypeMap, sourceMap);
    }

    //We override the methods to make them visible to RawDataLoader in the same package
    @Override
    protected Set<DAORawDataFilter> getDaoFilters() {
        return super.getDaoFilters();
    }
    @Override
    protected Map<Integer, Gene> getRequestedGeneMap() {
        return super.getRequestedGeneMap();
    }
    @Override
    protected Map<Integer, RawDataCondition> getRequestedConditionMap() {
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
        builder.append("RawDataProcessedFilter [")
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