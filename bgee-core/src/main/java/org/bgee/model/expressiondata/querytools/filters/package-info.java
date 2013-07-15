/**
 * This package provides classes allowing to filter results of 
 * any expression data query tool. Filters available are: 
 * <ul>
 * <li>{@link ExpressionCallFilter}, the filter that users will likely use most of the time. 
 * It allows to filter data based on expression data calls (<code>EXPRESSION</code>, 
 * <code>OVEREXPRESSION</code>, <code>NOEXPRESSION</code>, ...), based on data qualities, 
 * data types, etc.
 * <li>{@link AdvancedDiffCallFilter}, a subclass of <code>ExpressionCallFilter</code> 
 * providing additional filtering functionalities specific to <code>OVEREXPRESSION</code> 
 * and <code>UNDEREXPRESSION</code> data calls. 
 * <li>{@link RawDataFilter} allowing to filter based on source raw data.
 * <li>{@link CompositeCallFilter} allowing to use both a <code>ExpressionCallFilter</code> 
 * (or an <code>AdvancedDiffCallFilter</code>) and a <code>RawDataFilter</code> 
 * at the same time.
 * </ul>
 * <p>
 * All these filters extend the {@link Filter} abstract class, which allows 
 * to use any of them indifferently when performing queries. <code>Filter</code> 
 * is an abstract class rather than an interface, because all filters also have 
 * common attributes or functionalities, such as listing all available data types. 
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
package org.bgee.model.expressiondata.querytools.filters;