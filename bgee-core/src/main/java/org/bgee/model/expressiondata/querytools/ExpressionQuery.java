package org.bgee.model.expressiondata.querytools;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.Entity;
import org.bgee.model.expressiondata.querytools.filters.CallFilter;

/**
 * This class is extended by query tools, and is responsible for retrieving 
 * raw expression calls using <code>DAO</code>s. The reason why this class 
 * is <code>package-private</code>, and is not a query tool on its own, 
 * is that it will return <code>TransferObject</code>s from the <code>bgee-dao-api</code> 
 * modules, and we do not want to expose such classes to the public API. These 
 * <code>TransferObject</code>s will be processed by the child classes, and only 
 * processed results using classes from the <code>bgee-core</code> module 
 * will be exposed. 
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
abstract class ExpressionQuery extends QueryTool {
	/**
     * <code>Logger/code> of this class.
     */
    private final static Logger log = 
    		LogManager.getLogger(ExpressionQuery.class.getName());
    
	/**
	 * Default constructor. 
	 */
    protected ExpressionQuery() {
    	super();
    }
    
    /**
	 * Aggregates as much as possible the {@link CallFilter}s associated with 
	 * the <code>Entity</code>s. It tries to merge all <code>CallFilter</code>s, 
	 * whether they are associated with a same <code>Entity</code>, or different 
	 * <code>Entity</code>s. The result will be that a <code>CallFilter</code> 
	 * could be associated with several <code>Entity</code>s. This methods returns 
	 * a <code>Map</code> where keys are <code>CallFilter</code>s merged as much 
	 * as possible, and associated values are <code>Collection</code>s of 
	 * <code>Entity</code>s. The aim is to be able afterwards to obtain the data 
	 * from a <code>DAO</code> as easily as possible. 
	 * 
	 * @param entitiesWithFilters  a <code>Map</code> associating <code>Entity</code>s 
	 *                             to a <code>Collection</code> of <code>CallFilter</code>s.
	 * @return     A <code>Map</code> where keys are <code>CallFilter</code>s that 
	 *             were merged as much as possible, associated with their related 
	 *             <code>Entity</code>s.
	 * @param T    The precise type of <code>Entity</code> used.
	 */
	protected <T extends Entity> Map<CallFilter, Set<T>> aggregateCallFilters(
	        Map<T, Collection<CallFilter>> entitiesWithFilters) {
		log.entry(entitiesWithFilters);
		
		if (log.isDebugEnabled()) {
		    log.debug("Start ggregation of CallFilters. {} Entities to consider.", 
		            entitiesWithFilters.keySet().size());
		}
		//try to merge all CallFilters, whether they are associated with a same Entity, 
		//or different Entities. The result will be that a CallFilter will be 
		//associated with several Entities.
		Map<CallFilter, Set<T>> filterToEntities = new HashMap<CallFilter, Set<T>>();
		
		//first we create a HashMap to store the association CallFilter -> Entity
		//(for now a CallFilter is associated to at most 1 Entity, while 1 Entity 
		//can be associated to several CallFilters). Of note, CallFilters do not 
		//implement hashCode or equals, so that all CallFilters will be considered 
		//unique at this point.
		Map<CallFilter, T> filterToSingleEntity = new HashMap<CallFilter, T>();
        for (Entry<T, Collection<CallFilter>> entry: entitiesWithFilters.entrySet()) {
            for (CallFilter filter: entry.getValue()) {
                filterToSingleEntity.put(filter, entry.getKey());
            }
        }
		//now we create a Deque containing all CallFilters associated with their 
		//related Entity. Then it will be possible to replace a CallFilter in the deque,  
        //if it could not be merge with another CallFilter, to try to merge it 
        //with another CallFilter in the deque
		Deque<Entry<CallFilter, T>> filtersToMerge = 
		        new ArrayDeque<Entry<CallFilter, T>>(filterToSingleEntity.entrySet());
		Entry<CallFilter, T> entryInspected;
        while ((entryInspected = filtersToMerge.pollFirst()) != null) {
            
            CallFilter filterInspected = entryInspected.getKey();
            T entityInspected          = entryInspected.getValue();
            //in order to store all Entities related to CallFilters that will be merged 
            //with filterInspected
            Set<T> entitiesInMerged = new HashSet<T>();
            entitiesInMerged.add(entityInspected);
            //we need the size of the deque to know when the current filterInspected 
            //will have been compared to all other CallFilters. Otherwise, as
            //Calls not merged with filterInspected are replaced at the end of the deque, 
            //we would try to merge them with filterInspected again.
            int size = filtersToMerge.size();
            if (log.isDebugEnabled()) {
                log.debug("Start merging for CallFilter {} associated with Entity {}. " +
            		"Number of CallFilters to be compared to: {}", 
                    filterInspected, entityInspected, size);
            }
            
            //compare to all other CallFilters
            for (int i = 0; i < size; i++) {
                Entry<CallFilter, T> tryToMerge = filtersToMerge.pollFirst();
                log.trace("Try to merge with CallFilter {} associated with Entity {}", 
                        tryToMerge.getKey(), tryToMerge.getValue());
                //apply the merge method depending on whether the CallFilters 
                //are associated to the same Entity, or different Entities. 
                CallFilter mergedFilter = null;
                if (entityInspected.equals(tryToMerge.getValue())) {
                    mergedFilter = filterInspected.mergeSameEntityCallFilter(
                            tryToMerge.getKey());
                } else {
                    mergedFilter = filterInspected.mergeDiffEntitiesCallFilter(
                            tryToMerge.getKey());
                }
                if (mergedFilter != null) {
                    //if merge successful
                    //will try to merge even further this merged CallFilter
                    filterInspected = mergedFilter;
                    //and we need to know all Entities related to the merged CallFiter
                    entitiesInMerged.add(tryToMerge.getValue());
                    log.trace("Merge successful");
                } else {
                    //otherwise, put back tryToMerge in the Deque, so that 
                    //it can latter be tried to merge it with other CallFilters.
                    filtersToMerge.offerLast(tryToMerge);
                    log.trace("Merge failed");
                }
            }
            //current filterInspected have been compared to all other CallFilters, 
            //and represents now a CallFilter that was merged as much as possible.
            if (log.isDebugEnabled()) {
                log.debug("Done merging for CallFilter {} associated with Entity {}. " +
                	"Resulting CallFilter: {} - associated with Entities: {}", 
            		entryInspected.getKey(), entryInspected.getValue(), filterInspected, 
            		entitiesInMerged);
            }
            filterToEntities.put(filterInspected, entitiesInMerged);
        }
        
        if (log.isDebugEnabled()) {
            log.debug("Done aggregation of CallFilers. {} resulting CallFilters merged.", 
                    filterToEntities.keySet().size());
        }
        
        return log.exit(filterToEntities);
	}
}
