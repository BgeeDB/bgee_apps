package org.bgee.model.topanat;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.TestAncestor;
import org.bgee.model.expressiondata.CallFilter;
import org.bgee.model.expressiondata.baseelements.CallType;
import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.model.expressiondata.baseelements.DecorrelationType;
import org.bgee.model.expressiondata.baseelements.StatisticTest;
import org.bgee.model.expressiondata.baseelements.SummaryQuality;
import org.bgee.model.topanat.exception.MissingParameterException;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link TopAnatParams}.
 * - Get a hash key according to the parameter values
 * - Get {@link CallFilter} corresponding to the parameter values
 * 
 * @author Mathieu Seppey
 * @version Bgee 13, March 2016
 * @since Bgee 13
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
                CallType.Expression.EXPRESSED);
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
    @Test
    public void testGetKey(){
        assertEquals("995e06db82365f141be8847080be4dc1453671f8",this.topAnatParams.getKey());
    }

    /**
     * Test that the generated CallFilter fits the provided parameters
     */
    @Test
    public void testConvertRawParametersToCallFilter(){
        CallFilter<?> callFilter = this.topAnatParams.convertRawParametersToCallFilter();
        assertEquals(callFilter.toString(),"CallFilter [geneFilter=GeneFilter "
                + "[geneIds=[G1, G2, G3, G4]], conditionFilters=[ConditionFilter [anatEntitieIds=[],"
                + " devStageIds=[a]]], dataPropagationFilter=DataPropagation "
                + "[anatEntityPropagationState=SELF, devStagePropagationState=SELF_OR_DESCENDANT, "
                + "includingObservedData=null], callDataFilters=[ExpressionCallData "
                + "[dataType=AFFYMETRIX, callType=EXPRESSED, dataQuality=HIGH, "
                + "dataPropagation=DataPropagation [anatEntityPropagationState=SELF, "
                + "devStagePropagationState=SELF_OR_DESCENDANT, includingObservedData=null]]]]");
    }
}

