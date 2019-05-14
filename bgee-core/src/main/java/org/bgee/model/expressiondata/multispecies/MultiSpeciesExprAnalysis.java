package org.bgee.model.expressiondata.multispecies;

import org.bgee.model.expressiondata.MultiGeneExprAnalysis;
import org.bgee.model.gene.Gene;

import java.util.Collection;
import java.util.Map;

public class MultiSpeciesExprAnalysis extends MultiGeneExprAnalysis<MultiSpeciesCondition> {

    public MultiSpeciesExprAnalysis(Collection<String> requestedPublicGeneIds,
                                    Collection<String> requestedPublicGeneIdsNotFound,
                                    Collection<Gene> genes,
                                    Map<MultiSpeciesCondition, MultiGeneExprCounts> condToCounts) {
        super(requestedPublicGeneIds, requestedPublicGeneIdsNotFound, genes, condToCounts);
    }
}
