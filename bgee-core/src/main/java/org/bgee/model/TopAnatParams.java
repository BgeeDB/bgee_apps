package org.bgee.model;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.expressiondata.CallData;
import org.bgee.model.expressiondata.CallData.DiffExpressionCallData;
import org.bgee.model.expressiondata.CallData.ExpressionCallData;
import org.bgee.model.expressiondata.baseelements.CallType;
import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.model.expressiondata.baseelements.DecorelationType;
import org.bgee.model.expressiondata.baseelements.StatisticTest;
import org.bgee.model.expressiondata.CallFilter;
import org.bgee.model.expressiondata.ConditionFilter;
import org.bgee.model.gene.GeneFilter;

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
    private final static Set<String> DEV_STAGE_IDS_DEFAULT = 
            new HashSet<String>(Arrays.asList("1","2","3"));

    /**
     * 
     */
    private final static DecorelationType DECORLATION_TYPE_DEFAULT = DecorelationType.PARENT_CHILD;

    /**
     * 
     */
    private final static StatisticTest STATISTIC_TEST_DEFAULT = StatisticTest.FISHER;

    /**
     * 
     */
    private final static int NODE_SIZE_DEFAULT = 5;

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
    private final static int NUMBER_OF_SIGNIFICANT_NODES_DEFAULT = 10;

    /**
     * 
     */
    private final static Logger log = LogManager
            .getLogger(TopAnatParams.class.getName());

    /**
     * 
     */
    private final Set<String> submittedIds;

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
    private Set<String> devStageIds;

    /**
     * 
     */
    private DecorelationType decorelationType;

    /**
     * 
     */
    private StatisticTest statisticTest;

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
    private ServiceFactory serviceFactory;

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
        private final Set<String> submittedIds;

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
        private Set<String> devStageIds;

        /**
         * 
         */
        private DecorelationType decorelationType;

        /**
         * 
         */
        private StatisticTest statisticTest;

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
         */
        private ServiceFactory serviceFactory;

        /**
         * 
         * @param submittedIds
         */
        public Builder(Set<String> submittedIds){
            this(submittedIds, null);
        }

        /**
         * @param submittedIds
         * @param submittedBackgroundIds
         */
        public Builder(Set<String> submittedIds, Set<String> submittedBackgroundIds) {
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
            this.statisticTest = TopAnatParams.STATISTIC_TEST_DEFAULT;
            this.devStageIds = TopAnatParams.DEV_STAGE_IDS_DEFAULT;
            this.fdrThreshold = TopAnatParams.FDR_THRESHOLD_DEFAULT;
            this.highConfidenceOnly = TopAnatParams.HIGH_CONFIDENCE_ONLY_DEFAULT;
            this.includeDifferentialExpression = TopAnatParams.INCLUDE_DIFFERENTIAL_EXPRESSION_DEFAULT;
            this.includeExpression = TopAnatParams.INCLUDE_EXPRESSION_DEFAULT;
            this.nodeSize = TopAnatParams.NODE_SIZE_DEFAULT;
            this.numberOfSignificantNode = TopAnatParams.NUMBER_OF_SIGNIFICANT_NODES_DEFAULT;
            this.pvalueThreashold = TopAnatParams.PVALUE_THRESHOLD_DEFAULT;
            this.serviceFactory = new ServiceFactory();
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
        public Builder devStageIds(Set<String> devStageIds){
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
         * @param statisticTest
         * @return
         */
        public Builder statisticTest(StatisticTest statisticTest){
            log.entry(statisticTest);
            this.statisticTest = statisticTest;
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
         * @param callServiceFactory
         * @return
         */
        public Builder serviceFactory(ServiceFactory serviceFactory){
            log.entry(serviceFactory);
            this.serviceFactory = serviceFactory;
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
        this.statisticTest = builder.statisticTest;
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
        this.serviceFactory = builder.serviceFactory;
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
     * 
     * @return the statisticTest
     */
    public StatisticTest getStatisticTest() {
        return statisticTest;
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

    /**
     * 
     * @return
     */
    public ServiceFactory getServiceFactory(){
        return serviceFactory;
    }

    /**
     * 
     * @return
     */
    public Set<CallFilter> getCallFiltersForForeground(){
        return getCallFiltersFromRawParams(false);
    }

    /**
     * 
     * @return
     */
    public Set<CallFilter> getCallFiltersForBackground(){
        if(this.isBackgroundSubmitted()){
            return getCallFiltersFromRawParams(true);
        }
        return null;
    }

    /**
     * 
     * @return
     */
    private Set<CallFilter> getCallFiltersFromRawParams(boolean isBackground){  
        Set<CallFilter> callFilters = new HashSet<CallFilter>();
        ConditionFilter conditionFilter = new ConditionFilter(this.devStageIds);
        Set<CallData<? extends CallType>> callDataFilters = 
                new HashSet<CallData<? extends CallType>>();
        if(this.includeExpression){
            callDataFilters.add(new ExpressionCallData());
        }
        if(this.includeDifferentialExpression){
            callDataFilters.add(new DiffExpressionCallData());
        }
        CallDataConditionFilter callDataConditionFilter = 
                new CallDataConditionFilter(conditionFilter,callDataFilters); 
        if(isBackground){
            this.submittedBackgroundIds.forEach(id -> callFilters.add(new CallFilter(
                    new GeneFilter(id),callDataConditionFilter)));
        }
        else{
            this.submittedIds.forEach(id -> callFilters.add(new CallFilter(
                    new GeneFilter(id),callDataConditionFilter)));            
        }
        return callFilters;
    }

}

