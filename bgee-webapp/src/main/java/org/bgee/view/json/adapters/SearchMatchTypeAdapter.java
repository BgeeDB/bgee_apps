package org.bgee.view.json.adapters;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.gene.Gene;
import org.bgee.model.search.SearchMatch;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * A {@code TypeAdapter} to read/write {@code SearchMatch}s in JSON. It is needed because
 * the {@code getTerm()} method of {@code SearchMatch} is not always filled, and this is the attribute
 * that GSON would use by default. With this {@code TypeAdapter} we can always use the method
 * {@code getMatch()}.
 */
public final class SearchMatchTypeAdapter extends TypeAdapter<SearchMatch<?>> {
    private final Gson gson;
    private static final Logger log = LogManager.getLogger(SearchMatchTypeAdapter.class.getName());


    protected SearchMatchTypeAdapter(Gson gson) {
        this.gson = gson;
    }

    @Override
    public void write(JsonWriter out, SearchMatch<?> value) throws IOException {
        log.traceEntry("{}, {}", out, value);
        if (value == null) {
            out.nullValue();
            log.traceExit(); return;
        }
        out.beginObject();
        if(value.getType() == Gene.class) {
            out.name("gene");
            this.gson.getAdapter(Gene.class).write(out, (Gene)value.getSearchedObject());
        } else if (value.getType() == AnatEntity.class) {
            out.name("namedEntity");
            this.gson.getAdapter(AnatEntity.class).write(out, (AnatEntity)value.getSearchedObject());
        } else if (value.getType() == String.class) {
            out.name("strain");
            this.gson.getAdapter(String.class).write(out, (String)value.getSearchedObject());
        }
        out.name("match").value(value.getMatch());
        out.name("matchSource").value(value.getMatchSource().toString().toLowerCase());

        out.endObject();
        log.traceExit();
    }

    @Override
    public SearchMatch<Gene> read(JsonReader in) throws IOException {
        //for now, we never read JSON values
        throw log.throwing(new UnsupportedOperationException("No custom JSON reader for SearchMatch."));
    }
}
