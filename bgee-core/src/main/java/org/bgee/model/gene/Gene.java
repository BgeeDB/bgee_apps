package org.bgee.model.gene;

import java.util.Comparator;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.species.Species;

/**
 * Class allowing to describe genes. The unique identifier for {@code Gene}s
 * are composed of the Ensembl gene ID (see {@link #getEnsemblGeneId()}) and of the species ID
 * (see {@link #getSpecies()} and {@link org.bgee.model.species.Species#getId() Species.getId()}).
 * This is because Ensembl gene IDs are not unique in Bgee, as we sometimes used the genome
 * of a closely-related species for species with no genome available.
 * 
 * @author  Frederic Bastian
 * @author  Valentine Rech de Laval
 * @version Bgee 14 Mar. 2017
 * @since   Bgee 01
 */
//Note: this class does not extend NamedEntity, because we don't want to expose
//the internal Bgee gene IDs, and because Ensembl gene IDs are not unique in Bgee,
//as we sometimes use genomes of closely-related species.
public class Gene {
    private final static Logger log = LogManager.getLogger(Gene.class.getName());

    /**
     * A {@code Comparator} for {@code Gene}s. Sort {@code Gene}s based on their species ID first
     * ({@code null} species ID sorted last), then comparing their Ensembl gene ID ({@code null} values sorted last).
     */
    public static Comparator<Gene> COMPARATOR = Comparator
            .<Gene, Integer>comparing(g -> g.getSpecies().getId(), Comparator.nullsLast(Comparator.naturalOrder()))
            .thenComparing(g -> g.getEnsemblGeneId(), Comparator.nullsLast(Comparator.naturalOrder()));
    /**
     * A {@code String} that is the Ensembl gene ID.
     */
    private final String ensemblGeneId;
    
    /**
     * A {@code String} that is the name of the gene.
     */
    private final String name;

    /**
     * A {@code String} that is the description of the gene.
     */
    private final String description;

	/**
	 * The {@code Species} this {@code Gene} belongs to.
	 */
	private final Species species;

    /**
     * The {@code GeneBioType} of this {@code Gene}.
     */
    private final GeneBioType geneBioType;
	
	/**
	 * @see #getGeneMappedToSameEnsemblGeneIdCount()
	 */
	private final int geneMappedToSameEnsemblGeneIdCount;
    
    /**
     * Constructor providing the {@code ensemblGeneId} and the {@code Species} of this {@code Gene}.
     * <p>  
     * These {@code ensemblGeneId} and {@code species} cannot be {@code null}, or blank,
     * otherwise an {@code IllegalArgumentException} will be thrown.
     *
     * @param ensemblGeneId A {@code String} representing the ID of this object.
     * @param species       A {@code Species} representing the species this gene belongs to.
     * @param geneBioType   The {@code GeneBioType} of this {@code Gene}.
     * @throws IllegalArgumentException     if {@code ensemblGeneId} is blank,
     *                                      or {@code Species} is {@code null}.
     */
    public Gene(String ensemblGeneId, Species species, GeneBioType geneBioType) throws IllegalArgumentException {
        this(ensemblGeneId, null, null, species, geneBioType, 1);
    }
    /**
     * Constructor providing the {@code ensemblGeneId}, the name, the description,
     * and the {@code Species} of this {@code Gene}.  
     * <p>
     * These {@code ensemblGeneId} and {@code species} cannot be {@code null}, or blank,
     * otherwise an {@code IllegalArgumentException} will be thrown.
     * 
     * @param ensemblGeneId                         A {@code String} representing the Ensembl ID
     *                                              of this {@code Gene}.
     * @param name                                  A {@code String} representing the name of this gene.
     * @param description                           A {@code String} representing the description
     *                                              of this gene.
     * @param species                               A {@code Species} representing the species
     *                                              this gene belongs to.
     * @param geneBioType                           The {@code GeneBioType} of this {@code Gene}.
     * @param geneMappedToSameEnsemblGeneIdCount    An {@code Integer} that is the number of genes
     *                                              in the Bgee database with the same Ensembl gene ID.
     *                                              See {@link #getGeneMappedToSameEnsemblGeneIdCount()}
     *                                              for more details.
     * @throws IllegalArgumentException     if {@code ensemblGeneId} is blank,
     *                                      or {@code Species} is {@code null}.
     */
    public Gene(String ensemblGeneId, String name, String description, Species species, GeneBioType geneBioType,
            int geneMappedToSameEnsemblGeneIdCount)
        throws IllegalArgumentException {
        if (StringUtils.isBlank(ensemblGeneId)) {
            throw log.throwing(new IllegalArgumentException("The Ensembl gene ID must be provided."));
        }
        if (species == null) {
            throw log.throwing(new IllegalArgumentException("The Species must be provided."));
        }
        if (geneBioType == null) {
            throw log.throwing(new IllegalArgumentException("The GeneBioType must be provided."));
        }
        if (geneMappedToSameEnsemblGeneIdCount < 1) {
            throw log.throwing(new IllegalArgumentException(
                    "Each gene has at least one match with same Ensembl ID: itself."));
        }
        this.ensemblGeneId = ensemblGeneId;
        this.name = name;
        this.description = description;
        this.species = species;
        this.geneBioType = geneBioType;
        this.geneMappedToSameEnsemblGeneIdCount = geneMappedToSameEnsemblGeneIdCount;
    }
    
	/**
	 * @return The {@code String} that is the Ensembl gene ID.
	 */
	public String getEnsemblGeneId() {
        return ensemblGeneId;
    }
    /**
     * @return  The {@code String} that is the name of the gene.
     */
    public String getName() {
        return name;
    }
    /**
     * @return  The {@code String} that is the description of the gene.
     */
    public String getDescription() {
        return description;
    }
    /**
	 * @return The {@code Species} this {@code Gene} belongs to.
	 */
	public Species getSpecies() {
		return this.species;
	}
    /**
     * @return  The {@code GeneBioType} of this {@code Gene}
     * @see <a target='_top' href='http://vega.archive.ensembl.org/info/about/gene_and_transcript_types.html'>http://vega.archive.ensembl.org/info/about/gene_and_transcript_types.html</a>
     */
    public GeneBioType getGeneBioType() {
        return this.geneBioType;
    }
	/**
	 * @return  An {@code Integer} that is the number of genes in the Bgee database
     *          with the same Ensembl gene ID. In Bgee, for some species with no genome available,
     *          we use the genome of a closely-related species, such as chimpanzee genome
     *          for analyzing bonobo data. For this reason, a same Ensembl gene ID
     *          can be mapped to several species in Bgee. The value returned here is equal to 1
     *          when the Ensembl gene ID is uniquely used in the Bgee database.
	 */
	public int getGeneMappedToSameEnsemblGeneIdCount() {
	    return this.geneMappedToSameEnsemblGeneIdCount;
	}

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((ensemblGeneId == null) ? 0 : ensemblGeneId.hashCode());
        result = prime * result + ((species == null) ? 0 : species.hashCode());
        result = prime * result + ((geneBioType == null) ? 0 : geneBioType.hashCode());
        return result;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Gene other = (Gene) obj;
        if (ensemblGeneId == null) {
            if (other.ensemblGeneId != null) {
                return false;
            }
        } else if (!ensemblGeneId.equals(other.ensemblGeneId)) {
            return false;
        }
        if (species == null) {
            if (other.species != null) {
                return false;
            }
        } else if (!species.equals(other.species)) {
            return false;
        }
        if (geneBioType == null) {
            if (other.geneBioType != null) {
                return false;
            }
        } else if (!geneBioType.equals(other.geneBioType)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Gene [ensemblGeneId=").append(ensemblGeneId)
               .append(", name=").append(name)
               .append(", description=").append(description)
               .append(", species=").append(species)
               .append(", geneBioType=").append(geneBioType)
               .append(", geneMappedToSameEnsemblGeneIdCount=").append(geneMappedToSameEnsemblGeneIdCount).append("]");
        return builder.toString();
    }
}
