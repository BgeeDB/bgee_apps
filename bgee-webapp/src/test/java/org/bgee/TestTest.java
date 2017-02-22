package org.bgee;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.BgeeProperties;
import org.bgee.controller.exception.PageNotFoundException;
import org.bgee.model.Service;
import org.bgee.model.ServiceFactory;
import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.expressiondata.Call.ExpressionCall;
import org.bgee.model.expressiondata.Call.ExpressionCall.ClusteringMethod;
import org.bgee.model.expressiondata.CallData.ExpressionCallData;
import org.bgee.model.expressiondata.CallFilter.ExpressionCallFilter;
import org.bgee.model.expressiondata.CallService;
import org.bgee.model.expressiondata.ConditionFilter;
import org.bgee.model.expressiondata.baseelements.CallType.Expression;
import org.bgee.model.expressiondata.baseelements.DataPropagation;
import org.bgee.model.expressiondata.baseelements.DataPropagation.PropagationState;
import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.model.gene.Gene;
import org.bgee.model.gene.GeneFilter;
import org.bgee.model.ontology.Ontology;
import org.bgee.model.ontology.Ontology.RelationType;
import org.bgee.model.species.Species;
import org.junit.Test;

public class TestTest extends TestAncestor {

	private final static Logger log = LogManager.getLogger(TestTest.class.getName());
	private final static boolean FILTER_ON_CLUSTERING_RESULTS = false;
	private final static int CLUSTER_RANK_LIMIT = 0;
	private final static int LIMIT_NUMBER_OF_ANAT_ENTITIES = 10;
	private final static DataType DATATYPE = null;
	private final static PropagationState ANAT_ENTITY_PROPAGATION = PropagationState.SELF;
	private final static PropagationState DEV_STAGE_PROPAGATION = PropagationState.SELF;
	private final static String FILE_PATH = "src/test/data/";

	@Override
	protected Logger getLogger() {
		return log;
	}

	@Test
	public void testGetAnatEntity() {
		ServiceFactory factory = new ServiceFactory();
		log.debug(factory.getAnatEntityService().loadAnatEntityById("UBERON:0000178"));
		factory.close();
	}

	@Test
	public void testGetSpecies() {
		ServiceFactory factory = new ServiceFactory();
		// log.debug(factory.getGeneService().loadGeneById("ENSG00000244734"));
		Set<Species> speciesSet = factory.getSpeciesService().loadSpeciesInDataGroups(true);// loadSpeciesByIds(Collections.singleton("9606"),
																							// false));
		for (Species species : speciesSet) {
			System.out.println(species.getId() + "\t" + species.getName());
		}
		factory.close();
	}

	@Test
	public void testCallExpressions() throws PageNotFoundException, IOException {
		//init all parameters of anat entity retrieval
		Instant startTime = Instant.now();
		
		
		//species and anat. entities to retrieve
		List<String> speciesIDs = Collections.singletonList("9606");
		List<String> anatEntitiesIDs = Collections.singletonList("UBERON:0000955");
// 		List<String> speciesIDs = Arrays.asList("9606","10090");
//		List<String> anatEntitiesIDs =
//		Arrays.asList("UBERON:0000955","UBERON:0002037","UBERON:0000451","UBERON:0002021","UBERON:0001954","UBERON:0001898","UBERON:2007003");
//		List<String> speciesIDs =
//		Arrays.asList("9606","10090","8364","7955","9598","7227","6239");
//		List<String> anatEntitiesIDs =
//		Arrays.asList("UBERON:0000955","UBERON:0002037","UBERON:0002107","UBERON:0000948","UBERON:0000178");
		//loop over all species and anat. entities
		for (String speciesID : speciesIDs) {
			for (String anatEntityID : anatEntitiesIDs) {
				ServiceFactory serviceFactory = new ServiceFactory();
				final Ontology<AnatEntity> descendants = serviceFactory.getOntologyService().getAnatEntityOntology(new ArrayList<String>(Collections.singleton(speciesID)), new ArrayList<String>(Collections.singleton(anatEntityID)), new ArrayList<RelationType>(Collections.singleton(RelationType.ISA_PARTOF)), false, true, serviceFactory.getAnatEntityService());
				
				System.out.println("number of elements "+descendants.getElements().size());
//				if (descendants == null) {
//					serviceFactory.close();
//					throw log.throwing(new PageNotFoundException("No ontology of descendants corresponding to " + anatEntityID +" for species " +speciesID));
//				}
				log.debug("species_ID : " + speciesID + " & anatEntity_ID : " + anatEntityID);
				//retrieve anatEntity object
				AnatEntity anatEntity = serviceFactory.getAnatEntityService().loadAnatEntityById(anatEntityID);
				if (anatEntity == null) {
					serviceFactory.close();
					throw log.throwing(new PageNotFoundException("No anatEntity corresponding to " + anatEntityID));
				}
				//return species object
				Species species = serviceFactory.getSpeciesService()
						.loadSpeciesByIds(Collections.singleton(speciesID), false).iterator().next();
				if (species == null) {
					serviceFactory.close();
					throw log.throwing(new PageNotFoundException("No species corresponding to " + speciesID));
				}
				//define ordering approach
				LinkedHashMap<CallService.OrderingAttribute, Service.Direction> serviceOrdering = new LinkedHashMap<>();
				serviceOrdering.put(CallService.OrderingAttribute.GLOBAL_RANK, Service.Direction.ASC);
				CallService callService = serviceFactory.getCallService();
				//define condition filter (use anatEntityID)
				Collection<ConditionFilter> condFilterCollection = new ArrayList<>();
				condFilterCollection.add(new ConditionFilter(Collections.singleton(anatEntity.getId()), null));
				// Allow to select propagation for anat. entities and development stage
				// list of best ranked ExpressionCall for each gene
				List<ExpressionCall> expressionCallWithBestRank = new ArrayList<>(callService
						.loadExpressionCalls(species.getId(),
								new ExpressionCallFilter(null, condFilterCollection, new DataPropagation(ANAT_ENTITY_PROPAGATION,DEV_STAGE_PROPAGATION),
										Arrays.asList(new ExpressionCallData(Expression.EXPRESSED, DATATYPE))),
								EnumSet.of(CallService.Attribute.GENE_ID, CallService.Attribute.ANAT_ENTITY_ID,
										CallService.Attribute.DEV_STAGE_ID, CallService.Attribute.GLOBAL_RANK),
								serviceOrdering)
						// collect data into a map in order to keep best ranked ExpressionCall for each gene
						.collect(Collectors.toMap(p -> p.getGeneId(), p -> p, (v1, v2) -> v1, LinkedHashMap::new))
						.values());
				log.debug("expression call Map with only first expression call size : "
						+ expressionCallWithBestRank.size());

				List<Gene> genes = serviceFactory.getGeneService().loadGenesByIdsAndSpeciesIds(
						expressionCallWithBestRank.stream().map(p -> p.getGeneId()).collect(Collectors.toList()),
						Collections.singletonList(speciesID));
				log.debug("finish uniqueCalls");
				log.debug("redundant expression calls number " + expressionCallWithBestRank.size());

				// format output
				String output = "gene_ID" + "\t" + "gene_name" + "\t" + "rank_value" + "\t" + "gene_definition\n";
				int outputInitialLength = output.length();
				for (ExpressionCall call : expressionCallWithBestRank) {
					Gene gene = genes.stream().filter(x -> call.getGeneId().equals(x.getId()))
							.collect(Collectors.toList()).get(0);
					//filter or not on anat entity expression for the gene (use or not cluster of gene expression)
					if(!FILTER_ON_CLUSTERING_RESULTS){
						output += call.getGeneId() + "\t" + gene.getName() + "\t" + call.getFormattedGlobalMeanRank()
								+ "\t" + gene.getDescription() + "\n";
					}else if(filterOnClusters(gene, anatEntityID, callService,descendants)){
						output += call.getGeneId() + "\t" + gene.getName() + "\t" + call.getFormattedGlobalMeanRank()
						+ "\t" + gene.getDescription() + "\n";
					}
				}
				// write output file
				if(output.length() > outputInitialLength){
					String fileName = FILE_PATH + species.getName().replace(" ", "_") + "_"
							+ anatEntity.getName().replace(" ", "_") + "_"+ANAT_ENTITY_PROPAGATION+"_"+DEV_STAGE_PROPAGATION;
					if(DATATYPE!=null){
						fileName +="_"+DATATYPE.toString();
					}
					if(FILTER_ON_CLUSTERING_RESULTS){
						fileName +="_"+CLUSTER_RANK_LIMIT+"_"+LIMIT_NUMBER_OF_ANAT_ENTITIES+"_DES";
					}
					fileName+=".tsv";
					writeOutputFile(output,fileName);
					
				}
				serviceFactory.close();
			}

		}
		Instant endTime = Instant.now();
		log.debug("execution time : "+Duration.between(startTime, endTime).getSeconds());
	}
	
	private void writeOutputFile(String outputString, String outputFilePath) throws IOException{
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(new File(outputFilePath)))) {
			writer.write(outputString);
		} catch (IOException e){
			throw log.throwing(new IOException("Can't write output file "+outputFilePath+" \n"+e.getMessage()));
		}
	}

	public Boolean filterOnClusters(Gene gene, String anatEntityId, CallService callService, Ontology<AnatEntity> descendants) {
		LinkedHashMap<CallService.OrderingAttribute, Service.Direction> serviceOrdering = new LinkedHashMap<>();
		serviceOrdering.put(CallService.OrderingAttribute.GLOBAL_RANK, Service.Direction.ASC);
		// list of expression call, ordered by anat entity
		LinkedHashMap<String, ExpressionCall> callsByAnatEntityId = callService
				.loadExpressionCalls(gene.getSpeciesId(),
						new ExpressionCallFilter(new GeneFilter(gene.getId()), null, new DataPropagation(ANAT_ENTITY_PROPAGATION,DEV_STAGE_PROPAGATION),
								Arrays.asList(new ExpressionCallData(Expression.EXPRESSED,DATATYPE))),
						EnumSet.of(CallService.Attribute.GENE_ID, CallService.Attribute.ANAT_ENTITY_ID,
								CallService.Attribute.DEV_STAGE_ID, CallService.Attribute.GLOBAL_RANK),
						serviceOrdering)
				// return best ranked expression call for each anat entity
				// mapped by anat entity
				.collect(Collectors.toMap(p -> p.getCondition().getAnatEntityId(), p -> p, (v1, v2) -> v1,
						LinkedHashMap::new));
		// define clustering method
		Function<List<ExpressionCall>, Map<ExpressionCall, Integer>> clusteringFunction = getClusteringFunction();
		// Store a clustering of ExpressionCalls, by considering only one best ExpressionCall from each anatomical entity (anat entity part of first cluster of gene expression)
		Map<String, Integer> clusteringBestEachAnatEntity = clusteringFunction
				.apply(new ArrayList<ExpressionCall>(callsByAnatEntityId.values()))
				.entrySet().stream()
				//filter on cluster number
				.filter(map -> map.getValue() <= CLUSTER_RANK_LIMIT)
				//create a Map like anatEntityID -> cluster rank
				.collect(Collectors.toMap(e -> e.getKey().getCondition().getAnatEntityId(), e -> e.getValue()));
		clusteringBestEachAnatEntity = clusteringBestEachAnatEntity.entrySet().stream().filter(s -> descendants.getElement(s.getKey())==null||s.getKey().equals(anatEntityId)).collect(Collectors.toMap(s -> s.getKey(), s -> s.getValue()));
		if (clusteringBestEachAnatEntity.containsKey(anatEntityId)) {
			log.debug("number of anat entities in first cluster : "+clusteringBestEachAnatEntity.size()+" for gene "+gene.getId());
			if (clusteringBestEachAnatEntity.size() <= LIMIT_NUMBER_OF_ANAT_ENTITIES){
				return log.exit(true);
			}
		}
		log.debug("gene "+gene.getId()+" is expressed in selected anat entity but not more expressed than in other anat entities");
		return log.exit(false);
	}

	private Function<List<ExpressionCall>, Map<ExpressionCall, Integer>> getClusteringFunction()
			throws IllegalStateException {
		log.entry();
		// init clustering method and clustering threshold with default values
		String clusteringMethod = BgeeProperties.GENE_SCORE_CLUSTERING_METHOD_DEFAULT;
		Double ClusteringThreshold = BgeeProperties.GENE_SCORE_CLUSTERING_THRESHOLD_DEFAULT;
		if (clusteringMethod == null) {
			throw log.throwing(new IllegalStateException("No clustering method specified."));
		}
		// Distance threshold
		if (ClusteringThreshold == null ||
		// we don't want negative nor near-zero values
				ClusteringThreshold < 0.000001) {
			throw log.throwing(new IllegalStateException("A clustering method was specified, "
					+ "but no distance threshold or incorrect threshold value assigned."));
		}
		try {
			// find clustering method
			final ClusteringMethod method = ClusteringMethod.valueOf(clusteringMethod.trim());

			// define clustering function
			log.debug("Using clustering method {} with distance threshold {}", method, ClusteringThreshold);
			return log.exit(
					callList -> ExpressionCall.generateMeanRankScoreClustering(callList, method, ClusteringThreshold));
		} catch (IllegalArgumentException e) {
			throw log.throwing(
					new IllegalStateException("No custering method corresponding to " + clusteringMethod.trim()));
		}
	}
	
	
//	private Set<AnatEntity> manageDescendants (List<AnatEntity> parent,ServiceFactory factory, String speciesId){
//		Ontology<AnatEntity> descendantEntities = factory.getOntologyService().getAnatEntityOntology(Collections.singleton(speciesId), Collections.singleton(parent.getId()), Collections.singleton(RelationType.ISA_PARTOF), false, true, factory.getAnatEntityService());
//		descendantEntities.
//		return null;
//	}

}
