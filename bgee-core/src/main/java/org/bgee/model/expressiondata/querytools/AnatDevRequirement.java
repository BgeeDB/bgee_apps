package org.bgee.model.expressiondata.querytools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bgee.model.expressiondata.DataParameters.CallType;
import org.bgee.model.expressiondata.querytools.filters.CallFilter;
import org.bgee.model.gene.Gene;

/**
 * List and allow to validate conditions of gene expression data 
 * on {@link org.bgee.model.anatdev.AnatDevEntity AnatDevEntity}s, 
 * when performing an expression reasoning using an {@link AnatDevExpressionQuery}. 
 * These conditions define which genes should have which types of expression data, 
 * for instance: "Validate anatomical structures where gene A is expressed, 
 * gene B not expressed, and gene C over-expressed". 
 * <p>
 * Each condition is described by a {@link GeneCallRequirement}. 
 * How this <code>GeneCallRequirement</code>s are used to validate this 
 * <code>AnatDevRequirement</code> is defined by the value returned by 
 * {@link #getValidationType()}.
 * <p>
 * Note that an <code>AnatDevExpressionQuery</code> can use several 
 * <code>AnatDevRequirement</code>s, offering different ways of validating 
 * an <code>AnatDevEntity</code>. 
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 *
 */
public class AnatDevRequirement {
	/**
	 * An <code>enum</code> to define how this <code>AnatDevRequirement</code> 
	 * should be validated: 
	 * <ul>
	 * <li><code>ALL</code>: all <code>GeneCallRequirement</code>s must be validated.
	 * <li><code>CONSERVATION</code>: all <code>Gene</code>s part of the 
	 * <code>GeneCallRequirement</code>s must have the same <code>CallType</code> 
	 * in a given <code>AnatDevEntity</code> for it to be validated. This allows 
	 * for instance to request for both <code>EXPRESSION</code> and <code>NOEXPRESSION</code> 
	 * data, but to get only <code>AnatDevEntity</code>s where <code>Gene</code>s 
	 * are similarly expressed. 
	 * <li><code>DIVERGENCE</code>: at least on of the <code>Gene</code>s part of the 
	 * <code>GeneCallRequirement</code>s must have a <code>CallType</code> different  
	 * from the other in a given <code>AnatDevEntity</code>, for it to be validated. 
	 * This allows for instance to request for both <code>EXPRESSION</code> and 
	 * <code>NOEXPRESSION</code> data, but to get only <code>AnatDevEntity</code>s 
	 * where gene expression is divergent. 
	 * <li><code>GENETHRESHOLD</code>: the <code>AnatDevRequirement</code> 
	 * will be validated based on the number of <code>Gene</code>s (defined 
	 * using {@link AnatDevRequirement#setValidationThreshold(int)}), 
	 * amongst all the <code>Gene</code>s present in the <code>GeneCallRequirement</code>s, 
	 * that exhibit the reference <code>CallType</code> (defined using 
	 * {@link AnatDevRequirement#setReferenceCallType(CallType)}).
	 * <li><code>SPECIESTHRESHOLD</code>: the <code>AnatDevRequirement</code> 
	 * will be validated based on the number of <code>Species</code>s (defined 
	 * using {@link AnatDevRequirement#setValidationThreshold(int)}), 
	 * amongst all the <code>Species</code>s represented by the <code>Gene</code>s 
	 * in the <code>GeneCallRequirement</code>s, that exhibit the reference 
	 * <code>CallType</code> (set using 
	 * {@link AnatDevRequirement#setReferenceCallType(CallType)}).
	 * </ul>
	 * 
	 * @author Frederic Bastian
	 * @version Bgee 13
	 * @see AnatDevRequirement
	 * @see GeneCallRequirement
	 * @since Bgee 13
	 */
	public enum ValidationType {
		ALL, CONSERVATION, DIVERGENCE, GENETHRESHOLD, SPECIESTHRESHOLD;
	}
	
	/**
	 * A <code>Collection</code> of <code>GeneCallRequirement</code>s 
	 * defining gene expression data to retrieve and conditions to satisfy 
	 * for a  <code>AnatDevEntity</code> to be validated, when performing 
     * an expression reasoning using an {@link AnatDevExpressionQuery}.
	 */
	private final Collection<GeneCallRequirement> requirements;
	/**
	 * A <code>ValidationType</code> to define how this <code>AnatDevRequirement</code> 
	 * should be validated.
	 * 
	 * @see #validationThreshold
	 * @see #referenceCall
	 */
	private ValidationType validationType;
	/**
	 * An <code>int</code> defining a threshold to validate this 
	 * <code>AnatDevRequirement</code>, regarding the number of <code>Gene</code>s 
	 * or of <code>Species</code>s (depending on {@link #validationType}), 
	 * that must exhibit the reference <code>CallType</code> of {@link #referenceCall}.
	 * If {@link #validationType} is not equal to <code>GENETHRESHOLD</code> or 
	 * <code>SPECIESTHRESHOLD</code>, this attribute is not used. 
	 * 
	 * @see #validationType
	 * @see #referenceCall
	 */
	private int validationThreshold;
	/**
	 * A <code>CallType</code> that must be exhibited by a defined number of 
	 * <code>Gene</code> or <code>Species</code>s, for this <code>AnatDevRequirement</code> 
	 * to be validated. {@link #validationThreshold} defines the number, 
	 * {@link #validationType} defines whether <code>Gene</code> or <code>Species</code>s 
	 * should be used. If {@link #validationType} is not equal to 
	 * <code>GENETHRESHOLD</code> or <code>SPECIESTHRESHOLD</code>, this attribute 
	 * is not used.
	 *  
	 * @see #validationType
	 * @see #validationThreshold
	 */
	private CallType referenceCall;
	
	/**
	 * Default constructor. 
	 */
	public AnatDevRequirement() {
		this.requirements = new ArrayList<GeneCallRequirement>();
	}

	/**
	 * Return the <code>ValidationType</code> defining how this 
	 * <code>AnatDevRequirement</code> should be validated.
	 * 
	 * @return 	the <code>ValidationType</code> to validate 
	 * 			this <code>AnatDevRequirement</code>.
	 * @see #getValidationThreshold()
	 * @see #getReferenceCall()
	 */
	public ValidationType getValidationType() {
		return this.validationType;
	}
	/**
	 * Set the <code>ValidationType</code> defining how this 
	 * <code>AnatDevRequirement</code> should be validated. 
	 * If <code>validationType</code> is equal to <code>GENETHRESHOLD</code> 
	 * or <code>SPECIESTHRESHOLD</code>, then a threshold must be set using 
	 * {@link #setValidationThreshold(int)} and a reference <code>CallType</code> 
	 * using {@link #setReferenceCall(CallType)}.
	 * 
	 * @param validationType 	A <code>ValidationType</code> defining how this 
	 * 							<code>AnatDevRequirement</code> should be validated. 
	 * @see #setValidationThreshold(int)
	 * @see #setReferenceCall(CallType) 
	 */
	public void setValidationType(ValidationType validationType) {
		this.validationType = validationType;
	}

	/**
	 * Get the <code>int</code> defining the threshold to validate this 
	 * <code>AnatDevRequirement</code>, regarding the number of <code>Gene</code>s 
	 * or of <code>Species</code>s (depending on the value returned by 
	 * {@link #getValidationType()}), that must exhibit the reference <code>CallType</code> 
	 * (that can be obtained using {@link #getReferenceCall()}.
	 * If the value returned by {@link #getValidationType()} is not equal to 
	 * <code>GENETHRESHOLD</code> nor <code>SPECIESTHRESHOLD</code>, 
	 * this parameter is not used. 
	 * 
	 * @return 	the <code>int</code> defining the threshold to validate this 
	 *			<code>AnatDevRequirement</code>
	 * @see #getValidationType()
	 * @see #getReferenceCall()
	 */
	public int getValidationThreshold() {
		return this.validationThreshold;
	}
	/**
	 * Set the <code>int</code> defining the threshold to validate this 
	 * <code>AnatDevRequirement</code>, regarding the number of <code>Gene</code>s 
	 * or of <code>Species</code>s (depending on the value returned by 
	 * {@link #getValidationType()}), that must exhibit the reference <code>CallType</code> 
	 * (that can be obtained using {@link #getReferenceCall()}.
	 * If the value returned by {@link #getValidationType()} is not equal to 
	 * <code>GENETHRESHOLD</code> nor <code>SPECIESTHRESHOLD</code>, 
	 * this parameter is not used. 
	 * 
	 * @param threshold 	A <code>int</code> to define the threshold to validate 
	 * 						this <code>AnatDevRequirement</code>.
	 * @see #setValidationType(ValidationType)
	 * @see #setReferenceCall(CallType)
	 */
	public void setValidationThreshold(int threshold) {
		this.validationThreshold = threshold;
	}

	/**
	 * Get the <code>CallType</code> that must be exhibited by a defined number of 
	 * <code>Gene</code>s or <code>Species</code>s, for this <code>AnatDevRequirement</code> 
	 * to be validated. The value returned by {@link #getValidationThreshold()} 
	 * defines the number, {@link #getValidationType()} defines whether <code>Gene</code>s 
	 * or <code>Species</code>s should be used. 
	 * If the value returned by {@link #getValidationType()} is not equal to 
	 * <code>GENETHRESHOLD</code> nor <code>SPECIESTHRESHOLD</code>, 
	 * this parameter is not used. 
	 * 
	 * @return 	the reference <code>CallType</code>, used depending on the value 
	 * 			returned by {@link #getValidationType()}
	 * @see #getValidationType()
	 * @see #getValidationThreshold()
	 */
	public CallType getReferenceCall() {
		return this.referenceCall;
	}
	/**
	 * Set the <code>CallType</code> that must be exhibited by a defined number of 
	 * <code>Gene</code>s or <code>Species</code>s, for this <code>AnatDevRequirement</code> 
	 * to be validated. The value returned by {@link #getValidationThreshold()} 
	 * defines the number, {@link #getValidationType()} defines whether <code>Gene</code>s 
	 * or <code>Species</code>s should be used. 
	 * If the value returned by {@link #getValidationType()} is not equal to 
	 * <code>GENETHRESHOLD</code> nor <code>SPECIESTHRESHOLD</code>, 
	 * this parameter is not used. 
	 * 
	 * @return 	the reference <code>CallType</code>, used depending on the value 
	 * 			returned by {@link #getValidationType()}
	 * 
	 * @param referenceCall the reference <code>CallType</code>, that will be used 
	 * 						depending on the value returned by {@link #getValidationType()}.
	 * @see #setValidationType(ValidationType)
	 * @see #setValidationThreshold(int)
	 */
	public void setReferenceCall(CallType referenceCall) {
		this.referenceCall = referenceCall;
	}

	/**
	 * Get the <code>Collection</code> of <code>GeneCallRequirement</code>s, 
	 * defining gene expression data to retrieve and conditions to satisfy 
	 * for a  <code>AnatDevEntity</code> to be validated, when performing 
     * an expression reasoning using an {@link AnatDevExpressionQuery}.
	 * 
	 * @return 	the <code>Collection</code> of <code>GeneCallRequirement</code>s
	 * 			associated to this <code>AnatDevRequirement</code>. 
	 */
	public Collection<GeneCallRequirement> getRequirements() {
		return this.requirements;
	}
	/**
	 * Add a <code>GeneCallRequirement</code> to this <code>AnatDevRequirement</code>, 
	 * defining gene expression data to retrieve and conditions to satisfy 
	 * for a  <code>AnatDevEntity</code> to be validated, when performing 
     * an expression reasoning using an {@link AnatDevExpressionQuery}.
     * 
	 * @param requirement	A <code>GeneCallRequirement</code> to be added 
	 * 						to this <code>AnatDevRequirement</code>.
	 * @see #addRequirements(Collection)
	 * @see #setValidationType(ValidationType)
	 */
	public void addRequirement(GeneCallRequirement requirement) {
		this.requirements.add(requirement);
	}
	/**
	 * Add <code>GeneCallRequirement</code>s to this <code>AnatDevRequirement</code>, 
	 * defining gene expression data to retrieve and conditions to satisfy 
	 * for a  <code>AnatDevEntity</code> to be validated, when performing 
     * an expression reasoning using an {@link AnatDevExpressionQuery}.
     * 
	 * @param requirements	A <code>Collection</code> of <code>GeneCallRequirement</code>s 
	 * 						to be added to this <code>AnatDevRequirement</code>.
	 * @see #addRequirement(GeneCallRequirement)
	 * @see #setValidationType(ValidationType)
	 */
	public void addRequirements(Collection<GeneCallRequirement> requirements) {
		this.requirements.addAll(requirements);
	}

	/**
     * Define custom conditions for an <code>AnatDevEntity</code> 
     * to be validated, regarding its gene expression data, when performing 
     * an expression reasoning using an {@link AnatDevExpressionQuery}. 
     * A <code>GeneCallRequirement</code> is always part of a {@link AnatDevRequirement}, 
     * listing all the necessary conditions that an <code>AnatDevEntity</code> 
     * must satisfied at the same time to be validated.
     * <p>
     * A <code>GeneCallRequirement</code> lists <code>Gene</code>s, associated to 
     * <code>CallFilter</code>s, to define which <code>Gene</code>s should have 
     * what kind of expression data for an <code>AnatDevEntity</code> to be validated. 
     * For instance, using <code>GeneCallRequirement</code>s, it is possible 
     * to query for <code>AnatDevEntity</code>s exhibiting expression of a gene A, 
     * expression or absence of expression of a gene B, and over-expression of a gene C, 
     * etc.
     * <p>
     * It can include several <code>Gene</code>s, or only one, and a <code>Gene</code> 
     * can be associated to several <code>CallFilter</code>s, or only one. 
     * It is possible to define whether all <code>CallFilter</code>s should be satisfied 
     * at the same time for a <code>Gene</code> to be validated, and whether 
     * all <code>Gene</code>s should be validated, or only once, for a 
     * <code>GeneCallRequirement</code> to be validated. 
     * 
	 * @author Frederic Bastian
	 * @see AnatDevRequirement
	 * @version Bgee 13
	 * @since Bgee 13
     *
     */
    public class GeneCallRequirement {
    	
    	/**
    	 * A <code>Map</code> associating each <code>Gene</code> in the key set, 
    	 * to a <code>Collection</code> of <code>CallFilter</code>s. 
    	 * The <code>CallFilter</code>s define for each <code>Gene</code> 
    	 * the expression data to retrieve for it. 
    	 * <p>
    	 * Whether all expression data requirements for a <code>Gene</code> 
    	 * must be satisfied, or only at least one, is defined 
    	 * by {@link #satisfyAllCallFilters}. 
    	 * <p>
    	 * Whether all <code>Gene</code>s must have their data requirements satisfied 
    	 * for this <code>GeneCallRequirement</code>, or only at least one of them, 
    	 * is defined by {@link #satisfyAllGenes}. 
    	 * <p>
    	 * See {@link #satisfyAllCallFilters} and {@link #satisfyAllGenes} 
    	 * for more information.
    	 * 
    	 * @see #satisfyAllCallFilters
    	 * @see #satisfyAllGenes
    	 */
    	private final Map<Gene, Collection<CallFilter>> genesWithParameters;
    	
    	/**
    	 * A <code>boolean</code> defining whether, when a <code>Gene</code> 
    	 * in <code>genesWithParameters</code> is associated to several 
    	 * <code>CallFilter</code>s, all of them must be satisfied for 
    	 * the <code>Gene</code> to be validated, or only at least one of them.
    	 * <p>
    	 * The recommended value is <code>false</code>. Setting this attribute 
    	 * to <code>true</code> should be useful only in specific cases, 
    	 * such as, to investigate contradictions between data types; for instance, 
    	 * by requesting <code>EXPRESSION</code> data from <code>AFFYMETRIX</code>, 
    	 * and at the same time, <code>NOEXPRESSION</code> data from <code>RNA-Seq</code>.
    	 * 
    	 * @see #genesWithParameters
    	 * @see #satisfyAllGenes
    	 */
    	private boolean satisfyAllCallFilters;

    	/**
    	 * A <code>boolean</code> defining whether, when several <code>Genes</code> 
    	 * are present in <code>genesWithParameters</code>, all of them 
    	 * must be satisfied for this <code>GeneCallRequirement</code> to be validated, 
    	 * or only at least one of them. Whether the requirements are satisfied 
    	 * for a given <code>Gene</code> is defined by its associated 
    	 * <code>CallFilter</code>s in <code>genesWithParameters</code> and the value of 
    	 * <code>satisfyAllCallFilters</code>. 
    	 * <p>
    	 * The recommended value is <code>true</code>.
    	 * 
    	 * @see #genesWithParameters
    	 * @see #satisfyAllCallFilters
    	 */
    	private boolean satisfyAllGenes;
    	
    	/**
    	 * Default constructor. 
    	 */
    	public GeneCallRequirement() {
    		this.genesWithParameters = new HashMap<Gene, Collection<CallFilter>>();
			this.setSatisfyAllGenes(false);
			this.setSatisfyAllCallFilters(false);
    	}
    	/**
    	 * Instantiate a <code>AnatDevRequirement</code> with requirements 
    	 * on one <code>Gene</code>, associated to one <code>CallFilter</code>. 
    	 * This is equivalent to calling: 
    	 * <ul>
    	 * <li><code>addGene(gene, filter)</code>
    	 * </ul>
    	 * 
    	 * @param gene		A <code>Gene</code> to be part of
    	 * 					this <code>GeneCallRequirement</code>. 
    	 * @param filter	A <code>CallFilter</code> to be associated to <code>gene</code>.
    	 */
    	public GeneCallRequirement(Gene gene, CallFilter filter) {
			this();
			this.addGene(gene, filter);
    	}
    	/**
    	 * Instantiate a <code>AnatDevRequirement</code> with requirements 
    	 * on several <code>Gene</code>s (with the condition of validating all of them), 
    	 * with each of them associated to a same <code>Collection</code> 
    	 * of <code>CallFilter</code>s (with the condition of validating any of them). 
    	 * This is equivalent to calling: 
    	 * <ul>
    	 * <li><code>addGenes(genes, filters)</code>
    	 * <li><code>setSatisfyAllGenes(true)</code>
    	 * <li><code>setSatisfyAllCallFilters(false)</code>
    	 * </ul>
    	 * 
    	 * @param genes		A <code>Collection</code> of <code>Gene</code>s to be part of
    	 * 					this <code>GeneCallRequirement</code>. They must all 
    	 * 					be validated for this <code>GeneCallRequirement</code> 
    	 * 					to be satisfied. 
    	 * @param filters	A <code>Collection</code> of <code>CallFilter</code>s,  
    	 * 					that are the requirements associated to each <code>Gene</code>.
    	 * 					Any of them must be satisfied for a <code>Gene</code> 
    	 * 					to be validated.  
    	 */
    	public GeneCallRequirement(Collection<Gene> genes, Collection<CallFilter> filters) {
			this();
			this.addGenes(genes, filters);
			this.setSatisfyAllGenes(true);
			this.setSatisfyAllCallFilters(false);
    	}
    	
    	/**
    	 * Return the <code>Map</code> associating <code>Gene</code>s to 
    	 * <code>Collection</code>s of <code>CallFilter</code>s. It defines for each 
    	 * <code>Gene</code> what are the data requirements for it. 
    	 * <p>
    	 * When a <code>Gene</code> is associated to several <code>CallFilter</code>s, 
    	 * the value returned by {@link isSatisfyAllCallFilters()} determines whether 
    	 * all of them must be satisfied for the <code>Gene</code> to be validated, 
    	 * or only at least one of them.
    	 * <p>
    	 * When there are several <code>Genes</code> in the key set, the value 
    	 * returned by {@link isSatisfyAllGenes()} determines whether all of them 
    	 * must be satisfied for this <code>GeneCallRequirement</code> to be validated, 
    	 * or only at least one of them.
    	 * 
    	 * @return 	a <code>Map</code> where each <code>Gene</code> in the key set 
    	 * 			is associated to a <code>Collection</code> of <code>CallFilter</code>s 
    	 * 			as value. 
    	 * @see #getGenes()
    	 * @see #getCallFilter(Gene)
    	 */
    	public Map<Gene, Collection<CallFilter>> getGenesWithParameters() {
    		return this.genesWithParameters;
    	}
    	/**
    	 * Return the <code>Set</code> of <code>Gene</code>s part of 
    	 * this <code>GeneCallRequirement</code>. 
    	 * 
    	 * @return 	the <code>Set</code> of <code>Gene</code>s part of 
    	 * 			this <code>GeneCallRequirement</code>.
    	 * @see #getGenesWithParameters()
    	 * @see #getCallFilter(Gene)
    	 */
    	public Set<Gene> getGenes() {
    		return this.genesWithParameters.keySet();
    	}
    	/**
    	 * Return the <code>CallFilter</code>s associated to <code>gene</code>. 
    	 * Return <code>null</code> if this <code>gene</code> is not part of 
    	 * this <code>GeneCallRequirement</code>, an empty <code>Collection</code> 
    	 * if no <code>CallFilter</code> is associated to it yet. 
    	 * <p>
    	 * If <code>gene</code> is associated to several <code>CallFilter</code>s, 
    	 * the value returned by {@link isSatisfyAllCallFilters()} determines whether 
    	 * all of them must be satisfied for it to be validated, or only 
    	 * at least one of them.
    	 * 
    	 * @param gene	A <code>Gene</code> for which the associated <code>CallFilter</code>s 
    	 * 				in this <code>GeneCallRequirement</code> should be returned. 
    	 * @return		A <code>Collection</code> of <code>CallFilter</code>s 
    	 * 				associated to <code>gene</code>. <code>null</code> if 
    	 * 				<code>gene</code> is not part of this 
    	 * 				<code>GeneCallRequirement</code>, an empty <code>Collection</code> 
    	 * 				if no <code>CallFilter</code> is associated to it yet.
    	 * @see #getGenesWithParameters()
    	 * @see #getGenes()
    	 */
    	public Collection<CallFilter> getCallFilters(Gene gene) {
    		return this.genesWithParameters.get(gene);
    	}
    	
    	/**
    	 * Add a <code>Gene</code> to this <code>GeneCallRequirement</code>, 
    	 * associated to a <code>CallFilter</code>. If <code>gene</code> is already 
    	 * part of this <code>GeneCallRequirement</code>, add <code>filter</code> 
    	 * to its associated <code>CallFilter</code>s.
    	 * 
    	 * @param gene 		A <code>Gene</code> to be associated to 
    	 * 					<code>filter</code> in this <code>GeneCallRequirement</code>.
    	 * @param filter	A <code>CallFilter</code> to be associated with <code>gene</code> 
    	 * 					in this <code>GeneCallRequirement</code>.
    	 * @see #addGene(Gene, Collection)
    	 * @see #addGenes(Collection, CallFilter)
    	 * @see #addGenes(Collection, Collection)
    	 */
    	public void addGene(Gene gene, CallFilter filter) {
    		this.addGenes(Arrays.asList(gene), Arrays.asList(filter));
    	}
    	/**
    	 * Add a <code>Gene</code>s to this <code>GeneCallRequirement</code>, 
    	 * associated to a <code>Collection</code> of <code>CallFilter</code>s. 
    	 * If <code>gene</code> is already part of this <code>GeneCallRequirement</code>, 
    	 * then <code>filters</code>  will be added to its associated 
    	 * <code>CallFilter</code>s.
    	 * 
    	 * @param gene 		A <code>Gene</code> to be associated to 
    	 * 					<code>filters</code> in this <code>GeneCallRequirement</code>.
    	 * @param filters	A <code>Collection</code> of <code>CallFilter</code>s 
    	 * 					to be associated to <code>gene</code> 
    	 * 					in this <code>GeneCallRequirement</code>.
    	 * @see #addGene(Gene, CallFilter)
    	 * @see #addGenes(Collection, CallFilter)
    	 * @see #addGenes(Collection, Collection)
    	 */
    	public void addGene(Gene gene, Collection<CallFilter> filters) {
    		this.addGenes(Arrays.asList(gene), filters);
    	}
    	/**
    	 * Add a <code>Collection</code> of <code>Gene</code>s to 
    	 * this <code>GeneCallRequirement</code>, with each of them associated to 
    	 * <code>filter</code>. If a <code>Gene</code> in <code>genes</code> is already 
    	 * part of this <code>GeneCallRequirement</code>, then <code>filter</code> 
    	 * will be added to its associated <code>CallFilter</code>s. 
    	 * 
    	 * @param genes		A <code>Collection</code> of <code>Gene</code>s 
    	 * 					to be associated to <code>filter</code>, 
    	 * 					in this <code>GeneCallRequirement</code>.
    	 * @param filter	A <code>CallFilter</code> to be associated to each 
    	 * 					of the <code>Gene</code> in <code>genes</code>, 
    	 * 					in this <code>GeneCallRequirement</code>.
    	 * @see #addGene(Gene, CallFilter)
    	 * @see #addGenes(Gene, Collection)
    	 * @see #addGenes(Collection, Collection)
    	 */
    	public void addGenes(Collection<Gene> genes, CallFilter filter) {
    		this.addGenes(genes, Arrays.asList(filter));
    	}
    	/**
    	 * Add a <code>Collection</code> of <code>Gene</code>s to 
    	 * this <code>GeneCallRequirement</code>, with each of them associated to 
    	 * a same <code>Collection</code> of <code>CallFilter</code>s. 
    	 * If a <code>Gene</code> in <code>genes</code> is already 
    	 * part of this <code>GeneCallRequirement</code>, then <code>filters</code> 
    	 * will be added to its associated <code>CallFilter</code>s.
    	 * 
    	 * @param genes		A <code>Collection</code> of <code>Gene</code>s 
    	 * 					to be associated to the <code>CallFilter</code>s 
    	 * 					in <code>filters</code>, in this <code>GeneCallRequirement</code>.
    	 * @param filters	A <code>Collection</code> of <code>CallFilter</code>s 
    	 * 					to be associated to each of the <code>Gene</code> 
    	 * 					in <code>genes</code> in this <code>GeneCallRequirement</code>.
    	 * @see #getGenes()
    	 * @see #addGenes(Collection)
    	 */
    	public void addGenes(Collection<Gene> genes, Collection<CallFilter> filters) {
    		for (Gene gene: genes) {
    			Collection<CallFilter> geneFilters = this.genesWithParameters.get(gene);
    			if (geneFilters == null) {
    				geneFilters = new HashSet<CallFilter>();
    				this.genesWithParameters.put(gene, geneFilters);
    			}
    			this.genesWithParameters.get(gene).addAll(filters);
    		}
    	}
		
		/**
    	 * Return the <code>boolean</code> defining whether, when a <code>Gene</code> 
    	 * is associated to several <code>CallFilter</code>s (in the <code>Map</code> 
    	 * returned by {@link getGenesWithParameters()}), all of them must be satisfied 
    	 * for the <code>Gene</code> to be validated, or only at least one of them.
    	 * 
		 * @return 	the <code>boolean</code> defining whether all <code>CallFilter</code>s 
		 * 			associated to a given <code>Gene</code> must be satisfied. 
		 * @see #setSatisfyAllCallFilters(boolean)
    	 * @see #getGenesWithParameters()
    	 * @see #isSatisfyAllGenes()
		 */
		public boolean isSatisfyAllCallFilters() {
			return this.satisfyAllCallFilters;
		}
		/**
		 * Set the <code>boolean</code> defining whether, when a <code>Gene</code> 
    	 * is associated to several <code>CallFilter</code>s (in the <code>Map</code> 
    	 * returned by {@link getGenesWithParameters()}), all of them must be satisfied 
    	 * for the <code>Gene</code> to be validated, or only at least one of them.
    	 * <p>
    	 * The recommended value is <code>false</code>. Setting this parameter 
    	 * to <code>true</code> should be useful only in specific cases, 
    	 * such as, to investigate contradictions between data types; for instance, 
    	 * by requesting <code>EXPRESSION</code> data from <code>AFFYMETRIX</code>, 
    	 * and at the same time, <code>NOEXPRESSION</code> data from <code>RNA-Seq</code>.
    	 * 
		 * @param satisfyAll	A <code>boolean</code> defining whether all
		 * 						<code>CallFilter</code>s associated to a given 
		 * 						<code>Gene</code> must be satisfied. 
		 * @see #isSatisfyAllCallFilters()
    	 * @see #getGenesWithParameters()
    	 * @see #isSatisfyAllGenes()
		 */
		public void setSatisfyAllCallFilters(boolean satisfyAll) {
			this.satisfyAllCallFilters = satisfyAll;
		}
		
		/**
		 * Return the <code>boolean</code> defining whether, when this 
		 * <code>GeneCallRequirement</code> has conditions on several <code>Gene</code>s, 
    	 * the requirements for all of them must be satisfied, or only 
    	 * for at least one of them. Whether the requirements of a given <code>Gene</code> 
    	 * are satisfied is defined by its associated <code>CallFilter</code>s 
    	 * (in the <code>Map</code> returned by {@link getGenesWithParameters()}, 
    	 * and the value returned by {@link #isSatisfyAllCallFilters()}. 
    	 * 
		 * @return 	the <code>boolean</code> defining whether the requirements 
		 * 			for all <code>Gene</code>s must be satisfied, or only for 
		 * 			at least one of them. 
    	 * @see #setSatisfyAllGenes(boolean)
    	 * @see #getGenesWithParameters()
    	 * @see #isSatisfyAllCallFilters()
		 */
		public boolean isSatisfyAllGenes() {
			return this.satisfyAllGenes;
		}
		/**
		 * Set the <code>boolean</code> defining whether, when this 
		 * <code>GeneCallRequirement</code> has conditions on several <code>Gene</code>s, 
    	 * the requirements for all of them must be satisfied, or only 
    	 * for at least one of them. Whether the requirements for a given <code>Gene</code> 
    	 * are satisfied is defined by its associated <code>CallFilter</code>s 
    	 * (in the <code>Map</code> returned by {@link getGenesWithParameters()}, 
    	 * and the value returned by {@link #isSatisfyAllCallFilters()}. 
    	 * <p>
    	 * The recommended value of this parameter is <code>true</code>.
		 * 
		 * @param satisfyAll 	A <code>boolean</code> defining whether the requirements 
		 * 						for all <code>Gene</code>s must be satisfied, or only for 
		 * 						at least one of them. 
    	 * @see #isSatisfyAllGenes()
    	 * @see #getGenesWithParameters()
    	 * @see #isSatisfyAllCallFilters()
		 */
		public void setSatisfyAllGenes(boolean satisfyAll) {
			this.satisfyAllGenes = satisfyAll;
		}
    }
    
}
