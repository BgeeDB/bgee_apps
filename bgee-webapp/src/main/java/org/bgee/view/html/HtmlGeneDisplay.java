package org.bgee.view.html;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.BgeeProperties;
import org.bgee.controller.CommandGene.GeneExpressionResponse;
import org.bgee.controller.CommandGene.GeneResponse;
import org.bgee.controller.RequestParameters;
import org.bgee.controller.URLParameters;
import org.bgee.model.XRef;
import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.anatdev.DevStage;
import org.bgee.model.dao.api.expressiondata.ConditionDAO;
import org.bgee.model.expressiondata.Call.ExpressionCall;
import org.bgee.model.expressiondata.CallData.ExpressionCallData;
import org.bgee.model.expressiondata.CallService;
import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.model.expressiondata.baseelements.SummaryQuality;
import org.bgee.model.expressiondata.baseelements.SummaryCallType.ExpressionSummary;
import org.bgee.model.gene.Gene;
import org.bgee.model.gene.GeneHomologs;
import org.bgee.model.search.SearchMatch;
import org.bgee.model.search.SearchMatchResult;
import org.bgee.model.source.Source;
import org.bgee.model.species.Taxon;
import org.bgee.view.GeneDisplay;
import org.bgee.view.JsonHelper;


/**
 * This class is the HTML implementation of the {@code GeneDisplay}.
 * 
 * @author  Philippe Moret
 * @author  Valentine Rech de Laval
 * @author  Frederic Bastian
 * @author  Julien Wollbrett
 * @version Bgee 15.0, Jun. 2021
 * @since   Bgee 13, Oct. 2015
 */
public class HtmlGeneDisplay extends HtmlParentDisplay implements GeneDisplay {
    private final static Logger log = LogManager.getLogger(HtmlGeneDisplay.class.getName());

    private final static int MAX_DISPLAYED_ITEMS = 10;

    private final static Comparator<XRef> X_REF_COMPARATOR = Comparator
            .<XRef, Integer>comparing(x -> x.getSource().getDisplayOrder(), Comparator.nullsLast(Integer::compareTo))
            .thenComparing(x -> x.getSource().getName(), Comparator.nullsLast(String::compareTo))
            .thenComparing((XRef::getXRefId), Comparator.nullsLast(String::compareTo));
    
    private final static Comparator<Gene> GENE_HOMOLOGY_COMPARATOR = Comparator
            .<Gene, Integer>comparing(x -> x.getSpecies().getPreferredDisplayOrder(), Comparator.nullsLast(Integer::compareTo))
            .thenComparing(x -> x.getGeneId(), Comparator.nullsLast(String::compareTo));
    
    /**
     * @param response             A {@code HttpServletResponse} that will be used to display 
     *                             the page to the client.
     * @param requestParameters    The {@code RequestParameters} that handles the parameters of
     *                             the current request.
     * @param prop                 A {@code BgeeProperties} instance that contains the properties
     *                             to use.
     * @param jsonHelper           A {@code JsonHelper} used to read/write variables into JSON. 
     * @param factory              The {@code HtmlFactory} that instantiated this object.
     * @throws IOException         If there is an issue when trying to get or to use the {@code PrintWriter}.
     */
    public HtmlGeneDisplay(HttpServletResponse response, RequestParameters requestParameters, BgeeProperties prop,
            JsonHelper jsonHelper, HtmlFactory factory) throws IllegalArgumentException, IOException {
        super(response, requestParameters, prop, jsonHelper, factory);
    }

    /*Genes: the terms you enter are searched in gene IDs, names, and synonyms.*/
    @Override
    public void displayGeneHomePage() {
        log.traceEntry();
        this.displayGeneSearchPage(null, null);
        log.traceExit();
    }

    @Override
    public void displayGeneSearchResult(String searchTerm, SearchMatchResult<Gene> result) {
        log.traceEntry("{}, {}", searchTerm, result);
        this.displayGeneSearchPage(searchTerm, result);
        log.traceExit();
    }

    private void displayGeneSearchPage(String searchTerm, SearchMatchResult<Gene> result) {
        log.traceEntry("{}, {}", searchTerm, result);
        String geneSearchDescription = null;
        if(searchTerm != null) {
            geneSearchDescription = "Genes matching " + htmlEntities(searchTerm) + "in Bgee";
        }
        this.startDisplay("Gene information", "WebPage", geneSearchDescription);
        
        this.writeln("<h1 property='schema:name'>Gene search</h1>");

        this.writeln("<div id='bgee_introduction' property='schema:description'>");
        this.writeln("<p>Search for genes based on gene IDs, gene names, " +
                "gene descriptions, synonyms and cross-references.</p>");
        this.writeln("</div>");

        this.writeln(this.getGeneSearchBox(false, searchTerm));

        if (searchTerm != null) {
            if  (result == null || result.getTotalMatchCount() == 0) {
                this.writeln("No gene found for '" + htmlEntities(searchTerm) + "'");
            } else {
                int matchCount = result.getSearchMatches() == null ? 0 : result.getSearchMatches().size();
                boolean estimation = result.getTotalMatchCount() > matchCount;
                String counterText = "";
                if (estimation) {
                    counterText = "About ";
                }
                counterText += result.getTotalMatchCount() + " gene(s) found for '" + htmlEntities(searchTerm) + "'";
                if (estimation) {
                    counterText += " (only the first " + matchCount + " genes are displayed)";
                }

                this.writeln("<div>");
                this.writeln("<p class='gene-count'>" + counterText + "</p>");
                this.writeln("</div>"); // close gene-count
                
                this.writeln("<div class='table-container'>");
                this.writeln(this.getSearchResultTable(result.getSearchMatches(), searchTerm));
                this.writeln("</div>"); // close table-container
            }
        }
        
        this.endDisplay();
        
        log.traceExit();
    }

    private String getSearchResultTable(List<SearchMatch<Gene>> geneMatches, String searchTerm) {
        log.traceEntry("{}, {}", geneMatches, searchTerm);
        
        StringBuilder sb = new StringBuilder();
        sb.append("<table class='gene-search-result stripe wrap compact responsive'>")
                .append("<thead><tr>")
                .append("   <th>Gene ID</th>")
                .append("   <th>Name</th>")
                .append("   <th>Description</th>")
                .append("   <th>Organism</th>")
                .append("   <th>Match</th>")
                .append("</tr></thead>");

        sb.append("<tbody>");
        for (SearchMatch<Gene> geneMatch: geneMatches) {
            Gene gene = geneMatch.getSearchedObject();

            sb.append("<tr>");
            sb.append("    <td>").append(getSpecificGenePageLink(gene, gene.getGeneId())).append("</td>");
            sb.append("    <td>").append(getSpecificGenePageLink(gene, getStringNotBlankOrDash(gene.getName()))).append("</td>");
            sb.append("    <td>").append(getStringNotBlankOrDash(htmlEntities(gene.getDescription()))).append("</td>");
            sb.append("    <td>").append(getCompleteSpeciesNameLink(gene.getSpecies(), false)).append("</td>");
            sb.append("    <td>").append(getMatch(geneMatch, searchTerm)).append("</td>");
            sb.append("</tr>");
        }
        sb.append("</tbody>");

        sb.append("</table>");
        return log.traceExit(sb.toString());
    }

    /**
     * Return the {@code String} representing the match.
     * 
     * @param geneMatch     A {@code GeneMatch} that is the match to display.
     * @param searchTerm    A {@code String} that is term of the search.
     * @return              The {@code String} representing the match.
     */
    private String getMatch(SearchMatch<Gene> geneMatch, String searchTerm) {
        log.traceEntry("{}, {}", geneMatch, searchTerm);
        
        if (SearchMatch.MatchSource.MULTIPLE.equals(geneMatch.getMatchSource())) {
            return log.traceExit("no exact match");
        }

        return log.traceExit(highlightSearchTerm(geneMatch.getMatch(), searchTerm) +
                " (" + htmlEntities(geneMatch.getMatchSource().toString().toLowerCase()) + ")");
    }

    /**
     * Modify the string to highlight the search term
     * 
     * @param label 
     * @param searchTerm
     * @return
     */
    private String highlightSearchTerm(String label, String searchTerm) {
        log.traceEntry("{}, {}", label, searchTerm);

        //we modify the string to highlight the search term
        //we do not use the tag <strong> yet, so that we can escape htmlentities after the replacement
        //(if we escaped html entities BEFORE the replacement, 
        //then it would not be possible to highlight a html entities term when used as a search term).
        //why using ":myStrongOpeningTag:" and ":myStrongClosingTag:"? 
        //Because it's unlikely to be present in the label :p (?i)([aeiou])
        String newLabel = label.replaceAll("(?i)(" + StringUtils.normalizeSpace(searchTerm) + ")",
                ":myStrongOpeningTag:$1:myStrongClosingTag:");
        //then we escape html entities
        newLabel = htmlEntities(newLabel);
        //and then we replace the <strong> tag
        newLabel = newLabel.replaceAll(":myStrongOpeningTag:", "<strong class='search-match'>")
                .replace(":myStrongClosingTag:", "</strong>");

        return log.traceExit(newLabel);
    }
    
    @Override
    public void displayGeneChoice(Set<Gene> genes) {
        log.traceEntry("{}", genes);
        
        if (genes == null || genes.isEmpty()) {
            throw log.throwing(new IllegalArgumentException(
                    "Gene list should not be empty to display the gene choice page."));
        }

        List<Gene> clnGenes = new ArrayList<>(genes);
        Gene gene = clnGenes.iterator().next();
        
        String titleStart = "Genes: " + gene.getName() + " - " + gene.getGeneId();
        
        this.startDisplay(titleStart, "WebPage");
        
        this.writeln("<h1 property='schema:name'>Gene search</h1>");

        this.writeln("<div id='bgee_introduction' property='schema:description'>");
        
        this.writeln("<p>The search gene ID is found in several species. Select the desired gene:<p>");

        this.writeln("</div>");

        StringBuilder geneList = new StringBuilder();
        geneList.append("<div class='row'>");
        geneList.append(clnGenes.stream()
            .sorted(Comparator.comparing(g -> g.getSpecies() == null?
                null: g.getSpecies().getPreferredDisplayOrder(), Comparator.nullsLast(Comparator.naturalOrder())))
            .map(g -> "<img src='" + this.getSpeciesImageSrc(g.getSpecies(), true) + "' " +
                    "alt='" + htmlEntities(g.getSpecies().getShortName()) 
                    + "' />" + getSpecificGenePageLink(g))
            .collect(Collectors.joining("</div><div class='col-md-offset-3 col-md-6 gene_choice'>",
                    "<div class='col-md-offset-3 col-md-6 gene_choice'>", "</div>")));
        geneList.append("</div>");
        
        this.writeln(geneList.toString());

        this.endDisplay();
        log.traceExit();
    }
    
    /** 
     * Get the link to the gene page as a HTML 'a' element.
     *
     * @param gene  A {@code Gene} that is the gene for which retrieve the link.
     * @return      The {@code String} that is the link to the gene page as a HTML 'a' element.
     */
    private String getSpecificGenePageLink(Gene gene) {
        log.traceEntry("{}", gene);
        return log.traceExit(getSpecificGenePageLink(gene, null));
    }

    /** 
     * Get the link to the gene page as a HTML 'a' element.
     *
     * @param gene      A {@code Gene} that is the gene for which retrieve the link.
     * @param linkText  A {@code String} that is the text of the link.
     * @return          The {@code String} that is the link to the gene page as a HTML 'a' element.
     */
    private String getSpecificGenePageLink(Gene gene, String linkText) {
        log.traceEntry("{}, {}", gene, linkText);
        RequestParameters url = this.getNewRequestParameters();
        url.setPage(RequestParameters.PAGE_GENE);
        url.setGeneId(gene.getGeneId());

        //speciesId only necessary if there are several genes matching a same ID
        if (gene.getGeneMappedToSameGeneIdCount() > 1) {
            url.setSpeciesId(gene.getSpecies().getId());
        }

        String text = StringUtils.isNotBlank(linkText)? htmlEntities(linkText):
                htmlEntities(gene.getName() + " - " + gene.getGeneId())
                        + " in " + getCompleteSpeciesName(gene.getSpecies(), false);

        return log.traceExit("<a href='" + url.getRequestURL() + "'>" + text + "</a>");
    }

    /**
     * Get the search box of a gene as a HTML 'div' element. 
     *
     * @return  the {@code String} that is the search box as HTML 'div' element.
     */
    protected String getGeneSearchBox(boolean isSmallBox, String searchTerm) {
        log.traceEntry("{}, {}", isSmallBox, searchTerm);
    
        RequestParameters urlExample = this.getNewRequestParameters();
        urlExample.setPage(RequestParameters.PAGE_GENE);

        StringBuilder example = new StringBuilder();
        String bgeeGeneSearchClass= "col-xs-11 small-search-box";
        if (!isSmallBox) {
            example.append("<span class='examples'>Examples: ");
            urlExample.setQuery("HBB");
            example.append("<a href='").append(urlExample.getRequestURL()).append("'>HBB</a>");
            urlExample.setQuery("Apoc1");
            example.append(", <a href='").append(urlExample.getRequestURL()).append("'>Apoc1</a>");
            urlExample.setQuery("PDE4DIP");
            example.append(", <a href='").append(urlExample.getRequestURL()).append("'>PDE4DIP</a>");
            urlExample.setQuery("insulin");
            example.append(", <a href='").append(urlExample.getRequestURL()).append("'>insulin</a>");
            example.append("</span>");

            bgeeGeneSearchClass = "col-xs-offset-1 col-xs-10 "
                    + "col-md-offset-2 col-md-8 "
                    + "col-lg-offset-3 col-lg-6";
        }
        
        String value = StringUtils.isNotBlank(searchTerm)? "value='" + htmlEntities(searchTerm) + "'" : "";
        StringBuilder box = new StringBuilder();
        box.append("<div class='row'>");
        box.append("<div id='bgee_gene_search' class='row well well-sm ").append(bgeeGeneSearchClass).append("'>");
        box.append("    <form method='get'>");
        box.append("        <div class='form'>");
        URLParameters urlParameters = this.getRequestParameters().getUrlParametersInstance();
        box.append("            <input type='hidden' id='page' name='"
                + htmlEntities(urlParameters.getParamPage().getName()) + "' value='"
                + htmlEntities(RequestParameters.PAGE_GENE) + "' />");
        box.append("            <label for='bgee_gene_search_completion_box'>Search gene</label>");
        box.append("            <span id='bgee_species_search_msg' class='search_msg'></span>");
        box.append("            <input id='bgee_gene_search_completion_box' class='form-control' " +
                                    "autocomplete='off' type='text' name='"
                + htmlEntities(urlParameters.getParamQuery().getName()) + "' autofocus " +
                                    "maxlength='100' " + value + " />");
        box.append("            <input id='bgee_species_search_submit' type='submit' value='Search' />");
        box.append(             example.toString());
        box.append("        </div>");
        box.append("    </form>");
        box.append("</div>");
        box.append("</div>");

        return log.traceExit(box.toString());
    }

    @Override
    public void displayGene(GeneResponse geneResponse) {
        log.traceEntry("{}", geneResponse);
        
        Gene gene = geneResponse.getGene();
        GeneHomologs geneHomologs = geneResponse.getGeneHomologs();
        
        String titleStart = "Gene: " + htmlEntities(gene.getName()) 
                + " - " + htmlEntities(gene.getGeneId()); 
        String description = htmlEntities(gene.getName()) + " gene expression in Bgee.";

        //Start of the page, gene search, title
        this.displayGenePageStart(titleStart, description);

        //Gene general information
        this.writeln("<div typeof='bs:Gene'>");
        
        this.writeln("<h2>General information</h2>");
        this.writeln("<div class='gene'>" + getGeneralInfo(gene, geneHomologs) + "</div>");

        //Expression data
        this.displayGeneExpressionSection(geneResponse);

        // Homology info
        this.displayHomologsInfo(geneHomologs);

        // Cross-references
        if (gene.getXRefs() != null && gene.getXRefs().size() > 0) {
            this.displayXRefsInfo(gene);
        }
        
        this.writeln("</div>"); // end Gene

        this.endDisplay();
        log.traceExit();
    }

    private void writeExpressionForm(Gene gene) {
        log.traceEntry("{}", gene);

        URLParameters urlParameters = this.getRequestParameters().getUrlParametersInstance();
        List<String> selectedCondParams = this.getRequestParameters().getValues(
                urlParameters.getCondParam());
        Set<String> clonedCondParams = selectedCondParams == null? new HashSet<>():
            new HashSet<>(selectedCondParams);
        this.writeln("<div id='cond_params_choice' class='row well well-sm'>");
        this.writeln("<p>Choose the condition parameters to display gene expression calls for: </p>");
        this.writeln("    <form method='get'>");
        this.writeln("        <div class='form'>");
        this.writeln("            <input type='hidden' id='page' name='"
                + htmlEntities(urlParameters.getParamPage().getName()) + "' value='"
                + htmlEntities(RequestParameters.PAGE_GENE) + "' />");
        this.writeln("            <input type='hidden' id='gene_id' name='"
                + htmlEntities(urlParameters.getParamGeneId().getName()) + "' value='"
                + htmlEntities(gene.getGeneId()) + "' />");
        //speciesId only necessary if there are several genes matching a same Ensembl ID
        if (gene.getGeneMappedToSameGeneIdCount() > 1) {
            this.writeln("            <input type='hidden' id='species_id' name='"
                    + htmlEntities(urlParameters.getParamSpeciesId().getName()) + "' value='"
                    + gene.getSpecies().getId() + "' />");
        }
        //Anat. entity and cell type are handled differently than other cond. params here,
        //since we always want both of them to be selected/unselected together.
        this.writeln("            <label for='cond_params_anat_entity_cell_type'>"
                + "Anat. entity and cell types</label>");
        this.write("<input type='checkbox' id='cond_params_anat_entity_cell_type' "
                + "name='" + htmlEntities(urlParameters.getCondParam().getName()) + "' "
                + "value='" + htmlEntities(CallService.Attribute.ANAT_ENTITY_ID.getCondParamName()
                + urlParameters.getCondParam().getSeparators().iterator().next()
                + CallService.Attribute.CELL_TYPE_ID.getCondParamName()) + "' ");
        if (clonedCondParams.contains(CallService.Attribute.ANAT_ENTITY_ID.getCondParamName()) ||
                clonedCondParams.contains(CallService.Attribute.CELL_TYPE_ID.getCondParamName()) ||
                clonedCondParams.contains(RequestParameters.ALL_VALUE) ||
                clonedCondParams.isEmpty()) {
            this.write("checked");
        }
        this.writeln(">");
        //Now we handle the other cond. params
        CallService.Attribute.getAllConditionParameters().stream()
        .filter(a -> !a.equals(CallService.Attribute.ANAT_ENTITY_ID) &&
                !a.equals(CallService.Attribute.CELL_TYPE_ID))
        .forEach(a -> {
            this.writeln("            <label for='cond_params_" + htmlEntities(a.getCondParamName()) + "'>"
                    + htmlEntities(a.getDisplayName()) + "</label>");
            this.write("<input type='checkbox' id='cond_params_" + htmlEntities(a.getCondParamName()) + "' "
                    + "name='" + htmlEntities(urlParameters.getCondParam().getName()) + "' "
                    + "value='" + htmlEntities(a.getCondParamName()) + "' ");
            if (clonedCondParams.contains(a.getCondParamName()) ||
                    clonedCondParams.contains(RequestParameters.ALL_VALUE)) {
                this.write("checked");
            }
            this.writeln(">");
        });
        this.writeln("            <input id='cond_params_submit' type='submit' value='Submit' />");
        this.writeln("        </div>");
        this.writeln("    </form>");
        this.writeln("</div>");

        log.traceExit();
    }
    /** 
     * Write sources corresponding to the gene species.
     * 
     * @param map               A {@code Map} where keys are {@code DataType}s, the associated value
     *                          being a {@code Set} of {@code Source}s providing annotations or data
     *                          for the associated {@code DataType}.
     * @param allowedDataTypes  An {@code EnumSet} of {@code DataType}s that are allowed data types
     *                          to display.
     * @param text              A {@code String} that is the sentence before the list of sources.
     */
    private void writeSources(Map<DataType, Set<Source>> map, EnumSet<DataType> allowedDataTypes,
            String text) {
        log.traceEntry("{}, {}, {}", map, allowedDataTypes, text);

        // We order the Map by DataType and Source alphabetical name order
        LinkedHashMap<DataType, List<Source>> dsByDataTypes = map.entrySet().stream()
                .filter(e -> allowedDataTypes.contains(e.getKey()))
                .sorted(Comparator.comparing(e -> e.getKey()))
                .collect(Collectors.toMap(
                        e -> e.getKey(),
                        e -> e.getValue().stream().sorted(Comparator.comparing(s -> s.getName()))
                                         .collect(Collectors.toList()),
                        (v1, v2) -> {throw new AssertionError("Impossible collision");},
                        LinkedHashMap::new));
        
        // Then, we display informations
        if (!dsByDataTypes.isEmpty()) {
            this.writeln("<div class='source-info'>");

            this.writeln(htmlEntities(text) + ": ");
            this.writeln("<ul>");

            for (Entry<DataType, List<Source>> e : dsByDataTypes.entrySet()) {
                this.writeln("<li>");
                this.writeln(htmlEntities(e.getKey().getStringRepresentation().substring(0, 1).toUpperCase(Locale.ENGLISH) 
                        + e.getKey().getStringRepresentation().substring(1)) + " data: ");
                StringJoiner sj = new StringJoiner(", ");
                for (Source source : e.getValue()) {
                    String target = source.getName().toLowerCase().equals("bgee")? "" : " target='_blank' rel='noopener'";
                    sj.add("<a href='"
                            //XXX: We should think about how to handle this display better,
                            //because maybe this base URL contains illegal chars :/
                            //(to htmlentities and/or url encode)
                            + source.getBaseUrl() + "'"
                            + target + ">" + 
                            htmlEntities(source.getName()) + "</a>");
                }
                this.writeln(sj.toString());
                this.writeln("</li>");
            }
            this.writeln("</ul>");
            this.writeln("</div>");
        }

        log.traceExit();
    }

    /**
     * Generates the HTML code displaying information about expression calls.
     * 
     * @param calls                 A {@code List} of {@code ExpressionCall}s to be displayed,
     *                              ordered by their global mean rank amd most precise condition for equal ranks. 
     * @param clustering            A {@code Map} where keys are {@code ExpressionCall}s,
     *                              the associated value being the index of the group
     *                              in which they are clustered, based on their global mean rank.
     * @param condParams            An {@code EnumSet} containing the condition parameters (see
     *                              {@link CallService.Attribute#isConditionParameter()}) requested
     *                              to retrieve the calls in {@code calls}.
     * @param requestedDataTypes    An {@code EnumSet} containing the requested {@code DataType}s
     *                              to produce the calls.
     */
    private String getExpressionHTML(List<ExpressionCall> calls, 
            Map<ExpressionCall, Integer> clustering,
            EnumSet<CallService.Attribute> condParams, EnumSet<DataType> requestedDataTypes) {
        log.traceEntry("{}, {}, {}, {}", calls, clustering, condParams, requestedDataTypes);

        StringBuilder sb = new StringBuilder();
        sb.append("<table class='expression stripe nowrap compact responsive'>");
        if (condParams.contains(CallService.Attribute.ANAT_ENTITY_ID)) {
            assert condParams.contains(CallService.Attribute.CELL_TYPE_ID):
                "Anat. entity and cell type are requested together for the gene page";
            sb.append("<th class='anat-entity'>Anatomical entity</th>");
        }
        if (condParams.contains(CallService.Attribute.DEV_STAGE_ID)) {
            sb.append("<th class='dev-stages min-table_sm'>Developmental stage</th>");
        }
        if (condParams.contains(CallService.Attribute.SEX_ID)) {
            sb.append("<th class='sexes min-table_sm'>Sex</th>");
        }
        if (condParams.contains(CallService.Attribute.STRAIN_ID)) {
            sb.append("<th class='strains min-table_sm'>Strain</th>");
        }
        sb.append("<th class='score'>Expression score</th>")
          .append("<th class='score'>FDR</th>")
                //XXX: temporarily "hide" qualities, as they are so incorrect at the moment. 
                //for now we only report presence/absence of data per data type.
//                .append("<th class='quality min-table_md'>Quality</th></tr></thead>\n");
          .append("<th class='quality min-table_md'>Sources</th></tr></thead>\n");
        
        sb.append("<tbody>");

        Integer previousGroupIndex = null;
        for (ExpressionCall call: calls) {
            boolean scoreShift = false;
            Integer currentGroupIndex = clustering.get(call);
            assert currentGroupIndex != null: "Every call should be part of a group.";
            if (previousGroupIndex != null && previousGroupIndex != currentGroupIndex) {
                scoreShift = true;
            }
            
            sb.append(getExpressionRow(call, scoreShift, condParams, requestedDataTypes))
                 .append("\n");
            previousGroupIndex = currentGroupIndex;
        }
        sb.append("</tbody>");
        sb.append("</table>");
        return log.traceExit(sb.toString());

    }

    /**
     * Generates the HTML code to display information about an expression call.
     * 
     * @param call                  The {@code ExpressionCall} to display.
     * @param scoreShift            A {@code boolean} defining whether the global mean rank 
     *                              for {@code call} is in the same cluster as the global mean rank
     *                              of the previous call displayed. If {@code true},
     *                              they are not in the same cluster.
     * @param condParams            An {@code EnumSet} of {@code CallService.Attribute}s that are
     *                              the condition parameters requested to retrieve the calls.
     * @param requestedDataTypes    An {@code EnumSet} containing the requested {@code DataType}s
     *                              to produce the calls.
     * @return                      A {@code String} that is the generated HTML.
     */
    private String getExpressionRow(ExpressionCall call, boolean scoreShift,
            EnumSet<CallService.Attribute> condParams, EnumSet<DataType> requestedDataTypes) {
        log.traceEntry("{}, {}, {}, {}", call, scoreShift, condParams, requestedDataTypes);

        StringBuilder sb = new StringBuilder();
        String scoreShiftClassName = "gene-score-shift";
        sb.append("<tr");
        //score shift *between* anatomical structures
        if (scoreShift) {
            sb.append(" class='").append(scoreShiftClassName).append("' ");
        }
        sb.append(">");
        
        // Anat entity ID and Anat entity cells
        if (condParams.contains(CallService.Attribute.ANAT_ENTITY_ID)) {
            assert condParams.contains(CallService.Attribute.CELL_TYPE_ID):
                "Anat. entity and cell type are requested together for the gene page";
            AnatEntity anatEntity = call.getCondition().getAnatEntity();
            AnatEntity cellType = call.getCondition().getCellType();
            sb.append("<td class='details small'>");
            // post-composition if not the root of cell type
            if(cellType != null && !ConditionDAO.CELL_TYPE_ROOT_ID.equals(cellType.getId())) {
                sb.append(getAnatEntityUrl(cellType, cellType.getId()))
                .append(" <i>in</i> ");
            }
            sb.append(getAnatEntityUrl(anatEntity, anatEntity.getId())).append(" ");
            // post-composition if not the root of cell type
            if(cellType != null && !ConditionDAO.CELL_TYPE_ROOT_ID.equals(cellType.getId())) {
                sb.append(htmlEntities(cellType.getName()))
                .append(" <i>in</i> ");
            }
            sb.append(htmlEntities(anatEntity.getName())).append("</td>");
        }
        // Dev stage
        if (condParams.contains(CallService.Attribute.DEV_STAGE_ID)) {
            final DevStage stage = call.getCondition().getDevStage();
            sb.append("<td class='details small'>")
              .append(htmlEntities(stage.getId())).append(" ")
              .append(htmlEntities(stage.getName()))
              .append("</td>");
        }
        // Sexes
        if (condParams.contains(CallService.Attribute.SEX_ID)) {
            sb.append("<td class='details small'>")
              .append(htmlEntities(call.getCondition().getSex().getName()))
              .append("</td>");
        }
        // Strains
        if (condParams.contains(CallService.Attribute.STRAIN_ID)) {
            sb.append("<td class='details small'>")
              .append(htmlEntities(call.getCondition().getStrain().getName()))
              .append("</td>");
        }
        
        //Expression score
        sb.append("<td>").append(getExpressionScoreHTML(call)).append("</td>");
        //FDR
        sb.append("<td>").append(getFdrHTML(call, requestedDataTypes)).append("</td>");
        // Data types
        sb.append("<td>").append(getDataTypeSpans(call.getCallData())).append("</td>");
        
        sb.append("</tr>");

        return log.traceExit(sb.toString());
    }
    
    /** Generates the HTML code displaying information about homologous genes.
    * 
    * @param gene               A {@code Gene} containing all homology informations
    * @param speciesByTaxon     A {@code LinkedHashMap} where keys are {@code Taxon}s, 
    *                           the associated value being the {@code Set} of {@code Species}
    *                           descendant of the taxon in Bgee. Ordered from more recent taxon
    *                           to older taxon.
    * @param orthologs          A {@code boolean} used to define if orthologs or paralogs have to be 
    *                           displayed. If {@code true}, orthologs will be displayed.
    *                           If {@code false}, paralogs will be displayed
    * @return                   A {@code String} that is the generated HTML.
    */
    private String getHomologyHTMLByTaxon(Gene gene, LinkedHashMap<Taxon, Set<Gene>> homologsByTaxon, 
            boolean orthologs) {
        log.traceEntry("{}, {}", homologsByTaxon, orthologs);
        //TODO shity part to modify once code is ok
        String homologyString = orthologs ? "Orthologs" : "Paralogs";

        // create header of the table
        StringBuilder sb = new StringBuilder();
        if (orthologs) {
            sb.append("<table class='orthologs stripe nowrap compact responsive'>");
        } else {
            sb.append("<table class='paralogs stripe nowrap compact responsive'>");
        }
        sb.append("<thead><tr>")
              .append("<th class='taxon-name'>Taxon name</th>");
        
        // number of species column present only on orthologs table
        if (orthologs) {
            sb.append("<th class='homo-species min-table_sm'>Species with " 
                      + homologyString.toLowerCase() + "</th>");
        }
        
        sb.append("<th class='homo-gene-id'>Gene(s)</th>")
              .append("<th class='exp-comp'>Expression comparison</th>")
              .append("<th class='details'>See details</th>")
              .append("</tr></thead>\n");
       
        // Start generation of html to display
        StringBuilder sbRow = new StringBuilder();
        
        // all homologs of one taxon
        // We will display to each taxon level all genes from more recent taxon
        Set<Gene> allGenes = new HashSet<>();
        for(Entry<Taxon,Set<Gene>> homologsOneTaxon: homologsByTaxon.entrySet()) {
            sbRow.append("<tr>");
            Taxon currentTaxon= homologsOneTaxon.getKey();
            allGenes.addAll(homologsOneTaxon.getValue());
            
            // sort genes by Id and group then by species Id in order to add a line as species 
            // separator
            LinkedHashMap<Integer, List<Gene>> homologsWithDescendantBySpeciesId = allGenes.stream()
                    .sorted(GENE_HOMOLOGY_COMPARATOR)
                    .collect(Collectors.groupingBy(g -> g.getSpecies().getId(), LinkedHashMap::new,
                            Collectors.mapping(g -> g, Collectors.toList())));
            
            //taxon Info
            sbRow.append("<td>")
                .append(getTaxonUrl(currentTaxon))
                .append("</td>");
            
            
            //species with orthologs info
            // boolean used to create vertical line each time a new species is displayed
            boolean needSpeciesSeparator = false;
            
            // number of species column present only on orthologs table
            if(orthologs) {
                sbRow.append("<td>")
                    .append(homologsWithDescendantBySpeciesId.size()).append(" species")
                    .append("<ul class='masked homo-species-list'>");
                // all homologs of one species
                for(Entry<Integer, List<Gene>> homologsOneSpecies: homologsWithDescendantBySpeciesId
                        .entrySet()) {
                    List<Gene> genes = homologsOneSpecies.getValue();
                    sbRow.append("<li class='homo-species");
                    if (needSpeciesSeparator) {
                        sbRow.append(" gene-score-shift");
                    }
                    sbRow.append("'><span class='details small'>")
                        .append(getCompleteSpeciesNameLink(genes.iterator().next().getSpecies(), 
                                true))
                        .append("</span></li>").append("\n");
                    //add empty lines in the list to be able to write genes in front of the proper 
                    // species
                    for (int i = 1; i< genes.size(); i++) {
                        sbRow.append("<li class='ortho-species'><br></li>").append("\n");
                    }
                    needSpeciesSeparator = true;
                    
                }
                sbRow.append("</ul></td>");
            }
            
            
            //genes info
            int numberGenes = allGenes.size();
            sbRow.append("<td>")
            .append(numberGenes).append(" gene").append(numberGenes > 1? "s": "")
                .append("<ul class='masked ortho-genes-list'>");
            needSpeciesSeparator = false;
            for(Entry<Integer, List<Gene>> homologsOneSpecies: homologsWithDescendantBySpeciesId.entrySet()) {
                List<Gene> homoGenes = homologsOneSpecies.getValue();
                for (Gene orthoGene:homoGenes) {
                    sbRow.append("<li class='homo-gene");
                    if(needSpeciesSeparator) {
                        sbRow.append(" gene-score-shift");
                    }
                    sbRow.append("'><span class='details small'>")
                    .append(getSpecificGenePageLink(orthoGene, orthoGene.getGeneId()))
                    .append(StringUtils.isBlank(orthoGene.getName())? "": " " + htmlEntities(orthoGene.getName()))
                    .append("</span></li>").append("\n");
                    needSpeciesSeparator = false;
                }
                needSpeciesSeparator = true;
            }
            sbRow.append("</ul></td>");
            
            //expression comparison link
            RequestParameters exprComparison = this.getNewRequestParameters();
            exprComparison.setPage(RequestParameters.PAGE_EXPR_COMPARISON);
            List<String> genesToCompare = allGenes.stream()
                    .map(Gene::getGeneId).collect(Collectors.toList());
            genesToCompare.add(gene.getGeneId());
            exprComparison.setGeneList(genesToCompare);
            sbRow.append("<td><a href='").append(exprComparison.getRequestURL())
                .append("'>Compare expression</a></td>");

            
            // See Details column
            sbRow.append("<td><span class='expandable' title='click to expand'>[+]</span></td>");
            
            
            sbRow.append("</tr>");
            
        }
        sb.append("<tbody>").append(sbRow.toString()).append("</tbody>");
        sb.append("</table>");
        return log.traceExit(sb.toString());
    }

    /**
     * Create a table containing general information for {@code Gene}
     * 
     * @param gene     The {@code Gene} for which to display information
     * @return         A {@code String} containing the HTML table containing the information.
     */
    private String getGeneralInfo(Gene gene, GeneHomologs geneHomologs) {
        log.traceEntry("{}, {}", gene, geneHomologs);

        final StringBuilder table = new StringBuilder("<div class='info-content'>");
        table.append("<table class='info-table'>");
        table.append("<tr><th scope='row'>Gene ID</th><td>")
                .append(htmlEntities(gene.getGeneId())).append("</td></tr>");
        table.append("<tr><th scope='row'>Name</th><td property='bs:name'>")
                .append(htmlEntities(getStringNotBlankOrDash(gene.getName()))).append("</td></tr>");
        table.append("<tr><th scope='row'>Description</th><td property='bs:description'>")
                .append(htmlEntities(getStringNotBlankOrDash(gene.getDescription()))).append("</td></tr>");
        table.append("<tr><th scope='row'>Organism</th><td property='bs:taxonomicRange' typeof='bs:Taxon'>")
                .append(getCompleteSpeciesNameLink(gene.getSpecies(), true)).append("</td></tr>");
        if (gene.getSynonyms() != null && gene.getSynonyms().size() > 0) {
            table.append("<tr><th scope='row'>Synonym(s)</th><td>")
                    .append(getSynonymDisplay(gene.getSynonyms()));
            table.append("</td></tr>");
        }
        // add orthologs and paralogs number
        if (geneHomologs != null && geneHomologs.getOrthologsByTaxon() != null && 
                !geneHomologs.getOrthologsByTaxon().isEmpty()) {
            table.append("<tr><th scope='row'>Orthologs(s)</th><td>")
                .append("<a href='#orthologs' title='orthologs details'>")
                .append(geneHomologs.getOrthologsByTaxon().entrySet().stream()
                        .flatMap(o -> o.getValue().stream())
                        .collect(Collectors.toSet()).size() + " orthologs</a>");
            table.append("</td></tr>");
        }
        if (geneHomologs != null && geneHomologs.getParalogsByTaxon() != null && 
                !geneHomologs.getParalogsByTaxon().isEmpty()) {
            table.append("<tr><th scope='row'>Paralog(s)</th><td>")
                    .append("<a href='#paralogs' title='paralogs details'>")
                    .append(geneHomologs.getParalogsByTaxon().entrySet().stream()
                            .flatMap(o -> o.getValue().stream())
                            .collect(Collectors.toSet()).size() + " paralogs</a>");
            table.append("</td></tr>");
        }
        table.append("</table>");
        table.append("</div>");

        return log.traceExit(table.toString());
    }

    private void displayHomologsInfo(GeneHomologs geneHomologs) {
        log.traceEntry("{}", geneHomologs);
        Gene gene = geneHomologs.getGene();
        // Orthologs info
        if(geneHomologs.getOrthologsByTaxon() != null &&
                !geneHomologs.getOrthologsByTaxon().isEmpty()) {
            this.writeln("<a id='orthologs' class='inactiveLink'><h2>Orthologs</h2></a>");
            this.writeln("<div id='orthologs_data' class='row'>");
            //table-container
            this.writeln("<div class='col-xs-12 col-md-12'>");
            this.writeln("<div class='table-container'>");

            this.writeln(getHomologyHTMLByTaxon(gene, geneHomologs.getOrthologsByTaxon(), true));
            this.writeln("</div>"); // end table-container
            this.writeln("</div>"); // end class

            this.writeln("<div id='orthology_source' class='col-xs-offset-1 col-sm-offset-2 col-sm-9 col-md-offset-0 col-md-10'>");
            this.writeln("<p>Orthology information comes from "
                    + htmlEntities(geneHomologs.getOrthologyXRef().getSource().getName())
                    + ": <a  target='_blank' rel='noopener' href='"
                    + geneHomologs.getOrthologyXRef().getXRefUrl(true, s -> this.urlEncode(s))
                    + "'>" + htmlEntities(geneHomologs.getOrthologyXRef().getXRefId()) + "</a>.</p>");
            this.writeln("</div>");

            this.writeln("</div>"); // end orthologs_data
        }

        // Paralogs info
        if(geneHomologs.getParalogsByTaxon() != null &&
                !geneHomologs.getParalogsByTaxon().isEmpty()) {
            this.writeln("<a id='paralogs' class='inactiveLink'><h2>Paralogs (same species)</h2></a>");
            this.writeln("<div id='paralogs_data' class='row'>");
            //table-container
            this.writeln("<div class='col-xs-12 col-md-12'>");
            this.writeln("<div class='table-container'>");

            this.writeln(getHomologyHTMLByTaxon(gene, geneHomologs.getParalogsByTaxon(), false));
            this.writeln("</div>"); // end table-container
            this.writeln("</div>"); // end class

            this.writeln("<div id='paralogy_source' class='col-xs-offset-1 col-sm-offset-2 col-sm-9 col-md-offset-0 col-md-10'>");
            this.writeln("<p>Paralogy information comes from "
                    + htmlEntities(geneHomologs.getParalogyXRef().getSource().getName())
                    + ": <a  target='_blank' rel='noopener' href='"
                    + geneHomologs.getParalogyXRef().getXRefUrl(true, s -> this.urlEncode(s))
                    + "'>" + htmlEntities(geneHomologs.getParalogyXRef().getXRefId()) + "</a>.</p>");
            this.writeln("</div>");

            this.writeln("</div>"); // end orthologs_data
        }
        log.traceExit();
    }

    /**
     * Write the HTML code of the cross-references table.
     *
     * @param gene  A {@code Gene} for which we want to display the cross-references
     */
    private void displayXRefsInfo(Gene gene) {
        log.traceEntry("{}", gene);
    
        Set<XRef> xRefs = gene.getXRefs();
    
        this.writeln("<h2>Cross-references</h2>");

        if (xRefs == null || xRefs.size() == 0) {
            this.writeln("No cross-references");
            log.traceExit(); return;
        }
    
        LinkedHashMap<Source, List<String>> xRefsBySource = xRefs.stream()
                .filter(x -> StringUtils.isNotBlank(x.getSource().getXRefUrl()))
                .sorted(X_REF_COMPARATOR)
                .collect(Collectors.groupingBy(XRef::getSource,
                        LinkedHashMap::new,
                        Collectors.mapping(x -> "<a typeof='bs:Gene' property='bs:sameAs' href='"
                                + x.getXRefUrl(true, s -> this.urlEncode(s)) + "' target='_blank' rel='noopener'>"
                                + htmlEntities(x.getXRefId()) + "</a>" + htmlEntities(getFormattedXRefName(x)),
                            Collectors.toList())));
        this.writeln("<div class='info-content'>");
        this.writeln("<table class='info-table'>");
    
        for (Entry<Source, List<String>> entry : xRefsBySource.entrySet()) {
            Source source = entry.getKey();
    
            List<String> sourceXRefs = entry.getValue();
            sourceXRefs.sort(Comparator.naturalOrder());
            
            this.writeln("<tr>");
            
            this.writeln("<th>" + htmlEntities(source.getName()) + "</th>");
            
            this.writeln("<td>");
            this.writeln(getListDisplay("source_" + source.getId(), sourceXRefs));
            this.writeln("</td>");
            
            this.writeln("</tr>");
        }
        this.writeln("</table>");
        this.writeln("</div>");
    
        log.traceExit();
    }

    private void displayGeneExpressionSection(GeneExpressionResponse geneExp) {
        log.traceEntry("{}", geneExp);

        if (ExpressionSummary.NOT_EXPRESSED.equals(geneExp.getCallType())) {
            this.writeln("<h2>Reported absence of expression</h2>");
        } else {
            this.writeln("<h2>Expression</h2>");
        }

        if (geneExp.getCalls() == null || geneExp.getCalls().isEmpty()) {
            if (ExpressionSummary.NOT_EXPRESSED.equals(geneExp.getCallType())) {
                this.writeln("No reported absence of expression for this gene");
            } else {
                this.writeln("No expression data for this gene");
            }
            log.traceExit(); return;
        }
        Gene gene = geneExp.getCalls().iterator().next().getGene();

        writeExpressionForm(gene);
        
        this.writeln("<div id='expr_data' class='row'>");
        
        //table-container
        this.writeln("<div class='col-xs-12 col-md-10'>");
        this.writeln("<div class='table-container'>");

        this.writeln(getExpressionHTML(
                geneExp.getCalls(), 
                geneExp.getClustering(), 
                geneExp.getCondParams(),
                geneExp.getDataTypes()));
        
        this.writeln("</div>"); // end table-container
        this.writeln("</div>"); // end class
        
        //legend
        this.writeln("<div class='legend col-xs-10 col-sm-8 col-md-2 row'>");
        this.writeln("<table class='col-xs-5 col-sm-3 col-md-12'>"
                + "<caption>Sources</caption>" +
                "<tr><th>A</th><td>Affymetrix</td></tr>" +
                "<tr><th>E</th><td>EST</td></tr>" +
                "<tr><th>I</th><td>In Situ</td></tr>" +
                "<tr><th>R</th><td>RNA-Seq</td></tr>" +
                "<tr><th>FL</th><td>scRNA-Seq Full Length</td></tr></table>");
        this.writeln("<table class='col-xs-offset-2 col-xs-5 col-sm-offset-1 col-sm-3 col-md-offset-0 col-md-12'>"
                //XXX: temporarily "hide" qualities, as they are so incorrect at the moment. 
                //for now we only report presence/absence of data per data type.
//                + "<caption>Qualities</caption>" +
//                "<tr><td><span class='quality high'>high quality</span></td></tr>" +
//                "<tr><td><span class='quality low'>low quality</span></td></tr>" +
//                "<tr><td><span class='quality nodata'>no data</span></td></tr></table>");
                + "<tr><td><span class='quality presence'>data</span></td></tr>" +
                  "<tr><td><span class='quality absence'>no data</span></td></tr></table>");
        this.writeln("<table class='col-xs-offset-2 col-xs-5 col-sm-offset-1 col-sm-4 col-md-offset-0 col-md-12'>"
                + "<caption>Expression scores</caption>"
                + "<tr><th><span class='low-qual-score'>3.25e4</span></th>"
                    + "<td>lightgrey: low confidence scores</td></tr>" +
                "<tr><th><hr class='dotted-line' /></th>"
                + "  <td>important score variation</td></tr></table>");
        this.writeln("</div>"); // end legend
        
        this.writeln("</div>"); // end expr_data 

        //other info
        this.writeln("<div class='row'>");

        this.writeln("<div id='expr_score_def' class='col-xs-offset-1 col-sm-offset-2 col-sm-9 col-md-offset-0 col-md-10'>"
                + "<p><strong>Expression scores </strong> of expression calls is based on the rank of a gene in a condition "
                + "according to its expression levels (non-parametric statistics), normalized "
                + "using the minimum and maximum Rank of the species. Values of Expression scores are between "
                + "0 and 100. Low score means that the gene is lowly expressed in the condition compared to other genes. "
                + "Scores are normalized and comparable across genes, conditions and species.</p></div>");
        
        //Source info
        EnumSet<DataType> allowedDataTypes = geneExp.getCalls().stream()
                .flatMap(call -> call.getCallData().stream())
                .map(d -> d.getDataType())
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(DataType.class)));

        boolean hasSourcesForAnnot = gene.getSpecies().getDataTypesByDataSourcesForAnnotation() != null && 
                !gene.getSpecies().getDataTypesByDataSourcesForAnnotation().isEmpty();
        boolean hasSourcesForData = gene.getSpecies().getDataTypesByDataSourcesForData() != null && 
                !gene.getSpecies().getDataTypesByDataSourcesForData().isEmpty();

        if (hasSourcesForAnnot || hasSourcesForData) {
              this.writeln("<div class='sources col-xs-offset-1 col-sm-offset-2 col-md-offset-0 row'>");
        }
        if (hasSourcesForAnnot) {
            this.writeSources(gene.getSpecies().getDataSourcesForAnnotationByDataTypes(), 
                    allowedDataTypes, "Sources of annotations to anatomy and development");
        }
        if (hasSourcesForData) {
            this.writeSources(gene.getSpecies().getDataSourcesForDataByDataTypes(), 
                    allowedDataTypes, "Sources of raw data");
        }
        
        if (hasSourcesForAnnot || hasSourcesForData) {
            this.writeln("</div>"); // end info_sources 
        }
        this.writeln("</div>"); // end other info
    }

    /**
     * Generates the HTML code to display the synonyms.
     *
     * @param synonyms  A {@code Set} of {@code String}s that are the synonyms to display 
     * @return          A {@code String} that is the HTML code to display synonyms
     */
    private static String getSynonymDisplay(Set<String> synonyms) {
        log.traceEntry("{}", synonyms);

        if (synonyms == null || synonyms.size() == 0) {
            return "No synonyms";
        }
        
        List<String> orderedEscapedSynonyms = synonyms.stream()
                .sorted()
                .map(s -> "<span property='bs:alternateName'>" + htmlEntities(s) + "</span>")
                .collect(Collectors.toList());

        String display = getListDisplay("syn", orderedEscapedSynonyms);
        return log.traceExit(display);
    }

    /**
     * Generate the formatted cross-reference name.
     * <p>
     * If the cross-reference name of {@code xRef} can be split, only the first one is displayed. 
     * 
     * @param xRef  A {@code XRef} that is the cross-reference for which the name should be retrieved. 
     * @return      The {@code String} that is the cross-reference name to display.
     */
    private static String getFormattedXRefName(XRef xRef) {
        log.traceEntry("{}", xRef);
        String xRefName = "";
        if (StringUtils.isNotBlank(xRef.getXRefName())) {
            String[] split = xRef.getXRefName().split("; ");
            // If we have several names, we display only the first one.
            xRefName = " (" + split[0] + ")";
        }
        return log.traceExit(xRefName);
    }

    /**
     * Generates the HTML code to display a list of items with the 'more' link.
     * <p>
     * <strong>{@code String}s in {@code items} must have been already properly HTML entity-escaped.
     *
     * @param idPrefix  A {@code String} that is the prefix of the attribute 'id'.
     * @param items     A {@code Set} of {@code String}s that are the items to display 
     * @return          A {@code String} that is the HTML code to display items
     */
    private static String getListDisplay(String idPrefix, List<String> items) {
        log.traceEntry("{}, {}", idPrefix, items);

        boolean tooManyItems = items.size() > MAX_DISPLAYED_ITEMS;

        String display = String.join(", ", tooManyItems?
                items.subList(0, MAX_DISPLAYED_ITEMS): items);
        if (tooManyItems) {
            display += "<span id='" + idPrefix + "_content' class='more-content'>, " +
                    String.join(", ", items.subList(MAX_DISPLAYED_ITEMS, items.size())) +
                    "</span>";
            display += " <span id='" + idPrefix + "_click' class='glyphicon glyphicon-plus'></span>";
        }
        return log.traceExit(display);
    }

    /**
     * Return the {@code name} if it is not blank. Otherwise, it returns '-'.
     *
     * @param s     A {@code String} that is the string to analyze.
     * @return      The {@code String} that is {@code name} if it is not blank.
     *              Otherwise, it returns '-'.
     */
    private static String getStringNotBlankOrDash(String s) {
        return StringUtils.isNotBlank(s) ? s: "-";
    }

    /**
     * Builds the data type 'span' elements representing presence/absence of data
     * for the given expression calls.
     * 
     * @param callData     A {@code Collection} of {@code ExpressionCallData} as input.
     * @return             The {@code String} containing the HTML code of the 'span' elements.
     */
    private static String getDataTypeSpans(Collection<ExpressionCallData> callData) {
        log.traceEntry("{}", callData);
        final Map<DataType, Set<ExpressionCallData>> callsByDataTypes = callData.stream()
                .collect(Collectors.groupingBy(ExpressionCallData::getDataType, Collectors.toSet()));

        return log.traceExit(EnumSet.allOf(DataType.class).stream()
                .map(type -> getDataSpan(type, callsByDataTypes.containsKey(type)))
                .collect(Collectors.joining()));
    }

    /**
     * Builds a 'span' element representing presence/absence of data for a given {@code DataType}.
     * 
     * @param hasData  A {@code boolean} defining whether there is data for {@code type}.
     * @param type     A {@code DataType} that is the data type for which 'span' should be displayed.
     * @return         The {@code String} containing the HTML code for the quality 'span'.
     */
    private static String getDataSpan(DataType type, boolean hasData) {
        log.traceEntry("{}, {}", type, hasData);
        
        StringBuilder sb = new StringBuilder();
        sb.append("<span class='quality ");

        if (hasData) {
            sb.append("presence");
        } else {
            sb.append("absence");
        }
        sb.append("' title='").append(htmlEntities(type.getStringRepresentation())).append(": ")
                .append(hasData?"presence":"absence").append("'>");

        switch (type) {
            case AFFYMETRIX:
                sb.append("A");
                break;
            case RNA_SEQ:
                sb.append("R");
                break;
            case IN_SITU:
                sb.append("I");
                break;
            case EST:
                sb.append("E");
                break;
            case FULL_LENGTH:
                sb.append("FL");
                break;
        }
        sb.append("</span>");
        return log.traceExit(sb.toString());
    }
    
    /**
     * @param call                  An {@code ExpressionCall} for which we want to display the FDR.
     * @param requestedDataTypes    An {@code EnumSet} containing the requested {@code DataType}s
     *                              to produce the calls.
     * @return     A {@code String} containing the HTML to display the FDR, 
     *             notably displaying information about confidence in the FDR.
     */
    private static String getFdrHTML(ExpressionCall call, EnumSet<DataType> requestedDataTypes) {
        log.traceEntry("{}, {}", call, requestedDataTypes);

        //If the rank is above a threshold and is only supported by ESTs and/or in situ data, 
        //then we consider it of low confidence
        //TODO: there should be a better mechanism to handle that, and definitely not in the view, 
        //it is not its role to determine what is of low confidence...
        //Maybe create in bgee-core a new RankScore class, storing the rank and the confidence.

        //For the gene page, for now we always consider all data types, so we retrieve the FDR
        //computed by taking into account all data types
        String fdr = htmlEntities(call.getPValueWithEqualDataTypes(requestedDataTypes)
                .getFormatedFDRPValue());
        //Now, we also want to know which data types have data supporting this call
        EnumSet<DataType> dataTypesWithData = call.getCallData().stream().map(ExpressionCallData::getDataType)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(DataType.class)));
        if (!SummaryQuality.BRONZE.equals(call.getSummaryQuality()) &&
                (dataTypesWithData.contains(DataType.AFFYMETRIX) ||
                 dataTypesWithData.contains(DataType.RNA_SEQ) ||
                 dataTypesWithData.contains(DataType.FULL_LENGTH) ||
                call.getMeanRank().compareTo(BigDecimal.valueOf(20000)) < 0)) {
            return log.traceExit(fdr);
        }
        StringBuilder sb = new StringBuilder();
        sb.append("<span class='low-qual-score'>").append(fdr).append("</span>");
        return log.traceExit(sb.toString());
    }
    
    /**
     * @param call An {@code ExpressionCall} for which we want to display expression score.
     * @return     A {@code String} containing the HTML to display the expression score, 
     *             notably displaying information about confidence in the call.
     */
    private static String getExpressionScoreHTML(ExpressionCall call) {
        log.traceEntry("{}", call);

        //If the rank of the call is above a threshold AND the call is only supported by ESTs 
        //and/or in situ data, then we consider it of low confidence and the corresponding score
        //is written with a different color
        //TODO: there should be a better mechanism to handle that, and definitely not in the view, 
        //it is not its role to determine what is of low confidence...
        //Maybe create in bgee-core a new ExpressionScore class, storing the expression score and its
        //confidence.
        Set<DataType> dataTypes = call.getCallData().stream().map(ExpressionCallData::getDataType)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(DataType.class)));
        String expressionScore = htmlEntities(call.getExpressionLevelInfo().getFormattedExpressionScore());
        if (!SummaryQuality.BRONZE.equals(call.getSummaryQuality()) && 
                (dataTypes.contains(DataType.AFFYMETRIX) ||
                dataTypes.contains(DataType.RNA_SEQ) ||
                dataTypes.contains(DataType.FULL_LENGTH) ||
                call.getMeanRank().compareTo(BigDecimal.valueOf(20000)) < 0)) {
            return log.traceExit(expressionScore);
        }
        StringBuilder sb = new StringBuilder();
        sb.append("<span class='low-qual-score'>").append(expressionScore).append("</span>");
        return log.traceExit(sb.toString());
    }

    @Override
    public void displayGeneGeneralInformation(Collection<Gene> genes) {
        log.traceEntry("{}", genes);

        //All genes are supposed to have the same ID here
        Set<String> ids = genes.stream().map(g -> g.getGeneId()).collect(Collectors.toSet());
        if (ids.size() != 1) {
            throw log.throwing(new IllegalArgumentException("All genes should have the same ID"));
        }
        String geneId = ids.iterator().next();

        Set<String> geneNames = genes.stream().map(g -> g.getName()).collect(Collectors.toSet());
        String geneName = null;
        if (geneNames.size() == 1) {
            geneName = geneNames.iterator().next();
        }

        String titleStart = "Gene expression for gene: "
                            + htmlEntities(geneId)
                            + (geneName != null? " - " + htmlEntities(geneName): "");
        String description = titleStart + ".";

        this.displayGenePageStart(titleStart, description);

        StringBuilder geneList = new StringBuilder();
        geneList.append("<div class='row'>");
        geneList.append(genes.stream()
            .sorted(Comparator.comparing(g -> g.getSpecies() == null?
                null: g.getSpecies().getPreferredDisplayOrder(), Comparator.nullsLast(Comparator.naturalOrder())))
            .map(g -> "<img src='" + this.getSpeciesImageSrc(g.getSpecies(), true) + "' " +
                    "alt='" + htmlEntities(g.getSpecies().getShortName())
                    + "' />" + getSpecificGenePageLink(g)
                    + "<h2>General information</h2><div class='gene'>" + getGeneralInfo(g, null) + "</div>")
            .collect(Collectors.joining("</div><div class='col-md-offset-3 col-md-6 gene_choice'>",
                    "<div class='col-md-offset-3 col-md-6 gene_choice'>", "</div>")));
        geneList.append("</div>");

        this.writeln(geneList.toString());

        this.endDisplay();
        log.traceExit();
    }

    @Override
    public void displayGeneHomologs(GeneHomologs geneHomologs) {
        log.traceEntry("{}", geneHomologs);

        Gene gene = geneHomologs.getGene();
        String titleStart = "Homology information for gene: " + htmlEntities(gene.getName())
                          + " - " + htmlEntities(gene.getGeneId());
        String description = titleStart + ".";

        this.displayGenePageStart(titleStart, description);
        this.displayHomologsInfo(geneHomologs);

        this.endDisplay();
        log.traceExit();
    }

    @Override
    public void displayGeneXRefs(Gene gene) {
        log.traceEntry("{}", gene);

        String titleStart = "Cross-reference information for gene: " + htmlEntities(gene.getName())
                          + " - " + htmlEntities(gene.getGeneId());
        String description = titleStart + ".";

        this.displayGenePageStart(titleStart, description);
        this.displayXRefsInfo(gene);

        this.endDisplay();
        log.traceExit();
    }

    @Override
    public void displayGeneExpression(GeneExpressionResponse geneExpressionResponse) {
        log.traceEntry("{}", geneExpressionResponse);

        //See notes in CommandGene about retrieving the Gene even if there is no expression result.
        String geneId = null;
        String geneName = null;
        if (geneExpressionResponse.getCalls() == null || geneExpressionResponse.getCalls().isEmpty()) {
            geneId = this.getRequestParameters().getGeneId();
        } else {
            Gene gene = geneExpressionResponse.getCalls().iterator().next().getGene();
            geneId = gene.getGeneId();
            geneName = gene.getName();
        }
        assert geneId != null;
        String titleStart = ExpressionSummary.NOT_EXPRESSED.equals(geneExpressionResponse.getCallType())?
                                    "Reported absence of expression": "Gene expression"
                            + " for gene: "
                            + htmlEntities(geneId)
                            + (geneName != null? " - " + htmlEntities(geneName): "");
        String description = titleStart + ".";

        this.displayGenePageStart(titleStart, description);
        this.displayGeneExpressionSection(geneExpressionResponse);

        this.endDisplay();
        log.traceExit();
    }

    private void displayGenePageStart(String title, String description) {
        log.traceEntry(title, description);

        this.startDisplay(title, "WebPage", description);

        // Gene search
        this.writeln("<div class='row'>");

        this.writeln("<div class='col-sm-3'>");
        this.writeln(getGeneSearchBox(true, null));
        this.writeln("</div>"); // close div

        //page title
        this.writeln("<h1 class='gene_title col-sm-9 col-lg-7' property='schema:name'>");
        this.writeln(title);
        this.writeln("</h1>");

        this.writeln("</div>"); // close row
        log.traceExit();
    }
    

    @Override
    protected void includeCss() {
        log.traceEntry();
        
        //If you ever add new files, you need to edit bgee-webapp/pom.xml 
        //to correctly merge/minify them.
        if (!this.prop.isMinify()) {
            this.includeCss("lib/jquery_plugins/jquery.dataTables.min.css");
            this.includeCss("lib/jquery_plugins/responsive.dataTables.min.css");
        } else {
            this.includeCss("lib/jquery_plugins/vendor_gene.css");
        }
        this.includeCss("gene.css");

        //we need to add the Bgee CSS files at the end, to override CSS file from external libs
        super.includeCss();
        
        log.traceExit();
    }

    @Override
    protected void includeJs() {
        log.traceEntry();
        
        super.includeJs();
        //If you ever add new files, you need to edit bgee-webapp/pom.xml 
        //to correctly merge/minify them.
        if (!this.prop.isMinify()) {
            this.includeJs("lib/jquery_plugins/jquery.dataTables.min.js");
            this.includeJs("lib/jquery_plugins/dataTables.responsive.min.js");
            this.includeJs("gene.js");
            this.includeJs("autoCompleteGene.js");
            this.includeJs("jquery_ui_autocomplete_modif.js");
        } else {
            this.includeJs("lib/jquery_plugins/vendor_gene.js");
            this.includeJs("script_gene.js");
        }
        log.traceExit();
    }
}
