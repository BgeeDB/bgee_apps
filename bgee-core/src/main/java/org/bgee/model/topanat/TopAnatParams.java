package org.bgee.model.topanat;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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
import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.model.expressiondata.baseelements.DecorrelationType;
import org.bgee.model.expressiondata.baseelements.StatisticTest;
import org.bgee.model.expressiondata.baseelements.SummaryCallType;
import org.bgee.model.expressiondata.baseelements.SummaryCallType.ExpressionSummary;
import org.bgee.model.expressiondata.baseelements.SummaryCallType.DiffExpressionSummary;
import org.bgee.model.expressiondata.baseelements.SummaryQuality;
import org.bgee.model.gene.GeneFilter;
import org.bgee.model.topanat.exception.MissingParameterException;

/**
 * This class provides the parameters needed to define a {@code TopAnatAnalysis}.
 * It has to be instanced through its Builder class.
 * 
 * @author Mathieu Seppey
 * @author Frederic Bastian
 * 
 * @version Bgee 13, March 2016
 * @since Bgee 13
 */
public class TopAnatParams {

    private final static Logger log = LogManager
            .getLogger(TopAnatParams.class.getName());

    /**
     * A {@code DataQuality} that is the default quality value when running a {@code TopAnatAnalysis}
     */
    private final static SummaryQuality DATA_QUALITY_DEFAULT = SummaryQuality.SILVER;

    /**
     * A {@code DecorrelationType} that is the default decorrelation parameter 
     * when running a {@code TopAnatAnalysis}
     */
    private final static DecorrelationType DECORLATION_TYPE_DEFAULT = DecorrelationType.WEIGTH;

    /**
     * A {@code StatisticTest} that is the default static test 
     * when running a {@code TopAnatAnalysis}
     */
    private final static StatisticTest STATISTIC_TEST_DEFAULT = StatisticTest.FISHER;

    /**
     * A {@code int} that is the default node size
     * when running a {@code TopAnatAnalysis}
     */
    private final static int NODE_SIZE_DEFAULT = 5;

    /**
     * A {@code Double} that is the default False Discovery Rate
     * when running a {@code TopAnatAnalysis}
     */
    private final static Double FDR_THRESHOLD_DEFAULT = 0.05d;

    /**
     * A {@code Double} that is the default p-value threshold
     * when running a {@code TopAnatAnalysis}
     */
    private final static Double PVALUE_THRESHOLD_DEFAULT = 0.05d;

    /**
     * An {@code int} that is the default number of nodes to display in the
     * generated graph of results when running a {@code TopAnatAnalysis}
     */
    private final static int NUMBER_OF_NODES_TO_DISPLAX_DEFAULT = 10;

    /**
     * A {@code Set} of {@code String} that contains all foreground gene ids to be tested
     */
    private final Set<String> submittedForegroundIds;

    /**
     * A {@code Set} of {@code String} which specifies the genes ids that constitute the background
     */
    private final Set<String> submittedBackgroundIds;

    /**
     * A {@code String} that contains the id of the species tested in the analysis
     */
    private final Integer speciesId;

    /**
     * A {@code SummaryCallType} that specifies the type of expression call in the analysis
     */
    private final SummaryCallType callType;

    /**
     * A {@code DataQuality} that specifies the minimal quality taken into account in the analysis
     */
    private final SummaryQuality summaryQuality;

    /**
     * A {@code Set} of {@code DataType} that contains all data type to be included in the analysis
     */
    private final Set<DataType> dataTypes;

    /**
     * A {@code String} that contains the developmental stage id to be considered during the analysis
     */
    private final String devStageId;

    /**
     * A {@code DecorrelationType} that contains the type of decorrelation to be used for the analysis
     */
    private final DecorrelationType decorrelationType;

    /**
     * A {@code StatisticTest} that contains the statistic test to be used for the analysis
     */
    private final StatisticTest statisticTest;

    /**
     * An {@code Integer} that contains the minimal node size below which an anatomical ontology
     * will not be considered in the analysis
     */
    private final Integer nodeSize;

    /**
     * A {@code Double} that contains the False Discovery Rate to be used in the analysis
     */
    private final Double fdrThreshold;

    /**
     * A {@code Double} that contains the p-value threshold to be used in the analysis
     */
    private final Double pvalueThreshold;

    /**
     * An {@code Integer} that contains the number of significant nodes to be displayed in the
     * generated graph of results
     */
    private final Integer numberOfSignificantNodes;

    /**
     * A {@code String} that contains a unique hash for identifying the analysis, based on its 
     * parameters
     */
    private final String key;
    
    /**
     * A {@code Boolean} that tells whether all results should be included in a zip file
     */
    private final Boolean isWithZip;

    /**
     * Builder for {@code TopAnatParams}.
     * It is the sole mean for obtaining a TopAnatParams instance through
     * the method {@code build}
     */
    public static class Builder {

        private final static Logger log = LogManager
                .getLogger(TopAnatParams.Builder.class.getName());

        /**
         * A {@code Collection} of {@code String} that contains all foreground gene ids to be tested
         */
        private final Collection<String> submittedForegroundIds;

        /**
         * A {@code String} that contains the id of the species tested in the analysis         
         */
        private final Integer speciesId;

        /**
         * A {@code Collection} of {@code String} which specifies the genes ids that constitute 
         * the background
         */
        private Collection<String> submittedBackgroundIds;

        /**
         * A {@code SummaryCallType} that specifies the type of expression call in the analysis
         */
        private SummaryCallType callType;

        /**
         * A {@code DataQuality} that specifies the minimal quality taken into account in 
         * the analysis
         */
        private SummaryQuality summaryQuality;

        /**
         * A {@code Set} of {@code DataType} that contains all data type to be included
         * in the analysis
         */
        private Set<DataType> dataTypes;

        /**
         * A {@code String} that contains the developmental stage id to be considered
         * during the analysis
         */
        private String devStageId;

        /**
         * A {@code DecorrelationType} that contains the type of decorrelation to be used
         * for the analysis
         */
        private DecorrelationType decorrelationType;

        /**
         * A {@code StatisticTest} that contains the statistic test to be used for the analysis
         */
        private StatisticTest statisticTest;

        /**
         * An {@code Integer} that contains the minimal node size below which an anatomical ontology
         * will not be considered in the analysis
         */
        private Integer nodeSize;

        /**
         * A {@code Double} that contains the False Discovery Rate to be used in the analysis
         */
        private Double fdrThreshold;

        /**
         * A {@code Double} that contains the p-value threshold to be used in the analysis
         */
        private Double pvalueThreshold;

        /**
         * An {@code Integer} that contains the number of significant nodes to be displayed in the
         * generated graph of results         
         */
        private Integer numberOfSignificantNode;
        
        /**
         * A {@code Boolean} that tells whether all results should be included in a zip file
         */
        private Boolean isWithZip;

        /**
         * Constructor of the Builder class with minimal attributes
         * 
         * @param submittedForegroundIds    A {@code Collection} of {@code String} that contains all 
         *                                  foreground gene ids to be tested
         *                                  
         * @param speciesId                 A {@code String} that contains the id of the species
         *                                  tested in the analysis            
         *                                                          
         * @param callType                  A {@code SummaryCallType} that specifies the type of
         *                                  expression call in the analysis
         */
        public Builder(Collection<String> submittedForegroundIds, Integer speciesId,
                SummaryCallType callType){
            this(submittedForegroundIds, null, speciesId, callType);
        }

        /**
         * Constructor of the Builder class with extended attributes
         *
         * @param submittedForegroundIds    A {@code Collection} of {@code String} that contains all 
         *                                  foreground gene ids to be tested
         *                                  
         * @param submittedBackgroundIds    A {@code Collection} of {@code String} which specifies 
         *                                  the genes ids that constitute the background
         *                                  
         * @param speciesId                 A {@code String} that contains the id of the species
         *                                  tested in the analysis         
         *
         * @param callType                  A {@code SummaryCallType} that specifies the type of
         *                                  expression call in the analysis
         */
        public Builder(Collection<String> submittedForegroundIds, Collection<String> submittedBackgroundIds,
                Integer speciesId, SummaryCallType callType) {
            log.entry(submittedForegroundIds,submittedBackgroundIds,speciesId,callType);
            this.submittedForegroundIds = submittedForegroundIds;
            this.submittedBackgroundIds = submittedBackgroundIds;
            this.speciesId = speciesId;
            this.callType = callType;
            log.traceExit();
        }

        /**
         * Update the attribute dataQuality
         * 
         * @param dataQuality   A {@code DataQuality} that specifies the minimal quality 
         *                      taken into account in the analysis
         * @return the updated current Builder instance
         */
        public Builder summaryQuality(SummaryQuality summaryQuality){
            log.entry(summaryQuality);
            this.summaryQuality = summaryQuality;
            return log.traceExit(this);
        } 

        /**
         * Update the attribute dataTypes
         * 
         * @param dataTypes     A {@code Set} of {@code DataType} that contains all data type 
         *                      to be included in the analysis
         * @return the updated current Builder instance
         */
        public Builder dataTypes(Set<DataType> dataTypes){
            log.entry(dataTypes);
            this.callType.checkCallTypeDataTypes(dataTypes);
            this.dataTypes = dataTypes;
            return log.traceExit(this);
        }  

        /**
         * Update the attribute devStageId
         * 
         * @param devStageId    A {@code String} that contains the developmental stage id 
         *                      to be considered during the analysis
         * @return the updated current Builder instance
         */
        public Builder devStageId(String devStageId){
            log.entry(devStageId);
            this.devStageId = devStageId;
            return log.traceExit(this);
        }         

        /**
         * Update the attribute decorrelationType
         * 
         * @param decorrelationType     A {@code DecorrelationType} that contains the type of 
         *                              decorrelation to be used for the analysis
         * @return the updated current Builder instance
         */
        public Builder decorrelationType(DecorrelationType decorrelationType){
            log.entry(decorrelationType);
            this.decorrelationType = decorrelationType;
            return log.traceExit(this);
        } 

        /**
         * Update the attribute statisticTest
         * 
         * @param statisticTest     A {@code StatisticTest} that contains the statistic test
         *                          to be used for the analysis
         * @return the updated current Builder instance
         */
        public Builder statisticTest(StatisticTest statisticTest){
            log.entry(statisticTest);
            this.statisticTest = statisticTest;
            return log.traceExit(this);
        } 

        /**
         * Update the attribute nodeSize
         * 
         * @param nodeSize      An {@code Integer} that contains the minimal node size 
         *                      below which an anatomical ontology will not be considered in the analysis
         * @return the updated current Builder instance
         */
        public Builder nodeSize(int nodeSize){
            log.entry(nodeSize);
            this.nodeSize = nodeSize;
            return log.traceExit(this);
        }  

        /**
         * Update the attribute fdrThreshold
         * 
         * @param fdrThreshold      A {@code Double} that contains the False Discovery Rate
         *                          to be used in the analysis
         * @return the updated current Builder instance
         */
        public Builder fdrThreshold(double fdrThreshold){
            log.entry(fdrThreshold);
            this.fdrThreshold = fdrThreshold;
            return log.traceExit(this);
        }   

        /**
         * Update the attribute pvalueThreshold
         * 
         * @param pvalueThreshold       A {@code Double} that contains the p-value threshold
         *                              to be used in the analysis
         * @return the updated current Builder instance
         */
        public Builder pvalueThreshold(double pvalueThreshold){
            log.entry(pvalueThreshold);
            this.pvalueThreshold = pvalueThreshold;
            return log.traceExit(this);
        }   

        /**
         * Update the attribute numberOfSignificantNode
         * 
         * @param numberOfSignificantNode       An {@code Integer} that contains the number of 
         *                                      significant nodes to be displayed in the
         *                                      generated graph of results  
         * @return the updated current Builder instance
         */
        public Builder numberOfSignificantNode(int numberOfSignificantNode){
            log.entry(numberOfSignificantNode);
            this.numberOfSignificantNode = numberOfSignificantNode;
            return log.traceExit(this);
        }  

        /**
         * Update the attribute isWithZip
         * 
         * @param isWithZip         A {@code Boolean} that tells whether all results should be
         *                          included in a zip file
         * @return the updated current Builder instance
         */
        public Builder isWithZip(boolean isWithZip){
            log.entry(isWithZip);
            this.isWithZip = isWithZip;
            return log.traceExit(this);
        }  

        /**
         * Create the TopAnatParams instance
         * 
         * @return A {@code TopAnatParams} having all provided attributes
         * @throws MissingParameterException    If a mandatory parameter is not properly set
         */
        public TopAnatParams build() throws MissingParameterException{
            log.traceEntry();
            return log.traceExit(new TopAnatParams(this));
        }
    }

    /**
     * Private constructor
     * 
     * @param builder   The Builder instance that provides all attributes
     * @throws MissingParameterException    If a mandatory parameter is not properly set
     */
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
        log.traceExit();
    }

    /**
     * @return A {@code Set} of {@code String} that contains all foreground gene ids to be tested
     */
    public Set<String> getSubmittedForegroundIds() {
        return submittedForegroundIds;
    }

    /**
     * @return  A {@code Set} of {@code String} which specifies the genes ids that constitute 
     *          the background
     */
    public Set<String> getSubmittedBackgroundIds() {
        return submittedBackgroundIds;
    }

    /**
     * @return A {@code String} that contains the id of the species tested in the analysis  
     */
    public Integer getSpeciesId(){
        return speciesId; 
    }

    /**
     * @return A {@code SummaryCallType} that specifies the type of expression call in the analysis
     */
    public SummaryCallType getCallType() {
        return callType;
    }

    /**
     * @return  A {@code DataQuality} that specifies the minimal quality taken into account in 
     *          the analysis
     */
    public SummaryQuality getSummaryQuality() {
        return summaryQuality;
    }

    /**
     * @return  A {@code Set} of {@code DataType} that contains all data type to be included
     *          in the analysis
     */
    public Set<DataType> getDataTypes() {
        return dataTypes;
    }

    /**
     * @return  A {@code String} that contains the developmental stage id to be considered
     *          during the analysis
     */
    public String getDevStageId() {
        return devStageId;
    }

    /**
     * @return  A {@code DecorrelationType} that contains the type of decorrelation to be used
     *          for the analysis
     */
    public DecorrelationType getDecorrelationType() {
        return decorrelationType;
    }

    /**
     * @return A {@code StatisticTest} that contains the statistic test to be used for the analysis
     */
    public StatisticTest getStatisticTest() {
        return statisticTest;
    }

    /**
     * @return  An {@code Integer} that contains the minimal node size below which an anatomical
     *          ontology will not be considered in the analysis
     */
    public Integer getNodeSize() {
        return nodeSize;
    }

    /**
     * @return  A {@code Double} that contains the False Discovery Rate to be used in the analysis
     */
    public Double getFdrThreshold() {
        return fdrThreshold;
    }

    /**
     * @return  A {@code Double} that contains the p-value threshold to be used in the analysis
     */
    public Double getPvalueThreshold() {
        return pvalueThreshold;
    }

    /**
     * @return  An {@code Integer} that contains the number of significant nodes to be displayed
     *          in the generated graph of results  
     */
    public Integer getNumberOfSignificantNodes() {
        return numberOfSignificantNodes;
    }

    /**
     * @return  A {@code String} that contains a unique hash for identifying the analysis,
     *          based on its parameters
     */
    public String getKey() {
        return key;
    }

    /**
     * @return  A {@code Boolean} that tells whether all results should be included in a zip file
     */
    public Boolean isWithZip(){
        return this.isWithZip;
    }
    
    /**
     * Create an return a {@code CallFilter} based on the parameter values 
     * 
     * @return A {@code CallFilter} to be used for the analysis
     */
    public CallFilter<?, ?> convertRawParametersToCallFilter() {
        log.traceEntry();
        
        GeneFilter geneFilter = new GeneFilter(this.speciesId, this.submittedBackgroundIds);
        
        //XXX: filter only on dev. stage. Could potentially add filtering on sex/strain
        Collection<ConditionFilter> condFilters = StringUtils.isBlank(this.devStageId)? null: 
            Collections.singleton(new ConditionFilter(null, Collections.singleton(this.devStageId), 
                    null, null, null, null));

        //TODO: verify this logic
        //(former note: we need to decide whether we want calls with data propagated only,
        //because they can have a higher quality thanks to data propagation.)
        Map<CallType.Expression, Boolean> obsDataFilter = null;
        if(StringUtils.isBlank(this.devStageId)) {
            obsDataFilter = new HashMap<>();
            obsDataFilter.put(null, true);
        }
        
        if (this.callType == ExpressionSummary.EXPRESSED) {
            Map<ExpressionSummary, SummaryQuality> callQualFilter = new HashMap<>();
            callQualFilter.put(ExpressionSummary.EXPRESSED, this.summaryQuality);
            return log.traceExit(new ExpressionCallFilter(
                    //call type and quality filter
                    callQualFilter,
                    //gene filter 
                    Collections.singleton(geneFilter), 
                    //condition filter
                    condFilters,
                    //data type filter
                    this.dataTypes,
                    //observed data filter
                    //XXX: this should be adapted if we want TopAnat to work on a graph of conditions
                    //TODO: investigate whether results are the same if we use all data,
                    //including redundant calls with observed data
                    //(if we just give values null, null, null)
                    obsDataFilter, 
                    //retrieve propagated anat. entities when no decorrelation in order to run a fischer test
                    //without running topGo 
                    (this.decorrelationType != DecorrelationType.NONE) ? true : null,
                    null
            ));
        }
        if (this.callType == DiffExpressionSummary.OVER_EXPRESSED) {
            //TODO: to implement, and use method getDiffExpressionCallData
            throw log.throwing(new UnsupportedOperationException(
                    "CallService for diff. expression not yet implemented"));
//            return log.traceExit(new DiffExpressionCallFilter(
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
 
//TODO: following methods to be used?
//    /**
//     * @return a {@code Collection} of {@code ExpressionCallData}
//     * TODO improve comment here
//     */
//    private Collection<ExpressionCallData> getExpressionCallData() {
//        log.traceEntry();
//
//        return log.traceExit(this.getCallData((dataType, dataQual) -> 
//            new ExpressionCallData(SummaryCallType.Expression.EXPRESSED,
//                dataQual, dataType, 
//                new DataPropagation(PropagationState.SELF, 
//                        PropagationState.SELF_OR_DESCENDANT))));
//    }
//
//    /**
//     * XXX check if correct: DiffExpressionFactor.ANATOMY ? DiffExpression.DIFF_EXPRESSED ?
//     * => storyboard says over-expressed of diff. expressed? I don't remember.
//     * XXX check if correct: DataPropagation
//     * @return
//     */
//    private Collection<DiffExpressionCallData> getDiffExpressionCallData() {
//        log.traceEntry();
//
//        return log.traceExit(this.getCallData((dataType, dataQual) -> 
//            new DiffExpressionCallData(DiffExpressionFactor.ANATOMY, 
//                SummaryCallType.DiffExpression.OVER_EXPRESSED, dataQual, dataType)));
//    }
//
//    /**
//     * TODO improve comment here
//     * @param callDataSupplier
//     * @return a {@code Collection} of {@code ExpressionCallData}
//     */
//    private <T extends CallData<?>> Collection<T> getCallData(
//            BiFunction<DataType, DataQuality, T> callDataSupplier) {
//        log.entry(callDataSupplier);
//
//        final DataQuality dataQual = this.dataQuality == null? DataQuality.LOW: this.dataQuality;
//
//        if (this.dataTypes == null || this.dataTypes.isEmpty() || 
//                this.dataTypes.containsAll(this.callType.getAllowedDataTypes())) {
//            return log.traceExit(Collections.singleton(callDataSupplier.apply(null, dataQual)));
//        }
//        return log.traceExit(this.dataTypes.stream()
//                .map(dataType -> callDataSupplier.apply(dataType, dataQual))
//                .collect(Collectors.toSet()));
//    }

    /**
     * Generate the unique key for the analysis based on the parameters
     * 
     * @return  A {@code String} that is the unique key
     */
    private String generateKey() {
        log.traceEntry();

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

        return log.traceExit(keyToReturn);
    }

    @Override
    public String toString(){
        return this.toString(": ", System.lineSeparator(), true);
    }

    /**
     * A formatted toString methods that is suitable for writing the params in a file
     * @param prettyPrinting    A {@code boolean} that enables the pretty format
     * @return  A formatted {@code String} 
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

