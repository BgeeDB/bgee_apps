package org.bgee.model.dao.mysql.expressiondata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.TOComparator;
import org.bgee.model.dao.api.expressiondata.CallDAO.CallTO.DataState;
import org.bgee.model.dao.api.expressiondata.NoExpressionCallDAO;
import org.bgee.model.dao.api.expressiondata.NoExpressionCallDAO.GlobalNoExpressionToNoExpressionTO;
import org.bgee.model.dao.api.expressiondata.NoExpressionCallDAO.NoExpressionCallTO;
import org.bgee.model.dao.api.expressiondata.NoExpressionCallDAO.NoExpressionCallTO.OriginOfLine;
import org.bgee.model.dao.api.expressiondata.NoExpressionCallParams;
import org.bgee.model.dao.mysql.MySQLITAncestor;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;


/**
 * Integration tests for {@link MySQLNoExpressionCallDAO}, performed on a real MySQL database. 
 * See the documentation of {@link org.bgee.model.dao.mysql.MySQLITAncestor} for 
 * important information.
 * 
 * @author Valentine Rech de Laval
 * @version Bgee 13
 * @see org.bgee.model.dao.api.expressiondata.NoExpressionCallDAO
 * @since Bgee 13
 */
public class MySQLNoExpressionCallDAOIT extends MySQLITAncestor {

    private final static Logger log = 
            LogManager.getLogger(MySQLNoExpressionCallDAOIT.class.getName());

    public MySQLNoExpressionCallDAOIT() {
        super();
    }
    
    @Override
    protected Logger getLogger() {
        return log;
    }

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    /**
     * Test the select method {@link MySQLNoExpressionCallDAO#getNoExpressionCalls()}.
     * @throws SQLException 
     */
    @Test
    public void shouldGetNoExpressionCalls() throws SQLException {
        this.useSelectDB();
        
        // On noExpression table 
        MySQLNoExpressionCallDAO dao = new MySQLNoExpressionCallDAO(this.getMySQLDAOManager());
        dao.setAttributes(Arrays.asList(
                NoExpressionCallDAO.Attribute.ID, 
                NoExpressionCallDAO.Attribute.GENEID, 
                NoExpressionCallDAO.Attribute.DEVSTAGEID, 
                NoExpressionCallDAO.Attribute.ANATENTITYID, 
                NoExpressionCallDAO.Attribute.AFFYMETRIXDATA, 
                NoExpressionCallDAO.Attribute.INSITUDATA,
                NoExpressionCallDAO.Attribute.RNASEQDATA
                // Remove INCLUDEPARENTSTRUCTURES because not data from DB
                //NoExpressionCallDAO.Attribute.INCLUDEPARENTSTRUCTURES, 
                // Remove ORIGINOFLINE because we test get no-expression calls on no-expression table
                //NoExpressionCallDAO.Attribute.ORIGINOFLINE
                ));
        
        // Without speciesIds and not include parent structures
        // Generate parameters
        Set<String> speciesIds = new HashSet<String>();
        NoExpressionCallParams params = new NoExpressionCallParams();
        params.addAllSpeciesIds(speciesIds);
        params.setIncludeParentStructures(false);
        // Generate manually expected result
        List<NoExpressionCallTO> expectedNoExprCalls = Arrays.asList(
                new NoExpressionCallTO("1","ID2", "Anat_id5", "Stage_id13", 
                        DataState.LOWQUALITY, DataState.HIGHQUALITY, DataState.NODATA,
                        DataState.HIGHQUALITY, false, OriginOfLine.SELF),
                new NoExpressionCallTO("2","ID1", "Anat_id1", "Stage_id1",
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.NODATA,
                        DataState.LOWQUALITY, false, OriginOfLine.SELF),
                new NoExpressionCallTO("3","ID3", "Anat_id6", "Stage_id6",
                        DataState.NODATA, DataState.NODATA, DataState.NODATA, 
                        DataState.LOWQUALITY, false, OriginOfLine.SELF),
                new NoExpressionCallTO("4","ID2", "Anat_id11", "Stage_id11", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.NODATA, 
                        DataState.HIGHQUALITY, false, OriginOfLine.SELF),
                new NoExpressionCallTO("5","ID3", "Anat_id8", "Stage_id10",
                        DataState.LOWQUALITY, DataState.LOWQUALITY, DataState.NODATA, 
                        DataState.LOWQUALITY, false, OriginOfLine.SELF),
                new NoExpressionCallTO("6","ID3", "Anat_id6", "Stage_id7", 
                        DataState.LOWQUALITY, DataState.NODATA, DataState.NODATA,
                        DataState.HIGHQUALITY, false, OriginOfLine.SELF),
                new NoExpressionCallTO("7","ID3", "Anat_id5", "Stage_id6", 
                        DataState.HIGHQUALITY, DataState.NODATA, DataState.NODATA,
                        DataState.HIGHQUALITY, false, OriginOfLine.SELF),
                new NoExpressionCallTO("8","ID3", "Anat_id5", "Stage_id14", 
                        DataState.LOWQUALITY, DataState.HIGHQUALITY, DataState.NODATA, 
                        DataState.NODATA, false, OriginOfLine.SELF)); 
        // Compare
        assertTrue("NoExpressionCallTOs incorrectly retrieved", 
                TOComparator.areTOCollectionsEqual(expectedNoExprCalls, 
                        dao.getNoExpressionCalls(params).getAllTOs()));

        // With speciesIds but not include parent structures 
        params.addAllSpeciesIds(Arrays.asList("21", "41"));
        //Generate manually expected result
        expectedNoExprCalls = Arrays.asList(
                new NoExpressionCallTO("1","ID2", "Anat_id5", "Stage_id13", 
                        DataState.LOWQUALITY, DataState.HIGHQUALITY, DataState.NODATA,
                        DataState.HIGHQUALITY, false, OriginOfLine.SELF),
                new NoExpressionCallTO("4","ID2", "Anat_id11", "Stage_id11",
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.NODATA,
                        DataState.HIGHQUALITY, false, OriginOfLine.SELF)); 
        // Compare
        assertTrue("NoExpressionCallTOs incorrectly retrieved", 
                TOComparator.areTOCollectionsEqual(expectedNoExprCalls, 
                        dao.getNoExpressionCalls(params).getAllTOs()));

        // On global no-expression table
        dao.setAttributes(Arrays.asList(
                NoExpressionCallDAO.Attribute.ID, 
                NoExpressionCallDAO.Attribute.GENEID, 
                NoExpressionCallDAO.Attribute.DEVSTAGEID, 
                NoExpressionCallDAO.Attribute.ANATENTITYID, 
                NoExpressionCallDAO.Attribute.AFFYMETRIXDATA, 
                NoExpressionCallDAO.Attribute.INSITUDATA,
                NoExpressionCallDAO.Attribute.RNASEQDATA,
                NoExpressionCallDAO.Attribute.ORIGINOFLINE));
        params.setIncludeParentStructures(true);
        
        // With speciesIds and include parent structures 
        // Generate parameters
        expectedNoExprCalls = Arrays.asList(
                new NoExpressionCallTO("1", "ID2", "Anat_id5", "Stage_id13",
                        DataState.LOWQUALITY, DataState.HIGHQUALITY, DataState.NODATA,
                        DataState.HIGHQUALITY, true, OriginOfLine.SELF),
                new NoExpressionCallTO("2", "ID2", "Anat_id2", "Stage_id13", 
                        DataState.LOWQUALITY, DataState.HIGHQUALITY, DataState.NODATA,
                        DataState.HIGHQUALITY, true, OriginOfLine.PARENT),
                new NoExpressionCallTO("3", "ID2", "Anat_id1", "Stage_id13", 
                        DataState.LOWQUALITY, DataState.HIGHQUALITY, DataState.NODATA, 
                        DataState.HIGHQUALITY, true, OriginOfLine.PARENT),
                new NoExpressionCallTO("7", "ID2", "Anat_id11", "Stage_id11",
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.NODATA,
                        DataState.HIGHQUALITY, true, OriginOfLine.SELF),
                new NoExpressionCallTO("8", "ID2", "Anat_id10", "Stage_id11", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.NODATA,
                        DataState.HIGHQUALITY, true, OriginOfLine.PARENT),
                new NoExpressionCallTO("9", "ID2", "Anat_id1", "Stage_id11", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.NODATA, 
                        DataState.HIGHQUALITY, true, OriginOfLine.PARENT));
        // Compare
        assertTrue("NoExpressionCallTOs incorrectly retrieved", 
                TOComparator.areTOCollectionsEqual(expectedNoExprCalls, 
                        dao.getNoExpressionCalls(params).getAllTOs()));

        // Without species filter but include substructures
        // Generate parameters
        params.clearSpeciesIds();
        // Generate manually expected result
        expectedNoExprCalls = Arrays.asList(
                new NoExpressionCallTO("1", "ID2", "Anat_id5", "Stage_id13", 
                        DataState.LOWQUALITY, DataState.HIGHQUALITY, DataState.NODATA, 
                        DataState.HIGHQUALITY, true, OriginOfLine.SELF),
                new NoExpressionCallTO("2", "ID2", "Anat_id2", "Stage_id13",
                        DataState.LOWQUALITY, DataState.HIGHQUALITY, DataState.NODATA, 
                        DataState.HIGHQUALITY, true, OriginOfLine.PARENT),
                new NoExpressionCallTO("3", "ID2", "Anat_id1", "Stage_id13",
                        DataState.LOWQUALITY, DataState.HIGHQUALITY, DataState.NODATA,
                        DataState.HIGHQUALITY, true, OriginOfLine.PARENT),
                new NoExpressionCallTO("4", "ID3", "Anat_id6", "Stage_id6",
                        DataState.NODATA, DataState.NODATA, DataState.NODATA, 
                        DataState.LOWQUALITY, true, OriginOfLine.SELF),
                new NoExpressionCallTO("5", "ID3", "Anat_id5", "Stage_id6",
                        DataState.HIGHQUALITY, DataState.NODATA, DataState.NODATA, 
                        DataState.HIGHQUALITY, true, OriginOfLine.BOTH),
                new NoExpressionCallTO("6", "ID3", "Anat_id1", "Stage_id6", 
                        DataState.NODATA, DataState.NODATA, DataState.NODATA, 
                        DataState.LOWQUALITY, true, OriginOfLine.PARENT),
                new NoExpressionCallTO("7", "ID2", "Anat_id11", "Stage_id11",
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.NODATA, 
                        DataState.HIGHQUALITY, true, OriginOfLine.SELF),
                new NoExpressionCallTO("8", "ID2", "Anat_id10", "Stage_id11", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.NODATA, 
                        DataState.HIGHQUALITY, true, OriginOfLine.PARENT),
                new NoExpressionCallTO("9", "ID2", "Anat_id1", "Stage_id11", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.NODATA, 
                        DataState.HIGHQUALITY, true, OriginOfLine.PARENT),
                new NoExpressionCallTO("10", "ID3", "Anat_id8", "Stage_id10", 
                        DataState.LOWQUALITY, DataState.LOWQUALITY, DataState.NODATA, 
                        DataState.LOWQUALITY, true, OriginOfLine.SELF),
                new NoExpressionCallTO("11", "ID3", "Anat_id7", "Stage_id10", 
                        DataState.LOWQUALITY, DataState.LOWQUALITY, DataState.NODATA,
                        DataState.LOWQUALITY, true, OriginOfLine.PARENT),
                new NoExpressionCallTO("12", "ID3", "Anat_id6", "Stage_id7", 
                        DataState.LOWQUALITY, DataState.NODATA, DataState.NODATA, 
                        DataState.HIGHQUALITY, true, OriginOfLine.SELF),
                new NoExpressionCallTO("13", "ID3", "Anat_id1", "Stage_id7", 
                        DataState.LOWQUALITY, DataState.NODATA, DataState.NODATA, 
                        DataState.HIGHQUALITY, true, OriginOfLine.PARENT));
        // Compare
        assertTrue("NoExpressionCallTOs incorrectly retrieved", 
                TOComparator.areTOCollectionsEqual(expectedNoExprCalls, 
                        dao.getNoExpressionCalls(params).getAllTOs()));
        
        // Test get only ID without species filter and without including substructures
        dao.clearAttributes();
        dao.setAttributes(Arrays.asList(NoExpressionCallDAO.Attribute.ID));
        params.clearSpeciesIds();
        params.setIncludeParentStructures(false);
        expectedNoExprCalls = Arrays.asList(
                new NoExpressionCallTO("1", null, null, null, DataState.NODATA, DataState.NODATA, 
                        DataState.NODATA, DataState.NODATA, false, OriginOfLine.SELF),
                new NoExpressionCallTO("2", null, null, null, DataState.NODATA, DataState.NODATA, 
                        DataState.NODATA, DataState.NODATA, false, OriginOfLine.SELF),
                new NoExpressionCallTO("3", null, null, null, DataState.NODATA, DataState.NODATA, 
                        DataState.NODATA, DataState.NODATA, false, OriginOfLine.SELF),
                new NoExpressionCallTO("4", null, null, null, DataState.NODATA, DataState.NODATA, 
                        DataState.NODATA, DataState.NODATA, false, OriginOfLine.SELF),
                new NoExpressionCallTO("5", null, null, null, DataState.NODATA, DataState.NODATA, 
                        DataState.NODATA, DataState.NODATA, false, OriginOfLine.SELF),
                new NoExpressionCallTO("6", null, null, null, DataState.NODATA, DataState.NODATA, 
                        DataState.NODATA, DataState.NODATA, false, OriginOfLine.SELF),
                new NoExpressionCallTO("7", null, null, null, DataState.NODATA, DataState.NODATA, 
                        DataState.NODATA, DataState.NODATA, false, OriginOfLine.SELF),
                new NoExpressionCallTO("8", null, null, null, DataState.NODATA, DataState.NODATA, 
                        DataState.NODATA, DataState.NODATA, false, OriginOfLine.SELF));
        // Compare
        assertTrue("NoExpressionCallTOs incorrectly retrieved", 
                TOComparator.areTOCollectionsEqual(expectedNoExprCalls, 
                        dao.getNoExpressionCalls(params).getAllTOs()));
    }
    
    /**
     * Test the get max method {@link MySQLNoExpressionCallDAO#getMaxNoExpressionCallID()}.
     */
    @Test
    public void shouldGetMaxNoExpressionCallID() throws SQLException {

        // Check on database with calls
        this.useSelectDB();

        MySQLNoExpressionCallDAO dao = new MySQLNoExpressionCallDAO(this.getMySQLDAOManager());

        // Generate manually expected result for expression table
        int expectedMaxNoExprId = 8;
        int maxNoExprId = dao.getMaxNoExpressionCallId(false);
        assertEquals("Max no-expression ID incorrectly retrieved", expectedMaxNoExprId, maxNoExprId);

        // Generate manually expected result for global expression table
        int expectedMaxGlobalNoExprId = 13;
        int maxGlobalNoExprId = dao.getMaxNoExpressionCallId(true);
        assertEquals("Max global no-expression ID incorrectly retrieved", 
                expectedMaxGlobalNoExprId, maxGlobalNoExprId);
        
        // Check on database without calls
        this.useEmptyDB();
        
        // Generate manually expected result for expression table
        expectedMaxNoExprId = 0;
        maxNoExprId = dao.getMaxNoExpressionCallId(false);
        assertEquals("Max no-expression ID incorrectly retrieved", 
                expectedMaxNoExprId, maxNoExprId);
        
        // Generate manually expected result for global expression table
        expectedMaxGlobalNoExprId = 0;
        maxGlobalNoExprId = dao.getMaxNoExpressionCallId(true);
        assertEquals("Max global no-expression ID incorrectly retrieved", 
                expectedMaxGlobalNoExprId, maxGlobalNoExprId);

        log.exit();
    }

    /**
     * Test the insert method {@link MySQLNoExpressionCallDAO#insertNoExpressionCalls()}.
     * @throws SQLException 
     */
    @Test
    public void shouldInsertNoExpressionCalls() throws SQLException {
        
        this.useEmptyDB();
        
        //create a Collection of NoExpressionCallTO to be inserted
        Collection<NoExpressionCallTO> noExprCallTOs = Arrays.asList(
                new NoExpressionCallTO("1", "ID3", "Anat_id1", "Stage_id1", 
                        DataState.LOWQUALITY, DataState.NODATA, 
                        DataState.NODATA, DataState.HIGHQUALITY, false, OriginOfLine.SELF),
                new NoExpressionCallTO("7", "ID2", "Anat_id11", "Stage_id13", 
                        DataState.NODATA, DataState.HIGHQUALITY, 
                        DataState.NODATA, DataState.LOWQUALITY, false, OriginOfLine.SELF),
                new NoExpressionCallTO("16", "ID2", "Anat_id3", "Stage_id18", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.NODATA, DataState.HIGHQUALITY, true, OriginOfLine.BOTH),
                new NoExpressionCallTO("20", "ID2", "Anat_id10", "Stage_id18", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.NODATA, DataState.HIGHQUALITY, true, OriginOfLine.SELF),
                new NoExpressionCallTO("21", "ID2", "Anat_id11", "Stage_id18", 
                        DataState.LOWQUALITY, DataState.NODATA, 
                        DataState.NODATA, DataState.HIGHQUALITY, true, OriginOfLine.PARENT));

        try {
            MySQLNoExpressionCallDAO dao = new MySQLNoExpressionCallDAO(this.getMySQLDAOManager());
            assertEquals("Incorrect number of rows inserted", 5, 
                    dao.insertNoExpressionCalls(noExprCallTOs));

            //we manually verify the insertion, as we do not want to rely on other methods 
            //that are tested elsewhere.
            try (BgeePreparedStatement stmt = this.getMySQLDAOManager().getConnection().
                    prepareStatement("select 1 from noExpression where " +
                      "noExpressionId = ? and geneId = ? and anatEntityId = ? and stageId = ? " +
                      "and noExpressionAffymetrixData = ? and noExpressionInSituData = ? and " +
                      "noExpressionRnaSeqData = ?")) {
                
                stmt.setString(1, "1");
                stmt.setString(2, "ID3");
                stmt.setString(3, "Anat_id1");
                stmt.setString(4, "Stage_id1");
                stmt.setString(5, "poor quality");
                stmt.setString(6, "no data");
                stmt.setString(7, "high quality");
                assertTrue("NoExpressionCallTO incorrectly inserted", 
                        stmt.getRealPreparedStatement().executeQuery().next());
                
                stmt.setString(1, "7");
                stmt.setString(2, "ID2");
                stmt.setString(3, "Anat_id11");
                stmt.setString(4, "Stage_id13");
                stmt.setString(5, "no data");
                stmt.setString(6, "high quality");
                stmt.setString(7, "poor quality");
                assertTrue("NoExpressionCallTO incorrectly inserted", 
                        stmt.getRealPreparedStatement().executeQuery().next());
            }
            
            try (BgeePreparedStatement stmt = this.getMySQLDAOManager().getConnection().
                    prepareStatement("select 1 from globalNoExpression where " +
                      "globalNoExpressionId = ? and geneId = ? and anatEntityId = ? and stageId = ? " +
                      "and noExpressionAffymetrixData = ? and noExpressionInSituData = ? " +
                      "and noExpressionRnaSeqData = ? and noExpressionOriginOfLine = ?")) {

                stmt.setString(1, "16");
                stmt.setString(2, "ID2");
                stmt.setString(3, "Anat_id3");
                stmt.setString(4, "Stage_id18");
                stmt.setString(5, "high quality");
                stmt.setString(6, "high quality");
                stmt.setString(7, "high quality");
                stmt.setString(8, "both");
                assertTrue("NoExpressionCallTO incorrectly inserted", 
                        stmt.getRealPreparedStatement().executeQuery().next());

                stmt.setString(1, "20");
                stmt.setString(2, "ID2");
                stmt.setString(3, "Anat_id10");
                stmt.setString(4, "Stage_id18");
                stmt.setString(5, "high quality");
                stmt.setString(6, "high quality");
                stmt.setString(7, "high quality");
                stmt.setString(8, "self");
                assertTrue("NoExpressionCallTO incorrectly inserted", 
                        stmt.getRealPreparedStatement().executeQuery().next());

                stmt.setString(1, "21");
                stmt.setString(2, "ID2");
                stmt.setString(3, "Anat_id11");
                stmt.setString(4, "Stage_id18");
                stmt.setString(5, "poor quality");
                stmt.setString(6, "no data");
                stmt.setString(7, "high quality");
                stmt.setString(8, "parent");
                assertTrue("NoExpressionCallTO incorrectly inserted", 
                        stmt.getRealPreparedStatement().executeQuery().next());
            }
        } finally {
            this.emptyAndUseDefaultDB();
        }
    }
    
    /**
     * Test the insert method 
     * {@link MySQLNoExpressionCallDAO#insertGlobalNoExpressionToNoExpression()}.
     * @throws SQLException 
     */
    @Test
    public void shouldInsertGlobalNoExpressionToNoExpression() throws SQLException {
        
        this.useEmptyDB();

        // Create a collection of NoExpressionCallTO to be inserted
        Collection<GlobalNoExpressionToNoExpressionTO> globalNoExprToNoExprTOs = Arrays.asList(
                new GlobalNoExpressionToNoExpressionTO("1","10"),
                new GlobalNoExpressionToNoExpressionTO("1","1"),
                new GlobalNoExpressionToNoExpressionTO("2","14"));

        try {
            MySQLNoExpressionCallDAO dao = new MySQLNoExpressionCallDAO(this.getMySQLDAOManager());
            assertEquals("Incorrect number of rows inserted", 3, 
                    dao.insertGlobalNoExprToNoExpr(globalNoExprToNoExprTOs));
            
            //we manually verify the insertion, as we do not want to rely on other methods 
            //that are tested elsewhere.
            try (BgeePreparedStatement stmt = this.getMySQLDAOManager().getConnection().
                    prepareStatement("select 1 from globalNoExpressionToNoExpression where " +
                      "noExpressionId = ? and globalNoExpressionId = ?")) {
                
                stmt.setString(1, "1");
                stmt.setString(2, "10");
                assertTrue("GlobalNoExpressionToNoExpressionTO incorrectly inserted", 
                        stmt.getRealPreparedStatement().executeQuery().next());
                
                stmt.setString(1, "1");
                stmt.setString(2, "1");
                assertTrue("GlobalNoExpressionToNoExpressionTO incorrectly inserted", 
                        stmt.getRealPreparedStatement().executeQuery().next());
                
                stmt.setString(1, "2");
                stmt.setString(2, "14");
                assertTrue("GlobalNoExpressionToNoExpressionTO incorrectly inserted", 
                        stmt.getRealPreparedStatement().executeQuery().next());
            }
        } finally {
            this.emptyAndUseDefaultDB();
        }
    }
    
    /**
     * Test the delete methods {@link MySQLNoExpressionCallDAO#deleteNoExprCalls()}.
     * @throws SQLException
     */
    @Test
    public void shouldDeleteNoExprCalls() throws SQLException {
        
        this.useEmptyDB();
        this.populateAndUseDatabase();
        
        Set<String> noExprIds = new HashSet<>(Arrays.asList("1", "5", "1111"));
        Set<String> globalNoExprIds = new HashSet<>(Arrays.asList("2", "7", "13", "99"));
        
        try {
            MySQLNoExpressionCallDAO dao = new MySQLNoExpressionCallDAO(this.getMySQLDAOManager());
            assertEquals("Incorrect nummber of rows deleted", 
                    2, dao.deleteNoExprCalls(noExprIds, false));

            try (BgeePreparedStatement stmt = this.getMySQLDAOManager().getConnection().
                    prepareStatement("select 1 from noExpression where noExpressionId = ?")) {
                stmt.setInt(1, 1);
                assertFalse("NoExpressionCallTO incorrectly deleted", 
                        stmt.getRealPreparedStatement().executeQuery().next());

                stmt.setInt(1, 5);
                assertFalse("NoExpressionCallTO incorrectly deleted", 
                        stmt.getRealPreparedStatement().executeQuery().next());
            }
        
            try (BgeePreparedStatement stmt = this.getMySQLDAOManager().getConnection().
                    prepareStatement("select 1 from globalNoExpressionToNoExpression where " +
                            "noExpressionId = ?")) {
                stmt.setInt(1, 1);
                assertFalse("NoExpressionCallTO incorrectly deleted", 
                        stmt.getRealPreparedStatement().executeQuery().next());
                
                stmt.setInt(1, 5);
                assertFalse("NoExpressionCallTO incorrectly deleted", 
                        stmt.getRealPreparedStatement().executeQuery().next());
            }

            assertEquals("Incorrect nummber of rows deleted", 
                    3, dao.deleteNoExprCalls(globalNoExprIds, true));
            
            try (BgeePreparedStatement stmt = this.getMySQLDAOManager().getConnection().
                    prepareStatement("select 1 from globalNoExpression where globalNoExpressionId = ?")) {
                stmt.setInt(1, 2);
                assertFalse("NoExpressionCallTO incorrectly deleted", 
                        stmt.getRealPreparedStatement().executeQuery().next());
                
                stmt.setInt(1, 7);
                assertFalse("NoExpressionCallTO incorrectly deleted", 
                        stmt.getRealPreparedStatement().executeQuery().next());

                stmt.setInt(1, 13);
                assertFalse("NoExpressionCallTO incorrectly deleted", 
                        stmt.getRealPreparedStatement().executeQuery().next());
            }
            
            try (BgeePreparedStatement stmt = this.getMySQLDAOManager().getConnection().
                    prepareStatement("select 1 from globalNoExpressionToNoExpression where " +
                            "globalNoExpressionId = ?")) {
                stmt.setInt(1, 2);
                assertFalse("NoExpressionCallTO incorrectly deleted", 
                        stmt.getRealPreparedStatement().executeQuery().next());
                
                stmt.setInt(1, 7);
                assertFalse("NoExpressionCallTO incorrectly deleted", 
                        stmt.getRealPreparedStatement().executeQuery().next());
                
                stmt.setInt(1, 13);
                assertFalse("NoExpressionCallTO incorrectly deleted", 
                        stmt.getRealPreparedStatement().executeQuery().next());
            }
        } finally {
            this.emptyAndUseDefaultDB();
        }
    }
    
    /**
     * Test the update method {@link MySQLNoExpressionCallDAO#updateNoExprCalls()}.
     * @throws SQLException 
     */
    @Test
    public void shouldUpdateNoExprCalls() throws SQLException {
        
        this.useEmptyDB();
        this.populateAndUseDatabase();

        Collection<NoExpressionCallTO> noExprCallTOs = Arrays.asList(
                // modify stageId, noExpressionInSituData, noExpressionRelaxedInSituData, noExpressionRnaSeqData
                new NoExpressionCallTO("1", "ID2", "Anat_id5", "Stage_id1", 
                        DataState.LOWQUALITY, DataState.NODATA, 
                        DataState.HIGHQUALITY, DataState.LOWQUALITY, false, OriginOfLine.SELF),
                // modify geneId, anatEntityId, noExpressionAffymetrixData
                new NoExpressionCallTO("8", "ID1", "Anat_id2", "Stage_id11", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.NODATA, DataState.NODATA, false, OriginOfLine.SELF),
                        
                // modify geneId, noExpressionAffymetrixData  
                new NoExpressionCallTO("2", "ID1", "Anat_id2", "Stage_id13", 
                        DataState.NODATA, DataState.HIGHQUALITY, 
                        DataState.NODATA, DataState.HIGHQUALITY, true, OriginOfLine.PARENT),
                // modify anatEntityId, noExpressionInSituData, noExpressionOriginOfLine  
                new NoExpressionCallTO("5", "ID3", "Anat_id2", "Stage_id6", 
                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                        DataState.NODATA, DataState.HIGHQUALITY, true, OriginOfLine.SELF),
                // modify stageId, noExpressionRelaxedInSituData, noExpressionRnaSeqData  
                new NoExpressionCallTO("10", "ID3", "Anat_id8", "Stage_id7", 
                        DataState.LOWQUALITY, DataState.LOWQUALITY, 
                        DataState.HIGHQUALITY, DataState.NODATA, true, OriginOfLine.SELF));
        
               //TODO: add a NoExpressionCallTO as in the database, that will have a match, 
               //but will thus not be actually updated

        try {
            MySQLNoExpressionCallDAO dao = new MySQLNoExpressionCallDAO(this.getMySQLDAOManager());
            assertEquals("Incorrect nummber of rows updated", 
                    5, dao.updateNoExprCalls(noExprCallTOs));

            try (BgeePreparedStatement stmt = this.getMySQLDAOManager().getConnection().
                    prepareStatement("select 1 from noExpression where noExpressionId = ? and " +
                            "geneId = ? and anatEntityId = ? and stageId  = ? and  " +
                            "noExpressionAffymetrixData = ? and noExpressionInSituData = ? and  " +
                            "noExpressionRelaxedInSituData = ? and noExpressionRnaSeqData = ?")) {
                stmt.setInt(1, 1);
                stmt.setString(2, "ID2");
                stmt.setString(3, "Anat_id5");
                stmt.setString(4, "Stage_id1");
                stmt.setString(5, DataState.LOWQUALITY.getStringRepresentation());
                stmt.setString(6, DataState.NODATA.getStringRepresentation());
                stmt.setString(7, DataState.HIGHQUALITY.getStringRepresentation());
                stmt.setString(8, DataState.LOWQUALITY.getStringRepresentation());
                assertTrue("NoExpressionCallTO incorrectly updated", 
                        stmt.getRealPreparedStatement().executeQuery().next());

                stmt.setInt(1, 8);
                stmt.setString(2, "ID1");
                stmt.setString(3, "Anat_id2");
                stmt.setString(4, "Stage_id11");
                stmt.setString(5, DataState.HIGHQUALITY.getStringRepresentation());
                stmt.setString(6, DataState.HIGHQUALITY.getStringRepresentation());
                stmt.setString(7, DataState.NODATA.getStringRepresentation());
                stmt.setString(8, DataState.NODATA.getStringRepresentation());
                assertTrue("NoExpressionCallTO incorrectly updated", 
                        stmt.getRealPreparedStatement().executeQuery().next());
            }

            try (BgeePreparedStatement stmt = this.getMySQLDAOManager().getConnection().
                    prepareStatement("select 1 from globalNoExpression where " +
                            "globalNoExpressionId = ? and geneId = ? and anatEntityId = ? and " +
                            "stageId  = ? and  noExpressionAffymetrixData = ? and " +
                            "noExpressionInSituData = ? and noExpressionRelaxedInSituData = ? and " +
                            "noExpressionRnaSeqData = ? and noExpressionOriginOfLine = ?")) {
                stmt.setInt(1, 2);
                stmt.setString(2, "ID1");
                stmt.setString(3, "Anat_id2");
                stmt.setString(4, "Stage_id13");
                stmt.setString(5, DataState.NODATA.getStringRepresentation());
                stmt.setString(6, DataState.HIGHQUALITY.getStringRepresentation());
                stmt.setString(7, DataState.NODATA.getStringRepresentation());
                stmt.setString(8, DataState.HIGHQUALITY.getStringRepresentation());
                stmt.setString(9, OriginOfLine.PARENT.getStringRepresentation());
                assertTrue("NoExpressionCallTO incorrectly updated", 
                        stmt.getRealPreparedStatement().executeQuery().next());

                stmt.setInt(1, 5);
                stmt.setString(2, "ID3");
                stmt.setString(3, "Anat_id2");
                stmt.setString(4, "Stage_id6");
                stmt.setString(5, DataState.HIGHQUALITY.getStringRepresentation());
                stmt.setString(6, DataState.HIGHQUALITY.getStringRepresentation());
                stmt.setString(7, DataState.NODATA.getStringRepresentation());
                stmt.setString(8, DataState.HIGHQUALITY.getStringRepresentation());
                stmt.setString(9, OriginOfLine.SELF.getStringRepresentation());
                assertTrue("NoExpressionCallTO incorrectly updated", 
                        stmt.getRealPreparedStatement().executeQuery().next());
                
                stmt.setInt(1, 10);
                stmt.setString(2, "ID3");
                stmt.setString(3, "Anat_id8");
                stmt.setString(4, "Stage_id7");
                stmt.setString(5, DataState.LOWQUALITY.getStringRepresentation());
                stmt.setString(6, DataState.LOWQUALITY.getStringRepresentation());
                stmt.setString(7, DataState.HIGHQUALITY.getStringRepresentation());
                stmt.setString(8, DataState.NODATA.getStringRepresentation());
                stmt.setString(9, OriginOfLine.SELF.getStringRepresentation());
                assertTrue("NoExpressionCallTO incorrectly updated", 
                        stmt.getRealPreparedStatement().executeQuery().next());
            }
            
            thrown.expect(IllegalArgumentException.class);
            thrown.expectMessage("The provided basic call 10 was not found in the data source");
            
            noExprCallTOs = Arrays.asList(
                    new NoExpressionCallTO("10", "ID2", "Anat_id5", "Stage_id1", 
                            DataState.LOWQUALITY, DataState.NODATA, 
                            DataState.HIGHQUALITY, DataState.LOWQUALITY, false, OriginOfLine.SELF));
            
            dao.updateNoExprCalls(noExprCallTOs);
        } finally {
            this.emptyAndUseDefaultDB();
        }
    }
}
