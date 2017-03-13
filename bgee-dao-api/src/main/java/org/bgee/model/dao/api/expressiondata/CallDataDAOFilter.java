package org.bgee.model.dao.api.expressiondata;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A filter to parameterize expression data queries. 
 * 
 * @author  Valentine Rech de Laval
 * @author  Frederic Bastian
 * @version Bgee 14, Mar. 2017
 * @since   Bgee 14, Mar. 2017
 */
public class CallDataDAOFilter {
    private final static Logger log = LogManager.getLogger(CallDataDAOFilter.class.getName());

    public enum DataType {
        AFFYMETRIX, EST, IN_SITU, RNA_SEQ;
    }

    /**
     * @see #getExperimentCountFilters()
     */
    private final Set<DAOExperimentCountFilter> daoExperimentCountFilters;
    /**
     * @see #getDataTypes()
     */
    private final Set<DataType> dataTypes;

    /**
     * 
     * @param daoExperimentCountFilters If several {@code DAOExperimentCountFilter}s are provided,
     *                                  they are seen as "OR" conditions. Cannot be {@code null},
     *                                  empty, or to contain {@code null} elements.
     * @param dataTypes                 A {@code Set} of {@code DataType}s that are the data types
     *                                  which attributes will be sum up to match the provided
     *                                  {@code DAOExperimentCountFilter}s. If {@code null} or empty,
     *                                  then all data types are used.
     * @throws IllegalArgumentException If {@code dAOExperimentCountFilters} is {@code null}, empty,
     *                                  or contains {@code null} elements.
     */
    public CallDataDAOFilter(Collection<DAOExperimentCountFilter> daoExperimentCountFilters,
            Collection<DataType> dataTypes) throws IllegalArgumentException {
        log.entry(daoExperimentCountFilters, dataTypes);
        if (daoExperimentCountFilters == null || daoExperimentCountFilters.isEmpty() || 
                daoExperimentCountFilters.stream().anyMatch(e -> e == null)) {
            throw log.throwing(new IllegalArgumentException(
                    "Some ExperimentCountFilters must be provided and none can be null."));
        }
        this.dataTypes = Collections.unmodifiableSet(
                dataTypes == null || dataTypes.isEmpty()? EnumSet.allOf(DataType.class):
                    EnumSet.copyOf(dataTypes));
        this.daoExperimentCountFilters = new HashSet<>(daoExperimentCountFilters);
    }

    /**
     * @return      A {@code Set} of {@code DAOExperimentCountFilter}s to parameterize
     *              global expression queries. If several {@code DAOExperimentCountFilter}s are present,
     *              they are seen as "AND" conditions.
     * @see #getDataTypes()
     */
    public Set<DAOExperimentCountFilter> getExperimentCountFilters() {
        return daoExperimentCountFilters;
    }
    /**
     * @return      A {@code Set} of {@code DataType}s that are the data types which attributes
     *              will be sum up to match the provided {@code DAOExperimentCountFilter}s.
     * @see #getExperimentCountFilters()
     */
    public Set<DataType> getDataTypes() {
        return dataTypes;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((dataTypes == null) ? 0 : dataTypes.hashCode());
        result = prime * result + ((daoExperimentCountFilters == null) ? 0 : daoExperimentCountFilters.hashCode());
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
        CallDataDAOFilter other = (CallDataDAOFilter) obj;
        if (dataTypes == null) {
            if (other.dataTypes != null) {
                return false;
            }
        } else if (!dataTypes.equals(other.dataTypes)) {
            return false;
        }
        if (daoExperimentCountFilters == null) {
            if (other.daoExperimentCountFilters != null) {
                return false;
            }
        } else if (!daoExperimentCountFilters.equals(other.daoExperimentCountFilters)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("CallDataDAOFilter [dAOExperimentCountFilters=").append(daoExperimentCountFilters)
                .append(", dataTypes=").append(dataTypes).append("]");
        return builder.toString();
    }
}
