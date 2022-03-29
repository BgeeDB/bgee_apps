package org.bgee.view.html;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.BgeeProperties;
import org.bgee.controller.RequestParameters;
import org.bgee.model.expressiondata.rawdata.*;
import org.bgee.model.expressiondata.rawdata.insitu.InSituEvidence;
import org.bgee.model.expressiondata.rawdata.insitu.InSituExperiment;
import org.bgee.model.expressiondata.rawdata.insitu.InSituSpot;
import org.bgee.model.expressiondata.rawdata.microarray.AffymetrixChip;
import org.bgee.model.expressiondata.rawdata.microarray.AffymetrixExperiment;
import org.bgee.model.expressiondata.rawdata.microarray.AffymetrixProbeset;
import org.bgee.model.gene.Gene;
import org.bgee.model.source.Source;
import org.bgee.view.JsonHelper;
import org.bgee.view.RawDataDisplay;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class is the HTML implementation of the {@code RawDataDisplay}.
 *
 * @author  Valentine Rech de Laval
 * @version Bgee 14, Sept. 2018
 * @since   Bgee 14, Aug. 2018
 */
public class HtmlRawDataDisplay extends HtmlParentDisplay implements RawDataDisplay {

    private final static Logger log = LogManager.getLogger(HtmlRawDataDisplay.class.getName());

    /**
     * @param response             A {@code HttpServletResponse} that will be used to display 
     *                             the page to the client.
     * @param requestParameters    The {@code RequestParameters} that handles the parameters of
     *                             the current request.
     * @param prop                 A {@code BgeeProperties} instance that contains the properties
     *                             to use.
     * @param jsonHelper           A {@code JsonHelper} used to read/write variables into JSON. 
     * @param factory              The {@code HtmlFactory} that instantiated this object.
     * @throws IllegalArgumentException If {@code factory} is {@code null}.
     * @throws IOException              If there is an issue when trying to get or to use the
     *                                  {@code PrintWriter} 
     */
    public HtmlRawDataDisplay(HttpServletResponse response, RequestParameters requestParameters,
                              BgeeProperties prop, JsonHelper jsonHelper, HtmlFactory factory)
            throws IllegalArgumentException, IOException {
        super(response, requestParameters, prop, jsonHelper, factory);
    }

    @Override
    public void displayRawCallHomePage() {
        log.traceEntry();
        
        // TODO add schema.org properties 
        
        this.startDisplay("Raw data information");

        this.writeln("<h1>Raw data search</h1>");

        this.writeln("<div id='bgee_introduction'>");

        this.writeln("<p>Search for raw data based on gene IDs, anatomical entity," +
                " and/or developmental stage.<p>");

        this.writeln("</div>");

        this.writeln(this.getRawDataSearchBox());

        this.endDisplay();
        log.traceExit();
    }

    /**
     * Get the search box of raw data as a HTML 'div' element. 
     *
     * @return  the {@code String} that is the search box as HTML 'div' element.
     */
    private String getRawDataSearchBox() {
        
        String searchClass = "col-xs-offset-1 col-xs-10 "
                + "col-md-offset-2 col-md-8 "
                + "col-lg-offset-3 col-lg-6";

        StringBuilder box = new StringBuilder();
        box.append("<div class='row'>");
        box.append("    <div id='bgee_raw_data_search' class='row well well-sm ").append(searchClass).append("'>");
        box.append("        <form>");
        box.append("            <div class='form-group'>");
        box.append("                <label for='bgee_gene_completion_box'>Gene</label>");
        box.append("                <input id='bgee_gene_completion_box' class='form-control' " +
                "                          type='text' placeholder='Gene'/>");
        box.append("            </div>");
        box.append("            <div class='form-group'>");
        box.append("                <label for='bgee_anat_entity_completion_box'>Anatomical entity</label>");
        box.append("                <input id='bgee_anat_entity_completion_box' class='form-control' " +
                "                          type='text' placeholder='Anatomical entity'/>");
        box.append("            </div>");
        box.append("            <div class='form-group'>");
        box.append("                <label for='bgee_dev_stage_completion_box'>Developmental stage</label>");
        box.append("                <input id='bgee_dev_stage_completion_box' class='form-control' " +
                "                          type='text' placeholder='Developmental stage'/>");
        box.append("            </div>");
        box.append("            <button id='raw_data_search_button' class='btn btn-default' type='submit'>");
        box.append("                <i class='glyphicon glyphicon-search'></i>&nbsp;Search");
        box.append("            </button>");
        box.append("        </form>");
        box.append("    </div>");
        box.append("</div>");

        return log.traceExit(box.toString());
    }

    @Override
//    public void displayRawCallPage(Stream<AffymetrixProbeset> affymetrixProbesets, Stream<RNASeqResult> rnaSeqResults, Stream<InSituSpot> inSituSpots, Stream<EST> ests) {
    public void displayRawCallPage(Stream<AffymetrixProbeset> affymetrixProbesets, Stream<InSituSpot> inSituSpots) {
        log.entry(affymetrixProbesets, inSituSpots);

        // TODO add schema.org properties 

        this.startDisplay("Bgee raw data");

        // FIXME to be build from user search
        String filter = "[provided filters]";
        
        this.writeln("<h1>Raw data retrieved for " + filter + "</h1>");

        this.writeln("<div id='bgee_introduction'>");
        this.writeln("    <p>This page provides ...</p>");
        this.writeln("    <p>Table of contents according to container...</p>");
        // XXX: Here need methods such as container.hasAffy(), container.hasRNASeq()...?
        this.writeln("</div>");

        this.writeAffymetrixArticle(filter, affymetrixProbesets);
        
        this.writeInSituArticle(filter, inSituSpots);

        this.writeln(this.getRawDataSearchBox());

        this.endDisplay();
        
        log.traceExit();
    }

    private void writeAffymetrixArticle(String filter, Stream<AffymetrixProbeset> affymetrixProbesets) {
        log.entry(filter, affymetrixProbesets);

        Iterator<AffymetrixProbeset> iterator = affymetrixProbesets.iterator();

        if (!iterator.hasNext()) {
            return;
        }

        this.writeln("<article id='affy-list'>");

        this.writeln("  <header><h2>Affymetrix experiments for " + filter + "</h2></header>");

        // TODO add collapse/expand all button in JS
        // https://stackoverflow.com/questions/22057505/bootstrap-collapse-expand-all
        // $('#accordion .panel-default').on('click', function () {
        //    $('#accordion .panel-collapse').collapse('toggle');
        //});

        // Stream is ordered by experiment/assay/gene.
        // So we do not need to read all the stream and to group to display them correctly
        String previousAssayId = null;
        String previousExpId = null;
        AffymetrixProbeset probeset = iterator.next();
        while (probeset != null) {
            AffymetrixChip assay = probeset.getAssay();
            AffymetrixExperiment experiment = assay.getExperiment();

            this.writeExpTableStart(experiment, previousExpId, "Affymetrix experiments", "Chips");

            this.writeAnnotatedAssayTableStart(assay, previousAssayId, 
                    Arrays.asList("Probeset ID", "Mapped to gene", "Detection flag",
                            "Quality", "Exclusion reason"));
            
            this.writeln("<tr>");

            RawCall rawCall = probeset.getRawCall();

            this.writeln("  <td>" + probeset.getId() + "</td>");
            this.writeln("  <td>" + this.getGeneLink(rawCall.getGene()) + "</td>");
            this.writeln("  <td>" + rawCall.getDetectionFlag() + "</td>");
            this.writeln("  <td>" + rawCall.getExpressionConfidence() + "</td>");
            this.writeln("  <td>");
            if (!RawCall.ExclusionReason.NOT_EXCLUDED.equals(rawCall.getExclusionReason())) {
                this.writeln(rawCall.getExclusionReason().getStringRepresentation());
            }
            this.writeln("  </td>");

            this.writeln("</tr>");

            previousAssayId = assay.getId();
            previousExpId = experiment.getId();
            if (iterator.hasNext()) {
                probeset = iterator.next();
            } else {
                probeset = null;
            }
            if (probeset == null || !probeset.getAssay().getId().equals(previousAssayId)) {
                this.writeAssayTableEnd();
            }
            if (probeset == null || !probeset.getAssay().getExperiment().getId().equals(previousExpId)) {
                this.writeExpTableEnd();
            }
        }

        this.writeln("</article>");

        log.traceExit();
    }
    private void writeInSituArticle(String filter, Stream<InSituSpot> inSituSpots) {
        log.entry(filter, inSituSpots);

        Iterator<InSituSpot> iterator = inSituSpots.iterator();

        if (!iterator.hasNext()) {
            return;
        }

        this.writeln("<article id='in-situ-list'>");

        this.writeln("  <header><h2>In-situ experiments for " + filter + "</h2></header>");

        // TODO add collapse/expand all button in JS
        // https://stackoverflow.com/questions/22057505/bootstrap-collapse-expand-all
        // $('#accordion .panel-default').on('click', function () {
        //    $('#accordion .panel-collapse').collapse('toggle');
        //});

        // Stream is ordered by experiment/assay/gene.
        // So we do not need to read all the stream and to group to display them correctly
        String previousAssayId = null;
        String previousExpId = null;
        InSituSpot spot = iterator.next();
        while (spot != null) {
            InSituEvidence assay = spot.getAssay();
            InSituExperiment experiment = assay.getExperiment();

            this.writeExpTableStart(experiment, previousExpId, "<em>In-situ</em> experiments", "Evidences");

            if (!assay.getId().equals(previousAssayId)) {
                this.writeln("<tr class='row'>");
                this.writeln("   <td class='col-xs-1'>" + assay.getId() + "</td>");
                this.writeln("   <td class='col-xs-11'>");
                this.writeln("     <table class='call-source'>");
                this.writeln("       <thead>");
                this.writeln("         <tr>");
                this.writeln("           <th scope='col'>Anatomical entity</th>");
                this.writeln("           <th scope='col'>Developmental stage</th>");
                this.writeln("           <th scope='col'>Gene ID</th>");
                this.writeln("           <th scope='col'>Quality</th>");
                this.writeln("         </tr>");
                this.writeln("       </thead>");
                this.writeln("     <tbody>");
            }

            this.writeln("<tr>");
            RawDataAnnotation annotation = spot.getAnnotation();

            RawCall rawCall = spot.getRawCall();

            this.writeln("  <td>" + this.getAnatEntityField(annotation) + "</td>");
            this.writeln("  <td>" + this.getDevStageField(annotation) + "</td>");
            this.writeln("  <td>" + this.getGeneLink(rawCall.getGene()) + "</td>");
            this.writeln("  <td>" + rawCall.getExpressionConfidence() + "</td>");
            this.writeln("</tr>");

            previousAssayId = assay.getId();
            previousExpId = experiment.getId();
            if (iterator.hasNext()) {
                spot = iterator.next();
            } else {
                spot = null;
            }
            if (spot == null || !spot.getAssay().getId().equals(previousAssayId)) {
                this.writeln("      </tbody>");
                this.writeln("    </table>");  // close call-source
                this.writeln("  </td>");
                this.writeln("</tr>");
            }
            if (spot == null || !spot.getAssay().getExperiment().getId().equals(previousExpId)) {
                this.writeExpTableEnd();
            }
        }

        this.writeln("</article>");

        log.traceExit();
    }

    private void writeExpTableStart(Experiment<?> experiment, String previousExpId, String expTitle, String assayName) {
        log.entry(experiment, previousExpId, expTitle);

        if (experiment.getId().equals(previousExpId)) {
            log.traceExit();
            return;
        }

        this.writeln("<section>");
        this.writeln("  <header><h3>" + expTitle + " " + experiment.getId() + "</h3></header>");
        this.writeln("  <table class='experiment'>");
        this.writeln("    <tr class='row'>");
        this.writeln("      <th scope='row' class='col-xs-1'>Name</th>");
        this.writeln("      <td  class='col-xs-11'>" + experiment.getName() + "</td>");
        this.writeln("    </tr>");
        this.writeln("    <tr class='row'>");
        this.writeln("      <th scope='row' class='col-xs-1'>Description</th>");
        this.writeln("      <td class='col-xs-11'>" + experiment.getDescription() + "</td>");
        this.writeln("    </tr>");
        this.writeln("    <tr class='row'>");
        this.writeln("      <th scope='row' class='col-xs-1'>Source</th>");
        this.writeln("      <td class='col-xs-11'>" + this.getSourceExperimentField(experiment) + "</td>");
        this.writeln("    </tr>");
        this.writeln("    <tr class='row'>");
        this.writeln("      <th scope='row' class='col-xs-1'>" + assayName + "</th>");
        this.writeln("      <td class='col-xs-11'>");
        this.writeln("        <table class='assay'>");

        log.traceExit();
    }

    private void writeExpTableEnd() {
        log.traceEntry();
        
        this.writeln("        </table>"); // close table assay
        this.writeln("      </td>");
        this.writeln("    </tr>");
        this.writeln("  </table>"); // close table experiment
        this.writeln("</section>");
        
        log.traceExit();
    }
    
    private void writeAnnotatedAssayTableStart(AffymetrixChip assay, String previousAssayId,
                                               List<String> callSourceColNames) {
        log.entry(assay, previousAssayId, callSourceColNames);

        if (assay.getId().equals(previousAssayId)) {
            log.traceExit();
            return;
        }

        RawDataAnnotation annotation = assay.getAnnotation();
        this.writeln("<tr class='row'>");
        this.writeln("  <td class='col-xs-2'>" + this.getSourceEvidenceLink(
                annotation.getAnnotationSource(), String.valueOf(assay.getId())));
        this.writeln("  </td>");
        this.writeln("  <td class='col-xs-10'> ");
        this.writeln("    <table class='call-source-group'>");
        this.writeln("      <tr class='row'>");
        this.writeln("        <th scope='row' class='annotation col-xs-5'>Anatomical entity</th>");
        this.writeln("        <td class='col-xs-7'>" + this.getAnatEntityField(annotation) + "</td>");
        this.writeln("      </tr>");
        this.writeln("      <tr class='row'>");
        this.writeln("        <th scope='row' class='annotation col-xs-5'>Developmental stage</th>");
        this.writeln("        <td class='col-xs-7'>" + this.getDevStageField(annotation) + "</td>");
        this.writeln("      </tr>");
        this.writeln("      <tr class='row'>");
        this.writeln("        <th scope='row' class='annotation col-xs-5'>Annotated by</th>");
        this.writeln("        <td class='col-xs-7'>" + annotation.getAnnotationSource().getName() + "</td>");
        this.writeln("      </tr>");
        this.writeln("      <tr class='row'>");
        this.writeln("        <table class='call-source'>");
        this.writeln("          <thead>");
        this.writeln("            <tr>");
        this.writeln(               callSourceColNames.stream()
                                                .map(n -> "<th scope='col'>"+n+"</th>")
                                                .collect(Collectors.joining()));
        this.writeln("            </tr>");
        this.writeln("          </thead>");
        this.writeln("          <tbody>");

        log.traceExit();
    }
    
    private void writeAssayTableEnd() {
        log.traceEntry();
        
        this.writeln("          </tbody>");
        this.writeln("        </table>");  // close call-source
        this.writeln("      </tr>");
        this.writeln("    </table>"); // close call-source-group
        this.writeln("  </td>");
        this.writeln("</tr>");
        
        log.traceExit();
    }
    
    private String getGeneLink(Gene gene) {
        log.entry(gene);
        
        RequestParameters urlGenePage = this.getNewRequestParameters();
        urlGenePage.setPage(RequestParameters.PAGE_GENE);
        urlGenePage.setGeneId(gene.getGeneId());
        urlGenePage.setSpeciesId(gene.getSpecies().getId());
        
        return log.traceExit("<a href='" + urlGenePage.getRequestURL() + "'>" + gene.getGeneId() + "</a>");
    }


    private String getAnatEntityField(RawDataAnnotation annotation) {
        return annotation.getRawDataCondition().getAnatEntity().getName() + 
                " [<a href='http://purl.obolibrary.org/obo/" + 
                this.urlEncode(annotation.getRawDataCondition().getAnatEntityId().replace(':', '_'))
                + "' target='_blank' rel='noopener'>" +annotation.getRawDataCondition().getAnatEntityId() + "</a>]";
    }

    private String getDevStageField(RawDataAnnotation annotation) {
        log.entry(annotation);
        // FIXME define dev. stage url        
        return log.traceExit(annotation.getRawDataCondition().getDevStage().getName() +
                " [<a href='FIXME" +
                this.urlEncode(annotation.getRawDataCondition().getDevStageId().replace(':', '_'))
                + "' target='_blank' rel='noopener'>" +annotation.getRawDataCondition().getDevStageId() + "</a>]");
    }

    private String getSourceEvidenceLink(Source source, String id) {
        log.entry(source, id);
        return log.traceExit("<a href='" + source.getEvidenceUrl().replace("[evidence_id]", String.valueOf(id))
                + "' target='_blank' rel='noopener'>" + id + "</a>");
    }
    
    private String getSourceExperimentField(Experiment exp) {
        log.entry(exp);
        return log.traceExit(exp.getDataSource().getName() + " [<a href='" + 
                exp.getDataSource().getExperimentUrl().replace("[experiment_id]", String.valueOf(exp.getId()))
                + "' target='_blank' rel='noopener'>" + exp.getId() + "</a>]");
    }
    
    @Override
    protected void includeCss() {
        log.traceEntry();

        this.includeCss("raw_data.css");

        //we need to add the Bgee CSS files at the end, to override CSS file from external libs
        super.includeCss();

        log.traceExit();
    }

}
