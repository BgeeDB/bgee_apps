package org.bgee.model.reasoner;

import java.util.Collection;

import org.bgee.model.expressiondata.ExprDataParams.CallType;
import org.bgee.model.gene.Gene;

/**
 * Define a list of custom conditions that must all be satisfied, 
 * for an <code>OntologyElement</code> to be validated, when performing 
 * an expression reasoning on an <code>Ontology</code>, 
 * using an {@link ExpressionReasoner}. These conditions define which genes 
 * should have which types of expression call in an <code>OntologyElement</code>, 
 * for instance, "Validate OntologyElements where gene A is expressed, 
 * gene B not expressed, and gene C over-expressed". 
 * <p>
 * Each condition is described by a 
 * {@link DataRequirement}. The <code>DataRequirement</code>s part of 
 * this <code>DataRequirementSet</code> must all be satisfied 
 * for an <code>OntologyElement</code> to be validated. 
 * <p>
 * Note that an <code>ExpressionReasoner</code> can use several 
 * <code>DataRequirementSet</code>s, offering different ways of validating 
 * an <code>OntologyElement</code>. Nevertheless, for one of these 
 * <code>DataRequirementSet</code>s to be satisfied, all its <code>DataRequirement</code>s 
 * must be satisfied.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 *
 */
public class DataRequirementSet {

	/**
     * Define a custom condition for an <code>OntologyElement</code> 
     * to be validated, when performing an expression reasoning on an <code>Ontology</code>, 
     * using an {@link ExpressionReasoner}. 
     * A <code>DataRequirement</code> is always part of a {@link DataRequirementSet}, 
     * listing all the necessary conditions that an <code>OntologyElement</code> 
     * must satisfied at the same time to be validated.
     * <p>
     * If any of the <code>Gene</code>s, contained in a <code>DataRequirement</code>, 
     * have any data in an <code>OntologyElement</code>, corresponding to one of the requested  
     * <code>CallType</code>s, then the requirement is satisfied 
     * (like a <code>OR</code> condition both on genes and expression data). 
     * Whether all <code>Gene</code>s must have at least some expression data in any case 
     * can be set by calling {@link #setAllGenesWithData(boolean)}.
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
     * individual gene, as part of a same <code>DataRequirementSet</code>, 
     * to specify that each gene must have some data in any case.
     * <p>
     * As another example, you could consider the <code>STANDARD</code> 
     * {@link org.bgee.model.reasoner.ExpressionReasoner.ValidationType ValidationType} 
     * equivalent to have a <code>DataRequirement</code> containing all <code>Gene</code>s 
     * compared, with the method <code>setAllGenesWithData</code> called with <code>true</code>.
     * Or to have one <code>DataRequirement</code> for each gene individually, 
     * part of a same <code>DataRequirementSet</code>, meaning that all genes must have 
     * at least some data. 
     * 
	 * @author Frederic Bastian
	 * @see DataRequirementSet
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
    	 */
    	private Collection<CallType> callTypes;
    	/**
    	 * A <code>boolean</code> defining whether, when the attribute {@link #genes} 
    	 * contains several <code>Gene</code>s, all of them must have at least 
    	 * some data in the tested <code>OntologyElement</code>, for the requirement 
    	 * to be satisfied, whatever the requested <code>CallType</code>s 
    	 * listed in {@link #callTypes} are.
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
    		
    	}
    	
    	/**
    	 * Return the <code>Collection</code> of <code>Gene</code>s part of 
    	 * this <code>DataRequirement</code>. At least one of them must have 
    	 * an expression data <code>CallType</code> corresponding to one of those returned by  
    	 * {@link #getCallTypes()}, for the requirement to be satisfied.
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
    	 * of those returned by {@link #getCallTypes()}, for the requirement to be satisfied.
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
    	 * returned by {@link #getCallTypes()} are.
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
    	 * returned by {@link #getCallTypes()} are.
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
