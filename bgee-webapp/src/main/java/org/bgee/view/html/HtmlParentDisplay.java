package org.bgee.view.html;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.BgeeProperties;
import org.bgee.controller.RequestParameters;
import org.bgee.view.ConcreteDisplayParent;

/**
 * Parent of all display for the {@code displayTypes} HTML
 * 
 * @author  Mathieu Seppey
 * @author Frederic Bastian
 * @version Bgee 13 Aug 2014
 * @since   Bgee 13
 */
public class HtmlParentDisplay extends ConcreteDisplayParent
{

    private final static Logger log = LogManager.getLogger(HtmlParentDisplay.class.getName());

    /**
     * Escape HTML entities in the provided {@code String}
     * @param stringToWrite A {@code String} that contains the HTML to escape
     * @return  The escaped HTML
     */
    public static String htmlEntities(String stringToWrite)
    {
        log.entry(stringToWrite);
    	try {                            
    	    return log.exit(StringEscapeUtils.escapeHtml4(stringToWrite).replaceAll("'", "&apos;"));
    	} catch (Exception e) {
    		return log.exit("");
    	}
    }
    /**
     * The {@code RequestParameters} holding the parameters of the current query 
     * being treated.
     */
    protected final RequestParameters requestParameters;
    /**
     * TODO comment, what is this ?
     */
    private int uniqueId;

    /**
     * Constructor 
     * 
     * @param response          A {@code HttpServletResponse} that will be used to display the 
     *                          page to the client
     * @param requestParameters The {@code RequestParameters} that handles the parameters of the 
     *                          current request.
     * @param prop              A {@code BgeeProperties} instance that contains the properties
     *                          to use.
     * 
     * @throws IOException      If there is an issue when trying to get or to use the
     *                          {@code PrintWriter} 
     */
    public HtmlParentDisplay(HttpServletResponse response, RequestParameters requestParameters, 
            BgeeProperties prop) throws IOException {
        super(response,prop);
        this.requestParameters = requestParameters;
        this.uniqueId = 0;
    }

    /**
     * @return An {@code int} TODO be more specific
     */
    protected int getUniqueId()
    {
        log.entry();
        //need to return 0 the first time this method is called;
        int idToReturn = this.uniqueId;
        this.uniqueId++;
        return log.exit(idToReturn);
    }

    public void emptyDisplay()
    {
        log.entry();
        this.sendHeaders(true);
        this.writeln("");
        log.exit();
    }
    //TODO: use an enum rather than a String page?
    //TODO: javadoc
    public void startDisplay(String page, String title)
    {
        log.entry(page, title);
        this.sendHeaders(false);
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
        this.writeln("<meta name='dcterms.rights' content='Bgee copyright 2007/2015 UNIL' />");
        this.writeln("<link rel='shortcut icon' type='image/x-icon' href='"
                +this.prop.getImagesRootDirectory()+"favicon.ico'/>");
        this.includeCss(); // load default css files, and css files specific of a view 
                           // (views must override this method if needed)
        this.includeJs();  // load default js files, and css files specific of a view 
                           // (views must override this method if needed)
        //google analytics
        this.writeln("<script>");
        this.writeln("(function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){");
        this.writeln("(i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),");
        this.writeln("m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)");
        this.writeln("})(window,document,'script','//www.google-analytics.com/analytics.js','ga');");
        this.writeln("ga('create', 'UA-18281910-2', 'auto');");
        this.writeln("ga('send', 'pageview');");
        this.writeln("</script>");
        
        this.writeln("</head>");
        
        this.writeln("<body>");
        this.writeln("<noscript>Sorry, your browser does not support JavaScript!</noscript>");
        this.writeln("<div id='bgee_top'><a id='TOP'></a></div>");
        this.writeln("<div id='sib_container'>");
        this.displayBgeeMenu();
        this.writeln("<div id='sib_body'>");

        log.exit();
    }

    /**
     * Display the end of the page
     */
    public void endDisplay()
    {
        log.entry();

        this.writeln("</div>");
        this.writeln("<footer>");
        this.writeln("<div id='sib_footer_content'>");
        this.writeln("<a href='http://www.isb-sib.ch'>SIB Swiss Institute of Bioinformatics</a>");
        this.writeln("<div id='sib_footer_right'>");
        this.writeln("<a href='#TOP' id='sib_footer_gototop'>"
                + "<span style='padding-left: 10px'>Back to the Top</span></a>");
        this.writeln("</div>");
        this.writeln("</div>");
        this.writeln("</footer>");
        this.writeln("</div>");
        
        this.writeln("</body>");
        this.writeln("</html>");
        log.exit();
    }

    //TODO: this is not a menu, this is a header...
    public void displayBgeeMenu() {
        log.entry();
        this.writeln("<header>");
        
        // Bgee logo
        this.writeln("<a href='" + this.prop.getBgeeRootDirectory() + "' title='Go to Bgee home page'>"
                + "<img id='sib_other_logo' src='"+this.prop.getImagesRootDirectory()+"bgee_logo.png' "
                + "title='Bgee: a dataBase for Gene Expression Evolution' "
                + "alt='Bgee: a dataBase for Gene Expression Evolution' />"
                + "</a>");
    
        // Title
        //TODO: change this hardcoded 'Release 13 download page', provide it as argument
        this.writeln("<h1>Bgee: Gene Expression Evolution</h1>"
                //+ "<h2>Release 13 download page</h2>"
                );
    
        // SIB logo
        this.writeln("<a href='http://www.isb-sib.ch/' target='_blank' title='Link to the SIB Swiss Institute of Bioinformatics'>"
                + "<img id='sib_logo' src='"+this.prop.getImagesRootDirectory()+"sib_logo_141x75.png' "
                + "title='Bgee is part of the SIB Swiss Institute of Bioinformatics' "
                + "alt='SIB Swiss Institute of Bioinformatics' />"
                + "</a>");
    
        this.writeln("</header>");
        log.exit();
    }

    @Override
	public void sendHeaders(boolean ajax)
	{
	    log.entry(ajax);
		if (this.response == null) {
			log.exit(); return;
		}
		if (!this.headersAlreadySent) {
			this.response.setContentType("text/html");
			log.trace("Set content type text/html");
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

    /**
     * Send the header in case of HTTP 503 error
     */
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
    /**
     * Send the header in case of HTTP 400 error
     */
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
    /**
     * Send the header in case of HTTP 404 error
     */
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
    /**
     * Send the header in case of HTTP 500 error
     */
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
    /**
     * TODO comment
     */
    protected String displayHelpLink(String cat, String display)
    {
        log.entry(cat, display);
        return log.exit("<span class='help'><a href='#' class='help|" + 
                cat + "'>" + display + "</a></span>");
    }
    /**
     * TODO comment
     */
    protected String displayHelpLink(String cat)
    {
        log.entry(cat);
        return log.exit(this.displayHelpLink(cat, "[?]"));
    }

    /**
     * Method that loads the javascript files. Has to be override by a child class that needs
     * custom javascripts. Don't forget to call super.includeJs() at the beginning of the overridden
     * method, unless it is a special page that does not use the standard js.
     */
    protected void includeJs(){
        this.includeJs("lib/jquery.min.js");
        this.includeJs("lib/jquery.visible.js");
        this.includeJs("lib/jquery-ui.min.js");
        this.includeJs("common.js");
        this.includeJs("requestparameters.js");
        this.includeJs("urlparameters.js");
//        this.includeJs("bgeeproperties.js");
    }
    /**
     * Method that loads the provided javascript file.
     * <strong>It should be called only within a {@link #includeJs()} method, whether overridden 
     * or not.</strong>.
     * @param filename  The name of the file to load
     */
    protected void includeJs(String filename){
        log.entry(filename);
        this.writeln("<script type='text/javascript' src='"+
                this.prop.getJavascriptFilesRootDirectory()+filename+"'></script>");
        log.exit();
    }
    /**
     * Method that loads the css files. Has to be override by a child class that needs
     * custom css. Don't forget to call super.includeCss() at the beginning of the overridden
     * method, unless it is a special page that does not use the standard css.
     */
    protected void includeCss(){
        this.includeCss("bgee.css"); 
    }
    /**
     * Method that loads the provided css file.
     * <strong>It should be called only within a {@link #includeCss()} method, whether overridden 
     * or not.</strong>
     * @param filename  The name of the file to load
     */
    protected void includeCss(String filename){
        log.entry(filename);
        this.writeln("<link rel='stylesheet' type='text/css' href='"
                + this.prop.getCssFilesRootDirectory() + filename + "'/>");
        log.exit();
    }

    /**
     * Return a new {@code RequestParameters} object to be used to generate URLs. 
     * This new {@code RequestParameters} will use the same {@code URLParameters} 
     * as those returned by {@link #requestParameters} when calling 
     * {@link #getUrlParametersInstance()}, and the {@code BgeeProperties} {@link #prop}. 
     * Also, parameters will be URL encoded, and parameter separator will be {@code &amp;}.
     * 
     * @return  A newly created RequestParameters object.
     */
    protected RequestParameters getNewRequestParameters() {
        log.entry();
        return log.exit(new RequestParameters(this.requestParameters.getUrlParametersInstance(), 
                this.prop, true, "&amp;"));
    }
}
