package org.bgee.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.ServiceFactory;
import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.expressiondata.Condition;
import org.bgee.model.expressiondata.MultiGeneExprAnalysis;
import org.bgee.model.expressiondata.MultiGeneExprAnalysis.MultiGeneExprCounts;
import org.bgee.model.expressiondata.SingleSpeciesExprAnalysis;
import org.bgee.model.expressiondata.baseelements.SummaryCallType;
import org.bgee.model.expressiondata.multispecies.MultiSpeciesExprAnalysis;
import org.bgee.model.gene.Gene;
import org.bgee.model.gene.GeneBioType;
import org.bgee.model.species.Species;
import org.bgee.view.ExpressionComparisonDisplay;
import org.bgee.view.ViewFactory;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

        // FIXME to be removed when mock is removed
        Set<Gene> genes = serviceFactory.getGeneService()
                .loadGenesByEnsemblIds(userGeneList).collect(Collectors.toSet());
        Set<Species> species = serviceFactory.getGeneService()
                .loadGenesByEnsemblIds(userGeneList).map(Gene::getSpecies).collect(Collectors.toSet());


        if (species.isEmpty()) {
            display.displayExpressionComparison(userGeneList);
            log.exit(); return;
        }
        
        if (species.size() == 1) {
//            SingleSpeciesExprAnalysis singleSpeciesExprAnalysis = serviceFactory.getCallService().loadMultiSpeciesExprAnalysis(userGeneList);
            Map<Condition, MultiGeneExprAnalysis.MultiGeneExprCounts> condToCounts = new HashMap<>();
            AnatEntity anatEntity = new AnatEntity("AE ID", "AE name", "AE desc");
            Species sp1 = new Species(9690);
            Species sp2 = new Species(9690);
            GeneBioType biotype = new GeneBioType("b");

            Gene g1 = new Gene("ID1", "name1", null, sp1, biotype, 1);
            Gene g2 = new Gene("ID2", "name2", null, sp1, biotype, 1);
            Gene g3 = new Gene("ID3", "name3", null, sp1, biotype, 1);
            Gene g4 = new Gene("ID4", "name4", null, sp2, biotype, 1);

            Map<SummaryCallType, Collection<Gene>> callTypeToGenes = new HashMap<>();
            callTypeToGenes.put(SummaryCallType.ExpressionSummary.EXPRESSED, Arrays.asList(g1, g2));
            callTypeToGenes.put(SummaryCallType.ExpressionSummary.NOT_EXPRESSED, Arrays.asList(g3));
            
            MultiGeneExprCounts counts = new MultiGeneExprCounts(callTypeToGenes, Arrays.asList(g4));
            condToCounts.put(new Condition(anatEntity, null, sp1), counts);
            SingleSpeciesExprAnalysis singleSpeciesExprAnalysis = new SingleSpeciesExprAnalysis(
                    userGeneList, Arrays.asList("Unknown ID"), genes, condToCounts);
            
            display.displayExpressionComparison(userGeneList, singleSpeciesExprAnalysis);
            log.exit(); return;
        }

        MultiSpeciesExprAnalysis multiSpeciesExprAnalysis = serviceFactory.getMultiSpeciesCallService().loadMultiSpeciesExprAnalysis(userGeneList);
        display.displayExpressionComparison(userGeneList, multiSpeciesExprAnalysis);
    }
}
