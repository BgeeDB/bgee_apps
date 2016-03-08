package org.bgee.view.html;

import java.io.IOException;
import java.util.Collection;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.BgeeProperties;
import org.bgee.controller.CommandGene.GeneResponse;
import org.bgee.controller.RequestParameters;
import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.anatdev.DevStage;
import org.bgee.model.expressiondata.Call.ExpressionCall;
import org.bgee.model.expressiondata.CallData.ExpressionCallData;
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
 * @version Bgee 13, Mar. 2016
 * @since   Bgee 13, Oct. 2015
 */
public class HtmlGeneDisplay extends HtmlParentDisplay implements GeneDisplay {
    private final static Logger log = LogManager.getLogger(HtmlGeneDisplay.class.getName());

	/**
	 * @param response             A {@code HttpServletResponse} that will be used to display 
	 *                             the page to the client.
	 * @param requestParameters    The {@code RequestParameters} that handles the parameters of
	 *                             the current request.
	 * @param prop                 A {@code BgeeProperties} instance that contains the properties
	 *                             to use.
	 * @param factory              The {@code HtmlFactory} that instantiated this object.
	 * @throws IOException         If there is an issue when trying to get or to use the {@code PrintWriter}.
	 */
	public HtmlGeneDisplay(HttpServletResponse response, RequestParameters requestParameters, BgeeProperties prop,
	        JsonHelper jsonHelper, HtmlFactory factory) throws IllegalArgumentException, IOException {
		super(response, requestParameters, prop, jsonHelper, factory);
	}

	/*Genes: the terms you enter are searched in gene IDs from Ensembl, names, and synonyms.*/
	@Override
	public void displayGeneHomePage() {
	    log.entry();
		this.startDisplay("Gene information");
		
		this.writeln("<h1>Gene search</h1>");

        this.writeln("<div id='bgee_introduction'>");
        
        this.writeln("<p>Search for genes based on Ensembl gene IDs, gene names, and synonyms.<p>");

        this.writeln("</div>");

		this.writeln(getGeneSearchBox(false));
		
		this.endDisplay();
		log.exit();
	}
    
    /**
     * Get the search box of a gene as a HTML 'div' element. 
     *
     * @return  the {@code String} that is the search box as HTML 'div' element.
     */
    protected String getGeneSearchBox(boolean isSmallBox) {
        log.entry();
    
        RequestParameters urlExample = this.getNewRequestParameters();
        urlExample.setPage(RequestParameters.PAGE_GENE);

        StringBuilder example = new StringBuilder();
        String bgeeGeneSearchClass= "col-xs-11 small-search-box";
        if (!isSmallBox) {
        	example.append("<span class='examples'>Examples: ");
        	urlExample.setGeneId("ENSG00000244734");
        	example.append("<a href='" + urlExample.getRequestURL() + "'>HBB</a> (human)");
        	urlExample.setGeneId("ENSMUSG00000040564");
        	example.append(", <a href='" + urlExample.getRequestURL() + "'>Apoc1</a> (mouse)");
        	urlExample.setGeneId("ENSG00000178104");
        	example.append(", <a href='" + urlExample.getRequestURL() + "'>PDE4DIP</a> (human)");
        	urlExample.setGeneId("ENSDARG00000035350");
        	example.append(", <a href='" + urlExample.getRequestURL() + "'>ins</a> (zebrafish)");
        	example.append("</span>");

        	bgeeGeneSearchClass = "col-xs-offset-1 col-xs-10 "
        			+ "col-md-offset-2 col-md-8 "
        			+ "col-lg-offset-3 col-lg-6";
        }
        
        StringBuilder box = new StringBuilder();
        box.append("<div class='row'>");
        box.append("<div id='bgee_gene_search' class='row well well-sm " + bgeeGeneSearchClass + "'>");
        box.append("    <form action='javascript:void(0);' method='get'>");
        box.append("        <div class='form'>");
        box.append("            <label for='bgee_gene_search_completion_box'>Search gene</label>");
        box.append(             example.toString());
        box.append("            <span id='bgee_species_search_msg' class='search_msg'></span>");
        box.append("            <input id='bgee_gene_search_completion_box' class='form-control' " +
                                    "autocomplete='off' type='text' name='search'/>");
        box.append("        </div>");
        box.append("    </form>");
        box.append("</div>");
        box.append("</div>");

        return log.exit(box.toString());
    }

	@Override
	public void displayGene(GeneResponse geneResponse) {
	    log.entry(geneResponse);
	    
	    Gene gene = geneResponse.getGene();
	    
	    String titleStart = "Gene: " + gene.getName() + " - " + gene.getId(); 
		this.startDisplay(titleStart);

		this.writeln("<div class='row'>");

		// Gene search
		this.writeln("<div class='col-sm-3'>");
		this.writeln(getGeneSearchBox(true));
		this.writeln("</div>"); // close div

		//page title
		this.writeln("<h1 class='gene_title col-sm-9'><img src='" 
		        + this.prop.getSpeciesImagesRootDirectory() + urlEncode(gene.getSpeciesId())
		        + "_light.jpg' alt='" + htmlEntities(gene.getSpecies().getShortName()) 
		        + "' />" + htmlEntities(titleStart) 
				+ " - <em>" + htmlEntities(gene.getSpecies().getScientificName()) + "</em> ("
                + htmlEntities(gene.getSpecies().getName()) + ")</h1>");
		
		
		this.writeln("</div>"); // close row

		//Gene general information
		this.writeln("<h2>Gene Information</h2>");
		this.writeln("<div class='gene'>" + getGeneInfo(gene) + "</div>");

		
        //Expression data
		this.writeln("<h2>Expression</h2>");
		this.writeln("<div id='expr_intro'>Expression calls ordered by the normalized ranks "
		        + "of the gene in the conditions: </div>");
		
		this.writeln("<div id='expr_data' class='row'>");
		
		//table-container
		this.writeln("<div class='col-xs-12 col-md-10'>");
		this.writeln("<div id='table-container'>");
		this.writeln(getExpressionHTMLByAnat(
		        filterAndGroupByAnatEntity(geneResponse), 
		        geneResponse.getConditionUtils()));
		this.writeln("</div>"); // end table-container
		this.writeln("</div>"); // end class
		
		//legend
        this.writeln("<div class='legend'>");
        this.writeln("<table class='col-xs-offset-1 col-xs-4 col-md-offset-0 col-md-2'>"
        		+ "<caption>Sources</caption>" +
                "<tr><th>A</th><td>Affymetrix</td></tr>" +
                "<tr><th>E</th><td>EST</td></tr>" +
                "<tr><th>I</th><td>In Situ</td></tr>" +
                "<tr><th>R</th><td>RNA-Seq</li></td></tr></table>");
        this.writeln("<table class='col-xs-offset-2 col-xs-4 col-md-offset-0 col-md-2'>"
        		+ "<caption>Qualities</caption>" +
                "<tr><td><span class='quality high'>high quality</span></td></tr>" +
                "<tr><td><span class='quality low'>low quality</span></td></tr>" +
                "<tr><td><span class='quality nodata'>no data</span></td></tr></table>");
        this.writeln("</div>"); // end legend
        
		this.writeln("</div>"); // end expr_data 

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
	private String getExpressionHTMLByAnat(Map<String, List<ExpressionCall>> byAnatEntityId,
	        final ConditionUtils conditionUtils) {
	    log.entry(byAnatEntityId, conditionUtils);

		StringBuilder sb = new StringBuilder();

		String elements = byAnatEntityId.entrySet().stream().map(e -> {
			final AnatEntity a = conditionUtils.getAnatEntity(e.getKey());
			final List<ExpressionCall> calls = e.getValue();

			return getExpressionRowsForAnatEntity(a, conditionUtils, calls);
		}).collect(Collectors.joining("\n"));

		sb.append("<table class='expression stripe nowrap compact responsive'>")
		        .append("<thead><tr><th class='anat-entity-id'>Anat. entity ID</th>")
		        .append("<th class='anat-entity'>Anatomical entity</th>")
				.append("<th class='dev-stages min-tablet-l'><strong>Developmental stage(s)</strong></th>")
				.append("<th class='quality'><strong>Quality</strong></th></tr></thead>\n");
		sb.append("<tbody>").append(elements).append("</tbody>");
		sb.append("</table>");
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
	private String getExpressionRowsForAnatEntity(AnatEntity anatEntity, ConditionUtils conditionUtils,
	        List<ExpressionCall> calls) {
	    log.entry(anatEntity, conditionUtils, calls);
	    
		StringBuilder sb = new StringBuilder();
		sb.append("<tr>");
		
		// Anat entity ID and Anat entity cells 
		String anatEntityUrl = "http://purl.obolibrary.org/obo/" 
		    + this.urlEncode(anatEntity.getId().replace(':', '_'));
		sb.append("<td class='details small'><a target='_blank' href='").append(anatEntityUrl)
		    .append("' title='External link to ontology visualization'>")
		    .append(htmlEntities(anatEntity.getId()))
		    .append("</a></td><td>")
			.append(htmlEntities(anatEntity.getName())).append("</td>");
		
		// Dev stage cell
		sb.append("<td><span class='expandable' title='click to expand'>[+] ").append(calls.size())
			.append(" stage(s)</span>")
			.append("<ul class='masked dev-stage-list'>")
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
				.append("<ul class='masked quality-list'>")
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
	 * of associated {@code ExpressionCall}s. The order of the input list is
	 * preserved, and redundant {@code ExpressionCall}s are filtered out.
	 * 
	 * @param geneResponse A {@code GeneResponse}, notably containing the {@code List} of 
	 *                     {@code ExpressionCall}s, and {@code Set} of redundant {@code ExpressionCall}s.
	 * @return             The @{code {@link LinkedHashMap} containing the association, 
	 *                     with {@code String}s representing anat. entity ID as key, the associated value 
	 *                     being a ranked {@code List} of {@code ExpressionCall}s.
	 */
	private static LinkedHashMap<String, List<ExpressionCall>> filterAndGroupByAnatEntity(
	        GeneResponse geneResponse) {
	    log.entry(geneResponse);
	    //we explicitly define the Collector, otherwise javac has a bug preventing to infer correct type.
	    Collector<ExpressionCall, ?, LinkedHashMap<String, List<ExpressionCall>>> collector = 
	            Collectors.groupingBy(ec -> ec.getCondition().getAnatEntityId(), 
	                    LinkedHashMap::new, Collectors.toList());
	    
		return log.exit(geneResponse.getExprCalls().stream()
		        .filter(call -> !geneResponse.getRedundantExprCalls().contains(call))
		        .collect(collector));
	}

	@Override
	protected void includeCss() {
	    log.entry();
	    
        //If you ever add new files, you need to edit bgee-webapp/pom.xml 
        //to correctly merge/minify them.
        if (!this.prop.isMinify()) {
            this.includeCss("lib/jquery_plugins/jquery.dataTables.min.css");
            this.includeCss("lib/jquery_plugins/responsive.dataTables.min.css");
        } else {
            this.includeCss("lib/jquery_plugins/jquery.dataTables.css");
            this.includeCss("lib/jquery_plugins/responsive.dataTables.css");
        }
        this.includeCss("gene.css");

        //we need to add the Bgee CSS files at the end, to override CSS file from external libs
        super.includeCss();
        
        log.exit();
	}

	@Override
	protected void includeJs() {
	    log.entry();
	    
		super.includeJs();
        //If you ever add new files, you need to edit bgee-webapp/pom.xml 
        //to correctly merge/minify them.
        if (!this.prop.isMinify()) {
            this.includeJs("lib/jquery_plugins/jquery.dataTables.min.js");
            this.includeJs("lib/jquery_plugins/dataTables.responsive.min.js");
            this.includeJs("gene.js");
            this.includeJs("autoCompleteGene.js");
            this.includeJs("jquery_ui_autocomplete_modif.js");
        } else {
            this.includeJs("lib/jquery_plugins/jquery.dataTables.js");
            this.includeJs("lib/jquery_plugins/dataTables.responsive.js");
            this.includeJs("script_gene.js");
        }
        log.exit();
	}
}
