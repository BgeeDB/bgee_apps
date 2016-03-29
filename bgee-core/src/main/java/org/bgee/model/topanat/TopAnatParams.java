package org.bgee.model.topanat;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.expressiondata.CallData;
import org.bgee.model.expressiondata.CallData.DiffExpressionCallData;
import org.bgee.model.expressiondata.CallData.ExpressionCallData;
import org.bgee.model.expressiondata.CallFilter;
import org.bgee.model.expressiondata.CallFilter.ExpressionCallFilter;
import org.bgee.model.expressiondata.ConditionFilter;
import org.bgee.model.expressiondata.baseelements.CallType;
import org.bgee.model.expressiondata.baseelements.DataPropagation;
import org.bgee.model.expressiondata.baseelements.DataQuality;
import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.model.expressiondata.baseelements.DecorrelationType;
import org.bgee.model.expressiondata.baseelements.DiffExpressionFactor;
import org.bgee.model.expressiondata.baseelements.StatisticTest;
import org.bgee.model.expressiondata.baseelements.CallType.DiffExpression;
import org.bgee.model.expressiondata.baseelements.CallType.Expression;
import org.bgee.model.expressiondata.baseelements.DataPropagation.PropagationState;
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
    private final static DataQuality DATA_QUALITY_DEFAULT = DataQuality.LOW;

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
    private final String speciesId;

    /**
     * A {@code CallType} that specifies the type of expression call in the analysis
     */
    private final CallType callType;

    /**
     * A {@code DataQuality} that specifies the minimal quality taken into account in the analysis
     */
    private final DataQuality dataQuality;

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
        private final String speciesId;

        /**
         * A {@code Collection} of {@code String} which specifies the genes ids that constitute 
         * the background
         */
        private Collection<String> submittedBackgroundIds;

        /**
         * A {@code CallType} that specifies the type of expression call in the analysis
         */
        private CallType callType;

        /**
         * A {@code DataQuality} that specifies the minimal quality taken into account in 
         * the analysis
         */
        private DataQuality dataQuality;

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
         * @param callType                  A {@code CallType} that specifies the type of
         *                                  expression call in the analysis
         */
        public Builder(Collection<String> submittedForegroundIds, String speciesId, CallType callType){
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
         * @param callType                  A {@code CallType} that specifies the type of
         *                                  expression call in the analysis
         */
        public Builder(Collection<String> submittedForegroundIds, Collection<String> submittedBackgroundIds,
                String speciesId, CallType callType) {
            log.entry(submittedForegroundIds,submittedBackgroundIds,speciesId,callType);
            this.submittedForegroundIds = submittedForegroundIds;
            this.submittedBackgroundIds = submittedBackgroundIds;
            this.speciesId = speciesId;
            this.callType = callType;
            log.exit();
        }

        /**
         * Update the attribute dataQuality
         * 
         * @param dataQuality   A {@code DataQuality} that specifies the minimal quality 
         *                      taken into account in the analysis
         * @return the updated current Builder instance
         */
        public Builder dataQuality(DataQuality dataQuality){
            log.entry(dataQuality);
            this.dataQuality = dataQuality;
            return log.exit(this);
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
            return log.exit(this);
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
            return log.exit(this);
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
            return log.exit(this);
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
            return log.exit(this);
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
            return log.exit(this);
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
            return log.exit(this);
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
            return log.exit(this);
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
            return log.exit(this);
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
            return log.exit(this);
        }  

        /**
         * Create the TopAnatParams instance
         * 
         * @return A {@code TopAnatParams} having all provided attributes
         * @throws MissingParameterException    If a mandatory parameter is not properly set
         */
        public TopAnatParams build() throws MissingParameterException{
            log.entry();
            return log.exit(new TopAnatParams(this));
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
        this.dataQuality = builder.dataQuality == null ? TopAnatParams.DATA_QUALITY_DEFAULT
                : builder.dataQuality;
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
    public String getSpeciesId(){
        return speciesId; 
    }

    /**
     * @return A {@code CallType} that specifies the type of expression call in the analysis
     */
    public CallType getCallType() {
        return callType;
    }

    /**
     * @return  A {@code DataQuality} that specifies the minimal quality taken into account in 
     *          the analysis
     */
    public DataQuality getDataQuality() {
        return dataQuality;
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
    public CallFilter<?> convertRawParametersToCallFilter() {
        log.entry();
        if (this.callType == Expression.EXPRESSED) {
            return log.exit(new ExpressionCallFilter(
                    //gene filter 
                    this.submittedBackgroundIds != null? new GeneFilter(this.submittedBackgroundIds): null, 
                    //condition filter
                    StringUtils.isBlank(this.devStageId)? null: 
                        Arrays.asList(new ConditionFilter(null, Arrays.asList(this.devStageId))), 
                    //data propagation
                    new DataPropagation(PropagationState.SELF, PropagationState.SELF_OR_DESCENDANT), 
                    this.getExpressionCallData()
                ));
        }
        if (this.callType == DiffExpression.OVER_EXPRESSED) {
            //TODO: to implement, and use method getDiffExpressionCallData
            throw log.throwing(new UnsupportedOperationException(
                    "CallService for diff. expression not yet implemented"));
        }
        throw log.throwing(new IllegalStateException("Unsupported CallType: " + this.callType));
    }
    
    /**
     * @return a {@code Collection} of {@code ExpressionCallData}
     * TODO improve comment here
     */
    private Collection<ExpressionCallData> getExpressionCallData() {
        log.entry();

        return log.exit(this.getCallData((dataType, dataQual) -> 
            new ExpressionCallData(CallType.Expression.EXPRESSED,
                dataQual, dataType, 
                new DataPropagation(PropagationState.SELF, 
                        PropagationState.SELF_OR_DESCENDANT))));
    }

    /**
     * XXX check if correct: DiffExpressionFactor.ANATOMY ? DiffExpression.DIFF_EXPRESSED ?
     * => storyboard says over-expressed of diff. expressed? I don't remember.
     * XXX check if correct: DataPropagation
     * @return
     */
    private Collection<DiffExpressionCallData> getDiffExpressionCallData() {
        log.entry();

        return log.exit(this.getCallData((dataType, dataQual) -> 
            new DiffExpressionCallData(DiffExpressionFactor.ANATOMY, 
                CallType.DiffExpression.OVER_EXPRESSED, dataQual, dataType)));
    }

    /**
     * TODO improve comment here
     * @param callDataSupplier
     * @return a {@code Collection} of {@code ExpressionCallData}
     */
    private <T extends CallData<?>> Collection<T> getCallData(
            BiFunction<DataType, DataQuality, T> callDataSupplier) {
        log.entry(callDataSupplier);

        final DataQuality dataQual = this.dataQuality == null? DataQuality.LOW: this.dataQuality;

        if (this.dataTypes == null || this.dataTypes.isEmpty() || 
                this.dataTypes.containsAll(this.callType.getAllowedDataTypes())) {
            return log.exit(Arrays.asList(callDataSupplier.apply(null, dataQual)));
        }
        return log.exit(this.dataTypes.stream()
                .map(dataType -> callDataSupplier.apply(dataType, dataQual))
                .collect(Collectors.toSet()));
    }

    /**
     * Generate the unique key for the analysis based on the parameters
     * 
     * @return  A {@code String} that is the unique key
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
    public String toString(){
        return this.toString(false);
    }

    /**
     * A formatted toString methods that is suitable for writing the params in a file
     * @param prettyPrinting    A {@code boolean} that enables the pretty format
     * @return  A formatted {@code String} 
     */
    public String toString(boolean prettyPrinting){
        String sep =" ";
        if(prettyPrinting == true)
            sep ="\t";
        StringBuffer ret = new StringBuffer();
        ret.append("submittedForegroundIds:");
        ret.append(sep);
        ret.append(this.submittedForegroundIds.toString());
        ret.append("\r\n");
        ret.append("submittedBackgroundIds:");
        ret.append(sep);
        if(this.submittedBackgroundIds != null)
            ret.append(this.submittedBackgroundIds.toString());
        ret.append("\r\n");
        ret.append("speciesId:");
        ret.append(sep);
        if(this.speciesId != null)
            ret.append(this.speciesId);
        ret.append("\r\n");
        ret.append("callType:");
        ret.append(sep);
        if(this.callType != null)
            ret.append(this.callType.toString());
        ret.append("\r\n");
        ret.append("dataQuality:");
        ret.append(sep);
        if(this.dataQuality != null)
            ret.append(this.dataQuality.toString());
        ret.append("\r\n");
        ret.append("dataTypes:");
        ret.append(sep);
        if(this.dataTypes != null)
            ret.append(this.dataTypes.toString());
        ret.append("\r\n");
        ret.append("devStageId:");
        ret.append(sep);
        if(this.devStageId != null)
            ret.append(this.devStageId.toString());
        ret.append("\r\n");
        ret.append("decorrelationType:");
        ret.append(sep);
        if(this.decorrelationType != null)
            ret.append(this.decorrelationType.toString());
        ret.append("\r\n");
        ret.append("statisticTest:");
        ret.append(sep);
        if(this.statisticTest != null)
            ret.append(this.statisticTest.toString());
        ret.append("\r\n");
        ret.append("nodeSize:");
        ret.append(sep);
        if(this.nodeSize != null)
            ret.append(this.nodeSize.toString());
        ret.append("\r\n");
        ret.append("fdrThreshold:");
        ret.append(sep);
        if(this.fdrThreshold != null)
            ret.append(this.fdrThreshold.toString());
        ret.append("\r\n");
        ret.append("pvalueThreshold:");
        ret.append(sep);
        if(this.pvalueThreshold != null)
            ret.append(this.pvalueThreshold.toString());
        ret.append("\r\n");
        ret.append("numberOfSignificantNodes:");
        ret.append(sep);
        if(this.numberOfSignificantNodes != null)
            ret.append(this.numberOfSignificantNodes.toString());
        ret.append("\r\n");
        return ret.toString();
    }

}

