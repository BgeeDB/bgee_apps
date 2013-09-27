package org.bgee.model.expressiondata;

import org.bgee.model.expressiondata.DataParameters.CallType;
import org.bgee.model.expressiondata.DataParameters.DiffExpressionFactor;
import org.bgee.model.expressiondata.rawdata.affymetrix.AffymetrixDataHolder;
import org.bgee.model.expressiondata.rawdata.rnaseq.RNASeqDataHolder;

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
 * This class, as all {@code Call}s, allows to retrieve the raw data applicable 
 * to its {@link org.bgee.model.expressiondata.DataParameters.CallType CallType}, 
 * that allowed to generate it. For this class, see {@link #getAffymetrixDataHolder()} 
 * and {@link #getRNASeqDataHolder()} (as of Bgee 13, only Affymetrix and RNA-Seq 
 * are used for differential expression analyzes).
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public class DiffExpressionCall extends Call {

    /**
     * An {@code int} defining the minimum number of conditions that were compared 
     * to generate this {@code DiffExpressionCall}.
     */
    private int conditionCount;
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
    public int getConditionCount() {
        return conditionCount;
    }
    /**
     * @param conditionCount    An {@code int} that is the minimum number of conditions 
     *                          that were compared to generate this {@code 
     *                          DiffExpressionCall}, over all the differential expression 
     *                          analyzes that allowed to generate it.
     */
    public void setConditionCount(int conditionCount) {
        this.conditionCount = conditionCount;
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
     * @return  the {@code AffymetrixDataHolder} holding the Affymetrix data 
     *          related to this {@code DiffExpressionCall}.
     */
    public AffymetrixDataHolder getAffymetrixDataHolder() {
        return super.getRawDataHolder().getAffymetrixDataHolder();
    }
    /**
     * @param holder    the {@code AffymetrixDataHolder} holding the Affymetrix data 
     *                  related to this {@code DiffExpressionCall}.
     */
    public void setAffymetrixDataHolder(AffymetrixDataHolder affyDataHolder) {
        super.getRawDataHolder().setAffymetrixDataHolder(affyDataHolder);
    }
    
    /**
     * @return  the {@code RNASeqDataHolder} holding the RNA-Seq data 
     *          related to this {@code DiffExpressionCall}.
     */
    public RNASeqDataHolder getRNASeqDataHolder() {
        return super.getRawDataHolder().getRNASeqDataHolder();
    }
    /**
     * @param holder    the {@code RNASeqDataHolder} holding the RNA-Seq data 
     *                  related to this {@code DiffExpressionCall}.
     */
    public void setRNASeqDataHolder(RNASeqDataHolder rnaSeqDataHolder) {
        super.getRawDataHolder().setRNASeqDataHolder(rnaSeqDataHolder);
    }
}
