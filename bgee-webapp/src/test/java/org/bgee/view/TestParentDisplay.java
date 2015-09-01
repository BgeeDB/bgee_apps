package org.bgee.view;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.bgee.controller.BgeeWebappProperties;
import org.bgee.controller.RequestParameters;

/**
 * This class exists only to be the parent of {@code TestDownloadDisplay}
 * 
 * @author Mathieu Seppey
 * @author Frederic Bastian
 * @version Bgee 13 Jul 2015
 * @since   Bgee 13
 */
// None of these classes launch test
public class TestParentDisplay extends ConcreteDisplayParent {

    public TestParentDisplay(HttpServletResponse response, RequestParameters requestParameters, 
	        BgeeWebappProperties prop, ViewFactory factory) throws IOException {
		super(response, requestParameters, prop, factory);
	}

    @Override
    protected String getContentType() {
        return "text/html";
    }

//    @SuppressWarnings("unused")
//    @Override
//    public void sendHeaders(boolean ajax) {
//        // Do nothing
//    }

}
