package org.bgee.view.json.adapters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.TestAncestor;
import org.bgee.controller.RequestParameters;
import org.bgee.model.expressiondata.baseelements.SummaryCallType.ExpressionSummary;
import org.bgee.model.topanat.TopAnatParams;
import org.bgee.model.topanat.TopAnatResults;
import org.bgee.model.topanat.TopAnatResults.TopAnatResultRow;
import org.junit.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonWriter;

/**
 * Unit tests for {@link TopAnatResultsTypeAdapter}.
 *
 * @author  Julien Wollbrett
 * @version Bgee 16
 * @since   Bgee 16
 */
public class TopAnatResultsTypeAdapterTest extends TestAncestor {
    private final static Logger log =
            LogManager.getLogger(TopAnatResultsTypeAdapterTest.class.getName());

    @Override
    protected Logger getLogger() {
        return log;
    }

    /**
     * Test the rows written: those of the results, flagged when the results keep the conditions
     * redundant with a more precise one, the JSON being unchanged otherwise.
     */
    @Test
    public void shouldWriteTheRowsOfTheResults() throws IOException {
        TopAnatResultRow kept = row("CL:0002609-UBERON:0000956", false);
        TopAnatResultRow redundant = row("CL:0002609-UBERON:0000955", true);
        TopAnatResultRow other = row("UBERON:0003499", false);

        JsonArray rows = this.write(results(false, List.of(kept, other)));
        assertEquals("Incorrect rows", List.of(kept.getAnatEntitiesId(), other.getAnatEntitiesId()),
                ids(rows));
        for (JsonElement row: rows) {
            assertFalse("No flag should be written when the redundant conditions are left out",
                    row.getAsJsonObject().has("redundant"));
        }

        rows = this.write(results(true, List.of(kept, redundant, other)));
        assertEquals("Incorrect rows", List.of(kept.getAnatEntitiesId(),
                redundant.getAnatEntitiesId(), other.getAnatEntitiesId()), ids(rows));
        List<Boolean> flags = new ArrayList<>();
        for (JsonElement row: rows) {
            flags.add(row.getAsJsonObject().get("redundant").getAsBoolean());
        }
        assertEquals("Incorrect flags of the redundant conditions", List.of(false, true, false), flags);
    }

    /**
     * @return  The JSON rows written for {@code results}.
     */
    private JsonArray write(TopAnatResults results) throws IOException {
        StringWriter json = new StringWriter();
        new TopAnatResultsTypeAdapter(new RequestParameters()).write(new JsonWriter(json), results);
        return JsonParser.parseString(json.toString()).getAsJsonObject().getAsJsonArray("results");
    }
    /**
     * @return  The IDs of the conditions of the JSON {@code rows}.
     */
    private static List<String> ids(JsonArray rows) {
        List<String> ids = new ArrayList<>();
        for (JsonElement row: rows) {
            ids.add(row.getAsJsonObject().get("anatEntityId").getAsString());
        }
        return ids;
    }
    /**
     * @return  {@code TopAnatResults} returning {@code rows}, keeping the conditions redundant
     *          with a more precise one or not.
     */
    private static TopAnatResults results(boolean keepRedundant, List<TopAnatResultRow> rows)
            throws IOException {
        TopAnatParams params = mock(TopAnatParams.class);
        when(params.getKey()).thenReturn("key");
        when(params.getDevStageId()).thenReturn("UBERON:0000104");
        when(params.getCallType()).thenReturn(ExpressionSummary.EXPRESSED);
        TopAnatResults results = mock(TopAnatResults.class);
        when(results.getTopAnatParams()).thenReturn(params);
        when(results.isKeepRedundant()).thenReturn(keepRedundant);
        when(results.getRows()).thenReturn(rows);
        return results;
    }

    /**
     * @return  A row of the results of the condition {@code conditionId}, read as from
     *          the result file.
     */
    private static TopAnatResultRow row(String conditionId, boolean redundant) {
        Map<String, Object> line = new HashMap<>();
        line.put("OrganId", conditionId);
        line.put("OrganName", "name of " + conditionId);
        line.put("Annotated", 8871.0);
        line.put("Significant", 93.0);
        line.put("Expected", 46.2);
        line.put("foldEnrichment", 2.01);
        line.put("p", 9.58e-23);
        line.put("fdr", 3.68e-20);
        line.put("redundant", redundant);
        return new TopAnatResultRow(line);
    }
}
