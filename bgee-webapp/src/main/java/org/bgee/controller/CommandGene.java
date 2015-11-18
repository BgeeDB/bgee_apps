package org.bgee.controller;

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.ServiceFactory;
import org.bgee.model.gene.Gene;
import org.bgee.view.GeneDisplay;
import org.bgee.view.ViewFactory;

/**
 * 
 * @author Philippe Moret
 * @version Bgee 13, Nov.
 * @since   Bgee 13, Nov.
 */
public class CommandGene extends CommandParent {

    private final static Logger log = LogManager.getLogger(CommandGene.class.getName());

	public CommandGene(HttpServletResponse response, RequestParameters requestParameters, BgeeProperties prop,
	        ViewFactory viewFactory, ServiceFactory serviceFactory) {
		super(response, requestParameters, prop, viewFactory, serviceFactory);
	}

	@Override
	public void processRequest() throws Exception {
		log.entry();
		GeneDisplay display = viewFactory.getGeneDisplay();
		String geneId = requestParameters.getGeneId();
		if (geneId == null) {
			display.displayGenePage();
		} else {
			display.displayGene(geneId);
		}
		log.exit();
	}

	private Gene getGene(String geneId) {
		//TODO: use GeneService here
		return new Gene(geneId);
	}
	
}
