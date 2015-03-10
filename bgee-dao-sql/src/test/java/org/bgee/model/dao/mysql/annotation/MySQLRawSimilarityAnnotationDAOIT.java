package org.bgee.model.dao.mysql.annotation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.sql.Date;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.annotation.RawSimilarityAnnotationDAO.RawSimilarityAnnotationTO;
import org.bgee.model.dao.mysql.MySQLITAncestor;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.junit.Test;


/**
 * Integration tests for {@link MySQLRawSimilarityAnnotationDAO}, performed on a real MySQL 
 * database. See the documentation of {@link org.bgee.model.dao.mysql.MySQLITAncestor} for 
 * important information.
 *
 * @author 	Valentine Rech de Laval
 * @version Bgee 13
 * @since 	Bgee 13
 */
public class MySQLRawSimilarityAnnotationDAOIT  extends MySQLITAncestor {

    private final static Logger log = 
            LogManager.getLogger(MySQLRawSimilarityAnnotationDAOIT.class.getName());
    
    public MySQLRawSimilarityAnnotationDAOIT() {
        super();
    }

    @Override
    protected Logger getLogger() {
        return log;
    }

    /**
     * Test the insert method 
     * {@link MySQLRawSimilarityAnnotationDAO#insertRawSimilarityAnnotations()}.
     */
    @Test
    public void shouldInsertRawSimilarityAnnotations() throws SQLException {
        
        this.useEmptyDB();

        // Create a Collection of SummarySimilarityAnnotationTO to be inserted,
        // no attribute could be null according database schema.
        Date date1 = new java.sql.Date(12345);
        Date date2 = new java.sql.Date(12349);
        Collection<RawSimilarityAnnotationTO> rawTOs = Arrays.asList(
                new RawSimilarityAnnotationTO("1", true, 
                        "ecoId1", "cioId1", "referenceId1", "referenceTitle1", 
                        "supportingText1", "assignedBy1", "curator1", date1),
                new RawSimilarityAnnotationTO("2", false, 
                        "ecoId2", "cioId2", "referenceId2", "referenceTitle2", 
                        "supportingText2", "assignedBy2", "curator2", date2));

        try {
            MySQLRawSimilarityAnnotationDAO dao = 
                    new MySQLRawSimilarityAnnotationDAO(this.getMySQLDAOManager());
            assertEquals("Incorrect number of rows inserted", 2, 
                    dao.insertRawSimilarityAnnotations(rawTOs));
            
            //we manually verify the insertion, as we do not want to rely on other methods 
            //that are tested elsewhere.
            try (BgeePreparedStatement stmt = this.getMySQLDAOManager().getConnection().
                    prepareStatement("SELECT 1 FROM rawSimilarityAnnotation " +
                            "WHERE summarySimilarityAnnotationId = ? AND negated = ? " +
                            "AND ECOId = ? AND CIOId = ? AND referenceId = ? AND referenceTitle = ? " +
                            "AND supportingText = ? AND assignedBy = ? AND curator = ? " +
                            "AND annotationDate = ?")) {
                
                stmt.setInt(1, 1);
                stmt.setBoolean(2, true);
                stmt.setString(3, "ecoId1");
                stmt.setString(4, "cioId1");
                stmt.setString(5, "referenceId1");
                stmt.setString(6, "referenceTitle1");
                stmt.setString(7, "supportingText1");
                stmt.setString(8, "assignedBy1");
                stmt.setString(9, "curator1");
                stmt.setDate(10, date1);
                assertTrue("RawSimilarityAnnotationTO incorrectly inserted", 
                        stmt.getRealPreparedStatement().executeQuery().next());
                
                stmt.setInt(1, 2);
                stmt.setBoolean(2, false);
                stmt.setString(3, "ecoId2");
                stmt.setString(4, "cioId2");
                stmt.setString(5, "referenceId2");
                stmt.setString(6, "referenceTitle2");
                stmt.setString(7, "supportingText2");
                stmt.setString(8, "assignedBy2");
                stmt.setString(9, "curator2");
                stmt.setDate(10, date2);
                assertTrue("RawSimilarityAnnotationTO incorrectly inserted", 
                        stmt.getRealPreparedStatement().executeQuery().next());
   
            }
            
            // We should throw an IllegalArgumentException when 
            // no RawSimilarityAnnotationTO provided
            try {
                dao.insertRawSimilarityAnnotations(new HashSet<RawSimilarityAnnotationTO>());
                // Test failed
                throw new AssertionError("insertRawSimilarityAnnotations did not throw " +
                        "an IllegalArgumentException as expected");
            } catch (IllegalArgumentException e) {
                // Test passed, do nothing
                log.catching(e);
            }
        } finally {
            this.emptyAndUseDefaultDB();
        }
    }
}
