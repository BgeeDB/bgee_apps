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
 * @version Bgee May 2015
 * @since 	Bgee 13
 */
public class FakeDocumentationDisplay extends FakeParentDisplay implements DocumentationDisplay {

    public FakeDocumentationDisplay(HttpServletResponse response, 
            RequestParameters requestParameters, BgeeProperties prop, ViewFactory factory) 
                    throws IOException {
        super(response, requestParameters, prop, factory);
    }

    @Override
    public void displayDocumentationHomePage() {
        this.out.println("Test page is good !");
    }
    @Override
    public void displayCallDownloadFileDocumentation() {
        this.out.println("Test2");
    }

    @Override
    public void displayRefExprDownloadFileDocumentation() {
        this.out.println("Test4");
    }

    @Override
    public void displayTopAnatDocumentation() {
    	this.out.println("Test5");
    }

    @Override
    public void displayHowToAccessDataDocumentation() {
        this.out.println("Test3");
    }

}
