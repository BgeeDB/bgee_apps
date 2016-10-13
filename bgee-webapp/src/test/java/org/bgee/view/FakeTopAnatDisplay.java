package org.bgee.view;

import java.io.IOException;
import java.util.LinkedHashMap;

import javax.servlet.http.HttpServletResponse;

import org.bgee.controller.BgeeProperties;
import org.bgee.controller.RequestParameters;
import org.bgee.model.job.Job;

/**
 * Fake view used for tests related to topAnat display. 
 * 
 * @author  Frederic Bastian
 * @author  Mathieu Seppey
 * @author  Valentine Rech de Laval
 * @version Bgee 13 Oct 2016
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
    public void sendGeneListReponse(LinkedHashMap<String, Object> data, String msg) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void sendTopAnatParameters(String hash) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void sendResultResponse(LinkedHashMap<String, Object> data, String msg) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void sendTrackingJobResponse(LinkedHashMap<String, Object> data,
            String msg) {
        // TODO Auto-generated method stub
        
    }
    
}
