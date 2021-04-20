package org.bgee.model.dao.api.expressiondata;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A filter to parameterize expression data queries based on data produced by each data type. 
 * 
 * @author  Valentine Rech de Laval
 * @author  Frederic Bastian
 * @version Bgee 15.0, Apr. 2021
 * @since   Bgee 14, Mar. 2017
 */
public class CallObservedDataDAOFilter {
    private final static Logger log = LogManager.getLogger(CallObservedDataDAOFilter.class.getName());

    /**
     * @see #getDataTypes()
     */
    private final Set<DAODataType> dataTypes;
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
        this.dataTypes = Collections.unmodifiableSet(
                dataTypes == null || dataTypes.isEmpty()? EnumSet.allOf(DAODataType.class):
                    EnumSet.copyOf(dataTypes));
        this.callObservedData = callObservedData;
        this.observedDataFilter = observedDataFilter == null? new LinkedHashMap<>():
            new LinkedHashMap<>(observedDataFilter);
    }

    /**
     * @return      A {@code Set} of {@code DAODataType}s that are the data types which attributes
     *              will be sum up to match the provided {@code DAOFDRPValueFilter}s.
     * @see #getExperimentCountFilters()
     */
    public Set<DAODataType> getDataTypes() {
        return dataTypes;
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
}
