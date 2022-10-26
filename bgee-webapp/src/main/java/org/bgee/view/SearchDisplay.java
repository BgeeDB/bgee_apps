package org.bgee.view;

import java.util.Collection;
import java.util.Set;

import org.bgee.model.anatdev.DevStage;
import org.bgee.model.search.SearchMatchResult;

/**
 * Interface defining methods to be implemented by views related to {@code Search}s.
 * 
 * @author  Valentine Rech de Laval
 * @version Bgee 15, Oct. 2021
 * @since   Bgee 13, Feb. 2016
 */
public interface SearchDisplay {
    /**
     * Display the response following a gene search parameters upload to server.
     *
     * @param count         An {code int} that is the number of genes found by a search.
     * @param searchTerm    A {code String} that is the search term.
     */
    void displayExpasyResult(int count, String searchTerm);

    void displayDefaultSphinxSearchResult(String searchTerm, SearchMatchResult<?> result);

    void displayDevStageSearchResult(Set<DevStage> result);

    void displayMatchesForGeneCompletion(Collection<String> matches);
}
