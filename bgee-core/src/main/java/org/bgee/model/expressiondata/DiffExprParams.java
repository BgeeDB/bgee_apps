package org.bgee.model.expressiondata;

/**
 * Parameters for differential expression data. 
 * Note that this class is always used as part of a {@link ExprDataParams}, 
 * and as such, the quality, data types, etc, of the differential expression data is defined 
 * by the <code>ExprDataParams</code> class. 
 * This class stores parameters specific to differential expression data, that are, 
 * the number of conditions compared when generating the differential expression call.
 * <p>
 * If you use an instance of this class in a hash-based <code>Collection</code> or 
 * <code>Map</code>, do not modify any of its fields afterwards, they are used 
 * in the <code>hashCode</code> method, but are not immutable. 
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public class DiffExprParams {
    
    /**
     * Create a default <code>DiffExprParams</code> object with no minimum number 
     * of conditions requested. (but the differential expression analysis pipeline 
     * has a minimum threshold on the number of conditions anyway, it means that 
     * this default threshold will be used).
     */
    public DiffExprParams()
    {
    	this.setOverallConditionCount(0);
    	this.setAnatomyConditionCount(0);
    	this.setDevelopmentConditionCount(0);
    }
    /**
     * Create a <code>DiffExprParams</code> object with a minimum number of conditions 
     * compared requested, over all the conditions. 
     * <p>
     * It does not explicitly specify the number of <code>AnatomicalEntity</code>s compared, 
     * and number of <code>Stage</code>s compared, so for instance, 
     * if <code>overallCondition</code> is equal to 2, experiments used could have 
     * 2 <code>AnatomicalEntity</code>s compared at 1 <code>Stage</code>, or 
     * 2 <code>Stage</code>s compared using 1 <code>AnatomicalEntity</code>, 
     * or 1 <code>AnatomicalEntity</code> at 1 <code>Stage</code> compared to 
     * another <code>AnatomicalEntity</code> at another <code>Stage</code> 
     * (so 2 <code>AnatomicalEntity</code> conditions and 2 <code>Stage</code> conditions).
     * 
     * 
     * @param overallCondition
     */
    public DiffExprParams(int overallCondition) {
    	need to think more about how these parameters interact
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
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + anatomyConditionCount;
		result = prime * result + conditionCount;
		result = prime * result + developmentConditionCount;
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		DiffExprParams other = (DiffExprParams) obj;
		if (anatomyConditionCount != other.anatomyConditionCount) {
			return false;
		}
		if (conditionCount != other.conditionCount) {
			return false;
		}
		if (developmentConditionCount != other.developmentConditionCount) {
			return false;
		}
		return true;
	}
}
