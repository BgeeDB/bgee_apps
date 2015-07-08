package org.bgee.view.json;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.BgeeProperties;
import org.bgee.controller.RequestParameters;
import org.bgee.view.AboutDisplay;
import org.bgee.view.DocumentationDisplay;
import org.bgee.view.DownloadDisplay;
import org.bgee.view.ErrorDisplay;
import org.bgee.view.GeneralDisplay;
import org.bgee.view.ViewFactory;

/**
 * {@code ViewFactory} returning objects generating JSON views.
 * 
 * @author  Frederic Bastian
 * @version Bgee 13 Jul 2015
 * @since   Bgee 13
 */
public class JsonFactory extends ViewFactory { 
    
    private final static Logger log = LogManager.getLogger(JsonFactory.class.getName());
    
    /**
     * Constructor providing the necessary dependencies. 
     * 
     * @param response          A {@code HttpServletResponse} that will be used to display the page to 
     *                          the client
     * @param requestParameters The {@code RequestParameters} that handles the parameters of the 
     *                          current request.
     * @param prop              An instance of {@code BgeeProperties} to provide the all 
     *                          the properties values
     */
    public JsonFactory(HttpServletResponse response, RequestParameters requestParameters, 
            BgeeProperties prop) {
        super(response, requestParameters, prop);
    }

    @Override
    public ErrorDisplay getErrorDisplay() throws IOException {
        log.entry();
        return log.exit(new JsonErrorDisplay(this.response, this.requestParameters,
            this.prop, this));
    }

    @Override
    public GeneralDisplay getGeneralDisplay() throws IOException {
        throw log.throwing(new UnsupportedOperationException("Not available for JSON display"));
    }

    @Override
    public DownloadDisplay getDownloadDisplay() throws IOException {
        throw log.throwing(new UnsupportedOperationException("Not available for JSON display"));
    }

    @Override
    public DocumentationDisplay getDocumentationDisplay() throws IOException {
        throw log.throwing(new UnsupportedOperationException("Not available for JSON display"));
    }

    @Override
    public AboutDisplay getAboutDisplay() throws IOException {
        throw log.throwing(new UnsupportedOperationException("Not available for JSON display"));
    }
    
}
