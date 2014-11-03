package org.bgee.model.dao.mysql.expressiondata.rawdata.insitu;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.mysql.MySQLITAncestor;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.junit.Rule;
import org.junit.rules.ExpectedException;


/**
 * Integration tests for {@link MySQLInSituSpotDAO}, performed on a real MySQL database. 
 * See the documentation of {@link org.bgee.model.dao.mysql.MySQLITAncestor} for 
 * important information.
 * 
 * @author Valentine Rech de Laval
 * @version Bgee 13
 * @see org.bgee.model.dao.api.expressiondata.rawdata.insitu.InSituSpotDAO
 * @since Bgee 13
 */
public class MySQLInSituSpotDAOIT extends MySQLITAncestor {

    private final static Logger log = 
            LogManager.getLogger(MySQLInSituSpotDAOIT.class.getName());

    public MySQLInSituSpotDAOIT() {
        super();
    }
    
    @Override
    protected Logger getLogger() {
        return log;
    }

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    /**
     * Test the select method {@link MySQLInSituSpotDAOIT#updateNoExpressionConflicts()}.
     */
//TODO remove comment    @Test
    public void shouldUpdateNoExpressionConflicts() throws SQLException {
     
        this.useEmptyDB();
        this.populateAndUseDatabase();

        Set<String> noExprIds = new HashSet<String>(Arrays.asList("2", "10"));
        try {
            MySQLInSituSpotDAO dao = new MySQLInSituSpotDAO(this.getMySQLDAOManager());
            assertEquals("Incorrect number of rows updated", 2, 
                    dao.updateNoExpressionConflicts(noExprIds));

            try (BgeePreparedStatement stmt = this.getMySQLDAOManager().getConnection().
                    prepareStatement("SELECT 1 FROM inSituSpot WHERE inSituSpotId = ? AND " + 
                            "inSituEvidenceId = ? AND inSituExpressionPatternId = ? AND " + 
                            "anatEntityId  = ? AND stageId = ? AND geneId = ? AND " + 
                            "detectionFlag = ? AND expressionId = ? AND noExpressionId = ? AND " + 
                            "inSituData = ? AND reasonForExclusion = ?")) {
                stmt.setString(1, "inSituSpotId");
                stmt.setString(2, "inSituEvidenceId");
                stmt.setString(3, "inSituExpressionPatternId");
                stmt.setString(4, "anatEntityId");
                stmt.setString(5, "stageId");
                stmt.setString(6, "geneId");
                stmt.setString(7, "detectionFlag");
                stmt.setInt(8, Integer.parseInt("expressionId"));
                stmt.setInt(9, Integer.parseInt("noExpressionId"));
                stmt.setString(10, "inSituData");
                stmt.setString(11, "reasonForExclusion");
                assertTrue("InSituSpotTO incorrectly updated", 
                        stmt.getRealPreparedStatement().executeQuery().next());

                stmt.setString(1, "inSituSpotId");
                stmt.setString(2, "inSituEvidenceId");
                stmt.setString(3, "inSituExpressionPatternId");
                stmt.setString(4, "anatEntityId");
                stmt.setString(5, "stageId");
                stmt.setString(6, "geneId");
                stmt.setString(7, "detectionFlag");
                stmt.setInt(8, Integer.parseInt("expressionId"));
                stmt.setInt(9, Integer.parseInt("noExpressionId"));
                stmt.setString(10, "inSituData");
                stmt.setString(11, "reasonForExclusion");
                assertTrue("InSituSpotTO incorrectly updated", 
                        stmt.getRealPreparedStatement().executeQuery().next());
            }
            
            thrown.expect(IllegalArgumentException.class);
            thrown.expectMessage("The provided no-expression ID 98 was not found in the data source");
            noExprIds = new HashSet<String>(Arrays.asList("98", "2"));
            dao.updateNoExpressionConflicts(noExprIds);

        } finally {
            this.emptyAndUseDefaultDB();
        }
    }
}
