package org.bgee.model.expressiondata.call;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.ComposedEntity;
import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.anatdev.AnatEntityService;
import org.bgee.model.anatdev.DevStage;
import org.bgee.model.anatdev.DevStageService;
import org.bgee.model.anatdev.Sex;
import org.bgee.model.anatdev.SexService;
import org.bgee.model.anatdev.Strain;
import org.bgee.model.anatdev.StrainService;
import org.bgee.model.dao.api.expressiondata.DAODataType;
import org.bgee.model.dao.api.expressiondata.call.CallObservedDataDAOFilter2;
import org.bgee.model.dao.api.expressiondata.call.ConditionDAO;
import org.bgee.model.dao.api.expressiondata.call.DAOConditionFilter2;
import org.bgee.model.dao.api.expressiondata.call.DAOFDRPValueFilter2;
import org.bgee.model.dao.api.expressiondata.call.DAOPropagationState;
import org.bgee.model.dao.api.expressiondata.call.ConditionDAO.ConditionTO;
import org.bgee.model.dao.api.expressiondata.call.ConditionDAO.ConditionTOResultSet;
import org.bgee.model.expressiondata.BaseConditionFilter2.FilterIds;
import org.bgee.model.expressiondata.baseelements.ConditionParameter;
import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.model.expressiondata.baseelements.SummaryCallType;
import org.bgee.model.expressiondata.baseelements.SummaryQuality;
import org.bgee.model.expressiondata.call.CallFilter.ExpressionCallFilter2;
import org.bgee.model.ontology.MultiSpeciesOntology;
import org.bgee.model.ontology.OntologyService;
import org.bgee.model.ontology.RelationType;
import org.bgee.model.species.Species;

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
                    case SC_RNA_SEQ: 
                        return log.traceExit(DAODataType.FULL_LENGTH);
                    default: 
                        throw log.throwing(new IllegalStateException("Unsupported DAODataType: " + dt));
                    }
                }).collect(Collectors.toCollection(() -> EnumSet.noneOf(DAODataType.class))));
    }

    public EnumSet<ConditionDAO.ConditionParameter> convertCondParamsToDAOCondParams(
            Collection<ConditionParameter<?, ?>> condParams) {
        log.traceEntry("{}", condParams);
        if (condParams == null) {
            return log.traceExit((EnumSet<ConditionDAO.ConditionParameter>) null);
        }
        if (condParams.isEmpty()) {
            return log.traceExit(EnumSet.noneOf(ConditionDAO.ConditionParameter.class));
        }
        if (condParams.stream().anyMatch(e -> e == null)) {
            throw log.throwing(new UnsupportedOperationException(
                    "No Condition parameter can be null"));
        }

        return log.traceExit(condParams.stream()
                .flatMap(param -> {
                    if (ConditionParameter.ANAT_ENTITY_CELL_TYPE.equals(param)) {
                        return Stream.of(ConditionDAO.ConditionParameter.ANAT_ENTITY,
                                ConditionDAO.ConditionParameter.CELL_TYPE);
                    } else if (ConditionParameter.DEV_STAGE.equals(param)) {
                        return Stream.of(ConditionDAO.ConditionParameter.STAGE);
                    } else if (ConditionParameter.SEX.equals(param)) {
                        return Stream.of(ConditionDAO.ConditionParameter.SEX);
                    } else if (ConditionParameter.STRAIN.equals(param)) {
                        return Stream.of(ConditionDAO.ConditionParameter.STRAIN);
                    }
                    throw log.throwing(new UnsupportedOperationException(
                            "Condition parameter not supported: " + param));
                })
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(
                        ConditionDAO.ConditionParameter.class))));
    }
    public EnumSet<ConditionDAO.Attribute> convertCondParamsToDAOCondAttributes(
            Collection<ConditionParameter<?, ?>> condParams) {
        log.traceEntry("{}", condParams);
        if (condParams == null) {
            return log.traceExit((EnumSet<ConditionDAO.Attribute>) null);
        }
        if (condParams.isEmpty()) {
            return log.traceExit(EnumSet.noneOf(ConditionDAO.Attribute.class));
        }
        if (condParams.stream().anyMatch(e -> e == null)) {
            throw log.throwing(new UnsupportedOperationException(
                    "No Condition parameter can be null"));
        }

        return log.traceExit(condParams.stream()
                .flatMap(param -> {
                    if (ConditionParameter.ANAT_ENTITY_CELL_TYPE.equals(param)) {
                        return Stream.of(ConditionDAO.Attribute.ANAT_ENTITY_ID,
                                ConditionDAO.Attribute.CELL_TYPE_ID);
                    } else if (ConditionParameter.DEV_STAGE.equals(param)) {
                        return Stream.of(ConditionDAO.Attribute.STAGE_ID);
                    } else if (ConditionParameter.SEX.equals(param)) {
                        return Stream.of(ConditionDAO.Attribute.SEX_ID);
                    } else if (ConditionParameter.STRAIN.equals(param)) {
                        return Stream.of(ConditionDAO.Attribute.STRAIN_ID);
                    }
                    throw log.throwing(new UnsupportedOperationException(
                            "Condition parameter not supported: " + param));
                })
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(
                        ConditionDAO.Attribute.class))));
    }

    public Set<Set<DAOFDRPValueFilter2>> generateExprQualDAOPValFilters(
            ExpressionCallFilter2 callFilter, BigDecimal presentLowThreshold,
            BigDecimal presentHighThreshold, BigDecimal absentLowThreshold,
            BigDecimal absentHighThreshold) {
        log.traceEntry("{}, {}, {}, {}, {}", callFilter, presentLowThreshold,
                presentHighThreshold, absentLowThreshold, absentHighThreshold);

        if (callFilter == null ||
                (callFilter.getSummaryCallTypeQualityFilter().equals(ExpressionCallFilter2.ALL_CALLS) &&
                //We still need the PValueFilters if only some data types are requested:
                //it is how we filter for data types supporting a call
                (callFilter.getDataTypeFilters().isEmpty() ||
                        callFilter.getDataTypeFilters().equals(EnumSet.allOf(DataType.class)))) &&
                //Same if only specific condition parameter combination is requested
                (callFilter.getCondParamCombination().isEmpty() ||
                        callFilter.getCondParamCombination().containsAll(ConditionParameter.allOf()))) {
            return new HashSet<>();
        }

        EnumSet<DAODataType> daoDataTypes = this.convertDataTypeToDAODataType(
                callFilter == null? null: callFilter.getDataTypeFilters());
        Set<ConditionParameter<?, ?>> condParams = callFilter == null? ConditionParameter.allOf():
            callFilter.getCondParamCombination();
        EnumSet<ConditionDAO.ConditionParameter> daoCondParams =
                this.convertCondParamsToDAOCondParams(condParams);

        return log.traceExit(callFilter.getSummaryCallTypeQualityFilter()
        .entrySet().stream()
        .flatMap(e -> {
            SummaryCallType.ExpressionSummary callType = e.getKey();
            SummaryQuality qual = e.getValue();
            //DAOFDRPValueFilters in the inner sets are seen as "AND" conditions,
            //the Sets in the outer Set are seen as "OR" conditions.
            Set<Set<DAOFDRPValueFilter2>> pValFilters = new HashSet<>();

            if (callType.equals(SummaryCallType.ExpressionSummary.EXPRESSED)) {
                if (qual.equals(SummaryQuality.GOLD)) {
                    //If minimum GOLD is requested, we only want calls with FDR-corrected p-value <= 0.01
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
            Collection<ConditionFilter2> condFilters, OntologyService ontService,
            Set<Integer> consideredSpeciesIds) {
        log.traceEntry("{}, {}, {}", condFilters, ontService, consideredSpeciesIds);
        if (condFilters == null || condFilters.isEmpty()) {
            return log.traceExit(new HashSet<>());
        }
    
        //First, in order to load appropriately the ontologies,
        //we retrieve terms and species for which we request to retrieve children terms.
        Set<String> anatEntityAndCellTypeIdsWithChildrenRequested = new HashSet<>();
        Set<Integer> speciesIdsWithAnatCellChildrenRequested = new HashSet<>();
        Set<String> devStageIdsWithChildrenRequested = new HashSet<>();
        Set<Integer> speciesIdsWithDevStageChildrenRequested = new HashSet<>();
        for (ConditionFilter2 filter: condFilters) {
            //For the time being, we still use the DAOConditionFilter using different arguments
            //for anat. entity and cell type. For now we will consider that the first FilterIds
            //in the new ComposedFilterIds are the anatEntityIds, and the second FilterIds the cellTypeIds.
            FilterIds<String> anatEntityFilterIds = filter.getComposedFilterIds(
                    ConditionParameter.ANAT_ENTITY_CELL_TYPE).getFilterIds(0);
            FilterIds<String> cellTypeFilterIds = filter.getComposedFilterIds(
                    ConditionParameter.ANAT_ENTITY_CELL_TYPE).getFilterIds(1);
            if (anatEntityFilterIds != null && anatEntityFilterIds.isIncludeChildTerms()) {
                anatEntityAndCellTypeIdsWithChildrenRequested.addAll(anatEntityFilterIds.getIds());
                speciesIdsWithAnatCellChildrenRequested.add(filter.getSpeciesId());
            }
            if (cellTypeFilterIds != null && cellTypeFilterIds.isIncludeChildTerms()) {
                anatEntityAndCellTypeIdsWithChildrenRequested.addAll(cellTypeFilterIds.getIds());
                speciesIdsWithAnatCellChildrenRequested.add(filter.getSpeciesId());
            }
            //For now we consider there is no composition for dev. stages
            assert !filter.getComposedFilterIds(ConditionParameter.DEV_STAGE).isComposed();
            FilterIds<String> devStageFilterIds = filter.getComposedFilterIds(
                    ConditionParameter.DEV_STAGE).getFilterIds(0);
            if (devStageFilterIds != null && devStageFilterIds.isIncludeChildTerms()) {
                devStageIdsWithChildrenRequested.addAll(devStageFilterIds.getIds());
                speciesIdsWithDevStageChildrenRequested.add(filter.getSpeciesId());
            }
        }
    
        //Now we load the ontologies if needed
        MultiSpeciesOntology<AnatEntity, String> anatOntology = anatEntityAndCellTypeIdsWithChildrenRequested.isEmpty()?
                null: ontService.getAnatEntityOntology(
                        speciesIdsWithAnatCellChildrenRequested, anatEntityAndCellTypeIdsWithChildrenRequested,
                        EnumSet.of(RelationType.ISA_PARTOF), false, true);
        MultiSpeciesOntology<DevStage, String> stageOntology = devStageIdsWithChildrenRequested.isEmpty()?
                null: ontService.getDevStageOntology(
                        speciesIdsWithDevStageChildrenRequested, devStageIdsWithChildrenRequested, false, true);
        //There is no ontology for RawDataSex and RawDataStrain (String), really it's simply one root
        //with all other terms at the first level.
    
        //Now we have everything we need to create the DAO filters
        Set<DAOConditionFilter2> daoCondFilters = new HashSet<>();
        for (ConditionFilter2 filter: condFilters) {
            Set<String> anatEntityIds = new HashSet<>();
            Set<String> devStageIds = new HashSet<>();
            Set<String> cellTypeIds = new HashSet<>();

            //For the time being, we still use the DAOConditionFilter using different arguments
            //for anat. entity and cell type. For now we will consider that the first FilterIds
            //in the new ComposedFilterIds are the anatEntityIds, and the second FilterIds the cellTypeIds.
            FilterIds<String> anatEntityFilterIds = filter.getComposedFilterIds(
                    ConditionParameter.ANAT_ENTITY_CELL_TYPE).getFilterIds(0);
            FilterIds<String> cellTypeFilterIds = filter.getComposedFilterIds(
                    ConditionParameter.ANAT_ENTITY_CELL_TYPE).getFilterIds(1);
            if (anatEntityFilterIds != null) {
                anatEntityIds.addAll(anatEntityFilterIds.getIds());
                if (anatEntityFilterIds.isIncludeChildTerms()) {
                    anatEntityIds.addAll(
                            anatEntityFilterIds.getIds().stream()
                            .flatMap(id -> anatOntology.getDescendantIds(
                                    id, false, Collections.singleton(filter.getSpeciesId()))
                                    .stream())
                            .collect(Collectors.toSet())
                    );
                }
            }
            if (cellTypeFilterIds != null) {
                cellTypeIds.addAll(cellTypeFilterIds.getIds());
                if (cellTypeFilterIds.isIncludeChildTerms()) {
                    cellTypeIds.addAll(
                            cellTypeFilterIds.getIds().stream()
                            .flatMap(id -> anatOntology.getDescendantIds(
                                    id, false, Collections.singleton(filter.getSpeciesId()))
                                    .stream())
                            .collect(Collectors.toSet())
                    );
                }
            }

            //For now we consider there is no composition for dev. stages
            assert !filter.getComposedFilterIds(ConditionParameter.DEV_STAGE).isComposed();
            FilterIds<String> devStageFilterIds = filter.getComposedFilterIds(
                    ConditionParameter.DEV_STAGE).getFilterIds(0);
            if (devStageFilterIds != null) {
                devStageIds.addAll(devStageFilterIds.getIds());
                if (devStageFilterIds.isIncludeChildTerms()) {
                    devStageIds.addAll(
                            devStageFilterIds.getIds().stream()
                            .flatMap(id -> stageOntology.getDescendantIds(
                                    id, false, Collections.singleton(filter.getSpeciesId()))
                                    .stream())
                            .collect(Collectors.toSet())
                    );
                }
            }

            //For now we consider there is no composition for sexes and strains
            assert !filter.getComposedFilterIds(ConditionParameter.SEX).isComposed();
            assert !filter.getComposedFilterIds(ConditionParameter.STRAIN).isComposed();
            Set<ConditionParameter<?, ?>> condParamComb = filter.getCondParamCombination();
            DAOConditionFilter2 daoCondFilter = new DAOConditionFilter2(
                    //consideredSpeciesIds might itself be null, but it could have
                    //the species IDs requested in GeneFilters, to query conditions
                    //only in those species
                    filter.getSpeciesId() == null? consideredSpeciesIds:
                        Collections.singleton(filter.getSpeciesId()),
                    !condParamComb.contains(ConditionParameter.ANAT_ENTITY_CELL_TYPE)?
                            Collections.singleton(ConditionDAO.ANAT_ENTITY_ROOT_ID):
                                anatEntityIds,
                    !condParamComb.contains(ConditionParameter.DEV_STAGE)?
                            Collections.singleton(ConditionDAO.DEV_STAGE_ROOT_ID):
                                devStageIds,
                    !condParamComb.contains(ConditionParameter.ANAT_ENTITY_CELL_TYPE)?
                            Collections.singleton(ConditionDAO.CELL_TYPE_ROOT_ID):
                                cellTypeIds,
                    !condParamComb.contains(ConditionParameter.SEX)?
                            Collections.singleton(ConditionDAO.SEX_ROOT_ID):
                                filter.getComposedFilterIds(ConditionParameter.SEX).getIds(0),
                    !condParamComb.contains(ConditionParameter.STRAIN)?
                            Collections.singleton(ConditionDAO.STRAIN_ROOT_ID):
                                filter.getComposedFilterIds(ConditionParameter.STRAIN).getIds(0),
                    convertCondParamsToDAOCondParams(filter.getObservedCondForParams()));
            log.debug("ConditionFilter: {} - condParamCombination: {} - Generated DAOConditionFilter: {}",
                    filter, condParamComb, daoCondFilter);
            daoCondFilters.add(daoCondFilter);
        }
    
        //Now we filter the daoCondFilters: if one of them target a species with no additional parameters,
        //then we discard any other filter targeting the same species
        Map<Set<Integer>, List<DAOConditionFilter2>> filtersPerSpecies = daoCondFilters.stream()
                .collect(Collectors.groupingBy(f -> f.getSpeciesIds()));
        return log.traceExit(
            filtersPerSpecies.values().stream().flatMap(l -> {
                DAOConditionFilter2 noFilter = l.stream()
                            .filter(f -> f.areAllFiltersExceptSpeciesEmpty())
                            .findAny().orElse(null);
                    if (noFilter != null) {
                        return Stream.of(noFilter);
                    }
                    return l.stream();
            }).collect(Collectors.toSet())
        );
    }

    public CallObservedDataDAOFilter2 convertCallObservedDataToDAO(ExpressionCallFilter2 filter) {
        log.traceEntry("{}", filter);
        if (filter == null || filter.getCallObservedDataCondParams().isEmpty()) {
            return log.traceExit((CallObservedDataDAOFilter2) null);
        }
        return log.traceExit(new CallObservedDataDAOFilter2(
                this.convertDataTypeToDAODataType(filter.getDataTypeFilters()),
                this.convertCondParamsToDAOCondParams(filter.getCallObservedDataCondParams()),
                filter.getCallObservedDataFilter()));
    }

    public Map<Integer, Condition2> loadGlobalConditionMap(Collection<Species> species,
            Collection<DAOConditionFilter2> conditionFilters,
            Collection<ConditionDAO.Attribute> conditionDAOAttrs, ConditionDAO conditionDAO,
            AnatEntityService anatEntityService, DevStageService devStageService,
            SexService sexService, StrainService strainService) {
        log.traceEntry("{}, {}, {}, {}, {}, {}, {}, {}", species, conditionFilters, conditionDAOAttrs, 
                conditionDAO, anatEntityService, devStageService, sexService, strainService);

        return log.traceExit(loadConditionMapFromResultSet(
                (attrs) -> conditionDAO.getGlobalConditions(conditionFilters, attrs),
                conditionDAOAttrs, species, anatEntityService, devStageService, sexService,
                strainService));
    }
    
    public Map<Integer, Condition2> loadConditionMapFromResultSet(
            Function<Collection<ConditionDAO.Attribute>, ConditionTOResultSet> rsFunc,
            Collection<ConditionDAO.Attribute> conditionDAOAttrs, Collection<Species> species,
            AnatEntityService anatEntityService, DevStageService devStageService,
            SexService sexService, StrainService strainService) {
        log.traceEntry("{}, {}, {}, {}, {}, {}, {}", rsFunc, conditionDAOAttrs, species, 
                anatEntityService, devStageService, sexService, strainService);

        if (species == null || species.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("Some species must be provided"));
        }

        Map<Integer, Species> speMap = species.stream()
                .collect(Collectors.toMap(s -> s.getId(), s -> s, (s1, s2) -> s1));
        Set<Integer> usedSpeciesIds = new HashSet<>();
        Set<String> anatEntityIds = new HashSet<>();
        Set<String> stageIds = new HashSet<>();
        Set<String> cellTypeIds = new HashSet<>();
        Set<String> sexIds = new HashSet<>();
        Set<String> strainIds = new HashSet<>();
        Set<ConditionTO> conditionTOs = new HashSet<>();

        //we need to retrieve the attributes requested, plus the condition ID and species ID in all cases.
        Set<ConditionDAO.Attribute> clonedAttrs = conditionDAOAttrs == null || conditionDAOAttrs.isEmpty()?
                EnumSet.allOf(ConditionDAO.Attribute.class): EnumSet.copyOf(conditionDAOAttrs);
        clonedAttrs.addAll(EnumSet.of(ConditionDAO.Attribute.ID, ConditionDAO.Attribute.SPECIES_ID));
        ConditionTOResultSet rs = rsFunc.apply(clonedAttrs);

        while (rs.next()) {
            ConditionTO condTO = rs.getTO();
            if (!speMap.keySet().contains(condTO.getSpeciesId())) {
                throw log.throwing(new IllegalArgumentException(
                        "The retrieved ConditionTOs do not match the provided Species."));
            }
            conditionTOs.add(condTO);

            usedSpeciesIds.add(condTO.getSpeciesId());

            if (condTO.getAnatEntityId() != null) {
                anatEntityIds.add(condTO.getAnatEntityId());
            }
            if (condTO.getStageId() != null) {
                stageIds.add(condTO.getStageId());
            }
            if (condTO.getCellTypeId() != null) {
                cellTypeIds.add(condTO.getCellTypeId());
            }
            if (condTO.getSex() != null) {
                sexIds.add(condTO.getSex().getStringRepresentation());
            }
            if (condTO.getStrainId() != null) {
                strainIds.add(condTO.getStrainId());
            }
        }
        
        //merge anat entities and cell types to call only once loadAnatEntities
        Set<String> anatAndCellIds = new HashSet<String>(anatEntityIds);
        anatAndCellIds.addAll(cellTypeIds);

        final Map<String, AnatEntity> anatAndCellMap = anatAndCellIds.isEmpty()? new HashMap<>():
            anatEntityService.loadAnatEntities(
                    usedSpeciesIds, true, anatAndCellIds, false)
            .collect(Collectors.toMap(a -> a.getId(), a -> a));
        if (!anatAndCellIds.isEmpty() && anatAndCellMap.size() != anatAndCellIds.size()) {
            anatAndCellIds.removeAll(anatAndCellMap.keySet());
            throw log.throwing(new IllegalStateException("Some anat. entities or cell type used in a condition "
                    + "are not supposed to exist in the related species. Species: " + usedSpeciesIds
                    + " - anat. entities: " + anatAndCellIds));
        }
        final Map<String, DevStage> stageMap = stageIds.isEmpty()? new HashMap<>():
            devStageService.loadDevStages(
                    usedSpeciesIds, true, stageIds, false)
            .collect(Collectors.toMap(s -> s.getId(), s -> s));
        if (!stageIds.isEmpty() && stageMap.size() != stageIds.size()) {
            stageIds.removeAll(stageMap.keySet());
            throw log.throwing(new IllegalStateException("Some stages used in a condition "
                    + "are not supposed to exist in the related species. Species: " + usedSpeciesIds
                    + " - stages: " + stageIds));
        }
        final Map<String, Sex> sexMap = sexIds.isEmpty()? new HashMap<>():
            //if a sex is not supported, an exception will be immediately thrown
            sexService.loadSexes(sexIds)
            .collect(Collectors.toMap(s -> s.getId(), s -> s));
        
        final Map<String, Strain> strainMap = strainIds.isEmpty()? new HashMap<>():
            strainService.loadStrains(strainIds)
            .collect(Collectors.toMap(s -> s.getId(), s -> s));
        //In case we retrieve strains from the database
        if (!strainIds.isEmpty() && strainMap.size() != strainIds.size()) {
            strainIds.removeAll(strainMap.keySet());
            throw log.throwing(new IllegalStateException("Some strains used in a condition "
                    + "are not supposed to exist in the related species. Species: " + usedSpeciesIds
                    + " - strains: " + strainIds));
        }
        
        return log.traceExit(conditionTOs.stream()
                .collect(Collectors.toMap(
                        cTO -> cTO.getId(), 
                        cTO -> {
                            Map<ConditionParameter<?, ?>, ComposedEntity<?>> condParamEntities =
                                    new HashMap<>();

                            AnatEntity anatEntity = cTO.getAnatEntityId() == null? null:
                                    Optional.ofNullable(anatAndCellMap.get(cTO.getAnatEntityId()))
                                    .orElseThrow(() -> new IllegalStateException(
                                            "Anat. entity not found: " + cTO.getAnatEntityId()));
                            AnatEntity cellType = cTO.getCellTypeId() == null? null:
                                Optional.ofNullable(anatAndCellMap.get(cTO.getCellTypeId()))
                                .orElseThrow(() -> new IllegalStateException(
                                        "Anat. entity not found: " + cTO.getCellTypeId()));
                            LinkedHashSet<AnatEntity> anatEntitiesCellTypes = new LinkedHashSet<>();
                            if (cellType != null) {
                                anatEntitiesCellTypes.add(cellType);
                            }
                            if (anatEntity != null) {
                                anatEntitiesCellTypes.add(anatEntity);
                            }
                            if (!anatEntitiesCellTypes.isEmpty()) {
                                condParamEntities.put(ConditionParameter.ANAT_ENTITY_CELL_TYPE,
                                        new ComposedEntity<>(anatEntitiesCellTypes, AnatEntity.class));
                            }

                            DevStage stage = cTO.getStageId() == null? null:
                                Optional.ofNullable(stageMap.get(cTO.getStageId()))
                                .orElseThrow(() -> new IllegalStateException("Stage not found: "
                                                + cTO.getStageId()));
                            if (stage != null) {
                                condParamEntities.put(ConditionParameter.DEV_STAGE,
                                        new ComposedEntity<>(stage, DevStage.class));
                            }

                            Sex sex = cTO.getSex() == null? null:
                                Optional.ofNullable(sexMap.get(cTO.getSex().getStringRepresentation()))
                                .orElseThrow(() -> new IllegalStateException("sex not found: "
                                            + cTO.getSex().getStringRepresentation()));
                            if (sex != null) {
                                condParamEntities.put(ConditionParameter.SEX,
                                        new ComposedEntity<>(sex, Sex.class));
                            }

                            Strain strain = cTO.getStrainId() == null? null:
                                Optional.ofNullable(strainMap.get(cTO.getStrainId()))
                                .orElseThrow(() -> new IllegalStateException("strain not found: "
                                            + cTO.getStrainId()));
                            if (strain != null) {
                                condParamEntities.put(ConditionParameter.STRAIN,
                                        new ComposedEntity<>(strain, Strain.class));
                            }

                            return new Condition2(condParamEntities,
                                    Optional.ofNullable(speMap.get(cTO.getSpeciesId()))
                                    .orElseThrow(() -> new IllegalStateException(
                                            "Species not found: " + cTO.getSpeciesId())));
                        })));
    }
}
