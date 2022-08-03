package org.bgee.view.json.adapters;

import java.io.IOException;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.RequestParameters;
import org.bgee.model.gene.Gene;
import org.bgee.model.gene.GeneHomologs;
import org.bgee.model.gene.GeneXRef;
import org.bgee.model.species.Taxon;
import org.bgee.view.json.JsonParentDisplay;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * A {@code TypeAdapter} to read/write {@code GeneHomologs}s in JSON. It is a complex object
 * notably with {@code Map}s.
 */
public final class GeneHomologsTypeAdapter extends TypeAdapter<GeneHomologs> {
    //TODO: refactor with comparator in HtmlGeneDisplay
    private final static Comparator<Gene> GENE_HOMOLOGY_COMPARATOR = Comparator
            .<Gene, Integer>comparing(x -> x.getSpecies().getPreferredDisplayOrder(),
                    Comparator.nullsLast(Integer::compareTo))
            .thenComparing(x -> x.getGeneId(), Comparator.nullsLast(String::compareTo));

    private final Gson gson;
    private final Supplier<RequestParameters> rpSupplier;
    private static final Logger log = LogManager.getLogger(GeneHomologsTypeAdapter.class.getName());

    protected GeneHomologsTypeAdapter(Gson gson, Supplier<RequestParameters> rpSupplier) {
        this.gson = gson;
        this.rpSupplier = rpSupplier;
    }

    @Override
    public void write(JsonWriter out, GeneHomologs value) throws IOException {
        log.traceEntry("{}, {}", out, value);
        if (value == null) {
            out.nullValue();
            log.traceExit(); return;
        }
        out.beginObject();

        out.name("gene");
        TypeAdaptersUtils.writeSimplifiedGene(out, value.getGene(), false, null);

        out.name("orthologsByTaxon");
        this.writeHomologsByTaxon(out, value.getGene(), value.getOrthologsByTaxon());
        out.name("paralogsByTaxon");
        this.writeHomologsByTaxon(out, value.getGene(), value.getParalogsByTaxon());

        out.name("orthologyXRef");
        this.gson.getAdapter(GeneXRef.class).write(out, value.getOrthologyXRef());
        out.name("paralogyXRef");
        this.gson.getAdapter(GeneXRef.class).write(out, value.getParalogyXRef());

        out.endObject();
        log.traceExit();
    }

    @Override
    public GeneHomologs read(JsonReader in) throws IOException {
        //for now, we never read JSON values
        throw log.throwing(new UnsupportedOperationException("No custom JSON reader for GeneHomologs."));
    }

    private void writeHomologsByTaxon(JsonWriter out, Gene targetGene,
            LinkedHashMap<Taxon, Set<Gene>> homologsByTaxon) throws IOException {
        log.traceEntry("{}, {}, {}", out, targetGene, homologsByTaxon);
        out.beginArray();
        // all homologs of one taxon
        // We will display to each taxon level all genes from more recent taxon
        Set<Gene> allGenes = new HashSet<>();
        for (Entry<Taxon, Set<Gene>> e: homologsByTaxon.entrySet()) {
            out.beginObject();

            out.name("taxon");
            this.gson.getAdapter(Taxon.class).write(out, e.getKey());

            allGenes.addAll(e.getValue());
            // sort genes
            List<Gene> orderedHomologsWithDescendant = allGenes.stream()
                    .sorted(GENE_HOMOLOGY_COMPARATOR)
                    .collect(Collectors.toList());
            out.name("genes");
            out.beginArray();
            for (Gene gene: orderedHomologsWithDescendant) {
                TypeAdaptersUtils.writeSimplifiedGene(out, gene, false, null);
            }
            out.endArray();

            //provide the parameters to produce a link to an expression comparison
            RequestParameters rp = this.rpSupplier.get();
            List<String> genesToCompare = orderedHomologsWithDescendant.stream()
                    .map(Gene::getGeneId).collect(Collectors.toList());
            genesToCompare.add(targetGene.getGeneId());
            rp.setGeneList(genesToCompare);
            out.name(JsonParentDisplay.STORABLE_PARAMS_INFO);
            this.gson.getAdapter(LinkedHashMap.class)
            .write(out, JsonParentDisplay.getStorableParamsInfo(rp));

            out.endObject();
        }
        out.endArray();
        log.traceExit();
    }
}
