package org.bgee.model.dao.api.gene;

import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.DAOResultSet;
import org.bgee.model.dao.api.TransferObject;
import org.bgee.model.dao.api.gene.GeneDAO.GeneTO;

public interface GeneNameSynonymDAO extends DAO<GeneNameSynonymDAO.Attribute> {

	public enum Attribute implements DAO.Attribute {
		GENE_ID, GENE_NAME_SYNONYM;
	}
	
	
    public GeneNameSynonymTOResultSet getGeneNameSynonyms(String geneId);
	
    
    public interface GeneNameSynonymTOResultSet extends DAOResultSet<GeneNameSynonymTO> {
        
    }
 
	public class GeneNameSynonymTO extends TransferObject {
		private static final long serialVersionUID = -7383187272238679344L;
		
		private final String geneId;
		
		private final String geneNameSynonym;
		
		public GeneNameSynonymTO(String geneId, String geneNameSynonym){
			this.geneId = geneId;
			this.geneNameSynonym = geneNameSynonym;
		}

		/**
		 * @return the geneId
		 */
		public String getGeneId() {
			return geneId;
		}

		/**
		 * @return the geneNameSynonym
		 */
		public String getGeneNameSynonym() {
			return geneNameSynonym;
		}
		
		
	}
	
}
