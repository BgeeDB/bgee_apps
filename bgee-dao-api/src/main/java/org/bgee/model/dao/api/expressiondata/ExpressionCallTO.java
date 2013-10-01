package org.bgee.model.dao.api.expressiondata;

/**
 * A {@code CallTO} specific to expression calls. Their specificity is that 
 * they can be produced using data propagation from child anatomical entities 
 * by <em>is_a</em> or <em>part_of</em> relations, and/or from child developmental 
 * stages by <em>is_a</em> or <em>part_of</em> relations. See {@link 
 * #isIncludeSubstructures()} and {@link #isIncludeSubStages()} for more details.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public final class ExpressionCallTO extends CallTO {
    /**
     * A {@code boolean} defining whether this expression call was generated 
     * using data from the anatomical entity with the ID {@link CallTO#getAnatEntityId()} 
     * alone, or by also considering all its descendants by <em>is_a</em> or 
     * <em>part_of</em> relations, even indirect. If {@code true}, all its descendants 
     * were considered. 
     */
    private boolean includeSubstructures;
    /**
     * A {@code boolean} defining whether this expression call was generated 
     * using data from the developmental stage with the ID {@link CallTO#getDevStageId()} 
     * alone, or by also considering all its descendants by <em>is_a</em> or 
     * <em>part_of</em> relations, even indirect. If {@code true}, all its descendants 
     * were considered.
     */
    private boolean includeSubStages;
    
    /**
     * Default constructor.
     */
    ExpressionCallTO() {
        super();
        this.setIncludeSubstructures(false);
        this.setIncludeSubStages(false);
    }

    /**
     * Returns the {@code boolean} defining whether this expression call was generated 
     * using data from the anatomical entity with the ID {@link CallTO#getAnatEntityId()} 
     * alone, or by also considering all its descendants by <em>is_a</em> or 
     * <em>part_of</em> relations, even indirect. If {@code true}, all its descendants 
     * were considered.
     * 
     * @return  If {@code true}, all descendants of the anatomical entity were considered. 
     */
    public boolean isIncludeSubstructures() {
        return includeSubstructures;
    }
    /**
     * Sets the {@code boolean} defining whether this expression call was generated 
     * using data from the anatomical entity with the ID {@link CallTO#getAnatEntityId()} 
     * alone, or by also considering all its descendants by <em>is_a</em> or 
     * <em>part_of</em> relations, even indirect. If {@code true}, all its descendants 
     * were considered.
     * 
     * @param includeSubstructures  A {@code boolean} defining whether descendants 
     *                              of the anatomical entity were considered.
     */
    void setIncludeSubstructures(boolean includeSubstructures) {
        this.includeSubstructures = includeSubstructures;
    }

    /**
     * Returns the {@code boolean} defining whether this expression call was generated 
     * using data from the developmental stage with the ID {@link CallTO#getDevStageId()} 
     * alone, or by also considering all its descendants by <em>is_a</em> or 
     * <em>part_of</em> relations, even indirect. If {@code true}, all its descendants 
     * were considered.
     * 
     * @return  If {@code true}, all descendants of the developmental stage 
     *          were considered. 
     */
    public boolean isIncludeSubStages() {
        return includeSubStages;
    }
    /**
     * Sets the {@code boolean} defining whether this expression call was generated 
     * using data from the developmental stage with the ID {@link CallTO#getDevStageId()} 
     * alone, or by also considering all its descendants by <em>is_a</em> or 
     * <em>part_of</em> relations, even indirect. If {@code true}, all its descendants 
     * were considered.
     * 
     * @param includeSubstructures  A {@code boolean} defining whether descendants 
     *                              of the developmental stage were considered.
     */
    void setIncludeSubStages(boolean includeSubStages) {
        this.includeSubStages = includeSubStages;
    }
}
