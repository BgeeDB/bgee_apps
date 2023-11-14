package org.bgee.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.exception.InvalidRequestException;
import org.bgee.model.SearchResult;
import org.bgee.model.ServiceFactory;
import org.bgee.model.expressiondata.call.SingleSpeciesExprAnalysis;
import org.bgee.model.expressiondata.call.multispecies.MultiSpeciesExprAnalysis;
import org.bgee.model.gene.Gene;
import org.bgee.model.species.Species;
import org.bgee.view.ExpressionComparisonDisplay;
import org.bgee.view.ViewFactory;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Controller handling requests related to expression comparison pages. 
 *
 * @author  Valentine Rech de Laval
 * @version Bgee 14, May 2019
 * @since   Bgee 14, May 2019
 */
public class CommandExpressionComparison extends CommandParent {
    
    private final static Logger log = LogManager.getLogger(CommandExpressionComparison.class.getName());

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
    public CommandExpressionComparison(HttpServletResponse response, RequestParameters requestParameters,
                                       BgeeProperties prop, ViewFactory viewFactory, ServiceFactory serviceFactory) {
        super(response, requestParameters, prop, viewFactory, serviceFactory);
    }

    @Override
    public void processRequest() throws Exception {
        log.traceEntry();

        final List<String> userGeneList = Collections.unmodifiableList(Optional.ofNullable(
                this.requestParameters.getGeneList()).orElse(new ArrayList<>()));

        ExpressionComparisonDisplay display = viewFactory.getExpressionComparisonDisplay();

        if (userGeneList.size() <= 1) {
            throw log.throwing(new InvalidRequestException("At least two IDs should be provided."));
        }

        SearchResult<String, Gene> searchResult = serviceFactory.getGeneService()
                .searchGenesByIds(userGeneList);
        Set<Species> species = searchResult.getResults().stream()
                .map(Gene::getSpecies).collect(Collectors.toSet());

        if (species.isEmpty()) {
            throw log.throwing(new InvalidRequestException(
                    "No gene from species presents in Bgee are detected."));
        }

        if (species.size() == 1) {
            SingleSpeciesExprAnalysis singleSpeciesExprAnalysis = serviceFactory.getCallService()
                    .loadSingleSpeciesExprAnalysis(searchResult.getResults());
            display.displayExpressionComparison(searchResult, singleSpeciesExprAnalysis);
            log.traceExit(); return;
        }

        MultiSpeciesExprAnalysis multiSpeciesExprAnalysis = serviceFactory.getMultiSpeciesCallService()
                .loadMultiSpeciesExprAnalysis(searchResult.getResults());
        display.displayExpressionComparison(searchResult, multiSpeciesExprAnalysis);
    }
}
