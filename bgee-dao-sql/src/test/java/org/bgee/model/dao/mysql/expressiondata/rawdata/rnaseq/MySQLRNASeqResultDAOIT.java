package org.bgee.model.dao.mysql.expressiondata.rawdata.rnaseq;

import static org.junit.Assert.assertTrue;

import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
