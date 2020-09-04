package org.bgee.model.dao.api.gene;

import java.util.Collection;
import java.util.Set;

import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.DAOResultSet;
import org.bgee.model.dao.api.TransferObject;
import org.bgee.model.dao.api.anatdev.StageDAO.StageTO;
import org.bgee.model.dao.api.gene.GeneXRefDAO.GeneXRefTO;

/**
 * DAO defining queries inserting, using or retrieving {@link GeneHomologsTO}s. 
 * 
 * @author  Julien Wollbrett
 * @version Bgee 15, Aug. 2020
 * @since   Bgee 15, Aug. 2020
 * @see GeneHomologsTO
 */
public interface GeneHomologsDAO extends DAO<GeneHomologsDAO.Attribute>{
    
    /**
     * {@code Enum} used to define the attributes to populate in the {@code GeneOrthologsTO}s 
     * obtained from this {@code GeneOrthologsDAO}.
     * <ul>
     * <li>{@code SOURCE_BGEE_GENE_ID}: corresponds to {@link GeneHomologsTO#getSourceBgeeGeneId()}.
     * <li>{@code TARGET_BGEE_GENE_ID}: corresponds to {@link GeneHomologsTO#getTargetBgeeGeneId()}.
     * <li>{@code TAXON_ID}: corresponds to {@link GeneHomologsTO#getTaxonId()}.
     * </ul>
     * @see org.bgee.model.dao.api.DAO#setAttributes(Collection)
     * @see org.bgee.model.dao.api.DAO#setAttributes(Enum[])
     * @see org.bgee.model.dao.api.DAO#clearAttributes()
     */
    public enum Attribute implements DAO.Attribute {
        SOURCE_BGEE_GENE_ID, TARGET_BGEE_GENE_ID, TAXON_ID;
    }
    
    public GeneHomologsTOResultSet getOrthologousGenes(Integer bgeeGeneId);
    
    public GeneHomologsTOResultSet getOrthologousGenesAtTaxonLevel(Integer bgeeGeneId, Integer taxonId);
    
    public GeneHomologsTOResultSet getParalogousGenes(Integer bgeeGeneId);
    
    public GeneHomologsTOResultSet getParalogousGenesAtTaxonLevel(Integer bgeeGeneId, Integer taxonId);
    
    public void insertParalogs(Set<GeneHomologsTO> paralogs);
    
    public void insertOrthologs(Set<GeneHomologsTO> orthologs);
    
    public interface GeneHomologsTOResultSet extends DAOResultSet<GeneHomologsTO> {
        
    }
    
    /**
     * {@code TransfertObject} representing a symmetric homology relation between 2 genes 
     * in the Bgee database.
    * 
    * @author  Julien Wollbrett
    * @version Bgee 15, Aug. 2020
    * @since   Bgee 15, Aug. 2020
    */
    
    public class GeneHomologsTO extends TransferObject {
        private static final long serialVersionUID = 1453371494754653227L;

        /**
         * An {@code Integer} that is the Bgee ID of the source gene of the
         * homology relation.
         */
        private final Integer sourceBgeeGeneId;

        /**
         * An {@code Integer} that is the Bgee ID of the target gene of the
         * homology relation.
         */
        private final Integer targetBgeeGeneId;
        
        /**
         * An {@code Integer} that is the taxon ID of the least common ancestor of the 2 
         * species these genes belongs to in Bgee.
         */
        private final Integer taxonId;
        
        /**
         * Constructor providing the ID of the source Bgee gene ID, the ID of the target bgeeGeneId,
         * and the taxonomical level these 2 genes are homologs at.
         *
         * @param sourceBgeeGeneId    An {@code Integer} that is the Bgee ID of the source gene of the
         *                            homology relation.
         * @param targetBgeeGeneId    An {@code Integer} that is the Bgee ID of the target gene of the
         *                            homology relation.
         * @param taxonId             An {@code Integer} that is the taxon ID of the least common 
         *                            ancestor of the 2 species these genes belongs to in Bgee.
         */
        
        public GeneHomologsTO(Integer sourceBgeeGeneId, Integer targetBgeeGeneId, Integer taxonId) {
            this.sourceBgeeGeneId = sourceBgeeGeneId;
            this.targetBgeeGeneId = targetBgeeGeneId;
            this.taxonId = taxonId;
        }

        public Integer getSourceBgeeGeneId() {
            return sourceBgeeGeneId;
        }

        public Integer getTargetBgeeGeneId() {
            return targetBgeeGeneId;
        }

        public Integer getTaxonId() {
            return taxonId;
        }

        @Override
        public String toString() {
            return "GeneHomologsTO [sourceBgeeGeneId=" + sourceBgeeGeneId + 
                    ", targetBgeeGeneId=" + targetBgeeGeneId + ", taxonId=" + taxonId + "]";
        }
        
        
    }

}
