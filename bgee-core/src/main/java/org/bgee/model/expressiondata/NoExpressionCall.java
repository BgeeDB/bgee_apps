package org.bgee.model.expressiondata;

import org.bgee.model.expressiondata.DataParameters.CallType;
import org.bgee.model.expressiondata.rawdata.affymetrix.AffymetrixDataHolder;
import org.bgee.model.expressiondata.rawdata.insitu.InSituDataHolder;
import org.bgee.model.expressiondata.rawdata.rnaseq.RNASeqDataHolder;

/**
 * A {@link Call} reporting an explicit absence of expression. 
 * <p>
 * The attribute specific to a {@code NoExpressionCall} is whether this call 
 * was generated using data from an anatomical entity alone, or by also using 
 * data from all its parent structures, see {@link #isIncludeParentStructures()}.
 * <p>
 * This class, as all {@code Call}s, allows to retrieve the raw data applicable 
 * to its {@link org.bgee.model.expressiondata.DataParameters.CallType CallType}, 
 * that allowed to generate it. For this class, all types of raw data but ESTs 
 * are applicable, see {@link #getAffymetrixDataHolder()}, {@link #getInSituDataHolder()}, 
 * and {@link #getRNASeqDataHolder()} (EST data are not used to generate no-expression 
 * calls).
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public class NoExpressionCall extends Call {

    /**
     * A {@code boolean} defining whether this {@code NoExpressionCall} was generated 
     * using the data from an anatomical entity alone, or by also considering 
     * all its parents by {@code ISA_PARTOF} relation, even indirect. 
     * If {@code true}, all its parents were considered. So for instance, 
     * if B is_a A, and absence of expression has been reported in A, 
     * then B could benefit from this information.
     */
    private boolean includeParentStructures;
    
    /**
     * Default constructor.
     */
    public NoExpressionCall() {
        super(CallType.Expression.NOTEXPRESSED);
    }

    @Override
    public CallType.Expression getCallType() {
        return (CallType.Expression) super.getCallType();
    }
    
    //*****************************************
    // GETTERS/SETTERS SPECIFIC TO THIS CLASS
    //*****************************************
    /**
     * Returns the {@code boolean} defining whether this {@code NoExpressionCall} 
     * was generated using the data from an anatomical entity alone, or by also 
     * considering all its parents by {@code ISA_PARTOF} relation, even indirect. 
     * If {@code true}, all its parents were considered. So for instance, 
     * if B is_a A, and absence of expression has been reported in A, 
     * then B could benefit from this information.
     * 
     * @return  the {@code boolean} indicating, when {@code true}, that 
     *          this {@code NoExpressionCall} included parents of 
     *          an anatomical entity.
     */
    public boolean isIncludeParentStructures() {
        return includeParentStructures;
    }
    /**
     * Sets the {@code boolean} defining whether this {@code NoExpressionCall} 
     * was generated using the data from an anatomical entity alone, or by also 
     * considering all its parents by {@code ISA_PARTOF} relation, even indirect. 
     * If {@code true}, all its parents were considered. So for instance, 
     * if B is_a A, and absence of expression has been reported in A, 
     * then B could benefit from this information.
     * 
     * @param includeParents  the {@code boolean} indicating, when {@code true}, 
     *                        that this {@code NoExpressionCall} included parents 
     *                        of an anatomical entity.
     */
    public void setIncludeSubstructures(boolean includeParents) {
        this.includeParentStructures = includeParents;
    }

    //*****************************************
    // RawDataHolder METHODS EXPOSED
    //*****************************************
    /**
     * @return  the {@code AffymetrixDataHolder} holding the Affymetrix data 
     *          related to this {@code ExpressionCall}.
     */
    public AffymetrixDataHolder getAffymetrixDataHolder() {
        return super.getRawDataHolder().getAffymetrixDataHolder();
    }
    /**
     * @param holder    the {@code AffymetrixDataHolder} holding the Affymetrix data 
     *                  related to this {@code ExpressionCall}.
     */
    public void setAffymetrixDataHolder(AffymetrixDataHolder affyDataHolder) {
        super.getRawDataHolder().setAffymetrixDataHolder(affyDataHolder);
    }
    
    /**
     * @return  the {@code InSituDataHolder} holding the in situ data 
     *          related to this {@code ExpressionCall}.
     */
    public InSituDataHolder getInSituDataHolder() {
        return super.getRawDataHolder().getInSituDataHolder();
    }
    /**
     * @param holder    the {@code InSituDataHolder} holding the in situ data 
     *                  related to this {@code ExpressionCall}.
     */
    public void setInSituDataHolder(InSituDataHolder inSituDataHolder) {
        super.getRawDataHolder().setInSituDataHolder(inSituDataHolder);
    }
    
    /**
     * @return  the {@code RNASeqDataHolder} holding the RNA-Seq data 
     *          related to this {@code ExpressionCall}.
     */
    public RNASeqDataHolder getRNASeqDataHolder() {
        return super.getRawDataHolder().getRNASeqDataHolder();
    }
    /**
     * @param holder    the {@code RNASeqDataHolder} holding the RNA-Seq data 
     *                  related to this {@code ExpressionCall}.
     */
    public void setRNASeqDataHolder(RNASeqDataHolder rnaSeqDataHolder) {
        super.getRawDataHolder().setRNASeqDataHolder(rnaSeqDataHolder);
    }
}
