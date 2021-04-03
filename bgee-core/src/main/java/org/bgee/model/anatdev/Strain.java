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
        super(id.trim());
    }

    //We need to reimplement equals/hashCode (usually, it is not necessary for subclasses of Entity,
    //since the comparison is solely based on an ID).
    //The problem here is that the strain "ID" is really just a strain name,
    //there can be some upper/lowercase differences, mysql consider them equals,
    //not Java. This leads to have duplicated Conditions tried to be inserted in database
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.getId() == null) ? 0 : this.getId().toLowerCase().hashCode());
        return result;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Strain other = (Strain) obj;
        if (this.getId() == null) {
            if (other.getId() != null) {
                return false;
            }
        } else if (!this.getId().equalsIgnoreCase(other.getId())) {
            return false;
        }
        return true;
    }
}