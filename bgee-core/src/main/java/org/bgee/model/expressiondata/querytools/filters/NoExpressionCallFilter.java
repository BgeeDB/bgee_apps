package org.bgee.model.expressiondata.querytools.filters;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.expressiondata.DataParameters.CallType;

/**
 * A {@code BasicCallFilter} for {@code NOTEXPRESSED} call type. 
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
 * {@code merge} and {@code canMerge}.
*/
public class NoExpressionCallFilter extends BasicCallFilter {
	/**
	 * {@code Logger} of the class. 
	 */
	private final static Logger log = LogManager.getLogger(NoExpressionCallFilter.class.getName());

	/**
	 * A {@code boolean} defining whether {@code NOEXPRESSION} calls 
	 * should be propagated to 
	 * {@link org.bgee.model.anatdev.AnatomicalEntity AnatEntity} children 
	 * following {@link org.bgee.model.ontologycommon.OntologyElement.RelationType 
	 * ISA_PARTOF} relations. 
	 * If {@code true}, it means that {@code NOEXPRESSION} calls 
	 * in an {@code AnatEntity} will take into account absence of expression 
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
     * Determines whether this {@code NoExpressionCallFilter} and 
     * {@code filterToMerge} can be merged. 
     * <p>
     * If {@code sameEntity} is {@code true}, it means that {@code filterToMerge} 
     * and this {@code NoExpressionCallFilter} are related to a same {@code Entity} 
     * (see {@link CallFilter#mergeSameEntityCallFilter(CallFilter)}), otherwise, to different 
     * {@code Entity}s (see {@link CallFilter#mergeDiffEntitiesCallFilter(CallFilter)}).
     * 
     * @param filterToMerge   A {@code CallFilter} that is tried to be merged 
     *                      with this {@code NoExpressionCallFilter}.
     * @param sameEntity    a {@code boolean} defining whether {@code filterToMerge} 
     *                      and this {@code NoExpressionCallFilter} are related to a same 
     *                      {@code Entity}, or different ones. 
     * @return              {@code true} if they could be merged. 
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
	 * Return the {@code boolean} defining whether {@code NOEXPRESSION} calls 
	 * should be propagated to 
	 * {@link org.bgee.model.anatdev.AnatomicalEntity AnatEntity} children 
	 * following {@link org.bgee.model.ontologycommon.OntologyElement.RelationType 
	 * ISA_PARTOF} relations. 
	 * If {@code true}, it means that {@code NOEXPRESSION} calls 
	 * in an {@code AnatEntity} will take into account absence of expression 
	 * reported in its parents.
	 *
	 * @return 	a {@code boolean}, when {@code true}, 
	 * 			{@code NOEXPRESSION} calls data are propagated to 
	 * 			{@code AnatEntity} children.
	 * @see #setPropagateAnatEntities(boolean)
	 */
	public boolean isPropagateAnatEntities() {
		return this.propagateAnatEntities;
	}
	/**
	 * Set the {@code boolean} defining whether {@code NOEXPRESSION} calls 
	 * should be propagated to 
	 * {@link org.bgee.model.anatdev.AnatomicalEntity AnatEntity} children 
	 * following {@link org.bgee.model.ontologycommon.OntologyElement.RelationType 
	 * ISA_PARTOF} relations. 
	 * If {@code true}, it means that {@code NOEXPRESSION} calls 
	 * in an {@code AnatEntity} will take into account absence of expression 
	 * reported in its parents.
	 *
	 * @param propagate 	a {@code boolean} defining the propagation rule 
	 * 						between {@code AnatEntity}s. 
	 * 						If {@code true}, data will be propagated to children.
	 * @see #isPropagateAnatEntities()
	 */
	public void setPropagateAnatEntities(boolean propagate) {
		log.entry(propagate);
		this.propagateAnatEntities = propagate;
		log.exit();
	}
}
