package org.bgee.view.json.adapters;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.expressiondata.baseelements.ConditionParameter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public class ConditionParameterTypeAdapter extends TypeAdapter<ConditionParameter<?, ?>> {
    private static final Logger log = LogManager.getLogger(ConditionParameterTypeAdapter.class.getName());

    @Override
    public void write(JsonWriter out, ConditionParameter<?, ?> value) throws IOException {
        log.traceEntry("{}, {}", out, value);
        if (value == null) {
            out.nullValue();
            log.traceExit(); return;
        }
        out.value(value.getParameterName());
        log.traceExit();
    }

    @Override
    public ConditionParameter<?, ?> read(JsonReader in) throws IOException {
      //for now, we never read JSON values
        throw log.throwing(new UnsupportedOperationException("No custom JSON reader for ConditionParameter."));
    }
}
