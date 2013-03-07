package org.bgee.model.data.common.hierarchicalGroup;

import java.util.Collection;

public interface HierarchicalGroupDAO {

	/**
	 * Retrieves all the orthologus genes corresponding to the queried gene at
	 * the taxonomy level specified.
	 * <p>
	 * This method takes as parameters a <code>String</code> representing the
	 * gene ID, and a <code>long</code> representing the NCBI taxonomy ID for
	 * the taxonomy level queried. Then, the orthologus genes for the submitted
	 * gene ID at the particular taxonomy level are retrieved and returned as a
	 * <code>Collection</code> of <code>String</code>.
	 * 
	 * @param queryGene
	 *            A <code>String</code> representing the gene ID queried, whose
	 *            orthologus genes are to be retrieved.
	 * 
	 * @param ncbiTaxonomyId
	 *            A <code>long</code> representing the NCBI taxonomy ID of the
	 *            hierarchical level queried.
	 * @return A <code>Collection</code> of <code>String</code> containing all
	 *         the orthologus genes of the query gene corresponding to the
	 *         taxonomy level queried.
	 */

	public Collection<String> getHierarchicalOrthologusGenes(String queryGene,
			long ncbiTaxonomyId);

	/**
	 * Retrieves the parent node for the query to be performed on the nested set
	 * model of {@link #getHierarchicalOrthologusGenes(String, long)}.
	 * 
	 * @param hierarchicalGroupTO
	 * @see #getHierarchicalOrthologusGenes(String, long)
	 */
	public void getParentNodeForNestedSetQuery(
			HierarchicalGroupTO hierarchicalGroupTO);

	/**
	 * Retrieves all the orthologus genes in the sub-group whose parent node is
	 * retrieved by {@link #getParentNodeForNestedSetQuery(HierarchicalGroupTO)}
	 * 
	 * @param hierarchicalGroupTO
	 * @see #getParentNodeForNestedSetQuery(HierarchicalGroupTO)
	 */
	public void performNestedSetQuery(HierarchicalGroupTO hierarchicalGroupTO);

}
