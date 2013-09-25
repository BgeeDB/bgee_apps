/**
 * This package contains the classes related to Affymetrix data. 
 * <ul>
 * <li>The class extending {@link org.bgee.model.expressiondata.rawdata.Experiment 
 * Experiment} is {@link AffymetrixExp}.
 * <li>The class extending {@link org.bgee.model.expressiondata.rawdata.SampleAssay 
 * SampleAssay} is {@link AffymetrixChip}. 
 * <li>The class extending {@link org.bgee.model.expressiondata.rawdata.CallSource 
 * CallSource} is {@link AffymetrixProbeset}. 
 * <p>
 * As of Bgee 13, Affymetrix data are used to generate calls {@code EXPRESSION}, 
 * {@code NOEXPRESSION}, {@code OVEREXPRESSION}, {@code UNDEREXPRESSION}, 
 * {@code NODIFFEXPRESSION}.
 * <p>
 * As Affymetrix data can be used for differential expression analyses, classes from this package 
 * are used in the package {@link org.bgee.model.expressiondata.rawdata.diffexpression.affymetrix}
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
package org.bgee.model.expressiondata.rawdata.affymetrix;