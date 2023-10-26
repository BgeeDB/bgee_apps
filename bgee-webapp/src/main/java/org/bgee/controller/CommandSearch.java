package org.bgee.controller;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.exception.InvalidRequestException;
import org.bgee.controller.exception.PageNotFoundException;
import org.bgee.model.ServiceFactory;
import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.expressiondata.rawdata.baseelements.ExperimentAssay;
import org.bgee.model.gene.Gene;
import org.bgee.model.search.SearchMatchResult;
import org.bgee.model.search.SearchMatchResultService;
import org.bgee.view.SearchDisplay;
import org.bgee.view.ViewFactory;

/**
 * Controller that handles requests having the category "search", i.e. with the parameter
 * page=search
 *
 * @author  Valentine Rech de Laval
 * @author  Frederic Bastian
 * @version Bgee 15.0, Jan. 2023
 * @since   Bgee 13, Feb. 2016
 */
public class CommandSearch extends CommandParent {

	/**
     * {@code Logger} of the class. 
     */
    private final static Logger log = LogManager.getLogger(CommandSearch.class.getName());

    /**
     * An {@code int} that is the maximum allowed number of results
     * to retrieve in one request. Value: 10,000.
     */
    private final static int LIMIT_MAX = 10000;
    /**
     * An {@code int} that is the default number of results
     * to retrieve in one request. Value: 100.
     */
    private final static int DEFAULT_LIMIT = 100;

    /**
     * Default constructor.
     *
     * @param response                  A {@code HttpServletResponse} that will be used to display the 
     *                                  page to the client
     * @param requestParameters         The {@code RequestParameters} that handles the parameters of the 
     *                                  current request.
     * @param prop                      A {@code BgeeProperties} instance that contains the properties
     *                                  to use.
     * @param viewFactory               A {@code ViewFactory} that provides the display type to be used.
     * @param serviceFactory            A {@code ServiceFactory} that provides bgee services.
     */
	public CommandSearch(HttpServletResponse response, RequestParameters requestParameters, 
            BgeeProperties prop, ViewFactory viewFactory, ServiceFactory serviceFactory) {
        super(response, requestParameters, prop, viewFactory, serviceFactory);
    }

    @Override
    public void processRequest() throws IOException, PageNotFoundException, InvalidRequestException {
        log.traceEntry();
        
        SearchDisplay display = this.viewFactory.getSearchDisplay();
        SearchMatchResultService searchMatchService = serviceFactory.getSearchMatchResultService(this.prop);
        int limit = this.requestParameters.getLimit() == null? DEFAULT_LIMIT:
            this.requestParameters.getLimit();
        if (limit > LIMIT_MAX) {
            throw log.throwing(new InvalidRequestException("It is not possible to request more than "
                    + LIMIT_MAX + " results."));
        }
        
        if (this.requestParameters.getAction() != null &&
        		this.requestParameters.getAction().equals(RequestParameters.ACTION_AUTO_COMPLETE_GENE_SEARCH)) {
            String searchTerm = this.getSearchTerm();
            List<String> result = searchMatchService.autocomplete(searchTerm, limit);
            display.displayMatchesForGeneCompletion(result);
            
        } else if (this.requestParameters.getAction() != null &&
                this.requestParameters.getAction().equals(RequestParameters.ACTION_EXPASY_RESULT)) {
            String searchTerm = this.getSearchTerm();
            SearchMatchResult<Gene> result = searchMatchService.searchGenesByTerm(searchTerm, null, 0, 1);
            display.displayExpasyResult(result.getTotalMatchCount(), searchTerm);

        } else if (this.requestParameters.getAction() != null &&
                this.requestParameters.getAction().equals(RequestParameters.ACTION_SEARCH_ANAT_ENTITIES)) {
            String searchTerm = this.getSearchTerm();
            Integer speciesId = this.requestParameters.getSpeciesId();
            SearchMatchResult<AnatEntity> result = serviceFactory
                    .getSearchMatchResultService(this.prop)
                    .searchAnatEntitiesByTerm(searchTerm, speciesId == null? null: Set.of(speciesId),
                            true, false, 0, limit);
            display.displayDefaultSphinxSearchResult(searchTerm, result);

        } else if (this.requestParameters.getAction() != null &&
                this.requestParameters.getAction().equals(RequestParameters.ACTION_SEARCH_STRAINS)) {
            String searchTerm = this.getSearchTerm();
            Integer speciesId = this.requestParameters.getSpeciesId();
            SearchMatchResult<String> result = serviceFactory
                    .getSearchMatchResultService(this.prop)
                    .searchStrainsByTerm(searchTerm, speciesId == null? null: Set.of(speciesId),
                            0, limit);
            display.displayDefaultSphinxSearchResult(searchTerm, result);

        } else if (this.requestParameters.getAction() != null &&
                this.requestParameters.getAction().equals(RequestParameters.ACTION_SEARCH_CELL_TYPES)) {
            String searchTerm = this.getSearchTerm();
            Integer speciesId = this.requestParameters.getSpeciesId();
            SearchMatchResult<AnatEntity> result = serviceFactory
                    .getSearchMatchResultService(this.prop)
                    .searchAnatEntitiesByTerm(searchTerm, speciesId == null? null: Set.of(speciesId),
                            false, true, 0, limit);
            display.displayDefaultSphinxSearchResult(searchTerm, result);
        } else if (this.requestParameters.getAction() != null &&
                this.requestParameters.getAction().equals(RequestParameters.ACTION_SEARCH_EXPERIMENTS_ASSAYS)) {
            String searchTerm = this.getSearchTerm();
            SearchMatchResult<ExperimentAssay> result = serviceFactory
                    .getSearchMatchResultService(this.prop)
                    .searchExperimentsAndAssaysByTerm(searchTerm, 0, limit);
            display.displayDefaultSphinxSearchResult(searchTerm, result);
        } else {
            throw log.throwing(new PageNotFoundException("Incorrect " + 
                this.requestParameters.getUrlParametersInstance().getParamAction() + 
                " parameter value."));
        }
        
        log.traceExit();
    }

    private String getSearchTerm() throws InvalidRequestException {
        String searchTerm = this.requestParameters.getQuery();
        if (StringUtils.isBlank(searchTerm)) {
            throw log.throwing(new InvalidRequestException("Blank search term provided."));
        }
        return searchTerm;
    }
}
