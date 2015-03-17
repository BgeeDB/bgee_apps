package org.bgee.model.dao.api.species;

import java.util.Collection;
import java.util.Set;

import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.DAOResultSet;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.ontologycommon.NestedSetModelElementTO;

/**
 * DAO defining queries using or retrieving {@link TaxonTO}s. 
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @see TaxonTO
 * @since Bgee 13
 */
public interface TaxonDAO extends DAO<TaxonDAO.Attribute> {
    /**
     * {@code Enum} used to define the attributes to populate in the {@code TaxonTO}s 
     * obtained from this {@code TaxonDAO}.
     * <ul>
     * <li>{@code ID}: corresponds to {@link TaxonTO#getId()}.
     * <li>{@code COMMON_NAME}: corresponds to {@link TaxonTO#getName()}.
     * <li>{@code SCIENTIFICNAME}: corresponds to {@link TaxonTO#getScientificName()}.
     * <li>{@code LEFTBOUND}: corresponds to {@link 
     * org.bgee.model.dao.api.ontologycommon.NestedSetModelElementTO#getLeftBound() 
     * NestedSetModelElementTO#getLeftBound()}.
     * <li>{@code RIGHTBOUND}: corresponds to {@link 
     * org.bgee.model.dao.api.ontologycommon.NestedSetModelElementTO#getRightBound() 
     * NestedSetModelElementTO#getRightBound()}.
     * <li>{@code LEVEL}: corresponds to {@link 
     * org.bgee.model.dao.api.ontologycommon.NestedSetModelElementTO#getLevel() 
     * NestedSetModelElementTO#getLevel()}.
     * <li>{@code LCA}: corresponds to {@link TaxonTO#isLca()}.
     * </ul>
     * @see org.bgee.model.dao.api.DAO#setAttributes(Collection)
     * @see org.bgee.model.dao.api.DAO#setAttributes(Enum[])
     * @see org.bgee.model.dao.api.DAO#clearAttributes()
     */
    public enum Attribute implements DAO.Attribute {
        ID, COMMON_NAME, SCIENTIFIC_NAME, LEFT_BOUND, RIGHT_BOUND, LEVEL, LCA;
    }
    
    /**
     * Inserts the provided taxa into the Bgee database, represented as 
     * a {@code Collection} of {@code TaxonTO}s.
     * 
     * @param taxa      a {@code Collection} of {@code TaxonTO}s to be inserted 
     *                  into the database.
     * @throws IllegalArgumentException If {@code taxa} is empty or null. 
     * @throws DAOException     If a {@code SQLException} occurred while trying 
     *                          to insert {@code taxa}. The {@code SQLException} 
     *                          will be wrapped into a {@code DAOException} ({@code DAOs} 
     *                          do not expose these kind of implementation details).
     */
    public int insertTaxa(Collection<TaxonTO> taxa) throws DAOException, IllegalArgumentException ;

    /**
     * Retrieve all taxa from data source.
     * <p>
     * The taxa are retrieved and returned as a {@code TaxonTOResultSet}. 
     * It is the responsibility of the caller to close this {@code DAOResultSet} once 
     * results are retrieved.
     * 
     * @return A {@code TaxonTOResultSet} containing all taxa from data source.
     * @throws DAOException If an error occurred when accessing the data source. 
     */
    public TaxonTOResultSet getAllTaxa() throws DAOException;
    
    /**
     * Retrieve the LCA of the provided species. If {@code includeAncestors} is {@code true}, 
     * all ancestors of the LCA will also be retrieved.
     * <p>
     * The {@code TaxonTOResultSet} returned is guaranteed to return at least 
     * one {@code TaxonTO} (in the most extreme case: the LCA of all species in Bgee). 
     * If {@code includeAncestors} is {@code false}, the {@code TaxonTOResultSet} returned 
     * is guaranteed to return one and only one {@code TaxonTO}.
     * 
     * @param speciesIds        A {@code Set} of {@code String}s that are the IDs of the species 
     *                          for which we want to retrieve the LCA.
     * @param includeAncestors  A {@code boolean} defining whether the ancestors of the LCA 
     *                          should also be retrieved; if {@code true}, there are retrieved.
     * @return                  A {@code TaxonTOResultSet} allowing to obtain the requested 
     *                          {@code TaxonTO}s.
     * @throws DAOException     If an error occurred when accessing the data source. 
     */
    public TaxonTOResultSet getLeastCommonAncestor(Set<String> speciesIds, 
            boolean includeAncestors) throws DAOException, IllegalArgumentException;

    /**
     * {@code DAOResultSet} specifics to {@code TaxonTO}s
     * 
     * @author Valentine Rech de Laval
     * @version Bgee 13
     * @since Bgee 13
     */
	public interface TaxonTOResultSet extends DAOResultSet<TaxonTO> {
		
	}

    /**
     * {@code NestedSetModelElementTO} representing a taxon in the Bgee data source.
     * 
     * @author Frederic Bastian
     * @version Bgee 13
     * @since Bgee 13
     */
    public final class TaxonTO extends NestedSetModelElementTO {
    	private static final long serialVersionUID = 704571970140502441L;
    	/**
         * A {@code String} that is the scientific name of this taxon (for instance, 
         * "Corynebacteriaceae" for "Coryneform bacteria").
         * Corresponds to the DAO {@code Attribute} {@link TaxonDAO.Attribute SCIENTIFICNAME}.
         */
        private final String scientificName;
        /**
         * A {@code boolean} defining whether this taxon is the least common ancestor 
         * of two species used in Bgee. This allows to easily identify important branchings.
         * Corresponds to the DAO {@code Attribute} {@link TaxonDAO.Attribute LCA}.
         */
        private final Boolean lca;
        
        /**
         * Constructor providing the ID, the common name, the scientific name, 
         * the left bound, the right bound, the level, and whether it is a least 
         * common ancestor of two species used in Bgee. 
         * <p>
         * <p>
         * All of these parameters are optional, so they can be {@code null} when not used.
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
         * @param lca               A {@code Boolean} defining whether this taxon is the 
         *                          least common ancestor of two species used in Bgee. 
         * @throws IllegalArgumentException If {@code id} is empty, or if any of 
         *                                  {code leftBound} or {code rightBound} or {code level} 
         *                                  is not {@code null} and less than 0.
         */
        public TaxonTO(String id, String commonName, String scientificName, 
                Integer leftBound, Integer rightBound, Integer level, Boolean lca) 
            throws IllegalArgumentException {
            super(id, commonName, null, leftBound, rightBound, level);
            this.scientificName = scientificName;
            this.lca = lca;
        }
        
        /**
         * @return  The {@code String} that is the common name of this taxon 
         *          (for instance, "Coryneform bacteria" for "Corynebacteriaceae").
         *          Corresponds to the DAO {@code Attribute} {@link TaxonDAO.Attribute 
         *          COMMON_NAME}. Returns {@code null} if value not set.
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
         * @return  the {@code Boolean} defining whether this taxon is the least 
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
}
