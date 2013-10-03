package org.bgee.model.dao.api.expressiondata;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.expressiondata.DiffExpressionCallTO.DiffCallType;
import org.bgee.model.dao.api.expressiondata.DiffExpressionCallTO.Factor;

/**
 * This class allows to provide the parameters specific to differential expression 
 * calls, when using a {@link DAO}, to params the differential expression calls 
 * used during queries. It allows to define conditions on the data types and 
 * data qualities of the differential expression calls to use, the call type  
 * that they should be based on, the experimental factor compared when generating 
 * them, or the minimum number of conditions that were compared during the analyzes 
 * that generated them.
 * <p>
 * Of note, there is no data propagation from anatomical entities nor developmental stages 
 * for differential expression calls, so no associated parameters.
 * <p>
 * In general, a {@code DiffCallType} and a {@code Factor} should be provided (see 
 * {@link #setDiffCallType(DiffCallType)}) and (see {@link #setFactor(Factor)}).
 * Otherwise, It means that different types of call would be used (for instance, 
 * {@code OVEREXPRESSED} and {@code UNDEREXPRESSED}), or comparing different things 
 * (for instance, comparing expression in an organ at different stage, or comparing 
 * expression in different organs at a same stage). This might sometimes be useful 
 * ("give me all the differential expression results of gene X"), but should be 
 * used with caution.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public class DiffExpressionCallParams extends CallParams {
    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = 
            LogManager.getLogger(DiffExpressionCallParams.class.getName());
    /**
     * An {@code int} defining the minimum number of conditions compared in Bgee 
     * when performing a differential expression analysis. As of Bgee 13, 
     * is equals to 3.
     */
    public final static int MINCONDITIONCOUNT = 3;
    
    /**
     * Default constructor.
     */
    public DiffExpressionCallParams() {
        super(new DiffExpressionCallTO());
    }
    
    @Override
    protected DiffExpressionCallTO getReferenceCallTO() {
        return (DiffExpressionCallTO) super.getReferenceCallTO();
    }

    //****************************************
    // MERGE METHODS
    //****************************************
    
    /**
     * @see #canMerge(CallParams, boolean)
     */
    @Override
    public DiffExpressionCallParams merge(CallParams paramsToMerge) {
        log.entry(paramsToMerge);
        //first, determine whether we can merge the CallParams
        if (!this.canMerge(paramsToMerge)) {
            return log.exit(null);
        }

        //OK, let's proceed to the merging
        //we blindly perform the merging here, even if if meaningless, it is the 
        //responsibility of the method canMerge to determine whether it is appropriate.
        DiffExpressionCallParams otherParams = (DiffExpressionCallParams) paramsToMerge;
        DiffExpressionCallParams mergedParams = new DiffExpressionCallParams();
        //of note, data types and qualities are merged by super.merge method
        super.merge(otherParams, mergedParams);
        //conditionCount of this CallParams and of otherParams should be the same, 
        //but it is not the responsibility of this method to decide whether the merging 
        //makes sense, so we use the highest value
        mergedParams.setMinConditionCount(Math.max(this.getMinConditionCount(), 
                otherParams.getMinConditionCount()));
        //Factor and DiffCallType should be the same, but once again...
        //we just pick up one of them
        mergedParams.setFactor(this.getFactor());
        mergedParams.setDiffCallType(this.getDiffCallType());

        return log.exit(mergedParams);
    }

    /**
     * Determines whether this {@code DiffExpressionCallParams} and 
     * {@code paramsToMerge} can be merged. 
     * 
     * @param paramsToMerge A {@code CallParams} that is tried to be merged 
     *                      with this {@code DiffExpressionCallParams}.
     * @return              {@code true} if they could be merged. 
     */
    @Override
    protected boolean canMerge(CallParams paramsToMerge) {
        log.entry(paramsToMerge);
        
        if (!(paramsToMerge instanceof DiffExpressionCallParams)) {
            return log.exit(false);
        }
        DiffExpressionCallParams otherParams = (DiffExpressionCallParams) paramsToMerge;
        
        if (!this.getDiffCallType().equals(otherParams.getDiffCallType()) || 
                !this.getFactor().equals(otherParams.getFactor()) || 
                this.getMinConditionCount() != otherParams.getMinConditionCount()) {
            return log.exit(false);
        }

        //of note, this method also takes care of the check for data types 
        //and qualities
        if (!super.canMerge(otherParams)) {
            return log.exit(false);
        }
        
        return log.exit(true);
    }
    

    //***********************************************
    // GETTERS/SETTERS DELEGATED TO referenceCallTO
    //***********************************************
    /**
     * Returns the {@code DiffCallType} defining the type of the differential 
     * expression calls to be used. If {@code null}, any will be used 
     * (take caution when interpreting the results in that case).
     * 
     * @return  the {@code DiffCallType} defining the type of the differential 
     *          expression calls to be used.
     */
    public DiffCallType getDiffCallType() {
        return this.getReferenceCallTO().getDiffCallType();
    }
    /**
     * Sets the {@code DiffCallType} defining the type of the differential 
     * expression calls to be used. If {@code null}, any will be used 
     * (take caution when interpreting the results in that case).
     * 
     * @param callType  the {@code DiffCallType} defining the type of the differential 
     *                  expression calls to be used.
     */
    public void setDiffCallType(DiffCallType callType) {
        this.getReferenceCallTO().setDiffCallType(callType);
    }

    /**
     * Returns the {@code Factor} defining what should be the experimental factor 
     * compared that generated the differential expression calls to be used. 
     * If {@code null}, any will be used (take caution when interpreting the results 
     * in that case).
     * 
     * @return  the {@code Factor} defining what should be the experimental factor 
     *          compared of the calls to use.
     */
    public Factor getFactor() {
        return this.getReferenceCallTO().getFactor();
    }
    /**
     * Sets the {@code Factor} defining what should be the experimental factor 
     * compared that generated the differential expression calls to be used. 
     * If {@code null}, any will be used (take caution when interpreting the results 
     * in that case).
     * 
     * @param factor    the {@code Factor} defining what should be the experimental 
     *                  factor compared of the calls to use.
     */
    public void setFactor(Factor factor) {
        this.getReferenceCallTO().setFactor(factor);
    }
    
    /**
     * An {@code int} allowing to params the differential calls to be used, 
     * based on the minimum number of conditions that were compared during 
     * the differential expression analyzes that generated them. Default is 
     * {@link #MINCONDITIONCOUNT}. 
     * 
     * @return  the {@code int} defining the minimum number of conditions that 
     *          should have been compared when generating the differential calls 
     *          to be used.
     */
    public int getMinConditionCount() {
        return this.getReferenceCallTO().getMinConditionCount();
    }
    /**
     * An {@code int} allowing to params the differential calls to be used, 
     * based on the minimum number of conditions that were compared during 
     * the differential expression analyzes that generated them. Default is 
     * {@link #MINCONDITIONCOUNT}. This methods will throw an {@code 
     * IllegalArgumentException} if {@code conditionCount} is below this value.
     * 
     * @param conditionCount    the {@code int} defining the minimum number of 
     *                          conditions that should have been compared when 
     *                          generating the differential calls to be used.
     * @throws IllegalArgumentException If {@code conditionCount} is less than 
     *                                  {@code #MINCONDITIONCOUNT}.
     */
    public void setMinConditionCount(int conditionCount) throws IllegalArgumentException {
        this.getReferenceCallTO().setMinConditionCount(conditionCount);
    }
}
