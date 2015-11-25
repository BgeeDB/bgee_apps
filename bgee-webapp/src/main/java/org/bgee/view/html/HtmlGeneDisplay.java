package org.bgee.view.html;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import org.bgee.controller.BgeeProperties;
import org.bgee.controller.RequestParameters;
import org.bgee.model.expressiondata.Call.ExpressionCall;
import org.bgee.model.gene.Gene;
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
		this.startDisplay("Gene Information");
		
		this.endDisplay();
	}

	private static String getExpressionHTML(ExpressionCall call) {
		StringBuilder sb = new StringBuilder();
		sb.append("<td>").append(call.getCondition().getAnatEntityId()).append("</td>");
		sb.append("<td>").append(call.getCondition().getDevStageId()).append("</td>");
		sb.append("<td>").append(call.getSummaryQuality().getStringRepresentation()).append("</td>");
		return sb.toString();
	}

	private  String getGeneInfo(Gene gene) {
		final StringBuilder table = new StringBuilder("<table style='margin: 1em 1em 2em 5em'>");
		table.append("<tr><td>").append("<strong>Ensembl Id</strong></td><td>").append(gene.getId()).append("</td></tr>");
		table.append("<tr><td>").append("<strong>Name</strong></td><td>").append(gene.getName()).append("</td></tr>");
		table.append("<tr><td>").append("<strong>Description</strong></td><td>").append(gene.getDescription()).append("</td></tr>");
		table.append("<tr><td>").append("<strong>Organism</strong></td><td><em>").append(gene.getSpecies().getScientificName()).append("</em> (")
			 .append(gene.getSpecies().getName()).append(")</td></tr>");

		return table.append("</table>").toString();

	}
	
	@Override
	public void displayGene(Gene gene, List<ExpressionCall> calls) {
		this.startDisplay("Gene: "+gene.getId());
		this.writeln("<h1>Gene: "+gene.getId()+"</h1>");
		this.write("<h2>Gene Information</h2>");
		this.writeln("<div>"+getGeneInfo(gene)+"</div>");
		this.write("<h2>Expression</h2>");
		final StringBuilder table = new StringBuilder("<center><table style='width:80%;margin: 1em 1em 2em 5em;'>");
		table.append("<tr><td><strong>AnatEntity</strong></td><td><strong>Stage</strong></td><td><strong>Quality</strong></td></tr>");
		table.append(calls.stream().map(HtmlGeneDisplay::getExpressionHTML).collect(Collectors.joining("</tr><tr>", "<tr>", "</tr>")));
		table.append("</table></center>");
		this.writeln(table.toString());
		
		this.endDisplay();
	}

}
