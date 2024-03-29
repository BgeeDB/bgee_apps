package org.bgee.model.expressiondata.baseelements;

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
 * This interface is used to type the summary call types associated to 
 * {@link org.bgee.model.expressiondata.call.Call Call}s. They represent an overall summary 
 * of the {@link CallType}s from individual data types, associated to a same {@code Call}.
 * 
 * @author  Frederic Bastian
 * @author  Valentine Rech de Laval
 * @version Bgee 14, Apr. 2017
 * @see CallType
 * @see org.bgee.model.expressiondata.call.Call Call
 * @since Bgee 13 Sept. 2015
 */
public interface SummaryCallType extends CallType {
    /**
     * {@link SummaryCallType} associated to {@link org.bgee.model.expressiondata.call.Call.ExpressionCall 
     * ExpressionCall}s. They represent an overall summary of the {@link CallType.Expression Expression}
     * calls from individual data types, associated to a same {@code ExpressionCall}.
     * <ul>
     * <li>{@code EXPRESSED}: report of presence of expression, from Bgee statistical tests 
     * and/or from in situ data sources.
     * <li>{@code NOT_EXPRESSED}: report of absence of expression, from Bgee statistical tests 
     * and/or from in situ data sources. In Bgee, calls of absence of expression 
     * are always discarded if there exists a contradicting call of expression, 
     * from the same data type and for the same gene, in the same anatomical entity 
     * and developmental stage, or in a child entity or child developmental stage.
     * <li>{@code WEAK_AMBIGUITY}: there exists a call of expression generated from a data type, 
     * but there exists a call of absence of expression generated from another data type 
     * for the same gene in a parent anatomical entity at the same developmental stage. 
     * For instance, gene A is reported to be expressed in the midbrain at young adult stage 
     * from Affymetrix data, but is reported to be not expressed in the brain at young adult stage 
     * from RNA-Seq data.
     * <li>{@code STRONG_AMBIGUITY}: there exists a call of expression generated from a data type, 
     * but there exists a call of absence of expression generated from another data type 
     * for the same gene, anatomical entity and developmental stage. For instance, gene A 
     * is reported to be expressed in the midbrain at young adult stage from Affymetrix data, 
     * but is reported to be not expressed in the midbrain at young adult stage from RNA-Seq data.
     * </ul>
     * 
     * @author Frederic Bastian
     * @version Bgee 14 Mar. 2017
     * @see CallType.Expression 
     * @see org.bgee.model.expressiondata.call.Call.ExpressionCall ExpressionCall
     * @since Bgee 13 Sept. 2015
     */
    //XXX: although there is no more "ambiguity" status starting from Bgee 14,
    //maybe the distinction between ExpressionSummary and Expression is still useful?
    public static enum ExpressionSummary implements SummaryCallType, BgeeEnumField {
        EXPRESSED, NOT_EXPRESSED;
        private final static Logger log = LogManager.getLogger(ExpressionSummary.class.getName());

        @Override
        public void checkPropagationState(PropagationState propState) throws IllegalArgumentException {
            log.traceEntry("{}", propState);

            boolean incorrectPropagation = false;
            switch (this) {
            case EXPRESSED:
                //no propagation from parents allowed for expressed calls,
                //all other propagations allowed.
                if (PropagationState.ANCESTOR.equals(propState)) {
                    incorrectPropagation = true;
                }
                break;
            case NOT_EXPRESSED:
                //no propagation from parents allowed for absent calls,
                //no propagation only from descendant allowed
                if (PropagationState.DESCENDANT.equals(propState) ||
                        PropagationState.ANCESTOR.equals(propState)) {
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
                        + "is incorrect for the ExpressionSummary " + this + ": " + propState));
            }
            log.traceExit();
        }

        @Override
        public Set<DataType> getAllowedDataTypes() {
            log.traceEntry();
            switch (this) {
            case EXPRESSED:
                return log.traceExit(CallType.Expression.EXPRESSED.getAllowedDataTypes());
            case NOT_EXPRESSED:
                return log.traceExit(CallType.Expression.NOT_EXPRESSED.getAllowedDataTypes());
            default:
                throw log.throwing(new IllegalStateException("CallType not supported: " 
                        + this));
            }
        }

        @Override
        public String getStringRepresentation() {
            log.traceEntry();
            return log.traceExit(this.name());
        }

        /**
         * Convert the {@code String} representation of a call type for baseline presence or
         * absence of expression (for instance, retrieved from request) into a
         * {@code SummaryCallType.ExpressionSummary}.
         * Operation performed by calling {@link BgeeEnum#convert(Class, String)} with
         * {@code SummaryCallType.ExpressionSummary} as the {@code Class} argument,
         * and {@code representation} as the {@code String} argument.
         *
         * @param representation            A {@code String} representing a data quality.
         * @return                          A {@code SummaryCallType.ExpressionSummary}
         *                                  corresponding to {@code representation}.
         * @throws IllegalArgumentException If {@code representation} does not correspond
         *                                  to any {@code SummaryCallType.ExpressionSummary}.
         * @see #convert(Class, String)
         */
        public static final ExpressionSummary convertToExpression(String representation) {
            return BgeeEnum.convert(SummaryCallType.ExpressionSummary.class, representation);
        }
    }
    /**
     * {@link SummaryCallType} associated to {@link org.bgee.model.expressiondata.call.Call.DiffExpressionCall 
     * DiffExpressionCall}s. They represent an overall summary of the {@link CallType.DiffExpression 
     * DiffExpression} calls from individual data types, associated to a same {@code DiffExpressionCall}.
     * <ul>
     * <li>{@code DIFF_EXPRESSED}: gene shown in one or more analyses to have 
     * a significant change of its expression level in a condition (either over-expression 
     * or under-expression), as compared to its expression levels in other conditions of the analyses.
     * <li>{@code OVER_EXPRESSED}: gene shown in one or more analyses to have 
     * a significant over-expression in a condition, as compared to its expression levels 
     * in other conditions of the analyses.
     * <li>{@code UNDER_EXPRESSED}: gene shown in one or more analyses to have 
     * a significant under-expression in a condition, as compared to its expression levels 
     * in other conditions of the analyses.
     * <li>{@code NOT_DIFF_EXPRESSED}: a gene was tested for differential expression 
     * in a condition, but was never shown to have a significant variation of its expression level 
     * as compared to the other conditions of the analyses.
     * <li>{@code WEAK_AMBIGUITY_OVER}: there exists a call of over-expression 
     * generated from a data type, but another data type showed no significant variation 
     * of the level of expression of this gene in the same condition, or showed 
     * an absence of expression.
     * <li>{@code WEAK_AMBIGUITY_UNDER}: there exists a call of under-expression 
     * generated from a data type, but another data type showed no significant variation 
     * of the level of expression of this gene in the same condition.
     * <li>{@code WEAK_AMBIGUITY_NOT_DIFF}: a gene was shown to have no significant 
     * variation of its level of expression in a condition from a data type (but is considered expressed), 
     * but was shown to be never expressed in the same condition from another data type.
     * <li>{@code STRONG_AMBIGUITY}: there exists a call of over-expression or under-expression 
     * generated from a data type, but there exists a call in the opposite direction 
     * generated from another data type for the same gene and condition. For instance, 
     * gene A is reported to be over-expressed in the midbrain at young adult stage 
     * from Affymetrix data, but is reported to be under-expressed in the midbrain 
     * at young adult stage from RNA-Seq data.
     * </ul>
     * 
     * @author Frederic Bastian
     * @version Bgee 14 Mar. 2017
     * @see CallType.DiffExpression 
     * @see org.bgee.model.expressiondata.call.Call.DiffExpressionCall DiffExpressionCall
     * @since Bgee 13 Sept. 2015
     */
    public static enum DiffExpressionSummary implements SummaryCallType, BgeeEnumField {
        DIFF_EXPRESSED, OVER_EXPRESSED, UNDER_EXPRESSED, NOT_DIFF_EXPRESSED, 
        WEAK_AMBIGUITY_OVER, WEAK_AMBIGUITY_UNDER, WEAK_AMBIGUITY_NOT_DIFF, STRONG_AMBIGUITY;
        private final static Logger log = LogManager.getLogger(DiffExpressionSummary.class.getName());

        @Override
        public void checkPropagationState(PropagationState propState) throws IllegalArgumentException {
            log.traceEntry("{}", propState);
            //no propagation allowed for any diff. expression call type
            if (!PropagationState.SELF.equals(propState)) {
                throw log.throwing(new IllegalArgumentException("The following propagation "
                        + "is incorrect for the CallType " + this + ": " + propState));
            }
            log.traceExit();
        }

        @Override
        public Set<DataType> getAllowedDataTypes() {
            log.traceEntry();
            //For now, the same data types are allowed for all DiffExpressionSummary types.
            //So we just delegate to any type of DiffExpression CallType.
            //Just a check to make sure all allowed data types are still the same
            Set<Set<DataType>> allCallTypeDataTypes = EnumSet.allOf(DiffExpression.class)
                    .stream().map(c -> c.getAllowedDataTypes())
                    .collect(Collectors.toSet());
            if (allCallTypeDataTypes.size() != 1) {
                throw log.throwing(new IllegalStateException(
                        "Not all allowed data types are the same for all diff expression call types"));
            }
            return log.traceExit(DiffExpression.DIFF_EXPRESSED.getAllowedDataTypes());
        }

        @Override
        public String getStringRepresentation() {
            log.traceEntry();
            return log.traceExit(this.name());
        }

        /**
         * Convert the {@code String} representation of a call type from differential expression
         * analyses (for instance, retrieved from request) into a
         * {@code SummaryCallType.DiffExpressionSummary}.
         * Operation performed by calling {@link BgeeEnum#convert(Class, String)} with
         * {@code SummaryCallType.DiffExpressionSummary} as the {@code Class} argument,
         * and {@code representation} as the {@code String} argument.
         *
         * @param representation            A {@code String} representing a data quality.
         * @return                          A {@code SummaryCallType.DiffExpressionSummary}
         *                                  corresponding to {@code representation}.
         * @throws IllegalArgumentException If {@code representation} does not correspond
         *                                  to any {@code SummaryCallType.DiffExpressionSummary}.
         * @see #convert(Class, String)
         */
        public static final DiffExpressionSummary convertToDiffExpression(String representation) {
            return BgeeEnum.convert(SummaryCallType.DiffExpressionSummary.class, representation);
        }
    }
}
