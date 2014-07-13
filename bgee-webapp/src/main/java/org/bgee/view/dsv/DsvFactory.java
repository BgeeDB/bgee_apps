package org.bgee.view.dsv;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.bgee.view.DownloadDisplay;
import org.bgee.view.GeneralDisplay;
import org.bgee.view.ViewFactory;

public class DsvFactory extends ViewFactory
{
    private String delimiter;
	
	public DsvFactory(HttpServletResponse response, String localDelimiter) 
	{
		super(response);
		this.delimiter = localDelimiter;
	}

	@Override
	public DownloadDisplay getDownloadDisplay() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public GeneralDisplay getGeneralDisplay() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

}
