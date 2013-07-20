package org.bgee.model.expressiondata.querytools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bgee.model.expressiondata.DataParameters;
import org.bgee.model.expressiondata.DataParameters.CallType;
import org.bgee.model.expressiondata.querytools.filters.CallFilter;
import org.bgee.model.gene.Gene;

/**
 * List and allow to validate custom conditions on gene expression data 
 * of an <code>OntologyEntity</code>, when performing an expression reasoning 
 * on an <code>Ontology</code>, using an {@link AnatDevExpressionQuery}. These conditions 
 * define which genes should have which types of expression call 
 * in an <code>OntologyEntity</code>, for instance: "Validate OntologyElements 
 * where gene A is expressed, gene B not expressed, and gene C over-expressed". 
 * <p>
 * Each condition is described by a {@link GeneCallRequirement}. 
 * The <code>GeneCallRequirement</code>s part of this <code>GeneCallValidator</code> 
 * must all be satisfied for an <code>OntologyEntity</code> to be validated. 
 * <p>
 * The methods allowing to validate an <code>OntologyEntity</code> are 
 * {@link #validate(OntologyEntity)} and {@link #validate(Collection)}.
 * <p>
 * Note that an <code>AnatDevExpressionQuery</code> can use several 
 * <code>GeneCallValidator</code>s, offering different ways of validating 
 * an <code>OntologyEntity</code>. Nevertheless, for one of these 
 * <code>GeneCallValidator</code>s to validate an <code>OntologyEntity</code>, 
 * all its <code>GeneCallRequirement</code>s must be satisfied.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 *
 */
public class GeneCallValidator {
	public enum GeneValidationType {
		ALL, ANY, GENETHRESHOLD, SPECIESTHRESHOLD;
	}
	private GeneValidationType geneValidationType;
	private int validationThreshold;

	/**
     * Define a custom condition for an <code>OntologyEntity</code> 
     * to be validated, regarding its gene expression data, when performing 
     * an expression reasoning on an <code>Ontology</code>, 
     * using an {@link AnatDevExpressionQuery}. 
     * A <code>GeneCallRequirement</code> is always part of a {@link GeneCallValidator}, 
     * listing all the necessary conditions that an <code>OntologyEntity</code> 
     * must satisfied at the same time to be validated.
     * <p>
     * If any of the <code>Gene</code>s, contained in a <code>GeneCallRequirement</code>, 
     * have any data in an <code>OntologyEntity</code>, corresponding to one of the requested  
     * <code>CallType</code>s, then the requirement is satisfied 
     * (like a <code>OR</code> condition both on genes and expression data). 
     * Whether all <code>Gene</code>s must have at least some expression data in any case 
     * can be set by calling {@link #setAllGenesWithData(boolean)} 
     * (understand, some expression data calls amongst the call types that were allowed 
     * by the <code>AnatDevExpressionQuery</code> for a given <code>Gene</code>, not amongst 
     * all the call types available for that <code>Gene</code>). 
     * If no <code>CallType</code>s are specified, then any expression data call type 
     * is accepted (so the requirement is simply that at least one of the <code>Gene</code>s 
     * must have some expression data, amongst the call types that were allowed 
     * by the <code>AnatDevExpressionQuery</code> for a given <code>Gene</code>). 
     * <p>
     * For instance, if a <code>GeneCallRequirement</code> contains a gene A and a gene B, 
     * and two <code>CallType</code>s, <code>EXPRESSION</code> and 
     * <code>NOEXPRESSION</code>, it means that if any of the gene A or B is expressed, 
     * or not expressed, in a given <code>OntologyEntity</code>, then the requirement 
     * is satisfied for this <code>OntologyEntity</code>.
     * <p>
     * Be careful when using a <code>GeneCallRequirement</code> including several genes: 
     * you will likely want all genes to have at least some data in any case; 
     * in the previous example, if gene A is expressed, 
     * the condition will be satisfied even if there are no data available at all 
     * for gene B. To validate a condition only if all genes have some data, call 
     * the method {@link #setAllGenesWithData(boolean)} with the parameter <code>true</code>. 
     * This is equivalent to adding one <code>GeneCallRequirement</code> for each 
     * individual gene, as part of a same <code>GeneCallValidator</code>, 
     * to specify that each gene must have some data in any case.
     * <p>
     * As another example, you could consider the <code>STANDARD</code> 
     * {@link org.bgee.model.expressiondata.AnatDevExpressionQuery.ValidationType ValidationType} 
     * equivalent to have a <code>GeneCallRequirement</code> containing all <code>Gene</code>s 
     * compared, with the method <code>setAllGenesWithData</code> called with <code>true</code>.
     * Or to have one <code>GeneCallRequirement</code> for each gene individually, 
     * part of a same <code>GeneCallValidator</code>, meaning that all genes must have 
     * at least some data. 
     * 
	 * @author Frederic Bastian
	 * @see GeneCallValidator
	 * @version Bgee 13
	 * @since Bgee 13
     *
     */
    public class GeneCallRequirement {
    	
    	/**
    	 * A <code>Map</code> associating each <code>Gene</code> in the key set, 
    	 * to a <code>Collection</code> of <code>CallFilter</code>s. 
    	 * The <code>CallFilter</code>s define for each gene the expression data  
    	 * to retrieve for it. 
    	 * <p>
    	 * Whether all expression data requirements for a <code>Gene</code> 
    	 * must be satisfied, or only at least one, is defined by {@link #satisfyAllParams}. 
    	 * <p>
    	 * Whether all <code>Genes</code> must have their data requirements satisfied 
    	 * for this <code>GeneCallRequirement</code>, or only at least one, 
    	 * or a custom threshold, is defined by {@link #geneValidationType}. 
    	 * <p>
    	 * See {@link #satisfyAllParams} and {@link #geneValidationType} 
    	 * for more information.
    	 * 
    	 * @see #satisfyAllParams
    	 * @see #geneValidationType
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
    	 */
    	private boolean satisfyAllCallFilters;

    	/**
    	 * A <code>boolean</code> defining whether, when several <code>Genes</code> 
    	 * are present in <code>genesWithParameters</code>, all of them 
    	 * must be satisfied for this <code>GeneCallRequirements</code> to be validated, 
    	 * or only at least one of them. Whether the requirements are satisfied 
    	 * for a given <code>Gene</code> is defined by its associated 
    	 * <code>CallFilter</code>s in <code>genesWithParameters</code> and the value of 
    	 * <code>satisfyAllParams</code>. 
    	 * <p>
    	 * The recommended value is <code>true</code>..
    	 */
    	private boolean satisfyAllGenes;
    	
    	/**
    	 * Default constructor. 
    	 */
    	public GeneCallRequirement() {
    		this(null);
    	}
    	/**
    	 * Instantiate a <code>GeneCallRequirement</code> for one <code>Gene</code>, 
    	 * with no <code>CallType</code> specified. It means that this <code>Gene</code>
    	 * must exhibit any expression data call in the <code>OntologyEntity</code>, 
    	 * for it to be validated. 
    	 * 
    	 * @param gene 		The <code>Gene</code> which this <code>GeneCallRequirement</code> 
    	 * 					is related to.
    	 * 					Any expression data call type will be accepted. 
    	 */
    	public GeneCallRequirement(Gene gene) {
    		this(gene, (Collection<CallType>) null);
    	}
    	/**
    	 * Instantiate a <code>GeneCallRequirement</code> for one <code>Gene</code> 
    	 * and one <code>CallType</code>. It means that this <code>Gene</code> 
    	 * must absolutely have an expression data call of this <code>CallType</code> 
    	 * in an <code>OntologyEntity</code>, for it to be validated. 
    	 * 
    	 * @param gene 		The <code>Gene</code> which this <code>GeneCallRequirement</code> 
    	 * 					is related to.
    	 * @param callType	The <code>CallType</code> the <code>gene</code> must exhibit. 
    	 */
    	public GeneCallRequirement(Gene gene, CallType callType) {
    		this(gene, new ArrayList<CallType>(Arrays.asList(callType)));
    	}
    	/**
    	 * Instantiate a <code>GeneCallRequirement</code> for one <code>Gene</code>, 
    	 * with several <code>CallType</code>s. It means that this <code>Gene</code> 
    	 * must exhibit an expression data call of any of these <code>CallType</code>s 
    	 * in an <code>OntologyEntity</code>, for it to be validated. 
    	 * <p>
    	 * If some <code>CallType</code>s are redundant (for instance, 
    	 * <code>EXPRESSION</code> and <code>OVEREXPRESSION</code> are redundant, 
    	 * all genes with over-expression are expressed), 
    	 * an <code>IllegalArgumentException</code> is thrown. This is for the sake of 
    	 * educating users :) This would actually not change the result of the query.
    	 * 
    	 * @param gene 		The <code>Gene</code> which this <code>GeneCallRequirement</code> 
    	 * 					is related to.
    	 * @param callTypes	A <code>Collection</code> of <code>CallType</code>s,  
    	 * 					<code>gene</code> must exhibit at least one of them. 
    	 * @throws IllegalArgumentException 	If some <code>CallType</code>s are redundant.
    	 */
    	public GeneCallRequirement(Gene gene, Collection<CallType> callTypes) {
    		this(new ArrayList<Gene>(Arrays.asList(gene)), callTypes, false);
    	}
    	/**
    	 * Instantiate a <code>GeneCallRequirement</code> for a <code>Collection</code> of 
    	 * <code>Gene</code>s, with no <code>CallType</code> specified. It means that 
    	 * at least one of the <code>Gene</code>s must exhibit any expression data call 
    	 * in the <code>OntologyEntity</code>, for it to be validated, 
    	 * amongst the call types that were allowed by the <code>AnatDevExpressionQuery</code> 
    	 * for that <code>Gene</code>. Besides, if <code>allGenesWithData</code> 
    	 * is <code>true</code>, then another requirement is that all <code>Gene</code>s 
    	 * must at least have some expression data, of any call type, in any case, 
    	 * amongst the call types that were allowed by the <code>AnatDevExpressionQuery</code> 
    	 * for those <code>Gene</code>s.
    	 * 
    	 * @param genes		A <code>Collection</code> of <code>Gene</code>s 
    	 * 					which this <code>GeneCallRequirement</code> is related to. 
    	 * 					Any expression data call type will be accepted. 
    	 * @param allGenesWithData	A <code>boolean</code> defining whether 
    	 * 							all <code>Gene</code>s should have at least some 
    	 * 							expression data in any case. If <code>true</code>, 
    	 * 							they should.
    	 */
    	public GeneCallRequirement(Collection<Gene> genes, boolean allGenesWithData) {
    		this(genes, (Collection<CallType>) null, allGenesWithData);
    	}
    	/**
    	 * Instantiate a <code>GeneCallRequirement</code> for a <code>Collection</code> of 
    	 * <code>Gene</code>s, and one <code>CallType</code>. It means that 
    	 * at least one of the <code>Gene</code>s must exhibit an expression data call 
    	 * of this <code>CallType</code> in the <code>OntologyEntity</code>, 
    	 * for it to be validated, amongst the call types that were allowed 
    	 * by the <code>AnatDevExpressionQuery</code> for that <code>Gene</code>. 
    	 * Besides, if <code>allGenesWithData</code> 
    	 * is <code>true</code>, then another requirement is that all <code>Gene</code>s 
    	 * must at least have some expression data, of any call type, in any case, 
    	 * amongst the call types that were allowed by the <code>AnatDevExpressionQuery</code> 
    	 * for those <code>Gene</code>s. 
    	 * 
    	 * @param genes		A <code>Collection</code> of <code>Gene</code>s 
    	 * 					which this <code>GeneCallRequirement</code> is related to.
    	 * @param callType	The <code>CallType</code> at lest one of the <code>Gene</code>s 
    	 * 					in <code>genes</code> must exhibit. 
    	 * @param allGenesWithData	A <code>boolean</code> defining whether 
    	 * 							all <code>Gene</code>s should have at least some 
    	 * 							expression data in any case. If <code>true</code>, 
    	 * 							they should.
    	 */
    	public GeneCallRequirement(Collection<Gene> genes, CallType callType, 
    			boolean allGenesWithData) {
    		
    		this(genes, new ArrayList<CallType>(Arrays.asList(callType)), 
    				allGenesWithData);
    	}
    	/**
    	 * Instantiate a <code>GeneCallRequirement</code> for a <code>Collection</code> of 
    	 * <code>Gene</code>s, with several <code>CallType</code>s. It means that  
    	 * at least one of the <code>Gene</code>s must exhibit an expression data call 
    	 * of any of these <code>CallType</code>s in the <code>OntologyEntity</code>, 
    	 * for it to be validated, amongst the call types that were allowed 
    	 * by the <code>AnatDevExpressionQuery</code> for that <code>Gene</code>. 
    	 * Besides, if <code>allGenesWithData</code> is <code>true</code>, 
    	 * then another requirement is that all <code>Gene</code>s 
    	 * must at least have some expression data, of any call type, in any case, 
    	 * amongst the call types that were allowed by the <code>AnatDevExpressionQuery</code> 
    	 * for those <code>Gene</code>s.
    	 * <p>
    	 * If some <code>CallType</code>s are redundant (for instance, 
    	 * <code>EXPRESSION</code> and <code>OVEREXPRESSION</code> are redundant, 
    	 * all genes with over-expression are expressed), 
    	 * an <code>IllegalArgumentException</code> is thrown. This is for the sake of 
    	 * educating users :) This would actually not change the result of the query.
    	 * 
    	 * @param genes		A <code>Collection</code> of <code>Gene</code>s 
    	 * 					which this <code>GeneCallRequirement</code> is related to.
    	 * @param callTypes	A <code>Collection</code> of <code>CallType</code>s,  
    	 * 					at least one <code>Gene</code> in <code>genes</code> 
    	 * 					must exhibit at least one of them. 
    	 * @param allGenesWithData	A <code>boolean</code> defining whether 
    	 * 							all <code>Gene</code>s should have at least some 
    	 * 							expression data in any case. If <code>true</code>, 
    	 * 							they should.
    	 * @throws IllegalArgumentException 	If some <code>CallType</code>s are redundant.
    	 */
    	public GeneCallRequirement(Collection<Gene> genes, Collection<CallType> callTypes, 
    			boolean allGenesWithData) {
    		
			this.setGenes(new ArrayList<Gene>());
			this.setCallTypes(new ArrayList<CallType>());
    		this.addGenes(genes);
    		this.addCallTypes(callTypes);
    		this.setAllGenesWithData(allGenesWithData);
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
    	 * must be satisfied for this <code>GeneCallRequirements</code> to be validated, 
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
    	 * this <code>GeneCallRequirements</code>, an empty <code>Collection</code> 
    	 * if no <code>CallFilter</code> is associated to it yet. 
    	 * <p>
    	 * If <code>gene</code> is associated to several <code>CallFilter</code>s, 
    	 * the value returned by {@link isSatisfyAllCallFilters()} determines whether 
    	 * all of them must be satisfied for it to be validated, or only 
    	 * at least one of them.
    	 * 
    	 * @param gene	A <code>Gene</code> for which the associated <code>CallFilter</code>s 
    	 * 				in this <code>GeneCallRequirements</code> should be returned. 
    	 * @return		A <code>Collection</code> of <code>CallFilter</code>s 
    	 * 				associated to <code>gene</code>. <code>null</code> if 
    	 * 				<code>gene</code> is not part of this 
    	 * 				<code>GeneCallRequirements</code>, an empty <code>Collection</code> 
    	 * 				if no <code>CallFilter</code> is associated to it yet.
    	 * @see #getGenesWithParameters()
    	 * @see #getGenes()
    	 */
    	public Collection<CallFilter> getCallFilter(Gene gene) {
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
		 * Check if the <code>CallType</code>s hold by this <code>GeneCallRequirement</code> 
    	 * would have some redundancy, or forbidden contradiction, 
    	 * if the <code>CallType</code>s in <code>callTypes</code> were added. 
    	 * For instance, <code>EXPRESSION</code> and <code>OVEREXPRESSION</code> 
    	 * are redundant, as all genes with over-expression are expressed.
    	 * This method throws an <code>IllegalArgumentException</code> if a redundancy 
    	 * or forbidden contradiction is detected, with detailed message. 
    	 * 
		 * @param callTypes 	A <code>Collection</code> of <code>CallType</code>s 
		 * 						to check if they would generate a redundancy or contradiction 
		 * 						if they were added to this <code>GeneCallRequirement</code>.
		 * @throws IllegalArgumentException 	If a redundancy or contradiction 
		 * 										would be generated 
		 * 										if <code>callTypes</code> were added 
		 * 										to this <code>GeneCallRequirement</code>. 
		 */
		private void checkCallTypes(Collection<CallType> callTypes) 
		    throws IllegalArgumentException {
			
			Collection<CallType> callTypesTemp = new ArrayList<CallType>();
			callTypesTemp.addAll(this.getCallTypes());
			callTypesTemp.addAll(callTypes);
			
			//redundancy expression / differential expression
			String exceptionMsg = null;
			if (callTypesTemp.contains(CallType.EXPRESSION)) {
			    if (callTypesTemp.contains(CallType.OVEREXPRESSION)) {
				    exceptionMsg = "A gene over-expressed has to be expressed, " +
					    "CallType.EXPRESSION && CallType.OVEREXPRESSION redundant.";
				} else if (callTypesTemp.contains(CallType.UNDEREXPRESSION)) {
				    exceptionMsg = "A gene under-expressed has to be expressed, " +
					    "CallType.EXPRESSION && CallType.UNDEREXPRESSION redundant.";
			    }
			}
			//redundancy between absence of expression types
			//check only if there is not already an error
			if (exceptionMsg == null && callTypesTemp.contains(CallType.NOEXPRESSION) && 
					callTypesTemp.contains(CallType.RELAXEDNOEXPRESSION)) {
				exceptionMsg = "Relaxed no-expression calls include no-expression calls, " +
						"CallType.NOEXPRESSION and CallType.RELAXEDNOEXPRESSION redundant.";
			}
			//we do not check for contradiction, such as CallType.OVEREXPRESSION and 
			//CallType.UNDEREXPRESSION, because it lets opened the possibility 
			//to request for data contradiction between data types, for instance, 
			//requesting over-expression detected by Affymetrix, under-expression 
			//detected by RNA-Seq, and identifying structures where the data 
			//are contradicting
			if (exceptionMsg != null) {
				throw new IllegalArgumentException(exceptionMsg);
			}
		}

		/**
		 * Return the <code>boolean</code> defining whether, when {@link #getGenes()} 
		 * returns several <code>Gene</code>s, all of them must have at least 
    	 * some data in the tested <code>OntologyEntity</code>, for the requirement 
    	 * to be satisfied, whatever the requested <code>CallType</code>s 
    	 * returned by {@link #getCallTypes()} are (understand, some data amongst 
    	 * the call types that were allowed by the <code>AnatDevExpressionQuery</code> 
    	 * for those <code>Gene</code>s).
    	 * <p>
    	 * For instance, if a <code>GeneCallRequirement</code> contains a gene A and a gene B, 
         * and the <code>CallType</code> <code>EXPRESSION</code>, it means that 
         * if gene A is expressed, the condition will be satisfied even if there are no data 
         * available at all for gene B. To avoid this problem, this <code>boolean</code> 
         * must be set to <code>true</code>: only if there are also data available for gene B 
         * (expression, no-expression, ...) the condition will be satisfied. 
         * 
		 * @return 	the <code>boolean</code> defining whether all <code>Gene</code>s 
		 * 			must have at least some expression data for the requirement 
		 * 			to be satisfied. If <code>true</code>, all <code>Gene</code>s 
		 * 			must have expression data.
		 * 
		 * @see #setAllGenesWithData(boolean)
		 */
		public boolean isAllGenesWithData() {
			return this.allGenesWithData;
		}
		/**
		 * Set the <code>boolean</code> defining whether, when {@link #getGenes()} 
		 * returns several <code>Gene</code>s, all of them must have at least 
    	 * some data in the tested <code>OntologyEntity</code>, for the requirement 
    	 * to be satisfied, whatever the requested <code>CallType</code>s 
    	 * returned by {@link #getCallTypes()} are (understand, some data amongst 
    	 * the call types that were allowed by the <code>AnatDevExpressionQuery</code> 
    	 * for those <code>Gene</code>s).
    	 * <p>
    	 * See {@link #isAllGenesWithData()} for more details.
         * 
		 * @param allGenesWithData 	the <code>boolean</code> defining whether 
		 * 							all <code>Gene</code>s must have at least 
		 * 							some expression data for the requirement 
		 * 							to be satisfied. If <code>true</code>, all 
		 * 							<code>Gene</code>s must have expression data. 
		 * @see #isAllGenesWithData()
		 */
		public void setAllGenesWithData(boolean allGenesWithData) {
			this.allGenesWithData = allGenesWithData;
		}
    }
    
}
