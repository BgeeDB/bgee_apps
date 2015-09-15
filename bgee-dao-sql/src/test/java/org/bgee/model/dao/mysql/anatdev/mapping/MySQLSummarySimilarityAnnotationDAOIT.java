package org.bgee.model.dao.mysql.anatdev.mapping;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.TOComparator;
import org.bgee.model.dao.api.anatdev.mapping.SummarySimilarityAnnotationDAO;
import org.bgee.model.dao.api.anatdev.mapping.SummarySimilarityAnnotationDAO.SimAnnotToAnatEntityTO;
import org.bgee.model.dao.api.anatdev.mapping.SummarySimilarityAnnotationDAO.SummarySimilarityAnnotationTO;
import org.bgee.model.dao.mysql.MySQLITAncestor;
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
    @Test
    public void shouldGetAllSummarySimilarityAnnotations() throws SQLException {
        this.useSelectDB();
        
        // Generate result with the method
        MySQLSummarySimilarityAnnotationDAO dao = 
                new MySQLSummarySimilarityAnnotationDAO(this.getMySQLDAOManager());
        dao.setAttributes(Arrays.asList(SummarySimilarityAnnotationDAO.Attribute.values()));
        List<SummarySimilarityAnnotationTO> actualResults = 
                dao.getAllSummarySimilarityAnnotations().getAllTOs();

        // Generate manually expected result
        List<SummarySimilarityAnnotationTO> expectedTaxa = Arrays.asList(
                new SummarySimilarityAnnotationTO("527", "111", false, "CIO:3"),
                new SummarySimilarityAnnotationTO("528", "411", false, "CIO:6"),
                new SummarySimilarityAnnotationTO("529", "511", false, "CIO:5"),
                new SummarySimilarityAnnotationTO("530", "311", true, "CIO:6"),
                new SummarySimilarityAnnotationTO("421", "611", false, "CIO:5"),
                new SummarySimilarityAnnotationTO("422", "511", false, "CIO:1"),
                new SummarySimilarityAnnotationTO("1870", "511", false, "CIO:5"));
        // Compare
        assertTrue("SummarySimilarityAnnotationTOs incorrectly retrieved", 
                TOComparator.areTOCollectionsEqual(actualResults, expectedTaxa));

        dao.setAttributes(Arrays.asList(SummarySimilarityAnnotationDAO.Attribute.CIO_ID));
        actualResults = dao.getAllSummarySimilarityAnnotations().getAllTOs();
        
        // Generate manually expected result
        expectedTaxa = Arrays.asList(
                new SummarySimilarityAnnotationTO(null, null, null, "CIO:1"),
                new SummarySimilarityAnnotationTO(null, null, null, "CIO:3"),
                new SummarySimilarityAnnotationTO(null, null, null, "CIO:5"),
                new SummarySimilarityAnnotationTO(null, null, null, "CIO:6"));
        assertTrue("SummarySimilarityAnnotationTOs incorrectly retrieved",
                TOComparator.areTOCollectionsEqual(actualResults, expectedTaxa));
    }

    /**
     * Test the select method 
     * {@link MySQLSummarySimilarityAnnotationDAO#getSummarySimilarityAnnotations()}.
     */
    @Test
    public void shouldGetSummarySimilarityAnnotations() throws SQLException {
        this.useSelectDB();
        
        // Generate result with the method
        MySQLSummarySimilarityAnnotationDAO dao = 
                new MySQLSummarySimilarityAnnotationDAO(this.getMySQLDAOManager());
        dao.setAttributes(Arrays.asList(SummarySimilarityAnnotationDAO.Attribute.values()));
        List<SummarySimilarityAnnotationTO> actualResults = 
                dao.getSummarySimilarityAnnotations("511").getAllTOs();

        // Generate manually expected result
        List<SummarySimilarityAnnotationTO> expectedTaxa = Arrays.asList(
                new SummarySimilarityAnnotationTO("527", "111", false, "CIO:3"),
                new SummarySimilarityAnnotationTO("529", "511", false, "CIO:5"),
                new SummarySimilarityAnnotationTO("530", "311", true, "CIO:6"),
                new SummarySimilarityAnnotationTO("422", "511", false, "CIO:1"),
                new SummarySimilarityAnnotationTO("1870", "511", false, "CIO:5"));
        // Compare
        assertTrue("SummarySimilarityAnnotationTOs incorrectly retrieved", 
                TOComparator.areTOCollectionsEqual(actualResults, expectedTaxa));

        dao.setAttributes(Arrays.asList(SummarySimilarityAnnotationDAO.Attribute.TAXON_ID));
        actualResults = dao.getSummarySimilarityAnnotations("411").getAllTOs();        
        // Generate manually expected result
        expectedTaxa = Arrays.asList(
                new SummarySimilarityAnnotationTO(null, "111", null, null),
                new SummarySimilarityAnnotationTO(null, "411", null, null),
                new SummarySimilarityAnnotationTO(null, "311", null, null));
        assertTrue("SummarySimilarityAnnotationTOs incorrectly retrieved",
                TOComparator.areTOCollectionsEqual(actualResults, expectedTaxa));
    }

    /**
     * Test the select method 
     * {@link MySQLSummarySimilarityAnnotationDAO#getSimAnnotToAnatEntity()}.
     */
    @Test
    public void shouldGetSimAnnotToAnatEntity() throws SQLException {
        this.useSelectDB();
        
        // Generate result with the method
        MySQLSummarySimilarityAnnotationDAO dao = 
                new MySQLSummarySimilarityAnnotationDAO(this.getMySQLDAOManager());
        dao.setAttributes(Arrays.asList(SummarySimilarityAnnotationDAO.Attribute.values()));
        List<SimAnnotToAnatEntityTO> actualResults = 
                dao.getSimAnnotToAnatEntity("511", null).getAllTOs();

        // Generate manually expected result
        List<SimAnnotToAnatEntityTO> expectedTaxa = Arrays.asList(
                new SimAnnotToAnatEntityTO("422", "UBERON:0001687"),
                new SimAnnotToAnatEntityTO("422", "UBERON:0011606"),
                //we keep the largest mapping possible only
                // new SimAnnotToAnatEntityTO("1870", "UBERON:0011606"), 
                //if an anatomical entity is used in annotations, 
                //we keep most recent valid taxon only
                // new SimAnnotToAnatEntityTO("527", "UBERON:0001853"),
                //not in provided taxon
                // new SimAnnotToAnatEntityTO("421", "UBERON:0001687"),
                // new SimAnnotToAnatEntityTO("528", "UBERON:0001853"),
                //we retrieve valid annotation only
                // new SimAnnotToAnatEntityTO("530", "UBERON:0001853"),
                new SimAnnotToAnatEntityTO("529", "UBERON:0001853"));
        // Compare
        assertTrue("SimAnnotToAnatEntityTOs incorrectly retrieved", 
                TOComparator.areTOCollectionsEqual(actualResults, expectedTaxa));

        Set<String> speciesIDs = new HashSet<String>(Arrays.asList("41"));
        dao.setAttributes(Arrays.asList(SummarySimilarityAnnotationDAO.Attribute.TAXON_ID));
        actualResults = dao.getSimAnnotToAnatEntity("511", speciesIDs).getAllTOs();        
        // Generate manually expected result
        expectedTaxa = Arrays.asList(
                 new SimAnnotToAnatEntityTO("422", "UBERON:0011606"),
                 new SimAnnotToAnatEntityTO("529", "UBERON:0001853"));

        assertTrue("SimAnnotToAnatEntityTOs incorrectly retrieved",
                TOComparator.areTOCollectionsEqual(actualResults, expectedTaxa));
    }

    /**
     * Test the select method 
     * {@link MySQLSummarySimilarityAnnotationDAO#getSimAnnotToLostAnatEntity()}.
     */
    @Test
    public void shouldGetSimAnnotToLostAnatEntity() throws SQLException {
        this.useSelectDB();
        
        Set<String> speciesIDs = new HashSet<String>();
        
        // Generate result with the method
        MySQLSummarySimilarityAnnotationDAO dao = 
                new MySQLSummarySimilarityAnnotationDAO(this.getMySQLDAOManager());
        dao.setAttributes(Arrays.asList(SummarySimilarityAnnotationDAO.Attribute.values()));

        try {
            dao.getSimAnnotToLostAnatEntity("511", null).getAllTOs();
            // Test failed
            fail("No IllegalArgumentException was thrown while speciesIDs is null"); 
        } catch (IllegalArgumentException e) {
            // Test passed
        }

        speciesIDs.addAll(Arrays.asList("41"));
        dao.setAttributes(Arrays.asList(SummarySimilarityAnnotationDAO.Attribute.TAXON_ID));
        List<SimAnnotToAnatEntityTO> actualResults = 
                dao.getSimAnnotToLostAnatEntity("511", speciesIDs).getAllTOs();        
        // Generate manually expected result
        List<SimAnnotToAnatEntityTO> expectedTaxa = Arrays.asList(
                new SimAnnotToAnatEntityTO("422", "UBERON:0001687"));

        assertTrue("SimAnnotToAnatEntityTOs incorrectly retrieved",
                TOComparator.areTOCollectionsEqual(actualResults, expectedTaxa));
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
