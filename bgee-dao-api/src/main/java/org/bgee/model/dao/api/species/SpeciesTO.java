package org.bgee.model.dao.api.species;

import org.apache.commons.lang3.StringUtils;
import org.bgee.model.dao.api.EntityTO;

/**
 * {@code EntityTO} representing a species in the Bgee data source.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 01
 */
public final class SpeciesTO extends EntityTO {

    /**
     * A {@code String} that is the genus of this species (for instance, <i>homo</i>).
     * Corresponds to the DAO {@code Attribute} {@link SpeciesDAO.Attribute GENUS}.
     */
    private final String genus;
    /**
     * A {@code String} that is the species name of this species (for instance, 
     * <i>sapiens</i>). Corresponds to the DAO {@code Attribute} 
     * {@link SpeciesDAO.Attribute SPECIES}.
     */
    private final String species;
    /**
     * A {@code String} that is the ID of the parent taxon of this species (for instance, 
     * {@code 9605} for <i>homo</i>, if this species was "human"). 
     * Corresponds to the DAO {@code Attribute} {@link SpeciesDAO.Attribute 
     * PARENTTAXONID}.
     */
    private final String parentTaxonId;
    /**
     * Constructor providing the ID, the common name, the genus, the species, and the ID 
     * of the parent taxon.
     * @param id            A {@code String} that is the ID.
     * @param commonName    A {@code String} that is the common name. 
     * @param genus         A {@code String} that is the genus of the species 
     *                      (for instance, <i>homo</i>).
     * @param species       A {@code String} that is the species name of the species 
     *                      (for instance, <i>sapiens</i>).
     * @param parentTaxonId A {@code String} that is the NCBI ID of the parent taxon 
     *                      of this species (for instance, {@code 9605} for <i>homo</i>, 
     *                      the parent taxon of human).
     * @throws IllegalArgumentException If one of the arguments is {@code null} or empty.
     */
    public SpeciesTO(String id, String commonName, String genus, String species, 
            String parentTaxonId) throws IllegalArgumentException {
        super(id, commonName);
        if (StringUtils.isBlank(commonName) || StringUtils.isBlank(genus) || 
                StringUtils.isBlank(species) || StringUtils.isBlank(parentTaxonId)) {
            throw new IllegalArgumentException("No argument can be null or empty.");
        }
        
        this.genus = genus;
        this.species = species;
        this.parentTaxonId = parentTaxonId;
    }
    
    /**
     * @return  The {@code String} that is the common name of this species. 
     *          Corresponds to the DAO {@code Attribute} {@link SpeciesDAO.Attribute 
     *          COMMONNAME}.
     */
    @Override
    public String getName() {
        //method overridden only to provide a more accurate javadoc
        return super.getName();
    }
    /**
     * @return  the {@code String} that is the genus of this species 
     *          (for instance, <i>homo</i>).
     *          Corresponds to the DAO {@code Attribute} {@link SpeciesDAO.Attribute 
     *          GENUS}.
     */
    public String getGenus() {
        return genus;
    }
    /**
     * @return  {@code String} that is the species name of this species 
     *          (for instance, <i>sapiens</i>).
     *          Corresponds to the DAO {@code Attribute} {@link SpeciesDAO.Attribute 
     *          SPECIES}.
     */
    public String getSpecies() {
        return species;
    }
    /**
     * @return  the {@code String} that is the ID of the parent taxon of this species 
     *          (for instance, {@code 9605} for <i>homo</i>, if this species was "human").
     *          Corresponds to the DAO {@code Attribute} {@link SpeciesDAO.Attribute 
     *          PARENTTAXONID}.
     */
    public String getParentTaxonId() {
        return parentTaxonId;
    }
    
}
