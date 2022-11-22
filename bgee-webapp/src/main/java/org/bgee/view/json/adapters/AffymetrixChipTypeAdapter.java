package org.bgee.view.json.adapters;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.expressiondata.rawdata.baseelements.RawDataAnnotation;
import org.bgee.model.expressiondata.rawdata.microarray.AffymetrixChip;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public class AffymetrixChipTypeAdapter extends TypeAdapter<AffymetrixChip> {
    private static final Logger log = LogManager.getLogger(AffymetrixChipTypeAdapter.class.getName());

    private final Gson gson;
    private final TypeAdaptersUtils utils;

    public AffymetrixChipTypeAdapter(Gson gson, TypeAdaptersUtils utils) {
        this.gson = gson;
        this.utils = utils;
    }

    @Override
    public void write(JsonWriter out, AffymetrixChip value) throws IOException {
        log.traceEntry("{}, {}", out, value);
        if (value == null) {
            out.nullValue();
            log.traceExit(); return;
        }
        out.beginObject();

        out.name("id").value(value.getId());
        out.name("experiment");
        this.utils.writeSimplifiedNamedEntity(out, value.getExperiment());
        out.name("annotation");
        this.gson.getAdapter(RawDataAnnotation.class).write(out, value.getAnnotation());

        out.endObject();
        log.traceExit();
    }

    @Override
    public AffymetrixChip read(JsonReader in) throws IOException {
        throw log.throwing(new UnsupportedOperationException("No custom JSON reader for AffymetrixChip."));
    }
}