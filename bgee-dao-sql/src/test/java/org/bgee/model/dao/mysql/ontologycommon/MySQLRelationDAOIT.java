package org.bgee.model.dao.mysql.ontologycommon;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.TOComparator;
import org.bgee.model.dao.api.ontologycommon.RelationDAO;
import org.bgee.model.dao.api.ontologycommon.RelationDAO.RelationTO;
import org.bgee.model.dao.api.ontologycommon.RelationDAO.RelationTO.RelationStatus;
import org.bgee.model.dao.api.ontologycommon.RelationDAO.RelationTO.RelationType;
import org.bgee.model.dao.api.ontologycommon.RelationDAO.RelationTOResultSet;
import org.bgee.model.dao.mysql.MySQLITAncestor;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;


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

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    /**
     * Test the method {@link MySQLRelationDAO#getAnatEntityRelations(Collection, boolean, 
     * Collection, Collection, boolean, Collection, Collection, Collection)}.
     */
    @Test
    public void shouldGetAnatEntityRelations() throws SQLException {

        this.useSelectDB();

        MySQLRelationDAO dao = new MySQLRelationDAO(this.getMySQLDAOManager());
        Collection<RelationTO<String>> allRelTOs = Arrays.asList(
        new RelationTO<>(1, "Anat_id1", "Anat_id1", RelationType.ISA_PARTOF, RelationStatus.REFLEXIVE),
        new RelationTO<>(10, "Anat_id10", "Anat_id10", RelationType.ISA_PARTOF, RelationStatus.REFLEXIVE),
        new RelationTO<>(23, "Anat_id11", "Anat_id10", RelationType.ISA_PARTOF, RelationStatus.DIRECT),
        new RelationTO<>(11, "Anat_id11", "Anat_id11", RelationType.ISA_PARTOF, RelationStatus.REFLEXIVE),
        new RelationTO<>(12, "Anat_id2", "Anat_id1", RelationType.ISA_PARTOF, RelationStatus.DIRECT),
        new RelationTO<>(2, "Anat_id2", "Anat_id2", RelationType.ISA_PARTOF, RelationStatus.REFLEXIVE),
        new RelationTO<>(14, "Anat_id4", "Anat_id2", RelationType.ISA_PARTOF, RelationStatus.DIRECT),
        new RelationTO<>(4, "Anat_id4", "Anat_id4", RelationType.ISA_PARTOF, RelationStatus.REFLEXIVE),
        new RelationTO<>(15, "Anat_id5", "Anat_id2", RelationType.ISA_PARTOF, RelationStatus.DIRECT),
        new RelationTO<>(5, "Anat_id5", "Anat_id5", RelationType.ISA_PARTOF, RelationStatus.REFLEXIVE),
        new RelationTO<>(18, "Anat_id6", "Anat_id1", RelationType.ISA_PARTOF, RelationStatus.DIRECT),
        new RelationTO<>(19, "Anat_id7", "Anat_id6", RelationType.DEVELOPSFROM, RelationStatus.DIRECT),
        new RelationTO<>(7, "Anat_id7", "Anat_id7", RelationType.ISA_PARTOF, RelationStatus.REFLEXIVE),
        new RelationTO<>(8, "Anat_id8", "Anat_id8", RelationType.ISA_PARTOF, RelationStatus.REFLEXIVE),
        new RelationTO<>(21, "Anat_id9", "Anat_id5", RelationType.ISA_PARTOF, RelationStatus.DIRECT));
                
        // Retrieve relations with several species IDs, any species requested
        Collection<RelationTO<String>> expectedRelations = allRelTOs;
        RelationTOResultSet<String> resultSet = dao.getAnatEntityRelations(Arrays.asList(11, 21), 
                true, null, null, null, null, null, null);
        assertTrue("RelationTOs incorrectly retrieved",
                TOComparator.areTOCollectionsEqual(expectedRelations, resultSet.getAllTOs()));
        
        //now, relations existing in all requested species
        expectedRelations = Arrays.asList(
                new RelationTO<>(1, "Anat_id1", "Anat_id1", RelationType.ISA_PARTOF, RelationStatus.REFLEXIVE),
                new RelationTO<>(10, "Anat_id10", "Anat_id10", RelationType.ISA_PARTOF, RelationStatus.REFLEXIVE),
                new RelationTO<>(23, "Anat_id11", "Anat_id10", RelationType.ISA_PARTOF, RelationStatus.DIRECT),
                new RelationTO<>(12, "Anat_id2", "Anat_id1", RelationType.ISA_PARTOF, RelationStatus.DIRECT),
                new RelationTO<>(2, "Anat_id2", "Anat_id2", RelationType.ISA_PARTOF, RelationStatus.REFLEXIVE),
                new RelationTO<>(15, "Anat_id5", "Anat_id2", RelationType.ISA_PARTOF, RelationStatus.DIRECT),
                new RelationTO<>(5, "Anat_id5", "Anat_id5", RelationType.ISA_PARTOF, RelationStatus.REFLEXIVE),
                new RelationTO<>(18, "Anat_id6", "Anat_id1", RelationType.ISA_PARTOF, RelationStatus.DIRECT),
                new RelationTO<>(19, "Anat_id7", "Anat_id6", RelationType.DEVELOPSFROM, RelationStatus.DIRECT));
        resultSet = dao.getAnatEntityRelations(Arrays.asList(11, 21), 
                false, null, null, null, null, null, null);
        assertTrue("RelationTOs incorrectly retrieved",
                TOComparator.areTOCollectionsEqual(expectedRelations, resultSet.getAllTOs()));
        
        //test with only one species
        expectedRelations = Arrays.asList(
                new RelationTO<>(1, "Anat_id1", "Anat_id1", RelationType.ISA_PARTOF, RelationStatus.REFLEXIVE),
                new RelationTO<>(10, "Anat_id10", "Anat_id10", RelationType.ISA_PARTOF, RelationStatus.REFLEXIVE),
                new RelationTO<>(23, "Anat_id11", "Anat_id10", RelationType.ISA_PARTOF, RelationStatus.DIRECT),
                new RelationTO<>(12, "Anat_id2", "Anat_id1", RelationType.ISA_PARTOF, RelationStatus.DIRECT),
                new RelationTO<>(2, "Anat_id2", "Anat_id2", RelationType.ISA_PARTOF, RelationStatus.REFLEXIVE),
                new RelationTO<>(14, "Anat_id4", "Anat_id2", RelationType.ISA_PARTOF, RelationStatus.DIRECT),
                new RelationTO<>(4, "Anat_id4", "Anat_id4", RelationType.ISA_PARTOF, RelationStatus.REFLEXIVE),
                new RelationTO<>(15, "Anat_id5", "Anat_id2", RelationType.ISA_PARTOF, RelationStatus.DIRECT),
                new RelationTO<>(5, "Anat_id5", "Anat_id5", RelationType.ISA_PARTOF, RelationStatus.REFLEXIVE),
                new RelationTO<>(18, "Anat_id6", "Anat_id1", RelationType.ISA_PARTOF, RelationStatus.DIRECT),
                new RelationTO<>(19, "Anat_id7", "Anat_id6", RelationType.DEVELOPSFROM, RelationStatus.DIRECT),
                new RelationTO<>(8, "Anat_id8", "Anat_id8", RelationType.ISA_PARTOF, RelationStatus.REFLEXIVE));
        resultSet = dao.getAnatEntityRelations(Arrays.asList(11), 
                null, null, null, null, null, null, null);
        assertTrue("RelationTOs incorrectly retrieved",
                TOComparator.areTOCollectionsEqual(expectedRelations, resultSet.getAllTOs()));
        
        //Now, we stop requesting the relation ID
        Collection<RelationDAO.Attribute> attrs = EnumSet.complementOf(
                EnumSet.of(RelationDAO.Attribute.RELATION_ID));
        expectedRelations = Arrays.asList(
                new RelationTO<>(null, "Anat_id1", "Anat_id1", RelationType.ISA_PARTOF, RelationStatus.REFLEXIVE),
                new RelationTO<>(null, "Anat_id10", "Anat_id10", RelationType.ISA_PARTOF, RelationStatus.REFLEXIVE),
                new RelationTO<>(null, "Anat_id11", "Anat_id10", RelationType.ISA_PARTOF, RelationStatus.DIRECT),
                new RelationTO<>(null, "Anat_id2", "Anat_id1", RelationType.ISA_PARTOF, RelationStatus.DIRECT),
                new RelationTO<>(null, "Anat_id2", "Anat_id2", RelationType.ISA_PARTOF, RelationStatus.REFLEXIVE),
                new RelationTO<>(null, "Anat_id5", "Anat_id2", RelationType.ISA_PARTOF, RelationStatus.DIRECT),
                new RelationTO<>(null, "Anat_id5", "Anat_id5", RelationType.ISA_PARTOF, RelationStatus.REFLEXIVE),
                new RelationTO<>(null, "Anat_id6", "Anat_id1", RelationType.ISA_PARTOF, RelationStatus.DIRECT),
                new RelationTO<>(null, "Anat_id7", "Anat_id6", RelationType.DEVELOPSFROM, RelationStatus.DIRECT));
        resultSet = dao.getAnatEntityRelations(Arrays.asList(11, 21), 
                false, null, null, null, null, null, attrs);
        assertTrue("RelationTOs incorrectly retrieved",
                TOComparator.areTOCollectionsEqual(expectedRelations, resultSet.getAllTOs()));
        
        //now we add filtering on sources/targets
        //filtering on sources only
        expectedRelations = Arrays.asList(
                new RelationTO<>(null, "Anat_id10", "Anat_id10", RelationType.ISA_PARTOF, RelationStatus.REFLEXIVE),
                new RelationTO<>(null, "Anat_id2", "Anat_id1", RelationType.ISA_PARTOF, RelationStatus.DIRECT),
                new RelationTO<>(null, "Anat_id2", "Anat_id2", RelationType.ISA_PARTOF, RelationStatus.REFLEXIVE));
        resultSet = dao.getAnatEntityRelations(Arrays.asList(11, 21), 
                false, Arrays.asList("Anat_id2", "Anat_id10"), null, null, null, null, attrs);
        assertTrue("RelationTOs incorrectly retrieved",
                TOComparator.areTOCollectionsEqual(expectedRelations, resultSet.getAllTOs()));

        //filtering on targets only
        expectedRelations = Arrays.asList(
                new RelationTO<>(null, "Anat_id10", "Anat_id10", RelationType.ISA_PARTOF, RelationStatus.REFLEXIVE),
                new RelationTO<>(null, "Anat_id11", "Anat_id10", RelationType.ISA_PARTOF, RelationStatus.DIRECT),
                new RelationTO<>(null, "Anat_id2", "Anat_id2", RelationType.ISA_PARTOF, RelationStatus.REFLEXIVE),
                new RelationTO<>(null, "Anat_id5", "Anat_id2", RelationType.ISA_PARTOF, RelationStatus.DIRECT));
        resultSet = dao.getAnatEntityRelations(Arrays.asList(11, 21), 
                false, null, Arrays.asList("Anat_id2", "Anat_id10"), null, null, null, attrs);
        assertTrue("RelationTOs incorrectly retrieved",
                TOComparator.areTOCollectionsEqual(expectedRelations, resultSet.getAllTOs()));

        //OR condition on sources and targets
        expectedRelations = Arrays.asList(
                new RelationTO<>(null, "Anat_id10", "Anat_id10", RelationType.ISA_PARTOF, RelationStatus.REFLEXIVE),
                new RelationTO<>(null, "Anat_id11", "Anat_id10", RelationType.ISA_PARTOF, RelationStatus.DIRECT),
                new RelationTO<>(null, "Anat_id2", "Anat_id1", RelationType.ISA_PARTOF, RelationStatus.DIRECT),
                new RelationTO<>(null, "Anat_id2", "Anat_id2", RelationType.ISA_PARTOF, RelationStatus.REFLEXIVE),
                new RelationTO<>(null, "Anat_id5", "Anat_id2", RelationType.ISA_PARTOF, RelationStatus.DIRECT),
                new RelationTO<>(null, "Anat_id7", "Anat_id6", RelationType.DEVELOPSFROM, RelationStatus.DIRECT));
        resultSet = dao.getAnatEntityRelations(Arrays.asList(11, 21), false, 
                Arrays.asList("Anat_id2", "Anat_id10"), 
                Arrays.asList("Anat_id2", "Anat_id10", "Anat_id6"), true, null, null, attrs);
        assertTrue("RelationTOs incorrectly retrieved",
                TOComparator.areTOCollectionsEqual(expectedRelations, resultSet.getAllTOs()));

        //AND condition on sources and targets
        expectedRelations = Arrays.asList(
                new RelationTO<>(null, "Anat_id10", "Anat_id10", RelationType.ISA_PARTOF, RelationStatus.REFLEXIVE),
                new RelationTO<>(null, "Anat_id2", "Anat_id2", RelationType.ISA_PARTOF, RelationStatus.REFLEXIVE),
                new RelationTO<>(null, "Anat_id7", "Anat_id6", RelationType.DEVELOPSFROM, RelationStatus.DIRECT));
        resultSet = dao.getAnatEntityRelations(Arrays.asList(11, 21), false, 
                Arrays.asList("Anat_id2", "Anat_id10", "Anat_id7"), 
                Arrays.asList("Anat_id2", "Anat_id10", "Anat_id6"), false, null, null, attrs);
        assertTrue("RelationTOs incorrectly retrieved",
                TOComparator.areTOCollectionsEqual(expectedRelations, resultSet.getAllTOs()));
        
        //add filter on relation types
        expectedRelations = Arrays.asList(
                new RelationTO<>(null, "Anat_id10", "Anat_id10", RelationType.ISA_PARTOF, RelationStatus.REFLEXIVE),
                new RelationTO<>(null, "Anat_id2", "Anat_id2", RelationType.ISA_PARTOF, RelationStatus.REFLEXIVE));
        resultSet = dao.getAnatEntityRelations(Arrays.asList(11, 21), false, 
                Arrays.asList("Anat_id2", "Anat_id10", "Anat_id7"), 
                Arrays.asList("Anat_id2", "Anat_id10", "Anat_id6"), false, 
                Arrays.asList(RelationType.ISA_PARTOF, RelationType.TRANSFORMATIONOF), null, attrs);
        assertTrue("RelationTOs incorrectly retrieved",
                TOComparator.areTOCollectionsEqual(expectedRelations, resultSet.getAllTOs()));
        
        //filter on relation types and status
        expectedRelations = Arrays.asList(
                new RelationTO<>(null, "Anat_id7", "Anat_id6", RelationType.DEVELOPSFROM, RelationStatus.DIRECT));
        resultSet = dao.getAnatEntityRelations(Arrays.asList(11, 21), false, 
                Arrays.asList("Anat_id2", "Anat_id10", "Anat_id7"), 
                Arrays.asList("Anat_id2", "Anat_id10", "Anat_id6"), false, 
                Arrays.asList(RelationType.DEVELOPSFROM, RelationType.ISA_PARTOF), 
                Arrays.asList(RelationStatus.DIRECT, RelationStatus.INDIRECT), attrs);
        assertTrue("RelationTOs incorrectly retrieved",
                TOComparator.areTOCollectionsEqual(expectedRelations, resultSet.getAllTOs()));
    }

    /**
     * Test the select method {@link MySQLRelationDAO#getStageRelationsBySpeciesIds(Set, Set)}.
     */
    @Test
    public void shouldGetStageRelationsBySpeciesIds() throws SQLException {

        this.useSelectDB();

        MySQLRelationDAO dao = new MySQLRelationDAO(this.getMySQLDAOManager());
        //XXX: for now, we don't generate any stageRelationId (always set to 0), 
        //so we don't retrieve it. This might change in the future.
        List<RelationTO<String>> reflexiveRelTOs = Arrays.asList(
        new RelationTO<>(null, "Stage_id1", "Stage_id1", RelationType.ISA_PARTOF, RelationStatus.REFLEXIVE),
        new RelationTO<>(null, "Stage_id2", "Stage_id2", RelationType.ISA_PARTOF, RelationStatus.REFLEXIVE),
        new RelationTO<>(null, "Stage_id3", "Stage_id3", RelationType.ISA_PARTOF, RelationStatus.REFLEXIVE),
        new RelationTO<>(null, "Stage_id4", "Stage_id4", RelationType.ISA_PARTOF, RelationStatus.REFLEXIVE),
        new RelationTO<>(null, "Stage_id5", "Stage_id5", RelationType.ISA_PARTOF, RelationStatus.REFLEXIVE),
        new RelationTO<>(null, "Stage_id6", "Stage_id6", RelationType.ISA_PARTOF, RelationStatus.REFLEXIVE),
        new RelationTO<>(null, "Stage_id7", "Stage_id7", RelationType.ISA_PARTOF, RelationStatus.REFLEXIVE),
        new RelationTO<>(null, "Stage_id8", "Stage_id8", RelationType.ISA_PARTOF, RelationStatus.REFLEXIVE),
        new RelationTO<>(null, "Stage_id9", "Stage_id9", RelationType.ISA_PARTOF, RelationStatus.REFLEXIVE),
        new RelationTO<>(null, "Stage_id10", "Stage_id10", RelationType.ISA_PARTOF, RelationStatus.REFLEXIVE),
        new RelationTO<>(null, "Stage_id11", "Stage_id11", RelationType.ISA_PARTOF, RelationStatus.REFLEXIVE),
        new RelationTO<>(null, "Stage_id12", "Stage_id12", RelationType.ISA_PARTOF, RelationStatus.REFLEXIVE),
        new RelationTO<>(null, "Stage_id13", "Stage_id13", RelationType.ISA_PARTOF, RelationStatus.REFLEXIVE),
        new RelationTO<>(null, "Stage_id14", "Stage_id14", RelationType.ISA_PARTOF, RelationStatus.REFLEXIVE),
        new RelationTO<>(null, "Stage_id15", "Stage_id15", RelationType.ISA_PARTOF, RelationStatus.REFLEXIVE),
        new RelationTO<>(null, "Stage_id16", "Stage_id16", RelationType.ISA_PARTOF, RelationStatus.REFLEXIVE),
        new RelationTO<>(null, "Stage_id17", "Stage_id17", RelationType.ISA_PARTOF, RelationStatus.REFLEXIVE),
        new RelationTO<>(null, "Stage_id18", "Stage_id18", RelationType.ISA_PARTOF, RelationStatus.REFLEXIVE));
        List<RelationTO<String>> directRelTOs = Arrays.asList(
        new RelationTO<>(null, "Stage_id2", "Stage_id1", RelationType.ISA_PARTOF, RelationStatus.DIRECT), 
        new RelationTO<>(null, "Stage_id5", "Stage_id1", RelationType.ISA_PARTOF, RelationStatus.DIRECT), 
        new RelationTO<>(null, "Stage_id10", "Stage_id1", RelationType.ISA_PARTOF, RelationStatus.DIRECT), 
        new RelationTO<>(null, "Stage_id14", "Stage_id1", RelationType.ISA_PARTOF, RelationStatus.DIRECT), 
        new RelationTO<>(null, "Stage_id3", "Stage_id2", RelationType.ISA_PARTOF, RelationStatus.DIRECT), 
        new RelationTO<>(null, "Stage_id4", "Stage_id2", RelationType.ISA_PARTOF, RelationStatus.DIRECT), 
        new RelationTO<>(null, "Stage_id6", "Stage_id5", RelationType.ISA_PARTOF, RelationStatus.DIRECT), 
        new RelationTO<>(null, "Stage_id7", "Stage_id5", RelationType.ISA_PARTOF, RelationStatus.DIRECT), 
        new RelationTO<>(null, "Stage_id11", "Stage_id10", RelationType.ISA_PARTOF, RelationStatus.DIRECT), 
        new RelationTO<>(null, "Stage_id12", "Stage_id10", RelationType.ISA_PARTOF, RelationStatus.DIRECT), 
        new RelationTO<>(null, "Stage_id13", "Stage_id10", RelationType.ISA_PARTOF, RelationStatus.DIRECT), 
        new RelationTO<>(null, "Stage_id15", "Stage_id14", RelationType.ISA_PARTOF, RelationStatus.DIRECT), 
        new RelationTO<>(null, "Stage_id18", "Stage_id14", RelationType.ISA_PARTOF, RelationStatus.DIRECT), 
        new RelationTO<>(null, "Stage_id8", "Stage_id7", RelationType.ISA_PARTOF, RelationStatus.DIRECT), 
        new RelationTO<>(null, "Stage_id9", "Stage_id7", RelationType.ISA_PARTOF, RelationStatus.DIRECT), 
        new RelationTO<>(null, "Stage_id16", "Stage_id15", RelationType.ISA_PARTOF, RelationStatus.DIRECT), 
        new RelationTO<>(null, "Stage_id17", "Stage_id15", RelationType.ISA_PARTOF, RelationStatus.DIRECT));
        List<RelationTO<String>> indirectRelTOs = Arrays.asList(
        new RelationTO<>(null, "Stage_id3", "Stage_id1", RelationType.ISA_PARTOF, RelationStatus.INDIRECT), 
        new RelationTO<>(null, "Stage_id4", "Stage_id1", RelationType.ISA_PARTOF, RelationStatus.INDIRECT), 
        new RelationTO<>(null, "Stage_id6", "Stage_id1", RelationType.ISA_PARTOF, RelationStatus.INDIRECT), 
        new RelationTO<>(null, "Stage_id7", "Stage_id1", RelationType.ISA_PARTOF, RelationStatus.INDIRECT), 
        new RelationTO<>(null, "Stage_id8", "Stage_id1", RelationType.ISA_PARTOF, RelationStatus.INDIRECT), 
        new RelationTO<>(null, "Stage_id9", "Stage_id1", RelationType.ISA_PARTOF, RelationStatus.INDIRECT), 
        new RelationTO<>(null, "Stage_id11", "Stage_id1", RelationType.ISA_PARTOF, RelationStatus.INDIRECT), 
        new RelationTO<>(null, "Stage_id12", "Stage_id1", RelationType.ISA_PARTOF, RelationStatus.INDIRECT), 
        new RelationTO<>(null, "Stage_id13", "Stage_id1", RelationType.ISA_PARTOF, RelationStatus.INDIRECT), 
        new RelationTO<>(null, "Stage_id15", "Stage_id1", RelationType.ISA_PARTOF, RelationStatus.INDIRECT), 
        new RelationTO<>(null, "Stage_id16", "Stage_id1", RelationType.ISA_PARTOF, RelationStatus.INDIRECT), 
        new RelationTO<>(null, "Stage_id17", "Stage_id1", RelationType.ISA_PARTOF, RelationStatus.INDIRECT), 
        new RelationTO<>(null, "Stage_id18", "Stage_id1", RelationType.ISA_PARTOF, RelationStatus.INDIRECT), 
        new RelationTO<>(null, "Stage_id8", "Stage_id5", RelationType.ISA_PARTOF, RelationStatus.INDIRECT), 
        new RelationTO<>(null, "Stage_id9", "Stage_id5", RelationType.ISA_PARTOF, RelationStatus.INDIRECT), 
        new RelationTO<>(null, "Stage_id16", "Stage_id14", RelationType.ISA_PARTOF, RelationStatus.INDIRECT), 
        new RelationTO<>(null, "Stage_id17", "Stage_id14", RelationType.ISA_PARTOF, RelationStatus.INDIRECT));
                
        // Test recovery of all attributes without filters.
        List<RelationTO<String>> expectedRels = new ArrayList<>();
        expectedRels.addAll(reflexiveRelTOs);
        expectedRels.addAll(directRelTOs);
        expectedRels.addAll(indirectRelTOs);
        List<RelationTO<String>> actualRels = dao.getStageRelationsBySpeciesIds(null, null).getAllTOs();
        assertTrue("RelationTOs incorrectly retrieved, expected: " + expectedRels + " - " +
        		"but was: " + actualRels, 
        		TOComparator.areTOCollectionsEqual(expectedRels, actualRels));
        
        //filter on RelationStatus
        expectedRels = new ArrayList<>();
        expectedRels.addAll(directRelTOs);
        actualRels = dao.getStageRelationsBySpeciesIds(null, 
                new HashSet<RelationStatus>(Arrays.asList(RelationStatus.DIRECT))).getAllTOs();
        assertTrue("RelationTOs incorrectly retrieved, expected: " + expectedRels + " - " +
                "but was: " + actualRels, 
                TOComparator.areTOCollectionsEqual(expectedRels, actualRels));

        expectedRels = new ArrayList<>();
        expectedRels.addAll(directRelTOs);
        expectedRels.addAll(indirectRelTOs);
        actualRels = dao.getStageRelationsBySpeciesIds(null, new HashSet<RelationStatus>(
                Arrays.asList(RelationStatus.DIRECT, RelationStatus.INDIRECT))).getAllTOs();
        assertTrue("RelationTOs incorrectly retrieved, expected: " + expectedRels + " - " +
                "but was: " + actualRels, 
                TOComparator.areTOCollectionsEqual(expectedRels, actualRels));
        
        //filter on speciesIds
        expectedRels = Arrays.asList(
        //reflexive relations
        new RelationTO<>(null, "Stage_id1", "Stage_id1", RelationType.ISA_PARTOF, RelationStatus.REFLEXIVE),
        new RelationTO<>(null, "Stage_id2", "Stage_id2", RelationType.ISA_PARTOF, RelationStatus.REFLEXIVE),
        new RelationTO<>(null, "Stage_id5", "Stage_id5", RelationType.ISA_PARTOF, RelationStatus.REFLEXIVE),
        new RelationTO<>(null, "Stage_id6", "Stage_id6", RelationType.ISA_PARTOF, RelationStatus.REFLEXIVE),
        new RelationTO<>(null, "Stage_id7", "Stage_id7", RelationType.ISA_PARTOF, RelationStatus.REFLEXIVE),
        new RelationTO<>(null, "Stage_id8", "Stage_id8", RelationType.ISA_PARTOF, RelationStatus.REFLEXIVE),
        new RelationTO<>(null, "Stage_id14", "Stage_id14", RelationType.ISA_PARTOF, RelationStatus.REFLEXIVE),
        new RelationTO<>(null, "Stage_id15", "Stage_id15", RelationType.ISA_PARTOF, RelationStatus.REFLEXIVE),
        new RelationTO<>(null, "Stage_id16", "Stage_id16", RelationType.ISA_PARTOF, RelationStatus.REFLEXIVE),
        new RelationTO<>(null, "Stage_id18", "Stage_id18", RelationType.ISA_PARTOF, RelationStatus.REFLEXIVE), 
        //direct relations
        new RelationTO<>(null, "Stage_id2", "Stage_id1", RelationType.ISA_PARTOF, RelationStatus.DIRECT), 
        new RelationTO<>(null, "Stage_id5", "Stage_id1", RelationType.ISA_PARTOF, RelationStatus.DIRECT), 
        new RelationTO<>(null, "Stage_id14", "Stage_id1", RelationType.ISA_PARTOF, RelationStatus.DIRECT), 
        new RelationTO<>(null, "Stage_id6", "Stage_id5", RelationType.ISA_PARTOF, RelationStatus.DIRECT), 
        new RelationTO<>(null, "Stage_id7", "Stage_id5", RelationType.ISA_PARTOF, RelationStatus.DIRECT), 
        new RelationTO<>(null, "Stage_id15", "Stage_id14", RelationType.ISA_PARTOF, RelationStatus.DIRECT), 
        new RelationTO<>(null, "Stage_id18", "Stage_id14", RelationType.ISA_PARTOF, RelationStatus.DIRECT), 
        new RelationTO<>(null, "Stage_id8", "Stage_id7", RelationType.ISA_PARTOF, RelationStatus.DIRECT), 
        new RelationTO<>(null, "Stage_id16", "Stage_id15", RelationType.ISA_PARTOF, RelationStatus.DIRECT), 
        //indirect relations
        new RelationTO<>(null, "Stage_id6", "Stage_id1", RelationType.ISA_PARTOF, RelationStatus.INDIRECT), 
        new RelationTO<>(null, "Stage_id7", "Stage_id1", RelationType.ISA_PARTOF, RelationStatus.INDIRECT), 
        new RelationTO<>(null, "Stage_id8", "Stage_id1", RelationType.ISA_PARTOF, RelationStatus.INDIRECT), 
        new RelationTO<>(null, "Stage_id15", "Stage_id1", RelationType.ISA_PARTOF, RelationStatus.INDIRECT), 
        new RelationTO<>(null, "Stage_id16", "Stage_id1", RelationType.ISA_PARTOF, RelationStatus.INDIRECT), 
        new RelationTO<>(null, "Stage_id18", "Stage_id1", RelationType.ISA_PARTOF, RelationStatus.INDIRECT), 
        new RelationTO<>(null, "Stage_id8", "Stage_id5", RelationType.ISA_PARTOF, RelationStatus.INDIRECT), 
        new RelationTO<>(null, "Stage_id16", "Stage_id14", RelationType.ISA_PARTOF, RelationStatus.INDIRECT));
        actualRels = dao.getStageRelationsBySpeciesIds(new HashSet<>(Arrays.asList(11)), null).getAllTOs();
        assertTrue("RelationTOs incorrectly retrieved, expected: " + expectedRels + " - " +
                "but was: " + actualRels, 
                TOComparator.areTOCollectionsEqual(expectedRels, actualRels));
        
        //filter on speciesIds and RelationStatus
        expectedRels = Arrays.asList(
        //direct relations
        new RelationTO<>(null, "Stage_id2", "Stage_id1", RelationType.ISA_PARTOF, RelationStatus.DIRECT), 
        new RelationTO<>(null, "Stage_id4", "Stage_id2", RelationType.ISA_PARTOF, RelationStatus.DIRECT), 
        new RelationTO<>(null, "Stage_id5", "Stage_id1", RelationType.ISA_PARTOF, RelationStatus.DIRECT), 
        new RelationTO<>(null, "Stage_id14", "Stage_id1", RelationType.ISA_PARTOF, RelationStatus.DIRECT), 
        new RelationTO<>(null, "Stage_id6", "Stage_id5", RelationType.ISA_PARTOF, RelationStatus.DIRECT), 
        new RelationTO<>(null, "Stage_id7", "Stage_id5", RelationType.ISA_PARTOF, RelationStatus.DIRECT), 
        new RelationTO<>(null, "Stage_id15", "Stage_id14", RelationType.ISA_PARTOF, RelationStatus.DIRECT), 
        new RelationTO<>(null, "Stage_id18", "Stage_id14", RelationType.ISA_PARTOF, RelationStatus.DIRECT), 
        new RelationTO<>(null, "Stage_id8", "Stage_id7", RelationType.ISA_PARTOF, RelationStatus.DIRECT), 
        new RelationTO<>(null, "Stage_id16", "Stage_id15", RelationType.ISA_PARTOF, RelationStatus.DIRECT), 
        new RelationTO<>(null, "Stage_id17", "Stage_id15", RelationType.ISA_PARTOF, RelationStatus.DIRECT), 
        //indirect relations
        new RelationTO<>(null, "Stage_id4", "Stage_id1", RelationType.ISA_PARTOF, RelationStatus.INDIRECT), 
        new RelationTO<>(null, "Stage_id6", "Stage_id1", RelationType.ISA_PARTOF, RelationStatus.INDIRECT), 
        new RelationTO<>(null, "Stage_id7", "Stage_id1", RelationType.ISA_PARTOF, RelationStatus.INDIRECT), 
        new RelationTO<>(null, "Stage_id8", "Stage_id1", RelationType.ISA_PARTOF, RelationStatus.INDIRECT), 
        new RelationTO<>(null, "Stage_id15", "Stage_id1", RelationType.ISA_PARTOF, RelationStatus.INDIRECT), 
        new RelationTO<>(null, "Stage_id16", "Stage_id1", RelationType.ISA_PARTOF, RelationStatus.INDIRECT), 
        new RelationTO<>(null, "Stage_id17", "Stage_id1", RelationType.ISA_PARTOF, RelationStatus.INDIRECT), 
        new RelationTO<>(null, "Stage_id18", "Stage_id1", RelationType.ISA_PARTOF, RelationStatus.INDIRECT), 
        new RelationTO<>(null, "Stage_id8", "Stage_id5", RelationType.ISA_PARTOF, RelationStatus.INDIRECT), 
        new RelationTO<>(null, "Stage_id16", "Stage_id14", RelationType.ISA_PARTOF, RelationStatus.INDIRECT), 
        new RelationTO<>(null, "Stage_id17", "Stage_id14", RelationType.ISA_PARTOF, RelationStatus.INDIRECT));
        actualRels = dao.getStageRelationsBySpeciesIds(
                new HashSet<>(Arrays.asList(11, 31)), new HashSet<RelationStatus>(
                    Arrays.asList(RelationStatus.DIRECT, RelationStatus.INDIRECT))).getAllTOs();
        assertTrue("RelationTOs incorrectly retrieved, expected: " + expectedRels + " - " +
                "but was: " + actualRels, 
                TOComparator.areTOCollectionsEqual(expectedRels, actualRels));
        
        //weird filter on species IDs
        expectedRels = directRelTOs;
        actualRels = dao.getStageRelationsBySpeciesIds(
                new HashSet<>(Arrays.asList(11, 21, 31, 44)), 
                new HashSet<RelationStatus>(Arrays.asList(RelationStatus.DIRECT))).getAllTOs();
        assertTrue("RelationTOs incorrectly retrieved, expected: " + expectedRels + " - " +
                "but was: " + actualRels, 
                TOComparator.areTOCollectionsEqual(expectedRels, actualRels));
    }
    
    /**
     * Test the method {@link MySQLRelationDAO#getStageRelations(Collection, boolean, 
     * Collection, Collection, boolean, Collection, Collection)}.
     */
    @Test
    public void shouldGetStageRelations() throws SQLException {

        this.useSelectDB();

        MySQLRelationDAO dao = new MySQLRelationDAO(this.getMySQLDAOManager());
        //XXX: for now, we don't generate any stageRelationId (always set to 0), 
        //so we don't retrieve it. This might change in the future.
                
        // Retrieve relations with several species IDs, any species requested
        Collection<RelationTO<String>> expectedRelations = Arrays.asList(
            new RelationTO<>(null, "Stage_id1", "Stage_id1", RelationType.ISA_PARTOF, RelationStatus.REFLEXIVE),
            new RelationTO<>(null, "Stage_id2", "Stage_id2", RelationType.ISA_PARTOF, RelationStatus.REFLEXIVE),
            new RelationTO<>(null, "Stage_id3", "Stage_id3", RelationType.ISA_PARTOF, RelationStatus.REFLEXIVE),
            new RelationTO<>(null, "Stage_id5", "Stage_id5", RelationType.ISA_PARTOF, RelationStatus.REFLEXIVE),
            new RelationTO<>(null, "Stage_id6", "Stage_id6", RelationType.ISA_PARTOF, RelationStatus.REFLEXIVE),
            new RelationTO<>(null, "Stage_id7", "Stage_id7", RelationType.ISA_PARTOF, RelationStatus.REFLEXIVE),
            new RelationTO<>(null, "Stage_id8", "Stage_id8", RelationType.ISA_PARTOF, RelationStatus.REFLEXIVE),
            new RelationTO<>(null, "Stage_id9", "Stage_id9", RelationType.ISA_PARTOF, RelationStatus.REFLEXIVE),
            new RelationTO<>(null, "Stage_id10", "Stage_id10", RelationType.ISA_PARTOF, RelationStatus.REFLEXIVE),
            new RelationTO<>(null, "Stage_id11", "Stage_id11", RelationType.ISA_PARTOF, RelationStatus.REFLEXIVE),
            new RelationTO<>(null, "Stage_id12", "Stage_id12", RelationType.ISA_PARTOF, RelationStatus.REFLEXIVE),
            new RelationTO<>(null, "Stage_id13", "Stage_id13", RelationType.ISA_PARTOF, RelationStatus.REFLEXIVE),
            new RelationTO<>(null, "Stage_id14", "Stage_id14", RelationType.ISA_PARTOF, RelationStatus.REFLEXIVE),
            new RelationTO<>(null, "Stage_id15", "Stage_id15", RelationType.ISA_PARTOF, RelationStatus.REFLEXIVE),
            new RelationTO<>(null, "Stage_id16", "Stage_id16", RelationType.ISA_PARTOF, RelationStatus.REFLEXIVE),
            new RelationTO<>(null, "Stage_id18", "Stage_id18", RelationType.ISA_PARTOF, RelationStatus.REFLEXIVE), 
            new RelationTO<>(null, "Stage_id2", "Stage_id1", RelationType.ISA_PARTOF, RelationStatus.DIRECT), 
            new RelationTO<>(null, "Stage_id5", "Stage_id1", RelationType.ISA_PARTOF, RelationStatus.DIRECT), 
            new RelationTO<>(null, "Stage_id10", "Stage_id1", RelationType.ISA_PARTOF, RelationStatus.DIRECT), 
            new RelationTO<>(null, "Stage_id14", "Stage_id1", RelationType.ISA_PARTOF, RelationStatus.DIRECT), 
            new RelationTO<>(null, "Stage_id3", "Stage_id2", RelationType.ISA_PARTOF, RelationStatus.DIRECT), 
            new RelationTO<>(null, "Stage_id6", "Stage_id5", RelationType.ISA_PARTOF, RelationStatus.DIRECT), 
            new RelationTO<>(null, "Stage_id7", "Stage_id5", RelationType.ISA_PARTOF, RelationStatus.DIRECT), 
            new RelationTO<>(null, "Stage_id11", "Stage_id10", RelationType.ISA_PARTOF, RelationStatus.DIRECT), 
            new RelationTO<>(null, "Stage_id12", "Stage_id10", RelationType.ISA_PARTOF, RelationStatus.DIRECT), 
            new RelationTO<>(null, "Stage_id13", "Stage_id10", RelationType.ISA_PARTOF, RelationStatus.DIRECT), 
            new RelationTO<>(null, "Stage_id15", "Stage_id14", RelationType.ISA_PARTOF, RelationStatus.DIRECT), 
            new RelationTO<>(null, "Stage_id18", "Stage_id14", RelationType.ISA_PARTOF, RelationStatus.DIRECT), 
            new RelationTO<>(null, "Stage_id8", "Stage_id7", RelationType.ISA_PARTOF, RelationStatus.DIRECT), 
            new RelationTO<>(null, "Stage_id9", "Stage_id7", RelationType.ISA_PARTOF, RelationStatus.DIRECT), 
            new RelationTO<>(null, "Stage_id16", "Stage_id15", RelationType.ISA_PARTOF, RelationStatus.DIRECT), 
            new RelationTO<>(null, "Stage_id3", "Stage_id1", RelationType.ISA_PARTOF, RelationStatus.INDIRECT), 
            new RelationTO<>(null, "Stage_id6", "Stage_id1", RelationType.ISA_PARTOF, RelationStatus.INDIRECT), 
            new RelationTO<>(null, "Stage_id7", "Stage_id1", RelationType.ISA_PARTOF, RelationStatus.INDIRECT), 
            new RelationTO<>(null, "Stage_id8", "Stage_id1", RelationType.ISA_PARTOF, RelationStatus.INDIRECT), 
            new RelationTO<>(null, "Stage_id9", "Stage_id1", RelationType.ISA_PARTOF, RelationStatus.INDIRECT), 
            new RelationTO<>(null, "Stage_id11", "Stage_id1", RelationType.ISA_PARTOF, RelationStatus.INDIRECT), 
            new RelationTO<>(null, "Stage_id12", "Stage_id1", RelationType.ISA_PARTOF, RelationStatus.INDIRECT), 
            new RelationTO<>(null, "Stage_id13", "Stage_id1", RelationType.ISA_PARTOF, RelationStatus.INDIRECT), 
            new RelationTO<>(null, "Stage_id15", "Stage_id1", RelationType.ISA_PARTOF, RelationStatus.INDIRECT), 
            new RelationTO<>(null, "Stage_id16", "Stage_id1", RelationType.ISA_PARTOF, RelationStatus.INDIRECT), 
            new RelationTO<>(null, "Stage_id18", "Stage_id1", RelationType.ISA_PARTOF, RelationStatus.INDIRECT), 
            new RelationTO<>(null, "Stage_id8", "Stage_id5", RelationType.ISA_PARTOF, RelationStatus.INDIRECT), 
            new RelationTO<>(null, "Stage_id9", "Stage_id5", RelationType.ISA_PARTOF, RelationStatus.INDIRECT), 
            new RelationTO<>(null, "Stage_id16", "Stage_id14", RelationType.ISA_PARTOF, RelationStatus.INDIRECT));
        RelationTOResultSet<String> resultSet = dao.getStageRelations(Arrays.asList(11, 21), 
                true, null, null, null, null, null);
        assertTrue("RelationTOs incorrectly retrieved",
                TOComparator.areTOCollectionsEqual(expectedRelations, resultSet.getAllTOs()));

        //now, relations existing in all requested species
        expectedRelations = Arrays.asList(
            new RelationTO<>(null, "Stage_id1", "Stage_id1", RelationType.ISA_PARTOF, RelationStatus.REFLEXIVE),
            new RelationTO<>(null, "Stage_id2", "Stage_id2", RelationType.ISA_PARTOF, RelationStatus.REFLEXIVE),
            new RelationTO<>(null, "Stage_id5", "Stage_id5", RelationType.ISA_PARTOF, RelationStatus.REFLEXIVE),
            new RelationTO<>(null, "Stage_id6", "Stage_id6", RelationType.ISA_PARTOF, RelationStatus.REFLEXIVE),
            new RelationTO<>(null, "Stage_id7", "Stage_id7", RelationType.ISA_PARTOF, RelationStatus.REFLEXIVE),
            new RelationTO<>(null, "Stage_id14", "Stage_id14", RelationType.ISA_PARTOF, RelationStatus.REFLEXIVE),
            new RelationTO<>(null, "Stage_id15", "Stage_id15", RelationType.ISA_PARTOF, RelationStatus.REFLEXIVE),
            new RelationTO<>(null, "Stage_id2", "Stage_id1", RelationType.ISA_PARTOF, RelationStatus.DIRECT), 
            new RelationTO<>(null, "Stage_id5", "Stage_id1", RelationType.ISA_PARTOF, RelationStatus.DIRECT),  
            new RelationTO<>(null, "Stage_id14", "Stage_id1", RelationType.ISA_PARTOF, RelationStatus.DIRECT), 
            new RelationTO<>(null, "Stage_id6", "Stage_id5", RelationType.ISA_PARTOF, RelationStatus.DIRECT), 
            new RelationTO<>(null, "Stage_id7", "Stage_id5", RelationType.ISA_PARTOF, RelationStatus.DIRECT), 
            new RelationTO<>(null, "Stage_id15", "Stage_id14", RelationType.ISA_PARTOF, RelationStatus.DIRECT), 
            new RelationTO<>(null, "Stage_id6", "Stage_id1", RelationType.ISA_PARTOF, RelationStatus.INDIRECT), 
            new RelationTO<>(null, "Stage_id7", "Stage_id1", RelationType.ISA_PARTOF, RelationStatus.INDIRECT),  
            new RelationTO<>(null, "Stage_id15", "Stage_id1", RelationType.ISA_PARTOF, RelationStatus.INDIRECT));
        resultSet = dao.getStageRelations(Arrays.asList(11, 21), 
                false, null, null, null, null, null);
        assertTrue("RelationTOs incorrectly retrieved",
                TOComparator.areTOCollectionsEqual(expectedRelations, resultSet.getAllTOs()));

        //test with only one species
        expectedRelations = Arrays.asList(
            new RelationTO<>(null, "Stage_id1", "Stage_id1", RelationType.ISA_PARTOF, RelationStatus.REFLEXIVE),
            new RelationTO<>(null, "Stage_id2", "Stage_id2", RelationType.ISA_PARTOF, RelationStatus.REFLEXIVE),
            new RelationTO<>(null, "Stage_id5", "Stage_id5", RelationType.ISA_PARTOF, RelationStatus.REFLEXIVE),
            new RelationTO<>(null, "Stage_id6", "Stage_id6", RelationType.ISA_PARTOF, RelationStatus.REFLEXIVE),
            new RelationTO<>(null, "Stage_id7", "Stage_id7", RelationType.ISA_PARTOF, RelationStatus.REFLEXIVE),
            new RelationTO<>(null, "Stage_id8", "Stage_id8", RelationType.ISA_PARTOF, RelationStatus.REFLEXIVE),
            new RelationTO<>(null, "Stage_id14", "Stage_id14", RelationType.ISA_PARTOF, RelationStatus.REFLEXIVE),
            new RelationTO<>(null, "Stage_id15", "Stage_id15", RelationType.ISA_PARTOF, RelationStatus.REFLEXIVE),
            new RelationTO<>(null, "Stage_id16", "Stage_id16", RelationType.ISA_PARTOF, RelationStatus.REFLEXIVE),
            new RelationTO<>(null, "Stage_id18", "Stage_id18", RelationType.ISA_PARTOF, RelationStatus.REFLEXIVE), 
            new RelationTO<>(null, "Stage_id2", "Stage_id1", RelationType.ISA_PARTOF, RelationStatus.DIRECT), 
            new RelationTO<>(null, "Stage_id5", "Stage_id1", RelationType.ISA_PARTOF, RelationStatus.DIRECT), 
            new RelationTO<>(null, "Stage_id14", "Stage_id1", RelationType.ISA_PARTOF, RelationStatus.DIRECT), 
            new RelationTO<>(null, "Stage_id6", "Stage_id5", RelationType.ISA_PARTOF, RelationStatus.DIRECT), 
            new RelationTO<>(null, "Stage_id7", "Stage_id5", RelationType.ISA_PARTOF, RelationStatus.DIRECT), 
            new RelationTO<>(null, "Stage_id15", "Stage_id14", RelationType.ISA_PARTOF, RelationStatus.DIRECT), 
            new RelationTO<>(null, "Stage_id18", "Stage_id14", RelationType.ISA_PARTOF, RelationStatus.DIRECT), 
            new RelationTO<>(null, "Stage_id8", "Stage_id7", RelationType.ISA_PARTOF, RelationStatus.DIRECT), 
            new RelationTO<>(null, "Stage_id16", "Stage_id15", RelationType.ISA_PARTOF, RelationStatus.DIRECT), 
            new RelationTO<>(null, "Stage_id6", "Stage_id1", RelationType.ISA_PARTOF, RelationStatus.INDIRECT), 
            new RelationTO<>(null, "Stage_id7", "Stage_id1", RelationType.ISA_PARTOF, RelationStatus.INDIRECT), 
            new RelationTO<>(null, "Stage_id8", "Stage_id1", RelationType.ISA_PARTOF, RelationStatus.INDIRECT), 
            new RelationTO<>(null, "Stage_id15", "Stage_id1", RelationType.ISA_PARTOF, RelationStatus.INDIRECT), 
            new RelationTO<>(null, "Stage_id16", "Stage_id1", RelationType.ISA_PARTOF, RelationStatus.INDIRECT), 
            new RelationTO<>(null, "Stage_id18", "Stage_id1", RelationType.ISA_PARTOF, RelationStatus.INDIRECT), 
            new RelationTO<>(null, "Stage_id8", "Stage_id5", RelationType.ISA_PARTOF, RelationStatus.INDIRECT), 
            new RelationTO<>(null, "Stage_id16", "Stage_id14", RelationType.ISA_PARTOF, RelationStatus.INDIRECT));
        resultSet = dao.getStageRelations(Arrays.asList(11), null, null, null, null, null, null);
        assertTrue("RelationTOs incorrectly retrieved",
                TOComparator.areTOCollectionsEqual(expectedRelations, resultSet.getAllTOs()));
        
        //Now, we stop requesting the relation type and ID
        Collection<RelationDAO.Attribute> attrs = EnumSet.complementOf(
                EnumSet.of(RelationDAO.Attribute.RELATION_ID, RelationDAO.Attribute.RELATION_TYPE));
        expectedRelations = Arrays.asList(
            new RelationTO<>(null, "Stage_id1", "Stage_id1", null, RelationStatus.REFLEXIVE),
            new RelationTO<>(null, "Stage_id2", "Stage_id2", null, RelationStatus.REFLEXIVE),
            new RelationTO<>(null, "Stage_id5", "Stage_id5", null, RelationStatus.REFLEXIVE),
            new RelationTO<>(null, "Stage_id6", "Stage_id6", null, RelationStatus.REFLEXIVE),
            new RelationTO<>(null, "Stage_id7", "Stage_id7", null, RelationStatus.REFLEXIVE),
            new RelationTO<>(null, "Stage_id14", "Stage_id14", null, RelationStatus.REFLEXIVE),
            new RelationTO<>(null, "Stage_id15", "Stage_id15", null, RelationStatus.REFLEXIVE),
            new RelationTO<>(null, "Stage_id2", "Stage_id1", null, RelationStatus.DIRECT), 
            new RelationTO<>(null, "Stage_id5", "Stage_id1", null, RelationStatus.DIRECT),  
            new RelationTO<>(null, "Stage_id14", "Stage_id1", null, RelationStatus.DIRECT), 
            new RelationTO<>(null, "Stage_id6", "Stage_id5", null, RelationStatus.DIRECT), 
            new RelationTO<>(null, "Stage_id7", "Stage_id5", null, RelationStatus.DIRECT), 
            new RelationTO<>(null, "Stage_id15", "Stage_id14", null, RelationStatus.DIRECT), 
            new RelationTO<>(null, "Stage_id6", "Stage_id1", null, RelationStatus.INDIRECT), 
            new RelationTO<>(null, "Stage_id7", "Stage_id1", null, RelationStatus.INDIRECT),  
            new RelationTO<>(null, "Stage_id15", "Stage_id1", null, RelationStatus.INDIRECT));
        resultSet = dao.getStageRelations(Arrays.asList(11, 21), 
                false, null, null, null, null, attrs);
        assertTrue("RelationTOs incorrectly retrieved",
                TOComparator.areTOCollectionsEqual(expectedRelations, resultSet.getAllTOs()));

        //now we add filtering on sources/targets
        //filtering on sources only
        expectedRelations = Arrays.asList(
            new RelationTO<>(null, "Stage_id1", "Stage_id1", null, RelationStatus.REFLEXIVE),
            new RelationTO<>(null, "Stage_id5", "Stage_id5", null, RelationStatus.REFLEXIVE),
            new RelationTO<>(null, "Stage_id6", "Stage_id6", null, RelationStatus.REFLEXIVE),
            new RelationTO<>(null, "Stage_id15", "Stage_id15", null, RelationStatus.REFLEXIVE),
            new RelationTO<>(null, "Stage_id5", "Stage_id1", null, RelationStatus.DIRECT),  
            new RelationTO<>(null, "Stage_id6", "Stage_id5", null, RelationStatus.DIRECT), 
            new RelationTO<>(null, "Stage_id15", "Stage_id14", null, RelationStatus.DIRECT), 
            new RelationTO<>(null, "Stage_id6", "Stage_id1", null, RelationStatus.INDIRECT), 
            new RelationTO<>(null, "Stage_id15", "Stage_id1", null, RelationStatus.INDIRECT));
        resultSet = dao.getStageRelations(Arrays.asList(11, 21), 
                false, Arrays.asList("Stage_id1", "Stage_id5", "Stage_id6", "Stage_id15"), 
                null, null, null, attrs);
        assertTrue("RelationTOs incorrectly retrieved",
                TOComparator.areTOCollectionsEqual(expectedRelations, resultSet.getAllTOs()));

        //filtering on targets only
        expectedRelations = Arrays.asList(
            new RelationTO<>(null, "Stage_id1", "Stage_id1", null, RelationStatus.REFLEXIVE),
            new RelationTO<>(null, "Stage_id2", "Stage_id1", null, RelationStatus.DIRECT), 
            new RelationTO<>(null, "Stage_id5", "Stage_id1", null, RelationStatus.DIRECT),  
            new RelationTO<>(null, "Stage_id14", "Stage_id1", null, RelationStatus.DIRECT), 
            new RelationTO<>(null, "Stage_id6", "Stage_id1", null, RelationStatus.INDIRECT), 
            new RelationTO<>(null, "Stage_id7", "Stage_id1", null, RelationStatus.INDIRECT),  
            new RelationTO<>(null, "Stage_id15", "Stage_id1", null, RelationStatus.INDIRECT));
        resultSet = dao.getStageRelations(Arrays.asList(11, 21), 
                false, null, Arrays.asList("Stage_id1"), null, null, attrs);
        assertTrue("RelationTOs incorrectly retrieved",
                TOComparator.areTOCollectionsEqual(expectedRelations, resultSet.getAllTOs()));

        //OR condition on sources and targets
        expectedRelations = Arrays.asList(
            new RelationTO<>(null, "Stage_id1", "Stage_id1", null, RelationStatus.REFLEXIVE),
            new RelationTO<>(null, "Stage_id5", "Stage_id5", null, RelationStatus.REFLEXIVE),
            new RelationTO<>(null, "Stage_id6", "Stage_id6", null, RelationStatus.REFLEXIVE),
            new RelationTO<>(null, "Stage_id15", "Stage_id15", null, RelationStatus.REFLEXIVE),
            new RelationTO<>(null, "Stage_id2", "Stage_id1", null, RelationStatus.DIRECT), 
            new RelationTO<>(null, "Stage_id5", "Stage_id1", null, RelationStatus.DIRECT),  
            new RelationTO<>(null, "Stage_id6", "Stage_id5", null, RelationStatus.DIRECT), 
            new RelationTO<>(null, "Stage_id14", "Stage_id1", null, RelationStatus.DIRECT), 
            new RelationTO<>(null, "Stage_id15", "Stage_id14", null, RelationStatus.DIRECT), 
            new RelationTO<>(null, "Stage_id6", "Stage_id1", null, RelationStatus.INDIRECT), 
            new RelationTO<>(null, "Stage_id7", "Stage_id1", null, RelationStatus.INDIRECT),  
            new RelationTO<>(null, "Stage_id15", "Stage_id1", null, RelationStatus.INDIRECT));
        resultSet = dao.getStageRelations(Arrays.asList(11, 21), 
                false, Arrays.asList("Stage_id1", "Stage_id5", "Stage_id6", "Stage_id15"), 
                Arrays.asList("Stage_id1"), true, null, attrs);
        assertTrue("RelationTOs incorrectly retrieved",
                TOComparator.areTOCollectionsEqual(expectedRelations, resultSet.getAllTOs()));

        //AND condition on sources and targets
        expectedRelations = Arrays.asList(
            new RelationTO<>(null, "Stage_id1", "Stage_id1", null, RelationStatus.REFLEXIVE),
            new RelationTO<>(null, "Stage_id15", "Stage_id15", null, RelationStatus.REFLEXIVE),
            new RelationTO<>(null, "Stage_id5", "Stage_id1", null, RelationStatus.DIRECT),  
            new RelationTO<>(null, "Stage_id15", "Stage_id14", null, RelationStatus.DIRECT), 
            new RelationTO<>(null, "Stage_id6", "Stage_id1", null, RelationStatus.INDIRECT), 
            new RelationTO<>(null, "Stage_id15", "Stage_id1", null, RelationStatus.INDIRECT));
        resultSet = dao.getStageRelations(Arrays.asList(11, 21), 
                false, Arrays.asList("Stage_id1", "Stage_id5", "Stage_id6", "Stage_id15"), 
                Arrays.asList("Stage_id1", "Stage_id14", "Stage_id15"), false, null, attrs);
        assertTrue("RelationTOs incorrectly retrieved",
                TOComparator.areTOCollectionsEqual(expectedRelations, resultSet.getAllTOs()));

        //add filter on relation status
        expectedRelations = Arrays.asList(
            new RelationTO<>(null, "Stage_id5", "Stage_id1", null, RelationStatus.DIRECT),  
            new RelationTO<>(null, "Stage_id15", "Stage_id14", null, RelationStatus.DIRECT), 
            new RelationTO<>(null, "Stage_id6", "Stage_id1", null, RelationStatus.INDIRECT), 
            new RelationTO<>(null, "Stage_id15", "Stage_id1", null, RelationStatus.INDIRECT));
        resultSet = dao.getStageRelations(Arrays.asList(11, 21), 
                false, Arrays.asList("Stage_id1", "Stage_id5", "Stage_id6", "Stage_id15"), 
                Arrays.asList("Stage_id1", "Stage_id14", "Stage_id15"), false, 
                Arrays.asList(RelationStatus.DIRECT, RelationStatus.INDIRECT), attrs);
        assertTrue("RelationTOs incorrectly retrieved",
                TOComparator.areTOCollectionsEqual(expectedRelations, resultSet.getAllTOs()));
    }
    
    /**
     * Test the method {@link MySQLRelationDAO#getTaxonRelations(Collection, Collection, Boolean,
            Collection, boolean, Collection)}.
     */
    @Test
    public void shouldGetTaxonRelations() throws SQLException {

        this.useSelectDB();

        MySQLRelationDAO dao = new MySQLRelationDAO(this.getMySQLDAOManager());
        //XXX: for now, we don't generate any taxonRelationId (always set to 0), 
        //so we don't retrieve it. This might change in the future.
        
        Collection<RelationTO<Integer>> expectedRelations = Arrays.asList(
            new RelationTO<>(null, 111, 111, RelationType.ISA_PARTOF, RelationStatus.REFLEXIVE),
            new RelationTO<>(null, 211, 211, RelationType.ISA_PARTOF, RelationStatus.REFLEXIVE),
            new RelationTO<>(null, 311, 311, RelationType.ISA_PARTOF, RelationStatus.REFLEXIVE),
            new RelationTO<>(null, 411, 411, RelationType.ISA_PARTOF, RelationStatus.REFLEXIVE),
            new RelationTO<>(null, 511, 511, RelationType.ISA_PARTOF, RelationStatus.REFLEXIVE),
            new RelationTO<>(null, 611, 611, RelationType.ISA_PARTOF, RelationStatus.REFLEXIVE),
            new RelationTO<>(null, 711, 711, RelationType.ISA_PARTOF, RelationStatus.REFLEXIVE), 
            
            new RelationTO<>(null, 211, 111, RelationType.ISA_PARTOF, RelationStatus.DIRECT), 
            new RelationTO<>(null, 311, 111, RelationType.ISA_PARTOF, RelationStatus.DIRECT), 
            new RelationTO<>(null, 711, 111, RelationType.ISA_PARTOF, RelationStatus.DIRECT), 
            new RelationTO<>(null, 411, 311, RelationType.ISA_PARTOF, RelationStatus.DIRECT), 
            new RelationTO<>(null, 511, 311, RelationType.ISA_PARTOF, RelationStatus.DIRECT), 
            new RelationTO<>(null, 611, 511, RelationType.ISA_PARTOF, RelationStatus.DIRECT), 
            
            new RelationTO<>(null, 411, 111, RelationType.ISA_PARTOF, RelationStatus.INDIRECT), 
            new RelationTO<>(null, 511, 111, RelationType.ISA_PARTOF, RelationStatus.INDIRECT), 
            new RelationTO<>(null, 611, 111, RelationType.ISA_PARTOF, RelationStatus.INDIRECT), 
            new RelationTO<>(null, 611, 311, RelationType.ISA_PARTOF, RelationStatus.INDIRECT));
        RelationTOResultSet<Integer> resultSet = dao.getTaxonRelations(null, null, null, null, false, null);
        assertTrue("RelationTOs incorrectly retrieved",
                TOComparator.areTOCollectionsEqual(expectedRelations, resultSet.getAllTOs()));

        //All rels but only connecting LCA taxa
        expectedRelations = Arrays.asList(
                new RelationTO<>(null, 111, 111, RelationType.ISA_PARTOF, RelationStatus.REFLEXIVE),
                new RelationTO<>(null, 411, 411, RelationType.ISA_PARTOF, RelationStatus.REFLEXIVE),
                new RelationTO<>(null, 511, 511, RelationType.ISA_PARTOF, RelationStatus.REFLEXIVE),
                new RelationTO<>(null, 611, 611, RelationType.ISA_PARTOF, RelationStatus.REFLEXIVE),
                
                new RelationTO<>(null, 611, 511, RelationType.ISA_PARTOF, RelationStatus.DIRECT), 
                
                new RelationTO<>(null, 411, 111, RelationType.ISA_PARTOF, RelationStatus.INDIRECT), 
                new RelationTO<>(null, 511, 111, RelationType.ISA_PARTOF, RelationStatus.INDIRECT), 
                new RelationTO<>(null, 611, 111, RelationType.ISA_PARTOF, RelationStatus.INDIRECT));
            resultSet = dao.getTaxonRelations(null, null, null, null, true, null);
            assertTrue("RelationTOs incorrectly retrieved",
                    TOComparator.areTOCollectionsEqual(expectedRelations, resultSet.getAllTOs()));

        //Now, we stop requesting the relation type and ID
        Collection<RelationDAO.Attribute> attrs = EnumSet.complementOf(
                EnumSet.of(RelationDAO.Attribute.RELATION_ID, RelationDAO.Attribute.RELATION_TYPE));
        expectedRelations = Arrays.asList(
            new RelationTO<>(null, 111, 111, null, RelationStatus.REFLEXIVE),
            new RelationTO<>(null, 211, 211, null, RelationStatus.REFLEXIVE),
            new RelationTO<>(null, 311, 311, null, RelationStatus.REFLEXIVE),
            new RelationTO<>(null, 411, 411, null, RelationStatus.REFLEXIVE),
            new RelationTO<>(null, 511, 511, null, RelationStatus.REFLEXIVE),
            new RelationTO<>(null, 611, 611, null, RelationStatus.REFLEXIVE),
            new RelationTO<>(null, 711, 711, null, RelationStatus.REFLEXIVE), 
            
            new RelationTO<>(null, 211, 111, null, RelationStatus.DIRECT), 
            new RelationTO<>(null, 311, 111, null, RelationStatus.DIRECT), 
            new RelationTO<>(null, 711, 111, null, RelationStatus.DIRECT), 
            new RelationTO<>(null, 411, 311, null, RelationStatus.DIRECT), 
            new RelationTO<>(null, 511, 311, null, RelationStatus.DIRECT), 
            new RelationTO<>(null, 611, 511, null, RelationStatus.DIRECT), 
            
            new RelationTO<>(null, 411, 111, null, RelationStatus.INDIRECT), 
            new RelationTO<>(null, 511, 111, null, RelationStatus.INDIRECT), 
            new RelationTO<>(null, 611, 111, null, RelationStatus.INDIRECT), 
            new RelationTO<>(null, 611, 311, null, RelationStatus.INDIRECT));
        resultSet = dao.getTaxonRelations(null, null, null, null, false, attrs);
        assertTrue("RelationTOs incorrectly retrieved",
                TOComparator.areTOCollectionsEqual(expectedRelations, resultSet.getAllTOs()));
        
        //now we add filtering on sources/targets
        //filtering on sources only
        expectedRelations = Arrays.asList(
            new RelationTO<>(null, 111, 111, null, RelationStatus.REFLEXIVE),
            new RelationTO<>(null, 611, 611, null, RelationStatus.REFLEXIVE),
            
            new RelationTO<>(null, 611, 511, null, RelationStatus.DIRECT), 
            
            new RelationTO<>(null, 611, 111, null, RelationStatus.INDIRECT), 
            new RelationTO<>(null, 611, 311, null, RelationStatus.INDIRECT));
        resultSet = dao.getTaxonRelations(Arrays.asList(111, 611), null, null, null, false, attrs);
        assertTrue("RelationTOs incorrectly retrieved",
                TOComparator.areTOCollectionsEqual(expectedRelations, resultSet.getAllTOs()));

        //filtering on targets only
        expectedRelations = Arrays.asList(
            new RelationTO<>(null, 111, 111, null, RelationStatus.REFLEXIVE),
            new RelationTO<>(null, 611, 611, null, RelationStatus.REFLEXIVE),
            
            new RelationTO<>(null, 211, 111, null, RelationStatus.DIRECT), 
            new RelationTO<>(null, 311, 111, null, RelationStatus.DIRECT), 
            new RelationTO<>(null, 711, 111, null, RelationStatus.DIRECT), 
            
            new RelationTO<>(null, 411, 111, null, RelationStatus.INDIRECT), 
            new RelationTO<>(null, 511, 111, null, RelationStatus.INDIRECT), 
            new RelationTO<>(null, 611, 111, null, RelationStatus.INDIRECT));
        resultSet = dao.getTaxonRelations(null, Arrays.asList(111, 611), null, null, false, attrs);
        assertTrue("RelationTOs incorrectly retrieved",
                TOComparator.areTOCollectionsEqual(expectedRelations, resultSet.getAllTOs()));
        
        //OR condition on sources and targets
        expectedRelations = Arrays.asList(
            new RelationTO<>(null, 111, 111, null, RelationStatus.REFLEXIVE),
            new RelationTO<>(null, 611, 611, null, RelationStatus.REFLEXIVE),
            
            new RelationTO<>(null, 211, 111, null, RelationStatus.DIRECT), 
            new RelationTO<>(null, 311, 111, null, RelationStatus.DIRECT), 
            new RelationTO<>(null, 711, 111, null, RelationStatus.DIRECT), 
            new RelationTO<>(null, 611, 511, null, RelationStatus.DIRECT), 
            
            new RelationTO<>(null, 411, 111, null, RelationStatus.INDIRECT), 
            new RelationTO<>(null, 511, 111, null, RelationStatus.INDIRECT), 
            new RelationTO<>(null, 611, 111, null, RelationStatus.INDIRECT), 
            new RelationTO<>(null, 611, 311, null, RelationStatus.INDIRECT));
        resultSet = dao.getTaxonRelations(Arrays.asList(111, 611), Arrays.asList(111, 611), 
                true, null, false, attrs);
        assertTrue("RelationTOs incorrectly retrieved",
                TOComparator.areTOCollectionsEqual(expectedRelations, resultSet.getAllTOs()));
        
        //OR condition on sources and targets with LCA filtering
        expectedRelations = Arrays.asList(
                new RelationTO<>(null, 111, 111, RelationType.ISA_PARTOF, RelationStatus.REFLEXIVE),
                new RelationTO<>(null, 311, 311, RelationType.ISA_PARTOF, RelationStatus.REFLEXIVE), 
                
                new RelationTO<>(null, 311, 111, RelationType.ISA_PARTOF, RelationStatus.DIRECT),
                new RelationTO<>(null, 411, 311, RelationType.ISA_PARTOF, RelationStatus.DIRECT), 
                new RelationTO<>(null, 511, 311, RelationType.ISA_PARTOF, RelationStatus.DIRECT),
                
                new RelationTO<>(null, 411, 111, RelationType.ISA_PARTOF, RelationStatus.INDIRECT), 
                new RelationTO<>(null, 511, 111, RelationType.ISA_PARTOF, RelationStatus.INDIRECT), 
                new RelationTO<>(null, 611, 111, RelationType.ISA_PARTOF, RelationStatus.INDIRECT), 
                new RelationTO<>(null, 611, 311, RelationType.ISA_PARTOF, RelationStatus.INDIRECT));
        resultSet = dao.getTaxonRelations(Arrays.asList(111, 311), Arrays.asList(111, 311), 
                true, null, true, attrs);
        assertTrue("RelationTOs incorrectly retrieved",
                TOComparator.areTOCollectionsEqual(expectedRelations, resultSet.getAllTOs()));
        
        //AND condition on sources and targets
        expectedRelations = Arrays.asList(
            new RelationTO<>(null, 111, 111, null, RelationStatus.REFLEXIVE),
            new RelationTO<>(null, 611, 611, null, RelationStatus.REFLEXIVE),
            
            new RelationTO<>(null, 611, 111, null, RelationStatus.INDIRECT));
        resultSet = dao.getTaxonRelations(Arrays.asList(111, 611), Arrays.asList(111, 611), 
                false, null, false, attrs);
        assertTrue("RelationTOs incorrectly retrieved",
                TOComparator.areTOCollectionsEqual(expectedRelations, resultSet.getAllTOs()));
        //LCA parameter shouldn't change anything here, we keep anyway all relations connecting
        //both sources and targets
        resultSet = dao.getTaxonRelations(Arrays.asList(111, 611), Arrays.asList(111, 611), 
                false, null, true, attrs);
        assertTrue("RelationTOs incorrectly retrieved",
                TOComparator.areTOCollectionsEqual(expectedRelations, resultSet.getAllTOs()));
        
        //add filter on relation status
        expectedRelations = Arrays.asList(
                new RelationTO<>(null, 611, 111, null, RelationStatus.INDIRECT));
        resultSet = dao.getTaxonRelations(Arrays.asList(111, 611), Arrays.asList(111, 611), 
                false, EnumSet.of(RelationStatus.DIRECT, RelationStatus.INDIRECT), false, attrs);
        assertTrue("RelationTOs incorrectly retrieved",
                TOComparator.areTOCollectionsEqual(expectedRelations, resultSet.getAllTOs()));
    }

    /**
     * Test the insert method {@link MySQLRelationDAO#insertAnatEntityRelations()}.
     */
    @Test
    public void shouldInsertAnatEntityRelations() throws SQLException {
        
        this.useEmptyDB();

        //create a Collection of TaxonConstraintTO to be inserted
        Collection<RelationTO<String>> relationTOs = Arrays.asList(
                new RelationTO<>(1, "sourceId1", "targetId1", RelationType.ISA_PARTOF, RelationStatus.DIRECT),
                new RelationTO<>(2, "sourceId2", "targetId2", RelationType.DEVELOPSFROM, RelationStatus.REFLEXIVE),
                new RelationTO<>(3, "sourceId3", "targetId3", RelationType.TRANSFORMATIONOF, RelationStatus.INDIRECT));

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
            
            this.thrown.expect(IllegalArgumentException.class);
            dao.insertAnatEntityRelations(new HashSet<RelationTO<String>>());
        } finally {
            this.emptyAndUseDefaultDB();
        }
    }
    
    /**
     * Test the insert method {@link MySQLRelationDAO#insertGeneOntologyRelations(Collection)}.
     */
    @Test
    public void shouldInsertGeneOntologyRelations() throws SQLException {
        
        this.useEmptyDB();
        
        //create a Collection of TaxonConstraintTO to be inserted
        Collection<RelationTO<String>> relationTOs = Arrays.asList(
                new RelationTO<>(1, "sourceId1", "targetId1", RelationType.ISA_PARTOF, RelationStatus.DIRECT),
                new RelationTO<>("sourceId2", "targetId2"),
                new RelationTO<>("sourceId3", "targetId3"));

        try {
            MySQLRelationDAO dao = new MySQLRelationDAO(this.getMySQLDAOManager());
            assertEquals("Incorrect number of rows inserted", 3, 
                    dao.insertGeneOntologyRelations(relationTOs));
            
            //we manually verify the insertion, as we do not want to rely on other methods 
            //that are tested elsewhere.
            try (BgeePreparedStatement stmt = this.getMySQLDAOManager().getConnection().
                    prepareStatement("select 1 from geneOntologyRelation " +
                            "where goAllSourceId = ? AND goAllTargetId = ?")) {
                
                stmt.setString(1, "sourceId1");
                stmt.setString(2, "targetId1");
                assertTrue("RelationTO (GeneOntologyRelations) incorrectly inserted", 
                        stmt.getRealPreparedStatement().executeQuery().next());
                
                stmt.setString(1, "sourceId2");
                stmt.setString(2, "targetId2");
                assertTrue("RelationTO (GeneOntologyRelations) incorrectly inserted", 
                        stmt.getRealPreparedStatement().executeQuery().next());

                stmt.setString(1, "sourceId3");
                stmt.setString(2, "targetId3");
                assertTrue("RelationTO (GeneOntologyRelations) incorrectly inserted", 
                        stmt.getRealPreparedStatement().executeQuery().next());
            }
            
            this.thrown.expect(IllegalArgumentException.class);
            dao.insertAnatEntityRelations(new HashSet<RelationTO<String>>());
        } finally {
            this.emptyAndUseDefaultDB();
        }
    }
}
