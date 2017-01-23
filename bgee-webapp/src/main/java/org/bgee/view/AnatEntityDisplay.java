package org.bgee.view;

import org.bgee.controller.CommandAnatEntity.AnatEntityResponse;

/**
 * Interface defining methods to be implemented by views related to {@code Gene}s.
 * 
 * @author  Julien Wollbrett
 * @version Bgee 13, Feb. 2016
 * @since   Bgee 13, Nov. 2015
 */
public interface AnatEntityDisplay {

	/**
	 * Displays the default anat. entity page (when no arguments are given)
	 */
	void displayAnatEntityHomePage();
	
	/**
	 * Displays information about a specific {@code AnatEntity}.
	 * 
	 * @param geneResponse     An {@code AnatEntityResponse} containing information about an {@code AnatEntity} 
	 *                         to be displayed.
	 */
	void displayAnatEntity(AnatEntityResponse anatEntityResponse);
}
