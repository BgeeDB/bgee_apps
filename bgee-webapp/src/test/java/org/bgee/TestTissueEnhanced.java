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
import org.bgee.model.expressiondata.baseelements.DataPropagation.PropagationState;
import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.model.expressiondata.baseelements.CallType.Expression;
import org.bgee.model.gene.Gene;
import org.bgee.model.gene.GeneFilter;
import org.bgee.model.ontology.Ontology;
import org.bgee.model.ontology.Ontology.RelationType;
import org.bgee.model.species.Species;
import org.junit.Test;

public class TestTissueEnhanced extends TestAncestor{
	
	private final static Logger log = LogManager.getLogger(TestTissueEnhanced.class.getName());
	
	private final static String FILE_PATH = "src/test/data/";
	private final static DataType DATATYPE = null;
	private final static int MAX_NUMBER_GROUP_SPECIFIC=7;
	private final static boolean USE_ONTOLOGY_STRUCTURE_FOR_GROUP = false;
	
	@Override
	protected Logger getLogger() {
		return log;
	}
	
	@Test
	public void testGetSpecies() {
		ServiceFactory factory = new ServiceFactory();
		Set<Species> speciesSet = factory.getSpeciesService().loadSpeciesInDataGroups(true);
		for (Species species : speciesSet) {
			log.debug(species.getId() + "\t" + species.getName());
		}
		factory.close();
	}
	
	@Test
	public void testTissueEnhanced() throws PageNotFoundException, IOException{
		Instant startTime = Instant.now();
		String speciesID = "10090";
		Set<String> anatEntitiesIDs = new HashSet<String>(Collections.singleton("UBERON:0000061"));
		ServiceFactory serviceFactory = new ServiceFactory();
		Species species = serviceFactory.getSpeciesService()
				.loadSpeciesByIds(Collections.singleton(speciesID), false).iterator().next();
		if (species == null) {
			serviceFactory.close();
			throw log.throwing(new PageNotFoundException("No species corresponding to " + speciesID));
		}
		for(String anatEntityID:anatEntitiesIDs){	
			
			//an ontology containing parents and descendants terms linked by IS_A or PART_OF relationships
//			Ontology<AnatEntity> linkedElementsOntology = serviceFactory.getOntologyService().getAnatEntityOntology(new ArrayList<String>(Collections.singleton(speciesID)), new ArrayList<String>(Collections.singleton(anatEntityID)), new ArrayList<RelationType>(Collections.singleton(RelationType.ISA_PARTOF)), true, true, serviceFactory.getAnatEntityService());
			//retrieve anatEntity object
			AnatEntity anatEntity = serviceFactory.getAnatEntityService().loadAnatEntityById(anatEntityID);
			if (anatEntity == null) {
				serviceFactory.close();
				throw log.throwing(new PageNotFoundException("No anatEntity corresponding to " + anatEntityID));
			}
			Map<String, Gene> genes = returnGenesForOneAnatEntity(anatEntityID, speciesID, serviceFactory);
			List<String> tissueEnhanced = new ArrayList<>();
			genes.values().stream().forEach(gene -> {
				Map<String, ExpressionCall> callsByAnatEntityId = returnAnatEntitesForOneGene(serviceFactory.getCallService(), gene.getId(), speciesID);
				List<ExpressionCall> calls = new ArrayList<>(callsByAnatEntityId.values());
				ExpressionCall anatEntityCall = callsByAnatEntityId.get(anatEntityID);
				if(anatEntityCall!= null & calls!=null&&calls.size()>1){
					tissueEnhanced.add(this.returnTissueEnhanced(calls, anatEntityCall, gene));
				}
			});	
			writeOutputTissueSpe("TISSUEENHANCED_", tissueEnhanced, species.getName(), anatEntity.getName(),true);
		}
		serviceFactory.close();
		
		Instant endTime = Instant.now();
		log.debug("execution time : "+Duration.between(startTime, endTime).getSeconds());
	}
	
	@Test
	public void testGroupSpe() throws PageNotFoundException, IOException{
		Instant startTime = Instant.now();
		String speciesID = "10090";
		Set<String> anatEntitiesIDs = new HashSet<String>(Collections.singleton("UBERON:0000061"));
		ServiceFactory serviceFactory = new ServiceFactory();
		Species species = serviceFactory.getSpeciesService()
				.loadSpeciesByIds(Collections.singleton(speciesID), false).iterator().next();
		if (species == null) {
			serviceFactory.close();
			throw log.throwing(new PageNotFoundException("No species corresponding to " + speciesID));
		}
		for(String anatEntityID:anatEntitiesIDs){	
			
			//an ontology containing parents and descendants terms linked by IS_A or PART_OF relationships
			Ontology<AnatEntity> linkedElementsOntology = serviceFactory.getOntologyService().getAnatEntityOntology(new ArrayList<String>(Collections.singleton(speciesID)), new ArrayList<String>(Collections.singleton(anatEntityID)), new ArrayList<RelationType>(Collections.singleton(RelationType.ISA_PARTOF)), true, true, serviceFactory.getAnatEntityService());
			//retrieve anatEntity object
			AnatEntity anatEntity = serviceFactory.getAnatEntityService().loadAnatEntityById(anatEntityID);
			if (anatEntity == null) {
				serviceFactory.close();
				throw log.throwing(new PageNotFoundException("No anatEntity corresponding to " + anatEntityID));
			}
			Map<String, Gene> genes = returnGenesForOneAnatEntity(anatEntityID, speciesID, serviceFactory);
			List<String> groupSpe = new ArrayList<>();
			genes.values().parallelStream().forEach(gene -> {
				Map<String, ExpressionCall> callsByAnatEntityId = returnAnatEntitesForOneGene(serviceFactory.getCallService(), gene.getId(), speciesID);
				List<ExpressionCall> calls = new ArrayList<>(callsByAnatEntityId.values());
				ExpressionCall anatEntityCall = callsByAnatEntityId.get(anatEntityID);
				if(calls!=null&&calls.size()>1){
					calls = calls.stream()
							.filter(s -> linkedElementsOntology.getElement(s.getCondition().getAnatEntityId())==null||s.getCondition().getAnatEntityId().equals(anatEntityID))
							.collect(Collectors.toList());
					if(calls.size()>1){
						//start ratio detection at the first position of the calls list
						int currentAnatEntityPosition = 0;
						BigDecimal ratio = null;
						boolean findAnatEntity = false;
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
									if(ratio==null||currentRatio.compareTo(ratio)>0){
										ratio=currentRatio;
									}
								}
							}else{
								currentAnatEntityPosition = MAX_NUMBER_GROUP_SPECIFIC;
							}
							currentAnatEntityPosition++;
						}
						if (ratio != null){
							
						}
					}
				}
			});	
			writeOutputTissueSpe("GROUPSPE_", groupSpe, species.getName(), anatEntity.getName(),true);
		}
		serviceFactory.close();
		
		Instant endTime = Instant.now();
		log.debug("execution time : "+Duration.between(startTime, endTime).getSeconds());
	}
	
	
	private Map<String, Gene> returnGenesForOneAnatEntity(String anatEntityId, String speciesId, ServiceFactory serviceFactory) throws PageNotFoundException{
		CallService callService = serviceFactory.getCallService();
		//define condition filter (use anatEntityID)
		Collection<ConditionFilter> condFilterCollection = new ArrayList<>();
		condFilterCollection.add(new ConditionFilter(Collections.singleton(anatEntityId), null));
		List<ExpressionCall> expressionCallWithBestRank = null;
		expressionCallWithBestRank = new ArrayList<ExpressionCall>(
				retrieveExprCallsForAnatEntities(callService, speciesId, condFilterCollection)
				//filter on best ranked expression call for each gene. Don't take into account development stage
				.collect(Collectors.toMap(p -> p.getGeneId(), p -> p, (v1, v2) -> v1, LinkedHashMap::new))
				.values());
		log.debug("Size of map with only best ranked expression call for each gene : "+ expressionCallWithBestRank.size());
		Map<String,Gene> genes = serviceFactory.getGeneService().loadGenesByIdsAndSpeciesIds(
				expressionCallWithBestRank.stream().map(p -> p.getGeneId()).collect(Collectors.toList()),
				Collections.singletonList(speciesId)).stream().collect(Collectors.toMap(p -> p.getId(), p -> p));
		if (genes == null) {
			serviceFactory.close();
			throw log.throwing(new PageNotFoundException("No genes corresponding to " + anatEntityId));
		}
		return genes;
	}
	
	
	private Map<String, ExpressionCall> returnAnatEntitesForOneGene(CallService callService, String geneId, String speciesId){
		LinkedHashMap<CallService.OrderingAttribute, Service.Direction> serviceOrdering = new LinkedHashMap<>();
		serviceOrdering.put(CallService.OrderingAttribute.GLOBAL_RANK, Service.Direction.ASC);
		return callService
				.loadExpressionCalls(speciesId,
						new ExpressionCallFilter(new GeneFilter(geneId), null, new DataPropagation(),
								Arrays.asList(new ExpressionCallData(Expression.EXPRESSED))),
						EnumSet.of(CallService.Attribute.GENE_ID, CallService.Attribute.ANAT_ENTITY_ID,
								CallService.Attribute.DEV_STAGE_ID, CallService.Attribute.GLOBAL_DATA_QUALITY, CallService.Attribute.GLOBAL_RANK),
						serviceOrdering)
				// return best ranked expression call for each anat entity. Don't take into account devStage
				.collect(Collectors.toMap(p -> p.getCondition().getAnatEntityId(), p -> p, (v1, v2) -> v1,
						LinkedHashMap::new));
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
	
	private String returnTissueEnhanced(List<ExpressionCall> calls, ExpressionCall anatEntityCall, Gene gene){
		BigDecimal average = calls
	            .stream()
	            .map(s -> s.getGlobalMeanRank())
	            .reduce(BigDecimal.ZERO, BigDecimal::add).divide(new BigDecimal(calls.size()),1, RoundingMode.HALF_UP);
		System.out.println(anatEntityCall.getGlobalMeanRank());
		log.debug(" mean : "+average+", anat. entity rank : "+anatEntityCall.getGlobalMeanRank()+" for "+gene.getId());
		BigDecimal anatEntityRank = anatEntityCall.getGlobalMeanRank();
		BigDecimal rate = average.divide(anatEntityRank,1, RoundingMode.HALF_UP);
		return gene.getId() + "\t" + gene.getName() + "\t" + anatEntityCall.getFormattedGlobalMeanRank() + "\t" + rate.toString() + "\t" + gene.getDescription();
	}
	
	private void writeOutputTissueSpe(String filePrefix, List<String> output, String speciesName, String anatEntityName, boolean tissueSpeColumn) throws IOException{
		if(!output.isEmpty()){
			String columnsNames = "gene_ID" + "\t" + "gene_name" + "\t" + "rank_value" + "\t";
			columnsNames += tissueSpeColumn ? "tissue spe"+ "\t" + "gene_definition" : "gene_definition";
			output.add(0,columnsNames);
			String fileName = FILE_PATH + filePrefix + "_" + speciesName.replace(" ", "_") + "_"
					+ anatEntityName.replace(" ", "_");//+"_"+CLUSTER_THRESHOLD.toString()+ "_"+ANAT_ENTITY_PROPAGATION+"_"+DEV_STAGE_PROPAGATION;
			if(DATATYPE!=null){
				fileName +="_"+DATATYPE.toString();
			}
			fileName+=".tsv";
			try (BufferedWriter writer = new BufferedWriter(new FileWriter(new File(fileName)))) {
				writer.write(String.join("\n", output));
			} catch (IOException e){
				throw log.throwing(new IOException("Can't write output file "+fileName+" \n"+e.getMessage()));
			}			
		}
	}
	
	
	
}
