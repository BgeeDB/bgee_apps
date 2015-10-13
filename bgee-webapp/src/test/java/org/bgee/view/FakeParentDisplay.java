package org.bgee.view;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.bgee.controller.BgeeProperties;
import org.bgee.controller.RequestParameters;

/**
 * This class exists only to be the parent of {@code FakeDownloadDisplay}
 * 
 * @author Mathieu Seppey
 * @author Frederic Bastian
 * @version Bgee 13 Jul 2015
 * @since   Bgee 13
 */
// None of these classes launch test
public class FakeParentDisplay extends ConcreteDisplayParent {

    public FakeParentDisplay(HttpServletResponse response, RequestParameters requestParameters, 
	        BgeeProperties prop, ViewFactory factory) throws IOException {
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
