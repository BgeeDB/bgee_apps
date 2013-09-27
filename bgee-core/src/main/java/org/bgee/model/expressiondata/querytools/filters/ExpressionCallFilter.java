package org.bgee.model.expressiondata.querytools.filters;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.expressiondata.DataParameters.CallType;
import org.bgee.model.expressiondata.ExpressionCall;

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
	 * Default constructor. 
	 */
	public ExpressionCallFilter() {
		super(new ExpressionCall());
	}

    //****************************************
    // BasicCallFilter METHODS OVERRIDEN
    //****************************************
	@Override
    protected ExpressionCall getReferenceCall() {
        return (ExpressionCall) super.getReferenceCall();
    }
    
    @Override
    public CallType.Expression getCallType() {
        return this.getReferenceCall().getCallType();
    }
    
    //****************************************
    // MERGE METHODS
    //****************************************
    
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
        ExpressionCallFilter mergedFilter = new ExpressionCallFilter();
        super.merge(otherFilter, mergedFilter, sameEntity);
        
        mergedFilter.setIncludeSubstructures(
        		(this.isIncludeSubstructures() || otherFilter.isIncludeSubstructures()));
        mergedFilter.setIncludeSubStages(
        		(this.isIncludeSubStages() || otherFilter.isIncludeSubStages()));

        return log.exit(mergedFilter);
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
		if (this.isIncludeSubstructures() != otherFilter.isIncludeSubstructures() || 
			    this.isIncludeSubStages() != otherFilter.isIncludeSubStages()) {
			return log.exit(false);
		}
		
		return log.exit(true);
	}
	
	//************************************
	//  GETTERS/SETTERS
	//************************************
	/**
	 * Return the {@code boolean} defining whether {@code ExpressionCall}s  
	 * to retrieve should have been generated using data from an anatomical 
	 * entity alone, or by also using data from all its substructures 
	 * by {@link org.bgee.model.ontologycommon.OntologyElement.RelationType 
	 * ISA_PARTOF} relations. 
	 *
	 * @return a {@code boolean} indicating, when {@code true}, that 
	 *         {@code ExpressionCall}s to retrieve should take into account 
	 *         substructures of anatomical entities.
     * @see #isIncludeSubStages()
	 * @see org.bgee.model.expressiondata.ExpressionCall#isIncludeSubstructures()
	 */
	public boolean isIncludeSubstructures() {
		return this.getReferenceCall().isIncludeSubstructures();
	}
	/**
	 * Set the {@code boolean} defining whether {@code ExpressionCall}s  
     * to retrieve should have been generated using data from an anatomical 
     * entity alone, or by also using data from all its substructures 
     * by {@link org.bgee.model.ontologycommon.OntologyElement.RelationType 
     * ISA_PARTOF} relations. 
	 *
	 * @param includeSubstructures a {@code boolean} indicating, when {@code true}, 
	 *                             that {@code ExpressionCall}s to retrieve should 
	 *                             take into account substructures of anatomical 
	 *                             entities.
	 * @see #setIncludeSubStages(boolean)
     * @see org.bgee.model.expressiondata.ExpressionCall#setIncludeSubstructures(boolean)
	 */
	public void setIncludeSubstructures(boolean includeSubstructures) {
		this.getReferenceCall().setIncludeSubstructures(includeSubstructures);
	}

	/**
     * Return the {@code boolean} defining whether {@code ExpressionCall}s  
     * to retrieve should have been generated using data from an developmental 
     * stage alone, or by also using data from all its sub-stages 
     * by {@link org.bgee.model.ontologycommon.OntologyElement.RelationType 
     * ISA_PARTOF} relations. 
     *
     * @return a {@code boolean} indicating, when {@code true}, that 
     *         {@code ExpressionCall}s to retrieve should take into account 
     *         sub-stages of developmental stages.
     * @see #isIncludeSubstructuress()
     * @see org.bgee.model.expressiondata.ExpressionCall#isIncludeSubStage()
     */
	public boolean isIncludeSubStages() {
		return this.getReferenceCall().isIncludeSubStages();
	}
	/**
     * Set the {@code boolean} defining whether {@code ExpressionCall}s  
     * to retrieve should have been generated using data from an developmental 
     * stage alone, or by also using data from all its sub-stages 
     * by {@link org.bgee.model.ontologycommon.OntologyElement.RelationType 
     * ISA_PARTOF} relations. 
     *
     * @param includeSubStages  a {@code boolean} indicating, when {@code true}, 
     *                          that {@code ExpressionCall}s to retrieve should 
     *                          take into account sub-stages of developmental stages.
     * @see #setIncludeSubstructures(boolean)
     * @see org.bgee.model.expressiondata.ExpressionCall#setIncludeSubStages(boolean)
     */
	public void setIncludeSubStages(boolean includeSubStages) {
		this.getReferenceCall().setIncludeSubStages(includeSubStages);
	}
}
