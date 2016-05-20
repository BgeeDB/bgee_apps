package org.bgee.model.species;

import org.bgee.model.NamedEntity;
import org.bgee.model.ontology.OntologyElement;

//TODO: javadoc
public class Taxon extends NamedEntity implements OntologyElement<Taxon> {

    
    
    protected Taxon(String id, String name, String description) {
        super(id, name, description);
    }

    
    
}
