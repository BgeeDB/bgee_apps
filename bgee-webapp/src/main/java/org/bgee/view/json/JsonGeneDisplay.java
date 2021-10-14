package org.bgee.view.json;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.BgeeProperties;
import org.bgee.controller.CommandGene.GeneResponse;
import org.bgee.controller.RequestParameters;
import org.bgee.model.XRef;
import org.bgee.model.expressiondata.Call.ExpressionCall;
import org.bgee.model.expressiondata.CallData.ExpressionCallData;
import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.model.gene.Gene;
import org.bgee.model.gene.GeneMatch;
import org.bgee.model.gene.GeneMatchResult;
import org.bgee.view.GeneDisplay;
import org.bgee.view.JsonHelper;

public class JsonGeneDisplay extends JsonParentDisplay implements GeneDisplay {

    private final static Logger log = LogManager.getLogger(JsonGeneDisplay.class.getName());

    private final static Comparator<XRef> X_REF_COMPARATOR = Comparator
            .<XRef, Integer>comparing(x -> x.getSource().getDisplayOrder(), Comparator.nullsLast(Integer::compareTo))
            .thenComparing(x -> x.getSource().getName(), Comparator.nullsLast(String::compareTo))
            .thenComparing((XRef::getXRefId), Comparator.nullsLast(String::compareTo));

    public JsonGeneDisplay(HttpServletResponse response, RequestParameters requestParameters, BgeeProperties prop,
            JsonHelper jsonHelper, JsonFactory factory) throws IllegalArgumentException, IOException {
        super(response, requestParameters, prop, jsonHelper, factory);
    }

    @Override
    public void displayGeneHomePage() {
        throw log.throwing(new UnsupportedOperationException("Not available for JSON display"));

    }

    @Override
    public void displayGeneSearchResult(String searchTerm, GeneMatchResult result) {
        LinkedHashMap<String, Object> resultHashMap = new LinkedHashMap<String, Object>();
        resultHashMap.put("query", searchTerm);
        resultHashMap.put("result", getSearchResultTable(result.getGeneMatches(), searchTerm));
        this.sendResponse("General information, expression calls and cross-references of the requested gene",
                resultHashMap);
        
    }

    @Override
    public void displayGene(GeneResponse geneResponse) {
        log.traceEntry("{}", geneResponse);

        // create LinkedHashMap that we will pass to Gson in order to generate the JSON 
        LinkedHashMap<String, Object> JSONHashMap = new LinkedHashMap<String, Object>();

        // LinkedHashMap containing gene infos
        LinkedHashMap<String, String> geneHashMap = new LinkedHashMap<String, String>();
        Gene gene = geneResponse.getGene();
        geneHashMap.put("geneId", gene.getEnsemblGeneId());
        geneHashMap.put("geneName", gene.getName());

        // ArrayList of anatomical entities
        ArrayList<LinkedHashMap<String, Object>> anatEntitiesList = 
                new ArrayList<LinkedHashMap<String, Object>>(); 
        // for each anatomical entity
        geneResponse.getCallsByOrganCall().forEach((anat, calls) -> {
            ArrayList<LinkedHashMap<String, Object>> developmentalStages = 
                    new ArrayList<LinkedHashMap<String, Object>>();

            for (ExpressionCall call: calls) {
                LinkedHashMap<String, Object> developmentalStageHashMap = 
                        new LinkedHashMap<String, Object>();
                developmentalStageHashMap.put("id", call.getCondition().getDevStage().getId());
                developmentalStageHashMap.put("name", call.getCondition().getDevStage().getName());
                developmentalStageHashMap.put("rank", call.getMeanRank());
                developmentalStageHashMap.put("score", call.getExpressionScore());
                List<Boolean> dataTypes = new ArrayList<Boolean>();
                dataTypes = getDataTypeSpans(call.getCallData());
                developmentalStageHashMap.put("Affymetrix", dataTypes.get(DataType
                        .valueOf("AFFYMETRIX").ordinal()));
                developmentalStageHashMap.put("EST", dataTypes.get(DataType.valueOf("EST")
                        .ordinal()));
                developmentalStageHashMap.put("in situ hybridization", dataTypes.get(DataType
                        .valueOf("IN_SITU").ordinal()));
                developmentalStageHashMap.put("RNA-Seq", dataTypes.get(DataType.valueOf("RNA_SEQ")
                        .ordinal()));

                log.debug(getDataTypeSpans(call.getCallData()));
                developmentalStages.add(developmentalStageHashMap);

            }
            LinkedHashMap<String, Object> anatEntitieHashMap = new LinkedHashMap<String, Object>();
            //TODO: to adapt to new code
//            anatEntitieHashMap.put("id", anat.getId());
//            anatEntitieHashMap.put("name", anat.getName());
//            // The min rank and highest expression score of all dev. stages is used at anat. entity 
//            // level
//            anatEntitieHashMap.put("rank", calls.get(0).getMeanRank());
//            anatEntitieHashMap.put("score", calls.get(0).getExpressionScore());
//            anatEntitieHashMap.put("devStages", developmentalStages);
//            anatEntitiesList.add(anatEntitieHashMap);

        });

        JSONHashMap.put("gene", geneHashMap);
        JSONHashMap.put("anatEntities", anatEntitiesList);
        JSONHashMap.put("sources", getXRefDisplay(gene.getXRefs()));

        this.sendResponse("General information, expression calls and cross-references of the requested gene",
                JSONHashMap);

    }

    @Override
    public void displayGeneChoice(Set<Gene> genes) {
        // TODO Auto-generated method stub
        throw log.throwing(new UnsupportedOperationException("Not available for JSON display"));

    }

    private static List<Boolean> getDataTypeSpans(Collection<ExpressionCallData> callData) {
        log.traceEntry("{}", callData);
        final Map<DataType, Set<ExpressionCallData>> callsByDataTypes = callData.stream()
                .collect(Collectors.groupingBy(ExpressionCallData::getDataType, Collectors.toSet()));
        log.debug(callsByDataTypes);

        return log.traceExit(EnumSet.allOf(DataType.class).stream().map(type -> {
            return getDataSpan(type, callsByDataTypes.containsKey(type));
        }).collect(Collectors.toList()));
    }

    private static Boolean getDataSpan(DataType type, boolean hasData) {
        log.traceEntry("{}, {}", hasData, type);

        boolean presence = false;
        if (hasData) {
            presence = true;
        }
        return log.traceExit(presence);
    }

    private LinkedHashMap<String, LinkedHashMap<String, String>> getXRefDisplay(Set<XRef> xRefs) {
        log.traceEntry("{}", xRefs);

        LinkedHashMap<String, LinkedHashMap<String, String>> xRefsBySource = new ArrayList<>(xRefs).stream()
                .filter(x -> StringUtils.isNotBlank(x.getSource().getXRefUrl())).sorted(X_REF_COMPARATOR)
                .collect(Collectors.groupingBy(x -> String.valueOf(x.getSource().getName()), LinkedHashMap::new,
                        Collectors.toMap(x -> String.valueOf(x.getXRefId()),
                                x -> String.valueOf(x.getXRefUrl(true, s -> this.urlEncode(s))), (u, v) -> {
                                    throw new IllegalStateException(String.format("Duplicate key %s", u));
                                }, LinkedHashMap::new)));

        return xRefsBySource;
    }

    protected String urlEncode(String stringToWrite) {
        log.traceEntry("{}", stringToWrite);
        try {
            return log.traceExit(java.net.URLEncoder.encode(stringToWrite, "UTF-8"));
        } catch (Exception e) {
            log.catching(e);
            return log.traceExit("");
        }
    }
    
    private ArrayList<LinkedHashMap<String, String>> getSearchResultTable(List<GeneMatch> geneMatches, String searchTerm) {
        log.traceEntry("{}, {}", geneMatches, searchTerm);

        ArrayList<LinkedHashMap<String, String>> searchResultArrayList = new ArrayList<LinkedHashMap<String, String>>(); 
                
        for (GeneMatch geneMatch: geneMatches) {
            LinkedHashMap<String, String> geneMatchHashMap = new LinkedHashMap<String, String>();
            Gene gene = geneMatch.getGene();
            geneMatchHashMap.put("id",gene.getEnsemblGeneId());
            geneMatchHashMap.put("name",gene.getName());
            geneMatchHashMap.put("description",gene.getDescription());
            geneMatchHashMap.put("organism",gene.getSpecies().getScientificName() + " (" + gene.getSpecies().getName() + ")");
            geneMatchHashMap.put("match",getMatch(geneMatch, searchTerm));
            searchResultArrayList.add(geneMatchHashMap);
        }
        return searchResultArrayList;
    }
    
    private String getMatch(GeneMatch geneMatch, String searchTerm) {
        log.traceEntry("{}, {}", geneMatch, searchTerm);
        
        if (GeneMatch.MatchSource.MULTIPLE.equals(geneMatch.getMatchSource())) {
            return log.traceExit("no exact match");
        }

        return log.traceExit(geneMatch.getMatch() +
                " (" + geneMatch.getMatchSource().toString().toLowerCase() + ")");
    }
}