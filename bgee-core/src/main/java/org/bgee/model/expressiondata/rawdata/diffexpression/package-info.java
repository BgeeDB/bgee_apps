/**
 * This package is specific to data used to generate differential expression calls. 
 * This is because they used a different logic than for expression/no expression data. 
 * A {@link DiffExpressionAnalysis} compare different conditions; each condition 
 * (for instance, an organ at a given developmental stage) is represented 
 * by a group of several {@link org.bgee.model.expressiondata.rawdata.SampleAssay}s,  
 * studying this condition (see {@link SampleAssayGroup}). Equivalent 
 * {@link org.bgee.model.expressiondata.rawdata.CallSource}s in a same 
 * <code>SampleAssayGroup</code> are grouped as a {@link CallSourceGroup} 
 * (for instance, a same Affymetrix probeset on several chips of a same type, 
 * studying a given condition; or a RNA-Seq result of RPKM value for a same gene, 
 * in different libraries studying a given condition). 
 * <p>
 * A <code>DiffExpressionAnalysis</code> then consists in the comparison of equivalent 
 * <code>CallSourceGroup</code>s between its different <code>SampleAssayGroup</code>s, 
 * in order to generate fold changes, and p-values of likeliness of differential expression 
 * of a gene. For instance, a multiple comparison to the mean of same probesets, 
 * on several chips of the same type, studying different conditions, with replicates 
 * for each condition. Or, a multiple comparison to the mean of RPKM values 
 * for same genes, from several RNA-Seq libraries, studying different conditions, 
 * with library replicates for each condition.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
package org.bgee.model.expressiondata.rawdata.diffexpression;