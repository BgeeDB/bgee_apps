package org.bgee.model.expressiondata.call;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.expressiondata.DAODataType;
import org.bgee.model.dao.api.expressiondata.call.ConditionDAO;
import org.bgee.model.dao.api.expressiondata.call.ConditionDAO.ConditionRankInfoTO;
import org.bgee.model.dao.api.expressiondata.call.GlobalExpressionCallDAO.GlobalExpressionCallDataTO;
import org.bgee.model.dao.api.expressiondata.call.GlobalExpressionCallDAO.GlobalExpressionCallTO;
import org.bgee.model.expressiondata.baseelements.ConditionParameter;
import org.bgee.model.expressiondata.baseelements.DataPropagation2;
import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.model.expressiondata.baseelements.ExpressionLevelInfo;
import org.bgee.model.expressiondata.baseelements.FDRPValue;
import org.bgee.model.expressiondata.baseelements.FDRPValueCondition2;
import org.bgee.model.expressiondata.baseelements.SummaryQuality;
import org.bgee.model.expressiondata.baseelements.SummaryCallType.ExpressionSummary;
import org.bgee.model.expressiondata.call.Call.ExpressionCall2;
import org.bgee.model.expressiondata.call.CallData.ExpressionCallData2;
import org.bgee.model.expressiondata.call.CallFilter.ExpressionCallFilter2;
import org.bgee.model.gene.Gene;

class CallMapping {
    private final static Logger log = LogManager.getLogger(CallMapping.class.getName());

    private final ExpressionCallProcessedFilter processedFilter;
    private final CallServiceUtils utils;

    public CallMapping(ExpressionCallProcessedFilter processedFilter) {
        this(processedFilter, new CallServiceUtils());
    }
    public CallMapping(ExpressionCallProcessedFilter processedFilter, CallServiceUtils utils) {
        if (processedFilter == null) {
            throw log.throwing(new IllegalArgumentException("processedFilter must be provided"));
        }
        if (utils == null) {
            throw log.throwing(new IllegalArgumentException("CallServiceUtils must be provided"));
        }
        this.utils = utils;
        this.processedFilter = processedFilter;
    }

    ExpressionCall2 mapGlobalCallTOToExpressionCall(GlobalExpressionCallTO globalCallTO, 
            Map<Integer, Gene> geneMap, Map<Integer, Condition2> condMap,
            ExpressionCallFilter2 callFilter, Map<Integer, ConditionRankInfoTO> maxRankPerSpecies,
            Set<CallService.Attribute> attrs) {
        log.traceEntry("{}, {}, {}, {}, {}, {}", globalCallTO, geneMap, condMap, callFilter, 
                maxRankPerSpecies, attrs);

        EnumSet<DataType> dataTypeFilters = callFilter == null? null: callFilter.getDataTypeFilters();
        //***********************************
        // ExpressionCallData
        //***********************************
        Set<ExpressionCallData2> callData = mapGlobalCallTOToExpressionCallData(globalCallTO,
                attrs, dataTypeFilters, callFilter.getCondParamCombination());

        //***********************************
        // Gene and Condition
        //***********************************
        Condition2 cond = condMap.get(globalCallTO.getConditionId());
        if (cond == null && attrs.stream().anyMatch(c -> c.isConditionParameter())) {
            throw log.throwing(new IllegalStateException("Could not find Condition for globalConditionId: "
                + globalCallTO.getConditionId() + " for callTO: " + globalCallTO));
        }
        Gene gene = geneMap.get(globalCallTO.getBgeeGeneId());
        if (gene == null && attrs.contains(CallService.Attribute.GENE)) {
            throw log.throwing(new IllegalStateException("Could not find Gene for bgeeGeneId: "
                    + globalCallTO.getBgeeGeneId() + " for callTO: " + globalCallTO));
        }

        //***********************************
        // Info needed for expression score
        //***********************************
        assert cond == null || gene == null || cond.getSpeciesId() == gene.getSpecies().getId();
        assert (attrs == null || attrs.isEmpty() || attrs.contains(CallService.Attribute.EXPRESSION_SCORE)) &&
        maxRankPerSpecies != null && !maxRankPerSpecies.isEmpty() ||
                attrs != null && !attrs.isEmpty() && !attrs.contains(CallService.Attribute.EXPRESSION_SCORE);
        ConditionRankInfoTO maxRankInfo = null;
        if (maxRankPerSpecies != null) {
            if (maxRankPerSpecies.size() == 1) {
                maxRankInfo = maxRankPerSpecies.values().iterator().next();
            } else {
                int speciesId = cond != null? cond.getSpeciesId(): gene.getSpecies().getId();
                maxRankInfo = maxRankPerSpecies.get(speciesId);
            }
            if (maxRankInfo == null) {
                throw log.throwing(new IllegalStateException(
                        "No max rank could be retrieved for call " + globalCallTO));
            }
        }
        EnumSet<DAODataType> requestedDAODataTypes = this.utils.convertDataTypeToDAODataType(
                dataTypeFilters);

        //Retrieve mean rank for the requested data types if needed
        BigDecimal meanRank = null;
        if (attrs == null || attrs.isEmpty() || attrs.contains(CallService.Attribute.EXPRESSION_SCORE) ||
                attrs.contains(CallService.Attribute.MEAN_RANK) ||
                attrs.contains(CallService.Attribute.EXPRESSION_SCORE) ||
                attrs.contains(CallService.Attribute.ANAT_ENTITY_QUAL_EXPR_LEVEL) ||
                attrs.contains(CallService.Attribute.GENE_QUAL_EXPR_LEVEL)) {
            meanRank = globalCallTO.getMeanRanks()
                    .stream().filter(r -> r.getDataTypes().equals(requestedDAODataTypes))
                    .map(r -> r.getMeanRank())
                    .findAny()
                    .orElseThrow(() -> new IllegalStateException("No matching mean rank found"));
        }
        //Compute expression score
        BigDecimal expressionScore = attrs == null || attrs.isEmpty() ||
                attrs.contains(CallService.Attribute.EXPRESSION_SCORE)?
                        computeExpressionScore(meanRank,
                                this.processedFilter.isUseGlobalRank()?
                                        maxRankInfo.getGlobalMaxRank(): maxRankInfo.getMaxRank()):
                null;

        //***********************************
        // FDR-corrected p-values, ExpressionSummary and SummaryQuality
        //***********************************
        Set<FDRPValue> fdrPValues = attrs == null || attrs.isEmpty() ||
                attrs.contains(CallService.Attribute.P_VALUE_INFO_ALL_DATA_TYPES) ||
                attrs.contains(CallService.Attribute.CALL_TYPE) ||
                attrs.contains(CallService.Attribute.DATA_QUALITY)?
                        globalCallTO.getPValues().stream()
                        .map(p -> new FDRPValue(p.getFdrPValue(),
                                mapDAODataTypeToDataType(p.getDataTypes(), dataTypeFilters)))
                        .collect(Collectors.toSet()):
                        null;
       Set<FDRPValueCondition2> bestDescendantFdrPValues = attrs == null || attrs.isEmpty() ||
               attrs.contains(CallService.Attribute.P_VALUE_INFO_ALL_DATA_TYPES) ||
               attrs.contains(CallService.Attribute.CALL_TYPE) ||
               attrs.contains(CallService.Attribute.DATA_QUALITY)?
                       globalCallTO.getBestDescendantPValues().stream()
                       .map(p -> new FDRPValueCondition2(p.getFdrPValue(),
                               mapDAODataTypeToDataType(p.getDataTypes(), dataTypeFilters),
                               null))
                       .collect(Collectors.toSet()):
                       null;
        ExpressionSummary exprSummary = null;
        SummaryQuality summaryQual = null;
        if (attrs == null || attrs.isEmpty() || attrs.contains(CallService.Attribute.CALL_TYPE) ||
                attrs.contains(CallService.Attribute.DATA_QUALITY)) {
            Entry<ExpressionSummary, SummaryQuality> callQual = inferSummaryCallTypeAndQuality(
                    fdrPValues, bestDescendantFdrPValues, dataTypeFilters);
            if (callQual == null) {
                throw log.throwing(new IllegalStateException(
                        "Invalid data to compute ExpressionSummary and SummaryQuality, fdrPValues: "
                        + fdrPValues + ", bestDescendantFdrPValues: " + bestDescendantFdrPValues
                        + ", requestedDataTypes: " + dataTypeFilters));
            }
            exprSummary = attrs == null || attrs.isEmpty() || attrs.contains(CallService.Attribute.CALL_TYPE)?
                    callQual.getKey(): null;
            summaryQual = attrs == null || attrs.isEmpty() || attrs.contains(CallService.Attribute.DATA_QUALITY)?
                    callQual.getValue(): null;
        }

       //***********************************
       // Build new ExpressionCall
       //***********************************
        return log.traceExit(new ExpressionCall2(
            attrs == null || attrs.isEmpty() || attrs.contains(CallService.Attribute.GENE)?
                    gene: null,
            attrs == null || attrs.isEmpty() || attrs.stream().anyMatch(a -> a.isConditionParameter())?
                    cond: null,
            attrs == null || attrs.isEmpty() || attrs.contains(CallService.Attribute.OBSERVED_DATA)?
                    computeDataPropagation(callData): null,
            attrs == null || attrs.isEmpty() || attrs.contains(CallService.Attribute.P_VALUE_INFO_ALL_DATA_TYPES)?
                    fdrPValues: null,
            attrs == null || attrs.isEmpty() || attrs.contains(CallService.Attribute.P_VALUE_INFO_ALL_DATA_TYPES)?
                    bestDescendantFdrPValues: null,
            exprSummary,
            summaryQual,
            attrs == null || attrs.isEmpty() || attrs.contains(CallService.Attribute.DATA_TYPE_RANK_INFO) ||
            attrs.contains(CallService.Attribute.OBSERVED_DATA) ||
            attrs.contains(CallService.Attribute.P_VALUE_INFO_EACH_DATA_TYPE) ||
            attrs.contains(CallService.Attribute.P_VALUE_INFO_ALL_DATA_TYPES)?
                    callData: null,
            attrs == null || attrs.isEmpty() || attrs.contains(CallService.Attribute.MEAN_RANK) ||
            attrs.contains(CallService.Attribute.EXPRESSION_SCORE) ||
            attrs.contains(CallService.Attribute.ANAT_ENTITY_QUAL_EXPR_LEVEL) ||
            attrs.contains(CallService.Attribute.GENE_QUAL_EXPR_LEVEL)?
                    loadExpressionLevelInfo(exprSummary, meanRank, expressionScore,
                            maxRankInfo == null? null:
                                this.processedFilter.isUseGlobalRank()?
                                        maxRankInfo.getGlobalMaxRank(): maxRankInfo.getMaxRank()): null));
    }

    private static Set<ExpressionCallData2> mapGlobalCallTOToExpressionCallData(
            GlobalExpressionCallTO globalCallTO, Set<CallService.Attribute> attrs,
            Set<DataType> requestedDataTypes, Set<ConditionParameter<?, ?>> condParamComb) {
        log.traceEntry("{}, {}, {}, {}", globalCallTO, attrs, requestedDataTypes, condParamComb);
        
        if (globalCallTO.getCallDataTOs() == null || globalCallTO.getCallDataTOs().isEmpty()) {
            log.debug("No CallData available");
            return log.traceExit((Set<ExpressionCallData2>) null);
        }

        return log.traceExit(globalCallTO.getCallDataTOs().stream().map(cdTO -> {
            DataType dt = mapDAODataTypeToDataType(Collections.singleton(cdTO.getDataType()),
                    requestedDataTypes).iterator().next();

            boolean getRankInfo = attrs == null || attrs.isEmpty() ||
                    attrs.contains(CallService.Attribute.DATA_TYPE_RANK_INFO);
            //This info of FDR-corrected p-values is stored in the ExpressionCall,
            //but when we also have the p-values per data type, we also store them
            //in the related ExpressionCallData objet.
            //And since we could use P_VALUE_INFO_ALL_DATA_TYPES, but request only one data type,
            //we also accept this attribute
            boolean getPValues = attrs == null || attrs.isEmpty() ||
                    attrs.contains(CallService.Attribute.P_VALUE_INFO_EACH_DATA_TYPE);
            boolean getObsCount = attrs == null || attrs.isEmpty() ||
                    attrs.contains(CallService.Attribute.P_VALUE_INFO_EACH_DATA_TYPE) ||
                    attrs.contains(CallService.Attribute.P_VALUE_INFO_ALL_DATA_TYPES) ||
                    attrs.contains(CallService.Attribute.CALL_TYPE) ||
                    attrs.contains(CallService.Attribute.DATA_QUALITY) ||
                    attrs.contains(CallService.Attribute.OBSERVED_DATA);
            assert !getRankInfo || cdTO.getRank() != null && cdTO.getRankNorm() != null &&
                    cdTO.getWeightForMeanRank() != null;
            assert !getObsCount || cdTO.getSelfObservationCount() != null &&
                    cdTO.getDescendantObservationCount() != null;

            return new ExpressionCallData2(dt,
                    getPValues? cdTO.getPValue(): null,
                    getPValues? cdTO.getBestDescendantPValue(): null,
                    getRankInfo? cdTO.getRank(): null,
                    getRankInfo? cdTO.getRankNorm(): null,
                    getRankInfo? cdTO.getWeightForMeanRank(): null,
                    getObsCount? mapDAOCallDataTOToDataPropagation(cdTO, condParamComb): null);
        }).collect(Collectors.toSet()));
    }

    private static DataPropagation2 mapDAOCallDataTOToDataPropagation(
            GlobalExpressionCallDataTO callDataTO, Set<ConditionParameter<?, ?>> condParamComb) {
        log.traceEntry("{}, {}", callDataTO, condParamComb);

        if (callDataTO == null || callDataTO.getSelfObservationCount() == null ||
                callDataTO.getDescendantObservationCount() == null ||
                callDataTO.getSelfObservationCount().values().stream().allMatch(v -> v == 0) &&
                callDataTO.getDescendantObservationCount().values().stream().allMatch(v -> v == 0)) {
            return log.traceExit((DataPropagation2) null);
        }

        return log.traceExit(new DataPropagation2(
                callDataTO.getSelfObservationCount().entrySet().stream()
                //For now, the DAO cond params anatEntity and cellType both map to
                //ConditionParameter.ANAT_ENTITY_CELL_TYPE, creating a key collision,
                //but really we are only interested in the post-composition of them,
                //so we discard any combination with only the individual ones
                .filter(e -> e.getKey().contains(ConditionDAO.Attribute.ANAT_ENTITY_ID) &&
                                e.getKey().contains(ConditionDAO.Attribute.CELL_TYPE_ID) ||
                                !e.getKey().contains(ConditionDAO.Attribute.ANAT_ENTITY_ID) &&
                                !e.getKey().contains(ConditionDAO.Attribute.CELL_TYPE_ID))
                //And really we are only interested in the requested combination
                //XXX: are we?
                .map(e -> Map.entry(mapCondDAOAttrsToCondParams(e.getKey()), e.getValue()))
                .filter(e -> condParamComb.containsAll(e.getKey()) && e.getKey().containsAll(condParamComb))
                .collect(Collectors.toMap(
                        e -> e.getKey(),
                        e -> e.getValue())),
                callDataTO.getDescendantObservationCount().entrySet().stream()
                //For now, the DAO cond params anatEntity and cellType both map to
                //ConditionParameter.ANAT_ENTITY_CELL_TYPE, creating a key collision,
                //but really we are only interested in the post-composition of them,
                //so we discard any combination with only the individual ones
                .filter(e -> e.getKey().contains(ConditionDAO.Attribute.ANAT_ENTITY_ID) &&
                                e.getKey().contains(ConditionDAO.Attribute.CELL_TYPE_ID) ||
                                !e.getKey().contains(ConditionDAO.Attribute.ANAT_ENTITY_ID) &&
                                !e.getKey().contains(ConditionDAO.Attribute.CELL_TYPE_ID))
                //And really we are only interested in the requested combination
                //XXX: are we?
                .map(e -> Map.entry(mapCondDAOAttrsToCondParams(e.getKey()), e.getValue()))
                .filter(e -> condParamComb.containsAll(e.getKey()) && e.getKey().containsAll(condParamComb))
                .collect(Collectors.toMap(
                        e -> e.getKey(),
                        e -> e.getValue()))));
    }
    private static Set<ConditionParameter<?, ?>> mapCondDAOAttrsToCondParams(
            Collection<ConditionDAO.Attribute> daoAttrs) {
        log.traceEntry("{}", daoAttrs);
        return log.traceExit(daoAttrs.stream()
                .filter(a -> a.isConditionParameter())
                .map(a -> mapCondDAOAttrToCondParam(a))
                .collect(Collectors.toSet()));
    }
    private static ConditionParameter<?, ?> mapCondDAOAttrToCondParam(ConditionDAO.Attribute daoAttr) {
        log.traceEntry("{}", daoAttr);
        switch (daoAttr) {
            case ANAT_ENTITY_ID:
                return log.traceExit(ConditionParameter.ANAT_ENTITY_CELL_TYPE);
            case STAGE_ID:
                return log.traceExit(ConditionParameter.DEV_STAGE);
            case CELL_TYPE_ID:
                return log.traceExit(ConditionParameter.ANAT_ENTITY_CELL_TYPE);
            case SEX_ID:
                return log.traceExit(ConditionParameter.SEX);
            case STRAIN_ID:
                return log.traceExit(ConditionParameter.STRAIN);
            default:
                throw log.throwing(new UnsupportedOperationException(
                    "Condition parameter not supported: " + daoAttr));
        }
    }
    private static Set<DataType> mapDAODataTypeToDataType(Set<DAODataType> dts,
            Set<DataType> requestedDataTypes) throws IllegalArgumentException, IllegalStateException {
        log.traceEntry("{}, {}", dts, requestedDataTypes);

        Set<DataType> mappedDataTypes = null;
        if (dts == null || dts.isEmpty()) {
            mappedDataTypes = EnumSet.allOf(DataType.class);
        } else {
            mappedDataTypes = dts.stream()
                    .map(dt -> {
                        switch(dt) {
                        case AFFYMETRIX:
                            return log.traceExit(DataType.AFFYMETRIX);
                        case EST:
                            return log.traceExit(DataType.EST);
                        case IN_SITU:
                            return log.traceExit(DataType.IN_SITU);
                        case RNA_SEQ:
                            return log.traceExit(DataType.RNA_SEQ);
                        case FULL_LENGTH:
                            return log.traceExit(DataType.FULL_LENGTH);
                        default:
                            throw log.throwing(new IllegalStateException("Unsupported DataType: " + dt));
                        }
                    }).collect(Collectors.toSet());
        }

        if (requestedDataTypes != null && !requestedDataTypes.isEmpty() &&
                !requestedDataTypes.containsAll(mappedDataTypes)) {
            mappedDataTypes.removeAll(requestedDataTypes);
            throw log.throwing(new IllegalArgumentException(
                    "Some DataTypes were retrieved from data source but were not requested: "
                    + mappedDataTypes));
        }

        return log.traceExit(mappedDataTypes);
    }

    private BigDecimal computeExpressionScore(BigDecimal rank, BigDecimal maxRank) {
        log.traceEntry("{}, {}", rank, maxRank);
        if (maxRank == null) {
            throw log.throwing(new IllegalArgumentException("Max rank must be provided"));
        }
        if (rank == null) {
            log.debug("Rank is null, cannot compute expression score");
            return log.traceExit((BigDecimal) null);
        }
        if (rank.compareTo(new BigDecimal("0")) <= 0 || maxRank.compareTo(new BigDecimal("0")) <= 0) {
            throw log.throwing(new IllegalArgumentException("Rank and max rank cannot be less than or equal to 0"));
        }
        if (rank.compareTo(maxRank) > 0) {
            throw log.throwing(new IllegalArgumentException("Rank cannot be greater than maxRank. Rank: " + rank
                    + " - maxRank: " + maxRank));
        }

        BigDecimal invertedRank = maxRank.add(new BigDecimal("1")).subtract(rank);
        BigDecimal expressionScore = invertedRank.multiply(new BigDecimal("100")).divide(maxRank, 5, RoundingMode.HALF_UP);
        //We want expression score to be at least greater than EXPRESSION_SCORE_MIN_VALUE
        if (expressionScore.compareTo(this.processedFilter.getExprScoreMinValue()) < 0) {
            expressionScore = this.processedFilter.getExprScoreMinValue();
        }
        if (expressionScore.compareTo(this.processedFilter.getExprScoreMaxValue()) > 0) {
            log.warn("Expression score should always be lower or equals to "
                    + this.processedFilter.getExprScoreMaxValue()
                    + ". The value was " + expressionScore + "and was then manually updated to "
                    + this.processedFilter.getExprScoreMaxValue() + ".");
            expressionScore = this.processedFilter.getExprScoreMaxValue();
        }
        return log.traceExit(expressionScore);
    }

    private <T extends FDRPValue> Entry<ExpressionSummary, SummaryQuality>
    inferSummaryCallTypeAndQuality(Set<FDRPValue> fdrPValues, Set<T> bestDescendantFdrPValues,
            Set<DataType> requestedDataTypes) {
        log.traceEntry("{}, {}, {}", fdrPValues, bestDescendantFdrPValues, requestedDataTypes);

        EnumSet<DataType> realRequestedDataTypes =
                requestedDataTypes == null || requestedDataTypes.isEmpty()?
                EnumSet.allOf(DataType.class): EnumSet.copyOf(requestedDataTypes);
        EnumSet<DataType> requestedDataTypesTrustedForAbsentCalls = realRequestedDataTypes.stream()
                .filter(dt -> dt.isTrustedForAbsentCalls())
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(DataType.class)));
        BigDecimal dataTypesPVal = null;
        BigDecimal dataTypesTrustedForAbsentPVal = null;
        for (FDRPValue pVal: fdrPValues) {
            if (pVal.getDataTypes().equals(realRequestedDataTypes)) {
                assert dataTypesPVal == null:
                    "There should be only one FDR p-value matching data type selection";
                dataTypesPVal = pVal.getPValue();
            }
            if (!requestedDataTypesTrustedForAbsentCalls.isEmpty() &&
                    pVal.getDataTypes().equals(requestedDataTypesTrustedForAbsentCalls)) {
                assert dataTypesTrustedForAbsentPVal == null:
                    "There should be only one FDR p-value matching data type selection";
                dataTypesTrustedForAbsentPVal = pVal.getPValue();
            }
        }
        BigDecimal dataTypesBestDescendantPVal = null;
        BigDecimal dataTypesTrustedForAbsentBestDescendantPVal = null;
        if(bestDescendantFdrPValues != null) {
            for (FDRPValue pVal: bestDescendantFdrPValues) {
                if (pVal.getDataTypes().equals(realRequestedDataTypes)) {
                    assert dataTypesBestDescendantPVal == null:
                        "There should be only one FDR p-value matching data type selection";
                    dataTypesBestDescendantPVal = pVal.getPValue();
                }
                if (!requestedDataTypesTrustedForAbsentCalls.isEmpty() &&
                        pVal.getDataTypes().equals(requestedDataTypesTrustedForAbsentCalls)) {
                    assert dataTypesTrustedForAbsentBestDescendantPVal == null:
                        "There should be only one FDR p-value matching data type selection";
                    dataTypesTrustedForAbsentBestDescendantPVal = pVal.getPValue();
                }
            }
        }
        log.trace("{} - {} - {} - {} - {} - {}", dataTypesPVal, requestedDataTypesTrustedForAbsentCalls,
                dataTypesTrustedForAbsentPVal, bestDescendantFdrPValues, dataTypesBestDescendantPVal,
                dataTypesTrustedForAbsentBestDescendantPVal);
        String exceptionMsg = "It should have been possible to infer "
                + "ExpressionSummary and SummaryQuality. dataTypesPVal: " + dataTypesPVal
                + ", dataTypesTrustedForAbsentPVal: " + dataTypesTrustedForAbsentPVal
                + ", dataTypesBestDescendantPVal: " + dataTypesBestDescendantPVal
                + ", dataTypesTrustedForAbsentBestDescendantPVal: "
                + dataTypesTrustedForAbsentBestDescendantPVal;
        if (dataTypesPVal == null ||
                !(bestDescendantFdrPValues == null || bestDescendantFdrPValues.isEmpty()) && 
                dataTypesBestDescendantPVal == null) {
            throw log.throwing(new IllegalStateException(exceptionMsg));
        }

        //The order of the comparison is important
        if (dataTypesPVal.compareTo(this.processedFilter.getPresentHighThreshold()) <= 0) {
            return log.traceExit(new AbstractMap.SimpleEntry<>(
                    ExpressionSummary.EXPRESSED, SummaryQuality.GOLD));
        }
        if (dataTypesPVal.compareTo(this.processedFilter.getPresentLowThreshold()) <= 0) {
            return log.traceExit(new AbstractMap.SimpleEntry<>(
                    ExpressionSummary.EXPRESSED, SummaryQuality.SILVER));
        }
        if (dataTypesBestDescendantPVal != null &&
                dataTypesBestDescendantPVal.compareTo(this.processedFilter.getPresentLowThreshold()) <= 0) {
            return log.traceExit(new AbstractMap.SimpleEntry<>(
                    ExpressionSummary.EXPRESSED, SummaryQuality.BRONZE));
        }
        //If there is PRESENT LOW expression in a sub-condition when only considering
        //the data types trusted for ABSENT calls, the resulting SummaryQuality
        //cannot be better than BRONZE.
        //(the case where there is a PRESENT LOW expression in a sub-condition when considering
        //all requested data types is already addressed in the previous condition).
        //Also if there is no data type trusted for absent calls, it can't be better than bronze
        boolean absCallCannotBeBetterThanBronze = requestedDataTypesTrustedForAbsentCalls.isEmpty() ||
                //In case there was no data from any trusted data type
                dataTypesTrustedForAbsentPVal == null ||
                //Or in case there is a present call in a sub-condition when using trusted data types
                dataTypesTrustedForAbsentBestDescendantPVal != null &&
                        dataTypesTrustedForAbsentBestDescendantPVal
                        .compareTo(this.processedFilter.getPresentLowThreshold()) <= 0;
        if (dataTypesTrustedForAbsentPVal != null &&
                dataTypesPVal.compareTo(this.processedFilter.getAbsentHighThreshold()) > 0 &&
                dataTypesTrustedForAbsentPVal.compareTo(this.processedFilter.getAbsentHighThreshold()) > 0) {
            return log.traceExit(new AbstractMap.SimpleEntry<>(
                    ExpressionSummary.NOT_EXPRESSED,
                    absCallCannotBeBetterThanBronze? SummaryQuality.BRONZE: SummaryQuality.GOLD));
        }
        if (dataTypesPVal.compareTo(this.processedFilter.getAbsentLowThreshold()) > 0) {
            if (dataTypesTrustedForAbsentPVal != null &&
                    dataTypesTrustedForAbsentPVal.compareTo(this.processedFilter.getAbsentLowThreshold()) > 0) {
                return log.traceExit(new AbstractMap.SimpleEntry<>(
                        ExpressionSummary.NOT_EXPRESSED,
                        absCallCannotBeBetterThanBronze? SummaryQuality.BRONZE: SummaryQuality.SILVER));
            }
            return log.traceExit(new AbstractMap.SimpleEntry<>(
                    ExpressionSummary.NOT_EXPRESSED, SummaryQuality.BRONZE));
        }
        throw log.throwing(new IllegalStateException(exceptionMsg));
    }

    private static DataPropagation2 computeDataPropagation(Set<ExpressionCallData2> callData) {
        log.traceEntry("{}", callData);

        if (callData == null || callData.isEmpty()) {
            throw log.throwing(new IllegalArgumentException(
                    "Missing info for inferring data propagation. CallData: " + callData));
        }

        return log.traceExit(mergeDataPropagations(callData.stream()
                .filter(cd -> cd.getDataPropagation() != null)
                .map(cd -> cd.getDataPropagation())
                .collect(Collectors.toSet())));
    }
    protected static DataPropagation2 mergeDataPropagations(Collection<DataPropagation2> dataProps) {
        log.traceEntry("{}", dataProps);

        if (dataProps == null || dataProps.isEmpty() || dataProps.stream().anyMatch(e -> e == null)) {
            throw log.throwing(new IllegalArgumentException("Invalid DataPropagations"));
        }
        Map<Set<ConditionParameter<?, ?>>, Integer> selfObservationCounts = dataProps.stream()
                .flatMap(dp -> dp.getSelfObservationCounts().entrySet().stream())
                .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue(),
                        (v1, v2) -> (v1 + v2)));
        Map<Set<ConditionParameter<?, ?>>, Integer> descendantObservationCounts = dataProps.stream()
                .flatMap(dp -> dp.getDescendantObservationCounts().entrySet().stream())
                .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue(),
                        (v1, v2) -> (v1 + v2)));

        return log.traceExit(new DataPropagation2(selfObservationCounts, descendantObservationCounts));
    }

    private static ExpressionLevelInfo loadExpressionLevelInfo(ExpressionSummary exprSummary,
            BigDecimal rank, BigDecimal expressionScore, BigDecimal maxRankForExpressionScore) {
        log.traceEntry("{}, {}, {}", exprSummary, rank, expressionScore);
        return log.traceExit(new ExpressionLevelInfo(rank, expressionScore, maxRankForExpressionScore,
                null, null));
    }
}
