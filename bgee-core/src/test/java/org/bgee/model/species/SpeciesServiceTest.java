package org.bgee.model.species;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.bgee.model.TestAncestor;
import org.bgee.model.dao.api.DAOManager;
import org.bgee.model.dao.api.species.SpeciesDAO;
import org.bgee.model.dao.api.species.SpeciesDAO.SpeciesTO;
import org.bgee.model.dao.api.species.SpeciesDAO.SpeciesTOResultSet;
import org.junit.Test;

/**
 * This class holds the unit tests for the {@code SpeciesService} class.
 * @author Philippe Moret
 */
public class SpeciesServiceTest extends TestAncestor {

	@Test
	public void testLoadAllSpecies() {
		// initialize mocks
		DAOManager managerMock = mock(DAOManager.class);
		SpeciesDAO speciesDAOMock = mock(SpeciesDAO.class);
		
		// mock behavior
        SpeciesTOResultSet resultSetMock = getMockResultSet(SpeciesTOResultSet.class, Arrays.asList(
                new SpeciesTO("9606", "human", "Homo", "sapiens", "4312", "3241", "321", null), 
                new SpeciesTO("1234", "name", "genus", "someSpecies", "1123", "3432241", "1321", null)));
		when(speciesDAOMock.getSpeciesFromDataGroups()).thenReturn(resultSetMock);
		when(managerMock.getSpeciesDAO()).thenReturn(speciesDAOMock);
		
		Set<Species> expected = new HashSet<>(Arrays.asList(
		        new Species("9606", "human", null, "Homo", "sapiens"), 
		        new Species("1234", "name", null, "genus", "someSpecies")));

		// actual use of the service
		SpeciesService service = new SpeciesService(managerMock);
		assertEquals(expected, service.loadSpeciesInDataGroups());

	}
}
