package org.bgee.model.dao.mysql.expressiondata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.DAOManager;
import org.bgee.model.dao.api.TOComparator;
import org.bgee.model.dao.api.expressiondata.CallDAO.CallTO.DataState;
import org.bgee.model.dao.api.expressiondata.ExpressionCallDAO;
import org.bgee.model.dao.api.expressiondata.ExpressionCallDAO.ExpressionCallTO;
import org.bgee.model.dao.api.expressiondata.ExpressionCallDAO.ExpressionCallTO.OriginOfLine;
import org.bgee.model.dao.api.expressiondata.ExpressionCallDAO.GlobalExpressionToExpressionTO;
import org.bgee.model.dao.api.expressiondata.ExpressionCallParams;
import org.bgee.model.dao.mysql.MySQLITAncestor;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.model.dao.mysql.expressiondata.MySQLExpressionCallDAO.MySQLExpressionCallTOResultSet;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;


/**
 * Integration tests for {@link MySQLExpressionCallDAO}, performed on a real MySQL database. 
 * See the documentation of {@link org.bgee.model.dao.mysql.MySQLITAncestor} for 
 * important information.
 * 
 * @author Valentine Rech de Laval
 * @version Bgee 13
 * @see org.bgee.model.dao.api.expressiondata.ExpressionCallDAO
 * @since Bgee 13
 */
public class MySQLExpressionCallDAOIT extends MySQLITAncestor {

    private final static Logger log = 
            LogManager.getLogger(MySQLExpressionCallDAOIT.class.getName());

    public MySQLExpressionCallDAOIT() {
        super();
    }
    
    @Override
    protected Logger getLogger() {
        return log;
    }

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    /**
     * Test the select method {@link MySQLExpressionCallDAO#getExpressionCalls()}.
     */
//    @Test
    public void shouldGetExpressionCalls() throws SQLException {
        
        this.useSelectDB();

        // On expression table 
        MySQLExpressionCallDAO dao = new MySQLExpressionCallDAO(this.getMySQLDAOManager());

        // Without speciesIds and not include substructures
        // Generate parameters
        Set<String> speciesIds = new HashSet<String>();
        ExpressionCallParams params = new ExpressionCallParams();
        params.addAllSpeciesIds(speciesIds);
        params.setIncludeSubstructures(false);
        // Generate manually expected result
        List<ExpressionCallTO> expectedExprCalls = Arrays.asList(
                new ExpressionCallTO("1", "ID3", "Anat_id1", "Stage_id1", DataState.LOWQUALITY, 
                        DataState.NODATA, DataState.HIGHQUALITY, DataState.HIGHQUALITY,
                        false, false, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("2", "ID1", "Anat_id6", "Stage_id6", DataState.LOWQUALITY,
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.LOWQUALITY,
                        false, false, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("3", "ID1", "Anat_id6", "Stage_id7", DataState.NODATA, 
                        DataState.NODATA, DataState.NODATA, DataState.LOWQUALITY,
                        false, false, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("4", "ID2", "Anat_id2", "Stage_id18", DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        false, false, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("5", "ID1", "Anat_id7", "Stage_id10", DataState.LOWQUALITY, 
                        DataState.LOWQUALITY, DataState.LOWQUALITY, DataState.LOWQUALITY, 
                        false, false, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("6", "ID2", "Anat_id11", "Stage_id12", DataState.HIGHQUALITY, 
                        DataState.LOWQUALITY, DataState.NODATA, DataState.HIGHQUALITY, 
                        false, false, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("7", "ID2", "Anat_id11", "Stage_id13", DataState.NODATA, 
                        DataState.HIGHQUALITY, DataState.LOWQUALITY, DataState.NODATA, 
                        false, false, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("8", "ID3", "Anat_id3", "Stage_id1", DataState.NODATA, 
                        DataState.HIGHQUALITY, DataState.LOWQUALITY, DataState.NODATA,
                        false, false, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("9", "ID2", "Anat_id1", "Stage_id9", DataState.HIGHQUALITY, 
                        DataState.LOWQUALITY, DataState.NODATA, DataState.HIGHQUALITY, 
                        false, false, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("10", "ID1", "Anat_id6", "Stage_id8", DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, DataState.NODATA, DataState.NODATA, 
                        false, false, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("11", "ID2", "Anat_id1", "Stage_id2", DataState.NODATA, 
                        DataState.HIGHQUALITY, DataState.LOWQUALITY, DataState.NODATA, 
                        false, false, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("12", "ID1", "Anat_id1", "Stage_id1", DataState.NODATA, 
                        DataState.NODATA, DataState.LOWQUALITY, DataState.NODATA, 
                        false, false, OriginOfLine.SELF, OriginOfLine.SELF, true));

        // Compare
        MySQLExpressionCallTOResultSet rs = 
                (MySQLExpressionCallTOResultSet) dao.getExpressionCalls(params);
        List<ExpressionCallTO> expressions = rs.getAllTOs();
        assertTrue("ExpressionCallTOs incorrectly retrieved", 
                TOComparator.areTOCollectionsEqual(expectedExprCalls, expressions));
        assertFalse("Incorrect filtering of duplicates", rs.isFilterDuplicates());
        assertFalse("Incorrect use of the LIMIT feature", rs.isUsingLimitFeature());

        // Same test but with only two attributes
        // Generate parameters
        dao.setAttributes(
                ExpressionCallDAO.Attribute.GENE_ID, ExpressionCallDAO.Attribute.OBSERVED_DATA);
        // Generate manually expected result
        expectedExprCalls = Arrays.asList(
                new ExpressionCallTO(null, "ID1", null, null, null, null, null, null, null, null, 
                        null, null, true),
                new ExpressionCallTO(null, "ID2", null, null, null, null, null, null, null, null, 
                        null, null, true),
                new ExpressionCallTO(null, "ID3", null, null, null, null, null, null, null, null, 
                        null, null, true));
        // Compare
        rs = (MySQLExpressionCallTOResultSet) dao.getExpressionCalls(params);
        expressions = rs.getAllTOs();
        assertTrue("ExpressionCallTOs incorrectly retrieved", 
                TOComparator.areTOCollectionsEqual(expectedExprCalls, expressions));
        assertFalse("Incorrect filtering of duplicates", rs.isFilterDuplicates());
        assertFalse("Incorrect use of the LIMIT feature", rs.isUsingLimitFeature());
        
        // With speciesIds but not include substructures 
        dao.clearAttributes();
        // Generate parameters
        params.addAllSpeciesIds(Arrays.asList("11", "41")); // 41 = species Id that does not exist
        // Generate manually expected result
        expectedExprCalls = Arrays.asList(
                new ExpressionCallTO("2","ID1", "Anat_id6", "Stage_id6", DataState.LOWQUALITY, 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.LOWQUALITY, 
                        false, false, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("3","ID1", "Anat_id6", "Stage_id7", DataState.NODATA, 
                        DataState.NODATA, DataState.NODATA, DataState.LOWQUALITY, 
                        false, false, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("5","ID1", "Anat_id7", "Stage_id10", DataState.LOWQUALITY, 
                        DataState.LOWQUALITY, DataState.LOWQUALITY, DataState.LOWQUALITY, 
                        false, false, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("10", "ID1", "Anat_id6", "Stage_id8", DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, DataState.NODATA, DataState.NODATA, 
                        false, false, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("12", "ID1", "Anat_id1", "Stage_id1", DataState.NODATA, 
                        DataState.NODATA, DataState.LOWQUALITY, DataState.NODATA, 
                        false, false, OriginOfLine.SELF, OriginOfLine.SELF, true));
        // Compare
        rs = (MySQLExpressionCallTOResultSet) dao.getExpressionCalls(params);
        expressions = rs.getAllTOs();
        assertTrue("ExpressionCallTOs incorrectly retrieved", 
                TOComparator.areTOCollectionsEqual(expectedExprCalls, expressions));
        assertFalse("Incorrect filtering of duplicates", rs.isFilterDuplicates());
        assertFalse("Incorrect use of the LIMIT feature", rs.isUsingLimitFeature());        

        // On global expression table 
        // With speciesIds (11 and 41) and include substructures 
        // Generate parameters
        params.setIncludeSubstructures(true);
        // Generate manually expected result
        expectedExprCalls = Arrays.asList(
                new ExpressionCallTO("2", "ID1", "Anat_id6", "Stage_id6", DataState.LOWQUALITY, 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.LOWQUALITY, 
                        true, false, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("3", "ID1", "Anat_id6", "Stage_id7", DataState.NODATA, 
                        DataState.NODATA, DataState.NODATA, DataState.LOWQUALITY, 
                        true, false, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("5", "ID1", "Anat_id7", "Stage_id10", DataState.LOWQUALITY, 
                        DataState.LOWQUALITY, DataState.LOWQUALITY, DataState.LOWQUALITY, 
                        true, false, OriginOfLine.BOTH, OriginOfLine.SELF, true),
                new ExpressionCallTO("22", "ID1", "Anat_id6", "Stage_id8", DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, DataState.NODATA, DataState.NODATA, 
                        true, false, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("24", "ID1", "Anat_id1", "Stage_id8", DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, DataState.NODATA, DataState.NODATA, 
                        true, false, OriginOfLine.DESCENT, OriginOfLine.SELF, false),
                new ExpressionCallTO("26", "ID1", "Anat_id1", "Stage_id1", DataState.NODATA, 
                        DataState.NODATA, DataState.LOWQUALITY, DataState.NODATA, 
                        true, false, OriginOfLine.SELF, OriginOfLine.SELF, true));

        // Compare
        rs = (MySQLExpressionCallTOResultSet) dao.getExpressionCalls(params);
        expressions = rs.getAllTOs();
        assertTrue("ExpressionCallTOs incorrectly retrieved, expected: " + expectedExprCalls + 
                ", but was: " + expressions, 
                TOComparator.areTOCollectionsEqual(expectedExprCalls, expressions));
        assertFalse("Incorrect filtering of duplicates", rs.isFilterDuplicates());
        assertFalse("Incorrect use of the LIMIT feature", rs.isUsingLimitFeature());

        // Same test but with only two attributes
        // Generate parameters
        dao.setAttributes(
                ExpressionCallDAO.Attribute.GENE_ID, ExpressionCallDAO.Attribute.OBSERVED_DATA);
        // Generate manually expected result
        expectedExprCalls = Arrays.asList(
                new ExpressionCallTO(null, "ID1", null, null, null, null, null, null, 
                        null, null, null, null, true),
                new ExpressionCallTO(null, "ID1", null, null, null, null, null, null, 
                        null, null, null, null, false));
        // Compare
        rs = (MySQLExpressionCallTOResultSet) dao.getExpressionCalls(params);
        expressions = rs.getAllTOs();
        assertTrue("ExpressionCallTOs incorrectly retrieved", 
                TOComparator.areTOCollectionsEqual(expectedExprCalls, expressions));
        assertFalse("Incorrect filtering of duplicates", rs.isFilterDuplicates());
        assertFalse("Incorrect use of the LIMIT feature", rs.isUsingLimitFeature());
        
        // Without species filter but include substructures
        dao.clearAttributes();
        // Generate parameters
        params.clearSpeciesIds();
        //Generate manually expected result
        expectedExprCalls = Arrays.asList(
                new ExpressionCallTO("1", "ID3", "Anat_id1", "Stage_id1", 
                        DataState.LOWQUALITY, DataState.NODATA, DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, true, false, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("2", "ID1", "Anat_id6", "Stage_id6", 
                        DataState.LOWQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.LOWQUALITY, true, false, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("3", "ID1", "Anat_id6", "Stage_id7", 
                        DataState.NODATA, DataState.NODATA, DataState.NODATA, 
                        DataState.LOWQUALITY, true, false, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("4", "ID2", "Anat_id2", "Stage_id18",
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY,
                        DataState.HIGHQUALITY, true, false, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("5", "ID1", "Anat_id7", "Stage_id10", 
                        DataState.LOWQUALITY, DataState.LOWQUALITY, DataState.LOWQUALITY, 
                        DataState.LOWQUALITY, true, false, OriginOfLine.BOTH, OriginOfLine.SELF, true),
                new ExpressionCallTO("6", "ID2", "Anat_id11", "Stage_id12",
                        DataState.HIGHQUALITY, DataState.LOWQUALITY, DataState.NODATA, 
                        DataState.HIGHQUALITY, true, false, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("7", "ID2", "Anat_id11", "Stage_id13",
                        DataState.NODATA, DataState.HIGHQUALITY, DataState.LOWQUALITY, 
                        DataState.NODATA, true, false, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("8", "ID3", "Anat_id2", "Stage_id1", 
                        DataState.LOWQUALITY, DataState.NODATA, DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, true, false, OriginOfLine.DESCENT, OriginOfLine.SELF, false),
                new ExpressionCallTO("9", "ID3", "Anat_id3", "Stage_id1", 
                        DataState.LOWQUALITY, DataState.NODATA, DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, true, false, OriginOfLine.DESCENT, OriginOfLine.SELF, false),
                new ExpressionCallTO("10", "ID3", "Anat_id4", "Stage_id1", 
                        DataState.LOWQUALITY, DataState.NODATA, DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, true, false, OriginOfLine.DESCENT, OriginOfLine.SELF, false),
                new ExpressionCallTO("11", "ID3", "Anat_id5", "Stage_id1", 
                        DataState.LOWQUALITY, DataState.NODATA, DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, true, false, OriginOfLine.DESCENT, OriginOfLine.SELF, false),
                new ExpressionCallTO("12", "ID3", "Anat_id6", "Stage_id1", 
                        DataState.LOWQUALITY, DataState.NODATA, DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, true, false, OriginOfLine.DESCENT, OriginOfLine.SELF, false),
                new ExpressionCallTO("13", "ID3", "Anat_id9", "Stage_id1", 
                        DataState.LOWQUALITY, DataState.NODATA, DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, true, false, OriginOfLine.DESCENT, OriginOfLine.SELF, false),
                new ExpressionCallTO("14", "ID3", "Anat_id10", "Stage_id1", 
                        DataState.LOWQUALITY, DataState.NODATA, DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, true, false, OriginOfLine.DESCENT, OriginOfLine.SELF, false),
                new ExpressionCallTO("15", "ID3", "Anat_id11", "Stage_id1", 
                        DataState.LOWQUALITY, DataState.NODATA, DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, true, false, OriginOfLine.DESCENT, OriginOfLine.SELF, false),
                new ExpressionCallTO("16", "ID2", "Anat_id3", "Stage_id18", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY,
                        DataState.HIGHQUALITY, true, false, OriginOfLine.DESCENT, OriginOfLine.SELF, false),
                new ExpressionCallTO("17", "ID2", "Anat_id4", "Stage_id18", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, true, false, OriginOfLine.DESCENT, OriginOfLine.SELF, false),
                new ExpressionCallTO("18", "ID2", "Anat_id5", "Stage_id18", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, true, false, OriginOfLine.DESCENT, OriginOfLine.SELF, false),
                new ExpressionCallTO("19", "ID2", "Anat_id9", "Stage_id18", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, true, false, OriginOfLine.DESCENT, OriginOfLine.SELF, false),
                new ExpressionCallTO("20", "ID2", "Anat_id10", "Stage_id18", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, true, false, OriginOfLine.DESCENT, OriginOfLine.SELF, false),
                new ExpressionCallTO("21", "ID2", "Anat_id11", "Stage_id18", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, true, false, OriginOfLine.DESCENT, OriginOfLine.SELF, false),
                new ExpressionCallTO("22", "ID1", "Anat_id6", "Stage_id8", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.NODATA, 
                        DataState.NODATA, true, false, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("23", "ID2", "Anat_id1", "Stage_id1", 
                        DataState.HIGHQUALITY, DataState.LOWQUALITY, DataState.NODATA, 
                        DataState.HIGHQUALITY, true, false, OriginOfLine.DESCENT, OriginOfLine.SELF, false),
                new ExpressionCallTO("24", "ID1", "Anat_id1", "Stage_id8", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.NODATA, 
                        DataState.NODATA, true, false, OriginOfLine.DESCENT, OriginOfLine.SELF, false),
                new ExpressionCallTO("25", "ID2", "Anat_id1", "Stage_id2", 
                        DataState.NODATA, DataState.HIGHQUALITY, DataState.LOWQUALITY, 
                        DataState.NODATA, true, false, OriginOfLine.SELF, OriginOfLine.SELF, false),
                new ExpressionCallTO("26", "ID1", "Anat_id1", "Stage_id1", DataState.NODATA, 
                        DataState.NODATA, DataState.LOWQUALITY, DataState.NODATA, 
                        true, false, OriginOfLine.SELF, OriginOfLine.SELF, true));
        // Compare
        rs = (MySQLExpressionCallTOResultSet) dao.getExpressionCalls(params);
        expressions = rs.getAllTOs();
        assertTrue("ExpressionCallTOs incorrectly retrieved", 
                TOComparator.areTOCollectionsEqual(expectedExprCalls, expressions));
        assertFalse("Incorrect filtering of duplicates", rs.isFilterDuplicates());
        assertFalse("Incorrect use of the LIMIT feature", rs.isUsingLimitFeature());
        
        // Test get only GENEID without species filter and without including substructures
        dao.clearAttributes();
        dao.setAttributes(Arrays.asList(ExpressionCallDAO.Attribute.GENE_ID));
        params.clearSpeciesIds();
        params.setIncludeSubstructures(false);
        expectedExprCalls = Arrays.asList(
                new ExpressionCallTO(null, "ID1", null, null, null, null, null, null, null, null, null, null, null),
                new ExpressionCallTO(null, "ID2", null, null, null, null, null, null, null, null, null, null, null),
                new ExpressionCallTO(null, "ID3", null, null, null, null, null, null, null, null, null, null, null));
        // Compare
        rs = (MySQLExpressionCallTOResultSet) dao.getExpressionCalls(params);
        expressions = rs.getAllTOs();
        assertTrue("ExpressionCallTOs incorrectly retrieved", 
                TOComparator.areTOCollectionsEqual(expectedExprCalls, expressions));
        assertFalse("Incorrect filtering of duplicates", rs.isFilterDuplicates());
        assertFalse("Incorrect use of the LIMIT feature", rs.isUsingLimitFeature());

        // Test get only GENEID without species filter and without including substructures
        dao.clearAttributes();
        dao.setAttributes(Arrays.asList(ExpressionCallDAO.Attribute.INCLUDE_SUBSTRUCTURES));
        expectedExprCalls = Arrays.asList(
                new ExpressionCallTO(null, null, null, null, null, null, null, null, false, null, null, null, null));
        // Compare
        rs = (MySQLExpressionCallTOResultSet) dao.getExpressionCalls(params);
        expressions = rs.getAllTOs();
        assertTrue("ExpressionCallTOs incorrectly retrieved", 
                TOComparator.areTOCollectionsEqual(expectedExprCalls, expressions));
        assertFalse("Incorrect filtering of duplicates", rs.isFilterDuplicates());
        assertFalse("Incorrect use of the LIMIT feature", rs.isUsingLimitFeature());

        // Test get only ID without species filter and including substructures
        dao.clearAttributes();
        dao.setAttributes(Arrays.asList(ExpressionCallDAO.Attribute.ID));
        params.setIncludeSubstructures(true);
        expectedExprCalls = Arrays.asList(
                new ExpressionCallTO("1", null, null, null, null, null, null, null, null, null, null, null, null),
                new ExpressionCallTO("2", null, null, null, null, null, null, null, null, null, null, null, null),
                new ExpressionCallTO("3", null, null, null, null, null, null, null, null, null, null, null, null),
                new ExpressionCallTO("4", null, null, null, null, null, null, null, null, null, null, null, null),
                new ExpressionCallTO("5", null, null, null, null, null, null, null, null, null, null, null, null),
                new ExpressionCallTO("6", null, null, null, null, null, null, null, null, null, null, null, null),
                new ExpressionCallTO("7", null, null, null, null, null, null, null, null, null, null, null, null),
                new ExpressionCallTO("8", null, null, null, null, null, null, null, null, null, null, null, null),
                new ExpressionCallTO("9", null, null, null, null, null, null, null, null, null, null, null, null),
                new ExpressionCallTO("10", null, null, null, null, null, null, null, null, null, null, null, null),
                new ExpressionCallTO("11", null, null, null, null, null, null, null, null, null, null, null, null),
                new ExpressionCallTO("12", null, null, null, null, null, null, null, null, null, null, null, null),
                new ExpressionCallTO("13", null, null, null, null, null, null, null, null, null, null, null, null),
                new ExpressionCallTO("14", null, null, null, null, null, null, null, null, null, null, null, null),
                new ExpressionCallTO("15", null, null, null, null, null, null, null, null, null, null, null, null),
                new ExpressionCallTO("16", null, null, null, null, null, null, null, null, null, null, null, null),
                new ExpressionCallTO("17", null, null, null, null, null, null, null, null, null, null, null, null),
                new ExpressionCallTO("18", null, null, null, null, null, null, null, null, null, null, null, null),
                new ExpressionCallTO("19", null, null, null, null, null, null, null, null, null, null, null, null),
                new ExpressionCallTO("20", null, null, null, null, null, null, null, null, null, null, null, null),
                new ExpressionCallTO("21", null, null, null, null, null, null, null, null, null, null, null, null),
                new ExpressionCallTO("22", null, null, null, null, null, null, null, null, null, null, null, null), 
                new ExpressionCallTO("23", null, null, null, null, null, null, null, null, null, null, null, null), 
                new ExpressionCallTO("24", null, null, null, null, null, null, null, null, null, null, null, null), 
                new ExpressionCallTO("25", null, null, null, null, null, null, null, null, null, null, null, null), 
                new ExpressionCallTO("26", null, null, null, null, null, null, null, null, null, null, null, null));
        // Compare
        rs = (MySQLExpressionCallTOResultSet) dao.getExpressionCalls(params);
        expressions = rs.getAllTOs();
        assertTrue("ExpressionCallTOs incorrectly retrieved", 
                TOComparator.areTOCollectionsEqual(expectedExprCalls, expressions));
        assertFalse("Incorrect filtering of duplicates", rs.isFilterDuplicates());
        assertFalse("Incorrect use of the LIMIT feature", rs.isUsingLimitFeature());
        
        // Test get INCLUDESUBSTRUCTURES (and STAGEID) without OriginOfLine including substructures
        dao.clearAttributes();
        dao.setAttributes(
                ExpressionCallDAO.Attribute.STAGE_ID, ExpressionCallDAO.Attribute.INCLUDE_SUBSTRUCTURES);
        expectedExprCalls = Arrays.asList(
                new ExpressionCallTO(null, null, null, "Stage_id1", 
                        null, null, null, null, true, null, null, null, null),
                new ExpressionCallTO(null, null, null, "Stage_id2", 
                        null, null, null, null, true, null, null, null, null),
                new ExpressionCallTO(null, null, null, "Stage_id6", 
                        null, null, null, null, true, null, null, null, null),
                new ExpressionCallTO(null, null, null, "Stage_id7", 
                        null, null, null, null, true, null, null, null, null),
                new ExpressionCallTO(null, null, null, "Stage_id8", 
                        null, null, null, null, true, null, null, null, null),
                new ExpressionCallTO(null, null, null, "Stage_id18",
                        null, null, null, null, true, null, null, null, null),
                new ExpressionCallTO(null, null, null, "Stage_id10", 
                        null, null, null, null, true, null, null, null, null),
                new ExpressionCallTO(null, null, null, "Stage_id12",
                        null, null, null, null, true, null, null, null, null),
                new ExpressionCallTO(null, null, null, "Stage_id13",
                        null, null, null, null, true, null, null, null, null));
        // Compare
        rs = (MySQLExpressionCallTOResultSet) dao.getExpressionCalls(params);
        expressions = rs.getAllTOs();
        assertTrue("ExpressionCallTOs incorrectly retrieved", 
                TOComparator.areTOCollectionsEqual(expectedExprCalls, expressions));
        assertFalse("Incorrect filtering of duplicates", rs.isFilterDuplicates());
        assertFalse("Incorrect use of the LIMIT feature", rs.isUsingLimitFeature());
    }

    /**
     * Test the select method {@link MySQLExpressionCallDAO#getExpressionCalls()} 
     * when including data from sub-stages.
     */
    @Test
    public void shouldGetExpressionCallsIncludeSubStages() throws SQLException {
        
        this.useSelectDB();

        //to test the LIMIT feature used when propagating expression calls on-the-fly, 
        //we change the gene count limit from the properties.
        Properties newProps = DAOManager.getDefaultProperties();
        newProps.setProperty(MySQLDAOManager.EXPR_PROPAGATION_GENE_COUNT_KEY, "2");
        try {
        MySQLDAOManager manager = this.getMySQLDAOManager(newProps);
        
        MySQLExpressionCallDAO dao = new MySQLExpressionCallDAO(manager);

        // On expression table 
        // Without speciesIds and not include organ substructures
        // Generate parameters
        Set<String> speciesIds = new HashSet<String>();
        ExpressionCallParams params = new ExpressionCallParams();
        params.addAllSpeciesIds(speciesIds);
        params.setIncludeSubstructures(false);
        params.setIncludeSubStages(true);
        // Generate manually expected result
        List<ExpressionCallTO> expectedExprCalls = Arrays.asList(
                new ExpressionCallTO("ID1__Anat_id1__Stage_id1", "ID1", "Anat_id1", "Stage_id1",
                        DataState.NODATA, DataState.NODATA, DataState.LOWQUALITY, 
                        DataState.NODATA, false, true, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("ID1__Anat_id6__Stage_id1", "ID1", "Anat_id6", "Stage_id1",
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.LOWQUALITY, false, true, OriginOfLine.SELF, OriginOfLine.DESCENT, false),
                new ExpressionCallTO("ID1__Anat_id6__Stage_id5", "ID1", "Anat_id6", "Stage_id5", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.LOWQUALITY, false, true, OriginOfLine.SELF, OriginOfLine.DESCENT, false),
                new ExpressionCallTO("ID1__Anat_id6__Stage_id6", "ID1", "Anat_id6", "Stage_id6", 
                        DataState.LOWQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.LOWQUALITY, false, true, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("ID1__Anat_id6__Stage_id7", "ID1", "Anat_id6", "Stage_id7", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.NODATA, 
                        DataState.LOWQUALITY, false, true, OriginOfLine.SELF, OriginOfLine.BOTH, true),
                new ExpressionCallTO("ID1__Anat_id6__Stage_id8", "ID1", "Anat_id6", "Stage_id8", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.NODATA, 
                        DataState.NODATA, false, true, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("ID1__Anat_id7__Stage_id1", "ID1", "Anat_id7", "Stage_id1", 
                        DataState.LOWQUALITY, DataState.LOWQUALITY, DataState.LOWQUALITY, 
                        DataState.LOWQUALITY, false, true, OriginOfLine.SELF, OriginOfLine.DESCENT, false),
                new ExpressionCallTO("ID1__Anat_id7__Stage_id10", "ID1", "Anat_id7", "Stage_id10", 
                        DataState.LOWQUALITY, DataState.LOWQUALITY, DataState.LOWQUALITY, 
                        DataState.LOWQUALITY, false, true, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("ID2__Anat_id1__Stage_id1", "ID2", "Anat_id1", "Stage_id1", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.LOWQUALITY, 
                        DataState.HIGHQUALITY, false, true, OriginOfLine.SELF, OriginOfLine.DESCENT, false), 
                new ExpressionCallTO("ID2__Anat_id1__Stage_id2", "ID2", "Anat_id1", "Stage_id2", 
                        DataState.NODATA, DataState.HIGHQUALITY, DataState.LOWQUALITY, 
                        DataState.NODATA, false, true, OriginOfLine.SELF, OriginOfLine.SELF, true), 
                new ExpressionCallTO("ID2__Anat_id1__Stage_id5", "ID2", "Anat_id1", "Stage_id5", 
                        DataState.HIGHQUALITY, DataState.LOWQUALITY, DataState.NODATA, 
                        DataState.HIGHQUALITY, false, true, OriginOfLine.SELF, OriginOfLine.DESCENT, false), 
                new ExpressionCallTO("ID2__Anat_id1__Stage_id7", "ID2", "Anat_id1", "Stage_id7", 
                        DataState.HIGHQUALITY, DataState.LOWQUALITY, DataState.NODATA, 
                        DataState.HIGHQUALITY, false, true, OriginOfLine.SELF, OriginOfLine.DESCENT, false), 
                new ExpressionCallTO("ID2__Anat_id1__Stage_id9", "ID2", "Anat_id1", "Stage_id9", 
                        DataState.HIGHQUALITY, DataState.LOWQUALITY, DataState.NODATA, 
                        DataState.HIGHQUALITY, false, true, OriginOfLine.SELF, OriginOfLine.SELF, true), 
                new ExpressionCallTO("ID2__Anat_id2__Stage_id1", "ID2", "Anat_id2", "Stage_id1", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, false, true, OriginOfLine.SELF, OriginOfLine.DESCENT, false),
                new ExpressionCallTO("ID2__Anat_id2__Stage_id14", "ID2", "Anat_id2", "Stage_id14", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, false, true, OriginOfLine.SELF, OriginOfLine.DESCENT, false),
                new ExpressionCallTO("ID2__Anat_id2__Stage_id18", "ID2", "Anat_id2", "Stage_id18", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, false, true, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("ID2__Anat_id11__Stage_id1", "ID2", "Anat_id11", "Stage_id1", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.LOWQUALITY, 
                        DataState.HIGHQUALITY, false, true, OriginOfLine.SELF, OriginOfLine.DESCENT, false),
                new ExpressionCallTO("ID2__Anat_id11__Stage_id10", "ID2", "Anat_id11", "Stage_id10", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.LOWQUALITY, 
                        DataState.HIGHQUALITY, false, true, OriginOfLine.SELF, OriginOfLine.DESCENT, false),
                new ExpressionCallTO("ID2__Anat_id11__Stage_id12", "ID2", "Anat_id11", "Stage_id12", 
                        DataState.HIGHQUALITY, DataState.LOWQUALITY, DataState.NODATA, 
                        DataState.HIGHQUALITY, false, true, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("ID2__Anat_id11__Stage_id13", "ID2", "Anat_id11", "Stage_id13", 
                        DataState.NODATA, DataState.HIGHQUALITY, DataState.LOWQUALITY, 
                        DataState.NODATA, false, true, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("ID3__Anat_id1__Stage_id1", "ID3", "Anat_id1", "Stage_id1", 
                        DataState.LOWQUALITY, DataState.NODATA, DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, false, true, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("ID3__Anat_id3__Stage_id1", "ID3", "Anat_id3", "Stage_id1", 
                        DataState.NODATA, DataState.HIGHQUALITY, DataState.LOWQUALITY, 
                        DataState.NODATA, false, true, OriginOfLine.SELF, OriginOfLine.SELF, true));
        // Compare
        MySQLExpressionCallTOResultSet rs = 
                (MySQLExpressionCallTOResultSet) dao.getExpressionCalls(params);
        List<ExpressionCallTO> expressions = rs.getAllTOs();
        assertTrue("ExpressionCallTOs incorrectly retrieved, expected " + expectedExprCalls + 
                ", but was: " + expressions, 
                TOComparator.areTOCollectionsEqual(expectedExprCalls, expressions));
        assertFalse("Incorrect filtering of duplicates", rs.isFilterDuplicates());
        assertTrue("Incorrect use of the LIMIT feature", rs.isUsingLimitFeature());

        // With speciesIds but not include substructures 
        // Generate parameters
        params.addAllSpeciesIds(Arrays.asList("11", "41")); // 41 = species Id that does not exist
        // Generate manually expected result
        expectedExprCalls = Arrays.asList(
                new ExpressionCallTO("ID1__Anat_id1__Stage_id1", "ID1", "Anat_id1", "Stage_id1",
                        DataState.NODATA, DataState.NODATA, DataState.LOWQUALITY, 
                        DataState.NODATA, false, true, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("ID1__Anat_id6__Stage_id6","ID1", "Anat_id6", "Stage_id6", 
                        DataState.LOWQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.LOWQUALITY, false, true, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("ID1__Anat_id6__Stage_id5","ID1", "Anat_id6", "Stage_id5", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.LOWQUALITY, false, true, OriginOfLine.SELF, OriginOfLine.DESCENT, false),
                new ExpressionCallTO("ID1__Anat_id6__Stage_id1","ID1", "Anat_id6", "Stage_id1", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.LOWQUALITY, false, true, OriginOfLine.SELF, OriginOfLine.DESCENT, false),
                new ExpressionCallTO("ID1__Anat_id6__Stage_id7","ID1", "Anat_id6", "Stage_id7", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.NODATA, DataState.LOWQUALITY, 
                        false, true, OriginOfLine.SELF, OriginOfLine.BOTH, true),
                new ExpressionCallTO("ID1__Anat_id6__Stage_id8","ID1", "Anat_id6", "Stage_id8", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.NODATA, DataState.NODATA, 
                        false, true, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("ID1__Anat_id7__Stage_id10","ID1", "Anat_id7", "Stage_id10", 
                        DataState.LOWQUALITY, DataState.LOWQUALITY, DataState.LOWQUALITY, 
                        DataState.LOWQUALITY, false, true, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("ID1__Anat_id7__Stage_id1","ID1", "Anat_id7", "Stage_id1", 
                        DataState.LOWQUALITY, DataState.LOWQUALITY, DataState.LOWQUALITY, 
                        DataState.LOWQUALITY, false, true, OriginOfLine.SELF, OriginOfLine.DESCENT, false)); 
        // Compare
        rs = (MySQLExpressionCallTOResultSet) dao.getExpressionCalls(params);
        expressions = rs.getAllTOs();
        assertTrue("ExpressionCallTOs incorrectly retrieved, expected: " + expectedExprCalls + 
                ", but was: " + expressions, 
                TOComparator.areTOCollectionsEqual(expectedExprCalls, expressions));
        assertFalse("Incorrect filtering of duplicates", rs.isFilterDuplicates());
        assertTrue("Incorrect use of the LIMIT feature", rs.isUsingLimitFeature());

        // On global expression table 
        // With speciesIds and include substructures 
        // Generate parameters
        params.setIncludeSubstructures(true);
        params.clearSpeciesIds();
        params.addAllSpeciesIds(Arrays.asList("11", "21", "41")); // 41 = species Id that does not exist
        // Generate manually expected result
        expectedExprCalls = Arrays.asList(
                new ExpressionCallTO("ID1__Anat_id6__Stage_id6", "ID1", "Anat_id6", "Stage_id6", 
                        DataState.LOWQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.LOWQUALITY, true, true, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("ID1__Anat_id6__Stage_id5", "ID1", "Anat_id6", "Stage_id5", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.LOWQUALITY, true, true, OriginOfLine.SELF, OriginOfLine.DESCENT, false),
                new ExpressionCallTO("ID1__Anat_id6__Stage_id1", "ID1", "Anat_id6", "Stage_id1", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.LOWQUALITY, true, true, OriginOfLine.SELF, OriginOfLine.DESCENT, false),
                new ExpressionCallTO("ID1__Anat_id6__Stage_id7", "ID1", "Anat_id6", "Stage_id7", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.NODATA, DataState.LOWQUALITY, 
                        true, true, OriginOfLine.SELF, OriginOfLine.BOTH, true),
                new ExpressionCallTO("ID1__Anat_id6__Stage_id8", "ID1", "Anat_id6", "Stage_id8", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.NODATA, DataState.NODATA, 
                        true, true, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("ID1__Anat_id1__Stage_id8", "ID1", "Anat_id1", "Stage_id8", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.NODATA, DataState.NODATA, 
                        true, true, OriginOfLine.DESCENT, OriginOfLine.SELF, false),
                new ExpressionCallTO("ID1__Anat_id1__Stage_id7", "ID1", "Anat_id1", "Stage_id7", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.NODATA, DataState.NODATA, 
                        true, true, OriginOfLine.DESCENT, OriginOfLine.DESCENT, false),
                new ExpressionCallTO("ID1__Anat_id1__Stage_id5", "ID1", "Anat_id1", "Stage_id5", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.NODATA, DataState.NODATA, 
                        true, true, OriginOfLine.DESCENT, OriginOfLine.DESCENT, false),
                new ExpressionCallTO("ID1__Anat_id1__Stage_id1", "ID1", "Anat_id1", "Stage_id1", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.LOWQUALITY, DataState.NODATA, 
                        true, true, OriginOfLine.BOTH, OriginOfLine.BOTH, true),
                new ExpressionCallTO("ID1__Anat_id7__Stage_id10", "ID1", "Anat_id7", "Stage_id10", 
                        DataState.LOWQUALITY, DataState.LOWQUALITY, DataState.LOWQUALITY, 
                        DataState.LOWQUALITY, true, true, OriginOfLine.BOTH, OriginOfLine.SELF, true),
                new ExpressionCallTO("ID1__Anat_id7__Stage_id1", "ID1", "Anat_id7", "Stage_id1", 
                        DataState.LOWQUALITY, DataState.LOWQUALITY, DataState.LOWQUALITY, 
                        DataState.LOWQUALITY, true, true, OriginOfLine.BOTH, OriginOfLine.DESCENT, false),
                new ExpressionCallTO("ID2__Anat_id1__Stage_id1", "ID2", "Anat_id1", "Stage_id1", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.LOWQUALITY, 
                        DataState.HIGHQUALITY, true, true,
                        OriginOfLine.BOTH, OriginOfLine.BOTH, false), 
                new ExpressionCallTO("ID2__Anat_id1__Stage_id2", "ID2", "Anat_id1", "Stage_id2",
                        DataState.NODATA, DataState.HIGHQUALITY, DataState.LOWQUALITY, 
                        DataState.NODATA, true, true, 
                        OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("ID2__Anat_id11__Stage_id1", "ID2", "Anat_id11", "Stage_id1", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, true, true, 
                        OriginOfLine.BOTH, OriginOfLine.DESCENT, false), 
                new ExpressionCallTO("ID2__Anat_id11__Stage_id10", "ID2", "Anat_id11", "Stage_id10", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.LOWQUALITY, 
                        DataState.HIGHQUALITY, true, true, 
                        OriginOfLine.SELF, OriginOfLine.DESCENT, false),
                new ExpressionCallTO("ID2__Anat_id11__Stage_id12", "ID2", "Anat_id11", "Stage_id12",
                        DataState.HIGHQUALITY, DataState.LOWQUALITY, DataState.NODATA, 
                        DataState.HIGHQUALITY, true, true, 
                        OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("ID2__Anat_id11__Stage_id13", "ID2", "Anat_id11", "Stage_id13", 
                        DataState.NODATA, DataState.HIGHQUALITY, DataState.LOWQUALITY, 
                        DataState.NODATA, true, true, 
                        OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("ID2__Anat_id11__Stage_id14", "ID2", "Anat_id11", "Stage_id14", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, true, true, 
                        OriginOfLine.DESCENT, OriginOfLine.DESCENT, false), 
                new ExpressionCallTO("ID2__Anat_id11__Stage_id18", "ID2", "Anat_id11", "Stage_id18",
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, true, true, 
                        OriginOfLine.DESCENT, OriginOfLine.SELF, false), 
                new ExpressionCallTO("ID2__Anat_id2__Stage_id1", "ID2", "Anat_id2", "Stage_id1", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, true, true, 
                        OriginOfLine.SELF, OriginOfLine.DESCENT, false),
                new ExpressionCallTO("ID2__Anat_id2__Stage_id14", "ID2", "Anat_id2", "Stage_id14", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, true, true, 
                        OriginOfLine.SELF, OriginOfLine.DESCENT, false),
                new ExpressionCallTO("ID2__Anat_id2__Stage_id18", "ID2", "Anat_id2", "Stage_id18", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, true, true, 
                        OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("ID2__Anat_id3__Stage_id18", "ID2", "Anat_id3", "Stage_id18", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY,
                        DataState.HIGHQUALITY, true, true, 
                        OriginOfLine.DESCENT, OriginOfLine.SELF, false),
                new ExpressionCallTO("ID2__Anat_id3__Stage_id14", "ID2", "Anat_id3", "Stage_id14", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, true, true,
                        OriginOfLine.DESCENT, OriginOfLine.DESCENT, false),
                new ExpressionCallTO("ID2__Anat_id3__Stage_id1", "ID2", "Anat_id3", "Stage_id1", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, true, true, 
                        OriginOfLine.DESCENT, OriginOfLine.DESCENT, false),
                new ExpressionCallTO("ID2__Anat_id4__Stage_id18", "ID2", "Anat_id4", "Stage_id18", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY,
                        DataState.HIGHQUALITY, true, true, 
                        OriginOfLine.DESCENT, OriginOfLine.SELF, false),
                new ExpressionCallTO("ID2__Anat_id4__Stage_id14", "ID2", "Anat_id4", "Stage_id14", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, true, true, 
                        OriginOfLine.DESCENT, OriginOfLine.DESCENT, false),
                new ExpressionCallTO("ID2__Anat_id4__Stage_id1", "ID2", "Anat_id4", "Stage_id1", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, true, true,
                        OriginOfLine.DESCENT, OriginOfLine.DESCENT, false),
                new ExpressionCallTO("ID2__Anat_id5__Stage_id18", "ID2", "Anat_id5", "Stage_id18", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, true, true, 
                        OriginOfLine.DESCENT, OriginOfLine.SELF, false),
                new ExpressionCallTO("ID2__Anat_id5__Stage_id14", "ID2", "Anat_id5", "Stage_id14", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, true, true, 
                        OriginOfLine.DESCENT, OriginOfLine.DESCENT, false),
                new ExpressionCallTO("ID2__Anat_id5__Stage_id1", "ID2", "Anat_id5", "Stage_id1", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY,
                        DataState.HIGHQUALITY, true, true, 
                        OriginOfLine.DESCENT, OriginOfLine.DESCENT, false),
                new ExpressionCallTO("ID2__Anat_id9__Stage_id18", "ID2", "Anat_id9", "Stage_id18", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY,
                        DataState.HIGHQUALITY, true, true, 
                        OriginOfLine.DESCENT, OriginOfLine.SELF, false),
                new ExpressionCallTO("ID2__Anat_id9__Stage_id14", "ID2", "Anat_id9", "Stage_id14", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY,
                        DataState.HIGHQUALITY, true, true, 
                        OriginOfLine.DESCENT, OriginOfLine.DESCENT, false),
                new ExpressionCallTO("ID2__Anat_id9__Stage_id1", "ID2", "Anat_id9", "Stage_id1", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY,
                        DataState.HIGHQUALITY, true, true, 
                        OriginOfLine.DESCENT, OriginOfLine.DESCENT, false),
                new ExpressionCallTO("ID2__Anat_id10__Stage_id18", "ID2", "Anat_id10", "Stage_id18", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, true, true, 
                        OriginOfLine.DESCENT, OriginOfLine.SELF, false),
                new ExpressionCallTO("ID2__Anat_id10__Stage_id14", "ID2", "Anat_id10", "Stage_id14", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY,
                        DataState.HIGHQUALITY, true, true, 
                        OriginOfLine.DESCENT, OriginOfLine.DESCENT, false),
                new ExpressionCallTO("ID2__Anat_id10__Stage_id1", "ID2", "Anat_id10", "Stage_id1", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.HIGHQUALITY, true, true, 
                        OriginOfLine.DESCENT, OriginOfLine.DESCENT, false));
        // Compare
        rs = (MySQLExpressionCallTOResultSet) dao.getExpressionCalls(params);
        expressions = rs.getAllTOs();
        assertTrue("ExpressionCallTOs incorrectly retrieved, expected: " + expectedExprCalls + 
                ", but was: " + expressions, 
                TOComparator.areTOCollectionsEqual(expectedExprCalls, expressions));
        assertFalse("Incorrect filtering of duplicates", rs.isFilterDuplicates());
        assertTrue("Incorrect use of the LIMIT feature", rs.isUsingLimitFeature());
        
        // Test get only GENEID without species filter and without including substructures, 
        // but with including sub-stages
        dao.clearAttributes();
        dao.setAttributes(Arrays.asList(ExpressionCallDAO.Attribute.GENE_ID));
        params.clearSpeciesIds();
        params.setIncludeSubstructures(false);
        params.setIncludeSubStages(true);
        expectedExprCalls = Arrays.asList(
                new ExpressionCallTO(null, "ID1", null, null, null, null, null, null, null, null, null, null, null),
                new ExpressionCallTO(null, "ID2", null, null, null, null, null, null, null, null, null, null, null),
                new ExpressionCallTO(null, "ID3", null, null, null, null, null, null, null, null, null, null, null));
        // Compare
        rs = (MySQLExpressionCallTOResultSet) dao.getExpressionCalls(params);
        expressions = rs.getAllTOs();
        assertTrue("ExpressionCallTOs incorrectly retrieved", 
                TOComparator.areTOCollectionsEqual(expectedExprCalls, expressions));
        assertFalse("Incorrect filtering of duplicates", rs.isFilterDuplicates());
        assertTrue("Incorrect use of the LIMIT feature", rs.isUsingLimitFeature());

        // Test get only INCLUDE_SUBSTRUCTURES without species filter and without including substructures, 
        // but with including sub-stages
        dao.clearAttributes();
        dao.setAttributes(Arrays.asList(ExpressionCallDAO.Attribute.INCLUDE_SUBSTRUCTURES));
        expectedExprCalls = Arrays.asList(
                new ExpressionCallTO(null, null, null, null, null, null, null, null, false, null, null, null, null));
        // Compare
        rs = (MySQLExpressionCallTOResultSet) dao.getExpressionCalls(params);
        expressions = rs.getAllTOs();
        assertTrue("ExpressionCallTOs incorrectly retrieved", 
                TOComparator.areTOCollectionsEqual(expectedExprCalls, expressions));
        assertTrue("Incorrect filtering of duplicates", rs.isFilterDuplicates());
        assertTrue("Incorrect use of the LIMIT feature", rs.isUsingLimitFeature());
        
       // Test get only INCLUDE_SUBSTAGES without species filter and without including substructures, 
        // but with including sub-stages
        dao.clearAttributes();
        dao.setAttributes(Arrays.asList(ExpressionCallDAO.Attribute.INCLUDE_SUBSTAGES));
        expectedExprCalls = Arrays.asList(
                new ExpressionCallTO(null, null, null, null, null, null, null, null, null, true, null, null, null));
        // Compare
        rs = (MySQLExpressionCallTOResultSet) dao.getExpressionCalls(params);
        expressions = rs.getAllTOs();
        assertTrue("ExpressionCallTOs incorrectly retrieved", 
                TOComparator.areTOCollectionsEqual(expectedExprCalls, expressions));
        assertTrue("Incorrect filtering of duplicates", rs.isFilterDuplicates());
        assertTrue("Incorrect use of the LIMIT feature", rs.isUsingLimitFeature());

        // Test get only ID with species filter and including substructures and sub-stages
        dao.clearAttributes();
        dao.setAttributes(Arrays.asList(ExpressionCallDAO.Attribute.ID));
        params = new ExpressionCallParams();
        params.addAllSpeciesIds(Arrays.asList("11", "41")); // 41 = species Id that does not exist
        params.setIncludeSubstructures(true);
        params.setIncludeSubStages(true);
        expectedExprCalls = Arrays.asList(
                new ExpressionCallTO("ID1__Anat_id6__Stage_id1", null, null, null, null, null, 
                        null, null, null, null, null, null, null),
                new ExpressionCallTO("ID1__Anat_id6__Stage_id5", null, null, null, null, null, 
                        null, null, null, null, null, null, null),
                new ExpressionCallTO("ID1__Anat_id6__Stage_id6", null, null, null, null, null, 
                        null, null, null, null, null, null, null),
                new ExpressionCallTO("ID1__Anat_id6__Stage_id7", null, null, null, null, null, 
                        null, null, null, null, null, null, null),
                new ExpressionCallTO("ID1__Anat_id6__Stage_id8", null, null, null, null, null, 
                        null, null, null, null, null, null, null),
                new ExpressionCallTO("ID1__Anat_id1__Stage_id1", null, null, null, null, null, 
                        null, null, null, null, null, null, null),
                new ExpressionCallTO("ID1__Anat_id1__Stage_id5", null, null, null, null, null, 
                        null, null, null, null, null, null, null),
                new ExpressionCallTO("ID1__Anat_id1__Stage_id7", null, null, null, null, null, 
                        null, null, null, null, null, null, null),
                new ExpressionCallTO("ID1__Anat_id1__Stage_id8", null, null, null, null, null, 
                        null, null, null, null, null, null, null),
                new ExpressionCallTO("ID1__Anat_id7__Stage_id1", null, null, null, null, null, 
                        null, null, null, null, null, null, null),
                new ExpressionCallTO("ID1__Anat_id7__Stage_id10", null, null, null, null, null, 
                        null, null, null, null, null, null, null));
        // Compare
        rs = (MySQLExpressionCallTOResultSet) dao.getExpressionCalls(params);
        expressions = rs.getAllTOs();
        assertTrue("ExpressionCallTOs incorrectly retrieved, expected: " + expectedExprCalls + 
                ", but was: " + expressions, 
                TOComparator.areTOCollectionsEqual(expectedExprCalls, expressions));
        assertFalse("Incorrect filtering of duplicates", rs.isFilterDuplicates());
        assertTrue("Incorrect use of the LIMIT feature", rs.isUsingLimitFeature());
        
        // Test get INCLUDE_SUBSTAGES (and STAGEID) without OriginOfLine including sub-stages
        dao.clearAttributes();
        dao.setAttributes(
                ExpressionCallDAO.Attribute.STAGE_ID, ExpressionCallDAO.Attribute.INCLUDE_SUBSTAGES);
        expectedExprCalls = Arrays.asList(
                new ExpressionCallTO(null, null, null, "Stage_id1", 
                null, null, null, null, null, true, null, null, null),
                new ExpressionCallTO(null, null, null, "Stage_id5", 
                        null, null, null, null, null, true, null, null, null),
                new ExpressionCallTO(null, null, null, "Stage_id6", 
                        null, null, null, null, null, true, null, null, null),
                new ExpressionCallTO(null, null, null, "Stage_id7", 
                        null, null, null, null, null, true, null, null, null),
                new ExpressionCallTO(null, null, null, "Stage_id8",
                        null, null, null, null, null, true, null, null, null),
                new ExpressionCallTO(null, null, null, "Stage_id10", 
                        null, null, null, null, null, true, null, null, null));
        // Compare
        rs = (MySQLExpressionCallTOResultSet) dao.getExpressionCalls(params);
        expressions = rs.getAllTOs();
        assertTrue("ExpressionCallTOs incorrectly retrieved", 
                TOComparator.areTOCollectionsEqual(expectedExprCalls, expressions));
        assertTrue("Incorrect filtering of duplicates", rs.isFilterDuplicates());
        assertTrue("Incorrect use of the LIMIT feature", rs.isUsingLimitFeature());
        } finally {
            //restore default parameters
            this.getMySQLDAOManager(DAOManager.getDefaultProperties());
        }
    }
    
    /**
     * Test the get max method {@link MySQLExpressionCallDAO#getMaxExpressionCallId()}.
     */
    @Test
    public void shouldGetMaxExpressionCallId() throws SQLException {

        // Check on database with calls
        this.useSelectDB();

        MySQLExpressionCallDAO dao = new MySQLExpressionCallDAO(this.getMySQLDAOManager());

        // Generate manually expected result for expression table
        assertEquals("Max expression ID incorrectly retrieved", 12, 
                dao.getMaxExpressionCallId(false));

        // Generate manually expected result for global expression table
        assertEquals("Max expression ID incorrectly retrieved", 26, 
                dao.getMaxExpressionCallId(true));

        // Check on database without calls
        this.useEmptyDB();
        
        try {
            // Generate manually expected result for expression table
            assertEquals("Max expression ID incorrectly retrieved", 0, 
                    dao.getMaxExpressionCallId(false));

            // Generate manually expected result for global expression table
            assertEquals("Max expression ID incorrectly retrieved", 0, 
                    dao.getMaxExpressionCallId(true));
        } finally {
            this.emptyAndUseDefaultDB();
        }
    }

    /**
     * Test the select method {@link MySQLExpressionCallDAO#insertExpressionCalls()}.
     */
    @Test
    public void shouldInsertExpressionCalls() throws SQLException {
        
        this.useEmptyDB();
        //create a Collection of ExpressionCallTO to be inserted
        Collection<ExpressionCallTO> exprCallTOs = Arrays.asList(
                new ExpressionCallTO("1", "ID3", "Anat_id1", "Stage_id1", DataState.LOWQUALITY,
                        DataState.NODATA, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        false, false, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("7", "ID2", "Anat_id11", "Stage_id13", DataState.NODATA,
                        DataState.HIGHQUALITY, DataState.LOWQUALITY, DataState.NODATA, 
                        false, false, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("16", "ID2", "Anat_id3", "Stage_id18", DataState.HIGHQUALITY,
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        false, false, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("19", "ID2", "Anat_id10", "Stage_id18", DataState.HIGHQUALITY,
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        true, false, OriginOfLine.SELF, OriginOfLine.SELF, true),
                new ExpressionCallTO("20", "ID2", "Anat_id8", "Stage_id18", DataState.NODATA, 
                        DataState.LOWQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        true, false, OriginOfLine.BOTH, OriginOfLine.SELF, false),
                new ExpressionCallTO("21", "ID2", "Anat_id11", "Stage_id18", DataState.LOWQUALITY, 
                        DataState.NODATA, DataState.HIGHQUALITY, DataState.LOWQUALITY, 
                        true, false, OriginOfLine.DESCENT, OriginOfLine.SELF, false));

        try {
            MySQLExpressionCallDAO dao = new MySQLExpressionCallDAO(this.getMySQLDAOManager());
            assertEquals("Incorrect number of rows inserted", 6, 
                    dao.insertExpressionCalls(exprCallTOs));
            
            //we manually verify the insertion, as we do not want to rely on other methods 
            //that are tested elsewhere.
            try (BgeePreparedStatement stmt = this.getMySQLDAOManager().getConnection().
                    prepareStatement("select 1 from expression where " +
                      "expressionId = ? and geneId = ? and anatEntityId = ? and stageId = ? " +
                      "and estData = ? and affymetrixData = ? and inSituData = ? and rnaSeqData = ?")) {
                
                stmt.setString(1, "1");
                stmt.setString(2, "ID3");
                stmt.setString(3, "Anat_id1");
                stmt.setString(4, "Stage_id1");
                stmt.setString(5, "no data");
                stmt.setString(6, "poor quality");
                stmt.setString(7, "high quality");
                stmt.setString(8, "high quality");
                assertTrue("ExpressionCallTO incorrectly inserted", 
                        stmt.getRealPreparedStatement().executeQuery().next());
                
                stmt.setString(1, "7");
                stmt.setString(2, "ID2");
                stmt.setString(3, "Anat_id11");
                stmt.setString(4, "Stage_id13");
                stmt.setString(5, "high quality");
                stmt.setString(6, "no data");
                stmt.setString(7, "poor quality");
                stmt.setString(8, "no data");
                assertTrue("ExpressionCallTO incorrectly inserted", 
                        stmt.getRealPreparedStatement().executeQuery().next());
                
                stmt.setString(1, "16");
                stmt.setString(2, "ID2");
                stmt.setString(3, "Anat_id3");
                stmt.setString(4, "Stage_id18");
                stmt.setString(5, "high quality");
                stmt.setString(6, "high quality");
                stmt.setString(7, "high quality");
                stmt.setString(8, "high quality");
                assertTrue("ExpressionCallTO incorrectly inserted", 
                        stmt.getRealPreparedStatement().executeQuery().next());
            }

            try (BgeePreparedStatement stmt = this.getMySQLDAOManager().getConnection().
                    prepareStatement("select 1 from globalExpression where " +
                      "globalExpressionId = ? and geneId = ? and anatEntityId = ? and stageId = ? " +
                      "and estData = ? and affymetrixData = ? and inSituData = ? and rnaSeqData = ? " +
                      "and originOfLine = ?")) {
                stmt.setString(1, "19");
                stmt.setString(2, "ID2");
                stmt.setString(3, "Anat_id10");
                stmt.setString(4, "Stage_id18");
                stmt.setString(5, "high quality");
                stmt.setString(6, "high quality");
                stmt.setString(7, "high quality");
                stmt.setString(8, "high quality");
                stmt.setString(9, "self");
                assertTrue("ExpressionCallTO incorrectly inserted", 
                        stmt.getRealPreparedStatement().executeQuery().next());

                stmt.setString(1, "20");
                stmt.setString(2, "ID2");
                stmt.setString(3, "Anat_id8");
                stmt.setString(4, "Stage_id18");
                stmt.setString(5, "poor quality");
                stmt.setString(6, "no data");
                stmt.setString(7, "high quality");
                stmt.setString(8, "high quality");
                stmt.setString(9, "both");
                assertTrue("ExpressionCallTO incorrectly inserted", 
                        stmt.getRealPreparedStatement().executeQuery().next());

                stmt.setString(1, "21");
                stmt.setString(2, "ID2");
                stmt.setString(3, "Anat_id11");
                stmt.setString(4, "Stage_id18");
                stmt.setString(5, "no data");
                stmt.setString(6, "poor quality");
                stmt.setString(7, "high quality");
                stmt.setString(8, "poor quality");
                stmt.setString(9, "descent");
                assertTrue("ExpressionCallTO incorrectly inserted", 
                        stmt.getRealPreparedStatement().executeQuery().next());
            }
            
            this.thrown.expect(IllegalArgumentException.class);
            dao.insertExpressionCalls(new HashSet<ExpressionCallTO>());
        } finally {
            this.emptyAndUseDefaultDB();
        }
    }   

    /**
     * Test the select method {@link MySQLExpressionCallDAO#insertGlobalExpressionToExpression()}.
     */
    @Test
    public void shouldInsertGlobalExpressionToExpression() throws SQLException {
        
        this.useEmptyDB();

        //create a Collection of ExpressionCallTO to be inserted
        Collection<GlobalExpressionToExpressionTO> globalExprToExprTOs = Arrays.asList(
                new GlobalExpressionToExpressionTO("1","10"),
                new GlobalExpressionToExpressionTO("1","1"),
                new GlobalExpressionToExpressionTO("2","14"));

        try {
            MySQLExpressionCallDAO dao = new MySQLExpressionCallDAO(this.getMySQLDAOManager());
            assertEquals("Incorrect number of rows inserted", 3, 
                    dao.insertGlobalExpressionToExpression(globalExprToExprTOs));
            
            //we manually verify the insertion, as we do not want to rely on other methods 
            //that are tested elsewhere.
            try (BgeePreparedStatement stmt = this.getMySQLDAOManager().getConnection().
                    prepareStatement("select 1 from globalExpressionToExpression where " +
                      "expressionId = ? and globalExpressionId = ?")) {
                
                stmt.setString(1, "1");
                stmt.setString(2, "10");
                assertTrue("GlobalExpressionToExpressionTO incorrectly inserted", 
                        stmt.getRealPreparedStatement().executeQuery().next());
                
                stmt.setString(1, "1");
                stmt.setString(2, "1");
                assertTrue("GlobalExpressionToExpressionTO incorrectly inserted", 
                        stmt.getRealPreparedStatement().executeQuery().next());
                
                stmt.setString(1, "2");
                stmt.setString(2, "14");
                assertTrue("GlobalExpressionToExpressionTO incorrectly inserted", 
                        stmt.getRealPreparedStatement().executeQuery().next());
            }
            
            this.thrown.expect(IllegalArgumentException.class);
            dao.insertGlobalExpressionToExpression(new HashSet<GlobalExpressionToExpressionTO>());
        } finally {
            this.emptyAndUseDefaultDB();
        }
    }    
}
