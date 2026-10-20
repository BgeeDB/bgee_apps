package org.bgee.model.species;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bgee.model.ServiceFactory;
import org.bgee.model.TestAncestor;
import org.bgee.model.ontology.Ontology;
import org.bgee.model.ontology.OntologyService;
import org.bgee.model.ontology.RelationType;
import org.bgee.model.source.Source;
import org.junit.Test;

/**
 * Unit tests for {@link TaxonTreeService}.
 *
 * @author  Harald Detering
 * @version Bgee 16, Mar. 2026
 * @since   Bgee 16, Mar. 2026
 */
public class TaxonTreeServiceTest extends TestAncestor {

    private static final List<Taxon> TAXA = Arrays.asList(
            new Taxon(1, "root", "desc1", "Root", 1, true),
            new Taxon(2, "genus", "desc2", "Genus", 2, true),
            new Taxon(3, "species1", "desc3", "Species1", 3, false),
            new Taxon(4, "species2", "desc4", "Species2", 3, true));

    /**
     * Test {@link TaxonTreeService#buildTaxonTreeWithSpecies(Collection, Map)} with provided
     * species map. Tree structure: root(1) -> genus(2) -> [species1(3), species2(4)].
     * Species are attached at genus level (parentTaxonId=2).
     */
    @Test
    public void shouldBuildTaxonTreeWithSpeciesWhenSpeciesProvided() {
        ServiceFactory serviceFactory = mock(ServiceFactory.class);
        OntologyService ontService = mock(OntologyService.class);

        when(serviceFactory.getOntologyService()).thenReturn(ontService);

        Ontology<Taxon, Integer> taxonOnt = new Ontology<>(null,
                new HashSet<>(TAXA),
                Arrays.asList(
                        new org.bgee.model.dao.api.ontologycommon.RelationDAO.RelationTO<>(
                                null, 2, 1,
                                org.bgee.model.dao.api.ontologycommon.RelationDAO.RelationTO.RelationType.ISA_PARTOF,
                                org.bgee.model.dao.api.ontologycommon.RelationDAO.RelationTO.RelationStatus.DIRECT),
                        new org.bgee.model.dao.api.ontologycommon.RelationDAO.RelationTO<>(
                                null, 3, 2,
                                org.bgee.model.dao.api.ontologycommon.RelationDAO.RelationTO.RelationType.ISA_PARTOF,
                                org.bgee.model.dao.api.ontologycommon.RelationDAO.RelationTO.RelationStatus.DIRECT),
                        new org.bgee.model.dao.api.ontologycommon.RelationDAO.RelationTO<>(
                                null, 4, 2,
                                org.bgee.model.dao.api.ontologycommon.RelationDAO.RelationTO.RelationType.ISA_PARTOF,
                                org.bgee.model.dao.api.ontologycommon.RelationDAO.RelationTO.RelationStatus.DIRECT)),
                EnumSet.of(RelationType.ISA_PARTOF),
                Taxon.class);

        when(ontService.getTaxonOntologyLeadingToSpecies(
                org.mockito.ArgumentMatchers.<Collection<Integer>>any(), eq(true), eq(false)))
                .thenReturn(taxonOnt);

        Species spe1 = new Species(100, "Human", null, "Homo", "sapiens", "v1", "asm1",
                new Source(1), 0, 2, null, null, 1);
        Species spe2 = new Species(101, "Mouse", null, "Mus", "musculus", "v2", "asm2",
                new Source(1), 0, 2, null, null, 2);
        Map<Integer, Species> speciesById = Map.of(100, spe1, 101, spe2);

        TaxonTreeService service = new TaxonTreeService(serviceFactory);
        TaxonWithSpecies root = service.buildTaxonTreeWithSpecies(
                Arrays.asList(100, 101), speciesById);

        assertNotNull("Root should not be null", root);
        assertEquals("Root taxon should be taxon 1", TAXA.get(0), root.getTaxon());
        assertTrue("Root should have no direct species", root.getSpecies().isEmpty());
        assertEquals("Root should have 1 child (genus)", 1, root.getChildren().size());

        TaxonWithSpecies genusNode = root.getChildren().get(0);
        assertEquals("Child should be taxon 2 (genus)", TAXA.get(1), genusNode.getTaxon());
        assertEquals("Genus should have 2 species", 2, genusNode.getSpecies().size());
        assertTrue("Genus should contain spe1", genusNode.getSpecies().contains(spe1));
        assertTrue("Genus should contain spe2", genusNode.getSpecies().contains(spe2));
        assertEquals("Genus should have 2 children (species taxa)", 2, genusNode.getChildren().size());

        List<Taxon> childTaxa = genusNode.getChildren().stream()
                .map(TaxonWithSpecies::getTaxon)
                .collect(Collectors.toList());
        assertTrue("Children should include taxon 3", childTaxa.contains(TAXA.get(2)));
        assertTrue("Children should include taxon 4", childTaxa.contains(TAXA.get(3)));
    }

    /**
     * Test {@link TaxonTreeService#buildTaxonTreeWithSpecies(Collection, Map)} rejects null
     * or empty speciesIds.
     */
    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowWhenSpeciesIdsNull() {
        TaxonTreeService service = new TaxonTreeService(mock(ServiceFactory.class));
        service.buildTaxonTreeWithSpecies(null, Collections.emptyMap());
    }

    /**
     * Test {@link TaxonTreeService#buildTaxonTreeWithSpecies(Collection, Map)} rejects empty
     * speciesIds.
     */
    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowWhenSpeciesIdsEmpty() {
        TaxonTreeService service = new TaxonTreeService(mock(ServiceFactory.class));
        service.buildTaxonTreeWithSpecies(Collections.emptyList(), Collections.emptyMap());
    }
}
