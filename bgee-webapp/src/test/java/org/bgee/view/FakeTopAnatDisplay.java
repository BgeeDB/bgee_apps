package org.bgee.view;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.bgee.controller.BgeeProperties;
import org.bgee.controller.RequestParameters;
import org.bgee.model.TaskManager;
import org.bgee.model.anatdev.DevStage;
import org.bgee.model.topanat.TopAnatResults;

/**
 * Fake view used for tests related to topAnat display. 
 * 
 * @author  Frederic Bastian
 * @author  Mathieu Seppey
 * @author  Valentine Rech de Laval
 * @version Bgee 13 Nov 2015
 * @since   Bgee 13
 */
public class FakeTopAnatDisplay extends FakeParentDisplay implements TopAnatDisplay {

    public FakeTopAnatDisplay(HttpServletResponse response,
            RequestParameters requestParameters, BgeeProperties prop,
            ViewFactory factory) throws IOException {
        super(response, requestParameters, prop, factory);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void displayTopAnatHomePage() {
        this.out.println("Test topAnat container");
    }

    @Override
    public void sendGeneListReponse(Map<String, Long> speciesIdToGeneCount, String selectedSpeciesId,
            Set<DevStage> validStages, Set<String> submittedGeneIds, Set<String> underteminedGeneIds,
            int statusCode, String msg) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void sendTopAnatParameters(String hash) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void sendNewJobResponse(int jobTrackingId) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void sendResultResponse(TopAnatResults results) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void sendJobStatusResponse(TaskManager taskManager) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void sendJobErrorResponse(TaskManager taskManager) {
        // TODO Auto-generated method stub
        
    }
    
}
