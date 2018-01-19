package org.bgee.model.keyword;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bgee.model.ServiceFactory;
import org.bgee.model.TestAncestor;
import org.bgee.model.dao.api.DAOManager;
import org.bgee.model.dao.api.keyword.KeywordDAO;
import org.bgee.model.dao.api.keyword.KeywordDAO.EntityToKeywordTO;
import org.bgee.model.dao.api.keyword.KeywordDAO.EntityToKeywordTOResultSet;
import org.bgee.model.dao.api.keyword.KeywordDAO.KeywordTO;
import org.bgee.model.dao.api.keyword.KeywordDAO.KeywordTOResultSet;
import org.junit.Test;

/**
 * Unit tests for {@link KeywordService}.
 * 
 * @author  Frederic Bastian
 * @author  Valentine Rech de Laval
 * @version Bgee 14, Feb. 2017
 * @since   Bgee 13, Oct. 2015
 */
public class KeywordServiceTest extends TestAncestor {

    /**
     * Test {@link KeywordService#getKeywordForSpecies(Collection)}
     */
    @Test
    public void shouldGetKeywordForSpecies() {
        // initialize mocks
        DAOManager managerMock = mock(DAOManager.class);
        ServiceFactory serviceFactory = mock(ServiceFactory.class);
        when(serviceFactory.getDAOManager()).thenReturn(managerMock);
        KeywordDAO dao = mock(KeywordDAO.class);
        when(managerMock.getKeywordDAO()).thenReturn(dao);
        KeywordTOResultSet mockKeywordRs = getMockResultSet(KeywordTOResultSet.class, Arrays.asList(
                new KeywordTO(1, "k1 k1"), 
                new KeywordTO(2, "k2"), 
                new KeywordTO(3, "k33 33")));
        EntityToKeywordTOResultSet<Integer> mockEntityToKeywordRs =
            getMockResultSet(EntityToKeywordTOResultSet.class, 
                Arrays.asList(new EntityToKeywordTO<Integer>(1, 1), 
                              new EntityToKeywordTO<Integer>(1, 2), 
                              new EntityToKeywordTO<Integer>(2, 3)));
        Collection<Integer> speIds = Arrays.asList(1, 2, 3);
        when(dao.getKeywordsRelatedToSpecies(speIds)).thenReturn(mockKeywordRs);
        when(dao.getKeywordToSpecies(speIds)).thenReturn(mockEntityToKeywordRs);
        
        Map<Integer, Set<String>> expectedMapping = new HashMap<>();
        expectedMapping.put(1, new HashSet<>(Arrays.asList("k1 k1", "k2")));
        expectedMapping.put(2, new HashSet<>(Arrays.asList("k33 33")));
        KeywordService service = new KeywordService(serviceFactory);
        assertEquals("Incorrect species IDs to keywords mapping", expectedMapping, 
                service.getKeywordForSpecies(speIds));
        
        // Test with speciesIds is null
        mockKeywordRs = getMockResultSet(KeywordTOResultSet.class, Arrays.asList(
                new KeywordTO(1, "k1 k1"), 
                new KeywordTO(2, "k2"), 
                new KeywordTO(3, "k33 33")));
        mockEntityToKeywordRs = getMockResultSet(EntityToKeywordTOResultSet.class, 
                Arrays.asList(new EntityToKeywordTO<Integer>(1, 1), 
                        	  new EntityToKeywordTO<Integer>(1, 2), 
                              new EntityToKeywordTO<Integer>(2, 3), 
                              new EntityToKeywordTO<Integer>(3, 2)));
        when(dao.getKeywordsRelatedToSpecies(null)).thenReturn(mockKeywordRs);
        when(dao.getKeywordToSpecies(null)).thenReturn(mockEntityToKeywordRs);
        
        expectedMapping.put(3, new HashSet<>(Arrays.asList("k2")));
        assertEquals("Incorrect species IDs to keywords mapping", expectedMapping, 
                service.getKeywordForSpecies(null));

    }
}
