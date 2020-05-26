package org.bgee.view.html;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.BgeeProperties;
import org.bgee.controller.RequestParameters;
import org.bgee.model.expressiondata.CallService;
import org.bgee.model.file.DownloadFile;
import org.bgee.model.file.DownloadFile.CategoryEnum;
import org.bgee.model.file.SpeciesDataGroup;
import org.bgee.model.species.Species;
import org.bgee.view.SpeciesDisplay;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.bgee.model.file.DownloadFile.CategoryEnum.*;


/**
 * This class is the HTML implementation of the {@code SpeciesDisplay}.
 *
 * @author  Valentine Rech de Laval
 * @version Bgee 14, July 2019
 * @since   Bgee 14, June 2019
 */
public class HtmlSpeciesDisplay extends HtmlParentDisplay implements SpeciesDisplay {

    /**
     * {@code Logger} of the class.
     */
    private final static Logger log = LogManager.getLogger(HtmlSpeciesDisplay.class.getName());

    public HtmlSpeciesDisplay(HttpServletResponse response, RequestParameters requestParameters,
                              BgeeProperties prop, HtmlFactory factory)
            throws IllegalArgumentException, IOException {
        super(response, requestParameters, prop, factory);
    }

    @Override
    public void displaySpeciesHomePage(List<Species> speciesList) {
        log.entry(speciesList);

        this.startDisplay("Bgee species list", null, "List of species in Bgee.");

        this.writeln("<h1>Bgee species list</h1>");

        this.writeln("<div class='species_list'>");

        this.writeln(speciesList.stream()
                .map(species -> 
                           "<a class='species_element' href='" + getSpeciesPageUrl(species.getId()) + "'>" +
                           "    <figure><img src='"+ this.getSpeciesImageSrc(species, true)+"' alt='"+ htmlEntities(species.getShortName())+"'>" +
                           "        <figcaption>" +
                           "            <p><i>" + htmlEntities(species.getShortName()) + "</i></p>" +
                           "            <p>" + (StringUtils.isNotBlank(species.getName()) ?
                                            htmlEntities(species.getName()): "-") + "</p>" +
                           "        </figcaption>" +
                           "    </figure>" +
                           "</a>")
                .collect(Collectors.joining()));

        this.writeln("</div>");

        this.endDisplay();

        log.exit();
    }

    @Override
    public void sendSpeciesResponse(List<Species> species) {
        throw log.throwing(new UnsupportedOperationException("Not available for HTML display"));
    }

    @Override
    public void displaySpecies(Species species, SpeciesDataGroup speciesDataGroup) {
        log.entry(species, speciesDataGroup);
        String description = htmlEntities(species.getScientificName()) + " species in Bgee.";
        this.startDisplay("Species: " + htmlEntities(species.getScientificName()), "WebPage", description);

        this.writeln(this.getSchemaMarkup(speciesDataGroup));
        
        //page title
        this.writeln("<h1 property='schema:name'>");
        this.writeln("<img src='" + this.getSpeciesImageSrc(species, true)
                + "' alt='" + htmlEntities(species.getShortName()) + "' />");
        this.writeln("Species: " + getCompleteSpeciesName(species, false));
        this.writeln("</h1>");
        
        //Species general information
        this.writeln("<h2>General information</h2>");

        this.writeln("<div typeof='bs:Taxon'>");
        this.writeln(this.getGeneralInfo(species));
        this.writeln("</div>");

        //Species download files
        this.writeln("<h2>Gene expression call files</h2>");
        this.writeln(this.getCallFileSection(speciesDataGroup));

        this.writeln("<h2>Processed expression value files</h2>");
        this.writeln(this.getValueFileSection(speciesDataGroup));

        this.endDisplay();

        log.exit();
    }

    private String getSchemaMarkup(SpeciesDataGroup sdg) {
        log.entry(sdg);
        
        List<String> props = new ArrayList<>();
        
        Species species = sdg.getMembers().get(0);
        String callUrl = null;
        for (DownloadFile f: sdg.getDownloadFiles()) {
            if (f.getCategory().equals(DownloadFile.CategoryEnum.EXPR_CALLS_COMPLETE) &&
                    f.getConditionParameters() != null
                    && f.getConditionParameters().equals(new HashSet<>(Arrays.asList(
                    CallService.Attribute.ANAT_ENTITY_ID, CallService.Attribute.DEV_STAGE_ID)))) {
                callUrl = this.prop.getDownloadRootDirectory() + f.getPath();
                break;
            }
        }
        props.add("{" +
                "    \"@type\": \"Dataset\"," +
                "    \"@id\": \"" + this.getDatasetSchemaId(species.getId(), EXPR_CALLS_COMPLETE) + "\"," +
                "    \"url\": \"" + this.getSpeciesPageUrl(species.getId()) + "\"," +
                "    \"name\": \"" + species.getScientificName() + " gene expression calls\"," +
                "    \"description\": \"" + species.getScientificName() + " calls of baseline presence/absence of expression\"," +
                (callUrl == null? "" :
                "    \"distribution\": {\"@type\": \"DataDownload\", \"encodingFormat\": \"TSV\", \"contentUrl\": \"" + callUrl + "\"},") +
                "    \"license\": \"" + LICENCE_CC0_URL + "\"," +
                "    \"keywords\": [\"gene expression\",\"call\",\"" + species.getScientificName() + "\"" +
                (StringUtils.isBlank(species.getName())? "": ",\"" + species.getName() + "\"") + "]," +
                "    \"version\": \"" + this.getWebAppVersion() + "\"," +
                "    \"includedInDataCatalog\": {" +
                "           \"@type\": \"DataCatalog\"," +
                "           \"@id\": \""+ this.prop.getBgeeRootDirectory() + "\"," +
                "           \"sameAs\": \"" + this.prop.getBgeeRootDirectory() + "\"" +
                "    }" +
                "}");

        props.add("{" +
                "    \"@type\": \"Dataset\"," +
                "    \"@id\": \"" + this.getDatasetSchemaId(species.getId(), RNASEQ_DATA) + "\"," +
                "    \"url\": \"" + this.getSpeciesPageUrl(species.getId()) + "\"," +
                "    \"name\": \"" + species.getScientificName() + " RNA-seq processed expression values\"," +
                "    \"description\": \"Annotations and experiment information (e.g., annotations " +
                            "to anatomy and development, quality scores used in QCs, library information), " +
                            "and processed expression values (e.g., read counts, TPM and FPKM values) " +
                            "for " + species.getScientificName() + "\"," +
                "    \"distribution\": {\"@type\": \"DataDownload\", \"encodingFormat\": \"TSV\", \"contentUrl\": \""
                + this.prop.getDownloadRNASeqProcExprValueFilesRootDirectory() + species.getScientificName().replace(" ", "_") + "\"}," +
                "    \"license\": \"" + LICENCE_CC0_URL + "\"," +
                "    \"keywords\": [\"annotations\",\"experiment information\",\"processed expression values\"," +
                "           \"RNA-Seq\",\"" + species.getScientificName() + "\"" +
                (StringUtils.isBlank(species.getName())? "": ",\"" + species.getName() + "\"") + "]," +
                "    \"version\": \"" + this.getWebAppVersion() + "\"," +
                "    \"includedInDataCatalog\": {" +
                "           \"@type\": \"DataCatalog\"," +
                "           \"@id\": \""+ this.prop.getBgeeRootDirectory() + "\"," +
                "           \"sameAs\": \"" + this.prop.getBgeeRootDirectory() + "\"" +
                "    }" +
                "}");
        
        props.add(
                "{" +
                "    \"@type\": \"Dataset\"," +
                        "    \"@id\": \"" + this.getDatasetSchemaId(species.getId(),AFFY_DATA) + "\"," +
                        "    \"url\": \"" + this.getSpeciesPageUrl(species.getId()) + "\"," +
                "    \"name\": \"" + species.getScientificName() + " Affymetrix processed expression values\"," +
                "    \"description\": \"Annotations and experiment information (e.g., annotations " +
                            "to anatomy and development, quality scores used in QCs, chip information), " +
                            "and processed expression values (e.g., log values of " +
                            "Affymetrix probeset normalized signal intensities) " +
                "for " + species.getScientificName() + "\"," +
                "    \"distribution\": {\"@type\": \"DataDownload\", \"encodingFormat\": \"TSV\", \"contentUrl\": \""
                + this.prop.getDownloadAffyProcExprValueFilesRootDirectory() + species.getScientificName().replace(" ", "_") + "\"}," +
                "    \"license\": \"" + LICENCE_CC0_URL + "\"," +
                "    \"keywords\": [\"annotations\",\"experiment information\",\"processed expression values\"," +
                "           \"Affymetrix\",\"" + species.getScientificName() + "\"" +
                (StringUtils.isBlank(species.getName())? "": ",\"" + species.getName() + "\"") + "]," +
                "    \"version\": \"" + this.getWebAppVersion() + "\"," +
                "    \"includedInDataCatalog\": {" +
                "           \"@type\": \"DataCatalog\"," +
                "           \"@id\": \""+ this.prop.getBgeeRootDirectory() + "\"," +
                "           \"sameAs\": \"" + this.prop.getBgeeRootDirectory() + "\"" +
                "    }" +
                "}");
        
        return log.exit(getSchemaMarkupGraph(props));
    }

    private String getGeneralInfo(Species species) {
        log.entry(species);
        
        final StringBuilder table = new StringBuilder("<div class='info-content'>");
        table.append("<table class='info-table'>");
        
        table.append("<tr><th scope='row'>Scientific name</th><td property='bs:name'><em>")
                .append(htmlEntities(species.getScientificName())).append("</em></td></tr>");
        if (StringUtils.isNotBlank(species.getName())) {
            table.append("<tr><th scope='row'>Common name</th><td>")
                    .append(htmlEntities(species.getName())).append("</td></tr>");
        }
        table.append("<tr><th scope='row'>Species ID</th><td>")
                .append("<a class='external_link' target='_blank' rel='noopener' href='" +
                        "https://www.ncbi.nlm.nih.gov/Taxonomy/Browser/wwwtax.cgi?lvl=0&amp;id=")
                .append(species.getId()).append("'>")
                .append(htmlEntities(String.valueOf(species.getId()))).append("</a></td></tr>");
        String speciesSourceURL = species.getGenomeSource().getBaseUrl() + 
                species.getScientificName().replace(" ", "_");
        table.append("<tr><th scope='row'>Genome source</th><td><a class='external_link' target='_blank' rel='noopener' " +
                "href='").append(speciesSourceURL).append("'>")
                .append(htmlEntities(species.getGenomeSource().getName())).append("</a></td></tr>");
        table.append("<tr><th scope='row'>Genome version</th><td>")
                .append(htmlEntities(species.getGenomeVersion())).append("</td></tr>");

        table.append("</table>");
        table.append("</div>");

        return log.exit(table.toString());
    }

    private String getCallFileSection(SpeciesDataGroup speciesDataGroup) {
        log.entry(speciesDataGroup);

        RequestParameters urlDoc = this.getNewRequestParameters();
        urlDoc.setPage(RequestParameters.PAGE_DOCUMENTATION);
        urlDoc.setAction(RequestParameters.ACTION_DOC_CALL_DOWLOAD_FILES);
        
        StringBuilder text = new StringBuilder();

        text.append("<p>Bgee provides calls of presence/absence of expression. Each call " +
                "corresponds to a unique combination of a gene, an anatomical entity, " +
                "and a life stage, with reported presence or absence of expression. " +
                "More information in our <a href='").append(urlDoc.getRequestURL())
                .append("'>documentation</a></p>");
        
        text.append("<h3>Anatomical entities only</h3>");

        text.append("<ul>");
        Set<CallService.Attribute> condParam = Collections.singleton(CallService.Attribute.ANAT_ENTITY_ID);
        Optional<DownloadFile> file = this.getCallFile(speciesDataGroup, EXPR_CALLS_SIMPLE, condParam);
        text.append(getFileLi(file, "File without advanced column"));
        file = this.getCallFile(speciesDataGroup, EXPR_CALLS_COMPLETE, condParam);
        text.append(getFileLi(file, "File with advanced columns"));
        text.append("</ul>");

        text.append("<h3>Anatomical entities and developmental stages</h3>");
        text.append("<ul>");
        condParam = new HashSet<>(Arrays.asList(
                CallService.Attribute.ANAT_ENTITY_ID, CallService.Attribute.DEV_STAGE_ID));
        file = this.getCallFile(speciesDataGroup, EXPR_CALLS_SIMPLE, condParam);
        text.append(getFileLi(file, "File without advanced column"));
        file = this.getCallFile(speciesDataGroup, EXPR_CALLS_COMPLETE, condParam);
        text.append(getFileLi(file, "File with advanced columns"));
        text.append("</ul>");

        return log.exit(text.toString());
    }

    private Optional<DownloadFile> getCallFile(SpeciesDataGroup speciesDataGroup, CategoryEnum category,
                                               Set<CallService.Attribute> attrs) {
        log.entry(speciesDataGroup, category, attrs);
        
        return log.exit(speciesDataGroup.getDownloadFiles().stream()
                    .filter(f -> category.equals(f.getCategory()))
                    .filter(f -> attrs.equals(f.getConditionParameters()))
                    .findFirst());
    }

    private String getValueFileSection(SpeciesDataGroup speciesDataGroup) {
        log.entry(speciesDataGroup);

        final StringBuilder section = new StringBuilder();

        section.append("<p>Bgee provides annotations and experiment annotations, and processed expression values.</p>");

        section.append("<h3>Affymetrix</h3>");

        section.append("<ul>");
        Optional<DownloadFile> file = speciesDataGroup.getDownloadFiles().stream()
                .filter(f -> AFFY_ANNOT.equals(f.getCategory())).findFirst();
        section.append(getFileLi(file, "Experiments/chips annotations and meta data"));
        file = speciesDataGroup.getDownloadFiles().stream()
                .filter(f -> AFFY_DATA.equals(f.getCategory())).findFirst();
        section.append(getFileLi(file, "Data (signal intensities)"));
        section.append("</ul>");

        section.append("<h3>RNA-Seq</h3>");
        section.append("<ul>");
        file = speciesDataGroup.getDownloadFiles().stream()
                .filter(f -> RNASEQ_ANNOT.equals(f.getCategory())).findFirst();
        section.append(getFileLi(file, "Experiments/libraries annotations and meta data"));
        file = speciesDataGroup.getDownloadFiles().stream()
                .filter(f -> RNASEQ_DATA.equals(f.getCategory())).findFirst();
        section.append(getFileLi(file, "Data (read counts, TPMs, and FPKMs)"));
        section.append("</ul>");

        return log.exit(section.toString());
    }

    private String getFileLi(Optional<DownloadFile> file, String label) {
        log.entry(file, label);
        
        if (file.isPresent()) {
            DownloadFile downloadFile = file.get();
            return log.exit("<li><p>" + label + ": " +
                    "<a class='btn btn-default btn-xs' href='" + 
                    this.prop.getDownloadRootDirectory() + downloadFile.getPath() + "'>" + 
                    downloadFile.getName() + "</a> (" +
                    FileUtils.byteCountToDisplaySize(downloadFile.getSize()) + ")</p></li>");
        }
        return log.exit("");
    }

    @Override
    protected void includeCss() {
        log.entry();

        //If you ever add new files, you need to edit bgee-webapp/pom.xml 
        //to correctly merge/minify them.
        this.includeCss("species.css");

        //we need to add the Bgee CSS files at the end, to override CSS file from external libs
        super.includeCss();

        log.exit();
    }
}
