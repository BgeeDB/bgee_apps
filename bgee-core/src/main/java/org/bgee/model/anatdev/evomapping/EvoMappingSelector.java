package org.bgee.model.anatdev.evomapping;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.bgee.model.anatdev.evomapping.AnatDevMapping.TransRelationType;
import org.bgee.model.ontologycommon.Confidence;
import org.bgee.model.ontologycommon.EvidenceCode;
import org.bgee.model.species.Species;
import org.bgee.model.species.Taxon;
import org.bgee.model.species.TaxonFactory;

/**
 * This class allows to provide all the parameters available to select 
 * {@link AnatDevMapping}s. The parameters defined the {@link TransRelationType} 
 * to use (see {@link #setRelationType(TransRelationType)}), the taxa for which
 * the relations should hold (see {@link #setTaxonScoping(Taxon)} and {@link 
 * #setUseAncestralTaxa(boolean)}), and finally, allowed to filter the mappings 
 * based on the {org.bgee.model.ontologycommon.EvidenceCode}s and {@link 
 * #org.bgee.model.ontologycommon.Confidence} supporting them (see {@link 
 * #addConfidence(Confidence)} and {@link #addEvidenceCode(EvidenceCode)}).
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public class EvoMappingSelector {
    /**
     * A {@code TransRelationType} defining what evolutionary relation 
     * the {@link AnatDevMapping}s should be based on.
     */
    private final TransRelationType evoRelationType;
    /**
     * The {@code Taxon} for which the {@link #evoRelationType} should hold, 
     * in order to build the {@link AnatDevMapping}s.
     * For instance, if {@link #evoRelationType} is {@code HOMOLOGY}, then 
     * this {@code Taxon} defines the last common ancestor where an ancestral 
     * structure existed, and which some homologous structures evolved from, 
     * that will be grouped into an {@code AnatDevMapping}.
     * <p>
     * If ancestors of this {@code Taxon} should also be considered, then 
     * {@link #useAncestralTaxa} must be set to {@code true}. 
     * 
     * @see #useAncestralTaxa
     */
    private final Taxon taxonScoping;
    /**
     * A {@code boolean} defining whether ancestral taxa of {@link #taxonScoping} 
     * should also be considered. If {@code true}, it will lead to also select 
     * {@link AnatDevMapping}s based on relations holding for taxa ancestors of 
     * {@link #taxonScoping} (relations of the type defined by {@link 
     * #evoRelationType}). This will result in selecting mappings defined for 
     * the taxon specifid by {@link #taxonScoping}, but also mappings that encompass 
     * this specified taxon, spanning a wider taxonomical range; it means that 
     * the mappings will be valid for the specified taxon, but also for other taxa.
     * <p>
     * If an {@code AnatDevElement} is related to different structures at different 
     * taxonomic levels (for instance, a structure with its different states of evolution 
     * leading to different homology relations to different species), so that 
     * it would be part of several {@code AnatDevMapping}s, only the relation 
     * holding for the oldest ancestor (but still in the scope defined 
     * by {@link #taxonScoping}) will be considered. A same {@code AnatDevElement} 
     * will never be part of several {@code AnatDevMapping}s, as defined by a same 
     * {@code EvoMappingSelector}.
     * <p>
     * Default is {@code true}.
     * 
     * @see #taxonScoping
     * @see #useDescentTaxa
     */
    private boolean useAncestralTaxa;
    /**
     * A {@code boolean} defining whether taxa descendant of {@link #taxonScoping} 
     * should also be considered. If {@code true}, it will lead to also select 
     * {@link AnatDevMapping}s based on relations holding for sub-taxa of 
     * {@link #taxonScoping} (relations of the type defined by {@link 
     * #evoRelationType}). This will result in selecting mappings defined for 
     * the taxon specifid by {@link #taxonScoping}, but also mappings that are 
     * more restricted, valid only for a subset of the taxa encompassed by 
     * {@link #taxonScoping} (so, valid only for a subset of the species member 
     * of {@link #taxonScoping}, not all of them).
     * <p>
     * It is possible to restrict the sub-taxa considered by using {@link 
     * #descentTaxonRestriction}. It can be easily set by providing {@code Species} 
     * rather than {@code Taxon}, see its documentation.
     * <p>
     * If an {@code AnatDevElement} is related to different structures at different 
     * taxonomic levels (for instance, a structure with its different states of evolution 
     * leading to different homology relations to different species), so that 
     * it would be part of several {@code AnatDevMapping}s, only the relation 
     * holding for the oldest ancestor (but still in the scope defined 
     * by {@link #taxonScoping}) will be considered. A same {@code AnatDevElement} 
     * will never be part of several {@code AnatDevMapping}s, as defined by a same 
     * {@code EvoMappingSelector}.
     * <p>
     * Default is {@code false}.
     * 
     * @see #taxonScoping
     * @see #useAncestralTaxa
     */
    private boolean useDescentTaxa;
    /**
     * A {@code Collection} of {@code Taxon}s, used when {@link #useDescentTaxa} 
     * is {@code true}, to restrict the descent taxa considered. Usually, 
     * when {@link #useDescentTaxa} is {@code true}, additional mappings valid 
     * in any sub-taxa of {@link #taxonScoping} are considered. But if this 
     * {@code Collection} is not empty, only the sub-taxa included in it will be 
     * considered. 
     */
    private final Set<Taxon> descentTaxonRestrictions;
    /**
     * A {@code Collection} of {@code Confidence}s defining the allowed confidence 
     * information: only mappings involving these {@code Confidence}s will be selected.
     */
    private final Collection<Confidence> allowedConfidences;
    /**
     * A {@code Collection} of {@code EvidenceCode}s defining the allowed evidence 
     * codes: only mappings based on these {@code EvidenceCode}s will be selected.
     */
    private final Collection<EvidenceCode> allowedEvidences;
    
    /**
     * Constructor providing the {@code TransRelationType} defining what evolutionary 
     * relation the {@link AnatDevMapping}s should be based on, and the {@code Taxon} 
     * for which this {@code TransRelationType} should hold.
     * 
     * @param relationType  The {@code TransRelationType} that is the relation which 
     *                      the {@link AnatDevMapping}s should be based on.
     * @param taxonScoping  The {@code Taxon} which the {@link AnatDevMapping}s 
     *                      should be defined for.
     * @see #EvoMappingSelector(TransRelationType, Collection)
     */
    public EvoMappingSelector(TransRelationType relationType, Taxon taxonScoping) {
        
        this.evoRelationType          = relationType;
        this.taxonScoping             = taxonScoping;
        this.setUseAncestralTaxa(true);
        this.setUseDescentTaxa(false);
        this.descentTaxonRestrictions = new HashSet<Taxon>();
        
        this.allowedConfidences       = new HashSet<Confidence>();
        this.allowedEvidences         = new HashSet<EvidenceCode>();
    }
    
    /**
     * Constructor providing the {@code TransRelationType} defining what evolutionary 
     * relation the {@link AnatDevMapping}s should be based on, and a {@code Collection} 
     * of {@code Species} allowing to define the taxon scoping, meaning the {@code Taxon} 
     * for which this {@code TransRelationType} should hold.
     * <p>
     * This constructor identifies the most recent ancestor {@link Taxon} common to all  
     * the {@code Species} in {@code speciesInScope}, and use it as the taxon scoping 
     * (see {@link #getTaxonScoping()}). 
     * It also calls {@link #addAllDescentSpeciesRestrictions(Collection)} using 
     * {@code speciesInScope}, so that if {@link #setUseDescentTaxa(boolean)} is 
     * latter called with the argument {@code true}, there will be already descent 
     * taxon restrictions in place (see {@link #getDescentTaxonRestrictions()}).
     * 
     * @param relationType      The {@code TransRelationType} that is the relation which 
     *                          the {@link AnatDevMapping}s should be based on.
     * @param speciesInScope    A {@code Collection} of {@code Species} allowing 
     *                          to identify the taxon scoping and to provide restrictions 
     *                          on descent sub-taxa to consider.
     */
    public EvoMappingSelector(TransRelationType relationType, 
            Collection<Species> speciesInScope) {
        this(relationType, (new TaxonFactory()).getMostRecentCommonTaxon(speciesInScope));
        this.addAllDescentSpeciesRestrictions(speciesInScope);
    }

    //**************************************
    // GETTERS/SETTERS
    //**************************************

    /**
     * @return  the {@code TransRelationType} defining what evolutionary relation 
     *          the {@link AnatDevMapping}s should be based on.
     */
    public TransRelationType getEvoRelationType() {
        return evoRelationType;
    }

    /**
     * Returns the {@code Taxon} for which the {@code TransRelationType} returned 
     * by {@link #getEvoRelationType()} should hold, in order to build the {@link 
     * AnatDevMapping}s.
     * <p>
     * For instance, if {@link #getEvoRelationType()} returns {@code HOMOLOGY}, 
     * then this {@code Taxon} defines the last common ancestor where an ancestral 
     * structure existed, and which some homologous structures evolved from, 
     * that will be grouped into an {@code AnatDevMapping}.
     * <p>
     * If ancestors of this {@code Taxon} should also be considered, then users 
     * must call {@link #setUseAncestralTaxa(boolean)} with the value {@code true}. 
     * 
     * @return  the {@code Taxon} for which the {@code TransRelationType} returned 
     *          by {@link #getEvoRelationType()} should hold, in order to build the 
     *          {@link AnatDevMapping}s
     * @see #isUseAncestralTaxa()
     */
    public Taxon getTaxonScoping() {
        return taxonScoping;
    }
    
    /**
     * Returns the {@code boolean} defining whether ancestral taxa of the {@code Taxon} 
     * returned by {@link #getTaxonScoping()} should also be considered. 
     * If {@code true}, it will lead to also select {@link AnatDevMapping}s 
     * based on relations holding for taxa ancestors of the {@code Taxon} 
     * returned by {@link #getTaxonScoping()} (with relations still of the type 
     * defined by {@link #getEvoRelationType()}). This will result in selecting 
     * mappings defined for the taxon specifid by {@link #getTaxonScoping()}, 
     * but also mappings that encompass this specified taxon, spanning a wider 
     * taxonomical range; it means that the mappings will be valid for the specified 
     * taxon, but also for other taxa.
     * <p>
     * If an {@code AnatDevElement} is related to different structures at different 
     * taxonomic levels (for instance, a structure with its different states of evolution 
     * leading to different homology relations to different species), so that 
     * it would be part of several {@code AnatDevMapping}s, only the relation 
     * holding for the oldest ancestor (but still in the scope defined 
     * by {@link #getTaxonScoping()}) will be considered. A same {@code AnatDevElement} 
     * will never be part of several {@code AnatDevMapping}s, as defined by a same 
     * {@code EvoMappingSelector}.
     * <p>
     * Default value is {@code true}.
     * 
     * @return  A {@code boolean} defining whether ancestral taxa of the {@code Taxon} 
     *          returned by {@link #getTaxonScoping()} should also be considered.
     * @see #getTaxonScoping()
     * @see #isUseDescentTaxa()
     */
    public boolean isUseAncestralTaxa() {
        return useAncestralTaxa;
    }
    /**
     * Set the {@code boolean} defining whether ancestral taxa of the {@code Taxon} 
     * returned by {@link #getTaxonScoping()} should also be considered. 
     * If {@code true}, it will lead to also select {@link AnatDevMapping}s 
     * based on relations holding for taxa ancestors of the {@code Taxon} 
     * returned by {@link #getTaxonScoping()} (with relations still of the type 
     * defined by {@link #getEvoRelationType()}). This will result in selecting 
     * mappings defined for the taxon specifid by {@link #getTaxonScoping()}, 
     * but also mappings that encompass this specified taxon, spanning a wider 
     * taxonomical range; it means that the mappings will be valid for the specified 
     * taxon, but also for other taxa.
     * <p>
     * If an {@code AnatDevElement} is related to different structures at different 
     * taxonomic levels (for instance, a structure with its different states of evolution 
     * leading to different homology relations to different species), so that 
     * it would be part of several {@code AnatDevMapping}s, only the relation 
     * holding for the oldest ancestor (but still in the scope defined 
     * by {@link #getTaxonScoping()}) will be considered. A same {@code AnatDevElement} 
     * will never be part of several {@code AnatDevMapping}s, as defined by a same 
     * {@code EvoMappingSelector}.
     * <p>
     * Default value is {@code true}.
     * 
     * @param useAncestralTaxa  A {@code boolean} defining whether ancestral taxa 
     *                          of the {@code Taxon} returned by {@link #getTaxonScoping()} 
     *                          should also be considered.
     * @see #getTaxonScoping()
     * @see #setUseDescentTaxa(boolean)
     */
    public void setUseAncestralTaxa(boolean useAncestralTaxa) {
        this.useAncestralTaxa = useAncestralTaxa;
    }
    
    /**
     * Returns the {@code boolean} defining whether taxa descendant of the {@code Taxon} 
     * returned by {@link #getTaxonScoping()} should also be considered. 
     * If {@code true}, it will lead to also select {@link AnatDevMapping}s 
     * based on relations holding for sub-taxa of the {@code Taxon} 
     * returned by {@link #getTaxonScoping()} (with relations still of the type 
     * defined by {@link #getEvoRelationType()}). This will result in selecting 
     * mappings defined for the taxon specifid by {@link #getTaxonScoping()}, 
     * but also mappings that are more restricted, valid only for a subset 
     * of the taxa encompassed by the specified taxon (so, valid only for 
     * a subset of the species member of the specified taxon, not all of them).
     * <p>
     * It is possible to restrict the sub-taxa considered, see {@link 
     * #getDescentTaxonRestrictions()}.
     * <p>
     * If an {@code AnatDevElement} is related to different structures at different 
     * taxonomic levels (for instance, a structure with its different states of evolution 
     * leading to different homology relations to different species), so that 
     * it would be part of several {@code AnatDevMapping}s, only the relation 
     * holding for the most recent ancestor (but still in the scope defined 
     * by {@link #getTaxonScoping()}) will be considered. A same {@code AnatDevElement} 
     * will never be part of several {@code AnatDevMapping}s, as defined by a same 
     * {@code EvoMappingSelector}.
     * <p>
     * Default value is {@code false}.
     * 
     * @return  A {@code boolean} defining whether ancestral taxa of the {@code Taxon} 
     *          returned by {@link #getTaxonScoping()} should also be considered.
     * @see #getTaxonScoping()
     * @see #getDescentTaxonRestrictions()
     * @see #isUseAncestralTaxa()
     */
    public boolean isUseDescentTaxa() {
        return useDescentTaxa;
    }
    /**
     * Sets the {@code boolean} defining whether taxa descendant of the {@code Taxon} 
     * returned by {@link #getTaxonScoping()} should also be considered. 
     * If {@code true}, it will lead to also select {@link AnatDevMapping}s 
     * based on relations holding for sub-taxa of the {@code Taxon} 
     * returned by {@link #getTaxonScoping()} (with relations still of the type 
     * defined by {@link #getEvoRelationType()}). This will result in selecting 
     * mappings defined for the taxon specifid by {@link #getTaxonScoping()}, 
     * but also mappings that are more restricted, valid only for a subset 
     * of the taxa encompassed by the specified taxon (so, valid only for 
     * a subset of the species member of the specified taxon, not all of them).
     * <p>
     * It is possible to restrict the sub-taxa considered, see {@link 
     * #getDescentTaxonRestrictions()}.
     * <p>
     * If an {@code AnatDevElement} is related to different structures at different 
     * taxonomic levels (for instance, a structure with its different states of evolution 
     * leading to different homology relations to different species), so that 
     * it would be part of several {@code AnatDevMapping}s, only the relation 
     * holding for the most recent ancestor (but still in the scope defined 
     * by {@link #getTaxonScoping()}) will be considered. A same {@code AnatDevElement} 
     * will never be part of several {@code AnatDevMapping}s, as defined by a same 
     * {@code EvoMappingSelector}.
     * <p>
     * Default value is {@code false}.
     * 
     * @param useDescentTaxa    A {@code boolean} defining whether ancestral taxa 
     *                          of the {@code Taxon} returned by {@link #getTaxonScoping()} 
     *                          should also be considered.
     * @see #getTaxonScoping()
     * @see #getDescentTaxonRestrictions()
     * @see #setUseAncestralTaxa(boolean)
     */
    public void setUseDescentTaxa(boolean useDescentTaxa) {
        this.useDescentTaxa = useDescentTaxa;
    }
    
    /**
     * Returns the {@code Collection} of {@code Taxon}s, used when {@link 
     * #isUseDescentTaxa()} returns {@code true}, to restrict the descent taxa 
     * considered. Usually, when {@link #isUseDescentTaxa()} is {@code true}, 
     * additional mappings valid in any sub-taxa of the {@code Taxon} specified by 
     * {@link #getTaxonScoping()} are considered. But if this {@code Collection} 
     * is not empty, only the taxa included in it will be considered.
     * <p>
     * These taxa can either be provided directly (see {@link 
     * #addDescentTaxonRestriction(Taxon)} and 
     * {@link #addAllDescentTaxonRestrictions(Collection)}), or by providing 
     * {@code Species} (see {@link #addDescentSpeciesRestriction(Species)} and 
     * {@link #addAllDescentSpeciesRestrictions(Collection)}).
     * 
     * @return  A {@code Collection} of {@code Taxon}s to limit the sub-taxa considered, 
     *          when {@link #isUseDescentTaxa()} returns {@code true}.
     * @see #addDescentTaxonRestriction(Taxon)
     * @see #addAllDescentTaxonRestrictions(Collection)
     * @see #addDescentSpeciesRestriction(Species)
     * @see #addAllDescentSpeciesRestrictions(Collection)
     */
    public Set<Taxon> getDescentTaxonRestrictions() {
        return this.descentTaxonRestrictions;
    }
    /**
     * Add {@code taxonRestricted} to the {@code Collection} of {@code Taxon}s, 
     * used when {@link #isUseDescentTaxa()} returns {@code true}, to restrict 
     * the descent taxa considered. Usually, when {@link #isUseDescentTaxa()} 
     * is {@code true}, additional mappings valid in any sub-taxa of the {@code Taxon} 
     * specified by {@link #getTaxonScoping()} are considered. But if 
     * {@link #getDescentTaxonRestrictions()} returns a non empty {@code Collection}, 
     * only the taxa included in it will be considered.
     * <p>
     * These taxa can either be provided directly, as by using this method, or 
     * by providing {@code Species} (see {@link #addDescentSpeciesRestriction(Species)} 
     * and {@link #addAllDescentSpeciesRestrictions(Collection)}).
     * 
     * @param taxonRestricted   A {@code Taxon} to be added to the {@code Collection} of 
     *                          {@code Taxon}s used to limit the sub-taxa considered, 
     *                          when {@link #isUseDescentTaxa()} returns {@code true}.
     * @see #getDescentTaxonRestrictions()
     * @see #addAllDescentTaxonRestrictions(Collection)
     * @see #addDescentSpeciesRestriction(Species)
     * @see #addAllDescentSpeciesRestrictions(Collection)
     */
    public void addDescentTaxonRestriction(Taxon taxonRestricted) {
        this.descentTaxonRestrictions.add(taxonRestricted);
    }
    /**
     * Add {@code taxaRestricted} to the {@code Collection} of {@code Taxon}s, 
     * used when {@link #isUseDescentTaxa()} returns {@code true}, to restrict 
     * the descent taxa considered. Usually, when {@link #isUseDescentTaxa()} 
     * is {@code true}, additional mappings valid in any sub-taxa of the {@code Taxon} 
     * specified by {@link #getTaxonScoping()} are considered. But if 
     * {@link #getDescentTaxonRestrictions()} returns a non empty {@code Collection}, 
     * only the taxa included in it will be considered.
     * <p>
     * These taxa can either be provided directly, as by using this method, or 
     * by providing {@code Species} (see {@link #addDescentSpeciesRestriction(Species)} 
     * and {@link #addAllDescentSpeciesRestrictions(Collection)}).
     * 
     * @param taxaRestricted    A {@code Collection} of {@code Taxon}s to be added to 
     *                          the {@code Collection} of {@code Taxon}s used to limit 
     *                          the sub-taxa considered, when {@link #isUseDescentTaxa()} 
     *                          returns {@code true}.
     * @see #getDescentTaxonRestrictions()
     * @see #addDescentTaxonRestriction(Taxon)
     * @see #addDescentSpeciesRestriction(Species)
     * @see #addAllDescentSpeciesRestrictions(Collection)
     */
    public void addAllDescentTaxonRestrictions(Collection<Taxon> taxaRestricted) {
        this.descentTaxonRestrictions.addAll(taxaRestricted);
    }
    /**
     * Helper method to provide taxon restrictions when {@link #isUseDescentTaxa()} 
     * returns {@code true}. This method will retrieve all parent taxa of 
     * {@code speciesRestriction}, and will pass them to the method 
     * {@link #addAllDescentTaxonRestrictions(Collection)}. See this latter method 
     * for more details. 
     * 
     * @param speciesRestriction    A {@code Species} used to provide taxon restriction 
     *                              when {@link #isUseDescentTaxa()} returns {@code true}.
     * @see #addAllDescentTaxonRestrictions(Collection)
     * @see #getDescentTaxonRestrictions()
     */
    public void addDescentSpeciesRestriction(Species speciesRestriction) {
        this.addAllDescentTaxonRestrictions(
                (new TaxonFactory()).getAncestorTaxa(speciesRestriction));
    }
    /**
     * Helper method to provide taxon restrictions when {@link #isUseDescentTaxa()} 
     * returns {@code true}. This method will retrieve all parent taxa of all 
     * {@code Species} in {@code speciesRestrictions}, and will pass them to the method 
     * {@link #addAllDescentTaxonRestrictions(Collection)}. See this latter method 
     * for more details. 
     * 
     * @param speciesRestrictions   A {@code Collection} of {@code Species} used to provide 
     *                              taxon restriction when {@link #isUseDescentTaxa()} 
     *                              returns {@code true}.
     * @see #addAllDescentTaxonRestrictions(Collection)
     * @see #getDescentTaxonRestrictions()
     */
    public void addAllDescentSpeciesRestrictions(Collection<Species> speciesRestrictions) {
        this.addAllDescentTaxonRestrictions(
                (new TaxonFactory()).getAncestorTaxa(speciesRestrictions));
    }

    /**
     * @return  The {@code Collection} of {@code Confidence}s defining the allowed 
     *          confidence information: only mappings involving these {@code Confidence}s 
     *          will be selected.
     */
    public Collection<Confidence> getAllowedConfidences() {
        return this.allowedConfidences;
    }
    /**
     * Add a {@code Collection} of {@code Confidence}s to the allowed {@code Confidence}s 
     * (returned by {@link #getAllowedConfidences}): only mappings involving 
     * these {@code Confidence}s will be selected.
     * 
     * @param confidences    A {@code Collection} of {@code Confidence}s 
     *                       to be added to this {@code EvoMappingSelector}. 
     */
    public void addAllConfidences(Collection<Confidence> confidences) {
        this.allowedConfidences.addAll(confidences);
    }
    /**
     * Add a {{@code Confidence} to the allowed {@code Confidence}s 
     * (returned by {@link #getAllowedConfidences}): only mappings involving 
     * these {@code Confidence}s will be selected.
     * 
     * @param confidence     A {@code Confidence} to be added to this 
     *                       {@code EvoMappingSelector}. 
     */
    public void addConfidence(Confidence confidence) {
        this.allowedConfidences.add(confidence);
    }

    /**
     * @return  The {@code Collection} of {@code EvidenceCode}s defining the allowed 
     *          evidence code: only mappings involving these {@code EvidenceCode}s 
     *          will be selected.
     */
    public Collection<EvidenceCode> getAllowedEvidenceCodes() {
        return this.allowedEvidences;
    }
    /**
     * Add a {@code Collection} of {@code EvidenceCode}s to the allowed {@code EvidenceCode}s 
     * (returned by {@link #getAllowedEvidenceCodes}): only mappings involving 
     * these {@code EvidenceCode}s will be selected.
     * 
     * @param evidenceCodes      A {@code Collection} of {@code EvidenceCode}s 
     *                           to be added to this {@code EvoMappingSelector}. 
     */
    public void addAllEvidenceCodes(Collection<EvidenceCode> evidenceCodes) {
        this.allowedEvidences.addAll(evidenceCodes);
    }
    /**
     * Add an {{@code EvidenceCode} to the allowed {@code EvidenceCode}s 
     * (returned by {@link #getEvidenceCodes}): only mappings involving 
     * these {@code EvidenceCode}s will be selected.
     * 
     * @param evidenceCode   A {@code EvidenceCode} to be added to this 
     *                       {@code EvoMappingSelector}. 
     */
    public void addEvidenceCode(EvidenceCode evidenceCode) {
        this.allowedEvidences.add(evidenceCode);
    }
}
