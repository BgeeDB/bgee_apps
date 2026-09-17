package org.bgee.view.json.adapters;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.CommandGene.GeneExpressionResponse;
import org.bgee.model.ComposedEntity;
import org.bgee.model.NamedEntity;
import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.dao.api.expressiondata.call.ConditionDAO;
import org.bgee.model.expressiondata.baseelements.ConditionParameter;
import org.bgee.model.expressiondata.baseelements.SummaryCallType.ExpressionSummary;
import org.bgee.model.expressiondata.baseelements.SummaryQuality;
import org.bgee.model.expressiondata.call.CallService;
import org.bgee.model.expressiondata.call.Condition2;
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
            //The summary call type and quality inferred for that call, with the thresholds
            //the calls were filtered with (see CommandGene#loadExpression).
            Entry<ExpressionSummary, SummaryQuality> callTypeQuality =
                    value.getCallTypeQuality(call);
            //XXX: this condition used to also accept a call whose mean rank was better than
            //20000, whatever its data types. The OTF propagation does not compute a rank, so
            //a call supported neither by bulk nor by single-cell RNA-Seq is now always
            //reported with a low confidence.
            boolean highQualScore = callTypeQuality != null &&
                    !SummaryQuality.BRONZE.equals(callTypeQuality.getValue()) &&
                    (dataTypes.contains(DataType.RNA_SEQ) ||
                            dataTypes.contains(DataType.SC_RNA_SEQ));

            out.beginObject();
            out.name("condition");
            this.writeGeneExpressionCondition(out, call.getCondition(), condParams);

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
            
            if (callTypeQuality != null) {
                //Lower case, as the values these two properties had before the OTF propagation
                out.name("expressionState")
                        .value(callTypeQuality.getKey().toString().toLowerCase());
                out.name("expressionQuality")
                        .value(callTypeQuality.getValue().toString().toLowerCase());
            }
            //FIXME: the clustering of the calls was based on their mean rank, which the OTF
            //propagation does not compute. Hardcoded until it is decided what replaces it.
            //FIXME: TODO BEFORE BGEE 16 RELEASE
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

    private void writeGeneExpressionCondition(JsonWriter out, Condition2 condition,
            Collection<CallService.Attribute> requestedCondParams) throws IOException {
        log.traceEntry("{}, {}, {}", out, condition, requestedCondParams);
        if (condition == null) {
            out.nullValue();
            log.traceExit();
            return;
        }

        out.beginObject();

        boolean anatRequested = requestedCondParams == null
                || requestedCondParams.contains(CallService.Attribute.ANAT_ENTITY_ID);
        boolean cellTypeRequested = requestedCondParams == null
                || requestedCondParams.contains(CallService.Attribute.CELL_TYPE_ID);
        boolean devStageRequested = requestedCondParams == null
                || requestedCondParams.contains(CallService.Attribute.DEV_STAGE_ID);
        boolean sexRequested = requestedCondParams == null
                || requestedCondParams.contains(CallService.Attribute.SEX_ID);
        boolean strainRequested = requestedCondParams == null
                || requestedCondParams.contains(CallService.Attribute.STRAIN_ID);

        ComposedEntity<AnatEntity> anatCellValue =
                condition.getConditionParameterValue(ConditionParameter.ANAT_ENTITY_CELL_TYPE);
        if (!anatCellValue.isEmpty()) {
            AnatEntity anatEntity = anatCellValue.size() > 1? anatCellValue.getEntity(1): anatCellValue.getEntity(0);
            AnatEntity cellType = anatCellValue.size() > 1? anatCellValue.getEntity(0): null;
            if (anatRequested) {
                out.name("anatEntity");
                this.utils.writeSimplifiedNamedEntity(out, anatEntity);
            }
            if (cellTypeRequested && cellType != null &&
                    !ConditionDAO.CELL_TYPE_ROOT_ID.equals(cellType.getId())) {
                out.name("cellType");
                this.utils.writeSimplifiedNamedEntity(out, cellType);
            }
        }

        if (devStageRequested && !condition.getConditionParameterValue(ConditionParameter.DEV_STAGE).isEmpty()) {
            out.name(ConditionParameter.DEV_STAGE.getAttributeName());
            this.utils.writeSimplifiedNamedEntity(out,
                    condition.getConditionParameterValue(ConditionParameter.DEV_STAGE).getEntity(0));
        }
        if (sexRequested && !condition.getConditionParameterValue(ConditionParameter.SEX).isEmpty()) {
            out.name(ConditionParameter.SEX.getAttributeName());
            NamedEntity<?> sexEntity = condition.getConditionParameterValue(ConditionParameter.SEX).getEntity(0);
            out.value(sexEntity.getName());
        }
        if (strainRequested && !condition.getConditionParameterValue(ConditionParameter.STRAIN).isEmpty()) {
            out.name(ConditionParameter.STRAIN.getAttributeName());
            NamedEntity<?> strainEntity = condition.getConditionParameterValue(ConditionParameter.STRAIN).getEntity(0);
            out.value(strainEntity.getName());
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
