package org.bgee.model.expressiondata.querytools.filters;

/**
 * Parent abstract class of all filters, allowing to parameterized 
 * any expression data query: {@link BasicCallFilter}, {@link AdvancedDiffCallFilter}, 
 * {@link RawDataFilter}, and {@link CompositeFilter}. Having a common parent class 
 * for all these filters allows a greater modularity and simplicity when using 
 * an expression data query tool. it is an abstract class rather than an interface, 
 * because all filters also have common attributes or functionalities, 
 * such as listing all available data types. 
 * <p>
 * Users will likely use the <code>BasicCallFilter</code> most of the time. It allows 
 * to filter data based on expression data calls (<code>EXPRESSION</code>, 
 * <code>OVEREXPRESSION</code>, <code>NOEXPRESSION</code>, ...), based on data qualities, 
 * data types, etc.
 * <p>
 * The <code>AdvancedDiffCallFilter</code> is a subclass of <code>BasicCallFilter</code>, 
 * only applicable to <code>OVEREXPRESSION</code> and <code>UNDEREXPRESSION</code> 
 * data calls, and offering additional filtering functionalities specific to these data.
 * <p>
 * The <code>RawDataFilter</code> allows to filter source raw data, for instance 
 * to retrieve only results from specific experiments, or specific probesets, etc.
 * <p>
 * Finally, the <code>CompositeFilter</code> allows to use both 
 * a <code>BasicCallFilter</code> (or an <code>AdvancedDiffCallFilter</code>) and 
 * a <code>RawDataFilter</code> at the same time. 
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public abstract class ExprDataFilter {

}
