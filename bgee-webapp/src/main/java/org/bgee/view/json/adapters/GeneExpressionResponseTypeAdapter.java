package org.bgee.view.json.adapters;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.CommandGene.GeneExpressionResponse;
import org.bgee.model.expressiondata.call.CallService;
import org.bgee.model.expressiondata.call.OTFExpressionCall;
import org.bgee.model.expressiondata.baseelements.DataType;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * A {@code TypeAdapter} to read/write {@code GeneExpressionResponse}s in JSON. It is needed because
 * of the complexity of the object and because we want to fine tune the response.
 */
public final class GeneExpressionResponseTypeAdapter extends TypeAdapter<GeneExpressionResponse> {
    private static final Logger log = LogManager.getLogger(GeneExpressionResponseTypeAdapter.class.getName());

    private final TypeAdaptersUtils utils;

    protected GeneExpressionResponseTypeAdapter(TypeAdaptersUtils utils) {
        this.utils = utils;
    }

    @Override
    public void write(JsonWriter out, GeneExpressionResponse value) throws IOException {
        log.traceEntry("{}, {}", out, value);
        if (value == null) {
            out.nullValue();
            log.traceExit(); return;
        }
        out.beginObject();
        //Retrieve requested condition parameters
        EnumSet<CallService.Attribute> condParams = value.getCondParams();

        out.name("requestedCallType").value(value.getCallType().getStringRepresentation());

        out.name("requestedDataTypes");
        out.beginArray();
        for (DataType d: value.getDataTypes()) {
            out.value(d.getStringRepresentation());
        }
        out.endArray();

        out.name("requestedConditionParameters");
        out.beginArray();
        for (CallService.Attribute a: condParams) {
            out.value(a.getDisplayName());
        }
        out.endArray();

        EnumSet<DataType> dataTypesWithData = EnumSet.noneOf(DataType.class);
        out.name("calls");
        out.beginArray();
        for (OTFExpressionCall call: value.getCalls()) {
            Set<DataType> dataTypes = call.getSupportingDataTypes();
            dataTypesWithData.addAll(dataTypes);
            boolean highQualScore = false;
            //FIXME: commented for testing OTF propagation. Summary quality should be calculated
            // as part of OTF propagation before Bgee 16 release.
//            if (!SummaryQuality.BRONZE.equals(call.getSummaryQuality()) && 
//                    (dataTypes.contains(DataType.RNA_SEQ) ||
//                    dataTypes.contains(DataType.SC_RNA_SEQ) ||
//                    call.getMeanRank().compareTo(BigDecimal.valueOf(20000)) < 0)) {
//                highQualScore = true;
//            }

            out.beginObject();
            out.name("condition");
            Condition2TypeAdapter cond2Adapter = new Condition2TypeAdapter(this.utils);
            cond2Adapter.write(out, call.getCondition(), true);

            out.name("expressionScore");
            out.beginObject();
            out.name("expressionScore").value(call.getExpressionScore());
            out.name("expressionScoreConfidence");
            if (highQualScore) {
                out.value("high");
            } else {
                out.value("low");
            }
            out.endObject();

            String fdr = call.getFormattedAllDatatypePValue();
            out.name("fdr").value(fdr);

            out.name("dataTypesWithData");
//            EnumSet<DataType> dtWithData = call.getCallData().stream()
//                    .filter(c -> c.getDataPropagation() != null &&
//                        c.getDataPropagation().getCondParamCombinations().stream()
//                        .anyMatch(comb -> c.getDataPropagation().getTotalObservationCount(comb) > 0))
//                    .map(c -> c.getDataType())
//                    .collect(Collectors.toCollection(() -> EnumSet.noneOf(DataType.class)));
            out.beginArray();
            for (DataType d: dataTypes) {
                out.value(d.getStringRepresentation());
            }
            out.endArray();
            
            //FIXME: these 3 values are hardcoded for the sake of testing OTF propagation. Should be computed from the data.
            //FIXME: TODO BEFORE BGEE 16 RELEASE
            out.name("expressionState").value(call.getExpressionScore().compareTo(new BigDecimal(0.05)) < 0 ? "not expressed" : "expressed");
            out.name("expressionQuality").value("gold");
            out.name("clusterIndex").value(0);

            out.endObject();
        }
        out.endArray();

        if (!value.getCalls().isEmpty()) {
            assert !dataTypesWithData.isEmpty();
            out.name("gene");
            this.utils.writeSimplifiedGene(out, value.getCalls().iterator().next().getGene(),
                    true, dataTypesWithData);
        }

        out.endObject();
        log.traceExit();
    }

    @Override
    public GeneExpressionResponse read(JsonReader in) throws IOException {
        //for now, we never read JSON values
        throw log.throwing(new UnsupportedOperationException(
                "No custom JSON reader for GeneExpressionResponse."));
    }
}
