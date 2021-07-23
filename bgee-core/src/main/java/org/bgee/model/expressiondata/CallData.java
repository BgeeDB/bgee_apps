package org.bgee.model.expressiondata;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Map.Entry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.expressiondata.baseelements.FDRPValue;
import org.bgee.model.expressiondata.baseelements.FDRPValueCondition;
import org.bgee.model.expressiondata.baseelements.CallType;
import org.bgee.model.expressiondata.baseelements.DataPropagation;
import org.bgee.model.expressiondata.baseelements.CallType.DiffExpression;
import org.bgee.model.expressiondata.baseelements.CallType.Expression;
import org.bgee.model.expressiondata.baseelements.SummaryCallType.ExpressionSummary;
import org.bgee.model.expressiondata.baseelements.SummaryQuality;
import org.bgee.model.expressiondata.baseelements.DataQuality;
import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.model.expressiondata.baseelements.DiffExpressionFactor;

/**
 * A {@code CallData} represents the expression state of a {@link org.bgee.model.gene.Gene} in a {@link Condition}
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
 * @version Bgee 15.0, Apr. 2021
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
            super(dataType, callType);

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
                   .append(", callType=").append(getCallType()).append("]");
            return builder.toString();
        }

    }

    //TODO: javadoc of all methods and attributes
    public static class ExpressionCallData extends CallData<Expression> {
        //********************************************
        // STATIC ATTRIBUTES AND METHODS
        //********************************************

        /**
         * Computes the {@code CallType.Expression} that the FDR-corrected p-values
         * of this {@code CallData} allow to produce.
         *
         * @param dataType                  The {@code DataType} to compute the
         *                                  {@code CallType.Expression} for. Cannot be {@code null}.
         * @param fdrPValue                 A {@code BigDecimal} that is the FDR-corrected p-value
         *                                  computed from all the p-values obtained by {@code dataType}
         *                                  in a condition and all its sub-conditions for a gene.
         * @param bestDescendantFDRPValue   A {@code BigDecimal} that is the best FDR-corrected
         *                                  p-value obtained by {@code dataType} among the sub-conditions
         *                                  of the condition of a call for a gene.
         * @return                          The {@code CallType.Expression} inferred.
         *                                  {@code null} if the p-values provided are null.
         * @throws IllegalStateException    If the p-values provided do not allow to produce
         *                                  a {@code CallType.Expression}.
         */
        private static Expression inferCallType(DataType dataType,
                BigDecimal fdrPValue, BigDecimal bestDescendantFDRPValue) {
            log.traceEntry("{}, {}, {}", dataType, fdrPValue, bestDescendantFDRPValue);
            
            if (fdrPValue == null || bestDescendantFDRPValue == null) {
                return log.traceExit((Expression) null);
            }
            Entry<ExpressionSummary, SummaryQuality> exprQualSummary =
                    CallService.inferSummaryCallTypeAndQuality(
                            Collections.singleton(new FDRPValue(fdrPValue, EnumSet.of(dataType))),
                            Collections.singleton(new FDRPValueCondition(fdrPValue,
                                    EnumSet.of(dataType), null)),
                            EnumSet.of(dataType));
            if (exprQualSummary == null) {
                return log.traceExit((Expression) null);
            }
            switch(exprQualSummary.getKey()) {
            case EXPRESSED:
                return log.traceExit(Expression.EXPRESSED);
            case NOT_EXPRESSED:
                return log.traceExit(Expression.NOT_EXPRESSED);
            }

            //this point should be reached only if a new CallType.Expression is not supported here,
            //so it's an IllegalStateException, not an IllegalArgumentException
            throw log.throwing(new IllegalStateException(
                    "Could not infer CallType from FDR-corrected p-values"));
        }

        //********************************************
        // INSTANCE ATTRIBUTES AND CONSTRUCTORS
        //********************************************
        private final DataPropagation dataPropagation;

        private final List<BigDecimal> selfPValues;
        //useful if we don't want to retrieve all self p-values but just to retrieve the count
        private final Map<EnumSet<CallService.Attribute>, Integer> selfObservationCount;
        private final List<BigDecimal> descendantPValues;
        //useful if we don't want to retrieve all descendant p-values but just to retrieve the count
        private final Map<EnumSet<CallService.Attribute>, Integer> descendantObservationCount;
        private final List<BigDecimal> allPValues;
        private final BigDecimal fdrPValue;
        private final BigDecimal bestDescendantFDRPValue;

        private final BigDecimal rank;
        
        private final BigDecimal normalizedRank;
        
        private final BigDecimal weightForMeanRank;

        public ExpressionCallData(DataType dataType,
                Collection<BigDecimal> selfPValues,
                Map<EnumSet<CallService.Attribute>, Integer> selfObservationCount,
                Collection<BigDecimal> descendantPValues,
                Map<EnumSet<CallService.Attribute>, Integer> descendantObservationCount,
                BigDecimal rank, BigDecimal normalizedRank, BigDecimal weightForMeanRank,
                DataPropagation dataPropagation) {
            this(dataType, selfPValues, selfObservationCount,
                    descendantPValues, descendantObservationCount,
                    null, null, 
                    rank, normalizedRank, weightForMeanRank, dataPropagation);
        }
        public ExpressionCallData(DataType dataType,
                BigDecimal fdrPValue, BigDecimal bestDescendantFDRPValue,
                Map<EnumSet<CallService.Attribute>, Integer> selfObservationCount,
                Map<EnumSet<CallService.Attribute>, Integer> descendantObservationCount,
                BigDecimal rank, BigDecimal normalizedRank, BigDecimal weightForMeanRank,
                DataPropagation dataPropagation) {
            this(dataType, null, selfObservationCount, null, descendantObservationCount,
                    fdrPValue, bestDescendantFDRPValue,
                    rank, normalizedRank, weightForMeanRank, dataPropagation);
        }
        public ExpressionCallData(DataType dataType,
                Collection<BigDecimal> selfPValues,
                Map<EnumSet<CallService.Attribute>, Integer> selfObservationCount,
                Collection<BigDecimal> descendantPValues,
                Map<EnumSet<CallService.Attribute>, Integer> descendantObservationCount,
                BigDecimal fdrPValue, BigDecimal bestDescendantFDRPValue,
                BigDecimal rank, BigDecimal normalizedRank, BigDecimal weightForMeanRank,
                DataPropagation dataPropagation) {
            super(dataType, inferCallType(dataType, fdrPValue, bestDescendantFDRPValue));

            //Sort the p-values
            List<BigDecimal> sortedSelfPValues = selfPValues == null? new ArrayList<>():
                new ArrayList<>(selfPValues);
            Collections.sort(sortedSelfPValues);
            this.selfPValues = Collections.unmodifiableList(sortedSelfPValues);
            //we will use defensive copying for this attribute,
            //there is no unmodifiableEnumSet
            if (selfObservationCount == null && selfPValues != null && !selfPValues.isEmpty()) {
                this.selfObservationCount = new HashMap<>();
                this.selfObservationCount.put(CallService.Attribute.getAllConditionParameters(),
                        selfPValues.size());
            } else if (selfObservationCount != null) {
                if (selfObservationCount.entrySet().stream().anyMatch(e ->
                        e.getKey() == null || e.getValue() == null ||
                        e.getKey().stream().anyMatch(a -> !a.isConditionParameter()) ||
                        e.getValue() < 0)) {
                    throw log.throwing(new IllegalArgumentException("Invalid selfObservationCount"));
                }
                this.selfObservationCount = selfObservationCount.entrySet().stream()
                        .collect(Collectors.toMap(
                                e -> EnumSet.copyOf(e.getKey()),
                                e -> e.getValue()));
            } else {
                this.selfObservationCount = null;
            }
            List<BigDecimal> sortedDescendantPValues = descendantPValues == null? new ArrayList<>():
                new ArrayList<>(descendantPValues);
            Collections.sort(sortedDescendantPValues);
            this.descendantPValues = Collections.unmodifiableList(sortedDescendantPValues);
            //we will use defensive copying for this attribute,
            //there is no unmodifiableEnumSet
            if (descendantObservationCount == null && descendantPValues != null && !descendantPValues.isEmpty()) {
                this.descendantObservationCount = new HashMap<>();
                this.descendantObservationCount.put(CallService.Attribute.getAllConditionParameters(),
                        descendantPValues.size());
            } else if (descendantObservationCount != null) {
                if (descendantObservationCount.entrySet().stream().anyMatch(e ->
                        e.getKey() == null || e.getValue() == null ||
                        e.getKey().stream().anyMatch(a -> !a.isConditionParameter()) ||
                        e.getValue() < 0)) {
                    throw log.throwing(new IllegalArgumentException("Invalid descendantObservationCount"));
                }
                this.descendantObservationCount = descendantObservationCount.entrySet().stream()
                        .collect(Collectors.toMap(
                                e -> EnumSet.copyOf(e.getKey()),
                                e -> e.getValue()));
            } else {
                this.descendantObservationCount = null;
            }
            List<BigDecimal> allPValues = new ArrayList<>(sortedSelfPValues);
            allPValues.addAll(sortedDescendantPValues);
            Collections.sort(allPValues);
            this.allPValues = Collections.unmodifiableList(allPValues);

            this.fdrPValue = fdrPValue;
            this.bestDescendantFDRPValue = bestDescendantFDRPValue;

            if (dataPropagation != null && this.getCallType() != null) {
                dataPropagation.getAllPropagationStates().stream()
                .forEach(state -> this.getCallType().checkPropagationState(state));
            }

            this.dataPropagation = dataPropagation;
            //BigDecimal are immutable so we're good
            this.rank = rank;
            this.normalizedRank = normalizedRank;
            this.weightForMeanRank = weightForMeanRank;
        }

        //********************************************
        // GETTERS
        //********************************************

        public DataPropagation getDataPropagation() {
            return dataPropagation;
        }

        /**
         * Returns the p-values produced from expression data of a gene in a condition itself.
         * Of note, as opposed to the method {@ink #getSelfObservationCount()},
         * which allows to specify the condition parameters to consider,
         * the p-values returned are all observed in the condition itself by considering
         * all condition parameters.
         *
         * @return  A {@code List} of {@code BigDecimal}s representing the p-values
         *          computed from tests to detect active signal of expression of a gene
         *          using {@link #getDataType()}, in the condition itself.
         *          The p-values are ordered in ascending order.
         */
        public List<BigDecimal> getSelfPValues() {
            return selfPValues;
        }
        /**
         * Returns the count of observations in the condition itself, considering some combinations
         * of condition parameters (see {@link
         * org.bgee.model.expressiondata.CallService.Attribute#isConditionParameter()
         * CallService.Attribute#isConditionParameter()}). The "official" "real" count would be associated
         * to the {@code EnumSet} returned by {@link
         * org.bgee.model.expressiondata.CallService.Attribute#getAllConditionParameters()
         * CallService.Attribute#getAllConditionParameters()}. For instance, if we have
         * two observations of expression data for a gene in:
         * <ul>
         * <li>{@code AnatEntity=hypothalamus, DevStage=early adulthood}
         * <li>{@code AnatEntity=brain, DevStage=late adulthood}
         * </ul>
         * and the condition we are considering is {@code AnatEntity=brain, DevStage=adult}.
         * The "real" self observation count in the condition itself, considering the condition parameters
         * {@code CallService.Attribute.ANAT_ENTITY_ID} and {@code CallService.Attribute.DEV_STAGE_ID},
         * is 0, the "real" descendant observation count is 2 (all data have been propagated to the parent
         * stage "adult"). But we might be interested in knowing how many observations we have
         * in "brain" at any "adult" dev. stage. The self observation count in the condition itself,
         * considering only the condition parameter {@code CallService.Attribute.ANAT_ENTITY_ID},
         * is 1, and the descendant observation count is 1.
         *
         * @return  A {@code Map} where keys are {@code EnumSet}s of {@code CallService.Attribute}s
         *          representing the combinations of condition parameters considered, the associated
         *          value being an {@code Integer} that is the number of observations producing a p-value
         *          in the condition itself.
         * @see #getSelfPValues()
         * @see #getDescendantObservationCount()
         */
        public Map<EnumSet<CallService.Attribute>, Integer> getSelfObservationCount() {
            //defensive copying, there is no unmodifiableEnumSet
            return selfObservationCount.entrySet().stream()
                    .collect(Collectors.toMap(
                            e -> EnumSet.copyOf(e.getKey()),
                            e -> e.getValue()));
        }
        /**
         * @return  A {@code List} of {@code BigDecimal}s representing the p-values
         *          computed from tests to detect active signal of expression of a gene
         *          using {@link #getDataType()}, in the descendant conditions
         *          of the requested condition. The p-values are ordered in ascending order.
         */
        public List<BigDecimal> getDescendantPValues() {
            return descendantPValues;
        }
        /**
         * Returns the count of observations in the descendant conditions of the requested condition,
         * considering some combinations of condition parameters. See {@link #getSelfObservationCount()}
         * for more details.
         *
         * @return  A {@code Map} where keys are {@code EnumSet}s of {@code CallService.Attribute}s
         *          representing the combinations of condition parameters considered, the associated
         *          value being an {@code Integer} that is the number of observations producing a p-value
         *          in the descendant conditions of the requested condition.
         * @see #getDescendantPValues()
         * @see #getSelfObservationCount()
         */
        public Map<EnumSet<CallService.Attribute>, Integer> getDescendantObservationCount() {
            //defensive copying, there is no unmodifiableEnumSet
            return descendantObservationCount.entrySet().stream()
                    .collect(Collectors.toMap(
                            e -> EnumSet.copyOf(e.getKey()),
                            e -> e.getValue()));
        }
        /**
         * @return  A {@code List} of {@code BigDecimal}s representing the p-values
         *          computed from tests to detect active signal of expression of a gene
         *          using {@link #getDataType()}, in a condition itself and its descendant conditions.
         *          The p-values are ordered in ascending order.
         */
        public List<BigDecimal> getAllPValues() {
            return allPValues;
        }
        /**
         * @return  A {@code BigDecimal} that is the FDR corrected p-value computed from
         *          all the p-values obtained by this data type in a condition
         *          and all its sub-conditions for a gene.
         */
        public BigDecimal getFDRPValue() {
            return fdrPValue;
        }
        /**
         * @return  A {@code BigDecimal} that is the best FDR corrected p-value obtained by
         *          this data type among the sub-conditions of the condition of a call for a gene.
         */
        public BigDecimal getBestDescendantFDRPValue() {
            return bestDescendantFDRPValue;
        }
        /**
         * @return  An {@code Integer} that is the number of observations producing a p-value
         *          in a condition itself and its descendant conditions.
         * @see #getAllPValues()
         */
        public Integer getAllObservationCount() {
            if (this.selfObservationCount.isEmpty() || this.descendantObservationCount.isEmpty() ||
                    Collections.disjoint(this.selfObservationCount.keySet(),
                            this.descendantObservationCount.keySet())) {
                return null;
            }
            Set<EnumSet<CallService.Attribute>> commonCondParamCombinations =
                    new HashSet<>(this.selfObservationCount.keySet());
            commonCondParamCombinations.retainAll(this.descendantObservationCount.keySet());
            EnumSet<CallService.Attribute> selectedCondParamComb =
                    commonCondParamCombinations.iterator().next();
            return this.selfObservationCount.get(selectedCondParamComb) +
                    this.descendantObservationCount.get(selectedCondParamComb);
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
            result = prime * result + ((allPValues == null) ? 0 : allPValues.hashCode());
            result = prime * result + ((bestDescendantFDRPValue == null) ? 0 : bestDescendantFDRPValue.hashCode());
            result = prime * result + ((dataPropagation == null) ? 0 : dataPropagation.hashCode());
            result = prime * result
                    + ((descendantObservationCount == null) ? 0 : descendantObservationCount.hashCode());
            result = prime * result + ((descendantPValues == null) ? 0 : descendantPValues.hashCode());
            result = prime * result + ((fdrPValue == null) ? 0 : fdrPValue.hashCode());
            result = prime * result + ((normalizedRank == null) ? 0 : normalizedRank.hashCode());
            result = prime * result + ((rank == null) ? 0 : rank.hashCode());
            result = prime * result + ((selfObservationCount == null) ? 0 : selfObservationCount.hashCode());
            result = prime * result + ((selfPValues == null) ? 0 : selfPValues.hashCode());
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
            //comparison based on equals of BigDecimal while it would be better to use compareTo
            //But what about hashCode?
            if (allPValues == null) {
                if (other.allPValues != null) {
                    return false;
                }
            } else if (!allPValues.equals(other.allPValues)) {
                return false;
            }
            if (bestDescendantFDRPValue == null) {
                if (other.bestDescendantFDRPValue != null) {
                    return false;
                }
            } else if (!bestDescendantFDRPValue.equals(other.bestDescendantFDRPValue)) {
                return false;
            }
            if (dataPropagation == null) {
                if (other.dataPropagation != null) {
                    return false;
                }
            } else if (!dataPropagation.equals(other.dataPropagation)) {
                return false;
            }
            if (descendantObservationCount == null) {
                if (other.descendantObservationCount != null) {
                    return false;
                }
            } else if (!descendantObservationCount.equals(other.descendantObservationCount)) {
                return false;
            }
            if (descendantPValues == null) {
                if (other.descendantPValues != null) {
                    return false;
                }
            } else if (!descendantPValues.equals(other.descendantPValues)) {
                return false;
            }
            if (fdrPValue == null) {
                if (other.fdrPValue != null) {
                    return false;
                }
            } else if (!fdrPValue.equals(other.fdrPValue)) {
                return false;
            }
            if (normalizedRank == null) {
                if (other.normalizedRank != null) {
                    return false;
                }
            } else if (!normalizedRank.equals(other.normalizedRank)) {
                return false;
            }
            if (rank == null) {
                if (other.rank != null) {
                    return false;
                }
            } else if (!rank.equals(other.rank)) {
                return false;
            }
            if (selfObservationCount == null) {
                if (other.selfObservationCount != null) {
                    return false;
                }
            } else if (!selfObservationCount.equals(other.selfObservationCount)) {
                return false;
            }
            if (selfPValues == null) {
                if (other.selfPValues != null) {
                    return false;
                }
            } else if (!selfPValues.equals(other.selfPValues)) {
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
            builder.append("ExpressionCallData [")
                   .append("dataType=").append(getDataType())
                   .append(", callType=").append(getCallType())
                   .append(", dataPropagation=").append(dataPropagation)
                   .append(", fdrPValue=").append(fdrPValue)
                   .append(", bestDescendantFDRPValue=").append(bestDescendantFDRPValue)
                   .append(", selfPValues=").append(selfPValues)
                   .append(", selfObservationCount=").append(selfObservationCount)
                   .append(", descendantPValues=").append(descendantPValues)
                   .append(", descendantObservationCount=").append(descendantObservationCount)
                   .append(", rank=").append(rank)
                   .append(", normalizedRank=").append(normalizedRank)
                   .append(", weightForMeanRank=").append(weightForMeanRank)
                    .append("]");
            return builder.toString();
        }
    }


    //**********************************************
    //   INSTANCE ATTRIBUTES AND CONSTRUCTORS
    //**********************************************
    
    private final DataType dataType;

    //FIXME: delete this attribute, as of Bgee 15.0 callTypes are computed using
    //aggregated p-values to produce a FDR-corrected p-value, it does not make sense
    //to perform the p-value correction for each data type independently.
    //Now this class only store the data per data type, the call type is computed in the Call class.
    private final T callType;
    
    /**
     * Constructor allowing to specify a {@code DataType}. 
     * 
     * @param dataType  The {@code DataType} that allowed to generate the {@code CallType}.
     * @throws IllegalArgumentException    If {@code dataType} is not {@code null}.
     */
    protected CallData(DataType dataType, T callType)
            throws IllegalArgumentException {
        log.traceEntry("{}, {}", dataType, callType);
        
        if (dataType == null) {
            throw log.throwing(new IllegalArgumentException
                ("A DataType must be defined to instantiate a CallData."));
        }
        if (callType != null) {
            //XXX: for now we disable this check. Indeed, scRNA-Seq and/or EST data
            //can be used to produce a NOT_EXPRESSED call of BRONZE quality, for consistency
            //between the different combinations of call types and qualities.
            //An alternative solution would be to set the CallType to NULL for these data types
            //when the call type is NOT_EXPRESSED. But then it would be inconsistent with their use
            //in the CallService.
//            callType.checkDataType(dataType);
        }

        this.dataType = dataType;
        this.callType = callType;
        log.traceExit();
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


    //**********************************************
    //  hashCode/equals/toString
    //**********************************************
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((callType == null) ? 0 : callType.hashCode());
        result = prime * result + ((dataType == null) ? 0 : dataType.hashCode());
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
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("CallData [dataType=").append(dataType)
               .append(", callType=").append(callType)
               .append("]");
        return builder.toString();
    }
}
