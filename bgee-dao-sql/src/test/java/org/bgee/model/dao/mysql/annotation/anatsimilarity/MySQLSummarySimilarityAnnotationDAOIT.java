package org.bgee.model.dao.mysql.annotation.anatsimilarity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.annotation.anatsimilarity.SummarySimilarityAnnotationDAO.SimAnnotToAnatEntityTO;
import org.bgee.model.dao.api.annotation.anatsimilarity.SummarySimilarityAnnotationDAO.SummarySimilarityAnnotationTO;
import org.bgee.model.dao.mysql.MySQLITAncestor;
import org.bgee.model.dao.mysql.annotation.anatsimilarity.MySQLSummarySimilarityAnnotationDAO;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.junit.Test;


/**
 * Integration tests for {@link MySQLSummarySimilarityAnnotationDAO}, performed on a real MySQL 
 * database. See the documentation of {@link org.bgee.model.dao.mysql.MySQLITAncestor} for 
 * important information.
 *
 * @author 	Valentine Rech de Laval
 * @version Bgee 13
 * @since 	Bgee 13
 */
public class MySQLSummarySimilarityAnnotationDAOIT extends MySQLITAncestor {

    private final static Logger log = 
            LogManager.getLogger(MySQLSummarySimilarityAnnotationDAOIT.class.getName());
    
    public MySQLSummarySimilarityAnnotationDAOIT() {
        super();
    }

    @Override
    protected Logger getLogger() {
        return log;
    }

    /**
     * Test the select method 
     * {@link MySQLSummarySimilarityAnnotationDAO#getAllSummarySimilarityAnnotations()}.
     */
    //TODO integration test
    @Test
    public void shouldGetAllSummarySimilarityAnnotations() throws SQLException {
    }
    
    /**
     * Test the insert method 
     * {@link MySQLSummarySimilarityAnnotationDAO#insertSummarySimilarityAnnotations()}.
     */
    @Test
    public void shouldInsertSummarySimilarityAnnotations() throws SQLException {
        
        this.useEmptyDB();

        // Create a Collection of SummarySimilarityAnnotationTO to be inserted,
        // no attribute could be null according database schema.
        Collection<SummarySimilarityAnnotationTO> summaryTOs = Arrays.asList(
                new SummarySimilarityAnnotationTO("1", "11", true, "cioId1"),
                new SummarySimilarityAnnotationTO("2", "22", false, "cioId2"),
                new SummarySimilarityAnnotationTO("3", "33", true, "cioId3"));

        try {
            MySQLSummarySimilarityAnnotationDAO dao = 
                    new MySQLSummarySimilarityAnnotationDAO(this.getMySQLDAOManager());
            assertEquals("Incorrect number of rows inserted", 3, 
                    dao.insertSummarySimilarityAnnotations(summaryTOs));
            
            //we manually verify the insertion, as we do not want to rely on other methods 
            //that are tested elsewhere.
            try (BgeePreparedStatement stmt = this.getMySQLDAOManager().getConnection().
                    prepareStatement("select 1 from summarySimilarityAnnotation " +
                            "where summarySimilarityAnnotationId = ? AND taxonId = ? " +
                            "AND negated = ? AND CIOId = ?")) {
                
                stmt.setInt(1, 1);
                stmt.setInt(2, 11);
                stmt.setBoolean(3, true);
                stmt.setString(4, "cioId1");
                assertTrue("SummarySimilarityAnnotationTO incorrectly inserted", 
                        stmt.getRealPreparedStatement().executeQuery().next());
                
                stmt.setInt(1, 2);
                stmt.setInt(2, 22);
                stmt.setBoolean(3, false);
                stmt.setString(4, "cioId2");
                assertTrue("SummarySimilarityAnnotationTO incorrectly inserted", 
                        stmt.getRealPreparedStatement().executeQuery().next());

                stmt.setInt(1, 3);
                stmt.setInt(2, 33);
                stmt.setBoolean(3, true);
                stmt.setString(4, "cioId3");
                assertTrue("SummarySimilarityAnnotationTO incorrectly inserted", 
                        stmt.getRealPreparedStatement().executeQuery().next());
            }
            
            // We should throw an IllegalArgumentException when 
            // no SummarySimilarityAnnotationTO provided
            try {
                dao.insertSummarySimilarityAnnotations(new HashSet<SummarySimilarityAnnotationTO>());
                // Test failed
                throw new AssertionError("insertSummarySimilarityAnnotations did not " +
                        "throw an IllegalArgumentException as expected");
            } catch (IllegalArgumentException e) {
                // Test passed, do nothing
                log.catching(e);
            }
        } finally {
            this.emptyAndUseDefaultDB();
        }
    }
    
    /**
     * Test the insert method 
     * {@link MySQLSummarySimilarityAnnotationDAO#insertSimilarityAnnotationsToAnatEntityIds()}.
     */
    @Test
    public void shouldInsertSimilarityAnnotationsToAnatEntityIds() throws SQLException {
        this.useEmptyDB();

        // Create a Collection of SimAnnotToAnatEntityTO to be inserted,
        // no attribute could be null according database schema.
        Collection<SimAnnotToAnatEntityTO> insertedTOs = Arrays.asList(
                new SimAnnotToAnatEntityTO("1","10"),
                new SimAnnotToAnatEntityTO("1","14"),
                new SimAnnotToAnatEntityTO("2","14"));

        try {
            MySQLSummarySimilarityAnnotationDAO dao = 
                    new MySQLSummarySimilarityAnnotationDAO(this.getMySQLDAOManager());
            assertEquals("Incorrect number of rows inserted", 3, 
                    dao.insertSimilarityAnnotationsToAnatEntityIds(insertedTOs));
            
            //we manually verify the insertion, as we do not want to rely on other methods 
            //that are tested elsewhere.
            try (BgeePreparedStatement stmt = this.getMySQLDAOManager().getConnection().
                    prepareStatement("SELECT 1 FROM similarityAnnotationToAnatEntityId " +
                            "WHERE summarySimilarityAnnotationId = ? AND anatEntityId = ? ")) {
                
                stmt.setInt(1, 1);
                stmt.setString(2,"10");
                assertTrue("SimAnnotToAnatEntityTO incorrectly inserted", 
                        stmt.getRealPreparedStatement().executeQuery().next());
                
                stmt.setInt(1, 1);
                stmt.setString(2, "14");
                assertTrue("SimAnnotToAnatEntityTO incorrectly inserted", 
                        stmt.getRealPreparedStatement().executeQuery().next());

                stmt.setInt(1, 2);
                stmt.setString(2, "14");
                assertTrue("SimAnnotToAnatEntityTO incorrectly inserted", 
                        stmt.getRealPreparedStatement().executeQuery().next());
            }
            
            // We should throw an IllegalArgumentException when 
            // no SimAnnotToAnatEntityTO provided
            try {
                dao.insertSimilarityAnnotationsToAnatEntityIds(
                        new HashSet<SimAnnotToAnatEntityTO>());
                // Test failed
                throw new AssertionError("insertSimilarityAnnotationsToAnatEntityIds did not " +
                        "throw an IllegalArgumentException as expected");
            } catch (IllegalArgumentException e) {
                // Test passed, do nothing
                log.catching(e);
            }
        } finally {
            this.emptyAndUseDefaultDB();
        }

    }
}
