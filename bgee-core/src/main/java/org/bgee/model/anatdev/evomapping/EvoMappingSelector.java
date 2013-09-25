package org.bgee.model.anatdev.evomapping;

import java.util.Collection;
import java.util.HashSet;

import org.bgee.model.anatdev.evomapping.AnatDevMapping.TransRelationType;
import org.bgee.model.ontologycommon.Confidence;
import org.bgee.model.ontologycommon.EvidenceCode;
import org.bgee.model.species.Taxon;

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
     * #evoRelationType}).
     * <p>
     * If an {@code AnatDevElement} is related to different structures at different 
     * taxonomic levels (for instance, a structure with its different states of evolution 
     * leading to different homology relations to different species), so that 
     * it would be part of several {@code AnatDevMapping}s, only the relation 
     * holding for the most recent ancestor (but still in the scope defined 
     * by {@link #taxonScoping}) will be considered. A same {@code AnatDevElement} 
     * will never be part of several {@code AnatDevMapping}s, as defined by a same 
     * {@code EvoMappingSelector}.
     * <p>
     * Default is {@code false}.
     * 
     * @see #taxonScoping
     */
    private boolean useAncestralTaxa;
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
    
    public EvoMappingSelector(TransRelationType relationType, Taxon taxonScoping) {
        this.evoRelationType    = relationType;
        this.taxonScoping       = taxonScoping;
        this.allowedConfidences = new HashSet<Confidence>();
        this.allowedEvidences   = new HashSet<EvidenceCode>();
        this.setUseAncestralTaxa(false);
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
     * defined by {@link #getEvoRelationType()}).
     * <p>
     * If an {@code AnatDevElement} is related to different structures at different 
     * taxonomic levels (for instance, a structure with its different states of evolution 
     * leading to different homology relations to different species), so that 
     * it would be part of several {@code AnatDevMapping}s, only the relation 
     * holding for the most recent ancestor (but still in the scope defined 
     * by {@link #getTaxonScoping()}) will be considered. A same {@code AnatDevElement} 
     * will never be part of several {@code AnatDevMapping}s, as defined by a same 
     * {@code EvoMappingSelector}.
     * 
     * @return  A {@code boolean} defining whether ancestral taxa of the {@code Taxon} 
     *          returned by {@link #getTaxonScoping()} should also be considered.
     * @see #getTaxonScoping()
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
     * defined by {@link #getEvoRelationType()}).
     * <p>
     * If an {@code AnatDevElement} is related to different structures at different 
     * taxonomic levels (for instance, a structure with its different states of evolution 
     * leading to different homology relations to different species), so that 
     * it would be part of several {@code AnatDevMapping}s, only the relation 
     * holding for the most recent ancestor (but still in the scope defined 
     * by {@link #getTaxonScoping()}) will be considered. A same {@code AnatDevElement} 
     * will never be part of several {@code AnatDevMapping}s, as defined by a same 
     * {@code EvoMappingSelector}.
     * 
     * @param useAncestralTaxa  A {@code boolean} defining whether ancestral taxa 
     *                          of the {@code Taxon} returned by {@link #getTaxonScoping()} 
     *                          should also be considered.
     * @see #getTaxonScoping()
     */
    public void setUseAncestralTaxa(boolean useAncestralTaxa) {
        this.useAncestralTaxa = useAncestralTaxa;
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
