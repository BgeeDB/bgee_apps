/**
 * This package provides classes allowing to filter results of 
 * any expression data query tool. All filters implement the {@link Filter} interface.
 * There are then two types of {@code Filter}s: {@link RawDataFilter} and 
 * {@link CallFilter}. {@code CallFilter}s include {@link BasicCallFilter} 
 * and {@link CompositeFilter}. {@code BasicCallFilter}s are then provided 
 * with different implementations depending on the possibilities 
 * offered by the {@code CallType} used: {@link ExpressionCallFilter}, 
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
 *     by the given {@code CallType}.
 *       <ul>
 *       <li>{@link ExpressionCallFilter}: filter for {@code EXPRESSION} calls.
 *       <li>{@link NoExpressionCallFilter}: filter for {@code NOEXPRESSION} calls.
 *       <li>{@link DiffExpressionCallFilter}: filter for differential expression calls 
 *       ({@code OVEREXPRESSION}, {@code UNDEREXPRESSION}, 
 *       {@code NODIFFEXPRESSION}).
 *       </ul>
 *     <li>{@link CompositeCallFilter}: a filter using at the same time 
 *     a {@code BasicCallFiler} and a {@code RawDataFilter}, allowing 
 *     to recompute on the fly expression data calls by filtering source raw data.
 *     </ul>
 *   </ul>
 * </ul>
 * <p>
 * Users will likely use a {@code BasicCallFilter} or a {@code RawDataFilter} 
 * most of the time.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
package org.bgee.model.expressiondata.querytools.filters;