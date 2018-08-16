package org.bgee.model.topanat;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.TestAncestor;
import org.bgee.model.expressiondata.CallFilter;
import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.model.expressiondata.baseelements.DecorrelationType;
import org.bgee.model.expressiondata.baseelements.StatisticTest;
import org.bgee.model.expressiondata.baseelements.SummaryCallType;
import org.bgee.model.expressiondata.baseelements.SummaryQuality;
import org.bgee.model.topanat.exception.MissingParameterException;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Unit tests for {@link TopAnatParams}.
 * - Get a hash key according to the parameter values
 * - Get {@link CallFilter} corresponding to the parameter values
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
        topAnatParamsBuilder.dataTypes(new HashSet<DataType>(Arrays.asList(DataType.AFFYMETRIX)));
        topAnatParamsBuilder.decorrelationType(DecorrelationType.ELIM);
        topAnatParamsBuilder.devStageId("a");
        topAnatParamsBuilder.fdrThreshold(1);
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
        // Value used to generate the key should be [G1, G2][G1, G2, G3, G4]999EXPRESSEDGOLD[AFFYMETRIX]aELIMFISHER101.01.05
        // The corresponding sha-1 should be 957bffc28bde2338dcb4e7102a1cbc85562bccb5
        assertEquals("957bffc28bde2338dcb4e7102a1cbc85562bccb5",this.topAnatParams.getKey());
    }

    /**
     * Test that the generated CallFilter fits the provided parameters
     */
    @Test
    public void testConvertRawParametersToCallFilter(){
        CallFilter<?, ?> callFilter = this.topAnatParams.convertRawParametersToCallFilter();
        assertEquals("ExpressionCallFilter [callObservedData={}, anatEntityObservedData=true,"
                + " devStageObservedData=null, geneFilters=[GeneFilter [speciesId=999,"
                + " geneIds=[G1, G2, G3, G4]]], conditionFilters=[ConditionFilter"
                + " [anatEntityIds=[], devStageIds=[a], observedConditions=null]],"
                + " dataTypeFilters=[AFFYMETRIX], summaryCallTypeQualityFilter={EXPRESSED=GOLD}]",
                callFilter.toString());
    }
}

