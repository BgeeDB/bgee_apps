package org.bgee.model.expressiondata.call;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.bgee.model.dao.api.expressiondata.call.DAOCallFilter;
import org.bgee.model.dao.api.expressiondata.call.ConditionDAO.ConditionRankInfoTO;
import org.bgee.model.expressiondata.ProcessedFilter;
import org.bgee.model.expressiondata.call.CallFilter.ExpressionCallFilter2;
import org.bgee.model.gene.Gene;
import org.bgee.model.gene.GeneBioType;
import org.bgee.model.source.Source;
import org.bgee.model.species.Species;

//Most methods and constructors are protected, so that only the {@link ExpressionCallService}
//can instantiate this class, and only {@link ExpressionCallLoader} use it.
public class ExpressionCallProcessedFilter extends ProcessedFilter<ExpressionCallFilter2,
DAOCallFilter, Condition2> {

    private final boolean useGlobalRank;
    private final BigDecimal exprScoreMinValue;
    private final BigDecimal exprScoreMaxValue;/**
     * A {@code BigDecimal} that is the value a FDR-corrected p-value must be less than or equal to
     * for PRESENT LOW QUALITY.
     */
    private final BigDecimal presentLowThreshold;
    /**
     * A {@code BigDecimal} that is the value a FDR-corrected p-value must be less than or equal to
     * for PRESENT HIGH QUALITY.
     */
    private final BigDecimal presentHighThreshold;
    /**
     * A {@code BigDecimal} that is the value a FDR-corrected p-value must be greater than
     * for ABSENT HIGH QUALITY.
     */
    private final BigDecimal absentLowThreshold;
    /**
     * A {@code BigDecimal} that is the value a FDR-corrected p-value must be greater than
     * for ABSENT LOW QUALITY.
     */
    private final BigDecimal absentHighThreshold;
    private final Map<Integer, ConditionRankInfoTO> maxRankPerSpecies;

    ExpressionCallProcessedFilter(ExpressionCallFilter2 sourceFilter,
            Collection<DAOCallFilter> daoFilters, Map<Integer, Gene> requestedGeneMap,
            Map<Integer, Condition2> requestedConditionMap,
            Map<Integer, Species> speciesMap, Map<Integer, GeneBioType> geneBioTypeMap,
            Map<Integer, Source> sourceMap, Map<Integer, ConditionRankInfoTO> maxRankPerSpecies,
            boolean useGlobalRank, BigDecimal exprScoreMinValue, BigDecimal exprScoreMaxValue,
            BigDecimal presentLowThreshold, BigDecimal presentHighThreshold,
            BigDecimal absentLowThreshold, BigDecimal absentHighThreshold) {
        super(sourceFilter, daoFilters, requestedGeneMap, requestedConditionMap,
                speciesMap, geneBioTypeMap, sourceMap);
        this.maxRankPerSpecies = maxRankPerSpecies;
        this.useGlobalRank = useGlobalRank;
        this.exprScoreMinValue = exprScoreMinValue;
        this.exprScoreMaxValue = exprScoreMaxValue;
        this.presentLowThreshold = presentLowThreshold;
        this.presentHighThreshold = presentHighThreshold;
        this.absentLowThreshold = absentLowThreshold;
        this.absentHighThreshold = absentHighThreshold;
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
    protected Map<Integer, Condition2> getRequestedConditionMap() {
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
    protected Map<Integer, ConditionRankInfoTO> getMaxRankPerSpecies() {
        return maxRankPerSpecies;
    }
    protected boolean isUseGlobalRank() {
        return useGlobalRank;
    }
    protected BigDecimal getExprScoreMinValue() {
        return exprScoreMinValue;
    }
    protected BigDecimal getExprScoreMaxValue() {
        return exprScoreMaxValue;
    }
    public BigDecimal getPresentLowThreshold() {
        return presentLowThreshold;
    }
    public BigDecimal getPresentHighThreshold() {
        return presentHighThreshold;
    }
    public BigDecimal getAbsentLowThreshold() {
        return absentLowThreshold;
    }
    public BigDecimal getAbsentHighThreshold() {
        return absentHighThreshold;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Objects.hash(absentHighThreshold, absentLowThreshold, exprScoreMaxValue,
                exprScoreMinValue, maxRankPerSpecies, presentHighThreshold, presentLowThreshold, useGlobalRank);
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
        ExpressionCallProcessedFilter other = (ExpressionCallProcessedFilter) obj;
        return Objects.equals(absentHighThreshold, other.absentHighThreshold)
                && Objects.equals(absentLowThreshold, other.absentLowThreshold)
                && Objects.equals(exprScoreMaxValue, other.exprScoreMaxValue)
                && Objects.equals(exprScoreMinValue, other.exprScoreMinValue)
                && Objects.equals(maxRankPerSpecies, other.maxRankPerSpecies)
                && Objects.equals(presentHighThreshold, other.presentHighThreshold)
                && Objects.equals(presentLowThreshold, other.presentLowThreshold)
                && useGlobalRank == other.useGlobalRank;
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
               .append(", maxRankPerSpecies=").append(maxRankPerSpecies)
               .append(", useGlobalRank=").append(useGlobalRank)
               .append(", exprScoreMinValue=").append(exprScoreMinValue)
               .append(", exprScoreMaxValue=").append(exprScoreMaxValue)
               .append(", presentLowThreshold=").append(presentLowThreshold)
               .append(", presentHighThreshold=").append(presentHighThreshold)
               .append(", absentLowThreshold=").append(absentLowThreshold)
               .append(", absentHighThreshold=").append(absentHighThreshold)
               .append("]");
        return builder.toString();
    }
}
