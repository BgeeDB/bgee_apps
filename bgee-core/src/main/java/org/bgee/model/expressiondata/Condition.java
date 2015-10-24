package org.bgee.model.expressiondata;

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
 * @version Bgee 13 Oct. 2015
 * @since Bgee 13 Sept. 2015
 */
//XXX: how to manage multi-species conditions? Should we have a class SingleSpeciesCondition 
//and a class MultiSpeciesCondition? Or, only a Condition, using a "SingleSpeciesAnatEntity" 
//or a "MultiSpeciesAnatEntity", etc?
public class Condition {
    private final static Logger log = LogManager.getLogger(Condition.class.getName());
    
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
