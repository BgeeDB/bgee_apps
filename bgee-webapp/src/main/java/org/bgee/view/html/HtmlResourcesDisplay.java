package org.bgee.view.html;

import java.io.IOException;
import java.util.Arrays;

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
 * @author  Valentine Rech de Laval
 * @version Bgee 14, June 2019
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
    
    private final static String CONTAINER_DESCRIPTION = "Docker container for BgeeCall and BgeeDB R "
            + "Bioconductor packages. Contains everything needed to download Bgee data, run TopAnat "
            + "or generate present/absent calls in R.";
    
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

        RequestParameters url = this.getNewRequestParameters();
        url.setPage(RequestParameters.PAGE_RESOURCES);
        url.setAction(RequestParameters.ACTION_RESOURCES_R_PACKAGES);
        
        this.startDisplay("R packages");

        this.writeln(getSchemaMarkupGraph( Arrays.asList(
                getSoftwareApplication("BgeeDB-R-package", "BgeeDB R package",
                        BGEEDB_DESCRIPTION, BGEEDB_R_PACKAGE_URL),
                getSoftwareApplication("BgeeCall-R-package", "BgeeCall R package",
                        BGEECALL_DESCRIPTION, BGEECALL_R_PACKAGE_URL))));

        this.writeln("<h1>R packages</h1>");

        this.writeln("<div class='feature_list'>");

        this.writeln(HtmlParentDisplay.getSingleFeatureLogo(
                BGEEDB_R_PACKAGE_URL, true, "BgeeDB R package", "BgeeDB R package",
                this.prop.getLogoImagesRootDirectory() + "r_logo_color.png", BGEEDB_DESCRIPTION));
        
        this.writeln(HtmlParentDisplay.getSingleFeatureLogo(
                BGEECALL_R_PACKAGE_URL, true, "BgeeCall R package", "BgeeCall R package",
                this.prop.getLogoImagesRootDirectory() + "r_logo_color.png", BGEECALL_DESCRIPTION));
        
        this.writeln(HtmlParentDisplay.getSingleFeatureLogo(
                R_PACKAGES_CONTAINER_URL, true, "Container for BgeeCall and BgeeDB", "Container "
                        + "for BgeeCall and BgeeDB", this.prop.getLogoImagesRootDirectory() 
                        + "docker_logo.png", CONTAINER_DESCRIPTION));
        
        this.writeln("</div>"); // close feature_list

        this.endDisplay();

        log.traceExit();
        
    }

    @Override
    public void displayAnnotations() {
        log.entry();
        
        String anatSimUrl = BGEE_GITHUB_URL + "/anatomical-similarity-annotations";
        String rnaSeqSourceFileUrl = MASTER_BGEE_PIPELINE_GITHUB_URL + "/source_files/RNA_Seq";
        String affySourceFileUrl = MASTER_BGEE_PIPELINE_GITHUB_URL + "/source_files/Affymetrix";
        String estSourceFileUrl = MASTER_BGEE_PIPELINE_GITHUB_URL + "/source_files/ESTs";
        String gtexUrl = "https://docs.google.com/document/d/1IuNu3WGTSIhXnJffP_yo7lK2abSgxZQDPJgG1SYF5vI";

        String anatSimDesc = "Anatomical similarity annotations used to define evolutionary "
                + "relations between anatomical entities described in the Uberon "
                + "ontology.";
        String rnaSeqDesc = "Annotations of RNA-Seq experiments, libraries and platforms used to "
                + "generate the last version of Bgee.";
        String affyDesc = "Annotations of Affymetrix experiments, chips, chip types used to "
                + "generate the last version of Bgee";
        String estDesc = "ESTs annotations used to generate the last version of Bgee";
        String gtexDesc = "Information on how the GTEx dataset was cleaned for Bgee";

        this.startDisplay("Annotation resources");

        this.writeln(getSchemaMarkupGraph(Arrays.asList(
                getCreativeWorkProperty("anatomical-similarity-annotations", "Anatomical similarity annotations",
                        anatSimDesc, anatSimUrl, RequestParameters.ACTION_RESOURCES_ANNOTATIONS),
                getCreativeWorkProperty("RNA-seq-source-files", "RNA-Seq annotations",
                        rnaSeqDesc, rnaSeqSourceFileUrl, RequestParameters.ACTION_RESOURCES_ANNOTATIONS),
                getCreativeWorkProperty("affymetrix-source-files", "Affymetrix annotations",
                        affyDesc, affySourceFileUrl, RequestParameters.ACTION_RESOURCES_ANNOTATIONS),
                getCreativeWorkProperty("EST-source-files", "ESTs annotations",
                        estDesc, estSourceFileUrl, RequestParameters.ACTION_RESOURCES_ANNOTATIONS),
                getCreativeWorkProperty("GTEx-cleaning-file", "GTEx cleaning for Bgee",
                        gtexUrl, gtexUrl, RequestParameters.ACTION_RESOURCES_ANNOTATIONS))));

        this.writeln("<h1>Annotation resources</h1>");
        
        this.writeln("<div class='feature_list'>");
        
        this.writeln(HtmlParentDisplay.getSingleFeatureLogo(
                anatSimUrl, true, "Anatomical similarity annotations", 
                "Anatomical similarity annotations",
                this.prop.getLogoImagesRootDirectory() + "github_logo.png", anatSimDesc));
        
        this.writeln(HtmlParentDisplay.getSingleFeatureLogo(
                rnaSeqSourceFileUrl, true, "RNA-Seq annotations", "RNA-Seq annotations",
                this.prop.getLogoImagesRootDirectory() + "github_logo.png", rnaSeqDesc));
        
        this.writeln(HtmlParentDisplay.getSingleFeatureLogo(
                affySourceFileUrl, true, "Affymetrix annotations", "Affymetrix annotations",
                this.prop.getLogoImagesRootDirectory() + "github_logo.png", affyDesc));
        
        this.writeln(HtmlParentDisplay.getSingleFeatureLogo(
                estSourceFileUrl, true, "ESTs annotations", "ESTs annotations",
                this.prop.getLogoImagesRootDirectory() + "github_logo.png",
                estDesc));
        
        this.writeln(HtmlParentDisplay.getSingleFeatureLogo(
                gtexUrl, true, "GTEx cleaning for Bgee", "GTEx cleaning for Bgee",
                this.prop.getLogoImagesRootDirectory() + "github_logo.png", gtexDesc));
        
        this.writeln("</div>"); // close feature_list

        this.endDisplay();

        log.traceExit();
        
    }

    @Override
    public void displayOntologies() {
        log.entry();
        
        this.startDisplay("Ontology resources");

        String devStageDesc = "A collection of species-specific developmental stage ontologies.";
        String homDesc = "Ontology describing homology-related concepts, notably used "
                + "in our annotations of similarity between anatomical structures.";
        String cioDesc = "Ontology providing confidence information about annotation assertions "
                + "in a systematic manner";
        String devStageUrl = "https://github.com/obophenotype/developmental-stage-ontologies";
        String cioUrl = BGEE_GITHUB_URL + "/confidence-information-ontology";
        String homUrl = BGEE_GITHUB_URL + "/homology-ontology";

        this.writeln(getSchemaMarkupGraph(Arrays.asList(
                getCreativeWorkProperty("developmental-stage-ontologies", "Developmental stage ontologies",
                        devStageDesc, devStageUrl, RequestParameters.ACTION_RESOURCES_ONTOLOGIES),
                getCreativeWorkProperty("confidence-information-ontology", "Confidence Information Ontology (CIO)",
                        cioDesc, cioUrl, RequestParameters.ACTION_RESOURCES_ONTOLOGIES),
                getCreativeWorkProperty("homology-ontology", "Homology ontology (HOM)",
                        homDesc, homUrl, RequestParameters.ACTION_RESOURCES_ONTOLOGIES))));

        this.writeln("<h1>Ontology resources</h1>");

        this.writeln("<div class='feature_list'>");
        
        this.writeln(HtmlParentDisplay.getSingleFeatureLogo(
                devStageUrl, true, "Developmental stage ontologies", "Developmental stage ontologies",
                this.prop.getLogoImagesRootDirectory() + "github_logo.png",
                devStageDesc + " The custom version of this ontology generated for Bgee and "
                + "information on how to create it are available "
                + "<a href='https://github.com/obophenotype/developmental-stage-ontologies/tree/master/external/bgee'"
                + " title='Link to custom version explanations' class='external_link' "
                + "target='_blank' rel='noopener'>on GitHub.</a>"));
        
        this.writeln(HtmlParentDisplay.getSingleFeatureLogo(
                cioUrl, true, "Confidence Information Ontology (CIO)", "Confidence Information Ontology (CIO)",
                this.prop.getLogoImagesRootDirectory() + "github_logo.png", cioDesc));
        
        this.writeln(HtmlParentDisplay.getSingleFeatureLogo(
                homUrl, true, "Homology ontology (HOM)", "Homology ontology (HOM)",
                this.prop.getLogoImagesRootDirectory() + "github_logo.png", homDesc));
        
        this.writeln(HtmlParentDisplay.getSingleFeatureLogo("https://uberon.github.io/",
                true, "Uberon ontology", "Uberon ontology",
                this.prop.getLogoImagesRootDirectory() + "uberon_logo.png",
                "Bgee uses the cross-species ontology Uberon covering anatomical "
                + "structures in animals. A "
                + "<a href='" + MASTER_BGEE_PIPELINE_GITHUB_URL + "/generated_files/uberon' "
                + "title='Link to custom composite version of Uberon' "
                + "class='external_link' target='_blank' rel='noopener'>custom version</a> is "
                + "generated for Bgee. Steps explaining how and why this custom "
                + "version is generated are described "
                + "<a href='" + MASTER_BGEE_PIPELINE_GITHUB_URL + "/pipeline/uberon#anatomical-ontology-todos-before-pipeline-run'"
                + " title='Link to custom version explanations' class='external_link' "
                + "target='_blank' rel='noopener'>on GitHub</a>. We also manually modified mapping to terms"
                + "from external ontologies."));
        
        this.writeln(HtmlParentDisplay.getSingleFeatureLogo(
                "http://www.obofoundry.org/ontology/ncbitaxon.html",
                true, "NCBITaxon ontology", "NCBITaxon ontology",
                this.prop.getLogoImagesRootDirectory() + "obofoundry_logo.png",
                "Bgee uses the NCBITaxon ontology. A "
                + "<a href='" + MASTER_BGEE_PIPELINE_GITHUB_URL + "/generated_files/species' "
                + "title = 'Link to custom ncbitaxon ontology' class='external_link' "
                + "target='_blank' rel='noopener'>custom version</a> is generated for Bgee. Steps "
                + "explaining how and why this custom version is generated are described "
                + "<a href='" + MASTER_BGEE_PIPELINE_GITHUB_URL + "/pipeline/species#details'"
                + " title='Link to custom version explanations' class='external_link' "
                + "target='_blank' rel='noopener'>on GitHub.</a>"));
        
        
        
        this.writeln("</div>"); // close feature_list

        this.endDisplay();

        log.traceExit();        
    }

    @Override
    public void displaySourceCode() {
        log.entry();
        
        this.startDisplay("Source code");

        String pipelineDesc = "Well documented source code of the Bgee pipeline used to generate "
                + "databases and download files.";
        String iqRayDesc = "A method for Affymetrix microarray quality control which outperforms "
                + "other methods in identification of poor quality arrays in datasets "
                + "composed of arrays from many independent experiments.";
        String bgeedbGithubUrl = BGEE_GITHUB_URL + "/BgeeDB_R";
        String bgeecallGithubUrl = BGEE_GITHUB_URL + "/BgeeCall";
        String iqRayGithubUrl = BGEE_GITHUB_URL + "/IQRray";

        this.writeln(this.getSchemaMarkupGraph(Arrays.asList(
                getSoftwareSourceCodeProperty("Bgee-pipeline", "Bgee pipeline code",
                        pipelineDesc, MASTER_BGEE_PIPELINE_GITHUB_URL, "Perl, R, and Java"),
                getSoftwareSourceCodeProperty("BgeeDB-R-package", "BgeeDB R package code",
                        BGEEDB_DESCRIPTION, bgeedbGithubUrl, "R"),
                getSoftwareSourceCodeProperty("BgeeCall-R-package", "BgeeCall R package code",
                        BGEECALL_DESCRIPTION, bgeecallGithubUrl, "R"),
                getSoftwareSourceCodeProperty("IQRray", "IQRay code",
                        iqRayDesc, iqRayGithubUrl, "R"))));

        this.writeln("<h1>Source code</h1>");

        this.writeln("<div class='feature_list'>");
        
        this.writeln(HtmlParentDisplay.getSingleFeatureLogo(
                MASTER_BGEE_PIPELINE_GITHUB_URL, true, "Bgee pipeline", "Bgee pipeline",
                this.prop.getLogoImagesRootDirectory() + "github_logo.png", pipelineDesc));
        
        this.writeln(HtmlParentDisplay.getSingleFeatureLogo(
                bgeedbGithubUrl, true, "BgeeDB R package", "BgeeDB R package",
                this.prop.getLogoImagesRootDirectory() + "github_logo.png", BGEEDB_DESCRIPTION));
        
        this.writeln(HtmlParentDisplay.getSingleFeatureLogo(
                bgeecallGithubUrl, true, "BgeeCall R package", "BgeeCall R package",
                this.prop.getLogoImagesRootDirectory() + "github_logo.png", BGEECALL_DESCRIPTION));
        
        this.writeln(HtmlParentDisplay.getSingleFeatureLogo(
                iqRayGithubUrl, true, "IQRay", "IQRay",
                this.prop.getLogoImagesRootDirectory() + "github_logo.png", iqRayDesc));
        
        this.writeln("</div>"); // close feature_list

        this.endDisplay();

        log.traceExit();         
    }

    private String getSoftwareSourceCodeProperty(String idSuffix, String name, String description, 
                                         String codeRepository, String programmingLanguage) {
        log.entry(idSuffix, name, description, codeRepository, programmingLanguage);
        
        RequestParameters url = this.getNewRequestParameters();
        url.setPage(RequestParameters.PAGE_RESOURCES);
        url.setAction(RequestParameters.ACTION_RESOURCES_SOURCE_CODE);

        return log.traceExit(
                "    {" +
                "       \"@type\": \"SoftwareSourceCode\"," +
                "       \"@id\": \"" + url.getRequestURL() + "#" + idSuffix + "\"," +
                "       \"name\": \"" + name + "\"," +
                "       \"description\": \"" + description + "\"," +
                "       \"url\": \"" + url.getRequestURL() + "\"," +
                "       \"codeRepository\": \"" + codeRepository + "\"," +
                "       \"programmingLanguage\": \"" + programmingLanguage + "\"" +
                "    }");
    }

    private String getCreativeWorkProperty(String idSuffix, String name, String description,
                                           String sameAsUrl, String requestParametersAction) {
        log.entry(idSuffix, name, description, sameAsUrl, requestParametersAction);

        RequestParameters url = this.getNewRequestParameters();
        url.setPage(RequestParameters.PAGE_RESOURCES);
        url.setAction(requestParametersAction);

        return log.traceExit("    {" +
                        "       \"@type\": \"CreativeWork\"," +
                        "       \"@id\": \"" + url.getRequestURL() + "#" + idSuffix + "\"," +
                        "       \"name\": \"" + name + "\"," +
                        "       \"description\": \"" + description + "\"," +
                        "       \"url\": \"" + url.getRequestURL() + "\"," +
                        "       \"sameAs\": \"" + sameAsUrl + "\"" +
                        "    }");
    }

    private String getSoftwareApplication(String idSuffix, String name, String description,
                                          String sameAs) {
        log.entry(idSuffix, name, description, sameAs);

        RequestParameters url = this.getNewRequestParameters();
        url.setPage(RequestParameters.PAGE_RESOURCES);
        url.setAction(RequestParameters.ACTION_RESOURCES_R_PACKAGES);

        return log.traceExit("{" +
                        "   \"@type\": \"SoftwareApplication\"," +
                        "   \"@id\": \"" + this.prop.getBgeeRootDirectory() + "#" + idSuffix + "\"," +
                        "   \"name\": \"" + name + "\"," +
                        "   \"description\": \"" + description + "\"," +
                        "   \"url\": \"" + url.getRequestURL() + "\"," +
                        "   \"offers\": {" +
                        "      \"@type\": \"Offer\"," +
                        "      \"price\": \"0.00\"," +
                        "      \"priceCurrency\": \"CHF\"" +
                        "   }, " +
                        "   \"applicationCategory\": \"https://www.wikidata.org/wiki/Q15544757\"," + // science software
                        "   \"sameAs\": \"" + sameAs + "\"" +
                        "}");
    }
}
