/**
 * Core layer (also known as "business layer") of the Bgee application. 
 * <h3>Entities</h3>
 * This layer provides classes decribing "entities" used in Bgee (for instance, 
 * <code>Gene</code>, <code>Species</code>, etc), and for each of these entity class, 
 * a <code>Factory</code> to obtain objects of this class (for instance, 
 * a <code>GeneLoader</code>, a <code>SpeciesLoader</code>). 
 * <p>
 * Factories to instantiate "entities" are obtained from the class 
 * {@link org.bgee.model.EntityFactoryProvider}. This is because some factories provide two implementations, 
 * that are hidden to the user by the <code>EntityFactoryProvider</code>. 
 * See its javadoc for more details. 
 * 
 * <h3>Data access layer</h3>
 * Beside the several packages of this core layer, organizing the "entity" classes, 
 * a layer is specifically used to access data sources: 
 * the {@link org.bgee.model.data} package. This package is completely independent 
 * from other packages (it never uses classes from other packages). 
 * It has the same packages organization than the "entity" packages.
 * See its javadoc for more details.
 * 
 * @author Frederic Bastian
 * @version Bgee 13, Mar 2013
 * @since Bgee 01
 */
package org.bgee.model;