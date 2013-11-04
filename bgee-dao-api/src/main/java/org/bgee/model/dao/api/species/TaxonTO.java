package org.bgee.model.dao.api.species;

import org.apache.commons.lang3.StringUtils;
import org.bgee.model.dao.api.EntityTO;

/**
 * {@code EntityTO} representing a taxon in the Bgee data source.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public final class TaxonTO extends EntityTO {

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
     * @param id                A {@code String} that is the ID.
     * @param commonName        A {@code String} that is the common name of this taxon.
     * @param scientificName    A {@code String} that is the scientific name of this taxon.
     * @param leftBound         An {@code int} that is the left bound of this taxon 
     *                          in the nested set model representing the taxonomy.
     * @param rightBound        An {@code int} that is the right bound of this taxon 
     *                          in the nested set model representing the taxonomy.
     * @param level             An {@code int} that is the level of this taxon 
     *                          in the nested set model representing the taxonomy.
     * @param lca               A {@code boolean} defining whether this taxon is the least 
     *                          common ancestor of two species used in Bgee.
     * @throws IllegalArgumentException If one of the arguments is {@code null} or empty.
     */
    public TaxonTO(String id, String commonName, String scientificName, 
            int leftBound, int rightBound, int level, boolean lca) 
        throws IllegalArgumentException {
        super(id, commonName);
        if (StringUtils.isBlank(commonName) || StringUtils.isBlank(scientificName) || 
                leftBound <= 0 || rightBound <= 0 || level <= 0) {
            throw new IllegalArgumentException("No argument can be null or empty, " +
            		"integer parameters must be positive.");
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
     *          COMMONNAME}.
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
     *          SCIENTIFICNAME}.
     */
    public String getScientificName() {
        return scientificName;
    }
    /**
     * @return  the {@code int} that is the left bound of this taxon in the nested 
     *          set model (taxonomy is a tree represented in Bgee by a nested set model).
     *          Corresponds to the DAO {@code Attribute} {@link TaxonDAO.Attribute 
     *          LEFTBOUND}.
     */
    public int getLeftBound() {
        return leftBound;
    }
    /**
     * @return  the {@code int} that is the right bound of this taxon in the nested 
     *          set model (taxonomy is a tree represented in Bgee by a nested set model).
     *          Corresponds to the DAO {@code Attribute} {@link TaxonDAO.Attribute 
     *          RIGHTBOUND}.
     */
    public int getRightBound() {
        return rightBound;
    }
    /**
     * @return  the {@code int} that is the level of this taxon in the nested 
     *          set model (taxonomy is a tree represented in Bgee by a nested set model).
     *          Corresponds to the DAO {@code Attribute} {@link TaxonDAO.Attribute 
     *          LEVEL}.
     */
    public int getLevel() {
        return level;
    }
    /**
     * @return  the {@code boolean} defining whether this taxon is the least 
     *          common ancestor of two species used in Bgee. This allows to easily 
     *          identify important branchings.
     *          Corresponds to the DAO {@code Attribute} {@link TaxonDAO.Attribute 
     *          LCA}.
     */
    public boolean isLca() {
        return lca;
    }
}
