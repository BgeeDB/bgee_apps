package org.bgee.model.dao.common.hierarchicalGroup;

import java.sql.SQLException;
import java.util.ArrayList;

/**
 * An interface
 * 
 * @author Komal Sanjeev
 */
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
	 *            A <code>String</code> representing the NCBI taxonomy ID of the
	 *            hierarchical level queried.
	 * @return A <code>Collection</code> of <code>String</code> containing all
	 *         the orthologus genes of the query gene corresponding to the
	 *         taxonomy level queried.
	 * @throws SQLException
	 */

	public ArrayList<String> getHierarchicalOrthologusGenes(String queryGene,
			String ncbiTaxonomyId) throws SQLException;
	
	public ArrayList<String> getHierarchicalOrthologusGenesForSpecies(String queryGene,
			String ncbiTaxonomyId, ArrayList<Long> speciesIds) throws SQLException;

}
