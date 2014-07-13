package org.bgee.view.html;

import java.io.IOException;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringEscapeUtils;
import org.bgee.controller.RequestParameters;
import org.bgee.view.ConcreteDisplayParent;
import org.bgee.view.DisplayParentInterface;

public class HtmlParentDisplay extends ConcreteDisplayParent implements DisplayParentInterface
{
    private int uniqueId;
    protected RequestParameters requestParameters;
    protected final static String parametersSeparator = "&amp;";
	
	public HtmlParentDisplay(HttpServletResponse response, RequestParameters requestParameters) throws IOException
    {
        super(response);
        this.uniqueId = 0;
        this.requestParameters = requestParameters;
    }
	
	protected int getUniqueId()
	{
		//need to return 0 the first time this method is called;
		int idToReturn = this.uniqueId;
		this.uniqueId++;
		return idToReturn;
	}
    
	@Override
	public void emptyDisplay()
    {
    	this.sendHeaders(true);
		this.writeln("");
    }
	@Override
	public void startDisplay(String page, String title)
    {
    }
	
	public void endDisplay()
    {
    }
    
	// TODO move into BgeeStringUtils ??? 
    public static String htmlEntities(String stringToWrite)
    {
    	try {							
    // TODO check if new version, HTML5
    		return StringEscapeUtils.escapeHtml4(stringToWrite).replaceAll("'", "&apos;");
    	} catch (Exception e) {
    		return "";
    	}
    }
    
    public String nl2br(String string)
    {
    	String localString = string;
    	localString = localString.replaceAll("\r\n", "\n");
    	localString = localString.replaceAll("\r",   "\n");	
    	localString = localString.replaceAll("\n",   "<br/>");
    	return localString;
    }
	
	@Override
	public void sendHeaders(boolean ajax)
	{
		if (this.response == null) {
			return;
		}
		if (!this.headersAlreadySent) {
			this.response.setContentType("text/html");
			if (ajax) {
				this.response.setDateHeader("Expires", 1);
				this.response.setHeader("Cache-Control", 
						"no-store, no-cache, must-revalidate, proxy-revalidate");
				this.response.addHeader("Cache-Control", "post-check=0, pre-check=0");
				this.response.setHeader("Pragma", "No-cache");
			}
			this.headersAlreadySent = true;
		}
	}
	
	public void sendServiceUnavailableHeaders()
	{
		if (this.response == null) {
			return;
		}
		if (!this.headersAlreadySent) {
			this.response.setContentType("text/html");
			this.response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
			this.headersAlreadySent = true;
		}
	}
	
	protected void sendBadRequestHeaders()
	{
		if (this.response == null) {
			return;
		}
		if (!this.headersAlreadySent) {
			this.response.setContentType("text/html");
			this.response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			this.headersAlreadySent = true;
		}
	}
	
	protected void sendPageNotFoundHeaders()
	{
		if (this.response == null) {
			return;
		}
		if (!this.headersAlreadySent) {
			this.response.setContentType("text/html");
			this.response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			this.headersAlreadySent = true;
		}
	}
	
	protected void sendInternalErrorHeaders()
	{
		if (this.response == null) {
			return;
		}
		if (!this.headersAlreadySent) {
			this.response.setContentType("text/html");
			this.response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			this.headersAlreadySent = true;
		}
	}
	
	public String urlEncode(String string)
    {
    	if (string == null) {
    		return null;
    	}
		String encodeString = string;
    	try {
    		// warning, you need to add an attribut to the connector in server.xml  
    		// in order to get the utf-8 encoding working : URIEncoding="UTF-8"
    		encodeString = java.net.URLEncoder.encode(string, "ISO-8859-1");
    	} catch (Exception e) {
    		
    	}
    	return encodeString;
    }
		
	protected String displayHelpLink(String cat, String display)
	{
		return "<span class='help'><a href='#' class='help|" + 
				cat + "'>" + display + "</a></span>";
	}
	protected String displayHelpLink(String cat)
	{
		return this.displayHelpLink(cat, "[?]");
	}
	
	@Override
	public void displayBgeeMenu() {
		// TODO Auto-generated method stub
		
	}
}
