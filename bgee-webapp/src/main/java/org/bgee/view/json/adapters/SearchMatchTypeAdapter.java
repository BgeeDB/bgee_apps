package org.bgee.view.json.adapters;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
 * @param <T>
 */
public final class SearchMatchTypeAdapter<T> extends TypeAdapter<SearchMatch<T>> {
    private final Gson gson;
    private static final Logger log = LogManager.getLogger(SearchMatchTypeAdapter.class.getName());


    protected SearchMatchTypeAdapter(Gson gson) {
        this.gson = gson;
    }

    @Override
    public void write(JsonWriter out, SearchMatch<T> value) throws IOException {
        log.traceEntry("{}, {}", out, value);
        if (value == null) {
            out.nullValue();
            log.traceExit(); return;
        }
        out.beginObject();
        if(Gene.class.isAssignableFrom(value.getType())) {
            out.name("gene");
        } else {
            out.name("object");
        }
        this.gson.getAdapter(value.getType()).write(out, value.getSearchedObject());

        out.name("match").value(value.getMatch());
        out.name("matchSource").value(value.getMatchSource().toString().toLowerCase());

        out.endObject();
        log.traceExit();
    }

    @Override
    public SearchMatch<T> read(JsonReader in) throws IOException {
        //for now, we never read JSON values
        throw log.throwing(new UnsupportedOperationException("No custom JSON reader for SearchMatch."));
    }
}
