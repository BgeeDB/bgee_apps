package org.bgee.view.json.adapters;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.expressiondata.CallService;
import org.bgee.model.expressiondata.Condition;
import org.bgee.model.expressiondata.MultiGeneExprAnalysis;
import org.bgee.model.expressiondata.baseelements.ExpressionLevelInfo;
import org.bgee.model.expressiondata.baseelements.SummaryCallType.ExpressionSummary;
import org.bgee.model.expressiondata.multispecies.MultiSpeciesCondition;
import org.bgee.model.gene.Gene;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * A {@code TypeAdapter} to read/write {@code MultiGeneExprAnalysis}s in JSON. It is needed because
 * of the complexity of the object and because we want to fine tune the response.
 */
public final class MultiGeneExprAnalysisTypeAdapter extends TypeAdapter<MultiGeneExprAnalysis<?>> {
    private static final Logger log = LogManager.getLogger(MultiGeneExprAnalysisTypeAdapter.class.getName());

    protected MultiGeneExprAnalysisTypeAdapter() {}

    @Override
    public void write(JsonWriter out, MultiGeneExprAnalysis<?> value) throws IOException {
        log.traceEntry("{}, {}", out, value);
        if (value == null) {
            out.nullValue();
            log.traceExit(); return;
        }
        out.beginArray();
        for (Entry<?, MultiGeneExprAnalysis.MultiGeneExprCounts> condToCounts:
            value.getCondToCounts().entrySet()) {

            writeCondToCounts(out, condToCounts);
        }
        out.endArray();

        log.traceExit();
    }

    @Override
    public MultiGeneExprAnalysis<?> read(JsonReader in) throws IOException {
        //for now, we never read JSON values
        throw log.throwing(new UnsupportedOperationException(
                "No custom JSON reader for MultiGeneExprAnalysis."));
    }

    private static void writeCondToCounts(JsonWriter out,
            Entry<?, MultiGeneExprAnalysis.MultiGeneExprCounts> condToCounts) throws IOException {
        log.traceEntry("{}, {}", out, condToCounts);
        out.beginObject();

        //Condition
        Object cond = condToCounts.getKey();
        if (cond instanceof MultiSpeciesCondition) {
            out.name("multiSpeciesCondition");
            TypeAdaptersUtils.writeSimplifiedMultiSpeciesCondition(out, (MultiSpeciesCondition) cond);
        } else if (cond instanceof Condition) {
            out.name("condition");
            //For now, we only use anat. entity and cell type for comparison
            TypeAdaptersUtils.writeSimplifiedCondition(out, (Condition) cond, EnumSet.of(
                    CallService.Attribute.ANAT_ENTITY_ID, CallService.Attribute.CELL_TYPE_ID));
        } else {
            throw log.throwing(new IllegalStateException("Unrecognized class: "
                    + cond.getClass().getSimpleName()));
        }

        //Conservation score
        Map<ExpressionSummary, Set<Gene>> callTypeToGenes = condToCounts.getValue().getCallTypeToGenes();
        Set<Gene> expressedGenes = callTypeToGenes.get(ExpressionSummary.EXPRESSED);
        if (expressedGenes == null) {
            expressedGenes = new HashSet<>();
        }
        Set<Gene> notExpressedGenes = callTypeToGenes.get(ExpressionSummary.NOT_EXPRESSED);
        if (notExpressedGenes == null) {
            notExpressedGenes = new HashSet<>();
        }
        //We need to cast to double explicitly otherwise the result of dividing is incorrect
        @SuppressWarnings("cast")
        double score = (double) (expressedGenes.size() - notExpressedGenes.size())
                / ((double) expressedGenes.size() + notExpressedGenes.size());
        out.name("conservationScore");
        out.value(String.format(Locale.US, "%.2f", score));

        // Max expression score
        Optional<ExpressionLevelInfo> collect = condToCounts.getValue().getGeneToExprLevelInfo().values().stream()
                .filter(eli -> eli != null && eli.getExpressionScore() != null)
                .max(Comparator.comparing(ExpressionLevelInfo::getExpressionScore,
                        Comparator.nullsFirst(BigDecimal::compareTo)));
        out.name("maxExpressionScore");
        out.value(collect.isPresent()? collect.get().getFormattedExpressionScore(): "NA");

        // Genes
        out.name("genesExpressionPresent");
        writeGenes(out, expressedGenes);
        out.name("genesExpressionAbsent");
        writeGenes(out, notExpressedGenes);
        out.name("genesNoData");
        writeGenes(out, condToCounts.getValue().getGenesWithNoData());

        out.endObject();
        log.traceExit();
    }

    private static void writeGenes(JsonWriter out, Set<Gene> genes) throws IOException {
        log.traceEntry("{}, {}", out, genes);

        out.beginArray();
        if (genes != null) {
            List<Gene> sortedGenes = genes.stream().sorted(Comparator.comparing(Gene::getGeneId))
                    .collect(Collectors.toList());
            for (Gene gene: sortedGenes) {
                TypeAdaptersUtils.writeSimplifiedGene(out, gene, false, null);
            }
        }
        out.endArray();

        log.traceExit();
    }
}
