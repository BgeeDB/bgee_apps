package org.bgee.view.json.adapters;

import java.io.IOException;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.NamedEntity;
import org.bgee.model.XRef;
import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.anatdev.DevStage;
import org.bgee.model.anatdev.Sex;
import org.bgee.model.anatdev.Strain;
import org.bgee.model.dao.api.expressiondata.ConditionDAO;
import org.bgee.model.expressiondata.CallService;
import org.bgee.model.expressiondata.Condition;
import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.model.expressiondata.multispecies.MultiSpeciesCondition;
import org.bgee.model.expressiondata.rawdata.RawDataCondition;
import org.bgee.model.gene.Gene;
import org.bgee.model.source.Source;
import org.bgee.model.species.Species;

import com.google.gson.stream.JsonWriter;

public class TypeAdaptersUtils {

    private static final Logger log = LogManager.getLogger(TypeAdaptersUtils.class.getName());

    public void writeSimplifiedRawDataCondition(JsonWriter out, RawDataCondition cond)
            throws IOException {
        log.traceEntry("{}, {}}", out, cond);
        if (cond == null) {
            out.nullValue();
            log.traceExit(); return;
        }
        out.beginObject();

        out.name("anatEntity");
        writeSimplifiedNamedEntity(out, cond.getAnatEntity());

        out.name("cellType");
        if (cond.getCellType() == null) {
            out.value("NA");
        } else {
            writeSimplifiedNamedEntity(out, cond.getCellType());
        }

        out.name("devStage");
        writeSimplifiedNamedEntity(out, cond.getDevStage());

        out.name("sex").value(cond.getSex().getStringRepresentation());

        out.name("strain").value(cond.getStrain());

        out.name("species");
        writeSimplifiedSpecies(out, cond.getSpecies(), false, null);

        out.endObject();
        log.traceExit();
    }

    public void writeSimplifiedCondition(JsonWriter out, Condition cond,
            EnumSet<CallService.Attribute> condParams) throws IOException {
        log.traceEntry("{}, {}, {}", out, cond, condParams);
        out.beginObject();

        // Anat entity ID and Anat entity cells
        AnatEntity anatEntity = cond.getAnatEntity();
        out.name("anatEntity");
        if (anatEntity != null && condParams.contains(CallService.Attribute.ANAT_ENTITY_ID)) {
            assert condParams.contains(CallService.Attribute.CELL_TYPE_ID):
                "Anat. entity and cell type are requested together for the gene page";
            writeSimplifiedNamedEntity(out, anatEntity);
        } else {
            out.nullValue();
        }

        AnatEntity cellType = cond.getCellType();
        out.name("cellType");
        // post-composition if not the root of cell type
        if (cellType != null && condParams.contains(CallService.Attribute.CELL_TYPE_ID) &&
                !ConditionDAO.CELL_TYPE_ROOT_ID.equals(cellType.getId())) {
            assert condParams.contains(CallService.Attribute.ANAT_ENTITY_ID):
                "Anat. entity and cell type are requested together for the gene page";
            writeSimplifiedNamedEntity(out, cellType);
        } else {
            out.nullValue();
        }

        // Dev stage
        DevStage stage = cond.getDevStage();
        out.name("devStage");
        if (stage != null && condParams.contains(CallService.Attribute.DEV_STAGE_ID)) {
            writeSimplifiedNamedEntity(out, stage);
        } else {
            out.nullValue();
        }

        // Sexes
        Sex sex = cond.getSex();
        out.name("sex");
        if (sex != null && condParams.contains(CallService.Attribute.SEX_ID)) {
            out.value(sex.getName());
        } else {
            out.nullValue();
        }

        // Strains
        Strain strain = cond.getStrain();
        out.name("strain");
        if (strain != null && condParams.contains(CallService.Attribute.STRAIN_ID)) {
            out.value(strain.getName());
        } else {
            out.nullValue();
        }
        out.endObject();
        log.traceExit();
    }

    public void writeSimplifiedMultiSpeciesCondition(JsonWriter out, MultiSpeciesCondition cond)
            throws IOException {
        log.traceEntry("{}, {}", out, cond);
        out.beginObject();

        out.name("anatEntities");
        out.beginArray();
        for (AnatEntity ae: cond.getAnatSimilarity().getSourceAnatEntities()) {
            writeSimplifiedNamedEntity(out, ae);
        }
        out.endArray();

        out.name("cellTypes");
        out.beginArray();
        for (AnatEntity cellType: cond.getCellTypeSimilarity().getSourceAnatEntities()) {
            if (cellType != null && !ConditionDAO.CELL_TYPE_ROOT_ID.equals(cellType.getId())) {
                writeSimplifiedNamedEntity(out, cellType);
            }
        }
        out.endArray();

        //TODO: stageSimilarity and Sex

        out.endObject();
        log.traceExit();
    }

    public void writeSimplifiedSource(JsonWriter out, Source source) throws IOException {
        log.traceEntry("{}, {}", out, source);
        if (source == null) {
            out.nullValue();
            log.traceExit(); return;
        }
        out.name("name").value(source.getName());
        out.name("description").value(source.getDescription());
        out.name("baseUrl").value(source.getBaseUrl());
        log.traceExit();
    }
    public void writeSimplifiedXRef(JsonWriter out, XRef xRef,
            Function<String, String> urlEncodeFunction) throws IOException {
        log.traceEntry("{}, {}", out, xRef, urlEncodeFunction);
        out.name("xRefId").value(xRef.getXRefId());
        out.name("xRefName").value(xRef.getXRefName());
        out.name("xRefURL").value(xRef.getXRefUrl(false, urlEncodeFunction));
        log.traceExit();
    }
    public void writeSimplifiedGene(JsonWriter out, Gene gene, boolean withSpeciesDataSource,
            EnumSet<DataType> allowedDataTypes)
            throws IOException {
        log.traceEntry("{}, {}, {}, {}", out, gene, withSpeciesDataSource, allowedDataTypes);
        out.beginObject();
        out.name("geneId").value(gene.getGeneId());
        out.name("name").value(gene.getName());

        //Simplified display of Species
        out.name("species");
        writeSimplifiedSpecies(out, gene.getSpecies(), withSpeciesDataSource, allowedDataTypes);

        out.name("geneMappedToSameGeneIdCount").value(gene.getGeneMappedToSameGeneIdCount());
        out.endObject();
        log.traceExit();
    }
    public void writeSimplifiedSpecies(JsonWriter out, Species species,
            boolean withSpeciesDataSource, EnumSet<DataType> allowedDataTypes) throws IOException {
        log.traceEntry("{}, {}, {}, {}", out, species, withSpeciesDataSource, allowedDataTypes);
        out.beginObject();
        out.name("id").value(species.getId());
        out.name("name").value(species.getName());
        out.name("genus").value(species.getGenus());
        out.name("speciesName").value(species.getSpeciesName());
        out.name("preferredDisplayOrder").value(species.getPreferredDisplayOrder());
        if (withSpeciesDataSource) {
            out.name("sourcesOfDataPerDataType");
            writeSourcesPerDataType(out, species.getDataSourcesForDataByDataTypes(),
                    allowedDataTypes);
            out.name("sourcesOfAnnotationsPerDataType");
            writeSourcesPerDataType(out, species.getDataSourcesForAnnotationByDataTypes(),
                    allowedDataTypes);
        }
        out.endObject();
        log.traceExit();
    }
    public void writeSourcesPerDataType(JsonWriter out, Map<DataType, Set<Source>> map,
            EnumSet<DataType> allowedDataTypes) throws IOException {
        log.traceEntry("{}, {}, {}", out, map, allowedDataTypes);
        // We order the Map by DataType and Source alphabetical name order
        LinkedHashMap<DataType, List<Source>> dsByDataTypes = map.entrySet().stream()
                .filter(e -> allowedDataTypes == null || allowedDataTypes.contains(e.getKey()))
                .sorted(Comparator.comparing(e -> e.getKey()))
                .collect(Collectors.toMap(
                        e -> e.getKey(),
                        e -> e.getValue().stream().sorted(Comparator.comparing(s -> s.getName()))
                                         .collect(Collectors.toList()),
                        (v1, v2) -> {throw new AssertionError("Impossible collision");},
                        LinkedHashMap::new));
        out.beginArray();
        for (Entry<DataType, List<Source>> e: dsByDataTypes.entrySet()) {
            out.beginObject();
            out.name("dataType").value(e.getKey().getStringRepresentation());
            out.name("sources");
            out.beginArray();
            for (Source s: e.getValue()) {
                out.beginObject();
                writeSimplifiedSource(out, s);
                out.endObject();
            }
            out.endArray();  // end List value
            out.endObject(); // end Entry
        }
        out.endArray(); // end Map
        log.traceExit();
    }
    public void writeSimplifiedNamedEntity(JsonWriter out, NamedEntity<String> namedEntity)
            throws IOException {
        log.traceEntry("{}, {}", out, namedEntity);
        if (namedEntity == null) {
            out.nullValue();
            log.traceExit(); return;
        }
        out.beginObject();
        out.name("id").value(namedEntity.getId());
        out.name("name").value(namedEntity.getName());
        out.endObject();
        log.traceExit();
    }
}