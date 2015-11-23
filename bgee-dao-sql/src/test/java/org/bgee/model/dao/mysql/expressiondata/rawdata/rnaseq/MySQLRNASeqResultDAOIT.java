package org.bgee.model.dao.mysql.expressiondata.rawdata.rnaseq;

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
    @Test
    public void shouldUpdateNoExpressionConflicts() throws SQLException {
     
        this.useEmptyDB();
        this.populateAndUseDatabase();

        Set<String> noExprIds = new HashSet<String>(Arrays.asList("4", "8", "98"));
        try {
            MySQLRNASeqResultDAO dao = new MySQLRNASeqResultDAO(this.getMySQLDAOManager());
            assertEquals("Incorrect number of rows updated", 2, 
                    dao.updateNoExpressionConflicts(noExprIds));

            try (BgeePreparedStatement stmt = this.getMySQLDAOManager().getConnection().
                    prepareStatement("SELECT 1 FROM rnaSeqResult WHERE rnaSeqLibraryId = ? AND " +
                            "geneId = ? AND rpkm = ? AND tpm = ? AND readsCount = ? AND " +
                            "expressionId is null AND noExpressionId is null " +
                            "AND detectionFlag = ? AND rnaSeqData = ? AND reasonForExclusion = ?")) {
                stmt.setString(1, "GSM1015161");
                stmt.setString(2, "ID2");
                stmt.setBigDecimal(3, "22.65");
                stmt.setBigDecimal(4, "54.3");
                stmt.setInt(5, 31);
                stmt.setString(6, DetectionFlag.ABSENT.getStringRepresentation());
                stmt.setString(7, DataState.HIGHQUALITY.getStringRepresentation());
                stmt.setString(8, ExclusionReason.NOEXPRESSIONCONFLICT.getStringRepresentation());
                assertTrue("RNASeqResultTO incorrectly updated", 
                        stmt.getRealPreparedStatement().executeQuery().next());

                stmt.setString(1, "GSM1015162");
                stmt.setString(2, "ID3");
                stmt.setBigDecimal(3, "10000");
                stmt.setBigDecimal(4, "9955.3223");
                stmt.setInt(5, 31);
                stmt.setString(6, DetectionFlag.ABSENT.getStringRepresentation());
                stmt.setString(7, DataState.LOWQUALITY.getStringRepresentation());
                stmt.setString(8, ExclusionReason.NOEXPRESSIONCONFLICT.getStringRepresentation());
                assertTrue("RNASeqResultTO incorrectly updated", 
                        stmt.getRealPreparedStatement().executeQuery().next());
            }
            
            thrown.expect(IllegalArgumentException.class);
            noExprIds.clear();
            dao.updateNoExpressionConflicts(noExprIds);
        } finally {
            this.emptyAndUseDefaultDB();
        }
    }
    

    /**
     * Regression test for rpkm/tpm fields using decimal type: we use to set these values 
     * using {@code setFloat} instead of {@code setBigDecimal}, and this was incorrectly 
     * truncating/rounding values.
     */
    @Test
    public void regressionTestForDecimalField() throws SQLException {
        this.useSelectDB();
        
        try (BgeePreparedStatement stmt = this.getMySQLDAOManager().getConnection().
                prepareStatement("SELECT 1 FROM rnaSeqResult WHERE rnaSeqLibraryId = ? AND " +
                        "geneId = ? AND rpkm = ? AND tpm = ?")) {

            stmt.setString(1, "GSM1015162");
            stmt.setString(2, "ID3");
            stmt.setBigDecimal(3, "10000");
            //this is the value that allowed to detect the bug when using setFloat
            stmt.setBigDecimal(4, "9955.3223");
            assertTrue("RNASeqResultTO incorrectly retrieved", 
                    stmt.getRealPreparedStatement().executeQuery().next());
        }
    }
}
