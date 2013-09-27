package org.bgee.model.expressiondata;

import org.bgee.model.expressiondata.DataParameters.CallType;
import org.bgee.model.expressiondata.DataParameters.DiffExpressionFactor;
import org.bgee.model.expressiondata.rawdata.affymetrix.AffymetrixDataHolder;
import org.bgee.model.expressiondata.rawdata.rnaseq.RNASeqDataHolder;

/**
 * This class represents the differential expression call of a {@code Gene}, 
 * obtained from differential expression analyzes (see {@link 
 * org.bgee.model.expressiondata.rawdata.diffexpression.DiffExpressionAnalysis 
 * DiffExpressionAnalysis}). This {@code DiffExpressionCall}, as any {@code Call},  
 * is most of the time hold by a {@code Gene}, an {@code AnatElement}, or 
 * a {@code DevElement}, as this class only manages the expression state part, 
 * not the spatio-temporal location, or gene definition part. It is an overall summary 
 * of the data contained in Bgee (for instance, a reconciliation of the differential 
 * expression states of a gene in several differential analyzes, studying a same 
 * organ at a same stage).
 * <p>
 * A {@code Call} provides the methods to set and retrieve the {@link 
 * DataParameters.DataType DataType}s that allowed to generate it, and their 
 * associated {@link DataParameters.DataQuality DataQuality}s. These methods 
 * check that the {@code DataType}s provided are consistent with 
 * a {@code DiffExpressionCall}.
 * <p>
 * This class also allows to retrieve the minimum number of conditions that were 
 * compared in the differential expression analyzes that generate this call 
 * (see {@link #getConditionCount()}), and the experimental factor along with 
 * the comparisons were made (see {@link #getFactor()}).
 * <p>
 * Finally, this class allows to retrieve the raw data that generated this call, 
 * see {@link #getAffymetrixDataHolder()} and {@link #getRNASeqDataHolder()} 
 * (only those data types allow to generate differential expression calls).
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
    protected DiffExpressionCall(CallType.DiffExpression callType, 
            DiffExpressionFactor factor) {
        super(callType);
        this.factor = factor;
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
