package org.bgee.model.expressiondata.baseelements;

public enum DecorelationType {
    
    NONE ("classic"), 
    ELIM ("elim"), 
    WEIGTH ("weight"), 
    PARENT_CHILD ("parentchild");
    
    private final String code;
    
    DecorelationType(String code){
        this.code = code;
    }

    /**
     * @return the code
     */
    public String getCode() {
        return code;
    }
    
}
