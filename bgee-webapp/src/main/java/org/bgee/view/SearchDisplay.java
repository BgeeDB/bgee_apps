package org.bgee.view;

import java.util.Collection;

import org.bgee.model.gene.GeneMatch;

/**
 * Interface defining methods to be implemented by views related to {@code Search}s.
 * 
 * @author  Valentine Rech de Laval
 * @version Bgee 13, Feb. 2016
 * @since   Bgee 13, Feb. 2016
 */
public interface SearchDisplay {

	/**
     * Display the response following an auto-complete gene search parameters upload to server.
	 *
	 * TODO
	 */
	void displayGeneCompletionByGeneList(Collection<GeneMatch> geneMatches, String searchTerm);

}
