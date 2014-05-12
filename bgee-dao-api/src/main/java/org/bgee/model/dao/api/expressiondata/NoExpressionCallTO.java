package org.bgee.model.dao.api.expressiondata;

/**
 * A {@code CallTO} specific to no-expression calls (explicit report of absence 
 * of expression). Their specificity is that they can be produced using data propagation 
 * from parent anatomical entities by <em>is_a</em> or <em>part_of</em> relations. 
 * See {@link #isIncludeParentStructures()} for more details.
 * <p>
 * Of note, there is no data propagation from developmental stages for no-expression 
 * calls.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public final class NoExpressionCallTO extends CallTO {
	private static final long serialVersionUID = 5793434647776540L;
	/**
     * A {@code boolean} defining whether this no-expression call was generated 
     * using the data from the anatomical entity with the ID {@link CallTO#getAnatEntityId()} 
     * alone, or by also considering all its parents by <em>is_a</em> or <em>part_of</em> 
     * relations, even indirect. If {@code true}, all its parents were considered. 
     * So for instance, if B is_a A, and absence of expression has been reported in A, 
     * then B could benefit from this information. In other words, when a gene 
     * is not expressed in a structure, it is expressed nowhere in that structure.
     */
    private boolean includeParentStructures;
    
    /**
     * Default constructor.
     */
    NoExpressionCallTO() {
        super();
        this.setIncludeParentStructures(false);
    }

    /**
     * Returns the {@code boolean} defining whether this no-expression call was generated 
     * using the data from the anatomical entity with the ID {@link CallTO#getAnatEntityId()} 
     * alone, or by also considering all its parents by <em>is_a</em> or <em>part_of</em> 
     * relations, even indirect. If {@code true}, all its parents were considered. 
     * So for instance, if B is_a A, and absence of expression has been reported in A, 
     * then B could benefit from this information. In other words, when a gene 
     * is not expressed in a structure, it is expressed nowhere in that structure.
     * 
     * @return  If {@code true}, all parents of the anatomical entity were considered.
     */
    public boolean isIncludeParentStructures() {
        return includeParentStructures;
    }
    /**
     * Sets the {@code boolean} defining whether this no-expression call was generated 
     * using the data from the anatomical entity with the ID {@link CallTO#getAnatEntityId()} 
     * alone, or by also considering all its parents by <em>is_a</em> or <em>part_of</em> 
     * relations, even indirect. If {@code true}, all its parents were considered. 
     * So for instance, if B is_a A, and absence of expression has been reported in A, 
     * then B could benefit from this information. In other words, when a gene 
     * is not expressed in a structure, it is expressed nowhere in that structure.
     * 
     * @param includeParentStructures   A {@code boolean} defining whether parents 
     *                                  of the anatomical entity were considered.
     */
    void setIncludeParentStructures(boolean includeParentStructures) {
        this.includeParentStructures = includeParentStructures;
    }

   
}