package org.bgee.model.expressiondata.querytools.filters;

/**
 * A {@link Filter} based on expression data calls. 
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public interface CallFilter extends Filter {
	/**
	 * Tries to merge this <code>CallFilter</code> with <code>callToMerge</code>, 
	 * by considering that they both define conditions on expression data 
	 * for a same <code>Gene</code>.
	 * <p>
	 * For instance, if this <code>CallFilter</code> defines a minimum quality 
	 * threshold for a given data type, and if <code>callToMerge</code> defines  
	 * for the same data type the quality threshold just below, then we can only keep 
	 * the lowest threshold and merge the two <code>CallFilter</code>s. 
	 * As they are related to the same <code>Gene</code>, we would retrieve the exact 
	 * same data whether we use the two <code>CallFilter</code>s, or the merged one. 
	 * Note that this is only an example, and that each <code>CallFiler</code> implementation 
	 * defines its own rules for merging. 
	 * <p>
	 * This method returns <code>null</code> if the <code>CallFilter</code>s could not 
	 * be merged. Otherwise, it returns the newly merged <code>CallFilter</code>. 
	 * This method is reflexive, so that it is equivalent to call this method 
	 * on this <code>CallFilter</code> with <code>callToMerge</code> as parameter, 
	 * or on <code>callToMerge</code> with this <code>CallFilter</code> as parameter. 
	 *  
	 * @param callToMerge	The <code>CallFilter</code> to be merged with this one.
	 * @return				The newly instantiated merged <code>CallFilter</code>, 
	 * 						or <code>null</code> if no merging could be done. 
	 */
    public CallFilter mergeSameGeneCallFilter(CallFilter callToMerge);
    public CallFilter mergeDifferentGeneCallFilter(CallFilter callToMerge);
}
