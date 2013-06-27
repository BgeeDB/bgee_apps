package org.bgee.model.expressiondata;

/**
 * Parameters for differential expression data. 
 * Note that this class is always used as part of a {@link ExprDataParams}, 
 * and as such, the quality, data types, etc, of the differential expression data is defined 
 * by the <code>ExprDataParams</code> class. 
 * This class stores parameters specific to differential expression data, that are, 
 * the number of conditions compared when generating the differential expression call.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public class DiffExprParams {
    
    /**
     * Create a default <code>DiffExprParams</code> object with a differential expression type 
     * set to <code>DiffExprType.OVER</code> and no minimum number of conditions requested 
     * (but the differential expression analysis pipeline has a minimum threshold on 
     * the number of conditions anyway, it means that this default threshold will be used).
     */
    public DiffExprParams()
    {
    	this.setOverallConditionCount(0);
    	this.setAnatomyConditionCount(0);
    	this.setDevelopmentConditionCount(0);
    }
    /**
     * An <code>int</code> defining the requested minimum number of anatomical structures 
     * compared when generating the differential expression data.
     */
    private int anatomyConditionCount;
    /**
     * An <code>int</code> defining the requested minimum number of developmental stages  
     * compared when generating the differential expression data.
     */
    private int developmentConditionCount;
    /**
     * An <code>int</code> defining the requested minimum number of conditions 
     * compared when generating the differential expression data. For instance, 
     * one organ compared at three developmental stages would represent three conditions.
     */
    private int conditionCount;
    
	/**
	 * @return 	An <code>int</code> being the requested minimum number of anatomical structures 
     * 			compared when generating the differential expression data.
	 */
	public int getAnatomyConditionCount() {
		return this.anatomyConditionCount;
	}
	/**
	 * @param conditionCount 	A <code>int</code> defining the requested minimum number 
	 * 							of anatomical structures compared when generating 
	 * 							the differential expression data. 
	 */
	public void setAnatomyConditionCount(int conditionCount) {
		this.anatomyConditionCount = conditionCount;
	}
    
	/**
	 * @return 	An <code>int</code> being the requested minimum number of developmental stages 
     * 			compared when generating the differential expression data.
	 */
	public int getDevelopmentConditionCount() {
		return this.developmentConditionCount;
	}
	/**
	 * @param conditionCount 	A <code>int</code> defining the requested minimum number 
	 * 							of developmental stages compared when generating 
	 * 							the differential expression data. 
	 */
	public void setDevelopmentConditionCount(int conditionCount) {
		this.developmentConditionCount = conditionCount;
	}
    
	/**
	 * @return 	An <code>int</code> defining the requested minimum number of conditions 
     * 			compared when generating the differential expression data. For instance, 
     * 			one organ compared at three developmental stages would represent 
     * 			three conditions.
	 */
	public int getOverallConditionCount() {
		return this.conditionCount;
	}
	/**
	 * @param conditionCount 	An <code>int</code> defining the requested minimum number 
	 * 							of conditions compared when generating the differential 
	 * 							expression data. For instance, one organ compared 
	 * 							at three developmental stages would represent three conditions.
	 */
	public void setOverallConditionCount(int conditionCount) {
		this.conditionCount = conditionCount;
	}
}
