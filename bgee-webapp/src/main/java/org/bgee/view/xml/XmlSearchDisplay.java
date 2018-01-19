package org.bgee.view.xml;

import java.io.IOException;
import java.util.Collection;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
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
 * @version Bgee 14, Mar. 2017
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

		this.startDisplay();
		
		this.writeln("<genes>");
        
		for (GeneMatch gene: geneMatches) {
        	//find out where the match came from, to display it
        	String label = "";
        	String labelSource = "";
        	if (gene.getGene().getName().toLowerCase().indexOf(searchTerm.toLowerCase()) != -1) {
            	//match name
        		label = gene.getGene().getName();
        		labelSource = "name";
        	} else if (gene.getGene().getEnsemblGeneId().toLowerCase().indexOf(searchTerm.toLowerCase()) != -1) {
            	//match ID
        		label = gene.getGene().getEnsemblGeneId();
        		labelSource = "id";
        	} else {
        		//find the matching synonym 
        		label = gene.getMatchedSynonym();
        		labelSource = "synonym";
        	}
        	
        	this.writeln("<gene id='" + XmlParentDisplay.xmlEntities(gene.getGene().getEnsemblGeneId()) + "' " +
        			"name='" + XmlParentDisplay.xmlEntities(gene.getGene().getName()) + "' " +
        			"label='" + XmlParentDisplay.xmlEntities(label) + "' " +
        			"label_source='" + labelSource + "' " +
        			"species_id='" + String.valueOf(gene.getGene().getSpecies().getId()) + "' " +
        			"species_name='" + XmlParentDisplay.xmlEntities(
        			        StringUtils.isNotBlank(gene.getGene().getSpecies().getName())? 
        			                gene.getGene().getSpecies().getName(): 
        			                gene.getGene().getSpecies().getShortName()) +"' "+ 
        			"/>");
        }
		this.writeln("</genes>");
		
		this.endDisplay();
		
		log.exit();
	}

}
