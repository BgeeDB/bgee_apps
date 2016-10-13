package org.bgee.view;

import java.util.LinkedHashMap;

/**
 * Interface defining the methods that views related to topAnat must implemented. 
 * 
 * @author  Frederic Bastian
 * @author  Valentine Rech de Laval
 * @version Bgee 13 Oct 2016
 * @since   Bgee 13
 */
//XXX, FB: Actually, I think these methods should not take Maps as arguments, 
//they were designed with JSON responses in mind. An HTML response would not use such Maps.
//The Maps should be created by the JSON view, the arguments should be the various response objects.
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
     * Send the response following the end of a TopAnat job.
     * 
     * @param data  A {@code LinkedHashMap} where keys are {@code String}s that are displayed 
     *              object names, the associated values being a {@code Object} that are the object
     *              to be displayed.
     * @param msg   A {@code String} that is the text message resuming the response.
     */
    public void sendResultResponse(LinkedHashMap<String, Object> data, String msg);
    
    /**
     * Send the response when a tracking job is requested.
     * 
     * @param data  A {@code LinkedHashMap} where keys are {@code String}s that are displayed 
     *              object names, the associated values being a {@code Object} that are the object
     *              to be displayed.
     * @param msg   A {@code String} that is the text message resuming the response.
     */
    //TODO: certainly to be moved to JobDisplay interface
    public void sendTrackingJobResponse(LinkedHashMap<String, Object> data, String msg);
}
