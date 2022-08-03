package org.bgee.view.json.adapters;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.gene.Gene;
import org.bgee.model.gene.GeneMatch;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * A {@code TypeAdapter} to read/write {@code GeneMatch}s in JSON. It is needed because
 * the {@code getTerm()} method of {@code GeneMatch} is not always filled, and this is the attribute
 * that GSON would use by default. With this {@code TypeAdapter} we can always use the method
 * {@code getMatch()}.
 */
public final class GeneMatchTypeAdapter extends TypeAdapter<GeneMatch> {
    private final Gson gson;
    
    private static final Logger log = LogManager.getLogger(GeneMatchTypeAdapter.class.getName());


    protected GeneMatchTypeAdapter(Gson gson) {
        this.gson = gson;
    }

    @Override
    public void write(JsonWriter out, GeneMatch value) throws IOException {
        log.traceEntry("{}, {}", out, value);
        if (value == null) {
            out.nullValue();
            log.traceExit(); return;
        }
        out.beginObject();

        out.name("gene");
        this.gson.getAdapter(Gene.class).write(out, value.getGene());
        out.name("match").value(value.getMatch());
        out.name("matchSource").value(value.getMatchSource().toString().toLowerCase());

        out.endObject();
        log.traceExit();
    }

    @Override
    public GeneMatch read(JsonReader in) throws IOException {
        //for now, we never read JSON values
        throw log.throwing(new UnsupportedOperationException("No custom JSON reader for GeneMatch."));
    }
}
