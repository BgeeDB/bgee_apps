package org.bgee.model.dao.api.expressiondata.call;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.EnumSet;
import java.util.stream.Collectors;

import org.bgee.model.dao.api.expressiondata.DAODataType;

/**
 * Class allowing to filter the retrieval of {@link GlobalExpressionCallTO}s
 * based on FDR p-values supporting the call.
 * Implements {@code Comparable} for more consistent ordering when used in a {@link CallDAOFilter}
 * and improving chances of cache hit.
 *
 * @author Frederic Bastian
 * @version Bgee 15.0, Dec. 2021
 * @see GlobalExpressionCallDAO
 * @since Bgee 15.0, Apr. 2021
 */
public class DAOFDRPValueFilter extends DAOFDRPValueFilterBase<ConditionDAO.Attribute> {

    public DAOFDRPValueFilter(BigDecimal fdrPValue, Collection<DAODataType> dataTypes,
            Qualifier qualifier, DAOPropagationState daoPropagationState, boolean selfObservationRequired,
            Collection<ConditionDAO.Attribute> condParams) {
        super(fdrPValue, dataTypes, qualifier, daoPropagationState, selfObservationRequired,
                condParams == null || condParams.isEmpty()?
                        EnumSet.allOf(ConditionDAO.Attribute.class).stream()
                            .filter(a -> a.isConditionParameter())
                            .collect(Collectors.toSet()):
                        condParams,
                ConditionDAO.Attribute.class);
    }
    //dependency injection of DAOFDRPValue rather than inheritance
    public DAOFDRPValueFilter(DAOFDRPValue fdrPValue, Qualifier qualifier,
            DAOPropagationState propagationState, boolean selfObservationRequired,
            Collection<ConditionDAO.Attribute> condParams) throws IllegalArgumentException {
        super(fdrPValue, qualifier, propagationState, selfObservationRequired,
                condParams == null || condParams.isEmpty()?
                        EnumSet.allOf(ConditionDAO.Attribute.class).stream()
                            .filter(a -> a.isConditionParameter())
                            .collect(Collectors.toSet()):
                        condParams,
                ConditionDAO.Attribute.class);
    }
}
