package org.bgee.view.html;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
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
import java.util.TreeMap;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.BgeeProperties;
import org.bgee.controller.CommandGene.GeneResponse;
import org.bgee.controller.RequestParameters;
import org.bgee.controller.URLParameters;
import org.bgee.model.NamedEntity;
import org.bgee.model.XRef;
import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.anatdev.DevStage;
import org.bgee.model.expressiondata.Call.ExpressionCall;
import org.bgee.model.expressiondata.CallData.ExpressionCallData;
import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.model.expressiondata.baseelements.SummaryQuality;
import org.bgee.model.gene.Gene;
import org.bgee.model.gene.GeneMatch;
import org.bgee.model.gene.GeneMatchResult;
import org.bgee.model.source.Source;
import org.bgee.view.GeneDisplay;
import org.bgee.view.JsonHelper;

/**
 * This class is the HTML implementation of the {@code GeneDisplay}.
 * 
 * @author  Philippe Moret
 * @author  Valentine Rech de Laval
 * @author  Frederic Bastian
 * @version Bgee 14, July 2019
 * @since   Bgee 13, Oct. 2015
 */
public class HtmlGeneDisplay extends HtmlParentDisplay implements GeneDisplay {
    private final static Logger log = LogManager.getLogger(HtmlGeneDisplay.class.getName());

    private final static int MAX_DISPLAYED_ITEMS = 10;

    private final static Comparator<XRef> X_REF_COMPARATOR = Comparator
            .<XRef, Integer>comparing(x -> x.getSource().getDisplayOrder(), Comparator.nullsLast(Integer::compareTo))
            .thenComparing(x -> x.getSource().getName(), Comparator.nullsLast(String::compareTo))
            .thenComparing((XRef::getXRefId), Comparator.nullsLast(String::compareTo));
    
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

    /*Genes: the terms you enter are searched in gene IDs from Ensembl, names, and synonyms.*/
    @Override
    public void displayGeneHomePage() {
        log.entry();
        this.displayGeneSearchPage(null, null);
        log.exit();
    }

    @Override
    public void displayGeneSearchResult(String searchTerm, GeneMatchResult result) {
        log.entry(searchTerm, result);
        this.displayGeneSearchPage(searchTerm, result);
        log.exit();
    }

    private void displayGeneSearchPage(String searchTerm, GeneMatchResult result) {
        log.entry(searchTerm, result);
        String geneSearchDescription = null;
        if(searchTerm != null) {
            geneSearchDescription = "Genes matching " + htmlEntities(searchTerm) + "in Bgee";
        }
        this.startDisplay("Gene information", "WebPage", geneSearchDescription);
        
        this.writeln("<h1 property='schema:name'>Gene search</h1>");

        this.writeln("<div id='bgee_introduction' property='schema:description'>");
        this.writeln("<p>Search for genes based on Ensembl gene IDs, gene names, " +
                "gene descriptions, synonyms and cross-references.</p>");
        this.writeln("</div>");

        this.writeln(this.getGeneSearchBox(false, searchTerm));

        if (searchTerm != null) {
            if  (result == null || result.getTotalMatchCount() == 0) {
                this.writeln("No gene found for '" + htmlEntities(searchTerm) + "'");
            } else {
                int matchCount = result.getGeneMatches() == null ? 0 : result.getGeneMatches().size();
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
                this.writeln(this.getSearchResultTable(result.getGeneMatches(), searchTerm));
                this.writeln("</div>"); // close table-container
            }
        }
        
        this.endDisplay();
        
        log.exit();
    }

    private String getSearchResultTable(List<GeneMatch> geneMatches, String searchTerm) {
        log.entry(geneMatches, searchTerm);
        
        StringBuilder sb = new StringBuilder();
        sb.append("<table class='gene-search-result stripe wrap compact responsive'>")
                .append("<thead><tr>")
                .append("   <th>Ensembl ID</th>")
                .append("   <th>Name</th>")
                .append("   <th>Description</th>")
                .append("   <th>Organism</th>")
                .append("   <th>Match</th>")
                .append("</tr></thead>");

        sb.append("<tbody>");
        for (GeneMatch geneMatch: geneMatches) {
            Gene gene = geneMatch.getGene();

            sb.append("<tr>");
            sb.append("    <td>").append(getSpecificGenePageLink(gene, gene.getEnsemblGeneId())).append("</td>");
            sb.append("    <td>").append(getSpecificGenePageLink(gene, getStringNotBlankOrDash(gene.getName()))).append("</td>");
            sb.append("    <td>").append(getStringNotBlankOrDash(htmlEntities(gene.getDescription()))).append("</td>");
            sb.append("    <td>").append(getCompleteSpeciesNameLink(gene.getSpecies(), false)).append("</td>");
            sb.append("    <td>").append(getMatch(geneMatch, searchTerm)).append("</td>");
            sb.append("</tr>");
        }
        sb.append("</tbody>");

        sb.append("</table>");
        return log.exit(sb.toString());
    }

    /**
     * Return the {@code String} representing the match.
     * 
     * @param geneMatch     A {@code GeneMatch} that is the match to display.
     * @param searchTerm    A {@code String} that is term of the search.
     * @return              The {@code String} representing the match.
     */
    private String getMatch(GeneMatch geneMatch, String searchTerm) {
        log.entry(geneMatch, searchTerm);
        
        if (GeneMatch.MatchSource.MULTIPLE.equals(geneMatch.getMatchSource())) {
            return log.exit("no exact match");
        }

        return log.exit(highlightSearchTerm(geneMatch.getMatch(), searchTerm) +
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
        log.entry(label, searchTerm);

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

        return log.exit(newLabel);
    }
    
    @Override
    public void displayGeneChoice(Set<Gene> genes) {
        log.entry(genes);
        
        if (genes == null || genes.isEmpty()) {
            throw log.throwing(new IllegalArgumentException(
                    "Gene list should not be empty to display the gene choice page."));
        }

        List<Gene> clnGenes = new ArrayList<>(genes);
        Gene gene = clnGenes.iterator().next();
        
        String titleStart = "Genes: " + gene.getName() + " - " + gene.getEnsemblGeneId();
        
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
        log.exit();
    }
    
    /** 
     * Get the link to the gene page as a HTML 'a' element.
     *
     * @param gene  A {@code Gene} that is the gene for which retrieve the link.
     * @return      The {@code String} that is the link to the gene page as a HTML 'a' element.
     */
    private String getSpecificGenePageLink(Gene gene) {
        log.entry(gene);
        return log.exit(getSpecificGenePageLink(gene, null));
    }

    /** 
     * Get the link to the gene page as a HTML 'a' element.
     *
     * @param gene      A {@code Gene} that is the gene for which retrieve the link.
     * @param linkText  A {@code String} that is the text of the link.
     * @return          The {@code String} that is the link to the gene page as a HTML 'a' element.
     */
    private String getSpecificGenePageLink(Gene gene, String linkText) {
        log.entry(gene, linkText);
        RequestParameters url = this.getNewRequestParameters();
        url.setPage(RequestParameters.PAGE_GENE);
        url.setGeneId(gene.getEnsemblGeneId());

        //speciesId only necessary if there are several genes matching a same Ensembl ID
        if (gene.getGeneMappedToSameEnsemblGeneIdCount() > 1) {
            url.setSpeciesId(gene.getSpecies().getId());
        }

        String text = StringUtils.isNotBlank(linkText)? htmlEntities(linkText):
                htmlEntities(gene.getName() + " - " + gene.getEnsemblGeneId())
                        + " in " + getCompleteSpeciesName(gene.getSpecies(), false);

        return log.exit("<a href='" + url.getRequestURL() + "'>" + text + "</a>");
    }

    /**
     * Get the search box of a gene as a HTML 'div' element. 
     *
     * @return  the {@code String} that is the search box as HTML 'div' element.
     */
    protected String getGeneSearchBox(boolean isSmallBox, String searchTerm) {
        log.entry(isSmallBox, searchTerm);
    
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

        return log.exit(box.toString());
    }

    @Override
    public void displayGene(GeneResponse geneResponse) {
        log.entry(geneResponse);
        
        Gene gene = geneResponse.getGene();
        
        String titleStart = "Gene: " + htmlEntities(gene.getName()) 
                + " - " + htmlEntities(gene.getEnsemblGeneId()); 
        String description = htmlEntities(gene.getName()) + " gene expression in Bgee.";
        this.startDisplay(titleStart, "WebPage", description);

        // Gene search
        this.writeln("<div class='row'>");

        this.writeln("<div class='col-sm-3'>");
        this.writeln(getGeneSearchBox(true, null));
        this.writeln("</div>"); // close div

        //page title
        this.writeln("<h1 class='gene_title col-sm-9 col-lg-7' property='schema:name'>");
        this.writeln("<img src='" + this.getSpeciesImageSrc(gene.getSpecies(), true)
                + "' alt='" + htmlEntities(gene.getSpecies().getShortName()) + "' />");
        this.writeln(htmlEntities(titleStart));
        this.writeln(" - " + getCompleteSpeciesName(gene.getSpecies(), false));
        this.writeln("</h1>");
        
        this.writeln("</div>"); // close row

        //Gene general information
        this.writeln("<div typeof='bs:Gene'>");
        
        this.writeln("<h2>General information</h2>");
        this.writeln("<div class='gene'>" + getGeneralInfo(gene) + "</div>");


        //Expression data
        this.writeln("<h2>Expression</h2>");
        
        this.writeln("<div id='expr_data' class='row'>");
        
        //table-container
        this.writeln("<div class='col-xs-12 col-md-10'>");
        this.writeln("<div class='table-container'>");

        this.writeln(getExpressionHTMLByAnat(
                geneResponse.getCallsByAnatEntity(), 
                geneResponse.getClusteringBestEachAnatEntity(), 
                geneResponse.getClusteringWithinAnatEntity()));
        
        this.writeln("</div>"); // end table-container
        this.writeln("</div>"); // end class
        
        //legend
        this.writeln("<div class='legend col-xs-10 col-sm-8 col-md-2 row'>");
        this.writeln("<table class='col-xs-5 col-sm-3 col-md-12'>"
                + "<caption>Sources</caption>" +
                "<tr><th>A</th><td>Affymetrix</td></tr>" +
                "<tr><th>E</th><td>EST</td></tr>" +
                "<tr><th>I</th><td>In Situ</td></tr>" +
                "<tr><th>R</th><td>RNA-Seq</td></tr></table>");
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
                + "<caption>Rank scores</caption>"
                + "<tr><th><span class='low-qual-score'>3.25e4</span></th>"
                    + "<td>lightgrey: low confidence scores</td></tr>" +
                "<tr><th><hr class='dotted-line' /></th>"
                + "  <td>important score variation</td></tr></table>");
        this.writeln("</div>"); // end legend
        
        this.writeln("</div>"); // end expr_data 

        //other info
        this.writeln("<div class='row'>");

        this.writeln("<div id='rank_def' class='col-xs-offset-1 col-sm-offset-2 col-sm-9 col-md-offset-0 col-md-10'>"
                + "<p><strong>Rank scores </strong> of expression calls are normalized across genes, conditions and species. "
                + "Low score means that the gene is highly expressed in the condition. "
                + "Max rank score in all species: 4.10e4. Min rank score varies across species.</p></div>");
        
        this.writeln("<div id='expr_score_def' class='col-xs-offset-1 col-sm-offset-2 col-sm-9 col-md-offset-0 col-md-10'>"
                + "<p><strong>Expression scores </strong> of expression calls use the  minimum and maximum Rank of the species to normalize the "
                + "expression to a value between 0 and 100. Low score means that the gene is lowly expressed in the "
                + "condition.</p></div>");

        //Source info
        Set<DataType> allowedDataTypes = geneResponse.getCallsByAnatEntity().values().stream()
                .flatMap(List::stream)
                .flatMap(call -> call.getCallData().stream())
                .map(d -> d.getDataType())
                .collect(Collectors.toSet());

        boolean hasSourcesForAnnot = gene.getSpecies().getDataTypesByDataSourcesForAnnotation() != null && 
                !gene.getSpecies().getDataTypesByDataSourcesForAnnotation().isEmpty();
        boolean hasSourcesForData = gene.getSpecies().getDataTypesByDataSourcesForData() != null && 
                !gene.getSpecies().getDataTypesByDataSourcesForData().isEmpty();

        if (hasSourcesForAnnot && hasSourcesForData) {
              this.writeln("<div class='sources col-xs-offset-1 col-sm-offset-2 col-md-offset-0 row'>");
        }
        if (hasSourcesForAnnot) {
            this.writeSources(gene.getSpecies().getDataTypesByDataSourcesForAnnotation(), 
                    allowedDataTypes, "Sources of annotations to anatomy and development");
        }
        if (hasSourcesForData) {
            this.writeSources(gene.getSpecies().getDataTypesByDataSourcesForData(), 
                    allowedDataTypes, "Sources of raw data");
        }
        
        if (hasSourcesForAnnot && hasSourcesForData) {
            this.writeln("</div>"); // end info_sources 
        }
        this.writeln("</div>"); // end other info
        
        // Cross-references
        if (gene.getXRefs() != null && gene.getXRefs().size() > 0) {
            this.writeln("<h2>Cross-references</h2>");
            this.writeln(getXRefDisplay(gene.getXRefs()));
        }
        
        this.writeln("</div>"); // end Gene

        this.endDisplay();
        log.exit();
    }

    /** 
     * Write sources corresponding to the gene species.
     * 
     * @param map               A {@code Map} where keys are {@code Source}s corresponding to 
     *                          data sources, the associated values being a {@code Set} of 
     *                          {@code DataType}s corresponding to data types of data.
     * @param allowedDataTypes  A {@code Set} of {@code DataType}s that are allowed data types
     *                          to display.
     * @param text              A {@code String} that is the sentence before the list of sources.
     */
    private void writeSources(Map<Source, Set<DataType>> map, Set<DataType> allowedDataTypes, String text) {
        log.entry(map, allowedDataTypes, text);

        // First, we invert map to be able to display data sources according to data types.
        // We use TreeMap to conserve order of data types.
        TreeMap<DataType, Set<Source>> dsByDataTypes = map.entrySet().stream()
                //transform the Entry<Source, Set<DataType>> into several Entry<DataType, Source>
                .flatMap(e -> e.getValue().stream().map(t -> new AbstractMap.SimpleEntry<>(t, e.getKey())))
                //keep only allowed data types
                .filter(e -> allowedDataTypes.contains(e.getKey()))
                //collect the Entry<DataType, Source> into a TreeMap<DataType, Set<Source>>
                .collect(Collectors.toMap(e -> e.getKey(), e -> new HashSet<>(Arrays.asList(e.getValue())), 
                        (s1, s2) -> {s1.addAll(s2); return s1;}, 
                        TreeMap::new));
        
        // Then, we display informations
        if (!dsByDataTypes.isEmpty()) {
            this.writeln("<div class='source-info'>");

            this.writeln(htmlEntities(text) + ": ");
            this.writeln("<ul>");

            for (Entry<DataType, Set<Source>> e : dsByDataTypes.entrySet()) {
                this.writeln("<li>");
                this.writeln(htmlEntities(e.getKey().getStringRepresentation().substring(0, 1).toUpperCase(Locale.ENGLISH) 
                        + e.getKey().getStringRepresentation().substring(1)) + " data: ");
                StringJoiner sj = new StringJoiner(", ");
                for (Source source : e.getValue().stream()
                        .sorted(Comparator.comparing(NamedEntity::getName)).collect(Collectors.toList())) {
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

        log.exit();
    }

    /**
     * Generates the HTML code displaying information about expression calls.
     * 
     * @param byAnatEntityId               A {@code Map} where keys are {@code String}s representing 
     *                                     anatomical entity IDs, the associated value being a {@code List} 
     *                                     of {@code ExpressionCall}s for this anatomical entity, 
     *                                     ordered by their global mean rank. 
     * @param clusteringBestEachAnatEntity A {@code Map} where keys are {@code ExpressionCall}s, 
     *                                     the associated value being the index of the group 
     *                                     in which they are clustered, based on their global mean rank. 
     *                                     This custering is generated by only considering 
     *                                     the best {@code ExpressionCall} from each anatomical entity.
     * @param clusteringWithinAnatEntity   A {@code Map} where keys are {@code ExpressionCall}s, 
     *                                     the associated value being the index of the group 
     *                                     in which they are clustered, based on their global mean rank. 
     *                                     This custering is generated independently 
     *                                     for each anatomical entity (so, {@code ExpressionCall}s 
     *                                     associated to a same value in the {@code Map} 
     *                                     might not be part of a same cluster).
     * @return                             A {@code String} that is the generated HTML.
     */
    private String getExpressionHTMLByAnat(Map<AnatEntity, List<ExpressionCall>> byAnatEntityId, 
            Map<ExpressionCall, Integer> clusteringBestEachAnatEntity, 
            Map<ExpressionCall, Integer> clusteringWithinAnatEntity) {
        log.entry(byAnatEntityId, clusteringBestEachAnatEntity, clusteringWithinAnatEntity);


        StringBuilder rowSb = new StringBuilder();
        Integer previousGroupIndex = null;
        for (Entry<AnatEntity, List<ExpressionCall>> anatRow: byAnatEntityId.entrySet()) {
            final AnatEntity a = anatRow.getKey();
            final List<ExpressionCall> calls = anatRow.getValue();
            
            boolean scoreShift = false;
            Integer currentGroupIndex = clusteringBestEachAnatEntity.get(calls.get(0));
            assert currentGroupIndex != null: "Every best call should be part of a group.";
            if (previousGroupIndex != null && previousGroupIndex != currentGroupIndex) {
                scoreShift = true;
            }
            
            rowSb.append(getExpressionRowsForAnatEntity(a, calls, scoreShift, 
                    clusteringWithinAnatEntity))
                 .append("\n");
            previousGroupIndex = currentGroupIndex;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("<table class='expression stripe nowrap compact responsive'>")
                .append("<thead><tr><th class='anat-entity-id'>Anat. entity ID</th>")
                .append("<th class='anat-entity'>Anatomical entity</th>")
                .append("<th class='dev-stages min-table_sm'>Developmental stage(s)</th>")
                .append("<th class='score'>Rank score</th>")
                .append("<th class='score'>Expression score</th>")
                //XXX: temporarily "hide" qualities, as they are so incorrect at the moment. 
                //for now we only report presence/absence of data per data type.
//                .append("<th class='quality min-table_md'>Quality</th></tr></thead>\n");
                .append("<th class='quality min-table_md'>Sources</th></tr></thead>\n");
        
        sb.append("<tbody>").append(rowSb.toString()).append("</tbody>");
        sb.append("</table>");
        return log.exit(sb.toString());

    }

    /**
     * Generates the HTML code to display information about expression calls occurring  
     * in one specific anatomical entity. 
     * 
     * @param anatEntity                   The {@code AnatEntity} for which the expression calls 
     *                                     will be displayed.
     * @param calls                        A {@code List} of {@code ExpressionCall}s related to 
     *                                     {@code anatEntity}, ordered by their global mean rank. 
     * @param scoreShift                   A {@code boolean} defining whether the global mean rank 
     *                                     for this anatomical entity is in the same cluster as 
     *                                     the global mean rank of the previous anatomical entity.
     *                                     If {@code true}, there are not in the same cluster. 
     * @param clusteringWithinAnatEntity   A {@code Map} where keys are {@code ExpressionCall}s, 
     *                                     the associated value being the index of the group 
     *                                     in which they are clustered, based on their global mean rank. 
     *                                     This custering is generated independently 
     *                                     for each anatomical entity (so, {@code ExpressionCall}s 
     *                                     associated to a same value in the {@code Map} 
     *                                     might not be part of a same cluster).
     * @return                             A {@code String} that is the generated HTML.
     */
    private String getExpressionRowsForAnatEntity(AnatEntity anatEntity, 
            List<ExpressionCall> calls, boolean scoreShift, 
            Map<ExpressionCall, Integer> clusteringWithinAnatEntity) {
        log.entry(anatEntity, calls, scoreShift, clusteringWithinAnatEntity);
        
        StringBuilder sb = new StringBuilder();
        String scoreShiftClassName = "gene-score-shift";
        sb.append("<tr");
        //score shift *between* anatomical structures
        if (scoreShift) {
            sb.append(" class='").append(scoreShiftClassName).append("' ");
        }
        sb.append(">");
        String toAddToTd = "";
        if (scoreShift) {
            toAddToTd = " class='" + scoreShiftClassName + "' ";
        }
        
        // Anat entity ID and Anat entity cells 
        sb.append("<td class='details small'>")
            .append(getAnatEntityUrl(anatEntity, anatEntity.getId()))
            .append("</td><td").append(toAddToTd).append(">")
            .append(htmlEntities(anatEntity.getName())).append("</td>");
        
        // Dev stage cell
        sb.append("<td><span class='expandable' title='click to expand'>[+] ").append(calls.size())
            .append(" stage").append(calls.size() > 1? "s": "").append("</span>")
            .append("<ul class='masked dev-stage-list'>");
        Integer previousGroupInd = null;
        for (ExpressionCall call: calls) {
            final DevStage stage = call.getCondition().getDevStage();
            int currentGroupInd = clusteringWithinAnatEntity.get(call);
            sb.append("<li class='dev-stage ");
            if (previousGroupInd != null && previousGroupInd != currentGroupInd) {
                sb.append(scoreShiftClassName);
            }
            sb.append("'><span class='details small'>")
                .append(htmlEntities(stage.getId())).append("</span>")
                .append(htmlEntities(stage.getName())).append("</li>");
            sb.append("\n");
            previousGroupInd = currentGroupInd;
        }
        sb.append("</ul></td>");
        
        //Global mean rank
        sb.append("<td>").append(getRankScoreHTML(calls.get(0)))
            .append("<ul class='masked score-list'>");
        previousGroupInd = null;
        for (ExpressionCall call: calls) {
            int currentGroupInd = clusteringWithinAnatEntity.get(call);
            sb.append("<li class='score ");
            if (previousGroupInd != null && previousGroupInd != currentGroupInd) {
                sb.append(scoreShiftClassName);
            }
            sb.append("'>").append(getRankScoreHTML(call))
              .append("</li>");
            sb.append("\n");
            previousGroupInd = currentGroupInd;
        }
        sb.append("</ul></td>");
        
      //Expression score
        sb.append("<td>").append(getExpressionScoreHTML(calls.get(0)))
            .append("<ul class='masked score-list'>");
        previousGroupInd = null;
        for (ExpressionCall call: calls) {
            int currentGroupInd = clusteringWithinAnatEntity.get(call);
            sb.append("<li class='score ");
            if (previousGroupInd != null && previousGroupInd != currentGroupInd) {
                sb.append(scoreShiftClassName);
            }
            sb.append("'>").append(getExpressionScoreHTML(call))
              .append("</li>");
            sb.append("\n");
            previousGroupInd = currentGroupInd;
        }
        sb.append("</ul></td>");

        // Quality cell
        sb.append("<td>")
                .append(getDataTypeSpans(calls.stream().flatMap(e -> e.getCallData().stream())
                        .collect(Collectors.toList())))
                .append("<ul class='masked quality-list'>")
                .append(calls.stream().map(call -> {
                        StringBuilder sb2 = new StringBuilder();
                        sb2.append("<li class='qualities'>")
                        .append(getDataTypeSpans(call.getCallData())).append("</li>");
                        return sb2.toString();
                    }).collect(Collectors.joining("\n")))
                .append("</ul></td>");
        
        sb.append("</tr>");

        return log.exit(sb.toString());
    }

    /**
     * Create a table containing general information for {@code Gene}
     * 
     * @param gene     The {@code Gene} for which to display information
     * @return         A {@code String} containing the HTML table containing the information.
     */
    private String getGeneralInfo(Gene gene) {
        log.entry(gene);

        final StringBuilder table = new StringBuilder("<div class='info-content'>");
        table.append("<table class='info-table'>");
        table.append("<tr><th scope='row'>Ensembl ID</th><td>")
                .append(htmlEntities(gene.getEnsemblGeneId())).append("</td></tr>");
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
        table.append("</table>");
        table.append("</div>");

        return log.exit(table.toString());
    }

    /**
     * Generates the HTML code to display the synonyms.
     *
     * @param synonyms  A {@code Set} of {@code String}s that are the synonyms to display 
     * @return          A {@code String} that is the HTML code to display synonyms
     */
    private static String getSynonymDisplay(Set<String> synonyms) {
        log.entry(synonyms);

        if (synonyms == null || synonyms.size() == 0) {
            return "No synonyms";
        }
        
        List<String> orderedEscapedSynonyms = synonyms.stream()
                .sorted()
                .map(s -> "<span property='bs:alternateName'>" + htmlEntities(s) + "</span>")
                .collect(Collectors.toList());

        String display = getListDisplay("syn", orderedEscapedSynonyms);
        return log.exit(display);
    }

    /**
     * Return the {@code String} that is the HTML code of the cross-references table.
     *
     * @param xRefs A {@code Set} of {@code XRef}s that are the cross-references to display
     * @return      A {@code String} containing the HTML code of the cross-references table
     */
    private String getXRefDisplay(Set<XRef> xRefs) {
        log.entry(xRefs);

        if (xRefs == null || xRefs.size() == 0) {
            return "No cross-references";
        }

        LinkedHashMap<Source, List<String>> xRefsBySource = new ArrayList<>(xRefs).stream()
                .filter(x -> StringUtils.isNotBlank(x.getSource().getXRefUrl()))
                .sorted(X_REF_COMPARATOR)
                .collect(Collectors.groupingBy(XRef::getSource,
                        LinkedHashMap::new,
                        Collectors.mapping(x -> "<a typeof='bs:Gene' property='bs:sameAs' href='"
                                + x.getXRefUrl(true, s -> this.urlEncode(s)) + "' target='_blank' rel='noopener'>"
                                + htmlEntities(x.getXRefId()) + "</a>" + htmlEntities(getFormattedXRefName(x)),
                            Collectors.toList())));
        StringBuilder display = new StringBuilder("<div class='info-content'>");
        display.append("<table class='info-table'>");

        for (Entry<Source, List<String>> entry : xRefsBySource.entrySet()) {
            Source source = entry.getKey();

            List<String> sourceXRefs = entry.getValue();
            sourceXRefs.sort(Comparator.naturalOrder());
            
            display.append("<tr>");
            
            display.append("<th>").append(htmlEntities(source.getName())).append("</th>");
            
            display.append("<td>");
            display.append(getListDisplay("source_" + source.getId(), sourceXRefs));
            display.append("</td>");
            
            display.append("</tr>");
        }
        display.append("</table>");
        display.append("</div>");

        return log.exit(display.toString());
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
        log.entry(xRef);
        String xRefName = "";
        if (StringUtils.isNotBlank(xRef.getXRefName())) {
            String[] split = xRef.getXRefName().split("; ");
            // If we have several names, we display only the first one.
            xRefName = " (" + split[0] + ")";
        }
        return log.exit(xRefName);
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
        log.entry(idPrefix, idPrefix);

        boolean tooManyItems = items.size() > MAX_DISPLAYED_ITEMS;

        String display = String.join(", ", tooManyItems?
                items.subList(0, MAX_DISPLAYED_ITEMS): items);
        if (tooManyItems) {
            display += "<span id='" + idPrefix + "_content' class='more-content'>, " +
                    String.join(", ", items.subList(MAX_DISPLAYED_ITEMS, items.size())) +
                    "</span>";
            display += " <span id='" + idPrefix + "_click' class='glyphicon glyphicon-plus'></span>";
        }
        return log.exit(display);
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
        log.entry(callData);
        final Map<DataType, Set<ExpressionCallData>> callsByDataTypes = callData.stream()
                .collect(Collectors.groupingBy(ExpressionCallData::getDataType, Collectors.toSet()));

        return log.exit(EnumSet.allOf(DataType.class).stream().map(type -> {
            return getDataSpan(type, callsByDataTypes.containsKey(type));
        }).collect(Collectors.joining()));
    }

    /**
     * Builds a 'span' element representing presence/absence of data for a given {@code DataType}.
     * 
     * @param hasData  A {@code boolean} defining whether there is data for {@code type}.
     * @param type     A {@code DataType} that is the data type for which 'span' should be displayed.
     * @return         The {@code String} containing the HTML code for the quality 'span'.
     */
    private static String getDataSpan(DataType type, boolean hasData) {
        log.entry(hasData, type);
        
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
        }
        sb.append("</span>");
        return log.exit(sb.toString());
    }
    
    /**
     * @param call An {@code ExpressionCall} for which we want to display global mean rank.
     * @return     A {@code String} containing the HTML to display the global mean rank, 
     *             notably displaying information about confidence in the rank.
     */
    private static String getRankScoreHTML(ExpressionCall call) {
        log.entry(call);

        //If the rank is above a threshold and is only supported by ESTs and/or in situ data, 
        //they we consider it of low confidence
        //TODO: there should be a better mechanism to handle that, and definitely not in the view, 
        //it is not its role to determine what is of low confidence...
        //Maybe create in bgee-core a new RankScore class, storing the rank and the confidence.
        Set<DataType> dataTypes = call.getCallData().stream().map(ExpressionCallData::getDataType)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(DataType.class)));
        String rankScore = htmlEntities(call.getFormattedMeanRank());
        if (!SummaryQuality.BRONZE.equals(call.getSummaryQuality()) && 
                (dataTypes.contains(DataType.AFFYMETRIX) || 
                dataTypes.contains(DataType.RNA_SEQ) || 
                call.getMeanRank().compareTo(BigDecimal.valueOf(20000)) < 0)) {
            return log.exit(rankScore);
        }
        StringBuilder sb = new StringBuilder();
        sb.append("<span class='low-qual-score'>").append(rankScore).append("</span>");
        return log.exit(sb.toString());
    }
    
    /**
     * @param call An {@code ExpressionCall} for which we want to display expression score.
     * @return     A {@code String} containing the HTML to display the expression score, 
     *             notably displaying information about confidence in the call.
     */
    private static String getExpressionScoreHTML(ExpressionCall call) {
        log.entry(call);

        //If the rank of the call is above a threshold AND the call is only supported by ESTs 
        //and/or in situ data, then we consider it of low confidence and the corresponding score
        //is written with a different color
        //TODO: there should be a better mechanism to handle that, and definitely not in the view, 
        //it is not its role to determine what is of low confidence...
        //Maybe create in bgee-core a new ExpressionScore class, storing the expression score and its
        //confidence.
        Set<DataType> dataTypes = call.getCallData().stream().map(ExpressionCallData::getDataType)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(DataType.class)));
        String expressionScore = htmlEntities(String.valueOf(call.getExpressionScore()));
        if (!SummaryQuality.BRONZE.equals(call.getSummaryQuality()) && 
                (dataTypes.contains(DataType.AFFYMETRIX) || 
                dataTypes.contains(DataType.RNA_SEQ) || 
                call.getMeanRank().compareTo(BigDecimal.valueOf(20000)) < 0)) {
            return log.exit(expressionScore);
        }
        StringBuilder sb = new StringBuilder();
        sb.append("<span class='low-qual-score'>").append(expressionScore).append("</span>");
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
            this.includeCss("lib/jquery_plugins/vendor_gene.css");
        }
        this.includeCss("gene.css");

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
            this.includeJs("gene.js");
            this.includeJs("autoCompleteGene.js");
            this.includeJs("jquery_ui_autocomplete_modif.js");
        } else {
            this.includeJs("lib/jquery_plugins/vendor_gene.js");
            this.includeJs("script_gene.js");
        }
        log.exit();
    }
}
