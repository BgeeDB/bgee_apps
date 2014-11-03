package org.bgee.model.dao.mysql.expressiondata.rawdata.rnaseq;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
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


public class MySQLRNASeqResultDAOIT  extends MySQLITAncestor {

    private final static Logger log = 
            LogManager.getLogger(MySQLRNASeqResultDAOIT.class.getName());

    public MySQLRNASeqResultDAOIT() {
        super();
    }
    
    @Override
    protected Logger getLogger() {
        return log;
    }

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    /**
     * Test the select method {@link MySQLRNASeqResultDAOIT#updateNoExpressionConflicts()}.
     */
//TODO remove comment    @Test
    public void shouldUpdateNoExpressionConflicts() throws SQLException {
     
        this.useEmptyDB();
        this.populateAndUseDatabase();

        Set<String> noExprIds = new HashSet<String>(Arrays.asList("2", "10"));
        try {
            MySQLRNASeqResultDAO dao = new MySQLRNASeqResultDAO(this.getMySQLDAOManager());
            assertEquals("Incorrect number of rows updated", 2, 
                    dao.updateNoExpressionConflicts(noExprIds));

            try (BgeePreparedStatement stmt = this.getMySQLDAOManager().getConnection().
                    prepareStatement("SELECT 1 FROM rnaSeqResult WHERE rnaSeqLibraryId = ? AND " +
                            "geneId = ? AND log2RPK = ? AND readsCount = ? AND " +
                            "expressionId = ? AND noExpressionId = ? AND detectionFlag = ? AND " +
                            "rnaSeqData = ? AND reasonForExclusion = ?")) {
                stmt.setString(1, "rnaSeqLibraryId");
                stmt.setString(2, "geneId");
                stmt.setBigDecimal(3, new BigDecimal("log2RPK"));
                stmt.setInt(4, Integer.parseInt("readsCount"));
                stmt.setInt(5, Integer.parseInt("expressionId"));
                stmt.setInt(6, Integer.parseInt("noExpressionId"));
                stmt.setString(7, "detectionFlag");
                stmt.setString(8, "rnaSeqData");
                stmt.setString(9, "reasonForExclusion");
                assertTrue("RNASeqResultTO incorrectly updated", 
                        stmt.getRealPreparedStatement().executeQuery().next());

                stmt.setString(1, "rnaSeqLibraryId");
                stmt.setString(2, "geneId");
                stmt.setBigDecimal(3, new BigDecimal("log2RPK"));
                stmt.setInt(4, Integer.parseInt("readsCount"));
                stmt.setInt(5, Integer.parseInt("expressionId"));
                stmt.setInt(6, Integer.parseInt("noExpressionId"));
                stmt.setString(7, "detectionFlag");
                stmt.setString(8, "rnaSeqData");
                stmt.setString(9, "reasonForExclusion");
                assertTrue("RNASeqResultTO incorrectly updated", 
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
