package org.bgee.model.expressiondata.baseelements;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.BgeeEnum;
import org.bgee.model.BgeeEnum.BgeeEnumField;
import org.bgee.model.expressiondata.baseelements.PropagationState;

/**
 * An interface representing types of expression calls from any types of analysis: 
 * baseline presence/absence of expression, and differential expression analyses. 
 * It then includes two nested {@code enum}s: {@link CallType.Expression} 
 * for baseline presence/absence, and {@link CallType.DiffExpression} for differential 
 * expression.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
//TODO: unit tests
public interface CallType {
    
    //**************** INNER CallType CLASSES ****************
    /**
     * An {@code enum} representing call types for baseline presence/absence of expression: 
     * <ul>
     * <li>{@code EXPRESSED}: expression calls (presence of expression).
     * <li>{@code NOT_EXPRESSED}: no-expression calls (absence of expression 
     * explicitly reported).
     * </ul>
     * 
     * @author Frederic Bastian
     * @version Bgee 13
     * @see DiffExpression
     * @since Bgee 13
     */
    public static enum Expression implements CallType, BgeeEnumField {
        EXPRESSED(Collections.unmodifiableSet(EnumSet.allOf(DataType.class))), 
        NOT_EXPRESSED(Collections.unmodifiableSet(
                EnumSet.of(DataType.AFFYMETRIX, DataType.IN_SITU, DataType.RNA_SEQ)));
        private final static Logger log = LogManager.getLogger(Expression.class.getName());
        
        /**
         * @see #getAllowedDataTypes()
         */
        private final Set<DataType> allowedDataTypes;
        
        private Expression(Set<DataType> allowedDataTypes) {
            this.allowedDataTypes = allowedDataTypes;
        }
        @Override
        public Set<DataType> getAllowedDataTypes() {
            return this.allowedDataTypes;
        }
        @Override
        public void checkPropagationState(PropagationState propState) throws IllegalArgumentException {
            log.entry(propState);

            boolean incorrectPropagation = false;

            switch (this) {
            case EXPRESSED:
                //no propagation from parents allowed for expression calls, 
                //all other propagations allowed. 
                if (PropagationState.ANCESTOR.equals(propState)) {
                    incorrectPropagation = true;
                }
                break;
            case NOT_EXPRESSED:
                //for no-expression calls, propagation from parent anat. entities.
                if (PropagationState.DESCENDANT.equals(propState)) {
                    incorrectPropagation = true;
                }
                break;
            default:
                throw log.throwing(new IllegalStateException("CallType not supported: " 
                        + this));
            }
            
            if (incorrectPropagation) {
                //log in TRACE level, since this method can simply be used to check validity
                //of a propagation state
                throw log.throwing(Level.TRACE, new IllegalArgumentException("The following propagation "
                        + "is incorrect for the CallType " + this + ": " + propState));
            }
            log.exit();
        }
        
        @Override
        public String getStringRepresentation() {
            log.entry();
            return log.exit(this.name());
        }
        
        /**
         * Convert the {@code String} representation of a call type for baseline presence or 
         * absence of expression (for instance, retrieved from request) into a
         * {@code CallType.Expression}.
         * Operation performed by calling {@link BgeeEnum#convert(Class, String)} with 
         * {@code CallType.Expression} as the {@code Class} argument, and {@code representation} 
         * as the {@code String} argument.
         * 
         * @param representation            A {@code String} representing a data quality.
         * @return                          A {@code CallType.Expression} corresponding 
         *                                  to {@code representation}.
         * @throws IllegalArgumentException If {@code representation} does not correspond 
         *                                  to any {@code CallType.Expression}.
         * @see #convert(Class, String)
         */
        public static final Expression convertToExpression(String representation) {
            return BgeeEnum.convert(Expression.class, representation);
        }
    }
    /**
     * An {@code enum} representing call types from differential expression analyses: 
     * <ul>
     * <li>{@code OVER_EXPRESSED}: over-expression calls obtained from 
     * differential expression analyses.
     * <li>{@code UNDER_EXPRESSED}: under-expression calls obtained from 
     * differential expression analyses.
     * <li>{@code DIFF_EXPRESSED}: differential expression calls of any kind 
     * (either over-expression or under-expression), obtained from 
     * differential expression analyses. This {@code enum} type is most likely used 
     * to define parameters of a query (for instance, to retrieve genes differentially 
     * expressed in brain, whatever the direction of the fold change found).
     * <li>{@code NOT_DIFF_EXPRESSED}: means that a gene was studied in 
     * a differential expression analysis, but was <strong>not</strong> found to be 
     * differentially expressed. This is different from {@code Expression.NOT_EXPRESSED}, 
     * as the gene could actually be expressed, but not differentially. 
     * </ul>
     * 
     * @author Frederic Bastian
     * @version Bgee 13
     * @see Expression
     * @since Bgee 13
     */
    public static enum DiffExpression implements CallType, BgeeEnumField {
        DIFF_EXPRESSED(), OVER_EXPRESSED(), 
        UNDER_EXPRESSED(), NOT_DIFF_EXPRESSED();
        
        /**
         * {@code Logger} of the class. 
         */
        private final static Logger log = LogManager.getLogger(DiffExpression.class.getName());
        
        /**
         * Allowed {@code DataType}s are the same for all {@code DiffExpression} {@code CallType}s.
         * @see #getAllowedDataTypes()
         */
        private static final Set<DataType> DIFF_EXPR_DATA_TYPES = 
                Collections.unmodifiableSet(EnumSet.of(DataType.AFFYMETRIX, DataType.RNA_SEQ));
        
        @Override
        public Set<DataType> getAllowedDataTypes() {
            return DIFF_EXPR_DATA_TYPES;
        }
        @Override
        public void checkPropagationState(PropagationState propState) throws IllegalArgumentException {
            log.entry(propState);
            //no propagation allowed for any diff. expression call type.
            //log in TRACE level, since this method can simply be used to check validity
            //of a propagation state
            if (!PropagationState.SELF.equals(propState)) {
                throw log.throwing(Level.TRACE, new IllegalArgumentException("The following propagation "
                        + "is incorrect for the CallType " + this + ": " + propState));
            }
            log.exit();
        }
        @Override
        public String getStringRepresentation() {
            log.entry();
            return log.exit(this.name());
        }
        /**
         * Convert the {@code String} representation of a call type from differential expression
         * analyses (for instance, retrieved from request) into a {@code CallType.DiffExpression}.
         * Operation performed by calling {@link BgeeEnum#convert(Class, String)} with 
         * {@code CallType.DiffExpression} as the {@code Class} argument, and {@code representation} 
         * as the {@code String} argument.
         * 
         * @param representation            A {@code String} representing a data quality.
         * @return                          A {@code CallType.DiffExpression} corresponding 
         *                                  to {@code representation}.
         * @throws IllegalArgumentException If {@code representation} does not correspond 
         *                                  to any {@code CallType.DiffExpression}.
         * @see #convert(Class, String)
         */
        public static final DiffExpression convertToDiffExpression(String representation) {
            return BgeeEnum.convert(DiffExpression.class, representation);
        }
    }

    
    //**************** DEFAULT CallType METHODS ****************
    /**
     * Check if {@code dataType} is compatible with this {@code CallType} 
     * (see {@link CallType#getAllowedDataTypes()}). 
     * An {@code IllegalArgumentException} is thrown if an incompatibility 
     * is detected.
     * 
     * @param dataType      A {@code DataType} to check against 
     *                      this {@code CallType}.
     * @see #isValidDataType(DataType)
     * @throws IllegalArgumentException     If an incompatibility is detected.
     */
    default void checkDataType(DataType dataType) 
            throws IllegalArgumentException {
        checkCallTypeDataTypes(Arrays.asList(dataType));
    }
    /**
     * Check if {@code dataType} is compatible with this {@code CallType} 
     * (see {@link CallType#getAllowedDataTypes()}).
     * 
     * @param dataType      A {@code DataType} to check against 
     *                      this {@code CallType}.
     * @return              {@code true} if {@code dataType} is compatible with this {@code CallType},
     *                      {@code false} otherwise.
     * @see #checkDataType(DataType)
     */
    default boolean isValidDataType(DataType dataType) {
        try {
            this.checkDataType(dataType);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    /**
     * Check if all {@code DataType}s in {@code dataTypes} are compatible 
     * with this {@code CallType} (see {@link CallType#getAllowedDataTypes()}). 
     * An {@code IllegalArgumentException} is thrown if an incompatibility 
     * is detected,
     * 
     * @param dataTypes     A {@code Collection} of {@code DataType}s to check 
     *                      against {@code callType}.
     * @throws IllegalArgumentException     If an incompatibility is detected.
     */
    default void checkCallTypeDataTypes(Collection<DataType> dataTypes) 
            throws IllegalArgumentException {
        //if dataTypes is empty, the code below will conclude that valid data types were provided,
        //so we need to check
        if (dataTypes == null || dataTypes.isEmpty()) {
            throw new IllegalArgumentException("No data types provided.");
        }
        
        Set<DataType> forbiddenTypes = dataTypes.stream()
                .filter(e -> !this.getAllowedDataTypes().contains(e))
                .collect(Collectors.toSet());
        if (!forbiddenTypes.isEmpty()) {
            throw new IllegalArgumentException("The following data types "
                    + "are incompatible with the call type " + this + ": " 
                    + forbiddenTypes.toString());
        }
    }

    /**
     * Determines whether this {@code CallType} can be propagated according to 
     * the provided {@code PropagationState}. An {@code IllegalArgumentException} is thrown 
     * if the propagation is not possible for this {@code CallType}.
     * <p>
     * <ul>
     * <li>{@link Expression#EXPRESSED}: can be propagated from descendant conditions. 
     * <li>{@link Expression#NOT_EXPRESSED}: can be propagated from some parent conditions
     * (along anatomical relations only).
     * <li>{@link DiffExpression}: none of the diff. expression call types can be propagated.
     * </ul>
     * 
     * @param propagation   The {@code PropagationState} to be checked for validity 
     *                      when use for this {@code CallType}.
     * @see #isValidPropagationState(DataType)
     * @throws IllegalArgumentException If {@code propState} is not valid for this {@code CallType}.
     */
    public void checkPropagationState(PropagationState propState) throws IllegalArgumentException;
    /**
     * Check if {@code propState} is compatible with this {@code CallType} 
     * (see {@link #checkPropagationState(PropagationState)}).
     * 
     * @param propagation   The {@code PropagationState} to be checked for validity 
     *                      when use for this {@code CallType}.
     * @return              {@code true} if {@code propState} is compatible with this {@code CallType},
     *                      {@code false} otherwise.
     * @see #checkPropagationState(DataType)
     */
    default boolean isValidPropagationState(PropagationState propState) {
        try {
            this.checkPropagationState(propState);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    
    /**
     * Returns the {@code DataType}s capable of producing this {@code CallType}. 
     * Some call types cannot be produced by all data types. 
     * <ul>
     * <li>EST data cannot produce {@code Expression.NOT_EXPRESSED} calls 
     * an any {@code DiffExpression} calls.
     * <li>In situ data cannot produce any {@code DiffExpression} calls.
     * 
     * @return    An unmodifiable {@code Set} containing the {@code DataType}s 
     *            capable of producing this {@code CallType}. 
     */
    public Set<DataType> getAllowedDataTypes();
}