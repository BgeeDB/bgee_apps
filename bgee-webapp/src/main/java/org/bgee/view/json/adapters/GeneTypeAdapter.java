package org.bgee.view.json.adapters;

import java.io.IOException;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.XRef;
import org.bgee.model.gene.Gene;
import org.bgee.model.gene.GeneBioType;
import org.bgee.model.source.Source;
import org.bgee.model.species.Species;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * A {@code TypeAdapter} to read/write {@code Gene}s in JSON. It is notably to group XRefs
 * per data source in the display.
 */
public final class GeneTypeAdapter extends TypeAdapter<Gene> {
    //TODO: refactor with comparator in HtmlGeneDisplay
    private final static Comparator<XRef> X_REF_COMPARATOR = Comparator
            .<XRef, Integer>comparing(x -> x.getSource().getDisplayOrder(), Comparator.nullsLast(Integer::compareTo))
            .thenComparing(x -> x.getSource().getName(), Comparator.nullsLast(String::compareTo))
            .thenComparing((XRef::getXRefId), Comparator.nullsLast(String::compareTo));

    private final Function<String, String> urlEncodeFunction;
    private final Gson gson;
    private static final Logger log = LogManager.getLogger(GeneTypeAdapter.class.getName());

    protected GeneTypeAdapter(Gson gson, Function<String, String> urlEncodeFunction) {
        this.gson = gson;
        this.urlEncodeFunction = urlEncodeFunction;
    }

    @Override
    public void write(JsonWriter out, Gene value) throws IOException {
        log.traceEntry("{}, {}", out, value);
        if (value == null) {
            out.nullValue();
            log.traceExit(); return;
        }
        out.beginObject();

        out.name("geneId").value(value.getGeneId());
        out.name("name").value(value.getName());
        out.name("description").value(value.getDescription());
        out.name("synonyms");
        this.gson.getAdapter(Set.class).write(out, value.getSynonyms());
        out.name("xRefs");
        this.writeXRefsBySource(out, value.getXRefs());
        out.name("species");
        this.gson.getAdapter(Species.class).write(out, value.getSpecies());
        out.name("geneBioType");
        this.gson.getAdapter(GeneBioType.class).write(out, value.getGeneBioType());
        out.name("geneMappedToSameGeneIdCount").value(value.getGeneMappedToSameGeneIdCount());

        out.endObject();
        log.traceExit();
    }

    @Override
    public Gene read(JsonReader in) throws IOException {
        //for now, we never read JSON values
        throw log.throwing(new UnsupportedOperationException("No custom JSON reader for Gene."));
    }

    private void writeXRefsBySource(JsonWriter out, Set<XRef> xRefs)
            throws IOException {
        log.traceEntry("{}, {}", out, xRefs);
        out.beginArray();

        LinkedHashMap<Source, List<XRef>> xRefsBySource = xRefs.stream()
                .filter(x -> StringUtils.isNotBlank(x.getSource().getXRefUrl()))
                .sorted(X_REF_COMPARATOR)
                .collect(Collectors.groupingBy(XRef::getSource,
                        LinkedHashMap::new,
                        Collectors.toList()));
        for (Entry<Source, List<XRef>> e: xRefsBySource.entrySet()) {
            out.beginObject();

            out.name("source");
            out.beginObject();
            TypeAdaptersUtils.writeSimplifiedSource(out, e.getKey());
            out.endObject();

            out.name("xRefs");
            out.beginArray();
            for (XRef xRef: e.getValue()) {
                out.beginObject();
                TypeAdaptersUtils.writeSimplifiedXRef(out, xRef, urlEncodeFunction);
                out.endObject();
            }
            out.endArray();

            out.endObject();
        }
        out.endArray();
        log.traceExit();
    }
}
