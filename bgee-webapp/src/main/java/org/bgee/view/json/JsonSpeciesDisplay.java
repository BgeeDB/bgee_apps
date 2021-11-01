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
 * @version Bgee 15, Oct. 2021
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
        
        LinkedHashMap<String, Object> resultSpeciesHome = new LinkedHashMap<String, Object>();
        resultSpeciesHome.put("species", speciesList);
        this.sendResponse("List of species in Bgee", resultSpeciesHome);
    }
    

    @Override
    public void sendSpeciesResponse(List<Species> species) {
        log.traceEntry("{}", species);
        
        LinkedHashMap<String, Object> data = new LinkedHashMap<String, Object>();
        data.put(this.getRequestParameters().getUrlParametersInstance()
                .getParamSpeciesList().getName(), species);
        
        this.sendResponse("List of requested species", data);
        
        log.traceExit();
    }

    @Override
    public void displaySpecies(Species species, SpeciesDataGroup speciesDataGroup) {
        
        // create LinkedHashMap that we will pass to Gson in order to generate the JSON 
        LinkedHashMap<String, Object> jsonHashMap = new LinkedHashMap<String, Object>();
        jsonHashMap.put("species", species);
        jsonHashMap.put("downloadFilesGroups", speciesDataGroup);

        this.sendResponse("Details on species " + species.getScientificName(),
                jsonHashMap);
    }
}