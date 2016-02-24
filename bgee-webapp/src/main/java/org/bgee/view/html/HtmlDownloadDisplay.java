package org.bgee.view.html;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.BgeeProperties;
import org.bgee.controller.RequestParameters;
import org.bgee.model.file.SpeciesDataGroup;
import org.bgee.model.species.Species;
import org.bgee.view.DownloadDisplay;
import org.bgee.view.JsonHelper;

/**
 * This class displays the page having the category "download", i.e. with the parameter
 * page=download for the HTML view.
 * 
 * @author  Mathieu Seppey
 * @author  Valentine Rech de Laval
 * @author  Philippe Moret
 * @version Bgee 13, June 2015
 * @since   Bgee 13
 */
public class HtmlDownloadDisplay extends HtmlParentDisplay implements DownloadDisplay {
 
    private final static Logger log = LogManager.getLogger(HtmlDownloadDisplay.class.getName());
    
    /**
     * An {@code enum defining} for which type of page some {@code SpeciesDataGroup}s 
     * are being displayed. 
     * <ul>
     * <li>{@code PROC_EXPR_VALUES}: processed expression values page.
     * <li>{@code EXPR_CALLS}: expression/diff. expression calls page.
     * <li>{@code HOME_PAGE}: summary of data for the home page.
     * </ul>
     *
     */
    public enum DownloadPageType {
        PROC_EXPR_VALUES, EXPR_CALLS, HOME_PAGE
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
     * @param jsonHelper        A {@code JsonHelper} used to read/write variables into JSON.
     * @param factory           The {@code HtmlFactory} that instantiated this object.
     * @throws IOException      If there is an issue when trying to get or to use the
     *                          {@code PrintWriter} 
     */
    public HtmlDownloadDisplay(HttpServletResponse response, RequestParameters requestParameters, 
            BgeeProperties prop, JsonHelper jsonHelper, HtmlFactory factory) throws IOException {
        super(response, requestParameters, prop, jsonHelper, factory);
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
    public void displayGeneExpressionCallDownloadPage(List<SpeciesDataGroup> groups, 
            Map<String, Set<String>> keywords) {
        log.entry(groups, keywords);
        
        this.startDisplay("Bgee gene expression call download page");
        
        this.writeln(this.getMoreResultDivs());
  		this.writeln(getDataGroupScriptTag(groups));
        this.writeln(getKeywordScriptTag(keywords, groups, DownloadPageType.EXPR_CALLS));
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
        this.writeln(getSingleSpeciesSection(DownloadPageType.EXPR_CALLS, groups, false));

        // Black banner when a species or a group is selected.
        this.writeln(this.getDownloadBanner(DownloadPageType.EXPR_CALLS));
        
        // Multi-species part
        this.writeln(this.getMultiSpeciesSection(DownloadPageType.EXPR_CALLS, groups));

        this.writeln("</div>");

        // Image sources
        this.writeln(getImageSources());
        
        this.endDisplay();
        
        log.exit();
    }

    @Override
    public void displayProcessedExpressionValuesDownloadPage(List<SpeciesDataGroup> groups,
    		                                                 Map<String, Set<String>> keywords) {
        log.entry(groups, keywords);
        
        this.startDisplay("Bgee " + PROCESSED_EXPR_VALUES_PAGE_NAME.toLowerCase() + " download page");
  		this.writeln(getDataGroupScriptTag(groups));
        this.writeln(getKeywordScriptTag(keywords, groups, DownloadPageType.PROC_EXPR_VALUES));
  		this.writeln(this.getExprValuesDirectoryScriptTag(groups));

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
        this.writeln(getSingleSpeciesSection(DownloadPageType.PROC_EXPR_VALUES, groups, false));

        // Black banner when a species or a group is selected.
        this.writeln(this.getDownloadBanner(DownloadPageType.PROC_EXPR_VALUES));
        
        this.writeln("</div>"); // close proc_values div

        // Image sources
        this.writeln(getImageSources());
        
        this.endDisplay();
        
        log.exit();
    }
    
    /**
     * Generates a javascript tag defining variables providing mappings 
     * from {@code SpeciesDataGroup} IDs to directories storing 
     * all Affymetrix and RNA-Seq processed expression values download files, 
     * for the corresponding group. 
     * <p>
     * The javascript maps created in the tag are named {@code rna_seq_expr_values_dirs} 
     * and {@code affy_expr_values_dirs}. 
     * 
     * @param groups    A {@code Collection} of {@code SpeciesDataGroup}s for which 
     *                  to generate mappings to directories. 
     * @return          A {@code String} containing a javascript tag defining the mappings. 
     */
    private String getExprValuesDirectoryScriptTag(Collection<SpeciesDataGroup> groups) {
        log.entry(groups);

        StringBuilder sb = new StringBuilder("<script>");

        String rnaSeqRootDir = this.prop.getDownloadRNASeqProcExprValueFilesRootDirectory();
        sb.append("var rnaSeqExprValuesDirs = ");
        sb.append(this.getJsonHelper().toJson(groups.stream().filter(SpeciesDataGroup::isSingleSpecies)
            .collect(Collectors.toMap(
                SpeciesDataGroup::getId, 
                g -> rnaSeqRootDir + g.getMembers().get(0).getScientificName().replace(" ", "_") + "/"
            ))
        ));
        sb.append(";");

        String affyRootDir = this.prop.getDownloadAffyProcExprValueFilesRootDirectory();
        sb.append("var affyExprValuesDirs = ");
        sb.append(this.getJsonHelper().toJson(groups.stream().filter(SpeciesDataGroup::isSingleSpecies)
            .collect(Collectors.toMap(
                SpeciesDataGroup::getId, 
                g -> affyRootDir + g.getMembers().get(0).getScientificName().replace(" ", "_") + "/"
            ))
        ));
        sb.append(";</script>");
        
        return log.exit(sb.toString());
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
            //pre-condition check in private method, use of assert allowed
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
        
        StringBuilder box = new StringBuilder();
        box.append("<div class='row'>");
        box.append("<div id='bgee_search_box' class='row well well-sm col-md-offset-3 col-md-6'>");
        box.append("    <form  action='/' method='get'>");
        box.append("        <div class='form-group col-md-10'>");
        box.append("            <label for='search_label'>Search species</label>");
        box.append("            <span id='results_nb'></span>");
        box.append("            <input id='search_label' type='text' name='search' class='form-control' "+"value='Scientific name, common name...'/>");
        box.append("        </div>");
        box.append("        <button type='submit' class='btn btn-default col-md-2'>Submit</button>");
        box.append("    </form>");
        box.append("</div>");
        box.append("</div>");
        
        return log.exit(box.toString());
    }


    
    /**
     * Get the multi-species figure list for the given multi-species datagroups
     * according the provided page type.
     *
     * @param pageType  A {@code DownloadPageType} that is the type of the page.
     * @return          the {@code String} that is the multi-species section as HTML 'div' element,
     *                  according {@code pageType}.
     */
    private String getMultiSpeciesFigures(DownloadPageType pageType,  List<SpeciesDataGroup> groups) {
    	StringBuilder sb = new StringBuilder();
    	
    	for (SpeciesDataGroup sdg: groups) {
    		if (sdg.isMultipleSpecies()) {
    			Map<String, String> attr = new HashMap<>();
    			attr.put("id", htmlEntities(sdg.getId()));
    			attr.put("name", htmlEntities(sdg.getName()));
    			sb.append(getHTMLTag("figure", attr, getHTMLTag("div", 
    			        getSpeciesImages(sdg.getMembers(), pageType) + getCaption(sdg))));
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
    private String getMultiSpeciesSection(DownloadPageType pageType, List<SpeciesDataGroup> groups) {
        log.entry(pageType);

        StringBuilder s = new StringBuilder(); 
        s.append("<div id='bgee_multi_species'>");
        s.append("<h2>Multi-species" +
                 "<span class='header_details'>(orthologous genes in homologous anatomical structures)</span></h2>");
        s.append("<div class='bgee_section bgee_download_section'>");
        s.append(getMultiSpeciesFigures(pageType, groups));
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
    
        StringBuilder banner = new StringBuilder();
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
            banner.append(this.getHelpLink("orthologs_help"));
            banner.append("<div id='ortholog_data'>" +
                    "<a id='ortholog_csv' class='download_link' href='' download></a>" +
                    this.getShowHeaderLink("show_ortholog_headers") + 
                    "</div>");
            banner.append("<div id='ortholog_headers' class='header_table'>" +
                    HtmlDocumentationCallFile.getOMAGroupFileHeaderDesc() + "</div>");
            banner.append("<p class='file_info'>This file provides groups of genes orthologous "
                    + "between the selected taxa.</p>");
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
     * Gets the html code to display images of species in figures.
     * 
     * @param species   A {@code List} of {@code Species} to be displayed in a figure.
     * @param pageType  A {@code DownloadPageType} for which to generate img tags.
     * @return          A {@String} containing the html code to display species images.
     */
    private String getSpeciesImages(List<Species> species, DownloadPageType pageType) {
        log.entry(species, pageType);
        
        //sanity check in private method, assert acceptable
        assert !species.isEmpty();
        
        StringBuilder images = new StringBuilder();
        for (Species spe : species) {
            Map<String,String> attrs = new HashMap<>();
            attrs.put("src", this.prop.getSpeciesImagesRootDirectory() + htmlEntities(spe.getId())+"_light.jpg");
            attrs.put("alt", htmlEntities(spe.getShortName()));
            attrs.put("class", "species_img");
            images.append(getHTMLTag("img", attrs));
        }
        if (pageType == DownloadPageType.PROC_EXPR_VALUES) {
            Map<String,String> attrs = new HashMap<>();
            attrs.put("src", this.prop.getLogoImagesRootDirectory() + "proc_values_zoom_logo.png");
            attrs.put("alt", htmlEntities(PROCESSED_EXPR_VALUES_PAGE_NAME));
            attrs.put("class", "page_img");
            images.append(getHTMLTag("img", attrs));
        }
        
        return log.exit(images.toString());
    }

    /**
     * Get the single species section of a download page as a HTML 'div' element,
     * according to the provided page type and data groups.
     *
     * @param pageType                  A {@code DownloadPageType} that is the type of the page.
     * @param groups                    A {@code List} of {@code SpeciesDataGroup}s 
     *                                  containing the information to be displayed.
     * @param includeDataGroupScriptTag A {@code boolean} defining whether this method 
     *                                  should be responsible for including the javascript tag 
     *                                  containing the species data groups information as JSON. 
     *                                  If not already included, should be {@code true}.
     * @return                          A {@code String} that is the single species section in HTML.
     */
    protected String getSingleSpeciesSection(DownloadPageType pageType, 
            List<SpeciesDataGroup> groups, boolean includeDataGroupScriptTag) {
        log.entry(pageType, groups, includeDataGroupScriptTag);

        StringBuilder s = new StringBuilder();
        if (includeDataGroupScriptTag) {
            s.append(getDataGroupScriptTag(groups));
        }
        s.append("<div id='bgee_uniq_species'> ");
        switch (pageType) {
        case PROC_EXPR_VALUES:
            s.append("<h2>Species with data in Bgee");
            break;
        case EXPR_CALLS: 
            s.append("<h2>Single-species");
            break; 
        default: 
            throw log.throwing(new IllegalArgumentException("DownloadPageType not supported: " + pageType));
        }
        s.append("<span class='header_details'>(click on species to see more details)</span></h2>");
        s.append("<div class='bgee_section bgee_download_section'>");
        s.append(getSingleSpeciesFigures(pageType, groups));
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
    //XXX: Could this method take a List<Species>, the ID attr should be the species ID, 
    //this way CommandHome would not need to call getSpeciesDataGroupService, 
    //but simply SpeciesService#loadSpeciesInDataGroups()
    private String getSingleSpeciesFigures(DownloadPageType pageType, List<SpeciesDataGroup> groups) {
        StringBuilder sb = new StringBuilder();

        groups.stream().filter(sdg -> sdg.isSingleSpecies()).forEach(sdg -> {
            Species species = sdg.getMembers().get(0);
            Map<String, String> attr = new HashMap<>();
            attr.put("id", htmlEntities(sdg.getId()));
            sb.append(getHTMLTag("figure", attr, getHTMLTag("div", 
                    getSpeciesImages(sdg.getMembers(), pageType)) + getCaption(species, sdg)));
        });
        return sb.toString();
    }

    /**
     * Generates the script tag for the speciesData object that is accessible
     * from the Javascript code of the page.
     * @param dataGroups The {@code List} of {@code SpeciesDataGroup} for which download files are availables
     * @return A {@String} containing the generated Javascript tag.
     */
    private String getDataGroupScriptTag(List<SpeciesDataGroup> dataGroups) {
        log.entry(dataGroups);
        StringBuilder sb = new StringBuilder("<script>");
        sb.append("var speciesData = ");
        sb.append(this.getJsonHelper().toJson(dataGroups.stream()
                .collect(Collectors.toMap(SpeciesDataGroup::getId, Function.identity()))));
        sb.append(";</script>");
        return log.exit(sb.toString());
    }
    
    /**
     * Generates the script tag for the species keywords (one entry is created per species group).
     * 
     * @param keywords  The {@code Map} of species id to keywords
     * @param groups    The {@code List} of species data groups
     * @param pageType  The {@code DownloadPageType} to generate the script tag for.
     * @return          A {@String} containing the generated Javascript tag.
     * @throws IllegalArgumentException If {@code keywords} is missing a mapping 
     *                                  for a {@code Species} member of a {@code SpeciesDataGroup}.
     */
    private String getKeywordScriptTag(Map<String, Set<String>> keywords, 
            List<SpeciesDataGroup> groups, DownloadPageType pageType) throws IllegalArgumentException {
        log.entry(keywords, groups, pageType);

        //check that we have keywords for all species (we should at least have their names, etc)
        //(not all species had keywords in a previous version, and the stream operations below 
        //were throwing a null pointer exception)
        if (!groups.stream().flatMap(e -> e.getMembers().stream()).map(spe -> spe.getId())
                .collect(Collectors.toSet()).equals(keywords.keySet())) {
            throw log.throwing(new IllegalArgumentException("Some species are missing associated keywords."));
        }
        
        //Map group ID -> associated search terms
        Map<String, Set<String>> groupIdsToTerms = groups.stream()
                //skip multi-species groups for processed expression values pages, 
                //to avoid proposing completion for multi-species group names (e.g., "macaque/chimpanzee")
                .filter(e -> pageType == DownloadPageType.EXPR_CALLS || e.isSingleSpecies())
                .collect(Collectors.toMap(e -> e.getId(), e -> {
                    Set<String> terms = new HashSet<>();
                    //first, store the group name to allow search by group name
                    terms.add(e.getName());
                    //then, add keywords associated to all species members of the group
                    terms.addAll(e.getMembers().stream()
                            .flatMap(spe -> keywords.get(spe.getId()).stream())
                            .collect(Collectors.toSet()));
                    return terms;
                }));
        log.trace("Group IDs to terms: {}", groupIdsToTerms);
        
        StringBuilder sb = new StringBuilder("<script>");
        sb.append("var keywords = ");
        sb.append(this.getJsonHelper().toJson(groupIdsToTerms));
        sb.append(";\n");
        
        sb.append("var autocomplete = ");
        sb.append(this.getJsonHelper().toJson(groupIdsToTerms.values().stream()
                .flatMap(e -> e.stream())
                .distinct() // filter potential duplicates 
                .sorted()   // sort the autocompletion list alphabetically
                .collect(Collectors.toList())));
        sb.append(";</script>");
        
        return log.exit(sb.toString());
    }
    
    /**
     * Gets the {@code figcaption} element for a given {@code Species} belonging to {@code group}.
     * @param species   A {@Species}
     * @param group     The {@code SpeciesDataGroup} which {@code species} belongs to.
     * @return A {@code String} containing the html code.
     */
    private static String getCaption(Species species, SpeciesDataGroup group) {
        log.entry(species, group);
        return log.exit(getHTMLTag("figcaption", getShortNameTag(species)
                //we display the group name as subtitle rather than the species common name, 
                //because we used to have incorrect common names at some point, 
                //and because this allows more flexibility (e.g. "human including GTEx data")
                +getHTMLTag("p", htmlEntities(group.getName()))));
    }
    
    /**
     * Gets the {@code figcaption} element for a given {@code SpeciesDataGroup}
     * @param species A {SpeciesDataGroup}
     * @return A {@code String} containing the html code
     */
    private static String getCaption(SpeciesDataGroup speciesDataGroup) {
    	return getHTMLTag("figcaption", htmlEntities(speciesDataGroup.getName()));
    }
    
    /**
     * Gets the short name element for a given {@Species}
     * @param species A {@Species}
     * @return A {@code String} containing the html code
     */
    private static String getShortNameTag(Species species) {
    	return getHTMLTag("p", getHTMLTag("i", htmlEntities(species.getShortName())));
    }
    
    
  	@Override
  	protected void includeJs() {
  		log.entry();
  		super.includeJs();
        //If you ever add new files, you need to edit bgee-webapp/pom.xml 
        //to correctly merge/minify them.
  		this.includeJs("download.js");
  		log.exit();
  	}

    @Override
    protected void includeCss() {
        log.entry();
        super.includeCss();
        //If you ever add new files, you need to edit bgee-webapp/pom.xml 
        //to correctly merge/minify them.
        this.includeCss("download.css");
        log.exit();
    }
}

