package org.bgee.model.dao.api.expressiondata.call;

import java.math.BigDecimal;
import java.util.Collection;

import org.bgee.model.dao.api.expressiondata.DAODataType;

public class DAOFDRPValueFilter2 extends DAOFDRPValueFilterBase<ConditionDAO.ConditionParameter> {
    public DAOFDRPValueFilter2(BigDecimal fdrPValue, Collection<DAODataType> dataTypes,
            Qualifier qualifier, DAOPropagationState daoPropagationState, boolean selfObservationRequired,
            Collection<ConditionDAO.ConditionParameter> condParams) {
        super(fdrPValue, dataTypes, qualifier, daoPropagationState, selfObservationRequired,
                condParams, ConditionDAO.ConditionParameter.class);
    }
    //dependency injection of DAOFDRPValue rather than inheritance
    public DAOFDRPValueFilter2(DAOFDRPValue fdrPValue, Qualifier qualifier,
            DAOPropagationState propagationState, boolean selfObservationRequired,
            Collection<ConditionDAO.ConditionParameter> condParams) throws IllegalArgumentException {
        super(fdrPValue, qualifier, propagationState, selfObservationRequired,
                condParams, ConditionDAO.ConditionParameter.class);
    }
}
