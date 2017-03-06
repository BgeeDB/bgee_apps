package org.bgee.model.topanat;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.expressiondata.CallFilter;
import org.bgee.model.expressiondata.CallFilter.ExpressionCallFilter;
import org.bgee.model.expressiondata.ConditionFilter;
import org.bgee.model.expressiondata.baseelements.CallType;
import org.bgee.model.expressiondata.baseelements.CallType.DiffExpression;
import org.bgee.model.expressiondata.baseelements.CallType.Expression;
import org.bgee.model.expressiondata.baseelements.DataPropagation;
import org.bgee.model.expressiondata.baseelements.DataPropagation.PropagationState;
import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.model.expressiondata.baseelements.DecorrelationType;
import org.bgee.model.expressiondata.baseelements.StatisticTest;
import org.bgee.model.expressiondata.baseelements.SummaryCallType.ExpressionSummary;
import org.bgee.model.expressiondata.baseelements.SummaryQuality;
import org.bgee.model.gene.GeneFilter;
import org.bgee.model.topanat.exception.MissingParameterException;


public class TopAnatParams {

    /**
     * 
     */
    private final static Logger log = LogManager
            .getLogger(TopAnatParams.class.getName());

    /**
     * 
     */
    private final static SummaryQuality DATA_QUALITY_DEFAULT = SummaryQuality.SILVER;

    /**
     * 
     */
    private final static DecorrelationType DECORLATION_TYPE_DEFAULT = DecorrelationType.WEIGTH;

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
    private final static Double FDR_THRESHOLD_DEFAULT = 0.05d;

    /**
     * 
     */
    private final static Double PVALUE_THRESHOLD_DEFAULT = 0.05d;

    /**
     * 
     */
    private final static int NUMBER_OF_NODES_TO_DISPLAX_DEFAULT = 10;

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
    private final Integer speciesId;

    /**
     * 
     */
    private final CallType callType;

    /**
     * 
     */
    private final SummaryQuality summaryQuality;

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
    private final DecorrelationType decorrelationType;

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
    private final Double fdrThreshold;

    /**
     * 
     */
    private final Double pvalueThreshold;

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
    private final Boolean isWithZip;

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
        private final Collection<String> submittedForegroundIds;

        /**
         * 
         */
        private final Integer speciesId;

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
        private SummaryQuality summaryQuality;

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
        private DecorrelationType decorrelationType;

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
        private Double fdrThreshold;

        /**
         * 
         */
        private Double pvalueThreshold;

        /**
         * 
         */
        private Integer numberOfSignificantNode;
        
        /**
         * 
         */
        private Boolean isWithZip;

        /**
         * @param submittedForegroundIds
         * @param callType
         */
        public Builder(Collection<String> submittedForegroundIds, Integer speciesId, CallType callType){
            this(submittedForegroundIds, null, speciesId, callType);
        }

        /**
         * @param submittedForegroundIds
         * @param submittedBackgroundIds
         * @param speciesId
         * @param callType
         */
        public Builder(Collection<String> submittedForegroundIds, Collection<String> submittedBackgroundIds,
                Integer speciesId, CallType callType) {
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
        public Builder summaryQuality(SummaryQuality summaryQuality){
            log.entry(summaryQuality);
            this.summaryQuality = summaryQuality;
            return log.exit(this);
        } 

        /**
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
         * @param decorrelationType
         * @return
         */
        public Builder decorrelationType(DecorrelationType decorrelationType){
            log.entry(decorrelationType);
            this.decorrelationType = decorrelationType;
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
        public Builder fdrThreshold(double fdrThreshold){
            log.entry(fdrThreshold);
            this.fdrThreshold = fdrThreshold;
            return log.exit(this);
        }   

        /**
         * 
         * @param pvalueThreshold
         * @return
         */
        public Builder pvalueThreshold(double pvalueThreshold){
            log.entry(pvalueThreshold);
            this.pvalueThreshold = pvalueThreshold;
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
         * @param isWithZip
         * @return
         */
        public Builder isWithZip(boolean isWithZip){
            log.entry(isWithZip);
            this.isWithZip = isWithZip;
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
        this.decorrelationType = builder.decorrelationType == null ? TopAnatParams.DECORLATION_TYPE_DEFAULT :
            builder.decorrelationType;
        this.statisticTest = builder.statisticTest == null ? TopAnatParams.STATISTIC_TEST_DEFAULT : 
            builder.statisticTest;
        this.devStageId = builder.devStageId;
        this.fdrThreshold = builder.fdrThreshold == null ? TopAnatParams.FDR_THRESHOLD_DEFAULT :
            builder.fdrThreshold;
        this.summaryQuality = builder.summaryQuality == null ? TopAnatParams.DATA_QUALITY_DEFAULT
                : builder.summaryQuality;
        this.nodeSize = builder.nodeSize == null ? TopAnatParams.NODE_SIZE_DEFAULT
                : builder.nodeSize;
        this.numberOfSignificantNodes = builder.numberOfSignificantNode == null ? 
                TopAnatParams.NUMBER_OF_NODES_TO_DISPLAX_DEFAULT : 
                    builder.numberOfSignificantNode;
        this.pvalueThreshold = builder.pvalueThreshold == null ? TopAnatParams.PVALUE_THRESHOLD_DEFAULT
                : builder.pvalueThreshold;
        this.submittedBackgroundIds = builder.submittedBackgroundIds == null ? null :
            Collections.unmodifiableSet(new HashSet<>(builder.submittedBackgroundIds));
        this.key = this.generateKey();
        this.isWithZip = builder.isWithZip == null ? true : builder.isWithZip;
        log.exit();
    }

    /**
     * @return the submittedIds
     */
    public Set<String> getSubmittedForegroundIds() {
        return submittedForegroundIds;
    }

    /**
     * @return the submittedBackgroundIds
     */
    public Set<String> getSubmittedBackgroundIds() {
        return submittedBackgroundIds;
    }

    /**
     * 
     * @return
     */
    public Integer getSpeciesId(){
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
    public SummaryQuality getSummaryQuality() {
        return summaryQuality;
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
     * @return the decorrelationType
     */
    public DecorrelationType getDecorrelationType() {
        return decorrelationType;
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
    public Double getFdrThreshold() {
        return fdrThreshold;
    }

    /**
     * @return the pvalueThreashold
     */
    public Double getPvalueThreashold() {
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
     * 
     * @return
     */
    public Boolean isWithZip(){
        return this.isWithZip;
    }
    
    /**
     * @return
     */
    public CallFilter<?> convertRawParametersToCallFilter() {
        log.entry();
        
        GeneFilter geneFilter = this.submittedBackgroundIds != null?
            new GeneFilter(this.submittedBackgroundIds): null;
        
        Collection<ConditionFilter> condFilters = StringUtils.isBlank(this.devStageId)? null: 
            Arrays.asList(new ConditionFilter(null, Arrays.asList(this.devStageId)));
        
        SummaryQuality summaryQualityFilter = this.summaryQuality == null?
            SummaryQuality.SILVER: this.summaryQuality;
        
        if (this.callType == Expression.EXPRESSED) {
            return log.exit(new ExpressionCallFilter(
                //gene filter 
                geneFilter, 
                //condition filter
                condFilters,
                this.dataTypes,
                summaryQualityFilter,
                ExpressionSummary.EXPRESSED,
                new DataPropagation(PropagationState.SELF, PropagationState.SELF_OR_DESCENDANT)
                ));
        }
        if (this.callType == DiffExpression.OVER_EXPRESSED) {
            //TODO: to implement, and use method getDiffExpressionCallData
            throw log.throwing(new UnsupportedOperationException(
                    "CallService for diff. expression not yet implemented"));
//            return log.exit(new DiffExpressionCallFilter(
//                //gene filter 
//                geneFilter, 
//                //condition filter
//                condFilters,
//                this.dataTypes,
//                summaryQualityFilter,
//                DiffExpressionSummary.OVER_EXPRESSED)
//            );
        }
        throw log.throwing(new IllegalStateException("Unsupported CallType: " + this.callType));
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
        if(this.summaryQuality != null)
            valueToHash.append(this.summaryQuality.toString());
        if(this.dataTypes != null)
            valueToHash.append(EnumSet.copyOf(this.dataTypes).toString());
        if(this.devStageId != null)
            valueToHash.append(this.devStageId.toString());
        if(this.decorrelationType != null)
            valueToHash.append(this.decorrelationType.toString());
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

        assert(StringUtils.isNotBlank(valueToHash));
        
        String keyToReturn = DigestUtils.sha1Hex(valueToHash.toString());
        
        log.info("Key generated: {}", keyToReturn);

        return log.exit(keyToReturn);
    }

    @Override
    /**
     * 
     */
    public String toString(){
        return this.toString(": ", System.lineSeparator(), true);
    }

    /**
     * 
     */
    public String toString(String valSep, String paramSep, boolean displayDetails){
        StringBuffer ret = new StringBuffer();
        if (displayDetails) {
            ret.append("submittedForegroundIds");
            ret.append(valSep);
            ret.append(this.submittedForegroundIds.toString());
            ret.append(paramSep);
            ret.append("submittedBackgroundIds");
            ret.append(valSep);
            if(this.submittedBackgroundIds != null)
                ret.append(this.submittedBackgroundIds.toString());
            ret.append(paramSep);
        }
        
        if(this.speciesId != null) {
            ret.append("speciesId");
            ret.append(valSep);
            ret.append(this.speciesId);
            ret.append(paramSep);
        }
        
        if(this.devStageId != null) {
            ret.append("devStageId");
            ret.append(valSep);
            ret.append(this.devStageId.toString());
            ret.append(paramSep);
        }
        
        if (displayDetails) {
            ret.append("callType");
            ret.append(valSep);
        }
        if(this.callType != null)
            ret.append(this.callType.toString());
        ret.append(paramSep);
        
        if (displayDetails) {
            ret.append("dataQuality");
            ret.append(valSep);
        }
        if(this.summaryQuality != null)
            ret.append(this.summaryQuality.toString());
        ret.append(paramSep);
        
        if (displayDetails) {
            ret.append("dataTypes");
            ret.append(valSep);
        }
        if(this.dataTypes != null)
            ret.append(this.dataTypes.stream().sorted()
                    .map(Object::toString).collect(Collectors.joining(valSep)));
        ret.append(paramSep);
        
        if (displayDetails) {
            ret.append("decorrelationType");
            ret.append(valSep);
        }
        if(this.decorrelationType != null) {
            ret.append(this.decorrelationType.toString());
        }
        
        if (displayDetails) {
            ret.append(paramSep);
            
            ret.append("statisticTest");
            ret.append(valSep);
            if(this.statisticTest != null)
                ret.append(this.statisticTest.toString());
            ret.append(paramSep);
            ret.append("nodeSize");
            ret.append(valSep);
            if(this.nodeSize != null)
                ret.append(this.nodeSize.toString());
            ret.append(paramSep);
            ret.append("fdrThreshold");
            ret.append(valSep);
            if(this.fdrThreshold != null)
                ret.append(this.fdrThreshold.toString());
            ret.append(paramSep);
            ret.append("pvalueThreshold");
            ret.append(valSep);
            if(this.pvalueThreshold != null)
                ret.append(this.pvalueThreshold.toString());
            ret.append(paramSep);
            ret.append("numberOfSignificantNodes");
            ret.append(valSep);
            if(this.numberOfSignificantNodes != null)
                ret.append(this.numberOfSignificantNodes.toString());
        }
        
        return ret.toString();
    }

}

