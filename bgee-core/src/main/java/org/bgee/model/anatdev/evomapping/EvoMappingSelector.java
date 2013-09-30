package org.bgee.model.anatdev.evomapping;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.bgee.model.ontologycommon.Confidence;
import org.bgee.model.ontologycommon.EvidenceCode;
import org.bgee.model.species.Species;

/**
 * This class allows to provide parameters to select {@link AnatDevMapping}s 
 * based on transitive relations, as defined by {@link TransRelationType}. It is 
 * mandatory to provide a {@code TransRelationType} at instantiation. If this is 
 * the only parameter provided, then all {@code AnatDevMapping}s based on this 
 * relation type will be retrieved.
 * <p>
 * It is then possible to restrict the scope of the relations, by providing a 
 * {@code Collection} of {@code Species} (see {@link #addSpecies(Species)} and 
 * {@link #addAllSpecies(Collection)}). Only the relations defining a mapping 
 * valid between <strong>all</strong> of the provided {@code Species} will be used. 
 * For instance, in the case of a {@code HOMOLOGY} relation, this is equivalent 
 * to retrieving relations defined for the common ancestral taxon of all the provided 
 * {@code Species}, and all their ancestor taxa. Also, the {@link AnatDevMapping}s 
 * retrieved will contain only {@code AnatDevEntity}s existing in at least one of 
 * those {@code Species}.
 * <p>
 * It is also possible to filter the mappings based on the 
 * {@link org.bgee.model.ontologycommon.EvidenceCode}s and {@link 
 * org.bgee.model.ontologycommon.Confidence} supporting them (see {@link 
 * #addConfidence(Confidence)} and {@link #addEvidenceCode(EvidenceCode)}).
 * <p>
 * It is likely that users most of the time will need to provide some {@code Species}, 
 * by using {@link #addSpecies(Species)} or {@link #addAllSpecies(Collection)} 
 * after instantiation.
 * <p>
 * Of note, it exists the subclass {@link TaxonEvoMappingSelector}, specific to {@link 
 * TransRelationType.TaxonBasedRelationType}, that provides additional methods 
 * to filter relations by using {@code Taxon} objects, besides {@code Species}. 
 * But this {@code EvoMappingSelector} can also be used to retrieve {@code 
 * TaxonBasedRelationType} relations in any case, as long as only {@code Species} 
 * are needed to be used. It is only when users want to use taxon-scoping, for 
 * greater flexibility, that the {@code TaxonEvoMappingSelector} needs to be used.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public class EvoMappingSelector {
    /**
     * Represents the different type of evolutionary transitive relations. 
     * They are taken from the 
     * <a href='http://www.obofoundry.org/cgi-bin/detail.cgi?id=homology_ontology'>
     * HOM ontology</a>, but as long as we do not use more concepts, we will 
     * simply used these {@code enum}s.
     * 
     * @author Frederic Bastian
     * @version Bgee 13
     * @since Bgee 13
     */
    public interface TransRelationType {
        /**
         * Represents the type of transitive relations for which the scope is taxon-based: 
         * these relations hold at the level of a {@code Taxon}. For instance, 
         * the {@code Taxon} associated to a {@code HOMOLOGY} relation represents 
         * the ancestral taxon where an ancestral structure first appeared 
         * (supposedly), from which mapped {@code AnatDevEntity}s derived from.
         * <p>
         * It means that it is possible to provide {@code Taxon}s to retrieve 
         * relations defined at their level.
         * <p>
         * Note: as of Bgee 13, {@code HOMOLOGY} is the only {@code TaxonBasedRelationType} 
         * implemented, but we let open the possibility to add others in later releases.
         */
        public enum TaxonBasedRelationType implements TransRelationType {
            HOMOLOGY;
        }
        /**
         * Represents the type of transitive relations that can hold between different 
         * {@code Species}, regardless of their taxonomic position. These relations 
         * are species-based, they are defined between a set of species.
         * <p>
         * It means that it is possible to provide a list of {@code Species} to retrieve 
         * relations holding between all of them.
         * <p>
         * Note: as of Bgee 13, functional equivalence is the only {@code GroupRelationType} 
         * that is planed to be integrated, and will be in a later release. 
         */
        public enum SpeciesBasedRelationType implements TransRelationType {
            FUNCTIONEQUIVALENCE;
        }
    }
    
    /**
     * A {@code TransRelationType} defining what evolutionary relation 
     * the {@link AnatDevMapping}s should be based on.
     */
    private final TransRelationType evoRelationType;
    
    /**
     * A {@code Set} of {@code Species} to provide restrictions on 
     * the {@code AnatDevMapping}s to retrieve, and/or on the {@code AnatDevEntity}s 
     * they contain. 
     * <p>
     * If this {@code Set} is not empty, these {@code Species} are used to select 
     * the relevant {@code AnatDevMapping}s: they will be based on relations 
     * holding between <strong>all</strong> of these {@code Species}. For instance, 
     * if {@code #evoRelationType} is equal to {@code HOMOLOGY}, this would be 
     * equivalent to using homology relations defined for their closest common 
     * ancestor {@code Taxon}, and all its ancestor taxa (so, to identify all 
     * structures in those {@code Species}, derived from structures existing 
     * in their common ancestor).
     * <p>
     * And in any case, the {@code AnatDevMapping}s obtained will only contain 
     * {@code AnatDevEntity}s existing in at least one of these {@code Species}.
     */
    private final Set<Species> species;
    
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
     * relation the {@link AnatDevMapping}s should be based on. It is then possible 
     * to additionally provide {@code Species} to restrict the scope of the relations 
     * (see {@link #addSpecies(Species)}, and {@link #addAllSpecies(Collection)}).
     * 
     * @param relationType  The {@code TransRelationType} that is the relation which 
     *                      the {@link AnatDevMapping}s should be based on.
     */
    public EvoMappingSelector(TransRelationType relationType) {
        
        this.evoRelationType = relationType;
        this.species         = new HashSet<Species>();
        
        this.allowedConfidences = new HashSet<Confidence>();
        this.allowedEvidences   = new HashSet<EvidenceCode>();
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
     * Add {@code speciesRestriction} to the {@code Collection} of {@code Species} 
     * used to provide restrictions on the {@code AnatDevMapping}s to retrieve, 
     * and/or on the {@code AnatDevEntity}s they contain. 
     * <p>
     * These {@code Species} are used to select the relevant {@code AnatDevMapping}s: 
     * they will be based on relations holding between <strong>all</strong> of 
     * these {@code Species}. For instance, if {@code #getEvoRelationType()} returns 
     * {@code HOMOLOGY}, this would be equivalent to using homology relations 
     * defined for their closest common ancestor {@code Taxon}, and all its ancestor 
     * taxa (so, to identify all structures in those {@code Species}, derived from 
     * structures existing in their common ancestor).
     * <p>
     * And in any case, the {@code AnatDevMapping}s obtained will only contain 
     * {@code AnatDevEntity}s existing in at least one of these {@code Species}.
     * 
     * @param speciesRestriction   A {@code Species} to be added to the {@code Collection}
     *                              of {@code Species} used to restrain the 
     *                              {@code AnatDevMapping}s retrieved, 
     *                              and their contained {@code AnatDevEntity}s.
     * @see #getSpeciesRestrictions()
     */
    public void addSpeciesRestriction(Species speciesRestriction) {
        this.species.add(speciesRestriction);
    }
    /**
     * Add {@code speciesRestriction} to the {@code Collection} of {@code Species} 
     * used to provide restrictions on the {@code AnatDevMapping}s to retrieve, 
     * and/or on the {@code AnatDevEntity}s they contain. 
     * <p>
     * These {@code Species} are used to select the relevant {@code AnatDevMapping}s: 
     * they will be based on relations holding between <strong>all</strong> of 
     * these {@code Species}. For instance, if {@code #getEvoRelationType()} returns 
     * {@code HOMOLOGY}, this would be equivalent to using homology relations 
     * defined for their closest common ancestor {@code Taxon}, and all its ancestor 
     * taxa (so, to identify all structures in those {@code Species}, derived from 
     * structures existing in their common ancestor).
     * <p>
     * And in any case, the {@code AnatDevMapping}s obtained will only contain 
     * {@code AnatDevEntity}s existing in at least one of these {@code Species}.
     * 
     * @param speciesRestrictions   A {@code Collection} of {@code Species} to be added 
     *                              to the {@code Species} used to restrain the 
     *                              {@code AnatDevMapping}s retrieved, 
     *                              and their contained {@code AnatDevEntity}s.
     * @see #getSpeciesRestrictions()
     */
    public void addAllSpeciesRestrictions(Collection<Species> speciesRestrictions) {
        this.species.addAll(speciesRestrictions);
    }
    /**
     * Returns the {@code Set} of {@code Species} used to provide restrictions 
     * on the {@code AnatDevMapping}s to retrieve, and/or on the {@code AnatDevEntity}s 
     * they contain. 
     * <p>
     * These {@code Species} are used to select the relevant {@code AnatDevMapping}s: 
     * they will be based on relations holding between <strong>all</strong> of 
     * these {@code Species}. For instance, if {@code #getEvoRelationType()} returns 
     * {@code HOMOLOGY}, this would be equivalent to using homology relations 
     * defined for their closest common ancestor {@code Taxon}, and all its ancestor 
     * taxa (so, to identify all structures in those {@code Species}, derived from 
     * structures existing in their common ancestor).
     * <p>
     * And in any case, the {@code AnatDevMapping}s obtained will only contain 
     * {@code AnatDevEntity}s existing in at least one of these {@code Species}.
     * 
     * @return  the {@code Collection} of {@code Species} used to define restrictions 
     *          on the {@code AnatDevMapping}s to retrieve, and their contained 
     *          {@code AnatDevEntity}s.
     */
    public Set<Species> getSpecies() {
        return this.species;
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
