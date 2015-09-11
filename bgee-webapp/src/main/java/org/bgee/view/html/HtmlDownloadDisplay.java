package org.bgee.view.html;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.BgeeProperties;
import org.bgee.controller.RequestParameters;
import org.bgee.model.ServiceFactory;
import org.bgee.model.dao.api.DAOManager;
import org.bgee.model.file.SpeciesDataGroup;
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
        s.append(generateSpeciesFigure(9606, pageType));
        s.append(generateSpeciesFigure(10090, pageType));
        s.append(generateSpeciesFigure(7955, pageType));
        s.append(generateSpeciesFigure(7227, pageType));
        s.append(generateSpeciesFigure(6239, pageType));
        s.append(generateSpeciesFigure(9597, pageType));
        s.append(generateSpeciesFigure(9598, pageType));
        s.append(generateSpeciesFigure(9593, pageType));
//        s.append(generateSpeciesFigure(9600, pageType)); // no more data for Pongo pygmaeus
        s.append(generateSpeciesFigure(9544, pageType));
        s.append(generateSpeciesFigure(10116, pageType));
        s.append(generateSpeciesFigure(9913, pageType));
        s.append(generateSpeciesFigure(9823, pageType));
        s.append(generateSpeciesFigure(13616, pageType));
        s.append(generateSpeciesFigure(9258, pageType));
        s.append(generateSpeciesFigure(9031, pageType));
        s.append(generateSpeciesFigure(28377, pageType));
        s.append(generateSpeciesFigure(8364, pageType));
//        s.append(generateSpeciesFigure(99883, pageType)); // no more data for Tetraodon nigroviridis
        s.append("</div>");
        s.append("</div>");
        
        return log.exit(s.toString());
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
        //TODO set all groups and with all species when all files will be generated 
        // Pairwises
        s.append(generateSpeciesFigure(Arrays.asList(9606, 10090), GROUP_NAME_HUMAN_MOUSE, true, pageType));
        //s.append(generateSpeciesFigure(Arrays.asList(9606, 7955), GROUP_NAME_HUMAN_ZEBRAFISH, true, pageType));
        //s.append(generateSpeciesFigure(Arrays.asList(9606, 7227), GROUP_NAME_HUMAN_FRUITFLY, true, pageType));
        //s.append(generateSpeciesFigure(Arrays.asList(9606, 6239), GROUP_NAME_HUMAN_NEMATODE, true, pageType));
        //s.append(generateSpeciesFigure(Arrays.asList(10090, 7955), GROUP_NAME_MOUSE_ZEBRAFISH, true, pageType));
        //s.append(generateSpeciesFigure(Arrays.asList(10090, 7227), GROUP_NAME_MOUSE_FRUITFLY, true, pageType));
        //s.append(generateSpeciesFigure(Arrays.asList(10090, 6239), GROUP_NAME_MOUSE_NEMATODE, true, pageType));
        //s.append(generateSpeciesFigure(Arrays.asList(7955, 7227), GROUP_NAME_ZEBRAFISH_FRUITFLY, true, pageType));
        //s.append(generateSpeciesFigure(Arrays.asList(7955, 6239), GROUP_NAME_ZEBRAFISH_NEMATODE, true, pageType));
        //s.append(generateSpeciesFigure(Arrays.asList(7227, 6239), GROUP_NAME_FRUITFLY_NEMATODE, true, pageType));
        // Groups
        //        s.append(generateSpeciesFigure(Arrays.asList(9606, 9598, 9593, 9544), GROUP_NAME_CATARRHINI, true, pageType));
        //        s.append(generateSpeciesFigure(Arrays.asList(9598, 9597, 9606, 9593, 9544), GROUP_NAME_PRIMATES, true, pageType));
        //        s.append(generateSpeciesFigure(Arrays.asList(9598, 9597, 9606, 9593, 9544, 10116, 10090, 9913, 9823, 13616), GROUP_NAME_THERIA, true, pageType));
        //        s.append(generateSpeciesFigure(Arrays.asList(9598, 9597, 9606, 9593, 9544, 10116, 10090, 9913, 9823, 13616, 9258), GROUP_NAME_MAMMALIA, true, pageType));
        //        s.append(generateSpeciesFigure(Arrays.asList(9598, 9597, 9606, 9593, 9544, 10116, 10090, 9913, 9823, 13616, 9258, 28377, 9031), GROUP_NAME_AMNIOTA, true, pageType));
        //        s.append(generateSpeciesFigure(Arrays.asList(9598, 9597, 9606, 9593, 9544, 10116, 10090, 9913, 9823, 13616, 9258, 28377, 9031, 8364, 7955, 7227, 6239), GROUP_NAME_BILATERIA, true, pageType));
        //        s.append(generateSpeciesFigure(Arrays.asList(9606, 9544), GROUP_NAME_CATARRHINI, true, pageType));
        s.append(generateSpeciesFigure(Arrays.asList(10090, 10116), GROUP_NAME_MURINAE, true, pageType));
        s.append(generateSpeciesFigure(Arrays.asList(9598, 9544), GROUP_NAME_MACAQUE_CHIMP, true, pageType));
        s.append(generateSpeciesFigure(Arrays.asList(9606, 9598, 9593, 9544, 10116, 10090, 9913, 13616), GROUP_NAME_THERIA, true, pageType));
        s.append(generateSpeciesFigure(Arrays.asList(9606, 9598, 9593, 9544, 10116, 10090, 9913, 13616, 9258), GROUP_NAME_MAMMALIA, true, pageType));
        s.append(generateSpeciesFigure(Arrays.asList(9606, 9598, 9593, 9544, 10116, 10090, 9913, 13616, 9258, 9031), GROUP_NAME_AMNIOTA, true, pageType));
        s.append(generateSpeciesFigure(Arrays.asList(9606, 9598, 9593, 9544, 10116, 10090, 9913, 13616, 9258, 9031, 8364), GROUP_NAME_TETRAPODA, true, pageType));
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
     * Generate the HTML figure tag with a figcaption tag from a {@code int} that is a 
     * species ID.
     * 
     * @param speciesId     An {@code int} that is the species ID of the species to be 
     *                      displayed.
     * @param pageType      A {@code DownloadPageType} that is the type of the page.
     * @return              A {@code String} that is the  HTML figure tag generated from the 
     *                      provided {@code int} of a species ID.
     */
    private String generateSpeciesFigure(int speciesId, DownloadPageType pageType) {
        log.entry(speciesId, pageType);
        return log.exit(generateSpeciesFigure(Arrays.asList(speciesId), null, false, pageType));
    }

    /**
     * Generate the HTML figure tag from a {@code List} of species IDs.
     * 
     * @param speciesIds   A {@code List} of {@code Integer} containing the species IDs to
     *                     be diplayed.
     * @param figcaption   A {@code String} that is the fieldcaption of the figure. If empty 
     *                     or {@code null}, it's generated with the last species of the 
     *                     {@code List}.
     * @param isGroup      A {@code boolean} that is {@code true} if the figure represents 
     *                     a group of species.
     * @param pageType     A {@code DownloadPageType} that is the type of the page.
     * @return             A {@code String} that is the  HTML figure tag generated from the 
     *                     provided {@code List} of species IDs.
     */
    private String generateSpeciesFigure(List<Integer> speciesIds, String figcaption, 
            boolean isGroup, DownloadPageType pageType) {
        log.entry(speciesIds, figcaption, isGroup, pageType);
        
        StringBuilder images = new StringBuilder();
        if (speciesIds == null || speciesIds.size() == 0) {
            return ("");
        }
        
        String name = null, commonName = null, shortName = null, alternateNames = null;

        // Hidden info, to improve the jQuery search, allow to look for any of the name, short,
        // or common name, even if not displayed... for example droso.
        String hiddenInfo = "";
        for (Integer speciesId: speciesIds) {
            switch(speciesId) {
                case 9606: 
                    name = "Homo sapiens";
                    shortName = "H. sapiens";
                    commonName = "human";
                    alternateNames = "";
                    break;
                case 10090: 
                    name = "Mus musculus";
                    shortName="M. musculus";
                    commonName = "mouse";
                    alternateNames = "house mouse, mice";
                    break;
                case 7955: 
                    name = "Danio rerio";
                    shortName = "D. rerio";
                    commonName = "zebrafish";
                    alternateNames = "leopard danio, zebra danio";
                    break;
                case 7227: 
                    name = "Drosophila melanogaster";
                    shortName = "D. melanogaster";
                    commonName = "fruit fly";
                    alternateNames = "vinegar fly";
                    break;
                case 6239: 
                    name = "Caenorhabditis elegans";
                    shortName = "C. elegans";
                    commonName = "nematode";
                    alternateNames = "worm, roundworm";
                    break;
                case 9597: 
                    name = "Pan paniscus";
                    shortName = "P. paniscus";
                    commonName = "bonobo";
                    alternateNames = "pygmy chimpanzee";
                    break;
                case 9598: 
                    name = "Pan troglodytes";
                    shortName = "P. troglodytes";
                    commonName = "chimpanzee";
                    alternateNames = "";
                    break;
                case 9593: 
                    name = "Gorilla gorilla";
                    shortName = "G. gorilla";
                    commonName = "gorilla";
                    alternateNames = "western gorilla";
                    break;
                case 9600: 
                    name = "Pongo pygmaeus";
                    shortName = "P. pygmaeus";
                    commonName = "orangutan";
                    alternateNames = "orang utan, orang-utan";
                    break;
                case 9544: 
                    name = "Macaca mulatta";
                    shortName = "M. mulatta";
                    commonName = "macaque";
                    alternateNames = "rhesus monkey";
                    break;
                case 10116: 
                    name = "Rattus norvegicus";
                    shortName = "R. norvegicus";
                    commonName = "rat";
                    alternateNames = "brown rat";
                    break;
                case 9913: 
                    name = "Bos taurus";
                    shortName = "B. taurus";
                    commonName = "cattle";
                    alternateNames = "cow, domestic cow, domestic cattle, bovine cow";
                    break;
                case 9823: 
                    name = "Sus scrofa";
                    shortName = "S. scrofa";
                    commonName = "pig";
                    alternateNames = "domestic pig, swine";
                    break;
                case 13616: 
                    name = "Monodelphis domestica";
                    shortName = "M. domestica";
                    commonName = "opossum";
                    alternateNames = "gray short-tailed opossum, gray short tailed opossum";
                    break;
                case 9258: 
                    name = "Ornithorhynchus anatinus";
                    shortName = "O. anatinus";
                    commonName = "platypus";
                    alternateNames = "duckbill platypus, duck-billed platypus";
                    break;
                case 9031: 
                    name = "Gallus gallus";
                    shortName = "G. gallus";
                    commonName = "chicken";
                    alternateNames = "bantam, red junglefowl, red jungle fowl";
                    break;
                case 28377: 
                    name = "Anolis carolinensis";
                    shortName = "A. carolinensis";
                    commonName = "green anole";
                    alternateNames = "anolis, carolina anole";
                    break;
                case 8364: 
                    name = "Xenopus tropicalis";
                    shortName = "X. tropicalis";
                    commonName = "western clawed frog";
                    alternateNames = "xenopus";
                    break;
                case 99883: 
                    name = "Tetraodon nigroviridis";
                    shortName = "T. nigroviridis";
                    commonName = "tetraodon";
                    alternateNames = "spotted green pufferfish";
                    break;
                default:
                    return ("");
            }
            
            if (isGroup) {
                hiddenInfo = hiddenInfo.concat(name + ", " + commonName + ", ");
            } else {
                hiddenInfo = name;
            }
            
            images.append(generateSpeciesImg(speciesId, name, shortName, commonName, alternateNames, true));
        }
        if (StringUtils.isBlank(figcaption)) {
            // If empty or null, it's generated with the last species ID of the given List. 
            figcaption = "<p><i>" + shortName + "</i></p><p>" + commonName + "</p>";   
        }

        String figure = null;
        if (isGroup) {
            figure = "<figure data-bgeegroupname='" + figcaption + "' " + 
                    this.getGroupFileData(figcaption) + ">";
        } else {
            figure = "<figure " + this.getSingleSpeciesFileData(speciesIds.get(0), pageType) + ">";
        }

        String pageImg = "";
        if (pageType.equals(DownloadPageType.PROC_EXPR_VALUES)) {
            pageImg = "<img class='page_img' src='" + this.prop.getLogoImagesRootDirectory() + 
                    "proc_values_zoom_logo.png' alt='" + PROCESSED_EXPR_VALUES_PAGE_NAME + "' />";
        }

        figure += "<div>" + images + pageImg + "</div>" + 
                  "<figcaption>" + figcaption + 
                  " <span class='invisible'>" + hiddenInfo + "</span>" + 
                  "</figcaption>" + 
                  "</figure>";
        return log.exit(figure);
    }

    private String getDataGroupScriptTag(List<SpeciesDataGroup> dataGroups) {
        StringBuffer sb = new StringBuffer("<script>");
        sb.append("var speciesData = ");
        Map<String, SpeciesDataGroup> map = new HashMap<>();
        for (SpeciesDataGroup sdg: dataGroups) {
        	map.put(sdg.getId(), sdg);
        }
        sb.append(JSHelper.toJson(map));
        sb.append("</script>");
        return sb.toString();
    }

    /**
     * Get custom data for a group.
     * 
     * @param groupName A {@code String} that is the name of the group.
     * @return          A {@code String} that is data according to the given group name.
     */
    private String getGroupFileData(String groupName) {
        log.entry(groupName);
        
        String diffExprAnatSimpleFileSize = null, diffExprAnatCompleteFileSize = null,
//                exprSimpleFileSize = null, exprCompleteFileSize = null,  
//                diffExprDevSimpleFileSize = null, diffExprDevCompleteFileSize = null, 
                orthologFileSize = null, filePrefix = null;

        switch (groupName) {
            //TODO: set file sizes            
            case GROUP_NAME_HUMAN_MOUSE:
//                exprSimpleFileSize = "xx MB";
//                exprCompleteFileSize = "xx MB"; 
                diffExprAnatSimpleFileSize = "0.7 MB";
                diffExprAnatCompleteFileSize  = "6 MB";
//                diffExprDevSimpleFileSize = "xx MB";
//                diffExprDevCompleteFileSize = "xx MB"; 
                orthologFileSize = "0.4 MB"; 
                filePrefix= "human_mouse";
                break;         
            case GROUP_NAME_MACAQUE_CHIMP:
//              exprSimpleFileSize = "xx MB";
//              exprCompleteFileSize = "xx MB"; 
              diffExprAnatSimpleFileSize = "0.3 MB";
              diffExprAnatCompleteFileSize  = "1 MB";
//              diffExprDevSimpleFileSize = "xx MB";
//              diffExprDevCompleteFileSize = "xx MB"; 
              orthologFileSize = "0.3 MB"; 
              filePrefix= "macaque_chimpanzee";
              break;
            case GROUP_NAME_HUMAN_ZEBRAFISH:
//                exprSimpleFileSize = "xx MB";
//                exprCompleteFileSize = "xx MB"; 
//                diffExprAnatSimpleFileSize = "xx MB";
//                diffExprAnatCompleteFileSize  = "xx MB";
//                diffExprDevSimpleFileSize = "xx MB";
//                diffExprDevCompleteFileSize = "xx MB"; 
                orthologFileSize = "xx MB"; 
                filePrefix= "human_zebrafish";
                break;
            case GROUP_NAME_HUMAN_FRUITFLY:
//                exprSimpleFileSize = "xx MB";
//                exprCompleteFileSize = "xx MB"; 
//                diffExprAnatSimpleFileSize = "xx MB";
//                diffExprAnatCompleteFileSize  = "xx MB";
//                diffExprDevSimpleFileSize = "xx MB";
//                diffExprDevCompleteFileSize = "xx MB"; 
                orthologFileSize = "xx MB"; 
                filePrefix= "human_fruitfly";
                break;
            case GROUP_NAME_HUMAN_NEMATODE:
//                exprSimpleFileSize = "xx MB";
//                exprCompleteFileSize = "xx MB"; 
//                diffExprAnatSimpleFileSize = "xx MB";
//                diffExprAnatCompleteFileSize  = "xx MB";
//                diffExprDevSimpleFileSize = "xx MB";
//                diffExprDevCompleteFileSize = "xx MB"; 
                orthologFileSize = "xx MB"; 
                filePrefix= "human_nematode";
                break;
            case GROUP_NAME_MOUSE_ZEBRAFISH:
//                exprSimpleFileSize = "xx MB";
//                exprCompleteFileSize = "xx MB"; 
//                diffExprAnatSimpleFileSize = "xx MB";
//                diffExprAnatCompleteFileSize  = "xx MB";
//                diffExprDevSimpleFileSize = "xx MB";
//                diffExprDevCompleteFileSize = "xx MB"; 
                orthologFileSize = "xx MB"; 
                filePrefix= "mouse_zebrafish";
                break;
            case GROUP_NAME_MOUSE_FRUITFLY:
//                exprSimpleFileSize = "xx MB";
//                exprCompleteFileSize = "xx MB"; 
//                diffExprAnatSimpleFileSize = "xx MB";
//                diffExprAnatCompleteFileSize  = "xx MB";
//                diffExprDevSimpleFileSize = "xx MB";
//                diffExprDevCompleteFileSize = "xx MB"; 
                orthologFileSize = "xx MB"; 
                filePrefix= "mouse_fruitfly";
                break;
            case GROUP_NAME_MOUSE_NEMATODE:
//                exprSimpleFileSize = "xx MB";
//                exprCompleteFileSize = "xx MB"; 
//                diffExprAnatSimpleFileSize = "xx MB";
//                diffExprAnatCompleteFileSize  = "xx MB";
//                diffExprDevSimpleFileSize = "xx MB";
//                diffExprDevCompleteFileSize = "xx MB"; 
                orthologFileSize = "xx MB"; 
                filePrefix= "mouse_nematode";
                break;
            case GROUP_NAME_ZEBRAFISH_FRUITFLY:
//                exprSimpleFileSize = "xx MB";
//                exprCompleteFileSize = "xx MB"; 
//                diffExprAnatSimpleFileSize = "xx MB";
//                diffExprAnatCompleteFileSize  = "xx MB";
//                diffExprDevSimpleFileSize = "xx MB";
//                diffExprDevCompleteFileSize = "xx MB"; 
                orthologFileSize = "xx MB"; 
                filePrefix= "zebrafish_fruitfly";
                break;
            case GROUP_NAME_ZEBRAFISH_NEMATODE:
//                exprSimpleFileSize = "xx MB";
//                exprCompleteFileSize = "xx MB"; 
//                diffExprAnatSimpleFileSize = "xx MB";
//                diffExprAnatCompleteFileSize  = "xx MB";
//                diffExprDevSimpleFileSize = "xx MB";
//                diffExprDevCompleteFileSize = "xx MB"; 
                orthologFileSize = "xx MB"; 
                filePrefix= "zebrafish_nematode";
                break;
            case GROUP_NAME_FRUITFLY_NEMATODE:
//                exprSimpleFileSize = "xx MB";
//                exprCompleteFileSize = "xx MB"; 
//                diffExprAnatSimpleFileSize = "xx MB";
//                diffExprAnatCompleteFileSize  = "xx MB";
//                diffExprDevSimpleFileSize = "xx MB";
//                diffExprDevCompleteFileSize = "xx MB"; 
                orthologFileSize = "xx MB"; 
                filePrefix= "fruitfly_nematode";
                break;
            case GROUP_NAME_CATARRHINI:
//                exprSimpleFileSize = "xx MB";
//                exprCompleteFileSize = "xx MB"; 
//                diffExprAnatSimpleFileSize = "xx MB";
//                diffExprAnatCompleteFileSize  = "xx MB";
//                diffExprDevSimpleFileSize = "xx MB";
//                diffExprDevCompleteFileSize = "xx MB"; 
                orthologFileSize = "xx MB"; 
                filePrefix= "catarrhini";
                break;
            case GROUP_NAME_MURINAE:
//                exprSimpleFileSize = "xx MB";
//                exprCompleteFileSize = "xx MB"; 
                diffExprAnatSimpleFileSize = "0.7 MB";
                diffExprAnatCompleteFileSize  = "4 MB";
//                diffExprDevSimpleFileSize = "xx MB";
//                diffExprDevCompleteFileSize = "xx MB"; 
                orthologFileSize = "0.3 MB"; 
                filePrefix= "murinae";
                break;
            case GROUP_NAME_THERIA:
//                exprSimpleFileSize = "xx MB";
//                exprCompleteFileSize = "xx MB"; 
                diffExprAnatSimpleFileSize = "3 MB";
                diffExprAnatCompleteFileSize  = "15 MB";
//                diffExprDevSimpleFileSize = "xx MB";
//                diffExprDevCompleteFileSize = "xx MB"; 
                orthologFileSize = "1 MB"; 
                filePrefix= "theria";
                break;
            case GROUP_NAME_MAMMALIA:
//                exprSimpleFileSize = "xx MB";
//                exprCompleteFileSize = "xx MB"; 
                diffExprAnatSimpleFileSize = "2 MB";
                diffExprAnatCompleteFileSize  = "12 MB";
//                diffExprDevSimpleFileSize = "xx MB";
//                diffExprDevCompleteFileSize = "xx MB"; 
                orthologFileSize = "0.7 MB"; 
                filePrefix= "mammalia";
                break;
            case GROUP_NAME_AMNIOTA:
//                exprSimpleFileSize = "xx MB";
//                exprCompleteFileSize = "xx MB"; 
                diffExprAnatSimpleFileSize = "3 MB";
                diffExprAnatCompleteFileSize  = "17 MB";
//                diffExprDevSimpleFileSize = "xx MB";
//                diffExprDevCompleteFileSize = "xx MB"; 
                orthologFileSize = "1 MB"; 
                filePrefix= "amniota";
                break;
            case GROUP_NAME_TETRAPODA:
//              exprSimpleFileSize = "xx MB";
//              exprCompleteFileSize = "xx MB"; 
              diffExprAnatSimpleFileSize = "3 MB";
              diffExprAnatCompleteFileSize  = "17 MB";
//              diffExprDevSimpleFileSize = "xx MB";
//              diffExprDevCompleteFileSize = "xx MB"; 
              orthologFileSize = "1 MB"; 
              filePrefix= "tetrapoda";
              break;
            case GROUP_NAME_BILATERIA:
//                exprSimpleFileSize = "xx MB";
//                exprCompleteFileSize = "xx MB"; 
                diffExprAnatSimpleFileSize = "632 KB";
                diffExprAnatCompleteFileSize  = "3.4 MB";
//                diffExprDevSimpleFileSize = "xx MB";
//                diffExprDevCompleteFileSize = "xx MB"; 
                orthologFileSize = "xx MB"; 
                filePrefix= "bilateria";
                break;
            default:
                throw log.throwing(new IllegalArgumentException("Unrecognized group: " + groupName));
        }
        
//        String beginExprFilePath = this.prop.getDownloadMultiExprFilesRootDirectory() + filePrefix + "_";
        // TODO: remove hardcoded "_" in file names. 
        // Use BgeeProperties... or RequestParameters ? or static variables?
        String beginDiffExprFilePath = this.prop.getDownloadMultiDiffExprFilesRootDirectory() + filePrefix + "_";
        String extension = ".tsv.zip";
        
        String data = "";
        // TODO: remove hardcoded "_orthologs" in file names. 
        // Use BgeeProperties... or RequestParameters ? or static variables?
        data += " data-bgeeorthologfileurl='" + this.prop.getDownloadOrthologFilesRootDirectory() + 
                filePrefix + "_orthologs" + extension +
                "' data-bgeeorthologfilesize='" + orthologFileSize + "'";
        
//        if (exprSimpleFileSize != null) {
//            data += " data-bgeeexprsimplefileurl='" + beginExprFilePath + 
//                    "multi-expr-simple" + extension +
//                    "' data-bgeeexprsimplefilesize='" + diffExprAnatSimpleFileSize + "'"; 
//        }
//        if (exprCompleteFileSize != null) {
//            data += " data-bgeeexprcompletefileurl='" + beginExprFilePath + 
//                    "multi-expr-complete" + extension +
//                    "' data-bgeeexprcompletefilesize='" + diffExprAnatSimpleFileSize + "'"; 
//        }
        
        if (diffExprAnatSimpleFileSize != null) {
            data += " data-bgeediffexpranatomysimplefileurl='" + beginDiffExprFilePath + 
                    "multi-diffexpr-anatomy-simple" + extension +
                    "' data-bgeediffexpranatomysimplefilesize='" + diffExprAnatSimpleFileSize + "'"; 
        }
        if (diffExprAnatCompleteFileSize != null) {
            data += " data-bgeediffexpranatomycompletefileurl='" + beginDiffExprFilePath + 
                    "multi-diffexpr-anatomy-complete" + extension +
                    "' data-bgeediffexpranatomycompletefilesize='" + diffExprAnatCompleteFileSize + "'"; 
        }
//        if (diffExprDevSimpleFileSize != null) {
//            data += " data-bgeediffexprdevelopmentsimplefileurl='" + beginDiffExprFilePath + 
//                    "multi-diffexpr-development-simple" + extension +
//                    "' data-bgeediffexprdevelopmentsimplefilesize='" + diffExprDevSimpleFileSize + "'"; 
//        }
//        if (diffExprDevCompleteFileSize != null) {
//            data += " data-bgeediffexprdevelopmentcompletefileurl='" + beginDiffExprFilePath + 
//                    "multi-diffexpr-development-complete" + extension +
//                    "' data-bgeediffexprdevelopmentcompletefilesize='" + diffExprDevCompleteFileSize + "'"; 
//        }
        return log.exit(data);
    }
    
    /**
     * Get custom data for a single species.
     * 
     * @param speciesId A {@code String} that is the ID of the species.
     * @param pageType  A {@code DownloadPageType} that is the type of the page.
     * @return          A {@code String} that is data according to the given species ID.
     */
    private String getSingleSpeciesFileData(int speciesId, DownloadPageType pageType) {
        log.entry(speciesId, pageType);
        
        String exprSimpleFileSize = null, exprCompleteFileSize = null, 
                diffExprAnatSimpleFileSize = null, diffExprAnatCompleteFileSize = null, 
                diffExprDevSimpleFileSize = null, diffExprDevCompleteFileSize = null, 
                rnaSeqDataFileSize = null, affyDataFileSize = null, inSituDataFileSize = null, 
                estDataFileSize = null, rnaSeqAnnotFileSize = null, affyAnnotFileSize = null, 
                inSituAnnotFileSize = null, estAnnotFileSize = null, latinName = null;

        switch (speciesId) {
            case 9606: 
                exprSimpleFileSize = "87 MB";
                exprCompleteFileSize = "711 MB"; 
                diffExprAnatSimpleFileSize = "4.4 MB";
                diffExprAnatCompleteFileSize  = "25 MB";
                diffExprDevSimpleFileSize = "0.5 MB";
                diffExprDevCompleteFileSize = "13 MB"; 
                rnaSeqDataFileSize = "32 MB";
                rnaSeqAnnotFileSize = "6 KB";
                affyDataFileSize = "1.4 GB";
                affyAnnotFileSize = "0.3 MB";
                //estDataFileSize = "xx MB";
                //estAnnotFileSize = "xx MB";
                latinName = "Homo_sapiens";
                break;
            case 10090: 
                exprSimpleFileSize = "121 MB";
                exprCompleteFileSize = "1.2 GB"; 
                diffExprAnatSimpleFileSize = "8 MB";
                diffExprAnatCompleteFileSize  = "40 MB";
                diffExprDevSimpleFileSize = "4 MB";
                diffExprDevCompleteFileSize = "32 MB";
                rnaSeqDataFileSize = "32 MB";
                rnaSeqAnnotFileSize = "9 KB";
                affyDataFileSize = "1.2 GB";
                affyAnnotFileSize = "0.5 MB";
                //inSituDataFileSize = "xx MB";
                //inSituAnnotFileSize = "xx MB";
                //estDataFileSize = "xx MB";
                //estAnnotFileSize = "xx MB";
                latinName = "Mus_musculus";
                break;
            case 7955: 
                exprSimpleFileSize = "4.2 MB";
                exprCompleteFileSize = "158 MB"; 
                diffExprAnatSimpleFileSize = "0.02 MB";
                diffExprAnatCompleteFileSize  = "0.2 MB";
                diffExprDevSimpleFileSize = "0,4 MB";
                diffExprDevCompleteFileSize = "1.3 MB";
                affyDataFileSize = "20 MB";
                affyAnnotFileSize = "22 KB";
                //inSituDataFileSize = "xx MB";
                //inSituAnnotFileSize = "xx MB";
                //estDataFileSize = "xx MB";
                //estAnnotFileSize = "xx MB";
                latinName = "Danio_rerio";
                break;
            case 7227: 
                exprSimpleFileSize = "4.7 MB";
                exprCompleteFileSize = "207 MB"; 
                diffExprAnatSimpleFileSize = "0.5 MB";
                diffExprAnatCompleteFileSize  = "1.6 MB";
                diffExprDevSimpleFileSize = "0.2 MB";
                diffExprDevCompleteFileSize = "0.9 MB"; 
                affyDataFileSize = "115 MB";
                affyAnnotFileSize = "69 KB";
                //inSituDataFileSize = "xx MB";
                //inSituAnnotFileSize = "xx MB";
                //estDataFileSize = "xx MB";
                //estAnnotFileSize = "xx MB";
                latinName = "Drosophila_melanogaster";
                break;
            case 6239: 
                exprSimpleFileSize = "1.2 MB";
                exprCompleteFileSize = "13 MB"; 
                diffExprDevSimpleFileSize = "0.2 MB";
                diffExprDevCompleteFileSize = "1.7 MB"; 
                affyDataFileSize = "13 MB";
                affyAnnotFileSize = "4 KB";
                rnaSeqDataFileSize = "11 MB";
                rnaSeqAnnotFileSize = "4 KB";
                //inSituDataFileSize = "xx MB";
                //inSituAnnotFileSize = "xx MB";
                latinName = "Caenorhabditis_elegans";
                break;
            case 9597: 
                exprSimpleFileSize = "0.7 MB";
                exprCompleteFileSize = "33 MB"; 
                rnaSeqDataFileSize = "3 MB";
                rnaSeqAnnotFileSize = "2 KB";
                latinName = "Pan_paniscus";
                break;
            case 9598: 
                exprSimpleFileSize = "0.5 MB";
                exprCompleteFileSize = "29 MB";  
                diffExprAnatSimpleFileSize = "0.2 MB";
                diffExprAnatCompleteFileSize  = "0.9 MB";
                rnaSeqDataFileSize = "3 MB";
                rnaSeqAnnotFileSize = "3 KB";
                latinName = "Pan_troglodytes";
                break;
            case 9593: 
                exprSimpleFileSize = "0.6 MB";
                exprCompleteFileSize = "26 MB"; 
                diffExprAnatSimpleFileSize = "0.2 MB";
                diffExprAnatCompleteFileSize  = "0.9 MB";
                rnaSeqDataFileSize = "3 MB";
                rnaSeqAnnotFileSize = "3 KB";
                latinName = "Gorilla_gorilla";
                break;
            case 9600: 
                latinName = "Pongo_pygmaeus";
                break;
            case 9544: 
                exprSimpleFileSize = "1.2 MB";
                exprCompleteFileSize = "54 MB"; 
                diffExprAnatSimpleFileSize = "0.4 MB";
                diffExprAnatCompleteFileSize  = "2.5 MB"; 
                rnaSeqDataFileSize = "10 MB";
                rnaSeqAnnotFileSize = "5 KB";
                latinName = "Macaca_mulatta";
                break;
            case 10116: 
                exprSimpleFileSize = "0.8 MB";
                exprCompleteFileSize = "27 MB"; 
                diffExprAnatSimpleFileSize = "0.5 MB";
                diffExprAnatCompleteFileSize  = "2 MB"; 
                rnaSeqDataFileSize = "6.2 MB";
                rnaSeqAnnotFileSize = "3 KB";
                latinName = "Rattus_norvegicus";
                break;
            case 9913: 
                exprSimpleFileSize = "0.7 MB";
                exprCompleteFileSize = "33 MB"; 
                diffExprAnatSimpleFileSize = "0.4 MB";
                diffExprAnatCompleteFileSize  = "1.9 MB"; 
                rnaSeqDataFileSize = "6 MB";
                rnaSeqAnnotFileSize = "3 KB";
                latinName = "Bos_taurus";
                break;
            case 9823: 
                exprSimpleFileSize = "0.3 MB";
                exprCompleteFileSize = "2.3 MB";  
                rnaSeqDataFileSize = "0.8 MB";
                rnaSeqAnnotFileSize = "1 KB";
                latinName = "Sus_scrofa";
                break;
            case 13616: 
                exprSimpleFileSize = "0.9 MB";
                exprCompleteFileSize = "25 MB";  
                diffExprAnatSimpleFileSize = "0.2 MB";
                diffExprAnatCompleteFileSize  = "0.5 MB"; 
                rnaSeqDataFileSize = "4 MB";
                rnaSeqAnnotFileSize = "3 KB";
                latinName = "Monodelphis_domestica";
                break;
            case 9258: 
                exprSimpleFileSize = "0.6 MB";
                exprCompleteFileSize = "19 MB"; 
                diffExprAnatSimpleFileSize = "0.2 MB";
                diffExprAnatCompleteFileSize  = "1.2 MB"; 
                rnaSeqDataFileSize = "4 MB";
                rnaSeqAnnotFileSize = "3 KB";
                latinName = "Ornithorhynchus_anatinus";
                break;
            case 9031: 
                exprSimpleFileSize = "1 MB";
                exprCompleteFileSize = "29 MB"; 
                diffExprAnatSimpleFileSize = "0.4 MB";
                diffExprAnatCompleteFileSize  = "1.7 MB"; 
                rnaSeqDataFileSize = "7 MB";
                rnaSeqAnnotFileSize = "5 KB";
                latinName = "Gallus_gallus";
                break;
            case 28377: 
                exprSimpleFileSize = "0.3 MB";
                exprCompleteFileSize = "5.7 MB";  
                rnaSeqDataFileSize = "0.8 MB";
                rnaSeqAnnotFileSize = "2 KB";
                latinName = "Anolis_carolinensis";
                break;
            case 8364: 
                exprSimpleFileSize = "2.6 MB";
                exprCompleteFileSize = "65 MB"; 
                diffExprAnatSimpleFileSize = "0.2 MB";
                diffExprAnatCompleteFileSize  = "1 MB";
                diffExprDevSimpleFileSize = "0.1 MB";
                diffExprDevCompleteFileSize = "0.6 MB";  
                rnaSeqDataFileSize = "12 MB";
                rnaSeqAnnotFileSize = "6 KB";
                //inSituDataFileSize = "xx MB";
                //inSituAnnotFileSize = "xx MB";
                //estDataFileSize = "xx MB";
                //estAnnotFileSize = "xx MB";
                latinName = "Xenopus_tropicalis";
                break;
            case 99883: 
                latinName = "Tetraodon_nigroviridis";
                break;
            default:
                return ("");
        }
        
        
        StringBuffer data = new StringBuffer();
        if (pageType.equals(DownloadPageType.EXPR_CALLS)) {
            String extension = ".tsv.zip";
            // TODO: remove hardcoded "_" in file names. 
            // Use BgeeProperties... or RequestParameters ? or static variables?
            String beginExprFilePath = this.prop.getDownloadExprFilesRootDirectory() + latinName + "_";
            String beginDiffExprFilePath = this.prop.getDownloadDiffExprFilesRootDirectory() + latinName + "_";
            data.append(" data-bgeeexprsimplefileurl='" + beginExprFilePath + "expr-simple" + extension + 
                    "' data-bgeeexprsimplefilesize='" + exprSimpleFileSize + 
                    "' data-bgeeexprcompletefileurl='" + beginExprFilePath + "expr-complete" + extension + 
                    "' data-bgeeexprcompletefilesize='" + exprCompleteFileSize+ "'");
            if (diffExprAnatSimpleFileSize != null) {
                data.append(" data-bgeediffexpranatomysimplefileurl='" + beginDiffExprFilePath + 
                                        "diffexpr-anatomy-simple" + extension +
                        "' data-bgeediffexpranatomysimplefilesize='" + diffExprAnatSimpleFileSize + "'"); 
            }
            if (diffExprAnatCompleteFileSize != null) {
                data.append(" data-bgeediffexpranatomycompletefileurl='" + beginDiffExprFilePath + 
                                        "diffexpr-anatomy-complete" + extension +
                        "' data-bgeediffexpranatomycompletefilesize='" + diffExprAnatCompleteFileSize + "'"); 
            }
            if (diffExprDevSimpleFileSize != null) {
                data.append(" data-bgeediffexprdevelopmentsimplefileurl='" + beginDiffExprFilePath + 
                                        "diffexpr-development-simple" + extension +
                        "' data-bgeediffexprdevelopmentsimplefilesize='" + diffExprDevSimpleFileSize + "'"); 
            }
            if (diffExprDevCompleteFileSize != null) {
                data.append(" data-bgeediffexprdevelopmentcompletefileurl='" + beginDiffExprFilePath + 
                                        "diffexpr-development-complete" + extension +
                        "' data-bgeediffexprdevelopmentcompletefilesize='" + diffExprDevCompleteFileSize + "'"); 
            }

        } else if (pageType.equals(DownloadPageType.PROC_EXPR_VALUES)) {
            String extension = ".zip";
            if (rnaSeqDataFileSize != null) {
                String rnaSeqProcValueDir = this.prop.getDownloadRNASeqProcExprValueFilesRootDirectory()
                        + latinName + "/";
                String filePrefix = latinName + "_RNA-Seq_";
                data.append(" data-bgeernaseqdatafileurl='" + rnaSeqProcValueDir + filePrefix + 
                        "read_counts_RPKM" + extension +
                        "' data-bgeernaseqdatafilesize='" + rnaSeqDataFileSize + "'"); 
                data.append(" data-bgeernaseqannotfileurl='" + rnaSeqProcValueDir + filePrefix + 
                        "experiments_libraries" + extension +
                        "' data-bgeernaseqannotfilesize='" + rnaSeqAnnotFileSize + "'"); 
                data.append(" data-bgeernaseqdatarooturl='" + rnaSeqProcValueDir + "' ");
            }
            if (affyDataFileSize != null) {
                String affyProcValueDir = this.prop.getDownloadAffyProcExprValueFilesRootDirectory()
                        + latinName + "/";
                String filePrefix = latinName + "_Affymetrix_";
                data.append(" data-bgeeaffydatafileurl='" + affyProcValueDir + filePrefix
                        + "probesets" + extension 
                        + "' data-bgeeaffydatafilesize='" + affyDataFileSize + "'"); 
                data.append(" data-bgeeaffyannotfileurl='" + affyProcValueDir + filePrefix 
                        + "experiments_chips" + extension +
                        "' data-bgeeaffyannotfilesize='" + affyAnnotFileSize + "'"); 
                data.append(" data-bgeeaffydatarooturl='" + affyProcValueDir + "' ");
            }
//            if (inSituDataFileSize != null) {
//                data.append(" data-bgeeinsitudatafileurl='" + beginProcValueFilePath + 
//                                        "InSitu_data" + extension +
//                        "' data-bgeeinsitudatafilesize='" + inSituDataFileSize + "'"); 
//                data.append(" data-bgeeinsituannotfileurl='" + beginProcValueFilePath + 
//                        "InSitu_annotations" + extension +
//                        "' data-bgeeinsituannotfilesize='" + inSituAnnotFileSize + "'"); 
//            }
//            if (estDataFileSize != null) {
//                data.append(" data-bgeeestdatafileurl='" + beginProcValueFilePath + 
//                                        "EST_data" + extension +
//                        "' data-bgeeestdatafilesize='" + estDataFileSize + "'"); 
//                data.append(" data-bgeeestannotfileurl='" + beginProcValueFilePath + 
//                        "EST_annotations" + extension +
//                        "' data-bgeeestannotfilesize='" + estAnnotFileSize + "'"); 
//            }
        } else {
            assert false: "Unknown DownloadPageType";
        }
        
        return log.exit(data.toString());
    }

    /**
     * Generate the HTML img tag of one species.
     * 
     * @return             A {@code String} that is the  HTML figure tag generated from the 
     *                     provided {@code List} of species IDs
     * @param id           An {@code int} of the species IDs to be diplayed.
     * @param name         A {@code String} that is the species name.
     * @param commonName   A {@code String} that is the species common name.
     * @param lightImg     A {@code boolean} that is {@code true} if the image to use is 
     *                     the light one.
     * @return             A {@code String} that is the  HTML img tag of the provided species 
     *                     data.
     */
    private String generateSpeciesImg(int id, String name, String shortName, 
            String commonName, String alternateNames, boolean lightImg) {
        log.entry(id, name, shortName, commonName, alternateNames, lightImg);
        StringBuilder image = new StringBuilder();
        image.append("<img class='species_img' src='");
        image.append(this.prop.getSpeciesImagesRootDirectory());
        image.append(id);
        if (lightImg) {
            image.append("_light");
        }
        image.append(".jpg' alt='");
        image.append(name);
        image.append("' data-bgeespeciesid='");
        image.append(id);
        image.append("' data-bgeespeciesname='");
        image.append(name);
        image.append("' data-bgeespeciesshortname='");
        image.append(shortName);
        image.append("' data-bgeespeciescommonname='");
        image.append(commonName);
        image.append("' data-bgeespeciesalternatenames='");
        image.append(alternateNames);
        image.append("' />");
                
        return log.exit(image.toString());
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
  		this.writeln(getDataGroupScriptTag(getAllSpeciesDataGroup()));
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

