package org.bgee.view.json.adapters;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.expressiondata.rawdata.RawDataAnnotation;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public class RawDataAnnotationTypeAdapter extends TypeAdapter<RawDataAnnotation> {
    private static final Logger log = LogManager.getLogger(RawDataAnnotationTypeAdapter.class.getName());

    private final TypeAdaptersUtils utils;

    public RawDataAnnotationTypeAdapter(TypeAdaptersUtils utils) {
        this.utils = utils;
    }

    @Override
    public void write(JsonWriter out, RawDataAnnotation value) throws IOException {
        log.traceEntry("{}, {}", out, value);
        if (value == null) {
            out.nullValue();
            log.traceExit(); return;
        }
        out.beginObject();

        out.name("rawDataCondition");
        this.utils.writeSimplifiedRawDataCondition(out, value.getRawDataCondition());
        out.name("annotationSource");
        this.utils.writeSimplifiedSource(out, value.getAnnotationSource());
        out.name("curator").value(value.getCurator());
        out.name("annotationDate").value(value.getAnnotationDate());

        out.endObject();
        log.traceExit();
    }

    @Override
    public RawDataAnnotation read(JsonReader in) throws IOException {
        throw log.throwing(new UnsupportedOperationException("No custom JSON reader for RawDataAnnotation."));
    }
}