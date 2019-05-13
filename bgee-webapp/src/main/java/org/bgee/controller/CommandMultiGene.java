package org.bgee.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.ServiceFactory;
import org.bgee.model.anatdev.multispemapping.AnatEntitySimilarityAnalysis;
import org.bgee.model.ontology.Ontology;
import org.bgee.model.species.Species;
import org.bgee.model.species.Taxon;
import org.bgee.view.MultiGeneDisplay;
import org.bgee.view.ViewFactory;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Controller handling requests related to multi gene pages. 
 *
 * @author  Valentine Rech de Laval
 * @version Bgee 14, May 2019
 * @since   Bgee 14, May 2019
 */
public class CommandMultiGene extends CommandParent {
    private final static Logger log = LogManager.getLogger(CommandGene.class.getName());

    /**
     * Constructor
     *
     * @param response          A {@code HttpServletResponse} that will be used to display the 
     *                          page to the client
     * @param requestParameters The {@code RequestParameters} that handles the parameters of the 
     *                          current request.
     * @param prop              A {@code BgeeProperties} instance that contains the properties
     *                          to use.
     * @param viewFactory       A {@code ViewFactory} that provides the display type to be used.
     * @param serviceFactory    A {@code ServiceFactory} that provides bgee services.
     */
    public CommandMultiGene(HttpServletResponse response, RequestParameters requestParameters,
                            BgeeProperties prop, ViewFactory viewFactory, ServiceFactory serviceFactory) {
        super(response, requestParameters, prop, viewFactory, serviceFactory);
    }

    @Override
    public void processRequest() throws Exception {
        log.entry();

        final List<String> geneList = Collections.unmodifiableList(Optional.ofNullable(
                this.requestParameters.getGeneList()).orElse(new ArrayList<>()));

        MultiGeneDisplay display = viewFactory.getMultiGeneDisplay();

        if (geneList.isEmpty()) {
            display.displayMultiGeneHomePage();
            return;
        }

        //FIXME Call the service
        
        display.displayMultiGene(geneList);

        log.exit();

    }
}
