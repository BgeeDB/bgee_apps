package org.bgee.model.ontology;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import org.bgee.model.ServiceFactory;
import org.bgee.model.TestAncestor;
import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.anatdev.DevStage;
import org.bgee.model.anatdev.TaxonConstraint;
import org.bgee.model.anatdev.TaxonConstraintService;
import org.bgee.model.dao.api.ontologycommon.RelationDAO.RelationTO;
import org.bgee.model.dao.api.ontologycommon.RelationDAO.RelationTO.RelationStatus;
import org.bgee.model.ontology.Ontology.RelationType;
import org.junit.Test;

/**
 * This class holds the unit tests for the {@code MultiSpeciesOntology} class.
 * 
 * @author  Valentine Rech de Laval
 * @version Bgee 13, May 2016
 * @since   Bgee 13, May 2016
 */
public class MultiSpeciesOntologyTest extends TestAncestor {

    private static Set<RelationType> ALL_RELATIONS = EnumSet.allOf(RelationType.class);
    private static Set<RelationType> ISA_RELATIONS = EnumSet.of(Ontology.RelationType.ISA_PARTOF);
    /**
     * Test the method {@link MultiSpeciesOntology#getElements(String)}.
     */
    @Test
    public void shouldGetElements() {
        ServiceFactory mockFact = mock(ServiceFactory.class);
        TaxonConstraintService tcService = mock(TaxonConstraintService.class);
        when(mockFact.getTaxonConstraintService()).thenReturn(tcService);

        AnatEntity ae1 = new AnatEntity("UBERON:0001"), ae2 = new AnatEntity("UBERON:0002"), 
                ae2p = new AnatEntity("UBERON:0002p"), ae3 = new AnatEntity("UBERON:0003"); 

        Set<AnatEntity> elements = new HashSet<>(Arrays.asList(ae1, ae2, ae2p, ae3));
        Set<RelationTO> relations = this.getAnatEntityRelationTOs();

        MultiSpeciesOntology<AnatEntity> ontology = new MultiSpeciesOntology<>(
                elements, relations, ALL_RELATIONS, mockFact, AnatEntity.class);

        HashSet<AnatEntity> expectedAE = new HashSet<>(Arrays.asList(ae1, ae2, ae2p, ae3));

        assertEquals("Incorrect element", expectedAE, ontology.getElements());

        Set<String> species = new HashSet<>(Arrays.asList("sp1"));
        when(tcService.loadAnatEntityTaxonConstraintBySpeciesIds(species)).thenReturn(
                new HashSet<>(Arrays.asList(
                        new TaxonConstraint("UBERON:0001", null),
                        new TaxonConstraint("UBERON:0002", null),
                        new TaxonConstraint("UBERON:0003", null))).stream());

        expectedAE = new HashSet<>(Arrays.asList(ae1, ae2, ae3));
        assertEquals("Incorrect element", expectedAE, ontology.getElements("sp1"));
    }

    /**
     * Test the method {@link MultiSpeciesOntology#
     * getAncestors(String, org.bgee.model.NamedEntity, java.util.Collection, boolean)}.
     */
    @Test
    public void shouldGetAncestors() {
        ServiceFactory mockFact = mock(ServiceFactory.class);
        TaxonConstraintService tcService = mock(TaxonConstraintService.class);
        when(mockFact.getTaxonConstraintService()).thenReturn(tcService);

        AnatEntity ae1 = new AnatEntity("UBERON:0001"), ae2 = new AnatEntity("UBERON:0002"), 
                ae2p = new AnatEntity("UBERON:0002p"), ae3 = new AnatEntity("UBERON:0003"); 
        Set<AnatEntity> elements = new HashSet<>(Arrays.asList(ae1, ae2, ae2p, ae3));
        Set<RelationTO> relations = this.getAnatEntityRelationTOs();

        MultiSpeciesOntology<AnatEntity> ontology = new MultiSpeciesOntology<AnatEntity>(elements, 
                relations, ALL_RELATIONS, mockFact, AnatEntity.class);

        Set<TaxonConstraint> tc_sp1 = new HashSet<>(Arrays.asList(
                // UBERON:0001 sp1/sp2/sp3 --------------------
                // |                    \                      |
                // UBERON:0002 sp1/sp2   UBERON:0002p sp2/sp3  | 
                // |                    /                      |
                // UBERON:0003 sp1/sp2 ------------------------
                new TaxonConstraint("UBERON:0001", null),
                new TaxonConstraint("UBERON:0002", "sp1"),
                new TaxonConstraint("UBERON:0003", "sp1")));
        when(tcService.loadAnatEntityTaxonConstraintBySpeciesIds(new HashSet<>(Arrays.asList("sp1"))))
            .thenReturn(tc_sp1.stream()).thenReturn(tc_sp1.stream())
            .thenReturn(tc_sp1.stream()).thenReturn(tc_sp1.stream());

        Set<TaxonConstraint> relTc_sp1 = new HashSet<>(Arrays.asList(
                // UBERON:0001 ------------------
                // | sp1/sp2   \ sp2            |
                // UBERON:0002   UBERON:0002p   | sp2/sp1 (indirect)
                // | sp1       / sp2            |
                // UBERON:0003 ------------------
                new TaxonConstraint("1", "sp1"),
                new TaxonConstraint("3", "sp1"),
                new TaxonConstraint("5", "sp1")));
        when(tcService.loadAnatEntityRelationTaxonConstraintBySpeciesIds(new HashSet<>(Arrays.asList("sp1"))))
            .thenReturn(relTc_sp1.stream()).thenReturn(relTc_sp1.stream());

        Set<AnatEntity> ancestors = ontology.getAncestors("sp1", ae3, ALL_RELATIONS, false);
        Set<AnatEntity> expAncestors = new HashSet<>(Arrays.asList(ae1, ae2));
        assertEquals("Incorrects ancestors", expAncestors, ancestors);

        ancestors = ontology.getAncestors("sp1", ae3, ALL_RELATIONS, true);
        expAncestors = new HashSet<>(Arrays.asList(ae2));
        assertEquals("Incorrects ancestors", expAncestors, ancestors);

        Set<TaxonConstraint> tc_sp2 = new HashSet<>(Arrays.asList(
                // UBERON:0001 sp1/sp2/sp3 --------------------
                // |                    \                      |
                // UBERON:0002 sp1/sp2   UBERON:0002p sp2/sp3  | 
                // |                    /                      |
                // UBERON:0003 sp1/sp2 ------------------------
                new TaxonConstraint("UBERON:0001", null),
                new TaxonConstraint("UBERON:0002", "sp2"),
                new TaxonConstraint("UBERON:0002p", "sp2"),
                new TaxonConstraint("UBERON:0003", "sp2")));
        when(tcService.loadAnatEntityTaxonConstraintBySpeciesIds(new HashSet<>(Arrays.asList("sp2"))))
            .thenReturn(tc_sp2.stream()).thenReturn(tc_sp2.stream());

        Set<TaxonConstraint> relTc_sp2 = new HashSet<>(Arrays.asList(
                // UBERON:0001 ------------------
                // | sp1/sp2   \ sp2            |
                // UBERON:0002   UBERON:0002p   | sp2
                // | sp1       / sp2            |
                // UBERON:0003 ------------------
                new TaxonConstraint("1", "sp2"),
                new TaxonConstraint("2", "sp2"),
                new TaxonConstraint("4", "sp2"),
                new TaxonConstraint("5", "sp2")));
        when(tcService.loadAnatEntityRelationTaxonConstraintBySpeciesIds(new HashSet<>(Arrays.asList("sp2"))))
            .thenReturn(relTc_sp2.stream());
        ancestors = ontology.getAncestors("sp2", ae3, ISA_RELATIONS, false);
        expAncestors = new HashSet<>(Arrays.asList(ae1, ae2p));
        assertEquals("Incorrects ancestors", expAncestors, ancestors);

        Set<TaxonConstraint> tc_sp3 = new HashSet<>(Arrays.asList(
                // UBERON:0001 sp1/sp2/sp3 --------------------
                // |                    \                      |
                // UBERON:0002 sp1/sp2   UBERON:0002p sp2/sp3  | 
                // |                    /                      |
                // UBERON:0003 sp1/sp2 ------------------------
                new TaxonConstraint("UBERON:0001", null),
                new TaxonConstraint("UBERON:0002p", "sp3")));
        when(tcService.loadAnatEntityTaxonConstraintBySpeciesIds(new HashSet<>(Arrays.asList("sp3"))))
            .thenReturn(tc_sp3.stream()).thenReturn(tc_sp3.stream());
        Set<TaxonConstraint> relTc_sp3 = new HashSet<>(Arrays.asList());
        when(tcService.loadAnatEntityRelationTaxonConstraintBySpeciesIds(new HashSet<>(Arrays.asList("sp3"))))
            .thenReturn(relTc_sp3.stream());

        ancestors = ontology.getAncestors("sp3", ae2p, ALL_RELATIONS, false);
        expAncestors = new HashSet<>();
        assertEquals("Incorrects ancestors", expAncestors, ancestors);
    }

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
                new RelationTO("5", "UBERON:0003", "UBERON:0001", RelationTO.RelationType.ISA_PARTOF, RelationStatus.INDIRECT)));
        return relations;
    }

    /**
     * Test the method {@link MultiSpeciesOntology#getDescendants(org.bgee.model.NamedEntity)}.
     */
    @Test
    public void shouldGetDescendants() {
        ServiceFactory mockFact = mock(ServiceFactory.class);
        TaxonConstraintService tcService = mock(TaxonConstraintService.class);
        when(mockFact.getTaxonConstraintService()).thenReturn(tcService);

        String speciesId = "sp2";
        // Get stage taxon constraints for tests.
        Set<TaxonConstraint> stageTCs = 
                // stage1 sp1/sp2 -------
                // |               \     \    
                // stage2 sp1/sp2   |     stage2p sp2
                // |               /      | 
                // stage3 sp1             stage3p sp2
                new HashSet<>(Arrays.asList(
                        new TaxonConstraint("stage1", null),
                        new TaxonConstraint("stage2", null),
                        new TaxonConstraint("stage2p", "sp2"),
                        new TaxonConstraint("stage3p", "sp2")));

        when(tcService.loadDevStageTaxonConstraintBySpeciesIds(new HashSet<>(Arrays.asList(speciesId))))
            .thenReturn(stageTCs.stream()).thenReturn(stageTCs.stream())
            .thenReturn(stageTCs.stream()).thenReturn(stageTCs.stream())
            .thenReturn(stageTCs.stream()).thenReturn(stageTCs.stream())
            .thenReturn(stageTCs.stream()).thenReturn(stageTCs.stream())
            .thenReturn(stageTCs.stream()).thenReturn(stageTCs.stream());

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

        MultiSpeciesOntology<DevStage> ontology = new MultiSpeciesOntology<>(
                elements, relations, ALL_RELATIONS, mockFact, DevStage.class);

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
        descendants = ontology.getDescendants(speciesId, ds1, ISA_RELATIONS, false);
        expDescendants = new HashSet<>(Arrays.asList(ds2, ds3p));
        assertEquals("Incorrects descendants", expDescendants, descendants);

        descendants = ontology.getDescendants(speciesId, ds1, ISA_RELATIONS, true);
        expDescendants = new HashSet<>(Arrays.asList(ds2));
        assertEquals("Incorrects descendants", expDescendants, descendants);

        descendants = ontology.getDescendants(speciesId, ds1, ALL_RELATIONS, false);
        expDescendants = new HashSet<>(Arrays.asList(ds2, ds2p, ds3p));
        assertEquals("Incorrects descendants", expDescendants, descendants);

        descendants = ontology.getDescendants(speciesId, ds1, ALL_RELATIONS, true);
        expDescendants = new HashSet<>(Arrays.asList(ds2, ds2p));
        assertEquals("Incorrects descendants", expDescendants, descendants);

        descendants = ontology.getDescendants(speciesId, ds2, ALL_RELATIONS, false);
        expDescendants = new HashSet<>();
        assertEquals("Incorrects descendants", expDescendants, descendants);

        descendants = ontology.getDescendants(ds3, ALL_RELATIONS);
        expDescendants = new HashSet<>();
        assertEquals("Incorrects descendants", expDescendants, descendants);
    }

}
