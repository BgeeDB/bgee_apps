package org.bgee.model.expressiondata.rawdata;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.TestAncestor;
import org.bgee.model.dao.api.expressiondata.call.ConditionDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.DAORawDataConditionFilter;
import org.bgee.model.dao.api.expressiondata.rawdata.DAORawDataFilter;
import org.bgee.model.dao.api.expressiondata.rawdata.RawDataConditionDAO.RawDataConditionTOResultSet;
import org.bgee.model.dao.api.gene.GeneDAO.GeneTOResultSet;
import org.bgee.model.expressiondata.call.ConditionTest;
import org.bgee.model.expressiondata.rawdata.RawDataProcessedFilter.RawDataProcessedFilterConditionPart;
import org.bgee.model.expressiondata.rawdata.RawDataProcessedFilter.RawDataProcessedFilterGeneSpeciesPart;
import org.bgee.model.expressiondata.rawdata.RawDataProcessedFilter.RawDataProcessedFilterInvariablePart;
import org.bgee.model.gene.GeneFilter;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Test {@link RawDataService}.
 *
 * @author Frederic Bastian
 * @version Bgee 15.1 May 2024
 * @since Bgee 15.0 Sep 2022
 */
public class RawDataServiceTest extends TestAncestor {
    private final static Logger log = LogManager.getLogger(ConditionTest.class.getName());

    @Override
    protected Logger getLogger() {
        return log;
    }

    @Test
    @Ignore
    public void shouldLoadRawDataLoader() {
        // *********************
        // PREPARE MOCK OBJECTS
        // *********************
        this.whenSpeciesDAOGetSpeciesByIds();
        this.whenGeneDAOGetGeneBioTypes();
        this.whenGetAnatEntityOntology();
        this.whenGetDevStageOntology();
        this.whenSourceServiceGetSources();

        //Genes
        //The RawDataService will only retrieve genes that are specifically requested,
        //not all genes of a species, therefore we only retrieve "geneId1", "geneId2"
        GeneTOResultSet geneTORS = getMockResultSet(GeneTOResultSet.class,
                List.of(GENE_TOS.get(1), GENE_TOS.get(2)));
        when(this.geneDAO.getGenesBySpeciesAndGeneIds(Map.of(1, Set.of("geneId1", "geneId2"))))
                .thenReturn(geneTORS);

        //Raw data conditions
        RawDataConditionTOResultSet condTORS = getMockResultSet(RawDataConditionTOResultSet.class,
                List.of(RAW_DATA_COND_TOS.get(6)));
        //********************EXPECTED DAORawDataConditionFilter
        DAORawDataConditionFilter daoCondFilter = new DAORawDataConditionFilter(Set.of(2),
                Set.of("anatId2", "anatId3", "anatId4"), Set.of("stageId3", "stageId4"),
                Set.of("cellTypeId2", "cellTypeId3", "cellTypeId4"),
                null, Set.of(ConditionDAO.STRAIN_ROOT_ID));
        //********************
        when(this.rawDataConditionDAO.getRawDataConditionsFromRawConditionFilters(
                collectionEq(Set.of(daoCondFilter)), anyCollection()))
        .thenReturn(condTORS);

        when(this.anatEntityService.loadAnatEntities(
                collectionEq(Set.of(2)), anyBoolean(), collectionEq(Set.of(
                        RAW_DATA_COND_TOS.get(6).getAnatEntityId(),
                        RAW_DATA_COND_TOS.get(6).getCellTypeId())),
                anyBoolean()))
        .thenReturn(Stream.of(ANAT_ENTITIES.get(RAW_DATA_COND_TOS.get(6).getAnatEntityId()),
                ANAT_ENTITIES.get(RAW_DATA_COND_TOS.get(6).getCellTypeId())));

        when(this.devStageService.loadDevStages(
                collectionEq(Set.of(2)), anyBoolean(), collectionEq(Set.of(
                        RAW_DATA_COND_TOS.get(6).getStageId())),
                anyBoolean()))
        .thenReturn(Stream.of(DEV_STAGES.get(RAW_DATA_COND_TOS.get(6).getStageId())));

        // *********************
        // TEST
        // *********************
        //Species with ID 3 will be the only one with no gene filter nor condition parameters,
        //for which all data are requested
        RawDataFilter filter = new RawDataFilter(
                Set.of(new GeneFilter(1, Set.of("geneId1", "geneId2")), new GeneFilter(2),
                        new GeneFilter(3)),
                Set.of(new RawDataConditionFilter(2, Set.of("anatId2", "anatId3"),
                        Set.of("stageId3"), Set.of("cellTypeId2", "cellTypeId3"),
                        Set.of(ConditionDAO.SEX_ROOT_ID), Set.of(ConditionDAO.STRAIN_ROOT_ID),
                        true, true, true, true, false))
                );

        RawDataService service = new RawDataService(this.serviceFactory);
        RawDataLoader actualLoader = service.loadRawDataLoader(filter);

        RawDataProcessedFilter expectedPrepProcessedInfo = new RawDataProcessedFilter(
                filter,
                Set.of(
                        new DAORawDataFilter(Set.of(1, 2), null),
                        new DAORawDataFilter(Set.of(3), null, null, null),
                        new DAORawDataFilter(null, Set.of(6))
                        ),
                new RawDataProcessedFilterGeneSpeciesPart(
                        filter.getGeneFilters(),
                        GENES.entrySet().stream().filter(e -> Set.of("geneId1", "geneId2").contains(
                                e.getValue().getGeneId()) && e.getValue().getSpecies().getId().equals(1))
                        .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue())),
                        SPECIES),
                new RawDataProcessedFilterConditionPart(
                        filter.getConditionFilters(),
                        RAW_DATA_CONDS.entrySet().stream().filter(e -> e.getKey().equals(6))
                        .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()))),
                new RawDataProcessedFilterInvariablePart(GENE_BIO_TYPES, SOURCES));
        assertEquals(expectedPrepProcessedInfo, actualLoader.getRawDataProcessedFilter());
    }
}