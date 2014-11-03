package org.bgee.model.dao.mysql.expressiondata.rawdata.affymetrix;

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
import org.junit.Test;
import org.junit.rules.ExpectedException;


/**
 * Integration tests for {@link MySQLAffymetrixProbesetDAO}, performed on a real MySQL database. 
 * See the documentation of {@link org.bgee.model.dao.mysql.MySQLITAncestor} for 
 * important information.
 * 
 * @author Valentine Rech de Laval
 * @version Bgee 13
 * @see org.bgee.model.dao.api.expressiondata.rawdata.affymetrix.AffymetrixProbesetDAO
 * @since Bgee 13
 */
public class MySQLAffymetrixProbesetDAOIT extends MySQLITAncestor {

    private final static Logger log = 
            LogManager.getLogger(MySQLAffymetrixProbesetDAOIT.class.getName());

    public MySQLAffymetrixProbesetDAOIT() {
        super();
    }
    
    @Override
    protected Logger getLogger() {
        return log;
    }

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    /**
     * Test the select method {@link MySQLAffymetrixProbesetDAOIT#updateNoExpressionConflicts()}.
     */
//TODO remove comment    @Test
    public void shouldUpdateNoExpressionConflicts() throws SQLException {

        this.useEmptyDB();
        this.populateAndUseDatabase();

        Set<String> noExprIds = new HashSet<String>(Arrays.asList("2", "10"));
        try {
            MySQLAffymetrixProbesetDAO dao = new MySQLAffymetrixProbesetDAO(this.getMySQLDAOManager());
            assertEquals("Incorrect number of rows updated", 2, 
                    dao.updateNoExpressionConflicts(noExprIds));

            try (BgeePreparedStatement stmt = this.getMySQLDAOManager().getConnection().
                    prepareStatement("SELECT 1 FROM affymetrixProbeset WHERE " + 
                            "affymetrixProbesetId = ? AND bgeeAffymetrixChipId = ? AND " + 
                            "geneId = ? AND  normalizedSignalIntensity = ? AND " + 
                            "detectionFlag = ? AND expressionId = ? AND noExpressionId = ? AND " + 
                            "affymetrixData = ? AND reasonForExclusion = ?")) {
                stmt.setString(1, "affymetrixProbesetId");
                stmt.setString(2, "bgeeAffymetrixChipId");
                stmt.setString(3, "geneId");
                stmt.setFloat(4, 999);
                stmt.setString(5, "detectionFlag");
                stmt.setInt(6, Integer.parseInt("expressionId"));
                stmt.setInt(7, Integer.parseInt("noExpressionId"));
                stmt.setString(8, "affymetrixData");
                stmt.setString(9, "reasonForExclusion");
                assertTrue("AffymetrixProbesetTO incorrectly updated", 
                        stmt.getRealPreparedStatement().executeQuery().next());

                stmt.setString(1, "affymetrixProbesetId");
                stmt.setString(2, "bgeeAffymetrixChipId");
                stmt.setString(3, "geneId");
                stmt.setFloat(4, 999);
                stmt.setString(5, "detectionFlag");
                stmt.setInt(6, Integer.parseInt("expressionId"));
                stmt.setInt(7, Integer.parseInt("noExpressionId"));
                stmt.setString(8, "affymetrixData");
                stmt.setString(9, "reasonForExclusion");
                assertTrue("AffymetrixProbesetTO incorrectly updated", 
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
