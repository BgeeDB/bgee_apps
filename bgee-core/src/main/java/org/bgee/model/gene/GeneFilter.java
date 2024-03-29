package org.bgee.model.gene;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
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
 * @version Bgee 15, Sep. 2022
 * @since   Bgee 13, Oct. 2015
 */
public class GeneFilter implements Predicate<Gene> {
    private final static Logger log = LogManager.getLogger(GeneFilter.class.getName());
    /**
     * @see #getGeneIds() 
     */
    private final Set<String> geneIds;
    /**
     * @see #getSpeciesId()
     */
    private final int speciesId;
    
    /**
     * Constructor allowing to set a {@code GeneFilter} for a given species ID.
     * The species ID is mandatory because in Bgee, the genome of a species can be used
     * for another closely-related species, thus a gene ID can correspond to several genes.
     * For instance, at some point in Bgee the chimpanzee genome was used for analyzing bonobo data.
     * 
     * @param speciesId An {@code int} that is the ID of the species to target.
     * @throws IllegalArgumentException If {@code geneId} is blank or {@code speciesId} less than 1.
     */
    public GeneFilter(int speciesId) {
        this(speciesId, (Collection<String>) null);
    }
    /**
     * Constructor allowing to set a {@code GeneFilter} for a single gene ID and a species ID.
     * The species ID is mandatory because in Bgee, the genome of a species can be used
     * for another closely-related species, thus a gene ID can correspond to several genes.
     * For instance, at some point in Bgee the chimpanzee genome was used for analyzing bonobo data.
     * 
     * @param speciesId An {@code int} that is the ID of the species to target.
     * @param geneId    An {@code String} that is the ID of a gene that this {@code GeneFilter} 
     *                  will specify to use.
     * @throws IllegalArgumentException If {@code geneId} is blank or {@code speciesId} is smaller than 1.
     */
    public GeneFilter(int speciesId, String geneId) {
        this(speciesId, Collections.singleton(geneId));
    }
    /**
     * Constructor allowing to set a {@code GeneFilter} for a collection of gene IDs
     * and a species ID. The species ID is mandatory because in Bgee, the genome of a species
     * can be used for another closely-related species, thus a gene ID can correspond
     * to several genes. For instance, at some point in Bgee the chimpanzee genome was used for analyzing bonobo data.
     * 
     * @param speciesId An {@code int} that is the ID of the species to target.
     * @param geneIds   A {@code Collection} of {@code String}s that are the IDs of the genes 
     *                  that this {@code GeneFilter} will specify to use. Can be {@code null} or empty.
     * @throws IllegalArgumentException If any of the gene IDs provided is blank,
     *                                  or if {@code speciesId} less than 1.
     */
    public GeneFilter(int speciesId, Collection<String> geneIds) throws IllegalArgumentException {
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
    public Set<String> getGeneIds() {
        return geneIds;
    }
    /**
     * @return  An {@code int} that is the ID of the species to target. The species ID
     *          is mandatory because in Bgee, the genome of a species can be used for another
     *          closely-related species, thus a gene ID can correspond to several genes.
     *          For instance, at some point in Bgee the chimpanzee genome was used for analyzing bonobo data.
     */
    public int getSpeciesId() {
        return speciesId;
    }

    @Override
    public boolean test(Gene gene) {
        log.traceEntry();
        if (gene == null) {
            throw log.throwing(new IllegalArgumentException("Cannot test null"));
        }
        return log.traceExit(gene.getSpecies().getId().equals(speciesId) &&
                (geneIds == null || geneIds.isEmpty() || geneIds.contains(gene.getGeneId())));
    }

    @Override
    public int hashCode() {
        return Objects.hash(geneIds, speciesId);
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        GeneFilter other = (GeneFilter) obj;
        return Objects.equals(geneIds, other.geneIds) && speciesId == other.speciesId;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("GeneFilter [speciesId=").append(speciesId)
               .append(", geneIds=").append(geneIds).append("]");
        return builder.toString();
    }
}
