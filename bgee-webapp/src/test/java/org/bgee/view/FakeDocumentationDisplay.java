package org.bgee.view;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.bgee.controller.BgeeProperties;
import org.bgee.controller.RequestParameters;

/**
 * This is a fake display used for tests. It should be called when the parameter 'page' provided
 * in the URL is 'documentation'.
 *
 * @author 	Valentine Rech de Laval
 * @version Bgee 14, Apr. 2019
 * @since   Bgee 13, Mar. 2015
 */
public class FakeDocumentationDisplay extends FakeParentDisplay implements DocumentationDisplay {

    public FakeDocumentationDisplay(HttpServletResponse response, 
            RequestParameters requestParameters, BgeeProperties prop, ViewFactory factory) 
                    throws IOException {
        super(response, requestParameters, prop, factory);
    }

    @Override
    public void displayDocumentationHomePage() {
        this.out.println("Test page displayDocumentationHomePage() is good !");
    }
    @Override
    public void displayCallDownloadFileDocumentation() {
        this.out.println("Test page displayCallDownloadFileDocumentation() is good !");
    }

    @Override
    public void displayRefExprDownloadFileDocumentation() {
        this.out.println("Test page displayRefExprDownloadFileDocumentation() is good !");
    }

    @Override
    public void displayTopAnatDocumentation() {
        this.out.println("Test page displayTopAnatDocumentation() is good !");
    }

    @Override
    public void displayDataSets() {
        this.out.println("Test page displayDataSets() is good !");
    }

    @Override
    public void displayFaq() {
        this.out.println("Test page displayFaq() is good !");
    }
}
