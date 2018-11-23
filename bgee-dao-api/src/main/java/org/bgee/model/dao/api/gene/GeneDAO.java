package org.bgee.model.dao.api.gene;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.DAOResultSet;
import org.bgee.model.dao.api.NamedEntityTO;
import org.bgee.model.dao.api.exception.DAOException;

/**
 * DAO defining queries using or retrieving {@link GeneTO}s. 
 * 
 * @author Valentine Rech de Laval
 * @author Philippe Moret
 * @author Frederic Bastian
 * @version Bgee 14 Sep. 2018
 * @see GeneTO
 * @see GeneBioTypeTO
 * @since Bgee 13
 */
public interface GeneDAO extends DAO<GeneDAO.Attribute> {
    /**
     * {@code Enum} used to define the attributes to populate in the {@code GeneTO}s 
     * obtained from this {@code GeneDAO}.
     * <ul>
     * <li>{@code ID}: corresponds to {@link GeneTO#getId()}.
     * <li>{@code ENSEMBL_ID}: corresponds to {@link GeneTO#getGeneId()}.
     * <li>{@code NAME}: corresponds to {@link GeneTO#getName()}.
     * <li>{@code DESCRIPTION}: corresponds to {@link GeneTO#getDescription()}.
     * <li>{@code SPECIES_ID}: corresponds to {@link GeneTO#getSpeciesId()}.
     * <li>{@code GENE_BIO_TYPE_ID}: corresponds to {@link GeneTO#getGeneBioTypeId()}.
     * <li>{@code OMA_PARENT_NODE_ID}: corresponds to {@link GeneTO#getOMAParentNodeId()}.
     * <li>{@code ENSEMBL_GENE}: corresponds to {@link GeneTO#isEnsemblGene()}.
     * <li>{@code GENE_MAPPED_TO_SAME_GENE_ID_COUNT}: corresponds to {@link GeneTO#getGeneMappedToGeneIdCount()}.
     * </ul>
     * @see org.bgee.model.dao.api.DAO#setAttributes(Collection)
     * @see org.bgee.model.dao.api.DAO#setAttributes(Enum[])
     * @see org.bgee.model.dao.api.DAO#clearAttributes()
     */
    public enum Attribute implements DAO.Attribute {
        ID, ENSEMBL_ID, NAME, DESCRIPTION, SPECIES_ID, GENE_BIO_TYPE_ID, OMA_PARENT_NODE_ID,
        ENSEMBL_GENE, GENE_MAPPED_TO_SAME_GENE_ID_COUNT;
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
     * Retrieve genes by their ensembl ids.
     * @param geneIds A {Collection} of gene ids.
     * @return  A {@code GeneTOResultSet} containing genes found from the data source.
     * @throws DAOException
     */
    public GeneTOResultSet getGenesByEnsemblGeneIds(Collection<String> geneIds) throws DAOException;
    
    /**
     * Retrieve genes by their bgee gene ids.
     * @param geneIds A {Collection} of gene ids.
     * @return  A {@code GeneTOResultSet} containing genes found from the data source.
     * @throws DAOException
     */
    public GeneTOResultSet getGenesByIds(Collection<Integer> geneIds) throws DAOException;
    
    /**
     * Retrieves genes from data source according to a {@code Collection} of {@code Integer}s
     * that are the IDs of species allowing to filter the genes to use.
     * <p>
     * The genes are retrieved and returned as a {@code GeneTOResultSet}. It is the
     * responsibility of the caller to close this {@code DAOResultSet} once results are retrieved.
     * 
     * @param speciesIds    A {@code Collection} of {@code Integer}s that are the IDs of species 
     *                      allowing to filter the genes to use.
     * @return              An {@code GeneTOResultSet} containing all genes from data source.
     * @throws DAOException If an error occurred when accessing the data source. 
     */
    public GeneTOResultSet getGenesBySpeciesIds(Collection<Integer> speciesIds) throws DAOException;
    /**
     * Retrieves genes with expression data for the requested species IDs. If no species IDs
     * provided, retrieve data for all species.
     * <p>
     * The genes are retrieved and returned as a {@code GeneTOResultSet}. It is the
     * responsibility of the caller to close this {@code DAOResultSet} once results are retrieved.
     * 
     * @param speciesIds    A {@code Collection} of {@code Integer}s that are the IDs of species 
     *                      to retrieve genes for. Can be {@code null} or empty.
     * @return              An {@code GeneTOResultSet} containing all genes from data source.
     * @throws DAOException If an error occurred when accessing the data source. 
     */
    public GeneTOResultSet getGenesWithDataBySpeciesIds(Collection<Integer> speciesIds)
            throws DAOException;

    /**
     * Retrieves genes from data source according to a {@code Collection} of {@code Integer}s
     * that are the IDs of species allowing to filter the genes to use.
     * <p>
     * The genes are retrieved and returned as a {@code GeneTOResultSet}. It is the
     * responsibility of the caller to close this {@code DAOResultSet} once results are retrieved.
     * 
     * @param speciesIdToGeneIds    A {@code Map} where keys are {@code Integer}s that are
     *                              species IDs, the associated value being a {@code Set}
     *                              of {@code String}s that are the Ensembl IDs of the genes
     *                              to retrieve in the associated species.
     * @return                      A {@code GeneTOResultSet} containing matching genes from data source.
     * @throws DAOException If an error occurred when accessing the data source. 
     */
    public GeneTOResultSet getGenesBySpeciesAndGeneIds(Map<Integer, Set<String>> speciesIdToGeneIds) 
            throws DAOException;
    
    /**
     * Returns genes according to a search term, matching gene name, id or one synonym.
     * 
     * @param searchTerm    A {@code String} containing the term to be searched.
     * @param speciesIds    A {@code Collection} of {@code Integer}s that are species Ids
     *                      (may be empty to search on all species).
     * @param limitStart    An {@code int} representing the index of the first element to return.
     * @param resultPerPage An {@code int} representing the number of elements to return
     * 
     * @return A @{code {@link GeneTOResultSet} encapsulating the results.
     */
    public GeneTOResultSet getGeneBySearchTerm(String searchTerm, Collection<Integer> speciesIds,
            int limitStart, int resultPerPage);

    /**
     * Retrieves the gene biotypes used in Bgee
     * <p>
     * The biotypes are retrieved and returned as a {@code GeneBioTypeTOResultSet}. It is the
     * responsibility of the caller to close this {@code DAOResultSet} once results are retrieved.
     *
     * @return              A {@code GeneBioTypeTOResultSet} containing all biotypes from data source.
     * @throws DAOException If an error occurred while accessing the data source. 
     */
    public GeneBioTypeTOResultSet getGeneBioTypes();

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
     * @throws IllegalArgumentException If {@code genes} is empty or null or if 
     *                                  {@code attributesToUpdate} is empty or null or contains 
     *                                  an ancestral OMA node ID or an ancestral OMA taxon ID.   
     */
    public int updateGenes(Collection<GeneTO> genes, Collection<GeneDAO.Attribute> attributesToUpdate) 
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
     * {@code NamedEntityTO} representing a gene in the Bgee database.
     * 
     * @author Valentine Rech de Laval
     * @version Bgee 13
     * @since Bgee 13
     */
    public class GeneTO extends NamedEntityTO<Integer> {

        private static final long serialVersionUID = -9011956802137411474L;

        /**
         * A {@code String} that is the ID of this gene in the Ensembl database.
         */
        private final String geneId;

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
         * @see #getGeneMappedToIdCount()
         */
        private final Integer geneMappedToGeneIdCount;

        /**
         * Constructor providing the ID (for instance, {@code Ensembl:ENSMUSG00000038253}), 
         * the name (for instance, {@code Hoxa5}), the description and the species ID of this gene.
         * <p>
         * All of these parameters are optional, so they can be {@code null} when not used.
         * Other attributes are set to {@code null}.
         * 
         * @param bgeeGeneId    An {@code Integer} that is the ID of this gene.
         * @param geneId        A {@code String} that is the ID of this gene in the Ensembl database.
         * @param geneName  A {@code String} that is the name of this gene.
         * @param speciesId An {@code Integer} of the species which this gene belongs to.
         */
        public GeneTO(Integer bgeeGeneId, String geneId, String geneName, Integer speciesId) {
            this(bgeeGeneId, geneId, geneName, null, speciesId, null, null, null, null);
        }

        /**
         * Constructor providing the Bgee gene ID, the Ensembl ID (for instance, {@code Ensembl:ENSMUSG00000038253}), 
         * the name (for instance, {@code Hoxa5}), the description, the species ID, the BioType, 
         * the ID of the OMA Hierarchical Orthologous Group, whether this gene is present in 
         * Ensembl (see {@link #isEnsemblGene()}).  
         * <p>
         * All of these parameters are optional, so they can be {@code null} when not used.
         * 
         * @param bgeeGeneId                An {@code Integer} that is the ID of this gene.
         * @param geneId                    A {@code String} that is the ID of this gene in the Ensembl database.
         * @param geneName                  A {@code String} that is the name of this gene.
         * @param geneDescription           A {@code String} that is the description of this gene.
         * @param speciesId                 An {@code Integer} that is the species ID which this 
         *                                  gene belongs to.
         * @param geneBioTypeId             An {@code Integer} that is the BioType of this gene.
         * @param OMAParentNodeId           An {@code Integer} that is the ID of the OMA Hierarchical 
         *                                  Orthologous Group.
         * @param ensemblGene               A {code Boolean} defining whether this gene is present 
         *                                  in Ensembl.
         * @param geneMappedToGeneIdCount   An {@code Integer} that is the number of genes
         *                                  in the Bgee database with the same Ensembl gene ID.
         */
        public GeneTO(Integer bgeeGeneId, String geneId, String geneName, String geneDescription, 
                Integer speciesId, Integer geneBioTypeId, Integer OMAParentNodeId, Boolean ensemblGene,
                Integer geneMappedToGeneIdCount) {
            super(bgeeGeneId, geneName, geneDescription);
            this.geneId = geneId;
            this.speciesId = speciesId;
            this.geneBioTypeId = geneBioTypeId;
            this.OMAParentNodeId = OMAParentNodeId;
            this.ensemblGene = ensemblGene;
            this.geneMappedToGeneIdCount = geneMappedToGeneIdCount;
        }

        /**
         * @return  A {@code String} that is the Ensembl gene ID.
         */
        public String getGeneId() {
            return this.geneId;
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
        /**
         * @return  An {@code Integer} that is the number of genes in the Bgee database
         *          with the same Ensembl gene ID. In Bgee, for some species with no genome available,
         *          we use the genome of a closely-related species, such as chimpanzee genome
         *          for analyzing bonobo data. For this reason, a same Ensembl gene ID
         *          can be mapped to several species in Bgee. The value returned here is equal to 1
         *          when the Ensembl gene ID is uniquely used in the Bgee database.
         */
        public Integer getGeneMappedToGeneIdCount() {
            return this.geneMappedToGeneIdCount;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("GeneTO [geneId=").append(geneId)
                   .append(", speciesId=").append(speciesId)
                   .append(", geneBioTypeId=").append(geneBioTypeId)
                   .append(", OMAParentNodeId=").append(OMAParentNodeId)
                   .append(", ensemblGene=").append(ensemblGene)
                   .append(", geneMappedToGeneIdCount=").append(geneMappedToGeneIdCount).append("]");
            return builder.toString();
        }
    }

    /**
     * {@code DAOResultSet} specifics to {@code GeneBioTypeTO}s
     * 
     * @author Frederic Bastian
     * @version Bgee 14 Sep. 2018
     * @since Bgee 14 Sep. 2018
     */
    public interface GeneBioTypeTOResultSet extends DAOResultSet<GeneBioTypeTO> {
        
    }

    /**
     * {@code NamedEntityTO} representing a bio type in the Bgee database.
     * 
     * @author Frederic Bastian
     * @version Bgee 14 Sep. 2018
     * @since Bgee 14 Sep. 2018
     */
    public class GeneBioTypeTO extends NamedEntityTO<Integer> {
        private static final long serialVersionUID = 1691071746394023190L;

        public GeneBioTypeTO(Integer id, String name) {
            super(id, name, null);
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("GeneBioTypeTO [geneBioTypeId=").append(this.getId())
                   .append(", geneBioTypeName=").append(this.getName())
                   .append("]");
            return builder.toString();
        }
    }
}
