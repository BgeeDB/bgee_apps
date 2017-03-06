package org.bgee.model.source;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bgee.model.ServiceFactory;
import org.bgee.model.TestAncestor;
import org.bgee.model.dao.api.DAOManager;
import org.bgee.model.dao.api.source.SourceDAO;
import org.bgee.model.dao.api.source.SourceDAO.SourceTO;
import org.bgee.model.dao.api.source.SourceDAO.SourceTOResultSet;
import org.bgee.model.dao.api.source.SourceToSpeciesDAO;
import org.bgee.model.dao.api.source.SourceToSpeciesDAO.SourceToSpeciesTO;
import org.bgee.model.dao.api.source.SourceToSpeciesDAO.SourceToSpeciesTO.InfoType;
import org.bgee.model.dao.api.source.SourceToSpeciesDAO.SourceToSpeciesTOResultSet;
import org.bgee.model.expressiondata.baseelements.DataType;
import org.junit.Test;

/**
 * This class holds the unit tests for the {@code SourceService} class.
 * 
 * @author  Valentine Rech de Laval
 * @version Bgee 13, July 2016
 * @since   Bgee 13, Nov. 2016
 */
public class SourceServiceTest extends TestAncestor {

    @Test
    public void shouldLoadSources() {
        // initialize mocks
        DAOManager managerMock = mock(DAOManager.class);
        ServiceFactory serviceFactory = mock(ServiceFactory.class);
        when(serviceFactory.getDAOManager()).thenReturn(managerMock);
        SourceDAO sourceDao = mock(SourceDAO.class);
        when(managerMock.getSourceDAO()).thenReturn(sourceDao);
        
        Date date1 = java.util.Date.from(LocalDate.of(2012, Month.OCTOBER, 20)
                .atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());

        List<SourceTO> sourceTOsInDb = Arrays.asList(
                new SourceTO(2, "NCBI Taxonomy", "Source taxonomy used in Bgee", "", "", "",
                        "http://www.ncbi.nlm.nih.gov/taxonomy", 
                        date1, "v13", false, SourceTO.SourceCategory.NONE, 3),
                new SourceTO(4, "ZFIN", "ZFIN desc", 
                        "http://zfin.org/cgi-bin/ZFIN_jump?record=[xref_id]",
                        "http://zfin.org/cgi-bin/ZFIN_jump?record=[experiment_id]", 
                        "http://zfin.org/cgi-bin/ZFIN_jump?record=[evidence_id]", 
                        "http://zfin.org/", null, "rv:2", true, SourceTO.SourceCategory.IN_SITU, 2));
        // ResultSet cannot be reused. As we have 2 tests, we need 2 ResultSet
        SourceTOResultSet mockSourceRs = getMockResultSet(SourceTOResultSet.class, sourceTOsInDb);
        SourceTOResultSet mockSourceRs2 = getMockResultSet(SourceTOResultSet.class, sourceTOsInDb);
        when(sourceDao.getAllDataSources(null)).thenReturn(mockSourceRs).thenReturn(mockSourceRs2);

        SourceToSpeciesDAO sourceToSpeciesDao = mock(SourceToSpeciesDAO.class);
        when(managerMock.getSourceToSpeciesDAO()).thenReturn(sourceToSpeciesDao);
        SourceToSpeciesTOResultSet mockSourceToSpeciesRs = getMockResultSet(SourceToSpeciesTOResultSet.class,
                Arrays.asList(
                        new SourceToSpeciesTO(2, 11, SourceToSpeciesTO.DataType.IN_SITU, InfoType.ANNOTATION),
                        new SourceToSpeciesTO(2, 11, SourceToSpeciesTO.DataType.RNA_SEQ, InfoType.ANNOTATION),
                        new SourceToSpeciesTO(2, 11, SourceToSpeciesTO.DataType.IN_SITU, InfoType.DATA),
                        new SourceToSpeciesTO(2, 21, SourceToSpeciesTO.DataType.EST, InfoType.DATA),
                        new SourceToSpeciesTO(4, 11, SourceToSpeciesTO.DataType.AFFYMETRIX, InfoType.DATA)));
        when(sourceToSpeciesDao.getAllSourceToSpecies(null)).thenReturn(mockSourceToSpeciesRs);

        List<Source> expectedSources = new ArrayList<Source>();
        expectedSources.add(new Source(2, "NCBI Taxonomy", "Source taxonomy used in Bgee", "", "", "",
                "http://www.ncbi.nlm.nih.gov/taxonomy", 
                date1, "v13", false, org.bgee.model.source.SourceCategory.NONE, 3));
        expectedSources.add(new Source(4, "ZFIN", "ZFIN desc", 
                "http://zfin.org/cgi-bin/ZFIN_jump?record=[xref_id]",
                "http://zfin.org/cgi-bin/ZFIN_jump?record=[experiment_id]", 
                "http://zfin.org/cgi-bin/ZFIN_jump?record=[evidence_id]", 
                "http://zfin.org/", null, "rv:2", true, org.bgee.model.source.SourceCategory.IN_SITU, 2));
        
        SourceService service = new SourceService(serviceFactory);
        assertEquals("Incorrect sources", expectedSources, service.loadAllSources(false));

        // Test getting data by species
        expectedSources.clear();
        Map<Integer, Set<DataType>> forData2 = new HashMap<>();
        forData2.put(11, new HashSet<DataType>(Arrays.asList(DataType.IN_SITU)));
        forData2.put(21, new HashSet<DataType>(Arrays.asList(DataType.EST)));
        Map<Integer, Set<DataType>> forAnnot2 = new HashMap<>();
        forAnnot2.put(11, new HashSet<DataType>(Arrays.asList(DataType.IN_SITU, DataType.RNA_SEQ)));
        expectedSources.add(new Source(2, "NCBI Taxonomy", "Source taxonomy used in Bgee", "", "", "",
                "http://www.ncbi.nlm.nih.gov/taxonomy", 
                date1, "v13", false, org.bgee.model.source.SourceCategory.NONE, 3, forData2, forAnnot2));
        Map<Integer, Set<DataType>> forData4 = new HashMap<>();
        forData4.put(11, new HashSet<DataType>(Arrays.asList(DataType.AFFYMETRIX)));
        expectedSources.add(new Source(4, "ZFIN", "ZFIN desc", 
                "http://zfin.org/cgi-bin/ZFIN_jump?record=[xref_id]",
                "http://zfin.org/cgi-bin/ZFIN_jump?record=[experiment_id]", 
                "http://zfin.org/cgi-bin/ZFIN_jump?record=[evidence_id]", 
                "http://zfin.org/", null, "rv:2", true, org.bgee.model.source.SourceCategory.IN_SITU,
                2, forData4, null));

        assertEquals("Incorrect sources", expectedSources, service.loadAllSources(true));
    }
    
    @Test
    public void shouldLoadDisplayableSources() {
        
        // initialize mocks
        DAOManager managerMock = mock(DAOManager.class);
        ServiceFactory serviceFactory = mock(ServiceFactory.class);
        when(serviceFactory.getDAOManager()).thenReturn(managerMock);
        SourceDAO dao = mock(SourceDAO.class);
        when(managerMock.getSourceDAO()).thenReturn(dao);
         
        List<SourceTO> sourceTOsInDb = Arrays.asList(
                new SourceTO(4, "ZFIN", "ZFIN desc", 
                        "http://zfin.org/cgi-bin/ZFIN_jump?record=[xref_id]",
                        "http://zfin.org/cgi-bin/ZFIN_jump?record=[experiment_id]", 
                        "http://zfin.org/cgi-bin/ZFIN_jump?record=[evidence_id]", 
                        "http://zfin.org/", null, "rv:2", true, SourceTO.SourceCategory.IN_SITU, 2));
        // ResultSet cannot be reused. As we have 2 tests, we need 2 ResultSet
        SourceTOResultSet mockSourceRs = getMockResultSet(SourceTOResultSet.class, sourceTOsInDb);
        SourceTOResultSet mockSourceRs2 = getMockResultSet(SourceTOResultSet.class, sourceTOsInDb);
        when(dao.getDisplayableDataSources(null)).thenReturn(mockSourceRs).thenReturn(mockSourceRs2);

        SourceToSpeciesDAO sourceToSpeciesDao = mock(SourceToSpeciesDAO.class);
        when(managerMock.getSourceToSpeciesDAO()).thenReturn(sourceToSpeciesDao);
        SourceToSpeciesTOResultSet mockSourceToSpeciesRs = getMockResultSet(SourceToSpeciesTOResultSet.class,
                Arrays.asList(
                        new SourceToSpeciesTO(2, 21, SourceToSpeciesTO.DataType.EST, InfoType.DATA),
                        new SourceToSpeciesTO(4, 11, SourceToSpeciesTO.DataType.AFFYMETRIX, InfoType.ANNOTATION)));
        when(sourceToSpeciesDao.getAllSourceToSpecies(null)).thenReturn(mockSourceToSpeciesRs);

        List<Source> expectedSources = new ArrayList<Source>();
        expectedSources.add(new Source(4, "ZFIN", "ZFIN desc", 
                "http://zfin.org/cgi-bin/ZFIN_jump?record=[xref_id]",
                "http://zfin.org/cgi-bin/ZFIN_jump?record=[experiment_id]", 
                "http://zfin.org/cgi-bin/ZFIN_jump?record=[evidence_id]", 
                "http://zfin.org/", null, "rv:2", true, org.bgee.model.source.SourceCategory.IN_SITU, 2));
        
        SourceService service = new SourceService(serviceFactory);
        assertEquals("Incorrect sources", expectedSources, service.loadDisplayableSources(false));

        Map<Integer, Set<DataType>> forAnnot4 = new HashMap<>();
        forAnnot4.put(11, new HashSet<DataType>(Arrays.asList(DataType.AFFYMETRIX)));
        expectedSources.clear();
        expectedSources.add(new Source(4, "ZFIN", "ZFIN desc", 
                "http://zfin.org/cgi-bin/ZFIN_jump?record=[xref_id]",
                "http://zfin.org/cgi-bin/ZFIN_jump?record=[experiment_id]", 
                "http://zfin.org/cgi-bin/ZFIN_jump?record=[evidence_id]", 
                "http://zfin.org/", null, "rv:2", true, org.bgee.model.source.SourceCategory.IN_SITU,
                2, null, forAnnot4));
        assertEquals("Incorrect sources", expectedSources, service.loadDisplayableSources(true));
    }
}
