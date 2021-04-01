package org.bgee.pipeline.expression.downloadfile;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.Service;
import org.bgee.model.ServiceFactory;
import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.anatdev.AnatEntityService;
import org.bgee.model.anatdev.DevStage;
import org.bgee.model.expressiondata.Call.ExpressionCall;
import org.bgee.model.expressiondata.CallFilter.ExpressionCallFilter;
import org.bgee.model.expressiondata.CallService;
import org.bgee.model.expressiondata.Condition;
import org.bgee.model.expressiondata.ConditionGraphService;
import org.bgee.model.expressiondata.baseelements.CallType;
import org.bgee.model.expressiondata.baseelements.ExpressionLevelInfo;
import org.bgee.model.expressiondata.baseelements.SummaryCallType.ExpressionSummary;
import org.bgee.model.expressiondata.baseelements.SummaryQuality;
import org.bgee.model.gene.Gene;
import org.bgee.model.gene.GeneBioType;
import org.bgee.model.gene.GeneFilter;
import org.bgee.model.gene.GeneService;
import org.bgee.model.source.Source;
import org.bgee.model.species.Species;
import org.bgee.model.species.SpeciesService;
import org.bgee.pipeline.TestAncestor;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.supercsv.cellprocessor.ConvertNullTo;
import org.supercsv.cellprocessor.constraint.LMinMax;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.constraint.StrNotNullOrEmpty;
import org.supercsv.cellprocessor.ift.CellProcessor;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.bgee.pipeline.expression.downloadfile.GenerateInsertGeneStats.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link GenerateInsertGeneStats}.
 *
 * @author  Valentine Rech de Laval
 * @version Bgee 14, Oct. 2018
 * @since   Bgee 14, Oct. 2018
 */
public class GenerateInsertGeneStatsTest extends TestAncestor {

    /**
     * {@code Logger} of the class.
     */
    private final static Logger log = LogManager.getLogger(GenerateInsertGeneStatsTest.class.getName());

    @Override
    protected Logger getLogger() {
        return log;
    }

    @Rule
    public final TemporaryFolder testFolder = new TemporaryFolder();

    /**
     * Test method {@link GenerateInsertGeneStats#generate(String, String, String, Collection)}.
     */
    @Test
    public void shouldGenerateStatFiles() throws IOException {

        ServiceFactory serviceFactory = mock(ServiceFactory.class);
        MockDAOManager mockManager = new MockDAOManager();

        Species sp1 = new Species(9606, null, null, "Genus11", "spName1", "whateverGenome",
                new Source(1), 0, 9605, null, null, 1);
        Species sp2 = new Species(10090, null, null, "Genus22", "spName2", "whateverGenome",
                new Source(1), 0, 1, null, null, 2);
        Collection<Integer> speciesIds = Arrays.asList(sp1.getId(), sp2.getId());
        SpeciesService speciesService = mock(SpeciesService.class);
        when(serviceFactory.getSpeciesService()).thenReturn(speciesService);
        when(speciesService.loadSpeciesByIds(speciesIds, false)).thenReturn(
                new HashSet<>(Arrays.asList(sp1, sp2)));

        AnatEntityService anatEntityService = mock(AnatEntityService.class);
        when(serviceFactory.getAnatEntityService()).thenReturn(anatEntityService);
        // Mock the load of non informative anatomical entities
        when(anatEntityService.loadNonInformativeAnatEntitiesBySpeciesIds(Collections.singleton(9606)))
        .thenReturn(Arrays.asList(new AnatEntity("NonInfoAnatEnt1")).stream());
        when(anatEntityService.loadNonInformativeAnatEntitiesBySpeciesIds(Collections.singleton(10090)))
        .thenReturn(Arrays.asList(new AnatEntity("NonInfoAnatEnt2")).stream());

        // FIXME add mock for condition graph
        ConditionGraphService condGraphService = mock(ConditionGraphService.class);
        when(serviceFactory.getConditionGraphService()).thenReturn(condGraphService);

        GeneService geneService = mock(GeneService.class);
        when(serviceFactory.getGeneService()).thenReturn(geneService);
        
        GeneBioType geneBioType1 = new GeneBioType("bt1");
        GeneBioType geneBioType2 = new GeneBioType("bt2");
        
        Gene gene1sp1 = new Gene("geneId1", "gene name 1 sp 1", "gene desc 1 sp1", null, null, sp1, geneBioType1, 1);
        Gene gene1sp2 = new Gene("geneId1", "gene name 1 sp 2", "gene desc 1 sp2", null, null, sp2, geneBioType1, 1);
        Gene gene2sp2 = new Gene("geneId2", "gene name 2", "gene desc 2", null, null, sp2, geneBioType2, 1);
        
        when(geneService.loadGenes(new GeneFilter(sp1.getId())))
                .thenReturn(Stream.of(gene1sp1));
        when(geneService.loadGenes(new GeneFilter(sp2.getId())))
                .thenReturn(Stream.of(gene1sp2, gene2sp2));

        CallService callService = mock(CallService.class);
        when(serviceFactory.getCallService()).thenReturn(callService);
        LinkedHashMap<CallService.OrderingAttribute, Service.Direction> serviceOrdering =
                new LinkedHashMap<>();
        serviceOrdering.put(CallService.OrderingAttribute.GLOBAL_RANK, Service.Direction.ASC);
        EnumSet<CallService.Attribute> condAttrs = Arrays.stream(CallService.Attribute.values())
                .filter(CallService.Attribute::isConditionParameter)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(CallService.Attribute.class)));
        condAttrs.add(CallService.Attribute.DATA_QUALITY);
        condAttrs.add(CallService.Attribute.CALL_TYPE);
        condAttrs.add(CallService.Attribute.MEAN_RANK);
        EnumSet<CallService.Attribute> organAttrs = EnumSet.of(CallService.Attribute.ANAT_ENTITY_ID, CallService.Attribute.CALL_TYPE,
                CallService.Attribute.DATA_QUALITY);
        Map<CallType.Expression, Boolean> obsDataFilter = new HashMap<>();
        obsDataFilter.put(null, true);

        AnatEntity ae1 = new AnatEntity("ae1", "anat. entity 1", null);
        AnatEntity ae2 = new AnatEntity("ae2", "anat. entity 2", null);
        DevStage ds1 = new DevStage("ds1", "dev. stage 1", null);
        DevStage ds2 = new DevStage("ds2", "dev. stage 2", null);

        GeneFilter g1Sp1Filter = new GeneFilter(gene1sp1.getSpecies().getId(), gene1sp1.getEnsemblGeneId());
        List<ExpressionCall> g1s1OrganCalls = Arrays.asList(
                new ExpressionCall(null, new Condition(ae1, null, null, null, null, sp1), null,
                        ExpressionSummary.EXPRESSED, SummaryQuality.GOLD, null, null, null));
        List<ExpressionCall> g1s1ConditionCalls = Arrays.asList(
                new ExpressionCall(null, new Condition(ae1, ds1, null, null, null, sp1), null,
                        ExpressionSummary.EXPRESSED, SummaryQuality.GOLD, null,
                        new ExpressionLevelInfo(new BigDecimal(10))),
                new ExpressionCall(null, new Condition(ae1, ds2, null, null, null, sp1), null,
                        ExpressionSummary.EXPRESSED, SummaryQuality.SILVER, null,
                        new ExpressionLevelInfo(new BigDecimal(11))));
        when(callService.loadExpressionCalls(
                new ExpressionCallFilter(null, Collections.singleton(g1Sp1Filter),
                        null, null, obsDataFilter, null, null, null, null, null),
                organAttrs,
                null)).thenReturn(g1s1OrganCalls.stream());
        when(callService.loadExpressionCalls(
                new ExpressionCallFilter(null, Collections.singleton(g1Sp1Filter),
                        null, null, obsDataFilter, null, null, null, null, null),
                condAttrs, serviceOrdering)).thenReturn(g1s1ConditionCalls.stream());

        GeneFilter g1Sp2Filter = new GeneFilter(gene1sp2.getSpecies().getId(), gene1sp2.getEnsemblGeneId());
        List<ExpressionCall> g1s2OrganCalls = Arrays.asList(
                new ExpressionCall(null, new Condition(ae2, null, null, null, null, sp1), null,
                        ExpressionSummary.EXPRESSED, SummaryQuality.SILVER, null, null, null));
        List<ExpressionCall> g1s2ConditionCalls = Arrays.asList(
                new ExpressionCall(null, new Condition(ae2, ds1, null, null, null, sp1), null,
                        ExpressionSummary.EXPRESSED, SummaryQuality.SILVER, null,
                        new ExpressionLevelInfo(new BigDecimal(10))));
        when(callService.loadExpressionCalls(
                new ExpressionCallFilter(null, Collections.singleton(g1Sp2Filter),
                        null, null, obsDataFilter, null, null, null, null, null),
                organAttrs, null)).thenReturn(g1s2OrganCalls.stream());
        when(callService.loadExpressionCalls(
                new ExpressionCallFilter(null, Collections.singleton(g1Sp2Filter),
                        null, null, obsDataFilter, null, null, null, null, null),
                condAttrs, serviceOrdering)).thenReturn(g1s2ConditionCalls.stream());
        
        GeneFilter g2Sp2Filter = new GeneFilter(gene2sp2.getSpecies().getId(), gene2sp2.getEnsemblGeneId());
        List<ExpressionCall> g2s2OrganCalls = Arrays.asList(
                new ExpressionCall(null, new Condition(ae1, null, null, null, null, sp1), null,
                        ExpressionSummary.NOT_EXPRESSED, SummaryQuality.SILVER, null, null, null),
                new ExpressionCall(null, new Condition(ae2, null, null, null, null, sp1), null,
                        ExpressionSummary.NOT_EXPRESSED, SummaryQuality.GOLD, null, null, null));
        List<ExpressionCall> g2s2ConditionCalls = Arrays.asList(
                new ExpressionCall(null, new Condition(ae1, ds2, null, null, null, sp1), null,
                        ExpressionSummary.NOT_EXPRESSED, SummaryQuality.SILVER, null,
                        new ExpressionLevelInfo(new BigDecimal(4))),
                new ExpressionCall(null, new Condition(ae2, ds1, null, null, null, sp1), null,
                        ExpressionSummary.NOT_EXPRESSED, SummaryQuality.GOLD, null,
                        new ExpressionLevelInfo(new BigDecimal(10))));
        when(callService.loadExpressionCalls(
                new ExpressionCallFilter(null, Collections.singleton(g2Sp2Filter),
                        null, null, obsDataFilter, null, null, null, null, null),
                organAttrs, null)).thenReturn(g2s2OrganCalls.stream());
        when(callService.loadExpressionCalls(
                new ExpressionCallFilter(null, Collections.singleton(g2Sp2Filter),
                        null, null, obsDataFilter, null, null, null, null, null),
                condAttrs, serviceOrdering)).thenReturn(g2s2ConditionCalls.stream());

        
        String directory = testFolder.newFolder("folder_stats").getPath();
        
        GenerateInsertGeneStats g = new GenerateInsertGeneStats(() -> serviceFactory, mockManager);
        g.generate(directory, "btSuffix", "gSuffix", speciesIds);

        // TODO test one gene stat file by gene/species
        // TODO test one biotype stat file by species
    }

    private CellProcessor[] getGeneStatProcessors() {
        return new CellProcessor[]{
                new StrNotNullOrEmpty(),        //BIO_TYPE_NAME_COLUMN_NAME
                new LMinMax(0, Long.MAX_VALUE), //GENE_COUNT_COLUMN_NAME
                new LMinMax(0, Long.MAX_VALUE), //GENE_WITH_DATA_COLUMN_NAME
                new StrNotNullOrEmpty(),        //GENE_ID_COLUMN_NAME
                new NotNull(),                  //GENE_NAME_COLUMN_NAME
                new LMinMax(0, Long.MAX_VALUE), //FILTERED_GENE_PAGE_PRESENT_ANAT_ENTITY_COLUMN_NAME
                new ConvertNullTo("-"),         //MIN_RANK_COLUMN_NAME
                new ConvertNullTo("-"),         //MAX_RANK_COLUMN_NAME
                new ConvertNullTo("-"),         //MIN_RANK_ANAT_ENTITY_COLUMN_NAME
                new ConvertNullTo("-"),         //FILTERED_GENE_PAGE_MIN_RANK_COLUMN_NAME
                new ConvertNullTo("-"),         //FILTERED_GENE_PAGE_MAX_RANK_COLUMN_NAME
                new ConvertNullTo("-"),         //FILTERED_GENE_PAGE_MIN_RANK_ANAT_ENTITY_COLUMN_NAME
                new LMinMax(0, Long.MAX_VALUE), //PRESENT_BRONZE_ANAT_ENTITY_COLUMN_NAME
                new LMinMax(0, Long.MAX_VALUE), //PRESENT_SILVER_ANAT_ENTITY_COLUMN_NAME
                new LMinMax(0, Long.MAX_VALUE), //PRESENT_GOLD_ANAT_ENTITY_COLUMN_NAME
                new LMinMax(0, Long.MAX_VALUE), //ABSENT_BRONZE_ANAT_ENTITY_COLUMN_NAME
                new LMinMax(0, Long.MAX_VALUE), //ABSENT_SILVER_ANAT_ENTITY_COLUMN_NAME
                new LMinMax(0, Long.MAX_VALUE), //ABSENT_GOLD_ANAT_ENTITY_COLUMN_NAME
                new LMinMax(0, Long.MAX_VALUE), //PRESENT_BRONZE_COND_COLUMN_NAME
                new LMinMax(0, Long.MAX_VALUE), //PRESENT_SILVER_COND_COLUMN_NAME
                new LMinMax(0, Long.MAX_VALUE), //PRESENT_GOLD_COND_COLUMN_NAME
                new LMinMax(0, Long.MAX_VALUE), //ABSENT_BRONZE_COND_COLUMN_NAME
                new LMinMax(0, Long.MAX_VALUE), //ABSENT_SILVER_COND_COLUMN_NAME
                new LMinMax(0, Long.MAX_VALUE)}; //ABSENT_GOLD_COND_COLUMN_NAME
    }

    private CellProcessor[] getBiotypeStatProcessors() {
        return new CellProcessor[]{
                new StrNotNullOrEmpty(),        //BIO_TYPE_NAME_COLUMN_NAME
                new LMinMax(0, Long.MAX_VALUE), //GENE_COUNT_COLUMN_NAME
                new LMinMax(0, Long.MAX_VALUE), //GENE_WITH_DATA_COLUMN_NAME
                new LMinMax(0, Long.MAX_VALUE), //GENE_PRESENT_ABSENT_SILVER_GOLD_COLUMN_NAME
                new LMinMax(0, Long.MAX_VALUE), //PRESENT_COND_GENE_COLUMN_NAME
                new LMinMax(0, Long.MAX_VALUE), //ABSENT_COND_GENE_COLUMN_NAME
                new LMinMax(0, Long.MAX_VALUE), //FILTERED_GENE_PAGE_GENE_COLUMN_NAME
                new LMinMax(0, Long.MAX_VALUE), //ANAT_ENTITY_WITH_DATA_COLUMN_NAME
                new LMinMax(0, Long.MAX_VALUE), //PRESENT_ANAT_ENTITY_COLUMN_NAME
                new LMinMax(0, Long.MAX_VALUE), //ABSENT_ANAT_ENTITY_COLUMN_NAME
                new LMinMax(0, Long.MAX_VALUE), //COND_WITH_DATA_COLUMN_NAME
                new LMinMax(0, Long.MAX_VALUE), //PRESENT_COND_COLUMN_NAME
                new LMinMax(0, Long.MAX_VALUE), //ABSENT_COND_COLUMN_NAME
                new LMinMax(0, Long.MAX_VALUE), //FILTERED_GENE_PAGE_PRESENT_ANAT_ENTITY_COLUMN_NAME
                new ConvertNullTo("-"),         //MIN_RANK_COLUMN_NAME
                new ConvertNullTo("-"),         //MAX_RANK_COLUMN_NAME
                new LMinMax(0, Long.MAX_VALUE), //PRESENT_BRONZE_ANAT_ENTITY_COLUMN_NAME
                new LMinMax(0, Long.MAX_VALUE), //PRESENT_SILVER_ANAT_ENTITY_COLUMN_NAME
                new LMinMax(0, Long.MAX_VALUE), //PRESENT_GOLD_ANAT_ENTITY_COLUMN_NAME
                new LMinMax(0, Long.MAX_VALUE), //ABSENT_BRONZE_ANAT_ENTITY_COLUMN_NAME
                new LMinMax(0, Long.MAX_VALUE), //ABSENT_SILVER_ANAT_ENTITY_COLUMN_NAME
                new LMinMax(0, Long.MAX_VALUE), //ABSENT_GOLD_ANAT_ENTITY_COLUMN_NAME
                new LMinMax(0, Long.MAX_VALUE), //PRESENT_BRONZE_COND_COLUMN_NAME
                new LMinMax(0, Long.MAX_VALUE), //PRESENT_SILVER_COND_COLUMN_NAME
                new LMinMax(0, Long.MAX_VALUE), //PRESENT_GOLD_COND_COLUMN_NAME
                new LMinMax(0, Long.MAX_VALUE), //ABSENT_BRONZE_COND_COLUMN_NAME
                new LMinMax(0, Long.MAX_VALUE), //ABSENT_SILVER_COND_COLUMN_NAME
                new LMinMax(0, Long.MAX_VALUE)}; //ABSENT_GOLD_COND_COLUMN_NAME
    }

    private String[] getBiotypeStatHeader() {
        return new String[] {
                BIO_TYPE_NAME_COLUMN_NAME, GENE_COUNT_COLUMN_NAME, GENE_WITH_DATA_COLUMN_NAME,
                GENE_PRESENT_ABSENT_SILVER_GOLD_COLUMN_NAME,
                PRESENT_COND_GENE_COLUMN_NAME, ABSENT_COND_GENE_COLUMN_NAME,
                FILTERED_GENE_PAGE_GENE_COLUMN_NAME,
                ANAT_ENTITY_WITH_DATA_COLUMN_NAME,
                PRESENT_ANAT_ENTITY_COLUMN_NAME, ABSENT_ANAT_ENTITY_COLUMN_NAME,
                COND_WITH_DATA_COLUMN_NAME, PRESENT_COND_COLUMN_NAME, ABSENT_COND_COLUMN_NAME,
                FILTERED_GENE_PAGE_PRESENT_ANAT_ENTITY_COLUMN_NAME,
                MIN_RANK_COLUMN_NAME, MAX_RANK_COLUMN_NAME,
                PRESENT_BRONZE_ANAT_ENTITY_COLUMN_NAME, PRESENT_SILVER_ANAT_ENTITY_COLUMN_NAME,
                PRESENT_GOLD_ANAT_ENTITY_COLUMN_NAME,
                ABSENT_BRONZE_ANAT_ENTITY_COLUMN_NAME, ABSENT_SILVER_ANAT_ENTITY_COLUMN_NAME,
                ABSENT_GOLD_ANAT_ENTITY_COLUMN_NAME,
                PRESENT_BRONZE_COND_COLUMN_NAME, PRESENT_SILVER_COND_COLUMN_NAME,
                PRESENT_GOLD_COND_COLUMN_NAME,
                ABSENT_BRONZE_COND_COLUMN_NAME, ABSENT_SILVER_COND_COLUMN_NAME, ABSENT_GOLD_COND_COLUMN_NAME};
    }
    
    private String[] getGeneStatHeader() {
        return new String[] {
                GENE_ID_COLUMN_NAME, GENE_NAME_COLUMN_NAME,
                FILTERED_GENE_PAGE_PRESENT_ANAT_ENTITY_COLUMN_NAME,
                MIN_RANK_COLUMN_NAME, MAX_RANK_COLUMN_NAME, MIN_RANK_ANAT_ENTITY_COLUMN_NAME,
                FILTERED_GENE_PAGE_MIN_RANK_COLUMN_NAME, FILTERED_GENE_PAGE_MAX_RANK_COLUMN_NAME,
                FILTERED_GENE_PAGE_MIN_RANK_ANAT_ENTITY_COLUMN_NAME,
                PRESENT_BRONZE_ANAT_ENTITY_COLUMN_NAME, PRESENT_SILVER_ANAT_ENTITY_COLUMN_NAME,
                PRESENT_GOLD_ANAT_ENTITY_COLUMN_NAME,
                ABSENT_BRONZE_ANAT_ENTITY_COLUMN_NAME, ABSENT_SILVER_ANAT_ENTITY_COLUMN_NAME,
                ABSENT_GOLD_ANAT_ENTITY_COLUMN_NAME,
                PRESENT_BRONZE_COND_COLUMN_NAME, PRESENT_SILVER_COND_COLUMN_NAME,
                PRESENT_GOLD_COND_COLUMN_NAME,
                ABSENT_BRONZE_COND_COLUMN_NAME, ABSENT_SILVER_COND_COLUMN_NAME,
                ABSENT_GOLD_COND_COLUMN_NAME};
    }

}
