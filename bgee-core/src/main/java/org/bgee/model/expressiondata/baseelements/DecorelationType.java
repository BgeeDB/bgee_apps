package org.bgee.model.expressiondata.baseelements;

import org.bgee.model.BgeeEnum;
import org.bgee.model.BgeeEnum.BgeeEnumField;

public enum DecorelationType implements BgeeEnumField {
    
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
    
    @Override
    public String getStringRepresentation() {
        return this.getCode();
    }
    
    /**
     * Convert the {@code String} representation of a decorrelation type (for instance, 
     * retrieved from request) into a {@code DecorelationType}.
     * Operation performed by calling {@link BgeeEnum#convert(Class, String)} with 
     * {@code DecorelationType} as the {@code Class} argument, and {@code representation} 
     * as the {@code String} argument.
     * 
     * @param representation            A {@code String} representing a decorrelation type.
     * @return                          A {@code DecorelationType} corresponding 
     *                                  to {@code representation}.
     * @throw IllegalArgumentException  If {@code representation} does not correspond 
     *                                  to any {@code DecorelationType}.
     * @see #convert(Class, String)
     */
    public static final DecorelationType convertToDecorelationType(String representation) {
        return BgeeEnum.convert(DecorelationType.class, representation);
    }
}
