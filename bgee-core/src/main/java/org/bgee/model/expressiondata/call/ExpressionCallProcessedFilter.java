package org.bgee.model.expressiondata.call;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.bgee.model.dao.api.expressiondata.call.DAOCallFilter;
import org.bgee.model.dao.api.expressiondata.call.ConditionDAO.ConditionRankInfoTO;
import org.bgee.model.expressiondata.ProcessedFilter;
import org.bgee.model.expressiondata.call.CallFilter.ExpressionCallFilter2;
import org.bgee.model.gene.Gene;
import org.bgee.model.gene.GeneBioType;
import org.bgee.model.gene.GeneFilter;
import org.bgee.model.source.Source;
import org.bgee.model.species.Species;

/**
 * This class stores information obtained from the processing of an an {@link ExpressionCallFilter2}.
 * It is provided because processing an {@link ExpressionCallFilter2} can be computer-intensive.
 * When calling the different methods of an {@code ExpressionCallLoader} object, we don't want
 * to re-process this information at each call.
 * <p>
 * This class has been created to store this information outside of an {@link ExpressionCallLoader},
 * because a {@code ExpressionCallLoader} is a {@code Service}, and holds a connection to a data source.
 * If we wanted to store this pre-processed information to, for instance, be reused by different threads,
 * storing it in an {@code ExpressionCallLoader} could maintain the connection open.
 * <p>
 * An {@code ExpressionCallProcessedFilter} can be obtained either by calling
 * {@link ExpressionCallService#processExpressionCallFilter(ExpressionCallFilter2)},
 * or by calling {@link ExpressionCallLoader#getProcessedFilter()} from
 * an already-existing {@code ExpressionCallLoader}.
 * <p>
 * See {@link ProcessedFilter} for additional details about the structure of the processed information,
 * and {@link ExpressionCallService#processExpressionCallFilter(ExpressionCallFilter2,
 * ExpressionCallProcessedFilterGeneSpeciesPart, ExpressionCallProcessedFilterConditionPart,
 * ExpressionCallProcessedFilterInvariablePart)} for a method allowing to use the different parts
 * of the processed information.
 *
 * @author Frederic Bastian
 * @version Bgee 15.1 May 2024
 * @since Bgee 15.0, Nov. 2022
 * @see #getSourceFilter()
 * @see ExpressionCallLoader#getProcessedFilter()
 * @see ExpressionCallService#getCallLoader(ExpressionCallProcessedFilter)
 * @see ExpressionCallService#processExpressionCallFilter(ExpressionCallFilter2)
 * @see ExpressionCallService#loadCallLoader(ExpressionCallFilter2)
 */
//Most methods and constructors are protected, so that only the {@link ExpressionCallService}
//can instantiate this class, and only {@link ExpressionCallLoader} use it.
public class ExpressionCallProcessedFilter extends ProcessedFilter<ExpressionCallFilter2,
DAOCallFilter, Condition2, ConditionFilter2> {

    //We redeclare these classes notably for making their constructor visible to the package
    public static class ExpressionCallProcessedFilterGeneSpeciesPart extends ProcessedFilterGeneSpeciesPart {
        ExpressionCallProcessedFilterGeneSpeciesPart(Collection<GeneFilter> geneFilters,
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
    public static class ExpressionCallProcessedFilterConditionPart
    extends ProcessedFilterConditionPart<ConditionFilter2, Condition2> {
        ExpressionCallProcessedFilterConditionPart(Collection<ConditionFilter2> conditionFilters,
                Map<Integer, Condition2> requestedConditionMap) {
            super(conditionFilters, requestedConditionMap);
        }

        @Override
        protected Map<Integer, Condition2> getRequestedConditionMap() {
            return super.getRequestedConditionMap();
        }
    }
    public static class ExpressionCallProcessedFilterInvariablePart extends ProcessedFilterInvariablePart {

        private final Map<Integer, ConditionRankInfoTO> maxRankPerSpecies;

        ExpressionCallProcessedFilterInvariablePart(Map<Integer, GeneBioType> geneBioTypeMap,
                Map<Integer, Source> sourceMap, Map<Integer, ConditionRankInfoTO> maxRankPerSpecies) {
            super(geneBioTypeMap, sourceMap);
            this.maxRankPerSpecies = Collections.unmodifiableMap(maxRankPerSpecies == null?
                    new HashMap<>(): new HashMap<>(maxRankPerSpecies));
        }

        protected Map<Integer, ConditionRankInfoTO> getMaxRankPerSpecies() {
            return maxRankPerSpecies;
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
            final int prime = 31;
            int result = super.hashCode();
            result = prime * result + Objects.hash(maxRankPerSpecies);
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
            ExpressionCallProcessedFilterInvariablePart other = (ExpressionCallProcessedFilterInvariablePart) obj;
            return Objects.equals(maxRankPerSpecies, other.maxRankPerSpecies);
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("ExpressionCallProcessedFilterInvariablePart [")
                   .append("maxRankPerSpecies=").append(maxRankPerSpecies)
                   .append(", getGeneBioTypeMap()=").append(getGeneBioTypeMap())
                   .append(", getSourceMap()=").append(getSourceMap())
                   .append("]");
            return builder.toString();
        }
    }

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

    ExpressionCallProcessedFilter(ExpressionCallFilter2 sourceFilter,
            Collection<DAOCallFilter> daoFilters,
            ExpressionCallProcessedFilterGeneSpeciesPart geneSpeciesPart,
            ExpressionCallProcessedFilterConditionPart conditionPart,
            ExpressionCallProcessedFilterInvariablePart invariablePart,
            boolean useGlobalRank, BigDecimal exprScoreMinValue, BigDecimal exprScoreMaxValue,
            BigDecimal presentLowThreshold, BigDecimal presentHighThreshold,
            BigDecimal absentLowThreshold, BigDecimal absentHighThreshold) {
        super(sourceFilter, daoFilters, geneSpeciesPart, conditionPart, invariablePart);
        this.useGlobalRank = useGlobalRank;
        this.exprScoreMinValue = exprScoreMinValue;
        this.exprScoreMaxValue = exprScoreMaxValue;
        this.presentLowThreshold = presentLowThreshold;
        this.presentHighThreshold = presentHighThreshold;
        this.absentLowThreshold = absentLowThreshold;
        this.absentHighThreshold = absentHighThreshold;
    }

    @Override
    public ExpressionCallProcessedFilterGeneSpeciesPart getGeneSpeciesPart() {
        return (ExpressionCallProcessedFilterGeneSpeciesPart) super.getGeneSpeciesPart();
    }
    @Override
    public ExpressionCallProcessedFilterConditionPart getConditionPart() {
        return (ExpressionCallProcessedFilterConditionPart) super.getConditionPart();
    }
    @Override
    public ExpressionCallProcessedFilterInvariablePart getInvariablePart() {
        return (ExpressionCallProcessedFilterInvariablePart) super.getInvariablePart();
    }

    //We override the methods to make them visible to ExpressionCallLoader in the same package
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
        return this.getInvariablePart().getMaxRankPerSpecies();
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
        result = prime * result + Objects.hash(absentHighThreshold, absentLowThreshold,
                exprScoreMaxValue, exprScoreMinValue,
                presentHighThreshold, presentLowThreshold, useGlobalRank);
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
               .append(", getGeneSpeciesPart()=").append(getGeneSpeciesPart())
               .append(", getConditionPart()=").append(getConditionPart())
               .append(", getInvariablePart()=").append(getInvariablePart())
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