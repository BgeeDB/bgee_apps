package org.bgee.model.dao.mysql.anatdev.mapping;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.TOComparator;
import org.bgee.model.dao.api.anatdev.mapping.RawSimilarityAnnotationDAO;
import org.bgee.model.dao.api.anatdev.mapping.RawSimilarityAnnotationDAO.RawSimilarityAnnotationTO;
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
     * Test the select method 
     * {@link MySQLRawSimilarityAnnotationDAO#getAllRawSimilarityAnnotations()}.
     */
    @Test
    public void shouldGetAllRawSimilarityAnnotations() throws SQLException {
        this.useSelectDB();
        
        MySQLRawSimilarityAnnotationDAO dao = 
                new MySQLRawSimilarityAnnotationDAO(this.getMySQLDAOManager());
        dao.setAttributes(Arrays.asList(RawSimilarityAnnotationDAO.Attribute.values()));
        
        List<RawSimilarityAnnotationTO> actualResults = 
                dao.getAllRawSimilarityAnnotations().getAllTOs();
        List<RawSimilarityAnnotationTO> expectedResults = Arrays.asList(
                new RawSimilarityAnnotationTO("527", false, "ECO:1", "CIO:2", "ISBN:978-0030223693", 
                        "Liem KF", "Text2", "bgee", "ANN", asDate(2015, Month.MARCH, 30)),
                new RawSimilarityAnnotationTO("527", true, "ECO:2", "CIO:6", "PMID:19786082", 
                        "Manley GA", "Text1", "bgee", "ANN", asDate(2015, Month.MARCH, 30)),
                new RawSimilarityAnnotationTO("528", false, "ECO:2", "CIO:6", "PMID:19786082", 
                        "Manley GA", "Text1", "bgee", "ANN", asDate(2015, Month.MARCH, 30)),
                new RawSimilarityAnnotationTO("529", false, "ECO:2", "CIO:5", "PMID:19786082", 
                        "Manley GA", "Text1", "bgee", "ANN", asDate(2015, Month.MARCH, 30)),
                new RawSimilarityAnnotationTO("530", true, "ECO:2", "CIO:6", "PMID:19786082", 
                        "Manley GA", "Text1", "bgee", "ANN", asDate(2015, Month.MARCH, 30)),
                new RawSimilarityAnnotationTO("421", false, "ECO:2", "CIO:5", "PMID:22686855", 
                        "Anthwal N", "Text3", "bgee", "ANN", asDate(2015, Month.APRIL, 01)),
                new RawSimilarityAnnotationTO("422", false, "ECO:3", "CIO:5", "DOI:10.1017/S0022215100009087", 
                        "Gerrie J", "Text4", "bgee", "ANN", asDate(2013, Month.JULY, 04)),
                new RawSimilarityAnnotationTO("422", false, "ECO:4", "CIO:5", "PMID:19786082", 
                        "Manley GA", "Text5", "bgee", "ANN", asDate(2013, Month.JULY, 04)),
                new RawSimilarityAnnotationTO("422", false, "ECO:5", "CIO:5", "PMID:19786082", 
                        "Manley GA", "Text5", "bgee", "ANN", asDate(2013, Month.JULY, 04)),
                new RawSimilarityAnnotationTO("422", false, "ECO:2", "CIO:5", "PMID:19786082", 
                        "Manley GA", "Text5", "bgee", "ANN", asDate(2013, Month.JULY, 04)),
                new RawSimilarityAnnotationTO("1870", false, "ECO:2", "CIO:5", "DOI:10.1017/S0022215100009087", 
                        "Gerrie J", "Text4", "bgee", "ANN", asDate(2015, Month.APRIL, 02)));
        assertTrue("RawSimilarityAnnotationTOs incorrectly retrieved", 
                TOComparator.areTOCollectionsEqual(actualResults, expectedResults));

        dao.setAttributes(Arrays.asList(RawSimilarityAnnotationDAO.Attribute.ECO_ID, 
                RawSimilarityAnnotationDAO.Attribute.CIO_ID));
        actualResults = dao.getAllRawSimilarityAnnotations().getAllTOs();
        expectedResults = Arrays.asList(
                new RawSimilarityAnnotationTO(null, null, "ECO:1", "CIO:2", null, null, null, null, null, null),
                new RawSimilarityAnnotationTO(null, null, "ECO:2", "CIO:6", null, null, null, null, null, null),
                new RawSimilarityAnnotationTO(null, null, "ECO:2", "CIO:5", null, null, null, null, null, null),
                new RawSimilarityAnnotationTO(null, null, "ECO:3", "CIO:5", null, null, null, null, null, null),
                new RawSimilarityAnnotationTO(null, null, "ECO:4", "CIO:5", null, null, null, null, null, null),
                new RawSimilarityAnnotationTO(null, null, "ECO:5", "CIO:5", null, null, null, null, null, null));
        assertTrue("RawSimilarityAnnotationTOs incorrectly retrieved",
                TOComparator.areTOCollectionsEqual(actualResults, expectedResults));
    }

    /**
     * Obtains an instance of Date from provided {@code year}, {@code month}, and {@code day}.
     * 
     * @param year  An {@code int} that is the year to be used.
     * @param month A {@code Month} that is the month-of-year to be used.
     * @param day   An {@code int} that is the day to be used.
     * @return      the {@code java.util.Date} representing the date of provided {@code year}, 
     *              {@code month}, and {@code day}.
     */
    private static java.util.Date asDate(int year, Month month, int day) {
        LocalDate localDate = LocalDate.of(year, month, day);
        return java.util.Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
    }
    
    /**
     * Test the insert method 
     * {@link MySQLRawSimilarityAnnotationDAO#insertRawSimilarityAnnotations()}.
     */
    @Test
    public void shouldInsertRawSimilarityAnnotations() throws SQLException {
        
        this.useEmptyDB();

        // Create a Collection of RawSimilarityAnnotationTO to be inserted,
        // no attribute could be null according database schema.
        java.sql.Date date1 = new java.sql.Date(12345);
        java.sql.Date date2 = new java.sql.Date(12349);
        Collection<RawSimilarityAnnotationTO> rawTOs = Arrays.asList(
                new RawSimilarityAnnotationTO("1", true, 
                        "ecoId1", "cioId1", "referenceId1", "referenceTitle1", 
                        "supportingText1", "assignedBy1", "curator1", date1),
                new RawSimilarityAnnotationTO("2", false, 
                        "ecoId2", "cioId2", "referenceId2", "referenceTitle2", 
                        "supportingText2", "assignedBy2", "curator2", date2),
                new RawSimilarityAnnotationTO("2", false, 
                        "ecoId3", "cioId3", null, null, null, null, null, null));

        try {
            MySQLRawSimilarityAnnotationDAO dao = 
                    new MySQLRawSimilarityAnnotationDAO(this.getMySQLDAOManager());
            assertEquals("Incorrect number of rows inserted", 3, 
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
            
            try (BgeePreparedStatement stmt = this.getMySQLDAOManager().getConnection().
                    prepareStatement("SELECT 1 FROM rawSimilarityAnnotation " +
                            "WHERE summarySimilarityAnnotationId = ? AND negated = ? " +
                            "AND ECOId = ? AND CIOId = ? AND referenceId = ? AND referenceTitle = ? " +
                            "AND supportingText = ? AND assignedBy = ? AND curator = ? " +
                            "AND annotationDate is null")) {
                
                stmt.setInt(1, 2);
                stmt.setBoolean(2, false);
                stmt.setString(3, "ecoId3");
                stmt.setString(4, "cioId3");
                stmt.setString(5, "");
                stmt.setString(6, "");
                stmt.setString(7, "");
                stmt.setString(8, "");
                stmt.setString(9, "");
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
