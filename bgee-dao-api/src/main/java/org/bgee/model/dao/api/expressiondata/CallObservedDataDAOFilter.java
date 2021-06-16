package org.bgee.model.dao.api.expressiondata;

import java.util.Collection;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A filter to parameterize expression data queries based on data produced by each data type.
 * Implements {@code Comparable} for more consistent ordering when used in a {@link CallDAOFilter}
 * and improving chances of cache hit.
 * 
 * @author  Valentine Rech de Laval
 * @author  Frederic Bastian
 * @version Bgee 15.0, Jun. 2021
 * @since   Bgee 14, Mar. 2017
 */
public class CallObservedDataDAOFilter implements Comparable<CallObservedDataDAOFilter> {
    private final static Logger log = LogManager.getLogger(CallObservedDataDAOFilter.class.getName());

    /**
     * @see #getDataTypes()
     */
    private final EnumSet<DAODataType> dataTypes;
    /**
     * @see #getCallObservedData()
     */
    private final Boolean callObservedData;
    /**
     * @see #getObservedDataFilter()
     */
    private final LinkedHashMap<ConditionDAO.Attribute, Boolean> observedDataFilter;

    /**
     * @param dataTypes                 A {@code Collection} of {@code DAODataType}s that are the data types
     *                                  which attributes will be sum up to match the provided
     *                                  {@code DAOFDRPValueFilter}s. If {@code null} or empty,
     *                                  then all data types are used.
     * @param callObservedData          See {@link #getCallObservedData()}.
     * @param observedDataFilter        See {@link #getObservedDataFilter()}. If a key or a value is {@code null},
     *                                  an {@code IllegalArgumentException} is thrown.
     * @throws IllegalArgumentException If any of the {@code Set}s used in {@code dAOExperimentCountFilters}
     *                                  is {@code null}, empty, or contains {@code null} elements.
     */
    public CallObservedDataDAOFilter(Collection<DAODataType> dataTypes, Boolean callObservedData,
            Map<ConditionDAO.Attribute, Boolean> observedDataFilter) throws IllegalArgumentException {
        log.traceEntry("{}, {}, {}", dataTypes, callObservedData, observedDataFilter);
        if (observedDataFilter != null && observedDataFilter.entrySet().stream()
                .anyMatch(e -> e.getKey() == null || e.getValue() == null)) {
            throw log.throwing(new IllegalArgumentException("No ObservedData Entry can have null key or value"));
        }
        if (observedDataFilter != null && observedDataFilter.keySet().stream()
                .anyMatch(a -> !a.isConditionParameter())) {
            throw log.throwing(new IllegalArgumentException("Not a condition parameter in observedDataFilter."));
        }
        if (callObservedData == null && (observedDataFilter == null || observedDataFilter.isEmpty())) {
            throw log.throwing(new IllegalArgumentException("No filter provided in CallObservedDataDAOFilter"));
        }
        this.dataTypes = dataTypes == null || dataTypes.isEmpty()? EnumSet.allOf(DAODataType.class):
                    EnumSet.copyOf(dataTypes);
        this.callObservedData = callObservedData;
        this.observedDataFilter = observedDataFilter == null? new LinkedHashMap<>():
            //For having a predictable iteration order and improve cache hit
            observedDataFilter.entrySet().stream()
            .sorted(Comparator.comparing(e -> e.getKey()))
            .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue(),
                    (v1, v2) -> {throw log.throwing(new IllegalStateException("Impossible collision"));},
                    LinkedHashMap::new));
    }

    /**
     * @return      An {@code EnumSet} of {@code DAODataType}s that are the data types which attributes
     *              will be sum up to match the provided {@code DAOFDRPValueFilter}s.
     * @see #getExperimentCountFilters()
     */
    public EnumSet<DAODataType> getDataTypes() {
        //Defensive copying, no Collections.unmodifiableEnumSet
        return EnumSet.copyOf(dataTypes);
    }
    /**
     * @return  A {@code Boolean} defining a filtering on whether the call was observed in the condition,
     *          if not {@code null}. This is independent from {@link #getObservedDataFilter()} to be able
     *          to distinguish between whether data were observed in, for instance, the anatomical entity,
     *          and propagated along the dev. stage ontology. For instance, you might want to retrieve expression calls
     *          at a given dev. stage (using any propagation states), only if observed in the anatomical structure itself.
     *          The "callObservedData" filter does not permit solely to perform such a query.
     *          Note that this is simply a helper method and field as compared to using propagation states
     *          and "self" p-values.
     */
    //XXX: maybe to remove and always set appropriate observation state/"self" p-values
    public Boolean getCallObservedData() {
        return callObservedData;
    }
    /**
     * @return  A {@code LinkedHashMap} where keys are {@code ConditionDAO.Attribute}s that are 
     *          condition parameters (see {@link ConditionDAO.Attribute#isConditionParameter()}),
     *          the associated value being a {@code Boolean} indicating whether the retrieved data
     *          should have been observed in the specified condition parameter.
     *          The {@code Boolean} values are never {@code null}.
     *          Provided as a {@code LinkedHashMap} for convenience, to consistently set parameters
     *          in queries.
     *          The filtering used only the data types defined in this {@code CallObservedDataDAOFilter}.
     */
    public LinkedHashMap<ConditionDAO.Attribute, Boolean> getObservedDataFilter() {
        //defensive copying, no unmodifiable LinkedHashMap
        return new LinkedHashMap<>(observedDataFilter);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((dataTypes == null) ? 0 : dataTypes.hashCode());
        result = prime * result + ((callObservedData == null) ? 0 : callObservedData.hashCode());
        result = prime * result + ((observedDataFilter == null) ? 0 : observedDataFilter.hashCode());
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
        if (dataTypes == null) {
            if (other.dataTypes != null) {
                return false;
            }
        } else if (!dataTypes.equals(other.dataTypes)) {
            return false;
        }
        if (callObservedData == null) {
            if (other.callObservedData != null) {
                return false;
            }
        } else if (!callObservedData.equals(other.callObservedData)) {
            return false;
        }
        if (observedDataFilter == null) {
            if (other.observedDataFilter != null) {
                return false;
            }
        } else if (!observedDataFilter.equals(other.observedDataFilter)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("CallObservedDataDAOFilter [dataTypes=").append(dataTypes)
                .append(", callObservedData=").append(callObservedData)
                .append(", observedDataFilter=").append(observedDataFilter).append("]");
        return builder.toString();
    }

    @Override
    public int compareTo(CallObservedDataDAOFilter o) {
        log.traceEntry("{}", 0);
        if (o == null) {
            throw new NullPointerException("The compared object cannot be null.");
        }
        if (o.equals(this)) {
            return log.traceExit(0);
        }

        int compareDataType = (new DAODataType.DAODataTypeEnumSetComparator())
                .compare(this.getDataTypes(), o.getDataTypes());
        if (compareDataType != 0) {
            return log.traceExit(compareDataType);
        }

        if (!this.getObservedDataFilter().equals(o.getObservedDataFilter())) {
            if (this.getObservedDataFilter().size() < o.getObservedDataFilter().size()) {
                return log.traceExit(-1);
            } else if (this.getObservedDataFilter().size() > o.getObservedDataFilter().size()) {
                return log.traceExit(+1);
            }
            for (ConditionDAO.Attribute a: ConditionDAO.Attribute.values()) {
                Boolean thisValue = this.getObservedDataFilter().get(a);
                Boolean otherValue = o.getObservedDataFilter().get(a);
                if (Objects.equals(thisValue, otherValue)) {
                    continue;
                }
                if (thisValue != null && otherValue == null) {
                    return log.traceExit(-1);
                } else if (thisValue == null && otherValue != null) {
                    return log.traceExit(+1);
                } else if (thisValue && !otherValue) {
                    return log.traceExit(-1);
                } else if (!thisValue && otherValue) {
                    return log.traceExit(+1);
                }
                assert false: "Unreachable code, " + thisValue + " - " + otherValue;
            }
            assert false: "Unreachable code, " + this.getObservedDataFilter() + " - " + o.getObservedDataFilter();
        }

        if (!Objects.equals(this.getCallObservedData(), o.getCallObservedData())) {
            if (this.getCallObservedData() == null && o.getCallObservedData() != null) {
                return log.traceExit(-1);
            } else if (this.getCallObservedData() != null && o.getCallObservedData() == null) {
                return log.traceExit(+1);
            } else if (this.getCallObservedData() && !o.getCallObservedData()) {
                return log.traceExit(-1);
            } else if (!this.getCallObservedData() && o.getCallObservedData()) {
                return log.traceExit(+1);
            }
            assert false: "Unreachable code, " + this.getCallObservedData() + " - " + o.getCallObservedData();
        }

        throw log.throwing(new AssertionError("Unreachable code: " + this + ", " + o));
    }
}
