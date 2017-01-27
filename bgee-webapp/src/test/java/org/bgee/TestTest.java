package org.bgee;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.Service;
import org.bgee.model.ServiceFactory;
import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.expressiondata.CallService;
import org.bgee.model.expressiondata.ConditionFilter;
import org.bgee.model.expressiondata.ConditionUtils;
import org.bgee.model.expressiondata.Call.ExpressionCall;
import org.bgee.model.expressiondata.CallData.ExpressionCallData;
import org.bgee.model.expressiondata.CallFilter.ExpressionCallFilter;
import org.bgee.model.expressiondata.baseelements.DataPropagation;
import org.bgee.model.species.Species;
import org.bgee.model.expressiondata.baseelements.CallType.Expression;
import org.junit.Test;

public class TestTest extends TestAncestor{
	
	private final static Logger log = 
            LogManager.getLogger(TestTest.class.getName());

	@Override
	protected Logger getLogger() {
		return log;
	}
	
//	@Test
//	public void testGetAnatEntity(){
//		ServiceFactory factory = new ServiceFactory();
//		log.debug(factory.getAnatEntityService().loadAnatEntityById("UBERON:0000178"));
//	}

	@Test
	public void testGetSpecies(){
		ServiceFactory factory = new ServiceFactory();
//		log.debug(factory.getGeneService().loadGeneById("ENSG00000244734"));
		log.debug(factory.getSpeciesService().loadSpeciesByIds(Collections.singleton("9606"), false));
		factory.close();
	}
	
	@Test
	public void testCallExpressions(){
		ServiceFactory serviceFactory = new ServiceFactory();
		//retrieve anatEntity
		AnatEntity anatEntity = serviceFactory.getAnatEntityService().loadAnatEntityById("UBERON:0000178");
		//retrieve species
		Species species = serviceFactory.getSpeciesService().loadSpeciesByIds(Collections.singleton("9606"), false).iterator().next();
		LinkedHashMap<CallService.OrderingAttribute, Service.Direction> serviceOrdering = 
                new LinkedHashMap<>();
    serviceOrdering.put(CallService.OrderingAttribute.GLOBAL_RANK, Service.Direction.ASC);
	CallService callService = serviceFactory.getCallService();
	Collection<ConditionFilter> condFilterCollection = new ArrayList<>();
	condFilterCollection.add(new ConditionFilter(Collections.singleton(anatEntity.getId()), null));
	List<ExpressionCall> expressionCalls = new ArrayList<ExpressionCall>();
	expressionCalls.addAll(
			callService.loadExpressionCalls(
			        species.getId(), 
	                new ExpressionCallFilter(null, condFilterCollection, new DataPropagation(), 
	                        Arrays.asList(new ExpressionCallData(Expression.EXPRESSED))), 
	                EnumSet.of(CallService.Attribute.GENE_ID, CallService.Attribute.ANAT_ENTITY_ID, 
	                        CallService.Attribute.DEV_STAGE_ID, CallService.Attribute.GLOBAL_RANK), 
	                serviceOrdering)
	            .collect(Collectors.toList()));
	log.debug("Expression Call number "+expressionCalls.size());
	if (expressionCalls.isEmpty()) {
        log.debug("No calls for anatomical entity {}", anatEntity.getId());
    }
 	ConditionUtils conditionUtils = new ConditionUtils(species.getId(), 
 			expressionCalls.stream().map(ExpressionCall::getCondition).collect(Collectors.toSet()), 
        serviceFactory);
 	//sort by global rank
 	Collections.sort(expressionCalls, new ExpressionCall.RankComparator(conditionUtils));
    log.debug("start uniqueCalls");
    Set<String> redundantGeneIds = new HashSet<>();
    List<ExpressionCall> uniqueExprCalls = new ArrayList<>();
    for(ExpressionCall call:expressionCalls){
    	if(!redundantGeneIds.contains(call.getGeneId())){
    		redundantGeneIds.add(call.getGeneId());
    		uniqueExprCalls.add(call);
    	}
    }
    
    log.debug("finish uniqueCalls");
    log.debug("redundant expression calls number "+uniqueExprCalls.size());
//    Collections.sort(expressionCalls, new ExpressionCall.RankComparator(conditionUtils));
	log.debug("Distinct expression calls "+uniqueExprCalls.stream().distinct().map(ExpressionCall::getGlobalMeanRank).collect(Collectors.toList()));
	serviceFactory.close();
	}
}
