package org.bgee.model.anatdev.evomapping;

import java.util.Collection;
import java.util.Set;

import org.bgee.model.anatdev.AnatDevElement;
import org.bgee.model.anatdev.core.AnatDevEntity;
import org.bgee.model.anatdev.evomapping.EvoMappingSelector.TransRelationType;
import org.bgee.model.species.Taxon;

/**
 * Group of {@link AnatDevEntity}s, related by a transitive evolutionary relation 
 * of a type {@link TransRelationType} (for instance, {@code HOMOLOGY}). 
 * This class allows to store the linked {@code AnatDevEntity}s (see 
 * {@link #getMainEntites()}), the type of the relation between them (see 
 * {@link #getRelationType()}), the {@link Taxon} specifying the scope of 
 * the relation (see {@link #getTaxon()}), and the {@link AssertionSupport}s 
 * supporting this relation (see {@link #getSupportingInformation()}). 
 * It also allows to store related entities (see below, and 
 * {@link #getRelatedEntities()}).
 * <p>
 * Most of the time, an {@code AnatDevMapping} will contain only one 
 * main {@code AnatDevEntity} (see {@link #getMainEntites()}); for instance, 
 * the {@link org.bgee.model.anatdev.AnatomicalEntity AnatEntity} "cranium" 
 * has a {@code TransRelationType} {@code HOMOLOGY}, standing in the 
 * {@code Taxon} "Craniata", meaning that "cranium" first evolved in 
 * the taxon "Craniata". This {@code AnatDevMapping} would then only contain 
 * one main {@code AnatEntity}, "cranium".
 * <p>
 * But as another example, in the case of the homology between "lung" and 
 * "swim bladder", it does not exist any {@code AnatEntity} 
 * in the {@code AnatomicalOntology}, representing the common ancestral 
 * structure which these organs originated from. So the {@code AnatDevMapping} 
 * would contain these two {@code AnatEntity}s as main entities.
 * <p>
 * This class also allows to store related {@code AnatDevEntity}s (see 
 * {@link #getRelatedEnties()}): these are {@code AnatDevEntity}s that should 
 * be grouped along with the main {@code AnatDevEntity}s, but are not annotated 
 * as such. Consider for instance an {@code AnatDevMapping} representing 
 * the homology of "brain" in the taxon "Chordata". The term "future brain" is not 
 * annotated as homologous in "Chordata" in Bgee, only the term "brain" is, but it would 
 * be good to consider it as well. In that case, the "future brain" will be retrieved 
 * thanks to the {@link org.bgee.model.ontologycommon.Ontology.RelationType TRANSFORMATION_OF} 
 * relation between "brain" and "future brain", and will be stored as a related entity, 
 * so that expression comparison could be made on it as well.
 * <p>
 * Of note, as expressed by the generic type, an {@code AnatDevMapping} can also 
 * contain {@link org.bgee.model.anatdev.Stage DevStage}s. This is because broad stages, 
 * such as "embryo", are considered homologous for the sake of performing expression 
 * comparisons in different species using developmental time too. 
 * <p>
 * Also, a fully "formed" {@code AnatDevMapping} should always contain a 
 * {@code TransRelationType}, a {@code Taxon}, and some 
 * {@code AssertionSupport}s. But we leave opened the possibility to only specify 
 * the main {@code AnatDevEntity}s, to be able to create "fake" groupings, 
 * in order for instance to test alternative homology hypotheses, or to provide 
 * randomized relations.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 *
 * @param <T>	The type of {@code AnatDevEntity} that this 
 * 				{@code AnatDevMapping} contains.
 */
public class AnatDevMapping<T extends AnatDevEntity> implements AnatDevElement {
	/**
     * A {@code Set} of {@code T}, representing the {@link AnatDevEntity}s 
     * grouped by an evolutionary relation. 
     * <p>
     * Most of the time, it will contain only one {@code AnatDevEntity}; 
     * for instance, the {@code AnatEntity} "cranium" has a {@link #relationType} 
     * {@code HOMOLOGY}, standing in the {@link #taxon} "Craniata"; 
     * this {@code Set} would then contain only one entry.
     * <p>
     * But as another example, in the case of the homology between "lung" and 
     * "swim bladder", it does not exist any {@code AnatEntity} 
     * in the {@code AnatomicalOntology} representing the common ancestral 
     * structure which these organs originated from. So this {@code Set} 
     * would then contain these two entries.
     * <p>
     * Related entities that should be grouped along with #mainEntities, but are not 
     * annotated as such, are stored in {@link #relatedEntities}, see this attribute 
     * for more details.
     * <p>
     * Of note, as expressed by the generic type, this {@code Set} can also 
     * contain {@code DevStage}s. This is because broad stages, such as "embryo", 
     * are considered homologous for the sake of performing expression comparisons 
     * in different species using developmental time too. 
     * 
     * @see #relatedEntities
     * @see #relationType
     * @see #taxon
     */
    private Set<T> mainEntities;
    
    /**
     * A {@code Set} of {@code T}, representing the {@link AnatDevEntity}s 
     * that should be grouped along with {@link #mainEntities}, but are not annotated 
     * as such. Consider for instance an {@code AnatDevMapping} representing 
     * the homology of "brain" in the taxon "Chordata". The term "future brain" is not 
     * annotated as homologous in "Chordata" in Bgee, only the term "brain" is, but it would 
     * be good to consider it as well. In that case, the "future brain" will be retrieved 
     * thanks to the {@link org.bgee.model.ontologycommon.Ontology.RelationType 
     * TRANSFORMATION_OF} relation between "brain" and "future brain", and will be stored as 
     * a related entity, so that expression comparison could be made on it as well.
     * <p>
     * {@code AnatDevEntity}s stored in this {@code Set} never have 
     * themselves an annotated relation of the type {@link #relationType} in Bgee.
     * 
     * @see #mainEntities
     */
    private Set<T> relatedEntities;
    /**
     * A {@code TransRelationType} defining the type of relation linking 
     * the {@link #mainEntities}, at the taxonomical range provided by {@link #taxon}.
     * @see #relatedEntities
     * @see #taxon
     */
    private TransRelationType relationType;
    /**
     * A {@code Taxon} providing the scope of this evolutionary relation.
     * For instance, if {@link #relationType} is {@code HOMOLOGY}, 
     * this attribute designs the taxon of the common ancestor that 
     * the {@link #mainEntities} were inherited from.
     * @see #relatedEntities
     * @see #relationType
     */
    private Taxon taxon;
    /**
     * A {@code Collection} of {@code AssertionSupport}s providing 
     * the supporting information for this evolutionary relation between 
     * the {@link #mainEntities}. Most of the time, only one 
     * {@code AssertionSupport} is backing up this assertion, but several 
     * can be provided (for instance, several types of evidence supporting 
     * an assertion).
     */
    private Collection<AssertionSupport> supportingInformation;
    @Override
    public void registerWithId(String id) {
        // TODO Auto-generated method stub
        
    }
    
}
