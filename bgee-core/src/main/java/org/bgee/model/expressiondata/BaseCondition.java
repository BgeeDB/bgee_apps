package org.bgee.model.expressiondata;

import java.util.Comparator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.anatdev.DevStage;
import org.bgee.model.species.Species;

/**
 * Parent class of classes describing conditions in Bgee.
 *
 * @author Frederic Bastian
 * @version Bgee 14, Sept 2018
 * @since Bgee 14, Sept 2018
 *
 * @param <T>   The precise type of the class that will extend this {@code BaseCondition} class.
 */
public abstract class BaseCondition<T extends BaseCondition<?>> implements Comparable<T> {
    private final static Logger log = LogManager.getLogger(BaseCondition.class.getName());

    /**
     * A {@code Comparator} of {@code Condition}s used for {@link #compareTo(Condition)}.
     */
    private static final Comparator<BaseCondition<?>> COND_COMPARATOR = Comparator
            .<BaseCondition<?>, String>comparing(BaseCondition::getAnatEntityId, Comparator.nullsLast(String::compareTo))
            .thenComparing(BaseCondition::getDevStageId, Comparator.nullsLast(String::compareTo))
            .thenComparing(c -> c.getSpecies().getId(), Comparator.nullsLast(Integer::compareTo));
    

    //*********************************
    //  ATTRIBUTES AND CONSTRUCTORS
    //*********************************
    /**
     * @see #getAnatEntity()
     */
    private final AnatEntity anatEntity;
    /**
     * @see #getDevStage()
     */
    private final DevStage devStage;
    /**
     * @see #getSpecies()
     */
    private final Species species;
    
    /**
     * Constructor providing the IDs of the anatomical entity, the developmental stage, 
     * and species ID of this {@code Condition}.
     *
     * @param anatEntity    The {@code AnatEntity} used in this gene expression condition,
     *                      without the descriptions loaded for lower memory usage.
     * @param devStage      The {@code DevStage} used in this gene expression condition,
     *                      without the descriptions loaded for lower memory usage.
     * @param species       The {@code Species} considered in this gene expression condition.
     * @throws IllegalArgumentException If both {@code anatEntity} and {@code devStage} are {@code null}, 
     *                                  or if {@code speciesId} is less than 1.
     */
    protected BaseCondition(AnatEntity anatEntity, DevStage devStage, Species species) throws IllegalArgumentException {
        if (anatEntity == null && devStage == null) {
            throw log.throwing(new IllegalArgumentException(
                    "The anat. entity and the dev. stage cannot be both null."));
        }
        if (species == null) {
            throw log.throwing(new IllegalArgumentException("The species cannot be null."));
        }
        this.anatEntity         = anatEntity;
        this.devStage           = devStage;
        this.species            = species;
    }

    //*********************************
    //  GETTERS
    //*********************************
    /**
     * @return  The {@code AnatEntity} used in this gene expression condition,
     *          without the descriptions loaded for lower memory usage.
     *          Can be {@code null}.
     */
    public AnatEntity getAnatEntity() {
        return anatEntity;
    }
    /**
     * @return  A {@code String} that is the ID of the anatomical entity 
     *          used in this gene expression condition.
     *          Can be {@code null}.
     */
    public String getAnatEntityId() {
        return anatEntity == null? null: anatEntity.getId();
    }
    /**
     * @return  The {@code DevStage} used in this gene expression condition,
     *          without the descriptions loaded for lower memory usage.
     *          Can be {@code null}.
     */
    public DevStage getDevStage() {
        return devStage;
    }
    /**
     * @return  A {@code String} that is the ID of the developmental stage 
     *          used in this gene expression condition.
     *          Can be {@code null}.
     */
    public String getDevStageId() {
        return devStage == null? null: devStage.getId();
    }
    /**
     * @return  The {@code Species} considered in this gene expression condition.
     */
    public Species getSpecies() {
        return species;
    }
    /**
     * @return  An {@code int} that is the NCBI ID of the {@code Species} considered
     *          in this gene expression condition.
     */
    public int getSpeciesId() {
        return species.getId();
    }

    //*********************************
    //  COMPARETO/HASHCODE/EQUALS/TOSTRING
    //*********************************
    /**
     * Performs a simple comparison based on the attributes of this class. For an ordering based 
     * on the relations between {@code Condition}s, see {@link ConditionGraph#compare(Condition, Condition)}.
     * 
     * @param other A {@code Condition} to be compared to this one.
     * @return      a negative {@code int}, zero, or a positive {@code int} 
     *              as the first argument is less than, equal to, or greater than the second.
     * @see ConditionGraph#compare(Condition, Condition)
     */
    @Override
    public int compareTo(T other) {
        return COND_COMPARATOR.compare(this, other);
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((anatEntity == null) ? 0 : anatEntity.hashCode());
        result = prime * result + ((devStage == null) ? 0 : devStage.hashCode());
        result = prime * result + ((species == null) ? 0 : species.hashCode());
        return result;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof BaseCondition)) {
            return false;
        }
        BaseCondition<?> other = (BaseCondition<?>) obj;
        if (anatEntity == null) {
            if (other.anatEntity != null) {
                return false;
            }
        } else if (!anatEntity.equals(other.anatEntity)) {
            return false;
        }
        if (devStage == null) {
            if (other.devStage != null) {
                return false;
            }
        } else if (!devStage.equals(other.devStage)) {
            return false;
        }
        if (species == null) {
            if (other.species != null) {
                return false;
            }
        } else if (!species.equals(other.species)) {
            return false;
        }
        return true;
    }
}
