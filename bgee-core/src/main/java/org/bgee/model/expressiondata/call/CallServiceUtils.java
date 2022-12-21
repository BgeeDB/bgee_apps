package org.bgee.model.expressiondata.call;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.expressiondata.DAODataType;
import org.bgee.model.dao.api.expressiondata.call.ConditionDAO;
import org.bgee.model.dao.api.expressiondata.call.DAOConditionFilter2;
import org.bgee.model.dao.api.expressiondata.call.DAOFDRPValueFilter2;
import org.bgee.model.dao.api.expressiondata.call.DAOPropagationState;
import org.bgee.model.expressiondata.baseelements.ConditionParameter;
import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.model.expressiondata.baseelements.SummaryCallType;
import org.bgee.model.expressiondata.baseelements.SummaryQuality;
import org.bgee.model.expressiondata.call.CallFilter.ExpressionCallFilter2;

public class CallServiceUtils {
    private final static Logger log = LogManager.getLogger(CallServiceUtils.class.getName());


    public EnumSet<DAODataType> convertDataTypeToDAODataType(Collection<DataType> dts)
            throws IllegalStateException{
        log.traceEntry("{}", dts);
        
        if (dts == null || dts.isEmpty()) {
            return log.traceExit(EnumSet.allOf(DAODataType.class));
        }
        return log.traceExit(
                //We create an EnumSet not to iterate over potentially redundant elements
                EnumSet.copyOf(dts).stream()
                .map(dt -> {
                    switch(dt) {
                    case AFFYMETRIX: 
                        return log.traceExit(DAODataType.AFFYMETRIX);
                    case EST: 
                        return log.traceExit(DAODataType.EST);
                    case IN_SITU: 
                        return log.traceExit(DAODataType.IN_SITU);
                    case RNA_SEQ: 
                        return log.traceExit(DAODataType.RNA_SEQ);
                    case FULL_LENGTH: 
                        return log.traceExit(DAODataType.FULL_LENGTH);
                    default: 
                        throw log.throwing(new IllegalStateException("Unsupported DAODataType: " + dt));
                    }
                }).collect(Collectors.toCollection(() -> EnumSet.noneOf(DAODataType.class))));
    }

    public EnumSet<ConditionDAO.ConditionParameter> convertCondParamsToDAOCondParams(
            Collection<ConditionParameter> condParams) {
        log.traceEntry("{}", condParams);
        if (condParams == null) {
            return log.traceExit((EnumSet<ConditionDAO.ConditionParameter>) null);
        }
        if (condParams.isEmpty()) {
            return log.traceExit(EnumSet.noneOf(ConditionDAO.ConditionParameter.class));
        }
        return log.traceExit(
                //We create an EnumSet not to iterate over potentially redundant elements
                EnumSet.copyOf(condParams).stream()
                .map(a -> convertCondParamToDAOCondParam(a))
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(
                        ConditionDAO.ConditionParameter.class))));
    }
    public ConditionDAO.ConditionParameter convertCondParamToDAOCondParam(
            ConditionParameter condParam) {
        log.traceEntry("{}", condParam);
        switch (condParam) {
            case ANAT_ENTITY:
                return log.traceExit(ConditionDAO.ConditionParameter.ANAT_ENTITY);
            case DEV_STAGE:
                return log.traceExit(ConditionDAO.ConditionParameter.STAGE);
            case CELL_TYPE:
                return log.traceExit(ConditionDAO.ConditionParameter.CELL_TYPE);
            case SEX:
                return log.traceExit(ConditionDAO.ConditionParameter.SEX);
            case STRAIN:
                return log.traceExit(ConditionDAO.ConditionParameter.STRAIN);
            default:
                throw log.throwing(new UnsupportedOperationException(
                    "Condition parameter not supported: " + condParam));
        }
    }

    public Set<Set<DAOFDRPValueFilter2>> generateExprQualDAOPValFilters(
            ExpressionCallFilter2 callFilter, Collection<ConditionParameter> condParams,
            BigDecimal presentLowThreshold, BigDecimal presentHighThreshold,
            BigDecimal absentLowThreshold, BigDecimal absentHighThreshold) {
        log.traceEntry("{}, {}, {}, {}, {}, {}", callFilter, condParams, presentLowThreshold,
                presentHighThreshold, absentLowThreshold, absentHighThreshold);

        EnumSet<DAODataType> daoDataTypes = this.convertDataTypeToDAODataType(
                callFilter == null? null: callFilter.getDataTypeFilters());
        EnumSet<ConditionDAO.ConditionParameter> daoCondParams =
                this.convertCondParamsToDAOCondParams(condParams);

        return log.traceExit((callFilter == null? ExpressionCallFilter2.ALL_CALLS:
                                                  callFilter.getSummaryCallTypeQualityFilter())
        .entrySet().stream()
        .flatMap(e -> {
            SummaryCallType.ExpressionSummary callType = e.getKey();
            SummaryQuality qual = e.getValue();
            //DAOFDRPValueFilters in the inner sets are seen as "AND" conditions,
            //the Sets in the outer Set are seen as "OR" conditions.
            Set<Set<DAOFDRPValueFilter2>> pValFilters = new HashSet<>();

            if (callType.equals(SummaryCallType.ExpressionSummary.EXPRESSED)) {
                if (qual.equals(SummaryQuality.GOLD)) {
                    //If minimum GOLD is requested, we only want calls with FDR-corrected p-value <= 0.1
                    pValFilters.add(Collections.singleton(
                            new DAOFDRPValueFilter2(presentHighThreshold,
                                    daoDataTypes,
                                    DAOFDRPValueFilter2.Qualifier.LESS_THAN_OR_EQUALS_TO,
                                    DAOPropagationState.SELF_AND_DESCENDANT,
                                    false, daoCondParams)));
                } else {
                    //If minimum SILVER is requested, we want calls with FDR-corrected p-value <= 0.05,
                    //we'll get calls SILVER or GOLD
                    pValFilters.add(Collections.singleton(
                            new DAOFDRPValueFilter2(presentLowThreshold,
                                    daoDataTypes,
                                    DAOFDRPValueFilter2.Qualifier.LESS_THAN_OR_EQUALS_TO,
                                    DAOPropagationState.SELF_AND_DESCENDANT,
                                    false, daoCondParams)));
                    //Then, if minimum BRONZE is requested, we also accept calls that are SILVER or GOLD
                    //in a descendant condition. We end up with the following conditions:
                    // * FDR-corrected p-value in condition including sub-conditions <= 0.05
                    //   (SILVER or GOLD)
                    // * OR FDR-corrected p-value in at least one sub-condition <= 0.05 (BRONZE)
                    if (qual.equals(SummaryQuality.BRONZE)) {
                        pValFilters.add(Collections.singleton(
                                new DAOFDRPValueFilter2(presentLowThreshold,
                                        daoDataTypes,
                                        DAOFDRPValueFilter2.Qualifier.LESS_THAN_OR_EQUALS_TO,
                                        DAOPropagationState.DESCENDANT,
                                        false, daoCondParams)));
                    }
                }

            } else if (callType.equals(SummaryCallType.ExpressionSummary.NOT_EXPRESSED)) {
                //For NOT_EXPRESSED, we request that the p-value of the call is non-significant,
                //But also that it is still non-significant when removing data types
                //that we don't trust to produce absent calls (except for BRONZE absent calls).
                //Requirement both for the p-value coming from the condition and its sub-conditions,
                //and the best p-value among the sub-conditions.
                EnumSet<DAODataType> daoDataTypesTrustedForNotExpressed =
                        convertTrustedAbsentDataTypesToDAODataTypes(callFilter.getDataTypeFilters());
                Set<DAOFDRPValueFilter2> absentAndFilters = new HashSet<>();
                //If we request SILVER or GOLD, and there is no data type requested
                //that we trust for generating ABSENT calls, we make an impossible condition
                //so that it returns no result
                //FIXME: the use of this boolean selfObservationRequired in DAOFDRPValueFilter
                //is maybe problematic, probably we should allow to target a specific combination
                //of condition parameters to assess whether there are observed data? I'm not sure,
                //to think about. Or actually, do we still really want this filtering that we must
                //have observed data? (yes, maybe)
                if (daoDataTypesTrustedForNotExpressed.isEmpty() && !qual.equals(SummaryQuality.BRONZE)) {
                    absentAndFilters.add(new DAOFDRPValueFilter2(new BigDecimal("1"),
                                        daoDataTypes,
                                        DAOFDRPValueFilter2.Qualifier.GREATER_THAN,
                                        DAOPropagationState.SELF_AND_DESCENDANT,
                                        true, daoCondParams));
                } else {
                    if (qual.equals(SummaryQuality.GOLD)) {
                        absentAndFilters.add(new DAOFDRPValueFilter2(absentHighThreshold,
                                daoDataTypes,
                                DAOFDRPValueFilter2.Qualifier.GREATER_THAN,
                                DAOPropagationState.SELF_AND_DESCENDANT,
                                true, daoCondParams));
                        //we want the same condition without considering
                        //the data types that we don't trust to produce absent calls
                        absentAndFilters.add(new DAOFDRPValueFilter2(absentHighThreshold,
                                daoDataTypesTrustedForNotExpressed,
                                DAOFDRPValueFilter2.Qualifier.GREATER_THAN,
                                DAOPropagationState.SELF_AND_DESCENDANT,
                                true, daoCondParams));
                    } else {
                        absentAndFilters.add(new DAOFDRPValueFilter2(absentLowThreshold,
                                daoDataTypes,
                                DAOFDRPValueFilter2.Qualifier.GREATER_THAN,
                                DAOPropagationState.SELF_AND_DESCENDANT,
                                true, daoCondParams));
                        //Unless we request BRONZE quality, we want the same condition without considering
                        //the data types that we don't trust to produce absent calls
                        if (qual.equals(SummaryQuality.SILVER)) {
                            absentAndFilters.add(new DAOFDRPValueFilter2(absentLowThreshold,
                                    daoDataTypesTrustedForNotExpressed,
                                    DAOFDRPValueFilter2.Qualifier.GREATER_THAN,
                                    DAOPropagationState.SELF_AND_DESCENDANT,
                                    true, daoCondParams));
                        }
                    }
                    //in all cases, we don't want PRESENT calls in a sub-condition
                    absentAndFilters.add(new DAOFDRPValueFilter2(presentLowThreshold,
                            daoDataTypes,
                            DAOFDRPValueFilter2.Qualifier.GREATER_THAN,
                            DAOPropagationState.DESCENDANT,
                            false, daoCondParams));
                    //And unless we request BRONZE, we want the same to hold true
                    //with only the data types we trust to produce ABSENT calls
                    if (!qual.equals(SummaryQuality.BRONZE)) {
                        absentAndFilters.add(new DAOFDRPValueFilter2(presentLowThreshold,
                                daoDataTypesTrustedForNotExpressed,
                                DAOFDRPValueFilter2.Qualifier.GREATER_THAN,
                                DAOPropagationState.DESCENDANT,
                                false, daoCondParams));
                    }
                }
                pValFilters.add(absentAndFilters);
            }
            return pValFilters.stream();
        }).collect(Collectors.toSet()));
    }

    public EnumSet<DAODataType> convertTrustedAbsentDataTypesToDAODataTypes(
            Collection<DataType> dts) throws IllegalStateException {
        log.traceEntry("{}", dts);

        //Find DataTypes that can be trusted for absent calls. Maybe there will be none among
        //the requested data types. So we need to convert to DAODataTypes in two steps,
        //by checking if dataTypesToConsider is empty, because the method
        //convertDataTypeToDAODataType returns all DAODataTypes when the provided argument
        //of DataTypes is empty or null.
        Set<DataType> dataTypesToConsider =
                (dts == null || dts.isEmpty()? EnumSet.allOf(DataType.class): EnumSet.copyOf(dts))
                .stream()
                .filter(dt -> dt.isTrustedForAbsentCalls())
                .collect(Collectors.toSet());
        return log.traceExit(dataTypesToConsider.isEmpty()? EnumSet.noneOf(DAODataType.class):
            this.convertDataTypeToDAODataType(dataTypesToConsider));
    }

    public Set<DAOConditionFilter2> convertConditionFiltersToDAOConditionFilters(
            Collection<ConditionFilter2> condFilters, Collection<ConditionParameter> condParamCombination) {
        log.traceEntry("{}, {}", condFilters, condParamCombination);
        if (condFilters == null || condFilters.isEmpty()) {
            DAOConditionFilter2 filter = convertConditionFilterToDAOConditionFilter(
                    null, condParamCombination);
            if (filter == null) {
                return log.traceExit(new HashSet<>());
            }
            //Collections.singleton makes the Set immutable,
            //so we use it in HashSet constructor
            return log.traceExit(new HashSet<>(Collections.singleton(filter)));
        }
        return log.traceExit(condFilters.stream()
                .map(condFilter -> convertConditionFilterToDAOConditionFilter(
                        condFilter, condParamCombination))
                .filter(f -> f != null)
                .collect(Collectors.toSet()));
    }
    public DAOConditionFilter2 convertConditionFilterToDAOConditionFilter(
            ConditionFilter2 condFilter, Collection<ConditionParameter> condParamCombination) {
        log.traceEntry("{}, {}", condFilter, condParamCombination);

        EnumSet<ConditionParameter> comb =
                condParamCombination == null || condParamCombination.isEmpty()?
                        EnumSet.allOf(ConditionParameter.class): EnumSet.copyOf(condParamCombination);
        if (condFilter == null && condParamCombination.equals(
                EnumSet.allOf(ConditionParameter.class))) {
            return log.traceExit((DAOConditionFilter2) null);
        }

        DAOConditionFilter2 daoCondFilter = new DAOConditionFilter2(
                !comb.contains(ConditionParameter.ANAT_ENTITY)?
                        Collections.singleton(ConditionDAO.ANAT_ENTITY_ROOT_ID):
                            condFilter != null? condFilter.getAnatEntityIds(): null,
                !comb.contains(ConditionParameter.DEV_STAGE)?
                        Collections.singleton(ConditionDAO.DEV_STAGE_ROOT_ID):
                            condFilter != null? condFilter.getDevStageIds(): null,
                !comb.contains(ConditionParameter.CELL_TYPE)?
                        Collections.singleton(ConditionDAO.CELL_TYPE_ROOT_ID):
                            condFilter != null? condFilter.getCellTypeIds(): null,
                !comb.contains(ConditionParameter.SEX)?
                        Collections.singleton(ConditionDAO.SEX_ROOT_ID):
                            condFilter != null? condFilter.getSexIds(): null,
                !comb.contains(ConditionParameter.STRAIN)?
                        Collections.singleton(ConditionDAO.STRAIN_ROOT_ID):
                            condFilter != null? condFilter.getStrainIds(): null,
                condFilter != null?
                        convertCondParamsToDAOCondParams(condFilter.getObservedCondForParams()): null);
        log.debug("ConditionFilter: {} - condParamCombination: {} - Generated DAOConditionFilter: {}",
                condFilter, condParamCombination, daoCondFilter);
        return log.traceExit(daoCondFilter);
    }
}
