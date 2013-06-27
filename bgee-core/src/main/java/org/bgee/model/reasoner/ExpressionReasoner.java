package org.bgee.model.reasoner;

import java.util.Collection;

import org.bgee.model.expressiondata.ExprDataParams.ExprDataCallType;
import org.bgee.model.gene.Gene;

public class ExpressionReasoner 
{
	/**
	 * List the different methods to validate an <code>OntologyElement</code> 
	 * when performing an expression reasoning on an <code>Ontology</code>.
	 * <ul>
	 * <li><code>STANDARD</code>: for an <code>OntologyElement</code> to be validated, 
	 * all requested genes must have data calls in that <code>OntologyElement</code>. 
	 * Any expression data call is accepted (expression, over-expression, no-expression, etc, 
	 * see {@link org.bgee.model.expressiondata.ExprDataParams.ExprDataCallType 
	 * ExprDataCallType}).
	 * <li><code>CONSERVATION</code>: for an <code>OntologyElement</code> to be validated, 
	 * all requested genes must have data calls in agreement: either all genes with  
	 * expression/differential expression calls (<code>ExprDataCallType.EXPRESSION</code>, 
	 * <code>ExprDataCallType.OVEREXPRESSION</code>, 
	 * <code>ExprDataCallType.UNDEREXPRESSION</code>), or all genes with no-expression calls 
	 * (<code>ExprDataCallType.NOEXPRESSION</code>, 
	 * <code>ExprDataCallType.RELAXEDNOEXPRESSION</code>)
	 * <li><code>DIVERGENCE</code>: for an <code>OntologyElement</code> to be validated, 
	 * at least one gene must have data calls different from the other genes, for instance, 
	 * one gene with absence of expression while other genes are expressed. 
	 * <li><code>CUSTOM</code>: expression data calls required are set on a per gene 
	 * or per gene group basis, using {@link ValidationGroup}.
	 * </ul>
	 * 
	 * @author Frederic Bastian
	 * @version Bgee 13
	 * @since Bgee 13
	 */
    public enum ValidationType {
    	STANDARD, CONSERVATION, DIVERGENCE, CUSTOM;
    }
    
    /**
     * Define a custom requirement for an <code>OntologyElement</code> 
     * to be validated when performing an expression reasoning on an <code>Ontology</code>. 
     * If any of the <code>Gene</code>s contained in this group, have any data 
     * in the <code>OntologyElement</code> corresponding to any 
     * <code>ExprDataCallType</code>s listed in this group, 
     * then the requirement is satisfied (like a <code>OR</code> condition both on genes and 
     * expression data). 
     * <p>
     * Note that several <code>ValidationGroup</code>s can be used together to define 
     * several requirements to satisfy. 
     * 
	 * @author Frederic Bastian
	 * @version Bgee 13
	 * @since Bgee 13
     *
     */
    public class ValidationGroup {
    	/**
    	 * A <code>Collection</code> of <code>Gene</code>s for which at least one 
    	 * must have an expression data listed in {@link #callTypes} 
    	 * in the <code>OntologyElement</code> for the requirement to be satisfied. 
    	 */
    	private Collection<Gene> genes;
    	/**
    	 * A <code>Collection</code> of <code>ExprDataCallType</code>s listing 
    	 * the allowed expression data calls, that at least one of the <code>Gene</code>s 
    	 * listed in {@link #genes} must exhibit in an <code>OntologyElement</code>, 
    	 * for the element to be validated. Any of these <code>ExprDataCallType</code>s 
    	 * allows the validation.
    	 */
    	private Collection<ExprDataCallType> callTypes;
    }
    
    /**
     * A <code>ValidationType</code> defining the method used to validate 
     * an <code>OntologyElement</code> when performing an expression reasoning 
     * on an <code>Ontology</code>. 
	 * If this attribute is equal to <code>ValidationType.CUSTOM</code>, then the data 
	 * required for an <code>OntologyElement</code> to be validated are set on a per gene 
	 * or per gene group basis, using {@ #customValidation}.
     */
    private ValidationType validationType;
    
    /**
     * A <code>Collection</code> of <code>ValidationGroup</code>s, listing 
     * all the requirements an <code>OntologyElement</code> must satisfied to be validated, 
     * when performing an expression reasoning on an <code>Ontology</code>, 
     * and that {@link #validationType} is equal to <code>ValidationType.CUSTOM</code>. 
     * <p>
     * Be careful when adding a <code>ValidationGroup</code> including several genes, 
     * you will likely also need to add <code>ValidationGroup</code>s for each individual genes; 
     * for instance, if a gene A and a gene B are part of a same <code>ValidationGroup</code>, 
     * with a requested call type <code>EXPRESSION</code>, it means 
     * that an <code>OntologyElement</code> will be validated as soon as any of the gene A 
     * or the gene B has expression in it, even if the other gene has no expression data at all 
     * in it. So you might want to add a <code>ValidationGroup</code> for gene A, 
     * and a <code>ValidationGroup</code> for gene B, with no data call type specified, 
     * meaning that any data will be sufficient, but necessary, for both genes. 
     * <p>
     * As another example, you can consider the <code>STANDARD</code> 
     * <code>ValidationType</code> equivalent to have a <code>ValidationGroup</code> 
     * for each requested gene individually, with no data call type specified, meaning that 
     * all gene must have data calls, any type accepted.
     */
    private Collection<ValidationGroup> customValidation;
}
