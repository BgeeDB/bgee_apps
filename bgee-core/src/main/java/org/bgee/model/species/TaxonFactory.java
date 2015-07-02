package org.bgee.model.species;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import org.bgee.model.Factory;
import org.bgee.model.dao.api.species.TaxonDAO;

/**
 * A {@code Factory} to obtain {@link Taxon} objects.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public class TaxonFactory extends Factory {
    /**
     * A {@code TaxonDAO} allowing to query a data source for taxon-related data.
     */
    private final TaxonDAO dao;
    
    /**
     * Default constructor, it is recommended to use 
     * {@link org.bgee.model.Factory#getTaxonFactory()} instead.
     */
    public TaxonFactory() {
        this.dao = this.getDAOFactory().getTaxonDAO();
    }
    
    /**
     * Obtains from a {@code TaxonDAO} the {@code Taxon} that is the most recent 
     * ancestor common to all {@code Species} provided as argument.
     * 
     * @param species   A {@code Collection} of {@code Species} for which it is 
     *                  needed to identify the most recent ancestor which they all 
     *                  derived from.
     * @return          The {@code Taxon} that is the most recent ancestor which 
     *                  {@code species} all derived from.
     */
    public Taxon getMostRecentCommonTaxon(Collection<Species> species) {
        //TODO: auto-generated code
        return null;
        
    }
    
//    /**
//     * Obtains from a {@code TaxonDAO} all the {@code Taxon}s that are ancestors 
//     * of the specified {@code species}, meaning, the complete lineage of {@code Species}.
//     * 
//     * @param species   The {@code Species} for which we want the complete lineage.
//     * @return          A {@code Collection} of {@code Taxon}s that are all the ancestor 
//     *                  taxa of {@code species}.
//     * @see #getAncestorTaxa(Collection)
//     */
//    public Set<Taxon> getAncestorTaxa(Species species) {
//        return this.getAncestorTaxa(Collections.singleton(species));
//    }
//    /**
//     * Obtains from a {@code TaxonDAO} all the {@code Taxon}s that are ancestors 
//     * of the specified {@code species}, meaning, all the complete lineages of 
//     * all the {@code Species} in the argument.
//     * 
//     * @param species   A {@code Collection} of {@code Species} for which we want 
//     *                  the complete lineages.
//     * @return          A {@code Collection} of {@code Taxon}s that are all the ancestor 
//     *                  taxa of all {@code Species} in {@code species}.
//     * @see #getAncestorTaxa(Species)
//     */
//    public Set<Taxon> getAncestorTaxa(Collection<Species> species) {
//        
//    }
}
