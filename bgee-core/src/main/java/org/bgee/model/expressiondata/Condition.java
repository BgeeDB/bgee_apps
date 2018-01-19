package org.bgee.model.expressiondata;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.anatdev.DevStage;
import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.model.species.Species;

/**
 * This class describes the conditions related to gene expression. It notably captures 
 * the IDs of an anatomical entity and a developmental stage used in a gene expression condition. 
 * It could be easily extended to also manage other parameters, such as the sex of a sample, 
 * the strain, or other experimental conditions (gene knock-out, drug treatment, etc).
 * <p>
 * Users should acquire {@code Condition}s as part of a {@link ConditionGraph} or a {@link Call},
 * using a {@link CallService}.
 * <p>
 * Note that this class implements {@code Comparable<Condition>}, allowing to perform 
 * simple comparisons based on the attributes of this class. For an ordering based 
 * on the relations between {@code Condition}s, see {@link ConditionGraph#compare(Condition, Condition)}.
 * 
 * @author  Frederic Bastian
 * @author  Valentine Rech de Laval
 * @version Bgee 14, Mar. 2017
 * @since   Bgee 13. Sept. 2015
 */
//XXX: how to manage multi-species conditions? Should we have a class SingleSpeciesCondition 
//and a class MultiSpeciesCondition? Or, only a Condition, using a "SingleSpeciesAnatEntity" 
//or a "MultiSpeciesAnatEntity", etc?
public class Condition implements Comparable<Condition> {
    private final static Logger log = LogManager.getLogger(Condition.class.getName());

    /**
     * A {@code Comparator} of {@code Condition}s used for {@link #compareTo(Condition)}.
     */
    //Note that since equals/hashCode methods don't rely on maxRanksByDataType and globalMaxRanksByDataType,
    //this Comparator is indeed consistent with equals/hashCode.
    private static final Comparator<Condition> COND_COMPARATOR = Comparator
            .comparing(Condition::getAnatEntityId, Comparator.nullsLast(String::compareTo))
            .thenComparing(Condition::getDevStageId, Comparator.nullsLast(String::compareTo))
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
     * @see #getMaxRanksByDataType()
     */
    private final Map<DataType, BigDecimal> maxRanksByDataType;
    /**
     * @see #getGlobalMaxRanksByDataType()
     */
    private final Map<DataType, BigDecimal> globalMaxRanksByDataType;
    
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
    public Condition(AnatEntity anatEntity, DevStage devStage, Species species)
            throws IllegalArgumentException {
        this(anatEntity, devStage, species, null, null);
    }

    /**
     * Constructor providing the IDs of the anatomical entity, the developmental stage, 
     * and species ID of this {@code Condition}.
     *
     * @param anatEntity                    The {@code AnatEntity} used in this gene expression
     *                                      condition, without the descriptions loaded
     *                                      for lower memory usage.
     * @param devStage                      The {@code DevStage} used in this gene expression
     *                                      condition, without the descriptions loaded
     *                                      for lower memory usage.
     * @param species                       The {@code Species} considered in this gene expression condition.
     * @param maxRanksByDataType            A {@code Map} where keys are {@code DataType}s,
     *                                      the associated values being {@code BigDecimal}s
     *                                      corresponding to the max rank for this data type,
     *                                      solely in this condition, not taking into account
     *                                      child conditions.
     * @param globalMaxRanksByDataType  A {@code Map} where keys are {@code DataType}s,
     *                                      the associated values being {@code BigDecimal}s
     *                                      corresponding to the max rank for this data type,
     *                                      taking into account this condition, but also all
     *                                      its child conditions.
     * @throws IllegalArgumentException     If both {@code anatEntity} and {@code devStage} are blanks
     *                                      or if {@code speciesId} is less than 1.
     */
    public Condition(AnatEntity anatEntity, DevStage devStage, Species species,
            Map<DataType, BigDecimal> maxRanksByDataType,
            Map<DataType, BigDecimal> globalMaxRanksByDataType) throws IllegalArgumentException {
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
        this.maxRanksByDataType = Collections.unmodifiableMap(maxRanksByDataType == null?
                                    new HashMap<>(): maxRanksByDataType);
        this.globalMaxRanksByDataType = Collections.unmodifiableMap(
                globalMaxRanksByDataType == null? new HashMap<>(): globalMaxRanksByDataType);
    }

    //*********************************
    //  INSTANCE METHODS
    //*********************************
    /**
     * Determine whether the other {@code Condition} is more precise than this {@code Condition}. 
     * This method is only used for convenience, and actually delegates to 
     * {@link ConditionGraph#isConditionMorePrecise(Condition, Condition)}, with this {@code Condition} 
     * as first argument, and {@code other} as second argument. See this other method's description 
     * for more information.
     * 
     * @param other     A {@code Condition} to be checked whether it is more precise 
     *                  than this {@code Condition}.
     * @param graph     A {@code ConditionGraph} used to determine relations between {@code Condition}s. 
     *                  It should contain this {@code Condition} and {@code other}.
     * @return          {@code true} if {@code other} is more precise than this {@code Condition}. 
     * @throws IllegalArgumentException If this {@code Condition}, or {@code other}, are not registered to 
     *                                  {@code graph}.
     */
    public boolean isConditionMorePrecise(Condition other, ConditionGraph graph) throws IllegalArgumentException {
        log.entry(other, graph);
        return log.exit(graph.isConditionMorePrecise(this, other));
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

    /**
     * @return   A {@code Map} where keys are {@code DataType}s, the associated values being 
     *           {@code BigDecimal}s corresponding to the max rank for this data type,
     *           solely in this condition, not taking into account child conditions.
     */
    public Map<DataType, BigDecimal> getMaxRanksByDataType() {
        return maxRanksByDataType;
    }
    /**
     * @return  A {@code Map} where keys are {@code DataType}s, the associated values being
     *          {@code BigDecimal}s corresponding to the max rank for this data type,
     *          taking into account this condition, but also all its child conditions.
     */
    public Map<DataType, BigDecimal> getGlobalMaxRanksByDataType() {
        return globalMaxRanksByDataType;
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
    public int compareTo(Condition other) {
        return COND_COMPARATOR.compare(this, other);
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((anatEntity == null) ? 0 : anatEntity.hashCode());
        result = prime * result + ((devStage == null) ? 0 : devStage.hashCode());
        result = prime * result + ((species == null) ? 0 : species.hashCode());
        //Note that we don't rely on maxRanksByDataType and globalMaxRanksByDataType on purpose.
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
        if (getClass() != obj.getClass()) {
            return false;
        }
        Condition other = (Condition) obj;
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
        //Note that we don't rely on maxRanksByDataType and globalMaxRanksByDataType on purpose.
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Condition [anatEntity=").append(anatEntity)
               .append(", devStage=").append(devStage)
               .append(", species=").append(species)
               .append(", maxRanksByDataType=").append(maxRanksByDataType)
               .append(", globalMaxRanksByDataType=").append(globalMaxRanksByDataType)
               .append("]");
        return builder.toString();
    }
}
