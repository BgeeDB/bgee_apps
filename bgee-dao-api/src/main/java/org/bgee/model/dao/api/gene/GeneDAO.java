package org.bgee.model.dao.api.gene;

import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.DAOResultSet;
import org.bgee.model.dao.api.EntityTO;
import org.bgee.model.dao.api.gene.GOTermTO.Domain;

/**
 * DAO defining queries using or retrieving {@link GeneTO}s. 
 * 
 * @author Valentine Rech de Laval
 * @version Bgee 13
 * @see GeneTO
 * @since Bgee 13
 */
public interface GeneDAO extends DAO<GeneDAO.Attribute> {
    /**
     * {@code Enum} used to define the attributes to populate in the {@code GeneTO}s 
     * obtained from this {@code GeneDAO}.
     * <ul>
     * <li>{@code ID}: corresponds to {@link GeneTO#getId()}.
     * <li>{@code NAME}: corresponds to {@link GeneTO#getName()}.
     * <li>{@code DOMAIN}: corresponds to {@link GeneTO#getDomain()}.
     * <li>{@code SPECIESID}: corresponds to {@link GeneTO#getSpeciesId()}.
     * <li>{@code GENEBIOTYPEID}: corresponds to {@link GeneTO#getGeneBioTypeId()}.
     * <li>{@code OMANODEID}: corresponds to {@link GeneTO#getOMANodeId()}.
     * <li>{@code ENSEMBLGENE}: corresponds to {@link GeneTO#isEnsemblGene()}.
     * </ul>
     * @see org.bgee.model.dao.api.DAO.setAttributesToGet(Collection)
     * @see org.bgee.model.dao.api.DAO.setAttributesToGet(Object[])
     * @see org.bgee.model.dao.api.DAO.clearAttributesToGet()
     */
    public enum Attribute implements DAO.Attribute {
        ID, NAME, DESCRIPTION, SPECIESID, GENEBIOTYPEID, OMANODEID, ENSEMBLGENE;
    }
    
    /**
     * {@code DAOResultSet} specifics to {@code GeneTO}s
     * 
     * @author Valentine Rech de Laval
     * @version Bgee 13
     * @since Bgee 13
     */
	public interface GeneTOResultSet extends DAOResultSet<GeneTO> {
		
	}
    
    /**
     * {@code EntityTO} representing a Gene in the Bgee database.
     * 
     * @author Valentine Rech de Laval
     * @version Bgee 13
     * @since Bgee 13
     */
    public class GeneTO extends EntityTO {

    	private static final long serialVersionUID = -9011956802137411474L;


    	/**
         * A {@code int} that is the species id of the species.
         */
    	private final int speciesId;
        
    	
    	/**
    	 * A {@code int} that is the gene type ID (for instance,
    	 * the ID for protein_coding)
    	 */
    	private final int geneBioTypeId;
       
    	/**
    	 * A {@code int} that is unique ID for each node inside an OMA 
    	 * Hierarchical Orthologous Group. It can be null if the gene 
    	 * does not belong to a hierarchical group a gene can belong 
    	 * to one and only one group
    	 */
        private final int OMANodeId;
        
        /**
         * A {@code boolean} defining whether this gene is present in Ensembl. 
         * For some species, they are not (for instance, we generate 
         * our own custom IDs for some species)
         */
        private final boolean ensemblGene;

        /**
         * Constructor providing XXX.
         * 
         * @param geneId    a {@code String} that is the ID of this gene.
         * @param OMANodeId a {@code Integer} that is the id of the OMA Hierarchical 
         * 					Orthologous Group.
         */
    	public GeneTO(String geneId, Integer OMANodeId) {
    		this(geneId, null, null, null, null, OMANodeId, true);
    	}

    	/**
         * Constructor providing the ID (for instance, 
         * {@code Ensembl:ENSMUSG00000038253}), the name (for instance, 
         * {@code Hoxa5}), and the species id of this gene.
         * 
         * @param geneId    a {@code String} that is the ID of this gene.
         * @param geneName  a {@code String} that is the name of this gene.
         * @param speciesId a {@code int} of the species which this gene belongs to.
         */
    	public GeneTO(String geneId, String geneName, int speciesId) {
    		this(geneId, geneName, null, speciesId, null, null, true);
    	}

        /**
         * Constructor providing  XXX.
         * 
         * @param geneId    
         * 					a {@code String} that is the ID of this gene.
         * @param geneName  
         * 					a {@code String} that is the name of this gene.
         * @param geneDescription
         * 					a {@code String} that is the desciption of this gene.
         * @param speciesId 
         * 					a {@code Integer} that is the species id which this 
         * 					gene belongs to.
         * @param geneBioTypeId
         * 					a {@code Integer} that is the geneBioType of this gene.
         * @param OMANodeId
         * 					a {@code Integer} that is the id of the OMA Hierarchical 
         * 					Orthologous Group.
         * @param ensemblGene
         * 					a {code boolean} defining whether this gene is present 
         * 					in Ensembl. 
         */
    	public GeneTO(String geneId, String geneName, String geneDescription, Integer speciesId, Integer geneBioTypeId, Integer OMANodeId, boolean ensemblGene) {
    		super(geneId, geneName, geneDescription);
            if (speciesId != null && speciesId <= 0|| 
            		geneBioTypeId != null && geneBioTypeId <= 0 || 
            		OMANodeId != null && OMANodeId <= 0) {
                throw new IllegalArgumentException("Integer parameters must be positive.");
            }
    		this.speciesId = speciesId;
    		this.geneBioTypeId = geneBioTypeId;
    		this.OMANodeId = OMANodeId;
    		this.ensemblGene = ensemblGene;
    	}
    	
        /**
         * @return  The {@code String} that is the name of this gene 
         *          (for instance, "Hoxa5").
         *          Corresponds to the DAO {@code Attribute} {@link GeneDAO.Attribute 
         *          NAME}. Returns {@code null} if value not set.
         */
        @Override
        public String getName() {
            //method overridden only to provide a more accurate javadoc
            return super.getName();
        }


        /**
         * @return  The {@link Domain} that this Gene Ontology term belongs to.
         */
        public int getSpeciesId() {
            return this.speciesId;
        }

        /**
         * @return The the gene type ID (for instance, the ID for protein_coding)
         */
        public int getGeneBioTypeId() {
            return this.geneBioTypeId;
        }
        
        /**
         * @return  The OMA Hierarchical Orthologous Group ID that this gene belongs to.
         */
        public int getOMANodeId() {
            return this.OMANodeId;
        }
        
        /**
         * @return  the {@code boolean} defining whether this gene is present in Ensembl.
         */
        public boolean isEnsemblGene() {
            return this.ensemblGene;
        }
    }
}
