package org.bgee.model.anatdev.evomapping;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.Entity;
import org.bgee.model.ontologycommon.Confidence;
import org.bgee.model.ontologycommon.EvidenceCode;

/**
 * This class allows to store all the elements supporting an evolutionary assertion: 
 * an {@link org.bgee.model.ontologycommon.EvidenceCode EvidenceCode}, 
 * a {@link org.bgee.model.ontologycommon.Confidence Confidence}, one or several 
 * {@link References}, and a supporting text.
 * <p>
 * It can be used as part of a {@link AnatDevMapping}, that groups {@code AnatDevEntity}s 
 * related by an evolutionary relation.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 *
 */
public class AssertionSupport {
	/**
     * <code>Logger/code> of this class.
     */
    private final static Logger log = 
    		LogManager.getLogger(AssertionSupport.class.getName());
	/**
	 * A reference where the information supporting an assertion comes from, 
	 * used as part of an {@link AssertionSupport}.
	 * 
	 * @author Frederic Bastian
	 * @version Bgee 13
	 * @since Bgee 13
	 */
	protected class Reference extends Entity {

		/**
	     * Constructor providing the {@code id} of this {@code Reference}. 
	     * This {@code id} cannot be {@code null}, or empty (""), 
	     * or whitespace only, otherwise an {@code IllegalArgumentException} 
	     * will be thrown. The ID will also be immutable, see {@link #getId()}.
	     * <p>
	     * In the case of a {@code Reference}, this {@code id} is a DOI, 
	     * or an ISBN, or a Pubmed ID, otherwise, an {@code IllegalArgumentException} 
	     * is thrown. It is recommended to provide the tile as name 
	     * (using {@link #setName(String)}).
	     * 
	     * @param id	A {@code String} representing the ID of 
	     * 				this {@code Reference}.
	     * @throws IllegalArgumentException 	if {@code id} is {@code null},  
	     * 										empty, whitespace only, or is not a DOI, 
	     * 										an ISBN, nor a Pubmed ID. 
	     */
		protected Reference(String id) throws IllegalArgumentException {
			super(id);
			if (!this.isDOI() && !this.isISBN() && !this.isPMID()) {
				throw log.throwing(new IllegalArgumentException(
					"The ID provided to instantiate a Reference is neither a DOI, " +
					"nor an ISBN, nor a Pubmed ID."));
			}
		}
		
		/**
		 * @return 	{@code true} if the ID of this {@code Reference} 
		 * 			(returned by {@link #getId()}) is a DOI.
		 */
		public boolean isDOI() {
			return this.getId().toLowerCase().startsWith("doi");
		}
		/**
		 * @return 	{@code true} if the ID of this {@code Reference} 
		 * 			(returned by {@link #getId()}) is an ISBN.
		 */
		public boolean isISBN() {
			return this.getId().toLowerCase().startsWith("isbn");
		}
		/**
		 * @return 	{@code true} if the ID of this {@code Reference} 
		 * 			(returned by {@link #getId()}) is a Pubmed ID.
		 */
		public boolean isPMID() {
			return this.getId().toLowerCase().startsWith("pmid");
		}
	}
	

	/**
     * The {@code EvidenceCode} supporting this assertion. 
     */
    private final EvidenceCode evidenceCode;
    /**
     * The {@code Confidence} representing confidence in this assertion.
     */
    private final Confidence confidence;
    /**
     * A {@code String} that is a text supporting this assertion.
     */
    private final String supportingText;
    /**
     * A {@code Collection} of {@code Reference}s 
     * where information about this assertion comes from.
     */
    private final Collection<Reference> references;
	
	/**
	 * Constructor private, at least an {@code EvidenceCode}, 
	 * a {@code Confidence}, a {@code Reference}, and a 
	 * supporting text must be provided.
	 */
	@SuppressWarnings("unused")
	private AssertionSupport() {
		this(null, null, null, (Collection<Reference>) null);
	}
	
	/**
	 * When instantiating an {@code AssertionSupport}, at least an 
	 * {@code EvidenceCode}, a {@code Confidence}, a {@code Reference},  
	 * and a supporting text must be provided. 
	 * 
	 * @param evidenceCode		The {@code EvidenceCode} supporting this assertion.
	 * @param confidence		The {@code Confidence} representing confidence 
	 * 							in this assertion.
	 * @param supportingText	A {@code String} that is the supporting text 
	 * 							for this assertion.
	 * @param reference			a {@code Reference} where information about 
	 * 							this assertion comes from.
	 */
	public AssertionSupport(EvidenceCode evidenceCode, Confidence confidence, 
			String supportingText, Reference reference) {
		this(evidenceCode, confidence, supportingText, 
				Collections.singletonList(reference));
	}
	/**
	 * When instantiating an {@code AssertionSupport}, at least an 
	 * {@code EvidenceCode}, a {@code Confidence}, a {@code Reference},  
	 * and a supporting text must be provided. 
	 * 
	 * @param evidenceCode		The {@code EvidenceCode} supporting this assertion.
	 * @param confidence		The {@code Confidence} representing confidence 
	 * 							in this assertion.
	 * @param supportingText	A {@code String} that is the supporting text 
	 * 							for this assertion.
	 * @param references		a {@code Collection} of {@code Reference}s 
	 * 							where information about this assertion comes from.
	 */
	public AssertionSupport(EvidenceCode evidenceCode, Confidence confidence, 
			String supportingText, Collection<Reference> references) {
		this.evidenceCode   = evidenceCode;
		this.confidence     = confidence;
		this.supportingText = supportingText;
		this.references = new ArrayList<Reference>();
		this.references.addAll(references);
	}

	/**
	 * @return The {@code EvidenceCode} supporting this assertion. 
	 */
	public EvidenceCode getEvidenceCode() {
		return evidenceCode;
	}
	/**
	 * @return The {@code Confidence} representing confidence in this assertion.
	 */
	public Confidence getConfidence() {
		return confidence;
	}
	/**
	 * @return	A {@code Collection} of {@code Reference}s 
     * 			where information about this assertion comes from.
	 */
	public Collection<Reference> getReferences() {
		return references;
	}
	/**
	 * @return A {@code String} that is the supporting text for this assertion.
	 */
	public String getSupportingText() {
		return supportingText;
	}
}
