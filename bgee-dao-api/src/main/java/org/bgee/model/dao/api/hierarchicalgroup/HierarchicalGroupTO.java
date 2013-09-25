package org.bgee.model.dao.api.hierarchicalgroup;

import java.util.ArrayList;
import java.util.Collection;

/**
 * {@code TransferObject} for the class 
 * {@link org.bgee.model.hierarchicalgroup.HierarchicalGroup}.
 * <p>
 * For information on this {@code TransferObject} and its fields, 
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
	 * A {@code String} representing the gene ID queried, whose orthologus
	 * genes are to be retrieved.
	 */
	public String queryGeneId;

	/**
	 * A {@code String} representing the NCBI taxonomy ID of the hierarchical
	 * level queried.
	 */
	public String ncbiTaxonomyId;
	/**
	 * A {@code String} representing the ID for a particular group of
	 * orthologus genes.
	 */
	public String orthologousGroupId;

	/**
	 * An {@code int} representing the ID of a node in the tree of 
	 * hierarchical groups. 
	 */
	public int hierarchicalGroupId;
	/**
	 * An {@code int} representing the hierarchical left bound ID which is
	 * generated when each hierarchical group is stored as a nested set.
	 */
	public int hierarchicalGroupLeftBound;

	/**
	 * An {@code int} representing the hierarchical right bound ID which is
	 * generated when each hierarchical group is stored as a nested set.
	 */
	public int hierarchicalGroupRightBound;

	/**
	 * A {@code Collection} of {@code String}s which represents the
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
