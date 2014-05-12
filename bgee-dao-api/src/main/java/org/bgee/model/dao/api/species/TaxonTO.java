package org.bgee.model.dao.api.species;

import org.bgee.model.dao.api.EntityTO;

/**
 * {@code EntityTO} representing a taxon in the Bgee data source.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public final class TaxonTO extends EntityTO {
	private static final long serialVersionUID = 704571970140502441L;
	/**
     * A {@code String} that is the scientific name of this taxon (for instance, 
     * "Corynebacteriaceae" for "Coryneform bacteria").
     * Corresponds to the DAO {@code Attribute} {@link TaxonDAO.Attribute SCIENTIFICNAME}.
     */
    private final String scientificName;
    /**
     * An {@code int} that is the left bound of this taxon in the nested set model 
     * (taxonomy is a tree represented in Bgee by a nested set model).
     * Corresponds to the DAO {@code Attribute} {@link TaxonDAO.Attribute LEFTBOUND}.
     */
    private final int leftBound;
    /**
     * An {@code int} that is the right bound of this taxon in the nested set model 
     * (taxonomy is a tree represented in Bgee by a nested set model).
     * Corresponds to the DAO {@code Attribute} {@link TaxonDAO.Attribute RIGHTBOUND}.
     */
    private final int rightBound;
    /**
     * An {@code int} that is the level of this taxon in the nested set model 
     * (taxonomy is a tree represented in Bgee by a nested set model). 
     * Corresponds to the DAO {@code Attribute} {@link TaxonDAO.Attribute LEVEL}.
     */
    private final int level;
    /**
     * A {@code boolean} defining whether this taxon is the least common ancestor 
     * of two species used in Bgee. This allows to easily identify important branchings.
     * Corresponds to the DAO {@code Attribute} {@link TaxonDAO.Attribute LCA}.
     */
    private final boolean lca;
    
    /**
     * Constructor providing the ID, the common name, the scientific name, 
     * the left bound, the right bound, the level, and whether it is a least 
     * common ancestor of two species used in Bgee. 
     * <p>
     * All of these parameters are optional except {@code id}, so they can be 
     * {@code null} when not used.
     * We do not use a {@code builder pattern}, because {@code TransferObject}s 
     * are not meant to be instantiated by clients, but only by the application, 
     * so we do not really care about having non-friendly constructors.
     * 
     * @param id                A {@code String} that is the ID.
     * @param commonName        A {@code String} that is the common name of this taxon.
     * @param scientificName    A {@code String} that is the scientific name of this taxon.
     * @param leftBound         An {@code Integer} that is the left bound of this taxon 
     *                          in the nested set model representing the taxonomy.
     * @param rightBound        An {@code Integer} that is the right bound of this taxon 
     *                          in the nested set model representing the taxonomy.
     * @param level             An {@code Integer} that is the level of this taxon 
     *                          in the nested set model representing the taxonomy.
     * @param lca               A {@code boolean} defining whether this taxon is the least 
     *                          common ancestor of two species used in Bgee. 
     * @throws IllegalArgumentException If {@code id} is {@code null} or empty.
     */
    public TaxonTO(String id, String commonName, String scientificName, 
            Integer leftBound, Integer rightBound, Integer level, Boolean lca) 
        throws IllegalArgumentException {
        super(id, commonName);
        if (leftBound != null && leftBound <= 0 || 
                rightBound != null && rightBound <= 0 || 
                level != null && level <= 0) {
            throw new IllegalArgumentException("Integer parameters must be positive.");
        }
        this.scientificName = scientificName;
        this.leftBound = leftBound;
        this.rightBound = rightBound;
        this.level = level;
        this.lca = lca;
    }
    
    /**
     * @return  The {@code String} that is the common name of this taxon 
     *          (for instance, "Coryneform bacteria" for "Corynebacteriaceae").
     *          Corresponds to the DAO {@code Attribute} {@link TaxonDAO.Attribute 
     *          COMMONNAME}. Returns {@code null} if value not set.
     */
    @Override
    public String getName() {
        //method overridden only to provide a more accurate javadoc
        return super.getName();
    }
    /**
     * @return  the {@code String} that is the scientific name of this taxon 
     *          (for instance, "Corynebacteriaceae" for "Coryneform bacteria").
     *          Corresponds to the DAO {@code Attribute} {@link TaxonDAO.Attribute 
     *          SCIENTIFICNAME}. Returns {@code null} if value not set.
     */
    public String getScientificName() {
        return scientificName;
    }
    /**
     * @return  the {@code int} that is the left bound of this taxon in the nested 
     *          set model (taxonomy is a tree represented in Bgee by a nested set model).
     *          Corresponds to the DAO {@code Attribute} {@link TaxonDAO.Attribute 
     *          LEFTBOUND}. Returns {@code null} if value not set.
     */
    public Integer getLeftBound() {
        return leftBound;
    }
    /**
     * @return  the {@code int} that is the right bound of this taxon in the nested 
     *          set model (taxonomy is a tree represented in Bgee by a nested set model).
     *          Corresponds to the DAO {@code Attribute} {@link TaxonDAO.Attribute 
     *          RIGHTBOUND}. Returns {@code null} if value not set.
     */
    public Integer getRightBound() {
        return rightBound;
    }
    /**
     * @return  the {@code int} that is the level of this taxon in the nested 
     *          set model (taxonomy is a tree represented in Bgee by a nested set model).
     *          Corresponds to the DAO {@code Attribute} {@link TaxonDAO.Attribute 
     *          LEVEL}. Returns {@code null} if value not set.
     */
    public Integer getLevel() {
        return level;
    }
    /**
     * @return  the {@code boolean} defining whether this taxon is the least 
     *          common ancestor of two species used in Bgee. This allows to easily 
     *          identify important branchings.
     *          Corresponds to the DAO {@code Attribute} {@link TaxonDAO.Attribute 
     *          LCA}. Returns {@code null} if value not set.
     */
    public Boolean isLca() {
        return lca;
    }

    @Override
    public String toString() {
        return "ID: " + this.getId() + " - Common name: " + this.getName() + 
                " - Scientific name: " + this.getScientificName() + 
                " - Left bound: " + this.getLeftBound() + " - Right bound: " + 
                this.getRightBound() + " - Level: " + this.getLevel() + 
                " - Is least common ancestor: " + this.isLca();
    }
}
