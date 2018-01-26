package org.bgee.view.html;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringJoiner;
import java.util.TreeMap;
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
import org.bgee.model.source.Source;
import org.bgee.view.GeneDisplay;
import org.bgee.view.JsonHelper;

/**
 * This class is the HTML implementation of the {@code GeneDisplay}.
 * 
 * @author  Philippe Moret
 * @author  Valentine Rech de Laval
 * @author  Frederic Bastian
 * @version Bgee 13, July 2016
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
		this.writeln("<h1 class='gene_title col-sm-9 col-lg-7'><img src='" 
		        + this.prop.getBgeeRootDirectory() + this.prop.getSpeciesImagesRootDirectory() + urlEncode(gene.getSpeciesId())
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
		
		this.writeln("<div id='expr_data' class='row'>");
		
		//table-container
		this.writeln("<div class='col-xs-12 col-md-10'>");
		this.writeln("<div id='table-container'>");

		this.writeln(getExpressionHTMLByAnat(
		        geneResponse.getCallsByAnatEntityId(), 
		        geneResponse.getClusteringBestEachAnatEntity(), 
		        geneResponse.getClusteringWithinAnatEntity(), 
		        geneResponse.getConditionUtils()));
        
		this.writeln("</div>"); // end table-container
		this.writeln("</div>"); // end class
		
		//legend
        this.writeln("<div class='legend col-xs-offset-1 col-xs-10 col-sm-offset-2 col-sm-8 col-md-offset-0 col-md-2 row'>");
        this.writeln("<table class='col-xs-5 col-sm-3 col-md-12'>"
        		+ "<caption>Sources</caption>" +
                "<tr><th>A</th><td>Affymetrix</td></tr>" +
                "<tr><th>E</th><td>EST</td></tr>" +
                "<tr><th>I</th><td>In Situ</td></tr>" +
                "<tr><th>R</th><td>RNA-Seq</li></td></tr></table>");
        this.writeln("<table class='col-xs-offset-2 col-xs-5 col-sm-offset-1 col-sm-3 col-md-offset-0 col-md-12'>"
                //XXX: temporarily "hide" qualities, as they are so incorrect at the moment. 
                //for now we only report presence/absence of data per data type.
//        		+ "<caption>Qualities</caption>" +
//                "<tr><td><span class='quality high'>high quality</span></td></tr>" +
//                "<tr><td><span class='quality low'>low quality</span></td></tr>" +
//                "<tr><td><span class='quality nodata'>no data</span></td></tr></table>");
                + "<tr><td><span class='quality high'>data</span></td></tr>" +
                  "<tr><td><span class='quality nodata'>no data</span></td></tr></table>");
        this.writeln("<table class='col-xs-offset-2 col-xs-5 col-sm-offset-1 col-sm-4 col-md-offset-0 col-md-12'>"
                + "<caption>Rank scores</caption>"
                + "<tr><th><span class='low-qual-score'>3.25e4</span></th>"
                    + "<td>lightgrey: low confidence scores</td></tr>" +
                "<tr><th><hr class='dotted-line' /></th>"
                + "  <td>important score variation</td></tr></table>");
        this.writeln("</div>"); // end legend
        
		this.writeln("</div>"); // end expr_data 

		//other info
		this.writeln("<div class='row'>");

        this.writeln("<div id='expr_intro' class='col-xs-offset-1 col-sm-offset-2 col-sm-9 col-md-offset-0 col-md-10'>"
                + "Rank scores of expression calls are normalized across genes, conditions and species. "
                + "Low score means that the gene is highly expressed in the condition. "
                + "Max rank score in all species: 4.79e4. Min rank score varies across species.</div>");
        
		//Source info
		Set<DataType> allowedDataTypes = geneResponse.getExprCalls().stream()
		        .flatMap(call -> call.getCallData().stream())
		        .map(d -> d.getDataType())
		        .collect(Collectors.toSet());

		boolean hasSourcesForAnnot = gene.getSpecies().getDataTypesByDataSourcesForAnnotation() != null && 
		        !gene.getSpecies().getDataTypesByDataSourcesForAnnotation().isEmpty();
		boolean hasSourcesForData = gene.getSpecies().getDataTypesByDataSourcesForData() != null && 
		        !gene.getSpecies().getDataTypesByDataSourcesForData().isEmpty();

		if (hasSourcesForAnnot && hasSourcesForData) {
		      this.writeln("<div class='sources col-xs-offset-1 col-sm-offset-2 col-md-offset-0 row'>");
		}
		if (hasSourcesForAnnot) {
		    this.writeSources(gene.getSpecies().getDataTypesByDataSourcesForAnnotation(), 
		            allowedDataTypes, "Sources of annotations to anatomy and development");
		}
		if (hasSourcesForData) {
		    this.writeSources(gene.getSpecies().getDataTypesByDataSourcesForData(), 
		            allowedDataTypes, "Sources of raw data");
		}
		
		if (hasSourcesForAnnot && hasSourcesForData) {
		    this.writeln("</div>"); // end info_sources 
		}
		this.writeln("</div>"); // end other info
		
		this.endDisplay();
		log.exit();
	}

    /** 
     * Write sources corresponding to the gene species.
     * 
     * @param map               A {@code Map} where keys are {@code Source}s corresponding to 
     *                          data sources, the associated values being a {@code Set} of 
     *                          {@code DataType}s corresponding to data types of data.
     * @param allowedDataTypes  A {@code Set} of {@code DataType}s that are allowed data types
     *                          to display.
     * @param text              A {@code String} that is the sentence before the list of sources.
     */
    private void writeSources(Map<Source, Set<DataType>> map, Set<DataType> allowedDataTypes, String text) {
        log.entry(map, allowedDataTypes, text);

        // First, we invert map to be able to display data sources according to data types.
        // We use TreeMap to conserve order of data types.
        TreeMap<DataType, Set<Source>> dsByDataTypes = map.entrySet().stream()
                //transform the Entry<Source, Set<DataType>> into several Entry<DataType, Source>
                .flatMap(e -> e.getValue().stream().map(t -> new AbstractMap.SimpleEntry<>(t, e.getKey())))
                //keep only allowed data types
                .filter(e -> allowedDataTypes.contains(e.getKey()))
                //collect the Entry<DataType, Source> into a TreeMap<DataType, Set<Source>>
                .collect(Collectors.toMap(e -> e.getKey(), e -> new HashSet<>(Arrays.asList(e.getValue())), 
                        (s1, s2) -> {s1.addAll(s2); return s1;}, 
                        TreeMap::new));
        
        // Then, we display informations
        if (!dsByDataTypes.isEmpty()) {
            this.writeln("<div class='source-info'>");

            this.writeln(text + ": ");
            this.writeln("<ul>");

            for (Entry<DataType, Set<Source>> e : dsByDataTypes.entrySet()) {
                this.writeln("<li>");
                this.writeln(e.getKey().getStringRepresentation().substring(0, 1).toUpperCase(Locale.ENGLISH) 
                        + e.getKey().getStringRepresentation().substring(1) + " data: ");
                StringJoiner sj = new StringJoiner(", ");
                for (Source source : e.getValue()) {
                    String target = source.getName().toLowerCase().equals("bgee")? "" : " target='_blank'";
                    sj.add("<a href='" + source.getBaseUrl() + "'" + target + ">" + 
                            source.getName() + "</a>");
                }
                this.writeln(sj.toString());
            }
            this.writeln("</ul>");
            this.writeln("</div>");
        }

        log.exit();
    }

	/**
	 * Generates the HTML code displaying information about expression calls.
	 * 
	 * @param byAnatEntityId               A {@code Map} where keys are {@code String}s representing 
	 *                                     anatomical entity IDs, the associated value being a {@code List} 
	 *                                     of {@code ExpressionCall}s for this anatomical entity, 
	 *                                     ordered by their global mean rank. 
     * @param clusteringBestEachAnatEntity A {@code Map} where keys are {@code ExpressionCall}s, 
     *                                     the associated value being the index of the group 
     *                                     in which they are clustered, based on their global mean rank. 
     *                                     This custering is generated by only considering 
     *                                     the best {@code ExpressionCall} from each anatomical entity.
     * @param clusteringWithinAnatEntity   A {@code Map} where keys are {@code ExpressionCall}s, 
     *                                     the associated value being the index of the group 
     *                                     in which they are clustered, based on their global mean rank. 
     *                                     This custering is generated independently 
     *                                     for each anatomical entity (so, {@code ExpressionCall}s 
     *                                     associated to a same value in the {@code Map} 
     *                                     might not be part of a same cluster).
	 * @param conditionUtils               A {@code ConditionUtils} containing information 
	 *                                     about all {@code Condition}s retrieved from 
	 *                                     the {@code ExpressionCall}s in {@code byAnatEntityId}.
	 * @return                             A {@code String} that is the generated HTML.
	 */
	private String getExpressionHTMLByAnat(Map<String, List<ExpressionCall>> byAnatEntityId, 
	        Map<ExpressionCall, Integer> clusteringBestEachAnatEntity, 
	        Map<ExpressionCall, Integer> clusteringWithinAnatEntity, 
	        final ConditionUtils conditionUtils) {
	    log.entry(byAnatEntityId, clusteringBestEachAnatEntity, clusteringWithinAnatEntity, 
	            conditionUtils);


		StringBuilder rowSb = new StringBuilder();
		Integer previousGroupIndex = null;
		for (Entry<String, List<ExpressionCall>> anatRow: byAnatEntityId.entrySet()) {
            final AnatEntity a = conditionUtils.getAnatEntity(anatRow.getKey());
            final List<ExpressionCall> calls = anatRow.getValue();
            
            boolean scoreShift = false;
            Integer currentGroupIndex = clusteringBestEachAnatEntity.get(calls.get(0));
            assert currentGroupIndex != null: "Every best call should be part of a group.";
            if (previousGroupIndex != null && previousGroupIndex != currentGroupIndex) {
                scoreShift = true;
            }
            
            rowSb.append(getExpressionRowsForAnatEntity(a, conditionUtils, calls, scoreShift, 
                    clusteringWithinAnatEntity))
                 .append("\n");
            previousGroupIndex = currentGroupIndex;
		}

        StringBuilder sb = new StringBuilder();
		sb.append("<table class='expression stripe nowrap compact responsive'>")
		        .append("<thead><tr><th class='anat-entity-id'>Anat. entity ID</th>")
		        .append("<th class='anat-entity'>Anatomical entity</th>")
                .append("<th class='dev-stages min-table_sm'>Developmental stage(s)</th>")
                .append("<th class='score'>Rank score</th>")
                //XXX: temporarily "hide" qualities, as they are so incorrect at the moment. 
                //for now we only report presence/absence of data per data type.
//				.append("<th class='quality min-table_md'>Quality</th></tr></thead>\n");
                .append("<th class='quality min-table_md'>Sources</th></tr></thead>\n");
		
		sb.append("<tbody>").append(rowSb.toString()).append("</tbody>");
		sb.append("</table>");
		return log.exit(sb.toString());

	}

	/**
	 * Generates the HTML code to display information about expression calls occurring  
	 * in one specific anatomical entity. 
	 * 
	 * @param anatEntity                   The {@code AnatEntity} for which the expression calls 
	 *                                     will be displayed.
     * @param conditionUtils               A {@code ConditionUtils} containing information 
     *                                     about all {@code Condition}s retrieved from the {@code calls}.
	 * @param calls                        A {@code List} of {@code ExpressionCall}s related to 
	 *                                     {@code anatEntity}, ordered by their global mean rank. 
	 * @param scoreShift                   A {@code boolean} defining whether the global mean rank 
	 *                                     for this anatomical entity is in the same cluster as 
	 *                                     the global mean rank of the previous anatomical entity.
	 *                                     If {@code true}, there are not in the same cluster. 
     * @param clusteringWithinAnatEntity   A {@code Map} where keys are {@code ExpressionCall}s, 
     *                                     the associated value being the index of the group 
     *                                     in which they are clustered, based on their global mean rank. 
     *                                     This custering is generated independently 
     *                                     for each anatomical entity (so, {@code ExpressionCall}s 
     *                                     associated to a same value in the {@code Map} 
     *                                     might not be part of a same cluster).
	 * @return                             A {@code String} that is the generated HTML.
	 */
	private String getExpressionRowsForAnatEntity(AnatEntity anatEntity, ConditionUtils conditionUtils,
	        List<ExpressionCall> calls, boolean scoreShift, 
	        Map<ExpressionCall, Integer> clusteringWithinAnatEntity) {
	    log.entry(anatEntity, conditionUtils, calls, scoreShift, clusteringWithinAnatEntity);
        
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
		
		// Anat entity ID and Anat entity cells 
		String anatEntityUrl = "http://purl.obolibrary.org/obo/" 
		    + this.urlEncode(anatEntity.getId().replace(':', '_'));
		sb.append("<td class='details small'><a target='_blank' href='").append(anatEntityUrl)
		    .append("' title='External link to ontology visualization'>")
		    .append(htmlEntities(anatEntity.getId()))
		    .append("</a></td><td").append(toAddToTd)
            .append(">")
			.append(htmlEntities(anatEntity.getName())).append("</td>");
		
		
		// Dev stage cell
		sb.append("<td><span class='expandable' title='click to expand'>[+] ").append(calls.size())
			.append(" stage").append(calls.size() > 1? "s": "").append("</span>")
			.append("<ul class='masked dev-stage-list'>");
		Integer previousGroupInd = null;
		for (ExpressionCall call: calls) {
		    DevStage stage = conditionUtils.getDevStage(call.getCondition().getDevStageId());
		    int currentGroupInd = clusteringWithinAnatEntity.get(call);
            sb.append("<li class='dev-stage ");
            if (previousGroupInd != null && previousGroupInd != currentGroupInd) {
                sb.append(scoreShiftClassName);
            }
            sb.append("'><span class='details small'>")
                .append(htmlEntities(stage.getId())).append("</span>")
                .append(htmlEntities(stage.getName())).append("</li>");
            sb.append("\n");
            previousGroupInd = currentGroupInd;
		}
		sb.append("</ul></td>");
		
		//Global mean rank
	    sb.append("<td>").append(getRankScoreHTML(calls.get(0)))
	        .append("<ul class='masked score-list'>");
        previousGroupInd = null;
	    for (ExpressionCall call: calls) {
            int currentGroupInd = clusteringWithinAnatEntity.get(call);
            sb.append("<li class='score ");
            if (previousGroupInd != null && previousGroupInd != currentGroupInd) {
                sb.append(scoreShiftClassName);
            }
            sb.append("'>").append(getRankScoreHTML(call))
              .append("</li>");
            sb.append("\n");
            previousGroupInd = currentGroupInd;
        }
        sb.append("</ul></td>");

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
	
	/**
	 * @param call An {@code ExpressionCall} for which we want to display global mean rank.
	 * @return     A {@code String} containing the HTML to display the global mean rank, 
	 *             notably displaying information about confidence in the rank.
	 */
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
