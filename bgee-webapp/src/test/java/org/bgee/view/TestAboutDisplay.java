package org.bgee.view;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.bgee.controller.BgeeWebappProperties;
import org.bgee.controller.RequestParameters;

/**
 * This is a fake display used for tests. It should be called when the parameter 'page' provided
 * in the URL is 'about'.
 *
 * @author 	Valentine Rech de Laval
 * @version Bgee 13
 * @since 	Bgee 13
 */
public class TestAboutDisplay  extends TestParentDisplay implements AboutDisplay {

    public TestAboutDisplay(HttpServletResponse response, RequestParameters requestParameters, 
            BgeeWebappProperties prop, ViewFactory factory) throws IOException {
        super(response, requestParameters, prop, factory);
    }

    @Override
    public void displayAboutPage() {
        this.out.println("Test page is good !");
    }

}
