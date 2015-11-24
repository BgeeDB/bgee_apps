package org.bgee.model.expressiondata.baseelements;

import org.bgee.model.BgeeEnum;
import org.bgee.model.BgeeEnum.BgeeEnumField;

public enum DecorrelationType implements BgeeEnumField {
    
    NONE ("classic"), 
    ELIM ("elim"), 
    WEIGTH ("weight"), 
    PARENT_CHILD ("parentchild");
    
    private final String code;
    
    DecorrelationType(String code){
        this.code = code;
    }

    /**
     * @return the code
     */
    public String getCode() {
        return code;
    }
    
    @Override
    public String getStringRepresentation() {
        return this.getCode();
    }
    
    /**
     * Convert the {@code String} representation of a decorrelation type (for instance, 
     * retrieved from request) into a {@code DecorrelationType}.
     * Operation performed by calling {@link BgeeEnum#convert(Class, String)} with 
     * {@code DecorrelationType} as the {@code Class} argument, and {@code representation} 
     * as the {@code String} argument.
     * 
     * @param representation            A {@code String} representing a decorrelation type.
     * @return                          A {@code DecorrelationType} corresponding 
     *                                  to {@code representation}.
     * @throw IllegalArgumentException  If {@code representation} does not correspond 
     *                                  to any {@code DecorrelationType}.
     * @see #convert(Class, String)
     */
    public static final DecorrelationType convertToDecorrelationType(String representation) {
        return BgeeEnum.convert(DecorrelationType.class, representation);
    }
}
