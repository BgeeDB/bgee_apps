package org.bgee.model.dao.api.species;

import java.util.Set;

import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.DAOResultSet;
import org.bgee.model.dao.api.EntityTO;
import org.bgee.model.dao.api.exception.DAOException;

/**
 * DAO defining queries using or retrieving {@link SpeciesTO}s. 
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @see SpeciesTO
 * @since Bgee 01
 */
public interface SpeciesDAO extends DAO<SpeciesDAO.Attribute> {
    
    /**
     * {@code Enum} used to define the attributes to populate in the {@code SpeciesTO}s 
     * obtained from this {@code SpeciesDAO}.
     * <ul>
     * <li>{@code ID}: corresponds to {@link SpeciesTO#getId()}.
     * <li>{@code COMMON_NAME}: corresponds to {@link SpeciesTO#getName()}.
     * <li>{@code GENUS}: corresponds to {@link SpeciesTO#getGenus()}.
     * <li>{@code SPECIES_NAME}: corresponds to {@link SpeciesTO#getSpeciesName()}.
     * <li>{@code PARENT_TAXON_ID}: corresponds to {@link SpeciesTO#getParentTaxonId()}.
     * <li>{@code GENOME_FILE_PATH}: corresponds to {@link SpeciesTO#getGenomeFilePath()}.
     * <li>{@code GENOME_VERSION}: corresponds to {@link SpeciesTO#getGenomeVersion()}.
     * <li>{@code GENOME_SPECIES_ID}: corresponds to {@link SpeciesTO#getGenomeSpeciesId()}.
     * <li>{@code FAKE_GENE_ID_PREFIX}: corresponds to {@link SpeciesTO#getFakeGeneIdPrefix()}.
     * </ul>
     * @see org.bgee.model.dao.api.DAO#setAttributes(Collection)
     * @see org.bgee.model.dao.api.DAO#setAttributes(Enum[])
     * @see org.bgee.model.dao.api.DAO#clearAttributes()
     */
    public enum Attribute implements DAO.Attribute {
        ID("id"), COMMON_NAME("name"), GENUS("genus"), SPECIES_NAME("speciesName"), 
        PARENT_TAXON_ID("parentTaxonId"), GENOME_FILE_PATH("genomeFilePath"), 
        GENOME_SPECIES_ID("genomeSpeciesId"), FAKE_GENE_ID_PREFIX("fakeGeneIdPrefix"), 
        // TODO currently this info is not present in the table
        GENOME_VERSION("genomeVersion");

        /**
         * A {@code String} that is the corresponding field name in {@code RelationTO} class.
         * @see {@link Attribute#getTOFieldName()}
         */
        private final String fieldName;
        
        private Attribute(String fieldName) {
            this.fieldName = fieldName;
        }

        @Override
        public String getTOFieldName() {
            return this.fieldName;
        }
    }
    
    /**
     * Retrieve all species from data source.
     * <p>
     * The species are retrieved and returned as a {@code SpeciesTOResultSet}. 
     * It is the responsibility of the caller to close this {@code DAOResultSet} once 
     * results are retrieved.
     * 
     * @return A {@code SpeciesTOResultSet} containing all species from data source.
     * @throws DAOException If an error occurred when accessing the data source. 
     */
    public SpeciesTOResultSet getAllSpecies() throws DAOException;
    
    /**
     * Retrieve from the data source the species matching the provided IDs. 
     * If {@code speciesIds} is {@code null} or empty, this equivalent to calling 
     * {@link #getAllSpecies()}.
     * <p>
     * The species are retrieved and returned as a {@code SpeciesTOResultSet}. 
     * It is the responsibility of the caller to close this {@code DAOResultSet} once 
     * results are retrieved.
     * 
     * @param speciesIds    A {@code Set} of {@code String}s that are the NCBI IDs 
     *                      of the requested species (for instance, {@code 9606} for human).
     * @return A {@code SpeciesTOResultSet} containing all species from data source.
     * @throws DAOException If an error occurred when accessing the data source. 
     */
    public SpeciesTOResultSet getSpeciesByIds(Set<String> speciesIds) throws DAOException;

    /**
     * Retrieve all the species that are part of any data group.
     * @return A {@link org.bgee.model.dao.api.species.SpeciesDAO.SpeciesTOResultSet} containing all the species
     * that are part of at least one species data group.
     * @throws DAOException
     */
    public SpeciesTOResultSet getSpeciesFromDataGroups() throws DAOException;
    /**
     * {@code DAOResultSet} specifics to {@code SpeciesTO}s
     * 
     * @author Valentine Rech de Laval
     * @version Bgee 13
     * @since Bgee 13
     */
	public interface SpeciesTOResultSet extends DAOResultSet<SpeciesTO> {
		
	}

    /**
     * {@code EntityTO} representing a species in the Bgee data source.
     * 
     * @author Frederic Bastian
     * @version Bgee 13
     * @since Bgee 01
     */
    public final class SpeciesTO extends EntityTO {

    	private static final long serialVersionUID = 341628321446710146L;
    	/**
         * A {@code String} that is the genus of this species (for instance, <i>homo</i>).
         * Corresponds to the DAO {@code Attribute} {@link SpeciesDAO.Attribute GENUS}.
         */
        private final String genus;
        /**
         * A {@code String} that is the species name of this species (for instance, 
         * <i>sapiens</i>). Corresponds to the DAO {@code Attribute} 
         * {@link SpeciesDAO.Attribute SPECIES_NAME}.
         */
        private final String speciesName;
        /**
         * A {@code String} that is the ID of the parent taxon of this species (for instance, 
         * {@code 9605} for <i>homo</i>, if this species was "human"). 
         * Corresponds to the DAO {@code Attribute} {@link SpeciesDAO.Attribute 
         * PARENT_TAXON_ID}.
         */
        private final String parentTaxonId;
        
        /**
         * A {@code String} that is the path to retrieve the genome file we use 
         * for this species, from the GTF directory of the Ensembl FTP, without the Ensembl 
         * version suffix, nor the file type suffixes. For instance, for human, 
         * the GTF file in Ensembl 75 is stored at: 
         * {@code ftp://ftp.ensembl.org/pub/release-75/gtf/homo_sapiens/Homo_sapiens.GRCh37.75.gtf.gz}.
         * This attribute would then contain: {@code homo_sapiens/Homo_sapiens.GRCh37}
         * This attribute is needed because we use for some species the genome 
         * of another species (for instance, chimp genome for bonobo species).
         */
        private final String genomeFilePath;
        
        /**
         * A {@code String} that is the genome version we use for this species.
         * For instance, for human, this attribute would contain: {@code Homo_sapiens.GRCh37}.
         */
        private final String genomeVersion;

        /**
         * A {@code String} that is the ID of the species whose the genome was used 
         * for this species. This is used when a genome is not in Ensembl, but genome 
         * of a close species is. For instance, for bonobo (ID 9597), we use the chimp genome 
         * (ID 9598), because bonobo is not in Ensembl.
         */
        private final String genomeSpeciesId;
        
        /**
         * A {@code String} that is the prefix of gene IDs for this species, if its genome 
         * was not in Ensembl. This is because when another genome was used for a species 
         * in Bgee, we change the gene ID prefix (for instance, the chimp gene IDs, 
         * starting with 'ENSPTRG', will be changed to 'PPAG' when used for the bonobo).
         */
        private final String fakeGeneIdPrefix;
        
        /**
         * Constructor providing the ID, the common name, the genus, the species, and the ID 
         * of the parent taxon.
         * <p>
         * All of these parameters are optional, so they can be {@code null} when not used.
         * We do not use a {@code builder pattern}, because {@code TransferObject}s 
         * are not meant to be instantiated by clients, but only by the application, 
         * so we do not really care about having non-friendly constructors.
         * 
         * @param id                A {@code String} that is the ID.
         * @param commonName        A {@code String} that is the common name. 
         * @param genus             A {@code String} that is the genus of the species 
         *                          (for instance, <i>homo</i>).
         * @param speciesName       A {@code String} that is the species name of the species 
         *                          (for instance, <i>sapiens</i>).
         * @param parentTaxonId     A {@code String} that is the NCBI ID of the parent taxon 
         *                          of this species (for instance, {@code 9605} for <i>homo</i>, 
         *                          the parent taxon of human).
         * @param genomeFilePath    A {@code String} that is the path to retrieve the genome file 
         *                          we use for this species.
         * @param genomeVersion     A {@code String} that is the genome version 
         *                          we use for this species.
         * @param genomeSpeciesId   A {@code String} that is the ID of the species whose 
         *                          the genome was used for this species.
         * @param fakeGeneIdPrefix  A {@code String} that is the prefix of gene IDs for this
         *                          species, if its genome was not in Ensembl.
         */
        public SpeciesTO(String id, String commonName, String genus, String speciesName, 
                String parentTaxonId, String genomeFilePath, String genomeVersion, 
                String genomeSpeciesId, String fakeGeneIdPrefix) {
            super(id, commonName);
            
            this.genus = genus;
            this.speciesName = speciesName;
            this.parentTaxonId = parentTaxonId;
            this.genomeFilePath = genomeFilePath;
            this.genomeVersion = genomeVersion;
            this.genomeSpeciesId = genomeSpeciesId;
            this.fakeGeneIdPrefix = fakeGeneIdPrefix;
        }
        
        /**
         * @return  The {@code String} that is the common name of this species. 
         *          Corresponds to the DAO {@code Attribute} {@link SpeciesDAO.Attribute 
         *          COMMON_NAME}. Returns {@code null} if value not set.
         */
        @Override
        public String getName() {
            //method overridden only to provide a more accurate javadoc
            return super.getName();
        }
        /**
         * @return  the {@code String} that is the genus of this species 
         *          (for instance, <i>homo</i>).
         *          Corresponds to the DAO {@code Attribute} {@link SpeciesDAO.Attribute 
         *          GENUS}. Returns {@code null} if value not set.
         */
        public String getGenus() {
            return genus;
        }
        /**
         * @return  {@code String} that is the species name of this species 
         *          (for instance, <i>sapiens</i>).
         *          Corresponds to the DAO {@code Attribute} {@link SpeciesDAO.Attribute 
         *          SPECIES_NAME}. Returns {@code null} if value not set.
         */
        public String getSpeciesName() {
            return speciesName;
        }
        /**
         * @return  the {@code String} that is the ID of the parent taxon of this species 
         *          (for instance, {@code 9605} for <i>homo</i>, if this species was "human").
         *          Corresponds to the DAO {@code Attribute} {@link SpeciesDAO.Attribute 
         *          PARENT_TAXON_ID}. Returns {@code null} if value not set.
         */
        public String getParentTaxonId() {
            return parentTaxonId;
        }
        
        /**
         * @return  {@code String} that is the path to retrieve the genome file we use 
         *          for this species, from the GTF directory of the Ensembl FTP, 
         *          without the Ensembl version suffix, nor the file type suffixes.
         */
        public String getGenomeFilePath() {
            return genomeFilePath;
        }

        /**
         * @return  {@code String} that is the genome version we use for this species.
         */
        public String getGenomeVersion() {
            return genomeVersion;
        }

        /**
         * @return  A {@code String} that is the ID of the species whose the genome was used 
         *          for this species. This is used when a genome is not in Ensembl, 
         *          but genome of a close species is. 
         */
        public String getGenomeSpeciesId() {
            return genomeSpeciesId;
        }

        /**
         * @return  A {@code String} that is the prefix of gene IDs for this species, 
         *          if its genome was not in Ensembl. This is because when another genome 
         *          was used for a species in Bgee, we change the gene ID prefix 
         *          (for instance, the chimp gene IDs, starting with 'ENSPTRG', will be 
         *          changed to 'PPAG' when used for the bonobo).
         */
        public String getFakeGeneIdPrefix() {
            return fakeGeneIdPrefix;
        }

        @Override
        public String toString() {
            return "ID: " + this.getId() + " - Common name: " + this.getName() + 
                    " - Genus: " + this.getGenus() + " - Species name: " + this.getSpeciesName() + 
                    " - Parent taxon ID: " + this.getParentTaxonId() + " - Description: " + 
                    this.getDescription() + " - Genome file path: " + this.getGenomeFilePath() + 
                    " - Genome version: " + this.getGenomeFilePath() + " - Genome species ID: " + 
                    this.getGenomeSpeciesId() + " - Fake gene ID prefix: " + 
                    this.getFakeGeneIdPrefix();
        }
    }
}
