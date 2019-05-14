package org.bgee.view;

import org.bgee.model.expressiondata.SingleSpeciesExprAnalysis;
import org.bgee.model.expressiondata.multispecies.MultiSpeciesExprAnalysis;

import java.util.List;

/**
 * Interface defining methods to be implemented by views related to expression comparison.
 *
 * @author  Valentine Rech de Laval
 * @version Bgee 14, May 2019
 * @since   Bgee 14, May 2019
 */
public interface ExpressionComparisonDisplay {

    /**
     * Displays the default expression comparison page (when no arguments are given)
     */
    public void displayExpressionComparisonHomePage();

    /**
     * Displays information about a search of expression comparison with no result.
     *
     * @param geneList  A {@code List} of {@code String}s that are the gene IDs provided by the user.
     */
    public void displayExpressionComparison(List<String> geneList);
    
    /**
     * Displays information about a search of expression comparison in a single-species.
     * 
     * @param geneList  A {@code List} of {@code String}s that are the gene IDs provided by the user.
     * @param analysis  A {@code SingleSpeciesExprAnalysis} that is the result to be displayed.
     */
    void displayExpressionComparison(List<String> geneList, SingleSpeciesExprAnalysis analysis);

    /**
     * Displays information about a search of expression comparison in multi species.
     *
     * @param geneList  A {@code List} of {@code String}s that are the gene IDs provided by the user.
     * @param analysis  A {@code MultiSpeciesExprAnalysis} that is the result to be displayed.
     */
    void displayExpressionComparison(List<String> geneList, MultiSpeciesExprAnalysis analysis);
}
