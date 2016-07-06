package org.bgee.model.ontology;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import org.bgee.model.TestAncestor;
import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.dao.api.ontologycommon.RelationDAO.RelationTO;
import org.bgee.model.dao.api.ontologycommon.RelationDAO.RelationTO.RelationStatus;
import org.junit.Test;

/**
 * This class holds the unit tests for the {@code Ontology} class.
 * 
 * @author  Valentine Rech de Laval
 * @version Bgee 13, Dec. 2015
 * @since   Bgee 13, Dec. 2015
 */
public class OntologyTest extends TestAncestor {
  
    /**
     * Test the method {@link Ontology#getAncestors(Entity)}.
     */
    @Test
    public void shouldGetAncestors() {
        AnatEntity ae1 = new AnatEntity("UBERON:0001", "A", "A description"); 
        AnatEntity ae2 = new AnatEntity("UBERON:0002", "B", "B description"); 
        AnatEntity ae2p = new AnatEntity("UBERON:0002p", "Bprime", "Bprime description"); 
        AnatEntity ae3 = new AnatEntity("UBERON:0003", "C", "C description"); 
        Set<AnatEntity> elements = new HashSet<>(Arrays.asList(ae1, ae2, ae2p, ae3));
        Set<RelationTO> relations = this.getRelations();

        Ontology<AnatEntity> ontology = new Ontology<>(elements, relations, 
                EnumSet.allOf(Ontology.RelationType.class));
        
        Set<AnatEntity> ancestors = ontology.getAncestors(ae3, EnumSet.allOf(Ontology.RelationType.class));
        Set<AnatEntity> expAncestors = new HashSet<>(Arrays.asList(ae1, ae2, ae2p));
        assertEquals("Incorrects ancestors", expAncestors, ancestors);
        
        ancestors = ontology.getAncestors(ae2p, EnumSet.allOf(Ontology.RelationType.class));
        expAncestors = new HashSet<>(Arrays.asList(ae1));
        assertEquals("Incorrects ancestors", expAncestors, ancestors);

        ancestors = ontology.getAncestors(ae1, EnumSet.allOf(Ontology.RelationType.class));
        expAncestors = new HashSet<>();
        assertEquals("Incorrects ancestors", expAncestors, ancestors);
    }
    
    /**
     * Test the method {@link Ontology#getDescendants(Entity)}.
     */
    @Test
    public void shouldGetDescendants() {
        AnatEntity ae1 = new AnatEntity("UBERON:0001", "A", "A description"); 
        AnatEntity ae2 = new AnatEntity("UBERON:0002", "B", "B description"); 
        AnatEntity ae2p = new AnatEntity("UBERON:0002p", "Bprime", "Bprime description"); 
        AnatEntity ae3 = new AnatEntity("UBERON:0003", "C", "C description"); 
        Set<AnatEntity> elements = new HashSet<>(Arrays.asList(ae1, ae2, ae2p, ae3));
        Set<RelationTO> relations = this.getRelations();

        Ontology<AnatEntity> ontology = new Ontology<>(elements, relations, 
                EnumSet.allOf(Ontology.RelationType.class));
        
        Set<AnatEntity> descendants = ontology.getDescendants(ae1, EnumSet.of(Ontology.RelationType.ISA_PARTOF));
        Set<AnatEntity> expDescendants = new HashSet<>(Arrays.asList(ae2, ae3));
        assertEquals("Incorrects descendants", expDescendants, descendants);

        descendants = ontology.getDescendants(ae2, EnumSet.allOf(Ontology.RelationType.class));
        expDescendants = new HashSet<>(Arrays.asList(ae3));
        assertEquals("Incorrects descendants", expDescendants, descendants);

        descendants = ontology.getDescendants(ae3, EnumSet.allOf(Ontology.RelationType.class));
        expDescendants = new HashSet<>();
        assertEquals("Incorrects descendants", expDescendants, descendants);
    }

    /**
     * Test the method {@link Ontology#getElement(String)}.
     */
    @Test
    public void shouldGetElement() {
        AnatEntity ae1 = new AnatEntity("UBERON:0001", "A", "A description"); 
        AnatEntity ae2 = new AnatEntity("UBERON:0002", "B", "B description"); 

        Set<AnatEntity> elements = new HashSet<>(Arrays.asList(ae1, ae2));
        Set<RelationTO> relations = this.getRelations();

        Ontology<AnatEntity> ontology = new Ontology<>(elements, relations, 
                EnumSet.allOf(Ontology.RelationType.class));
        
        assertEquals("Incorrect element", ae1, ontology.getElement("UBERON:0001"));
        assertEquals("Incorrect element", ae2, ontology.getElement("UBERON:0002"));
        assertEquals("Incorrect element", null, ontology.getElement("UBERON:XXXX"));
    }

    /**
     * Get relations for tests.
     * 
     * @return  The {@code Set} of {@code RelationTO}s that are the relations to be used for tests.
     */
    private Set<RelationTO> getRelations() {        
        return new HashSet<>(Arrays.asList(
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
    }
}
