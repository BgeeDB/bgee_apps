package org.bgee.view.json.adapters;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.expressiondata.rawdata.RawCall;

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
        this.utils.writeSimplifiedGene(out, value.getGene(), false, false, null);
        out.name("pValue").value(value.getPValue());
        out.name("exclusionReason").value(value.getExclusionReason().getStringRepresentation());

        out.endObject();
        log.traceExit();
    }

    @Override
    public RawCall read(JsonReader in) throws IOException {
        throw log.throwing(new UnsupportedOperationException("No custom JSON reader for RawCall."));
    }
}