package org.bgee.model.dao.api.expressiondata;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * A filter to parameterize expression data queries. 
 * 
 * @author  Valentine Rech de Laval
 * @version Bgee 14, Mar. 2017
 * @since   Bgee 14, Mar. 2017
 */
public class CallDataDAOFilter {

    public enum DataType {
        AFFYMETRIX, EST, IN_SITU, RNA_SEQ
    }

    /**
     * @see #isObservedDataOnly()
     */
    private final boolean observedDataOnly;
    /**
     * @see #getMinTotalPresentHigh()
     */
    private final int minTotalPresentHigh;
    /**
     * @see #getMinTotalPresentLow()
     */
    private final int minTotalPresentLow;
    /**
     * @see #getMinTotalAbsentHigh()
     */
    private final int minTotalAbsentHigh;
    /**
     * @see #getMinTotalAbsentLow()
     */
    private final int minTotalAbsentLow;
    /**
     * @see #getDataTypes()
     */
    private final Set<DataType> dataTypes;

    /**
     * Constructor accepting all requested parameters.
     * 
     * @param observedDataOnly      A {@code boolean} defining whether the filter keeps observed 
     *                              data only. If {@code true} it keeps observed data only.
     * @param minTotalPresentHigh   An {@code int} that is the minimum count showing expression 
     *                              with a high quality.
     * @param minTotalPresentLow    An {@code int} that is the minimum count showing expression 
     *                              with a low quality. 
     * @param minTotalAbsentHigh    An {@code int} that is the minimum count showing absence of
     *                              expression with a high quality. 
     * @param minTotalAbsentLow     An {@code int} that is the minimum count showing absence of
     *                              expression with a low quality. 
     * @param dataTypes             A {@code Collection} of {@code DataType}s that are data types
     *                              to filter expression queries. Can be {@code null} or empty.
     */
    public CallDataDAOFilter(boolean observedDataOnly, int minTotalPresentHigh, int minTotalPresentLow, 
        int minTotalAbsentHigh, int minTotalAbsentLow, Set<DataType> dataTypes) {
        this.observedDataOnly = observedDataOnly;
        this.minTotalPresentHigh = minTotalPresentHigh;
        this.minTotalPresentLow = minTotalPresentLow;
        this.minTotalAbsentHigh = minTotalAbsentHigh;
        this.minTotalAbsentLow = minTotalAbsentLow;
        this.dataTypes = Collections.unmodifiableSet(
                dataTypes == null? new HashSet<>(): new HashSet<>(dataTypes));
    }

    /**
     * @return  The {@code boolean} defining whether the filter keeps observed data only.
     *          If {@code true} it keeps observed data only.
     */
    public boolean isObservedData() {
        return observedDataOnly;
    }

    /**
     * @return  The {@code int} that is the minimum count showing expression with a high quality. 
     */
    public int getMinTotalPresentHigh() {
        return minTotalPresentHigh;
    }

    /**
     * @return  The {@code int} that is the minimum count showing expression with a low quality. 
     */
    public int getMinTotalPresentLow() {
        return minTotalPresentLow;
    }

    /**
     * @return  The {@code int} that is the minimum count showing absence of expression
     *          with a high quality. 
     */
    public int getMinTotalAbsentHigh() {
        return minTotalAbsentHigh;
    }

    /**
     * @return  The {@code int} that is the minimum count showing absence of expression
     *          with a low quality. 
     */
    public int getMinTotalAbsentLow() {
        return minTotalAbsentLow;
    }

    /**
     * @return  The unmodifiable {@code Set} of {@code DataType}s that are data types
     *          to filter expression queries. Can be {@code null} or empty.
     */
    public Set<DataType> getDataTypes() {
        return dataTypes;
    }

    @Override
    public String toString() {
        return "CallDataDAOFilter [observedDataOnly=" + observedDataOnly + 
            ", minTotalPresentHigh=" + minTotalPresentHigh + 
            ", minTotalPresentLow=" + minTotalPresentLow +
            ", minTotalAbsentHigh=" + minTotalAbsentHigh +
            ", minTotalAbsentLow=" + minTotalAbsentLow + ", dataTypes=" + dataTypes + "]";
    }
}
