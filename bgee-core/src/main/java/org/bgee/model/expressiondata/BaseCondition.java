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
 * @version Bgee 15, Mar. 2021
 * @since Bgee 14, Sept 2018
 *
 * @param <T>   The precise type of the class that will extend this {@code BaseCondition} class.
 */
public abstract class BaseCondition<T extends BaseCondition<?>> {
    private final static Logger log = LogManager.getLogger(BaseCondition.class.getName());

    /**
     * A {@code Comparator} of {@code BaseCondition}s used for {@link #compareTo(BaseCondition)}.
     */
    protected static final Comparator<BaseCondition<?>> COND_COMPARATOR = Comparator
            .<BaseCondition<?>, String>comparing(BaseCondition::getAnatEntityId, Comparator.nullsLast(String::compareTo))
            .thenComparing(BaseCondition::getCellTypeId, Comparator.nullsLast(String::compareTo))
            .thenComparing(BaseCondition::getDevStageId, Comparator.nullsLast(String::compareTo));
    

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
     * @see #getCellType()
     */
    private final AnatEntity cellType;
    /**
     * @see #getStrain()
     */
    private final String strain;
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
     * @param strain        The {@code String} describing the strain used in this 
     *                      gene expression condition.
     * @param species       The {@code Species} considered in this gene expression condition.
     * @throws IllegalArgumentException If both {@code anatEntity} and {@code devStage} are {@code null}, 
     *                                  or if {@code speciesId} is less than 1.
     */
    protected BaseCondition(AnatEntity anatEntity, DevStage devStage, AnatEntity cellType,
            String strain, Species species) throws IllegalArgumentException {
        if (species == null) {
            throw log.throwing(new IllegalArgumentException("The species cannot be null."));
        }
        this.anatEntity         = anatEntity;
        this.devStage           = devStage;
        this.cellType           = cellType;
        this.strain             = strain;
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
     * @return  The {@code AnatEntity} corresponding to a cell type used in 
     *          this gene expression condition, without the descriptions 
     *          loaded for lower memory usage.
     *          Can be {@code null}.
     */
    public AnatEntity getCellType() {
        return cellType;
    }
    /**
     * @return  A {@code String} that is the ID of the cell type 
     *          used in this gene expression condition.
     *          Can be {@code null}.
     */
    public String getCellTypeId() {
        return cellType == null? null: cellType.getId();
    }
    /**
     * @return  The {@code String} corresponding to the strain used in 
     * this gene expression condition. Can be {@code null}.
     */
    public String getStrain() {
        return strain;
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
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((anatEntity == null) ? 0 : anatEntity.hashCode());
        result = prime * result + ((cellType == null) ? 0 : cellType.hashCode());
        result = prime * result + ((devStage == null) ? 0 : devStage.hashCode());
        result = prime * result + ((species == null) ? 0 : species.hashCode());
        result = prime * result + ((strain == null) ? 0 : strain.hashCode());
        return result;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof BaseCondition))
            return false;
        BaseCondition<?> other = (BaseCondition<?>) obj;
        if (anatEntity == null) {
            if (other.anatEntity != null)
                return false;
        } else if (!anatEntity.equals(other.anatEntity))
            return false;
        if (cellType == null) {
            if (other.cellType != null)
                return false;
        } else if (!cellType.equals(other.cellType))
            return false;
        if (devStage == null) {
            if (other.devStage != null)
                return false;
        } else if (!devStage.equals(other.devStage))
            return false;
        if (species == null) {
            if (other.species != null)
                return false;
        } else if (!species.equals(other.species))
            return false;
        if (strain == null) {
            if (other.strain != null)
                return false;
        } else if (!strain.equals(other.strain))
            return false;
        return true;
    }
}