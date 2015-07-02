package org.bgee.model.ontologycommon;

/**
 * Class implementing the {@link OntologyElement} interface. It is used to favor 
 * composition over inheritance: all classes that are part of an ontology 
 * can implement the interface, and delegate implementations to an instance of this class, 
 * rather than inheriting from it. This simplifies the inheritance graph a lot.
 * 
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public class BaseOntologyElement implements OntologyElement {

    @Override
    public void registerWithId(String id) {
        // TODO Auto-generated method stub
        
    }

}
