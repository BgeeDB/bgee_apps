package org.bgee.model.expressiondata.querytools.filters;

/**
 * Interface implemented by all filters, allowing to parameterized 
 * an expression data query (see classes allowing to perform queries 
 * in package {@link org.bgee.model.expressiondata.querytools querytools}). 
 * <p>
 * Users will likely use the <code>ExpressionCallFilter</code> implementation most of the time. 
 * It allows to filter data based on expression data calls (<code>EXPRESSION</code>, 
 * <code>OVEREXPRESSION</code>, <code>NOEXPRESSION</code>, ...), based on data qualities, 
 * data types, etc.
 * <p>
 * The <code>AdvancedDiffCallFilter</code> is a subclass of <code>ExpressionCallFilter</code>, 
 * only applicable to <code>OVEREXPRESSION</code> and <code>UNDEREXPRESSION</code> 
 * data calls, and offering additional filtering functionalities specific to these kind of data.
 * <p>
 * The <code>RawDataFilter</code> allows to filter source raw data, for instance 
 * to retrieve only results from specific experiments, or specific probesets, etc.
 * <p>
 * Finally, the <code>CompositeCallFilter</code> allows to use both 
 * a <code>ExpressionCallFilter</code> (or an <code>AdvancedDiffCallFilter</code>) and 
 * a <code>RawDataFilter</code> at the same time. 
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public interface Filter {

}
