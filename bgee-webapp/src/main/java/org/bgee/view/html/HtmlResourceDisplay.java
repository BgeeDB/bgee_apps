package org.bgee.view.html;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class displays the resources for the HTML view.
 *
 * @author  Julien Wollbrett
 * @version Bgee 14, May 2019
 * @since   Bgee 14, May 2019
 */

import org.bgee.controller.BgeeProperties;
import org.bgee.controller.RequestParameters;
import org.bgee.view.ResourceDisplay;

public class HtmlResourceDisplay extends HtmlParentDisplay implements ResourceDisplay{

    private final static Logger log = LogManager.getLogger(HtmlResourceDisplay.class.getName());
    
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
    public HtmlResourceDisplay(HttpServletResponse response,
            RequestParameters requestParameters, BgeeProperties prop, HtmlFactory factory) 
                    throws IOException {
        super(response, requestParameters, prop, factory);
    }

    @Override
    public void displayRPackages() {
        log.entry();
        
        RequestParameters urlTopAnat = this.getNewRequestParameters();
        urlTopAnat.setPage(RequestParameters.PAGE_TOP_ANAT);
        
        this.startDisplay("R Package overview");

        this.writeln("<h1>R Package overview</h1>");

        this.writeln("<div class='feature_list'>");

        this.writeln(HtmlParentDisplay.getSingleFeatureLogo(BGEEDB_R_PACKAGE_URL,
                true, "BgeeDB R package", "BgeeDB R package",
                this.prop.getLogoImagesRootDirectory() + "r_logo_color.png",
                BGEEDB_DESCRIPTION));
        
        this.writeln(HtmlParentDisplay.getSingleFeatureLogo(BGEECALL_R_PACKAGE_URL,
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
        
        this.startDisplay("Annotation resource overview");

        this.writeln("<h1>Annotation resource overview</h1>");
        
        this.writeln("<div class='feature_list'>");
        
        this.writeln(HtmlParentDisplay.getSingleFeatureLogo(ANAT_SIM_GITHUB_URL,
                true, "Annatomical similarity annotations", 
                "Annatomical similarity annotations",
                this.prop.getLogoImagesRootDirectory() + "github_logo.png",
                "Annatomical similarity annotations used to define evolutionary "
                + "relations between anatomical entities described in the Uberon "
                + "ontology."));
        
        this.writeln(HtmlParentDisplay.getSingleFeatureLogo(RNASEQ_ANNOT_GITHUB_URL,
                true, "RNA-Seq annotations", "RNA-Seq annotations",
                this.prop.getLogoImagesRootDirectory() + "github_logo.png",
                "Annotations of RNA-Seq experiments, libraries and platforms used to "
                + "generate the last version of Bgee."));
        
        this.writeln(HtmlParentDisplay.getSingleFeatureLogo(AFFI_ANNOT_GITHUB_URL,
                true, "Affymetrix annotations", "Affymetrix annotations",
                this.prop.getLogoImagesRootDirectory() + "github_logo.png",
                "Annotations of Affymetrix experiments, chips, chip types used to "
                + "generate the last version of Bgee"));
        
        this.writeln(HtmlParentDisplay.getSingleFeatureLogo(EST_ANNOT_GITHUB_URL,
                true, "ESTs annotations", "ESTs annotations",
                this.prop.getLogoImagesRootDirectory() + "github_logo.png",
                "ESTs annotations used to generate the last version of Bgee"));
        
        this.writeln(HtmlParentDisplay.getSingleFeatureLogo(GTEX_CLEANING_URL,
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
        
        this.startDisplay("Ontology resource overview");

        this.writeln("<h1>Ontology resource overview</h1>");

        this.writeln("<div class='feature_list'>");
        
        this.writeln(HtmlParentDisplay.getSingleFeatureLogo(DEV_STAGE_ONTOLOGIES_GITHUB_URL,
                true, "Developmental stage ontologies", "Developmental stage ontologies",
                this.prop.getLogoImagesRootDirectory() + "github_logo.png",
                "A collection of species-specific developmental stage ontologies. The custom "
                + "version of this ontology generated for Bgee and information on how to "
                + "create it are available "
                + "<a href='https://github.com/obophenotype/developmental-stage-ontologies/tree/master/external/bgee'"
                + " title = 'Link to custom version explanations' class='external_link' "
                + "target = '_blank'>here.</a>"));
        
        this.writeln(HtmlParentDisplay.getSingleFeatureLogo(CIO_GITHUB_URL,
                true, "Confidence Information Ontology (CIO)", "Confidence Information Ontology (CIO)",
                this.prop.getLogoImagesRootDirectory() + "github_logo.png",
                "Ontology providing confidence information about annotation assertions "
                + "in a more systematic manner"));
        
        this.writeln(HtmlParentDisplay.getSingleFeatureLogo(HOM_GITHUB_URL,
                true, "Homology ontology (HOM)", "Homology ontology (HOM)",
                this.prop.getLogoImagesRootDirectory() + "github_logo.png",
                "Ontology providing annotations of similarity between anatomical structure"));
        
        this.writeln(HtmlParentDisplay.getSingleFeatureLogo(UBERON_URL,
                true, "Uberon ontology", "Uberon ontology",
                this.prop.getLogoImagesRootDirectory() + "uberon_logo.png",
                "Bgee uses the cross-species ontology Uberon covering anatomical "
                + "structures in animals. A "
                + "<a href='https://github.com/BgeeDB/bgee_pipeline/tree/master/generated_files/uberon' "
                + "title = 'Link to custom composite version of Uberon' "
                + "class='external_link' target = '_blank'>custom version</a> is "
                + "generated for Bgee. Steps explaining how and why this custom "
                + "version is generated are described "
                + "<a href='https://github.com/BgeeDB/bgee_pipeline/tree/master/pipeline/uberon#anatomical-ontology-todos-before-pipeline-run'"
                + " title = 'Link to custom version explanations' class='external_link' "
                + "target = '_blank'>here</a>. We also manually modified mapping to terms"
                + "from external ontologies."));
        
        this.writeln(HtmlParentDisplay.getSingleFeatureLogo(NCBITAXON_URL,
                true, "NCBITaxon ontology", "NCBITaxon ontology",
                this.prop.getLogoImagesRootDirectory() + "obofoundry_logo.png",
                "Bgee uses the NCBITaxon ontology. A "
                + "<a href='https://github.com/BgeeDB/bgee_pipeline/tree/master/generated_files/species' "
                + "title = 'Link to custom ncbitaxon ontology' class='external_link' "
                + "target = '_blank'>custom version</a> is generated for Bgee. Steps "
                + "explaining how and why this custom version is generated are described "
                + "<a href='https://github.com/BgeeDB/bgee_pipeline/tree/master/pipeline/species#details'"
                + " title = 'Link to custom version explanations' class='external_link' "
                + "target = '_blank'>here.</a>"));
        
        
        
        this.writeln("</div>"); // close feature_list

        this.endDisplay();

        log.exit();        
    }

    @Override
    public void displaySourceCode() {
        log.entry();
        
        this.startDisplay("Source code resource overview");

        this.writeln("<h1>Source code resource overview</h1>");

        this.writeln("<div class='feature_list'>");
        
        this.writeln(HtmlParentDisplay.getSingleFeatureLogo(BGEE_PIPELINE_GITHUB_URL,
                true, "Bgee pipeline", "Bgee pipeline",
                this.prop.getLogoImagesRootDirectory() + "github_logo.png",
                "Well documented source code of the Bgee pipeline used to generate "
                + "databases and download files."));
        
        this.writeln(HtmlParentDisplay.getSingleFeatureLogo(BGEEDB_GITHUB_URL,
                true, "BgeeDB R package", "BgeeDB R package",
                this.prop.getLogoImagesRootDirectory() + "github_logo.png",
                BGEEDB_DESCRIPTION));
        
        this.writeln(HtmlParentDisplay.getSingleFeatureLogo(BGEECALL_R_PACKAGE_URL,
                true, "BgeeCall R package", "BgeeCall R package",
                this.prop.getLogoImagesRootDirectory() + "github_logo.png",
                BGEECALL_DESCRIPTION));
        
        this.writeln(HtmlParentDisplay.getSingleFeatureLogo(IQRAY_GITHUB_URL,
                true, "IQRay", "IQRay",
                this.prop.getLogoImagesRootDirectory() + "github_logo.png",
                "A method for Affymetrix microarray quality control which outperforms "
                + "other methods in identification of poor quality arrays in datasets "
                + "composed of arrays from many independent experiments."));
        
        this.writeln("</div>"); // close feature_list

        this.endDisplay();

        log.exit();         
    }

}
