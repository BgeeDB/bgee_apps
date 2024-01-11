package org.bgee.view.json.adapters;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.CommandGene.SpeciesGeneListResponse;
import org.bgee.model.gene.Gene;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public class SpeciesGeneListResponseTypeAdapter extends TypeAdapter<SpeciesGeneListResponse> {
    private static final Logger log = LogManager.getLogger(SpeciesGeneListResponseTypeAdapter.class.getName());

    private final TypeAdaptersUtils utils;

    protected SpeciesGeneListResponseTypeAdapter(TypeAdaptersUtils utils) {
        this.utils = utils;
    }

    @Override
    public void write(JsonWriter out, SpeciesGeneListResponse value) throws IOException {
        log.traceEntry("{}, {}", out, value);
        if (value == null) {
            out.nullValue();
            log.traceExit(); return;
        }
        out.beginObject();

        out.name("species");
        this.utils.writeSimplifiedSpecies(out, value.getSpecies(), false, null);

        out.name("genes");
        out.beginArray();
        for (Gene gene: value.getGenes()) {
            this.utils.writeSimplifiedGene(out, gene, false, false, null);
        }
        out.endArray();

        out.endObject();
        log.traceExit();
    }

    @Override
    public SpeciesGeneListResponse read(JsonReader in) throws IOException {
        //for now, we never read JSON values
        throw log.throwing(new UnsupportedOperationException(
                "No custom JSON reader for SpeciesGeneListResponse."));
    }

}
