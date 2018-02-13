package org.bgee.view.html;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
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
 * @author  Mathieu Seppey
 * @author  Frederic Bastian
 * @author  Valentine Rech de Laval
 * @author  Philippe Moret
 * @version Bgee 14, Feb. 2018
 * @since   Bgee 13
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

        this.displayHeroUnit();

        this.displaySpeciesBanner(groups);

        this.displayExplanations();

        this.writeln("<hr class='home-divider'/>");
        
        this.displayHomePageSpecies(groups);
        
        this.displayNews();


        this.writeln("<hr class='home-divider'/>");

        this.displayBgeeButtons("end_buttons");

        this.displayMoreInfo();

        this.endDisplay();
        
        log.exit();
    }
    
    /**
     * Display the banner with species (without any interaction).
     * 
     * @param groups	A {@code List} of {@code SpeciesDataGroup} for which display the image.
	 */
	private void displaySpeciesBanner(List<SpeciesDataGroup> groups) {
	    log.entry(groups);
	
	    StringBuilder homePageSpeciesSection = new StringBuilder();
	    
	    groups.stream().filter(sdg -> sdg.isSingleSpecies()).forEach(sdg -> {
	        Species species = sdg.getMembers().get(0);
	        Map<String,String> attrs = new HashMap<>();
	        attrs.put("src", this.prop.getBgeeRootDirectory() + this.prop.getSpeciesImagesRootDirectory() 
	                            + String.valueOf(species.getId()) + "_light.jpg");
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
     * Display the Bgee hero unit.
	 */
	private void displayHeroUnit() {
		log.entry();
		
		String archiveClass = this.prop.isArchive()? "archive": "";

		this.writeln("<div id='bgee_hero' class='row " + archiveClass + "'>");

	    String version = this.getWebAppVersion();
	    if (version != null) {
	        this.writeln("<span id='bgee_version'>version " + htmlEntities(version) + "</span>");
	    }

	    this.writeln("<div id='bgee_hp_logo'><img src='" + this.prop.getBgeeRootDirectory() + this.prop.getLogoImagesRootDirectory() 
	            + "bgee13_hp_logo.png' alt='Bgee logo'></div>");
	
	    this.writeln("<div class='mini_text'>Gene expression data in animals</div>");

	    this.displayBgeeButtons("start_buttons");
	
	    this.writeln("</div>"); // close bgee_hero row
	
	    log.exit();
	}

	/**
     * Display the buttons that allow to access quickly to different pages of the Bgee web site.
     * 
     * @param divId	A {@code String} that is the ID of HTML 'div' element.
	 */
	private void displayBgeeButtons(String divId) {
		log.entry(divId);
		
		RequestParameters urlTopAnat = this.getNewRequestParameters();
	    urlTopAnat.setPage(RequestParameters.PAGE_TOP_ANAT);
	    RequestParameters urlGeneSearch = this.getNewRequestParameters();
	    urlGeneSearch.setPage(RequestParameters.PAGE_GENE);
	    RequestParameters urlDownload = this.getNewRequestParameters();
	    urlDownload.setPage(RequestParameters.PAGE_DOWNLOAD);
	    
	    this.writeln("<div id='" + divId + "'>");
	    this.writeln("<a href='"+ urlTopAnat.getRequestURL() + 
	    		"'><span class='glyphicon glyphicon-stats'></span>Expression enrichment analysis</a>");
	    this.writeln("<a href='"+ urlGeneSearch.getRequestURL() + 
	    		"'><span class='glyphicon glyphicon-search'></span>Gene search</a>");
	    this.writeln("<a href='"+ urlDownload.getRequestURL() + 
	    		"'><span class='glyphicon glyphicon-download'></span>Download</a>");
	    this.writeln("</div>"); // close start_buttons
	    
	    log.exit();
	}

	/**
     * Display explanations on Bgee web site.
     */
    private void displayExplanations() {
    	log.entry();
    	
        this.writeln("<div id='bgee_explanations' class='row home_page_section'>");
		
        this.writeln("<div class='col-sm-4'>");
        this.writeln("<h2>Gene expression data</h2>");
        this.writeln("<p>Bgee is a database to retrieve and compare gene expression patterns "
                + "in multiple animal species, produced from multiple data types "
                + "(RNA-Seq, Affymetrix, <em>in situ</em> hybridization, and EST data).</p>");
        this.writeln("</div>");
        
        this.writeln("<div class='col-sm-4'>");
        this.writeln("<h2>Simply normal</h2>");
        this.writeln("<p>Bgee is based exclusively on curated \"normal\", healthy, expression data "
                + "(e.g., no gene knock-out, no treatment, no disease), "
                + "to provide a comparable reference of normal gene expression.</p>");
        this.writeln("</div>");

        this.writeln("<div class='col-sm-4'>");
        this.writeln("<h2>Comparable between species</h2>");
        this.writeln("<p>Bgee produces calls of presence/absence of expression, "
                + "and of differential over-/under-expression, "
                + "integrated along with information of gene orthology, and of homology "
                + "between organs. This allows comparisons of expression patterns "
                + "between species.</p>");
        this.writeln("</div>");

        this.writeln("</div>"); // close bgee_explanations

        log.exit();
	}
    
    /**
     * Display species home page section with links to download pages.
     * 
     * @param groups	A {@code List} of {@code SpeciesDataGroup} for which display the image.
     */
    private void displayHomePageSpecies(List<SpeciesDataGroup> groups) {
    	log.entry(groups);
    	
	    // Single species part
    	String homePageSpeciesSection;
    	try {
    		homePageSpeciesSection = ((HtmlDownloadDisplay) this.getFactory().getDownloadDisplay())
    				.getSingleSpeciesSection(DownloadPageType.HOME_PAGE, groups, true);
    	} catch (IOException|ClassCastException e) {
        	return;
    	}

    	// Black banner when a species or a group is selected.
    	homePageSpeciesSection += this.getDownloadPageLinkBanner();

    	this.writeln(homePageSpeciesSection);
    	
    	log.exit();
    }

    /**
     * Get the banner of download page links as a HTML 'div' element.
     *
     * @return  the {@code String} that is the black banner of download page links, 
     *          as a HTML 'div' element.
     */
    private String getDownloadPageLinkBanner() {
    	log.entry();

    	StringBuilder banner = new StringBuilder();
    	// This section is empty, it will be filled by JavaScript.
    	banner.append("<div id='bgee_data_selection' class='row'>");
    	// Cross to close the banner
        banner.append("<div id='bgee_data_selection_cross'>");
    	banner.append("<img class='closing_cross' src='" + this.prop.getBgeeRootDirectory() + this.prop.getImagesRootDirectory() + "cross.png' " +
    			"title='Close banner' alt='Cross' />");
        banner.append("</div>");

    	// Section on the right of the black banner
    	banner.append("<h1 class='col-xs-12 col-md-4'>"
    			+ "<span class='scientificname'></span>"
    			+ "<span class='commonname'></span></h1>");

    	RequestParameters urlProcExprValues = this.getNewRequestParameters();
    	urlProcExprValues.setPage(RequestParameters.PAGE_DOWNLOAD);
    	urlProcExprValues.setAction(RequestParameters.ACTION_DOWLOAD_PROC_VALUE_FILES);
    	RequestParameters urlGeneExprCalls = this.getNewRequestParameters();
    	urlGeneExprCalls.setPage(RequestParameters.PAGE_DOWNLOAD);
    	urlGeneExprCalls.setAction(RequestParameters.ACTION_DOWLOAD_CALL_FILES);
    	banner.append("<ul class='col-xs-12 col-md-8 row'>");
    	banner.append("<li class='col-xs-12 col-sm-6'><img class='bullet_point' src='" + this.prop.getBgeeRootDirectory() + this.prop.getImagesRootDirectory() + "arrow.png' alt='Arrow' />" +
    			"<a id='processed_expression_values_link' class='data_page_link' href='" +
    			urlProcExprValues.getRequestURL() + "' title='Bgee processed expression values'>" +
    			"See RNA-Seq and Affymetrix data</a></li>");
    	banner.append("<li class='col-xs-12 col-sm-6'><img class='bullet_point' src='" + this.prop.getBgeeRootDirectory() + this.prop.getImagesRootDirectory() + "arrow.png' alt='Arrow' />" +
    			"<a id='gene_expression_calls_link' class='data_page_link' href='" +
    			urlGeneExprCalls.getRequestURL() +
    			"' title='Bgee gene expression calls'>See gene expression calls</a></li>");
    	banner.append("</ul>");
    	banner.append("</div>"); // close 

    	return log.exit(banner.toString());
    }
    
	/**
	 * Display Bgee news.
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
        
        RequestParameters urlGenePage = this.getNewRequestParameters();
        urlGenePage.setPage(RequestParameters.PAGE_GENE);

        RequestParameters urlSourcePage = this.getNewRequestParameters();
        urlSourcePage.setPage(RequestParameters.PAGE_SOURCE);
	    
	    this.writeln("<div id='bgee_news' class='panel panel-default'>");
	    this.writeln("<div class='panel-heading'>");
	    this.writeln("<span class='panel-title'>News"
	                 + "    <span class='header_details'>(features are being added incrementally)"
	                 + "</span></span>");
	    this.writeln("</div>"); // close panel-heading
	    
	    this.writeln("<div class='panel-body'>");
	    
	    this.writeOneNews("2017-05-16", "Release of Bgee version 14-beta:"
	            + "<ul>"
	            + "  <li>12 new species, bringing the total to 29:"
	            + "    <ul>"
	            + "      <li>new mammal species: horse, rabbit, dog, cat, guinea pig, hedgehog;</li>"
	            + "      <li>new Drosophila species: D. ananassae, D. mojavensis, D. pseudoobscura, D. simulans, D. virilis, D. yakuba.</li>"
	            + "    </ul>"
	            + "  </li>"
	            + "  <li>All species now have RNA-Seq data.</li>"
	            + "  <li>Addition of curated human RNA-Seq data from GTEx, removing unhealthy samples; see "
                + "      <a href='" + urlDownloadCalls.getRequestURL() + "#id1'>human data</a>.</li>"
	            + "  <li>Improved quality annotation of calls: replacement of \"low quality\" / \"high quality\" by:"
	            + "    <ul>"
	            + "      <li>\"Gold\": ≥2 experiments with a high confidence calls;</li>"
	            + "      <li>\"Silver\": 1 experiment with a high confidence call, or ≥2 experiments with low confidence calls;</li>"
	            + "      <li>\"Bronze\": 1 experiment with a low confidence call; these are not shown by default.</li>"
	            + "    </ul>"
	            + "  </li>"
	            + "  <li>Update of download pages to make it easier to chose files to retrieve; inclusion of gene ranks (as used in gene pages) in call files..</li>"
	            + "</ul>"
	            + "You can still access to Bgee 13 at <a title='Archive site Bgee version 13' "
	            + "href='http://bgee.org/bgee13' target='_blank'>http://bgee.org/bgee13</a>.");

	    this.writeOneNews("2016-07-06", "Release of Bgee version 13.2: "
                + "<ul>"
                + "<li>Major update of our gene page and ranking algorithm: "
                  + "<ul>"
                  + "<li>We are happy to announce that we have updated our ranking algorithm allowing "
                  + "to discover the most relevant anatomical entities and life stages where "
                  + "a gene is expressed. We hope that you will appreciate the noticeable improvements.</li>"
                  + "<li>The gene page has been updated to display the rank scores of conditions "
                  + "where a gene is expressed, allowing to easily identify major functional shifts "
                  + "in gene expression.</li>"
                  + "</ul>"
                + "Give a try to this updated ranking by searching for your favorite gene, "
                + "or by using the example links, on the <a href='" + urlGenePage.getRequestURL() 
                + "'>gene search page</a>.</li>"
                + "<li>We now display more information about the sources of data used in Bgee, "
                + "see the new <a href='" + urlSourcePage.getRequestURL() + "'>data source page</a>, "
                + "and new information added to the gene pages.</li>"
                + "</ul>");
        
	    this.writeOneNews("2016-05-09", "Release of our new "
                + "<a href='https://github.com/BgeeDB/BgeeDB_R' class='external_link' target='_blank'>"
                + "BgeeDB R package</a>, a package for the annotation and gene expression "
                + "data download from Bgee database into R, and TopAnat analysis (see also "
                + "<a href='https://bioconductor.org/packages/release/bioc/html/BgeeDB.html' "
                + "class='external_link' target='_blank'>Bioconductor website</a>).");

        this.writeOneNews("2016-03-22", "Various improvements of our new interface.");
        
        this.writeOneNews("2016-03-09", "Release of our new <a href='" + urlGenePage.getRequestURL()
                          + "'>gene page</a>, allowing to discover the most relevant conditions where a gene is expressed. "
                          + "This update also includes an important revamping of our interfaces.");
	    
	    this.writeOneNews("2015-12-24", "Major update of <a href='" + urlTopAnat.getRequestURL()
	                      + "' title='Perform gene expression enrichment tests with TopAnat'>TopAnat</a>. "
	                      + "Happy Christmas!");
	    
	    this.writeOneNews("2015-11-24", "We are happy to release of our new exclusive tool "
	                      + "for gene expression enrichment analyses: <a href='" + urlTopAnat.getRequestURL()
	                      + "' title='Perform gene expression enrichment tests with TopAnat'>TopAnat</a>. "
	                      + "This is a tool with absolutely no equivalent, developped in collaboration with "
	                      + "the Web-Team  of the SIB Swiss Institute of Bioinformatics. Check it out!");
	    this.writeOneNews("2015-08-26", "Update of the home page.");
	    
	    this.writeOneNews("2015-06-08", "Release of Bgee version 13.1: "
	                      + "<ul>"
	                      + "<li>Update of the website interfaces.</li>"
	                      + "<li><a href='" + urlDownloadProcValues.getRequestURL()
	                      + "'>New download page</a> providing processed expression values.</li>"
	                      + "<li>Addition of mouse <i>in situ</i> data from MGI, see "
	                      + "<a href='" + urlDownloadCalls.getRequestURL() + "#id2"
	                      + "'>mouse data</a>.</li>"
	                      + "<li>Differential expression data have been added for "
	                      + "<a href='" + urlDownloadCalls.getRequestURL() + "#id3"
	                      + "'>zebrafish</a>, <a href='" + urlDownloadCalls.getRequestURL() + "#id6"
	                      + "'>chimpanzee</a>, <a href='" + urlDownloadCalls.getRequestURL() + "#id8"
	                      + "'>gorilla</a>, and <a href='" + urlDownloadCalls.getRequestURL() + "#id19"
	                      + "'>opossum</a>.</li>"
	                      + "<li>Addition of new multi-species differential expression data, see "
	                      + "for instance <a href='" + urlDownloadCalls.getRequestURL() + "#id9598_9544"
	                      + "'>chimpanzee/macaque comparison</a>.</li>"
	                      + "<li>New format to provide gene orthology information in multi-species files, "
	                      + "see for instance <a href='" + urlCallDoc.getRequestURL() + "#oma_hog"
	                      + "'>OMA Hierarchical orthologous groups documentation</a>.</li>"
	                      + "<li>Removal of data incorrectly considered as normal in <i>C. elegans</i>, "
	                      + "see <a href='" + urlDownloadCalls.getRequestURL() + "#id5"
	                      + "'>worm data</a>.</li>"
	                      + "<li>Improved filtering of propagated no-expression calls. As a result, "
	                      + "complete expression calls files do not contain invalid conditions anymore.</li>"
	                      + "<li>Filtering of invalid developmental stages for differential expression analyses.</li>"
	                      + "</ul>");
	    this.writeOneNews("2015-04-16", "Release of the multi-species " +
	                      "differential expression data (across anatomy) for 6 groups, see <a href='" +
	                      urlDownload.getRequestURL() + "' " + "title='Bgee download page'>" +
	                      "download page</a>.");
	    this.writeOneNews("2015-03-03", "Release of the single-species " +
	                      "differential expression data for 11 species, see <a href='" +
	                      urlDownload.getRequestURL() + "' " + "title='Bgee download page'>" +
	                      "download page</a>.");
	    this.writeOneNews("2014-12-19", "Release of the single-species " +
	                      "expression data for 17 species, see <a href='" +
	                      urlDownload.getRequestURL() + "' " + "title='Bgee download page'>" +
	                      "download page</a>.");
	    
	    this.writeln("</div>"); // close panel-body
	    this.writeln("</div>"); // close panel
	
	    log.exit();
	}

	/**
     * Display more information (for instance, image sources or 'view x site' link).
     */
    private void displayMoreInfo() {
    	log.entry();
    	
        this.writeln("<div id='bgee_more_info' class='row'>");
    	
        this.writeln("<div class='col-xs-12 col-md-10'>");
        this.writeln(getImageSources());
        this.writeln("</div>");

        this.writeln("<div class='col-xs-12 col-md-3 archive_site'>");
        this.writeln("View archive sites:");
        this.writeln("<a title='Archive site Bgee version 12' href='http://bgee.org/bgee12' target='_blank'>"
                + "version 12</a>");
        this.writeln("<a title='Archive site Bgee version 13' href='http://bgee.org/bgee13' target='_blank'>"
                + "version 13</a>");
        this.writeln("</div>");
        
        this.writeln("</div>"); // close bgee_more_info row
        
        log.exit();
	}

	/**
	 * Display an unique news.
	 * 
     * @param date			A {@code String} that is the date of the news. 
     * @param description	A {@code String} that is the description of the news.
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
    protected void includeJs() {
        log.entry();
        super.includeJs();
        //If you ever add new files, you need to edit bgee-webapp/pom.xml 
        //to correctly merge/minify them.
        this.includeJs("general.js");
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
