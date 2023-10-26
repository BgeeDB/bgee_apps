package org.bgee.view;

import org.bgee.model.SearchResult;
import org.bgee.model.expressiondata.call.SingleSpeciesExprAnalysis;
import org.bgee.model.expressiondata.call.multispecies.MultiSpeciesExprAnalysis;
import org.bgee.model.gene.Gene;

/**
 * Interface defining methods to be implemented by views related to expression comparison.
 *
 * @author  Valentine Rech de Laval
 * @version Bgee 14, May 2019
 * @since   Bgee 14, May 2019
 */
public interface ExpressionComparisonDisplay {

    /**
     * Displays information about a search of expression comparison with an error.
     *
     * @param erroMsg       A {@code String} that is the error message to be displayed.
     */
    public void displayExpressionComparison(String erroMsg);
    
    /**
     * Displays information about a search of expression comparison in a single-species.
     *
     * @param searchResult  A {@code SearchResult} storing the requested gene IDs,
     *                      the genes that were found, and the requested gene IDs not found.
     * @param analysis      A {@code SingleSpeciesExprAnalysis} that is the result to be displayed.
     */
    void displayExpressionComparison(SearchResult<String, Gene> searchResult,
            SingleSpeciesExprAnalysis analysis);

    /**
     * Displays information about a search of expression comparison in multi species.
     *
     * @param searchResult  A {@code SearchResult} storing the requested gene IDs,
     *                      the genes that were found, and the requested gene IDs not found.
     * @param analysis      A {@code MultiSpeciesExprAnalysis} that is the result to be displayed.
     */
    void displayExpressionComparison(SearchResult<String, Gene> searchResult,
            MultiSpeciesExprAnalysis analysis);
}
