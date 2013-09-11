/**
 * This package contains the classes related to <em>in situ</em> hybridization data. 
 * <ul>
 * <li>The class extending {@link org.bgee.model.expressiondata.rawdata.Experiment 
 * Experiment} is {@link InSituExp}.
 * <li>The class extending {@link org.bgee.model.expressiondata.rawdata.SampleAssay 
 * SampleAssay} is {@link InSituEvidence}. 
 * <li>The class extending {@link org.bgee.model.expressiondata.rawdata.CallSource 
 * CallSource} is {@link InSituSpot}. 
 * <p>
 * As of Bgee 13, <em>in situ</em> data are used to generate calls <code>EXPRESSION</code>, 
 * <code>NOEXPRESSION</code>.
 * <p>
 * The {@link org.bgee.model.expressiondata.DataParameters.DataType} 
 * <code>RELAXEDINSITU</code> is a specific use of these <em>in situ</em> data.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
package org.bgee.model.expressiondata.rawdata.insitu;