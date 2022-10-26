package org.bgee.view.json;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.BgeeProperties;
import org.bgee.controller.RequestParameters;
import org.bgee.model.anatdev.DevStage;
import org.bgee.model.search.SearchMatchResult;
import org.bgee.view.JsonHelper;
import org.bgee.view.SearchDisplay;

/**
 * JSON implementation of {@code SearchDisplay}.
 *
 * @author Frederic Bastian
 * @version Bgee 15, Oct. 2021
 * @since Bgee 15, Oct. 2021
 */
public class JsonSearchDisplay extends JsonParentDisplay implements SearchDisplay {
    private final static Logger log = LogManager.getLogger(JsonSearchDisplay.class.getName());

    public JsonSearchDisplay(HttpServletResponse response, RequestParameters requestParameters,
            BgeeProperties prop, JsonHelper jsonHelper, JsonFactory factory)
                    throws IllegalArgumentException, IOException {
        super(response, requestParameters, prop, jsonHelper, factory);
    }

    @Override
    public void displayExpasyResult(int count, String searchTerm) {
        throw log.throwing(new UnsupportedOperationException("Not available for JSON display"));
    }

    @Override
    public void displayMatchesForGeneCompletion(Collection<String> matches) {
        log.traceEntry("{}", matches);

        LinkedHashMap<String, Object> resultHashMap = new LinkedHashMap<String, Object>();
        if (matches == null || matches.isEmpty()) {
            resultHashMap.put("matchCount", 0);
        } else {
            resultHashMap.put("matchCount", matches.size());
            resultHashMap.put("match", matches);
        }
        this.sendResponse("Gene autocompletion request", resultHashMap);

        log.traceExit();
    }

    @Override
    public void displayDefaultSphinxSearchResult(String searchTerm,
            SearchMatchResult<?> result) {
        log.traceEntry("{}. {}", searchTerm, result);
        LinkedHashMap<String, Object> resultHashMap = new LinkedHashMap<String, Object>();
        resultHashMap.put("query", searchTerm);
        resultHashMap.put("result", result);
        this.sendResponse("Search result",
                resultHashMap);
        log.traceExit();
    }


    @Override
    public void displayDevStageSearchResult(Set<DevStage> result) {
        log.traceEntry("{}. {}", result);
        LinkedHashMap<String, Object> resultHashMap = new LinkedHashMap<String, Object>();
        resultHashMap.put("result", result);
        this.sendResponse("Search result",
                resultHashMap);
        log.traceExit();
    }
}