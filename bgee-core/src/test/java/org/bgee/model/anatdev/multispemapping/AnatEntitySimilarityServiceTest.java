package org.bgee.model.anatdev.multispemapping;

import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;

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
import org.bgee.model.species.Taxon;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link AnatEntitySimilarityService}.
 *
 * @author  Frederic Bastian
 * @version Bgee 14 Mar 2019
 * @since   Bgee 14 Mar 2019
 */
public class AnatEntitySimilarityServiceTest extends TestAncestor {
    private final static Logger log = LogManager.getLogger(AnatEntitySimilarityServiceTest.class.getName());
    @Override
    protected Logger getLogger() {
        return log;
    }

    @Before
    public void setMockObjects() {
        log.entry();
        List<Taxon> taxa = Arrays.asList(
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

        //Create taxon ontologies
        //Taxon ontology when requested taxon is 8287 Sarcopterygii
        Ontology<Taxon, Integer> taxOnt = new Ontology<>(null,
                Arrays.asList(taxa.get(0), taxa.get(1), taxa.get(2), taxa.get(4), taxa.get(5)),
                Arrays.asList(taxonRelations.get(4), taxonRelations.get(5), taxonRelations.get(6),
                        taxonRelations.get(7), taxonRelations.get(8), taxonRelations.get(9),
                        taxonRelations.get(10), taxonRelations.get(11), taxonRelations.get(12)),
                EnumSet.of(RelationType.ISA_PARTOF),
                serviceFactory, Taxon.class);
        when(this.ontService.getTaxonOntologyFromTaxonIds(Collections.singleton(8287), false, true, true))
        .thenReturn(taxOnt);
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
                serviceFactory, Taxon.class);
        when(this.ontService.getTaxonOntologyFromTaxonIds(Collections.singleton(7776), false, true, true))
        .thenReturn(taxOnt);
        //Taxon ontology when requested taxon is 7898 Actinopterygii
        taxOnt = new Ontology<>(null,
                Arrays.asList(taxa.get(0), taxa.get(1), taxa.get(2), taxa.get(4), taxa.get(6)),
                Arrays.asList(taxonRelations.get(0), taxonRelations.get(1), taxonRelations.get(2),
                        taxonRelations.get(3), taxonRelations.get(8), taxonRelations.get(9),
                        taxonRelations.get(10), taxonRelations.get(11), taxonRelations.get(12)),
                EnumSet.of(RelationType.ISA_PARTOF),
                serviceFactory, Taxon.class);
        when(this.ontService.getTaxonOntologyFromTaxonIds(Collections.singleton(7898), false, true, true))
        .thenReturn(taxOnt);
        //Taxon ontology when requested taxon is 6073 Cnidaria
        taxOnt = new Ontology<>(null,
                Arrays.asList(taxa.get(0), taxa.get(1), taxa.get(3)),
                Arrays.asList(taxonRelations.get(13), taxonRelations.get(14)),
                EnumSet.of(RelationType.ISA_PARTOF),
                serviceFactory, Taxon.class);
        when(this.ontService.getTaxonOntologyFromTaxonIds(Collections.singleton(6073), false, true, true))
        .thenReturn(taxOnt);

        //Create SummarySimilarityAnnotationTOs.
        //Eumetazoa annot
        SummarySimilarityAnnotationTO mouthAnnotTO = new SummarySimilarityAnnotationTO(1, 6072,
                false, "CIO:1");
        //Bilateria annots
        SummarySimilarityAnnotationTO mouthAnusAnnotTO = new SummarySimilarityAnnotationTO(2, 33213,
                false, "CIO:1");
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

        //Similarity annots for Gnathostomata including not-trusted
        SummarySimilarityAnnotationTOResultSet gnathostomataNotTrustedRS = getMockResultSet(
                SummarySimilarityAnnotationTOResultSet.class,
                Arrays.asList(mouthAnnotTO, mouthAnusAnnotTO, anusAnnotTO, lungSwimBladderAnnotTO,
                        fakeLungSwimBladderWhateverAnnotTO, lungAnnotTO, swimBladderAnnotTO, whateverAnnotTO));
        when(this.sumSimAnnotDAO.getSummarySimilarityAnnotations(7776, true, true, true, null, null))
        .thenReturn(gnathostomataNotTrustedRS);
        //Similarity annots for Gnathostomata only trusted
        SummarySimilarityAnnotationTOResultSet gnathostomataTrustedRS = getMockResultSet(
                SummarySimilarityAnnotationTOResultSet.class,
                Arrays.asList(mouthAnnotTO, mouthAnusAnnotTO, anusAnnotTO, fakeLungSwimBladderWhateverAnnotTO,
                        lungAnnotTO, swimBladderAnnotTO, whateverAnnotTO));
        when(this.sumSimAnnotDAO.getSummarySimilarityAnnotations(7776, true, true, true, true, null))
        .thenReturn(gnathostomataTrustedRS);
        //Similarity annots for Sarcopterygii including not-trusted
        SummarySimilarityAnnotationTOResultSet sarcopterygiiNotTrustedRS = getMockResultSet(
                SummarySimilarityAnnotationTOResultSet.class,
                Arrays.asList(mouthAnnotTO, mouthAnusAnnotTO, anusAnnotTO, lungSwimBladderAnnotTO,
                        fakeLungSwimBladderWhateverAnnotTO, lungAnnotTO));
        when(this.sumSimAnnotDAO.getSummarySimilarityAnnotations(8287, true, true, true, null, null))
        .thenReturn(sarcopterygiiNotTrustedRS);
        //Similarity annots for Actinopterygii including not-trusted
        SummarySimilarityAnnotationTOResultSet actinopterygiiNotTrustedRS = getMockResultSet(
                SummarySimilarityAnnotationTOResultSet.class,
                Arrays.asList(mouthAnnotTO, mouthAnusAnnotTO, anusAnnotTO, lungSwimBladderAnnotTO,
                        fakeLungSwimBladderWhateverAnnotTO, lungAnnotTO, swimBladderAnnotTO, whateverAnnotTO));
        when(this.sumSimAnnotDAO.getSummarySimilarityAnnotations(7898, true, true, true, null, null))
        .thenReturn(actinopterygiiNotTrustedRS);

        //Create SimAnnotToAnatEntityTOs.
        //Eumetazoa mappings
        SimAnnotToAnatEntityTO mouthAnatTO1 = new SimAnnotToAnatEntityTO(1, "mouth");
        //Bilateria mappings
        SimAnnotToAnatEntityTO mouthAnusAnatTO1 = new SimAnnotToAnatEntityTO(2, "mouth");
        SimAnnotToAnatEntityTO mouthAnusAnatTO2 = new SimAnnotToAnatEntityTO(2, "anus");
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

        //mappings for Gnathostomata including not-trusted
        SimAnnotToAnatEntityTOResultSet gnathostomataNotTrustedMappingsRS = getMockResultSet(
                SimAnnotToAnatEntityTOResultSet.class,
                Arrays.asList(mouthAnatTO1, mouthAnusAnatTO1, mouthAnusAnatTO2, anusAnatTO1,
                        lungSwimBladderAnatTO1, lungSwimBladderAnatTO2, fakeLungSwimBladderWhateverAnatTO1,
                        fakeLungSwimBladderWhateverAnatTO2, fakeLungSwimBladderWhateverAnatTO3,
                        lungAnatTO1, swimBladderAnatTO1, whateverAnatTO1));
        when(this.sumSimAnnotDAO.getSimAnnotToAnatEntity(7776, true, true, true, null))
        .thenReturn(gnathostomataNotTrustedMappingsRS);
        //TODO: continue for other taxa

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
        TaxonConstraint<String> mouthTC1 = new TaxonConstraint<>("mouth", 1);
        TaxonConstraint<String> mouthTC2 = new TaxonConstraint<>("mouth", 2);
        TaxonConstraint<String> anusTC1 = new TaxonConstraint<>("anus", 1);
        TaxonConstraint<String> anusTC2 = new TaxonConstraint<>("anus", 2);
        TaxonConstraint<String> lungTC1 = new TaxonConstraint<>("lung", 1);
        TaxonConstraint<String> swimBladderTC1 = new TaxonConstraint<>("swimBladder", 2);
        TaxonConstraint<String> whateverTC1 = new TaxonConstraint<>("whatever", 1);
        TaxonConstraint<String> transfOfAddedTC1 = new TaxonConstraint<>("whatever_precursor", 1);
        TaxonConstraint<Integer> transfOfTC1 = new TaxonConstraint<>(1, 1);
        MultiSpeciesOntology<AnatEntity, String> anatOnt = new MultiSpeciesOntology<>(null,
                Arrays.asList(mouth, anus, lung, swimBladder),
                Arrays.asList(),
                Arrays.asList(mouthTC1, mouthTC2, anusTC1, anusTC2, lungTC1, swimBladderTC1), 
                Arrays.asList(), 
                EnumSet.of(RelationType.TRANSFORMATIONOF),
                this.serviceFactory, AnatEntity.class);
        when(this.ontService.getAnatEntityOntology((Collection<Integer>) null,
                new HashSet<>(Arrays.asList(mouth.getId(), anus.getId(), lung.getId(),
                        swimBladder.getId())),
                EnumSet.of(RelationType.TRANSFORMATIONOF), true, true))
        .thenReturn(anatOnt);
        //TODO: continue for other requested anat. entity ID collections
        

        log.exit();
    }
    /**
     * Unit test for method {@link AnatEntitySimilarityService#loadPositiveAnatEntitySimilarities(
     * int, boolean, Collection)}
     */
    @Test
    public void shouldLoadPositiveAnatEntitySimilarities() {
        AnatEntitySimilarityService service = new AnatEntitySimilarityService(this.serviceFactory);
        service.loadPositiveAnatEntitySimilarities(7776, false, null);
    }
}
