package org.bgee.view.xml;

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.BgeeProperties;
import org.bgee.controller.RequestParameters;
import org.bgee.view.DownloadDisplay;
import org.bgee.view.GeneralDisplay;
import org.bgee.view.ViewFactory;

public class XmlFactory extends ViewFactory
{
    
    private final static Logger log = LogManager.getLogger(XmlFactory.class.getName());

	public XmlFactory(HttpServletResponse response, RequestParameters requestParameters,
	        BgeeProperties prop)
	{
		super(response, requestParameters, prop);
	}
	
	@Override
	public DownloadDisplay getDownloadDisplay() 
	{
	    log.entry();
		return log.exit(null);
	}

    @Override
	public GeneralDisplay getGeneralDisplay() {
	    log.entry();
		throw log.throwing(new UnsupportedOperationException("Not available for XML display"));
	}
}
