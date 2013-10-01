package org.bgee.model.expressiondata;

import org.bgee.model.expressiondata.DataParameters.CallType;
import org.bgee.model.expressiondata.DataParameters.DiffExpressionFactor;
import org.bgee.model.expressiondata.rawdata.diffexpression.affymetrix.DiffAffyDataHolder;
import org.bgee.model.expressiondata.rawdata.diffexpression.rnaseq.DiffRNASeqDataHolder;

/**
 * A {@link Call} that is a differential expression call obtained from differential 
 * expression analyzes (see {@link 
 * org.bgee.model.expressiondata.rawdata.diffexpression.DiffExpressionAnalysis 
 * DiffExpressionAnalysis}). 
 * <p>
 * The attributes specific to a {@code DiffExpressionCall} are: the minimum number 
 * of conditions that were compared in the differential expression analyzes that 
 * generated it (see {@link #getConditionCount()}), and the experimental factor 
 * along with the comparisons were made (see {@link #getFactor()}).
 * <p>
 * Of note, there is no data propagation from anatomical entities nor developmental stages 
 * for differential expression calls.
 * <p>
 * This class, as all {@code Call}s, allows to retrieve the raw data applicable 
 * to its {@link org.bgee.model.expressiondata.DataParameters.CallType CallType}, 
 * that allowed to generate it. For this class, see {@link #getDiffAffyDataHolder()} 
 * and {@link #getDiffRNASeqDataHolder()} (as of Bgee 13, only Affymetrix and 
 * RNA-Seq are used for differential expression analyzes).
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public class DiffExpressionCall extends Call {

    /**
     * An {@code int} defining the minimum number of conditions that were compared, 
     * among all the differential expression analyzes that allowed to produce this 
     * {@code DiffExpressionCall}.
     */
    private int minConditionCount;
    /**
     * A {@code DiffExpressionFactor} defining what was the comparison factor used 
     * during the differential expression analyzes generating 
     * this {@code DiffExpressionCall}. 
     */
    private final DiffExpressionFactor factor;
    
    /**
     * Constructor providing the call type and the comparison factor of 
     * this {@code DiffExpressionCall}.
     * 
     * @param callType  The {@code DiffExpression} {@code CallType} of this 
     *                  {@code DiffExpressionCall}. 
     * @param factor    A {@code DiffExpressionFactor} specifying 
     *                  the experimental factor of the differential expression 
     *                  analyzes that generated this {@code DiffExpressionCall}.
     */
    public DiffExpressionCall(CallType.DiffExpression callType, 
            DiffExpressionFactor factor) {
        super(callType);
        this.factor = factor;
    }
    
    @Override
    public CallType.DiffExpression getCallType() {
        return (CallType.DiffExpression) super.getCallType();
    }
    
    //*****************************************
    // GETTERS/SETTERS SPECIFIC TO THIS CLASS
    //*****************************************
    /**
     * @return  the {@code int} defining the minimum number of conditions that were 
     *          compared to generate this {@code DiffExpressionCall}, over all 
     *          the differential expression analyzes that allowed to generate it.
     */
    public int getMinConditionCount() {
        return minConditionCount;
    }
    /**
     * @param minConditionCount    An {@code int} that is the minimum number of conditions 
     *                          that were compared to generate this {@code 
     *                          DiffExpressionCall}, over all the differential expression 
     *                          analyzes that allowed to generate it.
     */
    public void setMinConditionCount(int conditionCount) {
        this.minConditionCount = conditionCount;
    }

    /**
     * @return  the {@code DiffExpressionFactor} defining what was the comparison 
     *          factor used during the differential expression analyzes generating 
     *          this {@code DiffExpressionCall}. 
     */
    public DiffExpressionFactor getFactor() {
        return factor;
    }
    

    //*****************************************
    // RawDataHolder METHODS EXPOSED
    //*****************************************
    /**
     * @return  the {@code DiffAffyDataHolder} holding the Affymetrix differential 
     *          expression data related to this {@code DiffExpressionCall}.
     */
    public DiffAffyDataHolder getDiffAffyDataHolder() {
        return super.getRawDataHolder().getDiffAffyDataHolder();
    }
    /**
     * @param holder    the {@code DiffAffyDataHolder} holding the Affymetrix 
     *                  differential expression data related to this 
     *                  {@code DiffExpressionCall}.
     */
    public void setDiffAffyDataHolder(DiffAffyDataHolder holder) {
        super.getRawDataHolder().setDiffAffyDataHolder(holder);
    }
    
    /**
     * @return  the {@code DiffRNASeqDataHolder} holding the RNA-Seq differential 
     *          expression data related to this {@code DiffExpressionCall}.
     */
    public DiffRNASeqDataHolder getDiffRNASeqDataHolder() {
        return super.getRawDataHolder().getDiffRNASeqDataHolder();
    }
    /**
     * @param holder    the {@code DiffRNASeqDataHolder} holding the RNA-Seq 
     *                  differential expression data related to this 
     *                  {@code DiffExpressionCall}.
     */
    public void setDiffRNASeqDataHolder(DiffRNASeqDataHolder holder) {
        super.getRawDataHolder().setDiffRNASeqDataHolder(holder);
    }
}
