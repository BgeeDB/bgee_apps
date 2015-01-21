package org.bgee.model.dao.api.expressiondata;

import org.bgee.model.dao.api.expressiondata.CallDAO.CallTO.DataState;
import org.bgee.model.dao.api.expressiondata.DiffExpressionCallDAO.DiffExpressionCallTO;
import org.bgee.model.dao.api.expressiondata.DiffExpressionCallDAO.DiffExpressionCallTO.DiffExprCallType;
import org.bgee.model.dao.api.expressiondata.DiffExpressionCallDAO.DiffExpressionCallTO.ComparisonFactor;

/**
 * This class allows to provide the parameters specific to differential expression 
 * calls, when using a {@link org.bgee.model.dao.api.DAO DAO}, to params 
 * the differential expression calls 
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
 * {@link #setDiffCallType(DiffCallType)} and 
 * {@link #setComparisonFactor(ComparisonFactor)}).
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
/*
 * (non-javadoc)
 * The super class {@code CallParams} provides all the methods related to 
 * data types and their {@code DataState}s, with a {@code protected} visibility.
 * Subclasses should then increase the visibility of the methods relative to 
 * their appropriate data types.
 * 
 * WARNING: if you add parameters specific to this class, you will likely need 
 * to modify the methods merge, canMerge, hasDataRestrictions, and 
 * getDifferentParametersCount.
 */
public class DiffExpressionCallParams extends CallParams {
        
//    /**
//     * An {@code int} defining the minimum number of conditions compared in Bgee 
//     * when performing a differential expression analysis. As of Bgee 13, 
//     * is equals to 3.
//     */
//    public final static int MINCONDITIONCOUNT = 3;

    /**
     * A {@code boolean} defining whether both requested minimum contributions (Affymetrix 
     * and RNA-seq data) have to be satisfied or at least one of the two.
     */
    private boolean whatever;

    /**
     * Default constructor.
     */
    public DiffExpressionCallParams() {
        super(new DiffExpressionCallTO());
        this.setComparisonFactor(null);
        this.setDiffExprCallTypeAffymetrix(null);
        this.setDiffExprCallTypeRNASeq(null);
        this.setWhatever(true);
    }
    
    @Override
    protected DiffExpressionCallTO getReferenceCallTO() {
        return (DiffExpressionCallTO) super.getReferenceCallTO();
    }

    //****************************************
    // MERGE METHODS
    //****************************************
    
//    /**
//     * @see #canMerge(CallParams)
//     */
//    @Override
//    protected DiffExpressionCallParams merge(CallParams paramsToMerge) {
//        log.entry(paramsToMerge);
//        //first, determine whether we can merge the CallParams
//        if (!this.canMerge(paramsToMerge)) {
//            return log.exit(null);
//        }
//
//        //OK, let's proceed to the merging
//        //we blindly perform the merging here, even if if meaningless, it is the 
//        //responsibility of the method canMerge to determine whether it is appropriate.
//        DiffExpressionCallParams otherParams = (DiffExpressionCallParams) paramsToMerge;
//        DiffExpressionCallParams mergedParams = new DiffExpressionCallParams();
//        //of note, data types and qualities are merged by super.merge method
//        super.merge(otherParams, mergedParams);
//        
//        mergedParams.setMinConditionCount(Math.max(this.getMinConditionCount(), 
//                otherParams.getMinConditionCount()));
//        //this condition check that if one of the CallParams did not have any 
//        //condition on Factor, it will remain that way (more data retrieved).
//        //otherwise, we simply pick up the value of one of the CallParams
//        if (this.getFactor() != null && otherParams.getFactor() != null) {
//            mergedParams.setFactor(this.getFactor());
//        }
//        //this condition check that if one of the CallParams did not have any 
//        //condition on CallType, it will remain that way (more data retrieved).
//        //otherwise, we simply pick up the value of one of the CallParams
//        if (this.getDiffCallType() != null && otherParams.getDiffCallType() != null) {
//            mergedParams.setDiffCallType(this.getDiffCallType());
//        }
//
//        return log.exit(mergedParams);
//    }
//
//    /**
//     * Determines whether this {@code DiffExpressionCallParams} and 
//     * {@code paramsToMerge} can be merged. 
//     * 
//     * @param paramsToMerge A {@code CallParams} that is tried to be merged 
//     *                      with this {@code DiffExpressionCallParams}.
//     * @return              {@code true} if they could be merged. 
//     */
//    @Override
//    protected boolean canMerge(CallParams paramsToMerge) {
//        log.entry(paramsToMerge);
//        
//        if (!(paramsToMerge instanceof DiffExpressionCallParams)) {
//            return log.exit(false);
//        }
//        DiffExpressionCallParams otherParams = (DiffExpressionCallParams) paramsToMerge;
//
//        //here we cannot just keep the smallest condition count, 
//        //the summary of differential expression calls are different 
//        //depending on the minimum number of conditions requested.
//        //so whatever happens, we cannot merge DiffExpressionCallParams 
//        //with different minConditionCounts (even if one of them is the default one, 
//        //this is why we do this check before the hasDataRestrictions check below)
//        if (this.getMinConditionCount() != otherParams.getMinConditionCount()) {
//            return log.exit(false);
//        }
//        
//        //if one of the CallParams has no restriction at all (all data retrieved), 
//        //then obviously a merging can occur, as the data retrieved by one CallParams 
//        //will be a subset of the data retrieved by the other one.
//        if (!this.hasDataRestrictions() || !otherParams.hasDataRestrictions()) {
//            return log.exit(true);
//        }
//        
//        //if there is more than 1 difference between the parameters of 
//        //the two DiffExpressionCallParams, merge not possible 
//        //(no "OR" condition possible).
//        if (this.getDifferentParametersCount(otherParams) > 1) {
//            return log.exit(false);
//        }
//        
//        //now that we have checked there were not more than 1 difference, 
//        //we can merge the CallParams if one of their parameter is null and 
//        //the other is not (one CallParams requests all data, and the other one 
//        //only a subset, so it will work). Otherwise, they have to be equal.
//        
//        //if one of the DiffCallType is null, then it means no restriction on it, all data 
//        //whatever their call type will be used, so we can merge the CallParams whatever 
//        //the value of the call type for the other CallParams is.
//        if (this.getDiffCallType() != null && otherParams.getDiffCallType() != null &&
//            !this.getDiffCallType().equals(otherParams.getDiffCallType())) {
//            return log.exit(false);
//        }
//        //if one of the Factor is null, then it means no restriction on it, all data 
//        //whatever their factor will be used, so we can merge the CallParams whatever 
//        //the value of Factor for the other CallParams is.
//        if (this.getFactor() != null && otherParams.getFactor() != null &&
//            !this.getFactor().equals(otherParams.getFactor())) {
//              return log.exit(false);
//        }
//
//        //of note, this method also takes care of the check for data types 
//        //and qualities
//        if (!super.canMerge(otherParams)) {
//            return log.exit(false);
//        }
//        
//        return log.exit(true);
//    }
//    
//    @Override
//    protected boolean hasDataRestrictions() {
//        log.entry();
//        if (this.getFactor() != null || this.getDiffCallType() != null || 
//                this.getMinConditionCount() != MINCONDITIONCOUNT) {
//            return log.exit(true);
//        }
//        
//        return log.exit(super.hasDataRestrictions());
//    }
//    
//    @Override
//    protected int getDifferentParametersCount(CallParams otherParams) {
//        log.entry();
//        int diff = 0;
//        if (otherParams instanceof DiffExpressionCallParams) {
//            DiffExpressionCallParams params = (DiffExpressionCallParams) otherParams;
//            
//            if (  (this.getDiffCallType() == null && params.getDiffCallType() != null) || 
//                    
//                  (this.getDiffCallType() != null && 
//                  !this.getDiffCallType().equals(params.getDiffCallType()))) {
//                diff ++;
//            }
//            if (  (this.getFactor() == null && params.getFactor() != null) || 
//                    
//                  (this.getFactor() != null && 
//                  !this.getFactor().equals(params.getFactor()))) {
//                diff ++;
//            }
//            if (this.getMinConditionCount() != params.getMinConditionCount()) {
//                diff++;
//            }
//        } else {
//            //number of parameters in this class restraining data retrieved
//            diff = 3;
//        }
//        
//        return log.exit(diff + super.getDifferentParametersCount(otherParams));
//    }
    

    //**************************************
    // GETTERS/SETTERS FOR PARAMETERS SPECIFIC TO THIS CLASS, 
    // DELEGATED TO referenceCallTO
    //**************************************
    /**
     * Returns the {@code DiffCallType} defining the type of the differential 
     * expression calls to be used for Affymetrix data. If {@code null}, any will be used 
     * (take caution when interpreting the results in that case).
     * 
     * @return  the {@code DiffCallType} defining the type of the differential 
     *          expression calls to be used for Affymetrix data.
     */
    public DiffExprCallType getDiffExprCallTypeAffymetrix() {
        return this.getReferenceCallTO().getDiffExprCallTypeAffymetrix();
    }
    /**
     * Sets the {@code DiffCallType} defining the type of the differential 
     * expression calls to be used for Affymetrix data. If {@code null}, any will be used 
     * (take caution when interpreting the results in that case).
     * 
     * @param callType  the {@code DiffCallType} defining the type of the differential 
     *                  expression calls to be used for Affymetrix data.
     */
    public void setDiffExprCallTypeAffymetrix(DiffExprCallType callType) {
        this.getReferenceCallTO().setDiffExprCallTypeAffymetrix(callType);
    }

    /**
     * Returns the {@code DiffCallType} defining the type of the differential 
     * expression calls to be used for RNA-seq data. If {@code null}, any will be used 
     * (take caution when interpreting the results in that case).
     * 
     * @return  the {@code DiffCallType} defining the type of the differential 
     *          expression calls to be used.
     */
    public DiffExprCallType getDiffExprCallTypeRNASeq() {
        return this.getReferenceCallTO().getDiffExprCallTypeRNASeq();
    }
    /**
     * Sets the {@code DiffCallType} defining the type of the differential 
     * expression calls to be used for RNA-seq data. If {@code null}, any will be used 
     * (take caution when interpreting the results in that case).
     * 
     * @param callType  the {@code DiffCallType} defining the type of the differential 
     *                  expression calls to be used for RNA-seq data.
     */
    public void setDiffExprCallTypeRNASeq(DiffExprCallType callType) {
        this.getReferenceCallTO().setDiffExprCallTypeRNASeq(callType);
    }

    /**
     * Returns the {@code ComparisonFactor} defining what should be the experimental factor 
     * compared that generated the differential expression calls to be used. 
     * If {@code null}, any will be used (take caution when interpreting the results 
     * in that case).
     * 
     * @return  the {@code ComparisonFactor} defining what should be the experimental factor 
     *          compared of the calls to use.
     */
    public ComparisonFactor getComparisonFactor() {
        return this.getReferenceCallTO().getComparisonFactor();
    }
    /**
     * Sets the {@code ComparisonFactor} defining what should be the experimental factor 
     * compared that generated the differential expression calls to be used. 
     * If {@code null}, any will be used (take caution when interpreting the results 
     * in that case).
     * 
     * @param factor    the {@code ComparisonFactor} defining what should be the experimental 
     *                  factor compared of the calls to use.
     */
    public void setComparisonFactor(ComparisonFactor factor) {
        this.getReferenceCallTO().setComparisonFactor(factor);
    }
    
    /**
     * A {@code boolean} defining whether both requested minimum contributions (Affymetrix 
     * and RNA-seq data) have to be satisfied or at least one of the two. 
     * 
     * @return  the {@code boolean} defining whether .
     */
    public boolean isWhatever() {
        return this.whatever;
    }
    
    /**
     * @param whatever  the {@code boolean} defining whether both requested minimum contributions 
     *                  (Affymetrix and RNA-seq data) have to be satisfied or at least one of the two.
     */
    public void setWhatever(boolean whatever) {
        this.whatever = whatever;
    }

    //***********************************************
    // SUPER CLASS GETTERS/SETTERS WITH INCREASED VISIBLITY
    //***********************************************
    /*
     * (non-javadoc)
     * The super class {@code CallParams} provides all the methods related to 
     * data types and their {@code DataState}s, with a {@code protected} visibility.
     * Subclasses should then increase the visibility of the methods relative to 
     * their appropriate data types.
     */
    @Override
    public DataState getAffymetrixData() {
        return super.getAffymetrixData();
    }
    @Override
    public void setAffymetrixData(DataState minContribution) {
        super.setAffymetrixData(minContribution);
    }

    @Override
    public DataState getRNASeqData() {
        return super.getRNASeqData();
    }
    @Override
    public void setRNASeqData(DataState minContribution) {
        super.setRNASeqData(minContribution);
    }
}
