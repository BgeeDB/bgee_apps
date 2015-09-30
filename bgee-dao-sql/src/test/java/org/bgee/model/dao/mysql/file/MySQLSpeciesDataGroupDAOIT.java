package org.bgee.model.dao.mysql.file;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.TOComparator;
import org.bgee.model.dao.api.file.SpeciesDataGroupDAO;
import org.bgee.model.dao.api.file.SpeciesDataGroupDAO.SpeciesDataGroupTO;
import org.bgee.model.dao.api.file.SpeciesDataGroupDAO.SpeciesToDataGroupTO;
import org.bgee.model.dao.mysql.MySQLITAncestor;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.junit.Test;

/**
 * The integration tests for the {@link MySQLSpeciesDataGroupDAO} class.
 * 
 * @author Philippe Moret
 * @author Valentine Rech de Laval
 */
public class MySQLSpeciesDataGroupDAOIT extends MySQLITAncestor {

    private static final Logger log = LogManager.getLogger(MySQLSpeciesDataGroupDAOIT.class.getName());

    @Override
    protected Logger getLogger() {
        return log;
    }

    /**
     * Test the {@link MySQLSpeciesDataGroupDAO#getAllSpeciesDataGroup()} method
     * @throws SQLException
     */
    @Test
    public void testGetAllSpeciesDataGroups() throws SQLException {
        super.useSelectDB();
        MySQLSpeciesDataGroupDAO dao = new MySQLSpeciesDataGroupDAO(getMySQLDAOManager());

        List<SpeciesDataGroupDAO.SpeciesDataGroupTO> list = dao.getAllSpeciesDataGroup().getAllTOs();
        List<SpeciesDataGroupDAO.SpeciesDataGroupTO> expected = Arrays.asList(
                new SpeciesDataGroupDAO.SpeciesDataGroupTO("1", "SingleSpecies1", "SS1 is a ..."),
                new SpeciesDataGroupDAO.SpeciesDataGroupTO("2", "MultiSpecies2", "A multi species group...")
        );

        assertTrue("SpeciesDataGroupTOs are incorrectly retrieved\nGOT\n"+list+"\nEXPECTED\n"+expected,
                TOComparator.areTOCollectionsEqual(list, expected));
        assertEquals(list, expected);

        dao.setAttributes(new SpeciesDataGroupDAO.Attribute[]{
                SpeciesDataGroupDAO.Attribute.ID, SpeciesDataGroupDAO.Attribute.DESCRIPTION});
        List<SpeciesDataGroupDAO.SpeciesDataGroupTO> list2 = dao.getAllSpeciesDataGroup().getAllTOs();
        List<SpeciesDataGroupDAO.SpeciesDataGroupTO> expected2 = Arrays.asList(
                new SpeciesDataGroupDAO.SpeciesDataGroupTO("1", null , "SS1 is a ..."),
                new SpeciesDataGroupDAO.SpeciesDataGroupTO("2", null, "A multi species group...")
        );

        assertTrue("SpeciesDataGroupTOs are incorrectly retrieved\nGOT\n"+list2+"\nEXPECTED\n"+expected2,
                TOComparator.areTOCollectionsEqual(list2, expected2));
        assertEquals(list2, expected2);
    }

    /**
     * Test the {@link MySQLSpeciesDataGroupDAO#getAllSpeciesToDataGroup()} method
     * @throws SQLException
     */
    @Test
    public void testGetAllSpeciesToDataGroup() throws SQLException {
        super.useSelectDB();

        MySQLSpeciesDataGroupDAO dao = new MySQLSpeciesDataGroupDAO(getMySQLDAOManager());

        List<SpeciesToDataGroupTO> list = dao.getAllSpeciesToDataGroup().getAllTOs();
        List<SpeciesToDataGroupTO> expected = Arrays.asList(
                new SpeciesToDataGroupTO("11", "1"),
                new SpeciesToDataGroupTO("21", "2"),
                new SpeciesToDataGroupTO("31", "2")
        );

        assertTrue("SpeciesToDataGroupTOs are incorrectly retrieved\nGOT\n" + list + 
                "\nEXPECTED\n" + expected, TOComparator.areTOCollectionsEqual(list, expected));
    }

    /**
     * Test the {@link MySQLDownloadFileDAO#insertSpeciesDataGroups()} method.
     * 
     * @throws SQLException if an error happens with the MySQL database
     */
    @Test
    public void testInsertSpeciesDataGroups() throws SQLException {
        this.useEmptyDB();
        
        //create a Collection of SpeciesDataGroupTOs to be inserted
        Collection<SpeciesDataGroupTO> groupTOs = Arrays.asList(
                new SpeciesDataGroupTO("101", "sdg name 1", "sdg desc 1"),
                new SpeciesDataGroupTO("102", "sdg name 2", "sdg desc 2"));
        try {
            MySQLSpeciesDataGroupDAO dao = new MySQLSpeciesDataGroupDAO(this.getMySQLDAOManager());
            assertEquals("Incorrect number of rows inserted", 2, 
                    dao.insertSpeciesDataGroups(groupTOs));
            
            //we manually verify the insertion, as we do not want to rely on other methods 
            //that are tested elsewhere.
            //This test method could be better written (DRY, ...)
            try (BgeePreparedStatement stmt = this.getMySQLDAOManager().getConnection().
                    prepareStatement("SELECT 1 FROM speciesDataGroup WHERE speciesDataGroupId = ? "
                            + "AND speciesDataGroupName = ? AND speciesDataGroupDescription = ?")) {
                
                stmt.setInt(1, 101);
                stmt.setString(2, "sdg name 1");
                stmt.setString(3, "sdg desc 1");
                assertTrue("SpeciesDataGroupTO incorrectly inserted", 
                        stmt.getRealPreparedStatement().executeQuery().next());
                
                stmt.setInt(1, 102);
                stmt.setString(2, "sdg name 2");
                stmt.setString(3, "sdg desc 2");
                assertTrue("SpeciesDataGroupTO incorrectly inserted", 
                        stmt.getRealPreparedStatement().executeQuery().next());
            }
            
            try {                
                dao.insertSpeciesDataGroups(new HashSet<SpeciesDataGroupTO>());
                fail("No IllegalArgumentException was thrown while no SpeciesDataGroupTO was provided"); 
            } catch (IllegalArgumentException e) {
                // Test passed
            }
        } finally {
            this.emptyAndUseDefaultDB();
        }
    }
    
    /**
     * Test the {@link MySQLDownloadFileDAO#insertSpeciesToDataGroup()} method.
     * 
     * @throws SQLException if an error happens with the MySQL database
     */
    @Test
    public void testInsertSpeciesToDataGroup() throws SQLException {
        this.useEmptyDB();
        
        //create a Collection of SpeciesToDataGroupTOs to be inserted
        Collection<SpeciesToDataGroupTO> mappingTOs = Arrays.asList(
                new SpeciesToDataGroupTO("11", "101"),
                new SpeciesToDataGroupTO("22", "101"),
                new SpeciesToDataGroupTO("11", "102"),
                new SpeciesToDataGroupTO("33", "102"));
        try {
            MySQLSpeciesDataGroupDAO dao = new MySQLSpeciesDataGroupDAO(this.getMySQLDAOManager());
            assertEquals("Incorrect number of rows inserted", 4, 
                    dao.insertSpeciesToDataGroup(mappingTOs));
            
            //we manually verify the insertion, as we do not want to rely on other methods 
            //that are tested elsewhere.
            //This test method could be better written (DRY, ...)
            try (BgeePreparedStatement stmt = this.getMySQLDAOManager().getConnection().
                    prepareStatement("SELECT 1 FROM speciesToDataGroup "
                            + "WHERE speciesDataGroupId = ? AND speciesId = ?")) {
                
                stmt.setString(1, "101");
                stmt.setString(2, "11");
                assertTrue("SpeciesToDataGroupTO incorrectly inserted", 
                        stmt.getRealPreparedStatement().executeQuery().next());
                
                stmt.setString(1, "101");
                stmt.setString(2, "22");
                assertTrue("SpeciesToDataGroupTO incorrectly inserted", 
                        stmt.getRealPreparedStatement().executeQuery().next());
                
                stmt.setString(1, "102");
                stmt.setString(2, "11");
                assertTrue("SpeciesToDataGroupTO incorrectly inserted", 
                        stmt.getRealPreparedStatement().executeQuery().next());
                
                stmt.setString(1, "102");
                stmt.setString(2, "33");
                assertTrue("SpeciesToDataGroupTO incorrectly inserted", 
                        stmt.getRealPreparedStatement().executeQuery().next());
            }
            
            try {
                dao.insertSpeciesToDataGroup(new HashSet<SpeciesToDataGroupTO>());
                fail("No IllegalArgumentException was thrown while no SpeciesToDataGroupTO was provided"); 
            } catch (IllegalArgumentException e) {
                // Test passed
            }
        } finally {
            this.emptyAndUseDefaultDB();
        }
    }
}
