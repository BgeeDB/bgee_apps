package org.bgee.model.dao.api.gene;

import java.util.Collection;

import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.DAOResultSet;
import org.bgee.model.dao.api.TransferObject;

/**
 * DAO defining queries inserting or retrieving {@link GeneHomologsTO}s. 
 * 
 * @author  Julien Wollbrett
 * @version Bgee 14.2, Feb. 2021
 * @since   Bgee 14.2, Feb. 2021
 * @see GeneHomologsTO
 */
public interface GeneHomologsDAO extends DAO<GeneHomologsDAO.Attribute>{
    
    /**
     * {@code Enum} used to define the attributes to populate in the {@code GeneHomologsTO}s 
     * obtained from this {@code GeneHomologsDAO}.
     * <ul>
     * <li>{@code BGEE_GENE_ID}: corresponds to {@link GeneHomologsTO#getBgeeGeneId()}.
     * <li>{@code TARGET_BGEE_GENE_ID}: corresponds to {@link GeneHomologsTO#getTargetGeneId()}.
     * <li>{@code TAXON_ID}: corresponds to {@link GeneHomologsTO#getTaxonId()}.
     * </ul>
     */
    public enum Attribute implements DAO.Attribute {
        BGEE_GENE_ID, TARGET_BGEE_GENE_ID, TAXON_ID;
    }
    
    public GeneHomologsTOResultSet getOrthologousGenes(Collection<Integer> bgeeGeneIds);
    
    public GeneHomologsTOResultSet getOrthologousGenesAtTaxonLevel(Collection<Integer> bgeeGeneIds, 
            Integer taxonId, boolean withDescendantTaxon, Collection<Integer> speciesIds);
    
    public GeneHomologsTOResultSet getParalogousGenes(Collection<Integer> bgeeGeneIds);
    
    public GeneHomologsTOResultSet getParalogousGenesAtTaxonLevel(Collection<Integer> bgeeGeneIds, 
            Integer taxonId, boolean withDescendantTaxon, Collection<Integer> speciesIds);
    
    public void insertParalogs(Collection<GeneHomologsTO> paralogs);
    
    public void insertOrthologs(Collection<GeneHomologsTO> orthologs);
    
    public interface GeneHomologsTOResultSet extends DAOResultSet<GeneHomologsTO> {
        
    }
    
    /**
     * {@code TransfertObject} representing an homology relation between two genes in the Bgee database.
    * 
    * @author  Julien Wollbrett
    * @version Bgee 14.2, Feb. 2021
    * @since   Bgee 14.2, Feb. 2021
    */
    
    public class GeneHomologsTO extends TransferObject {
        private static final long serialVersionUID = 1453371494754653227L;

        /**
         * An {@code Integer} that is the Bgee gene ID of the gene homologs have to be retrieved
         */
        private final Integer bgeeGeneId;

        /**
         * An {@code String} that is the Bgee gene ID of the target gene of the homology relation.
         */
        private final Integer targetGeneId;
        
        /**
         * An {@code Integer} that is the taxon ID of the least common ancestor of the 2 
         * species these genes belongs to in Bgee.
         */
        private final Integer taxonId;
        
        /**
         * Constructor providing the ID of the source Bgee gene ID, the ID of the target bgeeGeneId,
         * and the taxonomical level these 2 genes are homologs at.
         *
         * @param bgeeGeneId            An {@code Integer} that is the Bgee gene ID.
         * @param targetGeneId          An {@code Integer} that is the Bgee gene ID of the target gene of the
         *                              homology relation.
         * @param taxonId               An {@code Integer} that is the taxon ID of the least common 
         *                              ancestor of the 2 species these genes belongs to in Bgee.
         */
        
        public GeneHomologsTO(Integer bgeeGeneId, Integer targetGeneId, Integer taxonId) {
            this.bgeeGeneId = bgeeGeneId;
            this.targetGeneId = targetGeneId;
            this.taxonId = taxonId;
        }

        public Integer getBgeeGeneId() {
            return bgeeGeneId;
        }

        public Integer getTargetGeneId() {
            return targetGeneId;
        }

        public Integer getTaxonId() {
            return taxonId;
        }

        @Override
        public String toString() {
            return "GeneHomologsTO [sourceBgeeGeneId=" + bgeeGeneId + 
                    ", targetBgeeGeneId=" + targetGeneId + ", taxonId=" + taxonId + "]";
        }
    }

}
