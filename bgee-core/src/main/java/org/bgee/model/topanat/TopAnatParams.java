package org.bgee.model.topanat;

import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.exception.MissingParameterException;
import org.bgee.model.expressiondata.CallData;
import org.bgee.model.expressiondata.CallData.DiffExpressionCallData;
import org.bgee.model.expressiondata.CallData.ExpressionCallData;
import org.bgee.model.expressiondata.CallFilter;
import org.bgee.model.expressiondata.ConditionFilter;
import org.bgee.model.expressiondata.baseelements.CallType;
import org.bgee.model.expressiondata.baseelements.DataPropagation;
import org.bgee.model.expressiondata.baseelements.DataQuality;
import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.model.expressiondata.baseelements.DecorelationType;
import org.bgee.model.expressiondata.baseelements.DiffExpressionFactor;
import org.bgee.model.expressiondata.baseelements.StatisticTest;
import org.bgee.model.gene.GeneFilter;


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
    private final Integer nodeSize;

    /**
     * 
     */
    private final Float fdrThreshold;

    /**
     * 
     */
    private final Float pvalueThreshold;

    /**
     * 
     */
    private final Integer numberOfSignificantNodes;

    /**
     * 
     */
    private final String key;

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
        private Integer nodeSize;

        /**
         * 
         */
        private Float fdrThreshold;

        /**
         * 
         */
        private Float pvalueThreshold;

        /**
         * 
         */
        private Integer numberOfSignificantNode;

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
         * @param speciesId
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
        public Builder nodeSize(Integer nodeSize){
            log.entry(nodeSize);
            this.nodeSize = nodeSize;
            return log.exit(this);
        }  

        /**
         * 
         * @param fdrThreshold
         * @return
         */
        public Builder fdrThreshold(Float fdrThreshold){
            log.entry(fdrThreshold);
            this.fdrThreshold = fdrThreshold;
            return log.exit(this);
        }   

        /**
         * 
         * @param pvalueThreshold
         * @return
         */
        public Builder pvalueThreshold(Float pvalueThreshold){
            log.entry(pvalueThreshold);
            this.pvalueThreshold = pvalueThreshold;
            return log.exit(this);
        }   

        /**
         * 
         * @param numberOfSignificantNode
         * @return
         */
        public Builder numberOfSignificantNode(Integer numberOfSignificantNode){
            log.entry(numberOfSignificantNode);
            this.numberOfSignificantNode = numberOfSignificantNode;
            return log.exit(this);
        }  

        /**
         * 
         * @return
         * @throws MissingParameterException 
         */
        public TopAnatParams build() throws MissingParameterException{
            log.entry();
            return log.exit(new TopAnatParams(this));
        }


    }

    private TopAnatParams(Builder builder) throws MissingParameterException {
        log.entry(builder);
        // mandatory params
        if(builder.submittedForegroundIds == null){
            throw new MissingParameterException("foreground Ids");
        }

        if(builder.callType == null){
            throw new MissingParameterException("call type");           
        }

        if(builder.speciesId == null){
            throw new MissingParameterException("species id");           
        }

        this.submittedForegroundIds = Collections.unmodifiableSet(
                new HashSet<>(builder.submittedForegroundIds));       
        this.callType = builder.callType;
        this.speciesId = builder.speciesId;
        // optional params
        this.dataTypes = builder.dataTypes == null ? null :
            Collections.unmodifiableSet(new HashSet<>(builder.dataTypes));
        this.decorelationType = builder.decorelationType == null ? DecorelationType.PARENT_CHILD :
            builder.decorelationType;
        this.statisticTest = builder.statisticTest == null ? StatisticTest.FISHER : 
            builder.statisticTest;
        this.devStageId = builder.devStageId;
        this.fdrThreshold = builder.fdrThreshold == null ? 0 : builder.fdrThreshold;
        this.dataQuality = builder.dataQuality == null ? DataQuality.HIGH : builder.dataQuality;
        this.nodeSize = builder.nodeSize == null ? 0 : builder.nodeSize;
        this.numberOfSignificantNodes = builder.numberOfSignificantNode == null ? 0 : 
            builder.numberOfSignificantNode;
        this.pvalueThreshold = builder.pvalueThreshold == null ? 0 : builder.pvalueThreshold;
        this.submittedBackgroundIds = builder.submittedBackgroundIds == null ? null :
            Collections.unmodifiableSet(new HashSet<>(builder.submittedBackgroundIds));
        this.key = this.generateKey();
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
    public Integer getNodeSize() {
        return nodeSize;
    }

    /**
     * @return the fdrThreshold
     */
    public Float getFdrThreshold() {
        return fdrThreshold;
    }

    /**
     * @return the pvalueThreashold
     */
    public Float getPvalueThreashold() {
        return pvalueThreshold;
    }

    /**
     * @return the numberOfSignificantNodes
     */
    public Integer getNumberOfSignificantNodes() {
        return numberOfSignificantNodes;
    }

    /**
     * @return the key
     */
    public String getKey() {
        return key;
    }

    /**
     * @return
     */
    public CallFilter<CallData<?>> rawParametersToCallFilter() {
        log.entry();
        ConditionFilter conditionFilter = new ConditionFilter(null, Arrays.asList(this.devStageId));
        return log.exit(new CallFilter<CallData<?>>(
                this.submittedBackgroundIds != null ? new GeneFilter(this.submittedBackgroundIds): null, 
                        new HashSet<>(Arrays.asList(conditionFilter)), this.getCallData()
                ));
    }

    /**
     * 
     */
    public String getResultFileName(){
        return TopAnatParams.FILE_PREFIX + this.key + ".tsv";
    }

    /**
     * 
     */
    public String getResultPDFFileName(){
        return TopAnatParams.FILE_PREFIX + "PDF_" + this.key + ".pdf";
    }

    /**
     *
     */
    public String getGeneToAnatEntitiesFileName(){
        return TopAnatParams.FILE_PREFIX 
                + "GeneToAnatEntities_" + this.key + ".tsv";
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
                + "RScript_" + this.key + ".R";
    }

    /**
     * 
     */
    public String getParamsOutputFileName(){
        return TopAnatParams.FILE_PREFIX 
                + "Params_" + this.key + ".txt";
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

    /**
     * @return
     */
    private String generateKey() {
        log.entry();

        log.info("Trying to generate a key based on all params");

        StringBuilder valueToHash = new StringBuilder();

        if(this.submittedForegroundIds != null)
            valueToHash.append(new TreeSet<String>(this.submittedForegroundIds).toString());
        if(this.submittedBackgroundIds != null)
            valueToHash.append(new TreeSet<String>(this.submittedBackgroundIds).toString());
        if(this.speciesId != null)
            valueToHash.append(this.speciesId.toString());
        if(this.callType != null)
            valueToHash.append(this.callType.toString());
        if(this.dataQuality != null)
            valueToHash.append(this.dataQuality.toString());
        if(this.dataTypes != null)
            valueToHash.append(new TreeSet<DataType>(this.dataTypes).toString());
        if(this.devStageId != null)
            valueToHash.append(this.devStageId.toString());
        if(this.decorelationType != null)
            valueToHash.append(this.decorelationType.toString());
        if(this.statisticTest != null)
            valueToHash.append(this.statisticTest.toString());
        if(this.nodeSize != null)
            valueToHash.append(this.nodeSize.toString());
        if(this.fdrThreshold != null)
            valueToHash.append(this.fdrThreshold.toString());
        if(this.pvalueThreshold != null)
            valueToHash.append(this.pvalueThreshold.toString());
        if(this.numberOfSignificantNodes != null)
            valueToHash.append(this.numberOfSignificantNodes.toString());

        String keyToReturn = null;

        if (StringUtils.isNotBlank(valueToHash)) {
            keyToReturn = DigestUtils.sha1Hex(valueToHash.toString());
        }

        log.info("Key generated: {}", keyToReturn);

        return log.exit(keyToReturn);
    }

    @Override
    public String toString(){
        StringWriter ret = new StringWriter();
        ret.append("submittedForegroundIds:");
        ret.append("\t\t");
        ret.append(this.submittedForegroundIds.toString());
        ret.append("\n");
        ret.append("submittedBackgroundIds:");
        ret.append("\t\t");
        if(this.submittedBackgroundIds != null)
            ret.append(this.submittedBackgroundIds.toString());
        ret.append("\n");
        ret.append("speciesId:");
        ret.append("\t\t\t");
        if(this.speciesId != null)
            ret.append(this.speciesId);
        ret.append("\n");
        ret.append("callType:");
        ret.append("\t\t\t");
        if(this.callType != null)
            ret.append(this.callType.toString());
        ret.append("\n");
        ret.append("dataQuality:");
        ret.append("\t\t\t");
        if(this.dataQuality != null)
            ret.append(this.dataQuality.toString());
        ret.append("\n");
        ret.append("dataTypes:");
        ret.append("\t\t\t");
        if(this.dataTypes != null)
            ret.append(this.dataTypes.toString());
        ret.append("\n");
        ret.append("devStageId:");
        ret.append("\t\t\t");
        if(this.devStageId != null)
            ret.append(this.devStageId.toString());
        ret.append("\n");
        ret.append("decorelationType:");
        ret.append("\t\t");
        if(this.decorelationType != null)
            ret.append(this.decorelationType.toString());
        ret.append("\n");
        ret.append("statisticTest:");
        ret.append("\t\t\t");
        if(this.statisticTest != null)
            ret.append(this.statisticTest.toString());
        ret.append("\n");
        ret.append("nodeSize:");
        ret.append("\t\t\t");
        if(this.nodeSize != null)
            ret.append(this.nodeSize.toString());
        ret.append("\n");
        ret.append("fdrThreshold:");
        ret.append("\t\t\t");
        if(this.fdrThreshold != null)
            ret.append(this.fdrThreshold.toString());
        ret.append("\n");
        ret.append("pvalueThreshold:");
        ret.append("\t\t");
        if(this.pvalueThreshold != null)
            ret.append(this.pvalueThreshold.toString());
        ret.append("\n");
        ret.append("numberOfSignificantNodes:");
        ret.append("\t");
        if(this.numberOfSignificantNodes != null)
            ret.append(this.numberOfSignificantNodes.toString());
        ret.append("\n");
        return ret.toString();
    }

}

