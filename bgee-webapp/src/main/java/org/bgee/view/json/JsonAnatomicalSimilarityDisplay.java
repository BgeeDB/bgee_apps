package org.bgee.view.json;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.BgeeProperties;
import org.bgee.controller.RequestParameters;
import org.bgee.model.anatdev.multispemapping.AnatEntitySimilarityAnalysis;
import org.bgee.model.species.Species;
import org.bgee.view.AnatomicalSimilarityDisplay;
import org.bgee.view.JsonHelper;

/**
 * JSON implementation of {@code AnatomicalSimilarityDisplay}.
 *
 * @author  Frederic Bastian
 * @version Bgee 15, Dec. 2021
 * @since   Bgee 15, Dec. 2021
 */
public class JsonAnatomicalSimilarityDisplay extends JsonParentDisplay implements AnatomicalSimilarityDisplay {
    private final static Logger log = LogManager.getLogger(JsonAnatomicalSimilarityDisplay.class.getName());

    public JsonAnatomicalSimilarityDisplay(HttpServletResponse response,
            RequestParameters requestParameters, BgeeProperties prop, JsonHelper jsonHelper,
            JsonFactory factory) throws IllegalArgumentException, IOException {
        super(response, requestParameters, prop, jsonHelper, factory);
    }

    @Override
    public void displayAnatSimilarityHomePage(Set<Species> allSpecies) {
        // TODO Auto-generated method stub
    }

    @Override
    //TODO: we don't need the "allSpecies" anymore to display the form.
    //We also don't need the userSpeciesList but maybe we can keep it in the method signature
    public void displayAnatSimilarityResult(Set<Species> allSpecies,  List<Integer> userSpeciesList,
             List<String> userAnatEntityList, AnatEntitySimilarityAnalysis anatEntitySimilarityAnalysis) {
        log.traceEntry("{}, {}, {}, {}", allSpecies, userSpeciesList, userAnatEntityList,
                anatEntitySimilarityAnalysis);
        this.sendResponse(HttpServletResponse.SC_OK, "Anatomical similarity results",
                anatEntitySimilarityAnalysis, true);
        log.traceExit();
    }
}
