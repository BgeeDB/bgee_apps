package org.bgee.model.dao.api.hierarchicalgroup;

import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.DAOResultSet;
import org.bgee.model.dao.api.EntityTO;

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
public interface HierarchicalGroupDAO extends DAO<HierarchicalGroupDAO.Attribute>{

    /**
     * {@code Enum} used to define the attributes to populate in the {@code HierarchicalGroupTO}s 
     * obtained from this {@code HierarchicalGroupDAO}.
     * <ul>
     * <li>{@code NODEID}: corresponds to {@link HierarchicalGroupTO#getNodeId()}.
     * <li>{@code GROUPID}: corresponds to {@link HierarchicalGroupTO#getOMAGroupId()}.
     * <li>{@code NODELEFTBOUND}: corresponds to {@link HierarchicalGroupTO#getNodeLeftBound()}.
     * <li>{@code NODERIGHTBOUND}: corresponds to {@link HierarchicalGroupTO#getNodeRightBound()}.
     * <li>{@code TAXONID}: corresponds to {@link HierarchicalGroupTO#getNcbiTaxonomyId()}.
     * </ul>
     * @see org.bgee.model.dao.api.DAO.setAttributesToGet(Collection)
     * @see org.bgee.model.dao.api.DAO.setAttributesToGet(Object[])
     * @see org.bgee.model.dao.api.DAO.clearAttributesToGet()
     */
    public enum Attribute implements DAO.Attribute {
    	NODEID, GROUPID, NODELEFTBOUND, NODERIGHTBOUND, TAXONID;
    }

//	/**
//	 * Retrieves all the orthologus genes corresponding to the queried gene at
//	 * the taxonomy level specified.
//	 * <p>
//	 * This method takes as parameters a {@code String} representing the
//	 * gene ID, and a {@code String} representing the NCBI taxonomy ID for
//	 * the taxonomy level queried. Then, the IDs of orthologus genes for the submitted
//	 * gene ID at the particular taxonomy level are retrieved and returned as a
//	 * {@code Collection} of {@code String}s.
//	 * 
//	 * @param queryGene
//	 *            A {@code String} representing the gene ID queried, whose
//	 *            orthologus genes are to be retrieved.
//	 * 
//	 * @param ncbiTaxonomyId
//	 *            A {@code String} representing the NCBI taxonomy ID of the
//	 *            hierarchical level queried.
//	 * @return A {@code Collection} of {@code String}s containing 
//	 *         the IDs of orthologus genes of the query gene corresponding to the
//	 *         taxonomy level queried.
//     * @throws DAOException 	If an error occurred when accessing the data source.
//	 */
//	public Collection<String> getHierarchicalOrthologusGenes(String queryGene,
//			String ncbiTaxonomyId) throws DAOException;
//	
//	/**
//	 * Retrieves the orthologus genes corresponding to the queried gene at
//	 * the taxonomy level specified, belonging to the species specified.
//	 * <p>
//	 * This method takes as parameters a {@code String} representing the
//	 * gene ID, a {@code String} representing the NCBI taxonomy ID for
//	 * the taxonomy level queried, and a {@code Collection} of {@code String}s 
//	 * representing the species the returned genes should belong to. 
//	 * The IDs of orthologus genes are returned as a
//	 * {@code Collection} of {@code String}s.
//	 * 
//	 * @param queryGene
//	 *            A {@code String} representing the gene ID queried, whose
//	 *            orthologus genes are to be retrieved.
//	 * 
//	 * @param ncbiTaxonomyId
//	 *            A {@code String} representing the NCBI taxonomy ID of the
//	 *            hierarchical level queried.
//	 * @param speciesIds
//	 * 				A {@code Collection} of {@code String}s containing 
//	 * 				the IDs of the species the returned genes should belong to.
//	 * @return A {@code Collection} of {@code String}s containing 
//	 *         the IDs of orthologus genes of the query gene corresponding to the
//	 *         taxonomy level queried.
//     * @throws DAOException 	If an error occurred when accessing the data source.
//	 */
//	public Collection<String> getHierarchicalOrthologusGenesForSpecies(String queryGene,
//			String ncbiTaxonomyId, Collection<String> speciesIds) throws DAOException;
//
    
    /**
     * {@code DAOResultSet} specifics to {@code HierarchicalGroupTO}s
     * 
     * @author Valentine Rech de Laval
     * @version Bgee 13
     * @since Bgee 13
     */
	public interface HierarchicalGroupTOResultSet extends DAOResultSet<HierarchicalGroupTO> {
		
	}

    /**
     * {@code TransferObject} for the class 
     * {@link org.bgee.model.hierarchicalgroup.HierarchicalGroup}.
     * <p>
     * For information on this {@code TransferObject} and its fields, 
     * see the corresponding class.
     * 
     * @author Komal Sanjeev
     * @author Frederic Bastian
     * @author Valentine Rech de Laval
     * @version Bgee 13
     * @see org.bgee.model.hierarchicalgroup.HierarchicalGroup
     * @since Bgee 13
     */
    public class HierarchicalGroupTO extends EntityTO {

    	private static final long serialVersionUID = 3491884200260547404L;

    	/**
    	 * An {@code int} representing the ID for a particular OMA group of
    	 * orthologous genes.
    	 */
    	private final int OMAGroupId;

    	/**
    	 * An {@code int} representing the hierarchical left bound which is
    	 * generated when each hierarchical group is stored as a nested set.
    	 */
    	private final int nodeLeftBound;

    	/**
    	 * An {@code int} representing the hierarchical right bound which is
    	 * generated when each hierarchical group is stored as a nested set.
    	 */
    	private final int nodeRightBound;
    	
    	/**
    	 * A {@code String} representing the NCBI taxonomy ID of the hierarchical
    	 * level queried.
    	 */
    	private final String ncbiTaxonomyId;
    	
    	/**
         * Constructor providing the node ID, the OMA Group ID, and the hierarchical 
         * left and right bounds.
         * 
    	 * @param nodeId			an {@code int} that is the ID of a node in the tree of 
    	 * 							hierarchical groups
    	 * @param OMAGroupId		an {@code int} that is the ID for a particular OMA group of
    	 * 							orthologous genes
    	 * @param nodeLeftBound		an {@code int} that is the hierarchical left bound of the
    	 * 							nested set.
    	 * @param nodeRightBound	an {@code int} that is the hierarchical right bound of the
    	 * 							nested set.
    	 */
    	public HierarchicalGroupTO(int nodeId, int OMAGroupId, int nodeLeftBound, int nodeRightBound) {
    		this(nodeId, OMAGroupId, nodeLeftBound, nodeRightBound, null);
    	}

    	/**
         * Constructor providing the node ID, the OMA Group ID, the hierarchical 
         * left and right bounds, and the NCBI taxonomy ID.
         * 
    	 * @param nodeId			an {@code int} that is the ID of a node in the tree of 
    	 * 							hierarchical groups
    	 * @param OMAGroupId		an {@code int} that is the ID for a particular OMA group of
    	 * 							orthologous genes
    	 * @param nodeLeftBound		an {@code int} that is the hierarchical left bound of the
    	 * 							nested set.
    	 * @param nodeRightBound	an {@code int} that is the hierarchical right bound of the
    	 * 							nested set.
    	 * @param ncbiTaxonomyId	an {@code String} that is the NCBI taxonomy ID of the
    	 * 							hierarchical level queried.
    	 */
    	public HierarchicalGroupTO(int nodeId, int OMAGroupId, int nodeLeftBound, 
    				int nodeRightBound, String ncbiTaxonomyId) {
    		super(String.valueOf(nodeId));
    		this.OMAGroupId = OMAGroupId;
    		this.nodeLeftBound = nodeLeftBound;
    		this.nodeRightBound = nodeRightBound;
    		this.ncbiTaxonomyId = ncbiTaxonomyId;
    	}
    	
        /**
         * @return  A {@code String} that this the ID of a node in the tree of hierarchical 
         * 			groups. Corresponds to the DAO {@code Attribute} 
         * 			{@link HierarchicalGroupDAO.Attribute NODEID}. Returns {@code null} if
         * 			value not set.
    	 */
    	@Override
        public String getId() {
            //method overridden only to provide a more accurate javadoc
            return super.getId();
        }

    	/**
         * @return  An {@code int} that this an unique ID of a node in the tree of the generated
         * 			hierarchical groups. Corresponds to the DAO {@code Attribute} 
         * 			{@link HierarchicalGroupDAO.Attribute NODEID}. Same as 
         * 			{@link HierarchicalGroupTO#getId()} but return an {@code int} 
         * 			instead of a {@code String}.
    	 */
        public int getNodeId() {
            return Integer.parseInt(super.getId());
        }

        /**
         * @return  An {@code int} that this the ID of a particular Hierarchical Orthologous 
         * 			Group as provided by OMA. Corresponds to the DAO {@code Attribute} 
         * 			{@link HierarchicalGroupDAO.Attribute GROUPID}.
         */
        public int getOMAGroupId() {
            return this.OMAGroupId;
        }

        /**
         * @return  An {@code int} representing the hierarchical left bound 
         * 			which is generated when each hierarchical group is stored 
         * 			as a nested set.
         */
        public int getNodeLeftBound() {
            return this.nodeLeftBound;
        }

        /**
         * @return  An {@code int} representing the hierarchical right bound 
         * 			which is generated when each hierarchical group is stored 
         * 			as a nested set.
         */
        public int getNodeRightBound() {
            return this.nodeRightBound;
        }

        /**
         * @return  A {@code String} representing the NCBI taxonomy ID of the 
         * 			hierarchical level queried.
         */
        public String getNcbiTaxonomyId() {
            return this.ncbiTaxonomyId;
        }
    }

}
