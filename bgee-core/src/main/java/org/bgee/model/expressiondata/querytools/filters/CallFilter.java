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
	 * Tries to merge this <code>CallFilter</code> with <code>filterToMerge</code>, 
	 * by considering that they both define conditions on expression data 
	 * for a same <code>Entity</code> (for instance, a same <code>Gene</code>).
	 * <p>
	 * For instance, if this <code>CallFilter</code> defines a minimum quality 
	 * threshold for a given data type, and if <code>filterToMerge</code> defines  
	 * for the same data type the quality threshold just below, then it is possible  
	 * to keep only the lowest threshold and merge the two <code>CallFilter</code>s. 
	 * As they are related to the same <code>Entity</code>, we would retrieve the exact 
	 * same data whether we use the two <code>CallFilter</code>s, or the merged one. 
	 * Note that this is only an example, and that each <code>CallFiler</code> implementation 
	 * defines its own rules for merging. 
	 * <p>
	 * This method returns <code>null</code> if the <code>CallFilter</code>s could not 
	 * be merged. Otherwise, it returns the newly merged <code>CallFilter</code>. 
	 * This method is reflexive, so that it is equivalent to call this method 
	 * on this <code>CallFilter</code> with <code>filterToMerge</code> as parameter, 
	 * or on <code>filterToMerge</code> with this <code>CallFilter</code> as parameter. 
	 *  
	 * @param filterToMerge	The <code>CallFilter</code> to be merged with this one.
	 * @return				The newly instantiated merged <code>CallFilter</code>, 
	 * 						or <code>null</code> if no merging could be done. 
	 * @see #mergeDiffEntitiesCallFilter(CallFilter)
	 */
    public CallFilter mergeSameEntityCallFilter(CallFilter filterToMerge);
    /**
     * Tries to merge this <code>CallFilter</code> with <code>filterToMerge</code>, 
     * by considering that they define conditions on expression data for different 
     * <code>Entity</code>s (for instance, different <code>Gene</code>s).
     * <p>
     * As for {@link #mergeSameEntityCallFilter(CallFilter)}, the principle is that 
     * it should be possible to retrieve the exact same data whether we use 
     * the two <code>CallFilter</code>s, or the merged one. But as they are 
     * related to different <code>Entity</code>s, the conditions to accept 
     * to perform the merging are much more stringent. 
     * <p>
     * For instance, if two <code>CallFilter</code>s were requesting expression 
     * for a same <code>Gene</code>, but accepting different <code>DataType</code>s 
     * as parameters, they could be merged: it is equivalent to request expression 
     * of a <code>Gene</code> by one <code>DataType</code> in a <code>CallFilter</code>, 
     * and by another <code>DataType</code> in another <code>CallFilter</code>, 
     * to requesting both <code>DataType</code>s in one <code>CallFilter</code>. 
     * But if these two <code>CallFilter</code>s were related to different 
     * <code>Gene</code>s, then it would not be equivalent to request expression 
     * of a <code>Gene</code> by one <code>DataType</code>, and expression of another 
     * <code>Gene</code> by another <code>DataType</code>, to requesting expression 
     * defined by both <code>DataType</code>s for both <code>Gene</code>s.
     * <p>
     * Note that this is only an example, and that each <code>CallFiler</code> 
     * implementation defines its own rules for merging. 
     * <p>
     * This method returns <code>null</code> if the <code>CallFilter</code>s could not 
     * be merged. Otherwise, it returns the newly merged <code>CallFilter</code>. 
     * This method is reflexive, so that it is equivalent to call this method 
     * on this <code>CallFilter</code> with <code>filterToMerge</code> as parameter, 
     * or on <code>filterToMerge</code> with this <code>CallFilter</code> as parameter. 
     *  
     * @param filterToMerge   The <code>CallFilter</code> to be merged with this one.
     * @return              The newly instantiated merged <code>CallFilter</code>, 
     *                      or <code>null</code> if no merging could be done. 
     */
    public CallFilter mergeDiffEntitiesCallFilter(CallFilter filterToMerge);
}
