package org.bgee.model.expressiondata.call;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

public class OnTheFlyPropagation {

    private final static Logger log = LogManager.getLogger(OnTheFlyPropagation.class.getName());
    
    public static void main(String[] args) {
        
        // init services
        ServiceFactory serviceFactory = new ServiceFactory();
        OntologyService ontoService = serviceFactory.getOntologyService();
        RawDataService rawDataService = serviceFactory.getRawDataService();
        GeneService geneService = serviceFactory.getGeneService();
        ConditionGraphService condGraphService = serviceFactory.getConditionGraphService();
        
        // init objects used for this test
        int speciesId = 9606;
        Set<String> geneIds = Set.of("ENSG00000000003");
//        ,
//                "ENSG00000000005", "ENSG00000000419", "ENSG00000000457", "ENSG00000000460",
//                "ENSG00000000938", "ENSG00000000971", "ENSG00000001036", "ENSG00000001084",
//                "ENSG00000001167", "ENSG00000001460", "ENSG00000001461", "ENSG00000001497",
//                "ENSG00000001561", "ENSG00000001617", "ENSG00000001626", "ENSG00000001629",
//                "ENSG00000001630", "ENSG00000001631", "ENSG00000002016", "ENSG00000002079",
//                "ENSG00000002330", "ENSG00000002549", "ENSG00000002586", "ENSG00000002587",
//                "ENSG00000002726", "ENSG00000002745", "ENSG00000002746", "ENSG00000002822",
//                "ENSG00000002834");
        EnumSet<DataType> dataTypes = EnumSet.of(DataType.RNA_SEQ, DataType.AFFYMETRIX,
                DataType.SC_RNA_SEQ);
        
        Set<Gene> genes = geneService.loadGenes(new GeneFilter(speciesId, geneIds))
                .collect(Collectors.toSet());

        //Retrieve ontologies once for all genes

        Ontology<DevStage, String> stageOntology = ontoService
                .getDevStageOntology(speciesId, null);
        log.debug("stage {}", stageOntology.getElement("UBERON:0000104"));
      Ontology<AnatEntity, String> anatEntityOntology = ontoService
              .getAnatEntityOntology(speciesId, null);
      Ontology<AnatEntity, String> celltypeOntology = ontoService
              .getCellTypeOntology(speciesId, null);
        Ontology<Sex, String> sexOntology = ontoService.getSexOntology(speciesId, 
                Set.of(ConditionDAO.SEX_ROOT_ID), false, true);
        Ontology<Strain, String> strainOntology = ontoService.getStrainOntology(speciesId,
                Set.of(ConditionDAO.STRAIN_ROOT_ID), false, true);

        for (Gene gene: genes) {
            
            //Retrieve raw data
            RawDataFilter filter = new RawDataFilter(Set.of(new GeneFilter(speciesId, gene.getGeneId())),
                    null);
            
            //XXX: we need rank for each call and rank max for each assay.
            Map<RawDataDataType<?, ?>, RawDataContainer<?, ?>> dataTypeToContainer = OnTheFlyPropagation
                    .loadRawDataPerDatatype(dataTypes, filter, rawDataService);
            Set<RawDataCondition> rawDataCondition = transformDataTypeToContainersToRawDataCondition(dataTypeToContainer);

            //Transform raw data conditions to conditions.
            //XXX: we retrieve condition for all condition parameters
            Map<RawDataCondition, Condition> rawDataConditionToCondition = transformRawCondToCondMap(rawDataCondition,
                    CallService.Attribute.getAllConditionParameters());
            
            //Instantiate new ConditionGraph from conditions and ontologies
            ConditionGraph condGraph = condGraphService.loadConditionGraph(rawDataConditionToCondition.values(), true, false,
                    anatEntityOntology, stageOntology, celltypeOntology, sexOntology, strainOntology);
            
            OTFExpressionCallLoader loader = new OTFExpressionCallLoader(serviceFactory);
            List<OTFExpressionCall> calls = loader.loadOTFExpressionCalls(gene, condGraph,
                    rawDataConditionToCondition, dataTypeToContainer);
            log.info("YOUHOU: {}", calls.get(0));
            
        }
        serviceFactory.close();
    }

    private static Map<RawDataCondition, Condition>
        transformRawCondToCondMap(Set<RawDataCondition> rawDataConditions,
                Collection<CallService.Attribute> condParameters) {
        log.traceEntry("{}, {}", rawDataConditions, condParameters);
        return log.traceExit(rawDataConditions.stream()
                .collect(Collectors.toMap(c -> c, c -> c.toCondition(condParameters))));

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
        .map(e -> e.getAssays().stream().map(a -> a.getAnnotation().getRawDataCondition()))
        .flatMap(s -> s).collect(Collectors.toSet()));
    }
}
