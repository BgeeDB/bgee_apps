package org.bgee.view.json.adapters;

import java.io.IOException;
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
import org.bgee.model.expressiondata.call.OTFExpressionCall;
import org.bgee.model.expressiondata.baseelements.ExpressionLevelInfo;
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
        //An OTFExpressionCall exposes its expression score directly, there is no
        //ExpressionLevelInfo wrapper any more.
        Optional<OTFExpressionCall> maxScoreCall = call.getCalls().stream()
                .filter(c -> c.getExpressionScore() != null)
                .max(Comparator.comparing(OTFExpressionCall::getExpressionScore));
        //Formatted by OTFExpressionCall, the single place doing it.
        String formattedScore = maxScoreCall
                .map(OTFExpressionCall::getFormattedExpressionScore)
                .orElse("NA");
        EnumSet<DataType> dataTypesWithData = call.getCalls().stream()
                .flatMap(c -> c.getSupportingDataTypes().stream())
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(DataType.class)));
        //Single definition of that rule, shared with the single-species responses. The former
        //rule of this endpoint also accepted AFFYMETRIX, dropped from DataType, or a score
        //below 20000, which was a rank threshold: an OTF expression score runs from 0 to 100
        //with the opposite convention, so that clause would have made every call "high".
        //The data types are the union over the calls supporting this similarity.
        boolean highQual = OTFExpressionCall.isHighConfidenceExpressionScore(
                call.getSummaryQuality(), dataTypesWithData);
        String confidence = highQual ? "high" : "low";
        String expressionState = call.getSummaryCallType() != null
                ? call.getSummaryCallType().toString().toLowerCase() : "not_expressed";
        //The quality is inferred from the p-values of the supporting calls by
        //SimilarityExpressionCall2, an OTFExpressionCall carrying none of its own.
        String quality = call.getSummaryQuality() != null
                ? call.getSummaryQuality().toString().toLowerCase() : "bronze";

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
