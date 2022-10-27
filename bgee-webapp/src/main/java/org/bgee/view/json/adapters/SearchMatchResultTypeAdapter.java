package org.bgee.view.json.adapters;

import java.io.IOException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.gene.Gene;
import org.bgee.model.search.SearchMatchResult;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * A {@code TypeAdapter} to read/write {@code SearchMatchResult}s in JSON. It is needed because
 * depending on the type T of {@code SearchMatchResult<T>} the name of the collection of Matches
 * has to be different.
 **/
public class SearchMatchResultTypeAdapter extends TypeAdapter<SearchMatchResult<?>> {
    private final Gson gson;
    private static final Logger log = LogManager.getLogger(SearchMatchResultTypeAdapter.class.getName());


    protected SearchMatchResultTypeAdapter(Gson gson) {
        this.gson = gson;
    }

    @Override
    public void write(JsonWriter out, SearchMatchResult<?> value) throws IOException {
        log.traceEntry("{}, {}", out, value);
        if (value == null) {
            out.nullValue();
            log.traceExit(); return;
        }
        out.beginObject();
        out.name("totalMatchCount").value(value.getTotalMatchCount());
        if(value.getType() == Gene.class) {
            out.name("geneMatches");
            this.gson.getAdapter(List.class).write(out, value.getSearchMatches());
        } else {
            out.name("searchMatches");
            this.gson.getAdapter(List.class).write(out, value.getSearchMatches());
        }
        out.endObject();
        log.traceExit();
    }

    @Override
    public SearchMatchResult<?> read(JsonReader in) throws IOException {
        //for now, we never read JSON values
        throw log.throwing(new UnsupportedOperationException("No custom JSON reader for SearchMatch."));
    }
}
