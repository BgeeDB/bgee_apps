package org.bgee.model.expressiondata;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.bgee.model.expressiondata.baseelements.SummaryCallType;
import org.bgee.model.gene.Gene;

/**
 * A class allowing to store information about a comparison of expression between genes.
 * The genes can belong to a single species, in which case the class used, for retrieving
 * results of the gene expression comparison, is the extending class {@link SingleSpeciesExprAnalysis};
 * or they can belong to different species, in which case the extending class used is
 * {@link org.bgee.model.expressiondata.multispecies.MultiSpeciesExprAnalysis MultiSpeciesExprAnalysis}.
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

        public MultiGeneExprCounts(Map<SummaryCallType, Collection<Gene>> callTypeToGenes, Collection<Gene> genesWithNoData) {

            this.callTypeToGenes = Collections.unmodifiableMap(
                    callTypeToGenes == null? new HashMap<>():
                            callTypeToGenes.entrySet().stream()
                                    .collect(Collectors.toMap(
                                            e -> e.getKey(),
                                            e -> Collections.unmodifiableSet(new HashSet<>(e.getValue())))
                                    ));

            this.genesWithNoData = Collections.unmodifiableSet(
                    genesWithNoData == null? new HashSet<>(): new HashSet<>(genesWithNoData));
        }

        public Map<SummaryCallType, Set<Gene>> getCallTypeToGenes() {
            return callTypeToGenes;
        }

        public Set<Gene> getGenesWithNoData() {
            return genesWithNoData;
        }

        private Map<SummaryCallType, Set<Gene>> callTypeToGenes;
        private Set<Gene> genesWithNoData;
    }

    //***************************************
    // INSTANCE ATTRIBUTES AND METHODS
    //***************************************
    /**
     * @see #getRequestedPublicGeneIds()
     */
    private final Set<String> requestedPublicGeneIds;
    /**
     * @see #getRequestedPublicGeneIdsNotFound()
     */
    private final Set<String> requestedPublicGeneIdsNotFound;
    /**
     * @see #getGenes()
     */
    private final Set<Gene> genes;

    /**
     * @see #getCondToCounts()
     */
    private final Map<T, MultiGeneExprCounts> condToCounts;

    public MultiGeneExprAnalysis(Collection<String> requestedPublicGeneIds,
                                 Collection<String> requestedPublicGeneIdsNotFound,
                                 Collection<Gene> genes, Map<T, MultiGeneExprCounts> condToCounts) {
        this.requestedPublicGeneIds = Collections.unmodifiableSet(
                requestedPublicGeneIds == null? new HashSet<>(): new HashSet<>(requestedPublicGeneIds));
        this.requestedPublicGeneIdsNotFound = Collections.unmodifiableSet(
                requestedPublicGeneIdsNotFound == null? new HashSet<>(): new HashSet<>(requestedPublicGeneIdsNotFound));
        this.genes =  Collections.unmodifiableSet(genes == null? new HashSet<>(): new HashSet<>(genes));
        this.condToCounts = Collections.unmodifiableMap(
                condToCounts == null? new HashMap<>(): new HashMap<>(condToCounts));
    }

    /**
     * @return  An unmodifiable {@code Set} of {@code String}s that are the public IDs
     *          of genes requested for expression comparison. Genes that were retrieved
     *          can be retrieved by calling {@link #getGenes()}. IDs that did not correspond
     *          to genes in Bgee are listed by {@link #getRequestedPublicGeneIdsNotFound()}.
     * @see #getGenes()
     * @see #getRequestedPublicGeneIdsNotFound()
     */
    public Set<String> getRequestedPublicGeneIds() {
        return requestedPublicGeneIds;
    }
    /**
     * @return  An unmodifiable {@code Set} of {@code String}s that are the requested public gene IDs,
     *          among {@link #getRequestedPublicGeneIds()}, that were not found in Bgee.
     * @see #getRequestedPublicGeneIds()
     */
    public Set<String> getRequestedPublicGeneIdsNotFound() {
        return requestedPublicGeneIdsNotFound;
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
        result = prime * result + ((requestedPublicGeneIds == null) ? 0 : requestedPublicGeneIds.hashCode());
        result = prime * result
                + ((requestedPublicGeneIdsNotFound == null) ? 0 : requestedPublicGeneIdsNotFound.hashCode());
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
        if (requestedPublicGeneIds == null) {
            if (other.requestedPublicGeneIds != null) {
                return false;
            }
        } else if (!requestedPublicGeneIds.equals(other.requestedPublicGeneIds)) {
            return false;
        }
        if (requestedPublicGeneIdsNotFound == null) {
            if (other.requestedPublicGeneIdsNotFound != null) {
                return false;
            }
        } else if (!requestedPublicGeneIdsNotFound.equals(other.requestedPublicGeneIdsNotFound)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[requestedPublicGeneIds=").append(requestedPublicGeneIds)
                .append(", requestedPublicGeneIdsNotFound=").append(requestedPublicGeneIdsNotFound)
                .append(", genes=").append(genes)
                .append(", condToCounts=").append(condToCounts)
                .append("]");
        return builder.toString();
    }
}