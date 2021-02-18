package org.bgee.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.ServiceFactory;
import org.bgee.model.anatdev.multispemapping.AnatEntitySimilarityAnalysis;
import org.bgee.model.ontology.Ontology;
import org.bgee.model.species.Species;
import org.bgee.model.species.Taxon;
import org.bgee.view.AnatomicalSimilarityDisplay;
import org.bgee.view.ViewFactory;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Controller handling requests related to anatomical similarities.
 *
 * @author  Valentine Rech de Laval
 * @version Bgee 14, May 2019
 * @since   Bgee 14, May 2019
 */
public class CommandAnatomicalSimilarity extends CommandParent {

    /**
     * {@code Logger} of the class.
     */
    private final static Logger log = LogManager.getLogger(CommandAnatomicalSimilarity.class.getName());

    public CommandAnatomicalSimilarity(HttpServletResponse response, RequestParameters requestParameters,
                                       BgeeProperties prop, ViewFactory viewFactory,
                                       ServiceFactory serviceFactory) {
        super(response, requestParameters, prop, viewFactory, serviceFactory);
    }

    @Override
    public void processRequest() throws Exception {
        log.traceEntry();

        final List<Integer> speciesList = Collections.unmodifiableList(Optional.ofNullable(
                this.requestParameters.getSpeciesList()).orElse(new ArrayList<>()));

        final List<String> anatEntityList = Collections.unmodifiableList(Optional.ofNullable(
                this.requestParameters.getAnatEntityList()).orElse(new ArrayList<>()));

        Set<Species> allSpecies = serviceFactory.getSpeciesService().loadSpeciesByIds(null, false);
        
        AnatomicalSimilarityDisplay display = viewFactory.getAnatomicalSimilarityDisplay();
        
        if (speciesList.isEmpty() && anatEntityList.isEmpty()) {
            display.displayAnatSimilarityHomePage(allSpecies);
            return;
        }
        
        AnatEntitySimilarityAnalysis anatEntitySimilarityAnalysis = serviceFactory
                .getAnatEntitySimilarityService()
                .loadPositiveAnatEntitySimilarityAnalysis(speciesList, anatEntityList, false);

        Ontology<Taxon, Integer> taxonOntology = serviceFactory.getOntologyService()
                .getTaxonOntologyFromSpeciesLCA(speciesList, false, true, false);
        
        display.displayAnatSimilarityResult(allSpecies, speciesList, taxonOntology, 
                anatEntityList, anatEntitySimilarityAnalysis);

        log.traceExit();
    }
}
