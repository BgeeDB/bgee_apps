/**
 * Core layer (also known as "business layer") of the Bgee application. 
 * <h3>Entities</h3>
 * This layer provides classes describing "entities" used in Bgee (for instance, 
 * <code>Gene</code>, or <code>Species</code>), and for each of these entity class, 
 * a <code>Factory</code> to obtain objects of this class (for instance, 
 * a <code>GeneFactory</code>, a <code>SpeciesFactory</code>). 
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