package org.bgee.model.dao.api.expressiondata;

import java.math.BigDecimal;
import java.util.Collection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.expressiondata.DAOPropagationState;

/**
 * Class allowing to filter the retrieval of {@link GlobalExpressionCallTO}s
 * based on FDR p-values supporting the call.
 *
 * @author Frederic Bastian
 * @version Bgee 15.0, Apr. 2021
 * @see GlobalExpressionCallDAO
 * @since Bgee 15.0, Apr. 2021
 */
public class DAOFDRPValueFilter {
    private final static Logger log = LogManager.getLogger(DAOFDRPValueFilter.class.getName());

    public static enum Qualifier {
        EQUALS_TO("="), GREATER_THAN(">"), LESS_THAN("<"),
        GREATER_THAN_OR_EQUALS_TO(">="), LESS_THAN_OR_EQUALS_TO("<=");

        private final String symbol;

        private Qualifier(String symbol) {
            this.symbol = symbol;
        }
        public String getSymbol() {
            return symbol;
        }
    }

    /**
     * Check whether {@code propagationState} is valid to be used in objects of this class.
     *
     * @param propagationState  A {@code DAOPropagationState} to be checked for validity.
     * @return                  {@code true} if {@code propagationState} is valid,
     *                          {@code false} otherwise.
     */
    public static boolean isValidPropagationState(DAOPropagationState propagationState) {
        log.traceEntry("{}", propagationState);
        if (!DAOPropagationState.SELF_AND_DESCENDANT.equals(propagationState) &&
                !DAOPropagationState.DESCENDANT.equals(propagationState)) {
            return log.traceExit(false);
        }
        return log.traceExit(true);
    }

    private final DAOFDRPValue fdrPValue;
    private final Qualifier qualifier;
    private final DAOPropagationState propagationState;
    private final boolean selfObservationRequired;

    public DAOFDRPValueFilter(BigDecimal fdrPValue, Collection<DAODataType> dataTypes,
            Qualifier qualifier, DAOPropagationState daoPropagationState, boolean selfObservationRequired) {
        this(new DAOFDRPValue(fdrPValue, dataTypes), qualifier, daoPropagationState, selfObservationRequired);
    }
    //dependency injection of DAOFDRPValue rather than inheritance
    public DAOFDRPValueFilter(DAOFDRPValue fdrPValue, Qualifier qualifier,
            DAOPropagationState propagationState, boolean selfObservationRequired)
                    throws IllegalArgumentException {
        log.traceEntry("{}, {}, {}", fdrPValue, qualifier, propagationState, selfObservationRequired);

        if (fdrPValue == null || qualifier == null) {
            throw log.throwing(new IllegalArgumentException("No argument can be null"));
        }
        if (!isValidPropagationState(propagationState)) {
            throw log.throwing(new IllegalArgumentException("Invalid DAOPropagationState: "
                    + propagationState));
        }
        if (DAOPropagationState.DESCENDANT.equals(propagationState) && selfObservationRequired) {
            throw log.throwing(new IllegalArgumentException(
                    "When creating a filter for a p-value in descendant conditions, "
                    + "selfObservationRequired can only be false"));
        }

        this.fdrPValue = fdrPValue;
        this.qualifier = qualifier;
        this.propagationState = propagationState;
        this.selfObservationRequired = selfObservationRequired;

        log.traceExit();
    }

    /**
     * @return  The {@code DAOFDRPValue} used to identify valid FDR p-values of expression calls,
     *          in conjunction with the {@code Qualifier} returned by {@link #getQualifier()},
     *          and the {@code DAOPropagationState} returned by {@link #getPropagationState()}.
     */
    public DAOFDRPValue getFDRPValue() {
        return this.fdrPValue;
    }
    /**
     * @return  A {@code Qualifier} specifying how the FDR p-values of retrieved calls
     *          should be relative to the FDR p-value returned by {@link #getFDRPValue()}.
     */
    public Qualifier getQualifier() {
        return qualifier;
    }
    /**
     * @return  A {@code DAOPropagationState} representing the propagation state of the FDR p-values
     *          that should be compared to the FDR p-value returned by {@link #getFDRPValue()}
     *          with {@code Qualifier} returned by {@link #getQualifier()}: either FDR p-values
     *          computed from all p-values for the gene in the condition itself
     *          and all its sub-conditions (if equal to {@code SELF_AND_DESCENDANT}),
     *          or FDR p-values computed from all p-values for the gene in the sub-conditions
     *          of the targeted condition.
     */
    public DAOPropagationState getPropagationState() {
        return this.propagationState;
    }
    /**
     * @return  A {@code boolean} defining whether observations from the requested data types
     *          returned by {@link #getFDRPValue()}#{@link DAOBigDecimalLinkedToDataTypes#getDataTypes()}
     *          must include observations in the condition itself. If {@link #getPropagationState()}
     *          is equal to {@code DAOPropagationState.DESCENDANT}, this {@code boolean} can only
     *          be equal to {@code false}.
     */
    public boolean isSelfObservationRequired() {
        return selfObservationRequired;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((fdrPValue == null) ? 0 : fdrPValue.hashCode());
        //manage pVal value of DAOFDRPValue directly here
        result = prime * result + ((fdrPValue == null) ? 0 : fdrPValue.getFdrPValue().hashCode());
        result = prime * result + ((qualifier == null) ? 0 : qualifier.hashCode());
        result = prime * result + ((propagationState == null) ? 0 : propagationState.hashCode());
        result = prime * result + (selfObservationRequired ? 1231 : 1237);
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
        DAOFDRPValueFilter other = (DAOFDRPValueFilter) obj;
        if (fdrPValue == null) {
            if (other.fdrPValue != null) {
                return false;
            }
        } else if (!fdrPValue.equals(other.fdrPValue)) {
            return false;
        }
        //manage pVal value of DAOFDRPValue directly here
        if (fdrPValue != null && fdrPValue.getFdrPValue().compareTo(other.fdrPValue.getFdrPValue()) != 0) {
            return false;
        }
        if (qualifier != other.qualifier) {
            return false;
        }
        if (propagationState != other.propagationState) {
            return false;
        }
        if (selfObservationRequired != other.selfObservationRequired) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("DAOFDRPValueFilter [qualifier=").append(qualifier)
               .append(", FDRPValue=").append(fdrPValue)
               .append(", propagationState=").append(propagationState)
               .append(", selfObservationRequired=").append(selfObservationRequired)
               .append("]");
        return builder.toString();
    }
}
