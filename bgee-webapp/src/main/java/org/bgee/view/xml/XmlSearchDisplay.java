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
 * @version Bgee 14, Mar. 2019
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
	public void displayGeneCompletionByGeneList(Collection<GeneMatch> geneMatches) {
		log.entry(geneMatches);

		this.startDisplay();
		
		this.writeln("<genes>");
        
		if (geneMatches != null) {
			for (GeneMatch gene : geneMatches) {
				//find out where the match came from, to display it
				String label = "";
				String labelSource = "";
				switch (gene.getMatchSource()) {
					case NAME:
						label = gene.getGene().getName();
						labelSource = "name";
						break;
					case ID:
						label = gene.getGene().getEnsemblGeneId();
						labelSource = "id";
						break;
					case DESCRIPTION:
						label = gene.getGene().getDescription();
						labelSource = "description";
						break;
					case SYNONYM:
						label = gene.getTerm();
						labelSource = "synonym";
						break;
					case XREF:
						label = gene.getTerm();
						labelSource = "x-ref";
						break;
					default:
						throw log.throwing(new IllegalStateException("Unrecognized MatchSource: " + gene.getMatchSource()));
				}

				this.writeln("<gene id='" + XmlParentDisplay.xmlEntities(gene.getGene().getEnsemblGeneId()) + "' " +
						"name='" + XmlParentDisplay.xmlEntities(gene.getGene().getName()) + "' " +
						"label='" + XmlParentDisplay.xmlEntities(label) + "' " +
						"label_source='" + labelSource + "' " +
						"species_id='" + String.valueOf(gene.getGene().getSpecies().getId()) + "' " +
						"species_name='" + XmlParentDisplay.xmlEntities(
						StringUtils.isNotBlank(gene.getGene().getSpecies().getName()) ?
								gene.getGene().getSpecies().getName() :
								gene.getGene().getSpecies().getShortName()) + "' " +
						"/>");
			}
		}
		this.writeln("</genes>");
		
		this.endDisplay();
		
		log.exit();
	}

	public void displayMatchesForGeneCompletion(Collection<String> matches) {
		log.entry(matches);

		this.startDisplay();

		this.writeln("<matches>");

		if (matches != null) {
			for (String match : matches) {
				this.writeln("<match hit='" + match + "' />");
			}
		}
		this.writeln("</matches>");

		this.endDisplay();

		log.exit();
	}

	@Override
	public void displayExpasyResult(int count, String searchTerm) {
		log.entry(count, searchTerm);

		this.startDisplay();

		this.writeln("<ExpasyResult>");

		this.writeln("<count>");
		this.writeln(String.valueOf(count));
		this.writeln("</count>");
		
		this.writeln("<url>");
		// TODO do we need to set getNewRequestParameters() in XmlParentDisplay as HtmlParentDisplay?
		RequestParameters url = new RequestParameters(
				this.getRequestParameters().getUrlParametersInstance(), this.prop, true, "&amp;");
		url.setPage(RequestParameters.PAGE_GENE);
		url.setSearch(searchTerm);
		this.writeln(url.getRequestURL());
		this.writeln("</url>");

		this.writeln("<description>");
		this.writeln("Genes found with expression in Bgee");
		this.writeln("</description>");

		this.writeln("</ExpasyResult>");

		this.endDisplay();

		log.exit();
	}
}
