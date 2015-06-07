package org.bgee.view.html;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.BgeeProperties;
import org.bgee.controller.RequestParameters;
import org.bgee.view.GeneralDisplay;

/**
 * HTML View for the general category display
 * 
 * @author Mathieu Seppey
 * @author Frederic Bastian
 * @version Bgee 13, Aug 2014
 * @since Bgee 13
 */
public class HtmlGeneralDisplay extends HtmlParentDisplay implements GeneralDisplay {

    private final static Logger log = LogManager.getLogger(HtmlGeneralDisplay.class.getName());

    /**
     * Constructor
     * 
     * @param response          A {@code HttpServletResponse} that will be used to display the 
     *                          page to the client
     * @param requestParameters The {@code RequestParameters} that handles the parameters of the 
     *                          current request.
     * @param prop              A {@code BgeeProperties} instance that contains the properties
     *                          to use.
     * @param factory           The {@code HtmlFactory} that instantiated this object.
     * @throws IOException      If there is an issue when trying to get or to use the
     *                          {@code PrintWriter} 
     */
    public HtmlGeneralDisplay(HttpServletResponse response, RequestParameters requestParameters,
            BgeeProperties prop, HtmlFactory factory) throws IOException {
        super(response, requestParameters, prop, factory);
    }

    /**
     * Display the output expected in the case of a HTTP error 503
     */
    public void serviceUnavailable() {
        log.entry();
        this.sendServiceUnavailableHeaders();

        this.startDisplay("unavailable", 
                "Service unavailable for maintenance");

        this.writeln("<p class='alert'>Due to technical problems, Bgee is currently unavailable. " +
                "We are working to restore Bgee as soon as possible. " +
                "We apologize for any inconvenience.</p>");

        this.endDisplay();
        log.exit();
    }

    @Override
    public void displayHomePage() {
        log.entry();
        this.startDisplay("home", 
                "Welcome on Bgee: a dataBase for Gene Expression Evolution");

        //TODO: manage the version either from database, or from bgee-webapp.properties file.
        this.writeln("<h1>Welcome on the latest release of Bgee, Bgee release 13.1</h1>");
        
        RequestParameters urlDownloadGenerator = this.getNewRequestParameters();
        urlDownloadGenerator.setPage(RequestParameters.PAGE_DOWNLOAD);
        
        RequestParameters urlDocGenerator = this.getNewRequestParameters();
        urlDocGenerator.setPage(RequestParameters.PAGE_DOCUMENTATION);

        RequestParameters urlDownloadProcValuesGenerator = this.getNewRequestParameters();
        urlDownloadProcValuesGenerator.setPage(RequestParameters.PAGE_DOWNLOAD);
        urlDownloadProcValuesGenerator.setAction(RequestParameters.ACTION_DOWLOAD_PROC_VALUE_FILES);

        RequestParameters urlDownloadCallsGenerator = this.getNewRequestParameters();
        urlDownloadCallsGenerator.setPage(RequestParameters.PAGE_DOWNLOAD);
        urlDownloadCallsGenerator.setAction(RequestParameters.ACTION_DOWLOAD_CALL_FILES);
        

        this.writeln("<div id='feature_list'>");

        this.writeln(this.getFeatureDownloadLogos());

        this.writeln(this.getMainDocumentationLogo());

        this.writeln("</div>");

        this.writeln("<h2>News</h2>" +
                     "<span class='header_details'>(features are being added incrementally)</span>");
        this.writeln("<div id='bgee_news' class='bgee_section'>");
        this.writeln("<ul><li>2015-04-16: release of the multi-species " +
                "differential expression data (across anatomy) for 6 groups, see <a href='" + 
                urlDownloadGenerator.getRequestURL() + "' " + "title='Bgee download page'>" +
                "download page</a>.</li>");
        this.writeln("<li>2015-03-03: release of the single-species " +
                "differential expression data for 11 species, see <a href='" + 
                urlDownloadGenerator.getRequestURL() + "' " + "title='Bgee download page'>" +
                "download page</a>.</li>");
        this.writeln("<li>2014-12-19: release of the single-species " +
                "expression data for 17 species, see <a href='" + 
                urlDownloadGenerator.getRequestURL() + "' " + "title='Bgee download page'>" +
                "download page</a>.</li></ul>");
        this.writeln("</div>"); // end home_info

        this.writeln("<p id='bgee_more_info'>" +
                       "The complete website remains available for the previous release of Bgee:</p>");
        this.writeln("<div id='feature_list'>");
        this.writeln(HtmlParentDisplay.getSingleFeatureLogo("http://bgee.org/bgee/bgee", 
                "Bgee 12 home page", "Bgee 12", 
                this.prop.getImagesRootDirectory() + "logo/bgee12_logo.png", null));
        this.writeln("</div>");

        this.endDisplay();
        log.exit();
    }

    @Override
    public void displayAbout() {
        log.entry();
        this.startDisplay("home", 
                "Information about Bgee: a dataBase for Gene Expression Evolution");

        this.writeln("<h1>What is Bgee?</h1>");

        this.endDisplay();
        log.exit();
    }

    @Override
    public void displayRequestParametersNotFound(String key) {
        log.entry(key);
        this.sendBadRequestHeaders();
        this.startDisplay("", "Request parameters not found");
        this.writeln("<p class='alert'>Woops, something wrong happened</p>");
        this.writeln("<p>You tried to use in your query some parameters supposed to be stored on our server, " +
                "but we could not find them. Either the key you used was wrong, " +
                "or we were not able to save these parameters. " +
                "Your query should be rebuilt by setting all the parameters from scratch. " +
                "We apologize for any inconvenience.</p>");
        this.endDisplay();
        log.exit();
    }

    @Override
    public void displayPageNotFound(String message) {
        log.entry(message);
        this.sendPageNotFoundHeaders();
        this.startDisplay("", "404 not found");
        this.writeln("<p class='alert'>Woops, something wrong happened</p>");
        this.writeln("<p>404 not found. We could not understand your query, see details below:</p> " +
                "<p>" + htmlEntities(message) + "</p>");
        this.endDisplay();
        log.exit();
    }

    @Override
    public void displayUnexpectedError() {
        log.entry();
        this.sendInternalErrorHeaders();
        this.startDisplay("", "500 internal server error");
        this.writeln("<p class='alert'>Woops, something wrong happened</p>");
        this.writeln("<p>500 internal server error. " +
                "An error occurred on our side. This error was logged and will be investigated. " +
                "We apologize for any inconvenience.</p>");
        this.endDisplay();
        log.exit();
    }

    @Override
    public void displayMultipleParametersNotAllowed(String message) {
        log.entry(message);
        this.sendBadRequestHeaders();
        this.startDisplay("", "Multiple values not allowed");
        this.writeln("<p class='alert'>Woops, something wrong happened</p>");
        this.writeln("<p>"+ message
                + "</p>"
                + "Please check the URL and retry.</p>");
        this.endDisplay();	
        log.exit();
    }

    @Override
    public void displayRequestParametersNotStorable(String message) {
        log.entry(message);
        this.sendBadRequestHeaders();
        this.startDisplay("", "A parameter is not storable or the key is missing");
        this.writeln("<p class='alert'>Woops, something wrong happened</p>");
        this.writeln("<p>"+ message
                + "</p>");
        this.endDisplay();
        log.exit();
    }

    @Override
    public void displayWrongFormat(String message) {
        log.entry(message);
        this.sendBadRequestHeaders();
        this.startDisplay("", "Wrong format for a parameter");
        this.writeln("<p class='alert'>Woops, something wrong happened</p>");
        this.writeln("<p>"+ message
                + "</p>"
                + "Please check the URL and retry.</p>");
        this.endDisplay();  
        log.exit();
    }

    @Override
    protected void includeCss() {
        log.entry();
        super.includeCss();
        this.includeCss("general.css");
        log.exit();
    }
}
