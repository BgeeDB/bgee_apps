package org.bgee.model.expressiondata.baseelements;

import org.bgee.model.BgeeEnum;
import org.bgee.model.BgeeEnum.BgeeEnumField;

/**
 * An {@code enum} defining the confidence levels associated to expression calls. 
 * These information is computed differently based on the type of call 
 * and the data type.
 * 
 * @author Frederic Bastian
 * @version Bgee 13 Sept. 2015
 * @since Bgee 13
 */
public enum DataQuality implements BgeeEnumField {
    //WARNING: these Enums must be declared in order, from the lowest quality 
    //to the highest quality. This is because the compareTo implementation 
    //of the Enum class will be used.
    LOW, HIGH;

    @Override
    public String getStringRepresentation() {
        return this.name();
    }
    
    /**
     * Convert the {@code String} representation of a data quality (for instance, 
     * retrieved from request) into a {@code DataQuality}.
     * Operation performed by calling {@link BgeeEnum#convert(Class, String)} with 
     * {@code DataQuality} as the {@code Class} argument, and {@code representation} 
     * as the {@code String} argument.
     * 
     * @param representation            A {@code String} representing a data quality.
     * @return                          A {@code DataQuality} corresponding 
     *                                  to {@code representation}.
     * @throw IllegalArgumentException  If {@code representation} does not correspond 
     *                                  to any {@code DataQuality}.
     * @see #convert(Class, String)
     */
    public static final DataQuality convertToDataQuality(String representation) {
        return BgeeEnum.convert(DataQuality.class, representation);
    }
}
