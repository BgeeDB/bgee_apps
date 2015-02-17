package org.bgee.model.dao.mysql.expressiondata;

import static org.junit.Assert.assertTrue;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.TOComparator;
import org.bgee.model.dao.api.expressiondata.CallDAO.CallTO.DataState;
import org.bgee.model.dao.api.expressiondata.DiffExpressionCallDAO;
import org.bgee.model.dao.api.expressiondata.DiffExpressionCallDAO.DiffExpressionCallTO;
import org.bgee.model.dao.api.expressiondata.DiffExpressionCallDAO.DiffExpressionCallTO.ComparisonFactor;
import org.bgee.model.dao.api.expressiondata.DiffExpressionCallDAO.DiffExpressionCallTO.DiffExprCallType;
import org.bgee.model.dao.api.expressiondata.DiffExpressionCallParams;
import org.bgee.model.dao.mysql.MySQLITAncestor;
import org.junit.Test;


/**
 * Integration tests for {@link MySQLDiffExpressionCallDAO}, performed on a real MySQL database. 
 * See the documentation of {@link org.bgee.model.dao.mysql.MySQLITAncestor} for 
 * important information.
 * 
 * @author Valentine Rech de Laval
 * @version Bgee 13
 * @see org.bgee.model.dao.api.expressiondata.DiffExpressionCallDAO
 * @since Bgee 13
 */

public class MySQLDiffExpressionCallDAOIT extends MySQLITAncestor {

    private final static Logger log = 
            LogManager.getLogger(MySQLDiffExpressionCallDAOIT.class.getName());

    public MySQLDiffExpressionCallDAOIT() {
        super();
    }
    
    @Override
    protected Logger getLogger() {
        return log;
    }

    /**
     * Test the select method {@link MySQLDiffExpressionCallDAO#getDiffExpressionCalls()}.
     * @throws SQLException 
     */
    @Test
    public void shouldGetDiffExpressionCalls() throws SQLException {
        this.useSelectDB();
        
        // On differentialExpression table 
        MySQLDiffExpressionCallDAO dao = new MySQLDiffExpressionCallDAO(this.getMySQLDAOManager());

        // Without speciesIds
        Set<String> speciesIds = new HashSet<String>();
        DiffExpressionCallParams params = new DiffExpressionCallParams();
        params.addAllSpeciesIds(speciesIds);
        // Generate manually expected result
        List<DiffExpressionCallTO> expectedDiffExprCalls = Arrays.asList(
                new DiffExpressionCallTO ("321", "ID1", "Anat_id1", "Stage_id1", 
                        ComparisonFactor.ANATOMY, DiffExprCallType.NOT_DIFF_EXPRESSED, 
                        DataState.HIGHQUALITY, 0.02f, 2, 0, DiffExprCallType.NOT_DIFF_EXPRESSED, 
                        DataState.LOWQUALITY, 0.05f, 1, 0),
                new DiffExpressionCallTO("322", "ID2", "Anat_id9", "Stage_id3", 
                        ComparisonFactor.DEVELOPMENT, DiffExprCallType.UNDER_EXPRESSED, 
                        DataState.LOWQUALITY, 0.06f, 3, 1, DiffExprCallType.NOT_EXPRESSED, 
                        DataState.NODATA, 1f, 0, 0),
                new DiffExpressionCallTO("323", "ID1", "Anat_id2", "Stage_id1", 
                        ComparisonFactor.ANATOMY, DiffExprCallType.NOT_DIFF_EXPRESSED, 
                        DataState.LOWQUALITY, 0.01f, 3, 1, DiffExprCallType.OVER_EXPRESSED,
                        DataState.HIGHQUALITY, 0.001f, 4, 0),
                new DiffExpressionCallTO("324", "ID2", "Anat_id1", "Stage_id1",
                        ComparisonFactor.ANATOMY, DiffExprCallType.NOT_DIFF_EXPRESSED,
                        DataState.LOWQUALITY, 0.03f, 3, 2, DiffExprCallType.NOT_DIFF_EXPRESSED, 
                        DataState.HIGHQUALITY, 0.06f, 1, 0));
        // Compare
        assertTrue("DiffExpressionCallTOs incorrectly retrieved", 
                TOComparator.areTOCollectionsEqual(expectedDiffExprCalls, 
                        dao.getDiffExpressionCalls(params).getAllTOs()));

        // With speciesIds 
        params.addAllSpeciesIds(Arrays.asList("21", "41"));
        // Generate manually expected result
        expectedDiffExprCalls = Arrays.asList(
                new DiffExpressionCallTO("322", "ID2", "Anat_id9", "Stage_id3", 
                        ComparisonFactor.DEVELOPMENT, DiffExprCallType.UNDER_EXPRESSED, 
                        DataState.LOWQUALITY, 0.06f, 3, 1, DiffExprCallType.NOT_EXPRESSED, 
                        DataState.NODATA, 1f, 0, 0),
                new DiffExpressionCallTO("324", "ID2", "Anat_id1", "Stage_id1",
                        ComparisonFactor.ANATOMY, DiffExprCallType.NOT_DIFF_EXPRESSED,
                        DataState.LOWQUALITY, 0.03f, 3, 2, DiffExprCallType.NOT_DIFF_EXPRESSED, 
                        DataState.HIGHQUALITY, 0.06f, 1, 0));
        // Compare
        assertTrue("DiffExpressionCallTOs incorrectly retrieved", 
                TOComparator.areTOCollectionsEqual(expectedDiffExprCalls, 
                        dao.getDiffExpressionCalls(params).getAllTOs()));

        // Test get only COMPARISON_FACTOR without species filter
        params.clearSpeciesIds();
        dao.setAttributes(Arrays.asList(DiffExpressionCallDAO.Attribute.COMPARISON_FACTOR));
        // Generate manually expected result
        expectedDiffExprCalls = Arrays.asList(
                new DiffExpressionCallTO(null, null, null, null, ComparisonFactor.ANATOMY, 
                        null, null, null, null, null, null, null, null, null, null),
                new DiffExpressionCallTO(null, null, null, null, ComparisonFactor.DEVELOPMENT,
                        null, null, null, null, null, null, null, null, null, null));
        // Compare
        assertTrue("DiffExpressionCallTOs incorrectly retrieved", 
                TOComparator.areTOCollectionsEqual(expectedDiffExprCalls, 
                        dao.getDiffExpressionCalls(params).getAllTOs()));
        
        // Test filter on comparison factor without other filter
        params.setComparisonFactor(ComparisonFactor.DEVELOPMENT);
        dao.setAttributes(Arrays.asList(DiffExpressionCallDAO.Attribute.ID));
        // Generate manually expected result
        expectedDiffExprCalls = Arrays.asList(
                new DiffExpressionCallTO("322", null, null, null, null, null, 
                        null, null, null, null, null, null, null, null, null));
        // Compare
        assertTrue("DiffExpressionCallTOs incorrectly retrieved", 
                TOComparator.areTOCollectionsEqual(expectedDiffExprCalls, 
                        dao.getDiffExpressionCalls(params).getAllTOs()));
        
        // Test filter on diffExprCallTypeAffymetrix without other filter
        params.setComparisonFactor(null);
        params.setIncludeAffymetrixTypes(true);
        params.addAffymetrixDiffExprCallType(DiffExprCallType.NOT_DIFF_EXPRESSED);
        dao.clearAttributes();
        // Generate manually expected result
        expectedDiffExprCalls = Arrays.asList(
                new DiffExpressionCallTO ("321", "ID1", "Anat_id1", "Stage_id1", 
                        ComparisonFactor.ANATOMY, DiffExprCallType.NOT_DIFF_EXPRESSED, 
                        DataState.HIGHQUALITY, 0.02f, 2, 0, DiffExprCallType.NOT_DIFF_EXPRESSED, 
                        DataState.LOWQUALITY, 0.05f, 1, 0),
                new DiffExpressionCallTO("323", "ID1", "Anat_id2", "Stage_id1", 
                        ComparisonFactor.ANATOMY, DiffExprCallType.NOT_DIFF_EXPRESSED, 
                        DataState.LOWQUALITY, 0.01f, 3, 1, DiffExprCallType.OVER_EXPRESSED,
                        DataState.HIGHQUALITY, 0.001f, 4, 0),
                new DiffExpressionCallTO("324", "ID2", "Anat_id1", "Stage_id1",
                        ComparisonFactor.ANATOMY, DiffExprCallType.NOT_DIFF_EXPRESSED,
                        DataState.LOWQUALITY, 0.03f, 3, 2, DiffExprCallType.NOT_DIFF_EXPRESSED, 
                        DataState.HIGHQUALITY, 0.06f, 1, 0));
        // Compare
        assertTrue("DiffExpressionCallTOs incorrectly retrieved", 
                TOComparator.areTOCollectionsEqual(expectedDiffExprCalls, 
                        dao.getDiffExpressionCalls(params).getAllTOs()));

        // Test filter on diffExprCallTypeRNASeq without other filter
        params.clearAffymetrixDiffExprCallTypes();
        params.setIncludeRNASeqTypes(false);
        params.addRNASeqDiffExprCallType(DiffExprCallType.OVER_EXPRESSED);
        dao.clearAttributes();
        // Generate manually expected result
        expectedDiffExprCalls = Arrays.asList(
                new DiffExpressionCallTO ("321", "ID1", "Anat_id1", "Stage_id1", 
                        ComparisonFactor.ANATOMY, DiffExprCallType.NOT_DIFF_EXPRESSED, 
                        DataState.HIGHQUALITY, 0.02f, 2, 0, DiffExprCallType.NOT_DIFF_EXPRESSED, 
                        DataState.LOWQUALITY, 0.05f, 1, 0),
                new DiffExpressionCallTO("322", "ID2", "Anat_id9", "Stage_id3", 
                        ComparisonFactor.DEVELOPMENT, DiffExprCallType.UNDER_EXPRESSED, 
                        DataState.LOWQUALITY, 0.06f, 3, 1, DiffExprCallType.NOT_EXPRESSED, 
                        DataState.NODATA, 1f, 0, 0),
                new DiffExpressionCallTO("324", "ID2", "Anat_id1", "Stage_id1",
                        ComparisonFactor.ANATOMY, DiffExprCallType.NOT_DIFF_EXPRESSED,
                        DataState.LOWQUALITY, 0.03f, 3, 2, DiffExprCallType.NOT_DIFF_EXPRESSED, 
                        DataState.HIGHQUALITY, 0.06f, 1, 0));
        // Compare
        assertTrue("DiffExpressionCallTOs incorrectly retrieved", 
                TOComparator.areTOCollectionsEqual(expectedDiffExprCalls, 
                        dao.getDiffExpressionCalls(params).getAllTOs()));

        // Test filter on diffExprCallTypeAffymetrix and diffExprCallTypeRNASeq
        params.clearRNASeqDiffExprCallTypes();
        params.addAffymetrixDiffExprCallType(DiffExprCallType.NOT_DIFF_EXPRESSED);
        params.setIncludeRNASeqTypes(true);
        params.addRNASeqDiffExprCallType(DiffExprCallType.NOT_DIFF_EXPRESSED);
        params.setSatisfyAllCallTypeConditions(true);
        dao.clearAttributes();
        // Generate manually expected result
        expectedDiffExprCalls = Arrays.asList(
                new DiffExpressionCallTO ("321", "ID1", "Anat_id1", "Stage_id1", 
                        ComparisonFactor.ANATOMY, DiffExprCallType.NOT_DIFF_EXPRESSED, 
                        DataState.HIGHQUALITY, 0.02f, 2, 0, DiffExprCallType.NOT_DIFF_EXPRESSED, 
                        DataState.LOWQUALITY, 0.05f, 1, 0),
                new DiffExpressionCallTO("324", "ID2", "Anat_id1", "Stage_id1",
                        ComparisonFactor.ANATOMY, DiffExprCallType.NOT_DIFF_EXPRESSED,
                        DataState.LOWQUALITY, 0.03f, 3, 2, DiffExprCallType.NOT_DIFF_EXPRESSED, 
                        DataState.HIGHQUALITY, 0.06f, 1, 0));
        // Compare
        assertTrue("DiffExpressionCallTOs incorrectly retrieved", 
                TOComparator.areTOCollectionsEqual(expectedDiffExprCalls, 
                        dao.getDiffExpressionCalls(params).getAllTOs()));

        // Test filter on diffExprCallTypeAffymetrix or diffExprCallTypeRNASeq
        params.setSatisfyAllCallTypeConditions(false);
        dao.clearAttributes();
        // Generate manually expected result
        expectedDiffExprCalls = Arrays.asList(
                new DiffExpressionCallTO ("321", "ID1", "Anat_id1", "Stage_id1", 
                        ComparisonFactor.ANATOMY, DiffExprCallType.NOT_DIFF_EXPRESSED, 
                        DataState.HIGHQUALITY, 0.02f, 2, 0, DiffExprCallType.NOT_DIFF_EXPRESSED, 
                        DataState.LOWQUALITY, 0.05f, 1, 0),
                new DiffExpressionCallTO("323", "ID1", "Anat_id2", "Stage_id1", 
                        ComparisonFactor.ANATOMY, DiffExprCallType.NOT_DIFF_EXPRESSED, 
                        DataState.LOWQUALITY, 0.01f, 3, 1, DiffExprCallType.OVER_EXPRESSED,
                        DataState.HIGHQUALITY, 0.001f, 4, 0),
                new DiffExpressionCallTO("324", "ID2", "Anat_id1", "Stage_id1",
                        ComparisonFactor.ANATOMY, DiffExprCallType.NOT_DIFF_EXPRESSED,
                        DataState.LOWQUALITY, 0.03f, 3, 2, DiffExprCallType.NOT_DIFF_EXPRESSED, 
                        DataState.HIGHQUALITY, 0.06f, 1, 0));
        // Compare
        assertTrue("DiffExpressionCallTOs incorrectly retrieved", 
                TOComparator.areTOCollectionsEqual(expectedDiffExprCalls, 
                        dao.getDiffExpressionCalls(params).getAllTOs()));
        
        // Test all filters in same time
        params.addAllSpeciesIds(Arrays.asList("11"));
        params.setComparisonFactor(ComparisonFactor.ANATOMY);
        params.clearAffymetrixDiffExprCallTypes();
        params.addAffymetrixDiffExprCallType(DiffExprCallType.NOT_DIFF_EXPRESSED);
        params.setIncludeAffymetrixTypes(true);
        params.clearRNASeqDiffExprCallTypes();
        params.addRNASeqDiffExprCallType(DiffExprCallType.NOT_DIFF_EXPRESSED);
        params.setIncludeRNASeqTypes(false);
        params.setSatisfyAllCallTypeConditions(true);
        // Generate manually expected result
        expectedDiffExprCalls = Arrays.asList(
                new DiffExpressionCallTO("323", "ID1", "Anat_id2", "Stage_id1", 
                        ComparisonFactor.ANATOMY, DiffExprCallType.NOT_DIFF_EXPRESSED, 
                        DataState.LOWQUALITY, 0.01f, 3, 1, DiffExprCallType.OVER_EXPRESSED,
                        DataState.HIGHQUALITY, 0.001f, 4, 0));
        // Compare
        assertTrue("DiffExpressionCallTOs incorrectly retrieved", 
                TOComparator.areTOCollectionsEqual(expectedDiffExprCalls, 
                        dao.getDiffExpressionCalls(params).getAllTOs()));
    }
}
