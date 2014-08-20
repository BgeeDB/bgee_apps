package org.bgee.view.dsv;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.bgee.controller.BgeeProperties;
import org.bgee.controller.RequestParameters;
import org.bgee.view.DownloadDisplay;
import org.bgee.view.GeneralDisplay;
import org.bgee.view.ViewFactory;

public class DsvFactory extends ViewFactory
{	
	public DsvFactory(HttpServletResponse response, String localDelimiter,
	        RequestParameters requestParameters) 
	{
		super(response, requestParameters);
	}

	@Override
	public DownloadDisplay getDownloadDisplay(BgeeProperties prop) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public GeneralDisplay getGeneralDisplay(BgeeProperties prop) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

}
