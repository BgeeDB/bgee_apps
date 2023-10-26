package org.bgee.model.dao.api.expressiondata.call;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.expressiondata.DAODataType;

public class CallObservedDataDAOFilterBase<T extends Enum<T>>
implements Comparable<CallObservedDataDAOFilterBase<T>> {
    private final static Logger log = LogManager.getLogger(CallObservedDataDAOFilterBase.class.getName());

    /**
     * @see #getDataTypes()
     */
    private final EnumSet<DAODataType> dataTypes;
    /**
     * @see #getCondParams()
     */
    private final EnumSet<T> condParams;
    private final Class<T> condParamType;
    /**
     * @see #isCallObservedData()
     */
    private final boolean callObservedData;

    /**
     * @param dataTypes                 A {@code Collection} of {@code DAODataType}s that are the data types
     *                                  considered to check whether a call was observed or not.
     *                                  If {@code null} or empty, all data types are considered.
     * @param condParams                A {@code Collection} of condition parameters
     *                                  specifying which condition parameters to consider to determine
     *                                  whether the call was observed or not.
     *                                  If {@code null} or empty, all condition parameters are considered.
     * @param callObservedData          A {@code boolean} defining whether this filter will allow
     *                                  to retrieve calls that have been observed (if {@code true})
     *                                  or not observed (if {@code false}).
     * @throws IllegalArgumentException If {@code condParams} contains a {@code ConditionDAO.Attribute}
     *                                  that is not a condition parameter.
     */
    public CallObservedDataDAOFilterBase(Collection<DAODataType> dataTypes,
            Collection<T> condParams, boolean callObservedData, Class<T> condParamType)
                    throws IllegalArgumentException {
        log.traceEntry("{}, {}, {}", dataTypes, condParams, callObservedData);
        if (condParamType == null) {
            throw log.throwing(new IllegalArgumentException(
                    "The type of condition parameter must be provided"));
        }
        this.dataTypes = dataTypes == null || dataTypes.isEmpty()? EnumSet.allOf(DAODataType.class):
                    EnumSet.copyOf(dataTypes);
        this.condParams = condParams == null || condParams.isEmpty()?
                EnumSet.allOf(condParamType): EnumSet.copyOf(condParams);
        this.callObservedData = callObservedData;
        this.condParamType = condParamType;
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
     * @return  An {@code EnumSet} of condition parameters
     *          that will be considered to determine whether the call was observed or not
     *          (depending on {@link #isCallObservedData()}. For instance, you might want
     *          to retrieve expression calls at a given dev. stage (using any propagation states),
     *          only if observed in the anatomical entity itself.
     */
    public EnumSet<T> getCondParams() {
        //Defensive copying, no Collections.unmodifiableEnumSet
        return EnumSet.copyOf(condParams);
    }

    @Override
    public int hashCode() {
        return Objects.hash(callObservedData, condParams, dataTypes);
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        CallObservedDataDAOFilterBase<?> other = (CallObservedDataDAOFilterBase<?>) obj;
        return callObservedData == other.callObservedData
                && Objects.equals(condParams, other.condParams)
                && Objects.equals(dataTypes, other.dataTypes);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("CallObservedDataDAOFilter [")
               .append("dataTypes=").append(dataTypes)
               .append(", condParams=").append(condParams)
               .append(", callObservedData=").append(callObservedData)
               .append("]");
        return builder.toString();
    }

    @Override
    public int compareTo(CallObservedDataDAOFilterBase<T> o) {
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

        int compareComdParams = DAO.compareEnumSets(this.getCondParams(), o.getCondParams(),
                this.condParamType);
        return log.traceExit(compareComdParams);
    }
}
