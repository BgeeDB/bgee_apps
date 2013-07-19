/**
 * This package contains the classes relates to anatomical entities and ontologies, 
 * and developmental stages and ontologies.
 * <p>
 * {@link Stage} and {@link AnatomicalEntity} both inherit from {@link AnatDevEntity}. 
 * {@link StageOntology} and {@link AnatomicalOntology} both inherit from 
 * {@link AnatDevOntology}. 
 * <p>
 * Then, factories are provided for {@link Stage}s ({@link StageFactory} and 
 * {@link StageStaticFactory}), for {@link StageOntology}s ({@link StageOntologyFactory} and 
 * {@link StageOntologyStaticFactory}), for {@link AnatomicalEntity}s 
 * ({@link AnatEntityFactory} and {@link AnatEntityStaticFactory}), and for 
 * {@link AnatomicalOntology}s ({@link AnatOntologyFactory} and 
 * {@link AnatOntologyStaticFactory}). 
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 01
 */
package org.bgee.model.anatdev;