package org.bgee.model.topanat;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.ServiceFactory;
import org.bgee.model.expressiondata.CallData;
import org.bgee.model.expressiondata.CallData.ExpressionCallData;
import org.bgee.model.expressiondata.CallData.DiffExpressionCallData;
import org.bgee.model.expressiondata.CallFilter;
import org.bgee.model.expressiondata.ConditionFilter;
import org.bgee.model.expressiondata.baseelements.CallType;
import org.bgee.model.expressiondata.baseelements.DataPropagation;
import org.bgee.model.expressiondata.baseelements.DataQuality;
import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.model.expressiondata.baseelements.DecorelationType;
import org.bgee.model.expressiondata.baseelements.StatisticTest;
import org.bgee.model.gene.GeneFilter;


import org.bgee.model.expressiondata.baseelements.DiffExpressionFactor;

public class TopAnatParams {

    /**
     * 
     */
    private final static Logger log = LogManager
            .getLogger(TopAnatParams.class.getName());

    /**
     * 
     */
    private final Set<String> submittedForegroundIds;

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
    private CallType callType;

    /**
     * 
     */
    private DataQuality dataQuality;

    /**
     * 
     */
    private Set<DataType> dataTypes;

    /**
     * 
     */
    private String devStageId;

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
        private final Set<String> submittedForegroundIds;

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
        private CallType callType;

        /**
         * 
         */
        private DataQuality dataQuality;

        /**
         * 
         */
        private Set<DataType> dataTypes;

        /**
         * 
         */
        private String devStageId;

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
         * @param submittedForegroundIds
         * @param callType
         */
        public Builder(Set<String> submittedForegroundIds,CallType callType){
            this(submittedForegroundIds, null, callType);
        }

        /**
         * @param submittedForegroundIds
         * @param submittedBackgroundIds
         * @param callType
         */
        public Builder(Set<String> submittedForegroundIds, Set<String> submittedBackgroundIds, 
                CallType callType) {
            log.entry(submittedForegroundIds,submittedBackgroundIds);
            // Mandatory attributes
            this.submittedForegroundIds = submittedForegroundIds;
            this.submittedBackgroundIds = submittedBackgroundIds;
            if (this.submittedBackgroundIds != null) {
                this.backgroundSubmitted = true;                
            }
            else{
                this.backgroundSubmitted = false;  
            }
            this.callType = callType;

            log.exit();
        }

        /**
         * 
         * @param dataQuality
         * @return
         */
        public Builder dataQuality(DataQuality dataQuality){
            log.entry(dataQuality);
            this.dataQuality = dataQuality;
            return log.exit(this);
        } 

        /**
         * XXX checkCallTypeDataTypes here or elsewhere ? Convert to null if all of them ?
         * @param dataTypes
         * @return
         */
        public Builder dataTypes(Set<DataType> dataTypes){
            log.entry(dataTypes);
            this.callType.checkCallTypeDataTypes(dataTypes);
            this.dataTypes = dataTypes;
            return log.exit(this);
        }  

        /**
         * 
         * @param devStageId
         * @return
         */
        public Builder devStageId(String devStageId){
            log.entry(devStageId);
            this.devStageId = devStageId;
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
            if(this.serviceFactory == null){
                this.serviceFactory = new ServiceFactory();
            }
            return new TopAnatParams(this);
        }

    }

    private TopAnatParams(Builder builder) {
        log.entry();
        this.backgroundSubmitted = builder.backgroundSubmitted;
        this.callType = builder.callType;
        this.dataTypes = builder.dataTypes;
        this.decorelationType = builder.decorelationType;
        this.statisticTest = builder.statisticTest;
        this.devStageId = builder.devStageId;
        this.fdrThreshold = builder.fdrThreshold;
        this.dataQuality = builder.dataQuality;
        this.nodeSize = builder.nodeSize;
        this.numberOfSignificantNodes = builder.numberOfSignificantNode;
        this.pvalueThreashold = builder.pvalueThreashold;
        this.submittedBackgroundIds = builder.submittedBackgroundIds;
        this.submittedForegroundIds = builder.submittedForegroundIds;
        this.serviceFactory = builder.serviceFactory;
        log.exit();
    }

    /**
     * @return the submittedIds
     */
    public Collection<String> getSubmittedForegroundIds() {
        return submittedForegroundIds;
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
     * @return the callType
     */
    public CallType getCallType() {
        return callType;
    }

    /**
     * @return
     */
    public DataQuality getDataQuality() {
        return dataQuality;
    }

    /**
     * @return the dataTypes
     */
    public Set<DataType> getDataTypes() {
        return dataTypes;
    }

    /**
     * @return the devStageId
     */
    public String getDevStageId() {
        return devStageId;
    }

    /**
     * @return the decorelationType
     */
    public DecorelationType getDecorelationType() {
        return decorelationType;
    }

    /**
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
     * @return
     */
    public ServiceFactory getServiceFactory(){
        return serviceFactory;
    }

    /**
     * @return
     */
    public CallFilter<CallData<? extends CallType>> rawParametersToCallFilter(){
        ConditionFilter conditionFilter = new ConditionFilter(Arrays.asList(this.devStageId),null);
        GeneFilter geneFilter = null;
        if(this.isBackgroundSubmitted()){
            geneFilter = new GeneFilter(this.submittedBackgroundIds);
        }
        return new CallFilter<CallData<? extends CallType>>(
                geneFilter,
                new HashSet<ConditionFilter>(Arrays.asList(conditionFilter)),
                new HashSet<CallData<? extends CallType>>(this.getCallData())
                );
    }

    /**
     * XXX check if correct: DiffExpressionFactor.ANATOMY ? DiffExpression.DIFF_EXPRESSED ?
     * XXX check if correct: DataPropagation
     * @return
     */
    private List<CallData<? extends CallType>> getCallData(){

        List<CallData<? extends CallType>> callDataList = null;
        DataPropagation dataPropagation = new DataPropagation(
                DataPropagation.PropagationState.SELF,
                DataPropagation.PropagationState.SELF_OR_CHILD);
        if(this.dataQuality == null){
            this.dataQuality = DataQuality.LOW;
        }

        if(this.callType == CallType.Expression.EXPRESSED){
            if (this.dataTypes != null){
                callDataList = this.dataTypes.stream()
                        .map(dataType -> new ExpressionCallData(
                                CallType.Expression.EXPRESSED,
                                this.dataQuality,
                                dataType,
                                dataPropagation))
                        .collect(Collectors.toList()); 
            }
            else{
                callDataList = Arrays.asList(new ExpressionCallData(
                        CallType.Expression.EXPRESSED,
                        this.dataQuality,
                        null,
                        dataPropagation));
            }
        }
        else{
            if (this.dataTypes != null){
                callDataList = this.dataTypes.stream()
                        .map(dataType -> new DiffExpressionCallData(
                                DiffExpressionFactor.ANATOMY,
                                CallType.DiffExpression.DIFF_EXPRESSED,
                                this.dataQuality,
                                dataType))
                        .collect(Collectors.toList());
            }
            else{
                callDataList = Arrays.asList(new DiffExpressionCallData(
                        DiffExpressionFactor.ANATOMY,
                        CallType.DiffExpression.DIFF_EXPRESSED,
                        this.dataQuality,
                        null));
            }
        }                

        return callDataList;
    }
}

