package org.bgee.model.expressiondata.call;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.bgee.model.expressiondata.baseelements.ExpressionLevelInfo;
import org.bgee.model.expressiondata.baseelements.SummaryCallType.ExpressionSummary;
import org.bgee.model.gene.Gene;

/**
 * A class allowing to store information about a comparison of expression between genes.
 * The genes can belong to a single species, in which case the class used, for retrieving
 * results of the gene expression comparison, is the extending class {@link SingleSpeciesExprAnalysis};
 * or they can belong to different species, in which case the extending class used is
 * {@link org.bgee.model.expressiondata.call.multispecies.MultiSpeciesExprAnalysis MultiSpeciesExprAnalysis}.
 * {@code SingleSpeciesExprAnalysis} returns the comparisons using {@code Condition}s,
 * while {@code MultiSpeciesExprAnalysis} returns the comparison using {@code MultiSpeciesCondition}s.
 *
 * @author Frederic Bastian
 * @version Bgee 14 May 2019
 * @since Bgee 14 May 2019
 *
 * @param <T>   The type of conditions used to return the gene expression comparison.
 *              Specified by inheriting classes.
 */
public abstract class MultiGeneExprAnalysis<T> {

    //***************************************
    // INNER CLASSES AND STATIC ATTRIBUTES
    //***************************************
    public static final class MultiGeneExprCounts {

        private final Map<ExpressionSummary, Set<Gene>> callTypeToGenes;
        private final Set<Gene> genesWithNoData;
        private final Map<Gene, ExpressionLevelInfo> geneToExprLevelInfo;

        public MultiGeneExprCounts(Map<ExpressionSummary, Collection<Gene>> callTypeToGenes,
                Collection<Gene> genesWithNoData, Map<Gene, ExpressionLevelInfo> geneToExprLevelInfo) {

            this.callTypeToGenes = Collections.unmodifiableMap(
                    callTypeToGenes == null? new HashMap<>():
                            callTypeToGenes.entrySet().stream()
                                    .collect(Collectors.toMap(
                                            e -> e.getKey(),
                                            e -> Collections.unmodifiableSet(new HashSet<>(e.getValue())))
                                    ));

            this.genesWithNoData = Collections.unmodifiableSet(
                    genesWithNoData == null? new HashSet<>(): new HashSet<>(genesWithNoData));
            this.geneToExprLevelInfo = Collections.unmodifiableMap(
                    geneToExprLevelInfo == null? new HashMap<>(): new HashMap<>(geneToExprLevelInfo));
        }

        public Map<ExpressionSummary, Set<Gene>> getCallTypeToGenes() {
            return callTypeToGenes;
        }
        public Set<Gene> getGenesWithNoData() {
            return genesWithNoData;
        }
        /**
         * @return  An unmodifiable {@code Map} where keys are {@code Gene}s with data, the associated value
         *          being a {@code ExpressionLevelInfo} containing the gene min rank in the condition, if any
         *          (can be {@code null} if no rank available for the gene).
         */
        public Map<Gene, ExpressionLevelInfo> getGeneToExprLevelInfo() {
            return geneToExprLevelInfo;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((callTypeToGenes == null) ? 0 : callTypeToGenes.hashCode());
            result = prime * result + ((genesWithNoData == null) ? 0 : genesWithNoData.hashCode());
            result = prime * result + ((geneToExprLevelInfo == null) ? 0 : geneToExprLevelInfo.hashCode());
            return result;
        }
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            MultiGeneExprCounts other = (MultiGeneExprCounts) obj;
            if (callTypeToGenes == null) {
                if (other.callTypeToGenes != null) {
                    return false;
                }
            } else if (!callTypeToGenes.equals(other.callTypeToGenes)) {
                return false;
            }
            if (genesWithNoData == null) {
                if (other.genesWithNoData != null) {
                    return false;
                }
            } else if (!genesWithNoData.equals(other.genesWithNoData)) {
                return false;
            }
            if (geneToExprLevelInfo == null) {
                if (other.geneToExprLevelInfo != null) {
                    return false;
                }
            } else if (!geneToExprLevelInfo.equals(other.geneToExprLevelInfo)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("MultiGeneExprCounts [callTypeToGenes=").append(callTypeToGenes)
                   .append(", genesWithNoData=") .append(genesWithNoData)
                   .append(", geneToExprLevelInfo=") .append(geneToExprLevelInfo)
                   .append("]");
            return builder.toString();
        }
    }

    //***************************************
    // INSTANCE ATTRIBUTES AND METHODS
    //***************************************
    /**
     * @see #getGenes()
     */
    private final Set<Gene> genes;

    /**
     * @see #getCondToCounts()
     */
    private final Map<T, MultiGeneExprCounts> condToCounts;

    public MultiGeneExprAnalysis(Collection<Gene> genes, Map<T, MultiGeneExprCounts> condToCounts) {
        this.genes =  Collections.unmodifiableSet(genes == null? new HashSet<>(): new HashSet<>(genes));
        this.condToCounts = Collections.unmodifiableMap(
                condToCounts == null? new HashMap<>(): new HashMap<>(condToCounts));
    }

    /**
     * @return  An unmodifiable {@code Set} of {@code Gene}s that are the genes that were retrieved
     *          based on the requested public gene IDs (see {@link #getRequestedPublicGeneIds()}).
     *          IDs that could not be found are listed in {@link #getRequestedPublicGeneIdsNotFound()}.
     * @see #getRequestedPublicGeneIds()
     * @see #getRequestedPublicGeneIdsNotFound()
     */
    public Set<Gene> getGenes() {
        return genes;
    }
    /**
     * @return  An unmodifiable {@code Map} where keys are {@code T}s, the associated value being
     *          a {@code MultiGeneExprCounts} allowing to compare expression in the condition.
     */
    public Map<T, MultiGeneExprCounts> getCondToCounts() {
        return condToCounts;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((condToCounts == null) ? 0 : condToCounts.hashCode());
        result = prime * result + ((genes == null) ? 0 : genes.hashCode());
        return result;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        MultiGeneExprAnalysis<?> other = (MultiGeneExprAnalysis<?>) obj;
        if (condToCounts == null) {
            if (other.condToCounts != null) {
                return false;
            }
        } else if (!condToCounts.equals(other.condToCounts)) {
            return false;
        }
        if (genes == null) {
            if (other.genes != null) {
                return false;
            }
        } else if (!genes.equals(other.genes)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[genes=").append(genes)
               .append(", condToCounts=").append(condToCounts)
               .append("]");
        return builder.toString();
    }
}