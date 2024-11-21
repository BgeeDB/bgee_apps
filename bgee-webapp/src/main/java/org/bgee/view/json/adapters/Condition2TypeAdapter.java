package org.bgee.view.json.adapters;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.expressiondata.baseelements.ConditionParameter;
import org.bgee.model.expressiondata.call.Condition2;
import org.bgee.model.expressiondata.call.ConditionParameterValue;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public class Condition2TypeAdapter extends TypeAdapter<Condition2> {
    private static final Logger log = LogManager.getLogger(Condition2TypeAdapter.class.getName());

    private final TypeAdaptersUtils utils;

    public Condition2TypeAdapter(TypeAdaptersUtils utils) {
        this.utils = utils;
    }

    @Override
    public void write(JsonWriter out, Condition2 value) throws IOException {
        log.traceEntry("{}, {}", out, value);
        if (value == null) {
            out.nullValue();
            log.traceExit(); return;
        }
        out.beginObject();

        for (ConditionParameter<? extends ConditionParameterValue, ?> condParam: ConditionParameter.allOf()) {
            out.name(condParam.getAttributeName());
            this.utils.writeSimplifiedConditionParameterValue(out,
                    value.getConditionParameterValue(condParam));
        }
        out.name("species");
        this.utils.writeSimplifiedSpecies(out, value.getSpecies(), false, null);

        out.endObject();
        log.traceExit();
    }

    @Override
    public Condition2 read(JsonReader in) throws IOException {
        throw log.throwing(new UnsupportedOperationException("No custom JSON reader for Condition2."));
    }

}
