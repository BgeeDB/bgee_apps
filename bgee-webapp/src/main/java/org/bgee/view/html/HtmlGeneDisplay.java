package org.bgee.view.html;

import java.io.IOException;
import java.util.Collection;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import org.bgee.controller.BgeeProperties;
import org.bgee.controller.RequestParameters;
import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.anatdev.DevStage;
import org.bgee.model.expressiondata.Call.ExpressionCall;
import org.bgee.model.expressiondata.CallData.ExpressionCallData;
import org.bgee.model.expressiondata.baseelements.DataQuality;
import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.model.gene.Gene;
import org.bgee.view.GeneDisplay;
import org.bgee.view.JsonHelper;

/**
 * This class is the HTML implementation of the {@code GeneDisplay}.
 * 
 * @author pmoret
 * @version Bgee 13, Oct. 2015
 * @since Bgee 13, Oct. 2015
 */
public class HtmlGeneDisplay extends HtmlParentDisplay implements GeneDisplay {

	private static final int ELEMENT_LIMIT = 12;

	/**
	 * Constructor
	 * 
	 * @param response
	 *            A {@code HttpServletResponse} that will be used to display the
	 *            page to the client
	 * @param requestParameters
	 *            The {@code RequestParameters} that handles the parameters of
	 *            the current request.
	 * @param prop
	 *            A {@code BgeeProperties} instance that contains the properties
	 *            to use.
	 * @param factory
	 *            The {@code HtmlFactory} that instantiated this object.
	 * @throws IOException
	 *             If there is an issue when trying to get or to use the
	 *             {@code PrintWriter}
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

	@Override
	public void displayGene(Gene gene, List<ExpressionCall> calls, Map<String, AnatEntity> anatEntitiesMap,
	        Map<String, DevStage> devStageMap) {
		this.startDisplay("Gene: " + gene.getName());
		this.writeln("<h1>Gene: " + gene.getName() + "</h1>");
		this.writeln("<h2>Gene Information</h2>");
		this.writeln("<div>" + getGeneInfo(gene) + "</div>");
		this.writeln("<h2>Expression</h2>");
		this.writeln(getExpressionHTMLByAnat(byAnatEntity(calls), anatEntitiesMap, devStageMap));
		this.endDisplay();
	}

	/**
	 * Build a table from a collection of {@code ExpressionCall} group by
	 * anatomic entity id.
	 * 
	 * @param byAnatEntityId
	 *            A {@code Map} associating anatomic entities ids to a sorted
	 *            list of expression calls.
	 * @param anatEntitiesMap
	 *            A {@code Map} associating anatomic entities ids to their
	 *            {@code AnatEntity} instance.
	 * @param devStageMap
	 *            A {@code Map} associating development stage ids to their
	 *            {@code DevStage} instance.
	 * @return A {@code String} containing the HTML code of the table
	 */
	private static String getExpressionHTMLByAnat(Map<String, List<ExpressionCall>> byAnatEntityId,
	        Map<String, AnatEntity> anatEntitiesMap, Map<String, DevStage> devStageMap) {

		StringBuilder sb = new StringBuilder();
		sb.append("<table class='expression'>")
		        .append("<tr><td class='col15'></td><td class='col25'><strong>AnatEntity</strong></td>")
		        .append("<td class='col50'><strong>Stage</strong></td><td class='col10'>")
		        .append("<strong>Quality</strong></td></tr>\n");
		int size = byAnatEntityId.size();

		String elements = byAnatEntityId.entrySet().stream().limit(ELEMENT_LIMIT).map(e -> {
			final AnatEntity a = anatEntitiesMap.get(e.getKey());
			return getExpressionRowsForAnatEntity(a, devStageMap, e.getValue(), false);
		}).collect(Collectors.joining("\n"));
		sb.append(elements);

		if (size > ELEMENT_LIMIT) {
			String extraElements = byAnatEntityId.entrySet().stream().skip(ELEMENT_LIMIT).map(e -> {
				final AnatEntity a = anatEntitiesMap.get(e.getKey());
				return getExpressionRowsForAnatEntity(a, devStageMap, e.getValue(), true);
			}).collect(Collectors.joining("\n"));
			sb.append(extraElements);
		}

		sb.append("</table>");

		if (size > ELEMENT_LIMIT) {
			sb.append("<span class='show_extra expression'>show more</span>");
		}
		sb.append("<div class='gene details'><table class='expression'>")
		        .append("<tr><td><strong>Sources</strong></td><td><strong>Quality</strong></td></tr>")
		        .append("<tr><td><strong>A</strong>: Affymetrix</td><td><span class='quality high'>high quality</span></td><td></td></tr>")
		        .append("<tr><td><strong>E</strong>: EST (Expressed Sequence Tag)</td><td><span class='quality low'>low quality</span></td></tr>")
		        .append("<tr><td><strong>I</strong>: In Situ</td><td><span class='quality nodata'>no data</span></td></tr>")
		        .append("<tr><td><strong>R</strong>: RNA-Seq</td></tr></table></div>");
		return sb.toString();

	}

	/**
	 * Gets the rows ({@code tr}) element for the given anatomic entity
	 * @param anatEntity  The {@code AnatEntity}
	 * @param stages      The {@code Map} associating development stage ids to their
	 * @param calls       A {@code List} of {@code ExpressionCall} associated to the {@code AnatEntity}
	 * @param isExtra     True if the row is "extra", i.e., to be invisible on page load.
	 * @return            The HTML code containing the rows.
	 */
	private static String getExpressionRowsForAnatEntity(AnatEntity anatEntity, Map<String, DevStage> stages,
	        List<ExpressionCall> calls, boolean isExtra) {
		StringBuilder sb = new StringBuilder();
		if (!isExtra) {
			sb.append("<tr class='aggregate'>");
		} else {
			sb.append("<tr class='aggregate extra invisible'>");
		}
		sb.append("<td class='details right small'>").append(anatEntity.getId()).append("</td><td>").append(anatEntity.getName())
		        .append("</td>");
		sb.append("<td><span class='expandable' title='click to expand'>[+] ").append(calls.size())
		        .append(" development stage(s)</span>").append("</td>");
		sb.append("<td>")
		        .append(getQualitySpans(
		                calls.stream().flatMap(e -> e.getCallData().stream()).collect(Collectors.toList())))
		        .append("</td></tr>");
		sb.append(calls.stream().map(call -> {
			DevStage stage = stages.get(call.getCondition().getDevStageId());
			StringBuilder sb2 = new StringBuilder();
			sb2.append("<tr class='invisible'><td></td><td></td><td class='small'><span class='details'>")
		            .append(stage.getId()).append(" </span> ");
			sb2.append(stage.getName()).append("</td><td>").append(getQualitySpans(call.getCallData()));
			return sb2.toString();
		}).collect(Collectors.joining("\n")));

		return sb.toString();
	}

	/**
	 * Create a table containing information for {@code Gene}
	 * @param gene The {@code Gene} for which to display information
	 * @return A {@code String} containing the HTML table containing the information.
	 */
	private String getGeneInfo(Gene gene) {
		final StringBuilder table = new StringBuilder("<table class='expression'>");
		table.append("<tr><td>").append("<strong>Ensembl Id</strong></td><td>").append(gene.getId())
		        .append("</td></tr>");
		table.append("<tr><td>").append("<strong>Name</strong></td><td>").append(gene.getName()).append("</td></tr>");
		table.append("<tr><td>").append("<strong>Description</strong></td><td>").append(gene.getDescription())
		        .append("</td></tr>");
		table.append("<tr><td>").append("<strong>Organism</strong></td><td><em>")
		        .append(gene.getSpecies().getScientificName()).append("</em> (").append(gene.getSpecies().getName())
		        .append(")</td></tr>");

		return table.append("</table>").toString();
	}

	/**
	 * Builds the quality "span" elements for the given expression calls
	 * @param callData A {@code Collection} of {@code ExpressionCallData} as input
	 * @return A {@String} containing the HTML code of the span
	 */
	private static String getQualitySpans(Collection<ExpressionCallData> callData) {
		final Map<DataType, Set<DataQuality>> qualities = callData.stream()
		        .collect(Collectors.groupingBy(ExpressionCallData::getDataType,
		                Collectors.mapping(ExpressionCallData::getDataQuality, Collectors.toSet())));
		return EnumSet.allOf(DataType.class).stream().map(type -> {
			Set<DataQuality> quals = qualities.get(type);
			DataQuality quality = DataQuality.NODATA;
			if (quals != null) {
				if (quals.contains(DataQuality.HIGH)) {
					quality = DataQuality.HIGH;
				} else if (quals.contains(DataQuality.LOW))
					quality = DataQuality.LOW;
			}
			return getSpan(quality, type);
		}).collect(Collectors.joining());

	}

	/**
	 * Builds a 'span' element representing the quality for a given {@code DataType}
	 * @param quality The {@code DataQuality}
	 * @param type    The {@code DataType}
	 * @return A {@code String} containing the HTML code for the quality 'span'.
	 */
	private static String getSpan(DataQuality quality, DataType type) {
		StringBuilder sb = new StringBuilder();
		sb.append("<span class='quality ");

		switch (quality) {
			case HIGH:
				sb.append("high");
				break;
			case LOW:
				sb.append("low");
				break;
			case NODATA:
				sb.append("nodata");
		}

		sb.append("' title='").append(type.getStringRepresentation()).append(" : ")
		        .append(quality.getStringRepresentation()).append("'>");

		switch (type) {
			case AFFYMETRIX:
				sb.append("A");
				break;
			case RNA_SEQ:
				sb.append("R");
				break;
			case IN_SITU:
				sb.append("I");
				break;
			case EST:
				sb.append("E");
				break;
		}
		sb.append("</span>");
		return sb.toString();
	}

	/**
	 * Build a {@code Map} associating anatomic entities ID to the {@code List} of 
	 * associated {@code ExpressionCall}, the order of the input list is preserved.
	 * @param calls A {@code List} of {@code ExpressionCall} as input
	 * @return      The @{code {@link LinkedHashMap} containing the association.
	 */
	private Map<String, List<ExpressionCall>> byAnatEntity(List<ExpressionCall> calls) {
		return calls.stream().collect(Collectors.groupingBy(ec -> ec.getCondition().getAnatEntityId(),
		        LinkedHashMap::new, Collectors.toList()));
	}

	@Override
	protected void includeCss() {
		super.includeCss();
		this.includeCss("gene.css");
	}

	@Override
	protected void includeJs() {
		super.includeJs();
		this.includeJs("gene.js");
	}
}
