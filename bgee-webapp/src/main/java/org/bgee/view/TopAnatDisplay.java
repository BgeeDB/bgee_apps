package org.bgee.view;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.bgee.model.TaskManager;
import org.bgee.model.anatdev.DevStage;
import org.bgee.model.species.Species;
import org.bgee.model.topanat.TopAnatResults;

/**
 * Interface defining the methods that views related to topAnat must implemented. 
 * 
 * @author  Frederic Bastian
 * @author  Valentine Rech de Laval
 * @version Bgee 13 Nov 2015
 * @since   Bgee 13
 */
public interface TopAnatDisplay {
    /**
     * Display the topAnat home page.
     */
    public void displayTopAnatHomePage();
    
    /**
     * Display the response following a gene list upload to server
     * (either from file upload, or from copy/paste in textarea).
     * 
     * @param data  A {@code LinkedHashMap} where keys are {@code String}s that are displayed 
     *              object names, the associated values being a {@code Object} that are the object
     *              to be displayed.
     * @param msg   A {@code String} that is the text message resuming the response.
     */
    public void sendGeneListReponse(LinkedHashMap<String, Object> data, String msg);
    
    /**
     * Display the response following a top anat parameters upload to server.
     * 
     * @param hash  A {@code String} that is hash to be "de-hash".
     */
    public void sendTopAnatParameters(String hash);

    /**
     * Send the response following the submission of a new TopAnat job, with no cached results.
     * 
     * @param jobTrackingId A {@code int} that is the ID of the new TopAnat job.
     */
    public void sendNewJobResponse(int jobTrackingId);
    
    /**
     * Send the response following the end of a TopAnat job.
     * 
     * @param results A {@code TopAnatResults} that is the results of a finished TopAnat job.
     */
    public void sendResultResponse(TopAnatResults results);
    
    /**
     * Send the response while a TopAnat job is running.
     * 
     * @param taskManager   A {@code TaskManager} that is the task manager tracking a TopAnat job.
     */
    public void sendJobStatusResponse(TaskManager taskManager);
    
    /**
     * Send the response following an error in a TopAnat job.
     * 
     * @param taskManager   A {@code TaskManager} that is the task manager tracking a TopAnat job.
     */
    public void sendJobErrorResponse(TaskManager taskManager);
}
