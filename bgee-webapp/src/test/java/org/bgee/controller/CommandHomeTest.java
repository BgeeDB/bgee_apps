package org.bgee.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.TestAncestor;
import org.bgee.controller.exception.PageNotFoundException;
import org.bgee.model.ServiceFactory;
import org.bgee.model.file.DownloadFile;
import org.bgee.model.file.DownloadFile.CategoryEnum;
import org.bgee.model.source.Source;
import org.bgee.model.file.SpeciesDataGroup;
import org.bgee.model.file.SpeciesDataGroupService;
import org.bgee.model.species.Species;
import org.bgee.view.GeneralDisplay;
import org.bgee.view.ViewFactory;
import org.junit.Test;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

public class CommandHomeTest extends TestAncestor {

    private final static Logger log = LogManager.getLogger(CommandAboutTest.class.getName());

    @Override
    protected Logger getLogger() {
        return log;
    }

    /**
     * Test {@link CommandHome#processRequest()}.
     */
    @Test
    public void shouldProcessRequest() throws IOException, PageNotFoundException {

        //mock Services
        ServiceFactory serviceFac = mock(ServiceFactory.class);
        SpeciesDataGroupService groupService = mock(SpeciesDataGroupService.class);
        when(serviceFac.getSpeciesDataGroupService()).thenReturn(groupService);

        Species spe1 = new Species(9606, "human", null, "Homo", "sapiens", "hsap1", new Source(1),
                0, 1234, null, null, 1);
        Species spe2 = new Species(10090, "mouse", null, "Mus", "musculus", "mmus1", new Source(1),
                0, 2322, null, null, 2);

        // We don't care about this set, it's not taken into account but to create a SpeciesDataGroup
        // downlqad files cannot be equals to null
        Set<DownloadFile> dlFileGroup1 = new HashSet<>();
        dlFileGroup1.add(new DownloadFile("my/path/file.tsv.zip", "file.tsv.zip", 
                CategoryEnum.EXPR_CALLS_SIMPLE, 200L, 11));

        SpeciesDataGroup group1 = new SpeciesDataGroup(11, "single spe g1", null,
                Collections.singletonList(spe1), dlFileGroup1);
        SpeciesDataGroup group2 = new SpeciesDataGroup(11, "single spe g1", null,
                Arrays.asList(spe1, spe2), dlFileGroup1);

        List<SpeciesDataGroup> groups = Arrays.asList(group1, group2);
        when(groupService.loadAllSpeciesDataGroup()).thenReturn(groups);

        //mock view
        ViewFactory viewFac = mock(ViewFactory.class);
        GeneralDisplay display = mock(GeneralDisplay.class);
        when(viewFac.getGeneralDisplay()).thenReturn(display);

        RequestParameters params = new RequestParameters();
        CommandHome controller = new CommandHome(mock(HttpServletResponse.class), params,
                mock(BgeeProperties.class), viewFac, serviceFac);
        controller.processRequest();
        verify(display).displayHomePage(Collections.singletonList(group1));

        params = new RequestParameters();
        params.setPage("any page");
        controller = new CommandHome(mock(HttpServletResponse.class), params,
                mock(BgeeProperties.class), viewFac, serviceFac);
        try {
            controller.processRequest();
            fail("A PageNotFoundException should be thrown");
        } catch (PageNotFoundException e) {
            // test passed
        }
    }
}
