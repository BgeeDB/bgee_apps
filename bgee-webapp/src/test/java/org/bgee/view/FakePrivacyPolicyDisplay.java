package org.bgee.view;

import org.bgee.controller.BgeeProperties;
import org.bgee.controller.RequestParameters;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * This is a fake display used for tests. It should be called when the parameter 'page' provided
 * in the URL is 'privacy_policy'.
 *
 * @author  Valentine Rech de Laval
 * @version Bgee 14, Aug. 2018
 * @since   Bgee 14, Aug. 2018
 */
public class FakePrivacyPolicyDisplay extends FakeParentDisplay implements PrivacyPolicyDisplay {

    public FakePrivacyPolicyDisplay(HttpServletResponse response, RequestParameters requestParameters,
                                    BgeeProperties prop, ViewFactory factory) throws IOException {
        super(response, requestParameters, prop, factory);
    }

    @Override
    public void displayPrivacyPolicyPage() {
        this.out.println("Test page displayPrivacyPolicyPage() is good !");
    }
}
