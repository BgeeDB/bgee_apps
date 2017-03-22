package org.bgee;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.exception.PageNotFoundException;
import org.bgee.model.Service;
import org.bgee.model.ServiceFactory;
import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.expressiondata.CallService;
import org.bgee.model.expressiondata.ConditionFilter;
import org.bgee.model.expressiondata.Call.ExpressionCall;
import org.bgee.model.expressiondata.CallData.ExpressionCallData;
import org.bgee.model.expressiondata.CallFilter.ExpressionCallFilter;
import org.bgee.model.expressiondata.baseelements.DataPropagation;
import org.bgee.model.expressiondata.baseelements.DataQuality;
import org.bgee.model.expressiondata.baseelements.CallType.Expression;
import org.bgee.model.gene.Gene;
import org.bgee.model.gene.GeneFilter;
import org.bgee.model.ontology.Ontology;
import org.bgee.model.ontology.Ontology.RelationType;
import org.bgee.model.species.Species;
import org.junit.Test;

//create different output files potentially useful to show organ specificity.
//these different files are based on the Protein Atlas approach (http://www.proteinatlas.org/humanproteome/tissue+specific)
public class TestHumanAtlasApproach {

	private final static Logger log = LogManager.getLogger(TestHumanAtlasApproach.class.getName());
	private final static int MAX_NUMBER_GROUP_SPECIFIC=7;
	private final static int HOUSEKEEPING_GENE_NUMBER = 170;
	private final static boolean HIGH_QUALITY = false;
	private final static boolean USE_ONTOLOGY_STRUCTURE_FOR_GROUP = false;
	private final static Double CLUSTER_THRESHOLD=5.0;
	private final static String FILE_PATH = "src/test/data/";

		@Test
		public void testTissueSpecificityBasedOnRankComparison() throws PageNotFoundException, IOException{
			Instant startTime = Instant.now();
			String speciesID = "9606";
//			Set<String> anatEntitiesIDs = Collections.singleton("UBERON:0000955");//,"UBERON:0000473","UBERON:0000956","UBERON:0000473","UBERON:0003889","UBERON:0002107","UBERON:0001987","UBERON:0001132","UBERON:0002108","UBERON:0002106","UBERON:0000473","UBERON:0014895","UBERON:0000014","UBERON:0001301","UBERON:0002113","UBERON:0002371","UBERON:0000029","UBERON:0001043","UBERON:0002369","UBERON:0002046","UBERON:0001154","UBERON:0002349","UBERON:0002372","UBERON:0002048","UBERON:0001052","UBERON:0002367","UBERON:0001013","UBERON:0001155","UBERON:0000945","UBERON:0000002","UBERON:0002110","UBERON:0000310","UBERON:0000998","UBERON:0000992","UBERON:0001295","UBERON:0001135","UBERON:0001044","UBERON:0001264","UBERON:0001255"));
			//test housekeeping genes with high level anat entity element anatomical structure (UBERON:0000061)
			Set<String> anatEntitiesIDs = new HashSet<String>(Collections.singleton("UBERON:0000955"));
			for(String anatEntityID:anatEntitiesIDs){	
				ServiceFactory serviceFactory = new ServiceFactory();
				//an ontology containing parents and descendants terms linked by IS_A or PART_OF relationships
				Ontology<AnatEntity> linkedElementsOntology = serviceFactory.getOntologyService().getAnatEntityOntology(new ArrayList<String>(Collections.singleton(speciesID)), new ArrayList<String>(Collections.singleton(anatEntityID)), new ArrayList<RelationType>(Collections.singleton(RelationType.ISA_PARTOF)), true, true, serviceFactory.getAnatEntityService());
				//retrieve anatEntity object
				AnatEntity anatEntity = serviceFactory.getAnatEntityService().loadAnatEntityById(anatEntityID);
				if (anatEntity == null) {
					serviceFactory.close();
					throw log.throwing(new PageNotFoundException("No anatEntity corresponding to " + anatEntityID));
				}
				//retrieve species object
				Species species = serviceFactory.getSpeciesService()
						.loadSpeciesByIds(Collections.singleton(speciesID), false).iterator().next();
				if (species == null) {
					serviceFactory.close();
					throw log.throwing(new PageNotFoundException("No species corresponding to " + speciesID));
				}
				CallService callService = serviceFactory.getCallService();
				//define condition filter (use anatEntityID)
				Collection<ConditionFilter> condFilterCollection = new ArrayList<>();
				condFilterCollection.add(new ConditionFilter(Collections.singleton(anatEntity.getId()), null));
				List<ExpressionCall> expressionCallWithBestRank = null;
				if(HIGH_QUALITY){
					expressionCallWithBestRank = new ArrayList<ExpressionCall>(
					retrieveExprCallsForAnatEntities(callService, species.getId(), condFilterCollection)
					.filter(s -> s.getSummaryQuality().compareTo(DataQuality.HIGH) == 0)
					//filter on best ranked expression call for each gene. Don't take into account development stage
					.collect(Collectors.toMap(p -> p.getGeneId(), p -> p, (v1, v2) -> v1, LinkedHashMap::new))
					.values());
				}else{
					expressionCallWithBestRank = new ArrayList<ExpressionCall>(
							retrieveExprCallsForAnatEntities(callService, species.getId(), condFilterCollection)
							//filter on best ranked expression call for each gene. Don't take into account development stage
							.collect(Collectors.toMap(p -> p.getGeneId(), p -> p, (v1, v2) -> v1, LinkedHashMap::new))
							.values());
				}
				log.debug("Size of map with only best ranked expression call for each gene : "+ expressionCallWithBestRank.size());
				Map<String,Gene> genes = serviceFactory.getGeneService().loadGenesByIdsAndSpeciesIds(
						expressionCallWithBestRank.stream().map(p -> p.getGeneId()).collect(Collectors.toList()),
						Collections.singletonList(speciesID)).stream().collect(Collectors.toMap(p -> p.getId(), p -> p));
				if (genes == null) {
					serviceFactory.close();
					throw log.throwing(new PageNotFoundException("No species corresponding to " + speciesID));
				}
				List<String> tissueEnhanced = new ArrayList<>();
				List<String> housekeeper = new ArrayList<>();
				List<String> undefined = new ArrayList<>();
				List<String> grouppecific = new ArrayList<>();
				List<String> tissuepecific = new ArrayList<>();
				genes.values().parallelStream().forEach(gene -> {
					LinkedHashMap<CallService.OrderingAttribute, Service.Direction> serviceOrdering = new LinkedHashMap<>();
					serviceOrdering.put(CallService.OrderingAttribute.GLOBAL_RANK, Service.Direction.ASC);
					// list of expression call, ordered by anat entity
					LinkedHashMap<String, ExpressionCall> callsByAnatEntityId = null;
					if(HIGH_QUALITY){
					callsByAnatEntityId = callService
							.loadExpressionCalls(gene.getSpeciesId(),
									new ExpressionCallFilter(new GeneFilter(gene.getId()), null, new DataPropagation(),
											Arrays.asList(new ExpressionCallData(Expression.EXPRESSED))),
									EnumSet.of(CallService.Attribute.GENE_ID, CallService.Attribute.ANAT_ENTITY_ID,
											CallService.Attribute.DEV_STAGE_ID, CallService.Attribute.GLOBAL_DATA_QUALITY, CallService.Attribute.GLOBAL_RANK),
									serviceOrdering)
							.filter(s -> s.getSummaryQuality().compareTo(DataQuality.HIGH) == 0)
							// return best ranked expression call for each anat entity. Don't take into account devStage
							.collect(Collectors.toMap(p -> p.getCondition().getAnatEntityId(), p -> p, (v1, v2) -> v1,
									LinkedHashMap::new));
					}else{
						callsByAnatEntityId = callService
								.loadExpressionCalls(gene.getSpeciesId(),
										new ExpressionCallFilter(new GeneFilter(gene.getId()), null, new DataPropagation(),
												Arrays.asList(new ExpressionCallData(Expression.EXPRESSED))),
										EnumSet.of(CallService.Attribute.GENE_ID, CallService.Attribute.ANAT_ENTITY_ID,
												CallService.Attribute.DEV_STAGE_ID, CallService.Attribute.GLOBAL_DATA_QUALITY, CallService.Attribute.GLOBAL_RANK),
										serviceOrdering)
								// return best ranked expression call for each anat entity. Don't take into account devStage
								.collect(Collectors.toMap(p -> p.getCondition().getAnatEntityId(), p -> p, (v1, v2) -> v1,
										LinkedHashMap::new));
					}
					log.debug(callsByAnatEntityId.get(anatEntityID));
					List<ExpressionCall> calls = new ArrayList<>(callsByAnatEntityId.values());
					ExpressionCall anatEntityCall = callsByAnatEntityId.get(anatEntityID);
					if(calls!=null&&!calls.isEmpty()){//&&anatEntityCall!=null){
						//if we only have one anat. entity with gene expression, we considerate it tissue specific with a ratio of Integer.MAX_VALUE
						if(calls.size()==1){
							tissuepecific.add(anatEntityCall.getGeneId() + "\t" + gene.getName() + "\t" + anatEntityCall.getFormattedGlobalMeanRank() + "\t" + Integer.MAX_VALUE
									+ "\t" + gene.getDescription() + "\t" +anatEntityCall.getSummaryQuality());
						}else{
							boolean findCategory = false;
							//remove all elements coming from the parent/descendant ontology of the selected anat. entity
							calls = calls.stream()
							.filter(s -> linkedElementsOntology.getElement(s.getCondition().getAnatEntityId())==null||s.getCondition().getAnatEntityId().equals(anatEntityID))
							.collect(Collectors.toList());
							ExpressionCall firstCall = calls.get(0);
							// search for tissue specific genes
							if(calls.size()==1){
								tissuepecific.add(firstCall.getGeneId() + "\t" + gene.getName() + "\t" + firstCall.getFormattedGlobalMeanRank() + "\t" + Integer.MAX_VALUE
										+ "\t" + gene.getDescription()+ "\t" +anatEntityCall.getSummaryQuality());
							}else{
								log.debug("calls size : "+calls.size());
								BigDecimal ratio = null;
								boolean findAnatEntity = false;
								if(firstCall.equals(anatEntityCall)){
									findAnatEntity=true;
									ratio = calls.get(1).getGlobalMeanRank().divide(firstCall.getGlobalMeanRank(),1, RoundingMode.HALF_UP);
									if(ratio.compareTo(new BigDecimal(5))>0){
										log.debug("compare "+firstCall.getGeneId()+ " -> "+anatEntityCall.getGeneId()+" -> "+firstCall.getGlobalMeanRank());
										tissuepecific.add(firstCall.getGeneId() + "\t" + gene.getName() + "\t" + firstCall.getFormattedGlobalMeanRank() + "\t" + ratio
												+ "\t" + gene.getDescription()+ "\t" +firstCall.getSummaryQuality());
										findCategory = true;
										firstCall.getSummaryQuality();
									}
								}
								//search for group specific genes
								if(!findCategory){
									int currentAnatEntityPosition = 1;
									ratio = null;
									while(currentAnatEntityPosition< MAX_NUMBER_GROUP_SPECIFIC){
										ExpressionCall currentAnatEntity = calls.get(currentAnatEntityPosition);
										if(USE_ONTOLOGY_STRUCTURE_FOR_GROUP){
											final Ontology <AnatEntity> currentAnatEntityOntology = serviceFactory.getOntologyService().getAnatEntityOntology(new ArrayList<String>(Collections.singleton(speciesID)), new ArrayList<String>(Collections.singleton(currentAnatEntity.getCondition().getAnatEntityId())), new ArrayList<RelationType>(Collections.singleton(RelationType.ISA_PARTOF)), true, true, serviceFactory.getAnatEntityService());
											calls = calls.stream()
													.filter(s -> currentAnatEntityOntology.getElement(s.getCondition().getAnatEntityId())==null||s.getCondition().getAnatEntityId().equals(anatEntityID)||s.getCondition().getAnatEntityId().equals(currentAnatEntity.getCondition().getAnatEntityId()))
													.collect(Collectors.toList());
										}
										
										if(calls.size()>(currentAnatEntityPosition+1)){
											ExpressionCall nextAnatEntity= calls.get(currentAnatEntityPosition+1);
											if(!findAnatEntity){
												if(currentAnatEntity.equals(anatEntityCall)){
													findAnatEntity=true;
												}
											}
											if(findAnatEntity){
												BigDecimal currentRatio = nextAnatEntity.getGlobalMeanRank().divide(currentAnatEntity.getGlobalMeanRank(),1, RoundingMode.HALF_UP);
												if(currentRatio.compareTo(new BigDecimal(5))>=0&&(ratio==null||currentRatio.compareTo(ratio)>0)){
													ratio=currentRatio;
													findCategory = true;
												}
											}
										}else{
											if(ratio == null){ // if there is less than 7 anat. entities associated to this gene, this gene is tag as group specific.
												ratio = new BigDecimal(Integer.MAX_VALUE);
												findCategory = true;
												findAnatEntity=true;
												currentAnatEntityPosition = MAX_NUMBER_GROUP_SPECIFIC;
											}else{
												findCategory = true;
												currentAnatEntityPosition = MAX_NUMBER_GROUP_SPECIFIC;
											}
										}
										currentAnatEntityPosition++;
									}
									if(findCategory){
										grouppecific.add(gene.getId() + "\t" + gene.getName() + "\t" + anatEntityCall.getFormattedGlobalMeanRank() + "\t" + ratio
												+ "\t" + gene.getDescription()+ "\t" +anatEntityCall.getSummaryQuality());
									}
								}
								if(!findCategory){
									String outputString = testTissueEnhanced(calls, anatEntityCall, gene);
									
									if(calls.size()>HOUSEKEEPING_GENE_NUMBER&&calls.get(calls.size()/2).getGlobalMeanRank().compareTo(calls.get(0).getGlobalMeanRank().multiply(new BigDecimal(10))) < 0){
										housekeeper.add(gene.getId() + "\t" + gene.getName() + "\t" + anatEntityCall.getFormattedGlobalMeanRank() +  "\t" + gene.getDescription());
									}else if(outputString != null){
										tissueEnhanced.add(outputString);
									}else{
										undefined.add(gene.getId() + "\t" + gene.getName() + "\t" + anatEntityCall.getFormattedGlobalMeanRank() +  "\t" + gene.getDescription());
									}
								}
							}
						}
					}
				});
				writeOutputTissueSpe("TISSUESPE_", tissuepecific, species.getName(), anatEntity.getName(),true);
				writeOutputTissueSpe("GROUPSPE_", grouppecific, species.getName(), anatEntity.getName(),true);
				writeOutputTissueSpe("UNDEFINED_", undefined, species.getName(), anatEntity.getName(),false);
				writeOutputTissueSpe("TISSUEENHANCED_", tissueEnhanced, species.getName(), anatEntity.getName(),false);
				writeOutputTissueSpe("HOUSEKEEPER", housekeeper, species.getName(), anatEntity.getName(),false);
				serviceFactory.close();
				Instant endTime = Instant.now();
				log.debug("execution time : "+Duration.between(startTime, endTime).getSeconds());
			}
		}
		
		private void writeOutputTissueSpe(String filePrefix, List<String> output, String speciesName, String anatEntityName, boolean tissueSpeColumn) throws IOException{
			if(!output.isEmpty()){
				String columnsNames = "gene_ID" + "\t" + "gene_name" + "\t" + "rank_value" + "\t";
				columnsNames += tissueSpeColumn ? "tissue spe"+ "\t" + "gene_definition" : "gene_definition";
				output.add(0,columnsNames);
				String fileName = FILE_PATH + filePrefix + "_" + speciesName.replace(" ", "_") + "_"
						+ anatEntityName.replace(" ", "_");//+"_"+CLUSTER_THRESHOLD.toString()+ "_"+ANAT_ENTITY_PROPAGATION+"_"+DEV_STAGE_PROPAGATION;
				if(HIGH_QUALITY){
					fileName +="_"+"HIGH";
				}if(USE_ONTOLOGY_STRUCTURE_FOR_GROUP){
					fileName +="_"+"DES";
				}
				fileName+=".tsv";
				try (BufferedWriter writer = new BufferedWriter(new FileWriter(new File(fileName)))) {
					writer.write(String.join("\n", output));
				} catch (IOException e){
					throw log.throwing(new IOException("Can't write output file "+fileName+" \n"+e.getMessage()));
				}			
			}
		}

		private String testTissueEnhanced(List<ExpressionCall> calls, ExpressionCall anatEntityCall, Gene gene){
			BigDecimal average = calls
		            .stream()
		            .map(s -> s.getGlobalMeanRank())
		            .reduce(BigDecimal.ZERO, BigDecimal::add).divide(new BigDecimal(calls.size()),1, RoundingMode.HALF_UP);
			log.debug(" mean : "+average+", anat. entity rank : "+anatEntityCall.getGlobalMeanRank()+" for "+gene.getId());
			BigDecimal anatEntityRank = anatEntityCall.getGlobalMeanRank();
			if(average.compareTo(anatEntityRank.multiply(new BigDecimal(CLUSTER_THRESHOLD)))>0){
				return gene.getId() + "\t" + gene.getName() + "\t" + anatEntityCall.getFormattedGlobalMeanRank() + "\t" + gene.getDescription();
			}
			return null;
			
		}
		
		private Stream<ExpressionCall> retrieveExprCallsForAnatEntities(CallService callService, String speciesId, Collection<ConditionFilter> condFilterCollection){
			//define ordering approach
			LinkedHashMap<CallService.OrderingAttribute, Service.Direction> serviceOrdering = new LinkedHashMap<>();
			serviceOrdering.put(CallService.OrderingAttribute.GLOBAL_RANK, Service.Direction.ASC);
			return callService.loadExpressionCalls(speciesId,
					new ExpressionCallFilter(null, condFilterCollection, new DataPropagation(), 
							Arrays.asList(new ExpressionCallData(Expression.EXPRESSED))),
					EnumSet.of(CallService.Attribute.GENE_ID, CallService.Attribute.ANAT_ENTITY_ID,
							CallService.Attribute.DEV_STAGE_ID, CallService.Attribute.GLOBAL_DATA_QUALITY, CallService.Attribute.GLOBAL_RANK),
					serviceOrdering);
		}
		
}
