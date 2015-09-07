package org.bgee.model.expressiondata;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.anatdev.DevStage;

/**
 * This class describes the conditions related to gene expression. It notably captures 
 * the anatomical localization and the developmental stage of gene expression conditions. 
 * It could be easily extended to also manage other parameters, such as the sex of a sample, 
 * the strain, or other experimental conditions (gene knock-out, drug treatment, etc).
 * 
 * @author Frederic Bastian
 * @version Bgee 13.1
 * @since Bgee 13.1
 */
//XXX: should this class also be able of storing IDs rather than objects? 
//(for use in, e.g., SummaryCalls). 
//Or only IDs and no objects?
//XXX: how to manage multi-species conditions? Should we have a class SingleSpeciesCondition 
//and a class MultiSpeciesCondition? Or, only a Condition, using a "SingleSpeciesAnatEntity" 
//or a "MultiSpeciesAnatEntity", etc?
public class Condition {
    private final static Logger log = LogManager.getLogger(Condition.class.getName());
    /**
     * An {@code AnatEntity} representing the anatomical entity used in this gene expression condition.
     */
    private final AnatEntity anatEntity;
    /**
     * A {@code DevStage} representing the developmental stage used in this gene expression condition.
     */
    private final DevStage devStage;
    
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
     * Constructor providing the anatomical entity and developmental stage 
     * of this {@code Condition}.
     * 
     * @param anatEntity    An {@code AnatEntity} used in this gene expression condition.
     * @param devStage      A {@code DevStage} used in this gene expression condition.
     * @throws IllegalArgumentException     if {@code anatEntity} or {@code devStage} is {@code null}. 
     */
    public Condition(AnatEntity anatEntity, DevStage devStage) {
        if (anatEntity == null) {
            throw log.throwing(
                    new IllegalArgumentException("The provided anatomical entity cannot be null"));
        }
        if (devStage == null) {
            throw log.throwing(
                    new IllegalArgumentException("The provided developmental stage cannot be null"));
        }
        this.anatEntity = anatEntity;
        this.devStage   = devStage;
    }
    
    /**
     * @return The {@code AnatEntity} used in this gene expression condition.
     */
    public AnatEntity getAnatEntity() {
        return anatEntity;
    }
    /**
     * @return The {@code DevStage} used in this gene expression condition.
     */
    public DevStage getDevStage() {
        return devStage;
    }
}
