package org.bgee.model.dao.mysql.keyword;

import static org.junit.Assert.assertTrue;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.TOComparator;
import org.bgee.model.dao.api.keyword.KeywordDAO;
import org.bgee.model.dao.api.keyword.KeywordDAO.EntityToKeywordTO;
import org.bgee.model.dao.api.keyword.KeywordDAO.KeywordTO;
import org.bgee.model.dao.mysql.MySQLITAncestor;
import org.junit.Test;

public class MySQLKeywordDAOIT extends MySQLITAncestor {
    
    private final static Logger log = LogManager.getLogger(MySQLKeywordDAOIT.class.getName());

    public MySQLKeywordDAOIT() {
        super();
    }
    
    @Override
    protected Logger getLogger() {
        return log;
    }
    
    /**
     * Test the select method {@link MySQLKeywordDAO#getKeywordsRelatedToSpecies(Collection)}.
     */
    @Test
    public void shouldGetKeywordsRelatedToSpecies() throws SQLException {
        
        this.useSelectDB();

        MySQLKeywordDAO dao = new MySQLKeywordDAO(this.getMySQLDAOManager());
        
        //first, get all keywords related to any species, with all attributes
        Collection<KeywordTO> expectedTOs = Arrays.asList(
                new KeywordTO("2", "keywordRelatedToSpecies11"), 
                new KeywordTO("3", "keywordRelatedToSpecies11_2"), 
                new KeywordTO("4", "keywordRelatedToSpecies21"), 
                new KeywordTO("5", "keywordRelatedToSpecies11And21"));
        
        assertTrue("KeywordTOs incorrectly retrieved", 
                TOComparator.areTOCollectionsEqual(
                        dao.getKeywordsRelatedToSpecies(null).getAllTOs(), 
                        expectedTOs));
        //providing an empty Collection of speciesId should give same result
        assertTrue("KeywordTOs incorrectly retrieved", 
                TOComparator.areTOCollectionsEqual(
                        dao.getKeywordsRelatedToSpecies(Arrays.asList()).getAllTOs(), 
                        expectedTOs));
        
        //attributes null, empty, or containing all attributes, should give same result
        dao.setAttributes((Collection<KeywordDAO.Attribute>) null);
        assertTrue("KeywordTOs incorrectly retrieved", 
                TOComparator.areTOCollectionsEqual(
                        dao.getKeywordsRelatedToSpecies(null).getAllTOs(), 
                        expectedTOs));
        dao.setAttributes(Arrays.asList());
        assertTrue("KeywordTOs incorrectly retrieved", 
                TOComparator.areTOCollectionsEqual(
                        dao.getKeywordsRelatedToSpecies(null).getAllTOs(), 
                        expectedTOs));
        dao.setAttributes(KeywordDAO.Attribute.values());
        assertTrue("KeywordTOs incorrectly retrieved", 
                TOComparator.areTOCollectionsEqual(
                        dao.getKeywordsRelatedToSpecies(null).getAllTOs(), 
                        expectedTOs));
        
        //Now, get all keywords related to only some species, with all attributes
        expectedTOs = Arrays.asList(
                new KeywordTO("2", "keywordRelatedToSpecies11"), 
                new KeywordTO("3", "keywordRelatedToSpecies11_2"), 
                new KeywordTO("5", "keywordRelatedToSpecies11And21"));
        assertTrue("KeywordTOs incorrectly retrieved", 
                TOComparator.areTOCollectionsEqual(
                        dao.getKeywordsRelatedToSpecies(Arrays.asList("11")).getAllTOs(), 
                        expectedTOs));
        
        expectedTOs = Arrays.asList(
                new KeywordTO("4", "keywordRelatedToSpecies21"), 
                new KeywordTO("5", "keywordRelatedToSpecies11And21"));
        assertTrue("KeywordTOs incorrectly retrieved", 
                TOComparator.areTOCollectionsEqual(
                        dao.getKeywordsRelatedToSpecies(Arrays.asList("21")).getAllTOs(), 
                        expectedTOs));
        
        expectedTOs = Arrays.asList(
                new KeywordTO("2", "keywordRelatedToSpecies11"), 
                new KeywordTO("3", "keywordRelatedToSpecies11_2"), 
                new KeywordTO("4", "keywordRelatedToSpecies21"), 
                new KeywordTO("5", "keywordRelatedToSpecies11And21"));
        assertTrue("KeywordTOs incorrectly retrieved", 
                TOComparator.areTOCollectionsEqual(
                        dao.getKeywordsRelatedToSpecies(Arrays.asList("11", "21")).getAllTOs(), 
                        expectedTOs));
        
        expectedTOs = Arrays.asList(
                new KeywordTO("2", "keywordRelatedToSpecies11"), 
                new KeywordTO("3", "keywordRelatedToSpecies11_2"), 
                new KeywordTO("5", "keywordRelatedToSpecies11And21"));
        assertTrue("KeywordTOs incorrectly retrieved", 
                TOComparator.areTOCollectionsEqual(
                        dao.getKeywordsRelatedToSpecies(Arrays.asList("11", "31")).getAllTOs(), 
                        expectedTOs));
        
        expectedTOs = Arrays.asList();
        assertTrue("KeywordTOs incorrectly retrieved", 
                TOComparator.areTOCollectionsEqual(
                        dao.getKeywordsRelatedToSpecies(Arrays.asList("31")).getAllTOs(), 
                        expectedTOs));
        
        
        //now, retrieve only some Attributes
        dao.setAttributes(KeywordDAO.Attribute.ID);
        expectedTOs = Arrays.asList(
                new KeywordTO("2", null), 
                new KeywordTO("3", null), 
                new KeywordTO("5", null));
        assertTrue("KeywordTOs incorrectly retrieved", 
                TOComparator.areTOCollectionsEqual(
                        dao.getKeywordsRelatedToSpecies(Arrays.asList("11")).getAllTOs(), 
                        expectedTOs));

        dao.setAttributes(KeywordDAO.Attribute.NAME);
        expectedTOs = Arrays.asList(
                new KeywordTO(null, "keywordRelatedToSpecies11"), 
                new KeywordTO(null, "keywordRelatedToSpecies11_2"), 
                new KeywordTO(null, "keywordRelatedToSpecies11And21"));
        assertTrue("KeywordTOs incorrectly retrieved", 
                TOComparator.areTOCollectionsEqual(
                        dao.getKeywordsRelatedToSpecies(Arrays.asList("11")).getAllTOs(), 
                        expectedTOs));
    }
    

    /**
     * Test the select method {@link MySQLKeywordDAO#getKeywordsToSpecies(Collection)}.
     */
    @Test
    public void shouldGetKeywordToSpecies() throws SQLException {
        
        this.useSelectDB();

        MySQLKeywordDAO dao = new MySQLKeywordDAO(this.getMySQLDAOManager());
        
        //first, get all species-keyword relations for any species
        Collection<EntityToKeywordTO> expectedTOs = Arrays.asList(
                new EntityToKeywordTO("11", "2"), 
                new EntityToKeywordTO("11", "3"), 
                new EntityToKeywordTO("21", "4"), 
                new EntityToKeywordTO("11", "5"), 
                new EntityToKeywordTO("21", "5"));
        
        assertTrue("EntityToKeywordTOs incorrectly retrieved", 
                TOComparator.areTOCollectionsEqual(
                        dao.getKeywordToSpecies(null).getAllTOs(), 
                        expectedTOs));
        //providing an empty Collection of speciesId should give same result
        assertTrue("EntityToKeywordTOs incorrectly retrieved", 
                TOComparator.areTOCollectionsEqual(
                        dao.getKeywordToSpecies(Arrays.asList()).getAllTOs(), 
                        expectedTOs));
        
        //now, get species-keyword relations for only some species
        expectedTOs = Arrays.asList(
                new EntityToKeywordTO("11", "2"), 
                new EntityToKeywordTO("11", "3"), 
                new EntityToKeywordTO("11", "5"));
        assertTrue("EntityToKeywordTOs incorrectly retrieved", 
                TOComparator.areTOCollectionsEqual(
                        dao.getKeywordToSpecies(Arrays.asList("11")).getAllTOs(), 
                        expectedTOs));
        
        expectedTOs = Arrays.asList(
                new EntityToKeywordTO("21", "4"), 
                new EntityToKeywordTO("21", "5"));
        assertTrue("EntityToKeywordTOs incorrectly retrieved", 
                TOComparator.areTOCollectionsEqual(
                        dao.getKeywordToSpecies(Arrays.asList("21")).getAllTOs(), 
                        expectedTOs));
        
        expectedTOs = Arrays.asList(
                new EntityToKeywordTO("11", "2"), 
                new EntityToKeywordTO("11", "3"), 
                new EntityToKeywordTO("21", "4"), 
                new EntityToKeywordTO("11", "5"), 
                new EntityToKeywordTO("21", "5"));
        assertTrue("EntityToKeywordTOs incorrectly retrieved", 
                TOComparator.areTOCollectionsEqual(
                        dao.getKeywordToSpecies(Arrays.asList("11", "21")).getAllTOs(), 
                        expectedTOs));

        expectedTOs = Arrays.asList(
                new EntityToKeywordTO("11", "2"), 
                new EntityToKeywordTO("11", "3"), 
                new EntityToKeywordTO("11", "5"));
        assertTrue("EntityToKeywordTOs incorrectly retrieved", 
                TOComparator.areTOCollectionsEqual(
                        dao.getKeywordToSpecies(Arrays.asList("11", "31")).getAllTOs(), 
                        expectedTOs));

        expectedTOs = Arrays.asList();
        assertTrue("EntityToKeywordTOs incorrectly retrieved", 
                TOComparator.areTOCollectionsEqual(
                        dao.getKeywordToSpecies(Arrays.asList("31")).getAllTOs(), 
                        expectedTOs));
    }

}
