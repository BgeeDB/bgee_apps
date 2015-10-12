package org.bgee.model.dao.mysql.file;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.OrderingDAO;
import org.bgee.model.dao.api.TOComparator;
import org.bgee.model.dao.api.file.SpeciesDataGroupDAO;
import org.bgee.model.dao.api.file.SpeciesDataGroupDAO.SpeciesDataGroupTO;
import org.bgee.model.dao.api.file.SpeciesDataGroupDAO.SpeciesToDataGroupTO;
import org.bgee.model.dao.api.file.SpeciesDataGroupDAO.SpeciesToGroupOrderingAttribute;
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

        //no atributes nor ordering attributes
        Collection<SpeciesDataGroupDAO.SpeciesDataGroupTO> actual = dao.getAllSpeciesDataGroup(null, null).getAllTOs();
        Collection<SpeciesDataGroupDAO.SpeciesDataGroupTO> expected = Arrays.asList(
                new SpeciesDataGroupDAO.SpeciesDataGroupTO("1", "SingleSpecies1", "SS1 is a ...", 1),
                new SpeciesDataGroupDAO.SpeciesDataGroupTO("2", "MultiSpecies2", "A multi species group...", 2)
        );
        assertTrue("SpeciesDataGroupTOs are incorrectly retrieved\nGOT\n"+actual+"\nEXPECTED\n"+expected,
                TOComparator.areTOCollectionsEqual(actual, expected));

        //some attributes, no ordering
        actual = dao.getAllSpeciesDataGroup(
                Arrays.asList(SpeciesDataGroupDAO.Attribute.ID, SpeciesDataGroupDAO.Attribute.DESCRIPTION), 
                null).getAllTOs();
        expected = Arrays.asList(
                new SpeciesDataGroupDAO.SpeciesDataGroupTO("1", null , "SS1 is a ...", null),
                new SpeciesDataGroupDAO.SpeciesDataGroupTO("2", null, "A multi species group...", null)
        );
        assertTrue("SpeciesDataGroupTOs are incorrectly retrieved\nGOT\n"+actual+"\nEXPECTED\n"+expected,
                TOComparator.areTOCollectionsEqual(actual, expected));

        //some attributes, some ordering
        LinkedHashMap<SpeciesDataGroupDAO.OrderingAttribute, OrderingDAO.Direction> orderAttrs = 
                new LinkedHashMap<>();
        orderAttrs.put(SpeciesDataGroupDAO.OrderingAttribute.PREFERRED_ORDER, OrderingDAO.Direction.DESC);
        List<SpeciesDataGroupDAO.SpeciesDataGroupTO> actualList = dao.getAllSpeciesDataGroup(
                Arrays.asList(SpeciesDataGroupDAO.Attribute.NAME, SpeciesDataGroupDAO.Attribute.PREFERRED_ORDER), 
                orderAttrs).getAllTOs();
        List<SpeciesDataGroupDAO.SpeciesDataGroupTO> expectedList = Arrays.asList(
                new SpeciesDataGroupDAO.SpeciesDataGroupTO(null, "MultiSpecies2", null, 2), 
                new SpeciesDataGroupDAO.SpeciesDataGroupTO(null, "SingleSpecies1" , null, 1)
        );
        assertTrue("SpeciesDataGroupTOs are incorrectly retrieved\nGOT\n"+actualList+"\nEXPECTED\n"+expectedList,
                TOComparator.areTOCollectionsEqual(actualList, expectedList));
        assertEquals("Incorrect ordering of results", expectedList, actualList);
    }

    /**
     * Test the {@link MySQLSpeciesDataGroupDAO#getAllSpeciesToDataGroup()} method
     * @throws SQLException
     */
    @Test
    public void testGetAllSpeciesToDataGroup() throws SQLException {
        super.useSelectDB();

        MySQLSpeciesDataGroupDAO dao = new MySQLSpeciesDataGroupDAO(getMySQLDAOManager(), "11");

        //no ordering requested
        Collection<SpeciesToDataGroupTO> actual = dao.getAllSpeciesToDataGroup(null).getAllTOs();
        Collection<SpeciesToDataGroupTO> expected = Arrays.asList(
                new SpeciesToDataGroupTO("11", "1"),
                new SpeciesToDataGroupTO("21", "2"),
                new SpeciesToDataGroupTO("31", "2")
        );
        assertTrue("SpeciesToDataGroupTOs are incorrectly retrieved\nGOT\n" + actual + 
                "\nEXPECTED\n" + expected, TOComparator.areTOCollectionsEqual(actual, expected));
        
        //ordering requested only on taxonomic distance to species 11
        LinkedHashMap<SpeciesToGroupOrderingAttribute, OrderingDAO.Direction> attrs = new LinkedHashMap<>();
        attrs.put(SpeciesToGroupOrderingAttribute.DISTANCE_TO_SPECIES, OrderingDAO.Direction.ASC);
        List<SpeciesToDataGroupTO> actualList = dao.getAllSpeciesToDataGroup(attrs).getAllTOs();
        List<SpeciesToDataGroupTO> expectedList = Arrays.asList(
                new SpeciesToDataGroupTO("11", "1"),
                new SpeciesToDataGroupTO("31", "2"),
                new SpeciesToDataGroupTO("21", "2")
        );
        assertTrue("SpeciesToDataGroupTOs are incorrectly retrieved\nGOT\n" + actualList + 
                "\nEXPECTED\n" + expectedList, TOComparator.areTOCollectionsEqual(actualList, expectedList));
        assertEquals(expectedList, actualList);
        
        //ordering requested first on the data group ID, second on the taxonomic distance
        attrs = new LinkedHashMap<>();
        attrs.put(SpeciesToGroupOrderingAttribute.DATA_GROUP_ID, OrderingDAO.Direction.DESC);
        attrs.put(SpeciesToGroupOrderingAttribute.DISTANCE_TO_SPECIES, OrderingDAO.Direction.ASC);
        actualList = dao.getAllSpeciesToDataGroup(attrs).getAllTOs();
        expectedList = Arrays.asList(
                new SpeciesToDataGroupTO("31", "2"),
                new SpeciesToDataGroupTO("21", "2"),
                new SpeciesToDataGroupTO("11", "1")
        );
        assertTrue("SpeciesToDataGroupTOs are incorrectly retrieved\nGOT\n" + actualList + 
                "\nEXPECTED\n" + expectedList, TOComparator.areTOCollectionsEqual(actualList, expectedList));
        assertEquals(expectedList, actualList);
        
        //try with a different targeted species .
        dao = new MySQLSpeciesDataGroupDAO(getMySQLDAOManager(), "21");
        attrs = new LinkedHashMap<>();
        //descending order of taxonomic distance to species 21.  
        attrs.put(SpeciesToGroupOrderingAttribute.DISTANCE_TO_SPECIES, OrderingDAO.Direction.DESC);
        actualList = dao.getAllSpeciesToDataGroup(attrs).getAllTOs();
        expectedList = Arrays.asList(
                //species 11 and 31 have the same taxonomic distance to species 21, 
                //they should also be ordered by species ID among them.
                new SpeciesToDataGroupTO("11", "1"),
                new SpeciesToDataGroupTO("31", "2"),
                new SpeciesToDataGroupTO("21", "2")
        );
        assertTrue("SpeciesToDataGroupTOs are incorrectly retrieved\nGOT\n" + actualList + 
                "\nEXPECTED\n" + expectedList, TOComparator.areTOCollectionsEqual(actualList, expectedList));
        assertEquals(expectedList, actualList);
        
        //but if we request to also order by data group ID, it should have precedence 
        //over the species IDs, which is just an add for cases with several species having 
        //a same common ancestors
        attrs.put(SpeciesToGroupOrderingAttribute.DATA_GROUP_ID, OrderingDAO.Direction.DESC);
        actualList = dao.getAllSpeciesToDataGroup(attrs).getAllTOs();
        expectedList = Arrays.asList(
                //species 11 and 31 have the same taxonomic distance to species 21, 
                //but here we also requested ordering per data group ID.
                new SpeciesToDataGroupTO("31", "2"),
                new SpeciesToDataGroupTO("11", "1"),
                new SpeciesToDataGroupTO("21", "2")
        );
        assertTrue("SpeciesToDataGroupTOs are incorrectly retrieved\nGOT\n" + actualList + 
                "\nEXPECTED\n" + expectedList, TOComparator.areTOCollectionsEqual(actualList, expectedList));
        assertEquals(expectedList, actualList);
        
        
        //check that we have no results if we use an incorrect species ID for ordering
        dao = new MySQLSpeciesDataGroupDAO(getMySQLDAOManager(), "11953736");
        assertTrue("No SpeciesToDataGroupTOs should have been retrieved", 
                dao.getAllSpeciesToDataGroup(attrs).getAllTOs().isEmpty());
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
                new SpeciesDataGroupTO("101", "sdg name 1", "sdg desc 1", 1),
                new SpeciesDataGroupTO("102", "sdg name 2", "sdg desc 2", 2));
        try {
            MySQLSpeciesDataGroupDAO dao = new MySQLSpeciesDataGroupDAO(this.getMySQLDAOManager());
            assertEquals("Incorrect number of rows inserted", 2, 
                    dao.insertSpeciesDataGroups(groupTOs));
            
            //we manually verify the insertion, as we do not want to rely on other methods 
            //that are tested elsewhere.
            //This test method could be better written (DRY, ...)
            try (BgeePreparedStatement stmt = this.getMySQLDAOManager().getConnection().
                    prepareStatement("SELECT 1 FROM speciesDataGroup WHERE speciesDataGroupId = ? "
                            + "AND speciesDataGroupName = ? AND speciesDataGroupDescription = ? "
                            + "AND speciesDataGroupOrder = ?")) {
                
                stmt.setInt(1, 101);
                stmt.setString(2, "sdg name 1");
                stmt.setString(3, "sdg desc 1");
                stmt.setInt(4, 1);
                assertTrue("SpeciesDataGroupTO incorrectly inserted", 
                        stmt.getRealPreparedStatement().executeQuery().next());
                
                stmt.setInt(1, 102);
                stmt.setString(2, "sdg name 2");
                stmt.setString(3, "sdg desc 2");
                stmt.setInt(4, 2);
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
