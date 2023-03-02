package org.bgee.model.dao.api.expressiondata.call;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.expressiondata.DAODataType;

public class DAOFDRPValueFilterBase<T extends Enum<T>> implements Comparable<DAOFDRPValueFilterBase<T>> {
    private final static Logger log = LogManager.getLogger(DAOFDRPValueFilterBase.class.getName());

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

    public static class DAOFDRPvalueFilterLinkedHashSetComparator
    extends DAOFDRPvalueFilterLinkedHashSetComparatorBase<ConditionDAO.Attribute, DAOFDRPValueFilter> {
        
    }
    public static class DAOFDRPvalueFilterLinkedHashSetComparator2
    extends DAOFDRPvalueFilterLinkedHashSetComparatorBase<ConditionDAO.ConditionParameter, DAOFDRPValueFilter2> {
        
    }
    public static class DAOFDRPvalueFilterLinkedHashSetComparatorBase<T extends Enum<T>,
    U extends DAOFDRPValueFilterBase<T>> implements Comparator<LinkedHashSet<U>> {
        @Override
        public int compare(LinkedHashSet<U> s1, LinkedHashSet<U> s2) {
            log.traceEntry("{}, {}", s1, s2);
            if (s1 == null || s2 == null) {
                throw log.throwing(new NullPointerException("None of the LinkedHashSets can be null"));
            }
            if (s1.equals(s2)) {
                return log.traceExit(0);
            }
            if (s1.size() < s2.size()) {
                return log.traceExit(-1);
            }
            if (s1.size() > s2.size()) {
                return log.traceExit(+1);
            }

            List<U> l1 = new ArrayList<>(s1);
            List<U> l2 = new ArrayList<>(s2);
            for (int i = 0; i < l1.size(); i++) {
                int compareFilter = l1.get(i).compareTo(l2.get(i));
                if (compareFilter != 0) {
                    return log.traceExit(compareFilter);
                }
            }
            throw log.throwing(new AssertionError("Unreachable code, " + s1 + " - " + s2));
        }
    }

    private final DAOFDRPValue fdrPValue;
    private final Qualifier qualifier;
    private final DAOPropagationState propagationState;
    private final boolean selfObservationRequired;
    //The following attribute is necessary only if selfObservationRequired is true
    private final EnumSet<T> condParams;
    private final Class<T> condParamType;

    public DAOFDRPValueFilterBase(BigDecimal fdrPValue, Collection<DAODataType> dataTypes,
            Qualifier qualifier, DAOPropagationState daoPropagationState, boolean selfObservationRequired,
            Collection<T> condParams, Class<T> condParamType) {
        this(new DAOFDRPValue(fdrPValue, dataTypes), qualifier, daoPropagationState,
                selfObservationRequired, condParams, condParamType);
    }
    //dependency injection of DAOFDRPValue rather than inheritance
    public DAOFDRPValueFilterBase(DAOFDRPValue fdrPValue, Qualifier qualifier,
            DAOPropagationState propagationState, boolean selfObservationRequired,
            Collection<T> condParams, Class<T> condParamType) throws IllegalArgumentException {
        log.traceEntry("{}, {}, {}, {}, {}, {}", fdrPValue, qualifier, propagationState, selfObservationRequired,
                condParams, condParamType);

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
        if (condParamType == null) {
            throw log.throwing(new IllegalArgumentException(
                    "The type of condition parameter must be provided"));
        }

        this.fdrPValue = fdrPValue;
        this.qualifier = qualifier;
        this.propagationState = propagationState;
        this.selfObservationRequired = selfObservationRequired;
        this.condParams = condParams == null || condParams.isEmpty()?
                EnumSet.allOf(condParamType): EnumSet.copyOf(condParams);
        this.condParamType = condParamType;

        log.traceExit();
    }

    /**
     * @return  The {@code DAOFDRPValue} used to identify valid FDR p-values of expression calls,
     *          in conjunction with the {@code Qualifier} returned by {@link #getQualifier()},
     *          and the {@code DAOPropagationState} returned by {@link #getPropagationState()}.
     */
    public DAOFDRPValue getPValue() {
        return this.fdrPValue;
    }
    /**
     * @return  A {@code Qualifier} specifying how the FDR p-values of retrieved calls
     *          should be relative to the FDR p-value returned by {@link #getPValue()}.
     */
    public Qualifier getQualifier() {
        return qualifier;
    }
    /**
     * @return  A {@code DAOPropagationState} representing the propagation state of the FDR p-values
     *          that should be compared to the FDR p-value returned by {@link #getPValue()}
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
     *          returned by {@link #getPValue()}#{@link DAOBigDecimalLinkedToDataTypes#getDataTypes()}
     *          must include observations in the condition itself. If {@link #getPropagationState()}
     *          is equal to {@code DAOPropagationState.DESCENDANT}, this {@code boolean} can only
     *          be equal to {@code false}.
     */
    public boolean isSelfObservationRequired() {
        return selfObservationRequired;
    }

    /**
     * @return  An {@code EnumSet} of condition parameters,
     *          used when {@link #isSelfObservationRequired()} returns {@code true} to determine
     *          the condition parameters to consider for the observation.
     */
    public EnumSet<T> getCondParams() {
        return condParams;
    }

    @Override
    public int hashCode() {
        return Objects.hash(condParams, fdrPValue,
                propagationState, qualifier, selfObservationRequired);
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DAOFDRPValueFilterBase<?> other = (DAOFDRPValueFilterBase<?>) obj;
        return Objects.equals(condParams, other.condParams)
                && Objects.equals(fdrPValue, other.fdrPValue)
                && propagationState == other.propagationState
                && qualifier == other.qualifier
                && selfObservationRequired == other.selfObservationRequired;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("DAOFDRPValueFilter [qualifier=").append(qualifier)
               .append(", FDRPValue=").append(fdrPValue)
               .append(", propagationState=").append(propagationState)
               .append(", selfObservationRequired=").append(selfObservationRequired)
               .append(", condParams=").append(condParams)
               .append("]");
        return builder.toString();
    }

    @Override
    public int compareTo(DAOFDRPValueFilterBase<T> o) {
        log.traceEntry("{}", 0);
        if (o == null) {
            throw new NullPointerException("The compared object cannot be null.");
        }
        if (this.equals(o)) {
            return log.traceExit(0);
        }
        int compareFDR = this.getPValue().compareTo(o.getPValue());
        if (compareFDR != 0) {
            return log.traceExit(compareFDR);
        }
        int compareQualifier = this.getQualifier().compareTo(o.getQualifier());
        if (compareQualifier != 0) {
            return log.traceExit(compareQualifier);
        }
        int comparePropagationState = this.getPropagationState().compareTo(o.getPropagationState());
        if (comparePropagationState != 0) {
            return log.traceExit(comparePropagationState);
        }
        if (this.isSelfObservationRequired() && !o.isSelfObservationRequired()) {
            return log.traceExit(-1);
        }
        if (!this.isSelfObservationRequired() && o.isSelfObservationRequired()) {
            return log.traceExit(+1);
        }
        if (!this.getCondParams().equals(o.getCondParams())) {
            for (T condParam: EnumSet.allOf(this.condParamType)) {
                if (this.getCondParams().contains(condParam) && !o.getCondParams().contains(condParam)) {
                    return log.traceExit(-1);
                } else if (o.getCondParams().contains(condParam) && !this.getCondParams().contains(condParam)) {
                    return log.traceExit(+1);
                }
            }
        }
        throw log.throwing(new AssertionError("Unreachable code: " + this + ", " + o));
    }

}
