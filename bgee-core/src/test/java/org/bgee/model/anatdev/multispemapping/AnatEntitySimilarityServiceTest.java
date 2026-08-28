package org.bgee.model.anatdev.multispemapping;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.TestAncestor;
import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.anatdev.TaxonConstraint;
import org.bgee.model.dao.api.anatdev.mapping.SummarySimilarityAnnotationDAO.SimAnnotToAnatEntityTO;
import org.bgee.model.dao.api.anatdev.mapping.SummarySimilarityAnnotationDAO.SimAnnotToAnatEntityTOResultSet;
import org.bgee.model.dao.api.anatdev.mapping.SummarySimilarityAnnotationDAO.SummarySimilarityAnnotationTO;
import org.bgee.model.dao.api.anatdev.mapping.SummarySimilarityAnnotationDAO.SummarySimilarityAnnotationTOResultSet;
import org.bgee.model.dao.api.ontologycommon.CIOStatementDAO.CIOStatementTO;
import org.bgee.model.dao.api.ontologycommon.CIOStatementDAO.CIOStatementTOResultSet;
import org.bgee.model.dao.api.ontologycommon.RelationDAO.RelationTO;
import org.bgee.model.ontology.MultiSpeciesOntology;
import org.bgee.model.ontology.Ontology;
import org.bgee.model.ontology.RelationType;
import org.bgee.model.species.Species;
import org.bgee.model.species.Taxon;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link AnatEntitySimilarityService}.
 *
 * @author  Frederic Bastian
 * @version Bgee 15, Dec. 2021
 * @since   Bgee 14 Mar 2019
 */
public class AnatEntitySimilarityServiceTest extends TestAncestor {
    private final static Logger log = LogManager.getLogger(AnatEntitySimilarityServiceTest.class.getName());
    @Override
    protected Logger getLogger() {
        return log;
    }

    private List<Taxon> taxa;
    private Map<Taxon, Ontology<Taxon, Integer>> taxonToOnt;
    private Set<Integer> speciesIdsGnathostomataLCA = new HashSet<>(Arrays.asList(1, 2));
    private String anatEntityWithNoSimilarityId = "id_with_no_similarity";
    private String nonExistingAnatEntityId = "non_existing_id";

    @Before
    public void setMockObjects() {
        log.traceEntry();

        when(this.anatEntityService.loadAnatEntities(
                new HashSet<>(Arrays.asList(anatEntityWithNoSimilarityId, nonExistingAnatEntityId)), false))
        .thenReturn(Arrays.asList(new AnatEntity(anatEntityWithNoSimilarityId)).stream());
        when(this.anatEntityService.loadAnatEntities(
                new HashSet<>(), false))
        .thenReturn(Arrays.asList(new AnatEntity(anatEntityWithNoSimilarityId),
                new AnatEntity("lung"), new AnatEntity("swimbladder"), new AnatEntity("whatever"),
                new AnatEntity("whatever_precursor")).stream());

        Map<Integer, Species> speciesMap = new HashMap<>();
        speciesMap.put(1, new Species(1));
        speciesMap.put(2, new Species(2));
        when(this.speciesService.loadSpeciesMap(null, false))
        .thenReturn(speciesMap);

        when(this.taxonConstraintService.loadAnatEntityTaxonConstraintBySpeciesIds(null))
        .thenReturn(Arrays.asList(
                new TaxonConstraint<>("lung", 1),
                new TaxonConstraint<>("lung", 2),
                new TaxonConstraint<>("swimbladder", null),
                new TaxonConstraint<>("whatever", 1),
                new TaxonConstraint<>("whatever", 2),
                new TaxonConstraint<>("whatever_precursor", 1),
                new TaxonConstraint<>("whatever_precursor", 2),
                new TaxonConstraint<>(anatEntityWithNoSimilarityId, 1)).stream());

        taxonToOnt = new HashMap<>();
        taxa = Arrays.asList(
                new Taxon(131567, "cellular organisms", null, "cellular organisms", 1, false),
                new Taxon(6072, "Eumetazoa", null, "Eumetazoa", 5, false),
                new Taxon(33213, "Bilateria", null, "Bilateria", 6, true),
                new Taxon(6073, "Cnidaria", null, "cnidarians", 6, true),
                new Taxon(7776, "Gnathostomata", null, "jawed vertebrates", 11, true),
                new Taxon(8287, "Sarcopterygii ", null, "Sarcopterygii ", 14, false),
                new Taxon(7898, "Actinopterygii ", null, "ray-finned fishes ", 14, false));
        List<RelationTO<Integer>> taxonRelations = Arrays.asList(
                new RelationTO<>(1, 7898, 7776, RelationTO.RelationType.ISA_PARTOF, RelationTO.RelationStatus.DIRECT),
                new RelationTO<>(2, 7898, 33213, RelationTO.RelationType.ISA_PARTOF, RelationTO.RelationStatus.INDIRECT),
                new RelationTO<>(3, 7898, 6072, RelationTO.RelationType.ISA_PARTOF, RelationTO.RelationStatus.INDIRECT),
                new RelationTO<>(4, 7898, 131567, RelationTO.RelationType.ISA_PARTOF, RelationTO.RelationStatus.INDIRECT),
                new RelationTO<>(5, 8287, 7776, RelationTO.RelationType.ISA_PARTOF, RelationTO.RelationStatus.DIRECT),
                new RelationTO<>(6, 8287, 33213, RelationTO.RelationType.ISA_PARTOF, RelationTO.RelationStatus.INDIRECT),
                new RelationTO<>(7, 8287, 6072, RelationTO.RelationType.ISA_PARTOF, RelationTO.RelationStatus.INDIRECT),
                new RelationTO<>(8, 8287, 131567, RelationTO.RelationType.ISA_PARTOF, RelationTO.RelationStatus.INDIRECT),
                new RelationTO<>(9, 7776, 33213, RelationTO.RelationType.ISA_PARTOF, RelationTO.RelationStatus.DIRECT),
                new RelationTO<>(10, 7776, 6072, RelationTO.RelationType.ISA_PARTOF, RelationTO.RelationStatus.INDIRECT),
                new RelationTO<>(11, 7776, 131567, RelationTO.RelationType.ISA_PARTOF, RelationTO.RelationStatus.INDIRECT),
                new RelationTO<>(12, 33213, 6072, RelationTO.RelationType.ISA_PARTOF, RelationTO.RelationStatus.DIRECT),
                new RelationTO<>(13, 33213, 131567, RelationTO.RelationType.ISA_PARTOF, RelationTO.RelationStatus.INDIRECT),
                new RelationTO<>(14, 6073, 6072, RelationTO.RelationType.ISA_PARTOF, RelationTO.RelationStatus.DIRECT),
                new RelationTO<>(15, 6073, 131567, RelationTO.RelationType.ISA_PARTOF, RelationTO.RelationStatus.INDIRECT));

        //Mock TaxonService to identify a Gnathostomata common ancestor
        when(this.taxonService.loadLeastCommonAncestor(speciesIdsGnathostomataLCA))
        .thenReturn(taxa.get(4));

        //Create taxon ontologies
        //Taxon ontology when requested taxon is 8287 Sarcopterygii
        Ontology<Taxon, Integer> taxOnt = new Ontology<>(null,
                Arrays.asList(taxa.get(0), taxa.get(1), taxa.get(2), taxa.get(4), taxa.get(5)),
                Arrays.asList(taxonRelations.get(4), taxonRelations.get(5), taxonRelations.get(6),
                        taxonRelations.get(7), taxonRelations.get(8), taxonRelations.get(9),
                        taxonRelations.get(10), taxonRelations.get(11), taxonRelations.get(12)),
                EnumSet.of(RelationType.ISA_PARTOF),
                Taxon.class);
        when(this.ontService.getTaxonOntologyFromTaxonIds(Collections.singleton(taxa.get(5).getId()),
                false, true, true))
        .thenReturn(taxOnt);
        taxonToOnt.put(taxa.get(5), taxOnt);
        //Taxon ontology when requested taxon is 7776 Gnathostomata
        taxOnt = new Ontology<>(null,
                Arrays.asList(taxa.get(0), taxa.get(1), taxa.get(2), taxa.get(4), taxa.get(5),
                        taxa.get(6)),
                Arrays.asList(taxonRelations.get(0), taxonRelations.get(1), taxonRelations.get(2),
                        taxonRelations.get(3), taxonRelations.get(4), taxonRelations.get(5),
                        taxonRelations.get(6), taxonRelations.get(7), taxonRelations.get(8),
                        taxonRelations.get(9), taxonRelations.get(10), taxonRelations.get(11),
                        taxonRelations.get(12)),
                EnumSet.of(RelationType.ISA_PARTOF),
                Taxon.class);
        when(this.ontService.getTaxonOntologyFromTaxonIds(Collections.singleton(taxa.get(4).getId()), false, true, true))
        .thenReturn(taxOnt);
        taxonToOnt.put(taxa.get(4), taxOnt);
        //Taxon ontology when requested taxon is 7898 Actinopterygii
        taxOnt = new Ontology<>(null,
                Arrays.asList(taxa.get(0), taxa.get(1), taxa.get(2), taxa.get(4), taxa.get(6)),
                Arrays.asList(taxonRelations.get(0), taxonRelations.get(1), taxonRelations.get(2),
                        taxonRelations.get(3), taxonRelations.get(8), taxonRelations.get(9),
                        taxonRelations.get(10), taxonRelations.get(11), taxonRelations.get(12)),
                EnumSet.of(RelationType.ISA_PARTOF),
                Taxon.class);
        when(this.ontService.getTaxonOntologyFromTaxonIds(Collections.singleton(taxa.get(6).getId()), false, true, true))
        .thenReturn(taxOnt);
        taxonToOnt.put(taxa.get(6), taxOnt);
        //Taxon ontology when requested taxon is 6073 Cnidaria
        taxOnt = new Ontology<>(null,
                Arrays.asList(taxa.get(0), taxa.get(1), taxa.get(3)),
                Arrays.asList(taxonRelations.get(13), taxonRelations.get(14)),
                EnumSet.of(RelationType.ISA_PARTOF),
                Taxon.class);
        when(this.ontService.getTaxonOntologyFromTaxonIds(Collections.singleton(taxa.get(3).getId()), false, true, true))
        .thenReturn(taxOnt);
        taxonToOnt.put(taxa.get(3), taxOnt);
        //Taxon ontology when requested taxon is 33213 Bilateria
        taxOnt = new Ontology<>(null,
                Arrays.asList(taxa.get(0), taxa.get(1), taxa.get(2), taxa.get(4), taxa.get(5),
                        taxa.get(6)),
                Arrays.asList(taxonRelations.get(0), taxonRelations.get(1), taxonRelations.get(2),
                        taxonRelations.get(3), taxonRelations.get(4), taxonRelations.get(5),
                        taxonRelations.get(6), taxonRelations.get(7), taxonRelations.get(8),
                        taxonRelations.get(9), taxonRelations.get(10), taxonRelations.get(11),
                        taxonRelations.get(12)),
                EnumSet.of(RelationType.ISA_PARTOF),
                Taxon.class);
        when(this.ontService.getTaxonOntologyFromTaxonIds(Collections.singleton(taxa.get(2).getId()), false, true, true))
        .thenReturn(taxOnt);
        taxonToOnt.put(taxa.get(2), taxOnt);

        //Create SummarySimilarityAnnotationTOs.
        //Eumetazoa annot
        SummarySimilarityAnnotationTO mouthAnnotTO = new SummarySimilarityAnnotationTO(1, 6072,
                false, "CIO:1");
        SummarySimilarityAnnotationTO mouthAnusAnnotTO = new SummarySimilarityAnnotationTO(2, 6072,
                false, "CIO:1");
        //Bilateria annots
        SummarySimilarityAnnotationTO anusAnnotTO = new SummarySimilarityAnnotationTO(3, 33213,
                false, "CIO:1");
        //Gnathostomata annots
        SummarySimilarityAnnotationTO lungSwimBladderAnnotTO = new SummarySimilarityAnnotationTO(4, 7776,
                false, "CIO:1");
        SummarySimilarityAnnotationTO fakeLungSwimBladderWhateverAnnotTO =
                new SummarySimilarityAnnotationTO(5, 7776, false, "CIO:1");
        SummarySimilarityAnnotationTO lungAnnotTO = new SummarySimilarityAnnotationTO(6, 7776,
                false, "CIO:1");
        //Actinopterygii annots
        SummarySimilarityAnnotationTO swimBladderAnnotTO = new SummarySimilarityAnnotationTO(7, 7898,
                false, "CIO:1");
        //Sarcopterygii annots
        SummarySimilarityAnnotationTO whateverAnnotTO = new SummarySimilarityAnnotationTO(8, 8287,
                false, "CIO:2");
        //(create a redundant lung annotation at Sarcopterygii level)
        SummarySimilarityAnnotationTO lungSarcoAnnotTO = new SummarySimilarityAnnotationTO(9, 8287,
                false, "CIO:1");

        //Similarity annots for Gnathostomata including not-trusted
        SummarySimilarityAnnotationTOResultSet gnathostomataNotTrustedRS = getMockResultSet(
                SummarySimilarityAnnotationTOResultSet.class,
                Arrays.asList(mouthAnnotTO, mouthAnusAnnotTO, anusAnnotTO, lungSwimBladderAnnotTO,
                        fakeLungSwimBladderWhateverAnnotTO, lungAnnotTO, swimBladderAnnotTO, whateverAnnotTO,
                        lungSarcoAnnotTO));
        when(this.sumSimAnnotDAO.getSummarySimilarityAnnotations(7776, true, true, true, null, null))
        .thenReturn(gnathostomataNotTrustedRS);
        //Similarity annots for Gnathostomata only trusted
        SummarySimilarityAnnotationTOResultSet gnathostomataTrustedRS = getMockResultSet(
                SummarySimilarityAnnotationTOResultSet.class,
                Arrays.asList(mouthAnnotTO, mouthAnusAnnotTO, anusAnnotTO, lungSwimBladderAnnotTO,
                        fakeLungSwimBladderWhateverAnnotTO, lungAnnotTO, swimBladderAnnotTO,
                        lungSarcoAnnotTO));
        when(this.sumSimAnnotDAO.getSummarySimilarityAnnotations(7776, true, true, true, true, null))
        .thenReturn(gnathostomataTrustedRS);
        //Similarity annots for Sarcopterygii including not-trusted
        SummarySimilarityAnnotationTOResultSet sarcopterygiiNotTrustedRS = getMockResultSet(
                SummarySimilarityAnnotationTOResultSet.class,
                Arrays.asList(mouthAnnotTO, mouthAnusAnnotTO, anusAnnotTO, lungSwimBladderAnnotTO,
                        fakeLungSwimBladderWhateverAnnotTO, lungAnnotTO, lungSarcoAnnotTO, whateverAnnotTO));
        when(this.sumSimAnnotDAO.getSummarySimilarityAnnotations(8287, true, true, true, null, null))
        .thenReturn(sarcopterygiiNotTrustedRS);
        //Similarity annots for Sarcopterygii only trusted
        SummarySimilarityAnnotationTOResultSet sarcopterygiiTrustedRS = getMockResultSet(
                SummarySimilarityAnnotationTOResultSet.class,
                Arrays.asList(mouthAnnotTO, mouthAnusAnnotTO, anusAnnotTO, lungSwimBladderAnnotTO,
                        fakeLungSwimBladderWhateverAnnotTO, lungAnnotTO, lungSarcoAnnotTO));
        when(this.sumSimAnnotDAO.getSummarySimilarityAnnotations(8287, true, true, true, true, null))
        .thenReturn(sarcopterygiiTrustedRS);
        //Similarity annots for Actinopterygii including not-trusted
        SummarySimilarityAnnotationTOResultSet actinopterygiiNotTrustedRS = getMockResultSet(
                SummarySimilarityAnnotationTOResultSet.class,
                Arrays.asList(mouthAnnotTO, mouthAnusAnnotTO, anusAnnotTO, lungSwimBladderAnnotTO,
                        fakeLungSwimBladderWhateverAnnotTO, lungAnnotTO, swimBladderAnnotTO));
        when(this.sumSimAnnotDAO.getSummarySimilarityAnnotations(7898, true, true, true, null, null))
        .thenReturn(actinopterygiiNotTrustedRS);
        //Similarity annots for Cnidaria including not-trusted
        SummarySimilarityAnnotationTOResultSet cnidariaNotTrustedRS = getMockResultSet(
                SummarySimilarityAnnotationTOResultSet.class,
                Arrays.asList(mouthAnnotTO, mouthAnusAnnotTO));
        when(this.sumSimAnnotDAO.getSummarySimilarityAnnotations(6073, true, true, true, null, null))
        .thenReturn(cnidariaNotTrustedRS);
        //Similarity annots for Bilateria including not-trusted
        SummarySimilarityAnnotationTOResultSet bilateriaNotTrustedRS = getMockResultSet(
                SummarySimilarityAnnotationTOResultSet.class,
                Arrays.asList(mouthAnnotTO, mouthAnusAnnotTO, anusAnnotTO, lungSwimBladderAnnotTO,
                        fakeLungSwimBladderWhateverAnnotTO, lungAnnotTO, swimBladderAnnotTO, whateverAnnotTO,
                        lungSarcoAnnotTO));
        when(this.sumSimAnnotDAO.getSummarySimilarityAnnotations(33213, true, true, true, null, null))
        .thenReturn(bilateriaNotTrustedRS);

        //Create SimAnnotToAnatEntityTOs.
        //Eumetazoa mappings
        SimAnnotToAnatEntityTO mouthAnatTO1 = new SimAnnotToAnatEntityTO(1, "mouth");
        SimAnnotToAnatEntityTO mouthAnusAnatTO1 = new SimAnnotToAnatEntityTO(2, "mouth");
        SimAnnotToAnatEntityTO mouthAnusAnatTO2 = new SimAnnotToAnatEntityTO(2, "anus");
        //Bilateria mappings
        SimAnnotToAnatEntityTO anusAnatTO1 = new SimAnnotToAnatEntityTO(3, "anus");
        //Gnathostomata mappings
        SimAnnotToAnatEntityTO lungSwimBladderAnatTO1 = new SimAnnotToAnatEntityTO(4, "lung");
        SimAnnotToAnatEntityTO lungSwimBladderAnatTO2 = new SimAnnotToAnatEntityTO(4, "swimbladder");
        SimAnnotToAnatEntityTO fakeLungSwimBladderWhateverAnatTO1 = new SimAnnotToAnatEntityTO(5, "lung");
        SimAnnotToAnatEntityTO fakeLungSwimBladderWhateverAnatTO2 = new SimAnnotToAnatEntityTO(5, "swimbladder");
        SimAnnotToAnatEntityTO fakeLungSwimBladderWhateverAnatTO3 = new SimAnnotToAnatEntityTO(5, "whatever");
        SimAnnotToAnatEntityTO lungAnatTO1 = new SimAnnotToAnatEntityTO(6, "lung");
        //Actinopterygii mappings
        SimAnnotToAnatEntityTO swimBladderAnatTO1 = new SimAnnotToAnatEntityTO(7, "swimbladder");
        //Sarcopterygii mappings
        SimAnnotToAnatEntityTO whateverAnatTO1 = new SimAnnotToAnatEntityTO(8, "whatever");
        SimAnnotToAnatEntityTO lungSarcoAnatTO1 = new SimAnnotToAnatEntityTO(9, "lung");

        //mappings for Gnathostomata including not-trusted
        SimAnnotToAnatEntityTOResultSet gnathostomataNotTrustedMappingsRS = getMockResultSet(
                SimAnnotToAnatEntityTOResultSet.class,
                Arrays.asList(mouthAnatTO1, mouthAnusAnatTO1, mouthAnusAnatTO2, anusAnatTO1,
                        lungSwimBladderAnatTO1, lungSwimBladderAnatTO2, fakeLungSwimBladderWhateverAnatTO1,
                        fakeLungSwimBladderWhateverAnatTO2, fakeLungSwimBladderWhateverAnatTO3,
                        lungAnatTO1, swimBladderAnatTO1, whateverAnatTO1, lungSarcoAnatTO1));
        when(this.sumSimAnnotDAO.getSimAnnotToAnatEntity(7776, true, true, true, null))
        .thenReturn(gnathostomataNotTrustedMappingsRS);
        //mappings for Gnathostomata only trusted
        SimAnnotToAnatEntityTOResultSet gnathostomataTrustedMappingsRS = getMockResultSet(
                SimAnnotToAnatEntityTOResultSet.class,
                Arrays.asList(mouthAnatTO1, mouthAnusAnatTO1, mouthAnusAnatTO2, anusAnatTO1,
                        lungSwimBladderAnatTO1, lungSwimBladderAnatTO2, fakeLungSwimBladderWhateverAnatTO1,
                        fakeLungSwimBladderWhateverAnatTO2, fakeLungSwimBladderWhateverAnatTO3,
                        lungAnatTO1, swimBladderAnatTO1, lungSarcoAnatTO1));
        when(this.sumSimAnnotDAO.getSimAnnotToAnatEntity(7776, true, true, true, true))
        .thenReturn(gnathostomataTrustedMappingsRS);
        //mappings for Sarcopterygii including not-trusted
        SimAnnotToAnatEntityTOResultSet sarcopterygiiNotTrustedMappingsRS = getMockResultSet(
                SimAnnotToAnatEntityTOResultSet.class,
                Arrays.asList(mouthAnatTO1, mouthAnusAnatTO1, mouthAnusAnatTO2, anusAnatTO1,
                        lungSwimBladderAnatTO1, lungSwimBladderAnatTO2, fakeLungSwimBladderWhateverAnatTO1,
                        fakeLungSwimBladderWhateverAnatTO2, fakeLungSwimBladderWhateverAnatTO3,
                        lungAnatTO1, whateverAnatTO1, lungSarcoAnatTO1));
        when(this.sumSimAnnotDAO.getSimAnnotToAnatEntity(8287, true, true, true, null))
        .thenReturn(sarcopterygiiNotTrustedMappingsRS);
        //mappings for Sarcopterygii including only trusted
        SimAnnotToAnatEntityTOResultSet sarcopterygiiTrustedMappingsRS = getMockResultSet(
                SimAnnotToAnatEntityTOResultSet.class,
                Arrays.asList(mouthAnatTO1, mouthAnusAnatTO1, mouthAnusAnatTO2, anusAnatTO1,
                        lungSwimBladderAnatTO1, lungSwimBladderAnatTO2, fakeLungSwimBladderWhateverAnatTO1,
                        fakeLungSwimBladderWhateverAnatTO2, fakeLungSwimBladderWhateverAnatTO3,
                        lungAnatTO1, lungSarcoAnatTO1));
        when(this.sumSimAnnotDAO.getSimAnnotToAnatEntity(8287, true, true, true, true))
        .thenReturn(sarcopterygiiTrustedMappingsRS);
        //Similarity annots for Actinopterygii including not-trusted
        SimAnnotToAnatEntityTOResultSet actinopterygiiNotTrustedMappingsRS = getMockResultSet(
                SimAnnotToAnatEntityTOResultSet.class,
                Arrays.asList(mouthAnatTO1, mouthAnusAnatTO1, mouthAnusAnatTO2, anusAnatTO1,
                        lungSwimBladderAnatTO1, lungSwimBladderAnatTO2, fakeLungSwimBladderWhateverAnatTO1,
                        fakeLungSwimBladderWhateverAnatTO2, fakeLungSwimBladderWhateverAnatTO3,
                        lungAnatTO1, swimBladderAnatTO1));
        when(this.sumSimAnnotDAO.getSimAnnotToAnatEntity(7898, true, true, true, null))
        .thenReturn(actinopterygiiNotTrustedMappingsRS);
        //Similarity annots for Cnidaria including not-trusted
        SimAnnotToAnatEntityTOResultSet cnidariaNotTrustedMappingsRS = getMockResultSet(
                SimAnnotToAnatEntityTOResultSet.class,
                Arrays.asList(mouthAnatTO1, mouthAnusAnatTO1, mouthAnusAnatTO2));
        when(this.sumSimAnnotDAO.getSimAnnotToAnatEntity(6073, true, true, true, null))
        .thenReturn(cnidariaNotTrustedMappingsRS);
        //mappings for Bilateria including not-trusted
        SimAnnotToAnatEntityTOResultSet bilateriaNotTrustedMappingsRS = getMockResultSet(
                SimAnnotToAnatEntityTOResultSet.class,
                Arrays.asList(mouthAnatTO1, mouthAnusAnatTO1, mouthAnusAnatTO2, anusAnatTO1,
                        lungSwimBladderAnatTO1, lungSwimBladderAnatTO2, fakeLungSwimBladderWhateverAnatTO1,
                        fakeLungSwimBladderWhateverAnatTO2, fakeLungSwimBladderWhateverAnatTO3,
                        lungAnatTO1, swimBladderAnatTO1, whateverAnatTO1, lungSarcoAnatTO1));
        when(this.sumSimAnnotDAO.getSimAnnotToAnatEntity(33213, true, true, true, null))
        .thenReturn(bilateriaNotTrustedMappingsRS);

        //Confidence statements
        CIOStatementTO cio1 = new CIOStatementTO("CIO:1", "trusted", null, true, null, null, null);
        CIOStatementTO cio2 = new CIOStatementTO("CIO:2", "nontrusted", null, false, null, null, null);
        CIOStatementTOResultSet cioTORS = getMockResultSet(
                CIOStatementTOResultSet.class,
                Arrays.asList(cio1, cio2));
        when(this.cioStatementDAO.getAllCIOStatements()).thenReturn(cioTORS);

        //Anat. entity ontology
        AnatEntity mouth = new AnatEntity("mouth");
        AnatEntity anus = new AnatEntity("anus");
        AnatEntity lung = new AnatEntity("lung");
        AnatEntity swimBladder = new AnatEntity("swimbladder");
        AnatEntity whatever = new AnatEntity("whatever");
        AnatEntity transfOfAdded = new AnatEntity("whatever_precursor");
        RelationTO<String> transfOfRel = new RelationTO<>(1, "whatever", "whatever_precursor",
                RelationTO.RelationType.TRANSFORMATIONOF, RelationTO.RelationStatus.DIRECT);
        //We consider two species, with species IDs 1 and 2
        TaxonConstraint<String> mouthTC1 = new TaxonConstraint<>("mouth", 1);
        TaxonConstraint<String> mouthTC2 = new TaxonConstraint<>("mouth", 2);
        TaxonConstraint<String> anusTC1 = new TaxonConstraint<>("anus", 1);
        TaxonConstraint<String> anusTC2 = new TaxonConstraint<>("anus", 2);
        //We consider lung existing only in species 1
        TaxonConstraint<String> lungTC1 = new TaxonConstraint<>("lung", 1);
        //We consider swim bladder existing only in species 2
        TaxonConstraint<String> swimBladderTC1 = new TaxonConstraint<>("swimbladder", 2);
        TaxonConstraint<String> whateverTC1 = new TaxonConstraint<>("whatever", 1);
        TaxonConstraint<String> transfOfAddedTC1 = new TaxonConstraint<>("whatever_precursor", 1);
        TaxonConstraint<Integer> transfOfTC1 = new TaxonConstraint<>(1, 1);
        MultiSpeciesOntology<AnatEntity, String> anatOnt = new MultiSpeciesOntology<>(null,
                Arrays.asList(mouth, anus, lung, swimBladder, whatever, transfOfAdded),
                Arrays.asList(transfOfRel),
                Arrays.asList(mouthTC1, mouthTC2, anusTC1, anusTC2, lungTC1, swimBladderTC1, whateverTC1,
                        transfOfAddedTC1), 
                Arrays.asList(transfOfTC1), 
                EnumSet.of(RelationType.TRANSFORMATIONOF),
                AnatEntity.class);
        when(this.ontService.getAnatEntityOntology((Collection<Integer>) null,
                new HashSet<>(Arrays.asList(mouth.getId(), anus.getId(), lung.getId(),
                        swimBladder.getId(), whatever.getId())),
                EnumSet.of(RelationType.TRANSFORMATIONOF), true, true))
        .thenReturn(anatOnt);
        anatOnt = new MultiSpeciesOntology<>(null,
                Arrays.asList(mouth, anus, lung, swimBladder),
                Arrays.asList(),
                Arrays.asList(mouthTC1, mouthTC2, anusTC1, anusTC2, lungTC1, swimBladderTC1), 
                Arrays.asList(), 
                EnumSet.of(RelationType.TRANSFORMATIONOF),
                AnatEntity.class);
        when(this.ontService.getAnatEntityOntology((Collection<Integer>) null,
                new HashSet<>(Arrays.asList(mouth.getId(), anus.getId(), lung.getId(), swimBladder.getId())),
                EnumSet.of(RelationType.TRANSFORMATIONOF), true, true))
        .thenReturn(anatOnt);
        anatOnt = new MultiSpeciesOntology<>(null,
                Arrays.asList(mouth, anus, lung, whatever, transfOfAdded),
                Arrays.asList(transfOfRel),
                Arrays.asList(mouthTC1, mouthTC2, anusTC1, anusTC2, lungTC1, whateverTC1, transfOfAddedTC1), 
                Arrays.asList(transfOfTC1), 
                EnumSet.of(RelationType.TRANSFORMATIONOF),
                AnatEntity.class);
        when(this.ontService.getAnatEntityOntology((Collection<Integer>) null,
                new HashSet<>(Arrays.asList(mouth.getId(), anus.getId(), lung.getId(), whatever.getId())),
                EnumSet.of(RelationType.TRANSFORMATIONOF), true, true))
        .thenReturn(anatOnt);
        anatOnt = new MultiSpeciesOntology<>(null,
                Arrays.asList(mouth, anus, lung),
                Arrays.asList(),
                Arrays.asList(mouthTC1, mouthTC2, anusTC1, anusTC2, lungTC1), 
                Arrays.asList(), 
                EnumSet.of(RelationType.TRANSFORMATIONOF),
                AnatEntity.class);
        when(this.ontService.getAnatEntityOntology((Collection<Integer>) null,
                new HashSet<>(Arrays.asList(mouth.getId(), anus.getId(), lung.getId())),
                EnumSet.of(RelationType.TRANSFORMATIONOF), true, true))
        .thenReturn(anatOnt);
        anatOnt = new MultiSpeciesOntology<>(null,
                Arrays.asList(mouth, anus, swimBladder),
                Arrays.asList(),
                Arrays.asList(mouthTC1, mouthTC2, anusTC1, anusTC2, swimBladderTC1), 
                Arrays.asList(), 
                EnumSet.of(RelationType.TRANSFORMATIONOF),
                AnatEntity.class);
        when(this.ontService.getAnatEntityOntology((Collection<Integer>) null,
                new HashSet<>(Arrays.asList(mouth.getId(), anus.getId(), swimBladder.getId())),
                EnumSet.of(RelationType.TRANSFORMATIONOF), true, true))
        .thenReturn(anatOnt);
        anatOnt = new MultiSpeciesOntology<>(null,
                Arrays.asList(mouth),
                Arrays.asList(),
                Arrays.asList(mouthTC1, mouthTC2), 
                Arrays.asList(), 
                EnumSet.of(RelationType.TRANSFORMATIONOF),
                AnatEntity.class);
        when(this.ontService.getAnatEntityOntology((Collection<Integer>) null,
                new HashSet<>(Arrays.asList(mouth.getId())),
                EnumSet.of(RelationType.TRANSFORMATIONOF), true, true))
        .thenReturn(anatOnt);
        anatOnt = new MultiSpeciesOntology<>(null,
                Arrays.asList(mouth, anus),
                Arrays.asList(),
                Arrays.asList(mouthTC1, mouthTC2, anusTC1, anusTC2), 
                Arrays.asList(), 
                EnumSet.of(RelationType.TRANSFORMATIONOF),
                AnatEntity.class);
        when(this.ontService.getAnatEntityOntology((Collection<Integer>) null,
                new HashSet<>(Arrays.asList(mouth.getId(), anus.getId())),
                EnumSet.of(RelationType.TRANSFORMATIONOF), true, true))
        .thenReturn(anatOnt);

        log.traceExit();
    }
    /**
     * Unit test for method {@link AnatEntitySimilarityService#loadPositiveAnatEntitySimilarities(
     * int, boolean, Collection)} making a request for Gnathostomata all annotations.
     */
    @Test
    public void shouldLoadPositiveAnatEntitySimilaritiesGnathostomeAll() {
        AnatEntitySimilarityService service = new AnatEntitySimilarityService(this.serviceFactory);

        AnatEntitySimilarity mouthGnaSim = new AnatEntitySimilarity(Arrays.asList(new AnatEntity("mouth")),
                null, taxa.get(4), Arrays.asList(new AnatEntitySimilarityTaxonSummary(taxa.get(1), true, true)),
                taxonToOnt.get(taxa.get(4)));
        AnatEntitySimilarity anusGnaSim = new AnatEntitySimilarity(Arrays.asList(new AnatEntity("anus")),
                null, taxa.get(4), Arrays.asList(new AnatEntitySimilarityTaxonSummary(taxa.get(2), true, true)),
                taxonToOnt.get(taxa.get(4)));
        AnatEntitySimilarity fakeLungSwimBladderWhateverGnaSim = new AnatEntitySimilarity(
                Arrays.asList(new AnatEntity("lung"), new AnatEntity("swimbladder"), new AnatEntity("whatever")),
                Arrays.asList(new AnatEntity("whatever_precursor")), taxa.get(4),
                Arrays.asList(new AnatEntitySimilarityTaxonSummary(taxa.get(4), true, true)),
                taxonToOnt.get(taxa.get(4)));

        Set<AnatEntitySimilarity> expectedResults = new HashSet<>(Arrays.asList(mouthGnaSim, anusGnaSim,
                fakeLungSwimBladderWhateverGnaSim));
        assertEquals(expectedResults, service.loadPositiveAnatEntitySimilarities(7776, false, null));
    }
    /**
     * Unit test for method {@link AnatEntitySimilarityService#loadPositiveAnatEntitySimilarities(
     * int, boolean, Collection)} making a request for Gnathostomata only trusted annotations.
     */
    @Test
    public void shouldLoadPositiveAnatEntitySimilaritiesGnathostomeTrusted() {
        AnatEntitySimilarityService service = new AnatEntitySimilarityService(this.serviceFactory);

        AnatEntitySimilarity mouthGnaSim = new AnatEntitySimilarity(Arrays.asList(new AnatEntity("mouth")),
                null, taxa.get(4), Arrays.asList(new AnatEntitySimilarityTaxonSummary(taxa.get(1), true, true)),
                taxonToOnt.get(taxa.get(4)));
        AnatEntitySimilarity anusGnaSim = new AnatEntitySimilarity(Arrays.asList(new AnatEntity("anus")),
                null, taxa.get(4), Arrays.asList(new AnatEntitySimilarityTaxonSummary(taxa.get(2), true, true)),
                taxonToOnt.get(taxa.get(4)));
        AnatEntitySimilarity lungSwimBladderGnaSim = new AnatEntitySimilarity(
                Arrays.asList(new AnatEntity("lung"), new AnatEntity("swimbladder")),
                Arrays.asList(), taxa.get(4),
                Arrays.asList(new AnatEntitySimilarityTaxonSummary(taxa.get(4), true, true)),
                taxonToOnt.get(taxa.get(4)));

        Set<AnatEntitySimilarity> expectedResults = new HashSet<>(Arrays.asList(mouthGnaSim, anusGnaSim,
                lungSwimBladderGnaSim));
        assertEquals(expectedResults, service.loadPositiveAnatEntitySimilarities(7776, true, null));
    }
    /**
     * Unit test for method {@link AnatEntitySimilarityService#loadPositiveAnatEntitySimilarities(
     * int, boolean, Collection)} making a request for Sarcopterygii all annotations.
     */
    @Test
    public void shouldLoadPositiveAnatEntitySimilaritiesSarcoAll() {
        AnatEntitySimilarityService service = new AnatEntitySimilarityService(this.serviceFactory);

        AnatEntitySimilarity mouthSarcoSim = new AnatEntitySimilarity(Arrays.asList(new AnatEntity("mouth")),
                null, taxa.get(5), Arrays.asList(new AnatEntitySimilarityTaxonSummary(taxa.get(1), true, true)),
                taxonToOnt.get(taxa.get(5)));
        AnatEntitySimilarity anusSarcoSim = new AnatEntitySimilarity(Arrays.asList(new AnatEntity("anus")),
                null, taxa.get(5), Arrays.asList(new AnatEntitySimilarityTaxonSummary(taxa.get(2), true, true)),
                taxonToOnt.get(taxa.get(5)));
        AnatEntitySimilarity lungSarcoSim = new AnatEntitySimilarity(
                Arrays.asList(new AnatEntity("lung")), Arrays.asList(), taxa.get(5),
                Arrays.asList(new AnatEntitySimilarityTaxonSummary(taxa.get(4), true, true),
                              new AnatEntitySimilarityTaxonSummary(taxa.get(5), true, true)),
                taxonToOnt.get(taxa.get(5)));
        AnatEntitySimilarity whateverSarcoSim = new AnatEntitySimilarity(
                Arrays.asList(new AnatEntity("whatever")),
                Arrays.asList(new AnatEntity("whatever_precursor")), taxa.get(5),
                Arrays.asList(new AnatEntitySimilarityTaxonSummary(taxa.get(5), false, true)),
                taxonToOnt.get(taxa.get(5)));

        Set<AnatEntitySimilarity> expectedResults = new HashSet<>(Arrays.asList(mouthSarcoSim, anusSarcoSim,
                lungSarcoSim, whateverSarcoSim));
        assertEquals(expectedResults, service.loadPositiveAnatEntitySimilarities(8287, false, null));
    }
    /**
     * Unit test for method {@link AnatEntitySimilarityService#loadPositiveAnatEntitySimilarities(
     * int, boolean, Collection)} making a request for Sarcopterygii only trusted annotations.
     */
    @Test
    public void shouldLoadPositiveAnatEntitySimilaritiesSarcoTrusted() {
        AnatEntitySimilarityService service = new AnatEntitySimilarityService(this.serviceFactory);

        AnatEntitySimilarity mouthSarcoSim = new AnatEntitySimilarity(Arrays.asList(new AnatEntity("mouth")),
                null, taxa.get(5), Arrays.asList(new AnatEntitySimilarityTaxonSummary(taxa.get(1), true, true)),
                taxonToOnt.get(taxa.get(5)));
        AnatEntitySimilarity anusSarcoSim = new AnatEntitySimilarity(Arrays.asList(new AnatEntity("anus")),
                null, taxa.get(5), Arrays.asList(new AnatEntitySimilarityTaxonSummary(taxa.get(2), true, true)),
                taxonToOnt.get(taxa.get(5)));
        AnatEntitySimilarity lungSarcoSim = new AnatEntitySimilarity(
                Arrays.asList(new AnatEntity("lung")), Arrays.asList(), taxa.get(5),
                Arrays.asList(new AnatEntitySimilarityTaxonSummary(taxa.get(4), true, true),
                              new AnatEntitySimilarityTaxonSummary(taxa.get(5), true, true)),
                taxonToOnt.get(taxa.get(5)));

        Set<AnatEntitySimilarity> expectedResults = new HashSet<>(Arrays.asList(mouthSarcoSim, anusSarcoSim,
                lungSarcoSim));
        assertEquals(expectedResults, service.loadPositiveAnatEntitySimilarities(8287, true, null));
    }
    /**
     * Unit test for method {@link AnatEntitySimilarityService#loadPositiveAnatEntitySimilarities(
     * int, boolean, Collection)} making a request for Actinopterygii all annotations.
     */
    @Test
    public void shouldLoadPositiveAnatEntitySimilaritiesActinoAll() {
        AnatEntitySimilarityService service = new AnatEntitySimilarityService(this.serviceFactory);

        AnatEntitySimilarity mouthActinoSim = new AnatEntitySimilarity(Arrays.asList(new AnatEntity("mouth")),
                null, taxa.get(6), Arrays.asList(new AnatEntitySimilarityTaxonSummary(taxa.get(1), true, true)),
                taxonToOnt.get(taxa.get(6)));
        AnatEntitySimilarity anusActinoSim = new AnatEntitySimilarity(Arrays.asList(new AnatEntity("anus")),
                null, taxa.get(6), Arrays.asList(new AnatEntitySimilarityTaxonSummary(taxa.get(2), true, true)),
                taxonToOnt.get(taxa.get(6)));
        AnatEntitySimilarity swimBladderActinoSim = new AnatEntitySimilarity(
                Arrays.asList(new AnatEntity("swimbladder")), Arrays.asList(), taxa.get(6),
                Arrays.asList(new AnatEntitySimilarityTaxonSummary(taxa.get(6), true, true)),
                taxonToOnt.get(taxa.get(6)));
        AnatEntitySimilarity lungActinoSim = new AnatEntitySimilarity(
                Arrays.asList(new AnatEntity("lung")), Arrays.asList(), taxa.get(6),
                Arrays.asList(new AnatEntitySimilarityTaxonSummary(taxa.get(4), true, true)),
                taxonToOnt.get(taxa.get(6)));

        Set<AnatEntitySimilarity> expectedResults = new HashSet<>(Arrays.asList(mouthActinoSim, anusActinoSim,
                swimBladderActinoSim, lungActinoSim));
        assertEquals(expectedResults, service.loadPositiveAnatEntitySimilarities(7898, false, null));
    }
    /**
     * Unit test for method {@link AnatEntitySimilarityService#loadPositiveAnatEntitySimilarities(
     * int, boolean, Collection)} making a request for Cnidaria all annotations.
     */
    @Test
    public void shouldLoadPositiveAnatEntitySimilaritiesCnidariaAll() {
        AnatEntitySimilarityService service = new AnatEntitySimilarityService(this.serviceFactory);

        AnatEntitySimilarity mouthCnidariaSim = new AnatEntitySimilarity(Arrays.asList(new AnatEntity("mouth")),
                null, taxa.get(3), Arrays.asList(new AnatEntitySimilarityTaxonSummary(taxa.get(1), true, true)),
                taxonToOnt.get(taxa.get(3)));

        Set<AnatEntitySimilarity> expectedResults = new HashSet<>(Arrays.asList(mouthCnidariaSim));
        assertEquals(expectedResults, service.loadPositiveAnatEntitySimilarities(6073, false, null));
    }
    /**
     * Unit test for method {@link AnatEntitySimilarityService#loadPositiveAnatEntitySimilarities(
     * int, boolean, Collection)} making a request for Bilateria all annotations.
     */
    @Test
    public void shouldLoadPositiveAnatEntitySimilaritiesBilateriaAll() {
        AnatEntitySimilarityService service = new AnatEntitySimilarityService(this.serviceFactory);

        AnatEntitySimilarity mouthBilateriaSim = new AnatEntitySimilarity(Arrays.asList(new AnatEntity("mouth")),
                null, taxa.get(2), Arrays.asList(new AnatEntitySimilarityTaxonSummary(taxa.get(1), true, true)),
                taxonToOnt.get(taxa.get(2)));
        AnatEntitySimilarity anusActinoSim = new AnatEntitySimilarity(Arrays.asList(new AnatEntity("anus")),
                null, taxa.get(2), Arrays.asList(new AnatEntitySimilarityTaxonSummary(taxa.get(2), true, true)),
                taxonToOnt.get(taxa.get(2)));

        Set<AnatEntitySimilarity> expectedResults = new HashSet<>(Arrays.asList(
                mouthBilateriaSim, anusActinoSim));
        assertEquals(expectedResults, service.loadPositiveAnatEntitySimilarities(33213, false, null));
    }
    /**
     * Unit test for method {@link AnatEntitySimilarityService#loadPositiveAnatEntitySimilarities(
     * int, boolean, Collection)} making a request for Actinopterygii all annotations with filtering on
     * species IDs.
     */
    @Test
    public void shouldLoadPositiveAnatEntitySimilaritiesActinoAllWithSpeciesFiltering() {
        AnatEntitySimilarityService service = new AnatEntitySimilarityService(this.serviceFactory);

        AnatEntitySimilarity mouthActinoSim = new AnatEntitySimilarity(Arrays.asList(new AnatEntity("mouth")),
                null, taxa.get(6), Arrays.asList(new AnatEntitySimilarityTaxonSummary(taxa.get(1), true, true)),
                taxonToOnt.get(taxa.get(6)));
        AnatEntitySimilarity anusActinoSim = new AnatEntitySimilarity(Arrays.asList(new AnatEntity("anus")),
                null, taxa.get(6), Arrays.asList(new AnatEntitySimilarityTaxonSummary(taxa.get(2), true, true)),
                taxonToOnt.get(taxa.get(6)));
        AnatEntitySimilarity swimBladderActinoSim = new AnatEntitySimilarity(
                Arrays.asList(new AnatEntity("swimbladder")), Arrays.asList(), taxa.get(6),
                Arrays.asList(new AnatEntitySimilarityTaxonSummary(taxa.get(6), true, true)),
                taxonToOnt.get(taxa.get(6)));

        Set<AnatEntitySimilarity> expectedResults = new HashSet<>(Arrays.asList(mouthActinoSim, anusActinoSim,
                swimBladderActinoSim));
        assertEquals(expectedResults, service.loadPositiveAnatEntitySimilarities(7898, false, Arrays.asList(2)));
    }

    @Test
    public void shouldLoadSimilarAnatEntities() {
        AnatEntitySimilarityService service = new AnatEntitySimilarityService(this.serviceFactory);

        AnatEntitySimilarity fakeLungSwimBladderWhateverGnaSim = new AnatEntitySimilarity(
                Arrays.asList(new AnatEntity("lung"), new AnatEntity("swimbladder"), new AnatEntity("whatever")),
                Arrays.asList(new AnatEntity("whatever_precursor")), taxa.get(4),
                Arrays.asList(new AnatEntitySimilarityTaxonSummary(taxa.get(4), true, true)),
                taxonToOnt.get(taxa.get(4)));

        Set<AnatEntitySimilarity> expectedResults = new HashSet<>(Arrays.asList(fakeLungSwimBladderWhateverGnaSim));
        assertEquals(expectedResults, service.loadSimilarAnatEntities(speciesIdsGnathostomataLCA,
                Arrays.asList("lung"), false));
    }

    @Test
    public void shouldLoadPositiveAnatEntitySimilarityAnalysis() {
        AnatEntitySimilarityService service = new AnatEntitySimilarityService(this.serviceFactory);

        AnatEntitySimilarity fakeLungSwimBladderWhateverGnaSim = new AnatEntitySimilarity(
                Arrays.asList(new AnatEntity("lung"), new AnatEntity("swimbladder"), new AnatEntity("whatever")),
                Arrays.asList(new AnatEntity("whatever_precursor")), taxa.get(4),
                Arrays.asList(new AnatEntitySimilarityTaxonSummary(taxa.get(4), true, true)),
                taxonToOnt.get(taxa.get(4)));

        Collection<String> requestedAnatEntityIds = Arrays.asList("lung", anatEntityWithNoSimilarityId,
                nonExistingAnatEntityId);
        Collection<String> requestedAnatEntityIdsNotFound = Arrays.asList(nonExistingAnatEntityId);
        Set<Integer> requestedSpeciesIds = new HashSet<>(speciesIdsGnathostomataLCA);
        //Add a "non-existing" species ID
        Collection<Integer> requestedSpeciesIdsNotFound = Arrays.asList(9999);
        requestedSpeciesIds.addAll(requestedSpeciesIdsNotFound);
        Collection<Species> requestedSpecies = speciesIdsGnathostomataLCA.stream().map(id -> new Species(id))
                .collect(Collectors.toSet());
        Taxon leastCommonAncestor = this.taxonService.loadLeastCommonAncestor(speciesIdsGnathostomataLCA);
        Collection<AnatEntitySimilarity> anatEntitySimilarities = Arrays.asList(fakeLungSwimBladderWhateverGnaSim);
        Collection<AnatEntity> anatEntitiesWithNoSimilarities = Arrays.asList(new AnatEntity(anatEntityWithNoSimilarityId));
        Map<AnatEntity, Collection<Species>> anatEntitiesExistInSpecies = fakeLungSwimBladderWhateverGnaSim
                .getAllAnatEntities().stream()
                .collect(Collectors.toMap(ae -> ae, ae -> Arrays.asList(new Species(1), new Species(2))));
        anatEntitiesExistInSpecies.put(new AnatEntity(anatEntityWithNoSimilarityId), Arrays.asList(new Species(1)));

        AnatEntitySimilarityAnalysis expectedResults = new AnatEntitySimilarityAnalysis(requestedAnatEntityIds,
                requestedAnatEntityIdsNotFound, requestedSpeciesIds, requestedSpeciesIdsNotFound, requestedSpecies,
                leastCommonAncestor, taxonToOnt.get(leastCommonAncestor), anatEntitySimilarities, anatEntitiesWithNoSimilarities, anatEntitiesExistInSpecies);
        assertEquals(expectedResults, service.loadPositiveAnatEntitySimilarityAnalysis(requestedSpeciesIds,
                requestedAnatEntityIds, false));
    }
    /**
     * Regression test of a key collision when using {@code Collectors.toMap} in method
     * {@code loadPositiveAnatEntitySimilarityAnalysis}, because method {@code loadAnatEntities}
     * could be called with an empty {@code Collection}, leading to retrieve all anat. entities in Bgee,
     * rather than retrieving only requested anat. entity with no similarity annotations (happened if
     * all requested anat. entities had a similarity annotation).
     */
    @Test
    public void shouldLoadPositiveAnatEntitySimilarityAnalysisRegressionTest() {
        AnatEntitySimilarityService service = new AnatEntitySimilarityService(this.serviceFactory);
        Set<Integer> requestedSpeciesIds = new HashSet<>(speciesIdsGnathostomataLCA);
        service.loadPositiveAnatEntitySimilarityAnalysis(requestedSpeciesIds,
                Arrays.asList("lung"), false);
    }

    /**
     * Stub the CIO statement DAO so that {@code getAllCIOStatements()} can be consumed multiple
     * times within a single test. The default mock from {@link #setMockObjects()} only allows a
     * single consumption because the returned mock {@code DAOResultSet} exposes a single-use
     * {@code Stream}; the slow path of
     * {@link AnatEntitySimilarityService#loadAnatEntitySimilaritiesRespectingNegations(int, boolean, Collection)}
     * also queries CIO statements once for the sub-clade similarity construction, in addition to
     * the call made by {@code loadPositiveAnatEntitySimilarities}.
     */
    private void enableMultiUseCIOStatementMock() {
        when(this.cioStatementDAO.getAllCIOStatements()).thenAnswer(inv ->
                getMockResultSet(CIOStatementTOResultSet.class, Arrays.asList(
                        new CIOStatementTO("CIO:1", "trusted", null, true, null, null, null),
                        new CIOStatementTO("CIO:2", "nontrusted", null, false, null, null, null))));
    }

    /**
     * Unit test for method
     * {@link AnatEntitySimilarityService#loadAnatEntitySimilaritiesRespectingNegations(int, boolean, Collection)}:
     * when no negative annotation is valid for the requested taxon (Actinopterygii) or any of its
     * ancestors, the new method should return the same set of similarities as
     * {@link AnatEntitySimilarityService#loadPositiveAnatEntitySimilarities(int, boolean, Collection)}.
     */
    @Test
    public void shouldLoadAnatEntitySimilaritiesRespectingNegationsNoNegativeEqualsBaseline() {
        //No negative annotation valid for Actinopterygii or any ancestor.
        SummarySimilarityAnnotationTOResultSet emptyNegAnnotRS = getMockResultSet(
                SummarySimilarityAnnotationTOResultSet.class,
                Collections.<SummarySimilarityAnnotationTO>emptyList());
        when(this.sumSimAnnotDAO.getSummarySimilarityAnnotations(7898, true, false, false, null, null))
                .thenReturn(emptyNegAnnotRS);

        AnatEntitySimilarityService service = new AnatEntitySimilarityService(this.serviceFactory);

        AnatEntitySimilarity mouthActinoSim = new AnatEntitySimilarity(Arrays.asList(new AnatEntity("mouth")),
                null, taxa.get(6), Arrays.asList(new AnatEntitySimilarityTaxonSummary(taxa.get(1), true, true)),
                taxonToOnt.get(taxa.get(6)));
        AnatEntitySimilarity anusActinoSim = new AnatEntitySimilarity(Arrays.asList(new AnatEntity("anus")),
                null, taxa.get(6), Arrays.asList(new AnatEntitySimilarityTaxonSummary(taxa.get(2), true, true)),
                taxonToOnt.get(taxa.get(6)));
        AnatEntitySimilarity swimBladderActinoSim = new AnatEntitySimilarity(
                Arrays.asList(new AnatEntity("swimbladder")), Arrays.asList(), taxa.get(6),
                Arrays.asList(new AnatEntitySimilarityTaxonSummary(taxa.get(6), true, true)),
                taxonToOnt.get(taxa.get(6)));
        AnatEntitySimilarity lungActinoSim = new AnatEntitySimilarity(
                Arrays.asList(new AnatEntity("lung")), Arrays.asList(), taxa.get(6),
                Arrays.asList(new AnatEntitySimilarityTaxonSummary(taxa.get(4), true, true)),
                taxonToOnt.get(taxa.get(6)));

        Set<AnatEntitySimilarity> expectedResults = new HashSet<>(Arrays.asList(mouthActinoSim, anusActinoSim,
                swimBladderActinoSim, lungActinoSim));
        assertEquals(expectedResults, service.loadAnatEntitySimilaritiesRespectingNegations(7898, false));
    }

    /**
     * Unit test for method
     * {@link AnatEntitySimilarityService#loadAnatEntitySimilaritiesRespectingNegations(int, boolean, Collection)}:
     * a negative annotation that blocks an inherited positive similarity (here, "mouth" annotated
     * as not homologous at Cnidaria, while a positive annotation exists at the ancestor Eumetazoa)
     * and for which no positive annotation exists at any strict descendant of the requested taxon
     * must cause the blocked similarity to be dropped from the result. In this case Cnidaria has
     * no descendants in the test taxonomy, so "mouth" is dropped without substitution.
     */
    @Test
    public void shouldLoadAnatEntitySimilaritiesRespectingNegationsBlockAndDrop() {
        SummarySimilarityAnnotationTO mouthNegCnidariaTO = new SummarySimilarityAnnotationTO(
                10, 6073 /* Cnidaria */, true /* negated */, "CIO:1");
        SimAnnotToAnatEntityTO mouthNegCnidariaMappingTO = new SimAnnotToAnatEntityTO(10, "mouth");

        SummarySimilarityAnnotationTOResultSet negAnnotRS = getMockResultSet(
                SummarySimilarityAnnotationTOResultSet.class, Arrays.asList(mouthNegCnidariaTO));
        SimAnnotToAnatEntityTOResultSet negMappingRS = getMockResultSet(
                SimAnnotToAnatEntityTOResultSet.class, Arrays.asList(mouthNegCnidariaMappingTO));
        SummarySimilarityAnnotationTOResultSet emptyDescPosAnnotRS = getMockResultSet(
                SummarySimilarityAnnotationTOResultSet.class,
                Collections.<SummarySimilarityAnnotationTO>emptyList());

        when(this.sumSimAnnotDAO.getSummarySimilarityAnnotations(6073, true, false, false, null, null))
                .thenReturn(negAnnotRS);
        when(this.sumSimAnnotDAO.getSimAnnotToAnatEntity(6073, true, false, false, null))
                .thenReturn(negMappingRS);
        //No positive annotation at any strict descendant of Cnidaria (Cnidaria is a leaf in the
        //test taxonomy).
        when(this.sumSimAnnotDAO.getSummarySimilarityAnnotations(6073, false, true, true, null, null))
                .thenReturn(emptyDescPosAnnotRS);

        AnatEntitySimilarityService service = new AnatEntitySimilarityService(this.serviceFactory);
        //"mouth" was the only similarity returned at Cnidaria (see
        //shouldLoadPositiveAnatEntitySimilaritiesCnidariaAll); with the new method it is blocked
        //and discarded.
        Set<AnatEntitySimilarity> expectedResults = Collections.emptySet();
        assertEquals(expectedResults, service.loadAnatEntitySimilaritiesRespectingNegations(6073, false));
    }

    /**
     * Unit test for method
     * {@link AnatEntitySimilarityService#loadAnatEntitySimilaritiesRespectingNegations(int, boolean, Collection)}:
     * when a negative annotation blocks a UBERON term ("swimbladder" annotated as not homologous
     * at Bilateria) and a positive annotation exists for the same UBERON term at a single strict
     * descendant of the requested taxon (here, "swimbladder" positively annotated at
     * Actinopterygii), one sub-clade {@code AnatEntitySimilarity} is emitted, scoped to that
     * descendant taxon. The blocking negative annotation is preserved as a documentation summary
     * (with {@link AnatEntitySimilarityTaxonSummary#isPositive()} returning {@code false}).
     */
    @Test
    public void shouldLoadAnatEntitySimilaritiesRespectingNegationsBlockAndSplitSingleSubClade() {
        enableMultiUseCIOStatementMock();

        SummarySimilarityAnnotationTO swimBladderNegBilateriaTO = new SummarySimilarityAnnotationTO(
                10, 33213 /* Bilateria */, true /* negated */, "CIO:1");
        SimAnnotToAnatEntityTO swimBladderNegBilateriaMappingTO = new SimAnnotToAnatEntityTO(
                10, "swimbladder");
        //Existing positive annotation for "swimbladder" at Actinopterygii (id 7, see
        //setMockObjects): we reuse the same TO to drive the descendant-positive query.
        SummarySimilarityAnnotationTO swimBladderActinoPosTO = new SummarySimilarityAnnotationTO(
                7, 7898 /* Actinopterygii */, false, "CIO:1");
        SimAnnotToAnatEntityTO swimBladderActinoPosMappingTO = new SimAnnotToAnatEntityTO(7, "swimbladder");

        SummarySimilarityAnnotationTOResultSet negAnnotRS = getMockResultSet(
                SummarySimilarityAnnotationTOResultSet.class, Arrays.asList(swimBladderNegBilateriaTO));
        SimAnnotToAnatEntityTOResultSet negMappingRS = getMockResultSet(
                SimAnnotToAnatEntityTOResultSet.class, Arrays.asList(swimBladderNegBilateriaMappingTO));
        SummarySimilarityAnnotationTOResultSet descPosAnnotRS = getMockResultSet(
                SummarySimilarityAnnotationTOResultSet.class, Arrays.asList(swimBladderActinoPosTO));
        SimAnnotToAnatEntityTOResultSet descPosMappingRS = getMockResultSet(
                SimAnnotToAnatEntityTOResultSet.class, Arrays.asList(swimBladderActinoPosMappingTO));

        when(this.sumSimAnnotDAO.getSummarySimilarityAnnotations(33213, true, false, false, null, null))
                .thenReturn(negAnnotRS);
        when(this.sumSimAnnotDAO.getSimAnnotToAnatEntity(33213, true, false, false, null))
                .thenReturn(negMappingRS);
        when(this.sumSimAnnotDAO.getSummarySimilarityAnnotations(33213, false, true, true, null, null))
                .thenReturn(descPosAnnotRS);
        when(this.sumSimAnnotDAO.getSimAnnotToAnatEntity(33213, false, true, true, null))
                .thenReturn(descPosMappingRS);

        //AnatEntity ontology fetched for the union of blocked anat-entity IDs ({"swimbladder"}).
        AnatEntity swimBladder = new AnatEntity("swimbladder");
        TaxonConstraint<String> swimBladderTC = new TaxonConstraint<>("swimbladder", 2);
        MultiSpeciesOntology<AnatEntity, String> swimBladderOnt = new MultiSpeciesOntology<>(null,
                Arrays.asList(swimBladder),
                Arrays.asList(),
                Arrays.asList(swimBladderTC),
                Arrays.asList(),
                EnumSet.of(RelationType.TRANSFORMATIONOF),
                AnatEntity.class);
        when(this.ontService.getAnatEntityOntology((Collection<Integer>) null,
                new HashSet<>(Arrays.asList("swimbladder")),
                EnumSet.of(RelationType.TRANSFORMATIONOF), true, true))
                .thenReturn(swimBladderOnt);

        AnatEntitySimilarityService service = new AnatEntitySimilarityService(this.serviceFactory);

        //Baseline at Bilateria (see shouldLoadPositiveAnatEntitySimilaritiesBilateriaAll).
        AnatEntitySimilarity mouthBilateriaSim = new AnatEntitySimilarity(Arrays.asList(new AnatEntity("mouth")),
                null, taxa.get(2), Arrays.asList(new AnatEntitySimilarityTaxonSummary(taxa.get(1), true, true)),
                taxonToOnt.get(taxa.get(2)));
        AnatEntitySimilarity anusBilateriaSim = new AnatEntitySimilarity(Arrays.asList(new AnatEntity("anus")),
                null, taxa.get(2), Arrays.asList(new AnatEntitySimilarityTaxonSummary(taxa.get(2), true, true)),
                taxonToOnt.get(taxa.get(2)));
        //New sub-clade similarity for "swimbladder" with the positive at Actinopterygii (taxa[6])
        //and the blocking negative at Bilateria (taxa[2]).
        AnatEntitySimilarity swimBladderSubCladeSim = new AnatEntitySimilarity(
                Arrays.asList(new AnatEntity("swimbladder")), Arrays.asList(), taxa.get(2),
                Arrays.asList(
                        new AnatEntitySimilarityTaxonSummary(taxa.get(6), true, true),
                        new AnatEntitySimilarityTaxonSummary(taxa.get(2), true, false)),
                taxonToOnt.get(taxa.get(2)));

        Set<AnatEntitySimilarity> expectedResults = new HashSet<>(Arrays.asList(
                mouthBilateriaSim, anusBilateriaSim, swimBladderSubCladeSim));
        assertEquals(expectedResults, service.loadAnatEntitySimilaritiesRespectingNegations(33213, false));
        assertEquals(taxa.get(6), swimBladderSubCladeSim.getHomologyScopeTaxon());
    }

    /**
     * Unit test for method
     * {@link AnatEntitySimilarityService#loadAnatEntitySimilaritiesRespectingNegations(int, boolean, Collection)}:
     * when descendant positive annotations are themselves nested (one taxon being an ancestor of
     * the other in the annotation set), they must be merged into a single sub-clade
     * {@code AnatEntitySimilarity} carrying both descendant taxon summaries, rather than producing
     * two separate similarities. Here, "lung" is blocked by a negative at Bilateria, and two
     * positives exist at descendants: Gnathostomata (ancestor of Sarcopterygii) and Sarcopterygii.
     * Gnathostomata is maximal, so a single sub-clade similarity is emitted carrying summaries for
     * both descendant taxa plus the blocking negative.
     */
    @Test
    public void shouldLoadAnatEntitySimilaritiesRespectingNegationsBlockAndSplitNestedDescendantsMerge() {
        enableMultiUseCIOStatementMock();

        SummarySimilarityAnnotationTO lungNegBilateriaTO = new SummarySimilarityAnnotationTO(
                11, 33213 /* Bilateria */, true /* negated */, "CIO:1");
        SimAnnotToAnatEntityTO lungNegBilateriaMappingTO = new SimAnnotToAnatEntityTO(11, "lung");
        //Existing positive annotations for "lung" at Gnathostomata (id 6) and Sarcopterygii (id 9).
        SummarySimilarityAnnotationTO lungGnaPosTO = new SummarySimilarityAnnotationTO(
                6, 7776 /* Gnathostomata */, false, "CIO:1");
        SummarySimilarityAnnotationTO lungSarcoPosTO = new SummarySimilarityAnnotationTO(
                9, 8287 /* Sarcopterygii */, false, "CIO:1");
        SimAnnotToAnatEntityTO lungGnaPosMappingTO = new SimAnnotToAnatEntityTO(6, "lung");
        SimAnnotToAnatEntityTO lungSarcoPosMappingTO = new SimAnnotToAnatEntityTO(9, "lung");

        SummarySimilarityAnnotationTOResultSet negAnnotRS = getMockResultSet(
                SummarySimilarityAnnotationTOResultSet.class, Arrays.asList(lungNegBilateriaTO));
        SimAnnotToAnatEntityTOResultSet negMappingRS = getMockResultSet(
                SimAnnotToAnatEntityTOResultSet.class, Arrays.asList(lungNegBilateriaMappingTO));
        SummarySimilarityAnnotationTOResultSet descPosAnnotRS = getMockResultSet(
                SummarySimilarityAnnotationTOResultSet.class, Arrays.asList(lungGnaPosTO, lungSarcoPosTO));
        SimAnnotToAnatEntityTOResultSet descPosMappingRS = getMockResultSet(
                SimAnnotToAnatEntityTOResultSet.class,
                Arrays.asList(lungGnaPosMappingTO, lungSarcoPosMappingTO));

        when(this.sumSimAnnotDAO.getSummarySimilarityAnnotations(33213, true, false, false, null, null))
                .thenReturn(negAnnotRS);
        when(this.sumSimAnnotDAO.getSimAnnotToAnatEntity(33213, true, false, false, null))
                .thenReturn(negMappingRS);
        when(this.sumSimAnnotDAO.getSummarySimilarityAnnotations(33213, false, true, true, null, null))
                .thenReturn(descPosAnnotRS);
        when(this.sumSimAnnotDAO.getSimAnnotToAnatEntity(33213, false, true, true, null))
                .thenReturn(descPosMappingRS);

        AnatEntity lung = new AnatEntity("lung");
        TaxonConstraint<String> lungTC = new TaxonConstraint<>("lung", 1);
        MultiSpeciesOntology<AnatEntity, String> lungOnt = new MultiSpeciesOntology<>(null,
                Arrays.asList(lung),
                Arrays.asList(),
                Arrays.asList(lungTC),
                Arrays.asList(),
                EnumSet.of(RelationType.TRANSFORMATIONOF),
                AnatEntity.class);
        when(this.ontService.getAnatEntityOntology((Collection<Integer>) null,
                new HashSet<>(Arrays.asList("lung")),
                EnumSet.of(RelationType.TRANSFORMATIONOF), true, true))
                .thenReturn(lungOnt);

        AnatEntitySimilarityService service = new AnatEntitySimilarityService(this.serviceFactory);

        AnatEntitySimilarity mouthBilateriaSim = new AnatEntitySimilarity(Arrays.asList(new AnatEntity("mouth")),
                null, taxa.get(2), Arrays.asList(new AnatEntitySimilarityTaxonSummary(taxa.get(1), true, true)),
                taxonToOnt.get(taxa.get(2)));
        AnatEntitySimilarity anusBilateriaSim = new AnatEntitySimilarity(Arrays.asList(new AnatEntity("anus")),
                null, taxa.get(2), Arrays.asList(new AnatEntitySimilarityTaxonSummary(taxa.get(2), true, true)),
                taxonToOnt.get(taxa.get(2)));
        //Single sub-clade similarity merging the two nested descendant positives (Gnathostomata
        //is maximal because Sarcopterygii is one of its descendants).
        AnatEntitySimilarity lungSubCladeMergedSim = new AnatEntitySimilarity(
                Arrays.asList(new AnatEntity("lung")), Arrays.asList(), taxa.get(2),
                Arrays.asList(
                        new AnatEntitySimilarityTaxonSummary(taxa.get(4), true, true),
                        new AnatEntitySimilarityTaxonSummary(taxa.get(5), true, true),
                        new AnatEntitySimilarityTaxonSummary(taxa.get(2), true, false)),
                taxonToOnt.get(taxa.get(2)));

        Set<AnatEntitySimilarity> expectedResults = new HashSet<>(Arrays.asList(
                mouthBilateriaSim, anusBilateriaSim, lungSubCladeMergedSim));
        assertEquals(expectedResults, service.loadAnatEntitySimilaritiesRespectingNegations(33213, false));
        assertEquals(taxa.get(4), lungSubCladeMergedSim.getHomologyScopeTaxon());
    }

    @Test
    public void shouldReturnHomologyScopeTaxonFromPositiveSummaries() {
        AnatEntitySimilarity mouthGnaSim = new AnatEntitySimilarity(Arrays.asList(new AnatEntity("mouth")),
                null, taxa.get(4), Arrays.asList(new AnatEntitySimilarityTaxonSummary(taxa.get(1), true, true)),
                taxonToOnt.get(taxa.get(4)));
        assertEquals(taxa.get(1), mouthGnaSim.getHomologyScopeTaxon());
    }
}