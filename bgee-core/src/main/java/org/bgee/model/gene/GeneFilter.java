package org.bgee.model.gene;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A filter to parameterize queries using genes. 
 * 
 * @author  Frederic Bastian
 * @author  Valentine Rech de Laval
 * @version Bgee 14 Mar. 2017
 * @since   Bgee 13, Oct. 2015
 */
public class GeneFilter implements Predicate<Gene> {
    private final static Logger log = LogManager.getLogger(GeneFilter.class.getName());
    /**
     * @see #getGenesIds()
     */
    private final Set<String> geneIds;
    /**
     * @see #getSpeciesId();
     */
    private final Integer speciesId;
    
    /**
     * Constructor allowing to set a {@code GeneFilter} for a given species ID.
     * The species ID is mandatory because in Bgee, the genome of a species can be used
     * for another closely-related species, thus an Ensembl gene ID can correspond to several genes.
     * For instance, in Bgee the chimpanzee genome is used for analyzing bonobo data.
     * 
     * @param geneId    An {@code String} that is the Ensembl ID of a gene that this {@code GeneFilter} 
     *                  will specify to use.
     * @param speciesId An {@code int} that is the ID of the species to target.
     * @throws IllegalArgumentException If {@code geneId} is blank or {@code speciesId} less than 1.
     */
    public GeneFilter(int speciesId) {
        this((Collection<String>) null, speciesId);
    }
    /**
     * Constructor allowing to set a {@code GeneFilter} for a single Ensembl gene ID and a species ID.
     * The species ID is mandatory because in Bgee, the genome of a species can be used
     * for another closely-related species, thus an Ensembl gene ID can correspond to several genes.
     * For instance, in Bgee the chimpanzee genome is used for analyzing bonobo data.
     * 
     * @param geneId    An {@code String} that is the Ensembl ID of a gene that this {@code GeneFilter} 
     *                  will specify to use.
     * @param speciesId An {@code int} that is the ID of the species to target.
     * @throws IllegalArgumentException If {@code geneId} is blank or {@code speciesId} is smaller than 1.
     */
    public GeneFilter(String geneId, int speciesId) {
        this(Collections.singleton(geneId), speciesId);
    }
    /**
     * Constructor allowing to set a {@code GeneFilter} for a collection of Ensembl gene IDs
     * and a species ID. The species ID is mandatory because in Bgee, the genome of a species
     * can be used for another closely-related species, thus an Ensembl gene ID can correspond
     * to several genes. For instance, in Bgee the chimpanzee genome is used for analyzing bonobo data.
     * 
     * @param geneIds   A {@code Collection} of {@code String}s that are the Ensembl IDs of the genes 
     *                  that this {@code GeneFilter} will specify to use. Can be {@code null} or empty.
     * @param speciesId An {@code int} that is the ID of the species to target.
     * @throws IllegalArgumentException If any of the gene IDs provided is blank,
     *                                  or if {@code speciesId} less than 1.
     */
    public GeneFilter(Collection<String> geneIds, int speciesId) throws IllegalArgumentException {
        if (speciesId < 1) {
            throw log.throwing(new IllegalArgumentException("A species ID cannot be smaler than 1."));
        }
        if (geneIds != null && geneIds.stream().anyMatch(g -> StringUtils.isBlank(g))) {
            throw log.throwing(new IllegalArgumentException("No gene ID can be blank."));
        }
        this.geneIds = Collections.unmodifiableSet(
                geneIds == null? new HashSet<>(): new HashSet<>(geneIds));
        this.speciesId = speciesId;
    }

    /**
     * @return  An unmodifiable {@code Set} of {@code String}s that are the IDs of the genes 
     *          that this {@code GeneFilter} will specify to use.
     */
    public Set<String> getEnsemblGeneIds() {
        return geneIds;
    }
    /**
     * @return  An {@code int} that is the ID of the species to target. The species ID
     *          is mandatory because in Bgee, the genome of a species can be used for another
     *          closely-related species, thus an Ensembl gene ID can correspond to several genes.
     *          For instance, in Bgee the chimpanzee genome is used for analyzing bonobo data.
     */
    public int getSpeciesId() {
        return speciesId;
    }

    @Override
    public boolean test(Gene gene) {
        log.entry();
        return log.exit(gene == null || speciesId.equals(gene.getSpecies().getId()) &&
                (geneIds == null || geneIds.isEmpty() || geneIds.contains(gene.getEnsemblGeneId())));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((geneIds == null) ? 0 : geneIds.hashCode());
        result = prime * result + ((speciesId == null) ? 0 : speciesId.hashCode());
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
        GeneFilter other = (GeneFilter) obj;
        if (geneIds == null) {
            if (other.geneIds != null) {
                return false;
            }
        } else if (!geneIds.equals(other.geneIds)) {
            return false;
        }
        if (speciesId == null) {
            if (other.speciesId != null) {
                return false;
            }
        } else if (!speciesId.equals(other.speciesId)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("GeneFilter [speciesId=").append(speciesId)
               .append(", geneIds=").append(geneIds).append("]");
        return builder.toString();
    }
}
