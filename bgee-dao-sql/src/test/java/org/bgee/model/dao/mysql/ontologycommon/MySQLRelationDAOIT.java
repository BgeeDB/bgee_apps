package org.bgee.model.dao.mysql.ontologycommon;

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
import org.bgee.model.dao.api.ontologycommon.RelationDAO;
import org.bgee.model.dao.api.ontologycommon.RelationDAO.RelationTO;
import org.bgee.model.dao.api.ontologycommon.RelationDAO.RelationTO.RelationStatus;
import org.bgee.model.dao.api.ontologycommon.RelationDAO.RelationTO.RelationType;
import org.bgee.model.dao.api.ontologycommon.RelationDAO.RelationTOResultSet;
import org.bgee.model.dao.mysql.MySQLITAncestor;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.junit.Test;


/**
 * Integration tests for {@link MySQLRelationDAO}, performed on a real MySQL database. 
 * See the documentation of {@link org.bgee.model.dao.mysql.MySQLITAncestor} for 
 * important information.
 * 
 * @author Valentine Rech de Laval
 * @version Bgee 13
 * @see org.bgee.model.dao.api.ontologycommon.RelationDAO
 * @since Bgee 13
 */
public class MySQLRelationDAOIT extends MySQLITAncestor {
    
    private final static Logger log = LogManager.getLogger(MySQLRelationDAOIT.class.getName());

    public MySQLRelationDAOIT() {
        super();
    }

    @Override
    protected Logger getLogger() {
        return log;
    }

    /**
     * Test the select method {@link MySQLRelationDAO#getAllAnatEntityRelations()}.
     */
    @Test
    public void shouldGetAllAnatEntityRelations() throws SQLException {
        log.entry();

        this.useSelectDB();

        MySQLRelationDAO dao = new MySQLRelationDAO(this.getMySQLDAOManager());
        // Test recovery of all attributes without filter on species IDs
        List<RelationTO> expectedRelations = Arrays.asList(
                new RelationTO("1", "Anat_id1", "Anat_id1", RelationType.ISA_PARTOF, RelationStatus.REFLEXIVE),
                new RelationTO("2", "Anat_id2", "Anat_id2", RelationType.ISA_PARTOF, RelationStatus.REFLEXIVE),
                new RelationTO("3", "Anat_id3", "Anat_id3", RelationType.ISA_PARTOF, RelationStatus.REFLEXIVE),
                new RelationTO("4", "Anat_id4", "Anat_id4", RelationType.ISA_PARTOF, RelationStatus.REFLEXIVE),
                new RelationTO("5", "Anat_id5", "Anat_id5", RelationType.ISA_PARTOF, RelationStatus.REFLEXIVE),
                new RelationTO("6", "Anat_id6", "Anat_id6", RelationType.ISA_PARTOF, RelationStatus.REFLEXIVE),
                new RelationTO("7", "Anat_id7", "Anat_id7", RelationType.ISA_PARTOF, RelationStatus.REFLEXIVE),
                new RelationTO("8", "Anat_id8", "Anat_id8", RelationType.ISA_PARTOF, RelationStatus.REFLEXIVE),
                new RelationTO("9", "Anat_id9", "Anat_id9", RelationType.ISA_PARTOF, RelationStatus.REFLEXIVE),
                new RelationTO("10", "Anat_id10", "Anat_id10", RelationType.ISA_PARTOF, RelationStatus.REFLEXIVE),
                new RelationTO("11", "Anat_id11", "Anat_id11", RelationType.ISA_PARTOF, RelationStatus.REFLEXIVE),
                new RelationTO("12", "Anat_id2", "Anat_id1", RelationType.ISA_PARTOF, RelationStatus.DIRECT),
                new RelationTO("13", "Anat_id3", "Anat_id2", RelationType.ISA_PARTOF, RelationStatus.DIRECT),
                new RelationTO("14", "Anat_id4", "Anat_id2", RelationType.ISA_PARTOF, RelationStatus.DIRECT),
                new RelationTO("15", "Anat_id5", "Anat_id2", RelationType.ISA_PARTOF, RelationStatus.DIRECT),
                new RelationTO("16", "Anat_id5", "Anat_id7", RelationType.DEVELOPSFROM, RelationStatus.INDIRECT),
                new RelationTO("17", "Anat_id5", "Anat_id8", RelationType.DEVELOPSFROM, RelationStatus.DIRECT),
                new RelationTO("18", "Anat_id6", "Anat_id1", RelationType.ISA_PARTOF, RelationStatus.DIRECT),
                new RelationTO("19", "Anat_id7", "Anat_id6", RelationType.DEVELOPSFROM, RelationStatus.DIRECT),
                new RelationTO("20", "Anat_id8", "Anat_id7", RelationType.ISA_PARTOF, RelationStatus.DIRECT),
                new RelationTO("21", "Anat_id9", "Anat_id5", RelationType.ISA_PARTOF, RelationStatus.DIRECT),
                new RelationTO("22", "Anat_id10", "Anat_id5", RelationType.ISA_PARTOF, RelationStatus.DIRECT),
                new RelationTO("23", "Anat_id11", "Anat_id10", RelationType.ISA_PARTOF, RelationStatus.DIRECT));
        this.compareResultSetAndTOList(dao.getAllAnatEntityRelations(null), expectedRelations);

        // Test recovery of one attribute without filter on species IDs
        dao.setAttributes(Arrays.asList(RelationDAO.Attribute.RELATIONID));
        expectedRelations = Arrays.asList(
                new RelationTO("1", null, null, null, null),
                new RelationTO("2", null, null, null, null),
                new RelationTO("3", null, null, null, null),
                new RelationTO("4", null, null, null, null),
                new RelationTO("5", null, null, null, null),
                new RelationTO("6", null, null, null, null),
                new RelationTO("7", null, null, null, null),
                new RelationTO("8", null, null, null, null),
                new RelationTO("9", null, null, null, null),
                new RelationTO("10", null, null, null, null),
                new RelationTO("11", null, null, null, null),
                new RelationTO("12", null, null, null, null),
                new RelationTO("13", null, null, null, null),
                new RelationTO("14", null, null, null, null),
                new RelationTO("15", null, null, null, null),
                new RelationTO("16", null, null, null, null),
                new RelationTO("17", null, null, null, null),
                new RelationTO("18", null, null, null, null),
                new RelationTO("19", null, null, null, null),
                new RelationTO("20", null, null, null, null),
                new RelationTO("21", null, null, null, null),
                new RelationTO("22", null, null, null, null),
                new RelationTO("23", null, null, null, null));
        this.compareResultSetAndTOList(dao.getAllAnatEntityRelations(null), expectedRelations);

        // Test recovery of one attribute with filter on species IDs
        Set<String> speciesIds = new HashSet<String>();
        speciesIds.addAll(Arrays.asList("11","44"));
        expectedRelations = Arrays.asList(
                new RelationTO("1", null, null, null, null),
                new RelationTO("2", null, null, null, null),
                new RelationTO("4", null, null, null, null),
                new RelationTO("5", null, null, null, null),
                new RelationTO("8", null, null, null, null),
                new RelationTO("10", null, null, null, null),
                new RelationTO("12", null, null, null, null),
                new RelationTO("14", null, null, null, null),
                new RelationTO("15", null, null, null, null),
                new RelationTO("18", null, null, null, null),
                new RelationTO("19", null, null, null, null),
                new RelationTO("23", null, null, null, null));
        this.compareResultSetAndTOList(dao.getAllAnatEntityRelations(speciesIds), expectedRelations);
    }

    private void compareResultSetAndTOList(RelationTOResultSet resultSet,
            List<RelationTO> expectedRelations) {
        log.entry(resultSet, expectedRelations);

        try {
            int countNbEntites = 0;
            while (resultSet.next()) {
                boolean found = false;
                RelationTO resultSetTO = resultSet.getTO();
                countNbEntites++;
                for (RelationTO expTO: expectedRelations) {
                    log.trace("Comparing {} to {}", resultSetTO.getId(), expTO.getId());
                    if (resultSetTO.equals(expTO)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    log.debug("No equivalent RelationTO found for {}", resultSetTO.toString());
                    throw log.throwing(new AssertionError("Incorrect generated TO"));
                }
            }
            if (countNbEntites != expectedRelations.size()) {
                log.debug("Not all RelationTO found for {}, {} generated but {} expected",
                        expectedRelations.toString(), countNbEntites, expectedRelations.size());
                throw log.throwing(new AssertionError("Incorrect number of generated TOs"));
            }
        } finally {
            resultSet.close();
        }
    }
    
    /**
     * Test the insert method {@link MySQLRelationDAO#insertAnatEntityRelations()}.
     */
    @Test
    public void shouldInsertAnatEntityRelations() throws SQLException {
        log.entry();
        
        this.useEmptyDB();

        //create a Collection of TaxonConstraintTO to be inserted
        Collection<RelationTO> relationTOs = Arrays.asList(
                new RelationTO("1", "sourceId1", "targetId1", RelationType.ISA_PARTOF, RelationStatus.DIRECT),
                new RelationTO("2", "sourceId2", "targetId2", RelationType.DEVELOPSFROM, RelationStatus.REFLEXIVE),
                new RelationTO("3", "sourceId3", "targetId3", RelationType.TRANSFORMATIONOF, RelationStatus.INDIRECT));

        try {
            MySQLRelationDAO dao = new MySQLRelationDAO(this.getMySQLDAOManager());
            assertEquals("Incorrect number of rows inserted", 3, 
                    dao.insertAnatEntityRelations(relationTOs));
            
            //we manually verify the insertion, as we do not want to rely on other methods 
            //that are tested elsewhere.
            try (BgeePreparedStatement stmt = this.getMySQLDAOManager().getConnection().
                    prepareStatement("select 1 from anatEntityRelation " +
                            "where anatEntityRelationId = ? AND anatEntitySourceId = ? " +
                            "AND anatEntityTargetId = ? AND relationType = ? AND relationStatus = ?")) {
                
                stmt.setInt(1, 1);
                stmt.setString(2, "sourceId1");
                stmt.setString(3, "targetId1");
                stmt.setString(4, RelationType.ISA_PARTOF.getStringRepresentation());
                stmt.setString(5, RelationStatus.DIRECT.getStringRepresentation());
                assertTrue("RelationTO (AnatEntityRelation) incorrectly inserted", 
                        stmt.getRealPreparedStatement().executeQuery().next());
                
                stmt.setInt(1, 2);
                stmt.setString(2, "sourceId2");
                stmt.setString(3, "targetId2");
                stmt.setString(4, RelationType.DEVELOPSFROM.getStringRepresentation());
                stmt.setString(5, RelationStatus.REFLEXIVE.getStringRepresentation());
                assertTrue("RelationTO (AnatEntityRelation) incorrectly inserted", 
                        stmt.getRealPreparedStatement().executeQuery().next());

                stmt.setInt(1, 3);
                stmt.setString(2, "sourceId3");
                stmt.setString(3, "targetId3");
                stmt.setString(4, RelationType.TRANSFORMATIONOF.getStringRepresentation());
                stmt.setString(5, RelationStatus.INDIRECT.getStringRepresentation());
                assertTrue("RelationTO (AnatEntityRelation) incorrectly inserted", 
                        stmt.getRealPreparedStatement().executeQuery().next());
            }
        } finally {
            this.emptyAndUseDefaultDB();
        }

        log.exit();
    }

}
