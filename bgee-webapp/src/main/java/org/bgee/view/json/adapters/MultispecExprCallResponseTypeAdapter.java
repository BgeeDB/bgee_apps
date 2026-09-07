package org.bgee.view.json.adapters;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.CommandData.MultispecExprCallResponse;
import org.bgee.model.expressiondata.baseelements.ConditionParameter;
import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.model.expressiondata.baseelements.ExpressionLevelInfo;
import org.bgee.model.expressiondata.call.Call.ExpressionCall2;
import org.bgee.model.expressiondata.call.CallData.ExpressionCallData2;
import org.bgee.model.expressiondata.call.multispecies.SimilarityExpressionCall2;

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
            for (SimilarityExpressionCall2 call : value.getCalls()) {
                writeSimilarityExpressionCall(out, call);
            }
            out.endArray();
        }

        out.endObject();
        log.traceExit();
    }

    private void writeSimilarityExpressionCall(JsonWriter out, SimilarityExpressionCall2 call)
            throws IOException {
        Optional<ExpressionLevelInfo> maxInfo = call.getCalls().stream()
                .map(ExpressionCall2::getExpressionLevelInfo)
                .filter(Objects::nonNull)
                .filter(eli -> eli.getExpressionScore() != null)
                .max(Comparator.comparing(ExpressionLevelInfo::getExpressionScore,
                        Comparator.nullsFirst(BigDecimal::compareTo)));
        String formattedScore = maxInfo.map(ExpressionLevelInfo::getFormattedExpressionScore)
                .orElse("NA");
        EnumSet<DataType> dataTypesWithData = call.getCalls().stream()
                .flatMap(c -> c.getCallData().stream())
                .map(ExpressionCallData2::getDataType)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(DataType.class)));
        boolean highQual = !dataTypesWithData.isEmpty() && (
                dataTypesWithData.contains(DataType.AFFYMETRIX)
                        || dataTypesWithData.contains(DataType.RNA_SEQ)
                        || dataTypesWithData.contains(DataType.SC_RNA_SEQ)
                        || (maxInfo.isPresent() && maxInfo.get().getExpressionScore() != null
                                && maxInfo.get().getExpressionScore()
                                        .compareTo(BigDecimal.valueOf(20000)) < 0));
        String confidence = highQual ? "high" : "low";
        String expressionState = call.getSummaryCallType() != null
                ? call.getSummaryCallType().toString().toLowerCase() : "not_expressed";
        String quality = call.getCalls().stream()
                .map(ExpressionCall2::getSummaryQuality)
                .filter(Objects::nonNull)
                .findFirst()
                .map(q -> q.toString().toLowerCase())
                .orElse("bronze");

        out.beginObject();

        out.name("gene");
        this.utils.writeSimplifiedGene(out, call.getGene(), true, false, null);

        out.name("multiSpeciesCondition");
        this.utils.writeSimplifiedMultiSpeciesCondition(out, call.getMultiSpeciesCondition());

        out.name("expressionScore");
        out.beginObject();
        out.name("expressionScore").value(formattedScore);
        out.name("expressionScoreConfidence").value(confidence);
        out.endObject();

        out.name("dataTypesWithData");
        out.beginObject();
        for (DataType d : EnumSet.allOf(DataType.class)) {
            out.name(d.name()).value(dataTypesWithData.contains(d));
        }
        out.endObject();

        out.name("expressionState").value(expressionState);
        out.name("expressionQuality").value(quality);

        out.endObject();
    }

    @Override
    public MultispecExprCallResponse read(JsonReader in) throws IOException {
        throw log.throwing(new UnsupportedOperationException("No custom JSON reader for MultispecExprCallResponse."));
    }
}
