package org.bgee.model.species;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;
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
 * @author  Frederic Bastian
 * @version Bgee 14 Mar. 2019
 * @since   Bgee 13, Aug. 2016
 */
public class TaxonServiceTest extends TestAncestor {

    /**
     * Unit test for {@link TaxonService#loadLeastCommonAncestor(Collection)}
     */
    @Test
    public void testLoadLeastCommonAncestor() {
        
        // initialize mocks
        DAOManager managerMock = mock(DAOManager.class);
        ServiceFactory serviceFactory = mock(ServiceFactory.class);
        when(serviceFactory.getDAOManager()).thenReturn(managerMock);
        TaxonDAO dao = mock(TaxonDAO.class);
        when(managerMock.getTaxonDAO()).thenReturn(dao);

        Set<Integer> speciesIds = new HashSet<>(Arrays.asList(11));

        when(dao.getLeastCommonAncestor(speciesIds, null)).thenReturn(
                new TaxonTO(9443, "primates", "Primates", 3, 4, 3, true));

        TaxonService service = new TaxonService(serviceFactory);
        assertEquals("Incorrect taxa", new Taxon(9443,  "primates", null),
                service.loadLeastCommonAncestor(speciesIds));
    }
    /**
     * Unit test for {@link TaxonService#loadTaxa(Collection, boolean)}
     */
    @Test
    public void testLoadTaxa() {
        
        // initialize mocks
        DAOManager managerMock = mock(DAOManager.class);
        ServiceFactory serviceFactory = mock(ServiceFactory.class);
        when(serviceFactory.getDAOManager()).thenReturn(managerMock);
        TaxonDAO dao = mock(TaxonDAO.class);
        when(managerMock.getTaxonDAO()).thenReturn(dao);
        Collection<Integer> taxIds = Arrays.asList(9443, 40674, 7742);
        List<TaxonTO> taxonTOs = Arrays.asList(
                new TaxonTO(9443, "primates", "Primates", 3, 4, 3, true),
                new TaxonTO(40674, "mammals", "Mammalia", 2, 5, 2, false),
                new TaxonTO(7742, "vertebrates", "Vertebrata", 1, 6, 1, true));

        TaxonTOResultSet mockTaxonRs1 = getMockResultSet(TaxonTOResultSet.class, taxonTOs);
        when(dao.getTaxa(taxIds, false, null)).thenReturn(mockTaxonRs1);
        
        Set<Taxon> expectedTaxa = new HashSet<>(Arrays.asList(
                new Taxon(9443,  "primates", null), 
                new Taxon(40674, "mammals", null), 
                new Taxon(7742, "vertebrates", null)));
        TaxonService service = new TaxonService(serviceFactory);
        assertEquals("Incorrect taxa", expectedTaxa,
                service.loadTaxa(taxIds, false).collect(Collectors.toSet()));
    }
}