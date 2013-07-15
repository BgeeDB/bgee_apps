package org.bgee.model.expressiondata.querytools.filters;

/**
 * A <code>CompositeCallFilter</code> allows to use a <code>ExpressionCallFilter</code> 
 * and a <code>RawDataFilter</code> at the same time. It allows to filter 
 * based on expression data calls, as when using a <code>ExpressionCallFilter</code> 
 * (this is why it implements the interface <code>CallFilter</code>), but as if the calls 
 * had been computed from only a subset of the data in Bgee, filtered using 
 * the <code>RawDataFilter</code>. This leads to re-compute on-the-fly 
 * expression data calls summarizing expression data. As this is computationally intensive, 
 * <code>CompositeCallFilter</code>s can only be used for queries restrained 
 * to a <code>Gene</code>, or to a list of <code>Gene</code>s (as for instance, an 
 * {@link org.bgee.model.expressiondata.querytools.AnatDevExpressionQuery 
 * AnatDevExpressionQuery}). 
 * <p>
 * This class implements the methods from <code>CallFilter</code>, by delegating 
 * to the <code>ExpressionCallFilter</code> instance it holds. So, calling a method 
 * defined by the <code>CallFiler</code> interface, on an instance of this class, 
 * is equivalent to calling them on the <code>ExpressionCallFilter</code> instance it holds.
 * <p>
 * <h3>Explanations about the computations</h3>
 * Bgee summarizes expression data over several experiments or samples. For instance, 
 * if a sample shows expression of a gene with a high confidence in a condition, 
 * and another sample for the same condition shows absence of expression of that gene, 
 * Bgee will consider the gene as expressed in that condition, with a low quality score.
 * <p>
 * Because of this, removing some source raw data, or restraining results to 
 * some source raw data, will change the overall summarized data calls 
 * generated (in the previous example, removing the sample showing expression of the gene 
 * would lead to consider it as not expressed with a high confidence...). 
 * <p>
 * So basically, a <code>CompositeCallFilter</code> will work as a <code>ExpressionCallFilter</code>, 
 * but as if Bgee was containing only the source raw data filtered 
 * from the <code>RawDataFilter</code>.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public class CompositeCallFilter implements CallFilter {
	
    private BasicCallFilter callFilter;
    private RawDataFilter rawDataFilter;
}
