package org.bgee.controller;


import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.Service;
import org.bgee.model.ServiceFactory;
import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.anatdev.DevStage;
import org.bgee.model.expressiondata.Call.ExpressionCall;
import org.bgee.model.expressiondata.CallData.ExpressionCallData;
import org.bgee.model.expressiondata.CallFilter.ExpressionCallFilter;
import org.bgee.model.expressiondata.CallService;
import org.bgee.model.expressiondata.baseelements.CallType.Expression;
import org.bgee.model.expressiondata.baseelements.DataPropagation;
import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.model.gene.Gene;
import org.bgee.model.gene.GeneFilter;
import org.bgee.view.GeneDisplay;
import org.bgee.view.ViewFactory;

/**
 * 
 * @author Philippe Moret
 * @version Bgee 13, Nov.
 * @since   Bgee 13, Nov.
 */
public class CommandGene extends CommandParent {

    private final static Logger log = LogManager.getLogger(CommandGene.class.getName());

	public CommandGene(HttpServletResponse response, RequestParameters requestParameters, BgeeProperties prop,
	        ViewFactory viewFactory, ServiceFactory serviceFactory) {
		super(response, requestParameters, prop, viewFactory, serviceFactory);
	}

	@Override
	public void processRequest() throws Exception {
		log.entry();
		GeneDisplay display = viewFactory.getGeneDisplay();
		String geneId = requestParameters.getGeneId();
		if (geneId == null) {
			display.displayGenePage();
		} else {
			Gene gene = getGene(geneId);
			List<ExpressionCall> calls = getExpressions(gene);
			log.info("Expressions:" + calls.size()+" "+calls);
			display.displayGene(gene, calls, getEntitiesForCalls(calls), getDevStagesForCalls(calls));
		}
		log.exit();
	}

	private Gene getGene(String geneId) {
		Gene gene = serviceFactory.getGeneService().loadGeneById(geneId);
		return gene;
	}
	
	private Map<String, AnatEntity> getEntitiesForCalls(Collection<ExpressionCall> calls) {
		log.entry(calls);
		return log.exit(serviceFactory.getAnatEntityService()
		.loadAnatEntitiesByIds(calls.stream().map(c -> c.getCondition().getAnatEntityId()).collect(Collectors.toSet()))
		.collect(Collectors.toMap(AnatEntity::getId, Function.identity())));
	}

	private Map<String, DevStage> getDevStagesForCalls(Collection<ExpressionCall> calls) {
		log.entry(calls);
		return log.exit(serviceFactory.getDevStageService()
		.loadDevStagesByIds(calls.stream().map(c -> c.getCondition().getDevStageId()).collect(Collectors.toSet())).stream()
		.collect(Collectors.toMap(DevStage::getId, Function.identity())));
	}
	
	private List<ExpressionCall> getExpressions(Gene gene) {
		 LinkedHashMap<CallService.OrderingAttribute, Service.Direction> serviceOrdering = 
	                new LinkedHashMap<>();
	        serviceOrdering.put(CallService.OrderingAttribute.RANK, Service.Direction.ASC);
	        
		CallService service = serviceFactory.getCallService();
		return log.exit(service.loadExpressionCalls(gene.getSpeciesId(), 
                new ExpressionCallFilter(new GeneFilter(gene.getId()), null, new DataPropagation(), 
                        Arrays.asList(new ExpressionCallData(Expression.EXPRESSED, DataType.RNA_SEQ),
                        		new ExpressionCallData(Expression.EXPRESSED, DataType.AFFYMETRIX),
                        		new ExpressionCallData(Expression.EXPRESSED, DataType.EST))), 
                EnumSet.of(CallService.Attribute.GENE_ID, CallService.Attribute.ANAT_ENTITY_ID, 
                        CallService.Attribute.DEV_STAGE_ID, CallService.Attribute.CALL_DATA, 
                        CallService.Attribute.GLOBAL_DATA_QUALITY), 
                serviceOrdering)
                .collect(Collectors.toList()));
	}
	
}
