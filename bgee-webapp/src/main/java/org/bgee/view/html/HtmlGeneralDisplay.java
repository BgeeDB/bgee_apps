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
 * @author Valentine Rech de Laval
 * @version Bgee 13, June 2015
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

    @Override
    public void displayHomePage() {
        log.entry();
        this.startDisplay("Welcome on Bgee: a dataBase for Gene Expression Evolution");

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

        RequestParameters urlCallDocGenerator = this.getNewRequestParameters();
        urlCallDocGenerator.setPage(RequestParameters.PAGE_DOCUMENTATION);
        urlCallDocGenerator.setAction(RequestParameters.ACTION_DOC_CALL_DOWLOAD_FILES);
        

        this.writeln("<div id='feature_list'>");

        this.writeln(this.getFeatureDownloadLogos());

        this.writeln(this.getMainDocumentationLogo());

        this.writeln("</div>");

        this.writeln("<h2>News</h2>" +
                     "<span class='header_details'>(features are being added incrementally)</span>");
        this.writeln("<div id='bgee_news' class='bgee_section'>");
        this.writeln("<ul><li>2015-06-08: release of Bgee release 13.1: "
                + "<ul>"
                + "<li>Update of the website interfaces.</li>"
                + "<li><a href='" + urlDownloadProcValuesGenerator.getRequestURL() 
                + "'>New download page</a> providing processed expression values.</li>"
                + "<li>Addition of mouse <i>in situ</i> data from MGI, see "
                + "<a href='" + urlDownloadCallsGenerator.getRequestURL() + "#id10090"
                + "'>mouse data</a>.</li>"
                + "<li>Differential expression data have been added for "
                + "<a href='" + urlDownloadCallsGenerator.getRequestURL() + "#id7955"
                + "'>zebrafish</a>, <a href='" + urlDownloadCallsGenerator.getRequestURL() + "#id9598"
                + "'>chimpanzee</a>, <a href='" + urlDownloadCallsGenerator.getRequestURL() + "#id9593"
                + "'>gorilla</a>, and <a href='" + urlDownloadCallsGenerator.getRequestURL() + "#id13616"
                + "'>opossum</a>.</li>"
                + "<li>Addition of new multi-species differential expression data, see "
                + "for instance <a href='" + urlDownloadCallsGenerator.getRequestURL() + "#id9598_9544"
                + "'>chimpanzee/macaque comparison</a>.</li>"
                + "<li>New format to provide gene orthology information in multi-species files, "
                + "see for instance <a href='" + urlCallDocGenerator.getRequestURL() + "#oma_hog"
                + "'>OMA Hierarchical orthologous groups documentation</a>.</li>"
                + "<li>Removal of data incorrectly considered as normal in <i>C. elegans</i>, "
                + "see <a href='" + urlDownloadCallsGenerator.getRequestURL() + "#id6239"
                + "'>worm data</a>.</li>"
                + "<li>Improved filtering of propagated no-expression calls. As a result, "
                + "complete expression calls files do not contain invalid conditions anymore.</li>"
                + "<li>Filtering of invalid developmental stages for differential expression analyses.</li>"
                + "</ul></li>");
        this.writeln("<li>2015-04-16: release of the multi-species " +
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
                true, "Bgee 12 home page", "Bgee 12", 
                this.prop.getLogoImagesRootDirectory() + "bgee12_logo.png", null));
        this.writeln("</div>");

        this.endDisplay();
        log.exit();
    }

    @Override
    public void displayAbout() {
        log.entry();
        this.startDisplay("Information about Bgee: a dataBase for Gene Expression Evolution");

        this.writeln("<h1>What is Bgee?</h1>");

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
