package org.bgee.view;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.bgee.controller.BgeeProperties;
import org.bgee.controller.RequestParameters;

/**
 * Fake view used for tests related to topAnat display. 
 * 
 * @author Frederic Bastian
 * @author Mathieu Seppey
 * @version Bgee 13 Jul 2015
 * @since   Bgee 13
 */
public class TestTopAnatDisplay extends TestParentDisplay implements TopAnatDisplay {

    public TestTopAnatDisplay(HttpServletResponse response,
            RequestParameters requestParameters, BgeeProperties prop,
            ViewFactory factory) throws IOException {
        super(response, requestParameters, prop, factory);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void displayTopAnatPage() {
        this.out.println("Test topAnat container");
    }
    
}
