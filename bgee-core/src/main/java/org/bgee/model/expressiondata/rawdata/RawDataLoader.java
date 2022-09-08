package org.bgee.model.expressiondata.rawdata;


//RnaSeqTechnology technology = clonedAttrs.contains(RawDataService.Attribute.TECHNOLOGY)?
//        //TODO: implement protocol name in the database schema and in the dao
//        new RnaSeqTechnology( null, libIdToLibTO.get(to.getLibraryId()).getId(),
//                Strand.convertToStrand(to.getStrandSelection().getStringRepresentation()),
//                SequencedTranscriptPart.convertToSequencedTranscriptPart(
//                        to.getSequencedTranscriptPart().getStringRepresentation()),
//                CellCompartment.convertToCellCompartment(
//                        to.getCellCompartment().getStringRepresentation()),
//                libIdToLibTO.get(to.getLibraryId()).isSampleMultiplexing(),
//                libIdToLibTO.get(to.getLibraryId()).isLibraryMultiplexing(), 
//                false, to.getBarcode()):
//                    null;

//if (!attrs.contains(RawDataService.Attribute.TECHNOLOGY)) {
//    daoAttrs.remove(RNASeqLibraryAnnotatedSampleDAO.Attribute.CELL_COMPARTMENT);
//    daoAttrs.remove(RNASeqLibraryAnnotatedSampleDAO.Attribute.FRAGMENTATION);
//    daoAttrs.remove(RNASeqLibraryAnnotatedSampleDAO.Attribute.GENOTYPE_ID);
//    daoAttrs.remove(RNASeqLibraryAnnotatedSampleDAO.Attribute.STRAND_SELECTION);
//    daoAttrs.remove(RNASeqLibraryAnnotatedSampleDAO.Attribute.SEQUENCED_TRANSCRIPT_PART);
//    daoAttrs.remove(RNASeqLibraryAnnotatedSampleDAO.Attribute.POPULATION_CAPTURE_ID);
//    daoAttrs.remove(RNASeqLibraryAnnotatedSampleDAO.Attribute.BARCODE);
//}
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.anatdev.AnatEntityService;
import org.bgee.model.anatdev.DevStage;
import org.bgee.model.anatdev.DevStageService;
import org.bgee.model.dao.api.expressiondata.rawdata.DAORawDataConditionFilter;
import org.bgee.model.dao.api.expressiondata.rawdata.DAORawDataFilter;
import org.bgee.model.dao.api.expressiondata.rawdata.RawDataConditionDAO.RawDataConditionTOResultSet;
import org.bgee.model.dao.api.expressiondata.rawdata.microarray.AffymetrixChipDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.microarray.AffymetrixProbesetDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.microarray.MicroarrayExperimentDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.rnaseq.RNASeqExperimentDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.rnaseq.RNASeqLibraryAnnotatedSampleDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.rnaseq.RNASeqLibraryDAO.RNASeqLibraryTO;
import org.bgee.model.expressiondata.rawdata.RawCall.ExclusionReason;
import org.bgee.model.expressiondata.rawdata.RawDataCondition.RawDataSex;
import org.bgee.model.expressiondata.rawdata.microarray.AffymetrixChip;
import org.bgee.model.expressiondata.rawdata.microarray.AffymetrixExperiment;
import org.bgee.model.expressiondata.rawdata.microarray.AffymetrixProbeset;
import org.bgee.model.expressiondata.rawdata.rnaseq.RnaSeqExperiment;
import org.bgee.model.expressiondata.rawdata.rnaseq.RnaSeqLibraryAnnotatedSample;
import org.bgee.model.expressiondata.rawdata.rnaseq.RnaSeqLibraryPipelineSummary;
import org.bgee.model.gene.Gene;
import org.bgee.model.source.Source;
import org.bgee.model.species.Species;

public class RawDataLoader {
    private final static Logger log = LogManager.getLogger(RawDataLoader.class.getName());

    //XXX Do we really need a Set of rawDataFilters?
    private final Set<RawDataFilter> rawDataFilters;

    //attributes needed for making the necessary queries
    private final RawDataService rawDataService;
    /**
     * A {@code Set} of {@code DAORawDataFilter}s corresponding to the conversion
     * of the {@code RawDataFilter}s in {@link #rawDataFilters};
     */
    private final Set<DAORawDataFilter> daoRawDataFilters;
    /**
     * A {@code Map} where keys are {@code Integer}s corresponding to Bgee internal gene IDs,
     * the associated value being the corresponding {@code Gene}.
     */
    private final Map<Integer, Gene> geneMap;
    
    private final Map<Integer, RawDataCondition> rawDataConditionMap;

    RawDataLoader(Set<RawDataFilter> rawDataFilters, RawDataService rawDataService,
            Map<Integer, Gene> geneMap, Set<DAORawDataFilter> daoRawDataFilters) {
        if (rawDataFilters == null || rawDataFilters.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("At least one RawDataFilter must be provided"));
        }
        if (rawDataFilters.contains(null)) {
            throw log.throwing(new IllegalArgumentException("No RawDataFilter can be null"));
        }
        if (rawDataService == null) {
            throw log.throwing(new IllegalArgumentException("A RawDataService must be provided"));
        }
        this.rawDataFilters = Collections.unmodifiableSet(
                rawDataFilters == null? new HashSet<>(): new HashSet<>(rawDataFilters));
        this.rawDataService = rawDataService;
        this.geneMap = geneMap;
        this.rawDataConditionMap = loadRawDataConditionMap(rawDataFilters);
        this.daoRawDataFilters = daoRawDataFilters;
    }

///////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////// METHODS LOADING AFFYMETRIX RAW DATA ///////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Load affymetrix experiments from the database
     * 
     * @param affymetrixExperimentIds   A {@code Collection} of {@code String} allowing to filter
     *                                  on affymetrix experiment IDs
     * @param affymetrixChipIds         A {@code Collection} of {@code String} allowing to filter
     *                                  on affymetrix chip IDs
     * @param attrs                     A {@code Collection}  of {@code RawDataService.Attribute}
     *                                  defining information to retrieve in the 
     *                                  {@code MicroarrayExperiment}s.
     * @return  A {@code Stream} of {@code AffymetrixExperiment}s.
     *          If the {@code Stream} contains no element, it means that there were no data
     *          of this type for the requested parameters.
     */
    public Stream<AffymetrixExperiment> loadAffymetrixExperiments(Collection<String> affymetrixExperimentIds,
            Collection<String> affymetrixChipIds, Collection<RawDataService.Attribute> attrs) {
        log.traceEntry("{}, {}, {}, {}", affymetrixExperimentIds, affymetrixChipIds, attrs);
        final Set<String> clonedExpIds =  Collections.unmodifiableSet(affymetrixExperimentIds == null?
                new HashSet<String>(): new HashSet<String>(affymetrixExperimentIds));
        final Set<String> clonedChipIds =  Collections.unmodifiableSet(affymetrixChipIds == null?
                new HashSet<String>(): new HashSet<String>(affymetrixChipIds));
        final Set<RawDataService.Attribute> clonedAttrs=  Collections.unmodifiableSet(attrs == null?
                EnumSet.allOf(RawDataService.Attribute.class) : EnumSet.copyOf(attrs));

        MicroarrayExperimentDAO expDAO = this.rawDataService.getServiceFactory()
                .getDAOManager().getMicroarrayExperimentDAO();

        // return all datasources if required
        final Map<Integer,Source> dataSourceIdToDataSource = 
                clonedAttrs.contains(RawDataService.Attribute.DATASOURCE) == true?
                this.rawDataService.getServiceFactory().getSourceService().loadSourcesByIds(null):
                    new HashMap<>();

        // transform RnaSeqService Attributes to DAO attributes
        Set<MicroarrayExperimentDAO.Attribute> daoAttrs = EnumSet.allOf(MicroarrayExperimentDAO
                .Attribute.class);
        if (!clonedAttrs.contains(RawDataService.Attribute.DATASOURCE)) {
            daoAttrs.remove(MicroarrayExperimentDAO.Attribute.DATA_SOURCE_ID);
        }
        //generate AffymetrixExperiment objects and retrieve them
        return log.traceExit(this.daoRawDataFilters.stream().map( filter -> {
            return expDAO.getExperiments(clonedExpIds, clonedChipIds, filter, 
                    daoAttrs).stream()
                    .map(to -> new AffymetrixExperiment( to.getId(), to.getName(), 
                            to.getDescription(), to.getDataSourceId() == null ? null: 
                                dataSourceIdToDataSource.get(to.getDataSourceId())))
                    .collect(Collectors.toSet());
            }).flatMap(e -> e.stream()));
    }

    /**
     * Load affymetrix chips from the database
     * 
     * @param affymetrixExperimentIds   A {@code Collection} of {@code String} allowing to filter
     *                                  on affymetrix experiment IDs
     * @param affymetrixChipIds         A {@code Collection} of {@code String} allowing to filter
     *                                  on affymetrix chip IDs
     * @param attrs                     A {@code Collection}  of {@code RawDataService.Attribute}
     *                                  defining information to retrieve in the 
     *                                  {@code MicroarrayExperiment}s.
     * @return  A {@code Stream} of {@code AffymetrixChip}s.
     *          If the {@code Stream} contains no element, it means that there were no data
     *          of this type for the requested parameters.
     */
    public Stream<AffymetrixChip> loadAffymetrixChips(Collection<String> affymetrixExperimentIds, 
            Collection<String> affymetrixChipIds, Set<RawDataService.Attribute> attrs) {
        log.traceEntry("{}, {}, {}", affymetrixExperimentIds, affymetrixChipIds, attrs);
        final Set<String> clonedExpIds =  Collections.unmodifiableSet(affymetrixExperimentIds == null?
                new HashSet<String>(): new HashSet<String>(affymetrixExperimentIds));
        final Set<String> clonedChipIds =  Collections.unmodifiableSet(affymetrixChipIds == null?
                new HashSet<String>(): new HashSet<String>(affymetrixChipIds));
        final Set<RawDataService.Attribute> clonedAttrs=  Collections.unmodifiableSet(attrs == null?
                EnumSet.allOf(RawDataService.Attribute.class) : EnumSet.copyOf(attrs));

        // load Experiments
        final Map <String, AffymetrixExperiment> expIdToExperiments = 
                loadAffymetrixExperiments(clonedExpIds, clonedChipIds, clonedAttrs)
                .collect(Collectors.toMap(e -> e.getId(), e -> e));

        // load conditions based on rawDataFilters if required
        Map<Integer, RawDataCondition> condIdToCond = 
                clonedAttrs.contains(RawDataService.Attribute.ANNOTATION) == true?
                        rawDataConditionMap:
                    null;

      //create dao affymetrix chip attributes from service attributes
        Set<AffymetrixChipDAO.Attribute> daoAttrs = fromAttrsToAffyChipDAOAttrs(clonedAttrs);

        return log.traceExit(this.daoRawDataFilters.stream().map( filter -> {
            return this.rawDataService.getServiceFactory().getDAOManager().getAffymetrixChipDAO()
                    .getAffymetrixChips(clonedExpIds, clonedChipIds, filter, daoAttrs)
            .stream().map(to -> {
                return new AffymetrixChip( to.getAffymetrixChipId(), expIdToExperiments.get(to.getExperimentId()), 
                        new RawDataAnnotation(condIdToCond.get(to.getConditionId()), null, null, null));
            }).collect(Collectors.toSet());
        }).flatMap(c -> c.stream()));
    }

    private Map<Integer, AffymetrixChip> loadAffymetrixChipsByBgeeChipIds(Collection<String> affyExpIds,
            Collection<String> affyChipIds, Set<RawDataService.Attribute> attrs) {
        log.traceEntry("{}, {}, {}", affyExpIds, affyChipIds, attrs);

        // load Experiments
        final Map <String, AffymetrixExperiment> expIdToExperiments = 
                loadAffymetrixExperiments(affyExpIds, affyChipIds, attrs)
                .collect(Collectors.toMap(e -> e.getId(), e -> e));

        // load conditions based on rawDataFilters if required
        final Map<Integer, RawDataCondition> condIdToCond = 
                attrs.contains(RawDataService.Attribute.ANNOTATION) == true?
                        rawDataConditionMap:
                    null;

        //create dao affymetrix chip attributes from service attributes
        Set<AffymetrixChipDAO.Attribute> daoAttrs = fromAttrsToAffyChipDAOAttrs(attrs);

        return log.traceExit(this.daoRawDataFilters.stream().map( filter -> {
            return this.rawDataService.getServiceFactory().getDAOManager().getAffymetrixChipDAO()
                    .getAffymetrixChips(affyExpIds, affyChipIds, filter, daoAttrs)
                    .stream().collect(Collectors.toMap(to -> {Integer id = to.getId(); 
                    return id;
                    }, to -> {
                        RawDataAnnotation annotation = null;
                        if (to.getConditionId() != null) {
                            annotation = new RawDataAnnotation(condIdToCond.get(to.getConditionId()),
                                    null, null, null);
                        }
                        AffymetrixChip chip = new AffymetrixChip(to.getAffymetrixChipId(),
                                expIdToExperiments.containsKey(to.getExperimentId())? expIdToExperiments.get(to.getExperimentId()):null, 
                                        annotation);
                        return chip;
                    }));
        }).flatMap(a -> a.entrySet().stream())
                .collect(Collectors.toMap(q -> q.getKey(), q -> q.getValue())));
    }

    /**
     * Load affymetrix probesets from the database
     * 
     * @param affymetrixProbesetIds     A {@code Collection} of {@code String} allowing to filter
     *                                  on affymetrix probeset IDs
     * @param affymetrixChipIds         A {@code Collection} of {@code String} allowing to filter
     *                                  on affymetrix chip IDs
     * @param affymetrixExperimentIds   A {@code Collection} of {@code String} allowing to filter
     *                                  on affymetrix experiment IDs
     * @param attrs                     A {@code Collection}  of {@code RawDataService.Attribute}
     *                                  defining information to retrieve in the 
     *                                  {@code MicroarrayExperiment}s.
     * @return  A {@code Stream} of {@code AffymetrixProbeset}s.
     *          If the {@code Stream} contains no element, it means that there were no data
     *          of this type for the requested parameters.
     */
    public Stream<AffymetrixProbeset> loadAffymetrixProbesets(Collection<String> affymetrixExperimentIds,
            Collection<String> affymetrixChipIds, Collection<String> affymetrixProbesetIds,
            Collection<RawDataService.Attribute> attrs) {
        log.traceEntry("{}, {}, {}, {}", affymetrixExperimentIds, affymetrixChipIds,
                affymetrixProbesetIds, attrs);
        final Set<String> clonedProbesetIds =  Collections.unmodifiableSet(affymetrixProbesetIds == null?
                new HashSet<String>(): new HashSet<String>(affymetrixProbesetIds));
        final Set<String> clonedChipIds =  Collections.unmodifiableSet(affymetrixChipIds == null?
                new HashSet<String>(): new HashSet<String>(affymetrixChipIds));
        final Set<String> clonedExpIds =  Collections.unmodifiableSet(affymetrixExperimentIds == null?
                new HashSet<String>(): new HashSet<String>(affymetrixExperimentIds));
        final Set<RawDataService.Attribute> clonedAttrs=  Collections.unmodifiableSet(attrs == null?
                EnumSet.allOf(RawDataService.Attribute.class) : EnumSet.copyOf(attrs));

        // return all Assays by bgee chip ids
        final Map <Integer, AffymetrixChip> bgeeChipIdToAssay = 
            this.loadAffymetrixChipsByBgeeChipIds(clonedExpIds, clonedChipIds,clonedAttrs);

        //from service attributes to dao attributes
        Set<AffymetrixProbesetDAO.Attribute> daoAttrs = fromAttrsToAffyProbesetDAOAttrs(clonedAttrs);
        
        return this.daoRawDataFilters.stream().map( filter -> {
            return this.rawDataService.getServiceFactory().getDAOManager().getAffymetrixProbesetDAO()
                    .getAffymetrixProbesets(clonedExpIds, clonedChipIds, clonedProbesetIds, filter, daoAttrs)
            .stream().map(to -> {
                return new AffymetrixProbeset(to.getId(), 
                        bgeeChipIdToAssay.get(to.getAssayId()),
                            new RawCall(geneMap.get(to.getBgeeGeneId()), to.getPValue(),
                                    to.getExpressionConfidence(),
                                    ExclusionReason.convertToExclusionReason(to.getExclusionReason()
                                            .getStringRepresentation())),
                            to.getNormalizedSignalIntensity(), to.getqValue(), to.getRank());
            }).collect(Collectors.toSet());
        }).flatMap(c -> c.stream());
    }
    
    private Map<Integer, RawDataCondition> loadRawDataConditionMap(
            Collection<RawDataFilter> dataFilters) {
        log.traceEntry("{}, {}", dataFilters);
        // combine condition filters coming from all dataFilters and retrieve corresponding 
        // dao condition filters
        AnatEntityService aeService = rawDataService.getServiceFactory().getAnatEntityService();
        DevStageService dsService = rawDataService.getServiceFactory().getDevStageService();
        Set<DAORawDataConditionFilter> condFiltersDAO = 
                convertRawDataConditionFiltersToDAORawDataConditionFilters(
                        dataFilters.stream().map(df -> df.getConditionFilters())
                        .flatMap(df -> df.stream()).collect(Collectors.toSet()));
        // combine geneFilters coming from all dataFilters and retrieve corresponding speciesMap
        final Map<Integer, Species> speciesMap = rawDataService.getServiceFactory()
                .getSpeciesService().loadSpeciesMapFromGeneFilters(dataFilters.stream()
                        .map(df -> df.getGeneFilters())
                        .flatMap(gf -> gf.stream()).collect(Collectors.toSet()), false);

        final Map<String, AnatEntity> anatEntitiesMap = aeService.loadAnatEntities(
                speciesMap.keySet(), true, condFiltersDAO.stream()
                    .map(cf -> cf.getAnatEntityIds()).flatMap(aes -> aes.stream())
                    .collect(Collectors.toSet()), false)
                .collect(Collectors.toMap(ae -> ae.getId(), ae -> ae));
        final Map<String, AnatEntity> cellTypesMap = aeService.loadAnatEntities(
                speciesMap.keySet(), true, condFiltersDAO.stream()
                    .map(cf -> cf.getCellTypeIds()).flatMap(aes -> aes.stream())
                    .collect(Collectors.toSet()), false)
                .collect(Collectors.toMap(ct -> ct.getId(), ct -> ct));
        final Map<String,DevStage> devStagesMap = dsService.loadDevStages(
                speciesMap.keySet(), true, condFiltersDAO.stream()
                    .map(cf -> cf.getDevStageIds()).flatMap(aes -> aes.stream())
                    .collect(Collectors.toSet()), false)
                .collect(Collectors.toMap(ds -> ds.getId(), ds -> ds));
        final Map<String,RawDataSex> sexesMap = EnumSet.allOf(RawDataSex.class)
                .stream().collect(Collectors.toMap(s -> s.getStringRepresentation(), s -> s));
        //load raw data conditions
        RawDataConditionTOResultSet rawDataCondTOs = rawDataService.getServiceFactory().getDAOManager()
                .getRawDataConditionDAO().getRawDataConditions(speciesMap.keySet(),
                        condFiltersDAO, null);
        //create Map with condition ID as key and RawDataCondition as value
        return log.traceExit(rawDataCondTOs.stream().collect(Collectors
                .toMap(condTO -> condTO.getId(),
                        condTO -> 
                new RawDataCondition( anatEntitiesMap.get(condTO.getAnatEntityId()),
                        devStagesMap.get(condTO.getStageId()),
                        cellTypesMap.get(condTO.getCellTypeId()),
                        sexesMap.get(condTO.getSex().getStringRepresentation()),
                        condTO.getStrainId(), null)
                )));
    }
    
    private static Set<DAORawDataConditionFilter> 
    convertRawDataConditionFiltersToDAORawDataConditionFilters(
            Collection<RawDataConditionFilter> condFilters) {
        log.traceEntry("{}", condFilters);
        if (condFilters == null || condFilters.isEmpty()) {
            return log.traceExit(Set.of());
        }
        if (condFilters.contains(null)) {
            throw log.throwing(new IllegalArgumentException("no condition filter can be null"));
        }
        return log.traceExit(condFilters.stream().map(cf -> new DAORawDataConditionFilter(
                cf.getAnatEntityIds(),cf.getDevStageIds(),
                cf.getCellTypeIds(), cf.getSexes(), cf.getStrains(),
                cf.getIncludeSubConditions(), cf.getIncludeParentConditions()))
        .collect(Collectors.toSet()));
    }

    // generate dao attributes from service Attribute
    private static Set<AffymetrixChipDAO.Attribute> fromAttrsToAffyChipDAOAttrs(Set<RawDataService.Attribute> attrs) {
        log.traceEntry("{}", attrs);
        Set<AffymetrixChipDAO.Attribute> daoAttrs = EnumSet.allOf(AffymetrixChipDAO.Attribute.class);
        if (!attrs.contains(RawDataService.Attribute.ANNOTATION)) {
            daoAttrs.remove(AffymetrixChipDAO.Attribute.CONDITION_ID);
        }
        if (!attrs.contains(RawDataService.Attribute.ASSAY_PIPELINE_SUMMARY)) {
            daoAttrs.removeAll(Set.of(AffymetrixChipDAO.Attribute.DISTINCT_RANK_COUNT,
                    AffymetrixChipDAO.Attribute.MAX_RANK, 
                    AffymetrixChipDAO.Attribute.PERCENT_PRESENT));
        }
        return log.traceExit(daoAttrs);
    }
    
    // generate dao attributes from service Attribute
    private static Set<AffymetrixProbesetDAO.Attribute> fromAttrsToAffyProbesetDAOAttrs(Set<RawDataService.Attribute> attrs) {
        log.traceEntry("{}", attrs);
        Set<AffymetrixProbesetDAO.Attribute> daoAttrs = EnumSet.allOf(AffymetrixProbesetDAO.Attribute.class);
        if (!attrs.contains(RawDataService.Attribute.RAWCALL_PIPELINE_SUMMARY)) {
            daoAttrs.remove(AffymetrixProbesetDAO.Attribute.NORMALIZED_SIGNAL_INTENSITY);
            daoAttrs.remove(AffymetrixProbesetDAO.Attribute.QVALUE);
            daoAttrs.remove(AffymetrixProbesetDAO.Attribute.RANK);
        }
        return log.traceExit(daoAttrs);
    }
        
///////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////// METHODS LOADING RNA-SEQ RAW DATA ////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Load rna-seq experiments from the database
     * 
     * @param rnaSeqExperimentIds       A {@code Collection} of {@code String} allowing to filter
     *                                  on RNA-Seq experiment IDs
     * @param rnaSeqLibraryIds          A {@code Collection} of {@code String} allowing to filter
     *                                  on RNA-Seq library IDs
     * @param attrs                     A {@code Collection}  of {@code RawDataService.Attribute}
     *                                  defining information to retrieve in the 
     *                                  {@code RnaSeqExperiment}s. 
     * @return  A {@code Stream} of {@code RnaSeqExperiment}s.
     *          If the {@code Stream} contains no element, it means that there were no data
     *          of this type for the requested parameters.
     */
    public Stream<RnaSeqExperiment> loadRnaSeqExperiments(Collection<String> rnaSeqExperimentIds,
            Collection<String> rnaSeqLibraryIds, Collection<RawDataService.Attribute> attrs) {
        log.traceEntry("{}, {}, {}", rnaSeqExperimentIds, rnaSeqLibraryIds, attrs);
        final Set<String> clonedExpIds =  Collections.unmodifiableSet(rnaSeqExperimentIds == null?
                new HashSet<String>(): new HashSet<String>(rnaSeqExperimentIds));
        final Set<String> clonedLibIds =  Collections.unmodifiableSet(rnaSeqLibraryIds == null?
                new HashSet<String>(): new HashSet<String>(rnaSeqLibraryIds));
        final Set<RawDataService.Attribute> clonedAttrs=  Collections.unmodifiableSet(attrs == null?
                EnumSet.allOf(RawDataService.Attribute.class) : EnumSet.copyOf(attrs));

        RNASeqExperimentDAO expDAO = this.rawDataService.getServiceFactory()
                .getDAOManager().getRnaSeqExperimentDAO();

        // return all datasources if required
        final Map<Integer,Source> dataSourceIdToDataSource = 
                clonedAttrs.contains(RawDataService.Attribute.DATASOURCE) == true?
                this.rawDataService.getServiceFactory().getSourceService().loadSourcesByIds(null):
                    new HashMap<>();

        // transform RnaSeqService Attributes to DAO attributes
        Set<RNASeqExperimentDAO.Attribute> daoAttrs = EnumSet.allOf(RNASeqExperimentDAO
                .Attribute.class);
        if (!clonedAttrs.contains(RawDataService.Attribute.DATASOURCE)) {
            daoAttrs.remove(RNASeqExperimentDAO.Attribute.DATA_SOURCE_ID);
        }

        // load experiments
        return log.traceExit(this.daoRawDataFilters.stream().map( filter -> {
            return expDAO.getExperiments(clonedExpIds, clonedLibIds, filter, 
                    daoAttrs).stream()
                    .map(to -> new RnaSeqExperiment( to.getId(), to.getName(), 
                            to.getDescription(), to.getDataSourceId() == null ? null: 
                                dataSourceIdToDataSource.get(to.getDataSourceId())))
                    .collect(Collectors.toSet());
            }).flatMap(e -> e.stream()));
    }

    /**
     * Load Annotated libraries from the database.
     * 
     * @param rnaSeqExperimentIds       A {@code Collection} of {@code String} allowing to filter
     *                                  on rna-seq experiment IDs
     * @param affymetrixChipIds         A {@code Collection} of {@code String} allowing to filter
     *                                  on affymetrix chip IDs
     * @param attrs                     A {@code Collection}  of {@code RawDataService.Attribute}
     *                                  defining information to retrieve in the 
     *                                  {@code MicroarrayExperiment}s.
     * @return  A {@code Stream} of {@code AffymetrixChip}s.
     *          If the {@code Stream} contains no element, it means that there were no data
     *          of this type for the requested parameters.
     */
    public Stream<RnaSeqLibraryAnnotatedSample> loadRnaSeqLibraryAnnotatedSample(
            Collection<String> rnaSeqExperimentIds, Collection<String> rnaSeqLibraryIds,
            Set<RawDataService.Attribute> attrs) {
        log.traceEntry("{}, {}, {}", rnaSeqExperimentIds, rnaSeqLibraryIds, attrs);
        final Set<String> clonedExpIds =  Collections.unmodifiableSet(rnaSeqExperimentIds == null?
                new HashSet<String>(): new HashSet<String>(rnaSeqExperimentIds));
        final Set<String> clonedLibIds =  Collections.unmodifiableSet(rnaSeqLibraryIds == null?
                new HashSet<String>(): new HashSet<String>(rnaSeqLibraryIds));
        final Set<RawDataService.Attribute> clonedAttrs=  Collections.unmodifiableSet(attrs == null?
                EnumSet.allOf(RawDataService.Attribute.class) : EnumSet.copyOf(attrs));

        // load Experiments if required
        final Map <String, RnaSeqExperiment> expIdToExperiments = 
                loadRnaSeqExperiments(clonedExpIds, clonedLibIds, clonedAttrs)
                .collect(Collectors.toMap(e -> e.getId(), e -> e));

        // load conditions based on rawDataFilters if required
        Map<Integer, RawDataCondition> condIdToCond = 
                clonedAttrs.contains(RawDataService.Attribute.ANNOTATION)?
                        rawDataConditionMap: null;

        //generate dao attributes based on RawDataServiceAttributes
        final Set<RNASeqLibraryAnnotatedSampleDAO.Attribute> daoAttrs = 
                fromAttrsToRnaSeqLibAnnotSampleDAOAttrs(clonedAttrs);
        
        // load libraries 
        Map<String, RNASeqLibraryTO> libIdToLibTO = daoRawDataFilters.stream()
                .map(filter -> {
                    return rawDataService.getServiceFactory().getDAOManager().getRnaSeqLibraryDAO()
                            .getRnaSeqLibraries( clonedExpIds, clonedLibIds, filter, null)
                            .stream().collect(Collectors.toMap(l -> l.getId(), l -> l));
                }).flatMap(l -> l.entrySet().stream())
                .collect(Collectors.toMap(l -> l.getKey(), l -> l.getValue()));
        return log.traceExit(this.daoRawDataFilters.stream().map( filter -> {
            return this.rawDataService.getServiceFactory().getDAOManager()
                    .getRnaSeqLibraryAnnotatedSampleDAO()
                    .getRnaSeqLibraryAnnotatedSamples(clonedExpIds, clonedLibIds, filter, daoAttrs)
            .stream().map(to -> {
                //TODO: To continue. 
                
                RawDataAnnotation annotation = clonedAttrs.contains(RawDataService.Attribute.ANNOTATION)?
                        new RawDataAnnotation(condIdToCond.get(to.getConditionId()), null, null, null):
                            null;
                RnaSeqLibraryPipelineSummary pipelineSummary = clonedAttrs.contains(RawDataService
                        .Attribute.ASSAY_PIPELINE_SUMMARY)?
                        new RnaSeqLibraryPipelineSummary( to.getMeanAbundanceRefIntergenicDistribution(),
                                to.getSdAbundanceRefIntergenicDistribution(), to.getpValueThreshold(),
                                to.getAllReadCount(), to.getAllUMIsCount(), to.getMappedReadCount(),
                                to.getMappedUMIsCount(), to.getMinReadLength(), to.getMaxReadLength(),
                                to.getMaxRank(), to.getDistinctRankCount()):
                            null;
                return new RnaSeqLibraryAnnotatedSample( to.getLibraryId(),
                        expIdToExperiments.get(libIdToLibTO.get(to.getLibraryId()).getExperimentId()),
                        annotation, pipelineSummary, to.getBarcode(), to.getGenotype());
            }).collect(Collectors.toSet());
        }).flatMap(c -> c.stream()));
    }

    // generate dao attributes from service Attribute
    private static Set<RNASeqLibraryAnnotatedSampleDAO.Attribute> fromAttrsToRnaSeqLibAnnotSampleDAOAttrs(
            Set<RawDataService.Attribute> attrs) {
        log.traceEntry("{}", attrs);
        Set<RNASeqLibraryAnnotatedSampleDAO.Attribute> daoAttrs = 
                EnumSet.allOf(RNASeqLibraryAnnotatedSampleDAO.Attribute.class);
        if (!attrs.contains(RawDataService.Attribute.ASSAY_PIPELINE_SUMMARY)) {
            daoAttrs.remove(RNASeqLibraryAnnotatedSampleDAO.Attribute.ABUNDANCE_THRESHOLD);
            daoAttrs.remove(RNASeqLibraryAnnotatedSampleDAO.Attribute.ABUNDANCE_UNIT);
            daoAttrs.remove(RNASeqLibraryAnnotatedSampleDAO.Attribute.ALL_GENES_PERCENT_PRESENT);
            daoAttrs.remove(RNASeqLibraryAnnotatedSampleDAO.Attribute.ALL_READ_COUNT);
            daoAttrs.remove(RNASeqLibraryAnnotatedSampleDAO.Attribute.ALL_UMIS_COUNT);
            daoAttrs.remove(RNASeqLibraryAnnotatedSampleDAO.Attribute.DISTINCT_RANK_COUNT);
            daoAttrs.remove(RNASeqLibraryAnnotatedSampleDAO.Attribute.INTERGENIC_REGION_PERCENT_PRESENT);
            daoAttrs.remove(RNASeqLibraryAnnotatedSampleDAO.Attribute.MAPPED_READ_COUNT);
            daoAttrs.remove(RNASeqLibraryAnnotatedSampleDAO.Attribute.MAPPED_UMIS_COUNT);
            daoAttrs.remove(RNASeqLibraryAnnotatedSampleDAO.Attribute.MAX_RANK);
            daoAttrs.remove(RNASeqLibraryAnnotatedSampleDAO.Attribute.MAX_READ_LENGTH);
            daoAttrs.remove(RNASeqLibraryAnnotatedSampleDAO.Attribute.MEAN_ABUNDANCE_REF_INTERGENIC_DISCTRIBUTION);
            daoAttrs.remove(RNASeqLibraryAnnotatedSampleDAO.Attribute.MIN_READ_LENGTH);
            daoAttrs.remove(RNASeqLibraryAnnotatedSampleDAO.Attribute.PROTEIN_CODING_GENES_PERCENT_PRESENT);
            daoAttrs.remove(RNASeqLibraryAnnotatedSampleDAO.Attribute.PVALUE_THRESHOLD);
            daoAttrs.remove(RNASeqLibraryAnnotatedSampleDAO.Attribute.SD_ABUNDANCE_REF_INTERGENIC_DISCTRIBUTION);
            daoAttrs.remove(RNASeqLibraryAnnotatedSampleDAO.Attribute.TMM_FACTOR);
        }
        
        if (!attrs.contains(RawDataService.Attribute.ANNOTATION)) {
            daoAttrs.remove(RNASeqLibraryAnnotatedSampleDAO.Attribute.CONDITION_ID);
        }
        return log.traceExit(daoAttrs);
    }

    public Set<RawDataFilter> getRawDataFilters() {
        return this.rawDataFilters;
    }

    //hashCode/equals do not use the RawDataService
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((rawDataFilters == null) ? 0 : rawDataFilters.hashCode());
        return result;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        RawDataLoader other = (RawDataLoader) obj;
        if (rawDataFilters == null) {
            if (other.rawDataFilters != null) {
                return false;
            }
        } else if (!rawDataFilters.equals(other.rawDataFilters)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("RawDataLoader [rawDataFilters=").append(rawDataFilters)
               .append("]");
        return builder.toString();
    }

}
