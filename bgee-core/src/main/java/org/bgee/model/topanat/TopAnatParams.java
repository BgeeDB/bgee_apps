package org.bgee.model.topanat;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
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
    private final static String FILE_PREFIX = "topAnat_";

    /**
     * 
     */
    private final Set<String> submittedForegroundIds;

    /**
     * 
     */
    private final Set<String> submittedBackgroundIds;
    
    /**
     * 
     */
    private final String speciesId;

    /**
     * 
     */
    private final CallType callType;

    /**
     * 
     */
    private final DataQuality dataQuality;

    /**
     * 
     */
    private final Set<DataType> dataTypes;

    /**
     * 
     */
    private final String devStageId;

    /**
     * 
     */
    private final DecorelationType decorelationType;

    /**
     * 
     */
    private final StatisticTest statisticTest;

    /**
     * 
     */
    private final int nodeSize;

    /**
     * 
     */
    private final float fdrThreshold;

    /**
     * 
     */
    private final float pvalueThreashold;

    /**
     * 
     */
    private final int numberOfSignificantNodes;
    
    /**
     * 
     */
    private final String uniqueFileNameString;

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
        private final String speciesId;

        /**
         * 
         */
        private Set<String> submittedBackgroundIds;

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
        public Builder(Set<String> submittedForegroundIds, String speciesId, CallType callType){
            this(submittedForegroundIds, null, speciesId, callType);
        }

        /**
         * @param submittedForegroundIds
         * @param submittedBackgroundIds
         * @param callType
         */
        public Builder(Set<String> submittedForegroundIds, Set<String> submittedBackgroundIds,
                String speciesId,
                CallType callType) {
            log.entry(submittedForegroundIds,submittedBackgroundIds,speciesId,callType);
            this.submittedForegroundIds = submittedForegroundIds;
            this.submittedBackgroundIds = submittedBackgroundIds;
            this.speciesId = speciesId;
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
        this.speciesId = builder.speciesId;
        this.callType = builder.callType;
        this.dataTypes = builder.dataTypes == null ? null :
            Collections.unmodifiableSet(new HashSet<>(builder.dataTypes));
        this.decorelationType = builder.decorelationType;
        this.statisticTest = builder.statisticTest;
        this.devStageId = builder.devStageId;
        this.fdrThreshold = builder.fdrThreshold;
        this.dataQuality = builder.dataQuality;
        this.nodeSize = builder.nodeSize;
        this.numberOfSignificantNodes = builder.numberOfSignificantNode;
        this.pvalueThreashold = builder.pvalueThreashold;
        this.submittedBackgroundIds = builder.submittedBackgroundIds == null ? null :
            Collections.unmodifiableSet(new HashSet<>(builder.submittedBackgroundIds));
        this.submittedForegroundIds = builder.submittedForegroundIds == null ? null :
            Collections.unmodifiableSet(new HashSet<>(builder.submittedForegroundIds));
        // XXX Probably something else based on different params and digested
        this.uniqueFileNameString = this.speciesId+this.devStageId+this.callType;
        log.exit();
    }

    /**
     * @return the submittedIds
     */
    public Collection<String> getSubmittedForegroundIds() {
        return submittedForegroundIds;
    }

    /**
     * @return the submittedBackgroundIds
     */
    public Collection<String> getSubmittedBackgroundIds() {
        return submittedBackgroundIds;
    }
    
    /**
     * 
     * @return
     */
    public String getSpeciesId(){
       return speciesId; 
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
    public CallFilter<CallData<?>> rawParametersToCallFilter() {
        ConditionFilter conditionFilter = new ConditionFilter(null, Arrays.asList(this.devStageId));
        return new CallFilter<CallData<?>>(
                this.submittedBackgroundIds != null ? new GeneFilter(this.submittedBackgroundIds): null, 
                new HashSet<>(Arrays.asList(conditionFilter)), this.getCallData()
            );
    }
        
    /**
     * 
     */
    public String getResultFileName(){
        return TopAnatParams.FILE_PREFIX + this.uniqueFileNameString + ".tsv";
    }
    
    /**
     * 
     */
    public String getResultPDFFileName(){
        return TopAnatParams.FILE_PREFIX + "PDF_" + this.uniqueFileNameString + ".pdf";
    }
    
    /**
     *
     */
    public String getGeneToAnatEntitiesFileName(){
        return TopAnatParams.FILE_PREFIX 
                + "GeneToAnatEntities_" + this.uniqueFileNameString + ".tsv";
    }
    
    /**
     * @return
     */
    public String getAnatEntitiesNamesFileName(){
        return TopAnatParams.FILE_PREFIX + "AnatEntitiesNames_" + this.speciesId + ".tsv";
    }
    
    /**
     * 
     */
    public String getAnatEntitiesRelationshipsFileName(){
        return TopAnatParams.FILE_PREFIX 
                + "AnatEntitiesRelationships_" + this.speciesId + ".tsv";
    }
    
    /**
     * 
     */
    public String getRScriptOutputFileName(){
        return TopAnatParams.FILE_PREFIX 
                + "RScript" + this.uniqueFileNameString + ".R";
    }
    
    /**
     * XXX check if correct: DiffExpressionFactor.ANATOMY ? DiffExpression.DIFF_EXPRESSED ?
     * => storyboard says over-expressed of diff. expressed? I don't remember.
     * XXX check if correct: DataPropagation
     * @return
     */
    private Set<CallData<?>> getCallData() {
        log.entry();
        
        final DataPropagation dataPropagation = new DataPropagation(
                DataPropagation.PropagationState.SELF,
                DataPropagation.PropagationState.SELF_OR_CHILD);
        final DataQuality dataQual = this.dataQuality == null? DataQuality.LOW: this.dataQuality;
        
        Function<DataType, CallData<?>> callDataSupplier = null;
        if (this.callType == CallType.Expression.EXPRESSED) {
            callDataSupplier = dataType -> new ExpressionCallData(CallType.Expression.EXPRESSED,
                dataQual, dataType, dataPropagation);
        } else if (this.callType == CallType.DiffExpression.OVER_EXPRESSED) {
            callDataSupplier = dataType -> new DiffExpressionCallData(DiffExpressionFactor.ANATOMY,
                    CallType.DiffExpression.OVER_EXPRESSED, dataQual, dataType);
        }
        
        if (this.dataTypes == null || this.dataTypes.isEmpty() || 
                this.dataTypes.containsAll(this.callType.getAllowedDataTypes())) {
            return log.exit(new HashSet<>(Arrays.asList(callDataSupplier.apply(null))));
        }
        return log.exit(this.dataTypes.stream().map(callDataSupplier::apply).collect(Collectors.toSet()));
    }
}

