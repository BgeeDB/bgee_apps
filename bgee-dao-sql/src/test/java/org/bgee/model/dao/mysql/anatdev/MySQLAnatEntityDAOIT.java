package org.bgee.model.dao.mysql.anatdev;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.TOComparator;
import org.bgee.model.dao.api.anatdev.AnatEntityDAO;
import org.bgee.model.dao.api.anatdev.AnatEntityDAO.AnatEntityTO;
import org.bgee.model.dao.api.anatdev.AnatEntityDAO.AnatEntityTOResultSet;
import org.bgee.model.dao.mysql.MySQLITAncestor;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.junit.Test;

/**
 * Integration tests for {@link MySQLAnatEntityDAO}, performed on a real MySQL database. 
 * See the documentation of {@link org.bgee.model.dao.mysql.MySQLITAncestor} for 
 * important information.
 * 
 * @author Valentine Rech de Laval
 * @version Bgee 13
 * @see org.bgee.model.dao.api.anatdev.AnatEntityDAO
 * @since Bgee 13
 */
public class MySQLAnatEntityDAOIT extends MySQLITAncestor {
    
    private final static Logger log = 
            LogManager.getLogger(MySQLAnatEntityDAOIT.class.getName());

    public MySQLAnatEntityDAOIT() {
        super();
    }

    @Override
    protected Logger getLogger() {
        return log;
    }

    /**
     * Test the select method {@link MySQLAnatEntityDAO#getAllAnatEntities()}.
     */
    @Test
    public void shouldGetAllAnatEntities() throws SQLException {
        log.entry();

        this.useSelectDB();

        MySQLAnatEntityDAO dao = new MySQLAnatEntityDAO(this.getMySQLDAOManager());
        // Test recovery of all attributes without filter on species IDs
        List<AnatEntityTO> expectedAnatEntities = Arrays.asList(
                new AnatEntityTO("Anat_id1","anatStruct","anatStruct desc","Stage_id1","Stage_id2"),
                new AnatEntityTO("Anat_id2","organ","organ desc","Stage_id10","Stage_id18"),
                new AnatEntityTO("Anat_id3","heart","heart desc","Stage_id16","Stage_id18"),
                new AnatEntityTO("Anat_id4","gill","gill desc","Stage_id12","Stage_id18"),
                new AnatEntityTO("Anat_id5","brain","brain desc","Stage_id11","Stage_id17"),
                new AnatEntityTO("Anat_id6","embryoStruct","embryoStruct desc","Stage_id2","Stage_id5"),
                new AnatEntityTO("Anat_id7","ectoderm","ectoderm desc","Stage_id6","Stage_id13"),
                new AnatEntityTO("Anat_id8","neuralTube","neuralTube desc","Stage_id8","Stage_id17"),
                new AnatEntityTO("Anat_id9","forebrain","forebrain desc","Stage_id8","Stage_id17"),
                new AnatEntityTO("Anat_id10","hindbrain","hindbrain desc","Stage_id8","Stage_id17"),
                new AnatEntityTO("Anat_id11","cerebellum","cerebellum desc","Stage_id9","Stage_id13"));
        this.compareResultSetAndTOList(dao.getAllAnatEntities(null), expectedAnatEntities);

        // Test recovery of one attribute without filter on species IDs
        dao.setAttributes(Arrays.asList(AnatEntityDAO.Attribute.ID));
        expectedAnatEntities = Arrays.asList(
                new AnatEntityTO("Anat_id1", null, null, null, null),
                new AnatEntityTO("Anat_id2", null, null, null, null),
                new AnatEntityTO("Anat_id3", null, null, null, null),
                new AnatEntityTO("Anat_id4", null, null, null, null),
                new AnatEntityTO("Anat_id5", null, null, null, null),
                new AnatEntityTO("Anat_id6", null, null, null, null),
                new AnatEntityTO("Anat_id7", null, null, null, null),
                new AnatEntityTO("Anat_id8", null, null, null, null),
                new AnatEntityTO("Anat_id9", null, null, null, null),
                new AnatEntityTO("Anat_id10", null, null, null, null),
                new AnatEntityTO("Anat_id11", null, null, null, null));
        this.compareResultSetAndTOList(dao.getAllAnatEntities(null), expectedAnatEntities);

        // Test recovery of one attribute with filter on species IDs
        Set<String> speciesIds = new HashSet<String>();
        speciesIds.addAll(Arrays.asList("11","44"));
        expectedAnatEntities = Arrays.asList(
                new AnatEntityTO("Anat_id1", null, null, null, null),
                new AnatEntityTO("Anat_id2", null, null, null, null),
                new AnatEntityTO("Anat_id6", null, null, null, null),
                new AnatEntityTO("Anat_id8", null, null, null, null),
                new AnatEntityTO("Anat_id11", null, null, null, null));
        this.compareResultSetAndTOList(dao.getAllAnatEntities(speciesIds), expectedAnatEntities);
    }

    private void compareResultSetAndTOList(AnatEntityTOResultSet resultSet,
            List<AnatEntityTO> expectedTOs) {
        log.entry(resultSet, expectedTOs);
        
        try {
            int countNbEntites = 0;
            while (resultSet.next()) {
                boolean found = false;
                AnatEntityTO resultSetTO = resultSet.getTO();
                countNbEntites++;
                for (AnatEntityTO expTO: expectedTOs) {
                    log.trace("Comparing {} to {}", resultSetTO.getId(), expTO.getId());
                    if (TOComparator.areAnatEntityTOsEqual(resultSetTO, expTO)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    log.debug("No equivalent AnatEntityTO found for {}", resultSetTO.toString());
                    throw log.throwing(new AssertionError("Incorrect generated TO"));
                }
            }
            if (countNbEntites != expectedTOs.size()) {
                log.debug("Not all AnatEntityTO found for {}, {} generated vs {} expected",
                        expectedTOs.toString(), countNbEntites, expectedTOs.size());
                throw log.throwing(new AssertionError("Incorrect number of generated TOs"));
            }
        } finally {
            resultSet.close();
        }
    }
    
    /**
     * Test the insert method {@link MySQLAnatEntityDAO#insertAnatEntities()}.
     */
    @Test
    public void shouldInsertAnatEntities() throws SQLException {
        log.entry();
        
        this.useEmptyDB();
        
        //create a Collection of AnatEntityTO to be inserted
        Collection<AnatEntityTO> anatEntityTOs = Arrays.asList(
                new AnatEntityTO("Anat_id10","hindbrain","hindbrain desc","Stage_id8","Stage_id17"),
                new AnatEntityTO("Anat_id5","brain","brain desc","Stage_id11","Stage_id17"),
                new AnatEntityTO("Anat_id1","anatStruct",null,"Stage_id1","Stage_id2"));

        try {
            MySQLAnatEntityDAO dao = new MySQLAnatEntityDAO(this.getMySQLDAOManager());
            assertEquals("Incorrect number of rows inserted", 3, 
                    dao.insertAnatEntities(anatEntityTOs));
            
            //we manually verify the insertion, as we do not want to rely on other methods 
            //that are tested elsewhere.
            try (BgeePreparedStatement stmt = this.getMySQLDAOManager().getConnection().
                    prepareStatement("select 1 from anatEntity where " +
                      "anatEntityId = ? AND anatEntityName = ? AND anatEntityDescription = ? " +
                            "AND startStageId = ? AND endStageId = ?")) {
                
                stmt.setString(1, "Anat_id10");
                stmt.setString(2, "hindbrain");
                stmt.setString(3, "hindbrain desc");
                stmt.setString(4, "Stage_id8");
                stmt.setString(5, "Stage_id17");
                assertTrue("AnatEntityTO incorrectly inserted", 
                        stmt.getRealPreparedStatement().executeQuery().next());
                
                stmt.setString(1, "Anat_id5");
                stmt.setString(2, "brain");
                stmt.setString(3, "brain desc");
                stmt.setString(4, "Stage_id11");
                stmt.setString(5, "Stage_id17");
                assertTrue("AnatEntityTO incorrectly inserted", 
                        stmt.getRealPreparedStatement().executeQuery().next());
            }
            try (BgeePreparedStatement stmt = this.getMySQLDAOManager().getConnection().
                    prepareStatement("select 1 from anatEntity where " +
                      "anatEntityId = ? AND anatEntityName = ? AND anatEntityDescription is null " +
                            "AND startStageId = ? AND endStageId = ?")) {

                stmt.setString(1, "Anat_id1");
                stmt.setString(2, "anatStruct");
                stmt.setString(3, "Stage_id1");
                stmt.setString(4, "Stage_id2");
                assertTrue("AnatEntityTO incorrectly inserted", 
                        stmt.getRealPreparedStatement().executeQuery().next());
            }
        } finally {
            this.emptyAndUseDefaultDB();
        }

        log.exit();
    }   
}
