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

	private static String getExpressionHTMLByAnat(Map<String, List<ExpressionCall>> byAnatEntityId,  Map<String, AnatEntity> anatEntitiesMap, 
			Map<String, DevStage> devStageMap) {
		
		StringBuilder sb = new StringBuilder();
		sb.append("<table class='expression'>");
		sb.append("<tr><td><strong>AnatEntity</strong></td><td><strong>Stage</strong></td><td><strong>Quality</strong></td></tr>\n");
		String elements = byAnatEntityId.entrySet().stream().map(e -> {
			final AnatEntity a =  anatEntitiesMap.get(e.getKey());
			List<DevStage> stages =  e.getValue().stream()
					.map(c -> devStageMap.get(c.getCondition().getDevStageId()))
					.collect(Collectors.toList());
			return getExpressionHTMLForAnatEntity(a, stages, e.getValue());
		}).collect(Collectors.joining("</tr>\n<tr>","<tr>","</tr>\n")); 
		
		sb.append(elements);
		sb.append("</table>");

		return sb.toString();
		
	}

	private static String getExpressionHTMLByDev(Map<String, List<ExpressionCall>> byDevStageId,
	        Map<String, AnatEntity> anatEntitiesMap, Map<String, DevStage> devStageMap) {

		StringBuilder sb = new StringBuilder();
		sb.append("<table class='expression'>");
		sb.append(
		        "<tr><td><strong>AnatEntity</strong></td><td><strong>Stage</strong></td><td><strong>Quality</strong></td></tr>\n");
		String elements = byDevStageId.entrySet().stream().map(e -> {
			final DevStage ds = devStageMap.get(e.getKey());
			return e.getValue().stream()
		            .map(ec -> getExpressionHTML(ec, anatEntitiesMap.get(ec.getCondition().getAnatEntityId()), ds))
		            .collect(Collectors.joining("</tr>\n<tr>", "<tr>", "</tr>"));
		}).collect(Collectors.joining("\n"));

		sb.append(elements);
		sb.append("</table>");

		return sb.toString();
	}

	private static String getExpressionHTMLForAnatEntity(AnatEntity a, List<DevStage> stages,
	        List<ExpressionCall> calls) {
		StringBuilder sb = new StringBuilder();
		sb.append("<td>").append("[").append(a.getId()).append("] ").append(a.getName()).append("</td>");
		sb.append("<td><span class='expandable' title='click to expand'>[+] ").append(stages.size()).append(" development stage(s)</span>")
		.append("<div class='invisible details' id='a_").append(a.getId().replace(":", "_")).append("'>").append(stages.stream().map(DevStage::getName).collect(Collectors.joining("<br/>"))).append("</div>")
		.append("</td>");

		String qual = calls.iterator().next().getCallData().stream().map(data -> getQualitySpan(data)).collect(Collectors.joining());

		sb.append("<td>").append(getQualitySpans(calls.stream().flatMap(e -> e.getCallData().stream()).collect(Collectors.toList())))
		        /*qual*//* call.getSummaryQuality().getStringRepresentation() */.append("</td>");
		return sb.toString();
	}

	private static String getExpressionHTML(ExpressionCall call, AnatEntity a, DevStage d) {
		StringBuilder sb = new StringBuilder();
		sb.append("<td>").append("[").append(a.getId()).append("] ").append(a.getName()).append("</td>");
		sb.append("<td>").append("[").append(d.getId()).append("] ").append(d.getName()).append("</td>");
		String qual = getQualitySpans(call.getCallData()); 
				// call.getCallData().stream().map(data -> getQualitySpan(data)).collect(Collectors.joining());
		// data.getDataType()+":"+data.getDataQuality().toString())
		// .collect(Collectors.joining(" | ", "[ ", " ]")
		sb.append("<td>").append(
		        qual/* call.getSummaryQuality().getStringRepresentation() */).append("</td>");
		return sb.toString();
	}

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

	private String getExpressionTable(List<ExpressionCall> calls, Map<String, AnatEntity> anatEntitiesMap,
	        Map<String, DevStage> devStageMap) {
		final StringBuilder table = new StringBuilder("<table class='expression'>");
		table.append(
		        "<tr><td><strong>AnatEntity</strong></td><td><strong>Stage</strong></td><td><strong>Quality</strong></td></tr>");
		table.append(calls.stream()/* .limit(20) */
		        .map(c -> getExpressionHTML(c, anatEntitiesMap.get(c.getCondition().getAnatEntityId()),
		                devStageMap.get(c.getCondition().getDevStageId())))
		        .collect(Collectors.joining("</tr><tr>", "<tr>", "</tr>")));
		table.append("</table>");
		return table.toString();
	}

	private static String getDisplayQuality(ExpressionCallData data) {
		return data.getDataType().getStringRepresentation() + " -> " + data.getDataQuality().toString();
	}

	
	private static String getQualitySpans(Collection<ExpressionCallData> callData) {
		final Map<DataType,Set<DataQuality>> qualities = callData.stream()
		.collect(Collectors.groupingBy(ExpressionCallData::getDataType, 
				Collectors.mapping(ExpressionCallData::getDataQuality, Collectors.toSet())));
		return EnumSet.allOf(DataType.class).stream().map(type -> {
			Set<DataQuality> quals = qualities.get(type);
			DataQuality quality = DataQuality.NODATA;
			if ( quals != null) {
				if (quals.contains(DataQuality.HIGH)) {
					quality = DataQuality.HIGH;
				} else if (quals.contains(DataQuality.LOW)) 
					quality = DataQuality.LOW;
			}		
			return getSpan(quality, type);
		}).collect(Collectors.joining());
		
	}
	
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

		sb.append("' title='")
		  .append(type.getStringRepresentation())
		  .append(" : ")
		  .append(quality.getStringRepresentation())
		  .append("'>");

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
	
	private static String getQualitySpan(ExpressionCallData cd) {
		return getSpan(cd.getDataQuality(), cd.getDataType());
	}

	private Map<String, List<ExpressionCall>> byAnatEntity(List<ExpressionCall> calls) {
		return calls.stream().collect(Collectors.groupingBy(ec -> ec.getCondition().getAnatEntityId(),
		        LinkedHashMap::new, Collectors.toList()));
	}

	private Map<String, List<ExpressionCall>> byDevStage(List<ExpressionCall> calls) {
		return calls.stream().collect(Collectors.groupingBy(ec -> ec.getCondition().getDevStageId(), LinkedHashMap::new,
		        Collectors.toList()));
	}

	@Override
	public void displayGene(Gene gene, List<ExpressionCall> calls, Map<String, AnatEntity> anatEntitiesMap,
	        Map<String, DevStage> devStageMap) {
		this.startDisplay("Gene: " + gene.getName());
		this.writeln("<h1>Gene: " + gene.getName() + "</h1>");
		this.writeln("<h2>Gene Information</h2>");
		this.writeln("<div>" + getGeneInfo(gene) + "</div>");
		this.writeln("<h2>Expression</h2>");
		/*this.writeln(getExpressionTable(calls, anatEntitiesMap, devStageMap));

		this.writeln("<h2>Expression by Anat</h2>");*/
		this.writeln(getExpressionHTMLByAnat(byAnatEntity(calls), anatEntitiesMap, devStageMap));
	
		/*this.writeln("<h2>Expression by Stages</h2>");
		this.write(getExpressionHTMLByDev(byDevStage(calls), anatEntitiesMap, devStageMap));*/

		this.endDisplay();
	}

}
