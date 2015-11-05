package org.bgee.view.json;

import java.io.IOException;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.BgeeProperties;
import org.bgee.controller.RequestParameters;
import org.bgee.model.species.Species;
import org.bgee.view.JsonHelper;
import org.bgee.view.SpeciesDisplay;

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
     * @param factory           The {@code JsonFactory} that instantiated this object.
     * @throws IOException      If there is an issue when trying to get or to use the
     *                          {@code PrintWriter} 
     */
    public JsonSpeciesDisplay(HttpServletResponse response,
            RequestParameters requestParameters, BgeeProperties prop,
            JsonFactory factory) throws IllegalArgumentException, IOException {
        super(response, requestParameters, prop, factory);
    }

    @Override
    public void sendSpeciesResponse(Set<Species> species) {
        log.entry(species);

        log.debug(new JsonHelper().toJson(species));
        this.write(new JsonHelper().toJson(species));
        
        log.exit();
    }
}
