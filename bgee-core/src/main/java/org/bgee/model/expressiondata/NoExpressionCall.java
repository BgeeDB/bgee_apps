package org.bgee.model.expressiondata;

import org.bgee.model.expressiondata.DataParameters.CallType;

public class NoExpressionCall extends Call {

    /**
     * Default constructor.
     */
    public NoExpressionCall() {
        super(CallType.Expression.NOTEXPRESSED);
    }
    
}
