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
import org.bgee.model.QueryTool;

/**
 * This class is extended by query tools, and is responsible for retrieving 
 * raw expression calls using {@code DAO}s. The reason why this class 
 * is {@code package-private}, and is not a query tool on its own, 
 * is that it will return {@code TransferObject}s from the {@code bgee-dao-api} 
 * modules, and we do not want to expose such classes to the public API. These 
 * {@code TransferObject}s will be processed by the child classes, and only 
 * processed results using classes from the {@code bgee-core} module 
 * will be exposed. 
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
//XXX: rename "ExpressionQueryTool" or "ExpressionQueryEngine"?
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
    
    //XXX: are we going to accept several CallFilters? E.g.: 
    //genes expressed in organA or organB and over-expressed in organ C
    
//    /**
//	 * Aggregates as much as possible the {@link CallQueryFilter}s associated with 
//	 * the {@code Entity}s. It tries to merge all {@code CallQueryFilter}s, 
//	 * whether they are associated with a same {@code Entity}, or different 
//	 * {@code Entity}s. The result will be that a {@code CallQueryFilter} 
//	 * could be associated with several {@code Entity}s. This methods returns 
//	 * a {@code Map} where keys are {@code CallQueryFilter}s merged as much 
//	 * as possible, and associated values are {@code Collection}s of 
//	 * {@code Entity}s. The aim is to be able afterwards to obtain the data 
//	 * from a {@code DAO} as easily as possible. 
//	 * 
//	 * @param entitiesWithFilters  a {@code Map} associating {@code Entity}s 
//	 *                             to a {@code Collection} of {@code CallQueryFilter}s.
//	 * @return     A {@code Map} where keys are {@code CallQueryFilter}s that 
//	 *             were merged as much as possible, associated with their related 
//	 *             {@code Entity}s.
//	 * @param T    The precise type of {@code Entity} used.
//	 */
//	protected <T extends Entity> Map<CallQueryFilter, Set<T>> aggregateCallFilters(
//	        Map<T, Collection<CallQueryFilter>> entitiesWithFilters) {
//		log.entry(entitiesWithFilters);
//		
//		if (log.isDebugEnabled()) {
//		    log.debug("Start ggregation of CallFilters. {} Entities to consider.", 
//		            entitiesWithFilters.keySet().size());
//		}
//		//try to merge all CallFilters, whether they are associated with a same Entity, 
//		//or different Entities. The result will be that a CallQueryFilter will be 
//		//associated with several Entities.
//		Map<CallQueryFilter, Set<T>> filterToEntities = new HashMap<CallQueryFilter, Set<T>>();
//		
//		//first we create a HashMap to store the association CallQueryFilter -> Entity
//		//(for now a CallQueryFilter is associated to at most 1 Entity, while 1 Entity 
//		//can be associated to several CallFilters). Of note, CallFilters do not 
//		//implement hashCode or equals, so that all CallFilters will be considered 
//		//unique at this point.
//		Map<CallQueryFilter, T> filterToSingleEntity = new HashMap<CallQueryFilter, T>();
//        for (Entry<T, Collection<CallQueryFilter>> entry: entitiesWithFilters.entrySet()) {
//            for (CallQueryFilter filter: entry.getValue()) {
//                filterToSingleEntity.put(filter, entry.getKey());
//            }
//        }
//		//now we create a Deque containing all CallFilters associated with their 
//		//related Entity. Then it will be possible to replace a CallQueryFilter in the deque,  
//        //if it could not be merge with another CallQueryFilter, to try to merge it 
//        //with another CallQueryFilter in the deque
//		Deque<Entry<CallQueryFilter, T>> filtersToMerge = 
//		        new ArrayDeque<Entry<CallQueryFilter, T>>(filterToSingleEntity.entrySet());
//		Entry<CallQueryFilter, T> entryInspected;
//        while ((entryInspected = filtersToMerge.pollFirst()) != null) {
//            
//            CallQueryFilter filterInspected = entryInspected.getKey();
//            T entityInspected          = entryInspected.getValue();
//            //in order to store all Entities related to CallFilters that will be merged 
//            //with filterInspected
//            Set<T> entitiesInMerged = new HashSet<T>();
//            entitiesInMerged.add(entityInspected);
//            //we need the size of the deque to know when the current filterInspected 
//            //will have been compared to all other CallFilters. Otherwise, as
//            //Calls not merged with filterInspected are replaced at the end of the deque, 
//            //we would try to merge them with filterInspected again.
//            int size = filtersToMerge.size();
//            if (log.isDebugEnabled()) {
//                log.debug("Start merging for CallQueryFilter {} associated with Entity {}. " +
//            		"Number of CallFilters to be compared to: {}", 
//                    filterInspected, entityInspected, size);
//            }
//            
//            //compare to all other CallFilters
//            for (int i = 0; i < size; i++) {
//                Entry<CallQueryFilter, T> tryToMerge = filtersToMerge.pollFirst();
//                log.trace("Try to merge with CallQueryFilter {} associated with Entity {}", 
//                        tryToMerge.getKey(), tryToMerge.getValue());
//                //apply the merge method depending on whether the CallFilters 
//                //are associated to the same Entity, or different Entities. 
//                CallQueryFilter mergedFilter = null;
//                if (entityInspected.equals(tryToMerge.getValue())) {
//                    mergedFilter = filterInspected.mergeSameEntityCallFilter(
//                            tryToMerge.getKey());
//                } else {
//                    mergedFilter = filterInspected.mergeDiffEntitiesCallFilter(
//                            tryToMerge.getKey());
//                }
//                if (mergedFilter != null) {
//                    //if merge successful
//                    //will try to merge even further this merged CallQueryFilter
//                    filterInspected = mergedFilter;
//                    //and we need to know all Entities related to the merged CallFiter
//                    entitiesInMerged.add(tryToMerge.getValue());
//                    log.trace("Merge successful");
//                } else {
//                    //otherwise, put back tryToMerge in the Deque, so that 
//                    //it can latter be tried to merge it with other CallFilters.
//                    filtersToMerge.offerLast(tryToMerge);
//                    log.trace("Merge failed");
//                }
//            }
//            //current filterInspected have been compared to all other CallFilters, 
//            //and represents now a CallQueryFilter that was merged as much as possible.
//            if (log.isDebugEnabled()) {
//                log.debug("Done merging for CallQueryFilter {} associated with Entity {}. " +
//                	"Resulting CallQueryFilter: {} - associated with Entities: {}", 
//            		entryInspected.getKey(), entryInspected.getValue(), filterInspected, 
//            		entitiesInMerged);
//            }
//            filterToEntities.put(filterInspected, entitiesInMerged);
//        }
//        
//        if (log.isDebugEnabled()) {
//            log.debug("Done aggregation of CallFilers. {} resulting CallFilters merged.", 
//                    filterToEntities.keySet().size());
//        }
//        
//        return log.exit(filterToEntities);
//	}
}
