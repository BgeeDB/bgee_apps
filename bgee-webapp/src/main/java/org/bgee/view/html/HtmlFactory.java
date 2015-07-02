package org.bgee.view.html;

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
import org.bgee.view.TopAnatDisplay;
import org.bgee.view.ViewFactory;

/**
 * {@code ViewFactory} that returns all displays for the HTML view.
 * 
 * @author  Mathieu Seppey
 * @author  Valentine Rech de Laval
 * @version Bgee 13 Aug 2014
 * @since   Bgee 13
 */
public class HtmlFactory extends ViewFactory {
    
    private final static Logger log = LogManager.getLogger(HtmlFactory.class.getName());
    
    /**
     * Constructor 
     * 
     * @param response          A {@code HttpServletResponse} that will be used to display the page to 
     *                          the client
     * @param requestParameters The {@code RequestParameters} that handles the parameters of the 
     *                          current request.
     * @param prop              An instance of {@code BgeeProperties} to provide the all 
     *                          the properties values
     */
	public HtmlFactory(HttpServletResponse response, RequestParameters requestParameters,
	        BgeeProperties prop) {
    	super(response, requestParameters, prop);
    }
    
	@Override
	public DownloadDisplay getDownloadDisplay()  throws IOException {
	    log.entry();
		return log.exit(new HtmlDownloadDisplay(this.response, this.requestParameters,
		        this.prop, this));
	}

	@Override
	public GeneralDisplay getGeneralDisplay() throws IOException {
	    log.entry();
		return log.exit(new HtmlGeneralDisplay(this.response, this.requestParameters,
		        this.prop, this));
	}

    @Override
    public ErrorDisplay getErrorDisplay() throws IOException {
        log.entry();
        return log.exit(new HtmlErrorDisplay(this.response, this.requestParameters,
                this.prop, this));
    }

    @Override
    public DocumentationDisplay getDocumentationDisplay() throws IOException {
        log.entry();
        return log.exit(new HtmlDocumentationDisplay(
                this.response, this.requestParameters, this.prop, this));
    }

    @Override
    public AboutDisplay getAboutDisplay() throws IOException {
        log.entry();
        return log.exit(new HtmlAboutDisplay(
                this.response, this.requestParameters, this.prop, this));
    }

    @Override
    public TopAnatDisplay getTopAnatDisplay() throws IOException {
        log.entry();
        return log.exit(new HtmlTopAnatDisplay(
                this.response, this.requestParameters, this.prop, this));
    }
}
