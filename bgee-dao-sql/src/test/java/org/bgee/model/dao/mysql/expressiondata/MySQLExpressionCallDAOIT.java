package org.bgee.model.dao.mysql.expressiondata;

import static org.junit.Assert.*;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.expressiondata.ExpressionCallDAO;
import org.bgee.model.dao.api.expressiondata.ExpressionCallParams;
import org.bgee.model.dao.api.expressiondata.ExpressionCallDAO.ExpressionCallTO;
import org.bgee.model.dao.api.expressiondata.CallDAO.CallTO.DataState;
import org.bgee.model.dao.api.expressiondata.ExpressionCallDAO.ExpressionCallTO.OriginOfLineType;
import org.bgee.model.dao.api.expressiondata.ExpressionCallDAO.ExpressionCallTOResultSet;
import org.bgee.model.dao.api.expressiondata.ExpressionCallDAO.GlobalExpressionToExpressionTO;
import org.bgee.model.dao.mysql.MySQLITAncestor;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.junit.Test;


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

    /**
     * Test the select method {@link MySQLExpressionCallDAO#getAllExpressionCalls()}.
     */
    @Test
    public void shouldGetAllExpressionCalls() throws SQLException {
        log.entry();
        
        this.useSelectDB();

        // On expression table 
        MySQLExpressionCallDAO dao = new MySQLExpressionCallDAO(this.getMySQLDAOManager());
        dao.setAttributes(Arrays.asList(ExpressionCallDAO.Attribute.ID, 
                ExpressionCallDAO.Attribute.GENEID, 
                ExpressionCallDAO.Attribute.DEVSTAGEID, 
                ExpressionCallDAO.Attribute.ANATENTITYID, 
                ExpressionCallDAO.Attribute.AFFYMETRIXDATA, 
                ExpressionCallDAO.Attribute.ESTDATA, 
                ExpressionCallDAO.Attribute.INSITUDATA,
                // Remove RELAXEDINSITUDATA because not already in database
                //ExpressionCallDAO.Attribute.RELAXEDINSITUDATA, 
                ExpressionCallDAO.Attribute.RNASEQDATA
                // Remove INCLUDESUBSTRUCTURES and INCLUDESUBSTAGES because not data from DB
                //ExpressionCallDAO.Attribute.INCLUDESUBSTRUCTURES, 
                //ExpressionCallDAO.Attribute.INCLUDESUBSTAGES,
                // Remove ORIGINOFLINE because we test get expression calls on expression table
                //ExpressionCallDAO.Attribute.ORIGINOFLINE
                ));

        // Without speciesIds and not include substructures
        // Generate parameters
        Set<String> speciesIds = new HashSet<String>();
        ExpressionCallParams params = new ExpressionCallParams();
        params.addAllSpeciesIds(speciesIds);
        params.setIncludeSubstructures(false);
        // Generate manually expected result
        List<ExpressionCallTO> expectedExprCalls = Arrays.asList(
                new ExpressionCallTO("1","ID3", "Anat_id1", "Stage_id1", DataState.LOWQUALITY, DataState.NODATA, DataState.HIGHQUALITY, DataState.NODATA, DataState.HIGHQUALITY, false, false),
                new ExpressionCallTO("2","ID1", "Anat_id6", "Stage_id6", DataState.LOWQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.NODATA, DataState.LOWQUALITY, false, false),
                new ExpressionCallTO("3","ID1", "Anat_id6", "Stage_id7", DataState.NODATA, DataState.NODATA, DataState.NODATA, DataState.NODATA, DataState.LOWQUALITY, false, false),
                new ExpressionCallTO("4","ID2", "Anat_id2", "Stage_id18", DataState.HIGHQUALITY,DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.NODATA, DataState.HIGHQUALITY, false, false),
                new ExpressionCallTO("5","ID1", "Anat_id7", "Stage_id10", DataState.LOWQUALITY,DataState.LOWQUALITY, DataState.LOWQUALITY, DataState.NODATA, DataState.LOWQUALITY, false, false),
                new ExpressionCallTO("6","ID2", "Anat_id11", "Stage_id12",DataState.HIGHQUALITY, DataState.LOWQUALITY, DataState.NODATA, DataState.NODATA, DataState.HIGHQUALITY, false, false),
                new ExpressionCallTO("7","ID2", "Anat_id11", "Stage_id13",DataState.NODATA,DataState.HIGHQUALITY, DataState.LOWQUALITY, DataState.NODATA, DataState.NODATA, false, false),
                new ExpressionCallTO("8","ID3", "Anat_id3", "Stage_id1", DataState.NODATA, DataState.HIGHQUALITY,DataState.LOWQUALITY, DataState.NODATA, DataState.NODATA, false, false),
                new ExpressionCallTO("9","ID2", "Anat_id1", "Stage_id9", DataState.HIGHQUALITY, DataState.LOWQUALITY,DataState.NODATA, DataState.NODATA, DataState.HIGHQUALITY, false, false)); 
        // Compare
        this.compareTOResultSetAndTOList(dao.getAllExpressionCalls(params), expectedExprCalls);

        // With speciesIds but not include substructures 
        // Generate parameters
        params.addAllSpeciesIds(Arrays.asList("11", "41")); // 41 = species Id that does not exist
        // Generate manually expected result
        expectedExprCalls = Arrays.asList(
                new ExpressionCallTO("2","ID1", "Anat_id6", "Stage_id6", DataState.LOWQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.NODATA, DataState.LOWQUALITY, false, false),
                new ExpressionCallTO("3","ID1", "Anat_id6", "Stage_id7", DataState.NODATA, DataState.NODATA, DataState.NODATA, DataState.NODATA, DataState.LOWQUALITY, false, false),
                new ExpressionCallTO("5","ID1", "Anat_id7", "Stage_id10", DataState.LOWQUALITY,DataState.LOWQUALITY, DataState.LOWQUALITY, DataState.NODATA, DataState.LOWQUALITY, false, false)); 
        // Compare
        this.compareTOResultSetAndTOList(dao.getAllExpressionCalls(params), expectedExprCalls);
        
        // On global expression table 
        dao.setAttributes(Arrays.asList(ExpressionCallDAO.Attribute.ID, 
                ExpressionCallDAO.Attribute.GENEID, 
                ExpressionCallDAO.Attribute.DEVSTAGEID, 
                ExpressionCallDAO.Attribute.ANATENTITYID, 
                ExpressionCallDAO.Attribute.AFFYMETRIXDATA, 
                ExpressionCallDAO.Attribute.ESTDATA, 
                ExpressionCallDAO.Attribute.INSITUDATA,
                ExpressionCallDAO.Attribute.RNASEQDATA,
                ExpressionCallDAO.Attribute.ORIGINOFLINE));
        // With speciesIds and include substructures 
        // Generate parameters
        params.setIncludeSubstructures(true);
        // Generate manually expected result
        expectedExprCalls = Arrays.asList(
                new ExpressionCallTO("2", "ID1", "Anat_id6", "Stage_id6", DataState.LOWQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.NODATA, DataState.LOWQUALITY, true, false, OriginOfLineType.SELF),
                new ExpressionCallTO("3", "ID1", "Anat_id6", "Stage_id7", DataState.NODATA, DataState.NODATA, DataState.NODATA, DataState.NODATA, DataState.LOWQUALITY, true, false, OriginOfLineType.SELF),
                new ExpressionCallTO("5", "ID1", "Anat_id7", "Stage_id10", DataState.LOWQUALITY, DataState.LOWQUALITY, DataState.LOWQUALITY, DataState.NODATA, DataState.LOWQUALITY, true, false, OriginOfLineType.SELF));
        // Compare
        this.compareTOResultSetAndTOList(dao.getAllExpressionCalls(params), expectedExprCalls);
        
        // Without species filter but include substructures
        // Generate parameters
        params.clearSpeciesIds();
        //Generate manually expected result
        expectedExprCalls = Arrays.asList(
                new ExpressionCallTO("1", "ID3", "Anat_id1", "Stage_id1", DataState.LOWQUALITY, DataState.NODATA, DataState.HIGHQUALITY, DataState.NODATA, DataState.HIGHQUALITY, true, false, OriginOfLineType.SELF),
                new ExpressionCallTO("2", "ID1", "Anat_id6", "Stage_id6", DataState.LOWQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.NODATA, DataState.LOWQUALITY, true, false, OriginOfLineType.SELF),
                new ExpressionCallTO("3", "ID1", "Anat_id6", "Stage_id7", DataState.NODATA, DataState.NODATA, DataState.NODATA, DataState.NODATA, DataState.LOWQUALITY, true, false, OriginOfLineType.SELF),
                new ExpressionCallTO("4", "ID2", "Anat_id2", "Stage_id18", DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.NODATA, DataState.HIGHQUALITY, true, false, OriginOfLineType.SELF),
                new ExpressionCallTO("5", "ID1", "Anat_id7", "Stage_id10", DataState.LOWQUALITY, DataState.LOWQUALITY, DataState.LOWQUALITY, DataState.NODATA, DataState.LOWQUALITY, true, false, OriginOfLineType.SELF),
                new ExpressionCallTO("6", "ID2", "Anat_id11", "Stage_id12", DataState.HIGHQUALITY, DataState.LOWQUALITY, DataState.NODATA, DataState.NODATA, DataState.HIGHQUALITY, true, false, OriginOfLineType.SELF),
                new ExpressionCallTO("7", "ID2", "Anat_id11", "Stage_id13", DataState.NODATA, DataState.HIGHQUALITY, DataState.LOWQUALITY, DataState.NODATA, DataState.NODATA, true, false, OriginOfLineType.SELF),
                new ExpressionCallTO("8", "ID3", "Anat_id2", "Stage_id1", DataState.LOWQUALITY, DataState.NODATA, DataState.HIGHQUALITY, DataState.NODATA, DataState.HIGHQUALITY, true, false, OriginOfLineType.DESCENT),
                new ExpressionCallTO("9", "ID3", "Anat_id3", "Stage_id1", DataState.LOWQUALITY, DataState.NODATA, DataState.HIGHQUALITY, DataState.NODATA, DataState.HIGHQUALITY, true, false, OriginOfLineType.DESCENT),
                new ExpressionCallTO("10", "ID3", "Anat_id4", "Stage_id1", DataState.LOWQUALITY, DataState.NODATA, DataState.HIGHQUALITY, DataState.NODATA, DataState.HIGHQUALITY, true, false, OriginOfLineType.DESCENT),
                new ExpressionCallTO("11", "ID3", "Anat_id5", "Stage_id1", DataState.LOWQUALITY, DataState.NODATA, DataState.HIGHQUALITY, DataState.NODATA, DataState.HIGHQUALITY, true, false, OriginOfLineType.DESCENT),
                new ExpressionCallTO("12", "ID3", "Anat_id6", "Stage_id1", DataState.LOWQUALITY, DataState.NODATA, DataState.HIGHQUALITY, DataState.NODATA, DataState.HIGHQUALITY, true, false, OriginOfLineType.DESCENT),
                new ExpressionCallTO("13", "ID3", "Anat_id9", "Stage_id1", DataState.LOWQUALITY, DataState.NODATA, DataState.HIGHQUALITY, DataState.NODATA, DataState.HIGHQUALITY, true, false, OriginOfLineType.DESCENT),
                new ExpressionCallTO("14", "ID3", "Anat_id10", "Stage_id1", DataState.LOWQUALITY, DataState.NODATA, DataState.HIGHQUALITY, DataState.NODATA, DataState.HIGHQUALITY, true, false, OriginOfLineType.DESCENT),
                new ExpressionCallTO("15", "ID3", "Anat_id11", "Stage_id1", DataState.LOWQUALITY, DataState.NODATA, DataState.HIGHQUALITY, DataState.NODATA, DataState.HIGHQUALITY, true, false, OriginOfLineType.DESCENT),
                new ExpressionCallTO("22", "ID3", "Anat_id3", "Stage_id1", DataState.NODATA, DataState.HIGHQUALITY, DataState.LOWQUALITY, DataState.NODATA, DataState.NODATA, true, false, OriginOfLineType.DESCENT),
                new ExpressionCallTO("23", "ID2", "Anat_id1", "Stage_id1", DataState.HIGHQUALITY, DataState.LOWQUALITY, DataState.NODATA, DataState.NODATA, DataState.HIGHQUALITY, true, false, OriginOfLineType.DESCENT),
                new ExpressionCallTO("16", "ID2", "Anat_id3", "Stage_id18", DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.NODATA, DataState.HIGHQUALITY, true, false, OriginOfLineType.DESCENT),
                new ExpressionCallTO("17", "ID2", "Anat_id4", "Stage_id18", DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.NODATA, DataState.HIGHQUALITY, true, false, OriginOfLineType.DESCENT),
                new ExpressionCallTO("18", "ID2", "Anat_id5", "Stage_id18", DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.NODATA, DataState.HIGHQUALITY, true, false, OriginOfLineType.DESCENT),
                new ExpressionCallTO("19", "ID2", "Anat_id9", "Stage_id18", DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.NODATA, DataState.HIGHQUALITY, true, false, OriginOfLineType.DESCENT),
                new ExpressionCallTO("20", "ID2", "Anat_id10", "Stage_id18", DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.NODATA, DataState.HIGHQUALITY, true, false, OriginOfLineType.DESCENT),
                new ExpressionCallTO("21", "ID2", "Anat_id11", "Stage_id18", DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.NODATA, DataState.HIGHQUALITY, true, false, OriginOfLineType.DESCENT));
        // Compare
        this.compareTOResultSetAndTOList(dao.getAllExpressionCalls(params), expectedExprCalls);
    }
    
    private void compareTOResultSetAndTOList(ExpressionCallTOResultSet methResults,
            List<ExpressionCallTO> expectedExprCalls) {
        log.entry(methResults, expectedExprCalls);
        
        try {
            int countMethExprCalls = 0;
            while (methResults.next()) {
                boolean found = false;
                ExpressionCallTO methExprCall = methResults.getTO();
                countMethExprCalls++;
                for (ExpressionCallTO expExprCall: expectedExprCalls) {
                    log.trace("Comparing {} to {}", methExprCall.getId(), expExprCall.getId());
                    if (this.areExpressionCallTOsEqual(methExprCall, expExprCall)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    log.debug("No equivalent ExpressionCallTO found for {}", methExprCall.toString());
                    throw log.throwing(new AssertionError("Incorrect generated TO"));
                }
            }
            if (countMethExprCalls != expectedExprCalls.size()) {
                log.debug("Not all ExpressionCallTO found for {}, {} generated vs {} expected",
                        expectedExprCalls.toString(), countMethExprCalls, expectedExprCalls.size());
                throw log.throwing(new AssertionError("Incorrect number of generated TOs"));
            }
        } finally {
            methResults.close();
        }
    }
    
    /**
     * Method to compare two {@code ExpressionCallTO}s, to check for complete equality of each
     * attribute. This is because the {@code equals} method of {@code ExpressionCallTO}s is 
     * solely based on their ID, gene ID, developmental stage ID, and anatomical entity ID, not on 
     * other attributes.
     * 
     * @param exprCallTO1   A {@code ExpressionCallTO} to be compared to {@code exprCallTO2}.
     * @param exprCallTO2   A {@code ExpressionCallTO} to be compared to {@code exprCallTO1}.
     * @return              {@code true} if {@code exprCallTO1} and {@code exprCallTO2} have all 
     *                      attributes equal.
     */
    private boolean areExpressionCallTOsEqual(
            ExpressionCallTO exprCallTO1, ExpressionCallTO exprCallTO2) {
        log.entry(exprCallTO1, exprCallTO2);
        if (exprCallTO1.getGeneId().equals(exprCallTO1.getGeneId()) &&
            exprCallTO1.getDevStageId().equals(exprCallTO1.getDevStageId()) &&
            exprCallTO1.getAnatEntityId().equals(exprCallTO1.getAnatEntityId()) &&
            exprCallTO1.getAffymetrixData() == exprCallTO2.getAffymetrixData() &&
            exprCallTO1.getESTData() == exprCallTO2.getESTData() &&
            exprCallTO1.getInSituData() == exprCallTO2.getInSituData() &&
            exprCallTO1.getRNASeqData() == exprCallTO2.getRNASeqData() &&
            exprCallTO1.getRelaxedInSituData() == exprCallTO2.getRelaxedInSituData() &&
            exprCallTO1.getAffymetrixData() == exprCallTO2.getAffymetrixData() &&
            exprCallTO1.isIncludeSubStages() == exprCallTO2.isIncludeSubStages() &&
            exprCallTO1.isIncludeSubstructures() == exprCallTO2.isIncludeSubstructures()) {
            return log.exit(true);
        }
        log.debug("Expression calls {} and {} are not equivalent", exprCallTO1, exprCallTO2);
        return log.exit(false);
    }

    /**
     * Test the select method {@link MySQLExpressionCallDAO#insertExpressionCalls()}.
     */
    @Test
    public void shouldInsertExpressionCalls() throws SQLException {
        log.entry();
        this.useEmptyDB();
        //create a Collection of ExpressionCallTO to be inserted
        Collection<ExpressionCallTO> exprCallTOs = Arrays.asList(
                new ExpressionCallTO("1", "ID3", "Anat_id1", "Stage_id1", DataState.LOWQUALITY, DataState.NODATA, DataState.HIGHQUALITY, DataState.NODATA, DataState.HIGHQUALITY, false, false),
                new ExpressionCallTO("7", "ID2", "Anat_id11", "Stage_id13", DataState.NODATA, DataState.HIGHQUALITY, DataState.LOWQUALITY, DataState.NODATA, DataState.NODATA, false, false),
                new ExpressionCallTO("16", "ID2", "Anat_id3", "Stage_id18", DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.NODATA, DataState.HIGHQUALITY, false, false),
                new ExpressionCallTO("20", "ID2", "Anat_id10", "Stage_id18", DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.NODATA, DataState.HIGHQUALITY, true, false, OriginOfLineType.SELF),
                new ExpressionCallTO("21", "ID2", "Anat_id11", "Stage_id18", DataState.LOWQUALITY, DataState.NODATA, DataState.HIGHQUALITY, DataState.NODATA, DataState.LOWQUALITY, true, false, OriginOfLineType.DESCENT));

        try {
            MySQLExpressionCallDAO dao = new MySQLExpressionCallDAO(this.getMySQLDAOManager());
            assertEquals("Incorrect number of rows inserted", 5, 
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
                stmt.setString(1, "20");
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
        } finally {
            this.emptyAndUseDefaultDB();
        }

        log.exit();
    }   

    /**
     * Test the select method {@link MySQLExpressionCallDAO#insertGlobalExpressionToExpression()}.
     */
    @Test
    public void shouldInsertGlobalExpressionToExpression() throws SQLException {
        log.entry();
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
        } finally {
            this.emptyAndUseDefaultDB();
        }

        log.exit();
    }    
}
