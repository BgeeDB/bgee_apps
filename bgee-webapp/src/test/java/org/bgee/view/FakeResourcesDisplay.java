package org.bgee.view;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.bgee.controller.BgeeProperties;
import org.bgee.controller.RequestParameters;

/**
 * Fake view used for tests related to resource display. 
 * 
 * @author  Julien Wollbrett
 * @version Bgee 14 May 2019
 * @since   Bgee 14 May 2019
 */

public class FakeResourcesDisplay extends FakeParentDisplay implements ResourcesDisplay{

    public FakeResourcesDisplay(HttpServletResponse response, 
            RequestParameters requestParameters, BgeeProperties prop, ViewFactory factory) 
                    throws IOException {
        super(response, requestParameters, prop, factory);
    }

    @Override
    public void displayRPackages() {
        this.out.println("Test page displayRPackagesPage() is good !");        
    }

    @Override
    public void displayAnnotations() {
        this.out.println("Test page displayAnnotations() is good !");        
    }

    @Override
    public void displayOntologies() {
        this.out.println("Test page displayOntologies() is good !");        
    }

    @Override
    public void displaySourceCode() {
        this.out.println("Test page displaySourceCode() is good !");                
    }

}
