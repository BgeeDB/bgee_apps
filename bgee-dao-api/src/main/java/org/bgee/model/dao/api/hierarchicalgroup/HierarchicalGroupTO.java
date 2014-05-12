package org.bgee.model.dao.api.hierarchicalgroup;

import org.bgee.model.dao.api.EntityTO;

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
	 * A {@code int} representing the ID for a particular OMA group of
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
	public HierarchicalGroupTO(int nodeId, int OMAGroupId, int nodeLeftBound, int nodeRightBound, String ncbiTaxonomyId) {
		super(String.valueOf(nodeId));
        if (OMAGroupId <= 0 || nodeLeftBound <= 0 || nodeRightBound <= 0 || 
        		ncbiTaxonomyId != null && Integer.parseInt(ncbiTaxonomyId) <= 0) {
            throw new IllegalArgumentException("Integer parameters must be positive.");
        }
		this.OMAGroupId = OMAGroupId;
		this.nodeLeftBound = nodeLeftBound;
		this.nodeRightBound = nodeRightBound;
		this.ncbiTaxonomyId = ncbiTaxonomyId;
	}
	
    /**
     * @return  A {@code String} that this the ID of a node in the tree of hierarchical groups.
     *          Corresponds to the DAO {@code Attribute} {@link HierarchicalGroupDAO.Attribute 
     *          NODEID}. Returns {@code null} if value not set.
	 */
	@Override
    public String getId() {
        //method overridden only to provide a more accurate javadoc
        return super.getId();
    }

	/**
     * @return  A {@code int} that this the ID of a node in the tree of hierarchical groups.
     *          Corresponds to the DAO {@code Attribute} {@link HierarchicalGroupDAO.Attribute 
     *          NODEID}. Returns {@code null} if value not set.
	 */
    public int getNodeId() {
        return Integer.parseInt(super.getId());
    }

    /**
     * @return  A {@code int} that this the ID for a particular OMA group 
     * 			of orthologous genes.
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
