package org.bgee.model.dao.api.species;

import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.DAOResultSet;
import org.bgee.model.dao.api.EntityTO;

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
     * <li>{@code COMMONNAME}: corresponds to {@link SpeciesTO#getName()}.
     * <li>{@code GENUS}: corresponds to {@link SpeciesTO#getGenus()}.
     * <li>{@code SPECIESNAME}: corresponds to {@link SpeciesTO#getSpeciesName()}.
     * <li>{@code PARENTTAXONID}: corresponds to {@link SpeciesTO#getParentTaxonId()}.
     * </ul>
     * @see org.bgee.model.dao.api.DAO.setAttributesToGet(Collection)
     * @see org.bgee.model.dao.api.DAO.setAttributesToGet(Object[])
     * @see org.bgee.model.dao.api.DAO.clearAttributesToGet()
     */
    public enum Attribute implements DAO.Attribute {
        ID, COMMONNAME, GENUS, SPECIESNAME, PARENTTAXONID;
    }
    
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
         * {@link SpeciesDAO.Attribute SPECIESNAME}.
         */
        private final String speciesName;
        /**
         * A {@code String} that is the ID of the parent taxon of this species (for instance, 
         * {@code 9605} for <i>homo</i>, if this species was "human"). 
         * Corresponds to the DAO {@code Attribute} {@link SpeciesDAO.Attribute 
         * PARENTTAXONID}.
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
         * All of these parameters are optional except {@code id}, so they can be 
         * {@code null} when not used.
         * We do not use a {@code builder pattern}, because {@code TransferObject}s 
         * are not meant to be instantiated by clients, but only by the application, 
         * so we do not really care about having non-friendly constructors.
         * 
         * @param id            A {@code String} that is the ID.
         * @param commonName    A {@code String} that is the common name. 
         * @param genus         A {@code String} that is the genus of the species 
         *                      (for instance, <i>homo</i>).
         * @param speciesName   A {@code String} that is the species name of the species 
         *                      (for instance, <i>sapiens</i>).
         * @param parentTaxonId A {@code String} that is the NCBI ID of the parent taxon 
         *                      of this species (for instance, {@code 9605} for <i>homo</i>, 
         *                      the parent taxon of human).
         * @throws IllegalArgumentException If {@code id} is {@code null} or empty.
         */
        public SpeciesTO(String id, String commonName, String genus, String speciesName, 
                String parentTaxonId, String genomeFilePath, String genomeSpeciesId, 
                String fakeGeneIdPrefix) throws IllegalArgumentException {
            super(id, commonName);
            
            this.genus = genus;
            this.speciesName = speciesName;
            this.parentTaxonId = parentTaxonId;
            this.genomeFilePath = genomeFilePath;
            this.genomeSpeciesId = genomeSpeciesId;
            this.fakeGeneIdPrefix = fakeGeneIdPrefix;
        }
        
        /**
         * @return  The {@code String} that is the common name of this species. 
         *          Corresponds to the DAO {@code Attribute} {@link SpeciesDAO.Attribute 
         *          COMMONNAME}. Returns {@code null} if value not set.
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
         *          SPECIESNAME}. Returns {@code null} if value not set.
         */
        public String getSpeciesName() {
            return speciesName;
        }
        /**
         * @return  the {@code String} that is the ID of the parent taxon of this species 
         *          (for instance, {@code 9605} for <i>homo</i>, if this species was "human").
         *          Corresponds to the DAO {@code Attribute} {@link SpeciesDAO.Attribute 
         *          PARENTTAXONID}. Returns {@code null} if value not set.
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
                    this.getDescription() + " - Genome file path: " + 
                    this.getGenomeFilePath() + " - Genome species ID: " + 
                    this.getGenomeSpeciesId() + " - Fake gene ID prefix: " + 
                    this.getFakeGeneIdPrefix();
        }
    }
}
