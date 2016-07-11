package org.bgee.model.anatdev;

/**
 * Class describing taxon constraints.
 * 
 * @author  Valentine Rech de Laval
 * @version Bgee 13, May 2016
 * @since   Bgee 13, May 2016
 */
public class TaxonConstraint {
    
    /**
     * A {@code String} that is the ID of the entity that has a taxon constraint.
     */
    private final String entityId;
    
    /**
     * A {@code String} that is the ID of the species that define the taxon constraint. 
     * If it is {@code null}, it means that the entity exists in all species.
     */
    private final String speciesId;

    /**
     * Constructor providing the {@code entityId} and the {@code speciesId}
     * of this {@code TaxonConstraint}.
     * <p>
     * If {@code speciesId} is {@code null}, it means that the entity exists in all species.
     * 
     * @param entityId    A {@code String} that is the ID of the entity that has a taxon constraint.
     * @param speciesId   A {@code String} that is the ID of the species that define
     *                    the taxon constraint.
     */
    public TaxonConstraint(String entityId, String speciesId) {
        this.entityId = entityId;
        this.speciesId = speciesId;
    }

    /**
     * @return  A {@code String} that is the ID of the entity that has a taxon constraint.
     */
    public String getEntityId() {
        return entityId;
    }

    /**
     * @return  A {@code String} that is the ID of the species that define the taxon constraint. 
     *          If it is {@code null}, it means that the entity exists in all species.
     */
    public String getSpeciesId() {
        return speciesId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((entityId == null) ? 0 : entityId.hashCode());
        result = prime * result + ((speciesId == null) ? 0 : speciesId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        TaxonConstraint other = (TaxonConstraint) obj;
        if (entityId == null) {
            if (other.entityId != null)
                return false;
        } else if (!entityId.equals(other.entityId))
            return false;
        if (speciesId == null) {
            if (other.speciesId != null)
                return false;
        } else if (!speciesId.equals(other.speciesId))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "Entity ID: " + this.getEntityId() + " - Species ID: " + this.getSpeciesId();
    }
}
