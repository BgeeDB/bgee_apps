package org.bgee.model.reasoner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.bgee.model.expressiondata.ExprDataParams.CallType;
import org.bgee.model.gene.Gene;

/**
 * List and allow to validate custom conditions on gene expression data 
 * of an <code>OntologyElement</code>, when performing an expression reasoning 
 * on an <code>Ontology</code>, using an {@link ExpressionReasoner}. These conditions 
 * define which genes should have which types of expression call 
 * in an <code>OntologyElement</code>, for instance: "Validate OntologyElements 
 * where gene A is expressed, gene B not expressed, and gene C over-expressed". 
 * <p>
 * Each condition is described by a {@link DataRequirement}. 
 * The <code>DataRequirement</code>s part of this <code>DataRequirementValidator</code> 
 * must all be satisfied for an <code>OntologyElement</code> to be validated. 
 * <p>
 * The methods allowing to validate an <code>OntologyElement</code> are 
 * {@link #validate(OntologyElement)} and {@link #validate(Collection)}.
 * <p>
 * Note that an <code>ExpressionReasoner</code> can use several 
 * <code>DataRequirementValidator</code>s, offering different ways of validating 
 * an <code>OntologyElement</code>. Nevertheless, for one of these 
 * <code>DataRequirementValidator</code>s to validate an <code>OntologyElement</code>, 
 * all its <code>DataRequirement</code>s must be satisfied.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 *
 */
public class DataRequirementValidator {

	/**
     * Define a custom condition for an <code>OntologyElement</code> 
     * to be validated, regarding its gene expression data, when performing 
     * an expression reasoning on an <code>Ontology</code>, 
     * using an {@link ExpressionReasoner}. 
     * A <code>DataRequirement</code> is always part of a {@link DataRequirementValidator}, 
     * listing all the necessary conditions that an <code>OntologyElement</code> 
     * must satisfied at the same time to be validated.
     * <p>
     * If any of the <code>Gene</code>s, contained in a <code>DataRequirement</code>, 
     * have any data in an <code>OntologyElement</code>, corresponding to one of the requested  
     * <code>CallType</code>s, then the requirement is satisfied 
     * (like a <code>OR</code> condition both on genes and expression data). 
     * Whether all <code>Gene</code>s must have at least some expression data in any case 
     * can be set by calling {@link #setAllGenesWithData(boolean)} 
     * (understand, some expression data calls amongst the call types that were allowed 
     * by the <code>ExpressionReasoner</code> for a given <code>Gene</code>, not amongst 
     * all the call types available for that <code>Gene</code>). 
     * If no <code>CallType</code>s are specified, then any expression data call type 
     * is accepted (so the requirement is simply that at least one of the <code>Gene</code>s 
     * must have some expression data, amongst the call types that were allowed 
     * by the <code>ExpressionReasoner</code> for a given <code>Gene</code>). 
     * <p>
     * For instance, if a <code>DataRequirement</code> contains a gene A and a gene B, 
     * and two <code>CallType</code>s, <code>EXPRESSION</code> and 
     * <code>NOEXPRESSION</code>, it means that if any of the gene A or B is expressed, 
     * or not expressed, in a given <code>OntologyElement</code>, then the requirement 
     * is satisfied for this <code>OntologyElement</code>.
     * <p>
     * Be careful when using a <code>DataRequirement</code> including several genes: 
     * you will likely want all genes to have at least some data in any case; 
     * in the previous example, if gene A is expressed, 
     * the condition will be satisfied even if there are no data available at all 
     * for gene B. To validate a condition only if all genes have some data, call 
     * the method {@link #setAllGenesWithData(boolean)} with the parameter <code>true</code>. 
     * This is equivalent to adding one <code>DataRequirement</code> for each 
     * individual gene, as part of a same <code>DataRequirementValidator</code>, 
     * to specify that each gene must have some data in any case.
     * <p>
     * As another example, you could consider the <code>STANDARD</code> 
     * {@link org.bgee.model.reasoner.ExpressionReasoner.ValidationType ValidationType} 
     * equivalent to have a <code>DataRequirement</code> containing all <code>Gene</code>s 
     * compared, with the method <code>setAllGenesWithData</code> called with <code>true</code>.
     * Or to have one <code>DataRequirement</code> for each gene individually, 
     * part of a same <code>DataRequirementValidator</code>, meaning that all genes must have 
     * at least some data. 
     * 
	 * @author Frederic Bastian
	 * @see DataRequirementValidator
	 * @version Bgee 13
	 * @since Bgee 13
     *
     */
    public class DataRequirement {
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
         * by the <code>ExpressionReasoner</code> for that <code>Gene</code>)
    	 */
    	private Collection<CallType> callTypes;
    	/**
    	 * A <code>boolean</code> defining whether, when the attribute {@link #genes} 
    	 * contains several <code>Gene</code>s, all of them must have at least 
    	 * some data in the tested <code>OntologyElement</code>, for the requirement 
    	 * to be satisfied, whatever the requested <code>CallType</code>s 
    	 * listed in {@link #callTypes} are (understand, some data  
    	 * amongst the call types that were allowed by the <code>ExpressionReasoner</code> 
    	 * for a given <code>Gene</code>, not amongst all the call types available 
    	 * in the database for that <code>Gene</code>). 
    	 * <p>
    	 * For instance, if a <code>DataRequirement</code> contains a gene A and a gene B, 
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
    	public DataRequirement() {
    		this(null);
    	}
    	/**
    	 * Instantiate a <code>DataRequirement</code> for one <code>Gene</code>, 
    	 * with no <code>CallType</code> specified. It means that this <code>Gene</code>
    	 * must exhibit any expression data call in the <code>OntologyElement</code>, 
    	 * for it to be validated. 
    	 * 
    	 * @param gene 		The <code>Gene</code> which this <code>DataRequirement</code> 
    	 * 					is related to.
    	 * 					Any expression data call type will be accepted. 
    	 */
    	public DataRequirement(Gene gene) {
    		this(gene, (Collection<CallType>) null);
    	}
    	/**
    	 * Instantiate a <code>DataRequirement</code> for one <code>Gene</code> 
    	 * and one <code>CallType</code>. It means that this <code>Gene</code> 
    	 * must absolutely have an expression data call of this <code>CallType</code> 
    	 * in an <code>OntologyElement</code>, for it to be validated. 
    	 * 
    	 * @param gene 		The <code>Gene</code> which this <code>DataRequirement</code> 
    	 * 					is related to.
    	 * @param callType	The <code>CallType</code> the <code>gene</code> must exhibit. 
    	 */
    	public DataRequirement(Gene gene, CallType callType) {
    		this(gene, new ArrayList<CallType>(Arrays.asList(callType)));
    	}
    	/**
    	 * Instantiate a <code>DataRequirement</code> for one <code>Gene</code>, 
    	 * with several <code>CallType</code>s. It means that this <code>Gene</code> 
    	 * must exhibit an expression data call of any of these <code>CallType</code>s 
    	 * in an <code>OntologyElement</code>, for it to be validated. 
    	 * 
    	 * @param gene 		The <code>Gene</code> which this <code>DataRequirement</code> 
    	 * 					is related to.
    	 * @param callTypes	A <code>Collection</code> of <code>CallType</code>s,  
    	 * 					<code>gene</code> must exhibit at least one of them. 
    	 */
    	public DataRequirement(Gene gene, Collection<CallType> callTypes) {
    		this(new ArrayList<Gene>(Arrays.asList(gene)), callTypes, false);
    	}
    	/**
    	 * Instantiate a <code>DataRequirement</code> for a <code>Collection</code> of 
    	 * <code>Gene</code>s, with no <code>CallType</code> specified. It means that 
    	 * at least one of the <code>Gene</code>s must exhibit any expression data call 
    	 * in the <code>OntologyElement</code>, for it to be validated, 
    	 * amongst the call types that were allowed by the <code>ExpressionReasoner</code> 
    	 * for that <code>Gene</code>. Besides, if <code>allGenesWithData</code> 
    	 * is <code>true</code>, then another requirement is that all <code>Gene</code>s 
    	 * must at least have some expression data, of any call type, in any case, 
    	 * amongst the call types that were allowed by the <code>ExpressionReasoner</code> 
    	 * for those <code>Gene</code>s.
    	 * 
    	 * @param genes		A <code>Collection</code> of <code>Gene</code>s 
    	 * 					which this <code>DataRequirement</code> is related to. 
    	 * 					Any expression data call type will be accepted. 
    	 * @param allGenesWithData	A <code>boolean</code> defining whether 
    	 * 							all <code>Gene</code>s should have at least some 
    	 * 							expression data in any case. If <code>true</code>, 
    	 * 							they should.
    	 */
    	public DataRequirement(Collection<Gene> genes, boolean allGenesWithData) {
    		this(genes, (Collection<CallType>) null, allGenesWithData);
    	}
    	/**
    	 * Instantiate a <code>DataRequirement</code> for a <code>Collection</code> of 
    	 * <code>Gene</code>s, and one <code>CallType</code>. It means that 
    	 * at least one of the <code>Gene</code>s must exhibit an expression data call 
    	 * of this <code>CallType</code> in the <code>OntologyElement</code>, 
    	 * for it to be validated, amongst the call types that were allowed 
    	 * by the <code>ExpressionReasoner</code> for that <code>Gene</code>. 
    	 * Besides, if <code>allGenesWithData</code> 
    	 * is <code>true</code>, then another requirement is that all <code>Gene</code>s 
    	 * must at least have some expression data, of any call type, in any case, 
    	 * amongst the call types that were allowed by the <code>ExpressionReasoner</code> 
    	 * for those <code>Gene</code>s. 
    	 * 
    	 * @param genes		A <code>Collection</code> of <code>Gene</code>s 
    	 * 					which this <code>DataRequirement</code> is related to.
    	 * @param callType	The <code>CallType</code> at lest one of the <code>Gene</code>s 
    	 * 					in <code>genes</code> must exhibit. 
    	 * @param allGenesWithData	A <code>boolean</code> defining whether 
    	 * 							all <code>Gene</code>s should have at least some 
    	 * 							expression data in any case. If <code>true</code>, 
    	 * 							they should.
    	 */
    	public DataRequirement(Collection<Gene> genes, CallType callType, 
    			boolean allGenesWithData) {
    		
    		this(genes, new ArrayList<CallType>(Arrays.asList(callType)), 
    				allGenesWithData);
    	}
    	/**
    	 * Instantiate a <code>DataRequirement</code> for a <code>Collection</code> of 
    	 * <code>Gene</code>s, with several <code>CallType</code>s. It means that  
    	 * at least one of the <code>Gene</code>s must exhibit an expression data call 
    	 * of any of these <code>CallType</code>s in the <code>OntologyElement</code>, 
    	 * for it to be validated, amongst the call types that were allowed 
    	 * by the <code>ExpressionReasoner</code> for that <code>Gene</code>. 
    	 * Besides, if <code>allGenesWithData</code> is <code>true</code>, 
    	 * then another requirement is that all <code>Gene</code>s 
    	 * must at least have some expression data, of any call type, in any case, 
    	 * amongst the call types that were allowed by the <code>ExpressionReasoner</code> 
    	 * for those <code>Gene</code>s.
    	 * 
    	 * @param genes		A <code>Collection</code> of <code>Gene</code>s 
    	 * 					which this <code>DataRequirement</code> is related to.
    	 * @param callTypes	A <code>Collection</code> of <code>CallType</code>s,  
    	 * 					at least one <code>Gene</code> in <code>genes</code> 
    	 * 					must exhibit at least one of them. 
    	 * @param allGenesWithData	A <code>boolean</code> defining whether 
    	 * 							all <code>Gene</code>s should have at least some 
    	 * 							expression data in any case. If <code>true</code>, 
    	 * 							they should.
    	 */
    	public DataRequirement(Collection<Gene> genes, Collection<CallType> callTypes, 
    			boolean allGenesWithData) {
    		if (genes != null) {
    			this.setGenes(genes);
    		} else {
    			this.setGenes(new ArrayList<Gene>());
    		}
    		if (callTypes != null) {
    			this.setCallTypes(callTypes);
    		} else {
    			this.setCallTypes(new ArrayList<CallType>());
    		}
    		this.setAllGenesWithData(allGenesWithData);
    	}
    	
    	
    	
    	
    	
    	/**
    	 * Return the <code>Collection</code> of <code>Gene</code>s part of 
    	 * this <code>DataRequirement</code>. At least one of them must have 
    	 * an expression data <code>CallType</code> corresponding to one of those returned by  
    	 * {@link #getCallTypes()}, for the requirement to be satisfied 
    	 * (or any expression data if <code>getCallTypes</code> returns an empty 
    	 * <code>Collection</code>).
    	 * 
    	 * @return 	the <code>Collection</code> of <code>Gene</code>s part of 
    	 * 			this <code>DataRequirement</code>.
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
    	 * Add a <code>Gene</code> to this <code>DataRequirement</code>. 
    	 * At least one of the <code>Gene</code>s part of this <code>DataRequirement</code> 
    	 * must have an expression data <code>CallType</code> corresponding to one 
    	 * of those returned by {@link #getCallTypes()}, for the requirement to be satisfied 
    	 * (or any expression data if <code>getCallTypes</code> returns an empty 
    	 * <code>Collection</code>).
    	 * 
    	 * @param gene 	A <code>Gene</code> to be added to this <code>DataRequirement</code>.
    	 * @see #getGenes()
    	 */
    	public void addGene(Gene gene) {
    		this.genes.add(gene);
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
		 * 
		 * @param callType	A <code>CallType</code> to be added to 
		 * 					this <code>DataRequirement</code>.
		 * @see #getCallTypes()
		 */
		public void addCallType(CallType callType) {
			this.callTypes.add(callType);
		}

		/**
		 * Return the <code>boolean</code> defining whether, when {@link #getGenes()} 
		 * returns several <code>Gene</code>s, all of them must have at least 
    	 * some data in the tested <code>OntologyElement</code>, for the requirement 
    	 * to be satisfied, whatever the requested <code>CallType</code>s 
    	 * returned by {@link #getCallTypes()} are (understand, some data amongst 
    	 * the call types that were allowed by the <code>ExpressionReasoner</code> 
    	 * for those <code>Gene</code>s).
    	 * <p>
    	 * For instance, if a <code>DataRequirement</code> contains a gene A and a gene B, 
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
    	 * the call types that were allowed by the <code>ExpressionReasoner</code> 
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
    
    public boolean validate(OntologyElement e) {
    	
    }
    
    public Collection<OntologyElement> validate(Collection<OntologyElement> elements) {
    	
    }
}
