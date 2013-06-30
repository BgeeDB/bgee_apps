package org.bgee.model.expressiondata;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.bgee.model.expressiondata.ExprDataParams.CallType;
import org.bgee.model.gene.Gene;

/**
 * List and allow to validate custom conditions on gene expression data 
 * of an <code>OntologyElement</code>, when performing an expression reasoning 
 * on an <code>Ontology</code>, using an {@link AnatDevExpressionQuery}. These conditions 
 * define which genes should have which types of expression call 
 * in an <code>OntologyElement</code>, for instance: "Validate OntologyElements 
 * where gene A is expressed, gene B not expressed, and gene C over-expressed". 
 * <p>
 * Each condition is described by a {@link GeneCallRequirement}. 
 * The <code>GeneCallRequirement</code>s part of this <code>GeneCallValidator</code> 
 * must all be satisfied for an <code>OntologyElement</code> to be validated. 
 * <p>
 * The methods allowing to validate an <code>OntologyElement</code> are 
 * {@link #validate(OntologyElement)} and {@link #validate(Collection)}.
 * <p>
 * Note that an <code>AnatDevExpressionQuery</code> can use several 
 * <code>GeneCallValidator</code>s, offering different ways of validating 
 * an <code>OntologyElement</code>. Nevertheless, for one of these 
 * <code>GeneCallValidator</code>s to validate an <code>OntologyElement</code>, 
 * all its <code>GeneCallRequirement</code>s must be satisfied.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 *
 */
public class GeneCallValidator {

	/**
     * Define a custom condition for an <code>OntologyElement</code> 
     * to be validated, regarding its gene expression data, when performing 
     * an expression reasoning on an <code>Ontology</code>, 
     * using an {@link AnatDevExpressionQuery}. 
     * A <code>GeneCallRequirement</code> is always part of a {@link GeneCallValidator}, 
     * listing all the necessary conditions that an <code>OntologyElement</code> 
     * must satisfied at the same time to be validated.
     * <p>
     * If any of the <code>Gene</code>s, contained in a <code>GeneCallRequirement</code>, 
     * have any data in an <code>OntologyElement</code>, corresponding to one of the requested  
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
     * or not expressed, in a given <code>OntologyElement</code>, then the requirement 
     * is satisfied for this <code>OntologyElement</code>.
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
    	 * A <code>Collection</code> of <code>Gene</code>s for which at least one of them 
    	 * must have an expression data call listed in {@link #callTypes} 
    	 * for the requirement to be satisfied. 
    	 */
    	private Collection<Gene> genes;
    	/**
    	 * A <code>Collection</code> of <code>CallType</code>s listing 
    	 * the allowed expression data calls, that at least one of the <code>Gene</code>s 
    	 * listed in {@link #genes} must exhibit in an <code>OntologyElement</code>, 
    	 * for the element to be validated. Any of these <code>CallType</code>s 
    	 * allows the validation.
    	 * <p>
    	 * If no <code>CallType</code>s are specified, then any expression data call type 
    	 * is accepted (so the requirement is simply that at least one 
    	 * of the <code>Gene</code>s in <code>genes</code> must have some expression data, 
    	 * amongst the call types that were allowed 
         * by the <code>AnatDevExpressionQuery</code> for that <code>Gene</code>)
    	 */
    	private Collection<CallType> callTypes;
    	/**
    	 * A <code>boolean</code> defining whether, when the attribute {@link #genes} 
    	 * contains several <code>Gene</code>s, all of them must have at least 
    	 * some data in the tested <code>OntologyElement</code>, for the requirement 
    	 * to be satisfied, whatever the requested <code>CallType</code>s 
    	 * listed in {@link #callTypes} are (understand, some data  
    	 * amongst the call types that were allowed by the <code>AnatDevExpressionQuery</code> 
    	 * for a given <code>Gene</code>, not amongst all the call types available 
    	 * in the database for that <code>Gene</code>). 
    	 * <p>
    	 * For instance, if a <code>GeneCallRequirement</code> contains a gene A and a gene B, 
         * and the <code>CallType</code> <code>EXPRESSION</code>, it means that 
         * if gene A is expressed, the condition will be satisfied even if there are no data 
         * available at all for gene B. To avoid this problem, this <code>boolean</code> 
         * must be set to <code>true</code>: only of there are also data available for gene B 
         * (expression, no-expression, ...) the condition will be satisfied. 
    	 */
    	private boolean allGenesWithData;
    	
    	/**
    	 * Default constructor. 
    	 */
    	public GeneCallRequirement() {
    		this(null);
    	}
    	/**
    	 * Instantiate a <code>GeneCallRequirement</code> for one <code>Gene</code>, 
    	 * with no <code>CallType</code> specified. It means that this <code>Gene</code>
    	 * must exhibit any expression data call in the <code>OntologyElement</code>, 
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
    	 * in an <code>OntologyElement</code>, for it to be validated. 
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
    	 * in an <code>OntologyElement</code>, for it to be validated. 
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
    	 * in the <code>OntologyElement</code>, for it to be validated, 
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
    	 * of this <code>CallType</code> in the <code>OntologyElement</code>, 
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
    	 * of any of these <code>CallType</code>s in the <code>OntologyElement</code>, 
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
    	 * Return the <code>Collection</code> of <code>Gene</code>s part of 
    	 * this <code>GeneCallRequirement</code>. At least one of them must have 
    	 * an expression data <code>CallType</code> corresponding to one of those returned by  
    	 * {@link #getCallTypes()}, for the requirement to be satisfied 
    	 * (or any expression data if <code>getCallTypes</code> returns an empty 
    	 * <code>Collection</code>).
    	 * 
    	 * @return 	the <code>Collection</code> of <code>Gene</code>s part of 
    	 * 			this <code>GeneCallRequirement</code>.
    	 * @see #addGene(Gene)
    	 */
    	public Collection<Gene> getGenes() {
    		return this.genes;
    	}
    	/**
    	 * @param genes		a <code>Collection</code> of <code>Gene</code>s to set 
    	 * 					{@link #genes}.
    	 * @see #addGene(Gene)
    	 * @see #getGenes()
    	 */
    	private void setGenes(Collection<Gene> genes) {
    		this.genes = genes;
    	}
    	/**
    	 * Add a <code>Gene</code> to this <code>GeneCallRequirement</code>. 
    	 * At least one of the <code>Gene</code>s part of this <code>GeneCallRequirement</code> 
    	 * must have an expression data <code>CallType</code> corresponding to one 
    	 * of those returned by {@link #getCallTypes()}, for the requirement to be satisfied 
    	 * (or any expression data if <code>getCallTypes</code> returns an empty 
    	 * <code>Collection</code>).
    	 * 
    	 * @param gene 	A <code>Gene</code> to be added to this <code>GeneCallRequirement</code>.
    	 * @see #getGenes()
    	 * @see #addGenes(Collection)
    	 */
    	public void addGene(Gene gene) {
    		if (gene == null) {
				return;
			}
    		this.genes.add(gene);
    	}
    	/**
    	 * Add a <code>Collection</code> of <code>Gene</code>s to 
    	 * this <code>GeneCallRequirement</code>. 
    	 * At least one of the <code>Gene</code>s part of this <code>GeneCallRequirement</code> 
    	 * must have an expression data <code>CallType</code> corresponding to one 
    	 * of those returned by {@link #getCallTypes()}, for the requirement to be satisfied 
    	 * (or any expression data if <code>getCallTypes</code> returns an empty 
    	 * <code>Collection</code>).
    	 * 
    	 * @param genes 	A <code>Collection</code> of <code>Gene</code>s 
    	 * 					to be added to this <code>GeneCallRequirement</code>.
    	 * @see #getGenes()
    	 * @see #addGene(Gene)
    	 */
    	public void addGenes(Collection<Gene> genes) {
    		if (genes == null) {
				return;
			}
    		this.genes.addAll(genes);
    	}

		/**
		 * Return the <code>Collection</code> of <code>CallType</code>s listing 
    	 * the allowed expression data calls, that at least one of the <code>Gene</code>s 
    	 * returned by {@link #getGenes()} must exhibit in an <code>OntologyElement</code>, 
    	 * for the element to be validated. Any of these <code>CallType</code>s 
    	 * allows the validation.
    	 * <p>
    	 * If the returned <code>Collection</code> is empty, then any expression data 
    	 * call type is accepted (so the requirement is simply that at least one 
    	 * of the <code>Gene</code>s returned by <code>getGenes</code> must have 
    	 * some expression data)
    	 * 
		 * @return 	the <code>Collection</code> of <code>CallType</code>s defining 
		 * 			which expression data calls allow to validate 
		 * 			an <code>OntologyElement</code>.
		 * @see #addCallType(CallType)
		 */
		public Collection<CallType> getCallTypes() {
			return callTypes;
		}
		/**
		 * @param callTypes A <code>Collection</code> of <code>CallType</code>s 
		 * 					to set {@link #callTypes} 
		 * @see #addCallType(CallType)
		 * @see #addCallTypes(Collection)
		 * @see #getCallTypes()
		 */
		private void setCallTypes(Collection<CallType> callTypes) {
			this.callTypes = callTypes;
		}
		/**
		 * Add a <code>CallType</code> to the list of allowed expression data calls, 
		 * that at least one of the <code>Gene</code>s returned by {@link #getGenes()} 
		 * must exhibit in an <code>OntologyElement</code>, for the element 
		 * to be validated. 
    	 * <p>
    	 * If some <code>CallType</code>s hold by this <code>GeneCallRequirement</code> 
    	 * would be redundant following a call to this method (for instance, 
    	 * <code>EXPRESSION</code> and <code>OVEREXPRESSION</code> are redundant, 
    	 * all genes with over-expression are expressed), 
    	 * an <code>IllegalArgumentException</code> is thrown and the operation 
    	 * is not performed. This is for the sake of educating users :) 
    	 * This would actually not change the result of the query.
		 * 
		 * @param callType	A <code>CallType</code> to be added to 
		 * 					this <code>GeneCallRequirement</code>.
    	 * @throws IllegalArgumentException 	If some <code>CallType</code>s hold 
    	 * 										by this <code>GeneCallRequirement</code> 
    	 * 										would be redundant following a call 
    	 * 										to this method.
		 * @see #getCallTypes()
		 * @see #addCallTypes(Collection)
		 */
		public void addCallType(CallType callType) {
			if (callType == null) {
				return;
			}
			this.addCallTypes(Arrays.asList(callType));
		}
		/**
		 * Add a <code>Collection</code> of <code>CallType</code>s to the list 
		 * of allowed expression data calls, that at least one of the <code>Gene</code>s 
		 * returned by {@link #getGenes()} must exhibit in an <code>OntologyElement</code>, 
		 * for the element to be validated. 
    	 * <p>
    	 * If some <code>CallType</code>s hold by this <code>GeneCallRequirement</code> 
    	 * would be redundant following a call to this method (for instance, 
    	 * <code>EXPRESSION</code> and <code>OVEREXPRESSION</code> are redundant, 
    	 * all genes with over-expression are expressed), 
    	 * an <code>IllegalArgumentException</code> is thrown and the operation 
    	 * is not performed. This is for the sake of educating users :) 
    	 * This would actually not change the result of the query.
		 * 
		 * @param callTypes	A <code>Collection</code> of <code>CallType</code>s 
		 * 					to be added to this <code>GeneCallRequirement</code>.
    	 * @throws IllegalArgumentException 	If some <code>CallType</code>s hold 
    	 * 										by this <code>GeneCallRequirement</code> 
    	 * 										wuld be redundant following a call 
    	 * 										to this method.
		 * @see #getCallTypes()
		 * @see #addCallType(CallType)
		 */
		public void addCallTypes(Collection<CallType> callTypes) {
			if (callTypes == null) {
				return;
			}
			this.checkCallTypes(callTypes);
			this.callTypes.addAll(callTypes);
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
    	 * some data in the tested <code>OntologyElement</code>, for the requirement 
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
    	 * some data in the tested <code>OntologyElement</code>, for the requirement 
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
