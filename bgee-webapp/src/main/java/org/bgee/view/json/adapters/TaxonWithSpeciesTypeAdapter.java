package org.bgee.view.json.adapters;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.species.TaxonWithSpecies;
import org.bgee.model.species.Species;
import org.bgee.model.species.Taxon;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public class TaxonWithSpeciesTypeAdapter extends TypeAdapter<TaxonWithSpecies> {
    private static final Logger log = LogManager.getLogger(TaxonWithSpeciesTypeAdapter.class.getName());

    private final Gson gson;
    private final TypeAdaptersUtils utils;

    public TaxonWithSpeciesTypeAdapter(Gson gson, TypeAdaptersUtils utils) {
        this.gson = gson;
        this.utils = utils;
    }

    @Override
    public void write(JsonWriter out, TaxonWithSpecies value) throws IOException {
        log.traceEntry("{}, {}", out, value);
        if (value == null) {
            out.nullValue();
            log.traceExit();
            return;
        }
        out.beginObject();

        out.name("taxon");
        this.gson.getAdapter(Taxon.class).write(out, value.getTaxon());

        if (!value.getSpecies().isEmpty()) {
            out.name("species");
            out.beginArray();
            for (Species species : value.getSpecies()) {
                this.utils.writeSimplifiedSpecies(out, species, false, null);
            }
            out.endArray();
        }

        if (!value.getChildren().isEmpty()) {
            out.name("children");
            out.beginArray();
            for (TaxonWithSpecies child : value.getChildren()) {
                write(out, child);
            }
            out.endArray();
        }

        out.endObject();
        log.traceExit();
    }

    @Override
    public TaxonWithSpecies read(JsonReader in) throws IOException {
        throw log.throwing(new UnsupportedOperationException(
                "No custom JSON reader for TaxonWithSpecies."));
    }
}
