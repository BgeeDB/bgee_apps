package org.bgee.view;

import java.util.Collection;
import java.util.Set;

import org.bgee.controller.CommandGene.GeneResponse;
import org.bgee.model.gene.Gene;
import org.bgee.model.gene.GeneMatchResult;

/**
 * Interface defining methods to be implemented by views related to {@code Gene}s.
 * 
 * @author  Philippe Moret
 * @author  Valentine Rech de Laval
 * @author  Frederic Bastian
 * @version Bgee 15, Oct. 2021
 * @since   Bgee 13, Nov. 2015
 */
public interface GeneDisplay {

    /**
     * Displays the default gene page (when no argument is given).
     */
    void displayGeneHomePage();
    
    /**
     * Displays the result of a gene search. 
     * @param searchTerm    A {@code String} that is the query of the gene search. 
     * @param result        A {@code GeneMatchResult} that are the results of the query. 
     */
    void displayGeneSearchResult(String searchTerm, GeneMatchResult result);

    /**
     * Displays information about a specific {@code Gene}.
     * 
     * @param geneResponse     A {@code GeneResponse} containing information about a {@code Gene} 
     *                         to be displayed.
     */
    //XXX: note that if a view needed to display information both considering and not considering 
    //redundant calls, then this method should simply accept two GeneResponses; CommandGene was built 
    //to easily handle this need. 
    void displayGene(GeneResponse geneResponse);

    /**
     * Displays the general information for a list of genes. Is also used as part of the response
     * {@link #displayGene(GeneResponse)}.
     *
     * @param genes  The {@code Collection} of {@code Gene}s for which we want to display general information for.
     */
    void displayGeneGeneralInformation(Collection<Gene> genes);

    /**
     * Displays a {@code Set} of {@code Gene}s.
     * 
     * @param genes     A {@code Set} of {@code Gene}s to be displayed.
     */
    void displayGeneChoice(Set<Gene> genes);
}
