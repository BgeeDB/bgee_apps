package org.bgee.view.json.adapters;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.NamedEntity;
import org.bgee.model.anatdev.DevStage;
import org.bgee.model.ontology.Ontology;
import org.bgee.model.ontology.OntologyElement;
import org.bgee.model.ontology.RelationType;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * A {@code TypeAdapter} to read/write an {@code Ontology} in JSON.
 */
public class OntologyTypeAdapter<T extends NamedEntity<U> & OntologyElement<T, U>,
        U extends Comparable<U>> extends TypeAdapter<Ontology<T,U>> {
    private static final Logger log = LogManager.getLogger(OntologyTypeAdapter.class.getName());
    
    private final Gson gson;
    private final Comparator<T> comparator;

    protected OntologyTypeAdapter(Gson gson) {
        this.gson = gson;
        this.comparator = (o1, o2) -> {
            if (o1 == null && o2 == null) {
                return 0;
            }
            if (o1 == null) {
                return 1;
            }
            if (o2 == null) {
                return -1;
            }
            if (o1 instanceof DevStage && o2 instanceof DevStage) {
                DevStage castO1 = (DevStage) o1;
                DevStage castO2 = (DevStage) o2;
                return castO1.getLeftBound() - castO2.getLeftBound();
            }
            return o2.getId().compareTo(o2.getId());
        };
    }

    @Override
    public void write(JsonWriter out, Ontology<T, U> value) throws IOException {
        log.traceEntry("{}, {}", out, value);
        List<T> roots = value.getRootElements(EnumSet.of(RelationType.ISA_PARTOF))
                .stream().collect(Collectors.toList());
        if (roots.isEmpty()) {
            log.traceExit(); return;
        }

        roots.sort(comparator);
        JsonArray arrayOfDesc = new JsonArray();
        for (T root : roots) {
            JsonElement innerContain = convertOntologyToJson(value, root);
            if (innerContain != null) {
                arrayOfDesc.add(innerContain);
            }
        }
        JsonElement jsonElement = gson.toJsonTree(arrayOfDesc);
        gson.toJson(jsonElement, out);
    }

    private JsonElement convertOntologyToJson(Ontology<T,U> ontology, T element) {
        log.traceEntry("{}, {}, {}", ontology, element, comparator);
        JsonArray arrayOfDesc = new JsonArray();
        List<T> descendants = new ArrayList<>(ontology.getDescendants(element, true));
        descendants.sort(comparator);
        for (T desc : descendants) {
            JsonElement innerContain = convertOntologyToJson(ontology, desc);
            if (innerContain != null) {
                arrayOfDesc.add(innerContain);
            }
        }
        JsonElement jsonElement = gson.toJsonTree(element);
        jsonElement.getAsJsonObject().add("descendants", gson.toJsonTree(arrayOfDesc));

        return jsonElement;
    }

    @Override
    public Ontology<T, U> read(JsonReader in) throws IOException {
      //for now, we never read JSON values
        throw log.throwing(new UnsupportedOperationException("No custom JSON reader for Ontology."));
    }
}
