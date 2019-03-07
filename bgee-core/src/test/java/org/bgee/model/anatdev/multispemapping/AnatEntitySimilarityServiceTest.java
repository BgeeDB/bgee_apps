package org.bgee.model.anatdev.multispemapping;

import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.TestAncestor;
import org.bgee.model.dao.api.anatdev.mapping.SummarySimilarityAnnotationDAO.SummarySimilarityAnnotationTO;
import org.bgee.model.dao.api.anatdev.mapping.SummarySimilarityAnnotationDAO.SummarySimilarityAnnotationTOResultSet;
import org.bgee.model.dao.api.ontologycommon.RelationDAO.RelationTO;
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
                new RelationTO<>(15, 6073, 131567, RelationTO.RelationType.ISA_PARTOF, RelationTO.RelationStatus.INDIRECT),
                new RelationTO<>(16, 6072, 131567, RelationTO.RelationType.ISA_PARTOF, RelationTO.RelationStatus.DIRECT));

        //Create taxon ontologies
        //TODO: update the indexes
        //Taxon ontology when requested taxon is 8287 Sarcopterygii
        when(this.ontService.getTaxonOntologyFromTaxonIds(Collections.singleton(8287), false, true, true))
        .thenReturn(new Ontology<>(null, Arrays.asList(taxa.get(0), taxa.get(1), taxa.get(2), taxa.get(3), taxa.get(4)),
                Arrays.asList(taxonRelations.get(4), taxonRelations.get(5), taxonRelations.get(6), taxonRelations.get(7),
                        taxonRelations.get(8), taxonRelations.get(9), taxonRelations.get(10), taxonRelations.get(11),
                        taxonRelations.get(12), taxonRelations.get(13)), EnumSet.of(RelationType.ISA_PARTOF),
                serviceFactory, Taxon.class));
        //Taxon ontology when requested taxon is 7776 Gnathostomata
        when(this.ontService.getTaxonOntologyFromTaxonIds(Collections.singleton(7776), false, true, true))
        .thenReturn(new Ontology<>(null, taxa, taxonRelations, EnumSet.of(RelationType.ISA_PARTOF),
                serviceFactory, Taxon.class));
        //TODO: continue for taxon Actinopterygii and Cnidaria

        //Create SummarySimilarityAnnotationTOs.
        //Eumetazoa annot
        SummarySimilarityAnnotationTO mouthAnnotTO = new SummarySimilarityAnnotationTO(1, 6072, false, "CIO:1");
        //Bilateria annots
        SummarySimilarityAnnotationTO mouthAnusAnnotTO = new SummarySimilarityAnnotationTO(2, 33213, false, "CIO:1");
        SummarySimilarityAnnotationTO anusAnnotTO = new SummarySimilarityAnnotationTO(3, 33213, false, "CIO:1");
        //Gnathostomata annots
        SummarySimilarityAnnotationTO lungSwimBladderAnnotTO = new SummarySimilarityAnnotationTO(4, 7776, false, "CIO:1");
        SummarySimilarityAnnotationTO fakeLungSwimBladderWhateverAnnotTO = new SummarySimilarityAnnotationTO(5, 7776, false, "CIO:1");
        SummarySimilarityAnnotationTO lungAnnotTO = new SummarySimilarityAnnotationTO(6, 7776, false, "CIO:1");
        //Actinopterygii annots
        SummarySimilarityAnnotationTO swimBladderAnnotTO = new SummarySimilarityAnnotationTO(7, 7898, false, "CIO:1");
        SummarySimilarityAnnotationTO whateverAnnotTO = new SummarySimilarityAnnotationTO(8, 8287, false, "CIO:1");

        //Similarity annots for Gnathostomata including not-trusted
        SummarySimilarityAnnotationTOResultSet gnathostomataNotTrustedRS = getMockResultSet(
                SummarySimilarityAnnotationTOResultSet.class,
                Arrays.asList(mouthAnnotTO, mouthAnusAnnotTO, anusAnnotTO, lungSwimBladderAnnotTO,
                        fakeLungSwimBladderWhateverAnnotTO, lungAnnotTO, swimBladderAnnotTO, whateverAnnotTO));
        when(this.sumSimAnnotDAO.getSummarySimilarityAnnotations(7776, true, true, true, false, null))
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
        when(this.sumSimAnnotDAO.getSummarySimilarityAnnotations(8287, true, true, true, false, null))
        .thenReturn(sarcopterygiiNotTrustedRS);
        //Similarity annots for Actinopterygii including not-trusted
        SummarySimilarityAnnotationTOResultSet actinopterygiiNotTrustedRS = getMockResultSet(
                SummarySimilarityAnnotationTOResultSet.class,
                Arrays.asList(mouthAnnotTO, mouthAnusAnnotTO, anusAnnotTO, lungSwimBladderAnnotTO,
                        fakeLungSwimBladderWhateverAnnotTO, lungAnnotTO, swimBladderAnnotTO, whateverAnnotTO));
        when(this.sumSimAnnotDAO.getSummarySimilarityAnnotations(7898, true, true, true, false, null))
        .thenReturn(actinopterygiiNotTrustedRS);
        log.exit();
    }
    /**
     * Unit test for method {@link AnatEntitySimilarityService#loadPositiveAnatEntitySimilarities(int, boolean)}
     */
    @Test
    public void shouldLoadPositiveAnatEntitySimilarities() {
        
    }
}
