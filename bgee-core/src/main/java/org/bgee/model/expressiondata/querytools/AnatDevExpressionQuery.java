package org.bgee.model.expressiondata.querytools;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.anatdev.core.AnatDevEntity;
import org.bgee.model.expressiondata.querytools.AnatDevRequirement.GeneCallRequirement;

/**
 * This class allows to retrieve <code>AnatEntity</code>s and/or 
 * <code>DevStage</code>s based on their gene expression data, and on their position 
 * in their <code>AnatDevOntology</code>. 
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public class AnatDevExpressionQuery extends QueryTool {
	public enum QueryType {
		ANATOMY, DEVELOPMENT, ANATDEV;
	}
	public enum Filtering {
		FROMTOP, FROMBOTTOM, NONE;
	}
	
	/**
     * <code>Logger/code> of this class.
     */
    private final static Logger log = 
    		LogManager.getLogger(AnatDevExpressionQuery.class.getName());
    @Override
    protected Logger getLogger() {
    	return log;
    }

    /**
     * A <code>Collection</code> of {@link AnatDevRequirement}s defining which 
     * expression data to retrieve for which <code>Gene</code>s, and what are 
     * the requirements for an <code>AnatDevElement</code> to be validated. 
     */
	private final Collection<AnatDevRequirement> requirements;
	
	/**
	 * Default constructor. 
	 */
	public AnatDevExpressionQuery() {
		this.requirements = new ArrayList<AnatDevRequirement>();
	}
	
	//*********************************
    // QUERY METHODS
	//*********************************
	
	public void launchQuery() {
		log.entry();
		//in order to know if the query was successfully completed
		boolean queryCompleted = false;
		
		try {
			this.startQuery("Querying blabla", 1, "");//TODO
			
			this.aggregateCallFilters();
			
			queryCompleted = true;
		} catch (InterruptedException e) {
			//long-running queries can be interrupted by the TaskManager, so we need 
			//to be prepared to catch an interruption.
			//TODO: clean up to do here?
			//propagate the interruption, keep the interruption status
			Thread.currentThread().interrupt();
		} finally {
			this.endQuery(queryCompleted);
		}
		log.exit();
	}
	
	/**
	 * Retrieves all {@link Call}s required in all <code>AnatDevRequirement</code>s 
	 * (returned by {@link #getRequirements()}), for all <code>Gene</code>s, 
	 * and aggregates them. The aim is to be able afterwards to obtain the data 
	 * from a <code>DAO</code> as easily as possible. This methods: 
	 * <ul>
	 * <li>first retrieves all {@link CallFilter}s for all <code>Gene</code>s 
	 * in all <code>AnatDevRequirement</code>s.
	 * <li>then tries, for each <code>Gene</code>, to merge as much as possible 
	 * all its related {@link CallFiler}s.
	 * <li>finally, tries to merge equivalent <code>CallFiler</code>s of different 
	 * <code>Gene</code>s, so that a same <code>CallFiler</code> could be associated 
	 * to several <code>Gene</code>s.
	 * </ul>  
	 * After these operations, the query sent to the <code>DAO</code> should be 
	 * as simplified as possible. 
	 */
	private void aggregateCallFilters() {
		
	}
	
	
	//*********************************
	// GETTERS AND SETTERS
	//*********************************
	
	/**
	 * Add an {@link AnatDevRequirement} to the <code>Collection</code> of 
	 * <code>AnatDevRequirement</code>s defining expression data to retrieve, 
	 * for which <code>Gene</code>s, and how to validate the <code>AnatDevElement</code> 
	 * to keep. 
	 * 
	 * @param requirement	an <code>AnatDevRequirement</code> to be added to 
	 * 						this <code>AnatDevExpressionQuery</code>
	 * @see #getRequirements()
	 * @see #addAllRequirements(Collection)
	 */
	public void addRequirement(AnatDevRequirement requirement) {
		this.requirements.add(requirement);
	}
	/**
	 * Add a <code>Collection</code> of {@link AnatDevRequirement}s to 
	 * the <code>Collection</code> of <code>AnatDevRequirement</code>s defining 
	 * expression data to retrieve, for which <code>Gene</code>s, and how 
	 * to validate the <code>AnatDevElement</code> to keep. 
	 * 
	 * @param requirement	a <code>Collection</code> of <code>AnatDevRequirement</code>s 
	 * 						to be added to this <code>AnatDevExpressionQuery</code>.
	 * @see #getRequirements()
	 * @see #addRequirement(AnatDevRequirement)
	 */
	public void addAllRequirements(Collection<AnatDevRequirement> requirements) {
		this.requirements.addAll(requirements);
	}
	/**
	 * Return a <code>Collection</code> of {@link AnatDevRequirement}s defining which 
     * expression data to retrieve for which <code>Gene</code>s, and what are 
     * the requirements for an <code>AnatDevElement</code> to be validated. 
     * 
	 * @return	a <code>Collection</code> of <code>AnatDevRequirement</code>s
	 * @see #addRequirement(AnatDevRequirement)
	 * @see #addAllRequirements(Collection)
	 */
	public Collection<AnatDevRequirement> getRequirements() {
		return this.requirements;
	}
	
	
	private boolean reconcileDataTypeCalls;//or: noDataTypeContradiction?
	private boolean withTopEntities;
	private int targetNumberTopEntities;
	private int levelCountToWalk;
	private Set<AnatDevEntity> rootEntites;
	private Set<AnatDevEntity> filteringEntities;
	private boolean acceptFilteringEntities;
	
	private EvoTransRelation evoRelation; ou EvoGroup ?
	
    
    
    restriction on organs? (e.g., jacknife on HOGs for my analyses): only in organs/never in organs kind of?
    		useful for all anaylses or only this class?
    				
    				also, parameters "with mean expression level by experiment", probably useful for all query tools
    				this could be compute for each gene for an organ query, or for each organ on a gene query
    				this could be a last view, after data count, raw data: mean expression compared from raw data
    			    and maybe we can compute a rank for all organs for each experiment independently, something like that
}
