package org.bgee.view.json.adapters;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.expressiondata.rawdata.baseelements.ExperimentAssay;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public class ExperimentAssayTypeAdapter extends TypeAdapter<ExperimentAssay> {
    private static final Logger log = LogManager.getLogger(ExperimentAssayTypeAdapter.class.getName());

    private final TypeAdaptersUtils utils;

    public ExperimentAssayTypeAdapter(TypeAdaptersUtils utils) {
        this.utils = utils;
    }

    @Override
    public void write(JsonWriter out, ExperimentAssay value) throws IOException {
        log.traceEntry("{}, {}", out, value);
        if (value == null) {
            out.nullValue();
            log.traceExit(); return;
        }
        this.utils.writeSimplifiedNamedEntity(out, value);
        log.traceExit();
    }

    @Override
    public ExperimentAssay read(JsonReader in) throws IOException {
      //for now, we never read JSON values
        throw log.throwing(new UnsupportedOperationException("No custom JSON reader for ExperimentAssay."));
    }

}
