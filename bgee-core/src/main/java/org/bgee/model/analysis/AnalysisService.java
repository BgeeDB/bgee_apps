//package org.bgee.model.analysis;
//
//import java.util.Collection;
//import java.util.HashMap;
//import java.util.IdentityHashMap;
//import java.util.Map;
//import java.util.Set;
//import java.util.concurrent.locks.Condition;
//
//import org.bgee.model.anatdev.AnatEntityService;
//import org.bgee.model.anatdev.AnatEntitySimilarity;
//import org.bgee.model.anatdev.DevStageService;
//import org.bgee.model.anatdev.DevStageSimilarity;
//import org.bgee.model.dao.api.expressiondata.ExpressionCallDAO.ExpressionCallTO;
//import org.bgee.model.expressiondata.Call.ExpressionCall;
//import org.bgee.model.expressiondata.CallService;
//import org.bgee.model.expressiondata.ConditionFilter;
//import org.bgee.model.expressiondata.ConditionUtils;
//import org.bgee.model.expressiondata.MultiSpeciesCall;
//import org.bgee.model.gene.Gene;
//import org.bgee.model.gene.GeneService;
//
//public class AnalysisService {
//
//    private GeneService geneService;
//
//    private CallService callService;
//
//    private AnatEntityService anatEntityService;
//
//    private DevStageService devStageService;
//
//    public Set<MultiSpeciesCall<?>> loadMultiSpecies(Gene gene, Set<String> speciesIds) {
//        String speciesId = gene.getSpeciesId();
//        if (speciesId == null) {
//            throw new IllegalArgumentException("Expecting a species Id for gene:" + gene);
//        }
//
//        // 1. Get all relevant taxons from the species
//        Set<String> taxonIds; // TODO: implement
//
//        //
//        Map<String, MultiSpeciesCall<?>> results = new HashMap<String, MultiSpeciesCall<?>>();
//        for (String taxonId : taxonIds) {
//            Map<String, Set<String>> omaToGeneIds = geneService.getOrthologies(taxonId, speciesIds);
//            // single loading for the given
//            // callService.loadCallsInMultiSpecies(taxonFilter, callFilters);
//            // new method in CallService ?
//            Set<ExpressionCallTO> expressionCalls; // TODO: get something from call filter
//
//            Set<AnatEntitySimilarity> anatEntitySimilarities = anatEntityService.getSimilarities(taxonId, speciesIds);
//            Set<DevStageSimilarity> devStageSimilarities = devStageService.getDevStageSimilarities(taxonId, speciesIds);
//            
//            Collection<ConditionFilter> conditionFilter; // TODO: construct
//            ConditionUtils conditionUtils; // TODO: construct (multi-species support ?)
//            Set<ExpressionCall> propCalls = callService.propagateExpressionTOs(expressionCalls, conditionFilter,
//                    conditionUtils);
//
//
//        }
//
//        return null;
//
//    }
//
//    public MultiSpeciesCall<?> groupCalls(Set<AnatEntitySimilarity> anatEntitySimilarities,
//            Set<DevStageSimilarity> devStageSimilarities, Set<ExpressionCall> calls) {
//        
//        
//        
//    }
//
//}
