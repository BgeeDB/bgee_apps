package org.bgee.view;

import org.bgee.model.file.SpeciesDataGroup;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Interface that defines the methods a display for the download category, i.e. page=download
 * has to implements
 * @author  Mathieu Seppey
 * @author  Valentine Rech de Laval
 * @author  Philippe Moret
 * @version Bgee 14, Mar. 2017
 * @since   Bgee 13
 */
public interface DownloadDisplay {
    
	/**
	 * Display the download page.
	 */
	 void displayDownloadHomePage();
	
    /**
     * Display the download page of processed raw data files.
     */
     void displayProcessedExpressionValuesDownloadPage(List<SpeciesDataGroup> groups, 
                                                       Map<Integer, Set<String>> keywords);

    /**
     * Display the download page of gene expression call files.
     */
     void displayGeneExpressionCallDownloadPage(List<SpeciesDataGroup> groups,
    		                                    Map<Integer, Set<String>> keywords);

}
