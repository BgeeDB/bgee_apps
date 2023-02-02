package org.bgee.view.json.adapters;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.expressiondata.rawdata.baseelements.RawCall;
import org.bgee.model.expressiondata.rawdata.baseelements.RawCall.ExclusionReason;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public class RawCallTypeAdapter extends TypeAdapter<RawCall> {
    private static final Logger log = LogManager.getLogger(RawCallTypeAdapter.class.getName());

    private final TypeAdaptersUtils utils;

    public RawCallTypeAdapter(TypeAdaptersUtils utils) {
        this.utils = utils;
    }

    @Override
    public void write(JsonWriter out, RawCall value) throws IOException {
        log.traceEntry("{}, {}", out, value);
        if (value == null) {
            out.nullValue();
            log.traceExit(); return;
        }
        out.beginObject();

        out.name("gene");
        this.utils.writeSimplifiedGene(out, value.getGene(), true, false, null);
        out.name("pValue");
        if (ExclusionReason.NOT_EXCLUDED != value.getExclusionReason()) {
            out.value("NA");
        } else {
            out.value(value.getFormattedPValue());
        }
        out.name("exclusionReason");
        if (ExclusionReason.NOT_EXCLUDED == value.getExclusionReason()) {
            out.nullValue();
        } else {
            out.value(value.getExclusionReason().getStringRepresentation());
        }

        out.endObject();
        log.traceExit();
    }

    @Override
    public RawCall read(JsonReader in) throws IOException {
        throw log.throwing(new UnsupportedOperationException("No custom JSON reader for RawCall."));
    }
}