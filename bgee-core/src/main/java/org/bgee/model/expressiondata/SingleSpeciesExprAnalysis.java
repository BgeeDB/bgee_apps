package org.bgee.model.expressiondata;

import org.bgee.model.gene.Gene;

import java.util.Collection;
import java.util.Map;

public class SingleSpeciesExprAnalysis extends MultiGeneExprAnalysis<Condition> {

    public SingleSpeciesExprAnalysis(Collection<String> requestedPublicGeneIds,
                                     Collection<String> requestedPublicGeneIdsNotFound, 
                                     Collection<Gene> genes,
                                     Map<Condition, MultiGeneExprCounts> condToCounts) {
        super(requestedPublicGeneIds, requestedPublicGeneIdsNotFound, genes, condToCounts);
    }
}
