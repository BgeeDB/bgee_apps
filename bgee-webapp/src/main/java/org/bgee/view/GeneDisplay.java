package org.bgee.view;

import java.util.Set;

import org.bgee.controller.CommandGene.GeneResponse;
import org.bgee.model.gene.Gene;

/**
 * Interface defining methods to be implemented by views related to {@code Gene}s.
 * 
 * @author  Philippe Moret
 * @author  Valentine Rech de Laval
 * @version Bgee 13, Mar. 2017
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

    /**
     * Displays a {@code Set} of {@code Gene}s.
     * 
     * @param genes     A {@code Set} of {@code Gene}s to be displayed.
     */
    void displayGeneChoice(Set<Gene> genes);
}
