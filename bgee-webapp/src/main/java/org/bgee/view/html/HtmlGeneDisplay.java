package org.bgee.view.html;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.BgeeProperties;
import org.bgee.controller.RequestParameters;
import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.anatdev.DevStage;
import org.bgee.model.expressiondata.Call.ExpressionCall;
import org.bgee.model.expressiondata.CallData.ExpressionCallData;
import org.bgee.model.expressiondata.Condition;
import org.bgee.model.expressiondata.ConditionUtils;
import org.bgee.model.expressiondata.baseelements.DataQuality;
import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.model.gene.Gene;
import org.bgee.view.GeneDisplay;
import org.bgee.view.JsonHelper;

/**
 * This class is the HTML implementation of the {@code GeneDisplay}.
 * 
 * @author  Philippe Moret
 * @author  Valentine Rech de Laval
 * @author  Frederic Bastian
 * @version Bgee 13, Jan. 2016
 * @since   Bgee 13, Oct. 2015
 */
public class HtmlGeneDisplay extends HtmlParentDisplay implements GeneDisplay {
    private final static Logger log = LogManager.getLogger(HtmlGeneDisplay.class.getName());

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
	    log.entry();
		this.startDisplay("Gene Information");
		this.endDisplay();
		log.exit();
	}

	@Override
	public void displayGene(Gene gene, List<ExpressionCall> calls, ConditionUtils conditionUtils) {
	    log.entry(gene, calls, conditionUtils);
	    
	    String titleStart = "Gene: " + gene.getName() + " - " + gene.getId(); 
		this.startDisplay(titleStart);
		this.writeln("<h1 class='gene_title'><span id='species_img'><img height='75' width='75' src='" 
		        + this.prop.getSpeciesImagesRootDirectory() + htmlEntities(gene.getSpeciesId()) + "_light.jpg' alt='" 
		        + htmlEntities(gene.getSpecies().getShortName()) + "' /></span><span class='gene_title'>" 
		        + htmlEntities(titleStart) + " - <em>" + htmlEntities(gene.getSpecies().getScientificName()) + "</em> ("
                + htmlEntities(gene.getSpecies().getName()) + ")</span></h1>");
		this.writeln("<h2>Gene Information</h2>");
		this.writeln("<div class='gene'>" + getGeneInfo(gene) + "</div>");
		this.writeln("<h2>Expression</h2>");
		this.writeln("<div id='expr_intro'>Expression calls ordered by biological relevance: </div>");
		this.writeln("<div id='table-container'>");
		this.writeln(getExpressionHTMLByAnat(byAnatEntity(filterCalls(calls, conditionUtils)), conditionUtils));
		this.writeln("</div>");
		this.endDisplay();
		
		log.exit();
	}

	/**
	 * Generates the HTML code displaying information about expression calls.
	 * 
	 * @param byAnatEntityId   A {@code Map} where keys are {@code String}s representing 
	 *                         anatomical entity IDs, the associated value being a {@code List} 
	 *                         of {@code ExpressionCall}s for this anatomical entity, 
	 *                         ordered by biological relevance. 
	 * @param conditionUtils   A {@code ConditionUtils} containing information about all {@code Condition}s 
     *                         retrieved from the {@code ExpressionCall}s in {@code byAnatEntityId}.
	 * @return                 A {@code String} that is the generated HTML.
	 */
	private static String getExpressionHTMLByAnat(Map<String, List<ExpressionCall>> byAnatEntityId,
	        final ConditionUtils conditionUtils) {
	    log.entry(byAnatEntityId, conditionUtils);

		StringBuilder sb = new StringBuilder();

		String elements = byAnatEntityId.entrySet().stream().map(e -> {
			final AnatEntity a = conditionUtils.getAnatEntity(e.getKey());
			final List<ExpressionCall> calls = e.getValue();

			return getExpressionRowsForAnatEntity(a, conditionUtils, calls);
		}).collect(Collectors.joining("\n"));

		sb.append("<table class='expression stripe'>")
		        .append("<thead><tr><td class='col15'></td><td class='col25'><strong>AnatEntity</strong></td>")
		        .append("<td class='col50'><strong>Stage</strong></td><td class='col10'>")
		        .append("<strong>Quality</strong></td></tr></thead>\n");
		sb.append("<tbody>").append(elements).append("</tbody>");
		sb.append("</table>");
		sb.append("<div class='gene details'><table class='legend'>")
		        .append("<tr><td></td><td><strong>Sources</strong></td><td><strong>Quality</strong></td></tr>")
		        .append("<tr><td><strong>A</strong></td><td>Affymetrix</td><td><span class='quality high'>high quality</span></td><td></td></tr>")
		        .append("<tr><td><strong>E</strong></td><td>EST (Expressed Sequence Tag)</td><td><span class='quality low'>low quality</span></td></tr>")
		        .append("<tr><td><strong>I</strong></td><td>In Situ</td><td><span class='quality nodata'>no data</span></td></tr>")
		        .append("<tr><td><strong>R</strong></td><td>RNA-Seq</td></tr></table></div>");
		return log.exit(sb.toString());

	}

	/**
	 * Generates the HTML code to display information about expression calls occurring  
	 * in one specific anatomical entity. 
	 * 
	 * @param anatEntity       The {@code AnatEntity} for which the expression calls will be displayed.
     * @param conditionUtils   A {@code ConditionUtils} containing information about all {@code Condition}s 
     *                         retrieved from the {@code calls}.
	 * @param calls            A {@code List} of {@code ExpressionCall}s related to {@code anatEntity}, 
     *                         ordered by biological relevance. 
	 * @return                 A {@code String} that is the generated HTML.
	 */
	private static String getExpressionRowsForAnatEntity(AnatEntity anatEntity, ConditionUtils conditionUtils,
	        List<ExpressionCall> calls) {
	    log.entry(anatEntity, conditionUtils, calls);
	    
		StringBuilder sb = new StringBuilder();
		sb.append("<tr>");
		
		// Anat entity ID and Anat entity cells 
		String anatEntityUrl = "http://purl.obolibrary.org/obo/";
        try {
            anatEntityUrl += java.net.URLEncoder.encode(anatEntity.getId().replace(':', '_'), "UTF-8");
        } catch (UnsupportedEncodingException e1) {
            //Nothing here, UTF-8 has to be supported...
            throw log.throwing(new AssertionError("Unreachable code"));
        }
		sb.append("<td class='details right small'><a target='_blank' href='").append(anatEntityUrl)
		    .append("' title='External link to ontology visualization'>")
		    .append(htmlEntities(anatEntity.getId()))
		    .append("</a></td><td>")
			.append(htmlEntities(anatEntity.getName())).append("</td>");
		
		// Dev stage cell
		sb.append("<td><span class='expandable' title='click to expand'>[+] ").append(calls.size())
			.append(" development stage(s)</span>")
			.append("<ul class='invisible dev-stage-list'>")
			.append(calls.stream().map(call -> {
				DevStage stage = conditionUtils.getDevStage(call.getCondition().getDevStageId());
				StringBuilder sb2 = new StringBuilder();
				sb2.append("<li class='dev-stage'><span class='details small'>")
				    .append(htmlEntities(stage.getId())).append("</span>")
					.append(htmlEntities(stage.getName())).append("</li>");
				return sb2.toString();
			}).collect(Collectors.joining("\n")))      
			.append("</ul></td>");
		
		// Quality cell
		sb.append("<td>")
		        .append(getQualitySpans(
		                calls.stream().flatMap(e -> e.getCallData().stream()).collect(Collectors.toList())))
				.append("<ul class='invisible quality-list'>")
				.append(calls.stream().map(call -> {
						StringBuilder sb2 = new StringBuilder();
						sb2.append("<li class='qualities'>").append(getQualitySpans(call.getCallData())).append("</li>");
						return sb2.toString();
					}).collect(Collectors.joining("\n")))
				.append("</ul></td>")
		        .append("</td>");
		
		sb.append("</tr>");

		return log.exit(sb.toString());
	}

	/**
	 * Create a table containing general information for {@code Gene}
	 * 
	 * @param gene     The {@code Gene} for which to display information
	 * @return         A {@code String} containing the HTML table containing the information.
	 */
	private static String getGeneInfo(Gene gene) {
	    log.entry(gene);
	    
		final StringBuilder table = new StringBuilder("<table id='geneinfo'>");
		table.append("<tr><th>").append("Ensembl ID</th><td>").append(htmlEntities(gene.getId()))
		        .append("</td></tr>");
		table.append("<tr><th>").append("Name</th><td>").append(htmlEntities(gene.getName())).append("</td></tr>");
		table.append("<tr><th>").append("Description</th><td>").append(htmlEntities(gene.getDescription()))
		        .append("</td></tr>");
		table.append("<tr><th>").append("Organism</th><td><em>")
		        .append(htmlEntities(gene.getSpecies().getScientificName())).append("</em> (")
		        .append(htmlEntities(gene.getSpecies().getName()))
		        .append(")</td></tr>");

		return log.exit(table.append("</table>").toString());
	}

	/**
	 * Builds the quality "span" elements for the given expression calls
	 * 
	 * @param callData     A {@code Collection} of {@code ExpressionCallData} as input
	 * @return             A {@String} containing the HTML code of the span
	 */
	private static String getQualitySpans(Collection<ExpressionCallData> callData) {
	    log.entry(callData);
	    
		final Map<DataType, Set<DataQuality>> qualities = callData.stream()
		        .collect(Collectors.groupingBy(ExpressionCallData::getDataType,
		                Collectors.mapping(ExpressionCallData::getDataQuality, Collectors.toSet())));
		return log.exit(EnumSet.allOf(DataType.class).stream().map(type -> {
			Set<DataQuality> quals = qualities.get(type);
			DataQuality quality = DataQuality.NODATA;
			if (quals != null) {
				if (quals.contains(DataQuality.HIGH)) {
					quality = DataQuality.HIGH;
				} else if (quals.contains(DataQuality.LOW))
					quality = DataQuality.LOW;
			}
			return getSpan(quality, type);
		}).collect(Collectors.joining()));

	}

	/**
	 * Builds a 'span' element representing the quality for a given {@code DataType}
	 * 
	 * @param quality  The {@code DataQuality}
	 * @param type     The {@code DataType}
	 * @return         A {@code String} containing the HTML code for the quality 'span'.
	 */
	private static String getSpan(DataQuality quality, DataType type) {
	    log.entry(quality, type);
	    
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
			break;
	    default: 
	        throw log.throwing(new IllegalStateException("Unsupported quality: " + quality));
		}

		sb.append("' title='").append(htmlEntities(type.getStringRepresentation())).append(": ")
		        .append(htmlEntities(quality.getStringRepresentation())).append("'>");

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
		return log.exit(sb.toString());
	}

	/**
	 * Build a {@code Map} associating anatomic entities ID to the {@code List}
	 * of associated {@code ExpressionCall}, the order of the input list is
	 * preserved.
	 * 
	 * @param calls    A {@code List} of {@code ExpressionCall} as input
	 * @return         The @{code {@link LinkedHashMap} containing the association.
	 */
	private static Map<String, List<ExpressionCall>> byAnatEntity(List<ExpressionCall> calls) {
	    log.entry(calls);
		return log.exit(calls.stream().collect(Collectors.groupingBy(ec -> ec.getCondition().getAnatEntityId(),
		        LinkedHashMap::new, Collectors.toList())));
	}
	
	/**
	 * Filter {@code calls} for redundant calls with higher ranks. This method creates
	 * a new {@code List} of {@code ExpressionCall}s, based on {@code calls}, 
	 * by discarding all {@code ExpressionCall}s for which there exists a more precise call 
	 * (i.e., with a more precise condition) at a better rank (i.e., with a lower index in the list). 
	 * 
	 * @param calls            The original {@code List} of {@code ExpressionCall}s to be filtered.
	 * @param conditionUtils   A {@code ConditionUtils} containing all the {@code Condition}s 
	 *                         related to {@code calls}.
	 * @return                 A new filtered {@code List} of {@code ExpressionCall}s, 
	 *                         corresponding to {@code calls}, with redundant calls removed.
	 */
	private static List<ExpressionCall> filterCalls(List<ExpressionCall> calls, ConditionUtils conditionUtils) {
	    log.entry(calls, conditionUtils);

        long startFilteringTimeInMs = System.currentTimeMillis();
        
        List<ExpressionCall> filteredCalls = new ArrayList<>();
        Set<Condition> validatedConditions = new HashSet<>();
        for (ExpressionCall call: calls) {
            //Check whether this call is less precise than another call with a better rank. 
            Condition cond = call.getCondition();
            if (Collections.disjoint(validatedConditions, conditionUtils.getDescendantConditions(cond))) {
                validatedConditions.add(cond);
                filteredCalls.add(call);
            } else {
                log.trace("Redundant call identified with condition: {}", cond);
            }
        }
        log.debug("Redundant calls filtered in {} ms", System.currentTimeMillis() - startFilteringTimeInMs);
        
        return log.exit(filteredCalls);
	}

	@Override
	protected void includeCss() {
	    log.entry();
		super.includeCss();
		this.includeCss("gene.css");
        this.includeCss("lib/jquery_plugins/jquery.dataTables.min.css");
        log.exit();
	}

	@Override
	protected void includeJs() {
	    log.entry();
		super.includeJs();
		this.includeJs("gene.js");
        this.includeJs("lib/jquery_plugins/jquery.dataTables.min.js");
        log.exit();
	}
}
