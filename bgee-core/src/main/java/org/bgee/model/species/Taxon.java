package org.bgee.model.species;

import org.bgee.model.NamedEntity;
import org.bgee.model.ontology.OntologyElement;

/**
 * Class describing taxa.
 * 
 * @author  Frederic Bastian
 * @author  Philippe Moret
 * @author  Valentine Rech de Laval
 * @version Bgee 14 Mar. 2019
 * @version Bgee 13, Sep. 2013
 */
public class Taxon extends NamedEntity<Integer> implements OntologyElement<Taxon, Integer> {

    private final String scientificName;
    private final int level;
    private final boolean lca;

    /**
     * @param id                An {@code int} representing the NCBI ID of this {@code Taxon}.
     * @param commonName        A {@code String} representing the common name of this {@code Taxon}.
     * @param description       A {@code String} representing the description of this {@code Taxon}.
     * @param scientificName    See {@link #getScientificName()}.
     * @param level             See {@link #getLevel()}.
     * @param lca               See {@link #isLca()}.
     * @throws IllegalArgumentException if {@code id} is blank, or if {@code level} is non-{@code null}
     *                                  and less than or equal to 0.
     */
    public Taxon(int id, String commonName, String description, String scientificName,
            int level, boolean lca) {
        super(id, commonName, description);
        if (level <= 0) {
            throw new IllegalArgumentException("Level cannot be less than or equal to 0");
        }
        if (scientificName == null) {
            throw new IllegalArgumentException("Scientific name cannot be null");
        }
        this.scientificName = scientificName;
        this.level = level;
        this.lca = lca;
    }

    /**
     * @return  A {@code String} that is the scientific name of this {@code Taxon}.
     */
    public String getScientificName() {
        return scientificName;
    }
    /**
     * @return  An {@code int} that is the level in the taxonomy of this {@code Taxon}.
     */
    public int getLevel() {
        return level;
    }
    /**
     * @return  A {@code boolean} defining whether this {@code Taxon} is a least common ancestor.
     */
    public boolean isLca() {
        return lca;
    }

    //Entity classes do not implement equals/hashCode, only based on ID

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Taxon [id=").append(getId())
               .append(", name=").append(getName())
               .append(", description=").append(getDescription())
               .append(", scientificName=").append(scientificName)
               .append(", level=").append(level)
               .append(", lca=").append(lca).append("]");
        return builder.toString();
    }
}