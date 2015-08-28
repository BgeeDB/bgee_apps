package org.bgee.view.html;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
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
 * @version Bgee 13, August 2015
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
        this.startDisplay("Welcome to Bgee: a dataBase for Gene Expression Evolution");

        //TODO: manage the version either from database, or from bgee-webapp.properties file.
        this.writeln("<h1>Welcome to Bgee release 13.1</h1>");
        
        this.writeln(this.displayHomePageSpecies());

        this.writeln("<h2>Browse Bgee content</h2>");
        this.writeln("<div class='bgee_section'>");

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
        

        this.writeln("<div class='feature_list'>");
        this.writeln(this.getFeatureDownloadLogos());
        this.writeln(this.getMainDocumentationLogo());
        this.writeln("</div>");

        this.writeln("</div>"); // close Browse Bgee content

        this.writeln("<h2>News</h2>" +
                     "<span class='header_details'>(features are being added incrementally)</span>");
        this.writeln("<div id='bgee_news' class='bgee_section'>");
        this.writeln("<ul>");
        this.writeln("<li>2015-08-26: update of the home page.</li>");
        this.writeln("<li>2015-06-08: release of Bgee release 13.1: "
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
        this.writeln("<div class='feature_list'>");
        this.writeln(HtmlParentDisplay.getSingleFeatureLogo("http://bgee.org/bgee/bgee", 
                true, "Bgee 12 home page", "Bgee 12", 
                this.prop.getLogoImagesRootDirectory() + "bgee12_logo.png", null));
        this.writeln("</div>");

        this.writeln(this.getImageSources());

        this.endDisplay();
        log.exit();
    }

    private String displayHomePageSpecies() {
        log.entry();
        
        // Single species part
        String homePageSpeciesSection = this.getSingleSpeciesSection();

        // Black banner when a species or a group is selected.
        homePageSpeciesSection += this.getDownloadBanner();

        return log.exit(homePageSpeciesSection);
    }

    /**
     * Get the single species section of a download page as a HTML 'div' element,
     * according the provided page type.
     *
     * @return  the {@code String} that is the single species section as HTML 'div' element,
     *          according {@code pageType}.
     */
    // TODO: DRY: copy from HtmlDownloadDisplay  
    private String getSingleSpeciesSection() {
        log.entry();

        StringBuffer s = new StringBuffer(); 
        s.append("<div id='bgee_uniq_species'> ");
        s.append("<h2>Species with data in Bgee</h2>");
        s.append("<span class='header_details'>(click on species to see more details)</span>");
        s.append("<div class='bgee_section bgee_download_section'>");
        s.append(generateSpeciesFigure(9606));
        s.append(generateSpeciesFigure(10090));
        s.append(generateSpeciesFigure(7955));
        s.append(generateSpeciesFigure(7227));
        s.append(generateSpeciesFigure(6239));
        s.append(generateSpeciesFigure(9597));
        s.append(generateSpeciesFigure(9598));
        s.append(generateSpeciesFigure(9593));
//        s.append(generateSpeciesFigure(9600)); // no more data for Pongo pygmaeus
        s.append(generateSpeciesFigure(9544));
        s.append(generateSpeciesFigure(10116));
        s.append(generateSpeciesFigure(9913));
        s.append(generateSpeciesFigure(9823));
        s.append(generateSpeciesFigure(13616));
        s.append(generateSpeciesFigure(9258));
        s.append(generateSpeciesFigure(9031));
        s.append(generateSpeciesFigure(28377));
        s.append(generateSpeciesFigure(8364));
//        s.append(generateSpeciesFigure(99883)); // no more data for Tetraodon nigroviridis
        s.append("</div>");
        s.append("</div>");
        
        return log.exit(s.toString());
    }

    /**
     * Generate the HTML figure tag with a figcaption tag from a {@code int} that is a 
     * species ID.
     * 
     * @param speciesId An {@code int} that is the species ID of the species to be displayed.
     * @return          A {@code String} that is the  HTML figure tag generated from the 
     *                  provided {@code int} of a species ID.
     */
    // TODO: DRY: copy from HtmlDownloadDisplay  
    private String generateSpeciesFigure(int speciesId) {
        log.entry(speciesId);
        return log.exit(this.generateSpeciesFigure(Arrays.asList(speciesId), null));
    }

    /**
     * Generate the HTML figure tag from a {@code List} of species IDs.
     * 
     * @param speciesIds   A {@code List} of {@code Integer} containing the species IDs to
     *                     be diplayed.
     * @param figcaption   A {@code String} that is the fieldcaption of the figure. If empty 
     *                     or {@code null}, it's generated with the last species of the 
     *                     {@code List}.
     * @return             A {@code String} that is the  HTML figure tag generated from the 
     *                     provided {@code List} of species IDs.
     */
    // TODO: DRY: copy from HtmlDownloadDisplay  
    private String generateSpeciesFigure(List<Integer> speciesIds, String figcaption) {
        log.entry(speciesIds, figcaption);
        
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
            
            hiddenInfo = name;
            
            images.append(this.generateSpeciesImg(
                    speciesId, name, shortName, commonName, alternateNames, true));
        }
        if (StringUtils.isBlank(figcaption)) {
            // If empty or null, it's generated with the last species ID of the given List. 
            figcaption = "<p><i>" + shortName + "</i></p><p>" + commonName + "</p>";   
        }

        String figure = "<figure " + this.getSingleSpeciesFileData(speciesIds.get(0)) + ">";

        figure += "<div>" + images + "</div>" + 
                  "<figcaption>" + figcaption + 
                  " <span class='invisible'>" + hiddenInfo + "</span>" + 
                  "</figcaption>" + 
                  "</figure>";
        return log.exit(figure);
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
    // TODO: DRY: exact copy from HtmlDownloadDisplay  
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

    /**
     * Get custom data for a single species.
     * 
     * @param speciesId A {@code String} that is the ID of the species.
     * @return          A {@code String} that is data according to the given species ID.
     */
    // TODO: DRY: copy from HtmlDownloadDisplay  
    private String getSingleSpeciesFileData(int speciesId) {
        log.entry(speciesId);
        
        String  rnaSeqDataFileSize = null, affyDataFileSize = null,
                rnaSeqAnnotFileSize = null, affyAnnotFileSize = null, 
                latinName = null;

        switch (speciesId) {
            case 9606: 
                rnaSeqDataFileSize = "32 MB";
                rnaSeqAnnotFileSize = "6 KB";
                affyDataFileSize = "1.4 GB";
                affyAnnotFileSize = "0.3 MB";
                //estDataFileSize = "xx MB";
                //estAnnotFileSize = "xx MB";
                latinName = "Homo_sapiens";
                break;
            case 10090: 
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
                affyDataFileSize = "20 MB";
                affyAnnotFileSize = "22 KB";
                //inSituDataFileSize = "xx MB";
                //inSituAnnotFileSize = "xx MB";
                //estDataFileSize = "xx MB";
                //estAnnotFileSize = "xx MB";
                latinName = "Danio_rerio";
                break;
            case 7227: 
                affyDataFileSize = "115 MB";
                affyAnnotFileSize = "69 KB";
                //inSituDataFileSize = "xx MB";
                //inSituAnnotFileSize = "xx MB";
                //estDataFileSize = "xx MB";
                //estAnnotFileSize = "xx MB";
                latinName = "Drosophila_melanogaster";
                break;
            case 6239: 
                affyDataFileSize = "13 MB";
                affyAnnotFileSize = "4 KB";
                rnaSeqDataFileSize = "11 MB";
                rnaSeqAnnotFileSize = "4 KB";
                //inSituDataFileSize = "xx MB";
                //inSituAnnotFileSize = "xx MB";
                latinName = "Caenorhabditis_elegans";
                break;
            case 9597: 
                rnaSeqDataFileSize = "3 MB";
                rnaSeqAnnotFileSize = "2 KB";
                latinName = "Pan_paniscus";
                break;
            case 9598: 
                rnaSeqDataFileSize = "3 MB";
                rnaSeqAnnotFileSize = "3 KB";
                latinName = "Pan_troglodytes";
                break;
            case 9593: 
                rnaSeqDataFileSize = "3 MB";
                rnaSeqAnnotFileSize = "3 KB";
                latinName = "Gorilla_gorilla";
                break;
            case 9600: 
                latinName = "Pongo_pygmaeus";
                break;
            case 9544: 
                rnaSeqDataFileSize = "10 MB";
                rnaSeqAnnotFileSize = "5 KB";
                latinName = "Macaca_mulatta";
                break;
            case 10116: 
                rnaSeqDataFileSize = "6.2 MB";
                rnaSeqAnnotFileSize = "3 KB";
                latinName = "Rattus_norvegicus";
                break;
            case 9913: 
                rnaSeqDataFileSize = "6 MB";
                rnaSeqAnnotFileSize = "3 KB";
                latinName = "Bos_taurus";
                break;
            case 9823: 
                rnaSeqDataFileSize = "0.8 MB";
                rnaSeqAnnotFileSize = "1 KB";
                latinName = "Sus_scrofa";
                break;
            case 13616: 
                rnaSeqDataFileSize = "4 MB";
                rnaSeqAnnotFileSize = "3 KB";
                latinName = "Monodelphis_domestica";
                break;
            case 9258: 
                rnaSeqDataFileSize = "4 MB";
                rnaSeqAnnotFileSize = "3 KB";
                latinName = "Ornithorhynchus_anatinus";
                break;
            case 9031: 
                rnaSeqDataFileSize = "7 MB";
                rnaSeqAnnotFileSize = "5 KB";
                latinName = "Gallus_gallus";
                break;
            case 28377: 
                rnaSeqDataFileSize = "0.8 MB";
                rnaSeqAnnotFileSize = "2 KB";
                latinName = "Anolis_carolinensis";
                break;
            case 8364: 
                rnaSeqDataFileSize = "12 MB";
                rnaSeqAnnotFileSize = "6 KB";
                latinName = "Xenopus_tropicalis";
                break;
            case 99883: 
                latinName = "Tetraodon_nigroviridis";
                break;
            default:
                return ("");
        }
        
        
        StringBuffer data = new StringBuffer();
        String extension = ".tsv.zip";
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


        return log.exit(data.toString());
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
    
        StringBuffer banner = new StringBuffer();
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
     * Get the images sources of a download page as a HTML 'div' element. 
     *
     * @return  the {@code String} that is the images sources as HTML 'div' element.
     */
    //XXX: DRY: exact copy from HtmlDownloadDisplay 
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
        this.includeJs("general.js");
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
