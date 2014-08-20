package org.bgee.view.html;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.BgeeProperties;
import org.bgee.controller.RequestParameters;
import org.bgee.view.DownloadDisplay;
import org.bgee.view.GeneralDisplay;
import org.bgee.view.ViewFactory;


public class HtmlFactory extends ViewFactory
{
    private final static Logger log = LogManager.getLogger(HtmlFactory.class.getName());
    
	public HtmlFactory(HttpServletResponse response, RequestParameters requestParameters)
    {
    	super(response, requestParameters);
    }
    
	@Override
	public DownloadDisplay getDownloadDisplay(BgeeProperties prop)  throws IOException
	{
	    log.entry(prop);
		return log.exit(new HtmlDownloadDisplay(this.response, this.requestParameters, prop));
	}

	@Override
	public GeneralDisplay getGeneralDisplay(BgeeProperties prop) throws IOException {
	    log.entry(prop);
		return log.exit(new HtmlGeneralDisplay(this.response, this.requestParameters, prop));
	}
    
}
