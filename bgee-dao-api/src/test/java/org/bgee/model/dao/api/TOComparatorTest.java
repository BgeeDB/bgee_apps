package org.bgee.model.dao.api;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.anatdev.AnatEntityDAO.AnatEntityTO;
import org.bgee.model.dao.api.anatdev.StageDAO.StageTO;
import org.bgee.model.dao.api.anatdev.TaxonConstraintDAO.TaxonConstraintTO;
import org.bgee.model.dao.api.anatdev.mapping.SummarySimilarityAnnotationDAO.SimAnnotToAnatEntityTO;
import org.bgee.model.dao.api.anatdev.mapping.SummarySimilarityAnnotationDAO.SummarySimilarityAnnotationTO;
import org.bgee.model.dao.api.expressiondata.DAODataType;
import org.bgee.model.dao.api.expressiondata.call.ConditionDAO;
import org.bgee.model.dao.api.expressiondata.call.CallDAO.CallTO.DataState;
import org.bgee.model.dao.api.expressiondata.call.ConditionDAO.ConditionRankInfoTO;
import org.bgee.model.dao.api.expressiondata.call.ConditionDAO.ConditionTO;
import org.bgee.model.dao.api.expressiondata.call.DiffExpressionCallDAO.DiffExpressionCallTO;
import org.bgee.model.dao.api.expressiondata.call.DiffExpressionCallDAO.DiffExpressionCallTO.ComparisonFactor;
import org.bgee.model.dao.api.expressiondata.call.DiffExpressionCallDAO.DiffExpressionCallTO.DiffExprCallType;
import org.bgee.model.dao.api.expressiondata.call.GlobalExpressionCallDAO.EntityMinMaxRanksTO;
import org.bgee.model.dao.api.expressiondata.rawdata.RawDataCallSourceDAO.CallSourceDataTO.ExclusionReason;
import org.bgee.model.dao.api.expressiondata.rawdata.RawDataConditionDAO.RawDataConditionTO;
import org.bgee.model.dao.api.expressiondata.rawdata.est.ESTDAO.ESTTO;
import org.bgee.model.dao.api.expressiondata.rawdata.est.ESTLibraryDAO.ESTLibraryTO;
import org.bgee.model.dao.api.expressiondata.rawdata.insitu.InSituEvidenceDAO.InSituEvidenceTO;
import org.bgee.model.dao.api.expressiondata.rawdata.insitu.InSituExperimentDAO.InSituExperimentTO;
import org.bgee.model.dao.api.expressiondata.rawdata.insitu.InSituSpotDAO.InSituSpotTO;
import org.bgee.model.dao.api.expressiondata.rawdata.microarray.AffymetrixChipDAO.AffymetrixChipTO;
import org.bgee.model.dao.api.expressiondata.rawdata.microarray.AffymetrixChipDAO.AffymetrixChipTO.DetectionType;
import org.bgee.model.dao.api.expressiondata.rawdata.microarray.AffymetrixChipDAO.AffymetrixChipTO.NormalizationType;
import org.bgee.model.dao.api.expressiondata.rawdata.microarray.AffymetrixProbesetDAO.AffymetrixProbesetTO;
import org.bgee.model.dao.api.expressiondata.rawdata.microarray.MicroarrayExperimentDAO.MicroarrayExperimentTO;
import org.bgee.model.dao.api.expressiondata.rawdata.rnaseq.RNASeqExperimentDAO.RNASeqExperimentTO;
import org.bgee.model.dao.api.expressiondata.rawdata.rnaseq.RNASeqLibraryAnnotatedSampleDAO.RNASeqLibraryAnnotatedSampleTO.AbundanceUnit;
import org.bgee.model.dao.api.expressiondata.rawdata.rnaseq.RNASeqResultAnnotatedSampleDAO.RNASeqResultAnnotatedSampleTO;
import org.bgee.model.dao.api.file.DownloadFileDAO.DownloadFileTO;
import org.bgee.model.dao.api.file.DownloadFileDAO.DownloadFileTO.CategoryEnum;
import org.bgee.model.dao.api.file.SpeciesDataGroupDAO.SpeciesDataGroupTO;
import org.bgee.model.dao.api.file.SpeciesDataGroupDAO.SpeciesToDataGroupTO;
import org.bgee.model.dao.api.gene.GeneDAO.GeneTO;
import org.bgee.model.dao.api.gene.GeneOntologyDAO.GOTermTO;
import org.bgee.model.dao.api.gene.GeneOntologyDAO.GOTermTO.Domain;
import org.bgee.model.dao.api.gene.GeneXRefDAO.GeneXRefTO;
import org.bgee.model.dao.api.gene.HierarchicalGroupDAO.HierarchicalNodeTO;
import org.bgee.model.dao.api.gene.HierarchicalGroupDAO.HierarchicalNodeToGeneTO;
import org.bgee.model.dao.api.keyword.KeywordDAO.EntityToKeywordTO;
import org.bgee.model.dao.api.keyword.KeywordDAO.KeywordTO;
import org.bgee.model.dao.api.ontologycommon.RelationDAO.RelationTO;
import org.bgee.model.dao.api.ontologycommon.RelationDAO.RelationTO.RelationStatus;
import org.bgee.model.dao.api.ontologycommon.RelationDAO.RelationTO.RelationType;
import org.bgee.model.dao.api.source.SourceDAO.SourceTO;
import org.bgee.model.dao.api.source.SourceDAO.SourceTO.SourceCategory;
import org.bgee.model.dao.api.source.SourceToSpeciesDAO.SourceToSpeciesTO;
import org.bgee.model.dao.api.source.SourceToSpeciesDAO.SourceToSpeciesTO.InfoType;
import org.bgee.model.dao.api.species.SpeciesDAO.SpeciesTO;
import org.bgee.model.dao.api.species.TaxonDAO.TaxonTO;
import org.junit.Test;

/**
 * Test the functionalities of {@link TOComparator}.
 *  
 * @author  Frederic Bastian
 * @author  Valentine Rech de Laval
 * @version Bgee 14, Apr. 2019
 * @since   Bgee 13, Sep. 2014
 */
//TODO: add test for GlobalExpressionCallTO
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
     * Test the generic method {@link TOComparator#areTOsEqual(TransferObject, TransferObject, boolean)} 
     * using {@code SpeciesTO}s.
     */
    @Test
    public void testAreSpeciesTOEqual() {
        SpeciesTO to1 = new SpeciesTO(1, "name1", "genus1", "species1", 1,
                1, "path1", "version1", "assemblyXref", 2, 1);
        SpeciesTO to2 = new SpeciesTO(1, "name1", "genus1", "species1", 1,
                1, "path1", "version1", "assemblyXref", 2, 1);
        assertTrue(TOComparator.areTOsEqual(to1, to2, true));
        assertTrue(TOComparator.areTOsEqual(to1, to2, false));

        to2 = new SpeciesTO(2, "name1", "genus1", "species1", 1,
                1, "path1", "version1", "assemblyXref", 2, 1);
        assertFalse(TOComparator.areTOsEqual(to1, to2, true));

        to2 = new SpeciesTO(2, "name1", "genus1", "species1", 1,
                1, "path1", "version1", "assemblyXref", 2, 1);
        assertFalse(TOComparator.areTOsEqual(to1, to2, true));
        
        to2 = new SpeciesTO(2, "name1", "genus1", "species1", 1,
                1, "path1", "version1", "assemblyXref", 2, 1);
        assertFalse(TOComparator.areTOsEqual(to1, to2, true));
        assertTrue(TOComparator.areTOsEqual(to1, to2, false));
        
        to2 = new SpeciesTO(1, "name1", "genus1", "species1", 2, 
                1, "path1", "version1", "assemblyXref", 2, 1);
        assertFalse(TOComparator.areTOsEqual(to1, to2, true));
        assertFalse(TOComparator.areTOsEqual(to1, to2, false));
    }
    
    /**
     * Test the generic method {@link TOComparator#areTOsEqual(TransferObject, TransferObject, boolean)} 
     * using {@code TaxonTO}s.
     */
    @Test
    public void testAreTaxonTOEqual() {
        TaxonTO to1 = new TaxonTO(1, "name1", "sciName1", 1, 3, 1, true);
        TaxonTO to2 = new TaxonTO(1, "name1", "sciName1", 1, 3, 1, true);
        assertTrue(TOComparator.areTOsEqual(to1, to2, true));
        assertTrue(TOComparator.areTOsEqual(to1, to2, false));

        to2 = new TaxonTO(1, "name1", "sciName1", 1, 3, 1, false);
        assertFalse(TOComparator.areTOsEqual(to1, to2, true));
        
        to2 = new TaxonTO(2, "name1", "sciName1", 1, 3, 1, true);
        assertFalse(TOComparator.areTOsEqual(to1, to2, true));
        assertTrue(TOComparator.areTOsEqual(to1, to2, false));
    }
    
    /**
     * Test the generic method {@link TOComparator#areTOsEqual(TransferObject, TransferObject, boolean)} 
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
     * Test the generic method {@link TOComparator#areTOsEqual(TransferObject, TransferObject, boolean)}
     * using {@code GeneTO}s.
     */
    @Test
    public void testAreGeneTOEqual() {
        GeneTO to1 = new GeneTO(1, "ID1", "name1", "desc1", 1, 2, 3, true, 1, "expression summary");
        GeneTO to2 = new GeneTO(1, "ID1", "name1", "desc1", 1, 2, 3, true, 1, "expression summary");
        assertTrue(TOComparator.areTOsEqual(to1, to2, true));
        assertTrue(TOComparator.areTOsEqual(to1, to2, false));
        
        to2 = new GeneTO(1, "ID1", "name1", "desc1", 1, 2, 3, false, 1, "expression summary");
        assertFalse(TOComparator.areTOsEqual(to1, to2, true));
        
        to2 = new GeneTO(2, "ID1", "name1", "desc1", 1, 2, 3, true, 1, "expression summary");
        assertFalse(TOComparator.areTOsEqual(to1, to2, true));
        assertTrue(TOComparator.areTOsEqual(to1, to2, false));
        
        to2 = new GeneTO(1, "ID1", "name1", "desc1", 1, 2, 3, true, 2, "expression summary");
        assertFalse(TOComparator.areTOsEqual(to1, to2, true));
    }
    
    /**
     * Test the generic method {@link TOComparator#areTOsEqual(TransferObject, TransferObject, boolean)}
     * using {@code GeneXRefTO}s.
     */
    @Test
    public void testAreGeneXRefTOEqual() {
        GeneXRefTO to1 = new GeneXRefTO(1, "xref1 ID", "xref1 name", 1);
        GeneXRefTO to2 = new GeneXRefTO(1, "xref1 ID", "xref1 name", 1);
        assertTrue(TOComparator.areTOsEqual(to1, to2));
        assertTrue(TOComparator.areTOsEqual(to1, to2, false));
        
        to2 = new GeneXRefTO(2, "xref1 ID", "xref1 name", 1);
        assertFalse(TOComparator.areTOsEqual(to1, to2, true));
        assertFalse(TOComparator.areTOsEqual(to1, to2, false));
        
        to2 = new GeneXRefTO(2, "xref1 ID", "xref1 name", 22);
        assertFalse(TOComparator.areTOsEqual(to1, to2, true));
        assertFalse(TOComparator.areTOsEqual(to1, to2, false));
    }
    
    /**
     * Test the generic method {@link TOComparator#areTOsEqual(TransferObject, TransferObject, boolean)}
     * using {@code HierarchicalNodeTO}s.
     */
    @Test
    public void testAreHierarchicalNodeTOEqual() {
        HierarchicalNodeTO to1 = new HierarchicalNodeTO(1, "ID1", 1, 2, 10);
        HierarchicalNodeTO to2 = new HierarchicalNodeTO(1, "ID1", 1, 2, 10);
        assertTrue(TOComparator.areTOsEqual(to1, to2, true));
        assertTrue(TOComparator.areTOsEqual(to1, to2, false));
        
        to2 = new HierarchicalNodeTO(1, "ID1", 1, 2, 5);
        assertFalse(TOComparator.areTOsEqual(to1, to2));
        
        to2 = new HierarchicalNodeTO(2, "ID1", 1, 2, 10);
        assertFalse(TOComparator.areTOsEqual(to1, to2, true));
        assertTrue(TOComparator.areTOsEqual(to1, to2, false));
    }
    
    /**
     * Test the generic method {@link TOComparator#areTOsEqual(TransferObject, TransferObject, boolean)}
     * using {@code HierarchicalNodeToGeneTO}s.
     */
    @Test
    public void testAreHierarchicalNodeToGeneTOEqual() {
        HierarchicalNodeToGeneTO to1 = new HierarchicalNodeToGeneTO(1, 1, 1);
        HierarchicalNodeToGeneTO to2 = new HierarchicalNodeToGeneTO(1, 1, 1);
        assertTrue(TOComparator.areTOsEqual(to1, to2));
        
        to2 = new HierarchicalNodeToGeneTO(1, 2, 1);
        assertFalse(TOComparator.areTOsEqual(to1, to2));
        
        to1 = new HierarchicalNodeToGeneTO(2, 2, 2);
        assertFalse(TOComparator.areTOsEqual(to1, to2, true));
    }
    
    /**
     * Test the generic method {@link TOComparator#areTOsEqual(TransferObject, TransferObject, boolean)}
     * using {@code AnatEntityTO}s.
     */
    @Test
    public void testAreAnatEntityTOEqual() {
        AnatEntityTO to1 = new AnatEntityTO("ID1", "name1", "desc1", 
                "stage:1", "stage:2", false, false);
        AnatEntityTO to2 = new AnatEntityTO("ID1", "name1", "desc1", 
                "stage:1", "stage:2", false, false);
        assertTrue(TOComparator.areTOsEqual(to1, to2, true));
        assertTrue(TOComparator.areTOsEqual(to1, to2, false));
        
        to2 = new AnatEntityTO("ID1", "name1", "desc1", 
                "stage:1", "stage:2", true, false);
        assertFalse(TOComparator.areTOsEqual(to1, to2, true));
        
        to2 = new AnatEntityTO("ID2", "name1", "desc1", 
                "stage:1", "stage:2", false, false);
        assertFalse(TOComparator.areTOsEqual(to1, to2, true));
        assertTrue(TOComparator.areTOsEqual(to1, to2, false));
    }
    
    /**
     * Test the generic method {@link TOComparator#areTOsEqual(TransferObject, TransferObject, boolean)}
     * using {@code DownloadFileTO}s.
     */
    @Test
    public void testAreDownloadFileTOsEqual() {
        DownloadFileTO to1 = new DownloadFileTO(1, "name1", "desc1", 
                "path/", 10L, CategoryEnum.EXPR_CALLS_COMPLETE, 1,
                Collections.singleton(ConditionDAO.Attribute.ANAT_ENTITY_ID));
        DownloadFileTO to2 = new DownloadFileTO(1, "name1", "desc1", 
                "path/", 10L, CategoryEnum.EXPR_CALLS_COMPLETE, 1,
                Collections.singleton(ConditionDAO.Attribute.ANAT_ENTITY_ID));
        assertTrue(TOComparator.areTOsEqual(to1, to2, true));
        
        assertTrue(TOComparator.areTOsEqual(to1, to2, false));
        
        to2 = new DownloadFileTO(1, "name1", "desc1", 
                "path/", 10L, CategoryEnum.DIFF_EXPR_ANAT_COMPLETE, 1,
                Collections.singleton(ConditionDAO.Attribute.ANAT_ENTITY_ID));
        assertFalse(TOComparator.areTOsEqual(to1, to2, true));
        
        to2 = new DownloadFileTO(2, "name1", "desc1", 
                "path/", 10L, CategoryEnum.EXPR_CALLS_COMPLETE, 1,
                Collections.singleton(ConditionDAO.Attribute.ANAT_ENTITY_ID));
        assertFalse(TOComparator.areTOsEqual(to1, to2, true));
        assertTrue(TOComparator.areTOsEqual(to1, to2, false));
        
        //regression test when using Long size value > 127, 
        //see http://stackoverflow.com/a/20542511/1768736
        to1 = new DownloadFileTO(1, "name1", "desc1", 
                "path/", 3000L, CategoryEnum.EXPR_CALLS_COMPLETE, 1,
                Collections.singleton(ConditionDAO.Attribute.ANAT_ENTITY_ID));
        to2 = new DownloadFileTO(1, "name1", "desc1", 
                "path/", 3000L, CategoryEnum.EXPR_CALLS_COMPLETE, 1,
                Collections.singleton(ConditionDAO.Attribute.ANAT_ENTITY_ID));
        assertTrue(TOComparator.areTOsEqual(to1, to2, true));
        to1 = new DownloadFileTO(1, "name1", "desc1", 
                "path/", null, CategoryEnum.EXPR_CALLS_COMPLETE, 1,
                Collections.singleton(ConditionDAO.Attribute.ANAT_ENTITY_ID));
        to2 = new DownloadFileTO(1, "name1", "desc1", 
                "path/", null, CategoryEnum.EXPR_CALLS_COMPLETE, 1,
                Collections.singleton(ConditionDAO.Attribute.ANAT_ENTITY_ID));
        assertTrue(TOComparator.areTOsEqual(to1, to2, true));
        
        to2 = new DownloadFileTO(1, "name1", "desc1", 
                "path/", null, CategoryEnum.EXPR_CALLS_COMPLETE, 1,
                new HashSet<>(Arrays.asList(ConditionDAO.Attribute.ANAT_ENTITY_ID, ConditionDAO.Attribute.STAGE_ID)));
        assertFalse(TOComparator.areTOsEqual(to1, to2, true));
    }

    /**
     * Test the generic method {@link TOComparator#areTOsEqual(TransferObject, TransferObject, boolean)}
     * using {@code SpeciesDataGroupTO}s.
     */
    @Test
    public void testAreSpeciesDataGroupTOsEqual() {
        SpeciesDataGroupTO to1 = new SpeciesDataGroupTO(1, "name1", "desc1", 1);

        SpeciesDataGroupTO to2 = new SpeciesDataGroupTO(1, "name1", "desc1", 1);
        assertTrue(TOComparator.areTOsEqual(to1, to2, true));
        
        assertTrue(TOComparator.areTOsEqual(to1, to2, false));
        
        to2 = new SpeciesDataGroupTO(1, "name1", "desc2", 1);
        assertFalse(TOComparator.areTOsEqual(to1, to2, true));
        
        to2 = new SpeciesDataGroupTO(2, "name1", "desc1", 1);
        assertFalse(TOComparator.areTOsEqual(to1, to2, true));
        assertTrue(TOComparator.areTOsEqual(to1, to2, false));
        
        to1 = new SpeciesDataGroupTO(1, "name1", "desc1", null);
        to2 = new SpeciesDataGroupTO(1, "name1", "desc1", 1);
        assertFalse(TOComparator.areTOsEqual(to1, to2, true));
        assertFalse(TOComparator.areTOsEqual(to1, to2, false));
    }

    /**
     * Test the generic method {@link TOComparator#areTOsEqual(TransferObject, TransferObject)}
     * using {@code SpeciesToDataGroupTO}s.
     */
    @Test
    public void testAreSpeciesToDataGroupTOsEqual() {
        SpeciesToDataGroupTO to1 = new SpeciesToDataGroupTO(1, 11);

        SpeciesToDataGroupTO to2 = new SpeciesToDataGroupTO(1, 11);
        assertTrue(TOComparator.areTOsEqual(to1, to2));
        
        to2 = new SpeciesToDataGroupTO(1, 12);
        assertFalse(TOComparator.areTOsEqual(to1, to2));
        
        to2 = new SpeciesToDataGroupTO(2, 11);
        assertFalse(TOComparator.areTOsEqual(to1, to2));
    }

    /**
     * Test the generic method {@link TOComparator#areTOsEqual(TransferObject, TransferObject, boolean)}
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
     * Test the generic method {@link TOComparator#areTOsEqual(TransferObject, TransferObject, boolean)}
     * using {@code TaxonConstraintTO}s.
     */
    @Test
    public void testAreTaxonConstraintTOsEqual() {
        TaxonConstraintTO<String> to1 = new TaxonConstraintTO<>("ID1", 2);
        TaxonConstraintTO<String> to2 = new TaxonConstraintTO<>("ID1", 2);
        assertTrue(TOComparator.areTOsEqual(to1, to2, true));
        assertTrue(TOComparator.areTOsEqual(to1, to2, false));
        
        to2 = new TaxonConstraintTO<>("ID1", 3);
        assertFalse(TOComparator.areTOsEqual(to1, to2, true));
    }
    
    /**
     * Test the generic method {@link TOComparator#areTOsEqual(TransferObject, TransferObject, boolean)}
     * using {@code RelationTO}s.
     */
    @Test
    // TODO: test with Integers
    public void testAreRelationTOsEqual() {
        RelationTO<String> to1 = new RelationTO<>(1, "ID1", "ID2", RelationType.ISA_PARTOF, 
                RelationStatus.DIRECT);
        RelationTO<String> to2 = new RelationTO<>(1, "ID1", "ID2", RelationType.ISA_PARTOF, 
                RelationStatus.DIRECT);
        assertTrue(TOComparator.areTOsEqual(to1, to2, true));
        assertTrue(TOComparator.areTOsEqual(to1, to2, false));
        
        to2 = new RelationTO<>(1, "ID1", "ID2", RelationType.DEVELOPSFROM, 
                RelationStatus.DIRECT);
        assertFalse(TOComparator.areTOsEqual(to1, to2, true));
        
        to2 = new RelationTO<>(1, "ID1", "ID2", RelationType.ISA_PARTOF, 
                RelationStatus.INDIRECT);
        assertFalse(TOComparator.areTOsEqual(to1, to2, true));
        
        to2 = new RelationTO<>(2, "ID1", "ID2", RelationType.ISA_PARTOF, 
                RelationStatus.DIRECT);
        assertFalse(TOComparator.areTOsEqual(to1, to2, true));
        assertTrue(TOComparator.areTOsEqual(to1, to2, false));
    }
    
    /**
     * Test the generic method {@link TOComparator#areTOsEqual(TransferObject, TransferObject, boolean)}
     * using {@code ConditionRankInfoTO}s.
     */
    @Test
    public void testAreConditionRankInfoTOsEqual() {
        ConditionRankInfoTO to1 = new ConditionRankInfoTO(DAODataType.AFFYMETRIX, new BigDecimal("1000"), new BigDecimal("10000"));
        ConditionRankInfoTO to2 = new ConditionRankInfoTO(DAODataType.AFFYMETRIX, new BigDecimal("1000"), new BigDecimal("10000"));
        assertTrue(TOComparator.areTOsEqual(to1, to2));

        //Check with BigDecimals of different scales
        to2 = new ConditionRankInfoTO(DAODataType.AFFYMETRIX, new BigDecimal("1000.00"), new BigDecimal("10000.00"));
        assertTrue(TOComparator.areTOsEqual(to1, to2));

        //Check when they are not equal
        to2 = new ConditionRankInfoTO(DAODataType.EST, new BigDecimal("1000"), new BigDecimal("10000"));
        assertFalse(TOComparator.areTOsEqual(to1, to2, true));
        
        to2 = new ConditionRankInfoTO(DAODataType.AFFYMETRIX, new BigDecimal("5000"), new BigDecimal("10000"));
        assertFalse(TOComparator.areTOsEqual(to1, to2, true));
        
        to2 = new ConditionRankInfoTO(DAODataType.AFFYMETRIX, new BigDecimal("1000"), new BigDecimal("50000"));
        assertFalse(TOComparator.areTOsEqual(to1, to2, true));
    }
    
    /**
     * Test the generic method {@link TOComparator#areTOsEqual(TransferObject, TransferObject, boolean)}
     * using {@code ConditionTO}s.
     */
    @Test
    public void testAreConditionTOsEqual() {
        ConditionTO to1 = new ConditionTO(1, "anatEntityId1", "stageId1", "cellTypeId1", ConditionTO.DAOSex.FEMALE, "wildtype", 99, null);
        ConditionTO to2 = new ConditionTO(1, "anatEntityId1", "stageId1", "cellTypeId1", ConditionTO.DAOSex.FEMALE, "wildtype", 99, null);
        assertTrue(TOComparator.areTOsEqual(to1, to2, true));
        assertTrue(TOComparator.areTOsEqual(to1, to2, false));

        Collection<ConditionRankInfoTO> rankTOs = Arrays.asList(
                new ConditionRankInfoTO(DAODataType.AFFYMETRIX, new BigDecimal("1000"), new BigDecimal("10000")),
                new ConditionRankInfoTO(DAODataType.EST, new BigDecimal("1000"), new BigDecimal("10000")));
        to1 = new ConditionTO(1, "anatEntityId1", "stageId1", "cellTypeId1", ConditionTO.DAOSex.FEMALE, "wildtype", 99, rankTOs);
        to2 = new ConditionTO(1, "anatEntityId1", "stageId1", "cellTypeId1", ConditionTO.DAOSex.FEMALE, "wildtype", 99, rankTOs);
        assertTrue(TOComparator.areTOsEqual(to1, to2, true));
        assertTrue(TOComparator.areTOsEqual(to1, to2, false));

        to2 = new ConditionTO(1, "anatEntityId1", "stageId1", "cellTypeId1", ConditionTO.DAOSex.FEMALE, "wildtype", 99, null);
        assertFalse(TOComparator.areTOsEqual(to1, to2, true));

        to1 = new ConditionTO(1, "anatEntityId1", "stageId1", "cellTypeId1", ConditionTO.DAOSex.FEMALE, "wildtype", 99, null);
        to2 = new ConditionTO(1, "anatEntityId1", "stageId1", "cellTypeId1", ConditionTO.DAOSex.FEMALE, "wildtype", 8, null);
        assertFalse(TOComparator.areTOsEqual(to1, to2, true));

        to2 = new ConditionTO(1, "anatEntityId2", "stageId1", "cellTypeId1", ConditionTO.DAOSex.FEMALE, "wildtype", 99, null);
        assertFalse(TOComparator.areTOsEqual(to1, to2, true));

        to2 = new ConditionTO(86, "anatEntityId1", "stageId1", "cellTypeId1", ConditionTO.DAOSex.FEMALE, "wildtype", 99, null);
        assertFalse(TOComparator.areTOsEqual(to1, to2, true));
        assertTrue(TOComparator.areTOsEqual(to1, to2, false));
        
        to1 = new ConditionTO(1, "anatEntityId1", "stageId1", "cellTypeId1", ConditionTO.DAOSex.FEMALE, "wildtype", 99, rankTOs);
        to2 = new ConditionTO(1, "anatEntityId1", "stageId1", "cellTypeId2", ConditionTO.DAOSex.FEMALE, "wildtype", 99, rankTOs);
        assertFalse(TOComparator.areTOsEqual(to1, to2, true));
        assertFalse(TOComparator.areTOsEqual(to1, to2, false));
    }

    /**
     * Test the generic method {@link TOComparator#areTOsEqual(Object, Object, boolean)}
     * using {@code RawDataConditionTO}s.
     */
    @Test
    public void testAreRawDataConditionTOsEqual() {
        RawDataConditionTO to1 = new RawDataConditionTO(1, 2, "anatEntityId1", "stageId1", "cellTypeId1",
                RawDataConditionTO.DAORawDataSex.FEMALE, false, "strain1", 99);
        RawDataConditionTO to2 = new RawDataConditionTO(1, 2, "anatEntityId1", "stageId1", "cellTypeId1",
                RawDataConditionTO.DAORawDataSex.FEMALE, false, "strain1", 99);
        assertTrue(TOComparator.areTOsEqual(to1, to2, true));
        assertTrue(TOComparator.areTOsEqual(to1, to2, false));

        to2 = new RawDataConditionTO(1, 10, "anatEntityId1", "stageId1", "cellTypeId1",
                RawDataConditionTO.DAORawDataSex.FEMALE, false, "strain1", 99);
        assertFalse(TOComparator.areTOsEqual(to1, to2, true));

        to2 = new RawDataConditionTO(1, 2, "anatEntityId1", "stageId1", "cellTypeId1",
                RawDataConditionTO.DAORawDataSex.MALE, false, "strain1", 99);
        assertFalse(TOComparator.areTOsEqual(to1, to2, true));

        to2 = new RawDataConditionTO(1, 2, "anatEntityId1", "stageId1", "cellTypeId1",
                RawDataConditionTO.DAORawDataSex.FEMALE, true, "strain1", 99);
        assertFalse(TOComparator.areTOsEqual(to1, to2, true));

        to2 = new RawDataConditionTO(1, 2, "anatEntityId1", "stageId1", "cellTypeId1",
                RawDataConditionTO.DAORawDataSex.FEMALE, false, "strain2", 99);
        assertFalse(TOComparator.areTOsEqual(to1, to2, true));

        to2 = new RawDataConditionTO(1, 2, "anatEntityId1", "stageId1", "cellTypeId2",
                RawDataConditionTO.DAORawDataSex.FEMALE, false, "strain1", 99);
        assertFalse(TOComparator.areTOsEqual(to1, to2, true));
        assertFalse(TOComparator.areTOsEqual(to1, to2, false));

        to2 = new RawDataConditionTO(50, 2, "anatEntityId1", "stageId1", "cellTypeId1",
                RawDataConditionTO.DAORawDataSex.FEMALE, false, "strain1", 99);
        assertFalse(TOComparator.areTOsEqual(to1, to2, true));
        assertTrue(TOComparator.areTOsEqual(to1, to2, false));
    }

    /**
     * Test the generic method {@link TOComparator#areTOCollectionsEqual(Collection, Collection, boolean)}.
     */
    @Test
    public void testAreTOCollectionsEqual() {
        Collection<AnatEntityTO> c1 = Arrays.asList(
                new AnatEntityTO("ID1", "name1", "desc1", "stage:1", "stage:2", false, false), 
                new AnatEntityTO("ID2", "name2", "desc2", "stage:2", "stage:3", true, false), 
                new AnatEntityTO("ID3", "name3", "desc3", "stage:3", "stage:4", false, false));
        Collection<AnatEntityTO> c2 = Arrays.asList(
                new AnatEntityTO("ID1", "name1", "desc1", "stage:1", "stage:2", false, false), 
                new AnatEntityTO("ID2", "name2", "desc2", "stage:2", "stage:3", true, false), 
                new AnatEntityTO("ID3", "name3", "desc3", "stage:3", "stage:4", false, false));
        assertTrue(TOComparator.areTOCollectionsEqual(c1, c2, true));
        assertTrue(TOComparator.areTOCollectionsEqual(c1, c2, false));
        
        c2 = Arrays.asList(
                new AnatEntityTO("ID4", "name1", "desc1", "stage:1", "stage:2", false, false), 
                new AnatEntityTO("ID2", "name2", "desc2", "stage:2", "stage:3", true, false), 
                new AnatEntityTO("ID3", "name3", "desc3", "stage:3", "stage:4", false, false));
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
                new AnatEntityTO("ID1", "name1", "desc1", "stage:1", "stage:2", false, false), 
                new AnatEntityTO("ID1", "name1", "desc1", "stage:1", "stage:2", false, false), 
                new AnatEntityTO("ID3", "name3", "desc3", "stage:3", "stage:4", false, false));
        Collection<AnatEntityTO> c2 = Arrays.asList(
                new AnatEntityTO("ID1", "name1", "desc1", "stage:1", "stage:2", false, false), 
                new AnatEntityTO("ID3", "name3", "desc3", "stage:3", "stage:4", false, false), 
                new AnatEntityTO("ID1", "name1", "desc1", "stage:1", "stage:2", false, false));
        assertTrue(TOComparator.areTOCollectionsEqual(c1, c2, true));
        assertTrue(TOComparator.areTOCollectionsEqual(c1, c2, false));
        
        c2 = Arrays.asList(
                new AnatEntityTO("ID1", "name1", "desc1", "stage:1", "stage:2", false, false), 
                new AnatEntityTO("ID3", "name3", "desc3", "stage:3", "stage:4", false, false), 
                new AnatEntityTO("ID3", "name3", "desc3", "stage:3", "stage:4", false, false));
        assertFalse(TOComparator.areTOCollectionsEqual(c1, c2, true));
        assertFalse(TOComparator.areTOCollectionsEqual(c1, c2, false));
    }

    /**
     * Test the generic method {@link TOComparator#areTOsEqual(TransferObject, TransferObject, boolean)}
     * using {@code DiffExpressionCallTO}s.
     */
    @Test
    public void testAreDiffExpressionCallTOEqual() {
        DiffExpressionCallTO to1 = new DiffExpressionCallTO(1, 1, 1, 
                ComparisonFactor.ANATOMY, DiffExprCallType.NOT_DIFF_EXPRESSED, 
                DataState.HIGHQUALITY, 0.02f, 2, 0, DiffExprCallType.NOT_DIFF_EXPRESSED, 
                DataState.LOWQUALITY, 0.05f, 1, 0);
        DiffExpressionCallTO to2 = new DiffExpressionCallTO(1, 1, 1, 
                ComparisonFactor.ANATOMY, DiffExprCallType.NOT_DIFF_EXPRESSED, 
                DataState.HIGHQUALITY, 0.02f, 2, 0, DiffExprCallType.NOT_DIFF_EXPRESSED, 
                DataState.LOWQUALITY, 0.05f, 1, 0);
        assertTrue(TOComparator.areTOsEqual(to1, to2, true));
        assertTrue(TOComparator.areTOsEqual(to1, to2, false));
        
        //Different diffExprCallTypeRNASeq
        to2 = new DiffExpressionCallTO(1, 1, 1, 
                ComparisonFactor.ANATOMY, DiffExprCallType.NOT_DIFF_EXPRESSED, 
                DataState.HIGHQUALITY, 0.02f, 2, 0, DiffExprCallType.UNDER_EXPRESSED, 
                DataState.LOWQUALITY, 0.05f, 1, 0);
        assertFalse(TOComparator.areTOsEqual(to1, to2, true));
        assertFalse(TOComparator.areTOsEqual(to1, to2, false));
        
        //Different id
        to2 = new DiffExpressionCallTO(2, 1, 1, 
                ComparisonFactor.ANATOMY, DiffExprCallType.NOT_DIFF_EXPRESSED, 
                DataState.HIGHQUALITY, 0.02f, 2, 0, DiffExprCallType.NOT_DIFF_EXPRESSED, 
                DataState.LOWQUALITY, 0.05f, 1, 0);
        assertFalse(TOComparator.areTOsEqual(to1, to2, true));
        assertTrue(TOComparator.areTOsEqual(to1, to2, false));

        //both best p-value are null
        to1 = new DiffExpressionCallTO(1, 1, 1, 
                ComparisonFactor.ANATOMY, DiffExprCallType.NOT_DIFF_EXPRESSED, 
                DataState.HIGHQUALITY, null, 2, 0, DiffExprCallType.NOT_DIFF_EXPRESSED, 
                DataState.LOWQUALITY, 0.05f, 1, 0);
        to2 = new DiffExpressionCallTO(1, 1, 1, 
                ComparisonFactor.ANATOMY, DiffExprCallType.NOT_DIFF_EXPRESSED, 
                DataState.HIGHQUALITY, null, 2, 0, DiffExprCallType.NOT_DIFF_EXPRESSED, 
                DataState.LOWQUALITY, 0.05f, 1, 0);
        assertTrue(TOComparator.areTOsEqual(to1, to2, true));

        //best p-value for Affymetrix is null
        to1 = new DiffExpressionCallTO(1, 1, 1, 
                ComparisonFactor.ANATOMY, DiffExprCallType.NOT_DIFF_EXPRESSED, 
                DataState.HIGHQUALITY, null, 2, 0, DiffExprCallType.NOT_DIFF_EXPRESSED, 
                DataState.LOWQUALITY, 0.05f, 1, 0);
        to2 = new DiffExpressionCallTO(1, 1, 1, 
                ComparisonFactor.ANATOMY, DiffExprCallType.NOT_DIFF_EXPRESSED, 
                DataState.HIGHQUALITY, 0.05f, 2, 0, DiffExprCallType.NOT_DIFF_EXPRESSED, 
                DataState.LOWQUALITY, 0.05f, 1, 0);
        assertFalse(TOComparator.areTOsEqual(to1, to2, true));
    }

    /**
     * Test the generic method {@link TOComparator#areTOsEqual(TransferObject, TransferObject, boolean)}
     * using {@code KeywordTO}s.
     */
    @Test
    public void testAreKeywordTOEqual() {
        KeywordTO to1 = new KeywordTO(1, "name1");
        KeywordTO to2 = new KeywordTO(1, "name1");
        assertTrue(TOComparator.areTOsEqual(to1, to2, true));
        assertTrue(TOComparator.areTOsEqual(to1, to2, false));

        to2 = new KeywordTO(2, "name1");
        assertFalse(TOComparator.areTOsEqual(to1, to2, true));

        to2 = new KeywordTO(1, "name2");
        assertFalse(TOComparator.areTOsEqual(to1, to2, true));
        
        to2 = new KeywordTO(2, "name1");
        assertFalse(TOComparator.areTOsEqual(to1, to2, true));
        assertTrue(TOComparator.areTOsEqual(to1, to2, false));
    }

    /**
     * Test the generic method {@link TOComparator#areTOsEqual(TransferObject, TransferObject, boolean)}
     * using {@code EntityToKeywordTO}s.
     */
    @Test
    public void testAreEntityToKeywordTOEqual() {
        EntityToKeywordTO<Integer> to1 = new EntityToKeywordTO<>(1, 1);
        EntityToKeywordTO<Integer> to2 = new EntityToKeywordTO<>(1, 1);
        assertTrue(TOComparator.areTOsEqual(to1, to2));

        to2 = new EntityToKeywordTO<>(2, 1);
        assertFalse(TOComparator.areTOsEqual(to1, to2));

        to2 = new EntityToKeywordTO<>(1, 2);
        assertFalse(TOComparator.areTOsEqual(to1, to2));
    }

    /**
     * Test the generic method {@link TOComparator#areTOsEqual(TransferObject, TransferObject, boolean)}
     * using {@code SourceTO}s.
     */
    @Test
    public void testAreSourceTOEqual() {
        Date date  = Date.from(LocalDate.of(2012, Month.SEPTEMBER, 19)
                .atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
        Date date2 = Date.from(LocalDate.of(2013, Month.SEPTEMBER, 19)
                .atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
        Integer displayOrder = 1;
        Integer displayOrder2 = 1;
        SourceTO to1 = new SourceTO(1, "First DataSource", "My custom data source", "XRefUrl", 
                "experimentUrl", "evidenceUrl", "baseUrl", date, "1.0", false, 
                SourceCategory.GENOMICS, displayOrder);
        SourceTO to2 = new SourceTO(1, "First DataSource", "My custom data source", "XRefUrl", 
                "experimentUrl", "evidenceUrl", "baseUrl", date, "1.0", false, 
                SourceCategory.GENOMICS, displayOrder);
        assertTrue(TOComparator.areTOsEqual(to1, to2));

        to2 = new SourceTO(1, "Second DataSource", "My custom data source", "XRefUrl", 
                "experimentUrl", "evidenceUrl", "baseUrl", date, "1.0", false, 
                SourceCategory.GENOMICS, displayOrder);
        assertFalse(TOComparator.areTOsEqual(to1, to2));

        to2 = new SourceTO(1, "First DataSource", "My custom data source", "XRefUrl", 
                "experimentUrl", "evidenceUrl", "baseUrl", date2, "1.0", true, 
                SourceCategory.GENOMICS, displayOrder);
        assertFalse(TOComparator.areTOsEqual(to1, to2));
        
        to2 = new SourceTO(1, "First DataSource", "My custom data source", "XRefUrl", 
                "experimentUrl", "evidenceUrl", "baseUrl", date, "1.0", false, 
                SourceCategory.GENOMICS, displayOrder2);
        assertTrue(TOComparator.areTOsEqual(to1, to2));

        to2 = new SourceTO(2, "First DataSource", "My custom data source", "XRefUrl", 
                "experimentUrl", "evidenceUrl", "baseUrl", date, "1.0", false, 
                SourceCategory.GENOMICS, displayOrder);
        assertFalse(TOComparator.areTOsEqual(to1, to2, true));
        assertTrue(TOComparator.areTOsEqual(to1, to2, false));
    }

    /**
     * Test the generic method {@link TOComparator#areTOsEqual(TransferObject, TransferObject, boolean)}
     * using {@code SourceToSpeciesTO}s.
     */
    @Test
    public void testAreSourceToSpeciesTOEqual() {
        SourceToSpeciesTO to1 = new SourceToSpeciesTO(1, 11, DAODataType.EST, InfoType.DATA);
        SourceToSpeciesTO to2 = new SourceToSpeciesTO(1, 11, DAODataType.EST, InfoType.DATA);
        assertTrue(TOComparator.areTOsEqual(to1, to2));

        to2 = new SourceToSpeciesTO(1, 11, DAODataType.AFFYMETRIX, InfoType.DATA);
        assertFalse(TOComparator.areTOsEqual(to1, to2));

        to2 = new SourceToSpeciesTO(1, 11, DAODataType.EST, InfoType.ANNOTATION);
        assertFalse(TOComparator.areTOsEqual(to1, to2));
        
        to2 = new SourceToSpeciesTO(1, 21, DAODataType.EST, InfoType.DATA);
        assertFalse(TOComparator.areTOsEqual(to1, to2));

        to2 = new SourceToSpeciesTO(2, 11, DAODataType.EST, InfoType.DATA);
        assertFalse(TOComparator.areTOsEqual(to1, to2, true));
        assertFalse(TOComparator.areTOsEqual(to1, to2, false));
    }

    /**
     * Test the generic method {@link TOComparator#areTOsEqual(Object, Object)}
     * using {@code AffymetrixProbesetTO}s.
     */
    @Test
    public void testAreAffymetrixProbesetTOEqual() {
        AffymetrixProbesetTO to1 = new AffymetrixProbesetTO("A1", 1, 11, new BigDecimal("11.1"), new BigDecimal("0.5"),
                new BigDecimal("0.9"), 110L, new BigDecimal("5.5"), DataState.HIGHQUALITY, ExclusionReason.NOT_EXCLUDED);
        AffymetrixProbesetTO to2 = new AffymetrixProbesetTO("A1", 1, 11, new BigDecimal("11.1"), new BigDecimal("0.5"),
                new BigDecimal("0.9"), 110L, new BigDecimal("5.5"), DataState.HIGHQUALITY, ExclusionReason.NOT_EXCLUDED);
        assertTrue(TOComparator.areTOsEqual(to1, to2));

        to2 = new AffymetrixProbesetTO("A2", 1, 11, new BigDecimal("11.1"), new BigDecimal("0.5"),
                new BigDecimal("0.9"), 110L, new BigDecimal("5.5"), DataState.HIGHQUALITY, ExclusionReason.NOT_EXCLUDED);
        assertFalse(TOComparator.areTOsEqual(to1, to2));
        assertTrue(TOComparator.areTOsEqual(to1, to2, false));

        to2 = new AffymetrixProbesetTO("A1", 1, 11, new BigDecimal("11.01"), new BigDecimal("0.5"),
                new BigDecimal("0.9"), 110L, new BigDecimal("5.5"), DataState.HIGHQUALITY, ExclusionReason.NOT_EXCLUDED);
        assertFalse(TOComparator.areTOsEqual(to1, to2));
        
        to2 = new AffymetrixProbesetTO("A1", 1, 11, new BigDecimal("11.1"), new BigDecimal("0.05"),
                new BigDecimal("0.9"), 110L, new BigDecimal("5.5"), DataState.HIGHQUALITY, ExclusionReason.NOT_EXCLUDED);
        assertFalse(TOComparator.areTOsEqual(to1, to2));

        to2 = new AffymetrixProbesetTO("A1", 1, 11, new BigDecimal("11.1"), new BigDecimal("0.5"),
                new BigDecimal("0.9"), 110L, new BigDecimal("5.5"), DataState.LOWQUALITY, ExclusionReason.NOT_EXCLUDED);
        assertFalse(TOComparator.areTOsEqual(to1, to2));

        to2 = new AffymetrixProbesetTO("A1", 1, 11, new BigDecimal("11.1"), new BigDecimal("0.5"),
                new BigDecimal("0.9"), 110L, new BigDecimal("5.5"), DataState.HIGHQUALITY, ExclusionReason.PRE_FILTERING);
        assertFalse(TOComparator.areTOsEqual(to1, to2, true));
        assertFalse(TOComparator.areTOsEqual(to1, to2, false));
    }
    /**
     * Test the generic method {@link TOComparator#areTOsEqual(Object, Object)}
     * using {@code AffymetrixChipTO}s.
     */
    @Test
    public void testAreAffymetrixChipTOEqual() {
        AffymetrixChipTO to1 = new AffymetrixChipTO(1, "Chip1", "Exp1", "ChipTypeId1", "2018-07-20", NormalizationType.GC_RMA,
                DetectionType.SCHUSTER, 1, new BigDecimal("10"), new BigDecimal("95.5"), new BigDecimal("8557.5"), 9000);
        AffymetrixChipTO to2 = new AffymetrixChipTO(1, "Chip1", "Exp1", "ChipTypeId1", "2018-07-20", NormalizationType.GC_RMA,
                DetectionType.SCHUSTER, 1, new BigDecimal("10"), new BigDecimal("95.5"), new BigDecimal("8557.5"), 9000);
        assertTrue(TOComparator.areTOsEqual(to1, to2, true));
        assertTrue(TOComparator.areTOsEqual(to1, to2, false));

        to2 = new AffymetrixChipTO(2, "Chip1", "Exp1", "ChipTypeId1", "2018-07-20", NormalizationType.GC_RMA,
                DetectionType.SCHUSTER, 1, new BigDecimal("10"), new BigDecimal("95.5"), new BigDecimal("8557.5"), 9000);
        assertFalse(TOComparator.areTOsEqual(to1, to2, true));
        assertTrue(TOComparator.areTOsEqual(to1, to2, false));

        to2 = new AffymetrixChipTO(1, "Chip1", "Exp1", "ChipTypeId1", "2017-07-20", NormalizationType.GC_RMA,
                DetectionType.SCHUSTER, 1, new BigDecimal("10"), new BigDecimal("95.5"), new BigDecimal("8557.5"), 9000);
        assertFalse(TOComparator.areTOsEqual(to1, to2));
    }
    /**
     * Test the generic method {@link TOComparator#areTOsEqual(Object, Object)}
     * using {@code MicroarrayExperimentTO}s.
     */
    @Test
    public void testAreMicroarrayExperimentTOEqual() {
        MicroarrayExperimentTO to1 = new MicroarrayExperimentTO("Exp1", "name", "description", 1);
        MicroarrayExperimentTO to2 = new MicroarrayExperimentTO("Exp1", "name", "description", 1);
        assertTrue(TOComparator.areTOsEqual(to1, to2));
        assertTrue(TOComparator.areTOsEqual(to1, to2, true));
        assertTrue(TOComparator.areTOsEqual(to1, to2, false));

        to2 = new MicroarrayExperimentTO("Exp2", "name", "description", 1);
        assertFalse(TOComparator.areTOsEqual(to1, to2, true));
        assertTrue(TOComparator.areTOsEqual(to1, to2, false));

        to2 = new MicroarrayExperimentTO("Exp1", "name", "description", 2);
        assertFalse(TOComparator.areTOsEqual(to1, to2));
    }
    /**
     * Test the generic method {@link TOComparator#areTOsEqual(Object, Object)}
     * using {@code RNASeqResultTO}s.
     */
    @Test
    public void testAreRNASeqResultTOEqual() {
        RNASeqResultAnnotatedSampleTO to1 = new RNASeqResultAnnotatedSampleTO(1, 11, AbundanceUnit.TPM,
                new BigDecimal("11.1"), new BigDecimal("5.5"), new BigDecimal("100.2"),
                new BigDecimal("40"), new BigDecimal("0.34"), new BigDecimal("0.05"), 1254L,
                DataState.HIGHQUALITY, ExclusionReason.NOT_EXCLUDED);
        RNASeqResultAnnotatedSampleTO to2 = new RNASeqResultAnnotatedSampleTO(1, 11, AbundanceUnit.TPM,
                new BigDecimal("11.1"), new BigDecimal("5.5"), new BigDecimal("100.2"),
                new BigDecimal("40"), new BigDecimal("0.34"), new BigDecimal("0.05"), 1254L,
                DataState.HIGHQUALITY, ExclusionReason.NOT_EXCLUDED);

        to2 = new RNASeqResultAnnotatedSampleTO(1, 11, AbundanceUnit.TPM,
                new BigDecimal("11.1"), new BigDecimal("5.5"), new BigDecimal("100.2"),
                new BigDecimal("40"), new BigDecimal("0.34"), new BigDecimal("0.05"), 1254L,
                DataState.LOWQUALITY, ExclusionReason.NOT_EXCLUDED);
        assertFalse(TOComparator.areTOsEqual(to1, to2));

        to2 = new RNASeqResultAnnotatedSampleTO(1, 11, AbundanceUnit.TPM,
                new BigDecimal("11.1"), new BigDecimal("8"), new BigDecimal("100.2"),
                new BigDecimal("40"), new BigDecimal("0.34"), new BigDecimal("0.05"), 1254L,
                DataState.HIGHQUALITY, ExclusionReason.NOT_EXCLUDED);
        assertFalse(TOComparator.areTOsEqual(to1, to2));
    }
//    /**
//     * Test the generic method {@link TOComparator#areTOsEqual(Object, Object)}
//     * using {@code RNASeqLibraryTO}s.
//     */
//    @Test
//    public void testAreRNASeqLibraryTOEqual() {
//        RNASeqLibraryTO to1 = new RNASeqLibraryTO("L1", "Exp1", 12, "Platform1", new BigDecimal("36"), new BigDecimal("10"),
//                new BigDecimal("50"), new BigDecimal("70"), new BigDecimal("80"), new BigDecimal("2"), new BigDecimal("5"),
//                1000000, 900000, 100, 150, LibraryType.PAIRED_END, LibraryOrientation.FORWARD, new BigDecimal("8557.5"), 9000);
//        RNASeqLibraryTO to2 = new RNASeqLibraryTO("L1", "Exp1", 12, "Platform1", new BigDecimal("36"), new BigDecimal("10"),
//                new BigDecimal("50"), new BigDecimal("70"), new BigDecimal("80"), new BigDecimal("2"), new BigDecimal("5"),
//                1000000, 900000, 100, 150, LibraryType.PAIRED_END, LibraryOrientation.FORWARD, new BigDecimal("8557.5"), 9000);
//        assertTrue(TOComparator.areTOsEqual(to1, to2));
//        assertTrue(TOComparator.areTOsEqual(to1, to2, true));
//        assertTrue(TOComparator.areTOsEqual(to1, to2, false));
//
//        to2 = new RNASeqLibraryTO("L2", "Exp1", 12, "Platform1", new BigDecimal("36"), new BigDecimal("10"),
//                new BigDecimal("50"), new BigDecimal("70"), new BigDecimal("80"), new BigDecimal("2"), new BigDecimal("5"),
//                1000000, 900000, 100, 150, LibraryType.PAIRED_END, LibraryOrientation.FORWARD, new BigDecimal("8557.5"), 9000);
//        assertFalse(TOComparator.areTOsEqual(to1, to2, true));
//        assertTrue(TOComparator.areTOsEqual(to1, to2, false));
//
//        to2 = new RNASeqLibraryTO("L1", "Exp1", 12, "Platform1", new BigDecimal("36"), new BigDecimal("10"),
//                new BigDecimal("50"), new BigDecimal("70"), new BigDecimal("80"), new BigDecimal("2"), new BigDecimal("5"),
//                1000000, 900000, 100, 150, LibraryType.SINGLE_READ, LibraryOrientation.FORWARD, new BigDecimal("8557.5"), 9000);
//        assertFalse(TOComparator.areTOsEqual(to1, to2));
//    }
    /**
     * Test the generic method {@link TOComparator#areTOsEqual(Object, Object)}
     * using {@code RNASeqExperimentTO}s.
     */
    @Test
    public void testAreRNASeqExperimentTOEqual() {
        RNASeqExperimentTO to1 = new RNASeqExperimentTO("Exp1", "name", "description", 1, true, 232, "DOI1");
        RNASeqExperimentTO to2 = new RNASeqExperimentTO("Exp1", "name", "description", 1, true, 232, "DOI1");
        assertTrue(TOComparator.areTOsEqual(to1, to2));
        assertTrue(TOComparator.areTOsEqual(to1, to2, true));
        assertTrue(TOComparator.areTOsEqual(to1, to2, false));

        to2 = new RNASeqExperimentTO("Exp2", "name", "description", 1, true, 232, "DOI1");
        assertFalse(TOComparator.areTOsEqual(to1, to2, true));
        assertTrue(TOComparator.areTOsEqual(to1, to2, false));

        to2 = new RNASeqExperimentTO("Exp1", "name", "description", 2, true, 232, "DOI1");
        assertFalse(TOComparator.areTOsEqual(to1, to2));
    }
    /**
     * Test the generic method {@link TOComparator#areTOsEqual(Object, Object)}
     * using {@code ESTLibraryTO}s.
     */
    @Test
    public void testAreESTLibraryTOEqual() {
        ESTLibraryTO to1 = new ESTLibraryTO("Exp1", "name", "description", 1, 2);
        ESTLibraryTO to2 = new ESTLibraryTO("Exp1", "name", "description", 1, 2);
        assertTrue(TOComparator.areTOsEqual(to1, to2));
        assertTrue(TOComparator.areTOsEqual(to1, to2, true));
        assertTrue(TOComparator.areTOsEqual(to1, to2, false));

        to2 = new ESTLibraryTO("Exp2", "name", "description", 1, 2);
        assertFalse(TOComparator.areTOsEqual(to1, to2, true));
        assertTrue(TOComparator.areTOsEqual(to1, to2, false));

        to2 = new ESTLibraryTO("Exp1", "name2", "description", 1, 2);
        assertFalse(TOComparator.areTOsEqual(to1, to2));

        to2 = new ESTLibraryTO("Exp1", "name", "description", 2, 2);
        assertFalse(TOComparator.areTOsEqual(to1, to2));
    }
    /**
     * Test the generic method {@link TOComparator#areTOsEqual(Object, Object)}
     * using {@code ESTTO}s.
     */
    @Test
    public void testAreESTTOEqual() {
        ESTTO to1 = new ESTTO("ID1", "ID2", "LibId1", "clusterId1", 1, DataState.HIGHQUALITY, new BigDecimal(1), 110L);
        ESTTO to2 = new ESTTO("ID1", "ID2", "LibId1", "clusterId1", 1, DataState.HIGHQUALITY, new BigDecimal(1), 110L);
        assertTrue(TOComparator.areTOsEqual(to1, to2));

        to2 = new ESTTO("ID2", "ID2", "LibId1", "clusterId1", 1, DataState.HIGHQUALITY, new BigDecimal(1), 110L);
        assertFalse(TOComparator.areTOsEqual(to1, to2));
        assertTrue(TOComparator.areTOsEqual(to1, to2, false));

        to2 = new ESTTO("ID1", "ID3", "LibId1", "clusterId1", 1, DataState.HIGHQUALITY, new BigDecimal(1), 110L);
        assertFalse(TOComparator.areTOsEqual(to1, to2));
        
        to2 = new ESTTO("ID1", "ID2", "LibId2", "clusterId1", 1, DataState.HIGHQUALITY, new BigDecimal(1), 110L);
        assertFalse(TOComparator.areTOsEqual(to1, to2));

        to2 = new ESTTO("ID1", "ID2", "LibId1", "clusterId2", 1, DataState.HIGHQUALITY, new BigDecimal(1), 110L);
        assertFalse(TOComparator.areTOsEqual(to1, to2));

        to2 = new ESTTO("ID1", "ID2", "LibId1", "clusterId1", 1, DataState.HIGHQUALITY, new BigDecimal(1), 1L);
        assertFalse(TOComparator.areTOsEqual(to1, to2, true));
        assertFalse(TOComparator.areTOsEqual(to1, to2, false));
    }
    /**
     * Test the generic method {@link TOComparator#areTOsEqual(Object, Object)}
     * using {@code InSituExperimentTO}s.
     */
    @Test
    public void testAreInSituExperimentTOEqual() {
        InSituExperimentTO to1 = new InSituExperimentTO("Exp1", "name", "description", 1);
        InSituExperimentTO to2 = new InSituExperimentTO("Exp1", "name", "description", 1);
        assertTrue(TOComparator.areTOsEqual(to1, to2));
        assertTrue(TOComparator.areTOsEqual(to1, to2, true));
        assertTrue(TOComparator.areTOsEqual(to1, to2, false));

        to2 = new InSituExperimentTO("Exp2", "name", "description", 1);
        assertFalse(TOComparator.areTOsEqual(to1, to2, true));
        assertTrue(TOComparator.areTOsEqual(to1, to2, false));

        to2 = new InSituExperimentTO("Exp1", "name", "description", 2);
        assertFalse(TOComparator.areTOsEqual(to1, to2));
    }
    /**
     * Test the generic method {@link TOComparator#areTOsEqual(Object, Object)}
     * using {@code InSituEvidenceTO}s.
     */
    @Test
    public void testAreInSituEvidenceTOEqual() {
        InSituEvidenceTO to1 = new InSituEvidenceTO("Evidence1", "Exp1", true, "urlPart");
        InSituEvidenceTO to2 = new InSituEvidenceTO("Evidence1", "Exp1", true, "urlPart");
        assertTrue(TOComparator.areTOsEqual(to1, to2));
        assertTrue(TOComparator.areTOsEqual(to1, to2, true));
        assertTrue(TOComparator.areTOsEqual(to1, to2, false));

        to2 = new InSituEvidenceTO("Evidence2", "Exp1", true, "urlPart");
        assertFalse(TOComparator.areTOsEqual(to1, to2, true));
        assertTrue(TOComparator.areTOsEqual(to1, to2, false));

        to2 = new InSituEvidenceTO("Evidence1", "Exp1", false, "urlPart");
        assertFalse(TOComparator.areTOsEqual(to1, to2));

        to2 = new InSituEvidenceTO("Evidence1", "Exp1", true, "urlPart2");
        assertFalse(TOComparator.areTOsEqual(to1, to2));
    }
    /**
     * Test the generic method {@link TOComparator#areTOsEqual(Object, Object)}
     * using {@code InSituSpotTO}s.
     */
    @Test
    public void testAreInSituSpotTOEqual() {
        InSituSpotTO to1 = new InSituSpotTO("ID1", "A1", "pattern1", 1, 11, 110L, new BigDecimal(0.5), DataState.HIGHQUALITY,
                ExclusionReason.NOT_EXCLUDED);
        InSituSpotTO to2 = new InSituSpotTO("ID1", "A1", "pattern1", 1, 11, 110L, new BigDecimal(0.5), DataState.HIGHQUALITY,
                ExclusionReason.NOT_EXCLUDED);
        assertTrue(TOComparator.areTOsEqual(to1, to2));

        to2 = new InSituSpotTO("ID2", "A1", "pattern1", 1, 11, 110L, new BigDecimal(0.5), DataState.HIGHQUALITY,
                ExclusionReason.NOT_EXCLUDED);
        assertFalse(TOComparator.areTOsEqual(to1, to2));
        assertTrue(TOComparator.areTOsEqual(to1, to2, false));

        to2 = new InSituSpotTO("ID1", "A1", "pattern2", 1, 11, 110L, new BigDecimal(0.5), DataState.HIGHQUALITY,
                ExclusionReason.NOT_EXCLUDED);
        assertFalse(TOComparator.areTOsEqual(to1, to2));
        
        to2 = new InSituSpotTO("ID2", "A1", "pattern1", 2, 11, 110L, new BigDecimal(0.5), DataState.HIGHQUALITY,
                ExclusionReason.NOT_EXCLUDED);
        assertFalse(TOComparator.areTOsEqual(to1, to2));

        to2 = new InSituSpotTO("ID1", "A1", "pattern1", 1, 11, 110L, new BigDecimal(0.05), DataState.HIGHQUALITY,
                ExclusionReason.NOT_EXCLUDED);
        assertFalse(TOComparator.areTOsEqual(to1, to2));

        to2 = new InSituSpotTO("ID2", "A1", "pattern1", 1, 11, 120L, new BigDecimal(0.5), DataState.HIGHQUALITY,
                ExclusionReason.NOT_EXCLUDED);
        assertFalse(TOComparator.areTOsEqual(to1, to2, true));
        assertFalse(TOComparator.areTOsEqual(to1, to2, false));
    }

    /**
     * Test the custom {@code BigDecimal} comparator.
     */
    @Test
    public void testBigDecimalComparator() {
        BigDecimal ten0 = BigDecimal.valueOf(10.0);
        BigDecimal ten1 = BigDecimal.valueOf(10);
        BigDecimal tenPlus = BigDecimal.valueOf(10.001);

        assertTrue("Null are the same", TOComparator.areBigDecimalEquals(null, null));
        assertFalse("Null is different", TOComparator.areBigDecimalEquals(null, ten1));
        assertFalse("Null is different", TOComparator.areBigDecimalEquals(ten0, null));
        assertFalse("Should not be equals: " + ten0 + " " + tenPlus, TOComparator.areBigDecimalEquals(ten0, tenPlus));

        assertTrue("Should be equals: " + ten0 + " " + ten1, TOComparator.areBigDecimalEquals(ten0, ten1));
        assertTrue("Should be equals: " + ten0 + " " + ten0, TOComparator.areBigDecimalEquals(ten0, ten0));
    }

    /**
     * Test the generic method {@link TOComparator#areTOsEqual(TransferObject, TransferObject)}
     * using {@code SummarySimilarityAnnotationTO}s.
     */
    @Test
    public void testSummarySimilarityAnnotationComparator() {
        SummarySimilarityAnnotationTO a1 = new SummarySimilarityAnnotationTO(1, 9606, true, "cioId");
        SummarySimilarityAnnotationTO a2 = new SummarySimilarityAnnotationTO(1, 9606, true, "cioId");
        
        assertTrue("Null are the same", TOComparator.areTOsEqual(null, null));
        assertFalse("Null is different", TOComparator.areTOsEqual(a1, null));
        assertFalse("Null is different", TOComparator.areTOsEqual(null, a2));

        assertTrue("Should be equals: " + a1 + " " + a2, TOComparator.areTOsEqual(a1, a2));
        assertTrue("Should be equals: " + a1 + " " + a1, TOComparator.areTOsEqual(a1, a1));

        SummarySimilarityAnnotationTO a3 = new SummarySimilarityAnnotationTO(2, 9606, true, "cioId");
        assertFalse("Should not be equals: " + a1 + " " + a3, TOComparator.areTOsEqual(a1, a3));

        a3 = new SummarySimilarityAnnotationTO(1, 0, true, "cioId");
        assertFalse("Should not be equals: " + a1 + " " + a3, TOComparator.areTOsEqual(a1, a3));

        a3 = new SummarySimilarityAnnotationTO(1, 9606, false, "cioId");
        assertFalse("Should not be equals: " + a1 + " " + a3, TOComparator.areTOsEqual(a1, a3));

        a3 = new SummarySimilarityAnnotationTO(1, 9606, true, "XXX");
        assertFalse("Should not be equals: " + a1 + " " + a3, TOComparator.areTOsEqual(a1, a3));
    }

    /**
     * Test the generic method {@link TOComparator#areTOsEqual(TransferObject, TransferObject)}
     * using {@code SimAnnotToAnatEntityTO}s.
     */
    @Test
    public void testSimAnnotToAnatEntityComparator() {
        SimAnnotToAnatEntityTO a1 = new SimAnnotToAnatEntityTO(1, "cioId1");
        SimAnnotToAnatEntityTO a2 = new SimAnnotToAnatEntityTO(1, "cioId1");

        assertTrue("Null are the same", TOComparator.areTOsEqual(null, null));
        assertFalse("Null is different", TOComparator.areTOsEqual(a1, null));
        assertFalse("Null is different", TOComparator.areTOsEqual(null, a2));

        assertTrue("Should be equals: " + a1 + " " + a2, TOComparator.areTOsEqual(a1, a2));
        assertTrue("Should be equals: " + a1 + " " + a1, TOComparator.areTOsEqual(a1, a1));

        SimAnnotToAnatEntityTO a3 = new SimAnnotToAnatEntityTO(2, "cioId1");
        assertFalse("Should not be equals: " + a1 + " " + a3, TOComparator.areTOsEqual(a1, a3));

        a3 = new SimAnnotToAnatEntityTO(1, "XX");
        assertFalse("Should not be equals: " + a1 + " " + a3, TOComparator.areTOsEqual(a1, a3));
    }

    /**
     * Test the generic method {@link TOComparator#areTOsEqual(Object, Object, boolean)}
     * using {@code EntityMinMaxRanksTO}s.
     */
    @Test
    public void testAreEntityMinMaxRanksTOsEqual() {
        EntityMinMaxRanksTO<Integer> to1 = new EntityMinMaxRanksTO<>(1, new BigDecimal("1"), new BigDecimal("2"), 1);
        EntityMinMaxRanksTO<Integer> to2 = new EntityMinMaxRanksTO<>(1, new BigDecimal("1"), new BigDecimal("2"), 1);
        assertTrue(TOComparator.areTOsEqual(to1, to2, true));
        assertTrue(TOComparator.areTOsEqual(to1, to2, false));

        to2 = new EntityMinMaxRanksTO<>(1, new BigDecimal("1.5"), new BigDecimal("2"), 1);
        assertFalse(TOComparator.areTOsEqual(to1, to2, true));

        to2 = new EntityMinMaxRanksTO<>(1, new BigDecimal("1"), new BigDecimal("2.5"), 1);
        assertFalse(TOComparator.areTOsEqual(to1, to2, true));

        to2 = new EntityMinMaxRanksTO<>(1, new BigDecimal("1"), new BigDecimal("2"), 2);
        assertFalse(TOComparator.areTOsEqual(to1, to2, true));

        to2 = new EntityMinMaxRanksTO<>(2, new BigDecimal("1"), new BigDecimal("2"), 1);
        assertFalse(TOComparator.areTOsEqual(to1, to2, true));
        assertTrue(TOComparator.areTOsEqual(to1, to2, false));
    }

    /**
     * Test the generic method {@link TOComparator#areTOsEqual(Object, Object)}
     * using {@code SummarySimilarityAnnotationTO}s.
     */
    @Test
    public void testAreSummarySimilarityAnnotationTOEqual() {
        SummarySimilarityAnnotationTO to1 = new SummarySimilarityAnnotationTO(1, 1, false, "CIO:001");
        SummarySimilarityAnnotationTO to2 = new SummarySimilarityAnnotationTO(1, 1, false, "CIO:001");
        assertTrue(TOComparator.areTOsEqual(to1, to2));

        to2 = new SummarySimilarityAnnotationTO(2, 1, false, "CIO:001");
        assertFalse(TOComparator.areTOsEqual(to1, to2));
        assertTrue(TOComparator.areTOsEqual(to1, to2, false));

        to2 = new SummarySimilarityAnnotationTO(1, 2, false, "CIO:001");
        assertFalse(TOComparator.areTOsEqual(to1, to2));
        
        to2 = new SummarySimilarityAnnotationTO(1, 1, true, "CIO:001");
        assertFalse(TOComparator.areTOsEqual(to1, to2));

        to2 = new SummarySimilarityAnnotationTO(1, 1, false, "CIO:002");
        assertFalse(TOComparator.areTOsEqual(to1, to2));

        to2 = new SummarySimilarityAnnotationTO(2, 2, false, "CIO:001");
        assertFalse(TOComparator.areTOsEqual(to1, to2, true));
        assertFalse(TOComparator.areTOsEqual(to1, to2, false));
    }
    /**
     * Test the generic method {@link TOComparator#areTOsEqual(Object, Object)}
     * using {@code SimAnnotToAnatEntityTO}s.
     */
    @Test
    public void testAreSimAnnotToAnatEntityTOEqual() {
        SimAnnotToAnatEntityTO to1 = new SimAnnotToAnatEntityTO(1, "UBERON:001");
        SimAnnotToAnatEntityTO to2 = new SimAnnotToAnatEntityTO(1, "UBERON:001");
        assertTrue(TOComparator.areTOsEqual(to1, to2));

        to2 = new SimAnnotToAnatEntityTO(2, "UBERON:001");
        assertFalse(TOComparator.areTOsEqual(to1, to2));

        to2 = new SimAnnotToAnatEntityTO(1, "UBERON:002");
        assertFalse(TOComparator.areTOsEqual(to1, to2));
    }
}
