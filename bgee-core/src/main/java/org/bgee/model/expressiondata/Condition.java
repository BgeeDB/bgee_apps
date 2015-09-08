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
 * @version Bgee 13 Sept. 2015
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
     * Default constructor not public, at least an anat. entity and a dev. stage must be provided, 
     * see {@link #Condition(AnatEntity, DevStage)}.
     */
    //Constructor not public on purpose, suppress warnings
    @SuppressWarnings("unused")
    private Condition() {
        this(null, null);
    }
    /**
     * Constructor providing the IDs of the anatomical entity and the developmental stage 
     * of this {@code Condition}.
     * 
     * @param anatEntityId  A {@code String} that is the ID of the anatomical entity 
     *                      used in this gene expression condition.
     * @param devStageId    A {@code String} that is the ID of the developmental stage  
     *                      used in this gene expression condition.
     * @throws IllegalArgumentException     if {@code anatEntity} or {@code devStage} is {@code null}. 
     */
    public Condition(String anatEntityId, String devStageId) {
        if (StringUtils.isBlank(anatEntityId)) {
            throw log.throwing(
                    new IllegalArgumentException("The provided anatomical entity ID cannot be blank"));
        }
        if (StringUtils.isBlank(devStageId)) {
            throw log.throwing(
                    new IllegalArgumentException("The provided developmental stage ID cannot be blank"));
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
}
