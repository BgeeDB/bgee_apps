package org.bgee.model.dao.api.gene;

import java.util.Collection;
import java.util.Set;

import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.DAOResultSet;
import org.bgee.model.dao.api.EntityTO;
import org.bgee.model.dao.api.exception.DAOException;

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
     * <li>{@code DESCRIPTION}: corresponds to {@link GeneTO#getDescription()}.
     * <li>{@code SPECIESID}: corresponds to {@link GeneTO#getSpeciesId()}.
     * <li>{@code GENEBIOTYPEID}: corresponds to {@link GeneTO#getGeneBioTypeId()}.
     * <li>{@code OMAPARENTNODEID}: corresponds to {@link GeneTO#getOMAParentNodeId()}.
     * <li>{@code ENSEMBLGENE}: corresponds to {@link GeneTO#isEnsemblGene()}.
     * </ul>
     * @see org.bgee.model.dao.api.DAO#setAttributes(Collection)
     * @see org.bgee.model.dao.api.DAO#setAttributes(Enum[])
     * @see org.bgee.model.dao.api.DAO#clearAttributes()
     */
    public enum Attribute implements DAO.Attribute {
        ID, NAME, DESCRIPTION, SPECIESID, GENEBIOTYPEID, OMAPARENTNODEID, ENSEMBLGENE;
    }
    
    /**
     * Retrieve all genes from data source.
     * <p>
     * The genes are retrieved and returned as a {@code GeneTOResultSet}. It is the responsibility 
     * of the caller to close this {@code DAOResultSet} once results are retrieved.
     * 
     * @return              A {@code GeneTOResultSet} containing all genes from data source.
     * @throws DAOException If an error occurred when accessing the data source. 
     */
    public GeneTOResultSet getAllGenes() throws DAOException;
    
    /**
     * Retrieves genes from data source according to a {@code Set} of {@code String}s
     * that are the IDs of species allowing to filter the genes to use.
     * <p>
     * The genes are retrieved and returned as a {@code GeneTOResultSet}. It is the
     * responsibility of the caller to close this {@code DAOResultSet} once results are retrieved.
     * 
     * @param speciesIds    A {@code Set} of {@code String}s that are the IDs of species 
     *                      allowing to filter the genes to use.
     * @return              An {@code GeneTOResultSet} containing all genes from data source.
     * @throws DAOException If an error occurred when accessing the data source. 
     */
    //TODO: change method name to 'getGenesBySpeciesIds'
    public GeneTOResultSet getGenes(Set<String> speciesIds) throws DAOException;

    /**
     * Update {@code Attribute}s of the provided genes, which are represented as a 
     * {@code Collection} of {@code GeneTO}s
     * 
     * @param genes                 A {@code Collection} of {@code GeneTO}s that are genes
     *                              to be updated into the data source.
     * @param attributesToUpdate    A {@code Collection} of {@code Attribute}s that are 
     *                              attributes to be updated into the data source.
     * @return                      An {@code int} representing the number of genes updated.
     * @throws DAOException             If an error occurred while updating the data.
     * @throws IllegalArgumentException If {@code genes} is empty or null. 
     */
    public int updateGenes(Collection<GeneTO> genes, 
            Collection<GeneDAO.Attribute> attributesToUpdate) 
                    throws DAOException, IllegalArgumentException;
    
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
     * {@code EntityTO} representing a gene in the Bgee database.
     * 
     * @author Valentine Rech de Laval
     * @version Bgee 13
     * @since Bgee 13
     */
    public class GeneTO extends EntityTO {

        private static final long serialVersionUID = -9011956802137411474L;

        /**
         * An {@code Integer} that is the species ID of the gene.
         */
        private final Integer speciesId;
        
        
        /**
         * An {@code Integer} that is the gene type ID (for instance, the ID for protein_coding).
         */
        private final Integer geneBioTypeId;
       
        /**
         * An {@code Integer} that is unique ID for each node inside an OMA Hierarchical Orthologous 
         * Group. It can be {@code null} if the gene does not belong to a hierarchical group. A gene 
         * can belong to one and only one group.
         */
        private final Integer OMAParentNodeId;
        
        /**
         * A {@code Boolean} defining whether this gene is present in Ensembl. For some species, 
         * they are not (for instance, we generate our own custom IDs for some species)
         */
        private final Boolean ensemblGene;

        /**
         * Constructor providing the ID (for instance, {@code Ensembl:ENSMUSG00000038253}), 
         * the name (for instance, {@code Hoxa5}), the description and the species ID of this gene.
         * <p>
         * All of these parameters are optional, so they can be {@code null} when not used.
         * Other attributes are set to {@code null}.
         * 
         * @param geneId    A {@code String} that is the ID of this gene.
         * @param geneName  A {@code String} that is the name of this gene.
         * @param speciesId An {@code Integer} of the species which this gene belongs to.
         * @throws IllegalArgumentException If {@code id} is empty, .
         */
        public GeneTO(String geneId, String geneName, Integer speciesId) 
                throws IllegalArgumentException {
            this(geneId, geneName, null, speciesId, null, null, null);
        }

        /**
         * Constructor providing the ID (for instance, {@code Ensembl:ENSMUSG00000038253}), 
         * the name (for instance, {@code Hoxa5}), the description, the species ID, the BioType, 
         * the ID of the OMA Hierarchical Orthologous Group, whether this gene is present in 
         * Ensembl (see {@link #isEnsemblGene()}).  
         * <p>
         * All of these parameters are optional, so they can be {@code null} when not used.
         * 
         * @param geneId            A {@code String} that is the ID of this gene.
         * @param geneName          A {@code String} that is the name of this gene.
         * @param geneDescription   A {@code String} that is the description of this gene.
         * @param speciesId         An {@code Integer} that is the species ID which this 
         *                          gene belongs to.
         * @param geneBioTypeId     An {@code Integer} that is the BioType of this gene.
         * @param OMAParentNodeId   An {@code Integer} that is the ID of the OMA Hierarchical 
         *                          Orthologous Group.
         * @param ensemblGene       A {code Boolean} defining whether this gene is present 
         *                          in Ensembl.
         * @throws IllegalArgumentException If {@code id} is empty.
         */
        public GeneTO(String geneId, String geneName, String geneDescription, Integer speciesId,
                Integer geneBioTypeId, Integer OMAParentNodeId, Boolean ensemblGene) 
                        throws IllegalArgumentException {
            super(geneId, geneName, geneDescription);
            this.speciesId = speciesId;
            this.geneBioTypeId = geneBioTypeId;
            this.OMAParentNodeId = OMAParentNodeId;
            this.ensemblGene = ensemblGene;
        }

        /**
         * @return  The {@code String} that is the name of this gene (for instance, "Hoxa5").
         *          Corresponds to the DAO {@code Attribute} {@link GeneDAO.Attribute 
         *          NAME}. Returns {@code null} if value not set.
         */
        @Override
        public String getName() {
            //method overridden only to provide a more accurate javadoc
            return super.getName();
        }


        /**
         * @return  The species ID.
         */
        public Integer getSpeciesId() {
            return this.speciesId;
        }

        /**
         * @return The gene bio type ID (for instance, the ID for protein_coding).
         */
        public Integer getGeneBioTypeId() {
            return this.geneBioTypeId;
        }
        
        /**
         * @return  The OMA Hierarchical Orthologous Group ID that this gene belongs to.
         */
        public Integer getOMAParentNodeId() {
            return this.OMAParentNodeId;
        }
        
        /**
         * @return  The {@code Boolean} defining whether this gene is present in Ensembl.
         */
        public Boolean isEnsemblGene() {
            return this.ensemblGene;
        }

        @Override
        public String toString() {
            return "ID: " + this.getId() + " - Label: " + this.getName() + 
                   " - Species ID: " + this.getSpeciesId() + 
                   " - Gene bio type ID: " + this.getGeneBioTypeId() + 
                   " - OMA Hierarchical Orthologous Group ID: " + this.getOMAParentNodeId() + 
                   " - Is Ensembl Gene: " + this.isEnsemblGene();
        }
    }
}
