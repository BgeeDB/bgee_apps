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
     * Display the download page of processed raw data files.
     * 
     * @param groups    A {@code Collection} of {@code SpeciesDataGroup}s for which 
     *                  to display files.
     * @param keywords  A {@code Map} where keys are {@code Integer}s that are species IDs,
     *                  the associated value being a {@code Set} of {@code String}s
     *                  corresponding to keywords associated to the species.
     */
     void displayProcessedExpressionValuesDownloadPage(List<SpeciesDataGroup> groups, 
                                                       Map<Integer, Set<String>> keywords);

    /**
     * Display the download page of gene expression call files.
     * 
     * @param groups    A {@code Collection} of {@code SpeciesDataGroup}s for which 
     *                  to display files.
     * @param keywords  A {@code Map} where keys are {@code Integer}s that are species IDs,
     *                  the associated value being a {@code Set} of {@code String}s
     *                  corresponding to keywords associated to the species.
     */
     void displayGeneExpressionCallDownloadPage(List<SpeciesDataGroup> groups,
    		                                    Map<Integer, Set<String>> keywords);
     
     /**
      * Display the download page of the Dumps.
      */
      void displayDumpsPage();

}
