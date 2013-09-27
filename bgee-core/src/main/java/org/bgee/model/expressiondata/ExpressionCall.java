package org.bgee.model.expressiondata;

import org.bgee.model.expressiondata.DataParameters.CallType;

public class ExpressionCall extends Call{

    /**
     * Default constructor.
     */
    public ExpressionCall() {
        super(CallType.Expression.EXPRESSED);
    }
    
}
