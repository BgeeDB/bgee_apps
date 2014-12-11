package org.bgee.view.dsv;

import javax.servlet.http.HttpServletResponse;

import org.bgee.controller.BgeeProperties;
import org.bgee.controller.RequestParameters;
import org.bgee.view.DownloadDisplay;
import org.bgee.view.GeneralDisplay;
import org.bgee.view.ViewFactory;

public class DsvFactory extends ViewFactory
{	
	public DsvFactory(HttpServletResponse response, String localDelimiter,
	        RequestParameters requestParameters, BgeeProperties prop) 
	{
		super(response, requestParameters, prop);
	}

	@Override
	public DownloadDisplay getDownloadDisplay() {
		return null;
	}

	@Override
	public GeneralDisplay getGeneralDisplay() {
		return null;
	}

}
