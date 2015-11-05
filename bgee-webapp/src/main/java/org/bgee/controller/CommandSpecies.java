package org.bgee.controller;

import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.species.Species;
import org.bgee.view.SpeciesDisplay;
import org.bgee.view.ViewFactory;

/**
 * Controller handling requests relative to species.
 * 
 * @author  Valentine Rech de Laval
 * @version Bgee 13 Nov 2015
 * @since   Bgee 13 Nov 2015
 */
public class CommandSpecies extends CommandParent {

    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = LogManager.getLogger(CommandSpecies.class.getName());
    
    /**
     * Constructor providing necessary dependencies. 
     * 
     * @param response          A {@code HttpServletResponse} that will be used 
     *                          to display the page to the client
     * @param requestParameters The {@code RequestParameters} that handles 
     *                          the parameters of the current request.
     * @param prop              A {@code BgeeProperties} instance that contains 
     *                          the properties to use.
     * @param viewFactory       A {@code ViewFactory} providing the views of the appropriate 
     *                          display type.
     */
    public CommandSpecies(HttpServletResponse response,
            RequestParameters requestParameters, BgeeProperties prop,
            ViewFactory viewFactory) {
        super(response, requestParameters, prop, viewFactory);
    }

    @Override
    public void processRequest() throws Exception {
        log.entry();
        
        SpeciesDisplay display = this.viewFactory.getSpeciesDisplay();
        
        // Get submitted species IDs
        Set<String> submittedSpeciesIds = new HashSet<String>(requestParameters.getValues(
                requestParameters.getUrlParametersInstance().getParamSpeciesId()));

        // Load detected species
        Set<Species> species = serviceFactory.getSpeciesService().
                loadSpeciesByIds(submittedSpeciesIds);
        if (species.isEmpty()) {
            throw log.throwing(new IllegalStateException(
                    "A SpeciesService did not allow to obtain any Species."));
        }

        display.sendSpeciesResponse(species);

        log.exit();
    }
}
