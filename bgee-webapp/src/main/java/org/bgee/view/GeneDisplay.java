package org.bgee.view;

import org.bgee.controller.CommandGene.GeneResponse;

/**
 * Interface defining methods to be implemented by views related to {@code Gene}s.
 * 
 * @author Philippe Moret
 * @version Bgee 13, Nov.
 * @since   Bgee 13, Nov.
 */
public interface GeneDisplay {


	/**
	 * Displays the default gene page (when no arguments are given)
	 */
	void displayGenePage();
	
	/**
	 * Displays information about a specific {@code Gene}.
	 * 
	 * @param geneResponse     A {@code GeneResponse} containing information about a {@code Gene} 
	 *                         to be displayed.
	 */
	void displayGene(GeneResponse geneResponse);
}
