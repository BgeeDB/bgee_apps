package org.bgee.view;

import org.bgee.controller.BgeeProperties;
import org.bgee.controller.RequestParameters;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * This is a fake display used for tests. It should be called when the parameter 'page' provided
 * in the URL is 'projects'.
 *
 * @author  Valentine Rech de Laval
 * @version Bgee 14, Apr. 2019
 * @since   Bgee 14, Apr. 2019
 */
public class FakeProjectsDisplay extends FakeParentDisplay implements ProjectsDisplay {

    public FakeProjectsDisplay(HttpServletResponse response, RequestParameters requestParameters,
                               BgeeProperties prop, ViewFactory factory) throws IOException {
        super(response, requestParameters, prop, factory);
    }

    @Override
    public void displayProjectsPage() {
        this.out.println("Test page displayProjectsPage() is good !");
    }
}
