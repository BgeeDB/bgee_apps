package org.bgee.controller;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.TestAncestor;
import org.bgee.controller.exception.PageNotFoundException;
import org.bgee.model.ServiceFactory;
import org.bgee.model.file.DownloadFile;
import org.bgee.model.file.DownloadFile.CategoryEnum;
import org.bgee.model.file.SpeciesDataGroup;
import org.bgee.model.file.SpeciesDataGroupService;
import org.bgee.model.keyword.KeywordService;
import org.bgee.model.source.Source;
import org.bgee.model.species.Species;
import org.bgee.view.DownloadDisplay;
import org.bgee.view.ViewFactory;
import org.junit.Test;

/**
 * Unit tests for {@link CommandDownload}.
 * 
 * @author  Frederic Bastian
 * @author  Valentine Rech de Laval
 * @version Bgee 14, Mar. 2017
 * @since   Bgee 13 Oct. 2015
 */
public class CommandDownloadTest extends TestAncestor {
    
    private final static Logger log = 
            LogManager.getLogger(CommandDownloadTest.class.getName());

    @Override
    protected Logger getLogger() {
        return log;
    }

    /**
     * Test {@link CommandDownload#processRequest()}.
     */
    @Test
    public void shouldProcessRequest() throws IOException, IllegalStateException, PageNotFoundException {
        //mock Services
        ServiceFactory serviceFac = mock(ServiceFactory.class);
        SpeciesDataGroupService groupService = mock(SpeciesDataGroupService.class);
        when(serviceFac.getSpeciesDataGroupService()).thenReturn(groupService);
        KeywordService keywordService = mock(KeywordService.class);
        when(serviceFac.getKeywordService()).thenReturn(keywordService);
        
        //mock data returned by Services
        List<SpeciesDataGroup> groups = getTestGroups();
        when(groupService.loadAllSpeciesDataGroup()).thenReturn(groups);
        when(keywordService.getKeywordForAllSpecies()).thenReturn(getTestSpeToKeywords());

        //mock view
        ViewFactory viewFac = mock(ViewFactory.class);
        DownloadDisplay display = mock(DownloadDisplay.class);
        when(viewFac.getDownloadDisplay()).thenReturn(display);
        
        //launch tests
        Map<Integer, Set<String>> speToTerms = getTestSpeciesToTerms();
        
        RequestParameters params = new RequestParameters();
        params.setPage(RequestParameters.PAGE_DOWNLOAD);
        params.setAction(RequestParameters.ACTION_DOWLOAD_CALL_FILES);
        CommandDownload controller = new CommandDownload(mock(HttpServletResponse.class), params, 
                mock(BgeeProperties.class), viewFac, serviceFac);
        controller.processRequest();
        verify(display).displayGeneExpressionCallDownloadPage(groups, speToTerms);
        
        params = new RequestParameters();
        params.setPage(RequestParameters.PAGE_DOWNLOAD);
        params.setAction(RequestParameters.ACTION_DOWLOAD_PROC_VALUE_FILES);
        controller = new CommandDownload(mock(HttpServletResponse.class), params, 
                mock(BgeeProperties.class), viewFac, serviceFac);
        controller.processRequest();
        verify(display).displayProcessedExpressionValuesDownloadPage(groups, speToTerms);

        params = new RequestParameters();
        params.setAction("fake action");
        controller = new CommandDownload(mock(HttpServletResponse.class), params,
                mock(BgeeProperties.class), viewFac, serviceFac);
        try {
            controller.processRequest();
            fail("A PageNotFoundException should be thrown");
        } catch (PageNotFoundException e) {
            // test passed
        }
    }
    
    /**
     * @return  A {@code List} of {@code SpeciesDataGroup}s to be used in unit tests related to 
     *          download files.
     */
    public static List<SpeciesDataGroup> getTestGroups() {
        log.traceEntry();
        //Species:
        Species spe1 = new Species(9606, "human", null, "Homo", "sapiens", "hsap1", "assemblyHsap1", new Source(1), 1234, null, null, null, null);
        Species spe2 = new Species(10090, "mouse", null, "Mus", "musculus", "mmus1","assemblyMmus1",  new Source(1), 2322, null, null, null, null);
        Species spe3 = new Species(7955, "zebrafish", null, "Danio", "rerio", "dre1", "assemblyDre1", new Source(1), 2311, null, null, null, null);
        Species spe4 = new Species(7227, "fly", null, "Drosophila", "melanogaster", "dmel1", "assemblyDmel1", new Source(1), 211, null, null, null, null);
        
        //make all file types available for at least one species
        Set<DownloadFile> dlFileGroup1 = new HashSet<DownloadFile>();
        int i = 0;
        for (CategoryEnum cat: CategoryEnum.values()) {
            dlFileGroup1.add(new DownloadFile("my/path/file" + i + ".tsv.zip", "file" + i + ".tsv.zip", 
                    cat, i * 200L, 11));
            i++;
        }
        //arbitrary files for other groups
        Set<DownloadFile> dlFileGroup2 = new HashSet<DownloadFile>(Arrays.asList(
                new DownloadFile("my/path/fileg2_1.tsv.zip", "fileg2_1.tsv.zip", 
                        CategoryEnum.EXPR_CALLS_SIMPLE, 5000L, 22), 
                new DownloadFile("my/path/fileg2_2.tsv.zip", "fileg2_2.tsv.zip", 
                        CategoryEnum.EXPR_CALLS_COMPLETE, 50000L, 22), 
                new DownloadFile("my/path/fileg2_3.tsv.zip", "fileg2_3.tsv.zip", 
                        CategoryEnum.RNASEQ_ANNOT, 5000L, 22)
                ));
        Set<DownloadFile> dlFileGroup3 = new HashSet<DownloadFile>(Arrays.asList(
                new DownloadFile("my/path/fileg3_1.tsv.zip", "fileg3_1.tsv.zip", 
                        CategoryEnum.DIFF_EXPR_ANAT_SIMPLE, 500L, 33), 
                new DownloadFile("my/path/fileg3_2.tsv.zip", "fileg3_2.tsv.zip", 
                        CategoryEnum.DIFF_EXPR_ANAT_COMPLETE, 5000L, 33), 
                new DownloadFile("my/path/fileg3_3.tsv.zip", "fileg3_3.tsv.zip", 
                        CategoryEnum.AFFY_DATA, 5000L, 33), 
                new DownloadFile("my/path/fileg3_4.tsv.zip", "fileg3_4.tsv.zip", 
                        CategoryEnum.AFFY_ANNOT, 5000L, 33)
                ));
        Set<DownloadFile> dlFileGroup4 = new HashSet<DownloadFile>(Arrays.asList(
                new DownloadFile("my/path/fileg4_1.tsv.zip", "fileg4_1.tsv.zip", 
                        CategoryEnum.DIFF_EXPR_ANAT_SIMPLE, 5500L, 44), 
                new DownloadFile("my/path/fileg4_2.tsv.zip", "fileg4_2.tsv.zip", 
                        CategoryEnum.DIFF_EXPR_ANAT_COMPLETE, 55000L, 44), 
                new DownloadFile("my/path/fileg4_3.tsv.zip", "fileg4_3.tsv.zip", 
                        CategoryEnum.AFFY_DATA, 55000L, 44), 
                new DownloadFile("my/path/fileg4_4.tsv.zip", "fileg4_4.tsv.zip", 
                        CategoryEnum.AFFY_ANNOT, 55000L, 44), 
                new DownloadFile("my/path/fileg4_5.tsv.zip", "fileg4_5.tsv.zip", 
                        CategoryEnum.RNASEQ_ANNOT, 55000L, 44), 
                new DownloadFile("my/path/fileg4_6.tsv.zip", "fileg4_6.tsv.zip", 
                        CategoryEnum.RNASEQ_DATA, 55000L, 44)
                ));
        Set<DownloadFile> dlFileGroup5 = new HashSet<DownloadFile>(Arrays.asList(
                new DownloadFile("my/path/fileg5_1.tsv.zip", "fileg5_1.tsv.zip", 
                        CategoryEnum.DIFF_EXPR_ANAT_SIMPLE, 55000L, 55), 
                new DownloadFile("my/path/fileg5_2.tsv.zip", "fileg5_2.tsv.zip", 
                        CategoryEnum.DIFF_EXPR_ANAT_COMPLETE, 55000L, 55), 
                new DownloadFile("my/path/fileg5_3.tsv.zip", "fileg5_3.tsv.zip", 
                        CategoryEnum.ORTHOLOG, 55000L, 55)
                ));
        
        //groups: 
        return log.traceExit(Arrays.asList(
                new SpeciesDataGroup(11, "single spe g1", null, 
                        Arrays.asList(spe1), dlFileGroup1), 
                new SpeciesDataGroup(22, "single spe g2", null, 
                        Arrays.asList(spe2), dlFileGroup2), 
                new SpeciesDataGroup(33, "single spe g3", null, 
                        Arrays.asList(spe3), dlFileGroup3), 
                new SpeciesDataGroup(44, "single spe g4", null, 
                        Arrays.asList(spe4), dlFileGroup4), 
                new SpeciesDataGroup(55, "multi spe g5", null, 
                        Arrays.asList(spe2, spe3, spe4), dlFileGroup5)
                ));
    }
    
    /**
     * @return  A {@code Map} allowing to mock the value returned by 
     *          {@link KeywordService#getKeywordForAllSpecies()}.
     */
    public static Map<Integer, Set<String>> getTestSpeToKeywords() {
        log.traceEntry();
        return log.traceExit(Stream.of(
            //regression test, no keywords associated to species 9606 on purpose, 
            //a previous version of the code threw a NPE
            new AbstractMap.SimpleEntry<>(10090, new HashSet<>(Arrays.asList("house mouse", "mice"))), 
            new AbstractMap.SimpleEntry<>(7955, new HashSet<>(Arrays.asList("leopard danio", "zebra danio"))), 
            new AbstractMap.SimpleEntry<>(7227, new HashSet<>(Arrays.asList("vinegar fly", "fruit fly"))))
            .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue())));
    }
    
    /**
     * @return  A {@code Map} to provide mappings from species to related terms, 
     *          to be provided to the methods {@code DownloadDisplay.displayGeneExpressionCallDownloadPage} 
     *          and {@code DownloadDisplay.displayProcessedExpressionValuesDownloadPage}. 
     *          This {@code Map} is notably produced by using the groups returned by 
     *          {@link #getTestGroups()}, and the mapping to keywords returned by 
     *          {@link #getTestSpeToKeywords()}.
     */
    public static Map<Integer, Set<String>> getTestSpeciesToTerms() {
        log.traceEntry();
        List<SpeciesDataGroup> groups = getTestGroups();
        return log.traceExit(Stream.of(
                new AbstractMap.SimpleEntry<>(groups.get(0).getMembers().get(0).getId(), new HashSet<>(
                        Arrays.asList(groups.get(0).getMembers().get(0).getName(), 
                                groups.get(0).getMembers().get(0).getScientificName(), 
                                groups.get(0).getMembers().get(0).getShortName(), 
                                String.valueOf(groups.get(0).getMembers().get(0).getId())))), 
                new AbstractMap.SimpleEntry<>(groups.get(1).getMembers().get(0).getId(), new HashSet<>(
                        Arrays.asList("house mouse", "mice", 
                                groups.get(1).getMembers().get(0).getName(), 
                                groups.get(1).getMembers().get(0).getScientificName(), 
                                groups.get(1).getMembers().get(0).getShortName(), 
                                String.valueOf(groups.get(1).getMembers().get(0).getId())))), 
                new AbstractMap.SimpleEntry<>(groups.get(2).getMembers().get(0).getId(), new HashSet<>(
                        Arrays.asList("leopard danio", "zebra danio", 
                                groups.get(2).getMembers().get(0).getName(), 
                                groups.get(2).getMembers().get(0).getScientificName(), 
                                groups.get(2).getMembers().get(0).getShortName(), 
                                String.valueOf(groups.get(2).getMembers().get(0).getId())))), 
                new AbstractMap.SimpleEntry<>(groups.get(3).getMembers().get(0).getId(), new HashSet<>(
                        Arrays.asList("vinegar fly", "fruit fly", 
                                groups.get(3).getMembers().get(0).getName(), 
                                groups.get(3).getMembers().get(0).getScientificName(), 
                                groups.get(3).getMembers().get(0).getShortName(), 
                                String.valueOf(groups.get(3).getMembers().get(0).getId())))))
                .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue())));
    }
}
