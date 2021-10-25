package org.bgee.view.json;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.BgeeProperties;
import org.bgee.controller.RequestParameters;
import org.bgee.model.file.SpeciesDataGroup;
import org.bgee.view.DownloadDisplay;
import org.bgee.view.JsonHelper;

/**
 * Implementation in JSON of {@code DownloadDisplay}.
 *
 * @author Frederic Bastian
 * @version Bgee 15, Oct. 2021
 * @since Bgee 15, Oct. 2021
 */
public class JsonDownloadDisplay extends JsonParentDisplay implements DownloadDisplay {
    private final static Logger log = LogManager.getLogger(JsonGeneDisplay.class.getName());

    public JsonDownloadDisplay(HttpServletResponse response, RequestParameters requestParameters, BgeeProperties prop,
            JsonHelper jsonHelper, JsonFactory factory) throws IllegalArgumentException, IOException {
        super(response, requestParameters, prop, jsonHelper, factory);
    }

    @Override
    public void displayProcessedExpressionValuesDownloadPage(List<SpeciesDataGroup> groups,
            Map<Integer, Set<String>> keywords) {
        log.traceEntry("{}. {}", groups, keywords);
        this.displayDownloadPage("List of processed expression values download files",
                groups, keywords);
        log.traceExit();
    }

    @Override
    public void displayGeneExpressionCallDownloadPage(List<SpeciesDataGroup> groups,
            Map<Integer, Set<String>> keywords) {
        log.traceEntry("{}. {}", groups, keywords);
        this.displayDownloadPage("List of expression calls download files",
                groups, keywords);
        log.traceExit();
    }

    private void displayDownloadPage(String msg, List<SpeciesDataGroup> groups,
            Map<Integer, Set<String>> keywords) {
        log.traceEntry("{}. {}, {}", msg, groups, keywords);
        LinkedHashMap<String, Object> resultHashMap = new LinkedHashMap<String, Object>();
        resultHashMap.put("downloadFilesGroups", groups);
        resultHashMap.put("speciesIdToKeywords", keywords);
        this.sendResponse(msg, resultHashMap);
        log.traceExit();
    }

    @Override
    public void displayDumpsPage() {
        // TODO Auto-generated method stub
        
    }
}