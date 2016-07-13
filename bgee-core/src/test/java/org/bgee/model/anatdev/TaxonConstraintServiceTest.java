package org.bgee.model.anatdev;

import static org.junit.Assert.assertEquals;
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
import org.bgee.model.dao.api.anatdev.TaxonConstraintDAO;
import org.bgee.model.dao.api.anatdev.TaxonConstraintDAO.TaxonConstraintTO;
import org.bgee.model.dao.api.anatdev.TaxonConstraintDAO.TaxonConstraintTOResultSet;
import org.junit.Test;

/**
 * This class holds the unit tests for the {@code TaxonConstraintService} class.
 * 
 * @author  Valentine Rech de Laval
 * @version Bgee 13, May 2016
 * @since   Bgee 13, May 2016
 */
public class TaxonConstraintServiceTest extends TestAncestor {

    /**
     * Test the method 
     * {@link TaxonConstraintService#loadAnatEntityTaxonConstraintBySpeciesIds(java.util.Collection)}.
     */
    @Test
    public void shouldLoadAnatEntityTaxonConstraintBySpeciesIds() {

        DAOManager managerMock = mock(DAOManager.class);
        ServiceFactory serviceFactory = mock(ServiceFactory.class);
        when(serviceFactory.getDAOManager()).thenReturn(managerMock);
        TaxonConstraintDAO dao = mock(TaxonConstraintDAO.class);
        when(managerMock.getTaxonConstraintDAO()).thenReturn(dao);
        
        List<TaxonConstraintTO> taxonConstraintTOs = Arrays.asList(
                new TaxonConstraintTO("UBERON:0001853", "11"),
                new TaxonConstraintTO("UBERON:0001853", "41"),
                new TaxonConstraintTO("UBERON:0011606", null)); 

        // Filter on species IDs is not tested here (tested in TaxonConstraintDAO)
        // but we need a variable to mock DAO answer
        Set<String> speciesIds = new HashSet<String>();
        speciesIds.addAll(Arrays.asList("11", "41"));

        TaxonConstraintTOResultSet mockRs1 = 
                getMockResultSet(TaxonConstraintTOResultSet.class, taxonConstraintTOs);
        when(dao.getAnatEntityTaxonConstraints(speciesIds, null)).thenReturn(mockRs1);
        
        List<TaxonConstraint> expectedTCs = Arrays.asList(
                new TaxonConstraint("UBERON:0001853", "11"), 
                new TaxonConstraint("UBERON:0001853", "41"), 
                new TaxonConstraint("UBERON:0011606", null));
        TaxonConstraintService service = new TaxonConstraintService(serviceFactory);
        assertEquals("Incorrect anat. entity taxon constraints", expectedTCs,
                service.loadAnatEntityTaxonConstraintBySpeciesIds(speciesIds).collect(Collectors.toList()));
    }
    
    /**
     * Test the method 
     * {@link TaxonConstraintService#loadAnatEntityRelationTaxonConstraintBySpeciesIds(java.util.Collection)}.
     */
    @Test
    public void shouldLoadAnatEntityRelationTaxonConstraintBySpeciesIds() {

        DAOManager managerMock = mock(DAOManager.class);
        ServiceFactory serviceFactory = mock(ServiceFactory.class);
        when(serviceFactory.getDAOManager()).thenReturn(managerMock);
        TaxonConstraintDAO dao = mock(TaxonConstraintDAO.class);
        when(managerMock.getTaxonConstraintDAO()).thenReturn(dao);
        
        List<TaxonConstraintTO> taxonConstraintTOs = Arrays.asList(
                new TaxonConstraintTO("1", null),
                new TaxonConstraintTO("2", "11"),
                new TaxonConstraintTO("2", "21")); 

        // Filter on species IDs is not tested here (tested in TaxonConstraintDAO)
        // but we need a variable to mock DAO answer
        Set<String> speciesIds = new HashSet<String>();
        speciesIds.addAll(Arrays.asList("11", "21"));

        TaxonConstraintTOResultSet mockRs1 = 
                getMockResultSet(TaxonConstraintTOResultSet.class, taxonConstraintTOs);
        when(dao.getAnatEntityRelationTaxonConstraints(speciesIds, null)).thenReturn(mockRs1);
        
        List<TaxonConstraint> expectedTCs = Arrays.asList(
                new TaxonConstraint("1", null), 
                new TaxonConstraint("2", "11"), 
                new TaxonConstraint("2", "21"));
        TaxonConstraintService service = new TaxonConstraintService(serviceFactory);
        assertEquals("Incorrect anat. entity relation taxon constraints", expectedTCs,
                service.loadAnatEntityRelationTaxonConstraintBySpeciesIds(speciesIds)
                    .collect(Collectors.toList()));
    }

    /**
     * Test the method 
     * {@link TaxonConstraintService#loadDevStageTaxonConstraintBySpeciesIds(java.util.Collection)}.
     */
    @Test
    public void shouldLoadDevStageTaxonConstraintBySpeciesIds() {

        DAOManager managerMock = mock(DAOManager.class);
        ServiceFactory serviceFactory = mock(ServiceFactory.class);
        when(serviceFactory.getDAOManager()).thenReturn(managerMock);
        TaxonConstraintDAO dao = mock(TaxonConstraintDAO.class);
        when(managerMock.getTaxonConstraintDAO()).thenReturn(dao);
        
        List<TaxonConstraintTO> taxonConstraintTOs = Arrays.asList(
                new TaxonConstraintTO("Stage_id1", null),
                new TaxonConstraintTO("Stage_id2", null),
                new TaxonConstraintTO("Stage_id3", "21"), 
                new TaxonConstraintTO("Stage_id4", "31")); 

        // Filter on species IDs is not tested here (tested in TaxonConstraintDAO)
        // but we need a variable to mock DAO answer
        Set<String> speciesIds = new HashSet<String>();
        speciesIds.addAll(Arrays.asList("11", "21", "31"));

        TaxonConstraintTOResultSet mockRs1 = 
                getMockResultSet(TaxonConstraintTOResultSet.class, taxonConstraintTOs);
        when(dao.getStageTaxonConstraints(speciesIds, null)).thenReturn(mockRs1);
        
        List<TaxonConstraint> expectedTCs = Arrays.asList(
                new TaxonConstraint("Stage_id1", null), 
                new TaxonConstraint("Stage_id2", null), 
                new TaxonConstraint("Stage_id3", "21"), 
                new TaxonConstraint("Stage_id4", "31"));
        TaxonConstraintService service = new TaxonConstraintService(serviceFactory);
        assertEquals("Incorrect dev. stage taxon constraints", expectedTCs,
                service.loadDevStageTaxonConstraintBySpeciesIds(speciesIds).collect(Collectors.toList()));
    }
}
