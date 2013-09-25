package org.bgee.model.expressiondata.querytools.filters;

/**
 * A {@link Filter} to filter or retrieve expression data calls. 
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public interface CallFilter extends Filter {
	/**
	 * Tries to merge this {@code CallFilter} with {@code filterToMerge}, 
	 * by considering that they both define conditions on expression data 
	 * for a same {@code Entity} (for instance, a same {@code Gene}).
	 * <p>
	 * For instance, if this {@code CallFilter} defines a minimum quality 
	 * threshold for a given data type, and if {@code filterToMerge} defines  
	 * for the same data type the quality threshold just below, then it is possible  
	 * to keep only the lowest threshold and merge the two {@code CallFilter}s. 
	 * As they are related to the same {@code Entity}, we would retrieve the exact 
	 * same data whether we use the two {@code CallFilter}s, or the merged one. 
	 * Note that this is only an example, and that each {@code CallFiler} implementation 
	 * defines its own rules for merging. 
	 * <p>
	 * This method returns {@code null} if the {@code CallFilter}s could not 
	 * be merged. Otherwise, it returns the newly merged {@code CallFilter}. 
	 * This method is reflexive, so that it is equivalent to call this method 
	 * on this {@code CallFilter} with {@code filterToMerge} as parameter, 
	 * or on {@code filterToMerge} with this {@code CallFilter} as parameter. 
	 *  
	 * @param filterToMerge	The {@code CallFilter} to be merged with this one.
	 * @return				The newly instantiated merged {@code CallFilter}, 
	 * 						or {@code null} if no merging could be done. 
	 * @see #mergeDiffEntitiesCallFilter(CallFilter)
	 */
    public CallFilter mergeSameEntityCallFilter(CallFilter filterToMerge);
    /**
     * Tries to merge this {@code CallFilter} with {@code filterToMerge}, 
     * by considering that they define conditions on expression data for different 
     * {@code Entity}s (for instance, different {@code Gene}s).
     * <p>
     * As for {@link #mergeSameEntityCallFilter(CallFilter)}, the principle is that 
     * it should be possible to retrieve the exact same data whether we use 
     * the two {@code CallFilter}s, or the merged one. But as they are 
     * related to different {@code Entity}s, the conditions to accept 
     * to perform the merging are much more stringent. 
     * <p>
     * For instance, if two {@code CallFilter}s were requesting expression 
     * for a same {@code Gene}, but accepting different {@code DataType}s 
     * as parameters, they could be merged: it is equivalent to request expression 
     * of a {@code Gene} by one {@code DataType} in a {@code CallFilter}, 
     * and by another {@code DataType} in another {@code CallFilter}, 
     * to requesting both {@code DataType}s in one {@code CallFilter}. 
     * But if these two {@code CallFilter}s were related to different 
     * {@code Gene}s, then it would not be equivalent to request expression 
     * of a {@code Gene} by one {@code DataType}, and expression of another 
     * {@code Gene} by another {@code DataType}, to requesting expression 
     * defined by both {@code DataType}s for both {@code Gene}s.
     * <p>
     * Note that this is only an example, and that each {@code CallFiler} 
     * implementation defines its own rules for merging. 
     * <p>
     * This method returns {@code null} if the {@code CallFilter}s could not 
     * be merged. Otherwise, it returns the newly merged {@code CallFilter}. 
     * This method is reflexive, so that it is equivalent to call this method 
     * on this {@code CallFilter} with {@code filterToMerge} as parameter, 
     * or on {@code filterToMerge} with this {@code CallFilter} as parameter. 
     *  
     * @param filterToMerge   The {@code CallFilter} to be merged with this one.
     * @return              The newly instantiated merged {@code CallFilter}, 
     *                      or {@code null} if no merging could be done. 
     */
    public CallFilter mergeDiffEntitiesCallFilter(CallFilter filterToMerge);
}
