package org.bgee.model.expressiondata.querytools.filters;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.expressiondata.DataParameters.CallType;

/**
 * A <code>BasicCallFilter</code> for <code>NOEXPRESSION</code> call type. 
 * Provides methods specific to this call type. 
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
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
		super(CallType.NOEXPRESSION);
	}

	/**
	 * A <code>boolean</code> defining whether <code>NOEXPRESSION</code> calls 
	 * should be propagated to 
	 * {@link org.bgee.model.anatdev.AnatomicalEntity AnatomicalEntity} children 
	 * following {@link org.bgee.model.ontologycommon.OntologyEntity.RelationType 
	 * ISA_PARTOF} relations. 
	 * If <code>true</code>, it means that <code>NOEXPRESSION</code> calls 
	 * in an <code>AnatomicalEntity</code> will take into account absence of expression 
	 * reported in its parents.
	 */
	private boolean propagateAnatEntities;


	
	/**
	 * Return the <code>boolean</code> defining whether <code>NOEXPRESSION</code> calls 
	 * should be propagated to 
	 * {@link org.bgee.model.anatdev.AnatomicalEntity AnatomicalEntity} children 
	 * following {@link org.bgee.model.ontologycommon.OntologyEntity.RelationType 
	 * ISA_PARTOF} relations. 
	 * If <code>true</code>, it means that <code>NOEXPRESSION</code> calls 
	 * in an <code>AnatomicalEntity</code> will take into account absence of expression 
	 * reported in its parents.
	 *
	 * @return 	a <code>boolean</code>, when <code>true</code>, 
	 * 			<code>NOEXPRESSION</code> calls data are propagated to 
	 * 			<code>AnatomicalEntity</code> children.
	 * @see #setPropagateAnatEntities(boolean)
	 */
	public boolean isPropagateAnatEntities() {
		return this.propagateAnatEntities;
	}
	/**
	 * Set the <code>boolean</code> defining whether <code>NOEXPRESSION</code> calls 
	 * should be propagated to 
	 * {@link org.bgee.model.anatdev.AnatomicalEntity AnatomicalEntity} children 
	 * following {@link org.bgee.model.ontologycommon.OntologyEntity.RelationType 
	 * ISA_PARTOF} relations. 
	 * If <code>true</code>, it means that <code>NOEXPRESSION</code> calls 
	 * in an <code>AnatomicalEntity</code> will take into account absence of expression 
	 * reported in its parents.
	 *
	 * @param propagate 	a <code>boolean</code> defining the propagation rule 
	 * 						between <code>AnatomicalEntity</code>s. 
	 * 						If <code>true</code>, data will be propagated to children.
	 * @see #isPropagateAnatEntities()
	 */
	public void setPropagateAnatEntities(boolean propagate) {
		log.entry(propagate);
		this.propagateAnatEntities = propagate;
		log.exit();
	}
}
