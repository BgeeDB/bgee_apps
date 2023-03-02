package org.bgee.view.json.adapters;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.expressiondata.call.Call.ExpressionCall2;
import org.bgee.model.expressiondata.call.CallData.ExpressionCallData2;
import org.bgee.controller.CommandData.ExpressionCallResponse;
import org.bgee.model.expressiondata.baseelements.ConditionParameter;
import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.model.expressiondata.baseelements.SummaryQuality;
import org.bgee.model.expressiondata.call.Condition2;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public class ExpressionCallResponseTypeAdapter extends TypeAdapter<ExpressionCallResponse> {
    private static final Logger log = LogManager.getLogger(ExpressionCallResponseTypeAdapter.class.getName());

    private final Gson gson;
    private final TypeAdaptersUtils utils;

    public ExpressionCallResponseTypeAdapter(Gson gson, TypeAdaptersUtils utils) {
        this.gson = gson;
        this.utils = utils;
    }

    @Override
    public void write(JsonWriter out, ExpressionCallResponse value) throws IOException {
        log.traceEntry("{}, {}", out, value);
        if (value == null) {
            out.nullValue();
            log.traceExit(); return;
        }
        out.beginObject();

        out.name("requestedConditionParameters");
        out.beginArray();
        for (ConditionParameter<?, ?> param: value.getCondParams()) {
            out.value(param.getParameterName());
        }
        out.endArray();

        out.name("requestedDataTypes");
        out.beginArray();
        for (DataType d: value.getRequestedDataTypes()) {
            out.value(d.getStringRepresentation());
        }
        out.endArray();


        if (value.getCalls() != null) {
            out.name("expressionCalls");
            out.beginArray();
            for (ExpressionCall2 call: value.getCalls()) {
                EnumSet<DataType> dataTypes = call.getCallData().stream().map(ExpressionCallData2::getDataType)
                        .collect(Collectors.toCollection(() -> EnumSet.noneOf(DataType.class)));
                boolean highQualScore = false;
                if (!SummaryQuality.BRONZE.equals(call.getSummaryQuality()) &&
                        (dataTypes.contains(DataType.AFFYMETRIX) ||
                                dataTypes.contains(DataType.RNA_SEQ) ||
                                dataTypes.contains(DataType.SC_RNA_SEQ) ||
                                call.getMeanRank().compareTo(BigDecimal.valueOf(20000)) < 0)) {
                    highQualScore = true;
                }
                out.beginObject();

                out.name("gene");
                this.utils.writeSimplifiedGene(out, call.getGene(), true, false, null);
                out.name("condition");
                this.gson.getAdapter(Condition2.class).write(out, call.getCondition());

                out.name("expressionScore");
                out.beginObject();
                out.name("expressionScore").value(call.getFormattedExpressionScore());
                out.name("expressionScoreConfidence");
                if (highQualScore) {
                    out.value("high");
                } else {
                    out.value("low");
                }
                out.endObject();

                String fdr = call.getPValueWithEqualDataTypes(value.getRequestedDataTypes())
                        .getFormattedPValue();
                out.name("fdr").value(fdr);

                out.name("dataTypesWithData");
                //        EnumSet<DataType> dtWithData = call.getCallData().stream()
                //                .filter(c -> c.getDataPropagation() != null &&
                //                    c.getDataPropagation().getCondParamCombinations().stream()
                //                    .anyMatch(comb -> c.getDataPropagation().getTotalObservationCount(comb) > 0))
                //                .map(c -> c.getDataType())
                //                .collect(Collectors.toCollection(() -> EnumSet.noneOf(DataType.class)));
                out.beginObject();
                for (DataType d: EnumSet.allOf(DataType.class)) {
                    out.name(d.name()).value(dataTypes.contains(d));
                }
                out.endObject();

                out.name("expressionState").value(call.getSummaryCallType().toString().toLowerCase());
                out.name("expressionQuality").value(call.getSummaryQuality().toString().toLowerCase());

                out.endObject();
            }
            out.endArray();
        }

        out.endObject();
        log.traceExit();
    }

    @Override
    public ExpressionCallResponse read(JsonReader in) throws IOException {
        throw log.throwing(new UnsupportedOperationException("No custom JSON reader for ExpressionCall2."));
    }
}
