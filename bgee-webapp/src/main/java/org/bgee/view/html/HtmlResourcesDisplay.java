package org.bgee.view.html;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.BgeeProperties;
import org.bgee.controller.RequestParameters;
import org.bgee.view.ResourcesDisplay;

/**
 * This class displays the resources for the HTML view.
 *
 * @author  Julien Wollbrett
 * @version Bgee 14, May 2019
 * @since   Bgee 14, May 2019
 */
public class HtmlResourcesDisplay extends HtmlParentDisplay implements ResourcesDisplay {

    private final static Logger log = LogManager.getLogger(HtmlResourcesDisplay.class.getName());
    
    private final static String BGEECALL_DESCRIPTION = "Generate present/absent gene expression calls "
            + "for your own RNA-Seq libraries as long as the species are present in Bgee. "
            + "BgeeCall uses reference intergenic regions to define a threshold of presence of "
            + "expression specific to your RNA-Seq library.";
    
    private final static String BGEEDB_DESCRIPTION = "Retrieve annotations, quantitative data and "
            + "expression calls produced by the Bgee pipeline. Run GO-like enrichment "
            + "analyses based on anatomical terms, where genes are mapped to anatomical "
            + "terms by expression patterns.";
    
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
    public HtmlResourcesDisplay(HttpServletResponse response,
            RequestParameters requestParameters, BgeeProperties prop, HtmlFactory factory) 
                    throws IOException {
        super(response, requestParameters, prop, factory);
    }

    @Override
    public void displayRPackages() {
        log.entry();
        
        RequestParameters urlTopAnat = this.getNewRequestParameters();
        urlTopAnat.setPage(RequestParameters.PAGE_TOP_ANAT);

        this.addRPackaqeSchemaMarkups();
        
        this.startDisplay("R packages");

        this.writeln("<h1>R packages</h1>");

        this.writeln("<div class='feature_list'>");

        this.writeln(HtmlParentDisplay.getSingleFeatureLogo(BGEEDB_R_PACKAGE_URL,
                true, "BgeeDB R package", "BgeeDB R package",
                this.prop.getLogoImagesRootDirectory() + "r_logo_color.png",
                BGEEDB_DESCRIPTION));
        
        this.writeln(HtmlParentDisplay.getSingleFeatureLogo(
                "https://bioconductor.org/packages/release/workflows/html/BgeeCall.html",
                true, "BgeeCall R package", "BgeeCall R package",
                this.prop.getLogoImagesRootDirectory() + "r_logo_color.png",
                BGEECALL_DESCRIPTION));
        
        this.writeln("</div>"); // close feature_list

        this.endDisplay();

        log.exit();
        
    }

    @Override
    public void displayAnnotations() {
        log.entry();
        
        this.startDisplay("Annotation resources");

        this.writeln("<h1>Annotation resources</h1>");
        
        this.writeln("<div class='feature_list'>");
        
        this.writeln(HtmlParentDisplay.getSingleFeatureLogo(
                BGEE_GITHUB_URL + "/anatomical-similarity-annotations",
                true, "Anatomical similarity annotations", 
                "Anatomical similarity annotations",
                this.prop.getLogoImagesRootDirectory() + "github_logo.png",
                "Anatomical similarity annotations used to define evolutionary "
                + "relations between anatomical entities described in the Uberon "
                + "ontology."));
        
        this.writeln(HtmlParentDisplay.getSingleFeatureLogo(
                MASTER_BGEE_PIPELINE_GITHUB_URL + "/source_files/RNA_Seq",
                true, "RNA-Seq annotations", "RNA-Seq annotations",
                this.prop.getLogoImagesRootDirectory() + "github_logo.png",
                "Annotations of RNA-Seq experiments, libraries and platforms used to "
                + "generate the last version of Bgee."));
        
        this.writeln(HtmlParentDisplay.getSingleFeatureLogo(
                MASTER_BGEE_PIPELINE_GITHUB_URL + "/source_files/Affymetrix",
                true, "Affymetrix annotations", "Affymetrix annotations",
                this.prop.getLogoImagesRootDirectory() + "github_logo.png",
                "Annotations of Affymetrix experiments, chips, chip types used to "
                + "generate the last version of Bgee"));
        
        this.writeln(HtmlParentDisplay.getSingleFeatureLogo(
                MASTER_BGEE_PIPELINE_GITHUB_URL + "/source_files/ESTs",
                true, "ESTs annotations", "ESTs annotations",
                this.prop.getLogoImagesRootDirectory() + "github_logo.png",
                "ESTs annotations used to generate the last version of Bgee"));
        
        this.writeln(HtmlParentDisplay.getSingleFeatureLogo(
                "https://docs.google.com/document/d/1IuNu3WGTSIhXnJffP_yo7lK2abSgxZQDPJgG1SYF5vI",
                true, "GTEx cleaning for Bgee", "GTEx cleaning for Bgee",
                this.prop.getLogoImagesRootDirectory() + "github_logo.png",
                "Information on how the GTEx dataset was cleaned for Bgee"));
        
        this.writeln("</div>"); // close feature_list

        this.endDisplay();

        log.exit();
        
    }

    @Override
    public void displayOntologies() {
        log.entry();
        
        this.startDisplay("Ontology resources");

        this.writeln("<h1>Ontology resources</h1>");

        this.writeln("<div class='feature_list'>");
        
        this.writeln(HtmlParentDisplay.getSingleFeatureLogo(
                "https://github.com/obophenotype/developmental-stage-ontologies",
                true, "Developmental stage ontologies", "Developmental stage ontologies",
                this.prop.getLogoImagesRootDirectory() + "github_logo.png",
                "A collection of species-specific developmental stage ontologies. The custom "
                + "version of this ontology generated for Bgee and information on how to "
                + "create it are available "
                + "<a href='https://github.com/obophenotype/developmental-stage-ontologies/tree/master/external/bgee'"
                + " title = 'Link to custom version explanations' class='external_link' "
                + "target = '_blank'>here.</a>"));
        
        this.writeln(HtmlParentDisplay.getSingleFeatureLogo(
                BGEE_GITHUB_URL + "/confidence-information-ontology",
                true, "Confidence Information Ontology (CIO)", "Confidence Information Ontology (CIO)",
                this.prop.getLogoImagesRootDirectory() + "github_logo.png",
                "Ontology providing confidence information about annotation assertions "
                + "in a more systematic manner"));
        
        this.writeln(HtmlParentDisplay.getSingleFeatureLogo(
                BGEE_GITHUB_URL + "/homology-ontology",
                true, "Homology ontology (HOM)", "Homology ontology (HOM)",
                this.prop.getLogoImagesRootDirectory() + "github_logo.png",
                "Ontology providing annotations of similarity between anatomical structure"));
        
        this.writeln(HtmlParentDisplay.getSingleFeatureLogo("https://uberon.github.io/",
                true, "Uberon ontology", "Uberon ontology",
                this.prop.getLogoImagesRootDirectory() + "uberon_logo.png",
                "Bgee uses the cross-species ontology Uberon covering anatomical "
                + "structures in animals. A "
                + "<a href='" + MASTER_BGEE_PIPELINE_GITHUB_URL + "/generated_files/uberon' "
                + "title = 'Link to custom composite version of Uberon' "
                + "class='external_link' target = '_blank'>custom version</a> is "
                + "generated for Bgee. Steps explaining how and why this custom "
                + "version is generated are described "
                + "<a href='" + MASTER_BGEE_PIPELINE_GITHUB_URL + "/pipeline/uberon#anatomical-ontology-todos-before-pipeline-run'"
                + " title = 'Link to custom version explanations' class='external_link' "
                + "target = '_blank'>here</a>. We also manually modified mapping to terms"
                + "from external ontologies."));
        
        this.writeln(HtmlParentDisplay.getSingleFeatureLogo(
                "http://www.obofoundry.org/ontology/ncbitaxon.html",
                true, "NCBITaxon ontology", "NCBITaxon ontology",
                this.prop.getLogoImagesRootDirectory() + "obofoundry_logo.png",
                "Bgee uses the NCBITaxon ontology. A "
                + "<a href='" + MASTER_BGEE_PIPELINE_GITHUB_URL + "/generated_files/species' "
                + "title = 'Link to custom ncbitaxon ontology' class='external_link' "
                + "target = '_blank'>custom version</a> is generated for Bgee. Steps "
                + "explaining how and why this custom version is generated are described "
                + "<a href='" + MASTER_BGEE_PIPELINE_GITHUB_URL + "/pipeline/species#details'"
                + " title = 'Link to custom version explanations' class='external_link' "
                + "target = '_blank'>here.</a>"));
        
        
        
        this.writeln("</div>"); // close feature_list

        this.endDisplay();

        log.exit();        
    }

    @Override
    public void displaySourceCode() {
        log.entry();
        
        this.startDisplay("Source code");

        this.writeln("<h1>Source code</h1>");

        this.writeln("<div class='feature_list'>");
        
        this.writeln(HtmlParentDisplay.getSingleFeatureLogo(
                BGEE_GITHUB_URL + "/bgee_pipeline",
                true, "Bgee pipeline", "Bgee pipeline",
                this.prop.getLogoImagesRootDirectory() + "github_logo.png",
                "Well documented source code of the Bgee pipeline used to generate "
                + "databases and download files."));
        
        this.writeln(HtmlParentDisplay.getSingleFeatureLogo(
                BGEE_GITHUB_URL + "/BgeeDB_R",
                true, "BgeeDB R package", "BgeeDB R package",
                this.prop.getLogoImagesRootDirectory() + "github_logo.png",
                BGEEDB_DESCRIPTION));
        
        this.writeln(HtmlParentDisplay.getSingleFeatureLogo(
                BGEE_GITHUB_URL + "/BgeeCall",
                true, "BgeeCall R package", "BgeeCall R package",
                this.prop.getLogoImagesRootDirectory() + "github_logo.png",
                BGEECALL_DESCRIPTION));
        
        this.writeln(HtmlParentDisplay.getSingleFeatureLogo(
                BGEE_GITHUB_URL + "/IQRray",
                true, "IQRay", "IQRay",
                this.prop.getLogoImagesRootDirectory() + "github_logo.png",
                "A method for Affymetrix microarray quality control which outperforms "
                + "other methods in identification of poor quality arrays in datasets "
                + "composed of arrays from many independent experiments."));
        
        this.writeln("</div>"); // close feature_list

        this.endDisplay();

        log.exit();         
    }

    /**
     * Add schema.org markups to the page.
     */
    private void addRPackaqeSchemaMarkups() {
        log.entry();

        RequestParameters url = this.getNewRequestParameters();
        url.setPage(RequestParameters.PAGE_RESOURCES);
        url.setAction(RequestParameters.ACTION_RESOURCES_R_PACKAGES);

        this.writeln("<script type='application/ld+json'>");

        this.writeln("{" +
                "  \"@context\": \"https://schema.org\"," +
                "  \"@graph\": [" +
                "    {" +
                "       \"@type\": \"SoftwareApplication\"," +
                "       \"@id\": \"" + this.prop.getBgeeRootDirectory() + "#BgeeDB-R-package\"," +
                "       \"name\": \"BgeeDB R package\"," +
                "       \"url\": \"" + url.getRequestURL() + "\"," +
                "       \"offers\": {" +
                "          \"@type\": \"Offer\"," +
                "          \"price\": \"0.00\"," +
                "          \"priceCurrency\": \"CHF\"" +
                "       }, " +
                "       \"applicationCategory\": \"https://www.wikidata.org/wiki/Q15544757\"" + // science software
                "    }, " +
                "    {" +
                "       \"@type\": \"SoftwareApplication\"," +
                "       \"@id\": \"" + this.prop.getBgeeRootDirectory() + "#BgeeCall-R-package\"," +
                "       \"name\": \"BgeeCall R package\"," +
                "       \"url\": \"" + url.getRequestURL() + "\"," +
                "       \"offers\": {" +
                "          \"@type\": \"Offer\"," +
                "          \"price\": \"0.00\"," +
                "          \"priceCurrency\": \"CHF\"" +
                "       }, " +
                "       \"applicationCategory\": \"https://www.wikidata.org/wiki/Q15544757\"" + // science software
                "    }" +
                "  ]" +
                "}");

        this.writeln("</script>");
        log.exit();
    }
}
