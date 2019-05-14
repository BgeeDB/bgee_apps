package org.bgee.model.expressiondata;

import java.util.Map;
import java.util.Set;

import org.bgee.model.expressiondata.baseelements.SummaryCallType;
import org.bgee.model.expressiondata.multispecies.MultiSpeciesCondition;
import org.bgee.model.gene.Gene;

//TODO: put back all attributes final
public abstract class MultiGeneExprAnalysis<T> {

    public static final class MultiGeneExprCounts {
        private Map<SummaryCallType, Set<Gene>> callTypeToGenes;
        private Set<Gene> genesWithNoData;
    }
    private Set<String> requestedPublicGeneIds;
    private Set<String> requestedPublicGeneIdsNotFound;
    private Set<Gene> genes;

    private Map<T, MultiGeneExprCounts> condToCounts;
}
