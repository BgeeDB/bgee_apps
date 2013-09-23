package org.bgee.model.expressiondata.querytools.filters;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.expressiondata.DataParameters.CallType;

/**
 * A <code>BasicCallFilter</code> for <code>EXPRESSED</code> call type. 
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
 * <code>mergeSameEntityCallFilter</code>, <code>canMergeSameEntityCallFilter</code>, 
 * <code>mergeDiffEntitiesCallFilter</code>, and 
 * <code>canMergeDiffEntitiesCallFilter</code>
 */
public class ExpressionCallFilter extends BasicCallFilter {
	/**
	 * <code>Logger</code> of the class. 
	 */
	private final static Logger log = LogManager.getLogger(ExpressionCallFilter.class.getName());

	/**
	 * A <code>boolean</code> defining whether <code>EXPRESSION</code> calls 
	 * should be propagated to 
	 * {@link org.bgee.model.anatdev.AnatomicalEntity AnatEntity} parents 
	 * following {@link org.bgee.model.ontologycommon.OntologyElement.RelationType 
	 * ISA_PARTOF} relations. 
	 * If <code>true</code>, it means that <code>EXPRESSION</code> calls 
	 * in an <code>AnatEntity</code> will take into account expression in its children.
	 */
	private boolean propagateAnatEntities;
	/**
	 * A <code>boolean</code> defining whether <code>EXPRESSION</code> calls 
	 * should be propagated to 
	 * {@link org.bgee.model.anatdev.Stage DevStage} parents 
	 * following {@link org.bgee.model.ontologycommon.OntologyElement.RelationType 
	 * ISA_PARTOF} relations. 
	 * If <code>true</code>, it means that <code>EXPRESSION</code> calls 
	 * in an <code>DevStage</code> will take into account expression in its child stages.
	 */
	private boolean propagateStages;

	/**
	 * Default constructor. 
	 */
	public ExpressionCallFilter() {
		super(CallType.Expression.EXPRESSED);
	}

	@Override
	public CallFilter mergeSameEntityCallFilter(CallFilter callToMerge) {
		log.entry(callToMerge);
		return log.exit(this.merge(callToMerge, true));
	}
	@Override
	public CallFilter mergeDiffEntitiesCallFilter(CallFilter callToMerge) {
		log.entry(callToMerge);
		return log.exit(this.merge(callToMerge, false));
	}
	
	/**
	 * Merges this <code>ExpressionCallFilter</code> with <code>callToMerge</code>, 
	 * and returns the resulting merged new <code>ExpressionCallFilter</code>.
	 * If <code>callToMerge</code> cannot be merged with this <code>BasicCallFilter</code>, 
	 * this method returns <code>null</code>
	 * <p>
	 * If <code>sameEntity</code> is <code>true</code>, this method corresponds to 
	 * {@link CallFilter#mergeSameEntityCallFilter(CallFilter)}, otherwise, to 
	 * {@link CallFilter#mergeDiffEntitiesCallFilter(CallFilter)}.
	 * 
	 * @param callToMerge		a <code>CallFilter</code> to be merged with this one.
	 * @param sameEntity		a <code>boolean</code> defining whether <code>callToMerge</code> 
	 * 							and this <code>ExpressionCallFilter</code> are related to a same 
	 * 							<code>Entity</code>, or different ones. 
	 * @see {@link #canMerge(CallFilter, boolean)}
	 */
	private ExpressionCallFilter merge(CallFilter callToMerge, boolean sameEntity) {
		log.entry(callToMerge, sameEntity);
        //first, determine whether we can merge the CallFilters
        if (!this.canMerge(callToMerge, sameEntity)) {
        	return log.exit(null);
        }

        //OK, let's proceed to the merging
        ExpressionCallFilter otherCall = (ExpressionCallFilter) callToMerge;
        ExpressionCallFilter mergedCall = new ExpressionCallFilter();
        //we blindly perform the merging, it is the responsibility of the method 
        //canMerge to determine whether it is appropriate.
        super.merge(otherCall, mergedCall, sameEntity);
        mergedCall.setPropagateAnatEntities(
        		(this.isPropagateAnatEntities() || otherCall.isPropagateAnatEntities()));
        mergedCall.setPropagateStages(
        		(this.isPropagateStages() || otherCall.isPropagateStages()));

        return log.exit(mergedCall);
	}

	/**
	 * Determines whether this <code>ExpressionCallFilter</code> and 
	 * <code>callToMerge</code> can be merged. 
	 * <p>
	 * If <code>sameEntity</code> is <code>true</code>, it means that <code>callToMerge</code> 
	 * and this <code>ExpressionCallFilter</code> are related to a same <code>Entity</code> 
	 * (see {@link CallFilter#mergeSameEntityCallFilter(CallFilter)}), otherwise, to different 
	 * <code>Entity</code>s (see {@link CallFilter#mergeDiffEntitiesCallFilter(CallFilter)}).
	 * 
	 * @param callToMerge	A <code>CallFilter</code> that is tried to be merged 
	 * 						with this <code>ExpressionCallFilter</code>.
	 * @param sameEntity	a <code>boolean</code> defining whether <code>callToMerge</code> 
	 * 						and this <code>ExpressionCallFilter</code> are related to a same 
	 * 						<code>Entity</code>, or different ones. 
	 * @return				<code>true</code> if they could be merged. 
	 */
	private boolean canMerge(CallFilter callToMerge, boolean sameEntity) {
		log.entry(callToMerge, sameEntity);
		
		if (!(callToMerge instanceof ExpressionCallFilter)) {
        	return log.exit(false);
        }
        ExpressionCallFilter otherCall = (ExpressionCallFilter) callToMerge;
        
		if (!super.canMerge(otherCall, sameEntity)) {
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
		if (this.isPropagateAnatEntities() != otherCall.isPropagateAnatEntities() || 
			    this.isPropagateStages() != otherCall.isPropagateStages()) {
			return log.exit(false);
		}
		
		return log.exit(true);
	}
	
	//************************************
	//  GETTERS/SETTERS
	//************************************
	/**
	 * Return the <code>boolean</code> defining whether <code>EXPRESSION</code> calls 
	 * should be propagated to 
	 * {@link org.bgee.model.anatdev.AnatomicalEntity AnatEntity} parents 
	 * following {@link org.bgee.model.ontologycommon.OntologyElement.RelationType 
	 * ISA_PARTOF} relations. 
	 * If <code>true</code>, it means that <code>EXPRESSION</code> calls 
	 * in an <code>AnatEntity</code> will take into account expression in its children.
	 *
	 * @return 	a <code>boolean</code>, when <code>true</code>, 
	 * 			<code>EXPRESSION</code> calls data are propagated to 
	 * 			<code>AnatEntity</code> parents.
	 * @see #setPropagateAnatEntities(boolean)
	 * @see #isPropagateStages()
	 * @see #setPropagateStages(boolean)
	 */
	public boolean isPropagateAnatEntities() {
		return this.propagateAnatEntities;
	}
	/**
	 * Set the <code>boolean</code> defining whether <code>EXPRESSION</code> calls 
	 * should be propagated to 
	 * {@link org.bgee.model.anatdev.AnatomicalEntity AnatEntity} parents 
	 * following {@link org.bgee.model.ontologycommon.OntologyElement.RelationType 
	 * ISA_PARTOF} relations. 
	 * If <code>true</code>, it means that <code>EXPRESSION</code> calls 
	 * in an <code>AnatEntity</code> will take into account expression in its children.
	 *
	 * @param propagate 	a <code>boolean</code> defining the propagation rule 
	 * 						between <code>AnatEntity</code>s. 
	 * 						If <code>true</code>, data will be propagated to parents.
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
	 * Return the <code>boolean</code> defining whether <code>EXPRESSION</code> calls 
	 * should be propagated to 
	 * {@link org.bgee.model.anatdev.Stage DevStage} parents 
	 * following {@link org.bgee.model.ontologycommon.OntologyElement.RelationType 
	 * ISA_PARTOF} relations. 
	 * If <code>true</code>, it means that <code>EXPRESSION</code> calls 
	 * in a <code>DevStage</code> will take into account expression in its child stages.
	 *
	 * @return 	a <code>boolean</code>, when <code>true</code>, 
	 * 			<code>EXPRESSION</code> calls data are propagated to 
	 * 			<code>DevStage</code> parents.
	 * @see #setPropagateStages(boolean)
	 * @see #isPropagateAnatEntities()
	 * @see #setPropagateAnatEntities(boolean)
	 */
	public boolean isPropagateStages() {
		return this.propagateStages;
	}
	/**
	 * Set the <code>boolean</code> defining whether <code>EXPRESSION</code> calls 
	 * should be propagated to 
	 * {@link org.bgee.model.anatdev.Stage DevStage} parents 
	 * following {@link org.bgee.model.ontologycommon.OntologyElement.RelationType 
	 * ISA_PARTOF} relations. 
	 * If <code>true</code>, it means that <code>EXPRESSION</code> calls 
	 * in a <code>DevStage</code> will take into account expression in its child stages.
	 *
	 * @param propagate	a <code>boolean</code> defining the propagation rule 
	 * 					between <code>DevStage</code>s. 
	 * 					If <code>true</code>, data will be propagated to parents.
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
