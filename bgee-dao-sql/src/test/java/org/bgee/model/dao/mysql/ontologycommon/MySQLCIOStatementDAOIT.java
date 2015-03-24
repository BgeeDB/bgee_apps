package org.bgee.model.dao.mysql.ontologycommon;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.TOComparator;
import org.bgee.model.dao.api.ontologycommon.CIOStatementDAO;
import org.bgee.model.dao.api.ontologycommon.CIOStatementDAO.CIOStatementTO;
import org.bgee.model.dao.api.ontologycommon.CIOStatementDAO.CIOStatementTO.ConfidenceLevel;
import org.bgee.model.dao.api.ontologycommon.CIOStatementDAO.CIOStatementTO.EvidenceConcordance;
import org.bgee.model.dao.api.ontologycommon.CIOStatementDAO.CIOStatementTO.EvidenceTypeConcordance;
import org.bgee.model.dao.mysql.MySQLITAncestor;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.junit.Test;


/**
 * Integration tests for {@link MySQLCIOStatementDAO}, performed on a real MySQL database. 
 * See the documentation of {@link org.bgee.model.dao.mysql.MySQLITAncestor} for 
 * important information.
 *
 * @author 	Valentine Rech de Laval
 * @version Bgee 13
 * @since 	Bgee 13
 */
public class MySQLCIOStatementDAOIT extends MySQLITAncestor {

    private final static Logger log = 
            LogManager.getLogger(MySQLCIOStatementDAOIT.class.getName());
    
    public MySQLCIOStatementDAOIT() {
        super();
    }

    @Override
    protected Logger getLogger() {
        return log;
    }

    /**
     * Test the insert method {@link MySQLCIOStatementDAO#getAllCIOStatements(Collection<CIOStatementTO>)}.
     */
    @Test
    public void shouldGetAllCIOStatements() throws SQLException {

        this.useSelectDB();
        
        // Generate result with the method
        MySQLCIOStatementDAO dao = new MySQLCIOStatementDAO(this.getMySQLDAOManager());
        dao.setAttributes(Arrays.asList(CIOStatementDAO.Attribute.values()));
        List<CIOStatementTO> methCIOStmt = dao.getAllCIOStatements().getAllTOs();

        // Generate manually expected result
        List<CIOStatementTO> expectedCIOStmt = Arrays.asList(
                new CIOStatementTO("1", "name1", "desc1", true, ConfidenceLevel.HIGH_CONFIDENCE, 
                        EvidenceConcordance.CONGRUENT, EvidenceTypeConcordance.SAME_TYPE),
                new CIOStatementTO("2", "name2", "desc2", false, ConfidenceLevel.LOW_CONFIDENCE, 
                        EvidenceConcordance.SINGLE_EVIDENCE, null),
                new CIOStatementTO("3", "name3", null, true, ConfidenceLevel.MEDIUM_CONFIDENCE, 
                        EvidenceConcordance.STRONGLY_CONFLICTING, EvidenceTypeConcordance.DIFFERENT_TYPE),
                new CIOStatementTO("4", "name4", "desc4", false, ConfidenceLevel.MEDIUM_CONFIDENCE, 
                        EvidenceConcordance.WEAKLY_CONFLICTING, EvidenceTypeConcordance.SAME_TYPE),
                new CIOStatementTO("5", "name5", null, true, ConfidenceLevel.HIGH_CONFIDENCE, 
                        EvidenceConcordance.SINGLE_EVIDENCE, null));

        //Compare
        assertTrue("CIOStatementTOs incorrectly retrieved", 
                TOComparator.areTOCollectionsEqual(methCIOStmt, expectedCIOStmt));

        // without declared attribute should return same TOs that with all attributes 
        dao.clearAttributes();
        methCIOStmt = dao.getAllCIOStatements().getAllTOs();
        //Compare
        assertTrue("CIOStatementTOs incorrectly retrieved", 
                TOComparator.areTOCollectionsEqual(methCIOStmt, expectedCIOStmt));

        // Generate manually expected result 
        // Check distinct
        dao.setAttributes(Arrays.asList(
                CIOStatementDAO.Attribute.TRUSTED, CIOStatementDAO.Attribute.CONFIDENCE_LEVEL));
        methCIOStmt = dao.getAllCIOStatements().getAllTOs();
        expectedCIOStmt = Arrays.asList(
                new CIOStatementTO(null, null, null, true, ConfidenceLevel.HIGH_CONFIDENCE, null, null),
                new CIOStatementTO(null, null, null, false, ConfidenceLevel.LOW_CONFIDENCE, null, null),
                new CIOStatementTO(null, null, null, true, ConfidenceLevel.MEDIUM_CONFIDENCE, null, null),
                new CIOStatementTO(null, null, null, false, ConfidenceLevel.MEDIUM_CONFIDENCE, null, null));
        //Compare
        assertTrue("CIOStatementTOs incorrectly retrieved", 
                TOComparator.areTOCollectionsEqual(methCIOStmt, expectedCIOStmt));
    }

    /**
     * Test the insert method 
     * {@link MySQLCIOStatementDAO#insertCIOStatements(Collection<CIOStatementTO>)}.
     */
    @Test
    public void shouldInsertCIOStatements() throws SQLException {
        
        this.useEmptyDB();

        // Create a Collection of CIOStatementTO to be inserted,
        // Only the attribute description could be null according database schema.
        Collection<CIOStatementTO> cioTOs = Arrays.asList(
                new CIOStatementTO("1", "name1", "desc1", true, ConfidenceLevel.HIGH_CONFIDENCE, 
                        EvidenceConcordance.CONGRUENT, EvidenceTypeConcordance.SAME_TYPE),
                new CIOStatementTO("2", "name2", "desc2", false, ConfidenceLevel.LOW_CONFIDENCE, 
                        EvidenceConcordance.SINGLE_EVIDENCE, null),
                new CIOStatementTO("3", "name3", null, true, ConfidenceLevel.MEDIUM_CONFIDENCE, 
                        EvidenceConcordance.STRONGLY_CONFLICTING, EvidenceTypeConcordance.DIFFERENT_TYPE),
                new CIOStatementTO("4", "name4", "desc4", false, ConfidenceLevel.MEDIUM_CONFIDENCE, 
                        EvidenceConcordance.WEAKLY_CONFLICTING, EvidenceTypeConcordance.SAME_TYPE),
                new CIOStatementTO("5", "name5", null, true, ConfidenceLevel.HIGH_CONFIDENCE, 
                        EvidenceConcordance.SINGLE_EVIDENCE, null));

        try {
            MySQLCIOStatementDAO dao = new MySQLCIOStatementDAO(this.getMySQLDAOManager());
            assertEquals("Incorrect number of rows inserted", 5, 
                    dao.insertCIOStatements(cioTOs));
            
            //we manually verify the insertion, as we do not want to rely on other methods 
            //that are tested elsewhere.
            // No null attribute
            try (BgeePreparedStatement stmt = this.getMySQLDAOManager().getConnection().
                    prepareStatement("SELECT 1 FROM CIOStatement " +
                            "WHERE CIOId = ? AND CIOName = ? AND CIODescription = ? " +
                            "AND trusted = ? AND confidenceLevel = ? " +
                            "AND evidenceConcordance = ? AND evidenceTypeConcordance = ?")) { 
                stmt.setInt(1, 1);
                stmt.setString(2, "name1");
                stmt.setString(3, "desc1");
                stmt.setBoolean(4, true);
                stmt.setEnumDAOField(5, ConfidenceLevel.HIGH_CONFIDENCE);
                stmt.setEnumDAOField(6, EvidenceConcordance.CONGRUENT);
                stmt.setEnumDAOField(7, EvidenceTypeConcordance.SAME_TYPE);
                assertTrue("CIOStatementTO incorrectly inserted", 
                        stmt.getRealPreparedStatement().executeQuery().next());
                
                stmt.setInt(1, 4);
                stmt.setString(2, "name4");
                stmt.setString(3, "desc4");
                stmt.setBoolean(4, false);
                stmt.setEnumDAOField(5, ConfidenceLevel.MEDIUM_CONFIDENCE);
                stmt.setEnumDAOField(6, EvidenceConcordance.WEAKLY_CONFLICTING);
                stmt.setEnumDAOField(7, EvidenceTypeConcordance.SAME_TYPE);
                assertTrue("CIOStatementTO incorrectly inserted", 
                        stmt.getRealPreparedStatement().executeQuery().next());
            }

            // Only description attribute is null
            try (BgeePreparedStatement stmt = this.getMySQLDAOManager().getConnection().
                    prepareStatement("SELECT 1 FROM CIOStatement " +
                            "WHERE CIOId = ? AND CIOName = ? " +
                            "AND CIODescription IS NULL AND trusted = ? AND confidenceLevel = ? " +
                            "AND evidenceConcordance = ? AND evidenceTypeConcordance = ?")) {
                stmt.setInt(1, 3);
                stmt.setString(2, "name3");
                stmt.setBoolean(3, true);
                stmt.setEnumDAOField(4, ConfidenceLevel.MEDIUM_CONFIDENCE);
                stmt.setEnumDAOField(5, EvidenceConcordance.STRONGLY_CONFLICTING);
                stmt.setEnumDAOField(6, EvidenceTypeConcordance.DIFFERENT_TYPE);
                assertTrue("CIOStatementTO incorrectly inserted", 
                        stmt.getRealPreparedStatement().executeQuery().next());
            }

            // Only evidenceTypeConcordance attribute is null
            try (BgeePreparedStatement stmt = this.getMySQLDAOManager().getConnection().
                    prepareStatement("SELECT 1 FROM CIOStatement " +
                            "WHERE CIOId = ? AND CIOName = ? " +
                            "AND CIODescription = ? AND trusted = ? AND confidenceLevel = ? " +
                            "AND evidenceConcordance = ? AND evidenceTypeConcordance IS NULL")) {
                stmt.setInt(1, 2);
                stmt.setString(2, "name2");
                stmt.setString(3, "desc2");
                stmt.setBoolean(4, false);
                stmt.setEnumDAOField(5, ConfidenceLevel.LOW_CONFIDENCE);
                stmt.setEnumDAOField(6, EvidenceConcordance.SINGLE_EVIDENCE);
                assertTrue("CIOStatementTO incorrectly inserted", 
                        stmt.getRealPreparedStatement().executeQuery().next());
            }
            
            // description and evidenceTypeConcordance attributes are null
            try (BgeePreparedStatement stmt = this.getMySQLDAOManager().getConnection().
                    prepareStatement("SELECT 1 FROM CIOStatement " +
                            "WHERE CIOId = ? AND CIOName = ? " +
                            "AND CIODescription IS NULL AND trusted = ? AND confidenceLevel = ? " +
                            "AND evidenceConcordance = ? AND evidenceTypeConcordance IS NULL")) {
                stmt.setInt(1, 5);
                stmt.setString(2, "name5");
                stmt.setBoolean(3, true);
                stmt.setEnumDAOField(4, ConfidenceLevel.HIGH_CONFIDENCE);
                stmt.setEnumDAOField(5, EvidenceConcordance.SINGLE_EVIDENCE);
                assertTrue("CIOStatementTO incorrectly inserted", 
                        stmt.getRealPreparedStatement().executeQuery().next());
            }
            
            // We should throw an IllegalArgumentException when no CIOStatementTO provided
            try {
                dao.insertCIOStatements(new HashSet<CIOStatementTO>());
                // Test failed
                throw new AssertionError("insertCIOStatements did not throw " +
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
