package org.bgee.model.expressiondata;

import java.util.Comparator;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class describes the conditions related to gene expression. It notably captures 
 * the IDs of an anatomical entity and a developmental stage used in a gene expression condition. 
 * It could be easily extended to also manage other parameters, such as the sex of a sample, 
 * the strain, or other experimental conditions (gene knock-out, drug treatment, etc).
 * 
 * @author Frederic Bastian
 * @version Bgee 13 Dec. 2015
 * @since Bgee 13 Sept. 2015
 */
//XXX: how to manage multi-species conditions? Should we have a class SingleSpeciesCondition 
//and a class MultiSpeciesCondition? Or, only a Condition, using a "SingleSpeciesAnatEntity" 
//or a "MultiSpeciesAnatEntity", etc?
public class Condition implements Comparable<Condition> {
    private final static Logger log = LogManager.getLogger(Condition.class.getName());

    /**
     * A {@code Comparator} of {@code Condition}s used for {@link #compareTo(Condition)}.
     */
    private static final Comparator<Condition> COND_COMPARATOR = Comparator
            .comparing(Condition::getAnatEntityId, Comparator.nullsLast(String::compareTo))
            .thenComparing(Condition::getDevStageId, Comparator.nullsLast(String::compareTo));
    
    /**
     * @see #getAnatEntityId()
     */
    private final String anatEntityId;
    /**
     * @see #getDevStageId()
     */
    private final String devStageId;
    
    /**
     * Constructor providing the IDs of the anatomical entity and the developmental stage 
     * of this {@code Condition}.
     * 
     * @param anatEntityId  A {@code String} that is the ID of the anatomical entity 
     *                      used in this gene expression condition.
     * @param devStageId    A {@code String} that is the ID of the developmental stage  
     *                      used in this gene expression condition.
     * @throws IllegalArgumentException if both {@code anatEntity} and {@code devStage} are blank. 
     */
    public Condition(String anatEntityId, String devStageId) throws IllegalArgumentException {
        if (StringUtils.isBlank(anatEntityId) && StringUtils.isBlank(devStageId)) {
            throw log.throwing(new IllegalArgumentException(
                    "The anat. entity ID and the dev. stage ID cannot be both blank."));
        }
        this.anatEntityId = anatEntityId;
        this.devStageId   = devStageId;
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

    //*********************************
    //  COMPARETO/HASHCODE/EQUALS/TOSTRING
    //*********************************
    @Override
    public int compareTo(Condition other) {
        return COND_COMPARATOR.compare(this, other);
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((anatEntityId == null) ? 0 : anatEntityId.hashCode());
        result = prime * result + ((devStageId == null) ? 0 : devStageId.hashCode());
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
        return true;
    }
    
    @Override
    public String toString() {
        return "Condition [anatEntityId=" + anatEntityId + ", devStageId=" + devStageId + "]";
    }
}
