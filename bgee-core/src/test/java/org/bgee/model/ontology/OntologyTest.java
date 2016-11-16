package org.bgee.model.ontology;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bgee.model.ServiceFactory;
import org.bgee.model.TestAncestor;
import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.anatdev.DevStage;
import org.bgee.model.anatdev.TaxonConstraint;
import org.bgee.model.anatdev.TaxonConstraintService;
import org.bgee.model.dao.api.ontologycommon.RelationDAO.RelationTO;
import org.bgee.model.dao.api.ontologycommon.RelationDAO.RelationTO.RelationStatus;
import org.bgee.model.species.Taxon;
import org.junit.Test;

/**
 * This class holds the unit tests for the {@code OntologyBase} class.
 * 
 * @author  Valentine Rech de Laval
 * @version Bgee 13, Nov. 2016
 * @since   Bgee 13, Dec. 2015
 */
public class OntologyTest extends TestAncestor {
  
    private static Set<RelationType> ALL_RELATIONS = EnumSet.allOf(RelationType.class);
    private static Set<RelationType> ISA_RELATIONS = EnumSet.of(RelationType.ISA_PARTOF);

    /**
     * Test the methods:
     * - {@link OntologyBase#getAncestors(Entity)},
     * - {@link OntologyBase#getAncestors(NamedEntity, boolean)},
     * - {@link OntologyBase#getAncestors(NamedEntity, Collection)}, and
     * - {@link OntologyBase#getAncestors(NamedEntity, Collection, boolean)}
     */
    @Test
    public void shouldGetAncestors() {
        
        ServiceFactory serviceFactory = mock(ServiceFactory.class);

        AnatEntity ae1 = new AnatEntity("UBERON:0001", "A", "A description"); 
        AnatEntity ae2 = new AnatEntity("UBERON:0002", "B", "B description"); 
        AnatEntity ae2p = new AnatEntity("UBERON:0002p", "Bprime", "Bprime description"); 
        AnatEntity ae3 = new AnatEntity("UBERON:0003", "C", "C description"); 
        Set<AnatEntity> elements = new HashSet<>(Arrays.asList(ae1, ae2, ae2p, ae3));
        Set<RelationTO> relations = this.getAnatEntityRelationTOs();

        Ontology<AnatEntity> ontology = new Ontology<AnatEntity>("sp1",
                elements, relations, ALL_RELATIONS, serviceFactory, AnatEntity.class);
        
        Set<AnatEntity> ancestors = ontology.getAncestors(ae3);
        Set<AnatEntity> expAncestors = new HashSet<>(Arrays.asList(ae1, ae2, ae2p));
        assertEquals("Incorrects ancestors", expAncestors, ancestors);
        
        ancestors = ontology.getAncestors(ae3, ALL_RELATIONS);
        assertEquals("Incorrects ancestors", expAncestors, ancestors);

        ancestors = ontology.getAncestors(ae3, null, false);
        assertEquals("Incorrects ancestors", expAncestors, ancestors);

        ancestors = ontology.getAncestors(ae3, true);
        expAncestors = new HashSet<>(Arrays.asList(ae2, ae2p));
        assertEquals("Incorrects ancestors", expAncestors, ancestors);

        ancestors = ontology.getAncestors(ae2p, EnumSet.of(RelationType.ISA_PARTOF), false);
        expAncestors = new HashSet<>();
        assertEquals("Incorrects ancestors", expAncestors, ancestors);

        ancestors = ontology.getAncestors(ae2p, null);
        expAncestors = new HashSet<>(Arrays.asList(ae1));
        assertEquals("Incorrects ancestors", expAncestors, ancestors);

        ancestors = ontology.getAncestors(ae1, null);
        expAncestors = new HashSet<>();
        assertEquals("Incorrects ancestors", expAncestors, ancestors);
    }
    
    /**
     * Test the method {@link OntologyBase#getAncestors(Collection, NamedEntity, Collection, boolean)}.
     */
    @Test
    public void shouldGetAncestors_multiSpecies() {
        ServiceFactory mockFact = mock(ServiceFactory.class);
        TaxonConstraintService tcService = mock(TaxonConstraintService.class);
        when(mockFact.getTaxonConstraintService()).thenReturn(tcService);

        AnatEntity ae1 = new AnatEntity("UBERON:0001"), ae2 = new AnatEntity("UBERON:0002"), 
                ae2p = new AnatEntity("UBERON:0002p"), ae3 = new AnatEntity("UBERON:0003"); 
        Set<AnatEntity> elements = new HashSet<>(Arrays.asList(ae1, ae2, ae2p, ae3));
        Set<RelationTO> relations = this.getAnatEntityRelationTOs();

        Set<TaxonConstraint> taxonConstraint = new HashSet<>(Arrays.asList(
                    // UBERON:0001 sp1/sp2/sp3 --------------------
                    // |                    \                      |
                    // UBERON:0002 sp1/sp2   UBERON:0002p sp2/sp3  | 
                    // |                    /                      |
                    // UBERON:0003 sp1/sp2 ------------------------
                    new TaxonConstraint("UBERON:0001", null),
                    new TaxonConstraint("UBERON:0002", "sp1"),
                    new TaxonConstraint("UBERON:0002", "sp2"),
                    new TaxonConstraint("UBERON:0002p", "sp2"),
                    new TaxonConstraint("UBERON:0002p", "sp3"),
                    new TaxonConstraint("UBERON:0003", "sp1"),
                    new TaxonConstraint("UBERON:0003", "sp2")));
        
        Set<TaxonConstraint> relationTaxonConstraint = new HashSet<>(Arrays.asList(
                    // UBERON:0001 ------------------
                    // | sp1/sp2   \ sp2            |
                    // UBERON:0002   UBERON:0002p   | sp2/sp1 (indirect)
                    // | sp1       / sp2            |
                    // UBERON:0003 ------------------
                    new TaxonConstraint("1", "sp1"),
                    new TaxonConstraint("1", "sp2"),
                    new TaxonConstraint("2", "sp2"),
                    new TaxonConstraint("3", "sp1"),
                    new TaxonConstraint("4", "sp2"),
                    new TaxonConstraint("5", "sp1"),
                    new TaxonConstraint("5", "sp2")));

        MultiSpeciesOntology<AnatEntity> ontology = new MultiSpeciesOntology<AnatEntity>(
                Arrays.asList("sp1", "sp2", "sp3"), elements, relations,
                taxonConstraint, relationTaxonConstraint, ALL_RELATIONS, 
                mockFact, AnatEntity.class);

        List<String> speciesIds = Arrays.asList("sp1");
        Set<AnatEntity> ancestors = ontology.getAncestors(ae3, ALL_RELATIONS, false, speciesIds);
        Set<AnatEntity> expAncestors = new HashSet<>(Arrays.asList(ae1, ae2));
        assertEquals("Incorrects ancestors", expAncestors, ancestors);

        ancestors = ontology.getAncestors(ae3, ALL_RELATIONS, true, speciesIds);
        expAncestors = new HashSet<>(Arrays.asList(ae2));
        assertEquals("Incorrects ancestors", expAncestors, ancestors);

        ancestors = ontology.getAncestors(ae3, false, speciesIds);
        expAncestors = new HashSet<>(Arrays.asList(ae1, ae2));
        assertEquals("Incorrects ancestors", expAncestors, ancestors);

        speciesIds = Arrays.asList("sp2");
        ancestors = ontology.getAncestors(ae3, ISA_RELATIONS, false, speciesIds);
        expAncestors = new HashSet<>(Arrays.asList(ae1, ae2p));
        assertEquals("Incorrects ancestors", expAncestors, ancestors);

        speciesIds = Arrays.asList("sp3");
        try {
            ancestors = ontology.getAncestors(ae3, ALL_RELATIONS, false, speciesIds);
            fail("Should fail due to element not in provided species");
        } catch (IllegalArgumentException e) {
            // Test passed
        }
    }
    
    /**
     * Test the methods:
     * - {@link OntologyBase#getDescendants(Entity)},
     * - {@link OntologyBase#getDescendants(NamedEntity, boolean)},
     * - {@link OntologyBase#getDescendants(NamedEntity, Collection)}, and
     * - {@link OntologyBase#getDescendants(NamedEntity, Collection, boolean)}
     */
    @Test
    public void shouldGetDescendants() {
        ServiceFactory mockFact = mock(ServiceFactory.class);

        AnatEntity ae1 = new AnatEntity("UBERON:0001", "A", "A description"); 
        AnatEntity ae2 = new AnatEntity("UBERON:0002", "B", "B description"); 
        AnatEntity ae2p = new AnatEntity("UBERON:0002p", "Bprime", "Bprime description"); 
        AnatEntity ae3 = new AnatEntity("UBERON:0003", "C", "C description"); 
        Set<AnatEntity> elements = new HashSet<>(Arrays.asList(ae1, ae2, ae2p, ae3));
        Set<RelationTO> relations = this.getAnatEntityRelationTOs();

        OntologyBase<AnatEntity> ontology = new Ontology<>("sp1", elements, 
                relations, ALL_RELATIONS, mockFact, AnatEntity.class);
        
        Set<AnatEntity> descendants = ontology.getDescendants(ae1);
        Set<AnatEntity> expDescendants = new HashSet<>(Arrays.asList(ae2, ae2p, ae3));
        assertEquals("Incorrects descendants", expDescendants, descendants);
        
        descendants = ontology.getDescendants(ae1, ISA_RELATIONS);
        expDescendants = new HashSet<>(Arrays.asList(ae2, ae3));
        assertEquals("Incorrects descendants", expDescendants, descendants);

        descendants = ontology.getDescendants(ae1, ALL_RELATIONS, true);
        expDescendants = new HashSet<>(Arrays.asList(ae2, ae2p));
        assertEquals("Incorrects descendants", expDescendants, descendants);

        descendants = ontology.getDescendants(ae1, true);
        assertEquals("Incorrects descendants", expDescendants, descendants);

        descendants = ontology.getDescendants(ae2, ALL_RELATIONS);
        expDescendants = new HashSet<>(Arrays.asList(ae3));
        assertEquals("Incorrects descendants", expDescendants, descendants);

        descendants = ontology.getDescendants(ae3, ALL_RELATIONS);
        expDescendants = new HashSet<>();
        assertEquals("Incorrects descendants", expDescendants, descendants);
    }
    
    /**
     * Test the method {@link OntologyBase#getDescendants(Collection, NamedEntity, Collection, boolean)}.
     */
    @Test
    public void shouldGetDescendants_multiSpecies() {
        ServiceFactory mockFact = mock(ServiceFactory.class);
        TaxonConstraintService tcService = mock(TaxonConstraintService.class);
        when(mockFact.getTaxonConstraintService()).thenReturn(tcService);

        Set<String> speciesIds = new HashSet<>(Arrays.asList("sp2"));
        // Get stage taxon constraints
        Set<TaxonConstraint> taxonConstraint = new HashSet<>(Arrays.asList(
        // stage1 sp1/sp2 -------
        // |               \     \    
        // stage2 sp1/sp2   |     stage2p sp2
        // |               /      | 
        // stage3 sp1             stage3p sp2
                new TaxonConstraint("stage1", null),
                new TaxonConstraint("stage2", null),
                new TaxonConstraint("stage2p", "sp2"),
                new TaxonConstraint("stage3p", "sp2")));

        DevStage ds1 = new DevStage("stage1"), ds2 = new DevStage("stage2"), 
                ds3 = new DevStage("stage3"), ds2p = new DevStage("stage2p"), 
                ds3p = new DevStage("stage3p"); 

        Set<DevStage> elements = new HashSet<>(Arrays.asList(ds1, ds2, ds2p, ds3, ds3p));
        Set<RelationTO> relations = new HashSet<>(Arrays.asList(
                // stage1 -----------------------------------
                // | is_a  \                    \ dev_from   \   
                // stage2   |                    stage2p      | is_a (indirect)
                // | is_a  / is_a (indirect)     | is_a      /
                // stage3                        stage3p ----   
                new RelationTO("1", "stage2", "stage1", RelationTO.RelationType.ISA_PARTOF, RelationStatus.DIRECT),
                new RelationTO("2", "stage3", "stage2", RelationTO.RelationType.ISA_PARTOF, RelationStatus.DIRECT),
                new RelationTO("3", "stage3", "stage1", RelationTO.RelationType.ISA_PARTOF, RelationStatus.INDIRECT),

                new RelationTO("4", "stage2p", "stage1", RelationTO.RelationType.DEVELOPSFROM, RelationStatus.DIRECT),
                new RelationTO("5", "stage3p", "stage2p", RelationTO.RelationType.ISA_PARTOF, RelationStatus.DIRECT),
                new RelationTO("6", "stage3p", "stage1", RelationTO.RelationType.ISA_PARTOF, RelationStatus.INDIRECT)));

        MultiSpeciesOntology<DevStage> ontology = new MultiSpeciesOntology<>(speciesIds,
                elements, relations, taxonConstraint, null, ALL_RELATIONS, mockFact, DevStage.class);

        Set<DevStage> descendants = ontology.getDescendants(ds1, ISA_RELATIONS);
        Set<DevStage> expDescendants = new HashSet<>(Arrays.asList(ds2, ds3, ds3p));
        assertEquals("Incorrects descendants", expDescendants, descendants);

        descendants = ontology.getDescendants(ds2, ALL_RELATIONS);
        expDescendants = new HashSet<>(Arrays.asList(ds3));
        assertEquals("Incorrects descendants", expDescendants, descendants);

        descendants = ontology.getDescendants(ds3, ALL_RELATIONS);
        expDescendants = new HashSet<>();
        assertEquals("Incorrects descendants", expDescendants, descendants);

        // with provided species
        descendants = ontology.getDescendants(ds1, ISA_RELATIONS, false, speciesIds);
        expDescendants = new HashSet<>(Arrays.asList(ds2, ds3p));
        assertEquals("Incorrects descendants", expDescendants, descendants);

        descendants = ontology.getDescendants(ds1, ISA_RELATIONS, true, speciesIds);
        expDescendants = new HashSet<>(Arrays.asList(ds2));
        assertEquals("Incorrects descendants", expDescendants, descendants);

        descendants = ontology.getDescendants(ds1, ALL_RELATIONS, false, speciesIds);
        expDescendants = new HashSet<>(Arrays.asList(ds2, ds2p, ds3p));
        assertEquals("Incorrects descendants", expDescendants, descendants);

        descendants = ontology.getDescendants(ds1, false, speciesIds);
        expDescendants = new HashSet<>(Arrays.asList(ds2, ds2p, ds3p));
        assertEquals("Incorrects descendants", expDescendants, descendants);

        descendants = ontology.getDescendants(ds1, ALL_RELATIONS, true, speciesIds);
        expDescendants = new HashSet<>(Arrays.asList(ds2, ds2p));
        assertEquals("Incorrects descendants", expDescendants, descendants);

        descendants = ontology.getDescendants(ds2, ALL_RELATIONS, false, speciesIds);
        expDescendants = new HashSet<>();
        assertEquals("Incorrects descendants", expDescendants, descendants);

        descendants = ontology.getDescendants(ds3, ALL_RELATIONS);
        expDescendants = new HashSet<>();
        assertEquals("Incorrects descendants", expDescendants, descendants);
    }

    /**
     * Test the method {@link OntologyBase#getElement(String)}.
     */
    @Test
    public void shouldGetElement() {
        AnatEntity ae1 = new AnatEntity("UBERON:0001", "A", "A description"); 
        AnatEntity ae2 = new AnatEntity("UBERON:0002", "B", "B description"); 

        Set<AnatEntity> elements = new HashSet<>(Arrays.asList(ae1, ae2));
        Set<RelationTO> relations = this.getAnatEntityRelationTOs();

        ServiceFactory mockFact = mock(ServiceFactory.class);
        OntologyBase<AnatEntity> ontology = new Ontology<>("sp1", elements,
                relations, ALL_RELATIONS, mockFact, AnatEntity.class);
        
        assertEquals("Incorrect element", ae1, ontology.getElement("UBERON:0001"));
        assertEquals("Incorrect element", ae2, ontology.getElement("UBERON:0002"));
        assertEquals("Incorrect element", null, ontology.getElement("UBERON:XXXX"));
    }
    
    /**
     * Test the method {@link OntologyBase#getElements(String)}.
     */
    @Test
    public void shouldGetElements() {
        ServiceFactory mockFact = mock(ServiceFactory.class);
        TaxonConstraintService tcService = mock(TaxonConstraintService.class);
        when(mockFact.getTaxonConstraintService()).thenReturn(tcService);
        // UBERON:0001 sp1/sp2/sp3 --------------------
        // |                    \                      |
        // UBERON:0002 sp1/sp2   UBERON:0002p sp2/sp3  | 
        // |                    /                      |
        // UBERON:0003 sp1/sp2 ------------------------

        List<String> speciesIds = Arrays.asList("sp1", "sp2", "sp3");
        Set<TaxonConstraint> relationTaxonConstraint = new HashSet<>(
                Arrays.asList(new TaxonConstraint("rel1", "sp3")));

        Set<TaxonConstraint> taxonConstraints = new HashSet<>(Arrays.asList(
                new TaxonConstraint("UBERON:0001", null),
                new TaxonConstraint("UBERON:0002", "sp1"),
                new TaxonConstraint("UBERON:0002", "sp2"),
                new TaxonConstraint("UBERON:0002p", "sp2"),
                new TaxonConstraint("UBERON:0002p", "sp3"),
                new TaxonConstraint("UBERON:0003", "sp2"),
                new TaxonConstraint("UBERON:0003", "sp3")));

        AnatEntity ae1 = new AnatEntity("UBERON:0001"), ae2 = new AnatEntity("UBERON:0002"), 
                ae2p = new AnatEntity("UBERON:0002p"), ae3 = new AnatEntity("UBERON:0003"); 

        Set<AnatEntity> elements = new HashSet<>(Arrays.asList(ae1, ae2, ae2p, ae3));
        Set<RelationTO> relations = this.getAnatEntityRelationTOs();

        MultiSpeciesOntology<AnatEntity> ontology = new MultiSpeciesOntology<>(speciesIds,
                elements, relations, taxonConstraints, relationTaxonConstraint, ALL_RELATIONS, 
                mockFact, AnatEntity.class);

        HashSet<AnatEntity> expectedAE = new HashSet<>(Arrays.asList(ae1, ae2, ae2p, ae3));
        assertEquals("Incorrect element", expectedAE, ontology.getElements());

        expectedAE = new HashSet<>(Arrays.asList(ae1, ae2p, ae3));
        assertEquals("Incorrect element", expectedAE, ontology.getElements(Arrays.asList("sp3")));
    }

    /**
     * Test the method {@link OntologyBase#getOrderedRelations(NamedEntity)}.
     */
    @Test
    public void shouldGetOrderedRelations() {
        ServiceFactory mockFact = mock(ServiceFactory.class);
        TaxonConstraintService tcService = mock(TaxonConstraintService.class);
        when(mockFact.getTaxonConstraintService()).thenReturn(tcService);

        Set<String> speciesIds = new HashSet<>(Arrays.asList("sp2"));
        // Get stage taxon constraints
        when(tcService.loadDevStageTaxonConstraintBySpeciesIds(speciesIds))
            .thenReturn( // stage1 sp1/sp2 ---
                         // |                 \    
                         // stage2 sp1/sp2     stage5 sp2
                         // |                  | 
                         // stage3 sp1         stage6 sp2
                         // |
                         // stage4 sp1
                    new HashSet<>(Arrays.asList(
                            new TaxonConstraint("stage1", null),
                            new TaxonConstraint("stage2", null),
                            new TaxonConstraint("stage3", "sp1"),
                            new TaxonConstraint("stage4", "sp1"),
                            new TaxonConstraint("stage5", "sp2"),
                            new TaxonConstraint("stage6", "sp2"))).stream());

        DevStage ds1 = new DevStage("stage1"), ds2 = new DevStage("stage2"), 
                ds3 = new DevStage("stage3"), ds4 = new DevStage("stage4"), 
                ds5 = new DevStage("stage5"), ds6 = new DevStage("stage6"); 

        Set<DevStage> elements = new HashSet<>(Arrays.asList(ds1, ds2, ds3, ds4, ds5, ds6));
        RelationTO rel1 = new RelationTO("3", "stage4", "stage3", RelationTO.RelationType.ISA_PARTOF, RelationStatus.DIRECT);
        RelationTO rel2 = new RelationTO("4", "stage3", "stage2", RelationTO.RelationType.ISA_PARTOF, RelationStatus.DIRECT);
        RelationTO rel3 = new RelationTO("6", "stage2", "stage1", RelationTO.RelationType.ISA_PARTOF, RelationStatus.DIRECT);
        Collection<RelationTO> relations = Arrays.asList(
                // stage1 ---
                // | is_a     \ dev_from   
                // stage2      stage5
                // | is_a      | is_a
                // stage3      stage6
                // | is_a
                // stage4   
            rel2,
            new RelationTO("1", "stage4", "stage1", RelationTO.RelationType.ISA_PARTOF, RelationStatus.INDIRECT),
            new RelationTO("2", "stage4", "stage2", RelationTO.RelationType.ISA_PARTOF, RelationStatus.INDIRECT),
            rel1,
            new RelationTO("5", "stage3", "stage1", RelationTO.RelationType.ISA_PARTOF, RelationStatus.INDIRECT),
            new RelationTO("8", "stage6", "stage1", RelationTO.RelationType.ISA_PARTOF, RelationStatus.INDIRECT),
            rel3,
            new RelationTO("7", "stage6", "stage5", RelationTO.RelationType.ISA_PARTOF, RelationStatus.DIRECT),
            new RelationTO("9", "stage5", "stage1", RelationTO.RelationType.ISA_PARTOF, RelationStatus.DIRECT));

        MultiSpeciesOntology<DevStage> ontology = new MultiSpeciesOntology<>(speciesIds,
                elements, relations, null, null, ALL_RELATIONS, mockFact, DevStage.class);

        List<RelationTO> actualOrderedRels = ontology.getOrderedRelations(ds4);
        
        List<RelationTO> expectedOrderedRels = Arrays.asList(rel1, rel2, rel3);
        
        assertEquals("Incorrect count of relations", expectedOrderedRels.size(), actualOrderedRels.size());
        for (int i = 0; i < expectedOrderedRels.size(); i++) {
            assertEquals("Incorrect relation ID", expectedOrderedRels.get(i).getId(),
                    actualOrderedRels.get(i).getId());
        }
    }
    
    /**
     * Test the method {@link OntologyBase#getOrderedRelations(NamedEntity)}.
     */
    @Test
    public void shouldGetOrderedAncestors() {
        ServiceFactory mockFact = mock(ServiceFactory.class);

        Set<String> speciesIds = new HashSet<>(Arrays.asList("sp1", "sp2", "sp3"));

        Taxon tax1 = new Taxon("taxId1"), tax2 = new Taxon("taxId2"), tax3 = new Taxon("taxId3"), 
            tax10 = new Taxon("taxId10"), tax100 = new Taxon("taxId100"); 
        // tax100--
        // |        \
        // tax10     \
        // |     \    \
        // tax1  tax2  tax3
        // |     |     |
        // sp1   sp2   sp3
        
        Set<Taxon> elements = new HashSet<>(Arrays.asList(tax1, tax2, tax3, tax10, tax100));
        RelationTO rel1 = new RelationTO("1", "taxId1", "taxId10", RelationTO.RelationType.ISA_PARTOF, RelationStatus.DIRECT);
        RelationTO rel1b = new RelationTO("1b", "taxId1", "taxId100", RelationTO.RelationType.DEVELOPSFROM, RelationStatus.INDIRECT);
        RelationTO rel2 = new RelationTO("2", "taxId2", "taxId10", RelationTO.RelationType.ISA_PARTOF, RelationStatus.DIRECT);
        RelationTO rel2b = new RelationTO("2b", "taxId2", "taxId100", RelationTO.RelationType.ISA_PARTOF, RelationStatus.INDIRECT);
        RelationTO rel3 = new RelationTO("3", "taxId3", "taxId100", RelationTO.RelationType.ISA_PARTOF, RelationStatus.DIRECT);
        RelationTO rel4 = new RelationTO("4", "taxId10", "taxId100", RelationTO.RelationType.DEVELOPSFROM, RelationStatus.DIRECT);
        Set<RelationTO> relations = new HashSet<>(Arrays.asList(rel1, rel1b, rel2, rel2b, rel3, rel4));

        MultiSpeciesOntology<Taxon> ontology = new MultiSpeciesOntology<>(speciesIds,
                elements, relations, null, null, ALL_RELATIONS, mockFact, Taxon.class);
        assertEquals("Incorrect ordered ancestors",
            Arrays.asList(tax10, tax100), ontology.getOrderedAncestors(tax1));
        
        assertEquals("Incorrect ordered ancestors",
            Arrays.asList(tax100), ontology.getOrderedAncestors(tax10));

        assertEquals("Incorrect ordered ancestors",
            Arrays.asList(), ontology.getOrderedAncestors(tax100));
        
        assertEquals("Incorrect ordered ancestors",
            Arrays.asList(tax100), ontology.getOrderedAncestors(tax3));
        
        assertEquals("Incorrect ordered ancestors",
            Arrays.asList(tax10), ontology.getOrderedAncestors(tax1, ISA_RELATIONS));

        try {
            ontology.getOrderedAncestors(new Taxon("taxIdX"));
            fail("Should throws an exception");
        } catch (IllegalArgumentException e) {
            // Test passed
        }
    }
    
    /**
     * Get relations for tests.
     * 
     * @return  The {@code Set} of {@code RelationTO}s that are the relations to be used for tests.
     */
    private Set<RelationTO> getAnatEntityRelationTOs() {
        Set<RelationTO> relations = new HashSet<>(Arrays.asList(
                // UBERON:0001 ------------------
                // | is_a       \ dev_from      |
                // UBERON:0002   UBERON:0002p   | is_a (indirect)
                // | is_a       / is_a          |
                // UBERON:0003 ------------------
                new RelationTO("1", "UBERON:0002", "UBERON:0001", RelationTO.RelationType.ISA_PARTOF, RelationStatus.DIRECT),
                new RelationTO("2", "UBERON:0002p", "UBERON:0001", RelationTO.RelationType.DEVELOPSFROM, RelationStatus.DIRECT),
                new RelationTO("3", "UBERON:0003", "UBERON:0002", RelationTO.RelationType.ISA_PARTOF, RelationStatus.DIRECT),
                new RelationTO("4", "UBERON:0003", "UBERON:0002p", RelationTO.RelationType.ISA_PARTOF, RelationStatus.DIRECT),
                new RelationTO("5", "UBERON:0003", "UBERON:0001", RelationTO.RelationType.ISA_PARTOF, RelationStatus.INDIRECT),
                new RelationTO("6", "totoA", "totoB", RelationTO.RelationType.ISA_PARTOF, RelationStatus.DIRECT)));
        return relations;
    }
}
