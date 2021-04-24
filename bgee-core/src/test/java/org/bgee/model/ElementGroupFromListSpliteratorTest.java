package org.bgee.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.expressiondata.Call.ExpressionCall;
import org.bgee.model.expressiondata.baseelements.SummaryCallType.ExpressionSummary;
import org.bgee.model.gene.Gene;
import org.bgee.model.gene.GeneBioType;
import org.bgee.model.species.Species;
import org.junit.Test;

/**
 * Unit tests for {@link ElementGroupFromListSpliterator}.
 * 
 * @author Frederic Bastian
 * @version Bgee 14 Feb. 2019
 * @since Bgee 14 Feb. 2019
 */
public class ElementGroupFromListSpliteratorTest extends TestAncestor {
    private final static Logger log = LogManager.getLogger(BgeeUtilsTest.class.getName());
    @Override
    protected Logger getLogger() {
        return log;
    }

    @Test
    public void shouldGetGroupedElementStream() {
        //We create a List of Expression Calls ordered by Genes, to be grouped by Genes
        Species spe1 = new Species(1);
        GeneBioType biotype = new GeneBioType("biotype1");
        Gene gene1 = new Gene("id1", spe1, biotype);
        Gene gene2 = new Gene("id2", spe1, biotype);
        ExpressionCall call1 = new ExpressionCall(gene1, null, null, null, null, ExpressionSummary.EXPRESSED, null, null, null);
        ExpressionCall call2 = new ExpressionCall(gene1, null, null, null, null, ExpressionSummary.NOT_EXPRESSED, null, null, null);
        ExpressionCall call3 = new ExpressionCall(gene2, null, null, null, null, ExpressionSummary.EXPRESSED, null, null, null);
        ExpressionCall call4 = new ExpressionCall(gene2, null, null, null, null, ExpressionSummary.NOT_EXPRESSED, null, null, null);
        List<ExpressionCall> calls = Arrays.asList(call1, call2, call3, call4);
        Function<ExpressionCall, Gene> extractGeneFunc = c1 -> c1.getGene();

        ElementGroupFromListSpliterator<ExpressionCall, Gene> spliterator =
                new ElementGroupFromListSpliterator<>(calls.stream(), extractGeneFunc, Gene.COMPARATOR);
        List<List<ExpressionCall>> expectedResults = Arrays.asList(Arrays.asList(call1, call2), Arrays.asList(call3, call4));
        assertEquals("Incorrect grouped Stream produced", expectedResults,
                StreamSupport.stream(spliterator, false).collect(Collectors.toList()));

        //Check that it works with stream with one element in each group
        spliterator = new ElementGroupFromListSpliterator<>(Arrays.asList(call1, call3).stream(),
                extractGeneFunc, Gene.COMPARATOR);
        expectedResults = Arrays.asList(Arrays.asList(call1), Arrays.asList(call3));
        assertEquals("Incorrect grouped Stream produced for 1 element per group", expectedResults,
                StreamSupport.stream(spliterator, false).collect(Collectors.toList()));

        //Check that it works with only one resulting group
        spliterator = new ElementGroupFromListSpliterator<>(Arrays.asList(call1, call2).stream(),
                extractGeneFunc, Gene.COMPARATOR);
        expectedResults = Arrays.asList(Arrays.asList(call1, call2));
        assertEquals("Incorrect grouped Stream produced for 1 group", expectedResults,
                StreamSupport.stream(spliterator, false).collect(Collectors.toList()));

        //Check that it works with only one element
        spliterator = new ElementGroupFromListSpliterator<>(Arrays.asList(call1).stream(),
                extractGeneFunc, Gene.COMPARATOR);
        expectedResults = Arrays.asList(Arrays.asList(call1));
        assertEquals("Incorrect grouped Stream produced for 1 element", expectedResults,
                StreamSupport.stream(spliterator, false).collect(Collectors.toList()));

        //Check that an exception is thrown if we provide an incorrect comparator
        spliterator = new ElementGroupFromListSpliterator<>(calls.stream(), extractGeneFunc,
                (g1, g2) -> g2.getEnsemblGeneId().compareTo(g1.getEnsemblGeneId()));
        try {
            StreamSupport.stream(spliterator, false).collect(Collectors.toList());
            //test failed if no exception was thrown
            fail("An IllegalStateException should have been thrown because of incorrect comparator");
        } catch (IllegalStateException e) {
            //test passed
        }

        //Pretty much the same, but test that an exception is thrown if the source stream is not ordered correctly
        spliterator = new ElementGroupFromListSpliterator<>(Arrays.asList(call3, call4, call1, call2).stream(),
                extractGeneFunc, Gene.COMPARATOR);
        try {
            StreamSupport.stream(spliterator, false).collect(Collectors.toList());
            //test failed if no exception was thrown
            fail("An IllegalStateException should have been thrown because of incorrect comparator");
        } catch (IllegalStateException e) {
            //test passed
        }
    }
}