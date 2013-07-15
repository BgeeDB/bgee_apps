package org.bgee.model.expressiondata.querytools.filters;

/**
 * This class allows to perform filtering of query results based on 
 * source raw data identifiers, such as RNA-Seq experiment IDs, or Affymetrix chip IDs, 
 * etc. This <code>Filter</code> can either be used alone, completely disconnected 
 * from the expression data calls generated (for instance to retrieve the 
 * <code>Gene</code>s present on a specific <code>AffymetrixChip</code>, 
 * or the <code>AnatomicalEntity</code>s studied in a specific <code>RNASeqExp</code>); 
 * or it can be used as part of a {@link CompositeCallFilter}, 
 * allowing to re-compute expression data calls using only a subset of 
 * the source raw data in Bgee, filtered thanks to a <code>RawDataFilter</code>. 
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public class RawDataFilter implements Filter {
	
}
