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
//XXX: A Service exposing SummaryCalls only?
class CallService extends QueryTool {
	/**
     * <code>Logger/code> of this class.
     */
    private final static Logger log = 
    		LogManager.getLogger(CallService.class.getName());
    
    //XXX: Enum class for fields of Call to populate? 
    //(e.g., GENE, ANAT_ENTITY, STAGE, DATA). But this means that we once again 
    //"duplicate" the concepts in the Condition class. 
    
	/**
	 * Default constructor. 
	 */
    protected CallService() {
    	super();
    }
    
    
}
