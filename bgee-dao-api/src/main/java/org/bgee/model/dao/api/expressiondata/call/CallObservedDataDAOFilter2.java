package org.bgee.model.dao.api.expressiondata.call;

import java.util.Collection;

import org.bgee.model.dao.api.expressiondata.DAODataType;
import org.bgee.model.dao.api.expressiondata.call.ConditionDAO.ConditionParameter;

public class CallObservedDataDAOFilter2 extends CallObservedDataDAOFilterBase<ConditionDAO.ConditionParameter> {

    public CallObservedDataDAOFilter2(Collection<DAODataType> dataTypes,
            Collection<ConditionParameter> condParams, boolean callObservedData)
                    throws IllegalArgumentException {
        super(dataTypes, condParams, callObservedData, ConditionParameter.class);
    }
}
