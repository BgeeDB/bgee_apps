package org.bgee.model.dao.api.hierarchicalgroup;

import org.bgee.model.dao.api.TransferObject;

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
public class HierarchicalGroupTO extends TransferObject {

	/**
	 * An {@code int} representing the ID of a node in the tree of 
	 * hierarchical groups. 
	 */
	private final int nodeId;

	/**
	 * A {@code int} representing the ID for a particular OMA group of
	 * orthologous genes.
	 */
	private final int OMAGroupId;

	/**
	 * An {@code int} representing the hierarchical left bound ID which is
	 * generated when each hierarchical group is stored as a nested set.
	 */
	private final int nodeLeftBound;

	/**
	 * An {@code int} representing the hierarchical right bound ID which is
	 * generated when each hierarchical group is stored as a nested set.
	 */
	private final int nodeRightBound;
	
	/**
	 * A {@code String} representing the NCBI taxonomy ID of the hierarchical
	 * level queried.
	 */
	private final String ncbiTaxonomyId;
	
	public HierarchicalGroupTO(int nodeId, int OMAGroupId, int nodeLeftBound, int nodeRightBound) {
		this(nodeId, OMAGroupId, nodeLeftBound, nodeRightBound, null);
	}

	public HierarchicalGroupTO(int nodeId, int OMAGroupId, int nodeLeftBound, int nodeRightBound, String ncbiTaxonomyId) {
		super();
		this.nodeId = nodeId;
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
     * @return  A {@code int} that this the ID of a node in the tree of 
	 * 			hierarchical groups.
	 */
    public int getNodeId() {
        return this.nodeId;
    }

    /**
     * @return  A {@code int} that this the ID for a particular OMA group 
     * 			of orthologous genes.
     */
    public int getOMAGroupId() {
        return this.OMAGroupId;
    }

    /**
     * @return  An {@code int} representing the hierarchical left bound ID 
     * 			which is generated when each hierarchical group is stored 
     * 			as a nested set.
     */
    public int getNodeLeftBound() {
        return this.nodeLeftBound;
    }

    /**
     * @return  An {@code int} representing the hierarchical right bound ID 
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
