package org.bgee.model.expressiondata.querytools;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bgee.model.expressiondata.querytools.filters.CallFilter;
import org.bgee.model.gene.Gene;

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
	 * Default constructor. 
	 */
    protected ExpressionQuery() {
    	super();
    }
    
    /**
	 * Aggregates as much as possible the {@link CallFilter}s associated with 
	 * the <code>Gene</code>s. The aim is to be able afterwards to obtain the data 
	 * from a <code>DAO</code> as easily as possible. This methods: 
	 * <ul>
	 * <li>tries, for each <code>Gene</code>, to merge as much as possible 
	 * all its related {@link CallFiler}s.
	 * <li>then, tries to merge equivalent <code>CallFiler</code>s of different 
	 * <code>Gene</code>s, so that a same <code>CallFiler</code> could be associated 
	 * to several <code>Gene</code>s.
	 * </ul>  
	 * After these operations, the query sent to the <code>DAO</code> should be 
	 * as simplified as possible. 
	 */
	private void aggregateCallFilters(Map<Gene, Collection<CallFilter>> genesWithCalls) {
		//first, we try to simplify gene by gene. We create a new Map.
		Map<Gene, Collection<CallFilter>> simplifiedGenes = 
				new HashMap<Gene, Collection<CallFilter>>();
		for (Entry<Gene, Collection<CallFilter>> entry: genesWithCalls.entrySet()) {
			Collection<CallFilter> simplifiedCalls = new ArrayList<CallFilter>();
			//use a deque to be able to replace a CallFilter in the deque,  
			//if it could not be merge with another CallFilter, to try to merge it 
			//with another CallFilter in the deque
			Deque<CallFilter> callsToMerge = new ArrayDeque<CallFilter>(entry.getValue());
			CallFilter callInspected;
			while ((callInspected = callsToMerge.pollFirst()) != null) {
				//we need the size of the deque to know when the current callInspected 
				//will have been compared to all other CallFilters. Otherwise, as
				//Calls not merged with callInspected are replaced at the end of the deque, 
				//we would try to merge them with the callInspected again.
				int size = callsToMerge.size();
				for (int i = 0; i < size; i++) {
					CallFilter tryToMerge = callsToMerge.pollFirst();
					CallFilter merged = callInspected.mergeSameGeneCallFilter(tryToMerge);
					TO CONTINUE
				}
			}
			
			
		}
	}
}
