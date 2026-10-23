package org.bgee.model.topanat;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThrows;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.TestAncestor;
import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.model.expressiondata.baseelements.DecorrelationType;
import org.bgee.model.expressiondata.baseelements.StatisticTest;
import org.bgee.model.expressiondata.baseelements.SummaryCallType;
import org.bgee.model.expressiondata.baseelements.SummaryQuality;
import org.bgee.model.topanat.TopAnatService.Background;
import org.bgee.model.topanat.TopAnatService.CallFileType;
import org.bgee.model.topanat.exception.MissingParameterException;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link TopAnatParams}.
 * - Get a hash key according to the parameter values
 * - Get the subset of the generated call files and the background corresponding to the parameter values
 * 
 * @author  Mathieu Seppey
 * @author  Valentine Rech de Laval
 * @version Bgee 14, Mar. 2017
 * @since   Bgee 13
 */
public class TopAnatParamsTest extends TestAncestor {

    private final static Logger log = LogManager.getLogger(TopAnatParamsTest.class.getName());

    @Override
    protected Logger getLogger() {
        return log;
    } 

    /**
     * The {@link TopAnatParams} instance to be tested
     */
    private TopAnatParams topAnatParams;

    /**
     * This method inits the objects needed to run the tests
     * @throws MissingParameterException 
     */
    @Before
    public void initTest() throws MissingParameterException{
        // Set all parameters and build topAnatParams
        TopAnatParams.Builder topAnatParamsBuilder = new TopAnatParams.Builder(
                new HashSet<String>(Arrays.asList("G1","G2")),
                new HashSet<String>(Arrays.asList("G1","G2","G3","G4")), 999,
                SummaryCallType.ExpressionSummary.EXPRESSED);
        topAnatParamsBuilder.summaryQuality(SummaryQuality.GOLD);
        topAnatParamsBuilder.dataTypes(new HashSet<DataType>(Arrays.asList(DataType.RNA_SEQ)));
        topAnatParamsBuilder.decorrelationType(DecorrelationType.ELIM);
        topAnatParamsBuilder.devStageId("a");
        topAnatParamsBuilder.fdrThreshold(1d);
        topAnatParamsBuilder.nodeSize(10);
        topAnatParamsBuilder.numberOfSignificantNode(5);
        topAnatParamsBuilder.pvalueThreshold(1d);
        topAnatParamsBuilder.statisticTest(StatisticTest.FISHER);
        this.topAnatParams = topAnatParamsBuilder.build();
    }
    /**
     * Test that the generated key corresponds to the hash of the parameters
     */
    //FIXME: to reactivate
    @Test
//    @Ignore("to reactivate")
    public void testGetKey(){
        // Value used to generate the key should be [G1, G2][G1, G2, G3, G4]999EXPRESSEDGOLD[RNA_SEQ]aELIMFISHER101.01.05
        // The corresponding sha-1 should be ba8f92c9686a8025da37bf8513a64d43b8d9cff5
        assertEquals("ba8f92c9686a8025da37bf8513a64d43b8d9cff5",this.topAnatParams.getKey());
    }

    /**
     * Test the subset of the generated call files that the analysis reads, and its background.
     */
    @Test
    public void shouldGiveCallFileSubsetAndBackground() {
        //A decorrelation reads the observed calls
        assertEquals(CallFileType.OBSERVED, this.topAnatParams.getCallFileType());
        assertEquals(EnumSet.of(DataType.RNA_SEQ), this.topAnatParams.getDataTypeCombination());
        //Background gene IDs submitted: a custom background
        assertEquals(Background.CUSTOM, this.topAnatParams.getBackground());
    }

    /**
     * Test the default values of the subset of the generated call files and of the background.
     */
    @Test
    public void shouldDefaultToPropagatedCallsAndProteinCodingGenes() throws MissingParameterException {
        TopAnatParams params = new TopAnatParams.Builder(Set.of("G1"), 999,
                SummaryCallType.ExpressionSummary.EXPRESSED).build();
        assertEquals(CallFileType.PROPAGATED, params.getCallFileType());
        assertEquals(EnumSet.allOf(DataType.class), params.getDataTypeCombination());
        assertEquals(Background.PROTEIN_CODING, params.getBackground());
        //Another background is another analysis
        assertNotEquals(params.getKey(), new TopAnatParams.Builder(Set.of("G1"), 999,
                SummaryCallType.ExpressionSummary.EXPRESSED).background(Background.POLY_A).build()
                .getKey());
    }

    /**
     * Test that background gene IDs and a background of Bgee cannot be both provided.
     */
    @Test(expected = IllegalArgumentException.class)
    public void shouldRefuseBackgroundIdsWithBackground() throws MissingParameterException {
        new TopAnatParams.Builder(Set.of("G1"), Set.of("G1", "G2"), 999,
                SummaryCallType.ExpressionSummary.EXPRESSED).background(Background.POLY_A).build();
    }

    /**
     * Test that a custom background requires background gene IDs, which identify it.
     */
    @Test
    public void shouldRequireBackgroundIdsForCustomBackground() throws MissingParameterException {
        assertThrows("A custom background requires background gene IDs",
                IllegalArgumentException.class,
                () -> new TopAnatParams.Builder(Set.of("G1"), 999,
                        SummaryCallType.ExpressionSummary.EXPRESSED)
                        .background(Background.CUSTOM).build());
        //Requested or not, the custom background is the one of the background gene IDs
        assertEquals(new TopAnatParams.Builder(Set.of("G1"), Set.of("G1", "G2"), 999,
                        SummaryCallType.ExpressionSummary.EXPRESSED).build().getKey(),
                new TopAnatParams.Builder(Set.of("G1"), Set.of("G1", "G2"), 999,
                        SummaryCallType.ExpressionSummary.EXPRESSED)
                        .background(Background.CUSTOM).build().getKey());
    }
}
