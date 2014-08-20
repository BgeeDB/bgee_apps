package org.bgee.view.html;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.BgeeProperties;
import org.bgee.controller.RequestParameters;
import org.bgee.view.ConcreteDisplayParent;
import org.bgee.view.DisplayParentInterface;

public class HtmlParentDisplay extends ConcreteDisplayParent implements DisplayParentInterface
{
    
    private final static Logger log = LogManager.getLogger(HtmlParentDisplay.class.getName());
    
	private int uniqueId;
	protected RequestParameters requestParameters;
	protected final static String parametersSeparator = "&amp;";

	public HtmlParentDisplay(HttpServletResponse response, RequestParameters requestParameters, BgeeProperties prop) throws IOException
	{
		super(response,prop);
		this.uniqueId = 0;
		this.requestParameters = requestParameters;
	}

	protected int getUniqueId()
	{
	    log.entry();
		//need to return 0 the first time this method is called;
		int idToReturn = this.uniqueId;
		this.uniqueId++;
		return log.exit(idToReturn);
	}

	@Override
	public void emptyDisplay()
	{
	    log.entry();
		this.sendHeaders(true);
		this.writeln("");
		log.exit();
	}
	@Override
	public void startDisplay(String page, String title)
	{
	    log.entry(page, title);
		this.writeln("<!DOCTYPE html>");
		this.writeln("<html lang='en'>");
		this.writeln("<head>");
		this.writeln("<meta charset='UTF-8'>");
		this.writeln("<title>"+title+"</title>");
		this.writeln("<meta name='description' content='Bgee allows to automatically"
				+ " compare gene expression patterns between species, by referencing"
				+ " expression data on anatomical ontologies, and designing homology"
				+ " relationships between them.'/>");
		this.writeln("<meta name='keywords' content='bgee, gene expression, "
				+ "evolution, ontology, anatomy, development, evo-devo database, "
				+ "anatomical ontology, developmental ontology, gene expression "
				+ "evolution'/>");
		this.writeln("<meta name='dcterms.rights' content='Bgee copyright 2007/2014 UNIL' />");
		this.writeln("<link rel='shortcut icon' type='image/x-icon' href='"
				+this.prop.getImagesRootDirectory()+"favicon.ico'/>");
		this.includeCss(page+".css"); // default css for every pages
		this.includeCss(); // additional css if override
		this.includeJs();// add js if override
		this.writeln("</head>");
		this.writeln("<body>");
		this.writeln("<noscript>Sorry, your browser does not support JavaScript!</noscript>");
		this.displayBgeeMenu();
		this.writeln("<div id='bgee_top'><a id='TOP'></a></div>");
		this.writeln("<div id='sib_container'>");
		log.exit();
	}

	public void endDisplay()
	{
	    log.entry();
		this.writeln("</div>");
		this.writeln("<footer>");
		this.writeln("<div id = 'sib_footer_content'>");
		this.writeln("<a href = 'http://www.isb-sib.ch'>SIB Swiss Institute of Bioinformatics</a>");
		this.writeln("<div id = 'sib_footer_right'>");
		this.writeln("<a href='#TOP' id = 'sib_footer_gototop'>"
				+ "<span style = 'padding-left: 10px'>Back to the Top</span></a>");
		this.writeln("</div>");
		this.writeln("</div>");
		this.writeln("</footer>");
		this.writeln("</div>");
		this.writeln("</body>");
		this.writeln("</html>");
		log.exit();
	}

	public static String htmlEntities(String stringToWrite)
	{
	    log.entry(stringToWrite);
		try {                            
		    return log.exit(StringEscapeUtils.escapeHtml4(stringToWrite).replaceAll("'", "&apos;"));
		} catch (Exception e) {
			return log.exit("");
		}
	}

	public String nl2br(String string)
	{
	    log.entry(string);
		String localString = string;
		localString = localString.replaceAll("\r\n", "\n");
		localString = localString.replaceAll("\r",   "\n");    
		localString = localString.replaceAll("\n",   "<br/>");
		return log.exit(localString);
	}

	@Override
	public void sendHeaders(boolean ajax)
	{
	    log.exit(ajax);
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
		log.exit();
	}

	public void sendServiceUnavailableHeaders()
	{
	    log.entry();
		if (this.response == null) {
			return;
		}
		if (!this.headersAlreadySent) {
			this.response.setContentType("text/html");
			this.response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
			this.headersAlreadySent = true;
		}
		log.exit();
	}

	protected void sendBadRequestHeaders()
	{
	    log.entry();
		if (this.response == null) {
			return;
		}
		if (!this.headersAlreadySent) {
			this.response.setContentType("text/html");
			this.response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			this.headersAlreadySent = true;
		}
		log.exit();
	}

	protected void sendPageNotFoundHeaders()
	{
	    log.entry();
		if (this.response == null) {
			return;
		}
		if (!this.headersAlreadySent) {
			this.response.setContentType("text/html");
			this.response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			this.headersAlreadySent = true;
		}
		log.exit();
	}

	protected void sendInternalErrorHeaders()
	{
	    log.entry();
		if (this.response == null) {
			return;
		}
		if (!this.headersAlreadySent) {
			this.response.setContentType("text/html");
			this.response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			this.headersAlreadySent = true;
		}
		log.exit();
	}

	public String urlEncode(String string)
	{
	    log.entry(string);
		if (string == null) {
			return log.exit(null);
		}
		String encodeString = string;
		try {
			// warning, you need to add an attribut to the connector in server.xml  
			// in order to get the utf-8 encoding working : URIEncoding="UTF-8"
			encodeString = java.net.URLEncoder.encode(string, "ISO-8859-1");
		} catch (Exception e) {

		}
		return log.exit(encodeString);
	}

	protected String displayHelpLink(String cat, String display)
	{
	    log.entry(cat, display);
		return log.exit("<span class='help'><a href='#' class='help|" + 
				cat + "'>" + display + "</a></span>");
	}
	protected String displayHelpLink(String cat)
	{
	    log.entry(cat);
		return log.exit(this.displayHelpLink(cat, "[?]"));
	}

	@Override
	public void displayBgeeMenu() {
	    log.entry();
		this.writeln("<header>");

		// Bgee logo
		this.writeln("<a href='#' title='Go to Bgee home page'>"
				+ "<img id='sib_other_logo' src='"+this.prop.getImagesRootDirectory()+"bgee_logo.png' "
				+ "title='Bgee: a dataBase for Gene Expression Evolution' "
				+ "alt='Bgee: a dataBase for Gene Expression Evolution' /> "
				+ "</a>");

		// Title
		this.writeln("<h1>Bgee: Gene Expression Evolution</h1>"
				+ "<h2>Release 13 download page</h2>"
				);

		// SIB logo
		this.writeln("<a href='http://www.isb-sib.ch/' target='_blank' title='Link to the Swiss Institute of Bioinformatics' >"
				+ "<img id='sib_logo' src='"+this.prop.getImagesRootDirectory()+"sib_logo_141x75.png' "
				+ "title='Bgee is part of the Swiss Institute of Bioinformatics' "
				+ "alt='SIB Swiss Institute of Bioinformatics' /> "
				+ "</a>");

		this.writeln("</header>");
		log.exit();
	}

	protected void includeJs(){

	}

	protected void includeJs(String filename){
	    log.entry(filename);
		this.writeln("<script  type='text/javascript' src='"+
				this.prop.getJavascriptFilesRootDirectory()+filename+"'></script>");
		log.exit();
	}

	protected void includeCss(){

	}

	protected void includeCss(String filename){
	    log.entry(filename);
		this.writeln("<link rel='stylesheet' type='text/css' href='"
				+ this.prop.getCssFilesRootDirectory() + filename + "'/>");
		log.exit();
	}

}
