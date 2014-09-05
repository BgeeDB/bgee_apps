package org.bgee.model.dao.mysql.expressiondata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.expressiondata.CallDAO.CallTO.DataState;
import org.bgee.model.dao.api.expressiondata.NoExpressionCallDAO;
import org.bgee.model.dao.api.expressiondata.NoExpressionCallDAO.GlobalNoExpressionToNoExpressionTO;
import org.bgee.model.dao.api.expressiondata.NoExpressionCallDAO.NoExpressionCallTO;
import org.bgee.model.dao.api.expressiondata.NoExpressionCallDAO.NoExpressionCallTO.OriginOfLineType;
import org.bgee.model.dao.api.expressiondata.NoExpressionCallDAO.NoExpressionCallTOResultSet;
import org.bgee.model.dao.api.expressiondata.NoExpressionCallParams;
import org.bgee.model.dao.mysql.MySQLITAncestor;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.junit.Test;


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

    /**
     * Test the select method {@link MySQLNoExpressionCallDAO#getAllNoExpressionCalls()}.
     * @throws SQLException 
     */
    @Test
    public void shouldGetAllNoExpressionCalls() throws SQLException {
        log.entry();
        
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
                new NoExpressionCallTO("1","ID2", "Anat_id5", "Stage_id13", DataState.LOWQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, false),
                new NoExpressionCallTO("2","ID1", "Anat_id1", "Stage_id1", DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.LOWQUALITY, false),
                new NoExpressionCallTO("3","ID3", "Anat_id6", "Stage_id6", DataState.NODATA, DataState.NODATA, DataState.LOWQUALITY, false),
                new NoExpressionCallTO("4","ID2", "Anat_id11", "Stage_id11", DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, false),
                new NoExpressionCallTO("5","ID3", "Anat_id8", "Stage_id10", DataState.LOWQUALITY, DataState.LOWQUALITY, DataState.LOWQUALITY, false),
                new NoExpressionCallTO("6","ID3", "Anat_id6", "Stage_id7", DataState.LOWQUALITY, DataState.NODATA, DataState.HIGHQUALITY, false),
                new NoExpressionCallTO("7","ID3", "Anat_id5", "Stage_id6", DataState.HIGHQUALITY, DataState.NODATA, DataState.HIGHQUALITY, false),
                new NoExpressionCallTO("8","ID3", "Anat_id5", "Stage_id14", DataState.LOWQUALITY, DataState.HIGHQUALITY, DataState.NODATA, false)); 
        // Compare
        compareTOResultSetAndTOList(dao.getAllNoExpressionCalls(params), expectedNoExprCalls);

        // With speciesIds but not include parent structures 
        params.addAllSpeciesIds(Arrays.asList("21", "41"));
        //Generate manually expected result
        expectedNoExprCalls = Arrays.asList(
                new NoExpressionCallTO("1","ID2", "Anat_id5", "Stage_id13", DataState.LOWQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, false),
                new NoExpressionCallTO("4","ID2", "Anat_id11", "Stage_id11", DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, false)); 
        // Compare
        compareTOResultSetAndTOList(dao.getAllNoExpressionCalls(params), expectedNoExprCalls);
        
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
                new NoExpressionCallTO("1", "ID2", "Anat_id5", "Stage_id13", DataState.LOWQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, true, OriginOfLineType.SELF),
                new NoExpressionCallTO("2", "ID2", "Anat_id2", "Stage_id13", DataState.LOWQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, true, OriginOfLineType.PARENT),
                new NoExpressionCallTO("3", "ID2", "Anat_id1", "Stage_id13", DataState.LOWQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, true, OriginOfLineType.PARENT),
                new NoExpressionCallTO("7", "ID2", "Anat_id11", "Stage_id11", DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, true, OriginOfLineType.SELF),
                new NoExpressionCallTO("8", "ID2", "Anat_id10", "Stage_id11", DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, true, OriginOfLineType.PARENT),
                new NoExpressionCallTO("9", "ID2", "Anat_id1", "Stage_id11", DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, true, OriginOfLineType.PARENT));
        // Compare
        compareTOResultSetAndTOList(dao.getAllNoExpressionCalls(params), expectedNoExprCalls);
        
        // Without species filter but include substructures
        // Generate parameters
        params.clearSpeciesIds();
        // Generate manually expected result
        expectedNoExprCalls = Arrays.asList(
                new NoExpressionCallTO("1", "ID2", "Anat_id5", "Stage_id13", DataState.LOWQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, true, OriginOfLineType.SELF),
                new NoExpressionCallTO("2", "ID2", "Anat_id2", "Stage_id13", DataState.LOWQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, true, OriginOfLineType.PARENT),
                new NoExpressionCallTO("3", "ID2", "Anat_id1", "Stage_id13", DataState.LOWQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, true, OriginOfLineType.PARENT),
                new NoExpressionCallTO("4", "ID3", "Anat_id6", "Stage_id6", DataState.NODATA, DataState.NODATA, DataState.LOWQUALITY, true, OriginOfLineType.SELF),
                new NoExpressionCallTO("5", "ID3", "Anat_id5", "Stage_id6", DataState.HIGHQUALITY, DataState.NODATA, DataState.HIGHQUALITY, true, OriginOfLineType.BOTH),
                new NoExpressionCallTO("6", "ID3", "Anat_id1", "Stage_id6", DataState.NODATA, DataState.NODATA, DataState.LOWQUALITY, true, OriginOfLineType.PARENT),
                new NoExpressionCallTO("7", "ID2", "Anat_id11", "Stage_id11", DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, true, OriginOfLineType.SELF),
                new NoExpressionCallTO("8", "ID2", "Anat_id10", "Stage_id11", DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, true, OriginOfLineType.PARENT),
                new NoExpressionCallTO("9", "ID2", "Anat_id1", "Stage_id11", DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, true, OriginOfLineType.PARENT),
                new NoExpressionCallTO("10", "ID3", "Anat_id8", "Stage_id10", DataState.LOWQUALITY, DataState.LOWQUALITY, DataState.LOWQUALITY, true, OriginOfLineType.SELF),
                new NoExpressionCallTO("11", "ID3", "Anat_id7", "Stage_id10", DataState.LOWQUALITY, DataState.LOWQUALITY, DataState.LOWQUALITY, true, OriginOfLineType.PARENT),
                new NoExpressionCallTO("12", "ID3", "Anat_id6", "Stage_id7", DataState.LOWQUALITY, DataState.NODATA, DataState.HIGHQUALITY, true, OriginOfLineType.SELF),
                new NoExpressionCallTO("13", "ID3", "Anat_id1", "Stage_id7", DataState.LOWQUALITY, DataState.NODATA, DataState.HIGHQUALITY, true, OriginOfLineType.PARENT));
        // Compare
        compareTOResultSetAndTOList(dao.getAllNoExpressionCalls(params), expectedNoExprCalls);
        
        log.exit();
    }
    
    /**
     * Compare a {@code NoExpressionCallTOResultSet} with a {@code List} of 
     * {@code NoExpressionCallTO}.
     * 
     * @param noExpressionResultSet A {@code NoExpressionCallTOResultSet} to be compared to 
     *                              {@code expectedNoExprCalls}.
     * @param listNoExpressionTO    A {@code List} of {@code NoExpressionCallTO} to be compared to 
     *                              {@code noExpressionResultSet}.
     * @return                      {@code true} if {@code noExpressionResultSet} and 
     *                              {@code listNoExpressionTO} have same {@code NoExpressionCallTO}s.
     */
    private void compareTOResultSetAndTOList(NoExpressionCallTOResultSet methResults,
            List<NoExpressionCallTO> expectedNoExprCalls) {
        log.entry(methResults, expectedNoExprCalls);

        try {
            int countMethNoExprCalls = 0;
            while (methResults.next()) {
                boolean found = false;
                NoExpressionCallTO methNoExprCall = methResults.getTO();
                countMethNoExprCalls++;
                for (NoExpressionCallTO expNoExprCall: expectedNoExprCalls) {
                    if (this.areNoExpressionCallTOsEqual(methNoExprCall, expNoExprCall)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    log.debug("No equivalent NoExpressionCallTO found for {}",
                            methNoExprCall.toString());
                    throw log.throwing(new AssertionError("Incorrect generated TO"));
                }
            }
            if (countMethNoExprCalls != expectedNoExprCalls.size()) {
                log.debug("Not all NoExpressionCallTOs found for {}, {} generated but {} expected",
                        expectedNoExprCalls.toString(), countMethNoExprCalls, 
                        expectedNoExprCalls.size());
                throw log.throwing(new AssertionError("Incorrect number of generated TOs"));
            }
        } finally {
            methResults.close();
        }

        log.exit();
    }

    /**
     * Method to compare two {@code NoExpressionCallTO}s, to check for complete equality of each
     * attribute. This is because the {@code equals} method of {@code NoExpressionCallTO}s is 
     * solely based on their ID, gene ID, developmental stage ID, and anatomical entity ID, not on 
     * other attributes.
     * 
     * @param exprCallTO1   A {@code NoExpressionCallTO} to be compared to {@code exprCallTO2}.
     * @param exprCallTO2   A {@code NoExpressionCallTO} to be compared to {@code exprCallTO1}.
     * @return              {@code true} if {@code exprCallTO1} and {@code exprCallTO2} have all 
     *                      attributes equal.
     */
    private boolean areNoExpressionCallTOsEqual(
            NoExpressionCallTO exprCallTO1, NoExpressionCallTO exprCallTO2) {
        log.entry(exprCallTO1, exprCallTO2);
        if (exprCallTO1.getGeneId().equals(exprCallTO1.getGeneId()) &&
            exprCallTO1.getDevStageId().equals(exprCallTO1.getDevStageId()) &&
            exprCallTO1.getAnatEntityId().equals(exprCallTO1.getAnatEntityId()) &&
            exprCallTO1.getAffymetrixData() == exprCallTO2.getAffymetrixData() &&
            exprCallTO1.getInSituData() == exprCallTO2.getInSituData() &&
            exprCallTO1.getRNASeqData() == exprCallTO2.getRNASeqData() &&
            exprCallTO1.getAffymetrixData() == exprCallTO2.getAffymetrixData() &&
            exprCallTO1.isIncludeParentStructures() == exprCallTO2.isIncludeParentStructures()) {
            return log.exit(true);
        }
        log.debug("No-expression calls {} and {} are not equivalent", exprCallTO1, exprCallTO2);
        return log.exit(false);
    }

    /**
     * Test the select method {@link MySQLNoExpressionCallDAO#insertNoExpressionCalls()}.
     * @throws SQLException 
     */
    @Test
    public void shouldInsertNoExpressionCalls() throws SQLException {
        log.entry();
        
        this.useEmptyDB();
        
        //create a Collection of NoExpressionCallTO to be inserted
        Collection<NoExpressionCallTO> noExprCallTOs = Arrays.asList(
                new NoExpressionCallTO("1", "ID3", "Anat_id1", "Stage_id1", DataState.LOWQUALITY, DataState.NODATA, DataState.HIGHQUALITY, false),
                new NoExpressionCallTO("7", "ID2", "Anat_id11", "Stage_id13", DataState.NODATA, DataState.HIGHQUALITY, DataState.LOWQUALITY, false),
                new NoExpressionCallTO("16", "ID2", "Anat_id3", "Stage_id18", DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, true, OriginOfLineType.BOTH),
                new NoExpressionCallTO("20", "ID2", "Anat_id10", "Stage_id18", DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, true, OriginOfLineType.SELF),
                new NoExpressionCallTO("21", "ID2", "Anat_id11", "Stage_id18", DataState.LOWQUALITY, DataState.NODATA, DataState.HIGHQUALITY, true, OriginOfLineType.PARENT));

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
        log.exit();
    }
    
    /**
     * Test the select method 
     * {@link MySQLNoExpressionCallDAO#insertGlobalNoExpressionToNoExpression()}.
     * @throws SQLException 
     */
    @Test
    public void shouldInsertGlobalNoExpressionToNoExpression() throws SQLException {
        log.entry();
        
        this.useEmptyDB();

        // Create a collection of NoExpressionCallTO to be inserted
        Collection<GlobalNoExpressionToNoExpressionTO> globalNoExprToNoExprTOs = Arrays.asList(
                new GlobalNoExpressionToNoExpressionTO("1","10"),
                new GlobalNoExpressionToNoExpressionTO("1","1"),
                new GlobalNoExpressionToNoExpressionTO("2","14"));

        try {
            MySQLNoExpressionCallDAO dao = new MySQLNoExpressionCallDAO(this.getMySQLDAOManager());
            assertEquals("Incorrect number of rows inserted", 3, 
                    dao.insertGlobalNoExpressionToNoExpression(globalNoExprToNoExprTOs));
            
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

        log.exit();
    }
}
