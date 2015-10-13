package org.bgee.model.expressiondata.baseelements;

public interface SummaryCallType {
    public static enum ExpressionSummary implements SummaryCallType {
        EXPRESSED, NOT_EXPRESSED, AMBIGUITY_ETC;
    }
    public static enum DiffExpressionSummary implements SummaryCallType {
        DIFF_EXPRESSED, OVER_EXPRESSED, UNDER_EXPRESSED, NOT_DIFF_EXPRESSED, AMBIGUITY_ETC;
    }
}
