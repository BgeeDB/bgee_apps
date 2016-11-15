package org.bgee.model.species;

import org.bgee.model.NamedEntity;
import org.bgee.model.ontology.OntologyElement;

/**
 * Class describing taxa.
 * 
 * @author  Frederic Bastian
 * @author  Philippe Moret
 * @author  Valentine Rech de Laval
 * @version Bgee 13, Aug. 2016
 * @version Bgee 13, Sep. 2013
 */
public class Taxon extends NamedEntity implements OntologyElement<Taxon> {

    /**
     * Default constructor not public, an ID must always be provided, 
     * see {@link #AnatEntity(String)}.
     */
    //Constructor not public on purpose, suppress warnings
    @SuppressWarnings("unused")
    private Taxon() {
        this(null);
    }

    /**
     * Constructor providing the ID of this {@code Taxon}. 
     * {@code id} cannot be blank, otherwise an {@code IllegalArgumentException} is thrown. 
     * 
     * @param id    A {@code String} representing the ID of this {@code Taxon}.
     * @throws IllegalArgumentException     if {@code id} is blank. 
     */
    public Taxon(String id) {
        this(id, null, null);
    }

    /**
     * Constructor providing the ID, name, and description corresponding to this {@code Taxon}. 
     * {@code id} cannot be blank, otherwise an {@code IllegalArgumentException} is thrown. 
     * 
     * @param id            A {@code String} representing the ID of this {@code Taxon}.
     * @param name          A {@code String} representing the name of this {@code Taxon}.
     * @param description   A {@code String} representing the description of this {@code Taxon}.
     * @throws IllegalArgumentException     if {@code id} is blank. 
     */
    public Taxon(String id, String name, String description) {
        super(id, name, description);
    }
}
