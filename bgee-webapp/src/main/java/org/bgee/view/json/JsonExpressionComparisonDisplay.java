package org.bgee.view.json;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.BgeeProperties;
import org.bgee.controller.RequestParameters;
import org.bgee.model.SearchResult;
import org.bgee.model.expressiondata.MultiGeneExprAnalysis;
import org.bgee.model.expressiondata.SingleSpeciesExprAnalysis;
import org.bgee.model.expressiondata.multispecies.MultiSpeciesExprAnalysis;
import org.bgee.model.gene.Gene;
import org.bgee.view.ExpressionComparisonDisplay;
import org.bgee.view.JsonHelper;

/**
 * Implementation in JSON of {@code ExpressionComparisonDisplay}
 *
 * @author Frederic Bastian
 * @version Bgee 15, Nov. 2021
 * @version Bgee 15, Nov. 2021
 */
public class JsonExpressionComparisonDisplay extends JsonParentDisplay implements ExpressionComparisonDisplay {

    private final static Logger log = LogManager.getLogger(JsonExpressionComparisonDisplay.class.getName());

    public JsonExpressionComparisonDisplay(HttpServletResponse response,
            RequestParameters requestParameters, BgeeProperties prop, JsonHelper jsonHelper,
            JsonFactory factory) throws IllegalArgumentException, IOException {
        super(response, requestParameters, prop, jsonHelper, factory);
    }

    @Override
    public void displayExpressionComparisonHomePage() {
        throw log.throwing(new UnsupportedOperationException("Not implemented for JSON answers"));
    }

    @Override
    public void displayExpressionComparison(String erroMsg) {
        throw log.throwing(new UnsupportedOperationException("Not implemented for JSON answers"));
    }

    @Override
    //TODO: now we have one request for the gene list and one request for the results.
    //We don't need to provide searchResult to this method anymore
    public void displayExpressionComparison(SearchResult<String, Gene> searchResult,
            SingleSpeciesExprAnalysis analysis) {
        log.traceEntry("{}, {}", searchResult, analysis);
        this.displayExpressionComparison(analysis);
        log.traceExit();
    }
    @Override
    //TODO: now we have one request for the gene list and one request for the results.
    //We don't need to provide searchResult to this method anymore
    public void displayExpressionComparison(SearchResult<String, Gene> searchResult,
            MultiSpeciesExprAnalysis analysis) {
        log.traceEntry("{}, {}", searchResult, analysis);
        this.displayExpressionComparison(analysis);
        log.traceExit();
    }
    private void displayExpressionComparison(MultiGeneExprAnalysis<?> analysis) {
        log.traceEntry("{}", analysis);
        this.sendResponse("", analysis);
        log.traceExit();
    }
}