package org.bgee.model;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Map.Entry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.anatdev.AnatEntityService;
import org.bgee.model.anatdev.DevStage;
import org.bgee.model.anatdev.DevStageService;
import org.bgee.model.anatdev.TaxonConstraintService;
import org.bgee.model.anatdev.multispemapping.AnatEntitySimilarityService;
import org.bgee.model.dao.api.DAOManager;
import org.bgee.model.dao.api.DAOResultSet;
import org.bgee.model.dao.api.TransferObject;
import org.bgee.model.dao.api.anatdev.mapping.SummarySimilarityAnnotationDAO;
import org.bgee.model.dao.api.expressiondata.ConditionDAO;
import org.bgee.model.dao.api.expressiondata.GlobalExpressionCallDAO;
import org.bgee.model.dao.api.gene.GeneDAO;
import org.bgee.model.dao.api.gene.GeneDAO.GeneBioTypeTO;
import org.bgee.model.dao.api.gene.GeneDAO.GeneBioTypeTOResultSet;
import org.bgee.model.dao.api.gene.GeneDAO.GeneTO;
import org.bgee.model.dao.api.gene.GeneNameSynonymDAO.GeneNameSynonymTO;
import org.bgee.model.dao.api.gene.GeneXRefDAO.GeneXRefTO;
import org.bgee.model.dao.api.ontologycommon.CIOStatementDAO;
import org.bgee.model.dao.api.ontologycommon.RelationDAO;
import org.bgee.model.dao.api.source.SourceDAO.SourceTO;
import org.bgee.model.dao.api.source.SourceToSpeciesDAO;
import org.bgee.model.dao.api.species.SpeciesDAO;
import org.bgee.model.dao.api.species.SpeciesDAO.SpeciesTO;
import org.bgee.model.dao.api.species.SpeciesDAO.SpeciesTOResultSet;
import org.bgee.model.gene.Gene;
import org.bgee.model.gene.GeneBioType;
import org.bgee.model.gene.GeneXRef;
import org.bgee.model.ontology.MultiSpeciesOntology;
import org.bgee.model.ontology.Ontology;
import org.bgee.model.ontology.OntologyService;
import org.bgee.model.source.Source;
import org.bgee.model.source.SourceCategory;
import org.bgee.model.source.SourceService;
import org.bgee.model.species.Species;
import org.bgee.model.species.SpeciesService;
import org.bgee.model.species.TaxonService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.mockito.ArgumentMatcher;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * Parent class of all classes implementing unit testing. 
 * It allows to automatically log starting, succeeded and failed tests.
 * 
 * @author Frederic Bastian
 * @version Bgee 15 Sep. 2022
 * @since Bgee 13
 */
public abstract class TestAncestor {

    /**
     * Create and return an unmodifiable {@code LinkedHashMap}. The returned type is {@code Map}
     * because there is no method {@code Collections.unmodifiableLinkedHashMap}.
     *
     * @param <K>       The type of the keys.
     * @param <V>       The type of the values.
     * @param entries   A {@code List} of {@code Entry} to populate the {@code LinkedHashMap}.
     * @return          A unmodifiable {@code Map} view of a {@code LinkedHashMap}.
     */
    public static <K, V> Map<K, V> unmodifiableLinkedHashMap(List<Entry<? extends K, ? extends V>> entries) {
        final LinkedHashMap<K, V> map = new LinkedHashMap<>();
        entries.forEach(entry -> map.put(entry.getKey(), entry.getValue()));
        return Collections.unmodifiableMap(map);
    }

    private static Date parseDate(String date) {
        try {
            return new SimpleDateFormat("yyyy-MM-dd").parse(date);
        } catch (ParseException e) {
            return null;
        }
    }


    //*************************
    // SOURCES
    //*************************
    protected static final Map<Integer, SourceTO> SOURCE_TOS = unmodifiableLinkedHashMap(List.of(
            Map.entry(1, new SourceTO(1, "sourceName1", "sourceDescription1",
                    //TODO: modify those to have actual patterns to find/replace
                    "sourceXRefURL1", "sourceExperimentUrl1", "sourceEvidenceUrl1", "sourceBaseUrl1",
                    parseDate("1970-01-31"),
                    "sourceReleaseVersion1", true,
                    org.bgee.model.dao.api.source.SourceDAO.SourceTO.SourceCategory.GENOMICS,
                    1)),
            Map.entry(2, new SourceTO(2, "sourceName2", "sourceDescription2",
                    //TODO: modify those to have actual patterns to find/replace
                    "sourceXRefURL2", "sourceExperimentUrl2", "sourceEvidenceUrl2", "sourceBaseUrl2",
                    parseDate("1980-12-31"),
                    "sourceReleaseVersion2", true,
                    org.bgee.model.dao.api.source.SourceDAO.SourceTO.SourceCategory.GENOMICS,
                    2))));
    protected static final Map<Integer, Source> SOURCES = unmodifiableLinkedHashMap(
            SOURCE_TOS.values().stream().map(to ->
                    Map.entry(to.getId(), new Source(to.getId(), to.getName(), to.getDescription(),
                            to.getXRefUrl(), to.getExperimentUrl(), to.getEvidenceUrl(), to.getBaseUrl(),
                            to.getReleaseDate(), to.getReleaseVersion(), to.isToDisplay(),
                            SourceCategory.convertToSourceCategory(to.getSourceCategory().name()),
                            to.getDisplayOrder(),
                            null, null)))
            .collect(Collectors.toList()));


    //*************************
    // SPECIES
    //*************************
    protected static final Map<Integer, SpeciesTO> SPECIES_TOS = unmodifiableLinkedHashMap(List.of(
            Map.entry(1, new SpeciesTO(1, "spe1", "spe", "1", 1, 100, "genomeFilePath1",
                    "genomeVersion1", "genomeAssemblyXRef1", 1, 1)),
            Map.entry(2, new SpeciesTO(2, "spe2", "spe", "2", 2, 100, "genomeFilePath2",
                    "genomeVersion2", "genomeAssemblyXRef2", 2, 2)),
            Map.entry(3, new SpeciesTO(3, "spe3", "spe", "3", 3, 200, "genomeFilePath3",
                    "genomeVersion2", "genomeAssemblyXRef2", 2,
                    2 //use the same genome as species 2
                    ))));
    protected static Map<Integer, Species> loadSpeciesMap(boolean withSpeciesSourceInfo) {
        return unmodifiableLinkedHashMap(SPECIES_TOS.values().stream().map(speciesTO ->
                Map.entry(speciesTO.getId(), new Species(speciesTO.getId(),
                        speciesTO.getName(),
                        null, //description
                        speciesTO.getGenus(),
                        speciesTO.getSpeciesName(),
                        speciesTO.getGenomeVersion(),
                        speciesTO.getGenomeAssemblyXRef(),
                        withSpeciesSourceInfo? SOURCES.get(speciesTO.getDataSourceId()): null,
                        speciesTO.getGenomeSpeciesId(),
                        speciesTO.getParentTaxonId(),
                        //TODO: populate these two attributes
                        null, null,
                        speciesTO.getDisplayOrder()))
                ).collect(Collectors.toList())
        );
    }
    protected static final Map<Integer, Species> SPECIES = loadSpeciesMap(false);
    protected static final Map<Integer, Species> SPECIES_WITH_SOURCE_INFO = loadSpeciesMap(true);


    //*************************
    // GENES
    //*************************
    protected static final Map<Integer, GeneBioTypeTO> GENE_BIO_TYPE_TOS = unmodifiableLinkedHashMap(List.of(
            Map.entry(1, new GeneBioTypeTO(1, "geneBioType1")),
            Map.entry(2, new GeneBioTypeTO(2, "geneBioType2"))));
    protected static final Map<Integer, GeneBioType> GENE_BIO_TYPES = unmodifiableLinkedHashMap(
            GENE_BIO_TYPE_TOS.values().stream().map(to ->
                    Map.entry(to.getId(), new GeneBioType(to.getName())))
            .collect(Collectors.toList()));

    protected static final Map<Integer, GeneTO> GENE_TOS = unmodifiableLinkedHashMap(List.of(
            Map.entry(1, new GeneTO(
                    1,                  //Bgee gene ID
                    "geneId1",          //public gene ID
                    "geneName1",        //name
                    "geneDescription1", //description
                    1,                  //speciesId
                    1,                  //geneBioTypeId
                    100,                //OMAParentNodeId
                    true,               //From Ensembl?
                    1                   //Number of genes with same public ID
                    )),
            Map.entry(2, new GeneTO(
                    2, "geneId2", "geneName2", "geneDescription2",
                    1, //same species as geneId1
                    2, //alternative geneBioType
                    100, true, 1)),
            Map.entry(3, new GeneTO(3,
                    "geneId3_4", //two different genes with same public ID in species 2 and species 3
                    "geneName3", "geneDescription3",
                    2,           //species 2
                    1, 100,
                    true,        //species 2 and 3 has a genome from a different database than Ensembl
                    2            //two different genes with same public ID in species 2 and species 3
                    )),
            Map.entry(4, new GeneTO(4,
                    "geneId3_4", //two different genes with same public ID in species 2 and species 3
                    "geneName4", "geneDescription4",
                    2,           //species 3
                    1, 100,
                    false,       //species 2 and 3 has a genome from a different database than Ensembl
                    2            //two different genes with same public ID in species 2 and species 3
                    ))));
    protected static final Map<Integer, GeneXRefTO> GENE_X_REF_TOS = unmodifiableLinkedHashMap(List.of(
            Map.entry(1, new GeneXRefTO(
                    1, //Bgee gene ID
                    "xRefId1",
                    "xRefName1",
                    SPECIES_TOS.get(GENE_TOS.get(1).getSpeciesId()).getDataSourceId()  //dataSourceId
                    )),
            Map.entry(2, new GeneXRefTO(
                    2, //Bgee gene ID
                    "xRefId2",
                    "xRefName2",
                    SPECIES_TOS.get(GENE_TOS.get(2).getSpeciesId()).getDataSourceId()  //dataSourceId
                    )),
            Map.entry(3, new GeneXRefTO(
                    3, //Bgee gene ID
                    "xRefId3",
                    "xRefName3",
                    SPECIES_TOS.get(GENE_TOS.get(3).getSpeciesId()).getDataSourceId()  //dataSourceId
                    )),
            Map.entry(4, new GeneXRefTO(
                    4, //Bgee gene ID
                    "xRefId4",
                    "xRefName4",
                    SPECIES_TOS.get(GENE_TOS.get(4).getSpeciesId()).getDataSourceId()  //dataSourceId
                    ))
            ));
    protected static final Map<Integer, GeneXRef> GENE_X_REFS = unmodifiableLinkedHashMap(
            GENE_X_REF_TOS.entrySet().stream().map(e ->
                    Map.entry(e.getKey(), new GeneXRef(
                            e.getValue().getXRefId(),
                            e.getValue().getXRefName(),
                            SOURCES.get(e.getValue().getDataSourceId()),
                            GENE_TOS.get(e.getValue().getBgeeGeneId()).getGeneId(),
                            SPECIES.get(GENE_TOS.get(e.getValue().getBgeeGeneId()).getSpeciesId()).getScientificName())))
            .collect(Collectors.toList()));
    protected static final Map<Integer, GeneNameSynonymTO> GENE_NAME_SYNONYM_TOS = unmodifiableLinkedHashMap(List.of(
            Map.entry(1, new GeneNameSynonymTO(
                    1, //Bgee gene ID
                    "synonym_geneId1_1")),
            Map.entry(2, new GeneNameSynonymTO(
                    1, //Bgee gene ID
                    "synonym_geneId1_2")),
            Map.entry(3, new GeneNameSynonymTO(
                    2, //Bgee gene ID
                    "synonym_geneId2_1"))));
    protected static Map<Integer, Gene> loadGeneMap(boolean withSynonyms, boolean withXRefs) {
        return unmodifiableLinkedHashMap(
                GENE_TOS.values().stream().map(to ->
                Map.entry(to.getId(), new Gene(
                        to.getGeneId(),
                        to.getName(),
                        to.getDescription(),
                        withSynonyms? GENE_NAME_SYNONYM_TOS.values().stream() //synonyms
                            .filter(synTO -> synTO.getBgeeGeneId().equals(to.getId()))
                            .map(synTO -> synTO.getGeneNameSynonym())
                            .collect(Collectors.toSet()): null,
                        withXRefs? GENE_X_REF_TOS.entrySet().stream()     //GeneXRefs
                            .filter(e -> e.getValue().getBgeeGeneId().equals(to.getId()))
                            .map(e -> GENE_X_REFS.get(e.getKey()))
                            .collect(Collectors.toSet()): null,
                        SPECIES.get(to.getSpeciesId()),
                        GENE_BIO_TYPES.get(to.getGeneBioTypeId()),
                        to.getGeneMappedToGeneIdCount())))
        .collect(Collectors.toList()));
    }
    protected static final Map<Integer, Gene> GENES = loadGeneMap(false, false);
    protected static final Map<Integer, Gene> GENES_WITH_SYNONYMS_XREFS = loadGeneMap(true, true);

    /**
     * Get a mock {@code DAOResultSet} configured to returned the provided {@code TransferObject}s.
     * 
     * @param resultSetType A {@code Class} that is the type of {@code DAOResultSet} to return.
     * @param values        A {@code List} of {@code TransferObject}s to be returned by 
     *                      the mock {@code DAOResultSet}.
     * @return              A configured mock {@code DAOResultSet}.
     * @param T             The type of {@code TransferObject} to return.
     * @param U             The type of {@code DAOResultSet} to return.
     */
    protected static <T extends TransferObject, U extends DAOResultSet<T>> U getMockResultSet(
            Class<U> resultSetType, List<T> values) {
        /**
         * An {@code Answer} to manage calls to {@code next} method.
         */
        final class ResultSetNextAnswer implements Answer<Boolean> {
            /**
             * An {@code int} that is the number of results to be returned by this {@code Answer}.
             */
            private final int size;
            /**
             * An {@code int} defining the current iteration (starts at -1, 
             * so that the first call to next put the cursor on the first result).
             */
            private int iteration;
            
            private ResultSetNextAnswer(int size) {
                this.iteration = -1;
                this.size = size;
            }
            @Override
            public Boolean answer(InvocationOnMock invocation) throws Throwable {
                this.iteration++;
                if (this.iteration < this.size) {
                    return true;
                }
                return false;
            }
        }
        /**
         * An {@code Answer} to manage calls to {@code getTO} method.
         */
        final class ResultSetGetTOAnswer implements Answer<T> {
            /**
             * A {@code List} of {@code T}s to be returned by the mock {@code DAOResultSet}.
             */
            private final List<T> values;
            /**
             * The {@code Answer} used by the same mock {@code DAOResultSet} to respond to {@code next()}.
             * Allows to know which element to return;
             */
            private final ResultSetNextAnswer answerToNext;
            
            private ResultSetGetTOAnswer(List<T> values, ResultSetNextAnswer answerToNext) {
                this.values = values;
                this.answerToNext = answerToNext;
            }
            @Override
            public T answer(InvocationOnMock invocation) throws Throwable {
                if (this.answerToNext.iteration >= 0 && 
                        this.answerToNext.iteration < this.values.size()) {
                    return this.values.get(this.answerToNext.iteration);
                }
                return null;
            }
        }
        
        List<T> clonedValues = new ArrayList<>(values);
        U rs = mock(resultSetType);
        ResultSetNextAnswer nextAnswer = new ResultSetNextAnswer(clonedValues.size());
        ResultSetGetTOAnswer getTOAnswer = new ResultSetGetTOAnswer(clonedValues, nextAnswer);
        
        when(rs.next()).thenAnswer(nextAnswer);
        when(rs.getTO()).thenAnswer(getTOAnswer);
        //XXX: note that the normal behavior of stream() and getAllTOs is to throw an exception 
        //if next was already called on this resultset. But that would need yet another 
        //custom Answer class...
        when(rs.stream()).thenReturn(clonedValues.stream());
        when(rs.getAllTOs()).thenReturn(clonedValues);
        
        return rs;
    }
    
    /**
     * An {@code ArgumentMatcher} allowing to determine whether two {@code Collection}s 
     * contains the same elements (as considered by their {@code equals} method), 
     * independently of the iteration order of the {@code Collection}s.
     */
    protected static class IsCollectionEqual<T> implements ArgumentMatcher<Collection<T>> {
        private final static Logger log = LogManager.getLogger(IsCollectionEqual.class.getName());
        private final Collection<?> expectedCollection;
        
        IsCollectionEqual(Collection<T> expectedCollection) {
            log.traceEntry("{}", expectedCollection);
            this.expectedCollection = expectedCollection;
            log.traceExit();
        }

        @Override
        public boolean matches(Collection<T> argument) {
            log.traceEntry("{}", argument);
            log.trace("Trying to match expected Collection [" + expectedCollection + "] versus "
                    + "provided argument [" + argument + "]");
            if (expectedCollection == argument) {
                return log.traceExit(true);
            }
            if (expectedCollection == null) {
                if (argument == null) {
                    return log.traceExit(true);
                } 
                return log.traceExit(false);
            } else if (argument == null) {
                return log.traceExit(false);
            }
            if (argument.size() != expectedCollection.size()) {
                return log.traceExit(false);
            }
            return log.traceExit(argument.containsAll(expectedCollection) && expectedCollection.containsAll(argument));
        }
    }
    /**
     * Helper method to obtain a {@link IsCollectionEqual} {@code ArgumentMatcher}, 
     * for readability. 
     * @param expectedCollection    The {@code Collection} that is expected, to be used 
     *                              in stub or verify methods. 
     */
    protected static <T> Collection<T> collectionEq(Collection<T> expectedCollection) {
        return argThat(new IsCollectionEqual<>(expectedCollection));
    }


    //attributes that will hold mock services, DAOs and complex objects
    //Services
    protected ServiceFactory serviceFactory;
    protected SpeciesService speciesService;
    protected OntologyService ontService;
    protected AnatEntityService anatEntityService;
    protected DevStageService devStageService;
    protected TaxonService taxonService;
    protected SourceService sourceService;
    protected TaxonConstraintService taxonConstraintService;
    protected AnatEntitySimilarityService anatEntitySimilarityService;
    //DAOs
    protected DAOManager manager;
    protected GlobalExpressionCallDAO globalExprCallDAO;
    protected ConditionDAO condDAO;
    protected GeneDAO geneDAO;
    protected RelationDAO relationDAO;
    protected SpeciesDAO speciesDAO;
    protected SourceToSpeciesDAO sourceToSpeciesDAO;
    protected SummarySimilarityAnnotationDAO sumSimAnnotDAO;
    protected CIOStatementDAO cioStatementDAO;
    //Complex objects
    protected Ontology<AnatEntity, String> anatEntityOnt;
    protected MultiSpeciesOntology<AnatEntity, String> multiSpeAnatEntityOnt;
    protected Ontology<DevStage, String> devStageOnt;
    protected MultiSpeciesOntology<DevStage, String> multiSpeDevStageOnt;

    /**
     * Default Constructor. 
     */
    public TestAncestor() {
        
    }

    @Before
    //suppress warning as we cannot specify generic type for a mock
    @SuppressWarnings("unchecked")
    public void loadMockObjects() {
        getLogger().traceEntry();

        //Services
        this.serviceFactory = mock(ServiceFactory.class);
        this.speciesService = mock(SpeciesService.class);
        this.ontService = mock(OntologyService.class);
        this.anatEntityService = mock(AnatEntityService.class);
        this.devStageService = mock(DevStageService.class);
        this.taxonService = mock(TaxonService.class);
        this.sourceService = mock(SourceService.class);
        this.taxonConstraintService = mock(TaxonConstraintService.class);
        this.anatEntitySimilarityService = mock(AnatEntitySimilarityService.class);
        //DAOs
        this.manager = mock(DAOManager.class);
        this.globalExprCallDAO = mock(GlobalExpressionCallDAO.class);
        this.condDAO = mock(ConditionDAO.class);
        this.geneDAO = mock(GeneDAO.class);
        this.relationDAO = mock(RelationDAO.class);
        this.speciesDAO = mock(SpeciesDAO.class);
        this.sourceToSpeciesDAO = mock(SourceToSpeciesDAO.class);
        this.sumSimAnnotDAO = mock(SummarySimilarityAnnotationDAO.class);
        this.cioStatementDAO = mock(CIOStatementDAO.class);
        //Complex objects
        this.anatEntityOnt = mock(Ontology.class);
        this.multiSpeAnatEntityOnt = mock(MultiSpeciesOntology.class);
        this.devStageOnt = mock(Ontology.class);
        this.multiSpeDevStageOnt = mock(MultiSpeciesOntology.class);


        //Services
        when(this.serviceFactory.getSpeciesService()).thenReturn(this.speciesService);
        when(this.serviceFactory.getOntologyService()).thenReturn(this.ontService);
        when(this.serviceFactory.getAnatEntityService()).thenReturn(this.anatEntityService);
        when(this.serviceFactory.getDevStageService()).thenReturn(this.devStageService);
        when(this.serviceFactory.getTaxonService()).thenReturn(this.taxonService);
        when(this.serviceFactory.getSourceService()).thenReturn(this.sourceService);
        when(this.serviceFactory.getTaxonConstraintService()).thenReturn(this.taxonConstraintService);
        when(this.serviceFactory.getAnatEntitySimilarityService()).thenReturn(this.anatEntitySimilarityService);
        //DAOs
        when(this.serviceFactory.getDAOManager()).thenReturn(this.manager);
        when(this.manager.getGlobalExpressionCallDAO()).thenReturn(this.globalExprCallDAO);
        when(this.manager.getConditionDAO()).thenReturn(this.condDAO);
        when(this.manager.getGeneDAO()).thenReturn(this.geneDAO);
        when(this.manager.getRelationDAO()).thenReturn(this.relationDAO);
        when(this.manager.getSpeciesDAO()).thenReturn(this.speciesDAO);
        when(this.manager.getSourceToSpeciesDAO()).thenReturn(this.sourceToSpeciesDAO);
        when(this.manager.getSummarySimilarityAnnotationDAO()).thenReturn(this.sumSimAnnotDAO);
        when(this.manager.getCIOStatementDAO()).thenReturn(this.cioStatementDAO);

        getLogger().traceExit();
    }


    //*****************************
    // GENERAL MOCK CONFIGURATION
    //*****************************
    protected void whenSpeciesDAOGetSpeciesByIds() {
        getLogger().traceEntry();
        SpeciesTOResultSet rs = getMockResultSet(SpeciesTOResultSet.class, List.copyOf(SPECIES_TOS.values()));
        when(this.speciesDAO.getSpeciesByIds(any(), any())).thenReturn(rs);
        getLogger().traceExit();
    }
    protected void whenGeneDAOGetGeneBioTypes() {
        getLogger().traceEntry();
        GeneBioTypeTOResultSet rs = getMockResultSet(GeneBioTypeTOResultSet.class,
                List.copyOf(GENE_BIO_TYPE_TOS.values()));
        when(this.geneDAO.getGeneBioTypes()).thenReturn(rs);
        getLogger().traceExit();
    }


    //*****************************
    // OTHER
    //*****************************
    /**
     * A {@code TestWatcher} to log starting, succeeded and failed tests. 
     */
    @Rule
    public TestWatcher watchman = new TestWatcher() {
        @Override
        protected void starting(Description description) {
            getLogger().info("Starting test: {}", description);
        }
        @Override
        protected void failed(Throwable e, Description description) {
            if (getLogger().isErrorEnabled()) {
                getLogger().error("Test failed: " + description, e);
            }
        }
        @Override
        protected void succeeded(Description description) {
            getLogger().info("Test succeeded: {}", description);
        }
    };
    
    /**
     * Return the logger of the class. 
     * @return     A {@code Logger}
     */
    protected Logger getLogger() {
         return LogManager.getLogger(this.getClass().getName());
    }
}
