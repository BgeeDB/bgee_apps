package org.bgee.model.expressiondata.rawdata;

/**
 * Interface implemented by all classes that represent expression data annotated 
 * by the Bgee curators to anatomy and development. These classes should not 
 * implement this interface themselves, but rather, should delegate implementation 
 * to the class {@link Annotation} (pattern "composition over inheritance").
 * <p>
 * This interface is needed because: 
 * i) classes with expression data annotated have nothing in common and cannot 
 * inherit from a common superclass; and ii), because these classes will use 
 * composition over inheritance, by delegating methods from this interface to 
 * the class {@link Annotation}; it is not actually needed for all these classes 
 * to be of the same type (they could just delegate to <code>Annotation</code>, 
 * nothing more), but this is to ensure that any modifications in the future 
 * to the annotation process (for instance, annotating experimental conditions 
 * as well) will be transmitted to classes representing data annotated. 
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public interface DataAnnotated {

}
