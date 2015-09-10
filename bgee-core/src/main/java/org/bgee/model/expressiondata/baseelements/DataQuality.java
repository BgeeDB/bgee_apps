package org.bgee.model.expressiondata.baseelements;

/**
 * An {@code enum} defining the confidence levels associated to expression calls. 
 * These information is computed differently based on the type of call 
 * and the data type.
 * 
 * @author Frederic Bastian
 * @version Bgee 13 Sept. 2015
 * @since Bgee 13
 */
public enum DataQuality {
    //WARNING: these Enums must be declared in order, from the lowest quality 
    //to the highest quality. This is because the compareTo implementation 
    //of the Enum class will be used.
    LOW, HIGH;
}
