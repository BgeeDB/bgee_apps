package org.bgee.model.anatdev.evolution;

import java.util.Set;

import org.bgee.model.ontologycommon.Confidence;
import org.bgee.model.ontologycommon.EvidenceCode;
import org.bgee.model.species.Taxon;

/**
 * Represent an evolutionary transitive relation, for instance, 
 * "historical homology", or "homoplasy".
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public class EvoTransRelation {
	/**
	 * Represents the different type of evolutionary transitive relations. 
	 * THey are taken from the 
	 * <a href='http://www.obofoundry.org/cgi-bin/detail.cgi?id=homology_ontology'>
	 * HOM ontology</a>, but as long as we do not use more concepts, we will 
	 * simply used this <code>enum</code>.
	 * 
	 * @author Frederic Bastian
	 * @version Bgee 13
     * @since Bgee 13
	 */
    public enum RelationType {
    	HOMOLOGY, HOMOPLASY;
    }
    
    Hmm ,en fait tout ça c'est plutot les attributs d'un groupe d'organes, 
    pas de la relation elle-meme
    /**
     * A <code>RelationType</code> defining the type of relation to use.
     */
    private RelationType relationType;
    /**
     * A <code>Taxon</code> providing the scope of this evolutionary relation.
     */
    private Taxon taxon;
    /**
     * The <code>EvidenceCode</code> supporting this relation. 
     */
    private EvidenceCode evidenceCode;
    /**
     * The <code>Confidence</code> representing confidence in this relation.
     */
    private Confidence confidence;
    /**
     * A <code>String</code> that is a text supporting this relation, 
     * from one of the related reference.
     */
    private String supportingText;
    /**
     * A <code>Set</code> containing <code>String</code>s that are the references 
     * where information about this relation comes from, in the form: 
     * <code>DOI/ISBN/URL "Author list, title"</code>.
     */
    private Set<String> references;
}
