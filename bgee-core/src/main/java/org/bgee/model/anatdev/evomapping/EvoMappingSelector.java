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
 * {@link AnatDevMapping}s. The mandatory parameters are: 
 * <ul>
 * <li>the {@link TransRelationType} to use. It is provided at instantiation and 
 * can be obtained by calling {@link #getEvoRelationType()}). It defines what 
 * evolutionary relation the {@link AnatDevMapping}s to retrieve should be based on.
 * <li>the {@code Taxon} for which the mappings should hold (see {@link 
 * #getTaxonScoping()} for more details.) This {@code Taxon} can be either directly 
 * provided at instantiation (see {@link #EvoMappingSelector(TransRelationType, Taxon)}), 
 * or can be inferred from a {@code Collection} of {@code Species} provided at 
 * instantiation (see {@link #EvoMappingSelector(TransRelationType, Collection)}); 
 * in that case, the {@code Taxon} used for scoping will be the most recent ancestor 
 * common to all the {@code Species}.
 * </ul>
 * Additional parameters are: 
 * <ul>
 * <li>providing {@code Species} to restrict the {@code AnatDevMapping}s 
 * to retrieve. If some are provided, all {@code AnatDevMapping}s retrieved will be valid 
 * for taxa in the lineage of any of those {@code Species} (which is useful when 
 * {@link isUseDescentTaxa()} is {@code true}), and they will contain 
 * only {@code AnatDevEntity}s existing in at least one of those {@code Species} 
 * (which can be useful in any case). 
 * See {@link #addSpeciesRestriction(Species)} and 
 * {@link #addAllSpeciesRestrictions(Collection)}.
 * <li>whether the taxa ancestors of the {@code Taxon} used for scoping should also 
 * be considered, see {@link #setUseAncestralTaxa(boolean)}.
 * <li>whether the taxa descendants of the {@code Taxon} used for scoping should also 
 * be considered, see {@link #setUseDescentTaxa(boolean)}. In that case, it means 
 * that the {@link AnatDevMapping}s retrieved will also include mappings valid between  
 * only <strong>some</strong> of the {@code Species} belonging to the {@code Taxon} 
 * used for scoping, and not between <strong>all</strong> of them as it would be 
 * otherwise the case.
 * <li>finally, it is possible to filter the mappings based on the 
 * {@link org.bgee.model.ontologycommon.EvidenceCode}s and {@link 
 * org.bgee.model.ontologycommon.Confidence} supporting them (see {@link 
 * #addConfidence(Confidence)} and {@link #addEvidenceCode(EvidenceCode)}).
 * </ul>
 * <p>
 * It is likely that users most of the time will only need to call the constructor 
 * {@link #EvoMappingSelector(TransRelationType, Collection)}, and to decide whether 
 * they want to retrieve mappings holding between all provided {@code Species}, or 
 * between some of the provided {@code Species}, by calling 
 * {@link #setUseDescentTaxa(boolean)}, or not.
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
     * {@link #useAncestralTaxa} must be set to {@code true}. If descendants 
     * of this {@code Taxon} should also be considered, then {@link #useDescentTaxa} 
     * must be set to {@code true}. See these attributes for important details.
     * 
     * @see #useAncestralTaxa
     * @see #useDescentTaxa
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
     * {@link #taxonScoping} (so, valid only between some of the species member 
     * of {@link #taxonScoping}, not valid between all of them as it is usually the case).
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
     * A {@code Set} of {@code Species} used to define restrictions on the 
     * {@code AnatDevMapping}s to retrieve. If not empty, only the {@code AnatDevMapping}s 
     * that are valid for a {@code Taxon} in the lineage of one of these {@code Species} 
     * will be considered, and they will contain only {@code AnatDevEntity}s existing 
     * in one of these {@code Species}.
     */
    private Set<Species> speciesRestrictions;
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
        this.speciesRestrictions      = new HashSet<Species>();
        
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
     * It then calls {@link #addAllSpeciesRestrictions(Collection)} using 
     * {@code speciesInScope}, so that all {@code AnatDevMapping}s retrieved will be valid 
     * for taxa in the lineage of any of those {@code Species} (which is useful if you 
     * latter call {@link setUseDescentTaxa(boolean)} with {@code true}), and 
     * they will contain only {@code AnatDevEntity}s existing in at least one of 
     * those {@code Species} (which can be useful in any case).
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
        this.addAllSpeciesRestrictions(speciesInScope);
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
     * If descendants of this {@code Taxon} should also be considered, then users 
     * must call {@link #setUseDescentTaxa(boolean)} with the value {@code true}.
     * See these methods for important details.
     * 
     * @return  the {@code Taxon} for which the {@code TransRelationType} returned 
     *          by {@link #getEvoRelationType()} should hold, in order to build the 
     *          {@link AnatDevMapping}s
     * @see #isUseAncestralTaxa()
     * @see #isUseDescentTaxa()
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
     * of the taxa encompassed by the specified taxon (so, valid only between 
     * some of the species member of the specified taxon, not valid between all 
     * of them as it is usually the case).
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
     * of the taxa encompassed by the specified taxon (so, valid only between 
     * some of the species member of the specified taxon, not valid between all 
     * of them as it is usually the case).
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
     * @see #setUseAncestralTaxa(boolean)
     */
    public void setUseDescentTaxa(boolean useDescentTaxa) {
        this.useDescentTaxa = useDescentTaxa;
    }
    
    /**
     * Add {@code speciesRestriction} to the {@code Collection} of {@code Species} 
     * used to define restrictions on the {@code AnatDevMapping}s to retrieve. 
     * Only the {@code AnatDevMapping}s that are valid for a {@code Taxon} in the lineage 
     * of one of these {@code Species} will be considered (which is useful when 
     * {@link isUseDescentTaxa()} is {@code true}), and they will contain 
     * only {@code AnatDevEntity}s existing in one of these {@code Species} 
     * (which can be useful in any case).
     * 
     * @param speciesRestrictions   A {@code Species} to be added to the {@code Collection}
     *                              of {@code Species} used to restrain the 
     *                              {@code AnatDevMapping}s retrieved, 
     *                              and their contained {@code AnatDevEntity}s.
     * @see #getSpeciesRestrictions()
     */
    public void addSpeciesRestriction(Species speciesRestriction) {
        this.speciesRestrictions.add(speciesRestriction);
    }
    /**
     * Add {@code speciesRestrictions} to the {@code Collection} of {@code Species} 
     * used to define restrictions on the {@code AnatDevMapping}s to retrieve. 
     * Only the {@code AnatDevMapping}s that are valid for a {@code Taxon} in the lineage 
     * of one of these {@code Species} will be considered (which is useful when 
     * {@link isUseDescentTaxa()} is {@code true}), and they will contain 
     * only {@code AnatDevEntity}s existing in one of these {@code Species} 
     * (which can be useful in any case).
     * 
     * @param speciesRestrictions   A {@code Collection} of {@code Species} to be added 
     *                              to the {@code Species} used to restrain the 
     *                              {@code AnatDevMapping}s retrieved, 
     *                              and their contained {@code AnatDevEntity}s.
     * @see #getSpeciesRestrictions()
     */
    public void addAllSpeciesRestrictions(Collection<Species> speciesRestrictions) {
        this.speciesRestrictions.addAll(speciesRestrictions);
    }
    /**
     * Returns the {@code Collection} of {@code Species} used to define restrictions 
     * on the {@code AnatDevMapping}s to retrieve. If not empty, Only the 
     * {@code AnatDevMapping}s that are valid for a {@code Taxon} in the lineage 
     * of one of these {@code Species} will be considered (which is useful when 
     * {@link isUseDescentTaxa()} is {@code true}), and they will contain 
     * only {@code AnatDevEntity}s existing in one of these {@code Species} 
     * (which can be useful in any case).
     * 
     * @return  the {@code Collection} of {@code Species} used to define restrictions 
     *          on the {@code AnatDevMapping}s to retrieve.
     */
    public Set<Species> getSpeciesRestrictions() {
        return this.speciesRestrictions;
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
