package org.bgee.view.html;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.BgeeProperties;
import org.bgee.controller.RequestParameters;
import org.bgee.model.ServiceFactory;
import org.bgee.model.dao.api.DAOManager;
import org.bgee.model.file.SpeciesDataGroup;
import org.bgee.model.species.Species;
import org.bgee.utils.JSHelper;
import org.bgee.view.DownloadDisplay;

/**
 * This class displays the page having the category "download", i.e. with the parameter
 * page=download for the HTML view.
 * 
 * @author  Mathieu Seppey
 * @author  Valentine Rech de Laval
 * @version Bgee 13, June 2015
 * @since   Bgee 13
 */
public class HtmlDownloadDisplay extends HtmlParentDisplay implements DownloadDisplay {
 
    private final static Logger log = LogManager.getLogger(HtmlDownloadDisplay.class.getName());

    /**
     * A {@code String} that is the group name of the pairwise Human/Mouse.
     */
    private final static String GROUP_NAME_HUMAN_MOUSE= "Human/Mouse";
    /**
     * A {@code String} that is the group name of the pairwise Human/Zebrafish.
     */
    private final static String GROUP_NAME_HUMAN_ZEBRAFISH = "Human/Zebrafish";
    /**
     * A {@code String} that is the group name of the pairwise Human/Fruit fly.
     */
    private final static String GROUP_NAME_HUMAN_FRUITFLY = "Human/Fruit fly";
    /**
     * A {@code String} that is the group name of the pairwise Human/Nematode.
     */
    private final static String GROUP_NAME_HUMAN_NEMATODE = "Human/Nematode";
    /**
     * A {@code String} that is the group name of the pairwise Mouse/Zebrafish.
     */
    private final static String GROUP_NAME_MOUSE_ZEBRAFISH= "Mouse/Zebrafish";
    /**
     * A {@code String} that is the group name of the pairwise Mouse/Fruit fly.
     */
    private final static String GROUP_NAME_MOUSE_FRUITFLY = "Mouse/Fruit fly";
    /**
     * A {@code String} that is the group name of the pairwise Mouse/Nematode.
     */
    private final static String GROUP_NAME_MOUSE_NEMATODE = "Mouse/Nematode";
    /**
     * A {@code String} that is the group name of the pairwise Zebrafish/Fruit fly.
     */
    private final static String GROUP_NAME_ZEBRAFISH_FRUITFLY = "Zebrafish/Fruit fly";
    /**
     * A {@code String} that is the group name of the pairwise Zebrafish/Nematode.
     */
    private final static String GROUP_NAME_ZEBRAFISH_NEMATODE = "Zebrafish/Nematode";
    /**
     * A {@code String} that is the group name of the pairwise Fruit fly/Nematode.
     */
    private final static String GROUP_NAME_FRUITFLY_NEMATODE = "Fruit fly/Nematode";
    /**
     * A {@code String} that is the group name of the pairwise Fruit fly/Nematode.
     */
    private final static String GROUP_NAME_MACAQUE_CHIMP = "Macaque/Chimpanzee";
    /**
     * A {@code String} that is the group name of Catarrhini.
     */
    private final static String GROUP_NAME_CATARRHINI = "Catarrhini";
    /**
     * A {@code String} that is the group name of Rodentia.
     */
    private final static String GROUP_NAME_MURINAE = "Murinae";
    /**
     * A {@code String} that is the group name of Theria.
     */
   private final static String GROUP_NAME_THERIA = "Theria";
   /**
    * A {@code String} that is the group name of Mammalia.
    */
    private final static String GROUP_NAME_MAMMALIA = "Mammalia";
    /**
     * A {@code String} that is the group name of Amniota.
     */
    private final static String GROUP_NAME_AMNIOTA = "Amniota";
    /**
     * A {@code String} that is the group name of Tetrapoda.
     */
    private final static String GROUP_NAME_TETRAPODA = "Tetrapoda";
    /**
     * A {@code String} that is the group name of Bilateria.
     */
    private final static String GROUP_NAME_BILATERIA = "Bilateria";
        
    public enum DownloadPageType {
        PROC_EXPR_VALUES, EXPR_CALLS;
    }
    /**
     * Constructor
     * 
     * @param response          A {@code HttpServletResponse} that will be used to display the 
     *                          page to the client
     * @param requestParameters The {@code RequestParameters} handling the parameters of the 
     *                          current request, to determine the requested displayType, 
     *                          and for display purposes.
     * @param prop              A {@code BgeeProperties} instance that contains the properties
     *                          to use.
     * @param factory           The {@code HtmlFactory} that instantiated this object.
     * @throws IOException      If there is an issue when trying to get or to use the
     *                          {@code PrintWriter} 
     */
    public HtmlDownloadDisplay(HttpServletResponse response, RequestParameters requestParameters, 
            BgeeProperties prop, HtmlFactory factory) throws IOException {
        super(response, requestParameters, prop, factory);
    }

    @Override
    public void displayDownloadHomePage() {
        log.entry();
        
        this.startDisplay("Bgee download pages");

        this.writeln("<h1>Bgee download page</h1>");

        this.writeln("<div class='feature_list'>");
        this.writeln(this.getFeatureDownloadLogos());
        this.writeln("</div>");
        
        this.endDisplay();

        log.exit();
    }
    
    @Override
    public void displayGeneExpressionCallDownloadPage() {
        log.entry();
        
        this.startDisplay("Bgee gene expression call download page");

        this.writeln(this.getMoreResultDivs());

        this.writeln("<div id='expr_calls'>");

        this.writeln("<div id='bgee_title'>");
        this.writeln("<h1>");
        this.writeln("<img src='" + this.prop.getLogoImagesRootDirectory() + "expr_calls_logo.png' " + 
                "alt='" + GENE_EXPR_CALLS_PAGE_NAME + " logo'/>" + GENE_EXPR_CALLS_PAGE_NAME);
        this.writeln("</h1>");
        this.writeln("</div>");
        
        // Introduction
        this.writeln("<div id='bgee_introduction' class='bgee_section bgee_download_section'>");
        this.writeln(this.getIntroduction(DownloadPageType.EXPR_CALLS));
        this.writeln("</div>");

        // Search Box
        this.writeln(this.getSearchBox());
        
        // Single species part
        this.writeln(this.getSingleSpeciesSection(DownloadPageType.EXPR_CALLS));

        // Black banner when a species or a group is selected.
        this.writeln(this.getDownloadBanner(DownloadPageType.EXPR_CALLS));
        
        // Multi-species part
        this.writeln(this.getMultiSpeciesSection(DownloadPageType.EXPR_CALLS));

        this.writeln("</div>");

        // Image sources
        this.writeln(this.getImageSources());
        
        this.endDisplay();
        
        log.exit();
    }

    @Override
    public void displayProcessedExpressionValuesDownloadPage() {
        log.entry();
        
        this.startDisplay("Bgee " + PROCESSED_EXPR_VALUES_PAGE_NAME.toLowerCase() + " download page");
        
        this.writeln(this.getMoreResultDivs());

        this.writeln("<div id='proc_values'>");
    
        this.writeln("<div id='bgee_title'>");
        this.writeln("<h1>");
        this.writeln("<img src='" + this.prop.getLogoImagesRootDirectory() + "proc_values_logo.png'" + 
                "' alt='" + PROCESSED_EXPR_VALUES_PAGE_NAME + " logo'/>" + 
                PROCESSED_EXPR_VALUES_PAGE_NAME);
        this.writeln("</h1>");
        this.writeln("</div>");

        // Introduction
        this.writeln("<div id='bgee_introduction' class='bgee_section bgee_download_section'>");
        this.writeln(this.getIntroduction(DownloadPageType.PROC_EXPR_VALUES));
        this.writeln("</div>");
    
        // Search Box
        this.writeln(this.getSearchBox());
        
        // Single species part
        this.writeln(this.getSingleSpeciesSection(DownloadPageType.PROC_EXPR_VALUES));

        // Black banner when a species or a group is selected.
        this.writeln(this.getDownloadBanner(DownloadPageType.PROC_EXPR_VALUES));
        
        this.writeln("</div>"); // close proc_values div

        // Image sources
        this.writeln(this.getImageSources());
        
        this.endDisplay();
        
        log.exit();
    }
    
    /**
     * Return the introduction text for pages providing download files in HTML. 
     * This HTML contains only 'p' elements, and text.
     * 
     * @param pageType  A {@code DownloadPageType} defining for which type of download page 
     *                  the introduction text should be generated.
     * @return          A {@code String} that is an introduction text for {@code pageType}, 
     *                  in HTML.
     */
    private String getIntroduction(DownloadPageType pageType) {
        log.entry(pageType);
        
        String intro = "<p>Bgee is a database to retrieve and compare gene expression patterns "
                + "in multiple animal species, based exclusively on curated \"normal\" "
                + "expression data (e.g., no gene knock-out, no treatment, no disease), "
                + "from multiple data types, "
                + "to provide a comparable reference of normal gene expression.</p>";
        if (pageType == DownloadPageType.EXPR_CALLS) {
            intro += "<p>This page provides calls of baseline "
                + "presence/absence of expression, and of differential over-/under-expression, "
                + "either in single species, or made comparable between multiple species. "
                + "Click on a species or a group of species to browse files available for download. ";
        } else if (pageType == DownloadPageType.PROC_EXPR_VALUES) {
            intro += "<p>This page provides annotations and experiment information "
                    + "(e.g., annotations to anatomy and development, quality scores used in QCs, "
                    + "chip or library information), and processed expression values "
                    + "(e.g., read counts, RPKM values, log values of Affymetrix "
                    + "probeset normalized signal intensities). Click on a species "
                    + "to browse files available for download. ";
        } else {
            assert false: "Unknown DownloadPageType";
        }
        intro += "See also ";
        if (pageType == DownloadPageType.EXPR_CALLS) {
            RequestParameters urlGenerator = this.getNewRequestParameters();
            urlGenerator.setPage(RequestParameters.PAGE_DOWNLOAD);
            urlGenerator.setAction(RequestParameters.ACTION_DOWLOAD_PROC_VALUE_FILES);
            intro += "<a href='" + urlGenerator.getRequestURL() 
                    + "' title='See Bgee processed expression values'>processed expression values</a>";
        } else {
            RequestParameters urlGenerator = this.getNewRequestParameters();
            urlGenerator.setPage(RequestParameters.PAGE_DOWNLOAD);
            urlGenerator.setAction(RequestParameters.ACTION_DOWLOAD_CALL_FILES);
            intro += "<a href='" + urlGenerator.getRequestURL() 
                    + "' title='See Bgee gene expression calls'>gene expression calls</a>";
        }

        intro += ", and <a href='" + this.prop.getFTPRootDirectory() 
                + "statistics.tsv' title='Database statistics TSV file'>"
                + "database statistics</a>.</p>";
        return log.exit(intro);
    }
    
    /**
     * Get the 'More results' of a download page as a HTML 'div' element. 
     *
     * @return  the {@code String} that is the 'More results' HTML 'div' element.
     */
    private String getMoreResultDivs() {
        log.entry();
        
        return log.exit(
                "<div id='bgee_more_results_up'>More result(s)</div>" +
                "<div id='bgee_more_results_down'>More result(s)</div>");
    }

    /**
     * Get the search box of a download page as a HTML 'div' element. 
     *
     * @return  the {@code String} that is the search box as HTML 'div' element.
     */
    private String getSearchBox() {
        log.entry();
    
        return log.exit(
                "<div id='bgee_search_box'>" +
                        "<form action='/' method='get'>" +
                            "<label for='search_label'>Search species</label>" +
                            "<input id='search_label' class='sib_text' type='text' name='search' " +
                                "value='Scientific name, common name...'/>" +
                            "<input type='image' alt='Submit' " +
                                "src='"+this.prop.getImagesRootDirectory()+"submit_button.png'/>" +
                            "<div id='results_nb'></div>" +
                        "</form>" +
                "</div>");
    }

    /**
     * Get the single species section of a download page as a HTML 'div' element,
     * according the provided page type.
     *
     * @param pageType  A {@code DownloadPageType} that is the type of the page.
     * @return          the {@code String} that is the single species section as HTML 'div' element,
     *                  according {@code pageType}.
     */
    private String getSingleSpeciesSection(DownloadPageType pageType) {
        log.entry(pageType);

        StringBuffer s = new StringBuffer(); 
        s.append("<div id='bgee_uniq_species'> ");
        s.append("<h2>Single-species</h2>");
        s.append("<div class='bgee_section bgee_download_section'>");
        s.append(getSingleSpeciesSection(pageType, getAllSpeciesDataGroup()));
        s.append("</div>");
        s.append("</div>");
        
        return log.exit(s.toString());
    }
    
    /**
     * Gets the single species section as a {@code String}
     * @param pageType the {@code DownloadPageType} of this page
     * @param groups   the {@code List} of {@code SpeciesDataGroup} to display
     * @return A {@String} containing the html section 
     */
    private String getSingleSpeciesSection(DownloadPageType pageType, List<SpeciesDataGroup> groups) {
    	StringBuffer sb = new StringBuffer();
    	
    	for (SpeciesDataGroup sdg: groups) {
    		if (sdg.isSingleSpecies()) {
    			Species species = sdg.getMembers().get(0);
    			Map<String, String> attr = new HashMap<>();
    			attr.put("id", sdg.getId());
    			sb.append(getHTMLTag("figure", attr, getHTMLTag("div", getImage(species))+getCaption(species) ));
    		}
    	}
    	return sb.toString();
    }
    
    /**
     * Gets the html code for a species image.
     * @param species The {@code Species} for which to get the image
     * @return A {@String} containing the html code for this image
     */
    private String getImage(Species species) {
    	Map<String,String> attrs = new HashMap<>();
    	attrs.put("src", "img/species/"+species.getId()+"_light.jpg");
    	attrs.put("alt", species.getShortName());
    	attrs.put("class", "species_img");
    	//TODO: backward compat => should go
    	/*attrs.put("data-bgeespeciesname", species.getName());
    	attrs.put("data-bgeespeciesid", species.getId());
    	attrs.put("data-bgeespeciesshortname", species.getShortName());
    	attrs.put("data-bgeespeciescommonname", species.getShortName());
    	attrs.put("data-bgeespeciesalternatenames", species.getShortName());*/

    	return getHTMLTag("img", attrs);
    }
    
    /**
     * Gets the {@code figcaption} element for a given {@code Species}
     * @param species A {@Species}
     * @return A {@code String} containing the html code
     */
    private String getCaption(Species species) {
    	return getHTMLTag("figcaption", getShortNameTag(species)
    			+getHTMLTag("p", species.getName()));
    }
    
    /**
     * Gets the {@code figcaption} element for a given {@code SpeciesDataGroup}
     * @param species A {SpeciesDataGroup}
     * @return A {@code String} containing the html code
     */
    private String getCaption(SpeciesDataGroup speciesDataGroup) {
    	return getHTMLTag("figcaption", speciesDataGroup.getName());
    }
    
    /**
     * Gets the short name element for a given {@Species}
     * @param species A {@Species}
     * @return A {@code String} containing the html code
     */
    private String getShortNameTag(Species species) {
    	return getHTMLTag("p", getHTMLTag("i", species.getShortName()));
    }
    
   /**
    * Helper method to get an html tag
    * @param name    A {@code String} representing the name of the element 
    * @param content A {@code String} reprensenting the content of the element
    * @return The HTML code as {@code String}
    */
    private static String getHTMLTag(String name, String content) {
    	StringBuffer sb = new StringBuffer();
    	sb.append("<").append(name).append(">\n")
    	  .append(content)
    	  .append("</").append(name).append(">\n");
    	return sb.toString();
    }
    
    /**
     * Helper method to get an html tag
     * @param name    A {@code String} representing the name of the element 
     * @param content A {@code String} reprensenting the content of the element
     * @return The HTML code as {@code String}
     */
    private static String getHTMLTag(String name, Map<String, String> attributes) {
    	StringBuffer sb = new StringBuffer();
    	sb.append("<").append(name); 
    	for (Map.Entry<String, String> attr: attributes.entrySet()) {
    		sb.append(" ").append(attr.getKey()).append("=\"").append(attr.getValue()).append("\"");
    	}
    	sb.append(" />\n");
    	return sb.toString();
    }
    
    /**
     * Helper method to get an html tag
     * @param name    A {@code String} representing the name of the element 
     * @param content A {@code String} reprensenting the content of the element
     * @return The HTML code as {@code String}
     */
    private static String getHTMLTag(String name, Map<String, String> attributes, String content) {
    	StringBuffer sb = new StringBuffer();
    	sb.append("<").append(name); 
    	for (Map.Entry<String, String> attr: attributes.entrySet()) {
    		sb.append(" ").append(attr.getKey()).append("='").append(attr.getValue()).append("'");
    	}
    	sb.append(">\n").append(content).append("</").append(name).append(">\n");
    	return sb.toString();
    }
    
    /**
     * Get the multi-species figure list for the given multi-species datagroups
     * according the provided page type.
     *
     * @param pageType  A {@code DownloadPageType} that is the type of the page.
     * @return          the {@code String} that is the multi-species section as HTML 'div' element,
     *                  according {@code pageType}.
     */
    private String getMultiSpeciesSection(DownloadPageType pageType,  List<SpeciesDataGroup> groups) {
    	StringBuffer sb = new StringBuffer();
    	
    	for (SpeciesDataGroup sdg: groups) {
    		if (sdg.isMultipleSpecies()) {
    			StringBuffer images = new StringBuffer();
    			for (Species species : sdg.getMembers()) {
    				images.append(getImage(species));
    			}
    			Map<String, String> attr = new HashMap<>();
    			attr.put("id", sdg.getId());
    			attr.put("name", sdg.getName());
    			sb.append(getHTMLTag("figure", attr, getHTMLTag("div", images.toString()+getCaption(sdg))));
    		}
    	}
    	return sb.toString();
    }
    
    /**
     * Get the multi-species section of a download page as a HTML 'div' element, 
     * according the provided page type.
     *
     * @param pageType  A {@code DownloadPageType} that is the type of the page.
     * @return          the {@code String} that is the multi-species section as HTML 'div' element,
     *                  according {@code pageType}.
     */
    private String getMultiSpeciesSection(DownloadPageType pageType) {
        log.entry(pageType);

        StringBuffer s = new StringBuffer(); 
        s.append("<div id='bgee_multi_species'>");
        s.append("<h2>Multi-species</h2>" +
                 "<span class='header_details'>(orthologous genes in homologous anatomical structures)</span>");
        s.append("<div class='bgee_section bgee_download_section'>");
        s.append(getMultiSpeciesSection(pageType, getAllSpeciesDataGroup()));
        s.append("</div>");
        s.append("</div>");

        return log.exit(s.toString());
    }

    /**
     * Get the banner of a download page as a HTML 'div' element, 
     * according the provided page type.
     *
     * @param pageType  A {@code DownloadPageType} that is the type of the page.
     * @return          the {@code String} that is the black banner of a download page 
     *                  as a HTML 'div' element according {@code pageType}.
     */
    private String getDownloadBanner(DownloadPageType pageType) {
        log.entry(pageType);
    
        StringBuffer banner = new StringBuffer();
        // This section is empty, it will be filled by JavaScript.
        banner.append("<div id='bgee_data_selection'>");
        
        // Cross to close the banner
        banner.append("<div id='bgee_data_selection_cross'>");
        banner.append("<a id='switch_page_link' href=''></a>");
        banner.append("<img src='" + this.prop.getImagesRootDirectory() + "cross.png' " +
                "title='Close banner' alt='Cross' />");
        banner.append("</div>");
        
        // Section on the left of the black banner: image for single species or patchwork for group
        banner.append("<div id='bgee_data_selection_img'></div>");
    
        // Section on the right of the black banner
        banner.append("<div id='bgee_data_selection_text'>");
        banner.append("<h1 class='scientificname'></h1><h1 class='commonname'></h1>");
        banner.append("<p class='groupdescription'></p>");
        
        if (pageType.equals(DownloadPageType.EXPR_CALLS)) {
            //Ortholog file (only in multi-species banner, manage by download.js)
            banner.append("<div id='ortholog_file_buttons' class='bgee_download_file_buttons'>");
            banner.append("<h2>Hierarchical orthologous groups</h2>");
            //TODO: uncomment when documentation generated, add url management in JS
//            banner.append("<a id='ortholog_help' href='" + urlDoc.getRequestURL() + "'>"+
//                    this.getHelpImg() + "</a>");
            banner.append("<div id='ortholog_data'>" +
                    "<a id='ortholog_csv' class='download_link' href='' download></a>" +
//                    this.getShowHeaderLink("show_ortholog_headers") + 
                    "</div>");
            banner.append("<p class='file_info'>This file provides groups of genes orthologous "
                    + "between the selected taxa.</p>");
            //TODO: uncomment when documentation generated, add url management in JS
//            banner.append("<div id='ortholog_headers' class='header_table'>" +
//                    HtmlDocumentationCallFile.getOrthologHeaderDesc() + "</div>");
            banner.append("</div>"); 

            // Presence/absence expression files
            banner.append("<div class='bgee_download_file_buttons'>");
            banner.append("<h2>Presence/Absence of expression</h2>");
            banner.append(this.getHelpLink("expr_help"));
            banner.append("<p id='expr_coming_soon' class='no_data'>Coming soon</p>");
            banner.append("<p id='expr_no_data' class='no_data'>Not enough data</p>");
            banner.append("<div id='expr_data'>" +
                    "<a id='expr_simple_csv' class='download_link' href='' download></a>" +
                    this.getShowHeaderLink("show_single_simple_expr_headers") +
                    "<a id='expr_complete_csv' class='download_link' href='' download></a>" +
                    this.getShowHeaderLink("show_single_complete_expr_headers") + "</div>");
            banner.append("<div id='single_simple_expr_headers' class='header_table'>" +
                    HtmlDocumentationCallFile.getSingleSpeciesSimpleExprFileHeaderDesc() + "</div>");
            banner.append("<div id='single_complete_expr_headers' class='header_table'>" + 
                    HtmlDocumentationCallFile.getSingleSpeciesCompleteExprFileHeaderDesc() + "</div>");
            banner.append("</div>");
            
            // Differential expression files across anatomy
            banner.append("<div class='bgee_download_file_buttons'>");
            banner.append("<h2>Over-/Under-expression across anatomy</h2>");
            banner.append(this.getHelpLink("diffexpr_anatomy_help"));
            banner.append("<p id='diffexpr_anatomy_no_data' class='no_data'>Not enough data</p>");
            banner.append("<div id='diffexpr_anatomy_data'>" + 
                    "<a id='diffexpr_anatomy_simple_csv' class='download_link' href='' download></a>" +
                    this.getShowHeaderLink("show_single_simple_diffexpr_anatomy_headers") +
                    this.getShowHeaderLink("show_multi_simple_diffexpr_anatomy_headers") +
                    "<a id='diffexpr_anatomy_complete_csv' class='download_link' href='' download></a>" +
                    this.getShowHeaderLink("show_single_complete_diffexpr_anatomy_headers") +
                    this.getShowHeaderLink("show_multi_complete_diffexpr_anatomy_headers") +
                    "</div>");
            banner.append("<div id='single_simple_diffexpr_anatomy_headers' class='header_table'>" + 
                    HtmlDocumentationCallFile.getSingleSpeciesSimpleDiffExprFileHeaderDesc() + "</div>");
            banner.append("<div id='single_complete_diffexpr_anatomy_headers' class='header_table'>" + 
                    HtmlDocumentationCallFile.getSingleSpeciesCompleteDiffExprFileHeaderDesc() + "</div>");
            banner.append("<div id='multi_simple_diffexpr_anatomy_headers' class='header_table'>" + 
                    HtmlDocumentationCallFile.getMultiSpeciesSimpleDiffExprFileHeaderDesc() + "</div>");
            banner.append("<div id='multi_complete_diffexpr_anatomy_headers' class='header_table'>" + 
                    HtmlDocumentationCallFile.getMultiSpeciesCompleteDiffExprFileHeaderDesc() + "</div>");
            banner.append("</div>");
            
            // Differential expression files across life stages
            banner.append("<div class='bgee_download_file_buttons'>");
            banner.append("<h2>Over-/Under-expression across life stages</h2>");
            banner.append(this.getHelpLink("diffexpr_development_help"));
            banner.append("<p id='diffexpr_development_coming_soon' class='no_data'>Coming soon</p>");
            banner.append("<p id='diffexpr_development_no_data' class='no_data'>Not enough data</p>");
            banner.append("<div id='diffexpr_development_data'>" + 
                    "<a id='diffexpr_development_simple_csv' class='download_link' href='' download></a>" +
                    this.getShowHeaderLink("show_single_simple_diffexpr_development_headers") +
                    "<a id='diffexpr_development_complete_csv' class='download_link' href='' download></a>" +
                    this.getShowHeaderLink("show_single_complete_diffexpr_development_headers") +
                    "</div>");
            banner.append("<div id='single_simple_diffexpr_development_headers' class='header_table'>" + 
                    HtmlDocumentationCallFile.getSingleSpeciesSimpleDiffExprFileHeaderDesc() + "</div>");
            banner.append("<div id='single_complete_diffexpr_development_headers' class='header_table'>" + 
                    HtmlDocumentationCallFile.getSingleSpeciesCompleteDiffExprFileHeaderDesc() + "</div>");
            banner.append("</div>");            
        } else if (pageType.equals(DownloadPageType.PROC_EXPR_VALUES)) {
            // RNA-Seq data
            banner.append("<div class='bgee_download_file_buttons'>");
            
            banner.append("<h2>RNA-Seq data</h2>");
//            banner.append(this.getHelpLink("rnaseq_help"));
//            banner.append("<p class='no_data'>Coming soon</p>");
            banner.append("<p id='rnaseq_no_data' class='no_data'>No data</p>");
            
            //data section
            banner.append("<div id='rnaseq_data'>");
            banner.append("<a id='rnaseq_annot_csv' class='download_link' href='' download></a>" +
                    //this.getShowHeaderLink("show_rnaseq_annot_headers") +
                    "<a id='rnaseq_data_csv' class='download_link' href='' download></a>");
                    //this.getShowHeaderLink("show_rnaseq_data_headers") +
            banner.append("<p class='file_info'>Files can also be retrieved per experiment, "
                    //href will be filed by the javascript.
                    + "see <a id='rna_seq_data_root_link' title='Retrieve RNA-Seq data "
                    + "per experiment for this species'>RNA-Seq data directory</a>.</p>");
            banner.append("</div>"); //end data section
            
            banner.append("</div>"); // end RNA-Seq data
            
            // Affymetrix data
            banner.append("<div class='bgee_download_file_buttons'>");
            banner.append("<h2>Affymetrix data</h2>");
//            banner.append(this.getHelpLink("affy_help"));
//            banner.append("<p class='no_data'>Coming soon</p>");
            banner.append("<p id='affy_no_data' class='no_data'>No data</p>");
            
            //data section
            banner.append("<div id='affy_data'>"); 
            banner.append("<a id='affy_annot_csv' class='download_link' href='' download></a>" +
                    //this.getShowHeaderLink("show_affy_annot_headers") +
                    "<a id='affy_data_csv' class='download_link' href='' download></a>");
                    //this.getShowHeaderLink("show_affy_data_headers") +
            banner.append("<p class='file_info'>Files can also be retrieved per experiment, "
                    //href will be filed by the javascript.
                    + "see <a id='affy_data_root_link' title='Retrieve Affymetrix data "
                    + "per experiment for this species'>Affymetrix data directory</a>.</p>");
            banner.append("</div>"); // end of data section
            
            banner.append("</div>"); // end of Affy data
            
            // In situ data
            banner.append("<div class='bgee_download_file_buttons'>");
            banner.append("<h2><em>In situ</em> data</h2>");
//            banner.append(this.getHelpLink("in_situ_help"));
            banner.append("<p id='insitudata_coming_soon' class='no_data'>Coming soon</p>");
            banner.append("<p id='in_situ_no_data' class='no_data'>No data</p>");
            banner.append("<div id='in_situ_data'>" + 
                    "<a id='in_situ_annot_csv' class='download_link' href='' download></a>" +
                    this.getShowHeaderLink("show_in_situ_annot_headers") +
                    "<a id='in_situ_data_csv' class='download_link' href='' download></a>" +
                    this.getShowHeaderLink("show_in_situ_data_headers") +
                    "</div>");
            banner.append("</div>");            
            // EST data
            banner.append("<div class='bgee_download_file_buttons'>");
            banner.append("<h2>EST data</h2>");
//            banner.append(this.getHelpLink("est_help"));
            banner.append("<p id='estdata_coming_soon' class='no_data'>Coming soon</p>");
            banner.append("<p id='est_no_data' class='no_data'>No data</p>");
            banner.append("<div id='est_data'>" + 
                    "<a id='est_annot_csv' class='download_link' href='' download></a>" +
                    this.getShowHeaderLink("show_est_annot_headers") +
                    "<a id='est_data_csv' class='download_link' href='' download></a>" +
                    this.getShowHeaderLink("show_est_data_headers") +
                    "</div>");
            banner.append("</div>");            
        } else {
            assert false: "Unknown DownloadPageType";
        }
        
        banner.append("</div>");
        banner.append("</div>");
    
        return log.exit(banner.toString());
    }

    /**
     * Get the 'help' image of the banner in a download page as a HTML 'img' element. 
     *
     * @param id    A {@code String} that is the 'id' attribute of the HTML 'a' element.
     * @return      the {@code String} that is the 'help' image as HTML 'img' element.
     */
    private String getHelpLink(String id) {
        log.entry(id);
        
        RequestParameters urlDoc = this.getNewRequestParameters();
        urlDoc.setPage(RequestParameters.PAGE_DOCUMENTATION);
        urlDoc.setAction(RequestParameters.ACTION_DOC_CALL_DOWLOAD_FILES);

        return log.exit("<a id='" + id + "' class='specific-help' href=''>"+
                        "<img class='details' src='" + this.prop.getImagesRootDirectory() +
                        "help.png' title='Help' alt='Help' /></a>");
    }

    /**
     * Get the 'show headers' image of the banner in a download page as a HTML 'img' element. 
     *
     * @return  the {@code String} that is the 'show headers' image as HTML 'img' element.
     */
    private String getShowHeaderLink(String id) {
        log.entry(id);
        return log.exit("<a id='" + id + "' class='show-header'>" + 
                "<img class='details' src='" + this.prop.getImagesRootDirectory() +
                "plus.png' title='Show headers' alt='Plus'/></a>");
    }

    /**
     * Get the images sources of a download page as a HTML 'div' element. 
     *
     * @return  the {@code String} that is the images sources as HTML 'div' element.
     */
    private String getImageSources() {
        log.entry();
        
        StringBuffer sources = new StringBuffer();
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
     * Generates the script tag for the speciesData object that is accessible
     * from the Javascript code of the page.
     * @param dataGroups The {@code List} of {@code SpeciesDataGroup} for which download files are availables
     * @return A {@String} containing the generated Javascript tag.
     */
    private String getDataGroupScriptTag(List<SpeciesDataGroup> dataGroups) {
        StringBuffer sb = new StringBuffer("<script>");
        sb.append("var speciesData = ");
        sb.append(JSHelper.toJson(dataGroups.stream()
                .collect(Collectors.toMap(SpeciesDataGroup::getId, Function.identity()))));
        sb.append("</script>");
        return sb.toString();
    }

  //TODO: this ugly method is for testing and must disappear ASAP
  	private List<SpeciesDataGroup> getAllSpeciesDataGroup() {
  		try {
  			InputStream in = this.getClass().getResourceAsStream("/bgee.dao.properties");
  			Properties p = new Properties();
  			p.load(in);
  			DAOManager man = DAOManager.getDAOManager(p);
  			ServiceFactory sf = new ServiceFactory(man);
  			return log.exit(sf.getSpeciesDataGroupService().loadAllSpeciesDataGroup());
  		} catch (IOException e) {
  			log.error(e);
  			return log.exit(new LinkedList<>());
  		}
  	}
  	
  	@Override
  	protected void includeJs() {
  		log.entry();
  		super.includeJs();
  		this.includeJs("download.js");
  		this.writeln(getDataGroupScriptTag(new ServiceFactory().getSpeciesDataGroupService().loadAllSpeciesDataGroup()));
  		log.exit();
  	}

    @Override
    protected void includeCss() {
        log.entry();
        super.includeCss();
        this.includeCss("download.css");
        log.exit();
    }
}

