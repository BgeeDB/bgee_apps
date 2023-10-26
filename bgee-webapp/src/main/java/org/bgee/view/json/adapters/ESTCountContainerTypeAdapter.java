package org.bgee.view.json.adapters;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.expressiondata.rawdata.est.ESTCountContainer;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * We create this adapter because an {@code ESTCountContainer} does not have
 * an {@code experimentCount} attribute, while all other count containers extend
 * {@code RawDataCountContainerWithExperiment}, with a {@code experimentCount} attribute.
 * This causes inconsistency in the responses, and we create an adapter to add a "fake"
 * {@code experimentCount} attribute in the response for {@code ESTCountContainer}.
 *
 * @author Frederic Bastian
 * @version Bgee 15.0 Dec. 2022
 * @since Bgee 15.0 Dec. 2022
 */
public class ESTCountContainerTypeAdapter extends TypeAdapter<ESTCountContainer> {
    private static final Logger log = LogManager.getLogger(ESTCountContainerTypeAdapter.class.getName());

    @Override
    public void write(JsonWriter out, ESTCountContainer value) throws IOException {
        log.traceEntry("{}, {}", out, value);
        if (value == null) {
            out.nullValue();
            log.traceExit(); return;
        }
        out.beginObject();

        if (value.getAssayCount() != null) {
            out.name("experimentCount").value(value.getAssayCount());
            out.name("assayCount").value(value.getAssayCount());
        }
        if (value.getCallCount() != null) {
            out.name("callCount").value(value.getCallCount());
        }
        out.name("resultFound").value(value.isResultFound());

        out.endObject();
        log.traceExit();
    }

    @Override
    public ESTCountContainer read(JsonReader in) throws IOException {
      //for now, we never read JSON values
        throw log.throwing(new UnsupportedOperationException("No custom JSON reader for ESTCountContainer."));
    }

}
