package org.bgee.view.json;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.BgeeProperties;
import org.bgee.controller.RequestParameters;
import org.bgee.model.file.SpeciesDataGroup;
import org.bgee.model.species.Species;
import org.bgee.view.JsonHelper;
import org.bgee.view.SpeciesDisplay;

/**
 * This class is the JSON view of the {@code SpeciesDisplay}.
 *
 * @author  Frederic Bastian
 * @author  Valentine Rech de Laval
 * @version Bgee 14, July 2019
 * @since   Bgee 13, June 2015
 */
public class JsonSpeciesDisplay extends JsonParentDisplay implements SpeciesDisplay {

    private final static Logger log = LogManager.getLogger(JsonSpeciesDisplay.class.getName());

    /**
     * Constructor providing the necessary dependencies. 
     * 
     * @param response          A {@code HttpServletResponse} that will be used to display the 
     *                          page to the client
     * @param requestParameters The {@code RequestParameters} handling the parameters of the 
     *                          current request.
     * @param prop              A {@code BgeeProperties} instance that contains the properties
     *                          to use.
     * @param jsonHelper        A {@code JsonHelper} used to dump variables into Json.
     * @param factory           The {@code JsonFactory} that instantiated this object.
     * 
     * @throws IllegalArgumentException If {@code factory} or {@code jsonHelper} is {@code null}.
     * @throws IOException      If there is an issue when trying to get or to use the
     *                          {@code PrintWriter} 
     */
    public JsonSpeciesDisplay(HttpServletResponse response,
            RequestParameters requestParameters, BgeeProperties prop,
            JsonHelper jsonHelper, JsonFactory factory) throws IllegalArgumentException, IOException {
        super(response, requestParameters, prop, jsonHelper, factory);
    }

    @Override
    public void displaySpeciesHomePage(List<Species> speciesList) {
        throw log.throwing(new UnsupportedOperationException("Not available for JSON display"));
    }

    @Override
    public void sendSpeciesResponse(List<Species> species) {
        log.entry(species);
        
        LinkedHashMap<String, Object> data = new LinkedHashMap<>();
        data.put(this.getRequestParameters().getUrlParametersInstance()
                .getParamSpeciesList().getName(), species);
        
        this.sendResponse("List of requested species", data);
        
        log.exit();
    }

    @Override
    public void displaySpecies(Species species, SpeciesDataGroup speciesDataGroup) {
        throw log.throwing(new UnsupportedOperationException("Not available for JSON display"));
    }
}
