package org.bgee.controller;


import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.exception.PageNotFoundException;
import org.bgee.model.BgeeUtils;
import org.bgee.model.Service;
import org.bgee.model.ServiceFactory;
import org.bgee.model.expressiondata.Call;
import org.bgee.model.expressiondata.Call.ExpressionCall;
import org.bgee.model.expressiondata.CallData.ExpressionCallData;
import org.bgee.model.expressiondata.CallFilter.ExpressionCallFilter;
import org.bgee.model.expressiondata.CallService;
import org.bgee.model.expressiondata.Condition;
import org.bgee.model.expressiondata.ConditionUtils;
import org.bgee.model.expressiondata.baseelements.CallType.Expression;
import org.bgee.model.expressiondata.baseelements.DataPropagation;
import org.bgee.model.gene.Gene;
import org.bgee.model.gene.GeneFilter;
import org.bgee.view.GeneDisplay;
import org.bgee.view.ViewFactory;

/**
 * Controller handling requests related to gene pages. 
 * 
 * @author  Philippe Moret
 * @author  Frederic Bastian
 * @author  Valentine Rech de Laval
 * @version Bgee 13, May 2016
 * @since   Bgee 13, Nov. 2015
 */
public class CommandGene extends CommandParent {

    private final static Logger log = LogManager.getLogger(CommandGene.class.getName());
    
    /**
     * Contains all information necessary to produce a view related to a {@code Gene}.
     * 
     * @author Frederic Bastian
     * @version Bgee 13, Jan. 2016
     * @since   Bgee 13, Jan. 2016
     */
    public static class GeneResponse {
        private final Gene gene;
        private final List<ExpressionCall> exprCalls;
        private final Set<ExpressionCall> redundantExprCalls;
        private final ConditionUtils conditionUtils;
        
        /**
         * @param gene                  See {@link #getGene()}.
         * @param exprCalls             See {@link #getExprCalls()}.
         * @param redundantExprCalls    See {@link #getRedundantExprCalls()}.
         * @param conditionUtils        See {@link #getConditionUtils()}.
         */
        public GeneResponse(Gene gene, List<ExpressionCall> exprCalls, 
                Set<ExpressionCall> redundantExprCalls, ConditionUtils conditionUtils) {
            this.gene = gene;
            this.exprCalls = BgeeUtils.toList(exprCalls);
            this.redundantExprCalls = BgeeUtils.toSet(redundantExprCalls);
            this.conditionUtils = conditionUtils;
        }

        /**
         * @return  The {@code Gene} which information are requested for. 
         */
        public Gene getGene() {
            return gene;
        }
        /**
         * @return  A {@code List} of {@code ExpressionCall}s, ranked by the normalized global rank 
         *          of the gene in the related {@code Condition}s.
         */
        public List<ExpressionCall> getExprCalls() {
            return exprCalls;
        }
        /**
         * @return  A {@code Set} of {@code ExpressionCall}s for which there exists a more precise call 
         *          (i.e., with a more precise {@code Condition}), at a better rank (i.e., 
         *          with a lower index in the {@code List} returned by {@link #getExprCalls()})
         */
        public Set<ExpressionCall> getRedundantExprCalls() {
            return redundantExprCalls;
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
			display.displayGeneHomePage();
		} else {
			display.displayGene(this.buildGeneResponse(geneId));
		}
		log.exit();
	}

	private GeneResponse buildGeneResponse(String geneId) throws PageNotFoundException {
	    log.entry(geneId);
	    
	    Gene gene = this.getGene(geneId);
	    List<ExpressionCall> exprCalls = this.getExpressions(gene);
	    if (!exprCalls.isEmpty()) {
	    ConditionUtils conditionUtils = new ConditionUtils(Arrays.asList(gene.getSpeciesId()), 
	            exprCalls.stream().map(ExpressionCall::getCondition).collect(Collectors.toSet()), 
	            serviceFactory);
        log.debug("Expressions: {} {}", exprCalls.size(), exprCalls);
	    
	    return log.exit(new GeneResponse(gene, exprCalls, 
	            getRedundantCalls(exprCalls, conditionUtils),  
	            conditionUtils));
	    }
	    
	  	 log.info("No calls for gene "+geneId);
	  	 return log.exit(new GeneResponse(gene, exprCalls, new HashSet<ExpressionCall>(), null));
	}
	
	/**
	 * Gets the {@code Gene} instance from its id. 
	 * 
	 * @param geneId A {@code String} containing the gene id.
	 * @return       The {@code Gene} instance.
	 * @throws PageNotFoundException   If no {@code Gene} could be found corresponding to {@code geneId}.
	 */
	private Gene getGene(String geneId) throws PageNotFoundException {
	    log.entry(geneId);
		Gene gene = serviceFactory.getGeneService().loadGeneById(geneId);
		if (gene == null) {
		    throw log.throwing(new PageNotFoundException("No gene corresponding to " + geneId));
		}
		return log.exit(gene);
	}
	
	/**
	 * Retrieves the sorted list of {@code ExpressionCall} associated to this gene, 
	 * ordered by biological relevance.
	 * 
	 * @param gene The {@code Gene}
	 * @return     The {@code List} of {@code ExpressionCall} associated to this gene, ordered by relevance.
	 */
	private List<ExpressionCall> getExpressions(Gene gene) {
	    log.entry(gene);
	    
		LinkedHashMap<CallService.OrderingAttribute, Service.Direction> serviceOrdering = 
	                new LinkedHashMap<>();
	    serviceOrdering.put(CallService.OrderingAttribute.RANK, Service.Direction.ASC);
	        
		CallService service = serviceFactory.getCallService();
		return log.exit(service.loadExpressionCalls(
		        gene.getSpeciesId(), 
                new ExpressionCallFilter(new GeneFilter(gene.getId()), null, new DataPropagation(), 
                        Arrays.asList(new ExpressionCallData(Expression.EXPRESSED))), 
                EnumSet.of(CallService.Attribute.GENE_ID, CallService.Attribute.ANAT_ENTITY_ID, 
                        CallService.Attribute.DEV_STAGE_ID, CallService.Attribute.CALL_DATA, 
                        CallService.Attribute.GLOBAL_DATA_QUALITY), 
                serviceOrdering)
            .collect(Collectors.toList()));
	}

    /**
     * Identifies redundant {@code Calls} from the provided {@code List}. This method returns
     * {@code Call}s for which there exists a more precise call (i.e., with a more 
     * precise condition), at a better rank (i.e., with a lower index in the provided list). 
     * 
     * @param calls            The original ranked {@code List} of {@code Call}s.
     * @param conditionUtils   A {@code ConditionUtils}, containing all the {@code Condition}s 
     *                         related to {@code calls}.
     * @return                 A {@code Set} containing the {@code Call}s that are redundant.
     * @param <T>              The type of {@code Call} to be filtered.
     */
    private static <T extends Call<?, ?>> Set<T> getRedundantCalls(List<T> calls, ConditionUtils conditionUtils) {
        log.entry(calls, conditionUtils);

        long startFilteringTimeInMs = System.currentTimeMillis();
        
        Set<T> redundantCalls = new HashSet<>();
        Set<Condition> validatedConditions = new HashSet<>();
        for (T call: calls) {
            //Check whether this call is less precise than another call with a better rank. 
            Condition cond = call.getCondition();
            if (Collections.disjoint(validatedConditions, conditionUtils.getDescendantConditions(cond))) {
                log.trace("Valid call identified with condition: {}", cond);
                validatedConditions.add(cond);
            } else {
                log.trace("Redundant call identified with condition: {}", cond);
                redundantCalls.add(call);
            }
        }
        log.debug("Redundant calls filtered in {} ms", System.currentTimeMillis() - startFilteringTimeInMs);
        
        return log.exit(redundantCalls);
    }
}
