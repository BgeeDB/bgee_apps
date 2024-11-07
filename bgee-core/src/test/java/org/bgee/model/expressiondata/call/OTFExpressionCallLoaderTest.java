package org.bgee.model.expressiondata.call;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.anatdev.DevStage;
import org.bgee.model.dao.api.expressiondata.call.CallDAO.CallTO.DataState;
import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.model.expressiondata.baseelements.PropagationState;
import org.bgee.model.expressiondata.rawdata.baseelements.RawCall;
import org.bgee.model.expressiondata.rawdata.baseelements.RawDataAnnotation;
import org.bgee.model.expressiondata.rawdata.baseelements.RawDataCondition;
import org.bgee.model.expressiondata.rawdata.baseelements.RawDataContainer;
import org.bgee.model.expressiondata.rawdata.baseelements.RawDataDataType;
import org.bgee.model.expressiondata.rawdata.baseelements.RawCall.ExclusionReason;
import org.bgee.model.expressiondata.rawdata.baseelements.RawCallSource;
import org.bgee.model.expressiondata.rawdata.baseelements.RawDataCondition.RawDataSex;
import org.bgee.model.expressiondata.rawdata.microarray.AffymetrixChip;
import org.bgee.model.expressiondata.rawdata.microarray.AffymetrixChipPipelineSummary;
import org.bgee.model.expressiondata.rawdata.microarray.AffymetrixContainer;
import org.bgee.model.expressiondata.rawdata.microarray.AffymetrixExperiment;
import org.bgee.model.expressiondata.rawdata.microarray.AffymetrixProbeset;
import org.bgee.model.expressiondata.rawdata.rnaseq.RnaSeqContainer;
import org.bgee.model.expressiondata.rawdata.rnaseq.RnaSeqExperiment;
import org.bgee.model.expressiondata.rawdata.rnaseq.RnaSeqLibrary;
import org.bgee.model.expressiondata.rawdata.rnaseq.RnaSeqLibraryAnnotatedSample;
import org.bgee.model.expressiondata.rawdata.rnaseq.RnaSeqLibraryAnnotatedSamplePipelineSummary;
import org.bgee.model.expressiondata.rawdata.rnaseq.RnaSeqResultAnnotatedSample;
import org.bgee.model.gene.Gene;
import org.bgee.model.gene.GeneBioType;
import org.bgee.model.species.Species;
import org.junit.Test;

public class OTFExpressionCallLoaderTest {
    private static final Logger log = LogManager.getLogger(OTFExpressionCallLoaderTest.class.getName());

    @Test
    public void testComputeExpressionScore() {
        // Define max rank
        BigDecimal maxRank = new BigDecimal(50);

        // Test cases for edge ranks and mid-ranks
        // Rank = 1 (should return 100)
        BigDecimal result = OTFExpressionCallLoader.computeExpressionScore(BigDecimal.ONE, maxRank);
        assertEquals("Rank 1 should return a score of 100", new BigDecimal("100.00000"), result);

        // Rank = maxRank (should return 1)
        result = OTFExpressionCallLoader.computeExpressionScore(maxRank, maxRank);
        assertEquals("Max rank should return a score of 1", new BigDecimal("1.00000"), result);

        // Rank = 25 (middle rank in this case, should return around 50)
        result = OTFExpressionCallLoader.computeExpressionScore(new BigDecimal(25), maxRank);
        assertEquals("Middle rank should return a score of around 50", new BigDecimal("51.51020"), result);
        
        // Rank = 10 (lower part of the range)
        result = OTFExpressionCallLoader.computeExpressionScore(new BigDecimal(10), maxRank);
        assertEquals("Rank 10 should return a score of around 82", new BigDecimal("81.81633"), result);

        // Rank = 40 (upper part of the range)
        result = OTFExpressionCallLoader.computeExpressionScore(new BigDecimal(40), maxRank);
        assertEquals("Rank 40 should return a score of around 20", new BigDecimal("21.20408"), result);
    }

    @Test
    public void testTransformToRawDataPerCondition() {
        AnatEntity anatEntity1 = new AnatEntity("anatEntity1");
        AnatEntity anatEntity2 = new AnatEntity("anatEntity2");
        AnatEntity cellType = new AnatEntity("cellType");
        DevStage devStage1 = new DevStage("devStage1");
        DevStage devStage2 = new DevStage("devStage2");
        RawDataSex sex = RawDataSex.FEMALE;
        String strain = "wild-type";
        Species species = new Species(9606);

        RawDataCondition rawDataCond1 = new RawDataCondition(anatEntity1, devStage1, cellType,
                sex, strain, species);
        RawDataCondition rawDataCond2 = new RawDataCondition(anatEntity2, devStage1, cellType,
                sex, strain, species);
        RawDataCondition rawDataCond3 = new RawDataCondition(anatEntity1, devStage2, cellType,
                sex, strain, species);

        Condition cond1 = new Condition(anatEntity1, null, cellType, null, null, species);
        Condition cond2 = new Condition(anatEntity2, null, cellType, null, null, species);

        Map<RawDataCondition, Condition> rawDataCondToCond = Map.of(
                rawDataCond1, cond1,
                rawDataCond2, cond2,
                rawDataCond3, cond1);

        RawDataAnnotation annot1 = new RawDataAnnotation(rawDataCond1, null, null, null, null, null);
        RawDataAnnotation annot2 = new RawDataAnnotation(rawDataCond2, null, null, null, null, null);
        RawDataAnnotation annot3 = new RawDataAnnotation(rawDataCond3, null, null, null, null, null);

        GeneBioType geneBioType = new GeneBioType("geneBioType");
        Gene gene1 = new Gene("gene1", species, geneBioType);
        RawCall call1 = new RawCall(gene1, new BigDecimal(0.01), DataState.HIGHQUALITY,
                ExclusionReason.NOT_EXCLUDED, new BigDecimal(1));
        RawCall call2 = new RawCall(gene1, new BigDecimal(0.05), DataState.HIGHQUALITY,
                ExclusionReason.NOT_EXCLUDED, new BigDecimal(2));
        RawCall call3 = new RawCall(gene1, new BigDecimal(0.1), DataState.HIGHQUALITY,
                ExclusionReason.NOT_EXCLUDED, new BigDecimal(3));

        AffymetrixExperiment affyExp = new AffymetrixExperiment("AffyExp1", null, null, null, null, 2);
        AffymetrixChip chip1 = new AffymetrixChip("chip1", affyExp, annot1, null, null);
        AffymetrixChip chip2 = new AffymetrixChip("chip2", affyExp, annot2, null, null);
        AffymetrixChip chip3 = new AffymetrixChip("chip3", affyExp, annot3, null, null);
        AffymetrixProbeset probeset1 = new AffymetrixProbeset("probeset1", chip1, call1,
                null, null);
        AffymetrixProbeset probeset1bis = new AffymetrixProbeset("probeset1bis", chip1, call1,
                null, null);
        AffymetrixProbeset probeset2 = new AffymetrixProbeset("probeset2", chip1, call2,
                null, null);
        AffymetrixProbeset probeset3 = new AffymetrixProbeset("probeset3", chip2, call1,
                null, null);
        AffymetrixProbeset probeset4 = new AffymetrixProbeset("probeset4", chip3, call3,
                null, null);
        AffymetrixContainer affyContainer = new AffymetrixContainer(Set.of(affyExp),
                Set.of(chip1, chip2, chip3), Set.of(probeset1, probeset1bis, probeset2, probeset3, probeset4));

        RnaSeqExperiment rnaSeqExp = new RnaSeqExperiment("rnaSeqExp", null, null, null, null,
                null, 1, false);
        RnaSeqLibrary rnaSeqLib = new RnaSeqLibrary("rnaSeqLib", null, null, rnaSeqExp);
        RnaSeqLibraryAnnotatedSample sample = new RnaSeqLibraryAnnotatedSample(rnaSeqLib,
                annot1, null, null);
        RnaSeqResultAnnotatedSample rnaSeqCall = new RnaSeqResultAnnotatedSample(sample, call1,
                null, null, null, null, null);
        RnaSeqContainer rnaSeqContainer = new RnaSeqContainer(Set.of(rnaSeqExp),
                Set.of(rnaSeqLib), Set.of(sample), Set.of(rnaSeqCall));

        Map<RawDataDataType<?, ?>, RawDataContainer<?, ?>> rawDataContainers = Map.of(
                RawDataDataType.AFFYMETRIX, affyContainer,
                RawDataDataType.BULK_RNA_SEQ, rnaSeqContainer);

        //Cannot just do an assertEquals on this expected Map because we don't care about the order
        //of the RawCalls in the Lists, we use a List to not loose in a Set the RawCalls that are equal
//        Map<Condition, Map<RawDataDataType<?, ?>, List<RawCall>>> expectedMap = Map.of(
//                cond1, Map.of(
//                        RawDataDataType.AFFYMETRIX, List.of(probeset1, probeset1bis, probeset2, probeset4),
//                        RawDataDataType.BULK_RNA_SEQ, List.of(rnaSeqCall)),
//                cond2, Map.of(RawDataDataType.AFFYMETRIX, List.of(probeset3)));
        Map<Condition, Map<DataType, List<RawCallSource<?>>>> transformedMap = OTFExpressionCallLoader
                .transformToRawDataPerCondition(rawDataCondToCond, rawDataContainers);
        assertTrue(transformedMap.keySet().equals(Set.of(cond1, cond2)));
        Map<DataType, List<RawCallSource<?>>> dataCond1 = transformedMap.get(cond1);
        assertTrue(dataCond1.keySet().equals(Set.of(DataType.AFFYMETRIX, DataType.RNA_SEQ)));
        List<RawCallSource<?>> affCallsCond1 = dataCond1.get(DataType.AFFYMETRIX);
        assertTrue(affCallsCond1.size() == 4 &&
                affCallsCond1.containsAll(List.of(probeset1, probeset1bis, probeset2, probeset4)));
        List<RawCallSource<?>> rnaSeqCond1 = dataCond1.get(DataType.RNA_SEQ);
        assertTrue(rnaSeqCond1.equals(List.of(rnaSeqCall)));
        Map<DataType, List<RawCallSource<?>>> dataCond2 = transformedMap.get(cond2);
        assertTrue(dataCond2.keySet().equals(Set.of(DataType.AFFYMETRIX)));
        List<RawCallSource<?>> affCallsCond2 = dataCond2.get(DataType.AFFYMETRIX);
        assertTrue(affCallsCond2.equals(List.of(probeset3)));
        log.info("TransformedMap: {}", transformedMap);
    }
    
    @Test
    public void testcomputeMedian() {
        // Test with an odd-sized list
        List<BigDecimal> oddList = Arrays.asList(
            new BigDecimal("0.6"), new BigDecimal("0.5"), new BigDecimal("0.4"), new BigDecimal("0.3"), new BigDecimal("0.2")
        );
        BigDecimal result = OTFExpressionCallLoader.computeMedian(oddList);
        assertEquals("The median multiplied by 2 for odd-sized list should be 0.8", new BigDecimal("0.8"), result);

        // Test with an even-sized list
        List<BigDecimal> evenList = Arrays.asList(
            new BigDecimal("0.01"), new BigDecimal("0.07"), new BigDecimal("0.002"), new BigDecimal("0.01")
        );
        result = OTFExpressionCallLoader.computeMedian(evenList);
        assertEquals("The median multiplied by 2 for even-sized list should be 0.02", new BigDecimal("0.02"), result);
        
     // Test with an odd-sized list
        List<BigDecimal> bigPvalueList = Arrays.asList(
            new BigDecimal("0.9"), new BigDecimal("0.9"), new BigDecimal("0.8"), new BigDecimal("0.9"), new BigDecimal("0.7")
        );
        result = OTFExpressionCallLoader.computeMedian(bigPvalueList);
        assertEquals("The median calculation should return 1 and not above when pValues are too large", new BigDecimal("1"), result);
        
        // Test with a single-element list
        List<BigDecimal> singleElementList = Collections.singletonList(new BigDecimal("0.05"));
        result = OTFExpressionCallLoader.computeMedian(singleElementList);
        assertEquals("The median calculation on a single pValue list should return the same pValue", new BigDecimal("0.05"), result);
    }
    
    @Test
    public void testLoadOTFExpressionCall() {
        AnatEntity anatEntity1 = new AnatEntity("anatEntity1");
        AnatEntity anatEntity2 = new AnatEntity("anatEntity2");
        AnatEntity cellType1 = new AnatEntity("cellType1");
        AnatEntity cellType2 = new AnatEntity("cellType2");
        DevStage devStage1 = new DevStage("devStage1");
        RawDataSex sex = RawDataSex.FEMALE;
        String strain = "wild-type";
        Species species = new Species(9606);

        RawDataCondition rawDataCond1 = new RawDataCondition(anatEntity1, devStage1, cellType1,
                sex, strain, species);
        
        GeneBioType geneBioType = new GeneBioType("geneBioType");
        Gene gene1 = new Gene("gene1", species, geneBioType);
        

        Condition cond1 = new Condition(anatEntity1, null, cellType1, null, null, species);
        Condition cond2 = new Condition(anatEntity2, null, cellType1, null, null, species);
        Condition cond3 = new Condition(anatEntity1, null, cellType2, null, null, species);

        RawDataAnnotation annot1 = new RawDataAnnotation(rawDataCond1, null, null, null, null, null);

        // Initialize raw calls
        RawCall RCaffymetrix = new RawCall(gene1, new BigDecimal("0.20"), DataState.HIGHQUALITY, ExclusionReason.NOT_EXCLUDED, new BigDecimal("200"));
        RawCall RCrnaseq1 = new RawCall(gene1, new BigDecimal("0.25"), DataState.HIGHQUALITY, ExclusionReason.NOT_EXCLUDED, new BigDecimal("1000"));
        RawCall RCrnaseq2 = new RawCall(gene1, new BigDecimal("0.40"), DataState.HIGHQUALITY, ExclusionReason.NOT_EXCLUDED, new BigDecimal("30"));

        AffymetrixChipPipelineSummary affysummary = new AffymetrixChipPipelineSummary(100, new BigDecimal(200), strain, strain, null, new BigDecimal(0.7));
        
        // Initialize experiments and samples
        AffymetrixExperiment affyExp = new AffymetrixExperiment("AffyExp1", null, null, null, null, 1);
        AffymetrixChip chip1 = new AffymetrixChip("chip1", affyExp, null, null, affysummary);
        
        RnaSeqLibraryAnnotatedSamplePipelineSummary rnaseqsummary1 = new RnaSeqLibraryAnnotatedSamplePipelineSummary(new BigDecimal("0.0003"),new BigDecimal("0.01"), new BigDecimal("0.001"), 100000, 95000, new BigDecimal("50000"), 4500);
        RnaSeqLibraryAnnotatedSamplePipelineSummary rnaseqsummary2 = new RnaSeqLibraryAnnotatedSamplePipelineSummary(new BigDecimal("0.0003"),new BigDecimal("0.01"), new BigDecimal("0.001"), 100000, 95000, new BigDecimal("30000"), 17500);
        
        RnaSeqExperiment rnaSeqExp1 = new RnaSeqExperiment("rnaSeqExp1", null, null, null, null, null, 1, false);
        RnaSeqLibrary rnaSeqLib1 = new RnaSeqLibrary("rnaSeqLib1", null, null, rnaSeqExp1);
        RnaSeqLibraryAnnotatedSample sample1 = new RnaSeqLibraryAnnotatedSample(rnaSeqLib1, annot1, rnaseqsummary1, null);

        RnaSeqExperiment rnaSeqExp2 = new RnaSeqExperiment("rnaSeqExp2", null, null, null, null, null, 1, false);
        RnaSeqLibrary rnaSeqLib2 = new RnaSeqLibrary("rnaSeqLib2", null, null, rnaSeqExp2);
        RnaSeqLibraryAnnotatedSample sample2 = new RnaSeqLibraryAnnotatedSample(rnaSeqLib2, annot1, rnaseqsummary2, null);

        // Initialize raw data map
        Map<DataType, List<RawCallSource<?>>> rawData = Map.of(
            DataType.AFFYMETRIX, List.of(
                new AffymetrixProbeset("probeset1", chip1, RCaffymetrix, null, null)
            ),
            DataType.RNA_SEQ, List.of(
                new RnaSeqResultAnnotatedSample(sample1, RCrnaseq1, null, null, null, null, null),
                new RnaSeqResultAnnotatedSample(sample2, RCrnaseq2, null, null, null, null, null)
            )
        );
        
       
        OTFExpressionCall childCall1 = new OTFExpressionCall(gene1, cond2,
                EnumSet.of(DataType.AFFYMETRIX, DataType.RNA_SEQ),
                new BigDecimal("0.10"), new BigDecimal("0.15"),
                new BigDecimal("0.001"), new BigDecimal("0.003"),
                new BigDecimal("20000"), new BigDecimal("12"),
                new BigDecimal("10000"), new BigDecimal("90"),
                PropagationState.SELF_AND_DESCENDANT);
        
        OTFExpressionCall childCall2 = new OTFExpressionCall(gene1, cond3,
                EnumSet.of(DataType.AFFYMETRIX, DataType.RNA_SEQ),
                new BigDecimal("0.50"), new BigDecimal("0.70"),
                new BigDecimal("0.25"), new BigDecimal("0.35"),
                new BigDecimal("10000"), new BigDecimal("15"),
                new BigDecimal("50000"), new BigDecimal("40"),
                PropagationState.SELF_AND_DESCENDANT);

        OTFExpressionCall expectedCall = new OTFExpressionCall(gene1, cond1,
                EnumSet.of(DataType.AFFYMETRIX, DataType.RNA_SEQ),
                new BigDecimal("0.45").setScale(50, RoundingMode.HALF_UP), new BigDecimal("0.55").setScale(50, RoundingMode.HALF_UP), 
                new BigDecimal("0.001"), new BigDecimal("0.003"), 
                new BigDecimal("52100"), new BigDecimal("49.51"), 
                new BigDecimal("10000"), new BigDecimal("90"), 
                PropagationState.SELF_AND_DESCENDANT);

        OTFExpressionCall testCall = OTFExpressionCallLoader.loadOTFExpressionCall(gene1, cond1, rawData, Set.of(childCall1, childCall2));
        
     // Logging attributes for debugging
        log.debug("Expected Call Attributes: Gene = {}, Condition = {}, Supporting Data Types = {}, Trusted DataType P-Value = {}, All DataType P-Value = {}, Best Descendant Trusted DataType P-Value = {}, Best Descendant All DataType P-Value = {}, Expression Score Weight = {}, Expression Score = {}, Best Descendant Expression Score Weight = {}, Best Descendant Expression Score = {}, Propagation State = {}",
                expectedCall.getGene(),
                expectedCall.getCondition(),
                expectedCall.getSupportingDataTypes(),
                expectedCall.getTrustedDataTypePValue(),
                expectedCall.getAllDataTypePValue(),
                expectedCall.getBestDescendantTrustedDataTypePValue(),
                expectedCall.getBestDescendantAllDataTypePValue(),
                expectedCall.getExpressionScoreWeight(),
                expectedCall.getExpressionScore(),
                expectedCall.getBestDescendantExpressionScoreWeight(),
                expectedCall.getBestDescendantExpressionScore(),
                expectedCall.getDataPropagation());

        log.debug("Test Call Attributes: Gene = {}, Condition = {}, Supporting Data Types = {}, Trusted DataType P-Value = {}, All DataType P-Value = {}, Best Descendant Trusted DataType P-Value = {}, Best Descendant All DataType P-Value = {}, Expression Score Weight = {}, Expression Score = {}, Best Descendant Expression Score Weight = {}, Best Descendant Expression Score = {}, Propagation State = {}",
                testCall.getGene(),
                testCall.getCondition(),
                testCall.getSupportingDataTypes(),
                testCall.getTrustedDataTypePValue(),
                testCall.getAllDataTypePValue(),
                testCall.getBestDescendantTrustedDataTypePValue(),
                testCall.getBestDescendantAllDataTypePValue(),
                testCall.getExpressionScoreWeight(),
                testCall.getExpressionScore(),
                testCall.getBestDescendantExpressionScoreWeight(),
                testCall.getBestDescendantExpressionScore(),
                testCall.getDataPropagation());
        
        assertEquals(expectedCall, testCall);

        
    }
}

