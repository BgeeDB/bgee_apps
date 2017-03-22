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
import java.util.stream.Stream;

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

//Filter @Test method potentially useful to filter genes retrieved by the anat. entity web page
public class TestAnatEntityWithFilter extends TestAncestor {

	private final static Logger log = LogManager.getLogger(TestAnatEntityWithFilter.class.getName());
	private final static boolean FILTER_ON_CLUSTERING_RESULTS = true;
	private final static int LIMIT_NUMBER_OF_ANAT_ENTITIES = 10;
	private final static int CLUSTER_RANK_LIMIT = 1;
	private final static boolean MANAGE_GENE_PROPAGATION = false;
	private final static DataType DATATYPE = null;
	private final static PropagationState ANAT_ENTITY_PROPAGATION = PropagationState.SELF;
	private final static PropagationState DEV_STAGE_PROPAGATION = PropagationState.SELF;
	private final static String FILE_PATH = "src/test/data/";



	@Override
	protected Logger getLogger() {
		return log;
	}
	
	/**
	 * Test method used to have correspondance between species ids and species name
	 */
	@Test
	public void testGetSpecies() {
		ServiceFactory factory = new ServiceFactory();
		Set<Species> speciesSet = factory.getSpeciesService().loadSpeciesInDataGroups(true);
		for (Species species : speciesSet) {
			log.debug(species.getId() + "\t" + species.getName());
		}
		factory.close();
	}
	
	
	
	
	/**
	 * Method used to test different filter approach for the creation of the anat entity page.
	 * These filters are :
	 * - Return all genes for one anat. entity (FILTER_ON_CLUSTERING_RESULTS = false)
	 * - Return all genes for one anat. entity but only when this anat entity is present in the 
	 *   first CLUSTER_RANK_LIMIT clusters of expression for this gene AND when this cluster don’t
	 *   contain more than LIMIT_NUMBER_OF_ANAT_ENTITIES anat. entities (FILTER_ON_CLUSTERING_RESULTS = true)
	 * - previous one + take into account arborescence of the anat. entity ontology in order to remove all
	 *   descendant elements of the selected anat entity from the cluster list (MANAGE_GENE_PROPAGATION = true)
	 * 
	 * @throws PageNotFoundException
	 * @throws IOException
	 */
	@Test
	public void testAnatEntityWithFilter() throws PageNotFoundException, IOException {
		//init all parameters of anat entity retrieval
		Instant startTime = Instant.now();
		//species and anat. entities to retrieve
		List<String> speciesIDs = Collections.singletonList("9606");
		List<String> anatEntitiesIDs = Collections.singletonList("UBERON:0002114");
// 		List<String> speciesIDs = Arrays.asList("9606","10090");
//		List<String> anatEntitiesIDs =
//		Arrays.asList("UBERON:0000451","UBERON:0002021","UBERON:0001954","UBERON:0001898","UBERON:2007003");
//		List<String> speciesIDs =
//		Arrays.asList("9606",/**"10090,"*/"8364","7955","9598","7227","6239");
//		List<String> anatEntitiesIDs =
//		Arrays.asList("UBERON:0000955","UBERON:0002037","UBERON:0002107","UBERON:0000948","UBERON:0000178");
		//loop over all species and anat. entities
		for (String speciesID : speciesIDs) {
			for (String anatEntityID : anatEntitiesIDs) {
				ServiceFactory serviceFactory = new ServiceFactory();
				//create ontology with all descendants of the anatEntity
				Ontology<AnatEntity> descendantsOntology = serviceFactory.getOntologyService().getAnatEntityOntology(new ArrayList<String>(Collections.singleton(speciesID)), new ArrayList<String>(Collections.singleton(anatEntityID)), new ArrayList<RelationType>(Collections.singleton(RelationType.ISA_PARTOF)), true, true, serviceFactory.getAnatEntityService());
				log.debug("species_ID : " + speciesID + " & anatEntity_ID : " + anatEntityID);
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
				// return list of best ranked ExpressionCall for each gene
				List<ExpressionCall> expressionCallWithBestRank = 
						new ArrayList<ExpressionCall>(
				retrieveExprCallsForAnatEntities(callService, species.getId(), condFilterCollection)
				//filter on best ranked expression call for each gene
						.collect(Collectors.toMap(p -> p.getGeneId(), p -> p, (v1, v2) -> v1, LinkedHashMap::new))
						.values());
				log.debug("expression call Map with only first expression call size : "+ expressionCallWithBestRank.size());
				//retrieve list of all genes expressed in this anatEntity
				Map<String,Gene> genes = serviceFactory.getGeneService().loadGenesByIdsAndSpeciesIds(
						expressionCallWithBestRank.stream().map(p -> p.getGeneId()).collect(Collectors.toList()),
						Collections.singletonList(speciesID)).stream().collect(Collectors.toMap(p -> p.getId(), p -> p));
				if (genes == null) {
					serviceFactory.close();
					throw log.throwing(new PageNotFoundException("No species corresponding to " + speciesID));
				}
				log.debug("finish uniqueCalls");
				log.debug("redundant expression calls number " + expressionCallWithBestRank.size());

				// format output
				String output = "gene_ID" + "\t" + "gene_name" + "\t" + "rank_value" + "\t" + "gene_definition\n";
				int outputInitialLength = output.length();
				for (ExpressionCall call : expressionCallWithBestRank) {
					Gene gene = genes.get(call.getGeneId());
					//filter or not on anat entity expression for the gene (use or not cluster of gene expression)
					if(!FILTER_ON_CLUSTERING_RESULTS){
						output += call.getGeneId() + "\t" + gene.getName() + "\t" + call.getFormattedGlobalMeanRank()
								+ "\t" + gene.getDescription() + "\n";
					}else if(filterOnClusters(gene, anatEntityID, callService,descendantsOntology)){
						output += call.getGeneId() + "\t" + gene.getName() + "\t" + call.getFormattedGlobalMeanRank()
						+ "\t" + gene.getDescription() + "\n";
					}
				}
				// write output file
				writeOutputFile(outputInitialLength, output, species.getName(), anatEntity.getName());
				serviceFactory.close();
			}
		}
		Instant endTime = Instant.now();
		log.debug("execution time : "+Duration.between(startTime, endTime).getSeconds());
	}
	
	private Boolean filterOnClusters(Gene gene, String anatEntityId, CallService callService, Ontology<AnatEntity> descendants) {
		LinkedHashMap<CallService.OrderingAttribute, Service.Direction> serviceOrdering = new LinkedHashMap<>();
		serviceOrdering.put(CallService.OrderingAttribute.GLOBAL_RANK, Service.Direction.ASC);
		// list of expression call, ordered by anat entity
		LinkedHashMap<String, ExpressionCall> callsByAnatEntityId = callService
				.loadExpressionCalls(gene.getSpeciesId(),
						new ExpressionCallFilter(new GeneFilter(gene.getId()), null, new DataPropagation(ANAT_ENTITY_PROPAGATION,DEV_STAGE_PROPAGATION),
								Arrays.asList(new ExpressionCallData(Expression.EXPRESSED,DATATYPE))),
						EnumSet.of(CallService.Attribute.GENE_ID, CallService.Attribute.ANAT_ENTITY_ID,
								CallService.Attribute.DEV_STAGE_ID, CallService.Attribute.GLOBAL_DATA_QUALITY, CallService.Attribute.GLOBAL_RANK),
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
				.filter(map -> map.getValue() < CLUSTER_RANK_LIMIT)
				//create a Map like anatEntityID -> cluster rank
				.collect(Collectors.toMap(e -> e.getKey().getCondition().getAnatEntityId(), e -> e.getValue()));
		if(MANAGE_GENE_PROPAGATION){
			clusteringBestEachAnatEntity = clusteringBestEachAnatEntity.entrySet().stream().filter(s -> descendants.getElement(s.getKey())==null||s.getKey().equals(anatEntityId)).collect(Collectors.toMap(s -> s.getKey(), s -> s.getValue()));
		}
		if (clusteringBestEachAnatEntity.containsKey(anatEntityId)) {
			log.debug("number of anat entities in first cluster : "+clusteringBestEachAnatEntity.size()+" for gene "+gene.getId());
			if (clusteringBestEachAnatEntity.size() < LIMIT_NUMBER_OF_ANAT_ENTITIES){
				return log.exit(true);
			}
		}
		log.debug("gene "+gene.getId()+" is expressed in selected anat entity but not more expressed than in other anat entities");
		return log.exit(false);
	}
	
	
	private Stream<ExpressionCall> retrieveExprCallsForAnatEntities(CallService callService, String speciesId, Collection<ConditionFilter> condFilterCollection){
		//define ordering approach
		LinkedHashMap<CallService.OrderingAttribute, Service.Direction> serviceOrdering = new LinkedHashMap<>();
		serviceOrdering.put(CallService.OrderingAttribute.GLOBAL_RANK, Service.Direction.ASC);
		return callService.loadExpressionCalls(speciesId,
				new ExpressionCallFilter(null, condFilterCollection, new DataPropagation(ANAT_ENTITY_PROPAGATION,DEV_STAGE_PROPAGATION), 
						Arrays.asList(new ExpressionCallData(Expression.EXPRESSED, DATATYPE))),
				EnumSet.of(CallService.Attribute.GENE_ID, CallService.Attribute.ANAT_ENTITY_ID,
						CallService.Attribute.DEV_STAGE_ID, CallService.Attribute.GLOBAL_DATA_QUALITY, CallService.Attribute.GLOBAL_RANK),
				serviceOrdering);
	}
	
	/**
	 * 
	 * @param outputInitialLength : Length of the header part of the output String
	 * @param output : output String to write
	 * @param speciesName : name of the species
	 * @param anatEntityName : name of the anatEntity
	 * @throws IOException : If not possible to write the output file
	 */
	private void writeOutputFile(int outputInitialLength, String output, String speciesName, String anatEntityName) throws IOException{
		if(output.length() > outputInitialLength){
			String fileName = FILE_PATH + speciesName.replace(" ", "_") + "_"
					+ anatEntityName.replace(" ", "_");// + "_"+ANAT_ENTITY_PROPAGATION+"_"+DEV_STAGE_PROPAGATION;
			if(DATATYPE!=null){
				fileName +="_"+DATATYPE.toString();
			}
			if(FILTER_ON_CLUSTERING_RESULTS){
				fileName +="_"+(CLUSTER_RANK_LIMIT)+"_"+(LIMIT_NUMBER_OF_ANAT_ENTITIES);
			}if(MANAGE_GENE_PROPAGATION){
				fileName +="_DES";
			}
			fileName+=".tsv";
			try (BufferedWriter writer = new BufferedWriter(new FileWriter(new File(fileName)))) {
				writer.write(output);
			} catch (IOException e){
				throw log.throwing(new IOException("Can't write output file "+fileName+" \n"+e.getMessage()));
			}			
		}
	}
	
	
	/**
	 * 
	 * @return result of the clustering function
	 * @throws IllegalStateException
	 */
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

}
