package org.bgee.view;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.bgee.controller.BgeeProperties;
import org.bgee.controller.RequestParameters;

/**
 * This is a fake display used for tests. It should be called when the parameter 'page' provided
 * in the URL is 'download'.
 * 
 * @author Mathieu Seppey
 * @version Bgee 13 Aug 2014
 * @since   Bgee 13
 */
public class TestDownloadDisplay extends TestParentDisplay implements DownloadDisplay
{
    public TestDownloadDisplay(HttpServletResponse response, RequestParameters requestParameters,
            BgeeProperties prop) throws IOException
    {
        super(response, requestParameters, prop);
    }

    @Override
    /**
     * This method call the {@code PrintWriter}. This will be used to assess that the
     * controller handles all parameters correctly to send the correct output to the
     * {@code HttpServletResponse}
     */
    public void displayDownloadPage() {
        this.out.println("Test page is good !");
    }

    @Override
    public void displayDocumentation() {
        this.out.println("Test page is good !");
    }
}
