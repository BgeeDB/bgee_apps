package org.bgee.model.dao.api.hierarchicalgroup;

import java.util.ArrayList;
import java.util.Collection;

/**
 * A object used to store parameters of the hierarchial orthologues query.
 * <p>
 * This object contains all the parameter required for the database query such
 * as the {@link #hierarchicalGroupId}, {@link #hierarchicalGroupLeftBound},
 * {@link #hierarchicalGroupRightBound} etc.
 */
public class HierarchicalGroupTO {

	/**
	 * A <code>String</code> representing the gene ID queried, whose orthologus
	 * genes are to be retrieved.
	 */
	public String queryGeneId;

	/**
	 * A <code>long</code> representing the NCBI taxonomy ID of the hierarchical
	 * level queried.
	 */
	public long ncbiTaxonomyId;

	/**
	 * 
	 */
	public long hierarchicalGroupId;

	/**
	 * A <code>long</code> representing the unique id for a particular group of
	 * orthologus genes.
	 */
	public long orthologousGroupId;

	/**
	 * A <code>long</code> representing the hierarchical left bound ID which is
	 * generated when each orthologus group is stored as a nested set.
	 */
	public long hierarchicalGroupLeftBound;

	/**
	 * A <code>long</code> representing the hierarchical right bound ID which is
	 * generated when each orthologus group is stored as a nested set.
	 */
	public long hierarchicalGroupRightBound;

	/**
	 * A <code>Collection</code> of <code>String</code> which represents the
	 * group of orthologus genes retrieved for the {@link #queryGeneId} at the
	 * {@link #ncbiTaxonomyId}
	 */
	public Collection<String> orthologGenes;

	public HierarchicalGroupTO() {

		this.hierarchicalGroupId = 0;
		this.orthologousGroupId = 0;
		this.hierarchicalGroupLeftBound = 0;
		this.hierarchicalGroupRightBound = 0;
		this.ncbiTaxonomyId = 0;
		this.queryGeneId = null;
		this.orthologGenes = new ArrayList<String>();

	}

}
