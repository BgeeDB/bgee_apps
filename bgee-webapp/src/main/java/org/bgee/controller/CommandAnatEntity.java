package org.bgee.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.exception.PageNotFoundException;
import org.bgee.model.Service;
import org.bgee.model.ServiceFactory;
import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.anatdev.AnatEntityService;
import org.bgee.model.expressiondata.CallService;
import org.bgee.model.expressiondata.ConditionFilter;
import org.bgee.model.expressiondata.ConditionUtils;
import org.bgee.model.expressiondata.Call.ExpressionCall;
import org.bgee.model.expressiondata.CallData.ExpressionCallData;
import org.bgee.model.expressiondata.CallFilter.ExpressionCallFilter;
import org.bgee.model.expressiondata.baseelements.DataPropagation;
import org.bgee.model.gene.Gene;
import org.bgee.model.gene.GeneService;
import org.bgee.model.expressiondata.baseelements.CallType.Expression;
import org.bgee.model.species.Species;
import org.bgee.model.species.SpeciesService;
import org.bgee.view.AnatEntityDisplay;
import org.bgee.view.ViewFactory;


/**
 * Controller handling requests related to anat.entity pages. 
 * 
 * @author Julien Wollbrett
 * @version Bgee 13, Jan. 2017
 * @since   Bgee 13, Jan. 2017
 */

public class CommandAnatEntity extends CommandParent{

	private final static Logger log = LogManager.getLogger(CommandAnatEntity.class.getName());
	
	protected final GeneService geneService = this.serviceFactory.getGeneService();
	protected final SpeciesService speciesService = this.serviceFactory.getSpeciesService();
	protected final AnatEntityService anatEntityService = this.serviceFactory.getAnatEntityService();
	 /**
     * Contains all information necessary to produce a view related to a {@code AnatEntity}.
     * 
     * @author Julien Wollbrett
     * @version Bgee 13, Jan. 2017
     * @since   Bgee 13, Jan. 2017
     */
    public static class AnatEntityResponse {
    	
        private final AnatEntity anatEntity;
        private final List<ExpressionCall> exprCalls;
        private final Map<String,Gene> genesMap;
        private final ConditionUtils conditionUtils;
        
        /**
         * @param anatEntity                    See {@link #getAnatEntity()}.
         * @param exprCalls                     See {@link #getExprCalls()}.
         * @param genesMap                    	See {@link #getGenesMap()}.
         * @param conditionUtils                See {@link #getConditionUtils()}.
         * 
         */ 
        
        public AnatEntityResponse(AnatEntity anatEntity, List<ExpressionCall> exprCalls, Map<String,Gene> genesMap, ConditionUtils conditionUtils) {
			this.anatEntity = anatEntity;
			this.exprCalls = exprCalls;
			this.genesMap = genesMap;
			this.conditionUtils = conditionUtils;
		}
        
        /**
         * @return  The {@code AnatEntity} which information are requested for. 
         */
        public AnatEntity getAnatEntity() {
			return anatEntity;
		}
        
        /**
         * @return  A {@code List}  of {@code ExpressionCall}s of the anat. entity in the related {@code Condition}s.
         */
        public List<ExpressionCall> getExprCalls() {
			return exprCalls;
		}
        
        /**
         * @return  A {@code HashMap} of containing all {@code Gene}s associated to the anat. entity in the related {@code Condition}s. Keys correspond to geneIds
         */
        public Map<String,Gene> getGenesMap() {
			return genesMap;
		}
        /**
         * @return  A {@code ConditionUtils} loaded from all {@code Condition}s 
         *          retrieved from the {@code ExpressionCall}s in the {@code List} returned by 
         *          {@link #getExprCalls()}.
         */
        public ConditionUtils getConditionUtils() {
            return conditionUtils;
        }
 	}
	
	/**
     * Constructor
     * 
     * @param response          A {@code HttpServletResponse} that will be used to display the 
     *                          page to the client
     * @param requestParameters The {@code RequestParameters} that handles the parameters of the 
     *                          current request.
     * @param prop              A {@code BgeeProperties} instance that contains the properties
     *                          to use.
     * @param viewFactory       A {@code ViewFactory} that provides the display type to be used.
     * @param serviceFactory    A {@code ServiceFactory} that provides bgee services.
     */
	public CommandAnatEntity(HttpServletResponse response, RequestParameters requestParameters, BgeeProperties prop,
	        ViewFactory viewFactory, ServiceFactory serviceFactory) {
		super(response, requestParameters, prop, viewFactory, serviceFactory);
	}



	@Override
	public void processRequest() throws Exception {
		log.entry();
		AnatEntityDisplay display = viewFactory.getAnatEntityDisplay();
		String anatEntityId = requestParameters.getAnatEntityId();
		List<String> speciesIds = requestParameters.getSpeciesList();
		if (anatEntityId == null) {
			display.displayAnatEntityHomePage();
		} else if(speciesIds.size() != 1){
			throw log.throwing(new PageNotFoundException("Not possible to query with more than one species"));
		}else {
			display.displayAnatEntity(this.buildAnatEntityResponse(anatEntityId, speciesIds.get(0)));
		}
		log.exit();
		
	}
	
	private AnatEntityResponse buildAnatEntityResponse(String anatEntityId, String speciesId) 
	        throws PageNotFoundException, IllegalStateException {
		log.entry(anatEntityId, speciesId);
		
		//**************************************
        // Retrieve AnatEntities and species
        //**************************************
	    AnatEntity anatEntity = this.getAnatEntity(anatEntityId);
	    Species species = this.getSpecies(speciesId);
	    
	    
	    List<ExpressionCall> exprCalls = this.getExpressions(anatEntity,species);
	    
	    if (exprCalls.isEmpty()) {
	        log.debug("No calls for anatomical entity {}", anatEntityId);
	         return log.exit(new AnatEntityResponse(anatEntity, exprCalls, new HashMap<>(), null));
	    }
	 	ConditionUtils conditionUtils = new ConditionUtils(species.getId(), 
            exprCalls.stream().map(ExpressionCall::getCondition).collect(Collectors.toSet()), 
            serviceFactory);
	 	
	 	//we need to make sure that the ExpressionCalls are ordered in exactly the same way 
	    //for the display and for the clustering, otherwise the display will be buggy, 
	    //notably for calls with equal ranks. And we need to take into account 
	    //relations between Conditions for filtering them, which would be difficult to achieve
	    //only by a query to the data source. So, we order them anyway. 
        long startFilteringTimeInMs = System.currentTimeMillis();
        Collections.sort(exprCalls, new ExpressionCall.RankComparator(conditionUtils));
        log.debug("Calls sorted in {} ms", System.currentTimeMillis() - startFilteringTimeInMs);
        
        //**************************************
        // remove redundant ExpressionCalls for one gene
        //**************************************
        Map<String,Gene> redundantGenes = new HashMap<String,Gene>();
        List<ExpressionCall> uniqueExprCalls = new ArrayList<>();
        for(ExpressionCall call:exprCalls){
        	if(!redundantGenes.containsKey(call.getGeneId())){
        		redundantGenes.put(call.getGeneId(),geneService.loadGeneById(call.getGeneId()));
        		uniqueExprCalls.add(call);
        	}
        }
	    return log.exit(new AnatEntityResponse(anatEntity, uniqueExprCalls, redundantGenes, conditionUtils));
	}
	
	
	/**
	 * Gets the {@code AnatEntity} instance from its id. 
	 * 
	 * @param anatEntityId A {@code String} containing the AnatEntity id.
	 * @return       The {@code AnatEntiy} instance.
	 * @throws PageNotFoundException   If no {@code AnatEntity} could be found corresponding to {@code anatEntityId}.
	 */
	private AnatEntity getAnatEntity(String anatEntityId) throws PageNotFoundException {
	    log.entry(anatEntityId);
	    AnatEntity anatEntity = anatEntityService.loadAnatEntityById(anatEntityId);
		if (anatEntity == null) {
		    throw log.throwing(new PageNotFoundException("No anatomical entity corresponding to " + anatEntity));
		}
		return log.exit(anatEntity);
	}
	
	/**
	 * Gets the {@code Species} instance from its id.
	 * 
	 * @param speciesIds A {@code List} of {@code String} containing the Species ids.
	 * @return       The {@code Set} of {@code Species} instances.
	 */
	private Species getSpecies(String speciesId) throws PageNotFoundException {
	    log.entry(speciesId);
	    Set<Species> speciesSet = speciesService.loadSpeciesByIds(Collections.singleton(speciesId), false);
	    if(speciesSet.size() == 0){
	    	throw log.throwing(new PageNotFoundException("No species corresponding to " + speciesId));
	    }
		return log.exit(speciesSet.iterator().next());
	}
	
	/**
	 * Retrieves the sorted list of {@code ExpressionCall} associated to this anat. entity for selected species, 
	 * ordered by global mean rank.
	 * 
	 * @param anatEntity The {@code AnatEntity}
	 * @param speciesIds The {@code Set} of {@code Species} Ids.
	 * @return     The {@code List} of {@code ExpressionCall} associated to this anat. entity, 
	 *             ordered by global mean rank as values.
	 */
	private List<ExpressionCall> getExpressions(AnatEntity anatEntity, Species species) {
	    log.entry(anatEntity);
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
		                        CallService.Attribute.DEV_STAGE_ID, CallService.Attribute.CALL_DATA, 
		                        CallService.Attribute.GLOBAL_DATA_QUALITY, CallService.Attribute.GLOBAL_RANK), 
		                serviceOrdering)
		            .collect(Collectors.toList()));
		return log.exit(expressionCalls);
	}
}

	
