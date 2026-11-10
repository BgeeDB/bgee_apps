package org.bgee.view.json.adapters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.TestAncestor;
import org.bgee.controller.CommandData.MultispecExprCallResponse;
import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.model.expressiondata.call.OTFExpressionCall;
import org.bgee.model.expressiondata.call.multispecies.MultiSpeciesCondition;
import org.bgee.model.expressiondata.call.multispecies.SimilarityExpressionCall2;
import org.bgee.model.gene.Gene;
import org.junit.Test;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonWriter;

/**
 * Unit tests for {@link MultispecExprCallResponseTypeAdapter}.
 */
public class MultispecExprCallResponseTypeAdapterTest extends TestAncestor {
    private final static Logger log =
            LogManager.getLogger(MultispecExprCallResponseTypeAdapterTest.class.getName());

    @Override
    protected Logger getLogger() {
        return log;
    }

    /**
     * The descendant score of a similarity is the highest
     * {@code OTFExpressionCall#getBestDescendantExpressionScore()} among its calls, written
     * as a number. The call's own expression score stays the formatted maximum it was.
     */
    @Test
    public void shouldWriteTheHighestDescendantExpressionScore() throws IOException {
        JsonObject score = this.writeScore(call("90.00", "10.00"), call("40.00", "81.40"));

        assertEquals("The expression score stays the formatted maximum of the calls",
                "90.00", score.get("expressionScore").getAsString());
        assertEquals("The descendant score should be the highest one",
                0, new BigDecimal("81.40").compareTo(
                        score.get("bestDescendantExpressionScore").getAsBigDecimal()));

        score = this.writeScore(call("99.00", null), call("1.00", "12.50"));
        assertEquals("A call without a descendant score should not hide the one that has it",
                0, new BigDecimal("12.50").compareTo(
                        score.get("bestDescendantExpressionScore").getAsBigDecimal()));

        score = this.writeScore(call("5.00", "0.00"));
        assertEquals("A descendant score of zero is a score",
                0, BigDecimal.ZERO.compareTo(
                        score.get("bestDescendantExpressionScore").getAsBigDecimal()));
    }

    /**
     * No descendant call means a JSON {@code null}, the same absence the gene page and
     * {@code expr_calls} write.
     */
    @Test
    public void shouldWriteNullWhenNoCallHasADescendantScore() throws IOException {
        JsonObject score = this.writeScore(call("12.00", null), call("3.00", null));

        assertTrue("A similarity with no descendant score should write null",
                score.get("bestDescendantExpressionScore").isJsonNull());
        assertTrue("The field is present so a client can rely on the key",
                score.has("bestDescendantExpressionScore"));
    }

    /**
     * @param calls The {@code OTFExpressionCall}s supporting one similarity.
     * @return      The {@code expressionScore} object written for that similarity.
     */
    private JsonObject writeScore(OTFExpressionCall... calls) throws IOException {
        TypeAdaptersUtils utils = mock(TypeAdaptersUtils.class);
        doAnswer(invocation -> {
            JsonWriter writer = invocation.getArgument(0);
            writer.beginObject();
            writer.endObject();
            return null;
        }).when(utils).writeSimplifiedGene(any(JsonWriter.class), nullable(Gene.class),
                eq(true), eq(false), isNull());
        doAnswer(invocation -> {
            JsonWriter writer = invocation.getArgument(0);
            writer.beginObject();
            writer.endObject();
            return null;
        }).when(utils).writeSimplifiedMultiSpeciesCondition(any(JsonWriter.class),
                nullable(MultiSpeciesCondition.class));

        SimilarityExpressionCall2 similarity = new SimilarityExpressionCall2(
                null, null, List.of(calls));
        MultispecExprCallResponse response = new MultispecExprCallResponse(
                List.of(similarity), new LinkedHashSet<>(), EnumSet.noneOf(DataType.class));

        StringWriter json = new StringWriter();
        new MultispecExprCallResponseTypeAdapter(utils).write(new JsonWriter(json), response);
        return JsonParser.parseString(json.toString()).getAsJsonObject()
                .getAsJsonArray("expressionCalls").get(0).getAsJsonObject()
                .getAsJsonObject("expressionScore");
    }

    /**
     * @param expressionScore                   The expression score of the call.
     * @param bestDescendantExpressionScore     The best descendant expression score of the call,
     *                                          {@code null} when it has none.
     * @return                                  An {@code OTFExpressionCall} carrying those scores.
     */
    private static OTFExpressionCall call(String expressionScore,
            String bestDescendantExpressionScore) {
        return new OTFExpressionCall(null, null, EnumSet.noneOf(DataType.class),
                null, null, null, null, 0,
                null, null,
                BigDecimal.ONE, new BigDecimal(expressionScore),
                bestDescendantExpressionScore == null ? null : BigDecimal.ONE,
                bestDescendantExpressionScore == null ? null : new BigDecimal(bestDescendantExpressionScore),
                null);
    }
}
