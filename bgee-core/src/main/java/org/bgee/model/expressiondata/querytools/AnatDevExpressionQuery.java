package org.bgee.model.expressiondata.querytools;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.anatdev.core.AnatDevEntity;
import org.bgee.model.expressiondata.querytools.AnatDevRequirement.GeneCallRequirement;

/**
 * This class allows to retrieve {@link org.bgee.model.anatdev.AnatDevElement}s based on 
 * their gene expression data, and on their position in their {@code Ontology}. 
 * <p>
 * The validation of {@code AnatDevElement}s based on their gene expression data 
 * is defined by providing {@link AnatDevRequirement}s to this 
 * {@code AnatDevExpressionQuery} (see {@link #addRequirement(AnatDevRequirement)} and 
 * {@link #addAllRequirements(Collection)}).
 * <p>
 * What are the types of {@code AnatDevElement}s to retrieve are defined using 
 * a {@link QueryType}. How the {@code AnatDevElement}s retrieved using 
 * the {@code AnatDevRequirement}s should be selected and organized is defined 
 * using a {@link DataRendering}. Both a {@code QueryType} and a {@code DataRendering} 
 * must be provided at instantiation.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public class AnatDevExpressionQuery extends ExpressionQuery {
    /**
     * An {@code enum} defining the different types of query that 
     * an {@link AnatDevExpressionQuery} can perform: 
     * <ul>
     * <li>{@code ANATOMY}: query for {@link org.bgee.model.anatdev.AnatElement}s
     * <li>{@code DEVELOPMENT}: query for {@link org.bgee.model.anatdev.DevElement}s
     * <li>{@code ANATDEV}: query for {code AnatElement}s with details of 
     * {code DevElement}s with expression data for each of them. Note that 
     * this {@code QueryType} is not compatible with the {@link DataRendering} 
     * {@code ONTOLOGY} and {@code SUMMARY}.
     * </ul>
     * 
     * @author Frederic Bastian
     * @version Bgee 13
     * @since Bgee 13
     */
	public enum QueryType {
		ANATOMY, DEVELOPMENT, ANATWITHDEV, DEVWITHANAT;
	}
	/**
	 * An {@code enum} defining the different data rendering methods available 
	 * from an {@link AnatDevExpressionQuery}. It defines the different ways 
	 * of selecting and organizing {@code AnatDevElement}s, once they have been 
	 * retrieved from an expression query.
	 * <ul>
	 * <li>{@code ONTOLOGY}: render the {@code AnatDevElement}s validated organized 
	 * as an {@code Ontology}. The root elements of this {@code Ontology} and 
	 * the number of level to walk from these roots are defined using 
	 * the {@code AnatDevExpressionQuery}.
	 * <li>{@code SUMMARY}: select a defined number of top {@code AnatDevElement}s 
	 * in an {@code Ontology}, so that they, and their substructures, include 
	 * all {@code AnatDevElement}s validated. The number of elements is defined 
	 * using the {@code AnatDevExpressionQuery}.
	 * <li>{@code: ALL}: all {@code AnatDevElement}s validated are provided without 
	 * any organization nor filtering.
	 * <li>{@code: PRECISE}: select only the most precise and independent 
	 * {@code AnatDevElement}s. It means that amongst the validated {@code AnatDevElement}s, 
	 * it will be kept only the closest to the leaves ("most precise"), so that 
	 * no {@code AnatDevElement}s selected will be a parent of another one by a 
	 * {@code ISA_PARTOF} relation ("independent").
	 * <li>{@code ALLGROUPED}: same as {@code ALL}, except that the validated 
	 * {@code AnatDevElement}s will tried to be grouped. The grouping is made 
	 * by defining a wished number of {@code AnatDevElement}s by group, and the groups 
	 * correspond to selected {@code AnatDevElement}s in the {@code Ontology}, 
	 * that are the parents by {@code ISA_PARTOF} relations the closest to 
	 * the validated {@code AnatDevElement}s, and each encompassing a number of 
	 * {@code AnatDevElement}s the closest to the wished number. The wished number 
	 * is defined using the {@code AnatDevExpressionQuery}. 
	 * <li>{@code PRECISEGROUPED}: same as {@code PRECISE}, except that the validated 
     * {@code AnatDevElement}s will tried to be grouped. This is the same principle 
     * than {@code ALLGROUPED}, see its description above.
	 * </ul>
	 * 
	 * @author Frederic Bastian
	 * @version Bgee 13
	 * @since Bgee 13
	 */
	public enum DataRendering {
		ONTOLOGY, SUMMARY, ALL, PRECISE, ALLGROUPED, PRECISEGROUPED;
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
     * A {@code Collection} of {@link AnatDevRequirement}s defining which 
     * expression data to retrieve for which {@code Gene}s, and what are 
     * the requirements for an {@code AnatDevElement} to be validated. 
     */
	private final Collection<AnatDevRequirement> requirements;
	
	/**
	 * A {@code QueryType} defining what is the query that this 
	 * {@code AnatDevExpressionQuery} should perform.
	 */
	private final QueryType queryType;
	/**
	 * The {@code DataRendering} defining how {@code AnatDevElement}s 
	 * should be selected and organized after they were validated by 
	 * the {@link #requirements}.
	 */
	private DataRendering rendering;
	/**
	 * An {@code int} defining what is the wished number of 
	 * {@code AnatDevElement}s by group. This value is applicable when 
	 * {@link #rendering} corresponds to a {@link DataRendering} requesting 
	 * a grouping of the {@code AnatDevElement}s retrieved. This value is only 
	 * a wish, this {@code AnatDevExpressionQuery} will make its best 
	 * to obtain groups with a number of {@code AnatDevElement}s as closed 
	 * to this value as possible.
	 */
	private int elementsByGroup;
	
	/**
	 * Constructor defining the type of query that this {@code AnatDevExpressionQuery} 
	 * should perform, and the way data should be rendered.
	 * <p>
	 * An {@code IllegalArgumentException} can be thrown if {@code queryType} and 
	 * {@code rendering} are incompatible (see {@link QueryType} for more details.) 
	 * 
	 * @param queryType    A {@code QueryType} defining the type of query to perform.
	 * @param rendering    A {@code DataRendering} defining the data rendering to perform.
	 * @throws IllegalArgumentException    if {@code queryType} and {@code rendering} 
	 *                                     are incompatible
	 */
	public AnatDevExpressionQuery(QueryType queryType, DataRendering rendering) 
	    throws IllegalArgumentException {
	    if ((queryType.equals(QueryType.ANATWITHDEV) || 
	        queryType.equals(QueryType.DEVWITHANAT)) && 
	       (rendering.equals(DataRendering.ONTOLOGY) || 
	        rendering.equals(DataRendering.SUMMARY))) {
	        throw log.throwing(new IllegalArgumentException("The QueryType provided (" +
	        		queryType + ") is incompatible with the DataRendering provided (" +
	        		rendering + ")"));
	    }
	    this.queryType = queryType;
	    this.rendering = rendering;
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
			
			//calls need to be manually propagated. => really true for AnatEntities?
			//the new CallFilter merging does not merge CallFilters with 
			//different propagation rules...
			//So it is only a problem if a CompositeCallFilter is used (but that 
			//could be taken care of by a different class/method), 
			//and for Stages, that do not have a globalExpression table in the database.
			
			//--- Remove following comments based on decision: ---
			
			//As a conclusion, you should keep in mind that the global expression tables 
			//generated in the Bgee database are useful to retrieve sets of Genes 
			//with specific expression patterns, not to retrieve expression patterns 
			//of a given set of Genes. 
			
			//so we need to reset the propagation parameter of the CallFilters to query the calls, 
			//then propagate manually when needed. 
			
			//---                                              ---
			
			this.analyzeRequirements();
			
			queryCompleted = true;
		} catch (InterruptedException e) {
			//long-running queries can be interrupted by the TaskManager, so we need 
			//to be prepared to catch an interruption.
			//TODO: clean up to do here?
			//propagate the interruption, keep the interruption status
		    log.catching(e);
			Thread.currentThread().interrupt();
		} finally {
			this.endQuery(queryCompleted);
		}
		log.exit();
	}
	
	private void analyzeRequirements() {
		
	}
	
	/**
	 * Retrieves all {@link Call}s required in all {@code AnatDevRequirement}s 
	 * (returned by {@link #getRequirements()}), for all {@code Gene}s, 
	 * and aggregates them. The aim is to be able afterwards to obtain the data 
	 * from a {@code DAO} as easily as possible. This methods: 
	 * <ul>
	 * <li>first retrieves all {@link CallFilter}s for all {@code Gene}s 
	 * in all {@code AnatDevRequirement}s.
	 * <li>then tries, for each {@code Gene}, to merge as much as possible 
	 * all its related {@link CallFiler}s.
	 * <li>finally, tries to merge equivalent {@code CallFiler}s of different 
	 * {@code Gene}s, so that a same {@code CallFiler} could be associated 
	 * to several {@code Gene}s.
	 * </ul>  
	 * After these operations, the query sent to the {@code DAO} should be 
	 * as simplified as possible. 
	 */
	private void aggregateCallFilters() {
		
	}
	
	
	//*********************************
	// GETTERS AND SETTERS
	//*********************************
	
	/**
	 * Add an {@link AnatDevRequirement} to the {@code Collection} of 
	 * {@code AnatDevRequirement}s defining expression data to retrieve, 
	 * for which {@code Gene}s, and how to validate the {@code AnatDevElement} 
	 * to keep. 
	 * 
	 * @param requirement	an {@code AnatDevRequirement} to be added to 
	 * 						this {@code AnatDevExpressionQuery}
	 * @see #getRequirements()
	 * @see #addAllRequirements(Collection)
	 */
	public void addRequirement(AnatDevRequirement requirement) {
		this.requirements.add(requirement);
	}
	/**
	 * Add a {@code Collection} of {@link AnatDevRequirement}s to 
	 * the {@code Collection} of {@code AnatDevRequirement}s defining 
	 * expression data to retrieve, for which {@code Gene}s, and how 
	 * to validate the {@code AnatDevElement} to keep. 
	 * 
	 * @param requirement	a {@code Collection} of {@code AnatDevRequirement}s 
	 * 						to be added to this {@code AnatDevExpressionQuery}.
	 * @see #getRequirements()
	 * @see #addRequirement(AnatDevRequirement)
	 */
	public void addAllRequirements(Collection<AnatDevRequirement> requirements) {
		this.requirements.addAll(requirements);
	}
	/**
	 * Return a {@code Collection} of {@link AnatDevRequirement}s defining which 
     * expression data to retrieve for which {@code Gene}s, and what are 
     * the requirements for an {@code AnatDevElement} to be validated. 
     * 
	 * @return	a {@code Collection} of {@code AnatDevRequirement}s
	 * @see #addRequirement(AnatDevRequirement)
	 * @see #addAllRequirements(Collection)
	 */
	public Collection<AnatDevRequirement> getRequirements() {
		return this.requirements;
	}
	
	//---------------------------------------------
	private boolean reconcileDataTypeCalls;//or: noDataTypeContradiction?
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
