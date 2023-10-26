package org.bgee.view.json.adapters;

import java.io.IOException;
import java.util.function.Function;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.XRef;
import org.bgee.model.gene.GeneXRef;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * A {@code TypeAdapter} to read/write {@code GeneHomologs}s in JSON. It is notably to be able
 * to call the method {@code getXRefUrl()}.
 */
public final class XRefTypeAdapter extends TypeAdapter<XRef> {
    private static final Logger log = LogManager.getLogger(XRefTypeAdapter.class.getName());

    private final Function<String, String> urlEncodeFunction;
    private final TypeAdaptersUtils utils;

    public XRefTypeAdapter(Function<String, String> urlEncodeFunction,
            TypeAdaptersUtils utils) {
        this.urlEncodeFunction = urlEncodeFunction;
        this.utils = utils;
    }

    @Override
    public void write(JsonWriter out, XRef value) throws IOException {
        log.traceEntry("{}, {}", out, value);
        if (value == null) {
            out.nullValue();
            log.traceExit(); return;
        }
        out.beginObject();

        this.utils.writeSimplifiedXRef(out, value, urlEncodeFunction);

        //Simplified display of Source inside XRefs
        out.name("source");
        out.beginObject();
        this.utils.writeSimplifiedSource(out, value.getSource());
        out.endObject();

        out.endObject();
        log.traceExit();
    }

    @Override
    public GeneXRef read(JsonReader in) throws IOException {
        //for now, we never read JSON values
        throw log.throwing(new UnsupportedOperationException("No custom JSON reader for GeneXRef."));
    }
}
