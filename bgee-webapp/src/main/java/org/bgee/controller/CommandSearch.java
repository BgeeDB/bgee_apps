package org.bgee.controller;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.exception.PageNotFoundException;
import org.bgee.model.ServiceFactory;
import org.bgee.model.gene.Gene;
import org.bgee.model.gene.GeneMatch;
import org.bgee.view.SearchDisplay;
import org.bgee.view.ViewFactory;

/**
 * Controller that handles requests having the category "search", i.e. with the parameter
 * page=search
 *
 * @author  Valentine Rech de Laval
 * @version Bgee 13, Feb. 2016
 * @since   Bgee 13, Feb. 2016
 */
public class CommandSearch extends CommandParent {

	/**
     * {@code Logger} of the class. 
     */
    private final static Logger log = LogManager.getLogger(CommandSearch.class.getName());

    /**
     * Default constructor.
     *
     * @param response          A {@code HttpServletResponse} that will be used to display the 
     *                          page to the client
     * @param requestParameters The {@code RequestParameters} that handles the parameters of the 
     *                          current request.
     * @param prop              A {@code BgeeProperties} instance that contains the properties
     *                          to use.
     * @param viewFactory       A {@code ViewFactory} that provides the display type to be used.
     */
	public CommandSearch(HttpServletResponse response, RequestParameters requestParameters, 
            BgeeProperties prop, ViewFactory viewFactory, ServiceFactory serviceFactory) {
        super(response, requestParameters, prop, viewFactory, serviceFactory);
    }

    @Override
    public void processRequest() throws IOException, PageNotFoundException {
        log.entry();
        
        SearchDisplay display = this.viewFactory.getSearchDisplay();
        
        if (this.requestParameters.getAction() != null &&
        		this.requestParameters.getAction().equals(RequestParameters.ACTION_AUTO_COMPLETE_GENE_SEARCH)) {
        	String searchTerm = this.requestParameters.getSearch();
        	
        	//TODO use service instead of fake GeneMatch
        	/*Gene gene1 = new Gene("ID1","spID1", "name1");
        	GeneMatch geneMatch1 = new GeneMatch(gene1, "syn1");
        	Gene gene2 = new Gene("ID2","spID2", "name2");
        	GeneMatch geneMatch2 = new GeneMatch(gene2, "syn2");

            List<GeneMatch> geneMatches = Arrays.asList(geneMatch1, geneMatch2);
//            		serviceFactory.getGeneService().searchByTerm(this.requestParameters.getSearch());*/
            List<GeneMatch> geneMatches = serviceFactory.getGeneService().searchByTerm(searchTerm);
            
            display.displayGeneCompletionByGeneList(geneMatches, this.requestParameters.getSearch());
        } else {
            throw log.throwing(new PageNotFoundException("Incorrect " + 
                this.requestParameters.getUrlParametersInstance().getParamAction() + 
                " parameter value."));
        }
        
        log.exit();
    }

}
