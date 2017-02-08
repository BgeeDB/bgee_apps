package org.bgee.model.expressiondata.baseelements;

import org.bgee.model.BgeeEnum.BgeeEnumField;

/** 
 * An {@code enum} defining the confidence levels associated to expression calls. 
 * They represent an overall summary of the {@link DataQuality}s from individual data qualities,
 * associated to a same {@code Call}.
 * 
 * @author Valentine Rech de Laval
 * @version Bgee 14, Feb. 2017
 * @since   Bgee 14, Jan. 2017
 */
public enum SummaryQuality implements BgeeEnumField {
    //WARNING: these Enums must be declared in order, from the lowest quality 
    //to the highest quality. This is because the compareTo implementation 
    //of the Enum class will be used.
	BRONZE, SILVER, GOLD;

	@Override
	public String getStringRepresentation() {
        return this.name();
	}
}
