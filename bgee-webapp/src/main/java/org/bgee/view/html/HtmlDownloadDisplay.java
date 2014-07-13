package org.bgee.view.html;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.bgee.controller.RequestParameters;
import org.bgee.view.DownloadDisplay;

public class HtmlDownloadDisplay extends HtmlParentDisplay implements DownloadDisplay
{

	public HtmlDownloadDisplay(HttpServletResponse response, RequestParameters requestParameters) throws IOException
    {
		super(response,requestParameters);
    }

    @Override
	public void displayDownloadPage()
    {
    	this.startDisplay("download", "Download area");
    	
    	this.write("<h1>Page de download (Elle arrive) </h1>");
    	
    	this.endDisplay();
    }
      
}

