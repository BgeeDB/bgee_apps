package org.bgee.model.expressiondata.rawdata;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.bgee.model.dao.api.expressiondata.rawdata.DAORawDataFilter;
import org.bgee.model.expressiondata.ProcessedFilter;
import org.bgee.model.expressiondata.rawdata.baseelements.RawDataCondition;
import org.bgee.model.gene.Gene;
import org.bgee.model.gene.GeneBioType;
import org.bgee.model.gene.GeneFilter;
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
 * <p>
 * A {@code RawDataProcessedFilter} can be obtained either by calling
 * {@link RawDataService#processRawDataFilter(RawDataFilter)},
 * or by calling {@link RawDataLoader#getRawDataProcessedFilter()} from
 * an already-existing {@code RawDataLoader}.
 * <p>
 * See {@link ProcessedFilter} for additional details about the structure of the processed information.
 *
 * @author Frederic Bastian
 * @version Bgee 15.1 May 2024
 * @since Bgee 15.0, Nov. 2022
 * @see #getSourceFilter()
 * @see RawDataLoader#getRawDataProcessedFilter()
 * @see RawDataService#getRawDataLoader(RawDataProcessedFilter)
 * @see RawDataService#processRawDataFilter(RawDataFilter)
 * @see RawDataService#loadRawDataLoader(RawDataFilter)
 */
//Most methods and constructors are protected, so that only the {@link RawDataService}
//can instantiate this class, and only {@link RawDataLoader} use it.
public class RawDataProcessedFilter extends ProcessedFilter<RawDataFilter,
DAORawDataFilter, RawDataCondition, RawDataConditionFilter> {

    //We redeclare these classes notably for making their constructor visible to the package
    public static class RawDataProcessedFilterGeneSpeciesPart extends ProcessedFilterGeneSpeciesPart {
        RawDataProcessedFilterGeneSpeciesPart(Collection<GeneFilter> geneFilters,
                Map<Integer, Gene> requestedGeneMap, Map<Integer, Species> speciesMap) {
            super(geneFilters, requestedGeneMap, speciesMap);
        }
        @Override
        protected Map<Integer, Gene> getRequestedGeneMap() {
            return super.getRequestedGeneMap();
        }
        @Override
        protected Map<Integer, Species> getSpeciesMap() {
            return super.getSpeciesMap();
        }
    }
    public static class RawDataProcessedFilterConditionPart
    extends ProcessedFilterConditionPart<RawDataConditionFilter, RawDataCondition> {
        RawDataProcessedFilterConditionPart(Collection<RawDataConditionFilter> conditionFilters,
                Map<Integer, RawDataCondition> requestedConditionMap) {
            super(conditionFilters, requestedConditionMap);
        }
        @Override
        protected Map<Integer, RawDataCondition> getRequestedConditionMap() {
            return super.getRequestedConditionMap();
        }
    }
    public static class RawDataProcessedFilterInvariablePart extends ProcessedFilterInvariablePart {
        RawDataProcessedFilterInvariablePart(Map<Integer, GeneBioType> geneBioTypeMap,
                Map<Integer, Source> sourceMap) {
            super(geneBioTypeMap, sourceMap);
        }
        @Override
        protected Map<Integer, GeneBioType> getGeneBioTypeMap() {
            return super.getGeneBioTypeMap();
        }
        @Override
        protected Map<Integer, Source> getSourceMap() {
            return super.getSourceMap();
        }
    }

    RawDataProcessedFilter(RawDataFilter sourceFilter,
            Collection<DAORawDataFilter> daoFilters,
            ProcessedFilterGeneSpeciesPart geneSpeciesPart,
            RawDataProcessedFilterConditionPart conditionPart,
            ProcessedFilterInvariablePart invariablePart) {
        super(sourceFilter, daoFilters, geneSpeciesPart, conditionPart, invariablePart);
    }

    @Override
    public RawDataProcessedFilterGeneSpeciesPart getGeneSpeciesPart() {
        return (RawDataProcessedFilterGeneSpeciesPart) super.getGeneSpeciesPart();
    }
    @Override
    public RawDataProcessedFilterConditionPart getConditionPart() {
        return (RawDataProcessedFilterConditionPart) super.getConditionPart();
    }
    @Override
    public RawDataProcessedFilterInvariablePart getInvariablePart() {
        return (RawDataProcessedFilterInvariablePart) super.getInvariablePart();
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
               .append(", getGeneSpeciesPart()=").append(getGeneSpeciesPart())
               .append(", getConditionPart()=").append(getConditionPart())
               .append(", getInvariablePart()=").append(getInvariablePart())
               .append("]");
        return builder.toString();
    }
}