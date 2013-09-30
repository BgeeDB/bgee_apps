package org.bgee.model.anatdev.evomapping;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.bgee.model.anatdev.evomapping.EvoMappingSelector.TransRelationType.TaxonBasedRelationType;
import org.bgee.model.species.Taxon;

/**
 * An {@link EvoMappingSelector} that provides additional methods specific for 
 * retrieving mappings based on relations of the type 
 * {@link EvoMappingSelector.TransRelationType.TaxonBasedRelationType}. Note that 
 * the methods provided by {@code EvoMappingSelector} can also be used to retrieve 
 * mappings based on {@code TaxonBasedRelationType} relations, it is not mandatory 
 * to use this {@code TaxonEvoMappingSelector} to retrieve them.
 * <p>
 * The additional methods allow to restrict the scope of the relations by using 
 * {@code Taxon} objects (see {@link #addTaxon(Taxon)} and {@link #addAllTaxa(Collection)}). 
 * {@link AnatDevMapping}s retrieved will be based on relations defined at the level 
 * of these taxa, and, by default, at the level of any of their ancestor taxa as well. 
 * This default behavior can be modified, see {@link #setUseAncestralTaxa(boolean)}. 
 * If {@code Taxon}s are provided, it is still possible to provide {@code Species} 
 * by using the {@code EvoMappingSelector} methods, so that {@link AnatDevMapping}s 
 * retrieved will contain only {@code AnatDevEntity}s existing in at least one 
 * of them (see {@link EvoMappingSelector#addSpecies(Species)} and {@link 
 * EvoMappingSelector#addAllSpecies(Collection)}). But they will not be used 
 * to identify the relations, as it would be the case by using solely an 
 * {@code EvoMappingSelector}.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public class TaxonEvoMappingSelector extends EvoMappingSelector {
    /**
     * A {@code Set} of {@code Taxon}s used to retrieve {@link AnatDevMapping}s,  
     * based on relations defined at the level of any of them. For instance, if 
     * {@link #evoRelationType} is {@code HOMOLOGY}, it means that the 
     * {@link AnatDevMapping}s will contain {@code AnatDevEntity}s derived 
     * from ancestral structures that first appeared in one of those {@code Taxon}s. 
     * <p>
     * If {@link #useAncestralTaxa} is {@code true}, then the relations defined for 
     * any of the ancestral taxa of these {@code Taxon}s will also be considered; 
     * in our previous example, it means that the {@link AnatDevMapping}s will 
     * represent ancestral structures that were <strong>existing</code> in these 
     * {@code Taxon}s, meaning, that first appeared in these {@code Taxon}s, or one 
     * of their ancestral taxa. Default value is {@code true}.
     * <p>
     * If {@link #species} is not empty, then the {@link AnatDevMapping}s retrieved 
     * will only contain {@code AnatDevEntity}s existing in at least on of these 
     * {@code Species}.
     * 
     * @see #useAncestralTaxa
     * @see #species
     */
    private final Set<Taxon> taxa;
    /**
     * A {@code boolean} defining whether ancestral taxa of {@link #taxa} 
     * should also be considered. If {@code true}, it will lead to also select 
     * {@link AnatDevMapping}s based on relations holding for taxa ancestors of 
     * these {@code Taxon}s (relations of the type defined by {@link #evoRelationType} 
     * in any case). 
     * <p>
     * If an {@code AnatDevEntity} is related to different structures at different 
     * taxonomic levels (for instance, a structure with its different states of evolution 
     * leading to different homology relations to different species), so that 
     * it would be part of several {@code AnatDevMapping}s, only the relation 
     * holding for the oldest ancestor will be considered. A same {@code AnatDevEntity} 
     * will never be part of several {@code AnatDevMapping}s, as defined by a same 
     * {@code EvoMappingSelector}.
     * <p>
     * Default is {@code true}.
     * 
     * @see #taxa
     */
    private boolean useAncestralTaxa;
    
    /**
     * Constructor providing the {@code TaxonBasedRelationType} defining what 
     * evolutionary relation the {@link AnatDevMapping}s should be based on, 
     * and the {@code Taxon}s which the relations should be defined for.
     * <p>
     * By default, this will also retrieved relations defined at the level of 
     * their ancestral taxa, see {@link #setIsUseAncestralTaxa(boolean)}. 
     * 
     * @param relationType  The {@code TaxonBasedRelationType} that is the type of 
     *                      the relation which the {@link AnatDevMapping}s should be 
     *                      based on.
     * @param taxa          A {@code Collection} of {@code Taxon}s specifying for which 
     *                      taxa the relations should be defined for.
     */
    public TaxonEvoMappingSelector(TaxonBasedRelationType relationType, 
            Collection<Taxon> taxa) {
        super(relationType);
        this.taxa = new HashSet<Taxon>();
        this.taxa.addAll(taxa);
        this.setUseAncestralTaxa(true);
    }

    //**************************************
    // GETTERS/SETTERS
    //**************************************
    @Override
    public TaxonBasedRelationType getEvoRelationType() {
        return (TaxonBasedRelationType) super.getEvoRelationType();
    }

    /**
     * Returns the {@code Taxon}s for which the {@code TaxonBasedTransRelationType} 
     * returned by {@link #getEvoRelationType()} should hold, in order to build the {@link 
     * AnatDevMapping}s.
     * <p>
     * For instance, if {@link #getEvoRelationType()} returns {@code HOMOLOGY}, 
     * it means that the {@link AnatDevMapping}s will contain {@code AnatDevEntity}s 
     * derived from ancestral structures that first appeared in one of those {@code Taxon}s. 
     * <p>
     * If {@link #isUseAncestralTaxa()} returns {@code true}, then the relations 
     * defined for any of the ancestral taxa of these {@code Taxon}s will also be 
     * considered; in our previous example, it means that the {@link AnatDevMapping}s 
     * will represent ancestral structures that were <strong>existing</code> in these 
     * {@code Taxon}s, meaning, that first appeared in these {@code Taxon}s, or in one 
     * of their ancestral taxa. Default value is {@code true}.
     * <p>
     * If the {@code Collection} of {@code Species} returned by {@link 
     * EvoMappingSelector#getSpecies()} is not empty, then the {@link AnatDevMapping}s 
     * retrieved will only contain {@code AnatDevEntity}s existing in at least one 
     * of these {@code Species}.
     * 
     * @return  the {@code Collection} of {@code Taxon}s for which the 
     *          {@code TraxonBasedRelationType} returned by {@link #getEvoRelationType()} 
     *          should hold, in order to build the {@link AnatDevMapping}s.
     * @see #isUseAncestralTaxa()
     * @see EvoMappingSelector#getSpecies()
     */
    public Set<Taxon> getTaxa() {
        return this.taxa;
    }
    
    /**
     * A {@code boolean} defining whether ancestral taxa of the {@code Taxon}s 
     * returned by {@link #getTaxa()} should also be considered. If {@code true}, 
     * it will lead to also select {@link AnatDevMapping}s based on relations 
     * holding for taxa ancestors of these {@code Taxon}s (relations of the type 
     * defined by {@link #getEvoRelationType()} in any case). 
     * <p>
     * If an {@code AnatDevEntity} is related to different structures at different 
     * taxonomic levels (for instance, a structure with its different states of evolution 
     * leading to different homology relations to different species), so that 
     * it would be part of several {@code AnatDevMapping}s, only the relation 
     * holding for the oldest ancestor will be considered. A same {@code AnatDevEntity} 
     * will never be part of several {@code AnatDevMapping}s, as defined by a same 
     * {@code EvoMappingSelector}.
     * <p>
     * Default value is {@code true}.
     * 
     * @return  A {@code boolean} defining whether ancestral taxa of the {@code Taxon}s 
     *          returned by {@link #getTaxa()} should also be considered.
     * @see #getTaxa()
     */
    public boolean isUseAncestralTaxa() {
        return useAncestralTaxa;
    }
    /**
     * Sets the {@code boolean} defining whether ancestral taxa of the {@code Taxon}s 
     * returned by {@link #getTaxa()} should also be considered. If {@code true}, 
     * it will lead to also select {@link AnatDevMapping}s based on relations 
     * holding for taxa ancestors of these {@code Taxon}s (relations of the type 
     * defined by {@link #getEvoRelationType()} in any case). 
     * <p>
     * If an {@code AnatDevEntity} is related to different structures at different 
     * taxonomic levels (for instance, a structure with its different states of evolution 
     * leading to different homology relations to different species), so that 
     * it would be part of several {@code AnatDevMapping}s, only the relation 
     * holding for the oldest ancestor will be considered. A same {@code AnatDevEntity} 
     * will never be part of several {@code AnatDevMapping}s, as defined by a same 
     * {@code EvoMappingSelector}.
     * <p>
     * Default value is {@code true}.
     * 
     * @param useAncestralTaxa  A {@code boolean} defining whether ancestral taxa 
     *                          of the {@code Taxon}s returned by {@link #getTaxa()} 
     *                          should also be considered.
     * @see #getTaxa()
     */
    public void setUseAncestralTaxa(boolean useAncestralTaxa) {
        this.useAncestralTaxa = useAncestralTaxa;
    }
}
