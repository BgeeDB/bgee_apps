package org.bgee.model.expressiondata.call;

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
        
        // init services
        OntologyService ontoService = this.getServiceFactory().getOntologyService();
        RawDataService rawDataService = this.getServiceFactory().getRawDataService();
        GeneService geneService = this.getServiceFactory().getGeneService();
        ConditionGraphService condGraphService = this.getServiceFactory().getConditionGraphService();
        
        // init objects used for this test
        int speciesId = 9606;
        Set<String> geneIds = Set.of("ENSG00000163914", "ENSG00000007372", "ENSG00000012048", "ENSG00000065361");
//        ,
//                "ENSG00000000005", "ENSG00000000419", "ENSG00000000457", "ENSG00000000460",
//                "ENSG00000000938", "ENSG00000000971", "ENSG00000001036", "ENSG00000001084",
//                "ENSG00000001167", "ENSG00000001460", "ENSG00000001461", "ENSG00000001497",
//                "ENSG00000001561", "ENSG00000001617", "ENSG00000001626", "ENSG00000001629",
//                "ENSG00000001630", "ENSG00000001631", "ENSG00000002016", "ENSG00000002079",
//                "ENSG00000002330", "ENSG00000002549", "ENSG00000002586", "ENSG00000002587",
//                "ENSG00000002726", "ENSG00000002745", "ENSG00000002746", "ENSG00000002822",
//                "ENSG00000002834");
        EnumSet<DataType> dataTypes = EnumSet.of(DataType.AFFYMETRIX, DataType.RNA_SEQ,
                DataType.SC_RNA_SEQ);
        
        Set<Gene> genes = geneService.loadGenes(new GeneFilter(speciesId, geneIds))
                .collect(Collectors.toSet());

        //Retrieve ontologies once for all genes
        log.info("Start retrieval of ontologies");
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

        OTFExpressionCallLoader loader = new OTFExpressionCallLoader(this.getServiceFactory());

        for (Gene gene: genes) {

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

            log.info("Start propagation");
            long otfStartTime = System.currentTimeMillis();
            List<OTFExpressionCall> calls = loader.loadOTFExpressionCalls(gene, condGraph,
                    rawDataConditionToCondition, dataTypeToContainer);
            long otfEndTime = System.currentTimeMillis();
            log.info("Propagation time: {}ms; number of OTF calls: {}", otfEndTime - otfStartTime, calls.size());
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
