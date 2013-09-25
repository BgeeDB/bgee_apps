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
 * on {@link org.bgee.model.anatdev.AnatDevElement AnatDevElement}s, 
 * when performing an expression reasoning using an {@link AnatDevExpressionQuery}. 
 * These conditions define which genes should have which types of expression data, 
 * for instance: "Validate anatomical structures where gene A is expressed, 
 * gene B not expressed, and gene C over-expressed". 
 * <p>
 * Each condition is described by a {@link GeneCallRequirement}. 
 * How this {@code GeneCallRequirement}s are used to validate this 
 * {@code AnatDevRequirement} is defined by the value returned by 
 * {@link #getValidationType()}.
 * <p>
 * Note that an {@code AnatDevExpressionQuery} can use several 
 * {@code AnatDevRequirement}s, offering different ways of validating 
 * an {@code AnatDevElement}. 
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 *
 */
public class AnatDevRequirement {
	//No logger used, these classes basically only have getters/setters.
	
	/**
	 * An {@code enum} to define how this {@code AnatDevRequirement} 
	 * should be validated: 
	 * <ul>
	 * <li>{@code ALL}: all {@code GeneCallRequirement}s must be validated.
	 * <li>{@code CONSERVATION}: all {@code Gene}s part of the 
	 * {@code GeneCallRequirement}s must have the same {@code CallType} 
	 * in a given {@code AnatDevElement} for it to be validated. This allows 
	 * for instance to request for both {@code EXPRESSION} and {@code NOEXPRESSION} 
	 * data, but to get only {@code AnatDevElement}s where {@code Gene}s 
	 * are similarly expressed. 
	 * <li>{@code DIVERGENCE}: at least on of the {@code Gene}s part of the 
	 * {@code GeneCallRequirement}s must have a {@code CallType} different  
	 * from the others in a given {@code AnatDevElement}, for it to be validated. 
	 * This allows for instance to request for both {@code EXPRESSION} and 
	 * {@code NOEXPRESSION} data, but to get only {@code AnatDevElement}s 
	 * where gene expression is divergent. 
	 * <li>{@code GENETHRESHOLD}: the {@code AnatDevRequirement} 
	 * will be validated based on the number of {@code Gene}s (defined 
	 * using {@link AnatDevRequirement#setValidationThreshold(int)}), 
	 * amongst all the {@code Gene}s present in the {@code GeneCallRequirement}s, 
	 * that exhibit the reference {@code CallType} (defined using 
	 * {@link AnatDevRequirement#setReferenceCallType(CallType)}).
	 * <li>{@code SPECIESTHRESHOLD}: the {@code AnatDevRequirement} 
	 * will be validated based on the number of {@code Species}s (defined 
	 * using {@link AnatDevRequirement#setValidationThreshold(int)}), 
	 * amongst all the {@code Species}s represented by the {@code Gene}s 
	 * in the {@code GeneCallRequirement}s, that exhibit the reference 
	 * {@code CallType} (set using 
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
	 * A {@code Collection} of {@code GeneCallRequirement}s 
	 * defining gene expression data to retrieve and conditions to satisfy 
	 * for a  {@code AnatDevElement} to be validated, when performing 
     * an expression reasoning using an {@link AnatDevExpressionQuery}.
	 */
	private final Collection<GeneCallRequirement> requirements;
	/**
	 * A {@code ValidationType} to define how this {@code AnatDevRequirement} 
	 * should be validated.
	 * 
	 * @see #validationThreshold
	 * @see #referenceCall
	 */
	private ValidationType validationType;
	/**
	 * An {@code int} defining a threshold to validate this 
	 * {@code AnatDevRequirement}, regarding the number of {@code Gene}s 
	 * or of {@code Species}s (depending on {@link #validationType}), 
	 * that must exhibit the reference {@code CallType} of {@link #referenceCall}.
	 * If {@link #validationType} is not equal to {@code GENETHRESHOLD} or 
	 * {@code SPECIESTHRESHOLD}, this attribute is not used. 
	 * 
	 * @see #validationType
	 * @see #referenceCall
	 */
	private int validationThreshold;
	/**
	 * A {@code CallType} that must be exhibited by a defined number of 
	 * {@code Gene} or {@code Species}s, for this {@code AnatDevRequirement} 
	 * to be validated. {@link #validationThreshold} defines the number, 
	 * {@link #validationType} defines whether {@code Gene} or {@code Species}s 
	 * should be used. If {@link #validationType} is not equal to 
	 * {@code GENETHRESHOLD} or {@code SPECIESTHRESHOLD}, this attribute 
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
	 * Return the {@code ValidationType} defining how this 
	 * {@code AnatDevRequirement} should be validated.
	 * 
	 * @return 	the {@code ValidationType} to validate 
	 * 			this {@code AnatDevRequirement}.
	 * @see #getValidationThreshold()
	 * @see #getReferenceCall()
	 */
	public ValidationType getValidationType() {
		return this.validationType;
	}
	/**
	 * Set the {@code ValidationType} defining how this 
	 * {@code AnatDevRequirement} should be validated. 
	 * If {@code validationType} is equal to {@code GENETHRESHOLD} 
	 * or {@code SPECIESTHRESHOLD}, then a threshold must be set using 
	 * {@link #setValidationThreshold(int)} and a reference {@code CallType} 
	 * using {@link #setReferenceCall(CallType)}.
	 * 
	 * @param validationType 	A {@code ValidationType} defining how this 
	 * 							{@code AnatDevRequirement} should be validated. 
	 * @see #setValidationThreshold(int)
	 * @see #setReferenceCall(CallType) 
	 */
	public void setValidationType(ValidationType validationType) {
		this.validationType = validationType;
	}

	/**
	 * Get the {@code int} defining the threshold to validate this 
	 * {@code AnatDevRequirement}, regarding the number of {@code Gene}s 
	 * or of {@code Species}s (depending on the value returned by 
	 * {@link #getValidationType()}), that must exhibit the reference {@code CallType} 
	 * (that can be obtained using {@link #getReferenceCall()}.
	 * If the value returned by {@link #getValidationType()} is not equal to 
	 * {@code GENETHRESHOLD} nor {@code SPECIESTHRESHOLD}, 
	 * this parameter is not used. 
	 * 
	 * @return 	the {@code int} defining the threshold to validate this 
	 *			{@code AnatDevRequirement}
	 * @see #getValidationType()
	 * @see #getReferenceCall()
	 */
	public int getValidationThreshold() {
		return this.validationThreshold;
	}
	/**
	 * Set the {@code int} defining the threshold to validate this 
	 * {@code AnatDevRequirement}, regarding the number of {@code Gene}s 
	 * or of {@code Species}s (depending on the value returned by 
	 * {@link #getValidationType()}), that must exhibit the reference {@code CallType} 
	 * (that can be obtained using {@link #getReferenceCall()}.
	 * If the value returned by {@link #getValidationType()} is not equal to 
	 * {@code GENETHRESHOLD} nor {@code SPECIESTHRESHOLD}, 
	 * this parameter is not used. 
	 * 
	 * @param threshold 	A {@code int} to define the threshold to validate 
	 * 						this {@code AnatDevRequirement}.
	 * @see #setValidationType(ValidationType)
	 * @see #setReferenceCall(CallType)
	 */
	public void setValidationThreshold(int threshold) {
		this.validationThreshold = threshold;
	}

	/**
	 * Get the {@code CallType} that must be exhibited by a defined number of 
	 * {@code Gene}s or {@code Species}s, for this {@code AnatDevRequirement} 
	 * to be validated. The value returned by {@link #getValidationThreshold()} 
	 * defines the number, {@link #getValidationType()} defines whether {@code Gene}s 
	 * or {@code Species}s should be used. 
	 * If the value returned by {@link #getValidationType()} is not equal to 
	 * {@code GENETHRESHOLD} nor {@code SPECIESTHRESHOLD}, 
	 * this parameter is not used. 
	 * 
	 * @return 	the reference {@code CallType}, used depending on the value 
	 * 			returned by {@link #getValidationType()}
	 * @see #getValidationType()
	 * @see #getValidationThreshold()
	 */
	public CallType getReferenceCall() {
		return this.referenceCall;
	}
	/**
	 * Set the {@code CallType} that must be exhibited by a defined number of 
	 * {@code Gene}s or {@code Species}s, for this {@code AnatDevRequirement} 
	 * to be validated. The value returned by {@link #getValidationThreshold()} 
	 * defines the number, {@link #getValidationType()} defines whether {@code Gene}s 
	 * or {@code Species}s should be used. 
	 * If the value returned by {@link #getValidationType()} is not equal to 
	 * {@code GENETHRESHOLD} nor {@code SPECIESTHRESHOLD}, 
	 * this parameter is not used. 
	 * 
	 * @return 	the reference {@code CallType}, used depending on the value 
	 * 			returned by {@link #getValidationType()}
	 * 
	 * @param referenceCall the reference {@code CallType}, that will be used 
	 * 						depending on the value returned by {@link #getValidationType()}.
	 * @see #setValidationType(ValidationType)
	 * @see #setValidationThreshold(int)
	 */
	public void setReferenceCall(CallType referenceCall) {
		this.referenceCall = referenceCall;
	}

	/**
	 * Get the {@code Collection} of {@code GeneCallRequirement}s, 
	 * defining gene expression data to retrieve and conditions to satisfy 
	 * for a  {@code AnatDevElement} to be validated, when performing 
     * an expression reasoning using an {@link AnatDevExpressionQuery}.
	 * 
	 * @return 	the {@code Collection} of {@code GeneCallRequirement}s
	 * 			associated to this {@code AnatDevRequirement}. 
	 */
	public Collection<GeneCallRequirement> getRequirements() {
		return this.requirements;
	}
	/**
	 * Add a {@code GeneCallRequirement} to this {@code AnatDevRequirement}, 
	 * defining gene expression data to retrieve and conditions to satisfy 
	 * for a  {@code AnatDevElement} to be validated, when performing 
     * an expression reasoning using an {@link AnatDevExpressionQuery}.
     * 
	 * @param requirement	A {@code GeneCallRequirement} to be added 
	 * 						to this {@code AnatDevRequirement}.
	 * @see #addAllRequirements(Collection)
	 * @see #setValidationType(ValidationType)
	 */
	public void addRequirement(GeneCallRequirement requirement) {
		this.requirements.add(requirement);
	}
	/**
	 * Add {@code GeneCallRequirement}s to this {@code AnatDevRequirement}, 
	 * defining gene expression data to retrieve and conditions to satisfy 
	 * for a  {@code AnatDevElement} to be validated, when performing 
     * an expression reasoning using an {@link AnatDevExpressionQuery}.
     * 
	 * @param requirements	A {@code Collection} of {@code GeneCallRequirement}s 
	 * 						to be added to this {@code AnatDevRequirement}.
	 * @see #addRequirement(GeneCallRequirement)
	 * @see #setValidationType(ValidationType)
	 */
	public void addAllRequirements(Collection<GeneCallRequirement> requirements) {
		this.requirements.addAll(requirements);
	}

	/**
     * Define custom conditions for an {@code AnatDevElement} 
     * to be validated, regarding its gene expression data, when performing 
     * an expression reasoning using an {@link AnatDevExpressionQuery}. 
     * A {@code GeneCallRequirement} is always part of a {@link AnatDevRequirement}, 
     * listing all the necessary conditions that an {@code AnatDevElement} 
     * must satisfied at the same time to be validated.
     * <p>
     * A {@code GeneCallRequirement} lists {@code Gene}s, associated to 
     * {@code CallFilter}s, to define which {@code Gene}s should have 
     * what kind of expression data for an {@code AnatDevElement} to be validated. 
     * For instance, using {@code GeneCallRequirement}s, it is possible 
     * to query for {@code AnatDevElement}s exhibiting expression of a gene A, 
     * expression or absence of expression of a gene B, and over-expression of a gene C, 
     * etc.
     * <p>
     * It can include several {@code Gene}s, or only one, and a {@code Gene} 
     * can be associated to several {@code CallFilter}s, or only one. 
     * It is possible to define whether all {@code CallFilter}s should be satisfied 
     * at the same time for a {@code Gene} to be validated, and whether 
     * all {@code Gene}s should be validated, or only once, for a 
     * {@code GeneCallRequirement} to be validated. 
     * 
	 * @author Frederic Bastian
	 * @see AnatDevRequirement
	 * @version Bgee 13
	 * @since Bgee 13
     *
     */
    public class GeneCallRequirement {
    	
    	/**
    	 * A {@code Map} associating each {@code Gene} in the key set, 
    	 * to a {@code Collection} of {@code CallFilter}s. 
    	 * The {@code CallFilter}s define for each {@code Gene} 
    	 * the expression data to retrieve for it. 
    	 * <p>
    	 * Whether all expression data requirements for a {@code Gene} 
    	 * must be satisfied, or only at least one, is defined 
    	 * by {@link #satisfyAllCallFilters}. 
    	 * <p>
    	 * Whether all {@code Gene}s must have their data requirements satisfied 
    	 * for this {@code GeneCallRequirement}, or only at least one of them, 
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
    	 * A {@code boolean} defining whether, when a {@code Gene} 
    	 * in {@code genesWithParameters} is associated to several 
    	 * {@code CallFilter}s, all of them must be satisfied for 
    	 * the {@code Gene} to be validated, or only at least one of them.
    	 * <p>
    	 * The recommended value is {@code false}. Setting this attribute 
    	 * to {@code true} should be useful only in specific cases, 
    	 * such as, to investigate contradictions between data types; for instance, 
    	 * by requesting {@code EXPRESSION} data from {@code AFFYMETRIX}, 
    	 * and at the same time, {@code NOEXPRESSION} data from {@code RNA-Seq}.
    	 * 
    	 * @see #genesWithParameters
    	 * @see #satisfyAllGenes
    	 */
    	private boolean satisfyAllCallFilters;

    	/**
    	 * A {@code boolean} defining whether, when several {@code Genes} 
    	 * are present in {@code genesWithParameters}, all of them 
    	 * must be satisfied for this {@code GeneCallRequirement} to be validated, 
    	 * or only at least one of them. Whether the requirements are satisfied 
    	 * for a given {@code Gene} is defined by its associated 
    	 * {@code CallFilter}s in {@code genesWithParameters} and the value of 
    	 * {@code satisfyAllCallFilters}. 
    	 * <p>
    	 * The recommended value is {@code true}.
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
    	 * Instantiate a {@code AnatDevRequirement} with requirements 
    	 * on one {@code Gene}, associated to one {@code CallFilter}. 
    	 * This is equivalent to calling: 
    	 * <ul>
    	 * <li>{@code addGene(gene, filter)}
    	 * </ul>
    	 * 
    	 * @param gene		A {@code Gene} to be part of
    	 * 					this {@code GeneCallRequirement}. 
    	 * @param filter	A {@code CallFilter} to be associated to {@code gene}.
    	 */
    	public GeneCallRequirement(Gene gene, CallFilter filter) {
			this();
			this.addGene(gene, filter);
    	}
    	/**
    	 * Instantiate a {@code AnatDevRequirement} with requirements 
    	 * on several {@code Gene}s (with the condition of validating all of them), 
    	 * with each of them associated to a same {@code Collection} 
    	 * of {@code CallFilter}s (with the condition of validating any of them). 
    	 * This is equivalent to calling: 
    	 * <ul>
    	 * <li>{@code addGenes(genes, filters)}
    	 * <li>{@code setSatisfyAllGenes(true)}
    	 * <li>{@code setSatisfyAllCallFilters(false)}
    	 * </ul>
    	 * Whether all {@code Gene} conditions and/or all {@code CallFilter} 
    	 * conditions must be satisfied can be set afterwards.
    	 * 
    	 * @param genes		A {@code Collection} of {@code Gene}s to be part of
    	 * 					this {@code GeneCallRequirement}. They must all 
    	 * 					be validated for this {@code GeneCallRequirement} 
    	 * 					to be satisfied. 
    	 * @param filters	A {@code Collection} of {@code CallFilter}s,  
    	 * 					that are the requirements associated to each {@code Gene}.
    	 * 					Any of them must be satisfied for a {@code Gene} 
    	 * 					to be validated.  
    	 */
    	public GeneCallRequirement(Collection<Gene> genes, Collection<CallFilter> filters) {
			this();
			this.addGenes(genes, filters);
			this.setSatisfyAllGenes(true);
			this.setSatisfyAllCallFilters(false);
    	}
    	
    	/**
    	 * Return the {@code Map} associating {@code Gene}s to 
    	 * {@code Collection}s of {@code CallFilter}s. It defines for each 
    	 * {@code Gene} what are the data requirements for it. 
    	 * <p>
    	 * When a {@code Gene} is associated to several {@code CallFilter}s, 
    	 * the value returned by {@link isSatisfyAllCallFilters()} determines whether 
    	 * all of them must be satisfied for the {@code Gene} to be validated, 
    	 * or only at least one of them.
    	 * <p>
    	 * When there are several {@code Genes} in the key set, the value 
    	 * returned by {@link isSatisfyAllGenes()} determines whether all of them 
    	 * must be satisfied for this {@code GeneCallRequirement} to be validated, 
    	 * or only at least one of them.
    	 * 
    	 * @return 	a {@code Map} where each {@code Gene} in the key set 
    	 * 			is associated to a {@code Collection} of {@code CallFilter}s 
    	 * 			as value. 
    	 * @see #getGenes()
    	 * @see #getCallFilter(Gene)
    	 */
    	public Map<Gene, Collection<CallFilter>> getGenesWithParameters() {
    		return this.genesWithParameters;
    	}
    	/**
    	 * Return the {@code Set} of {@code Gene}s part of 
    	 * this {@code GeneCallRequirement}. 
    	 * 
    	 * @return 	the {@code Set} of {@code Gene}s part of 
    	 * 			this {@code GeneCallRequirement}.
    	 * @see #getGenesWithParameters()
    	 * @see #getCallFilter(Gene)
    	 */
    	public Set<Gene> getGenes() {
    		return this.genesWithParameters.keySet();
    	}
    	/**
    	 * Return the {@code CallFilter}s associated to {@code gene}. 
    	 * Return {@code null} if this {@code gene} is not part of 
    	 * this {@code GeneCallRequirement}, an empty {@code Collection} 
    	 * if no {@code CallFilter} is associated to it yet. 
    	 * <p>
    	 * If {@code gene} is associated to several {@code CallFilter}s, 
    	 * the value returned by {@link isSatisfyAllCallFilters()} determines whether 
    	 * all of them must be satisfied for it to be validated, or only 
    	 * at least one of them.
    	 * 
    	 * @param gene	A {@code Gene} for which the associated {@code CallFilter}s 
    	 * 				in this {@code GeneCallRequirement} should be returned. 
    	 * @return		A {@code Collection} of {@code CallFilter}s 
    	 * 				associated to {@code gene}. {@code null} if 
    	 * 				{@code gene} is not part of this 
    	 * 				{@code GeneCallRequirement}, an empty {@code Collection} 
    	 * 				if no {@code CallFilter} is associated to it yet.
    	 * @see #getGenesWithParameters()
    	 * @see #getGenes()
    	 */
    	public Collection<CallFilter> getCallFilters(Gene gene) {
    		return this.genesWithParameters.get(gene);
    	}
    	
    	/**
    	 * Add a {@code Gene} to this {@code GeneCallRequirement}, 
    	 * associated to a {@code CallFilter}. If {@code gene} is already 
    	 * part of this {@code GeneCallRequirement}, add {@code filter} 
    	 * to its associated {@code CallFilter}s.
    	 * 
    	 * @param gene 		A {@code Gene} to be associated to 
    	 * 					{@code filter} in this {@code GeneCallRequirement}.
    	 * @param filter	A {@code CallFilter} to be associated with {@code gene} 
    	 * 					in this {@code GeneCallRequirement}.
    	 * @see #addGene(Gene, Collection)
    	 * @see #addGenes(Collection, CallFilter)
    	 * @see #addGenes(Collection, Collection)
    	 */
    	public void addGene(Gene gene, CallFilter filter) {
    		this.addGenes(Arrays.asList(gene), Arrays.asList(filter));
    	}
    	/**
    	 * Add a {@code Gene}s to this {@code GeneCallRequirement}, 
    	 * associated to a {@code Collection} of {@code CallFilter}s. 
    	 * If {@code gene} is already part of this {@code GeneCallRequirement}, 
    	 * then {@code filters}  will be added to its associated 
    	 * {@code CallFilter}s.
    	 * 
    	 * @param gene 		A {@code Gene} to be associated to 
    	 * 					{@code filters} in this {@code GeneCallRequirement}.
    	 * @param filters	A {@code Collection} of {@code CallFilter}s 
    	 * 					to be associated to {@code gene} 
    	 * 					in this {@code GeneCallRequirement}.
    	 * @see #addGene(Gene, CallFilter)
    	 * @see #addGenes(Collection, CallFilter)
    	 * @see #addGenes(Collection, Collection)
    	 */
    	public void addGene(Gene gene, Collection<CallFilter> filters) {
    		this.addGenes(Arrays.asList(gene), filters);
    	}
    	/**
    	 * Add a {@code Collection} of {@code Gene}s to 
    	 * this {@code GeneCallRequirement}, with each of them associated to 
    	 * {@code filter}. If a {@code Gene} in {@code genes} is already 
    	 * part of this {@code GeneCallRequirement}, then {@code filter} 
    	 * will be added to its associated {@code CallFilter}s. 
    	 * 
    	 * @param genes		A {@code Collection} of {@code Gene}s 
    	 * 					to be associated to {@code filter}, 
    	 * 					in this {@code GeneCallRequirement}.
    	 * @param filter	A {@code CallFilter} to be associated to each 
    	 * 					of the {@code Gene} in {@code genes}, 
    	 * 					in this {@code GeneCallRequirement}.
    	 * @see #addGene(Gene, CallFilter)
    	 * @see #addGenes(Gene, Collection)
    	 * @see #addGenes(Collection, Collection)
    	 */
    	public void addGenes(Collection<Gene> genes, CallFilter filter) {
    		this.addGenes(genes, Arrays.asList(filter));
    	}
    	/**
    	 * Add a {@code Collection} of {@code Gene}s to 
    	 * this {@code GeneCallRequirement}, with each of them associated to 
    	 * a same {@code Collection} of {@code CallFilter}s. 
    	 * If a {@code Gene} in {@code genes} is already 
    	 * part of this {@code GeneCallRequirement}, then {@code filters} 
    	 * will be added to its associated {@code CallFilter}s.
    	 * 
    	 * @param genes		A {@code Collection} of {@code Gene}s 
    	 * 					to be associated to the {@code CallFilter}s 
    	 * 					in {@code filters}, in this {@code GeneCallRequirement}.
    	 * @param filters	A {@code Collection} of {@code CallFilter}s 
    	 * 					to be associated to each of the {@code Gene} 
    	 * 					in {@code genes} in this {@code GeneCallRequirement}.
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
    	 * Return the {@code boolean} defining whether, when a {@code Gene} 
    	 * is associated to several {@code CallFilter}s (in the {@code Map} 
    	 * returned by {@link getGenesWithParameters()}), all of them must be satisfied 
    	 * for the {@code Gene} to be validated, or only at least one of them.
    	 * 
		 * @return 	the {@code boolean} defining whether all {@code CallFilter}s 
		 * 			associated to a given {@code Gene} must be satisfied. 
		 * @see #setSatisfyAllCallFilters(boolean)
    	 * @see #getGenesWithParameters()
    	 * @see #isSatisfyAllGenes()
		 */
		public boolean isSatisfyAllCallFilters() {
			return this.satisfyAllCallFilters;
		}
		/**
		 * Set the {@code boolean} defining whether, when a {@code Gene} 
    	 * is associated to several {@code CallFilter}s (in the {@code Map} 
    	 * returned by {@link getGenesWithParameters()}), all of them must be satisfied 
    	 * for the {@code Gene} to be validated, or only at least one of them.
    	 * <p>
    	 * The recommended value is {@code false}. Setting this parameter 
    	 * to {@code true} should be useful only in specific cases, 
    	 * such as, to investigate contradictions between data types; for instance, 
    	 * by requesting {@code EXPRESSION} data from {@code AFFYMETRIX}, 
    	 * and at the same time, {@code NOEXPRESSION} data from {@code RNA-Seq}.
    	 * 
		 * @param satisfyAll	A {@code boolean} defining whether all
		 * 						{@code CallFilter}s associated to a given 
		 * 						{@code Gene} must be satisfied. 
		 * @see #isSatisfyAllCallFilters()
    	 * @see #getGenesWithParameters()
    	 * @see #isSatisfyAllGenes()
		 */
		public void setSatisfyAllCallFilters(boolean satisfyAll) {
			this.satisfyAllCallFilters = satisfyAll;
		}
		
		/**
		 * Return the {@code boolean} defining whether, when this 
		 * {@code GeneCallRequirement} has conditions on several {@code Gene}s, 
    	 * the requirements for all of them must be satisfied, or only 
    	 * for at least one of them. Whether the requirements of a given {@code Gene} 
    	 * are satisfied is defined by its associated {@code CallFilter}s 
    	 * (in the {@code Map} returned by {@link getGenesWithParameters()}, 
    	 * and the value returned by {@link #isSatisfyAllCallFilters()}. 
    	 * 
		 * @return 	the {@code boolean} defining whether the requirements 
		 * 			for all {@code Gene}s must be satisfied, or only for 
		 * 			at least one of them. 
    	 * @see #setSatisfyAllGenes(boolean)
    	 * @see #getGenesWithParameters()
    	 * @see #isSatisfyAllCallFilters()
		 */
		public boolean isSatisfyAllGenes() {
			return this.satisfyAllGenes;
		}
		/**
		 * Set the {@code boolean} defining whether, when this 
		 * {@code GeneCallRequirement} has conditions on several {@code Gene}s, 
    	 * the requirements for all of them must be satisfied, or only 
    	 * for at least one of them. Whether the requirements for a given {@code Gene} 
    	 * are satisfied is defined by its associated {@code CallFilter}s 
    	 * (in the {@code Map} returned by {@link getGenesWithParameters()}, 
    	 * and the value returned by {@link #isSatisfyAllCallFilters()}. 
    	 * <p>
    	 * The recommended value of this parameter is {@code true}.
		 * 
		 * @param satisfyAll 	A {@code boolean} defining whether the requirements 
		 * 						for all {@code Gene}s must be satisfied, or only for 
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
