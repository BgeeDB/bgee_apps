package org.bgee.model.dao.api.species;

import org.bgee.model.dao.api.EntityTO;

/**
 * {@code EntityTO} representing a species in the Bgee data source.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 01
 */
public final class SpeciesTO extends EntityTO {

	private static final long serialVersionUID = 341628321446710146L;
	/**
     * A {@code String} that is the genus of this species (for instance, <i>homo</i>).
     * Corresponds to the DAO {@code Attribute} {@link SpeciesDAO.Attribute GENUS}.
     */
    private final String genus;
    /**
     * A {@code String} that is the species name of this species (for instance, 
     * <i>sapiens</i>). Corresponds to the DAO {@code Attribute} 
     * {@link SpeciesDAO.Attribute SPECIESNAME}.
     */
    private final String speciesName;
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
     * <p>
     * All of these parameters are optional except {@code id}, so they can be 
     * {@code null} when not used.
     * We do not use a {@code builder pattern}, because {@code TransferObject}s 
     * are not meant to be instantiated by clients, but only by the application, 
     * so we do not really care about having non-friendly constructors.
     * 
     * @param id            A {@code String} that is the ID.
     * @param commonName    A {@code String} that is the common name. 
     * @param genus         A {@code String} that is the genus of the species 
     *                      (for instance, <i>homo</i>).
     * @param speciesName   A {@code String} that is the species name of the species 
     *                      (for instance, <i>sapiens</i>).
     * @param parentTaxonId A {@code String} that is the NCBI ID of the parent taxon 
     *                      of this species (for instance, {@code 9605} for <i>homo</i>, 
     *                      the parent taxon of human).
     * @throws IllegalArgumentException If {@code id} is {@code null} or empty.
     */
    public SpeciesTO(String id, String commonName, String genus, String speciesName, 
            String parentTaxonId) throws IllegalArgumentException {
        super(id, commonName);
        
        this.genus = genus;
        this.speciesName = speciesName;
        this.parentTaxonId = parentTaxonId;
    }
    
    /**
     * @return  The {@code String} that is the common name of this species. 
     *          Corresponds to the DAO {@code Attribute} {@link SpeciesDAO.Attribute 
     *          COMMONNAME}. Returns {@code null} if value not set.
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
     *          GENUS}. Returns {@code null} if value not set.
     */
    public String getGenus() {
        return genus;
    }
    /**
     * @return  {@code String} that is the species name of this species 
     *          (for instance, <i>sapiens</i>).
     *          Corresponds to the DAO {@code Attribute} {@link SpeciesDAO.Attribute 
     *          SPECIESNAME}. Returns {@code null} if value not set.
     */
    public String getSpeciesName() {
        return speciesName;
    }
    /**
     * @return  the {@code String} that is the ID of the parent taxon of this species 
     *          (for instance, {@code 9605} for <i>homo</i>, if this species was "human").
     *          Corresponds to the DAO {@code Attribute} {@link SpeciesDAO.Attribute 
     *          PARENTTAXONID}. Returns {@code null} if value not set.
     */
    public String getParentTaxonId() {
        return parentTaxonId;
    }
    
    @Override
    public String toString() {
        return "ID: " + this.getId() + " - Common name: " + this.getName() + 
                " - Genus: " + this.getGenus() + " - Species name: " + this.getSpeciesName() + 
                " - Parent taxon ID: " + this.getParentTaxonId() + " - Description: " + 
                this.getDescription();
    }
}
