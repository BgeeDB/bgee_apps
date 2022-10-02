package org.bgee.model.expressiondata.rawdata;

import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.TestAncestor;
import org.bgee.model.dao.api.expressiondata.ConditionDAO;
import org.bgee.model.dao.api.gene.GeneDAO.GeneTOResultSet;
import org.bgee.model.expressiondata.ConditionTest;
import org.bgee.model.gene.GeneFilter;
import org.junit.Test;

/**
 * Test {@link RawDataService}.
 *
 * @author Frederic Bastian
 * @version Bgee 15.0 Sep 2022
 * @since Bgee 15.0 Sep 2022
 */
public class RawDataServiceTest extends TestAncestor {
    private final static Logger log = LogManager.getLogger(ConditionTest.class.getName());

    @Override
    protected Logger getLogger() {
        return log;
    }

    @Test
    public void shouldLoadRawDataLoader() {
        this.loadMockObjects();
        this.whenSpeciesDAOGetSpeciesByIds();
        this.whenGeneDAOGetGeneBioTypes();

        //The RawDataService will only retrieve genes that are specifically requested,
        //not all genes of a species, therefore we only retrieve "geneId1", "geneId2"
        GeneTOResultSet geneTORS = getMockResultSet(GeneTOResultSet.class,
                List.of(GENE_TOS.get(1), GENE_TOS.get(2)));
        when(this.geneDAO.getGenesBySpeciesAndGeneIds(Map.of(1, Set.of("geneId1", "geneId2"))))
                .thenReturn(geneTORS);

        RawDataFilter filter = new RawDataFilter(
                Set.of(new GeneFilter(1, Set.of("geneId1", "geneId2")), new GeneFilter(2),
                        new GeneFilter(3)),
                Set.of(new RawDataConditionFilter(2, Set.of("anatId1", "anatId2"),
                        Set.of("stageId1", "stageId2"), Set.of("cellTypeId1", "cellTypeId2"),
                        Set.of(ConditionDAO.SEX_ROOT_ID), Set.of(ConditionDAO.STRAIN_ROOT_ID),
                        true, true, true, true, false)),
                null
                );
        RawDataService service = new RawDataService(this.serviceFactory);
        service.loadRawDataLoader(filter);
    }
}
