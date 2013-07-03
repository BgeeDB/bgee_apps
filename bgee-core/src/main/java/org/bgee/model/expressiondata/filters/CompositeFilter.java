package org.bgee.model.expressiondata.filters;

/**
 * 
 * 
 * <strong>Warning when using a <code>CompositeFilter</code></strong>: 
 * It might not be doing what you think it should do. Read below: 
 * <p>
 * Bgee summarizes expression data calls over several experiments or samples, 
 * and notably, reconciles inconsistencies. For instance, if a sample shows 
 * expression of a gene, and another sample for the same condition shows absence 
 * of expression of that gene, Bgee will consider the gene as expressed in 
 * that condition, with a low quality score.
 * <p>
 * Because of this, removing some source raw data, or restraining results to 
 * some source raw data, could change the overall summarized data calls 
 * generated (in the previous example, removing the sample showing expression of the gene 
 * would lead to consider it as not expressed with a high confidence...). 
 * <p>
 * It is <strong>not</strong> what this <code>CompositeFilter</code> does. 
 * While it is possible to exclude expression data calls involving some specific 
 * source raw data, or to consider only expression data calls involving some 
 * specific source raw data, it is not possible to recompute the expression data calls 
 * as if some source raw data were not in the database. 
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public class CompositeFilter extends ExprDataFilter {

}
