package org.bgee.view.json;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.BgeeProperties;
import org.bgee.controller.RequestParameters;
import org.bgee.model.source.Source;
import org.bgee.view.JsonHelper;
import org.bgee.view.SourceDisplay;

/**
 * JSON implementation of {@code SourceDisplay}.
 *
 * @author  Frederic Bastian
 * @version Bgee 15, Dec. 2021
 * @since   Bgee 15, Dec. 2021
 */
public class JsonSourceDisplay extends JsonParentDisplay implements SourceDisplay {
    private final static Logger log = LogManager.getLogger(JsonSourceDisplay.class.getName());

    public JsonSourceDisplay(HttpServletResponse response, RequestParameters requestParameters,
            BgeeProperties prop, JsonHelper jsonHelper, JsonFactory factory)
                    throws IllegalArgumentException, IOException {
        super(response, requestParameters, prop, jsonHelper, factory);
    }

    @Override
    public void displaySources(List<Source> sources) {
        log.traceEntry("{}", sources);
        LinkedHashMap<String, Object> resultHashMap = new LinkedHashMap<String, Object>();
        resultHashMap.put("sources", sources);
        this.sendResponse("Data sources used in Bgee", resultHashMap);
        log.traceExit();
    }
}