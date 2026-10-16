package org.bgee.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.TestAncestor;
import org.bgee.controller.exception.InvalidRequestException;
import org.bgee.controller.user.User;
import org.bgee.model.ServiceFactory;
import org.bgee.model.dao.api.DAOManager;
import org.bgee.model.dao.api.gene.GeneDAO;
import org.bgee.model.dao.api.gene.GeneDAO.GeneBioTypeTO;
import org.bgee.model.dao.api.gene.GeneDAO.GeneBioTypeTOResultSet;
import org.bgee.model.dao.api.gene.GeneDAO.GeneTO;
import org.bgee.model.dao.api.gene.GeneDAO.GeneTOResultSet;
import org.bgee.model.job.Job;
import org.bgee.model.job.JobService;
import org.bgee.model.topanat.TopAnatService;
import org.bgee.model.topanat.TopAnatService.Background;
import org.bgee.model.topanat.TopAnatService.CallFileType;
import org.bgee.view.RPackageDisplay;
import org.bgee.view.ViewFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Integration tests of the actions of {@link CommandRPackage} serving the files generated for
 * topAnat by {@link TopAnatService}, on small files written in a temporary directory,
 * and the genes of the backgrounds of topAnat, read from a mocked {@code GeneDAO}.
 *
 * @author  Julien Wollbrett
 * @version Bgee 16
 */
public class CommandRPackageTest extends TestAncestor {

    private final static Logger log = LogManager.getLogger(CommandRPackageTest.class.getName());

    @Override
    protected Logger getLogger() {
        return log;
    }

    private final static int SPECIES_ID = 1;

    @Rule
    public TemporaryFolder tmpFolder = new TemporaryFolder();

    private BgeeProperties props;
    private ServiceFactory serviceFactory;
    private ViewFactory viewFactory;
    private JobService jobService;
    private User user;
    /**
     * The columns and rows the last request displayed.
     */
    private List<String> displayedColumns;
    private List<List<String>> displayedRows;

    @Before
    public void setUp() throws Exception {
        String root = this.tmpFolder.getRoot().getPath();
        //Only the column of the combination of all data types, the one requested when none is
        write(TopAnatService.getCallFilePath(root, CallFileType.PROPAGATED, SPECIES_ID),
                "geneId\tstageId\tconditionId\tIN_SITU-SC_RNA_SEQ-RNA_SEQ",
                "G1\tUBERON:0000104\tUBERON:0000955\tGOLD",
                "G1\tUBERON:0000104\tBGEE:0000000\tSILVER",
                "G2\tUBERON:0000104\tCL:0000540-UBERON:0000955\tSILVER",
                "G2\tUBERON:0000092\tUBERON:0000955\tSILVER");
        write(TopAnatService.getCallFilePath(root, CallFileType.OBSERVED, SPECIES_ID),
                "geneId\tstageId\tconditionId\tIN_SITU-SC_RNA_SEQ-RNA_SEQ",
                "G1\tUBERON:0000104\tUBERON:0000955\tGOLD");
        write(TopAnatService.getConditionFilePath(root, SPECIES_ID),
                "conditionId\tconditionName\tanatEntityId\tcellTypeId",
                "BGEE:0000000\tanatomical entity\tBGEE:0000000\tGO:0005575",
                "CL:0000540-UBERON:0000955\tneuron in brain\tUBERON:0000955\tCL:0000540",
                "UBERON:0000955\tbrain\tUBERON:0000955\tGO:0005575");
        write(TopAnatService.getConditionRelationFilePath(root, SPECIES_ID),
                "sourceConditionId\ttargetConditionId",
                "CL:0000540-UBERON:0000955\tUBERON:0000955",
                "UBERON:0000955\tBGEE:0000000");

        this.props = mock(BgeeProperties.class);
        when(this.props.getTopAnatResultsWritingDirectory()).thenReturn(root);
        this.viewFactory = mock(ViewFactory.class);
        RPackageDisplay display = mock(RPackageDisplay.class);
        when(this.viewFactory.getRPackageDisplay()).thenReturn(display);
        //The rows are read lazily from the file: they are consumed while the command displays
        //them, before it closes the file
        doAnswer(invocation -> {
            this.displayedColumns = invocation.getArgument(0);
            Stream<String[]> rows = invocation.getArgument(1);
            this.displayedRows = rows.map(Arrays::asList).collect(Collectors.toList());
            return null;
        }).when(display).displayRows(any(), any());
        //The genes of the backgrounds: a protein coding gene, a lncRNA, which the polyA libraries
        //do not target, a pseudogene, which they target and which has no observation, and
        //a second protein coding gene
        GeneBioTypeTO proteinCoding = new GeneBioTypeTO(1, "protein_coding");
        GeneBioTypeTO lncRNA = new GeneBioTypeTO(2, "lncRNA");
        GeneBioTypeTO pseudogene = new GeneBioTypeTO(3, "processed_pseudogene");
        GeneTO coding1 = gene(1, "G1", proteinCoding);
        GeneTO lnc = gene(2, "G2", lncRNA);
        GeneTO pseudo = gene(3, "G3", pseudogene);
        GeneTO coding2 = gene(4, "G4", proteinCoding);
        GeneDAO geneDAO = mock(GeneDAO.class);
        //A result set can only be read once: a new one for each call
        when(geneDAO.getGenesBySpeciesIds(Set.of(SPECIES_ID))).thenAnswer(
                invocation -> geneResultSet(List.of(coding2, lnc, pseudo, coding1)));
        when(geneDAO.getGenesWithDataBySpeciesIdsOrdered(Set.of(SPECIES_ID), 0, 0)).thenAnswer(
                invocation -> geneResultSet(List.of(coding1, lnc, coding2)));
        when(geneDAO.getGeneBioTypes()).thenAnswer(
                invocation -> bioTypeResultSet(List.of(proteinCoding, lncRNA, pseudogene)));
        when(geneDAO.getGeneBioTypesByPopulationCaptureIds(Set.of("polyA"))).thenAnswer(
                invocation -> bioTypeResultSet(List.of(proteinCoding, pseudogene)));
        DAOManager daoManager = mock(DAOManager.class);
        when(daoManager.getGeneDAO()).thenReturn(geneDAO);
        this.serviceFactory = mock(ServiceFactory.class);
        when(this.serviceFactory.getDAOManager()).thenReturn(daoManager);

        this.jobService = mock(JobService.class);
        when(this.jobService.registerNewJob(any())).thenReturn(mock(Job.class));
        this.user = mock(User.class);
        when(this.user.getUUID()).thenReturn(UUID.randomUUID());
    }
    private static void write(Path file, String... lines) throws IOException {
        Files.createDirectories(file.getParent());
        Files.write(file, Arrays.asList(lines));
    }
    private static GeneTO gene(int bgeeGeneId, String geneId, GeneBioTypeTO bioType) {
        return new GeneTO(bgeeGeneId, geneId, null, null, SPECIES_ID, bioType.getId(), true,
                null, null, 1, null);
    }
    private static GeneTOResultSet geneResultSet(List<GeneTO> genes) {
        GeneTOResultSet resultSet = mock(GeneTOResultSet.class);
        when(resultSet.stream()).thenReturn(genes.stream());
        return resultSet;
    }
    private static GeneBioTypeTOResultSet bioTypeResultSet(List<GeneBioTypeTO> bioTypes) {
        GeneBioTypeTOResultSet resultSet = mock(GeneBioTypeTOResultSet.class);
        when(resultSet.stream()).thenReturn(bioTypes.stream());
        return resultSet;
    }

    /**
     * @return  The {@code RequestParameters} of a request to the R package API for an action
     *          on the species of the test files.
     */
    private static RequestParameters params(String action) {
        RequestParameters params = new RequestParameters();
        params.setPage(RequestParameters.PAGE_RPACKAGE);
        params.setAction(action);
        params.addValue(params.getUrlParametersInstance().getParamSpeciesList(), SPECIES_ID);
        return params;
    }
    private void process(RequestParameters params) throws Exception {
        new CommandRPackage(mock(HttpServletResponse.class), params, this.props,
                this.viewFactory, this.serviceFactory, this.jobService, this.user)
        .processRequest();
    }

    /**
     * Test the action {@code get_expression_calls}.
     */
    @Test
    public void shouldGetExpressionCalls() throws Exception {
        //The kind of call file must be requested explicitly
        assertThrows("A request with no call file type should be rejected",
                InvalidRequestException.class, () -> this.process(params("get_expression_calls")));

        //No stage requested: the calls of the root of the stages
        RequestParameters propagatedParams = params("get_expression_calls");
        propagatedParams.addValue(
                propagatedParams.getUrlParametersInstance().getParamCallFileType(),
                CallFileType.PROPAGATED.getStringRepresentation());
        this.process(propagatedParams);
        assertEquals("Incorrect columns of the expression calls",
                List.of(CommandRPackage.CALLS_GENE_ID_PARAM,
                        CommandRPackage.CALLS_CONDITION_ID_PARAM),
                this.displayedColumns);
        assertEquals("Incorrect expression calls of the propagated file", List.of(
                List.of("G1", "UBERON:0000955"),
                List.of("G1", "BGEE:0000000"),
                List.of("G2", "CL:0000540-UBERON:0000955")),
                this.displayedRows);

        RequestParameters params = params("get_expression_calls");
        params.addValue(params.getUrlParametersInstance().getParamCallFileType(),
                CallFileType.OBSERVED.getStringRepresentation());
        params.addValue(params.getUrlParametersInstance().getParamDataQuality(), "GOLD");
        this.process(params);
        assertEquals("Incorrect expression calls of the observed file",
                List.of(List.of("G1", "UBERON:0000955")), this.displayedRows);
    }

    /**
     * Test the action {@code get_background_genes}.
     */
    @Test
    public void shouldGetBackgroundGenes() throws Exception {
        //The background must be requested explicitly
        assertThrows("A request with no background type should be rejected",
                InvalidRequestException.class, () -> this.process(params("get_background_genes")));

        for (Entry<Background, List<List<String>>> expected: Map.of(
                Background.PROTEIN_CODING, List.of(List.of("G1"), List.of("G4")),
                Background.POLY_A, List.of(List.of("G1"), List.of("G3"), List.of("G4")),
                Background.OBSERVED, List.of(List.of("G1"), List.of("G2"), List.of("G4")))
                .entrySet()) {
            RequestParameters params = params("get_background_genes");
            params.addValue(params.getUrlParametersInstance().getParamBackgroundType(),
                    expected.getKey().getStringRepresentation());
            this.process(params);
            assertEquals("Incorrect columns of the background genes",
                    List.of(CommandRPackage.BACKGROUND_GENE_ID_PARAM), this.displayedColumns);
            assertEquals("Incorrect genes of the background " + expected.getKey(),
                    expected.getValue(), this.displayedRows);
        }
    }

    /**
     * Test the action {@code get_anat_entities}, displaying the conditions.
     */
    @Test
    public void shouldGetConditions() throws Exception {
        this.process(params("get_anat_entities"));
        assertEquals("Incorrect default columns of the conditions", List.of(
                CommandRPackage.CONDITIONS_ID_PARAM, CommandRPackage.CONDITIONS_NAME_PARAM,
                CommandRPackage.CONDITIONS_ANAT_ENTITY_ID_PARAM,
                CommandRPackage.CONDITIONS_CELL_TYPE_ID_PARAM),
                this.displayedColumns);
        assertEquals("Incorrect conditions", List.of(
                List.of("BGEE:0000000", "anatomical entity", "BGEE:0000000", "GO:0005575"),
                List.of("CL:0000540-UBERON:0000955", "neuron in brain", "UBERON:0000955",
                        "CL:0000540"),
                List.of("UBERON:0000955", "brain", "UBERON:0000955", "GO:0005575")),
                this.displayedRows);

        //The attributes the R package requests, in the order requested
        RequestParameters params = params("get_anat_entities");
        params.addValues(params.getUrlParametersInstance().getParamAttributeList(), List.of(
                CommandRPackage.CONDITIONS_NAME_PARAM, CommandRPackage.CONDITIONS_ID_PARAM));
        this.process(params);
        assertEquals("Incorrect requested columns of the conditions", List.of(
                CommandRPackage.CONDITIONS_NAME_PARAM, CommandRPackage.CONDITIONS_ID_PARAM),
                this.displayedColumns);
        assertEquals("Incorrect requested values of the conditions", List.of(
                List.of("anatomical entity", "BGEE:0000000"),
                List.of("neuron in brain", "CL:0000540-UBERON:0000955"),
                List.of("brain", "UBERON:0000955")),
                this.displayedRows);

        //The descriptions of the anat. entities are not part of the conditions
        RequestParameters invalidParams = params("get_anat_entities");
        invalidParams.addValue(invalidParams.getUrlParametersInstance().getParamAttributeList(),
                "DESCRIPTION");
        assertThrows("An attribute not available should be rejected",
                InvalidRequestException.class, () -> this.process(invalidParams));
    }

    /**
     * Test the action {@code get_anat_entity_relations}, displaying the relations between
     * the conditions.
     */
    @Test
    public void shouldGetConditionRelations() throws Exception {
        this.process(params("get_anat_entity_relations"));
        assertEquals("Incorrect default columns of the relations", List.of(
                CommandRPackage.RELATIONS_SOURCE_PARAM, CommandRPackage.RELATIONS_TARGET_PARAM),
                this.displayedColumns);
        assertEquals("Incorrect relations", List.of(
                List.of("CL:0000540-UBERON:0000955", "UBERON:0000955"),
                List.of("UBERON:0000955", "BGEE:0000000")),
                this.displayedRows);

        RequestParameters invalidParams = params("get_anat_entity_relations");
        invalidParams.addValue(invalidParams.getUrlParametersInstance().getParamAttributeList(),
                "RELATION_TYPE");
        assertThrows("An attribute not available should be rejected",
                InvalidRequestException.class, () -> this.process(invalidParams));
    }

    /**
     * Test the actions on a species whose files have not been generated.
     */
    @Test
    public void shouldRejectSpeciesWithNoFile() throws Exception {
        for (String action: List.of("get_expression_calls", "get_anat_entities",
                "get_anat_entity_relations")) {
            RequestParameters params = new RequestParameters();
            params.setPage(RequestParameters.PAGE_RPACKAGE);
            params.setAction(action);
            params.addValue(params.getUrlParametersInstance().getParamSpeciesList(), 2);
            params.addValue(params.getUrlParametersInstance().getParamCallFileType(),
                    CallFileType.PROPAGATED.getStringRepresentation());
            assertThrows("A species with no generated file should be rejected: " + action,
                    InvalidRequestException.class, () -> this.process(params));
        }
    }
}
