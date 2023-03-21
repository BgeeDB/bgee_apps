package org.bgee.view.json.adapters;

import java.io.IOException;
import java.util.Collection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.URLParameters;
import org.bgee.model.NamedEntity;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonWriter;

public abstract class PostFilterTypeAdapter<T> extends TypeAdapter<T> {
    private static final Logger log = LogManager.getLogger(PostFilterTypeAdapter.class.getName());

    protected final TypeAdaptersUtils utils;
    protected final URLParameters urlParameters;

    public PostFilterTypeAdapter(TypeAdaptersUtils utils,
            URLParameters urlParameters) {
        this.utils = utils;
        this.urlParameters = urlParameters;
    }

    protected void writePostFilterNamedEntityParameter(JsonWriter out, String filterName,
            String urlParameterName, Collection<? extends NamedEntity<?>> values) throws IOException {
        log.traceEntry("{}, {}, {}, {}", out, filterName, urlParameterName, values);
        this.writePostFilterNamedEntityParameter(out, filterName, urlParameterName, values, true, true);
        log.traceExit();
    }
    protected void writePostFilterNamedEntityParameter(JsonWriter out, String filterName,
            String urlParameterName, Collection<? extends NamedEntity<?>> values,
                    boolean informativeId, boolean informativeName) throws IOException {
        log.traceEntry("{}, {}, {}, {}, {}, {}", out, filterName, urlParameterName, values,
                informativeId, informativeName);

        startWritePostFilterParameter(out, filterName, urlParameterName, informativeId, informativeName);
        for (NamedEntity<?> value: values) {
            this.utils.writeSimplifiedNamedEntity(out, value);
        }
        endWritePostFilterParameter(out);

        log.traceExit();
    }

    protected static void startWritePostFilterParameter(JsonWriter out, String filterName,
            String urlParameterName, boolean informativeId, boolean informativeName) throws IOException {
        log.traceEntry("{}, {}, {}, {}, {}", out, filterName, urlParameterName,
                informativeId, informativeName);
        out.beginObject();
        out.name("filterName").value(filterName);
        out.name("urlParameterName").value(urlParameterName);
        out.name("informativeId").value(informativeId);
        out.name("informativeName").value(informativeName);
        out.name("values");
        out.beginArray();
        log.traceExit();
    }
    protected static void endWritePostFilterParameter(JsonWriter out) throws IOException {
        log.traceEntry("{}", out);
        out.endArray();
        out.endObject();
        log.traceExit();
    }
}
