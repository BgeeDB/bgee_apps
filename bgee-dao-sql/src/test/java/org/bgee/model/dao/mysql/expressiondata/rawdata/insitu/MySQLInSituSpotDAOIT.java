package org.bgee.model.dao.mysql.expressiondata.rawdata.insitu;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.expressiondata.CallDAO.CallTO.DataState;
import org.bgee.model.dao.api.expressiondata.rawdata.CallSourceRawDataDAO.CallSourceRawDataTO.DetectionFlag;
import org.bgee.model.dao.api.expressiondata.rawdata.CallSourceRawDataDAO.CallSourceRawDataTO.ExclusionReason;
import org.bgee.model.dao.mysql.MySQLITAncestor;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.junit.Rule;
import org.junit.Test;
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
    @Test
    public void shouldUpdateNoExpressionConflicts() throws SQLException {
     
        this.useEmptyDB();
        this.populateAndUseDatabase();

        Set<String> noExprIds = new HashSet<String>(Arrays.asList("2"));
        try {
            MySQLInSituSpotDAO dao = new MySQLInSituSpotDAO(this.getMySQLDAOManager());
            assertEquals("Incorrect number of rows updated", 2, 
                    dao.updateNoExpressionConflicts(noExprIds));

            try (BgeePreparedStatement stmt = this.getMySQLDAOManager().getConnection().
                    prepareStatement("SELECT 1 FROM inSituSpot WHERE inSituSpotId = ? AND " + 
                            "inSituEvidenceId = ? AND inSituExpressionPatternId = ? AND " + 
                            "anatEntityId  = ? AND stageId = ? AND geneId = ? AND " + 
                            "detectionFlag = ? AND expressionId is null AND " +
                            "noExpressionId is null AND inSituData = ? AND reasonForExclusion = ?")) {
                stmt.setString(1, "BDGP-10000");
                stmt.setString(2, "BDGP_140958");
                stmt.setString(3, "");
                stmt.setString(4, "Anat_id11");
                stmt.setString(5, "Stage_id8");
                stmt.setString(6, "ID1");
                stmt.setString(7, DetectionFlag.ABSENT.getStringRepresentation());
                stmt.setString(8, DataState.LOWQUALITY.getStringRepresentation());
                stmt.setString(9, ExclusionReason.NOEXPRESSIONCONFLICT.getStringRepresentation());
                assertTrue("InSituSpotTO incorrectly updated", 
                        stmt.getRealPreparedStatement().executeQuery().next());

                stmt.setString(1, "mgi-1061");
                stmt.setString(2, "MGI:2677299.7");
                stmt.setString(3, "");
                stmt.setString(4, "Anat_id10");
                stmt.setString(5, "Stage_id2");
                stmt.setString(6, "ID3");
                stmt.setString(7, DetectionFlag.ABSENT.getStringRepresentation());
                stmt.setString(8, DataState.LOWQUALITY.getStringRepresentation());
                stmt.setString(9, ExclusionReason.NOEXPRESSIONCONFLICT.getStringRepresentation());
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
