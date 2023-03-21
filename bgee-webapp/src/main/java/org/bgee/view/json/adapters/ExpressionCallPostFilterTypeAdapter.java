package org.bgee.view.json.adapters;

import java.io.IOException;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.URLParameters;
import org.bgee.model.NamedEntity;
import org.bgee.model.expressiondata.baseelements.ConditionParameter;
import org.bgee.model.expressiondata.call.ExpressionCallPostFilter;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public class ExpressionCallPostFilterTypeAdapter extends PostFilterTypeAdapter<ExpressionCallPostFilter> {
    private static final Logger log = LogManager.getLogger(ExpressionCallPostFilterTypeAdapter.class.getName());

    public ExpressionCallPostFilterTypeAdapter(TypeAdaptersUtils utils,
            URLParameters urlParameters) {
        super(utils, urlParameters);
    }

    @Override
    public void write(JsonWriter out, ExpressionCallPostFilter value) throws IOException {
        log.traceEntry("{}, {}", out, value);
        if (value == null) {
            out.nullValue();
            log.traceExit(); return;
        }
        out.beginObject();

        for (ConditionParameter<?, ?> condParam: ConditionParameter.allOf()) {
            Set<? extends NamedEntity<?>> values = value.getEntities(condParam);
            if (!values.isEmpty()) {
                out.name(condParam.getAttributeName());
                this.writePostFilterNamedEntityParameter(out, condParam.getDisplayName(),
                        condParam.getRequestFilterParameterName(), values,
                        condParam.isInformativeId(),
                        true);
            }
        }

        out.endObject();
        log.traceExit();
    }

    @Override
    public ExpressionCallPostFilter read(JsonReader in) throws IOException {
        //for now, we never read JSON values
        throw log.throwing(new UnsupportedOperationException("No custom JSON reader for ExpressionCallPostFilter."));
    }
}
