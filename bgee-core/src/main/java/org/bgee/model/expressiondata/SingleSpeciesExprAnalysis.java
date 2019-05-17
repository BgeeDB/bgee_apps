package org.bgee.model.expressiondata;

import org.bgee.model.gene.Gene;

import java.util.Collection;
import java.util.Map;

/**
 * A class storing information about the comparison of expression between genes
 * belonging to a single species. See {@link MultiGeneExprAnalysis} for more details.
 * This class extends {@code MultiGeneExprAnalysis} by defining its generic type
 * as {@code Condition}.
 * See {@link org.bgee.model.expressiondata.multispecies.MultiSpeciesExprAnalysis MultiSpeciesExprAnalysis}
 * for comparisons in several species.
 *
 * @author Frederic Bastian
 * @version Bgee 14 May 2019
 * @see org.bgee.model.expressiondata.multispecies.MultiSpeciesExprAnalysis MultiSpeciesExprAnalysis
 * @since Bgee 14 May 2019
 */
public class SingleSpeciesExprAnalysis extends MultiGeneExprAnalysis<Condition> {

    public SingleSpeciesExprAnalysis(Collection<String> requestedPublicGeneIds,
                                     Collection<String> requestedPublicGeneIdsNotFound, 
                                     Collection<Gene> genes,
                                     Map<Condition, MultiGeneExprCounts> condToCounts) {
        super(requestedPublicGeneIds, requestedPublicGeneIdsNotFound, genes, condToCounts);
    }

    @Override
    public String toString() {
        return "SingleSpeciesExprAnalysis " + super.toString();
    }
}
