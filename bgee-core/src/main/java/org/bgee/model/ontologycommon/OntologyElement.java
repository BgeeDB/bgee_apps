package org.bgee.model.ontologycommon;

/**
 * An interface for elements part of an {@link Ontology}. It is implemented by 
 * {@link BaseOntologyElement}. It allows to favor composition over inheritance: 
 * all classes that are part of an ontology can implement this interface, 
 * and delegate implementations to a <code>BaseOntologyElement</code> instance, 
 * rather than inheriting from it. This simplify the inheritance graph a lot.
 * <p>
 * It does not specify methods such as <code>getId</code>, as one could expect 
 * from an element belonging to an ontology. This is because, in Bgee, several 
 * <code>OntologyElement</code>s can sometimes be merged into a single one 
 * (see for instance {@link org.bgee.model.anatdev.evogrouping.AnatDevEvoGroup}). 
 * These merged elements would then not correspond to only one ID, but several 
 * (see for instance {@link #registerWithId(String)}). Of note, this is why this interface 
 * is called an <code>OntologyElement</code>, rather than an <code>OntologyEntity</code>, 
 * to not refer to the class {@link org.bgee.model.Entity}, which provides methods 
 * such as <code>getId</code>.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public interface OntologyElement {
	/**
	 * Register this <code>OntologyElement</code> to its <code>Ontology</code> 
	 * (see {@link #getOntology()}) with the provided <code>id</code>. 
	 * Attempts to locate an <code>OntologyElement</code> based on this ID 
	 * in the <code>Ontology</code> should then return this instance. 
	 * A same <code>OntologyElement</code> can be registered with several IDs, 
	 * which is useful when merging several <code>OntologyElement</code>s into one 
	 * (see for instance {@link org.bgee.model.anatdev.evogrouping.AnatDevEvoGroup}).
	 * 
	 * @param id	A <code>String</code> to use to register this <code>OntologyElement</code>
	 * 				to its <code>Ontology</code>.
	 */
    public void registerWithId(String id);
}
