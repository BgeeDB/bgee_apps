package org.bgee.model.dao.api.expressiondata;

import org.bgee.model.dao.api.expressiondata.DiffExpressionCallTO.DiffCallType;
import org.bgee.model.dao.api.expressiondata.DiffExpressionCallTO.Factor;

/**
 * This class allows to provide the parameters specific to differential expression 
 * calls, when using a {@link DAO}, to filter the differential expression calls 
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
     * An {@code int} defining the minimum number of conditions compared in Bgee 
     * when performing a differential expression analysis. As of Bgee 13, 
     * is equals to 3.
     */
    public final static int MINCONDITIONCOUNT = 3;
    
    /**
     * A {@code DiffExpressionCallTO} that will hold some parameters of this 
     * {@code DiffExpressionCallParams}. This is because they have some parameters 
     * in common, so the corresponding methods will be delegated to 
     * {@code referenceCallTO}.
     * <p>
     * Only the appropriate methods will be exposed.
     */
    private final DiffExpressionCallTO referenceCallTO;
    
    /**
     * Default constructor.
     */
    public DiffExpressionCallParams() {
        super();
        this.referenceCallTO = new DiffExpressionCallTO();
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
        return this.referenceCallTO.getDiffCallType();
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
        this.referenceCallTO.setDiffCallType(callType);
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
        return this.referenceCallTO.getFactor();
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
        this.referenceCallTO.setFactor(factor);
    }
    
    /**
     * An {@code int} allowing to filter the differential calls to be used, 
     * based on the minimum number of conditions that were compared during 
     * the differential expression analyzes that generated them. Default is 
     * {@link #MINCONDITIONCOUNT}. 
     * 
     * @return  the {@code int} defining the minimum number of conditions that 
     *          should have been compared when generating the differential calls 
     *          to be used.
     */
    public int getMinConditionCount() {
        return this.referenceCallTO.getMinConditionCount();
    }
    /**
     * An {@code int} allowing to filter the differential calls to be used, 
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
        this.referenceCallTO.setMinConditionCount(conditionCount);
    }
}
