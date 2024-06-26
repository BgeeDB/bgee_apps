package org.bgee.view.html;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.text.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.BgeeProperties;
import org.bgee.controller.RequestParameters;
import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.file.SpeciesDownloadFile;
import org.bgee.model.species.Species;
import org.bgee.model.species.Taxon;
import org.bgee.view.ConcreteDisplayParent;
import org.bgee.view.JsonHelper;
import org.bgee.view.ViewFactory;

/**
 * Parent of all display for the HTML view.
 *
 * @author  Mathieu Seppey
 * @author  Frederic Bastian
 * @author  Valentine Rech de Laval
 * @author  Philippe Moret
 * @author  Sebastien Moretti
 * @version Bgee 14, July 2019
 * @since   Bgee 13, Jul. 2014
 */
public class HtmlParentDisplay extends ConcreteDisplayParent {

    private final static Logger log = LogManager.getLogger(HtmlParentDisplay.class.getName());

    /**
     * A {@code String} that is the page name of the 'annotations' resources page.
     */
    protected final static String ANNOTATIONS_PAGE_NAME = "Annotations";
    /**
     * A {@code String} that is the page name of the 'gene expression calls' download page.
     */
    protected final static String GENE_EXPR_CALLS_PAGE_NAME = "Gene expression calls";
    /**
     * A {@code String} that is the page name of the 'dumps' download page.
     */
    protected final static String DUMPS_PAGE_NAME = "Data dumps";
    /**
     * A {@code String} that is the page name of the 'Ontologies' resources page.
     */
    protected final static String ONTOLOGIES_PAGE_NAME = "Ontologies";
    /**
     * A {@code String} that is the page name of the 'processed expression values' download page.
     */
    protected final static String PROCESSED_EXPR_VALUES_PAGE_NAME = "Processed expression values";
    /**
     * A {@code String} that is the page name of the 'R packages' resources page.
     */
    protected final static String R_PACKAGES_PAGE_NAME = "R packages";
    /**
     * A {@code String} that is the page name of the 'Source code' resources page.
     */
    protected final static String SOURCE_CODE_PAGE_NAME = "Source code";
    /**
     * A {@code String} that is the page name of the 'gene expression calls' download page.
     */
    protected final static String TOP_ANAT_PAGE_NAME = "TopAnat: Expression enrichment analysis";

    /**
     * A {@code String} to be used in {@code class} attribute.
     */
    protected static final String CENTERED_ELEMENT_CLASS =
            "col-xs-12 col-xs-offset-0 col-sm-offset-1 col-sm-10";

    /**
     * A {@code String} that is the URL of the Bioconductor BgeeDB R package.
     */
    protected static final String BGEEDB_R_PACKAGE_URL =
            "https://bioconductor.org/packages/BgeeDB/";

    /**
     * A {@code String} that is the URL of the Bioconductor BgeeCall R package.
     */
    protected static final String BGEECALL_R_PACKAGE_URL =
            "https://bioconductor.org/packages/BgeeCall/";

    /**
     * A {@code String} that is the URL of the container for BgeeCall and BgeeDB R packages
     */
    protected static final String R_PACKAGES_CONTAINER_URL =
            "https://hub.docker.com/r/bgeedb/bgee_r";

    /**
     * A {@code String} that is the URL of the Bgee GitHub.
     */
    protected static final String BGEE_GITHUB_URL = "https://github.com/BgeeDB";

    /**
     * A {@code String} that is the URL of the Bgee pipeline master branch in GitHub.
     */
    protected static final String MASTER_BGEE_PIPELINE_GITHUB_URL = BGEE_GITHUB_URL +
            "/bgee_pipeline/tree/master";

    /**
     * A {@code String} that is the URL of the Bgee pipeline develop branch in GitHub.
     */
    protected static final String DEVELOP_BGEE_PIPELINE_GITHUB_URL = BGEE_GITHUB_URL +
            "/bgee_pipeline/tree/develop";

    /**
     * A {@code String} to be used to build the URL to OBO terms.
     */
    protected static final String UBERON_ID_URL = "http://purl.obolibrary.org/obo/";

    /**
     * A {@code String} to be used to build the URL to NCBI taxonomy.
     */
    protected static final String NCBI_TAXONOMY_URL =
            "https://www.ncbi.nlm.nih.gov/Taxonomy/Browser/wwwtax.cgi?mode=Info&amp;id=";

    /**
     * A {@code String} that are the keywords defining Bgee.
     */
    protected static final String BGEE_KEYWORDS =
            "bgee, gene expression, evolution, ontology, anatomy, development, " +
            "evo-devo database, anatomical ontology, developmental ontology, gene expression evolution";
    /**
     * A {@code String} that is the description of Bgee.
     */
    protected static final String BGEE_DESCRIPTION =
            "Bgee is a database for retrieval and comparison of gene expression patterns "
            + "across multiple animal species. It provides an intuitive answer to the question "
            + "-where is a gene expressed?- and supports research in cancer and agriculture "
            + "as well as evolutionary biology.";

    /**
     * A {@code String} that is the URL of the licence CC0 of Creative Commons.
     */
    protected static final String LICENCE_CC0_URL =
            "https://creativecommons.org/publicdomain/zero/1.0/";

    /**
     * A {@code String} that is the ID of the human species.
     */
    private static final int HUMAN_SPECIES_ID = 9606;

    /**
     * A {@code String} that is the name of the 'EasyBgee' database.
     */
    protected final static String EASY_BGEE_NAME = "EasyBgee";

    /**
     * A {@code String} that is the value of the type tag describing Bgee creator
     * for Schema.org
     */
    protected final static String SCHEMA_CREATOR_TYPE_BGEE = "Person";

    /**
     * A {@code String} that is the value of the name tag describing Bgee name
     * for Schema.org
     */
    protected final static String SCHEMA_BGEE_NAME = "Bgee";

    /**
     * A {@code String} that is the value of the name tag describing Bgee creator
     * for Schema.org
     */
    protected final static String SCHEMA_CREATOR_NAME_BGEE = "The Bgee Team";

    /**
     * A {@code String} that is the list (HTLM tag {@code <ul>}) of condition parameters
     * with their description.
     */
    protected static final String COND_PARAM_DESC_LIST = "<ul class='doc_content'>"
            + "<li><span class='list_element_title'>anatomical entities only (by default) </span> "
            + "files contain one expression call for each unique pair of gene and anatomical entity. "
            + "If more than one developmental stage maps this unique pair, the resulting expression "
            + "call corresponds to summarized information coming from all developmental stages."
            + "</li>"
            + "<li><span class='list_element_title'>anatomical entities and developmental stages</span> "
            + "files contain one expression call for each unique gene, anatomical entity and developmental stage."
            + "</li>"
            + "</ul>";

    /**
     * Escape HTML entities in the provided {@code String}
     * @param stringToWrite A {@code String} that contains the HTML to escape
     * @return  The escaped HTML
     */
    protected static String htmlEntities(String stringToWrite) {
        log.traceEntry("{}", stringToWrite);
        try {
            return log.traceExit(StringEscapeUtils.escapeHtml4(stringToWrite).replaceAll("'", "&apos;"));
        } catch (Exception e) {
            log.catching(e);
            return log.traceExit("");
        }
    }

    /**
     * Helper method to get an html tag
     * @param name    A {@code String} representing the name of the element
     * @param content A {@code String} reprensenting the content of the element
     * @return The HTML code as {@code String}.
     */
     protected static String getHTMLTag(String name, String content) {
         log.traceEntry("{} - {}", name, content);
         return log.traceExit(getHTMLTag(name, null, content));
     }

     /**
      * Helper method to get an html tag with attributes set.
      * @param name          A {@code String} representing the name of the element
      * @param attributes    A {@code Map} where keys are attribute names and values are attribute values.
      * @return The HTML code as {@code String}
      */
     protected static String getHTMLTag(String name, Map<String, String> attributes) {
         log.traceEntry("{} - {}", name, attributes);
         return log.traceExit(getHTMLTag(name, attributes, null));
     }

     /**
      * Helper method to get an html tag
      * @param name          A {@code String} representing the name of the element
      * @param attributes    A {@code Map} where keys are attribute names and values are attribute values.
      *                      Can be {@code null} or empty.
      * @param content       A {@code String} representing the content of the element
      * @return The HTML code as {@code String}.
      */
     protected static String getHTMLTag(String name, Map<String, String> attributes, String content) {
         log.traceEntry("{} - {} - {}", name, attributes, content);

         StringBuilder sb = new StringBuilder();
         sb.append("<").append(name);
         if (attributes != null) {
             for (Map.Entry<String, String> attr: attributes.entrySet()) {
                 sb.append(" ").append(attr.getKey()).append("='").append(attr.getValue()).append("'");
             }
         }
         sb.append(">");
         if (StringUtils.isNotBlank(content)) {
             sb.append(content);
         }
         if (!name.equals("img")) {
             sb.append("</").append(name).append(">");
         }
         return log.traceExit(sb.toString());
     }

     /**
     * Get the single feature logo with a description as a HTML 'div' element.
     *
     * @param url           A {@code String} that is the URL of the link.
     * @param externalLink  A {@code boolean} defining whether the link points to a Bgee
     *                      internal URL, or an external resource (in which case a 'target'
     *                      attribute with the '_blank' value will be append to the link).
     *                      If {@code true}, the link points to an external resource.
     * @param title         A {@code String} that is the title and the alternate text of the image.
     * @param figcaption    A {@code String} that is the caption of the 'figure' element.
     * @param imgPath       A {@code String} that is the path of the image.
     * @param desc          A {@code String} that is the description of this feature.
     * @return              A {@code String} that is the single feature logo as a HTML 'div' element,
     *                      formated in HTML and HTML escaped if necessary.
     */
    protected static String getSingleFeatureLogo(
            String url, boolean externalLink, String title, String figcaption,
            String imgPath, String desc) {
        log.traceEntry("{} - {} - {} - {} - {} - {}", url, externalLink, title, figcaption, imgPath, desc);

        StringBuilder feature = new StringBuilder();
        feature.append("<div class='single_feature'>");
        feature.append("<a href='" + url + "' title='" + title + "'"
                + (externalLink ? " target='_blank' rel='noopener'" : "") + ">" +
                "<figure><img src='" + imgPath + "' alt='" + title + " logo' />" +
                "<figcaption>" + figcaption + "</figcaption>" +
                "</figure></a>");
        if (desc != null && !desc.isEmpty()) {
            feature.append("<p>" + desc + "</p>");
        }
        feature.append("</div>");

        return log.traceExit(feature.toString());
    }

    /**
      * The {@code JsonHelper} used to read/write variables into JSON.
      */
     private final JsonHelper jsonHelper;

     /**
      * A {@code String} defining the character encoding for encoding query strings.
      */
     private final String charEncoding;


     /**
      * Constructor providing the necessary dependencies, except the {@code JsonHelper},
      * that will thus be based on the default implementation.
      *
      * @param response          A {@code HttpServletResponse} that will be used to display the
      *                          page to the client
      * @param requestParameters The {@code RequestParameters} that handles the parameters of the
      *                          current request.
      * @param prop              A {@code BgeeProperties} instance that contains the properties
      *                          to use.
      * @param factory           The {@code HtmlFactory} that was used to instantiate this object.
      *
      * @throws IllegalArgumentException If {@code factory} is {@code null}.
      * @throws IOException              If there is an issue when trying to get or to use the
      *                                  {@code PrintWriter}
      * @see #HtmlParentDisplay(HttpServletResponse, RequestParameters, BgeeProperties, JsonHelper, HtmlFactory)
      */
     public HtmlParentDisplay(HttpServletResponse response, RequestParameters requestParameters,
             BgeeProperties prop, HtmlFactory factory) throws IllegalArgumentException, IOException {
         this(response, requestParameters, prop, null, factory);
     }
    /**
     * Constructor providing the necessary dependencies.
     *
     * @param response          A {@code HttpServletResponse} that will be used to display the
     *                          page to the client
     * @param requestParameters The {@code RequestParameters} that handles the parameters of the
     *                          current request.
     * @param prop              A {@code BgeeProperties} instance that contains the properties
     *                          to use.
     * @param jsonHelper        A {@code JsonHelper} used to read/write variables into JSON.
     * @param factory           The {@code HtmlFactory} that was used to instantiate this object.
     *
     * @throws IllegalArgumentException If {@code factory} is {@code null}.
     * @throws IOException              If there is an issue when trying to get or to use the
     *                                  {@code PrintWriter}
     */
    public HtmlParentDisplay(HttpServletResponse response, RequestParameters requestParameters,
            BgeeProperties prop, JsonHelper jsonHelper, HtmlFactory factory)
                    throws IllegalArgumentException, IOException {
        super(response, requestParameters, prop, factory);
        this.charEncoding = this.getRequestParameters().getCharacterEncoding();
        this.jsonHelper = jsonHelper;
    }

    @Override
    protected String getContentType() {
        log.traceEntry();
        return log.traceExit("text/html");
    }

    public void emptyDisplay() {
        log.traceEntry();
        this.sendHeaders();
        this.writeln("");
        log.traceExit();
    }

    /**
     * URL encode the provided {@code String}, with the character encoding used to generate URLs.
     *
     * @param stringToWrite A {@code String} to be encoded.
     * @return              The encoded {@code String}.
     */
    protected String urlEncode(String stringToWrite) {
        log.traceEntry("{}", stringToWrite);
        try {
            return log.traceExit(java.net.URLEncoder.encode(stringToWrite, this.charEncoding));
        } catch (Exception e) {
            log.catching(e);
            return log.traceExit("");
        }
    }

    /**
     * Display the start of the HTML page (common to all pages).
     *
     * @param title A {@code String} that is the title to be used for the page.
     */
    protected void startDisplay(String title) {
        log.traceEntry("{}", title);
        this.startDisplay(title, null);
        log.traceExit();
    }

    /**
     * Display the start of the HTML page (common to all pages).
     *
     * @param title             A {@code String} that is the title to be used for the page.
     * @param typeOfSchemaPage  A {@code String} that is the schema.org type of the page.
     *                          If {@code null}, no property will be set.
     */
    protected void startDisplay(String title, String typeOfSchemaPage) {
        log.traceEntry("{} - {}", title, typeOfSchemaPage);
        this.startDisplay(title, typeOfSchemaPage, null);
        log.traceExit();
    }


    /**
     * Display the start of the HTML page (common to all pages).
     *
     * @param title             A {@code String} that is the title to be used for the page.
     * @param typeOfSchemaPage  A {@code String} that is the schema.org type of the page.
     *                          If {@code null}, no property will be set.
     * @param description       A {@code String} that is a description specific to the page.
     *                          This description will be combined to the generic description
     *                          of Bgee. If {@code null}, only generic Bgee description will
     *                          be used.
     */
    protected void startDisplay(String title, String typeOfSchemaPage, String description) {
        log.traceEntry("{} - {} - {}", title, typeOfSchemaPage, description);
        this.sendHeaders();
        this.writeln("<!DOCTYPE html>");
        this.writeln("<html lang='en' class='no-js'>");
        this.writeln("<head>");
        this.writeln("<meta charset='UTF-8'>");
        this.writeln("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        this.writeln("<title>"+title+"</title>");
        this.write("<meta name='description' content='");
        if (description != null) {
            this.write(description);
        } else {
            this.write(BGEE_DESCRIPTION);
        }
        this.writeln("' />");

        this.writeln("<meta name='keywords' content='" + BGEE_KEYWORDS + "'/>");
        this.writeln("<meta name='dcterms.rights' content='Bgee copyright 2007/"
                + ZonedDateTime.now(ZoneId.of("Europe/Zurich")).getYear()
                + " UNIL' />");
        this.writeln("<link rel='shortcut icon' type='image/x-icon' href='"
                + this.prop.getBgeeRootDirectory() + this.prop.getImagesRootDirectory() + "favicon.ico'/>");
        this.includeCss(); // load default css files, and css files specific of a view
                           // (views must override this method if needed)
        this.includeJs();  // load default js files, and css files specific of a view
                           // (views must override this method if needed)
        //Matomo
        this.writeln("<script>");
        this.writeln("var _mtm = window._mtm = window._mtm || [];");
        this.writeln("_mtm.push({'mtm.startTime': (new Date().getTime()), 'event': 'mtm.Start'});");
        this.writeln("var d=document, g=d.createElement('script'), s=d.getElementsByTagName('script')[0];");
        this.writeln("g.async=true; g.src='https://matomo.sib.swiss/js/container_F5WPJc2X.js'; s.parentNode.insertBefore(g,s);");
        this.writeln("</script>");

        this.writeln("</head>");
        
        this.writeln("<body prefix='bs: https://bioschemas.org/'>");
        this.writeln("<noscript>Sorry, your browser does not support JavaScript!</noscript>");
        this.writeln("<div id='bgee_top'><span id='TOP'></span></div>");
        this.writeln("<div id='sib_container' class='container-fluid'>");
        //FIXME: I noticed that this header disappear in printed version
        this.displayBgeeHeader();
        this.displayArchiveMessage();
        this.displayWarningMessage();
        if (StringUtils.isBlank(typeOfSchemaPage)) {
            this.writeln("<div id='sib_body'>");
        } else {
            this.writeln("<div id='sib_body' typeof='schema:" + typeOfSchemaPage + "'>");
            this.writeln("    <meta property='schema:url' content='" +
                    this.getRequestParameters().getRequestURL() + "' />");

        }

        log.traceExit();
    }

    /**
     * Display the end of the HTML page (common to all pages).
     */
    protected void endDisplay() {
        log.traceEntry();

        RequestParameters urlPublication = this.getNewRequestParameters();
        urlPublication.setPage(RequestParameters.PAGE_PUBLICATION);

        this.writeln("</div>"); // close sib_body

        //FIXME: I noticed that this footer disappear in printed version
        this.writeln("<nav id='bgee_footer' class='navbar navbar-default'>");
        this.writeln("<div class='container-fluid'>");

        this.writeln("<ul class='nav navbar-nav'>");
        this.writeln("    <li><a href='https://www.sib.swiss' target='_blank' rel='noopener'>SIB Swiss Institute of Bioinformatics</a></li>");
        this.writeln("    <li>");
        this.writeln("        <a rel='license noopener' href='" + LICENCE_CC0_URL + "' target='_blank'>");
        this.writeln("            <img src='" + this.prop.getBgeeRootDirectory() + this.prop.getImagesRootDirectory() +
                                    "cc-zero.png' width='80' height='15' alt='CC0 license' />");
        this.writeln("        </a>");
        this.writeln("    </li>");
        this.writeln("</ul>");


        this.writeln("<ul class='nav navbar-nav navbar-right'>");
        this.writeln("<li><a class='js-tooltip js-copy' " +
                "data-copy='" + this.getRequestParameters().getStableRequestURL() + "' " +
                "data-toggle='tooltip' data-placement='top' " +
                "data-original-title='Click to copy to clipboard'>Copy permanent link</a>" +
                "</li>");
        this.writeln("<li><a href='" + urlPublication.getRequestURL() + "' title='Bgee publication page'>Cite us</a></li>");
        this.writeln("<li>" + this.getObfuscateHelpEmail() + "</li>");
        this.writeln("</ul>");

        this.writeln("</div>"); // close container
        this.writeln("</nav>"); // close bgee_footer nev

        this.writeln("<div id='bgee_privacy_banner'>");
        // This section is empty, it will be filled by common.js.
        this.writeln("    <p id='bgee_privacy_banner_text' class='col-sm-9 col-lg-10'></p>");
        this.writeln("    <a id='bgee_privacy_banner_accept' class='col-sm-3 col-lg-2'>Do not show this banner again</a>");
        this.writeln("</div>"); // close privacy-panel

        this.writeln("</div>"); // close sib_container

        this.writeln("</body>");
        this.writeln("</html>");
        log.traceExit();
    }

    /**
     * Display the Bgee header of the HTML page.
     */
    private void displayBgeeHeader() {
        log.traceEntry();

        RequestParameters urlTopAnat = this.getNewRequestParameters();
        urlTopAnat.setPage(RequestParameters.PAGE_TOP_ANAT);

        RequestParameters urlGeneSearch = this.getNewRequestParameters();
        urlGeneSearch.setPage(RequestParameters.PAGE_GENE);

        RequestParameters urlSpeciesList = this.getNewRequestParameters();
        urlSpeciesList.setPage(RequestParameters.PAGE_SPECIES);

        RequestParameters urlSparql = this.getNewRequestParameters();
        urlSparql.setPage(RequestParameters.PAGE_SPARQL);

        RequestParameters urlExprComp = this.getNewRequestParameters();
        urlExprComp.setPage(RequestParameters.PAGE_EXPR_COMPARISON);

        RequestParameters urlDownload = this.getNewRequestParameters();
        urlDownload.setPage(RequestParameters.PAGE_DOWNLOAD);

        RequestParameters urlDownloadProcValueFile = this.getNewRequestParameters();
        urlDownloadProcValueFile.setPage(RequestParameters.PAGE_DOWNLOAD);
        urlDownloadProcValueFile.setAction(RequestParameters.ACTION_DOWLOAD_PROC_VALUE_FILES);

        RequestParameters urlMySQLDumps = this.getNewRequestParameters();
        urlMySQLDumps.setPage(RequestParameters.PAGE_DOWNLOAD);
        urlMySQLDumps.setAction(RequestParameters.ACTION_DOWNLOAD_DUMPS);

        RequestParameters urlDownloadExprCallFiles = this.getNewRequestParameters();
        urlDownloadExprCallFiles.setPage(RequestParameters.PAGE_DOWNLOAD);
        urlDownloadExprCallFiles.setAction(RequestParameters.ACTION_DOWLOAD_CALL_FILES);

        // Request parameters for the Resources menu
        RequestParameters urlResourcesRPackages = this.getNewRequestParameters();
        urlResourcesRPackages.setPage(RequestParameters.PAGE_RESOURCES);
        urlResourcesRPackages.setAction(RequestParameters.ACTION_RESOURCES_R_PACKAGES);

        RequestParameters urlResourcesAnnotations = this.getNewRequestParameters();
        urlResourcesAnnotations.setPage(RequestParameters.PAGE_RESOURCES);
        urlResourcesAnnotations.setAction(RequestParameters.ACTION_RESOURCES_ANNOTATIONS);

        RequestParameters urlResourcesOntologies = this.getNewRequestParameters();
        urlResourcesOntologies.setPage(RequestParameters.PAGE_RESOURCES);
        urlResourcesOntologies.setAction(RequestParameters.ACTION_RESOURCES_ONTOLOGIES);

        RequestParameters urlResourcesSourceCode = this.getNewRequestParameters();
        urlResourcesSourceCode.setPage(RequestParameters.PAGE_RESOURCES);
        urlResourcesSourceCode.setAction(RequestParameters.ACTION_RESOURCES_SOURCE_CODE);

        RequestParameters urlDocDataSets = this.getNewRequestParameters();
        urlDocDataSets.setPage(RequestParameters.PAGE_DOCUMENTATION);
        urlDocDataSets.setAction(RequestParameters.ACTION_DOC_DATA_SETS);

        RequestParameters urlDocExprCallFiles = this.getNewRequestParameters();
        urlDocExprCallFiles.setPage(RequestParameters.PAGE_DOCUMENTATION);
        urlDocExprCallFiles.setAction(RequestParameters.ACTION_DOC_CALL_DOWLOAD_FILES);

        RequestParameters urlDocTopAnat = this.getNewRequestParameters();
        urlDocTopAnat.setPage(RequestParameters.PAGE_DOCUMENTATION);
        urlDocTopAnat.setAction(RequestParameters.ACTION_DOC_TOP_ANAT);

        RequestParameters urlFaq = this.getNewRequestParameters();
        urlFaq.setPage(RequestParameters.PAGE_DOCUMENTATION);
        urlFaq.setAction(RequestParameters.ACTION_DOC_FAQ);

        RequestParameters urlBgeeSources = this.getNewRequestParameters();
        urlBgeeSources.setPage(RequestParameters.PAGE_SOURCE);

        RequestParameters urlAbout = this.getNewRequestParameters();
        urlAbout.setPage(RequestParameters.PAGE_ABOUT);

        RequestParameters urlPrivacyPolicy = this.getNewRequestParameters();
        urlPrivacyPolicy.setPage(RequestParameters.PAGE_PRIVACY_POLICY);

        RequestParameters urlCollaborations = this.getNewRequestParameters();
        urlCollaborations.setPage(RequestParameters.PAGE_COLLABORATIONS);

        RequestParameters urlAnatSim = this.getNewRequestParameters();
        urlAnatSim.setPage(RequestParameters.PAGE_ANAT_SIM);

        RequestParameters urlPublications = this.getNewRequestParameters();
        urlPublications.setPage(RequestParameters.PAGE_PUBLICATION);

        // Navigation bar
        StringBuilder navbar = new StringBuilder();

        String navbarClass = this.prop.isArchive()? "navbar-archive": "navbar-default";

        navbar.append("<nav id='bgee-menu' class='navbar ").append(navbarClass).append("'>");

        // Brand and toggle get grouped for better mobile display
        navbar.append("<div class='navbar-header'>");
        navbar.append("<button type='button' class='navbar-toggle collapsed' data-toggle='collapse' "
                + "data-target='#bgee-navbar' aria-expanded='false'>");
        navbar.append("<span class='sr-only'>Toggle navigation</span>");
        navbar.append("<span class='icon-bar'></span>");
        navbar.append("<span class='icon-bar'></span>");
        navbar.append("<span class='icon-bar'></span>");
        navbar.append("</button>");
        navbar.append("<a class='navbar-brand' href='").append(this.getNewRequestParameters().getRequestURL())
                .append("' title='Go to Bgee home page'><img id='bgee_logo' src='")
                .append(this.prop.getBgeeRootDirectory()).append(this.prop.getLogoImagesRootDirectory())
                .append("bgee13_hp_logo.png' alt='Bgee logo'></a>");

        navbar.append("</div>"); //close navbar-header

        // Nav links
        navbar.append("<div id='bgee-navbar' class='collapse navbar-collapse'>");

        // Left nav links
        navbar.append("<ul class='nav navbar-nav'>");

        // Analysis
        navbar.append("<li class='dropdown'>");
        navbar.append("<a href='#' class='dropdown-toggle' data-toggle='dropdown' role='button' "
              + "aria-haspopup='true' aria-expanded='false'>Analysis <span class='caret'></span></a>");
        navbar.append("<ul class='dropdown-menu'>");
        navbar.append("<li><a title='TopAnat: Enrichment analyses of expression localization' href='")
                .append(urlTopAnat.getRequestURL()).append("'>").append(TOP_ANAT_PAGE_NAME)
                .append("</a></li>");
        navbar.append("<li><a href='").append(urlExprComp.getRequestURL())
                .append("' title='Expression comparison'>Expression comparison</a></li>");
        navbar.append("</ul>");
        navbar.append("</li>");

        // Search
        navbar.append("<li class='dropdown'>");
        navbar.append("<a href='#' class='dropdown-toggle' data-toggle='dropdown' role='button' "
              + "aria-haspopup='true' aria-expanded='false'>Search <span class='caret'></span></a>");
        navbar.append("<ul class='dropdown-menu'>");
        navbar.append("<li><a title='Gene search' href='").append(urlGeneSearch.getRequestURL())
                .append("'>Gene search</a></li>");
        navbar.append("<li><a href='").append(urlAnatSim.getRequestURL()).append("' >")
                .append("Anatomical homology search</a></li>");
        navbar.append("<li><a title='SPARQL endpoint' href='").append(urlSparql.getRequestURL())
                .append("'>SPARQL endpoint</a></li>");
        navbar.append("<li><a title='Species list' href='").append(urlSpeciesList.getRequestURL())
                .append("'>Species list</a></li>");
        navbar.append("</ul>");
        navbar.append("</li>");

        // Download
        navbar.append("<li class='dropdown'>");
        navbar.append("<a href='#' class='dropdown-toggle' data-toggle='dropdown' role='button' "
              + "aria-haspopup='true' aria-expanded='false'>Download <span class='caret'></span></a>");
        navbar.append("<ul class='dropdown-menu'>");
        navbar.append("<li><a href='").append(urlDownloadExprCallFiles.getRequestURL()).append("'>")
                .append(GENE_EXPR_CALLS_PAGE_NAME).append("</a></li>");
        navbar.append("<li><a href='").append(urlDownloadProcValueFile.getRequestURL()).append("'>")
                .append(PROCESSED_EXPR_VALUES_PAGE_NAME).append("</a></li>");
        navbar.append("<li><a href='").append(urlMySQLDumps.getRequestURL()).append("'>")
                .append(DUMPS_PAGE_NAME).append("</a></li>");
        navbar.append("</ul>");
        navbar.append("</li>");

        // Resources menu
        navbar.append("<li class='dropdown'>");
        navbar.append("<a href='#' class='dropdown-toggle' data-toggle='dropdown' role='button' "
              + "aria-haspopup='true' aria-expanded='false'>Resources <span class='caret'></span></a>");
        navbar.append("<ul class='dropdown-menu'>");
        navbar.append("<li><a href='").append(urlResourcesRPackages.getRequestURL())
                .append("' title='R packages'>")
                .append(R_PACKAGES_PAGE_NAME).append("</a></li>");
        navbar.append("<li><a title='SPARQL endpoint' href='").append(urlSparql.getRequestURL())
                .append("'>SPARQL endpoint</a></li>");
        navbar.append("<li><a href='").append(urlResourcesAnnotations.getRequestURL())
                .append("' title='Annotation resources'>")
                .append(ANNOTATIONS_PAGE_NAME).append("</a></li>");
        navbar.append("<li><a href='").append(urlResourcesOntologies.getRequestURL())
                .append("' title='Ontology resources'>")
                .append(ONTOLOGIES_PAGE_NAME).append("</a></li>");
        navbar.append("<li><a href='").append(urlResourcesSourceCode.getRequestURL())
                .append("' title='Source codes'>")
                .append(SOURCE_CODE_PAGE_NAME).append("</a></li>");
        navbar.append("</ul>");
        navbar.append("</li>");

        // Support menu
        navbar.append("<li class='dropdown'>");
        navbar.append("<a href='#' class='dropdown-toggle' data-toggle='dropdown' role='button' "
              + "aria-haspopup='true' aria-expanded='false'>Support <span class='caret'></span></a>");
        navbar.append("<ul class='dropdown-menu'>");
        navbar.append("<li><a title='See how to access to GTEx data' href='")
                .append(urlDocDataSets.getRequestURL()).append("'>GTEx in Bgee</a></li>");
        navbar.append("<li><a title='TopAnat documentation' href='").append(urlDocTopAnat.getRequestURL())
                .append("'>").append(TOP_ANAT_PAGE_NAME).append("</a></li>");
        navbar.append("<li><a title='Gene expression call files documentation' href='")
                .append(urlDocExprCallFiles.getRequestURL()).append("'>").append(GENE_EXPR_CALLS_PAGE_NAME)
                .append("</a></li>");
        navbar.append("<li><a href='").append(urlFaq.getRequestURL()).append("'>FAQ</a></li>");
        navbar.append("<li>").append(this.getObfuscateHelpEmail()).append("</li>");
        navbar.append("</ul>");
        navbar.append("</li>");

        // About
        navbar.append("<li class='dropdown'>");
        navbar.append("<a href='#' class='dropdown-toggle' data-toggle='dropdown' role='button' "
                + "aria-haspopup='true' aria-expanded='false'>About <span class='caret'></span></a>");
        navbar.append("<ul class='dropdown-menu'>");
        navbar.append("<li><a href='").append(urlAbout.getRequestURL()).append("'>About Bgee</a></li>");
        navbar.append("<li><a href='").append(urlCollaborations.getRequestURL())
                .append("'>Bgee collaborations</a></li>");
        navbar.append("<li><a href='").append(urlPublications.getRequestURL())
        .append("'>Bgee publications</a></li>");
        navbar.append("<li><a href='").append(urlBgeeSources.getRequestURL())
                .append("'>Bgee sources</a></li>");
        navbar.append("<li><a href='https://bgeedb.wordpress.com' target='_blank' rel='noopener'>Bgee blog</a></li>");
        navbar.append("<li><a href='").append(urlPrivacyPolicy.getRequestURL()).append("'>Bgee privacy notice</a></li>");
        navbar.append("</ul>");
        navbar.append("</li>");

        navbar.append("</ul>"); // close left nav links

        // Right nav links
        navbar.append("<ul class='nav navbar-nav navbar-right'>");

        // R package
        navbar.append("<li><a title='Download Bgee data with the BgeeDB R package' target='_blank' rel='noopener' href='" + BGEEDB_R_PACKAGE_URL + "'>" +
                "<img class='social-img' alt='R logo' src='")
                .append(this.prop.getLogoImagesRootDirectory()).append("r_logo.png'></a></li>");

        // Twitter
        navbar.append("<li><a title='Follow @Bgeedb on Twitter' target='_blank' rel='noopener' href='https://twitter.com/Bgeedb'>" +
                "<img class='social-img' alt='Twitter logo' src='").append(this.prop.getBgeeRootDirectory())
                .append(this.prop.getLogoImagesRootDirectory()).append("twitter_logo.png'></a></li>");

        // UNIL
        navbar.append("<li><a id='unil_brand' href='https://www.unil.ch/central/en/home.html' target='_blank' rel='noopener' " +
                "title='Link to the UNIL Université de Lausanne'><img src='")
                .append(this.prop.getBgeeRootDirectory()).append(this.prop.getLogoImagesRootDirectory())
                .append("unil_logo_noir.png' alt='UNIL Université de Lausanne' /></a></li>");

        // SIB
        navbar.append("<li><a id='sib_brand' href='https://www.sib.swiss' target='_blank' rel='noopener' " +
                "title='Link to the SIB Swiss Institute of Bioinformatics'><img src='")
                .append(this.prop.getBgeeRootDirectory()).append(this.prop.getLogoImagesRootDirectory())
                .append("sib_emblem.png' alt='SIB Swiss Institute of Bioinformatics' /></a></li>");

        navbar.append("</ul>");  // close right nav links

        navbar.append("</div>"); // close nav links

        navbar.append("</nav>"); // close navbar navbar-default

        this.writeln(navbar.toString());
        log.traceExit();
    }

    /**
     * Display a warning message on all pages if {@link BgeeProperties#getWarningMessage()}
     * returns a non-blank value (see {@link #prop}).
     */
    private void displayWarningMessage() {
        log.traceEntry();

        if (StringUtils.isNotBlank(this.prop.getWarningMessage())) {
            this.writeln("<div class='alert alert-warning'>" +
                this.prop.getWarningMessage() +
            "</div>");
        }

        log.traceExit();
    }

    /**
     * Display a archive message on all pages if {@link BgeeProperties#isArchive()}
     * returns {@code true} (see {@link #prop}).
     */
    private void displayArchiveMessage() {
        log.traceEntry();

        if (this.prop.isArchive()) {
            this.write("<div class='alert alert-danger'> This is an archived version of Bgee ");

            String version = this.getWebAppVersion();
            if (version != null) {
                this.write("(version " + version + ") ");
            }

            if (StringUtils.isNotBlank(this.prop.getBgeeCurrentUrl())) {
                this.write("<a href=' "+this.prop.getBgeeCurrentUrl()+"' class='alert-link'" +
                        " title='Access latest version of Bgee'>Access latest version of Bgee</a>");
            }

            this.writeln("</div>");
        }

        log.traceExit();
    }

    /**
     * @return  The {@code String} that is the HTML code of the contact link in menu.
     */
    //TODO move javascript in common.js
    private String getObfuscateHelpEmail() {
        return getObfuscateEmailLink("%43%6F%6E%74%61%63%74%20%75%73");
    }

    /**
     * @return  The {@code String} that is the HTML code of the contact link in text,
     *          displaying 'Bgee e-mail'.
     */
    protected String getObfuscateBgeeEmail() {
        return getObfuscateEmailLink("%42%67%65%65%20%65%2D%6D%61%69%6C");
    }

    /**
     * @return  The {@code String} that is the HTML code of the contact link in text,
     *          displaying 'e-mail'.
     */
    protected String getObfuscateEmail() {
        return getObfuscateEmailLink("%65%2D%6D%61%69%6C");
    }

    private String getObfuscateEmailLink(String encodedLinkText) {
        return "<script>eval(unescape("
                + "'%66%75%6E%63%74%69%6F%6E%20%70%67%72%65%67%67%5F%74%72%61%6E%73%70%6F%73%65"
                + "%31%28%68%29%20%7B%76%61%72%20%73%3D%27%61%6D%6C%69%6F%74%42%3A%65%67%40%65"
                + "%69%73%2E%62%77%73%73%69%73%27%3B%76%61%72%20%72%3D%27%27%3B%66%6F%72%28%76"
                + "%61%72%20%69%3D%30%3B%69%3C%73%2E%6C%65%6E%67%74%68%3B%69%2B%2B%2C%69%2B%2B"
                + "%29%7B%72%3D%72%2B%73%2E%73%75%62%73%74%72%69%6E%67%28%69%2B%31%2C%69%2B%32"
                + "%29%2B%73%2E%73%75%62%73%74%72%69%6E%67%28%69%2C%69%2B%31%29%7D%68%2E%68%72"
                + "%65%66%3D%72%3B%7D%64%6F%63%75%6D%65%6E%74%2E%77%72%69%74%65%28%27%3C%61%20"
                + "%68%72%65%66%3D%22%23%22%20%6F%6E%4D%6F%75%73%65%4F%76%65%72%3D%22%6A%61%76"
                + "%61%73%63%72%69%70%74%3A%70%67%72%65%67%67%5F%74%72%61%6E%73%70%6F%73%65%31"
                + "%28%74%68%69%73%29%22%20%6F%6E%46%6F%63%75%73%3D%22%6A%61%76%61%73%63%72%69"
                + "%70%74%3A%70%67%72%65%67%67%5F%74%72%61%6E%73%70%6F%73%65%31%28%74%68%69%73"
                + "%29%22%3E" + encodedLinkText + "%3C%2F%61%3E%27%29%3B'));</script>";
    }

    /**
     * Get the main logo of the documentation page, as HTML 'div' element.
     *
     * @return  A {@code String} that is the main documentation logo as HTML 'div' element,
     *          formated in HTML and HTML escaped if necessary.
     */
    protected String getMainDocumentationLogo() {
        log.traceEntry();

        RequestParameters urlDocumentationGenerator = this.getNewRequestParameters();
        urlDocumentationGenerator.setPage(RequestParameters.PAGE_DOCUMENTATION);

        return log.traceExit(HtmlParentDisplay.getSingleFeatureLogo(
                urlDocumentationGenerator.getRequestURL(), false,
                "Bgee documentation page", "Documentation",
                this.prop.getBgeeRootDirectory() + this.prop.getLogoImagesRootDirectory() + "doc_logo.png", null));
    }

//    /**
//     * Get the main logo of the download page, as HTML 'div' element.
//     *
//     * @return  A {@code String} that is the main download logo as HTML 'div' element,
//     *          formated in HTML and HTML escaped if necessary.
//     */
//    protected String getMainDownloadLogo() {
//        log.traceEntry();
//
//        RequestParameters urlDownloadGenerator = this.getNewRequestParameters();
//        urlDownloadGenerator.setPage(RequestParameters.PAGE_DOWNLOAD);
//
//        return log.traceExit(HtmlParentDisplay.getSingleFeatureLogo(urlDownloadGenerator.getRequestURL(),
//                "Bgee expression data page", "Expression data",
//                this.prop.getLogoImagesRootDirectory() + "download_logo.png",
//                "Calls of baseline presence/absence of expression, "
//                + "and of differential over-/under-expression."));
//    }

    /**
     * Get the feature logos of the download page, as HTML 'div' elements.
     *
     * @return  A {@code String} that is the feature download logos as HTML 'div' elements,
     *          formated in HTML and HTML escaped if necessary.
     */
    protected String getFeatureDownloadLogos() {
        log.traceEntry();

        RequestParameters urlDownloadRefExprGenerator = this.getNewRequestParameters();
        urlDownloadRefExprGenerator.setPage(RequestParameters.PAGE_DOWNLOAD);
        urlDownloadRefExprGenerator.setAction(RequestParameters.ACTION_DOWLOAD_PROC_VALUE_FILES);

        RequestParameters urlDownloadCallsGenerator = this.getNewRequestParameters();
        urlDownloadCallsGenerator.setPage(RequestParameters.PAGE_DOWNLOAD);
        urlDownloadCallsGenerator.setAction(RequestParameters.ACTION_DOWLOAD_CALL_FILES);

        StringBuilder logos = new StringBuilder();

        logos.append(HtmlParentDisplay.getSingleFeatureLogo(
                urlDownloadCallsGenerator.getRequestURL(), false,
                "Bgee " + GENE_EXPR_CALLS_PAGE_NAME.toLowerCase() + " page", GENE_EXPR_CALLS_PAGE_NAME,
                this.prop.getBgeeRootDirectory() + this.prop.getLogoImagesRootDirectory() + "expr_calls_logo.png",
                "Calls of baseline presence/absence of expression, "
                + "and of differential over-/under-expression, in single or multiple species."));

        logos.append(HtmlParentDisplay.getSingleFeatureLogo(
                urlDownloadRefExprGenerator.getRequestURL(), false,
                "Bgee " + PROCESSED_EXPR_VALUES_PAGE_NAME.toLowerCase() + " page",
                PROCESSED_EXPR_VALUES_PAGE_NAME,
                this.prop.getBgeeRootDirectory() + this.prop.getLogoImagesRootDirectory() + "proc_values_logo.png",
                "Annotations and processed expression data (e.g., read counts, TPM and "
                + "RPKM values, Affymetrix probeset signal intensities)."));

        return log.traceExit(logos.toString());
    }

    /**
     * Write HTML code allowing to include common javascript files. Subclasses needing to include
     * additional javascript files must override this method.
     * <p>
     * <strong>Important</strong>:
     * <ul>
     * <li>Javascript files should always be included by calling {@link #includeJs(String)}.
     * {@link #includeJs(String)} will set the proper directory, and will automatically
     * define versioned file names.
     * <li>{@code super.includeJs()} should always be called by these overriding methods,
     * unless the aim is to generate a special page not using the common Bgee javascript libraries.
     * </ul>
     * @see #includeJs(String)
     */
    protected void includeJs() {
        log.traceEntry();
        if (!this.prop.isMinify()) {
            this.includeJs("lib/jquery.min.js");
            this.includeJs("lib/jquery_plugins/jquery.visible.min.js");
            this.includeJs("lib/jquery_plugins/jquery-ui.min.js");
            this.includeJs("lib/jquery_plugins/toastr.min.js");
            //we need to add the bootstrap JS file after jQuery JS file to override it for tooltip
            this.includeJs("lib/jquery_plugins/bootstrap.min.js");
            this.includeJs("bgeeproperties.js");
            this.includeJs("urlparameters.js");
            this.includeJs("requestparameters.js");
            this.includeJs("common.js");
        } else {
            //If you ever add new files, you need to edit bgee-webapp/pom.xml
            //to correctly merge/minify them.
            this.includeJs("vendor_common.js");
            this.includeJs("script_common.js");
        }
        log.traceExit();
    }
    /**
     * Write the HTML code allowing to include the javascript file named {@code fileName}.
     * This method will notably retrieve the directory hosting the files, and will
     * define the versioned file name corresponding to {@code fileName}, as hosted
     * on the server. HTML is written using {@link #writeln(String)}.
     * <strong>It should be called only within a {@link #includeJs()} method, whether overridden
     * or not.</strong>.
     *
     * @param fileName  The original name of the javascript file to include.
     * @see #getVersionedJsFileName(String)
     */
    protected void includeJs(String fileName) {
        log.traceEntry("{}", fileName);
        this.writeln("<script src='" +
                this.prop.getBgeeRootDirectory() + this.prop.getJavascriptFilesRootDirectory() +
                this.getVersionedJsFileName(fileName) + "'></script>");
        log.traceExit();
    }
    /**
     * Transform the name of a javascript file into a name including version information,
     * following the pattern used for javascript files hosted on the server. This is to avoid
     * caching issues. The extension to use for version information is provided by
     * {@link BgeeProperties#getJavascriptVersionExtension()}.
     * <p>
     * For instance, if {@code getJavascriptVersionExtension} returns "-13",
     * and if {@code originalFileName} is equal to "common.js", the value returned
     * by this method will be: "common-13.js".
     * <p>
     * For simplicity, only file names ending with '.js' are accepted, otherwise,
     * an {@code IllegalArgumentException} is thrown.
     *
     * @param originalFileName  A {@code String} that is the name of a javascript file,
     *                          ending with ".js", to transform into a versioned file name.
     * @return                  A {@code String} that is the versioned javascript file name,
     *                          as used on the server, including the version extension
     *                          returned by {@link BgeeProperties#getJavascriptVersionExtension()}.
     */
    protected String getVersionedJsFileName(String originalFileName) {
        log.traceEntry("{}", originalFileName);
        if (!originalFileName.endsWith(".js")) {
            throw log.throwing(new IllegalArgumentException("The provided file name "
                    + "must end with an extension '.js'."));
        }
        //if no version info was provided, or if we don't want to use the minified files,
        //return original name.
        if (StringUtils.isBlank(this.prop.getJavascriptVersionExtension()) ||
                !this.prop.isMinify()) {
            return log.traceExit(originalFileName);
        }
        return log.traceExit(originalFileName.replaceAll("(.+?)\\.js",
                "$1." + this.prop.getJavascriptVersionExtension() + ".js"));
    }

    /**
     * Write HTML code allowing to include common CSS files. Subclasses needing to include
     * additional CSS files must override this method.
     * <p>
     * <strong>Important</strong>:
     * <ul>
     * <li>CSS files should always be included by calling {@link #includeCss(String)}.
     * {@link #includeCss(String)} will set the proper directory, and will automatically
     * define versioned file names.
     * <li>{@code super.includeCss()} should always be called by these overriding methods,
     * unless the aim is to generate a special page not using the common CSS definitions.
     * </ul>
     * @see #includeCss(String)
     */
    protected void includeCss() {
        log.traceEntry();
        if (!this.prop.isMinify()) {
            this.includeCss("lib/jquery_plugins/jquery-ui.min.css");
            this.includeCss("lib/jquery_plugins/jquery-ui.structure.min.css");
            this.includeCss("lib/jquery_plugins/jquery-ui.theme.min.css");
            this.includeCss("lib/jquery_plugins/toastr.min.css");
            //we need to add the bootstrap CSS file after jQuery CSS file to override it for tooltip
            this.includeCss("lib/jquery_plugins/bootstrap.min.css");
            //we need to add the Bgee CSS files at the end, to override CSS file from bootstrap
            this.includeCss("bgee.css");
        } else {
            //If you ever add new files, you need to edit bgee-webapp/pom.xml
            //to correctly merge/minify them.
            //the CSS files need to keep their relative location to other paths the same,
            //this is why we keep their location and don't merge them all
            this.includeCss("lib/jquery_plugins/vendor_common.css");
            //we need to add the Bgee CSS files at the end, to override CSS file from bootstrap
            this.includeCss("common.css");
        }
        log.traceExit();
    }
    /**
     * Write the HTML code allowing to include the CSS file named {@code fileName}.
     * This method will notably retrieve the directory hosting the files, and will
     * define the versioned file name corresponding to {@code fileName}, as hosted
     * on the server. HTML is written using {@link #writeln(String)}.
     * <strong>It should be called only within a {@link #includeCss()} method, whether overridden
     * or not.</strong>.
     *
     * @param fileName  The original name of the CSS file to include.
     * @see #getVersionedCssFileName(String)
     */
    protected void includeCss(String fileName) {
        log.traceEntry("{}", fileName);
        this.writeln("<link rel='stylesheet' type='text/css' href='"
                + this.prop.getBgeeRootDirectory() + this.prop.getCssFilesRootDirectory()
                + this.getVersionedCssFileName(fileName) + "'/>");
        log.traceExit();
    }
    /**
     * Transform the name of a CSS file into a name including version information,
     * following the pattern used for CSS files hosted on the server. This is to avoid
     * caching issues. The extension to use for version information is provided by
     * {@link BgeeProperties#getCssVersionExtension()}.
     * <p>
     * For instance, if {@code getCssVersionExtension} returns "-13",
     * and if {@code originalFileName} is equal to "bgee.css", the value returned
     * by this method will be: "bgee-13.css".
     * <p>
     * For simplicity, only file names ending with '.css' are accepted, otherwise,
     * an {@code IllegalArgumentException} is thrown.
     *
     * @param originalFileName  A {@code String} that is the name of a CSS file,
     *                          ending with ".css", to transform into a versioned file name.
     * @return                  A {@code String} that is the versioned CSS file name,
     *                          as used on the server, including the version extension
     *                          returned by {@link BgeeProperties#getCssVersionExtension()}.
     */
    protected String getVersionedCssFileName(String originalFileName) {
        log.traceEntry("{}", originalFileName);
        if (!originalFileName.endsWith(".css")) {
            throw log.throwing(new IllegalArgumentException("The provided file name "
                    + "must end with an extension '.css'."));
        }
        //if no version info was provided, or if we don't want to use the minified files,
        //return original name.
        if (StringUtils.isBlank(this.prop.getCssVersionExtension()) ||
                !this.prop.isMinify()) {
            return log.traceExit(originalFileName);
        }
        return log.traceExit(originalFileName.replaceAll("(.+?)\\.css",
                "$1." + this.prop.getCssVersionExtension() + ".css"));
    }

    /**
     * Return a new {@code RequestParameters} object to be used to generate URLs.
     * This new {@code RequestParameters} will use the same {@code URLParameters}
     * as those returned by {@link #getRequestParameters()} when calling
     * {@link RequestParameters#getUrlParametersInstance()},
     * and the {@code BgeeProperties} {@link #prop}.
     * Also, parameters will be URL encoded, and parameter separator will be {@code &amp;}.
     *
     * @return  A newly created RequestParameters object.
     */
    protected RequestParameters getNewRequestParameters() {
        log.traceEntry();
        return log.traceExit(new RequestParameters(
                this.getRequestParameters().getUrlParametersInstance(),
                this.prop, true, "&amp;"));
    }

    /**
     * @return  The {@code HtmlFactory} that instantiated this object. This method is provided
     *          only for convenience to avoid having to cast the {@code Viewfactory} returned by
     *          {@link #getFactory()}.
     */
    protected HtmlFactory getHtmlFactory() {
        return (HtmlFactory) super.getFactory();
    }
    /**
     * @return  The {@code ViewFactory} that instantiated this object, of type {@code HtmlFactory}.
     *          See {@link #getHtmlFactory()} for a method returning directly the factory
     *          as a {@code HtmlFactory}.
     */
    @Override
    //method overridden only to provide more accurate javadoc
    protected ViewFactory getFactory() {
        return super.getFactory();
    }

    /**
     * @return  The {@code JsonHelper} used to read/write variables into JSON.
     */
    protected JsonHelper getJsonHelper() {
        return jsonHelper;
    }

    /**
     * Get the images sources of a download page as a HTML 'div' element.
     *
     * @return  the {@code String} that is the images sources as HTML 'div' element.
     */
    protected static String getImageSources() {
        log.traceEntry();

        String commonsWikipedia = "https://commons.wikimedia.org/wiki";
        String gnuOrg           = "https://www.gnu.org/copyleft/fdl.html";
        String creativeCommons  = "https://creativecommons.org";
        String gompelDroso      = "http://gompel.org/images-2/drosophilidae";
        StringBuilder sources = new StringBuilder();
        sources.append("<p id='creativecommons_title'>Images from Wikimedia Commons. In most cases, pictures corresponds to the sequenced strains. <a>Show information about original images.</a></p>");
        sources.append("<div id='creativecommons'>");
        sources.append("<p><i>Homo sapiens</i> picture by Leonardo da Vinci (Life time: 1519) [Public domain]. <a target='_blank' rel='noopener' href='" + commonsWikipedia + "/File:Da_Vinci%27s_Anatomical_Man.jpg#mediaviewer/File:Da_Vinci%27s_Anatomical_Man.jpg'>See <i>H. sapiens</i> picture via Wikimedia Commons</a></p>");
        sources.append("<p><i>Mus musculus</i> picture by Rasbak [<a target='_blank' rel='noopener' href='" + gnuOrg + "'>GFDL</a> or <a target='_blank' rel='noopener' href='" + creativeCommons + "/licenses/by-sa/3.0/'>CC-BY-SA-3.0</a>], <a target='_blank' rel='noopener' href='" + commonsWikipedia + "/File%3AApodemus_sylvaticus_bosmuis.jpg'>See <i>M. musculus</i> picture via Wikimedia Commons</a></p>");
        sources.append("<p><i>Danio rerio</i> picture by Azul (Own work) [see page for license], <a target='_blank' rel='noopener' href='" + commonsWikipedia + "/File%3AZebrafisch.jpg'>See <i>D. rerio</i> picture via Wikimedia Commons</a></p>");
        sources.append("<p><i>Drosophila melanogaster</i> picture by Andr&eacute; Karwath aka Aka (Own work) [<a target='_blank' rel='noopener' href='" + creativeCommons + "/licenses/by-sa/2.5'>CC-BY-SA-2.5</a>], <a target='_blank' rel='noopener' href='" + commonsWikipedia + "/File%3ADrosophila_melanogaster_-_side_(aka).jpg'>See <i>D. melanogaster</i> picture via Wikimedia Commons</a></p>");
        sources.append("<p><i>Caenorhabditis elegans</i> picture by Bob Goldstein, UNC Chapel Hill http://bio.unc.edu/people/faculty/goldstein/ (Own work) [<a target='_blank' rel='noopener' href='" + creativeCommons + "/licenses/by-sa/3.0'>CC-BY-SA-3.0</a>], <a target='_blank' rel='noopener' href='" + commonsWikipedia + "/File%3ACelegansGoldsteinLabUNC.jpg'>See <i>C. elegans</i> picture via Wikimedia Commons</a></p>");
        sources.append("<p><i>Pan troglodytes</i> picture by Thomas Lersch (Own work) [<a target='_blank' rel='noopener' href='" + gnuOrg + "'>GFDL</a>, <a target='_blank' rel='noopener' href='" + creativeCommons + "/licenses/by-sa/3.0/'>CC-BY-SA-3.0</a> or <a target='_blank' rel='noopener' href='" + creativeCommons + "/licenses/by/2.5'>CC-BY-2.5</a>], <a target='_blank' rel='noopener' href='" + commonsWikipedia + "/File%3ASchimpanse_Zoo_Leipzig.jpg'>See <i>P. troglodytes</i> picture via Wikimedia Commons</a></p>");
        sources.append("<p><i>Pan paniscus</i> picture by Ltshears (Own work) [<a target='_blank' rel='noopener' href='" + creativeCommons + "/licenses/by-sa/3.0'>CC-BY-SA-3.0</a> or <a target='_blank' rel='noopener' href='" + gnuOrg + "'>GFDL</a>], <a target='_blank' rel='noopener' href='" + commonsWikipedia + "/File%3ABonobo1_CincinnatiZoo.jpg'>See <i>P. paniscus</i> picture via Wikimedia Commons</a></p>");
        sources.append("<p><i>Gorilla gorilla</i> picture by Brocken Inaglory (Own work) [<a target='_blank' rel='noopener' href='" + creativeCommons + "/licenses/by-sa/3.0'>CC-BY-SA-3.0</a> or <a target='_blank' rel='noopener' href='" + gnuOrg + "'>GFDL</a>], <a target='_blank' rel='noopener' href='" + commonsWikipedia + "/File%3AMale_gorilla_in_SF_zoo.jpg'>See <i>G. gorilla</i> picture via Wikimedia Commons</a></p>");
        sources.append("<p><i>Macaca mulatta</i> picture by Aiwok (Own work) [<a target='_blank' rel='noopener' href='" + gnuOrg + "'>GFDL</a> or <a target='_blank' rel='noopener' href='" + creativeCommons + "/licenses/by-sa/3.0'>CC-BY-SA-3.0-2.5-2.0-1.0</a>], <a target='_blank' rel='noopener' href='" + commonsWikipedia + "/File%3AMacaca_mulatta_3.JPG'>See <i>M. mulatta</i> picture via Wikimedia Commons</a></p>");
        sources.append("<p><i>Rattus norvegicus</i> picture by Reg Mckenna (originally posted to Flickr as Wild Rat) [<a target='_blank' rel='noopener' href='" + creativeCommons + "/licenses/by/2.0'>CC-BY-2.0</a>], <a target='_blank' rel='noopener' href='" + commonsWikipedia + "/File%3AWildRat.jpg'>See <i>R. norvegicus</i> picture via Wikimedia Commons</a></p>");
        sources.append("<p><i>Bos taurus</i> picture by User Robert Merkel on en.wikipedia (US Department of Agriculture) [Public domain], <a target='_blank' rel='noopener' href='" + commonsWikipedia + "/File%3AHereford_bull_large.jpg'>See <i>B. taurus</i> picture via Wikimedia Commons</a></p>");
        sources.append("<p><i>Sus scrofa</i> picture by Joshua Lutz (Own work) [Public domain], <a target='_blank' rel='noopener' href='" + commonsWikipedia + "/File%3ASus_scrofa_scrofa.jpg'>See <i>S. scrofa</i> picture via Wikimedia Commons</a></p>");
        sources.append("<p><i>Equus caballus</i> picture by Doug Antczak Baker Institute for Animal Health College of Veterinary Medicine Cornell University [Public Domain], <a target='_blank' rel='noopener' href='" + commonsWikipedia + "/File:Twilight20008-300.jpg#/media/File:Twilight20008-300.jpg'>See <i>E. caballus</i> picture via Wikimedia Commons</a></p>");
        sources.append("<p><i>Oryctolagus cuniculus</i> picture by JJ Harrison (Own work) [<a target='_blank' rel='noopener' href='" + creativeCommons + "/licenses/by-sa/3.0'>CC-BY-SA-3.0</a>], <a target='_blank' rel='noopener' href='" + commonsWikipedia + "/File:Oryctolagus_cuniculus_Tasmania_2.jpg#/media/File:Oryctolagus_cuniculus_Tasmania_2.jpg'>See <i>O. cuniculus</i> picture via Wikimedia Commons</a></p>");
        sources.append("<p><i>Canis lupus familiaris</i> picture by Mood210 (Own work) [<a target='_blank' rel='noopener' href='" + creativeCommons + "/licenses/by-sa/3.0'>CC-BY-SA-3.0</a>], <a target='_blank' rel='noopener' href='" + commonsWikipedia + "/File:Male_fawn_Boxer_undocked.jpg#/media/File:Male_fawn_Boxer_undocked.jpg'>See <i>C. lupus familiaris</i> picture via Wikimedia Commons</a></p>");
        sources.append("<p><i>Felis catus</i> picture [<a target='_blank' rel='noopener' href='" + creativeCommons + "/licenses/by-sa/3.0'>CC-BY-SA-3.0</a>], <a target='_blank' rel='noopener' href='" + commonsWikipedia + "/File:Valentino.jpg#/media/File:Valentino.jpg'>See <i>F. catus</i> picture via Wikimedia Commons</a></p>");
        sources.append("<p><i>Cavia porcellus</i> picture by Variraptor (Own work) [<a target='_blank' rel='noopener' href='" + creativeCommons + "/licenses/by-sa/3.0'>CC-BY-SA-3.0</a>], <a target='_blank' rel='noopener' href='" + commonsWikipedia + "/File:Yoyocochondinde.JPG#/media/File:Yoyocochondinde.JPG'>See <i>C. porcellus</i> picture via Wikimedia Commons</a></p>");
        //sources.append("<p><i>Erinaceus europaeus</i> picture by Michael Gäbler (Own work) [<a target='_blank' rel='noopener' href='" + creativeCommons + "/licenses/by-sa/3.0'>CC-BY-SA-3.0</a>], <a target='_blank' rel='noopener' href='" + commonsWikipedia + "/File:Erinaceus_europaeus_(Linnaeus,_1758).jpg#/media/File:Erinaceus_europaeus_(Linnaeus,_1758).jpg'>See <i>E. europaeus</i> picture via Wikimedia Commons</a></p>");
        sources.append("<p><i>Monodelphis domestica</i> picture by <i>Marsupial Genome Sheds Light on the Evolution of Immunity.</i> Hill E, PLoS Biology Vol. 4/3/2006, e75 <a rel='nofollow' href='http://dx.doi.org/10.1371/journal.pbio.0040075'>http://dx.doi.org/10.1371/journal.pbio.0040075</a> [<a target='_blank' rel='noopener' href='" + creativeCommons + "/licenses/by/2.5'>CC-BY-2.5</a>], <a target='_blank' rel='noopener' href='" + commonsWikipedia + "/File%3AOpossum_with_young.png'>See <i>M. domestica</i> picture via Wikimedia Commons</a></p>");
        sources.append("<p><i>Ornithorhynchus anatinus</i> picture by Dr. Philip Bethge (private) [<a target='_blank' rel='noopener' href='" + gnuOrg + "'>GFDL</a> or <a target='_blank' rel='noopener' href='" + creativeCommons + "/licenses/by-sa/3.0'>CC-BY-SA-3.0-2.5-2.0-1.0</a>], <a target='_blank' rel='noopener' href='" + commonsWikipedia + "/File%3AOrnithorhynchus.jpg'>See <i>O. anatinus</i> picture via Wikimedia Commons</a></p>");
        sources.append("<p><i>Gallus gallus</i> picture by Subramanya C K (Own work) [<a target='_blank' rel='noopener' href='" + creativeCommons + "/licenses/by-sa/3.0'>CC-BY-SA-3.0</a>], <a target='_blank' rel='noopener' href='" + commonsWikipedia + "/File%3ARed_jungle_fowl.png'>See <i>G. gallus</i> picture via Wikimedia Commons</a></p>");
        sources.append("<p><i>Anolis carolinensis</i> picture by PiccoloNamek (Moved from Image:P1010027.jpg) [<a target='_blank' rel='noopener' href='" + gnuOrg + "'>GFDL</a> or <a target='_blank' rel='noopener' href='" + creativeCommons + "/licenses/by-sa/3.0/'>CC-BY-SA-3.0</a>], <a target='_blank' rel='noopener' href='" + commonsWikipedia + "/File%3AAnolis_carolinensis.jpg'>See <i>A. carolinensis</i> picture via Wikimedia Commons</a></p>");
        sources.append("<p><i>Xenopus tropicalis</i> picture by V&aacute;clav Gvo&zcaron;d&iacute;k (http://calphotos.berkeley.edu) [<a target='_blank' rel='noopener' href='" + creativeCommons + "/licenses/by-sa/2.5'>CC-BY-SA-2.5</a> or <a target='_blank' rel='noopener' href='" + creativeCommons + "/licenses/by-sa/3.0'>CC-BY-SA-3.0</a>], <a target='_blank' rel='noopener' href='" + commonsWikipedia + "/File%3AXenopus_tropicalis01.jpeg'>See <i>X. tropicalis</i> picture via Wikimedia Commons</a></p>");
        sources.append("<p><i>Xenopus laevis</i> picture by Brian Gratwicke (https://www.flickr.com/photos/19731486@N07/8325732255) [<a target='_blank' rel='noopener' href='" + creativeCommons + "/licenses/by/2.0'>CC-BY-2.0</a>], <a target='_blank' rel='noopener' href='" + commonsWikipedia + "/File%3AXenopus_laevis_02.jpg'>See <i>X. laevis</i> picture via Wikimedia Commons</a></p>");
        sources.append("<p><i>Branchiostoma lanceolatum</i> picture by Hans Hillewaert (Own work) [<a target='_blank' rel='noopener' href='" + creativeCommons + "/licenses/by-sa/4.0'>CC-BY-SA-4.0</a>], <a target='_blank' rel='noopener' href='" + commonsWikipedia + "/File%3ABranchiostoma_lanceolatum.jpg'>See <i>B. lanceolatum</i> picture via Wikimedia Commons</a></p>");
        sources.append("<p><i>Latimeria chalumnae</i> picture by Alberto Fernandez Fernandez (Own work) [<a target='_blank' rel='noopener' href='" + gnuOrg + "'>GFDL</a>, <a target='_blank' rel='noopener' href='" + creativeCommons + "/licenses/by-sa/3.0'>CC-BY-SA-3.0</a> or <a target='_blank' rel='noopener' href='" + creativeCommons + "/licenses/by-sa/2.5'>CC-BY-SA-2.5</a>], <a target='_blank' rel='noopener' href='" + commonsWikipedia + "/File%3ALatimeria_Chalumnae_-_Coelacanth_-_NHMW.jpg'>See <i>L. chalumnae</i> picture via Wikimedia Commons</a></p>");
        sources.append("<p><i>Lepisosteus oculatus</i> picture by Brian Gratwicke (Own work) [<a target='_blank' rel='noopener' href='" + creativeCommons + "/licenses/by/2.5'>CC-BY-2.5</a>], <a target='_blank' rel='noopener' href='" + commonsWikipedia + "/File%3ALepisosteus_oculatus1.jpg'>See <i>L. oculatus</i> picture via Wikimedia Commons</a></p>");
        sources.append("<p><i>Anguilla anguilla</i> picture by GerardM (http://www.digischool.nl/bi/onderwaterbiologie/) [<a target='_blank' rel='noopener' href='" + gnuOrg + "'>GFDL</a> or <a target='_blank' rel='noopener' href='" + creativeCommons + "/licenses/by-sa/3.0'>CC-BY-SA-3.0</a>], <a target='_blank' rel='noopener' href='" + commonsWikipedia + "/File%3AAnguilla_anguilla.jpg'>See <i>A. anguilla</i> picture via Wikimedia Commons</a></p>");
        sources.append("<p><i>Astyanax mexicanus</i> picture by H. Zell (Own work) [<a target='_blank' rel='noopener' href='" + gnuOrg + "'>GFDL</a> or <a target='_blank' rel='noopener' href='" + creativeCommons + "/licenses/by-sa/3.0'>CC-BY-SA-3.0</a>], <a target='_blank' rel='noopener' href='" + commonsWikipedia + "/File%3AAstyanax_mexicanus_01.jpg'>See <i>A. mexicanus</i> picture via Wikimedia Commons</a></p>");
        sources.append("<p><i>Esox lucius</i> picture by Jik jik (Own work) [<a target='_blank' rel='noopener' href='" + gnuOrg + "'>GFDL</a> or <a target='_blank' rel='noopener' href='" + creativeCommons + "/licenses/by-sa/3.0'>CC-BY-SA-3.0-2.5-2.0-1.0</a>], <a target='_blank' rel='noopener' href='" + commonsWikipedia + "/File%3AEsox_lucius_ZOO_1.jpg'>See <i>E. lucius</i> picture via Wikimedia Commons</a></p>");
        sources.append("<p><i>Salmo salar</i> picture by Hans-Petter Fjeld (Own work) [<a target='_blank' rel='noopener' href='" + creativeCommons + "/licenses/by-sa/2.5'>CC-BY-SA-2.5</a>], <a target='_blank' rel='noopener' href='" + commonsWikipedia + "/File%3ASalmo_salar-Atlantic_Salmon-Atlanterhavsparken_Norway.JPG'>See <i>S. salar</i> picture via Wikimedia Commons</a></p>");
        sources.append("<p><i>Gadus morhua</i> picture by Hans-Petter Fjeld (Own work) [<a target='_blank' rel='noopener' href='" + creativeCommons + "/licenses/by-sa/2.5'>CC-BY-SA-2.5</a>], <a target='_blank' rel='noopener' href='" + commonsWikipedia + "/File%3AGadus_morhua_Cod-2b-Atlanterhavsparken-Norway.JPG'>See <i>G. morhua</i> picture via Wikimedia Commons</a></p>");
        sources.append("<p><i>Poecilia reticulata</i> picture by Per Harald Olsen (Own work) [<a target='_blank' rel='noopener' href='" + gnuOrg + "'>GFDL</a> or <a target='_blank' rel='noopener' href='" + creativeCommons + "/licenses/by/3.0'>CC-BY-3.0</a>], <a target='_blank' rel='noopener' href='" + commonsWikipedia + "/File%3AGuppy_pho_0048.jpg'>See <i>P. reticulata</i> picture via Wikimedia Commons</a></p>");
        sources.append("<p><i>Oryzias latipes</i> picture by NOZO (Own work) [<a target='_blank' rel='noopener' href='" + gnuOrg + "'>GFDL</a> or <a target='_blank' rel='noopener' href='" + creativeCommons + "/licenses/by/3.0'>CC-BY-3.0</a>], <a target='_blank' rel='noopener' href='" + commonsWikipedia + "/File%3ANihonmedaka.jpg'>See <i>O. latipes</i> picture via Wikimedia Commons</a></p>");
        sources.append("<p><i>Astatotilapia calliptera</i> picture by Alexandra Tyers (https://www.flickr.com/photos/52993488@N03/5441877789) [<a target='_blank' rel='noopener' href='" + creativeCommons + "/licenses/by-sa/2.0'>CC-BY-SA-2.0</a>], <a target='_blank' rel='noopener' href='" + commonsWikipedia + "/File%3AAstatotilapia_calliptera.jpg'>See <i>A. calliptera</i> picture via Wikimedia Commons</a></p>");
        sources.append("<p><i>Meleagris gallopavo</i> picture by Riki7 (Own work) [Public domain], <a target='_blank' rel='noopener' href='" + commonsWikipedia + "/File%3AGall-dindi.jpg'>See <i>M. gallopavo</i> picture via Wikimedia Commons</a></p>");
        sources.append("<p><i>Callithrix jacchus</i> picture by Leszek Leszczynski (https://www.flickr.com/photos/leszekleszczynski/6952548339/) [<a target='_blank' rel='noopener' href='" + creativeCommons + "/licenses/by/2.0'>CC-BY-2.0</a>], <a target='_blank' rel='noopener' href='" + commonsWikipedia + "/File%3ACommon_marmoset_(Callithrix_jacchus).jpg'>See <i>C. jacchus</i> picture via Wikimedia Commons</a></p>");
        sources.append("<p><i>Cercocebus atys</i> picture by Giulio Russo Photography (Own work) [<a target='_blank' rel='noopener' href='" + creativeCommons + "/licenses/by/4.0'>CC-BY-4.0</a>], <a target='_blank' rel='noopener' href='" + commonsWikipedia + "/File%3ACercocebo_Dal_Collare.jpg'>See <i>C. atys</i> picture via Wikimedia Commons</a></p>");
        sources.append("<p><i>Macaca fascicularis</i> picture by Andr&eacute; Ueberbach (Eigene Aufnahme von Andr&eacute; Ueberbach/Own production) [<a target='_blank' rel='noopener' href='" + creativeCommons + "/licenses/by-sa/2.0/de'>CC-BY-SA-2.0 DE</a>], <a target='_blank' rel='noopener' href='" + commonsWikipedia + "/File%3AMacaca_fascicularis.jpg'>See <i>M. fascicularis</i> picture via Wikimedia Commons</a></p>");
        sources.append("<p><i>Macaca nemestrina</i> picture by Hectonichus (Own work) [<a target='_blank' rel='noopener' href='" + creativeCommons + "/licenses/by-sa/3.0'>CC-BY-SA-3.0</a> or <a target='_blank' rel='noopener' href='" + gnuOrg + "'>GFDL</a>], <a target='_blank' rel='noopener' href='" + commonsWikipedia + "/File%3ACercopithecidae_-_Macaca_nemastrina.jpg'>See <i>M. nemestrina</i> picture via Wikimedia Commons</a></p>");
        sources.append("<p><i>Papio anubis</i> picture by Charles J. Sharp (Own work, from Sharp Photography, http://www.sharpphotography.co.uk/) [<a target='_blank' rel='noopener' href='" + creativeCommons + "/licenses/by-sa/4.0'>CC-BY-SA-4.0</a>], <a target='_blank' rel='noopener' href='" + commonsWikipedia + "/File%3AOlive_baboon_(Papio_anubis)_with_juvenile.jpg'>See <i>P. anubis</i> picture via Wikimedia Commons</a></p>");
        sources.append("<p><i>Capra hircus</i> picture by flagstaffotos [at] gmail.com (Own work) [<a target='_blank' rel='noopener' href='" + gnuOrg + "'>GFDL</a>], <a target='_blank' rel='noopener' href='" + commonsWikipedia + "/File%3ADomestic_goat_May_2006.jpg'>See <i>C. hircus</i> picture via Wikimedia Commons</a></p>");
        sources.append("<p><i>Ovis aries</i> picture by Jacquie Wingate from Recovery, USA (https://www.flickr.com/photos/11948828@N00/2212889583/) [<a target='_blank' rel='noopener' href='" + creativeCommons + "/licenses/by-sa/1.0'>CC-BY-SA-1.0</a>], <a target='_blank' rel='noopener' href='" + commonsWikipedia + "/File%3ATake_ours!.jpg'>See <i>O. aries</i> picture via Wikimedia Commons</a></p>");
        sources.append("<p><i>Manis javanica</i> picture by Frendi Apen Irawan (Own work) [<a target='_blank' rel='noopener' href='" + creativeCommons + "/licenses/by-sa/4.0'>CC-BY-SA-4.0</a>], <a target='_blank' rel='noopener' href='" + commonsWikipedia + "/File%3ATrenggiling_Sunda_Sunda_Pangolin_Manis_javanica.jpg'>See <i>M. javanica</i> picture via Wikimedia Commons</a></p>");
        sources.append("<p><i>Heterocephalus glaber</i> picture by Roman Klementschitz, Wien (Own work) [<a target='_blank' rel='noopener' href='" + gnuOrg + "'>GFDL</a> or <a target='_blank' rel='noopener' href='" + creativeCommons + "/licenses/by-sa/3.0'>CC-BY-SA-3.0</a>], <a target='_blank' rel='noopener' href='" + commonsWikipedia + "/File%3ANacktmull.jpg'>See <i>H. glaber</i> picture via Wikimedia Commons</a></p>");
        sources.append("<p><i>Microcebus murinus</i> picture by Charles J. Sharp (Own work, from Sharp Photography, http://www.sharpphotography.co.uk/) [<a target='_blank' rel='noopener' href='" + creativeCommons + "/licenses/by-sa/3.0'>CC-BY-SA-3.0</a>], <a target='_blank' rel='noopener' href='" + commonsWikipedia + "/File%3AGray_mouse_lemur_microcebus_murinus.jpg'>See <i>M. murinus</i> picture via Wikimedia Commons</a></p>");
        sources.append("<p><i>Neolamprologus brichardi</i> picture by David Midgley (www.sydneycichlid.com) (Own work) [<a target='_blank' rel='noopener' href='" + creativeCommons + "/licenses/by-sa/2.5'>CC-BY-SA-2.5</a>], <a target='_blank' rel='noopener' href='" + commonsWikipedia + "/File%3ANeolamprologus_brichardi.jpg'>See <i>N. brichardi</i> picture via Wikimedia Commons</a></p>");
        sources.append("<p><i>Scophthalmus maximus</i> picture by Luc Viatour (Own work http://www.lucnix.be/) [<a target='_blank' rel='noopener' href='" + gnuOrg + "'>GFDL</a> or <a target='_blank' rel='noopener' href='" + creativeCommons + "/licenses/by-sa/3.0'>CC-BY-SA-3.0-2.5-2.0-1.0</a>], <a target='_blank' rel='noopener' href='" + commonsWikipedia + "/File%3APsetta_maxima_Luc_Viatour.jpg'>See <i>S. maximus</i> picture via Wikimedia Commons</a></p>");
        sources.append("<p><i>Chlorocebus sabaeus</i> picture by Charles J. Sharp (Own work, from Sharp Photography, http://www.sharpphotography.co.uk/) [<a target='_blank' rel='noopener' href='" + creativeCommons + "/licenses/by-sa/4.0'>CC-BY-SA-4.0</a>], <a target='_blank' rel='noopener' href='" + commonsWikipedia + "/File%3AGreen_monkey_(Chlorocebus_sabaeus)_male.jpg'>See <i>C. sabaeus</i> picture via Wikimedia Commons</a></p>");
        sources.append("<p><i>Gasterosteus aculeatus</i> picture by Viridiflavus (Own work) [<a target='_blank' rel='noopener' href='" + gnuOrg + "'>GFDL</a> or <a target='_blank' rel='noopener' href='" + creativeCommons + "/licenses/by-sa/3.0'>CC-BY-SA-3.0</a>], <a target='_blank' rel='noopener' href='" + commonsWikipedia + "/File%3APICT0246-1.JPG'>See <i>G. aculeatus</i> picture via Wikimedia Commons</a></p>");
        sources.append("<p><i>Nothobranchius furzeri</i> picture by Ugau (Own work, Leibniz Institute for Age Research - Fritz Lipmann Institute (FLI), Jena, Germany) [<a target='_blank' rel='noopener' href='" + gnuOrg + "'>GFDL</a> or <a target='_blank' rel='noopener' href='" + creativeCommons + "/licenses/by-sa/3.0'>CC-BY-SA-3.0</a>], <a target='_blank' rel='noopener' href='" + commonsWikipedia + "/File%3ANothobranchius_furzeri_GRZ_thumb.jpg'>See <i>N. furzeri</i> picture via Wikimedia Commons</a></p>");
        //sources.append("<p><i>Drosophila ananassae</i> picture by Nicolas Gompel [<a target='_blank' rel='noopener' href='" + creativeCommons + "/licenses/by-nc-sa/2.0/'>CC BY-NC-SA 2.0 FR</a>], <a target='_blank' rel='noopener' href='" + gompelDroso + "'>See <i>D. ananassae</i> picture via Nicolas Gompel's lab website</a></p>");
        //sources.append("<p><i>Drosophila mojavensis</i> picture by Nicolas Gompel [<a target='_blank' rel='noopener' href='" + creativeCommons + "/licenses/by-nc-sa/2.0/'>CC BY-NC-SA 2.0 FR</a>], <a target='_blank' rel='noopener' href='" + gompelDroso + "'>See <i>D. mojavensis</i> picture via Nicolas Gompel's lab website</a></p>");
        sources.append("<p><i>Drosophila pseudoobscura</i> picture, <a target='_blank' rel='noopener' href='https://metazoa.ensembl.org/i/species/Drosophila_pseudoobscura.png'>See <i>D. pseudoobscura </i> picture via Ensembl Metazoa</a></p>");
        sources.append("<p><i>Drosophila simulans</i> picture by Nicolas Gompel [<a target='_blank' rel='noopener' href='" + creativeCommons + "/licenses/by-nc-sa/2.0/'>CC BY-NC-SA 2.0 FR</a>], <a target='_blank' rel='noopener' href='" + gompelDroso + "'>See <i>D. simulans</i> picture via Nicolas Gompel's lab website</a></p>");
        //sources.append("<p><i>Drosophila virilis</i> picture by Nicolas Gompel [<a target='_blank' rel='noopener' href='" + creativeCommons + "/licenses/by-nc-sa/2.0/'>CC BY-NC-SA 2.0 FR</a>], <a target='_blank' rel='noopener' href='" + gompelDroso + "'>See <i>D. virilis</i> picture via Nicolas Gompel's lab website</a></p>");
        //sources.append("<p><i>Drosophila yakuba</i> picture by Nicolas Gompel [<a target='_blank' rel='noopener' href='" + creativeCommons + "/licenses/by-nc-sa/2.0/'>CC BY-NC-SA 2.0 FR</a>], <a target='_blank' rel='noopener' href='" + gompelDroso + "'>See <i>D. yakuba</i> picture via Nicolas Gompel's lab website</a></p>");
        //        sources.append("<p><i>Pongo pygmaeus</i> picture by Greg Hume (Own work) [<a target='_blank' rel='noopener' href='" + creativeCommons + "/licenses/by-sa/3.0'>CC-BY-SA-3.0</a>], <a target='_blank' rel='noopener' href='" + commonsWikipedia + "/File%3ASUMATRAN_ORANGUTAN.jpg'>See <i>P. pygmaeus</i> picture via Wikimedia Commons</a></p>");
        //        sources.append("<p><i>Tetraodon nigroviridis</i> picture by Starseed (Own work) [<a target='_blank' rel='noopener' href='" + creativeCommons + "/licenses/by-sa/3.0/de/deed.en'>CC-BY-SA-3.0-de</a> or <a target='_blank' rel='noopener' href='" + creativeCommons + "/licenses/by-sa/3.0'>CC-BY-SA-3.0</a>], <a target='_blank' rel='noopener' href='" + commonsWikipedia + "/File%3ATetraodon_nigroviridis_1.jpg'>See <i>T. nigroviridis</i> picture via Wikimedia Commons</a></p>");
        sources.append("</div>");

        return log.traceExit(sources.toString());
    }

    /**
     * Get the src of the provided species for a HTML 'img' element.
     *
     * @param species       A {@code Species} that is the species for which the src is returned.
     * @param isLightImg    A {@code boolean} defining whether image should be the light version.
     * @return              The {@code String} that is the 'src' of the provided species
     *                      for the HTML 'img' element.
     */
    protected String getSpeciesImageSrc(Species species, boolean isLightImg) {
        log.traceEntry("{} - {}", species, isLightImg);

        StringBuilder src = new StringBuilder();
        src.append(this.prop.getBgeeRootDirectory()).append(this.prop.getSpeciesImagesRootDirectory());
        src.append(String.valueOf(species.getId()));
        if (species.getId() == HUMAN_SPECIES_ID) {
            src.append("_gtex");
        }
        src.append("_light.jpg");

        return log.traceExit(src.toString());
    }

    /**
     * @return  A {@code String} that is the formatted version number of the webapp.
     *          {@code null} if this information is not available.
     */
    protected String getWebAppVersion() {
        log.traceEntry();
        String version = null;
        if (StringUtils.isNotBlank(this.prop.getMajorVersion())) {
            version = this.prop.getMajorVersion();
            if (StringUtils.isNotBlank(this.prop.getMinorVersion())) {
                version += "." + this.prop.getMinorVersion();
            }
        }
        return log.traceExit(version);
    }

    /**
     * @param anatEntity    An {@code AnatEntity} that is the anat. entity for which build the URL.
     * @param text          A {@code String} that is the text to be displayed for the link.
     * @return              A {@code String} that is the URLs of the provided anat. entity.
     */
    protected String getAnatEntityUrl(AnatEntity anatEntity, String text) {
        log.traceEntry("{} - {}", anatEntity, text);
        if (anatEntity == null) {
            throw log.throwing(new IllegalArgumentException("The provided anat. entity should be not null"));
        }

        return log.traceExit("<a target='_blank' rel='noopener' href='" + UBERON_ID_URL +
                this.urlEncode(anatEntity.getId().replace(':', '_')) + "'>" + htmlEntities(text) +
                "</a>");
    }

    /**
     * @param taxon     A {@code Taxon} that is the taxon for which build the URL.
     * @return          A {@code String} that is the 'a' tag for the provided taxon.
     */
    protected String getTaxonUrl(Taxon taxon) {
        log.traceEntry("{}", taxon);
        if (taxon == null) {
            throw log.throwing(new IllegalArgumentException("The provided taxon should be not null"));
        }

        return log.traceExit("<a target='_blank' rel='noopener' href='" + NCBI_TAXONOMY_URL + taxon.getId()
                + "'>" + htmlEntities(taxon.getScientificName()) +
                "</a>");
    }

    protected String getSchemaMarkupGraph(List<String> properties) {
        log.traceEntry("{}", properties);

        return log.traceExit(
                "<script type='application/ld+json'>" +
                "    {" +
                "        \"@context\": \"https://schema.org\"," +
                "        \"@graph\": [" + String.join(",", properties) + "]" +
                "    }" +
                "</script>");
    }

    /**
     * Return the {@code String} representing the species scientific and common names as a HTML 'a' element.
     * The common name, surrounded by brackets, is displayed only if it is defined.
     *
     * @param species       A {@code Species} that is the species that should be displayed.
     * @param hasSchemaTag  A {@code boolean} defining whether the Bioschemas property 'name' should be set.
     * @return              The {@code String} that is the species scientific and common names.
     */
    protected String getCompleteSpeciesNameLink(Species species, boolean hasSchemaTag) {
        log.traceEntry("{} - {}", species, hasSchemaTag);
        return log.traceExit("<a href='" + getSpeciesPageUrl(species.getId()) + "'>"
                + getCompleteSpeciesName(species, hasSchemaTag) + "</a>");
    }

    /**
     * Return the {@code String} representing the species page URL.
     *
     * @param speciesId A {@code Integer} that is the species ID that should be used.
     * @return          The {@code String} that is the species page URL.
     */
    protected String getSpeciesPageUrl(Integer speciesId) {
        log.traceEntry("{}", speciesId);

        RequestParameters speciesPage = getNewRequestParameters();
        speciesPage.setPage(RequestParameters.PAGE_SPECIES);
        speciesPage.setSpeciesId(speciesId);

        return log.traceExit(speciesPage.getRequestURL());
    }
    /**
     * Return the {@code String} representing the species scientific and common names.
     * The common name, surrounded by brackets, is displayed only if it is defined.
     *
     * @param species       A {@code Species} that is the species that should be displayed.
     * @param hasSchemaTag  A {@code boolean} defining whether the Bioschemas property 'name' should be set.
     * @return              The {@code String} that is the species scientific and common names.
     */
    protected static String getCompleteSpeciesName(Species species, boolean hasSchemaTag) {
        log.traceEntry("{} - {}", species, hasSchemaTag);
        String schemaTag = hasSchemaTag? "property='bs:name'": "";
        return log.traceExit("<em " + schemaTag + ">" + htmlEntities(species.getScientificName()) + "</em>"
                + (StringUtils.isNotBlank(species.getName()) ? " (" + htmlEntities(species.getName()) + ")" : ""));
    }

    /**
     * Return the {@code String} that is the Dataset schema.org id.
     *
     * @param speciesId     An {@code Integer} that is the species ID that should be used.
     * @param category      A {@code Category} that is the type of the Dataset.
     * @return              The {@code String} that is the Dataset schema.org id.
     */
    protected String getDatasetSchemaId(Integer speciesId, SpeciesDownloadFile.Category category) {
        log.traceEntry("{} - {}", speciesId, category);

        String hash;
        switch (category) {
            case EXPR_CALLS_COMPLETE:
                hash = "expr-calls";
                break;
            case AFFY_DATA:
                hash = "proc-values-affymetrix";
                break;
            case RNASEQ_DATA:
                hash = "proc-values-rna-seq";
                break;
            default:
                throw log.throwing(new IllegalArgumentException(
                        "CategoryEnum not supported: " + category));
        }
        return log.traceExit(getSpeciesPageUrl(speciesId) + "#" + hash);
    }

    /**
     * Return the {@code String} that is the Dataset schema.org name.
     *
     * @param speciesId     An {@code Integer} that is the species ID that should be used.
     * @param category      A {@code Category} that is the type of the Dataset.
     * @return              The {@code String} that is the Dataset schema.org name.
     */
    protected String getDatasetSchemaName(Integer speciesId, SpeciesDownloadFile.Category category) {
        log.traceEntry("{} - {}", speciesId, category);
        if (category == null) {
            throw log.throwing(new IllegalArgumentException(
                    "CategoryEnum can not be null " + category));
        } else if (category == SpeciesDownloadFile.Category.EXPR_CALLS_COMPLETE) {
            return log.traceExit("expr-calls "+speciesId);
        } else if (category == SpeciesDownloadFile.Category.AFFY_DATA) {
            return log.traceExit("proc-values-affymetrix "+speciesId);
        } else if (category == SpeciesDownloadFile.Category.RNASEQ_DATA) {
            return log.traceExit("proc-values-rna-seq "+speciesId);
        } else {
            throw log.throwing(new IllegalArgumentException(
                    "CategoryEnum not supported: " + category));
        }
    }

    /**
     * Return the {@code String} that is the Dataset schema.org description.
     *
     * @param speciesId     An {@code Integer} that is the species ID that should be used.
     * @param category      A {@code Category} that is the type of the Dataset.
     * @return              The {@code String} that is the Dataset schema.org description.
     */
    protected String getDatasetSchemaDescription(Integer speciesId, SpeciesDownloadFile.Category category) {
        log.traceEntry("{} - {}", speciesId, category);
        if (category == null) {
            throw log.throwing(new IllegalArgumentException(
                    "CategoryEnum can not be null " + category));
        } else if (category == SpeciesDownloadFile.Category.EXPR_CALLS_COMPLETE) {
            return log.traceExit("Expression calls generated by Bgee for the species "+speciesId);
        } else if (category == SpeciesDownloadFile.Category.AFFY_DATA) {
            return log.traceExit("Affymetrix expression values processed for the species "+speciesId);
        } else if (category == SpeciesDownloadFile.Category.RNASEQ_DATA) {
            return log.traceExit("RNA-Seq expression values processed for the species "+speciesId);
        } else {
            throw log.throwing(new IllegalArgumentException(
                    "CategoryEnum not supported: " + category));
        }
    }
}
