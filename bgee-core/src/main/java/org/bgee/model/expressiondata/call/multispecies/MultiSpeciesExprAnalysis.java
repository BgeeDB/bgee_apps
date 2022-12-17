package org.bgee.model.expressiondata.call.multispecies;

import org.bgee.model.expressiondata.call.MultiGeneExprAnalysis;
import org.bgee.model.gene.Gene;

import java.util.Collection;
import java.util.Map;

/**
 * A class storing information about the comparison of expression between genes
 * belonging to several species. See {@link MultiGeneExprAnalysis} for more details.
 * This class extends {@code MultiGeneExprAnalysis} by defining its generic type
 * as {@code MultiSpeciesCondition}.
 * See {@link org.bgee.model.expressiondata.call.SingleSpeciesExprAnalysis SingleSpeciesExprAnalysis}
 * for comparisons in a single species.
 *
 * @author Frederic Bastian
 * @version Bgee 14 May 2019
 * @see org.bgee.model.expressiondata.call.SingleSpeciesExprAnalysis SingleSpeciesExprAnalysis
 * @since Bgee 14 May 2019
 */
public class MultiSpeciesExprAnalysis extends MultiGeneExprAnalysis<MultiSpeciesCondition> {

    public MultiSpeciesExprAnalysis(Collection<Gene> genes,
                                    Map<MultiSpeciesCondition, MultiGeneExprCounts> condToCounts) {
        super(genes, condToCounts);
    }

    @Override
    public String toString() {
        return "MultiSpeciesExprAnalysis " + super.toString();
    }
}
