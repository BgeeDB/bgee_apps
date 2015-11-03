package org.bgee.view;

import java.util.List;
import java.util.Map;

import org.bgee.model.anatdev.DevStage;
import org.bgee.model.species.Species;
import org.bgee.model.topanat.TopAnatResults;

/**
 * Interface defining the methods that views related to topAnat must implemented. 
 * 
 * @author  Frederic Bastian
 * @version Bgee 13 Jul 2015
 * @since   Bgee 13
 */
public interface TopAnatDisplay {
    /**
     * Display the topAnat home page.
     */
    public void displayTopAnatHomePage();
    /**
     * Display the response following a gene list upload to server (either from file upload, 
     * or from copy/paste in textarea).
     */
    //TODO: example parameters. Maybe we want to use Species objects as keys in the Map?
    //Should the Species class has a validStages attribute? Or is it not directly related to Species?
    public void sendGeneListReponse(Map<String, Integer> speciesIdToGeneCount, 
            Species selectedSpecies, List<DevStage> validStages);
    
    /**
     * Sends the response following the submission of a new TopAnat job, with no cached results.
     */
    public void sendNewJobResponse(int jobTrackingId);
    
    /**
     * Display the topAnat page with displayed results.
     */
    public void displayResultPage(TopAnatResults results);
}
