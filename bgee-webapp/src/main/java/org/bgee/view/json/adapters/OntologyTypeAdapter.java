package org.bgee.view.json.adapters;

import java.io.IOException;
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
import com.google.gson.GsonBuilder;
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

    private final Gson gson;
    private static final Logger log = LogManager.getLogger(OntologyTypeAdapter.class.getName());

    protected OntologyTypeAdapter(Gson gson) {
        this.gson = gson;
    }

    @Override
    public void write(JsonWriter out, Ontology<T, U> value) throws IOException {
        log.traceEntry("{}, {}", out, value);
        List<T> elements = value.getRootElements(EnumSet.of(RelationType.ISA_PARTOF))
                .stream().collect(Collectors.toList());
        Class<?> type = elements == null ? NamedEntity.class : 
            elements.get(0).getClass();
        @SuppressWarnings("unchecked")
        Comparator<T> comparator = type == DevStage.class?
                (Comparator<T>)Comparator.comparing(DevStage::getLeftBound) :
                    (Comparator<T>)Comparator.comparing(T::getId);
        elements.sort(comparator);
        JsonArray arrayOfDesc = new JsonArray();
        for (T desc : elements) {
            JsonElement innerContain = convertOntologyToJson(value, desc, comparator);
            if (innerContain != null) {
                arrayOfDesc.add(innerContain);
            }
        }
        JsonElement jsonElement = gson.toJsonTree(arrayOfDesc);
        gson.toJson(jsonElement, out);
    }

    private JsonElement convertOntologyToJson(Ontology<T,U> ontology, T element, Comparator<T> comparator) {
        log.traceEntry("{}, {}, {}", ontology, element, comparator);
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.setPrettyPrinting().create();
        JsonArray arrayOfDesc = new JsonArray();
        List<T> descendants = ontology.getDescendants(element, true).stream().collect(Collectors.toList());
        descendants.sort(comparator);
        for (T desc : descendants) {
            JsonElement innerContain = convertOntologyToJson(ontology, desc, comparator);
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
