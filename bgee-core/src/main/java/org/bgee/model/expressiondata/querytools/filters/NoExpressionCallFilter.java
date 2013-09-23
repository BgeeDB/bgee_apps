package org.bgee.model.expressiondata.querytools.filters;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.expressiondata.DataParameters.CallType;

/**
 * A <code>BasicCallFilter</code> for <code>NOTEXPRESSED</code> call type. 
 * Provides methods specific to this call type. 
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @see ExpressionCallFilter
 * @see DiffExpressionCallFilter
 * @since Bgee 13
 */
/*
* (non-javadoc)
* If you add attributes to this class, you might need to modify the methods 
* <code>mergeSameEntityCallFilter</code>, <code>canMergeSameEntityCallFilter</code>, 
* <code>mergeDiffEntitiesCallFilter</code>, and 
* <code>canMergeDiffEntitiesCallFilter</code>
*/
public class NoExpressionCallFilter extends BasicCallFilter {
	/**
	 * <code>Logger</code> of the class. 
	 */
	private final static Logger log = LogManager.getLogger(NoExpressionCallFilter.class.getName());

	/**
	 * Default constructor. 
	 */
	public NoExpressionCallFilter() {
		super(CallType.Expression.NOTEXPRESSED);
	}

	/**
	 * A <code>boolean</code> defining whether <code>NOEXPRESSION</code> calls 
	 * should be propagated to 
	 * {@link org.bgee.model.anatdev.AnatomicalEntity AnatEntity} children 
	 * following {@link org.bgee.model.ontologycommon.OntologyElement.RelationType 
	 * ISA_PARTOF} relations. 
	 * If <code>true</code>, it means that <code>NOEXPRESSION</code> calls 
	 * in an <code>AnatEntity</code> will take into account absence of expression 
	 * reported in its parents.
	 */
	private boolean propagateAnatEntities;


	
	/**
	 * Return the <code>boolean</code> defining whether <code>NOEXPRESSION</code> calls 
	 * should be propagated to 
	 * {@link org.bgee.model.anatdev.AnatomicalEntity AnatEntity} children 
	 * following {@link org.bgee.model.ontologycommon.OntologyElement.RelationType 
	 * ISA_PARTOF} relations. 
	 * If <code>true</code>, it means that <code>NOEXPRESSION</code> calls 
	 * in an <code>AnatEntity</code> will take into account absence of expression 
	 * reported in its parents.
	 *
	 * @return 	a <code>boolean</code>, when <code>true</code>, 
	 * 			<code>NOEXPRESSION</code> calls data are propagated to 
	 * 			<code>AnatEntity</code> children.
	 * @see #setPropagateAnatEntities(boolean)
	 */
	public boolean isPropagateAnatEntities() {
		return this.propagateAnatEntities;
	}
	/**
	 * Set the <code>boolean</code> defining whether <code>NOEXPRESSION</code> calls 
	 * should be propagated to 
	 * {@link org.bgee.model.anatdev.AnatomicalEntity AnatEntity} children 
	 * following {@link org.bgee.model.ontologycommon.OntologyElement.RelationType 
	 * ISA_PARTOF} relations. 
	 * If <code>true</code>, it means that <code>NOEXPRESSION</code> calls 
	 * in an <code>AnatEntity</code> will take into account absence of expression 
	 * reported in its parents.
	 *
	 * @param propagate 	a <code>boolean</code> defining the propagation rule 
	 * 						between <code>AnatEntity</code>s. 
	 * 						If <code>true</code>, data will be propagated to children.
	 * @see #isPropagateAnatEntities()
	 */
	public void setPropagateAnatEntities(boolean propagate) {
		log.entry(propagate);
		this.propagateAnatEntities = propagate;
		log.exit();
	}
}
