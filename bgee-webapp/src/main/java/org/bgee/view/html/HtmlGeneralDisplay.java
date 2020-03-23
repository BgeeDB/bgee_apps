package org.bgee.view.html;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.BgeeProperties;
import org.bgee.controller.RequestParameters;
import org.bgee.model.file.SpeciesDataGroup;
import org.bgee.model.species.Species;
import org.bgee.view.GeneralDisplay;
import org.bgee.view.html.HtmlDownloadDisplay.DownloadPageType;

import static org.bgee.model.file.DownloadFile.CategoryEnum.AFFY_DATA;
import static org.bgee.model.file.DownloadFile.CategoryEnum.EXPR_CALLS_COMPLETE;
import static org.bgee.model.file.DownloadFile.CategoryEnum.RNASEQ_DATA;

/**
 * HTML View for the general category display
 * 
 * @author  Mathieu Seppey
 * @author  Frederic Bastian
 * @author  Valentine Rech de Laval
 * @author  Philippe Moret
 * @version Bgee 14, July 2019
 * @since   Bgee 13, July 2014
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

        this.addSchemaMarkups(groups);
        
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
     * Add schema.org markup to the page.
     */
    private void addSchemaMarkups(List<SpeciesDataGroup> groups) {
        log.entry(groups);
        
        // We build the RequestParameters without geneId to keep '{' and '}' instead of replace them due to by secureString() 
        RequestParameters urlActionTarget = this.getNewRequestParameters();
        urlActionTarget.setPage(RequestParameters.PAGE_GENE);
        RequestParameters urlDownloadCalls = this.getNewRequestParameters();
        urlDownloadCalls.setPage(RequestParameters.PAGE_DOWNLOAD);
        urlDownloadCalls.setAction(RequestParameters.ACTION_DOWLOAD_CALL_FILES);
        RequestParameters urlDownloadProcValues = this.getNewRequestParameters();
        urlDownloadProcValues.setPage(RequestParameters.PAGE_DOWNLOAD);
        urlDownloadProcValues.setAction(RequestParameters.ACTION_DOWLOAD_PROC_VALUE_FILES);
        
        String datasets = groups.stream()
                .filter(SpeciesDataGroup::isSingleSpecies)
                .map(sdg -> {
                    Integer spId = sdg.getMembers().get(0).getId();
                    return "{" +
                            "    \"@type\": \"Dataset\"," +
                            "    \"@id\": \"" + this.getDatasetSchemaId(spId, EXPR_CALLS_COMPLETE) + "\"," +
                            "    \"name\": \"" + this.getDatasetSchemaName(spId, EXPR_CALLS_COMPLETE) + "\"," +
                            "    \"description\": \"" + this.getDatasetSchemaDescription(spId, EXPR_CALLS_COMPLETE) + "\"," +
                            "    \"license\": \"" + LICENCE_CC0_URL + "\"," +
                            "    \"sameAs\": \"" + this.getSpeciesPageUrl(spId) + "\"" +
                            "}, " +
                            "{" +
                            "    \"@type\": \"Dataset\"," +
                            "    \"@id\": \"" + this.getDatasetSchemaId(spId, RNASEQ_DATA) + "\"," +
                            "    \"name\": \"" + this.getDatasetSchemaName(spId, RNASEQ_DATA) + "\"," +
                            "    \"description\": \"" + this.getDatasetSchemaDescription(spId, RNASEQ_DATA) + "\"," +
                            "    \"license\": \"" + LICENCE_CC0_URL + "\"," +
                            "    \"sameAs\": \"" + this.getSpeciesPageUrl(spId) + "\"" +
                            "}," +
                            "{" +
                            "    \"@type\": \"Dataset\"," +
                            "    \"@id\": \"" + this.getDatasetSchemaId(spId, AFFY_DATA) + "\"," +
                            "    \"name\": \"" + this.getDatasetSchemaName(spId, AFFY_DATA) + "\"," +
                            "    \"description\": \"" + this.getDatasetSchemaDescription(spId, AFFY_DATA) + "\"," +
                            "    \"license\": \"" + LICENCE_CC0_URL + "\"," +
                            "    \"sameAs\": \"" + this.getSpeciesPageUrl(spId) + "\"" +
                            "}";
                        }
                )
                .collect(Collectors.joining(","));

        String sibSchema =
                "{" +
                "    \"@type\": \"Organization\"," +
                "    \"name\": \"SIB Swiss Institute of Bioinformatics\"," +
                "    \"sameAs\": [ \"https://www.sib.swiss\", " +
                        "\"https://fr.wikipedia.org/wiki/Institut_suisse_de_bioinformatique\" ]" +
                "}";

        String unilSchema =
                "{" +
                "    \"@type\": \"CollegeOrUniversity\"," +
                "    \"name\": \"UNIL University of Lausanne\"," +
                "    \"sameAs\": [ \"https://unil.ch/\", " +
                        "\"https://fr.wikipedia.org/wiki/Universit%C3%A9_de_Lausanne\" ]" +
                "}";

        String ebGroupSchema =
                "{" +
                "    \"@type\": \"EducationalOrganization\"," +
                "    \"name\": \"Evolutionary Bioinformatics group\"," +
                "    \"sameAs\": \"https://www.unil.ch/dee/robinson-rechavi-group\"" +
                "}";
        
        this.writeln(getSchemaMarkupGraph(Arrays.asList(
                "    {" +
                "      \"@type\": \"Organization\"," +
                "      \"name\": \"Bgee - Bring Gene Expression Expertise\"," +
                "      \"url\": \"" + this.prop.getBgeeRootDirectory() + "\"," +
                "      \"description\": \"The aim of Bgee is to help biologists to use or understand gene expression\"," +
                "      \"logo\": \""+ this.prop.getBgeeRootDirectory() + this.prop.getLogoImagesRootDirectory() + "bgee13_hp_logo.png\"," +
                "      \"sameAs\": [ \"https://twitter.com/Bgeedb\", \"https://bgeedb.wordpress.com/\"]," +
                "      \"parentOrganization\": [" + sibSchema + "," + unilSchema + "," + ebGroupSchema + "]" +
                "    }",
                "    {" +
                "      \"@type\": \"DataCatalog\"," +
                "      \"@id\": \"" + this.prop.getBgeeRootDirectory() + "\"," +
                "      \"url\": \"" + this.prop.getBgeeRootDirectory() + "\"," +
                "      \"name\": \"Bgee gene expression data\"," +
                "      \"description\": \"" + BGEE_DESCRIPTION + "\"," +
                "      \"keywords\": \"" + BGEE_KEYWORDS + "\"," +
                "      \"creator\": [" + ebGroupSchema + "]," +
                "      \"provider\": [" + sibSchema + "," + unilSchema + "," + ebGroupSchema + "]," +
                "      \"license\": \"" + LICENCE_CC0_URL + "\"," +
                "      \"version\": \"" + this.getWebAppVersion() + "\"," +
                "      \"potentialAction\": {" +
                "        \"@type\": \"SearchAction\"," +
                "        \"target\": \"" + urlActionTarget.getRequestURL() + "&" + 
                            urlActionTarget.getUrlParametersInstance().getParamQuery() +"={query}\"," +
                "        \"query-input\": \"required name=query\"" +
                "      }," +
                "      \"dataset\": [" + datasets + "]" +
                "    }")));

        log.exit();
    }

    /**
     * Display the banner with species (without any interaction).
     * 
     * @param groups    A {@code List} of {@code SpeciesDataGroup} for which display the image.
     */
    private void displaySpeciesBanner(List<SpeciesDataGroup> groups) {
        log.entry(groups);
    
        StringBuilder homePageSpeciesSection = new StringBuilder();
        
        groups.stream().filter(sdg -> sdg.isSingleSpecies()).forEach(sdg -> {
            Species species = sdg.getMembers().get(0);
            Map<String,String> attrs = new HashMap<>();
            attrs.put("src", this.getSpeciesImageSrc(species, true));
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
     * @param divId    A {@code String} that is the ID of HTML 'div' element.
     */
    private void displayBgeeButtons(String divId) {
        log.entry(divId);
        
        RequestParameters urlTopAnat = this.getNewRequestParameters();
        urlTopAnat.setPage(RequestParameters.PAGE_TOP_ANAT);
        RequestParameters urlGeneSearch = this.getNewRequestParameters();
        urlGeneSearch.setPage(RequestParameters.PAGE_GENE);
        RequestParameters urlExprComp = this.getNewRequestParameters();
        urlExprComp.setPage(RequestParameters.PAGE_EXPR_COMPARISON);
        
        this.writeln("<div id='" + divId + "'>");
        this.writeln("<a href='"+ urlExprComp.getRequestURL() +
                "'><span class='glyphicon glyphicon-th-list'></span>Expression comparison</a>");
        this.writeln("<a href='"+ urlTopAnat.getRequestURL() + 
                "'><span class='glyphicon glyphicon-stats'></span>Expression enrichment analysis</a>");
        this.writeln("<a href='"+ urlGeneSearch.getRequestURL() + 
                "'><span class='glyphicon glyphicon-search'></span>Gene search</a>");
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
                + "(RNA-Seq, Affymetrix, <em>in situ</em> hybridization, and EST data) "
                + "and from multiple data sets (including <a href='https://www.gtexportal.org/home/'" +
                " title='GTEx portal' target='_blank' rel='noopener'>GTEx data</a>).</p>");
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
     * @param groups    A {@code List} of {@code SpeciesDataGroup} for which display the image.
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
        banner.append("<img class='closing_cross' src='")
                .append(this.prop.getBgeeRootDirectory()).append(this.prop.getImagesRootDirectory())
                .append("cross.png' title='Close banner' alt='Cross' />");
        banner.append("</div>");

        // Section on the right of the black banner
        banner.append("<h1 class='col-xs-12 col-lg-3'>"
                + "<span class='scientificname'></span>"
                + "<span class='commonname'></span></h1>");

        RequestParameters urlProcExprValues = this.getNewRequestParameters();
        urlProcExprValues.setPage(RequestParameters.PAGE_DOWNLOAD);
        urlProcExprValues.setAction(RequestParameters.ACTION_DOWLOAD_PROC_VALUE_FILES);
        RequestParameters urlGeneExprCalls = this.getNewRequestParameters();
        urlGeneExprCalls.setPage(RequestParameters.PAGE_DOWNLOAD);
        urlGeneExprCalls.setAction(RequestParameters.ACTION_DOWLOAD_CALL_FILES);
        RequestParameters urlSpeciesPage = this.getNewRequestParameters();
        urlSpeciesPage.setPage(RequestParameters.PAGE_SPECIES);

        banner.append("<ul class='col-xs-12 col-lg-9 row'>");
        banner.append("<li class='col-xs-12 col-md-4'><img class='bullet_point' src='")
                .append(this.prop.getBgeeRootDirectory()).append(this.prop.getImagesRootDirectory())
                .append("arrow.png' alt='Arrow' /><a id='processed_expression_values_link' class='data_page_link' href='")
                .append(urlProcExprValues.getRequestURL()).append("' title='Bgee processed expression values'>")
                .append("See RNA-Seq and Affymetrix data</a></li>");
        banner.append("<li class='col-xs-12 col-md-4'><img class='bullet_point' src='")
                .append(this.prop.getBgeeRootDirectory()).append(this.prop.getImagesRootDirectory())
                .append("arrow.png' alt='Arrow' /><a id='gene_expression_calls_link' class='data_page_link' href='")
                .append(urlGeneExprCalls.getRequestURL())
                .append("' title='Bgee gene expression calls'>See gene expression calls</a></li>");
        banner.append("<li class='col-xs-12 col-md-4'><img class='bullet_point' src='")
                .append(this.prop.getBgeeRootDirectory()).append(this.prop.getImagesRootDirectory())
                .append("arrow.png' alt='Arrow' /><a id='species_info_link' class='data_page_link' href='")
                .append(urlSpeciesPage.getRequestURL())
                .append("' title='Bgee species information'>See species information</a></li>");
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

        RequestParameters urlPrivatePolicyPage = this.getNewRequestParameters();
        urlPrivatePolicyPage.setPage(RequestParameters.PAGE_PRIVACY_POLICY);

        RequestParameters urlFaqPage = this.getNewRequestParameters();
        urlFaqPage.setPage(RequestParameters.PAGE_DOCUMENTATION);
        urlFaqPage.setAction(RequestParameters.ACTION_DOC_FAQ);

        RequestParameters urlDatasetPage = this.getNewRequestParameters();
        urlDatasetPage.setPage(RequestParameters.PAGE_DOCUMENTATION);
        urlDatasetPage.setAction(RequestParameters.ACTION_DOC_DATA_SETS);

        RequestParameters urlGeneSearchHbb = this.getNewRequestParameters();
        urlGeneSearchHbb.setPage(RequestParameters.PAGE_GENE);
        urlGeneSearchHbb.setQuery("HBB");

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

        RequestParameters urlMySQLDumps = this.getNewRequestParameters();
        urlMySQLDumps.setPage(RequestParameters.PAGE_DOWNLOAD);
        urlMySQLDumps.setAction(RequestParameters.ACTION_DOWNLOAD_MYSQL_DUMPS);
        
        RequestParameters urlSparql = this.getNewRequestParameters();
        urlSparql.setPage(RequestParameters.PAGE_SPARQL);

        RequestParameters urlCollaborations = this.getNewRequestParameters();
        urlCollaborations.setPage(RequestParameters.PAGE_COLLABORATIONS);

        RequestParameters urlExprComp = this.getNewRequestParameters();
        urlExprComp.setPage(RequestParameters.PAGE_EXPR_COMPARISON);
        
        RequestParameters urlSpecies = this.getNewRequestParameters();
        urlSpecies.setPage(RequestParameters.PAGE_SPECIES);

        RequestParameters urlAnatSim = this.getNewRequestParameters();
        urlAnatSim.setPage(RequestParameters.PAGE_ANAT_SIM);

        this.writeln("<div id='bgee_news' class='panel panel-default'>");
        this.writeln("<div class='panel-heading'>");
        this.writeln("<span class='panel-title'>News"
                     + "    <span class='header_details'>(features are being added incrementally)"
                     + "</span></span>");
        this.writeln("</div>"); // close panel-heading
        
        this.writeln("<div class='panel-body'>");

        this.writeOneNews("2020-03-26", "Release of Bgee version 14.1"
                + "<p>this is an incremental update of Bgee, with an updated RNA-Seq dataset, "
                + "using the same genomes and ontologies as for the previous version Bgee 14.0 "
                + "(genomes and ontologies are updated for major releases of Bgee). "
                + "New RNA-Seq libraries have been added; some libraries previously integrated in Bgee "
                + "have been discarded following corrections and improvements of quality controls. "
                + "The Affymetrix, <i>in situ</i> hybridization, and EST datasets, are the same as "
                + "for the previous release of Bgee 14.0. "
                + "All calls of presence/absence of expression, expression scores, "
                + "and all the Bgee tools and download files, have been updated accordingly.</p>"
                + "<p>For most species in Bgee, notably non-human primates and farm and domestic animals, "
                + "this represents a major improvement of the anatomy and life stage coverage of the data, "
                + "making all Bgee tools much more useful, notably <a href='" + urlTopAnat.getRequestURL()
                + "' title='Perform gene expression enrichment tests with TopAnat'>"
                + "TopAnat</a> and the <a href='" + urlGenePage.getRequestURL() + "' title='Gene page'>"
                + "gene pages</a>.</p>"
                + "<ul>"
                + "  <li>For human, 663 RNA-Seq libraries added, 13 removed, for a total of 5,676 libraries in Bgee 14.1; "
                + "  there is now in total 4,766 conditions annotated in Bgee, in 334 anatomical entities.</li>"
                + "  <li>For model organisms: "
                + "    <ul>"
                + "      <li>197 libraries added for mouse (total 330 libraries); "
                + "      there is now in total 14,720 conditions annotated in Bgee, "
                + "      in 3,275 anatomical entities.</li>"
                + "      <li>193 added for <i>Xenopus tropicalis</i> (total 259); "
                + "      4,921 conditions annotated in Bgee, in 395 anatomical entities.</li>"
                + "      <li>239 added for <i>Drosophila melanogaster</i> (total 253); "
                + "      6,011 conditions annotated in Bgee, in 1,138 anatomical entities.</li>"
                + "      <li>80 added for <i>Danio rerio</i> (total 147); "
                + "      7,408 conditions annotated in Bgee, in 1,292 anatomical entities.</li>"
                + "      <li>9 libraries removed for <i>Caenorhabditis elegans</i> (total 41), "
                + "      following improvements of quality controls; "
                + "      1,220 conditions annotated in Bgee, in 360 anatomical entities.</li>"
                + "    </ul>"
                + "  </li>"
                + "  <li>Addition of data also in non-human primates: "
                + "    <ul>"
                + "      <li>235 libraries added for <i>Pan troglodytes</i> (total 250 libraries); "
                + "      there is now in total 168 conditions annotated in Bgee, "
                + "      in 26 anatomical entities.</li>"
                + "      <li>196 libraries added, 48 removed for <i>Macaca mulatta</i> (total 238); "
                + "      114 conditions annotated in Bgee, in 25 anatomical entities.</li>"
                + "      <li>1 library added for <i>Pan paniscus</i> (total 13); "
                + "      25 conditions annotated in Bgee, in 7 anatomical entities.</li>"
                + "    </ul>"
                + "  </li>"
                + "  <li>For farm and domestic animals:"
                + "    <ul>"
                + "      <li>224 libraries added for <i>Equus caballus</i> (total 232); "
                + "      there is now in total 71 conditions annotated in Bgee, "
                + "      in 24 anatomical entities.</li>"
                + "      <li>159 added for <i>Sus scrofa</i> (total 169); "
                + "      103 conditions annotated in Bgee, in 41 anatomical entities.</li>"
                + "      <li>135 added for <i>Canis lupus familiaris</i> (total 141); "
                + "      150 conditions annotated in Bgee, in 54 anatomical entities.</li>"
                + "      <li>88 added for <i>Bos taurus</i> (total 121); "
                + "      36 conditions annotated in Bgee, in 19 anatomical entities.</li>"
                + "      <li>49 added for <i>Oryctolagus cuniculus</i> (total 55); "
                + "      44 conditions annotated in Bgee, in 19 anatomical entities.</li>"
                + "      <li>3 added for <i>Gallus gallus</i> (total 48); "
                + "      48 conditions annotated in Bgee, in 14 anatomical entities.</li>"
                + "      <li>23 added for <i>Felis catus</i> (total 32); "
                + "      20 conditions annotated in Bgee, in 11 anatomical entities.</li>"
                + "      <li>19 added for <i>Cavia porcellus</i> (total 28); "
                + "      9 conditions annotated in Bgee, in 5 anatomical entities.</li>"
                + "    </ul>"
                + "  </li>"
                + "  <li>Other species:"
                + "    <ul>"
                + "      <li>89 libraries added for <i>Monodelphis domestica</i> (total 108); "
                + "      there is now in total 100 conditions annotated in Bgee, "
                + "      in 24 anatomical entities.</li>"
                + "      <li>70 added for <i>Rattus norvegicus</i> (total 106); "
                + "      110 conditions annotated in Bgee, in 22 anatomical entities.</li>"
                + "      <li>27 added for <i>Anolis carolinensis</i> (total 31); "
                + "      40 conditions annotated in Bgee, in 15 anatomical entities.</li>"
                + "      <li>4 added for <i>Ornithorhynchus anatinus</i> (total 21); "
                + "      32 conditions annotated in Bgee, in 9 anatomical entities.</li>"
                + "      <li>2 added, 4 removed for <i>Drosophila simulans</i> (total 15); "
                + "      8 conditions annotated in Bgee, in 4 anatomical entities.</li>"
                + "      <li>4 removed for <i>Drosophila pseudoobscura</i> (total 10); "
                + "      6 conditions annotated in Bgee, in 4 anatomical entities.</li>"
                + "    </ul>"
                + "  </li>"
                + "</ul>"
                + "<p>You can still access to Bgee version 14.0 at <a title='Archive site Bgee version 14' "
                + "href='" + this.prop.getBgeeRootDirectory() + "bgee14' target='_blank' rel='noopener'>"
                + this.prop.getBgeeRootDirectory() + "bgee14</a>.</p>");

        this.writeOneNews("2019-10-05",
                "<ul>" +
                "    <li>New score on <a href='" + urlGenePage.getRequestURL() + "' title='Gene page'>" + 
                         "gene pages</a>.</li>" +
                "    <li>New <a href='" + urlSpecies.getRequestURL() + "' title='Species'>" +
                         "species</a> page.</li>" +
                "    <li>Update of the <a href='" + urlExprComp.getRequestURL() + "' title='Expression comparison'>" +
                         "expression comparison</a>." +
                "    <ul>" + 
                "        <li>Better sorting approach.</li>" +
                "        <li>Possibility to export results (copy to clipboard or TSV).</li>" + 
                "    </ul></li>" +   
                "    <li>Improve findability of data by adding <a href='https://bioschemas.org' " + 
                "    title='Bioschemas'>Bioschemas</a>  markup.</li>" +
                "</ul>");
        
        this.writeOneNews("2019-05-21",
                "<ul>" +
                "    <li>New <a href='" + urlExprComp.getRequestURL() + "' title='Expression comparison'>" +
                        "expression comparison</a> page.</li>" +
                "    <li>New <a href='" + urlAnatSim.getRequestURL() + "' title='Anatomical homology'>" +
                        "anatomical homology</a> page.</li>" +
                "    <li>New resource pages: " +
                        "<a href='" + urlResourcesRPackages.getRequestURL() + "' title='R packages'>" + 
                        R_PACKAGES_PAGE_NAME + "</a>, " +
                        "<a href='" + urlResourcesAnnotations.getRequestURL() + "' title='Annotations'>" + 
                        ANNOTATIONS_PAGE_NAME + "</a>, " +
                        "<a href='" + urlResourcesOntologies.getRequestURL() + "' title='Ontologies'>" + 
                        ONTOLOGIES_PAGE_NAME + "</a> and " +
                        "<a href='" + urlResourcesSourceCode.getRequestURL() + "' title='Source code'>" + 
                        SOURCE_CODE_PAGE_NAME + "</a> pages.</li>" +
                "    <li>New <a href='" + urlMySQLDumps.getRequestURL() + "' title='MySQL dumps'>" + 
                        MYSQL_DUMPS_PAGE_NAME + "</a> and <a href='" + urlSparql.getRequestURL() + 
                        "' title='Bgee SPARQL endpoint'>SPARQL endpoint</a> pages.</li>" +
                "    <li>New <a href='" + urlCollaborations.getRequestURL() + 
                        "' title='Bgee collaborations'>Bgee collaborations</a> page.</li>" +
                "    <li>Update of the menu</li>" +
                "</ul>");

        this.writeOneNews("2019-05-12",
                "<ul>" +
                "    <li>Update of the <a href='" + urlGenePage.getRequestURL() + "'>gene search page</a>:" +
                "    <ul>" +
                "        <li>Addition of a gene search result page (i.e. <a href='" +
                         urlGeneSearchHbb.getRequestURL() + "' title='Search genes with \"HBB\"'>search with \"HBB\"</a>)</li>" +
                "        <li>Improvement of the speed of autocompletion</li>" +
                "    </ul></li>" +
                "    <li>Modification of gene pages to display gene name synoyms," +
                "    and cross-references to other resources</li>" +
                "</ul>");

        this.writeOneNews("2019-04-05",
                  "<ul>"
                + "  <li>New <a href='" + urlPrivatePolicyPage.getRequestURL() 
                                + "'>privacy policy page</a></li>"
                + "  <li>New <a href='" + urlFaqPage.getRequestURL() + "'>FAQ page</a> "
                + "                where we address common user queries</li>"
                + "  <li>New <a href='" + urlDatasetPage.getRequestURL() + "'>documentation page</a>"
                + "             specific to GTEx project to learn how we integrated these data" 
                + "             into Bgee</li>"
                + "  <li>Update to Bgee 14.0 of the <a href='" + urlCallDoc.getRequestURL()+ "'>gene expression call documentation</a>"
                + "             </li>"
                + "  <li>Update of the <a href='" + urlSourcePage.getRequestURL()+ "'>data source page</a>"
                + "             to provide version information</li>"
                + "  <li>We have clarified our license; we have chosen CC0.</li>"
                + "  <li>Update of the menu</li>"
                + "</ul>");
                
        this.writeOneNews("2018-02-14", "Release of Bgee version 14.0:"
                + "<ul>"
                + "  <li>Release of the production version of Bgee release 14:"
                + "    <ul>"
                + "      <li><a href='" + urlTopAnat.getRequestURL()
                +            "' title='Perform gene expression enrichment tests with TopAnat'>"
                +            "TopAnat</a> can now be used based on Bgee 14 data.</li>"
                + "      <li><a href='" + urlGenePage.getRequestURL()
                +            "' title='Search expression call for a gene'>Gene expression calls</a> "
                +            "should now be properly retrieved for all genes.</li>"
                + "    </ul>"
                + "  </li>"
                + "</ul>"
                + "You can still access to Bgee 13 at <a title='Archive site Bgee version 13' "
                + "href='" + this.prop.getBgeeRootDirectory() + "bgee13' target='_blank' rel='noopener'>"
                + this.prop.getBgeeRootDirectory() + "bgee13</a>.");

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
                + "href='" + this.prop.getBgeeRootDirectory() + "bgee13' target='_blank' rel='noopener'>"
                + this.prop.getBgeeRootDirectory() + "bgee13</a>.");

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
                + "<a href='https://bioconductor.org/packages/release/bioc/html/BgeeDB.html' class='external_link' target='_blank' rel='noopener'>"
                + "BgeeDB R package</a>, a package for the annotation and gene expression "
                + "data download from Bgee database into R, and TopAnat analysis (see also "
                + "<a href='https://bioconductor.org/packages/release/bioc/html/BgeeDB.html' "
                + "class='external_link' target='_blank' rel='noopener'>Bioconductor website</a>).");

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
                          urlDownload.getRequestURL() + "' " + "title='Download overview'>" +
                          "download overview</a>.");
        this.writeOneNews("2015-03-03", "Release of the single-species " +
                          "differential expression data for 11 species, see <a href='" +
                          urlDownload.getRequestURL() + "' " + "title='Download overview'>" +
                          "download overview</a>.");
        this.writeOneNews("2014-12-19", "Release of the single-species " +
                          "expression data for 17 species, see <a href='" +
                          urlDownload.getRequestURL() + "' " + "title='Download overview'>" +
                          "download overview</a>.");
        
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
        
        this.writeln("<div class='col-xs-12 col-lg-9'>");
        this.writeln(getImageSources());
        this.writeln("</div>");

        this.writeln("<div class='col-xs-12 col-lg-3 archive_site'>");
        this.writeln("View archive sites:");
        this.writeln("<a title='Archive site Bgee version 14.0' href='" +
                this.prop.getBgeeRootDirectory() + "bgee14' target='_blank' rel='noopener'>version 14.0</a>");
        this.writeln("<a title='Archive site Bgee version 13' href='" +
                this.prop.getBgeeRootDirectory() + "bgee13' target='_blank' rel='noopener'>version 13</a>");
        this.writeln("<a title='Archive site Bgee version 12' href='" +
                this.prop.getBgeeRootDirectory() + "bgee12' target='_blank' rel='noopener'>version 12</a>");
        this.writeln("</div>");
        
        this.writeln("</div>"); // close bgee_more_info row
        
        log.exit();
    }

    /**
     * Display an unique news.
     * 
     * @param date          A {@code String} that is the date of the news. 
     * @param description   A {@code String} that is the description of the news.
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
