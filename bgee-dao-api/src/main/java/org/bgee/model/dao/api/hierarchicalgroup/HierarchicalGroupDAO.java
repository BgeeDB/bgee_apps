package org.bgee.model.dao.api.hierarchicalgroup;

import org.bgee.model.dao.api.DAO;

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
}
