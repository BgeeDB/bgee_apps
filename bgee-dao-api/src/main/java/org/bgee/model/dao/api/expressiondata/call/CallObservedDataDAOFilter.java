package org.bgee.model.dao.api.expressiondata.call;

import java.util.Collection;
import java.util.EnumSet;
import java.util.stream.Collectors;

import org.bgee.model.dao.api.expressiondata.DAODataType;

/**
 * A filter to request expression calls based on their observation status in a condition:
 * whether the call has been directly observed in a condition, or propagated from some descendant
 * conditions of the considered condition. This filter accepts the data types to consider
 * (for instance, whether the call was observed from Affymetrix and/or RNA-Seq data)
 * and the condition parameters to consider (for instance, whether the call was observed
 * in an anat. entity, while accepting that it might have been propagated along the dev. stage
 * ontology).
 * <p>
 * Implements {@code Comparable} for more consistent ordering when used in a {@link CallDAOFilter}
 * and improving chances of cache hit.
 * 
 * @author  Valentine Rech de Laval
 * @author  Frederic Bastian
 * @version Bgee 15.0, Jul. 2021
 * @since   Bgee 14, Mar. 2017
 */
public class CallObservedDataDAOFilter extends CallObservedDataDAOFilterBase<ConditionDAO.Attribute> {

    /**
     * @param dataTypes                 A {@code Collection} of {@code DAODataType}s that are the data types
     *                                  considered to check whether a call was observed or not.
     *                                  If {@code null} or empty, all data types are considered.
     * @param condParams                A {@code Collection} of {@code ConditionDAO.Attribute}s
     *                                  that are condition parameters (see {@link
     *                                  org.bgee.model.dao.api.expressiondata.call.ConditionDAO.Attribute
     *                                  #isConditionParameter() ConditionDAO.Attribute#isConditionParameter()})
     *                                  specifying which condition parameters to consider to determine
     *                                  whether the call was observed or not.
     *                                  If {@code null} or empty, all condition parameters are considered.
     * @param callObservedData          A {@code boolean} defining whether this filter will allow
     *                                  to retrieve calls that have been observed (if {@code true})
     *                                  or not observed (if {@code false}).
     * @throws IllegalArgumentException If {@code condParams} contains a {@code ConditionDAO.Attribute}
     *                                  that is not a condition parameter.
     */
    public CallObservedDataDAOFilter(Collection<DAODataType> dataTypes,
            Collection<ConditionDAO.Attribute> condParams, boolean callObservedData)
                    throws IllegalArgumentException {
        super(dataTypes,
                condParams == null || condParams.isEmpty()?
                        EnumSet.allOf(ConditionDAO.Attribute.class).stream()
                            .filter(a -> a.isConditionParameter())
                            .collect(Collectors.toSet()):
                        condParams,
                callObservedData,
                ConditionDAO.Attribute.class);
        if (this.getCondParams().stream().anyMatch(a -> !a.isConditionParameter())) {
            throw new IllegalArgumentException("Not a condition parameter in condParams.");
        }
    }
}