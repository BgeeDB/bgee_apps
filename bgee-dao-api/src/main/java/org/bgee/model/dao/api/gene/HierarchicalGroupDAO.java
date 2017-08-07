package org.bgee.model.dao.api.gene;

import java.util.Collection;
import java.util.Set;

import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.DAOResultSet;
import org.bgee.model.dao.api.TransferObject;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.ontologycommon.NestedSetModelElementTO;

/**
 * DAO defining queries using or retrieving {@link HierarchicalNodeTO}s.
 * XXX Is it better to create a HierarchicalNodeTO corresponding to database schema?
 * 
 * @author Komal Sanjeev
 * @author Frederic Bastian
 * @author Valentine Rech de Laval
 * @author Julien Wollbrett
 * @version Bgee 14
 * @see HierarchicalNodeTO
 * @since Bgee 13
 */
public interface HierarchicalGroupDAO extends DAO<HierarchicalGroupDAO.Attribute> {

    /**
     * {@code Enum} used to define the attributes to populate in the
     * {@code HierarchicalNodeTO}s obtained from this {@code HierarchicalGroupDAO}.
     * <ul>
     * <li>{@code ID}: corresponds to {@link HierarchicalNodeTO#getId()}.
     * <li>{@code OMA_GROUP_ID}: corresponds to {@link HierarchicalNodeTO#getOMAGroupId()}.
     * <li>{@code LEFT_BOUND}:  corresponds to {@link HierarchicalNodeTO#getLeftBound()}.
     * <li>{@code RIGHT_BOUND}: corresponds to {@link HierarchicalNodeTO#getRightBound()}.
     * <li>{@code TAXON_ID}: corresponds to {@link HierarchicalNodeTO#getTaxonId()}.
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
     * {@code Collection} of {@code HierarchicalNodeTO}s.
     * 
     * @param groups        A {@code Collection} of {@code HierarchicalNodeTO}s to be
     *                      inserted into the database.
     * @throws IllegalArgumentException If {@code groups} is empty or null. 
     * @throws DAOException If an {@code Exception} occurred while trying to insert
     *                      {@code terms}. The {@code SQLException} will be wrapped into a
     *                      {@code DAOException} ({@code DAOs} do not expose these kind of
     *                      implementation details).
     */
    public int insertHierarchicalNodes(Collection<HierarchicalNodeTO> groups)
            throws DAOException, IllegalArgumentException;

    /**
     * Inserts the provided GroupToGene into the Bgee database, represented as a
     * {@code Collection} of {@code HierarchicalNodeToGeneTO}s.
     * 
     * @param groupToGene   A {@code Collection} of {@code HierarchicalNodeToGeneTO}s to be
     *                      inserted into the database.
     * @throws IllegalArgumentException If {@code groupToGene} is empty or null. 
     * @throws DAOException If an {@code Exception} occurred while trying to insert
     *                      {@code terms}. The {@code SQLException} will be wrapped into a
     *                      {@code DAOException} ({@code DAOs} do not expose these kind of
     *                      implementation details).
     */
    public int insertHierarchicalNodeToGene(Collection<HierarchicalNodeToGeneTO> groupToGene)
            throws DAOException, IllegalArgumentException;
    
    public HierarchicalNodeTOResultSet getOMANodesFromStartingGenes(Collection<Integer> taxonIds, 
    		Integer startingSpeciesId, Set<String> startingGeneIds) 
    				throws DAOException, IllegalArgumentException;
    
    public HierarchicalNodeToGeneTOResultSet getGenesByNodeFromNodes(Collection<Integer> omaNodesIds,
    		Collection<Integer> speciesIds) throws DAOException, IllegalArgumentException;
    
    /**
	 * Retrieve the mapping from genes to groups of homologous genes, 
	 * valid for the provided taxon: genes that are homologous at the level 
	 * of the provided taxon will have the same group ID 
	 * (see HierarchicalNodeToGeneTO#getGroupId()). This group ID corresponds to the ID 
	 * of a Hierarchical Group (see {@link HierarchicalNodeTO}). 
	 * <p>
	 * Genes can be filtered further by providing a list of species: only genes belonging 
	 * to these species will be retrieved.
	 * <p>
	 * Note that using the {@code setAttributes} methods (see {@link DAO}) has no effect 
	 * on attributes retrieved in {@code HierarchicalNodeToGeneTO}s. Also, it is 
	 * the responsibility of the caller to close the returned {@code DAOResultSet} 
	 * once results are retrieved.
	 * 
	 * @param taxonId       An {@code int} that is the NCBI ID of the taxon for which 
	 *                      homologous genes should be retrieved.
	 * @param speciesIds    A {@code Set} of {@code Integer}s that are the IDs of species 
	 *                      for which we want to retrieve genes. Can be {@code null} or empty, 
	 *                      in order to retrieve all homologous genes for the provided taxon.
	 * @return              A {@code HierarchicalNodeToGeneTOResultSet} allowing to retrieve 
	 *                      the requested {@code HierarchicalNodeToGeneTO}s.
	 * @throws IllegalArgumentException If {@code taxonId} is empty or null. 
	 * @throws DAOException             If an error occurred when accessing the data source. 
	 */
	public HierarchicalNodeToGeneTOResultSet getOMANodeToGene(Integer taxonId, 
			Collection<Integer> speciesIds) throws DAOException, IllegalArgumentException;

	/**
     * {@code DAOResultSet} specifics to {@code HierarchicalNodeTO}s
     * 
     * @author Valentine Rech de Laval
     * @version Bgee 13
     * @since Bgee 13
     */
    public interface HierarchicalNodeTOResultSet extends
            DAOResultSet<HierarchicalNodeTO> {

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
    public class HierarchicalNodeTO extends NestedSetModelElementTO<Integer> {

        private static final long serialVersionUID = 3491884200260547404L;

        /**
         * A {@code String} representing the ID for a particular OMA group of orthologous
         * genes.
         */
        private final String omaGroupId;

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
        public HierarchicalNodeTO(Integer id, String OMAGroupId, Integer leftBound,
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
        public HierarchicalNodeTO(Integer id, String omaGroupId, Integer leftBound,
                Integer rightBound, Integer taxonId) throws IllegalArgumentException {
            super(id, null, null, leftBound, rightBound, null);
            this.omaGroupId = omaGroupId;
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
            return this.omaGroupId;
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
     * {@code DAOResultSet} specifics to {@code HierarchicalNodeToGeneTO}s.
     * 
     * @author Frederic Bastian
     * @version Bgee 13 Mar. 2015
     * @since Bgee 13
     */
    public interface HierarchicalNodeToGeneTOResultSet 
                    extends DAOResultSet<HierarchicalNodeToGeneTO> {
    }
    /**
     * A {@code TransferObject} allowing to map genes to groups of homologous genes. 
     * <p>
     * This class provides a node ID (see {@link #getNodeId()}, a gene ID 
     * (see {@link #getGeneId()}), and a taxon ID (see {@link #getTaxonId()}). The node ID corresponds to the ID of a node of a Hierarchical Group 
     * (see {@link HierarchicalNodeTO}).
     * <p>
     * Note that this class is one of the few {@code TransferObject}s that are not 
     * an {@link org.bgee.model.dao.api.EntityTO}.
     * 
     * @author Frederic Bastian
     * @version Bgee 13 Mar. 2015
     * @since Bgee 13
     */
    public final class HierarchicalNodeToGeneTO extends TransferObject {
        private static final long serialVersionUID = 3165617503583438525L;
        
        /**
         * An {@code Integer} representing the ID of a node of homologous genes. 
         * This corresponds to the ID of a node of Hierarchical Group (see {@link HierarchicalNodeTO}).
         */
        private final Integer nodeId;
        /**
         * An {@code Integer} that is the ID of a gene belonging to the node with ID 
         * {@link #nodeId}.
         */
        private final Integer bgeeGeneId;
        /**
         * An {@code Integer} that is the ID of a taxon.
         */
        private final Integer taxonId;
        
        /**
         * Constructor providing the ID of a node of a Hierarchical Group the ID of a gene belonging to 
         * the node, and the ID of the taxon belonging to the node.
         * 
         * @param nodeId        An {@code Integer} that is the ID of a node of a Hierarchical Group.
         * @param bgeeGeneId    An {@code Integer} that is the ID of a gene belonging to the node.
         * @param taxonId       An {@code Integer} that is the ID of a taxon belonging to the node.
         */
        public HierarchicalNodeToGeneTO (Integer nodeId, Integer bgeeGeneId, Integer taxonId) {
            this.nodeId = nodeId;
            this.bgeeGeneId = bgeeGeneId;
            this.taxonId = taxonId;
        }

        /**
         * @return  An {@code Integer} representing the ID of a node of Hierarchical Group. 
         *          (see {@link HierarchicalNodeTO}).
         */
        public Integer getNodeId() {
            return nodeId;
        }
        /**
         * @return  An {@code Integer} that is the ID of a gene belonging to the node with ID 
         *          {@link #getNodeId()}.
         */
        public Integer getBgeeGeneId() {
            return bgeeGeneId;
        }
        /**
         * @return  An {@code Integer} that is the ID of a taxonomy level belonging to the group with ID 
         *          {@link #getNodeId()}.
         */
        public Integer getTaxonId() {
            return taxonId;
        }
        
        @Override
        public String toString() {
            return "HierarchicalNodeToGeneTO[nodeId="+this.nodeId+", bgeeGeneId="+this.bgeeGeneId
            		+", taxonId="+this.getTaxonId()+"]";
        }
        
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((this.getBgeeGeneId() == null) ? 0 : this.getBgeeGeneId().hashCode());
            result = prime * result + ((this.getNodeId() == null) ? 0 : this.getNodeId().hashCode());
            result = prime * result + ((this.getTaxonId() == null) ? 0 : this.getTaxonId().hashCode());
            return result;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            HierarchicalNodeToGeneTO other = (HierarchicalNodeToGeneTO) obj;
            if (this.getBgeeGeneId() == null) {
                if (other.getBgeeGeneId() != null) {
                    return false;
                }
            } else if (!this.getBgeeGeneId().equals(other.getBgeeGeneId())) {
                return false;
            }
            if (this.getNodeId() == null) {
                if (other.getNodeId()  != null) {
                    return false;
                }
            } else if (!this.getNodeId() .equals(other.getNodeId() )) {
                return false;
            }
            if (this.getTaxonId() == null) {
                if (other.getTaxonId()  != null) {
                    return false;
                }
            } else if (!this.getTaxonId() .equals(other.getTaxonId() )) {
                return false;
            }
            return true;
        }
    }
}
