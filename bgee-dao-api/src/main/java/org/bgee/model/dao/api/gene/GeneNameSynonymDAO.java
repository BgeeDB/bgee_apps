package org.bgee.model.dao.api.gene;

import java.util.Set;

import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.DAOResultSet;
import org.bgee.model.dao.api.TransferObject;

/**
 * DAO defining queries using or retrieving {@link GeneNameSynonymTO}s. 
 * 
 * @author Philippe Moret
 * @version Bgee 13.2
 * @see GeneNameSynonymTO
 * @since Bgee 13.2
 */
public interface GeneNameSynonymDAO extends DAO<GeneNameSynonymDAO.Attribute> {

    /**
     * {@code Enum} used to define the attributes to populate in the {@code GeneNameSynonymTO}s 
     * obtained from this {@code GeneNameSynonymDAO}.
     * <ul>
     * <li>{@code GENE_ID}: corresponds to {@link GeneNameSynonymTO#getGeneId()}.
     * <li>{@code GENE_NAME_SYNONYM}: corresponds to {@link GeneNameSynonymTO#getGeneNameSynonym()}.
     * </ul>
     * @see org.bgee.model.dao.api.DAO#setAttributes(Collection)
     * @see org.bgee.model.dao.api.DAO#setAttributes(Enum[])
     * @see org.bgee.model.dao.api.DAO#clearAttributes()
     */
	public enum Attribute implements DAO.Attribute {
		GENE_ID, GENE_NAME_SYNONYM;
	}
	
	/**
	 * Loads the gene name synonyms for a set of genes
	 * @param bgeeGeneIds A {@code Set} of gene IDs.
	 * @return A {@code GeneNameSynonymTOResultSet} encapsulating the result.
	 */
    public GeneNameSynonymTOResultSet getGeneNameSynonyms(Set<Integer> bgeeGeneIds);
	
    /**
     * {@code DAOResultSet} specifics to {@code GeneNameSynonymTO}s
     * 
     * @author Philippe Moret
     * @version Bgee 13.2
     * @since Bgee 13.2
     */
    public interface GeneNameSynonymTOResultSet extends DAOResultSet<GeneNameSynonymTO> {
        
    }
 
    /**
     * {@code EntityTO} representing a gene name synonym in the Bgee database.
     * 
     * @author Philippe Moret
     * @version Bgee 13.2
     * @since Bgee 13.2
     */
	public class GeneNameSynonymTO extends TransferObject {
		private static final long serialVersionUID = -7383187272238679344L;
		
		/**
		 * The gene ID
		 */
		private final Integer bgeeGeneId;
		
		/**
		 * The synonym
		 */
		private final String geneNameSynonym;
		
		/**
		 * Constructor providing the two fields of the {@code GeneNameSynonymTO}
		 * @param bgeeGeneId      An {@code Integer} representing the gene id
		 * @param geneNameSynonym A {@code String} containing the gene name synonyms
		 */
		public GeneNameSynonymTO(Integer bgeeGeneId, String geneNameSynonym){
			this.bgeeGeneId = bgeeGeneId;
			this.geneNameSynonym = geneNameSynonym;
		}

		/**
		 * @return the geneId
		 */
		public Integer getBgeeGeneId() {
			return bgeeGeneId;
		}

		/**
		 * @return the geneNameSynonym
		 */
		public String getGeneNameSynonym() {
			return geneNameSynonym;
		}
		
	}
	
}
