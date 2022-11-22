package org.bgee.view.json.adapters;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.expressiondata.rawdata.baseelements.RawCall;
import org.bgee.model.expressiondata.rawdata.microarray.AffymetrixChip;
import org.bgee.model.expressiondata.rawdata.microarray.AffymetrixProbeset;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public class AffymetrixProbesetTypeAdapter extends TypeAdapter<AffymetrixProbeset> {
    private static final Logger log = LogManager.getLogger(AffymetrixProbesetTypeAdapter.class.getName());

    private final Gson gson;

    public AffymetrixProbesetTypeAdapter(Gson gson) {
        this.gson = gson;
    }

    @Override
    public void write(JsonWriter out, AffymetrixProbeset value) throws IOException {
        log.traceEntry("{}, {}", out, value);
        if (value == null) {
            out.nullValue();
            log.traceExit(); return;
        }
        out.beginObject();

        out.name("id").value(value.getId());
        out.name("assay");
        this.gson.getAdapter(AffymetrixChip.class).write(out, value.getAssay());
        out.name("normalizedSignalIntensity").value(value.getNormalizedSignalIntensity());
        out.name("expressionCall");
        this.gson.getAdapter(RawCall.class).write(out, value.getRawCall());

        out.endObject();
        log.traceExit();
    }

    @Override
    public AffymetrixProbeset read(JsonReader in) throws IOException {
        throw log.throwing(new UnsupportedOperationException("No custom JSON reader for AffymetrixProbeset."));
    }
}
