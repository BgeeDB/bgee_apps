package org.bgee.view.html;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.BgeeProperties;
import org.bgee.controller.RequestParameters;
import org.bgee.model.file.SpeciesDataGroup;
import org.bgee.model.species.Species;
import org.bgee.view.GeneralDisplay;
import org.bgee.view.html.HtmlDownloadDisplay.DownloadPageType;

/**
 * HTML View for the general category display
 * 
 * @author Mathieu Seppey
 * @author Frederic Bastian
 * @author Valentine Rech de Laval
 * @author Philippe Moret
 * @version Bgee 13, Feb. 2016
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
    public void displayHomePage(List<SpeciesDataGroup> groups) {
        log.entry(groups);

        this.startDisplay("Welcome to Bgee: a dataBase for Gene Expression Evolution");

        if (groups.stream().anyMatch(SpeciesDataGroup::isMultipleSpecies)) {
            throw log.throwing(new IllegalArgumentException(
                    "Only single-species groups should be displayed on the home page."));
        }

        this.displayStartBanner();

        this.displaySpeciesBanner(groups);

        this.displayExplanation();

        this.writeln("<hr class='home-divider'/>");
        
        this.displayHomePageSpecies(groups);
        
        this.displayNews();

        this.displayMoreInfo();

        this.writeln("<hr class='home-divider'/>");

        this.displayStartButtons();

        this.writeln("<hr class='home-divider'/>");

        this.writeln(getImageSources());

        this.endDisplay();
        
        log.exit();
    }
    
    
    /**TODO
	 * @param groups
	 */
	private void displaySpeciesBanner(List<SpeciesDataGroup> groups) {
	    log.entry(groups);
	
	    StringBuilder homePageSpeciesSection = new StringBuilder();
	    
	    groups.stream().filter(sdg -> sdg.isSingleSpecies()).forEach(sdg -> {
	        Species species = sdg.getMembers().get(0);
	        Map<String,String> attrs = new HashMap<>();
	        attrs.put("src", this.prop.getSpeciesImagesRootDirectory() + htmlEntities(species.getId())+"_light.jpg");
	        attrs.put("alt", htmlEntities(species.getShortName()));
	        attrs.put("class", "species_img");
	        homePageSpeciesSection.append(getHTMLTag("img", attrs));
	    });
	
	    this.writeln("<div id='bgee_species' class='row'>");
        this.writeln("<div class='hidden-xs col-sm-12'>");
	    this.writeln(homePageSpeciesSection.toString());
	    this.writeln("</div>");
	    this.writeln("</div>");
	
	    log.exit();
	}

	/**
	 * TODO
	 */
	private void displayStartBanner() {
		log.entry();
			
	    this.writeln("<div id='bgee_start' class='row'>");
	    
	    //TODO: manage the version either from database, or from bgee-webapp.properties file.
	    this.writeln("<span id='bgee_version'>version 13.1</span>");

	    this.writeln("<div id='bgee_hp_logo'><img src='" + this.prop.getLogoImagesRootDirectory() 
	            + "bgee13_hp_logo.png' alt='Bgee logo'></div>");
	
	    this.writeln("<div class='mini_text'>Gene expression data in animals</div>");

	    this.displayStartButtons();
	
	    this.writeln("</div>"); // close bgee_start row
	
	    log.exit();
	}

	private void displayStartButtons() {
		RequestParameters urlTopAnat = this.getNewRequestParameters();
	    urlTopAnat.setPage(RequestParameters.PAGE_TOP_ANAT);
	    RequestParameters urlGeneSearch = this.getNewRequestParameters();
	    urlGeneSearch.setPage(RequestParameters.PAGE_GENE);
	    RequestParameters urlDownload = this.getNewRequestParameters();
	    urlDownload.setPage(RequestParameters.PAGE_DOWNLOAD);
	    
	    this.writeln("<div id='start_buttons'>");
	    this.writeln("<a href='"+ urlTopAnat.getRequestURL() + 
	    		"'><span class='glyphicon glyphicon-stats'></span>Expression enrichment analysis</a>");
	    this.writeln("<a href='"+ urlGeneSearch.getRequestURL() + 
	    		"'><span class='glyphicon glyphicon-search'></span>Gene search</a>");
	    this.writeln("<a href='"+ urlDownload.getRequestURL() + 
	    		"'><span class='glyphicon glyphicon-download'></span>Download</a>");
	    this.writeln("</div>"); // close start_buttons
	}

	/**
     * TODO
     */
    private void displayExplanation() {
    	log.entry();
    	
        this.writeln("<div id='bgee_explanations' class='row home_page_section'>");
		
        this.writeln("<div class='col-sm-4'>");
        this.writeln("<h2>Gene expression data</h2>");
        this.writeln("<p>Bgee is a database to retrieve and compare gene expression patterns "
                + "in multiple animal species. Bgee curates heterogeneous expression data "
                + "(RNA-Seq, Affymetrix, <em>in situ</em> hybridization, and EST data), "
                + "and performs analyses to extract meaningful and comparable signal of expression.</p>");
        this.writeln("</div>");
        
        this.writeln("<div class='col-sm-4'>");
        this.writeln("<h2>Simply normal</h2>");
        this.writeln("<p>Bgee is based exclusively on curated \"normal\", healthy, expression data "
                + "(e.g., no gene knock-out, no treatment, no disease), "
                + "to provide a comparable reference of normal gene expression.</p>");
        this.writeln("</div>");

        this.writeln("<div class='col-sm-4'>");
        this.writeln("<h2>Comparable between species</h2>");
        this.writeln("<p>Bgee produces comparable calls of baseline presence/absence of expression, "
                + "and of differential over-/under-expression, that are then "
                + "integrated along with information of gene orthology, and of homology "
                + "between organs. This allows comparisons of expression patterns "
                + "between species.</p>");
        this.writeln("</div>");

        this.writeln("</div>"); // close bgee_explanations

        log.exit();
	}
    
    /**TODO
     * @param groups
     */
    private void displayHomePageSpecies(List<SpeciesDataGroup> groups) {
    	log.entry(groups);
    	
	    this.writeln("<div id='bgee_data' class='panel panel-default'>");
	    this.writeln("<div class='panel-heading'>");
	    this.writeln("<span class='panel-title'>Species with data in Bgee"
                + "    <span class='header_details'>(click on species to see more details)"
                + "</span></span>");
	    this.writeln("</div>"); // close panel-heading
	    
	    this.writeln("<div class='panel-body'>");

	    // Single species part
    	String homePageSpeciesSection;
    	try {
    		homePageSpeciesSection = ((HtmlDownloadDisplay) this.getFactory().getDownloadDisplay())
    				.getSingleSpeciesSection(DownloadPageType.HOME_PAGE, groups, true);
    	} catch (IOException|ClassCastException e) {
        	return;
    	}

    	// Black banner when a species or a group is selected.
    	homePageSpeciesSection += this.getDownloadBanner();

    	this.writeln(homePageSpeciesSection);
    	
    	this.writeln("</div>");
    	this.writeln("</div>");
    	
    	log.exit();
    }

    /**
     * Get the banner of a download page as a HTML 'div' element, 
     * according the provided page type.
     *
     * @return  the {@code String} that is the black banner of a download page 
     *          as a HTML 'div' element according {@code pageType}.
     */
    // TODO: DRY: copy from HtmlDownloadDisplay  
    private String getDownloadBanner() {
    	log.entry();

    	StringBuilder banner = new StringBuilder();
    	// This section is empty, it will be filled by JavaScript.
    	banner.append("<div id='bgee_data_selection'>");
    	// Cross to close the banner
    	banner.append("<div id='bgee_data_selection_cross'>");
    	banner.append("<img src='" + this.prop.getImagesRootDirectory() + "cross.png' " +
    			"title='Close banner' alt='Cross' />");
    	banner.append("</div>");

    	// Section on the right of the black banner
    	banner.append("<div id='bgee_data_selection_text'>");
    	banner.append("<h1 class='scientificname'></h1><h1 class='commonname'></h1>");

    	RequestParameters urlProcExprValues = this.getNewRequestParameters();
    	urlProcExprValues.setPage(RequestParameters.PAGE_DOWNLOAD);
    	urlProcExprValues.setAction(RequestParameters.ACTION_DOWLOAD_PROC_VALUE_FILES);
    	RequestParameters urlGeneExprCalls = this.getNewRequestParameters();
    	urlGeneExprCalls.setPage(RequestParameters.PAGE_DOWNLOAD);
    	urlGeneExprCalls.setAction(RequestParameters.ACTION_DOWLOAD_CALL_FILES);
    	banner.append("<ul>");
    	banner.append("<li><img class='bullet_point' src='" + this.prop.getImagesRootDirectory() + "arrow.png' alt='Arrow' />" +
    			"<a id='processed_expression_values_link' class='data_page_link' href='" +
    			urlProcExprValues.getRequestURL() + "' title='Bgee processed expression values'>" +
    			"See RNA-Seq and Affymetrix data</a></li>");
    	banner.append("<li><img class='bullet_point' src='" + this.prop.getImagesRootDirectory() + "arrow.png' alt='Arrow' />" +
    			"<a id='gene_expression_calls_link' class='data_page_link' href='" +
    			urlGeneExprCalls.getRequestURL() +
    			"' title='Bgee gene expression calls'>See gene expression calls</a></li>");
    	banner.append("</ul>");
    	banner.append("</div>"); // close bgee_data_selection_text
    	banner.append("</div>"); // close 

    	return log.exit(banner.toString());
    }
    
	/**
	 * TODO
	 */
	private void displayNews() {
	    log.entry();
	    
	    RequestParameters urlTopAnat = this.getNewRequestParameters();
	    urlTopAnat.setPage(RequestParameters.PAGE_TOP_ANAT);
	    
	    RequestParameters urlDownload = this.getNewRequestParameters();
	    urlDownload.setPage(RequestParameters.PAGE_DOWNLOAD);
	    
	    RequestParameters urlDownloadProcValues = this.getNewRequestParameters();
	    urlDownloadProcValues.setPage(RequestParameters.PAGE_DOWNLOAD);
	    urlDownloadProcValues.setAction(RequestParameters.ACTION_DOWLOAD_PROC_VALUE_FILES);
	    
	    RequestParameters urlDownloadCalls = this.getNewRequestParameters();
	    urlDownloadCalls.setPage(RequestParameters.PAGE_DOWNLOAD);
	    urlDownloadCalls.setAction(RequestParameters.ACTION_DOWLOAD_CALL_FILES);
	    
	    RequestParameters urlCallDoc = this.getNewRequestParameters();
	    urlCallDoc.setPage(RequestParameters.PAGE_DOCUMENTATION);
	    urlCallDoc.setAction(RequestParameters.ACTION_DOC_CALL_DOWLOAD_FILES);
	    
	    this.writeln("<div id='bgee_news' class='panel panel-default'>");
	    this.writeln("<div class='panel-heading'>");
	    this.writeln("<span class='panel-title'>News"
	                 + "    <span class='header_details'>(features are being added incrementally)"
	                 + "</span></span>");
	    this.writeln("</div>"); // close panel-heading
	    
	    this.writeln("<div class='panel-body'>");
	    
	    this.writeOneNews("2015-12-24", "major update of <a href='" + urlTopAnat.getRequestURL()
	                      + "' title='Perform gene expression enrichment tests with TopAnat'>TopAnat</a>. "
	                      + "Happy Christmas!");
	    
	    this.writeOneNews("2015-11-24", "we are happy to release of our new exclusive tool "
	                      + "for gene expression enrichment analyses: <a href='" + urlTopAnat.getRequestURL()
	                      + "' title='Perform gene expression enrichment tests with TopAnat'>TopAnat</a>. "
	                      + "This is a tool with absolutely no equivalent, developped in collaboration with "
	                      + "the Web-Team  of the Swiss Institute of Bioinformatics. Check it out!");
	    this.writeOneNews("2015-08-26", "update of the home page.");
	    
	    this.writeOneNews("2015-06-08", "release of Bgee release 13.1: "
	                      + "<ul>"
	                      + "<li>Update of the website interfaces.</li>"
	                      + "<li><a href='" + urlDownloadProcValues.getRequestURL()
	                      + "'>New download page</a> providing processed expression values.</li>"
	                      + "<li>Addition of mouse <i>in situ</i> data from MGI, see "
	                      + "<a href='" + urlDownloadCalls.getRequestURL() + "#id10090"
	                      + "'>mouse data</a>.</li>"
	                      + "<li>Differential expression data have been added for "
	                      + "<a href='" + urlDownloadCalls.getRequestURL() + "#id7955"
	                      + "'>zebrafish</a>, <a href='" + urlDownloadCalls.getRequestURL() + "#id9598"
	                      + "'>chimpanzee</a>, <a href='" + urlDownloadCalls.getRequestURL() + "#id9593"
	                      + "'>gorilla</a>, and <a href='" + urlDownloadCalls.getRequestURL() + "#id13616"
	                      + "'>opossum</a>.</li>"
	                      + "<li>Addition of new multi-species differential expression data, see "
	                      + "for instance <a href='" + urlDownloadCalls.getRequestURL() + "#id9598_9544"
	                      + "'>chimpanzee/macaque comparison</a>.</li>"
	                      + "<li>New format to provide gene orthology information in multi-species files, "
	                      + "see for instance <a href='" + urlCallDoc.getRequestURL() + "#oma_hog"
	                      + "'>OMA Hierarchical orthologous groups documentation</a>.</li>"
	                      + "<li>Removal of data incorrectly considered as normal in <i>C. elegans</i>, "
	                      + "see <a href='" + urlDownloadCalls.getRequestURL() + "#id6239"
	                      + "'>worm data</a>.</li>"
	                      + "<li>Improved filtering of propagated no-expression calls. As a result, "
	                      + "complete expression calls files do not contain invalid conditions anymore.</li>"
	                      + "<li>Filtering of invalid developmental stages for differential expression analyses.</li>"
	                      + "</ul>");
	    this.writeOneNews("2015-04-16", "release of the multi-species " +
	                      "differential expression data (across anatomy) for 6 groups, see <a href='" +
	                      urlDownload.getRequestURL() + "' " + "title='Bgee download page'>" +
	                      "download page</a>.");
	    this.writeOneNews("2015-03-03", "release of the single-species " +
	                      "differential expression data for 11 species, see <a href='" +
	                      urlDownload.getRequestURL() + "' " + "title='Bgee download page'>" +
	                      "download page</a>.");
	    this.writeOneNews("2014-12-19", "release of the single-species " +
	                      "expression data for 17 species, see <a href='" +
	                      urlDownload.getRequestURL() + "' " + "title='Bgee download page'>" +
	                      "download page</a>.");
	    
	    this.writeln("</div>"); // close panel-body
	    this.writeln("</div>"); // close panel
	
	    log.exit();
	}

	/**
     * TODO
     */
    private void displayMoreInfo() {
    	log.entry();
    	
        this.writeln("<div id='bgee_more_info' class='row'>");
	    
	    this.writeln("<p>The complete website remains available for the previous release of Bgee:</p>");
	    
	    this.writeln("<div class='feature_list'>");
	    this.writeln(HtmlParentDisplay.getSingleFeatureLogo("http://bgee.org/bgee/bgee",
	    		true, "Bgee 12 home page", "Bgee 12",
	    		this.prop.getLogoImagesRootDirectory() + "bgee12_logo.png", null));
	    this.writeln("</div>"); // close feature_list
	    
        this.writeln("</div>"); // close bgee_more_info row
        
        log.exit();
	}

	/**TODO
     * @param date
     * @param description
     */
    private void writeOneNews(String date, String description) {
        log.entry(date, description);
        
        this.writeln("<div class='row'>");
        this.writeln("<div class='col-sm-offset-1 col-sm-2 col-lg-1 news-date'>");
        this.writeln(date);
        this.writeln("</div>");
        this.writeln("<div class='col-sm-9 col-lg-10 news-desc'>");
        this.writeln(description);
        this.writeln("</div>");
        this.writeln("</div>");
        
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
    protected void includeJs() {
        log.entry();
        super.includeJs();
        //If you ever add new files, you need to edit bgee-webapp/pom.xml 
        //to correctly merge/minify them.
        this.includeJs("general.js");
        this.includeJs("autoCompleteGene.js");
        log.exit();
    }

    @Override
    protected void includeCss() {
        log.entry();
        super.includeCss();
        //If you ever add new files, you need to edit bgee-webapp/pom.xml 
        //to correctly merge/minify them.
        this.includeCss("general.css");
        log.exit();
    }
}
