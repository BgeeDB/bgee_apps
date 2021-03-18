package org.bgee.model.expressiondata.rawdata;

import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.anatdev.DevStage;
import org.bgee.model.expressiondata.BaseCondition;
import org.bgee.model.species.Species;

/**
 * This class describes the conditions used to annotate raw data in Bgee.
 *
 * @author Frederic Bastian
 * @version Bgee 14, Sept 2018
 * @since Bgee 14, Sept 2018
 */
public class RawDataCondition extends BaseCondition<RawDataCondition> {

    public RawDataCondition(AnatEntity anatEntity, DevStage devStage, AnatEntity cellType, Sex sex, 
            String strain, Species species) throws IllegalArgumentException {
        super(anatEntity, devStage, cellType, sex, strain, species);
    }

    //*********************************
    //  COMPARETO/HASHCODE/EQUALS/TOSTRING
    //*********************************

    //So far there is no other attributes than the ones in BaseCondition,
    //we might need to implement equals/hashCode in the future of other attributes are added.

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("RawDataCondition [anatEntity=").append(getAnatEntity())
               .append(", devStage=").append(getDevStage())
               .append(", species=").append(getSpecies())
               .append("]");
        return builder.toString();
    }
}
