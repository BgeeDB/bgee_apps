package org.bgee.model.expressiondata;

import java.math.BigDecimal;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.expressiondata.baseelements.ExperimentExpressionCount;
import org.bgee.model.expressiondata.baseelements.CallType;
import org.bgee.model.expressiondata.baseelements.DataPropagation;
import org.bgee.model.expressiondata.baseelements.CallType.DiffExpression;
import org.bgee.model.expressiondata.baseelements.CallType.Expression;
import org.bgee.model.expressiondata.baseelements.PropagationState;
import org.bgee.model.expressiondata.baseelements.DataQuality;
import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.model.expressiondata.baseelements.DiffExpressionFactor;

/**
 * A {@code CallData} represents the expression state of a {@link Gene} in a {@link Condition}
 * computed from a specific {@code DataType}, as part of a {@link Call}. 
 * This class only manages the expression state part, not the spatio-temporal location, 
 * or gene definition part. It represents the expression state of a baseline present/absent call, 
 * or a differential expression call; a call represents an overall summary 
 * of the expression data contained in Bgee (for instance, the expression state of a gene 
 * summarized over all Affymetrix chips studied in a given organ at a given stage).
 * <p>
 * For a class also managing the gene and condition definitions, and managing 
 * expression data from different data types for a given call, see the class {@link Call}. 
 * 
 * @author  Frederic Bastian
 * @author  Valentine Rech de Laval
 * @version Bgee 14, Mar. 2017
 * @since   Bgee 13, Sept. 2015
 */
//TODO: javadoc of all attributes and methods
public abstract class CallData<T extends Enum<T> & CallType> {
    private final static Logger log = LogManager.getLogger(CallData.class.getName());

    //**********************************************
    //   INNER CLASSES
    //**********************************************

    //XXX: attributes to be added in the future: min p-value, min/max fold change, ...
    //XXX: where to manage the DiffExpressionFactor? Here, or only in a "Call" class? 
    //But then, we could not use this CallData in query filters to specify the factor to use.
    public static class DiffExpressionCallData extends CallData<DiffExpression> {
        //XXX: I'm not very happy about this field, as it is redundant as compared to the field in 
        //DiffExpressionCall, and as it is not something specific to a data type, 
        //which is what this class is supposed to be about.
        //This field was created only to be able to parameterize queries to a CallService, 
        //though a CallFilter, to request diff. expression calls produced from analyzes 
        //over anatomy, and/or over development.
        //But maybe we can argue that it is always useful to be able to know from which type 
        //of analysis a DiffExpressionCallData comes from...
        private final DiffExpressionFactor diffExpressionFactor;

        private final DataQuality dataQuality;

        public DiffExpressionCallData(DiffExpressionFactor factor, DiffExpression callType) {
            this(factor, callType, null);
        }
        public DiffExpressionCallData(DiffExpressionFactor factor, DiffExpression callType, 
                DataType dataType) {
            this(factor, callType, DataQuality.LOW, dataType);
        }
        public DiffExpressionCallData(DiffExpressionFactor factor, DiffExpression callType, 
                DataQuality dataQual, DataType dataType) {
            this(factor, callType, dataQual, dataType,
                    new DataPropagation(PropagationState.SELF, PropagationState.SELF, true));
        }
        public DiffExpressionCallData(DiffExpressionFactor factor, DiffExpression callType, 
                DataQuality dataQual, DataType dataType, DataPropagation dataPropagation) {
            super(dataType, callType, dataPropagation);

            this.dataQuality = dataQual;
            this.diffExpressionFactor = factor;
        }
        
        public DiffExpressionFactor getDiffExpressionFactor() {
            return diffExpressionFactor;
        }
        
        //XXX: this will certainly change as in ExpressionCallData
        public DataQuality getDataQuality() {
            return dataQuality;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = super.hashCode();
            result = prime * result + ((dataQuality == null) ? 0 : dataQuality.hashCode());
            result = prime * result + ((diffExpressionFactor == null) ? 0 : diffExpressionFactor.hashCode());
            return result;
        }
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!super.equals(obj)) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            DiffExpressionCallData other = (DiffExpressionCallData) obj;
            if (dataQuality != other.dataQuality) {
                return false;
            }
            if (diffExpressionFactor != other.diffExpressionFactor) {
                return false;
            }
            return true;
        }
        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("DiffExpressionCallData [diffExpressionFactor=").append(diffExpressionFactor)
                   .append(", dataQuality=").append(dataQuality)
                   .append(", dataType=").append(getDataType())
                   .append(", callType=").append(getCallType())
                   .append(", dataPropagation=").append(getDataPropagation()).append("]");
            return builder.toString();
        }

    }

    //TODO: javadoc of all methods and attributes
    public static class ExpressionCallData extends CallData<Expression> {
        //********************************************
        // STATIC ATTRIBUTES AND METHODS
        //********************************************
        /**
         * A {@code Map} where keys are {@code DataType}s, the associated value being
         * a {@code Set} of {@code ExperimentExpressionCount}s that are all the types
         * of {@code ExperimentExpressionCount}s that must be associated to this {@code DataType}.
         * {@link org.bgee.model.expressiondata.baseelements.ExperimentExpressionCount#getExperimentCount() 
         * ExperimentExpressionCount.getExperimentCount()} returns 0 for all
         * {@code ExperimentExpressionCount}s in these {@code Set}s.
         */
        private static final Map<DataType, Set<ExperimentExpressionCount>> VALID_EXP_COUNTS = 
            //we go through all combinations of DataType, CallType.Expression,
            //PropagationState, and DataQuality, to identify and store the valid ones.
            Collections.unmodifiableMap(EnumSet.allOf(DataType.class).stream()
            .flatMap(dataType -> EnumSet.allOf(CallType.Expression.class).stream()
                .filter(callType -> callType.isValidDataType(dataType))
                .flatMap(callType -> ExperimentExpressionCount.ALLOWED_PROP_STATES.stream()
                    .filter(propState -> callType.isValidPropagationState(propState))
                    .flatMap(propState -> EnumSet.allOf(DataQuality.class).stream()
                        .map(
                            dataQuality -> new AbstractMap.SimpleEntry<>(dataType, 
                                new ExperimentExpressionCount(callType, dataQuality, propState, 0))
                        )
                    )
                )
            ).collect(Collectors.groupingBy(e -> e.getKey(), 
                    Collectors.mapping(e -> e.getValue(), Collectors.toSet()))));

        /**
         * Computes the {@code CallType.Expression} that the {@code ExperimentExpressionCount}s
         * of this {@code CallData} allow to produce.
         * 
         * @param counts    A {@code Set} of {@code ExperimentExpressionCount}s producing
         *                  the {@code CallType.Expression}.
         * @return          The {@code CallType.Expression} inferred.
         * @throws IllegalArgumentException If {@code expCounts} do not allow to produce
         *                                  a {@code CallType.Expression}.
         */
        private static Expression inferCallType(Set<ExperimentExpressionCount> expCounts) {
            log.entry(expCounts);
            
            if (expCounts == null || expCounts.isEmpty()) {
                return log.exit(null);
            }
        
            Set<ExperimentExpressionCount> propAllPositiveCounts = expCounts.stream()
                    //we don't do sanity checks on null here, so we filter them out
                    //to not have a null pointer exception
                    .filter(c -> c != null && PropagationState.ALL.equals(c.getPropagationState()) && 
                            c.getCount() > 0)
                    .collect(Collectors.toSet());
            if (propAllPositiveCounts.isEmpty()) {
                throw log.throwing(new IllegalArgumentException("Inference of expression is not possible"
                    + " because all total experimentCounts are missing or equal to 0"));
            }
            
            if (propAllPositiveCounts.stream()
                    .anyMatch(c -> Expression.EXPRESSED.equals(c.getCallType()))) {
                return log.exit(Expression.EXPRESSED);
            }
            if (propAllPositiveCounts.stream()
                    .anyMatch(c -> Expression.NOT_EXPRESSED.equals(c.getCallType()))) {
                return log.exit(Expression.NOT_EXPRESSED);
            }
            //this point should be reached only if a new CallType.Expression is not supported here,
            //so it's an IllegalStateException, not an IllegalArgumentException
            throw log.throwing(new IllegalStateException(
                    "Could not infer CallType from ExperimentExpressionCount"));
        }

        //********************************************
        // INSTANCE ATTRIBUTES AND CONSTRUCTORS
        //********************************************
        private final Set<ExperimentExpressionCount> experimentCounts;
        
        private final int propagatedExperimentCount;

        private final BigDecimal rank;
        
        private final BigDecimal normalizedRank;
        
        private final BigDecimal weightForMeanRank;

        public ExpressionCallData(DataType dataType, Set<ExperimentExpressionCount> experimentCounts,
            int propagatedExperimentCount, BigDecimal rank, BigDecimal normalizedRank, BigDecimal weightForMeanRank,
            DataPropagation dataPropagation) {
            super(dataType, inferCallType(experimentCounts), dataPropagation);

            // sanity checks
            if (experimentCounts != null && !experimentCounts.isEmpty()) {
                if (experimentCounts.stream().anyMatch(c -> c == null)) {
                    throw log.throwing(new IllegalArgumentException(
                            "All ExpressionExperimentCounts must be not null."));
                }
                final Set<ExperimentExpressionCount> expectedExpCounts = VALID_EXP_COUNTS.get(dataType);
                //map experimentCounts to ExperimentExpressionCounts with count of 0 to compare
                //to the expected ExperimentExpressionCounts
                Set<ExperimentExpressionCount> fakeCounts = experimentCounts.stream()
                        .map(c -> new ExperimentExpressionCount(c.getCallType(), c.getDataQuality(), 
                                c.getPropagationState(), 0))
                        .collect(Collectors.toSet());
                if (!expectedExpCounts.containsAll(fakeCounts)) {
                    fakeCounts.removeAll(expectedExpCounts);
                    throw log.throwing(new IllegalArgumentException("Unexpected combinations of "
                            + "CallType/DataQuality/PropagationState in ExperimentExpressionCount. "
                            + "Unexpected counts: " + fakeCounts));
                }
            }

            this.experimentCounts = experimentCounts == null? new HashSet<>(): 
                Collections.unmodifiableSet(new HashSet<>(experimentCounts));
            this.propagatedExperimentCount = propagatedExperimentCount;
            //BigDecimal are immutable so we're good
            this.rank = rank;
            this.normalizedRank = normalizedRank;
            this.weightForMeanRank = weightForMeanRank;
        }


        //********************************************
        // INSTANCE METHODS
        //********************************************
//        public int getExperimentCounts() {
//            return experimentCounts.stream()
//                .filter(c -> PropagationState.SELF.equals(c.getKey().getPropagationState()) 
//                    && c.getValue() > 0)
//                .map(Entry::getValue)
//                .mapToInt(Integer::intValue)
//                .sum();
//        }
//
//        public int getAllTotalCount() {
//            return experimentCounts.entrySet().stream()
//                .filter(c -> PropagationState.ALL.equals(c.getKey().getPropagationState()) 
//                    && c.getValue() > 0)
//                .map(Entry::getValue)
//                .mapToInt(Integer::intValue)
//                .sum();
//        }

        //XXX: DataQual is now inferred only from the integration of all data types, 
        //so it doesn't make sense to have a quality score per data type. We only need experiment experimentCounts.
//        @Override
//        public DataQuality getDataQuality() {
//            log.entry();
//            if (this.getPresentHighTotalCount() > 0) {
//                return log.exit(DataQuality.HIGH);
//            }
//            if (this.getPresentLowTotalCount() > 0) {
//                return log.exit(DataQuality.LOW);
//            }
//            if (this.getAbsentHighTotalCount() > 0) {
//                return log.exit(DataQuality.HIGH);
//            }
//            if (this.getAbsentLowTotalCount() > 0) {
//                return log.exit(DataQuality.LOW);
//            }
//            return log.exit(DataQuality.NODATA);
//        }

        public ExperimentExpressionCount getExperimentCount(CallType.Expression callType,
                DataQuality dataQuality, PropagationState propState) {
            log.entry(callType, dataQuality, propState);
            if (callType == null || dataQuality == null || propState == null) {
                throw log.throwing(new IllegalArgumentException("No argument can be null."));
            }
            if (!ExperimentExpressionCount.ALLOWED_PROP_STATES.contains(propState)) {
                throw log.throwing(new IllegalArgumentException(
                        "The provided PropagationState is invalid for ExperimentExpressionCounts: "
                        + propState));
            }
            Set<ExperimentExpressionCount> matchingExpCounts = experimentCounts.stream()
                    .filter(c -> callType.equals(c.getCallType()) &&
                            dataQuality.equals(c.getDataQuality()) &&
                            propState.equals(c.getPropagationState()))
                    .collect(Collectors.toSet());
            if (matchingExpCounts.size() != 1) {
                throw log.throwing(new IllegalStateException(
                        "Could not find matching ExperimentExpressionCount for parameters: "
                        + callType + " - " + dataQuality + " - " + propState));
            }
            return log.exit(matchingExpCounts.iterator().next());
        }

        public Set<ExperimentExpressionCount> getExperimentCounts(PropagationState propState) {
            log.entry(propState);
            if (propState == null) {
                throw log.throwing(new IllegalArgumentException("PropagationState cannot be null."));
            }
            if (!ExperimentExpressionCount.ALLOWED_PROP_STATES.contains(propState)) {
                throw log.throwing(new IllegalArgumentException(
                        "The provided PropagationState is invalid for ExperimentExpressionCounts: "
                        + propState));
            }
            return log.exit(experimentCounts.stream()
                    .filter(c -> propState.equals(c.getPropagationState()))
                    .collect(Collectors.toSet()));
        }

        //********************************************
        // GETTERS
        //********************************************
        public Set<ExperimentExpressionCount> getExperimentCounts() {
            return experimentCounts;
        }
        public int getPropagatedExperimentCount() {
            return propagatedExperimentCount;
        }
        public BigDecimal getRank() {
            return rank;
        }
        public BigDecimal getNormalizedRank() {
            return normalizedRank;
        }
        public BigDecimal getWeightForMeanRank() {
            return weightForMeanRank;
        }


        //********************************************
        // hashCode/equals/toString
        //********************************************
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = super.hashCode();
            result = prime * result + ((experimentCounts == null) ? 0 : experimentCounts.hashCode());
            result = prime * result + propagatedExperimentCount;
            result = prime * result + ((rank == null) ? 0 : rank.hashCode());
            result = prime * result + ((normalizedRank == null) ? 0 : normalizedRank.hashCode());
            result = prime * result + ((weightForMeanRank == null) ? 0 : weightForMeanRank.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!super.equals(obj)) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            ExpressionCallData other = (ExpressionCallData) obj;
            if (experimentCounts == null) {
                if (other.experimentCounts != null) {
                    return false;
                }
            } else if (!experimentCounts.equals(other.experimentCounts)) {
                return false;
            }
            if (propagatedExperimentCount != other.propagatedExperimentCount) {
                return false;
            }
            if (rank == null) {
                if (other.rank != null) {
                    return false;
                }
            } else if (!rank.equals(other.rank)) {
                return false;
            }
            if (normalizedRank == null) {
                if (other.normalizedRank != null) {
                    return false;
                }
            } else if (!normalizedRank.equals(other.normalizedRank)) {
                return false;
            }
            if (weightForMeanRank == null) {
                if (other.weightForMeanRank != null) {
                    return false;
                }
            } else if (!weightForMeanRank.equals(other.weightForMeanRank)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("ExpressionCallData [dataType=").append(getDataType())
                   .append(", callType=").append(getCallType())
                   .append(", dataPropagation=").append(getDataPropagation())
                   .append(", experimentCounts=").append(experimentCounts)
                   .append(", propagatedExperimentCount=").append(propagatedExperimentCount)
                   .append(", rank=").append(rank)
                   .append(", normalizedRank=").append(normalizedRank)
                   .append(", weightForMeanRank=").append(weightForMeanRank).append("]");
            return builder.toString();
        }
    }


    //**********************************************
    //   INSTANCE ATTRIBUTES AND CONSTRUCTORS
    //**********************************************
    
    private final DataType dataType;
    
    private final T callType;

    //XXX:to move to ExpressionCall? (also in CallData, to be moved to ExpressionCallData?)
    private final DataPropagation dataPropagation;
    
    /**
     * Constructor allowing to specify a {@code DataType}. 
     * 
     * @param dataType  The {@code DataType} that allowed to generate the {@code CallType}.
     * @throws IllegalArgumentException    If {@code dataType} is not {@code null}.
     */
    protected CallData(DataType dataType, T callType, DataPropagation dataPropagation)
            throws IllegalArgumentException {
        log.entry(dataType, callType, dataPropagation);
        
        if (dataType == null) {
            throw log.throwing(new IllegalArgumentException
                ("A DataType must be defined to instantiate a CallData."));
        }
        if (callType != null) {
            callType.checkDataType(dataType);

            if (dataPropagation != null) {
                dataPropagation.getAllPropagationStates().stream()
                        .forEach(state -> callType.checkPropagationState(state));
            }
        }

        this.dataType = dataType;
        this.callType = callType;
        this.dataPropagation = dataPropagation;
        log.exit();
    }


    //**********************************************
    //   GETTERS
    //**********************************************
    public DataType getDataType() {
        return dataType;
    }
    public T getCallType() {
        return callType;
    }
    public DataPropagation getDataPropagation() {
        return dataPropagation;
    }


    //**********************************************
    //  hashCode/equals/toString
    //**********************************************
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((callType == null) ? 0 : callType.hashCode());
        result = prime * result + ((dataType == null) ? 0 : dataType.hashCode());
        result = prime * result + ((dataPropagation == null) ? 0 : dataPropagation.hashCode());
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
        CallData<?> other = (CallData<?>) obj;
        if (callType == null) {
            if (other.callType != null) {
                return false;
            }
        } else if (!callType.equals(other.callType)) {
            return false;
        }
        if (dataType != other.dataType) {
            return false;
        }
        if (dataPropagation == null) {
            if (other.dataPropagation != null) {
                return false;
            }
        } else if (!dataPropagation.equals(other.dataPropagation)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("CallData [dataType=").append(dataType)
               .append(", callType=").append(callType)
               .append(", dataPropagation=").append(dataPropagation)
               .append("]");
        return builder.toString();
    }
}
