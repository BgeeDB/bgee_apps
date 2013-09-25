package org.bgee.model.expressiondata.querytools.filters;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.expressiondata.DataParameters.CallType;

/**
 * A {@code BasicCallFilter} for {@code EXPRESSED} call type. 
 * Provides methods specific to this call type. 
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @see NoExpressionCallFilter
 * @see DiffExpressionCallFilter
 * @since Bgee 01
 */
/*
 * (non-javadoc)
 * If you add attributes to this class, you might need to modify the methods 
 * {@code merge} and {@code canMerge}.
 */
public class ExpressionCallFilter extends BasicCallFilter {
	/**
	 * {@code Logger} of the class. 
	 */
	private final static Logger log = 
	        LogManager.getLogger(ExpressionCallFilter.class.getName());

	/**
	 * A {@code boolean} defining whether {@code EXPRESSION} calls 
	 * should be propagated to 
	 * {@link org.bgee.model.anatdev.AnatomicalEntity AnatEntity} parents 
	 * following {@link org.bgee.model.ontologycommon.OntologyElement.RelationType 
	 * ISA_PARTOF} relations. 
	 * If {@code true}, it means that {@code EXPRESSION} calls 
	 * in an {@code AnatEntity} will take into account expression in its children.
	 */
	private boolean propagateAnatEntities;
	/**
	 * A {@code boolean} defining whether {@code EXPRESSION} calls 
	 * should be propagated to 
	 * {@link org.bgee.model.anatdev.Stage DevStage} parents 
	 * following {@link org.bgee.model.ontologycommon.OntologyElement.RelationType 
	 * ISA_PARTOF} relations. 
	 * If {@code true}, it means that {@code EXPRESSION} calls 
	 * in an {@code DevStage} will take into account expression in its child stages.
	 */
	private boolean propagateStages;

	/**
	 * Default constructor. 
	 */
	public ExpressionCallFilter() {
		super(CallType.Expression.EXPRESSED);
	}
	
	/**
     * @see #canMerge(CallFilter, boolean)
     */
    @Override
	protected ExpressionCallFilter merge(CallFilter filterToMerge, boolean sameEntity) {
		log.entry(filterToMerge, sameEntity);
        //first, determine whether we can merge the CallFilters
        if (!this.canMerge(filterToMerge, sameEntity)) {
        	return log.exit(null);
        }

        //OK, let's proceed to the merging
        //we blindly perform the merging, it is the responsibility of the method 
        //canMerge to determine whether it is appropriate.
        ExpressionCallFilter otherFilter = (ExpressionCallFilter) filterToMerge;
        ExpressionCallFilter mergedCall = new ExpressionCallFilter();
        super.merge(otherFilter, mergedCall, sameEntity);
        
        mergedCall.setPropagateAnatEntities(
        		(this.isPropagateAnatEntities() || otherFilter.isPropagateAnatEntities()));
        mergedCall.setPropagateStages(
        		(this.isPropagateStages() || otherFilter.isPropagateStages()));

        return log.exit(mergedCall);
	}

	/**
	 * Determines whether this {@code ExpressionCallFilter} and 
	 * {@code filterToMerge} can be merged. 
	 * <p>
	 * If {@code sameEntity} is {@code true}, it means that {@code filterToMerge} 
	 * and this {@code ExpressionCallFilter} are related to a same {@code Entity} 
	 * (see {@link CallFilter#mergeSameEntityCallFilter(CallFilter)}), otherwise, to different 
	 * {@code Entity}s (see {@link CallFilter#mergeDiffEntitiesCallFilter(CallFilter)}).
	 * 
	 * @param filterToMerge	A {@code CallFilter} that is tried to be merged 
	 * 						with this {@code ExpressionCallFilter}.
	 * @param sameEntity	a {@code boolean} defining whether {@code filterToMerge} 
	 * 						and this {@code ExpressionCallFilter} are related to a same 
	 * 						{@code Entity}, or different ones. 
	 * @return				{@code true} if they could be merged. 
	 */
	private boolean canMerge(CallFilter filterToMerge, boolean sameEntity) {
		log.entry(filterToMerge, sameEntity);
		
		if (!(filterToMerge instanceof ExpressionCallFilter)) {
        	return log.exit(false);
        }
        ExpressionCallFilter otherFilter = (ExpressionCallFilter) filterToMerge;
        
		if (!super.canMerge(otherFilter, sameEntity)) {
			return log.exit(false);
		}
		
		//ExpressionCallFilters with different expression propagation rules 
		//are not merged, because expression calls using propagation would use 
		//the best data qualities over all sub-structures/sub-stages. As a result, 
		//it would not be possible to retrieve data qualities when no propagation is used, 
		//and so, not possible to check for the data quality conditions held 
		//by an ExpressionCallFilter not using propagation.
		//An exception is that, if an ExpressionCallFilter not using propagation 
		//was not requesting any specific quality, it could be merged with 
		//an ExpressionCallFilter using propagation. But it would be a nightmare to deal 
		//with all these specific cases in other parts of the code...
		//So, we simply do not merge in that case.
		if (this.isPropagateAnatEntities() != otherFilter.isPropagateAnatEntities() || 
			    this.isPropagateStages() != otherFilter.isPropagateStages()) {
			return log.exit(false);
		}
		
		return log.exit(true);
	}
	
	//************************************
	//  GETTERS/SETTERS
	//************************************
	/**
	 * Return the {@code boolean} defining whether {@code EXPRESSION} calls 
	 * should be propagated to 
	 * {@link org.bgee.model.anatdev.AnatomicalEntity AnatEntity} parents 
	 * following {@link org.bgee.model.ontologycommon.OntologyElement.RelationType 
	 * ISA_PARTOF} relations. 
	 * If {@code true}, it means that {@code EXPRESSION} calls 
	 * in an {@code AnatEntity} will take into account expression in its children.
	 *
	 * @return 	a {@code boolean}, when {@code true}, 
	 * 			{@code EXPRESSION} calls data are propagated to 
	 * 			{@code AnatEntity} parents.
	 * @see #setPropagateAnatEntities(boolean)
	 * @see #isPropagateStages()
	 * @see #setPropagateStages(boolean)
	 */
	public boolean isPropagateAnatEntities() {
		return this.propagateAnatEntities;
	}
	/**
	 * Set the {@code boolean} defining whether {@code EXPRESSION} calls 
	 * should be propagated to 
	 * {@link org.bgee.model.anatdev.AnatomicalEntity AnatEntity} parents 
	 * following {@link org.bgee.model.ontologycommon.OntologyElement.RelationType 
	 * ISA_PARTOF} relations. 
	 * If {@code true}, it means that {@code EXPRESSION} calls 
	 * in an {@code AnatEntity} will take into account expression in its children.
	 *
	 * @param propagate 	a {@code boolean} defining the propagation rule 
	 * 						between {@code AnatEntity}s. 
	 * 						If {@code true}, data will be propagated to parents.
	 * @see #isPropagateAnatEntities()
	 * @see #isPropagateStages()
	 * @see #setPropagateStages(boolean)
	 */
	public void setPropagateAnatEntities(boolean propagate) {
		log.entry(propagate);
		this.propagateAnatEntities = propagate;
		log.exit();
	}

	/**
	 * Return the {@code boolean} defining whether {@code EXPRESSION} calls 
	 * should be propagated to 
	 * {@link org.bgee.model.anatdev.Stage DevStage} parents 
	 * following {@link org.bgee.model.ontologycommon.OntologyElement.RelationType 
	 * ISA_PARTOF} relations. 
	 * If {@code true}, it means that {@code EXPRESSION} calls 
	 * in a {@code DevStage} will take into account expression in its child stages.
	 *
	 * @return 	a {@code boolean}, when {@code true}, 
	 * 			{@code EXPRESSION} calls data are propagated to 
	 * 			{@code DevStage} parents.
	 * @see #setPropagateStages(boolean)
	 * @see #isPropagateAnatEntities()
	 * @see #setPropagateAnatEntities(boolean)
	 */
	public boolean isPropagateStages() {
		return this.propagateStages;
	}
	/**
	 * Set the {@code boolean} defining whether {@code EXPRESSION} calls 
	 * should be propagated to 
	 * {@link org.bgee.model.anatdev.Stage DevStage} parents 
	 * following {@link org.bgee.model.ontologycommon.OntologyElement.RelationType 
	 * ISA_PARTOF} relations. 
	 * If {@code true}, it means that {@code EXPRESSION} calls 
	 * in a {@code DevStage} will take into account expression in its child stages.
	 *
	 * @param propagate	a {@code boolean} defining the propagation rule 
	 * 					between {@code DevStage}s. 
	 * 					If {@code true}, data will be propagated to parents.
	 * @see #isPropagateStages()
	 * @see #isPropagateAnatEntities()
	 * @see #setPropagateAnatEntities(boolean)
	 */
	public void setPropagateStages(boolean propagate) {
		log.entry(propagate);
		this.propagateStages = propagate;
		log.exit();
	}
}
