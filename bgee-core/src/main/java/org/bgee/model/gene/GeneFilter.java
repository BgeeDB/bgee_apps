package org.bgee.model.gene;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A filter to parameterize queries using genes. 
 * 
 * @author  Frederic Bastian
 * @author  Valentine Rech de Laval
 * @version Bgee 13, Nov. 2015
 * @since   Bgee 13, Oct. 2015
 */
public class GeneFilter implements Predicate<Gene> {
    private final static Logger log = LogManager.getLogger(GeneFilter.class.getName());
    /**
     * @see #getGenesIds()
     */
    private final Set<String> geneIds;
    
    /**
     * Constructor allowing to set a {@code GeneFilter} for a single gene ID.
     * 
     * @param geneId    An {@code String} that is the ID of a gene that this {@code GeneFilter} 
     *                  will specify to use.
     * @throws IllegalArgumentException If {@code geneId} is blank.
     */
    public GeneFilter(String geneId) {
        this(Arrays.asList(geneId));
    }
    /**
     * Constructor allowing to set a {@code GeneFilter} for a collection of gene IDs.
     * 
     * @param geneIds   A {@code Collection} of {@code String}s that are the IDs of the genes 
     *                  that this {@code GeneFilter} will specify to use.
     * @throws IllegalArgumentException If any of the gene IDs provided is blank.
     */
    public GeneFilter(Collection<String> geneIds) throws IllegalArgumentException {
        if (geneIds != null && geneIds.stream().anyMatch(g -> g == null)) {
            throw log.throwing(new IllegalArgumentException("No gene ID can be blank."));
        }
        //for now, as geneIds is the only parameter, we throw an exception if null or empty
        if (geneIds == null || geneIds.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("At least one gene ID must be specified."));
        }
        this.geneIds = Collections.unmodifiableSet(
                geneIds == null? new HashSet<>(): new HashSet<>(geneIds));
    }

    /**
     * @return  An unmodifiable {@code Set} of {@code String}s that are the IDs of the genes 
     *          that this {@code GeneFilter} will specify to use.
     */
    public Set<String> getGeneIds() {
        return geneIds;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((geneIds == null) ? 0 : geneIds.hashCode());
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
        return true;
    }
    @Override
    public String toString() {
        return "GeneFilter [geneIds=" + geneIds + "]";
    }

    @Override
    public boolean test(Gene gene) {
        log.entry();
        // FIXME we should take into account the species because, since bgee 14, gene IDs are not uniques
        return log.exit(gene == null || geneIds.contains(gene.getEnsemblGeneId()));
    }
}
