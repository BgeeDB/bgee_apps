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
import org.bgee.model.dao.api.expressiondata.DiffExpressionCallDAO.DiffExpressionCallTO;
import org.bgee.model.dao.api.expressiondata.DiffExpressionCallDAO.DiffExpressionCallTO.ComparisonFactor;
import org.bgee.model.dao.api.expressiondata.DiffExpressionCallDAO.DiffExpressionCallTO.DiffExprCallType;
import org.bgee.model.dao.api.expressiondata.ExpressionCallDAO.ExpressionCallTO;
import org.bgee.model.dao.api.expressiondata.ExpressionCallDAO.GlobalExpressionToExpressionTO;
import org.bgee.model.dao.api.expressiondata.NoExpressionCallDAO.GlobalNoExpressionToNoExpressionTO;
import org.bgee.model.dao.api.expressiondata.NoExpressionCallDAO.NoExpressionCallTO;
import org.bgee.model.dao.api.file.DownloadFileDAO.DownloadFileTO;
import org.bgee.model.dao.api.file.DownloadFileDAO.DownloadFileTO.CategoryEnum;
import org.bgee.model.dao.api.file.SpeciesDataGroupDAO.SpeciesToDataGroupTO;
import org.bgee.model.dao.api.file.SpeciesDataGroupDAO.SpeciesDataGroupTO;
import org.bgee.model.dao.api.gene.GeneDAO.GeneTO;
import org.bgee.model.dao.api.gene.GeneOntologyDAO.GOTermTO;
import org.bgee.model.dao.api.gene.GeneOntologyDAO.GOTermTO.Domain;
import org.bgee.model.dao.api.gene.HierarchicalGroupDAO.HierarchicalGroupTO;
import org.bgee.model.dao.api.keyword.KeywordDAO.EntityToKeywordTO;
import org.bgee.model.dao.api.keyword.KeywordDAO.KeywordTO;
import org.bgee.model.dao.api.ontologycommon.RelationDAO.RelationTO;
import org.bgee.model.dao.api.ontologycommon.RelationDAO.RelationTO.RelationStatus;
import org.bgee.model.dao.api.ontologycommon.RelationDAO.RelationTO.RelationType;
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
     * Test the generic method {@link TOComparator#areTOsEqual(Object, Object, boolean)} 
     * using {@code SpeciesTO}s.
     */
    @Test
    public void testAreSpeciesTOEqual() {
        SpeciesTO to1 = new SpeciesTO("ID:1", "name1", "genus1", "species1", 
                "parentTaxon1", "path1", "genSpeId1", "fakePrefix1");
        SpeciesTO to2 = new SpeciesTO("ID:1", "name1", "genus1", "species1", 
                "parentTaxon1", "path1", "genSpeId1", "fakePrefix1");
        assertTrue(TOComparator.areTOsEqual(to1, to2, true));
        assertTrue(TOComparator.areTOsEqual(to1, to2, false));

        to2 = new SpeciesTO("ID:2", "name1", "genus1", "species1", 
                "parentTaxon1", "path1", "genSpeId1", "fakePrefix2");
        assertFalse(TOComparator.areTOsEqual(to1, to2, true));

        to2 = new SpeciesTO("ID:1", "name1", "genus1", "species1", 
                "parentTaxon1", "path1", "genSpeId1", "fakePrefix2");
        assertFalse(TOComparator.areTOsEqual(to1, to2, true));
        
        to2 = new SpeciesTO("ID:2", "name1", "genus1", "species1", 
                "parentTaxon1", "path1", "genSpeId1", "fakePrefix1");
        assertFalse(TOComparator.areTOsEqual(to1, to2, true));
        assertTrue(TOComparator.areTOsEqual(to1, to2, false));
    }
    
    /**
     * Test the generic method {@link TOComparator#areTOsEqual(Object, Object, boolean)} 
     * using {@code TaxonTO}s.
     */
    @Test
    public void testAreTaxonTOEqual() {
        TaxonTO to1 = new TaxonTO("id1", "name1", "sciName1", 1, 3, 1, true);
        TaxonTO to2 = new TaxonTO("id1", "name1", "sciName1", 1, 3, 1, true);
        assertTrue(TOComparator.areTOsEqual(to1, to2, true));
        assertTrue(TOComparator.areTOsEqual(to1, to2, false));

        to2 = new TaxonTO("id1", "name1", "sciName1", 1, 3, 1, false);
        assertFalse(TOComparator.areTOsEqual(to1, to2, true));
        
        to2 = new TaxonTO("id2", "name1", "sciName1", 1, 3, 1, true);
        assertFalse(TOComparator.areTOsEqual(to1, to2, true));
        assertTrue(TOComparator.areTOsEqual(to1, to2, false));
    }
    
    /**
     * Test the generic method {@link TOComparator#areTOsEqual(Object, Object, boolean)} 
     * using {@code GOTermTO}s.
     */
    @Test
    public void testAreGOTermTOEqual() {
        GOTermTO to1 = new GOTermTO("id1", "name1", Domain.BP, Arrays.asList("ALT1", "ALT2"));
        GOTermTO to2 = new GOTermTO("id1", "name1", Domain.BP, Arrays.asList("ALT2", "ALT1"));
        assertTrue(TOComparator.areTOsEqual(to1, to2, true));
        assertTrue(TOComparator.areTOsEqual(to1, to2, false));

        to2 = new GOTermTO("id1", "name1", Domain.BP, Arrays.asList("ALT2"));
        assertFalse(TOComparator.areTOsEqual(to1, to2, true));
        
        to2 = new GOTermTO("id2", "name1", Domain.BP, Arrays.asList("ALT1", "ALT2"));
        assertFalse(TOComparator.areTOsEqual(to1, to2, true));
        assertTrue(TOComparator.areTOsEqual(to1, to2, false));
    }
    
    /**
     * Test the generic method {@link TOComparator#areTOsEqual(Object, Object, boolean)} 
     * using {@code GeneTO}s.
     */
    @Test
    public void testAreGeneTOEqual() {
        GeneTO to1 = new GeneTO("ID1", "name1", "desc1", 1, 2, 3, true);
        GeneTO to2 = new GeneTO("ID1", "name1", "desc1", 1, 2, 3, true);
        assertTrue(TOComparator.areTOsEqual(to1, to2, true));
        assertTrue(TOComparator.areTOsEqual(to1, to2, false));
        
        to2 = new GeneTO("ID1", "name1", "desc1", 1, 2, 3, false);
        assertFalse(TOComparator.areTOsEqual(to1, to2, true));
        
        to2 = new GeneTO("ID2", "name1", "desc1", 1, 2, 3, true);
        assertFalse(TOComparator.areTOsEqual(to1, to2, true));
        assertTrue(TOComparator.areTOsEqual(to1, to2, false));
    }
    
    /**
     * Test the generic method {@link TOComparator#areTOsEqual(Object, Object, boolean)} 
     * using {@code HierarchicalGroupTO}s.
     */
    @Test
    public void testAreHierarchicalGroupTOEqual() {
        HierarchicalGroupTO to1 = new HierarchicalGroupTO("1", "ID1", 1, 2, 10);
        HierarchicalGroupTO to2 = new HierarchicalGroupTO("1", "ID1", 1, 2, 10);
        assertTrue(TOComparator.areTOsEqual(to1, to2, true));
        assertTrue(TOComparator.areTOsEqual(to1, to2, false));
        
        to2 = new HierarchicalGroupTO("1", "ID1", 1, 2, 5);
        assertFalse(TOComparator.areTOsEqual(to1, to2));
        
        to2 = new HierarchicalGroupTO("2", "ID1", 1, 2, 10);
        assertFalse(TOComparator.areTOsEqual(to1, to2, true));
        assertTrue(TOComparator.areTOsEqual(to1, to2, false));
    }
    
    /**
     * Test the generic method {@link TOComparator#areTOsEqual(Object, Object, boolean)} 
     * using {@code AnatEntityTO}s.
     */
    @Test
    public void testAreAnatEntityTOEqual() {
        AnatEntityTO to1 = new AnatEntityTO("ID1", "name1", "desc1", 
                "stage:1", "stage:2", false);
        AnatEntityTO to2 = new AnatEntityTO("ID1", "name1", "desc1", 
                "stage:1", "stage:2", false);
        assertTrue(TOComparator.areTOsEqual(to1, to2, true));
        assertTrue(TOComparator.areTOsEqual(to1, to2, false));
        
        to2 = new AnatEntityTO("ID1", "name1", "desc1", 
                "stage:1", "stage:2", true);
        assertFalse(TOComparator.areTOsEqual(to1, to2, true));
        
        to2 = new AnatEntityTO("ID2", "name1", "desc1", 
                "stage:1", "stage:2", false);
        assertFalse(TOComparator.areTOsEqual(to1, to2, true));
        assertTrue(TOComparator.areTOsEqual(to1, to2, false));
    }
    
    /**
     * Test the generic method {@link TOComparator#areTOsEqual(Object, Object, boolean)} 
     * using {@code DownloadFileTO}s.
     */
    @Test
    public void testAreDownloadFileTOsEqual() {
        DownloadFileTO to1 = new DownloadFileTO("ID1", "name1", "desc1", 
                "path/", 10L, CategoryEnum.EXPR_CALLS_COMPLETE, "");
        DownloadFileTO to2 = new DownloadFileTO("ID1", "name1", "desc1", 
                "path/", 10L, CategoryEnum.EXPR_CALLS_COMPLETE, "");
        assertTrue(TOComparator.areTOsEqual(to1, to2, true));
        
        assertTrue(TOComparator.areTOsEqual(to1, to2, false));
        
        to2 = new DownloadFileTO("ID1", "name1", "desc1", 
                "path/", 10L, CategoryEnum.DIFF_EXPR_ANAT_COMPLETE, "");
        assertFalse(TOComparator.areTOsEqual(to1, to2, true));
        
        to2 = new DownloadFileTO("ID2", "name1", "desc1", 
                "path/", 10L, CategoryEnum.EXPR_CALLS_COMPLETE, "");
        assertFalse(TOComparator.areTOsEqual(to1, to2, true));
        assertTrue(TOComparator.areTOsEqual(to1, to2, false));
        
        //regression test when using Long size value > 127, 
        //see http://stackoverflow.com/a/20542511/1768736
        to1 = new DownloadFileTO("ID1", "name1", "desc1", 
                "path/", 3000L, CategoryEnum.EXPR_CALLS_COMPLETE, "");
        to2 = new DownloadFileTO("ID1", "name1", "desc1", 
                "path/", 3000L, CategoryEnum.EXPR_CALLS_COMPLETE, "");
        assertTrue(TOComparator.areTOsEqual(to1, to2, true));
        to1 = new DownloadFileTO("ID1", "name1", "desc1", 
                "path/", null, CategoryEnum.EXPR_CALLS_COMPLETE, "");
        to2 = new DownloadFileTO("ID1", "name1", "desc1", 
                "path/", null, CategoryEnum.EXPR_CALLS_COMPLETE, "");
        assertTrue(TOComparator.areTOsEqual(to1, to2, true));
    }

    /**
     * Test the generic method {@link TOComparator#areTOsEqual(Object, Object)} 
     * using {@code SpeciesDataGroupTO}s.
     */
    @Test
    public void testAreSpeciesDataGroupTOsEqual() {
        SpeciesDataGroupTO to1 = new SpeciesDataGroupTO("ID1", "name1", "desc1", 1);

        SpeciesDataGroupTO to2 = new SpeciesDataGroupTO("ID1", "name1", "desc1", 1);
        assertTrue(TOComparator.areTOsEqual(to1, to2, true));
        
        assertTrue(TOComparator.areTOsEqual(to1, to2, false));
        
        to2 = new SpeciesDataGroupTO("ID1", "name1", "desc2", 1);
        assertFalse(TOComparator.areTOsEqual(to1, to2, true));
        
        to2 = new SpeciesDataGroupTO("ID2", "name1", "desc1", 1);
        assertFalse(TOComparator.areTOsEqual(to1, to2, true));
        assertTrue(TOComparator.areTOsEqual(to1, to2, false));
        
        to1 = new SpeciesDataGroupTO("ID1", "name1", "desc1", null);
        to2 = new SpeciesDataGroupTO("ID1", "name1", "desc1", 1);
        assertFalse(TOComparator.areTOsEqual(to1, to2, true));
        assertFalse(TOComparator.areTOsEqual(to1, to2, false));
    }

    /**
     * Test the generic method {@link TOComparator#areTOsEqual(Object, Object)} 
     * using {@code SpeciesToDataGroupTO}s.
     */
    @Test
    public void testAreSpeciesToDataGroupTOsEqual() {
        SpeciesToDataGroupTO to1 = new SpeciesToDataGroupTO("1", "11");

        SpeciesToDataGroupTO to2 = new SpeciesToDataGroupTO("1", "11");
        assertTrue(TOComparator.areTOsEqual(to1, to2));
        
        to2 = new SpeciesToDataGroupTO("1", "12");
        assertFalse(TOComparator.areTOsEqual(to1, to2));
        
        to2 = new SpeciesToDataGroupTO("2", "11");
        assertFalse(TOComparator.areTOsEqual(to1, to2));
    }

    /**
     * Test the generic method {@link TOComparator#areTOsEqual(Object, Object, boolean)} 
     * using {@code StageTO}s.
     */
    @Test
    public void testAreStageTOEqual() {
        StageTO to1 = new StageTO("ID1", "name1", "desc1", 1, 2, 3, false, false);
        StageTO to2 = new StageTO("ID1", "name1", "desc1", 1, 2, 3, false, false);
        assertTrue(TOComparator.areTOsEqual(to1, to2, true));
        assertTrue(TOComparator.areTOsEqual(to1, to2, false));
        
        to2 = new StageTO("ID1", "name1", "desc1", 1, 2, 3, false, true);
        assertFalse(TOComparator.areTOsEqual(to1, to2, true));
        
        to2 = new StageTO("ID2", "name1", "desc1", 1, 2, 3, false, false);
        assertFalse(TOComparator.areTOsEqual(to1, to2, true));
        assertTrue(TOComparator.areTOsEqual(to1, to2, false));
    }
    
    /**
     * Test the generic method {@link TOComparator#areTOsEqual(Object, Object, boolean)} 
     * using {@code TaxonConstraintTO}s.
     */
    @Test
    public void testAreTaxonConstraintTOsEqual() {
        TaxonConstraintTO to1 = new TaxonConstraintTO("ID1", "ID2");
        TaxonConstraintTO to2 = new TaxonConstraintTO("ID1", "ID2");
        assertTrue(TOComparator.areTOsEqual(to1, to2, true));
        assertTrue(TOComparator.areTOsEqual(to1, to2, false));
        
        to2 = new TaxonConstraintTO("ID1", "ID3");
        assertFalse(TOComparator.areTOsEqual(to1, to2, true));
    }
    
    /**
     * Test the generic method {@link TOComparator#areTOsEqual(Object, Object, boolean)} 
     * using {@code RelationTO}s.
     */
    @Test
    public void testAreRelationTOsEqual() {
        RelationTO to1 = new RelationTO("1", "ID1", "ID2", RelationType.ISA_PARTOF, 
                RelationStatus.DIRECT);
        RelationTO to2 = new RelationTO("1", "ID1", "ID2", RelationType.ISA_PARTOF, 
                RelationStatus.DIRECT);
        assertTrue(TOComparator.areTOsEqual(to1, to2, true));
        assertTrue(TOComparator.areTOsEqual(to1, to2, false));
        
        to2 = new RelationTO("1", "ID1", "ID2", RelationType.DEVELOPSFROM, 
                RelationStatus.DIRECT);
        assertFalse(TOComparator.areTOsEqual(to1, to2, true));
        
        to2 = new RelationTO("1", "ID1", "ID2", RelationType.ISA_PARTOF, 
                RelationStatus.INDIRECT);
        assertFalse(TOComparator.areTOsEqual(to1, to2, true));
        
        to2 = new RelationTO("2", "ID1", "ID2", RelationType.ISA_PARTOF, 
                RelationStatus.DIRECT);
        assertFalse(TOComparator.areTOsEqual(to1, to2, true));
        assertTrue(TOComparator.areTOsEqual(to1, to2, false));
    }
    
    /**
     * Test the generic method {@link TOComparator#areTOsEqual(Object, Object, boolean)} 
     * using {@code ExpressionCallTO}s.
     */
    @Test
    public void testAreExpressionCallTOEqual() {
        ExpressionCallTO to1 = new ExpressionCallTO("1", "ID1", "Anat_id1", "Stage_id6", 
                DataState.HIGHQUALITY, DataState.LOWQUALITY, DataState.HIGHQUALITY, 
                DataState.LOWQUALITY, false, false, 
                ExpressionCallTO.OriginOfLine.SELF, ExpressionCallTO.OriginOfLine.SELF, true);
        ExpressionCallTO to2 = new ExpressionCallTO("1", "ID1", "Anat_id1", "Stage_id6", 
                DataState.HIGHQUALITY, DataState.LOWQUALITY, DataState.HIGHQUALITY, 
                DataState.LOWQUALITY, false, false, 
                ExpressionCallTO.OriginOfLine.SELF, ExpressionCallTO.OriginOfLine.SELF, true);
        assertTrue(TOComparator.areTOsEqual(to1, to2, true));
        assertTrue(TOComparator.areTOsEqual(to1, to2, false));
        
        to2 = new ExpressionCallTO("1", "ID1", "Anat_id1", "Stage_id6", 
                DataState.HIGHQUALITY, DataState.LOWQUALITY, DataState.HIGHQUALITY, 
                DataState.LOWQUALITY, false, false, 
                ExpressionCallTO.OriginOfLine.DESCENT, ExpressionCallTO.OriginOfLine.SELF, true);
        assertFalse(TOComparator.areTOsEqual(to1, to2, true));
        
        to2 = new ExpressionCallTO("1", "ID1", "Anat_id1", "Stage_id6", 
                DataState.HIGHQUALITY, DataState.LOWQUALITY, DataState.HIGHQUALITY, 
                DataState.LOWQUALITY, false, false, 
                ExpressionCallTO.OriginOfLine.SELF, ExpressionCallTO.OriginOfLine.BOTH, true);
        assertFalse(TOComparator.areTOsEqual(to1, to2, true));
        
        to2 = new ExpressionCallTO("2", "ID1", "Anat_id1", "Stage_id6", 
                DataState.HIGHQUALITY, DataState.LOWQUALITY, DataState.HIGHQUALITY, 
                DataState.LOWQUALITY, false, false, 
                ExpressionCallTO.OriginOfLine.SELF, ExpressionCallTO.OriginOfLine.SELF, true);
        assertFalse(TOComparator.areTOsEqual(to1, to2, true));
        assertTrue(TOComparator.areTOsEqual(to1, to2, false));
        
        to2 = new ExpressionCallTO("1", "ID1", "Anat_id1", "Stage_id6", 
                DataState.HIGHQUALITY, DataState.LOWQUALITY, DataState.HIGHQUALITY, 
                DataState.LOWQUALITY, false, false, 
                ExpressionCallTO.OriginOfLine.SELF, ExpressionCallTO.OriginOfLine.SELF, false);
        assertFalse(TOComparator.areTOsEqual(to1, to2, true));
    }

    /**
     * Test the generic method {@link TOComparator#areTOsEqual(Object, Object, boolean)} 
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
        assertTrue(TOComparator.areTOsEqual(to1, to2, true));
        assertTrue(TOComparator.areTOsEqual(to1, to2, false));
        
        to2 = new NoExpressionCallTO("1", "ID1", "Anat_id1", "Stage_id6", 
                DataState.HIGHQUALITY, DataState.LOWQUALITY, DataState.HIGHQUALITY, 
                DataState.LOWQUALITY, false, NoExpressionCallTO.OriginOfLine.PARENT);
        assertFalse(TOComparator.areTOsEqual(to1, to2));
        
        to2 = new NoExpressionCallTO("2", "ID1", "Anat_id1", "Stage_id6", 
                DataState.HIGHQUALITY, DataState.LOWQUALITY, DataState.HIGHQUALITY, 
                DataState.LOWQUALITY, false, NoExpressionCallTO.OriginOfLine.SELF);
        assertFalse(TOComparator.areTOsEqual(to1, to2, true));
        assertTrue(TOComparator.areTOsEqual(to1, to2, false));
    }

    /**
     * Test the generic method {@link TOComparator#areTOsEqual(Object, Object, boolean)} 
     * using {@code GlobalExpressionToExpressionTO}s.
     */
    @Test
    public void testAreGlobalExpressionToExpressionTOEqual() {
        GlobalExpressionToExpressionTO to1 = new GlobalExpressionToExpressionTO("1", "10");
        GlobalExpressionToExpressionTO to2 = new GlobalExpressionToExpressionTO("1", "10");
        assertTrue(TOComparator.areTOsEqual(to1, to2, true));
        assertTrue(TOComparator.areTOsEqual(to1, to2, false));
        
        to2 = new GlobalExpressionToExpressionTO("1", "20");
        assertFalse(TOComparator.areTOsEqual(to1, to2, true));
    }
    
    /**
     * Test the generic method {@link TOComparator#areTOsEqual(Object, Object, boolean)} 
     * using {@code GlobalNoExpressionToNoExpressionTO}s.
     */
    @Test
    public void testAreGlobalNoExpressionToNoExpressionTOEqual() {
        GlobalNoExpressionToNoExpressionTO to1 = new GlobalNoExpressionToNoExpressionTO("1", "10");
        GlobalNoExpressionToNoExpressionTO to2 = new GlobalNoExpressionToNoExpressionTO("1", "10");
        assertTrue(TOComparator.areTOsEqual(to1, to2));
        assertTrue(TOComparator.areTOsEqual(to1, to2, true));
        assertTrue(TOComparator.areTOsEqual(to1, to2, false));
        
        to2 = new GlobalNoExpressionToNoExpressionTO("1", "20");
        assertFalse(TOComparator.areTOsEqual(to1, to2, true));
    }

    /**
     * Test the generic method {@link TOComparator#areTOCollectionsEqual(Collection, Collection, boolean)}.
     */
    @Test
    public void testAreTOCollectionsEqual() {
        Collection<AnatEntityTO> c1 = Arrays.asList(
                new AnatEntityTO("ID1", "name1", "desc1", "stage:1", "stage:2", false), 
                new AnatEntityTO("ID2", "name2", "desc2", "stage:2", "stage:3", true), 
                new AnatEntityTO("ID3", "name3", "desc3", "stage:3", "stage:4", false));
        Collection<AnatEntityTO> c2 = Arrays.asList(
                new AnatEntityTO("ID1", "name1", "desc1", "stage:1", "stage:2", false), 
                new AnatEntityTO("ID2", "name2", "desc2", "stage:2", "stage:3", true), 
                new AnatEntityTO("ID3", "name3", "desc3", "stage:3", "stage:4", false));
        assertTrue(TOComparator.areTOCollectionsEqual(c1, c2, true));
        assertTrue(TOComparator.areTOCollectionsEqual(c1, c2, false));
        
        c2 = Arrays.asList(
                new AnatEntityTO("ID4", "name1", "desc1", "stage:1", "stage:2", false), 
                new AnatEntityTO("ID2", "name2", "desc2", "stage:2", "stage:3", true), 
                new AnatEntityTO("ID3", "name3", "desc3", "stage:3", "stage:4", false));
        assertFalse(TOComparator.areTOCollectionsEqual(c1, c2, true));
        assertTrue(TOComparator.areTOCollectionsEqual(c1, c2, false));
    }

    /**
     * Test the generic method {@link TOComparator#areTOCollectionsEqual(Collection, Collection, boolean)} 
     * with tricky collections, like {Element1, Element1, Element2} versus 
     * {Element1, Element2, Element2}.
     */
    @Test
    public void regressionTestAreTOCollectionsEqual() {
        Collection<AnatEntityTO> c1 = Arrays.asList(
                new AnatEntityTO("ID1", "name1", "desc1", "stage:1", "stage:2", false), 
                new AnatEntityTO("ID1", "name1", "desc1", "stage:1", "stage:2", false), 
                new AnatEntityTO("ID3", "name3", "desc3", "stage:3", "stage:4", false));
        Collection<AnatEntityTO> c2 = Arrays.asList(
                new AnatEntityTO("ID1", "name1", "desc1", "stage:1", "stage:2", false), 
                new AnatEntityTO("ID3", "name3", "desc3", "stage:3", "stage:4", false), 
                new AnatEntityTO("ID1", "name1", "desc1", "stage:1", "stage:2", false));
        assertTrue(TOComparator.areTOCollectionsEqual(c1, c2, true));
        assertTrue(TOComparator.areTOCollectionsEqual(c1, c2, false));
        
        c2 = Arrays.asList(
                new AnatEntityTO("ID1", "name1", "desc1", "stage:1", "stage:2", false), 
                new AnatEntityTO("ID3", "name3", "desc3", "stage:3", "stage:4", false), 
                new AnatEntityTO("ID3", "name3", "desc3", "stage:3", "stage:4", false));
        assertFalse(TOComparator.areTOCollectionsEqual(c1, c2, true));
        assertFalse(TOComparator.areTOCollectionsEqual(c1, c2, false));
    }

    /**
     * Test the generic method {@link TOComparator#areTOsEqual(Object, Object, boolean)} 
     * using {@code DiffExpressionCallTO}s.
     */
    @Test
    public void testAreDiffExpressionCallTOEqual() {
        DiffExpressionCallTO to1 = new DiffExpressionCallTO("321", "ID1", "Anat_id1", "Stage_id1", 
                ComparisonFactor.ANATOMY, DiffExprCallType.NOT_DIFF_EXPRESSED, 
                DataState.HIGHQUALITY, 0.02f, 2, 0, DiffExprCallType.NOT_DIFF_EXPRESSED, 
                DataState.LOWQUALITY, 0.05f, 1, 0);
        DiffExpressionCallTO to2 = new DiffExpressionCallTO("321", "ID1", "Anat_id1", "Stage_id1", 
                ComparisonFactor.ANATOMY, DiffExprCallType.NOT_DIFF_EXPRESSED, 
                DataState.HIGHQUALITY, 0.02f, 2, 0, DiffExprCallType.NOT_DIFF_EXPRESSED, 
                DataState.LOWQUALITY, 0.05f, 1, 0);
        assertTrue(TOComparator.areTOsEqual(to1, to2, true));
        assertTrue(TOComparator.areTOsEqual(to1, to2, false));
        
        //Different diffExprCallTypeRNASeq
        to2 = new DiffExpressionCallTO("321", "ID1", "Anat_id1", "Stage_id1", 
                ComparisonFactor.ANATOMY, DiffExprCallType.NOT_DIFF_EXPRESSED, 
                DataState.HIGHQUALITY, 0.02f, 2, 0, DiffExprCallType.UNDER_EXPRESSED, 
                DataState.LOWQUALITY, 0.05f, 1, 0);
        assertFalse(TOComparator.areTOsEqual(to1, to2, true));
        assertFalse(TOComparator.areTOsEqual(to1, to2, false));
        
        //Different id
        to2 = new DiffExpressionCallTO("322", "ID1", "Anat_id1", "Stage_id1", 
                ComparisonFactor.ANATOMY, DiffExprCallType.NOT_DIFF_EXPRESSED, 
                DataState.HIGHQUALITY, 0.02f, 2, 0, DiffExprCallType.NOT_DIFF_EXPRESSED, 
                DataState.LOWQUALITY, 0.05f, 1, 0);
        assertFalse(TOComparator.areTOsEqual(to1, to2, true));
        assertTrue(TOComparator.areTOsEqual(to1, to2, false));

        //both best p-value are null
        to1 = new DiffExpressionCallTO("321", "ID1", "Anat_id1", "Stage_id1", 
                ComparisonFactor.ANATOMY, DiffExprCallType.NOT_DIFF_EXPRESSED, 
                DataState.HIGHQUALITY, null, 2, 0, DiffExprCallType.NOT_DIFF_EXPRESSED, 
                DataState.LOWQUALITY, 0.05f, 1, 0);
        to2 = new DiffExpressionCallTO("321", "ID1", "Anat_id1", "Stage_id1", 
                ComparisonFactor.ANATOMY, DiffExprCallType.NOT_DIFF_EXPRESSED, 
                DataState.HIGHQUALITY, null, 2, 0, DiffExprCallType.NOT_DIFF_EXPRESSED, 
                DataState.LOWQUALITY, 0.05f, 1, 0);
        assertTrue(TOComparator.areTOsEqual(to1, to2, true));

        //best p-value for Affymetrix is null
        to1 = new DiffExpressionCallTO("321", "ID1", "Anat_id1", "Stage_id1", 
                ComparisonFactor.ANATOMY, DiffExprCallType.NOT_DIFF_EXPRESSED, 
                DataState.HIGHQUALITY, null, 2, 0, DiffExprCallType.NOT_DIFF_EXPRESSED, 
                DataState.LOWQUALITY, 0.05f, 1, 0);
        to2 = new DiffExpressionCallTO("321", "ID1", "Anat_id1", "Stage_id1", 
                ComparisonFactor.ANATOMY, DiffExprCallType.NOT_DIFF_EXPRESSED, 
                DataState.HIGHQUALITY, 0.05f, 2, 0, DiffExprCallType.NOT_DIFF_EXPRESSED, 
                DataState.LOWQUALITY, 0.05f, 1, 0);
        assertFalse(TOComparator.areTOsEqual(to1, to2, true));
    }

    /**
     * Test the generic method {@link TOComparator#areTOsEqual(Object, Object, boolean)} 
     * using {@code KeywordTO}s.
     */
    @Test
    public void testAreKeywordTOEqual() {
        KeywordTO to1 = new KeywordTO("ID:1", "name1");
        KeywordTO to2 = new KeywordTO("ID:1", "name1");
        assertTrue(TOComparator.areTOsEqual(to1, to2, true));
        assertTrue(TOComparator.areTOsEqual(to1, to2, false));

        to2 = new KeywordTO("ID:2", "name1");
        assertFalse(TOComparator.areTOsEqual(to1, to2, true));

        to2 = new KeywordTO("ID:1", "name2");
        assertFalse(TOComparator.areTOsEqual(to1, to2, true));
        
        to2 = new KeywordTO("ID:2", "name1");
        assertFalse(TOComparator.areTOsEqual(to1, to2, true));
        assertTrue(TOComparator.areTOsEqual(to1, to2, false));
    }

    /**
     * Test the generic method {@link TOComparator#areTOsEqual(Object, Object, boolean)} 
     * using {@code EntityToKeywordTO}s.
     */
    @Test
    public void testAreEntityToKeywordTOEqual() {
        EntityToKeywordTO to1 = new EntityToKeywordTO("ID:1", "SP:1");
        EntityToKeywordTO to2 = new EntityToKeywordTO("ID:1", "SP:1");
        assertTrue(TOComparator.areTOsEqual(to1, to2));

        to2 = new EntityToKeywordTO("ID:2", "SP:1");
        assertFalse(TOComparator.areTOsEqual(to1, to2));

        to2 = new EntityToKeywordTO("ID:1", "SP:2");
        assertFalse(TOComparator.areTOsEqual(to1, to2));
    }
}
