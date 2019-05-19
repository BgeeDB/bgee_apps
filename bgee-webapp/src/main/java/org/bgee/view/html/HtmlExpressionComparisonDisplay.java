package org.bgee.view.html;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.BgeeProperties;
import org.bgee.controller.RequestParameters;
import org.bgee.model.SearchResult;
import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.expressiondata.Condition;
import org.bgee.model.expressiondata.MultiGeneExprAnalysis;
import org.bgee.model.expressiondata.SingleSpeciesExprAnalysis;
import org.bgee.model.expressiondata.baseelements.SummaryCallType;
import org.bgee.model.expressiondata.baseelements.SummaryCallType.ExpressionSummary;
import org.bgee.model.expressiondata.multispecies.MultiSpeciesCondition;
import org.bgee.model.expressiondata.multispecies.MultiSpeciesExprAnalysis;
import org.bgee.model.gene.Gene;
import org.bgee.model.species.Species;
import org.bgee.view.ExpressionComparisonDisplay;
import org.bgee.view.JsonHelper;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * This class is the HTML implementation of the {@code ExpressionComparisonDisplay}.
 *
 * @author  Valentine Rech de Laval
 * @version Bgee 14, May 2019
 * @since   Bgee 14, May 2019
 */
public class HtmlExpressionComparisonDisplay extends HtmlParentDisplay
        implements ExpressionComparisonDisplay {

    /**
     * {@code Logger} of the class.
     */
    private final static Logger log = LogManager.getLogger(HtmlExpressionComparisonDisplay.class.getName());

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
    public HtmlExpressionComparisonDisplay(HttpServletResponse response, RequestParameters requestParameters,
                                           BgeeProperties prop, JsonHelper jsonHelper, HtmlFactory factory)
            throws IllegalArgumentException, IOException {
        super(response, requestParameters, prop, jsonHelper, factory);
    }

    @Override
    public void displayExpressionComparisonHomePage() {
        log.entry();
        
        this.displayExpressionComparison(null, null, null, null, null);
        
        log.exit();
    }
    
    public void displayExpressionComparison(String errorMsg) {
        log.entry(errorMsg);

        this.displayExpressionComparison(null, null, null, null, errorMsg);

        log.exit();
    }
    
    @Override
    public void displayExpressionComparison(SearchResult<String, Gene> searchResult, SingleSpeciesExprAnalysis result) {
        log.entry(searchResult, result);

        Function<Condition, Set<AnatEntity>> fun = c -> Collections.singleton(c.getAnatEntity());
        this.displayExpressionComparison(searchResult, result, fun, false, null);

        log.exit();
    }

    @Override
    public void displayExpressionComparison(SearchResult<String, Gene> searchResult, MultiSpeciesExprAnalysis result) {
        log.entry(searchResult, result);

        Function<MultiSpeciesCondition, Set<AnatEntity>> fun = msc -> msc.getAnatSimilarity().getSourceAnatEntities();
        this.displayExpressionComparison(searchResult, result, fun, true, null);

        log.exit();
    }

    private <T> void displayExpressionComparison(SearchResult<String, Gene> searchResult, MultiGeneExprAnalysis<T> result,
                                                 Function<T, Set<AnatEntity>> function, Boolean isMultiSpecies,
                                                 String errorMsg) {
        log.entry(searchResult, result, function, isMultiSpecies, errorMsg);
        

        this.startDisplay("Expression comparison page");

        this.writeln("<h1>Expression comparison</h1>");

        this.writeln("<div id='bgee_introduction'>");

        this.writeln("<p>Compare expression from several genes.</p>");

        this.writeln("</div>");

        this.writeln(this.getForm(errorMsg));

        if (result != null) {
            if (isMultiSpecies == null) {
                throw log.throwing(new IllegalArgumentException(
                        "If the result should be displayed, we should know if it concerns single or multiple species."));
            }
            this.writeln(this.getResult(searchResult, result, function, isMultiSpecies));
        }

        this.endDisplay();

        log.exit();
    }

    private String getForm(String errorMsg) {
        log.entry(errorMsg);

        StringBuilder sb = new StringBuilder();

        RequestParameters action = this.getNewRequestParameters();
        action.setPage(RequestParameters.PAGE_EXPR_COMPARISON);

        sb.append("<div class='row'>");
        sb.append("    <div id='bgee_expr_comp' class='row well well-sm col-xs-offset-2 col-xs-8 " +
                "                                  col-lg-offset-4 col-lg-4'>");
        sb.append("        <form id='bgee_expr_comp_form' method='post' action='")
                .append(action.getRequestURL()).append("' >");

        // Hidden parameter defining it's a POST form
        sb.append("            <input type='hidden' name='")
                .append(this.getRequestParameters()
                        .getUrlParametersInstance().getParamPostFormSubmit().getName())
                .append("' value='1' />");

        // Gene ID list
        String idsText = this.getRequestParameters().getGeneList() == null ? "" :
            htmlEntities(String.join("\n", this.getRequestParameters().getGeneList()));
        sb.append("            <div class='form-group'>");
        sb.append("                <label for='bgee_gene_list' class='group-title'>" +
                "                       Gene list</label>");
        sb.append("                <textarea id='bgee_gene_list' class='form-control' name='")
                .append(this.getRequestParameters().getUrlParametersInstance()
                        .getParamGeneList().getName()).append("'" +
                "                            form='bgee_expr_comp_form' autofocus rows='10'" +
                "                            placeholder='Enter a list of Ensembl IDs'>")
                .append(idsText).append("</textarea>");
        sb.append("            </div>");

        // Submit
        sb.append("            <input id='bgee_expr_comp_submit' type='submit' value='Search'>");

        // Message

        String msg = StringUtils.isBlank(errorMsg) ? "" : htmlEntities(errorMsg);
        String spanClass = StringUtils.isBlank(errorMsg) ? "" : "class='errorMessage'";
        sb.append("            <span id='bgee_expr_comp_msg' ").append(spanClass).append(">")
                .append(msg).append("</span>");

        sb.append("        </form>");
        sb.append("    </div>");
        sb.append("</div>");

        return log.exit(sb.toString());
    }

    private <T> String getResult(SearchResult<String, Gene> searchResult, MultiGeneExprAnalysis<T> result,
            Function<T, Set<AnatEntity>> function, boolean isMultiSpecies) {
        log.entry(result, function, isMultiSpecies);

        StringBuilder sb = new StringBuilder();

        sb.append("<h2>Results</h2>");

        sb.append("<div class='table-container'>");
        String tableClass = isMultiSpecies? "multi-sp" : "single-sp";
        sb.append("    <table class='expr_comp stripe compact ").append(tableClass).append("'>");
        sb.append("        <thead>");
        sb.append("            <tr>");
        sb.append("                <th>Anatomical entities</th>");
        sb.append("                <th>Genes count with presence of expression</th>");
        sb.append("                <th>Gene count with absence of expression</th>");
        sb.append("                <th>Gene count with no data</th>");
        if (isMultiSpecies) {
            sb.append("            <th>Species count with presence of expression</th>");
            sb.append("            <th>Species count with absence of expression</th>");
            sb.append("            <th>Species count with no data</th>");
        }
        sb.append("                <th>See details</th>");
        sb.append("            </tr>");
        sb.append("        </thead>");
        sb.append("        <tbody>");
        sb.append(result.getCondToCounts().entrySet().stream()
                .map(e -> getRow(e, function, isMultiSpecies))
                .collect(Collectors.joining()));
        sb.append("        </tbody>");
        sb.append("    </table>");
        sb.append("</div>");

        if (searchResult != null && !searchResult.getRequestElementsNotFound().isEmpty()) {
            sb.append("<p>Unknown Ensembl IDs: ");
            sb.append(searchResult.getRequestElementsNotFound().stream()
                    .sorted()
                    .map(gId -> "'" + htmlEntities(gId) + "'")
                    .collect(Collectors.joining(" - ")));
            sb.append("</p>");
        }

        return log.exit(sb.toString());

    }

    private <T> String getRow(Map.Entry<T, MultiGeneExprAnalysis.MultiGeneExprCounts> condToCounts,
                              Function<T, Set<AnatEntity>> function, boolean isMultiSpecies) {
        log.entry(condToCounts, function, isMultiSpecies);

        StringBuilder row = new StringBuilder();
        row.append("<tr>");
        
        row.append("    <td>");
        row.append(function.apply(condToCounts.getKey()).stream()
                .sorted(Comparator.comparing(AnatEntity::getName))
                .map(ae -> getAnatEntityUrl(ae, ae.getName() + " (" + ae.getId() + ")"))
                .collect(Collectors.joining(" - ")));
        row.append("    </td>");

        Map<SummaryCallType, Set<Gene>> callTypeToGenes = condToCounts.getValue().getCallTypeToGenes();
        Set<Gene> expressedGenes = callTypeToGenes.get(ExpressionSummary.EXPRESSED);
        Set<Gene> notExpressedGenes = callTypeToGenes.get(ExpressionSummary.NOT_EXPRESSED);
        
        row.append(this.getGeneCountCell(expressedGenes));
        row.append(this.getGeneCountCell(notExpressedGenes));
        row.append(this.getGeneCountCell(condToCounts.getValue().getGenesWithNoData()));
        if (isMultiSpecies) {
            row.append(this.getSpeciesCountCell(expressedGenes));
            row.append(this.getSpeciesCountCell(notExpressedGenes));
            row.append(this.getSpeciesCountCell(condToCounts.getValue().getGenesWithNoData()));
        }
        
        row.append("    <td><span class='expandable' title='Click to expand'>[+]</span></td>");
        row.append("</tr>");
        return log.exit(row.toString());
    }

    private String getGeneCountCell(Set<Gene> genes) {
        log.entry(genes);

        Function<Gene, String> f = g -> {
            RequestParameters geneUrl = this.getNewRequestParameters();
            geneUrl.setPage(RequestParameters.PAGE_GENE);
            geneUrl.setGeneId(g.getEnsemblGeneId());
            geneUrl.setSpeciesId(g.getSpecies().getId());
            return "<a href='" + geneUrl.getRequestURL() + "'>" + htmlEntities(g.getEnsemblGeneId()) + "</a>";
        };

        //Need a compiler hint of generic type for my Java version
        return log.exit(this.<Gene>getCell(genes.stream()
                        .sorted(Comparator.comparing(Gene::getEnsemblGeneId))
                        .collect(Collectors.toList()),
                "gene" + (genes.size() > 1? "s": ""),
                f, g -> StringUtils.isBlank(g.getName())? "": htmlEntities(g.getName())));
    }

    private String getSpeciesCountCell(Set<Gene> genes) {
        log.entry(genes);
        //Need a compiler hint of generic type for my Java version
        return log.exit(this.<Species>getCell(genes.stream()
                        .map(Gene::getSpecies)
                        .distinct()
                        .sorted(Comparator.comparing(Species::getPreferredDisplayOrder))
                        .collect(Collectors.toList()),
                "species", s -> String.valueOf(s.getId()),
                s -> "<em>" + htmlEntities(s.getScientificName()) + "</em>"));
    }
    
    private <T> String getCell(List<T> set, String text, Function<T, String> getMainText,
                               Function<T, String> getOptionalText) {
        log.entry(set, text, getMainText, getOptionalText);

        StringBuilder cell = new StringBuilder();

        cell.append("<td>");
        cell.append(     set.size()).append(" ").append(text);
        cell.append("    <ul class='masked'>");
        for (T element: set) {
            String optional = getOptionalText.apply(element);
            cell.append("    <li class='gene'>");
            cell.append("        <span class='details small'>")
                    .append(getMainText.apply(element)).append("</span>")
                    .append(StringUtils.isBlank(optional)? "": " " + optional);
            cell.append("    </li>");
        }
        cell.append("    </ul>");
        cell.append("</td>");

        return log.exit(cell.toString());
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
            this.includeCss("lib/jquery_plugins/vendor_expr_comp.css");
        }
        this.includeCss("expr_comp.css");

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
            this.includeJs("expr_comp.js");
        } else {
            this.includeJs("lib/jquery_plugins/vendor_expr_comp.js");
            this.includeJs("script_expr_comp.js");
        }
        log.exit();
    }
}

