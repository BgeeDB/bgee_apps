package org.bgee.view.html;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.BgeeProperties;
import org.bgee.controller.RequestParameters;
import org.bgee.view.DocumentationDisplay;

/**
 * This class displays the documentation for the HTML view.
 *
 * @author  Frederic Bastian
 * @author  Valentine Rech de Laval
 * @version Bgee 14, June 2018
 * @since   Bgee 13, Mar. 2015
 */
public class HtmlDocumentationDisplay extends HtmlParentDisplay implements DocumentationDisplay {

    private final static Logger log = LogManager.getLogger(HtmlDocumentationDisplay.class.getName());

    //*******************************************************
    // MISCELLANEOUS STATIC METHODS
    //*******************************************************
    /**
     * Provide explanations about how to retrieve correct genes in Ensembl, when we use 
     * the genome of another species for a given species. For instance, for bonobo we use 
     * the chimpanzee genome, and replace the 'ENSPTRG' prefix of chimp genes by 
     * the prefix 'PPAG'.
     * 
     * @return  A {@code String} formatted in HTML, providing the explanation.
     */
    //TODO: this needs to be generated automatically from the species table in database.
    public static String getGenomeMappingExplanation() {
        log.entry();
        return log.exit("Please note that "
        + "for <i>P. paniscus</i> (bonobo) we use <i>P. troglodytes</i> genome (chimpanzee), "
        + "and that for <i>P. pygmaeus</i> (Bornean orangutan) we use <i>P. abelii</i> genome "
        + "(Sumatran orangutan). Only for those species (bonobo and Bornean orangutan), "
        + "we modify the Ensembl gene IDs, to ensure that we provide unique gene identifiers "
        + "over all species. It is therefore necessary, to obtain correct Ensembl gene IDs "
        + "for those species, to replace gene ID prefix 'PPAG' with 'ENSPTRG', "
        + "and 'PPYG' prefix with 'ENSPPYG'.");
    }
    /**
     * @return  A {@code String} that is a general introduction to the concept 
     *          of presence/absence calls of expression, to be used in various places 
     *          of the documentation, in HTML, and HTML escaped if necessary.
     */
    public static String getExprCallExplanation() {
        log.entry();
        return log.exit("<p>Bgee provides calls of presence/absence of expression. Each call "
                + "corresponds to a unique combination of a gene, an anatomical entity, "
                + "and a life stage, with reported presence or absence of expression. "
                + "Life stages describe development and aging. Only \"normal\" "
                + "expression is considered in Bgee (i.e., no treatment, no disease, "
                + "no gene knock-out, etc.). Bgee collects data from different types, "
                + "from different studies, in different organisms, and provides a summary "
                + "from all these data as unique calls <code>gene - anatomical entity - "
                + "developmental stage</code>, with confidence information, notably taking "
                + "into account potential conflicts.</p>"
                + "<p>Calls of presence/absence of expression are very similar to the data "
                + "that can be reported using <i>in situ</i> hybridization methods; Bgee applies "
                + "dedicated statistical analyses to generate such calls from EST, Affymetrix, "
                + "and RNA-Seq data, with confidence information, and also collects "
                + "<i>in situ</i> hybridization calls from model organism databases. "
                + "This offers the possibility to aggregate and compare these calls of "
                + "presence/absence of expression between different experiments, "
                + "different data types, and different species, and to benefit from both "
                + "the high anatomy coverage provided by low-throughput methods, "
                + "and the high genomic coverage provided by high-throughput methods.</p>");
    }
    /**
     * @return  A {@code String} that is a general introduction to the concept 
     *          of over-/under-expression calls, to be used in various places 
     *          of the documentation, in HTML, and HTML escaped if necessary.
     */
    public static String getDiffExprCallExplanation() {
        log.entry();
        return log.exit("<p>Bgee provides calls of over-/under-expression. A call "
                + "corresponds to a gene, with significant variation of "
                + "its level of expression, in an anatomical entity "
                + "during a developmental stage, as compared to, either: i) other anatomical entities "
                + "at the same (broadly defined) developmental stage (over-/under-expression "
                + "across anatomy); "
                + "ii) the same anatomical entity at different (precise) developmental stages "
                + "(over-/under-expression across life stages). "
                + "These analyses of differential expression are performed using Affymetrix "
                + "and RNA-Seq experiments with at least 3 suitable conditions (anatomical entity/"
                + "developmental stage), and at least 2 replicates for each; as for all data in Bgee, "
                + "only \"normal\" expression is considered (i.e., no treatment, no disease, "
                + "no gene knock-out, etc.). </p>"
                + "<p>Bgee runs all possible differential expression analyses for each experiment "
                + "independently, then collects all results and provides a summary "
                + "as unique calls <code>gene - anatomical entity - developmental stage</code>, "
                + "with confidence information, and conflicts within each data type resolved "
                + "using a voting system weighted by p-values (conflicts between different "
                + "data types are treated differently). This offers the possibility "
                + "to aggregate and compare these calls between different experiments, "
                + "different data types, and different species. </p>");
    }

    
    
    //************************************
    // Instance attributes and methods
    //************************************
    
    /**
     * A {@code HtmlDocumentationCallFile} used to write the documentation 
     * about call download files (see {@link #displayCallDownloadFileDocumentation()}).
     */
    private final HtmlDocumentationCallFile callFileDoc;
    /**
     * A {@code HtmlDocumentationRefExprFile} used to write the documentation 
     * about ref. expression download files (see {@link #displayRefExprDownloadFileDocumentation()}).
     */
    private final HtmlDocumentationRefExprFile refExprFileDoc;
    /**
     * A {@code HtmlDocumentationTopAnat} used to write the documentation 
     * about TopAnat (see {@link #displayTopAnatDocumentation()}).
     */
    private final HtmlDocumentationTopAnat topAnatDoc;

    /**
     * A {@code HtmlDocumentationDataSets} used to write the documentation 
     * about data sets in Bgee (see {@link #displayDataSets()}).
     */
    private final HtmlDocumentationDataSets dataSetsDoc;

    /**
     * Default constructor.
     *
     * @param response          A {@code HttpServletResponse} that will be used to display the 
     *                          page to the client.
     * @param requestParameters A {@code RequestParameters} handling the parameters of the 
     *                          current request, to determine the requested displayType, 
     *                          and for display purposes.
     * @param prop              A {@code BgeeProperties} instance that contains the properties
     *                          to use.
     * @param factory           The {@code HtmlFactory} that instantiated this object.
     * @throws IOException      If there is an issue when trying to get or to use the
     *                          {@code PrintWriter}.
     */
    public HtmlDocumentationDisplay(HttpServletResponse response,
            RequestParameters requestParameters, BgeeProperties prop, HtmlFactory factory) 
                    throws IOException {
        this(response, requestParameters, prop, factory, null, null, null, null);
    }
    /**
     * Constructor providing other dependencies.
     *
     * @param response          A {@code HttpServletResponse} that will be used to display the 
     *                          page to the client.
     * @param requestParameters A {@code RequestParameters} handling the parameters of the 
     *                          current request, to determine the requested displayType, 
     *                          and for display purposes.
     * @param prop              A {@code BgeeProperties} instance that contains the properties
     *                          to use.
     * @param factory           The {@code HtmlFactory} that instantiated this object.
     * @param callFileDoc       A {@code HtmlDocumentationCallFile} used to write the documentation 
     *                          about call download files (see {@link 
     *                          #displayCallDownloadFileDocumentation()}). If {@code null}, 
     *                          the default implementation will be used 
     *                          ({@link HtmlDocumentationCallFile}).
     * @param refExprFileDoc    A {@code HtmlDocumentationRefExprFile} used to write the documentation 
     *                          about ref. expression download files (see {@link 
     *                          #displayRefExprDownloadFileDocumentation()}). If {@code null}, 
     *                          the default implementation will be used 
     *                          ({@link HtmlDocumentationRefExprFile}).
     * @throws IOException      If there is an issue when trying to get or to use the
     *                          {@code PrintWriter}.
     */
    public HtmlDocumentationDisplay(HttpServletResponse response,
            RequestParameters requestParameters, BgeeProperties prop, HtmlFactory factory,
            HtmlDocumentationCallFile callFileDoc, HtmlDocumentationRefExprFile refExprFileDoc,
            HtmlDocumentationTopAnat topAnatDoc, HtmlDocumentationDataSets dataSetsDoc) 
                    throws IOException {
        super(response, requestParameters, prop, factory);
        if (callFileDoc == null) {
            this.callFileDoc = 
                    new HtmlDocumentationCallFile(response, requestParameters, prop, factory);
        } else {
            this.callFileDoc = callFileDoc;
        }
        if (refExprFileDoc == null) {
            this.refExprFileDoc = 
                    new HtmlDocumentationRefExprFile(response, requestParameters, prop, factory);
        } else {
            this.refExprFileDoc = refExprFileDoc;
        }
        if (topAnatDoc == null) {
            this.topAnatDoc = new HtmlDocumentationTopAnat(response, requestParameters, prop, factory);
        } else {
            this.topAnatDoc = topAnatDoc;
        }
        if (dataSetsDoc == null) {
            this.dataSetsDoc = new HtmlDocumentationDataSets(response, requestParameters, prop, factory);
        } else {
            this.dataSetsDoc = dataSetsDoc;
        }
    }
    
    @Override
    public void displayDocumentationHomePage() {
        log.entry();

        String version = this.getWebAppVersion();
        String releaseDoc = "Bgee ";
        if (version != null) {
            releaseDoc += "release " + version + " ";
        }
        releaseDoc += "documentation";

        this.startDisplay(releaseDoc + " home page");

        this.writeln("<h1>" + releaseDoc + "</h1>");

        this.writeln("<div class='feature_list'>");

        this.writeln(this.getFeatureDocumentationLogos());

        this.writeln("</div>");
        
        this.endDisplay();

        log.exit();
    }

    /**
     * Get the feature logos of the documentation page, as HTML 'div' elements.
     *
     * @return  A {@code String} that is the feature documentation logos as HTML 'div' elements,
     *          formated in HTML and HTML escaped if necessary.
     */
    private String getFeatureDocumentationLogos() {
        log.entry();
        
        RequestParameters urlCallFilesGenerator = this.getNewRequestParameters();
        urlCallFilesGenerator.setPage(RequestParameters.PAGE_DOCUMENTATION);
        urlCallFilesGenerator.setAction(RequestParameters.ACTION_DOC_CALL_DOWLOAD_FILES);
        
        RequestParameters urlTopAnatGenerator = this.getNewRequestParameters();
        urlTopAnatGenerator.setPage(RequestParameters.PAGE_DOCUMENTATION);
        urlTopAnatGenerator.setAction(RequestParameters.ACTION_DOC_TOP_ANAT);

        RequestParameters urlSourcesGenerator = this.getNewRequestParameters();
        urlSourcesGenerator.setPage(RequestParameters.PAGE_SOURCE);

        StringBuilder logos = new StringBuilder(); 

        //TODO update image when top anat logo is created
        logos.append(HtmlParentDisplay.getSingleFeatureLogo(urlTopAnatGenerator.getRequestURL(), 
                false, "TopAnat documentation page", "TopAnat documentation", 
                this.prop.getBgeeRootDirectory() + this.prop.getLogoImagesRootDirectory() + "bgee_access_logo.png", null));

        logos.append(HtmlParentDisplay.getSingleFeatureLogo(urlCallFilesGenerator.getRequestURL(), 
                false, "Download file documentation page", "Download file documentation", 
                this.prop.getBgeeRootDirectory() + this.prop.getLogoImagesRootDirectory() + "download_logo.png", null));

        logos.append(HtmlParentDisplay.getSingleFeatureLogo("https://bgeedb.wordpress.com", 
                true, "Bgee blog", "Bgee blog", 
                this.prop.getBgeeRootDirectory() + this.prop.getLogoImagesRootDirectory() + "bgee_access_logo.png", null));

        logos.append(HtmlParentDisplay.getSingleFeatureLogo(urlSourcesGenerator.getRequestURL(), 
                false, "Data sources of Bgee", "Bgee data sources", 
                this.prop.getBgeeRootDirectory() + this.prop.getLogoImagesRootDirectory() + "bgee_access_logo.png", null));

        return log.exit(logos.toString());
    }

    //*******************************************************
    // DOCUMENTATION FOR CALL DOWNLOAD FILES 
    //*******************************************************
    @Override
    public void displayCallDownloadFileDocumentation() {
        log.entry();
        
        this.startDisplay("Expression call download file documentation");
        
        this.writeln("<div class='row'>");
        this.writeln("<div class='" + CENTERED_ELEMENT_CLASS + "'>");

        this.callFileDoc.writeDocumentation();
        
        this.writeln("</div>"); // close class
        this.writeln("</div>"); // close row

        this.endDisplay();

        log.exit();
    }
    @Override
    public void displayRefExprDownloadFileDocumentation() {
        log.entry();
        
        this.startDisplay(PROCESSED_EXPR_VALUES_PAGE_NAME + " download file documentation");
        
        this.writeln("<div class='row'>");
        this.writeln("<div class='" + CENTERED_ELEMENT_CLASS + "'>");

        this.refExprFileDoc.writeDocumentation();
        
        this.writeln("</div>"); // close class
        this.writeln("</div>"); // close row

        this.endDisplay();

        log.exit();
    }

    @Override
    public void displayTopAnatDocumentation() {
        log.entry();
        
        this.startDisplay("TopAnat documentation");
        
        this.writeln("<div class='row'>");
        this.writeln("<div class='" + CENTERED_ELEMENT_CLASS + "'>");
        
        this.topAnatDoc.writeDocumentation();
        
        this.writeln("</div>"); // close class
        this.writeln("</div>"); // close row

        this.endDisplay();

        log.exit();
    }

    @Override
    public void displayDataSets() {
        log.entry();

        this.startDisplay("Data sets into Bgee");

        this.writeln("<div class='row'>");
        this.writeln("<div class='" + CENTERED_ELEMENT_CLASS + "'>");

        this.dataSetsDoc.writeDocumentation();

        this.writeln("</div>"); // close CENTERED_ELEMENT_CLASS class
        this.writeln("</div>"); // close row

        this.endDisplay();

        log.exit();
    }

    //*******************************************************
    // COMMON METHODS
    //*******************************************************
//    /**
//     * @return  the {@code String} that is the link of the back to the top.
//     */
//    private String getBackToTheTopLink() {
//        log.entry();
//        return log.exit("<a class='backlink' href='#sectionname'>Back to the top</a>");
//    }

    @Override
    protected void includeCss() {
        log.entry();
        super.includeCss();
        //If you ever add new files, you need to edit bgee-webapp/pom.xml 
        //to correctly merge/minify them.
        this.includeCss("documentation.css");
        log.exit();
    }
}
