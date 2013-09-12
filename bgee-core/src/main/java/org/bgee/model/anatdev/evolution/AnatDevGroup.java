package org.bgee.model.anatdev.evolution;

import java.util.Collection;
import java.util.Set;

import org.bgee.model.anatdev.AnatDevEntity;
import org.bgee.model.species.Taxon;

public class AnatDevGroup<T extends AnatDevEntity> {
	/**
	 * Represents the different type of evolutionary transitive relations. 
	 * They are taken from the 
	 * <a href='http://www.obofoundry.org/cgi-bin/detail.cgi?id=homology_ontology'>
	 * HOM ontology</a>, but as long as we do not use more concepts, we will 
	 * simply used this <code>enum</code>.
	 * 
	 * @author Frederic Bastian
	 * @version Bgee 13
     * @since Bgee 13
	 */
    public enum TransRelationType {
    	HOMOLOGY, HOMOPLASY;
    }
    to continue, constructors (all attributes final, or provide setters?), and class javadoc
    /**
     * A <code>Set</code> of <code>T</code>, representing the {@link AnatDevEntity}s 
     * related by an evolutionary relation. 
     * <p>
     * Most of the time, it will contain only one <code>AnatDevEntity</code>; 
     * for instance, the <code>AnatomicalEntity</code> "cranium" has a {@link #relationType} 
     * <code>HOMOLOGY</code>, standing in the {@link #taxon} "Craniata"; 
     * this <code>Set</code> would then contain only one entry.
     * <p>
     * But as another example, in the case of the homology between "lung" and 
     * "swim bladder", it does not exist any <code>AnatomicalEntity</code> 
     * in the <code>AnatomicalOntology</code> representing the common ancestral 
     * structure which these organs originated from. So this <code>Set</code> 
     * would then contain two entries.
     * <p>
     * Of note, as expressed by the generic type, this <code>Set</code> can also 
     * contain <code>Stage</code>s. This is because broad stages, such as "embryo", 
     * are considered homologous for the sake of performing expression comparisons 
     * in different species using developmental time too. 
     * @see #relationType
     * @see #taxon
     */
    private Set<T> relatedEntities;
    /**
     * A <code>TransRelationType</code> defining the type of relation linking 
     * the {@link #relatedEntities}, at the taxonomical range provided by {@link #taxon}.
     * @see #relatedEntities
     * @see #taxon
     */
    private TransRelationType relationType;
    /**
     * A <code>Taxon</code> providing the scope of this evolutionary relation.
     * For instance, if {@link #relationType} is <code>HOMOLOGY</code>, 
     * this attribute designs the taxon of the common ancestor that 
     * the {@link #relatedEntities} were inherited from.
     * @see #relatedEntities
     * @see #relationType
     */
    private Taxon taxon;
    /**
     * A <code>Collection</code> of <code>AssertionSupport</code>s providing 
     * the supporting information for this evolutionary relation between 
     * the {@link #relatedEntities}. Most of the time, only one 
     * <code>AssertionSupport</code> is backing up this assertion, but several 
     * can be provided (for instance, several types of evidence supporting 
     * an assertion).
     */
    private Collection<AssertionSupport> supportingInformation;
    
}
