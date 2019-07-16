package org.bgee.view.html;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.BgeeProperties;
import org.bgee.controller.RequestParameters;
import org.bgee.model.Entity;
import org.bgee.model.SearchResult;
import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.expressiondata.Condition;
import org.bgee.model.expressiondata.MultiGeneExprAnalysis;
import org.bgee.model.expressiondata.SingleSpeciesExprAnalysis;
import org.bgee.model.expressiondata.baseelements.ExpressionLevelInfo;
import org.bgee.model.expressiondata.baseelements.SummaryCallType.ExpressionSummary;
import org.bgee.model.expressiondata.multispecies.MultiSpeciesCondition;
import org.bgee.model.expressiondata.multispecies.MultiSpeciesExprAnalysis;
import org.bgee.model.gene.Gene;
import org.bgee.model.species.Species;
import org.bgee.view.ExpressionComparisonDisplay;
import org.bgee.view.JsonHelper;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * This class is the HTML implementation of the {@code ExpressionComparisonDisplay}.
 *
 * @author  Valentine Rech de Laval
 * @version Bgee 14, July 2019
 * @version Bgee 14, June 2019
 * @since   Bgee 14, May 2019
 */
public class HtmlExpressionComparisonDisplay extends HtmlParentDisplay
        implements ExpressionComparisonDisplay {

    /**
     * {@code Logger} of the class.
     */
    private final static Logger log = LogManager.getLogger(HtmlExpressionComparisonDisplay.class.getName());

    private final static String ENTITIES_SEPARATOR = ", ";
    
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

        this.addSchemaMarkups();

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

        RequestParameters example1 = this.getNewRequestParameters();
        example1.setPage(RequestParameters.PAGE_EXPR_COMPARISON);
        example1.setGeneList(Arrays.asList("ENSG00000139767", "ENSMUSG00000063919",
                "ENSPPAG00000028134", "ENSPTRG00000005517", "ENSBTAG00000008676",
                "ENSRNOG00000001141", "ENSSSCG00000009845", "ENSECAG00000021729",
                "ENSCAFG00000023113", "ENSOCUG00000004503", "ENSMODG00000015283",
                "ENSACAG00000004139", "ENSXETG00000019934"));
        RequestParameters example2 = this.getNewRequestParameters();
        example2.setPage(RequestParameters.PAGE_EXPR_COMPARISON);
        example2.setGeneList(Arrays.asList("ENSDARG00000059263", "ENSG00000170178", 
                "ENSMUSG00000001823"));

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
        sb.append("                <label for='bgee_gene_list' class='group-title'>Gene list</label>");
        sb.append("                <textarea id='bgee_gene_list' class='form-control' name='")
                .append(this.getRequestParameters().getUrlParametersInstance()
                        .getParamGeneList().getName()).append("'" +
                "                            form='bgee_expr_comp_form' autofocus rows='10'" +
                "                            placeholder='Enter a list of Ensembl IDs (one ID per line or separated by a comma)'>")
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
        sb.append("        <span class='examples col-sm-12'>Examples: ");
        sb.append("            <a href='").append(example1.getRequestURL()).append("'>SRRM4 (brain specific genes)</a>");
        sb.append("            <a href='").append(example2.getRequestURL()).append("'>Hoxd12 (development pattern genes)</a>");
        sb.append("        </span>");

        sb.append("    </div>");
        sb.append("</div>");

        return log.exit(sb.toString());
    }

    private <T> String getResult(SearchResult<String, Gene> searchResult, MultiGeneExprAnalysis<T> result,
            Function<T, Set<AnatEntity>> function, boolean isMultiSpecies) {
        log.entry(result, function, isMultiSpecies);

        StringBuilder sb = new StringBuilder();

        if (searchResult != null && !searchResult.getRequestElementsNotFound().isEmpty()) {
            sb.append("<p>Unknown Ensembl IDs: ");
            sb.append(searchResult.getRequestElementsNotFound().stream()
                    .sorted()
                    .map(gId -> "'" + htmlEntities(gId) + "'")
                    .collect(Collectors.joining(ENTITIES_SEPARATOR)));
            sb.append("</p>");
        }

        sb.append("<h2>Results</h2>");

        sb.append("<p>Results are ordered by 'Score', 'Genes with presence of expression' then 'Minimum rank'. " +
                "The order could be changed by clicking on one column, then press shift and click on another column.</p>");
        sb.append("<div class='table-container'>");
        String tableClass = isMultiSpecies? "multi-sp" : "single-sp";
        sb.append("    <table class='expr_comp stripe compact ").append(tableClass).append("'>");
        sb.append("        <thead>");
        sb.append("            <tr>");
        sb.append("                <th>Anatomical entities</th>");
        sb.append("                <th>Score</th>");
        sb.append("                <th>Minimum rank</th>");
        sb.append("                <th>Genes with presence of expression</th>");
        sb.append("                <th>Genes with absence of expression</th>");
        sb.append("                <th>Genes with no data</th>");
        if (isMultiSpecies) {
            sb.append("            <th>Species with presence of expression</th>");
            sb.append("            <th>Species with absence of expression</th>");
        }
        sb.append("                <th>See details</th>");
        sb.append("                <th>Anatomical entity IDs</th>");
        sb.append("                <th>Gene count with presence of expression</th>");
        sb.append("                <th>Gene count with absence of expression</th>");
        sb.append("                <th>Gene count with no data</th>");
        if (isMultiSpecies) {
            sb.append("            <th>Species count with presence of expression</th>");
            sb.append("            <th>Species count with absence of expression</th>");
        }
        sb.append("            </tr>");
        sb.append("        </thead>");
        sb.append("        <tbody>");
        sb.append(result.getCondToCounts().entrySet().stream()
                .map(e -> getRow(e, function, isMultiSpecies))
                .collect(Collectors.joining()));
        sb.append("        </tbody>");
        sb.append("    </table>");
        sb.append("</div>");

        return log.exit(sb.toString());

    }

    private <T> String getRow(Map.Entry<T, MultiGeneExprAnalysis.MultiGeneExprCounts> condToCounts,
                              Function<T, Set<AnatEntity>> function, boolean isMultiSpecies) {
        log.entry(condToCounts, function, isMultiSpecies);

        StringBuilder row = new StringBuilder();
        row.append("<tr>");
        
        List<AnatEntity> anatEntities = function.apply(condToCounts.getKey()).stream()
                .sorted(Comparator.comparing(AnatEntity::getName))
                .collect(Collectors.toList());
        
        row.append("    <td>");
        row.append(anatEntities.stream()
                .map(ae -> getAnatEntityUrl(ae, ae.getName()))
                .collect(Collectors.joining(ENTITIES_SEPARATOR)));
        row.append("    </td>");

        Map<ExpressionSummary, Set<Gene>> callTypeToGenes = condToCounts.getValue().getCallTypeToGenes();
        Set<Gene> expressedGenes = callTypeToGenes.get(ExpressionSummary.EXPRESSED);
        if (expressedGenes == null) {
            expressedGenes = new HashSet<>();
        }
        Set<Gene> notExpressedGenes = callTypeToGenes.get(ExpressionSummary.NOT_EXPRESSED);
        if (notExpressedGenes == null) {
            notExpressedGenes = new HashSet<>();
        }

        // Score
        double score = (double) Math.abs(expressedGenes.size() - notExpressedGenes.size())
                / ((double) expressedGenes.size() + notExpressedGenes.size());
        row.append("<td>").append(String.format(Locale.US, "%.2f", score)).append("</td>");
        
        // Min rank 
        Optional<ExpressionLevelInfo> collect = condToCounts.getValue().getGeneToMinRank().values().stream()
                .filter(Objects::nonNull)
                .min(Comparator.comparing(ExpressionLevelInfo::getRank,
                        Comparator.nullsLast(BigDecimal::compareTo)));
        
        row.append("<td>").append(collect.isPresent()? collect.get().getFormattedRank(): "").append("</td>");

        // Counts
        row.append(this.getGeneCountCell(expressedGenes));
        row.append(this.getGeneCountCell(notExpressedGenes));
        row.append(this.getGeneCountCell(condToCounts.getValue().getGenesWithNoData()));
        if (isMultiSpecies) {
            row.append(this.getSpeciesCountCell(expressedGenes));
            row.append(this.getSpeciesCountCell(notExpressedGenes));
        }
        
        // Expand details
        row.append("    <td><span class='expandable' title='Click to expand'>[+]</span></td>");

        // Columns for export only
        row.append("<td>").append(anatEntities.stream()
                .map(Entity::getId)
                .collect(Collectors.joining(ENTITIES_SEPARATOR))).append("</td>");
        row.append("<td>").append(expressedGenes.size()).append("</td>");
        row.append("<td>").append(notExpressedGenes.size()).append("</td>");
        row.append("<td>").append(condToCounts.getValue().getGenesWithNoData().size()).append("</td>");
        if (isMultiSpecies) {
            row.append("<td>")
                    .append(expressedGenes.stream().map(Gene::getSpecies).distinct().count())
                    .append("</td>");
            row.append("<td>")
                    .append(notExpressedGenes.stream().map(Gene::getSpecies).distinct().count())
                    .append("</td>");
        }

        row.append("</tr>");
        return log.exit(row.toString());
    }

    /**
     * Get table cell for a gene count as a HTML 'td' element.
     *
     * @param genes     A {@code Set} of {@code Gene}s that are the genes to be displayed. 
     * @return          The {@code String} that is the HTML of the cell.
     */
    private String getGeneCountCell(Set<Gene> genes) {
        log.entry(genes);

        Function<Gene, String> f = g -> {
            RequestParameters geneUrl = this.getNewRequestParameters();
            geneUrl.setPage(RequestParameters.PAGE_GENE);
            geneUrl.setGeneId(g.getEnsemblGeneId());
            geneUrl.setSpeciesId(g.getSpecies().getId());
            return "<a href='" + geneUrl.getRequestURL() + "'>" + htmlEntities(g.getEnsemblGeneId()) + "</a>" 
                    + (StringUtils.isBlank(g.getName())? "": " " + htmlEntities(g.getName()));
        };

        //Need a compiler hint of generic type for my Java version
        return log.exit(this.<Gene>getCell(genes.stream()
                        .sorted(Comparator.comparing(Gene::getEnsemblGeneId))
                        .collect(Collectors.toList()),
                "gene" + (genes.size() > 1? "s": ""),
                f));
    }

    /**
     * Get table cell for a species count as a HTML 'td' element.
     *
     * @param genes     A {@code Set} of {@code Gene}s that are the genes for which
     *                  the species should to be displayed. 
     * @return          The {@code String} that is the HTML of the cell.
     */
    private String getSpeciesCountCell(Set<Gene> genes) {
        log.entry(genes);
        //Need a compiler hint of generic type for my Java version
        return log.exit(this.<Species>getCell(genes.stream()
                        .map(Gene::getSpecies)
                        .distinct()
                        .sorted(Comparator.comparing(Species::getPreferredDisplayOrder))
                        .collect(Collectors.toList()),
                "species", s -> "<a href='" + getSpeciesPageUrl(s.getId()) + "'><em>" 
                            + htmlEntities(s.getScientificName()) + "</em></a>"));
    }

    /**
     * Get table cell as a HTML 'td' element.
     * 
     * @param set           A {@code List} of {@code T}s that are the elements to be diplayed. 
     * @param mainText      A {@code String} that is the main text of the cell.
     * @param getDetailText A {@code Function }accepting a {@code T} that is, in our case,
     *                      a {@code Gene} or a {@code Species}, and returning
     *                      a {@code String} to retrieve the text of details to be displayed.
     * @param <T>           The type of elements in {@code set}.
     * @return              The {@code String} that is the HTML of the cell.
     */
    private <T> String getCell(List<T> set, String mainText, Function<T, String> getDetailText) {
        log.entry(set, mainText, getDetailText);

        StringBuilder cell = new StringBuilder();

        cell.append("<td>");
        cell.append(     set.size()).append(" ").append(mainText);
        cell.append("    <ul class='masked'>");
        for (T element: set) {
            cell.append("    <li class='gene'>");
            cell.append("        <span class='details small'>")
                    .append(         getDetailText.apply(element))
                    .append(    "</span>");
            cell.append("    </li>");
        }
        cell.append("    </ul>");
        cell.append("</td>");

        return log.exit(cell.toString());
    }

    /**
     * Add schema.org markups to the page.
     */
    private void addSchemaMarkups() {
        log.entry();

        RequestParameters url = this.getNewRequestParameters();
        url.setPage(RequestParameters.PAGE_EXPR_COMPARISON);

        this.writeln("<script type='application/ld+json'>");

        this.writeln("{" +
                "  \"@context\": \"https://schema.org\"," +
                "  \"@type\": \"WebApplication\"," +
                "  \"@id\": \"" + url.getRequestURL() + "\"," +
                "  \"name\": \"Expression comparison\"," +
                "  \"url\": \"" + url.getRequestURL() + "\"," +
                "  \"description\": \"Compare expression from several genes\"," +
                "  \"offers\": {" +
                "    \"@type\": \"Offer\"," +
                "    \"price\": \"0.00\"," +
                "    \"priceCurrency\": \"CHF\"" +
                "  }, " +
                "  \"applicationCategory\": \"https://www.wikidata.org/wiki/Q15544757\"" + // science software
                "}");

        this.writeln("</script>");
        
        log.exit();
    }

    @Override
    protected void includeCss() {
        log.entry();

        //If you ever add new files, you need to edit bgee-webapp/pom.xml 
        //to correctly merge/minify them.
        if (!this.prop.isMinify()) {
            this.includeCss("lib/jquery_plugins/jquery.dataTables.min.css");
            this.includeCss("lib/jquery_plugins/responsive.dataTables.min.css");
            this.includeCss("lib/jquery_plugins/buttons.dataTables.min.css");
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
            this.includeJs("lib/jquery_plugins/dataTables.buttons.min.js");
            this.includeJs("lib/jquery_plugins/buttons.html5.min.js");
            this.includeJs("lib/jquery_plugins/jszip.min.js");
            this.includeJs("expr_comp.js");
        } else {
            this.includeJs("lib/jquery_plugins/vendor_expr_comp.js");
            this.includeJs("script_expr_comp.js");
        }
        log.exit();
    }
}

