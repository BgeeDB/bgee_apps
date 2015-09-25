package org.bgee.model;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.model.expressiondata.baseelements.DecorelationType;

public class TopAnatParams {

    /**
     * Default values //XXX Managed in BgeeProperties instead ?
     */
    
    /**
     * 
     */
    private final static boolean INCLUDE_EXPRESSION_DEFAULT = true;

    /**
     * 
     */
    private final static boolean INCLUDE_DIFFERENTIAL_EXPRESSION_DEFAULT = true;

    /**
     * 
     */
    private final static boolean HIGH_CONFIDENCE_ONLY_DEFAULT = false;
    
    /**
     * 
     */
    private final static Set<DataType> DATA_TYPES_DEFAULT = 
            new HashSet<DataType>(Arrays.asList(DataType.values()));
    
    /**
     * TODO need the true list, or make it mandatory
     */
    private final static Collection<String> DEV_STAGE_IDS_DEFAULT = 
            new HashSet<String>(Arrays.asList("1","2","3"));
    
    /**
     * 
     */
    private final static DecorelationType DECORLATION_TYPE_DEFAULT = DecorelationType.PARENT_CHILD;
    
    /**
     * 
     */
    private final static int NODE_SIZE_DEFAULT = 1;
    
    /**
     * 
     */
    private final static float FDR_THRESHOLD_DEFAULT = 0.05f;
    
    /**
     * 
     */
    private final static float PVALUE_THRESHOLD_DEFAULT = 0.05f;
    
    /**
     * 
     */
    private final static int NUMBER_OF_SIGNIFICANT_NODES_DEFAULT = 1;
    
    /**
     * 
     */
    private final static Logger log = LogManager
            .getLogger(TopAnatParams.class.getName());
    
    /**
     * 
     */
    private final Collection<String> submittedIds;

    /**
     * 
     */
    private final boolean backgroundSubmitted;

    /**
     * 
     */
    private final Collection<String> submittedBackgroundIds;
    
    /**
     * 
     */
    private boolean includeExpression;
    
    /**
     * 
     */
    private boolean includeDifferentialExpression;
    
    /**
     * 
     */
    private boolean highConfidenceOnly;
    
    /**
     * 
     */
    private Set<DataType> dataTypes;
        
    /**
     * 
     */
    private Collection<String> devStageIds;
        
    /**
     * 
     */
    private DecorelationType decorelationType;
    
    /**
     * 
     */
    private int nodeSize;
    
    /**
     * 
     */
    private float fdrThreshold;
    
    /**
     * 
     */
    private float pvalueThreashold;
    
    /**
     * 
     */
    private int numberOfSignificantNodes;

    /**
     * 
     */
    public static class Builder {

        /**
         * 
         */
        private final static Logger log = LogManager
                .getLogger(TopAnatParams.Builder.class.getName());
        
        /**
         * 
         */
        private final Collection<String> submittedIds;

        /**
         * 
         */
        private final boolean backgroundSubmitted;

        /**
         * 
         */
        private Collection<String> submittedBackgroundIds;
        
        /**
         * 
         */
        private boolean includeExpression;
        
        /**
         * 
         */
        private boolean includeDifferentialExpression;
        
        /**
         * 
         */
        private boolean highConfidenceOnly;
        
        /**
         * 
         */
        private Set<DataType> dataTypes;
                
        /**
         * 
         */
        private Collection<String> devStageIds;
            
        /**
         * 
         */
        private DecorelationType decorelationType;
        
        /**
         * 
         */
        private int nodeSize;
        
        /**
         * 
         */
        private float fdrThreshold;
        
        /**
         * 
         */
        private float pvalueThreashold;
        
        /**
         * 
         */
        private int numberOfSignificantNode;

        /**
         * 
         * @param submittedIds
         */
        public Builder(Collection<String> submittedIds){
            this(submittedIds, null);
        }
        
        /**
         * 
         * @param submittedIds
         * @param submittedBackgroundIds
         */
        public Builder(Collection<String> submittedIds, Collection<String> submittedBackgroundIds) {
            log.entry(submittedIds,submittedBackgroundIds);
            // Mandatory attributes
            this.submittedIds = submittedIds;
            this.submittedBackgroundIds = submittedBackgroundIds;
            if (this.submittedBackgroundIds != null) {
                this.backgroundSubmitted = true;                
            }
            else{
                this.backgroundSubmitted = false;  
            }
            // Default value for optional attributes
            this.dataTypes = TopAnatParams.DATA_TYPES_DEFAULT;
            this.decorelationType = TopAnatParams.DECORLATION_TYPE_DEFAULT;
            this.devStageIds = TopAnatParams.DEV_STAGE_IDS_DEFAULT;
            this.fdrThreshold = TopAnatParams.FDR_THRESHOLD_DEFAULT;
            this.highConfidenceOnly = TopAnatParams.HIGH_CONFIDENCE_ONLY_DEFAULT;
            this.includeDifferentialExpression = TopAnatParams.INCLUDE_DIFFERENTIAL_EXPRESSION_DEFAULT;
            this.includeExpression = TopAnatParams.INCLUDE_EXPRESSION_DEFAULT;
            this.nodeSize = TopAnatParams.NODE_SIZE_DEFAULT;
            this.numberOfSignificantNode = TopAnatParams.NUMBER_OF_SIGNIFICANT_NODES_DEFAULT;
            this.pvalueThreashold = TopAnatParams.PVALUE_THRESHOLD_DEFAULT;
            log.exit();
        }

        /**
         * 
         * @param includeDifferentialExpression
         * @return
         */
        public Builder includeDifferentialExpression(boolean includeDifferentialExpression){
            log.entry(includeDifferentialExpression);
            this.includeDifferentialExpression = includeDifferentialExpression;
            return log.exit(this);
        }    

        /**
         * 
         * @param includeExpression
         * @return
         */
        public Builder includeExpression(boolean includeExpression){
            log.entry(includeExpression);
            this.includeExpression = includeExpression;
            return log.exit(this);
        }   
        
        /**
         * 
         * @param highConfidenceOnly
         * @return
         */
        public Builder highConfidenceOnly(boolean highConfidenceOnly){
            log.entry(highConfidenceOnly);
            this.highConfidenceOnly = highConfidenceOnly;
            return log.exit(this);
        } 
                
        /**
         * 
         * @param dataTypes
         * @return
         */
        public Builder dataTypes(Set<DataType> dataTypes){
            log.entry(dataTypes);
            this.dataTypes = dataTypes;
            return log.exit(this);
        }  
        
        /**
         * 
         * @param devStageIds
         * @return
         */
        public Builder devStageIds(Collection<String> devStageIds){
            log.entry(devStageIds);
            this.devStageIds = devStageIds;
            return log.exit(this);
        }         
        
        /**
         * 
         * @param decorelationType
         * @return
         */
        public Builder decorelationType(DecorelationType decorelationType){
            log.entry(decorelationType);
            this.decorelationType = decorelationType;
            return log.exit(this);
        }               

        /**
         * 
         * @param nodeSize
         * @return
         */
        public Builder nodeSize(int nodeSize){
            log.entry(nodeSize);
            this.nodeSize = nodeSize;
            return log.exit(this);
        }  
        
        /**
         * 
         * @param fdrThreshold
         * @return
         */
        public Builder fdrThreshold(float fdrThreshold){
            log.entry(fdrThreshold);
            this.fdrThreshold = fdrThreshold;
            return log.exit(this);
        }   

        /**
         * 
         * @param pvalueThreashold
         * @return
         */
        public Builder pvalueThreashold(float pvalueThreashold){
            log.entry(pvalueThreashold);
            this.pvalueThreashold = pvalueThreashold;
            return log.exit(this);
        }   
   
        /**
         * 
         * @param numberOfSignificantNode
         * @return
         */
        public Builder numberOfSignificantNode(int numberOfSignificantNode){
            log.entry(numberOfSignificantNode);
            this.numberOfSignificantNode = numberOfSignificantNode;
            return log.exit(this);
        }       
       
        /**
         * 
         * @return
         */
        public TopAnatParams build(){
            return new TopAnatParams(this);
        }
        
    }
    
    private TopAnatParams(Builder builder) {
        log.entry();
        this.backgroundSubmitted = builder.backgroundSubmitted;
        this.dataTypes = builder.dataTypes;
        this.decorelationType = builder.decorelationType;
        this.devStageIds = builder.devStageIds;
        this.fdrThreshold = builder.fdrThreshold;
        this.highConfidenceOnly = builder.highConfidenceOnly;
        this.includeDifferentialExpression = builder.includeDifferentialExpression;
        this.includeExpression = builder.includeExpression;
        this.nodeSize = builder.nodeSize;
        this.numberOfSignificantNodes = builder.numberOfSignificantNode;
        this.pvalueThreashold = builder.pvalueThreashold;
        this.submittedBackgroundIds = builder.submittedBackgroundIds;
        this.submittedIds = builder.submittedIds;
        log.exit();
    }

    /**
     * @return the submittedIds
     */
    public Collection<String> getSubmittedIds() {
        return submittedIds;
    }

    /**
     * @return the backgroundSubmitted
     */
    public boolean isBackgroundSubmitted() {
        return backgroundSubmitted;
    }

    /**
     * @return the submittedBackgroundIds
     */
    public Collection<String> getSubmittedBackgroundIds() {
        return submittedBackgroundIds;
    }

    /**
     * @return the includeExpression
     */
    public boolean isIncludeExpression() {
        return includeExpression;
    }

    /**
     * @return the includeDifferentialExpression
     */
    public boolean isIncludeDifferentialExpression() {
        return includeDifferentialExpression;
    }

    /**
     * @return the highConfidenceOnly
     */
    public boolean isHighConfidenceOnly() {
        return highConfidenceOnly;
    }

    /**
     * @return the dataTypes
     */
    public Set<DataType> getDataTypes() {
        return dataTypes;
    }

    /**
     * @return the devStageIds
     */
    public Collection<String> getDevStageIds() {
        return devStageIds;
    }

    /**
     * @return the decorelationType
     */
    public DecorelationType getDecorelationType() {
        return decorelationType;
    }

    /**
     * @return the nodeSize
     */
    public int getNodeSize() {
        return nodeSize;
    }

    /**
     * @return the fdrThreshold
     */
    public float getFdrThreshold() {
        return fdrThreshold;
    }

    /**
     * @return the pvalueThreashold
     */
    public float getPvalueThreashold() {
        return pvalueThreashold;
    }

    /**
     * @return the numberOfSignificantNodes
     */
    public int getNumberOfSignificantNodes() {
        return numberOfSignificantNodes;
    }
  
}
