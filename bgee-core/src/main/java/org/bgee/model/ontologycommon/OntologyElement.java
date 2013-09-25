package org.bgee.model.ontologycommon;

/**
 * An interface for elements part of an {@link Ontology}. It is implemented by 
 * {@link BaseOntologyElement}. It allows to favor composition over inheritance: 
 * all classes that are part of an ontology can implement this interface, 
 * and delegate implementations to a {@code BaseOntologyElement} instance, 
 * rather than inheriting from it. This simplify the inheritance graph a lot.
 * <p>
 * It does not specify methods such as {@code getId}, as one could expect 
 * from an element belonging to an ontology. This is because, in Bgee, several 
 * {@code OntologyElement}s can sometimes be merged into a single one 
 * (see for instance {@link org.bgee.model.anatdev.evomapping.AnatDevMapping}). 
 * These merged elements would then not correspond to only one ID, but several 
 * (see for instance {@link #registerWithId(String)}). Of note, this is why this interface 
 * is called an {@code OntologyElement}, rather than an {@code OntologyEntity}, 
 * to not refer to the class {@link org.bgee.model.Entity}, which provides methods 
 * such as {@code getId}.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public interface OntologyElement {
	/**
	 * Register this {@code OntologyElement} to its {@code Ontology} 
	 * (see {@link #getOntology()}) with the provided {@code id}. 
	 * Attempts to locate an {@code OntologyElement} based on this ID 
	 * in the {@code Ontology} should then return this instance. 
	 * A same {@code OntologyElement} can be registered with several IDs, 
	 * which is useful when merging several {@code OntologyElement}s into one 
	 * (see for instance {@link org.bgee.model.anatdev.evomapping.AnatDevMapping}).
	 * 
	 * @param id	A {@code String} to use to register this {@code OntologyElement}
	 * 				to its {@code Ontology}.
	 */
    public void registerWithId(String id);
}
