package org.bgee.view;

import org.bgee.controller.BgeeProperties;
import org.bgee.controller.RequestParameters;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * This is a fake display used for tests. It should be called when the parameter 'page' provided
 * in the URL is 'collaborations'.
 *
 * @author  Valentine Rech de Laval
 * @version Bgee 14, May 2019
 * @since   Bgee 14, Apr. 2019
 */
public class FakeCollaborationDisplay extends FakeParentDisplay implements CollaborationDisplay {

    public FakeCollaborationDisplay(HttpServletResponse response, RequestParameters requestParameters,
                                    BgeeProperties prop, ViewFactory factory) throws IOException {
        super(response, requestParameters, prop, factory);
    }

    @Override
    public void displayCollaborationPage() {
        this.out.println("Test page displayCollaborationPage() is good !");
    }
}
