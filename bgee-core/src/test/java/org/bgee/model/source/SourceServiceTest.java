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
import java.util.List;

import org.bgee.model.TestAncestor;
import org.bgee.model.dao.api.DAOManager;
import org.bgee.model.dao.api.source.SourceDAO;
import org.bgee.model.dao.api.source.SourceDAO.SourceTO;
import org.bgee.model.dao.api.source.SourceDAO.SourceTOResultSet;
import org.junit.Test;

/**
 * This class holds the unit tests for the {@code SourceService} class.
 * 
 * @author  Valentine Rech de Laval
 * @version Bgee 13, Mar. 2016
 * @since   Bgee 13, Nov. 2016
 */
public class SourceServiceTest extends TestAncestor {

    @Test
    public void shouldLoadSources() {
        // initialize mocks
        DAOManager managerMock = mock(DAOManager.class);
        SourceDAO dao = mock(SourceDAO.class);
        when(managerMock.getSourceDAO()).thenReturn(dao);
        
        Date date1 = java.util.Date.from(LocalDate.of(2012, Month.OCTOBER, 20)
                .atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());

        SourceTOResultSet mockSourceRs = getMockResultSet(SourceTOResultSet.class,
                Arrays.asList(
                        new SourceTO("2", "NCBI Taxonomy", "Source taxonomy used in Bgee", "", "", "",
                                "http://www.ncbi.nlm.nih.gov/taxonomy", 
                                date1, "v13", false, SourceTO.SourceCategory.NONE, 3),
                        new SourceTO("4", "ZFIN", "ZFIN desc", 
                                "http://zfin.org/cgi-bin/ZFIN_jump?record=[xref_id]",
                                "http://zfin.org/cgi-bin/ZFIN_jump?record=[experiment_id]", 
                                "http://zfin.org/cgi-bin/ZFIN_jump?record=[evidence_id]", 
                                "http://zfin.org/", null, "rv:2", true, SourceTO.SourceCategory.IN_SITU, 2)));

        when(dao.getAllDataSources(null)).thenReturn(mockSourceRs);

        List<Source> expectedSources = new ArrayList<Source>();
        expectedSources.add(new Source("2", "NCBI Taxonomy", "Source taxonomy used in Bgee", "", "", "",
                "http://www.ncbi.nlm.nih.gov/taxonomy", 
                date1, "v13", false, org.bgee.model.source.SourceCategory.NONE, 3));
        expectedSources.add(new Source("4", "ZFIN", "ZFIN desc", 
                "http://zfin.org/cgi-bin/ZFIN_jump?record=[xref_id]",
                "http://zfin.org/cgi-bin/ZFIN_jump?record=[experiment_id]", 
                "http://zfin.org/cgi-bin/ZFIN_jump?record=[evidence_id]", 
                "http://zfin.org/", null, "rv:2", true, org.bgee.model.source.SourceCategory.IN_SITU, 2));
        
        SourceService service = new SourceService(managerMock);
        assertEquals("Incorrect sources", expectedSources, service.loadAllSources());
    }
    
    @Test
    public void shouldLoadDisplayableSources() {
        // initialize mocks
        DAOManager managerMock = mock(DAOManager.class);
        SourceDAO dao = mock(SourceDAO.class);
        when(managerMock.getSourceDAO()).thenReturn(dao);
        
        SourceTOResultSet mockSourceRs = getMockResultSet(SourceTOResultSet.class,
                Arrays.asList(
                        new SourceTO("4", "ZFIN", "ZFIN desc", 
                                "http://zfin.org/cgi-bin/ZFIN_jump?record=[xref_id]",
                                "http://zfin.org/cgi-bin/ZFIN_jump?record=[experiment_id]", 
                                "http://zfin.org/cgi-bin/ZFIN_jump?record=[evidence_id]", 
                                "http://zfin.org/", null, "rv:2", true, SourceTO.SourceCategory.IN_SITU, 2)));

        when(dao.getDisplayableDataSources(null)).thenReturn(mockSourceRs);

        List<Source> expectedSources = new ArrayList<Source>();
        expectedSources.add(new Source("4", "ZFIN", "ZFIN desc", 
                "http://zfin.org/cgi-bin/ZFIN_jump?record=[xref_id]",
                "http://zfin.org/cgi-bin/ZFIN_jump?record=[experiment_id]", 
                "http://zfin.org/cgi-bin/ZFIN_jump?record=[evidence_id]", 
                "http://zfin.org/", null, "rv:2", true, org.bgee.model.source.SourceCategory.IN_SITU, 2));
        
        SourceService service = new SourceService(managerMock);
        assertEquals("Incorrect sources", expectedSources, service.loadDisplayableSources());
    }
}
