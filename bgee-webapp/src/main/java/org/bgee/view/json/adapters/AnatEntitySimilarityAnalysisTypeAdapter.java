package org.bgee.view.json.adapters;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.anatdev.multispemapping.AnatEntitySimilarity;
import org.bgee.model.anatdev.multispemapping.AnatEntitySimilarityAnalysis;
import org.bgee.model.species.Species;
import org.bgee.model.species.Taxon;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * A {@code TypeAdapter} to read/write {@code AnatEntitySimilarityAnalysis}s in JSON. It is needed because
 * of the complexity of the object and because we want to fine tune the response.
 */
public final class AnatEntitySimilarityAnalysisTypeAdapter extends TypeAdapter<AnatEntitySimilarityAnalysis> {
    private final Gson gson;
    private static final Logger log = LogManager.getLogger(AnatEntitySimilarityAnalysisTypeAdapter.class.getName());

    protected AnatEntitySimilarityAnalysisTypeAdapter(Gson gson) {
        this.gson = gson;
    }

    @Override
    public void write(JsonWriter out, AnatEntitySimilarityAnalysis value) throws IOException {
        log.traceEntry("{}, {}", out, value);
        if (value == null) {
            out.nullValue();
            log.traceExit(); return;
        }
        out.beginObject();

        out.name("leastCommonAncestor");
        this.gson.getAdapter(Taxon.class).write(out, value.getLeastCommonAncestor());

        out.name("unrecognizedAnatEntityIds");
        this.gson.getAdapter(List.class).write(out, value.getRequestedAnatEntityIdsNotFound()
                .stream().sorted().collect(Collectors.toList()));

        out.name("anatEntitesWithNoSimilarityAnnotation");
        List<AnatEntity> noSimAnatEntities = value.getAnatEntitiesWithNoSimilarities().stream()
                .sorted(Comparator.comparing(AnatEntity::getName))
                .collect(Collectors.toList());
        out.beginArray();
        for (AnatEntity anatEntity: noSimAnatEntities) {
            TypeAdaptersUtils.writeSimplifiedNamedEntity(out, anatEntity);
        }
        out.endArray();

        out.name("anatEntitySimilarities");
        out.beginArray();
        for (AnatEntitySimilarity sim: value.getAnatEntitySimilarities()) {
            writeAnatEntitySimilarity(out, sim, value);
        }
        out.endArray();

        out.endObject();
        log.traceExit();
    }

    @Override
    public AnatEntitySimilarityAnalysis read(JsonReader in) throws IOException {
        //for now, we never read JSON values
        throw log.throwing(new UnsupportedOperationException(
                "No custom JSON reader for MultiGeneExprAnalysis."));
    }

    private void writeAnatEntitySimilarity(JsonWriter out, AnatEntitySimilarity sim,
            AnatEntitySimilarityAnalysis result) throws IOException {
        log.traceEntry("{}, {}", sim, result);
        out.beginObject();

        out.name("ancestralTaxon");
        this.gson.getAdapter(Taxon.class).write(out, sim.getTaxonSummaryAncestor());

        out.name("speciesWithAnatEntityPresence");
        List<Species> species = sim.getSourceAnatEntities().stream()
                .map(ae -> result.getAnatEntitiesExistInSpecies().get(ae))
                .flatMap(Set::stream)
                .distinct()
                .filter(sp -> result.getRequestedSpecies().contains(sp))
                .sorted(Comparator.comparing(s -> s.getPreferredDisplayOrder()))
                .collect(Collectors.toList());
        out.beginArray();
        for (Species sp: species) {
            TypeAdaptersUtils.writeSimplifiedSpecies(out, sp, false, null);
        }
        out.endArray();

        out.name("anatEntities");
        List<AnatEntity> anatEntities = sim.getSourceAnatEntities().stream()
                .sorted(Comparator.comparing(AnatEntity::getName))
                .collect(Collectors.toList());
        out.beginArray();
        for (AnatEntity anatEntity: anatEntities) {
            TypeAdaptersUtils.writeSimplifiedNamedEntity(out, anatEntity);
        }
        out.endArray();

        out.endObject();
        log.traceExit();
    }
}
