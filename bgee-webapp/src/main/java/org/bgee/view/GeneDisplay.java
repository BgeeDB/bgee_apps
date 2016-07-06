package org.bgee.view;

import org.bgee.controller.CommandGene.GeneResponse;

/**
 * Interface defining methods to be implemented by views related to {@code Gene}s.
 * 
 * @author  Philippe Moret
 * @author  Valentine Rech de Laval
 * @version Bgee 13, Feb. 2016
 * @since   Bgee 13, Nov. 2015
 */
public interface GeneDisplay {

	/**
	 * Displays the default gene page (when no arguments are given)
	 */
	void displayGeneHomePage();
	
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
}
