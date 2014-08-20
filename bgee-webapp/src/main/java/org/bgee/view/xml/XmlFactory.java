package org.bgee.view.xml;

import java.io.IOException;

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

	public XmlFactory(HttpServletResponse response, RequestParameters requestParameters)
	{
		super(response, requestParameters);
	}
	
	@Override
	public DownloadDisplay getDownloadDisplay(BgeeProperties prop) 
	{
	    log.entry(prop);
		// TODO Auto-generated method stub
		return log.exit(null);
	}

	@Override
	public GeneralDisplay getGeneralDisplay(BgeeProperties prop) throws IOException {
	    log.entry(prop);
		// TODO Auto-generated method stub
		return log.exit(null);
	}
}
