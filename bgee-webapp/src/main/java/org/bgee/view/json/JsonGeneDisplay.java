package org.bgee.view.json;

import java.io.IOException;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.BgeeProperties;
import org.bgee.controller.CommandGene.GeneExpressionResponse;
import org.bgee.controller.RequestParameters;
import org.bgee.model.expressiondata.baseelements.SummaryCallType.ExpressionSummary;
import org.bgee.model.gene.Gene;
import org.bgee.model.gene.GeneHomologs;
import org.bgee.model.search.SearchMatchResult;
import org.bgee.view.GeneDisplay;
import org.bgee.view.JsonHelper;

/**
 * Implementation in JSON of {@code GeneDisplay}
 *
 * @author Frederic Bastian
 * @author Theo Cavinato
 * @version Bgee 15, Oct. 2021
 * @version Bgee 15, Oct. 2021
 */
public class JsonGeneDisplay extends JsonParentDisplay implements GeneDisplay {

    private final static Logger log = LogManager.getLogger(JsonGeneDisplay.class.getName());

    public JsonGeneDisplay(HttpServletResponse response, RequestParameters requestParameters, BgeeProperties prop,
            JsonHelper jsonHelper, JsonFactory factory) throws IllegalArgumentException, IOException {
        super(response, requestParameters, prop, jsonHelper, factory);
    }

    @Override
    public void displayGeneSearchResult(String searchTerm, SearchMatchResult<Gene> result) {
        log.traceEntry("{}. {}", searchTerm, result);
        LinkedHashMap<String, Object> resultHashMap = new LinkedHashMap<String, Object>();
        resultHashMap.put("query", searchTerm);
        resultHashMap.put("result", result);
        this.sendResponse("Gene search result",
                resultHashMap);
        log.traceExit();
        
    }

    @Override
    public void displayGeneGeneralInformation(Collection<Gene> genes) {
        log.traceEntry("{}", genes);

        // create LinkedHashMap to generate the JSON
        LinkedHashMap<String, Object> resultHashMap = new LinkedHashMap<String, Object>();
        resultHashMap.put("genes", genes.stream()
            .sorted(Comparator.comparing(g -> g.getSpecies() == null?
                    null: g.getSpecies().getPreferredDisplayOrder(),
                    Comparator.nullsLast(Comparator.naturalOrder())))
            .collect(Collectors.toList()));

        //All genes are supposed to have the same ID here
        Set<String> ids = genes.stream().map(g -> g.getGeneId()).collect(Collectors.toSet());
        if (ids.size() != 1) {
            throw log.throwing(new IllegalArgumentException("All genes should have the same ID"));
        }
        String id = ids.iterator().next();
        String msg = "General information for gene " + id;

        this.sendResponse(msg, resultHashMap);
        log.traceExit();
    }

    @Override
    public void displayGeneHomologs(GeneHomologs geneHomologs) {
        log.traceEntry("{}", geneHomologs);
        Gene gene = geneHomologs.getGene();
        String msg = "Homology information for gene: " + gene.getName() + " - " + gene.getGeneId();
        this.sendResponse(msg, geneHomologs);
        log.traceExit();
    }

    @Override
    public void displayGeneXRefs(Gene gene) {
        log.traceEntry("{}", gene);
        String msg = "Cross-reference information for gene: " + gene.getName() + " - " + gene.getGeneId();
        LinkedHashMap<String, Object> resultHashMap = new LinkedHashMap<String, Object>();
        resultHashMap.put("gene", gene);
        this.sendResponse(msg, resultHashMap);
        log.traceExit();
    }

    @Override
    public void displayGeneExpression(GeneExpressionResponse geneExpressionResponse) {
        log.traceEntry("{}", geneExpressionResponse);
        //See notes in CommandGene about retrieving the Gene even if there is no expression result.
        //TODO: refactor with code in HtmlGeneDisplay#displayGeneExpression(GeneExpressionResponse)
        String geneId = null;
        String geneName = null;
        if (geneExpressionResponse.getCalls() == null || geneExpressionResponse.getCalls().isEmpty()) {
            geneId = this.getRequestParameters().getGeneId();
        } else {
            Gene gene = geneExpressionResponse.getCalls().iterator().next().getGene();
            geneId = gene.getGeneId();
            geneName = gene.getName();
        }
        String msg = ExpressionSummary.NOT_EXPRESSED.equals(geneExpressionResponse.getCallType())?
                "Reported absence of expression": "Gene expression"
                + " for gene: " + geneId + (geneName != null? " - " + geneName: "");
        this.sendResponse(msg, geneExpressionResponse);
        log.traceExit();
    }

    @Override
    public void displayGeneChoice(Set<Gene> genes) {
        // TODO Auto-generated method stub
        throw log.throwing(new UnsupportedOperationException("Not available for JSON display"));

    }

    //FIXME: to remove once everything is moved to JsonHelper
    protected String urlEncode(String stringToWrite) {
        log.traceEntry("{}", stringToWrite);
        try {
            return log.traceExit(java.net.URLEncoder.encode(stringToWrite, "UTF-8"));
        } catch (Exception e) {
            log.catching(e);
            return log.traceExit("");
        }
    }
}
