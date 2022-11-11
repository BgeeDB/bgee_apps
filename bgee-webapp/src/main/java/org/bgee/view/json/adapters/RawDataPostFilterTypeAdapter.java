package org.bgee.view.json.adapters;

import java.io.IOException;
import java.util.Collection;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.URLParameters;
import org.bgee.model.NamedEntity;
import org.bgee.model.expressiondata.rawdata.RawDataPostFilter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public class RawDataPostFilterTypeAdapter extends TypeAdapter<RawDataPostFilter> {
    private static final Logger log = LogManager.getLogger(RawDataPostFilterTypeAdapter.class.getName());

    private final TypeAdaptersUtils utils;
    private final URLParameters urlParameters;

    public RawDataPostFilterTypeAdapter(TypeAdaptersUtils utils,
            URLParameters urlParameters) {
        this.utils = utils;
        this.urlParameters = urlParameters;
    }

    @Override
    public void write(JsonWriter out, RawDataPostFilter value) throws IOException {
        log.traceEntry("{}, {}", out, value);
        if (value == null) {
            out.nullValue();
            log.traceExit(); return;
        }
        out.beginObject();

        out.name("anatEntities");
        this.writePostFilterNamedEntityParameter(out, "Anatomical entities",
                this.urlParameters.getParamAnatEntity().getName(), value.getAnatEntities());

        out.name("cellTypes");
        this.writePostFilterNamedEntityParameter(out, "Cell types",
                this.urlParameters.getParamCellType().getName(), value.getCellTypes());

        out.name("devStages");
        this.writePostFilterNamedEntityParameter(out, "Developmental and life stages",
                this.urlParameters.getParamDevStage().getName(), value.getDevStages());

        out.name("sexes");
        this.writePostFilterStringParameter(out, "Sexes",
                this.urlParameters.getParamSex().getName(),
                value.getSexes().stream().map(s -> s.getStringRepresentation())
                .collect(Collectors.toList()));

        out.name("strains");
        this.writePostFilterStringParameter(out, "Strains",
                this.urlParameters.getParamStrain().getName(),
                value.getStrains());

        out.endObject();
        log.traceExit();
    }

    @Override
    public RawDataPostFilter read(JsonReader in) throws IOException {
        //for now, we never read JSON values
        throw log.throwing(new UnsupportedOperationException("No custom JSON reader for RawDataPostFilter."));
    }

    private void writePostFilterNamedEntityParameter(JsonWriter out, String filterName,
            String urlParameterName, Collection<? extends NamedEntity<String>> values) throws IOException {
        log.traceEntry("{}, {}, {}, {}", out, filterName, urlParameterName, values);

        startWritePostFilterParameter(out, filterName, urlParameterName);
        for (NamedEntity<String> value: values) {
            this.utils.writeSimplifiedNamedEntity(out, value);
        }
        endWritePostFilterParameter(out);

        log.traceExit();
    }
    private void writePostFilterStringParameter(JsonWriter out, String filterName,
            String urlParameterName, Collection<String> values) throws IOException {
        log.traceEntry("{}, {}, {}, {}", out, filterName, urlParameterName, values);

        startWritePostFilterParameter(out, filterName, urlParameterName);
        for (String value: values) {
            out.value(value);
        }
        endWritePostFilterParameter(out);

        log.traceExit();
    }

    private static void startWritePostFilterParameter(JsonWriter out, String filterName,
            String urlParameterName) throws IOException {
        log.traceEntry("{}, {}, {}", out, filterName, urlParameterName);
        out.beginObject();
        out.name("filterName").value(filterName);
        out.name("urlParameterName").value(urlParameterName);
        out.name("values");
        out.beginArray();
        log.traceExit();
    }
    private static void endWritePostFilterParameter(JsonWriter out) throws IOException {
        log.traceEntry("{}", out);
        out.endArray();
        out.endObject();
        log.traceExit();
    }
}