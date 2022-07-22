package org.bgee.view;

import org.bgee.model.anatdev.multispemapping.AnatEntitySimilarityAnalysis;
import org.bgee.model.species.Species;

import java.util.List;
import java.util.Set;

/**
 * Interface defining methods to be implemented by views related to {@code AnatEntitySimilarity}s.
 *
 * @author  Valentine Rech de Laval
 * @author  Frederic Bastian
 * @version Bgee 15, Dec. 2021
 * @since   Bgee 14, May 2019
 */
public interface AnatomicalSimilarityDisplay {

    /**
     * Displays the default anatomical similarity page (when no arguments are given).
     * 
     * @param allSpecies            A {@code List} of {@code Species} that are species that can be selected. 
     */
    void displayAnatSimilarityHomePage(Set<Species> allSpecies);

    /**
     * Displays the result of a anatomical similarity analysis. 
     *
     * @param allSpecies                    A {@code List} of {@code Species} that are species
     *                                      that can be selected. 
     * @param userSpeciesList               A {@code List} of {@code Integer}s that are species ID
     *                                      s of the analysis. 
     * @param userAnatEntityList            A {@code List} of {@code String}s that are
     *                                      anat. entity IDs of the analysis.
     * @param anatEntitySimilarityAnalysis  A {@code AnatEntitySimilarityAnalysis} that is 
     *                                      the result of the anatomical similarity analysis.
     */
    void displayAnatSimilarityResult(Set<Species> allSpecies, List<Integer> userSpeciesList,
                                     List<String> userAnatEntityList,
                                     AnatEntitySimilarityAnalysis anatEntitySimilarityAnalysis);
}
