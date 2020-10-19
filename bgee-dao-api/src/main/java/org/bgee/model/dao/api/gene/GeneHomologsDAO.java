package org.bgee.model.dao.api.gene;

import java.util.Collection;
import java.util.Set;

import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.DAOResultSet;
import org.bgee.model.dao.api.TransferObject;

/**
 * DAO defining queries inserting or retrieving {@link GeneHomologsTO}s. 
 * 
 * @author  Julien Wollbrett
 * @version Bgee 15, Aug. 2020
 * @since   Bgee 15, Aug. 2020
 * @see GeneHomologsTO
 */
public interface GeneHomologsDAO extends DAO<GeneHomologsDAO.Attribute>{
    
    /**
     * {@code Enum} used to define the attributes to populate in the {@code GeneHomologsTO}s 
     * obtained from this {@code GeneHomologsDAO}.
     * <ul>
     * <li>{@code BGEE_GENE_ID}: corresponds to {@link GeneHomologsTO#getBgeeGeneId()}.
     * <li>{@code TARGET_ENSEMBL_ID}: corresponds to {@link GeneHomologsTO#getTargetGeneId()}.
     * <li>{@code TAXON_ID}: corresponds to {@link GeneHomologsTO#getTaxonId()}.
     * </ul>
     * @see org.bgee.model.dao.api.DAO#setAttributes(Collection)
     * @see org.bgee.model.dao.api.DAO#setAttributes(Enum[])
     * @see org.bgee.model.dao.api.DAO#clearAttributes()
     */
    public enum Attribute implements DAO.Attribute {
        BGEE_GENE_ID, TARGET_ENSEMBL_ID, TAXON_ID;
    }
    
    public GeneHomologsTOResultSet getOrthologousGenes(Set<Integer> bgeeGeneIds);
    
    public GeneHomologsTOResultSet getOrthologousGenesAtTaxonLevel(Set<Integer> bgeeGeneIds, 
            Integer taxonId, boolean withDescendantTaxon, Set<Integer> speciesIds);
    
    public GeneHomologsTOResultSet getParalogousGenes(Set<Integer> bgeeGeneIds);
    
    public GeneHomologsTOResultSet getParalogousGenesAtTaxonLevel(Set<Integer> bgeeGeneIds, 
            Integer taxonId, boolean withDescendantTaxon, Set<Integer> speciesIds);
    
    public void insertParalogs(Set<GeneHomologsTO> paralogs);
    
    public void insertOrthologs(Set<GeneHomologsTO> orthologs);
    
    public interface GeneHomologsTOResultSet extends DAOResultSet<GeneHomologsTO> {
        
    }
    
    /**
     * {@code TransfertObject} representing an homology relation between one bgee gene ID and
     * one ensembl ID in the Bgee database.
    * 
    * @author  Julien Wollbrett
    * @version Bgee 15, Aug. 2020
    * @since   Bgee 15, Aug. 2020
    */
    
    public class GeneHomologsTO extends TransferObject {
        private static final long serialVersionUID = 1453371494754653227L;

        /**
         * An {@code Integer} that is the Bgee gene ID of the gene homologs have to be retrieved
         */
        private final Integer bgeeGeneId;

        /**
         * An {@code String} that is the official gene ID of the target gene of the homology relation.
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
         * @param targetGeneId          A {@code String} that is the official gene ID of the target gene of the
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
