package org.bgee.model.dao.api.expressiondata.call;

import java.util.Collection;
import java.util.EnumSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
public class CallObservedDataDAOFilter implements Comparable<CallObservedDataDAOFilter> {
    private final static Logger log = LogManager.getLogger(CallObservedDataDAOFilter.class.getName());

    /**
     * @see #getDataTypes()
     */
    private final EnumSet<DAODataType> dataTypes;
    /**
     * @see #getCondParams()
     */
    private final EnumSet<ConditionDAO.Attribute> condParams;
    /**
     * @see #isCallObservedData()
     */
    private final boolean callObservedData;

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
        log.traceEntry("{}, {}, {}", dataTypes, condParams, callObservedData);
        if (condParams != null && condParams.stream().anyMatch(a -> !a.isConditionParameter())) {
            throw log.throwing(new IllegalArgumentException("Not a condition parameter in condParams."));
        }
        this.dataTypes = dataTypes == null || dataTypes.isEmpty()? EnumSet.allOf(DAODataType.class):
                    EnumSet.copyOf(dataTypes);
        this.condParams = condParams == null || condParams.isEmpty()?
                EnumSet.allOf(ConditionDAO.Attribute.class): EnumSet.copyOf(condParams);
        this.callObservedData = callObservedData;
    }

    /**
     * @return      An {@code EnumSet} of {@code DAODataType}s that are the data types
     *              that will be considered to determine whether the call was observed or not
     *              (depending on {@link #isCallObservedData()}.
     */
    public EnumSet<DAODataType> getDataTypes() {
        //Defensive copying, no Collections.unmodifiableEnumSet
        return EnumSet.copyOf(dataTypes);
    }
    /**
     * @return  A {@code Boolean} defining a filtering on whether the call was observed in the condition.
     *          This is independent from {@link #getObservedDataFilter()} to be able
     *          to distinguish between whether data were observed in, for instance, the anatomical entity,
     *          and propagated along the dev. stage ontology. For instance, you might want to retrieve expression calls
     *          at a given dev. stage (using any propagation states), only if observed in the anatomical structure itself.
     *          The "callObservedData" filter does not permit solely to perform such a query.
     *          Note that this is simply a helper method and field as compared to using propagation states
     *          and "self" p-values.
     */
    public boolean isCallObservedData() {
        return callObservedData;
    }
    /**
     * @return  An {@code EnumSet} of {@code ConditionDAO.Attribute}s that are the condition parameters
     *          that will be considered to determine whether the call was observed or not
     *          (depending on {@link #isCallObservedData()}. For instance, you might want
     *          to retrieve expression calls at a given dev. stage (using any propagation states),
     *          only if observed in the anatomical entity itself.
     */
    public EnumSet<ConditionDAO.Attribute> getCondParams() {
        //Defensive copying, no Collections.unmodifiableEnumSet
        return EnumSet.copyOf(condParams);
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (callObservedData ? 1231 : 1237);
        result = prime * result + ((condParams == null) ? 0 : condParams.hashCode());
        result = prime * result + ((dataTypes == null) ? 0 : dataTypes.hashCode());
        return result;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        CallObservedDataDAOFilter other = (CallObservedDataDAOFilter) obj;
        if (callObservedData != other.callObservedData) {
            return false;
        }
        if (condParams == null) {
            if (other.condParams != null) {
                return false;
            }
        } else if (!condParams.equals(other.condParams)) {
            return false;
        }
        if (dataTypes == null) {
            if (other.dataTypes != null) {
                return false;
            }
        } else if (!dataTypes.equals(other.dataTypes)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("CallObservedDataDAOFilter [dataTypes=").append(dataTypes)
               .append(", condParams=").append(condParams)
               .append(", callObservedData=").append(callObservedData).append("]");
        return builder.toString();
    }

    @Override
    public int compareTo(CallObservedDataDAOFilter o) {
        log.traceEntry("{}", o);
        if (o == null) {
            throw new NullPointerException("The compared object cannot be null.");
        }
        if (o.equals(this)) {
            return log.traceExit(0);
        }

        if (this.isCallObservedData() && !o.isCallObservedData()) {
            return log.traceExit(-1);
        }
        if (!this.isCallObservedData() && o.isCallObservedData()) {
            return log.traceExit(+1);
        }

        int compareDataType = (new DAODataType.DAODataTypeEnumSetComparator())
                .compare(this.getDataTypes(), o.getDataTypes());
        if (compareDataType != 0) {
            return log.traceExit(compareDataType);
        }

        int compareComdParams = (new ConditionDAO.CondParamEnumSetComparator())
                .compare(this.getCondParams(), o.getCondParams());
        return log.traceExit(compareComdParams);
    }
}