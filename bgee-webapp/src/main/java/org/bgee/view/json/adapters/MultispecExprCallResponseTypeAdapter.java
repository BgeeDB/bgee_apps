package org.bgee.view.json.adapters;

import java.io.IOException;
import java.util.EnumSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.CommandData.MultispecExprCallItem;
import org.bgee.controller.CommandData.MultispecExprCallResponse;
import org.bgee.model.expressiondata.baseelements.ConditionParameter;
import org.bgee.model.expressiondata.baseelements.DataType;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public class MultispecExprCallResponseTypeAdapter extends TypeAdapter<MultispecExprCallResponse> {
    private static final Logger log = LogManager.getLogger(MultispecExprCallResponseTypeAdapter.class.getName());

    private final TypeAdaptersUtils utils;

    public MultispecExprCallResponseTypeAdapter(TypeAdaptersUtils utils) {
        this.utils = utils;
    }

    @Override
    public void write(JsonWriter out, MultispecExprCallResponse value) throws IOException {
        log.traceEntry("{}, {}", out, value);
        if (value == null) {
            out.nullValue();
            log.traceExit();
            return;
        }
        out.beginObject();

        out.name("requestedConditionParameters");
        out.beginArray();
        for (ConditionParameter<?, ?> param : value.getCondParams()) {
            out.value(param.getParameterName());
        }
        out.endArray();

        out.name("requestedDataTypes");
        out.beginArray();
        for (DataType d : value.getRequestedDataTypes()) {
            out.value(d.getStringRepresentation());
        }
        out.endArray();

        if (value.getCalls() != null) {
            out.name("expressionCalls");
            out.beginArray();
            for (MultispecExprCallItem item : value.getCalls()) {
                out.beginObject();

                out.name("gene");
                this.utils.writeSimplifiedGene(out, item.getGene(), true, false, null);

                out.name("multiSpeciesCondition");
                this.utils.writeSimplifiedMultiSpeciesCondition(out, item.getMultiSpeciesCondition());

                out.name("expressionScore");
                out.beginObject();
                out.name("expressionScore").value(item.getFormattedExpressionScore());
                out.name("expressionScoreConfidence").value(item.getExpressionScoreConfidence());
                out.endObject();

                out.name("dataTypesWithData");
                out.beginObject();
                for (DataType d : EnumSet.allOf(DataType.class)) {
                    out.name(d.name()).value(Boolean.TRUE.equals(item.getDataTypesWithData().get(d)));
                }
                out.endObject();

                out.name("expressionState").value(item.getExpressionState());
                out.name("expressionQuality").value(item.getExpressionQuality());

                out.endObject();
            }
            out.endArray();
        }

        out.endObject();
        log.traceExit();
    }

    @Override
    public MultispecExprCallResponse read(JsonReader in) throws IOException {
        throw log.throwing(new UnsupportedOperationException("No custom JSON reader for MultispecExprCallResponse."));
    }
}
