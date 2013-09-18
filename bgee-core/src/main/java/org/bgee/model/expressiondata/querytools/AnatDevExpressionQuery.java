package org.bgee.model.expressiondata.querytools;

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
	/**
     * <code>Logger/code> of this class.
     */
    private final static Logger log = 
    		LogManager.getLogger(AnatDevExpressionQuery.class.getName());
    @Override
    protected Logger getLogger() {
    	return log;
    }
    
	public enum QueryType {
		ANATOMY, DEVELOPMENT, ANATDEV;
	}
	
	public enum Filtering {
		FROMTOP, FROMBOTTOM, NONE;
	}
	
	public void launchQuery() {
		try {
			this.startQuery("Querying blabla", 1, "");//TODO
			
		} catch (InterruptedException e) {
			//long-running queries can be interrupted by the TaskManager, so we need 
			//to be prepared to catch an interruption.
			//TODO: clean up to do here?
			//propagate the interruption, keep the interruption status
			Thread.currentThread().interrupt();
		} finally {
			//TODO: clean up to do here?
		}
	}
	
	private boolean reconcileDataTypeCalls;//or: noDataTypeContradiction?
	private boolean withTopEntities;
	private int targetNumberTopEntities;
	private int levelCountToWalk;
	private Set<AnatDevEntity> rootEntites;
	private Collection<AnatDevRequirement> requirements;
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
