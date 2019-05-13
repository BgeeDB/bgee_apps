package org.bgee.view.html;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.BgeeProperties;
import org.bgee.controller.RequestParameters;
import org.bgee.view.JsonHelper;
import org.bgee.view.MultiGeneDisplay;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * This class is the HTML implementation of the {@code MultiGeneDisplay}.
 *
 * @author  Valentine Rech de Laval
 * @version Bgee 14, May 2019
 * @since   Bgee 14, May 2019
 */
public class HtmlMultiGeneDisplay extends HtmlParentDisplay implements MultiGeneDisplay {

    /**
     * {@code Logger} of the class.
     */
    private final static Logger log = LogManager.getLogger(HtmlMultiGeneDisplay.class.getName());

    /**
     * @param response          A {@code HttpServletResponse} that will be used to display
     *                          the page to the client.
     * @param requestParameters The {@code RequestParameters} that handles the parameters of
     *                          the current request.
     * @param prop              A {@code BgeeProperties} instance that contains the properties
     *                          to use.
     * @param jsonHelper        A {@code JsonHelper} used to read/write variables into JSON.
     * @param factory           The {@code HtmlFactory} that instantiated this object.
     * @throws IOException If there is an issue when trying to get or to use the {@code PrintWriter}.
     */
    public HtmlMultiGeneDisplay(HttpServletResponse response, RequestParameters requestParameters, BgeeProperties prop,
                                JsonHelper jsonHelper, HtmlFactory factory) throws IllegalArgumentException, IOException {
        super(response, requestParameters, prop, jsonHelper, factory);
    }

    @Override
    public void displayMultiGeneHomePage() {
        log.entry();
        
        this.displayMultiGenePage(null);
        
        log.exit();
    }

    @Override
    public void displayMultiGene(List<String> geneList) {
        log.entry(geneList);

        this.displayMultiGenePage(geneList);

        log.exit();
    }

    private void displayMultiGenePage(List<String> userEnsemblIds) {
        log.entry(userEnsemblIds);

        this.startDisplay("Expression in several genes");

        this.writeln("<h1>Expression of several genes</h1>");

        this.writeln("<div id='bgee_introduction'>");

        this.writeln("<p>Retrieve expression summary of several genes from Ensembl IDs.</p>");

        this.writeln("</div>");

        this.writeln(getForm(userEnsemblIds));

//        if (result != null) {
            this.writeln(getResult());
//        }

        this.endDisplay();

        log.exit();
    }

    private String getForm(List<String> userEnsemblIds) {
        log.entry(userEnsemblIds);

        StringBuilder sb = new StringBuilder();

        RequestParameters action = this.getNewRequestParameters();
        // FIXME create param
        action.setPage(RequestParameters.PAGE_MULTI_GENE);

        sb.append("<div class='row'>");
        sb.append("    <div id='bgee_multigene' class='row well well-sm col-xs-offset-2 col-xs-8 " +
                "                                  col-lg-offset-4 col-lg-4'>");
        sb.append("        <form id='bgee_multigene_form' method='post' action='")
                .append(action.getRequestURL()).append("' >");

        // Hidden parameter defining it's a POST form
        // FIXME see branch anat-sim
        //        sb.append("            <input type='hidden' name='").append(this.getRequestParameters()
        //                .getUrlParametersInstance().getParamPostFormSubmit().getName()).append("' value='1' />");

        // Gene ID list
        String idsText = userEnsemblIds == null ? "" : htmlEntities(String.join("\n", userEnsemblIds));
        sb.append("            <div class='form-group'>");
        sb.append("                <label for='bgee_gene_list' class='group-title'>" +
                "                       Gene list</label>");
        sb.append("                <textarea id='bgee_gene_list' class='form-control' name='")
                .append(this.getRequestParameters().getUrlParametersInstance()
                        .getParamGeneList().getName()).append("'" +
                "                            form='bgee_multigene_form' autofocus rows='10'" +
                "                            placeholder='Enter a list of Ensembl IDs'>")
                .append(idsText)
                .append("</textarea>");
        sb.append("            </div>");

        // Submit
        sb.append("            <input id='bgee_multigene_submit' class='col-sm-2' type='submit' value='Search'>");

        // Message
        sb.append("            <span id='bgee_multigene_msg' class='col-sm-10'></span>");

        sb.append("        </form>");
        sb.append("    </div>");
        sb.append("</div>");

        return log.exit(sb.toString());
    }

    private String getResult() {
        log.entry();

        StringBuilder sb = new StringBuilder();

        sb.append("<h2>Results</h2>");

        sb.append("<div class='table-container'>");
        sb.append("    <table class='multi_gene_expression stripe compact'>");
        sb.append("        <thead>");
        sb.append("            <tr>");
        sb.append("                <th>Anatomical entities</th>");
        sb.append("                <th>Genes count with presence of expression</th>");
        sb.append("                <th>Gene count with absence of expression</th>");
        sb.append("                <th>Species count with presence of expression</th>");
        sb.append("                <th>Species count with absence of expression</th>");
        sb.append("            </tr>");
        sb.append("        </thead>");
        sb.append("        <tbody>");
        sb.append("            <tr>");
        //FIXME use retrieved data
        sb.append("                <td>lung (UBERON:0002048), swim bladder (UBERON:0006860)</td>");
        sb.append("                <td>13</td>");
        sb.append("                <td>2</td>");
        sb.append("                <td>2</td>");
        sb.append("                <td>1</td>");
        sb.append("            </tr>");
        //        sb.append(result.getData().stream()
//                .map(sim -> getRow(sim, result))
//                .collect(Collectors.joining()));
        sb.append("        </tbody>");
        sb.append("    </table>");
        sb.append("</div>");

//        if (!result.getRequestedGeneIdsNotFound().isEmpty()) {
//            sb.append("<p>Ensembl IDs unknown: ");
//            sb.append(result.getRequestedGeneIdsNotFound().stream()
//                    .sorted()
//                    .map(id -> "'" + htmlEntities(id) + "'")
//                    .collect(Collectors.joining(" - ")));
//            sb.append("</p>");
//        }

//        if (!result.getGenesWithNoData().isEmpty()) {
//            sb.append("<p>Ensembl IDs without data: ");
//            sb.append(result.getGenesWithNoData().stream()
//                    .sorted(Comparator.comparing(Gene::getEnsemblGeneId))
//                    .map(g -> getGeneUrl(g))
//                    .collect(Collectors.joining(" - ")));
//            sb.append("</p>");
//        }

        return log.exit(sb.toString());

    }
    
    @Override
    protected void includeCss() {
        log.entry();

        //If you ever add new files, you need to edit bgee-webapp/pom.xml 
        //to correctly merge/minify them.
        if (!this.prop.isMinify()) {
            this.includeCss("lib/jquery_plugins/jquery.dataTables.min.css");
            this.includeCss("lib/jquery_plugins/responsive.dataTables.min.css");
        } else {
            this.includeCss("lib/jquery_plugins/vendor_multi_gene.css");
        }
        this.includeCss("multi_gene.css");

        //we need to add the Bgee CSS files at the end, to override CSS file from external libs
        super.includeCss();

        log.exit();
    }

    @Override
    protected void includeJs() {
        log.entry();

        super.includeJs();
        //If you ever add new files, you need to edit bgee-webapp/pom.xml 
        //to correctly merge/minify them.
        if (!this.prop.isMinify()) {
            this.includeJs("lib/jquery_plugins/jquery.dataTables.min.js");
            this.includeJs("lib/jquery_plugins/dataTables.responsive.min.js");
            this.includeJs("multi_gene.js");
        } else {
            this.includeJs("lib/jquery_plugins/vendor_multi_gene.js");
            this.includeJs("script_multi_gene.js");
        }
        log.exit();
    }
}

