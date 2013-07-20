package org.bgee.model.expressiondata.querytools.filters;

/**
 * Interface implemented by all filters, allowing to parameterized 
 * an expression data query (see classes allowing to perform queries 
 * in package {@link org.bgee.model.expressiondata.querytools querytools}). 
 * <p>
 * There are then two types of <code>Filter</code>s: {@link RawDataFilter} and 
 * {@link CallFilter}. <code>CallFilter</code>s include {@link BasicCallFilter} 
 * and {@link CompositeFilter}. <code>BasicCallFilter</code>s are then provided 
 * with different implementations depending on the possibilities 
 * offered by the <code>CallType</code> used: {@link ExpressionCallFilter}, 
 * {@link NoExpressionCallFilter}, and {@link DiffExpressionCallFilter}.
 * <p>
 * Overall summary: 
 * <ul>
 * <li>{@link Filter}: any filter.
 *   <ul>
 *   <li>{@link RawDataFilter}: filtering of source raw data.
 *   <li>{@link CallFilter}: filter based on expression data calls.
 *     <ul>
 *     <li>{@link BasicCallFiler}: simple filter based on expression data calls. 
 *     There are different implementations depending on the possibilities offered 
 *     by the given <code>CallType</code>.
 *       <ul>
 *       <li>{@link ExpressionCallFilter}: filter for <code>EXPRESSION</code> calls.
 *       <li>{@link NoExpressionCallFilter}: filter for <code>NOEXPRESSION</code> calls.
 *       <li>{@link DiffExpressionCallFilter}: filter for differential expression calls 
 *       (<code>OVEREXPRESSION</code>, <code>UNDEREXPRESSION</code>, 
 *       <code>NODIFFEXPRESSION</code>).
 *       </ul>
 *     <li>{@link CompositeCallFilter}: a filter using at the same time 
 *     a <code>BasicCallFiler</code> and a <code>RawDataFilter</code>, allowing 
 *     to recompute on the fly expression data calls by filtering source raw data.
 *     </ul>
 *   </ul>
 * </ul>
 * <p>
 * Users will likely use a <code>BasicCallFilter</code> or a <code>RawDataFilter</code> 
 * most of the time.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public interface Filter {

}
