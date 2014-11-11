package org.bgee.model.dao.api.gene;

import java.util.Collection;

import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.DAOResultSet;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.ontologycommon.NestedSetModelElementTO;

/**
 * DAO defining queries using or retrieving {@link HierarchicalGroupTO}s.
 * 
 * @author Komal Sanjeev
 * @author Frederic Bastian
 * @author Valentine Rech de Laval
 * @version Bgee 13
 * @see HierarchicalGroupTO
 * @since Bgee 13
 */
public interface HierarchicalGroupDAO extends DAO<HierarchicalGroupDAO.Attribute> {

    /**
     * {@code Enum} used to define the attributes to populate in the
     * {@code HierarchicalGroupTO}s obtained from this {@code HierarchicalGroupDAO}.
     * <ul>
     * <li>{@code ID}: corresponds to {@link HierarchicalGroupTO#getId()}.
     * <li>{@code OMA_GROUP_ID}: corresponds to {@link HierarchicalGroupTO#getOMAGroupId()}.
     * <li>{@code LEFT_BOUND}:  corresponds to {@link HierarchicalGroupTO#getLeftBound()}.
     * <li>{@code RIGHT_BOUND}: corresponds to {@link HierarchicalGroupTO#getRightBound()}.
     * <li>{@code TAXON_ID}: corresponds to {@link HierarchicalGroupTO#getTaxonId()}.
     * </ul>
     * 
     * @see org.bgee.model.dao.api.DAO#setAttributes(Collection)
     * @see org.bgee.model.dao.api.DAO#setAttributes(Enum[])
     * @see org.bgee.model.dao.api.DAO#clearAttributes()
     */
    public enum Attribute implements DAO.Attribute {
        ID, OMA_GROUP_ID, LEFT_BOUND, RIGHT_BOUND, TAXON_ID;
    }

    /**
     * Inserts the provided Hierarchical Groups into the Bgee database, represented as a
     * {@code Collection} of {@code HierarchicalGroupTO}s.
     * 
     * @param groups        A {@code Collection} of {@code HierarchicalGroupTO}s to be
     *                      inserted into the database.
     * @throws IllegalArgumentException If {@code groups} is empty or null. 
     * @throws DAOException If a {@code SQLException} occurred while trying to insert
     *                      {@code terms}. The {@code SQLException} will be wrapped into a
     *                      {@code DAOException} ({@code DAOs} do not expose these kind of
     *                      implementation details).
     */
    public int insertHierarchicalGroups(Collection<HierarchicalGroupTO> groups)
            throws DAOException, IllegalArgumentException;

//    /**
//     * Retrieves all the orthologous genes corresponding to the queried gene at the
//     * taxonomy level specified.
//     * <p>
//     * This method takes as parameters a {@code String} representing the gene ID, and a
//     * {@code String} representing the NCBI taxonomy ID for the taxonomy level queried.
//     * Then, the IDs of orthologous genes for the submitted gene ID at the particular
//     * taxonomy level are retrieved and returned as a {@code Collection} of 
//     * {@code String}s.
//     * 
//     * @param queryGene      A {@code String} representing the gene ID queried, whose
//     *                       orthologous genes are to be retrieved.
//     * @param taxonId A {@code String} representing the NCBI taxonomy ID of the
//     *                       hierarchical level queried.
//     * @return               A {@code Collection} of {@code String}s containing the IDs of
//     *                       orthologous genes of the query gene corresponding to the 
//     *                       taxonomy level queried.
//     * @throws DAOException  If an error occurred when accessing the data source.
//     */
//    public Collection<String> getHierarchicalOrthologousGenes(String queryGene,
//            String taxonId) throws DAOException;
//
//    /**
//     * Retrieves the orthologous genes corresponding to the queried gene at the taxonomy
//     * level specified, belonging to the species specified.
//     * <p>
//     * This method takes as parameters a {@code String} representing the gene ID, a
//     * {@code String} representing the NCBI taxonomy ID for the taxonomy level queried,
//     * and a {@code Collection} of {@code String}s representing the species the returned
//     * genes should belong to. The IDs of orthologous genes are returned as a
//     * {@code Collection} of {@code String}s.
//     * 
//     * @param queryGene      A {@code String} representing the gene ID queried, whose
//     *                       orthologous genes are to be retrieved.
//     * 
//     * @param taxonId A {@code String} representing the NCBI taxonomy ID of the
//     *                       hierarchical level queried.
//     * @param speciesIds     A {@code Collection} of {@code String}s containing the IDs of
//     *                       the species the returned genes should belong to.
//     * @return               A {@code Collection} of {@code String}s containing the IDs of
//     *                       orthologous genes of the query gene corresponding to the
//     *                       taxonomy level queried.
//     * @throws DAOException  If an error occurred when accessing the data source.
//     */
//    public Collection<String> getHierarchicalOrthologousGenesForSpecies(String queryGene,
//            String taxonId, Collection<String> speciesIds) throws DAOException;    

    /**
     * {@code DAOResultSet} specifics to {@code HierarchicalGroupTO}s
     * 
     * @author Valentine Rech de Laval
     * @version Bgee 13
     * @since Bgee 13
     */
    public interface HierarchicalGroupTOResultSet extends
            DAOResultSet<HierarchicalGroupTO> {

    }

    /**
     * {@code TransferObject} for the class {@link org.bgee.model.dao.api.gene.HierarchicalGroupDAO}.
     * <p>
     * For information on this {@code TransferObject} and its fields, see the corresponding class.
     * 
     * @author Komal Sanjeev
     * @author Frederic Bastian
     * @author Valentine Rech de Laval
     * @version Bgee 13
     * @see org.bgee.model.dao.api.gene.HierarchicalGroupDAO
     * @since Bgee 13
     */
    public class HierarchicalGroupTO extends NestedSetModelElementTO {

        private static final long serialVersionUID = 3491884200260547404L;

        /**
         * A {@code String} representing the ID for a particular OMA group of orthologous
         * genes.
         */
        private final String OMAGroupId;

        /**
         * An {@code Integer} representing the NCBI taxonomy ID of the hierarchical level
         * queried.
         */
        private final Integer taxonId;

        /**
         * Constructor providing the node ID, the OMA Group ID, and the hierarchical left
         * and right bounds.
         * <p>
         * All of these parameters are optional, so they can be {@code null} when not used.
         * If a bound is not {@code null}, it should be positive.
         * 
         * @param id            An {@code Integer} that is the ID of a node in the tree of
         *                      hierarchical groups
         * @param OMAGroupId    A {@code String} that is the ID for a particular OMA
         *                      group of orthologous genes
         * @param leftBound     An {@code Integer} that is the hierarchical left bound of the
         *                      nested set.
         * @param rightBound    An {@code Integer} that is the hierarchical right bound of
         *                      the nested set.
         * @throws IllegalArgumentException If {@code id} is empty, or if any of {code leftBound} or
         *                                  {code rightBound} is not {@code null} and less than 0.
         */
        public HierarchicalGroupTO(Integer id, String OMAGroupId, Integer leftBound,
                Integer rightBound) throws IllegalArgumentException {
            this(id, OMAGroupId, leftBound, rightBound, null);
        }

        /**
         * Constructor providing the node ID, the OMA Group ID, the hierarchical left and
         * right bounds, and the NCBI taxonomy ID.
         * <p>
         * All of these parameters are optional, so they can be {@code null} when not used.
         * If a bound is not {@code null}, it should be positive.
         * 
         * @param id            An {@code Integer} that is the ID of a node in the tree of
         *                      hierarchical groups
         * @param OMAGroupId    A {@code String} that is the ID for a particular OMA
         *                      group of orthologous genes
         * @param leftBound     An {@code Integer} that is the hierarchical left bound of the
         *                      nested set.
         * @param rightBound    An {@code Integer} that is the hierarchical right bound of
         *                      the nested set.
         * @param taxonId       An {@code Integer} that is the NCBI taxonomy ID of the
         *                      hierarchical level queried.
         * @throws IllegalArgumentException If {@code id} is empty, or if any of {code leftBound} or
         *                                  {code rightBound} is not {@code null} and less than 0.
         */
        public HierarchicalGroupTO(Integer id, String OMAGroupId, Integer leftBound,
                Integer rightBound, Integer taxonId) throws IllegalArgumentException {
            super(String.valueOf(id), null, null, leftBound, rightBound, null);
            this.OMAGroupId = OMAGroupId;
            this.taxonId = taxonId;
        }

        /**
         * @return A {@code String} that this the ID of a node in the tree of hierarchical
         *         groups. Corresponds to the DAO {@code Attribute}
         *         {@link HierarchicalGroupDAO.Attribute ID}. Returns {@code null} if
         *         value not set.
         */
        @Override
        public String getId() {
            // method overridden only to provide a more accurate javadoc
            return super.getId();
        }

        /**
         * @return A {@code String} that this the ID of a particular Hierarchical
         *         Orthologous Group as provided by OMA. Only for XRef purpose. 
         *         Corresponds to the DAO {@code Attribute} 
         *         {@link HierarchicalGroupDAO.Attribute OMA_GROUP_ID}.
         */
        public String getOMAGroupId() {
            return this.OMAGroupId;
        }

        /**
         * @return An {@code Integer} representing the NCBI taxonomy ID of the hierarchical
         *         level queried.
         */
        //TODO: more explanatory javadoc
        public int getTaxonId() {
            return this.taxonId;
        }

        @Override
        public String toString() {
            return "ID: " + this.getId() + " - Label: " + this.getName() + 
                   " - OMA Group Id: " + this.getOMAGroupId() + 
                   " - Hierarchical left bound: " + this.getLeftBound() + 
                   " - Hierarchical right bound: " + this.getRightBound() + 
                   " - NCBI taxonomy ID: " + this.getTaxonId();
        }
    }
}
