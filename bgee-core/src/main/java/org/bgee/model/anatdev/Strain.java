package org.bgee.model.anatdev;

import org.bgee.model.NamedEntity;
import org.bgee.model.ontology.OntologyElement;

/**
 * Class describing strains.
 * 
 * @author Julien Wollbrett
 * @version Bgee 15.0
 *
 */
public class Strain extends NamedEntity<String> implements OntologyElement<Strain, String> {
    /**
     * Constructor providing the ID of this {@code Strain}. 
     * {@code id} cannot be blank, otherwise an {@code IllegalArgumentException} is thrown. 
     * 
     * @param id    A {@code String} representing the ID of this {@code Strain}.
     * @throws IllegalArgumentException     if {@code id} is blank. 
     **/
    public Strain(String id) {
        super(id);
    }

}
