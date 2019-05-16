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

//TODO: put back all attributes final
public abstract class MultiGeneExprAnalysis<T> {

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

    public MultiGeneExprAnalysis(Collection<String> requestedPublicGeneIds,
                                 Collection<String> requestedPublicGeneIdsNotFound,
                                 Collection<Gene> genes, Map<T, MultiGeneExprCounts> condToCounts) {
        this.requestedPublicGeneIds = Collections.unmodifiableSet(
                requestedPublicGeneIds == null? new HashSet<>(): new HashSet<>(requestedPublicGeneIds));
        this.requestedPublicGeneIdsNotFound = Collections.unmodifiableSet(
                requestedPublicGeneIdsNotFound == null? new HashSet<>(): new HashSet<>(requestedPublicGeneIdsNotFound));
        this.genes =  Collections.unmodifiableSet(genes == null? new HashSet<>(): new HashSet<>(genes));
        this.condToCounts = condToCounts;
    }

    private Set<String> requestedPublicGeneIds;
    private Set<String> requestedPublicGeneIdsNotFound;
    private Set<Gene> genes;

    private Map<T, MultiGeneExprCounts> condToCounts;

    public Set<String> getRequestedPublicGeneIds() {
        return requestedPublicGeneIds;
    }

    public Set<String> getRequestedPublicGeneIdsNotFound() {
        return requestedPublicGeneIdsNotFound;
    }

    public Set<Gene> getGenes() {
        return genes;
    }

    public Map<T, MultiGeneExprCounts> getCondToCounts() {
        return condToCounts;
    }
}
