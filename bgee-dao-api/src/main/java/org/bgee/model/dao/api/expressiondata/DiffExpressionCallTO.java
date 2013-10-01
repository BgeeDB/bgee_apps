package org.bgee.model.dao.api.expressiondata;

/**
 * A {@code CallTO} specific to differential expression calls (comparison of 
 * the expression of a gene in different conditions, as part of a differential 
 * expression analysis). Their specificity is that they are associated to 
 * the minimum number of conditions that were compared among all the differential 
 * expression analyzes that allowed to produce that call; and that they are 
 * associated to a {@link Factor}, defining what was the comparison factor used 
 * during the analyzes generating this call.
 * <p>
 * Of note, there is no data propagation from anatomical entities nor developmental stages 
 * for differential expression calls.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public class DiffExpressionCallTO extends CallTO {
    /**
     * Define the different types of differential expression analyses, 
     * based on the experimental factor studied: 
     * <ul>
     * <li>ANATOMY: analyzes comparing different anatomical structures at a same 
     * (broad) developmental stage. The experimental factor is the anatomy, 
     * these analyzes try to identify in which anatomical structures genes are 
     * differentially expressed. 
     * <li>DEVELOPMENT: analyzes comparing for a same anatomical structure 
     * different developmental stages. The experimental factor is the developmental time, 
     * these analyzes try to identify for a given anatomical structures at which 
     * developmental stages genes are differentially expressed. 
     * </ul>
     */
    public enum Factor {
        ANATOMY, DEVELOPMENT;
    }
    
    /**
     * An {@code int} defining the minimum number of conditions that were compared, 
     * among all the differential expression analyzes that allowed to produce this call.
     */
    private int minConditionCount;
    /**
     * A {@code Factor} defining what was the comparison factor used during 
     * the differential expression analyzes generating this call. 
     */
    private Factor factor;
    
    /**
     * Default constructor.
     */
    public DiffExpressionCallTO() {
        super();
        this.setMinConditionCount(0);
        this.setFactor(null);
    }

    /**
     * @return  the {@code int} defining the minimum number of conditions that 
     *          were compared, among all the differential expression analyzes 
     *          that allowed to produce this call.
     */
    public int getMinConditionCount() {
        return minConditionCount;
    }
    /**
     * @param minConditionCount    the {@code int} defining the minimum number of conditions 
     *                          that were compared, among all the differential expression 
     *                          analyzes that allowed to produce this call.
     */
    void setMinConditionCount(int conditionCount) {
        this.minConditionCount = conditionCount;
    }

    /**
     * @return  the {@code Factor} defining what was the comparison factor used 
     *          during the differential expression analyzes generating this call. 
     */
    public Factor getFactor() {
        return factor;
    }
    /**
     * @param factor    the {@code Factor} defining what was the comparison factor used 
     *                  during the differential expression analyzes generating this call.
     */
    void setFactor(Factor factor) {
        this.factor = factor;
    }

}