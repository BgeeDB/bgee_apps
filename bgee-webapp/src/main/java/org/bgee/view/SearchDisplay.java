package org.bgee.view;

import java.util.Collection;

import org.bgee.model.gene.GeneMatch;

/**
 * Interface defining methods to be implemented by views related to {@code Search}s.
 * 
 * @author  Valentine Rech de Laval
 * @version Bgee 14, Mar. 2019
 * @since   Bgee 13, Feb. 2016
 */
public interface SearchDisplay {

    /**
     * Display the response following an auto-complete gene search parameters upload to server.
     *
     * @param geneMatches   A {code Collection} of {@code GeneMatch}es that is the response
     *                      of a gene search.
     */
    void displayGeneCompletionByGeneList(Collection<GeneMatch> geneMatches);

    /**
     * Display the response following a gene search parameters upload to server.
     *
     * @param count         An {code int} that is the number of genes found by a search.
     * @param searchTerm    A {code String} that is the search term.
     */
    void displayExpasyResult(int count, String searchTerm);

    void displayMatchesForGeneCompletion(Collection<String> matches);
}
