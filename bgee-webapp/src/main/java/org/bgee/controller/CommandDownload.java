package org.bgee.controller;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.exception.PageNotFoundException;
import org.bgee.model.ServiceFactory;
import org.bgee.model.file.SpeciesDataGroup;
import org.bgee.view.DownloadDisplay;
import org.bgee.view.ViewFactory;

/**
 * Controller that handles requests having the category "download", i.e. with the parameter
 * page=download
 * 
 * @author  Mathieu Seppey
 * @author  Valentine Rech de Laval
 * @author  Philippe Moret
 * @author  Frederic Bastian
 * @version Bgee 15, Oct. 2021
 * @since   Bgee 13
 */
public class CommandDownload extends CommandParent {

    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = LogManager.getLogger(CommandDownload.class.getName());

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
    public CommandDownload (HttpServletResponse response, RequestParameters requestParameters, 
            BgeeProperties prop, ViewFactory viewFactory, ServiceFactory serviceFactory) {
        super(response, requestParameters, prop, viewFactory, serviceFactory);
    }

    @Override
    public void processRequest() throws IllegalStateException, IOException, PageNotFoundException {
        log.traceEntry();

        DownloadDisplay display = this.viewFactory.getDownloadDisplay();
        if (this.requestParameters.getAction() != null && (
                this.requestParameters.getAction().equals(
                RequestParameters.ACTION_DOWLOAD_PROC_VALUE_FILES) || 
                   this.requestParameters.getAction().equals(
                RequestParameters.ACTION_DOWLOAD_CALL_FILES))) {

            if (this.requestParameters.getAction().equals(
                    RequestParameters.ACTION_DOWLOAD_PROC_VALUE_FILES)) {
                List<SpeciesDataGroup> groups =
                        serviceFactory.getSpeciesDataGroupService().loadSpeciesDataGroup(false, true);
                Map<Integer, Set<String>> speciesIdsToTerms = getSpeciesRelatedTerms(groups);
                display.displayProcessedExpressionValuesDownloadPage(groups, speciesIdsToTerms);
            } else {
                List<SpeciesDataGroup> groups =
                        serviceFactory.getSpeciesDataGroupService().loadSpeciesDataGroup(true, false);
                Map<Integer, Set<String>> speciesIdsToTerms = getSpeciesRelatedTerms(groups);
                display.displayGeneExpressionCallDownloadPage(groups, speciesIdsToTerms);
            }
        } else {
            throw log.throwing(new PageNotFoundException("Incorrect " + 
                this.requestParameters.getUrlParametersInstance().getParamAction() + 
                " parameter value."));
        }
        
        log.traceExit();
    }
    
    /**
     * Gets a {@code Map} of terms related to species: IDs, common names, scientific names, 
     * and short names (for instance, "M. musculus"), as well as alternative names (e.g., "mice") 
     * retrieved from a {@code KeywordService}. 
     * 
     * @param groups    A {@code Set} of {@code SpeciesDataGroup} to retrieve information from. 
     * @return          A {@code Map} where keys are {@code Integer}s that are species IDs, 
     *                  the associated values being a {@code Set} of {@code String}s that are 
     *                  related terms.
     */
    private Map<Integer, Set<String>> getSpeciesRelatedTerms(Collection<SpeciesDataGroup> groups) {
        log.traceEntry("{}", groups);

        //first, associate species IDs to their corresponding name variations and to their ID itself.
        Map<Integer, Set<String>> speciesToNames = groups.stream()
                .flatMap(e -> e.getMembers().stream()) //get a Stream of Species
                .distinct()                            //keep only distinct Species objects
                .collect(Collectors.toMap(e -> e.getId(), e -> new HashSet<>(Arrays.asList(
                        String.valueOf(e.getId()), e.getName(), e.getScientificName(), e.getShortName()))));

        //now, we retrieve keywords associated to species (they correspond to alternative names), 
        //and add them to the mapping
        final Map<Integer, Set<String>> speciesToKeywords = serviceFactory.getKeywordService()
                .getKeywordForAllSpecies();
        Map<Integer, Set<String>> speciesToTerms = speciesToNames.entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey(), e -> {
                    Set<String> newVals = new HashSet<>(e.getValue()); 
                    newVals.addAll(speciesToKeywords.getOrDefault(e.getKey(), new HashSet<String>()));
                    return newVals;
                }));

        return log.traceExit(speciesToTerms);
    }
}
