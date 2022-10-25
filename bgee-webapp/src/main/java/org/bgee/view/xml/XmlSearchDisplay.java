package org.bgee.view.xml;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.BgeeProperties;
import org.bgee.controller.RequestParameters;
import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.anatdev.DevStage;
import org.bgee.model.search.SearchMatchResult;
import org.bgee.view.SearchDisplay;

/**
 * XML view for the search category display
 * 
 * @author  Valentine Rech de Laval
 * @version Bgee 15, Oct. 2021
 * @since   Bgee 13, Feb. 2016
 */
//TODO javadoc
public class XmlSearchDisplay extends XmlParentDisplay implements SearchDisplay {

	private final static Logger log = LogManager.getLogger(XmlSearchDisplay.class.getName());

	protected XmlSearchDisplay(HttpServletResponse response, RequestParameters requestParameters,
            BgeeProperties prop, XmlFactory factory) throws IllegalArgumentException, IOException {
		super(response, requestParameters, prop, factory);
	}

	public void displayMatchesForGeneCompletion(Collection<String> matches) {
		log.traceEntry("{}", matches);

		this.startDisplay();

		this.writeln("<matches>");

		if (matches != null) {
			for (String match : matches) {
				this.writeln("<match hit='" + xmlEntities(match) + "' />");
			}
		}
		this.writeln("</matches>");

		this.endDisplay();

		log.traceExit();
	}

	@Override
	public void displayExpasyResult(int count, String searchTerm) {
		log.traceEntry("{}, {}", count, searchTerm);

		this.startDisplay();

		this.writeln("<ExpasyResult>");

		this.writeln("<count>" + String.valueOf(count) + "</count>");

		// TODO find a way not to hardcode request parameters
		// Does it worth creating a FrontendRequestParameters?
		String url = this.prop.getFrontendUrl() + RequestParameters.PAGE_SEARCH
		        + "/genes?" + RequestParameters.PAGE_SEARCH + "=";
		url = url + searchTerm;
		this.writeln("<url>" + url + "</url>");

		this.writeln("<description>Genes found in gene expression database Bgee</description>");

		this.writeln("</ExpasyResult>");

		this.endDisplay();

		log.traceExit();
	}

    @Override
    public void displayAnatEntitySearchResult(String searchTerm,
            SearchMatchResult<AnatEntity> result) {
        throw log.throwing(new UnsupportedOperationException("Not available for XML display"));
    }

    @Override
    public void displayDevStageSearchResult(Set<DevStage> result) {
        throw log.throwing(new UnsupportedOperationException("Not available for XML display"));
    }

}
