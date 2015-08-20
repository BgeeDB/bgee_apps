package org.bgee.model.expressiondata;

import org.bgee.model.expressiondata.DataParameters.CallType;

/**
 * A {@link Call} reporting an expression. 
 * <p>
 * The attributes specific to a {@code ExpressionCall} are: whether this call 
 * was generated using data from an anatomical entity alone, or by also using 
 * data from all its substructures, see {@link #isIncludeSubstructures()}; and 
 * whether this call was generated using data from a developmental stage alone, 
 * or by also using data from all its sub-stages, see {@link #isIncludeSubStages()}.
 * <p>
 * This class, as all {@code Call}s, allows to retrieve the raw data applicable 
 * to its {@link org.bgee.model.expressiondata.DataParameters.CallType CallType}, 
 * that allowed to generate it. For this class, all types of raw data are applicable, 
 * see {@link #getAffymetrixDataHolder()}, {@link #getESTDataHolder()}, 
 * {@link #getInSituDataHolder()}, and {@link #getRNASeqDataHolder()}.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public class ExpressionCall extends Call{

    /**
     * A {@code boolean} defining whether this {@code ExpressionCall} was generated 
     * using the data from an anatomical entity alone, or by also considering 
     * all its descendants by {@code ISA_PARTOF} relation, even indirect. 
     * If {@code true}, all its descendants were considered. 
     */
    private boolean includeSubstructures;
    /**
     * A {@code boolean} defining whether this {@code ExpressionCall} was generated 
     * using the data from a developmental stage alone, or by also considering 
     * all its descendants by {@code ISA_PARTOF} relation, even indirect. 
     * If {@code true}, all its descendants were considered. 
     */
    private boolean includeSubStages;
    
    /**
     * Default constructor.
     */
    public ExpressionCall() {
        super(CallType.Expression.EXPRESSED);
    }
    
    @Override
    public CallType.Expression getCallType() {
        return (CallType.Expression) super.getCallType();
    }

    //*****************************************
    // GETTERS/SETTERS SPECIFIC TO THIS CLASS
    //*****************************************
    /**
     * Returns the {@code boolean} defining whether this {@code ExpressionCall} 
     * was generated using the data from an anatomical entity alone, or by also 
     * considering all its descendants by {@code ISA_PARTOF} relation, even indirect. 
     * If {@code true}, all its descendants were considered. 
     * 
     * @return  the {@code boolean} indicating, when {@code true}, that 
     *          this {@code ExpressionCall} included substructures of 
     *          an anatomical entity.
     */
    public boolean isIncludeSubstructures() {
        return includeSubstructures;
    }
    /**
     * Sets the {@code boolean} defining whether this {@code ExpressionCall} 
     * was generated using the data from an anatomical entity alone, or by also 
     * considering all its descendants by {@code ISA_PARTOF} relation, even indirect. 
     * If {@code true}, all its descendants were considered. 
     * 
     * @param includeSubstructures  the {@code boolean} indicating, when {@code true}, 
     *                              that this {@code ExpressionCall} included substructures 
     *                              of an anatomical entity.
     */
    public void setIncludeSubstructures(boolean includeSubstructures) {
        this.includeSubstructures = includeSubstructures;
    }

    /**
     * Returns the {@code boolean} defining whether this {@code ExpressionCall} 
     * was generated using the data from a developmental stage alone, or by also 
     * considering all its descendants by {@code ISA_PARTOF} relation, even indirect. 
     * If {@code true}, all its descendants were considered. 
     * 
     * @return  the {@code boolean} indicating, when {@code true}, that 
     *          this {@code ExpressionCall} included sub-stages of 
     *          a developmental stage.
     */
    public boolean isIncludeSubStages() {
        return includeSubStages;
    }
    /**
     * Sets the {@code boolean} defining whether this {@code ExpressionCall} 
     * was generated using the data from a developmental stage alone, or by also 
     * considering all its descendants by {@code ISA_PARTOF} relation, even indirect. 
     * If {@code true}, all its descendants were considered. 
     * 
     * @param includeSubStages  the {@code boolean} indicating, when {@code true}, 
     *                          that this {@code ExpressionCall} included sub-stages 
     *                          of a developmental stage.
     */
    public void setIncludeSubStages(boolean includeSubStages) {
        this.includeSubStages = includeSubStages;
    }
    
}
