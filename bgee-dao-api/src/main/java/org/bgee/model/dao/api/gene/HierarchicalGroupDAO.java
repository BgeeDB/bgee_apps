package org.bgee.model.dao.api.gene;

import java.util.Collection;
import java.util.Set;

import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.DAOResultSet;
import org.bgee.model.dao.api.TransferObject;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.ontologycommon.NestedSetModelElementTO;

/**
 * DAO defining queries using or retrieving {@link HierarchicalGroupTO}s.
 * 
 * @author Komal Sanjeev
 * @author Frederic Bastian
 * @author Valentine Rech de Laval
 * @version Bgee 14
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
     * @throws DAOException If an {@code Exception} occurred while trying to insert
     *                      {@code terms}. The {@code SQLException} will be wrapped into a
     *                      {@code DAOException} ({@code DAOs} do not expose these kind of
     *                      implementation details).
     */
    public int insertHierarchicalGroups(Collection<HierarchicalGroupTO> groups)
            throws DAOException, IllegalArgumentException;
    
    /**
     * Retrieve the mapping from genes to groups of homologous genes, 
     * valid for the provided taxon: genes that are homologous at the level 
     * of the provided taxon will have the same group ID 
     * (see HierarchicalGroupToGeneTO#getGroupId()). This group ID corresponds to the ID 
     * of a Hierarchical Group (see {@link HierarchicalGroupTO}). 
     * <p>
     * Genes can be filtered further by providing a list of species: only genes belonging 
     * to these species will be retrieved.
     * <p>
     * Note that using the {@code setAttributes} methods (see {@link DAO}) has no effect 
     * on attributes retrieved in {@code HierarchicalGroupToGeneTO}s. Also, it is 
     * the responsibility of the caller to close the returned {@code DAOResultSet} 
     * once results are retrieved.
     * 
     * @param taxonId       An {@code int} that is the NCBI ID of the taxon for which 
     *                      homologous genes should be retrieved.
     * @param speciesIds    A {@code Set} of {@code Integer}s that are the IDs of species 
     *                      for which we want to retrieve genes. Can be {@code null} or empty, 
     *                      in order to retrieve all homologous genes for the provided taxon.
     * @return              A {@code HierarchicalGroupToGeneTOResultSet} allowing to retrieve 
     *                      the requested {@code HierarchicalGroupToGeneTO}s.
     * @throws IllegalArgumentException If {@code taxonId} is empty or null. 
     * @throws DAOException             If an error occurred when accessing the data source. 
     */
    public HierarchicalGroupToGeneTOResultSet getGroupToGene(int taxonId, 
            Set<Integer> speciesIds) throws DAOException, IllegalArgumentException;

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
    public class HierarchicalGroupTO extends NestedSetModelElementTO<Integer> {

        private static final long serialVersionUID = 3491884200260547404L;

        /**
         * A {@code String} representing the ID for a particular OMA group of orthologous
         * genes.
         */
        private final String OMAGroupId;

        /**
         * An {@code Integer} representing the NCBI taxonomy ID corresponding to 
         * the node in the tree of hierarchical groups. It is equal to {@code 0} for paralog groups.
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
         *                      hierarchical groups.
         * @param OMAGroupId    A {@code String} that is the ID for a particular OMA
         *                      group of orthologous genes
         * @param leftBound     An {@code Integer} that is the hierarchical left bound of the
         *                      nested set.
         * @param rightBound    An {@code Integer} that is the hierarchical right bound of
         *                      the nested set.
         * @param taxonId       An {@code Integer} that is the NCBI taxonomy ID corresponding to 
         *                      the node in the tree of hierarchical groups.
         * @throws IllegalArgumentException If {@code id} is empty, or if any of {code leftBound} or
         *                                  {code rightBound} is not {@code null} and less than 0.
         */
        public HierarchicalGroupTO(Integer id, String OMAGroupId, Integer leftBound,
                Integer rightBound, Integer taxonId) throws IllegalArgumentException {
            super(id, null, null, leftBound, rightBound, null);
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
        public Integer getId() {
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
         * @return An {@code Integer} that is the NCBI taxonomy ID corresponding to 
         *         the node in the tree of hierarchical groups. 
         *         It is equal to {@code 0} for paralog groups.
         */
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
    
    /**
     * {@code DAOResultSet} specifics to {@code HierarchicalGroupToGeneTO}s.
     * 
     * @author Frederic Bastian
     * @version Bgee 13 Mar. 2015
     * @since Bgee 13
     */
    public interface HierarchicalGroupToGeneTOResultSet 
                    extends DAOResultSet<HierarchicalGroupToGeneTO> {
    }
    /**
     * A {@code TransferObject} allowing to map genes to groups of homologous genes. 
     * <p>
     * This class provides a group ID (see {@link #getGroupId()} and a gene ID 
     * (see {@link #getGeneId()}). The group ID corresponds to the ID of a Hierarchical Group 
     * (see {@link HierarchicalGroupTO}).
     * <p>
     * Note that this class is one of the few {@code TransferObject}s that are not 
     * an {@link org.bgee.model.dao.api.EntityTO}.
     * 
     * @author Frederic Bastian
     * @version Bgee 13 Mar. 2015
     * @since Bgee 13
     */
    public final class HierarchicalGroupToGeneTO extends TransferObject {
        private static final long serialVersionUID = 3165617503583438525L;
        
        /**
         * An {@code Integer} representing the ID of a group of homologous genes.
         * This corresponds to the ID of Hierarchical Group (see {@link HierarchicalGroupTO}).
         */
        private final Integer nodeId;
        /**
         * An {@code Integer} that is the ID of a gene belonging to the group with ID 
         * {@link #groupId}.
         */
        private final Integer bgeeGeneId;
        
        /** 
         * An {@code Integer} that is the ID of a taxonomy level belonging to the group with ID 
         * {@link #groupId}.
         */
        //XXX: why do we need this? Isn't it handled by the HierarchicalGroupTO already?
        private final Integer taxonId;
        
        /**
         * Constructor providing the ID of the group and the ID of a gene belonging to 
         * the group.
         * 
         * @param groupId       An {@code Integer} that is the ID of an group of homologous genes.
         * @param bgeeGeneId    An {@code Integer} that is the ID of a gene belonging to the group.
         * @param taxonId       An {@code Integer} that is the ID of a taxonomy level belonging to the group.
         */
        public HierarchicalGroupToGeneTO (Integer nodeId, Integer bgeeGeneId, Integer taxonId) {
            this.nodeId = nodeId;
            this.bgeeGeneId = bgeeGeneId;
            this.taxonId = taxonId;
        }

        /**
         * @return  An {@code Integer} representing the ID of a group of homologous genes. 
         *          This corresponds to the ID of Hierarchical Group (see 
         *          {@link HierarchicalGroupTO}).
         */
        public Integer getNodeId() {
            return nodeId;
        }
        /**
         * @return  An {@code Integer} that is the ID of a gene belonging to the group with ID 
         *          {@link #getNodeId()}.
         */
        public Integer getBgeeGeneId() {
            return bgeeGeneId;
        }
        /**
         * @return  An {@code Integer} that is the ID of a taxonomy level belonging to the group with ID 
         *          {@link #getNodeId()}.
         */
        //XXX: why do we need this? Isn't it handled by the HierarchicalGroupTO already?
        public Integer getTaxonId() {
            return taxonId;
        }

        @Override
        public String toString() {
            return "HierarchicalGroupToGeneTO[groupId="+this.nodeId+", bgeeGeneId="+this.bgeeGeneId+", taxonId="+this.taxonId+"]";
        }
    }
}
