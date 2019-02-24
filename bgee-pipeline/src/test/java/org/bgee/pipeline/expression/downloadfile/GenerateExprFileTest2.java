package org.bgee.pipeline.expression.downloadfile;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.Service;
import org.bgee.model.ServiceFactory;
import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.anatdev.AnatEntityService;
import org.bgee.model.anatdev.DevStage;
import org.bgee.model.dao.api.expressiondata.CallDAO.CallTO.DataState;
import org.bgee.model.expressiondata.Call.ExpressionCall;
import org.bgee.model.expressiondata.CallData.ExpressionCallData;
import org.bgee.model.expressiondata.CallFilter.ExpressionCallFilter;
import org.bgee.model.expressiondata.CallService;
import org.bgee.model.expressiondata.CallService.Attribute;
import org.bgee.model.expressiondata.Condition;
import org.bgee.model.expressiondata.baseelements.CallType;
import org.bgee.model.expressiondata.baseelements.CallType.Expression;
import org.bgee.model.expressiondata.baseelements.DataPropagation;
import org.bgee.model.expressiondata.baseelements.DataQuality;
import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.model.expressiondata.baseelements.ExperimentExpressionCount;
import org.bgee.model.expressiondata.baseelements.PropagationState;
import org.bgee.model.expressiondata.baseelements.SummaryCallType.ExpressionSummary;
import org.bgee.model.expressiondata.baseelements.SummaryQuality;
import org.bgee.model.gene.Gene;
import org.bgee.model.gene.GeneBioType;
import org.bgee.model.gene.GeneFilter;
import org.bgee.model.species.Species;
import org.bgee.model.species.SpeciesService;
import org.bgee.pipeline.expression.downloadfile.GenerateDownloadFile.ObservedData;
import org.bgee.pipeline.expression.downloadfile.GenerateExprFile2.SingleSpExprFileType2;
import org.junit.Test;
import org.supercsv.cellprocessor.constraint.IsElementOf;
import org.supercsv.cellprocessor.constraint.LMinMax;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.constraint.StrNotNullOrEmpty;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvListReader;
import org.supercsv.io.ICsvListReader;
import org.supercsv.prefs.CsvPreference;

/**
 * Unit tests for {@link GenerateExprFile2}.
 * 
 * @author  Valentine Rech de Laval
 * @version Bgee 14, Mar. 2017
 * @since   Bgee 13
 */
//FIXME: to reactivate?
public class GenerateExprFileTest2 extends GenerateDownloadFileTest {

    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = LogManager.getLogger(GenerateExprFileTest2.class.getName());

    public GenerateExprFileTest2(){
        super();
    }

    @Override
    protected Logger getLogger() {
        return log;
    }
    
    
    /**
     * Test method {@link GenerateExprFile2#generateExprFiles()}.
     */
    @Test
    public void shouldGenerateExprFiles() throws IOException {

        // First, we need a mock MySQLDAOManager, for the class to acquire mock DAOs. 
        MockDAOManager mockManager = new MockDAOManager();
        ServiceFactory serviceFactory = mock(ServiceFactory.class);
        CallService callService = mock(CallService.class);
        when(serviceFactory.getCallService()).thenReturn(callService);
        SpeciesService speciesService = mock(SpeciesService.class);
        when(serviceFactory.getSpeciesService()).thenReturn(speciesService);
        AnatEntityService anatEntityService = mock(AnatEntityService.class);
        when(serviceFactory.getAnatEntityService()).thenReturn(anatEntityService);
        
        Set<Integer> speciesIds = new HashSet<>(Arrays.asList(11, 22));
        
        // Mock the load of species
        when(speciesService.loadSpeciesByIds(speciesIds, false)).thenReturn(
                new HashSet<>(Arrays.asList(
                        new Species(11, null, null, "Genus11", "spName1", null, null),
                        new Species(22, null, null, "Genus22", "spName2", null, null))));
        
        // Mock the load of non informative anatomical entities
        when(anatEntityService.loadNonInformativeAnatEntitiesBySpeciesIds(Collections.singleton(11)))
        .thenReturn(Arrays.asList(new AnatEntity("NonInfoAnatEnt1")).stream());
        
        when(anatEntityService.loadNonInformativeAnatEntitiesBySpeciesIds(Collections.singleton(22)))
        .thenReturn(Arrays.asList(new AnatEntity("NonInfoAnatEnt2")).stream());
        
        ExpressionCallFilter callFilterSp11 = getCallFilter(Collections.singleton(new GeneFilter(11)));
        ExpressionCallFilter callFilterSp22 = getCallFilter(Collections.singleton(new GeneFilter(22)));

        LinkedHashMap<CallService.OrderingAttribute, Service.Direction> serviceOrdering = 
                new LinkedHashMap<>();
        //The ordering by gene ID is essential here, because we will load into memory 
        //all data from one gene at a time, for clustering and redundancy discovery. 
        //The ordering by rank is not mandatory, for a given gene we are going to reorder anyway
        serviceOrdering.put(CallService.OrderingAttribute.GENE_ID, Service.Direction.ASC);
        serviceOrdering.put(CallService.OrderingAttribute.ANAT_ENTITY_ID, Service.Direction.ASC);
        serviceOrdering.put(CallService.OrderingAttribute.DEV_STAGE_ID, Service.Direction.ASC);

        // Data can be not coherent, it is a quick test to see if writing is OK.
        Set<ExperimentExpressionCount> exprExperimentCounts = new HashSet<>();
        exprExperimentCounts.add(new ExperimentExpressionCount(
                CallType.Expression.EXPRESSED, DataQuality.HIGH, PropagationState.SELF, 1));
        exprExperimentCounts.add(new ExperimentExpressionCount(
                CallType.Expression.EXPRESSED, DataQuality.LOW, PropagationState.SELF, 1));
        exprExperimentCounts.add(new ExperimentExpressionCount(
                CallType.Expression.NOT_EXPRESSED, DataQuality.HIGH, PropagationState.SELF, 0));
        exprExperimentCounts.add(new ExperimentExpressionCount(
                CallType.Expression.NOT_EXPRESSED, DataQuality.LOW, PropagationState.SELF, 0));
        exprExperimentCounts.add(new ExperimentExpressionCount(
                CallType.Expression.EXPRESSED, DataQuality.HIGH, PropagationState.DESCENDANT, 0));
        exprExperimentCounts.add(new ExperimentExpressionCount(
                CallType.Expression.EXPRESSED, DataQuality.LOW, PropagationState.DESCENDANT, 0));
        exprExperimentCounts.add(new ExperimentExpressionCount(
                CallType.Expression.NOT_EXPRESSED, DataQuality.HIGH, PropagationState.ANCESTOR, 0));
        exprExperimentCounts.add(new ExperimentExpressionCount(
                CallType.Expression.NOT_EXPRESSED, DataQuality.LOW, PropagationState.ANCESTOR, 0));
        exprExperimentCounts.add(new ExperimentExpressionCount(
                CallType.Expression.EXPRESSED, DataQuality.HIGH, PropagationState.ALL, 1));
        exprExperimentCounts.add(new ExperimentExpressionCount(
                CallType.Expression.EXPRESSED, DataQuality.LOW, PropagationState.ALL, 1));
        exprExperimentCounts.add(new ExperimentExpressionCount(
                CallType.Expression.NOT_EXPRESSED, DataQuality.HIGH, PropagationState.ALL, 0));
        exprExperimentCounts.add(new ExperimentExpressionCount(
                CallType.Expression.NOT_EXPRESSED, DataQuality.LOW, PropagationState.ALL, 0));

        Set<ExperimentExpressionCount> noExprExperimentCounts = new HashSet<>();
        noExprExperimentCounts.add(new ExperimentExpressionCount(
                CallType.Expression.EXPRESSED, DataQuality.HIGH, PropagationState.SELF, 0));
        noExprExperimentCounts.add(new ExperimentExpressionCount(
                CallType.Expression.EXPRESSED, DataQuality.LOW, PropagationState.SELF, 0));
        noExprExperimentCounts.add(new ExperimentExpressionCount(
                CallType.Expression.NOT_EXPRESSED, DataQuality.HIGH, PropagationState.SELF, 0));
        noExprExperimentCounts.add(new ExperimentExpressionCount(
                CallType.Expression.NOT_EXPRESSED, DataQuality.LOW, PropagationState.SELF, 0));
        noExprExperimentCounts.add(new ExperimentExpressionCount(
                CallType.Expression.EXPRESSED, DataQuality.HIGH, PropagationState.DESCENDANT, 0));
        noExprExperimentCounts.add(new ExperimentExpressionCount(
                CallType.Expression.EXPRESSED, DataQuality.LOW, PropagationState.DESCENDANT, 0));
        noExprExperimentCounts.add(new ExperimentExpressionCount(
                CallType.Expression.NOT_EXPRESSED, DataQuality.HIGH, PropagationState.ANCESTOR, 1));
        noExprExperimentCounts.add(new ExperimentExpressionCount(
                CallType.Expression.NOT_EXPRESSED, DataQuality.LOW, PropagationState.ANCESTOR, 1));
        noExprExperimentCounts.add(new ExperimentExpressionCount(
                CallType.Expression.EXPRESSED, DataQuality.HIGH, PropagationState.ALL, 0));
        noExprExperimentCounts.add(new ExperimentExpressionCount(
                CallType.Expression.EXPRESSED, DataQuality.LOW, PropagationState.ALL, 0));
        noExprExperimentCounts.add(new ExperimentExpressionCount(
                CallType.Expression.NOT_EXPRESSED, DataQuality.HIGH, PropagationState.ALL, 1));
        noExprExperimentCounts.add(new ExperimentExpressionCount(
                CallType.Expression.NOT_EXPRESSED, DataQuality.LOW, PropagationState.ALL, 1));

        List<ExpressionCall> calls = Arrays.asList(
                new ExpressionCall(new Gene("ID1", new Species(1), new GeneBioType("type1")),
                        new Condition(new AnatEntity("ae1", "aeName1", "aeDesc1"),
                                new DevStage("ds1", "dsName1", "dsDesc1"), new Species(1)),
                        new DataPropagation(PropagationState.SELF, PropagationState.SELF, true),
                        ExpressionSummary.EXPRESSED, SummaryQuality.GOLD,
                        Arrays.asList(new ExpressionCallData(DataType.AFFYMETRIX, exprExperimentCounts,
                                10, new BigDecimal(99), new BigDecimal(88), new BigDecimal(77),
                                new DataPropagation())),
                        new BigDecimal(90), new BigDecimal(44)),
                new ExpressionCall(new Gene("ID1", new Species(1), new GeneBioType("type1")),
                        new Condition(new AnatEntity("ae1", "aeName1", "aeDesc1"),
                                new DevStage("ds2", "dsName2", "dsDesc2"), new Species(2)),
                        new DataPropagation(PropagationState.DESCENDANT, PropagationState.SELF, false),
                        ExpressionSummary.EXPRESSED, SummaryQuality.SILVER,
                        Arrays.asList(new ExpressionCallData(DataType.EST, exprExperimentCounts,
                                10, new BigDecimal(99), new BigDecimal(88), new BigDecimal(77),
                                new DataPropagation())), null, null),
                new ExpressionCall(new Gene("ID1", new Species(1), new GeneBioType("type1")),
                        new Condition(new AnatEntity("ae2", "aeName2", "aeDesc2"),
                                new DevStage("ds1", "dsName1", "dsDesc1"), new Species(1)),
                        new DataPropagation(PropagationState.SELF, PropagationState.ANCESTOR, false),
                        ExpressionSummary.NOT_EXPRESSED, SummaryQuality.SILVER,
                        Arrays.asList(new ExpressionCallData(DataType.RNA_SEQ, noExprExperimentCounts,
                                10, new BigDecimal(99), new BigDecimal(88), new BigDecimal(77),
                                new DataPropagation()),
                                new ExpressionCallData(DataType.IN_SITU, noExprExperimentCounts,
                                        10, new BigDecimal(99), new BigDecimal(88), new BigDecimal(77),
                                        new DataPropagation())
                                ), null, null));
        
        Set<Attribute> attr = getAttributes(true);
        
        when(callService.loadExpressionCalls(callFilterSp11, attr, serviceOrdering))
        .thenReturn(calls.stream().filter(c -> c.getCondition().getSpeciesId() == 1));
        when(callService.loadExpressionCalls(callFilterSp22, attr, serviceOrdering))
        .thenReturn(calls.stream().filter(c -> c.getCondition().getSpeciesId() == 2));
        
        Set<SingleSpExprFileType2> fileTypes = new HashSet<SingleSpExprFileType2>(
                Arrays.asList(SingleSpExprFileType2.EXPR_SIMPLE, SingleSpExprFileType2.EXPR_ADVANCED)); 

        String directory = testFolder.newFolder("folder_isObservedDataOnly_" + true).getPath();

        Set<Attribute> params = new HashSet<>(Arrays.asList(Attribute.ANAT_ENTITY_ID, Attribute.DEV_STAGE_ID));
        GenerateExprFile2 generate = new GenerateExprFile2(mockManager, 
                Arrays.asList(11, 22), fileTypes, directory, params, () -> serviceFactory);
        generate.generateExprFiles();
        
        String outputSimpleFile11 = new File(directory, "Genus11_spName1_" + 
                SingleSpExprFileType2.EXPR_SIMPLE + "_development" + GenerateDownloadFile.EXTENSION).getAbsolutePath();
        String outputSimpleFile22 = new File(directory, "Genus22_spName2_" + 
                SingleSpExprFileType2.EXPR_SIMPLE + "_development" + GenerateDownloadFile.EXTENSION).getAbsolutePath();
        
        String outputCompleteFile11 = new File(directory, "Genus11_spName1_" + 
                SingleSpExprFileType2.EXPR_ADVANCED + "_development" + GenerateDownloadFile.EXTENSION).getAbsolutePath();
        String outputCompleteFile22 = new File(directory, "Genus22_spName2_" + 
                SingleSpExprFileType2.EXPR_ADVANCED + "_development" + GenerateDownloadFile.EXTENSION).getAbsolutePath();
        
        assertExpressionFile(outputSimpleFile11, 11, true, 1, true);
        assertExpressionFile(outputSimpleFile22, 22, true, 0, true);
        assertExpressionFile(outputCompleteFile11, 11, false, 2, true);
        assertExpressionFile(outputCompleteFile22, 22, false, 1, true);
    }
//    
//    /**
//     * Test method {@link GenerateDownloadFile#convertToFileTypes(Collection, Class)} 
//     * used with a {@code ExprFileType}.
//     */
//    @Test
//    public void shouldConvertToFyleTypes() {
//        //all expression file types
//        Set<SingleSpExprFileType2> exprExpectedFileTypes = new HashSet<SingleSpExprFileType2>(
//                EnumSet.of(SingleSpExprFileType2.EXPR_SIMPLE, SingleSpExprFileType2.EXPR_COMPLETE));
//        assertEquals("Incorrect ExprFileTypes retrieved", exprExpectedFileTypes, 
//                GenerateDownloadFile.convertToFileTypes(Arrays.asList(
//                        SingleSpExprFileType2.EXPR_SIMPLE.getStringRepresentation(), 
//                        SingleSpExprFileType2.EXPR_COMPLETE.getStringRepresentation()), 
//                        SingleSpExprFileType2.class));
//        assertEquals("Incorrect ExprFileTypes retrieved", exprExpectedFileTypes, 
//                GenerateDownloadFile.convertToFileTypes(Arrays.asList(
//                        SingleSpExprFileType2.EXPR_SIMPLE.name(), 
//                        SingleSpExprFileType2.EXPR_COMPLETE.name()), 
//                        SingleSpExprFileType2.class));
//        
//        //only one expression file type
//        exprExpectedFileTypes = new HashSet<SingleSpExprFileType2>(
//                EnumSet.of(SingleSpExprFileType2.EXPR_COMPLETE));
//        assertEquals("Incorrect ExprFileTypes retrieved", exprExpectedFileTypes, 
//                GenerateDownloadFile.convertToFileTypes(Arrays.asList(
//                        SingleSpExprFileType2.EXPR_COMPLETE.getStringRepresentation()), 
//                        SingleSpExprFileType2.class));
//        assertEquals("Incorrect ExprFileTypes retrieved", exprExpectedFileTypes, 
//                GenerateDownloadFile.convertToFileTypes(Arrays.asList(
//                        SingleSpExprFileType2.EXPR_COMPLETE.name()), 
//                        SingleSpExprFileType2.class));
//        
//        //test exceptions
//        try {
//            //existing FileType name, but incorrect type provided
//            GenerateDownloadFile.convertToFileTypes(Arrays.asList(
//                    SingleSpExprFileType2.EXPR_COMPLETE.getStringRepresentation()), 
//                    SingleSpDiffExprFileType.class);
//            //test failed, exception not thrown as expected
//            throw log.throwing(new AssertionError("IllegalArgumentException not thrown as expected"));
//        } catch (IllegalArgumentException e) {
//            //test passed
//        }
//        try {
//            //non-existing FileType name
//            GenerateDownloadFile.convertToFileTypes(Arrays.asList("whatever"), 
//                    SingleSpExprFileType2.class);
//            //test failed, exception not thrown as expected
//            throw log.throwing(new AssertionError("IllegalArgumentException not thrown as expected"));
//        } catch (IllegalArgumentException e) {
//            //test passed
//        }
//    }
//
    /**
     * Test {@link GenerateExprFile2#generateExprFiles()},
     * which is the central method of the class doing all the job.
     */
    @Test
    public void shouldGenerateBasicExprFiles() throws IOException {
        // Filter keeps observed condition only (stage and organ are observed)
        this.shouldGenerateBasicExprFilesIsObservedDataOnly(true);
        
        // Filter keeps observed organ data only (organ is observed)
        this.shouldGenerateBasicExprFilesIsObservedDataOnly(false);
    }
//
    /**
     * Test {@link GenerateExprFile2#generateExprFiles()}, with two possible option for 
     * {@code isObservedDataOnly}.
     *
     * @param isObservedDataOnly    A {@code boolean} defining whether the filter for simple file 
     *                              keeps observed data only if {@code true} or organ observed data
     *                              only (propagated stages are allowed) if {@code false}.
     */
    private void shouldGenerateBasicExprFilesIsObservedDataOnly(boolean isObservedDataOnly) 
            throws IOException {        
        
        LinkedHashMap<CallService.OrderingAttribute, Service.Direction> serviceOrdering = 
                getOrderingAttributes(isObservedDataOnly);
        
        Species spe11 = new Species(11, null, null, "Genus11", "species11", null, null);
        Species spe22 = new Species(22, null, null, "Genus22", "species22", null, null);
        AnatEntity ae1 = new AnatEntity("Anat_id1", "name", "desc");
        AnatEntity ae2 = new AnatEntity("Anat_id2", "name", "desc");
        AnatEntity ae3 = new AnatEntity("Anat_id3", "name", "desc");
        DevStage ds1 = new DevStage("Stage_id1", "name", "desc");
        DevStage ds2 = new DevStage("Stage_id2", "name", "desc");
        DevStage parentDs2 = new DevStage("ParentStage_id2", "name", "desc");
        GeneBioType biotype = new GeneBioType("b");
        Gene g1 = new Gene("ID1", "name1", null, spe11, biotype, 1);
        Gene g2 = new Gene("ID2", "name2", null, spe11, biotype, 1);
        Gene g3 = new Gene("ID3", "name3", null, spe11, biotype, 1);
        Gene g4 = new Gene("ID4", "name4", null, spe22, biotype, 1);

        // First, we need a mock MySQLDAOManager, for the class to acquire mock DAOs. 
        MockDAOManager mockManager = new MockDAOManager();
        ServiceFactory serviceFactory = mock(ServiceFactory.class);
        AnatEntityService anatEntityService = mock(AnatEntityService.class);
        when(serviceFactory.getAnatEntityService()).thenReturn(anatEntityService);
        SpeciesService speciesService = mock(SpeciesService.class);
        when(serviceFactory.getSpeciesService()).thenReturn(speciesService);
        CallService service = mock(CallService.class);
        when(serviceFactory.getCallService()).thenReturn(service);
        
        // For each species, we need to mock getNonInformativeAnatEntities(), getExpressionCalls() 
        // and getNoExpressionCalls() (basic and global calls)
        
        //// Species 11
        Set<Integer> speciesIds = new HashSet<Integer>(Arrays.asList(spe11.getId())); 

        // Non informative anatomical entities
        when(anatEntityService.loadNonInformativeAnatEntitiesBySpeciesIds(speciesIds))
        .thenReturn(Arrays.asList(new AnatEntity("NonInfoAnatEnt1")).stream());
        
        //Load species info
        when(speciesService.loadSpeciesByIds(new HashSet<>(Arrays.asList(spe22.getId(), spe11.getId())), false))
        .thenReturn(new HashSet<>(Arrays.asList(spe11, spe22)));

        // Expression calls
        List<ExpressionCall> callsSp11 = new ArrayList<ExpressionCall>();
        // Line number: simple true: 1 simple false: 1 complete: 1
        callsSp11.add(new ExpressionCall(g1 , new Condition(ae1, ds1, spe11), 
                new DataPropagation(PropagationState.SELF, PropagationState.SELF, true),
                ExpressionSummary.EXPRESSED, SummaryQuality.SILVER, 
                new HashSet<ExpressionCallData>(Arrays.asList(
                    new ExpressionCallData(DataType.EST, new HashSet<>(Arrays.asList(
                            new ExperimentExpressionCount(CallType.Expression.EXPRESSED, 
                                    DataQuality.LOW, PropagationState.ALL, 1),
                            new ExperimentExpressionCount(CallType.Expression.EXPRESSED, 
                                    DataQuality.HIGH, PropagationState.ALL, 0),
                            new ExperimentExpressionCount(CallType.Expression.NOT_EXPRESSED, 
                                    DataQuality.LOW, PropagationState.ALL, 0),
                            new ExperimentExpressionCount(CallType.Expression.NOT_EXPRESSED, 
                                    DataQuality.HIGH, PropagationState.ALL, 0)
                            )),
                            0, new BigDecimal(99), new BigDecimal(88), new BigDecimal(77),
                          new DataPropagation(PropagationState.SELF, PropagationState.SELF, true)), 
                    new ExpressionCallData(DataType.RNA_SEQ, new HashSet<>(Arrays.asList(
                            new ExperimentExpressionCount(CallType.Expression.NOT_EXPRESSED, 
                                    DataQuality.LOW, PropagationState.ALL, 0),
                            new ExperimentExpressionCount(CallType.Expression.NOT_EXPRESSED, 
                                    DataQuality.HIGH, PropagationState.ALL, 0),
                            new ExperimentExpressionCount( CallType.Expression.EXPRESSED, 
                                    DataQuality.HIGH, PropagationState.ALL, 0),
                            new ExperimentExpressionCount( CallType.Expression.EXPRESSED, 
                                    DataQuality.LOW, PropagationState.ALL, 1)
                            )),
                            0, new BigDecimal(99), new BigDecimal(88), new BigDecimal(77),
                            new DataPropagation(PropagationState.SELF, PropagationState.SELF, true)))), 
                new BigDecimal(1257.34), new BigDecimal(100.00)));
        
        // Line number: simple true: X simple false: 2 complete: 2
        callsSp11.add(new ExpressionCall(g1 , new Condition(ae1, parentDs2, spe11), 
                new DataPropagation(PropagationState.SELF, PropagationState.DESCENDANT, false),
                ExpressionSummary.EXPRESSED, SummaryQuality.SILVER, 
                new HashSet<ExpressionCallData>(Arrays.asList(
                    new ExpressionCallData(DataType.AFFYMETRIX, new HashSet<>(Arrays.asList(
                             new ExperimentExpressionCount(CallType.Expression.EXPRESSED, 
                                    DataQuality.LOW, PropagationState.ALL, 1),
                             new ExperimentExpressionCount(CallType.Expression.EXPRESSED, 
                                     DataQuality.HIGH, PropagationState.ALL, 1),
                             new ExperimentExpressionCount(CallType.Expression.NOT_EXPRESSED, 
                                     DataQuality.LOW, PropagationState.ALL, 0),
                             new ExperimentExpressionCount(CallType.Expression.NOT_EXPRESSED, 
                                     DataQuality.HIGH, PropagationState.ALL, 0)
                            )),
                            0, new BigDecimal(99), new BigDecimal(88), new BigDecimal(77),
                          new DataPropagation(PropagationState.SELF, PropagationState.DESCENDANT, false)), 
                    new ExpressionCallData(DataType.IN_SITU, new HashSet<>(Arrays.asList(
                            new ExperimentExpressionCount( CallType.Expression.EXPRESSED, 
                                    DataQuality.LOW, PropagationState.ALL, 1),
                            new ExperimentExpressionCount( CallType.Expression.EXPRESSED, 
                                    DataQuality.HIGH, PropagationState.ALL, 0),
                            new ExperimentExpressionCount(CallType.Expression.NOT_EXPRESSED, 
                                    DataQuality.LOW, PropagationState.ALL, 0),
                            new ExperimentExpressionCount(CallType.Expression.NOT_EXPRESSED, 
                                    DataQuality.HIGH, PropagationState.ALL, 0)
                            )),
                            0, new BigDecimal(99), new BigDecimal(88), new BigDecimal(77),
                            new DataPropagation(PropagationState.SELF, PropagationState.DESCENDANT, false)))), 
                new BigDecimal(1257.34), new BigDecimal(100.00)));

        // Line number: simple true: 2 simple false: 3 complete: 3
        callsSp11.add(new ExpressionCall(g2 , new Condition(ae1, ds2, spe11), 
                new DataPropagation(PropagationState.SELF, PropagationState.SELF, true),
                ExpressionSummary.NOT_EXPRESSED, SummaryQuality.BRONZE, 
                new HashSet<ExpressionCallData>(Arrays.asList(
                    new ExpressionCallData(DataType.AFFYMETRIX, new HashSet<>(Arrays.asList(
                             new ExperimentExpressionCount(CallType.Expression.NOT_EXPRESSED, 
                                    DataQuality.LOW, PropagationState.ALL, 1),
                             new ExperimentExpressionCount(CallType.Expression.NOT_EXPRESSED, 
                                    DataQuality.HIGH, PropagationState.ALL, 0),
                             new ExperimentExpressionCount(CallType.Expression.EXPRESSED, 
                                     DataQuality.LOW, PropagationState.ALL, 0),
                              new ExperimentExpressionCount(CallType.Expression.EXPRESSED, 
                                     DataQuality.HIGH, PropagationState.ALL, 0))),
                            0, new BigDecimal(99), new BigDecimal(88), new BigDecimal(77),
                            new DataPropagation(PropagationState.SELF, PropagationState.SELF, true)))), 
                new BigDecimal(1257.34), new BigDecimal(100.00)));
        
        // Line number: simple true: X simple false: 4 complete: 4
        callsSp11.add(new ExpressionCall(g2 , new Condition(ae1, parentDs2, spe11), 
                new DataPropagation(PropagationState.SELF_AND_ANCESTOR, PropagationState.ANCESTOR, false),
                ExpressionSummary.EXPRESSED, SummaryQuality.BRONZE, 
                new HashSet<ExpressionCallData>(Arrays.asList(
                    new ExpressionCallData(DataType.AFFYMETRIX, new HashSet<>(Arrays.asList(
                             new ExperimentExpressionCount(CallType.Expression.EXPRESSED, 
                                    DataQuality.LOW, PropagationState.ALL, 1),
                             new ExperimentExpressionCount(CallType.Expression.EXPRESSED, 
                                     DataQuality.HIGH, PropagationState.ALL, 0),
                             new ExperimentExpressionCount(CallType.Expression.NOT_EXPRESSED, 
                                     DataQuality.LOW, PropagationState.ALL, 0),
                             new ExperimentExpressionCount(CallType.Expression.NOT_EXPRESSED, 
                                     DataQuality.HIGH, PropagationState.ALL, 0))),
                            0, new BigDecimal(99), new BigDecimal(88), new BigDecimal(77),
                            new DataPropagation(PropagationState.SELF, PropagationState.DESCENDANT, false)),
                    new ExpressionCallData(DataType.IN_SITU, new HashSet<>(Arrays.asList(
                            new ExperimentExpressionCount(CallType.Expression.NOT_EXPRESSED, 
                                   DataQuality.HIGH, PropagationState.ALL, 1),
                            new ExperimentExpressionCount(CallType.Expression.NOT_EXPRESSED, 
                                    DataQuality.LOW, PropagationState.ALL, 0),
                            new ExperimentExpressionCount(CallType.Expression.EXPRESSED, 
                                    DataQuality.HIGH, PropagationState.ALL, 0),
                             new ExperimentExpressionCount(CallType.Expression.EXPRESSED, 
                                     DataQuality.LOW, PropagationState.ALL, 0))),
                           0, new BigDecimal(99), new BigDecimal(88), new BigDecimal(77),
                           new DataPropagation(PropagationState.ANCESTOR, PropagationState.SELF, false)))), 
                new BigDecimal(1257.34), new BigDecimal(100.00)));

        // Line number: simple true: 3 simple false: 5 complete: 5
        callsSp11.add(new ExpressionCall(g3 , new Condition(ae1, ds2, spe11), 
                new DataPropagation(PropagationState.SELF_AND_ANCESTOR, PropagationState.SELF, true),
                ExpressionSummary.EXPRESSED, SummaryQuality.BRONZE, 
                new HashSet<ExpressionCallData>(Arrays.asList(
                    new ExpressionCallData(DataType.AFFYMETRIX, new HashSet<>(Arrays.asList(
                             new ExperimentExpressionCount(CallType.Expression.EXPRESSED, 
                                    DataQuality.LOW, PropagationState.ALL, 1),
                             new ExperimentExpressionCount(CallType.Expression.EXPRESSED, 
                                     DataQuality.HIGH, PropagationState.ALL, 0),
                             new ExperimentExpressionCount(CallType.Expression.NOT_EXPRESSED, 
                                     DataQuality.LOW, PropagationState.ALL, 0),
                             new ExperimentExpressionCount(CallType.Expression.NOT_EXPRESSED, 
                                     DataQuality.HIGH, PropagationState.ALL, 0))),
                            0, new BigDecimal(99), new BigDecimal(88), new BigDecimal(77),
                            new DataPropagation(PropagationState.SELF, PropagationState.SELF, true)),
                    new ExpressionCallData(DataType.IN_SITU, new HashSet<>(Arrays.asList(
                            new ExperimentExpressionCount(CallType.Expression.NOT_EXPRESSED, 
                                   DataQuality.LOW, PropagationState.ALL, 1),
                            new ExperimentExpressionCount(CallType.Expression.NOT_EXPRESSED, 
                                    DataQuality.HIGH, PropagationState.ALL, 0),
                            new ExperimentExpressionCount(CallType.Expression.EXPRESSED, 
                                    DataQuality.LOW, PropagationState.ALL, 0),
                             new ExperimentExpressionCount(CallType.Expression.EXPRESSED, 
                                     DataQuality.HIGH, PropagationState.ALL, 0))),
                           0, new BigDecimal(99), new BigDecimal(88), new BigDecimal(77),
                           new DataPropagation(PropagationState.ANCESTOR, PropagationState.SELF, false)))), 
                new BigDecimal(1257.34), new BigDecimal(100.00)));

        // Line number: simple true: X simple false: X complete: 6
        callsSp11.add(new ExpressionCall(g3 , new Condition(ae3, ds2, spe11), 
                new DataPropagation(PropagationState.ANCESTOR_AND_DESCENDANT, PropagationState.SELF, false),
                ExpressionSummary.EXPRESSED, SummaryQuality.BRONZE, 
                new HashSet<ExpressionCallData>(Arrays.asList(
                    new ExpressionCallData(DataType.EST, new HashSet<>(Arrays.asList(
                             new ExperimentExpressionCount(CallType.Expression.EXPRESSED, 
                                    DataQuality.LOW, PropagationState.ALL, 1),
                             new ExperimentExpressionCount(CallType.Expression.EXPRESSED, 
                                     DataQuality.HIGH, PropagationState.ALL, 0),
                             new ExperimentExpressionCount(CallType.Expression.NOT_EXPRESSED, 
                                     DataQuality.LOW, PropagationState.ALL, 0),
                             new ExperimentExpressionCount(CallType.Expression.NOT_EXPRESSED, 
                                     DataQuality.HIGH, PropagationState.ALL, 0))),
                            0, new BigDecimal(99), new BigDecimal(88), new BigDecimal(77),
                            new DataPropagation(PropagationState.DESCENDANT, PropagationState.SELF, false)),
                    new ExpressionCallData(DataType.IN_SITU, new HashSet<>(Arrays.asList(
                            new ExperimentExpressionCount(CallType.Expression.NOT_EXPRESSED, 
                                   DataQuality.LOW, PropagationState.ALL, 1),
                            new ExperimentExpressionCount(CallType.Expression.NOT_EXPRESSED, 
                                    DataQuality.HIGH, PropagationState.ALL, 0),
                            new ExperimentExpressionCount(CallType.Expression.EXPRESSED, 
                                    DataQuality.LOW, PropagationState.ALL, 0),
                             new ExperimentExpressionCount(CallType.Expression.EXPRESSED, 
                                     DataQuality.HIGH, PropagationState.ALL, 0))),
                           0, new BigDecimal(99), new BigDecimal(88), new BigDecimal(77),
                           new DataPropagation(PropagationState.ANCESTOR, PropagationState.SELF, false)))), 
                new BigDecimal(1257.34), new BigDecimal(100.00)));
        
        ExpressionCallFilter callFilter = getCallFilter(Collections.singleton(new GeneFilter(spe11.getId())));
        
        Set<Attribute> attr = getAttributes(isObservedDataOnly);
        
        Stream<ExpressionCall> mockCallStreamSp11 = callsSp11.stream();
        when(service.loadExpressionCalls(callFilter, attr, serviceOrdering))
        .thenReturn(mockCallStreamSp11);
        
        when(service.loadExpressionCalls(callFilter, attr, serviceOrdering))
        .thenReturn(mockCallStreamSp11);
        
        //// Species 22
        speciesIds = new HashSet<Integer>(Arrays.asList(spe22.getId())); 

        // Non informative anatomical entities
        when(anatEntityService.loadNonInformativeAnatEntitiesBySpeciesIds(speciesIds))
            .thenReturn(Arrays.asList(new AnatEntity("NonInfoAnatEnt2")).stream());
        
        // Expression calls
        
        
        List<ExpressionCall> callsSp22 = new ArrayList<ExpressionCall>(); 
        // Line number: simple true: 1 simple false: 1 complete: 1
        callsSp22.add(new ExpressionCall(g4, new Condition(ae1, ds2, spe22),
                new DataPropagation(PropagationState.SELF, PropagationState.SELF, true),
                ExpressionSummary.EXPRESSED, SummaryQuality.BRONZE, 
                new HashSet<ExpressionCallData>(Arrays.asList(
                    new ExpressionCallData(DataType.AFFYMETRIX, new HashSet<>(Arrays.asList(
                             new ExperimentExpressionCount(CallType.Expression.NOT_EXPRESSED, 
                                    DataQuality.LOW, PropagationState.ALL, 1),
                             new ExperimentExpressionCount(CallType.Expression.NOT_EXPRESSED, 
                                     DataQuality.HIGH, PropagationState.ALL, 0),
                             new ExperimentExpressionCount(CallType.Expression.EXPRESSED, 
                                     DataQuality.HIGH, PropagationState.ALL, 0),
                             new ExperimentExpressionCount(CallType.Expression.EXPRESSED, 
                                     DataQuality.LOW, PropagationState.ALL, 0))),
                            0, new BigDecimal(99), new BigDecimal(88), new BigDecimal(77),
                            new DataPropagation(PropagationState.SELF, PropagationState.SELF, true)),
                    new ExpressionCallData(DataType.IN_SITU, new HashSet<>(Arrays.asList(
                            new ExperimentExpressionCount(CallType.Expression.EXPRESSED, 
                                   DataQuality.LOW, PropagationState.ALL, 1),
                            new ExperimentExpressionCount(CallType.Expression.EXPRESSED, 
                                    DataQuality.HIGH, PropagationState.ALL, 0),
                            new ExperimentExpressionCount(CallType.Expression.NOT_EXPRESSED, 
                                    DataQuality.LOW, PropagationState.ALL, 0),
                             new ExperimentExpressionCount(CallType.Expression.NOT_EXPRESSED, 
                                     DataQuality.HIGH, PropagationState.ALL, 0))),
                           0, new BigDecimal(99), new BigDecimal(88), new BigDecimal(77),
                           new DataPropagation(PropagationState.SELF, PropagationState.SELF, true)))), 
                new BigDecimal(1257.34), new BigDecimal(100.00)));

        // Line number: simple true: X simple false: X complete: 2
        callsSp22.add(new ExpressionCall(g4, new Condition(ae2, ds2, spe22),
                new DataPropagation(PropagationState.ANCESTOR, PropagationState.SELF, false),
                ExpressionSummary.NOT_EXPRESSED, SummaryQuality.SILVER, 
                new HashSet<ExpressionCallData>(Arrays.asList(
                    new ExpressionCallData(DataType.IN_SITU, new HashSet<>(Arrays.asList(
                             new ExperimentExpressionCount(CallType.Expression.NOT_EXPRESSED, 
                                    DataQuality.HIGH, PropagationState.ALL, 1),
                             new ExperimentExpressionCount(CallType.Expression.NOT_EXPRESSED, 
                                     DataQuality.LOW, PropagationState.ALL, 0),
                             new ExperimentExpressionCount(CallType.Expression.EXPRESSED, 
                                     DataQuality.LOW, PropagationState.ALL, 0),
                             new ExperimentExpressionCount(CallType.Expression.EXPRESSED, 
                                     DataQuality.HIGH, PropagationState.ALL, 0))),
                            0, new BigDecimal(99), new BigDecimal(88), new BigDecimal(77),
                            new DataPropagation(PropagationState.ANCESTOR, PropagationState.SELF, false)))), 
                new BigDecimal(1257.34), new BigDecimal(100.00)));
        
        if (!isObservedDataOnly) {
            callsSp22.add(new ExpressionCall(g4, new Condition(ae2, null, spe22),
                    new DataPropagation(PropagationState.ANCESTOR, PropagationState.SELF, false),
                    ExpressionSummary.NOT_EXPRESSED, SummaryQuality.SILVER, 
                    new HashSet<ExpressionCallData>(Arrays.asList(
                        new ExpressionCallData(DataType.IN_SITU, new HashSet<>(Arrays.asList(
                                 new ExperimentExpressionCount(CallType.Expression.NOT_EXPRESSED, 
                                        DataQuality.HIGH, PropagationState.ALL, 1),
                                 new ExperimentExpressionCount(CallType.Expression.NOT_EXPRESSED, 
                                         DataQuality.LOW, PropagationState.ALL, 0),
                                 new ExperimentExpressionCount(CallType.Expression.EXPRESSED, 
                                         DataQuality.LOW, PropagationState.ALL, 0),
                                 new ExperimentExpressionCount(CallType.Expression.EXPRESSED, 
                                         DataQuality.HIGH, PropagationState.ALL, 0))),
                                0, new BigDecimal(99), new BigDecimal(88), new BigDecimal(77),
                                new DataPropagation(PropagationState.ANCESTOR, PropagationState.SELF, false)))), 
                    new BigDecimal(1257.34), new BigDecimal(100.00)));
        }

        Stream<ExpressionCall> mockCallStreamSp22 = callsSp22.stream();
        
        callFilter = getCallFilter(Collections.singleton(new GeneFilter(spe22.getId())));

        when(service.loadExpressionCalls(callFilter, attr, serviceOrdering))
        .thenReturn(mockCallStreamSp22);

        Set<SingleSpExprFileType2> fileTypes = new HashSet<SingleSpExprFileType2>(
                Arrays.asList(SingleSpExprFileType2.EXPR_SIMPLE, SingleSpExprFileType2.EXPR_ADVANCED)); 
                
        String directory = testFolder.newFolder("folder_isObservedDataOnly_" + isObservedDataOnly).getPath();
        
        String outputSimpleFile11 = new File(directory, "Genus11_species11_" + 
                SingleSpExprFileType2.EXPR_SIMPLE + (isObservedDataOnly==true?"_development":"") + 
                GenerateDownloadFile.EXTENSION).getAbsolutePath();
        String outputSimpleFile22 = new File(directory, "Genus22_species22_" + 
                SingleSpExprFileType2.EXPR_SIMPLE + (isObservedDataOnly==true?"_development":"") + 
                GenerateDownloadFile.EXTENSION).getAbsolutePath();
        String outputAdvancedFile11 = new File(directory, "Genus11_species11_" + 
                SingleSpExprFileType2.EXPR_ADVANCED + (isObservedDataOnly==true?"_development":"") + 
                GenerateDownloadFile.EXTENSION).getAbsolutePath();
        String outputAdvancedFile22 = new File(directory, "Genus22_species22_" + 
                SingleSpExprFileType2.EXPR_ADVANCED + (isObservedDataOnly==true?"_development":"") + 
                GenerateDownloadFile.EXTENSION).getAbsolutePath();

        Set<Attribute> params = new HashSet<>(Arrays.asList(Attribute.ANAT_ENTITY_ID));
        if(isObservedDataOnly) {
            params.add(Attribute.DEV_STAGE_ID);
        }
        
        GenerateExprFile2 generate = new GenerateExprFile2(mockManager, Arrays.asList(spe11.getId(), spe22.getId()),
                fileTypes, directory, params, () -> serviceFactory);
        generate.generateExprFiles();
        
        if (isObservedDataOnly) {
            // Filter keeps observed data only (stage and organ are observed)
            assertExpressionFile(outputSimpleFile11, spe11.getId(), true, 3, isObservedDataOnly);
            assertExpressionFile(outputSimpleFile22, spe22.getId(), true, 1, isObservedDataOnly);
            assertExpressionFile(outputAdvancedFile11, spe11.getId(), false, 6, isObservedDataOnly);
            assertExpressionFile(outputAdvancedFile22, spe22.getId(), false, 2, isObservedDataOnly);
        } else {
            assertExpressionFile(outputSimpleFile11, spe11.getId(), true, 3, isObservedDataOnly);
            assertExpressionFile(outputSimpleFile22, spe22.getId(), true, 1, isObservedDataOnly);
            assertExpressionFile(outputAdvancedFile11, spe11.getId(), false, 6, isObservedDataOnly);
            assertExpressionFile(outputAdvancedFile22, spe22.getId(), false, 3, isObservedDataOnly);
        }

        // Verify that all ResultSet are closed.
//        verify(mockSpeciesTORs).close();
//        verify(mockGeneTORs).close();
//        verify(mockAnatEntityTORs).close();
//        verify(mockStageTORs).close();

        verifyStreamClosed(mockCallStreamSp11);
        verifyStreamClosed(mockCallStreamSp22);
        
        //check that the connection was closed at each species iteration
        verify(mockManager.mockManager, times(2)).releaseResources();

    }
    

    private LinkedHashMap<CallService.OrderingAttribute, Service.Direction> getOrderingAttributes(Boolean isObservedDataOnly) {
        log.entry(isObservedDataOnly);
        LinkedHashMap<CallService.OrderingAttribute, Service.Direction> serviceOrdering = 
                new LinkedHashMap<>();
        serviceOrdering.put(CallService.OrderingAttribute.GENE_ID, Service.Direction.ASC);
        serviceOrdering.put(CallService.OrderingAttribute.ANAT_ENTITY_ID, Service.Direction.ASC);
        if(isObservedDataOnly) {
            serviceOrdering.put(CallService.OrderingAttribute.DEV_STAGE_ID, Service.Direction.ASC);
        }
        return log.exit(serviceOrdering);
    }
    
    private Set<Attribute> getAttributes(Boolean isObservedDataOnly) {
        log.entry(isObservedDataOnly);
        Set<Attribute> attrs = new HashSet<>(Arrays.asList(Attribute.ANAT_ENTITY_ID,
            Attribute.DATA_QUALITY, Attribute.DATA_TYPE_RANK_INFO, Attribute.OBSERVED_DATA, 
            Attribute.GENE, Attribute.CALL_TYPE, Attribute.EXPERIMENT_COUNTS,
            Attribute.MEAN_RANK));
        if(isObservedDataOnly) {
            attrs.add(Attribute.DEV_STAGE_ID);
        }
        return log.exit(attrs);
    }

    private ExpressionCallFilter getCallFilter(Set<GeneFilter> geneFilters) {
        log.entry();
        
        Map<ExpressionSummary, SummaryQuality> summaryQuality = new HashMap<>();
        summaryQuality.put(ExpressionSummary.EXPRESSED, SummaryQuality.SILVER);
        summaryQuality.put(ExpressionSummary.NOT_EXPRESSED, SummaryQuality.SILVER);
        
        Map<CallType.Expression, Boolean> obsDataFilter = new HashMap<>();
        obsDataFilter.put(null, true);
        
        ExpressionCallFilter callFilter = 
                new ExpressionCallFilter(summaryQuality, geneFilters, null, null, obsDataFilter, null, null);
        return log.exit(callFilter);
    }

    /** 
     * Verify stream is closed.
     * 
     * @param stream
     */
    private void verifyStreamClosed(Stream<?> stream) {
        try {
            stream.findAny();        
        } catch (IllegalStateException e) {
            assertEquals(e.getLocalizedMessage(), "stream has already been operated upon or closed");
        }
    }

    //TODO reactivate or remove
//    /**
//     * Test if a file is created when no data are retrieved using 
//     * {@link GenerateExprFile2#generateExprFiles()},
//     * which is the central method of the class doing all the job.
//     */
//    @Test
//    public void shouldGenerateBasicExprFilesNoData() throws IOException {
//        // First, we need a mock MySQLDAOManager, for the class to acquire mock DAOs. 
//        MockDAOManager mockManager = new MockDAOManager();
//        
//        ServiceFactory serviceFactory = mock(ServiceFactory.class);
//        AnatEntityService anatEntityService = mock(AnatEntityService.class);
//        when(serviceFactory.getAnatEntityService()).thenReturn(anatEntityService);
//        CallService callService = mock(CallService.class);
//        when(serviceFactory.getCallService()).thenReturn(callService);
//
//        Set<String> speciesIds = new HashSet<String>(Arrays.asList("33")); 
//
//        // Species 
//        MySQLSpeciesTOResultSet mockSpeciesTORs33 = createMockDAOResultSet(
//                Arrays.asList(new SpeciesTO("33", null, "Genus33", "species33", null, null, null, null, null)),
//                MySQLSpeciesTOResultSet.class);
//        when(mockManager.mockSpeciesDAO.getSpeciesByIds(
//                new HashSet<String>(Arrays.asList("33")))).thenReturn(mockSpeciesTORs33);
//
//        // Gene names
//        MySQLGeneTOResultSet mockGeneTORs33 = createMockDAOResultSet(
//                Arrays.asList(new GeneTO("IDX", "genNX", null)), MySQLGeneTOResultSet.class);
//        // The only Attributes requested should be ID and name, this will be checked 
//        // at the end of the test
//        when(mockManager.mockGeneDAO.getGenesBySpeciesIds(speciesIds)). thenReturn(mockGeneTORs33);
//
//        // Stage names
//        MySQLStageTOResultSet mockStageTORs33 = createMockDAOResultSet(Arrays.asList(
//                new StageTO("Stage_idX", "stageNX", null, null, null, null, null, null)),
//                MySQLStageTOResultSet.class);
//        // The only Attributes requested should be ID and name, this will be checked 
//        // at the end of the test
//        when(mockManager.mockStageDAO.getStagesBySpeciesIds(speciesIds)).thenReturn(mockStageTORs33);
//
//        // Anatomical entity names
//        MySQLAnatEntityTOResultSet mockAnatEntityTORs33 = createMockDAOResultSet(
//                Arrays.asList(new AnatEntityTO("Anat_idX", "anatNameX", null, null, null, null)),
//                MySQLAnatEntityTOResultSet.class);
//        // The only Attributes requested should be ID and name, this will be checked 
//        // at the end of the test
//        when(mockManager.mockAnatEntityDAO.getAnatEntitiesBySpeciesIds(speciesIds)).
//        thenReturn(mockAnatEntityTORs33);
//
//        // Non informative anatomical entities
//        when(anatEntityService.loadNonInformativeAnatEntitiesBySpeciesIds(speciesIds))
//            .thenReturn(Arrays.asList(new AnatEntity("NonInfoAnatEnt1")).stream());
//
//        // Expression calls
//        when(callService.loadExpressionCalls(eq("33"), anyObject(), eq(null), anyObject(), eq(true)))
//            .thenReturn(Stream.empty());
//
//        // Expression calls
//        Stream<ExpressionCall> mockCalls = Stream.empty();
//        when(callService.loadExpressionCalls("33", getCallFilter(), null, getOrderingAttributes(), true))
//            .thenReturn(mockCalls);
//
//        String directory = testFolder.newFolder("tmpFolder").getPath();
//        Set<SingleSpExprFileType2> fileTypes = new HashSet<SingleSpExprFileType2>(
//                Arrays.asList(SingleSpExprFileType2.EXPR_COMPLETE)); 
//        GenerateExprFile2 generate = new GenerateExprFile2(mockManager, 
//                Arrays.asList("33"), fileTypes, directory, false, () -> serviceFactory);
//        generate.generateExprFiles();
//
//        File file = new File(directory, "Genus33_species33_" + 
//                SingleSpExprFileType2.EXPR_COMPLETE + GenerateDownloadFile.EXTENSION);
//        assertFalse("File should not be created", file.exists());
//
//        // Verify that all ResultSet are closed.
//        verify(mockSpeciesTORs33).close();
//        verify(mockGeneTORs33).close();
//        verify(mockStageTORs33).close();
//        verify(mockAnatEntityTORs33).close();
//        
//        //check that the connection was closed at each species iteration
//        verify(mockManager.mockManager, times(1)).releaseResources();
//
//        // Verify that setAttributes are correctly called.        
//        verify(mockManager.mockAnatEntityDAO, times(1)).setAttributes(
//                AnatEntityDAO.Attribute.ID, AnatEntityDAO.Attribute.NAME);
//        verify(mockManager.mockGeneDAO, times(1)).setAttributes(
//                GeneDAO.Attribute.ID, GeneDAO.Attribute.NAME);
//        verify(mockManager.mockStageDAO, times(1)).setAttributes(
//                StageDAO.Attribute.ID, StageDAO.Attribute.NAME);
//        verifyStreamClosed(mockCalls);
//    }
    
    //TODO reactivate or remove
//    /**
//     * Test if exception is launch when an expression call and a no-expression call 
//     * have data for the same data type using 
//     * {@link GenerateExprFile2#generateExprFiles()},
//     * which is the central method of the class doing all the job.
//     */
//    @Test
//    public void shouldGenerateBasicExprFilesDataConflictException() throws IOException {
//        // First, we need a mock MySQLDAOManager, for the class to acquire mock DAOs. 
//        MockDAOManager mockManager = new MockDAOManager();
//        
//        ServiceFactory serviceFactory = mock(ServiceFactory.class);
//        AnatEntityService anatEntityService = mock(AnatEntityService.class);
//        when(serviceFactory.getAnatEntityService()).thenReturn(anatEntityService);
//        CallService callService = mock(CallService.class);
//        when(serviceFactory.getCallService()).thenReturn(callService);
//
//        Set<String> speciesIds = new HashSet<String>(Arrays.asList("33")); 
//
//        // Species 
//        MySQLSpeciesTOResultSet mockSpeciesTORs33 = createMockDAOResultSet(
//                Arrays.asList(new SpeciesTO("33", null, "Genus33", "species33", null, null, null, null, null)),
//                MySQLSpeciesTOResultSet.class);
//        when(mockManager.mockSpeciesDAO.getSpeciesByIds(
//                new HashSet<String>(Arrays.asList("33")))).thenReturn(mockSpeciesTORs33);
//
//        // Gene names
//        MySQLGeneTOResultSet mockGeneTORs33 = createMockDAOResultSet(
//                Arrays.asList(new GeneTO("IDX", "genNX", null)), MySQLGeneTOResultSet.class);
//        // The only Attributes requested should be ID and name, this will be checked 
//        // at the end of the test
//        when(mockManager.mockGeneDAO.getGenesBySpeciesIds(speciesIds)). thenReturn(mockGeneTORs33);
//
//        // Stage names
//        MySQLStageTOResultSet mockStageTORs33 = createMockDAOResultSet(Arrays.asList(
//                new StageTO("Stage_idX", "stageNX", null, null, null, null, null, null)),
//                MySQLStageTOResultSet.class);
//        // The only Attributes requested should be ID and name, this will be checked 
//        // at the end of the test
//        when(mockManager.mockStageDAO.getStagesBySpeciesIds(speciesIds)).thenReturn(mockStageTORs33);
//
//        // Anatomical entity names
//        MySQLAnatEntityTOResultSet mockAnatEntityTORs33 = createMockDAOResultSet(
//                Arrays.asList(new AnatEntityTO("Anat_idX", "anatNameX", null, null, null, null)),
//                MySQLAnatEntityTOResultSet.class);
//        // The only Attributes requested should be ID and name, this will be checked 
//        // at the end of the test
//        when(mockManager.mockAnatEntityDAO.getAnatEntitiesBySpeciesIds(speciesIds)).
//        thenReturn(mockAnatEntityTORs33);
//
//        // Non informative anatomical entities
//        when(anatEntityService.loadNonInformativeAnatEntitiesBySpeciesIds(speciesIds))
//            .thenReturn(Arrays.asList(new AnatEntity("NonInfoAnatEnt1")).stream());
//
//        // Expression calls
//        when(callService.loadExpressionCalls(eq("33"), anyObject(), eq(null), anyObject(), eq(true)))
//            .thenReturn(Stream.empty());
//
//        // Expression calls
//        List<ExpressionCall> calls = new ArrayList<ExpressionCall>(); 
//        calls.add(new ExpressionCall("IDX", new Condition("Anat_idX", "Stage_idX", "22"),
//                new DataPropagation(PropagationState.SELF, PropagationState.SELF, true),
//                ExpressionSummary.EXPRESSED, null,
//                new HashSet<>(Arrays.asList(
//                        new ExpressionCallData(CallType.Expression.NOT_EXPRESSED, DataQuality.LOW, DataType.IN_SITU,
//                                new DataPropagation(PropagationState.SELF, PropagationState.SELF, true)),
//                        new ExpressionCallData(CallType.Expression.EXPRESSED, DataQuality.LOW, DataType.IN_SITU,
//                                new DataPropagation(PropagationState.SELF, PropagationState.SELF, true)))),
//                null));
//        Stream<ExpressionCall> mockCalls = calls.stream();
//        
//        when(callService.loadExpressionCalls("33", getCallFilter(), null, getOrderingAttributes(), true))
//            .thenReturn(mockCalls);
//
//        String directory = testFolder.newFolder("tmpFolder").getPath();
//        Set<SingleSpExprFileType2> fileTypes = new HashSet<SingleSpExprFileType2>(
//                Arrays.asList(SingleSpExprFileType2.EXPR_COMPLETE)); 
//        GenerateExprFile2 generate = new GenerateExprFile2(mockManager, 
//                Arrays.asList("33"), fileTypes, directory, false, () -> serviceFactory);
//        try {
//            generate.generateExprFiles();
//            fail("An IllegalArgumentException should be thrown");
//        } catch (IllegalArgumentException e) {
//            log.debug("Test passed");
//        }
//        // Verify that all ResultSet are closed.
//        verify(mockSpeciesTORs33).close();
//        verify(mockGeneTORs33).close();
//        verify(mockStageTORs33).close();
//        verify(mockAnatEntityTORs33).close();
//        
//        //check that the connection was closed at each species iteration
//        verify(mockManager.mockManager, times(1)).releaseResources();
//
//        // Verify that setAttributes are correctly called.        
//        verify(mockManager.mockAnatEntityDAO, times(1)).setAttributes(
//                AnatEntityDAO.Attribute.ID, AnatEntityDAO.Attribute.NAME);
//        verify(mockManager.mockGeneDAO, times(1)).setAttributes(
//                GeneDAO.Attribute.ID, GeneDAO.Attribute.NAME);
//        verify(mockManager.mockStageDAO, times(1)).setAttributes(
//                StageDAO.Attribute.ID, StageDAO.Attribute.NAME);
//        verifyStreamClosed(mockCalls);
//    }
    
    /**
     * Asserts that the simple expression/no-expression file is good.
     * <p>
     * Read given download file and check whether the file contents corresponds to what is expected. 
     * 
     * @param file              A {@code String} that is the path to the file were data was written 
     *                          as TSV.
     * @param isSimplified      An {@code Integer} defining the species ID.
     * @param expNbLines        An {@code Integer} defining the expected number of lines in 
     *                          {@code file}.
     * @param observedDataOnly  A {@code boolean} defining whether the filter for simple file keeps 
     *                          observed data only if {@code true} or organ observed data only 
     *                          (propagated stages are allowed) if {@code false}.
     * @throws IOException      If the file could not be used.
     */
    private void assertExpressionFile(String file, Integer speciesId, boolean isSimplified, 
            int expNbLines, boolean observedDataOnly) throws IOException {
        log.entry(file, speciesId, isSimplified, expNbLines);
        
        // We use '$' as character used to escape columns containing the delimiter to be able 
        // to test that '"' is around columns with name
        CsvPreference preference = new CsvPreference.Builder('$', '\t', "\n").build();
        
        List<Object> expressionSummaries = new ArrayList<Object>();
        for (ExpressionSummary sum : ExpressionSummary.values()) {
            expressionSummaries.add(GenerateDownloadFile.convertExpressionSummaryToString(sum));
        }
        
        List<Object> expressions = new ArrayList<Object>();
        for (Expression expr : Expression.values()) {
            expressions.add(GenerateDownloadFile.convertExpressionToString(expr));
        }
        expressions.add(GenerateDownloadFile.NO_DATA_VALUE);

        List<Object> qualitySummaries = new ArrayList<Object>();
        qualitySummaries.add(GenerateDownloadFile.convertSummaryQualityToString(SummaryQuality.GOLD));
        qualitySummaries.add(GenerateDownloadFile.convertSummaryQualityToString(SummaryQuality.SILVER));
        qualitySummaries.add(GenerateDownloadFile.convertSummaryQualityToString(SummaryQuality.BRONZE));
        
        List<Object> originValues = new ArrayList<Object>();
        for (ObservedData data : ObservedData.values()) {
            originValues.add(data.getStringRepresentation());
        }

        List<CellProcessor> processorsList = new ArrayList<>(Arrays.asList(
                new StrNotNullOrEmpty(),    // gene ID
                new NotNull(),              // gene Name
                new StrNotNullOrEmpty(),    // anatomical entity ID
                new StrNotNullOrEmpty()));  // anatomical entity name
        
        if(observedDataOnly) {
            processorsList.addAll(new ArrayList<>(Arrays.asList(
                    new StrNotNullOrEmpty(),    // developmental stage ID
                    new StrNotNullOrEmpty()))); // developmental stage name
        }
        
        processorsList.addAll(new ArrayList<>(Arrays.asList(
                new IsElementOf(expressionSummaries),   // Expression
                new IsElementOf(qualitySummaries),      // Quality
                new StrNotNullOrEmpty())));             // Expression rank
              
        if (!isSimplified) {
            processorsList.addAll(new ArrayList<>(Arrays.asList(
                new IsElementOf(originValues),          // Including observed data 
                new IsElementOf(expressions),           // Affymetrix data
                new LMinMax(0, Long.MAX_VALUE),         // Affymetrix present high
                new LMinMax(0, Long.MAX_VALUE),         // Affymetrix present low
                new LMinMax(0, Long.MAX_VALUE),         // Affymetrix absent high
                new LMinMax(0, Long.MAX_VALUE),         // Affymetrix absent low
                new IsElementOf(originValues),          // Including Affymetrix data
                new IsElementOf(expressions),           // EST data
                new LMinMax(0, Long.MAX_VALUE),         // EST present high
                new LMinMax(0, Long.MAX_VALUE),         // EST present low
                new IsElementOf(originValues),          // Including EST data
                new IsElementOf(expressions),           // In Situ data
                new LMinMax(0, Long.MAX_VALUE),         // In Situ present high
                new LMinMax(0, Long.MAX_VALUE),         // In Situ present low
                new LMinMax(0, Long.MAX_VALUE),         // In Situ absent high
                new LMinMax(0, Long.MAX_VALUE),         // In Situ absent low
                new IsElementOf(originValues),          // Including in Situ data
                new IsElementOf(expressions),           // RNA-seq data
                new LMinMax(0, Long.MAX_VALUE),         // RNA-seq present high
                new LMinMax(0, Long.MAX_VALUE),         // RNA-seq present low
                new LMinMax(0, Long.MAX_VALUE),         // RNA-seq absent high
                new LMinMax(0, Long.MAX_VALUE),         // RNA-seq absent low
                new IsElementOf(originValues))));         // Including RNA-seq data
        }
        
        CellProcessor[] processors = processorsList.toArray(new CellProcessor[processorsList.size()]);

        try (ICsvListReader listReader = new CsvListReader(new FileReader(file), preference)) {
            String[] headers = listReader.getHeader(true);
            log.trace("Headers: {}", (Object[]) headers);

            // Check that the headers are what we expect
            List<String> expecteds = new ArrayList<String>(Arrays.asList( 
                    GenerateDownloadFile.GENE_ID_COLUMN_NAME, 
                    "\"" + GenerateDownloadFile.GENE_NAME_COLUMN_NAME + "\"",
                    GenerateDownloadFile.ANAT_ENTITY_ID_COLUMN_NAME, 
                    "\"" + GenerateDownloadFile.ANAT_ENTITY_NAME_COLUMN_NAME + "\""));
            
            if(observedDataOnly) {
                expecteds.addAll(new ArrayList<String>(Arrays.asList(
                        GenerateDownloadFile.STAGE_ID_COLUMN_NAME,
                        "\"" + GenerateDownloadFile.STAGE_NAME_COLUMN_NAME + "\"")));
            }
            
            expecteds.addAll(new ArrayList<String>(Arrays.asList(
                    GenerateDownloadFile.EXPRESSION_COLUMN_NAME,
                    GenerateDownloadFile.QUALITY_COLUMN_NAME,
                    GenerateDownloadFile.EXPRESSION_RANK_COLUMN_NAME)));
            
            if (!isSimplified) {
                expecteds.addAll(new ArrayList<String>(Arrays.asList(
                        GenerateDownloadFile.INCLUDING_OBSERVED_DATA_COLUMN_NAME,
                        GenerateDownloadFile.AFFYMETRIX_DATA_COLUMN_NAME, 
                        GenerateDownloadFile.AFFYMETRIX_PRESENT_HIGH_COUNT_COLUMN_NAME,
                        GenerateDownloadFile.AFFYMETRIX_PRESENT_LOW_COUNT_COLUMN_NAME,
                        GenerateDownloadFile.AFFYMETRIX_ABSENT_HIGH_COUNT_COLUMN_NAME,
                        GenerateDownloadFile.AFFYMETRIX_ABSENT_LOW_COUNT_COLUMN_NAME,
                        GenerateDownloadFile.AFFYMETRIX_OBSERVED_DATA_COLUMN_NAME, 
                        GenerateDownloadFile.EST_DATA_COLUMN_NAME, 
                        GenerateDownloadFile.EST_PRESENT_HIGH_COUNT_COLUMN_NAME,
                        GenerateDownloadFile.EST_PRESENT_LOW_COUNT_COLUMN_NAME,
                        GenerateDownloadFile.EST_OBSERVED_DATA_COLUMN_NAME, 
                        GenerateDownloadFile.IN_SITU_DATA_COLUMN_NAME, 
                        GenerateDownloadFile.IN_SITU_PRESENT_HIGH_COUNT_COLUMN_NAME,
                        GenerateDownloadFile.IN_SITU_PRESENT_LOW_COUNT_COLUMN_NAME,
                        GenerateDownloadFile.IN_SITU_ABSENT_HIGH_COUNT_COLUMN_NAME,
                        GenerateDownloadFile.IN_SITU_ABSENT_LOW_COUNT_COLUMN_NAME,
                        GenerateDownloadFile.IN_SITU_OBSERVED_DATA_COLUMN_NAME, 
                        GenerateDownloadFile.RNASEQ_DATA_COLUMN_NAME,
                        GenerateDownloadFile.RNASEQ_PRESENT_HIGH_COUNT_COLUMN_NAME,
                        GenerateDownloadFile.RNASEQ_PRESENT_LOW_COUNT_COLUMN_NAME,
                        GenerateDownloadFile.RNASEQ_ABSENT_HIGH_COUNT_COLUMN_NAME,
                        GenerateDownloadFile.RNASEQ_ABSENT_LOW_COUNT_COLUMN_NAME,
                        GenerateDownloadFile.RNASEQ_OBSERVED_DATA_COLUMN_NAME)));
            }
            
            assertArrayEquals("Incorrect headers", expecteds.stream().toArray(), headers);

            List<Object> rowList;
            int lineCount = 0;

            while ((rowList = listReader.read(processors)) != null) {
                log.debug("Row for {}/{}: {}", speciesId, isSimplified, rowList);
                lineCount++;
//                int columnCount = 0;
//                String geneId = (String) rowList.get(columnCount++);
//                String geneName = (String) rowList.get(columnCount++);
//                String anatEntityId = (String) rowList.get(columnCount++);
//                String anatEntityName = (String) rowList.get(columnCount++);
//                String stageId = null;
//                String stageName = null;
//                if (observedDataOnly) {
//                    stageId = (String) rowList.get(columnCount++);
//                    stageName = (String) rowList.get(columnCount++);
//                }
//                String resume = (String) rowList.get(columnCount++);
//                String quality = (String) rowList.get(columnCount++);
//                String exprRank = (String) rowList.get(columnCount++);
//
//                String affymetrixData = null, estData = null, inSituData = null, rnaSeqData = null, 
//                        affymetrixPresentHigh = null, affymetrixPresentLow = null,
//                        affymetrixAbsentHigh = null, affymetrixAbsentLow = null,
//                        estPresentHigh = null, estPresentLow = null, inSituPresentHigh = null,
//                        inSituPresentLow = null, inSituAbsentHigh = null, inSituAbsentLow = null,
//                        rnaSeqPresentHigh = null, rnaSeqPresentLow = null, rnaSeqAbsentHigh = null,
//                        rnaSeqAbsentLow = null,
//                        affymetrixObsData = null, estObsData = null, inSituObsData = null,
//                        rnaSeqObsData = null, observedData = null;
//
//                if (!isSimplified) {
//                    observedData = (String) rowList.get(columnCount++);
//                    affymetrixData = (String) rowList.get(columnCount++);
//                    affymetrixPresentHigh = (String) rowList.get(columnCount++);
//                    affymetrixPresentLow = (String) rowList.get(columnCount++);
//                    affymetrixAbsentHigh = (String) rowList.get(columnCount++);
//                    affymetrixAbsentLow = (String) rowList.get(columnCount++);
//                    affymetrixObsData = (String) rowList.get(columnCount++);
//                    estData = (String) rowList.get(columnCount++);
//                    estPresentHigh = (String) rowList.get(columnCount++);
//                    estPresentLow = (String) rowList.get(columnCount++);
//                    estObsData = (String) rowList.get(columnCount++);
//                    inSituData = (String) rowList.get(columnCount++);
//                    inSituPresentHigh = (String) rowList.get(columnCount++);
//                    inSituPresentLow = (String) rowList.get(columnCount++);
//                    inSituAbsentHigh = (String) rowList.get(columnCount++);
//                    inSituAbsentLow = (String) rowList.get(columnCount++);
//                    inSituObsData = (String) rowList.get(columnCount++);
//                    rnaSeqData = (String) rowList.get(columnCount++);
//                    rnaSeqPresentHigh = (String) rowList.get(columnCount++);
//                    rnaSeqPresentLow = (String) rowList.get(columnCount++);
//                    rnaSeqAbsentHigh = (String) rowList.get(columnCount++);
//                    rnaSeqAbsentLow = (String) rowList.get(columnCount++);
//                    rnaSeqObsData = (String) rowList.get(columnCount++);
//                }
                
//                if (isObservedDataOnly) {
//                    // Filter keeps observed data only (stage and organ are observed)
//                    assertExpressionFile(outputSimpleFile11, spe11.getId(), true, 3, isObservedDataOnly);
//                    assertExpressionFile(outputSimpleFile22, spe22.getId(), true, 1, isObservedDataOnly);
//                    assertExpressionFile(outputAdvancedFile11, spe11.getId(), false, 6, isObservedDataOnly);
//                    assertExpressionFile(outputAdvancedFile22, spe22.getId(), false, 2, isObservedDataOnly);
//                } else {
//                    assertExpressionFile(outputSimpleFile11, spe11.getId(), true, 3, isObservedDataOnly);
//                    assertExpressionFile(outputSimpleFile22, spe22.getId(), true, 1, isObservedDataOnly);
//                    assertExpressionFile(outputAdvancedFile11, spe11.getId(), false, 6, isObservedDataOnly);
//                    assertExpressionFile(outputAdvancedFile22, spe22.getId(), false, 3, isObservedDataOnly);
//                }

            }
            assertEquals("Incorrect number of lines in simple download file", expNbLines, lineCount);
        }
    }
    
    /**
     * Assert that specific complete file columns row are equal. It checks affymetrix data, 
     * EST data, <em>in Situ</em> data, and RNA-seq data columns. 
     * 
     * @param geneId            A {@code String} that is the gene ID of the row.
     * @param expAffyData       An {@code Expression} that is the expected affymetrix data. 
     * @param affyData          A {@code String} that is the actual affymetrix data.
     * @param expESTData        An {@code Expression} that is the expected EST data.
     * @param estData           A {@code String} that is the actual EST data.
     * @param expInSituData     An {@code Expression} that is the expected <em>in Situ</em> data.
     * @param inSituData        A {@code String} that is the actual <em>in Situ</em> data.
     * @param expRNAseqData     An {@code Expression} that is the expected RNA-seq data.
     * @param rnaSeqData        A {@code String} that is the actual RNA-seq data.
     */
    private void assertCompleteExprColumnRowEqual(String geneId, 
            Expression expAffyData, String affyData, DataState expAffyQual, String affyQual, 
            ObservedData expAffyObsData, String affyObsData,
            Expression expESTData, String estData, DataState expESTQual, String estQual, 
            ObservedData expESTObsData, String estObsData,
            Expression expInSituData, String inSituData, DataState expInSituQual, String inSituQual, 
            ObservedData expInSituObsData, String inSituObsData, 
            Expression expRNAseqData, String rnaSeqData, DataState expRNAseqQual, String rnaSeqQual, 
            ObservedData expRNAseqObsData, String rnaSeqObsData,
            ObservedData expObservedData, String observedData) {
        log.debug("geneId: {} - expAffyData: {} - affyData: {} - expAffyQual: {} - affyQual: {} "
                + "- expAffyObsData: {} - affyObsData: {} - expESTData: {} - estData: {} "
                + "- expESTQual: {} - estQual: {} - expESTObsData: {} - estObsData: {} "
                + "- expInSituData: {} - inSituData: {} - expInSituQual: {} - inSituQual: {} "
                + "- expInSituObsData: {} - inSituObsData: {} - expRNAseqData: {} - rnaSeqData: {} "
                + "- expRNAseqQual: {} - rnaSeqQual: {} - expRNAseqObsData: {} - rnaSeqObsData: {} "
                + "- expObservedData: {} - observedData",  geneId, expAffyData, affyData,
                expAffyQual, affyQual, expAffyObsData, affyObsData, expESTData, estData, expESTQual,
                estQual, expESTObsData, estObsData, expInSituData, inSituData, expInSituQual,
                inSituQual, expInSituObsData, inSituObsData, expRNAseqData, rnaSeqData,
                expRNAseqQual, rnaSeqQual, expRNAseqObsData, rnaSeqObsData, expObservedData, observedData);
        assertEquals("Incorrect Affymetrix data for " + geneId, expAffyData == null?
                GenerateDownloadFile.NO_DATA_VALUE :
                    GenerateDownloadFile.convertExpressionToString(expAffyData), affyData);
        assertEquals("Incorrect Affymetrix quality for " + geneId, expAffyQual == null? 
                GenerateDownloadFile.NA_VALUE :
            GenerateDownloadFile.convertDataStateToString(expAffyQual), affyQual);
        assertEquals("Incorrect Affymetrix observed data for " + geneId, 
                expAffyObsData.getStringRepresentation(), affyObsData);
        
        assertEquals("Incorrect EST data for " + geneId, expESTData == null?
                GenerateDownloadFile.NO_DATA_VALUE :
                    GenerateDownloadFile.convertExpressionToString(expESTData), estData);
        assertEquals("Incorrect EST quality for " + geneId, expESTQual== null? 
                GenerateDownloadFile.NA_VALUE :
            GenerateDownloadFile.convertDataStateToString(expESTQual), estQual);
        assertEquals("Incorrect EST observed data for " + geneId, 
                expESTObsData.getStringRepresentation(), estObsData);
        assertEquals("Incorrect in situ data for " + geneId, expInSituData == null?
                GenerateDownloadFile.NO_DATA_VALUE :
                    GenerateDownloadFile.convertExpressionToString(expInSituData), inSituData);
        assertEquals("Incorrect in situ quality for " + geneId, expInSituQual== null? 
                GenerateDownloadFile.NA_VALUE :
            GenerateDownloadFile.convertDataStateToString(expInSituQual), inSituQual);
        assertEquals("Incorrect in situ observed data for " + geneId, 
                expInSituObsData.getStringRepresentation(), inSituObsData);
        assertEquals("Incorrect RNA-seq data for " + geneId, expRNAseqData == null?
                GenerateDownloadFile.NO_DATA_VALUE :
                    GenerateDownloadFile.convertExpressionToString(expRNAseqData), rnaSeqData);
        assertEquals("Incorrect RNA-seq quality for " + geneId, expRNAseqQual== null? 
                GenerateDownloadFile.NA_VALUE :
            GenerateDownloadFile.convertDataStateToString(expRNAseqQual), rnaSeqQual);
        assertEquals("Incorrect RNA-seq observed data for " + geneId, 
                expRNAseqObsData.getStringRepresentation(), rnaSeqObsData);
        assertEquals("Incorrect observed data for " + geneId, 
                expObservedData.getStringRepresentation(), observedData);
    }

}
