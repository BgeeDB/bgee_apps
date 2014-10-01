package org.bgee.model.dao.api;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.anatdev.AnatEntityDAO.AnatEntityTO;
import org.bgee.model.dao.api.anatdev.StageDAO.StageTO;
import org.bgee.model.dao.api.anatdev.TaxonConstraintDAO.TaxonConstraintTO;
import org.bgee.model.dao.api.expressiondata.CallDAO.CallTO.DataState;
import org.bgee.model.dao.api.expressiondata.ExpressionCallDAO.ExpressionCallTO;
import org.bgee.model.dao.api.expressiondata.ExpressionCallDAO.GlobalExpressionToExpressionTO;
import org.bgee.model.dao.api.expressiondata.NoExpressionCallDAO.GlobalNoExpressionToNoExpressionTO;
import org.bgee.model.dao.api.expressiondata.NoExpressionCallDAO.NoExpressionCallTO;
import org.bgee.model.dao.api.gene.GeneDAO.GeneTO;
import org.bgee.model.dao.api.gene.GeneOntologyDAO.GOTermTO;
import org.bgee.model.dao.api.gene.GeneOntologyDAO.GOTermTO.Domain;
import org.bgee.model.dao.api.gene.HierarchicalGroupDAO.HierarchicalGroupTO;
import org.bgee.model.dao.api.species.SpeciesDAO.SpeciesTO;
import org.bgee.model.dao.api.species.TaxonDAO.TaxonTO;
import org.junit.Test;

/**
 * Test the functionalities of {@link TOComparator}.
 *  
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public class TOComparatorTest extends TestAncestor {
    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = 
            LogManager.getLogger(TOComparatorTest.class.getName());

    @Override
    protected Logger getLogger() {
        return log;
    }
    
    /**
     * Test the generic method {@link TOComparator#areTOsEqual(Object, Object)} 
     * using {@code SpeciesTO}s.
     */
    @Test
    public void testAreSpeciesTOEqual() {
        SpeciesTO to1 = new SpeciesTO("ID:1", "name1", "genus1", "species1", 
                "parentTaxon1", "path1", "genSpeId1", "fakePrefix1");
        SpeciesTO to2 = new SpeciesTO("ID:1", "name1", "genus1", "species1", 
                "parentTaxon1", "path1", "genSpeId1", "fakePrefix1");
        assertTrue(TOComparator.areTOsEqual(to1, to2));

        to2 = new SpeciesTO("ID:2", "name1", "genus1", "species1", 
                "parentTaxon1", "path1", "genSpeId1", "fakePrefix2");
        assertFalse(TOComparator.areTOsEqual(to1, to2));

        to2 = new SpeciesTO("ID:1", "name1", "genus1", "species1", 
                "parentTaxon1", "path1", "genSpeId1", "fakePrefix2");
        assertFalse(TOComparator.areTOsEqual(to1, to2));
    }
    
    /**
     * Test the generic method {@link TOComparator#areTOsEqual(Object, Object)} 
     * using {@code TaxonTO}s.
     */
    @Test
    public void testAreTaxonTOEqual() {
        TaxonTO to1 = new TaxonTO("id1", "name1", "sciName1", 1, 3, 1, true);
        TaxonTO to2 = new TaxonTO("id1", "name1", "sciName1", 1, 3, 1, true);
        assertTrue(TOComparator.areTOsEqual(to1, to2));

        to2 = new TaxonTO("id1", "name1", "sciName1", 1, 3, 1, false);
        assertFalse(TOComparator.areTOsEqual(to1, to2));
    }
    
    /**
     * Test the generic method {@link TOComparator#areTOsEqual(Object, Object)} 
     * using {@code GOTermTO}s.
     */
    @Test
    public void testAreGOTermTOEqual() {
        GOTermTO to1 = new GOTermTO("id1", "name1", Domain.BP, Arrays.asList("ALT1", "ALT2"));
        GOTermTO to2 = new GOTermTO("id1", "name1", Domain.BP, Arrays.asList("ALT2", "ALT1"));
        assertTrue(TOComparator.areTOsEqual(to1, to2));

        to2 = new GOTermTO("id1", "name1", Domain.BP, Arrays.asList("ALT2"));
        assertFalse(TOComparator.areTOsEqual(to1, to2));
    }
    
    /**
     * Test the generic method {@link TOComparator#areTOsEqual(Object, Object)} 
     * using {@code GeneTO}s.
     */
    @Test
    public void testAreGeneTOEqual() {
        GeneTO to1 = new GeneTO("ID1", "name1", "desc1", 1, 2, 3, true);
        GeneTO to2 = new GeneTO("ID1", "name1", "desc1", 1, 2, 3, true);
        assertTrue(TOComparator.areTOsEqual(to1, to2));
        
        to2 = new GeneTO("ID1", "name1", "desc1", 1, 2, 3, false);
        assertFalse(TOComparator.areTOsEqual(to1, to2));
    }
    
    /**
     * Test the generic method {@link TOComparator#areTOsEqual(Object, Object)} 
     * using {@code HierarchicalGroupTO}s.
     */
    @Test
    public void testAreHierarchicalGroupTOEqual() {
        HierarchicalGroupTO to1 = new HierarchicalGroupTO(1, "ID1", 1, 2, 10);
        HierarchicalGroupTO to2 = new HierarchicalGroupTO(1, "ID1", 1, 2, 10);
        assertTrue(TOComparator.areTOsEqual(to1, to2));
        
        to2 = new HierarchicalGroupTO(1, "ID1", 1, 2, 5);
        assertFalse(TOComparator.areTOsEqual(to1, to2));
    }
    
    /**
     * Test the generic method {@link TOComparator#areTOsEqual(Object, Object)} 
     * using {@code AnatEntityTO}s.
     */
    @Test
    public void testAreAnatEntityTOEqual() {
        AnatEntityTO to1 = new AnatEntityTO("ID1", "name1", "desc1", 
                "stage:1", "stage:2", false);
        AnatEntityTO to2 = new AnatEntityTO("ID1", "name1", "desc1", 
                "stage:1", "stage:2", false);
        assertTrue(TOComparator.areTOsEqual(to1, to2));
        
        to2 = new AnatEntityTO("ID1", "name1", "desc1", 
                "stage:1", "stage:2", true);
        assertFalse(TOComparator.areTOsEqual(to1, to2));
    }
    
    /**
     * Test the generic method {@link TOComparator#areTOsEqual(Object, Object)} 
     * using {@code StageTO}s.
     */
    @Test
    public void testAreStageTOEqual() {
        StageTO to1 = new StageTO("ID1", "name1", "desc1", 1, 2, 3, false, false);
        StageTO to2 = new StageTO("ID1", "name1", "desc1", 1, 2, 3, false, false);
        assertTrue(TOComparator.areTOsEqual(to1, to2));
        
        to2 = new StageTO("ID1", "name1", "desc1", 1, 2, 3, false, true);
        assertFalse(TOComparator.areTOsEqual(to1, to2));
    }
    
    /**
     * Test the generic method {@link TOComparator#areTOsEqual(Object, Object)} 
     * using {@code TaxonConstraintTO}s.
     */
    @Test
    public void testAreTaxonConstraintTOEqual() {
        TaxonConstraintTO to1 = new TaxonConstraintTO("ID1", "ID2");
        TaxonConstraintTO to2 = new TaxonConstraintTO("ID1", "ID2");
        assertTrue(TOComparator.areTOsEqual(to1, to2));
        
        to2 = new TaxonConstraintTO("ID1", "ID3");
        assertFalse(TOComparator.areTOsEqual(to1, to2));
    }
    
    /**
     * Test the generic method {@link TOComparator#areTOsEqual(Object, Object)} 
     * using {@code ExpressionCallTO}s.
     */
    @Test
    public void testAreExpressionCallTOEqual() {
        ExpressionCallTO to1 = new ExpressionCallTO("1", "ID1", "Anat_id1", "Stage_id6", 
                DataState.HIGHQUALITY, DataState.LOWQUALITY, DataState.HIGHQUALITY, 
                DataState.LOWQUALITY, false, false, ExpressionCallTO.OriginOfLine.SELF);
        ExpressionCallTO to2 = new ExpressionCallTO("1", "ID1", "Anat_id1", "Stage_id6", 
                DataState.HIGHQUALITY, DataState.LOWQUALITY, DataState.HIGHQUALITY, 
                DataState.LOWQUALITY, false, false, ExpressionCallTO.OriginOfLine.SELF);
        assertTrue(TOComparator.areTOsEqual(to1, to2));
        
        to2 = new ExpressionCallTO("1", "ID1", "Anat_id1", "Stage_id6", 
                DataState.HIGHQUALITY, DataState.LOWQUALITY, DataState.HIGHQUALITY, 
                DataState.LOWQUALITY, false, false, ExpressionCallTO.OriginOfLine.DESCENT);
        assertFalse(TOComparator.areTOsEqual(to1, to2));
    }

    /**
     * Test the generic method {@link TOComparator#areTOsEqual(Object, Object)} 
     * using {@code NoExpressionCallTO}s.
     */
    @Test
    public void testAreNoExpressionCallTOEqual() {
        NoExpressionCallTO to1 = new NoExpressionCallTO("1", "ID1", "Anat_id1", "Stage_id6", 
                DataState.HIGHQUALITY, DataState.LOWQUALITY, DataState.HIGHQUALITY, 
                DataState.LOWQUALITY, false, NoExpressionCallTO.OriginOfLine.SELF);
        NoExpressionCallTO to2 = new NoExpressionCallTO("1", "ID1", "Anat_id1", "Stage_id6", 
                DataState.HIGHQUALITY, DataState.LOWQUALITY, DataState.HIGHQUALITY, 
                DataState.LOWQUALITY, false, NoExpressionCallTO.OriginOfLine.SELF);
        assertTrue(TOComparator.areTOsEqual(to1, to2));
        
        to2 = new NoExpressionCallTO("1", "ID1", "Anat_id1", "Stage_id6", 
                DataState.HIGHQUALITY, DataState.LOWQUALITY, DataState.HIGHQUALITY, 
                DataState.LOWQUALITY, false, NoExpressionCallTO.OriginOfLine.PARENT);
        assertFalse(TOComparator.areTOsEqual(to1, to2));
    }

    /**
     * Test the generic method {@link TOComparator#areTOsEqual(Object, Object)} 
     * using {@code GlobalExpressionToExpressionTO}s.
     */
    @Test
    public void testAreGlobalExpressionToExpressionTOEqual() {
        GlobalExpressionToExpressionTO to1 = new GlobalExpressionToExpressionTO("1", "10");
        GlobalExpressionToExpressionTO to2 = new GlobalExpressionToExpressionTO("1", "10");
        assertTrue(TOComparator.areTOsEqual(to1, to2));
        
        to2 = new GlobalExpressionToExpressionTO("1", "20");
        assertFalse(TOComparator.areTOsEqual(to1, to2));
    }
    
    /**
     * Test the generic method {@link TOComparator#areTOsEqual(Object, Object)} 
     * using {@code GlobalNoExpressionToNoExpressionTO}s.
     */
    @Test
    public void testAreGlobalNoExpressionToNoExpressionTOEqual() {
        GlobalNoExpressionToNoExpressionTO to1 = new GlobalNoExpressionToNoExpressionTO("1", "10");
        GlobalNoExpressionToNoExpressionTO to2 = new GlobalNoExpressionToNoExpressionTO("1", "10");
        assertTrue(TOComparator.areTOsEqual(to1, to2));
        
        to2 = new GlobalNoExpressionToNoExpressionTO("1", "20");
        assertFalse(TOComparator.areTOsEqual(to1, to2));
    }

    /**
     * Test the generic method {@link TOComparator#areTOCollectionsEqual(Collection, Collection)}.
     */
    @Test
    public void testAreTOCollectionsEqual() {
        Collection<TaxonConstraintTO> c1 = Arrays.asList(new TaxonConstraintTO("ID1", "ID2"), 
        new TaxonConstraintTO("ID3", "ID4"));
        Collection<TaxonConstraintTO> c2 = Arrays.asList(new TaxonConstraintTO("ID1", "ID2"), 
                new TaxonConstraintTO("ID3", "ID4"));
        assertTrue(TOComparator.areTOCollectionsEqual(c1, c2));
        
        c2 = Arrays.asList(new TaxonConstraintTO("ID1", "ID2"), 
                new TaxonConstraintTO("ID3", "ID5"));
        assertFalse(TOComparator.areTOCollectionsEqual(c1, c2));
    }
}
