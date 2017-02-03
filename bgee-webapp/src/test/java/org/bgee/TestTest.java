package org.bgee;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.Service;
import org.bgee.model.ServiceFactory;
import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.anatdev.DevStage;
import org.bgee.model.expressiondata.CallService;
import org.bgee.model.expressiondata.Condition;
import org.bgee.model.expressiondata.ConditionFilter;
import org.bgee.model.expressiondata.ConditionUtils;
import org.bgee.model.expressiondata.Call.ExpressionCall;
import org.bgee.model.expressiondata.CallData.ExpressionCallData;
import org.bgee.model.expressiondata.CallFilter.ExpressionCallFilter;
import org.bgee.model.expressiondata.baseelements.DataPropagation;
import org.bgee.model.gene.Gene;
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
	
	@Test
	public void testGetAnatEntity(){
		ServiceFactory factory = new ServiceFactory();
		log.debug(factory.getAnatEntityService().loadAnatEntityById("UBERON:0000178"));
	}

	@Test
	public void testGetSpecies(){
		ServiceFactory factory = new ServiceFactory();
//		log.debug(factory.getGeneService().loadGeneById("ENSG00000244734"));
		Set<Species> speciesSet = factory.getSpeciesService().loadSpeciesInDataGroups(true);//loadSpeciesByIds(Collections.singleton("9606"), false));
		for(Species species:speciesSet){
			System.out.println(species.getId()+"\t"+species.getName());
		}
	}
	
	@Test
	public void testCallExpressions() {
//		List <String> speciesIDs = Collections.singletonList("10090");
//		List <String> anatEntitiesIDs = Collections.singletonList("UBERON:0000948");
		List<String> speciesIDs = Arrays.asList("9606","10090");//,"8364","7955","9598","7227","6239");
		List<String> anatEntitiesIDs = Arrays.asList("UBERON:0000955","UBERON:0002037","UBERON:0000451","UBERON:0002021","UBERON:0001954","UBERON:0001898","UBERON:2007003");//,"UBERON:0002107","UBERON:0000948","UBERON:0000178");
		final String filePath = "src/test/resources/";
		ServiceFactory serviceFactory = new ServiceFactory();
		for(String speciesID:speciesIDs){
			for(String anatEntityID:anatEntitiesIDs){
				log.debug("species_ID : "+speciesID+" & anatEntity_ID : "+anatEntityID);
				// retrieve anatEntity
				AnatEntity anatEntity = serviceFactory.getAnatEntityService().loadAnatEntityById(anatEntityID);
				// retrieve species
				Species species = serviceFactory.getSpeciesService().loadSpeciesByIds(Collections.singleton(speciesID), false)
						.iterator().next();
				LinkedHashMap<CallService.OrderingAttribute, Service.Direction> serviceOrdering = new LinkedHashMap<>();
				serviceOrdering.put(CallService.OrderingAttribute.GLOBAL_RANK, Service.Direction.ASC);
				CallService callService = serviceFactory.getCallService();
				Collection<ConditionFilter> condFilterCollection = new ArrayList<>();
				condFilterCollection.add(new ConditionFilter(Collections.singleton(anatEntity.getId()), null));
				List<ExpressionCall> expressionCalls = callService
						.loadExpressionCalls(species.getId(),
								new ExpressionCallFilter(null, condFilterCollection, new DataPropagation(),
										Arrays.asList(new ExpressionCallData(Expression.EXPRESSED))),
								EnumSet.of(CallService.Attribute.GENE_ID, CallService.Attribute.ANAT_ENTITY_ID,
										CallService.Attribute.DEV_STAGE_ID, CallService.Attribute.GLOBAL_RANK),
								serviceOrdering)
						.collect(Collectors.toList());
				log.debug("Expression Call number " + expressionCalls.size());
				if (expressionCalls.isEmpty()) {
					log.error("No calls for anatomical entity {} for species {}", anatEntity.getId(), species.getId());
				}else{
					ConditionUtils conditionUtils = new ConditionUtils(species.getId(),
							expressionCalls.stream().map(ExpressionCall::getCondition).collect(Collectors.toSet()), serviceFactory);
					// sort by global rank
//					Collections.sort(expressionCalls, new ExpressionCall.RankComparator(conditionUtils));
					log.debug("start uniqueCalls");
					Set<String> redundantGeneIds = new HashSet<>();
					List<ExpressionCall> uniqueExprCalls = new ArrayList<>();
					for (ExpressionCall call : expressionCalls) {
						if (!redundantGeneIds.contains(call.getGeneId())) {
							redundantGeneIds.add(call.getGeneId());
							uniqueExprCalls.add(call);
						}
					}
					List<Gene> genes = serviceFactory.getGeneService().loadGenesByIdsAndSpeciesIds(
							redundantGeneIds.stream().collect(Collectors.toList()), Collections.singletonList(speciesID));
					HashMap<String, Gene> genesByIds = new HashMap<>();
					for (Gene gene : genes) {
						genesByIds.put(gene.getId(), gene);
					}
					log.debug("finish uniqueCalls");
					log.debug("redundant expression calls number " + uniqueExprCalls.size());
					List<String> devStagesIds = uniqueExprCalls.stream().map(ExpressionCall::getCondition)
							.map(Condition::getDevStageId).collect(Collectors.toList());
					List<DevStage> devStages = serviceFactory.getDevStageService()
							.loadDevStages(Collections.singletonList(speciesID), false, devStagesIds).collect(Collectors.toList());
					HashMap<String, String> devStageByIds = new HashMap<>();
					for (DevStage stage : devStages) {
						devStageByIds.put(stage.getId(), stage.getName());
					}
					//format output
					String output = "gene_ID" + "\t" + "gene_name" + "\t" + "rank_value" + "\t" + "gene_definition\n";
					for (ExpressionCall call : uniqueExprCalls) {
						// System.out.println(call.getGeneId()+"\t"+genesByIds.get(call.getGeneId())+"\t"+devStageByIds.get(call.getCondition().getDevStageId())+"\t"+call.getFormattedGlobalMeanRank());
						output += call.getGeneId() + "\t" + genesByIds.get(call.getGeneId()).getName() + "\t"
								+ call.getFormattedGlobalMeanRank() + "\t" + genesByIds.get(call.getGeneId()).getDescription()
								+ "\n";
					}
					//write output file
					BufferedWriter writer = null;
					try {
						File outputFile = new File(filePath + species.getName().replace(" ", "_") + "_" + anatEntity.getName().replace(" ", "_") + ".tsv");
						writer = new BufferedWriter(new FileWriter(outputFile));
						writer.write(output);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				
			}
		}
		serviceFactory.close();
	}
}
