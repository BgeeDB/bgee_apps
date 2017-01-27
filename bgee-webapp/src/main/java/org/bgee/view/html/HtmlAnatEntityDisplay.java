package org.bgee.view.html;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.BgeeProperties;
import org.bgee.controller.CommandAnatEntity.AnatEntityResponse;
import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.anatdev.DevStage;
import org.bgee.model.expressiondata.ConditionUtils;
import org.bgee.model.expressiondata.Call.ExpressionCall;
import org.bgee.model.expressiondata.CallData.ExpressionCallData;
import org.bgee.model.expressiondata.baseelements.DataQuality;
import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.model.gene.Gene;
import org.bgee.controller.RequestParameters;
import org.bgee.view.AnatEntityDisplay;
import org.bgee.view.JsonHelper;

/**
 * This class is the HTML implementation of the {@code AnatEntityDisplay}.
 * 
 * @author  Julien Wollbrett
 * @version Bgee 13, Jan. 2017
 * @since   Bgee 13, Jan. 2017
 */
public class HtmlAnatEntityDisplay extends HtmlParentDisplay implements AnatEntityDisplay{
	
	private final static Logger log = LogManager.getLogger(HtmlGeneDisplay.class.getName());
	
	public HtmlAnatEntityDisplay(HttpServletResponse response, RequestParameters requestParameters, BgeeProperties prop,
	        JsonHelper jsonHelper, HtmlFactory factory) throws IllegalArgumentException, IOException {
		super(response, requestParameters, prop, jsonHelper, factory);
	}

	@Override
	public void displayAnatEntityHomePage() {
		log.entry();
		this.startDisplay("Anatomical entity information");
		
		this.writeln("<h1>Anatomical entity search</h1>");

        this.writeln("<div id='bgee_introduction'>");
        
        this.writeln("<p>Search for anatomical entities based on Uberon ontology.<p>");

        this.writeln("</div>");

		this.writeln(getAnatEntitySearchBox(false));
		
		this.endDisplay();
		log.exit();
		
	}
	
	/**
     * Get the search box of an anat. entity as a HTML 'div' element. 
     *
     * @return  the {@code String} that is the search box as HTML 'div' element.
     */
    protected String getAnatEntitySearchBox(boolean isSmallBox) {
        log.entry();
    
        RequestParameters urlExample = this.getNewRequestParameters();
        urlExample.setPage(RequestParameters.PAGE_ANAT_ENTITY);

        StringBuilder example = new StringBuilder();
        String bgeeAnatEntitySearchClass= "col-xs-11 small-search-box";
        if (!isSmallBox) {
        	example.append("<span class='examples'>Examples: ");
        	urlExample.setAnatEntityId("UBERON_0002107");
        	example.append("<a href='" + urlExample.getRequestURL() + "'>Liver</a>");
        	urlExample.setAnatEntityId("UBERON_0000948");
        	example.append(", <a href='" + urlExample.getRequestURL() + "'>Heart</a>");
        	urlExample.setAnatEntityId("UBERON_0000955");
        	example.append(", <a href='" + urlExample.getRequestURL() + "'>Brain</a>");
        	example.append("</span>");

        	bgeeAnatEntitySearchClass = "col-xs-offset-1 col-xs-10 "
        			+ "col-md-offset-2 col-md-8 "
        			+ "col-lg-offset-3 col-lg-6";
        }
        
        StringBuilder box = new StringBuilder();
        box.append("<div class='row'>");
        box.append("<div id='bgee_anat_entity_search' class='row well well-sm " + bgeeAnatEntitySearchClass + "'>");
        box.append("    <form action='javascript:void(0);' method='get'>");
        box.append("        <div class='form'>");
        box.append("            <label for='bgee_anat_entity_search_completion_box'>Search anatomical entity</label>");
        box.append(             example.toString());
        box.append("            <span id='bgee_species_search_msg' class='search_msg'></span>");
        box.append("            <input id='bgee_anat_entity_search_completion_box' class='form-control' " +
                                    "autocomplete='off' type='text' name='search'/>");
        box.append("        </div>");
        box.append("    </form>");
        box.append("</div>");
        box.append("</div>");

        return log.exit(box.toString());
    }

	@Override
	public void displayAnatEntity(AnatEntityResponse anatEntityResponse) {
		log.entry(anatEntityResponse);
	    
	    AnatEntity anatEntity = anatEntityResponse.getAnatEntity();
	    
	    String titleStart = "Anatomical entity: " + anatEntity.getName() + " - " + anatEntity.getId(); 
		this.startDisplay(titleStart);

		this.writeln("<div class='row'>");

		// Gene search
		this.writeln("<div class='col-sm-3'>");
		this.writeln(getAnatEntitySearchBox(true));
		this.writeln("</div>"); // close div

		//page title
		this.writeln("<h1 class='gene_title col-sm-9 col-lg-7'><img src='" 
		        + this.prop.getSpeciesImagesRootDirectory() + urlEncode(anatEntity.getName())
		        + "_light.jpg' alt='" + htmlEntities(anatEntity.getId()) 
		        + "' />" + htmlEntities(titleStart));
		
		
		this.writeln("</div>"); // close row

		//Anat. entity general information
		this.writeln("<h2>Anatomical Entity Information</h2>");
		this.writeln("<div class='anatEntity'>" + getAnatEntityInfo(anatEntity) + "</div>");

		
        //Expression data
		this.writeln("<h2>Expression</h2>");
		
		this.writeln("<div id='expr_data' class='row'>");
		
		//table-container
		this.writeln("<div class='col-xs-12 col-md-10'>");
		this.writeln("<div id='table-container'>");

		this.writeln(getExpressionHTMLByGene(
		        anatEntityResponse.getExprCalls(), 
		        anatEntityResponse.getGenesMap(),
		        anatEntityResponse.getConditionUtils()));
        
//		this.writeln("</div>"); // end table-container
//		this.writeln("</div>"); // end class
//		
//		//legend
//        this.writeln("<div class='legend col-xs-offset-1 col-xs-10 col-sm-offset-2 col-sm-8 col-md-offset-0 col-md-2 row'>");
//        this.writeln("<table class='col-xs-5 col-sm-3 col-md-12'>"
//        		+ "<caption>Sources</caption>" +
//                "<tr><th>A</th><td>Affymetrix</td></tr>" +
//                "<tr><th>E</th><td>EST</td></tr>" +
//                "<tr><th>I</th><td>In Situ</td></tr>" +
//                "<tr><th>R</th><td>RNA-Seq</li></td></tr></table>");
//        this.writeln("<table class='col-xs-offset-2 col-xs-5 col-sm-offset-1 col-sm-3 col-md-offset-0 col-md-12'>"
//                //XXX: temporarily "hide" qualities, as they are so incorrect at the moment. 
//                //for now we only report presence/absence of data per data type.
////        		+ "<caption>Qualities</caption>" +
////                "<tr><td><span class='quality high'>high quality</span></td></tr>" +
////                "<tr><td><span class='quality low'>low quality</span></td></tr>" +
////                "<tr><td><span class='quality nodata'>no data</span></td></tr></table>");
//                + "<tr><td><span class='quality high'>data</span></td></tr>" +
//                  "<tr><td><span class='quality nodata'>no data</span></td></tr></table>");
//        this.writeln("<table class='col-xs-offset-2 col-xs-5 col-sm-offset-1 col-sm-4 col-md-offset-0 col-md-12'>"
//                + "<caption>Rank scores</caption>"
//                + "<tr><th><span class='low-qual-score'>3.25e4</span></th>"
//                    + "<td>lightgrey: low confidence scores</td></tr>" +
//                "<tr><th><hr class='dotted-line' /></th>"
//                + "  <td>important score variation</td></tr></table>");
//        this.writeln("</div>"); // end legend
//        
//		this.writeln("</div>"); // end expr_data 
//
//		//other info
//		this.writeln("<div class='row'>");
//
//        this.writeln("<div id='expr_intro' class='col-xs-offset-1 col-sm-offset-2 col-sm-9 col-md-offset-0 col-md-10'>"
//                + "Rank scores of expression calls are normalized across genes, conditions and species. "
//                + "Low score means that the gene is highly expressed in the condition. "
//                + "Max rank score in all species: 4.79e4. Min rank score varies across species.</div>");
//        
//		//Source info
//		Set<DataType> allowedDataTypes = geneResponse.getExprCalls().stream()
//		        .flatMap(call -> call.getCallData().stream())
//		        .map(d -> d.getDataType())
//		        .collect(Collectors.toSet());
//
//		boolean hasSourcesForAnnot = gene.getSpecies().getDataTypesByDataSourcesForAnnotation() != null && 
//		        !gene.getSpecies().getDataTypesByDataSourcesForAnnotation().isEmpty();
//		boolean hasSourcesForData = gene.getSpecies().getDataTypesByDataSourcesForData() != null && 
//		        !gene.getSpecies().getDataTypesByDataSourcesForData().isEmpty();
//
//		if (hasSourcesForAnnot && hasSourcesForData) {
//		      this.writeln("<div class='sources col-xs-offset-1 col-sm-offset-2 col-md-offset-0 row'>");
//		}
//		if (hasSourcesForAnnot) {
//		    this.writeSources(gene.getSpecies().getDataTypesByDataSourcesForAnnotation(), 
//		            allowedDataTypes, "Sources of annotations to anatomy and development");
//		}
//		if (hasSourcesForData) {
//		    this.writeSources(gene.getSpecies().getDataTypesByDataSourcesForData(), 
//		            allowedDataTypes, "Sources of raw data");
//		}
//		
//		if (hasSourcesForAnnot && hasSourcesForData) {
//		    this.writeln("</div>"); // end info_sources 
//		}
//		this.writeln("</div>"); // end other info
//		
//		this.endDisplay();
//		log.exit();
		
	}
	

	
	private String getExpressionHTMLByGene(List<ExpressionCall> exprCalls, 
	        Map<String, Gene> genesByGeneId, 
	        final ConditionUtils conditionUtils) {
	    log.entry(exprCalls, genesByGeneId, conditionUtils);


		StringBuilder rowSb = new StringBuilder();
		for (ExpressionCall call: exprCalls) {
            final Gene gene = genesByGeneId.get(call.getGeneId());
            rowSb.append(getExpressionRowsForGene(gene, conditionUtils, call, false))
                 .append("\n");
		}

        StringBuilder sb = new StringBuilder();
		sb.append("<table class='expression stripe nowrap compact responsive'>")
		        .append("<thead><tr><th class='anat-entity-id'>Anat. entity ID</th>")
		        .append("<th class='anat-entity'>Anat. entity name</th>")
                .append("<th class='dev-stages min-table_sm'>Developmental stage</th>")
                .append("<th class='score'>Rank score</th>")
                //XXX: temporarily "hide" qualities, as they are so incorrect at the moment. 
                //for now we only report presence/absence of data per data type.
//				.append("<th class='quality min-table_md'>Quality</th></tr></thead>\n");
                .append("<th class='quality min-table_md'>Sources</th></tr></thead>\n");
		
		sb.append("<tbody>").append(rowSb.toString()).append("</tbody>");
		sb.append("</table>");
		return log.exit(sb.toString());

	}
	
	private String getExpressionRowsForGene(Gene gene, ConditionUtils conditionUtils,
	        ExpressionCall call, boolean scoreShift) {
	    log.entry(gene, conditionUtils, call, scoreShift);
        
		StringBuilder sb = new StringBuilder();
		String scoreShiftClassName = "gene-score-shift";
		sb.append("<tr");
		//score shift *between* anatomical structures
		if (scoreShift) {
		    sb.append(" class='").append(scoreShiftClassName).append("' ");
		}
		sb.append(">");
		String toAddToTd = "";
		if (scoreShift) {
		    toAddToTd = " class='" + scoreShiftClassName + "' ";
        }
		
		// gene ID and gene Name
		sb.append("<td class='details small'><a target='_blank'")
		    .append(htmlEntities(gene.getId()))
		    .append("</a></td><td").append(toAddToTd)
            .append(">")
			.append(htmlEntities(gene.getName())).append("</td>");
		
		
		// Dev stage cell
		DevStage stage = conditionUtils.getDevStage(call.getCondition().getDevStageId());
		sb.append("<td  class='details small'>")
                .append(htmlEntities(stage.getId())).append("</span>")
                .append(htmlEntities(stage.getName())).append("</li>");
            sb.append("</td>");
		
		// Rank
        sb.append("<td><li class='score' ");
        sb.append("'>").append(getRankScoreHTML(call))
          .append("</li>");
        sb.append("</td>");

		// Quality cell
		sb.append("<td class='qualities'>").
		append(getQualitySpans(call.getCallData()));
		sb.append("</td>");
		
		sb.append("</tr>");

		return log.exit(sb.toString());
	}
	
	private static String getAnatEntityInfo(AnatEntity anatEntity) {
	    log.entry(anatEntity);
	    
		final StringBuilder table = new StringBuilder("<table id='geneinfo'>");
		table.append("<tr><th>").append("Uberon ID</th><td>").append(htmlEntities(anatEntity.getId()))
		        .append("</td></tr>");
		table.append("<tr><th>").append("Name</th><td>").append(htmlEntities(anatEntity.getName())).append("</td></tr>");
		table.append("<tr><th>").append("Description</th><td>").append(htmlEntities(anatEntity.getDescription()))
		        .append("</td></tr>");
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
            //XXX: temporarily "hide" qualities, as they are so incorrect at the moment. 
            //for now we only report presence/absence of data per data type.
//			sb.append("low");
            sb.append("high");
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

private static String getRankScoreHTML(ExpressionCall call) {
    log.entry(call);

    //If the rank is above a threshold and is only supported by ESTs and/or in situ data, 
    //they we consider it of low confidence
    //TODO: there should be a better mechanism to handle that, and definitely not in the view, 
    //it is not its role to determine what is of low confidence...
    //Maybe create in bgee-core a new RankScore class, storing the rank and the confidence.
    Set<DataType> dataTypes = call.getCallData().stream().map(ExpressionCallData::getDataType)
            .collect(Collectors.toCollection(() -> EnumSet.noneOf(DataType.class)));
    String rankScore = htmlEntities(call.getFormattedGlobalMeanRank());
    if (dataTypes.contains(DataType.AFFYMETRIX) || 
            dataTypes.contains(DataType.RNA_SEQ) || 
            call.getGlobalMeanRank().compareTo(BigDecimal.valueOf(20000)) < 0) {
        return log.exit(rankScore);
    }
    StringBuilder sb = new StringBuilder();
    sb.append("<span class='low-qual-score'>").append(rankScore).append("</span>");
    return log.exit(sb.toString());
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
            this.includeCss("lib/jquery_plugins/vendor_gene.css");
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
            this.includeJs("lib/jquery_plugins/vendor_gene.js");
            this.includeJs("script_gene.js");
        }
        log.exit();
	}

}
