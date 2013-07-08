package org.bgee.model.dao.api.hierarchicalgroup;

import java.util.ArrayList;
import java.util.Collection;

/**
 * <code>TransferObject</code> for the class 
 * {@link org.bgee.model.hierarchicalgroup.HierarchicalGroup}.
 * <p>
 * For information on this <code>TransferObject</code> and its fields, 
 * see the corresponding class.
 * 
 * @author Komal Sanjeev
 * @author Frederic Bastian
 * @version Bgee 13
 * @see org.bgee.model.hierarchicalgroup.HierarchicalGroup
 * @since Bgee 13
 */
public class HierarchicalGroupTO {

	/**
	 * A <code>String</code> representing the gene ID queried, whose orthologus
	 * genes are to be retrieved.
	 */
	public String queryGeneId;

	/**
	 * A <code>String</code> representing the NCBI taxonomy ID of the hierarchical
	 * level queried.
	 */
	public String ncbiTaxonomyId;
	/**
	 * A <code>String</code> representing the ID for a particular group of
	 * orthologus genes.
	 */
	public String orthologousGroupId;

	/**
	 * An <code>int</code> representing the ID of a node in the tree of 
	 * hierarchical groups. 
	 */
	public int hierarchicalGroupId;
	/**
	 * An <code>int</code> representing the hierarchical left bound ID which is
	 * generated when each hierarchical group is stored as a nested set.
	 */
	public int hierarchicalGroupLeftBound;

	/**
	 * An <code>int</code> representing the hierarchical right bound ID which is
	 * generated when each hierarchical group is stored as a nested set.
	 */
	public int hierarchicalGroupRightBound;

	/**
	 * A <code>Collection</code> of <code>String</code>s which represents the
	 * IDs of orthologus genes retrieved for the {@link #queryGeneId} for the
	 * taxon represented by {@link #ncbiTaxonomyId}
	 */
	public Collection<String> orthologGenes;

	public HierarchicalGroupTO() {

		this.hierarchicalGroupId = 0;
		this.orthologousGroupId = null;
		this.hierarchicalGroupLeftBound = 0;
		this.hierarchicalGroupRightBound = 0;
		this.ncbiTaxonomyId = null;
		this.queryGeneId = null;
		this.orthologGenes = new ArrayList<String>();

	}

}
