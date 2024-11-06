package org.bgee.model.expressiondata.rawdata.baseelements;

import java.util.Collection;
import java.util.Comparator;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.BgeeEnum.BgeeEnumField;
import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.anatdev.DevStage;
import org.bgee.model.anatdev.Sex;
import org.bgee.model.anatdev.Strain;
import org.bgee.model.dao.api.expressiondata.call.ConditionDAO;
import org.bgee.model.expressiondata.BaseCondition;
import org.bgee.model.expressiondata.call.CallService;
import org.bgee.model.expressiondata.call.Condition;
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

    public Condition toCondition(Collection<CallService.Attribute> condParameters) {
        log.traceEntry("{}", condParameters);
        if (!CallService.Attribute.getAllConditionParameters().containsAll(condParameters)) {
            throw log.throwing(new IllegalArgumentException(
                    "condParamaters should only contain Attributes that are"
                    + "condition parameters"));
        }
        
        Condition cond = new Condition(
                condParameters.contains(CallService.Attribute.ANAT_ENTITY_ID)?
                        new AnatEntity(ConditionDAO.ANAT_ENTITY_ROOT_ID) : this.getAnatEntity(),
                condParameters.contains(CallService.Attribute.DEV_STAGE_ID)?
                        new DevStage(ConditionDAO.DEV_STAGE_ROOT_ID) : this.getDevStage(),
                condParameters.contains(CallService.Attribute.CELL_TYPE_ID)?
                           new AnatEntity(ConditionDAO.CELL_TYPE_ROOT_ID) : this.getCellType(),
                condParameters.contains(CallService.Attribute.SEX_ID)?
                          new Sex(ConditionDAO.SEX_ROOT_ID) : new Sex(this.getSex().getStringRepresentation()),
                condParameters.contains(CallService.Attribute.STRAIN_ID)?
                        new Strain(ConditionDAO.STRAIN_ROOT_ID) : new Strain(this.getStrain()),
                this.getSpecies());
        return log.traceExit(cond);
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
        result = prime * result + Objects.hash(sex, strain);
        return result;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        RawDataCondition other = (RawDataCondition) obj;
        return sex == other.sex && Objects.equals(strain, other.strain);
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
