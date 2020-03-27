package org.bgee.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.SearchResult;
import org.bgee.model.ServiceFactory;
import org.bgee.model.expressiondata.SingleSpeciesExprAnalysis;
import org.bgee.model.expressiondata.multispecies.MultiSpeciesExprAnalysis;
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
        log.entry();

        final List<String> userGeneList = Collections.unmodifiableList(Optional.ofNullable(
                this.requestParameters.getGeneList()).orElse(new ArrayList<>()));

        ExpressionComparisonDisplay display = viewFactory.getExpressionComparisonDisplay();

        if (userGeneList.isEmpty()) {
            display.displayExpressionComparisonHomePage();
            log.exit(); return;
        }
        if (userGeneList.size() == 1) {
            display.displayExpressionComparison("At least two Ensembl IDs should be provided.");
            log.exit(); return;
        }

        SearchResult<String, Gene> searchResult = serviceFactory.getGeneService()
                .searchGenesByEnsemblIds(userGeneList);
        Set<Species> species = searchResult.getResults().stream()
                .map(Gene::getSpecies).collect(Collectors.toSet());

        if (species.isEmpty()) {
            display.displayExpressionComparison("No gene from species presents in Bgee are detected.");
            log.exit(); return;
        }

        if (species.size() == 1) {
            SingleSpeciesExprAnalysis singleSpeciesExprAnalysis = serviceFactory.getCallService()
                    .loadSingleSpeciesExprAnalysis(searchResult.getResults());
            display.displayExpressionComparison(searchResult, singleSpeciesExprAnalysis);
            log.exit(); return;
        }

        MultiSpeciesExprAnalysis multiSpeciesExprAnalysis = serviceFactory.getMultiSpeciesCallService()
                .loadMultiSpeciesExprAnalysis(searchResult.getResults());
        display.displayExpressionComparison(searchResult, multiSpeciesExprAnalysis);
    }
}
