package org.bgee.view.xml;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.bgee.controller.BgeeProperties;
import org.bgee.view.DownloadDisplay;
import org.bgee.view.GeneralDisplay;
import org.bgee.view.ViewFactory;

public class XmlFactory extends ViewFactory
{

	public XmlFactory(HttpServletResponse response)
	{
		super(response);
	}
	
	@Override
	public DownloadDisplay getDownloadDisplay(BgeeProperties prop) 
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public GeneralDisplay getGeneralDisplay(BgeeProperties prop) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}
}
