package org.bgee.model.expressiondata;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.expressiondata.baseelements.DataType;

/**
 * This class describes the conditions related to gene expression. It notably captures 
 * the IDs of an anatomical entity and a developmental stage used in a gene expression condition. 
 * It could be easily extended to also manage other parameters, such as the sex of a sample, 
 * the strain, or other experimental conditions (gene knock-out, drug treatment, etc).
 * <p>
 * Note that this class implements {@code Comparable<Condition>}, allowing to perform 
 * simple comparisons based on the attributes of this class. For an ordering based 
 * on the relations between {@code Condition}s, see {@link ConditionUtils#compare(Condition, Condition)}.
 * 
 * @author  Frederic Bastian
 * @author  Valentine Rech de Laval
 * @version Bgee 14, Feb. 2017
 * @since   Bgee 13. Sept. 2015
 */
//XXX: how to manage multi-species conditions? Should we have a class SingleSpeciesCondition 
//and a class MultiSpeciesCondition? Or, only a Condition, using a "SingleSpeciesAnatEntity" 
//or a "MultiSpeciesAnatEntity", etc?
//TODO: for various reasons, I think this class should be single species, 
//and simply have a mandatory speciesId attribute. Mapping between homologous conditions 
//should be managed in a different way. 
//TODO: I guess this means the ConditionUtils should use the new MultiSpeciesOntology mechanism, 
//to be able to perform computations over any species.
//FIXME: provides the Species object rather than the speciesId
//FIXME: in a second step, also provides the AnatEntity and DevStage objects rather than the IDs?
//Not sure about this one, organ and stage descriptions are rather big Strings.
public class Condition implements Comparable<Condition> {
    private final static Logger log = LogManager.getLogger(Condition.class.getName());

    /**
     * A {@code Comparator} of {@code Condition}s used for {@link #compareTo(Condition)}.
     */
    private static final Comparator<Condition> COND_COMPARATOR = Comparator
            .comparing(Condition::getAnatEntityId, Comparator.nullsLast(String::compareTo))
            .thenComparing(Condition::getDevStageId, Comparator.nullsLast(String::compareTo))
            .thenComparing(Condition::getSpeciesId, Comparator.nullsLast(Integer::compareTo));
    
    
    /**
     * @see #getAnatEntityId()
     */
    private final String anatEntityId;
    /**
     * @see #getDevStageId()
     */
    private final String devStageId;
    /**
     * @see #getSpeciesId()
     */
    private final Integer speciesId;

    private final Map<DataType, BigDecimal> maxRanksByDataType;
    
    /**
     * Constructor providing the IDs of the anatomical entity, the developmental stage, 
     * and species ID of this {@code Condition}.
     * 
     * @param anatEntityId  A {@code String} that is the ID of the anatomical entity 
     *                      used in this gene expression condition.
     * @param devStageId    A {@code String} that is the ID of the developmental stage  
     *                      used in this gene expression condition.
     * @param speciesId     An {@code Integer} that is the ID of the species  
     *                      used in this gene expression condition.
     * @throws IllegalArgumentException If both {@code anatEntity} and {@code devStage} are blanks 
     *                                  or if {@code speciesId} is empty or less than 1. 
     */
    public Condition(String anatEntityId, String devStageId, Integer speciesId)
            throws IllegalArgumentException {
        this(anatEntityId, devStageId, speciesId, null);
    }

    /**
     * Constructor providing the IDs of the anatomical entity, the developmental stage, 
     * and species ID of this {@code Condition}.
     * 
     * @param anatEntityId          A {@code String} that is the ID of the anatomical entity 
     *                              used in this gene expression condition.
     * @param devStageId            A {@code String} that is the ID of the developmental stage  
     *                              used in this gene expression condition.
     * @param speciesId             An {@code Integer} that is the ID of the species  
     *                              used in this gene expression condition.
     * @param maxRanksByDataType    A {@code Map} where keys are {@code DataType}s corresponding
     *                              to the data type, the associated values being
     *                              {@code BigDecimal}s corresponding to max ranks.
     * @throws IllegalArgumentException If both {@code anatEntity} and {@code devStage} are blanks 
     *                                  or if {@code speciesId} is empty or less than 1. 
     */
    public Condition(String anatEntityId, String devStageId, Integer speciesId,
            Map<DataType, BigDecimal> maxRanksByDataType) throws IllegalArgumentException {
        if (StringUtils.isBlank(anatEntityId) && StringUtils.isBlank(devStageId)) {
            throw log.throwing(new IllegalArgumentException(
                    "The anat. entity ID and the dev. stage ID cannot be both blank."));
        }
        if (speciesId == null || speciesId <= 0) {
            throw log.throwing(new IllegalArgumentException(
                "The species ID cannot be null or equals or less than 1."));
        }
        this.anatEntityId       = anatEntityId;
        this.devStageId         = devStageId;
        this.speciesId          = speciesId;
        this.maxRanksByDataType = Collections.unmodifiableMap(maxRanksByDataType == null?
                                    new HashMap<>(): maxRanksByDataType);
    }
    
    /**
     * Determine whether the other {@code Condition} is more precise than this {@code Condition}. 
     * This method is only used for convenience, and actually delegates to 
     * {@link ConditionUtils#isConditionMorePrecise(Condition, Condition)}, with this {@code Condition} 
     * as first argument, and {@code other} as second argument. See this other method's description 
     * for more information.
     * 
     * @param other     A {@code Condition} to be checked whether it is more precise 
     *                  than this {@code Condition}.
     * @param utils     A {@code ConditionUtils} used to determine relations between {@code Condition}s. 
     *                  It should contain this {@code Condition} and {@code other}.
     * @return          {@code true} if {@code other} is more precise than this {@code Condition}. 
     * @throws IllegalArgumentException If this {@code Condition}, or {@code other}, are not registered to 
     *                                  {@code utils}.
     */
    public boolean isConditionMorePrecise(Condition other, ConditionUtils utils) throws IllegalArgumentException {
        log.entry(other, utils);
        return log.exit(utils.isConditionMorePrecise(this, other));
    }

    //*********************************
    //  GETTERS/SETTERS
    //*********************************
    /**
     * @return  A {@code String} that is the ID of the anatomical entity 
     *          used in this gene expression condition.
     */
    public String getAnatEntityId() {
        return anatEntityId;
    }
    /**
     * @return  A {@code String} that is the ID of the developmental stage 
     *          used in this gene expression condition.
     */
    public String getDevStageId() {
        return devStageId;
    }
    /**
     * @return  An {@code Integer} that is the ID of the species 
     *          used in this gene expression condition.
     */
    public Integer getSpeciesId() {
        return speciesId;
    }

    /** TODO
     * @return
     */
    public Map<DataType, BigDecimal> getMaxRanksByDataType() {
        return maxRanksByDataType;
    }

    //*********************************
    //  COMPARETO/HASHCODE/EQUALS/TOSTRING
    //*********************************
    /**
     * Performs a simple comparison based on the attributes of this class. For an ordering based 
     * on the relations between {@code Condition}s, see {@link ConditionUtils#compare(Condition, Condition)}.
     * 
     * @param other A {@code Condition} to be compared to this one.
     * @return      a negative {@code int}, zero, or a positive {@code int} 
     *              as the first argument is less than, equal to, or greater than the second.
     * @see ConditionUtils#compare(Condition, Condition)
     */
    @Override
    public int compareTo(Condition other) {
        return COND_COMPARATOR.compare(this, other);
    }
    
    //TODO: to remove to rely on the Entity hashCode/equals method
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((anatEntityId == null) ? 0 : anatEntityId.hashCode());
        result = prime * result + ((devStageId == null) ? 0 : devStageId.hashCode());
        result = prime * result + ((speciesId == null) ? 0 : speciesId.hashCode());
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
        if (anatEntityId == null) {
            if (other.anatEntityId != null) {
                return false;
            }
        } else if (!anatEntityId.equals(other.anatEntityId)) {
            return false;
        }
        if (devStageId == null) {
            if (other.devStageId != null) {
                return false;
            }
        } else if (!devStageId.equals(other.devStageId)) {
            return false;
        }
        if (speciesId == null) {
            if (other.speciesId != null) {
                return false;
            }
        } else if (!speciesId.equals(other.speciesId)) {
            return false;
        }
        return true;
    }
    
    @Override
    public String toString() {
        return "Condition [anatEntityId=" + anatEntityId + ", devStageId=" + devStageId + 
            ", speciesId=" + speciesId + "]";
    }
}
