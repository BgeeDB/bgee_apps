package org.bgee.pipeline.expression.downloadfile.collaboration;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.ServiceFactory;
import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.anatdev.AnatEntityService;
import org.bgee.model.anatdev.DevStage;
import org.bgee.model.anatdev.DevStageService;
import org.bgee.model.dao.api.ontologycommon.RelationDAO.RelationTO;
import org.bgee.model.expressiondata.Call.ExpressionCall;
import org.bgee.model.expressiondata.CallData.ExpressionCallData;
import org.bgee.model.expressiondata.baseelements.DataPropagation;
import org.bgee.model.expressiondata.baseelements.ExpressionLevelInfo;
import org.bgee.model.expressiondata.baseelements.SummaryQuality;
import org.bgee.model.expressiondata.baseelements.SummaryCallType.ExpressionSummary;
import org.bgee.model.expressiondata.CallService;
import org.bgee.model.expressiondata.Condition;
import org.bgee.model.gene.Gene;
import org.bgee.model.gene.GeneBioType;
import org.bgee.model.gene.GeneService;
import org.bgee.model.ontology.Ontology;
import org.bgee.model.ontology.OntologyService;
import org.bgee.model.ontology.RelationType;
import org.bgee.model.species.Species;
import org.bgee.model.species.SpeciesService;
import org.bgee.pipeline.TestAncestor;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
* Unit tests for {@link GenerateOncoMXFile}.
*
* @author  Frederic Bastian
* @since Bgee 14 Feb. 2019
* @version Bgee 14 Feb. 2019
*/
public class GenerateOncoMXFileTest extends TestAncestor {
    private final static Logger log = LogManager.getLogger(GenerateOncoMXFileTest.class.getName());
    @Override
    protected Logger getLogger() {
        return log;
    }

    private static final String UBERON_TERM_FILE = "/oncomx/human_doid_slim_uberon_mapping.csv";

    @Rule
    public final TemporaryFolder testFolder = new TemporaryFolder();


    /**
     * Test {@link GenerateOncoMXFile#generateFile(int, Collection, String, String)}.
     */
    @Test
    public void shouldGenerateOncoMXFile() throws IOException {
        //*****************************************
        // MOCK CREATION
        //*****************************************
        //*** Mock services ***
        ServiceFactory serviceFactory = mock(ServiceFactory.class);
        SpeciesService speciesService = mock(SpeciesService.class);
        when(serviceFactory.getSpeciesService()).thenReturn(speciesService);
        OntologyService ontologyService = mock(OntologyService.class);
        when(serviceFactory.getOntologyService()).thenReturn(ontologyService);
//        AnatEntityService anatEntityService = mock(AnatEntityService.class);
//        when(serviceFactory.getAnatEntityService()).thenReturn(anatEntityService);
//        DevStageService devStageService = mock(DevStageService.class);
//        when(serviceFactory.getDevStageService()).thenReturn(devStageService);
//        GeneService geneService = mock(GeneService.class);
//        when(serviceFactory.getGeneService()).thenReturn(geneService);
        CallService callService = mock(CallService.class);
        when(serviceFactory.getCallService()).thenReturn(callService);

        Ontology<AnatEntity, String> anatEntOnt = mock(Ontology.class);
        Ontology<DevStage, String> devStageOnt = mock(Ontology.class);

        //*** Create objects returned by the mock services ***
        Species human = new Species(9606, "human", "uberHuman", "Homo", "sapiens",
                "whateverGenome", 9605);
        Species mouse = new Species(10090, "mouse", "uberMouse", "Mus", "musculus",
                "whateverGenome", 10089);

        //Ids need to be provided in alphabetical order for proper ordering of calls returned
        List<String> requestedUberonIds = Arrays.asList("UBERON:0000002", "UBERON:0000007",
                "UBERON:0002369");
        AnatEntity anatEntity1 = new AnatEntity(requestedUberonIds.get(0), "anatEntity1Name", "anat1");
        AnatEntity anatEntity2 = new AnatEntity(requestedUberonIds.get(1), "anatEntity2Name", "anat2");
        AnatEntity anatEntity3 = new AnatEntity(requestedUberonIds.get(2), "anatEntity3Name", "anat3");
        //We will consider this term as a descendant of a valid Uberon term
        //in human_doid_slim_uberon_mapping.csv, so that it will be valid as well
        AnatEntity anatEntity4 = new AnatEntity("child1", "anatEntity4Name", "anat4");
        Set<String> validAnatEntityIds = new HashSet<>(Arrays.asList(requestedUberonIds.get(0),
                requestedUberonIds.get(1), requestedUberonIds.get(2), anatEntity4.getId()));

        List<String> requestedStageIds = Arrays.asList("devStage1", "devStage2");
        DevStage devStage1 = new DevStage(requestedStageIds.get(0), "devStage1Name", "stage1");
        DevStage devStage2 = new DevStage(requestedStageIds.get(1), "devStage2Name", "stage2");
        //We will consider this term as a descendant of a valid requested stage,
        //so that it will be valid as well
        DevStage devStage3 = new DevStage("stageChild1", "devStage3Name", "stage3");
        Set<String> validDevStageIds = new HashSet<>(Arrays.asList(requestedStageIds.get(0),
                requestedStageIds.get(1), devStage3.getId()));

        // *** Configure the calls to the mock services ***
        when(speciesService.loadSpeciesByIds(Collections.singleton(9606), false))
            .thenReturn(Collections.singleton(human));

        when(ontologyService.getAnatEntityOntology(9606, new HashSet<>(requestedUberonIds),
                EnumSet.of(RelationType.ISA_PARTOF), false, true))
            .thenReturn(anatEntOnt);
        when(anatEntOnt.getElement(requestedUberonIds.get(0))).thenReturn(anatEntity1);
        when(anatEntOnt.getDescendants(anatEntity1)).thenReturn(new HashSet<>());
        when(anatEntOnt.getElement(requestedUberonIds.get(1))).thenReturn(anatEntity2);
        when(anatEntOnt.getDescendants(anatEntity2)).thenReturn(new HashSet<>());
        when(anatEntOnt.getElement(requestedUberonIds.get(2))).thenReturn(anatEntity3);
        when(anatEntOnt.getDescendants(anatEntity3)).thenReturn(new HashSet<>(
                Arrays.asList(anatEntity4)));
        when(anatEntOnt.getElement(anatEntity4.getId())).thenReturn(anatEntity4);
        when(anatEntOnt.getDescendants(anatEntity4)).thenReturn(new HashSet<>());


        when(ontologyService.getDevStageOntology(9606, new HashSet<>(requestedStageIds),
                false, true)).thenReturn(devStageOnt);
        when(devStageOnt.getElement(requestedStageIds.get(0))).thenReturn(devStage1);
        when(devStageOnt.getDescendants(devStage1)).thenReturn(new HashSet<>());
        when(devStageOnt.getElement(requestedStageIds.get(1))).thenReturn(devStage2);
        when(devStageOnt.getDescendants(devStage2)).thenReturn(new HashSet<>(Arrays.asList(devStage3)));
        when(devStageOnt.getElement(devStage3.getId())).thenReturn(devStage3);
        when(devStageOnt.getDescendants(devStage3)).thenReturn(new HashSet<>());


        Condition cond1_1 = new Condition(anatEntity1, devStage1, human);
        Condition cond1_2 = new Condition(anatEntity1, devStage2, human);
        Condition cond1_3 = new Condition(anatEntity1, devStage3, human);
        Condition cond2_1 = new Condition(anatEntity2, devStage1, human);
        Condition cond2_2 = new Condition(anatEntity2, devStage2, human);
        Condition cond2_3 = new Condition(anatEntity2, devStage3, human);
        Condition cond3_1 = new Condition(anatEntity3, devStage1, human);
        Condition cond3_2 = new Condition(anatEntity3, devStage2, human);
        Condition cond3_3 = new Condition(anatEntity3, devStage3, human);
        Condition cond4_1 = new Condition(anatEntity4, devStage1, human);
        Condition cond4_2 = new Condition(anatEntity4, devStage2, human);
        Condition cond4_3 = new Condition(anatEntity4, devStage3, human);
        Gene gene1 = new Gene("gene1", "geneName1", "g1", human, new GeneBioType("biotype1"), 1);
        Gene gene2 = new Gene("gene2", "geneName2", "g2", human, new GeneBioType("biotype1"), 1);
        Gene gene3 = new Gene("gene3", "geneName3", "g3", human, new GeneBioType("biotype1"), 1);
        //*** Anat. entity calls ***
        //First, an organ with 3 genes expressed, at different stage,
        //including invalid stages, because we still use them to retrieve min/max ranks
        //per organ
        BigDecimal maxRank = new BigDecimal("10000");
        ExpressionCall anatEntity1Call1 = new ExpressionCall(gene1, cond1_1, null, 
                null, null, 
                null, new ExpressionLevelInfo(new BigDecimal("10.1")));
        ExpressionCall anatEntity1Call2 = new ExpressionCall(gene2, cond1_2, null, 
                null, null, 
                null, new ExpressionLevelInfo(new BigDecimal("200")));
        ExpressionCall anatEntity1Call3 = new ExpressionCall(gene3, cond1_3, null, 
                null, null, 
                null, new ExpressionLevelInfo(new BigDecimal("300")));
        //Now, an organ with only one call
        ExpressionCall anatEntity2Call1 = new ExpressionCall(gene1, cond2_1, null, 
                null, null, 
                null, new ExpressionLevelInfo(new BigDecimal("1")));
        //An organ with 2 genes expressed
        ExpressionCall anatEntity3Call1 = new ExpressionCall(gene1, cond3_1, null, 
                null, null, 
                null, new ExpressionLevelInfo(new BigDecimal("1")));
        ExpressionCall anatEntity3Call2 = new ExpressionCall(gene2, cond3_3, null, 
                null, null, 
                null, new ExpressionLevelInfo(new BigDecimal("1000")));
        List<ExpressionCall> anatEntityCalls = Arrays.asList(anatEntity1Call1,
                anatEntity1Call2, anatEntity1Call3,
                anatEntity2Call1,
                anatEntity3Call1, anatEntity3Call2);
        when(callService.loadExpressionCalls(
                GenerateOncoMXFile.getAnatEntityCallFilter(9606, validAnatEntityIds),
                GenerateOncoMXFile.getAnatEntityCallAttributes(),
                GenerateOncoMXFile.getAnatEntityServiceOrdering()
        )).thenReturn(anatEntityCalls.stream());

        //*** Gene calls ***
        ExpressionCall gene1Call1 = new ExpressionCall(gene1, cond1_1, null, 
                ExpressionSummary.EXPRESSED, SummaryQuality.BRONZE, 
                null, new ExpressionLevelInfo(new BigDecimal("10.1")));
        ExpressionCall gene1Call2 = new ExpressionCall(gene1, cond2_1, null, 
                ExpressionSummary.EXPRESSED, SummaryQuality.BRONZE, 
                null, new ExpressionLevelInfo(new BigDecimal("1")));
        ExpressionCall gene1Call3 = new ExpressionCall(gene1, cond3_1, null, 
                ExpressionSummary.EXPRESSED, SummaryQuality.BRONZE, 
                null, new ExpressionLevelInfo(new BigDecimal("1")));
        //not expressed call, should not have been seen in the anat. entity calls
        ExpressionCall gene1Call4 = new ExpressionCall(gene1, cond3_2, null, 
                ExpressionSummary.NOT_EXPRESSED, SummaryQuality.SILVER, 
                null, new ExpressionLevelInfo(maxRank));
        ExpressionCall gene2Call1 = new ExpressionCall(gene1, cond1_2, null, 
                ExpressionSummary.EXPRESSED, SummaryQuality.BRONZE, 
                null, new ExpressionLevelInfo(new BigDecimal("200")));
        ExpressionCall gene2Call2 = new ExpressionCall(gene1, cond3_3, null, 
                ExpressionSummary.EXPRESSED, SummaryQuality.BRONZE, 
                null, new ExpressionLevelInfo(new BigDecimal("1000")));
        ExpressionCall gene3Call1 = new ExpressionCall(gene3, cond1_3, null, 
                ExpressionSummary.EXPRESSED, SummaryQuality.BRONZE, 
                null, new ExpressionLevelInfo(new BigDecimal("300")));
        List<ExpressionCall> geneCalls = Arrays.asList(gene1Call1, gene1Call2,
                gene1Call3, gene1Call4,
                gene2Call1, gene2Call2,
                gene3Call1);
        when(callService.loadExpressionCalls(
                GenerateOncoMXFile.getGeneCallFilter(9606),
                GenerateOncoMXFile.getGeneCallAttributes(),
                GenerateOncoMXFile.getGeneServiceOrdering()
        )).thenReturn(geneCalls.stream());

        //*****************************************
        // ACTUAL TEST
        //*****************************************
        File createdFolder= testFolder.newFolder("subfolder");
        GenerateOncoMXFile generateFileTest = new GenerateOncoMXFile(serviceFactory);
        generateFileTest.generateFile(9606, requestedStageIds,
                getClass().getResource(UBERON_TERM_FILE).getFile(),
                createdFolder.getPath());
    }
}
