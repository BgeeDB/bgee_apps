package org.bgee.view;

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
     * @param speciesToGeneCount    A {@code Map} where keys are {@code Species} objects, 
     *                              the associated values being a {@code Long} that are gene ID count 
     *                              found in the species.
     * @param selectedSpeciesId     A {@code String} that is the species ID to be used.
     * @param validStages           A {@code Set} of {@code DevStage}s containing valid stages.
     * @param submittedGeneIds      A {@code Set} of {@code String}s that are submitted gene IDs.
     *                              It could be {@code null} if we do not need to add them
     *                              to the response.
     * @param undeterminedGeneIds   A {@code Set} of {@code String}s that are submitted gene IDs
     *                              from undetermined species.
     * @param statusCode            An {@code int} that is the status code of the response.
     * @param msg                   A {@code String} that is the text message resuming the response.
     */
    //XXX: Should the Species class has a validStages attribute? Or is it not directly related to Species?
    public void sendGeneListReponse(Map<Species, Long> speciesToGeneCount, String selectedSpeciesId,
            Set<DevStage> validStages, Set<String> submittedGeneIds, Set<String> undeterminedGeneIds,
            String msg);
    
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
