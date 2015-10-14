package org.bgee.model.expressiondata.querytool;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.Entity;
import org.bgee.model.QueryTool;
import org.bgee.model.expressiondata.Call;
import org.bgee.model.expressiondata.CallData;
import org.bgee.model.expressiondata.CallData.ExpressionCallData;
import org.bgee.model.expressiondata.baseelements.CallType;
import org.bgee.model.expressiondata.baseelements.SummaryCallType;
import org.bgee.model.expressiondata.baseelements.SummaryCallType.ExpressionSummary;

public class AnatEntityService extends QueryTool {
	/**
     * <code>Logger/code> of this class.
     */
    private final static Logger log = 
    		LogManager.getLogger(AnatEntityService.class.getName());
            
	/**
	 * Default constructor. 
	 */
    public AnatEntityService(String speciesId) {
        super();
    }

    @Override
    protected Logger getLogger() {
        return log;
    }
    
    public Map<String,String> getAnatEntities(){
        return null;
    }

    public Map<String,Set<String>> getAnatEntitiesRelationships(){
        return null;
    }
    
}
