package org.bgee.view.xml;

import java.io.IOException;
import java.util.Collection;

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.BgeeProperties;
import org.bgee.controller.RequestParameters;
import org.bgee.model.gene.GeneMatch;
import org.bgee.view.SearchDisplay;

/**
 * XML view for the search category display
 * 
 * @author  Valentine Rech de Laval
 * @version Bgee 13, Feb. 2016
 * @since   Bgee 13, Feb. 2016
 */
//TODO javadoc
public class XmlSearchDisplay extends XmlParentDisplay implements SearchDisplay {

	private final static Logger log = LogManager.getLogger(XmlSearchDisplay.class.getName());

	protected XmlSearchDisplay(HttpServletResponse response, RequestParameters requestParameters,
            BgeeProperties prop, XmlFactory factory) throws IllegalArgumentException, IOException {
		super(response, requestParameters, prop, factory);
	}

	@Override
	public void displayGeneCompletionByGeneList(Collection<GeneMatch> geneMatches, String searchTerm) {
		log.entry(geneMatches, searchTerm);
		log.fatal("geneMatches={}, searchTerm={}", geneMatches, searchTerm);
		this.startDisplay(true);
		
		this.writeln("<genes>");
        
		for (GeneMatch gene: geneMatches) {
        	//find out where the match came from, to display it
        	String label = "";
        	String labelSource = "";
        	if (gene.getGene().getName().toLowerCase().indexOf(searchTerm.toLowerCase()) != -1) {
            	//match name
        		label = gene.getGene().getName();
        		labelSource = "name";
        	} else if (gene.getGene().getId().toLowerCase().indexOf(searchTerm.toLowerCase()) != -1) {
            	//match ID
        		label = gene.getGene().getId();
        		labelSource = "id";
        	} else {
        		//find the matching synonym 
        		label = gene.getMatchedSynonym();
        		labelSource = "synonym";
        	}
        	
        	this.writeln("<gene id='" + XmlParentDisplay.xmlEntities(gene.getGene().getId()) + "' " +
        			"name='" + XmlParentDisplay.xmlEntities(gene.getGene().getName()) + "' " +
        			"label='" + XmlParentDisplay.xmlEntities(label) + "' " +
        			"label_source='" + labelSource + "' " +
        			"species_id='" + XmlParentDisplay.xmlEntities(gene.getGene().getSpeciesId()) + "' " +
        			"species_name='" + XmlParentDisplay.xmlEntities(gene.getGene().getSpecies().getName())+"' "+ 
        			"/>");
        }
		this.writeln("</genes>");
		
		this.endDisplay();
		
		log.exit();
	}

}
