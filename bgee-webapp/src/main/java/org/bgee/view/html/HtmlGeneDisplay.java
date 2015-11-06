package org.bgee.view.html;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.bgee.controller.BgeeProperties;
import org.bgee.controller.RequestParameters;
import org.bgee.view.GeneDisplay;
import org.bgee.view.JsonHelper;

/**
 * This class is the HTML implementation of the {@code GeneDisplay}.
 * @author pmoret
 * @version Bgee 13, Oct. 2015
 * @since   Bgee 13, Oct. 2015
 */
public class HtmlGeneDisplay extends HtmlParentDisplay implements GeneDisplay {
	
	/**
     * Constructor
     * 
     * @param response          A {@code HttpServletResponse} that will be used to display the 
     *                          page to the client
     * @param requestParameters The {@code RequestParameters} that handles the parameters of the 
     *                          current request.
     * @param prop              A {@code BgeeProperties} instance that contains the properties
     *                          to use.
     * @param factory           The {@code HtmlFactory} that instantiated this object.
     * @throws IOException      If there is an issue when trying to get or to use the
     *                          {@code PrintWriter} 
     */
	public HtmlGeneDisplay(HttpServletResponse response, RequestParameters requestParameters, BgeeProperties prop,
	        JsonHelper jsonHelper, HtmlFactory factory) throws IllegalArgumentException, IOException {
		super(response, requestParameters, prop, jsonHelper, factory);
	}

	@Override
	public void displayGenePage() {
		this.startDisplay("Gene Page");

		this.endDisplay();
	}

	@Override
	public void displayGene(String geneId) {
		this.startDisplay("Gene: "+geneId);
		this.writeln("<h1>Gene: "+geneId+"<h1>");
		this.endDisplay();
	}

}
