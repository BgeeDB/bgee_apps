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
 * <code>merge</code> and <code>canMerge</code>.
*/
public class NoExpressionCallFilter extends BasicCallFilter {
	/**
	 * <code>Logger</code> of the class. 
	 */
	private final static Logger log = LogManager.getLogger(NoExpressionCallFilter.class.getName());

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
     * Default constructor. 
     */
    public NoExpressionCallFilter() {
        super(CallType.Expression.NOTEXPRESSED);
    }

    /**
     * @see #canMerge(CallFilter, boolean)
     */
    @Override
    protected NoExpressionCallFilter merge(CallFilter filterToMerge, boolean sameEntity) {
        log.entry(filterToMerge, sameEntity);
        //first, determine whether we can merge the CallFilters
        if (!this.canMerge(filterToMerge, sameEntity)) {
            return log.exit(null);
        }

        //OK, let's proceed to the merging
        //we blindly perform the merging, it is the responsibility of the method 
        //canMerge to determine whether it is appropriate.
        NoExpressionCallFilter otherFilter  = (NoExpressionCallFilter) filterToMerge;
        NoExpressionCallFilter mergedCall = new NoExpressionCallFilter();
        super.merge(otherFilter, mergedCall, sameEntity);
        
        mergedCall.setPropagateAnatEntities(
                (this.isPropagateAnatEntities() || otherFilter.isPropagateAnatEntities()));

        return log.exit(mergedCall);
    }

    /**
     * Determines whether this <code>NoExpressionCallFilter</code> and 
     * <code>filterToMerge</code> can be merged. 
     * <p>
     * If <code>sameEntity</code> is <code>true</code>, it means that <code>filterToMerge</code> 
     * and this <code>NoExpressionCallFilter</code> are related to a same <code>Entity</code> 
     * (see {@link CallFilter#mergeSameEntityCallFilter(CallFilter)}), otherwise, to different 
     * <code>Entity</code>s (see {@link CallFilter#mergeDiffEntitiesCallFilter(CallFilter)}).
     * 
     * @param filterToMerge   A <code>CallFilter</code> that is tried to be merged 
     *                      with this <code>NoExpressionCallFilter</code>.
     * @param sameEntity    a <code>boolean</code> defining whether <code>filterToMerge</code> 
     *                      and this <code>NoExpressionCallFilter</code> are related to a same 
     *                      <code>Entity</code>, or different ones. 
     * @return              <code>true</code> if they could be merged. 
     */
    private boolean canMerge(CallFilter filterToMerge, boolean sameEntity) {
        log.entry(filterToMerge, sameEntity);
        
        if (!(filterToMerge instanceof NoExpressionCallFilter)) {
            return log.exit(false);
        }
        NoExpressionCallFilter otherFilter = (NoExpressionCallFilter) filterToMerge;
        
        if (!super.canMerge(otherFilter, sameEntity)) {
            return log.exit(false);
        }
        
        //NoExpressionCallFilters with different expression propagation rules 
        //are not merged, because no-expression calls using propagation would use 
        //the best data qualities over all parent structures. As a result, 
        //it would not be possible to retrieve data qualities when no propagation is used, 
        //and so, not possible to check for the data quality conditions held 
        //by an NoExpressionCallFilter not using propagation.
        //An exception is that, if an NoExpressionCallFilter not using propagation 
        //was not requesting any specific quality, it could be merged with 
        //a NoExpressionCallFilter using propagation. But it would be a nightmare to deal 
        //with all these specific cases in other parts of the code...
        //So, we simply do not merge in that case.
        if (this.isPropagateAnatEntities() != otherFilter.isPropagateAnatEntities()) {
            return log.exit(false);
        }
        
        return log.exit(true);
    }
    
    //************************************
    //  GETTERS/SETTERS
    //************************************

	
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
