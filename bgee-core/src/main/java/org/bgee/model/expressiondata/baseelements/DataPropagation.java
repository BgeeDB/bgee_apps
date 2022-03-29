package org.bgee.model.expressiondata.baseelements;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.expressiondata.CallService;

/**
 * Defines the source of expression data of a {@code CallData} or {@code Call}, along 
 * the ontologies used to capture conditions. For instance, the expression of a gene 
 * in a given anatomical entity could have been observed in the anatomical entity itself, 
 * or only in some substructures of the entity, or in both. Similarly, expression in a given 
 * developmental stage could have been observed only in a sub-stage, or in the stage itself, 
 * etc. 
 * 
 * @author Frederic Bastian
 * @version Bgee 15 Aug. 2021
 * @since Bgee 13 Sep. 2015
 */
public class DataPropagation {
    private final static Logger log = LogManager.getLogger(DataPropagation.class.getName());

    private static void checkMapArgument(Map<EnumSet<CallService.Attribute>, Integer> map) {
        log.traceEntry("{}", map);
        if (map == null || map.isEmpty() ||
                //All keys must be EnumSet of condition parameters only
                map.keySet().stream().anyMatch(es -> es == null || es.isEmpty() ||
                        es.stream().anyMatch(a -> !a.isConditionParameter())) ||
                //All values must be non-null and positive
                map.values().stream().anyMatch(v -> v == null || v < 0)) {
            throw log.throwing(new IllegalArgumentException("Invalid observation count Map: "
                    + map));
        }
        log.traceExit();
    }

    private final Map<EnumSet<CallService.Attribute>, Integer> selfObservationCounts;
    private final Map<EnumSet<CallService.Attribute>, Integer> descendantObservationCounts;

    /**
     * Constructor accepting the self and descendant observation counts associated to
     * an expression call for the requested combinations of condition parameters.
     * The keys in both {@code Map}s but be equal.
     *
     * @param selfObservationCounts         A {@code Map} where keys are {@code EnumSet}s of
     *                                      {@code CallService.Attribute}s representing combinations
     *                                      of condition parameters, the associated value being
     *                                      an {@code Integer} that is the count of observation
     *                                      in the condition itself for the related combination.
     * @param descendantObservationCounts   A {@code Map} where keys are {@code EnumSet}s of
     *                                      {@code CallService.Attribute}s representing combinations
     *                                      of condition parameters, the associated value being
     *                                      an {@code Integer} that is the count of observation
     *                                      in the descendant conditions of the requested condition
     *                                      for the related combination.
     * @throws IllegalArgumentException     If any of the argument is {@code null} or empty,
     *                                      or the keys or the values in any of the argument
     *                                      are {@code null} or empty, or the keys in any of the argument
     *                                      contain {@code CallService.Attribute}s that are not
     *                                      condition parameters (see {@link
     *                                      CallService.Attribute#isConditionParameter()}),
     *                                      or the values in any of the argument is less than 0,
     *                                      or the keys in the two {@code Map} arguments are not equal.
     */
    public DataPropagation(Map<EnumSet<CallService.Attribute>, Integer> selfObservationCounts,
            Map<EnumSet<CallService.Attribute>, Integer> descendantObservationCounts)
                    throws IllegalArgumentException {
        checkMapArgument(selfObservationCounts);
        checkMapArgument(descendantObservationCounts);
        //The keysets in both Map should be identical
        if (!selfObservationCounts.keySet().equals(descendantObservationCounts.keySet())) {
            throw log.throwing(new IllegalArgumentException(
                    "Inconsistent condition parameter combinations in self and descendant observation counts"));
        }
        //We will use defensive copying, there is no Unmodifiable EnumSet
        this.selfObservationCounts = selfObservationCounts.entrySet().stream()
                .collect(Collectors.toMap(e -> EnumSet.copyOf(e.getKey()), e -> e.getValue()));
        this.descendantObservationCounts = descendantObservationCounts.entrySet().stream()
                .collect(Collectors.toMap(e -> EnumSet.copyOf(e.getKey()), e -> e.getValue()));
    }

    /**
     * @return  A {@code Set} of {@code EnumSet}s representing the combinations of condition parameters
     *          considered in this {@code DataPropagation} object.
     */
    public Set<EnumSet<CallService.Attribute>> getCondParamCombinations() {
        //defensive copying, there is no Unmodifiable EnumSet
        return this.selfObservationCounts.keySet().stream()
                .map(es -> EnumSet.copyOf(es))
                .collect(Collectors.toSet());
    }

    /**
     * Returns the count of observations associated to an expression call in the condition itself,
     * according to combinations of condition parameters.
     * <p>
     * The "official" "real" count would be associated
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
     *          representing combinations of condition parameters, the associated value being
     *          an {@code Integer} that is the count of observation in the condition itself
     *          for the related combination.
     */
    public Map<EnumSet<CallService.Attribute>, Integer> getSelfObservationCounts() {
        //defensive copying, there is no Unmodifiable EnumSet
        return this.selfObservationCounts.entrySet().stream()
                .collect(Collectors.toMap(e -> EnumSet.copyOf(e.getKey()), e -> e.getValue()));
    }
    /**
     * Returns the count of observations associated to an expression call in the condition itself,
     * according to a combination of condition parameters.
     * If the information is not available for this combination, or there is no data associated to
     * this combination (for instance, if this {@code DataPropagation} object is associated to
     * a {@code DataType} that did not contribute to produce an expression call), this method returns
     * {@code null}.
     * <p>
     * The "official" "real" count would be associated
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
     * @param condParamCombination  A {@code Collection} of {@code CallService.Attribute}s targeting
     *                              a combination of condition parameters. Of note, the combination
     *                              can target only one condition parameter.
     * @return                      An {@code Integer} that is the self observation count
     *                              for the requested combination of condition parameters,
     *                              {@code null} if this cannot be determined or the information
     *                              was not requested.
     * @throws IllegalArgumentException If {@code condParamCombination} contains
     *                                  {@code CallService.Attribute}s that are not condition parameters
     *                                  (see {@link CallService.Attribute#isConditionParameter()}).
     */
    public Integer getSelfObservationCount(Collection<CallService.Attribute> condParamCombination)
            throws IllegalArgumentException {
        log.traceEntry("{}", condParamCombination);
        return log.traceExit(this.selfObservationCounts.get(
                CallService.Attribute.getCondParamCombination(condParamCombination)));
    }

    /**
     * Returns the count of observations associated to an expression call in the descendant conditions
     * of the related condition according to combinations of condition parameters.
     * <p>
     * The "official" "real" count would be associated
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
     *          representing combinations of condition parameters, the associated value being
     *          an {@code Integer} that is the count of observation in the descendant conditions
     *          of the condition related to an expression call for the related combination.
     */
    public Map<EnumSet<CallService.Attribute>, Integer> getDescendantObservationCounts() {
        //defensive copying, there is no Unmodifiable EnumSet
        return this.descendantObservationCounts.entrySet().stream()
                .collect(Collectors.toMap(e -> EnumSet.copyOf(e.getKey()), e -> e.getValue()));
    }
    /**
     * Returns the count of observations associated to an expression call in the descendant conditions
     * of the related condition of an expression call, according to a combination of condition parameters.
     * If the information is not available for this combination, or there is no data associated to
     * this combination (for instance, if this {@code DataPropagation} object is associated to
     * a {@code DataType} that did not contribute to produce an expression call), this method returns
     * {@code null}.
     * <p>
     * The "official" "real" count would be associated
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
     * @param condParamCombination  A {@code Collection} of {@code CallService.Attribute}s targeting
     *                              a combination of condition parameters. Of note, the combination
     *                              can target only one condition parameter.
     * @return                      An {@code Integer} that is the descendant observation count
     *                              for the requested combination of condition parameters,
     *                              {@code null} if this cannot be determined or the information
     *                              was not requested.
     * @throws IllegalArgumentException If {@code condParamCombination} contains
     *                                  {@code CallService.Attribute}s that are not condition parameters
     *                                  (see {@link CallService.Attribute#isConditionParameter()}).
     */
    public Integer getDescendantObservationCount(Collection<CallService.Attribute> condParamCombination)
            throws IllegalArgumentException {
        log.traceEntry("{}", condParamCombination);
        return log.traceExit(this.descendantObservationCounts.get(
                CallService.Attribute.getCondParamCombination(condParamCombination)));
    }

    /**
     * Returns the total observation count associated to an expression call in the related condition
     * and the descendant conditions, according to a combination of condition parameters.
     * If the information is not available for this combination, or there is no data associated to
     * this combination (for instance, if this {@code DataPropagation} object is associated to
     * a {@code DataType} that did not contribute to produce an expression call), this method returns
     * {@code null}.
     *
     * @param condParamCombination  A {@code Collection} of {@code CallService.Attribute}s targeting
     *                              a combination of condition parameters. Of note, the combination
     *                              can target only one condition parameter.
     * @return                      An {@code Integer} that is the sum of the self and descendant
     *                              observation count for the requested combination of
     *                              condition parameters, {@code null} if this cannot be determined
     *                              or the information was not requested.
     * @throws IllegalArgumentException If {@code condParamCombination} contains
     *                                  {@code CallService.Attribute}s that are not condition parameters
     *                                  (see {@link CallService.Attribute#isConditionParameter()}).
     */
    public Integer getTotalObservationCount(Collection<CallService.Attribute> condParamCombination)
            throws IllegalArgumentException {
        log.traceEntry("{}", condParamCombination);
        Integer selfObservationCounts = this.getSelfObservationCount(condParamCombination);
        Integer descendantObservationCounts = this.getDescendantObservationCount(condParamCombination);
        if (selfObservationCounts == null) {
            assert descendantObservationCounts == null;
            return log.traceExit((Integer) null);
        }
        assert descendantObservationCounts != null;
        return log.traceExit(selfObservationCounts + descendantObservationCounts);
    }

    /**
     * Returns the {@code PropagationState} for the requested combination of condition parameters.
     * If the information is not available for this combination, this method returns
     * {@code PropagationState.UNKNOWN}. If there is no data associated to this combination
     * (for instance, if this {@code DataPropagation} object is associated to a {@code DataType}
     * that did not contribute to produce an expression call), this method returns {@code null}.
     * <p>
     * Of note, this method is simply a helper method as compared to using the methods
     * {@link #getSelfObservationCounts(Collection)} and {@link #getDescendantObservationCounts(Collection)}.
     *
     * @param condParamCombination  A {@code Collection} of {@code CallService.Attribute}s targeting
     *                              a combination of condition parameters. Of note, the combination
     *                              can target only one condition parameter.
     * @return                      The {@code PropagationState} informing about how the data
     *                              were propagated to produce an expression call for the requested
     *                              combination of condition parameters.
     * @throws IllegalArgumentException If {@code condParamCombination} contains
     *                                  {@code CallService.Attribute}s that are not condition parameters
     *                                  (see {@link CallService.Attribute#isConditionParameter()}).
     */
    public PropagationState getPropagationState(Collection<CallService.Attribute> condParamCombination)
            throws IllegalArgumentException {
        log.traceEntry("{}", condParamCombination);
        Integer selfObservationCounts = this.getSelfObservationCount(condParamCombination);
        Integer descendantObservationCounts = this.getDescendantObservationCount(condParamCombination);
        if (selfObservationCounts == null) {
            assert descendantObservationCounts == null;
            return log.traceExit(PropagationState.UNKNOWN);
        }
        assert descendantObservationCounts != null;
        if (selfObservationCounts == 0 && descendantObservationCounts == 0) {
            return log.traceExit((PropagationState) null);
        }
        if (selfObservationCounts > 0 && descendantObservationCounts == 0) {
            return log.traceExit(PropagationState.SELF);
        }
        if (selfObservationCounts > 0 && descendantObservationCounts > 0) {
            return log.traceExit(PropagationState.SELF_AND_DESCENDANT);
        }
        if (selfObservationCounts == 0 && descendantObservationCounts > 0) {
            return log.traceExit(PropagationState.DESCENDANT);
        }
        throw log.throwing(new IllegalStateException(
                "Impossible combination of self and descendant observation counts. Self count: "
                + selfObservationCounts + " - descendant count: " + descendantObservationCounts
                + " - requested condition parameter combination: " + condParamCombination));
    }

    /**
     * Returns whether this {@code DataPropagation} is linked to data including observed data
     * for the requested combination of condition parameters, meaning, not from call propagation only.
     * If the information is not available for this combination, or there is no data associated to
     * this combination (for instance, if this {@code DataPropagation} object is associated to
     * a {@code DataType} that did not contribute to produce an expression call), this method returns
     * {@code null}.
     * <p>
     * Of note, this method is simply a helper method as compared to using the methods
     * {@link #getSelfObservationCounts(Collection)} and {@link #getDescendantObservationCounts(Collection)}.
     *
     * @return  {@code true} if the related data included observed data, {@code false} otherwise,
     *          {@code null} if this cannot be determined or the information was not requested.
     */
    public Boolean isIncludingObservedData(Collection<CallService.Attribute> condParamCombination) {
        log.traceEntry("{}", condParamCombination);
        PropagationState propState = this.getPropagationState(condParamCombination);
        if (propState != null) {
            return log.traceExit(propState.isIncludingObservedData());
        }
        return log.traceExit((Boolean) null);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((descendantObservationCounts == null) ? 0 : descendantObservationCounts.hashCode());
        result = prime * result + ((selfObservationCounts == null) ? 0 : selfObservationCounts.hashCode());
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
        DataPropagation other = (DataPropagation) obj;
        if (descendantObservationCounts == null) {
            if (other.descendantObservationCounts != null) {
                return false;
            }
        } else if (!descendantObservationCounts.equals(other.descendantObservationCounts)) {
            return false;
        }
        if (selfObservationCounts == null) {
            if (other.selfObservationCounts != null) {
                return false;
            }
        } else if (!selfObservationCounts.equals(other.selfObservationCounts)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("DataPropagation [selfObservationCounts=").append(selfObservationCounts)
               .append(", descendantObservationCounts=").append(descendantObservationCounts)
               .append("]");
        return builder.toString();
    }
}