package org.bgee.model;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Map.Entry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.anatdev.AnatEntityService;
import org.bgee.model.anatdev.DevStage;
import org.bgee.model.anatdev.DevStageService;
import org.bgee.model.anatdev.TaxonConstraint;
import org.bgee.model.anatdev.TaxonConstraintService;
import org.bgee.model.anatdev.multispemapping.AnatEntitySimilarityService;
import org.bgee.model.dao.api.DAOManager;
import org.bgee.model.dao.api.DAOResultSet;
import org.bgee.model.dao.api.TransferObject;
import org.bgee.model.dao.api.anatdev.AnatEntityDAO.AnatEntityTO;
import org.bgee.model.dao.api.anatdev.StageDAO.StageTO;
import org.bgee.model.dao.api.anatdev.TaxonConstraintDAO;
import org.bgee.model.dao.api.anatdev.TaxonConstraintDAO.TaxonConstraintTO;
import org.bgee.model.dao.api.anatdev.mapping.SummarySimilarityAnnotationDAO;
import org.bgee.model.dao.api.expressiondata.ConditionDAO;
import org.bgee.model.dao.api.expressiondata.GlobalExpressionCallDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.RawDataConditionDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.RawDataConditionDAO.RawDataConditionTO;
import org.bgee.model.dao.api.expressiondata.rawdata.RawDataConditionDAO.RawDataConditionTO.DAORawDataSex;
import org.bgee.model.dao.api.gene.GeneDAO;
import org.bgee.model.dao.api.gene.GeneDAO.GeneBioTypeTO;
import org.bgee.model.dao.api.gene.GeneDAO.GeneBioTypeTOResultSet;
import org.bgee.model.dao.api.gene.GeneDAO.GeneTO;
import org.bgee.model.dao.api.gene.GeneNameSynonymDAO.GeneNameSynonymTO;
import org.bgee.model.dao.api.gene.GeneXRefDAO.GeneXRefTO;
import org.bgee.model.dao.api.ontologycommon.CIOStatementDAO;
import org.bgee.model.dao.api.ontologycommon.RelationDAO;
import org.bgee.model.dao.api.ontologycommon.RelationDAO.RelationTO;
import org.bgee.model.dao.api.source.SourceDAO.SourceTO;
import org.bgee.model.dao.api.source.SourceToSpeciesDAO;
import org.bgee.model.dao.api.species.SpeciesDAO;
import org.bgee.model.dao.api.species.SpeciesDAO.SpeciesTO;
import org.bgee.model.dao.api.species.SpeciesDAO.SpeciesTOResultSet;
import org.bgee.model.expressiondata.rawdata.RawDataCondition;
import org.bgee.model.expressiondata.rawdata.RawDataCondition.RawDataSex;
import org.bgee.model.gene.Gene;
import org.bgee.model.gene.GeneBioType;
import org.bgee.model.gene.GeneXRef;
import org.bgee.model.ontology.MultiSpeciesOntology;
import org.bgee.model.ontology.Ontology;
import org.bgee.model.ontology.OntologyService;
import org.bgee.model.ontology.RelationType;
import org.bgee.model.source.Source;
import org.bgee.model.source.SourceCategory;
import org.bgee.model.source.SourceService;
import org.bgee.model.species.Species;
import org.bgee.model.species.SpeciesService;
import org.bgee.model.species.TaxonService;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * Parent class of all classes implementing unit testing. 
 * It allows to automatically log starting, succeeded and failed tests.
 * 
 * @author Frederic Bastian
 * @version Bgee 15 Sep. 2022
 * @since Bgee 13
 */
public abstract class TestAncestor {

    /**
     * Create and return an unmodifiable {@code LinkedHashMap}. The returned type is {@code Map}
     * because there is no method {@code Collections.unmodifiableLinkedHashMap}.
     *
     * @param <K>       The type of the keys.
     * @param <V>       The type of the values.
     * @param entries   A {@code List} of {@code Entry} to populate the {@code LinkedHashMap}.
     * @return          A unmodifiable {@code Map} view of a {@code LinkedHashMap}.
     */
    public static <K, V> Map<K, V> unmodifiableLinkedHashMap(List<Entry<? extends K, ? extends V>> entries) {
        final LinkedHashMap<K, V> map = new LinkedHashMap<>();
        entries.forEach(entry -> map.put(entry.getKey(), entry.getValue()));
        return Collections.unmodifiableMap(map);
    }

    private static Date parseDate(String date) {
        try {
            return new SimpleDateFormat("yyyy-MM-dd").parse(date);
        } catch (ParseException e) {
            return null;
        }
    }


    //*************************
    // SOURCES
    //*************************
    protected static final Map<Integer, SourceTO> SOURCE_TOS = unmodifiableLinkedHashMap(List.of(
            Map.entry(1, new SourceTO(1, "sourceName1", "sourceDescription1",
                    //TODO: modify those to have actual patterns to find/replace
                    "sourceXRefURL1", "sourceExperimentUrl1", "sourceEvidenceUrl1", "sourceBaseUrl1",
                    parseDate("1970-01-31"),
                    "sourceReleaseVersion1", true,
                    org.bgee.model.dao.api.source.SourceDAO.SourceTO.SourceCategory.GENOMICS,
                    1)),
            Map.entry(2, new SourceTO(2, "sourceName2", "sourceDescription2",
                    //TODO: modify those to have actual patterns to find/replace
                    "sourceXRefURL2", "sourceExperimentUrl2", "sourceEvidenceUrl2", "sourceBaseUrl2",
                    parseDate("1980-12-31"),
                    "sourceReleaseVersion2", true,
                    org.bgee.model.dao.api.source.SourceDAO.SourceTO.SourceCategory.GENOMICS,
                    2))));
    protected static final Map<Integer, Source> SOURCES = unmodifiableLinkedHashMap(
            SOURCE_TOS.values().stream().map(to ->
                    Map.entry(to.getId(), new Source(to.getId(), to.getName(), to.getDescription(),
                            to.getXRefUrl(), to.getExperimentUrl(), to.getEvidenceUrl(), to.getBaseUrl(),
                            to.getReleaseDate(), to.getReleaseVersion(), to.isToDisplay(),
                            SourceCategory.convertToSourceCategory(to.getSourceCategory().name()),
                            to.getDisplayOrder(),
                            null, null)))
            .collect(Collectors.toList()));


    //*************************
    // SPECIES
    //*************************
    protected static final Map<Integer, SpeciesTO> SPECIES_TOS = unmodifiableLinkedHashMap(List.of(
            Map.entry(1, new SpeciesTO(1, "spe1", "spe", "1", 1, 100, "genomeFilePath1",
                    "genomeVersion1", "genomeAssemblyXRef1", 1, 1)),
            Map.entry(2, new SpeciesTO(2, "spe2", "spe", "2", 2, 100, "genomeFilePath2",
                    "genomeVersion2", "genomeAssemblyXRef2", 2, 2)),
            Map.entry(3, new SpeciesTO(3, "spe3", "spe", "3", 3, 200, "genomeFilePath3",
                    "genomeVersion2", "genomeAssemblyXRef2", 2,
                    2 //use the same genome as species 2
                    ))));
    protected static Map<Integer, Species> loadSpeciesMap(boolean withSpeciesSourceInfo) {
        return unmodifiableLinkedHashMap(SPECIES_TOS.values().stream().map(speciesTO ->
                Map.entry(speciesTO.getId(), new Species(speciesTO.getId(),
                        speciesTO.getName(),
                        null, //description
                        speciesTO.getGenus(),
                        speciesTO.getSpeciesName(),
                        speciesTO.getGenomeVersion(),
                        speciesTO.getGenomeAssemblyXRef(),
                        withSpeciesSourceInfo? SOURCES.get(speciesTO.getDataSourceId()): null,
                        speciesTO.getGenomeSpeciesId(),
                        speciesTO.getParentTaxonId(),
                        //TODO: populate these two attributes
                        null, null,
                        speciesTO.getDisplayOrder()))
                ).collect(Collectors.toList())
        );
    }
    protected static final Map<Integer, Species> SPECIES = loadSpeciesMap(false);
    protected static final Map<Integer, Species> SPECIES_WITH_SOURCE_INFO = loadSpeciesMap(true);


    //*************************
    // GENES
    //*************************
    protected static final Map<Integer, GeneBioTypeTO> GENE_BIO_TYPE_TOS = unmodifiableLinkedHashMap(List.of(
            Map.entry(1, new GeneBioTypeTO(1, "geneBioType1")),
            Map.entry(2, new GeneBioTypeTO(2, "geneBioType2"))));
    protected static final Map<Integer, GeneBioType> GENE_BIO_TYPES = unmodifiableLinkedHashMap(
            GENE_BIO_TYPE_TOS.values().stream().map(to ->
                    Map.entry(to.getId(), new GeneBioType(to.getName())))
            .collect(Collectors.toList()));

    protected static final Map<Integer, GeneTO> GENE_TOS = unmodifiableLinkedHashMap(List.of(
            Map.entry(1, new GeneTO(
                    1,                  //Bgee gene ID
                    "geneId1",          //public gene ID
                    "geneName1",        //name
                    "geneDescription1", //description
                    1,                  //speciesId
                    1,                  //geneBioTypeId
                    100,                //OMAParentNodeId
                    true,               //From Ensembl?
                    1                   //Number of genes with same public ID
                    )),
            Map.entry(2, new GeneTO(
                    2, "geneId2", "geneName2", "geneDescription2",
                    1, //same species as geneId1
                    2, //alternative geneBioType
                    100, true, 1)),
            Map.entry(3, new GeneTO(3,
                    "geneId3_4", //two different genes with same public ID in species 2 and species 3
                    "geneName3", "geneDescription3",
                    2,           //species 2
                    1, 100,
                    true,        //species 2 and 3 has a genome from a different database than Ensembl
                    2            //two different genes with same public ID in species 2 and species 3
                    )),
            Map.entry(4, new GeneTO(4,
                    "geneId3_4", //two different genes with same public ID in species 2 and species 3
                    "geneName4", "geneDescription4",
                    2,           //species 3
                    1, 100,
                    false,       //species 2 and 3 has a genome from a different database than Ensembl
                    2            //two different genes with same public ID in species 2 and species 3
                    ))));
    protected static final Map<Integer, GeneXRefTO> GENE_X_REF_TOS = unmodifiableLinkedHashMap(List.of(
            Map.entry(1, new GeneXRefTO(
                    1, //Bgee gene ID
                    "xRefId1",
                    "xRefName1",
                    SPECIES_TOS.get(GENE_TOS.get(1).getSpeciesId()).getDataSourceId()  //dataSourceId
                    )),
            Map.entry(2, new GeneXRefTO(
                    2, //Bgee gene ID
                    "xRefId2",
                    "xRefName2",
                    SPECIES_TOS.get(GENE_TOS.get(2).getSpeciesId()).getDataSourceId()  //dataSourceId
                    )),
            Map.entry(3, new GeneXRefTO(
                    3, //Bgee gene ID
                    "xRefId3",
                    "xRefName3",
                    SPECIES_TOS.get(GENE_TOS.get(3).getSpeciesId()).getDataSourceId()  //dataSourceId
                    )),
            Map.entry(4, new GeneXRefTO(
                    4, //Bgee gene ID
                    "xRefId4",
                    "xRefName4",
                    SPECIES_TOS.get(GENE_TOS.get(4).getSpeciesId()).getDataSourceId()  //dataSourceId
                    ))
            ));
    protected static final Map<Integer, GeneXRef> GENE_X_REFS = unmodifiableLinkedHashMap(
            GENE_X_REF_TOS.entrySet().stream().map(e ->
                    Map.entry(e.getKey(), new GeneXRef(
                            e.getValue().getXRefId(),
                            e.getValue().getXRefName(),
                            SOURCES.get(e.getValue().getDataSourceId()),
                            GENE_TOS.get(e.getValue().getBgeeGeneId()).getGeneId(),
                            SPECIES.get(GENE_TOS.get(e.getValue().getBgeeGeneId()).getSpeciesId()).getScientificName())))
            .collect(Collectors.toList()));
    protected static final Map<Integer, GeneNameSynonymTO> GENE_NAME_SYNONYM_TOS = unmodifiableLinkedHashMap(List.of(
            Map.entry(1, new GeneNameSynonymTO(
                    1, //Bgee gene ID
                    "synonym_geneId1_1")),
            Map.entry(2, new GeneNameSynonymTO(
                    1, //Bgee gene ID
                    "synonym_geneId1_2")),
            Map.entry(3, new GeneNameSynonymTO(
                    2, //Bgee gene ID
                    "synonym_geneId2_1"))));
    protected static Map<Integer, Gene> loadGeneMap(boolean withSynonyms, boolean withXRefs) {
        return unmodifiableLinkedHashMap(
                GENE_TOS.values().stream().map(to ->
                Map.entry(to.getId(), new Gene(
                        to.getGeneId(),
                        to.getName(),
                        to.getDescription(),
                        withSynonyms? GENE_NAME_SYNONYM_TOS.values().stream() //synonyms
                            .filter(synTO -> synTO.getBgeeGeneId().equals(to.getId()))
                            .map(synTO -> synTO.getGeneNameSynonym())
                            .collect(Collectors.toSet()): null,
                        withXRefs? GENE_X_REF_TOS.entrySet().stream()     //GeneXRefs
                            .filter(e -> e.getValue().getBgeeGeneId().equals(to.getId()))
                            .map(e -> GENE_X_REFS.get(e.getKey()))
                            .collect(Collectors.toSet()): null,
                        SPECIES.get(to.getSpeciesId()),
                        GENE_BIO_TYPES.get(to.getGeneBioTypeId()),
                        to.getGeneMappedToGeneIdCount())))
        .collect(Collectors.toList()));
    }
    protected static final Map<Integer, Gene> GENES = loadGeneMap(false, false);
    protected static final Map<Integer, Gene> GENES_WITH_SYNONYMS_XREFS = loadGeneMap(true, true);

    //*************************
    // ANAT DEV
    //*************************

    //Anat. ontology:
    //
    //                        bgeeRootId (species 1, 2, 3, nonInformative = true)
    //                                           1/         2\
    //anatRootId (species 1, 2, 3, nonInformative = true)   cellTypeRootId (species 1, 2, 3, nonInformative = true)
    //               3/        5\                                            14/       16\
    //anatId2 (species 1, 2)    anatId3 (species 2, 3)      cellTypeId2 (species 1, 2)    cellTypeId3 (species 2, 3)
    //             7\ (species 1)    8/        11\                        18\ (species 1)    19/        22\
    //             anatId4 (species 1, 2)   anatId5 (species 3)      cellTypeId4 (species 1, 2)    cellTypeId5 (species 3)

    protected static final Map<String, AnatEntityTO> ANAT_ENTITY_TOS = unmodifiableLinkedHashMap(List.of(
            Map.entry("bgeeRootId", new AnatEntityTO("bgeeRootId", "bgeeRootName", "bgeeRootDescription",
                    ConditionDAO.DEV_STAGE_ROOT_ID, ConditionDAO.DEV_STAGE_ROOT_ID, true)),
            Map.entry(ConditionDAO.ANAT_ENTITY_ROOT_ID, new AnatEntityTO(ConditionDAO.ANAT_ENTITY_ROOT_ID, "anatName1", "anatDescription1",
                    ConditionDAO.DEV_STAGE_ROOT_ID, ConditionDAO.DEV_STAGE_ROOT_ID, true)),
            Map.entry("anatId2", new AnatEntityTO("anatId2", "anatName2", "anatDescription2",
                    ConditionDAO.DEV_STAGE_ROOT_ID, ConditionDAO.DEV_STAGE_ROOT_ID, false)),
            Map.entry("anatId3", new AnatEntityTO("anatId3", "anatName3", "anatDescription3",
                    ConditionDAO.DEV_STAGE_ROOT_ID, ConditionDAO.DEV_STAGE_ROOT_ID, false)),
            Map.entry("anatId4", new AnatEntityTO("anatId4", "anatName4", "anatDescription4",
                    ConditionDAO.DEV_STAGE_ROOT_ID, ConditionDAO.DEV_STAGE_ROOT_ID, false)),
            Map.entry("anatId5", new AnatEntityTO("anatId5", "anatName5", "anatDescription5",
                    ConditionDAO.DEV_STAGE_ROOT_ID, ConditionDAO.DEV_STAGE_ROOT_ID, false)),
            Map.entry(ConditionDAO.CELL_TYPE_ROOT_ID, new AnatEntityTO(ConditionDAO.CELL_TYPE_ROOT_ID, "cellTypeName1", "cellTypeDescription1",
                    ConditionDAO.DEV_STAGE_ROOT_ID, ConditionDAO.DEV_STAGE_ROOT_ID, true)),
            Map.entry("cellTypeId2", new AnatEntityTO("cellTypeId2", "cellTypeName2", "cellTypeDescription2",
                    ConditionDAO.DEV_STAGE_ROOT_ID, ConditionDAO.DEV_STAGE_ROOT_ID, false)),
            Map.entry("cellTypeId3", new AnatEntityTO("cellTypeId3", "cellTypeName3", "cellTypeDescription3",
                    ConditionDAO.DEV_STAGE_ROOT_ID, ConditionDAO.DEV_STAGE_ROOT_ID, false)),
            Map.entry("cellTypeId4", new AnatEntityTO("cellTypeId4", "cellTypeName4", "cellTypeDescription4",
                    ConditionDAO.DEV_STAGE_ROOT_ID, ConditionDAO.DEV_STAGE_ROOT_ID, false)),
            Map.entry("cellTypeId5", new AnatEntityTO("cellTypeId5", "cellTypeName5", "cellTypeDescription5",
                    ConditionDAO.DEV_STAGE_ROOT_ID, ConditionDAO.DEV_STAGE_ROOT_ID, false))));
    protected static final Map<String, AnatEntity> ANAT_ENTITIES = unmodifiableLinkedHashMap(
            ANAT_ENTITY_TOS.values().stream().map(to ->
                    Map.entry(to.getId(), new AnatEntity(to.getId(), to.getName(), to.getDescription())))
            .collect(Collectors.toList()));

    protected static final Map<Integer, TaxonConstraintTO<String>> ANAT_ENTITY_TC_TOS = unmodifiableLinkedHashMap(List.of(
            Map.entry(1, new TaxonConstraintTO<>("bgeeRootId", null)),
            Map.entry(2, new TaxonConstraintTO<>(ConditionDAO.ANAT_ENTITY_ROOT_ID, null)),
            Map.entry(3, new TaxonConstraintTO<>("anatId2", 1)),
            Map.entry(3, new TaxonConstraintTO<>("anatId2", 2)),
            Map.entry(4, new TaxonConstraintTO<>("anatId3", 2)),
            Map.entry(5, new TaxonConstraintTO<>("anatId3", 3)),
            Map.entry(6, new TaxonConstraintTO<>("anatId4", 1)),
            Map.entry(6, new TaxonConstraintTO<>("anatId4", 2)),
            Map.entry(7, new TaxonConstraintTO<>("anatId5", 3)),
            Map.entry(8, new TaxonConstraintTO<>(ConditionDAO.CELL_TYPE_ROOT_ID, null)),
            Map.entry(9, new TaxonConstraintTO<>("cellTypeId2", 1)),
            Map.entry(9, new TaxonConstraintTO<>("cellTypeId2", 2)),
            Map.entry(10, new TaxonConstraintTO<>("cellTypeId3", 2)),
            Map.entry(11, new TaxonConstraintTO<>("cellTypeId3", 3)),
            Map.entry(12, new TaxonConstraintTO<>("cellTypeId4", 1)),
            Map.entry(12, new TaxonConstraintTO<>("cellTypeId4", 2)),
            Map.entry(13, new TaxonConstraintTO<>("cellTypeId5", 3))));
    protected static final Map<Integer, TaxonConstraint<String>> ANAT_ENTITY_TCS = unmodifiableLinkedHashMap(
            ANAT_ENTITY_TC_TOS.entrySet().stream().map(e ->
                    Map.entry(e.getKey(), new TaxonConstraint<>(e.getValue().getEntityId(), e.getValue().getSpeciesId())))
            .collect(Collectors.toList()));


    // Dev. stage ontology:
    //
    //        1  stageRootId (species 1, 2, 3) 10
    //                     1/     2\
    //2 stageId2 (species 1, 2) 3   4 stageId3 (species 2, 3) 9
    //          3\ (species 1)        4/                     6\
    //    5 stageId4 (species 1, 2, too granular = true) 6   7 stageId5 (species 3, tooGranular = true) 8

    protected static final Map<String, StageTO> STAGE_TOS = unmodifiableLinkedHashMap(List.of(
            Map.entry(ConditionDAO.DEV_STAGE_ROOT_ID, new StageTO(ConditionDAO.DEV_STAGE_ROOT_ID, "stageName1", "stageDescription1", 1, 10,
                    1, false, true)),
            Map.entry("stageId2", new StageTO("stageId2", "stageName2", "stageDescription2", 2, 3,
                    2, false, false)),
            Map.entry("stageId3", new StageTO("stageId3", "stageName3", "stageDescription3", 4, 9,
                    2, false, true)),
            Map.entry("stageId4", new StageTO("stageId4", "stageName4", "stageDescription4", 5, 6,
                    3, true, false)),
            Map.entry("stageId5", new StageTO("stageId5", "stageName5", "stageDescription5", 7, 8,
                    3, true, false))));
    protected static final Map<String, DevStage> DEV_STAGES = unmodifiableLinkedHashMap(
            STAGE_TOS.values().stream().map(to ->
                    Map.entry(to.getId(), new DevStage(to.getId(), to.getName(), to.getDescription(),
                            to.getLeftBound(), to.getRightBound(), to.getLevel(),
                            to.isTooGranular(), to.isGroupingStage())))
            .collect(Collectors.toList()));

    protected static final Map<Integer, TaxonConstraintTO<String>> STAGE_TC_TOS = unmodifiableLinkedHashMap(List.of(
            Map.entry(1, new TaxonConstraintTO<>(ConditionDAO.DEV_STAGE_ROOT_ID, null)),
            Map.entry(2, new TaxonConstraintTO<>("stageId2", 1)),
            Map.entry(3, new TaxonConstraintTO<>("stageId2", 2)),
            Map.entry(4, new TaxonConstraintTO<>("stageId3", 2)),
            Map.entry(5, new TaxonConstraintTO<>("stageId3", 3)),
            Map.entry(6, new TaxonConstraintTO<>("stageId4", 1)),
            Map.entry(7, new TaxonConstraintTO<>("stageId4", 2)),
            Map.entry(8, new TaxonConstraintTO<>("stageId5", 3))));
    protected static final Map<Integer, TaxonConstraint<String>> DEV_STAGE_TCS = unmodifiableLinkedHashMap(
            STAGE_TC_TOS.entrySet().stream().map(e ->
                    Map.entry(e.getKey(), new TaxonConstraint<>(e.getValue().getEntityId(), e.getValue().getSpeciesId())))
            .collect(Collectors.toList()));

    //*************************
    // Ontologies
    //*************************

    //Anat. ontology
    protected static final Map<Integer, RelationTO<String>> ANAT_ENTITY_REL_TOS = unmodifiableLinkedHashMap(List.of(

            Map.entry(1, new RelationTO<>(1, ConditionDAO.ANAT_ENTITY_ROOT_ID, "bgeeRootId",
                    RelationTO.RelationType.ISA_PARTOF, RelationTO.RelationStatus.DIRECT)),
            Map.entry(2, new RelationTO<>(2, ConditionDAO.CELL_TYPE_ROOT_ID, "bgeeRootId",
                    RelationTO.RelationType.ISA_PARTOF, RelationTO.RelationStatus.DIRECT)),

            Map.entry(3, new RelationTO<>(3, "anatId2", ConditionDAO.ANAT_ENTITY_ROOT_ID,
                    RelationTO.RelationType.ISA_PARTOF, RelationTO.RelationStatus.DIRECT)),
            Map.entry(4, new RelationTO<>(4, "anatId2", "bgeeRootId",
                    RelationTO.RelationType.ISA_PARTOF, RelationTO.RelationStatus.INDIRECT)),
            Map.entry(5, new RelationTO<>(5, "anatId3", ConditionDAO.ANAT_ENTITY_ROOT_ID,
                    RelationTO.RelationType.ISA_PARTOF, RelationTO.RelationStatus.DIRECT)),
            Map.entry(6, new RelationTO<>(6, "anatId3", "bgeeRootId",
                    RelationTO.RelationType.ISA_PARTOF, RelationTO.RelationStatus.INDIRECT)),
            Map.entry(7, new RelationTO<>(7, "anatId4", "anatId2",
                    RelationTO.RelationType.ISA_PARTOF, RelationTO.RelationStatus.DIRECT)),
            Map.entry(8, new RelationTO<>(8, "anatId4", "anatId3",
                    RelationTO.RelationType.ISA_PARTOF, RelationTO.RelationStatus.DIRECT)),
            Map.entry(9, new RelationTO<>(9, "anatId4", ConditionDAO.ANAT_ENTITY_ROOT_ID,
                    RelationTO.RelationType.ISA_PARTOF, RelationTO.RelationStatus.INDIRECT)),
            Map.entry(10, new RelationTO<>(10, "anatId4", "bgeeRootId",
                    RelationTO.RelationType.ISA_PARTOF, RelationTO.RelationStatus.INDIRECT)),
            Map.entry(11, new RelationTO<>(11, "anatId5", "anatId3",
                    RelationTO.RelationType.ISA_PARTOF, RelationTO.RelationStatus.DIRECT)),
            Map.entry(12, new RelationTO<>(12, "anatId5", ConditionDAO.ANAT_ENTITY_ROOT_ID,
                    RelationTO.RelationType.ISA_PARTOF, RelationTO.RelationStatus.INDIRECT)),
            Map.entry(13, new RelationTO<>(13, "anatId5", "bgeeRootId",
                    RelationTO.RelationType.ISA_PARTOF, RelationTO.RelationStatus.INDIRECT)),

            Map.entry(14, new RelationTO<>(14, "cellTypeId2", ConditionDAO.CELL_TYPE_ROOT_ID,
                    RelationTO.RelationType.ISA_PARTOF, RelationTO.RelationStatus.DIRECT)),
            Map.entry(15, new RelationTO<>(15, "cellTypeId2", "bgeeRootId",
                    RelationTO.RelationType.ISA_PARTOF, RelationTO.RelationStatus.INDIRECT)),
            Map.entry(16, new RelationTO<>(16, "cellTypeId3", ConditionDAO.CELL_TYPE_ROOT_ID,
                    RelationTO.RelationType.ISA_PARTOF, RelationTO.RelationStatus.DIRECT)),
            Map.entry(17, new RelationTO<>(17, "cellTypeId3", "bgeeRootId",
                    RelationTO.RelationType.ISA_PARTOF, RelationTO.RelationStatus.INDIRECT)),
            Map.entry(18, new RelationTO<>(18, "cellTypeId4", "cellTypeId2",
                    RelationTO.RelationType.ISA_PARTOF, RelationTO.RelationStatus.DIRECT)),
            Map.entry(19, new RelationTO<>(19, "cellTypeId4", "cellTypeId3",
                    RelationTO.RelationType.ISA_PARTOF, RelationTO.RelationStatus.DIRECT)),
            Map.entry(20, new RelationTO<>(20, "cellTypeId4", ConditionDAO.CELL_TYPE_ROOT_ID,
                    RelationTO.RelationType.ISA_PARTOF, RelationTO.RelationStatus.INDIRECT)),
            Map.entry(21, new RelationTO<>(21, "cellTypeId4", "bgeeRootId",
                    RelationTO.RelationType.ISA_PARTOF, RelationTO.RelationStatus.INDIRECT)),
            Map.entry(22, new RelationTO<>(22, "cellTypeId5", "cellTypeId3",
                    RelationTO.RelationType.ISA_PARTOF, RelationTO.RelationStatus.DIRECT)),
            Map.entry(23, new RelationTO<>(23, "cellTypeId5", ConditionDAO.CELL_TYPE_ROOT_ID,
                    RelationTO.RelationType.ISA_PARTOF, RelationTO.RelationStatus.INDIRECT)),
            Map.entry(24, new RelationTO<>(24, "cellTypeId5", "bgeeRootId",
                    RelationTO.RelationType.ISA_PARTOF, RelationTO.RelationStatus.INDIRECT))));

    protected static final Map<Integer, TaxonConstraintTO<Integer>> ANAT_ENTITY_REL_TC_TOS = unmodifiableLinkedHashMap(List.of(

            Map.entry(1, new TaxonConstraintTO<>(1, null)), //anatRootId -> bgeeRootId
            Map.entry(2, new TaxonConstraintTO<>(2, null)), //cellTypeRootId -> bgeeRootId

            //Direct rels anat. entities
            Map.entry(3, new TaxonConstraintTO<>(3, 1)),  //anatId2 -> anatRootId
            Map.entry(4, new TaxonConstraintTO<>(3, 2)),  //anatId2 -> anatRootId
            Map.entry(5, new TaxonConstraintTO<>(5, 2)),  //anatId3 -> anatRootId
            Map.entry(6, new TaxonConstraintTO<>(5, 3)),  //anatId3 -> anatRootId
            Map.entry(7, new TaxonConstraintTO<>(7, 1)),  //anatId4 -> anatId2
            Map.entry(8, new TaxonConstraintTO<>(8, 2)),  //anatId4 -> anatId3
            Map.entry(9, new TaxonConstraintTO<>(11, 3)), //anatId5 -> anatId3
            //Direct rels cell types
            Map.entry(10, new TaxonConstraintTO<>(14, 1)), //cellTypeId2 -> cellTypeRootId
            Map.entry(11, new TaxonConstraintTO<>(14, 2)), //cellTypeId2 -> cellTypeRootId
            Map.entry(12, new TaxonConstraintTO<>(16, 2)), //cellTypeId3 -> cellTypeRootId
            Map.entry(13, new TaxonConstraintTO<>(16, 3)), //cellTypeId3 -> cellTypeRootId
            Map.entry(14, new TaxonConstraintTO<>(18, 1)), //cellTypeId4 -> cellTypeId2
            Map.entry(15, new TaxonConstraintTO<>(19, 2)), //cellTypeId4 -> cellTypeId3
            Map.entry(16, new TaxonConstraintTO<>(22, 3)), //cellTypeId5 -> cellTypeId3
            //Indirect rels anat. entities
            Map.entry(17, new TaxonConstraintTO<>(4, 1)),  //anatId2 -> bgeeRootId
            Map.entry(18, new TaxonConstraintTO<>(4, 2)),  //anatId2 -> bgeeRootId
            Map.entry(19, new TaxonConstraintTO<>(6, 2)),  //anatId3 -> bgeeRootId
            Map.entry(20, new TaxonConstraintTO<>(6, 3)),  //anatId3 -> bgeeRootId
            Map.entry(21, new TaxonConstraintTO<>(9, 1)),  //anatId4 -> anatRootId
            Map.entry(22, new TaxonConstraintTO<>(9, 2)),  //anatId4 -> anatRootId
            Map.entry(23, new TaxonConstraintTO<>(10, 1)), //anatId4 -> bgeeRootId
            Map.entry(24, new TaxonConstraintTO<>(10, 2)), //anatId4 -> bgeeRootId
            Map.entry(25, new TaxonConstraintTO<>(12, 3)), //anatId5 -> anatRootId
            Map.entry(26, new TaxonConstraintTO<>(13, 3)), //anatId5 -> bgeeRootId
            //Indirect rels cell types
            Map.entry(27, new TaxonConstraintTO<>(15, 1)), //cellTypeId2 -> bgeeRootId
            Map.entry(28, new TaxonConstraintTO<>(15, 2)), //cellTypeId2 -> bgeeRootId
            Map.entry(29, new TaxonConstraintTO<>(17, 2)), //cellTypeId3 -> bgeeRootId
            Map.entry(30, new TaxonConstraintTO<>(17, 3)), //cellTypeId3 -> bgeeRootId
            Map.entry(31, new TaxonConstraintTO<>(20, 1)), //cellTypeId4 -> cellTypeRootId
            Map.entry(32, new TaxonConstraintTO<>(20, 2)), //cellTypeId4 -> cellTypeRootId
            Map.entry(33, new TaxonConstraintTO<>(21, 1)), //cellTypeId4 -> bgeeRootId
            Map.entry(34, new TaxonConstraintTO<>(21, 2)), //cellTypeId4 -> bgeeRootId
            Map.entry(35, new TaxonConstraintTO<>(23, 3)), //cellTypeId5 -> cellTypeRootId
            Map.entry(36, new TaxonConstraintTO<>(24, 3))  //cellTypeId5 -> bgeeRootId
            ));
    protected static final Map<Integer, TaxonConstraint<Integer>> ANAT_ENTITY_REL_TCS = unmodifiableLinkedHashMap(
            ANAT_ENTITY_REL_TC_TOS.entrySet().stream().map(e ->
                    Map.entry(e.getKey(), new TaxonConstraint<>(e.getValue().getEntityId(), e.getValue().getSpeciesId())))
            .collect(Collectors.toList()));

    // Dev. stage ontology:
    protected static final Map<Integer, RelationTO<String>> STAGE_REL_TOS = unmodifiableLinkedHashMap(List.of(

            Map.entry(1, new RelationTO<>(1, "stageId2", ConditionDAO.DEV_STAGE_ROOT_ID,
                    RelationTO.RelationType.ISA_PARTOF, RelationTO.RelationStatus.DIRECT)),
            Map.entry(2, new RelationTO<>(2, "stageId3", ConditionDAO.DEV_STAGE_ROOT_ID,
                    RelationTO.RelationType.ISA_PARTOF, RelationTO.RelationStatus.DIRECT)),

            Map.entry(3, new RelationTO<>(3, "stageId4", "stageId2",
                    RelationTO.RelationType.ISA_PARTOF, RelationTO.RelationStatus.DIRECT)),
            Map.entry(4, new RelationTO<>(4, "stageId4", "stageId3",
                    RelationTO.RelationType.ISA_PARTOF, RelationTO.RelationStatus.DIRECT)),
            Map.entry(5, new RelationTO<>(5, "stageId4", ConditionDAO.DEV_STAGE_ROOT_ID,
                    RelationTO.RelationType.ISA_PARTOF, RelationTO.RelationStatus.INDIRECT)),
            Map.entry(6, new RelationTO<>(6, "stageId5", "stageId3",
                    RelationTO.RelationType.ISA_PARTOF, RelationTO.RelationStatus.DIRECT)),
            Map.entry(7, new RelationTO<>(7, "stageId5", ConditionDAO.DEV_STAGE_ROOT_ID,
                    RelationTO.RelationType.ISA_PARTOF, RelationTO.RelationStatus.INDIRECT))));

    protected static final Map<Integer, TaxonConstraintTO<Integer>> STAGE_REL_TC_TOS = unmodifiableLinkedHashMap(List.of(

            Map.entry(1, new TaxonConstraintTO<>(1, 1)), //stageId2 -> stageRootId
            Map.entry(2, new TaxonConstraintTO<>(1, 2)), //stageId2 -> stageRootId
            Map.entry(3, new TaxonConstraintTO<>(2, 2)), //stageId3 -> stageRootId
            Map.entry(4, new TaxonConstraintTO<>(2, 3)), //stageId3 -> stageRootId
            Map.entry(5, new TaxonConstraintTO<>(3, 1)), //stageId4 -> stageId2
            Map.entry(6, new TaxonConstraintTO<>(4, 2)), //stageId4 -> stageId3
            Map.entry(7, new TaxonConstraintTO<>(5, 1)), //stageId4 -> stageRootId
            Map.entry(8, new TaxonConstraintTO<>(5, 2)), //stageId4 -> stageRootId
            Map.entry(9, new TaxonConstraintTO<>(6, 3)), //stageId5 -> stageId3
            Map.entry(10, new TaxonConstraintTO<>(7, 3)) //stageId5 -> stageRootId
            ));
    protected static final Map<Integer, TaxonConstraint<Integer>> STAGE_REL_TCS = unmodifiableLinkedHashMap(
            STAGE_REL_TC_TOS.entrySet().stream().map(e ->
                    Map.entry(e.getKey(), new TaxonConstraint<>(e.getValue().getEntityId(), e.getValue().getSpeciesId())))
            .collect(Collectors.toList()));


    //*************************
    // Raw data
    //*************************

    protected static final Map<Integer, RawDataConditionTO> RAW_DATA_COND_TOS = unmodifiableLinkedHashMap(List.of(
            // Species 1
            Map.entry(1, new RawDataConditionTO(1, 1, "anatId2", "stageId2", ConditionDAO.CELL_TYPE_ROOT_ID,
                    DAORawDataSex.MIXED, false, "strain1", 1)),
            Map.entry(2, new RawDataConditionTO(2, 1, "anatId2", "stageId4", ConditionDAO.CELL_TYPE_ROOT_ID,
                    DAORawDataSex.MIXED, false, "strain1", 1)),
            Map.entry(3, new RawDataConditionTO(3, 2, "anatId4", "stageId2", "cellTypeId4",
                    DAORawDataSex.FEMALE, true, "strain2", 1)),
           // Species 2
            Map.entry(4, new RawDataConditionTO(4, 3, "anatId3", "stageId3", ConditionDAO.CELL_TYPE_ROOT_ID,
                    DAORawDataSex.MIXED, false, "strain3", 2)),
            Map.entry(5, new RawDataConditionTO(5, 3, "anatId3", "stageId4", ConditionDAO.CELL_TYPE_ROOT_ID,
                    DAORawDataSex.MIXED, false, "strain3", 2)),
            Map.entry(6, new RawDataConditionTO(6, 4, "anatId4", "stageId3", "cellTypeId4",
                    DAORawDataSex.MALE, true, "strain4", 2)),
            // Species 3
            Map.entry(7, new RawDataConditionTO(7, 5, "anatId3", "stageId3", ConditionDAO.CELL_TYPE_ROOT_ID,
                    DAORawDataSex.MIXED, false, "strain5", 3)),
            Map.entry(8, new RawDataConditionTO(8, 5, "anatId3", "stageId5", ConditionDAO.CELL_TYPE_ROOT_ID,
                    DAORawDataSex.MIXED, false, "strain5", 3)),
            Map.entry(9, new RawDataConditionTO(9, 6, "anatId5", "stageId3", "cellTypeId5",
                    DAORawDataSex.MALE, false, "strain6", 3))));
    protected static final Map<Integer, RawDataCondition> RAW_DATA_CONDS = unmodifiableLinkedHashMap(
            RAW_DATA_COND_TOS.values().stream().map(to ->
                    Map.entry(to.getId(), new RawDataCondition(
                            ANAT_ENTITIES.get(to.getAnatEntityId()),
                            DEV_STAGES.get(to.getStageId()),
                            ANAT_ENTITIES.get(to.getCellTypeId()),
                            BgeeEnum.convert(RawDataSex.class, to.getSex().getStringRepresentation()),
                            to.getStrainId(),
                            SPECIES.get(to.getSpeciesId()))))
            .collect(Collectors.toList()));

    /**
     * Get a mock {@code DAOResultSet} configured to returned the provided {@code TransferObject}s.
     * 
     * @param resultSetType A {@code Class} that is the type of {@code DAOResultSet} to return.
     * @param values        A {@code List} of {@code TransferObject}s to be returned by 
     *                      the mock {@code DAOResultSet}.
     * @return              A configured mock {@code DAOResultSet}.
     * @param T             The type of {@code TransferObject} to return.
     * @param U             The type of {@code DAOResultSet} to return.
     */
    protected static <T extends TransferObject, U extends DAOResultSet<T>> U getMockResultSet(
            Class<U> resultSetType, List<T> values) {
        /**
         * An {@code Answer} to manage calls to {@code next} method.
         */
        final class ResultSetNextAnswer implements Answer<Boolean> {
            /**
             * An {@code int} that is the number of results to be returned by this {@code Answer}.
             */
            private final int size;
            /**
             * An {@code int} defining the current iteration (starts at -1, 
             * so that the first call to next put the cursor on the first result).
             */
            private int iteration;
            
            private ResultSetNextAnswer(int size) {
                this.iteration = -1;
                this.size = size;
            }
            @Override
            public Boolean answer(InvocationOnMock invocation) throws Throwable {
                this.iteration++;
                if (this.iteration < this.size) {
                    return true;
                }
                return false;
            }
        }
        /**
         * An {@code Answer} to manage calls to {@code getTO} method.
         */
        final class ResultSetGetTOAnswer implements Answer<T> {
            /**
             * A {@code List} of {@code T}s to be returned by the mock {@code DAOResultSet}.
             */
            private final List<T> values;
            /**
             * The {@code Answer} used by the same mock {@code DAOResultSet} to respond to {@code next()}.
             * Allows to know which element to return;
             */
            private final ResultSetNextAnswer answerToNext;
            
            private ResultSetGetTOAnswer(List<T> values, ResultSetNextAnswer answerToNext) {
                this.values = values;
                this.answerToNext = answerToNext;
            }
            @Override
            public T answer(InvocationOnMock invocation) throws Throwable {
                if (this.answerToNext.iteration >= 0 && 
                        this.answerToNext.iteration < this.values.size()) {
                    return this.values.get(this.answerToNext.iteration);
                }
                return null;
            }
        }
        
        List<T> clonedValues = new ArrayList<>(values);
        U rs = mock(resultSetType);
        ResultSetNextAnswer nextAnswer = new ResultSetNextAnswer(clonedValues.size());
        ResultSetGetTOAnswer getTOAnswer = new ResultSetGetTOAnswer(clonedValues, nextAnswer);
        
        when(rs.next()).thenAnswer(nextAnswer);
        when(rs.getTO()).thenAnswer(getTOAnswer);
        //XXX: note that the normal behavior of stream() and getAllTOs is to throw an exception 
        //if next was already called on this resultset. But that would need yet another 
        //custom Answer class...
        when(rs.stream()).thenReturn(clonedValues.stream());
        when(rs.getAllTOs()).thenReturn(clonedValues);
        
        return rs;
    }
    
    /**
     * An {@code ArgumentMatcher} allowing to determine whether two {@code Collection}s 
     * contains the same elements (as considered by their {@code equals} method), 
     * independently of the iteration order of the {@code Collection}s.
     */
    protected static class IsCollectionEqual<T> implements ArgumentMatcher<Collection<T>> {
        private final static Logger log = LogManager.getLogger(IsCollectionEqual.class.getName());
        private final Collection<?> expectedCollection;
        
        IsCollectionEqual(Collection<T> expectedCollection) {
            log.traceEntry("{}", expectedCollection);
            this.expectedCollection = expectedCollection;
            log.traceExit();
        }

        @Override
        public boolean matches(Collection<T> argument) {
            log.traceEntry("{}", argument);
            log.debug("Trying to match expected Collection [{}] versus "
                    + "provided argument [{}]", expectedCollection, argument);
            if (expectedCollection == argument) {
                return log.traceExit(true);
            }
            if (expectedCollection == null) {
                if (argument == null) {
                    return log.traceExit(true);
                } 
                return log.traceExit(false);
            } else if (argument == null) {
                return log.traceExit(false);
            }
            if (argument.size() != expectedCollection.size()) {
                return log.traceExit(false);
            }
            return log.traceExit(argument.containsAll(expectedCollection) && expectedCollection.containsAll(argument));
        }
    }
    /**
     * Helper method to obtain a {@link IsCollectionEqual} {@code ArgumentMatcher}, 
     * for readability. 
     * @param expectedCollection    The {@code Collection} that is expected, to be used 
     *                              in stub or verify methods. 
     */
    protected static <T> Collection<T> collectionEq(Collection<T> expectedCollection) {
        return argThat(new IsCollectionEqual<>(expectedCollection));
    }


    //attributes that will hold mock services, DAOs and complex objects
    //**************************************
    // Services
    //**************************************
    @Mock
    protected ServiceFactory serviceFactory;
    @Mock
    protected SpeciesService speciesService;
    @Mock
    protected OntologyService ontService;
    @Mock
    protected AnatEntityService anatEntityService;
    @Mock
    protected DevStageService devStageService;
    @Mock
    protected TaxonService taxonService;
    @Mock
    protected SourceService sourceService;
    @Mock
    protected TaxonConstraintService taxonConstraintService;
    @Mock
    protected AnatEntitySimilarityService anatEntitySimilarityService;
    //**************************************
    // DAOs
    //**************************************
    @Mock
    protected DAOManager manager;
    @Mock
    protected GlobalExpressionCallDAO globalExprCallDAO;
    @Mock
    protected ConditionDAO condDAO;
    @Mock
    protected GeneDAO geneDAO;
    @Mock
    protected RelationDAO relationDAO;
    @Mock
    protected SpeciesDAO speciesDAO;
    @Mock
    protected SourceToSpeciesDAO sourceToSpeciesDAO;
    @Mock
    protected SummarySimilarityAnnotationDAO sumSimAnnotDAO;
    @Mock
    protected CIOStatementDAO cioStatementDAO;
    @Mock
    protected TaxonConstraintDAO taxonConstraintDAO;
    @Mock
    protected RawDataConditionDAO rawDataConditionDAO;
    //**************************************
    // Complex objects
    // (to be configured directly in tests)
    //**************************************
    @Mock
    protected Ontology<AnatEntity, String> anatEntityOnt;
    @Mock
    protected MultiSpeciesOntology<AnatEntity, String> multiSpeAnatEntityOnt;
    @Mock
    protected Ontology<DevStage, String> devStageOnt;
    @Mock
    protected MultiSpeciesOntology<DevStage, String> multiSpeDevStageOnt;

    /**
     * Default Constructor. 
     */
    public TestAncestor() {
        
    }

    //*********************************************
    // In order to use "@Mock" mockito annotations
    // (and intialize "thenReturn" for ServiceFactory and DAOManager)
    //*********************************************
    private AutoCloseable closeable;
    @Before
    public void openMocks() {
        closeable = MockitoAnnotations.openMocks(this);
    }
    @After
    public void releaseMocks() throws Exception {
        closeable.close();
    }
    @Before
    public void configureMockObjects() {
        getLogger().traceEntry();

        //Services
        when(this.serviceFactory.getSpeciesService()).thenReturn(this.speciesService);
        when(this.serviceFactory.getOntologyService()).thenReturn(this.ontService);
        when(this.serviceFactory.getAnatEntityService()).thenReturn(this.anatEntityService);
        when(this.serviceFactory.getDevStageService()).thenReturn(this.devStageService);
        when(this.serviceFactory.getTaxonService()).thenReturn(this.taxonService);
        when(this.serviceFactory.getSourceService()).thenReturn(this.sourceService);
        when(this.serviceFactory.getTaxonConstraintService()).thenReturn(this.taxonConstraintService);
        when(this.serviceFactory.getAnatEntitySimilarityService()).thenReturn(this.anatEntitySimilarityService);
        //DAOs
        when(this.serviceFactory.getDAOManager()).thenReturn(this.manager);
        when(this.manager.getGlobalExpressionCallDAO()).thenReturn(this.globalExprCallDAO);
        when(this.manager.getConditionDAO()).thenReturn(this.condDAO);
        when(this.manager.getGeneDAO()).thenReturn(this.geneDAO);
        when(this.manager.getRelationDAO()).thenReturn(this.relationDAO);
        when(this.manager.getTaxonConstraintDAO()).thenReturn(this.taxonConstraintDAO);
        when(this.manager.getSpeciesDAO()).thenReturn(this.speciesDAO);
        when(this.manager.getSourceToSpeciesDAO()).thenReturn(this.sourceToSpeciesDAO);
        when(this.manager.getSummarySimilarityAnnotationDAO()).thenReturn(this.sumSimAnnotDAO);
        when(this.manager.getCIOStatementDAO()).thenReturn(this.cioStatementDAO);
        when(this.manager.getRawDataConditionDAO()).thenReturn(this.rawDataConditionDAO);

        getLogger().traceExit();
    }


    //*********************************************
    // GENERAL MOCK CONFIGURATION
    //*********************************************
    protected void whenSpeciesDAOGetSpeciesByIds() {
        getLogger().traceEntry();
        SpeciesTOResultSet rs = getMockResultSet(SpeciesTOResultSet.class, List.copyOf(SPECIES_TOS.values()));
        when(this.speciesDAO.getSpeciesByIds(any(), any())).thenReturn(rs);
        getLogger().traceExit();
    }
    protected void whenGeneDAOGetGeneBioTypes() {
        getLogger().traceEntry();
        GeneBioTypeTOResultSet rs = getMockResultSet(GeneBioTypeTOResultSet.class,
                List.copyOf(GENE_BIO_TYPE_TOS.values()));
        when(this.geneDAO.getGeneBioTypes()).thenReturn(rs);
        getLogger().traceExit();
    }
    protected void whenSourceServiceGetSources() {
        getLogger().traceEntry();
        when(this.sourceService.loadAllSources(false)).thenReturn(List.copyOf(SOURCES.values()));
        when(this.sourceService.loadSourcesByIds(null)).thenReturn(SOURCES);
        getLogger().traceExit();
    }
    @SuppressWarnings("unchecked")
    protected void whenGetAnatEntityOntology() {
        getLogger().traceEntry();
        //We need to explicitly cast for the compiler to determine which method to use,
        //therefore we have a warning
        when(this.ontService.getAnatEntityOntology((Collection<Integer>) any(), any(), any(),
                anyBoolean(), anyBoolean()))
        .thenReturn(new MultiSpeciesOntology<AnatEntity, String>(
                SPECIES.keySet(), ANAT_ENTITIES.values(),
                ANAT_ENTITY_REL_TOS.values(), ANAT_ENTITY_TCS.values(),
                ANAT_ENTITY_REL_TCS.values(),
                EnumSet.allOf(RelationType.class), AnatEntity.class
                ));
        getLogger().traceExit();
    }
    @SuppressWarnings("unchecked")
    protected void whenGetDevStageOntology() {
        getLogger().traceEntry();
        //We need to explicitly cast for the compiler to determine which method to use,
        //therefore we have a warning
        when(this.ontService.getDevStageOntology((Collection<Integer>) any(), any(),
                anyBoolean(), anyBoolean()))
        .thenReturn(new MultiSpeciesOntology<DevStage, String>(
                SPECIES.keySet(), DEV_STAGES.values(),
                STAGE_REL_TOS.values(), DEV_STAGE_TCS.values(),
                STAGE_REL_TCS.values(),
                EnumSet.of(RelationType.ISA_PARTOF), DevStage.class
                ));
        getLogger().traceExit();
    }
    //XXX FB: probably we can do something clever for AnatEntityService and DevStageService


    //*********************************************
    // OTHER
    //*********************************************
    /**
     * A {@code TestWatcher} to log starting, succeeded and failed tests. 
     */
    @Rule
    public TestWatcher watchman = new TestWatcher() {
        @Override
        protected void starting(Description description) {
            getLogger().info("Starting test: {}", description);
        }
        @Override
        protected void failed(Throwable e, Description description) {
            if (getLogger().isErrorEnabled()) {
                getLogger().error("Test failed: " + description, e);
            }
        }
        @Override
        protected void succeeded(Description description) {
            getLogger().info("Test succeeded: {}", description);
        }
    };
    
    /**
     * Return the logger of the class. 
     * @return     A {@code Logger}
     */
    protected Logger getLogger() {
         return LogManager.getLogger(this.getClass().getName());
    }
}
