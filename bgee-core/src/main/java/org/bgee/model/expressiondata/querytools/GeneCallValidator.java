package org.bgee.model.expressiondata.querytools;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
     * Define custom conditions for an <code>AnatDevEntity</code> 
     * to be validated, regarding its gene expression data, when performing 
     * an expression reasoning using an {@link AnatDevExpressionQuery}. 
     * A <code>GeneCallRequirement</code> is always part of a {@link GeneCallValidator}, 
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
	 * @see GeneCallValidator
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
    	 * Instantiate a <code>GeneCallValidator</code> with requirements 
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
    	 * Instantiate a <code>GeneCallValidator</code> with requirements 
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
