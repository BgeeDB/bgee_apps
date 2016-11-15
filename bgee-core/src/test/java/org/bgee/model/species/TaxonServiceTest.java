package org.bgee.model.species;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.bgee.model.ServiceFactory;
import org.bgee.model.TestAncestor;
import org.bgee.model.dao.api.DAOManager;
import org.bgee.model.dao.api.species.TaxonDAO;
import org.bgee.model.dao.api.species.TaxonDAO.TaxonTO;
import org.bgee.model.dao.api.species.TaxonDAO.TaxonTOResultSet;
import org.junit.Test;

/**
 * This class holds the unit tests for the {@code TaxonService} class.
 * 
 * @author  Valentine Rech de Laval
 * @version Bgee 13, Aug. 2016
 * @since   Bgee 13, Aug. 2016
 */
public class TaxonServiceTest extends TestAncestor {

    @Test
    public void testLoadTaxa() {
        
        // initialize mocks
        DAOManager managerMock = mock(DAOManager.class);
        ServiceFactory serviceFactory = mock(ServiceFactory.class);
        when(serviceFactory.getDAOManager()).thenReturn(managerMock);
        TaxonDAO dao = mock(TaxonDAO.class);
        when(managerMock.getTaxonDAO()).thenReturn(dao);
        List<TaxonTO> taxonTOs = Arrays.asList(
                new TaxonTO("9443", "primates", "Primates", 3, 4, 3, true),
                new TaxonTO("40674", "mammals", "Mammalia", 2, 5, 2, false),
                new TaxonTO("7742", "vertebrates", "Vertebrata", 1, 6, 1, true));
        
        // Filter on species IDs is not tested here (tested in TaxonDAO)
        // but we need a variable to mock DAO answer
        Set<String> speciesIds = new HashSet<String>(Arrays.asList("11"));

        TaxonTOResultSet mockTaxonRs1 = getMockResultSet(TaxonTOResultSet.class, taxonTOs);
        when(dao.getLeastCommonAncestor(eq(speciesIds), eq(true))).thenReturn(mockTaxonRs1);
        
        List<Taxon> expectedTaxa = Arrays.asList(
                new Taxon("9443",  "primates", null), 
                new Taxon("40674", "mammals", null), 
                new Taxon("7742", "vertebrates", null));
        TaxonService service = new TaxonService(serviceFactory);
        assertEquals("Incorrect taxa", expectedTaxa,
                service.loadTaxa(speciesIds, true).collect(Collectors.toList()));
    }
    
    @Test
    public void testLoadAllLeastCommonAncestorAndParentTaxa() {
        
        // initialize mocks
        DAOManager managerMock = mock(DAOManager.class);
        ServiceFactory serviceFactory = mock(ServiceFactory.class);
        when(serviceFactory.getDAOManager()).thenReturn(managerMock);
        TaxonDAO dao = mock(TaxonDAO.class);
        when(managerMock.getTaxonDAO()).thenReturn(dao);
        List<TaxonTO> taxonTOs = Arrays.asList(
                new TaxonTO("9443", "primates", "Primates", 3, 4, 3, true),
                new TaxonTO("40674", "mammals", "Mammalia", 2, 5, 2, false),
                new TaxonTO("7742", "vertebrates", "Vertebrata", 1, 6, 1, true));
        
        TaxonTOResultSet mockTaxonRs1 = getMockResultSet(TaxonTOResultSet.class, taxonTOs);
        when(dao.getAllLeastCommonAncestorAndParentTaxa(null)).thenReturn(mockTaxonRs1);
        
        List<Taxon> expectedTaxa = Arrays.asList(
                new Taxon("9443",  "primates", null), 
                new Taxon("40674", "mammals", null), 
                new Taxon("7742", "vertebrates", null));
        TaxonService service = new TaxonService(serviceFactory);
        assertEquals("Incorrect taxa", expectedTaxa,
                service.loadAllLeastCommonAncestorAndParentTaxa().collect(Collectors.toList()));
    }

}
