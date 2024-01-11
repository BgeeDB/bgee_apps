package org.bgee.view.json.adapters;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.CommandGene.SpeciesGeneListResponse;
import org.bgee.model.gene.Gene;
import org.bgee.model.gene.GeneBioType;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public class SpeciesGeneListResponseTypeAdapter extends TypeAdapter<SpeciesGeneListResponse> {
    private static final Logger log = LogManager.getLogger(SpeciesGeneListResponseTypeAdapter.class.getName());

    private final Gson gson;
    private final TypeAdaptersUtils utils;

    protected SpeciesGeneListResponseTypeAdapter(Gson gson, TypeAdaptersUtils utils) {
        this.gson = gson;
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
            this.writeGene(out, gene);
        }
        out.endArray();

        out.endObject();
        log.traceExit();
    }

    /**
     * Method to write some but not all info for a {@code Gene}.
     * We do not rely on {@link TypeAdaptersUtils#writeSimplifiedGene(
     * JsonWriter, Gene, boolean, boolean, EnumSet)}, since we want to display the {@code GeneBioType}
     * and need access to the {@code Gson} object for that. We do not rely on
     * {@link GeneTypeAdapter#write(JsonWriter, Gene)} since this would display too much info.
     *
     * @param out
     * @param value
     * @throws IOException
     */
    private void writeGene(JsonWriter out, Gene value) throws IOException {
        log.traceEntry("{}, {}", out, value);
        out.beginObject();
        out.name("geneId").value(value.getGeneId());
        out.name("name").value(value.getName());
        out.name("description").value(value.getDescription());

        out.name("geneBioType");
        this.gson.getAdapter(GeneBioType.class).write(out, value.getGeneBioType());

        out.name("geneMappedToSameGeneIdCount").value(value.getGeneMappedToSameGeneIdCount());

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
