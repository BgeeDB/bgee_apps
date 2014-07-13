package org.bgee.view.xml;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

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
	public DownloadDisplay getDownloadDisplay() 
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public GeneralDisplay getGeneralDisplay() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}
}
