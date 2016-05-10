package org.bgee.model.expressiondata.baseelements;

/**
 * This interface is used to type the summary call types associated to 
 * {@link org.bgee.model.expressiondata.Call Call}s. They represent an overall summary 
 * of the {@link CallType}s from individual data types, associated to a same {@code Call}.
 * 
 * @author Frederic Bastian
 * @version Bgee 13 Nov. 2015
 * @see CallType
 * @see org.bgee.model.expressiondata.Call Call
 * @since Bgee 13 Sept. 2015
 */
public interface SummaryCallType {
    /**
     * {@link SummaryCallType} associated to {@link org.bgee.model.expressiondata.Call.ExpressionCall 
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
     * @version Bgee 13 Nov. 2015
     * @see CallType.Expression 
     * @see org.bgee.model.expressiondata.Call.ExpressionCall ExpressionCall
     * @since Bgee 13 Sept. 2015
     */
    public static enum ExpressionSummary implements SummaryCallType {
        EXPRESSED, NOT_EXPRESSED, WEAK_AMBIGUITY, STRONG_AMBIGUITY;
    }
    /**
     * {@link SummaryCallType} associated to {@link org.bgee.model.expressiondata.Call.DiffExpressionCall 
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
     * @version Bgee 13 Nov. 2015
     * @see CallType.DiffExpression 
     * @see org.bgee.model.expressiondata.Call.DiffExpressionCall DiffExpressionCall
     * @since Bgee 13 Sept. 2015
     */
    public static enum DiffExpressionSummary implements SummaryCallType {
        DIFF_EXPRESSED, OVER_EXPRESSED, UNDER_EXPRESSED, NOT_DIFF_EXPRESSED, 
        WEAK_AMBIGUITY_OVER, WEAK_AMBIGUITY_UNDER, WEAK_AMBIGUITY_NOT_DIFF, STRONG_AMBIGUITY;
    }
}
