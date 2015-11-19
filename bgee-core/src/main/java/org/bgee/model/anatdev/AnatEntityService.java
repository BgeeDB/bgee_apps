package org.bgee.model.anatdev;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.Entity;
import org.bgee.model.QueryTool;
import org.bgee.model.Service;
import org.bgee.model.dao.api.DAOManager;
import org.bgee.model.expressiondata.Call;
import org.bgee.model.expressiondata.CallData;
import org.bgee.model.expressiondata.CallData.ExpressionCallData;
import org.bgee.model.expressiondata.baseelements.CallType;
import org.bgee.model.expressiondata.baseelements.SummaryCallType;
import org.bgee.model.expressiondata.baseelements.SummaryCallType.ExpressionSummary;

public class AnatEntityService extends Service {
    private final static Logger log = LogManager.getLogger(AnatEntityService.class.getName());

    /**
     * 0-arg constructor that will cause this {@code AnatEntityService} to use 
     * the default {@code DAOManager} returned by {@link DAOManager#getDAOManager()}. 
     * 
     * @see #CallService(DAOManager)
     */
    public AnatEntityService() {
        this(DAOManager.getDAOManager());
    }
    /**
     * @param daoManager    The {@code DAOManager} to be used by this {@code AnatEntityService} 
     *                      to obtain {@code DAO}s.
     * @throws IllegalArgumentException If {@code daoManager} is {@code null}.
     */
    public AnatEntityService(DAOManager daoManager) {
        super(daoManager);
    }
    
    public Stream<AnatEntity> getAnatEntities(String speciesId) {
        return null;
    }

    public Map<String,Set<String>> getAnatEntitiesRelationships(String speciesId){
        return null;
    }
    
}
