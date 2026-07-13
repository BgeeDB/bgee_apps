package org.bgee.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.TestAncestor;
import org.bgee.controller.CommandData.MultispecExprCallResponse;
import org.bgee.controller.exception.InvalidRequestException;
import org.bgee.controller.user.User;
import org.bgee.controller.utils.BgeeCacheService;
import org.bgee.model.SearchResult;
import org.bgee.model.ServiceFactory;
import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.model.expressiondata.baseelements.ExpressionLevelInfo;
import org.bgee.model.expressiondata.baseelements.SummaryCallType.ExpressionSummary;
import org.bgee.model.expressiondata.baseelements.SummaryQuality;
import org.bgee.model.expressiondata.call.Call.ExpressionCall2;
import org.bgee.model.expressiondata.call.CallData.ExpressionCallData2;
import org.bgee.model.expressiondata.call.multispecies.MultiSpeciesCallService;
import org.bgee.model.expressiondata.call.multispecies.MultiSpeciesCondition;
import org.bgee.model.expressiondata.call.multispecies.SimilarityExpressionCall2;
import org.bgee.model.expressiondata.call.multispecies.SimilarityExpressionCallFilter;
import org.bgee.model.expressiondata.call.multispecies.SimilarityExpressionCallLoader;
import org.bgee.model.gene.Gene;
import org.bgee.model.gene.GeneBioType;
import org.bgee.model.gene.GeneService;
import org.bgee.model.job.Job;
import org.bgee.model.job.JobService;
import org.bgee.model.source.Source;
import org.bgee.model.species.Species;
import org.bgee.model.species.Taxon;
import org.bgee.model.species.TaxonService;
import org.bgee.model.species.TaxonTreeService;
import org.bgee.model.species.TaxonWithSpecies;
import org.bgee.view.DataDisplay;
import org.bgee.view.ViewFactory;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

/**
 * Unit tests for the multi-species expression calls flow in {@link CommandData}.
 */
public class CommandDataMultispecTest extends TestAncestor {

    private static final int HUMAN_ID = 9606;
    private static final int MOUSE_ID = 10090;
    private static final int LCA_ID = 7742;

    private final static Logger log =
            LogManager.getLogger(CommandDataMultispecTest.class.getName());

    private ServiceFactory serviceFactory;
    private GeneService geneService;
    private TaxonService taxonService;
    private TaxonTreeService taxonTreeService;
    private MultiSpeciesCallService multiSpeciesCallService;
    private JobService jobService;
    private Job job;
    private User user;
    private ViewFactory viewFactory;
    private DataDisplay dataDisplay;
    private SimilarityExpressionCallLoader loader;
    private BgeeCacheService cacheService;

    private Species humanSpecies;
    private Species mouseSpecies;
    private Gene humanGene;
    private Gene mouseGene;

    @Override
    protected Logger getLogger() {
        return log;
    }

    @Before
    public void setUp() throws Exception {
        serviceFactory = mock(ServiceFactory.class);
        geneService = mock(GeneService.class);
        taxonService = mock(TaxonService.class);
        taxonTreeService = mock(TaxonTreeService.class);
        multiSpeciesCallService = mock(MultiSpeciesCallService.class);
        jobService = mock(JobService.class);
        job = mock(Job.class);
        user = mock(User.class);
        viewFactory = mock(ViewFactory.class);
        dataDisplay = mock(DataDisplay.class);
        loader = mock(SimilarityExpressionCallLoader.class);
        cacheService = mock(BgeeCacheService.class);
        //Simulate a cache miss: invoke the compute supplier (3rd argument) and return its result,
        //so the underlying loader is still exercised as in production on a cold cache.
        when(cacheService.useCacheNonAtomic(any(), any(), any(), any()))
                .thenAnswer(invocation -> ((Supplier<?>) invocation.getArgument(2)).get());

        when(serviceFactory.getGeneService()).thenReturn(geneService);
        when(serviceFactory.getTaxonService()).thenReturn(taxonService);
        when(serviceFactory.getTaxonTreeService()).thenReturn(taxonTreeService);
        when(serviceFactory.getMultiSpeciesCallService()).thenReturn(multiSpeciesCallService);
        when(viewFactory.getDataDisplay()).thenReturn(dataDisplay);
        when(user.getUUID()).thenReturn(UUID.randomUUID());
        when(jobService.registerNewJob(any())).thenReturn(job);
        when(multiSpeciesCallService.loadSimilarityCallLoader(any(SimilarityExpressionCallFilter.class)))
                .thenReturn(loader);

        humanSpecies = new Species(HUMAN_ID, "human", null, "Homo", "sapiens",
                "hsap1", "assemblyHsap1", new Source(1), null, null, null, null, null);
        mouseSpecies = new Species(MOUSE_ID, "mouse", null, "Mus", "musculus",
                "mmus1", "assemblyMmus1", new Source(1), null, null, null, null, null);
        humanGene = new Gene("ENSG00000130208", humanSpecies, new GeneBioType("protein_coding"));
        mouseGene = new Gene("ENSMUSG00000040564", mouseSpecies, new GeneBioType("protein_coding"));

        when(taxonService.loadLeastCommonAncestor(new HashSet<>(Arrays.asList(HUMAN_ID, MOUSE_ID))))
                .thenReturn(new Taxon(LCA_ID, "Euarchontoglires", null, "Euarchontoglires", 1, true));

        TaxonWithSpecies taxonTree = new TaxonWithSpecies(
                new Taxon(LCA_ID, "Euarchontoglires", null, "Euarchontoglires", 1, true),
                List.of(), List.of());
        when(taxonTreeService.buildTaxonTreeWithSpecies(
                eq(new HashSet<>(Arrays.asList(HUMAN_ID, MOUSE_ID))), any()))
                .thenReturn(taxonTree);
    }

    @Test
    public void shouldRejectMultispecRequestWithFewerThanTwoGenes() throws Exception {
        RequestParameters params = newMultispecParams();
        params.addValues(params.getUrlParametersInstance().getParamGeneList(),
                Collections.singletonList("ENSG00000130208"));
        params.addValue(params.getUrlParametersInstance().getParamGetResults(), true);

        try {
            buildController(params).processRequest();
            fail("An InvalidRequestException should be thrown");
        } catch (InvalidRequestException e) {
            assertEquals("At least two gene IDs must be provided in gene_list.", e.getMessage());
        }
    }

    @Test
    public void shouldRejectMultispecPostFiltersRequest() throws Exception {
        stubGeneSearch();
        RequestParameters params = newMultispecParams();
        params.addValues(params.getUrlParametersInstance().getParamGeneList(),
                Arrays.asList(humanGene.getGeneId(), mouseGene.getGeneId()));
        params.addValue(params.getUrlParametersInstance().getParamGetFilters(), true);

        try {
            buildController(params).processRequest();
            fail("An InvalidRequestException should be thrown");
        } catch (InvalidRequestException e) {
            assertEquals("Post-filters are not supported for multi-species expression calls.",
                    e.getMessage());
        }
    }

    @Test
    public void shouldRejectMultispecRequestWithLimitAboveMaximum() throws Exception {
        stubGeneSearch();
        RequestParameters params = newMultispecParams();
        params.addValues(params.getUrlParametersInstance().getParamGeneList(),
                Arrays.asList(humanGene.getGeneId(), mouseGene.getGeneId()));
        params.addValue(params.getUrlParametersInstance().getParamGetResults(), true);
        params.addValue(params.getUrlParametersInstance().getParamLimit(), 10001);

        try {
            buildController(params).processRequest();
            fail("An InvalidRequestException should be thrown");
        } catch (InvalidRequestException e) {
            assertEquals("It is not possible to request more than 10000 results.", e.getMessage());
        }
    }

    @Test
    public void shouldRejectMultispecRequestWithNegativeOffset() throws Exception {
        stubGeneSearch();
        RequestParameters params = newMultispecParams();
        params.addValues(params.getUrlParametersInstance().getParamGeneList(),
                Arrays.asList(humanGene.getGeneId(), mouseGene.getGeneId()));
        params.addValue(params.getUrlParametersInstance().getParamGetResults(), true);
        params.addValue(params.getUrlParametersInstance().getParamOffset(), -1L);

        try {
            buildController(params).processRequest();
            fail("An InvalidRequestException should be thrown");
        } catch (InvalidRequestException e) {
            assertEquals("Offset cannot be less than 0.", e.getMessage());
        }
    }

    @Test
    public void shouldProcessMultispecRequestWithResultsAndCount() throws Exception {
        stubGeneSearch();

        SimilarityExpressionCall2 similarityCall = buildSimilarityCall();
        when(loader.loadDataCount()).thenReturn(42L);
        when(loader.loadData(5L, 10)).thenReturn(Collections.singletonList(similarityCall));

        RequestParameters params = newMultispecParams();
        params.addValues(params.getUrlParametersInstance().getParamGeneList(),
                Arrays.asList(humanGene.getGeneId(), mouseGene.getGeneId()));
        params.addValue(params.getUrlParametersInstance().getParamGetResults(), true);
        params.addValue(params.getUrlParametersInstance().getParamGetResultCount(), true);
        params.addValue(params.getUrlParametersInstance().getParamOffset(), 5L);
        params.addValue(params.getUrlParametersInstance().getParamLimit(), 10);

        buildController(params).processRequest();

        ArgumentCaptor<SimilarityExpressionCallFilter> filterCaptor =
                ArgumentCaptor.forClass(SimilarityExpressionCallFilter.class);
        verify(multiSpeciesCallService).loadSimilarityCallLoader(filterCaptor.capture());
        SimilarityExpressionCallFilter capturedFilter = filterCaptor.getValue();
        assertEquals(LCA_ID, capturedFilter.getTaxonId());
        assertEquals(false, capturedFilter.isOnlyTrusted());
        assertEquals(2, capturedFilter.getGeneFilters().size());

        verify(loader).loadDataCount();
        verify(loader).loadData(5L, 10);
        verify(job).startJob();
        verify(job).completeWithSuccess();
        verify(job).release();

        ArgumentCaptor<MultispecExprCallResponse> responseCaptor =
                ArgumentCaptor.forClass(MultispecExprCallResponse.class);
        verify(dataDisplay).displayMultispecExprCallPage(
                any(TaxonWithSpecies.class),
                eq(null),
                eq(null),
                responseCaptor.capture(),
                eq(42L),
                eq(null));

        MultispecExprCallResponse response = responseCaptor.getValue();
        assertEquals(1, response.getCalls().size());
        SimilarityExpressionCall2 call = response.getCalls().get(0);
        assertEquals(humanGene, call.getGene());
        assertEquals(ExpressionSummary.EXPRESSED, call.getSummaryCallType());
        assertEquals(1, call.getCalls().size());
        assertEquals(EnumSet.allOf(DataType.class), response.getRequestedDataTypes());
    }

    private void stubGeneSearch() {
        List<String> geneIds = Arrays.asList(humanGene.getGeneId(), mouseGene.getGeneId());
        when(geneService.searchGenesByIds(geneIds)).thenReturn(new SearchResult<>(
                geneIds, Collections.emptyList(), Arrays.asList(humanGene, mouseGene)));
    }

    private SimilarityExpressionCall2 buildSimilarityCall() {
        MultiSpeciesCondition condition = new MultiSpeciesCondition(null, null, null, null);
        ExpressionCall2 supportingCall = mock(ExpressionCall2.class);
        ExpressionLevelInfo levelInfo = new ExpressionLevelInfo(
                BigDecimal.valueOf(5000), BigDecimal.valueOf(50), null, null, null);
        ExpressionCallData2 callData = new ExpressionCallData2(
                DataType.RNA_SEQ, Collections.emptyList(), Collections.emptyList(),
                null, null, null, null);
        when(supportingCall.getExpressionLevelInfo()).thenReturn(levelInfo);
        when(supportingCall.getCallData()).thenReturn(Set.of(callData));
        when(supportingCall.getSummaryCallType()).thenReturn(ExpressionSummary.EXPRESSED);
        when(supportingCall.getSummaryQuality()).thenReturn(SummaryQuality.BRONZE);
        return new SimilarityExpressionCall2(
                humanGene, condition, Collections.singletonList(supportingCall));
    }

    private RequestParameters newMultispecParams() {
        RequestParameters params = new RequestParameters();
        params.setAction(RequestParameters.ACTION_MULTISPEC_EXPR_CALLS);
        return params;
    }

    private CommandData buildController(RequestParameters params) {
        return new CommandData(
                mock(HttpServletResponse.class),
                params,
                mock(BgeeProperties.class),
                viewFactory,
                serviceFactory,
                jobService,
                cacheService,
                user);
    }
}
