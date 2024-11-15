package org.bgee.model.expressiondata.call;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.CommonService;
import org.bgee.model.ServiceFactory;
import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.anatdev.DevStage;
import org.bgee.model.anatdev.Sex;
import org.bgee.model.anatdev.Strain;
import org.bgee.model.dao.api.expressiondata.call.ConditionDAO;
import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.model.expressiondata.rawdata.RawDataFilter;
import org.bgee.model.expressiondata.rawdata.RawDataLoader;
import org.bgee.model.expressiondata.rawdata.RawDataLoader.InformationType;
import org.bgee.model.expressiondata.rawdata.RawDataService;
import org.bgee.model.expressiondata.rawdata.baseelements.RawDataCondition;
import org.bgee.model.expressiondata.rawdata.baseelements.RawDataContainer;
import org.bgee.model.expressiondata.rawdata.baseelements.RawDataDataType;
import org.bgee.model.gene.Gene;
import org.bgee.model.gene.GeneFilter;
import org.bgee.model.gene.GeneService;
import org.bgee.model.ontology.Ontology;
import org.bgee.model.ontology.OntologyService;

public class OnTheFlyPropagation extends CommonService {
    private final static Logger log = LogManager.getLogger(OnTheFlyPropagation.class.getName());


    public static void main(String[] args) {
        OnTheFlyPropagation test = new OnTheFlyPropagation();
        test.test();
    }

    public OnTheFlyPropagation() {
        super(new ServiceFactory());
    }
    
    public void test() {
        log.traceEntry();
        
        // init services
        OntologyService ontoService = this.getServiceFactory().getOntologyService();
        RawDataService rawDataService = this.getServiceFactory().getRawDataService();
        GeneService geneService = this.getServiceFactory().getGeneService();
        ConditionGraphService condGraphService = this.getServiceFactory().getConditionGraphService();
        
        // init objects used for this test
        int speciesId = 9606;
        Set<String> geneIds = Set.of("ENSG00000008988"
//                , "ENSG00000133112", "ENSG00000087460",
//                "ENSG00000111640", "ENSG00000013563", "ENSG00000080824", "ENSG00000139644",
//                "ENSG00000142534", "ENSG00000074800", "ENSG00000143933", "ENSG00000026025",
//                "ENSG00000198034", "ENSG00000145592", "ENSG00000132475", "ENSG00000113140",
//                "ENSG00000137154", "ENSG00000100201", "ENSG00000063177", "ENSG00000135821",
//                "ENSG00000100234", "ENSG00000174444", "ENSG00000131143", "ENSG00000186468",
//                "ENSG00000140416", "ENSG00000167526", "ENSG00000164032", "ENSG00000167986",
//                "ENSG00000143549", "ENSG00000172757", "ENSG00000115461",
//
//                "ENSG00000000005", "ENSG00000000419", "ENSG00000000457", "ENSG00000000460",
//                "ENSG00000000938", "ENSG00000000971", "ENSG00000001036", "ENSG00000001084",
//                "ENSG00000001167", "ENSG00000001460", "ENSG00000001461", "ENSG00000001497",
//                "ENSG00000001561", "ENSG00000001617", "ENSG00000001626", "ENSG00000001629",
//                "ENSG00000001630", "ENSG00000001631", "ENSG00000002016", "ENSG00000002079",
//                "ENSG00000002330", "ENSG00000002549", "ENSG00000002586", "ENSG00000002587",
//                "ENSG00000002726", "ENSG00000002745", "ENSG00000002746", "ENSG00000002822",
//                "ENSG00000002834"
                );
        EnumSet<DataType> dataTypes = EnumSet.of(DataType.AFFYMETRIX, DataType.RNA_SEQ,
                DataType.SC_RNA_SEQ);
        
        Set<Gene> genes = geneService.loadGenes(new GeneFilter(speciesId, geneIds))
                .collect(Collectors.toSet());

        //Retrieve ontologies once for all genes
        log.info("Start retrieval of ontologies");
        long startTime = System.currentTimeMillis();
        //XXX: can we request only is_a/part_of?
        Ontology<DevStage, String> stageOntology = ontoService
                .getDevStageOntology(speciesId, null);
        Ontology<AnatEntity, String> anatEntityOntology = ontoService
              .getAnatEntityOntology(speciesId, null);
        Ontology<AnatEntity, String> celltypeOntology = ontoService
              .getCellTypeOntology(speciesId, null);
        Ontology<Sex, String> sexOntology = ontoService.getSexOntology(speciesId, 
                Set.of(ConditionDAO.SEX_ROOT_ID), false, true);
        Ontology<Strain, String> strainOntology = ontoService.getStrainOntology(speciesId,
                Set.of(ConditionDAO.STRAIN_ROOT_ID), false, true);
        long endTime = System.currentTimeMillis();
        log.info("Time taken to retrieve ontologies: {}", endTime - startTime);


        List<Long> rawDataTimes = new ArrayList<>();
        List<Long> condGraphTimes = new ArrayList<>();
        List<Long> propagationTimes = new ArrayList<>();

        List<Long> rawDataMappingTimes = new ArrayList<>();
        List<Long> dfsOrderingTimes = new ArrayList<>();
        List<Long> callOrderingTimes = new ArrayList<>();

        List<Double> averageRetrievingChildCondTime = new ArrayList<>();
        int averageRetrievingChildCondTimeCound = 0;
        List<Double> averageDataPreparationTimes = new ArrayList<>();
        int averageDataPreparationTimeCount = 0;
        List<Double> avgLoadExpressionCallTimes = new ArrayList<>();
        int avgLoadExpressionCallTimeCount = 0;
        List<Double> avgSelfCallComputeTimes = new ArrayList<>();
        int avgSelfCallComputeTimeCount = 0;
        List<Double> avgAllChildrenComputeTimes = new ArrayList<>();
        int avgAllChildrenComputeTimeCount = 0;
        List<Double> avgFinishingBusinessTimes = new ArrayList<>();
        int avgFinishingBusinessTimeCount = 0;
        List<Double> avgBestDescentComputeTimes = new ArrayList<>();
        int avgBestDescentComputeTimeCount = 0;
        List<Double> avgExpressionScoreComputeTimes = new ArrayList<>();
        int avgExpressionScoreComputeTimeCount = 0;
        List<Double> avgFdrComputeTimes = new ArrayList<>();
        int avgFdrComputeTimeCount = 0;
        List<Double> avgMedianComputeTimes = new ArrayList<>();
        int avgMedianComputeTimeCount = 0;
        for (Gene gene: genes) {
            OTFExpressionCallLoader loader = new OTFExpressionCallLoader(this.getServiceFactory());

            log.info("Start retrieval of raw data for gene: {}", gene.getGeneId());
            long rawDataStartTime = System.currentTimeMillis();
            //Retrieve raw data
            RawDataFilter filter = new RawDataFilter(Set.of(new GeneFilter(speciesId, gene.getGeneId())),
                    null);
            
            //XXX: we need rank for each call and rank max for each assay.
            //FIXME: in some conditions, all calls are excluded. They should be filtered out
            //before identifying the conditions to subset.
            Map<RawDataDataType<?, ?>, RawDataContainer<?, ?>> dataTypeToContainer = OnTheFlyPropagation
                    .loadRawDataPerDatatype(dataTypes, filter, rawDataService);
            Set<RawDataCondition> rawDataConditions = transformDataTypeToContainersToRawDataCondition(dataTypeToContainer);
            log.info("Number of RawDataConditions: {}", rawDataConditions.size());

            //Transform raw data conditions to conditions.
            //XXX: we retrieve condition for all condition parameters
            Map<RawDataCondition, Condition> rawDataConditionToCondition = transformRawCondToCondMap(rawDataConditions,
                    CallService.Attribute.getAllConditionParameters());
            long rawDataEndTime = System.currentTimeMillis();
            log.info("Time for retrieving raw data: {}ms", rawDataEndTime - rawDataStartTime);
            rawDataTimes.add(rawDataEndTime - rawDataStartTime);
            
            //Instantiate new ConditionGraph from conditions and ontologies
            log.info("Start ConditionGraph generation");
            long condGraphStartTime = System.currentTimeMillis();
            log.info("Number of conditions: {}",
                    rawDataConditionToCondition.values().stream().distinct().count());
            Set<Condition> allConds = new HashSet<>(rawDataConditionToCondition.values());
            AnatEntity rootAnatEntity = new AnatEntity(ConditionDAO.ANAT_ENTITY_ROOT_ID);
            DevStage rootDevStage = new DevStage(ConditionDAO.DEV_STAGE_ROOT_ID);
            AnatEntity rootCellType = new AnatEntity(ConditionDAO.CELL_TYPE_ROOT_ID);
            Sex rootSex = new Sex(ConditionDAO.SEX_ROOT_ID);
            Strain rootStrain = new Strain(ConditionDAO.STRAIN_ROOT_ID);
            allConds.add(new Condition(
                    rootAnatEntity,
                    rootDevStage,
                    rootCellType,
                    rootSex,
                    rootStrain,
                    gene.getSpecies()));
            ConditionGraph condGraph = condGraphService.loadConditionGraph(allConds, false, false,
                    anatEntityOntology, stageOntology, celltypeOntology, sexOntology, strainOntology);
            long condGraphEndTime = System.currentTimeMillis();
            log.info("ConditionGraph generation time: {}ms", condGraphEndTime - condGraphStartTime);
            condGraphTimes.add(condGraphEndTime - condGraphStartTime);

            log.info("Start propagation");
            long otfStartTime = System.currentTimeMillis();
            List<OTFExpressionCall> calls = loader.loadOTFExpressionCalls(gene, condGraph,
                    rawDataConditionToCondition, dataTypeToContainer);
            long otfEndTime = System.currentTimeMillis();
            log.info("Propagation time: {}ms; number of OTF calls: {}", otfEndTime - otfStartTime, calls.size());
            propagationTimes.add(otfEndTime - otfStartTime);

            rawDataMappingTimes.add(loader.rawDataMappingTime);
            dfsOrderingTimes.add(loader.dfsOrderingTime);
            callOrderingTimes.add(loader.callOrderingTime);

            averageRetrievingChildCondTime.add(loader.retrievingChildCondTimes.stream()
                    .mapToLong(Long::longValue)
                    .average()
                    .orElse(0.0));
            averageRetrievingChildCondTimeCound += loader.retrievingChildCondTimes.size();
            averageDataPreparationTimes.add(loader.dataPreparationTimes.stream()
                    .mapToLong(Long::longValue)
                    .average()
                    .orElse(0.0));
            averageDataPreparationTimeCount += loader.dataPreparationTimes.size();

            avgLoadExpressionCallTimes.add(loader.loadExpressionCallTimes.stream()
                    .mapToLong(Long::longValue)
                    .average()
                    .orElse(0.0));
            avgLoadExpressionCallTimeCount += loader.loadExpressionCallTimes.size();
            avgSelfCallComputeTimes.add(loader.selfCallComputeTimes.stream()
                    .mapToLong(Long::longValue)
                    .average()
                    .orElse(0.0));
            avgSelfCallComputeTimeCount += loader.selfCallComputeTimes.size();
            avgAllChildrenComputeTimes.add(loader.allChildrenComputeTimes.stream()
                    .mapToLong(Long::longValue)
                    .average()
                    .orElse(0.0));
            avgAllChildrenComputeTimeCount += loader.allChildrenComputeTimes.size();
            avgFinishingBusinessTimes.add(loader.finishingBusinessTimes.stream()
                    .mapToLong(Long::longValue)
                    .average()
                    .orElse(0.0));
            avgFinishingBusinessTimeCount += loader.finishingBusinessTimes.size();

            avgBestDescentComputeTimes.add(loader.bestDescentComputeTimes.stream()
                    .mapToLong(Long::longValue)
                    .average()
                    .orElse(0.0));
            avgBestDescentComputeTimeCount += loader.bestDescentComputeTimes.size();
            avgExpressionScoreComputeTimes.add(loader.expressionScoreComputeTimes.stream()
                    .mapToLong(Long::longValue)
                    .average()
                    .orElse(0.0));
            avgExpressionScoreComputeTimeCount += loader.expressionScoreComputeTimes.size();
            avgFdrComputeTimes.add(loader.fdrComputeTimes.stream()
                    .mapToLong(Long::longValue)
                    .average()
                    .orElse(0.0));
            avgFdrComputeTimeCount += loader.fdrComputeTimes.size();
            avgMedianComputeTimes.add(loader.medianComputeTimes.stream()
                    .mapToLong(Long::longValue)
                    .average()
                    .orElse(0.0));
            avgMedianComputeTimeCount += loader.medianComputeTimes.size();
            
            List<OTFExpressionCall> filteredCalls = calls.stream()
                    .filter(c -> c.getCondition().getDevStage().equals(rootDevStage) &&
                            c.getCondition().getSex().equals(rootSex) &&
                            c.getCondition().getStrain().equals(rootStrain))
                    .collect(Collectors.toList());
            filteredCalls.stream().limit(20L).forEach(c -> System.out.println("Call: AnatEntity: "
                    + c.getCondition().getAnatEntityId() + " - " + c.getCondition().getAnatEntity().getName()
                    + "; CellType: " + c.getCondition().getCellTypeId() + " - " + c.getCondition().getCellType().getName()
                    + "; expressionScore: " + c.getExpressionScore() + "; bestDescendantExpressionScore: "
                    + c.getBestDescendantExpressionScore() + "; pValue: "
                    + c.getAllDataTypePValue() + "; bestDescendantPValue: "
                    + c.getBestDescendantAllDataTypePValue() + "; supportingDataType: "
                    + c.getSupportingDataTypes()));
            
        }
        this.getServiceFactory().close();

        log.info("Average time for retrieving raw data: {}ms - done {} times - Approximate time spent: {} ms",
                rawDataTimes.stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0.0), rawDataTimes.size(),
                rawDataTimes.stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0.0) * rawDataTimes.size());
        log.info("Average time for producing condition graph: {}ms - done {} times - Approximate time spent: {} ms",
                condGraphTimes.stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0.0), condGraphTimes.size(),
                condGraphTimes.stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0.0) * condGraphTimes.size());
        log.info("Average time for overall propagated call computations: {}ms - done {} times - Approximate time spent: {} ms",
                propagationTimes.stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0.0), propagationTimes.size(),
                propagationTimes.stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0.0) * propagationTimes.size());

        log.info("Average time for raw data mapping to conds: {}ms - done {} times - Approximate time spent: {} ms",
                rawDataMappingTimes.stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0.0), rawDataMappingTimes.size(),
                rawDataMappingTimes.stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0.0) * rawDataMappingTimes.size());
        log.info("Average time for DFS ordering: {}ms - done {} times - Approximate time spent: {} ms",
                dfsOrderingTimes.stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0.0), dfsOrderingTimes.size(),
                dfsOrderingTimes.stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0.0) * dfsOrderingTimes.size());
        log.info("Average time for ordering calls per expression score: {}ms - done {} times - Approximate time spent: {} ms",
                callOrderingTimes.stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0.0), callOrderingTimes.size(),
                callOrderingTimes.stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0.0) * callOrderingTimes.size());

        log.info("Average time for retrieving child conds: {}ms - done {} times - Approximate time spent: {} ms",
                averageRetrievingChildCondTime.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0), averageRetrievingChildCondTimeCound,
                averageRetrievingChildCondTime.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0) * averageRetrievingChildCondTimeCound);
        log.info("Average time for retrieving raw data mapped to cond and calls mapped to child conds: {}ms - done {} times - Approximate time spent: {} ms",
                averageDataPreparationTimes.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0), averageDataPreparationTimeCount,
                averageDataPreparationTimes.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0) * averageDataPreparationTimeCount);

        log.info("Average time for loading individual calls: {}ms - done {} times - Approximate time spent: {} ms",
                avgLoadExpressionCallTimes.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0), avgLoadExpressionCallTimeCount,
                avgLoadExpressionCallTimes.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0) * avgLoadExpressionCallTimeCount);
        log.info("Average time for computing self call info: {}ms - done {} times - Approximate time spent: {} ms",
                avgSelfCallComputeTimes.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0), avgSelfCallComputeTimeCount,
                avgSelfCallComputeTimes.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0) * avgSelfCallComputeTimeCount);
        log.info("Average time for computing overall info from child calls: {}ms - done {} times - Approximate time spent: {} ms",
                avgAllChildrenComputeTimes.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0), avgAllChildrenComputeTimeCount,
                avgAllChildrenComputeTimes.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0) * avgAllChildrenComputeTimeCount);
        log.info("Average time for finishing business for individual calls: {}ms - done {} times - Approximate time spent: {} ms",
                avgFinishingBusinessTimes.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0), avgFinishingBusinessTimeCount,
                avgFinishingBusinessTimes.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0) * avgFinishingBusinessTimeCount);

        log.info("Average time for retrieving best descendant info from child calls: {}ms - done {} times - Approximate time spent: {} ms",
                avgBestDescentComputeTimes.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0), avgBestDescentComputeTimeCount,
                avgBestDescentComputeTimes.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0) * avgBestDescentComputeTimeCount);
        log.info("Average time for computing expression scores: {}ms - done {} times - Approximate time spent: {} ms",
                avgExpressionScoreComputeTimes.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0), avgExpressionScoreComputeTimeCount,
                avgExpressionScoreComputeTimes.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0) * avgExpressionScoreComputeTimeCount);
        log.info("Average time for computing expression FDRs: {}ms - done {} times - Approximate time spent: {} ms",
                avgFdrComputeTimes.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0), avgFdrComputeTimeCount,
                avgFdrComputeTimes.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0) * avgFdrComputeTimeCount);
        log.info("Average time for computing expression medians: {}ms - done {} times - Approximate time spent: {} ms",
                avgMedianComputeTimes.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0), avgMedianComputeTimeCount,
                avgMedianComputeTimes.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0) * avgMedianComputeTimeCount);
        log.traceExit();
    }

    private static Map<RawDataCondition, Condition>
        transformRawCondToCondMap(Set<RawDataCondition> rawDataConditions,
                Collection<CallService.Attribute> condParameters) {
        log.traceEntry("{}, {}", rawDataConditions, condParameters);
        return log.traceExit(rawDataConditions.stream()
                .collect(Collectors.toMap(c -> c, c -> mapRawDataConditionToCondition(c))));

    }
    
    private static Map<RawDataDataType<?, ?>, RawDataContainer<?, ?>>
        loadRawDataPerDatatype(Set<DataType> dataTypes, RawDataFilter filter,
                RawDataService rawDataService) {
        log.traceEntry("{}, {}", dataTypes, filter, rawDataService);
        RawDataLoader loader = rawDataService.loadRawDataLoader(filter);
        return log.traceExit(dataTypes.stream().map(dt -> {
            RawDataDataType<?, ?> rawDataDataType = RawDataDataType.getRawDataDataType(dt);
            RawDataContainer<?, ?> container = loader.loadData(InformationType.CALL,
                    rawDataDataType, null, null);
            return Map.entry(RawDataDataType.getRawDataDataType(dt), container);
        }).collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue())));
    }

    private static Set<RawDataCondition> transformDataTypeToContainersToRawDataCondition(Map<RawDataDataType<?, ?>, RawDataContainer<?, ?>> dataTypeToContainer) {
        log.traceEntry("{}", dataTypeToContainer);
        return log.traceExit(dataTypeToContainer.values().stream()
                .flatMap(e -> e.getAssays().stream()
                        .map(a -> a.getAnnotation().getRawDataCondition()))
                .collect(Collectors.toSet()));
    }
}
