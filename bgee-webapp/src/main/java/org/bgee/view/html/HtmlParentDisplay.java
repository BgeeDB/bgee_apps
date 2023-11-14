package org.bgee.view.html;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.BgeeProperties;
import org.bgee.controller.RequestParameters;
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
 * @version Bgee 14, Feb. 2018
 * @since   Bgee 13, Jul. 2014
 */
public class HtmlParentDisplay extends ConcreteDisplayParent {

    private final static Logger log = LogManager.getLogger(HtmlParentDisplay.class.getName());

    /**
     * A {@code String} that is the page name of the 'processed expression values' download page.
     */
    protected final static String PROCESSED_EXPR_VALUES_PAGE_NAME = "Processed expression values";
    /**
     * A {@code String} that is the page name of the 'gene expression calls' download page.
     */
    protected final static String GENE_EXPR_CALLS_PAGE_NAME = "Gene expression calls";
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
     * Escape HTML entities in the provided {@code String}
     * @param stringToWrite A {@code String} that contains the HTML to escape
     * @return  The escaped HTML
     */
    protected static String htmlEntities(String stringToWrite) {
        log.entry(stringToWrite);
        try {                            
            return log.exit(StringEscapeUtils.escapeHtml4(stringToWrite).replaceAll("'", "&apos;"));
        } catch (Exception e) {
            log.catching(e);
            return log.exit("");
        }
    }

    /**
     * Helper method to get an html tag
     * @param name    A {@code String} representing the name of the element 
     * @param content A {@code String} reprensenting the content of the element
     * @return The HTML code as {@code String}.
     */
     protected static String getHTMLTag(String name, String content) {
         log.entry(name, content);
         return log.exit(getHTMLTag(name, null, content));
     }
     
     /**
      * Helper method to get an html tag with attributes set. 
      * @param name          A {@code String} representing the name of the element 
      * @param attributes    A {@code Map} where keys are attribute names and values are attribute values.
      * @return The HTML code as {@code String}
      */
     protected static String getHTMLTag(String name, Map<String, String> attributes) {
         log.entry(name, attributes);
         return log.exit(getHTMLTag(name, attributes, null));
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
         log.entry(name, attributes, content);
         
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
         sb.append("</").append(name).append(">");
         return log.exit(sb.toString());
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
        log.entry(url, externalLink, title, figcaption, imgPath, desc);
        
        StringBuilder feature = new StringBuilder();
        feature.append("<div class='single_feature'>");
        feature.append("<a href='" + url + "' title='" + title + "'"
                + (externalLink ? " target='_blank'" : "") + ">" +
                "<figure><img src='" + imgPath + "' alt='" + title + " logo' />" +
                "<figcaption>" + figcaption + "</figcaption>" +
                "</figure></a>");
        if (desc != null && !desc.isEmpty()) {
            feature.append("<p>" + desc + "</p>");
        }
        feature.append("</div>");
        
        return log.exit(feature.toString());
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
        log.entry();
        return log.exit("text/html");
    }

    public void emptyDisplay() {
        log.entry();
        this.sendHeaders();
        this.writeln("");
        log.exit();
    }
    
    /**
     * URL encode the provided {@code String}, with the character encoding used to generate URLs. 
     * 
     * @param stringToWrite A {@code String} to be encoded.
     * @return              The encoded {@code String}.
     */
    protected String urlEncode(String stringToWrite) {
        log.entry(stringToWrite);
        try {                            
            return log.exit(java.net.URLEncoder.encode(stringToWrite, this.charEncoding));
        } catch (Exception e) {
            log.catching(e);
            return log.exit("");
        }
    }

    /**
     * Display the start of the HTML page (common to all pages).
     *
     * @param title A {@code String} that is the title to be used for the page. 
     */
    protected void startDisplay(String title) {
        log.entry(title);
        this.sendHeaders();
        this.writeln("<!DOCTYPE html>");
        this.writeln("<html lang='en' class='no-js'>");
        this.writeln("<head>");
        this.writeln("<meta charset='UTF-8'>");
        this.writeln("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        this.writeln("<title>"+title+"</title>");
        this.writeln("<meta name='description' content='Bgee is a database "
            + "for retrieval and comparison of gene expression patterns "
            + "across multiple animal species. It provides an intuitive answer to the question "
            + "-where is a gene expressed?- and supports research in cancer and agriculture "
            + "as well as evolutionary biology.'/>");
        this.writeln("<meta name='keywords' content='bgee, gene expression, evolution, "
                + "ontology, anatomy, development, evo-devo database, anatomical ontology, "
                + "developmental ontology, gene expression evolution'/>");
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
        
        this.writeln("<body>");
        this.writeln("<noscript>Sorry, your browser does not support JavaScript!</noscript>");
        this.writeln("<div id='bgee_top'><span id='TOP'></span></div>");
        this.writeln("<div id='sib_container' class='container-fluid'>");
        //FIXME: I noticed that this header disappear in printed version
        this.displayBgeeHeader();
        this.displayArchiveMessage();
        this.displayWarningMessage();
        this.writeln("<div id='sib_body'>");

        log.exit();
    }

    /**
     * Display the end of the HTML page (common to all pages).
     */
    protected void endDisplay() {
        log.entry();

        this.writeln("</div>"); // close sib_body
        
        //FIXME: I noticed that this footer disappear in printed version
        this.writeln("<nav id='bgee_footer' class='navbar navbar-default'>");
        this.writeln("<div class='container-fluid'>");

        this.writeln("<ul class='nav navbar-nav'>");
        this.writeln("<li><a href='http://www.sib.swiss'>SIB Swiss Institute of Bioinformatics</a></li>");
        this.writeln("</ul>");
        

        this.writeln("<ul class='nav navbar-nav navbar-right'>");
        this.writeln("<li><a href='#TOP' id='sib_footer_gototop'>"
                + "<span class='glyphicon glyphicon-menu-up'></span> Back to the top</a></li>");
        this.writeln("</ul>");
        
        this.writeln("</div>"); // close container
        this.writeln("</nav>"); // close bgee_footer nev

        this.writeln("</div>"); // close sib_container
        
        this.writeln("</body>");
        this.writeln("</html>");
        log.exit();
    }

    /**
     * Display the Bgee header of the HTML page.
     */
    private void displayBgeeHeader() {
        log.entry();
        
        RequestParameters urlTopAnat = this.getNewRequestParameters();
        urlTopAnat.setPage(RequestParameters.PAGE_TOP_ANAT);

        RequestParameters urlGeneSearch = this.getNewRequestParameters();
        urlGeneSearch.setPage(RequestParameters.PAGE_GENE);

        RequestParameters urlDownloadProcValueFile = this.getNewRequestParameters();
        urlDownloadProcValueFile.setPage(RequestParameters.PAGE_DOWNLOAD);
        urlDownloadProcValueFile.setAction(RequestParameters.ACTION_DOWLOAD_PROC_VALUE_FILES);
        
        RequestParameters urlDownloadExprCallFiles = this.getNewRequestParameters();
        urlDownloadExprCallFiles.setPage(RequestParameters.PAGE_DOWNLOAD);
        urlDownloadExprCallFiles.setAction(RequestParameters.ACTION_DOWLOAD_CALL_FILES);

        RequestParameters urlDocBgeeAccess = this.getNewRequestParameters();
        urlDocBgeeAccess.setPage(RequestParameters.PAGE_DOCUMENTATION);
        urlDocBgeeAccess.setAction(RequestParameters.ACTION_DOC_HOW_TO_ACCESS);

        RequestParameters urlDocExprCallFiles = this.getNewRequestParameters();
        urlDocExprCallFiles.setPage(RequestParameters.PAGE_DOCUMENTATION);
        urlDocExprCallFiles.setAction(RequestParameters.ACTION_DOC_CALL_DOWLOAD_FILES);

        RequestParameters urlDocTopAnat = this.getNewRequestParameters();
        urlDocTopAnat.setPage(RequestParameters.PAGE_DOCUMENTATION);
        urlDocTopAnat.setAction(RequestParameters.ACTION_DOC_TOP_ANAT);
        
        RequestParameters urlBgeeSources = this.getNewRequestParameters();
        urlBgeeSources.setPage(RequestParameters.PAGE_SOURCE);

        RequestParameters urlAbout = this.getNewRequestParameters();
        urlAbout.setPage(RequestParameters.PAGE_ABOUT);

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
        navbar.append("<li><a title='TopAnat: Enrichment analyses of expression localization' "
              + "href='" + urlTopAnat.getRequestURL() + "'>" + TOP_ANAT_PAGE_NAME + "</a></li>");
        navbar.append("</ul>");
        navbar.append("</li>");

        // Search
        // For the moment, we only have gene search 
        navbar.append("<li class='dropdown'>");
        navbar.append("<a href='#' class='dropdown-toggle' data-toggle='dropdown' role='button' "
              + "aria-haspopup='true' aria-expanded='false'>Search <span class='caret'></span></a>");
        navbar.append("<ul class='dropdown-menu'>");
        navbar.append("<li><a title='Gene search' href='" + urlGeneSearch.getRequestURL() + 
              "'>Gene search</a></li>");
        navbar.append("</ul>");
        navbar.append("</li>");

        // Download
        navbar.append("<li class='dropdown'>");
        navbar.append("<a href='#' class='dropdown-toggle' data-toggle='dropdown' role='button' "
              + "aria-haspopup='true' aria-expanded='false'>Download <span class='caret'></span></a>");
        navbar.append("<ul class='dropdown-menu'>");
        navbar.append("<li><a href='" + urlDownloadExprCallFiles.getRequestURL() + "'>"
              + GENE_EXPR_CALLS_PAGE_NAME + "</a></li>");
        navbar.append("<li><a href='" + urlDownloadProcValueFile.getRequestURL() + "'>"
              + PROCESSED_EXPR_VALUES_PAGE_NAME + "</a></li>");
        navbar.append("</ul>");
        navbar.append("</li>");

        // Documentation
        navbar.append("<li class='dropdown'>");
        navbar.append("<a href='#' class='dropdown-toggle' data-toggle='dropdown' role='button' "
              + "aria-haspopup='true' aria-expanded='false'>Documentation <span class='caret'></span></a>");
        navbar.append("<ul class='dropdown-menu'>");
        navbar.append("<li><a title='See how to access to Bgee data' href='" + urlDocBgeeAccess.getRequestURL()
              + "'>How to access Bgee data</a></li>");
        navbar.append("<li><a title='TopAnat documentation' href='" + urlDocTopAnat.getRequestURL()
              + "'>" + TOP_ANAT_PAGE_NAME + "</a></li>");
        navbar.append("<li><a title='Gene expression call files documentation' href='" + 
              urlDocExprCallFiles.getRequestURL() + "'>" + GENE_EXPR_CALLS_PAGE_NAME + "</a></li>");
//        navbar.append("<li><a title='Processed expression value files documentation' href='" + 
//            urlDocProcValueFiles.getRequestURL() + "'>" + PROCESSED_EXPR_VALUES_PAGE_NAME + "</a></li>");
        navbar.append("<li><a title='Bgee blog' href='https://bgeedb.wordpress.com' target='_blank'>Bgee blog</a></li>");
        navbar.append("<li><a title='Bgee sources' href='" + urlBgeeSources.getRequestURL()
              + "'>Bgee sources</a></li>");
        navbar.append("</ul>");
        navbar.append("</li>");
        
        // About
        navbar.append("<li><a title='About page' href='" + urlAbout.getRequestURL() + "'>About</a></li>");
        
        // Help
        navbar.append("<li>" + this.getObfuscateEmail() + "</li>");

        navbar.append("</ul>"); // close left nav links

        // Right nav links
        navbar.append("<ul class='nav navbar-nav navbar-right'>");
        
        // Twitter
        navbar.append("<li><a title='Follow @Bgeedb on Twitter' target='_blank' href='https://twitter.com/Bgeedb'>" + 
                "<img class='social-img' alt='Twitter logo' src='" + this.prop.getBgeeRootDirectory() + this.prop.getLogoImagesRootDirectory() + 
                "twitter_logo.png'></a></li>");

        // Blog
        navbar.append("<li><a title='See our blog' target='_blank' href='https://bgeedb.wordpress.com'>" + 
                "<img class='social-img' alt='Wordpress logo' src='" + this.prop.getBgeeRootDirectory() + this.prop.getLogoImagesRootDirectory() + 
                "wordpress_logo.png'></a></li>");
        
        // SIB
        navbar.append("<li><a id='sib_brand' href='https://www.sib.swiss' target='_blank' "
                + "title='Link to the SIB Swiss Institute of Bioinformatics'>"
                + "<img src='" + this.prop.getBgeeRootDirectory() + this.prop.getLogoImagesRootDirectory() +
                "sib_emblem.png' alt='SIB Swiss Institute of Bioinformatics' /></a></li>");

        navbar.append("</ul>");  // close right nav links
        
        navbar.append("</div>"); // close nav links

        navbar.append("</nav>"); // close navbar navbar-default
        
        this.writeln(navbar.toString());
        log.exit();
    }
    
    /**
     * Display a warning message on all pages if {@link BgeeProperties#getWarningMessage()} 
     * returns a non-blank value (see {@link #prop}).
     */
    private void displayWarningMessage() {
        log.entry();
        
        if (StringUtils.isNotBlank(this.prop.getWarningMessage())) {
            this.writeln("<div class='alert alert-warning'>" + 
                htmlEntities(this.prop.getWarningMessage()) + 
            "</div>");
        }
        
        log.exit();
    }

    /**
     * Display a archive message on all pages if {@link BgeeProperties#isArchive()}
     * returns {@code true} (see {@link #prop}).
     */
    private void displayArchiveMessage() {
        log.entry();

        if (this.prop.isArchive()) {
            this.write("<div class='alert alert-danger'> This is an old version of Bgee ");

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

        log.exit();
    }

    /**
     * @return          the {@code String} that is the HTML code of the Contact link.
     */
    //TODO move javascript in common.js
    private String getObfuscateEmail() {
        return "<script type='text/javascript'>eval(unescape("
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
                + "%29%22%3E%48%65%6C%70%3C%2F%61%3E%27%29%3B'));</script>";
    }

    /**
     * Get the main logo of the documentation page, as HTML 'div' element.
     *
     * @return  A {@code String} that is the main documentation logo as HTML 'div' element,
     *          formated in HTML and HTML escaped if necessary.
     */
    protected String getMainDocumentationLogo() {
        log.entry();
        
        RequestParameters urlDocumentationGenerator = this.getNewRequestParameters();
        urlDocumentationGenerator.setPage(RequestParameters.PAGE_DOCUMENTATION);
    
        return log.exit(HtmlParentDisplay.getSingleFeatureLogo(
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
//        log.entry();
//        
//        RequestParameters urlDownloadGenerator = this.getNewRequestParameters();
//        urlDownloadGenerator.setPage(RequestParameters.PAGE_DOWNLOAD);
//    
//        return log.exit(HtmlParentDisplay.getSingleFeatureLogo(urlDownloadGenerator.getRequestURL(), 
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
        log.entry();

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
                "Annotations and processed expression data (e.g., read counts, RPKM values, "
                + "Affymetrix probeset signal intensities)."));
        
        return log.exit(logos.toString());
    }
    
    /**
     * TODO comment
     */
    protected String displayHelpLink(String cat, String display) {
        //TODO: to provide the cat, use a html5 data- attribute rather than 
        //a formatted String for the class attribute
        log.entry(cat, display);
        return log.exit("<span class='help'><a href='#' class='help|" + 
                cat + "'>" + display + "</a></span>");
    }
    
    /**
     * TODO comment
     */
    protected String displayHelpLink(String cat) {
        log.entry(cat);
        return log.exit(this.displayHelpLink(cat, "[?]"));
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
        log.entry();
        if (!this.prop.isMinify()) {
            this.includeJs("lib/jquery.min.js");
            this.includeJs("lib/jquery_plugins/bootstrap.min.js");
            this.includeJs("lib/jquery_plugins/jquery.visible.min.js");
            this.includeJs("lib/jquery_plugins/jquery-ui.min.js");
            this.includeJs("lib/jquery_plugins/toastr.min.js");
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
        log.exit();
    }
    /**
     * Write the HTML code allowing to include the javascript file named {@code fileName}. 
     * This method will notably retrieve the directory hosting the files, and will 
     * define the versioned file name corresponding to {@code fileName}, as hosted 
     * on the server. HTML is written using {@link #writeln()}.
     * <strong>It should be called only within a {@link #includeJs()} method, whether overridden 
     * or not.</strong>.
     * 
     * @param fileName  The original name of the javascript file to include.
     * @see #getVersionedJsFileName(String)
     */
    protected void includeJs(String fileName) {
        log.entry(fileName);
        this.writeln("<script type='text/javascript' src='" +
                this.prop.getBgeeRootDirectory() + this.prop.getJavascriptFilesRootDirectory() + 
                this.getVersionedJsFileName(fileName) + "'></script>");
        log.exit();
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
        log.entry(originalFileName);
        if (!originalFileName.endsWith(".js")) {
            throw log.throwing(new IllegalArgumentException("The provided file name "
                    + "must end with an extension '.js'."));
        }
        //if no version info was provided, or if we don't want to use the minified files, 
        //return original name.
        if (StringUtils.isBlank(this.prop.getJavascriptVersionExtension()) || 
                !this.prop.isMinify()) {
            return log.exit(originalFileName);
        }
        return log.exit(originalFileName.replaceAll("(.+?)\\.js", 
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
        if (!this.prop.isMinify()) {
            this.includeCss("lib/jquery_plugins/bootstrap.min.css");
            this.includeCss("lib/jquery_plugins/jquery-ui.min.css");
            this.includeCss("lib/jquery_plugins/jquery-ui.structure.min.css");
            this.includeCss("lib/jquery_plugins/jquery-ui.theme.min.css");
            this.includeCss("lib/jquery_plugins/toastr.min.css");
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
    }
    /**
     * Write the HTML code allowing to include the CSS file named {@code fileName}. 
     * This method will notably retrieve the directory hosting the files, and will 
     * define the versioned file name corresponding to {@code fileName}, as hosted 
     * on the server. HTML is written using {@link #writeln()}.
     * <strong>It should be called only within a {@link #includeCss()} method, whether overridden 
     * or not.</strong>.
     * 
     * @param fileName  The original name of the CSS file to include.
     * @see #getVersionedCssFileName(String)
     */
    protected void includeCss(String fileName) {
        log.entry(fileName);
        this.writeln("<link rel='stylesheet' type='text/css' href='"
                + this.prop.getBgeeRootDirectory() + this.prop.getCssFilesRootDirectory() 
                + this.getVersionedCssFileName(fileName) + "'/>");
        log.exit();
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
        log.entry(originalFileName);
        if (!originalFileName.endsWith(".css")) {
            throw log.throwing(new IllegalArgumentException("The provided file name "
                    + "must end with an extension '.css'."));
        }
        //if no version info was provided, or if we don't want to use the minified files, 
        //return original name.
        if (StringUtils.isBlank(this.prop.getCssVersionExtension()) || 
                !this.prop.isMinify()) {
            return log.exit(originalFileName);
        }
        return log.exit(originalFileName.replaceAll("(.+?)\\.css", 
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
        log.entry();
        return log.exit(new RequestParameters(
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
     *          See {@link getHtmlFactory()} for a method returning directly the factory 
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
        log.entry();
        
        StringBuilder sources = new StringBuilder();
        sources.append("<p id='creativecommons_title'>Images from Wikimedia Commons. In most cases, pictures corresponds to the sequenced strains. <a>Show information about original images.</a></p>");
        sources.append("<div id='creativecommons'>");
        sources.append("<p><i>Homo sapiens</i> picture by Leonardo da Vinci (Life time: 1519) [Public domain]. <a target='_blank' href='http://commons.wikimedia.org/wiki/File:Da_Vinci%27s_Anatomical_Man.jpg#mediaviewer/File:Da_Vinci%27s_Anatomical_Man.jpg'>See <i>H. sapiens</i> picture via Wikimedia Commons</a></p>");
        sources.append("<p><i>Mus musculus</i> picture by Rasbak [<a target='_blank' href='http://www.gnu.org/copyleft/fdl.html'>GFDL</a> or <a target='_blank' href='http://creativecommons.org/licenses/by-sa/3.0/'>CC-BY-SA-3.0</a>], <a target='_blank' href='http://commons.wikimedia.org/wiki/File%3AApodemus_sylvaticus_bosmuis.jpg'>See <i>M. musculus</i> picture via Wikimedia Commons</a></p>");
        sources.append("<p><i>Danio rerio</i> picture by Azul (Own work) [see page for license], <a target='_blank' href='http://commons.wikimedia.org/wiki/File%3AZebrafisch.jpg'>See <i>D. rerio</i> picture via Wikimedia Commons</a></p>");
        sources.append("<p><i>Drosophila melanogaster</i> picture by Andr&eacute; Karwath aka Aka (Own work) [<a target='_blank' href='http://creativecommons.org/licenses/by-sa/2.5'>CC-BY-SA-2.5</a>], <a target='_blank' href='http://commons.wikimedia.org/wiki/File%3ADrosophila_melanogaster_-_side_(aka).jpg'>See <i>D. melanogaster</i> picture via Wikimedia Commons</a></p>");
        sources.append("<p><i>Caenorhabditis elegans</i> picture by Bob Goldstein, UNC Chapel Hill http://bio.unc.edu/people/faculty/goldstein/ (Own work) [<a target='_blank' href='http://creativecommons.org/licenses/by-sa/3.0'>CC-BY-SA-3.0</a>], <a target='_blank' href='http://commons.wikimedia.org/wiki/File%3ACelegansGoldsteinLabUNC.jpg'>See <i>C. elegans</i> picture via Wikimedia Commons</a></p>");
        sources.append("<p><i>Pan paniscus</i> picture by Ltshears (Own work) [<a target='_blank' href='http://creativecommons.org/licenses/by-sa/3.0'>CC-BY-SA-3.0</a> or <a target='_blank' href='http://www.gnu.org/copyleft/fdl.html'>GFDL</a>], <a target='_blank' href='http://commons.wikimedia.org/wiki/File%3ABonobo1_CincinnatiZoo.jpg'>See <i>P. paniscus</i> picture via Wikimedia Commons</a></p>");
        sources.append("<p><i>Pan troglodytes</i> picture by Thomas Lersch (Own work) [<a target='_blank' href='http://www.gnu.org/copyleft/fdl.html'>GFDL</a>, <a target='_blank' href='http://creativecommons.org/licenses/by-sa/3.0/'>CC-BY-SA-3.0</a> or <a target='_blank' href='http://creativecommons.org/licenses/by/2.5'>CC-BY-2.5</a>], <a target='_blank' href='http://commons.wikimedia.org/wiki/File%3ASchimpanse_Zoo_Leipzig.jpg'>See <i>P. troglodytes</i> picture via Wikimedia Commons</a></p>");
        sources.append("<p><i>Gorilla gorilla</i> picture by Brocken Inaglory (Own work) [<a target='_blank' href='http://creativecommons.org/licenses/by-sa/3.0'>CC-BY-SA-3.0</a> or <a target='_blank' href='http://www.gnu.org/copyleft/fdl.html'>GFDL</a>], <a target='_blank' href='http://commons.wikimedia.org/wiki/File%3AMale_gorilla_in_SF_zoo.jpg'>See <i>G. gorilla</i> picture via Wikimedia Commons</a></p>");
        sources.append("<p><i>Pongo pygmaeus</i> picture by Greg Hume (Own work) [<a target='_blank' href='http://creativecommons.org/licenses/by-sa/3.0'>CC-BY-SA-3.0</a>], <a target='_blank' href='http://commons.wikimedia.org/wiki/File%3ASUMATRAN_ORANGUTAN.jpg'>See <i>P. pygmaeus</i> picture via Wikimedia Commons</a></p>");
        sources.append("<p><i>Macaca mulatta</i> picture by Aiwok (Own work) [<a target='_blank' href='http://www.gnu.org/copyleft/fdl.html'>GFDL</a> or <a target='_blank' href='http://creativecommons.org/licenses/by-sa/3.0'>CC-BY-SA-3.0-2.5-2.0-1.0</a>], <a target='_blank' href='http://commons.wikimedia.org/wiki/File%3AMacaca_mulatta_3.JPG'>See <i>M. mulatta</i> picture via Wikimedia Commons</a></p>");
        sources.append("<p><i>Rattus norvegicus</i> picture by Reg Mckenna (originally posted to Flickr as Wild Rat) [<a target='_blank' href='http://creativecommons.org/licenses/by/2.0'>CC-BY-2.0</a>], <a target='_blank' href='http://commons.wikimedia.org/wiki/File%3AWildRat.jpg'>See <i>R. norvegicus</i> picture via Wikimedia Commons</a></p>");
        sources.append("<p><i>Bos taurus</i> picture by User Robert Merkel on en.wikipedia (US Department of Agriculture) [Public domain], <a target='_blank' href='http://commons.wikimedia.org/wiki/File%3AHereford_bull_large.jpg'>See <i>B. taurus</i> picture via Wikimedia Commons</a></p>");
        sources.append("<p><i>Sus scrofa</i> picture by Joshua Lutz (Own work) [Public domain], <a target='_blank' href='http://commons.wikimedia.org/wiki/File%3ASus_scrofa_scrofa.jpg'>See <i>S. scrofa</i> picture via Wikimedia Commons</a></p>");
        sources.append("<p><i>Monodelphis domestica</i> picture by <i>Marsupial Genome Sheds Light on the Evolution of Immunity.</i> Hill E, PLoS Biology Vol. 4/3/2006, e75 <a rel='nofollow' href='http://dx.doi.org/10.1371/journal.pbio.0040075'>http://dx.doi.org/10.1371/journal.pbio.0040075</a> [<a target='_blank' href='http://creativecommons.org/licenses/by/2.5'>CC-BY-2.5</a>], <a target='_blank' href='http://commons.wikimedia.org/wiki/File%3AOpossum_with_young.png'>See <i>M. domestica</i> picture via Wikimedia Commons</a></p>");
        sources.append("<p><i>Ornithorhynchus anatinus</i> picture by Dr. Philip Bethge (private) [<a target='_blank' href='http://www.gnu.org/copyleft/fdl.html'>GFDL</a> or <a target='_blank' href='http://creativecommons.org/licenses/by-sa/3.0'>CC-BY-SA-3.0-2.5-2.0-1.0</a>], <a target='_blank' href='http://commons.wikimedia.org/wiki/File%3AOrnithorhynchus.jpg'>See <i>O. anatinus</i> picture via Wikimedia Commons</a></p>");
        sources.append("<p><i>Gallus gallus</i> picture by Subramanya C K (Own work) [<a target='_blank' href='http://creativecommons.org/licenses/by-sa/3.0'>CC-BY-SA-3.0</a>], <a target='_blank' href='http://commons.wikimedia.org/wiki/File%3ARed_jungle_fowl.png'>See <i>G. gallus</i> picture via Wikimedia Commons</a></p>");
        sources.append("<p><i>Anolis carolinensis</i> picture by PiccoloNamek (Moved from Image:P1010027.jpg) [<a target='_blank' href='http://www.gnu.org/copyleft/fdl.html'>GFDL</a> or <a target='_blank' href='http://creativecommons.org/licenses/by-sa/3.0/'>CC-BY-SA-3.0</a>], <a target='_blank' href='http://commons.wikimedia.org/wiki/File%3AAnolis_carolinensis.jpg'>See <i>A. carolinensis</i> picture via Wikimedia Commons</a></p>");
        sources.append("<p><i>Xenopus tropicalis</i> picture by V&aacute;clav Gvo&zcaron;d&iacute;k (http://calphotos.berkeley.edu) [<a target='_blank' href='http://creativecommons.org/licenses/by-sa/2.5'>CC-BY-SA-2.5</a>, <a target='_blank' href='http://creativecommons.org/licenses/by-sa/2.5'>CC-BY-SA-2.5</a> or <a target='_blank' href='http://creativecommons.org/licenses/by-sa/3.0'>CC-BY-SA-3.0</a>], <a target='_blank' href='http://commons.wikimedia.org/wiki/File%3AXenopus_tropicalis01.jpeg'>See <i>X. tropicalis</i> picture via Wikimedia Commons</a></p>");
        sources.append("<p><i>Tetraodon nigroviridis</i> picture by Starseed (Own work) [<a target='_blank' href='http://creativecommons.org/licenses/by-sa/3.0/de/deed.en'>CC-BY-SA-3.0-de</a> or <a target='_blank' href='http://creativecommons.org/licenses/by-sa/3.0'>CC-BY-SA-3.0</a>], <a target='_blank' href='http://commons.wikimedia.org/wiki/File%3ATetraodon_nigroviridis_1.jpg'>See <i>T. nigroviridis</i> picture via Wikimedia Commons</a></p>");
        sources.append("</div>");
    
        return log.exit(sources.toString());
    }

    /**
     * @return  A {@code String} that is the formatted version number of the webapp.
     *          {@code null} if this information is not available.
     */
    protected String getWebAppVersion() {
        log.entry();
        String version = null;
        if (StringUtils.isNotBlank(this.prop.getMajorVersion())) {
            version = this.prop.getMajorVersion();
            if (StringUtils.isNotBlank(this.prop.getMinorVersion())) {
                version += "." + this.prop.getMinorVersion();
            }
        }
        return log.exit(version);
    }
}
