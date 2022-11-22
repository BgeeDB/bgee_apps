package org.bgee.model.expressiondata.rawdata.baseelements;

import java.util.Comparator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.BgeeEnum.BgeeEnumField;
import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.anatdev.DevStage;
import org.bgee.model.expressiondata.BaseCondition;
import org.bgee.model.species.Species;

/**
 * This class describes the conditions used to annotate raw data in Bgee.
 *
 * @author Frederic Bastian
 * @version Bgee 15, Mar 2021
 * @since Bgee 14, Sept 2018
 */
public class RawDataCondition extends BaseCondition<RawDataCondition> implements Comparable<RawDataCondition> {
    private final static Logger log = LogManager.getLogger(RawDataCondition.class.getName());

    /**
     * A {@code Comparator} of {@code Condition}s used for {@link #compareTo(Condition)}.
     */
    private static final Comparator<RawDataCondition> COND_COMPARATOR = Comparator
            .<RawDataCondition, RawDataCondition>comparing(c -> c, BaseCondition.COND_COMPARATOR)
            .thenComparing(RawDataCondition::getSex, Comparator.nullsLast(RawDataSex::compareTo))
            .thenComparing(RawDataCondition::getStrain, Comparator.nullsLast(String::compareTo))
            .thenComparing(c -> c.getSpecies().getId(), Comparator.nullsLast(Integer::compareTo));

    public enum RawDataSex implements BgeeEnumField{
        NOT_ANNOTATED("not annotated"), HERMAPHRODITE("hermaphrodite"), FEMALE("female"), MALE("male"),
        MIXED("mixed"), NA("NA");
        
        private final String representation;
        
        private RawDataSex(String representation) {
            this.representation = representation;
        }

        @Override
        public String getStringRepresentation() {
            return this.representation;
        }
    }

    private final RawDataSex sex;
    private final String strain;

    public RawDataCondition(AnatEntity anatEntity, DevStage devStage, AnatEntity cellType, RawDataSex sex, 
            String strain, Species species) throws IllegalArgumentException {
        super(anatEntity, devStage, cellType, species);
        if (anatEntity == null && devStage == null && cellType == null && sex == null && strain == null) {
            throw log.throwing(new IllegalArgumentException(
                    "The anat. entity, the dev. stage, the cell type, the sex, and the strain "
                    + "cannot be null at the same time."));
        }
        this.sex = sex;
        this.strain = strain;
    }

    /**
     * @return  The {@code RawDataSex} used in this {@code RawDataCondition}.
     */
    public RawDataSex getSex() {
        return sex;
    }
    
    /**
     * @return  The {@code String} representing the strain used in this {@code RawDataCondition}.
     */
    public String getStrain() {
        return strain;
    }

    //*********************************
    //  COMPARETO/HASHCODE/EQUALS/TOSTRING
    //*********************************
    /**
     * Performs a simple comparison based on the attributes of this class.
     * 
     * @param other A {@code RawDataCondition} to be compared to this one.
     * @return      a negative {@code int}, zero, or a positive {@code int} 
     *              as the first argument is less than, equal to, or greater than the second.
     */
    @Override
    public int compareTo(RawDataCondition other) {
        return COND_COMPARATOR.compare(this, other);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((sex == null) ? 0 : sex.hashCode());
        result = prime * result + ((strain == null) ? 0 : strain.hashCode());
        return result;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        RawDataCondition other = (RawDataCondition) obj;
        if (sex != other.sex) {
            return false;
        }
        if (strain == null) {
            if (other.strain != null)
                return false;
        } else if (!strain.equals(other.strain))
            return false;
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("RawDataCondition [anatEntity=").append(getAnatEntity())
               .append(", devStage=").append(getDevStage())
               .append(", cellType=").append(getCellType())
               .append(", sex=").append(getSex())
               .append(", strain=").append(getStrain())
               .append(", species=").append(getSpecies())
               .append("]");
        return builder.toString();
    }
}
