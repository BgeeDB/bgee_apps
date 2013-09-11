/**
 * This package contains the classes related to RNA-Seq data. 
 * <ul>
 * <li>The class extending {@link org.bgee.model.expressiondata.rawdata.Experiment 
 * Experiment} is {@link RNASeqExp}.
 * <li>The class extending {@link org.bgee.model.expressiondata.rawdata.SampleAssay 
 * SampleAssay} is {@link RNASeqLibrary}. 
 * <li>The class extending {@link org.bgee.model.expressiondata.rawdata.CallSource 
 * CallSource} is {@link RNASeqResult}. 
 * <p>
 * As of Bgee 13, RNA-Seq data are used to generate calls <code>EXPRESSION</code>, 
 * <code>NOEXPRESSION</code>, <code>OVEREXPRESSION</code>, <code>UNDEREXPRESSION</code>, 
 * <code>NODIFFEXPRESSION</code>.
 * <p>
 * As RNA-Seq data can be used for differential expression analyses, classes from this package 
 * are used in the package {@link org.bgee.model.expressiondata.rawdata.diffexpression.rnaseq}
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
package org.bgee.model.expressiondata.rawdata.rnaseq;