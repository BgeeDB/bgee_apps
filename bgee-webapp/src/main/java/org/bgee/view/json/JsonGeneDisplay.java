package org.bgee.view.json;

import java.io.IOException;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.BgeeProperties;
import org.bgee.controller.CommandGene.GeneResponse;
import org.bgee.controller.RequestParameters;
import org.bgee.model.XRef;
import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.expressiondata.Call.ExpressionCall;
import org.bgee.model.expressiondata.CallData.ExpressionCallData;
import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.model.gene.Gene;
import org.bgee.model.gene.GeneMatchResult;
import org.bgee.model.gene.GeneXRef;
import org.bgee.model.source.Source;
import org.bgee.model.species.Species;
import org.bgee.view.GeneDisplay;
import org.bgee.view.JsonHelper;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class JsonGeneDisplay extends JsonParentDisplay implements GeneDisplay{
	
	private final static Logger log = LogManager.getLogger(JsonGeneDisplay.class.getName());

    private final static Comparator<XRef> X_REF_COMPARATOR = Comparator
            .<XRef, Integer>comparing(x -> x.getSource().getDisplayOrder(), Comparator.nullsLast(Integer::compareTo))
            .thenComparing(x -> x.getSource().getName(), Comparator.nullsLast(String::compareTo))
            .thenComparing((XRef::getXRefId), Comparator.nullsLast(String::compareTo));
    
    private final static int MAX_DISPLAYED_ITEMS = 10;
    

    public JsonGeneDisplay(HttpServletResponse response, RequestParameters requestParameters, BgeeProperties prop,
			JsonHelper jsonHelper, JsonFactory factory) throws IllegalArgumentException, IOException {
		super(response, requestParameters, prop, jsonHelper, factory);
		// TODO Auto-generated constructor stub
	}

	
	@Override
	public void displayGeneHomePage() {
		// TODO Auto-generated method stub
        throw log.throwing(new UnsupportedOperationException("Not available for JSON display"));
		
	}

	@Override
	public void displayGeneSearchResult(String searchTerm, GeneMatchResult result) {
		// TODO Auto-generated method stub
        throw log.throwing(new UnsupportedOperationException("Not available for JSON display"));

		
	}
	

	@Override
	public void displayGene(GeneResponse geneResponse) {
		// TODO Auto-generated method stub
		 log.entry(geneResponse);

		/*
		 * Here we create a main LinkedHashMap "JSONHashMap" that we will pass to a Gson in order
		 * to generate the JSON response
		 * 
		 * So, here we will create different LinkedHashMap that we will pass to "JSONHashMap"
		 */
		
		LinkedHashMap<String, Object> JSONHashMap = new LinkedHashMap<String, Object>();//The main LinkedHashMap

		LinkedHashMap<String,String> geneHashMap = new LinkedHashMap<String,String>(); //LinkedHashMap containing the gene infos
		Gene gene = geneResponse.getGene();
		geneHashMap.put("geneId", gene.getEnsemblGeneId());
		geneHashMap.put("geneName", gene.getName());
		
		ArrayList<LinkedHashMap<String, Object>> anatEntitiesList = new ArrayList<LinkedHashMap<String, Object>>(); //ArrayList of anatomical entities
		geneResponse.getCallsByAnatEntity().forEach((anat, calls) -> {
		     ArrayList<LinkedHashMap<String, Object>> developmentalStages= new ArrayList<LinkedHashMap<String, Object>>(); //ArrayList of developmental stages
	    	 for(int i=0; i<calls.size(); i++) {
	    		 LinkedHashMap<String, Object> developmentalStageHashMap = new LinkedHashMap<String, Object>();
	    		 developmentalStageHashMap.put("id", calls.get(i).getCondition().getDevStage().getId());
	    		 developmentalStageHashMap.put("name", calls.get(i).getCondition().getDevStage().getName());
	    		 developmentalStageHashMap.put("rank", calls.get(i).getMeanRank());
	    		 developmentalStageHashMap.put("score", calls.get(i).getExpressionScore());
	    		 List<Boolean> dataTypes = new ArrayList<Boolean>();
	    		 dataTypes = getDataTypeSpans(calls.get(i).getCallData());
	    		 developmentalStageHashMap.put("Affymetrix", dataTypes.get(0));
	    		 developmentalStageHashMap.put("EST", dataTypes.get(1));
	    		 developmentalStageHashMap.put("in situ hybridization", dataTypes.get(2));
	    		 developmentalStageHashMap.put("RNA-Seq", dataTypes.get(3));
	    		 


	    		 log.debug(getDataTypeSpans(calls.get(i).getCallData()));
	    		 developmentalStages.add(developmentalStageHashMap);

	    	 }
	    	 LinkedHashMap<String, Object> anatEntitieHashMap = new LinkedHashMap<String, Object>();
	    	 anatEntitieHashMap.put("id", anat.getId());
	    	 anatEntitieHashMap.put("name", anat.getName());
	    	 anatEntitieHashMap.put("rank", calls.get(0).getMeanRank());
	    	 anatEntitieHashMap.put("score", calls.get(0).getExpressionScore());
	    	 anatEntitieHashMap.put("devStages", developmentalStages);
	    	 anatEntitiesList.add(anatEntitieHashMap);
	    	 
	     });
				
		JSONHashMap.put("gene", geneHashMap);
		JSONHashMap.put("anatEntities", anatEntitiesList);
		JSONHashMap.put("sources", getXRefDisplay(gene.getXRefs()));

		
        this.sendResponse("General information, expression calls and cross-references of the requested gene", JSONHashMap);

	}

	@Override
	public void displayGeneChoice(Set<Gene> genes) {
		// TODO Auto-generated method stub
        throw log.throwing(new UnsupportedOperationException("Not available for JSON display"));
		
	}

	
	private static List<Boolean> getDataTypeSpans(Collection<ExpressionCallData> callData) {
        log.entry(callData);
        final Map<DataType, Set<ExpressionCallData>> callsByDataTypes = callData.stream()
                .collect(Collectors.groupingBy(ExpressionCallData::getDataType, Collectors.toSet()));
        log.debug(callsByDataTypes);

        return log.exit(EnumSet.allOf(DataType.class).stream().map(type -> {
            return getDataSpan(type, callsByDataTypes.containsKey(type));
        }).collect(Collectors.toList()));
    }

	private static Boolean getDataSpan(DataType type, boolean hasData) {
        log.entry(hasData, type);
        
        boolean presence = false;
        if (hasData) {
            presence = true;
        }
        return log.exit(presence);
    }

	
	private LinkedHashMap<String, LinkedHashMap<String, String>>  getXRefDisplay(Set<XRef> xRefs) {
        log.entry(xRefs);

        LinkedHashMap<String, LinkedHashMap<String, String>> xRefsBySource = new
        		ArrayList<>(xRefs).stream()
        		                 .filter(x ->
        		StringUtils.isNotBlank(x.getSource().getXRefUrl()))
        		                 .sorted(X_REF_COMPARATOR)
        		                 .collect(Collectors.groupingBy(x ->
        		String.valueOf(x.getSource().getName()),
        		                         LinkedHashMap::new,
        		                             Collectors.toMap(x ->
        		String.valueOf(x.getXRefId()),
        		                                     x ->
        		String.valueOf(x.getXRefUrl(true, s -> this.urlEncode(s))),
        		                                     (u, v) -> {
        		                                         throw new
        		IllegalStateException(String.format("Duplicate key %s", u));
        		                                     },
        		                                     LinkedHashMap::new)));
        
        return xRefsBySource;
    }
	
	protected String urlEncode(String stringToWrite) {
        log.entry(stringToWrite);
        try {                            
            return log.exit(java.net.URLEncoder.encode(stringToWrite));
        } catch (Exception e) {
            log.catching(e);
            return log.exit("");
        }
    }
	
	
	
	class JsonGeneAdapter {
	    
	    private final String anatEntityId;
	    private final String anatomicalEntity;
	    private final ArrayList<String> developmentalId;
	    private final ArrayList<String> developmentalName;
	    private final BigDecimal rankScore;
	    private final BigDecimal expressionScore;
	    private final boolean affymetrixSources;
	    private final boolean ESTSources;
	    private final boolean inSituSources;
	    private final boolean RNASeqSources;

	    public JsonGeneAdapter(String anatEntityid, String anatomicalEntity, BigDecimal rankScore, 
	    		ArrayList<String> developmentalId, ArrayList<String> developmentalName, BigDecimal expressionScore,
	    		boolean affymetrixSources, boolean ESTSources, boolean inSituSources, boolean RNASeqSources) {
	        this.anatEntityId = anatEntityid;
	    	this.anatomicalEntity = anatomicalEntity;
	    	this.developmentalId=(ArrayList<String>) developmentalId.clone();
	    	this.developmentalName=(ArrayList<String>) developmentalName.clone();
	        this.rankScore =rankScore;
	        this.expressionScore=expressionScore;
	        this.affymetrixSources=affymetrixSources;
	        this.ESTSources=ESTSources;
	        this.inSituSources=inSituSources;
	        this.RNASeqSources=RNASeqSources;
	    }
	}
	
	class JsonGeneAdapter2 {
		private JsonGeneInfoAdapter gene;
		private ArrayList<JsonXrefAdapter> xrefs;
		private ArrayList<JsonAnatEntityAdapter> anatEntities;
		
		private JsonGeneAdapter2(JsonGeneInfoAdapter pGene, ArrayList<JsonXrefAdapter> pXrefs,
				ArrayList<JsonAnatEntityAdapter> pAnatEntities) {
			gene = pGene;
			xrefs = (ArrayList<JsonXrefAdapter>) pXrefs.clone();
			anatEntities = (ArrayList<JsonAnatEntityAdapter>) pAnatEntities.clone();
		}
	
	}
	
	private class JsonAnatEntityAdapter {
		private String id;
		private String name;
		private BigDecimal rank;
		private BigDecimal score;
		private String url;
		private ArrayList<JsonDevelopmentalStageAdapter> developmentalStages;
		
		private JsonAnatEntityAdapter(String pId, String pName, BigDecimal pRank, BigDecimal pScore, String pUrl,
				ArrayList<JsonDevelopmentalStageAdapter> pDevelopmentalStages) {
			id=pId;
			name=pName;
			rank=pRank;
			score=pScore;
			url=pUrl;
			developmentalStages = pDevelopmentalStages;
		}
	}
	
	//will be use in JsonAnatEntityAdapter, give informations relative to developmental stages
	private class JsonDevelopmentalStageAdapter{
		private String id;
		private String name;
		private BigDecimal rank;
		private BigDecimal score;
		private String url;
		
		private JsonDevelopmentalStageAdapter() {
			id = "default";
			name = "default";
			rank = new BigDecimal(0.0);
			score = new BigDecimal(0.0);
			url = "default";	
		}
		
		private JsonDevelopmentalStageAdapter(String pId, String pName, BigDecimal pRank, BigDecimal pScore, String pUrl) {
			id = pId;
			name = pName;
			rank = pRank;
			score = pScore;
			url = pUrl;
		}
		
		public void setId(String sId) {
			this.id=sId;
		}
		public void setName(String sName) {
			this.name=sName;
		}
		public void setRank(BigDecimal sRank) {
			this.rank=sRank;
		}
		public void setScore(BigDecimal sScore) {
			this.score=sScore;
		}
		public void setUrl(String sUrl) {
			this.url=sUrl;
		}
	}
	
	//will be use in JsonGeneAdapter, give informations relative to the gene
	private class JsonGeneInfoAdapter {
		private String id;
		private String name;
		
		private JsonGeneInfoAdapter() {
			id = "";
			name = "";
		}
		
		private JsonGeneInfoAdapter(String pId, String pName) {
			id = pId;
			name = pName;
		}
		
		public void setId(String sId) {
			this.id=sId;
		}
		
		public void setName(String sName) {
			this.name= sName;
		}
		
	}
	
	private class JsonXrefAdapter{
		private String name;
		private String url;
		
		private JsonXrefAdapter(String pName, String pUrl){
			name=pName;
			url=pUrl;
		}
	}

}
